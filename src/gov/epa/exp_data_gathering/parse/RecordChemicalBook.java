package gov.epa.exp_data_gathering.parse;
import gov.epa.ghs_data_gathering.Database.MySQL_DB;
import gov.epa.ghs_data_gathering.GetData.RecordDashboard;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


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

		static final String sourceName="ChemicalBook";	

	
private static void parseDocument(RecordChemicalBook rcb, Document doc) {
	Elements table_elements = doc.select("tr.ProdSupplierGN_ProductA_2, tr.ProdSupplierGN_ProductA_1");
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
				else if (header.contains("density")) { rcb.density = data; }
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
				else if (header.contains("")) { rcb.watersolubility = data; }
				else if (header.contains("max")) { rcb.lambdamax = data; }					
				else if (header.contains("MW")) { rcb.MW = data;}

				}
			}
}

private static RecordChemicalBook parseWebpage(Vector <String> url) {
	RecordChemicalBook cbr = new RecordChemicalBook();
	try {
		Document doc = Jsoup.connect(url.get(4)).get();
		parseDocument(cbr,doc);
	} catch (Exception ex) {
		ex.printStackTrace();
	}
	
	return cbr;
}


private static Vector<String> getsearchURLsFromDashboardRecords(Vector<RecordDashboard> records,int start,int end) {
	String baseURL = "https://www.chemicalbook.com/Search_EN.aspx?keyword=";
	Vector<String> urls = new Vector<String>();
	for (int i = start; i < end; i++) {
		String CAS = records.get(i).CASRN;
		if (!CAS.startsWith("NOCAS")) {
			urls.add(baseURL+CAS);
		}
	}
	return urls;
}

private static Vector<String> getpropertyURLfromsearchURL(Vector<String> url){
	Vector<String> propertyURL = new Vector<String>();
	for(int i = 0; i < url.size(); ++i) {
		try {
			Document doc = Jsoup.connect(url.get(i)).get();
			Element importantregion = doc.select("ul.actionspro").first();
			 propertyURL.add(importantregion.select("a:contains(Chemical)").attr("abs:href").toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("There's a human verification system that's preventing you from scraping");
		} 
	}
	return propertyURL;
}

public static void downloadWebpagesFromExcelToDatabase(String filename,int start,int end,boolean startFresh) {
	Vector<RecordDashboard> records = Parse.getDashboardRecordsFromExcel(filename);
	Vector<String> searchurls = getsearchURLsFromDashboardRecords(records,start,end);
	Vector<String> propertyurls = getpropertyURLfromsearchURL(searchurls);
	System.out.println(propertyurls.get(8));
	ParseChemicalBook p = new ParseChemicalBook();
	p.mainFolder = p.mainFolder + File.separator + "General";
	p.databaseFolder = p.mainFolder;
	p.downloadWebpagesToDatabaseAdaptive(propertyurls,"main_960px",sourceName,startFresh);		
}

/**
 * Parses the HTML strings in the raw HTML database to RecordLookChem objects
 * @return	A vector of RecordLookChem objects containing the data from the raw HTML database
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
			String url = rs.getString("url");
			Document doc = Jsoup.parse(html);
			
			RecordChemicalBook rcb=new RecordChemicalBook();
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

public static void main(String args[]) {
	downloadWebpagesFromExcelToDatabase("Data" + "/list_chemicals-2020-11-23-09-54-12.xls",1,10,true);
	Vector<RecordChemicalBook> rcb = parseWebpagesInDatabase();
	for (int i = 0; i < rcb.size(); i++) {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		System.out.println(gson.toJson(rcb.get(i)));

	}
	
}


}
