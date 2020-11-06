package gov.epa.exp_data_gathering.parse;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.AADashboard;
import gov.epa.api.ExperimentalConstants;
import gov.epa.api.HazardRecord;
import gov.epa.api.ScoreRecord;
import gov.epa.api.RawDataRecord;
import gov.epa.ghs_data_gathering.Database.CreateGHS_Database;
import gov.epa.ghs_data_gathering.Database.MySQL_DB;
import gov.epa.ghs_data_gathering.Utilities.FileUtilities;
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

	public static void downloadWebpagesFromExcelToZipFile(String filename,int start,int end) {
		String sourceName=ExperimentalConstants.strSourceLookChem;
		String mainFolder = AADashboard.dataFolder + File.separator + sourceName;
		String folderNameWebpages = "web pages";

		String destFolder=mainFolder+File.separator+folderNameWebpages;
		String destZipFolder=destFolder+".zip";
		
		String baseURL = "https://www.lookchem.com/cas-";
		
		Vector<RecordDashboard> records = Parse.getDashboardRecordsFromExcel(filename);
		Vector<String> urls = new Vector<String>();
		for (int i = start; i < end; i++) {
			String CAS = records.get(i).CASRN;
			String prefix = CAS.substring(0,3);
			if (prefix.charAt(2)=='-') { prefix = prefix.substring(0,2); }
			urls.add(baseURL+prefix+"/"+CAS+".html");
		}

		Parse.downloadWebpagesToZipFile(urls,sourceName,mainFolder);
		
	}
	
	public static void downloadWebpagesFromExcelToDatabase(String filename,int start,int end,boolean startFresh) {
		String tableName=ExperimentalConstants.strSourceLookChem;
		
		String baseURL = "https://www.lookchem.com/cas-";
		
		Vector<RecordDashboard> records = Parse.getDashboardRecordsFromExcel(filename);
		Vector<String> urls = new Vector<String>();
		for (int i = start; i < end; i++) {
			String CAS = records.get(i).CASRN;
			String prefix = CAS.substring(0,3);
			if (prefix.charAt(2)=='-') { prefix = prefix.substring(0,2); }
			urls.add(baseURL+prefix+"/"+CAS+".html");
		}

		Parse.downloadWebpagesToDatabase(urls,tableName,startFresh);
		
	}
	
	private static RecordLookChem parseWebpage(File file) {
		RecordLookChem lcr = new RecordLookChem();
		
		try {

			BufferedReader br=new BufferedReader(new FileReader(file));

			Document doc = Jsoup.parse(file, "UTF-8");

			parseDocument(file.getName(),lcr,doc);
			
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			
			System.out.println(gson.toJson(lcr));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return lcr;
	}
	
	private static RecordLookChem parseZipWebpage(ZipFile zipFile, ZipEntry zipEntry)
			throws IOException, UnsupportedEncodingException {
		
		RecordLookChem lcr=new RecordLookChem();

		InputStream input = zipFile.getInputStream(zipEntry);
		BufferedReader br = new BufferedReader(new InputStreamReader(input, "UTF-8"));

		Document doc = Jsoup.parse(zipFile.getInputStream(zipEntry),"UTF-8","");

		String filename=zipEntry.getName().replace("web pages\\", "");
		parseDocument(filename, lcr, doc);
		
		// GsonBuilder builder = new GsonBuilder();
		// builder.setPrettyPrinting();
		// Gson gson = builder.create();
		// System.out.println(gson.toJson(lcr));
		
		return lcr;
	}
	
	public static Vector<RecordLookChem> parseWebpagesInZipFile(String zipFilePath) {
		String sourceName=ExperimentalConstants.strSourceLookChem;
		String mainFolder = AADashboard.dataFolder + File.separator + sourceName;
		Vector<RecordLookChem> records = new Vector<>();

		try {

			ZipFile zipFile = new ZipFile(zipFilePath);

			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			// Eventually print this to show progress when handling multiple files
			int counter = 1;

			while (entries.hasMoreElements()) {

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
			
			return records;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Vector<RecordLookChem> parseWebpagesInDatabase() {
		Vector<RecordLookChem> records = new Vector<>();

		try {

			Statement stat = MySQL_DB.getStatement(Parse.pathRawHTMLDatabase);
			ResultSet rs = MySQL_DB.getAllRecords(stat, ExperimentalConstants.strSourceLookChem);

			while (rs.next()) {

				String html = rs.getString("html");
				String url = rs.getString("url");
				Document doc = Jsoup.parse(html);
				
				RecordLookChem lcr=new RecordLookChem();
				parseDocument(url,lcr,doc);

				if (lcr.CAS != null) {
					records.add(lcr);
				} else {
					rs.updateString("html", ExperimentalConstants.strRecordUnavailable);
				}
			}
			
			return records;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static void parseDocument(String filename, RecordLookChem lcr, Document doc) {
		
		try {
			Element basicInfoTable = doc.selectFirst("table");
			Elements rows = basicInfoTable.getElementsByTag("tr");
			
			for (Element row:rows) {
				String header = row.getElementsByTag("th").text();
				String data = row.getElementsByTag("td").text();
				// Will need to check & adjust these conditions as necessary if other pages formatted differently
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
				lcr.fileName=filename;
			}
		} catch (Exception ex) {
			System.out.println("No data in "+filename);
			lcr = null;
		}

	}
	
	public static void main(String[] args) {
		downloadWebpagesFromExcelToDatabase(AADashboard.dataFolder+"/PFASSTRUCT.xls",1,10,true);
	}
	
}
