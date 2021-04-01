package gov.epa.exp_data_gathering.parse.Bagley;

import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;

public class ParseBagley extends Parse {

	public ParseBagley() {
		sourceName = "Bagley"; // TODO Consider creating ExperimentalConstants.strSourceBagley instead.
		this.init();

		// TODO Is this a toxicity source? If so, rename original and experimental records files here.
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordBagley.parseBagleyRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

//	@Override
//	protected ExperimentalRecords goThroughOriginalRecords() {
//		// TODO
//	}
	
	public static void main(String[] args) {
		// TODO
	}
}
