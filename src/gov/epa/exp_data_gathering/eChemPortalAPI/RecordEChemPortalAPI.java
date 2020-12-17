package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.Block;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.NestedBlock;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.OriginalValue;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.Result;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.ResultsPage;
import gov.epa.ghs_data_gathering.Database.CreateGHS_Database;
import gov.epa.ghs_data_gathering.Database.MySQL_DB;

public class RecordEChemPortalAPI {
	String baseURL;
	String chapter;
	String endpointKey;
	String endpointKind;
	String endpointURL;
	boolean memberOfCategory;
	String infoType;
	String reliability;
	String value;
	String pressure;
	String temperature;
	String pH;
	String participantID;
	String participantAcronym;
	String participantURL;
	String substanceID;
	String name;
	String nameType;
	String number;
	String substanceURL;
	String numberType;
	String dateAccessed;
	
	private static final String sourceName = "eChemPortal API";
	private static final String databaseFolder = "Data\\Experimental\\"+sourceName;
	
	private static void downloadResultsToDatabase(Query query,boolean startFresh) {
		String databasePath = databaseFolder+"\\eChemPortalAPI_raw_json.db";
		String tableName = "results";
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		java.sql.Connection conn = CreateGHS_Database.createDatabaseTable(databasePath, tableName, RawDataEChemPortalAPI.fieldNames, startFresh);
		QueryHandler qh = new QueryHandler();
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
		
		try {
			Date date = new Date();  
			String strDate=formatter.format(date);
			
			String strQuery = qh.prettyGson.toJson(query);
			
			ResultsPage page = qh.getResultsPage(query);
			String strPage = qh.prettyGson.toJson(page);
			
			RawDataEChemPortalAPI data = new RawDataEChemPortalAPI(strDate,strQuery,strPage);
			data.addRecordToDatabase(conn);
			
			int totalResults = page.pageInfo.totalElements;
			System.out.println("Found "+totalResults+" results. Downloading to eChemPortalAPI_raw_json.db...");
			
			int offset = 100;
			while (offset < totalResults) {
				date = new Date();  
				strDate=formatter.format(date);
				
				query.updateOffset(offset);
				strQuery = qh.prettyGson.toJson(query);
				
				data = new RawDataEChemPortalAPI(strDate,strQuery,"");
				page = qh.getResultsPage(query);
				strPage = qh.prettyGson.toJson(page);
				data.content = strPage;
				data.addRecordToDatabase(conn);
				
				offset += 100;
			}
			System.out.println("Done!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static Vector<RecordEChemPortalAPI> parseResultsInDatabase() {
		String databasePath = databaseFolder+"\\eChemPortalAPI_raw_json.db";
		Vector<RecordEChemPortalAPI> records = new Vector<RecordEChemPortalAPI>();
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		
		try {
			Statement stat = MySQL_DB.getStatement(databasePath);
			ResultSet rs = MySQL_DB.getAllRecords(stat,"results");
			while (rs.next()) {
				String date = rs.getString("date");
				String content = rs.getString("content");
				ResultsPage page = gson.fromJson(content,ResultsPage.class);
				List<Result> results = page.results;
				for (Result r:results) {
					List<Block> blocks = r.blocks;
					List<OriginalValue> infoTypeAndReliability = blocks.get(0).nestedBlocks.get(0).originalValues;
					List<NestedBlock> experimentalData = blocks.get(1).nestedBlocks;
					for (NestedBlock nb:experimentalData) {
						RecordEChemPortalAPI rec = new RecordEChemPortalAPI();
						rec.baseURL = r.baseUrl;
						rec.chapter = r.chapter;
						rec.endpointKey = r.endpointKey;
						rec.endpointKind = r.endpointKind;
						rec.endpointURL = r.endpointUrl;
						rec.memberOfCategory = r.memberOfCategory;
						rec.participantID = r.participantId;
						rec.participantAcronym = r.participantAcronym;
						rec.participantURL = r.participantUrl;
						rec.substanceID = r.substanceId;
						rec.name = r.name;
						rec.nameType = r.nameType;
						rec.number = r.number;
						rec.substanceURL = r.substanceUrl;
						rec.numberType = r.numberType;
						rec.dateAccessed = date.substring(0,date.indexOf(" "));
						rec.infoType = infoTypeAndReliability.get(0).value;
						rec.reliability = infoTypeAndReliability.get(1).value;
						List<OriginalValue> originalValues = nb.originalValues;
						for (OriginalValue v:originalValues) {
							switch (v.name) {
							case "Value":
								rec.value = v.value;
								break;
							case "Pressure":
								rec.pressure = v.value;
								break;
							case "Temperature":
								rec.temperature = v.value;
								break;
							case "pH":
								rec.pH = v.value;
								break;
							}
						}
						records.add(rec);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return records;
	}
	
	public static void main(String[] args) {
		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
		Query query = QueryHandler.generateQuery(ExperimentalConstants.strWaterSolubility,2,
				"0","1",ExperimentalConstants.str_g_L,
				null,null,null,
				null,"20",ExperimentalConstants.str_C,
				"0","7");
		downloadResultsToDatabase(query,true);
		Vector<RecordEChemPortalAPI> results = parseResultsInDatabase();
		try {

			System.out.println("Writing to eChemPortalAPI Original Records.json...");
			File file = new File(databaseFolder+"\\eChemPortalAPI Original Records.json");
			file.getParentFile().mkdirs();

			FileWriter fw = new FileWriter(file);
			fw.write(prettyGson.toJson(results));
			fw.flush();
			fw.close();
			
			System.out.println("Done!");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
