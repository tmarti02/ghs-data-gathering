package gov.epa.exp_data_gathering.parse.NICEATM;

import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

public class RecordNICEATM {
	public String Compound_name;
	public String CASRN;
	public String SMILES;
	public String Activity;
	public String Class;
	public String EC3_;
	public String MW;
	public String Chemical_Class;
	public static final String[] fieldNames = {"Compound_name","CASRN","SMILES","Activity","Class","EC3_","MW","Chemical_Class"};

	public static final String lastUpdated = "04/01/2021";
	public static final String sourceName = ExperimentalConstants.strSourceNICEATM;

	private static final String fileName = "NICEATM LLNA DB_original.xlsx";

	public static Vector<JsonObject> parseNICEATMRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(0);
		return records;
	}

}