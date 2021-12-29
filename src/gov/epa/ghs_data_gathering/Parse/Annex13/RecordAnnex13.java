package gov.epa.ghs_data_gathering.Parse.Annex13;

import java.util.HashMap;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.api.ScoreRecord;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;
import gov.epa.ghs_data_gathering.Parse.ParseSIN;

public class RecordAnnex13 {
	
	
	public String Chemno;
	public String Selection;
	public String CASNR;
	public String Name;
	public String WILDL;
	public String HUM;
	public String COMB;

	


	public static final String[] fieldNames = {"Chemno","Selection","CASNR","Name","WILDL","HUM","COMB"};
	
	public static final String lastUpdated = "09/27/2021";//email from John Wambaugh
	public static final String sourceName = ScoreRecord.strSourceAnnex13;
	
	private static final String fileName = "Annex 13.xlsx";
	
	public static Vector<JsonObject> parseRecordsFromExcel() {
		String mainFolderPath="AA Dashboard\\Data";
		ExcelSourceReader esr = new ExcelSourceReader(fileName, mainFolderPath,sourceName);
		
//		esr.createClassTemplateFiles();		
		
		HashMap<Integer,String> hm = esr.generateDefaultMap(fieldNames, 0);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(hm, 2);
		return records;
		
	}
	
	
	public static void main(String[] args) {
		Vector<JsonObject> records =parseRecordsFromExcel();
		
		Gson gson= new Gson();
		for (JsonObject record:records) {
			RecordAnnex13 r = gson.fromJson(record.toString(),RecordAnnex13.class);
			System.out.println(r.CASNR+"\t"+r.Name+"\t"+r.HUM);
		}
		
	}
	
}
