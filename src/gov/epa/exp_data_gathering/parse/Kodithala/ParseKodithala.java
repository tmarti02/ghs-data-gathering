package gov.epa.exp_data_gathering.parse.Kodithala;

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
import gov.epa.exp_data_gathering.parse.Hayashi.ParseHayashi;
import gov.epa.exp_data_gathering.parse.OECD_Toolbox.ParseOECD_Toolbox_alt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;

public class ParseKodithala extends Parse {

	public ParseKodithala() {
		sourceName = "Kodithala"; // TODO Consider creating ExperimentalConstants.strSourceKodithala instead.
		this.init();

		// TODO Is this a toxicity source? If so, rename original and experimental records files here.
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordKodithala.parseKodithalaRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			List<RecordKodithala> recordsKodithala = new ArrayList<RecordKodithala>();
			RecordKodithala[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordKodithala[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsKodithala.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordKodithala[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsKodithala.add(tempRecords[i]);
					}
				}
			}

			Iterator<RecordKodithala> it = recordsKodithala.iterator();
			while (it.hasNext()) {
				RecordKodithala r = it.next();
				addExperimentalRecord(r,recordsExperimental);
				// TODO Write addExperimentalRecord() method to parse this source.
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}

	private void addExperimentalRecord(RecordKodithala r, ExperimentalRecords recordsExperimental) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
		Date date = new Date();  
		String strDate=formatter.format(date);
		String dayOnly = strDate.substring(0,strDate.indexOf(" "));		

		ExperimentalRecord er=new ExperimentalRecord();
		er.date_accessed=dayOnly;
		er.source_name=sourceName;
		er.property_name=ExperimentalConstants.strSkinIrritationPII;
		er.property_value_units_final=ExperimentalConstants.str_pii;
		er.chemical_name=r.Compound_name;
		er.property_value_string=r.Observed_PII;
		er.property_value_point_estimate_final = Double.parseDouble(r.Observed_PII);
		er.original_source_name="Kiran Kodithala, A. J. Hopfinger, Edward D. Thompson, Michael K. Robinson, Prediction of Skin Irritation from Organic Chemicals Using Membrane-Interaction QSAR Analysis, Toxicological Sciences, Volume 66, Issue 2, April 2002, Pages 336-346, https://doi.org/10.1093/toxsci/66.2.336";
		recordsExperimental.add(er);
		
	}
	
	
	public static void main(String[] args) {
		ParseKodithala p = new ParseKodithala();
		p.createFiles();
		

	}

}