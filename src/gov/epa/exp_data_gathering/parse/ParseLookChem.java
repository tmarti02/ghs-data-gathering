package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

public class ParseLookChem extends Parse {
	String[] versions;
	
	public ParseLookChem(String[] versions) {
		sourceName = ExperimentalConstants.strSourceLookChem;
		this.init();
		this.versions = versions;
		fileNameSourceExcel=null;
		folderNameWebpages=null;
		folderNameExcel=null;
	}
	
	/**
	 * Parses HTML entries, either in zip folder or database, to RecordLookChem objects, then saves them to a JSON file
	 */
	@Override
	protected void createRecords() {
		// Vector<RecordLookChem> records = RecordLookChem.parseWebpagesInZipFile();
		Vector<RecordLookChem> records = new Vector<RecordLookChem>();
		for (String v:versions) {
			Vector<RecordLookChem> versionRecords = RecordLookChem.parseWebpagesInDatabase(v);
			records.addAll(versionRecords);
			System.out.println("Added "+versionRecords.size()+" records from "+v+"; total size "+records.size());
		}
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
	
	private void addAppearanceRecord(RecordLookChem lcr,ExperimentalRecords records) {
		ExperimentalRecord er=new ExperimentalRecord();
		er.date_accessed = lcr.date_accessed;
		er.casrn=lcr.CAS;
		er.einecs=lcr.EINECS;
		er.chemical_name=lcr.chemicalName;
		if (lcr.synonyms != null) { er.synonyms=lcr.synonyms.replace(';','|'); }
		er.property_name=ExperimentalConstants.strAppearance;
		er.property_value_string=lcr.appearance;
		er.property_value_qualitative=lcr.appearance.toLowerCase().replaceAll("colour","color").replaceAll("odour","odor").replaceAll("vapour","vapor");
		er.source_name=ExperimentalConstants.strSourceLookChem;
		er.keep = true;
		er.flag = false;
		
		// Constructs a LookChem URL from the CAS RN
		String baseURL = "https://www.lookchem.com/cas-";
		String prefix = lcr.CAS.substring(0,3);
		if (prefix.charAt(2)=='-') { prefix = prefix.substring(0,2); }
		er.url = baseURL+prefix+"/"+lcr.CAS+".html";
		uc.convertRecord(er);
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
		er.date_accessed = lcr.date_accessed;
		er.casrn=lcr.CAS;
		er.einecs=lcr.EINECS;
		er.chemical_name=lcr.chemicalName;
		if (lcr.synonyms != null) { er.synonyms=lcr.synonyms.replace(';','|'); }
		er.property_name=propertyName;
		er.property_value_string=propertyValue;
		er.source_name=ExperimentalConstants.strSourceLookChem;
		er.keep = true;
		er.flag = false;
		
		// Constructs a LookChem URL from the CAS RN
		String baseURL = "https://www.lookchem.com/cas-";
		String prefix = lcr.CAS.substring(0,3);
		if (prefix.charAt(2)=='-') { prefix = prefix.substring(0,2); }
		er.url = baseURL+prefix+"/"+lcr.CAS+".html";

		boolean foundNumeric = false;
		propertyValue = propertyValue.replaceAll("(\\d),(\\d)", "$1.$2");
		if (propertyName==ExperimentalConstants.strDensity) {
			foundNumeric = ParseUtilities.getDensity(er, propertyValue);
			ParseUtilities.getPressureCondition(er,propertyValue,sourceName);
			ParseUtilities.getTemperatureCondition(er,propertyValue);
		} else if (propertyName==ExperimentalConstants.strMeltingPoint || propertyName==ExperimentalConstants.strBoilingPoint ||
				propertyName==ExperimentalConstants.strFlashPoint) {
			foundNumeric = ParseUtilities.getTemperatureProperty(er,propertyValue);
			ParseUtilities.getPressureCondition(er,propertyValue,sourceName);
		} else if (propertyName==ExperimentalConstants.strWaterSolubility) {
			foundNumeric = ParseWaterSolubility.getWaterSolubility(er, propertyValue,sourceName);
			ParseUtilities.getTemperatureCondition(er,propertyValue);
			ParseWaterSolubility.getQualitativeSolubility(er, propertyValue,sourceName);
		}
		
		// Adds measurement methods and notes to valid records
		// Clears all numerical fields if property value was not obtainable
		if (foundNumeric) {
			if (propertyValue.contains("lit.")) { er.updateNote(ExperimentalConstants.str_lit); }
			if (propertyValue.contains("dec.")) { er.updateNote(ExperimentalConstants.str_dec); }
			if (propertyValue.contains("subl.")) { er.updateNote(ExperimentalConstants.str_subl); }
			// Warns if there may be a problem with an entry
			if (propertyName.contains("?")) {
				er.flag = true;
				er.reason = "Question mark";
			}
		} else {
			er.property_value_units_original = null;
			er.pressure_mmHg = null;
			er.temperature_C = null;
		}
		
		if ((propertyValue.toLowerCase().contains("tox") && er.property_value_units_original==null)
				|| (er.property_value_units_original==null && er.property_value_qualitative==null && er.note==null)) {
			er.keep = false;
			er.reason = "Bad data or units";
		} else if (propertyValue.toLowerCase().contains("predicted")) {
			er.keep = false;
			er.reason = "Predicted";
		}
		
		uc.convertRecord(er);
		
		recordsExperimental.add(er);
	}
	
	public static void main(String[] args) {
		String[] arr = {"General","PFAS"};
		ParseLookChem p = new ParseLookChem(arr);
		p.createFiles();
	}
}
