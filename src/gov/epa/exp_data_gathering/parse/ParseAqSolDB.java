package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

public class ParseAqSolDB extends Parse {
	public ParseAqSolDB() {
		sourceName = ExperimentalConstants.strSourceAqSolDB;
		this.init();
	}
	
	@Override
	protected void createRecords() {
		Vector<RecordAqSolDB> records = RecordAqSolDB.parseAqSolDBRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordAqSolDB[] recordsAqSolDB = gson.fromJson(new FileReader(jsonFile), RecordAqSolDB[].class);
			
			for (int i = 0; i < recordsAqSolDB.length; i++) {
				RecordAqSolDB rec = recordsAqSolDB[i];
				addExperimentalRecords(rec,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return recordsExperimental;
	}
	
	private void addExperimentalRecords(RecordAqSolDB ar,ExperimentalRecords records) {
		if (ar.solubility!=null && !ar.solubility.isBlank()) {
			ExperimentalRecord er = new ExperimentalRecord();
			er.source_name = ExperimentalConstants.strSourceAqSolDB;
			er.chemical_name = ar.name;
			er.smiles = ar.smiles;
			er.property_name = ExperimentalConstants.strWaterSolubility;
			er.property_value_point_estimate_original = Math.pow(10.0,  Double.parseDouble(ar.solubility));
			er.property_value_units_original = ExperimentalConstants.str_M;
			er.finalizeUnits();
			er.keep = true;
			er.flag = false;
			records.add(er);
		}
	}
	
	public static void main(String[] args) {
		ParseAqSolDB p = new ParseAqSolDB();
		p.createFiles();
	}
}
