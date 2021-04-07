package gov.epa.exp_data_gathering.parse.Bradley;

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
import gov.epa.exp_data_gathering.parse.ParseUtilities;

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
		Vector<JsonObject> records = RecordBradley.parseBradleyRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordBradley> recordsBradley = new ArrayList<RecordBradley>();
			RecordBradley[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordBradley[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsBradley.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordBradley[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsBradley.add(tempRecords[i]);
					}
				}
			}
			
			Iterator<RecordBradley> it = recordsBradley.iterator();
			while (it.hasNext()) {
				RecordBradley r = it.next();
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	private void addExperimentalRecords(RecordBradley br,ExperimentalRecords records) {
		if (br.concentration_M!=null && !br.concentration_M.isBlank()) {
			ExperimentalRecord er = new ExperimentalRecord();
			er.date_accessed = RecordBradley.lastUpdated;
			er.source_name = ExperimentalConstants.strSourceBradley;
			er.original_source_name = br.sample_or_citation;
			er.url = br.ref;
			er.chemical_name = br.solute;
			er.smiles = br.solute_SMILES;
			er.property_name = ExperimentalConstants.strWaterSolubility;
			er.property_value_string = "Concentration (M): "+br.concentration_M;
			ParseUtilities.getNumericalValue(er,br.concentration_M,br.concentration_M.length(),false);
			er.property_value_units_original = ExperimentalConstants.str_M;
			if (br.notes!=null && !br.notes.isBlank()) {
				ParseUtilities.getTemperatureCondition(er,br.notes);
				er.property_value_string = er.property_value_string + "; Temperature: "+br.notes;
			}
			uc.convertRecord(er);
			records.add(er);
		}
	}
	
	public static void main(String[] args) {
		ParseBradley p = new ParseBradley();
		p.createFiles();
	}
}
