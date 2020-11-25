package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

public class ParseChemicalBook extends Parse {
	
	public ParseChemicalBook() {
		sourceName = "ChemicalBook";
		this.init();
	}
	
	@Override
	protected void createRecords() {
		Vector<RecordChemicalBook> records = RecordChemicalBook.parseWebpagesInDatabase();
		writeOriginalRecordsToFile(records);
	}

	/**
	 * Reads the JSON file created by createRecords() and translates it to an ExperimentalRecords object
	 */
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental = new ExperimentalRecords();
		
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordChemicalBook[] recordsChemicalBook = gson.fromJson(new FileReader(jsonFile), RecordChemicalBook[].class);
			System.out.println(recordsChemicalBook[2]);
			
			for (int i = 0; i < recordsChemicalBook.length; i++) {
				RecordChemicalBook r = recordsChemicalBook[i];
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	
	private void addExperimentalRecords(RecordChemicalBook cbr,ExperimentalRecords recordsExperimental) {
		if (cbr.density != null && !cbr.density.isBlank()) {
			addNewExperimentalRecord(cbr,ExperimentalConstants.strDensity,cbr.density,recordsExperimental);
	    }
        if (cbr.meltingPoint != null && !cbr.meltingPoint.isBlank()) {
			addNewExperimentalRecord(cbr,ExperimentalConstants.strMeltingPoint,cbr.meltingPoint,recordsExperimental);
        }
        if (cbr.boilingPoint != null && !cbr.boilingPoint.isBlank()) {
			addNewExperimentalRecord(cbr,ExperimentalConstants.strBoilingPoint,cbr.boilingPoint,recordsExperimental);
	    }
        if (cbr.solubility != null && !cbr.solubility.isBlank()) {
			addNewExperimentalRecord(cbr,ExperimentalConstants.strWaterSolubility,cbr.solubility,recordsExperimental);
        } 
         
	}


	private void addNewExperimentalRecord(RecordChemicalBook cbr, String propertyName, String propertyValue, ExperimentalRecords recordsExperimental) {
		// TODO Auto-generated method stub
		ExperimentalRecord er = new ExperimentalRecord();
		er.casrn=cbr.CAS;
		er.einecs=cbr.EINECS;
		er.chemical_name=cbr.chemicalName;
		if (cbr.synonyms != null) { er.synonyms=cbr.synonyms.replace(';','|'); }
		er.property_name=propertyName;
		er.property_value_string=propertyValue;
		er.source_name= "Chemical Book";
		er.url = "https://www.chemicalbook.com/" + cbr.fileName;
		
		recordsExperimental.add(er);
		
	}
	
	
	public static void main(String[] args) {
		ParseChemicalBook p = new ParseChemicalBook();
		p.mainFolder = p.mainFolder + File.separator + "General";
		p.databaseFolder = p.mainFolder;
		p.jsonFolder= p.mainFolder;
		p.createFiles();
	}
}

