package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		er.property_value_string = cbr.standardRelation + cbr.standardValue + (cbr.standardUnits.equals("No Data") ? "" : (" "+cbr.standardUnits));
		if (cbr.standardType.equals("Tm")) {
			er.property_name = ExperimentalConstants.strMeltingPoint;
			if (cbr.standardRelation!=null && !cbr.standardRelation.isBlank() && !cbr.standardRelation.equals("=")) {
				er.property_value_numeric_qualifier = cbr.standardRelation;
			}
			if (cbr.standardValue!=null && !cbr.standardValue.isBlank()) {
				er.property_value_point_estimate_original = Double.parseDouble(cbr.standardValue);
				if (cbr.standardUnits.equals("degrees C")) { er.property_value_units_original = ExperimentalConstants.str_C; }
			}
		} else if (cbr.standardType.equals("pKa")) {
			er.property_name = ExperimentalConstants.str_pKA;
			if (cbr.standardRelation!=null && !cbr.standardRelation.isBlank() && !cbr.standardRelation.equals("=")) {
				er.property_value_numeric_qualifier = cbr.standardRelation;
			}
			if (cbr.standardValue!=null && !cbr.standardValue.isBlank()) {
				er.property_value_point_estimate_original = Double.parseDouble(cbr.standardValue);
			}
		} else if (cbr.standardType.equals("Solubility")) {
			er.property_name = ExperimentalConstants.strWaterSolubility;
			if (cbr.standardRelation!=null && !cbr.standardRelation.isBlank() && !cbr.standardRelation.equals("=")) {
				er.property_value_numeric_qualifier = cbr.standardRelation;
			}
			if (cbr.standardValue!=null && !cbr.standardValue.isBlank()) {
				er.property_value_point_estimate_original = Double.parseDouble(cbr.standardValue);
				if (cbr.standardUnits.equals("ug.mL-1")) { er.property_value_units_original = ExperimentalConstants.str_ug_mL; }
			}
			Matcher pHMatcher = Pattern.compile("pH ([0-9.]+)").matcher(cbr.assayDescription);
			if (pHMatcher.find()) {
				er.pH = pHMatcher.group(1);
			}
		} else if (cbr.standardType.equals("LogP")) {
			er.property_name = ExperimentalConstants.strLogKow;
			if (cbr.standardRelation!=null && !cbr.standardRelation.isBlank() && !cbr.standardRelation.equals("=")) {
				er.property_value_numeric_qualifier = cbr.standardRelation;
			}
			if (cbr.standardValue!=null && !cbr.standardValue.isBlank()) {
				er.property_value_point_estimate_original = Double.parseDouble(cbr.standardValue);
			}
		}
		er.original_source_name = cbr.documentJournal + " " + cbr.documentYear;
		if ((er.chemical_name==null || er.chemical_name.isBlank()) && (er.smiles==null || er.smiles.isBlank())) {
			er.keep = false;
			er.reason = "No identifiers";
		} else if (er.property_value_point_estimate_original==null) {
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
