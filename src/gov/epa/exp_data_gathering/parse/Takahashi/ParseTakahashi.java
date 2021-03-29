package gov.epa.exp_data_gathering.parse.Takahashi;

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

public class ParseTakahashi extends Parse {
	public ParseTakahashi() {
		sourceName = ExperimentalConstants.strSourceTakahashi;
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
		Vector<RecordTakahashi> records = RecordTakahashi.parseTakahashiRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordTakahashi> recordsTakahashi = new ArrayList<RecordTakahashi>();
			RecordTakahashi[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordTakahashi[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsTakahashi.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordTakahashi[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsTakahashi.add(tempRecords[i]);
					}
				}
			}
			
			Iterator<RecordTakahashi> it = recordsTakahashi.iterator();
			while (it.hasNext()) {
				RecordTakahashi r = it.next();
				addExperimentalRecord(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	private void addExperimentalRecord(RecordTakahashi tr,ExperimentalRecords records) {
		ExperimentalRecord er = new ExperimentalRecord();
		er.date_accessed = RecordTakahashi.lastUpdated;
		er.source_name = ExperimentalConstants.strSourceTakahashi;
		er.url = "https://doi.org/10.3109/15569521003587327";
		
		er.chemical_name = tr.testMaterial;
		er.casrn = tr.casNo;
		er.property_name = ExperimentalConstants.strEyeIrritation;
		er.property_value_string = "Draize score (binary classification): " + tr.draizeScore100;
	
		er.property_value_point_estimate_original = getTakahashiBinary(tr.draizeScore100);
		er.property_value_point_estimate_final = er.property_value_point_estimate_original;
		er.property_value_units_original = "binary";
		er.property_value_units_final = "binary";
		
		uc.convertRecord(er);
		
		if (er.property_value_point_estimate_final==-1) {
			er.keep=false;
			er.reason="Ambiguous eye irritation score";
		}
		records.add(er);
	}
	
	private double getTakahashiBinary(String draizeScore) {
		if (draizeScore.contains("NI")) {
			return 0.0;
		} else if (draizeScore.contains("I")) {
			return 1.0;
		} else {
			return -1.0;
		}
			
	}
	
	public static void main(String[] args) {
		ParseTakahashi p = new ParseTakahashi();
		p.createFiles();
	}
}
