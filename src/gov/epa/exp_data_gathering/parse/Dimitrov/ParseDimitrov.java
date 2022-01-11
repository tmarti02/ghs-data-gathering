package gov.epa.exp_data_gathering.parse.Dimitrov;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;

public class ParseDimitrov extends Parse {

	public ParseDimitrov() {
		sourceName = "Dimitrov"; // TODO Consider creating ExperimentalConstants.strSourceDimitrov instead.
		this.init();

		// TODO Is this a toxicity source? If so, rename original and experimental records files here.
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordDimitrov.parseDimitrovRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			ArrayList<RecordDimitrov> recordsDimitrov = new ArrayList<RecordDimitrov>();
			RecordDimitrov[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordDimitrov[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsDimitrov.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordDimitrov[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsDimitrov.add(tempRecords[i]);
					}
				}
			}

			Iterator<RecordDimitrov> it = recordsDimitrov.iterator();
			while (it.hasNext()) {
				RecordDimitrov r = it.next();
				addExperimentalRecord(r,recordsExperimental);
				// TODO Write addExperimentalRecord() method to parse this source.
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}
	
	
	
	private static void addExperimentalRecord(RecordDimitrov r, ExperimentalRecords recordsExperimental) {
	ExperimentalRecord er = new ExperimentalRecord();
	
	er.keep = false;
	er.casrn = r.CAS_RN;
	er.chemical_name = r.Name;
	er.property_value_string = "";
	if (r.Exp_Log_BCF != null) {
		er.keep = true;
		er.property_value_string = r.Exp_Log_BCF;
		er.property_value_point_estimate_final = Double.parseDouble(r.Exp_Log_BCF);
	}
	
	er.original_source_name = "Dimitrov";
	
	recordsExperimental.add(er);
	}
	
	public static void main(String[] args) {
		ParseDimitrov p = new ParseDimitrov();
		p.createFiles();
	}

	
	
	
}