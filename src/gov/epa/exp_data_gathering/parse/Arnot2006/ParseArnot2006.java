package gov.epa.exp_data_gathering.parse.Arnot2006;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

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
	
	String propertyName=ExperimentalConstants.strBCF;
	
	
	public ParseArnot2006() {
		sourceName = RecordArnot2006.sourceName; 
		this.init();
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
			
			System.out.println(htSpecies.get("bluegill sunfish").size());
			
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

					if(propertyName.equals(ExperimentalConstants.strBCF)) {
						ExperimentalRecord er=recordArnot2006.toExperimentalRecordBCF(htSpecies);	
						
						if(er!=null)						
							recordsExperimental.add(er);
					}
				}
			}
			

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
		ParseArnot2006 p = new ParseArnot2006();
		
		p.generateOriginalJSONRecords=false;
		p.removeDuplicates=true;
		
		p.writeJsonExperimentalRecordsFile=true;
		p.writeExcelExperimentalRecordsFile=true;
		p.writeExcelFileByProperty=true;		
		p.writeCheckingExcelFile=false;		
		p.createFiles();


	}
}
