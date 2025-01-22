package gov.epa.exp_data_gathering.parse.NIEHS_ICE_2024_08;

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
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.PublicSource;
import gov.epa.exp_data_gathering.parse.UnitConverter;

public class RecordNIEHS_ICE_2024_08 {
	public String Record_ID;
	public String Data_Type;
	public String Formulation_ID;
	public String Formulation_Name;
	public String Chemical_Name;
	public String CASRN;
	public String DTXSID;
	public String Percent_Active_Ingredient;
	public String Mixture;
	public String Species;
	public String Route;
	public String Assay;
	public String Endpoint;
	public String Response_Modifier;
	public String Response;
	public String Response_Unit;
	public String Reference;
	public String SMILES;
	public String Preferred_Name;
	public String Synonyms;
	public String URL_CompTox;
	public String URL_CEBS;
	public String Internal_Data_Source;
	public String Duration;
	public String URL;
	public String Sex;
	public String Strain;
	public String Level_of_Evidence;
	public String Tissue;
	public String Location;
	public String Lesion;
	public String Lesion_Incidence;
	public String Study_ID;
	public String Life_Stage;
	public String Critical_Effect;
	public String Unified_Medical_Language_System;
	public String PMID;
	public String Reported_Strain;
	public String Maximum_Dose;
	public String Maximum_Dose_Units;
	public String Age_at_First_Dose;
	public String Age_Ovariectomized_or_Castrated;
	public String Time_Elapsed_Between_Surgery_and_Treatment;
	public String Treatment_Duration;
	public String Time_Elapsed_Between_Last_Dose_and_Necropsy;
	public String Number_of_Doses_Tested;
	public String Reference_Hormone;
	public String Reference_Hormone_Dose;
	public String Reference_Hormone_Dose_Units;
	public String Reference_Hormone_Route;
	public String Reported_Response_Modifier;
	public String Reported_Response;
	public String Reported_Response_Unit;
	public String Conversion_Factor;
	public String Conversion_Factor_Value;
	public String Conversion_Factor_Source;
	public String Converted_Response;
	public String Converted_Response_Unit;
	public String Concentration;
	public String Concentration_Units;
	public String Converted_Response_Modifier;
	public static final String[] fieldNames = {"Record_ID","Data_Type","Formulation_ID","Formulation_Name","Chemical_Name","CASRN","DTXSID","Percent_Active_Ingredient","Mixture","Species","Route","Assay","Endpoint","Response_Modifier","Response","Response_Unit","Reference","SMILES","Preferred_Name","Synonyms","URL_CompTox","URL_CEBS","Internal_Data_Source","Duration","URL","Sex","Strain","Level_of_Evidence","Tissue","Location","Lesion","Lesion_Incidence","Study_ID","Life_Stage","Critical_Effect","Unified_Medical_Language_System","PMID","Reported_Strain","Maximum_Dose","Maximum_Dose_Units","Age_at_First_Dose","Age_Ovariectomized_or_Castrated","Time_Elapsed_Between_Surgery_and_Treatment","Treatment_Duration","Time_Elapsed_Between_Last_Dose_and_Necropsy","Number_of_Doses_Tested","Reference_Hormone","Reference_Hormone_Dose","Reference_Hormone_Dose_Units","Reference_Hormone_Route","Reported_Response_Modifier","Reported_Response","Reported_Response_Unit","Conversion_Factor","Conversion_Factor_Value","Conversion_Factor_Source","Converted_Response","Converted_Response_Unit","Concentration","Concentration_Units","Converted_Response_Modifier"};

	public static final String lastUpdated = "2024_08";
	public static final String sourceName = "NIEHS_ICE_2024_08"; // TODO Consider creating ExperimentalConstants.strSourceNIEHS_ICE_2024_08 instead.

	private static final transient UnitConverter unitConverter = new UnitConverter("data/density.txt");

	public static Vector<JsonObject> parseNIEHS_ICE_2024_08RecordsFromExcel(String fileName,String sheetName) {
		ExcelSourceReader esr = new ExcelSourceReader(fileName,"data\\experimental", sourceName,sheetName);
		
		esr.headerRowNum=2;
		Vector<JsonObject> records = esr.parseRecordsFromExcel("Chemical_Name"); // TODO Chemical name index guessed from header. Is this accurate?
		return records;
	}

	
	private void setPropertyValues(ExperimentalRecord er) {
		DecimalFormat df=new DecimalFormat("0.0");
		if(Endpoint.equals("LD50")) {
			er.property_value_point_estimate_original=Double.parseDouble(Response);
			er.property_value_units_original=Response_Unit;
			er.property_value_numeric_qualifier=Response_Modifier;
			
			if(er.property_value_point_estimate_original!=null)
				unitConverter.convertRecord(er);

		} 
		
		if(this.Endpoint.equals("EC3")) {
			double value = Double.parseDouble(Response);
			double nonSensitizing = 0;
			double sensitizing = 1;
			er.property_value_string = "EC3 = "+df.format(value) + "%";
			if(Response_Unit.equals("nmol")) {
				er.keep=false;
				er.reason="Bad units";
			}
			er.property_value_units_original=Response_Unit;
			er.property_value_point_estimate_original=value;
			er.property_value_numeric_qualifier=Response_Modifier;
			if (Response_Modifier != null) {
				er.property_value_string = Response_Modifier + er.property_value_string;
			}
			if (Response_Modifier != null && Response_Modifier.equals(">")) {

				if (value >= 100) {
					er.property_value_units_final=ExperimentalConstants.str_binary;
					er.property_value_qualitative = "Not sensitizing";
					er.property_value_point_estimate_final=nonSensitizing;
					er.updateNote("EC3 (>" + value+ "%) was greater than 100%");
				} else {
					er.keep = false;
					er.property_value_qualitative = "Ambiguous";
					er.updateNote("unknown if EC3 (>"+ value + "%) is > 100%");
				}
		} else {
			er.property_value_qualitative = "Sensitizing";
			er.property_value_units_final=ExperimentalConstants.str_binary;
			er.property_value_point_estimate_final=sensitizing;
			er.updateNote("0% < EC3 (" + df.format(value) + "%) < 100%");
			// System.out.println(er.note);
			} 
		}
			
		if(this.Endpoint.equals("Max stimulation index")){
			double value = Double.parseDouble(Response);
			double nonSensitizing = 0;
			double sensitizing = 1;
			er.property_value_string = "SI = "+df.format(value);
			er.property_value_units_original=Response_Unit;
			er.property_value_point_estimate_original=value;
			if(value<3) {
				er.property_value_units_final=ExperimentalConstants.str_binary;
				er.property_value_qualitative = "Not sensitizing";
				er.property_value_point_estimate_final=nonSensitizing;
				er.updateNote("SI (" + df.format(value) + ") was less than 3.0");
			} else {
				er.property_value_qualitative = "Sensitizing";
				er.property_value_units_final=ExperimentalConstants.str_binary;
				er.property_value_point_estimate_final=sensitizing;
				er.updateNote("SI (" + df.format(value) + ") was greater than 3.0");
			}
		}
		
		if(this.Endpoint.equals("GHS classification") || this.Endpoint.equals("EPA classification") || this.Endpoint.equals("Call")) {
			if(this.Response!=null) {
				er.property_value_units_final=ExperimentalConstants.str_binary;
				er.property_value_units_original=ExperimentalConstants.str_binary;
				double nonSensitizing = 0;
				double sensitizing = 1;
				er.property_value_string=this.Response;
				if(this.Response.equals("Sensitizer") || this.Response.equals("Active")) {
					er.property_value_point_estimate_final=sensitizing;
					er.property_value_qualitative = "Sensitizing";
				} else if(this.Response.equals("Non-sensitizer") || this.Response.equals("Inactive")) {
					er.property_value_point_estimate_final=nonSensitizing;
					er.property_value_qualitative = "Not sensitizing";
				} 
			} else {
					er.keep=false;
					er.reason="No response";
			}
		}
	}
	
	
	private void setPropertyName(ExperimentalRecord er) {
		if(this.Assay.equals("Rat Acute Oral Toxicity") && this.Endpoint.equals("LD50")) {
			er.property_name=ExperimentalConstants.strORAL_RAT_LD50;
			er.property_category=ExperimentalConstants.strAcuteOralToxicity;
		
		} else if(this.Assay.equals("LLNA")) {
			er.property_name=ExperimentalConstants.strSkinSensitizationLLNA;		
			
		} else {
			
			er.property_name="Not set";
			er.keep=false;
			er.reason="Property name not set";
		}
	}
	public ExperimentalRecord toExperimentalRecord() {
		ExperimentalRecord er=new ExperimentalRecord();
		
		er.casrn=this.CASRN;
		er.chemical_name=this.Chemical_Name;
		er.source_name=sourceName;
		er.synonyms=this.Synonyms;
		er.dsstox_substance_id=this.DTXSID;

		if(this.Mixture.equals("Mixture")) {
			er.keep=false;
			er.reason="Mixture";
		} else {
//			System.out.println(Mixture);
		}
				
		setSources(er);
		setPropertyName(er);
		setPropertyValues(er);
				
		er.date_accessed=this.lastUpdated;
		
		
		if(this.Sex!=null) er.experimental_parameters.put("Sex",this.Sex);
		
		//TODO Auto-generated method stub
		return er;
	}


	private void setSources(ExperimentalRecord er) {
		if(this.Reference!=null) {
			
			if (URL!=null) {
				er.publicSourceOriginal=new PublicSource();
				er.publicSourceOriginal.name=this.Reference.replace(" (undated)","");
				er.publicSourceOriginal.url=this.URL;
				er.url=this.URL;
			} 
			
			if(Reference.contains(";")) {
				if(this.Assay.equals("LLNA")) {
					er.literatureSource=new LiteratureSource();
					String[] ref = this.Reference.split("; ");
					er.literatureSource.name=ref[0];
					if(!ref[1].equals("Not available")) {
						er.literatureSource.url="https://pubmed.ncbi.nlm.nih.gov/" + ref[1] + "/";
						er.url=er.literatureSource.url;
					} else {
						er.literatureSource.url=null;
					}
					er.reference=er.literatureSource.name;
					if(!ref[2].equals("Not available")) {
						er.literatureSource.doi=ref[2];
					} else {
						er.literatureSource.doi=null;
					}
				}
//				System.out.println(Reference);//TODO parse into citation, pubmed url, doi 
			} else if(Reference!=null) {
				er.publicSourceOriginal=new PublicSource();
				er.publicSourceOriginal.name=this.Reference.replace(" (undated)","");
				er.reference=this.Reference.replace(" (undated)","");
			}
					
			
			
		}
	}

}