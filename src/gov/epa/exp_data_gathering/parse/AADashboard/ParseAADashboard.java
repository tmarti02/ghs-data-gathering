package gov.epa.exp_data_gathering.parse.AADashboard;

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
import gov.epa.exp_data_gathering.parse.AADashboard.ParseAADashboard;
import gov.epa.exp_data_gathering.parse.AADashboard.RecordAADashboard;
import gov.epa.exp_data_gathering.parse.Bagley.RecordBagley;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;


public class ParseAADashboard extends Parse {
	
	public ParseAADashboard() {
		sourceName = "AADashboard";
		this.init();
	}
	
	@Override
	protected void createRecords() {
    	Vector<RecordAADashboard> records = RecordAADashboard.databaseReader("AA dashboard.db", RecordAADashboard.sourceName);
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			List<RecordAADashboard> recordsAADashboard = new ArrayList<RecordAADashboard>();
			RecordAADashboard[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordAADashboard[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsAADashboard.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordAADashboard[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsAADashboard.add(tempRecords[i]);
					}
				}
			}

			Iterator<RecordAADashboard> it = recordsAADashboard.iterator();
			while (it.hasNext()) {
				RecordAADashboard r = it.next();
				addExperimentalRecord(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}

	
	private void addExperimentalRecord(RecordAADashboard r, ExperimentalRecords recordsExperimental) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
		Date date = new Date();  
		String strDate=formatter.format(date);
		String dayOnly = strDate.substring(0,strDate.indexOf(" "));		
		
		ExperimentalRecord er=new ExperimentalRecord();
		er.date_accessed=dayOnly;
		er.source_name=sourceName;
		
		// everything form here on out is specific to the skin irritation endpoint I am interested in.
		
		if (r.hazardName.equals("Skin Irritation") && r.source.equals("Japan")) {
			er.casrn = r.CAS;
			er.chemical_name = r.name;
			er.original_source_name = r.url;
			er.property_value_string = r.category + r.hazardCode;
			er.updateNote(r.rationale);
			er.updateNote(r.note);
			recordsExperimental.add(er);
		}
		

	}

	
	public static void main(String[] args) {
		ParseAADashboard p = new ParseAADashboard();
		p.createFiles();
	}




}
