package gov.epa.exp_data_gathering.parse;

import java.io.File;

import gov.epa.ghs_data_gathering.Database.CreateGHS_Database;

public class DataFetcher {
	
	public static final String mainFolder = "Data"+File.separator+"Experimental";
	public static final String databasePath = mainFolder+File.separator+"ExperimentalRecords.db";
	public static final String jsonPath = mainFolder+File.separator+"ExperimentalRecords.json";
	
	private ExperimentalRecords pullAllData() {
		ExperimentalRecords records = new ExperimentalRecords();
		String[] sources = {"eChemPortal\\eChemPortal","LookChem\\LookChem PFAS\\LookChem","PubChem\\PubChem"};
		for (String source:sources) {
			String recordFileName = mainFolder+File.separator+source+" Experimental Records.json";
			ExperimentalRecords sourceRecords = ExperimentalRecords.loadFromJSON(recordFileName);
			records.addAll(sourceRecords);
		}
		records.toJSON_File(jsonPath);
		return records;
	}
	
	public void writeAllRecordsToDatabase() {
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		java.sql.Connection conn=CreateGHS_Database.createDatabaseTable(databasePath, "records", ExperimentalRecord.allFieldNames, true);
		ExperimentalRecords records = pullAllData();
		int counter = 0;
		for (ExperimentalRecord rec:records) {
//			try {
				rec.addRecordToDatabase("records", conn);
				counter++;
				if (counter % 1000 == 0) { System.out.println("Wrote "+counter+" entries to database"); }
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
		}
	}
	
	public static void main(String[] args) {
		DataFetcher d = new DataFetcher();
		d.writeAllRecordsToDatabase();
	}
}
