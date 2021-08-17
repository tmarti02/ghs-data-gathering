package gov.epa.ghs_data_gathering.Parse.OPERA_MDH;

import java.util.HashMap;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gov.epa.api.ScoreRecord;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

public class RecordOPERA_MDH {

	public String MoleculeID;
	public String Name;
	public String CAS;
	
	public String CERAPP_Ago_exp;//*
	public String CERAPP_Ago_pred;
	public String AD_CERAPP_Ago;
	public String AD_index_CERAPP_Ago;
	public String Conf_index_CERAPP_Ago;
	
	public String CERAPP_Anta_exp;//*
	public String CERAPP_Anta_pred;
	public String AD_CERAPP_Anta;
	public String AD_index_CERAPP_Anta;
	public String Conf_index_CERAPP_Anta;
	
	public String CERAPP_Bind_exp;
	public String CERAPP_Bind_pred;
	public String AD_CERAPP_Bind;
	public String AD_index_CERAPP_Bind;
	public String Conf_index_CERAPP_Bind;
	
	public String CoMPARA_Ago_exp;//*
	public String CoMPARA_Ago_pred;
	public String AD_CoMPARA_Ago;
	public String AD_index_CoMPARA_Ago;
	public String Conf_index_CoMPARA_Ago;
	
	public String CoMPARA_Anta_exp;//*
	public String CoMPARA_Anta_pred;
	public String AD_CoMPARA_Anta;
	public String AD_index_CoMPARA_Anta;
	public String Conf_index_CoMPARA_Anta;
	
	public String CoMPARA_Bind_exp;
	public String CoMPARA_Bind_pred;
	public String AD_CoMPARA_Bind;
	public String AD_index_CoMPARA_Bind;
	public String Conf_index_CoMPARA_Bind;
	public String AUC_Agonist_ER_paper;

	public static final String[] fieldNames = { "MoleculeID", "Name", "CAS", "CERAPP_Ago_exp", "CERAPP_Ago_pred",
			"AD_CERAPP_Ago", "AD_index_CERAPP_Ago", "Conf_index_CERAPP_Ago", "CERAPP_Anta_exp", "CERAPP_Anta_pred",
			"AD_CERAPP_Anta", "AD_index_CERAPP_Anta", "Conf_index_CERAPP_Anta", "CERAPP_Bind_exp", "CERAPP_Bind_pred",
			"AD_CERAPP_Bind", "AD_index_CERAPP_Bind", "Conf_index_CERAPP_Bind", "CoMPARA_Ago_exp", "CoMPARA_Ago_pred",
			"AD_CoMPARA_Ago", "AD_index_CoMPARA_Ago", "Conf_index_CoMPARA_Ago", "CoMPARA_Anta_exp", "CoMPARA_Anta_pred",
			"AD_CoMPARA_Anta", "AD_index_CoMPARA_Anta", "Conf_index_CoMPARA_Anta", "CoMPARA_Bind_exp",
			"CoMPARA_Bind_pred", "AD_CoMPARA_Bind", "AD_index_CoMPARA_Bind", "Conf_index_CoMPARA_Bind",
			"AUC_Agonist_ER_paper" };
	
	public static final String lastUpdated = "07/19/2021";//results from running opera version 2.7: https://github.com/kmansouri/OPERA/releases/tag/v2.7-beta2
	public static final String sourceName = ScoreRecord.strSourceOPERA_MDH;
	
	private static final String fileName = "MNDOHTOXFREE list_chemicals-2021-07-14-13-47-13-smi_OPERA2.7Pred.xlsx";
	
	
	
	public static Vector<JsonObject> parseOPERA_MDH_RecordsFromExcel() {
		String mainFolderPath="AA Dashboard\\Data";
		ExcelSourceReader esr = new ExcelSourceReader(fileName, mainFolderPath,sourceName);
		
//		esr.createClassTemplateFiles();		
		
		HashMap<Integer,String> hm = esr.generateDefaultMap(fieldNames, 0);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(hm, 0);
		return records;
	}
	
	
	public static void main(String[] args) {
		Vector<JsonObject> records =parseOPERA_MDH_RecordsFromExcel();
		
		Gson gson= new Gson();
		for (JsonObject record:records) {
			RecordOPERA_MDH r = gson.fromJson(record.toString(),RecordOPERA_MDH.class);
			
//			System.out.println(r.CASRN+"\t"+r.AUC_Agonist+"\t"+r.AUC_Antagonist);
		}
		
	}
}
