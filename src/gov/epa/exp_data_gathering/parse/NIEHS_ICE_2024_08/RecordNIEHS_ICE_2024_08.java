package gov.epa.exp_data_gathering.parse.NIEHS_ICE_2024_08;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
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
		if(Endpoint.equals("LD50")) {
			er.property_value_point_estimate_original=Double.parseDouble(Response);
			er.property_value_units_original=Response_Unit;
			er.property_value_numeric_qualifier=Response_Modifier;
			
			if(er.property_value_point_estimate_original!=null)
				unitConverter.convertRecord(er);

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
			}
			
			if(Reference.contains(";")) {//TODO it can also have a | delimiter and have 2 sets of values
				//For skin sens, Reference is usually a literature source:
//				Arfsen et al. 2006; 16980244; 10.1080/15569520600860306    (brief citation; pubmedid ; doi)
				
//				For example that pubmed id => literatureSource.url="https://pubmed.ncbi.nlm.nih.gov/16980244/"
				
				System.out.println(Reference);//TODO parse into citation, pubmed url, doi 
			}
					
			
			
		}
	}

}