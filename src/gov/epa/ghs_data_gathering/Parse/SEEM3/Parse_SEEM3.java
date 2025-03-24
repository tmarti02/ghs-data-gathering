package gov.epa.ghs_data_gathering.Parse.SEEM3;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.Parse;


public class Parse_SEEM3 extends Parse {

	public Parse_SEEM3() {
		sourceName = RecordSEEM3.sourceName; 
		this.init();

		// TODO Is this a toxicity source? If so, rename original and experimental records files here.
	}

	@Override
	protected void createRecords() {
//		Vector<JsonObject> records = RecordSEEM3.parseRecordsFromExcel();
		JsonArray records = RecordSEEM3.parseRecordsFromCSV();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected Chemicals goThroughOriginalRecords() {
	
		Chemicals chemicals=new Chemicals();
		try {

			String jsonFilePath = mainFolder + File.separator + this.fileNameJSON_Records;

			Gson gson = new Gson();
			RecordSEEM3[] records = gson.fromJson(new FileReader(jsonFilePath), RecordSEEM3[].class);

			for (int i = 0; i < records.length; i++) {
				RecordSEEM3 sinRecord = records[i];
				Chemical chemical=createChemical(sinRecord);
				if (chemical==null) continue;
				handleMultipleCAS(chemicals, chemical);
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	private Chemical createChemical(RecordSEEM3 r) {
		Chemical chemical = new Chemical();
		
		
//		"dsstox_substance_id": "DTXSID00110012",
//	    "delta_Diet_pred": "0.36499999999999999",//TODO
//	    "delta_Res_pred": "0.4012",
//	    "delta_Pest_pred": "0.32119999999999999",
//	    "delta_Indust_pred": "0.4672",
//	    "seem3": "2.9631137820529898E-7",
//	    "seem3_u95": "NA",
//	    "Pathway": "Unknown",
//	    "AD": "0"
	    	
	    			
		chemical.CAS=r.CAS;
		chemical.name=r.Substance_Name.replace("|", "_");
		
		if(chemical.name.substring(0,1).equals("\"") && chemical.name.substring(chemical.name.length()-1,chemical.name.length()).equals("\"")) {
			chemical.name=chemical.name.substring(1,chemical.name.length()-1);
//			System.out.println(chemical.CAS+"\tfixed");
		}
		
		chemical.dtxsid=r.dsstox_substance_id;

		Score score=chemical.scoreExposure;
		
		//Agonist
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		sr.dtxsid=chemical.dtxsid;
		
		sr.source = r.sourceName;
		
		if (r.seem3_u95==null || r.seem3_u95.equals("NA")) {
			sr.valueMass=Double.NaN;
		} else {
			sr.valueMass=Double.parseDouble(r.seem3_u95);	
		}
		
		
		sr.valueMassUnits="mg/kg-day";
		

		if (r.AD.equals("1")) {
			if (sr.valueMass>=1) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale="SEEM3 u95 exposure >= 1 mg/kg-day";
			} else if (sr.valueMass>=1e-3) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale="1e-3 mg/kg-day <= SEEM3 u95 exposure < 1 mg/kg-day";
			} else if (sr.valueMass>=1e-4) {
				sr.score = ScoreRecord.scoreM;
				sr.rationale="1e-4 mg/kg-day <= SEEM3 u95 exposure < 1e-3 mg/kg-day";
			} else {
				sr.score = ScoreRecord.scoreL;
				sr.rationale="SEEM3 u95 exposure < 1e-4 mg/kg-day";
			}
			
		} else {
			sr.score=ScoreRecord.scoreNA;
			sr.rationale="Applicability domain of SEEM3 model violated";
		}
		
		sr.route=r.Pathway;
		sr.url="https://www.epa.gov/sites/default/files/2019-02/documents/comptoxcop-wambaugh-seem3-022819.pdf";//TODO
		sr.url+="; https://doi.org/10.1021/acs.est.8b04056";
		sr.url+="; https://github.com/HumanExposure/SEEM3RPackage";
				
		sr.listType=ScoreRecord.typePredicted;
		
		score.records.add(sr);
		
		return chemical;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Parse_SEEM3 p = new Parse_SEEM3();
		
		p.generateOriginalJSONRecords=false;		
		p.writeJsonChemicalsFile=false;
		p.writeFlatFile=true;
		p.createDictionaryFile=false;
		p.createFiles();
	}

}
