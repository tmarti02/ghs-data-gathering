package gov.epa.exp_data_gathering.parse.Hayashi;

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
import gov.epa.exp_data_gathering.parse.ToxicityDictionary.DictionarySkinCorrosionIrritation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;


public class ParseHayashi extends Parse {

	public ParseHayashi() {
		sourceName = ExperimentalConstants.strSourceHayashi; // TODO Consider creating ExperimentalConstants.strSourceHayashi instead.
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
		Vector<JsonObject> records = RecordHayashi.parseHayashiRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			List<RecordHayashi> recordsHayashi = new ArrayList<RecordHayashi>();
			RecordHayashi[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordHayashi[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsHayashi.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordHayashi[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsHayashi.add(tempRecords[i]);
					}
				}
			}

			Iterator<RecordHayashi> it = recordsHayashi.iterator();
			while (it.hasNext()) {
				RecordHayashi r = it.next();
				addExperimentalRecord(r,recordsExperimental);
				// TODO Write addExperimentalRecord() method to parse this source.
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}

	private void addExperimentalRecord(RecordHayashi r, ExperimentalRecords recordsExperimental) {
		// TODO Auto-generated method stub
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
		Date date = new Date();  
		String strDate=formatter.format(date);
		String dayOnly = strDate.substring(0,strDate.indexOf(" "));		

		ExperimentalRecord er=new ExperimentalRecord();
		er.date_accessed=dayOnly;
		er.source_name=sourceName;
		er.property_name=ExperimentalConstants.strSkinIrritationPII;
		er.property_value_units_final=ExperimentalConstants.str_pii;
		er.chemical_name=r.Chemical;
		er.property_value_string=r.Experimental_skin_irritation_score;
		er.property_value_point_estimate_final = Double.parseDouble(r.Experimental_skin_irritation_score);
		er.note = "MW =" + r.MW;
		er.original_source_name="Hayashi M, Nakamura Y, Higashi K, Kato H, Kishida F, Kaneko H. A quantitative structure-Activity relationship study of the skin irritation potential of phenols. Toxicol In Vitro. 1999 Dec;13(6):915-22. doi: 10.1016/s0887-2333(99)00077-6. PMID: 20654567.";
		
		/*
		ExperimentalRecord erCorr = gson.fromJson(gson.toJson(er), ExperimentalRecord.class);
		erCorr.property_name = "rabbit_" + ExperimentalConstants.strSkinCorrosion;
		erCorr.property_value_units_final="binary";
		erCorr.property_value_point_estimate_original = convertPIIToBinaryCorrosion(er.property_value_point_estimate_final);
		erCorr.property_value_point_estimate_final = erCorr.property_value_point_estimate_original;
		if (erCorr.property_value_point_estimate_final==-1) {
			erCorr.keep=false;
			erCorr.reason="Not a corrosion record";
		}
		*/

		ExperimentalRecord erIrr = gson.fromJson(gson.toJson(er), ExperimentalRecord.class);
		erIrr.property_name = "rabbit_" + ExperimentalConstants.strSkinIrritation;
		erIrr.property_value_units_final="binary";
		erIrr.property_value_point_estimate_original = DictionarySkinCorrosionIrritation.convertPIIToBinaryIrritation(er.property_value_point_estimate_final);
		erIrr.property_value_point_estimate_final = erIrr.property_value_point_estimate_original;
		if (erIrr.property_value_point_estimate_final==-1) {
			erIrr.keep=false;
			erIrr.reason="Ambiguous skin irritation score";
		}

		// recordsExperimental.add(erCorr);
		recordsExperimental.add(erIrr);
		
	}
	
	/*
	private static double convertPIIToBinaryCorrosion(double propertyValue) {
		double CorrBinary = -1; // inapplicable record to be discarded
		if ((propertyValue >= 2.3) && (propertyValue < 4.0)) {
			CorrBinary = 0;
		} else if ((propertyValue >= 4.0)) {
			CorrBinary = 1;
		}
		return CorrBinary;
	}
	*/
	
	public static void main(String[] args) {
		ParseHayashi p = new ParseHayashi();
		p.createFiles();
		

	}

}