package gov.epa.exp_data_gathering.parse.Arnot;

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
import kong.unirest.json.JSONObject;

public class ParseArnot extends Parse {

	public ParseArnot() {
		sourceName = "Arnot"; // TODO Consider creating ExperimentalConstants.strSourceArnot instead.
		this.init();

		// TODO Is this a toxicity source? If so, rename original and experimental records files here.
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordArnot.parseArnotRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			List<RecordArnot> recordsArnot = new ArrayList<RecordArnot>();
			RecordArnot[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordArnot[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsArnot.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordArnot[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsArnot.add(tempRecords[i]);
					}
				}
			}

			Iterator<RecordArnot> it = recordsArnot.iterator();
			while (it.hasNext()) {
				RecordArnot r = it.next();
				addExperimentalRecord(r,recordsExperimental);
				// TODO Write addExperimentalRecord() method to parse this source.
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}
	
	
	
	private static void addExperimentalRecord(RecordArnot r, ExperimentalRecords recordsExperimental) {
		ExperimentalRecord er = new ExperimentalRecord();
		
		er.chemical_name = r.Chemical_Name;
		er.casrn = r.CAS;
		er.property_name = ExperimentalConstants.strLogBCF;
		er.keep = false;
		
		
		JSONObject jsonNote = new JSONObject();
		jsonNote.put("species", r.commond_name);
		jsonNote.put("tissue", r.TissueAnalyzed);
		jsonNote.put("exposure concentrations (mean micrograms per liter)", r.waterConcMeanugPerLiter);
		
		er.updateNote(jsonNote.toString());

		er.original_source_name = "Arnot";
		er.reference = r.ReferenceTitle;
		
		System.out.println(r.wwlogbcfExptl);
		
		er.property_value_string = r.wwlogbcfExptl;
		
		if (r.endpoint_1_BAF_2_BCF_3_BCFfd_4_BAF.toString().equals("2.0") || r.endpoint_1_BAF_2_BCF_3_BCFfd_4_BAF.toString().equals("3.0")) {
			er.keep = true;
			er.property_value_point_estimate_final = Double.parseDouble(r.wwlogbcfExptl);
			
		}
		
		recordsExperimental.add(er);
	}
	
	
	public static void main(String[] args) {
		ParseArnot p = new ParseArnot();
		p.createFiles();
	}
	
}