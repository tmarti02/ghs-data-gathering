package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

/**
 * Parses data from ADDoPT, accessible at: https://onlinelibrary.wiley.com/doi/epdf/10.1002/jcc.24424 (supplementary info table 1)
 * @author GSINCL01
 *
 */
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
		if (ar.solubility!=null && !ar.solubility.isBlank()) {
			ExperimentalRecord er = new ExperimentalRecord();
			er.source_name = ExperimentalConstants.strSourceADDoPT;
			er.date_accessed = RecordADDoPT.lastUpdated;
			er.original_source_name = "Yalchowsky & He 2003";
			er.url = "https://doi.org/10.1002/jcc.24424";
			er.casrn = ar.cas;
			er.property_name = ExperimentalConstants.strWaterSolubility;
			er.property_value_string = "Observed solubility, log(M): "+ar.solubility+"; Temperature: "+ar.temp;
			er.property_value_point_estimate_original = Double.parseDouble(ar.solubility);
			er.property_value_units_original = ExperimentalConstants.str_log_M;
			er.temperature_C = Double.parseDouble(ar.temp);
			uc.convertRecord(er);
			records.add(er);
		}
	}
	
	public static void main(String[] args) {
		ParseADDoPT p = new ParseADDoPT();
		p.createFiles();
	}
}
