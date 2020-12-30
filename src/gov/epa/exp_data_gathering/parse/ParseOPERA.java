package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

public class ParseOPERA extends Parse {

	public ParseOPERA() {
		sourceName = "OPERA";
		this.init();
	}
	@Override
	protected void createRecords() {
		Vector<RecordOPERA> records = RecordOPERA.parseOperaSdf();
		writeOriginalRecordsToFile(records);
	}
	
	/**
	 * Reads the JSON file created by createRecords() and translates it to an ExperimentalRecords object
	 */
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordOPERA[] recordsOPERA = gson.fromJson(new FileReader(jsonFile), RecordOPERA[].class);
			
			for (int i = 0; i < recordsOPERA.length; i++) {
				RecordOPERA r = recordsOPERA[i];
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	/**
	 * populates experimentalrecord fields with data from the recordOPERA object.
	 * @param rs
	 * @param records
	 */
	private void addExperimentalRecords(RecordOPERA ro,ExperimentalRecords records) {
		// if (ro.property_name == ExperimentalConstants.strMeltingPoint) {
			ExperimentalRecord er = new ExperimentalRecord();
			er.chemical_name = ro.preferred_name;
			er.property_name = ro.property_name;
			er.property_value_point_estimate_original = getPropertyValueOriginal(ro);
			if (er.property_value_point_estimate_original != null) {
				er.property_value_string = er.property_value_point_estimate_original.toString();
			}
			er.property_value_units_original = ro.property_value_units_original;
			er.casrn = ro.CAS;
			er.note = "qc_level= " + ro.qc_level;
			er.date_accessed = java.time.LocalDate.now().toString();
			
			
			er.original_source_name = ro.Reference;
			if (!(ro.dsstox_compound_id == null))
			er.dsstox_substance_id = ro.dsstox_compound_id;
			
			// handles temperature recorded as 24|25 and absent temperatures
			getTemperatureCondition(er,ro.Temperature);
			
			finalizePropertyValues(er);
			// 	er.finalizePropertyValues();

			
			if (er.casrn.contains("NOCAS")) {
			er.keep = false;
			er.reason = "do we want this gone or no?";
			}
			else {
			er.keep = true;
			}
			records.add(er);
	}
	
	private static void finalizePropertyValues(ExperimentalRecord er) {
		if (er.property_name == ExperimentalConstants.strHenrysLawConstant) {
			er.property_value_units_final = ExperimentalConstants.str_atm_m3_mol;
			er.property_value_point_estimate_final = Math.pow(10,er.property_value_point_estimate_original);
		}
		else if (er.property_name == ExperimentalConstants.strBoilingPoint || er.property_name == ExperimentalConstants.strMeltingPoint) {
			er.property_value_point_estimate_final = er.property_value_point_estimate_original;
			er.property_value_units_final = er.property_value_units_original;
		}
		else if (er.property_name == ExperimentalConstants.strWaterSolubility) {
			er.property_value_point_estimate_original = Math.pow(10,er.property_value_point_estimate_original);
			er.property_value_units_final = ExperimentalConstants.str_M;
		}
	}
	
	private static void getTemperatureCondition(ExperimentalRecord er, String propertyValue) {
		if ((propertyValue != null && propertyValue.length() > 0) && (!propertyValue.contains("|")))
			er.temperature_C = Double.parseDouble(propertyValue);
		else if ((propertyValue != null && propertyValue.length() > 0) && propertyValue.contains("|")) {
			System.out.println(propertyValue);
			int vertLineIndex = propertyValue.indexOf("|");
			String temp1 = propertyValue.substring(0,vertLineIndex);
			String temp2 = propertyValue.substring(vertLineIndex + 1,propertyValue.length());
			double temp1double = Double.parseDouble(temp1);
			double temp2double = Double.parseDouble(temp2);
			er.temperature_C = temp1double + temp2double / 2;
			double temp3 = temp1double + temp2double / 2;
			System.out.println(temp3);
		}
	}
	
	private static Double getPropertyValueOriginal(RecordOPERA ro) {
		if (!(ro.BP == null))
			return Double.parseDouble(ro.BP);
		else if (!(ro.LogHL == null))
			return Double.parseDouble(ro.LogHL);
		else if (!(ro.LogP == null))
			return Double.parseDouble(ro.LogP);
		else if (!(ro.MP == null))
			return Double.parseDouble(ro.MP);
		else if (!(ro.LogVP == null))
			return Double.parseDouble(ro.LogVP);
		else if (!(ro.LogMolar == null))
			return Double.parseDouble(ro.LogMolar);
		else
			return (double)0;
	}



	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParseOPERA p = new ParseOPERA();
		p.mainFolder = p.mainFolder + File.separator + "General";
		p.jsonFolder= p.mainFolder;
		p.createFiles();
	}

}
