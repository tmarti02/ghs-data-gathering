package gov.epa.api;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import gov.epa.database.SQLite_CreateTable;
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;

public class RawDataRecord {

	public String date;//date accessed
	public String url;//url accessed
	public String content;//raw data as html or json
	
	public final static String [] fieldNames= {"date","url","content"};
	
	public RawDataRecord (String date,String url,String html) {
		this.date=date;
		this.url=url;
		this.content=html;
	}
	
	public void addRecordToDatabase(String tableName,Connection conn) {
		String [] values= {date,url,content};
		SQLite_CreateTable.addDataToTable(tableName, fieldNames, values, conn);
	}
	
	public boolean haveRecordInDatabase(String databasePath,String tableName,Connection conn) {

		try {
			Statement stat = SQLite_Utilities.getStatement(conn);
			ResultSet rs = SQLite_GetRecords.getRecords(stat,tableName,"url",url);
			return rs.next();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		
	}
	
}
