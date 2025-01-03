package gov.epa.exp_data_gathering.parse.EChemPortal;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Processing.FinalRecord;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Processing.FinalRecords;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.eChemPortalAPI.eChemPortalAPI;

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
		String filePathDB=databaseFolder + File.separator + sourceName+"_raw_tox_json.db";
		System.out.println(filePathDB);
		
		FinalRecords records = FinalRecords.getToxResultsInDatabase(filePathDB);
		writeOriginalRecordsToFile(new Vector<FinalRecord>(records));
	}
	
	/**
	 * Reads the JSON file created by createRecords() and translates it to an ExperimentalRecords object
	 */
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<FinalRecord> finalRecords = new ArrayList<FinalRecord>();
			FinalRecord[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), FinalRecord[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					finalRecords.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), FinalRecord[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						finalRecords.add(tempRecords[i]);
					}
				}
			}
			
			Iterator<FinalRecord> it = finalRecords.iterator();
			while (it.hasNext()) {
				FinalRecord r = it.next();
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
