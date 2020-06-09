package gov.epa.ghs_data_gathering.Parse.ToxVal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;


import gov.epa.ghs_data_gathering.API.Chemical;
import gov.epa.ghs_data_gathering.API.Chemicals;

import gov.epa.ghs_data_gathering.Database.MySQL_DB;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_bcfbaf.ParseToxValBCFBAF;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_bcfbaf.RecordToxValBCFBAF;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_cancer_summary.ParseToxValCancer;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_cancer_summary.RecordToxValCancer;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_genetox_summary.ParseToxValGenetox;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_genetox_summary.RecordToxValGenetox;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_models.ParseToxValModels;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_models.RecordToxValModels;
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
    	
//    	System.out.println("\n"+SQL);
    	
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
    	
    	SQL+="WHERE chemical.casrn=\""+CAS+"\" AND "+table+".dtxsid is not null;";		
//    	SQL+="WHERE chemical.casrn=\""+CAS+"\";";
    	
//    	System.out.println("\n"+SQL);
    	
    	return SQL;
    	    	

    }

    private String createSQLQuery(String CAS,String table,String [] varlist,String [] fieldNames,String [] fieldValues) {
    	
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
    	
    	SQL+="WHERE chemical.casrn=\""+CAS+"\" AND ";
    	
    	for (int i=0;i<fieldNames.length;i++) {
    		SQL+=fieldNames[i]+" = \""+fieldValues[i]+"\"";
    		if (i<fieldNames.length-1) SQL+=" AND ";
    	}
    	
    	SQL+=";";
    	    			        	
//    	System.out.println("\n"+SQL);
    	
    	return SQL;
    	    	

    }
    
	
	
	void getDataFromTable_toxval(Chemical chemical) {
		
		try {
			
			String sql=createSQLQuery_toxval(chemical.CAS);				
			ResultSet rs=MySQL_DB.getRecords(statToxVal, sql);

			int count=0;
			
			while (rs.next()) {						 

				RecordToxVal r=new RecordToxVal();				
				createRecord(rs,r);
				ParseToxVal.createScoreRecord(chemical, r);
				//System.out.println(r.risk_assessment_class);
				count++;
			}
			
			System.out.println("Records in toxval table for "+chemical.CAS+" = "+count);

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
				RecordToxValCancer r=new RecordToxValCancer();			
				createRecord(rs, r);
				
				ParseToxValCancer.createScoreRecord(chemical, r, dictCC);
				//System.out.println(r.risk_assessment_class);
				count++;
			}
			
			System.out.println("Records in cancer_summary table for "+chemical.CAS+" = "+count);

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
				RecordToxValGenetox r=new RecordToxValGenetox();						
				createRecord(rs, r);						
				ParseToxValGenetox.createScoreRecord(chemical, r,dictCC);
				//System.out.println(r.risk_assessment_class);
				count++;
			}
			
			System.out.println("Records in genetox_summary table for "+chemical.CAS+" = "+count);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	void getDataFromTable_models(Chemical chemical) {
		
		try {
						
			
			createRecordBCF_OPERA(chemical);
			
			
			createRecordBCF_EPISUITE(chemical);
			
			//Create record based on opera biodegradation prediction:
			createRecordPersistence_OPERA(chemical);

			//Create record based on episuite biodegradation prediction:
			createRecordPersistence_EPISUITE(chemical);		

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}


// Note different spellings: "Biodegredation Half-life" vs. "Biodegradation Score"  -Leora
	
	private void createRecordPersistence_OPERA(Chemical chemical) throws SQLException {
		//Get OPERA Persistence (from Biodegredation Half-life).
		// I think I changed this code correctly.  -Leora
		
		String model="OPERA";
		String [] fieldNames= {"model","metric"};

		String [] fieldValuesPersistence= {model,"Biodegredation Half-life"};			
		String query=createSQLQuery(chemical.CAS, "models", RecordToxValModels.varlist, fieldNames, fieldValuesPersistence);			
		ResultSet rs=MySQL_DB.getRecords(statToxVal, query);			
	
		
		RecordToxValModels r=null;
		
		if (rs.next()) {
			r=new RecordToxValModels();						
			createRecord(rs, r);				
//			System.out.println("BCF value="+r.value);				
		} else {
			return;
		}

		
		ParseToxValModels.createScoreRecordPersistence_Opera(chemical, r);
	}


	private void createRecordPersistence_EPISUITE(Chemical chemical) throws SQLException {
		//Get EpiSuite Persistence:

		String model="EpiSuite";
		String [] fieldNames= {"model","metric"};

		String [] fieldValuesBCF= {model,"Biodegradation Score"};			
		String query=createSQLQuery(chemical.CAS, "models", RecordToxValModels.varlist, fieldNames, fieldValuesBCF);			
		ResultSet rs=MySQL_DB.getRecords(statToxVal, query);			


		RecordToxValModels r=null;
//		RecordToxValModels rBCF_AD=null;

		if (rs.next()) {
			r=new RecordToxValModels();						
			createRecord(rs, r);				
//			System.out.println("BCF value="+r.value);				
		} else {
			return;
		}


		ParseToxValModels.createScoreRecordPersistence_EpiSuite(chemical, r);
	}
		
		
	private void createRecordBCF_OPERA(Chemical chemical) throws SQLException {
		//Get OPERA BCF:
		
		String model="OPERA";
		String [] fieldNames= {"model","metric"};
		String [] fieldValuesBCF= {model,"BCF"};	
		String queryBCF=createSQLQuery(chemical.CAS, "models", RecordToxValModels.varlist, fieldNames, fieldValuesBCF);			
		ResultSet rsBCF=MySQL_DB.getRecords(statToxVal, queryBCF);	
	
		RecordToxValModels rBCF=null;
		RecordToxValModels rBCF_AD=null;
		
		if (rsBCF.next()) {
			rBCF=new RecordToxValModels();						
			createRecord(rsBCF, rBCF);				
//			System.out.println("BCF value="+r.value);				
		} else {
			return;
		}
		
		String [] fieldValuesBCF_AD= {model,"BCF_AD"};		
		String queryBCF_AD=createSQLQuery(chemical.CAS, "models", RecordToxValModels.varlist, fieldNames, fieldValuesBCF_AD);			
		ResultSet rsBCF_AD=MySQL_DB.getRecords(statToxVal, queryBCF_AD);
			
		if (rsBCF_AD.next()) {
			rBCF_AD=new RecordToxValModels();						
			createRecord(rsBCF_AD, rBCF_AD);										
//			System.out.println("BCF_AD value="+r.value);				
		} else {
//			System.out.println("No values for AD");
			return;
		}
		
		
		ParseToxValModels.createScoreRecordBCF_Opera(chemical, rBCF,rBCF_AD);
	}

	
	
	
	private void createRecordBCF_EPISUITE(Chemical chemical) throws SQLException {
		//Get OPERA BCF:

		String model="EpiSuite";

		String [] fieldNames= {"model","metric"};
		String [] fieldValuesBCF= {model,"BCF"};			
		String queryBCF=createSQLQuery(chemical.CAS, "models", RecordToxValModels.varlist, fieldNames, fieldValuesBCF);			
		ResultSet rsBCF=MySQL_DB.getRecords(statToxVal, queryBCF);			
		
		RecordToxValModels rBCF=null;
		
		if (rsBCF.next()) {
			rBCF=new RecordToxValModels();						
			createRecord(rsBCF, rBCF);				
//			System.out.println("BCF value="+r.value);				
		} else {
			return;
		}
		
		
		ParseToxValModels.createScoreRecordBCF_EPISUITE(chemical, rBCF);
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
				getDataFromTable_models(chemical);
				getDataFromTable_bcfbaf(chemical);//TODO

			}

			chemicals.writeToFile(destfilepathJson);
			chemicals.toFlatFile(destfilepathText, "\t");
			//			writeChemicalToFile(chemical, destfilepath);
			

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	

	 void getDataFromTable_bcfbaf(Chemical chemical) {

		 try {

				String sql=createSQLQuery(chemical.CAS,"bcfbaf",RecordToxValBCFBAF.varlist);				

//				System.out.println(sql);

				ResultSet rs=MySQL_DB.getRecords(statToxVal, sql);

				int count=0;


				while (rs.next()) {						 
					RecordToxValBCFBAF r=new RecordToxValBCFBAF();			
					createRecord(rs, r);

					ParseToxValBCFBAF.createScoreRecord(chemical, r);
					//System.out.println(r.risk_assessment_class);
					count++;
				}

				System.out.println("Records in bcfbaf table for "+chemical.CAS+" = "+count);

			} catch (Exception ex) {
				ex.printStackTrace();
			}

	}


	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ParseToxValDB p = new ParseToxValDB();
				
//		String folder = "C:\\Users\\Leora\\Desktop\\Tele\\ToxVal";
		 String folder="E:\\Documents\\0000 epa\\0 telework\\AA dashboard";

		Vector<String>casList=new Vector<String>();

		//**********************************************************************************
//		String CAS="79-06-1";
//		casList.add(CAS);
//		String filePathRecordsForCAS_json=folder+File.separator+"records_"+CAS+".json";		
//		String filePathRecordsForCAS_txt=folder+File.separator+"records_"+CAS+".txt";
//		p.goThroughRecordsMultipleChemicals(casList, filePathRecordsForCAS_json, filePathRecordsForCAS_txt);

		//**********************************************************************************
		casList.add("79-06-1");
		casList.add("79-01-6"); 
		casList.add("108-95-2"); 
		casList.add("50-00-0"); 
		casList.add("111-30-8");
		casList.add("302-01-2"); 
		casList.add("75-21-8"); 
		casList.add("7803-57-8"); 
		casList.add("101-77-9"); 
		casList.add("10588-01-9"); 
		
//		casList.add("107-13-1"); 
//		casList.add("110-91-8"); 
//		casList.add("106-93-4"); 
//		casList.add("67-56-1"); 
//		casList.add("7664-39-3"); 
//		casList.add("556-52-5"); 
//		casList.add("87-86-5"); 
//		casList.add("62-53-3"); 
//		casList.add("106-89-8"); 
//		casList.add("7778-50-9");
//		casList.add("123-45-6");
		
		
		String filePathRecordsForCASList_json=folder+File.separator+"toxval_pod_summary_top 10.json";		
		String filePathRecordsForCASList_txt=folder+File.separator+"toxval_pod_summary_Top10.txt";
		p.goThroughRecordsMultipleChemicals(casList, filePathRecordsForCASList_json, filePathRecordsForCASList_txt);
		
		
	}

}
