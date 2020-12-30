package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.io.File;

import gov.epa.exp_data_gathering.parse.ExperimentalRecords;

public class DataDiff {
	private ExperimentalRecords apiRecords;
	private ExperimentalRecords excelRecords;
	
	public static final String mainFolder = "Data"+File.separator+"Experimental";

	public DataDiff() {
		apiRecords = new ExperimentalRecords();
		String apiRecordFileName = mainFolder+File.separator+"eChemPortalAPI Experimental Records.json";
		String apiBadRecordFileName = mainFolder+File.separator+"eChemPortalAPI Experimental Records-Bad.json";
		try {
			ExperimentalRecords records = ExperimentalRecords.loadFromJSON(apiRecordFileName);
			ExperimentalRecords badRecords = ExperimentalRecords.loadFromJSON(apiBadRecordFileName);
			apiRecords.addAll(records);
			if(badRecords!=null) apiRecords.addAll(badRecords);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		excelRecords = new ExperimentalRecords();
		String excelRecordFileName = mainFolder+File.separator+"eChemPortal Experimental Records.json";
		String excelBadRecordFileName = mainFolder+File.separator+"eChemPortal Experimental Records-Bad.json";
		try {
			ExperimentalRecords records = ExperimentalRecords.loadFromJSON(excelRecordFileName);
			ExperimentalRecords badRecords = ExperimentalRecords.loadFromJSON(excelBadRecordFileName);
			apiRecords.addAll(records);
			if(badRecords!=null) apiRecords.addAll(badRecords);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
