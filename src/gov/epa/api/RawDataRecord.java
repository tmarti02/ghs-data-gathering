package gov.epa.api;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import gov.epa.ghs_data_gathering.Database.CreateGHS_Database;
import gov.epa.ghs_data_gathering.Database.MySQL_DB;

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
		CreateGHS_Database.addDataToTable(tableName, fieldNames, values, conn);
	}
	
	public boolean haveRecordInDatabase(String databasePath,String tableName,Connection conn) {

		try {
			Statement stat = MySQL_DB.getStatement(conn);
			ResultSet rs = MySQL_DB.getRecords(stat,tableName,"url",url);
			return rs.next();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		
	}
	
}
