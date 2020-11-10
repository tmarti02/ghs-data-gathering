package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Objects;
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
		if (lcr.density != null && !lcr.density.isBlank()) {
			createRecord(lcr,ExperimentalConstants.strDensity,lcr.density,recordsExperimental);
	    }
        if (lcr.meltingPoint != null && !lcr.meltingPoint.isBlank()) {
			createRecord(lcr,ExperimentalConstants.strMeltingPoint,lcr.meltingPoint,recordsExperimental);
        }
        if (lcr.boilingPoint != null && !lcr.boilingPoint.isBlank()) {
			createRecord(lcr,ExperimentalConstants.strBoilingPoint,lcr.boilingPoint,recordsExperimental);
	    }
        if (lcr.flashPoint != null && !lcr.flashPoint.isBlank()) {
			createRecord(lcr,ExperimentalConstants.strFlashPoint,lcr.flashPoint,recordsExperimental);
	    }
        if (lcr.solubility != null && !lcr.solubility.isBlank()) {
			createRecord(lcr,ExperimentalConstants.strWaterSolubility,lcr.solubility,recordsExperimental);
        } 
	}
	
	/**
	 * Does the actual "dirty work" of translating a RecordLookChem object to an experimental data record
	 * @param lcr					The RecordLookChem object to be translated
	 * @param propertyName			The name of the property to be translated
	 * @param propertyValue			The property value in the RecordLookChem object, as a string
	 * @param recordsExperimental	The ExperimentalRecords object to store the new record
	 */
	void createRecord(RecordLookChem lcr,String propertyName,String propertyValue,ExperimentalRecords recordsExperimental) {
		// Creates a new ExperimentalRecord object and sets all the fields that do not require advanced parsing
		ExperimentalRecord er=new ExperimentalRecord();
		er.casrn=lcr.CAS;
		er.chemical_name=lcr.chemicalName;
		if (lcr.synonyms != null) { er.synonyms=lcr.synonyms.replace(';','|'); }
		er.property_name=propertyName;
		er.property_value_string=propertyValue;
		er.source_name=ExperimentalConstants.strSourceLookChem;
		
		// Constructs a LookChem URL from the CAS RN
		String baseURL = "https://www.lookchem.com/cas-";
		String prefix = lcr.CAS.substring(0,3);
		if (prefix.charAt(2)=='-') { prefix = prefix.substring(0,2); }
		er.url = baseURL+prefix+"/"+lcr.CAS+".html";

		boolean badUnits = true;
		int unitsIndex = -1;
		// Replaces any intra-numerical commas with decimal points to handle international decimal format
		// Possible this could cause issues w/ commas as thousands separators, but I haven't seen any yet
		propertyValue = propertyValue.replaceAll("([0-9]),([0-9])", "$1.$2");
		if (propertyName==ExperimentalConstants.strDensity) {
			if (propertyValue.toLowerCase().contains("g/cm3") || propertyValue.toLowerCase().contains("g/cm 3")) {
				er.property_value_units = ExperimentalConstants.str_g_cm3;
				unitsIndex = propertyValue.indexOf("g/cm");
				badUnits = false;
			} else if (propertyValue.toLowerCase().contains("g/ml")) {
				er.property_value_units = ExperimentalConstants.str_g_mL;
				unitsIndex = propertyValue.indexOf("g/m");
				badUnits = false;
			} else {
				// Only g/cm3 or g/mL are ever used in LookChem, so we can safely assume units where they are missing
				er.property_value_units = ExperimentalConstants.str_g_cm3;
				unitsIndex = propertyValue.length();
				badUnits = false;
				updateNote(er,ExperimentalConstants.str_g_cm3+" assumed");
			}

			getPressureCondition(er,propertyValue);
			getTemperatureCondition(er,propertyValue);
			
		} else if (propertyName==ExperimentalConstants.strMeltingPoint) {
			String units = getTemperatureUnits(propertyValue);
			if (units.length()!=0) {
				er.property_value_units = units;
				unitsIndex = propertyValue.indexOf(units);
				badUnits = false;
			}
		} else if (propertyName==ExperimentalConstants.strBoilingPoint || propertyName==ExperimentalConstants.strFlashPoint) {
			String units = getTemperatureUnits(propertyValue);
			if (units.length()!=0) {
				er.property_value_units = units;
				unitsIndex = propertyValue.indexOf(units);
				badUnits = false;
			}
			
			getPressureCondition(er,propertyValue);
			
		} else if (propertyName==ExperimentalConstants.strWaterSolubility) {
			if (propertyValue.toLowerCase().contains("mg/l")) {
				er.property_value_units = ExperimentalConstants.str_mg_L;
				unitsIndex = propertyValue.indexOf("mg/");
				badUnits = false;
			} else if (propertyValue.toLowerCase().contains("g/l")) {
				er.property_value_units = ExperimentalConstants.str_g_L;
				unitsIndex = propertyValue.indexOf("g/");
				badUnits = false;
			}
			
			getTemperatureCondition(er,propertyValue);
			
		}
		
		if (badUnits) { unitsIndex = propertyValue.length(); }
		
		try {
			double[] range = extractFirstDoubleRangeFromString(propertyValue,unitsIndex);
			if (!badUnits) {
				er.property_value_min = range[0];
				er.property_value_max = range[1];
			}
		} catch (IllegalStateException ex1) {
			try {
				double propertyValueAsDouble = extractFirstDoubleFromString(propertyValue,unitsIndex);
				int propertyValueIndex = propertyValue.indexOf(Double.toString(propertyValueAsDouble).charAt(0));
				if (!badUnits) {
					er.property_value_point_estimate = propertyValueAsDouble;
					if (propertyValueIndex > 0) {
						if (propertyValue.charAt(propertyValueIndex-1)=='>') {
							er.property_value_numeric_qualifier = ">";
						} else if (propertyValue.charAt(propertyValueIndex-1)=='<') {
							er.property_value_numeric_qualifier = "<";
						} else if (propertyValue.charAt(propertyValueIndex-1)=='~') {
							er.property_value_numeric_qualifier = "~";
						}
					}
				}
			} catch (IllegalStateException ex2) {
				propertyName = "";
			}
		}
		
		// Adds measurement methods and notes to valid records
		// Clears all numerical fields if property value was not obtainable
		if (propertyName.length()!=0 && !badUnits) {
			if (propertyValue.contains("lit.")) { updateNote(er,ExperimentalConstants.str_lit); }
			if (propertyValue.contains("dec.")) { updateNote(er,ExperimentalConstants.str_dec); }
			if (propertyValue.contains("subl.")) { updateNote(er,ExperimentalConstants.str_subl); }
			// Warns if there may be multiple records in one entry
			if (propertyValue.contains(",")) {
				System.out.println(propertyName+" record for chemical "+lcr.chemicalName+" was created successfully, but requires manual checking");
			}
		} else {
			er.property_value_units = null;
			er.pressure_kPa = null;
			er.temperature_C = null;
		}
		
		recordsExperimental.add(er);
	}
	
	/**
	 * Adds a string to the note field of an ExperimentalRecord object
	 * @param er	The ExperimentalRecord object to be updated
	 * @param str	The string to be added
	 * @return		The updated ExperimentalRecord object
	 */
	private static void updateNote(ExperimentalRecord er, String str) {
		er.note = Objects.isNull(er.note) ? str : er.note+", "+str;
	}
	
	/**
	 * If the property value string contains temperature units, returns the units in standardized format
	 * @param propertyValue	The string to be read
	 * @return				A standardized temperature unit string from ExperimentalConstants
	 */
	private static String getTemperatureUnits(String propertyValue) {
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
	 * Sets the pressure condition for an ExperimentalRecord object, if present
	 * @param er			The ExperimentalRecord object to be updated
	 * @param propertyValue	The string to be read
	 * @return				The pressure condition in kPa
	 */
	private static void getPressureCondition(ExperimentalRecord er,String propertyValue) {
		propertyValue = propertyValue.toLowerCase();
		int pressureIndex = propertyValue.indexOf("mm");
		// If "mm" not found, looks for "torr" instead - a handful of records use this
		if (pressureIndex == -1) { pressureIndex = propertyValue.indexOf("torr"); }
		// If either set of pressure units were found, looks for the last number that precedes them
		if (pressureIndex > 0) {
			try {
				Matcher m = Pattern.compile("[-]?[0-9]*\\.?[0-9]+").matcher(propertyValue.substring(0,pressureIndex));
				String pressure = "";
				while (m.find()) { pressure = m.group(); }
				if (pressure.length()!=0) { er.pressure_kPa = Double.parseDouble(pressure)*ExperimentalConstants.mmHg_to_kPa; }
			} catch (Exception ex) { }
		}
	}
	
	/**
	 * Sets the temperature condition for an ExperimentalRecord object, if present
	 * @param er			The ExperimentalRecord object to be updated
	 * @param propertyValue	The string to be read
	 * @return				The temperature condition in C
	 */
	private static void getTemperatureCondition(ExperimentalRecord er, String propertyValue) {
		String units = getTemperatureUnits(propertyValue);
		int tempIndex = propertyValue.indexOf(units);
		// If temperature units were found, looks for the last number that precedes them
		if (tempIndex > 0) {
			try {
				Matcher m = Pattern.compile("[-]?[0-9]*\\.?[0-9]+").matcher(propertyValue.substring(0,tempIndex));
				String tempStr = "";
				while (m.find()) { tempStr = m.group(); }
				if (tempStr.length()!=0) {
					// Converts to C as needed
					double tempC = Double.parseDouble(tempStr);
					switch (units) {
					case "C":
						er.temperature_C = tempC;
						break;
					case "F":
						er.temperature_C = (tempC-32)*5/9;
						break;
					}
				}
			} catch (Exception ex) { }
		}
	}
	
	/**
	 * Extracts the first number before a given index in a string
	 * @param str	The string to be read
	 * @param end	The index to stop searching
	 * @return		The number found as a double
	 * @throws IllegalStateException	If no number is found in the given range
	 */
	private static double extractFirstDoubleFromString(String str,int end) throws IllegalStateException {
		Matcher numberMatcher = Pattern.compile("[-]?[ ]?[0-9]*\\.?[0-9]+").matcher(str.substring(0,end));
		numberMatcher.find();
		return Double.parseDouble(numberMatcher.group().replace(" ",""));
	}
	
	/**
	 * Extracts the first range of numbers before a given index in a string
	 * @param str	The string to be read
	 * @param end	The index to stop searching
	 * @return		The range found as a double[2]
	 * @throws IllegalStateException	If no number range is found in the given range
	 */
	private static double[] extractFirstDoubleRangeFromString(String str,int end) throws IllegalStateException {
		// Format "n[ ]-[ ]m [units]"
		Matcher anyRangeMatcher = Pattern.compile("[-]?[ ]?[0-9]*\\.?[0-9]+[ ]*[-]{1}[ ]*[-]?[ ]?[0-9]*\\.?[0-9]+").matcher(str.substring(0,end));
		anyRangeMatcher.find();
		String rangeAsStr = anyRangeMatcher.group();
		double[] range = new double[2];
		Matcher absMatcher = Pattern.compile("[0-9]*\\.?[0-9]+").matcher(rangeAsStr);
		Matcher negMinMatcher = Pattern.compile("[-][ ]?[0-9]*\\.?[0-9]+[ ]*[-]{1}[ ]*[-]?[ ]?[0-9]*\\.?[0-9]+").matcher(rangeAsStr);
		Matcher negMaxMatcher = Pattern.compile("[-]?[ ]?[0-9]*\\.?[0-9]+[ ]*[-]{1}[ ]*[-][ ]?[0-9]*\\.?[0-9]+").matcher(rangeAsStr);
		absMatcher.find();
		range[0] = Double.parseDouble(absMatcher.group().replace(" ",""));
		absMatcher.find();
		range[1] = Double.parseDouble(absMatcher.group().replace(" ",""));
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
