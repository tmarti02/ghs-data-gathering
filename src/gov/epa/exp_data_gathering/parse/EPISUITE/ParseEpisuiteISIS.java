package gov.epa.exp_data_gathering.parse.EPISUITE;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.ParseUtilities;

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
	
	
	private void addExperimentalRecords(RecordEpisuiteISIS r, ExperimentalRecords records) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
		Date date = new Date();  
		String strDate=formatter.format(date);
		String dayOnly = strDate.substring(0,strDate.indexOf(" "));
		
		ExperimentalRecord er = new ExperimentalRecord();
		
		er.keep=true;
		er.property_name=ExperimentalConstants.strWaterSolubility;
		er.date_accessed = dayOnly;
		er.temperature_C = r.Temperature;
		er.casrn=ParseUtilities.fixCASLeadingZero(r.CAS);		
		er.chemical_name = r.Name;
		er.smiles=r.Smiles;
		
		if (r.Smiles==null || r.Smiles.isEmpty()) {
			er.keep=false;
			er.reason="smiles missing";
		} else if (r.WS_LogMolar==null) {
			er.flag=true;
			er.note="logMolar value is null";
			System.out.println(er.casrn+"\t"+er.note);
		} else if (r.WS_LogMolarCalc==null) {
			er.flag=true;
			er.note="logMolarCalc value is null";
			System.out.println(er.casrn+"\t"+er.note);
		} else {
//			System.out.println(er.casrn+"\t"+r.WS_LogMolar+"\t"+r.WS_LogMolarCalc+"\t"+Math.abs(r.WS_LogMolar-r.WS_LogMolarCalc));
			
			if (Math.abs(r.WS_LogMolar-r.WS_LogMolarCalc)>0.5) {
				er.keep=false;
				er.reason="logM value doesnt match value calculated from mg/L value";
				System.out.println(er.casrn+"\t"+r.WS_LogMolar+"\t"+r.WS_LogMolarCalc);
			} else if (Math.abs(r.WS_LogMolar-r.WS_LogMolarCalc)>0.1) {
				er.flag=true;				
				er.note="logM value ("+r.WS_LogMolar+") doesnt match value calculated from mg/L value ("+r.WS_LogMolarCalc+")";
			}
		}

		
		er.property_value_string=r.WS_mg_L+" mg/L";
		er.property_value_point_estimate_original = r.WS_mg_L;
		er.property_value_units_original = ExperimentalConstants.str_mg_L;
		
//		er.property_value_string=r.WS_LogMolar+" "+ExperimentalConstants.str_log_M;
//		er.property_value_point_estimate_original = r.WS_LogMolar;
//		er.property_value_units_original = ExperimentalConstants.str_log_M;
				
		er.source_name = ExperimentalConstants.strSourceEpisuiteISIS;
		er.original_source_name=r.Reference;
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
