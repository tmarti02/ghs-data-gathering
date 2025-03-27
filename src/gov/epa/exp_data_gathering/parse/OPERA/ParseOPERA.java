package gov.epa.exp_data_gathering.parse.OPERA;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;
import gov.epa.api.ScoreRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.TextUtilities;

public class ParseOPERA extends Parse {

	
	String userName="tmarti02";
	
	public ParseOPERA() {
//		sourceName = ExperimentalConstants.strSourceOPERA29;
		sourceName = ExperimentalConstants.strSourceOPERA28;
		this.init();
//		this.writeFlatFile=true;
	}
	
	class CustomComparator implements Comparator<ExperimentalRecord> {
	    @Override
	    public int compare(ExperimentalRecord o1, ExperimentalRecord o2) {
	        String key1=o1.property_name+"\t"+o1.comboID;
	        String key2=o2.property_name+"\t"+o2.comboID;
	    	return key1.compareTo(key2);
	    }
	}

	
	@Override
	protected void createRecords() {
		
		if (!this.generateOriginalJSONRecords) return;
		
		System.out.println("enter create records");
		
		Vector<RecordOPERA> records=new Vector<>();

		Vector<RecordOPERA> recordsSDF = RecordOPERA.parseOperaSdfs(sourceName);
		System.out.println(recordsSDF.size());
		records.addAll(recordsSDF);
//		System.out.println(gson.toJson(recordsSDF));
		
		Vector<RecordOPERA> recordsCSV = RecordOPERA.parseOperaCSVs(sourceName);
		System.out.println(recordsCSV.size());
		records.addAll(recordsCSV);

		Vector<RecordOPERA> recordsCSV2 = RecordOPERA.parseOperaCSVs2(sourceName,true);
		System.out.println(recordsCSV2.size());
		records.addAll(recordsCSV2);
		
//		Hashtable<String,RecordOPERA>htSDF=new Hashtable<>();
//		Hashtable<String,RecordOPERA>htCSV=new Hashtable<>();
//		
//		for (RecordOPERA ro:recordsCSV) {
//			if(ro.dsstox_substance_id!=null)			
//				htCSV.put(ro.dsstox_substance_id, ro);
//			else {
////				System.out.println(ro.CAS+"\t"+ro.ChemicalName+"\tNo DTXSID\t"+ro.property_name);
//			}
//		}
		
//		for (RecordOPERA ro:recordsSDF) {
//			
//			if(ro.dsstox_substance_id!=null)			
//				htSDF.put(ro.dsstox_substance_id, ro);
//			else {
////				System.out.println(ro.CAS+"\t"+ro.ChemicalName+"\tNo DTXSID\t"+ro.property_name);
//			}
//		}
		
//		for (String dtxsid:htCSV.keySet()) {
//			RecordOPERA roCSV=htCSV.get(dtxsid);
//					
//			if(!htSDF.containsKey(dtxsid)) continue; 
//			RecordOPERA roSDF=htSDF.get(dtxsid);
//			
//			double diff= Math.abs(roCSV.property_value_original-roSDF.property_value_original)/roCSV.property_value_original*100;
//			
//			if(diff>1)//1%
//				System.out.println(dtxsid+"\t"+roCSV.property_value_original+"\t"+roSDF.property_value_original+"\t"+diff);
//		}
				
//		for (String dtxsid:htSDF.keySet()) {
//			RecordOPERA roSDF=htSDF.get(dtxsid);
//			if(!htCSV.containsKey(dtxsid)) {
//				System.out.println("Missing "+dtxsid+" in csv");
//				continue;
//			}
//
//		}
		
//		for (String dtxsid:htCSV.keySet()) {
//			RecordOPERA roCSV=htCSV.get(dtxsid);
//			if(!htSDF.containsKey(dtxsid)) {
//				System.out.println("Missing "+dtxsid+" in sdf");
//				continue;
//			}
//
//		}

		writeOriginalRecordsToFile(records);
	}
	
	/**
	 * Reads the JSON file created by createRecords() and translates it to an ExperimentalRecords object
	 */
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {

		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordOPERA> recordsOPERA = new ArrayList<RecordOPERA>();
			RecordOPERA[] tempRecords = null;
			
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordOPERA[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsOPERA.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					//Note need to look for indexOf .json and not . since can have . in the sourceName
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".json")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					System.out.println(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordOPERA[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsOPERA.add(tempRecords[i]);
					}
				}
			}
			
			System.out.println("recordsOPERA.size()="+recordsOPERA.size());
			
			Iterator<RecordOPERA> it = recordsOPERA.iterator();
			while (it.hasNext()) {
				RecordOPERA r = it.next();
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("recordsExperimental.size()="+recordsExperimental.size());
		
		for (ExperimentalRecord er:recordsExperimental) {
			er.setComboID("\t");
		}
		
		recordsExperimental.getRecordsByProperty();
		
		
		Collections.sort(recordsExperimental,new CustomComparator());
		return recordsExperimental;
	}
	
	/**
	 * populates experimentalrecord fields with data from the recordOPERA object.
	 * @param rs
	 * @param records
	 */
	private void addExperimentalRecords(RecordOPERA ro,ExperimentalRecords records) {
		//TODO make the pka experimentalrecords rely on getLogProperty like the logP ones do.
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
		Date date = new Date();  
		String strDate=formatter.format(date);
		String dayOnly = strDate.substring(0,strDate.indexOf(" "));

		if (ro.property_name.equals(ExperimentalConstants.str_pKA)) {
			
			if(ro.pKa_a!=null) {
				ExperimentalRecord er = ro.toExperimentalRecord(dayOnly,sourceName,ExperimentalConstants.str_pKAa);
				er.property_value_string = ro.pKa_a+" "+ExperimentalConstants.str_LOG_UNITS;
				er.property_value_point_estimate_original=Double.parseDouble(ro.pKa_a);
				splitRecords(records, er);
			}
			if(ro.pKa_b!=null) {
				ExperimentalRecord er = ro.toExperimentalRecord(dayOnly,sourceName,ExperimentalConstants.str_pKAb);
				er.property_value_string = ro.pKa_b+" "+ExperimentalConstants.str_LOG_UNITS;
				er.property_value_point_estimate_original=Double.parseDouble(ro.pKa_b);
				splitRecords(records, er);
			}
		} else if(ro.property_name.equals(ExperimentalConstants.strAR)) {
			
			if(ro.BD_potency_Exp!=null) {
				ExperimentalRecord er = ro.toExperimentalRecord(dayOnly,sourceName,ExperimentalConstants.str_ANDROGEN_RECEPTOR_BINDING);
				er.property_value_string = ro.BD_potency_Exp;
				er.property_value_qualitative=er.property_value_string;
				er.property_value_point_estimate_original=getPotencyNumeric(er.property_value_string);

				splitRecords(records, er);
			}
			
			if(ro.AG_Potency_Exp!=null) {
				ExperimentalRecord er = ro.toExperimentalRecord(dayOnly,sourceName,ExperimentalConstants.str_ANDROGEN_RECEPTOR_AGONIST);
				er.property_value_string = ro.AG_Potency_Exp;
				er.property_value_qualitative=er.property_value_string;
				er.property_value_point_estimate_original=getPotencyNumeric(er.property_value_string);

				splitRecords(records, er);
			}

			if(ro.AN_potency_Exp!=null) {
				ExperimentalRecord er = ro.toExperimentalRecord(dayOnly,sourceName,ExperimentalConstants.str_ANDROGEN_RECEPTOR_ANTAGONIST);
				er.property_value_string = ro.AN_potency_Exp;
				er.property_value_qualitative=er.property_value_string;
				er.property_value_point_estimate_original=getPotencyNumeric(er.property_value_string);

				splitRecords(records, er);
			}


		} else if(ro.property_name.equals(ExperimentalConstants.str_ESTROGEN_RECEPTOR_BINDING)) {
			if(ro.Potency_class_binding!=null) {
				ExperimentalRecord er = ro.toExperimentalRecord(dayOnly,sourceName,ro.property_name);
				er.property_value_string = ro.Potency_class_binding;
				er.property_value_qualitative=er.property_value_string;
				er.property_value_point_estimate_original=getPotencyNumeric(er.property_value_string);
				splitRecords(records, er);
			}
		} else if(ro.property_name.equals(ExperimentalConstants.str_ESTROGEN_RECEPTOR_AGONIST)) {
			if(ro.Potency_class_agonist!=null) {
				ExperimentalRecord er = ro.toExperimentalRecord(dayOnly,sourceName,ro.property_name);
				er.property_value_string = ro.Potency_class_agonist;
				er.property_value_qualitative=er.property_value_string;
				er.property_value_point_estimate_original=getPotencyNumeric(er.property_value_string);

				splitRecords(records, er);
			}
		} else if(ro.property_name.equals(ExperimentalConstants.str_ESTROGEN_RECEPTOR_ANTAGONIST)) {
			if(ro.Potency_class_antagonist!=null) {
				ExperimentalRecord er = ro.toExperimentalRecord(dayOnly,sourceName,ro.property_name);
				er.property_value_string = ro.Potency_class_antagonist;
				er.property_value_qualitative=er.property_value_string;
				er.property_value_point_estimate_original=getPotencyNumeric(er.property_value_string);
				splitRecords(records, er);
			}

//		} else if(ro.property_name.equals(ExperimentalConstants.strER)) {
//			
//			if(ro.Potency_class_binding!=null) {
//				ExperimentalRecord er = ro.toExperimentalRecord(dayOnly,sourceName,ExperimentalConstants.str_ESTROGEN_RECEPTOR_BINDING);
//				er.property_value_string = ro.Potency_class_binding;
//				splitRecords(records, er);
//			}
//			
//			if(ro.Potency_class_agonist!=null) {
//				ExperimentalRecord er = ro.toExperimentalRecord(dayOnly,sourceName,ExperimentalConstants.str_ESTROGEN_RECEPTOR_AGONIST);
//				er.property_value_string = ro.Potency_class_agonist;
//				splitRecords(records, er);
//			}
//
//			if(ro.Potency_class_antagonist!=null) {
//				ExperimentalRecord er = ro.toExperimentalRecord(dayOnly,sourceName,ExperimentalConstants.str_ESTROGEN_RECEPTOR_ANTAGONIST);
//				er.property_value_string = ro.Potency_class_antagonist;
//				splitRecords(records, er);
//			}
		} else if(ro.property_name.equals(ExperimentalConstants.strORAL_RAT_LD50)) {
			ExperimentalRecord er = ro.toExperimentalRecord(dayOnly,sourceName,ro.property_name);
			if(ro.CATMoS_LD50_str==null) return;
			er.property_category=ExperimentalConstants.strAcuteOralToxicity;
			
			//Use detailed code to get qualifier, min/max/point estimates: 
			TextUtilities.getNumericalValue(er, ro.CATMoS_LD50_str, ro.CATMoS_LD50_str.length(), false);
			er.property_value_string=ro.CATMoS_LD50_str+" "+ro.property_value_units_original;
			splitRecords(records, er);
			
		} else {
			ExperimentalRecord er = ro.toExperimentalRecord(dayOnly,sourceName,ro.property_name);
			er.property_value_point_estimate_original = ro.property_value_original;
			if (er.property_value_point_estimate_original != null && ro.property_value_units_original != null) {
				er.property_value_string = er.property_value_point_estimate_original.toString() + " " + ro.property_value_units_original;
			} else if (er.property_value_point_estimate_original != null) {
				er.property_value_string = er.property_value_point_estimate_original.toString();
			}
			splitRecords(records, er);
		}			
			
	}
	
	
	Double getPotencyNumeric(String potencyClass) {
		
		if(potencyClass.equals("Inactive")) return 0.0;
		if(potencyClass.equals("Active(very weak)")) return 0.25;
		if(potencyClass.equals("Active(weak)")) return 0.5;
		if(potencyClass.equals("Active(medium)")) return 0.75;
		if(potencyClass.equals("Active(strong)")) return 1.0;
		if(potencyClass.equals("Active(NA)")) return 0.625;
		
		System.out.println("Cant account for "+potencyClass);
		return null;
		
	}
	

	private void splitRecords(ExperimentalRecords records, ExperimentalRecord er) {
		
		
		if (er.dsstox_substance_id!=null) {
			
			String [] dtxsids=null;
			if(er.dsstox_substance_id.contains(",")) dtxsids=er.dsstox_substance_id.split(",");
			else dtxsids=er.dsstox_substance_id.split("\\|");	

			if(dtxsids.length>1) er.updateNote("originally a mixture");

			
//			if(er.dsstox_substance_id.equals("DTXSID8040222")) {
//				System.out.println("DTXSID8040222\t"+dtxsids.length+"\t"+er.property_name);
//			}
			
			//Need to clone it- otherwise we wont be able to map it to dsstox:
			for (String dtxsid:dtxsids)	{			
//					System.out.println(dtxsid);
				ExperimentalRecord erClone=(ExperimentalRecord) er.clone();
				erClone.dsstox_substance_id=dtxsid.trim();
				
				
				
				if(erClone.dsstox_substance_id.contains("?")) {
//						erClone.dsstox_substance_id=null;
//						System.out.println("Discarding:\n"+gson.toJson(erClone)+"\n");
//						System.out.println("Original:\n"+gson.toJson(er)+"\n");
//						System.out.println("********************************\n\n");
					continue;//discard since we already have a different record with a DTXSID
				} else {//void them out since can pull from dtxsid
					erClone.casrn=null;
					erClone.chemical_name=null;
					erClone.smiles=null;
				}
				
//				System.out.println("SID split record");
//				System.out.println(gson.toJson(erClone)+"\n****************\n");
				
				records.add(erClone);
				uc.convertRecord(erClone);
//					System.out.println(gson.toJson(erClone));
			}

		} else if (er.casrn!=null) {
			
//			System.out.println(er.casrn+"\t"+er.chemical_name+"\t"+er.smiles);

			String [] casrns=er.casrn.split("\\|");
			
			if(casrns.length>1) er.updateNote("originally a mixture");


			String [] names=null;
			if (er.chemical_name!=null) {
				names=er.chemical_name.split("\\|");	
			}
			
			String [] smiles=null;
			
			if(er.smiles!=null) smiles=er.smiles.split("\\|");
			
			//Need to clone it- otherwise we wont be able to map it to dsstox:
			for (int i=0;i<casrns.length;i++)	{			
//					System.out.println(dtxsid);
				
				ExperimentalRecord erClone=(ExperimentalRecord) er.clone();
				
				erClone.casrn=casrns[i].trim();
				
				if(names!=null) {					
					if(names.length>1)
						erClone.chemical_name=names[i].trim();
					else if (names.length==1) {
						erClone.chemical_name=er.chemical_name;
					}
				}
				
				if(smiles!=null) {					
					if(smiles.length>1)
						erClone.smiles=smiles[i].trim();
					else if (smiles.length==1) {
						erClone.smiles=er.smiles;
					}
				}
				
				if(erClone.casrn.contains("?")) {
//						erClone.casrn=null;
					continue;//discard since we already have a different record with a CAS
				}
				
//				if(casrns.length>1)
//					System.out.println(er.property_name+"\t"+erClone.casrn+"\t"+erClone.chemical_name+"\t"+erClone.smiles);
				
				records.add(erClone);
				uc.convertRecord(erClone);
//				System.out.println("CAS split record");
//				System.out.println(gson.toJson(erClone)+"\n****************\n");
//					System.out.println(gson.toJson(erClone));
			}
		
		} else {
			
//			System.out.println("Normal record");
//			System.out.println(gson.toJson(er)+"\n****************\n");

			uc.convertRecord(er);
			records.add(er);	
		}
	}
	
	
	
	private LiteratureSource createLiteratureSource(String reference) {

		LiteratureSource ls=new LiteratureSource();

		if (reference.contains(";")) {
			reference=reference.substring(0,reference.indexOf(";"));
		}
		
		if (reference.contains("(")) {
			ls.setAuthor(reference.substring(0,reference.indexOf("(")).trim());
			ls.setYear(reference.substring(reference.indexOf("(")+1,reference.indexOf(")")));
		}

//		ls.setCitation(reference+" (via OPERA)");
//		ls.setName(reference+" (via OPERA)");
		
		ls.setCitation(reference);
		ls.setName(reference);


//		System.out.println(reference+"\t"+ls.getAuthor()+"\t"+ls.getYear());
		return ls;
	}
	
	private static void finalizePropertyValues(ExperimentalRecord er) {
		if (er.property_name == ExperimentalConstants.strHenrysLawConstant) {
			er.property_value_units_final = ExperimentalConstants.str_atm_m3_mol;
			er.property_value_point_estimate_final = Math.pow(10,er.property_value_point_estimate_original);
		}
		else if (er.property_name == ExperimentalConstants.strBoilingPoint || er.property_name == ExperimentalConstants.strMeltingPoint) {
			er.property_value_point_estimate_final = er.property_value_point_estimate_original;
			er.property_value_units_final = er.property_value_units_original;
		}
		else if (er.property_name == ExperimentalConstants.strWaterSolubility) {
			er.property_value_point_estimate_original = Math.pow(10,er.property_value_point_estimate_original);
			er.property_value_units_final = ExperimentalConstants.str_M;
		}
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParseOPERA p = new ParseOPERA();
//		p.generateOriginalJSONRecords = true;
//		p.howManyOriginalRecordsFiles=2;
		
		p.generateOriginalJSONRecords = true;
		p.howManyOriginalRecordsFiles=3;

		
		p.maxExcelRows=999999;
		p.createFiles();
	}

}
