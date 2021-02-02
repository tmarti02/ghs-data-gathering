package gov.epa.exp_data_gathering.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;
import java.util.stream.Stream;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import gov.epa.api.ExperimentalConstants;
import gov.epa.eChemPortalAPI.eChemPortalAPI;
import gov.epa.eChemPortalAPI.Processing.FinalRecords;
import gov.epa.eChemPortalAPI.Processing.FinalRecord;

public class ToxParseEChemPortalAPI extends ParseEChemPortalAPI {
	boolean downloadNew;

	public ToxParseEChemPortalAPI(boolean downloadNew) {
		sourceName = ExperimentalConstants.strSourceEChemPortalAPI;
		this.init();
		this.downloadNew = downloadNew;
		fileNameSourceExcel=null;
		folderNameWebpages=null;
		folderNameExcel=null;
		fileNameJSON_Records = sourceName +" Toxicity Original Records.json";
		fileNameFlatExperimentalRecords = sourceName +" Toxicity Experimental Records.txt";
		fileNameFlatExperimentalRecordsBad = sourceName +" Toxicity Experimental Records-Bad.txt";
		fileNameJsonExperimentalRecords = sourceName +" Toxicity Experimental Records.json";
		fileNameJsonExperimentalRecordsBad = sourceName +" Toxicity Experimental Records-Bad.json";
		fileNameExcelExperimentalRecords = sourceName +" Toxicity Experimental Records.xlsx";
	}
	
	public void downloadInhalationLC50Results() {
		String databaseName = sourceName+"_raw_inhalationlc50_json.db";
		String databasePath = databaseFolder+File.separator+databaseName;
		eChemPortalAPI.downloadInhalationLC50Results(databasePath);
	}
	
	public void downloadAllDashboardToxResults() {
		String databaseName = sourceName+"_raw_tox_json.db";
		String databasePath = databaseFolder+File.separator+databaseName;
		eChemPortalAPI.downloadAllDashboardToxResults(databasePath);
	}
	
	/**
	 * Parses JSON entries in database to RecordPubChem objects, then saves them to a JSON file
	 */
	@Override
	protected void createRecords() {
		if (downloadNew) { downloadAllDashboardToxResults(); }
		FinalRecords records = FinalRecords.getToxResultsInDatabase(databaseFolder + File.separator + sourceName+"_raw_tox_json.db");
		writeOriginalRecordsToFile(new Vector<FinalRecord>(records));
	}
	
	/**
	 * Reads the JSON file created by createRecords() and translates it to an ExperimentalRecords object
	 */
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);

			FinalRecord[] finalRecordsArr = gson.fromJson(new FileReader(jsonFile), FinalRecord[].class);
			
			for (int i = 0; i < finalRecordsArr.length; i++) {
				FinalRecord r = finalRecordsArr[i];
				addExperimentalRecords(r,recordsExperimental);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	/**
	 * Creates and adds the ExperimentalRecord corresponding to a given RecordEChemPortalAPI object
	 * @param r			The RecordEChemPortalAPI object to translate
	 * @param records	The ExperimentalRecords to add created record to
	 */
	private void addExperimentalRecords(FinalRecord r, ExperimentalRecords records) {
		ExperimentalRecords ers = new ExperimentalRecords(r);
		
		for (ExperimentalRecord er:ers) {
			uc.convertRecord(er);
			
			if (!ParseUtilities.hasIdentifiers(er)) {
				er.keep = false;
				er.reason = "No identifiers";
			}
		}
		
		records.addAll(ers);
	}
	
	public static void main(String[] args) {
		ToxParseEChemPortalAPI p = new ToxParseEChemPortalAPI(false);
		p.createFiles();
	}
}
