package gov.epa.exp_data_gathering.parse.NIEHS_ICE_2024_08;

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
import gov.epa.exp_data_gathering.parse.QSAR_ToolBox.RecordQSAR_ToolBox;

public class ParseNIEHS_ICE_2024_08 extends Parse {

	String fileNameAcuteOral="acute_oral.xlsx";
	String fileNameSensitization="skin_sensitization.xlsx";
	
//	String fileName=fileNameAcuteOral;
	String fileName=fileNameSensitization;
	String sheetName;
	
	
	public ParseNIEHS_ICE_2024_08() {
		
		sourceName = "NIEHS_ICE_2024_08"; // TODO Consider creating ExperimentalConstants.strSourceNIEHS_ICE_2024_08 instead.
				
		if(fileName.equals(fileNameAcuteOral)) {
			init("acute oral");
			sheetName="Data";
		} else if (fileName.equals(fileNameSensitization)) {
			init("skin sensitization");
			sheetName="Data_invivo";
		} 
		
	}

	@Override
	protected void createRecords() {
		if(generateOriginalJSONRecords) {
			Vector<JsonObject> records = RecordNIEHS_ICE_2024_08.parseNIEHS_ICE_2024_08RecordsFromExcel(fileName,sheetName);
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
				
				RecordNIEHS_ICE_2024_08[] tempRecords = gson.fromJson(new FileReader(file), RecordNIEHS_ICE_2024_08[].class);
				
				System.out.println("\n"+file.getName()+"\t"+tempRecords.length);

				for (RecordNIEHS_ICE_2024_08 recordNIEHS:tempRecords) {
					ExperimentalRecord er=recordNIEHS.toExperimentalRecord();
					recordsExperimental.add(er);

				}
			}
			

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}



	public static void main(String[] args) {
		ParseNIEHS_ICE_2024_08 p = new ParseNIEHS_ICE_2024_08();
		
		p.generateOriginalJSONRecords=true;
		
		p.removeDuplicates=false;
		
		p.writeJsonExperimentalRecordsFile=true;
		p.writeExcelExperimentalRecordsFile=true;
		p.writeExcelFileByProperty=true;		
		p.writeCheckingExcelFile=false;//creates random sample spreadsheet
		p.createFiles();
		
	}
}