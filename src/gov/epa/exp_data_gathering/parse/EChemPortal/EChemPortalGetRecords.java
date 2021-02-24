package gov.epa.exp_data_gathering.parse.EChemPortal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
import gov.epa.eChemPortalAPI.Processing.FinalRecord;
import gov.epa.eChemPortalAPI.Processing.FinalRecords;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;

public class EChemPortalGetRecords {
public static final String databasePathExperimentalRecords = "data/experimental/ToxicityRecords.db";
	
	public static ExperimentalRecords getExperimentalRecordsByPropertyName(String propertyName) {
		ExperimentalRecords records=new ExperimentalRecords();

		try {		
			Connection conn = SQLite_Utilities.getConnection(databasePathExperimentalRecords);
			Statement stat = conn.createStatement();
			String sql="Select * from ExperimentalRecords where property_name='"+propertyName+"'";
			System.out.println(sql);
			ResultSet rs = stat.executeQuery(sql);			
			while (rs.next()) {
				ExperimentalRecord record = new ExperimentalRecord();
				SQLite_GetRecords.createRecord(rs, record);
				records.add(record);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
		
	}
	
	public static FinalRecords getFinalRecordsByEndpointType(String endpointType) {
		FinalRecords records=new FinalRecords();

		try {		
			Connection conn = SQLite_Utilities.getConnection(databasePathExperimentalRecords);
			Statement stat = conn.createStatement();
			String sql="Select * from FinalRecords where endpointType='"+endpointType+"'";
			System.out.println(sql);
			ResultSet rs = stat.executeQuery(sql);			
			while (rs.next()) {
				FinalRecord record = new FinalRecord();
				SQLite_GetRecords.createRecord(rs, record);
				records.add(record);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
		
	}
}
