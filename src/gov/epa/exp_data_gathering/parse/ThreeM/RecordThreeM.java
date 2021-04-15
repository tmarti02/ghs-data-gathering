package gov.epa.exp_data_gathering.parse.ThreeM;

import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.exp_data_gathering.parse.ExcelSourceReader;


public class RecordThreeM {
	public String Name;
	public String reason_not_extracted;
	public String test_substance_name;
	public String other_test_substance_name;
	public String CASRN;
	public String property;
	public String property_value;
	public String property_value_min;
	public String property_value_max;
	public String property_value_units;
	public String property_measurement_conditions;
	public String comments;
	public String property_value_method;
	public static final String[] fieldNames = {"Name","reason_not_extracted","test_substance_name","other_test_substance_name","CASRN","property","property_value","property_value_min","property_value_max","property_value_units","property_measurement_conditions","comments","property_value_method"};

	public static final String lastUpdated = "04/08/2021";
	public static final String sourceName = "ThreeM";

	private static final String fileName = "physchem_brief_extraction_16mar2021_CR_edits.xlsx";

	public static Vector<JsonObject> parseThreeMRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(0);
		return records;
	}
}
