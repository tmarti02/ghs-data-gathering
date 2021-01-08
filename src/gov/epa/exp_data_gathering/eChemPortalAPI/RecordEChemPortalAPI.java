package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.Block;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.NestedBlock;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.OriginalValue;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.Result;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.ResultsPage;

/**
 * Stores data downloaded from eChemPortal API
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class RecordEChemPortalAPI {
	public String baseURL;
	public String chapter;
	public String endpointKey;
	public String endpointKind;
	public String endpointURL;
	public boolean memberOfCategory;
	public String infoType;
	public String reliability;
	public String value;
	public String pressure;
	public String temperature;
	public String pH;
	public String participantID;
	public String participantAcronym;
	public String participantURL;
	public String substanceID;
	public String name;
	public String nameType;
	public String number;
	public String substanceURL;
	public String numberType;
	public String dateAccessed;
	
	private static final String sourceName = ExperimentalConstants.strSourceEChemPortalAPI;
	
	public static void downloadAllPhyschemResultsToDatabase(boolean startFresh) {
		List<QueryOptions> allOptions = QueryOptions.generateAllQueryOptions();
		String databaseName = sourceName+"_raw_json.db";
		int counter = 0;
		for (QueryOptions options:allOptions) {
			if (counter==0) {
				options.runDownload(databaseName,startFresh);
			} else {
				options.runDownload(databaseName,false);
			}
			counter++;
		}
	}
	
	/**
	 * Parses raw JSON search results from a database into a vector of RecordEChemPortalAPI objects
	 * @return		The search results as RecordEChemPortalAPI objects
	 */
	public static List<RecordEChemPortalAPI> parseResultsInDatabase() {
		ParseEChemPortalAPI p = new ParseEChemPortalAPI();
		String databaseName = sourceName+"_raw_json.db";
		String databasePath = p.databaseFolder+File.separator+databaseName;
		List<RecordEChemPortalAPI> records = new ArrayList<RecordEChemPortalAPI>();
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		
		try {
			int count = 0;
			int countEliminated = 0;
			// Uses a HashSet to speed up duplicate checking by URL
//			HashSet<String> urlCheck = new HashSet<String>();
			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = SQLite_GetRecords.getAllRecords(stat,"results");
			while (rs.next()) {
				String date = rs.getString("date");
				String content = rs.getString("content");
				ResultsPage page = gson.fromJson(content,ResultsPage.class);
				List<Result> results = page.results;
				for (Result r:results) {
					List<Block> blocks = r.blocks;
					List<OriginalValue> infoTypeAndReliability = blocks.get(0).nestedBlocks.get(0).originalValues;
					List<NestedBlock> experimentalData = blocks.get(1).nestedBlocks;
					for (NestedBlock data:experimentalData) {
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
						rec.name = StringEscapeUtils.escapeHtml4(r.name);
						rec.nameType = r.nameType;
						rec.number = r.number;
						rec.substanceURL = r.substanceUrl;
						rec.numberType = r.numberType;
						rec.dateAccessed = date.substring(0,date.indexOf(" "));
						rec.infoType = infoTypeAndReliability.get(0).value;
						rec.reliability = infoTypeAndReliability.get(1).value;
						List<OriginalValue> originalValues = data.originalValues;
						for (OriginalValue value:originalValues) {
							switch (value.name) {
							case "Value":
								rec.value = value.value;
								break;
							case "Pressure":
								rec.pressure = value.value;
								break;
							case "Temperature":
								rec.temperature = value.value;
								break;
							case "pH":
								rec.pH = value.value;
								break;
							case "isLog":
								rec.value = rec.value + " (" + value.value +")";
								break;
							}
						}
						records.add(rec);
						count++;
						// Now handled by general deduplication code
//						if (urlCheck.add(rec.endpointURL)) {
//							// If URL not seen before, adds the record immediately and moves on
//							records.add(rec);
//							count++;
//						} else {
//							// Otherwise, iterates and checks all records deeply for equivalence
//							boolean haveRecord = false;
//							ListIterator<RecordEChemPortalAPI> it = records.listIterator(records.size());
//							while (it.hasPrevious() && !haveRecord) {
//								RecordEChemPortalAPI existingRec = it.previous();
//								if (rec.recordEquals(existingRec)) {
//									haveRecord = true;
//								}
//							}
//							if (!haveRecord) {
//								// Adds new record if it is not a duplicate
//								records.add(rec);
//								count++;
//							} else {
//								// Counts the number of records eliminated
//								countEliminated++;
//							}
//						}
						// if (count % 10000==0) { System.out.println("Added "+count+" records..."); }
					}
				}
			}
//			System.out.println("Added "+count+" records; eliminated "+countEliminated+" records. Done!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return records;
	}
	
	protected boolean recordEquals(RecordEChemPortalAPI rec) {
		if (rec==null) {
			return false;
		} else if (Objects.equals(this.baseURL, rec.baseURL) &&
				Objects.equals(this.chapter, rec.chapter) &&
				Objects.equals(this.endpointKey, rec.endpointKey) &&
				Objects.equals(this.endpointKind, rec.endpointKind) &&
				Objects.equals(this.endpointURL, rec.endpointURL) &&
				Objects.equals(this.memberOfCategory, rec.memberOfCategory) &&
				Objects.equals(this.infoType, rec.infoType) &&
				Objects.equals(this.reliability, rec.reliability) &&
				Objects.equals(this.value, rec.value) &&
				Objects.equals(this.pressure, rec.pressure) &&
				Objects.equals(this.temperature, rec.temperature) &&
				Objects.equals(this.pH, rec.pH) &&
				Objects.equals(this.participantID, rec.participantID) &&
				Objects.equals(this.substanceID, rec.substanceID) &&
				Objects.equals(this.name, rec.name) &&
				Objects.equals(this.number, rec.number)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void main(String[] args) {
		downloadAllPhyschemResultsToDatabase(true);
	}
}
