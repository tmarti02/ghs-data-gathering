package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

public class ParseChemBL extends Parse {

	public ParseChemBL() {
		sourceName = ExperimentalConstants.strSourceChemBL;
		this.init();
	}
	
	@Override
	protected void createRecords() {
		Vector<RecordChemBL> records = RecordChemBL.parseChemBLQueriesFromCSV();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordChemBL[] recordsChemBL = gson.fromJson(new FileReader(jsonFile), RecordChemBL[].class);
			
			for (int i = 0; i < recordsChemBL.length; i++) {
				RecordChemBL rec = recordsChemBL[i];
				addExperimentalRecords(rec,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return recordsExperimental;
	}
	
	private void addExperimentalRecords(RecordChemBL cbr,ExperimentalRecords records) {
		ExperimentalRecord er = new ExperimentalRecord();
		er.source_name = ExperimentalConstants.strSourceChemBL;
		er.chemical_name = cbr.moleculeName;
		er.smiles = cbr.smiles;
		er.property_value_string = cbr.standardRelation + cbr.standardValue + " " + cbr.standardUnits;
		if (cbr.standardType.equals("Tm")) {
			er.property_name = ExperimentalConstants.strMeltingPoint;
			if (cbr.standardRelation!=null && !cbr.standardRelation.isBlank() && !cbr.standardRelation.equals("=")) {
				er.property_value_numeric_qualifier = cbr.standardRelation;
			}
			if (cbr.standardValue!=null && !cbr.standardValue.isBlank()) {
				er.property_value_point_estimate_original = Double.parseDouble(cbr.standardValue);
				er.property_value_units_original = ExperimentalConstants.str_C;
			}
		}
		er.original_source_name = cbr.documentJournal + " " + cbr.documentYear;
		if ((er.chemical_name==null || er.chemical_name.isBlank()) && (er.smiles==null || er.smiles.isBlank())) {
			er.keep = false;
			er.reason = "No identifiers";
		} else if (er.property_value_units_original==null || er.property_value_units_original.isBlank()) {
			er.keep = false;
			er.reason = "Bad data or units";
		} else {
			er.keep = true;
		}
		er.flag = false;
		er.finalizeUnits();
		records.add(er);
	}
	
	public static void main(String[] args) {
		ParseChemBL p = new ParseChemBL();
		p.createFiles();
	}
	
}
