package gov.epa.exp_data_gathering.parse.Bagley;

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
import gov.epa.exp_data_gathering.parse.ToxicityDictionary.DictionarySkinCorrosionIrritation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;

public class ParseBagley extends Parse {

	public ParseBagley() {
		sourceName = ExperimentalConstants.strSourceBagley; 
		this.init();
		
		fileNameJSON_Records = sourceName +" Toxicity Original Records.json";
		fileNameFlatExperimentalRecords = sourceName +" Toxicity Experimental Records.txt";
		fileNameFlatExperimentalRecordsBad = sourceName +" Toxicity Experimental Records-Bad.txt";
		fileNameJsonExperimentalRecords = sourceName +" Toxicity Experimental Records.json";
		fileNameJsonExperimentalRecordsBad = sourceName +" Toxicity Experimental Records-Bad.json";
		fileNameExcelExperimentalRecords = sourceName +" Toxicity Experimental Records.xlsx";


	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordBagley.parseBagleyRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			List<RecordBagley> recordsBagley = new ArrayList<RecordBagley>();
			RecordBagley[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordBagley[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsBagley.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordBagley[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsBagley.add(tempRecords[i]);
					}
				}
			}

			Iterator<RecordBagley> it = recordsBagley.iterator();
			while (it.hasNext()) {
				RecordBagley r = it.next();
				addExperimentalRecord(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}
	
	
private void addExperimentalRecord(RecordBagley r, ExperimentalRecords recordsExperimental) {
	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
	Date date = new Date();  
	String strDate=formatter.format(date);
	String dayOnly = strDate.substring(0,strDate.indexOf(" "));		

	ExperimentalRecord er=new ExperimentalRecord();
	er.date_accessed=dayOnly;
	er.source_name=sourceName;
	er.property_name=ExperimentalConstants.strSkinIrritationPII;
	er.property_value_units_final=ExperimentalConstants.str_pii;
	er.chemical_name=r.Chemical;
	er.property_value_string=r.PII;
	er.note = "Purity: " + r.Purity;
	er.original_source_name="Bagley DM, Gardner JR, Holland G, Lewis RW, Regnier JF, Stringer DA, Walker AP. Skin irritation: Reference chemicals data bank. Toxicol In Vitro. 1996 Feb;10(1):1-6. doi: 10.1016/0887-2333(95)00099-2. PMID: 20650176.";
	// handles the numeric qualifiers and then assigns a value to property value point estimate final.
	Pattern digitpattern = Pattern.compile("\\d");
	Matcher matcher = digitpattern.matcher(r.PII);
	if(matcher.find()) {
		int index = matcher.start();
		er.property_value_numeric_qualifier = ParseUtilities.getNumericQualifier(r.PII, index);
	}
	Pattern numericpat = Pattern.compile("[0-9]*\\.?[0-9]+");
	Matcher numbermatcher = numericpat.matcher(r.PII);
	if(numbermatcher.find()) {
		String number_alone_string = numbermatcher.group(0);
		er.property_value_point_estimate_final = Double.parseDouble(number_alone_string);
	}

	// corrosion commented out for the time being.
	/*
	ExperimentalRecord erCorr = gson.fromJson(gson.toJson(er), ExperimentalRecord.class);
	erCorr.property_name = "rabbit_" + ExperimentalConstants.strSkinCorrosion;
	erCorr.property_value_units_final="binary";
	erCorr.property_value_point_estimate_original = convertPIIToBinaryCorrosion(er.property_value_point_estimate_final);
	erCorr.property_value_numeric_qualifier = "";
	erCorr.property_value_point_estimate_final = erCorr.property_value_point_estimate_original;
	if (erCorr.property_value_point_estimate_final==-1) {
		erCorr.keep=false;
		erCorr.reason="Not a corrosion record";
	}
	*/
	
	ExperimentalRecord erIrr = gson.fromJson(gson.toJson(er), ExperimentalRecord.class);
	erIrr.property_name = "rabbit_" + ExperimentalConstants.strSkinIrritation;
	erIrr.property_value_units_final="binary";
	erIrr.property_value_point_estimate_original = DictionarySkinCorrosionIrritation.convertPIIToBinaryIrritation(er.property_value_point_estimate_final);
	erIrr.property_value_numeric_qualifier = "";
	erIrr.property_value_point_estimate_final = erIrr.property_value_point_estimate_original;
	if (erIrr.property_value_point_estimate_final==-1) {
		erIrr.keep=false;
		erIrr.reason="Ambiguous skin irritation score";
	}


//	recordsExperimental.add(erCorr);
	recordsExperimental.add(erIrr);

		
	}

/*
	private static double convertPIIToBinaryCorrosion(double propertyValue) {
		double CorrBinary = -1; // inapplicable record to be discarded
		if ((propertyValue >= 2.3) && (propertyValue < 4.0)) {
			CorrBinary = 0;
		} else if ((propertyValue >= 4.0)) {
			CorrBinary = 1;
		}
		return CorrBinary;
	}
*/

	
	public static void main(String[] args) {
		ParseBagley p = new ParseBagley();
		p.createFiles();
	}
}
