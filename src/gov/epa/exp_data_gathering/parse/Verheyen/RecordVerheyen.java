package gov.epa.exp_data_gathering.parse.Verheyen;

import java.util.Vector;
import com.google.gson.JsonObject;
import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;


public class RecordVerheyen {
	public String Name;
	public String CAS;
	public String Eye;
	public String Skin;
	public static final String[] fieldNames = {"Name","CAS","Eye","Skin"};

	public static final String lastUpdated = "04/29/2021";
	public static final String sourceName = "Verheyen"; // TODO Consider creating ExperimentalConstants.strSourceVerheyen instead.

	private static final String fileName = "1-s2.0-S037842741633301X-mmc1_No_header.xlsx";

	public static Vector<JsonObject> parseVerheyenRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(0); // TODO Chemical name index guessed from header. Is this accurate?
		return records;
	}
}