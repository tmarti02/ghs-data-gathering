package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import gov.epa.ghs_data_gathering.Database.CreateGHS_Database;
import gov.epa.ghs_data_gathering.Database.MySQL_DB;

public class RawDataEChemPortalAPI {
	
	public String date;
	public String query;
	public String content;
	
	public static final String[] fieldNames = {"date","query","content"};
	
	public RawDataEChemPortalAPI (String date,String query,String content) {
		this.date=date;
		this.query=query.replaceAll("'", "\'");
		this.content=content.replaceAll("'", "\'");
	}
	
	public void addRecordToDatabase(Connection conn) {
		String[] values= {date,query,content};
		CreateGHS_Database.addDataToTable("results",fieldNames,values,conn);
	}
}
