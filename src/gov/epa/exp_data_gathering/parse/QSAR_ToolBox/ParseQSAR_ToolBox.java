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

	public static String fileNameAcuteToxicityDB="acute oral toxicity db.xlsx";
	public static String fileNameAcuteToxicityEchaReach="echa reach acute toxicity by test material.xlsx";
	public static String fileNameSensitizationEchaReach="echa reach sensitization by test material.xlsx";
	public static String fileNameSensitization="skin sensitization.xlsx";
	public static String fileNameBCFCanada="Bioaccumulation Canada.xlsx";
	public static String fileNameBCFCEFIC="Bioaccumulation Fish CEFIC LRI.xlsx";
	public static String fileNameBCFNITE="Bioconcentration and LogKow NITE.xlsx";
	
//	String fileName=fileNameAcuteToxicityEchaReach;
//	String fileName=fileNameAcuteToxicityDB;
//	String fileName=fileNameSensitizationEchaReach;
//	public String fileName=fileNameSensitization;
//	String fileName=fileNameBCFCEFIC;
//	String fileName=fileNameBCFCanada;
	String fileName=fileNameBCFNITE;
	
	
	String original_source_name;
	List<String>selectedEndpoints;
	
	public ParseQSAR_ToolBox(String propertyName) {
		sourceName = RecordQSAR_ToolBox.sourceName; // TODO Consider creating ExperimentalConstants.strSourceQSAR_ToolBox instead.
		this.init();
//		mainFolder = "Data" + File.separator + "Experimental" + File.separator + sourceName;
//		jsonFolder= mainFolder;
//		new File(mainFolder).mkdirs();
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
		} else if (fileName.equals(fileNameBCFCanada)) {
			removeDuplicates=true;
			original_source_name="Canada";
			selectedEndpoints = Arrays.asList(propertyName);
			mainFolder = "Data" + File.separator + "Experimental" + File.separator + sourceName + File.separator+"BCF Canada";
			mainFolder+=File.separator+propertyName;//output json/excel in subfolder
			jsonFolder= mainFolder;
			new File(mainFolder).mkdirs();
		} else if (fileName.equals(fileNameBCFNITE)) {
			removeDuplicates=true;
			original_source_name="NITE";
			selectedEndpoints = Arrays.asList(propertyName);
			mainFolder = "Data" + File.separator + "Experimental" + File.separator + sourceName + File.separator+"BCF NITE";
			mainFolder+=File.separator+propertyName;//output json/excel in subfolder;
			jsonFolder= mainFolder;
			new File(mainFolder).mkdirs();
		} else if (fileName.equals(fileNameBCFCEFIC)) {
			removeDuplicates=true;
			original_source_name="CEFIC";
			selectedEndpoints = Arrays.asList(propertyName);
			mainFolder = "Data" + File.separator + "Experimental" + File.separator + sourceName + File.separator+"BCF CEFIC";
			mainFolder+=File.separator+propertyName;//output json/excel in subfolder
			jsonFolder= mainFolder;
			new File(mainFolder).mkdirs();
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
					
					//Must change propertyName, limitToFish, limitToWhole Organism to switch between BCF filters. Can only filter if filename is CEFIC
					boolean limitToFish=false;
					boolean limitToWholeOrganism=false;
					String propertyName=ExperimentalConstants.strBCF;
					if(fileName.equals(fileNameBCFCEFIC)) {
						
						ExperimentalRecord erKinetic=recordQSAR_ToolBox.toExperimentalRecordBCF_Kinetic(propertyName, limitToFish, limitToWholeOrganism);
						if(erKinetic!=null)	recordsExperimental.add(erKinetic);
		
						ExperimentalRecord erSS=recordQSAR_ToolBox.toExperimentalRecordBCF_SS(propertyName, limitToFish, limitToWholeOrganism);
						if(erSS!=null)	recordsExperimental.add(erSS);
						
		
					} else if(fileName.equals(fileNameBCFCanada)) {
						ExperimentalRecord erCanada=recordQSAR_ToolBox.toExperimentalRecordBCFCanada(propertyName, limitToFish, limitToWholeOrganism);
						if(erCanada!=null)	recordsExperimental.add(erCanada);
					} else if(fileName.equals(fileNameBCFNITE)) {
						ExperimentalRecord erNITE=recordQSAR_ToolBox.toExperimentalRecordBCFNITE(propertyName, limitToFish, limitToWholeOrganism);
						if(erNITE!=null)	recordsExperimental.add(erNITE);
					} else {
						ExperimentalRecord er=recordQSAR_ToolBox.toExperimentalRecord(original_source_name);
						if(selectedEndpoints.contains(er.property_name))		
							recordsExperimental.add(er);
					}
				}
			}		

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}
	


	public static void main(String[] args) {
		ParseQSAR_ToolBox p = new ParseQSAR_ToolBox(ExperimentalConstants.strBCF);
		
		p.generateOriginalJSONRecords=true;
		p.removeDuplicates=true;
		
		p.writeJsonExperimentalRecordsFile=true;
		p.writeExcelExperimentalRecordsFile=true;
		p.writeExcelFileByProperty=true;		
		p.writeCheckingExcelFile=false;//creates random sample spreadsheet
		p.createFiles();
		
	}
}