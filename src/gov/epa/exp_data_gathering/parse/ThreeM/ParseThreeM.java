package gov.epa.exp_data_gathering.parse.ThreeM;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.exec.util.StringUtils;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.UnitConverter;

public class ParseThreeM extends Parse {

	public ParseThreeM() {
		sourceName = ExperimentalConstants.strSource3M;
		this.init();
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordThreeM.parseThreeMRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			List<RecordThreeM> recordsThreeM = new ArrayList<RecordThreeM>();
			RecordThreeM[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordThreeM[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsThreeM.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordThreeM[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsThreeM.add(tempRecords[i]);
					}
				}
			}

			Iterator<RecordThreeM> it = recordsThreeM.iterator();
			while (it.hasNext()) {
				RecordThreeM r = it.next();
				addExperimentalRecord(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}
	
	private void addExperimentalRecord(RecordThreeM r3m, ExperimentalRecords recordsExperimental) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
		Date date = new Date();  
		String strDate=formatter.format(date);
		String dayOnly = strDate.substring(0,strDate.indexOf(" "));

		
		
		ExperimentalRecord er = new ExperimentalRecord();
		er.chemical_name = r3m.test_substance_name;
		er.casrn = r3m.CASRN;
		er.synonyms = r3m.other_test_substance_name;
		er.synonyms = r3m.other_test_substance_name;
		er.date_accessed = dayOnly;
		
		
		if (r3m.property!=null && !r3m.property.isBlank()) {
			if (r3m.property.equals("Vapour pressure")) {
				er.property_name = ExperimentalConstants.strVaporPressure;
			} else if (r3m.property.toLowerCase().equals("normal boiling point")) {
				er.property_name = ExperimentalConstants.strBoilingPoint;
				er.pressure_mmHg = "760";
			} else if (r3m.property.equals("Freezing Temperature")) {
				er.property_name = ExperimentalConstants.strMeltingPoint;
			} else if (r3m.property.toLowerCase().contains("partition") || r3m.property.toLowerCase().contains("octanol")) {
				er.property_name = ExperimentalConstants.strLogKow;
			} else if (r3m.property.toLowerCase().equals("k_oc")) {
				er.property_name = ExperimentalConstants.strLogKOC;
			} else if (r3m.property.toLowerCase().equals("bcf")) {
				er.property_name = ExperimentalConstants.strLogBCF;
			} else if (r3m.property.toLowerCase().equals("solubility in water")) {
				er.property_name = ExperimentalConstants.strWaterSolubility;
			} else if (r3m.property.toLowerCase().equals("boiling temperature")) {
				er.property_name = ExperimentalConstants.strBoilingPoint;
			} else if (r3m.property.toLowerCase().equals("freezing temperature")) {
				er.property_name = ExperimentalConstants.strMeltingPoint;
			} else if (r3m.property.toLowerCase().contains("dissociation constant")) {
				er.property_name = ExperimentalConstants.str_pKA;
			} else if (r3m.property.toLowerCase().equals("Pka")) {
				er.property_name = ExperimentalConstants.str_pKA;
			}
		
			
			
			else {
				er.property_name =  r3m.property.substring(0,1).toUpperCase() + r3m.property.substring(1).toLowerCase();
			}
		}
		
		
			
		if (r3m.property_value_units!=null) {
			if (r3m.property_value_units.equals("degrees C")) {
				er.property_value_units_original = ExperimentalConstants.str_C;
			} else if (r3m.property_value_units.equals("Pa")) {
				er.property_value_units_original = ExperimentalConstants.str_pa;
			} else if (r3m.property_value_units.equals("not determined")) {
				er.property_value_units_original = "";
			} else { 
				er.property_value_units_original = r3m.property_value_units.replace("mm Hg", "mmHg");
				if (r3m.property_value_units != null && r3m.property_value_units.equals("mm"))
					er.property_value_units_original = ExperimentalConstants.str_mmHg;
		}
		}
		
		
		if (r3m.property_value!=null && !r3m.property_value.isBlank() && 
				!(r3m.property_value.equals("not determined") || r3m.property_value.equals("ff 3.7") || r3m.property_value.equals("ff 349") || r3m.property_value.contains("+"))) {
			er.property_value_point_estimate_original = Double.parseDouble(r3m.property_value.replace(",", ""));
			er.property_value_string = "Value: " + r3m.property_value;
		} else if (r3m.property_value_min!=null && !r3m.property_value_min.isBlank()) {
			er.property_value_min_original = Double.parseDouble(r3m.property_value_min);
			if (r3m.property_value_max!=null && !r3m.property_value_max.isBlank()) {
				er.property_value_max_original = Double.parseDouble(r3m.property_value_max);
				er.property_value_string = "Value: " + r3m.property_value_min + "-" + r3m.property_value_max;
			}
			else {
				er.property_value_string = "Value: >" + r3m.property_value_min;
				er.property_value_numeric_qualifier = ">";
			}	
		} else if (r3m.property_value_max!=null && !r3m.property_value_max.isBlank()) {
			er.property_value_max_original = Double.parseDouble(r3m.property_value_max);
			er.property_value_string = "Value: <" + r3m.property_value_max;
			er.property_value_numeric_qualifier = "<";
		}
		
		if (r3m.property_value_method!=null && !r3m.property_value_method.isBlank()) {
			er.measurement_method = r3m.property_value_method;
		}
		
		if (er.property_name != null) {
			er.keep = true;
			uc.convertRecord(er);
		}
		
		if (r3m.property_measurement_conditions != null) {
			if (r3m.property_measurement_conditions.contains("-")) {
				getTemperatureRange(er, r3m.property_measurement_conditions);
			}
			else {
				ParseUtilities.getTemperatureCondition(er, r3m.property_measurement_conditions.replace("degrees", "").replace("+", ""));
				if (r3m.property_measurement_conditions.contains("+-1"))
					er.temperature_C = 20.0;
			}
		ParseUtilities.getPressureCondition(er, r3m.property_measurement_conditions, "r3m");
		}
		
		// throwing out records that aren't physicochemical properties
		if (r3m.reason_not_extracted!=null && !r3m.reason_not_extracted.isBlank()) {
			er.keep = false;
			er.reason = "not extracted";
			
			} else if (er.property_name == null) {
				er.keep = false;
				er.reason = "empty property";
			} else if (er.property_name != null && (er.property_name.toLowerCase().contains("so2")
					|| er.property_name.toLowerCase().contains("sof2")
					|| er.property_name.toLowerCase().contains("kraft")
					|| er.property_name.toLowerCase().contains("hydrolysis")
					|| er.property_name.toLowerCase().contains("acetone")
					|| er.property_name.toLowerCase().contains("airborn")
					|| er.property_name.toLowerCase().contains("photolysis")
					|| er.property_name.toLowerCase().contains("activated")
					|| er.property_name.toLowerCase().contains("summary")
					|| er.property_name.toLowerCase().contains("soil")
					|| er.property_name.toLowerCase().contains("_c")
					|| er.property_name.toLowerCase().contains("molar mass")
					|| er.property_name.toLowerCase().contains("molecular weight")
					|| er.property_name.toLowerCase().contains("surface tension")
					|| er.property_name.toLowerCase().contains("enthalpy")
					|| er.property_name.toLowerCase().contains("methanol")
					|| er.property_name.toLowerCase().contains("acidity function")
					|| er.property_name.toLowerCase().contains("micelle")
					|| er.property_name.toLowerCase().contains("acentric")
					|| er.property_name.toLowerCase().contains("ultraviolet")
					|| er.property_name.toLowerCase().contains("Log k_aw")
					|| er.property_name.toLowerCase().contains("explosive")
					|| er.property_name.toLowerCase().equals("ph")
					|| er.property_name.toLowerCase().contains("ld")
					|| er.property_name.toLowerCase().contains("cod")
					|| er.property_name.toLowerCase().contains("relative density")
					|| er.property_name.toLowerCase().contains("ethanol")
					|| er.property_name.toLowerCase().contains("ld")
					|| er.property_name.toLowerCase().equals("log k_aw")


		)) {
				er.keep = false;
				er.reason = "not a physicochemical property";
				
			} else if (er.property_name.toLowerCase().equals("solubility")){
				er.keep = false;
				er.reason = "calculation/prediction";
			}
		
		if (r3m.property_value.equals("") && r3m.property_value_max.equals("") && r3m.property_value_min.equals("")) {
			er.keep = false;
			er.reason = "no data";
		} else if ((er.pressure_mmHg != null) && (Double.parseDouble(er.pressure_mmHg) < 736.0)) {
			er.keep = false;
			er.reason = "not atmospheric pressure";
		}
		if (er.property_name != null && (er.property_name == ExperimentalConstants.strLogKow || er.property_name.equals("K_ow"))) {
			er.property_name = ExperimentalConstants.strLogKow;
			if (er.property_value_point_estimate_final != null) {
				er.property_value_point_estimate_final = Math.log10(er.property_value_point_estimate_final);	
			} else if (er.property_value_min_final != null) {
				er.property_value_min_final = Math.log10(er.property_value_min_final);
			}
		} else if (er.property_name != null && (er.property_name.equals(ExperimentalConstants.strLogKOC))){
			if (er.property_value_max_final != null && er.property_value_min_final != null) {
				er.property_value_min_final = Math.log10(er.property_value_min_final);
				er.property_value_max_final = Math.log10(er.property_value_min_final);
			}	else if (er.property_value_point_estimate_final != null) { er.property_value_point_estimate_final = Math.log10(er.property_value_point_estimate_final); }
			else {er.keep = false; er.reason = "no data";}
		
			

		}

		if (er.property_name != null && er.casrn != null && er.property_name == ExperimentalConstants.strLogKow && er.casrn.equals("2795-39-3")) {
			er.keep = false;
			er.reason = "unspecified or out of bounds logP";
		} else if (er.chemical_name.equals("Chemical name redacted")) {
			er.keep = false;
			er.reason = "no identifying information";
		} else if (r3m.comments.contains("Testing was not conducted")) {
			er.keep = false;
			er.reason = "testing was not conducted";
		} else if (r3m.comments.contains("very likely reduced pressure")) {
			er.keep = false;
			er.reason = "very likely reduced pressure";
		} else if (er.property_name !=null && er.property_name.equals(ExperimentalConstants.strWaterSolubility) && !(er.property_value_units_final.equals(ExperimentalConstants.str_g_L))) {
			er.keep = false;
			er.reason = "not convertible or redundant";
		} else if (er.property_value_units_final != null && er.property_value_units_final.equals("torr")) {
			er.keep=false;
			er.reason = "bad records";
		}
		
		if (er.property_name != null && er.property_name.toLowerCase().equals("log k_ow")) {
			er.property_name = ExperimentalConstants.strLogKow;
		} 
		// else if (er.property_name.toLowerCase().equals(""))
		
		recordsExperimental.add(er);

	}
	
	private static void getTemperatureRange(ExperimentalRecord er, String propertyValue) throws IllegalStateException {
		try {
		Matcher tempRangeMatcher = Pattern.compile("(\\s)?([0-9]*\\.?[0-9]+)(\\-)?([0-9]*\\.?[0-9]+)").matcher(propertyValue);
		if (tempRangeMatcher.find()) {
		String lowertemp = tempRangeMatcher.group(2);
		String rangeCheck = tempRangeMatcher.group(3);
		String highertemp = tempRangeMatcher.group(4);
		if (!(rangeCheck == null)) {
			double min = Double.parseDouble(lowertemp);
			double max = Double.parseDouble(highertemp);
			er.temperature_C = min+max / 2;
		}
		}
		
	}	 catch (IllegalStateException e) {
		e.printStackTrace();
	}
	}

	
	public static void main(String[] args) {
		ParseThreeM p = new ParseThreeM();
		p.createFiles();
		// String s = "55-60 Degrees C";
		// getTemperatureRange(s);
	}
}