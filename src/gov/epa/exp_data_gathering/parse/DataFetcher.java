package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import gov.epa.ghs_data_gathering.Database.MySQL_DB;

public class DataFetcher {
	
	private static ExperimentalRecords records;
	
	public static final String mainFolder = "Data"+File.separator+"Experimental";
	public static final String databasePath = mainFolder+File.separator+"ExperimentalRecords.db";
	public static final String jsonPath = mainFolder+File.separator+"ExperimentalRecords.json";
	
	public DataFetcher(String[] sources) {
		records = new ExperimentalRecords();
		for (String source:sources) {
			String recordFileName = mainFolder+File.separator+source+" Experimental Records.json";
			String badRecordFileName = mainFolder+File.separator+source+" Experimental Records-Bad.json";
			try {
				System.out.println("Fetching data from "+source.substring(source.lastIndexOf("\\")+1));
				ExperimentalRecords sourceRecords = ExperimentalRecords.loadFromJSON(recordFileName);
				ExperimentalRecords badSourceRecords = ExperimentalRecords.loadFromJSON(badRecordFileName);
				records.addAll(sourceRecords);
				records.addAll(badSourceRecords);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void createExperimentalRecordsDatabase() {
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		makeDatabase(records);
	}
	
	public void createExperimentalRecordsJSON() {
		File json = new File(jsonPath);
		if(!json.getParentFile().exists()) { json.getParentFile().mkdirs(); }
		records.toJSON_File(jsonPath);
	}
	
	private ExperimentalRecords getExperimentalRecordsSubset(String[] cas) {
		ExperimentalRecords subsetRecords = new ExperimentalRecords();
		for (ExperimentalRecord rec:records) {
			String casCheck="";
			if (rec.casrn!=null) { casCheck = rec.casrn; }
			boolean inSubset = false;
			int i = 0;
			while (!inSubset && i < cas.length) {
				if (casCheck.contains(cas[i])) { inSubset = true; }
				i++;
			}
			if (inSubset) { subsetRecords.add(rec); }
		}
		return subsetRecords;
	}
	
	public void createExperimentalRecordsSubsetJSON(String[] cas,String filename) {
		String path = mainFolder+File.separator+filename;
		File json = new File(path);
		if(!json.getParentFile().exists()) { json.getParentFile().mkdirs(); }
		ExperimentalRecords subsetRecords = getExperimentalRecordsSubset(cas);
		subsetRecords.toJSON_File(path);
	}
	
	public void createExperimentalRecordsSubsetExcel(String[] cas,String filename) {
		String path = mainFolder+File.separator+filename;
		File json = new File(path);
		if(!json.getParentFile().exists()) { json.getParentFile().mkdirs(); }
		ExperimentalRecords subsetRecords = getExperimentalRecordsSubset(cas);
		subsetRecords.toExcel_File(path);
	}
	
	private void makeDatabase(ExperimentalRecords records) {
		String[] fieldNames = ExperimentalRecord.outputFieldNames;
		String tableName = "records";
		System.out.println("Creating database at "+databasePath+" with fields:\n"+String.join("\n",fieldNames));
		try {
			Connection conn= MySQL_DB.getConnection(databasePath);
			Statement stat = MySQL_DB.getStatement(conn);			
			conn.setAutoCommit(true);		
			stat.executeUpdate("drop table if exists "+tableName+";");
			stat.executeUpdate("VACUUM;");
			
			MySQL_DB.create_table_key_with_duplicates(stat, tableName, fieldNames,"casrn");
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

				counter++;
				
				for (int i = 0; i < list.length; i++) {
					if (list[i]!=null && !list[i].isBlank()) {
						prep.setString(i + 1, list[i]);
					} else {
						prep.setString(i + 1, null);
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
			
			System.out.println("Created database with "+counter+" entries");

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	public static void main(String[] args) {
		String[] sources = {"eChemPortal\\eChemPortal","LookChem\\LookChem PFAS\\LookChem","PubChem\\PubChem","OChem\\OChem"};
		DataFetcher d = new DataFetcher(sources);
		d.createExperimentalRecordsDatabase();
		d.createExperimentalRecordsJSON();
		String[] cas = {"335-76-2","3108-42-7","3830-45-3","375-95-1","4149-60-4","307-24-4","355-46-4","3871-99-6","375-22-4","10495-86-0"};
		d.createExperimentalRecordsSubsetJSON(cas, "ExperimentalRecords_CPHEA_112520.json");
		d.createExperimentalRecordsSubsetExcel(cas, "ExperimentalRecords_CPHEA_112520.xlsx");
	}
}
