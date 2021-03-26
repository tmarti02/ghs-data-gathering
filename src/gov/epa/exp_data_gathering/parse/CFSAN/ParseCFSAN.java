package gov.epa.exp_data_gathering.parse.CFSAN;

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

public class ParseCFSAN extends Parse {
	
	public ParseCFSAN() {
		sourceName = ExperimentalConstants.strSourceCFSAN;
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
		Vector<RecordCFSAN> records = RecordCFSAN.parseCFSANRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordCFSAN> recordsCFSAN = new ArrayList<RecordCFSAN>();
			RecordCFSAN[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordCFSAN[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsCFSAN.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordCFSAN[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsCFSAN.add(tempRecords[i]);
					}
				}
			}
			
			Iterator<RecordCFSAN> it = recordsCFSAN.iterator();
			while (it.hasNext()) {
				RecordCFSAN r = it.next();
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	private void addExperimentalRecords(RecordCFSAN cr,ExperimentalRecords records) {
		if (cr.activity!=null && !cr.activity.isBlank()) {
			ExperimentalRecord er = new ExperimentalRecord();
			er.date_accessed = RecordCFSAN.lastUpdated;
			er.source_name = ExperimentalConstants.strSourceCFSAN;
			er.url = "https://doi.org/10.1016/j.yrtph.2014.11.011";
			
			er.chemical_name = cr.chemName;
			er.casrn = cr.casNr;
			er.property_name = ExperimentalConstants.strEyeIrritation;
			er.property_value_string = cr.activity;
			er.property_value_point_estimate_original = Double.parseDouble(cr.activity);
			er.property_value_point_estimate_final = er.property_value_point_estimate_original;
			er.property_value_units_original = "binary";
			er.property_value_units_final = "binary";
//			uc.convertRecord(er);
			
			if (er.property_value_point_estimate_final==-1) {
				er.keep=false;
				er.reason="Ambiguous eye irritation score";
			}
			records.add(er);
		}
	}
	
	public static void main(String[] args) {
		ParseCFSAN p = new ParseCFSAN();
		p.createFiles();
	}
}
