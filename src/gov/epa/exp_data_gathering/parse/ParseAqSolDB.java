package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

/**
 * Parses data from AqSolDB, accessible at: https://dataverse.harvard.edu/dataset.xhtml?persistentId=doi:10.7910/DVN/OVHAW8
 * @author GSINCL01
 *
 */
public class ParseAqSolDB extends Parse {

	public ParseAqSolDB() {
		sourceName = ExperimentalConstants.strSourceAqSolDB;
		this.init();
	}
	
	@Override
	protected void createRecords() {
		Vector<RecordAqSolDB> records = RecordAqSolDB.parseAqSolDBRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordAqSolDB[] recordsAqSolDB = gson.fromJson(new FileReader(jsonFile), RecordAqSolDB[].class);
			
			for (int i = 0; i < recordsAqSolDB.length; i++) {
				RecordAqSolDB rec = recordsAqSolDB[i];
				addExperimentalRecords(rec,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return recordsExperimental;
	}
	
	private void addExperimentalRecords(RecordAqSolDB ar,ExperimentalRecords records) {
		if (ar.solubility!=null && !ar.solubility.isBlank()) {
			ExperimentalRecord er = new ExperimentalRecord();
			er.date_accessed = RecordAqSolDB.lastUpdated;
			er.source_name = ExperimentalConstants.strSourceAqSolDB;
			if (ar.id.contains("A")) { 
				er.original_source_name = ExperimentalConstants.strSourceEChemPortal;
				er.url = "https://www.echemportal.org/echemportal/property-search";
			} else if (ar.id.contains("B")) { 
				er.original_source_name = "EPI Suite Data 1995";
				er.url = "http://esc.syrres.com/interkow/Download/WaterFragmentDataFiles.zip";
			} else if (ar.id.contains("C")) { 
				er.original_source_name = "Raevsky, Grigorev, Poliancyk, et al. 2014";
				er.url = "https://doi.org/10.1021/ci400692n";
			} else if (ar.id.contains("D")) { 
				er.original_source_name = "EPI Suite Data 1994";
				er.url = "http://esc.syrres.com/interkow/Download/WSKOWWIN_Datasets.zip";
			} else if (ar.id.contains("E")) { 
				er.original_source_name = "Huuskonen 2000";
				er.url = "http://cheminformatics.org/datasets/";
			} else if (ar.id.contains("F")) { 
				er.original_source_name = "Wang, Hou, & Xu 2009";
				er.url = "https://doi.org/10.1021/ci800406y";
			} else if (ar.id.contains("G")) { 
				er.original_source_name = "Delaney 2004";
				er.url = "https://doi.org/10.1021/ci034243x";
			} else if (ar.id.contains("H")) { 
				er.original_source_name = "Wang, Hou, & Xu 2009";
				er.url = "https://doi.org/10.1021/ci800406y";
			} else if (ar.id.contains("I")) {
				er.original_source_name = "Llinas, Glen, & Goodman 2008";
				er.url = "http://www-jmg.ch.cam.ac.uk/data/solubility/";
			}
			er.chemical_name = ar.name;
			er.smiles = ar.smiles;
			er.property_name = ExperimentalConstants.strWaterSolubility;
			er.property_value_string = "LogS: "+ar.solubility;
			er.property_value_point_estimate_original = Double.parseDouble(ar.solubility);
			er.property_value_units_original = ExperimentalConstants.str_log_M;
			RecordFinalizer.finalizeRecord(er);
			records.add(er);
		}
	}
	
	public static void main(String[] args) {
		ParseAqSolDB p = new ParseAqSolDB();
		p.createFiles();
	}
}
