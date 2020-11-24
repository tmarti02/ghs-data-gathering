package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;
import gov.epa.api.ExperimentalConstants;


public class ParseChemicalBook extends Parse {
	
	public ParseChemicalBook() {
		sourceName = "ChemicalBook";
		this.init();
	}
}

// test

/**
 * Reads the JSON file created by createRecords() and translates it to an ExperimentalRecords object
 */
@Override
protected ExperimentalRecords goThroughOriginalRecords() {
	ExperimentalRecords recordsExperimental=new ExperimentalRecords();
	
	try {
		File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
		
		RecordLookChem[] recordsLookChem = gson.fromJson(new FileReader(jsonFile), RecordLookChem[].class);
		
		for (int i = 0; i < recordsLookChem.length; i++) {
			RecordLookChem r = recordsLookChem[i];
			addExperimentalRecords(r,recordsExperimental);
		}
	} catch (Exception ex) {
		ex.printStackTrace();
	}
	
	return recordsExperimental;
}

}