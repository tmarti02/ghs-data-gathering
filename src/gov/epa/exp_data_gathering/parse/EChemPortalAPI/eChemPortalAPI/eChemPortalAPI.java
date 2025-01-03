package gov.epa.exp_data_gathering.parse.EChemPortalAPI.eChemPortalAPI;

import java.io.File;
import java.io.FileWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Processing.FinalRecords;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Processing.RecordDeduplicator;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query.APIConstants;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query.Query;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query.QueryHandler;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query.QueryOptions;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query.ToxQueryOptions;

/**
 * Runs download and write methods for single and batch queries on the eChemPortal API
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class eChemPortalAPI {
	
	static int maxSize=2000;
	
	
	/**
	 * Runs a single physicochemical endpoint query for AA Dashboard and stores the raw data as JSONs in a database
	 * @param endpointKind	The endpoint to download
	 * @param databasePath	Database to download results to
	 */
	public static void downloadEndpointDashboardPhyschemResults(String endpointKind,String databasePath) {
		QueryOptions options = new QueryOptions(endpointKind);
		options.runDownload(databasePath,true, maxSize);
	}
	
	/**
	 * Runs all physicochemical endpoint queries for AA Dashboard and stores the raw data as JSONs in a single database
	 * @param databasePath	Database to download results to
	 */
	public static void downloadAllDashboardPhyschemResults(String databasePath) {
		int counter = 0;
		for (String endpoint:APIConstants.physchemEndpoints) {
			QueryOptions options = new QueryOptions(endpoint);
			if (counter==0) {
				options.runDownload(databasePath,true, maxSize);
			} else {
				options.runDownload(databasePath,false, maxSize);
			}
			counter++;
		}
	}
	
	/**
	 * Runs a single toxicity endpoint query for AA Dashboard and stores the raw data as JSONs in a database
	 * @param endpointKind	The endpoint to download
	 * @param databasePath	Database to download results to
	 */
	public static void downloadEndpointDashboardToxResults(String endpointKind,String databasePath) {
		QueryOptions options = ToxQueryOptions.generateEndpointToxQueryOptionsForDashboard(endpointKind);
		options.runDownload(databasePath,true, maxSize);
	}

	/**
	 * Runs all toxicity endpoint queries for AA Dashboard and stores the raw data as JSONs in a single database
	 * @param databasePath	Database to download results to
	 */
	public static void downloadAllDashboardToxResults(String databasePath) {
		int counter = 0;
		for (String endpoint:APIConstants.dashboardToxEndpoints) {
			ToxQueryOptions options = ToxQueryOptions.generateEndpointToxQueryOptionsForDashboard(endpoint);
			if (counter==0) {
				options.runDownload(databasePath,true, maxSize);
			} else {
				options.runDownload(databasePath,false, maxSize);
			}
			counter++;
		}
	}

	/**
	 * Runs a single AA Dashboard query, stores the results in a database, and writes parsed records to an Excel file
	 * @param endpointKind		The endpoint kind to query
	 */
	public static void autoDownloadAndWriteEndpointDashboardToxResults(String endpointKind) {
		String databaseName = "eChemPortalAPI_" + endpointKind + "_RawData.db";
		String databasePath = APIConstants.dashboardFolder + File.separator + "Databases" + File.separator + databaseName;
		ToxQueryOptions options = ToxQueryOptions.generateEndpointToxQueryOptionsForDashboard(endpointKind);
		options.runDownload(databasePath, true, maxSize);
		
		File excelFolder=new File(APIConstants.dashboardFolder + File.separator + "Excel");
		excelFolder.mkdirs();
		String excelFileName = "eChemPortalAPI_" + endpointKind + "_FinalRecords.xlsx";
		String excelFilePath = excelFolder.getAbsolutePath()+File.separator + excelFileName;
		
		parseAndWriteEndpointResults(databasePath,excelFilePath);
	}
	
	/**
	 * Runs all AA Dashboard queries, stores the results in databases, and writes parsed records to an Excel file
	 */
	public static void autoDownloadAndWriteAllDashboardToxResults() {
		for (String endpoint:APIConstants.dashboardToxEndpoints) {
			autoDownloadAndWriteEndpointDashboardToxResults(endpoint);
		}
	}
	
	/**
	 * Runs the specified inhalation LC50 query requested by Todd Martin and stores the results in a database
	 * @param databaseName		Database to download results to
	 */
	public static void downloadInhalationLC50Results(String databaseName) {
		// Must operate on Query rather than QueryOptions in order to utilize block logic
		// All of this is handled by runDownload() for other queries
		Query query = ToxQueryOptions.generateInhalationLC50Queries();
		QueryHandler handler = new QueryHandler(1000,10);
		handler.downloadQueryResultsToDatabase(query,databaseName,true);
	}
	
	/**
	 * Runs a single ToxVal query, stores the results in a database, and writes parsed records to an Excel file
	 * @param endpointKind		The endpoint kind to query
	 */
	public static void downloadAndWriteEndpointToxValResults(String endpointKind,boolean download) {

//		String databaseName = "eChemPortalAPI_" + endpointKind + "_RawData.db";
//		String databasePath = APIConstants.toxValFolder + File.separator + "Databases" + File.separator + databaseName;

		String sourceName="eChemPortalAPI";
		String folderMain="data\\experimental\\"+sourceName+"\\";
		String databasePath=folderMain+sourceName+"_"+endpointKind+"_RawData.db";
		
		ToxQueryOptions options = ToxQueryOptions.generateEndpointToxQueryOptionsForToxVal(endpointKind);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
//		System.out.println(gson.toJson(options));
		
		if(download) options.runDownload(databasePath, true, maxSize);
		
		File excelFolder=new File(folderMain + "Excel");
		excelFolder.mkdirs();
		String excelFileName = "eChemPortalAPI_" + endpointKind + "_FinalRecords.xlsx";
		String excelFilePath = excelFolder.getAbsolutePath()+File.separator + excelFileName;
		
		FinalRecords records=parseAndWriteEndpointResults(databasePath,excelFilePath);
		records.toJsonFile(excelFilePath.replace(".xlsx", ".json"));

		
	}
	
	
	/**
	 * Runs all ToxVal queries, stores the results in databases, and writes parsed records to an Excel file
	 */
	public static void downloadAndWriteAllToxValResults() {
		for (String endpoint:APIConstants.toxValEndpoints) {
			downloadAndWriteEndpointToxValResults(endpoint,true);
		}
	}

	/**
	 * Parses downloaded raw data from a database into an Excel file of processed results
	 * @param databasePath		Path to the raw data database to be parsed
	 * @param excelFilePath		Path to save processed Excel file to
	 */
	public static FinalRecords parseAndWriteEndpointResults(String databasePath,String excelFilePath) {
		System.out.println("Parsing records in "+databasePath+"...");
		
		FinalRecords records = FinalRecords.getToxResultsInDatabase(databasePath);
		records = RecordDeduplicator.removeDuplicates(records);
		
		records.toExcelFile(excelFilePath);
		return records;
	}

	public static void main(String[] args) {
//		downloadAndWriteEndpointToxValResults(APIConstants.geneticToxicityVitro);
		
		downloadAndWriteEndpointToxValResults(APIConstants.acuteToxicityOral,false);
//		downloadAndWriteEndpointToxValResults(APIConstants.skinSensitisation);
		
//		downloadAndWriteEndpointToxValResults(APIConstants.geneticToxicityVivo);
//		downloadAndWriteEndpointToxValResults(APIConstants.shortTermToxicityToFish);
//		downloadAllDashboardToxResults("bob.db");
//		downloadInhalationLC50Results("inhalationLC50.db");
	}
}
