package gov.epa.exp_data_gathering.parse.CoMPAIT;

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
* CoMPAIT Inhalation tox modeling project
* 
* * 
*/
public class RecordCoMPAIT {

	String DTXSID;
	String Inhalation_Db_Index;
	String CASRN;
	String PREFERRED_NAME;
	String Original_SMILES;
	String QSAR_READY_SMILES;
	String Four_hr_value_log_mgL;
	String Four_hr_value_log_ppm;

	public static final String sourceName = "CoMPAIT";
	public static final String fileName="LC50_Tr.xlsx";
	static final UnitConverter uc = new UnitConverter("Data" + File.separator + "density.txt");
	
	
	public static Vector<JsonObject> parseRecordsFromExcel() {
		String sourceFolderPath = "data" + File.separator + "experimental";
		ExcelSourceReader esr=new ExcelSourceReader(fileName,sourceFolderPath,sourceName,"LC50_Tr");
		Vector<JsonObject>records=esr.parseRecordsFromExcel(1);
		return records;
	}
	
	
	ExperimentalRecord toExperimentalRecord(String date,String units) {

		ExperimentalRecord er=new ExperimentalRecord();
		er.date_accessed=date;
		er.source_name=sourceName;
		er.property_name=ExperimentalConstants.strFOUR_HOUR_INHALATION_RAT_LC50;
		
		er.dsstox_substance_id=DTXSID;
		er.chemical_name=PREFERRED_NAME;
		er.casrn=CASRN;
		er.smiles=Original_SMILES;
		
		if(units.equals(ExperimentalConstants.str_log_mg_L)) {
			er.property_value_point_estimate_original=Double.parseDouble(Four_hr_value_log_mgL);
		} else if (units.equals(ExperimentalConstants.str_log_ppm)) {
			er.property_value_point_estimate_original=Double.parseDouble(Four_hr_value_log_ppm);
		}

		er.property_value_units_original=units;
		er.property_value_units_final=er.property_value_units_original;
		er.property_value_point_estimate_final=er.property_value_point_estimate_original;

		er.experimental_parameters.put("qsar_ready_smiles",QSAR_READY_SMILES);//store just in case...
//		
//		er.property_value_units_original=ExperimentalConstants.str_dimensionless;
//		uc.convertRecord(er);
		return er;
		
	}

}
