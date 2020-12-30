package gov.epa.exp_data_gathering.parse;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import gov.epa.database.SQLite_CreateTable;
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;

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
		SQLite_CreateTable.addDataToTable(tableName, fieldNames, values, conn);
	}
	
	public boolean haveRecordInDatabase(String databasePath,String tableName,Connection conn) {

		try {
			Statement stat = SQLite_Utilities.getStatement(conn);
			ResultSet rs = SQLite_GetRecords.getRecords(stat,tableName,"cid",cid);
			return rs.next();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		
	}
	
}
