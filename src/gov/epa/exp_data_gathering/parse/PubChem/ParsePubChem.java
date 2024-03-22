package gov.epa.exp_data_gathering.parse.PubChem;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Reference;

public class ParsePubChem extends Parse {
	
	public ParsePubChem() {
		sourceName = RecordPubChem.sourceName;
		this.init();
		folderNameWebpages=null;
	}
	
	
	Gson gson = new Gson();

	/**
	 * Parses JSON entries in database to RecordPubChem objects, then saves them to a JSON file
	 */
	@Override
	protected void createRecords() {
		Vector<RecordPubChem> records = RecordPubChem.parseJSONsInDatabase();
		System.out.println("Added "+records.size()+" records");
		writeOriginalRecordsToFile(records);
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
			
			List<RecordPubChem> recordsPubChem = new ArrayList<RecordPubChem>();
			RecordPubChem[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordPubChem[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsPubChem.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordPubChem[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsPubChem.add(tempRecords[i]);
					}
				}
			}
			
			System.out.println(recordsPubChem.size());
			
			Iterator<RecordPubChem> it = recordsPubChem.iterator();
			while (it.hasNext()) {
				RecordPubChem r = it.next();
				
				if(r.propertyName.equals("Other Experimental Properties")) continue;
				if(r.propertyName.equals("Collision Cross Section")) continue;
				if(r.propertyName.equals("Odor Threshold")) continue;
				if(r.propertyName.equals("Ionization Potential")) continue;
				if(r.propertyName.equals("Polymerization")) continue;
				if(r.propertyName.equals("Stability/Shelf Life")) continue;
				if(r.propertyName.equals("Decomposition")) continue;
				if(r.propertyName.equals("Heat of Vaporization")) continue;
				if(r.propertyName.equals("Heat of Combustion")) continue;
				if(r.propertyName.equals("Corrosivity")) continue;
				if(r.propertyName.equals("Taste")) continue;
				if(r.propertyName.equals("Dissociation Constants")) continue;//TODO can get Acidic pKa if add more code
				
				
				recordsExperimental.add(r.toExperimentalRecord());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	
	public static void main(String[] args) {
		ParsePubChem p = new ParsePubChem();
		p.createFiles();
	}
}
