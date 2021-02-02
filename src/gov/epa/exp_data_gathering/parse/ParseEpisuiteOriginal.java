package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;


//References 15 and 17 of AqSolDB paper
//http://esc.syrres.com/interkow/Download/WaterFragmentDataFiles.zip
//http://esc.syrres.com/interkow/Download/WSKOWWIN_Datasets.zip


//Citation for AqSolDB paper
//Sorkun, M.C., Khetan, A. & Er, S. AqSolDB, a curated reference set of aqueous solubility and 2D descriptors for a diverse set of compounds. Sci Data 6, 143 (2019). https://doi.org/10.1038/s41597-019-0151-1



/**
 * @author cramslan
 * Turns recordEpisuite objects into experimentalrecord jsons.
 */
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
		
		er.source_name = ExperimentalConstants.strSourceEpisuite;
		er.url = reo.url;
		String temp = ParseUtilities.fixCASLeadingZero(reo.CAS);
		er.casrn = temp;

	
		if (reo.Property.matches(ExperimentalConstants.strWaterSolubility)) {
		er.keep=true;
		er.property_name=ExperimentalConstants.strWaterSolubility;
		er.date_accessed = dayOnly;
		er.temperature_C = reo.Temp;
		er.chemical_name = reo.Name;
		er.property_value_point_estimate_original = reo.WsolmgL;
		er.property_value_units_original = ExperimentalConstants.str_mg_L;
		er.property_value_string= "Wsol mg/L = " + reo.WsolmgL.toString();
		er.original_source_name=reo.Reference;
		
		uc.convertRecord(er);
		
		records.add(er);
		}
		
		else if (reo.Property.matches(ExperimentalConstants.strLogKow)) {
			er.keep=true;
			er.property_name=ExperimentalConstants.strLogKow;
			er.date_accessed= dayOnly;
			er.chemical_name=reo.Name;
			er.property_value_string = reo.LogP;
			ParseUtilities.getLogProperty(er,er.property_value_string);
			uc.convertRecord(er);
			
			records.add(er);
		}
	}
	
	public static void main(String[] args) {
		ParseEpisuiteOriginal p = new ParseEpisuiteOriginal();
		// p.generateOriginalJSONRecords = false;
		p.createFiles();
	}	
	
}
