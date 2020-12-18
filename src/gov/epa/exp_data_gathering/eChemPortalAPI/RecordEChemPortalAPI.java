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
	
	public static void downloadResultsToDatabase() {
		// TODO
	}
	
	public static Vector<RecordEChemPortalAPI> parseResultsInDatabase() {
		ParseEChemPortalAPI p = new ParseEChemPortalAPI();
		String databaseName = p.sourceName+"_raw_json.db";
		String databasePath = p.databaseFolder+File.separator+databaseName;
		Vector<RecordEChemPortalAPI> records = new Vector<RecordEChemPortalAPI>();
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		
		try {
			int countEliminated = 0;
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
						if (urlCheck.add(rec.endpointURL)) {
							records.add(rec);
						} else {
							boolean haveRecord = false;
							Iterator<RecordEChemPortalAPI> it = records.iterator();
							while (it.hasNext() && !haveRecord) {
								RecordEChemPortalAPI existingRec = it.next();
								if (Objects.equals(rec.endpointURL, existingRec.endpointURL) && Objects.equals(rec.name, existingRec.name) &&
										Objects.equals(rec.number, existingRec.number) && Objects.equals(rec.reliability, existingRec.reliability) &&
										Objects.equals(rec.value, existingRec.value) && Objects.equals(rec.pressure, existingRec.pressure) &&
										Objects.equals(rec.temperature, existingRec.temperature) && Objects.equals(rec.pH, existingRec.pH) &&
										Objects.equals(rec.endpointKind, existingRec.endpointKind)) {
									haveRecord = true;
								}
							}
							if (!haveRecord) {
								records.add(rec);
							} else {
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
		// TODO
	}
}
