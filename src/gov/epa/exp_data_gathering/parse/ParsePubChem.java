package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
			for (String s:pcr.physicalDescription) { addAppearanceRecord(pcr,s,recordsExperimental); }
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
	
	private static void addAppearanceRecord(RecordPubChem pcr,String physicalDescription,ExperimentalRecords records) {
		ExperimentalRecord er=new ExperimentalRecord();
		er.casrn=String.join("|", pcr.cas);
		er.chemical_name=pcr.iupacName;
		if (pcr.synonyms != null) { er.synonyms=pcr.synonyms; }
		er.property_name=ExperimentalConstants.strAppearance;
		er.property_value_string=physicalDescription;
		er.property_value_qualitative=physicalDescription.toLowerCase().replaceAll("colour","color").replaceAll("odour","odor").replaceAll("vapour","vapor");
		er.url="https://pubchem.ncbi.nlm.nih.gov/compound/"+pcr.cid;
		er.source_name=ExperimentalConstants.strSourcePubChem;
		er.keep = true;
		er.flag = false;
		
		records.add(er);
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
		er.url="https://pubchem.ncbi.nlm.nih.gov/compound/"+pcr.cid;
		er.source_name=ExperimentalConstants.strSourcePubChem;
		er.keep=true;
		
		boolean badUnits = true;
		int unitsIndex = -1;
		propertyValue = propertyValue.replaceAll("greater than( or equal to )?", ">");
		propertyValue = propertyValue.replaceAll("less than( or equal to )?", "<");
		if (propertyName==ExperimentalConstants.strDensity) {
			propertyValue = propertyValue.replaceAll("([0-9]),([0-9])", "$1.$2");
			if (propertyValue.toLowerCase().contains("g/cm") || propertyValue.toLowerCase().contains("g/cu cm") || propertyValue.toLowerCase().contains("gm/cu cm")) {
				er.property_value_units = ExperimentalConstants.str_g_cm3;
				unitsIndex = propertyValue.toLowerCase().indexOf("g");
				badUnits = false;
			} else if (propertyValue.toLowerCase().contains("g/ml")) {
				er.property_value_units = ExperimentalConstants.str_g_mL;
				unitsIndex = propertyValue.toLowerCase().indexOf("g/m");
				badUnits = false;
			} else if (propertyValue.toLowerCase().contains("g/l")) {
				er.property_value_units = ExperimentalConstants.str_g_L;
				unitsIndex = propertyValue.toLowerCase().indexOf("g/l");
				badUnits = false;
			} else if (propertyValue.toLowerCase().contains("relative")) {
				unitsIndex = propertyValue.length();
				badUnits = false;
				if (propertyValue.toLowerCase().contains("mixture")) {
					er.updateNote(ExperimentalConstants.str_relative_mixture_density);
				} else if (propertyValue.toLowerCase().contains("gas")) {
					er.updateNote(ExperimentalConstants.str_relative_gas_density);
				} else {
					er.updateNote(ExperimentalConstants.str_relative_density);
				}
			} else {
				er.property_value_units = ExperimentalConstants.str_g_cm3;
				if (propertyValue.contains(":")) {
					unitsIndex = propertyValue.length();
				} else if (propertyValue.contains(" ")) {
					unitsIndex = propertyValue.indexOf(" ");
				} else {
					unitsIndex = propertyValue.length();
				}
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
			
			Parse.getPressureCondition(er,propertyValue);
			
		} else if (propertyName==ExperimentalConstants.strBoilingPoint || propertyName==ExperimentalConstants.strFlashPoint) {
			String units = Parse.getTemperatureUnits(propertyValue);
			if (units.length()!=0) {
				er.property_value_units = units;
				unitsIndex = propertyValue.indexOf(units);
				badUnits = false;
			}
			
			Parse.getPressureCondition(er,propertyValue);
			if (propertyValue.contains("closed cup") || propertyValue.contains("c.c.")) { er.measurement_method = "closed cup"; }
			
		} else if (propertyName==ExperimentalConstants.strWaterSolubility) {
			propertyValue = propertyValue.replaceAll("([0-9]),([0-9]{3})", "$1$2");
			if (propertyValue.toLowerCase().contains("mg/l")) {
				er.property_value_units = ExperimentalConstants.str_mg_L;
				unitsIndex = propertyValue.indexOf("mg/");
				badUnits = false;
			} else if (propertyValue.toLowerCase().contains("mg/ml")) {
				er.property_value_units = ExperimentalConstants.str_mg_mL;
				unitsIndex = propertyValue.indexOf("mg/");
				badUnits = false;
			} else if (propertyValue.toLowerCase().contains("ug/l")) {
				er.property_value_units = ExperimentalConstants.str_ug_L;
				unitsIndex = propertyValue.indexOf("ug/");
				badUnits = false;
			} else if (propertyValue.toLowerCase().contains("ug/ml")) {
				er.property_value_units = ExperimentalConstants.str_ug_mL;
				unitsIndex = propertyValue.indexOf("ug/");
				badUnits = false;
			} else if (propertyValue.toLowerCase().contains("g/l")) {
				er.property_value_units = ExperimentalConstants.str_g_L;
				unitsIndex = propertyValue.indexOf("g/");
				badUnits = false;
			} else if (propertyValue.toLowerCase().contains("% w/w") || propertyValue.toLowerCase().contains("wt%")) {
				er.property_value_units = ExperimentalConstants.str_pctWt;
				unitsIndex = propertyValue.indexOf("%");
				badUnits = false;
			} else if (propertyValue.toLowerCase().contains("%")) {
				er.property_value_units = ExperimentalConstants.str_pct;
				unitsIndex = propertyValue.indexOf("%");
				badUnits = false;
			} else if (propertyValue.toLowerCase().contains("ppm")) {
				er.property_value_units = ExperimentalConstants.str_ppm;
				unitsIndex = propertyValue.indexOf("ppm");
				badUnits = false;
			} else if (propertyValue.contains("M")) {
				unitsIndex = propertyValue.indexOf("M");
				if (unitsIndex>0) {
					er.property_value_units = ExperimentalConstants.str_M;
					badUnits = false;
				}
			} 
			
			if (propertyValue.contains(":")) {
				unitsIndex = propertyValue.length();
			}
			
			if (!badUnits && Character.isAlphabetic(propertyValue.charAt(0)) && !propertyValue.contains("water")) {
				er.keep = false;
			}
			
			Parse.getTemperatureCondition(er,propertyValue);
			getQualitativeSolubility(er, propertyValue);
			
		} else if (propertyName==ExperimentalConstants.strVaporPressure) {
			propertyValue = propertyValue.replaceAll("([0-9]),([0-9]{3})", "$1$2");
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
			} else if (propertyValue.toLowerCase().contains(ExperimentalConstants.str_negl)) {
				er.property_value_qualitative = ExperimentalConstants.str_negl;
			}
			
			if (propertyValue.contains(":")) {
				unitsIndex = propertyValue.length();
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
			
		} else if (propertyName==ExperimentalConstants.str_pKA) {
			// No pKa values in test set yet
			unitsIndex = propertyValue.length();
			badUnits = false;
		}
		
		if (badUnits) { unitsIndex = propertyValue.length(); }
		
		boolean foundNumeric = false;
		if (!foundNumeric) {
			try {
				Matcher sciMatcher = Pattern.compile("([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]?(e|x10)[ ]?([-|\\+]?[ ]?[0-9]+)").matcher(propertyValue.toLowerCase().substring(0,unitsIndex));
				sciMatcher.find();
				String strMantissa = sciMatcher.group(1);
				String strMagnitude = sciMatcher.group(3);
				Double mantissa = Double.parseDouble(strMantissa.replaceAll(" ",""));
				Double magnitude =  Double.parseDouble(strMagnitude.replaceAll(" ", "").replaceAll("\\+", ""));
				er.property_value_point_estimate = mantissa*Math.pow(10, magnitude);
				foundNumeric = true;
				int propertyValueIndex;
				if ((propertyValueIndex = propertyValue.indexOf(strMantissa)) > 0) {
					if (propertyValue.replaceAll(" ","").charAt(propertyValueIndex-1)=='>') {
						er.property_value_numeric_qualifier = ">";
					} else if (propertyValue.replaceAll(" ","").charAt(propertyValueIndex-1)=='<') {
						er.property_value_numeric_qualifier = "<";
					} else if (propertyValue.replaceAll(" ","").charAt(propertyValueIndex-1)=='~') {
						er.property_value_numeric_qualifier = "~";
					}
				}
			} catch (Exception ex) { }
		}
		
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
						if (propertyValue.replaceAll(" ","").charAt(propertyValueIndex-1)=='>' || propertyValue.toLowerCase().contains("greater than")) {
							er.property_value_numeric_qualifier = ">";
						} else if (propertyValue.replaceAll(" ","").charAt(propertyValueIndex-1)=='<' || propertyValue.toLowerCase().contains("less than")) {
							er.property_value_numeric_qualifier = "<";
						} else if (propertyValue.replaceAll(" ","").charAt(propertyValueIndex-1)=='~') {
							er.property_value_numeric_qualifier = "~";
						}
					}
				}
			} catch (Exception ex) { }
		}
		
		if (!propertyName.equals(ExperimentalConstants.strWaterSolubility) && propertyValue.toLowerCase().contains("decomposes")) {
			er.updateNote(ExperimentalConstants.str_dec);
		}
		if (propertyValue.toLowerCase().contains("est")) { er.updateNote(ExperimentalConstants.str_est); }
		if ((propertyValue.toLowerCase().contains("ext") || propertyValue.toLowerCase().contains("from exp")) && !propertyValue.toLowerCase().contains("extreme")) {
			er.updateNote(ExperimentalConstants.str_ext);
		}
		// Warns if there may be a problem with an entry
		er.flag = false;
		if (propertyValue.contains("?")) { er.flag = true; }
		
		if ((er.property_value_point_estimate!=null || er.property_value_min!=null || er.property_value_qualitative!=null || er.note!=null)
				&& !er.keep==false) {
			er.keep = true;
		} else {
			er.keep = false;
		}
		
		recordsExperimental.add(er);
	}

	public static void main(String[] args) {
		ParsePubChem p = new ParsePubChem();
		p.createFiles();
	}
}
