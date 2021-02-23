package gov.epa.exp_data_gathering.parse;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.Chemidplus.RecordChemidplus.ToxicityRecord;

public class ParseUtilities extends Parse {

	public static boolean getNumericalValue(ExperimentalRecord er, String propertyValue, int unitsIndex, boolean badUnits) {
		Pattern tempPattern = Pattern.compile("[0-9.]+ ?\\u00B0 ?[CcFfKk]");
		if (badUnits) { unitsIndex = propertyValue.length(); }
		if (propertyValue.contains("±")) { unitsIndex = Math.min(propertyValue.indexOf("±"),unitsIndex); }
		if (!er.property_name.equals(ExperimentalConstants.strMeltingPoint) && !er.property_name.equals(ExperimentalConstants.strBoilingPoint) &&
				!er.property_name.equals(ExperimentalConstants.strFlashPoint) && unitsIndex==propertyValue.length()) {
			Matcher tempMatcher = tempPattern.matcher(propertyValue);
			if (tempMatcher.find()) {
				unitsIndex = tempMatcher.start();
			}
		}
		boolean foundNumeric = false;
		try {
			Matcher sciMatcher = Pattern.compile("([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]?(e|x[ ]?10\\^?|\\*?10\\^)[ ]?[\\(]?([-|\\+]?[ ]?[0-9]+)[\\)]?").matcher(propertyValue.toLowerCase().substring(0,unitsIndex));
			if (sciMatcher.find()) {
				String strMantissa = sciMatcher.group(1);
				String strMagnitude = sciMatcher.group(3);
				Double mantissa = Double.parseDouble(strMantissa.replaceAll("\\s",""));
				Double magnitude =  Double.parseDouble(strMagnitude.replaceAll("\\s","").replaceAll("\\+", ""));
				int propertyValueIndex;
				if (!badUnits) {
					foundNumeric = true;
					er.property_value_point_estimate_original = mantissa*Math.pow(10, magnitude);
					if (propertyValue.indexOf(strMantissa) > 0) {
						String checkSymbol = StringEscapeUtils.unescapeHtml4(propertyValue.replaceAll("\\s",""));
						propertyValueIndex = checkSymbol.indexOf(strMantissa);
						er.property_value_numeric_qualifier = getNumericQualifier(checkSymbol,propertyValueIndex);
					}
				}
			}
		} catch (Exception ex) {
			System.out.println(propertyValue);
			ex.printStackTrace();
		}

		if (!foundNumeric) {
			try {
				double[] range = extractFirstDoubleRangeFromString(propertyValue,unitsIndex);
				if (!badUnits && range!=null) {
					if (range[0]<=range[1]) {
						er.property_value_min_original = range[0];
						er.property_value_max_original = range[1];
						foundNumeric = true;
					} else {
						er.keep = false;
						er.reason = "Failed range correction";
					}
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
				propertyValue = StringEscapeUtils.unescapeHtml4(propertyValue);
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
			} else if (str.charAt(index-1)=='?') {
				symbol = "?";
			} else if (str.charAt(index-1)=='<') {
				symbol = "<";
			} else if (str.charAt(index-1)=='~' || str.contains("ca.") || str.contains("circa") || str.contains("approx")) {
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
	
	private static boolean containsNumber(String str) {
		if (str==null) { return false; }
		Matcher anyNumber = Pattern.compile(".*\\d.*").matcher(str);
		return anyNumber.find();
	}
	
	private static String adjustPropertyValue(ExperimentalRecord er,String propertyValue) {
		propertyValue = propertyValue.replaceAll("([0-9]+%-)?[0-9]+% (m?ethanol|alcohol|EtOH|alc)", "$2"); // Alcohol percentages confuse the parser, so snip them out
		propertyValue = propertyValue.replaceAll("[0-9]+% (M?ETHANOL|ALCOHOL)", "$1"); // Some PubChem records in all caps
		propertyValue = propertyValue.replaceAll("[0-9.]+ ?(M|N) (NaOH|HCl)", "$1 $2"); // Acid/base molarities confuse the parser, so snip them out
		propertyValue = propertyValue.replaceAll(" [Pp][Ee][Rr] ","/"); // Correct usage of "per" in some PubChem records
		
		String[] badSolvents = {"ether","benzene","naoh","hcl","chloroform","ligroin","acet","alc","dmso","dimethyl sulfoxide","etoh","hexane","meoh",
				"dichloromethane","dcm","toluene","glyc","oils","organic solvent","dmf","et2o","etoac","mcoh","chc1","xylene","dioxane","hydrocarbon","kerosene",
				"acid","oxide","pyri","carbon tetrachloride","pet","anol","ch3oh","c2h5oh","ch2cl2","chcl3","alkali","dsmo","dma","buffer","ammonia water","pgmea",
				"water-ethanol solution","cs2","ethylene dichloride","mineral oil","hydrochloric"};
		boolean foundWater = false;
		String[] waterSynonyms = {"water (distilled)","water","h2o","aqueous solution"};
		for (String solvent:badSolvents) {
			if (!propertyValue.toLowerCase().contains(solvent)) { continue; }
			if (solvent.equals("alc") && propertyValue.toLowerCase().contains("calc")) { continue; } // For obvious reasons
			// Snip out any easily-recognized entries for other solvents
			propertyValue = propertyValue.replaceAll(solvent+": ([ <>~=\\.0-9MmGguLl/@%\\(\\)\\u00B0CcFKPpHh]+)[;,$]","");
			// Check for non-aqueous solvents
			if (er.chemical_name==null || !er.chemical_name.contains(solvent)) {
				foundWater = false;
				// See if there is an aqueous record too
				for (String water:waterSynonyms) {
					// Stop searching if water synonym already found
					if (foundWater) { continue; }
					// If water synonym found, parse out aqueous entry
					if (propertyValue.toLowerCase().contains(water) && !solvent.contains(water)) { // Handles "ammonia water" and similar
						if (propertyValue.toLowerCase().contains("% "+water) || 
								propertyValue.toLowerCase().contains("/"+water) || 
								propertyValue.toLowerCase().contains(water+"/") ||
								propertyValue.toLowerCase().contains("?"+water) ||
								propertyValue.toLowerCase().contains("ethanol in "+water) ||
								propertyValue.toLowerCase().contains("etoh in "+water) ||
								propertyValue.toLowerCase().contains("ethanol: "+water) ||
								propertyValue.toLowerCase().contains("acidified")) { continue; } // Ignores records with water mixtures
						foundWater = true;
						boolean parsed = false;
						if (!parsed) {
							Matcher colonFormat1 = Pattern.compile(water+"( solubility)?(: |[ ]?=[ ]?)([ <>~=\\.0-9MmGguLl/@%()\u00B0CcFfKkPpHh]+)[;,$]")
									.matcher(propertyValue.toLowerCase().trim());
							if (colonFormat1.find() && containsNumber(colonFormat1.group(3))) {
								propertyValue = colonFormat1.group(3);
								parsed = true;
//								er.updateNote("Aqueous entry: "+propertyValue);
							}
						}
						if (!parsed) {
							Matcher colonFormat2 = Pattern.compile("solubility: ([ <>~=\\.0-9MmGguLl/@%()\\u00B0CcFfKkPpHh]+)\\("+water+"\\)[;,$]")
									.matcher(propertyValue.toLowerCase().trim());
							if (colonFormat2.find() && containsNumber(colonFormat2.group())) {
								propertyValue = colonFormat2.group();
								parsed = true;
//								er.updateNote("Aqueous entry: "+propertyValue);
							}
						}
						if (!parsed) {
							Matcher colonFormat3 = Pattern.compile("@ [0-9]+ \\u00B0[cfk]: "+water+"[ <>~=.*0-9XxMmGguLlat/@%()\\u00B0CcFfKkPpHhWwTtVvOoLl+-]*")
									.matcher(propertyValue.toLowerCase().trim());
							if (colonFormat3.find() && containsNumber(colonFormat3.group())) {
								propertyValue = colonFormat3.group();
								parsed = true;
//								er.updateNote("Aqueous entry: "+propertyValue);
							}
						}
						if (!parsed) {
							Matcher inWaterFormat1 = Pattern.compile("([<>=~?]{0,2} ?[0-9.]+[ <>~=.*0-9XxMmGguLlat/@%()\\u00B0CcFfKkPpHhWwTtVvOoznLl+-]* ?(g )?(in)? ?)"
									+water+":?( ?(@|at)? ?[ <>~=0-9MmGgLl/@%()\\u00B0CcFfKPpHh.]+)?")
									.matcher(propertyValue.toLowerCase().trim());
							if (inWaterFormat1.find() && containsNumber(inWaterFormat1.group())) {
								Matcher volMatcher = Pattern.compile("[0-9]+ ?ml ?"+water).matcher(inWaterFormat1.group());
								if (!volMatcher.matches()) {
									propertyValue = inWaterFormat1.group().replaceAll(":","");
									parsed = true;
//									er.updateNote("Aqueous entry: "+propertyValue);
								}
							}
						}
						if (!parsed) {
							Matcher inWaterFormat2 = Pattern.compile("sol(ubility)? in "+water+"([^:]*: | )([<>~=]{0,2}[0-9.]+[ <>~=.*0-9XxMmGguLlat/@%()\\u00B0CcFfKkPpHhWwTtVvOoLl+-]*)( in)?")
									.matcher(propertyValue.toLowerCase().trim());
							if (inWaterFormat2.find() && inWaterFormat2.group(4)==null) {
								propertyValue = inWaterFormat2.group(3);
								parsed = true;
//								er.updateNote("Aqueous entry: "+propertyValue);
							}
						}
						if (!parsed) {
							Matcher inWaterFormat3 = Pattern.compile("(in )?"+water+"(( ?[(]?(@|at)? ?[0-9.]+ ?\\u00B0[CcFfKk])?[)]?,?:? ([^).,;$]*((?<=[0-9])[\\.,][0-9])?([^).;$]|,(?! ))*)*[).,;$[^\\p{Graph}]])")
									.matcher(propertyValue.toLowerCase().trim());
							if (inWaterFormat3.find() && containsNumber(inWaterFormat3.group())) {
								propertyValue = inWaterFormat3.group();
								parsed = true;
//								er.updateNote("Aqueous entry: "+propertyValue);
							}
						}
						if (!parsed) {
							Matcher qualMatcher = Pattern.compile("(ly |in)sol(uble)? in (cold |hot |warm |boiling )?"+water).matcher(propertyValue.toLowerCase());
							if (qualMatcher.find()) {
								return null;
							}
						}
						if (!parsed) {
							er.keep = false;
							er.reason = "Bad data or units";
						}
					}
				}
				// If no water synonyms found, mark record as bad
				if (!foundWater) {
					er.keep = false;
					er.reason = "Non-aqueous solubility";
				}
			}
		}
		return propertyValue;
	}

	public static boolean getWaterSolubility(ExperimentalRecord er,String propertyValue,String sourceName) {
		if (propertyValue==null) { return false; }
		
		// Don't waste time looking for a numerical entry if there are no numbers in the string!
		if (!containsNumber(propertyValue)) { return false; }
		if (propertyValue.toLowerCase().contains("parts")) { return false; } // No way to interpret mass vs. volume vs. molar for "parts" ratio records
		if (propertyValue.toLowerCase().contains("water (6:3:1)")) { return false; } // Weird entry with a water mixture
		
		propertyValue = propertyValue.replaceAll("([0-9]),([0-9]{3})", "$1$2");
		propertyValue = propertyValue.replaceAll("([0-9]),([0-9])", "$1\\.$2");
		propertyValue = propertyValue.replaceAll("([Hh])20", "$12O"); // Yes, "H20" instead of "H2O" has actually caused problems for the parser...
		propertyValue = correctDegreeSymbols(propertyValue);
		
		if (er.source_name.equals(ExperimentalConstants.strSourcePubChem) || 
				er.source_name.equals(ExperimentalConstants.strSourceLookChem) ||
				er.source_name.equals(ExperimentalConstants.strSourceEChemPortalAPI) ||
				er.source_name.equals(ExperimentalConstants.strSourceChemicalBook)) {
			if (getTextSolubility(er,propertyValue)) { return true; }
			if (getSimpleNumericSolubility(er,propertyValue)) { return true; }
			String adjustedPropertyValue = adjustPropertyValue(er,propertyValue);
			if (adjustedPropertyValue==null) {
				return false;
			} else {
				propertyValue = adjustedPropertyValue;
			}
		}
		
		boolean badUnits = true;
		int unitsIndex = getSolubilityUnits(er,propertyValue);
		if (unitsIndex > -1) { badUnits = false; }
		
		if (badUnits && propertyValue.length() < er.property_value_string.length()) {
			int candidateIndex = getSolubilityUnits(er,er.property_value_string);
			if (candidateIndex > -1) {
				unitsIndex = propertyValue.length();
				badUnits = false;
			}
		}

		if (er.source_name!=ExperimentalConstants.strSourceOFMPub && unitsIndex < propertyValue.indexOf(":")) {
			unitsIndex = propertyValue.length();
		}

		boolean foundNumeric = false;
		if (er.keep) {
			foundNumeric = getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		}
		return foundNumeric;
	}
	
	private static int getSolubilityUnits(ExperimentalRecord er, String propertyValue) {
		int unitsIndex = -1;
		if (propertyValue.toLowerCase().contains("mg/ml")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
		} else if (propertyValue.toLowerCase().contains("mg/l") || propertyValue.toLowerCase().contains("mg l-1") || propertyValue.toLowerCase().contains("mgl-1") ||
				(propertyValue.toLowerCase().contains("mg/1") && !propertyValue.toLowerCase().contains("mg/10"))) {
			er.property_value_units_original = ExperimentalConstants.str_mg_L;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg");
		} else if (propertyValue.toLowerCase().contains("ug/ml") || propertyValue.toLowerCase().contains("\u00B5g/ml")) {
			er.property_value_units_original = ExperimentalConstants.str_ug_mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("ug/") == -1 ? propertyValue.toLowerCase().indexOf("\u00B5g/") : propertyValue.toLowerCase().indexOf("ug/");
		} else if (propertyValue.toLowerCase().contains("ug/l") || propertyValue.toLowerCase().contains("\u00B5g/l")) {
			er.property_value_units_original = ExperimentalConstants.str_ug_L;
			unitsIndex = propertyValue.toLowerCase().indexOf("ug/") == -1 ? propertyValue.toLowerCase().indexOf("\u00B5g/") : propertyValue.toLowerCase().indexOf("ug/");
		} else if (propertyValue.toLowerCase().contains("g/ml")) {
			er.property_value_units_original = ExperimentalConstants.str_g_mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/");
		} else if (propertyValue.toLowerCase().contains("g/cm")) {
			er.property_value_units_original = ExperimentalConstants.str_g_cm3;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/");
		} else if (propertyValue.toLowerCase().contains("g/l")) {
			er.property_value_units_original = ExperimentalConstants.str_g_L;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/");
		} else if (propertyValue.toLowerCase().contains("kg/m")) {
			er.property_value_units_original = ExperimentalConstants.str_kg_m3;
			unitsIndex = propertyValue.toLowerCase().indexOf("kg/");
		} else if (propertyValue.toLowerCase().contains("kg/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_kg_kg_H20;
			unitsIndex = propertyValue.toLowerCase().indexOf("kg/");
		} else if (propertyValue.toLowerCase().contains("g/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_g_kg_H20;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/");
		} else if (propertyValue.toLowerCase().contains("mg/100ml") || propertyValue.toLowerCase().contains("mg/100 ml")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_100mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
		} else if (propertyValue.toLowerCase().contains("mg/10ml") || propertyValue.toLowerCase().contains("mg/10 ml")
				|| propertyValue.toLowerCase().contains("mg/dl")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_10mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
		} else if (propertyValue.toLowerCase().contains("ug/100ml") || propertyValue.toLowerCase().contains("ug/100 ml")) {
			er.property_value_units_original = ExperimentalConstants.str_ug_100mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("ug/");
		} else if (propertyValue.toLowerCase().contains("g/100ml") || propertyValue.toLowerCase().contains("g / 100ml")
				 || propertyValue.toLowerCase().contains("g/ 100 ml") || propertyValue.toLowerCase().contains("g / 100 ml")
				 || propertyValue.toLowerCase().contains("g / 100 cc") || propertyValue.toLowerCase().contains("g/100 ml")
				 || propertyValue.toLowerCase().contains("g/100 cc")) {
			er.property_value_units_original = ExperimentalConstants.str_g_100mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("/");
		} else if (propertyValue.toLowerCase().contains("mg/100g") || propertyValue.toLowerCase().contains("mg / 100g")
				 || propertyValue.toLowerCase().contains("mg/ 100 g") || propertyValue.toLowerCase().contains("mg / 100 g")
				 || propertyValue.toLowerCase().contains("mg/100 g")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_100g;
			unitsIndex = propertyValue.toLowerCase().indexOf("/");
		} else if (propertyValue.toLowerCase().contains("g/100g") || propertyValue.toLowerCase().contains("g / 100g")
				 || propertyValue.toLowerCase().contains("g/ 100 g") || propertyValue.toLowerCase().contains("g / 100 g")
				 || propertyValue.toLowerCase().contains("g/100 g")) {
			er.property_value_units_original = ExperimentalConstants.str_g_100g;
			unitsIndex = propertyValue.toLowerCase().indexOf("/");
		} else if (propertyValue.toLowerCase().contains("g/10ml")  || propertyValue.toLowerCase().contains("g/10 ml")) {
			er.property_value_units_original = ExperimentalConstants.str_g_10mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/");
		} else if (propertyValue.toLowerCase().contains("% w/w") || propertyValue.toLowerCase().contains("wt%") || propertyValue.toLowerCase().contains("wt percent")
				 || propertyValue.toLowerCase().contains("% wt") || propertyValue.toLowerCase().contains("% (w/w)") ||
				 propertyValue.toLowerCase().contains("weight %") || propertyValue.toLowerCase().contains("wt %") ||
				 propertyValue.toLowerCase().contains("% by wt") ||
				 (propertyValue.toLowerCase().contains("%") && propertyValue.toLowerCase().contains("wt"))) {
			er.property_value_units_original = ExperimentalConstants.str_pctWt;
			unitsIndex = Math.max(propertyValue.indexOf("%"),propertyValue.indexOf("percent"));
		} else if (propertyValue.toLowerCase().contains("vol%")) {
			er.property_value_units_original = ExperimentalConstants.str_pctVol;
			unitsIndex = propertyValue.indexOf("vol");
		} else if (propertyValue.toLowerCase().contains("%")) {
			er.property_value_units_original = ExperimentalConstants.str_pct;
			unitsIndex = propertyValue.indexOf("%");
		} else if (propertyValue.toLowerCase().contains("ppm")) {
			er.property_value_units_original = ExperimentalConstants.str_ppm;
			unitsIndex = propertyValue.toLowerCase().indexOf("ppm");
		} else if (propertyValue.toLowerCase().contains("ppb")) {
			er.property_value_units_original = ExperimentalConstants.str_ppb;
			unitsIndex = propertyValue.toLowerCase().indexOf("ppb");
		} else if (propertyValue.toLowerCase().contains("mmol/l")) {
			er.property_value_units_original = ExperimentalConstants.str_mM;
			unitsIndex = propertyValue.toLowerCase().indexOf("mmol");
		} else if (checkMolarUnits(ExperimentalConstants.str_mM,propertyValue) > -1) {
			er.property_value_units_original = ExperimentalConstants.str_mM;
			unitsIndex = checkMolarUnits(ExperimentalConstants.str_mM,propertyValue);
		} else if (checkMolarUnits(ExperimentalConstants.str_uM,propertyValue) > -1) {
			er.property_value_units_original = ExperimentalConstants.str_uM;
			unitsIndex = checkMolarUnits(ExperimentalConstants.str_uM,propertyValue);
		} else if (propertyValue.toLowerCase().contains("mol/l") || propertyValue.toLowerCase().contains("mols/l")) {
			er.property_value_units_original = ExperimentalConstants.str_M;
			unitsIndex = propertyValue.toLowerCase().indexOf("mol");
		} else if (checkMolarUnits(ExperimentalConstants.str_M,propertyValue) > -1) {
			er.property_value_units_original = ExperimentalConstants.str_M;
			unitsIndex = checkMolarUnits(ExperimentalConstants.str_M,propertyValue);
		} else if (propertyValue.toLowerCase().contains("oz/gallon")) {
			er.property_value_units_original = ExperimentalConstants.str_oz_gal;
			unitsIndex = propertyValue.toLowerCase().indexOf("oz/");
		} else if (er.source_name==ExperimentalConstants.strSourceEChemPortalAPI) {
			unitsIndex = ExtraEChemPortalRecords(propertyValue, er, unitsIndex);
		}
		
		return unitsIndex;
	}
	
	private static int checkMolarUnits(String units,String propertyValue) {
		int index = -1;
		int checkUnitsLength = -1;
		switch (units) {
		case ExperimentalConstants.str_mM:
			index = propertyValue.toLowerCase().indexOf("mm");
			checkUnitsLength = 2;
			break;
		case ExperimentalConstants.str_uM:
			index = Math.max(propertyValue.toLowerCase().indexOf("um"),propertyValue.toLowerCase().indexOf("\u00B5m"));
			checkUnitsLength = 2;
			break;
		case ExperimentalConstants.str_M:
			index = propertyValue.toLowerCase().indexOf("m");
			checkUnitsLength = 1;
			break;
		}
		
		if (index==-1) { return index; }
		
		boolean standalone = false;
		boolean before = index > 0 && (Character.isDigit(propertyValue.charAt(index-1)) || Character.isWhitespace(propertyValue.charAt(index-1)));
		boolean after = (index+checkUnitsLength < propertyValue.length() && !Character.isAlphabetic(propertyValue.charAt(index+checkUnitsLength)))
				|| index == propertyValue.length()-1;
		standalone = before && after;
		
		if (standalone) { 
			return index;
		} else {
			return -1;
		}
	}
	
	private static int ExtraEChemPortalRecords(String propertyValue, ExperimentalRecord er, int unitsIndex) {
		if (propertyValue.toLowerCase().contains("mg/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_kg_H20;
			return unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
		} else if (propertyValue.toLowerCase().contains("g/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_g_kg_H20;
			return unitsIndex = propertyValue.toLowerCase().indexOf("g/");
		} else if (propertyValue.toLowerCase().contains("mol/m3")) {
			er.property_value_units_original = ExperimentalConstants.str_mol_m3_H20;
			return unitsIndex = propertyValue.toLowerCase().indexOf("mol");
		}
		Matcher microGperG = Pattern.compile("(\\u00B5g/g)").matcher(propertyValue.trim());
		if (microGperG.find()) {
//			System.out.println(microGperG.group(1));
			er.property_value_units_original = ExperimentalConstants.str_ug_g_H20;
			return propertyValue.toLowerCase().indexOf(microGperG.group(1));
		}
		Matcher microGAIperG = Pattern.compile("(\\u00B5g[ ]?a.i./)").matcher(propertyValue.trim());
		if (microGAIperG.find()) {
//			System.out.println(microGAIperG.group(1));
			er.property_value_units_original = ExperimentalConstants.str_ug_L;
			return propertyValue.toLowerCase().indexOf(microGAIperG.group(1));
		}
		Matcher microGpermL = Pattern.compile("(micrograms(per mL)?(/milliliter)?)").matcher(propertyValue.trim());
		if (microGpermL.find()) {
//			System.out.println(microGpermL.group(1));
			er.property_value_units_original = ExperimentalConstants.str_ug_mL;
			return propertyValue.toLowerCase().indexOf(microGpermL.group(1));
		}
		return unitsIndex;
	}
	
	
	public static void getQualitativeSolubility(ExperimentalRecord er, String propertyValue,String sourceName) {
		boolean gotQualitativeSolubility = false;
		String propertyValue1 = propertyValue.toLowerCase();
		String solventMatcherStr = "";
		if (sourceName.equals(ExperimentalConstants.strSourceLookChem)) {
			solventMatcherStr = "(([a-zA-Z0-9\s-%]+?)(,| and|\\.|\\z|[ ]?\\(|;))?";
		} else if (sourceName.equals(ExperimentalConstants.strSourcePubChem)) {
			solventMatcherStr = "(([a-zA-Z0-9\s,-]+?)(\\.|\\z| at| and only|\\(|;|[0-9]))?";
		}
		Matcher solubilityMatcher = Pattern.compile("(([a-zA-Z]+y[ ]?)?([a-zA-Z]+y[ ]?)?(in|im)?(so(uble|luble|l)]?|miscible))( [\\(]?(in|with) )?[[ ]?\\.{3}]*"+solventMatcherStr).matcher(propertyValue1);
		while (solubilityMatcher.find()) {
			String qualifier = solubilityMatcher.group(1);
			qualifier = qualifier.replaceAll("souble","soluble");
			String prep = solubilityMatcher.group(7);
			String solvent = solubilityMatcher.group(10);
			if (solvent==null || solvent.length()==0 || (solvent.contains("water") && !solvent.contains("alc")) || (
					solvent.contains("aqueous solution") && !solvent.contains("alkaline"))) {
				er.property_value_qualitative = qualifier;
				gotQualitativeSolubility = true;
			} else {
				prep = prep==null ? " " : prep;
				er.updateNote(qualifier + prep + solvent);
				gotQualitativeSolubility = true;
			}
		}
		
		if (er.note!=null) { er.note = er.note.replaceAll("[;.,];", ";"); } // Regex leaves double punctuation sometimes

		if (propertyValue1.contains("reacts") || propertyValue1.contains("reaction")) {
			er.property_value_qualitative = "reaction";
			gotQualitativeSolubility = true;
		}

		if (propertyValue1.contains("hydrolysis") || propertyValue1.contains("hydrolyse") || propertyValue1.contains("hydrolyze")) {
			er.property_value_qualitative = "hydrolysis";
			gotQualitativeSolubility = true;
		}

		if (propertyValue1.contains("decompos")) {
			er.property_value_qualitative = "decomposes";
			gotQualitativeSolubility = true;
		}

		if (propertyValue1.contains("autoignition")) {
			er.property_value_qualitative = "autoignition";
			gotQualitativeSolubility = true;
		}

		String[] qualifiers = {"none","very poor","poor","low","negligible","slight","significant","complete"};
		for (String qual:qualifiers) {
			if ((propertyValue1.startsWith(qual) || (propertyValue1.contains("solubility in water") && propertyValue1.contains(qual))) &&
					(er.property_value_qualitative==null || er.property_value_qualitative.isBlank())) {
				er.property_value_qualitative = qual;
				gotQualitativeSolubility = true;
			}
		}

		if (gotQualitativeSolubility && er.reason!=null && !er.reason.toLowerCase().contains("non-aqueous solubility")) {
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
		if (propertyValue.toLowerCase().contains("atm-m3/mole") || propertyValue.toLowerCase().contains("atm m�/mol") ||
				propertyValue.toLowerCase().contains("atm m^3/mol")) {
			er.property_value_units_original = ExperimentalConstants.str_atm_m3_mol;
			unitsIndex = propertyValue.toLowerCase().indexOf("atm");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("pa m�/mol") || propertyValue.toLowerCase().contains("pa m^3/mol")) {
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

	public static boolean getToxicity(ExperimentalRecord er,String propertyValue) {
		if (propertyValue==null) { return false; }
		boolean badUnits = true;
		int unitsIndex = -1;

		if (propertyValue.toLowerCase().contains("mg/l")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_L;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mg/m^3") || propertyValue.toLowerCase().contains("mg/cu m")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_m3;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("g/m^3") || propertyValue.toLowerCase().contains("g/m�") || propertyValue.toLowerCase().contains("g/m3") ||
				propertyValue.toLowerCase().contains("g/cubic meter")) {
			er.property_value_units_original = ExperimentalConstants.str_g_m3;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ml/m3")) {
			er.property_value_units_original = ExperimentalConstants.str_mL_m3;
			unitsIndex = propertyValue.toLowerCase().indexOf("ml/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ppm")) {
			er.property_value_units_original = ExperimentalConstants.str_ppm;
			unitsIndex = propertyValue.toLowerCase().indexOf("ppm");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mg/kg") || propertyValue.toLowerCase().contains("mg /kg")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_kg;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("g/kg") || propertyValue.toLowerCase().contains("gm/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_g_kg;
			unitsIndex = propertyValue.toLowerCase().indexOf("g");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ml/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_mL_kg;
			unitsIndex = propertyValue.toLowerCase().indexOf("ml/");
			badUnits = false;
		} else {
			badUnits = true;
			er.keep = false;
			er.reason = "Unhandled units";
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
		propertyValue = correctDegreeSymbols(propertyValue);
		String units = getTemperatureUnits(propertyValue);
		int tempIndex = propertyValue.indexOf(units);
		if (tempIndex==propertyValue.toLowerCase().indexOf("cc")) {
			tempIndex = propertyValue.indexOf(units,tempIndex+2);
		}
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
					case ExperimentalConstants.str_C:
						er.temperature_C = temp;
						break;
					case ExperimentalConstants.str_F:
						er.temperature_C = UnitConverter.F_to_C(temp);
						break;
					case ExperimentalConstants.str_K:
						er.temperature_C = UnitConverter.K_to_C(temp);
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
		Matcher anyRangeMatcher = Pattern.compile("([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]*([—]|[-]{1}|to|ca\\.|[\\?])[ ]*([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(str.substring(0,end));
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
					try {
						max = Double.parseDouble(strMax);
					} catch (Exception ex) {
						System.out.println("Failed range correction: "+str);
					}
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
	 * @param propertyValue1	The string to be read
	 * @return				A standardized temperature unit string from ExperimentalConstants
	 */
	public static String getTemperatureUnits(String propertyValue) {
		String propertyValue1=propertyValue.replaceAll("[ �]","");
		propertyValue1 = correctDegreeSymbols(propertyValue1);
		String units = "";
		if (propertyValue1.contains("\u00B0C") || propertyValue1.contains("oC") || propertyValue1.contains("deg.C")
				|| (propertyValue1.indexOf("C") > 0 && Character.isDigit(propertyValue1.charAt(propertyValue1.indexOf("C")-1))
						&& !propertyValue1.contains("CC"))) {
			units = ExperimentalConstants.str_C;
		} else if (propertyValue1.contains("\u00B0F") || propertyValue1.contains("oF")
				|| (propertyValue1.indexOf("F") > 0 && Character.isDigit(propertyValue1.charAt(propertyValue1.indexOf("F")-1)))) {
			units = ExperimentalConstants.str_F;
		} else if ((propertyValue1.indexOf("K") > 0 && Character.isDigit(propertyValue1.charAt(propertyValue1.indexOf("K")-1)))) {
			units = ExperimentalConstants.str_K;
		} 
		return units;
	}
	
	public static String correctDegreeSymbols(String s) {
		StringBuilder sb = new StringBuilder(s);
		replaceAll(sb,"[\u00BA\u1D52\u02DA\u309C\u18DE\u2070\u2218\u29B5\u1BC8\u26AC]","\u00B0");
		replaceAll(sb,"\u2103","\u00B0C");
		replaceAll(sb,"\u2109","\u00B0F");
		String s_new = sb.toString();
		s_new = s_new.replaceAll("&deg;","\u00B0"); // This replacement doesn't behave with StringBuilder due to automatic escaping of ()
		return s_new;
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
	 * @param casInput	CAS RN (or sequence of multiple CAS RNs delimited by pipe and/or semicolon)
	 * @return			True if checksum holds for each CAS RN in input; false otherwise
	 */
	public static boolean isValidCAS(String casInput) {
		String[] casArray = casInput.split("\\||;|,");
		boolean valid = true;
		for (String cas:casArray) {
			String casTemp = cas.replaceAll("[^0-9]","");
			int len = casTemp.length();
			if (len > 10 || len <= 0) { return false; }
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
		StringBuilder sb = new StringBuilder(str);
		replaceAll(sb,"\\^0","\u2070");// superscript 0
		replaceAll(sb,"\\^1","\u00B9");// superscript 1
		replaceAll(sb,"\\^2","\u00B2");// superscript 2
		replaceAll(sb,"\\^3","\u00B3");// superscript 3
		replaceAll(sb,"\\^4","\u2074");// superscript 4
		replaceAll(sb,"\\^5","\u2075");// superscript 5
		replaceAll(sb,"\\^6","\u2076");// superscript 6
		replaceAll(sb,"\\^7","\u2077");// superscript 7
		replaceAll(sb,"\\^8","\u2078");// superscript 8
		replaceAll(sb,"\\^9","\u2079");// superscript 9
		replaceAll(sb,"_0","\u2080");// subscript 0
		replaceAll(sb,"_1","\u2081");// subscript 1
		replaceAll(sb,"_2","\u2082");// subscript 2
		replaceAll(sb,"_3","\u2083");// subscript 3
		replaceAll(sb,"_4","\u2084");// subscript 4
		replaceAll(sb,"_5","\u2085");// subscript 5
		replaceAll(sb,"_6","\u2086");// subscript 6
		replaceAll(sb,"_7","\u2087");// subscript 7
		replaceAll(sb,"_8","\u2088");// subscript 8
		replaceAll(sb,"_9","\u2089");// subscript 9
		return sb.toString();
	}

	public static String fixChars(String str) {
		StringBuilder sb = new StringBuilder(str);
		try {
		replaceAll(sb,"Ã¢â¬â","-");
		replaceAll(sb,"Ã¢â¬â¢","'");
		replaceAll(sb,"\uff08", "(");// Ã¯Â¼Ë
		replaceAll(sb,"\uff09", ")");// Ã¯Â¼â°
		replaceAll(sb,"\uff0f", "/");// Ã¯Â¼ï¿½
		replaceAll(sb,"\u3000", " ");//blank
		replaceAll(sb,"\u00a0", " ");//blank
		replaceAll(sb,"\u2003", " ");//blank
		replaceAll(sb,"\u0009", " ");//blank
		replaceAll(sb,"\u300c", "");// Ã£â¬Å
		replaceAll(sb,"\u300d", "");// Ã£â¬ï¿½
		replaceAll(sb,"\u2070", "^0");// superscript 0
		replaceAll(sb,"\u00B9", "^1");// superscript 1
		replaceAll(sb,"\u00B2", "^2");// superscript 2
		replaceAll(sb,"\u00B3", "^3");// superscript 3
		replaceAll(sb,"\u2074", "^4");// superscript 4
		replaceAll(sb,"\u2075", "^5");// superscript 5
		replaceAll(sb,"\u2076", "^6");// superscript 6
		replaceAll(sb,"\u2077", "^7");// superscript 7
		replaceAll(sb,"\u2078", "^8");// superscript 8
		replaceAll(sb,"\u2079", "^9");// superscript 9
		replaceAll(sb,"\u2080", "_0");// subscript 0
		replaceAll(sb,"\u2081", "_1");// subscript 1
		replaceAll(sb,"\u2082", "_2");// subscript 2
		replaceAll(sb,"\u2083", "_3");// subscript 3
		replaceAll(sb,"\u2084", "_4");// subscript 4
		replaceAll(sb,"\u2085", "_5");// subscript 5
		replaceAll(sb,"\u2086", "_6");// subscript 6
		replaceAll(sb,"\u2087", "_7");// subscript 7
		replaceAll(sb,"\u2088", "_8");// subscript 8
		replaceAll(sb,"\u2089", "_9");// subscript 9
		} catch (Exception ex) {
			System.out.println(sb.toString());
		}
		return sb.toString();
	}
	
	private static void replaceAll(StringBuilder sb, String find, String replace){
        
        //compile pattern from find string
        Pattern p = Pattern.compile(find);
        
        //create new Matcher from StringBuilder object
        Matcher matcher = p.matcher(sb);
        
        //index of StringBuilder from where search should begin
        int startIndex = 0;
        
        while (matcher.find(startIndex)) {
            
            sb.replace(matcher.start(), matcher.end(), replace);
            
            //set next start index as start of the last match + length of replacement
            startIndex = matcher.start() + replace.length();
        }
    }

	public static boolean getTextSolubility(ExperimentalRecord er, String propertyValue) {
		String propertyValue1 = propertyValue.toLowerCase();
		Matcher matcher = Pattern.compile("([0-9.]+|one)[ ]?gr?a?m? .*(soluble |dissolves? )?in:?.*((less|greater|more) than|<|>|~|about)? ?([0-9.,]+) (ml|cc) (of )?(cold|hot|warm|boiling)? ?(water|h2o)").matcher(propertyValue1);
		if (!matcher.find()) { return false; }
		String num = matcher.group(1);
		String qual = matcher.group(3);
		String denom = matcher.group(5);
		if (num==null || num.isBlank() || denom==null || denom.isBlank()) { return false; }
		denom = denom.replaceAll(",", "");
		if (num.equals("one")) { 
			er.property_value_point_estimate_original = 1.0/Double.parseDouble(denom);
		} else {
			er.property_value_point_estimate_original = Double.parseDouble(num)/Double.parseDouble(denom);
		}
		er.property_value_units_original = ExperimentalConstants.str_g_mL;
		if (qual==null || qual.isBlank()) {
			return true;
		} else if (qual.contains("greater") || qual.contains(">") || qual.contains("more")) {
			er.property_value_numeric_qualifier = "<";
		} else if (qual.contains("about") || qual.contains("~")) {
			er.property_value_numeric_qualifier = "~";
		} else if (qual.contains("less") || qual.contains("<")) {
			er.property_value_numeric_qualifier = ">";
		}
		return true;
	}

	public static boolean getSimpleNumericSolubility(ExperimentalRecord er, String propertyValue) {
		String propertyValue1 = propertyValue.toLowerCase();
		Matcher matcher = Pattern.compile("([~=><]{1,2}|about |approx[\\.]?(imately)? )?([0-9.]+) ?g ?/ ?([0-9.]+) ?(ml|cc)( ?[(] ?[0-9]+ ?\\u00B0C ?[)])?( in| of)? (hot |cold |boiling |warm )?water(( at| @) ([0-9.]+) \u00B0C)?")
				.matcher(propertyValue1);
		if (!matcher.find()) { return false; }
		String qual = matcher.group(1);
		String num = matcher.group(3);
		String denom = matcher.group(4);
		if (num==null || num.isBlank() || denom==null || denom.isBlank()) { return false; }
		denom = denom.replaceAll(",", "");
		er.property_value_point_estimate_original = Double.parseDouble(num)/Double.parseDouble(denom);
		er.property_value_units_original = ExperimentalConstants.str_g_mL;
		if (qual!=null) { 
			if (qual.contains("about") || qual.contains("approx")) { 
				er.property_value_numeric_qualifier = "~";
			} else {
				er.property_value_numeric_qualifier = qual;
			}
		}
		return true;
	}

}
