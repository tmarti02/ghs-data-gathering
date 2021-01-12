package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.RecordFinalizer;

public class ToxParseEChemPortalAPI extends ParseEChemPortalAPI {

	public ToxParseEChemPortalAPI() {
		sourceName = ExperimentalConstants.strSourceEChemPortalAPI;
		this.init();
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
	
	/**
	 * Parses JSON entries in database to RecordPubChem objects, then saves them to a JSON file
	 */
	@Override
	protected void createRecords() {
		List<ToxRecordEChemPortalAPI> records = ToxRecordEChemPortalAPI.parseToxResultsInDatabase(sourceName+"_raw_inhalationlc50_json.db");
		writeOriginalRecordsToFile(new Vector<ToxRecordEChemPortalAPI>(records));
	}
	
	/**
	 * Reads the JSON file created by createRecords() and translates it to an ExperimentalRecords object
	 */
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			ToxRecordEChemPortalAPI[] toxRecordsEChemPortalAPI = gson.fromJson(new FileReader(jsonFile), ToxRecordEChemPortalAPI[].class);
			
			for (int i = 0; i < toxRecordsEChemPortalAPI.length; i++) {
				ToxRecordEChemPortalAPI r = toxRecordsEChemPortalAPI[i];
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
	private void addExperimentalRecords(ToxRecordEChemPortalAPI r, ExperimentalRecords records) {
		ExperimentalRecord er = new ExperimentalRecord();
		er.source_name = sourceName;
		er.url = r.endpointURL;
		er.original_source_name = r.participantAcronym;
		er.date_accessed = r.dateAccessed;
		er.reliability = r.reliability;
		
		if (!r.name.equals("-") && !r.name.contains("unnamed")) {
			er.chemical_name = r.name;
		}
		
		if (r.numberType!=null) {
			switch (r.numberType) {
			case "CAS Number":
				er.casrn = r.number;
				break;
			case "EC Number":
				er.einecs = r.number;
				break;
			}
		}
		
		ParseUtilities.getToxicity(er,r);
		er.property_value_string = "Value: "+r.value;
		
		if (r.testType!=null && !r.testType.isBlank()) {
			String testType = "Test Type: "+r.testType;
			er.property_value_string += "; "+testType;
		}
		
		if (r.strain!=null && !r.strain.isBlank()) {
			String strain = "Strain: "+r.strain;
			er.property_value_string += "; "+strain;
		}
		
		if (r.routeOfAdministration!=null && !r.routeOfAdministration.isBlank()) {
			String route = "Route of Administration: "+r.routeOfAdministration;
			er.property_value_string += "; "+route;
		}
		
		if (r.inhalationExposureType!=null && !r.inhalationExposureType.isBlank()) {
			String inhalationExposureType = "Inhalation Exposure Type: "+r.inhalationExposureType;
			er.property_value_string += "; "+inhalationExposureType;
		}
		
		RecordFinalizer.finalizeRecord(er);
		
		if (!ParseUtilities.hasIdentifiers(er)) {
			er.keep = false;
			er.reason = "No identifiers";
		}
		
		records.add(er);
	}
	
	public static void main(String[] args) {
		ToxParseEChemPortalAPI p = new ToxParseEChemPortalAPI();
		p.createFiles();
	}
}
