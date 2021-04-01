package gov.epa.exp_data_gathering.parse.ADDoPT;

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
		Vector<JsonObject> records = RecordADDoPT.parseADDoPTRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordADDoPT> recordsADDoPT = new ArrayList<RecordADDoPT>();
			RecordADDoPT[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordADDoPT[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsADDoPT.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordADDoPT[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsADDoPT.add(tempRecords[i]);
					}
				}
			}
			
			Iterator<RecordADDoPT> it = recordsADDoPT.iterator();
			while (it.hasNext()) {
				RecordADDoPT r = it.next();
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	private void addExperimentalRecords(RecordADDoPT ar,ExperimentalRecords records) {
		if (ar.Observed_solubility_lg_mol_L_!=null && !ar.Observed_solubility_lg_mol_L_.isBlank()) {
			ExperimentalRecord er = new ExperimentalRecord();
			er.source_name = ExperimentalConstants.strSourceADDoPT;
			er.date_accessed = RecordADDoPT.lastUpdated;
			er.original_source_name = "Yalchowsky & He 2003";
			er.url = "https://doi.org/10.1002/jcc.24424";
			er.casrn = ar.CAS_number;
			er.property_name = ExperimentalConstants.strWaterSolubility;
			er.property_value_string = "Observed solubility, log(M): "+ar.Observed_solubility_lg_mol_L_+"; Temperature: "+ar.T;
			er.property_value_point_estimate_original = Double.parseDouble(ar.Observed_solubility_lg_mol_L_);
			er.property_value_units_original = ExperimentalConstants.str_log_M;
			er.temperature_C = Double.parseDouble(ar.T);
			uc.convertRecord(er);
			records.add(er);
		}
	}
	
	public static void main(String[] args) {
		ParseADDoPT p = new ParseADDoPT();
		p.createFiles();
	}
}
