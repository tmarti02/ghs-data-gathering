package gov.epa.ghs_data_gathering.Parse.COMPARA_EXP;

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

public class ParseCOMPARA_Exp extends Parse {

	
	public ParseCOMPARA_Exp() {
		sourceName = ScoreRecord.strSourceCOMPARA_Exp; 
		this.init();
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordCOMPARA_Exp.parseCOMPARA_ExpRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected Chemicals goThroughOriginalRecords() {
	
		Chemicals chemicals=new Chemicals();
		try {

			String jsonFilePath = mainFolder + File.separator + this.fileNameJSON_Records;

			Gson gson = new Gson();
			RecordCOMPARA_Exp[] records = gson.fromJson(new FileReader(jsonFilePath), RecordCOMPARA_Exp[].class);

			for (int i = 0; i < records.length; i++) {
				RecordCOMPARA_Exp sinRecord = records[i];
				Chemical chemical=createChemical(sinRecord);
				if (chemical==null) continue;
				handleMultipleCAS(chemicals, chemical);
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	private Chemical createChemical(RecordCOMPARA_Exp r) {
		Chemical chemical = new Chemical();
		
		chemical.CAS=r.casrn;
		chemical.name=r.name;
		
		Score score=chemical.scoreEndocrine_Disruption;
		
//		columns AUC.Agonist and AUC.Antagonist. The rule is that if these are >=0.1, the chemical is active and otherwise, inactive
		
		//Agonist
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		sr.source = "Judson 2020";
		sr.valueMass=Double.parseDouble(r.AUC_Agonist);
		if (sr.valueMass>=0.1) {
			sr.score = ScoreRecord.scoreH;
			sr.rationale="Agonist score for androgen receptor (AUC_Agonist) >=0.1";
		} else {
			sr.score = ScoreRecord.scoreL;
			sr.rationale="Agonist score for androgen receptor (AUC_Agonist) < 0.1";
		}
		
		sr.note="Experimental in vitro ToxCast multiassay score from training set in Judson 2020";
		sr.url="https://doi.org/10.1016/j.yrtph.2020.104764";
		sr.listType=ScoreRecord.typeScreening;
		score.records.add(sr);
		
		//Antagonist
		sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		sr.source = "Judson 2020";
		sr.valueMass=Double.parseDouble(r.AUC_Antagonist);
		
		if (sr.valueMass>=0.1) {
			sr.score = ScoreRecord.scoreH;
			sr.rationale="Antagonist score for androgen receptor (AUC_Antagonist) >=0.1";
			
		} else {
			sr.score = ScoreRecord.scoreL;
			sr.rationale="Antagonist score for androgen receptor (AUC_Antagonist) < 0.1";
		}
		
		sr.note="Experimental in vitro ToxCast multiassay score from training set in Judson 2020";
		sr.url="https://doi.org/10.1016/j.yrtph.2020.104764";
		sr.listType=ScoreRecord.typeScreening;
		score.records.add(sr);
		
		return chemical;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParseCOMPARA_Exp p = new ParseCOMPARA_Exp();
		p.createFiles();
	}
	
}
