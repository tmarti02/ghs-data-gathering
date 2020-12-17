package gov.epa.exp_data_gathering.eChemPortalAPI;


import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.ResultsPage;
import gov.epa.ghs_data_gathering.Database.CreateGHS_Database;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Builds and runs queries on the eChemPortal API
 * @author GSINCL01
 *
 */
public class QueryHandler {
	public Gson gson = null;
	public Gson prettyGson = null;
	private Logger logger = null;
	private int limit;
	
	/**
	 * Creates a QueryHandler with a given page limit
	 * Optimal page limit seems to be ~5000, but may change with internet connection
	 * @param limit		Page limit
	 */
	public QueryHandler(int limit) {
		gson = new GsonBuilder().create();
		prettyGson = new GsonBuilder().setPrettyPrinting().create();
		logger = (Logger) LoggerFactory.getLogger("org.apache.http");
		// Can adjust debug logging as desired
    	logger.setLevel(Level.WARN);
    	logger.setAdditive(false);
    	Unirest.setTimeouts(0, 0);
    	this.limit = limit;
	}
	
	/**
	 * Translates our endpoint identifiers to eChemPortal's
	 * @param propertyName	Endpoint to query from ExperimentalConstants
	 * @return				eChemPortal's corresponding endpoint identifier
	 */
	private static String getEndpointKind(String propertyName) {
		String endpointKind = "";
		switch (propertyName) {
		case ExperimentalConstants.strMeltingPoint:
			endpointKind = "Melting";
			break;
		case ExperimentalConstants.strBoilingPoint:
			endpointKind = "BoilingPoint";
			break;
		case ExperimentalConstants.strFlashPoint:
			endpointKind = "FlashPoint";
			break;
		case ExperimentalConstants.strDensity:
			endpointKind = "Density";
			break;
		case ExperimentalConstants.strVaporPressure:
			endpointKind = "Vapour";
			break;
		case ExperimentalConstants.strLogKow:
			endpointKind = "Partition";
			break;
		case ExperimentalConstants.strWaterSolubility:
			endpointKind = "WaterSolubility";
			break;
		case ExperimentalConstants.str_pKA:
			endpointKind = "DissociationConstant";
			break;
		case ExperimentalConstants.strHenrysLawConstant:
			endpointKind = "HenrysLawConstant";
			break;
		}
		return endpointKind;
	}
	
	/**
	 * Creates an API query with indicated options
	 * @param propertyName				Endpoint to query from ExperimentalConstants
	 * @param maxReliabilityLevel		Maximum reliability to return (1 = most reliable, 4 = least; 2 recommended)
	 * @param endpointMin				Minimum (inclusive) value for endpoint (may be null)
	 * @param endpointMax				Maximum (inclusive) value for endpoint (may be null)
	 * @param endpointUnits				Endpoint units from ExperimentalConstants
	 * @param pressureMin				Minimum (inclusive) value for pressure condition (may be null)
	 * @param pressureMax				Maximum (inclusive) value for pressure condition (may be null)
	 * @param pressureUnits				Pressure condition units from ExperimentalConstants
	 * @param temperatureMin			Minimum (inclusive) value for temperature condition (may be null)
	 * @param temperatureMax			Maximum (inclusive) value for temperature condition (may be null)
	 * @param temperatureUnits			Temperature condition units from ExperimentalConstants
	 * @param pHMin						Minimum (inclusive) value for pH condition (may be null)
	 * @param pHMax						Maximum (inclusive) value for pH condition (may be null)
	 * @return							A Query object with the indicated options
	 */
	public Query generateQuery(String propertyName,int maxReliabilityLevel,
			String endpointMin,String endpointMax,String endpointUnits,
			String pressureMin,String pressureMax,String pressureUnits,
			String temperatureMin,String temperatureMax,String temperatureUnits,
			String pHMin,String pHMax) {
		String endpointKind = getEndpointKind(propertyName);
		QueryBlock queryBlock = new QueryBlock(endpointKind);
		queryBlock.addInfoTypeField();
		queryBlock.addReliabilityField(maxReliabilityLevel);
		
		// Disambiguates potential unit collisions between density and solubility, pressure and HLC
		if (endpointUnits.equals(ExperimentalConstants.str_g_L) || endpointUnits.equals(ExperimentalConstants.str_g_cm3) ||
				endpointUnits.equals(ExperimentalConstants.str_kg_m3)) {
			if (endpointKind.equals("Density")) {
				endpointUnits += "_density";
			} else if (endpointKind.equals("WaterSolubility")) {
				endpointUnits += "_solubility";
			}
		} else if (endpointUnits.equals(ExperimentalConstants.str_atm)) {
			if (endpointKind.equals("Vapour")) {
				endpointUnits += "_VP";
			} else if (endpointKind.equals("HenrysLawConstant")) {
				endpointUnits += "_H";
			}
		}
		
		// Endpoint range
		queryBlock.addEndpointField(endpointMin,endpointMax,endpointUnits);
		
		// Pressure condition
		if ((endpointKind.equals("Melting") || endpointKind.equals("BoilingPoint") || endpointKind.equals("FlashPoint") || endpointKind.equals("HenrysLawConstant"))
				&& (pressureMin!=null || pressureMax!=null || pressureUnits!=null)) {
			queryBlock.addAtmPressureField(pressureMin,pressureMax,pressureUnits);
		} else if (pressureMin!=null || pressureMax!=null || pressureUnits!=null) {
			System.out.println("Warning: Pressure conditions not supported for "+propertyName+". Non-null values ignored.");
		}
		
		// Temperature condition
		if ((endpointKind.equals("Density") || endpointKind.equals("Vapour") || endpointKind.equals("Partition") || 
				endpointKind.equals("WaterSolubility") || endpointKind.equals("DissociationConstant") || endpointKind.equals("HenrysLawConstant"))
				&& (temperatureMin!=null || temperatureMax!=null || temperatureUnits!=null)) {
			queryBlock.addTemperatureField(temperatureMin,temperatureMax,temperatureUnits);
		} else if (temperatureMin!=null || temperatureMax!=null || temperatureUnits!=null) {
			System.out.println("Warning: Temperature conditions not supported for "+propertyName+". Non-null values ignored.");
		}
		
		// pH condition
		if ((endpointKind.equals("Partition") || endpointKind.equals("WaterSolubility")) && (pHMin!=null || pHMax!=null)) {
			queryBlock.addpHField(pHMin,pHMax);
		} else if (pHMin!=null || pHMax!=null) {
			System.out.println("Warning: pH conditions not supported for "+propertyName+". Non-null values ignored.");
		}
		
		// Endpoint-specific necessary fields
		if (endpointKind.equals("Partition")) {
			queryBlock.addPartitionCoefficientFields();
		} else if (endpointKind.equals("WaterSolubility")) {
			queryBlock.addWaterSolubilityFields();
		}
		
		PropertyBlock propertyBlock = new PropertyBlock(0,"property",queryBlock);
		Query query = new Query(propertyBlock,limit);
		return query;
	}
	
	/**
	 * Gets a single page of query results
	 * @param query		The query to get results for
	 * @return			A page of query results as a ResultsPage object
	 */
	private ResultsPage getResultsPage(Query query) {
		ResultsPage page = null;
		String bodyString = gson.toJson(query);
		try {	
			HttpResponse<String> response = Unirest.post("https://www.echemportal.org/echemportal/api/property-search")
			  .header("Content-Type", "application/json")
			  .header("Accept", "application/json")
			  .body(bodyString)
			  .asString();
			Thread.sleep(500);
			String json = response.getBody();
			page = gson.fromJson(json, ResultsPage.class);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return page;
	}
	
	/**
	 * Runs the API query described by a Query object and gets results as a vector of ResultsPage objects
	 * @param query	API query to run
	 * @return		Results as a vector of ResultsPage objects
	 */
	private Vector<ResultsPage> getQueryResults(Query query) {
		Vector<ResultsPage> results = new Vector<ResultsPage>();
		int totalResults = getQueryTotalResults(query);
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
			
			ResultsPage page = getResultsPage(query);
			String strPage = prettyGson.toJson(page).replaceAll("—","-");
			
			RawDataEChemPortalAPI data = new RawDataEChemPortalAPI(strDate,strQuery,strPage);
			data.addRecordToDatabase(conn);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Runs the API query described by a Query object and downloads results to a database
	 * @param query			API query to run
	 * @param startFresh	True to rebuild database, false to append to existing database
	 */
	public void downloadQueryResultsToDatabase(Query query,boolean startFresh) {
		ParseEChemPortalAPI p = new ParseEChemPortalAPI();
		String databaseName = p.sourceName+"_raw_json.db";
		String databasePath = p.databaseFolder+File.separator+databaseName;
		String tableName = "results";
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		java.sql.Connection conn = CreateGHS_Database.createDatabaseTable(databasePath, tableName, RawDataEChemPortalAPI.fieldNames, startFresh);
		
		try {
			int totalResults = getQueryTotalResults(query);
			System.out.println("Found "+totalResults+" results. Downloading to eChemPortalAPI_raw_json.db...");
			
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
	 * Runs a tiny version of the query to get total results as fast as possible
	 * @param query		The query to find results for
	 * @return			The total number of results returned by the query
	 */
	public int getQueryTotalResults(Query query) {
		int originalLimit = query.paging.limit;
		query.paging.limit = 1;
		ResultsPage page = getResultsPage(query);
		int totalResults = page.pageInfo.totalElements;
		query.paging.limit = originalLimit;
		return totalResults;
	}

	public static void main(String[] args) {
		// TODO
	}
}
