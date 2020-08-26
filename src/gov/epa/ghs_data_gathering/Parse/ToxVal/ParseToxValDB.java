package gov.epa.ghs_data_gathering.Parse.ToxVal;


import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.epa.ghs_data_gathering.API.Chemical;
import gov.epa.ghs_data_gathering.API.Chemicals;
import gov.epa.ghs_data_gathering.API.FlatFileRecord;
import gov.epa.ghs_data_gathering.API.FlatFileRecord2;
import gov.epa.ghs_data_gathering.API.Score;
import gov.epa.ghs_data_gathering.API.ScoreRecord;
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


	//	public static final String DB_Path_AA_Dashboard_Records = "C:\\Users\\Leora\\Desktop\\Tele\\ToxVal\\databases\\toxval_v8.db";//fast if you add index for CAS: "CREATE INDEX idx_CAS ON "+tableName+" (CAS)"

	//use relative path so dont have to keep changing this- i.e. it is relative to java installation:  "D:\Users\TMARTI02\OneDrive - Environmental Protection Agency (EPA)\0 java\ghs-data-gathering\AA Dashboard\databases\toxval_v8.db"
	public static final String DB_Path_AA_Dashboard_Records = "AA Dashboard/databases/toxval_v8.db";

	public static Statement statToxVal = MySQL_DB.getStatement(DB_Path_AA_Dashboard_Records);


	@Deprecated	
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

//		System.out.println("\n"+SQL);

		return SQL;



	}
	
	/**
	 * Richard Judson used this query to create spreadsheet from toxval table (toxval db v8) 
	 * 
	 * Note: did not filter on human_eco field (since we need some eco data for aquatic tox)
	 * 
	 * @param CAS
	 * @return
	 */
	private String createSQLQuery_toxval2(String CAS) {
		
		String SQL="SELECT\r\na.dtxsid, a.casrn,a.name,\r\n" + 
				"b.toxval_id, b.source,b.subsource,b.toxval_type,b.toxval_type_original,b.toxval_subtype,b.toxval_subtype_original,e.toxval_type_supercategory,\r\n" + 
				"b.toxval_numeric_qualifier,b.toxval_numeric_qualifier_original,b.toxval_numeric,b.toxval_numeric_original,\r\n" + 
				"b.toxval_numeric_converted, b.toxval_units,b.toxval_units_original,b.toxval_units_converted, b.risk_assessment_class,\r\n" + 
				"b.study_type,b.study_type_original,b.study_duration_class,b.study_duration_class_original, b.study_duration_value,\r\n" + 
				"b.study_duration_value_original,b.study_duration_units,b.study_duration_units_original,b.human_eco,\r\n" + 
				"b.strain,b.strain_original,b.sex,b.sex_original,b.generation,\r\n" + 
				"d.species_id,b.species_original,\r\n" + 
				"d.species_common,d.species_supercategory,d.habitat,\r\n" + 
				"b.lifestage,b.exposure_route,b.exposure_route_original,b.exposure_method,b.exposure_method_original,\r\n" + 
				"b.exposure_form,b.exposure_form_original, b.media,b.media_original,b.critical_effect,b.year,b.quality_id,b.priority_id,\r\n" + 
				"b.source_source_id,b.details_text,b.toxval_uuid,b.toxval_hash,b.datestamp,\r\n" + 
				"c.long_ref, c.title, c.author, c.journal, c.volume, c.issue, c.url, c.document_name, c.record_source_type, c.record_source_hash\r\n" + 
				"\r\n" + 
				
				"FROM toxval b\r\n" + 
				"INNER JOIN chemical a on a.dtxsid=b.dtxsid\r\n" + 
				"LEFT JOIN species d on b.species_id=d.species_id\r\n" + 
				"INNER JOIN toxval_type_dictionary e on b.toxval_type=e.toxval_type\r\n" + 
				"LEFT JOIN record_source c ON b.toxval_id=c.toxval_id\r\n" +				
				"WHERE\r\n"+
				"b.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')\r\n" + 				
				"AND e.toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')\r\n" + 
				"AND b.toxval_numeric>0\r\n" + 									
				"AND a.casrn=\""+CAS+"\";";		

//		System.out.println("\n"+SQL);

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

//			String sql=createSQLQuery_toxval(chemical.CAS);				
			String sql=createSQLQuery_toxval2(chemical.CAS);
			
			ResultSet rs=MySQL_DB.getRecords(statToxVal, sql);

//			int count=0;

			Hashtable<String,Vector<RecordToxVal>>records=new Hashtable<>();
									
			while (rs.next()) {						 

				RecordToxVal r=new RecordToxVal();							
				createRecord(rs,r);
								
				if (records.get(r.toxval_id)==null) {										
					Vector<RecordToxVal>recs=new Vector<>();
					recs.add(r);
					records.put(r.toxval_id,recs);								
				} else {
					Vector<RecordToxVal>recs=records.get(r.toxval_id);
					recs.add(r);
				}
				
				
//				ParseToxVal.createScoreRecord(chemical, r);
				//System.out.println(r.risk_assessment_class);
//				count++;
			}
			
			System.out.println("size="+records.size());

			//**************************************************************************************************************
			//The following only creates one record for each toxval_id and merges the references (separated by <br>)
			
			//get the entry set using the entrySet method
			Set<Map.Entry<String,Vector<RecordToxVal>>> entries = records.entrySet();
			 
			//iterate using the forEach
			entries.forEach( entry ->{				
				String toxval_id=entry.getKey();				
				Vector<RecordToxVal>recs=entry.getValue();			
				RecordToxVal r0=recs.get(0);//use first one				
				getDOI(r0);
				
//				System.out.println("0"+"\t"+r0.toxval_id);
				
				for (int i=1;i<recs.size();i++) {					
					RecordToxVal ri=recs.get(i);
					getDOI(ri);
					
//					System.out.println(i+"\t"+ri.toxval_id);
															
					if (!ri.long_ref.isEmpty()) r0.long_ref+="<br>"+ri.long_ref;									
					if (!ri.url.isEmpty()) r0.url+="<br>"+ri.url;					
				}
				
				r0.long_ref=r0.long_ref.trim();
				r0.url=r0.url.trim();
				
				if (r0.long_ref.indexOf("<br>")==0) {
					r0.long_ref=r0.long_ref.substring(5,r0.long_ref.length());
				}
				
				if (r0.url.indexOf("<br>")==0) {
					r0.url=r0.url.substring(5,r0.url.length());
				}
						
//				if (recs.size()>1) {
//					System.out.println("**"+r0.long_ref+"**");
//					System.out.println("@@"+r0.url+"@@\n");
//				}

				//Store the record for the first record in chemical class:
				ParseToxVal.createScoreRecord(chemical, r0);
			});
														
			
			System.out.println("Records in toxval table for "+chemical.CAS+" = "+records.size());
			
//			System.out.println("CAS="+chemical.CAS);
//			System.out.println("records.size()="+records.size());
//			System.out.println("Records in toxval table for "+chemical.CAS+" = "+count);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	void getDOI(RecordToxVal r) {
		
		if (r.long_ref.contains("doi:") && r.url.isEmpty()) {
			r.url=r.long_ref.substring(r.long_ref.indexOf("doi: ")+5,r.long_ref.length());
//			System.out.println("new url="+r.url);
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
			//			createRecordPersistence_OPERA(chemical);

			//Create record based on episuite biodegradation prediction:
			
			createRecordPersistence_EPISUITE(chemical);		

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	//	I'm commenting this out since we're not including the OPERA persistence model data from ToxVal,
	//	as discussed.  -Leora

	//	// Note different spellings: "Biodegredation Half-life" vs. "Biodegradation Score"  -Leora
	//	
	//	private void createRecordPersistence_OPERA(Chemical chemical) throws SQLException {
	//		//Get OPERA Persistence (from Biodegredation Half-life).
	//		// I think I changed this code correctly.  -Leora
	//		
	//		String model="OPERA";
	//		String [] fieldNames= {"model","metric"};
	//
	//		String [] fieldValuesPersistence= {model,"Biodegredation Half-life"};			
	//		String query=createSQLQuery(chemical.CAS, "models", RecordToxValModels.varlist, fieldNames, fieldValuesPersistence);			
	//		ResultSet rs=MySQL_DB.getRecords(statToxVal, query);			
	//	
	//		
	//		RecordToxValModels r=null;
	//		
	//		if (rs.next()) {
	//			r=new RecordToxValModels();						
	//			createRecord(rs, r);				
	////			System.out.println("BCF value="+r.value);				
	//		} else {
	//			return;
	//		}
	//
	//		
	//		ParseToxValModels.createScoreRecordPersistence_Opera(chemical, r);
	//	}


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

	Chemicals goThroughRecordsMultipleChemicals(Vector<String>casList,String destfilepathJson, String destfilepathText) {


		try {

			Chemicals chemicals = new Chemicals();

			Chemical chemical = new Chemical();			

			for (int i=0;i<casList.size();i++) {

				String CAS=casList.get(i);

				chemical = new Chemical();
				chemical.CAS = CAS;				
				chemicals.add(chemical);

				getDataFromTable_toxval(chemical);
				
// ***Uncomment these later.***
//				getDataFromTable_cancer_summary(chemical);
//				getDataFromTable_genetox_summary(chemical);
//				getDataFromTable_models(chemical);
//				getDataFromTable_bcfbaf(chemical);//TODO

			}

			chemicals.writeToFile(destfilepathJson);
			chemicals.toFlatFile(destfilepathText, "\t");
			return chemicals;
			//			writeChemicalToFile(chemical, destfilepath);


		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
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
		//	 String folder="E:\\Documents\\0000 epa\\0 telework\\AA dashboard";
		String folder="AA dashboard/toxval";//use relative path so dont have to keep changing this- i.e. it is relative to java installation:  "D:\Users\TMARTI02\OneDrive - Environmental Protection Agency (EPA)\0 java\ghs-data-gathering\AA Dashboard\toxval"


		//***************************************************************************// 

		
		String CAS="79-06-1";
//		String CAS="123-91-1"; // casList.add(CAS);
//		CAS="75-73-0";

//		String CAS="75-73-0";
		
		
		Vector<String>casList=new Vector<String>();
		casList.add(CAS);
//		casList.add("79-06-1");
		
	//	casList.add("76-16-4");

		String filePathRecordsForCAS_json=folder+File.separator+"records_"+CAS+".json"; //
		String filePathRecordsForCAS_txt=folder+File.separator+"records_"+CAS+".txt";
		Chemicals chemicals=p.goThroughRecordsMultipleChemicals(casList, filePathRecordsForCAS_json,filePathRecordsForCAS_txt);
		
		String filePathExcelManual=folder+"/manual 79-06-1.xlsx";
		compareWithManual(chemicals,filePathExcelManual);

				
		//***************************************************************************


//		casList.add("76-16-4");
//		casList.add("7664-39-3");
		
		
//		casList.add("79-06-1"); casList.add("79-01-6"); casList.add("108-95-2");
//		casList.add("50-00-0"); casList.add("111-30-8"); casList.add("302-01-2");
//		casList.add("75-21-8"); casList.add("7803-57-8"); casList.add("101-77-9");
//		casList.add("10588-01-9");
//
//
//		casList.add("107-13-1");  casList.add("110-91-8"); //
//		casList.add("106-93-4");  casList.add("67-56-1"); //
//		casList.add("7664-39-3");  casList.add("556-52-5"); //
//		casList.add("87-86-5");  casList.add("62-53-3"); //
//		casList.add("106-89-8");  casList.add("7778-50-9"); //
//		casList.add("123-45-6");
//
//
//		String filePathRecordsForCASList_json=folder+File.separator+"toxval_pod_summary_top 10.json"; String
//		filePathRecordsForCASList_txt=folder+File.separator+"toxval_pod_summary_Top10.txt"; 
//		p.goThroughRecordsMultipleChemicals(casList,filePathRecordsForCASList_json, filePathRecordsForCASList_txt);


	}

	static Vector<FlatFileRecord> getManualResults(String excelFilePath) {
		
		Vector<FlatFileRecord>recs=new Vector<>();
		
		try
		{
											
			FileInputStream file = new FileInputStream(new File(excelFilePath));

			//Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);

			
			for (int i=0;i<workbook.getNumberOfSheets();i++) {
				//Get first/desired sheet from the workbook
				XSSFSheet sheet = workbook.getSheetAt(i);

				getRecordsFromSheet(recs, sheet);
			}
			System.out.println("here size="+recs.size());
			
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return recs;
		
	}

	private static void getRecordsFromSheet(Vector<FlatFileRecord> recs, XSSFSheet sheet) {
		try {
			Row row=sheet.getRow(0);

			Hashtable<String,Integer>htColNums=new Hashtable<>();

			for (int i=0;i<row.getLastCellNum();i++) {
				Cell cell=row.getCell(i);
				String colName=cell.getStringCellValue();
				htColNums.put(colName,new Integer(i));
			}

			for (int i=1;i<sheet.getLastRowNum();i++) {
				FlatFileRecord f=new FlatFileRecord();

				Row rowi=sheet.getRow(i);
				f.toxval_id=(int)(rowi.getCell(htColNums.get("toxval_id")).getNumericCellValue())+"";
				f.hazard_name=rowi.getCell(htColNums.get("ManualHazardEndpointCategorization")).getStringCellValue();
				f.score=rowi.getCell(htColNums.get("ManualScore")).getStringCellValue();


				if (htColNums.get("Note")!=null) {
					Cell cell=rowi.getCell(htColNums.get("Note"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
									
//					System.out.println("cell value="+cell.getStringCellValue());
					
					f.note=cell.getStringCellValue();//store leora's note
				}

				if (!hasRecord(recs, f.toxval_id))
					recs.add(f);

				//				System.out.println(f.toxval_id+"\t"+f.hazard_name+"\t"+f.score);

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	static boolean hasRecord(Vector<FlatFileRecord> recs,String toxval_id) {
		for (int i=0;i<recs.size();i++) {
			if (recs.get(i).toxval_id.contentEquals(toxval_id)) return true;
		}
		return false;
	}
	
	static Hashtable<String,FlatFileRecord>getHashtable(Vector<FlatFileRecord>records) {
		Hashtable<String,FlatFileRecord> ht=new Hashtable<>();
		
		for (int i=0;i<records.size();i++) {
			FlatFileRecord rec=records.get(i);
			ht.put(rec.toxval_id, rec);
		}
		return ht;
	}
	
	
	private static void compareWithManual(Chemicals chemicals,String excelFilePath) {
		
		Vector<FlatFileRecord>recordsManual=getManualResults(excelFilePath);
		Vector<FlatFileRecord>recordsJava=getJavaRecords(chemicals);
		
		
//		for (int i=0;i<recordsJava.size();i++) {
//			FlatFileRecord recJava=recordsJava.get(i);
//			System.out.println("recJava: "+i+"\t"+recJava.toxval_id);
//		}
		
		Hashtable<String,FlatFileRecord>htManual=getHashtable(recordsManual);
		Hashtable<String,FlatFileRecord>htJava=getHashtable(recordsJava);
		
		System.out.println("\nLooping through manual records:");
		//First loop through manual records to find records present in manual but not in java:
		for (int i=0;i<recordsManual.size();i++) {
			FlatFileRecord recManual=recordsManual.get(i);
			
			if (recManual.hazard_name.contentEquals("Exclude")) continue;
			
			if (htJava.get(recManual.toxval_id)==null) {
			
				System.out.println(recManual.toxval_id+" present in manual, not in Java");
			
			} else {
				FlatFileRecord recJava=htJava.get(recManual.toxval_id);
				
				if (!recManual.hazard_name.contentEquals(recJava.hazard_name)) {						
					System.out.println(recJava.toxval_id+"\t"+recJava.hazard_name+"\t"+recManual.hazard_name+"\tmismatch hazard name\t"+recManual.note);						
				}
				
				if (!recManual.score.contentEquals(recJava.score)) {						
					System.out.println(recJava.toxval_id+"\t"+recJava.score+"\t"+recManual.score+"\tmismatch score\t"+recManual.note);						
				} 
				
			}
		}
		
					
		//Second loop through java records to find records in java but not in manual:
		
		System.out.println("\nLooping through java records:");
		
		for (int i=0;i<recordsJava.size();i++) {
			FlatFileRecord recJava=recordsJava.get(i);
									
			if (htManual.get(recJava.toxval_id)==null) {			
				System.out.println(recJava.toxval_id+" present in Java, not in manual");			
			} 
		}
				
	}

	private static Vector<FlatFileRecord> getJavaRecords(Chemicals chemicals) {
		Vector<FlatFileRecord>recordsJava=new Vector<>();
		
		//Go through the all the records
		for (int i=0;i<chemicals.size();i++) {
			Chemical chemical=chemicals.get(i);
									
			for (int j=0;j<chemical.scores.size();j++) {
				
				Score score=chemical.scores.get(j);
				
				for (int k=0;k<score.records.size();k++) {
					
					ScoreRecord sr=score.records.get(k);
					
					FlatFileRecord recJava=new FlatFileRecord();
					recJava.toxval_id=sr.toxval_id;
					recJava.hazard_name=score.hazard_name;
					recJava.score=sr.score;					
					
					recordsJava.add(recJava);					
				}				
			}
		}
//		System.out.println(recordsJava.size());
		return recordsJava;
	}

}
