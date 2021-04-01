package gov.epa.exp_data_gathering.parse.DRD;

import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

public class RecordDRD {
	String studyNr;
	String testChemicalName;
	String casrn;
	String organicFunctionalGroups;
	String physicalFormAsTested;
	String physicalFormConfirmation;
	String dataSource;
	String commercialSource;
	String availablePurity;
	String nrOfStudies;
	String ghsClassification;
	String severityCutOff;
	String severityNrOfAnimals;
	String persistenceCutOff;
	String persistenceNrOfAnimals;
	String specificObservations;
	String specificObsNrOfAnimals;
	String comments;
	String shouldNotBeUsed;
	public static final String[] fieldNames = { "studyNr","testChemicalName","casrn","organicFunctionalGroups","physicalFormAsTested",
			"physicalFormConfirmation","dataSource","commercialSource","availablePurity","nrOfStudies","ghsClassification","severityCutOff",
			"severityNrOfAnimals","persistenceCutOff","persistenceNrOfAnimals","specificObservations","specificObsNrOfAnimals","comments","shouldNotBeUsed" };
	
	public static final String lastUpdated = "03/26/2021";
	public static final String sourceName = ExperimentalConstants.strSourceDRD;
	
	private static final String fileName = "Barroso2017_SI_Cleaned.xlsx";
	
	public static Vector<JsonObject> parseDRDRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(1);
		return records;
	}

}
