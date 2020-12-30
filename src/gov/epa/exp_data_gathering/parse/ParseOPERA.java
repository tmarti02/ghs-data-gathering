package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

public class ParseOPERA extends Parse {

	public ParseOPERA() {
		sourceName = "OPERA";
		this.init();
	}
	@Override
	protected void createRecords() {
		Vector<RecordOPERA> records = RecordOPERA.parseOperaSdf();
		writeOriginalRecordsToFile(records);
	}
	
	/**
	 * Reads the JSON file created by createRecords() and translates it to an ExperimentalRecords object
	 */
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordOPERA[] recordsOPERA = gson.fromJson(new FileReader(jsonFile), RecordOPERA[].class);
			
			for (int i = 0; i < recordsOPERA.length; i++) {
				RecordOPERA r = recordsOPERA[i];
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	/**
	 * populates experimentalrecord fields with data from the recordOPERA object.
	 * @param rs
	 * @param records
	 */
	private void addExperimentalRecords(RecordOPERA ro,ExperimentalRecords records) {
		// if (ro.property_name == ExperimentalConstants.strMeltingPoint) {
			ExperimentalRecord er = new ExperimentalRecord();
			er.chemical_name = ro.preferred_name;
			er.property_name = ro.property_name;
			er.property_value_point_estimate_original = ro.property_value_point_estimate_original;
			er.property_value_units_original = ro.property_value_units_original;
			er.casrn = ro.CAS;
			er.note = "qc_level= " + ro.qc_level;
			er.date_accessed = java.time.LocalDate.now().toString();
			er.finalizeRecord();

			
			if (!(ro.property_value_point_estimate_original.isNaN()) && (!(ro.CAS.contains("NOCAS")))) 
				er.keep = true;
			else
				er.keep = false;
			
				records.add(er);
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParseOPERA p = new ParseOPERA();
		p.mainFolder = p.mainFolder + File.separator + "General";
		p.jsonFolder= p.mainFolder;
		p.createFiles();
	}

}
