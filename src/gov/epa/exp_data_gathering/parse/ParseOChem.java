package gov.epa.exp_data_gathering.parse;

import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

public class ParseOChem extends Parse {

	public ParseOChem() {
		sourceName = ExperimentalConstants.strSourceOChem;
		this.init();
	}
	
	@Override
	protected void createRecords() {
		Vector<RecordOChem> records = RecordOChem.parseOChemQueriesFromExcel();
		writeOriginalRecordsToFile(records);
	}
	
	public static void main(String[] args) {
		ParseOChem p = new ParseOChem();
		p.createRecords();
	}
}
