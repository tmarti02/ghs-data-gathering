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
		er.url = opr.url;
		er.reliability = opr.reliability;
		er.keep = true;
		er.flag = false;
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
			} else { er.keep = false; }
		} else { er.keep = false; }
		
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
		
		er.property_value_string = opr.value;
		if (opr.resultRemarks!=null && !opr.resultRemarks.isBlank()) { er.property_value_string = er.property_value_string + ";" + opr.resultRemarks; }
		
		boolean foundNumeric = false;
		String propertyName = er.property_name;
		String propertyValue = opr.value;
		String remarks = opr.resultRemarks.toLowerCase();
		if (propertyName==ExperimentalConstants.strMeltingPoint || propertyName==ExperimentalConstants.strBoilingPoint) {
			propertyValue = propertyValue.replaceAll("Other @", "K @");
			foundNumeric = getTemperatureProperty(er,propertyValue);
			getPressureCondition(er,propertyValue);
			if ((er.pressure_mmHg==null || er.pressure_mmHg.isBlank()) && remarks.contains("pressure:")) {
				getPressureCondition(er,opr.resultRemarks);
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
			foundNumeric = getWaterSolubility(er, propertyValue);
			if (!foundNumeric && opr.resultRemarks!=null && !opr.resultRemarks.isBlank()) {
				foundNumeric = getWaterSolubility(er, opr.resultRemarks.replaceAll(" per ","/"));
			}
			getTemperatureCondition(er,propertyValue);
			if (er.temperature_C==null && remarks.contains("temperature:")) {
				getTemperatureCondition(er,opr.resultRemarks);
			}
			er.property_value_qualitative = opr.indicator.toLowerCase();
		} else if (propertyName==ExperimentalConstants.strVaporPressure) {
			foundNumeric = getVaporPressure(er,propertyValue);
			getTemperatureCondition(er,propertyValue);
			if (er.temperature_C==null && remarks.contains("temperature:")) {
				getTemperatureCondition(er,opr.resultRemarks);
			}
		} else if (propertyName==ExperimentalConstants.strLogKow) {
			foundNumeric = getLogProperty(er,propertyValue);
			getTemperatureCondition(er,propertyValue);
			if (er.temperature_C==null && remarks.contains("temperature:")) {
				getTemperatureCondition(er,opr.resultRemarks);
			}
		}
		
		if ((opr.categoryChemicalResultType!=null && (opr.categoryChemicalResultType.contains("Estimated") || opr.categoryChemicalResultType.contains("Read-Across") ||
				opr.categoryChemicalResultType.contains("Derived"))) || (opr.testSubstanceResultType!=null && opr.testSubstanceResultType.contains("Estimated")) ||
				(opr.testSubstanceComments!=null && opr.testSubstanceComments.contains("Read-Across"))) {
			er.keep = false;
		}
		
		if (!foundNumeric && (er.property_value_units_original==null || er.property_value_units_original.isBlank()) && 
				!((er.property_value_qualitative!=null && !er.property_value_qualitative.isBlank()) || (er.note!=null && !er.note.isBlank()))) {
			er.keep = false;
		}
		
		if (remarks.contains("adequately characterized") || remarks.contains("estimated") || remarks.contains("extrapolated") || 
				remarks.contains("calculated") || remarks.contains("model")) {
			er.flag = true;
		}
		
		er.finalizeUnits();
		records.add(er);
	}
	
	public static void main(String[] args) {
		ParseOFMPub p = new ParseOFMPub();
		p.createFiles();
	}
}
