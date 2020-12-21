package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.Block;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.NestedBlock;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.OriginalValue;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.Result;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.ResultsPage;
import gov.epa.ghs_data_gathering.Database.MySQL_DB;

/**
 * Stores data downloaded from eChemPortal API
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
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
	
	private static final String sourceName = ExperimentalConstants.strSourceEChemPortalAPI;
	
	public static void downloadAllResultsToDatabase(boolean startFresh) {
		Vector<QueryOptions> allOptions = new Vector<QueryOptions>();
		allOptions.add(new QueryOptions(ExperimentalConstants.strMeltingPoint));
		allOptions.add(new QueryOptions(ExperimentalConstants.strBoilingPoint));
		allOptions.add(new QueryOptions(ExperimentalConstants.strFlashPoint));
		allOptions.add(new QueryOptions(ExperimentalConstants.strDensity));
		allOptions.add(new QueryOptions(ExperimentalConstants.strVaporPressure));
		allOptions.add(new QueryOptions(ExperimentalConstants.strWaterSolubility));
		allOptions.add(new QueryOptions(ExperimentalConstants.strLogKow));
		allOptions.add(new QueryOptions(ExperimentalConstants.str_pKA));
		allOptions.add(new QueryOptions(ExperimentalConstants.strHenrysLawConstant));
		int counter = 0;
		for (QueryOptions options:allOptions) {
			if (counter==0) {
				options.runDownload(startFresh);
			} else {
				options.runDownload(false);
			}
			counter++;
		}
	}
	
	/**
	 * Parses raw JSON search results from a database into a vector of RecordEChemPortalAPI objects
	 * @return		The search results as RecordEChemPortalAPI objects
	 */
	public static Vector<RecordEChemPortalAPI> parseResultsInDatabase() {
		ParseEChemPortalAPI p = new ParseEChemPortalAPI();
		String databaseName = sourceName+"_raw_json.db";
		String databasePath = p.databaseFolder+File.separator+databaseName;
		Vector<RecordEChemPortalAPI> records = new Vector<RecordEChemPortalAPI>();
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		
		try {
			int countEliminated = 0;
			// Uses a HashSet to speed up duplicate checking by URL
			HashSet<String> urlCheck = new HashSet<String>();
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
						rec.name = r.name;
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
							}
						}
						if (urlCheck.add(rec.endpointURL)) {
							// If URL not seen before, adds the record immediately and moves on
							records.add(rec);
						} else {
							// If URL seen before, checks records deeply for equivalence, adding only non-duplicates
							boolean haveRecord = false;
							Iterator<RecordEChemPortalAPI> it = records.iterator();
							while (it.hasNext() && !haveRecord) {
								RecordEChemPortalAPI existingRec = it.next();
								if (Objects.equals(rec.endpointURL, existingRec.endpointURL) && Objects.equals(rec.name, existingRec.name) &&
										Objects.equals(rec.number, existingRec.number) && Objects.equals(rec.reliability, existingRec.reliability) &&
										Objects.equals(rec.value, existingRec.value) && Objects.equals(rec.pressure, existingRec.pressure) &&
										Objects.equals(rec.temperature, existingRec.temperature) && Objects.equals(rec.pH, existingRec.pH) &&
										Objects.equals(rec.endpointKind, existingRec.endpointKind) &&
										Objects.equals(rec.participantAcronym, existingRec.participantAcronym)) {
									haveRecord = true;
								}
							}
							if (!haveRecord) {
								records.add(rec);
							} else {
								// Counts the number of records eliminated
								countEliminated++;
							}
						}
					}
				}
			}
			System.out.println("Eliminated "+countEliminated+" duplicate records.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return records;
	}
	
	public static void main(String[] args) {
		QueryOptions options = new QueryOptions(ExperimentalConstants.strMeltingPoint);
		options.runDownload(true);
	}
}
