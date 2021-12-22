package gov.epa.ghs_data_gathering.Parse.Annex13;

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


public class Parse_Annex13 extends Parse {

	public Parse_Annex13() {
		sourceName = RecordAnnex13.sourceName; 
		this.init();

		// TODO Is this a toxicity source? If so, rename original and experimental records files here.
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordAnnex13.parseRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected Chemicals goThroughOriginalRecords() {
	
		Chemicals chemicals=new Chemicals();
		try {

			String jsonFilePath = mainFolder + File.separator + this.fileNameJSON_Records;

			Gson gson = new Gson();
			RecordAnnex13[] records = gson.fromJson(new FileReader(jsonFilePath), RecordAnnex13[].class);

			for (int i = 0; i < records.length; i++) {
				RecordAnnex13 sinRecord = records[i];
				Chemical chemical=createChemical(sinRecord);
				if (chemical==null) continue;
				handleMultipleCAS(chemicals, chemical);
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	private Chemical createChemical(RecordAnnex13 r) {
		Chemical chemical = new Chemical();
		chemical.CAS=r.CASNR;
		
		if (chemical.CAS.contains("No CAS")) chemical.CAS="";
		
		chemical.name=r.Name;
		Score score=chemical.scoreEndocrine_Disruption;
		
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		sr.source = RecordAnnex13.sourceName;
		
		if (r.HUM.equals("1") || r.HUM.equals("2")) {
			sr.score=ScoreRecord.scoreH;
			sr.category="Category "+r.HUM;
			sr.rationale="A score of "+sr.score+" was assigned for human endocrine disruption score of "+sr.category;
		} else if (r.HUM.equals("3")) {
			sr.score=ScoreRecord.scoreL;
			sr.category="Category "+r.HUM;
			sr.rationale="A score of "+sr.score+" was assigned for human endocrine disruption score of "+sr.category;		
		} else {
			sr.score=ScoreRecord.scoreNA;
			sr.rationale="A score could not be assigned since a human endocrine disruption score is not available";
		}
		
		sr.url="https://ec.europa.eu/environment/archives/docum/pdf/bkh_annex_13.pdf";
		sr.listType=ScoreRecord.typeScreening;
		score.records.add(sr);
		return chemical;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Parse_Annex13 p = new Parse_Annex13();
		p.generateOriginalJSONRecords=true;
		p.writeJsonChemicalsFile=true;
		
		p.createFiles();
	}

}