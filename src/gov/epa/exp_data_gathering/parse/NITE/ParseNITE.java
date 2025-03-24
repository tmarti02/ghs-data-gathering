package gov.epa.exp_data_gathering.parse.NITE;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.JSONUtilities;



/**
 * 
 * Gets records from OPPT's version of NITE data
 * 
* @author TMARTI02
*/
public class ParseNITE {

	Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public void getNITEExperimentalRecords() {

		List<RecordNITE>recordsOriginal=null;
		boolean createOriginalRecords=true;
		
		String source=RecordNITE.sourceName;
		String propertyName=ExperimentalConstants.strRBIODEG;

		String jsonPath = "data/experimental/"+source+File.separator+source+" "+propertyName+" original records.json";

		if (createOriginalRecords) {
			recordsOriginal=RecordNITE.parseRecordsFromExcel();
			JSONUtilities.batchAndWriteJSON(recordsOriginal,jsonPath);
		} else {
			try {
				RecordNITE[]records2 = gson.fromJson(new FileReader(jsonPath), RecordNITE[].class);
				recordsOriginal=Arrays.asList(records2);
				System.out.println(recordsOriginal.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
				
		ExperimentalRecords experimentalRecords=new ExperimentalRecords();

		for (RecordNITE r:recordsOriginal) {
			ExperimentalRecord er=r.toExperimentalRecord();
			experimentalRecords.add(er);
		}

		
		//Writer experimental records to Json file:
		String mainFolder = "Data" + File.separator + "Experimental" + File.separator + source;
		String fileNameJsonExperimentalRecords = source+"_"+propertyName+" Experimental Records.json";
		JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(experimentalRecords),mainFolder+File.separator+fileNameJsonExperimentalRecords);
		
		//Write experimental records to excel file:
		experimentalRecords.toExcel_File_Split(mainFolder+File.separator+fileNameJsonExperimentalRecords.replace("json", "xlsx"),100000);
		
	}
	
	public static void main(String[] args) {
		ParseNITE p=new ParseNITE();
		p.getNITEExperimentalRecords();
	}
}
