package gov.epa.exp_data_gathering.parse.NICEATM;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;

public class ParseNICEATM extends Parse {

	String recordTypeToParse;
	
	public ParseNICEATM(String recordTypeToParse) {
		sourceName = ExperimentalConstants.strSourceNICEATM;
		this.recordTypeToParse=recordTypeToParse;
		removeDuplicates=false;
		this.init();
		
		String toxNote = recordTypeToParse.toLowerCase().contains("tox") ? " Toxicity" : "";
				
		fileNameJSON_Records = sourceName +toxNote + " Original Records.json";
		fileNameFlatExperimentalRecords = sourceName +toxNote + " Experimental Records.txt";
		fileNameFlatExperimentalRecordsBad = sourceName +toxNote + " Experimental Records-Bad.txt";
		fileNameJsonExperimentalRecords = sourceName +toxNote + " Experimental Records.json";
		fileNameJsonExperimentalRecordsBad = sourceName +toxNote + " Experimental Records-Bad.json";
		fileNameExcelExperimentalRecords = sourceName +toxNote + " Experimental Records.xlsx";
						
	}
	
	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordNICEATM.parseNICEATMRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordNICEATM> recordsNICEATM = new ArrayList<RecordNICEATM>();
			RecordNICEATM[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordNICEATM[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsNICEATM.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordNICEATM[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsNICEATM.add(tempRecords[i]);
					}
				}
			}
			
			Iterator<RecordNICEATM> it = recordsNICEATM.iterator();
			while (it.hasNext()) {
				RecordNICEATM r = it.next();
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}

	/**
	 * This method adds binary LLNA records
	 * 
	 * @param recN
	 * @param records
	 */
	private void addExperimentalRecords(RecordNICEATM recN, ExperimentalRecords records) {

		ExperimentalRecord er=new ExperimentalRecord();
		
		er.source_name=sourceName;		
		er.chemical_name=recN.Compound_name;
		er.casrn=recN.CASRN;
		er.smiles=recN.SMILES;
		er.property_name=ExperimentalConstants.strSkinSensitizationLLNA;
				
		er.property_value_string=recN.EC3_;
				
		if (recN.EC3_.contentEquals("NC")) {
			er.property_value_point_estimate_final=Double.valueOf(0);
			er.property_value_units_final="binary";
		} else if (recN.EC3_.contains(">") || recN.EC3_.isEmpty() || recN.EC3_.contentEquals("IDR")) {
			er.keep=false;
			er.reason="Ambiguous value";	
		} else {
			try {
				double EC3=Double.parseDouble(recN.EC3_);							
				er.property_value_point_estimate_final=Double.valueOf(1);
				er.property_value_units_final="binary";
				er.property_value_string+=" %";
				
			}  catch (Exception ex) {				
				er.keep=false;
				er.reason="Ambiguous value";
				System.out.println(er.casrn+"\t"+recN.EC3_+"\tAmbiguous");				
			}
		}
		
		records.add(er);
	}
	
	public static void main(String[] args) {
		ParseNICEATM p = new ParseNICEATM("tox");
		p.createFiles();
	}
}
