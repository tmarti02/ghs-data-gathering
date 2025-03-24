package gov.epa.exp_data_gathering.parse.NITE;


import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;

/**
* @author TMARTI02
*/
public class RecordNITE {
	public static String sourceName=ExperimentalConstants.sourceNITE_OPPT;
	
	private static final String fileName = "BIOWIN Update Training Validation Chemicals.xlsx";
	private static final String sheetName="TrainValidationBiowin4.11-TMM";
	private static final String mainFolderPath="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental";
	
	String smiles;
	String casrn;
	String chemical_name;
	String set;
	int rbiodeg;

	static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();

	
	public static List<RecordNITE> parseRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, mainFolderPath, sourceName, sheetName);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(1); // TODO Chemical name index guessed from header. Is this accurate?
		
		List<String>fields=Arrays.asList("","CAS","RBIODEG","Set","CASRN","Name");
		
		List<RecordNITE> records2=new ArrayList<>();
		
		for (JsonObject jo:records) {
			
			System.out.println(gson.toJson(jo));
			
			RecordNITE r=new RecordNITE();
			records2.add(r);
			
			r.smiles=jo.get("SMILES_Notation_Structure").getAsString();
			r.casrn=jo.get("CAS").getAsString();
			r.rbiodeg=(int)Double.parseDouble(jo.get("RBIODEG").getAsString());
			r.set=jo.get("Set").getAsString();;
			r.chemical_name=jo.get("Name").getAsString();
			
//			System.out.println(gson.toJson(r));
		}
		
		
		
		return records2;
	}


	public static void main(String[] args) {


		List<RecordNITE> recordsNITE=parseRecordsFromExcel();
//		System.out.println(gson.toJson(recordsNITE));

	}


	public ExperimentalRecord toExperimentalRecord() {

		ExperimentalRecord er=new ExperimentalRecord();
		
		er.chemical_name=chemical_name;
		er.casrn=casrn;
		er.smiles=smiles;
		er.property_value_point_estimate_original=(double)rbiodeg;
		er.property_value_point_estimate_final=(double)rbiodeg;
		er.property_value_string=rbiodeg+" "+ExperimentalConstants.str_binary;
		er.experimental_parameters=new Hashtable<>();
		er.experimental_parameters.put("set", set);
		
		er.property_value_units_original=ExperimentalConstants.str_binary;
		er.property_value_units_final=ExperimentalConstants.str_binary;
		er.source_name=sourceName;
		er.property_name=ExperimentalConstants.strRBIODEG;
		er.keep=true;
			

		
		return er;	
	}

}
