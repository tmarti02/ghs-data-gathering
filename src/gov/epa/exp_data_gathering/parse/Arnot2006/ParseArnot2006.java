package gov.epa.exp_data_gathering.parse.Arnot2006;

import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.exp_data_gathering.parse.Parse;

/**
* @author TMARTI02
*/
public class ParseArnot2006 extends Parse {
	
	
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

	
	void getBCFExperimentalRecordsFish(String toxvalVersion,String propertyName) {
		
	}
	
	
	public static void main(String[] args) {
		ParseArnot2006 p = new ParseArnot2006();
		
		p.generateOriginalJSONRecords=false;
		
		p.createFiles();
		

	}
}
