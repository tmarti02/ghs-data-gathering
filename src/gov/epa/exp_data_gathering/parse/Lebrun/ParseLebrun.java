package gov.epa.exp_data_gathering.parse.Lebrun;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;

public class ParseLebrun extends Parse {

	public ParseLebrun() {
		sourceName = ExperimentalConstants.strSourceLebrun;
		this.init();
		
		fileNameJSON_Records = sourceName +" Toxicity Original Records.json";
		fileNameFlatExperimentalRecords = sourceName +" Toxicity Experimental Records.txt";
		fileNameFlatExperimentalRecordsBad = sourceName +" Toxicity Experimental Records-Bad.txt";
		fileNameJsonExperimentalRecords = sourceName +" Toxicity Experimental Records.json";
		fileNameJsonExperimentalRecordsBad = sourceName +" Toxicity Experimental Records-Bad.json";
		fileNameExcelExperimentalRecords = sourceName +" Toxicity Experimental Records.xlsx";
	}
	
	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordLebrun.parseLebrunRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordLebrun> recordsLebrun = new ArrayList<RecordLebrun>();
			RecordLebrun[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordLebrun[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsLebrun.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordLebrun[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsLebrun.add(tempRecords[i]);
					}
				}
			}
			
			Iterator<RecordLebrun> it = recordsLebrun.iterator();
			while (it.hasNext()) {
				RecordLebrun r = it.next();
				addExperimentalRecord(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	private void addExperimentalRecord(RecordLebrun lr,ExperimentalRecords records) {
		ExperimentalRecord erIrr = new ExperimentalRecord();
		erIrr.date_accessed = RecordLebrun.lastUpdated;
		erIrr.source_name = ExperimentalConstants.strSourceLebrun;
		erIrr.url = "https://doi.org/10.1016/j.tiv.2020.105070";
		
		erIrr.chemical_name = lr.chemicalName;
		erIrr.casrn = lr.casrn.replaceAll("/", "|");
		erIrr.property_name = "rabbit_" + ExperimentalConstants.strEyeIrritation;
		erIrr.property_value_string = "";
		String ghs = "";
		String epa = "";
		if (lr.inVivoGHS!=null && !lr.inVivoGHS.isBlank()) {
			ghs = lr.inVivoGHS.substring(0,lr.inVivoGHS.indexOf("[")).trim();
			erIrr.property_value_string += "GHS: " + ghs;
		}
		if (lr.inVivoEPA!=null && !lr.inVivoEPA.isBlank()) {
			epa = lr.inVivoEPA.substring(0,lr.inVivoEPA.indexOf("[")).trim();
			if (!erIrr.property_value_string.equals("")) { erIrr.property_value_string += "; "; }
			erIrr.property_value_string += "EPA: " + epa;
		}
		erIrr.property_value_point_estimate_original = convertCodesToBinaryIrritation(ghs, epa);
		erIrr.property_value_point_estimate_final = erIrr.property_value_point_estimate_original;
		erIrr.property_value_units_original = "binary";
		erIrr.property_value_units_final = "binary";
		uc.convertRecord(erIrr);
		
		if (erIrr.property_value_point_estimate_final==-1) {
			erIrr.keep=false;
			erIrr.reason="Ambiguous eye irritation score";
		}
		
		// Deep copy existing ER by serialization-deserialization
		ExperimentalRecord erCorr = gson.fromJson(gson.toJson(erIrr), ExperimentalRecord.class);
		erCorr.property_name = "rabbit_" + ExperimentalConstants.strEyeCorrosion;
		erCorr.property_value_point_estimate_original = convertCodesToBinaryCorrosion(ghs, epa, erIrr.property_value_point_estimate_final);
		erCorr.property_value_point_estimate_final = erCorr.property_value_point_estimate_original;
		
		records.add(erIrr);
		records.add(erCorr);
	}
	
	private static double convertCodesToBinaryIrritation(String ghs, String epa) {
		double ghsBinary = 0;
		if (ghs==null || ghs.isBlank()) {
			ghsBinary = -1;
		} else if (ghs.startsWith("1") || ghs.startsWith("2")) {
			ghsBinary = 1;
		} else if (ghs.startsWith("NC")) {
			ghsBinary = 0;
		} else {
			ghsBinary = -1;
		}
		
		double epaBinary = 0;
		if (epa==null || epa.isBlank()) {
			epaBinary = -1;
		} else if (epa.startsWith("I")) {
			if (epa.startsWith("IV")) {
				epaBinary = 0;
			} else {
				epaBinary = 1;
			}
		} else {
			epaBinary = -1;
		}
		
		return Math.max(ghsBinary, epaBinary);
	}
	
	private static double convertCodesToBinaryCorrosion(String ghs, String epa, double isIrritant) {
		if (isIrritant<=0) {
			return isIrritant;
		} else {
			double ghsBinary = 0;
			if (ghs==null || ghs.isBlank()) {
				ghsBinary = -1;
			} else if (ghs.startsWith("1")) {
				ghsBinary = 1;
			}
			
			double epaBinary = 0;
			if (epa==null || epa.isBlank()) {
				epaBinary = -1;
			} else if (epa.startsWith("IV") || epa.startsWith("II")) {
				epaBinary = 0;
			} else {
				epaBinary = 1;
			}
			
			return Math.max(ghsBinary,epaBinary);
		}
	}
	
	public static void main(String[] args) {
		ParseLebrun p = new ParseLebrun();
		p.createFiles();
	}
}
