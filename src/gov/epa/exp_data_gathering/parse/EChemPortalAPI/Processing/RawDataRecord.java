package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Processing;

import java.sql.Connection;

import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Utility.SQLiteDatabase;

/**
 * Stores raw data (date accessed, JSON search query, and JSON search result) for the eChemPortal API
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class RawDataRecord {
	
	private String date;
	private String query;
	private String content;
	
	public static final String[] fieldNames = {"date","query","content"};
	
	public RawDataRecord(String date,String query,String content) {
		this.date=date;
		// Fixes single quotes causing SQL insert problems
		this.query=query.replaceAll("'", "\'");
		this.content=content.replaceAll("'", "\'");
	}
	
	/**
	 * Adds RawDataRecord to a database
	 * @param conn		The Connection to the database to be updated
	 */
	public void addRecordToDatabase(Connection conn) {
		String[] values= {date,query,content};
		SQLiteDatabase.addDataToTable("results",fieldNames,values,conn);
	}
}
