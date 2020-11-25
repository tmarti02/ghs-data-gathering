package gov.epa.exp_data_gathering.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import gov.epa.ghs_data_gathering.Database.MySQL_DB;

public class DataFetcher {
	
	public static final String mainFolder = "Data"+File.separator+"Experimental";
	public static final String databasePath = mainFolder+File.separator+"ExperimentalRecords.db";
	public static final String jsonPath = mainFolder+File.separator+"ExperimentalRecords.json";
	
	private ExperimentalRecords getExperimentalRecords() {
		ExperimentalRecords records = new ExperimentalRecords();
		String[] sources = {"eChemPortal\\eChemPortal","LookChem\\LookChem PFAS\\LookChem","PubChem\\PubChem"};
		for (String source:sources) {
			String recordFileName = mainFolder+File.separator+source+" Experimental Records.json";
			String badRecordFileName = mainFolder+File.separator+source+" Experimental Records-Bad.json";
			try {
				ExperimentalRecords sourceRecords = ExperimentalRecords.loadFromJSON(recordFileName);
				ExperimentalRecords badSourceRecords = ExperimentalRecords.loadFromJSON(badRecordFileName);
				records.addAll(sourceRecords);
				records.addAll(badSourceRecords);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return records;
	}
	
	public void createExperimentalRecordsDatabase() {
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		ExperimentalRecords records = getExperimentalRecords();
		createDatabase(records);
	}
	
	private void createDatabase(ExperimentalRecords records) {
		String[] fieldNames = ExperimentalRecord.outputFieldNames;
		String tableName = "records";
		try {
			Connection conn= MySQL_DB.getConnection(databasePath);
			Statement stat = MySQL_DB.getStatement(conn);			
			conn.setAutoCommit(true);		
			stat.executeUpdate("drop table if exists "+tableName+";");
			stat.executeUpdate("VACUUM;");
			
			//Need CAS as the primary key if we are doing lots of searches- otherwise searches will be like 1 second each!
			MySQL_DB.create_table_key_with_duplicates(stat, tableName, fieldNames,"casrn");//need unique values in the table for key field for this to work!
			conn.setAutoCommit(false);

			String s = "insert into " + tableName + " values (";

			for (int i = 1; i <= fieldNames.length; i++) {
				s += "?";
				if (i < fieldNames.length)
					s += ",";
			}
			s += ");";

			int counter = 0;
			int batchCounter = 0;
			PreparedStatement prep = conn.prepareStatement(s);
			for (ExperimentalRecord rec:records) {
				String[] list = rec.getValuesForDatabase();

				if (list.length!=fieldNames.length) {
					System.out.println("Wrong number of values: "+list[0]);
				}

				if (list[0]!=null && !list[0].isBlank()) {
					counter++;
					for (int i = 0; i < list.length; i++) {
						if (list[i]!=null && !list[i].isBlank()) {
							prep.setString(i + 1, list[i]);
						} else {
							prep.setString(i + 1, null);
						}
					}
				}

				try {
					prep.addBatch();
				} catch (Exception ex) {
					System.out.println("Failed to add: "+String.join(",", list));
				}
				
				if (counter % 1000 == 0) {
					prep.executeBatch();
					batchCounter++;
				}

			}

			int[] count = prep.executeBatch();// do what's left
			
			conn.setAutoCommit(true);
						
			String sqlAddIndex="CREATE INDEX idx_casrn ON "+tableName+" (casrn)";
			stat.executeUpdate(sqlAddIndex);			

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	public static void main(String[] args) {
		DataFetcher d = new DataFetcher();
		d.createExperimentalRecordsDatabase();
	}
}
