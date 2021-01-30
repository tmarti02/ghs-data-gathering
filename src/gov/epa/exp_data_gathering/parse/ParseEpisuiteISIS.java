package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

public class ParseEpisuiteISIS extends Parse {
	
	public ParseEpisuiteISIS() {
		sourceName = ExperimentalConstants.strSourceEpisuiteISIS;
		this.init();
	}
	
	@Override
	protected void createRecords() {
		Vector<RecordEpisuiteISIS> records = RecordEpisuiteISIS.recordWaterFragmentData();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordEpisuiteISIS[] recordsEpisuiteISIS = gson.fromJson(new FileReader(jsonFile), RecordEpisuiteISIS[].class);
			
			for (int i = 0; i < recordsEpisuiteISIS.length; i++) {
				RecordEpisuiteISIS rec = recordsEpisuiteISIS[i];
				addExperimentalRecords(rec,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return recordsExperimental;
	}
	
	
	private void addExperimentalRecords(RecordEpisuiteISIS reo, ExperimentalRecords records) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
		Date date = new Date();  
		String strDate=formatter.format(date);
		String dayOnly = strDate.substring(0,strDate.indexOf(" "));
		
		ExperimentalRecord er = new ExperimentalRecord();
		
		er.keep=true;
		er.property_name=ExperimentalConstants.strWaterSolubility;
		er.date_accessed = dayOnly;
		er.temperature_C = reo.Temperature;
		er.casrn=ParseUtilities.fixCASLeadingZero(reo.CAS);		
		er.chemical_name = reo.Name;
		
		er.property_value_string=reo.WS_mg_L+"";
		er.property_value_point_estimate_original = reo.WS_mg_L;
		er.property_value_units_original = ExperimentalConstants.str_mg_L;
		
		er.source_name = ExperimentalConstants.strSourceEpisuiteISIS;
		er.original_source_name=reo.Reference;
		er.smiles=reo.Smiles;
		er.url="http://esc.syrres.com/interkow/EpiSuiteData_ISIS_SDF.htm";
				
		uc.convertRecord(er);
		
		records.add(er);
	}
	
	public static void main(String[] args) {
		ParseEpisuiteISIS p = new ParseEpisuiteISIS();
		p.generateOriginalJSONRecords = true;
		p.createFiles();
	}

	
	
}
