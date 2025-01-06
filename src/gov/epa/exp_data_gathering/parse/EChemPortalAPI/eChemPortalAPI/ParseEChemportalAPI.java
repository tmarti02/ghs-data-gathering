package gov.epa.exp_data_gathering.parse.EChemPortalAPI.eChemPortalAPI;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Processing.FinalRecord;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Processing.FinalRecords;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query.ToxQueryOptions;
import gov.epa.exp_data_gathering.parse.PubChem.ParsePubChem;
import gov.epa.exp_data_gathering.parse.PubChem.RecordPubChem;

public class ParseEChemportalAPI extends Parse {
	
	String endpointKind="AcuteToxicityOral";	
	int maxSize=2000;
	
	public ParseEChemportalAPI () {
		sourceName = "eChemPortalAPI";
		this.init();
		folderNameWebpages=null;
	}

	
	/**
	 * Parses JSON entries in database to RecordPubChem objects, then saves them to a JSON file
	 */
	@Override
	protected void createRecords() {
		
		if(generateOriginalJSONRecords) {
			
			String sourceName="eChemPortalAPI";
			String folderMain="data\\experimental\\"+sourceName+"\\";
			String databasePath=folderMain+sourceName+"_"+endpointKind+"_RawData.db";
			
			ToxQueryOptions options = ToxQueryOptions.generateEndpointToxQueryOptionsForToxVal(endpointKind);
			
			Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
//			System.out.println(gson.toJson(options));
			
			options.runDownload(databasePath, true, maxSize);
			
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
			for (FinalRecord fr:tempRecords) {
				

				
				//If there are multiple exp values:
//				for (String experimentalValue:fr.experimentalValues) {
//					
//					ExperimentalRecord er=fr.toExperimentalRecord();
//					System.out.println(gson.toJson(fr)+"\n");
//					System.out.println(gson.toJson(er)+"\n******************");
//					experimentalRecords.add(er);
//					
//				}
				
				//if not:
				ExperimentalRecord er=fr.toExperimentalRecord();
				System.out.println(gson.toJson(fr)+"\n");
				System.out.println(gson.toJson(er)+"\n******************");
				experimentalRecords.add(er);


				
				
				
				if(true) break;
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 

		return experimentalRecords;

	}


	public static void main(String[] args) {
		ParseEChemportalAPI p = new ParseEChemportalAPI();
		
		p.generateOriginalJSONRecords=false;

		p.removeDuplicates=true;
		p.writeJsonExperimentalRecordsFile=false;
		p.writeExcelExperimentalRecordsFile=true;
		p.writeExcelFileByProperty=true;		
		p.writeCheckingExcelFile=false;//creates random sample spreadsheet
		p.createFiles();

	}

}
