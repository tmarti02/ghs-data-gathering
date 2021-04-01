package gov.epa.exp_data_gathering.parse.Lebrun;

import java.util.HashMap;
import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

public class RecordLebrun {
	String chemicalName;
	String casrn;
	String inVivoGHS;
	String inVivoEPA;
	String functionalGroups;
	String bcopLLBO;
	String bcopOpKit;
	String epi;
	String ice;
	String oi;
	String os;
	String ste;
	public static final String[] fieldNames = { "chemicalName","casrn","inVivoGHS","inVivoEPA","functionalGroups","bcopLLBO","bcopOpKit",
			"epi","ice","oi","os","ste" };
	
	public static final String lastUpdated = "03/25/2021";
	public static final String sourceName = ExperimentalConstants.strSourceLebrun;
	
	private static final String fileName = "Lebrun2021_SI_Cleaned.xlsx";
	
	public static Vector<JsonObject> parseLebrunRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		HashMap<Integer,String> hm = esr.generateDefaultMap(RecordLebrun.fieldNames, 1);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(hm, 1);
		return records;
	}
}
