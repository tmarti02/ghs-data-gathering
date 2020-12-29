package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.eChemPortalAPI.ParseEChemPortalAPI;

public class Parse {
	
	public String sourceName;
	public String jsonFolder;
	public String databaseFolder;
	public String webpageFolder;

	protected String fileNameSourceExcel;//input excel spreadsheet
	protected String fileNameHtmlZip;//input as zip file of webpages
	protected String fileNameSourceText;
	protected String folderNameWebpages;//input as folder of webpages
	protected String folderNameExcel;
	
	protected String fileNameJSON_Records;//records in original format
	
	protected String fileNameFlatExperimentalRecords;//records in flat format
	protected String fileNameJsonExperimentalRecords;//records in ExperimentalRecord class format
	protected String fileNameExcelExperimentalRecords;
	protected String fileNameFlatExperimentalRecordsBad;
	protected String fileNameJsonExperimentalRecordsBad;
	protected String mainFolder;
	
	public static boolean generateOriginalJSONRecords=true; //runs code to generate json records from original data format (json file has all the chemicals in one file)	
	public static boolean writeFlatFile=false;//all data converted to final format stored as flat text file
	public static boolean writeJsonExperimentalRecordsFile=true;//all data converted to final format stored as Json file
	public static boolean writeExcelExperimentalRecordsFile=true;//all data converted to final format stored as xlsx file
	
	protected Gson gson=null;

	public void init() {
		fileNameJSON_Records = sourceName +" Original Records.json";
		fileNameFlatExperimentalRecords = sourceName +" Experimental Records.txt";
		fileNameFlatExperimentalRecordsBad = sourceName +" Experimental Records-Bad.txt";
		fileNameJsonExperimentalRecords = sourceName +" Experimental Records.json";
		fileNameJsonExperimentalRecordsBad = sourceName +" Experimental Records-Bad.json";
		fileNameExcelExperimentalRecords = sourceName +" Experimental Records.xlsx";
		mainFolder = "Data" + File.separator + "Experimental" + File.separator + sourceName;
		databaseFolder = mainFolder;
		jsonFolder= mainFolder;
		webpageFolder = mainFolder + File.separator + "web pages";
		folderNameExcel=mainFolder + File.separator + "excel files";
		
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting().disableHtmlEscaping();
		gson = builder.create();
	}
	
	/**
	 * Need to override
	 */
	protected void createRecords() {
		System.out.println("Need to override createRecords()!");	
	}
	
	/**
	 * Need to override
	 * @return
	 */
	protected ExperimentalRecords goThroughOriginalRecords() {
		System.out.println("Need to override goThroughOriginalRecords()!");
		return null;
	}
	
	public void createFiles() {
		System.out.println("Creating " + sourceName + " json files...");
		
		if (generateOriginalJSONRecords) {
			if (fileNameSourceExcel!=null) {
				System.out.println("Parsing "+fileNameSourceExcel);
			} else if (folderNameWebpages!=null) {
				System.out.println("Parsing webpages in "+folderNameWebpages);
			} else if (folderNameExcel!=null) {
				System.out.println("Parsing excel files in "+folderNameExcel);
			} else {
				System.out.println("Parsing original file(s)");	
			}

			createRecords();
		}

		System.out.println("Going through original records");
		ExperimentalRecords records=goThroughOriginalRecords();
		ExperimentalRecords recordsBad = dumpBadRecords(records);

		if (writeFlatFile) {
			System.out.println("Writing flat file for chemical records");
			records.toFlatFile(mainFolder+File.separator+fileNameFlatExperimentalRecords,"|");
			recordsBad.toFlatFile(mainFolder+File.separator+fileNameFlatExperimentalRecordsBad,"|");
		}
		
		if (writeJsonExperimentalRecordsFile) {
			System.out.println("Writing json file for chemical records");
			records.toJSON_File(mainFolder+File.separator+fileNameJsonExperimentalRecords);
			recordsBad.toJSON_File(mainFolder+File.separator+fileNameJsonExperimentalRecordsBad);
		}
		
		if (writeExcelExperimentalRecordsFile) {
			System.out.println("Writing Excel file for chemical records");
			ExperimentalRecords merge = new ExperimentalRecords();
			merge.addAll(records);
			merge.addAll(recordsBad);
			if (merge.size() <= 100000) {
				merge.toExcel_File(mainFolder+File.separator+fileNameExcelExperimentalRecords);
			} else {
				ExperimentalRecords temp = new ExperimentalRecords();
				Iterator<ExperimentalRecord> it = merge.iterator();
				int i = 0;
				int batch = 0;
				while (it.hasNext()) {
					temp.add(it.next());
					i++;
					if (i!=0 && i%100000==0) {
						batch++;
						temp.toExcel_File(mainFolder+File.separator+sourceName +" Experimental Records "+batch+".xlsx");
						temp.removeAllElements();
					}
				}
				batch++;
				temp.toExcel_File(mainFolder+File.separator+sourceName +" Experimental Records "+batch+".xlsx");
			}
		}
		
		System.out.println("done\n");
	}
	
	public static ExperimentalRecords dumpBadRecords(ExperimentalRecords records) {
		ExperimentalRecords recordsBad = new ExperimentalRecords();
		Iterator<ExperimentalRecord> it = records.iterator();
		while (it.hasNext() ) {
			ExperimentalRecord temp = it.next();
			if (!temp.keep) {
				recordsBad.add(temp);
				it.remove();
			}
		}
		return recordsBad;
	}
	
	protected void writeOriginalRecordsToFile(Vector<?> records) {
		try {
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting().disableHtmlEscaping();
			Gson gson = builder.create();
			
			String jsonPath = jsonFolder + File.separator + fileNameJSON_Records;
			File file = new File(jsonPath);
			if(!file.getParentFile().exists()) { file.getParentFile().mkdirs(); }
			
			FileWriter fw = new FileWriter(jsonPath);
			String strRecords=gson.toJson(records);
			
			strRecords=Parse.fixChars(strRecords);
			
			fw.write(strRecords);
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	static boolean getNumericalValue(ExperimentalRecord er, String propertyValue, int unitsIndex, boolean badUnits) {
		if (badUnits) { unitsIndex = propertyValue.length(); }
		if (propertyValue.contains("±")) { unitsIndex = Math.min(propertyValue.indexOf("±"),unitsIndex); }
		boolean foundNumeric = false;
		if (!foundNumeric) {
			try {
				Matcher sciMatcher = Pattern.compile("([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]?(e|x10)[ ]?([-|\\+]?[ ]?[0-9]+)").matcher(propertyValue.toLowerCase().substring(0,unitsIndex));
				sciMatcher.find();
				String strMantissa = sciMatcher.group(1);
				String strMagnitude = sciMatcher.group(3);
				Double mantissa = Double.parseDouble(strMantissa.replaceAll("\\s",""));
				Double magnitude =  Double.parseDouble(strMagnitude.replaceAll("\\s","").replaceAll("\\+", ""));
				er.property_value_point_estimate_original = mantissa*Math.pow(10, magnitude);
				foundNumeric = true;
				int propertyValueIndex;
				if ((propertyValueIndex = propertyValue.indexOf(strMantissa)) > 0) {
					String checkSymbol = propertyValue.replaceAll("\\s","");
					er.property_value_numeric_qualifier = getNumericQualifier(checkSymbol,propertyValueIndex);
				}
			} catch (Exception ex) { }
		}
		
		if (!foundNumeric) {
			try {
				double[] range = Parse.extractFirstDoubleRangeFromString(propertyValue,unitsIndex);
				if (!badUnits) {
					er.property_value_min_original = range[0];
					er.property_value_max_original = range[1];
					foundNumeric = true;
				}
				if (propertyValue.contains("~") || propertyValue.contains("ca.")) {
					er.property_value_numeric_qualifier = "~";
				}
			} catch (Exception ex) { }
		}
		
		if (!foundNumeric) {
			try {
				double[] range = Parse.extractAltFormatRangeFromString(propertyValue,unitsIndex);
				if (!badUnits) {
					er.property_value_min_original = range[0];
					er.property_value_max_original = range[1];
					foundNumeric = true;
				}
				if (propertyValue.contains("~") || propertyValue.contains("ca.")) {
					er.property_value_numeric_qualifier = "~";
				}
			} catch (Exception ex) { }
		}
		
		if (!foundNumeric) {
			try {
				double propertyValueAsDouble = Parse.extractDoubleFromString(propertyValue,unitsIndex);
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
			} catch (Exception ex) { }
		}
		return foundNumeric;
	}
	
	protected static String getNumericQualifier(String str,int index) {
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
	
	protected static boolean getDensity(ExperimentalRecord er, String propertyValue) {
		boolean badUnits = true;
		int unitsIndex = -1;
		propertyValue = propertyValue.replaceAll("([0-9]),([0-9])", "$1.$2");
		if (propertyValue.toLowerCase().contains("g/cm") || propertyValue.toLowerCase().contains("g/cu cm") || propertyValue.toLowerCase().contains("gm/cu cm")) {
			er.property_value_units_original = ExperimentalConstants.str_g_cm3;
			unitsIndex = propertyValue.toLowerCase().indexOf("g");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("g/ml")) {
			er.property_value_units_original = ExperimentalConstants.str_g_mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/m");
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
			er.property_value_units_original = ExperimentalConstants.str_g_cm3;
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
		boolean foundNumeric = getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		return foundNumeric;
	}
	
	// Applicable for melting point, boiling point, and flash point
	protected static boolean getTemperatureProperty(ExperimentalRecord er,String propertyValue) {
		boolean badUnits = true;
		int unitsIndex = -1;
		String units = Parse.getTemperatureUnits(propertyValue);
		if (units.length()!=0) {
			er.property_value_units_original = units;
			unitsIndex = propertyValue.indexOf(units);
			badUnits = false;
		}
		boolean foundNumeric = getNumericalValue(er,propertyValue, unitsIndex,badUnits);
		return foundNumeric;
	}
	
	protected boolean getWaterSolubility(ExperimentalRecord er,String propertyValue) {
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
	
	void getQualitativeSolubility(ExperimentalRecord er, String propertyValue) {
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

	protected boolean getVaporPressure(ExperimentalRecord er,String propertyValue) {
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

	protected static boolean getHenrysLawConstant(ExperimentalRecord er,String propertyValue) {
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

	// Applicable for LogKow and pKa
	protected static boolean getLogProperty(ExperimentalRecord er,String propertyValue) {
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
	protected static void getTemperatureCondition(ExperimentalRecord er, String propertyValue) {
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
			} catch (Exception ex) { }
		}
	}

	/**
	 * Sets the pressure condition for an ExperimentalRecord object, if present
	 * @param er			The ExperimentalRecord object to be updated
	 * @param propertyValue	The string to be read
	 * @return				The pressure condition in kPa
	 */
	protected void getPressureCondition(ExperimentalRecord er,String propertyValue) {
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
					try {
						double[] range = Parse.extractFirstDoubleRangeFromString(propertyValue,pressureIndex);
						String min = formatDouble(range[0]*conversionFactor);
						String max = formatDouble(range[1]*conversionFactor);
						er.pressure_mmHg = min+"-"+max;
						foundNumeric = true;
					} catch (Exception ex) { }
				}
				if (!foundNumeric) {
					try {
						double[] range = Parse.extractAltFormatRangeFromString(propertyValue,pressureIndex);
						String min = formatDouble(range[0]*conversionFactor);
						String max = formatDouble(range[1]*conversionFactor);
						er.pressure_mmHg = min+"-"+max;
						foundNumeric = true;
					} catch (Exception ex) { }
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
					} catch (Exception ex) { }
				}
			}
			if (!foundNumeric) {
				try {
					er.pressure_mmHg = formatDouble(conversionFactor*Parse.extractDoubleFromString(propertyValue,pressureIndex));
					foundNumeric = true;
				} catch (Exception ex) { }
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
	protected static double[] extractFirstDoubleRangeFromString(String str,int end) throws IllegalStateException {
		Matcher anyRangeMatcher = Pattern.compile("([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]*([-]{1}|to|ca\\.)[ ]*([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(str.substring(0,end));
		anyRangeMatcher.find();
		String strMin = anyRangeMatcher.group(1).replace(" ","");
		String strMax = anyRangeMatcher.group(3).replace(" ","");
		double min = Double.parseDouble(strMin);
		double max = Double.parseDouble(strMax);
		if (min >= max) {
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
	}
	
	protected static double[] extractAltFormatRangeFromString(String str,int end) throws IllegalStateException {
		Matcher anyRangeMatcher = Pattern.compile(">[=]?[ ]?([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]?<[=]?[ ]?([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(str.substring(0,end));
		anyRangeMatcher.find();
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
	}

	/**
	 * Extracts the last number before a given index in a string
	 * @param str	The string to be read
	 * @param end	The index to stop searching
	 * @return		The number found as a double
	 * @throws IllegalStateException	If no number is found in the given range
	 */
	protected static double extractDoubleFromString(String str,int end) throws IllegalStateException, NumberFormatException {
		Matcher numberMatcher = Pattern.compile("[-]?[ ]?[0-9]*\\.?[0-9]+").matcher(str.substring(0,end));
		String strDouble = "";
		while (numberMatcher.find()) { strDouble = numberMatcher.group(); }
		return Double.parseDouble(strDouble.replace(" ",""));
	}

	/**
	 * If the property value string contains temperature units, returns the units in standardized format
	 * @param propertyValue	The string to be read
	 * @return				A standardized temperature unit string from ExperimentalConstants
	 */
	static String getTemperatureUnits(String propertyValue) {
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

	public static String fixChars(String str) {
		str=str.replace("Ã¢â‚¬â€œ","-").replace("Ã¢â‚¬â„¢","'");
		str=str.replace("\uff08", "(");// Ã¯Â¼Ë†
		str=str.replace("\uff09", ")");// Ã¯Â¼â€°
		str=str.replace("\uff0f", "/");// Ã¯Â¼ï¿½
		str=str.replace("\u3000", " ");//blank
		str=str.replace("\u00a0", " ");//blank
		str=str.replace("\u2003", " ");//blank
		str=str.replace("\u0009", " ");//blank
		str=str.replace("\u300c", "");// Ã£â‚¬Å’
		str=str.replace("\u300d", "");// Ã£â‚¬ï¿½
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
	
	public static void main(String[] args) {
		ParseADDoPT.main(null);
		ParseAqSolDB.main(null);
		ParseBradley.main(null);
		ParseEChemPortalAPI.main(null);
		ParseLookChem.main(null);
		ParseOChem.main(null);
		ParseOFMPub.main(null);
		ParsePubChem.main(null);
		ParseQSARDB.main(null);
		// ParseChemBL.main(null);
		DataFetcher.main(null);
	}
}

