package gov.epa.exp_data_gathering.parse.NICEATM;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.UnitConverter;

public class RecordNICEATM {
	public String Chemical_Name;
	public String CASRN;
	public String Molecular_Weight_g_mol;
	public String Chemical_Class;
	public String LLNA_Vehicle;
	
	public String One_Conc;
	public String One_SI;
	public String Two_Conc;
	public String Two_SI;
	public String Three_Conc;
	public String Three_SI;
	public String Four_Conc;
	public String Four_SI;
	public String Five_Conc;
	public String Five_SI;
	public String Six_Conc;
	public String Six_SI;
	public String Seven_Conc;
	public String Seven_SI;
	public String Eight_Conc;
	public String Eight_SI;
	
	public String EC3;
	public String LLNA_Result;
	public String Brief_Citation;
	public String Citation;
	public static final String[] fieldNames = {"Chemical_Name","CASRN","Molecular_Weight_g_mol","Chemical_Class","LLNA_Vehicle","1_Conc","1_SI","2_Conc","2_SI","3_Conc","3_SI","4_Conc","4_SI","5_Conc","5_SI","6_Conc","6_SI","7_Conc","7_SI","8_Conc","8_SI","EC3","LLNA_Result","Brief_Citation","Citation"};

	public static final String lastUpdated = "12/23/2013";
	public static final String sourceName = "NICEATM"; // TODO Consider creating ExperimentalConstants.strSourceNICEATM instead.

	private static final String fileName = "niceatm-llnadatabase-23dec2013.xlsx";

	private static final transient UnitConverter unitConverter = new UnitConverter("data/density.txt");

	public static Vector<JsonObject> parseNICEATMRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		esr.headerRowNum=2;
		Vector<JsonObject> records = esr.parseRecordsFromExcel(0,true); // TODO Chemical name index guessed from header. Is this accurate?
		return records;
	}

	public ExperimentalRecord toExperimentalRecord() {
		ExperimentalRecord er=new ExperimentalRecord();
		
		if(this.CASRN!=null && !this.CASRN.contentEquals("NA")) {
			er.casrn=this.CASRN;	
		}
		
		er.experimental_parameters = new Hashtable<>();
		er.experimental_parameters.put("LLNA Vehicle", this.LLNA_Vehicle);
		er.experimental_parameters.put("EC3 Value", this.EC3);
		
		if(er.keep) {
			er.property_value_units_final=ExperimentalConstants.str_binary;
			er.property_value_units_original=ExperimentalConstants.str_binary;
		}
		double nonSensitizing = 0;
		double sensitizing = 1;
		er.property_value_string=this.LLNA_Result;		
		if(this.LLNA_Result.equals("POS")) {
			er.property_value_point_estimate_final=sensitizing;
		} else if(this.LLNA_Result.equals("NEG")) {
			er.property_value_point_estimate_final=nonSensitizing;
		}
		
		er.literatureSource.name=this.Brief_Citation;
		er.literatureSource.citation=this.Citation;
		
		return er;
	}

}