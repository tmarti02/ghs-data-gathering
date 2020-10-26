package gov.epa.ghs_data_gathering.GetData.ChemIDplus;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Database.MySQL_DB;



public class getAcuteMammalianTox {
	
	
	public static final String DB_Path_AA_Dashboard_Records = "databases/AA dashboard.db";


	String getQuery(String source,String hazardName,String species) {
		return "select * from HazardRecords where source=\""+source+"\" and hazardName=\""+hazardName+"\" and testOrganism=\""+species+"\"  and valueMassOperator=\"\""; 		
	}
	
	private  static ScoreRecord createScoreRecord(ResultSet rs) {
		ScoreRecord f=new ScoreRecord(null,null,null);
		
		//This assumes fields in database are also in ScoreRecord
		try {

			ResultSetMetaData rsmd = rs.getMetaData();
			
			for (int i = 1; i < rsmd.getColumnCount(); i++) {

				String name = rsmd.getColumnName(i);
				
				Field myField = f.getClass().getDeclaredField(name);

				if (myField.getType().getName().contains("Double")) {
					double val=rs.getDouble(i);
					//						System.out.println("*"+val);

					myField.set(f, val);

				} else {
					String val=rs.getString(i);

					if (val!=null) {
						myField.set(f, val);
					} 
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		
		return f;

	}
	
	
	Vector<ScoreRecord>getData(String source,String hazardName,String species) {
				
		try {
						
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_Path_AA_Dashboard_Records);

			String sql=getQuery(source,hazardName,species);
			
			ResultSet rs=MySQL_DB.getRecords(conn.createStatement(), sql);

			Vector<ScoreRecord>records=new Vector<>();
		
			
			while (rs.next()) {						 
										
				ScoreRecord sr=createScoreRecord(rs);
				records.add(sr);						
			}
			
//			for (int i=0;i<records.size();i++) {
//				System.out.println(i+"\t"+records.get(i).toString("|"));
//			}
						
			return records; 
		
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}
	
	Hashtable<String,ScoreRecord>getHT(Vector<ScoreRecord>records) {
		
		Hashtable<String,ScoreRecord>ht=new Hashtable<>();
		
		for (ScoreRecord sr:records) {
			ht.put(sr.CAS,sr);			
		}
		return ht;
		
	}
	
	
	void removeDuplicates(Vector<ScoreRecord>records) {
		
		Hashtable<String,Vector<ScoreRecord>>ht=new Hashtable<>();
		
		for (ScoreRecord sr:records) {
			
			if (ht.get(sr.CAS)==null) {
				Vector<ScoreRecord>recordsForCAS=new Vector<>();
				recordsForCAS.add(sr);
				ht.put(sr.CAS,recordsForCAS);
			} else {
				Vector<ScoreRecord>recordsForCAS=ht.get(sr.CAS);
				recordsForCAS.add(sr);
			}			
		}
		
		
//		System.out.println("Before dup removal:"+records.size());
		
		Enumeration<String> keys = ht.keys();
		
		int numDups=0;
		
		//iterate using while loop
		while( keys.hasMoreElements() ){
		    String CAS=keys.nextElement();
		    
		    Vector<ScoreRecord>recordsForCAS=ht.get(CAS);
		    
		    if (recordsForCAS.size()>1) {		    		    	
		    	removeDuplicatesForCAS(records,recordsForCAS);		    	
		    	numDups++;
		    }
		    
		}
//		System.out.println(numDups);
//				
//		System.out.println("After dup removal:"+records.size());
		
		String  [] fields= {"CAS","name","valueMassOperator","valueMass","valueMassUnits","effect","url"};

//		System.out.println(ScoreRecord.getHeader("|",fields));
//		for (ScoreRecord sr:records) {
//			System.out.println(sr.toString("|",fields));
//		}
	}
	
	
	public class CustomComparator implements Comparator<ScoreRecord> {
	    @Override
	    public int compare(ScoreRecord o1, ScoreRecord o2) {
	        return o1.valueMass.compareTo(o2.valueMass);
	    }
	}
	
	/**
	 * Keep most toxic value
	 * 
	 * @param records
	 * @param recordsForCAS
	 */
	void removeDuplicatesForCAS(Vector<ScoreRecord>records,Vector<ScoreRecord>recordsForCAS) {
		
		Collections.sort(recordsForCAS, new CustomComparator());
				
		while (recordsForCAS.size()>1) {
			
			ScoreRecord sr=recordsForCAS.get(1);
			
			recordsForCAS.remove(sr);
			records.remove(sr);
		}
		
		
	}
			

	void getIntersection (Vector<ScoreRecord>records1,Vector<ScoreRecord>records2) {
		
		Hashtable<String,ScoreRecord>ht2=getHT(records2);
		
		
		for (ScoreRecord sr1:records1) {
			
			if (ht2.get(sr1.CAS)!=null) {
				ScoreRecord sr2=ht2.get(sr1.CAS);
				
				System.out.println(sr1.CAS+"|"+sr1.name+"|"+sr1.valueMass+"|"+sr2.valueMass);
				
			}
			
		}
		
		
	}
	
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		getAcuteMammalianTox g=new getAcuteMammalianTox();
		
		Vector<ScoreRecord>recordsOral=g.getData("ChemIDplus","Acute Mammalian Toxicity Oral","rat");
		g.removeDuplicates(recordsOral);
		
		Vector<ScoreRecord>recordsInhalation=g.getData("ChemIDplus","Acute Mammalian Toxicity Inhalation","rat");
		g.removeDuplicates(recordsInhalation);
		
		g.getIntersection(recordsInhalation, recordsOral);

	}

}
