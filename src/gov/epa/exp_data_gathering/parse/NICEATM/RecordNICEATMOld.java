package gov.epa.exp_data_gathering.parse.NICEATM;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

public class RecordNICEATMOld {
	public String Compound_name;
	public String CASRN;
	public String SMILES;
	public String Activity;
	public String Class;
	public String EC3;
	public String MW;
	public String Chemical_Class;
	public static final String[] fieldNames = {"Compound_name","CASRN","SMILES","Activity","Class","EC3","MW","Chemical_Class"};

	public static final String lastUpdated = "02/08/2021";
	public static final String sourceName = ExperimentalConstants.strSourceNICEATM;

//	private static final String fileName = "NICEATM LLNA DB_original.xlsx";
	private static final String fileName = "niceatm-llnadatabase-23dec2013.xlsx";
		

	public static Vector<JsonObject> parseNICEATMRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(0);
		return records;
	}
	
	
	public static void main(String[] args) {

		ExcelSourceReader esr=new ExcelSourceReader(fileName, "data/experimental", sourceName, "LLNA Data");
		esr.headerRowNum=2;
		
		esr.createClassTemplateFiles(null);

	}
	
	
}