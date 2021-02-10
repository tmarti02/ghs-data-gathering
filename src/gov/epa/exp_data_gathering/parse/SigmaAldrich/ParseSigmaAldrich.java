package gov.epa.exp_data_gathering.parse.SigmaAldrich;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.Parse;

public class ParseSigmaAldrich extends Parse {

	public ParseSigmaAldrich() {
		sourceName = ExperimentalConstants.strSourceSigmaAldrich;
		this.init();
	}
	
}
