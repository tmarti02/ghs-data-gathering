package gov.epa.exp_data_gathering.parse.NICEATM;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import com.google.gson.JsonObject;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;

public class ParseNICEATM extends Parse {

	public ParseNICEATM() {
		sourceName = "NICEATM"; // TODO Consider creating ExperimentalConstants.strSourceNICEATM instead.
		this.init();

		// TODO Is this a toxicity source? If so, rename original and experimental records files here.
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordNICEATM.parseNICEATMRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			List<RecordNICEATM> recordsNICEATM = new ArrayList<RecordNICEATM>();
			RecordNICEATM[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordNICEATM[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsNICEATM.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordNICEATM[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsNICEATM.add(tempRecords[i]);
					}
				}
			}

			Iterator<RecordNICEATM> it = recordsNICEATM.iterator();
			while (it.hasNext()) {
				RecordNICEATM r = it.next();
				addExperimentalRecord(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}

	private void addExperimentalRecord(RecordNICEATM r,
				ExperimentalRecords recordsExperimental) {
		ExperimentalRecord er=r.toExperimentalRecord();
		recordsExperimental.add(er);
	}

	public static void main(String[] args) {
		ParseNICEATM p = new ParseNICEATM();
		
		p.generateOriginalJSONRecords=false;
		//p.howManyOriginalRecordsFiles=2;
		
		p.removeDuplicates=false;
		
		p.writeJsonExperimentalRecordsFile=true;
		p.writeExcelExperimentalRecordsFile=true;
		p.writeExcelFileByProperty=true;		
		p.writeCheckingExcelFile=false;//creates random sample spreadsheet
		p.createFiles();
		
	}
}