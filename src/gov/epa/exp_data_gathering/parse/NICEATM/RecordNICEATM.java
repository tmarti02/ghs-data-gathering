package gov.epa.exp_data_gathering.parse.NICEATM;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
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
	
	private void setPropertyValues(ExperimentalRecord er) {
		DecimalFormat df=new DecimalFormat("0.00");
		double nonSensitizing = 0;
		double sensitizing = 1;

		if(this.EC3.contains("nmol")) {
			er.keep=false;
			er.reason="Bad units";
			return;
		}

		er.property_value_units_final=ExperimentalConstants.str_binary;

		if(this.EC3.equals("IDR")) {
			er.property_value_units_original=ExperimentalConstants.str_binary;
			er.property_value_string=this.LLNA_Result  + ": " + "EC3=" + this.EC3;

			if(this.LLNA_Result.equals("POS")) {
				er.property_value_qualitative = "Sensitizing";
				er.property_value_point_estimate_final=sensitizing;
				er.updateNote("IDR (EC3 not available but sensitizing");
			}
//			System.out.println("CAS="+er.casrn+"\tVS="+er.property_value_string+"\tPEF="+er.property_value_point_estimate_final);
		} else {

			if(!EC3.equals("NC")) {
				double value = Double.parseDouble(EC3);
				
				if (value < 100) {
					er.property_value_qualitative = "Sensitizing";
					er.property_value_point_estimate_final=sensitizing;
					er.updateNote("0% < EC3 (" + df.format(value) + "%) < 100%");
				} else {//this may not happen
					er.property_value_point_estimate_final=nonSensitizing;
					er.property_value_qualitative = "Not sensitizing";
					er.updateNote("EC3 (" + df.format(value)+ "%) was greater than 100%");
					
					System.out.println("*** EC3>=100\t"+er.casrn);
				}
				
				er.property_value_units_original="%";
				er.property_value_point_estimate_original=value;
				
				try {
					er.property_value_string=LLNA_Result+": EC3="+df.format(value)+"%";
				} catch(Exception ex) {
					System.out.println("error formatting EC3="+EC3);
				}
				
//				System.out.println("CAS="+er.casrn+"\tVS="+er.property_value_string+"\tPEF="+er.property_value_point_estimate_final+"\tUO="+er.property_value_units_original);

			} else {
				
				er.property_value_units_original=ExperimentalConstants.str_binary;
				er.property_value_string=this.LLNA_Result  + ": " + "EC3=" + this.EC3;

				if(this.LLNA_Result.equals("NEG")) {
					er.property_value_qualitative = "Not sensitizing";
					er.property_value_point_estimate_final=nonSensitizing;
				} else {
					System.out.println("*** Handle not NEG");
				}
				
				System.out.println("CAS="+er.casrn+"\tVS="+er.property_value_string+"\tPEF="+er.property_value_point_estimate_final);

			}
		}
		
//		System.out.println("CAS="+er.casrn+"\tVS="+er.property_value_string+"\tPEF="+er.property_value_point_estimate_final);

	}

	public ExperimentalRecord toExperimentalRecord() {
		ExperimentalRecord er=new ExperimentalRecord();
		
		if(this.CASRN!=null && !this.CASRN.contentEquals("NA")) {
			er.casrn=this.CASRN;	
		}
		
		er.chemical_name=Chemical_Name;
		er.property_name=ExperimentalConstants.strSkinSensitizationLLNA;
		er.source_name=RecordNICEATM.sourceName;
		
		er.experimental_parameters = new Hashtable<>();
		er.experimental_parameters.put("LLNA Vehicle", this.LLNA_Vehicle);
		er.experimental_parameters.put("EC3 Value", this.EC3);
		
		setPropertyValues(er);
		
		er.literatureSource=new LiteratureSource();
		er.literatureSource.name=this.Brief_Citation;
		er.literatureSource.citation=this.Citation;
		
		return er;
	}

}