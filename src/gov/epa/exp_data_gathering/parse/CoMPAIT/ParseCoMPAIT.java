package gov.epa.exp_data_gathering.parse.CoMPAIT;

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


/**
* @author TMARTI02
*/
public class ParseCoMPAIT extends Parse {
	
	public ParseCoMPAIT() {
		sourceName = RecordCoMPAIT.sourceName;
		this.init();
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordCoMPAIT.parseRecordsFromExcel();
		
		System.out.println(gson.toJson(records));
		
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental = new ExperimentalRecords();
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordCoMPAIT> records = new ArrayList<RecordCoMPAIT>();
			
//			System.out.println(gson.toJson(records));
			
			RecordCoMPAIT[] tempRecords = null;
			if (howManyOriginalRecordsFiles == 1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordCoMPAIT[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					records.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0, jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordCoMPAIT[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						records.add(tempRecords[i]);
					}
				}
			}

			Iterator<RecordCoMPAIT> it = records.iterator();

			while (it.hasNext()) {
				RecordCoMPAIT r = it.next();
				addExperimentalRecord(r, recordsExperimental);
			}
			
			System.out.println(gson.toJson(recordsExperimental));

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}

	private void addExperimentalRecord(RecordCoMPAIT r, ExperimentalRecords recordsExperimental) {

		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = new Date();
		String strDate = formatter.format(date);
		String dayOnly = strDate.substring(0, strDate.indexOf(" "));
		
		ExperimentalRecord er=r.toExperimentalRecord(dayOnly,ExperimentalConstants.str_log_mg_L);
		recordsExperimental.add(er);

		er=r.toExperimentalRecord(dayOnly,ExperimentalConstants.str_log_ppm);
		recordsExperimental.add(er);

//		System.out.println(gson.toJson(er));
		
		
	}
	
	public static void main(String[] args) {
		ParseCoMPAIT p = new ParseCoMPAIT();
		p.removeDuplicates=false;
		p.generateOriginalJSONRecords=true;
		p.howManyOriginalRecordsFiles=1;

		p.createFiles();

	}

}
