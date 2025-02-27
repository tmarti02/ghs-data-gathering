package gov.epa.exp_data_gathering.parse.Fu2008;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;


/**
 * Stores data from Fu 2009, accessible at: doi.org/10.1897/08-233.1
 *
 */
public class ParseFu2008 extends Parse {
	
	public String fileName="Fu 2008.xlsx";
	String original_source_name;
	
	public ParseFu2008() {
		sourceName = RecordFu2008.sourceName;
		removeDuplicates=true;
		original_source_name="Fu 2008";
		this.init("Fu 2008");
	}
	
	@Override
	protected void createRecords() {
		if(generateOriginalJSONRecords) {
			Vector<JsonObject> records = RecordFu2008.parseFu2008RecordsFromExcel(fileName, sourceName);
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
				
				RecordFu2008[] tempRecords = gson.fromJson(new FileReader(file), RecordFu2008[].class);
				
				System.out.println("\n"+file.getName()+"\t"+tempRecords.length);

				for (RecordFu2008 recordFu2008:tempRecords) {

					ExperimentalRecord er=recordFu2008.toExperimentalRecords();	
					
					if(er!=null)						
						recordsExperimental.add(er);

				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}
	public static void main(String[] args) {
		ParseFu2008 p = new ParseFu2008();
		
		p.generateOriginalJSONRecords=false;
		p.removeDuplicates=false;
		
		p.writeJsonExperimentalRecordsFile=true;
		p.writeExcelExperimentalRecordsFile=true;		
		p.writeCheckingExcelFile=false;//creates random sample spreadsheet
		p.createFiles();
	}
}
