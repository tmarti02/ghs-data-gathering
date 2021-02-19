package gov.epa.exp_data_gathering.parse.MetabolomicsWorkbench;

import java.io.File;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import gov.epa.api.RawDataRecord;
import gov.epa.database.SQLite_CreateTable;
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.ghs_data_gathering.Utilities.FileUtilities;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

public class RecordRefMet {
	String regno;
	String refMetName;
	String systematicName;
	String smiles;
	String sumComposition;
	String exactMass;
	String formula;
	String inchi;
	String inchiKey;
	String superClass;
	String mainClass;
	String subClass;
	String pubChemCID;
	String annotationLevel;
	String molFile;
	String dateAccessed;
	String url;
	
	static final String[] fieldNames = {"regno","refMetName","systematicName","smiles","sumComposition","exactMass","formula",
			"inchi","inchiKey","superClass","mainClass","subClass","pubChemCID","annotationLevel","molFile","dateAccessed","url"};
	
	RecordRefMet() {} // Default constructor
	
	RecordRefMet(RecordMetaboliteDatabase rmd) { // Construct partial record from RecordMetaboliteDatabase
		regno = rmd.regno;
		systematicName = rmd.systematicName;
		pubChemCID = rmd.pubChemCID;
		formula = rmd.formula;
		exactMass = rmd.exactMass;
		dateAccessed = rmd.dateAccessed;
		url = rmd.url;
	}
	
	private static List<String> getRefMetNamesFromMetaboliteDatabaseRecords(List<RecordMetaboliteDatabase> records) {
		List<String> refMetNames = new ArrayList<String>();
		HashMap<String,String> hmCIDToRefMet = new HashMap<String,String>();
		
		ArrayList<String> lines = Utilities.readFileToArray("Data\\refmet.txt");
		for (int i = 1; i < lines.size(); i++) { // First line is header
			String[] vals = lines.get(i).split("\t",-1);

			if (vals.length>7) {
				String cid = vals[7];
				String refMetName = vals[0];
				if (cid!=null && !cid.isBlank()) {
					hmCIDToRefMet.put(cid, refMetName);
				}
			}
		}
		
		Logger logger = (Logger) LoggerFactory.getLogger("org.apache.http");
		logger.setLevel(Level.WARN);
    	logger.setAdditive(false);
		
		for (RecordMetaboliteDatabase rec:records) {		
			String cid = rec.pubChemCID;
			String refMetName = hmCIDToRefMet.get(cid);
			if (refMetName!=null) {
				refMetNames.add(refMetName);
			}
		}
		
		return refMetNames;
	}
	
	private static void downloadRefMetPagesToDatabase(List<String> refMetNames, boolean startFresh, String databasePath) {
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		String tableName = "RefMet";
		java.sql.Connection conn=SQLite_CreateTable.create_table(databasePath, tableName, RawDataRecord.fieldNames, startFresh);
    	
    	int size = refMetNames.size();
    	HashSet<String> hsURLs = new HashSet<String>();
    	if (!startFresh) {
	    	ResultSet rs = SQLite_GetRecords.getAllRecords(SQLite_Utilities.getStatement(conn), tableName);
			try {
				while (rs.next()) {
					hsURLs.add(rs.getString("url"));
				}
				System.out.println(hsURLs.size()+" pages already downloaded. Continuing download...");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
    	} else {
    		System.out.println(size+" pages to be downloaded. Starting download...");
    	}
		
//		Random rand = new Random();
//		long delay = 0;
		int countSuccess = 0;
		for (int i = 0; i < size; i++) {
			String refMetName = refMetNames.get(i);
			try {
				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
				Date date = new Date();  
				String strDate=formatter.format(date);
				
				String safeRefMetName = URLEncoder.encode(refMetName.replaceAll("\"", ""),"UTF-8");
				String getURL = "https://www.metabolomicsworkbench.org/databases/refmet/refmet_details.php?REFMET_NAME="+safeRefMetName;
				if (!startFresh && !hsURLs.add(getURL)) { continue; }
				
//				long startTime=System.currentTimeMillis();
				String content = getOneRefMetPage(getURL);
//				long endTime=System.currentTimeMillis();
//				delay = endTime-startTime;
				
				if (content!=null) {
					RawDataRecord rec=new RawDataRecord(strDate, getURL, content.replaceAll("'", "''").replaceAll(";", "\\;"));
					rec.addRecordToDatabase(tableName, conn);
					countSuccess++;
					if (countSuccess % 500 == 0) {
						System.out.println("Downloaded "+countSuccess+" pages successfully.");
					}
				} else {
					System.out.println("Failed to download page for "+refMetName+".");
				}
				
				Thread.sleep(200);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("Downloaded "+countSuccess+" pages successfully. Done!");
	}
	
	private static String getOneRefMetPage(String getURL) {
		try {
			HttpClient httpclient = HttpClients.createDefault();
			HttpGet httpget = new HttpGet(getURL);

			//Execute and get the response.
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity);
			
//			System.out.println(result);
			
			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	private static void checkRefMetPages(List<RecordMetaboliteDatabase> records, int start) {
		HashMap<String,String> hmCIDToRefMet = new HashMap<String,String>();
		ArrayList<String> lines = Utilities.readFileToArray("Data\\refmet.txt");
		for (int i = 1; i < lines.size(); i++) { // First line is header
			String[] vals = lines.get(i).split("\t",-1);
	
			if (vals.length>7) {
				String cid = vals[7];
				String refMetName = vals[0];
				if (cid!=null && !cid.isBlank()) {
					hmCIDToRefMet.put(cid, refMetName);
				}
			}
		}
	
		Logger logger = (Logger) LoggerFactory.getLogger("org.apache.http");
		logger.setLevel(Level.WARN);
    	logger.setAdditive(false);
    	
		int count = 0;
		int countSuccess = 0;
		int countTrueFailure = 0;
		System.out.println(records.size()+" records to be checked; starting at index "+start+".");
		for (int i = start; i < records.size(); i++) {		
			RecordMetaboliteDatabase rec = records.get(i);
			String cid = rec.pubChemCID;
			String refMetName = hmCIDToRefMet.get(cid);
			if (refMetName!=null) {
				countSuccess++;
			} else if (cid!=null && !cid.isBlank() && !cid.equals("-")) {
				try {
					Thread.sleep(200);
					String check = checkOneRefMetPage(cid);
					if (!check.equals("No matches in database for this query.")) {
						System.out.println("Failed to retrieve existing RefMet record for CID "+cid);
						countTrueFailure++;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			count++;
			if (count % 1000==0) {
				System.out.println("Searched "+count+" CIDs; "+countSuccess+" successfully matched to RefMet names; "+countTrueFailure+" existing RefMet pages missed.");
			}
		}
		System.out.println("Searched "+count+" CIDs; "+countSuccess+" successfully matched to RefMet names; "+countTrueFailure+" existing RefMet pages missed.");
	}
	
	private static String checkOneRefMetPage(String cid) {
		try {
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost("https://www.metabolomicsworkbench.org/databases/refmet/refmet_tableonlyM.php");

			// Request parameters and other properties.
			List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>(2);
			params.add(new BasicNameValuePair("page", "1"));
			params.add(new BasicNameValuePair("PUBCHEM_CID", cid));
			params.add(new BasicNameValuePair("SORT", "r.name"));
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

			//Execute and get the response.
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity);
			
//			System.out.println(result);
			
			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	static List<RecordRefMet> parseRefMetPagesInDatabase(String databasePath) {
		List<RecordRefMet> records = new ArrayList<RecordRefMet>();

		try {
			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = SQLite_GetRecords.getAllRecords(stat, "RefMet");

			while (rs.next()) {
				String html = rs.getString("content");
				String url = rs.getString("url");
				String date = rs.getString("date");
				Document doc = Jsoup.parse(html);
				
				RecordRefMet rec = new RecordRefMet();
				rec.dateAccessed = date.substring(0,date.indexOf(" "));
				rec.url = url;
				
				parseRefMetPageDocument(rec,doc);

				records.add(rec);
			}
			
			return records;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static void parseRefMetPageDocument(RecordRefMet rec, Document doc) {
		Pattern molFilePattern = Pattern.compile("window\\.onload=ketcher.showMolfileOpts\\('GGAKetcher1', '(.*END)'");
		Elements scripts = doc.getElementsByTag("script");
		for (Element script:scripts) {
			String data = script.data();
			if (data.contains("window.onload=ketcher.showMolfileOpts(")) {
				Matcher molFileMatcher = molFilePattern.matcher(data);
				if (molFileMatcher.find()) {
					rec.molFile = StringEscapeUtils.unescapeJava(molFileMatcher.group(1));
				}
			}
		}
		
		Element table = doc.selectFirst("table.datatable");
		if (table!=null) {
			Elements rows = table.getElementsByTag("tr");
			if (!rows.isEmpty()) {
				for (Element row:rows) {
					Element th = row.selectFirst("th");
					Element td = row.selectFirst("td");
					String header = th.text();
					switch (header) {
					case "MW structure":
						rec.regno = td.selectFirst("a").text();
						break;
					case "Main Class":
						rec.mainClass = td.selectFirst("a").text();
						break;
					case "Sub Class":
						rec.subClass = td.selectFirst("a").text();
						break;
					case "Pubchem CID":
						rec.pubChemCID = td.selectFirst("a").text();
						break;
					case "RefMet name":
						rec.refMetName = td.text();
						break;
					case "Systematic name":
						rec.systematicName = td.text();
						break;
					case "SMILES":
						rec.smiles = td.text();
						break;
					case "Sum Composition":
						rec.sumComposition = td.text();
						break;
					case "Exact mass":
						String text = td.text();
						rec.exactMass = text.substring(0,text.indexOf(" "));
						break;
					case "Formula":
						rec.formula = td.text();
						break;
					case "InChI":
						rec.inchi = td.text();
						break;
					case "InChIKey":
						rec.inchiKey = td.text();
						break;
					case "Super Class":
						rec.superClass = td.text();
						break;
					case "Annotation level":
						rec.annotationLevel = td.text();
						break;
					}
				}
			}
		}
	}
	
	String[] setSafeValuesForDatabase() {
		String safeRefMetName = refMetName==null ? null : refMetName.replaceAll("'", "''").replaceAll(";", "\\;");
		String safeSystematicName = systematicName==null ? null : systematicName.replaceAll("'", "''").replaceAll(";", "\\;");
		String safeSumComposition = sumComposition==null ? null : sumComposition.replaceAll("'", "''").replaceAll(";", "\\;");
		String safeSuperClass = superClass==null ? null : superClass.replaceAll("'", "''").replaceAll(";", "\\;");
		String safeMainClass = mainClass==null ? null : mainClass.replaceAll("'", "''").replaceAll(";", "\\;");
		String safeSubClass = subClass==null ? null : subClass.replaceAll("'", "''").replaceAll(";", "\\;");
		String [] values= {regno,safeRefMetName,safeSystematicName,
				smiles,safeSumComposition,exactMass,formula,inchi,inchiKey,
				safeSuperClass,safeMainClass,safeSubClass,
				pubChemCID,annotationLevel,molFile,
				dateAccessed,url};
		return values;
	}
	
	public static void main(String[] args) {
		String databasePath = "Data\\Experimental\\MetabolomicsWorkbench\\MetabolomicsWorkbench.db";
		List<RecordMetaboliteDatabase> records = RecordMetaboliteDatabase.parseMetaboliteDatabaseTablesInDatabase(databasePath);
//		List<String> refMetNames = getRefMetNamesFromMetaboliteDatabaseRecords(records);
//		downloadRefMetPagesToDatabase(refMetNames, false, databasePath);
		checkRefMetPages(records,50000);
	}
}
