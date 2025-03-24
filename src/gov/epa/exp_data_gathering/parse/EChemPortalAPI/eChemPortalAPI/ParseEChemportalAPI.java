package gov.epa.exp_data_gathering.parse.EChemPortalAPI.eChemPortalAPI;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query.APIConstants;
import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.UtilitiesUnirest;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Processing.FinalRecord;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Processing.FinalRecords;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query.ToxQueryOptions;
import gov.epa.exp_data_gathering.parse.PubChem.ParsePubChem;
import gov.epa.exp_data_gathering.parse.PubChem.RecordPubChem;

public class ParseEChemportalAPI extends Parse {
	
//	String endpointKind=APIConstants.acuteToxicityOral;
	public String endpointKind=APIConstants.skinSensitisation;
	
	public ParseEChemportalAPI () {
		sourceName = "eChemPortalAPI";
		this.init(endpointKind);
		
//		this.fileNameJsonExperimentalRecords=sourceName+" "+endpointKind+" Experimental Records.json";
//		this.fileNameJsonExperimentalRecordsBad=sourceName+" "+endpointKind+" Experimental Records-Bad.json";
		
//		if(endpointKind.equals(APIConstants.skinSensitisation)) {
//			this.fileNameJsonExperimentalRecords=sourceName+" "+ExperimentalConstants.strSkinSensitizationLLNA+" Experimental Records.json";
//		} else if(endpointKind.equals(APIConstants.acuteToxicityOral)) {
//			this.fileNameJsonExperimentalRecords=sourceName+" "+ExperimentalConstants.strSkinSensitizationLLNA+" Experimental Records.json";
//		}
		
		
		
		folderNameWebpages=null;
	}
	
	/**
	 * Parses JSON entries in database to RecordPubChem objects, then saves them to a JSON file
	 */
	@Override
	protected void createRecords() {
		
		int maxSize=10000;//max number of records in api call
		
		if(generateOriginalJSONRecords) {
			
			String sourceName="eChemPortalAPI";
			String folderMain="data\\experimental\\"+sourceName+"\\";
			String databasePath=folderMain+sourceName+"_"+endpointKind+"_RawData.db";
			
			ToxQueryOptions options = ToxQueryOptions.generateEndpointToxQueryOptionsForToxVal(endpointKind);
			
			Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
//			System.out.println(gson.toJson(options));
//			options.runDownload(databasePath, true, maxSize);
			
			File excelFolder=new File(folderMain + "excel files");
			excelFolder.mkdirs();
			String excelFileName = "eChemPortalAPI_" + endpointKind + "_FinalRecords.xlsx";
			String excelFilePath = excelFolder.getAbsolutePath()+File.separator + excelFileName;
			
			FinalRecords records=eChemPortalAPI.parseAndWriteEndpointResults(databasePath,excelFilePath);
			records.toJsonFile(excelFilePath.replace(".xlsx", ".json"));
	
		}
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {

		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();		

		String jsonPath=folderNameExcel+File.separator+"eChemPortalAPI_"+this.endpointKind+"_FinalRecords.json";
		System.out.println(folderNameExcel);
		ExperimentalRecords experimentalRecords=new ExperimentalRecords();

		try {
			FinalRecord[] tempRecords = gson.fromJson(new FileReader(jsonPath), FinalRecord[].class);
			
			
			int counter=0;
			
			
//			HashSet<String>valueTypes=new HashSet<>();
			Hashtable<String,Integer>valueTypes=new Hashtable<>();
//			HashSet<String>speciesList=new HashSet<>();
			Hashtable<String,Integer>speciesList=new Hashtable<>();
			
			
//			System.out.println(tempRecords.length);
			
			for (FinalRecord fr:tempRecords) {
			
				String species=fr.species.get(0);
				
				if(speciesList.containsKey(species)) {
					speciesList.put(species, speciesList.get(species)+1);
				} else {
					speciesList.put(species, 1);
				}
				
				
//				speciesList.add(species);
				
				
				counter++;
				
//				if(fr.species.get(0).equals("other: rats and cats")) {
//				if(fr.species.get(0).equals("other: rats and mice")) {
//					System.out.println(gson.toJson(fr)+"\n");
//				}
				
//				if(fr.valueTypes.size()>1) {
//					System.out.println(fr.valueTypes+"\t"+fr.experimentalValues+"\t"+fr.species);
//				}
				
//				if(fr.species.size()>1)
//					System.out.println(fr.species);
				
				//If there are multiple exp values:
				
//				for (String experimentalValue:fr.experimentalValues) {
					
				if(fr.experimentalValues.size()==0) {
					ExperimentalRecord er=fr.toExperimentalRecord();
//					System.out.println(gson.toJson(fr)+"\n");
//					System.out.println(gson.toJson(er)+"\n******************");
					
					if (er.property_name==null) {
						System.out.println(gson.toJson(er));
					}
					
					
					
					experimentalRecords.add(er);
//					if(true)break;

				} else {
					for (int i=0;i<fr.experimentalValues.size();i++) {	
						ExperimentalRecord er=fr.toExperimentalRecord(fr.experimentalValues.get(i),fr.valueTypes.get(i));
						
						if(er.property_name==null) continue;
						
//						System.out.println(gson.toJson(fr)+"\n");
//						System.out.println(gson.toJson(er)+"\n******************");
						experimentalRecords.add(er);
//						valueTypes.add(fr.valueTypes.get(i));
						
						
						String vt=fr.valueTypes.get(i);
						if(valueTypes.containsKey(vt)) {
							valueTypes.put(vt, valueTypes.get(vt)+1);
						} else {
							valueTypes.put(vt, 1);
						}
					}
				}
				
				
				
				
				//if not:
//				ExperimentalRecord er=fr.toExperimentalRecord();
//				System.out.println(gson.toJson(fr)+"\n");
//				System.out.println(gson.toJson(er)+"\n******************");
//				experimentalRecords.add(er);

//				if(counter==100)break;
				
			}
			
//			for(String valueType:valueTypes.keySet()) {
//				if(valueType.contains("LD50")) {
//					System.out.println(valueType+"\t"+valueTypes.get(valueType));	
//				}
//			}
			
//			for (String species:speciesList.keySet()) {
//				
//				if(species.contains("rat")) {
//					System.out.println(species+"\t"+speciesList.get(species));
//				}
//					
//			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} 

		return experimentalRecords;

	}


	public static void main(String[] args) {
		ParseEChemportalAPI p = new ParseEChemportalAPI();
		
		p.generateOriginalJSONRecords=false;

		
		if(p.endpointKind.equals(APIConstants.skinSensitisation) || p.endpointKind.equals(APIConstants.skinIrritationCorrosion)) {
			p.removeDuplicates=false;
		} else {
			p.removeDuplicates=true;
		}
		
		
		p.writeJsonExperimentalRecordsFile=true;
		p.writeExcelExperimentalRecordsFile=true;
		p.writeExcelFileByProperty=true;		
		p.writeCheckingExcelFile=false;//creates random sample spreadsheet
		p.createFiles();
		
	}

}
