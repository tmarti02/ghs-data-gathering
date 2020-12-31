package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
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

public class ToxRecordEChemPortalAPI extends RecordEChemPortalAPI {
	public String doseDescriptor;
	public String testType;
	public String species;
	public String strain;
	public String routeOfAdministration;
	public String inhalationExposureType;
	
	private static final String sourceName = ExperimentalConstants.strSourceEChemPortalAPI;

	public static void downloadAllInhalationLC50ResultsToDatabase(boolean startFresh) {
		List<ToxQueryOptions> allOptions = ToxQueryOptions.generateSimpleInhalationLC50Options();
		allOptions.addAll(ToxQueryOptions.generateComplexInhalationLC50Options());
		String databaseName = sourceName+"_raw_inhalationlc50_json.db";
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
	public static List<ToxRecordEChemPortalAPI> parseToxResultsInDatabase() {
		ParseEChemPortalAPI p = new ParseEChemPortalAPI();
		String databaseName = sourceName+"_raw_inhalationlc50_json.db";
		String databasePath = p.databaseFolder+File.separator+databaseName;
		List<ToxRecordEChemPortalAPI> records = new ArrayList<ToxRecordEChemPortalAPI>();
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		
		try {
			int count = 0;
			int countEliminated = 0;
			// Uses a HashSet to speed up duplicate checking by URL
			HashSet<String> urlCheck = new HashSet<String>();
			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = SQLite_GetRecords.getAllRecords(stat,"results");
			while (rs.next()) {
				String date = rs.getString("date");
				String content = rs.getString("content");
				ResultsPage page = gson.fromJson(content,ResultsPage.class);
				List<Result> results = page.results;
				for (Result r:results) {
					ToxRecordEChemPortalAPI rec = new ToxRecordEChemPortalAPI();
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
					List<Block> blocks = r.blocks;
					for (Block b:blocks) {
						List<NestedBlock> nestedBlocks = b.nestedBlocks;
						for (NestedBlock nb:nestedBlocks) {
							List<OriginalValue> originalValues = nb.originalValues;
							for (OriginalValue value:originalValues) {
								switch (value.name) {
								case "InfoType":
									rec.infoType = value.value;
									break;
								case "Reliability":
									rec.reliability = value.value;
									break;
								case "Value":
									rec.value = value.value;
									break;
								case "Dose Descriptor":
									rec.doseDescriptor = value.value;
									break;
								case "Test Type":
									rec.testType = value.value;
									break;
								case "Species":
									rec.species = value.value;
									break;
								case "Strain":
									rec.strain = value.value;
									break;
								case "Route of Administration":
									rec.routeOfAdministration = value.value;
									break;
								case "Inhalation Exposure Type":
									rec.inhalationExposureType = value.value;
									break;
								}
							}
						}
					}
					if (urlCheck.add(rec.endpointURL)) {
						// If URL not seen before, adds the record immediately and moves on
						records.add(rec);
						count++;
					} else {
						// Otherwise, iterates and checks all records deeply for equivalence
						boolean haveRecord = false;
						ListIterator<ToxRecordEChemPortalAPI> it = records.listIterator(records.size());
						while (it.hasPrevious() && !haveRecord) {
							ToxRecordEChemPortalAPI existingRec = it.previous();
							if (rec.recordEquals(existingRec)) {
								haveRecord = true;
							}
						}
						if (!haveRecord) {
							// Adds new record if it is not a duplicate
							records.add(rec);
							count++;
						} else {
							// Counts the number of records eliminated
							countEliminated++;
						}
					}
					if (count % 1000==0) { System.out.println("Added "+count+" records..."); }
				}
			}
			System.out.println("Added "+count+" records; eliminated "+countEliminated+" records. Done!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return records;
	}
	
	private boolean recordEquals(ToxRecordEChemPortalAPI rec) {
		if (rec==null) {
			return false;
		} else if (this.recordEquals((RecordEChemPortalAPI) rec) &&
				Objects.equals(this.doseDescriptor,rec.doseDescriptor) &&
				Objects.equals(this.testType,rec.testType) &&
				Objects.equals(this.species,rec.species) &&
				Objects.equals(this.strain,rec.strain) &&
				Objects.equals(this.routeOfAdministration,rec.routeOfAdministration) &&
				Objects.equals(this.inhalationExposureType,rec.inhalationExposureType)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void main(String[] args) {
		downloadAllInhalationLC50ResultsToDatabase(true);
	}
	
}
