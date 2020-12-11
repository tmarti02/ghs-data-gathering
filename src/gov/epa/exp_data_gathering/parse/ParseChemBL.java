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
		Vector<RecordChemBL> records = RecordChemBL.parseJSONsInDatabase();
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
		er.keep = true;
		er.source_name = ExperimentalConstants.strSourceChemBL;
		er.chemical_name = cbr.moleculePrefName;
		er.smiles = cbr.canonicalSmiles;
		er.measurement_method = cbr.assayDescription;
		er.url = cbr.url;
		er.property_value_string = cbr.standardRelation + cbr.standardValue + (cbr.standardUnits==null ? "" : (" "+cbr.standardUnits));
		if (!cbr.assayType.equals("P")) {
			return;
		} else if (cbr.standardType.toLowerCase().equals("tm")) {
			er.property_name = ExperimentalConstants.strMeltingPoint;
			if (cbr.standardRelation!=null && !cbr.standardRelation.isBlank() && !cbr.standardRelation.equals("=")) {
				er.property_value_numeric_qualifier = cbr.standardRelation;
			}
			if (cbr.standardValue!=null && !cbr.standardValue.isBlank()) {
				er.property_value_point_estimate_original = Double.parseDouble(cbr.standardValue);
				if (cbr.standardUnits.equals("degrees C")) { er.property_value_units_original = ExperimentalConstants.str_C; }
			}
		} else if (cbr.standardType.toLowerCase().equals("pka")) {
			er.property_name = ExperimentalConstants.str_pKA;
			if (cbr.standardRelation!=null && !cbr.standardRelation.isBlank() && !cbr.standardRelation.equals("=")) {
				er.property_value_numeric_qualifier = cbr.standardRelation;
			}
			if (cbr.standardValue!=null && !cbr.standardValue.isBlank()) {
				er.property_value_point_estimate_original = Double.parseDouble(cbr.standardValue);
			}
			String desc = cbr.assayDescription.toLowerCase();
			if (desc.contains("calculat")) {
				er.keep = false;
				er.reason = "Calculated";
			} else if (desc.contains("estimat")) {
				er.keep = false;
				er.reason = "Estimated";
			} else if (desc.contains("extrapolat")) {
				er.keep = false;
				er.reason = "Extrapolated";
			}
			Matcher pHMatcher = Pattern.compile("pH (of )?([0-9.]+)( to )?([0-9.]+)?").matcher(cbr.assayDescription);
			if (pHMatcher.find()) {
				er.pH = pHMatcher.group(2)+(pHMatcher.group(4)==null ? "" : "-"+pHMatcher.group(4));
			}
		} else if (cbr.standardType.toLowerCase().equals("solubility")) {
			er.property_name = ExperimentalConstants.strWaterSolubility;
			if (cbr.standardRelation!=null && !cbr.standardRelation.isBlank() && !cbr.standardRelation.equals("=")) {
				er.property_value_numeric_qualifier = cbr.standardRelation;
			}
			if (cbr.standardValue!=null && !cbr.standardValue.isBlank()) {
				er.property_value_point_estimate_original = Double.parseDouble(cbr.standardValue);
				if (cbr.standardUnits.equals("ug.mL-1")) { er.property_value_units_original = ExperimentalConstants.str_ug_mL; }
			}
			Matcher pHMatcher = Pattern.compile("pH (of )?([0-9.]+)( to )?([0-9.]+)?").matcher(cbr.assayDescription);
			if (pHMatcher.find()) {
				er.pH = pHMatcher.group(2)+(pHMatcher.group(4)==null ? "" : "-"+pHMatcher.group(4));
			}
		} else if (cbr.standardType.toLowerCase().equals("logp")) {
			er.property_name = ExperimentalConstants.strLogKow;
			if (cbr.standardRelation!=null && !cbr.standardRelation.isBlank() && !cbr.standardRelation.equals("=")) {
				er.property_value_numeric_qualifier = cbr.standardRelation;
			}
			if (cbr.standardValue!=null && !cbr.standardValue.isBlank()) {
				er.property_value_point_estimate_original = Double.parseDouble(cbr.standardValue);
			}
		} else {
			return;
		}
		
		er.original_source_name = cbr.documentJournal + " " + cbr.documentYear;
		if (er.keep && (er.chemical_name==null || er.chemical_name.isBlank()) && (er.smiles==null || er.smiles.isBlank())) {
			er.keep = false;
			er.reason = "No identifiers";
		} else if (er.keep && er.property_value_point_estimate_original==null) {
			er.keep = false;
			er.reason = "Bad data or units";
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
