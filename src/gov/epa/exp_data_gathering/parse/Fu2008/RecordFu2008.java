package gov.epa.exp_data_gathering.parse.Fu2008;

import java.util.LinkedHashMap;
import java.util.Vector;
import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.Fu2008.RecordFu2008;

/**
 * Stores data from Fu 2009, accessible at: doi.org/10.1897/08-233.1
 *
 */
public class RecordFu2008 {
	
	public String Name;
	public String logKow;
	public String pka;
	public String BCF;
	public String Reference;
	public String CompoundType;
	public String Note;
	public static final String[] fieldNames = {"Name", "logKow", "pka","BCF","Reference", "CompoundType", "Note"};
	
//	private static final String fileName = "Fu 2008.xlsx";
	public static String sourceName="Fu 2008";
	
	
	public static Vector<JsonObject> parseFu2008RecordsFromExcel(String fileName, String sourceName) {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		esr.headerRowNum=2;
		Vector<JsonObject> records = esr.parseRecordsFromExcel(0);
		return records;
	}
	
	public ExperimentalRecord toExperimentalRecords() {
		ExperimentalRecord er = new ExperimentalRecord();
		er.source_name="Fu 2008";
		er.property_name=ExperimentalConstants.strBCF;
		er.reference=this.Reference;
		er.chemical_name = this.Name;
		double value = Double.parseDouble(this.BCF);
		er.property_value_point_estimate_original=value;
		er.property_value_point_estimate_final=er.property_value_point_estimate_original;
		er.property_value_string= "BCF =" + value + " " + ExperimentalConstants.str_L_KG;
		er.property_value_units_original=ExperimentalConstants.str_L_KG;
		er.property_value_units_final=er.property_value_units_original;
		
		er.experimental_parameters=new LinkedHashMap<>();
		er.experimental_parameters.put("Log Kow", logKow);
		er.experimental_parameters.put("pka", pka);
		er.experimental_parameters.put("Compound Type", CompoundType);
		
		if(Note!=null) {
			er.note=Note;
		}
		
		return er;
	}
}
