package gov.epa.exp_data_gathering.parse.QSAR_ToolBox;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import gov.epa.QSAR.utilities.JsonUtilities;
import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.QSAR_ToolBox.RecordQSAR_ToolBox.Species;

public class ParseQSAR_ToolBox extends Parse {
	
	String propertyName;

	public static String fileNameAcuteToxicityDB="acute oral toxicity db.xlsx";
	public static String fileNameAcuteToxicityEchaReach="echa reach acute toxicity by test material.xlsx";
	public static String fileNameSensitizationEchaReach="echa reach sensitization by test material.xlsx";
	public static String fileNameSensitization="skin sensitization.xlsx";
	public static String fileNameBCFCanada="Bioaccumulation Canada.xlsx";
	public static String fileNameBCFCEFIC="Bioaccumulation Fish CEFIC LRI.xlsx";
	public static String fileNameBCFNITE="Bioconcentration and LogKow NITE v2.xlsx";
	
//	static String fileName=fileNameAcuteToxicityEchaReach;
//	static String fileName=fileNameAcuteToxicityDB;
//	static String fileName=fileNameSensitizationEchaReach;
//	static public String fileName=fileNameSensitization;
//	static String fileName=fileNameBCFCEFIC;
//	static String fileName=fileNameBCFCEFIC;
//	static String fileName=fileNameBCFCanada;
	static String fileName=fileNameBCFNITE;
	
	
	String original_source_name;
	List<String>selectedEndpoints;
	
	public ParseQSAR_ToolBox(String propertyName) {
		this.propertyName=propertyName;
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
			
			Type type = new TypeToken<Hashtable<String, List<RecordQSAR_ToolBox.Species>>>(){}.getType();
			Hashtable<String, List<Species>>htSpecies=JsonUtilities.gsonPretty.fromJson(new FileReader("data\\experimental\\Arnot 2006\\htSuperCategory.json"), type);

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
					
					//Can only filter by whole body if filename is CEFIC
					if(fileName.equals(fileNameBCFCEFIC)) {
						
						ExperimentalRecord erKinetic=recordQSAR_ToolBox.toExperimentalRecordBCF_NITE_Kinetic(propertyName, htSpecies);
						if(erKinetic!=null)	recordsExperimental.add(erKinetic);
		
						ExperimentalRecord erSS=recordQSAR_ToolBox.toExperimentalRecordBCF_NITE_SS(propertyName, htSpecies);
						if(erSS!=null)	recordsExperimental.add(erSS);
						
		
					} else if(fileName.equals(fileNameBCFCanada)) {
						ExperimentalRecord erCanada=recordQSAR_ToolBox.toExperimentalRecordBCFCanada(propertyName);
						if(erCanada!=null)	recordsExperimental.add(erCanada);
					} else if(fileName.equals(fileNameBCFNITE)) {
						ExperimentalRecord erNITE=recordQSAR_ToolBox.toExperimentalRecordBCFNITE(propertyName, htSpecies);
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

		
		Hashtable<String,ExperimentalRecords> htER = recordsExperimental.createExpRecordHashtableByCAS(ExperimentalConstants.str_L_KG);
		ExperimentalRecords.calculateAvgStdDevOverAllChemicals(htER, true);

		
		return recordsExperimental;
	}
	
	static void runBCF() {
		
		fileName=fileNameBCFNITE;

		List<String>properties=new ArrayList<>();
		properties.add(ExperimentalConstants.strBCF);
		properties.add(ExperimentalConstants.strFishBCF);
		if(!fileName.equals(fileNameBCFCanada))properties.add(ExperimentalConstants.strFishBCFWholeBody);
		
		for (String propertyName:properties) {
			ParseQSAR_ToolBox p = new ParseQSAR_ToolBox(propertyName);
			p.generateOriginalJSONRecords=true;
			p.removeDuplicates=true;
			p.writeJsonExperimentalRecordsFile=true;
			p.writeExcelExperimentalRecordsFile=true;
			p.writeExcelFileByProperty=true;		
			p.writeCheckingExcelFile=false;//creates random sample spreadsheet
			p.createFiles();
		}
		
	}
	
	public static void main(String[] args) {
		
		runBCF();

//******************************************************************************
//		fileName=fileNameAcuteToxicityDB;
//		ParseQSAR_ToolBox p=new ParseQSAR_ToolBox(null);
//		p.generateOriginalJSONRecords=true;
//		p.removeDuplicates=true;
//		p.writeJsonExperimentalRecordsFile=true;
//		p.writeExcelExperimentalRecordsFile=true;
//		p.writeExcelFileByProperty=true;		
//		p.writeCheckingExcelFile=false;//creates random sample spreadsheet
//		p.createFiles();

		
	}
}