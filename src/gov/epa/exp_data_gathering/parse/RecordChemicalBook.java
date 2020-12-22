package gov.epa.exp_data_gathering.parse;
import gov.epa.api.RawDataRecord;
import gov.epa.ghs_data_gathering.Database.CreateGHS_Database;
import gov.epa.ghs_data_gathering.Database.MySQL_DB;
import gov.epa.ghs_data_gathering.GetData.RecordDashboard;
import gov.epa.ghs_data_gathering.Utilities.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/*experimental records that appear relevant*/
/**
 * @author CRAMSLAN
 *
 */
public class RecordChemicalBook extends Parse {
		String chemicalName;
		String synonyms;
		String CAS; 
		String MF;
		String MW; 
		String EINECS;
		String molfile; 
		String meltingPoint; 
		String boilingPoint;
		String density; 
		String vapordensity;
		String vaporpressure; 
		String refractiveindex;
		String FP; 
		String storagetemp;
		String solubility; 
		String pka;
		String form; 
		String color;
		String odor; 
		String relativepolarity;
		String odorthreshold;
		String explosivelimit;
		String watersolubility;
		String lambdamax;
		String merck; 
		String BRN;
		String henrylc; 
		String exposurelimits;
		String stability; 
		String InCHlKey;
		String CASreference; 
		String IARC;
		String NISTchemref; 
		String EPAsrs;
		String hazardcodes; 
		String fileName;
		String date_accessed;

		static final String sourceName="ChemicalBook";	

	
/**
 * Parses the jsoup document and occupies the RecordchemicalBook fields with the correct values.
 * @param rcb
 * @param doc
 */
private static void parseDocument(RecordChemicalBook rcb, Document doc) {
	Elements table_elements = doc.select("tr.ProdSupplierGN_ProductA_2");
	Elements table_elements2 = doc.select("tr.ProdSupplierGN_ProductA_1");
	table_elements.addAll(table_elements2);
	for (Element table_element:table_elements) {
		String header = table_element.select("td").first().text();
		String data = new String();
		if (!table_element.getElementsByTag("a").text().equals("")) {
			data = table_element.getElementsByTag("a").text();
		} else if (!table_element.select("td + td").text().equals("")) {
			data = table_element.select("td + td").text(); // inconsistent with the other two  but illustrative
		} else {
			data = null; // don't want it taking images
		}
			if (data != null && !data.isBlank()) {
				if (header.contains("CAS:") && !header.contains("CAS DataBase Reference")) { rcb.CAS = data; }
				else if (header.contains("Synonyms:")) { rcb.synonyms = data; }
				else if (header.contains("MF")) { rcb.MF = data; }
				else if (header.contains("EINECS")) { rcb.EINECS = data; }
				else if (header.contains("Mol File:")) { rcb.molfile = data; }
				else if (header.contains("Boiling point")) { rcb.boilingPoint = data; }
				else if (header.contains("density") && (!(header.contains("vapor density")))) { rcb.density = data; }
				else if (header.contains("refractive index")) { rcb.refractiveindex = data; }
				else if (header.contains("Fp")) { rcb.FP = data; }
				else if (header.contains("form:")) { rcb.form = data; }
				else if (header.contains("Merck")) { rcb.merck = data; }
				else if (header.contains("BRN")) { rcb.BRN = data; }
				else if (header.contains("Henry's Law Constant")) { rcb.henrylc = data; }
				else if (header.contains("Exposure limits")) { rcb.exposurelimits = data; }
				else if (header.contains("Stability")) { rcb.stability = data; }
				else if (header.contains("InCHlKey")) { rcb.InCHlKey = data; }
				else if (header.contains("CAS DataBase")) { rcb.CASreference = data; }
				else if (header.contains("IARC")) { rcb.IARC = data; }
				else if (header.contains("NIST Chemistry")) { rcb.NISTchemref = data; }
				else if (header.contains("EPA")) { rcb.EPAsrs = data; }
				else if (header.contains("Hazard Codes")) { rcb.hazardcodes = data; }
				else if (header.contains("storage temp.")) { rcb.storagetemp = data; }
				else if (header.contains("solubility")) { rcb.solubility = data; }
				else if (header.contains("color")) { rcb.color = data; }
				else if (header.contains("Melting point")) { rcb.meltingPoint = data; }
				else if (header.contains("vapor density")) { rcb.vapordensity = data; }
				else if (header.contains("vapor pressure")) { rcb.vaporpressure = data; }
				else if (header.contains("Relative polarity")) { rcb.relativepolarity = data; }
				else if (header.contains("Odor Threshold")) { rcb.odorthreshold = data; }
				else if (header.contains("explosivelimit")) { rcb.explosivelimit = data; }
				else if (header.contains("Water Solubility")) { rcb.watersolubility = data; }
				else if (header.contains("Product Name:")) { rcb.chemicalName = data; }
				else if (header.contains("max")) { rcb.lambdamax = data; }					
				else if (header.contains("MW")) { rcb.MW = data;}

				}
	
			}
}

private static Vector<String> getSearchURLsFromDashboardRecords (Vector<RecordDashboard> records,int start,int end) {
	String baseURL = "https://www.chemicalbook.com/Search_EN.aspx?keyword=";
	Vector<String> urls = new Vector<String>();
	for (int i = start; i < end; i++) {
		String CAS = records.get(i).CASRN;
		if (!CAS.startsWith("NOCAS")) {
			urls.add(baseURL+CAS);
			// System.out.println(urls.get(i - start)); let's think of a better way of doing this
		}
	}
	return urls;
}


/**
 * Parses the HTML strings in the raw HTML database to RecordLookChem objects
 * @return	A vector of RecordChemicalBook objects containing the data from the raw HTML database
 */
public static Vector<RecordChemicalBook> parseWebpagesInDatabase() {
	String databaseFolder = "Data"+File.separator+"Experimental"+ File.separator + sourceName + File.separator + "General";
	String databasePath = databaseFolder+File.separator+sourceName+"_raw_html.db";
	Vector<RecordChemicalBook> records = new Vector<>();

	try {
		Statement stat = MySQL_DB.getStatement(databasePath);
		ResultSet rs = MySQL_DB.getAllRecords(stat, "ChemicalBook");
		
		int counter = 1;
	
		while (rs.next()) {
			if (counter % 100==0) { System.out.println("Parsed "+counter+" pages"); }
			
			String html = rs.getString("content");
			html = html.replaceAll("\\u2212", "-"); // there are some minus signs rather than "-" <- wanted, "eN" dash, and "eM" dashes
			html = html.replaceAll("\\u2264", "<="); // removes less than or equal to sign
			String url = rs.getString("url");
			String date = rs.getString("date");
			Document doc = Jsoup.parse(html);
			
			RecordChemicalBook rcb=new RecordChemicalBook();
			rcb.date_accessed = date.substring(0,date.indexOf(" "));

			rcb.fileName=url.substring(url.lastIndexOf("/")+1, url.length());
			
			parseDocument(rcb,doc);
			
			if (rcb.CAS != null) {
				records.add(rcb);
				counter++;
			} else {
				// rs.updateString("html", ExperimentalConstants.strRecordUnavailable);
				// Updater doesn't work - JDBC version issue?
			}
		}


		return records;
	} catch (Exception e) {
		e.printStackTrace();
	}
	
	return null;
}


/**
 * this is used to parse the search database, not the html one
 * @return
 */
public static Vector<String> parsePropertyLinksInDatabase() {
	String databaseFolder = "Data"+File.separator+"Experimental"+ File.separator + sourceName + File.separator + "General";
	String databasePath = databaseFolder+File.separator+"search_property_raw_html.db";
	Vector<String> records = new Vector<>();

	try {
		Statement stat = MySQL_DB.getStatement(databasePath);
		ResultSet rs = MySQL_DB.getAllRecords(stat, "searchAndPropertyLinks"); //hardcoded for now
		
		int counter = 1;
	
		while (rs.next()) {
			if (counter % 100==0) { System.out.println("Parsed "+counter+" pages"); }
			
			String propertylink = rs.getString("content");
			records.add(propertylink);
		}


		return records;
	} catch (Exception e) {
		e.printStackTrace();
	}
	
	return null;
}

public static void downloadWebpagesFromExcelToDatabase(String filename,int start,int end, int excelFinalRecord, boolean startFresh) {
	Vector<RecordDashboard> records = Parse.getDashboardRecordsFromExcel(filename);
	Vector<String> searchURLs = getSearchURLsFromDashboardRecords(records,1,excelFinalRecord);
	ParseChemicalBook p = new ParseChemicalBook();
	p.mainFolder = p.mainFolder + File.separator + "General";
	p.databaseFolder = p.mainFolder;
	// p.downloadPropertyLinksToDatabase(searchURLs,"searchAndPropertyLinks", start, end, startFresh);
	Vector<String> propertyURLs = parsePropertyLinksInDatabase();
	Vector<String> downloadedURLs = new Vector<String>();
	for (int i = 3001; i < 5000; i++) {
		downloadedURLs.add(propertyURLs.get(i));
	}
	p.downloadWebpagesToDatabaseAdaptive(downloadedURLs,"div.RFQbox ~ table",sourceName,false);		
}

public static void main(String[] args) {
	downloadWebpagesFromExcelToDatabase("Data" + "/PFASSTRUCT.xlsx",8060,8160,8164,false);
	}


}