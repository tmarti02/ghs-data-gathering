package gov.epa.exp_data_gathering.parse.Lebrun;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

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
		Vector<RecordLebrun> records = RecordLebrun.parseLebrunRecordsFromExcel();
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
		ExperimentalRecord er = new ExperimentalRecord();
		er.date_accessed = RecordLebrun.lastUpdated;
		er.source_name = ExperimentalConstants.strSourceLebrun;
		er.url = "https://doi.org/10.1016/j.tiv.2020.105070";
		
		er.chemical_name = lr.chemicalName;
		er.casrn = lr.casrn.replaceAll("/", "|");
		er.property_name = ExperimentalConstants.strEyeIrritation;
		er.property_value_string = "";
		String ghs = "";
		String epa = "";
		if (lr.inVivoGHS!=null && !lr.inVivoGHS.isBlank()) {
			ghs = lr.inVivoGHS.substring(0,lr.inVivoGHS.indexOf("[")).trim();
			er.property_value_string += "GHS: " + ghs;
		}
		if (lr.inVivoEPA!=null && !lr.inVivoEPA.isBlank()) {
			epa = lr.inVivoEPA.substring(0,lr.inVivoEPA.indexOf("[")).trim();
			if (!er.property_value_string.equals("")) { er.property_value_string += "; "; }
			er.property_value_string += "EPA: " + epa;
		}
		er.property_value_point_estimate_original = convertCodesToBinary(ghs, epa);
		er.property_value_point_estimate_final = er.property_value_point_estimate_original;
		er.property_value_units_original = "binary";
		er.property_value_units_final = "binary";
		
		if (er.property_value_point_estimate_final==-1) {
			er.keep=false;
			er.reason="Ambiguous eye irritation score";
		}
		records.add(er);
	}
	
	private static double convertCodesToBinary(String ghs, String epa) {
		if (ghs!=null && !ghs.isBlank() && (ghs.startsWith("1") || ghs.startsWith("2"))) {
			return 1;
		} else if (epa!=null && !epa.isBlank() && epa.startsWith("I")) {
			if (epa.startsWith("IV")) {
				return 0;
			} else {
				return 1;
			}
		} else {
			return -1;
		}
	}
	
	public static void main(String[] args) {
		ParseLebrun p = new ParseLebrun();
		p.createFiles();
	}
}
