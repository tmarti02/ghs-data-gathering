package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.ghs_data_gathering.Database.MySQL_DB;
import gov.epa.ghs_data_gathering.GetData.RecordDashboard;


public class RecordLookChem {
	String CAS;
	String chemicalName;
	String formula;
	String molecularWeight;
	String synonyms;
	String EINECS;
	String density;
	String meltingPoint;
	String boilingPoint;
	String flashPoint;
	String solubility;
	String appearance;
	String riskCodes;
	String safety;
	String transportInformation;
	String fileName;
	
	static final String sourceName=ExperimentalConstants.strSourceLookChem;

	/**
	 * Gets a list (or sublist) of chemicals from an Excel file and downloads the corresponding LookChem pages to a zip folder
	 * List must be downloaded from the CompTox dashboard or formatted to match
	 * @param filename	The file or path to the list
	 * @param start		The index in the list to start downloading
	 * @param end		The index in the list to stop downloading
	 */
	public static void downloadWebpagesFromExcelToZipFile(String filename,int start,int end) {
		Vector<RecordDashboard> records = Parse.getDashboardRecordsFromExcel(filename);
		Vector<String> urls = getURLsFromDashboardRecords(records,start,end);

		ParseLookChem p = new ParseLookChem();
		p.downloadWebpagesToZipFile(urls);	
	}
	
	/**
	 * Gets a list (or sublist) of chemicals from an Excel file and downloads the corresponding LookChem pages to a database
	 * List must be downloaded from the CompTox dashboard or formatted to match
	 * @param filename		The file or path to the list
	 * @param start			The index in the list to start downloading
	 * @param end			The index in the list to stop downloading
	 * @param startFresh	True to remake database table completely, false to append new records to existing table
	 */
	public static void downloadWebpagesFromExcelToDatabase(String filename,int start,int end,boolean startFresh) {
		Vector<RecordDashboard> records = Parse.getDashboardRecordsFromExcel(filename);
		Vector<String> urls = getURLsFromDashboardRecords(records,start,end);

		ParseLookChem p = new ParseLookChem();
		p.mainFolder = p.mainFolder + File.separator + "General";
		p.databaseFolder = p.mainFolder + File.separator + "databases";
		p.downloadWebpagesToDatabaseAdaptive(urls,"reir_l_info_table",sourceName,startFresh);		
	}
	
	/**
	 * Extracts CAS RNs from CompTox dashboard records and translates them to LookChem URLs
	 * @param records	A vector of RecordDashboard objects
	 * @param start		The index in the vector to start converting
	 * @param end		The index in the vector to stop converting
	 * @return			A vector of LookChem URLs as strings
	 */
	private static Vector<String> getURLsFromDashboardRecords(Vector<RecordDashboard> records,int start,int end) {
		String baseURL = "https://www.lookchem.com/cas-";
		Vector<String> urls = new Vector<String>();
		for (int i = start; i < end; i++) {
			String CAS = records.get(i).CASRN;
			if (!CAS.startsWith("NOCAS")) {
				String prefix = CAS.substring(0,3);
				if (prefix.charAt(2)=='-') { prefix = prefix.substring(0,2); }
				urls.add(baseURL+prefix+"/"+CAS+".html");
			}
		}
		return urls;
	}
	
	/**
	 * Parses an HTML file to a RecordLookChem object and prints it out in JSON format
	 * Unused in final code - for output checking and debugging
	 * @param file	The HTML file to parse
	 * @return		A RecordLookChem object containing the data from the HTML file
	 */
	private static RecordLookChem parseWebpage(File file) {
		RecordLookChem lcr = new RecordLookChem();
		
		try {
			lcr.fileName = file.getName();
			Document doc = Jsoup.parse(file, "UTF-8");
			
			parseDocument(lcr,doc);
			
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			System.out.println(gson.toJson(lcr));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return lcr;
	}
	
	/**
	 * Parses a single zipped HTML file to a RecordLookChem object
	 * @param zipFile	The zip file containing the HTML file to be parsed
	 * @param zipEntry	The zipped HTML file to be parsed
	 * @return			A RecordLookChem object containing the data from the HTML file
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private static RecordLookChem parseZipWebpage(ZipFile zipFile, ZipEntry zipEntry)
			throws IOException, UnsupportedEncodingException {
		
		RecordLookChem lcr=new RecordLookChem();
		
		String filename=zipEntry.getName().replace("web pages\\", "");
		lcr.fileName = filename;
		Document doc = Jsoup.parse(zipFile.getInputStream(zipEntry),"UTF-8","");
		
		parseDocument(lcr, doc);
		
		return lcr;
	}
	
	/**
	 * Parses the HTML files in a zip file to RecordLookChem objects
	 * @param zipFilePath	The path to the zip file containing the HTML files to be parsed
	 * @return				A vector of RecordLookChem objects containing the data from the HTML files
	 */
	public static Vector<RecordLookChem> parseWebpagesInZipFile() {
		String folderNameWebpages = "web pages";
		String mainFolder = "Data"+File.separator+"Experimental"+ File.separator + sourceName;
		String zipFilePath = mainFolder + File.separator+folderNameWebpages+".zip";
		Vector<RecordLookChem> records = new Vector<>();

		try {
			ZipFile zipFile = new ZipFile(zipFilePath);

			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			int counter = 1;
			while (entries.hasMoreElements()) {
				if (counter % 100==0) { System.out.println("Parsed "+counter+" pages"); }
				
				ZipEntry zipEntry = entries.nextElement();

				RecordLookChem lcr = parseZipWebpage(zipFile, zipEntry);

				if (lcr.CAS != null) {
					records.add(lcr);
					counter++;
				} else {
					File badFile=new File(mainFolder+zipEntry.getName());
					if (badFile.exists()) badFile.delete();
				}
			}
			
			System.out.println("Parsed "+(counter-1)+" pages");
			return records;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Parses the HTML strings in the raw HTML database to RecordLookChem objects
	 * @return	A vector of RecordLookChem objects containing the data from the raw HTML database
	 */
	public static Vector<RecordLookChem> parseWebpagesInDatabase() {
		String databaseFolder = "Data"+File.separator+"Experimental"+ File.separator + sourceName + File.separator + "General" + File.separator + "databases";
		String databasePath = databaseFolder+File.separator+sourceName+"_raw_html.db";
		Vector<RecordLookChem> records = new Vector<>();

		try {
			Statement stat = MySQL_DB.getStatement(databasePath);
			ResultSet rs = MySQL_DB.getAllRecords(stat, ExperimentalConstants.strSourceLookChem);

			int counter = 1;
			while (rs.next()) {
				if (counter % 100==0) { System.out.println("Parsed "+counter+" pages"); }
				
				String html = rs.getString("content");
				String url = rs.getString("url");
				Document doc = Jsoup.parse(html);
				
				RecordLookChem lcr=new RecordLookChem();
				lcr.fileName=url.substring(url.lastIndexOf("/")+1, url.length());
				
				parseDocument(lcr,doc);

				if (lcr.CAS != null) {
					records.add(lcr);
					counter++;
				} else {
					// rs.updateString("html", ExperimentalConstants.strRecordUnavailable);
					// Updater doesn't work - JDBC version issue?
				}
			}
			
			System.out.println("Parsed "+(counter-1)+" pages");
			return records;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Parses a jSoup Document object to a RecordLookChem object
	 * @param lcr	The RecordLookChem object to store data
	 * @param doc	The Document object to be parsed
	 */
	private static void parseDocument(RecordLookChem lcr, Document doc) {
		Element basicInfo = doc.selectFirst("table");
		if (basicInfo != null) { 
			Elements rows = basicInfo.getElementsByTag("tr");
			if (!rows.isEmpty()) {
				for (Element row:rows) {
					String header = row.getElementsByTag("th").text();
					String data = row.getElementsByTag("td").text();
					// Will need to check & adjust these conditions as necessary if other pages formatted differently
					if (data != null && !data.isBlank()) {
						if (header.contains("CAS No")) { lcr.CAS = data;
						} else if (header.contains("Name")) { lcr.chemicalName = data;
						} else if (header.contains("Formula")) { lcr.formula = data;
						} else if (header.contains("Molecular Weight")) { lcr.molecularWeight = data;
						} else if (header.contains("Synonyms")) { lcr.synonyms = data;
						} else if (header.contains("EINECS")) { lcr.EINECS = data;
						} else if (header.contains("Density")) { lcr.density = data;
						} else if (header.contains("Melting Point")) { lcr.meltingPoint = data;
						} else if (header.contains("Boiling Point")) { lcr.boilingPoint = data;
						} else if (header.contains("Flash Point")) { lcr.flashPoint = data;
						} else if (header.contains("Solubility")) {	lcr.solubility = data;
						} else if (header.contains("Appearance")) { lcr.appearance = data;
						} else if (header.contains("Risk Codes")) { lcr.riskCodes = data;
						} else if (header.contains("Safety")) { lcr.safety = data;
						} else if (header.contains("Transport Information")) { lcr.transportInformation = data; }
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		downloadWebpagesFromExcelToDatabase("Data"+"/ALLCAS.xlsx",11000,15000,false);
	}
	
}
