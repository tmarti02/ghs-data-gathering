package gov.epa.ghs_data_gathering.Parse.CERAPP_EXP;

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


public class ParseCERAPP_Exp extends Parse {

	public ParseCERAPP_Exp() {
		sourceName = ScoreRecord.strSourceCERAPP_Exp;; 
		this.init();

		// TODO Is this a toxicity source? If so, rename original and experimental records files here.
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordCERAPP_Exp.parseCERAPP_ExpRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected Chemicals goThroughOriginalRecords() {
	
		Chemicals chemicals=new Chemicals();
		try {

			String jsonFilePath = mainFolder + File.separator + this.fileNameJSON_Records;

			Gson gson = new Gson();
			RecordCERAPP_Exp[] records = gson.fromJson(new FileReader(jsonFilePath), RecordCERAPP_Exp[].class);

			for (int i = 0; i < records.length; i++) {
				RecordCERAPP_Exp sinRecord = records[i];
				Chemical chemical=createChemical(sinRecord);
				if (chemical==null) continue;
				handleMultipleCAS(chemicals, chemical);
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	private Chemical createChemical(RecordCERAPP_Exp r) {
		Chemical chemical = new Chemical();
		
		chemical.CAS=r.CASRN;
		chemical.name=r.Name;
		
		Score score=chemical.scoreEndocrine_Disruption;
		
//		columns AUC.Agonist and AUC.Antagonist. The rule is that if these are >=0.1, the chemical is active and otherwise, inactive
		
		//Agonist
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		sr.source = "Judson 2015";
		sr.valueMass=Double.parseDouble(r.AUC_Agonist);
		if (sr.valueMass>=0.1) {
			sr.score = ScoreRecord.scoreH;
			sr.rationale="Agonist score for estrogen receptor (AUC_Agonist) >=0.1";
		} else {
			sr.score = ScoreRecord.scoreL;
			sr.rationale="Agonist score for estrogen receptor (AUC_Agonist) < 0.1";
		}
		sr.note="Experimental in vitro ToxCast multiassay score from training set in Judson 2015";
		sr.url="https://doi.org/10.1093/toxsci/kfv168";
		sr.listType=ScoreRecord.typeScreening;
		
		score.records.add(sr);
		
		//Antagonist
		sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		sr.source = "Judson 2015";
		sr.valueMass=Double.parseDouble(r.AUC_Antagonist);
		
		if (sr.valueMass>=0.1) {
			sr.score = ScoreRecord.scoreH;
			sr.rationale="Antagonist score for estrogen receptor (AUC_Antagonist) >=0.1";
			
		} else {
			sr.score = ScoreRecord.scoreL;
			sr.rationale="Antagonist score for estrogen receptor (AUC_Antagonist) < 0.1";
		}
		sr.note="Experimental in vitro ToxCast multiassay score from training set in Judson 2015";
		sr.url="https://doi.org/10.1093/toxsci/kfv168";
		sr.listType=ScoreRecord.typeScreening;
		score.records.add(sr);
		
		return chemical;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParseCERAPP_Exp p = new ParseCERAPP_Exp();
		p.createFiles();
	}

}