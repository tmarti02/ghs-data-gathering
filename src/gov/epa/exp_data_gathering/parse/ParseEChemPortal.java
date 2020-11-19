package gov.epa.exp_data_gathering.parse;

import gov.epa.api.ExperimentalConstants;

public class ParseEChemPortal extends Parse {

	public ParseEChemPortal() {
		sourceName = ExperimentalConstants.strSourceEChem;
		this.init();
	}
	
}
