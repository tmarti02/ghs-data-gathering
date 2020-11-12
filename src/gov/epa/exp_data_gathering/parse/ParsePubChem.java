package gov.epa.exp_data_gathering.parse;

import gov.epa.api.ExperimentalConstants;

public class ParsePubChem extends Parse {
	
	public ParsePubChem() {
		sourceName = ExperimentalConstants.strSourcePubChem;
		this.init();
	}
	
	public static void main(String[] args) {
		// TODO
	}
}
