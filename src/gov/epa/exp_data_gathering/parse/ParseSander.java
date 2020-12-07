package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.util.Vector;

public class ParseSander extends Parse{
	
	public ParseSander() {
		sourceName = "Sander";
		this.init();
	}
	@Override
	protected void createRecords() {
		Vector<RecordSander> records = RecordSander.parseWebpagesInDatabase();
		writeOriginalRecordsToFile(records);
	}

	
	public static void main(String[] args) {
		ParseSander p = new ParseSander();
		p.mainFolder = p.mainFolder + File.separator + "General";
		p.databaseFolder = p.mainFolder;
		p.jsonFolder= p.mainFolder;
		p.createFiles();
	}
	

}
