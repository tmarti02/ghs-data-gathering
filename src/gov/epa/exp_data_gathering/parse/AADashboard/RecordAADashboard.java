package gov.epa.exp_data_gathering.parse.AADashboard;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.io.File;
import java.sql.Connection;  
import java.sql.DriverManager;  
import java.sql.SQLException;  
import gov.epa.api.ExperimentalConstants;
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
import java.sql.Statement;
import java.util.Vector;



public class RecordAADashboard {
	public String CAS;
	public String name;
	public String hazardName;
	public String source;
	public String sourceOriginal;
	public String score;
	public String listType;
	public String route;
	public String category;
	public String hazardCode;
	public String hazardStatement;
	public String rationale;
	public String note;
	public String note2;
	public String toxvalID;
	public String testOrganism;
	public String testType;
	public String valueMassOperator;
	public String valueMass;
	public String valueMassUnits;
	public String effect;
	public String duration;
	public String durationUnits;
	public String url;
	public String longRef;
	
public static final String sourceName = "AADashboard";


    public static void main(String[] args) {
    	Vector<RecordAADashboard> records = databaseReader("AA dashboard.db", RecordAADashboard.sourceName);
    	for (int i = 0; i < records.size(); i++) {
    		System.out.println(records.get(i).CAS);
    	}
	    }
	
    
	public static Vector<RecordAADashboard> databaseReader(String fileName, String sourceName) {
		Vector<RecordAADashboard> records = new Vector<>();
		
		String filePath = "databases" + File.separator + fileName;
        Connection conn = SQLite_Utilities.getConnection(filePath);
        
        try {
        	Statement stat = SQLite_Utilities.getStatement(conn);
    		ResultSet rs = SQLite_GetRecords.getAllRecords(stat, "HazardRecords");
    		
    		int counter = 1;
    		
    		while (rs.next()) {
    			if (counter % 100==0) { System.out.println("Parsed "+counter+" pages"); }
    			
    			RecordAADashboard raad = new RecordAADashboard();
    			SQLite_GetRecords.createRecord(rs, raad);
    			records.add(raad);

    			counter++;
        		}

    	} catch (Exception e) {
    		e.printStackTrace();
    	}	
        return records;
	}
		



}
