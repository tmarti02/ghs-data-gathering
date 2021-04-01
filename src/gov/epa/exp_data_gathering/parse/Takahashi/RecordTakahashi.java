package gov.epa.exp_data_gathering.parse.Takahashi;

import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

public class RecordTakahashi {
	String testMaterial;
	String casNo;
	String draizeScore100;
	String draizeScore10;
	String draizeRank;
	String chemicalClass;
	String supplier;
	String solventUsed;
	public static final String[] fieldNames = { "testMaterial","casNo","draizeScore100","draizeScore10","draizeRank","chemicalClass","supplier","solventUsed" };
	
	public static final String lastUpdated = "03/26/2021";
	public static final String sourceName = ExperimentalConstants.strSourceTakahashi;
	
	private static final String fileName = "Takahashi2010_SI_Cleaned.xlsx";
	
	public static Vector<JsonObject> parseTakahashiRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(0);
		return records;
	}
}
