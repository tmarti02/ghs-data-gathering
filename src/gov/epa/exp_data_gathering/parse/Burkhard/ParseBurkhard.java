package gov.epa.exp_data_gathering.parse.Burkhard;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.Burkhard.RecordBurkhard;
import kong.unirest.json.JSONObject;


public class ParseBurkhard extends Parse {

	public ParseBurkhard() {
		sourceName = ExperimentalConstants.strSourceBurkhard;
		this.init();
		
		// toxicity record status commented out for now
		/*
		fileNameJSON_Records = sourceName +" Toxicity Original Records.json";
		fileNameFlatExperimentalRecords = sourceName +" Toxicity Experimental Records.txt";
		fileNameFlatExperimentalRecordsBad = sourceName +" Toxicity Experimental Records-Bad.txt";
		fileNameJsonExperimentalRecords = sourceName +" Toxicity Experimental Records.json";
		fileNameJsonExperimentalRecordsBad = sourceName +" Toxicity Experimental Records-Bad.json";
		fileNameExcelExperimentalRecords = sourceName +" Toxicity Experimental Records.xlsx";
		*/

	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordBurkhard.parseBurkhardRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			List<RecordBurkhard> recordsBurkhard = new ArrayList<RecordBurkhard>();
			RecordBurkhard[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordBurkhard[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsBurkhard.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordBurkhard[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsBurkhard.add(tempRecords[i]);
					}
				}
			}

			Iterator<RecordBurkhard> it = recordsBurkhard.iterator();
			while (it.hasNext()) {
				RecordBurkhard r = it.next();
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}
	
	
	
	
	private void addExperimentalRecords(RecordBurkhard rb, ExperimentalRecords recordsExperimental) {
		if (rb.Log_BCF_Steady_State_mean != null && !rb.Log_BCF_Steady_State_mean.isBlank()) {
			addNewExperimentalRecord(rb,"LogBCFSteadyState", recordsExperimental);
		}
		if (rb.Log_BCF_Kinetic_mean != null && !rb.Log_BCF_Kinetic_mean.isBlank()) {
			addNewExperimentalRecord(rb,"LogBCFKinetic", recordsExperimental);
		}
		if (rb.Log_BAF_mean != null && !rb.Log_BAF_mean.isBlank()) {
			addNewExperimentalRecord(rb,"LogBAF", recordsExperimental);
		}
		
	}
	
	
	private void addNewExperimentalRecord(RecordBurkhard rb, String propertyName, ExperimentalRecords recordsExperimental) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
		Date date = new Date();  
		String strDate=formatter.format(date);
		String dayOnly = strDate.substring(0,strDate.indexOf(" "));
		ExperimentalRecord er = new ExperimentalRecord();
		
		JSONObject jsonNote = new JSONObject();
		jsonNote.put("species", rb.Common_Name);
		jsonNote.put("tissue", rb.Tissue);
		jsonNote.put("exposure concentrations", rb.Exposure_Concentrations);
		
		
		er.source_name=sourceName;
		er.chemical_name = rb.Chemical;
		er.casrn = rb.CASRN;
		er.dsstox_substance_id = rb.DTXSID;
		er.reference = rb.Reference;
		
		if (propertyName=="LogBCFSteadyState") {
			er.property_name=ExperimentalConstants.strLogBCF;
			er.property_value_units_final = rb.Log_BCF_Steady_State_units;
			jsonNote.put("study quality", rb.Study_Quality_BCF);
			er.updateNote(jsonNote.toString());

			try {
				String propertyValue = rb.Log_BCF_Steady_State_mean;
				if (rb.Log_BCF_Steady_State_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
					er.property_value_point_estimate_final = Math.log10(Double.parseDouble(rb.Log_BCF_Steady_State_mean));
		        	er.property_value_string = propertyValue + " (arithmetic)";

				}
				else {
	        	er.property_value_point_estimate_final = Double.parseDouble(rb.Log_BCF_Steady_State_mean);
	        	er.property_value_string = propertyValue + " (log)";

				}
	        	if ((!rb.Log_BCF_Steady_State_max.isBlank()) && (!rb.Log_BCF_Steady_State_min.isBlank())) {
	        	String property_value = rb.Log_BCF_Steady_State_min + "~" + rb.Log_BCF_Steady_State_max;
	        	if (rb.Log_BCF_Steady_State_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
					er.property_value_max_final = Math.log10(Double.parseDouble(rb.Log_BCF_Steady_State_max));
					er.property_value_min_final = Math.log10(Double.parseDouble(rb.Log_BCF_Steady_State_min));
					er.property_value_string = property_value + " (arithmetic)";
				} else {
	        	er.property_value_max_final = Double.parseDouble(rb.Log_BCF_Steady_State_max);
	        	er.property_value_min_final = Double.parseDouble(rb.Log_BCF_Steady_State_min);
	        	er.property_value_string = property_value + " (log)";
	        	}
	        	}
	        } catch (NumberFormatException e) {

	        }
		}	else if (propertyName == "LogBCFKinetic") {
			er.property_name=ExperimentalConstants.strLogBCF;
			er.property_value_units_final = rb.Log_BCF_Kinetic_units;
			jsonNote.put("study quality", rb.Study_Quality_BCF);
			er.updateNote(jsonNote.toString());

			try {
				String property_value = rb.Log_BCF_Kinetic_mean;
	        	if (rb.Log_BCF_Kinetic_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
					er.property_value_point_estimate_final = Math.log10(Double.parseDouble(rb.Log_BCF_Kinetic_mean));
					er.property_value_string = property_value + " (arithmetic)";
	        	} else {
	        		er.property_value_point_estimate_final = Double.parseDouble(rb.Log_BCF_Kinetic_mean);
					er.property_value_string = property_value + " (log)";
	        	}
	        	if ((!rb.Log_BCF_Kinetic_min.isBlank()) && (!rb.Log_BCF_Kinetic_max.isBlank())) {
		        	String propertyValue =  rb.Log_BCF_Kinetic_min + "~" + rb.Log_BCF_Kinetic_max;
		        	if (rb.Log_BCF_Kinetic_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
						er.property_value_max_final = Math.log10(Double.parseDouble(rb.Log_BCF_Kinetic_min));
						er.property_value_min_final = Math.log10(Double.parseDouble(rb.Log_BCF_Kinetic_max));
						er.property_value_string = propertyValue + " (arithmetic)";
					} else {
		        	er.property_value_max_final = Double.parseDouble(rb.Log_BCF_Kinetic_max);
		        	er.property_value_min_final = Double.parseDouble(rb.Log_BCF_Kinetic_min);
					er.property_value_string = propertyValue + " (log)";

		        	}
		        	}
			} catch (NumberFormatException e) {
				
			}
		
			
			
		
		} else if (propertyName == "LogBAF") {
			er.property_name=propertyName;
			er.keep = false;
			er.reason = "BAF value";
			er.property_value_units_final = rb.Log_BAF_units;
			er.updateNote("study quality: " + rb.Study_Quality_BAF);
			try {
			String propertyValue = rb.Log_BAF_mean;
			if ((!(rb.Log_BAF_arithmetic_or_logarithmic.isBlank())) &&
					rb.Log_BAF_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
				er.property_value_string = propertyValue + " (arithmetic)";
				er.property_value_point_estimate_final = Math.log10(Double.parseDouble(rb.Log_BAF_mean));
			}
			else {
        	er.property_value_point_estimate_final = Double.parseDouble(rb.Log_BAF_mean);
			er.property_value_string = propertyValue + " (log)";

			}
        	if (!(rb.Log_BAF_min.isBlank() && rb.Log_BAF_max.isBlank())) {
        	String propertyValue2 =  rb.Log_BAF_min + "~" + rb.Log_BAF_max;

			if ((!(rb.Log_BAF_arithmetic_or_logarithmic.isBlank())) &&
					rb.Log_BAF_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
				er.property_value_string = propertyValue2 + " (arithmetic)";

        	er.property_value_max_final = Math.log10(Double.parseDouble(rb.Log_BAF_min));
        	er.property_value_min_final = Math.log10(Double.parseDouble(rb.Log_BAF_max));
        	} else {
            	er.property_value_max_final = Double.parseDouble(rb.Log_BAF_min);
            	er.property_value_min_final = Double.parseDouble(rb.Log_BAF_max);
				er.property_value_string = propertyValue2 + " (log)";

        	}
        	}
			} catch (NumberFormatException e) {

			}
		}
		
		// limiting things to whole body BCF based on feedback from Todd Martin 9/1/2019
		if (!(rb.Tissue.contains("whole body"))) {
			er.keep = false;
			er.reason = "not whole body";
		} else if (rb.Tissue.contains("whole body") && !(propertyName.equals("LogBAF"))) {
			er.keep = true;
		}

		
		// limiting our search to fish
		if (!(rb.class_taxonomy.toLowerCase().contains("actinopteri") || rb.class_taxonomy.toLowerCase().contains("actinopterygii") || rb.class_taxonomy.toLowerCase().contains("teleostei"))) {
			er.keep = false;
			er.reason = "not a fish";
		}
		
		
		// limiting to only high and medium quality studies
		if (rb.Study_Quality_BAF.toLowerCase().contains("low") || rb.Study_Quality_BCF.toLowerCase().contains("low")){
			er.keep=false;
			er.reason = "untrusted study";
		}

		
		
		
		
		/*
		if ((rb.Tissue != null && !rb.Tissue.isBlank()) && (rb.Tissue.toLowerCase().contains("plasma"))) {
			er.keep = false;
			er.reason = "empty tissue or plasma record";
		}
		*/
		
		
		if (!(er.property_value_point_estimate_final != null)) {
			er.keep = false;
			er.reason = "unable to parse a value";
		}
		
		recordsExperimental.add(er);
		}

	
	
	public static void main(String[] args) {
		ParseBurkhard p = new ParseBurkhard();
		p.createFiles();
	}


}