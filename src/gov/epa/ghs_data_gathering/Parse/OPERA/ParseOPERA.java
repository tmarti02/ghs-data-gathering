package gov.epa.ghs_data_gathering.Parse.OPERA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.Database.ResolverDb2;
import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.database.SQLite_Utilities;
import gov.epa.ghs_data_gathering.Database.MySQL_DB;
import gov.epa.ghs_data_gathering.Parse.Parse;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

public class ParseOPERA extends Parse {
	public ParseOPERA() {
		sourceName = RecordOPERA.sourceName;
		this.init();
	}

	@Override
	protected void createRecords() {
		parseOPERA_RecordsFromDB();		
	}
	
	@Override
	protected Chemicals goThroughOriginalRecords() {
		return new Chemicals();
	}

	/**
	 * This method reads from the OPERA prediction db and then directly writes to another db to store OPERA ER/AR hazard records
	 */
	public void parseOPERA_RecordsFromDB() {
		
		System.out.println("parseOPERA_RecordsFromDB");
		
		Connection connIn=SQLite_Utilities.getConnection(RecordOPERA.dbpath);
		Statement statIn=SQLite_Utilities.getStatement(connIn);

		Connection connOut=SQLite_Utilities.getConnection("databases/OPERA ER AR hazard records.db");
		Statement statOut=SQLite_Utilities.getStatement(connOut);
		
		String tableName="HazardRecords";
		try {
			statOut.executeUpdate("drop table if exists "+tableName+";");
			statOut.executeUpdate("VACUUM;");//compress db now that have deleted the table
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Gson gson=new Gson();
		
		int num=0;
		int batch=10000;

		while (true) {

			int minID=batch*num;
			int maxID=minID+batch;
						
			Vector<JsonObject> records = RecordOPERA.getRecords(minID, maxID, statIn);
			
			Chemicals chemicals=new Chemicals();
			for (int i = 0; i < records.size(); i++) {
				JsonObject jo = records.get(i);
				
				long t1=System.currentTimeMillis();
				
				RecordOPERA ro=gson.fromJson(jo, RecordOPERA.class);
				long t2=System.currentTimeMillis();
				
//				System.out.println(i+"\t"+(t2-t1));
				
				Chemical chemical=createChemical(ro,statIn);
				if (chemical==null) continue;
				handleMultipleCAS(chemicals, chemical);
			}

			Vector<ScoreRecord>recordsAll=new Vector<>();
			
			for (Chemical chemical:chemicals) {
				for (Score score:chemical.scores) {
					recordsAll.addAll(score.records);
				}
			}
			System.out.println(minID+"\t"+maxID+"\t"+records.size()+"\t"+recordsAll.size());

			addHazardRecordsToDatabase(recordsAll, tableName, "CAS",connOut, statOut);
			
			if (records.size()==0) break;
			num++;
//			if (num==3) break;
		}
		
		try {
			String sqlAddIndex="CREATE INDEX IF NOT EXISTS CAS ON "+tableName+" (CAS)";
			statOut.executeUpdate(sqlAddIndex);
		} catch (SQLException e) {
			e.printStackTrace();
		}			

	}
	
	
	public static void addHazardRecordsToDatabase(Vector<ScoreRecord>recordsAll,String tableName,String indexName,Connection conn, Statement stat) {

		try {
//			System.out.println("Creating AA dashboard SQlite table");

			conn.setAutoCommit(true);
			
			//Need CAS as the primary key if we are doing lots of searches- otherwise searches will be like 1 second each!
			MySQL_DB.create_table_key_with_duplicates(stat, tableName, ScoreRecord.allFieldNames,"CAS");//need unique values in the table for key field for this to work!

			conn.setAutoCommit(false);

			String s = "insert into " + tableName + " values (";

			for (int i = 1; i <= ScoreRecord.allFieldNames.length; i++) {
				s += "?";
				if (i < ScoreRecord.allFieldNames.length)
					s += ",";
			}
			s += ");";

			int counter = 0;

			PreparedStatement prep = conn.prepareStatement(s);
			
			for (ScoreRecord sr:recordsAll) {
				counter++;
				for (int i = 0; i < ScoreRecord.allFieldNames.length; i++) {
					Field myField = sr.getClass().getDeclaredField(ScoreRecord.allFieldNames[i]);
					prep.setString(i + 1, (String) myField.get(sr));
				}
				prep.addBatch();

				if (counter % 1000 == 0) {
					// System.out.println(counter);
					prep.executeBatch();
				}
			}

			int[] count = prep.executeBatch();// do what's left

			conn.setAutoCommit(true);
									

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	public Chemical createChemical(RecordOPERA r,Statement stat) {
		Chemical chemical = new Chemical();

		long t1=System.currentTimeMillis();
		JsonObject jo=RecordOPERA.getRecordID(r.DSSTOX_COMPOUND_ID,stat);
		
		if (jo!=null) {
			chemical.CAS=jo.get("CASRN").getAsString();
			chemical.name=jo.get("PREFERRED_NAME").getAsString();
		} else {
			System.out.println("ID info missing for "+r.DSSTOX_COMPOUND_ID);
		}
		
		long t2=System.currentTimeMillis();
//		System.out.println("Time to get id info="+(t2-t1));
		
//		ArrayList<DSSToxRecord> recDSSTOX=ResolverDb2.lookupByDTXCID(r.DSSTOX_COMPOUND_ID);
//		
//		if (recDSSTOX.size()>0) {
//			DSSToxRecord rec0=recDSSTOX.get(0);
//			chemical.CAS=rec0.cas;
//			chemical.name=rec0.name;
//		}
		
		Score score=chemical.scoreEndocrine_Disruption;

		//******************************************************************************************
		//Agonist ER Exp		
		addExperimentalScoreRecord(chemical, score, r.CERAPP_Ago_exp,"estrogen receptor","agonist","Mansouri 2016","http://dx.doi.org/10.1289/ehp.1510267");
		
		//Agonist ER pred
		addPredictedScoreRecord(chemical, score, r.CERAPP_Ago_pred, r.AD_CERAPP_Ago,r.AD_index_CERAPP_Ago,r.Conf_index_CERAPP_Ago,"estrogen receptor","agonist","Mansouri 2016","http://dx.doi.org/10.1289/ehp.1510267");
		
		//******************************************************************************************
		//Antagonist ER Exp
		addExperimentalScoreRecord(chemical, score, r.CERAPP_Anta_exp,"estrogen receptor","antagonist","Mansouri 2016","http://dx.doi.org/10.1289/ehp.1510267");		
		
		//Antagonist ER pred
		addPredictedScoreRecord(chemical, score, r.CERAPP_Anta_pred, r.AD_CERAPP_Anta,r.AD_index_CERAPP_Anta,r.Conf_index_CERAPP_Anta,"estrogen receptor","antagonist","Mansouri 2016","http://dx.doi.org/10.1289/ehp.1510267");
		
		//******************************************************************************************
		//Agonist AR Exp
		addExperimentalScoreRecord(chemical, score, r.CoMPARA_Ago_exp,"androgen receptor","agonist","Mansouri 2020","https://doi.org/10.1289/EHP5580");
		
		//Agonist AR pred
		addPredictedScoreRecord(chemical, score, r.CoMPARA_Ago_pred,r.AD_CoMPARA_Ago,r.AD_index_CoMPARA_Ago,r.Conf_index_CoMPARA_Ago,"androgen receptor","agonist","Mansouri 2020","https://doi.org/10.1289/EHP5580");

		//Antagonist AR Exp
		addExperimentalScoreRecord(chemical, score, r.CoMPARA_Anta_exp,"androgen receptor","antagonist","Mansouri 2020","https://doi.org/10.1289/EHP5580");
		
		//Antagonist AR pred
		addPredictedScoreRecord(chemical, score, r.CoMPARA_Anta_pred,r.AD_CoMPARA_Anta,r.AD_index_CoMPARA_Anta,r.Conf_index_CoMPARA_Anta,"androgen receptor","antagonist","Mansouri 2020","https://doi.org/10.1289/EHP5580");


		return chemical;
	}

	private void addPredictedScoreRecord(Chemical chemical, Score score, String tox,String AD,String AD_index,String Conf_index, String receptor,String receptorEffect,String paperAuthorYear, String url) {
		
		if (tox.equals("?")) {
			tox="-1";
		}
				
		int itox=Integer.parseInt(tox);
		int itoxAD=Integer.parseInt(AD);
		double dAD_index=Double.parseDouble(AD_index);
		double dCI=Double.parseDouble(Conf_index);
		
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);

		if (itox==1) {
			sr.score = ScoreRecord.scoreH;
			sr.rationale="Chemical is predicted to be active "+receptorEffect+" to the "+receptor+" OPERA in vitro toxicity model";
		} else  if (itox==0){
			sr.score = ScoreRecord.scoreL;
			sr.rationale="Chemical is predicted to be inactive "+receptorEffect+" to the "+receptor+" using OPERA in vitro toxicity model";
		} else  {
			sr.score = ScoreRecord.scoreNA;
			sr.rationale="In vitro "+receptor+" "+receptorEffect+" toxicity could not be predicted using OPERA";			
		}
		
		if (itox!=-1) {
			if (itoxAD==1) {//Inside global AD
				if (dAD_index>=0.4 && dAD_index<=0.6) {
					sr.note="The prediction should be used with caution since the chemical is inside the global AD but 0.4 <= local AD index <= 0.6";
				} else if (dAD_index>0.6) {
					sr.note="The prediction is considered reliable since the chemical is inside the global and local AD.";
				} else {//AD_Index<0.4
					if (dCI>0.5) {
						sr.note="The prediction is considered reliable. While the chemical is not within in the local AD, it is inside the global AD and the confidence index > 0.5.";
					} else {
						sr.score = ScoreRecord.scoreNA;
						sr.rationale="The prediction for in vitro "+receptor+" "+receptorEffect+" toxicity is considered unreliable.";
						sr.note="The chemical is inside the global AD of the model but local AD index < 0.4 and confidence index <= 0.5.";
					}
				}
			} else {//Not inside global AD
				if (dAD_index>=0.4 && dAD_index<=0.6) {
					sr.note="The prediction has average reliability since the global AD is violated but 0.4 <= local AD index <= 0.6.";				
				} else if (dAD_index>0.6) {
					sr.note="The prediction is considered reliable since the global AD is violated but local AD index > 0.6.";
				} else  {//AD_Index<0.4
					sr.score = ScoreRecord.scoreNA;
					sr.rationale="The prediction for in vitro "+receptor+" "+receptorEffect+" toxicity is considered unreliable.";
					sr.note="The chemical is outside the local and global applicability domains.";
				}			
			}
		}
			
		sr.url=url;
		sr.source="OPERA (predicted value)";
		sr.sourceOriginal=paperAuthorYear;
		sr.listType=ScoreRecord.typePredicted;
		score.records.add(sr);
	}

	private void addExperimentalScoreRecord(Chemical chemical, Score score, String tox,String receptor, String receptorEffect, String paperAuthorName,String url) {
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		
		if (tox.equals("Inactive") || tox.equals("Active(weak)") || tox.equals("Active(very weak)")
				|| tox.equals("Active(strong)") || tox.equals("Active(medium)")) {
			if (tox.equals("Inactive")) {
				sr.score = ScoreRecord.scoreL;
			} else if (tox.equals("Active(weak)") || tox.equals("Active(very weak)")) {
				sr.score = ScoreRecord.scoreM;
			} else {
				sr.score = ScoreRecord.scoreH;
			}
			sr.rationale="Chemical is "+tox.toLowerCase()+" in in vitro tests as "+receptorEffect+" for "+receptor;
			sr.note="Experimental in vitro data from evaluation set in "+paperAuthorName;
			sr.url=url;
			sr.source="OPERA (experimental value)";
			sr.sourceOriginal=paperAuthorName;
			sr.listType=ScoreRecord.typeScreening;
			score.records.add(sr);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParseOPERA p = new ParseOPERA();
		p.createFiles();
	}
}
