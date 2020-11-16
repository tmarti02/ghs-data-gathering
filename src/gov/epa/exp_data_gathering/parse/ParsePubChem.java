package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;

public class ParsePubChem extends Parse {
	
	public ParsePubChem() {
		sourceName = ExperimentalConstants.strSourcePubChem;
		this.init();
		folderNameWebpages=null;
	}
	
	/**
	 * Parses JSON entries in database to RecordPubChem objects, then saves them to a JSON file
	 */
	@Override
	protected void createRecords() {
		Vector<RecordPubChem> records = RecordPubChem.parseJSONsInDatabase();
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
			
			RecordPubChem[] recordsPubChem = gson.fromJson(new FileReader(jsonFile), RecordPubChem[].class);
			
			for (int i = 0; i < recordsPubChem.length; i++) {
				RecordPubChem r = recordsPubChem[i];
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	private void addExperimentalRecords(RecordPubChem pcr, ExperimentalRecords recordsExperimental) {
		if (!pcr.physicalDescription.isEmpty()) {
			for (String s:pcr.physicalDescription) { addNewExperimentalRecord(pcr,ExperimentalConstants.strAppearance,s,recordsExperimental); }
	    }
		if (!pcr.density.isEmpty()) {
			for (String s:pcr.density) { addNewExperimentalRecord(pcr,ExperimentalConstants.strDensity,s,recordsExperimental); }
	    }
        if (!pcr.meltingPoint.isEmpty()) {
			for (String s:pcr.meltingPoint) { addNewExperimentalRecord(pcr,ExperimentalConstants.strMeltingPoint,s,recordsExperimental); }
        }
        if (!pcr.boilingPoint.isEmpty()) {
			for (String s:pcr.boilingPoint) { addNewExperimentalRecord(pcr,ExperimentalConstants.strBoilingPoint,s,recordsExperimental); }
	    }
        if (!pcr.flashPoint.isEmpty()) {
			for (String s:pcr.flashPoint) { addNewExperimentalRecord(pcr,ExperimentalConstants.strFlashPoint,s,recordsExperimental); }
	    }
        if (!pcr.solubility.isEmpty()) {
			for (String s:pcr.solubility) { addNewExperimentalRecord(pcr,ExperimentalConstants.strWaterSolubility,s,recordsExperimental); }
        }
        if (!pcr.vaporPressure.isEmpty()) {
			for (String s:pcr.vaporPressure) { addNewExperimentalRecord(pcr,ExperimentalConstants.strVaporPressure,s,recordsExperimental); }
        }
        if (!pcr.henrysLawConstant.isEmpty()) {
			for (String s:pcr.henrysLawConstant) { addNewExperimentalRecord(pcr,ExperimentalConstants.strHenrysLawConstant,s,recordsExperimental); }
        }
        if (!pcr.logP.isEmpty()) {
			for (String s:pcr.logP) { addNewExperimentalRecord(pcr,ExperimentalConstants.strLogKow,s,recordsExperimental); }
        }
        if (!pcr.pKa.isEmpty()) {
			for (String s:pcr.pKa) { addNewExperimentalRecord(pcr,ExperimentalConstants.str_pKA,s,recordsExperimental); }
        }
	}
	
	private void addNewExperimentalRecord(RecordPubChem pcr,String propertyName,String propertyValue,ExperimentalRecords recordsExperimental) {
		if (propertyValue==null) { return; }
		// Creates a new ExperimentalRecord object and sets all the fields that do not require advanced parsing
		ExperimentalRecord er=new ExperimentalRecord();
		er.casrn = String.join("|", pcr.cas);
		er.chemical_name=pcr.iupacName;
		er.smiles=pcr.smiles;
		if (pcr.synonyms != null) { er.synonyms=pcr.synonyms; }
		er.property_name=propertyName;
		er.property_value_string=propertyValue;
		// TODO carry over reference info, rather than just sourcing all as "PubChem"
		// TODO URL
		er.source_name=ExperimentalConstants.strSourcePubChem;
		er.keep=true;
		
		boolean badUnits = true;
		int unitsIndex = -1;
		if (propertyName==ExperimentalConstants.strDensity) {
			if (propertyValue.toLowerCase().contains("g/cm") || propertyValue.toLowerCase().contains("g/cu cm")) {
				er.property_value_units = ExperimentalConstants.str_g_cm3;
				unitsIndex = propertyValue.toLowerCase().indexOf("g/c");
				badUnits = false;
			} else if (propertyValue.toLowerCase().contains("g/ml")) {
				er.property_value_units = ExperimentalConstants.str_g_mL;
				unitsIndex = propertyValue.toLowerCase().indexOf("g/m");
				badUnits = false;
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
			} else if (propertyValue.toLowerCase().contains("mg/ml")) {
				er.property_value_units = ExperimentalConstants.str_mg_mL;
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
			
		} else if (propertyName==ExperimentalConstants.strVaporPressure) {
			if (propertyValue.toLowerCase().contains("mmhg") || propertyValue.toLowerCase().contains("mm hg")) {
				er.property_value_units = ExperimentalConstants.str_mmHg;
				unitsIndex = propertyValue.toLowerCase().indexOf("mm");
				badUnits = false;
			} else if (propertyValue.toLowerCase().contains("atm")) {
				er.property_value_units = ExperimentalConstants.str_atm;
				unitsIndex = propertyValue.toLowerCase().indexOf("atm");
				badUnits = false;
			} else if (propertyValue.toLowerCase().contains("kpa")) {
				er.property_value_units = ExperimentalConstants.str_kpa;
				unitsIndex = propertyValue.toLowerCase().indexOf("kpa");
				badUnits = false;
			}
				
			Parse.getTemperatureCondition(er,propertyValue);
			
		} else if (propertyName==ExperimentalConstants.strHenrysLawConstant) {
			if (propertyValue.toLowerCase().contains("atm-m3/mole")) {
				er.property_value_units = ExperimentalConstants.str_m3_atm_mol;
				unitsIndex = propertyValue.indexOf("atm");
				badUnits = false;
			}
		} else if (propertyName==ExperimentalConstants.strLogKow) {
			unitsIndex = propertyValue.length();
			badUnits = false;
			
			Parse.getTemperatureCondition(er,propertyValue);
		} else {
			// not handling other properties yet
			badUnits = true;
		}
		
		if (badUnits) { unitsIndex = propertyValue.length(); }
		
		try {
			double[] range = Parse.extractFirstDoubleRangeFromString(propertyValue,unitsIndex);
			if (!badUnits) {
				er.property_value_min = range[0];
				er.property_value_max = range[1];
			}
		} catch (IllegalStateException ex1) {
			try {
				double propertyValueAsDouble = Parse.extractFirstDoubleFromString(propertyValue,unitsIndex);
				int propertyValueIndex = propertyValue.replaceAll(" ","").indexOf(Double.toString(propertyValueAsDouble).charAt(0));
				if (!badUnits) {
					er.property_value_point_estimate = propertyValueAsDouble;
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
			} catch (IllegalStateException ex2) {
				propertyName = "";
			}
		}
		
		recordsExperimental.add(er);
	}

	public static void main(String[] args) {
		ParsePubChem p = new ParsePubChem();
		p.createFiles();
	}
}
