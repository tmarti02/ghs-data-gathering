package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

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
		} else {
			er.flag = true;
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
		
		er.property_value_string = opr.value;
		if (opr.resultRemarks!=null && !opr.resultRemarks.isBlank()) { er.property_value_string = er.property_value_string + ";" + opr.resultRemarks; }
		
		boolean foundNumeric = false;
		String propertyName = er.property_name;
		String propertyValue = opr.value;
		if (propertyName==ExperimentalConstants.strMeltingPoint || propertyName==ExperimentalConstants.strBoilingPoint) {
			foundNumeric = getTemperatureProperty(er,propertyValue);
			getPressureCondition(er,propertyValue);
			if ((er.pressure_mmHg==null || er.pressure_mmHg.isBlank()) && opr.resultRemarks.contains("Pressure:")) {
				getPressureCondition(er,opr.resultRemarks);
			}
			if (opr.indicator.contains("Decomposes")) { er.updateNote(ExperimentalConstants.str_dec); }
			if (opr.indicator.contains("Sublimes")) { er.updateNote(ExperimentalConstants.str_subl); }
		} else if (propertyName==ExperimentalConstants.strWaterSolubility) {
			foundNumeric = getWaterSolubility(er, propertyValue);
			getTemperatureCondition(er,propertyValue);
			if (er.temperature_C==null && opr.resultRemarks.contains("Temperature:")) {
				getTemperatureCondition(er,opr.resultRemarks);
			}
			er.property_value_qualitative = opr.indicator.toLowerCase();
		} else if (propertyName==ExperimentalConstants.strVaporPressure) {
			foundNumeric = getVaporPressure(er,propertyValue);
			getTemperatureCondition(er,propertyValue);
			if (er.temperature_C==null && opr.resultRemarks.contains("Temperature:")) {
				getTemperatureCondition(er,opr.resultRemarks);
			}
		} else if (propertyName==ExperimentalConstants.strLogKow) {
			foundNumeric = getLogProperty(er,propertyValue);
			getTemperatureCondition(er,propertyValue);
			if (er.temperature_C==null && opr.resultRemarks.contains("Temperature:")) {
				getTemperatureCondition(er,opr.resultRemarks);
			}
		}
		
		if ((opr.categoryChemicalResultType!=null && (opr.categoryChemicalResultType.contains("Estimated") || opr.categoryChemicalResultType.contains("Read-Across") ||
				opr.categoryChemicalResultType.contains("Derived"))) || (opr.testSubstanceResultType!=null && opr.testSubstanceResultType.contains("Estimated")) ||
				((er.casrn==null || er.casrn.isBlank()) && (er.chemical_name==null || er.chemical_name.isBlank()))) {
			er.keep = false;
		}
		
		er.finalizeUnits();
		records.add(er);
	}
	
	public static void main(String[] args) {
		ParseOFMPub p = new ParseOFMPub();
		p.createFiles();
	}
}
