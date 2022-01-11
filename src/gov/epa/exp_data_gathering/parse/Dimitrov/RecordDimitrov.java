package gov.epa.exp_data_gathering.parse.Dimitrov;

import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

public class RecordDimitrov {
	public String ID;
	public String Status;
	public String CAS_RN;
	public String Name;
	public String Exp_Log_BCF;
	public String Model_1;
	public String Model_2;
	public String Model_3;
	public String Model_4;
	public String Model_5;
	public String Model_6;
	public String Hybrid_Model;
	public static final String[] fieldNames = {"ID","Status","CAS_RN","Name","Exp_Log_BCF","Model_1","Model_2","Model_3","Model_4","Model_5","Model_6","Hybrid_Model"};

	public static final String lastUpdated = "01/11/2022";
	public static final String sourceName = "Dimitrov"; // TODO Consider creating ExperimentalConstants.strSourceDimitrov instead.

	private static final String fileName = "Edited_Copy_Dimitrov2005.xlsx";

	public static Vector<JsonObject> parseDimitrovRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(3); // TODO Chemical name index guessed from header. Is this accurate?
		return records;
	}
}