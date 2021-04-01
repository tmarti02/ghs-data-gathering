package gov.epa.exp_data_gathering.parse.CFSAN;

import java.util.HashMap;
import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

public class RecordCFSAN {
	String srNo;
	String chemName;
	String casNr;
	String activity;
	public static final String[] fieldNames = { "srNo", "chemName", "casNr", "activity" };
	
	public static final String lastUpdated = "03/23/2021";
	public static final String sourceName = ExperimentalConstants.strSourceCFSAN;
	
	private static final String fileName = "Verma2015_SI_XLSX.xlsx";
	
	public static Vector<JsonObject> parseCFSANRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		HashMap<Integer,String> hm = esr.generateDefaultMap(fieldNames, 0);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(hm, 1);
		return records;
	}
}
