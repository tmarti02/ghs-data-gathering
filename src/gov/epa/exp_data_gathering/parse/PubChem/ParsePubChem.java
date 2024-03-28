package gov.epa.exp_data_gathering.parse.PubChem;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.text.StringEscapeUtils;

import com.google.common.reflect.TypeToken;
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
		
		if(generateOriginalJSONRecords) {
			Vector<RecordPubChem> records = RecordPubChem.parseJSONsInDatabase();
			System.out.println("Added "+records.size()+" records");
			writeOriginalRecordsToFile(records);
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
			
			List<RecordPubChem> recordsPubChem = new ArrayList<RecordPubChem>();
			RecordPubChem[] tempRecords = null;
			
			System.out.println(howManyOriginalRecordsFiles);
			
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
			
			Hashtable<String,String>htCID=getCID_HT();
			
			Iterator<RecordPubChem> it = recordsPubChem.iterator();
			while (it.hasNext()) {
				RecordPubChem r = it.next();
				
				//Skip the following until we have code to handle it:
				if(r.propertyName.equals("Other Experimental Properties")) continue;//TODO see what properties are in there
				if(r.propertyName.equals("Collision Cross Section")) continue;
				if(r.propertyName.equals("Odor Threshold")) continue;
				if(r.propertyName.equals("Ionization Potential")) continue;
				if(r.propertyName.equals("Polymerization")) continue;
				if(r.propertyName.equals("Stability/Shelf Life")) continue;
				if(r.propertyName.equals("Decomposition")) continue;
				if(r.propertyName.equals("Heat of Vaporization")) continue;
				if(r.propertyName.equals("Heat of Combustion")) continue;
				if(r.propertyName.equals("Enthalpy of Sublimation")) continue;
				if(r.propertyName.equals("Corrosivity")) continue;
				if(r.propertyName.equals("Taste")) continue;
				if(r.propertyName.equals("Dissociation Constants")) continue;//TODO can get Acidic pKa if add more code
				if(r.propertyName.contains("pK")) continue;
				if(r.propertyName.equals("Ionization Efficiency")) continue;
				if(r.propertyName.equals("Optical Rotation")) continue;
				if(r.propertyName.equals("Refractive Index")) continue;
				if(r.propertyName.equals("Relative Evaporation Rate")) continue;
				if(r.propertyName.equals("Viscosity")) continue;				
				if(r.propertyName.equals("Surface Tension")) continue;
				if(r.propertyName.equals("pH")) continue;
				if(r.propertyName.equals("Acid Value")) continue;
				if(r.propertyName.equals("Additive")) continue;
				if(r.propertyName.equals("Organic modifier")) continue;
				if(r.propertyName.equals("Reference")) continue;
				if(r.propertyName.equals("Ionization mode")) continue;
				if(r.propertyName.equals("logIE")) continue;
				if(r.propertyName.equals("Acid Value")) continue;
				if(r.propertyName.equals("Instrument")) continue;
				if(r.propertyName.equals("Ion source")) continue;
				if(r.propertyName.equals("Stability")) continue;
				if(r.propertyName.equals("Dielectric Constant")) continue;
				if(r.propertyName.equals("Accelerating Rate Calorimetry (ARC)")) continue;
				if(r.propertyName.equals("Differential Scanning Calorimetry (DSC)")) continue;
				if(r.propertyName.equals("Dispersion")) continue;
				
				
				ExperimentalRecord er=r.toExperimentalRecord();
				
				if(er==null) continue;
				
				//do we want to trust the cid from compounds table in dsstox???
				if(htCID.containsKey(r.cid)) {
					er.dsstox_compound_id=htCID.get(r.cid);
				}
				
				
				recordsExperimental.add(er);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	public static Hashtable<String, String> getCID_HT() {
		Hashtable<String,String>ht=new Hashtable<>();

		Type listType = new TypeToken<ArrayList<JsonObject>>(){}.getType();
		
		List<JsonObject> jaMolWeight=null;
		try {
			Gson gson=new Gson();
			jaMolWeight = gson.fromJson(new FileReader("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\dsstox\\pubchem cids to dtxcids.json"), listType);
			
			for (JsonObject jo:jaMolWeight) {
				String pubchemCID=jo.get("pubchem_cid").getAsString();
				
				if(jo.get("dsstox_compound_id").isJsonNull()) continue;
				
				String dtxcid=jo.get("dsstox_compound_id").getAsString();
				ht.put(pubchemCID,dtxcid);
//				System.out.println(pubchemCID+"\t"+dtxcid);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ht;
	}

	
	public static void main(String[] args) {
		ParsePubChem p = new ParsePubChem();
		p.howManyOriginalRecordsFiles=3;
		p.generateOriginalJSONRecords=true;
		p.writeExcelExperimentalRecordsFile=true;
		p.writeCheckingExcelFile=false;
		p.createFiles();
	}
}
