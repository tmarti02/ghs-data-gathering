package gov.epa.exp_data_gathering.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.api.ExperimentalConstants;

public class ParseWaterSolubility {
	
	private static String extractAqueousEntry(ExperimentalRecord er, String propertyValue,String water,String solvent) {
		// er only needed so I can see which entries are parsed by which regex
		// Remove after regex defined
		boolean parsed = false;
		Matcher colonFormat1 = Pattern.compile(water+"( solubility)?:[ ]?([ <>~=\\.0-9mgul/@%\\(\\)\u00B0cfkph]+)[;,]")
				.matcher(propertyValue.toLowerCase());
		if (colonFormat1.find()) {
			propertyValue = colonFormat1.group(2);
			parsed = true;
			er.updateNote("Aqueous entry parsed (colon 1): "+propertyValue);
		}
		if (!parsed) {
			Matcher colonFormat2 = Pattern.compile("solubility: ([ <>~=\\.0-9MmGguLl/@%\\(\\)\u00B0CcFKPpHh]+)\\("+water+"\\)[;,]").matcher(propertyValue);
			if (colonFormat2.find()) {
				propertyValue = colonFormat2.group(1);
				parsed = true;
				er.updateNote("Aqueous entry parsed (colon 2): "+propertyValue);
			}
		}
		if (!parsed) {
			Matcher inWaterFormat1 = Pattern.compile("([<>=~\\?]*[ ]?[0-9]*\\.?[0-9]+[ <>~=\\.0-9XxMmGguLl/@%\\(\\)\u00B0CcFKPpHh\\+-]+ in )"+water
					+"( [@(at)] [ <>~=0-9MmGgLl/@%\\(\\)°CcFKPpHh]+)?").matcher(propertyValue);
			if (inWaterFormat1.find()) {
				propertyValue = inWaterFormat1.group(1)+water+(inWaterFormat1.group(2)==null ? "" : inWaterFormat1.group(2));
				parsed = true;
				er.updateNote("Aqueous entry parsed (in water 1): "+propertyValue);
			}
		}
		if (!parsed) {
			Matcher inWaterFormat2 = Pattern.compile("(in )?"+water+" \\(?([ <>~=\\.0-9XxMm(to)(percent)(about)(mols)gul/@%\\(\\)\u00B0cfkph\\+-]+)\\)?")
					.matcher(propertyValue.toLowerCase());
			if (inWaterFormat2.find()) {
				propertyValue = inWaterFormat2.group(2);
				parsed = true;
				er.updateNote("Aqueous entry parsed (in water 2): "+propertyValue);
			}
		}

		if (!parsed) {
			er.flag = true;
			er.updateNote("Aqueous entry: "+propertyValue+", Solvent: "+solvent+", Water: "+water);
		}
		
		return propertyValue;
	}

	public static boolean getWaterSolubility(ExperimentalRecord er,String propertyValue,String sourceName) {
		boolean badUnits = true;
		int unitsIndex = -1;
		propertyValue = propertyValue.replaceAll("([0-9]),([0-9]{3})", "$1$2");
		propertyValue = ParseUtilities.correctDegreeSymbols(propertyValue);
		
		Matcher containsNumber = Pattern.compile(".*\\d.*").matcher(propertyValue);
		if (!containsNumber.find()) { return false; }
		
		String[] badSolvents = {"ether","benzene","naoh","hcl","chloroform","ligroin","acet","alc","dmso","dimethyl sulfoxide","etoh","hexane","meoh",
				"dichloromethane","dcm","toluene","glyc","oils","organic solvent","dmf","et2o","etoac","mcoh","chc1","xylene","dioxane","hydrocarbon","kerosene",
				"acid","oxide","pyri","carbon tetrachloride","pet","anol","ch3oh","c2h5oh","ch2cl2","chcl3","alkali","dsmo","dma","buffer","ammonia water","ammon"};
		boolean foundSolvent = false;
		boolean foundWater = false;
		String[] waterSynonyms = {"water","h2o","h20","aqueous solution"};
		for (String solvent:badSolvents) {
			// Stop searching if a solvent has already been found
			if (foundSolvent) { continue; }
			// Check for non-aqueous solvents
			if (propertyValue.toLowerCase().contains(solvent) && (er.chemical_name==null || !er.chemical_name.contains(solvent))) {
				// Mark non-aqueous solvent found
				foundSolvent = true;
				foundWater = false;
				// See if there is an aqueous record too
				for (String water:waterSynonyms) {
					// Stop searching if water synonym already found
					if (foundWater) { continue; }
					// If water synonym found, parse out aqueous entry
					if (propertyValue.toLowerCase().contains(water) && !solvent.contains(water)) { // Necessary because of ammonia water
						foundWater = true;
						extractAqueousEntry(er,propertyValue,water,solvent);
					}
				}
				// If no water synonyms found, mark record as bad
				if (!foundWater) {
					er.keep = false;
					er.reason = "Non-aqueous solubility";
				}
			}
		}
		
		if (propertyValue.toLowerCase().contains("mg/ml")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mg/l") || (propertyValue.toLowerCase().contains("mg/1") && !propertyValue.toLowerCase().contains("mg/10"))) {
			er.property_value_units_original = ExperimentalConstants.str_mg_L;
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
		} else if (propertyValue.toLowerCase().contains("ug/100")) {
			er.property_value_units_original = ExperimentalConstants.str_ug_100mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("ug/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("g/100ml") || propertyValue.toLowerCase().contains("g / 100 ml") 
				|| propertyValue.toLowerCase().contains("g/100 ml") || propertyValue.toLowerCase().contains("g/100 cc")) {
			er.property_value_units_original = ExperimentalConstants.str_g_100mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("/");
			badUnits = false;
		// under construction - CR
		//
		//
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
		} else if (propertyValue.toLowerCase().contains("mol/l") || propertyValue.toLowerCase().contains("mols/l")) {
			er.property_value_units_original = ExperimentalConstants.str_M;
			unitsIndex = propertyValue.toLowerCase().indexOf("mol");
			badUnits = false;
		} else if (propertyValue.contains("M") && !(propertyValue.contains("ML") || propertyValue.contains("MG"))) {
			unitsIndex = propertyValue.indexOf("M");
			if (unitsIndex>0) {
				er.property_value_units_original = ExperimentalConstants.str_M;
				badUnits = false;
			}
		} 
	
		if (er.source_name!=ExperimentalConstants.strSourceOFMPub && unitsIndex < propertyValue.indexOf(":")) {
			unitsIndex = propertyValue.length();
		}
	
		boolean foundNumeric = false;
		if (er.keep) { foundNumeric = ParseUtilities.getNumericalValue(er,propertyValue, unitsIndex,badUnits); }
		return foundNumeric;
	}

	public static void getQualitativeSolubility(ExperimentalRecord er, String propertyValue,String sourceName) {
		String propertyValue1 = propertyValue.toLowerCase();
		String solventMatcherStr = "";
		if (sourceName.equals(ExperimentalConstants.strSourceLookChem)) {
			solventMatcherStr = "(([a-zA-Z0-9\s-%]+?)(,| and|\\.|\\z|[ ]?\\(|;))?";
		} else if (sourceName.equals(ExperimentalConstants.strSourcePubChem)) {
			solventMatcherStr = "(([a-zA-Z0-9\s,-]+?)(\\.|\\z| at| and only|\\(|;))?";
		}
		Matcher solubilityMatcher = Pattern.compile("(([a-zA-Z]+y[ ]?)?([a-zA-Z]+y[ ]?)?(in|im)?(so[l]?(uble)]?|miscible))( [\\(]?(in|with) )?[[ ]?\\.{3}]*"+solventMatcherStr).matcher(propertyValue1);
		while (solubilityMatcher.find()) {
			String qualifier = solubilityMatcher.group(1);
			qualifier = qualifier.equals("souble") ? "soluble" : qualifier;
			String prep = solubilityMatcher.group(7);
			String solvent = solubilityMatcher.group(9);
			if (solvent==null || solvent.length()==0 || solvent.contains("water") || solvent.contains("aqueous solution")) {
				er.property_value_qualitative = qualifier;
			} else {
				prep = prep==null ? " " : prep;
				er.updateNote(qualifier + prep + solvent);
			}
		}
	
		if (propertyValue1.contains("reacts") || propertyValue1.contains("reaction")) {
			er.property_value_qualitative = "reaction";
		}
	
		if (propertyValue1.contains("hydrolysis") || propertyValue1.contains("hydrolyse") || propertyValue1.contains("hydrolyze")) {
			er.property_value_qualitative = "hydrolysis";
		}
	
		if (propertyValue1.contains("decompos")) {
			er.property_value_qualitative = "decomposes";
		}
	
		if (propertyValue1.contains("autoignition")) {
			er.property_value_qualitative = "autoignition";
		}
	
		String[] qualifiers = {"none","very poor","poor","low","negligible","slight","significant","complete"};
		for (String qual:qualifiers) {
			if ((propertyValue1.startsWith(qual) || (propertyValue1.contains("solubility in water") && propertyValue1.contains(qual))) &&
					(er.property_value_qualitative==null || er.property_value_qualitative.isBlank())) {
				er.property_value_qualitative = qual;
			}
		}
	
		if ((er.reason==null || !er.reason.toLowerCase().contains("non-aqueous solubility")) &&
				(er.property_value_qualitative!=null || er.note!=null)) {
			er.keep = true;
			er.reason = null;
		}
	}

}
