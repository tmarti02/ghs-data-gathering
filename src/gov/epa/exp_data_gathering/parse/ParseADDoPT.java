package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

public class ParseADDoPT extends Parse {
	public ParseADDoPT() {
		sourceName = ExperimentalConstants.strSourceADDoPT;
		this.init();
	}
	
	@Override
	protected void createRecords() {
		Vector<RecordADDoPT> records = RecordADDoPT.parseADDoPTRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordADDoPT[] recordsADDoPT = gson.fromJson(new FileReader(jsonFile), RecordADDoPT[].class);
			
			for (int i = 0; i < recordsADDoPT.length; i++) {
				RecordADDoPT rec = recordsADDoPT[i];
				addExperimentalRecords(rec,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return recordsExperimental;
	}
	
	private void addExperimentalRecords(RecordADDoPT ar,ExperimentalRecords records) {
		if (ar.value!=null && !ar.value.isBlank()) {
			ExperimentalRecord er = new ExperimentalRecord();
			er.source_name = ExperimentalConstants.strSourceBradley;
			er.chemical_name = ar.name;
			er.casrn = ar.cas;
			er.property_name = ExperimentalConstants.strWaterSolubility;
			er.property_value_point_estimate_original = Math.pow(10.0,  Double.parseDouble(ar.value));
			er.property_value_units_original = ExperimentalConstants.str_M;
			er.temperature_C = Double.parseDouble(ar.temp);
			er.finalizeUnits();
			er.keep = true;
			er.flag = false;
			records.add(er);
		}
	}
	
	public static void main(String[] args) {
		ParseADDoPT p = new ParseADDoPT();
		p.createFiles();
	}
}
