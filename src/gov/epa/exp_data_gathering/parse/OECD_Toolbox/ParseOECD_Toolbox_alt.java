package gov.epa.exp_data_gathering.parse.OECD_Toolbox;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;


public class ParseOECD_Toolbox_alt extends Parse {
	String recordTypeToParse;
	
	public ParseOECD_Toolbox_alt(String recordTypeToParse) {
		this.recordTypeToParse=recordTypeToParse;
		sourceName = ExperimentalConstants.strSourceOECD_Toolbox;
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
		String folder="Data\\experimental\\"+sourceName+"\\";
		// String filename="Data matrix_1_8_19__15_52_25.xlsx";
		String filename="Data matrix Skin Irritation.xlsx";
		Vector<RecordOECD_Toolbox_alt> records = RecordOECD_Toolbox_alt.parseExcel2(folder+filename);
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordOECD_Toolbox_alt> recordsOECD_Toolbox_alt = new ArrayList<RecordOECD_Toolbox_alt>();
			RecordOECD_Toolbox_alt[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordOECD_Toolbox_alt[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsOECD_Toolbox_alt.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordOECD_Toolbox_alt[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsOECD_Toolbox_alt.add(tempRecords[i]);
					}
				}
			}
			
			Iterator<RecordOECD_Toolbox_alt> it = recordsOECD_Toolbox_alt.iterator();
			while (it.hasNext()) {
				RecordOECD_Toolbox_alt r = it.next();
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
	 * TODO- add code for other data types in this source
	 * 
	 * @param recOT
	 * @param records
	 */
	private void addExperimentalRecords(RecordOECD_Toolbox_alt recOT, ExperimentalRecords records) {

		ExperimentalRecord er=new ExperimentalRecord();
		
		er.source_name=sourceName;		
		er.chemical_name=recOT.chemical_name;
		er.casrn=recOT.CAS;
		er.smiles=recOT.smiles;
		// er.property_name=ExperimentalConstants.strSkinSensitizationLLNA;
		er.property_name="Skin irritation";
		er.original_source_name=recOT.reference;
		
		er.property_value_string=recOT.PII;
		/*		
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
		*/
		records.add(er);
	}
	
	public static void main(String[] args) {
		ParseOECD_Toolbox p = new ParseOECD_Toolbox("tox");
		p.createFiles();
	}

}
