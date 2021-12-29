package gov.epa.ghs_data_gathering.Parse.ECHA_endocrine_assessment_report;

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


public class ParseECHAEndocrine extends Parse {

	public ParseECHAEndocrine() {
		sourceName = RecordECHAEndocrine.sourceName; 
		this.init();

		// TODO Is this a toxicity source? If so, rename original and experimental records files here.
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordECHAEndocrine.parseRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected Chemicals goThroughOriginalRecords() {
	
		Chemicals chemicals=new Chemicals();
		try {

			String jsonFilePath = mainFolder + File.separator + this.fileNameJSON_Records;

			Gson gson = new Gson();
			RecordECHAEndocrine[] records = gson.fromJson(new FileReader(jsonFilePath), RecordECHAEndocrine[].class);

			for (int i = 0; i < records.length; i++) {
				RecordECHAEndocrine sinRecord = records[i];
				Chemical chemical=createChemical(sinRecord);
				if (chemical==null) continue;
				handleMultipleCAS(chemicals, chemical);
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	private Chemical createChemical(RecordECHAEndocrine r) {
		Chemical chemical = new Chemical();
		chemical.CAS=r.DISLIST_ED_casnumber;
		
		if (chemical.CAS.trim().equals("-")) chemical.CAS="";
		
		chemical.name=r.DISLIST_ED_name;
		Score score=chemical.scoreEndocrine_Disruption;
		
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		sr.source = r.sourceName;
		
		if (!r.DISLIST_ED_prc_public_status.equals("Concluded")) return null;
		
		if (r.DISLIST_ED_prc_conclusion.contains("ED HH")) {
			sr.score=ScoreRecord.scoreH;
			sr.rationale="Human health endocrine disruptor according to ECHA";			
		} else if (r.DISLIST_ED_prc_conclusion.contains("not ED")) {
			sr.score=ScoreRecord.scoreL;
			sr.rationale="Not a human health endocrine disruptor according to ECHA";			
		} else if (r.DISLIST_ED_prc_conclusion.contains("inconclusive")) {
			sr.score=ScoreRecord.scoreNA;
			sr.rationale="Inconclusive as human health endocrine disruptor according to ECHA";			
		} else {
			System.out.println("Conclusion not covered by code:"+r.DISLIST_ED_prc_conclusion);
			return null;
		}
				
		sr.url="https://echa.europa.eu/ed-assessment";
		sr.listType=ScoreRecord.typeScreening;
		score.records.add(sr);
		return chemical;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParseECHAEndocrine p = new ParseECHAEndocrine();
		p.generateOriginalJSONRecords=true;
		p.writeJsonChemicalsFile=true;
		
		p.createFiles();
	}

}