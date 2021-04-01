package gov.epa.exp_data_gathering.parse.AqSolDB;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;

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
		Vector<JsonObject> records = RecordAqSolDB.parseAqSolDBRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordAqSolDB> recordsAqSolDB = new ArrayList<RecordAqSolDB>();
			RecordAqSolDB[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordAqSolDB[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsAqSolDB.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordAqSolDB[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsAqSolDB.add(tempRecords[i]);
					}
				}
			}
			
			Iterator<RecordAqSolDB> it = recordsAqSolDB.iterator();
			while (it.hasNext()) {
				RecordAqSolDB r = it.next();
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	private void addExperimentalRecords(RecordAqSolDB ar,ExperimentalRecords records) {
		if (ar.Solubility!=null && !ar.Solubility.isBlank()) {
			ExperimentalRecord er = new ExperimentalRecord();
			er.date_accessed = RecordAqSolDB.lastUpdated;
			er.source_name = ExperimentalConstants.strSourceAqSolDB;
			if (ar.ID.contains("A")) { 
				er.original_source_name = ExperimentalConstants.strSourceEChemPortal;
				er.url = "https://www.echemportal.org/echemportal/property-search";
			} else if (ar.ID.contains("B")) { 
				er.original_source_name = "EPI Suite Data 1995";
				er.url = "http://esc.syrres.com/interkow/Download/WaterFragmentDataFiles.zip";
			} else if (ar.ID.contains("C")) { 
				er.original_source_name = "Raevsky, Grigorev, Poliancyk, et al. 2014";
				er.url = "https://doi.org/10.1021/ci400692n";
			} else if (ar.ID.contains("D")) { 
				er.original_source_name = "EPI Suite Data 1994";
				er.url = "http://esc.syrres.com/interkow/Download/WSKOWWIN_Datasets.zip";
			} else if (ar.ID.contains("E")) { 
				er.original_source_name = "Huuskonen 2000";
				er.url = "http://cheminformatics.org/datasets/";
			} else if (ar.ID.contains("F")) { 
				er.original_source_name = "Wang, Hou, & Xu 2009";
				er.url = "https://doi.org/10.1021/ci800406y";
			} else if (ar.ID.contains("G")) { 
				er.original_source_name = "Delaney 2004";
				er.url = "https://doi.org/10.1021/ci034243x";
			} else if (ar.ID.contains("H")) { 
				er.original_source_name = "Wang, Hou, & Xu 2009";
				er.url = "https://doi.org/10.1021/ci800406y";
			} else if (ar.ID.contains("I")) {
				er.original_source_name = "Llinas, Glen, & Goodman 2008";
				er.url = "http://www-jmg.ch.cam.ac.uk/data/solubility/";
			}
			er.chemical_name = ar.Name;
			er.smiles = ar.SMILES;
			er.property_name = ExperimentalConstants.strWaterSolubility;
			er.property_value_string = "LogS: "+ar.Solubility;
			er.property_value_point_estimate_original = Double.parseDouble(ar.Solubility);
			er.property_value_units_original = ExperimentalConstants.str_log_M;
			uc.convertRecord(er);
			records.add(er);
		}
	}
	
	public static void main(String[] args) {
		ParseAqSolDB p = new ParseAqSolDB();
		p.createFiles();
	}
}
