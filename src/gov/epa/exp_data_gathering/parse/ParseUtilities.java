package gov.epa.exp_data_gathering.parse;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.eChemPortalAPI.ToxRecordEChemPortalAPI;
import gov.epa.exp_data_gathering.parse.RecordChemidplus.ToxicityRecord;

public class ParseUtilities extends Parse {

	public static boolean getNumericalValue(ExperimentalRecord er, String propertyValue, int unitsIndex, boolean badUnits) {
		if (badUnits) { unitsIndex = propertyValue.length(); }
		if (propertyValue.contains("±")) { unitsIndex = Math.min(propertyValue.indexOf("±"),unitsIndex); }
		boolean foundNumeric = false;
		if (!foundNumeric) {
			try {
				Matcher sciMatcher = Pattern.compile("([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]?(e|x10)[ ]?([-|\\+]?[ ]?[0-9]+)").matcher(propertyValue.toLowerCase().substring(0,unitsIndex));
				if (sciMatcher.find()) {
					String strMantissa = sciMatcher.group(1);
					String strMagnitude = sciMatcher.group(3);
					Double mantissa = Double.parseDouble(strMantissa.replaceAll("\\s",""));
					Double magnitude =  Double.parseDouble(strMagnitude.replaceAll("\\s","").replaceAll("\\+", ""));
					er.property_value_point_estimate_original = mantissa*Math.pow(10, magnitude);
					foundNumeric = true;
					int propertyValueIndex;
					if (!badUnits && (propertyValueIndex = propertyValue.indexOf(strMantissa)) > 0) {
						String checkSymbol = propertyValue.replaceAll("\\s","");
						er.property_value_numeric_qualifier = getNumericQualifier(checkSymbol,propertyValueIndex);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (!foundNumeric) {
			try {
				double[] range = extractFirstDoubleRangeFromString(propertyValue,unitsIndex);
				if (!badUnits && range!=null) {
					er.property_value_min_original = range[0];
					er.property_value_max_original = range[1];
					foundNumeric = true;
				}
				if (!badUnits && (propertyValue.contains("~") || propertyValue.contains("ca."))) {
					er.property_value_numeric_qualifier = "~";
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (!foundNumeric) {
			try {
				double[] range = extractAltFormatRangeFromString(propertyValue,unitsIndex);
				if (!badUnits && range!=null) {
					er.property_value_min_original = range[0];
					er.property_value_max_original = range[1];
					foundNumeric = true;
				}
				if (!badUnits && (propertyValue.contains("~") || propertyValue.contains("ca."))) {
					er.property_value_numeric_qualifier = "~";
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (!foundNumeric) {
			try {
				double propertyValueAsDouble = extractDoubleFromString(propertyValue,unitsIndex);
				int propertyValueIndex = -1;
				if (propertyValueAsDouble >= 0 && propertyValueAsDouble < 1) {
					// Bug fix for zeroes, 12/17/2020 - This if statement is new; previous was just the contents of the "else" clause
					if (!propertyValue.contains(".")) {
						// If property value is the integer 0, sets start index to the location of the 0
						propertyValueIndex = propertyValue.replaceAll("\\s","").indexOf("0");
					} else {
						// Otherwise, sets start index to the location of the 0 (if present) or the . (if formatted .xx instead of 0.xx)
						// Without the above "if", if an entry contains just the integer 0, the Math.min will select -1 as the index since it can't find a .
						propertyValueIndex = Math.min(propertyValue.replaceAll("\\s","").indexOf("0"),propertyValue.replaceAll("\\s","").indexOf("."));
					}
				} else {
					propertyValueIndex = propertyValue.replaceAll("\\s","").indexOf(Double.toString(propertyValueAsDouble).charAt(0));
				}
				if (!badUnits) {
					er.property_value_point_estimate_original = propertyValueAsDouble;
					foundNumeric = true;
					if (propertyValueIndex > 0) {
						String checkSymbol = propertyValue.replaceAll("\\s","");
						er.property_value_numeric_qualifier = getNumericQualifier(checkSymbol,propertyValueIndex);
					}
				}
			} catch (NumberFormatException ex) {
				// NumberFormatException means no numerical value was found; leave foundNumeric = false and do nothing else
			}
		}
		
		return foundNumeric;
	}

	public static String getNumericQualifier(String str,int index) {
		String symbol = "";
		if (index > 0) {
			if (str.charAt(index-1)=='>') {
				symbol = ">";
			} else if (str.charAt(index-1)=='<') {
				symbol = "<";
			} else if (str.charAt(index-1)=='~' || str.contains("ca.") || str.contains("circa")) {
				symbol = "~";
			} else if (index > 1 && str.charAt(index-2)=='>' && str.charAt(index-1)=='=') {
				symbol = ">=";
			} else if (index > 1 && str.charAt(index-2)=='<' && str.charAt(index-1)=='=') {
				symbol = "<=";
			}

		}
		return symbol;
	}

	public static boolean getDensity(ExperimentalRecord er, String propertyValue) {
		boolean badUnits = true;
		int unitsIndex = -1;
		propertyValue = propertyValue.replaceAll("([0-9]),([0-9])", "$1.$2");
		if (propertyValue.toLowerCase().contains("g/cm") || propertyValue.toLowerCase().contains("g/cu cm") || propertyValue.toLowerCase().contains("gm/cu cm")) {
			er.property_value_units_original = ExperimentalConstants.str_g_cm3;
			unitsIndex = propertyValue.toLowerCase().indexOf("g");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("g/ml") || propertyValue.toLowerCase().contains("gm/ml")) {
			er.property_value_units_original = ExperimentalConstants.str_g_mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("g");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("kg/m")) {
			er.property_value_units_original = ExperimentalConstants.str_kg_m3;
			unitsIndex = propertyValue.toLowerCase().indexOf("kg/m");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("g/l")) {
			er.property_value_units_original = ExperimentalConstants.str_g_L;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/l");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("relative")) {
			if (er.source_name.equals(ExperimentalConstants.strSourceEChemPortalAPI)) {
				int relativeIndex = propertyValue.toLowerCase().indexOf("relative");
				int densityIndex = propertyValue.toLowerCase().indexOf("density");
				if (densityIndex>=0 && densityIndex<relativeIndex) {
					unitsIndex = densityIndex;
				} else {
					unitsIndex = relativeIndex;
				}
			} else {
				unitsIndex = propertyValue.length();
			}
			badUnits = false;
			if (propertyValue.toLowerCase().contains("mixture")) {
				er.updateNote(ExperimentalConstants.str_relative_mixture_density);
			} else if (propertyValue.toLowerCase().contains("gas") || propertyValue.toLowerCase().contains("air")) {
				er.updateNote(ExperimentalConstants.str_relative_gas_density);
			} else {
				er.updateNote(ExperimentalConstants.str_relative_density);
			}
		} else {
			er.property_value_units_original = ExperimentalConstants.str_g_cm3;
			if (er.source_name.equals(ExperimentalConstants.strSourceEChemPortalAPI)) {
				unitsIndex = propertyValue.length();
			} else if (propertyValue.contains(":")) {
				unitsIndex = propertyValue.length();
			} else if (propertyValue.contains(" ")) {
				unitsIndex = propertyValue.indexOf(" ");
			} else {
				unitsIndex = propertyValue.length();
			}
			badUnits = false;
			er.updateNote(ExperimentalConstants.str_g_cm3+" assumed");
		}
		boolean foundNumeric = getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		return foundNumeric;
	}

	// Applicable for melting point, boiling point, and flash point
	public static boolean getTemperatureProperty(ExperimentalRecord er,String propertyValue) {
		boolean badUnits = true;
		int unitsIndex = -1;
		String units = getTemperatureUnits(propertyValue);
		if (units.length()!=0) {
			er.property_value_units_original = units;
			unitsIndex = propertyValue.indexOf(units);
			badUnits = false;
		}
		boolean foundNumeric = getNumericalValue(er,propertyValue, unitsIndex,badUnits);
		return foundNumeric;
	}

	public static boolean getWaterSolubility(ExperimentalRecord er,String propertyValue,String sourceName) {
		boolean badUnits = true;
		int unitsIndex = -1;
		propertyValue = propertyValue.replaceAll("([0-9]),([0-9]{3})", "$1$2");
		if (propertyValue.toLowerCase().contains("mg/l")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_L;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mg/ml")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ug/ml") || propertyValue.toLowerCase().contains("µg/ml")) {
			er.property_value_units_original = ExperimentalConstants.str_ug_mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("ug/") == -1 ? propertyValue.toLowerCase().indexOf("µg/") : propertyValue.toLowerCase().indexOf("ug/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ug/l") || propertyValue.toLowerCase().contains("µg/l")) {
			er.property_value_units_original = ExperimentalConstants.str_ug_L;
			unitsIndex = propertyValue.toLowerCase().indexOf("ug/") == -1 ? propertyValue.toLowerCase().indexOf("µg/") : propertyValue.toLowerCase().indexOf("ug/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("g/ml")) {
			er.property_value_units_original = ExperimentalConstants.str_g_mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("g/cm")) {
			er.property_value_units_original = ExperimentalConstants.str_g_cm3;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("g/l")) {
			er.property_value_units_original = ExperimentalConstants.str_g_L;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("kg/m")) {
			er.property_value_units_original = ExperimentalConstants.str_kg_m3;
			unitsIndex = propertyValue.toLowerCase().indexOf("kg/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mg/100")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_100mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("g/100")) {
			er.property_value_units_original = ExperimentalConstants.str_g_100mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("% w/w") || propertyValue.toLowerCase().contains("wt%")) {
			er.property_value_units_original = ExperimentalConstants.str_pctWt;
			unitsIndex = propertyValue.indexOf("%");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("vol%")) {
			er.property_value_units_original = ExperimentalConstants.str_pctVol;
			unitsIndex = propertyValue.indexOf("vol");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("%")) {
			er.property_value_units_original = ExperimentalConstants.str_pct;
			unitsIndex = propertyValue.indexOf("%");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ppm")) {
			er.property_value_units_original = ExperimentalConstants.str_ppm;
			unitsIndex = propertyValue.toLowerCase().indexOf("ppm");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ppb")) {
			er.property_value_units_original = ExperimentalConstants.str_ppb;
			unitsIndex = propertyValue.toLowerCase().indexOf("ppb");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mmol/l")) {
			er.property_value_units_original = ExperimentalConstants.str_mM;
			unitsIndex = propertyValue.toLowerCase().indexOf("mmol");
			badUnits = false;
		} else if (propertyValue.contains("mM")) {
			er.property_value_units_original = ExperimentalConstants.str_mM;
			unitsIndex = propertyValue.indexOf("mM");
			badUnits = false;
		} else if (propertyValue.contains("µM") || propertyValue.contains("uM")) {
			er.property_value_units_original = ExperimentalConstants.str_uM;
			unitsIndex = propertyValue.indexOf("M");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mol/l")) {
			er.property_value_units_original = ExperimentalConstants.str_M;
			unitsIndex = propertyValue.toLowerCase().indexOf("mol");
			badUnits = false;
		} else if (propertyValue.contains("M")) {
			unitsIndex = propertyValue.indexOf("M");
			if (unitsIndex>0) {
				er.property_value_units_original = ExperimentalConstants.str_M;
				badUnits = false;
			}
		} 

		if (er.source_name!=ExperimentalConstants.strSourceOFMPub && unitsIndex < propertyValue.indexOf(":")) {
			unitsIndex = propertyValue.length();
		}

		if (Character.isAlphabetic(propertyValue.charAt(0)) && !(propertyValue.contains("water") || propertyValue.contains("h2o")) &&
				!(propertyValue.contains("ca") || propertyValue.contains("circa") || propertyValue.contains(">") ||
						propertyValue.contains("<") || propertyValue.contains("=") || propertyValue.contains("~"))) {
			er.keep = false;
			er.reason = "Non-aqueous solubility";
		}

		boolean foundNumeric = getNumericalValue(er,propertyValue, unitsIndex,badUnits);
		return foundNumeric;
	}

	public static void getQualitativeSolubility(ExperimentalRecord er, String propertyValue,String sourceName) {
		propertyValue = propertyValue.toLowerCase();
		String solventMatcherStr = "";
		if (sourceName.equals(ExperimentalConstants.strSourceLookChem)) {
			solventMatcherStr = "(([a-zA-Z0-9\s-]+?)(,| and|\\.|\\z|[ ]?\\(|;))?";
		} else if (sourceName.equals(ExperimentalConstants.strSourcePubChem)) {
			solventMatcherStr = "(([a-zA-Z0-9\s,-]+?)(\\.|\\z| at| and only|\\(|;))?";
		}
		Matcher solubilityMatcher = Pattern.compile("(([a-zA-Z]+y[ ]?)?([a-zA-Z]+y[ ]?)?(in|im)?(so[l]?uble|miscible))( (in|with) )?[[ ]?\\.{3}]*"+solventMatcherStr).matcher(propertyValue);
		while (solubilityMatcher.find()) {
			String qualifier = solubilityMatcher.group(1);
			qualifier = qualifier.equals("souble") ? "soluble" : qualifier;
			String prep = solubilityMatcher.group(6);
			String solvent = solubilityMatcher.group(9);
			if (solvent==null || solvent.length()==0 || solvent.contains("water")) {
				er.property_value_qualitative = qualifier;
			} else {
				prep = prep==null ? " " : prep;
				er.updateNote(qualifier + prep + solvent);
			}
		}

		if (propertyValue.contains("reacts") || propertyValue.contains("reaction")) {
			er.property_value_qualitative = "reaction";
		}

		if (propertyValue.contains("hydrolysis") || propertyValue.contains("hydrolyse") || propertyValue.contains("hydrolyze")) {
			er.property_value_qualitative = "hydrolysis";
		}

		if (propertyValue.contains("decompos")) {
			er.property_value_qualitative = "decomposes";
		}

		if (propertyValue.contains("autoignition")) {
			er.property_value_qualitative = "autoignition";
		}

		String[] qualifiers = {"none","very poor","poor","low","negligible","slight","significant","complete"};
		for (String qual:qualifiers) {
			if ((propertyValue.startsWith(qual) || (propertyValue.contains("solubility in water") && propertyValue.contains(qual))) &&
					(er.property_value_qualitative==null || er.property_value_qualitative.isBlank())) {
				er.property_value_qualitative = qual;
			}
		}

		if (er.property_value_qualitative!=null || er.note!=null) {
			er.keep = true;
			er.reason = null;
		}
	}

	public static boolean getVaporPressure(ExperimentalRecord er,String propertyValue) {
		boolean badUnits = true;
		int unitsIndex = -1;
		propertyValue = propertyValue.replaceAll("([0-9]),([0-9]{3})", "$1$2");
		if (propertyValue.toLowerCase().contains("kpa")) {
			er.property_value_units_original = ExperimentalConstants.str_kpa;
			unitsIndex = propertyValue.toLowerCase().indexOf("kpa");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mm")) {
			er.property_value_units_original = ExperimentalConstants.str_mmHg;
			unitsIndex = propertyValue.toLowerCase().indexOf("mm");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("atm")) {
			er.property_value_units_original = ExperimentalConstants.str_atm;
			unitsIndex = propertyValue.toLowerCase().indexOf("atm");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("hpa")) {
			er.property_value_units_original = ExperimentalConstants.str_hpa;
			unitsIndex = propertyValue.toLowerCase().indexOf("hpa");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("pa")) {
			er.property_value_units_original = ExperimentalConstants.str_pa;
			unitsIndex = propertyValue.toLowerCase().indexOf("pa");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mbar")) {
			er.property_value_units_original = ExperimentalConstants.str_mbar;
			unitsIndex = propertyValue.toLowerCase().indexOf("mb");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("bar")) {
			er.property_value_units_original = ExperimentalConstants.str_bar;
			unitsIndex = propertyValue.toLowerCase().indexOf("bar");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("torr")) {
			er.property_value_units_original = ExperimentalConstants.str_torr;
			unitsIndex = propertyValue.toLowerCase().indexOf("torr");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("psi")) {
			er.property_value_units_original = ExperimentalConstants.str_psi;
			unitsIndex = propertyValue.toLowerCase().indexOf("psi");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains(ExperimentalConstants.str_negl)) {
			er.property_value_qualitative = ExperimentalConstants.str_negl;
		}

		if (er.source_name!=ExperimentalConstants.strSourceOFMPub && propertyValue.contains(":")) {
			unitsIndex = propertyValue.length();
		}

		boolean foundNumeric = getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		return foundNumeric;
	}

	public static boolean getHenrysLawConstant(ExperimentalRecord er,String propertyValue) {
		boolean badUnits = true;
		int unitsIndex = -1;
		if (propertyValue.toLowerCase().contains("atm-m3/mole") || propertyValue.toLowerCase().contains("atm m³/mol") ||
				propertyValue.toLowerCase().contains("atm m^3/mol")) {
			er.property_value_units_original = ExperimentalConstants.str_atm_m3_mol;
			unitsIndex = propertyValue.toLowerCase().indexOf("atm");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("pa m³/mol") || propertyValue.toLowerCase().contains("pa m^3/mol")) {
			er.property_value_units_original = ExperimentalConstants.str_Pa_m3_mol;
			unitsIndex = propertyValue.toLowerCase().indexOf("pa");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("dimensionless - vol")) {
			er.property_value_units_original = ExperimentalConstants.str_dimensionless_H_vol;
			unitsIndex = propertyValue.toLowerCase().indexOf("dim");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("dimensionless")) {
			er.property_value_units_original = ExperimentalConstants.str_dimensionless_H;
			unitsIndex = propertyValue.toLowerCase().indexOf("dim");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("atm") && !propertyValue.toLowerCase().contains("mol")) {
			er.property_value_units_original = ExperimentalConstants.str_atm;
			unitsIndex = propertyValue.toLowerCase().indexOf("atm");
			badUnits = false;
		}
		boolean foundNumeric = getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		return foundNumeric;
	}

	public static boolean getToxicity(ExperimentalRecord er,ToxRecordEChemPortalAPI ecpr) {
		String propertyValue = ecpr.value;
		if (propertyValue==null) { return false; }
		boolean badUnits = true;
		int unitsIndex = -1;

		if (propertyValue.toLowerCase().contains("mg/l air")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_L;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mg/m^3 air")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_m3;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ppm")) {
			er.property_value_units_original = ExperimentalConstants.str_ppm;
			unitsIndex = propertyValue.toLowerCase().indexOf("ppm");
			badUnits = false;
		}
		
		if (propertyValue.toLowerCase().contains("nominal")) {
			er.updateNote("nominal units");
		} else if (propertyValue.toLowerCase().contains("analytical")) {
			er.updateNote("analytical units");
		}

		boolean foundNumeric = getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		return foundNumeric;
	}

	public static boolean getToxicity(ExperimentalRecord er,ToxicityRecord tr) {
		String propertyValue = tr.NormalizedDose;
		if (propertyValue == null) { return false; }
		boolean badUnits = true;
		int unitsIndex = -1;

		if (propertyValue.toLowerCase().contains("mg/m3")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_m3;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ml/m3")) {
			er.property_value_units_original = ExperimentalConstants.str_mL_m3;
			unitsIndex = propertyValue.toLowerCase().indexOf("ml/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mg/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_kg;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ml/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_mL_kg;
			unitsIndex = propertyValue.toLowerCase().indexOf("ml/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("iu/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_iu_kg;
			unitsIndex = propertyValue.toLowerCase().indexOf("iu/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("units/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_units_kg;
			unitsIndex = propertyValue.toLowerCase().indexOf("units/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ppm")) {
			er.property_value_units_original = ExperimentalConstants.str_ppm;
			unitsIndex = propertyValue.toLowerCase().indexOf("ppm");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mg")) {
			er.property_value_units_original = ExperimentalConstants.str_mg;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg");
			badUnits = false;
		}
		
		boolean foundNumeric = getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		
		String strippedReportedDose = tr.ReportedDose.replaceAll("\\s","");
		Matcher m = Pattern.compile("\\d").matcher(strippedReportedDose);
		if (m.find()) {
			er.property_value_numeric_qualifier = ParseUtilities.getNumericQualifier(strippedReportedDose,m.start());
		}
		
		if (strippedReportedDose.contains("H")) {
			String strH=strippedReportedDose.substring(strippedReportedDose.lastIndexOf("/")+1,strippedReportedDose.length()-1);
			try {
				double h=Double.parseDouble(strH);
				//use haber's rule that C*t=k (see DOI: 10.1093/toxsci/kfg213 that shows this might not be great approx)	
				if (h!=4.0) {
					er.property_value_point_estimate_original*=h/4.0;
					er.note="Duration: "+ParseUtilities.formatDouble(h)+" H, adjusted to 4 H using Haber's law (conc*time=constant)";
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}			
		}

		return foundNumeric;
	}

	// Applicable for LogKow and pKa
	public static boolean getLogProperty(ExperimentalRecord er,String propertyValue) {
		int unitsIndex = -1;
		if (propertyValue.contains("at")) {
			unitsIndex = propertyValue.indexOf("at");
		} else {
			unitsIndex = propertyValue.length();
		}
		boolean badUnits = false;
		boolean foundNumeric = getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		return foundNumeric;
	}

	/**
	 * Sets the temperature condition for an ExperimentalRecord object, if present
	 * @param er			The ExperimentalRecord object to be updated
	 * @param propertyValue	The string to be read
	 * @return				The temperature condition in C
	 */
	public static void getTemperatureCondition(ExperimentalRecord er, String propertyValue) {
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
					double temp = Double.parseDouble(tempStr);
					switch (units) {
					case "C":
						er.temperature_C = temp;
						break;
					case "F":
						er.temperature_C = (temp-32)*5/9;
						break;
					case "K":
						er.temperature_C = temp-273.15;
						break;
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Sets the pressure condition for an ExperimentalRecord object, if present
	 * @param er			The ExperimentalRecord object to be updated
	 * @param propertyValue	The string to be read
	 * @return				The pressure condition in kPa
	 */
	public static void getPressureCondition(ExperimentalRecord er,String propertyValue,String sourceName) {
		propertyValue = propertyValue.toLowerCase();
		int pressureIndex = -1;
		double conversionFactor = 1.0;
		if (propertyValue.contains("kpa")) {
			pressureIndex = propertyValue.indexOf("kpa");
			conversionFactor = UnitConverter.kPa_to_mmHg;
		} else if (propertyValue.contains("mmhg") || propertyValue.contains("mm hg") || propertyValue.contains("mm")) {
			pressureIndex = propertyValue.indexOf("mm");
		} else if (propertyValue.contains("atm")) {
			pressureIndex = propertyValue.indexOf("atm");
			conversionFactor = UnitConverter.atm_to_mmHg;
		} else if (propertyValue.contains("hpa")) {
			pressureIndex = propertyValue.indexOf("hpa");
			conversionFactor = UnitConverter.hPa_to_mmHg;
		} else if (propertyValue.contains("pa")) {
			pressureIndex = propertyValue.indexOf("pa");
			conversionFactor = UnitConverter.Pa_to_mmHg;
		} else if (propertyValue.contains("mbar")) {
			pressureIndex = propertyValue.indexOf("mb");
			conversionFactor = UnitConverter.hPa_to_mmHg;
		} else if (propertyValue.contains("bar")) {
			pressureIndex = propertyValue.indexOf("bar");
			conversionFactor = UnitConverter.bar_to_mmHg;
		} else if (propertyValue.contains("torr")) {
			pressureIndex = propertyValue.indexOf("torr");
		} else if (propertyValue.contains("psi")) {
			pressureIndex = propertyValue.indexOf("psi");
			conversionFactor = UnitConverter.psi_to_mmHg;
		} 
		// If any pressure units were found, looks for the last number that precedes them
		boolean foundNumeric = false;
		if (pressureIndex > 0) {
			if (sourceName.contains(ExperimentalConstants.strSourceEChemPortal)) {
				if (!foundNumeric) {
					double[] range = extractFirstDoubleRangeFromString(propertyValue,pressureIndex);
					if (range!=null) {
						String min = formatDouble(range[0]*conversionFactor);
						String max = formatDouble(range[1]*conversionFactor);
						er.pressure_mmHg = min+"-"+max;
						foundNumeric = true;
					}
				}
				if (!foundNumeric) {
					double[] range = extractAltFormatRangeFromString(propertyValue,pressureIndex);
					if (range!=null) {
						String min = formatDouble(range[0]*conversionFactor);
						String max = formatDouble(range[1]*conversionFactor);
						er.pressure_mmHg = min+"-"+max;
						foundNumeric = true;
					}
				}
				if (!foundNumeric) {
					try {
						Matcher caMatcher = Pattern.compile(".*?(ca. )?([-]?[ ]?[0-9]*\\.?[0-9]+)( ca. )([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(propertyValue.substring(0,pressureIndex));
						if (caMatcher.find()) {
							String numQual = caMatcher.group(1).isBlank() ? "" : "~";
							String min = formatDouble(Double.parseDouble(caMatcher.group(2)));
							String max = formatDouble(Double.parseDouble(caMatcher.group(4)));
							er.pressure_mmHg = numQual+min+"~"+max;
							foundNumeric = true;
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			if (!foundNumeric) {
				try {
					er.pressure_mmHg = formatDouble(conversionFactor*extractDoubleFromString(propertyValue,pressureIndex));
					foundNumeric = true;
				} catch (NumberFormatException ex) {
					// NumberFormatException means no numerical value was found; leave foundNumeric = false and do nothing else
				}
			}
			if (propertyValue.startsWith("ca.")) {
				er.pressure_mmHg = "~"+er.pressure_mmHg;
			}
		}
	}

	public static String formatDouble(double d) {
		DecimalFormat df2 = new DecimalFormat("0.###");
		DecimalFormat dfSci = new DecimalFormat("0.0##E0");
		if (d < 0.01) {
			return dfSci.format(d);
		} else {
			return df2.format(d);
		}

	}

	/**
	 * Extracts the first range of numbers before a given index in a string
	 * @param str	The string to be read
	 * @param end	The index to stop searching
	 * @return		The range found as a double[2]
	 * @throws IllegalStateException	If no number range is found in the given range
	 */
	public static double[] extractFirstDoubleRangeFromString(String str,int end) throws IllegalStateException {
		Matcher anyRangeMatcher = Pattern.compile("([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]*([—]|[-]{1}|to|ca\\.[\\?])[ ]*([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(str.substring(0,end));
		if (anyRangeMatcher.find()) {
			String strMin = anyRangeMatcher.group(1).replace(" ","");
			String strMax = anyRangeMatcher.group(3).replace(" ","");
			double min = Double.parseDouble(strMin);
			double max = Double.parseDouble(strMax);
			if (min > max) {
				int digits = strMax.length();
				if (digits > strMin.length() || (digits == strMin.length() && strMin.startsWith("-") && strMax.startsWith("-")) || strMax.equals("0")) {
					// Swaps values for negative ranges
					double temp = min;
					min = max;
					max = temp;
				} else {
					// Otherwise replaces substring
					strMax = strMin.substring(0,strMin.length()-digits)+strMax;
					max = Double.parseDouble(strMax);
				}
			}
			double[] range = {min, max};
			return range;
		} else {
			return null;
		}
	}

	public static double[] extractAltFormatRangeFromString(String str,int end) throws IllegalStateException {
		Matcher anyRangeMatcher = Pattern.compile(">[=]?[ ]?([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]?<[=]?[ ]?([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(str.substring(0,end));
		if (anyRangeMatcher.find()) {
			String strMin = anyRangeMatcher.group(1).replace(" ","");
			String strMax = anyRangeMatcher.group(2).replace(" ","");
			double min = Double.parseDouble(strMin);
			double max = Double.parseDouble(strMax);
			if (min >= max) {
				int digits = strMax.length();
				if (digits > strMin.length()) {
					// If max value is smaller but digitwise longer, swaps the values
					double temp = min;
					min = max;
					max = temp;
				} else {
					// Otherwise replaces substring
					strMax = strMin.substring(0,strMin.length()-digits)+strMax;
					max = Double.parseDouble(strMax);
				}
			}
			double[] range = {min, max};
			return range;
		} else {
			return null;
		}
	}

	/**
	 * Extracts the last number before a given index in a string
	 * @param str	The string to be read
	 * @param end	The index to stop searching
	 * @return		The number found as a double
	 * @throws IllegalStateException	If no number is found in the given range
	 */
	public static double extractDoubleFromString(String str,int end) throws NumberFormatException {
		Matcher numberMatcher = Pattern.compile("[-]?[ ]?[0-9]*\\.?[0-9]+").matcher(str.substring(0,end));
		String strDouble = "";
		while (numberMatcher.find()) { 
			strDouble = numberMatcher.group();
		}
		return Double.parseDouble(strDouble.replace(" ",""));
	}

	/**
	 * If the property value string contains temperature units, returns the units in standardized format
	 * @param propertyValue	The string to be read
	 * @return				A standardized temperature unit string from ExperimentalConstants
	 */
	public static String getTemperatureUnits(String propertyValue) {
		propertyValue=propertyValue.replaceAll(" ","");
		String units = "";
		if (propertyValue.contains("°C") || propertyValue.contains("ºC") || propertyValue.contains("oC")
				|| (propertyValue.indexOf("C") > 0 && Character.isDigit(propertyValue.charAt(propertyValue.indexOf("C")-1)))) {
			units = ExperimentalConstants.str_C;
		} else if (propertyValue.contains("°F") || propertyValue.contains("ºF") || propertyValue.contains("oF")
				|| (propertyValue.indexOf("F") > 0 && Character.isDigit(propertyValue.charAt(propertyValue.indexOf("F")-1)))) {
			units = ExperimentalConstants.str_F;
		} else if ((propertyValue.indexOf("K") > 0 && Character.isDigit(propertyValue.charAt(propertyValue.indexOf("K")-1)))) {
			units = ExperimentalConstants.str_K;
		} 
		return units;
	}
	
	public static boolean hasIdentifiers(ExperimentalRecord er) {
		if ((er.casrn==null || er.casrn.isBlank()) && (er.einecs==null || er.einecs.isBlank()) &&
				(er.chemical_name==null || er.chemical_name.isBlank()) && (er.smiles==null || er.smiles.isBlank())) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Strips whitespace and leading zero from CAS RN
	 * @param cas	CAS RN to fix
	 * @return		Fixed CAS RN
	 */
	public static String fixCASLeadingZero(String cas) {
		if (cas!=null && !cas.isBlank()) {
			cas=cas.trim();
			while (cas.substring(0,1).contentEquals("0")) {//trim off zeros at front
				cas=cas.substring(1,cas.length());
			}
			return cas;
		} else {
			return null;
		}
	}
	
	/**
	 * Verifies CAS checksum per https://www.cas.org/support/documentation/chemical-substances/checkdig
	 * 
	 * @param casInput	CAS RN (or pipe-delimited sequence of multiple CAS RNs)
	 * @return			True if checksum holds for all CAS RNs in input; false otherwise
	 */
	public static boolean isValidCAS(String casInput) {
		String[] casArray = casInput.split("\\|");
		boolean valid = true;
		for (String cas:casArray) {
			String casTemp = cas.replaceAll("[^0-9]","");
			int len = casTemp.length();
			if (len > 10) { return false; }
			int check = Character.getNumericValue(casTemp.charAt(len-1));
			int sum = 0;
			for (int i = 1; i <= len-1; i++) {
				sum += i*Character.getNumericValue(casTemp.charAt(len-1-i));
			}
			if (sum % 10 != check) {
				valid = false;
				break;
			}
			// There are no valid CAS RNs with bad formatting in the current data set, but if that happens in other sources, could add format correction here
//			else if (!cas.contains("-")) {
//				System.out.println("Valid CAS with bad format: "+cas);
//			}
		}
		return valid;
	}

	public static String reverseFixChars(String str) {
		str=str.replace("^0","\u2070");// superscript 0
		str=str.replace("^1","\u00B9");// superscript 1
		str=str.replace("^2","\u00B2");// superscript 2
		str=str.replace("^3","\u00B3");// superscript 3
		str=str.replace("^4","\u2074");// superscript 4
		str=str.replace("^5","\u2075");// superscript 5
		str=str.replace("^6","\u2076");// superscript 6
		str=str.replace("^7","\u2077");// superscript 7
		str=str.replace("^8","\u2078");// superscript 8
		str=str.replace("^9","\u2079");// superscript 9
		str=str.replace("_0","\u2080");// subscript 0
		str=str.replace("_1","\u2081");// subscript 1
		str=str.replace("_2","\u2082");// subscript 2
		str=str.replace("_3","\u2083");// subscript 3
		str=str.replace("_4","\u2084");// subscript 4
		str=str.replace("_5","\u2085");// subscript 5
		str=str.replace("_6","\u2086");// subscript 6
		str=str.replace("_7","\u2087");// subscript 7
		str=str.replace("_8","\u2088");// subscript 8
		str=str.replace("_9","\u2089");// subscript 9
		return str;
	}

	public static String fixChars(String str) {
		str=str.replace("Ã¢â¬â","-").replace("Ã¢â¬â¢","'");
		str=str.replace("\uff08", "(");// Ã¯Â¼Ë
		str=str.replace("\uff09", ")");// Ã¯Â¼â°
		str=str.replace("\uff0f", "/");// Ã¯Â¼ï¿½
		str=str.replace("\u3000", " ");//blank
		str=str.replace("\u00a0", " ");//blank
		str=str.replace("\u2003", " ");//blank
		str=str.replace("\u0009", " ");//blank
		str=str.replace("\u300c", "");// Ã£â¬Å
		str=str.replace("\u300d", "");// Ã£â¬ï¿½
		str=str.replace("\u2070", "^0");// superscript 0
		str=str.replace("\u00B9", "^1");// superscript 1
		str=str.replace("\u00B2", "^2");// superscript 2
		str=str.replace("\u00B3", "^3");// superscript 3
		str=str.replace("\u2074", "^4");// superscript 4
		str=str.replace("\u2075", "^5");// superscript 5
		str=str.replace("\u2076", "^6");// superscript 6
		str=str.replace("\u2077", "^7");// superscript 7
		str=str.replace("\u2078", "^8");// superscript 8
		str=str.replace("\u2079", "^9");// superscript 9
		str=str.replace("\u2080", "_0");// subscript 0
		str=str.replace("\u2081", "_1");// subscript 1
		str=str.replace("\u2082", "_2");// subscript 2
		str=str.replace("\u2083", "_3");// subscript 3
		str=str.replace("\u2084", "_4");// subscript 4
		str=str.replace("\u2085", "_5");// subscript 5
		str=str.replace("\u2086", "_6");// subscript 6
		str=str.replace("\u2087", "_7");// subscript 7
		str=str.replace("\u2088", "_8");// subscript 8
		str=str.replace("\u2089", "_9");// subscript 9
	
		return str;
	}

}
