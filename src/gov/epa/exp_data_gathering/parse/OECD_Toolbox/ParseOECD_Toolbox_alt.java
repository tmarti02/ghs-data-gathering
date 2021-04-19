package gov.epa.exp_data_gathering.parse.OECD_Toolbox;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
		Date date = new Date();  
		String strDate=formatter.format(date);
		String dayOnly = strDate.substring(0,strDate.indexOf(" "));		

		ExperimentalRecord er=new ExperimentalRecord();
		er.date_accessed= dayOnly;
		er.source_name=sourceName;		
		er.chemical_name=recOT.chemical_name;
		er.casrn=recOT.CAS;
		er.smiles=recOT.smiles;
		er.property_name=ExperimentalConstants.strSkinIrritationPII;
		er.original_source_name=recOT.reference;		
		er.property_value_string=recOT.PII;
		er.property_value_units_final= ExperimentalConstants.str_pii;
		// handles the numeric qualifiers and then assigns a value to property value point estimate final.
		Pattern digitpattern = Pattern.compile("\\d");
		Matcher matcher = digitpattern.matcher(recOT.PII);
		if(matcher.find()) {
			int index = matcher.start();
			er.property_value_numeric_qualifier = ParseUtilities.getNumericQualifier(recOT.PII, index);
		}
		Pattern numericpat = Pattern.compile("[0-9]*\\.?[0-9]+");
		Matcher numbermatcher = numericpat.matcher(recOT.PII);
		if(numbermatcher.find()) {
			String number_alone_string = numbermatcher.group(0);
			er.property_value_point_estimate_final = Double.parseDouble(number_alone_string);
		}
		if (recOT.species != null && (!(recOT.species.equals("Test organisms (species): Rabbit")))) {
			er.keep= false;
			er.reason="not a rabbit test";
			if (recOT.species != null)
				er.note = recOT.species;
		}

		records.add(er);
	}
	
	public static void main(String[] args) {
		ParseOECD_Toolbox_alt p = new ParseOECD_Toolbox_alt("tox");
		p.createFiles();
		

	}

}
