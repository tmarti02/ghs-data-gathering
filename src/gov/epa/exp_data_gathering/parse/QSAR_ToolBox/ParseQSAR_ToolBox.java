package gov.epa.exp_data_gathering.parse.QSAR_ToolBox;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;

public class ParseQSAR_ToolBox extends Parse {

	String fileNameAcuteToxicityDB="acute oral toxicity db.xlsx";
	String fileNameAcuteToxicityEchaReach="echa reach acute toxicity by test material.xlsx";
	String fileNameSensitizationEchaReach="echa reach sensitization by test material.xlsx";
	String fileNameSensitization="skin sensitization.xlsx";
	
//	String fileName=fileNameAcuteToxicityEchaReach;
//	String fileName=fileNameAcuteToxicityDB;
//	String fileName=fileNameSensitizationEchaReach;
	String fileName=fileNameSensitization;
	
	String original_source_name;
	List<String>selectedEndpoints;
	
	public ParseQSAR_ToolBox() {
		sourceName = RecordQSAR_ToolBox.sourceName; // TODO Consider creating ExperimentalConstants.strSourceQSAR_ToolBox instead.
		
		if(fileName.equals(fileNameAcuteToxicityEchaReach)) {
			removeDuplicates=true;
			
			original_source_name="ECHA Reach";
			selectedEndpoints = Arrays.asList("Dermal rabbit LD50", "Dermal rat LD50", "Inhalation mouse LC50",
					"Inhalation rat LC50", "Oral mouse LD50", "Oral rat LD50");
			init("Acute toxicity ECHA Reach");
			

		} else if (fileName.equals(fileNameAcuteToxicityDB)) {
			removeDuplicates=true;
			original_source_name="Acute oral toxicity db";
			selectedEndpoints = Arrays.asList("Dermal rabbit LD50", "Dermal rat LD50", "Inhalation mouse LC50",
					"Inhalation rat LC50", "Oral mouse LD50", "Oral rat LD50");
			init("Acute toxicity oral toxicity db");

		} else if (fileName.equals(fileNameSensitizationEchaReach)) {
			removeDuplicates=false;
			original_source_name="ECHA Reach";
			
			selectedEndpoints = Arrays.asList(ExperimentalConstants.strSkinSensitizationLLNA);
			
//			selectedEndpoints = Arrays.asList(ExperimentalConstants.strSkinSensitizationLLNA_EC3,
//					ExperimentalConstants.strSkinSensitizationLLNA_SI);
			
			
			init("Sensitization ECHA Reach");
			
		} else if (fileName.equals(fileNameSensitization)) {
			removeDuplicates=false;
//			original_source_name="ECHA Reach";
			selectedEndpoints = Arrays.asList(ExperimentalConstants.strSkinSensitizationLLNA);
			init("Sensitization");
		}
		
	}

	@Override
	protected void createRecords() {
		if(generateOriginalJSONRecords) {
			Vector<JsonObject> records = RecordQSAR_ToolBox.parseQSAR_ToolBoxRecordsFromExcel(fileName,sourceName);
			writeOriginalRecordsToFile(records);
		}
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			
			File Folder=new File(jsonFolder);
			
			if(Folder.listFiles()==null) {
				System.out.println("No files in json folder:"+jsonFolder);
				return null;
			}
			
			for(File file:Folder.listFiles()) {
				if(!file.getName().contains(".json")) continue;
				if(!file.getName().contains(sourceName+" Original Records")) continue;
				
				RecordQSAR_ToolBox[] tempRecords = gson.fromJson(new FileReader(file), RecordQSAR_ToolBox[].class);
				
				System.out.println("\n"+file.getName()+"\t"+tempRecords.length);

				for (RecordQSAR_ToolBox recordQSAR_ToolBox:tempRecords) {

					ExperimentalRecord er=recordQSAR_ToolBox.toExperimentalRecord(original_source_name);
					
					if(selectedEndpoints.contains(er.property_name))		
						recordsExperimental.add(er);

				}
			}
			

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}


	public static void main(String[] args) {
		ParseQSAR_ToolBox p = new ParseQSAR_ToolBox();
		
		p.generateOriginalJSONRecords=false;
		p.removeDuplicates=false;
		
		p.writeJsonExperimentalRecordsFile=true;
		p.writeExcelExperimentalRecordsFile=true;
		p.writeExcelFileByProperty=true;		
		p.writeCheckingExcelFile=false;//creates random sample spreadsheet
		p.createFiles();
		
	}
}