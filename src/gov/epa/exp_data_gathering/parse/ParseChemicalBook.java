package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.api.RawDataRecord;
import gov.epa.ghs_data_gathering.Database.CreateGHS_Database;

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
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			FileReader originalsource = new FileReader(jsonFile);
			RecordChemicalBook[] recordsChemicalBook = gson.fromJson(originalsource, RecordChemicalBook[].class);
			
			for (int i = 0; i < recordsChemicalBook.length; i++) {
				RecordChemicalBook r = recordsChemicalBook[i];
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	
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
         
	}

	private void addNewExperimentalRecord(RecordChemicalBook cbr, String propertyName, String propertyValue, ExperimentalRecords recordsExperimental) {
		ExperimentalRecord er = new ExperimentalRecord();
		er.casrn=cbr.CAS;
		er.einecs=cbr.EINECS;
		er.chemical_name=cbr.chemicalName;
		if (cbr.synonyms != null) { er.synonyms=cbr.synonyms.replace(';','|'); }
		er.property_name=propertyName;
		er.property_value_string=propertyValue;
		er.source_name= "Chemical Book";
		er.url = "https://www.chemicalbook.com/" + cbr.fileName;		
		
		// Adds measurement methods and notes to valid records
		// Clears all numerical fields if property value was not obtainable
		boolean foundNumeric = false;
		
		propertyValue = propertyValue.replaceAll(",", ".");
		if (propertyName==ExperimentalConstants.strDensity) {
			foundNumeric = getDensity(er, propertyValue);
			getPressureCondition(er,propertyValue);
			getTemperatureCondition(er,propertyValue);
		} else if (propertyName==ExperimentalConstants.strMeltingPoint || propertyName==ExperimentalConstants.strBoilingPoint ||
				propertyName==ExperimentalConstants.strFlashPoint) {
			foundNumeric = getTemperatureProperty(er,propertyValue);
			getPressureCondition(er,propertyValue);
		} else if (propertyName==ExperimentalConstants.strWaterSolubility) {
			foundNumeric = getWaterSolubility(er, propertyValue);
			getTemperatureCondition(er,propertyValue);
			// getQualitativeSolubility(er, propertyValue);
		}
		
		if (foundNumeric) {
			er.finalizeUnits();
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
			er.note = "predicted or estimated value";
		}
		recordsExperimental.add(er);
	
	}
	
	
	public static void main(String[] args) {
		ParseChemicalBook p = new ParseChemicalBook();
		p.mainFolder = p.mainFolder + File.separator + "General";
		p.databaseFolder = p.mainFolder;
		p.jsonFolder= p.mainFolder;
		p.createFiles();
	}
	
	public void downloadPropertyLinksToDatabase(Vector<String> urls,String tableName, int start, int end, boolean startFresh) {
		String databasePath = databaseFolder+File.separator+"search_property"+"_raw_html.db";
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		
		java.sql.Connection conn=CreateGHS_Database.createDatabaseTable(databasePath, tableName, RawDataRecord.fieldNames, startFresh);
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
	@Override
	boolean getWaterSolubility(ExperimentalRecord er,String propertyValue) {
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
		} else if (propertyValue.toLowerCase().contains("g/l")) {
			er.property_value_units_original = ExperimentalConstants.str_g_L;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/");
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
		} else if (propertyValue.toLowerCase().contains("%")) {
			er.property_value_units_original = ExperimentalConstants.str_pct;
			unitsIndex = propertyValue.indexOf("%");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ppm")) {
			er.property_value_units_original = ExperimentalConstants.str_ppm;
			unitsIndex = propertyValue.toLowerCase().indexOf("ppm");
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
		
		if (Character.isAlphabetic(propertyValue.charAt(0)) && !(propertyValue.toLowerCase().contains("water") || propertyValue.toLowerCase().contains("h2o"))) {
			er.note = "water";
			er.keep = false;
		}
		
		boolean foundNumeric = getNumericalValue(er,propertyValue, unitsIndex,badUnits);
		return foundNumeric;
	}



}
	
	

