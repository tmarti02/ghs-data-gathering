package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

public class ParseLookChem extends Parse {
	
	public ParseLookChem() {
		sourceName = ExperimentalConstants.strSourceLookChem;
		this.init();
	}
	
	/**
	 * Parses HTML entries, either in zip folder or database, to RecordLookChem objects, then saves them to a JSON file
	 */
	@Override
	protected void createRecords() {
		Vector<RecordLookChem> records = RecordLookChem.parseWebpagesInZipFile();
		// Vector<RecordLookChem> records = RecordLookChem.parseWebpagesInDatabase();
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
			addNewExperimentalRecord(lcr,ExperimentalConstants.strDensity,lcr.density,recordsExperimental);
	    }
        if (lcr.meltingPoint != null && !lcr.meltingPoint.isBlank()) {
			addNewExperimentalRecord(lcr,ExperimentalConstants.strMeltingPoint,lcr.meltingPoint,recordsExperimental);
        }
        if (lcr.boilingPoint != null && !lcr.boilingPoint.isBlank()) {
			addNewExperimentalRecord(lcr,ExperimentalConstants.strBoilingPoint,lcr.boilingPoint,recordsExperimental);
	    }
        if (lcr.flashPoint != null && !lcr.flashPoint.isBlank()) {
			addNewExperimentalRecord(lcr,ExperimentalConstants.strFlashPoint,lcr.flashPoint,recordsExperimental);
	    }
        if (lcr.solubility != null && !lcr.solubility.isBlank()) {
			addNewExperimentalRecord(lcr,ExperimentalConstants.strWaterSolubility,lcr.solubility,recordsExperimental);
        } 
        if (lcr.appearance != null && !lcr.appearance.isBlank()) {
    		addAppearanceRecord(lcr,recordsExperimental);
        } 
	}
	
	private static void addAppearanceRecord(RecordLookChem lcr,ExperimentalRecords records) {
		ExperimentalRecord er=new ExperimentalRecord();
		er.casrn=lcr.CAS;
		er.chemical_name=lcr.chemicalName;
		if (lcr.synonyms != null) { er.synonyms=lcr.synonyms.replace(';','|'); }
		er.property_name=ExperimentalConstants.strAppearance;
		er.property_value_string=lcr.appearance;
		er.property_value_qualitative=lcr.appearance;
		er.source_name=ExperimentalConstants.strSourceLookChem;
		
		// Constructs a LookChem URL from the CAS RN
		String baseURL = "https://www.lookchem.com/cas-";
		String prefix = lcr.CAS.substring(0,3);
		if (prefix.charAt(2)=='-') { prefix = prefix.substring(0,2); }
		er.url = baseURL+prefix+"/"+lcr.CAS+".html";
		
		er.keep = true;
		er.flag = false;
		
		records.add(er);
	}
	
	/**
	 * Does the actual "dirty work" of translating a RecordLookChem object to an experimental data record
	 * @param lcr					The RecordLookChem object to be translated
	 * @param propertyName			The name of the property to be translated
	 * @param propertyValue			The property value in the RecordLookChem object, as a string
	 * @param recordsExperimental	The ExperimentalRecords object to store the new record
	 */
	private void addNewExperimentalRecord(RecordLookChem lcr,String propertyName,String propertyValue,ExperimentalRecords recordsExperimental) {
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
				er.updateNote(ExperimentalConstants.str_g_cm3+" assumed");
			}

			Parse.getPressureCondition(er,propertyValue);
			Parse.getTemperatureCondition(er,propertyValue);
			
		} else if (propertyName==ExperimentalConstants.strMeltingPoint) {
			String units = Parse.getTemperatureUnits(propertyValue);
			if (units.length()!=0) {
				er.property_value_units = units;
				unitsIndex = propertyValue.indexOf(units);
				badUnits = false;
			}
		} else if (propertyName==ExperimentalConstants.strBoilingPoint || propertyName==ExperimentalConstants.strFlashPoint) {
			String units = Parse.getTemperatureUnits(propertyValue);
			if (units.length()!=0) {
				er.property_value_units = units;
				unitsIndex = propertyValue.indexOf(units);
				badUnits = false;
			}
			
			Parse.getPressureCondition(er,propertyValue);
			
		} else if (propertyName==ExperimentalConstants.strWaterSolubility) {
			if (propertyValue.toLowerCase().contains("mg/l")) {
				er.property_value_units = ExperimentalConstants.str_mg_L;
				unitsIndex = propertyValue.indexOf("mg/");
				badUnits = false;
			} else if (propertyValue.toLowerCase().contains("g/l")) {
				er.property_value_units = ExperimentalConstants.str_g_L;
				unitsIndex = propertyValue.indexOf("g/");
				badUnits = false;
			} else if (propertyValue.toLowerCase().contains("% w/w")) {
				er.property_value_units = ExperimentalConstants.str_pctWt;
				unitsIndex = propertyValue.indexOf("%");
				badUnits = false;
			} else if (propertyValue.toLowerCase().contains("ppm")) {
				er.property_value_units = ExperimentalConstants.str_ppm;
				unitsIndex = propertyValue.indexOf("ppm");
				badUnits = false;
			}
			
			Parse.getTemperatureCondition(er,propertyValue);
			Parse.getQualitativeSolubility(er, propertyValue);
			
		}
		
		if (badUnits) { unitsIndex = propertyValue.length(); }
		
		boolean foundNumeric = false;
		if (!foundNumeric) {
			try {
				double[] range = Parse.extractFirstDoubleRangeFromString(propertyValue,unitsIndex);
				if (!badUnits) {
					er.property_value_min = range[0];
					er.property_value_max = range[1];
				}
				foundNumeric = true;
			} catch (Exception ex) { }
		}
		
		if (!foundNumeric) {
			try {
				double propertyValueAsDouble = Parse.extractDoubleFromString(propertyValue,unitsIndex);
				int propertyValueIndex = propertyValue.replaceAll(" ","").indexOf(Double.toString(propertyValueAsDouble).charAt(0));
				if (!badUnits) {
					er.property_value_point_estimate = propertyValueAsDouble;
					foundNumeric = true;
					if (propertyValueIndex > 0) {
						if (propertyValue.replaceAll(" ","").charAt(propertyValueIndex-1)=='>') {
							er.property_value_numeric_qualifier = ">";
						} else if (propertyValue.replaceAll(" ","").charAt(propertyValueIndex-1)=='<') {
							er.property_value_numeric_qualifier = "<";
						} else if (propertyValue.replaceAll(" ","").charAt(propertyValueIndex-1)=='~') {
							er.property_value_numeric_qualifier = "~";
						}
					}
				}
			} catch (Exception ex) { }
		}
		
		// Adds measurement methods and notes to valid records
		// Clears all numerical fields if property value was not obtainable
		if (foundNumeric && !badUnits) {
			if (propertyValue.contains("lit.")) { er.updateNote(ExperimentalConstants.str_lit); }
			if (propertyValue.contains("dec.")) { er.updateNote(ExperimentalConstants.str_dec); }
			if (propertyValue.contains("subl.")) { er.updateNote(ExperimentalConstants.str_subl); }
			// Warns if there may be a problem with an entry
			er.flag = false;
			if (propertyValue.contains(",")) {
				er.flag=true;
			}
		} else {
			er.property_value_units = null;
			er.pressure_kPa = null;
			er.temperature_C = null;
		}
		
		// Handles specific entries with weird formatting
		// I know hard-coding this is not preferred, but having this error constantly popping up bothers me!
		if (propertyName==ExperimentalConstants.strWaterSolubility) {
			if (er.casrn.contains("13252-14-7")) { er.property_value_point_estimate = 0.01894; }
		}
		
		if (!(er.property_value_string.toLowerCase().contains("tox") && er.property_value_units==null)
				&& (er.property_value_units!=null || er.property_value_qualitative!=null)) {
			er.keep = true;
		} else {
			er.keep = false;
		}
		recordsExperimental.add(er);
	}
	
	public static void main(String[] args) {
		ParseLookChem p = new ParseLookChem();
		p.createFiles();
	}
}
