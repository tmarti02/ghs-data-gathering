package gov.epa.exp_data_gathering.parse;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import gov.epa.ghs_data_gathering.Database.CreateGHS_Database;
import gov.epa.ghs_data_gathering.Database.MySQL_DB;

public class RawDataRecordPubChem {

	public String date;//date accessed
	public String cid;//url accessed
	public String experimental;//raw experimental data as json
	public String identifiers;//raw id data as json
	public String cas;//raw cas data as json
	public String synonyms;//raw synonym data as txt
	
	public final static String [] fieldNames= {"date","cid","experimental","identifiers","cas","synonyms"};
	
	public RawDataRecordPubChem (String date,String cid,String experimental,String identifiers,String cas, String synonyms) {
		this.date=date;
		this.cid=cid;
		this.experimental=experimental;
		this.identifiers=identifiers;
		this.cas=cas;
		this.synonyms=synonyms;
	}
	
	public void addRecordToDatabase(String tableName,Connection conn) {
		String [] values= {date,cid,experimental,identifiers,cas,synonyms};
		CreateGHS_Database.addDataToTable(tableName, fieldNames, values, conn);
	}
	
	public boolean haveRecordInDatabase(String databasePath,String tableName,Connection conn) {

		try {
			Statement stat = MySQL_DB.getStatement(conn);
			ResultSet rs = MySQL_DB.getRecords(stat,tableName,"cid",cid);
			return rs.next();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		
	}
	
}
