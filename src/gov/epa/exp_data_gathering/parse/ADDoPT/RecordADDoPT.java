package gov.epa.exp_data_gathering.parse.ADDoPT;

import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

/**
 * Stores data from ADDoPT, accessible at: https://onlinelibrary.wiley.com/doi/epdf/10.1002/jcc.24424 (supplementary info table 1)
 * @author GSINCL01
 *
 */
public class RecordADDoPT {
	public String CAS_number;
	public String T;
	public String Observed_solubility_lg_mol_L;
	public static final String[] fieldNames = {"CAS_number","T","Observed_solubility_lg_mol_L"};

	public static final String lastUpdated = "12/14/2020";
	public static final String sourceName = ExperimentalConstants.strSourceADDoPT;

	private static final String fileName = "Klimenko_SuppInfo_Table1.xlsx";

	public static Vector<JsonObject> parseADDoPTRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(-1);
		return records;
	}
}