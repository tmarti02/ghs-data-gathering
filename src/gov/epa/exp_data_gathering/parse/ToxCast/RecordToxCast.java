package gov.epa.exp_data_gathering.parse.ToxCast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;


import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.UnitConverter;


/**
* @author TMARTI02
* 
* see https://doi.org/10.26434/chemrxiv-2024-pwgq6-v2
* See "S4-Single concentration" tab of "ttr-supplemental-tables.xlsx"
* * 
*/
public class RecordToxCast {

	
	String DTXSID;
	String Chemical;
	String CASRN;
	String SMILES;
	String Library;
	String Max_conc;
	String Median_activity;
	String Tested_in_CR;
	String dataset;
	
	public static final String sourceName = "ToxCast";
	public static final String fileName="ttr-supplemental-tables.xlsx";
	static final UnitConverter uc = new UnitConverter("Data" + File.separator + "density.txt");
	
	
	public static Vector<JsonObject> parseRecordsFromExcel() {
		String sourceFolderPath = "data" + File.separator + "experimental";
		ExcelSourceReader esr=new ExcelSourceReader(fileName,sourceFolderPath,sourceName,"S4-Single concentration");
		Vector<JsonObject>records=esr.parseRecordsFromExcel(1);
		return records;
	}
	
	
	ExperimentalRecord toExperimentalRecord(String date) {

		ExperimentalRecord er=new ExperimentalRecord();
		er.date_accessed=date;
		er.source_name=sourceName;
		er.property_name=ExperimentalConstants.strTTR_ANSA;
		
		er.dsstox_substance_id=DTXSID;
		er.chemical_name=Chemical;
		er.casrn=CASRN;
		er.smiles=SMILES;
		
		er.experimental_parameters.put("library",Library);

		if(Max_conc!=null && !Max_conc.isBlank()) {
			Double dblMaxConc=Double.parseDouble(Max_conc);
			er.experimental_parameters.put("maximum_concentration",dblMaxConc);
		} 
		
		if(Median_activity!=null && !Median_activity.isBlank()) {
			er.property_value_point_estimate_original=Double.parseDouble(Median_activity);
		} else {
			er.property_value_point_estimate_original=-9999.0;
		}
		
		if(Tested_in_CR!=null) {
			er.experimental_parameters.put("tested_in_concentration_response",Tested_in_CR);
		}
		 
		
		er.experimental_parameters.put("dataset",dataset);
		
		er.property_value_units_original=ExperimentalConstants.str_dimensionless;
		
		uc.convertRecord(er);
		
		
		return er;
		
	}

}
