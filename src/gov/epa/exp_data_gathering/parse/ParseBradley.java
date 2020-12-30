package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

/**
 * Parses data from Bradley, accessible at: https://www.nature.com/articles/npre.2010.4243.3
 * @author GSINCL01
 *
 */
public class ParseBradley extends Parse {
	
	public ParseBradley() {
		sourceName = ExperimentalConstants.strSourceBradley;
		this.init();
	}
	
	@Override
	protected void createRecords() {
		Vector<RecordBradley> records = RecordBradley.parseBradleyRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordBradley[] recordsBradley = gson.fromJson(new FileReader(jsonFile), RecordBradley[].class);
			
			for (int i = 0; i < recordsBradley.length; i++) {
				RecordBradley rec = recordsBradley[i];
				addExperimentalRecords(rec,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return recordsExperimental;
	}
	
	private void addExperimentalRecords(RecordBradley br,ExperimentalRecords records) {
		if (br.concentration!=null && !br.concentration.isBlank()) {
			ExperimentalRecord er = new ExperimentalRecord();
			er.date_accessed = RecordBradley.lastUpdated;
			er.source_name = ExperimentalConstants.strSourceBradley;
			er.original_source_name = br.citation;
			er.url = br.refURL;
			er.chemical_name = br.solute;
			er.smiles = br.soluteSMILES;
			er.property_name = ExperimentalConstants.strWaterSolubility;
			er.property_value_string = "Concentration (M): "+br.concentration;
			ParseUtilities.getNumericalValue(er,br.concentration,br.concentration.length(),false);
			er.property_value_units_original = ExperimentalConstants.str_M;
			if (br.notes!=null && !br.notes.isBlank()) { ParseUtilities.getTemperatureCondition(er,br.notes); }
			er.finalizeRecord();
			er.keep = true;
			er.flag = false;
			records.add(er);
		}
	}
	
	public static void main(String[] args) {
		ParseBradley p = new ParseBradley();
		p.createFiles();
	}
}
