package gov.epa.ghs_data_gathering.Parse.ECHA_endocrine_assessment_report;

import java.util.HashMap;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gov.epa.api.ScoreRecord;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

public class RecordECHAEndocrine {
	
	public String DISLIST_ED_name;
	public String DISLIST_ED_description;
	public String DISLIST_ED_ecnumber;
	public String DISLIST_ED_casnumber;
	public String DISLIST_ED_lec_submitter;
	public String DISLIST_ED_prc_public_status;
	public String DISLIST_ED_prc_conclusion;
	public String DISLIST_ED_prc_followup_activity;
	public String DISLIST_ED_diss_update_date;
	
	
	public static final String[] fieldNames = {"DISLIST_ED_name","DISLIST_ED_description",
			"DISLIST_ED_ecnumber","DISLIST_ED_casnumber","DISLIST_ED_lec_submitter",
			"DISLIST_ED_prc_public_status","DISLIST_ED_prc_conclusion","DISLIST_ED_prc_followup_activity",
			"DISLIST_ED_diss_update_date"};
	

	public static final String lastUpdated = "10/9/21";
	public static final String sourceName = ScoreRecord.strSourceECHAEndocrine;
	
	private static final String fileName = sourceName+".xlsx";
	
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
			RecordECHAEndocrine r = gson.fromJson(record.toString(),RecordECHAEndocrine.class);
			System.out.println(r.DISLIST_ED_casnumber+"\t"+r.DISLIST_ED_name+"\t"+r.DISLIST_ED_prc_conclusion);
		}
		
	}
	
}
