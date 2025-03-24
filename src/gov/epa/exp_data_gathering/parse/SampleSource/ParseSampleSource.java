package gov.epa.exp_data_gathering.parse.SampleSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.ThreeM.ParseThreeM;

import java.io.FileReader;

public class ParseSampleSource extends Parse {

	public ParseSampleSource() {
		sourceName = "SampleSource"; // TODO Consider creating ExperimentalConstants.strSourceSampleSource instead.
		this.init();

		// TODO Is this a toxicity source? If so, rename original and experimental records files here.
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordSampleSource.parseSampleSourceRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			List<RecordSampleSource> recordsSampleSource = new ArrayList<RecordSampleSource>();
			RecordSampleSource[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordSampleSource[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsSampleSource.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordSampleSource[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsSampleSource.add(tempRecords[i]);
					}
				}
			}

			Iterator<RecordSampleSource> it = recordsSampleSource.iterator();
			while (it.hasNext()) {
				RecordSampleSource r = it.next();
				addExperimentalRecord(r,recordsExperimental);
				// TODO Write addExperimentalRecord() method to parse this source.
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}

	private void addExperimentalRecord(RecordSampleSource r, ExperimentalRecords recordsExperimental) {
		// TODO Auto-generated method stub
		
	}
	
	
	public static void main(String[] args) {
		ParseSampleSource p = new ParseSampleSource();
		p.createFiles();

	}

}