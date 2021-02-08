package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.api.ExperimentalConstants;

public class ParseOFMPub extends Parse {

	public ParseOFMPub() {
		sourceName = ExperimentalConstants.strSourceOFMPub;
		this.init();
	}

	@Override
	protected void createRecords() {
		Vector<RecordOFMPub> records = RecordOFMPub.parseWebpagesInDatabase();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordOFMPub[] recordsOFMPub = gson.fromJson(new FileReader(jsonFile), RecordOFMPub[].class);
			
			for (int i = 0; i < recordsOFMPub.length; i++) {
				RecordOFMPub r = recordsOFMPub[i];
				if (r.value!=null && !r.value.isBlank()) {
					addExperimentalRecord(r,recordsExperimental);
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	private void addExperimentalRecord(RecordOFMPub opr,ExperimentalRecords records) {
		ExperimentalRecord er = new ExperimentalRecord();
		er.source_name = ExperimentalConstants.strSourceOFMPub;
		er.date_accessed = opr.date_accessed;
		er.url = opr.url;
		er.reliability = opr.reliability;

		if (opr.testSubstanceName!=null && !opr.testSubstanceName.isBlank() && opr.testSubstanceCAS!=null && !opr.testSubstanceCAS.isBlank()) {
			er.casrn = opr.testSubstanceCAS;
			er.chemical_name = opr.testSubstanceName;
		} else if (opr.categoryChemicalName!=null && !opr.categoryChemicalName.isBlank() && opr.categoryChemicalCAS!=null && !opr.categoryChemicalCAS.isBlank()) {
			er.casrn = opr.categoryChemicalCAS + " (category chemical)";
			er.chemical_name = opr.categoryChemicalName + " (category chemical)";
		} else if (opr.testSubstanceComments.contains("CAS No.")) {
			Matcher matchCASandName = Pattern.compile("CAS No\\.[ ]?([0-9-]+),[ ]?([-0-9a-zA-Z, ]+)( or)?").matcher(opr.testSubstanceComments);
			if (matchCASandName.find()) {
				er.casrn = matchCASandName.group(1);
				er.chemical_name = matchCASandName.group(2);
			} else {
				er.keep = false;
				er.reason = "No identifiers";
			}
		} else {
			er.keep = false;
			er.reason = "No identifiers";
		}
		
		switch (opr.endpoint) {
		case "Melting Point":
			er.property_name = ExperimentalConstants.strMeltingPoint;
			break;
		case "Boiling Point":
			er.property_name = ExperimentalConstants.strBoilingPoint;
			break;
		case "Vapor Pressure":
			er.property_name = ExperimentalConstants.strVaporPressure;
			break;
		case "Water Solubility":
			er.property_name = ExperimentalConstants.strWaterSolubility;
			break;
		case "Partition Coefficient":
			er.property_name = ExperimentalConstants.strLogKow;
			break;
		}
		
		er.property_value_string = "Value: "+opr.value;
		if (opr.resultRemarks!=null && !opr.resultRemarks.isBlank()) { er.property_value_string = er.property_value_string + "; Remarks: " + opr.resultRemarks; }
		
		boolean foundNumeric = false;
		String propertyName = er.property_name;
		String propertyValue = opr.value;
		String remarks = opr.resultRemarks.toLowerCase();
		if (propertyName==ExperimentalConstants.strMeltingPoint || propertyName==ExperimentalConstants.strBoilingPoint) {
			propertyValue = propertyValue.replaceAll("Other @", "K @");
			foundNumeric = ParseUtilities.getTemperatureProperty(er,propertyValue);
			ParseUtilities.getPressureCondition(er,propertyValue,sourceName);
			if ((er.pressure_mmHg==null || er.pressure_mmHg.isBlank()) && remarks.contains("pressure:")) {
				ParseUtilities.getPressureCondition(er,opr.resultRemarks,sourceName);
			}
			if (opr.indicator.contains("Decomposes") || (remarks.contains("decomposition: yes")
					&& !(remarks.contains("no [x") || remarks.contains("no [ x")))) {
				er.updateNote(ExperimentalConstants.str_dec);
			}
			if (opr.indicator.contains("Sublimes") || (remarks.contains("sublimation: yes")
					&& !remarks.contains("no [x"))) {
				er.updateNote(ExperimentalConstants.str_subl);
			}
		} else if (propertyName==ExperimentalConstants.strWaterSolubility) {
			foundNumeric = ParseWaterSolubility.getWaterSolubility(er, propertyValue,sourceName);
			if (!foundNumeric && opr.resultRemarks!=null && !opr.resultRemarks.isBlank()) {
				foundNumeric = ParseWaterSolubility.getWaterSolubility(er, opr.resultRemarks.replaceAll(" per ","/"),sourceName);
			}
			ParseUtilities.getTemperatureCondition(er,propertyValue);
			if (er.temperature_C==null && remarks.contains("temperature:")) {
				ParseUtilities.getTemperatureCondition(er,opr.resultRemarks);
			}
			er.property_value_qualitative = opr.indicator.toLowerCase();
		} else if (propertyName==ExperimentalConstants.strVaporPressure) {
			foundNumeric = ParseUtilities.getVaporPressure(er,propertyValue);
			if (!foundNumeric && opr.resultRemarks!=null && !opr.resultRemarks.isBlank()) {
				foundNumeric = ParseUtilities.getVaporPressure(er, opr.resultRemarks);
			}
			ParseUtilities.getTemperatureCondition(er,propertyValue);
			if (er.temperature_C==null && remarks.contains("temperature:")) {
				ParseUtilities.getTemperatureCondition(er,opr.resultRemarks);
			}
		} else if (propertyName==ExperimentalConstants.strLogKow) {
			foundNumeric = ParseUtilities.getLogProperty(er,propertyValue);
			ParseUtilities.getTemperatureCondition(er,propertyValue);
			if (er.temperature_C==null && remarks.contains("temperature:")) {
				ParseUtilities.getTemperatureCondition(er,opr.resultRemarks);
			}
		}
		
		if ((opr.categoryChemicalResultType!=null && (opr.categoryChemicalResultType.contains("Estimated") || opr.categoryChemicalResultType.contains("Read-Across") ||
				opr.categoryChemicalResultType.contains("Derived"))) || (opr.testSubstanceResultType!=null && opr.testSubstanceResultType.contains("Estimated")) ||
				(opr.testSubstanceComments!=null && opr.testSubstanceComments.contains("Read-Across"))) {
			er.keep = false;
			er.reason = "Estimated";
		}
		
		if (!foundNumeric && (er.property_value_units_original==null || er.property_value_units_original.isBlank()) && 
				!((er.property_value_qualitative!=null && !er.property_value_qualitative.isBlank()) || (er.note!=null && !er.note.isBlank()))) {
			er.keep = false;
			er.reason = "Bad data or units";
		}
		
		if (remarks.contains("calculated") && !remarks.contains("measured") && !remarks.contains("good correlation") && !remarks.contains("limit") &&
				!remarks.contains("hplc")) {
			er.keep = false;
			er.reason = "Remarks field suggests calculated value";
		}
		
		if (remarks.contains("estimated") && !remarks.contains("quality: estimated") && !remarks.contains("estimated to be reliable") &&
				!remarks.contains("consistent")) {
			Matcher expDataMatcher = Pattern.compile("experimental database match = ([-]?[0-9.]+)").matcher(remarks);
			if (expDataMatcher.find()) {
				er.property_value_point_estimate_original = Double.parseDouble(expDataMatcher.group(1));
				er.property_value_units_original = ExperimentalConstants.str_C;
				er.keep = true;
				er.flag = true;
				er.reason = "Experimental data extracted from model output in remarks field";
			} else {
				er.keep = false;
				er.reason = "Remarks field suggests estimated value";
			}
		}
		
		if (remarks.contains("extrapolated") && !remarks.contains("eluted")) {
			er.keep = false;
			er.reason = "Remarks field suggests extrapolated value";
		}
		
		if (remarks.contains("following are the results from the model")) {
			if (remarks.contains("experimental database") && er.property_name.equals(ExperimentalConstants.strBoilingPoint)) {
				Matcher bpMatcher = Pattern.compile("exp bp \\(deg c\\): ([0-9.]+)").matcher(remarks);
				if (bpMatcher.find()) {
					er.property_value_point_estimate_original = Double.parseDouble(bpMatcher.group(1));
					er.property_value_units_original = ExperimentalConstants.str_C;
					er.keep = true;
					er.flag = true;
					er.reason = "Experimental data extracted from model output in remarks field";
				} else {
					er.keep = false;
					er.reason = "Model output without experimental database match";
				}
			} else if (remarks.contains("experimental database") && er.property_name.equals(ExperimentalConstants.strMeltingPoint)) {
				Matcher mpMatcher = Pattern.compile("exp mp \\(deg c\\): ([0-9.]+)").matcher(remarks);
				if (mpMatcher.find()) {
					er.property_value_point_estimate_original = Double.parseDouble(mpMatcher.group(1));
					er.property_value_units_original = ExperimentalConstants.str_C;
					er.keep = true;
					er.flag = true;
					er.reason = "Experimental data extracted from model output in remarks field";
				} else {
					er.keep = false;
					er.reason = "Model output without experimental database match";
				}
			} else {
				er.keep = false;
				er.reason = "Model output without experimental database match";
			}
		}
		
		uc.convertRecord(er);
		records.add(er);
	}
	
	public static void main(String[] args) {
		ParseOFMPub p = new ParseOFMPub();
		p.createFiles();
	}
}
