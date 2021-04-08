package gov.epa.exp_data_gathering.parse.ThreeM;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.exec.util.StringUtils;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.UnitConverter;


public class ParseThreeM extends Parse {

	public ParseThreeM() {
		sourceName = "ThreeM";
		this.init();
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordThreeM.parseThreeMRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			List<RecordThreeM> recordsThreeM = new ArrayList<RecordThreeM>();
			RecordThreeM[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordThreeM[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsThreeM.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordThreeM[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsThreeM.add(tempRecords[i]);
					}
				}
			}

			Iterator<RecordThreeM> it = recordsThreeM.iterator();
			while (it.hasNext()) {
				RecordThreeM r = it.next();
				addExperimentalRecord(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}
	
	private void addExperimentalRecord(RecordThreeM r3m, ExperimentalRecords recordsExperimental) {
		ExperimentalRecord er = new ExperimentalRecord();
		er.chemical_name = r3m.test_substance_name;
		er.casrn = r3m.CASRN;
		er.synonyms = r3m.other_test_substance_name;
		er.synonyms = r3m.other_test_substance_name;
		
		if (r3m.property!=null && !r3m.property.isBlank())
		er.property_name =  r3m.property.substring(0,1).toUpperCase() + r3m.property.substring(1).toLowerCase();
		else er.property_name = "";
		
		if (r3m.property_value_units!=null) {
			if (r3m.property_value_units.equals("degrees C")) {
				er.property_value_units_original = ExperimentalConstants.str_C;
			} else if (r3m.property_value_units.equals("Pa")) {
				er.property_value_units_original = ExperimentalConstants.str_pa;
			} else if (r3m.property_value_units.equals("not determined")) {
				er.property_value_units_original = "";
		}
		}
		
		if (r3m.property_value!=null && !r3m.property_value.isBlank() && !(r3m.property_value.equals("not determined") || r3m.property_value.equals("ff 3.7") || r3m.property_value.equals("ff 349") || r3m.property_value.contains("+"))) {
			er.property_value_point_estimate_original = Double.parseDouble(r3m.property_value.replace(",", ""));
			er.property_value_string = "Value: " + r3m.property_value;
		} else if (r3m.property_value_min!=null && !r3m.property_value_min.isBlank()) {
			er.property_value_min_original = Double.parseDouble(r3m.property_value_min);
			if (r3m.property_value_max!=null && !r3m.property_value_max.isBlank()) {
				er.property_value_max_original = Double.parseDouble(r3m.property_value_max);
				er.property_value_string = "Value: " + r3m.property_value_min + "-" + r3m.property_value_max;
			}
			else {
				er.property_value_string = "Value: >" + r3m.property_value_min;
			}	
		} else if (r3m.property_value_max!=null && !r3m.property_value_max.isBlank()) {
			er.property_value_max_original = Double.parseDouble(r3m.property_value_max);
			er.property_value_string = "Value: <" + r3m.property_value_max;
		}
		
		if (r3m.property_value_method!=null && !r3m.property_value_method.isBlank()) {
			er.measurement_method = r3m.property_value_method;
		}
		
		uc.convertRecord(er);
		recordsExperimental.add(er);

	}
	
	public static void main(String[] args) {
		ParseThreeM p = new ParseThreeM();
		p.createFiles();
	}
}