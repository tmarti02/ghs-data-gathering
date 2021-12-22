package gov.epa.ghs_data_gathering.Parse.Exposure_MDH;






import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;
import gov.epa.ghs_data_gathering.Parse.Parse;

public class ParseExposure_MDH extends Parse {

	public static final String fileName="HCD_TFK_HPV_fix_20211203_2021-12-16.xlsx";
	
	
	public ParseExposure_MDH() {
		sourceName = ScoreRecord.strSourceExposure_MDH;
		this.init();
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = parseRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}
	
	
	public static Vector<JsonObject> parseRecordsFromExcel() {
		String mainFolderPath="AA Dashboard\\Data";
		ExcelSourceReader esr = new ExcelSourceReader(fileName, mainFolderPath,sourceName,"Exposure Records");		
		String [] fieldNames=esr.getHeaders();		
		HashMap<Integer,String> hm = ExcelSourceReader.generateDefaultMap(fieldNames, 0);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(hm, 1);

		
		esr = new ExcelSourceReader(fileName, mainFolderPath,sourceName,"Exposure Profiles");		
		fieldNames=esr.getHeaders();		
		hm = ExcelSourceReader.generateDefaultMap(fieldNames, 0);
		Vector<JsonObject> records2 = esr.parseRecordsFromExcel(hm, 1);

		for(JsonObject jo:records2) {
			jo.addProperty("note", "final score");
			
			if (jo.get("CAS").getAsString().equals("100-40-3"))
				System.out.println(jo.toString());
		}
		
		records.addAll(records2);
		
		return records;
	}

	@Override
	protected Chemicals goThroughOriginalRecords() {

		Chemicals chemicals=new Chemicals();
		try {

			String jsonFilePath = mainFolder + File.separator + this.fileNameJSON_Records;

			Gson gson = new Gson();
			ScoreRecord[] records = gson.fromJson(new FileReader(jsonFilePath), ScoreRecord[].class);

			for (int i = 0; i < records.length; i++) {
				ScoreRecord sinRecord = records[i];
				Chemical chemical=createChemical(sinRecord);
				if (chemical==null) continue;
				handleMultipleCAS(chemicals, chemical);

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	private Chemical createChemical(ScoreRecord sr) {
		Chemical chemical = new Chemical();		
		Score score=new Score();
		chemical.CAS=sr.CAS;
		chemical.name=sr.name;
		
		if (sr.rationale!=null) sr.rationale=sr.rationale.replace("|", ";");
		
		
		score.hazard_name=sr.hazardName;		
		score.records.add(sr);		
		chemical.scores.add(score);
		return chemical;
	}

	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParseExposure_MDH p = new ParseExposure_MDH();
		p.writeJsonChemicalsFile=false;
		p.createFiles();
	}
}

