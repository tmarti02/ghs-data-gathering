package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DataDiff {
	private ExperimentalRecords apiRecords;
	private ExperimentalRecords excelRecords;
	
	public static final String mainFolder = "Data"+File.separator+"Experimental";

	public DataDiff() {
		apiRecords = new ExperimentalRecords();
		String apiRecordFileName = mainFolder+File.separator+"eChemPortalAPI"+File.separator+"eChemPortalAPI Experimental Records.json";
		String apiBadRecordFileName = mainFolder+File.separator+"eChemPortalAPI"+File.separator+"eChemPortalAPI Experimental Records-Bad.json";
		try {
			ExperimentalRecords records = ExperimentalRecords.loadFromJSON(apiRecordFileName);
			if (records!=null) { apiRecords.addAll(records); }
			ExperimentalRecords badRecords = ExperimentalRecords.loadFromJSON(apiBadRecordFileName);
			if (badRecords!=null) { apiRecords.addAll(badRecords); }
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		excelRecords = new ExperimentalRecords();
		String excelRecordFileName = mainFolder+File.separator+"eChemPortal"+File.separator+"eChemPortal Experimental Records.json";
		String excelBadRecordFileName = mainFolder+File.separator+"eChemPortal"+File.separator+"eChemPortal Experimental Records-Bad.json";
		try {
			ExperimentalRecords records = ExperimentalRecords.loadFromJSON(excelRecordFileName);
			if (records!=null) { excelRecords.addAll(records); }
			ExperimentalRecords badRecords = ExperimentalRecords.loadFromJSON(excelBadRecordFileName);
			if (badRecords!=null) { excelRecords.addAll(badRecords); }
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public ExperimentalRecords compare() {
		ExperimentalRecords comp = new ExperimentalRecords();
		List<ExperimentalRecord> apiRecordsNotInExcel = new ArrayList<ExperimentalRecord>(apiRecords);
		List<ExperimentalRecord> excelRecordsToRemove = new ArrayList<ExperimentalRecord>(excelRecords);
		apiRecordsNotInExcel.removeAll(excelRecordsToRemove);
		System.out.println("Found "+apiRecordsNotInExcel.size()+" records in API missing from Excel.");
		List<ExperimentalRecord> apiRecordsToRemove = new ArrayList<ExperimentalRecord>(apiRecords);
		List<ExperimentalRecord> excelRecordsNotInAPI = new ArrayList<ExperimentalRecord>(excelRecords);
		excelRecordsNotInAPI.removeAll(apiRecordsToRemove);
		System.out.println("Found "+excelRecordsNotInAPI.size()+" records in Excel missing from API.");
		comp.addAll(apiRecordsNotInExcel);
		comp.addAll(excelRecordsNotInAPI);
		return comp;
	}
	
	public static void main(String[] args) {
		DataDiff d = new DataDiff();
		ExperimentalRecords comp = d.compare();
		comp.toJSON_File(d.mainFolder+File.separator+"eChemPortal_eChemPortalAPI_CompRecords.json");
		comp.toExcel_File(d.mainFolder+File.separator+"eChemPortal_eChemPortalAPI_CompRecords.xlsx");
	}
}
