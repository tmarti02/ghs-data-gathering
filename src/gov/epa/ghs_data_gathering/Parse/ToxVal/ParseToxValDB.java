package gov.epa.ghs_data_gathering.Parse.ToxVal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;


import gov.epa.ghs_data_gathering.API.Chemical;
import gov.epa.ghs_data_gathering.API.Chemicals;

import gov.epa.ghs_data_gathering.Database.MySQL_DB;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_cancer_summary.ParseToxValCancer;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_cancer_summary.RecordToxValCancer;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_genetox_summary.ParseToxValGenetox;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_genetox_summary.RecordToxValGenetox;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval.ParseToxVal;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval.RecordToxVal;

public class ParseToxValDB {

	
	
    public static final String DB_Path_AA_Dashboard_Records = "AA Dashboard/databases/toxval_v8.db";//fast if you add index for CAS: "CREATE INDEX idx_CAS ON "+tableName+" (CAS)"
	
    public static Statement statToxVal = MySQL_DB.getStatement(DB_Path_AA_Dashboard_Records);

	
    private String createSQLQuery_toxval(String CAS) {
    	
    	String SQL="SELECT ";
    	    	    	
    	for(String field : RecordToxVal.varlist) {    		

    		//Following comes from chemical_list table:
    		if(field.contentEquals("casrn")) continue;//skip it
    		if(field.contentEquals("name")) continue;
    		
    		//Following comes from toxval_type_dictionary:
    		if (field.contentEquals("toxval_type_supercategory")) continue; //do we need this field? need to add another join
    		
    		//Following comes from species table, not toxval table:
    		if (field.contentEquals("species_common")) continue; 
    		if (field.contentEquals("species_supercategory")) continue; 
    		if (field.contentEquals("habitat")) continue; 
    	
    		//Following comes from record_source table, not toxval table:
    		if (field.contentEquals("long_ref")) continue; 
    		if (field.contentEquals("title")) continue; 
    		if (field.contentEquals("author")) continue; 
    		if (field.contentEquals("journal")) continue; 
    		if (field.contentEquals("volume")) continue; 
    		if (field.contentEquals("issue")) continue; 
    		if (field.contentEquals("url")) continue; 
    		if (field.contentEquals("document_name")) continue; 
    		if (field.contentEquals("record_source_type")) continue; 
    		if (field.contentEquals("record_source_hash")) continue; 
    		
    		SQL+="toxval."+field+", ";    		
    	}
    	
    	SQL+="chemical.casrn, chemical.name, ";
    	SQL+="species.species_common, species.species_supercategory, species.habitat, \n";
    	SQL+="long_ref, title, author, journal, volume, issue, url, document_name, record_source_type, record_source_hash \n";
    	    	
    	SQL+="FROM chemical\n";
    	
    	SQL+="LEFT JOIN toxval ON toxval.dtxsid = chemical.dtxsid\n";
    	SQL+="LEFT JOIN species ON toxval.species_id=species.species_id\n";
    	SQL+="LEFT JOIN record_source ON toxval.toxval_id=record_source.toxval_id\n";
    	
    	SQL+="WHERE chemical.casrn=\""+CAS+"\";";		
    	
    	System.out.println(SQL);
    	
    	return SQL;
    	    	    	

    }
    
        
    
    private String createSQLQuery(String CAS,String table,String [] varlist) {
    	

    	String SQL="SELECT ";
    	
    	    	    	
    	for(String field : varlist) {    		

    		//Following comes from chemical_list table:
    		if(field.contentEquals("casrn")) continue;
    		if(field.contentEquals("name")) continue;
    		    		
    		SQL+=table+"."+field+", ";    		
    	}
    	
    	SQL+="chemical.casrn, chemical.name\n";   	    	
    	SQL+="FROM chemical\n";    	
    	SQL+="LEFT JOIN "+table+" ON "+table+".dtxsid = chemical.dtxsid\n";
    	
    	SQL+="WHERE chemical.casrn=\""+CAS+"\";";		
    	
    	System.out.println(SQL);
    	
    	return SQL;
    	    	

    }

	
	
	void getDataFromTable_toxval(Chemical chemical) {
		
		try {
			
			String sql=createSQLQuery_toxval(chemical.CAS);				
			ResultSet rs=MySQL_DB.getRecords(statToxVal, sql);

			int count=0;
			
			while (rs.next()) {						 
				RecordToxVal r=createRecordToxVal(rs);
				ParseToxVal.createScoreRecord(chemical, r);
				//System.out.println(r.risk_assessment_class);
				count++;
			}
			
			System.out.println("toxval count for "+chemical.CAS+" = "+count);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	void getDataFromTable_cancer_summary(Chemical chemical) {
		
		try {
			
			String sql=createSQLQuery(chemical.CAS,"cancer_summary",RecordToxValCancer.varlist);				
			ResultSet rs=MySQL_DB.getRecords(statToxVal, sql);

			int count=0;
			
			Hashtable<String,String>dictCC=ParseToxValCancer.populateCancerCallToScoreValue();
			
			while (rs.next()) {						 
				RecordToxValCancer r=createRecordToxValCancer(rs);
				ParseToxValCancer.createScoreRecord(chemical, r, dictCC);
				//System.out.println(r.risk_assessment_class);
				count++;
			}
			
			System.out.println("toxval count for "+chemical.CAS+" = "+count);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	void getDataFromTable_genetox_summary(Chemical chemical) {
		
		try {
			
			String sql=createSQLQuery(chemical.CAS,"genetox_summary",RecordToxValGenetox.varlist);				
			ResultSet rs=MySQL_DB.getRecords(statToxVal, sql);

			int count=0;
			
			Hashtable<String,String>dictCC=ParseToxValGenetox.populateGenetoxCallToScoreValue();
			
			
			while (rs.next()) {						 
				RecordToxValGenetox r=createRecordToxValGenetox(rs);
				ParseToxValGenetox.createScoreRecord(chemical, r,dictCC);
				//System.out.println(r.risk_assessment_class);
				count++;
			}
			
			System.out.println("toxval count for "+chemical.CAS+" = "+count);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
    
    
    

	private static void createRecord(ResultSet rs, Object r) {
		ResultSetMetaData rsmd;
		try {
			rsmd = rs.getMetaData();

			int columnCount = rsmd.getColumnCount();

			// The column count starts from 1
			for (int i = 1; i <= columnCount; i++ ) {
				String name = rsmd.getColumnName(i);
//				System.out.println(name);								
				String val=rs.getString(i);

//				System.out.println(name+"\t"+val);
				
				if (val!=null) {
					Field myField = r.getClass().getDeclaredField(name);			
					myField.set(r, val);
				}
				
			}


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void goThroughRecordsMultipleChemicals(Vector<String>casList,String destfilepathJson, String destfilepathText) {
			

		try {

			Chemicals chemicals = new Chemicals();

			Chemical chemical = new Chemical();			

			for (int i=0;i<casList.size();i++) {

				String CAS=casList.get(i);

				chemical = new Chemical();
				chemical.CAS = CAS;				
				chemicals.add(chemical);
											
				getDataFromTable_toxval(chemical);
				getDataFromTable_cancer_summary(chemical);
				getDataFromTable_genetox_summary(chemical);
				

			}

			chemicals.writeToFile(destfilepathJson);
			chemicals.toFlatFile(destfilepathText, "\t");
			//			writeChemicalToFile(chemical, destfilepath);
			

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	private  static RecordToxVal createRecordToxVal(ResultSet rs) {
		RecordToxVal r=new RecordToxVal();		
		createRecord(rs,r);
		return r;		 
	}
	
	private static RecordToxValCancer createRecordToxValCancer(ResultSet rs) {
		// TODO Auto-generated method stub
		RecordToxValCancer r=new RecordToxValCancer();		
		createRecord(rs, r);
		return r;
	}
	
	private static RecordToxValGenetox createRecordToxValGenetox(ResultSet rs) {
		// TODO Auto-generated method stub
		RecordToxValGenetox r=new RecordToxValGenetox();		
		createRecord(rs, r);
		return r;
	}


	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ParseToxValDB p = new ParseToxValDB();
				
		
		String folder="E:\\Documents\\0000 epa\\0 telework\\AA dashboard";

		Vector<String>casList=new Vector<String>();
		
		String CAS="79-06-1";
		
		casList.add(CAS);
		
		String filePathRecordsForCAS_json=folder+File.separator+"records_"+CAS+".json";		
		String filePathRecordsForCAS_txt=folder+File.separator+"records_"+CAS+".txt";
		p.goThroughRecordsMultipleChemicals(casList, filePathRecordsForCAS_json, filePathRecordsForCAS_txt);
		
//		String filePathRecordsForCASList_json=folder+File.separator+"toxval_pod_summary_top 20.json";		
//		String filePathRecordsForCASList_txt=folder+File.separator+"toxval_pod_summary_Top20.txt";
//		p.goThroughRecordsMultipleChemicals(casList, filePathRecordsForCASList_json, filePathRecordsForCASList_txt);
		
		
	}

}
