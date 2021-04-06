package gov.epa.exp_data_gathering.parse.ICF;

import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;

public class ParseICF extends Parse {

	public ParseICF() {
		sourceName = "ICF"; // TODO Consider creating ExperimentalConstants.strSourceICF instead.
		this.init();
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordICF.parseICFRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

//	@Override
//	protected ExperimentalRecords goThroughOriginalRecords() {
//		// TODO
//	}
	
	public static void main(String[] args) {
		ParseICF p = new ParseICF();
		p.createRecords();
	}
}