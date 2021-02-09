package gov.epa.exp_data_gathering.parse.OECD_Toolbox;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;


public class ParseOECD_Toolbox extends Parse {

	public ParseOECD_Toolbox() {
		sourceName = ExperimentalConstants.strSourceOECD_Toolbox;
		removeDuplicates=false;
		this.init();
	}
	
	@Override
	protected void createRecords() {
		String folder="data\\experimental\\"+sourceName+"\\";
		String filename="Data matrix_1_8_19__15_52_25.xlsx";
		Vector<RecordOECD_Toolbox> records = RecordOECD_Toolbox.parseExcel2(folder+filename);
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordOECD_Toolbox[] records = gson.fromJson(new FileReader(jsonFile), RecordOECD_Toolbox[].class);
			
			for (int i = 0; i < records.length; i++) {
				RecordOECD_Toolbox rec = records[i];
				addExperimentalRecords(rec,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return recordsExperimental;
	}

	/**
	 * This method adds binary LLNA records
	 * 
	 * TODO- add code for other data types in this source
	 * 
	 * @param recOT
	 * @param records
	 */
	private void addExperimentalRecords(RecordOECD_Toolbox recOT, ExperimentalRecords records) {

		ExperimentalRecord er=new ExperimentalRecord();
		
		er.source_name=sourceName;		
		er.chemical_name=recOT.chemical_name;
		er.casrn=recOT.CAS;
		er.smiles=recOT.smiles;
		er.property_name=ExperimentalConstants.strSkinSensitizationLLNA;
		er.original_source_name=recOT.reference;
		
		er.property_value_string=recOT.EC3+" "+recOT.EC3_Units;
				
		if (recOT.EC3.contentEquals("Negative")) {
			er.property_value_point_estimate_final=Double.valueOf(0);
			er.property_value_units_final="binary";
		} else if (recOT.EC3.contentEquals("Strongly positive") || recOT.EC3.contentEquals("Weakly positive")) {
			er.property_value_point_estimate_final=Double.valueOf(1);
			er.property_value_units_final="binary";
		} else {
			try {
				double EC3=Double.parseDouble(recOT.EC3);							
				er.property_value_point_estimate_final=Double.valueOf(1);
				er.property_value_units_final="binary";
				
			}  catch (Exception ex) {				
				er.keep=false;
				er.reason="Ambiguous value";
				System.out.println(recOT.CAS+"\t"+recOT.EC3+"\tAmbiguous");				
			}
		}
		
		records.add(er);
	}
	
	public static void main(String[] args) {
		ParseOECD_Toolbox p = new ParseOECD_Toolbox();
		p.createFiles();
	}

}
