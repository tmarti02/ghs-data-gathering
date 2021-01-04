package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.api.ExperimentalConstants;

public class ParseChemBL extends Parse {

	public ParseChemBL() {
		sourceName = ExperimentalConstants.strSourceChemBL;
		this.init();
	}
	
	@Override
	protected void createRecords() {
		Vector<RecordChemBL> records = RecordChemBL.parseJSONsInDatabase();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordChemBL[] recordsChemBL = gson.fromJson(new FileReader(jsonFile), RecordChemBL[].class);
			
			for (int i = 0; i < recordsChemBL.length; i++) {
				RecordChemBL rec = recordsChemBL[i];
				addExperimentalRecords(rec,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return recordsExperimental;
	}
	
	/**
	 * The most important method of the ParseChemBL class, populates fields of experimentalRecord objects with data from RecordChemBL objects
	 * @param cbr
	 * @param records
	 */
	private void addExperimentalRecords(RecordChemBL cbr,ExperimentalRecords records) {
		ExperimentalRecord er = new ExperimentalRecord();
		er.date_accessed = cbr.date_accessed;
		er.keep = true;
		er.source_name = ExperimentalConstants.strSourceChemBL;
		er.chemical_name = cbr.moleculePrefName;
		er.smiles = cbr.canonicalSmiles;
		// Assay description will print in the "measurement method" field of the final ExperimentalRecord so you can check entries
		er.measurement_method = cbr.assayDescription;
		er.url = cbr.url;
		er.property_value_string = cbr.standardRelation + cbr.standardValue + (cbr.standardUnits==null ? "" : (" "+cbr.standardUnits));
		String desc = cbr.assayDescription.toLowerCase();
		if (!cbr.assayType.equals("P")) {
			return; // Don't parse non-physicochemical assays
		} else if (cbr.standardType.toLowerCase().equals("tm") && desc.contains("melting point")) {
			// This is complete - there are few records for melting point and they are easy to parse
			er.property_name = ExperimentalConstants.strMeltingPoint;
			if (cbr.standardRelation!=null && !cbr.standardRelation.isBlank() && !cbr.standardRelation.equals("=")) {
				er.property_value_numeric_qualifier = cbr.standardRelation;
			}
			if (cbr.standardValue!=null && !cbr.standardValue.isBlank()) {
				er.property_value_point_estimate_original = Double.parseDouble(cbr.standardValue);
				if (cbr.standardUnits.equals("degrees C")) { er.property_value_units_original = ExperimentalConstants.str_C; }
			}
		} else if (cbr.standardType.toLowerCase().equals("pka")) {
			// There is a lot of weirdness with pKa that I have not even started trying to address yet
			// pKa of different functional groups, acidic vs. basic pKa, alternative solvents, etc.
			// Talk to Tony & Todd about which records to keep, then write if/then & regex to eliminate or flag entries appropriately
			er.property_name = ExperimentalConstants.str_pKA;
			if (cbr.standardRelation!=null && !cbr.standardRelation.isBlank() && !cbr.standardRelation.equals("=")) {
				er.property_value_numeric_qualifier = cbr.standardRelation;
			}
			if (cbr.standardValue!=null && !cbr.standardValue.isBlank()) {
				er.property_value_point_estimate_original = Double.parseDouble(cbr.standardValue);
			}
			sensiblePkaCheck(cbr.standardValue, er); // puts records with pka outside the -10 to 25 range into bad
			functionalGroupAI(cbr.assayDescription, er);
		}	
			
			else if (cbr.standardType.toLowerCase().equals("solubility") && (desc.contains("water") || desc.contains("aq")) && cbr.standardUnits!=null ||
					desc.contains("buff") && cbr.standardUnits!=null || desc.contains("sal") && cbr.standardUnits!=null 
					&& !cbr.standardUnits.isBlank() && !desc.contains("buffer") && !desc.contains("acetate") && !desc.contains("dextrose") && !desc.contains("dmso")
				&& !desc.contains("octanol") && !desc.contains("glycine") && !desc.contains("arginine") && !desc.contains("acid") && !desc.contains("pbs")
				&& !desc.contains("hcl") && !desc.contains("intestinal") && !desc.contains("triethanolamine") && !desc.contains("cyclodextrin")) {
			// I started on eliminating bad water solubility records (i.e. non-aqueous solubilities) but there are still more
			// Again - talk to Todd & Tony about what to keep, then add more if/then & regex to handle it
			// These if statements eliminate the records completely (they won't show up in Records-Bad) - is this what we want?
			er.property_name = ExperimentalConstants.strWaterSolubility;
			if (cbr.standardRelation!=null && !cbr.standardRelation.isBlank() && !cbr.standardRelation.equals("=")) {
				er.property_value_numeric_qualifier = cbr.standardRelation;
			}
			// ChemBL has lots of weird solubility units - I think I handled them all here, but double check
			if (cbr.standardValue!=null && !cbr.standardValue.isBlank()) {
				er.property_value_point_estimate_original = Double.parseDouble(cbr.standardValue);
				if (cbr.standardUnits.equals("ug.mL-1")) { er.property_value_units_original = ExperimentalConstants.str_ug_mL;
				} else if (cbr.standardUnits.equals("nM")) { er.property_value_units_original = ExperimentalConstants.str_nM;
				} else if (cbr.standardUnits.equals("%")) { er.property_value_units_original = ExperimentalConstants.str_pct;
				} else if (cbr.standardUnits.equals("ppm")) { er.property_value_units_original = ExperimentalConstants.str_ppm;
				} else if (cbr.standardUnits.equals("ppb")) { er.property_value_units_original = ExperimentalConstants.str_ppb;
				} else if (cbr.standardUnits.equals("mg/uL")) { er.property_value_units_original = ExperimentalConstants.str_g_mL;
				} else if (cbr.standardUnits.equals("10'-3mol/dm3")) { er.property_value_units_original = ExperimentalConstants.str_mM;
				} else if (cbr.standardUnits.equals("mg kg-1")) { er.property_value_units_original = ExperimentalConstants.str_mg_L;
				} else {
					er.keep = false;
					er.reason = "Bad data or units";
				}
			}
		} else if (cbr.standardType.toLowerCase().equals("logp") && desc.contains("octanol") && (desc.contains("water") || desc.contains("aq"))) {
			// Most restrictive criteria - description must explicitly say it is the octanol-water coefficient
			// Many partition coefficient records do not specify the solvents - can we assume it is octanol-water unless it says otherwise?
			er.property_name = ExperimentalConstants.strLogKow;
			if (cbr.standardRelation!=null && !cbr.standardRelation.isBlank() && !cbr.standardRelation.equals("=")) {
				er.property_value_numeric_qualifier = cbr.standardRelation;
			}
			if (cbr.standardValue!=null && !cbr.standardValue.isBlank()) {
				er.property_value_point_estimate_original = Double.parseDouble(cbr.standardValue);
			}
		} else {
			return;
		}
		
		// Finds pH values in assay description
		// I think this handles all cases in ChemBL, but double check
		Matcher pHMatcher = Pattern.compile("ph (value )?(of )?([-]?[ ]?[0-9]*\\.?[0-9]+)( to )?([-]?[ ]?[0-9]*\\.?[0-9]+)?").matcher(desc);
		if (pHMatcher.find()) {
			double min = Double.parseDouble(pHMatcher.group(3));
			er.pH = ParseUtilities.formatDouble(min);
			if (pHMatcher.group(5)!=null) {
				double max = Double.parseDouble(pHMatcher.group(5));
				er.pH += "-" + ParseUtilities.formatDouble(max);
			}
		}
		
		// Finds temperature values in assay description
		// I think this handles all cases in ChemBL, but double check
		Matcher tempMatcher = Pattern.compile("([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]?(\\+/- [0-9]*\\.?[0-9]+ )?deg(ree[s]? )?c").matcher(desc);
		if (tempMatcher.find()) {
			er.temperature_C = Double.parseDouble(tempMatcher.group(1).replaceAll(" ",""));
		}
		
		er.original_source_name = cbr.documentJournal + " " + cbr.documentYear;
		if (er.keep && !ParseUtilities.hasIdentifiers(er)) {
			er.keep = false;
			er.reason = "No identifiers";
		} else if (er.keep && er.property_value_point_estimate_original==null) {
			er.keep = false;
			er.reason = "Bad data or units";
		} else if (desc.contains("calculat") || desc.contains("logarithm of the sum of contributions")) {
			er.keep = false;
			er.reason = "Calculated";
		} else if (desc.contains("estimat")) {
			er.keep = false;
			er.reason = "Estimated";
		}
			else if (desc.contains("regression")) {
			er.keep = false;
			er.reason = "Regression calculation";
		}
			else if (desc.contains("extrapolat")) {
			er.keep = false;
			er.reason = "Extrapolated";
		}
		er.flag = false;
		RecordFinalizer.finalizeRecord(er);
		records.add(er);
	}
	
	public static void main(String[] args) {
		ParseChemBL p = new ParseChemBL();
		p.createFiles();
	}

	public static void sensiblePkaCheck(String standardVal, ExperimentalRecord er) {
		if ((!(standardVal == null)) && !standardVal.isBlank()) {
		if ((Double.parseDouble(standardVal) > 25 || Double.parseDouble(standardVal) < -10) ) {
			er.keep = false;
			er.reason = "nonsensical pka value";
		}
		}
	}
	
	public static void functionalGroupAI(String assayDescription, ExperimentalRecord er) {
		if (assayDescription.contains("carboxyl")) {
			er.updateNote("carboxyl");
		}	else if (assayDescription.contains("amino")) {
			er.updateNote("amino");
		} 	else if (assayDescription.contains("guanidine")) {
			er.updateNote("guanidine");
		}	else if (assayDescription.contains("oxime")) {
			er.updateNote("oxime");
		}	else if (assayDescription.contains("quinoline")) {
			er.updateNote("quinoline");
		}	else if (assayDescription.contains("NH group")) {
			er.updateNote("NHgroup");
		}	else if (assayDescription.contains("OH group")) {
			er.updateNote("OHgroup");
		}	else if (assayDescription.contains("sulfonamide")) {
			er.updateNote("sulfonamide");
		}	else if (assayDescription.contains("Amine")) {
			er.updateNote("amine");
		}	else if (assayDescription.contains("aliphatic nitrogen")) {
			er.updateNote("aliphaticnitrogen");
		}	else if (assayDescription.contains("hydroxyimine methyl acid")) {
			er.updateNote("hydroxyiminemethylacid");
		}	else if (assayDescription.contains("pyrophosphate")) {
			er.updateNote("tbd"); // TODO figure out what should go here
		}	else if (assayDescription.contains("formamidine")) {
			er.updateNote("formamidinering");
		}	else if (assayDescription.contains("imidazole")) {
			er.updateNote("imidazolering");
		}	else if (assayDescription.contains("pyridinium")) {
			er.updateNote("pyridinium");
		}	else if (assayDescription.contains("Ar-COOH")) {
			er.updateNote("Ar-COOH");
		}	else if (assayDescription.contains("P(O)O-OH")) {
			er.updateNote("P(O)O-OH");
		}	else if (assayDescription.contains("OH/C-ring")) {
			er.updateNote("OH/C-ring");
		}	else if (assayDescription.contains("N-1")) {
			er.updateNote("N-1");
		}	else if (assayDescription.contains("polyamino carboxylate")) {
			er.updateNote("polyamino carboxylate");
		}	else if (assayDescription.contains("benzimidazole")) {
			er.updateNote("benzimidazole");
		}	else if (assayDescription.contains("Phenolic")) { // sorted, this is where all the weird stuff starts showing up
			er.updateNote("phenolic");
		}	else if (assayDescription.contains("benzyl amine")) {
			er.updateNote("benzylamine");
		}	else if (assayDescription.contains("piperdine amine")) {
			er.updateNote("piperdineamine");
		}	else if (assayDescription.contains("heterocyclic component")) {
			er.updateNote("heterocyclic_component");
		}	else if (assayDescription.contains("of nitrogen -")) {
			er.updateNote("nitrogen");
		}	else if (assayDescription.contains("urea")) {
			er.updateNote("urea");
		}	else if (assayDescription.contains("Sugar COOH")) {
			er.updateNote("sugarCOOH");
		}	else if (assayDescription.contains("carboxylic acid")) {
			er.updateNote("carboxylic acid");
		}	else if (assayDescription.contains("sulfonamido")) {
			er.updateNote("sulfonamido");
			}
		
			
		}
}
	
