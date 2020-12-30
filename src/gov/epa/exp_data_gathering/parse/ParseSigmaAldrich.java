package gov.epa.exp_data_gathering.parse;

import gov.epa.api.ExperimentalConstants;

public class ParseSigmaAldrich extends ParseDownloader {

	public ParseSigmaAldrich() {
		sourceName = ExperimentalConstants.strSourceSigmaAldrich;
		this.init();
	}
	
}
