package gov.epa.exp_data_gathering.parse.DRD;

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

public class ParseDRD extends Parse {
	
	public ParseDRD() {
		sourceName = ExperimentalConstants.strSourceDRD;
		this.init();
		
		fileNameJSON_Records = sourceName +" Toxicity Original Records.json";
		fileNameFlatExperimentalRecords = sourceName +" Toxicity Experimental Records.txt";
		fileNameFlatExperimentalRecordsBad = sourceName +" Toxicity Experimental Records-Bad.txt";
		fileNameJsonExperimentalRecords = sourceName +" Toxicity Experimental Records.json";
		fileNameJsonExperimentalRecordsBad = sourceName +" Toxicity Experimental Records-Bad.json";
		fileNameExcelExperimentalRecords = sourceName +" Toxicity Experimental Records.xlsx";
	}
	
	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordDRD.parseDRDRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordDRD> recordsDRD = new ArrayList<RecordDRD>();
			RecordDRD[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordDRD[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsDRD.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordDRD[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsDRD.add(tempRecords[i]);
					}
				}
			}
			
			Iterator<RecordDRD> it = recordsDRD.iterator();
			while (it.hasNext()) {
				RecordDRD r = it.next();
				addExperimentalRecord(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	private void addExperimentalRecord(RecordDRD dr,ExperimentalRecords records) {
		ExperimentalRecord erIrr = new ExperimentalRecord();
		erIrr.date_accessed = RecordDRD.lastUpdated;
		erIrr.source_name = ExperimentalConstants.strSourceDRD;
		if (dr.dataSource!=null) {
			erIrr.original_source_name = dr.dataSource.replaceAll("[\n\r]", " ");
		}
		erIrr.url = "https://pubmed.ncbi.nlm.nih.gov/26997338/";
		
		String chemicalName = dr.testChemicalName.replaceAll("[\n\r]", " ").replaceAll(" \\([0-9]+ of [0-9]+\\)", "");
		erIrr.chemical_name = chemicalName;
		erIrr.casrn = dr.casrn.replaceAll("[\n\r]", " ");
		erIrr.property_name = "rabbit_" + ExperimentalConstants.strEyeIrritation;
		erIrr.property_value_string = dr.ghsClassification.replaceAll("[\n\r]", " ");

		erIrr.property_value_point_estimate_original = drdGHSToBinaryIrritation(erIrr.property_value_string);
		erIrr.property_value_point_estimate_final = erIrr.property_value_point_estimate_original;
		erIrr.property_value_units_original = "binary";
		erIrr.property_value_units_final = "binary";
		
		if (dr.shouldNotBeUsed!=null && !dr.shouldNotBeUsed.trim().isBlank()) {
			erIrr.keep = false;
			erIrr.reason = "Source indicates study should not be used: shouldNotBeUsed = \"" + dr.shouldNotBeUsed +"\"";
		}
		
		// TODO eliminate records based on test substance purity?
		
		uc.convertRecord(erIrr);
		
		if (erIrr.property_value_point_estimate_final==-1) {
			erIrr.keep=false;
			erIrr.reason="Ambiguous eye irritation score";
		}

		// Deep copy existing ER by serialization-deserialization
		// Will have same SCNM/shouldNotBeUsed -> same "keep" field, etc.
		if (erIrr.property_value_point_estimate_final>0) {
			ExperimentalRecord erCorr = gson.fromJson(gson.toJson(erIrr), ExperimentalRecord.class);
			erCorr.property_name = "rabbit_" + ExperimentalConstants.strEyeCorrosion;
			erCorr.property_value_point_estimate_original = drdGHSToBinaryCorrosion(erIrr.property_value_string, erIrr.property_value_point_estimate_final);
			erCorr.property_value_point_estimate_final = erCorr.property_value_point_estimate_original;
			
			records.add(erCorr);
		}
		
		records.add(erIrr);
	}
	
	private double drdGHSToBinaryIrritation(String ghs) {
		// Possible values: Cat 1, Cat 2A, Cat 2, Cat 2B, No Cat, "SCNM(Cat 1)", "SCNM(Cat 2A or higher)", "SCNM(Cat 2 or Cat 1)", "SCNM(Cat 2A)"
		// "SCNM(Cat 2)", "SCNM(Cat 2B)", "SCNM(No Cat)", "SCNM"
		// SCNM = study criteria not met

		if (ghs.contains("SCNM")) {
			return -1;
		} else if (ghs.contains("Cat 1") || ghs.contains("Cat 2")) {
			return 1;
		} else {
			return 0;
		}
	}
	
	private double drdGHSToBinaryCorrosion(String ghs, double isIrritant) {
		if (isIrritant<=0) {
			return isIrritant;
		} else if (ghs.contains("Cat 1")) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public static void main(String[] args) {
		ParseDRD p = new ParseDRD();
		p.createFiles();
	}
}
