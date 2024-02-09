package gov.epa.exp_data_gathering.parse.ECOTOX;

import java.util.Vector;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.UnitConverter;
import gov.epa.exp_data_gathering.parse.OPERA.ParseOPERA;
import gov.epa.exp_data_gathering.parse.OPERA.RecordOPERA;


import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
* @author TMARTI02
*/
public class ParseEcotox extends Parse {

	
	public ParseEcotox() {
		sourceName = ExperimentalConstants.strSourceEcotox;
		this.init();
//		this.writeFlatFile=true;
	}
	
	
	@Override
	protected void createRecords() {
		
		if (!this.generateOriginalJSONRecords) return;
		
		Vector<RecordEcotox>records=RecordEcotox.getToxRecords();

		writeOriginalRecordsToFile(records);
	}

	
	class CustomComparator implements Comparator<ExperimentalRecord> {
	    @Override
	    public int compare(ExperimentalRecord o1, ExperimentalRecord o2) {
	        String key1=o1.property_name+"\t"+o1.comboID;
	        String key2=o2.property_name+"\t"+o2.comboID;
	    	return key1.compareTo(key2);
	    }
	}
	/**
	 * Reads the JSON file created by createRecords() and translates it to an ExperimentalRecords object
	 */
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {

		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordEcotox> records = new ArrayList<RecordEcotox>();
			RecordEcotox[] tempRecords = null;
			
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordEcotox[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					records.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					//Note need to look for indexOf .json and not . since can have . in the sourceName
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".json")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					System.out.println(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordEcotox[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						records.add(tempRecords[i]);
					}
				}
			}
			
			System.out.println("recordsOPERA.size()="+records.size());
			
			Iterator<RecordEcotox> it = records.iterator();
			while (it.hasNext()) {
				RecordEcotox r = it.next();
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("recordsExperimental.size()="+recordsExperimental.size());
		
		for (ExperimentalRecord er:recordsExperimental) {
			er.setComboID("\t");
		}
		
		Collections.sort(recordsExperimental,new CustomComparator());
		return recordsExperimental;
	}
	
	private void addExperimentalRecords(RecordEcotox r, ExperimentalRecords recordsExperimental) {

		ExperimentalRecord er=new ExperimentalRecord();
		
		er.dsstox_substance_id=r.dtxsid;
		er.property_name=r.property_name;
		er.keep=true;
		
		
		er.property_value_string="1.23 mg/L";
		
//		uc.convertRecord(er);
		
		recordsExperimental.add(er);
		
	}


	public static void main(String[] args) {
		ParseEcotox p = new ParseEcotox();
		
		p.generateOriginalJSONRecords = false;

//		p.maxExcelRows=999999;
		p.createFiles();

	}

}
