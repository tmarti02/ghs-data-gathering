package gov.epa.exp_data_gathering.parse.EPISUITE;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordEpisuiteISIS> recordsEpisuiteISIS = new ArrayList<RecordEpisuiteISIS>();
			RecordEpisuiteISIS[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordEpisuiteISIS[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsEpisuiteISIS.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordEpisuiteISIS[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsEpisuiteISIS.add(tempRecords[i]);
					}
				}
			}
			
			Iterator<RecordEpisuiteISIS> it = recordsEpisuiteISIS.iterator();
			while (it.hasNext()) {
				RecordEpisuiteISIS r = it.next();
				addAllEndpointExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	private void addAllEndpointExperimentalRecords(RecordEpisuiteISIS r, ExperimentalRecords recordsExperimental) {
		
		String[] endpoints = {"HL", "VP"}; //,"VP", "BP", "MP","Kow"};   
		for (String s: endpoints) {           
			// addExperimentalRecords(r, recordsExperimental, s);
		}
		
		addExperimentalRecords(r, recordsExperimental, "HL");
		
	}
	
	
	private void addExperimentalRecords(RecordEpisuiteISIS r, ExperimentalRecords records, String abbrev) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
		Date date = new Date();  
		String strDate=formatter.format(date);
		String dayOnly = strDate.substring(0,strDate.indexOf(" "));
		
		ExperimentalRecord er = new ExperimentalRecord();
		
		boolean keep=false;
		er.date_accessed = dayOnly;
		er.temperature_C = r.Temperature;
		er.casrn=ParseUtilities.fixCASLeadingZero(r.CAS);		
		er.chemical_name = r.Name;
		er.smiles=r.Smiles;
		
		// why isn't this check working
		if (r.HL_dimensionless != null) {
			keep = true;
			er.keep=true;
			er.property_name = ExperimentalConstants.strHenrysLawConstant;
			er.property_value_units_original = ExperimentalConstants.str_dimensionless_H;
			er.property_value_string = String.valueOf(r.HL_dimensionless);
			er.property_value_point_estimate_original = r.HL_dimensionless;
			er.property_value_point_estimate_final = r.HL_dimensionless;
			
			uc.convertRecord(er);

		}
		
		er.source_name = ExperimentalConstants.strSourceEpisuiteISIS;
		er.original_source_name=r.Reference;
		er.url="http://esc.syrres.com/interkow/EpiSuiteData_ISIS_SDF.htm";
				
		if (keep=true) {
		// records.add(er);
		}
	}

		
		/*
		
		if (abbrev.equals("MP") && r.MP != null) {
			er.keep=true;
			er.reason = "";
			er.property_name = ExperimentalConstants.strMeltingPoint;
			er.property_value_units_original = ExperimentalConstants.str_C;
			er.property_value_string = String.valueOf(r.MP);
			er.property_value_point_estimate_original = r.MP;
			
			uc.convertRecord(er);

		}

		if (abbrev.equals("BP") && r.BP != null) {
			er.keep=true;
			er.reason = "";
			er.property_name = ExperimentalConstants.strBoilingPoint;
			er.property_value_units_original = ExperimentalConstants.str_C;
			er.property_value_string = String.valueOf(r.BP);
			er.property_value_point_estimate_original = r.BP;
			
			uc.convertRecord(er);

		}

		if (abbrev.equals("VP") && r.VP != null) {
			er.keep=true;
			er.reason = "";
			er.property_name = ExperimentalConstants.strVaporPressure;
			er.property_value_units_original = ExperimentalConstants.str_mmHg;
			er.property_value_string = String.valueOf(r.VP);
			er.property_value_point_estimate_original = r.VP;
			
			uc.convertRecord(er);
		}
		
		if (abbrev.equals("Kow") && r.KOW != null) {
			er.keep=true;
			er.reason = "";
			er.property_name = ExperimentalConstants.strLogKow;
			er.property_value_string = String.valueOf(r.KOW);
			er.property_value_point_estimate_original = r.KOW;
			
			uc.convertRecord(er);
		}
		
		/*

		/*
		
		if (abbrev.equals("WS") && (r.WS_LogMolar != null || r.WS_mg_L != null)) {
			er.property_name=ExperimentalConstants.strWaterSolubility;

			if (r.WS_LogMolar==null) {
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
		
		
		}
		
		

		if (r.Smiles==null || r.Smiles.isEmpty()) {
			er.keep=false;
			er.reason="smiles missing";
		}

*/
		
//		er.property_value_string=r.WS_LogMolar+" "+ExperimentalConstants.str_log_M;
//		er.property_value_point_estimate_original = r.WS_LogMolar;
//		er.property_value_units_original = ExperimentalConstants.str_log_M;
				
		
		/*
		er.source_name = ExperimentalConstants.strSourceEpisuiteISIS;
		er.original_source_name=r.Reference;
		er.url="http://esc.syrres.com/interkow/EpiSuiteData_ISIS_SDF.htm";
				
		if (er.keep=true) {
		records.add(er);
		}
	}
	*/
	
	public static void main(String[] args) {
		ParseEpisuiteISIS p = new ParseEpisuiteISIS();
		p.generateOriginalJSONRecords = true;
		p.createFiles();
	}

	
	
}
