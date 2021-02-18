package gov.epa.exp_data_gathering.parse.MetabolomicsWorkbench;

import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import gov.epa.api.RawDataRecord;
import gov.epa.database.SQLite_CreateTable;
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;

public class RecordMetaboliteDatabase {
	String regno;
	String commonName;
	String systematicName;
	String pubChemCID;
	String formula;
	String exactMass;
	String dateAccessed;
	String url;
	
	private static void downloadMetaboliteDatabaseTablesToDatabase(String databasePath) {
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		String tableName = "MetaboliteDatabase";
		java.sql.Connection conn=SQLite_CreateTable.create_table(databasePath, tableName, RawDataRecord.fieldNames, true);
		
		Logger logger = (Logger) LoggerFactory.getLogger("org.apache.http");
		logger.setLevel(Level.WARN);
    	logger.setAdditive(false);
		
		Random rand = new Random();
		long delay = 0;
		int countSuccess = 0;
		for (int i = 1; i <= 4530; i++) {
			try {
				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
				Date date = new Date();  
				String strDate=formatter.format(date);
				
				String url = "https://www.metabolomicsworkbench.org/data/mb_structure_tableonly.php?page="+i+"&SORT=m.refmet_name,b.name,b.sys_name";
				
				long startTime=System.currentTimeMillis();
				String content = getOneMetaboliteDatabaseTable(i);
				long endTime=System.currentTimeMillis();
				delay = endTime-startTime;
				
				if (content!=null) {
					RawDataRecord rec=new RawDataRecord(strDate, url, content.replaceAll("'", "''").replaceAll(";", "\\;"));
					rec.addRecordToDatabase(tableName, conn);
					countSuccess++;
					if (countSuccess % 500 == 0) {
						System.out.println("Downloaded "+countSuccess+" pages successfully.");
					}
				} else {
					System.out.println("Failed to download page "+i+".");
				}
				
				Thread.sleep((long) (delay*(1+rand.nextDouble())));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("Downloaded "+countSuccess+" pages successfully. Done!");
	}
	
	private static String getOneMetaboliteDatabaseTable(int page) {
		try {
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost("https://www.metabolomicsworkbench.org/data/mb_structure_tableonly.php");

			// Request parameters and other properties.
			List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>(2);
			params.add(new BasicNameValuePair("page", Integer.toString(page)));
			params.add(new BasicNameValuePair("SORT", "m.refmet_name,b.name,b.sys_name"));
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
	
	static List<RecordMetaboliteDatabase> parseMetaboliteDatabaseTablesInDatabase(String databasePath) {
		List<RecordMetaboliteDatabase> records = new ArrayList<RecordMetaboliteDatabase>();
		
		try {
			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = SQLite_GetRecords.getAllRecords(stat, "MetaboliteDatabase");

			while (rs.next()) {
				String html = rs.getString("content");
				String date = rs.getString("date");
				Document doc = Jsoup.parse(html);
				
				List<RecordMetaboliteDatabase> recs = new ArrayList<RecordMetaboliteDatabase>();
				// String dateAccessed = date.substring(0,date.indexOf(" "));
				// TODO Test database has different date format - change this back for final database
				String dateAccessed = date;

				parseMetaboliteDatabaseTableDocument(recs,doc,dateAccessed);

				records.addAll(recs);
			}
			
			return records;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static void parseMetaboliteDatabaseTableDocument(List<RecordMetaboliteDatabase> recs, Document doc, String dateAccessed) {
		Element table = doc.selectFirst("table");
		if (table!=null) {
			Elements rows = table.getElementsByTag("tr");
			if (!rows.isEmpty()) {
				for (Element row:rows) {
					RecordMetaboliteDatabase rec = new RecordMetaboliteDatabase();
					Elements cells = row.getElementsByTag("td");
					if (cells.size()==7) {
						String regnoURL = cells.get(0).getElementsByTag("a").first().attributes().get("href");
						rec.regno = regnoURL.substring(regnoURL.lastIndexOf("=")+1);
						rec.commonName = cells.get(2).text();
						rec.systematicName = cells.get(3).text();
						rec.pubChemCID = cells.get(4).text();
						rec.formula = cells.get(5).text();
						rec.exactMass = cells.get(6).text();
						rec.dateAccessed = dateAccessed;
						rec.url = "https://www.metabolomicsworkbench.org"+regnoURL;
						recs.add(rec);
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		downloadMetaboliteDatabaseTablesToDatabase("Data\\Experimental\\MetabolomicsWorkbench\\MetabolomicsWorkbench.db");
//		List<RecordMetaboliteDatabase> records = parseMetaboliteDatabaseTablesInDatabase("Data\\Experimental\\MetabolomicsWorkbench\\MetabolomicsWorkbenchTest.db");
//		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
//		System.out.println(prettyGson.toJson(records));
	}
}
