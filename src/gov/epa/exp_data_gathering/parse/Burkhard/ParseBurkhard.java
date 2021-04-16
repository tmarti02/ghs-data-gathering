package gov.epa.exp_data_gathering.parse.Burkhard;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;

public class ParseBurkhard extends Parse {

	public ParseBurkhard() {
		sourceName = ExperimentalConstants.strSourceBurkhard;
		this.init();

		// TODO Is this a toxicity source? If so, rename original and experimental records files here.
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
				addExperimentalRecord(r,recordsExperimental);
				// TODO Write addExperimentalRecord() method to parse this source.
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}
}
