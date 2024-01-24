package gov.epa.exp_data_gathering.parse.OPERA;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.ParseUtilities;

public class ParseOPERA extends Parse {

	
	String userName="tmarti02";
	
	public ParseOPERA() {
		sourceName = "OPERA";
		this.init();
//		this.writeFlatFile=true;
	}
	@Override
	protected void createRecords() {
		
		System.out.println("enter create records");
		
		RecordOPERA r=new RecordOPERA();
		Vector<RecordOPERA> records = r.parseOperaSdfs();
//		System.out.println(records.size());
		
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
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordOPERA[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsOPERA.add(tempRecords[i]);
					}
				}
			}
			
			Iterator<RecordOPERA> it = recordsOPERA.iterator();
			while (it.hasNext()) {
				RecordOPERA r = it.next();
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
//		System.out.println("recordsExperimental.size()="+recordsExperimental.size());
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
			
			if(!(ro.pKa_a.equals("NaN"))) {
				ExperimentalRecord er = ro.toExperimentalRecord(dayOnly,ExperimentalConstants.str_pKAa);
				er.property_value_string = ro.pKa_a+"";
				er.property_value_point_estimate_original=Double.parseDouble(ro.pKa_a);
				uc.convertRecord(er);
				records.add(er);
			}

			if ((!(ro.pKa_b.equals("NaN")))) {
				ExperimentalRecord er = ro.toExperimentalRecord(dayOnly,ExperimentalConstants.str_pKAb);
				er.property_value_string = ro.pKa_b+"";
				er.property_value_point_estimate_original=Double.parseDouble(ro.pKa_b);
				uc.convertRecord(er);
				records.add(er);
			}
			
		} else {

			
			ExperimentalRecord er = ro.toExperimentalRecord(dayOnly,ro.property_name);

			if(er.property_name.equals(ExperimentalConstants.strORAL_RAT_LD50)) {
				
				if(ro.CATMoS_LD50_str.equals("NA")) return;
				
				er.property_category="acute oral toxicity";
				
				//Use detailed code to get qualifier, min/max/point estimates: 
				ParseUtilities.getNumericalValue(er, ro.CATMoS_LD50_str, ro.CATMoS_LD50_str.length(), false);
				er.property_value_string=ro.CATMoS_LD50_str;
				
				if(ro.CATMoS_LD50_str.equals("NA")) {
					er.keep=false;
					er.reason="Exp. LD50 is NA";
				}
			} else {
				
				er.property_value_point_estimate_original = ro.property_value_original;
				if (er.property_value_point_estimate_original != null && ro.property_value_units_original != null) {
					er.property_value_string = er.property_value_point_estimate_original.toString() + " " + ro.property_value_units_original;
				} else if (er.property_value_point_estimate_original != null) {
					er.property_value_string = er.property_value_point_estimate_original.toString();
				}
			}
			
			if(ro.Reference!=null) {
//				System.out.println(ro.Reference);
//				er.literatureSource=createLiteratureSource(ro.Reference);
				er.document_name=ro.Reference;//just store as string to avoid complications in the db
			}
			
			if (ro.property_name == ExperimentalConstants.strLogKOW) {
				ParseUtilities.getLogProperty(er,er.property_value_string);
			}
			
//			if (!(ro.dsstox_compound_id == null))
//			er.dsstox_substance_id = ro.dsstox_compound_id;
			
			// handles temperature recorded as 24|25 and absent temperatures
			getTemperatureCondition(er,ro.Temperature);
			
			// finalizePropertyValues(er);
			// er.finalizePropertyValues();
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
	
	private static void getTemperatureCondition(ExperimentalRecord er, String propertyValue) {
		if ((propertyValue != null && propertyValue.length() > 0) && (!propertyValue.contains("|")))
			er.temperature_C = Double.parseDouble(propertyValue);
		else if ((propertyValue != null && propertyValue.length() > 0) && propertyValue.contains("|")) {
			int vertLineIndex = propertyValue.indexOf("|");
			String temp1 = propertyValue.substring(0,vertLineIndex);
			String temp2 = propertyValue.substring(vertLineIndex + 1,propertyValue.length());
			double temp1double = Double.parseDouble(temp1);
			double temp2double = Double.parseDouble(temp2);
			er.temperature_C = (temp1double + temp2double)/ 2;
		}
	}
	
	


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParseOPERA p = new ParseOPERA();
		p.generateOriginalJSONRecords = true;
		p.createFiles();
	}

}
