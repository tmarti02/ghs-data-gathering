package gov.epa.ghs_data_gathering.Parse.Links;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.Parse;


public class ParseLink extends Parse {

	ParseLink() {
		sourceName = RecordLink.sourceName; 
		this.init();
	}
	
	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordLink.parseRecordsFromExcel();
		writeOriginalRecordsToFile(records);
		toFlatFile(mainFolder+File.separator+sourceName+".txt", "|", records);
	}
	
	
	public void toFlatFile(String filepath,String del,Vector<JsonObject> records) {
		
		try {
			FileWriter fw=new FileWriter(filepath);
			fw.write(RecordLink.getHeader(del)+"\r\n");
			Gson gson=new Gson();
			
			RecordLink[] recs=gson.fromJson(records.toString(), RecordLink[].class);
			
			for (RecordLink rec:recs) {
				fw.write(rec.toString(del)+"\r\n");
			}
			fw.flush();
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParseLink p = new ParseLink();
		
		p.createRecords();
	}
	
}
