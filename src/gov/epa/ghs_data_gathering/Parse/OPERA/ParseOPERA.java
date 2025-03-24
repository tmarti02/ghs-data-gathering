package gov.epa.ghs_data_gathering.Parse.OPERA;

import java.io.FileWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import gov.epa.QSAR.utilities.JsonUtilities;
import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.database.SQLite_Utilities;
import gov.epa.database.SqlUtilities;
import gov.epa.exp_data_gathering.parse.JSONUtilities;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.ghs_data_gathering.Database.MySQL_DB;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_models.ParseToxValModels;



public class ParseOPERA  {

	class Identifier {
		String dtxcid;
		String dtxsid;
		String casrn;
		String name;
	}


	private Hashtable<String, Identifier> getIdentifiersByDTXCID_res_qsar()  {

		Hashtable<String,Identifier>htID=new Hashtable<>();

		String sqlID="select dtxcid, dtxsid,casrn,preferred_name from qsar_models.dsstox_records "
				+ "where fk_dsstox_snapshot_id=2";

		ResultSet rsID = SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sqlID);

		try {
			while (rsID.next()) {

				if(rsID.getString(1)==null) continue;

				Identifier i=new Identifier();

				i.dtxcid=rsID.getString(1);
				i.dtxsid=rsID.getString(2);
				i.casrn=rsID.getString(3);
				i.name=rsID.getString(4);

				if(i.casrn!=null && !ParseUtilities.isValidCAS(i.casrn)) {
					i.casrn=null;
				}

				if(i.name!=null && i.name.toLowerCase().contains("nocas"))  i.name=null;
				if(i.name!=null && i.name.toLowerCase().equals("noname"))  i.name=null;

				htID.put(i.dtxcid, i);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return htID;
	}


	/**
	 * This method reads from the OPERA prediction db and then directly writes to another db to store OPERA ER/AR hazard records
	 */
	public void parseOPERA_RecordsFromDB() {

		System.out.println("parseOPERA_RecordsFromDB");

		String dbSrc="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\OPERA_2.8.db";
		String dbDest="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18_EPA_Github\\databases\\OPERA2.8 ER AR hazard records.db";

		Hashtable<String, Identifier>htIdentifiersByCID=getIdentifiersByDTXCID_res_qsar();
		//		Hashtable<String, Identifier>htIdentifiersByCID=new Hashtable<>();
		System.out.println("Identifiers hashtable size="+htIdentifiersByCID.size());

		Connection connSrc=SQLite_Utilities.getConnection(dbSrc);
		Statement statSrc=SQLite_Utilities.getStatement(connSrc);

		//		Connection connDest=SQLite_Utilities.getConnection(dbDest);
		//		Statement statDest=SQLite_Utilities.getStatement(connDest);
		//		
		//		String tableName="HazardRecords";
		//		try {
		//			statDest.executeUpdate("drop table if exists "+tableName+";");
		//			statDest.executeUpdate("VACUUM;");//compress db now that have deleted the table
		//		} catch (SQLException e) {
		//			e.printStackTrace();
		//		}

		int num=0;
		int batch=250000;
		//		int batch=10;

		Vector<ScoreRecord>recordsAll=new Vector<>();

		while (true) {

			int minID=batch*num;
			int maxID=minID+batch;

			List<RecordOPERA> recordsOPERA = RecordOPERA.getRecords2(minID, maxID, statSrc);

			//			System.out.println(JsonUtilities.gsonPretty.toJson(recordsOPERA));
			//			if(true)break;

			Chemicals chemicals=new Chemicals();

			for (RecordOPERA ro:recordsOPERA) {
				//				System.out.println(i+"\t"+(t2-t1));
				Chemical chemical=createChemical(ro,statSrc,htIdentifiersByCID);
				if (chemical==null) continue;

				chemicals.add(chemical);
			}

			for (Chemical chemical:chemicals) {
				for (Score score:chemical.scores) {
					recordsAll.addAll(score.records);
				}
			}
			System.out.println(minID+"\t"+maxID+"\t"+recordsOPERA.size()+"\t"+recordsAll.size());

			//			addHazardRecordsToDatabase(recordsAll, tableName, "CAS",connDest, statDest);

			if (recordsOPERA.size()==0) break;
			num++;
			//			if (num==3) break;

			//			System.out.println(JsonUtilities.gsonPretty.toJson(recordsAll));

			//			if(true)break;

		}

		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\CERAPP_COMPARA\\";
		JSONUtilities.batchAndWriteJSON(recordsAll,folder+"OPERA2.8_CERAPP_COMPARA.json",100000);

		//		try {
		//			String sqlAddIndex="CREATE INDEX IF NOT EXISTS CAS ON "+tableName+" (CAS)";
		//			statDest.executeUpdate(sqlAddIndex);
		//		} catch (SQLException e) {
		//			e.printStackTrace();
		//		}			

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
	public Chemical createChemical(RecordOPERA r,Statement stat, Hashtable<String, Identifier> htIdentifiersByCID) {
		Chemical chemical = new Chemical();

		if(htIdentifiersByCID.containsKey(r.DSSTOX_COMPOUND_ID)) {
			Identifier i=htIdentifiersByCID.get(r.DSSTOX_COMPOUND_ID);
			chemical.CAS=i.casrn;
			chemical.name=i.name;
			chemical.dtxsid=i.dtxsid;
			chemical.dtxcid=i.dtxcid;
		} else {			
			//			System.out.println("Missing identifier for "+r.DSSTOX_COMPOUND_ID);
			return null;
		}

		createScoreRecordsEndocrineDisruption(r, chemical);

		//******************************************************************************************
		
//		Score score=chemical.scoreBioaccumulation;
//		
//		String sourceName="OPERA2.8";
//		
//		
//		if(r.LogBCF_exp!=null) {
//			String sourceOriginal="OPERA_BCF_Experimental_Value";
//			ParseToxValModels.createScoreRecordBCF(chemical.dtxsid,chemical.dtxcid,chemical.CAS,chemical.name,
//					sourceName, sourceOriginal,double value,String units,String listType,String url);
//			
//		}
//		ParseToxValModels.createScoreRecordBCF(String dtxsid,String dtxcid, String casrn,String name,
//				String sourceName, String sourceOriginal,double value,String units,String listType,String url);
//
		
//		addExperimentalScoreRecordBCF(chemical, score, r.LogBCF_exp,"OPERA_BCF","http://dx.doi.org/10.1289/ehp.1510267");
//		addPredictedScoreRecordBCF(chemical, score, r.LogBCF_pred, r.AD_BCF,r.AD_index_BCF,r.Conf_index_BCF,"OPERA_BCF","http://dx.doi.org/10.1289/ehp.1510267");

		return chemical;
	}


	private void createScoreRecordsEndocrineDisruption(RecordOPERA r, Chemical chemical) {
		Score score=chemical.scoreEndocrine_Disruption;


		//Use model name instead of paper name as the sourceOriginal:
		//******************************************************************************************
		//Agonist ER Exp		
		addExperimentalScoreRecord(chemical, score, r.CERAPP_Ago_exp,"estrogen receptor","agonist","OPERA_CERAPP-Agonist","http://dx.doi.org/10.1289/ehp.1510267");

		//Agonist ER pred
		addPredictedScoreRecord(chemical, score, r.CERAPP_Ago_pred, r.AD_CERAPP_Ago,r.AD_index_CERAPP_Ago,r.Conf_index_CERAPP_Ago,"estrogen receptor","agonist","OPERA_CERAPP-Agonist","http://dx.doi.org/10.1289/ehp.1510267");

		//******************************************************************************************
		//Antagonist ER Exp
		addExperimentalScoreRecord(chemical, score, r.CERAPP_Anta_exp,"estrogen receptor","antagonist","OPERA_CERAPP-Antagonist","http://dx.doi.org/10.1289/ehp.1510267");		

		//Antagonist ER pred
		addPredictedScoreRecord(chemical, score, r.CERAPP_Anta_pred, r.AD_CERAPP_Anta,r.AD_index_CERAPP_Anta,r.Conf_index_CERAPP_Anta,"estrogen receptor","antagonist","OPERA_CERAPP-Antagonist","http://dx.doi.org/10.1289/ehp.1510267");

		//******************************************************************************************
		//Binding ER Exp
		addExperimentalScoreRecord(chemical, score, r.CERAPP_Bind_exp,"estrogen receptor","binding","OPERA_CERAPP-Binding","http://dx.doi.org/10.1289/ehp.1510267");		

		//Binding ER pred
		addPredictedScoreRecord(chemical, score, r.CERAPP_Bind_pred, r.AD_CERAPP_Bind,r.AD_index_CERAPP_Bind,r.Conf_index_CERAPP_Bind,"estrogen receptor","binding","OPERA_CERAPP-Binding","http://dx.doi.org/10.1289/ehp.1510267");

		//******************************************************************************************
		//Agonist AR Exp
		addExperimentalScoreRecord(chemical, score, r.CoMPARA_Ago_exp,"androgen receptor","agonist","OPERA_CoMPARA-Agonist","https://doi.org/10.1289/EHP5580");

		//Agonist AR pred
		addPredictedScoreRecord(chemical, score, r.CoMPARA_Ago_pred,r.AD_CoMPARA_Ago,r.AD_index_CoMPARA_Ago,r.Conf_index_CoMPARA_Ago,"androgen receptor","agonist","OPERA_CoMPARA-Agonist","https://doi.org/10.1289/EHP5580");

		//******************************************************************************************
		//Antagonist AR Exp
		addExperimentalScoreRecord(chemical, score, r.CoMPARA_Anta_exp,"androgen receptor","antagonist","OPERA_CoMPARA-Antagonist","https://doi.org/10.1289/EHP5580");

		//Antagonist AR pred
		addPredictedScoreRecord(chemical, score, r.CoMPARA_Anta_pred,r.AD_CoMPARA_Anta,r.AD_index_CoMPARA_Anta,r.Conf_index_CoMPARA_Anta,"androgen receptor","antagonist","OPERA_CoMPARA-Antagonist","https://doi.org/10.1289/EHP5580");

		//******************************************************************************************
		//Binding AR Exp
		addExperimentalScoreRecord(chemical, score, r.CoMPARA_Bind_exp,"androgen receptor","binding","OPERA_CoMPARA-Binding","https://doi.org/10.1289/EHP5580");

		//Binding AR pred
		addPredictedScoreRecord(chemical, score, r.CoMPARA_Bind_pred,r.AD_CoMPARA_Bind,r.AD_index_CoMPARA_Bind,r.Conf_index_CoMPARA_Bind,"androgen receptor","binding","OPERA_CoMPARA-Binding","https://doi.org/10.1289/EHP5580");
		

		//old way use paper name for the sourceOriginal
			 //		//******************************************************************************************
			 //		//Agonist ER Exp		
			 //		addExperimentalScoreRecord(chemical, score, r.CERAPP_Ago_exp,"estrogen receptor","agonist","Mansouri 2016","http://dx.doi.org/10.1289/ehp.1510267");
			 //		
			 //		//Agonist ER pred
			 //		addPredictedScoreRecord(chemical, score, r.CERAPP_Ago_pred, r.AD_CERAPP_Ago,r.AD_index_CERAPP_Ago,r.Conf_index_CERAPP_Ago,"estrogen receptor","agonist","Mansouri 2016","http://dx.doi.org/10.1289/ehp.1510267");
			 //		
			 //		//******************************************************************************************
			 //		//Antagonist ER Exp
			 //		addExperimentalScoreRecord(chemical, score, r.CERAPP_Anta_exp,"estrogen receptor","antagonist","Mansouri 2016","http://dx.doi.org/10.1289/ehp.1510267");		
			 //		
			 //		//Antagonist ER pred
			 //		addPredictedScoreRecord(chemical, score, r.CERAPP_Anta_pred, r.AD_CERAPP_Anta,r.AD_index_CERAPP_Anta,r.Conf_index_CERAPP_Anta,"estrogen receptor","antagonist","Mansouri 2016","http://dx.doi.org/10.1289/ehp.1510267");
			 //		
			 //		//******************************************************************************************
			 //		//Agonist AR Exp
			 //		addExperimentalScoreRecord(chemical, score, r.CoMPARA_Ago_exp,"androgen receptor","agonist","Mansouri 2020","https://doi.org/10.1289/EHP5580");
			 //		
			 //		//Agonist AR pred
			 //		addPredictedScoreRecord(chemical, score, r.CoMPARA_Ago_pred,r.AD_CoMPARA_Ago,r.AD_index_CoMPARA_Ago,r.Conf_index_CoMPARA_Ago,"androgen receptor","agonist","Mansouri 2020","https://doi.org/10.1289/EHP5580");
			 //
			 //		//Antagonist AR Exp
			 //		addExperimentalScoreRecord(chemical, score, r.CoMPARA_Anta_exp,"androgen receptor","antagonist","Mansouri 2020","https://doi.org/10.1289/EHP5580");
			 //		
			 //		//Antagonist AR pred
			 //		addPredictedScoreRecord(chemical, score, r.CoMPARA_Anta_pred,r.AD_CoMPARA_Anta,r.AD_index_CoMPARA_Anta,r.Conf_index_CoMPARA_Anta,"androgen receptor","antagonist","Mansouri 2020","https://doi.org/10.1289/EHP5580");


	}


	public static ScoreRecord addPredictedScoreRecord(Chemical chemical, Score score, String tox,String AD,String AD_index,String Conf_index, String receptor,String receptorEffect,String modelName, String url) {

		if (tox.equals("?") ||  tox.isBlank()) {
			tox="-1";
		}

		int itox=Integer.parseInt(tox);
		int globalAD=Integer.parseInt(AD);
		double localAD=Double.parseDouble(AD_index);
		double confidenceIndex=Double.parseDouble(Conf_index);

		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		sr.dtxsid=chemical.dtxsid;
		sr.dtxcid=chemical.dtxcid;

		sr.valueMass=Double.valueOf(tox);
		sr.valueMassUnits="Binary";

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
			//			setAD(receptor, receptorEffect, globalAD, localAD, confidenceIndex, sr);
			sr.rationale+=". Applicability domain: "+getAD_reasoning(localAD,globalAD);
		}

		sr.url=url;
		sr.source="OPERA2.8";
		sr.sourceOriginal=modelName+"_Predicted_Value";
		sr.listType=ScoreRecord.typePredicted;
		sr.sourceTable="models";
		score.records.add(sr);
		
		return sr;
	}
	
	
	public static ScoreRecord createPredictedScoreRecordEndocrineDisruption(Double pred,String dtxsid, String dtxcid, String casrn, String name, String reasoningAD, String modelName,String sourceName) {

		String receptor="";
		String receptorEffect=modelName.substring(modelName.indexOf("-")+1,modelName.length());
		String url="";
		
		if (modelName.contains("CERAPP")) {
			url="http://dx.doi.org/10.1289/ehp.1510267";
			receptor="estrogen receptor";
		} else if(modelName.contains("CoMPARA")) {
			url="https://doi.org/10.1289/EHP5580";
			receptor="androgen receptor";
		} else {
			System.out.println("Handle modelName="+modelName);
			return null;
		}
		
		ScoreRecord sr = new ScoreRecord(Chemical.strEndocrine_Disruption,casrn,name);
		sr.dtxsid=dtxsid;
		sr.dtxcid=dtxcid;

		if(pred==null) {
			sr.score = ScoreRecord.scoreNA;
			sr.rationale="In vitro "+receptor+" "+receptorEffect+" toxicity could not be predicted using OPERA";			
		} else {
			sr.valueMass=pred;
			sr.valueMassUnits="Binary";
			
			if (sr.valueMass==1) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale="Chemical is predicted to be active "+receptorEffect+" to the "+receptor+" OPERA in vitro toxicity model";
			} else  if (sr.valueMass==0){
				sr.score = ScoreRecord.scoreL;
				sr.rationale="Chemical is predicted to be inactive "+receptorEffect+" to the "+receptor+" using OPERA in vitro toxicity model";
			} 
		}  
				
		if (sr.valueMass!=null) {
			//setAD(receptor, receptorEffect, globalAD, localAD, confidenceIndex, sr);
			sr.note=reasoningAD;
		}

		sr.url=url;
		sr.source=sourceName;
		sr.sourceOriginal=modelName+"_Predicted_Value";
		sr.listType=ScoreRecord.typePredicted;
		sr.sourceTable="models";
		
		return sr;
	}


	/**
	 * Based on guidance in OPERA paper and emails from Kamel Mansouri
	 * 
	 * @param receptor
	 * @param receptorEffect
	 * @param globalAD
	 * @param localAD
	 * @param dCI
	 * @param sr
	 */
	@Deprecated
	private void setAD(String receptor, String receptorEffect, int globalAD, double localAD, double dCI,
			ScoreRecord sr) {
		if (globalAD==1) {//Inside global AD
			if (localAD>=0.4 && localAD<=0.6) {
				sr.note="The prediction should be used with caution since the chemical is inside the global AD but 0.4 <= local AD index <= 0.6";
			} else if (localAD>0.6) {
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
			if (localAD>=0.4 && localAD<=0.6) {
				sr.note="The prediction has average reliability since the global AD is violated but 0.4 <= local AD index <= 0.6.";				
			} else if (localAD>0.6) {
				sr.note="The prediction is considered reliable since the global AD is violated but local AD index > 0.6.";
			} else  {//AD_Index<0.4
				sr.score = ScoreRecord.scoreNA;
				sr.rationale="The prediction for in vitro "+receptor+" "+receptorEffect+" toxicity is considered unreliable.";
				sr.note="The chemical is outside the local and global applicability domains.";
			}			
		}
	}

	/**
	 * Revised guidance from Kamel, no final AD set 
	 * 
	 * @param localAD
	 * @param globalAD
	 * @return
	 */
	static String getAD_reasoning(double localAD,double globalAD) {

		String reasoning=null;

		if(localAD<0.4 && globalAD==0) {
			reasoning=("outside training set (Global AD = 0) and poor local representation (Local AD index = "+localAD+" &lt; 0.4)");
		} else if (localAD<0.4 && globalAD==1) {
			reasoning=("inside training set (Global AD = 1) but poor local representation (Local AD index = "+localAD+" &le; 0.4)");
			//			System.out.println(pd.getDtxcid()+"\t"+pd.getModel().getDatasetName()+"\tInside global, outside local");
		} else if (localAD>=0.4 && localAD<=0.6  && globalAD==0) {
			reasoning=("outside training set (Global AD = 0) but fair local representation (0.4 &le; Local AD index = "+localAD+" &le; 0.6)");
		} else if (localAD>=0.4 && localAD<=0.6  && globalAD==1) {
			reasoning=("inside training set (Global AD = 1) and fair local representation  (0.4 &le; Local AD index = "+localAD+" &le; 0.6)");
		} else if (localAD>0.6 && globalAD==0) {
			reasoning=("outside training set (Global AD = 0) but good local representation (Local AD index = "+localAD+ " &gt; 0.6)");
		} else if (localAD>0.6 && globalAD==1) {
			reasoning=("inside training set (Global AD = 1) and good local representation (Local AD index = "+localAD+ " &gt; 0.6)");
		} else {
			reasoning=("cannot reach a conclusion based on the applicability domain results");//Does this even happen?
		}

		return reasoning;
	}


	private void addExperimentalScoreRecord(Chemical chemical, Score score, String tox,String receptor, String receptorEffect, String modelName,String url) {

		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);		
		sr.dtxsid=chemical.dtxsid;
		sr.dtxcid=chemical.dtxcid;

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
			sr.note="Experimental in vitro data from evaluation set in "+modelName;
			sr.url=url;
			sr.source="OPERA2.8";
			sr.sourceOriginal=modelName+"_Experimental_Value";
			sr.sourceTable="models";
			sr.listType=ScoreRecord.typeScreening;
			score.records.add(sr);

			//			System.out.println("Experimental record for "+sr.dtxsid);
		}
	}
	
	
	public static ScoreRecord createExperimentalScoreRecordEndocrineDisruption(String exp,String dtxsid, String dtxcid, String casrn, String name, String modelName,String sourceName) {

		String receptor="";
		String receptorEffect=modelName.substring(modelName.indexOf("-")+1,modelName.length());
		String url="";
		
		if (modelName.contains("CERAPP")) {
			url="http://dx.doi.org/10.1289/ehp.1510267";
			receptor="estrogen receptor";
		} else if(modelName.contains("CoMPARA")) {
			url="https://doi.org/10.1289/EHP5580";
			receptor="androgen receptor";
		} else {
			System.out.println("Handle modelName="+modelName);
			return null;
		}
		
		ScoreRecord sr = new ScoreRecord(Chemical.strEndocrine_Disruption,casrn,name);
		sr.dtxsid=dtxsid;
		sr.dtxcid=dtxcid;

		if (exp.equals("Inactive") || exp.equals("Active(weak)") || exp.equals("Active(very weak)")
				|| exp.equals("Active(strong)") || exp.equals("Active(medium)")) {
			if (exp.equals("Inactive")) {
				sr.score = ScoreRecord.scoreL;
			} else if (exp.equals("Active(weak)") || exp.equals("Active(very weak)")) {
				sr.score = ScoreRecord.scoreM;
			} else {
				sr.score = ScoreRecord.scoreH;
			}
			sr.rationale="Chemical is "+exp.toLowerCase()+" in in vitro tests as "+receptorEffect+" for "+receptor;
			sr.note="Experimental in vitro data from evaluation set in "+modelName;
			sr.url=url;
			sr.source=sourceName;
			sr.sourceOriginal=modelName+"_Experimental_Value";
//			sr.sourceTable="models";
			sr.listType=ScoreRecord.typeScreening;
			
			return sr;
			
		} else {
			return null;
		}
		
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParseOPERA p = new ParseOPERA();
		p.parseOPERA_RecordsFromDB();
	}
}
