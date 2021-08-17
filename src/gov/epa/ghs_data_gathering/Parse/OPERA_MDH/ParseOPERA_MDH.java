package gov.epa.ghs_data_gathering.Parse.OPERA_MDH;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.Parse;

public class ParseOPERA_MDH extends Parse {
	public ParseOPERA_MDH() {
		sourceName = ScoreRecord.strSourceOPERA_MDH;
		this.init();
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordOPERA_MDH.parseOPERA_MDH_RecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected Chemicals goThroughOriginalRecords() {

		Chemicals chemicals=new Chemicals();
		try {

			String jsonFilePath = mainFolder + File.separator + this.fileNameJSON_Records;

			Gson gson = new Gson();
			RecordOPERA_MDH[] records = gson.fromJson(new FileReader(jsonFilePath), RecordOPERA_MDH[].class);

			for (int i = 0; i < records.length; i++) {
				RecordOPERA_MDH sinRecord = records[i];
				Chemical chemical=createChemical(sinRecord);
				if (chemical==null) continue;
				handleMultipleCAS(chemicals, chemical);

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	private Chemical createChemical(RecordOPERA_MDH r) {
		Chemical chemical = new Chemical();
		chemical.CAS=r.CAS;
		chemical.name=r.Name;
		Score score=chemical.scoreEndocrine_Disruption;

		//******************************************************************************************
		//Agonist ER Exp		
		addExperimentalScoreRecord(chemical, score, r.CERAPP_Ago_exp,"estrogen receptor","agonist","Mansouri 2016","http://dx.doi.org/10.1289/ehp.1510267");
		
		//Agonist ER pred
		addPredictedScoreRecord(chemical, score, r.CERAPP_Ago_pred, r.AD_CERAPP_Ago,"estrogen receptor","agonist","Mansouri 2016","http://dx.doi.org/10.1289/ehp.1510267");
		
		//******************************************************************************************
		//Antagonist ER Exp
		addExperimentalScoreRecord(chemical, score, r.CERAPP_Anta_exp,"estrogen receptor","antagonist","Mansouri 2016","http://dx.doi.org/10.1289/ehp.1510267");		
		
		//Antagonist ER pred
		addPredictedScoreRecord(chemical, score, r.CERAPP_Anta_pred, r.AD_CERAPP_Anta,"estrogen receptor","antagonist","Mansouri 2016","http://dx.doi.org/10.1289/ehp.1510267");
		
		//******************************************************************************************
		//Agonist AR Exp
		addExperimentalScoreRecord(chemical, score, r.CoMPARA_Ago_exp,"androgen receptor","agonist","Mansouri 2020","https://doi.org/10.1289/EHP5580");
		
		//Agonist AR pred
		addPredictedScoreRecord(chemical, score, r.CoMPARA_Ago_pred,r.AD_CoMPARA_Ago,"androgen receptor","agonist","Mansouri 2020","https://doi.org/10.1289/EHP5580");

		//Antagonist AR Exp
		addExperimentalScoreRecord(chemical, score, r.CoMPARA_Anta_exp,"androgen receptor","antagonist","Mansouri 2020","https://doi.org/10.1289/EHP5580");
		
		//Antagonist AR pred
		addPredictedScoreRecord(chemical, score, r.CoMPARA_Anta_pred,r.AD_CoMPARA_Anta,"androgen receptor","antagonist","Mansouri 2020","https://doi.org/10.1289/EHP5580");


		return chemical;
	}

	private void addPredictedScoreRecord(Chemical chemical, Score score, String tox,String toxAD, String receptor,String receptorEffect,String paperAuthorYear, String url) {
		
		if (tox.equals("NaN")) {
			tox="-1";
		}
		
		
		int itox=Integer.parseInt(tox);
		int itoxAD=Integer.parseInt(toxAD);
		
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);

		if (itox==1) {
			sr.score = ScoreRecord.scoreH;
			sr.rationale="Chemical is predicted to be active "+receptorEffect+" to the "+receptor+" OPERA in vitro toxicity model";
		} else  if (itox==0){
			sr.score = ScoreRecord.scoreL;
			sr.rationale="Chemical is predicted to be inactive "+receptorEffect+" to the "+receptor+" using OPERA in vitro toxicity model";
		} else  {
			sr.score = ScoreRecord.scoreNA;
			sr.rationale="In vitro "+receptor+" "+receptorEffect+" toxicity could not be predicted using OPERA since descriptor calculations yielded missing descriptor(s)";			
		}
		
		if (itoxAD==0) {
			sr.score = ScoreRecord.scoreNA;
			sr.rationale="Chemical cannot be predicted for "+receptorEffect+" to the "+receptor+" using OPERA in vitro toxicity model since applicability domain is violated";
		}
		
//		sr.note="";//TODO add AD statement
		
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
		ParseOPERA_MDH p = new ParseOPERA_MDH();
		p.createFiles();
	}
}
