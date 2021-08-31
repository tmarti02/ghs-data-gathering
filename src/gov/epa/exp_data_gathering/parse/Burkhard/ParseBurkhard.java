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
import gov.epa.exp_data_gathering.parse.Burkhard2.RecordBurkhard2;


public class ParseBurkhard extends Parse {

	public ParseBurkhard() {
		sourceName = ExperimentalConstants.strSourceBurkhard; // TODO Consider creating ExperimentalConstants.strSourceBurkhard2 instead.
		this.init();

		// TODO Is this a toxicity source? If so, rename original and experimental records files here.
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordBurkhard2.parseBurkhard2RecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			List<RecordBurkhard2> recordsBurkhard2 = new ArrayList<RecordBurkhard2>();
			RecordBurkhard2[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordBurkhard2[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsBurkhard2.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordBurkhard2[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsBurkhard2.add(tempRecords[i]);
					}
				}
			}

			Iterator<RecordBurkhard2> it = recordsBurkhard2.iterator();
			while (it.hasNext()) {
				RecordBurkhard2 r = it.next();
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}
	
	
	
	
	private void addExperimentalRecords(RecordBurkhard2 rb, ExperimentalRecords recordsExperimental) {
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
	
	
	private void addNewExperimentalRecord(RecordBurkhard2 rb, String propertyName, ExperimentalRecords recordsExperimental) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
		Date date = new Date();  
		String strDate=formatter.format(date);
		String dayOnly = strDate.substring(0,strDate.indexOf(" "));
		ExperimentalRecord er = new ExperimentalRecord();
		er.updateNote("species:" + rb.Common_Name + ";" + "tissue:" + rb.Tissue + ";" + "exposure concentrations: " + rb.Exposure_Concentrations);
		er.source_name=sourceName;
		er.chemical_name = rb.Chemical;
		er.casrn = rb.CASRN;
		er.dsstox_substance_id = rb.DTXSID;
		er.original_source_name = rb.Reference;
		
		if (propertyName=="LogBCFSteadyState") {
			er.property_name=propertyName;
			er.property_value_units_final = rb.Log_BCF_Steady_State_units;
			er.updateNote("study quality: " + rb.Study_Quality_BCF);
			try {
				er.property_value_string = "Value: " + rb.Log_BCF_Steady_State_mean;
				if (rb.Log_BCF_Steady_State_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
					er.property_value_point_estimate_final = Math.log10(Double.parseDouble(rb.Log_BCF_Steady_State_mean));
				}
				else {
	        	er.property_value_point_estimate_final = Double.parseDouble(rb.Log_BCF_Steady_State_mean);
				}
	        	if ((!rb.Log_BCF_Steady_State_max.isBlank()) && (!rb.Log_BCF_Steady_State_min.isBlank())) {
	        	er.property_value_string = "Value: " + rb.Log_BCF_Steady_State_min + "~" + rb.Log_BCF_Steady_State_max;
	        	if (rb.Log_BCF_Steady_State_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
					er.property_value_max_final = Math.log10(Double.parseDouble(rb.Log_BCF_Steady_State_max));
					er.property_value_min_final = Math.log10(Double.parseDouble(rb.Log_BCF_Steady_State_min));
				} else {
	        	er.property_value_max_final = Double.parseDouble(rb.Log_BCF_Steady_State_max);
	        	er.property_value_min_final = Double.parseDouble(rb.Log_BCF_Steady_State_min);
	        	}
	        	}
	        } catch (NumberFormatException e) {

	        }
		}	else if (propertyName == "LogBCFKinetic") {
			er.property_name=propertyName;
			er.property_value_units_final = rb.Log_BCF_Kinetic_units;
			try {
				er.property_value_string = "Value: " + rb.Log_BCF_Kinetic_mean;
	        	if (rb.Log_BCF_Kinetic_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
					er.property_value_point_estimate_final = Math.log10(Double.parseDouble(rb.Log_BCF_Kinetic_mean));
	        	} else {
	        		er.property_value_point_estimate_final = Double.parseDouble(rb.Log_BCF_Kinetic_mean);
	        	}
	        	if ((!rb.Log_BCF_Kinetic_min.isBlank()) && (!rb.Log_BCF_Kinetic_max.isBlank())) {
		        	er.property_value_string = "Value: " + rb.Log_BCF_Kinetic_min + "~" + rb.Log_BCF_Kinetic_max;
		        	if (rb.Log_BCF_Kinetic_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
						er.property_value_max_final = Math.log10(Double.parseDouble(rb.Log_BCF_Kinetic_min));
						er.property_value_min_final = Math.log10(Double.parseDouble(rb.Log_BCF_Kinetic_max));
					} else {
		        	er.property_value_max_final = Double.parseDouble(rb.Log_BCF_Kinetic_max);
		        	er.property_value_min_final = Double.parseDouble(rb.Log_BCF_Kinetic_min);
		        	}
		        	}
			} catch (NumberFormatException e) {
				
			}
		} else if (propertyName == "LogBAF") {
			er.property_name=propertyName;
			er.property_value_units_final = rb.Log_BAF_units;
			er.updateNote("study quality: " + rb.Study_Quality_BAF);
			try {
			er.property_value_string = "Value: " + rb.Log_BAF_mean;
			if ((!(rb.Log_BAF_arithmetic_or_logarithmic.isBlank())) &&
					rb.Log_BAF_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
				er.property_value_point_estimate_final = Math.log10(Double.parseDouble(rb.Log_BAF_mean));
			}
			else {
        	er.property_value_point_estimate_final = Double.parseDouble(rb.Log_BAF_mean);
			}
        	if (!(rb.Log_BAF_min.isBlank() && rb.Log_BAF_max.isBlank())) {
	        er.property_value_string = "Value: " + rb.Log_BAF_min + "~" + rb.Log_BAF_max;

        	er.property_value_max_final = Double.parseDouble(rb.Log_BAF_min);
        	er.property_value_min_final = Double.parseDouble(rb.Log_BAF_max);
        	}
			} catch (NumberFormatException e) {

			}
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

		
		
		// limiting our search to whole body
		
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