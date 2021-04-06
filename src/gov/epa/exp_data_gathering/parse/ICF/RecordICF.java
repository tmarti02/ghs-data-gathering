package gov.epa.exp_data_gathering.parse.ICF;

import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

public class RecordICF {
	public String physchem_id;
	public String physchem_data_QCed_record_source_id;
	public String Record_source_level;
	public String Record_source_primary;
	public String casrn;
	public String chemical_name;
	public String synonyms;
	public String property_name;
	public String property_value_min;
	public String property_value_max;
	public String property_value_point_estimate;
	public String property_value_units;
	public String Uncertainty;
	public String Original_Property_value_min;
	public String Original_property_value_max;
	public String Original_property_value_point_estimate;
	public String Original_property_value_unit;
	public String Original_Uncertainty;
	public String Original_Temp;
	public String temperature_C;
	public String pH;
	public String pressure_atm;
	public String pressure_kPa;
	public String measurement_method;
	public String note;
	public String record_source_record_source_id;
	public String url;
	public String long_ref;
	public static final String[] fieldNames = {"physchem_id","physchem_data_QCed_record_source_id","Record_source_level","Record_source_primary","casrn","chemical_name","synonyms","property_name","property_value_min","property_value_max","property_value_point_estimate","property_value_units","Uncertainty","Original_Property_value_min","Original_property_value_max","Original_property_value_point_estimate","Original_property_value_unit","Original_Uncertainty","Original_Temp","temperature_C","pH","pressure_atm","pressure_kPa","measurement_method","note","record_source_record_source_id","url","long_ref"};

	public static final String lastUpdated = "04/06/2021";
	public static final String sourceName = ExperimentalConstants.strSourceICF;

	private static final String fileName = "physchem_data_QCed_w_url_and_longref.xlsx";

	public static Vector<JsonObject> parseICFRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(5); // TODO Chemical name index guessed from header. Is this accurate?
		return records;
	}
}
