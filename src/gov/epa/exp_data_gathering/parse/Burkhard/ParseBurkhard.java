package gov.epa.exp_data_gathering.parse.Burkhard;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
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
import com.google.gson.reflect.TypeToken;

import gov.epa.QSAR.utilities.JsonUtilities;
import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.JSONUtilities;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.Burkhard.RecordBurkhard.Species;
import kong.unirest.json.JSONObject;

public class ParseBurkhard  {


	Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public void getBCF_ExperimentalRecords(String propertyName) {

		String source=RecordBurkhard.sourceName;
		boolean createOriginalRecords=false;

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
//			System.out.println(records.size());
		} else {
			try {
				RecordBurkhard[]records2 = gson.fromJson(new FileReader(jsonPath), RecordBurkhard[].class);
				recordsOriginal=Arrays.asList(records2);
//				System.out.println(recordsOriginal.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		ExperimentalRecords experimentalRecords=new ExperimentalRecords();
		//		Hashtable<String, Double> htMWfromDTXSID = getMolWeightHashtable();//usi
		try {
			Type type = new TypeToken<Hashtable<String, List<RecordBurkhard.Species>>>(){}.getType();
			Hashtable<String, List<Species>>htSpecies=JsonUtilities.gsonPretty.fromJson(new FileReader("data\\experimental\\Arnot 2006\\htSuperCategory.json"), type);


			int counter=0;
			if(propertyName.toLowerCase().contains("bioconcentration")) {
				for (RecordBurkhard rb:recordsOriginal) {

					counter++;

					ExperimentalRecord erKinetic=rb.toExperimentalRecordBCF_Kinetic(propertyName, htSpecies);
					if(erKinetic!=null)	experimentalRecords.add(erKinetic);

					ExperimentalRecord erSS=rb.toExperimentalRecordBCF_SS(propertyName, htSpecies);
					if(erSS!=null)	experimentalRecords.add(erSS);

				}
			} else if(propertyName.toLowerCase().contains("bioaccumulation")) {
				for (RecordBurkhard rb:recordsOriginal) {

					counter++;
					
					ExperimentalRecord er=rb.toExperimentalRecordBAF(propertyName, htSpecies);
					if(er!=null) experimentalRecords.add(er);
				}
			}
//			for (RecordBurkhard rb:recordsOriginal) {
//
//				counter++;
//
//				ExperimentalRecord erKinetic=rb.toExperimentalRecordBCF_Kinetic(propertyName, htSpecies);
//				if(erKinetic!=null)	experimentalRecords.add(erKinetic);
//
//				ExperimentalRecord erSS=rb.toExperimentalRecordBCF_SS(propertyName, htSpecies);
//				if(erSS!=null)	experimentalRecords.add(erSS);
//
//			}

//		System.out.println(counter);


			Hashtable<String, ExperimentalRecords> htER = experimentalRecords.createExpRecordHashtableBySID(ExperimentalConstants.str_L_KG);
			ExperimentalRecords.calculateAvgStdDevOverAllChemicals(htER, true);
		
			Hashtable<String,Double>htMedian=ExperimentalRecords.calculateMedian(htER, true);
		
			System.out.println("property="+propertyName);
			System.out.println("originalRecords.size()="+recordsOriginal.size());
			System.out.println("experimentalRecords.size()="+experimentalRecords.size());
			//		

			//Writer experimental records to Json file:
			String mainFolder = "Data" + File.separator + "Experimental" + File.separator + source;
			mainFolder+=File.separator+propertyName;
			new File(mainFolder).mkdirs();
		
			String fileNameJsonExperimentalRecords = source+" Experimental Records.json";
			String fileNameJsonExperimentalRecordsBad = source+" Experimental Records-Bad.json";
			experimentalRecords.toExcel_File_Split(mainFolder+File.separator+fileNameJsonExperimentalRecords.replace("json", "xlsx"),100000);
		
			ExperimentalRecords experimentalRecordsBad = experimentalRecords.dumpBadRecords();

			JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(experimentalRecords),mainFolder+File.separator+fileNameJsonExperimentalRecords);
			JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(experimentalRecordsBad),mainFolder+File.separator+fileNameJsonExperimentalRecordsBad);

			System.out.println("");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}



	public static void main(String[] args) {
		ParseBurkhard p = new ParseBurkhard();
		//		p.generateOriginalJSONRecords=false;
		//		p.createFiles();

//		p.getBCF_ExperimentalRecords(ExperimentalConstants.strBCF);
//		p.getBCF_ExperimentalRecords(ExperimentalConstants.strFishBCF);
//		p.getBCF_ExperimentalRecords(ExperimentalConstants.strFishBCFWholeBody);
		p.getBCF_ExperimentalRecords(ExperimentalConstants.strBAF);
		p.getBCF_ExperimentalRecords(ExperimentalConstants.strFishBAF);
		p.getBCF_ExperimentalRecords(ExperimentalConstants.strFishBAFWholeBody);
	}

}