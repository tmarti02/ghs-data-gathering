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
		sourceName = RecordThreeM.sourceName;
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
	
	private static void getConditions(ExperimentalRecord er, String propertyValue) throws IllegalStateException {
		try {
		Matcher tempRangeMatcher = Pattern.compile("(\\s)?([0-9]*\\.?[0-9]+)(\\-)?([0-9]*\\.?[0-9]+)").matcher(propertyValue);
		if (tempRangeMatcher.find()) {
		String lowertemp = tempRangeMatcher.group(2);
		String rangeCheck = tempRangeMatcher.group(3);
		String highertemp = tempRangeMatcher.group(4);
		if (!(rangeCheck == null)) {
			double min = Double.parseDouble(lowertemp);
			double max = Double.parseDouble(highertemp);
			er.temperature_C = (min+max) / 2;
		}
		}
		
	}	 catch (IllegalStateException e) {
		e.printStackTrace();
	}
	}
	


	private void addExperimentalRecord(RecordThreeM r3m, ExperimentalRecords recordsExperimental) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
		Date date = new Date();  
		String strDate=formatter.format(date);
		String dayOnly = strDate.substring(0,strDate.indexOf(" "));
		ExperimentalRecord er = new ExperimentalRecord();
		
		er.source_name=sourceName;
		er.chemical_name = r3m.test_substance_name;
		er.casrn = r3m.CASRN;
		er.synonyms = r3m.other_test_substance_name;
		er.synonyms = r3m.other_test_substance_name;
		er.source_name=sourceName;
		er.date_accessed = dayOnly;
		
		boolean OriginallyKOW = false;
		
		// assigns the property
		if (r3m.property!=null && !r3m.property.isBlank()) {
			if (r3m.property.equals("Vapour pressure")) {
				er.property_name = ExperimentalConstants.strVaporPressure;
			} else if (r3m.property.toLowerCase().equals("normal boiling point")) {
				er.property_name = ExperimentalConstants.strBoilingPoint;
				er.pressure_mmHg = "760";
			} else if (r3m.property.equals("Freezing Temperature")) {
				er.property_name = ExperimentalConstants.strMeltingPoint;
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
			} else if (r3m.property.toLowerCase().equals("pka")) {
				er.property_name = ExperimentalConstants.str_pKA;
			} else if (r3m.property.toLowerCase().equals("log k_ow")) {
				er.property_name = ExperimentalConstants.strLogKow;
			} else if (r3m.property.toLowerCase().equals("LogBCF")) {
				er.property_name = ExperimentalConstants.strLogBCF;
			} else {
				er.property_name =  r3m.property.substring(0,1).toUpperCase() + r3m.property.substring(1).toLowerCase();

		}
		}

		// removes extraneous properties
		if (r3m.reason_not_extracted!=null && !r3m.reason_not_extracted.isBlank()) {
			er.keep = false;
			er.reason = "not extracted";
			
			} else if (r3m.property == null) {
				er.keep = false;
				er.reason = "empty property";
			} else if (r3m.property != null && (r3m.property.toLowerCase().contains("so2")
					|| r3m.property.toLowerCase().contains("sof2")
					|| r3m.property.toLowerCase().contains("kraft")
					|| r3m.property.toLowerCase().contains("hydrolysis")
					|| r3m.property.toLowerCase().contains("acetone")
					|| r3m.property.toLowerCase().contains("airborn")
					|| r3m.property.toLowerCase().contains("photolysis")
					|| r3m.property.toLowerCase().contains("activated")
					|| r3m.property.toLowerCase().contains("summary")
					|| r3m.property.toLowerCase().contains("soil")
					|| r3m.property.toLowerCase().contains("_c")
					|| r3m.property.toLowerCase().contains("molar mass")
					|| r3m.property.toLowerCase().contains("molecular weight")
					|| r3m.property.toLowerCase().contains("surface tension")
					|| r3m.property.toLowerCase().contains("enthalpy")
					|| r3m.property.toLowerCase().contains("methanol")
					|| r3m.property.toLowerCase().contains("acidity function")
					|| r3m.property.toLowerCase().contains("micelle")
					|| r3m.property.toLowerCase().contains("acentric")
					|| r3m.property.toLowerCase().contains("ultraviolet")
					|| r3m.property.toLowerCase().contains("Log k_aw")
					|| r3m.property.toLowerCase().contains("explosive")
					|| r3m.property.toLowerCase().equals("ph")
					|| r3m.property.toLowerCase().contains("ld")
					|| r3m.property.toLowerCase().contains("cod")
					|| r3m.property.toLowerCase().contains("relative density")
					|| r3m.property.toLowerCase().contains("ethanol")
					|| r3m.property.toLowerCase().contains("ld")
					|| r3m.property.toLowerCase().equals("log k_aw")
					|| r3m.property.toLowerCase().equals("biosorption partition coefficient")
					|| r3m.property.equals("air/water partition coefficient")
					|| r3m.property.equals("solubility in octanol")
					|| r3m.property.equals("solubility in n-octanol")
					|| r3m.property.toLowerCase().contains("acentric")
					|| r3m.property.toLowerCase().contains("c8")
					|| r3m.property.isBlank()

		)) {
				er.keep = false;
				er.reason = "not a physicochemical property or we're not interested";
				
			}
		// TODO figure out a way to assign the + properly
		if (r3m.property_value_method!=null && !r3m.property_value_method.isBlank()) {
			er.measurement_method = r3m.property_value_method;
		}

		
		if (r3m.property_value!=null && !r3m.property_value.isBlank() && 
				!(r3m.property_value.equals("not determined") || r3m.property_value.equals("ff 3.7") || r3m.property_value.equals("ff 349") || r3m.property_value.contains("+"))) {
			er.property_value_point_estimate_original = Double.parseDouble(r3m.property_value.replace(",", ""));
			
			er.property_value_string = "Value: " + r3m.property_value;
		} else if (r3m.property_value_min!=null && !(r3m.property_value_min.isBlank())) {
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
		
		// handles the inconsistency with the KOW data

		if (r3m.property!=null && !r3m.property.isBlank() && (r3m.property.toLowerCase().equals("k_ow")) 
				|| r3m.property.equals("n-octanol/water distribution")
				|| r3m.property.equals("distribution coefficient between n-octanol and water")
				|| r3m.property.equals("octanol/water distribution coefficient")
				|| r3m.property.contains("partition coefficient")								
				|| r3m.property.equals("distribution coefficient in n-octanol/water")) {

			er.property_name = ExperimentalConstants.strLogKow;
			if ((r3m.property_value != null) && !(r3m.property_value.equals("not determined")) && !(r3m.property_value.trim().isEmpty())) {
				er.property_value_point_estimate_final = Math.log10(Double.parseDouble(r3m.property_value.replaceAll(",","")));
			} 
			else if ((!(r3m.property_value_min.trim().isEmpty())) && (!(r3m.property_value_max.trim().isEmpty()))) {
			// else if ((r3m.property_value_min != null || !(r3m.property_value_min.trim().isEmpty())) && (r3m.property_value_max != null || !(r3m.property_value_max.trim().isEmpty()))) {
				System.out.println(r3m.property_value_min);
				er.property_value_min_final = Math.log10(Double.parseDouble(r3m.property_value_min));
				er.property_value_max_final = Math.log10(Double.parseDouble(r3m.property_value_max));
				
			}
			
			 else if (r3m.property_value_min != null && !(r3m.property_value_min.trim().isEmpty())) {
				er.property_value_min_final = Math.log10(Double.parseDouble(r3m.property_value_min));
			} else if (r3m.property_value_max != null && !(r3m.property_value_max.trim().isEmpty())) {
				er.property_value_max_final = Math.log10(Double.parseDouble(r3m.property_value_max));
			} else if (r3m.property_value.equals("not determined")) {
				er.keep = false;
				er.reason = "value not determined";
			}
			er.updateNote("originally KOW");
			OriginallyKOW = true;
			
			if (r3m.CR_Notes.toLowerCase().contains("hplc interpolation")) {
			er.updateNote("HPLC interpolation");	
			}
		}

		// gets conditions in terms of temperature (C), pressure (mmhg), or pH
		
		if (r3m.property_measurement_conditions != null) {
			ParseUtilities.getPressureCondition(er, r3m.property_measurement_conditions, "r3m");

		
			if (r3m.property_measurement_conditions.contains("-")) {
				getConditions(er, r3m.property_measurement_conditions);
			}
			else {
				ParseUtilities.getTemperatureCondition(er, r3m.property_measurement_conditions.replace("degrees", "").replace("+", ""));
		}
		}
		
		// gets the units
		
		if (r3m.property_value_units!=null) {
			if (r3m.property_value_units.equals("degrees C") || (r3m.property_value_units.equals("C"))) {
				er.property_value_units_original = ExperimentalConstants.str_C;
			} else if (r3m.property_value_units.equals("Pa")) {
				er.property_value_units_original = ExperimentalConstants.str_pa;
			} else if (r3m.property_value_units.equals("kPa")) {
				er.property_value_units_original = ExperimentalConstants.str_kpa;
			} else if (r3m.property_value_units.equals("not determined")) {
				er.property_value_units_original = "";
			} else if (r3m.property_value_units.contains("mm") || r3m.property_value_units.toLowerCase().contains("torr")){ 
				er.property_value_units_original = ExperimentalConstants.str_mmHg;
			} else if (r3m.property_value_units.equals("mg ai/L") || r3m.property_value_units.toLowerCase().equals("mg/l")) {
				er.property_value_units_original = ExperimentalConstants.str_mg_L;
			} else if (r3m.property_value_units.toLowerCase().equals("g/l")) {
				er.property_value_units_original = ExperimentalConstants.str_g_L;
			} else if (r3m.property_value_units.contains("%")) {
				er.keep = false;
				er.reason = "ambiguous units, proper units available in other records";
			} else if (r3m.property_value_units.toLowerCase().contains("ug/ml") || r3m.property_value_units.toLowerCase().contains("\u00B5g/ml")) {
				er.property_value_units_original = ExperimentalConstants.str_ug_mL;
			} else if (r3m.property_value_units.toLowerCase().contains("ug/l") || r3m.property_value_units.toLowerCase().contains("\u00B5g/l") || r3m.property_value_units.toLowerCase().contains("ng/ml")) {
				er.property_value_units_original = ExperimentalConstants.str_ug_L;
			}
		}
		
		// removes the ubiquitous chemical "chemical name redacted"
		if (r3m.test_substance_name.toLowerCase().contains("chemical name redacted")) {
			er.keep = false;
			er.reason = "no way of identifying this";
		}
		// flags the compounds that the documents had something suspect about the record
		if (r3m.CR_Notes != null && r3m.CR_Notes.toLowerCase().contains("flag"))
			er.flag = true;

		
		// assessment criteria for not keeping
		if (r3m.CR_Notes != null && (r3m.CR_Notes.toLowerCase().contains("none") || r3m.property_value.toLowerCase().contains("not determined") || r3m.Keep.equals("FALSE"))) {
			er.keep = false;
			er.reason = "absent data or misleading representation in original spreadsheet";
		} else if (r3m.CR_Notes != null && r3m.CR_Notes.contains("calculated")) {
			er.keep = false;
			er.reason = "not an experimental data point";
		}
		// handles pH
		if (r3m.CR_Notes != null && r3m.CR_Notes.contains("pH 7")) {
			er.pH = "7";
		}
		// handles references
		if (r3m.comments != null && (r3m.comments.toLowerCase().contains("reference: ") || r3m.comments.contains("references: "))) {
			er.original_source_name = r3m.comments;
		}
		// handles the unit conversions 
		if (er.property_name != null && OriginallyKOW == false) {
			uc.convertRecord(er);
			er.updateNote(r3m.CR_Notes);
		}
		// handles the logKOC
		if (er.property_name != null && er.property_name.equals(ExperimentalConstants.strLogKOC)) {
			if (er.property_value_point_estimate_original != null) {
				er.property_value_point_estimate_final = Math.log10(er.property_value_point_estimate_original);
			} else if (er.property_value_min_original != null && er.property_value_max_original != null){
				er.property_value_min_final = Math.log10(er.property_value_min_original);
				er.property_value_max_final = Math.log10(er.property_value_max_original);
			}
		}
		
		// handles the logBCF
		if (er.property_name != null && er.property_name.equals(ExperimentalConstants.strLogBCF)) {
			er.property_value_point_estimate_final = Math.log10(er.property_value_point_estimate_original);
		}
		
		System.out.println(er.property_value_point_estimate_original);
		recordsExperimental.add(er);

	}

	
	
	public static void main(String[] args) {
		ParseThreeM p = new ParseThreeM();
		p.createFiles();
		
	}
	
}