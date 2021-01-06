package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
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
		//TODO make the pka experimentalrecords rely on getLogProperty like the logP ones do.
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
		Date date = new Date();  
		String strDate=formatter.format(date);
		String dayOnly = strDate.substring(0,strDate.indexOf(" "));

		if (ro.property_name.equals(ExperimentalConstants.str_pKA)) {
			ExperimentalRecord er_a = new ExperimentalRecord();
			er_a.chemical_name = ro.Substance_Name;
			er_a.source_name = ExperimentalConstants.strSourceOPERA;
			er_a.smiles = ro.Original_SMILES;
			er_a.property_value_string = "pkaa=" + ro.pKa_a + "|"+ "pkab=" + ro.pKa_b;
			er_a.casrn = ro.Substance_CASRN;
			er_a.note = "qc_level= " + ro.DSSTox_QC_Level;
			er_a.dsstox_substance_id = ro.DSSTox_Substance_Id;
			er_a.date_accessed = dayOnly;

			if(!(ro.pKa_a.equals("NaN"))) {
				er_a.property_value_point_estimate_final=Double.parseDouble(ro.pKa_a);
				er_a.property_name = ExperimentalConstants.str_pKAa;
				// ParseUtilities.getLogProperty(er_a,er_a.property_value_point_estimate_final.toString()); // log quantity
				er_a.keep = true;
				records.add(er_a);

			}
			if ((!(ro.pKa_b.equals("NaN")))) {
				ExperimentalRecord er_b = er_a; // makes the second experimental record for bases from the one RecordOPERA record.
				er_b.property_value_point_estimate_final=Double.parseDouble(ro.pKa_b);
				er_a.property_name = ExperimentalConstants.str_pKAb;
				// ParseUtilities.getLogProperty(er_b,er_b.property_value_point_estimate_original.toString()); // log quantity
				er_b.keep = true;
				records.add(er_b);
			}
			
		}
		if (!(ro.property_name.equals(ExperimentalConstants.str_pKA))) {
			ExperimentalRecord er = new ExperimentalRecord();
			er.chemical_name = ro.preferred_name;
			er.source_name = ExperimentalConstants.strSourceOPERA;
			er.property_name = ro.property_name;
			er.property_value_point_estimate_original = getPropertyValueOriginal(ro);
			if (er.property_value_point_estimate_original != null) {
				er.property_value_string = er.property_value_point_estimate_original.toString();
			}
			if (ro.property_name == ExperimentalConstants.strLogKow) {
				ParseUtilities.getLogProperty(er,er.property_value_string);
			}
			er.property_value_units_original = ro.property_value_units_original;
			er.casrn = ro.CAS;
			er.smiles=ro.Original_SMILES;
			er.note = "qc_level= " + ro.qc_level;
			er.date_accessed = dayOnly;
			RecordFinalizer.finalizeRecord(er);

			er.original_source_name = ro.Reference;
			if (!(ro.dsstox_compound_id == null))
			er.dsstox_substance_id = ro.dsstox_compound_id;
			
			// handles temperature recorded as 24|25 and absent temperatures
			getTemperatureCondition(er,ro.Temperature);
			
			// finalizePropertyValues(er);
			// er.finalizePropertyValues();
			RecordFinalizer.finalizeRecord(er);
			
			
			er.keep = true;
			
			records.add(er);
		}
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
			int vertLineIndex = propertyValue.indexOf("|");
			String temp1 = propertyValue.substring(0,vertLineIndex);
			String temp2 = propertyValue.substring(vertLineIndex + 1,propertyValue.length());
			double temp1double = Double.parseDouble(temp1);
			double temp2double = Double.parseDouble(temp2);
			er.temperature_C = (temp1double + temp2double)/ 2;
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
		return null;
	}



	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParseOPERA p = new ParseOPERA();
		p.mainFolder = p.mainFolder + File.separator + "General";
		p.jsonFolder= p.mainFolder;
		p.createFiles();
	}

}
