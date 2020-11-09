package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.api.AADashboard;
import gov.epa.api.ExperimentalConstants;

public class ParseLookChem extends Parse {
	
	public ParseLookChem() {
		sourceName = ExperimentalConstants.strSourceLookChem;
		folderNameWebpages = "web pages";
		fileNameHtmlZip = "web pages.zip";
		this.init();
	}
	
	/**
	 * Parses HTML entries, either in zip folder or database, to RecordLookChem objects, then saves them to a JSON file
	 */
	@Override
	protected void createRecords() {
		// Vector<RecordLookChem> records = RecordLookChem.parseWebpagesInZipFile();
		Vector<RecordLookChem> records = RecordLookChem.parseWebpagesInDatabase();
		writeOriginalRecordsToFile(records);
	}
	
	/**
	 * Reads the JSON file created by createRecords() and translates it to an ExperimentalRecords object
	 */
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			File jsonFile = new File(mainFolder + "/" + fileNameJSON_Records);
			
			RecordLookChem[] recordsLookChem = gson.fromJson(new FileReader(jsonFile), RecordLookChem[].class);
			
			for (int i = 0; i < recordsLookChem.length; i++) {
				RecordLookChem r = recordsLookChem[i];
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	/**
	 * Translates a RecordLookChem object to a set of experimental data records and adds them to an ExperimentalRecords object
	 * @param lcr					The RecordLookChem object to be translated
	 * @param recordsExperimental	The ExperimentalRecords object to store the new records
	 */
	private void addExperimentalRecords(RecordLookChem lcr,ExperimentalRecords recordsExperimental) {
		if (lcr.density.length()!=0) {
			createRecord(lcr,ExperimentalConstants.strDensity,lcr.density,recordsExperimental);
	    }
        if (lcr.meltingPoint.length()!=0) {
			createRecord(lcr,ExperimentalConstants.strMeltingPoint,lcr.meltingPoint,recordsExperimental);
        }
        if (lcr.boilingPoint.length()!=0) {
			createRecord(lcr,ExperimentalConstants.strBoilingPoint,lcr.boilingPoint,recordsExperimental);
	    }
        if (lcr.flashPoint.length()!=0) {
			createRecord(lcr,ExperimentalConstants.strFlashPoint,lcr.flashPoint,recordsExperimental);
	    }
        if (lcr.solubility.length()!=0) {
			createRecord(lcr,ExperimentalConstants.strWaterSolubility,lcr.solubility,recordsExperimental);
        } 
	}
	
	/**
	 * Does the actual "dirty work" of translating a RecordLookChem object to an experimental data record
	 * Should probably be cleaned up and split into several methods
	 * @param lcr					The RecordLookChem object to be translated
	 * @param propertyName			The name of the property to be translated
	 * @param propertyValue			The property value in the RecordLookChem object, as a string
	 * @param recordsExperimental	The ExperimentalRecords object to store the new record
	 */
	void createRecord(RecordLookChem lcr,String propertyName,String propertyValue,ExperimentalRecords recordsExperimental) {
		ExperimentalRecord er=new ExperimentalRecord();
		
		er.casrn=lcr.CAS;
		er.chemical_name=lcr.chemicalName;
		er.synonyms=lcr.synonyms.replace(';','|');
		er.property_name=propertyName;
		er.property_value_string=propertyValue;
		er.url="https://www.lookchem.com/cas-"+lcr.CAS.substring(0,3)+"/"+lcr.CAS+".html";
		er.source_name=ExperimentalConstants.strSourceLookChem;

		boolean badUnits = false;
		String badUnitsMsg = "Unrecognized or missing units for property "+propertyName+" for chemical "+lcr.chemicalName;
		int unitsIndex = -1;
		if (propertyName==ExperimentalConstants.strDensity) {
			if (propertyValue.contains("g/cm3") || propertyValue.contains("g/cm 3")) {
				er.property_value_units = ExperimentalConstants.str_g_cm3;
				unitsIndex = propertyValue.indexOf("g/cm");
			} else {
				System.out.println(badUnitsMsg);
				badUnits = true;
				unitsIndex = propertyValue.length();
			}
		} else if (propertyName==ExperimentalConstants.strMeltingPoint) {
			String units = getTemperatureUnits(propertyValue);
			if (units.length()!=0) {
				er.property_value_units = units;
				unitsIndex = propertyValue.indexOf(units);
			} else {
				System.out.println(badUnitsMsg);
				badUnits = true;
				unitsIndex = propertyValue.length();
			}
		} else if (propertyName==ExperimentalConstants.strBoilingPoint) {
			String units = getTemperatureUnits(propertyValue);
			if (units.length()!=0) {
				er.property_value_units = units;
				unitsIndex = propertyValue.indexOf(units);
			} else {
				System.out.println(badUnitsMsg);
				badUnits = true;
				unitsIndex = propertyValue.length();
			}
			
			try {
				er.pressure_kPa = getPressureCondition(propertyValue);
			} catch (Exception ex) { }
		} else if (propertyName==ExperimentalConstants.strFlashPoint) {
			String units = getTemperatureUnits(propertyValue);
			if (units.length()!=0) {
				er.property_value_units = units;
				unitsIndex = propertyValue.indexOf(units);
			} else {
				System.out.println(badUnitsMsg);
				badUnits = true;
				unitsIndex = propertyValue.length();
			}
			
			try {
				er.pressure_kPa = getPressureCondition(propertyValue);
			} catch (Exception ex) { }
		} else if (propertyName==ExperimentalConstants.strWaterSolubility) {
			if (propertyValue.contains("mg/L") || propertyValue.contains("mg/l")) {
				er.property_value_units = ExperimentalConstants.str_mg_L;
				unitsIndex = propertyValue.indexOf("mg/");
			} else if (propertyValue.contains("g/L") || propertyValue.contains("g/l")) {
				er.property_value_units = ExperimentalConstants.str_g_L;
				unitsIndex = propertyValue.indexOf("g/");
			} else {
				System.out.println(badUnitsMsg);
				badUnits = true;
				unitsIndex = propertyValue.length();
			}
			
			// Checks if there is a temperature condition associated with solubility
			String units = getTemperatureUnits(propertyValue);
			if (units.equals(ExperimentalConstants.str_C)) {
				int tempIndex = propertyValue.indexOf("C");
				// Finds last number before "C" - excludes beginning of line
				Matcher m = Pattern.compile("(?<!^)[-]?[0-9]*\\.?[0-9]+").matcher(propertyValue.substring(0,tempIndex));
				String temp = "";
				while (m.find()) { temp = m.group(); }
				er.temperature_C = Double.parseDouble(temp);
			} else if (units.equals(ExperimentalConstants.str_F)) {
				int tempIndex = propertyValue.indexOf("F");
				Matcher m = Pattern.compile("(?<!^)[-]?[0-9]*\\.?[0-9]+").matcher(propertyValue.substring(0,tempIndex));
				String temp = "";
				while (m.find()) { temp = m.group(); }
				er.temperature_C = (Double.parseDouble(temp)-32)*5/9;
			}
		}
		
		// Gets numerical value of property
		try {
			double[] range = extractFirstDoubleRangeFromString(propertyValue,unitsIndex);
			if (!badUnits) {
				er.property_value_min = range[0];
				er.property_value_max = range[1];
			}
		} catch (IllegalStateException ex1) {
			try {
				double propertyValueAsDouble = extractFirstDoubleFromString(propertyValue,unitsIndex);
				// Rough - what if there are other characters first?
				if (propertyValue.charAt(0)=='>' && !badUnits) {
					er.property_value_min = propertyValueAsDouble;
				} else if (propertyValue.charAt(0)=='<' && !badUnits) {
					er.property_value_max = propertyValueAsDouble;
				} else if (!badUnits) {
					er.property_value_point_estimate = propertyValueAsDouble;
				}
			} catch (IllegalStateException ex2) {
				System.out.println("Unrecognized or missing value of property "+propertyName+" for chemical "+lcr.chemicalName);
				propertyName = "";
			}
		}
		
		// Adds measurement methods and notes to valid records
		// Clears all numerical fields if property value was not obtainable
		if (propertyName.length()!=0 && !badUnits) {
			if (propertyValue.contains("lit")) { er.measurement_method = ExperimentalConstants.str_lit; }
			if (propertyValue.contains("dec")) { er.note = ExperimentalConstants.str_dec; }
			if (propertyValue.contains("~")) { er.note = ExperimentalConstants.str_approx; }
		} else {
			er.property_value_units = null;
			er.pressure_kPa = null;
			er.temperature_C = null;
		}

		recordsExperimental.add(er);
	}
	
	/**
	 * If the property value string contains temperature units, returns the units in standardized format
	 * @param propertyValue	The string to be read
	 * @return				A standardized temperature unit string from ExperimentalConstants
	 */
	private String getTemperatureUnits(String propertyValue) {
		String units = "";
		if (propertyValue.contains("°C") || propertyValue.contains("ºC") 
				|| propertyValue.contains(" C") || propertyValue.contains("oC")) {
			units = ExperimentalConstants.str_C;
		} else if (propertyValue.contains("°F") || propertyValue.contains("ºF") 
				|| propertyValue.contains(" F")|| propertyValue.contains("oF")) {
			units = ExperimentalConstants.str_F;
		} 
		return units;
	}
	
	/**
	 * If the property value contains a pressure, returns the pressure converted to kPa
	 * @param propertyValue	The string to be read
	 * @return				The pressure condition in kPa
	 */
	private double getPressureCondition(String propertyValue) {
		// "mmHg","mm Hg","mm" Do any units besides mmHg appear in LookChem?
		int pressureIndex = propertyValue.indexOf("mm");
		String pressure = "";
		if (pressureIndex!=-1) {
			// Finds last number before "mm" - excludes beginning of line
			Matcher m = Pattern.compile("(?<!^)[-]?[0-9]*\\.?[0-9]+").matcher(propertyValue.substring(0,pressureIndex));
			while (m.find()) { pressure = m.group(); }
		}
		double pressurekPa = Double.parseDouble(pressure)*ExperimentalConstants.mmHg_to_kPa;
		return pressurekPa;
	}
	
	/**
	 * Extracts the first number before a given index in a string
	 * @param str	The string to be read
	 * @param end	The index to stop searching
	 * @return		The number found as a double
	 * @throws IllegalStateException	If no number is found in the given range
	 */
	private double extractFirstDoubleFromString(String str,int end) throws IllegalStateException {
		Matcher numberMatcher = Pattern.compile("[-]?[0-9]*\\.?[0-9]+").matcher(str.substring(0,end));
		numberMatcher.find();
		return Double.parseDouble(numberMatcher.group());
	}
	
	/**
	 * Extracts the first range of numbers before a given index in a string
	 * @param str	The string to be read
	 * @param end	The index to stop searching
	 * @return		The range found as a double[2]
	 * @throws IllegalStateException	If no number range is found in the given range
	 */
	private double[] extractFirstDoubleRangeFromString(String str,int end) throws IllegalStateException {
		// Format "n[ ]-[ ]m [units]"
		Matcher anyRangeMatcher = Pattern.compile("[-]?[0-9]*\\.?[0-9]+[ ]*[-]{1}[ ]*[-]?[0-9]*\\.?[0-9]+").matcher(str.substring(0,end));
		anyRangeMatcher.find();
		String rangeAsStr = anyRangeMatcher.group();
		double[] range = new double[2];
		Matcher absMatcher = Pattern.compile("[0-9]*\\.?[0-9]+").matcher(rangeAsStr);
		Matcher negMinMatcher = Pattern.compile("[-][0-9]*\\.?[0-9]+[ ]*[-]{1}[ ]*[-]?[0-9]*\\.?[0-9]+").matcher(rangeAsStr);
		Matcher negMaxMatcher = Pattern.compile("[-]?[0-9]*\\.?[0-9]+[ ]*[-]{1}[ ]*[-][0-9]*\\.?[0-9]+").matcher(rangeAsStr);
		absMatcher.find();
		range[0] = Double.parseDouble(absMatcher.group());
		absMatcher.find();
		range[1] = Double.parseDouble(absMatcher.group());
		if (negMinMatcher.matches()) { range[0] *= -1; }
		if (negMaxMatcher.matches()) { range[1] *= -1; }
		return range;
	}
	
	public static void main(String[] args) {
		ParseLookChem p = new ParseLookChem();
		p.createRecords();
		ExperimentalRecords records = p.goThroughOriginalRecords();
		records.toJSON_File(AADashboard.dataFolder+File.separator+sourceName+
				File.separator+sourceName+" Experimental Records.json");
	}
}
