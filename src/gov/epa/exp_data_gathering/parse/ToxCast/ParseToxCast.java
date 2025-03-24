package gov.epa.exp_data_gathering.parse.ToxCast;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;


/**
* @author TMARTI02
*/
public class ParseToxCast extends Parse {
	
	public ParseToxCast() {
		sourceName = RecordToxCast.sourceName;
		this.init();
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordToxCast.parseRecordsFromExcel();
		
		System.out.println(gson.toJson(records));
		
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental = new ExperimentalRecords();
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordToxCast> recordsToxCast = new ArrayList<RecordToxCast>();
			RecordToxCast[] tempRecords = null;
			if (howManyOriginalRecordsFiles == 1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordToxCast[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsToxCast.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0, jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordToxCast[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsToxCast.add(tempRecords[i]);
					}
				}
			}

			Iterator<RecordToxCast> it = recordsToxCast.iterator();

			while (it.hasNext()) {
				RecordToxCast r = it.next();
				addExperimentalRecord(r, recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}

	private void addExperimentalRecord(RecordToxCast r, ExperimentalRecords recordsExperimental) {

		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = new Date();
		String strDate = formatter.format(date);
		String dayOnly = strDate.substring(0, strDate.indexOf(" "));
		
		ExperimentalRecord er=r.toExperimentalRecord(dayOnly);
		System.out.println(gson.toJson(er));
		
		recordsExperimental.add(er);
		
	}
	
	public static void main(String[] args) {
		ParseToxCast p = new ParseToxCast();
		p.removeDuplicates=false;
		p.generateOriginalJSONRecords=false;
		p.howManyOriginalRecordsFiles=1;

		p.createFiles();

	}

}
