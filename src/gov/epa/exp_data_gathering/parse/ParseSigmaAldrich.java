package gov.epa.exp_data_gathering.parse;

import gov.epa.api.ExperimentalConstants;

public class ParseSigmaAldrich extends Parse {

	public ParseSigmaAldrich() {
		sourceName = ExperimentalConstants.strSourceSigmaAldrich;
		this.init();
	}
	
}
