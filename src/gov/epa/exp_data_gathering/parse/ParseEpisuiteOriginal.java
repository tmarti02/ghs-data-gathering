package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

public class ParseEpisuiteOriginal extends Parse {
	
	public ParseEpisuiteOriginal() {
		sourceName = ExperimentalConstants.strSourceEpisuite;
		this.init();
	}
	
	@Override
	protected void createRecords() {
		Vector<RecordEpisuiteOriginal> records = RecordEpisuiteOriginal.recordWaterFragmentData();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordEpisuiteOriginal[] recordsEpisuiteOriginal = gson.fromJson(new FileReader(jsonFile), RecordEpisuiteOriginal[].class);
			
			for (int i = 0; i < recordsEpisuiteOriginal.length; i++) {
				RecordEpisuiteOriginal rec = recordsEpisuiteOriginal[i];
				addExperimentalRecords(rec,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return recordsExperimental;
	}
	
	
	private void addExperimentalRecords(RecordEpisuiteOriginal reo, ExperimentalRecords records) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
		Date date = new Date();  
		String strDate=formatter.format(date);
		String dayOnly = strDate.substring(0,strDate.indexOf(" "));
		
		ExperimentalRecord er = new ExperimentalRecord();
		
		er.keep=true;
		er.property_name=ExperimentalConstants.strWaterSolubility;
		er.date_accessed = dayOnly;
		er.temperature_C = reo.Temp;
		String temp = ParseUtilities.fixCASLeadingZero(reo.CAS);
		er.casrn = temp;
		er.chemical_name = reo.Name;
		er.property_value_point_estimate_original = reo.LogWsol;
		er.property_value_units_original = ExperimentalConstants.str_log_M;
		er.source_name = ExperimentalConstants.strSourceEpisuiteOriginal;
		
		uc.convertRecord(er);
		
		records.add(er);
	}
	
	public static void main(String[] args) {
		ParseEpisuiteOriginal p = new ParseEpisuiteOriginal();
		p.generateOriginalJSONRecords = false;
		p.createFiles();
	}

	
	
}
