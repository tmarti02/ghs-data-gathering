package gov.epa.exp_data_gathering.parse.Arnot2006;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.xmlcml.cml.element.AbstractAtomType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.QSAR.utilities.JsonUtilities;
import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.JSONUtilities;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.Arnot2006.RecordArnot2006.Species;
import gov.epa.exp_data_gathering.parse.QSAR_ToolBox.RecordQSAR_ToolBox;

/**
* @author TMARTI02
*/
public class ParseArnot2006 extends Parse {
	
	String propertyName;
	
	public ParseArnot2006(String propertyName) {
		
		this.propertyName=propertyName;
		sourceName = RecordArnot2006.sourceName; 
		this.init();
		
		mainFolder = "Data" + File.separator + "Experimental" + File.separator + sourceName;
		jsonFolder= mainFolder;
		
		mainFolder+=File.separator+propertyName;//output json/excel in subfolder

		new File(mainFolder).mkdirs();

	}
	
	@Override
	protected void createRecords() {
		if(generateOriginalJSONRecords) {
			Vector<JsonObject> records = RecordArnot2006.parseRecordsFromExcel();
			System.out.println(records.size());
			writeOriginalRecordsToFile(records);
		}
	}

	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			
			
			Type type = new TypeToken<Hashtable<String, List<RecordArnot2006.Species>>>(){}.getType();
			Hashtable<String, List<Species>>htSpecies=JsonUtilities.gsonPretty.fromJson(new FileReader("data\\experimental\\Arnot 2006\\htSuperCategory.json"), type);
			
//			System.out.println(htSpecies.get("bluegill sunfish").size());
//			System.out.println(JsonUtilities.gsonPretty.toJson(htSpecies));
			
			File Folder=new File(jsonFolder);
			
			if(Folder.listFiles()==null) {
				System.out.println("No files in json folder:"+jsonFolder);
				return null;
			}
			
			for(File file:Folder.listFiles()) {
				if(!file.getName().contains(".json")) continue;
				if(!file.getName().contains(sourceName+" Original Records")) continue;
				
				RecordArnot2006[] tempRecords = gson.fromJson(new FileReader(file), RecordArnot2006[].class);
				
				System.out.println("\n"+file.getName()+"\t"+tempRecords.length);

				for (RecordArnot2006 recordArnot2006:tempRecords) {

					if(propertyName.toLowerCase().contains("bioconcentration factor")) {
						ExperimentalRecord er=recordArnot2006.toExperimentalRecordBCF(propertyName, htSpecies);
						if(er!=null)						
							recordsExperimental.add(er);
					} else if(propertyName.toLowerCase().contains("bioaccumulation factor")) {
						ExperimentalRecord er=recordArnot2006.toExperimentalRecordBCF(propertyName, htSpecies);
						if(er!=null)						
							recordsExperimental.add(er);
					}
					
//					if(er!=null)						
//						recordsExperimental.add(er);

				}
			}
			
//			System.out.println("Count failing duration calc:\t"+RecordArnot2006.countDurationNotOK);
//			System.out.println("Count passing duration calc:\t"+RecordArnot2006.countDurationOK);

			Hashtable<String,ExperimentalRecords> htER = recordsExperimental.createExpRecordHashtableByCAS(ExperimentalConstants.str_L_KG,true);
			
			boolean omitSingleton=true;
			ExperimentalRecords.calculateAvgStdDevOverAllChemicals(htER, true,omitSingleton);

//			Hashtable<String,Double>htMedian=ExperimentalRecords.calculateMedian(htER, true);


		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}
	
//	void getBCFExperimentalRecordsFish(String toxvalVersion,String propertyName) {
//		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
//		ExperimentalRecords experimentalRecords=new ExperimentalRecords();
//		Hashtable<String,ExperimentalRecords>htSid=experimentalRecords.createExpRecordHashtableBySID(null);
//		
//		ExperimentalRecords.calculateStdDev(htSid, true);
//
//		System.out.println("experimentalRecords.size()="+experimentalRecords.size());
//		System.out.println("experimentalRecords unique sids)="+htSid.size());
//
//		String mainFolder = "Data" + File.separator + "Experimental" + File.separator + "Arnot 2006";
//		
//		mainFolder+=File.separator+propertyName;
//		new File(mainFolder).mkdirs();
//		
//		String fileNameJsonExperimentalRecords = "Arnot 2006"+" Experimental Records.json";
//		String fileNameJsonExperimentalRecordsBad = "Arnot 2006"+" Experimental Records-Bad.json";
//		experimentalRecords.toExcel_File_Split(mainFolder+File.separator+fileNameJsonExperimentalRecords.replace("json", "xlsx"),100000);
//		
//		ExperimentalRecords experimentalRecordsBad = experimentalRecords.dumpBadRecords();
//		
//		JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(experimentalRecords),mainFolder+File.separator+fileNameJsonExperimentalRecords);
//		JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(experimentalRecordsBad),mainFolder+File.separator+fileNameJsonExperimentalRecordsBad);
//	
//	}
	
	
	public static void main(String[] args) {
		
//		List<String>propertyNames=Arrays.asList(ExperimentalConstants.strBCF,
//				ExperimentalConstants.strFishBCF,
//				ExperimentalConstants.strFishBCFWholeBody,
//				ExperimentalConstants.strFishBCFWholeBody+"_OverallScore_1_or_2",
//				ExperimentalConstants.strFishBCFWholeBody+"_OverallScore_1");		

		List<String>propertyNames=Arrays.asList(
				ExperimentalConstants.strBCF,
				ExperimentalConstants.strFishBCF,
				ExperimentalConstants.strFishBCFWholeBody,
				ExperimentalConstants.strBAF,
				ExperimentalConstants.strFishBAF,
				ExperimentalConstants.strFishBAFWholeBody
				);

		for (String propertyName:propertyNames) {
			
			System.out.println("\n**********************\n"+propertyName);
			
			ParseArnot2006 p = new ParseArnot2006(propertyName);
			p.generateOriginalJSONRecords=true;
			p.writeCheckingExcelFile=false;
			p.removeDuplicates=false;

//			p.writeJsonExperimentalRecordsFile=true;
//			p.writeExcelExperimentalRecordsFile=true;
//			p.writeExcelFileByProperty=true;		
			p.createFiles();
		}
	}
}
