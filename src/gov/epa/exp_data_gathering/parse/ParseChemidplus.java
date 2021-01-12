package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.RecordChemidplus.PhysicalPropertyRecord;
import gov.epa.exp_data_gathering.parse.RecordChemidplus.ToxicityRecord;


public class ParseChemidplus extends Parse {

	String recordTypeToParse;
	Hashtable<String, Double> htDensity = new Hashtable<String, Double>(); // density look up table, densities in g/ml
	
	public ParseChemidplus(String recordTypeToParse) {
		sourceName = ExperimentalConstants.strSourceChemidplus;
		this.recordTypeToParse = recordTypeToParse;
		super.init();
		fileNameSourceExcel=null;
		folderNameWebpages=null;
		folderNameExcel=null;
		String toxNote = recordTypeToParse.toLowerCase().contains("tox") ? " Toxicity" : "";
		fileNameJSON_Records = sourceName +toxNote + " Original Records.json";
		fileNameFlatExperimentalRecords = sourceName +toxNote + " Experimental Records.txt";
		fileNameFlatExperimentalRecordsBad = sourceName +toxNote + " Experimental Records-Bad.txt";
		fileNameJsonExperimentalRecords = sourceName +toxNote + " Experimental Records.json";
		fileNameJsonExperimentalRecordsBad = sourceName +toxNote + " Experimental Records-Bad.json";
		fileNameExcelExperimentalRecords = sourceName +toxNote + " Experimental Records.xlsx";
		loadDensityData();

	}

	class UniqueValues {
		ArrayList<String> needDensity = new ArrayList<>();
		ArrayList<String> needMolecularWeight = new ArrayList<>();
	}

	/**
	 * Parses HTML entries, either in zip folder or database, to RecordLookChem objects, then saves them to a JSON file
	 */
	@Override
	protected void createRecords() {
		String databasePath = databaseFolder+File.separator+sourceName+"_raw_html.db";		

		RecordChemidplus rc=new RecordChemidplus();
		Vector<RecordChemidplus> records=rc.parseWebpagesInDatabase(databasePath);
		writeOriginalRecordsToFile(records);
	}
	
	
	/**
	 * Loads density values and stores in a hashtable
	 */
	private void loadDensityData() {

		ArrayList<String> lines = gov.epa.ghs_data_gathering.Utilities.Utilities
				.readFileToArray(mainFolder+File.separator +"density.txt");

		for (int i = 1; i < lines.size(); i++) {// first line is header
			// System.out.println(lines.get(i));
			String[] vals = lines.get(i).split("\t");

			String CAS = vals[0];
			String strDensity = vals[1];

			htDensity.put(CAS, Double.parseDouble(strDensity));
		}

		// System.out.println(htDensity.get("7487-28-7"));

	}
	
	/**
	 * Reads the JSON file created by createRecords() and translates it to an ExperimentalRecords object
	 */
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordChemidplus[] recordsChemidplus = gson.fromJson(new FileReader(jsonFile), RecordChemidplus[].class);
			
			UniqueValues uv = new UniqueValues();
			
			for (int i = 0; i < recordsChemidplus.length; i++) {
				RecordChemidplus r = recordsChemidplus[i];
				addExperimentalRecords(r,recordsExperimental,uv);
			}
			
			DataRemoveDuplicateExperimentalValues d=new DataRemoveDuplicateExperimentalValues();	
			d.removeDuplicatesByComboID(recordsExperimental);//TODO maybe handle elsewhere?

			
			System.out.println("Created "+recordsExperimental.size()+" Experimental Records");
//			System.out.println(recordsExperimental.get(100).toJSON());

			printValues(uv);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}	
	
	private void addExperimentalRecords(RecordChemidplus r, ExperimentalRecords recordsExperimental,UniqueValues uv) {
		if (recordTypeToParse.toLowerCase().contains("tox")) {
			addToxicityExperimentalRecords(r,recordsExperimental,uv);
		} else {
			addPhyschemExperimentalRecords(r,recordsExperimental,uv);
		}
	}
	
	private void addToxicityExperimentalRecords(RecordChemidplus r, ExperimentalRecords recordsExperimental,UniqueValues uv) {
		ArrayList<String> okSpecies = new ArrayList<String>();
		okSpecies.add("mouse");// 27796
		okSpecies.add("rat");// 13124
		okSpecies.add("rabbit");// 1089
		okSpecies.add("guinea pig");// 970
				
		for (int i=0;i<r.ToxicityRecords.size();i++) {

			ToxicityRecord tr=r.ToxicityRecords.get(i);

			if (!okSpecies.contains(tr.Organism)) continue;
			
			boolean cond1=tr.Route.contentEquals("inhalation") && tr.TestType.contentEquals("LC50");
			boolean cond2=tr.Route.contentEquals("oral") && tr.TestType.contentEquals("LD50");
			boolean cond3=tr.Route.contentEquals("skin") && tr.TestType.contentEquals("LD50");
			
			if (cond1 || cond2 || cond3) {
				
				ExperimentalRecord er=new ExperimentalRecord();		
				er.casrn=r.CASRegistryNumber;
				er.chemical_name=r.NameOfSubstance;
								
				if (er.chemical_name.contains("[")) {
					er.chemical_name=er.chemical_name.substring(0,er.chemical_name.indexOf("[")).trim();
//					System.out.println(er.chemical_name);
				}
				
				er.smiles=r.Smiles;			
				er.source_name=sourceName;
				er.url=r.url;
				
				double MW=0;				
				if (r.Molecular_Weight!=null) MW=Double.parseDouble(r.Molecular_Weight);
				
				boolean parseOK=parseAndConvertUniqueMeasurements(er, tr,MW,uv);
				
				if (parseOK && er.property_value_numeric_qualifier==null) er.keep=true;
				else er.keep=false;
				
//				System.out.println(er.property_value_numeric_qualifier.isEmpty()+"\t"+parseOK);
				
				if (er.property_value_numeric_qualifier!=null && !er.property_value_numeric_qualifier.isEmpty()) 
					er.reason="Has numeric qualifier";										
				if (er.property_value_point_estimate_final==null) 
					er.reason="point_estimate_final=null";														
//				System.out.println(er.toJSON());			
				
				er.property_name=tr.Organism.replace(" ", "_")+"_"+tr.Route+"_"+tr.TestType;
				er.property_name=er.property_name.trim();
				
				recordsExperimental.add(er);			
				
//				System.out.println(recordsExperimental.size());

			}
					
		}
	}
	
	private void addPhyschemExperimentalRecords(RecordChemidplus r, ExperimentalRecords recordsExperimental,UniqueValues uv) {
				
		for (int i=0;i<r.PhysicalProperties.size();i++) {

			PhysicalPropertyRecord pr=r.PhysicalProperties.get(i);

			ExperimentalRecord er=new ExperimentalRecord();		
			er.casrn=r.CASRegistryNumber;
			er.chemical_name=r.NameOfSubstance;
							
			if (er.chemical_name.contains("[")) {
				er.chemical_name=er.chemical_name.substring(0,er.chemical_name.indexOf("[")).trim();
//					System.out.println(er.chemical_name);
			}
			
			er.smiles=r.Smiles;			
			er.source_name=sourceName;
			er.url=r.url;
			er.date_accessed = r.date_accessed;
			
			double MW=0;				
			if (r.Molecular_Weight!=null) MW=Double.parseDouble(r.Molecular_Weight);
			
			boolean valid = false;
			if (pr.PhysicalProperty.equals("Boiling Point")) {
				er.property_name = ExperimentalConstants.strBoilingPoint;
				er.property_value_units_original = ExperimentalConstants.str_C;
				ParseUtilities.getNumericalValue(er, pr.Value, pr.Value.length(), false);
				valid = true;
			} else if (pr.PhysicalProperty.equals("Henry's Law Constant")) {
				er.property_name = ExperimentalConstants.strHenrysLawConstant;
				er.property_value_units_original = ExperimentalConstants.str_atm_m3_mol;
				if (pr.TempdegC!=null && !pr.TempdegC.isBlank()) { er.temperature_C = Double.parseDouble(pr.TempdegC); }
				ParseUtilities.getNumericalValue(er, pr.Value, pr.Value.length(), false);
				valid = true;
			} else if (pr.PhysicalProperty.equals("log P (octanol-water)")) {
				er.property_name = ExperimentalConstants.strLogKow;
				ParseUtilities.getNumericalValue(er, pr.Value, pr.Value.length(), false);
				valid = true;
			} else if (pr.PhysicalProperty.equals("Melting Point")) {
				er.property_name = ExperimentalConstants.strMeltingPoint;
				er.property_value_units_original = ExperimentalConstants.str_C;
				ParseUtilities.getNumericalValue(er, pr.Value, pr.Value.length(), false);
				valid = true;
			} else if (pr.PhysicalProperty.equals("pKa Dissociation Constant")) {
				er.property_name = ExperimentalConstants.str_pKA;
				if (pr.TempdegC!=null && !pr.TempdegC.isBlank()) { er.temperature_C = Double.parseDouble(pr.TempdegC); }
				ParseUtilities.getNumericalValue(er, pr.Value, pr.Value.length(), false);
				valid = true;
			} else if (pr.PhysicalProperty.equals("Vapor Pressure")) {
				er.property_name = ExperimentalConstants.strVaporPressure;
				er.property_value_units_original = ExperimentalConstants.str_mmHg;
				if (pr.TempdegC!=null && !pr.TempdegC.isBlank()) { er.temperature_C = Double.parseDouble(pr.TempdegC); }
				ParseUtilities.getNumericalValue(er, pr.Value, pr.Value.length(), false);
				valid = true;
			} else if (pr.PhysicalProperty.equals("WaterSolubility")) {
				er.property_name = ExperimentalConstants.strWaterSolubility;
				er.property_value_units_original = ExperimentalConstants.str_mg_L;
				if (pr.TempdegC!=null && !pr.TempdegC.isBlank()) { er.temperature_C = Double.parseDouble(pr.TempdegC); }
				ParseUtilities.getNumericalValue(er, pr.Value, pr.Value.length(), false);
				valid = true;
			}
			
			if (valid) {
				if (pr.Source.toLowerCase().contains("est")) {
					er.updateNote(ExperimentalConstants.str_est);
					er.keep = false;
					er.reason = "Estimated";
				}
				
				if (!ParseUtilities.hasIdentifiers(er)) {
					er.keep = false;
					er.reason = "No identifiers";
				}
				
				RecordFinalizer.finalizeRecord(er);
				
				recordsExperimental.add(er);	
			}	
		}
	}

	private boolean parseAndConvertUniqueMeasurements(ExperimentalRecord er,ToxicityRecord tr,double MW,UniqueValues uv) {
	
		// Example bad records:
		// 12321-44-7 LD50 units/kg => unknown units
		// 103451-84-9 units iu/kg => unknown units
		// 31282-04-9 LD50 units/kg => unknown units
		// 108-42-9 LD50 ppm => concentration units
		// 103112-35-2 LD50 mg/m3 => concentration units
		// 143-18-0 LC50 mg/kg ==> dose units

//		er.property_value_units_final
		
		er.property_value_string=tr.NormalizedDose;
		
		
//		er.property_value_numeric_qualifier = "";
		
		// >=, \u2265
		// <=, \u2264
		if (tr.ReportedDose.contains("<") || tr.ReportedDose.contains(">") || tr.ReportedDose.contains("\u2264")
				|| tr.ReportedDose.contains("\u2265")) {
			er.property_value_numeric_qualifier = tr.ReportedDose.substring(0, 1);
		}

		if (tr.NormalizedDose.matches(".*[a-z].*")) {
			// finding first alphabetic
			Pattern p = Pattern.compile("\\p{Alpha}");
			Matcher m = p.matcher(tr.NormalizedDose);

			if (m.find()) {
				er.property_value_point_estimate_final = Double.parseDouble(tr.NormalizedDose.substring(0, m.start()));
				er.property_value_units_final = tr.NormalizedDose.substring(m.start(), tr.NormalizedDose.length());												
				
				er.property_value_point_estimate_original=er.property_value_point_estimate_final;
				er.property_value_units_original=er.property_value_units_final;
			}
		} else {
			er.property_value_point_estimate_final = Double.parseDouble(tr.NormalizedDose);
			er.property_value_units_final = "?";
			System.out.println(er.casrn + "\tunknown units for normalized dose");
			return false;
		}
		
			

		if (tr.TestType.equals("LC50")) {			
//			System.out.println(tr.TestType+"\t"+sr.valueMass+"\t"+er.property_value_units_final);
			
			if (tr.ReportedDose.contains("H")) {	
								
				String strHour=tr.ReportedDose.substring(tr.ReportedDose.lastIndexOf("/")+1,tr.ReportedDose.length()-1);
				
				try {
					double hour=Double.parseDouble(strHour);					
					//use haber's rule that C*t=k (see DOI: 10.1093/toxsci/kfg213 that shows this might not be great approx)					
					er.property_value_point_estimate_final*=hour/4.0;
					er.note="Duration = "+hour+" H";
					
					if (hour!=4.0) {
						er.note+="; experimental value adjusted to 4 H using Haber's law (conc*time=constant)";
					}
				
				} catch (Exception ex) {
					System.out.println("Error parsing "+tr.ReportedDose);
				}
//				System.out.println(hour);				
			}
			
//			20000ppm/10H (20000ppm)
			

			if (er.property_value_units_final.equals("mg/m3")) {
				// 1 mg/L= 1000 mg/m3
				// converting mg/L to mg/m3
				er.property_value_point_estimate_final /= 1000.0;
				er.property_value_units_final = "mg/L";

			} else if (er.property_value_units_final.equals("ppm")) {
				// To convert concentrations in air (at 25 °C) from ppm to mg/m3:
				// mg/m3 = (ppm) × (molecular weight of the compound)/(24.45).
				// 1 milligram per cubic meter ( mg/m3 ) = 0.0010 milligrams per liter ( mg/l ).
				// So mg/L = ((ppm) × (molecular weight of the compound)/(24.45))*0.001

					
				if (MW==0) {
					if (!uv.needMolecularWeight.contains(er.casrn)) {
						uv.needMolecularWeight.add(er.casrn);
						System.out.println(er.casrn+"\tneed MW");
						return false;
					}
				}
				
				er.property_value_point_estimate_final = (er.property_value_point_estimate_final * (MW) / (24.45)) * .001;
				er.property_value_units_final = "mg/L";

//				System.out.println(chemical.CAS+"\t"+sr.valueMass);

			} else if (er.property_value_units_final.equals("mL/m3")) {
				if (htDensity.get(er.casrn) == null) {
					if (!uv.needDensity.contains(er.casrn)) {
						uv.needDensity.add(er.casrn);
					}
					return false;

				} else {
					double density = htDensity.get(er.casrn);
					er.property_value_point_estimate_final *= density; // (ml/m3)*(density g/ml)*(1000 mg/g)*(1 m3/1000 L)
					// System.out.println(dose+" mg/kg (converted from mL/m3)");
					er.property_value_units_final = "mg/L";
				}
			} else if (er.property_value_units_final.equals("mg/L")) {
				// we are ok
			} else {
				// System.out.println(chemical.CAS + "\t" + tr.TestType + "\t" +
				// er.property_value_units_final);
				return false;
			}
		} else if (tr.TestType.equals("LD50")) {

			if (er.property_value_units_final.equals("mL/kg")) {
				// Need density to convert from mL to mg. Need to print a list of CAS numbers
				// that we need density for.

				if (htDensity.get(er.casrn) == null) {
					if (!uv.needDensity.contains(er.casrn)) {
						uv.needDensity.add(er.casrn);
					}
					return false;

				} else {
					double density = htDensity.get(er.casrn);
					er.property_value_point_estimate_final *= density * 1000.0;
					er.property_value_units_final = "mg/kg";
				}
			} else if (er.property_value_units_final.equals("iu/kg")) {
				// need concentration to convert iu to mg
				// System.out.println(chemical.CAS + "\tunits iu/kg");
				return false;
			} else if (er.property_value_units_final.equals("mg/kg")) {
				// we are ok
			} else {
				// System.out.println(chemical.CAS + "\t" + tr.TestType + "\t" +
				// er.property_value_units_final);
				return false;
			}

		} else {
			// System.out.println(chemical.CAS + "\t" + tr.TestType + "\t" +
			// er.property_value_units_final);
			return false;
		}

		return true;

	}
	
	private void printValues(UniqueValues uv) {

		if (uv.needDensity.size()>0) {
			System.out.println("Need density to convert from ml to mg/kg:");
			for (String s : uv.needDensity) {
				System.out.println(s);
			}
			System.out.println();
		}


		if (uv.needMolecularWeight.size()>0) {
			System.out.println("Need molecular weight to convert from ppm to mg/L:");
			for (String s : uv.needMolecularWeight) {
				System.out.println(s);
			}
		}
	}

	public static void main(String[] args) {
		ParseChemidplus p = new ParseChemidplus("physchem");
		p.createFiles();
	}

	
}
