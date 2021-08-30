package gov.epa.exp_data_gathering.parse.ChemicalBook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.api.RawDataRecord;
import gov.epa.database.SQLite_CreateTable;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.ParseUtilities;

/**
 * @author CRAMSLAN
 *
 */

public class ParseChemicalBook extends Parse {
	
	public ParseChemicalBook() {
		sourceName = "ChemicalBook";
		this.init();
	}
	
	@Override
	protected void createRecords() {
		Vector<RecordChemicalBook> records = RecordChemicalBook.parseWebpagesInDatabase();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordChemicalBook> recordsChemicalBook = new ArrayList<RecordChemicalBook>();
			RecordChemicalBook[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordChemicalBook[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsChemicalBook.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordChemicalBook[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsChemicalBook.add(tempRecords[i]);
					}
				}
			}
			
			Iterator<RecordChemicalBook> it = recordsChemicalBook.iterator();
			while (it.hasNext()) {
				RecordChemicalBook r = it.next();
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	
	/**
	 * This is the first part of what is essentially one addExperimentalRecord method for the parse class of other data sources<br> 
	 * does the 
	 * @param cbr
	 * @param recordsExperimental
	 */
	private void addExperimentalRecords(RecordChemicalBook cbr,ExperimentalRecords recordsExperimental) {
		if (cbr.density != null && !cbr.density.isBlank()) {
			addNewExperimentalRecord(cbr,ExperimentalConstants.strDensity,cbr.density,recordsExperimental);
	    }
        if (cbr.meltingPoint != null && !cbr.meltingPoint.isBlank()) {
			addNewExperimentalRecord(cbr,ExperimentalConstants.strMeltingPoint,cbr.meltingPoint,recordsExperimental);
        }
        if (cbr.boilingPoint != null && !cbr.boilingPoint.isBlank()) {
			addNewExperimentalRecord(cbr,ExperimentalConstants.strBoilingPoint,cbr.boilingPoint,recordsExperimental);
	    }
        if (cbr.solubility != null && !cbr.solubility.isBlank()) {
			addNewExperimentalRecord(cbr,ExperimentalConstants.strWaterSolubility,cbr.solubility,recordsExperimental);
        }
		if (cbr.vaporpressure != null && !cbr.vaporpressure.isBlank()) {
			addNewExperimentalRecord(cbr,ExperimentalConstants.strVaporPressure,cbr.vaporpressure,recordsExperimental);
	    }

         
	}

	/**
	 * This is the second part of what is essentially one addExperimentalRecord method for the parse class of other data sources<br>
	 * populates fields of experimentalRecord objects with data from RecordChemicalBook objects
	 * @param cbr
	 * @param propertyName
	 * @param propertyValue
	 * @param recordsExperimental
	 */
	private void addNewExperimentalRecord(RecordChemicalBook cbr, String propertyName, String propertyValue, ExperimentalRecords recordsExperimental) {
		ExperimentalRecord er = new ExperimentalRecord();
		er.casrn=cbr.CAS;
		er.date_accessed = cbr.date_accessed;
		er.einecs=cbr.EINECS;
		er.chemical_name=cbr.chemicalName;
		if (cbr.synonyms != null) { er.synonyms=cbr.synonyms.replace(';','|'); }
		er.property_name=propertyName;
		er.property_value_string=propertyValue;
		er.source_name= ExperimentalConstants.strSourceChemicalBook;
		er.url = "https://www.chemicalbook.com/" + cbr.fileName;		
		
		// Adds measurement methods and notes to valid records
		// Clears all numerical fields if property value was not obtainable
		boolean foundNumeric = false;
		propertyValue = propertyValue.replaceAll("(\\d),(\\d)", "$1.$2");

		if (propertyName==ExperimentalConstants.strDensity) {
			foundNumeric = ParseUtilities.getDensity(er, propertyValue);
			ParseUtilities.getPressureCondition(er,propertyValue,sourceName);
			ParseUtilities.getTemperatureCondition(er,propertyValue);
			if (propertyValue.contains("±")) {
				getUncertaintyRange(er,propertyValue);
			}
		} else if (propertyName==ExperimentalConstants.strMeltingPoint || propertyName==ExperimentalConstants.strBoilingPoint ) {
			foundNumeric = ParseUtilities.getTemperatureProperty(er,propertyValue);
			ParseUtilities.getPressureCondition(er,propertyValue,sourceName);
			// performs the missing temperature check
			ParseUtilities.getTemperatureProperty(er,propertyValue);
			String temp = ParseUtilities.getTemperatureUnits(propertyValue);
			if (temp.matches("")) {
				er.reason = "missing temperature units";
			}
			// hard coded bits. I don't see how they can be avoided
			if (propertyValue.contains("<=")) {
				er.property_value_numeric_qualifier = "<="; // CAS 15538-93-9 has a problem of alternative less than sign not registering.
			}
			
		} else if (propertyName==ExperimentalConstants.strWaterSolubility) {
			foundNumeric = ParseUtilities.getWaterSolubility(er, propertyValue,sourceName);
			ParseUtilities.getTemperatureCondition(er,propertyValue);
			getQualitativeSolubility(er, propertyValue);
		}
		
		if (propertyName==ExperimentalConstants.strVaporPressure) {
			if (propertyValue.contains("l")) {
				String s = propertyValue.replaceAll("l", "1");
				propertyValue = s;
				foundNumeric = ParseUtilities.getVaporPressure(er,propertyValue);
				ParseUtilities.getTemperatureCondition(er,propertyValue);

			} else {
			foundNumeric = ParseUtilities.getVaporPressure(er,propertyValue);
			ParseUtilities.getTemperatureCondition(er,propertyValue);
			}
			


		}

		
		
		if (foundNumeric) {
			uc.convertRecord(er);
			if (propertyValue.contains("lit.")) { er.updateNote(ExperimentalConstants.str_lit); }
			if (propertyValue.contains("dec.")) { er.updateNote(ExperimentalConstants.str_dec); }
			if (propertyValue.contains("subl.")) { er.updateNote(ExperimentalConstants.str_subl); }
			// Warns if there may be a problem with an entry
			er.flag = false;
			if (propertyName.contains("?")) { er.keep = true; }
		} else {
			er.property_value_units_original = null;
			er.pressure_mmHg = null;
			er.temperature_C = null;
		}
		
		if (!(er.property_value_string.toLowerCase().contains("tox") && er.property_value_units_original==null)
				&& (er.property_value_units_original!=null || er.property_value_qualitative!=null || er.note!=null)) {
			er.keep = true;
		} else {
			er.keep = false;
		}
		if ((er.property_value_string.toLowerCase().contains("predicted") || (er.property_value_string.toLowerCase().contains("estimate")))) {
			er.keep = false;
			er.reason = "predicted or estimated value";
		}
		if (propertyName==ExperimentalConstants.strMeltingPoint || propertyName==ExperimentalConstants.strBoilingPoint) {
			meltingSolventCheck(er,propertyValue);
		}
		if (propertyName==ExperimentalConstants.strWaterSolubility) {
			checkWaterSolubilities(er, propertyValue);
		}
		if (propertyName==ExperimentalConstants.strBoilingPoint){
			getPressureRange(er,propertyValue);
		}
		
		
		recordsExperimental.add(er);
	}
	
	public static void main(String[] args) {
		ParseChemicalBook p = new ParseChemicalBook();
		p.generateOriginalJSONRecords = false;
		p.createFiles();
	}
	
	
	/**
	 * obtains a range of values from a string of the form 3.2 - 4.5 (units trimmed earlier), where the "-" is a minus sign.
	 * @param er
	 * @param propertyValue
	 */
	public static void getUncertaintyRange(ExperimentalRecord er, String propertyValue) {
		Matcher solubilityMatcher = Pattern.compile("([0-9]*\\.?[0-9]+)(\\u00B1)?([0-9]*\\.?[0-9]+)").matcher(propertyValue);
		if (solubilityMatcher.find()) {
			String pointEst = solubilityMatcher.group(1);
			String uncertainty = solubilityMatcher.group(3);
			if (!(solubilityMatcher.group(2) == null)) {
				double center = Double.parseDouble(pointEst);
				double upperval = center + Double.parseDouble(uncertainty);
				double lowerval = center - Double.parseDouble(uncertainty);
				er.property_value_max_original = upperval;
				er.property_value_min_original = lowerval;
			}
		}

	}
	
	public void downloadPropertyLinksToDatabase(Vector<String> urls,String tableName, int start, int end, boolean startFresh) {
		String databasePath = databaseFolder+File.separator+"search_property"+"_raw_html.db";
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		
		java.sql.Connection conn=SQLite_CreateTable.create_table(databasePath, tableName, RawDataRecord.fieldNames, startFresh);
		Random rand = new Random();
		
		try {
			int counter = 0;
			for (int i = start; i < end; i++) {
				String url = urls.get(i);
				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
				Date date = new Date();  
				String strDate=formatter.format(date);
				
				RawDataRecord rec=new RawDataRecord(strDate, url, "");
				boolean haveRecord=rec.haveRecordInDatabase(databasePath,tableName,conn);
				if (!haveRecord || startFresh) {
					long delay = 0;
					try {
						long startTime=System.currentTimeMillis();
						rec.content= getSearchURLAndVerificationCheck(urls.get(i));
						if (!(rec.content == null)) {
						long endTime=System.currentTimeMillis();
						delay = endTime-startTime;
						rec.addRecordToDatabase(tableName, conn);
						counter++;
						if (counter % 100==0) { System.out.println("Downloaded "+counter+" pages"); }
						}
						else {
							System.out.println("index of search url where break occurred: " + i);
							break;
						}
					} catch (Exception ex) {
						System.out.println("Failed to download "+url);
					}
					Thread.sleep((long) (delay*(1+rand.nextDouble())));
				}
			}
			
			System.out.println("Downloaded "+counter+" pages");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * related to the results page when a particular CAS is searched. Essentially finds out whether a properties link exists  <br> 
	 * This method really belongs in recordchemicalbook class but I put it here because it started out as non static or something.
	 * @param url
	 * @return
	 */
	public static String getSearchURLAndVerificationCheck(String url) {
		try {
			Document doc = Jsoup.connect(url).get();
			Element resultsHeader = doc.select("#ContentPlaceHolder1_LabelSummary").first(); // reveals if there are results for the search
			if (resultsHeader == null) {
				Element emptysearchcheck = doc.select(".SearchEmpty").first();
				System.out.println(emptysearchcheck.outerHtml());
				return emptysearchcheck.toString();
			}
			else {
			String resultsFound = resultsHeader.text().toString();
			if (resultsFound.matches("No results found")) {
					return resultsFound; // reveals if there are no results found, second way the search can come back empty
				}
			else if (resultsFound.isEmpty()) {
				System.out.println("javascript preventing any further checking. open website and spin wheel at " + url);
				return null;
			}
			else {
				Element propertiesBox = doc.select(".actionspro").first();
					if (!(propertiesBox == null)) {
						String propertiesLink = propertiesBox.select("a:contains(Chemical)").attr("abs:href").toString();
						return propertiesLink;
					}
					else {
						return null;
					}
			}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Determines whether "solv" is contained in the string. Nonsensically structured.
	 * @param er
	 * @param propertyValue
	 */
	void meltingSolventCheck(ExperimentalRecord er, String propertyValue) {
		if (propertyValue.toLowerCase().contains("solv")){
			er.keep = false;
			er.reason = "solvent specified, want pure compound only";
		}
	}
	
	/**
	 * Parses water solubility records and removes the ones with bad features. Similarly nonsensically structured.
	 * @param er
	 * @param propertyValue
	 */
	void checkWaterSolubilities(ExperimentalRecord er, String propertyValue) {
		if (propertyValue.toLowerCase().contains("%")) {
			er.keep = false;
			er.reason = "common feature of bad solubility records";
		} else if (propertyValue.toLowerCase().contains("parts of water")) {
			er.keep = false;
			er.reason = "we don't want these units";
		} else if (!((propertyValue.toLowerCase().contains("water") || propertyValue.contains("H2O")))) {
			er.keep = false;
			er.reason = "no information about water solubility";
		}
		if (er.keep == true) {
			er.reason = ""; // for the moment I am hesitant to edit Parse class or override getWSaterSolubility, this is an inelegant solution
		}
		
	}
	
	/**
	 * guarantees that qualitative solubility descriptions are parsed properly.
	 * @param er
	 * @param propertyValue
	 */
	void getQualitativeSolubility(ExperimentalRecord er, String propertyValue) {
		propertyValue = propertyValue.toLowerCase();
		String solventMatcherStr = "";
		if (sourceName.equals(ExperimentalConstants.strSourceChemicalBook)) { // this is the only thing I changed
			solventMatcherStr = "(([a-zA-Z0-9\s-]+?)(,| and|\\.|\\z|[ ]?\\(|;))?";
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
		if (propertyValue.contains("reacts") || propertyValue.contains("reaction"))
			er.property_value_qualitative = "reaction";
		if (propertyValue.contains("hydrolysis") || propertyValue.contains("hydrolyse") || propertyValue.contains("hydrolyze"))
			er.property_value_qualitative = "hydrolysis";
		if (propertyValue.contains("decompos"))
			er.property_value_qualitative = "decomposes";
		if (propertyValue.contains("autoignition"))
			er.property_value_qualitative = "autoignition";
		
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
	
	/**
	 * populates the pressure condition field of experimental record when the pressure is given as a range.<br>
	 * only applicable when the data comes in as Press: 760 or Press: 38.0 - 47.8
	 * @param er
	 * @param propertyValue
	 */
	public static void getPressureRange(ExperimentalRecord er, String propertyValue) {
	// public static void getAveragePressureFromRange(String propertyValue) throws IllegalStateException {
		if (propertyValue.toLowerCase().contains("press")){
			try {
		Matcher afterPressMatcher = Pattern.compile("(Press:)(\\s)?([0-9]*\\.?[0-9]+)(\\-)?([0-9]*\\.?[0-9]+)").matcher(propertyValue);
		if (afterPressMatcher.find()) {
		if (!(afterPressMatcher.group(1) == null)) {
		String lowerPressure = afterPressMatcher.group(3);
		String rangeCheck = afterPressMatcher.group(4);
		String higherPressure = afterPressMatcher.group(5);
		if (!(rangeCheck == null)) {
			double min = Double.parseDouble(lowerPressure);
			double max = Double.parseDouble(higherPressure);
			er.pressure_mmHg = min+"~"+max;
		}
		}
		}
	} catch (IllegalStateException e) {
		e.printStackTrace();
	}

		}
	}

}
	
	

