package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.sql.Connection;

import gov.epa.ghs_data_gathering.Database.CreateGHS_Database;

/**
 * Stores raw data (date accessed, JSON search query, and JSON search result) for the eChemPortal API
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class RawDataEChemPortalAPI {
	
	public String date;
	public String query;
	public String content;
	
	public static final String[] fieldNames = {"date","query","content"};
	
	public RawDataEChemPortalAPI(String date,String query,String content) {
		this.date=date;
		// Fixes single quotes causing SQL insert problems
		this.query=query.replaceAll("'", "\'");
		this.content=content.replaceAll("'", "\'");
	}
	
	public void addRecordToDatabase(Connection conn) {
		String[] values= {date,query,content};
		CreateGHS_Database.addDataToTable("results",fieldNames,values,conn);
	}
}
