package gov.epa.exp_data_gathering.parse.Burkhard;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import gov.epa.exp_data_gathering.parse.ChemicalBook.RecordChemicalBook;



public class ParseBurkhard extends Parse {

	public ParseBurkhard() {
		sourceName = "Burkhard"; // TODO Consider creating ExperimentalConstants.strSourceBurkhard instead.
		this.init();

		// TODO Is this a toxicity source? If so, rename original and experimental records files here.
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordBurkhard.parseBurkhardRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			List<RecordBurkhard> recordsBurkhard = new ArrayList<RecordBurkhard>();
			RecordBurkhard[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordBurkhard[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsBurkhard.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordBurkhard[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsBurkhard.add(tempRecords[i]);
					}
				}
			}

			Iterator<RecordBurkhard> it = recordsBurkhard.iterator();
			while (it.hasNext()) {
				RecordBurkhard r = it.next();
				addExperimentalRecords(r,recordsExperimental);
				// TODO Write addExperimentalRecord() method to parse this source.
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}
	
	private void addExperimentalRecords(RecordBurkhard rb, ExperimentalRecords recordsExperimental) {
		if (rb.Log_BCF_Steady_State_mean != null && !rb.Log_BCF_Steady_State_mean.isBlank()) {
			addNewExperimentalRecord(rb,"LogBCF", recordsExperimental);
		}
		if (rb.Log_BCF_Kinetic_mean != null && !rb.Log_BCF_Kinetic_mean.isBlank()) {
			addNewExperimentalRecord(rb,"LogBCFKinetic", recordsExperimental);
		}
		if (rb.Log_BAF_mean != null && !rb.Log_BAF_mean.isBlank()) {
			addNewExperimentalRecord(rb,"LogBAF", recordsExperimental);
		}
		
	}
	
	
	private void addNewExperimentalRecord(RecordBurkhard rb, String propertyName, ExperimentalRecords recordsExperimental) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
		Date date = new Date();  
		String strDate=formatter.format(date);
		String dayOnly = strDate.substring(0,strDate.indexOf(" "));
		ExperimentalRecord er = new ExperimentalRecord();
		er.updateNote("species:" + rb.Common_Name + ";" + "tissue:" + rb.Tissue + ";" + "exposure concentrations: " + rb.Exposure_Concentrations);
		er.source_name=sourceName;
		er.chemical_name = rb.Chemical;
		er.casrn = rb.CAS;
		er.original_source_name = rb.Reference;
		
		if (propertyName=="LogBCF") {
			er.property_name=propertyName;
			if (!rb.Log_BCF_Steady_State_mean.equals("NA")) {
			er.property_value_point_estimate_final = Double.parseDouble(rb.Log_BCF_Steady_State_mean);
			}
		}	else if (propertyName == "LogBCFKinetic") {
			er.property_name=propertyName;
			if (!rb.Log_BCF_Kinetic_mean.equals("NA")) {
			er.property_value_point_estimate_final = Double.parseDouble(rb.Log_BCF_Kinetic_mean);
			}
		} else if (propertyName == "LogBAF") {
			er.property_name=propertyName;
			if (!rb.Log_BAF_mean.equals("NA")) {
			er.property_value_point_estimate_final = Double.parseDouble(rb.Log_BAF_mean);
		}
		}
		recordsExperimental.add(er);

	}
	
	public static void main(String[] args) {
		ParseBurkhard p = new ParseBurkhard();
		p.createFiles();
	}
}