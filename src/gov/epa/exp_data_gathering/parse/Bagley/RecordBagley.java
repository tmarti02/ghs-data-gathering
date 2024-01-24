package gov.epa.exp_data_gathering.parse.Bagley;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import com.google.gson.JsonObject;

import gov.epa.exp_data_gathering.parse.ExcelSourceReader;
import gov.epa.ghs_data_gathering.GetData.Scifinder;
import gov.epa.ghs_data_gathering.GetData.ScifinderRecord;
import gov.epa.ghs_data_gathering.Utilities.ExcelUtilities;

public class RecordBagley {
	public String Chemical;
	public String Purity;
	public String Number_of_animals_tested;
	public String PII;
	public static final String[] fieldNames = {"Chemical","Purity","Number_of_animals_tested","PII"};

	public static final String lastUpdated = "04/19/2021";
	public static final String sourceName = "Bagley"; // TODO Consider creating ExperimentalConstants.strSourceBagley instead.

	private static final String fileName = "Bagley.xlsx";

	public static Vector<JsonObject> parseBagleyRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(0); // TODO Chemical name index guessed from header. Is this accurate?
		return records;
	}
}