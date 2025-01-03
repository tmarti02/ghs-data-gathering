package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query;

import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Processing.RawDataRecord;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query.APIJSONs.ResultsPage;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Utility.SQLiteDatabase;

/**
 * Runs queries on the eChemPortal API
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class QueryHandler {
	private Gson gson = null;
	private Gson prettyGson = null;
	private Logger logger = null;
	private long wait;
	private int retryLimit;
	
	/**
	 * Creates a new QueryHandler with the specified wait time and retry limit
	 * @param wait			How long to wait between queries
	 * @param retryLimit	How many times to retry failed queries
	 */
	public QueryHandler(long wait, int retryLimit) {
		this.wait = wait;
		this.retryLimit = retryLimit;
		gson = new GsonBuilder().create();
		prettyGson = new GsonBuilder().setPrettyPrinting().create();
		logger = (Logger) LoggerFactory.getLogger("org.apache.http");
		// org.apache.http generates a huge amount of debug logging output if level is not set to WARN or below
    	logger.setLevel(Level.WARN);
    	logger.setAdditive(false);
    	Unirest.setTimeouts(0, 0);
	}
	
	/**
	 * Gets a single page of query results
	 * @param query		The query to get results for
	 * @return			A page of query results as a ResultsPage object
	 */
	private ResultsPage getResultsPage(Query query) {
		ResultsPage page = null;
		String bodyString = gson.toJson(query);
		HttpResponse<String> response = null;
		try {
			boolean downloaded = false;
			int retryCount = 0;
			while (retryCount < retryLimit && (!downloaded || response==null)) {
				try {
				response = Unirest.post("https://www.echemportal.org/echemportal/api/property-search")
						.header("Accept", "application/json")
						.header("Accept-Encoding", "gzip, deflate, br")
						.header("Accept-Language", "en-US,en;q=0.9")
						.header("Connection", "keep-alive")
						.header("Content-Type", "application/json")
						.header("Host", "www.echemportal.org")
						.header("Origin", "https://www.echemportal.org")
						.header("Referer", "https://www.echemportal.org/echemportal/property-search")
						.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
						.body(bodyString)
						.asString();
				downloaded = true;
				Thread.sleep(wait);
				} catch (Exception ex) {
					wait += wait; // Increments default wait time every time a download fails
					System.out.println("Download failed! Waiting "+wait+" ms to try again...");
					Thread.sleep(wait);
					System.out.println("Trying again...");
				}
				retryCount++;
			}
			String json = response.getBody();
			page = gson.fromJson(json, ResultsPage.class);
			return page;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Runs a full API query with paging and gets results as a vector of ResultsPage objects
	 * @param query	API query to run
	 * @return		Results as a vector of ResultsPage objects
	 */
	private List<ResultsPage> getQueryResults(Query query) {
		List<ResultsPage> results = new ArrayList<ResultsPage>();
		int totalResults = getQuerySize(query);
		System.out.println("Found "+totalResults+" results. Downloading...");
		while (query.paging.offset < totalResults) {
			ResultsPage page = getResultsPage(query);
			results.add(page);
			query.updateOffset();
		}
		return results;
	}
	
	/**
	 * Downloads a single page of query results to a database
	 * @param query		The query to get results for
	 * @param conn		The Connection to the desired database
	 */
	private void downloadResultsPageToDatabase(Query query,Connection conn) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  	
		try {
			Date date = new Date();  
			String strDate=formatter.format(date);
			
			String strQuery = prettyGson.toJson(query);
			
			System.out.println(strQuery);
			
			
			ResultsPage page = getResultsPage(query);
			String strPage = prettyGson.toJson(page).replaceAll("—","-");
			
			RawDataRecord data = new RawDataRecord(strDate,strQuery,strPage);
			
			System.out.println("Page.length="+strPage.length());
			
			data.addRecordToDatabase(conn);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Runs a full API query with paging and downloads results to a database
	 * @param query			API query to run
	 * @param startFresh	True to rebuild database, false to append to existing database
	 */
	public void downloadQueryResultsToDatabase(Query query,String databasePath,boolean startFresh) {
		String tableName = "results";
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		java.sql.Connection conn = SQLiteDatabase.createTable(databasePath, tableName, RawDataRecord.fieldNames, startFresh);
		
		try {
			int totalResults = getQuerySize(query);
			System.out.println("Found "+totalResults+" results. Downloading to "+databasePath+"...");
			
			while (query.paging.offset < totalResults) {
				downloadResultsPageToDatabase(query,conn);
				query.updateOffset();
			}
			
			System.out.println("Done!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Runs an empty version of a query to get total results as fast as possible
	 * @param query		The query to find results for
	 * @return			The total number of results returned by the query
	 */
	int getQuerySize(Query query) {
		int originalLimit = query.paging.limit;
		query.paging.limit = 0;
		ResultsPage page = getResultsPage(query);
		if (page.pageInfo==null) {
			System.out.println("Warning: Query failed. There is a structural problem somewhere in this JSON!");
			System.out.println(prettyGson.toJson(query));
		}
		int totalResults = page.pageInfo.totalElements;
		query.paging.limit = originalLimit;
		return totalResults;
	}
}
