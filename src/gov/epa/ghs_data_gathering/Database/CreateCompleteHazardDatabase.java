package gov.epa.ghs_data_gathering.Database;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.google.gson.reflect.TypeToken;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import AADashboard.Application.MySQL_DB;
//import ToxPredictor.Application.GUI.TESTApplication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.AADashboard;
import gov.epa.api.Chemical;
import gov.epa.api.DsstoxLookup;
import gov.epa.api.DsstoxLookup.DsstoxRecord;
import gov.epa.api.ExperimentalConstants;
import gov.epa.api.FlatFileRecord2;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;

import gov.epa.database.SQLite_GetRecords;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.UnitConverter;
import gov.epa.exp_data_gathering.parse.CompareExperimentalRecords;
import gov.epa.exp_data_gathering.parse.CompareExperimentalRecords.ExperimentalRecordManipulator;
import gov.epa.exp_data_gathering.parse.CompareExperimentalRecords.Source;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.ghs_data_gathering.Parse.OPERA.ParseOPERA;
import gov.epa.ghs_data_gathering.Parse.ToxVal.MySQL_DB;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseToxValDB;
import gov.epa.ghs_data_gathering.Parse.ToxVal.SqlUtilities;
import gov.epa.ghs_data_gathering.Parse.ToxVal.Utilities;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_bcfbaf.ParseToxValBCFBAF;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_bcfbaf.RecordToxValBCFBAF;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_cancer_summary.ParseToxValCancer;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_cancer_summary.RecordToxValCancer;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_genetox_summary.ParseToxValGenetox;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_genetox_summary.RecordToxValGenetox;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_models.ParseToxValModels;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_models.RecordToxValModels;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval.CreateAquaticToxicityRecords;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval.ParseToxVal;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval.RecordToxVal;
import gov.epa.exp_data_gathering.parse.CompareExperimentalRecords.Source;

/**
 * @author TMARTI02
 */
public class CreateCompleteHazardDatabase {

	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();

	final static  String formatHazard="hazard";
	final static String formatToxval="toxval";

	//	ParseToxValDB p=new ParseToxValDB();

	class RecordChemical {
		String dtxsid;
		String casrn;
		String name;
	}

	MySqlExport mySqlExport=new MySqlExport();


	/**
	 * Exports from mysql db to sqlite copy
	 */
	class MySqlExport {

		
		void createToxValCopy(Connection connMySql,Connection connDest,String versionToxVal) {
			exportChemical(connMySql, connDest);
			exportCancer(connMySql,connDest);//no change from v94 to v96
			exportGenetoxSummary(connMySql,connDest);//no change from v94 to v96
			exportToxval(versionToxVal, connMySql,connDest);
		}


		/**
		 * Exports from bcfbaf table (from Arnot 2006)
		 * 
		 * Should use either ExperimentalRecords json or records loaded into res_qsar
		 * 
		 * @param connSrc
		 * @param connDest
		 */
		@Deprecated
		void exportBCFBAF(Connection connSrc,Connection connDest) {

			String tableName="bcfbaf";

			ParseToxValDB p=new ParseToxValDB();

			try {
				String []varlist= RecordToxValBCFBAF.varlist;
				initTableFresh(connDest, tableName, varlist);			

				Statement statToxVal=connSrc.createStatement();
				String sql=p.createSQLQueryByTable2(tableName,varlist);				

				System.out.println(sql);

				ResultSet rs = statToxVal.executeQuery(sql);

				List<Object>records=new ArrayList<>();
				while (rs.next()) {						 
					RecordToxValBCFBAF r=new RecordToxValBCFBAF();			
					ParseToxValDB.createRecord(rs, r);
					records.add(r);
				}

				SqlUtilities.batchCreate(tableName, null, varlist,records, connDest);
				//			System.out.println(gson.toJson(records));
				System.out.println(records.size());

			} catch (Exception ex) {
				ex.printStackTrace();
			}		

		}

		/**
		 * 
		 * @param connSrc - mysql toxval db
		 * @param connDest - new sqlite db
		 */
		void exportCancer(Connection connSrc,Connection connDest) {

			String tableName="cancer_summary";

			try {
				String []varlist= {"dtxsid","source","cancer_call","exposure_route","url"};
				initTableFresh(connDest, tableName, varlist);			

				Statement statToxVal=connSrc.createStatement();
				String sql=ParseToxValDB.createSQLQueryByTable(tableName,varlist);				
				ResultSet rs = statToxVal.executeQuery(sql);

				List<Object>records=new ArrayList<>();
				while (rs.next()) {
					RecordToxValCancer r=new RecordToxValCancer();			
					ParseToxValDB.createRecord(rs, r);
					records.add(r);
				}

				System.out.println(records.size());
				SqlUtilities.batchCreate(tableName, null, varlist,records, connDest);
				//			System.out.println(gson.toJson(records));


			} catch (Exception ex) {
				ex.printStackTrace();
			}		

		}

		void exportChemical(Connection connSrc,Connection connDest) {

			String tableName="chemical";

			ParseToxValDB p=new ParseToxValDB();

			try {
				String []varlist= {"dtxsid","casrn","name"};
				initTableFresh(connDest, tableName, varlist);			

				Statement statToxVal=connSrc.createStatement();
				String sql=p.createSQLQueryByTable2(tableName,varlist);				

				System.out.println(sql);

				ResultSet rs = statToxVal.executeQuery(sql);

				List<Object>records=new ArrayList<>();
				while (rs.next()) {						 
					RecordChemical r=new RecordChemical();			
					ParseToxValDB.createRecord(rs, r);
					records.add(r);
				}

				SqlUtilities.batchCreate(tableName, null, varlist,records, connDest);
				//			System.out.println(gson.toJson(records));
				System.out.println(records.size());

			} catch (Exception ex) {
				ex.printStackTrace();
			}		

		}

		void exportGenetoxSummary(Connection connSrc, Connection connDest) {

			String tableName="genetox_summary";

			try {
				String []varlist= {"dtxsid","reports_pos","reports_neg","reports_other","ames","micronucleus","genetox_call"};

				initTableFresh(connDest, tableName, varlist);			

				Statement statToxVal=connSrc.createStatement();
				String sql=ParseToxValDB.createSQLQueryByTable(tableName,varlist);				
				ResultSet rs = statToxVal.executeQuery(sql);

				List<Object>records=new ArrayList<>();
				while (rs.next()) {						 
					RecordToxValGenetox r=new RecordToxValGenetox();			
					ParseToxValDB.createRecord(rs, r);
					records.add(r);
				}

				SqlUtilities.batchCreate(tableName, null, varlist,records, connDest);
				//			System.out.println(gson.toJson(records));
				System.out.println(records.size());

			} catch (Exception ex) {
				ex.printStackTrace();
			}		


		}

		void exportToxval(String versionToxVal, Connection connSrc, Connection connDest) {

			String tableName="toxval_complete";

			//		Connection conn=SqlUtilities.getConnectionToxVal();//Should be v96 once i get access to it

			try {

				String []varlist= RecordToxVal.varlist;

				Statement statSrc=connSrc.createStatement();


				Hashtable<String, List<RecordToxVal>> htRS = getRecordSourceHashtable(versionToxVal, statSrc);
				System.out.println("Record Source hashtable size="+htRS.size());

				//			if(true)return;


				String sql=null;
				if(versionToxVal.equals(ParseToxValDB.v96)) {
					sql=ParseToxValDB.createSQLQuery_toxval_v96();
				} else if (versionToxVal.equals(ParseToxValDB.v94)) {
					sql=ParseToxValDB.createSQLQuery_toxval_v94_no_record_source();
				}

				System.out.println(sql);

				//			if(true)return;

				ResultSet rs = statSrc.executeQuery(sql);

				List<Object>records=new ArrayList<>();
				while (rs.next()) {						 
					RecordToxVal r=new RecordToxVal();			
					ParseToxValDB.createRecord(rs, r);

					addReferenceInfo(htRS.get(r.toxval_id), r);

					if(!r.url.isBlank() && r.url.contains(" ")) {				
						System.out.println("\nAfter add:\n"+r.long_ref+"\n"+r.url+"\n");
					}

					//				System.out.println(r.toxval_id+"\t"+htRS.get(r.toxval_id).size());

					records.add(r);
				}

				//			System.out.println(gson.toJson(records));
				System.out.println(records.size());

				Statement statDest = MySQL_DB.getStatement(connDest);

				initTableFresh(connDest, tableName, varlist);			

				String sqlAddIndex="CREATE INDEX if not exists "+tableName+"_toxval_id ON "+tableName+" (toxval_id)";
				statDest.executeUpdate(sqlAddIndex);
				String sqlDelete ="Delete from "+tableName+" where toxval_id>0;";
				statDest.executeUpdate(sqlDelete);

				SqlUtilities.batchCreate(tableName, null, varlist,records, connDest);
				//			//			System.out.println(gson.toJson(records));
				//			System.out.println(records.size());

			} catch (Exception ex) {
				ex.printStackTrace();
			}		


		}


		private Hashtable<String, List<RecordToxVal>> getRecordSourceHashtable(String versionToxVal, Statement statToxVal)
				throws SQLException {
		
		
			String sqlRS=null;
		
			if(versionToxVal.equals(ParseToxValDB.v96)) {
				sqlRS=ParseToxValDB.createRecordSourceSqlV96();	
			} else if(versionToxVal.equals(ParseToxValDB.v94)) {
				sqlRS=ParseToxValDB.createRecordSourceSqlV94();
			}
		
			ResultSet rs = statToxVal.executeQuery(sqlRS);
		
			Hashtable<String,List<RecordToxVal>>htRS=new Hashtable<>();
		
			while (rs.next()) {						 
		
				RecordToxVal r=new RecordToxVal();			
				ParseToxValDB.createRecord(rs, r);
		
				//			if(r.url!=null && !r.url.equals("-"))
				//				System.out.println("Before edit:\t"+r.url);
		
				String urlBefore=r.url;
		
				editReferenceInfo(r);
		
				//			if(r.url!=null)
				//				System.out.println("After edit:\t"+r.url);
		
				if(htRS.containsKey(r.toxval_id)) {
					List<RecordToxVal>recs=htRS.get(r.toxval_id);
					recs.add(r);
				} else {
					List<RecordToxVal>recs=new ArrayList<>();
					recs.add(r);
					htRS.put(r.toxval_id, recs);
				}
		
				//				records.add(r);
			}
			return htRS;
		}


		private void editReferenceInfo(RecordToxVal ri) {
		
			if (getNA_refs().contains(ri.long_ref)) {//dont store NA references with usable info
				ri.long_ref=null;
			}
		
			if(ri.url!=null && ri.url.equals("-")) {
				ri.url=null;
			}
		
			if (ri.long_ref!=null && ri.long_ref.contains("//doi.org/")) {	//add DOI to url			
				String DOI=null;
		
				//			if (ri.long_ref.contains("doi:")) {
				//				DOI=ri.long_ref.substring(ri.long_ref.indexOf("doi: ")+5,ri.long_ref.length());
				//				
				//				if(!DOI.contains(".")) DOI=null;
				//				else DOI="https://doi.org/"+DOI;
		
				if (ri.long_ref.contains("https://doi.org/")) {
					DOI=ri.long_ref.substring(ri.long_ref.indexOf("https://doi.org/"),ri.long_ref.length());
				} else if (ri.long_ref.contains("http://doi.org/")) {
					DOI=ri.long_ref.substring(ri.long_ref.indexOf("http://doi.org/"),ri.long_ref.length());
				}
		
				if(DOI!=null && !DOI.isBlank()) {
					DOI=DOI.trim();
					if (DOI.contains(" ")) {
						DOI=DOI.substring(0,DOI.indexOf(" ")).trim();	
					}
					DOI=DOI.trim();
		
					//				if(DOI.length()>10) {
					//					System.out.println(DOI.length()+"\t"+DOI);
					//				}
		
					if (ri.url==null || ri.url.isEmpty())  ri.url=DOI;
					else ri.url+="<br>"+DOI;		
		
				}
		
			}									
		
			if(ri.long_ref!=null) {
				if (ri.long_ref.indexOf("<br>")==0) {
					ri.long_ref=ri.long_ref.substring(5,ri.long_ref.length());
				}
				ri.long_ref=ri.long_ref.trim();
			}
		
			if(ri.url==null) return;
		
			if (ri.url.indexOf("<br>")==0) {
				ri.url=ri.url.substring(5,ri.url.length());
			}
			ri.url=ri.url.trim();
		
		
		}


		/**
		 * Has predictions from text file created from:
		 * 1. OPERA2.8 - see hibernate qsar model building / PredictionDashboardScriptOPERA / createToxValModelCSVFromOPERA2_8_SqliteDB()
		 * 2. Episuite  - see hibernate qsar model building / PredictionResultsEPISUITEScript / writeToxvalModelRecordsFromJsonResultsFromFolder
		 * 
		 * TODO load from res_qsar instead
		 * 
		 * @param connV96_new
		 */
		@Deprecated
		public void addModelRecordsToToxval(Connection connV96_new) {
		
			//		String filepathRecords="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 scientists\\Tony\\update toxval models\\update BCF biowin3 records in toxval models table.txt";
		
		
			String filepathRecords="data\\episuite\\episuite BCF BIOWIN3 for toxval models.tsv";
		
			String tableName="models";
		
			String[] varlist = { "dtxsid", "model", 
					"metric", "value", "units", "qualifier" };		
		
			try {
				System.out.println("Adding records to models table");
		
				initTableFresh(connV96_new, tableName, varlist);	
		
				Statement stat=connV96_new.createStatement();
				String sqlAddIndex="CREATE INDEX if not exists "+tableName+"_model ON "+tableName+" (model)";
				stat.executeUpdate(sqlAddIndex);
				sqlAddIndex="CREATE INDEX if not exists "+tableName+"_metric ON "+tableName+" (metric)";
				stat.executeUpdate(sqlAddIndex);
		
		
				BufferedReader br = new BufferedReader(new FileReader(filepathRecords));
				String header = br.readLine();
		
				List<String>headers=Arrays.asList(header.split("\t"));
		
				List<Object>records=new ArrayList<>();
		
				while (true) {
					String Line = br.readLine();
		
					if (Line == null || Line.isEmpty())	break;
					String []values=Line.split("\t");
		
					RecordToxValModels rec=new RecordToxValModels();
					String strValue=values[headers.indexOf("value")];
					if(strValue.equals("null")) continue;
		
					rec.dtxsid=values[headers.indexOf("dtxsid")];
					rec.model=values[headers.indexOf("model")];
					rec.metric=values[headers.indexOf("metric")];
					rec.value=strValue;
					rec.units=values[headers.indexOf("units")];//units
					rec.qualifier=values[headers.indexOf("qualifier")];//qualifier
					records.add(rec);
				}
				br.close();
		
				SqlUtilities.batchCreate(tableName, null, varlist, records, connV96_new);
		
		
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		
		}


		/**
		 * Sets long_ref and url from multiple record_source records
		 * 
		 * @param recsRef
		 * @param ri
		 */
		private void addReferenceInfo(List<RecordToxVal>recsRef, RecordToxVal ri) {
		
			ri.long_ref="";
			ri.url="";
		
			for (int j=0;j<recsRef.size();j++) {								
				RecordToxVal recRef=recsRef.get(j);
		
				if (recRef.long_ref!=null && !getNA_refs().contains(recRef.long_ref)) {
					if (ri.long_ref.isEmpty()) ri.long_ref=recRef.long_ref; 
					else ri.long_ref+="<br>"+recRef.long_ref;//doesnt seem to happen
				}
		
				if(recRef.url!=null && !recRef.url.equals("-")) {
					if (ri.url.isEmpty())  ri.url=recRef.url;
					else ri.url+="<br>"+recRef.url;
				}
			}
		
			ri.long_ref=ri.long_ref.trim();
			ri.url=ri.url.trim();
		
			if (ri.long_ref.indexOf("<br>")==0) {
				ri.long_ref=ri.long_ref.substring(5,ri.long_ref.length());
			}
		
			if (ri.url.indexOf("<br>")==0) {
				ri.url=ri.url.substring(5,ri.url.length());
			}
		}


		List<String>getNA_refs() {
			return Arrays.asList("-","- - - -","- - - NA","- Unnamed - NA","- Unnamed - -");
		}


		private void checkIfDuplicateToxval_ids(List<Object> records) {
			Hashtable<String,List<ScoreRecord>>htRecs=new Hashtable<>();
		
			for (Object object:records) {
				ScoreRecord r=(ScoreRecord)object;
		
				if(htRecs.containsKey(r.toxvalID)) {
					List<ScoreRecord>recs=htRecs.get(r.toxvalID);
					recs.add(r);
				} else {
					List<ScoreRecord>recs=new ArrayList<>();
					recs.add(r);
					htRecs.put(r.toxvalID, recs);
				}
			}
			for (String toxval_id:htRecs.keySet()) {
				System.out.println(toxval_id+"\t"+htRecs.get(toxval_id).size());
			}
		}




	}


	public static boolean isValidCAS(String casInput) {
		long t1=System.currentTimeMillis();
		if(casInput.toUpperCase().contains("CHEMBL")) return false;
		if(casInput.toUpperCase().contains("SRC")) return false;

		String regex = "[0-9\\-]+"; //only has numbers and dashes
		Pattern p = Pattern.compile(regex); 
		Matcher m = p.matcher(casInput);

		if(!m.matches()) return false;

		if(casInput.substring(0,1).equals("-")) return false;

		String[] casArray = casInput.split("\\||;|,");
		boolean valid = true;
		for (String cas:casArray) {
			String casTemp = cas.replaceAll("[^0-9]","");//do we really want to discard non numbers???
			int len = casTemp.length();
			if (len > 10 || len <= 0) { return false; }
			int check = Character.getNumericValue(casTemp.charAt(len-1));
			int sum = 0;
			for (int i = 1; i <= len-1; i++) {
				sum += i*Character.getNumericValue(casTemp.charAt(len-1-i));
			}
			if (sum % 10 != check) {
				valid = false;
				break;
			}
			// There are no valid CAS RNs with bad formatting in the current data set, but if that happens in other sources, could add format correction here
			//			else if (!cas.contains("-")) {
			//				System.out.println("Valid CAS with bad format: "+cas);
			//			}
		}
		long t2=System.currentTimeMillis();
		//		System.out.println((t2-t1)+" millisecs to check cas");

		return valid;
	}

	class Identifier {
		String dtxsid;
		String casrn;
		String name;
	}
	
	
	private void initTableFresh(Connection conn, String tableName, String[] varlist) throws SQLException {
		Statement stat = MySQL_DB.getStatement(conn);
		stat.executeUpdate("drop table if exists "+tableName+";");
		initTable(stat, tableName, varlist);
	}


	private void initTable(Statement stat, String tableName, String[] varlist) throws SQLException {
		MySQL_DB.create_table(stat, tableName,varlist);
		String sqlAddIndex="CREATE INDEX if not exists "+tableName+"_dtxsid ON "+tableName+" (dtxsid)";
		stat.executeUpdate(sqlAddIndex);
	}


	HazardRecordCreator hazardRecordCreator=new HazardRecordCreator();

	class HazardRecordCreator {


		/**
		 * 
		 * @param conn
		 */
		void createScoreRecordsCancer(Connection conn) {

			String tableNameSrc="cancer_summary";


			try {
				Statement stat=conn.createStatement();

				String sql="SELECT * from "+tableNameSrc+" t\r\n"
						+"where t.dtxsid!='NODTXSID' and t.dtxsid!='NA';";
				ResultSet rs = stat.executeQuery(sql);

				Hashtable<String,String>dictCC=ParseToxValCancer.populateCancerCallToScoreValue();
				Hashtable<String, Chemical>htChemicals=new Hashtable<>();

				while (rs.next()) {						 
					RecordToxValCancer r=new RecordToxValCancer();			
					ParseToxValDB.createRecord(rs, r);
					//				System.out.println(gson.toJson(r));
					Chemical chemical = getChemical(htChemicals, r.dtxsid);
					ParseToxValCancer.createScoreRecord(chemical, r,dictCC);
				}

				Hashtable<String, Identifier> htID = getIdentifierHashtable(stat);
				List<Object> records = compileScoreRecords(htChemicals,htID);

				for(Object record:records) {
					ScoreRecord sr=(ScoreRecord)record;
					sr.sourceTable=tableNameSrc;
				}

				System.out.println("ScoreRecords.size()="+records.size());

				//			System.out.println(gson.toJson(records));

				saveHazardRecords(stat, records, tableNameSrc,true);

				//
			} catch (Exception ex) {
				ex.printStackTrace();
			}		

		}

		void createHazardRecordsFromJson(Connection conn,String filepathJson) {

			String tableNameSrc="models";

			try {
				Statement stat=conn.createStatement();

				List<Object>records=null;

				System.out.println("ScoreRecords.size()="+records.size());
				//			System.out.println(gson.toJson(records));

				saveHazardRecords(stat, records, tableNameSrc,true);

				//
			} catch (Exception ex) {
				ex.printStackTrace();
			}		

		}

		void createScoreRecordsGenetox(Connection conn) {

			String tableNameSrc="genetox_summary";

			try {
				Statement stat=conn.createStatement();

				String sql="SELECT * from "+tableNameSrc+" t\r\n"
						+"where t.dtxsid!='NODTXSID' and t.dtxsid!='NA';";
				ResultSet rs = stat.executeQuery(sql);

				Hashtable<String,String>dictCC=ParseToxValGenetox.populateGenetoxCallToScoreValue();
				Hashtable<String, Chemical>htChemicals=new Hashtable<>();

				while (rs.next()) {						 
					RecordToxValGenetox r=new RecordToxValGenetox();			
					ParseToxValDB.createRecord(rs, r);
					//				System.out.println(gson.toJson(r));
					Chemical chemical = getChemical(htChemicals, r.dtxsid);
					ParseToxValGenetox.createScoreRecord(chemical, r,dictCC);
				}

				Hashtable<String, Identifier> htID = getIdentifierHashtable(stat);
				List<Object> records = compileScoreRecords(htChemicals,htID);

				for(Object record:records) {
					ScoreRecord sr=(ScoreRecord)record;
					sr.sourceTable=tableNameSrc;
				}

				System.out.println("ScoreRecords.size()="+records.size());

				//			System.out.println(gson.toJson(records));
				saveHazardRecords(stat, records, tableNameSrc,true);

				//
			} catch (Exception ex) {
				ex.printStackTrace();
			}		

		}

		/**
		 * Create hazardRecords from models table (loaded from a text file in that format)
		 * 
		 * Models:
		 * - OPERA BCF
		 * - EpiSuite BCF
		 * - EpiSuite BIOWIN3
		 * 
		 *  
		 * 
		 * @param conn
		 */
		@Deprecated
		void createScoreRecordsModels(Connection conn) {

			String tableNameSrc="models";

			try {
				Statement stat=conn.createStatement();

				Hashtable<String, Chemical>htChemicals=new Hashtable<>();

				Hashtable<String, RecordToxValModels>htEpiSuiteBCF=getModelsHashtable(tableNameSrc, stat, "EpiSuite", "BCF");
				Hashtable<String, RecordToxValModels>htEpiSuiteBiodegradationScore=getModelsHashtable(tableNameSrc, stat, "EpiSuite", "Biodegradation Score");

				Hashtable<String, RecordToxValModels>htOPERA_BCF=getModelsHashtable(tableNameSrc, stat, "OPERA", "BCF");
				Hashtable<String, RecordToxValModels>htOPERA_BCF_AD=getModelsHashtable(tableNameSrc, stat, "OPERA", "BCF_AD");


				for (String dtxsid:htEpiSuiteBCF.keySet()) {
					Chemical chemical = getChemical(htChemicals, dtxsid);
					ParseToxValModels.createScoreRecordBCF_EPISUITE(chemical, htEpiSuiteBCF.get(dtxsid));
				}

				//			System.out.println(gson.toJson(htChemicals));

				for (String dtxsid:htEpiSuiteBiodegradationScore.keySet()) {
					Chemical chemical = getChemical(htChemicals, dtxsid);
					ParseToxValModels.createScoreRecordPersistence_EpiSuite(chemical, 
							htEpiSuiteBiodegradationScore.get(dtxsid));//TODO does this match valery's latest code?
				}

				for (String dtxsid:htOPERA_BCF.keySet()) {
					Chemical chemical = getChemical(htChemicals, dtxsid);
					ParseToxValModels.createScoreRecordBCF_Opera(chemical, 
							htOPERA_BCF.get(dtxsid),htOPERA_BCF_AD.get(dtxsid));
				}

				//Delete blank scores if want to print htChemicals:
				//			for (String dtxsid:htChemicals.keySet()) {
				//				Chemical chemical=htChemicals.get(dtxsid);
				//				for (int i=0;i<chemical.scores.size();i++) {
				//					Score score=chemical.scores.get(i);
				//					if(score.records.size()==0) chemical.scores.remove(i--);
				//				}
				//			}

				Hashtable<String, Identifier> htID = getIdentifiersByDTXSIDResQsar();
				System.out.println("ID hashtable size="+htID.size());

				List<Object> records = compileScoreRecords(htChemicals,htID);

				for(Object record:records) {
					ScoreRecord sr=(ScoreRecord)record;
					sr.sourceTable=tableNameSrc;
					sr.source=sr.sourceOriginal;//remove toxval as source
					sr.sourceOriginal=null;
					if(sr.source.equals("OPERA"))sr.source="OPERA2.8";
					if(sr.source.equals("EpiSuite"))sr.source="EpiWebSuite1.0";

				}

				System.out.println("ScoreRecords.size()="+records.size());
				//			System.out.println(gson.toJson(records));

				saveHazardRecords(stat, records, tableNameSrc,true);

				//
			} catch (Exception ex) {
				ex.printStackTrace();
			}		

		}

		void createScoreRecordsToxVal(Connection connDest,String toxvalVersion) {

			try {


				Statement statDest=connDest.createStatement();


				ParseToxValDB p=new ParseToxValDB();

				Hashtable<String, Chemical>htChemicals=new Hashtable<>();
				//			String sql=createSQLQuery_toxval(chemical.CAS);
				String sql=null;

				if(toxvalVersion.equals(ParseToxValDB.v94)) {
					sql=p.createSQLQuery_toxval_complete();
				} else if(toxvalVersion.equals(ParseToxValDB.v96)) {
					sql=p.createSQLQuery_toxval_complete();
				} else if (toxvalVersion.equals(ParseToxValDB.v8)) {
					sql=p.createSQLQuery_toxval_v8();
				}

				System.out.println(sql);


				ResultSet rs=MySQL_DB.getRecords(statDest, sql);

				//			Vector<RecordToxVal>records=new Vector();

				while (rs.next()) {						 
					RecordToxVal ri=new RecordToxVal();							
					ParseToxValDB.createRecord(rs,ri);
					//				records.add(ri);

					if(ri.dtxsid==null) {
						continue;
					}

					Chemical chemical = getChemical(htChemicals, ri.dtxsid);
					ParseToxVal.createScoreRecord(chemical, ri,toxvalVersion);
				}

				//			Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
				//***************************************************************************************************************************************

				Hashtable<String, Identifier> htID = getIdentifierHashtable(statDest);
				List<Object> records = compileScoreRecords(htChemicals,htID);

				System.out.println("ScoreRecords.size()="+records.size());

				//			checkIfDuplicateToxval_ids(records);

				//			if(true)return;

				//			System.out.println(gson.toJson(records));

				saveHazardRecords(statDest, records, "toxval",true);


			} catch (Exception ex) {			
				ex.printStackTrace();
			}

		}
		
		public void createHazardRecordsModelResults() {
			
//			String dbPathDestModels="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18_EPA_Github\\databases\\HazardRecordsModels.db";
			String dbPathDestModels="databases\\RevisedHazardDB\\HazardRecordsModels.db";
			Connection connModels= MySQL_DB.getConnection(dbPathDestModels);

			//			fixEpisuiteJsons();
			
			//In the future these should be exported from mv_predicted_data from res_qsar
			
			createEPISUITE_BCF_BIOWIN3_From_PredictionDashboardJsons(connModels);
			createOPERA_BCF_ER_AR_From_PredictionDashboardJsons(connModels,"OPERA2.8");
		}
		

		public void createHazardRecordsFromToxvalCopyTables(Connection connDest, String versionToxVal) {
			createScoreRecordsToxVal(connDest,versionToxVal);
			createScoreRecordsCancer(connDest);//no change from v94 to v96
			createScoreRecordsGenetox(connDest);

			//			createScoreRecordsModels(connDest);// old way
//			String folderOPERA28="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\";
//			String folder=folderOPERA28+"CERAPP_COMPARA\\";
//			createScoreRecordsFromOPERAHazardRecordJsons(connDest,folder);


		}
		

		private void createOPERA_BCF_ER_AR_From_PredictionDashboardJsons(Connection conn,String sourceName) {
		
			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\OPERA2.8\\export\\";

			try {

				Statement stat = conn.createStatement();
				
				HashSet<String>dtxsids=new HashSet<>();

				for (File file:new File(folder).listFiles()) {
					
					if(!file.getName().contains(".json")) continue;
					
					//				if(!file.getName().equals("BCF_BIOWIN3_episuite results 9000.json.json")) continue;
					List<Object>scoreRecords=goThroughOPERAPredictionDashboardFile(file,dtxsids,sourceName);
//					System.out.println(gson.toJson(scoreRecords));
					saveHazardRecords(stat, scoreRecords, null,false);
					System.out.println(file.getName()+"\t"+scoreRecords.size()+"\t"+dtxsids.size());
					
//					if(true)break;
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}

		
		private void createScoreRecordsFromOPERAHazardRecordJsons(Connection conn,String folder) {

			
			try {

				Statement stat = conn.createStatement();
				for (File file:new File(folder).listFiles()) {
					if(!file.getName().contains(".json")) continue;
					
					//				if(!file.getName().equals("BCF_BIOWIN3_episuite results 9000.json.json")) continue;
					
//					List<Object>scoreRecords=goThroughEpisuitePredictionDashboardFile(file);
					
					Type listType = new TypeToken<ArrayList<Object>>(){}.getType();
					List<Object>scoreRecords=gson.fromJson(new FileReader(file), listType);
					System.out.println(gson.toJson(scoreRecords));
//					saveHazardRecords(stat, scoreRecords, "models",false);
//					System.out.println(file.getName()+"\t"+scoreRecords.size());
					
					if(true)break;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			
		
			
		}


		/**
		 * Simplified serialized version of PredictionDashboard from Hibernate project
		 */
		class PredictionDashboard {
			String dtxsid;
			String dtxcid;
			String casrn;
			String preferredName;
			String smiles;
			String canonQsarSmiles;
			Double experimentalValue;
			String experimentalString;
			String modelName;
			Double predictionValue;
			String predictionString;
			
			List<ApplicabilityDomain>applicabilityDomains;
			
			class ApplicabilityDomain{
				String name;
				Double value;
				String conclusion;
				String reasoning;
			}
			
			String getCombinedADReasoning() {				
				
				if(applicabilityDomains==null) return null;
				
				for (ApplicabilityDomain ad:applicabilityDomains) {
					if(ad.name.equals("Combined Applicability Domain")) return ad.reasoning;
				}
				return null;
			}
			
			//Add adValue?
		}
		
		
		/**
		 * Goes through json files created by PredictionResultsEPISUITEScript // Loader // compileApiJsonResultsFromFolderToPredictionDashboardJson
		 * @param conn
		 */
		void createEPISUITE_BCF_BIOWIN3_From_PredictionDashboardJsons(Connection conn) {

			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\episuite\\BCF_BIOWIN3\\";

			try {

				Statement stat = conn.createStatement();

				for (File file:new File(folder).listFiles()) {
					
					if(!file.getName().contains(".json")) continue;
					
					//				if(!file.getName().equals("BCF_BIOWIN3_episuite results 9000.json.json")) continue;
					List<Object>scoreRecords=goThroughEpisuitePredictionDashboardFile(file);
					
//					System.out.println(gson.toJson(scoreRecords));
					saveHazardRecords(stat, scoreRecords, "models",false);
					System.out.println(file.getName()+"\t"+scoreRecords.size());
					
//					if(true)break;
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		
		void fixEpisuiteJsons() {
			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\episuite\\BCF_BIOWIN3\\";
			for (File file:new File(folder).listFiles()) {
				if(!file.getName().contains(".json"))continue;
				fixEpisuitePredictionDashboardFile(file);
//				if(true)break;
			}
		}

		private List<Object> goThroughEpisuitePredictionDashboardFile(File file) {
			try {
				BufferedReader br=new BufferedReader(new FileReader(file));

				
				List<Object>scoreRecords=new ArrayList<>();

				while (true) {
					
					
					String line=br.readLine();
					if(line==null)break;
					PredictionDashboard pd=gson.fromJson(line, PredictionDashboard.class);

					String sourceName="EpiWeb1.0";
					String url="https://episuite.app/EpiWebSuite";

					if(pd.modelName.equals("EPISUITE_BCF")) {
						String units="L/kg wet-wt";
						createBCF_ScoreRecords(scoreRecords, pd, sourceName, url,units);
					} else if (pd.modelName.equals("EPISUITE_BIOWIN3")) {

						String sourceOriginal=pd.modelName+"_Predicted_Value";
						String units="unitless";
						if(pd.predictionValue!=null) {
							ScoreRecord sr=ParseToxValModels.createScoreRecordPersistence(pd.dtxsid,pd.dtxcid, pd.casrn,pd.preferredName,
									sourceName, sourceOriginal,pd.predictionValue,units,ScoreRecord.typePredicted,url);
							scoreRecords.add(sr);
						} else {
//							System.out.println(gson.toJson(pd));
						}
					}
					
					
//					System.out.println(pd.dtxsid+"\t"+pd.experimentalValue+"\t"+pd.predictionValue+"\t"+pd.modelName);
				}
				
				br.close();
				
				return scoreRecords;
				
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
		
		private List<Object> goThroughOPERAPredictionDashboardFile(File file, HashSet<String> dtxsids,String sourceName) {
			try {
				BufferedReader br=new BufferedReader(new FileReader(file));

				List<Object>scoreRecords=new ArrayList<>();

				while (true) {
					
					String line=br.readLine();
					if(line==null)break;
					PredictionDashboard pd=gson.fromJson(line, PredictionDashboard.class);
					
					dtxsids.add(pd.dtxsid);

					if(pd.modelName.equals("OPERA_BCF")) {
						String url="https://github.com/kmansouri/OPERA";
						String units="L/kg wet-wt";
						createBCF_ScoreRecords(scoreRecords, pd, sourceName, url,units);
					} else {
						createEndocrineDisruptionScoreRecords(scoreRecords, pd, sourceName);
					}
//					System.out.println(pd.dtxsid+"\t"+pd.experimentalValue+"\t"+pd.predictionValue+"\t"+pd.modelName);
				}
				
				br.close();
				
				return scoreRecords;
				
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}

		private void createEndocrineDisruptionScoreRecords(List<Object> scoreRecords, PredictionDashboard pd,String sourceName) {
			
//			System.out.println(pd.modelName+"\t"+pd.predictionValue);
			
			if(pd.experimentalString!=null) {
				ScoreRecord sr=ParseOPERA.createExperimentalScoreRecordEndocrineDisruption(pd.experimentalString,
						pd.dtxsid,pd.dtxcid,pd.casrn,pd.preferredName, pd.modelName,sourceName);
				if(sr!=null)  {
					scoreRecords.add(sr);
				}
//				System.out.println(gson.toJson(sr));
			}
			
			if(pd.predictionValue!=null) {
				ScoreRecord sr=ParseOPERA.createPredictedScoreRecordEndocrineDisruption(pd.predictionValue,
						pd.dtxsid,pd.dtxcid,pd.casrn,pd.preferredName, pd.getCombinedADReasoning(),pd.modelName,sourceName);
				
				scoreRecords.add(sr);
//							System.out.println(gson.toJson(sr));
			}
		}

		private void createBCF_ScoreRecords(List<Object> scoreRecords, PredictionDashboard pd, String sourceName,String url,String units) {
			

			if(pd.experimentalValue!=null) {
				String sourceOriginal=pd.modelName+"_Experimental_Value";
				ScoreRecord sr=ParseToxValModels.createScoreRecordBCF(pd.dtxsid,pd.dtxcid, pd.casrn,pd.preferredName,
						sourceName, sourceOriginal,pd.experimentalValue,units,null,ScoreRecord.typeScreening,url);
				scoreRecords.add(sr);
//				System.out.println(gson.toJson(sr));
			}

			if(pd.predictionValue!=null) {
				
				String sourceOriginal=pd.modelName+"_Predicted_Value";
				ScoreRecord sr=ParseToxValModels.createScoreRecordBCF(pd.dtxsid,pd.dtxcid, pd.casrn,pd.preferredName,
						sourceName, sourceOriginal,pd.predictionValue,units,pd.getCombinedADReasoning(),ScoreRecord.typePredicted,url);
				scoreRecords.add(sr);

				String reasoning=pd.getCombinedADReasoning();
				sr.note=reasoning;
//							System.out.println(gson.toJson(sr));
			}
		}

		
		private void fixEpisuitePredictionDashboardFile(File file) {
			try {
				BufferedReader br=new BufferedReader(new FileReader(file));
				FileWriter fw=new FileWriter(file.getParentFile().getAbsolutePath()+File.separator+"fixed"+File.separator+file.getName());
				Gson gson=new Gson();
				
				System.out.println(file.getName());
				
				while (true) {
					String line=br.readLine();
					if(line==null)break;
					
					PredictionDashboard pd=gson.fromJson(line, PredictionDashboard.class);
					
					if(pd.modelName.equals("EPISUITE_BCF") && pd.experimentalValue!=null) {
						pd.experimentalValue=Math.pow(10,pd.experimentalValue);
//						System.out.println(pd.dtxsid+"\t"+pd.modelName+"\t"+pd.experimentalValue);	
					}
					fw.write(gson.toJson(pd)+"\r\n");
				}
				br.close();
				fw.flush();
				fw.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		/**
		 * Compiles the score records and also gets the fixed name and cas from the chemical
		 * 
		 * @param htChemicals
		 * @param htID 
		 * @return
		 */
		private List<Object> compileScoreRecords(Hashtable<String, Chemical> htChemicals, Hashtable<String, Identifier> htID) {
			List<Object>records=new ArrayList<>(); 
		
			for (String dtxsid:htChemicals.keySet()) {
				Chemical chemical=htChemicals.get(dtxsid);
		
				for (Score score:chemical.scores) {
		
					for(ScoreRecord scoreRecord:score.records) {
		
						if(!htID.containsKey(dtxsid))continue;
		
						Identifier i=htID.get(dtxsid);
						scoreRecord.CAS=i.casrn;
						scoreRecord.name=i.name;
						scoreRecord.dtxsid=dtxsid;
						scoreRecord.sourceTable="toxval";
						//						if(records.size()<10) {
						//							System.out.println(gson.toJson(scoreRecord));
						//						}
						records.add(scoreRecord);
					}
				}
			}
			return records;
		}

		private Hashtable<String, Identifier> getIdentifierHashtable(Statement statSrc) throws SQLException {
		
			Hashtable<String,Identifier>htID=new Hashtable<>();
		
			String sqlID="select dtxsid,casrn,name from chemical where dtxsid!='NODTXSID' and dtxsid!='NA';";
		
			ResultSet rsID = statSrc.executeQuery(sqlID);
		
			while (rsID.next()) {
				Identifier i=new Identifier();
				i.dtxsid=rsID.getString(1);
				i.casrn=rsID.getString(2);
				i.name=rsID.getString(3);
		
				if(i.dtxsid.equals("DTXSID2035649")) i.casrn="4299-07-4";
		
				if(i.casrn!=null && !isValidCAS(i.casrn)) {
					if(!i.casrn.toLowerCase().contains("nocas") && !i.casrn.toLowerCase().equals("-"))
						System.out.println(i.casrn+"\t"+i.dtxsid);
					i.casrn=null;
				}
		
				if(i.name!=null && i.name.toLowerCase().contains("nocas"))  i.name=null;
				if(i.name!=null && i.name.toLowerCase().equals("noname"))  i.name=null;
		
				htID.put(i.dtxsid, i);
			}
			return htID;
		}

		/**
		 * 
		 * @param statDest statement for database
		 * @param records hazardRecords to create
		 * @param sourceTable needed so can delete any existing hazard records from this table to avoid duplication
		 * @throws SQLException
		 */
		private void saveHazardRecords(Statement statDest, List<? extends Object> records, String sourceTable,boolean deleteExistingFromSourceTable)
				throws SQLException {
		
			List<String>fieldNamesHazard= Arrays.asList("dtxsid","CAS","name","hazardName","source","sourceTable","sourceOriginal", 
					"score", "listType","route", "category", "hazardCode",
					"hazardStatement", "rationale", "note","note2","toxvalID",
					"testOrganism","testOrganismType","testType","valueMassOperator","valueMass","valueMassUnits","effect",
					"duration","durationUnits","url","longRef");
		
			String[] varlist = fieldNamesHazard.toArray(new String[0]);
		
			String tableNameDest="HazardRecords";
		
			//initTable(statDest.getConnection(), tableNameDest, varlist);
			initTable(statDest, tableNameDest, varlist);
		
			String sqlAddIndex="CREATE INDEX if not exists "+tableNameDest+"_sourceTable ON "+tableNameDest+" (sourceTable)";
			statDest.executeUpdate(sqlAddIndex);
		
			sqlAddIndex="CREATE INDEX if not exists "+tableNameDest+"_toxvalID ON "+tableNameDest+" (toxvalID)";
			statDest.executeUpdate(sqlAddIndex);
		
			sqlAddIndex="CREATE INDEX if not exists "+tableNameDest+"_source ON "+tableNameDest+" (source)";
			statDest.executeUpdate(sqlAddIndex);
		
			sqlAddIndex="CREATE INDEX if not exists "+tableNameDest+"_hazardName ON "+tableNameDest+" (hazardName)";
			statDest.executeUpdate(sqlAddIndex);
		
			sqlAddIndex="CREATE INDEX if not exists "+tableNameDest+"_CAS ON "+tableNameDest+" (CAS)";
			statDest.executeUpdate(sqlAddIndex);
			
			sqlAddIndex="CREATE INDEX if not exists "+tableNameDest+"_listType ON "+tableNameDest+" (listType)";
			statDest.executeUpdate(sqlAddIndex);

		
			if(deleteExistingFromSourceTable) {
				String sqlDelete ="Delete from "+tableNameDest+" where sourceTable='"+sourceTable+"'";
				statDest.executeUpdate(sqlDelete);
			}
		
			SqlUtilities.batchCreate(tableNameDest, null, varlist,records, statDest.getConnection());
		}

		@Deprecated
		private Hashtable<String, RecordToxValModels> getModelsHashtable(String tableNameSrc, Statement stat, String model, String metric)
				throws SQLException {
			Hashtable<String, RecordToxValModels>htModels=new Hashtable<>();
		
			String sql = "SELECT * from " + tableNameSrc + " t\r\n"
					+ "where model='" + model + "' and metric='" + metric+ "'\r\n" 
					+ "order by dtxsid;";
		
			ResultSet rs = stat.executeQuery(sql);
		
			int count=0;
		
			while (rs.next()) {
				count++;
				RecordToxValModels r=new RecordToxValModels();			
				ParseToxValDB.createRecord(rs, r);
		
				htModels.put(r.dtxsid, r);
				//				System.out.println(gson.toJson(r));
		
				//			if(count==10)break;
		
			}
			System.out.println("Done getting hashtable for "+model+" "+metric);
		
			return htModels;
		}

		/**
		 * Gets the chemical and fixes name and cas
		 * @param htChemicals
		 * @param ri
		 * @return
		 */
		private Chemical getChemical(Hashtable<String, Chemical> htChemicals, String dtxsid) {
			Chemical chemical=null;
		
			if(htChemicals.containsKey(dtxsid)) {
				chemical=htChemicals.get(dtxsid);
			} else {
				chemical=new Chemical();
				htChemicals.put(dtxsid, chemical);
			}
			return chemical;
		}

		@Deprecated
		private Hashtable<String, Identifier> getIdentifiersByDTXSIDResQsar() throws SQLException {
		
			Hashtable<String,Identifier>htID=new Hashtable<>();
		
			String sqlID="select dtxsid,casrn,preferred_name from qsar_models.dsstox_records "
					+ "where fk_dsstox_snapshot_id=2";
		
			ResultSet rsID = SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sqlID);
		
			while (rsID.next()) {
				Identifier i=new Identifier();
				i.dtxsid=rsID.getString(1);
				i.casrn=rsID.getString(2);
				i.name=rsID.getString(3);
		
				if(i.casrn!=null && !isValidCAS(i.casrn)) {
					if(!i.casrn.toLowerCase().contains("nocas") && !i.casrn.toLowerCase().equals("-"))
						System.out.println(i.casrn+"\t"+i.dtxsid);
		
					i.casrn=null;
				}
		
				if(i.name!=null && i.name.toLowerCase().contains("nocas"))  i.name=null;
				if(i.name!=null && i.name.toLowerCase().equals("noname"))  i.name=null;
		
				htID.put(i.dtxsid, i);
			}
			return htID;
		}

		@Deprecated
		public void exportHazardRecords() {
			
			try {

				String dbPathOldHazard="databases\\AA dashboard.db";
				Connection connSrc= MySQL_DB.getConnection(dbPathOldHazard);
				List<Object> scoreRecords = getHazardRecords(connSrc);
				
				String dbPathDest="databases\\RevisedHazardDB\\AA dashboard.db";
				Connection connDest= MySQL_DB.getConnection(dbPathDest);
				Statement statDest=connDest.createStatement();

				saveHazardRecords(statDest, scoreRecords, null,false);
				//			System.out.println(gson.toJson(records));
//				System.out.println(scoreRecords.size());

			} catch (Exception ex) {
				ex.printStackTrace();
			}		
			
			
		}

		private List<Object> getHazardRecords(Connection connSrc) throws SQLException {
			
			String [] varlist=ScoreRecord.allFieldNames;			

			Statement stat=connSrc.createStatement();
			String sql=ParseToxValDB.createSQLQueryByTable2("HazardRecords",varlist);				

			System.out.println(sql);

			ResultSet rs = stat.executeQuery(sql);

			List<Object>scoreRecords=new ArrayList<>();
			
			int counter=0;

			HashSet<String>casrns=new HashSet<>();
			
			while (rs.next()) {						 
				counter++;
				ScoreRecord sr=new ScoreRecord();			
//					ParseToxValDB.createRecord(rs, sr);
				SQLite_GetRecords.createRecord(rs, sr);
				scoreRecords.add(sr);
				
				if(counter%10000==0) System.out.println(counter);
				
				if(sr.CAS!=null && isValidCAS(sr.CAS)) casrns.add(sr.CAS);
//					System.out.println(gson.toJson(sr));
//					if(counter==10) break;
				
			}
			
//			System.out.println(casrns.size());
//			for (String cas:casrns) {
//				System.out.println(cas);
//			}

			return scoreRecords;
		}
		

		private void copyHazardRecords(Connection connSrc,Statement statDest) throws SQLException {
			
			String [] varlist=ScoreRecord.allFieldNames;			

			Statement statSrc=connSrc.createStatement();
			String sql=ParseToxValDB.createSQLQueryByTable2("HazardRecords",varlist);				
//			System.out.println(sql);

			ResultSet rs = statSrc.executeQuery(sql);

			List<Object>scoreRecords=new ArrayList<>();
			
			int counter=0;
			while (rs.next()) {						 
				counter++;
				ScoreRecord sr=new ScoreRecord();			
				SQLite_GetRecords.createRecord(rs, sr);
				scoreRecords.add(sr);
				
				if(scoreRecords.size()==1000) {
					saveHazardRecords(statDest, scoreRecords, null,false);
					scoreRecords.clear();//clear it so dont run out of memory
				}
				
				if(counter%10000==0) System.out.println("\t"+counter);
				
			}

			//Do what's left
			if (scoreRecords.size()>0)
				saveHazardRecords(statDest, scoreRecords, null,false);
			
		}
		
		
		/**
		 * TODO in future pull records from res_qsar instead so that everything is mapped and converted
		 * 
		 */
		void createAquaticToxDatabase() {

			boolean lookupDTXSIDs=true;//from cas using dsstox

			String dbFileName="Aquatic toxicity.db";		
			String dbPath="databases\\RevisedHazardDB\\"+dbFileName;
			File dbFile=new File(dbPath);
			
//			if(dbFile.exists())dbFile.delete();
			
			Connection conn= MySQL_DB.getConnection(dbPath);
			Statement stat = MySQL_DB.getStatement(conn);

//			String sqlDelete="Delete from HazardRecords where ID>0;";
//			SqlUtilities.runSQLUpdate(conn, sqlDelete);
			
			List<Source>sources=new ArrayList<>();

			/**
			 * Arnot: 
			 * 		BCF: OK 
			 * 		BAF: OK 
			 * 
			 * Burkhard: 
			 * 		BCF: Need to fix original values 
			 * 		BAF:Need to fix original values
			 *  
			 * ECOTOX:
			 * 		BCF: OK
			 * 		BAF: OK- not much
			 * 		Acute: OK
			 * 		Chronic: OK
			 * 
			 * QSAR_Toolbox/NITE: 
			 * 		BCF: Done
			 * 		TODO does QSAR toolbox have any tox acute/chronic data as specific db?
			 */			

			String propertyName=null;
			
			propertyName=ExperimentalConstants.strBCF;
			sources.add(new Source("Arnot 2006",propertyName));
			sources.add(new Source("Burkhard",propertyName));
			sources.add(new Source("ECOTOX_2024_12_12",propertyName));
			sources.add(new Source("QSAR_Toolbox","BCF NITE//"+propertyName)); 

			propertyName=ExperimentalConstants.strBAF;
			sources.add(new Source("Arnot 2006",propertyName));
			sources.add(new Source("Burkhard",propertyName));
			sources.add(new Source("ECOTOX_2024_12_12",propertyName));

			propertyName=ExperimentalConstants.strAcuteAquaticToxicity;
			sources.add(new Source("ECOTOX_2024_12_12",propertyName));
			
			propertyName=ExperimentalConstants.strChronicAquaticToxicity;
			sources.add(new Source("ECOTOX_2024_12_12",propertyName));

			CompareExperimentalRecords cer=new CompareExperimentalRecords();
			ExperimentalRecords experimentalRecords=cer.rm.getAllExperimentalRecords(sources);
						
//			System.out.println(gson.toJson(recsBCF));

			int countRemoved=experimentalRecords.removeBadRecords();
			
//			if(true)return;
			
			getMW_For_Molar_Records(experimentalRecords);
			
			List<ScoreRecord> scoreRecords=new ArrayList<>();
			HashSet<String>casrns=new HashSet<>();
			
			for (ExperimentalRecord er:experimentalRecords) {
				
//				if(er.casrn!=null && er.casrn.equals("50-04-4")) { 
//					System.out.println(gson.toJson(er)+"\n");
//				}
				if(er.property_name.equals(ExperimentalConstants.strBCF)
						|| er.property_name.equals(ExperimentalConstants.strBAF)) {
					ScoreRecord sr=ParseToxValBCFBAF.createScoreRecord(er);
					if(sr==null) continue;
					
					scoreRecords.add(sr);
					casrns.add(sr.CAS);
				
				} else if(er.property_name.equals(ExperimentalConstants.strAcuteAquaticToxicity) 
						|| er.property_name.equals(ExperimentalConstants.strChronicAquaticToxicity)) {
					ScoreRecord sr=CreateAquaticToxicityRecords.createDurationRecord(er);
					if(sr==null) continue;
					
					scoreRecords.add(sr);
					casrns.add(sr.CAS);
				}
			}
			
			if(lookupDTXSIDs)
				ScoreRecord.lookup_dtxsid_from_casrn(scoreRecords);//gets from dsstox based on casrn
	
			System.out.println("experimentalRecords.size="+experimentalRecords.size());
			System.out.println("scoreRecords.size()="+scoreRecords.size());
			System.out.println("casrns.size()="+casrns.size());			
//			System.out.println(gson.toJson(scoreRecords));

//			if(true)return;

			
			System.out.println(dbFile.getAbsolutePath()+"\t"+dbFile.exists());
//			CreateGHS_Database.createDatabase(textFilePath,dbPath,del,"HazardRecords",ScoreRecord.allFieldNames,"idx_CAS_Hazard_Records");

			try {
				saveHazardRecords(stat, scoreRecords, null,false);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}

		private void getMW_For_Molar_Records(ExperimentalRecords experimentalRecords) {

			HashSet<String>casrnsNoDtxsid=new HashSet<>();
			for (ExperimentalRecord er:experimentalRecords) {
				if(er.property_value_units_final==null)continue;
				if(!er.property_value_units_final.equals("M"))continue;
				if(er.dsstox_substance_id==null) {
					if(er.casrn!=null  && !er.casrn.isBlank())
						casrnsNoDtxsid.add(er.casrn);
				}
			}
			
			DsstoxLookup dl=new DsstoxLookup();
			List<DsstoxRecord>dsstoxRecords=dl.getDsstoxRecordsByCAS(casrnsNoDtxsid,true);
//			System.out.println("casrnsNoDtxsid.size()="+casrnsNoDtxsid.size());
//			System.out.println("dsstoxRecords.size()="+dsstoxRecords.size());
			
			Hashtable<String,DsstoxRecord>htCAS=new Hashtable<>();
			for (DsstoxRecord dr:dsstoxRecords) htCAS.put(dr.casrn, dr);
			
			HashSet<String>cantMapNameCAS=new HashSet<>();
			
			for (ExperimentalRecord er:experimentalRecords) {
				if(er.dsstox_substance_id==null) {
					if(er.casrn!=null && htCAS.containsKey(er.casrn)) {
						er.dsstox_substance_id=htCAS.get(er.casrn).dtxsid;//assign SID from CAS
					} else {
						cantMapNameCAS.add(er.casrn+"\t"+er.chemical_name);
					}
				}
			}
			
			HashSet <String>dtxsidsNeedMW=new HashSet<>();
			for (ExperimentalRecord er:experimentalRecords) {
				if(er.property_value_units_final.equals("M")) {
					if(er.dsstox_substance_id!=null) {
						dtxsidsNeedMW.add(er.dsstox_substance_id);//keep track of which sids need MW
					} 
				}
			}

			System.out.println("dtxsidsNeedMW.size()="+dtxsidsNeedMW.size());
			
			dsstoxRecords=dl.getDsstoxRecordsByDTXSIDS(dtxsidsNeedMW);

			Hashtable<String,DsstoxRecord>htSID=new Hashtable<>();
			for (DsstoxRecord dr:dsstoxRecords) htSID.put(dr.dtxsid, dr);

			for (ExperimentalRecord er:experimentalRecords) {
				if(er.property_value_units_final.equals("M")) {
					if(er.dsstox_substance_id!=null) {
						if(htSID.containsKey(er.dsstox_substance_id)) {
							DsstoxRecord dr=htSID.get(er.dsstox_substance_id);
							er.molecular_weight=dr.molecularWeight;//assign MW from the sid record
//							System.out.println(er.dsstox_substance_id+"\t"+er.molecular_weight);
						} else {
//							System.out.println(er.dsstox_substance_id+"\tN/A");
						}
						
					} 
				}
			}
		}
		
		void createMainHazardDatabase() {
				//Create files for all sources:
		//		Parse.recreateFilesAllSources();
				
				boolean forMDH=false;
				
		//		String date="2023_02_07";
				String date="2025_03_10";
				
				String folder=AADashboard.dataFolder+"\\dictionary\\text output";
				String textFileName="flat file "+date+".txt";		
				if (forMDH) textFileName="flat file "+date+" forMDH.txt";
				
				String textFilePath=folder+"\\"+textFileName;
		
				//Create flat file for all data:
		//		ScoreRecord.createFlatFileFromAllSourcesSorted(forMDH, textFilePath);
				
				//TODO need to redo individual sources such as New Zealand since some old as 2018 and the original data could be parsed better

				List<ScoreRecord>scoreRecords=ScoreRecord.createScoreRecordsAllSourcesLookupDtxsidByCAS(forMDH, textFilePath);
				
//				if(true)return;
				
				String dbFileName="AA dashboard.db";		
				if (forMDH) dbFileName="AA dashboard MDH.db";
				
				String dbPath="databases\\RevisedHazardDB\\"+dbFileName;
				
				File dbFile=new File(dbPath);
				System.out.println(dbFile.getAbsolutePath()+"\t"+dbFile.exists());
				
//				CreateGHS_Database.createDatabase(textFilePath,dbPath,del,"HazardRecords",ScoreRecord.allFieldNames,"idx_CAS_Hazard_Records");

				Connection conn= MySQL_DB.getConnection(dbPath);
				Statement stat = MySQL_DB.getStatement(conn);

				try {
					saveHazardRecords(stat, scoreRecords, null,false);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
		//		if (forMDH) {//TODO
		//			textFilePath=AADashboard.dataFolder+File.separator+RecordLink.sourceName+File.separator+RecordLink.sourceName+".txt";
		//			CreateGHS_Database.createDatabase(textFilePath,dbPath,del,"Links",RecordLink.fieldNames,"idx_CAS_Links");
		//		}
		//		
			}

			public void createCompleteHazardDatabase() {

				String folder = "databases\\RevisedHazardDB\\";
				String dbPathDest = folder + "HazardRecordsComplete.db";
				
				try {
//					Utilities.CopyFile(fileSrc, new File(dbPathDest));//just copy this one as use as starting point since biggest

					Files.copy(Paths.get(folder+"HazardRecordsModels.db"),
				            Paths.get(dbPathDest), StandardCopyOption.REPLACE_EXISTING);

				} catch (IOException e) {
					e.printStackTrace();
				}
				
				List<String> dbNames = Arrays.asList("AA dashboard.db", "Aquatic toxicity.db", 
						"toxval_v96.db");
				
//				List<String> dbNames = Arrays.asList("toxval_v96.db");

				try {

					Connection connDest = MySQL_DB.getConnection(dbPathDest);
					Statement statDest = connDest.createStatement();

					for (String dbName : dbNames) {
						System.out.println("\n"+dbName);
						Connection connSrc = MySQL_DB.getConnection(folder + dbName);
						copyHazardRecords(connSrc,statDest);
					}

				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		
	}


	public static void main(String[] args) {
		CreateCompleteHazardDatabase c=new CreateCompleteHazardDatabase();

		//		String versionToxVal=ParseToxValDB.v94;
		String versionToxVal=ParseToxValDB.v96;

//		Connection connSrc=null;
//
//		if(versionToxVal.equals(ParseToxValDB.v96)) {
//			connSrc=SqlUtilities.getConnectionToxVal();
//		} else if (versionToxVal.equals(ParseToxValDB.v94)) {
//			String dbPathDest="databases/toxval_"+versionToxVal+".db";
//			connSrc= MySQL_DB.getConnection(dbPathDest);
//		}

//		String dbPathDest="databases\\RevisedHazardDB\\toxval_"+versionToxVal+".db";
//		Connection connDest= MySQL_DB.getConnection(dbPathDest);
//		c.mySqlExport.createToxValCopy(connSrc, connDest,versionToxVal);
		
//		c.hazardRecordCreator.createHazardRecordsFromToxvalCopyTables(connDest,versionToxVal);
//		c.hazardRecordCreator.createHazardRecordsModelResults();
//		c.hazardRecordCreator.createMainHazardDatabase();
//		c.hazardRecordCreator.createAquaticToxDatabase();
		c.hazardRecordCreator.createCompleteHazardDatabase();

		//TODO take current hazard database and add dtxsids from the cas or alternate cas
		//TODO BCF from Arnot, Ecotox, NITE, and Burkhard from experimentalRecords jsons or from res_qsar records
		//TODO load acute LC50 data from ECOTOX from experimentalRecords jsons
		//TODO load chronic NOEC,LOEC data from ECOTOX from experimentalRecords jsons
		

	}



}
