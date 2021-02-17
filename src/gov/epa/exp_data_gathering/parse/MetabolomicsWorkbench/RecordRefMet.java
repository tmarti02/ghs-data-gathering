package gov.epa.exp_data_gathering.parse.MetabolomicsWorkbench;

import java.io.File;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import gov.epa.api.RawDataRecord;
import gov.epa.database.SQLite_CreateTable;
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
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
	String pubchemCID;
	String annotationLevel;
	String molFile;
	String dateAccessed;
	
	private static List<String> getRefMetNamesFromMetaboliteDatabaseRecords(List<RecordMetaboliteDatabase> records) {
		List<String> refMetNames = new ArrayList<String>();
		HashMap<String,String> hmCIDToRefMet = new HashMap<String,String>();
		
		ArrayList<String> lines = Utilities.readFileToArray("Data\\refmet.csv");
		for (int i = 1; i < lines.size(); i++) { // First line is header
			String[] vals = lines.get(i).split(",",-1);

			if (vals.length==8) {
				String cid = vals[7];
				String refMetName = vals[0];
				if (cid!=null && !cid.isBlank()) {
					hmCIDToRefMet.put(cid, refMetName);
				}
			}
		}
		
		for (RecordMetaboliteDatabase rec:records) {		
			String cid = rec.pubchemCID;
			String refMetName = hmCIDToRefMet.get(cid);
			refMetNames.add(refMetName);
		}
		
		return refMetNames;
	}
	
	private static void downloadRefMetPagesToDatabase(List<String> refMetNames, String databasePath) {
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		String tableName = "RefMet";
		java.sql.Connection conn=SQLite_CreateTable.create_table(databasePath, tableName, RawDataRecord.fieldNames, true);
		
		Logger logger = (Logger) LoggerFactory.getLogger("org.apache.http");
		logger.setLevel(Level.WARN);
    	logger.setAdditive(false);
		
		Random rand = new Random();
		long delay = 0;
		int countSuccess = 0;
		for (String refMetName:refMetNames) {
			if (refMetName==null) { continue; }
			try {
				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
				Date date = new Date();  
				String strDate=formatter.format(date);
				
				String safeRefMetName = URLEncoder.encode(refMetName.replaceAll("\"", ""),"UTF-8");
				String getURL = "https://www.metabolomicsworkbench.org/databases/refmet/refmet_details.php?REFMET_NAME="+safeRefMetName;
				
				long startTime=System.currentTimeMillis();
				String content = getOneRefMetPage(getURL);
				long endTime=System.currentTimeMillis();
				delay = endTime-startTime;
				
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
				
				Thread.sleep((long) (delay*(1+rand.nextDouble())));
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
	
	static List<RecordRefMet> parseRefMetPagesInDatabase(String databasePath) {
		List<RecordRefMet> records = new ArrayList<RecordRefMet>();
		
		try {
			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = SQLite_GetRecords.getAllRecords(stat, "RefMet");

			while (rs.next()) {
				String html = rs.getString("content");
				String date = rs.getString("date");
				Document doc = Jsoup.parse(html);
				
				RecordRefMet rec = new RecordRefMet();
				rec.dateAccessed = date.substring(0,date.indexOf(" "));
				
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
					case "PubChem CID":
						rec.pubchemCID = td.selectFirst("a").text();
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
						rec.exactMass = td.text();
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
	
	public static void main(String[] args) {
		String databasePath = "Data\\Experimental\\MetabolomicsWorkbench\\MetabolomicsWorkbenchTest.db";
		List<RecordMetaboliteDatabase> records = RecordMetaboliteDatabase.parseMetaboliteDatabaseTablesInDatabase(databasePath);
		List<String> refMetNames = getRefMetNamesFromMetaboliteDatabaseRecords(records);
		downloadRefMetPagesToDatabase(refMetNames, databasePath);
//		List<RecordRefMet> records = parseRefMetPagesInDatabase(databasePath);
//		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
//		System.out.println(prettyGson.toJson(records));
	}
}
