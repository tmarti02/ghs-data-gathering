package gov.epa.api;

import java.sql.Connection;

import gov.epa.ghs_data_gathering.Database.CreateGHS_Database;

public class RawDataRecord {

	public String date;//date accessed
	public String url;//url accessed
	public String html;//raw data as html
	
	public final static String [] fieldNames= {"date","url","html"};
	
	public RawDataRecord (String date,String url,String html) {
		this.date=date;
		this.url=url;
		this.html=html;
	}
	
	public void addRecordToDatabase(String tableName,Connection conn) {
		String [] values= {date,url,html};
		CreateGHS_Database.addDataToTable(tableName, fieldNames, values, conn);
	}
	
}
