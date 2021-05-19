package gov.epa.exp_data_gathering.parse.AADashboard;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
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
import gov.epa.ghs_data_gathering.GetData.EChemPortalParse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

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
		Hashtable<String,String> dict = getIOR_Dictionary();
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
				addExperimentalRecord(r,recordsExperimental, dict);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}

	
	private void addExperimentalRecord(RecordAADashboard r, ExperimentalRecords recordsExperimental, Hashtable<String,String> dict) {
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
			er.property_value_string = r.category;
			// er.property_value_qualitative = r.category;
			try {
				er.property_value_point_estimate_final=Double.parseDouble(dict.get(r.category));
				
				if (er.property_value_point_estimate_final==-1) {
					er.keep=false;
					er.reason="Ambiguous skin irritation score";
				}
			} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("cant calc score:"+ r.category);
			}

			er.property_name = r.hazardName;
			er.chemical_name = r.name;
			er.updateNote(r.note);
			er.source_name = r.source;
			er.url = StringUtils.substringBefore(r.url, ";");
			recordsExperimental.add(er);
		}
		

	}
	
	
	private static Hashtable<String,String>getIOR_Dictionary() {
		Hashtable <String,String>ht=new Hashtable<>();
		
		String strA=EChemPortalParse.scoreAmbiguous;
		String strN=EChemPortalParse.scoreNegative;
		String strP=EChemPortalParse.scorePositive;
		
		ht.put("Classification not possible",strA);
		ht.put("Not classified",strN);
		ht.put("Category 1",strP);
		ht.put("Category 2",strP);
		ht.put("Category 1B",strP);
		ht.put("Category 1A-1C",strP);
		ht.put("Category 3",strP);
		ht.put("Category 2 Note: the substance of technical grade is in Category 1",strP);
		ht.put("Category 1C",strP);
		ht.put("Category 1A",strP);
		ht.put("Not classified (40% emulsion)",strA);
		ht.put("Category 2-3",strP);
		ht.put("Category 1B-C",strP);
		ht.put("o-: Category 1A-1C, m-: Category 2, p-: Category 1A-1C",strP);
		ht.put("O-: Category 3, S-: Category 3",strP);
		ht.put("Mixture: Classification not possible, O-: Classification not possible, S-: Classification not possible",strA);


		return ht;
	}

	
	public static void main(String[] args) {
		ParseAADashboard p = new ParseAADashboard();
		p.createFiles();
	}




}
