package gov.epa.exp_data_gathering.parse.Burkhard;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.JSONUtilities;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.Burkhard.RecordBurkhard;
import gov.epa.exp_data_gathering.parse.ECOTOX.RecordEcotox;
import gov.epa.exp_data_gathering.parse.Kodithala.RecordKodithala;
import gov.epa.exp_data_gathering.parse.ToxVal.ParseToxVal;
import kong.unirest.json.JSONObject;

public class ParseBurkhard  {


	Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public void getBCF_ExperimentalRecords(String propertyName) {

		String source=RecordBurkhard.sourceName;
		boolean createOriginalRecords=false;

		boolean limitToFish=false;
		if(propertyName.toLowerCase().contains("fish")) {
			limitToFish=true;
		}

		boolean limitToWholeOrganism=false;
		if(propertyName.toLowerCase().contains("whole")) {
			limitToWholeOrganism=true;
		}

//		boolean limitToStandardTestSpecies=false;
//		if(propertyName.toLowerCase().contains("standard")) {
//			limitToStandardTestSpecies=true;
//		}

		String jsonPath = "data/experimental/"+source+File.separator+source+" "+ExperimentalConstants.strBCF+" original records.json";
		List<RecordBurkhard>recordsOriginal=null;

		if (createOriginalRecords) {
			Vector<JsonObject> records = RecordBurkhard.parseBurkhardRecordsFromExcel();
			for (int i=0;i<records.size();i++) {
				JsonObject jo=records.get(i);
				if(jo.get("Chemical").getAsString().isEmpty()) records.remove(i--);
			}
			int howManyOriginalRecordsFiles = JSONUtilities.batchAndWriteJSON(records,jsonPath);
			recordsOriginal=new ArrayList<>();
			for(JsonObject record:records) {
				String json=gson.toJson(record);
				RecordBurkhard rb=gson.fromJson(json, RecordBurkhard.class);
				recordsOriginal.add(rb);
			}
			System.out.println(records.size());
		} else {
			try {
				RecordBurkhard[]records2 = gson.fromJson(new FileReader(jsonPath), RecordBurkhard[].class);
				recordsOriginal=Arrays.asList(records2);
				System.out.println(recordsOriginal.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		ExperimentalRecords experimentalRecords=new ExperimentalRecords();

		//		Hashtable<String, Double> htMWfromDTXSID = getMolWeightHashtable();//usi

		int counter=0;

		for (RecordBurkhard rb:recordsOriginal) {

			counter++;

			ExperimentalRecord erKinetic=rb.toExperimentalRecordBCF_Kinetic(propertyName,limitToWholeOrganism,limitToFish);
			if(erKinetic!=null)	experimentalRecords.add(erKinetic);

			ExperimentalRecord erSS=rb.toExperimentalRecordBCF_SS(propertyName,limitToWholeOrganism,limitToFish);
			if(erSS!=null)	experimentalRecords.add(erSS);

		}

		System.out.println(counter);

		

		Hashtable<String, List<ExperimentalRecord>> htER = experimentalRecords.createExpRecordHashtableBySID(ExperimentalConstants.str_L_KG);
		ExperimentalRecords.calculateStdDev(htER, true);
		
		Hashtable<String,Double>htMedian=ExperimentalRecords.calculateMedian(htER, true);
		
		
		System.out.println("originalRecords.size()="+recordsOriginal.size());
		System.out.println("experimentalRecords.size()="+experimentalRecords.size());
		//		

		//Writer experimental records to Json file:
		String mainFolder = "Data" + File.separator + "Experimental" + File.separator + source;


		String fileNameJsonExperimentalRecords = source+"_"+propertyName+" Experimental Records.json";
		JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(experimentalRecords),mainFolder+File.separator+fileNameJsonExperimentalRecords);

		//Write experimental records to excel file:
		experimentalRecords.toExcel_File_Split(mainFolder+File.separator+fileNameJsonExperimentalRecords.replace("json", "xlsx"),100000);
		experimentalRecords.toExcel_FileDetailed(mainFolder+File.separator+fileNameJsonExperimentalRecords.replace("Records", "Records_Detailed").replace("json", "xlsx"));


	}



	public static void main(String[] args) {
		ParseBurkhard p = new ParseBurkhard();
		//		p.generateOriginalJSONRecords=false;
		//		p.createFiles();

		p.getBCF_ExperimentalRecords(ExperimentalConstants.strBCF);
//		p.getBCF_ExperimentalRecords(ExperimentalConstants.strFishBCF);
		//		p.getBCF_ExperimentalRecords(ExperimentalConstants.strFishBCFWholeBody);
	}

}