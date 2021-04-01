package gov.epa.exp_data_gathering.parse.Bagley;

import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

public class RecordBagley {
	public String Chemical;
	public String Purity;
	public String Number_of_animals_tested;
	public String PII;
	public static final String[] fieldNames = {"Chemical","Purity","Number_of_animals_tested","PII"};

	public static final String lastUpdated = "04/01/2021";
	public static final String sourceName = "Bagley"; // TODO Consider creating ExperimentalConstants.strSourceBagley instead.

	private static final String fileName = "Bagleyclean.xlsx";

	public static Vector<JsonObject> parseBagleyRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(0); // TODO Chemical name index guessed from header. Is this accurate?
		return records;
	}
}