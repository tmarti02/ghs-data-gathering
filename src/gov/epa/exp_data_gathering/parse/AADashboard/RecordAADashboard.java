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
	String CAS;
	String name;
	String hazardName;
	String source;
	String sourceOriginal;
	String score;
	String listType;
	String route;
	String category;
	String hazardCode;
	String hazardStatement;
	String rationale;
	String note;
	String note2;
	String toxvalID;
	String testOrganism;
	String testType;
	String valueMassOperator;
	String valueMass;
	String valueMassUnits;
	String effect;
	String duration;
	String durationUnits;
	String url;
	String longRef;
	
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
    			RecordAADashboard raad = new RecordAADashboard();
    			if (counter % 100==0) { System.out.println("Parsed "+counter+" pages"); }
    			
    			raad.CAS = rs.getString("CAS");
    			raad.name = rs.getString("name");
    			raad.hazardName = rs.getString("hazardName");
    			raad.source = rs.getString("source");
    			raad.sourceOriginal = rs.getString("sourceOriginal");
    			raad.score = rs.getString("score");
    			raad.listType = rs.getString("listType");
    			raad.route = rs.getString("route");
    			raad.category = rs.getString("category");
    			raad.hazardCode = rs.getString("hazardCode");
    			raad.hazardStatement = rs.getString("hazardStatement");
    			raad.rationale = rs.getString("rationale");
    			raad.note = rs.getString("note");
    			raad.note2 = rs.getString("note2");
    			raad.toxvalID = rs.getString("toxvalID");
    			raad.testOrganism = rs.getString("testOrganism");
    			raad.testType = rs.getString("testType");
    			raad.valueMassOperator = rs.getString("valueMassOperator");
    			raad.valueMass = rs.getString("valueMass");
    			raad.valueMassUnits = rs.getString("valueMassUnits");
    			raad.effect = rs.getString("effect");
    			raad.duration = rs.getString("duration");
    			raad.durationUnits = rs.getString("durationUnits");
    			raad.url = rs.getString("url");
    			raad.longRef = rs.getString("longRef");
    			
    			counter++;
    			records.add(raad);
        		}

    	} catch (Exception e) {
    		e.printStackTrace();
    	}	
        return records;
	}
		



}
