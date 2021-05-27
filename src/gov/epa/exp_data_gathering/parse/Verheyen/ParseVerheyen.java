package gov.epa.exp_data_gathering.parse.Verheyen;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.exec.util.StringUtils;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.UnitConverter;
import gov.epa.exp_data_gathering.parse.ThreeM.ParseThreeM;
import gov.epa.exp_data_gathering.parse.ThreeM.RecordThreeM;


public class ParseVerheyen extends Parse {

	public ParseVerheyen() {
		sourceName = ExperimentalConstants.strSourceVerheyen;
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
		Vector<JsonObject> records = RecordVerheyen.parseVerheyenRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			List<RecordVerheyen> recordsVerheyen = new ArrayList<RecordVerheyen>();
			RecordVerheyen[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordVerheyen[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsVerheyen.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordVerheyen[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsVerheyen.add(tempRecords[i]);
					}
				}
			}

			Iterator<RecordVerheyen> it = recordsVerheyen.iterator();
			while (it.hasNext()) {
				RecordVerheyen r = it.next();
				addExperimentalRecord(r,recordsExperimental);
				// TODO Write addExperimentalRecord() method to parse this source.
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}
	
	private void addExperimentalRecord(RecordVerheyen r, ExperimentalRecords recordsExperimental) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
		Date date = new Date();  
		String strDate=formatter.format(date);
		String dayOnly = strDate.substring(0,strDate.indexOf(" "));
		ExperimentalRecord er = new ExperimentalRecord();
		er.source_name = ExperimentalConstants.strSourceVerheyen;
		er.chemical_name = r.Name;
		er.casrn = r.CAS;
		er.date_accessed = dayOnly;
		er.property_value_string = r.Skin;
		int binary = 0;
		if (r.Skin != null && !(r.Skin.isBlank())) {
			if (r.Skin.equals("pos")) {
				er.property_value_point_estimate_final=Double.valueOf(1);
				er.property_value_units_final="binary";
			} else if (r.Skin.equals("neg")) {
				er.property_value_point_estimate_final=Double.valueOf(0);
				er.property_value_units_final="binary";
			}

		} else {
			er.keep = false;
			er.reason = "no data";
		}
		er.original_source_name = "Geert R. Verheyen, Els Braeken, Koen Van Deun, Sabine Van Miert, Evaluation of existing (Q)SAR models for skin and eye irritation and corrosion to use for REACH registration,Toxicology Letters, Volume 265, 2017, Pages 47-52, ISSN 0378-4274, https://doi.org/10.1016/j.toxlet.2016.11.007.";
		er.property_name = "rabbit_" + ExperimentalConstants.strSkinIrritation;
		recordsExperimental.add(er);
	}

	public static void main(String[] args) {
		ParseVerheyen p = new ParseVerheyen();
		p.createFiles();
		
	}

	
	
}