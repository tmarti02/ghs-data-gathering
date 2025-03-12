package gov.epa.exp_data_gathering.parse.QSAR_ToolBox;

import java.io.File;
import java.lang.ref.Reference;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


import gov.epa.QSAR.utilities.JsonUtilities;
import gov.epa.api.ExperimentalConstants;
import gov.epa.database.SqlUtilities;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.ParameterValue;
import gov.epa.exp_data_gathering.parse.PublicSource;
import gov.epa.exp_data_gathering.parse.UnitConverter;
import gov.epa.exp_data_gathering.parse.ToxVal.ParseToxVal;

public class RecordQSAR_ToolBox {
	
	public String RecordNumber;
	public String CAS_Number;
	public String Chemical_name_s;
	public String SMILES;
	public String Molecular_formula;
	public String Predefined_substance_type;
	public String Additional_Ids;
	public String Identity;
	public String CAS_SMILES_relation;
	public String Comment;
	public String EndpointPath;
	public String Database;
	public String URL;
	public String Strain;
	
	public String Endpoint;
	public String Endpoint_other;

	public String Test_type;
	public String Conclusions;
	public String Reliability;
	public String Purpose_flag;
	public String GLP_compliance;
	public String Test_guideline;
	public String Study_result_type;
	public String Applied_transforms;
	public String Harmonized_Template;
	public String Qualifier_of_guideline;
	public String Route_of_administration;
	public String Test_organisms_species;
	public String Substance_Test_material_equality;
	public String APPLICANT_S_SUMMARY_AND_CONCLUSION_executive_summary;
	public String Assigned_SMILES;
	

	public String Year;
	public String Year_0;
	public String Year_1;
	public String Year_2;
	public String Year_3;
	public String Year_4;
	public String Year_5;
	public String Year_6;
	public String Year_7;
	public String Year_8;
	public String Year_9;
	public String Year_10;
	public String Year_11;
	public String Year_12;
	public String Year_13;
	public String Year_14;
	public String Year_15;
	public String Year_16;
	public String Year_17;
	public String Year_18;
	public String Year_19;
	
	public String Title;
	public String Title_0;
	public String Title_1;
	public String Title_2;
	public String Title_3;
	public String Title_4;
	public String Title_5;
	public String Title_6;
	public String Title_7;
	public String Title_8;
	public String Title_9;
	public String Title_10;
	public String Title_11;
	public String Title_12;
	public String Title_13;
	public String Title_14;
	public String Title_15;
	public String Title_16;
	public String Title_17;
	public String Title_18;
	public String Title_19;

	public String Strain_other;
	public String Test_type_other;
	public String Test_guideline_other;

	public String Bibliographic_source;
	public String Bibliographic_source_2;
	public String Bibliographic_source_5;
	public String Author_s_or_transferred_reference_5;

	
	public String Author_s_or_transferred_reference_2;
	public String Type_of_inhalation_exposure;
	public String Route_of_administration_original;
	public String Type_of_coverage;
	public String Author_s_or_transferred_reference;
	
	public String Test_guideline_0;
	public String Test_guideline_1;
	public String Test_guideline_2;
	public String Test_guideline_3;
	public String Test_guideline_4;
	public String Test_guideline_5;
	public String Test_guideline_6;
	public String Test_guideline_7;

	public String Test_guideline_other_0;
	public String Test_guideline_other_1;
	public String Test_guideline_other_2;
	public String Test_guideline_other_3;
	public String Test_guideline_other_4;
	public String Test_guideline_other_5;
	public String Test_guideline_other_6;
	public String Test_guideline_other_7;

	public String Qualifier_of_guideline_0;
	public String Qualifier_of_guideline_1;
	public String Qualifier_of_guideline_2;
	public String Qualifier_of_guideline_3;
	public String Qualifier_of_guideline_4;
	public String Qualifier_of_guideline_5;
	public String Qualifier_of_guideline_6;
	public String Qualifier_of_guideline_7;


	public String TestMaterialIsNull;

	public String Any_other_information_on_results_incl_tables;
	public String Test_organisms_species_other;
	public String Type_of_inhalation_exposure_other;
	public String Route_of_administration_other;
	public String Type_of_coverage_other;
	public String Route_of_administration_other_original;
	
	public String Duration_MaxQualifier;
	public String Duration;
	public String Duration_Unit;
	
	public String Unit_details;
	public String Qualifier;
	public String Value_MeanValue;
	public String Value_Qualifier;
	public String Value_Unit;
	public String Value_Scale;
	public String Value_MinValue;
	public String Value_MinQualifier;
	public String Value_MaxValue;
	public String Value_MaxQualifier;
	public String Original_value_MeanValue;
	public String Original_value_Qualifier;
	public String Original_value_Unit;
	public String Original_value_Scale;
	public String Original_value_MinValue;
	public String Original_value_MinQualifier;
	public String Original_value_MaxValue;
	public String Original_value_MaxQualifier;
	
	//Echa reach skin sensitization
	public String Organ;
	public String Type_of_method;
	public String Assay;
	public String Assay_original;
	public String Interpretation_Of_Results;
	public String Interpretation_Of_Results_other;
	public String Route_Of_Challenge_Exposure;
	public String Route_Of_Induction_Exposure;
	public String Assay_other;
	public String Route_Of_Challenge_Exposure_other;
	public String Route_Of_Induction_Exposure_other;
	
	
	//Extra info for skin sensitization:
	public String Author;
	public String Comments;
	public String Identity_in_file;
	public String Institution_and_country;
	public String Test_method_Data_source;
	public String Reference_source;
	public String Record_ID;
	public String Test_guideline_qualifier;
	public String Type_of_method_detail_if_other;
	public String Test_guideline_detail_if_other;
	public String Test_guideline_qualifier_detail;
	public String Test_organisms_species_detail_if_other;
	
	//BCF
	public String Reliability_score;
	public String pH;
	public String Temperature;
	public String Statistics;
	public String Water_type;
	public String Superclass;
	public String Species_common_name;
	public String BCFss_lipid_MeanValue;
	public String BCFss_lipid_Unit;
	public String Duration_MinValue;
	public String Duration_MeanValue;
	public String Duration_MaxValue;
	public String Temperature_MeanValue;
	public String Exposure_concentration_MeanValue;
	public String Tissue_analyzed;
	

	public static final String[] fieldNames = {"RecordNumber","CAS_Number","Chemical_name_s","SMILES","Molecular_formula","Predefined_substance_type","Additional_Ids","Identity","CAS_SMILES_relation","Comment","EndpointPath","Database","URL","Strain","Year_0","Year_1","Endpoint","Title_0","Title_1","Test_type","Conclusions","Reliability","Purpose_flag","Strain_other","GLP_compliance","Test_guideline","Test_type_other","Study_result_type","Applied_transforms","Harmonized_Template","Test_guideline_other","Qualifier_of_guideline","Bibliographic_source_0","Bibliographic_source_1","Route_of_administration","Test_organisms_species","Interpretation_of_results","Author_s_or_transferred_reference_0","Author_s_or_transferred_reference_1","Substance_Test_material_equality","Principles_of_method_if_other_than_guideline","Assigned_SMILES","Qualifier","Year","Title","Bibliographic_source","Type_of_inhalation_exposure","Route_of_administration_original","Author_s_or_transferred_reference","Interpretation_of_results_other","Year_2","Title_2","Bibliographic_source_2","Author_s_or_transferred_reference_2","Test_guideline_0","Test_guideline_1","Test_guideline_2","Qualifier_of_guideline_0","Qualifier_of_guideline_1","Qualifier_of_guideline_2","Type_of_coverage","Unit_details","Endpoint_other","Any_other_information_on_results_incl_tables","Test_organisms_species_other","APPLICANT_S_SUMMARY_AND_CONCLUSION_executive_summary","Test_guideline_other_2","Test_guideline_3","Qualifier_of_guideline_3","Type_of_inhalation_exposure_other","Route_of_administration_other","Type_of_coverage_other","TestMaterialIsNull","Route_of_administration_other_original","Year_3","Title_3","Test_guideline_other_1","Bibliographic_source_3","Author_s_or_transferred_reference_3","Test_guideline_other_0","Year_4","Title_4","Bibliographic_source_4","Author_s_or_transferred_reference_4","Year_5","Title_5","Bibliographic_source_5","Author_s_or_transferred_reference_5","Test_guideline_other_3","Year_6","Title_6","Bibliographic_source_6","Author_s_or_transferred_reference_6","Year_7","Year_8","Year_9","Title_7","Title_8","Title_9","Year_10","Title_10","Bibliographic_source_7","Bibliographic_source_8","Bibliographic_source_9","Bibliographic_source_10","Author_s_or_transferred_reference_7","Author_s_or_transferred_reference_8","Author_s_or_transferred_reference_9","Author_s_or_transferred_reference_10","Year_11","Title_11","Bibliographic_source_11","Author_s_or_transferred_reference_11","Year_12","Year_13","Year_14","Year_15","Year_16","Year_17","Year_18","Year_19","Title_12","Title_13","Title_14","Title_15","Title_16","Title_17","Title_18","Title_19","Bibliographic_source_12","Bibliographic_source_13","Bibliographic_source_14","Bibliographic_source_15","Bibliographic_source_16","Bibliographic_source_17","Bibliographic_source_18","Bibliographic_source_19","Author_s_or_transferred_reference_12","Author_s_or_transferred_reference_13","Author_s_or_transferred_reference_14","Author_s_or_transferred_reference_15","Author_s_or_transferred_reference_16","Author_s_or_transferred_reference_17","Author_s_or_transferred_reference_18","Author_s_or_transferred_reference_19","Test_guideline_4","Test_guideline_5","Test_guideline_other_4","Test_guideline_other_5","Qualifier_of_guideline_4","Qualifier_of_guideline_5","Duration_MeanValue","Duration_Qualifier","Duration_Unit","Duration_Scale","Duration_MinValue","Duration_MinQualifier","Duration_MaxValue","Duration_MaxQualifier","Duration","Value_MeanValue","Value_Qualifier","Value_Unit","Value_Scale","Value_MinValue","Value_MinQualifier","Value_MaxValue","Value_MaxQualifier","Original_value_MeanValue","Original_value_Qualifier","Original_value_Unit","Original_value_Scale","Original_value_MinValue","Original_value_MinQualifier","Original_value_MaxValue","Original_value_MaxQualifier","Organ","Type_of_method","Assay","Assay_original","Interpretation_Of_Results","Interpretation_Of_Results_other","Route_Of_Challenge_Exposure","Route_Of_Induction_Exposure","Assay_other","Route_Of_Challenge_Exposure_other","Route_Of_Induction_Exposure_other","Test_guideline_6","Test_guideline_7","Test_guideline_other_6","Test_guideline_other_7","Qualifier_of_guideline_6","Qualifier_of_guideline_7","Author","Comments","Identity_in_file","Institution_and_country","Test_method_Data_source","Reference_source","Record_ID","Test_guideline_qualifier","Type_of_method_detail_if_other","Test_guideline_detail_if_other","Test_guideline_qualifier_detail","Test_organisms_species_detail_if_other", "Reliability_score", "pH","Temperature", "Statistics", "Water_Type", "Species_common_name", "BCFss_lipid_MeanValue", "BCFss_lipid_Unit", "Duration_MinValue", "Duration_MeanValue", "Duration_MaxValue", "Superclass", "Temperature_MeanValue", "Exposure_Concentration_MeanValue", "Tissue_analyzed"};

	
	public String lastUpdated;

//	public static final String[] fieldNames = {"field0","CAS_Number","Chemical_name_s","SMILES","Molecular_formula","Predefined_substance_type","Additional_Ids","Identity","CAS_SMILES_relation","Comment","EndpointPath","Database","URL","Year","Title","Strain","Endpoint","Test_type","Conclusions","Reliability","Purpose_flag","GLP_compliance","Test_guideline","Study_result_type","Applied_transforms","Harmonized_Template","Qualifier_of_guideline","Route_of_administration","Test_organisms_species","Substance_Test_material_equality","APPLICANT_S_SUMMARY_AND_CONCLUSION_executive_summary","Assigned_SMILES","Qualifier","Year_0","Year_1","Title_0","Title_1","Strain_other","Test_type_other","Test_guideline_other","Year_2","Title_2","Bibliographic_source_2","Author_s_or_transferred_reference_2","Type_of_inhalation_exposure","Route_of_administration_original","Type_of_coverage","Bibliographic_source","Author_s_or_transferred_reference","Unit_details","Test_guideline_0","Test_guideline_1","Test_guideline_2","Test_guideline_other_2","Qualifier_of_guideline_0","Qualifier_of_guideline_1","Qualifier_of_guideline_2","TestMaterialIsNull","Test_guideline_3","Qualifier_of_guideline_3","Any_other_information_on_results_incl_tables","Test_organisms_species_other","Endpoint_other","Test_guideline_other_3","Type_of_inhalation_exposure_other","Route_of_administration_other","Type_of_coverage_other","Route_of_administration_other_original","Year_3","Year_4","Title_3","Title_4","Year_5","Title_5","Bibliographic_source_5","Author_s_or_transferred_reference_5","Test_guideline_other_1","Test_guideline_other_0","Year_6","Title_6","Year_7","Year_8","Year_9","Title_7","Title_8","Title_9","Year_10","Title_10","Year_11","Title_11","Year_12","Year_13","Year_14","Year_15","Year_16","Year_17","Year_18","Year_19","Title_12","Title_13","Title_14","Title_15","Title_16","Title_17","Title_18","Title_19","Test_guideline_4","Test_guideline_5","Test_guideline_other_4","Test_guideline_other_5","Qualifier_of_guideline_4","Qualifier_of_guideline_5","Duration_MaxQualifier","Duration","Value_MeanValue","Value_Qualifier","Value_Unit","Value_Scale","Value_MinValue","Value_MinQualifier","Value_MaxValue","Value_MaxQualifier","Original_value_MeanValue","Original_value_Qualifier","Original_value_Unit","Original_value_Scale","Original_value_MinValue","Original_value_MinQualifier","Original_value_MaxValue","Original_value_MaxQualifier"};


	public static String sourceName = "QSAR_ToolBox";
	
	private static final transient UnitConverter unitConverter = new UnitConverter("data/density.txt");
	static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues()
			.create();

	
	
	private void setIdentifiers(ExperimentalRecord er) {

		if(this.CAS_Number!=null && !this.CAS_Number.contentEquals("No CAS number") && !this.CAS_Number.contains("Missing")) {
			er.casrn=this.CAS_Number;	
			if(er.casrn.contains("Invalid")) er.casrn=null;
		}
		
		er.chemical_name=this.Chemical_name_s;
		
		if(er.chemical_name!=null && er.chemical_name.contains("Missing")) {
			er.chemical_name=null;
		}
		
		er.synonyms=this.Chemical_name_s;
		if(er.chemical_name!=null && er.chemical_name.contains(";")) {
			String []names=er.chemical_name.split(";");
			er.chemical_name=names[0];//use first name otherwise we wont have chemreg match
		}
		
		
		if (this.Additional_Ids!=null && this.Additional_Ids.contains("DTXSID :")) {
			
			String [] ids=this.Additional_Ids.split(";");
			
			for (String id:ids) {
				if(id.contains("DTXSID")) {
					er.dsstox_substance_id="DTXSID"+id.substring(id.indexOf(":")+1,id.length());
//					System.out.println(er.chemical_name+"\t"+id+"\t"+er.dsstox_substance_id);
				}
			}
			
		}
		
		if(er.casrn==null && er.chemical_name==null) {
			er.keep=false;
			er.reason="No chemical identifiers";
		}
	}
	
	/**
	 * For skin sensitization and acute toxicity
	 * 
	 * TODO break into separate methods
	 * 
	 * @param originalSourceName
	 * @return
	 */
	public ExperimentalRecord toExperimentalRecord(String originalSourceName) {
		ExperimentalRecord er=new ExperimentalRecord();
		
		setSourceInformation(er);		
		setIdentifiers(er);
		setGuidelines(er);
		
		if(EndpointPath!=null) { 
			
			if(EndpointPath.equals("Human Health Hazards#Sensitisation")) {
				
				setPropertyNameSensitization(er); 
				setPropertyValuesLLNA(er);
				
			} else if(EndpointPath.equals("Human Health Hazards#Acute Toxicity")) {
//				System.out.println("Assay="+Assay);
//				System.out.println("Assay="+Assay);

				setUnitsAcute(er);
				setPropertyValuesAcuteToxicity(er);
				setPropertyNameAcuteToxicity(er); 
				unitConverter.convertRecord(er);
			}
		} else {
			er.keep=false;
			er.reason="EndpointPath not set";
		}
		
		if(Database==null) { 
			er.keep=false;
			er.reason="Database is missing";
			
		} else if(Database.equals("Skin Sensitization")) {
			if(this.Reference_source!=null) {
				er.literatureSource=new LiteratureSource();
				
				
				if (Year!=null) Year=Year.substring(0,Year.indexOf("."));
				
				er.literatureSource.year=Year;
				er.literatureSource.author=this.Author;
				er.literatureSource.title=this.Title;
				er.literatureSource.journal=this.Reference_source;
				
				er.literatureSource.citation="";
				
				if (this.Author!=null) er.literatureSource.citation+=Author+" ";
				if (this.Year!=null) er.literatureSource.citation+="("+Year+"). ";
				if (this.Title!=null) er.literatureSource.citation+=Title+" ";
				if (this.Reference_source!=null) er.literatureSource.citation+=Reference_source+" ";
				
				er.literatureSource.citation=er.literatureSource.citation.trim();
				er.literatureSource.citation=er.literatureSource.citation.replace("?", "-");
				er.literatureSource.citation=er.literatureSource.citation.replace("û", "-");
				
				
//				System.out.println(er.literatureSource.citation);
				
			}				
			
		} else if(Database.equals("ECHA REACH")) {
			er.experimental_parameters.put("Reliability",Reliability);
			if(this.Reliability==null || this.Reliability.contains("3") || this.Reliability.contains("4")) {
				er.keep=false;
				er.reason="Insufficient reliability";
			}
		}	
		
		
		if (Test_organisms_species!=null) er.experimental_parameters.put("species", this.Test_organisms_species);
		if(this.Strain!=null) er.experimental_parameters.put("Strain",this.Strain);
		
		er.date_accessed=lastUpdated;
		
		// TODO Auto-generated method stub
		return er;
	}

	private void setSourceInformation(ExperimentalRecord er) {
		er.source_name="QSAR_Toolbox";
		er.original_source_name=Database;
//		er.original_source_name=originalSourceName;
		er.url=this.URL;
	}

	private void setPropertyValuesLLNA(ExperimentalRecord er) {

		DecimalFormat df=new DecimalFormat("0.0");
		double nonSensitizing = 0;
		double sensitizing = 1;
		
		if (Database.equals("Skin Sensitization")) {
			if (Assay.equals("LLNA")) {

				if(Value_Scale.equals("Skin sensitization EC3(ratio)")) {
					
					double value = Double.parseDouble(Value_MeanValue);
	
					er.property_value_string = "EC3 = "+df.format(value) + "%";
					er.property_value_point_estimate_original=value;
					er.property_value_units_original="%";
					er.property_value_numeric_qualifier=Value_Qualifier;
					
					if (Value_Qualifier != null)
						er.property_value_string = Value_Qualifier + er.property_value_string;

					if (Value_Qualifier != null && Value_Qualifier.equals(">")) {

						if (value >= 100) {
							er.property_value_qualitative = "Not sensitizing";
							er.updateNote("EC3 (>" + value+ "%) was greater than 100%");
						} else {
							er.keep = false;
							er.reason="Ambiguous";
							er.property_value_qualitative = "Ambiguous";
							er.updateNote("unknown if EC3 (>"+ value + "%) is > 100%");
						}
					} else if (Value_Qualifier != null && Value_Qualifier.equals("<")) {
						er.property_value_qualitative = "Sensitizing";
						er.updateNote("EC3 (" + Value_Qualifier + " "+ df.format(value) + "%) < 100%");
						
						// System.out.println(er.note);
					} else if (value >= 100) {
						er.property_value_qualitative = "Not sensitizing";
						er.updateNote("EC3 (" + value+ "%) was greater than 100%");
//						System.out.println("Need to handle EC3>100: " + Value_MeanValue + " " + Value_Unit);
						// er.updateNote("Negative because the EC3
						// ("+er.property_value_point_estimate_original+"%)+ was greater than 100%");
						// Doesnt happen
					} else {
						er.property_value_qualitative = "Sensitizing";
						er.updateNote("0% < EC3 (" + df.format(value) + "%) < 100%");
						// System.out.println(er.note);
					}		
					
				} else if (Value_Scale.equals("Skin sensitisation I (Oasis)")) {

					er.property_value_string=Value_MeanValue;

					if(Value_MeanValue.equals("Negative") || Value_MeanValue.equals("Not sensitising")) {
						er.property_value_qualitative="Not sensitizing";
						er.property_value_point_estimate_original=nonSensitizing;
						er.property_value_units_original=ExperimentalConstants.str_binary;
						er.updateNote("call = "+Value_MeanValue);
					} else if(Value_MeanValue.equals("Strongly positive") || Value_MeanValue.equals("Weakly positive")) {
						er.property_value_qualitative="Sensitizing";
						er.property_value_point_estimate_original=sensitizing;
						er.property_value_units_original=ExperimentalConstants.str_binary;
						er.updateNote("call = "+Value_MeanValue);
					} else { 
						System.out.println("Need to handle Value_MeanValue="+Value_MeanValue);
					}

				} else {
					System.out.println("Need to handle Value_Scale="+Value_Scale+" for Assay="+Assay);
				}

			} else {
//				System.out.println("Need to handle Value_Scale="+Value_Scale+" for Assay="+Assay);
			}
			
			if (er.property_value_qualitative!=null) {
				if (er.property_value_qualitative.contains("Not sensitizing")) {
					er.property_value_point_estimate_final=nonSensitizing;

				} else if (er.property_value_qualitative.contains("Sensitizing")) {
					er.property_value_point_estimate_final=sensitizing;
				}
				er.property_value_units_final=ExperimentalConstants.str_binary;
				
//				System.out.println(er.property_value_qualitative);
			}

		} else if(Database.equals("ECHA REACH")) {
			System.out.println("Handle ECHA REACH");
			//TODO: Doesnt look like it exports usable data
			
		}
	}

	private void setGuidelines(ExperimentalRecord er) {
		List<String>guidelines=new ArrayList<>();
		List<String>guidelineQualifiers=new ArrayList<>();
		
		//TODO add testguide_other values using reflection...
		
		if(this.Test_guideline!=null) {
			guidelines.add(this.Test_guideline);
			guidelineQualifiers.add(this.Qualifier_of_guideline);
		}
		if(this.Test_guideline_0!=null) {
			guidelines.add(this.Test_guideline_0);
			guidelineQualifiers.add(this.Qualifier_of_guideline_0);
		}
		if(this.Test_guideline_1!=null) {
			guidelines.add(this.Test_guideline_1);
			guidelineQualifiers.add(this.Qualifier_of_guideline_1);
		}
		if(this.Test_guideline_2!=null) {
			guidelines.add(this.Test_guideline_2);
			guidelineQualifiers.add(this.Qualifier_of_guideline_2);
		}
		if(this.Test_guideline_3!=null) {
			guidelines.add(this.Test_guideline_3);
			guidelineQualifiers.add(this.Qualifier_of_guideline_3);
		}
		if(this.Test_guideline_4!=null) {
			guidelines.add(this.Test_guideline_4);
			guidelineQualifiers.add(this.Qualifier_of_guideline_4);
		}
		
		if(this.Test_guideline_5!=null) {
			guidelines.add(this.Test_guideline_5);
			guidelineQualifiers.add(this.Qualifier_of_guideline_5);
		}
		
		if(this.Test_guideline_6!=null) {
			guidelines.add(this.Test_guideline_6);
			guidelineQualifiers.add(this.Qualifier_of_guideline_6);
		}
		
		if(this.Test_guideline_7!=null) {
			guidelines.add(this.Test_guideline_7);
			guidelineQualifiers.add(this.Qualifier_of_guideline_7);
		}

		
		
		//TODO use all the various guideline and qualifier fields to assemble all the guidelines into one string...
		//TODO set keep = false if none of the guidelines are a good guideline
				
		String strGuidelinesQualifiers = "";
		for (int g = 0; g < guidelines.size(); g++) {
			if (g > 0) strGuidelinesQualifiers += "; "; 
			if(guidelineQualifiers.get(g)!=null && guidelineQualifiers.get(g).trim().length()>0) {
				strGuidelinesQualifiers += guidelineQualifiers.get(g) + " ";
			}
			strGuidelinesQualifiers +=guidelines.get(g);
		}
				
//		if(strGuidelinesQualifiers.contains(";")) {
//			System.out.println(strGuidelinesQualifiers);
//		}
		
		if(strGuidelinesQualifiers!=null && strGuidelinesQualifiers.length()>0) {
			er.experimental_parameters.put("Guideline", strGuidelinesQualifiers);
		}
		
	}
	
	
	private void checkLLNA_Guideline(ExperimentalRecord er) {
		
		String strGuidelinesQualifiers=er.experimental_parameters.get("Guideline")+"";
		
//		List<String> badGuidelines = Arrays.asList("according to guideline other:", "according to guideline other: as below", "according to guideline other: as per mentioned below",
//				"according to guideline other: LLNA assay", "according to guideline other: The objective of the study was to evaluate the utility of the LLNA assay to determine the contact sensitization potential of the test chemical",
//				"according to guideline other: Sensitive mouse lymph node assay (SLNA)", "according to guideline other: The objective of the study was to evaluate the utility of the LLNA assay to determine the contact sensitization potential of the test chemical",
//				"equivalent or similar to guideline other: according to Ulrich, P. et al. 1998: Toxicology 125, 149-168", "equivalent or similar to guideline other: As mentioned below", " equivalent or similar to guideline other: Kimber et al., 1989");
		
		List<String> goodGuidelines = Arrays.asList("429", "870.2600", "B.42", "442A", "442B","442 B", "B.51", "406", "595.12", "B.6");

		
//		if(er.casrn!=null && er.casrn.equals("127-51-5")) {
//			System.out.println(gson.toJson(this)+"\r\n");
//			System.out.println(gson.toJson(er));
//		}
		
		if(er.keep) {
			if(er.experimental_parameters.get("Guideline")!=null && !hasString(goodGuidelines, strGuidelinesQualifiers)){
//				System.out.println(er.property_name+"\t"+strGuidelinesQualifiers);
				System.out.println("Invalid guideline:\t"+strGuidelinesQualifiers);
				er.keep=false;
				er.reason="Invalid guideline";	
			}
		}
		
	}
	
	boolean hasString(List<String>examples,String str) {
		for(String example:examples ) {
			if(str.contains(example)) {
				return true;
			}
		}

		return false;
	}


	private void setPropertyValuesAcuteToxicity(ExperimentalRecord er) {
		
		
		if(this.Value_MinValue!=null && this.Original_value_MaxValue!=null) {
			er.property_value_min_original=Double.parseDouble(this.Value_MinValue);
			er.property_value_max_original=Double.parseDouble(this.Value_MaxValue);
			
			er.property_value_string=er.property_value_min_original+" - "+er.property_value_max_original;
			
//			System.out.println(er.property_value_min_original+"\t"+er.property_value_max_original);
			
		} else if(this.Value_MinValue!=null) {
			er.property_value_min_original=Double.parseDouble(this.Value_MinValue);
			er.property_value_string="> "+er.property_value_min_original;
//			System.out.println(er.property_value_min_original);
			
		} else if(this.Value_MaxValue!=null) {
			er.property_value_max_original=Double.parseDouble(this.Value_MaxValue);
//			System.out.println(er.property_value_max_original);
			er.property_value_string="< "+er.property_value_max_original;
			
		} else if(this.Value_MeanValue!=null) {

			er.property_value_point_estimate_original=Double.parseDouble(this.Value_MeanValue);
			
			er.property_value_string="";
			
			if(this.Qualifier!=null) {
				
				if (Qualifier.contentEquals("No qualifier")) {
					er.property_value_numeric_qualifier="";
				} else if(Qualifier.equals("ca.")) {
					er.property_value_numeric_qualifier="~";
//					System.out.println("here:"+er.property_value_numeric_qualifier.length());
				} else if (Qualifier.contentEquals("?")) {
					er.property_value_numeric_qualifier="?";
					er.keep=false;
					er.reason="Unknown numeric qualifier";
				} else if (Qualifier.contentEquals(">")) {
					er.property_value_numeric_qualifier=">";
				} else if (Qualifier.contentEquals("<")) {
					er.property_value_numeric_qualifier="<";
				} else {
					System.out.println("Unhandled qualifier:\t"+this.Qualifier);	
				}
				
//					System.out.println("here:"+er.property_value_numeric_qualifier);				
				if(er.property_value_numeric_qualifier.length()>0)
					er.property_value_string+=er.property_value_numeric_qualifier+ " ";
				 
			}
			er.property_value_string+=er.property_value_point_estimate_original;
		} 
			
			
				
		if(er.property_value_string!=null && er.property_value_units_original!=null) {
			er.property_value_string+=" "+er.property_value_units_original;
		}
		
		if(er.property_value_string==null) {
			er.keep=false;
			er.reason="No property_value_string";
		}
		
	}

	private void setUnitsAcute(ExperimentalRecord er) {
		if (this.Value_Unit!=null) {
			
			String units=this.Value_Unit;
			units=units.replace(", (5.8 mg/L air)", "");
			units=units.replace("(22,948 mg/m3, 23 mg/L nominal)", "");
			units=units.replace("(5300 mg/m3, 5.3 mg/L)", "");
			units=units.replace("(4500 mg/kg bw)", "");
			 			
			units=units.replace("(analytical)", "");
			units=units.replace("(nominal)", "");
			
			units=units.replace("mg/Kg", ExperimentalConstants.str_mg_kg);
			
			units=units.replace("mL/kg bw", ExperimentalConstants.str_mL_kg);
			units=units.replace("ul/kg bw", ExperimentalConstants.str_uL_kg);
			units=units.replace("µl/kg bw", ExperimentalConstants.str_uL_kg);
						
			units=units.replace("cm3/kg bw", ExperimentalConstants.str_mL_kg);
			units=units.replace("cc/kg", ExperimentalConstants.str_mL_kg);
			
			units=units.replace("mg/L air", ExperimentalConstants.str_mg_L);
			units=units.replace("mg/L in drinking water", ExperimentalConstants.str_mg_L);

			units=units.replace("µl/L air", ExperimentalConstants.str_uL_L);
			
			units=units.replace("mg/m^3 air", ExperimentalConstants.str_mg_m3);
			units=units.replace("mg/m3", ExperimentalConstants.str_mg_m3);			

			units=units.replace("gm/kg bw", ExperimentalConstants.str_g_kg);			
			units=units.replace("g/kg/ bw", ExperimentalConstants.str_g_kg);			
			units=units.replace("g/Kg", ExperimentalConstants.str_g_kg);
			units=units.replace("g/kg bw.", ExperimentalConstants.str_g_kg);
			units=units.replace("g/kg/bw", ExperimentalConstants.str_g_kg);
						
			units=units.replace("g/m3 air", ExperimentalConstants.str_g_m3);
									
			er.property_value_units_original=units.trim();
			
		} else {
			er.keep=false;
			er.reason="Missing units";
		}
	}

	private void setPropertyNameAcuteToxicity(ExperimentalRecord er) {
		
		
		if (this.Harmonized_Template==null) {
			er.property_name="Not set";
			er.keep=false;
			er.reason="Missing Harmonized_Template";
		} else if(this.Harmonized_Template.equals("SkinSensitisation")) {

			if(Assay.contains("LLNA")) {
				er.property_name=ExperimentalConstants.strSkinSensitizationLLNA;
			}
			
			
//			if(this.Endpoint!=null && this.Endpoint.equals("EC3")) {
//				er.property_name=ExperimentalConstants.strSkinSensitizationLLNA_EC3;
//			} if(this.Endpoint_other!=null && this.Endpoint_other.equals("stimulation index")) {
//				er.property_name=ExperimentalConstants.strSkinSensitizationLLNA_SI;
//			}
			
//			System.out.println(this.CAS_Number+"\tSkinSensitization\t"+this.Endpoint_other);
			
		
		
		} else if (this.Test_organisms_species==null) {
			er.property_name="Not set";
			er.keep=false;
			er.reason="Missing Test_organisms_species";

		} else if (this.Endpoint_other!=null && !this.Harmonized_Template.equals("SkinSensitisation")) {
			er.keep=false;
			er.reason="Property not handled";
			er.property_name=Harmonized_Template.replace("AcuteToxicity", "")+" "+Test_organisms_species+" "+this.Endpoint_other;

		} else if(this.Harmonized_Template.equals("AcuteToxicityOral")) {

			if(this.Test_organisms_species.equals("rat")) {
				er.property_name=ExperimentalConstants.strORAL_RAT_LD50;
			} else if(this.Test_organisms_species.equals("mouse")) {
				er.property_name=ExperimentalConstants.strORAL_MOUSE_LD50;
			} else if(this.Test_organisms_species.equals("rabbit")) {
				er.property_name=ExperimentalConstants.strORAL_RABBIT_LD50;
			} else {
//				System.out.println("oral\t"+this.Harmonized_Template+"\t"+this.Test_organisms_species+"\t"+this.Test_organisms_species_other);
			}
			
			er.property_category=ExperimentalConstants.strAcuteOralToxicity;
			

		} else if(this.Harmonized_Template.equals("AcuteToxicityInhalation")) {
			if(this.Test_organisms_species.equals("rat")) {
				er.property_name=ExperimentalConstants.strInhalationRatLC50;
			} else if(this.Test_organisms_species.equals("mouse")) {
				er.property_name=ExperimentalConstants.strInhalationMouseLC50;
			} else if(this.Test_organisms_species.equals("rabbit")) {
				er.property_name=ExperimentalConstants.strInhalationRabbitLC50;
			} else {
//				System.out.println("inhalation\t"+this.Harmonized_Template+"\t"+this.Test_organisms_species+"\t"+this.Test_organisms_species_other);
			}
			
			er.property_category=ExperimentalConstants.strAcuteInhalationToxicity;
			
		} else if(this.Harmonized_Template.equals("AcuteToxicityDermal")) {
			if (this.Test_organisms_species.equals("rat")) {
				er.property_name=ExperimentalConstants.strDERMAL_RAT_LD50;
			} else if(this.Test_organisms_species.equals("mouse")) {
				er.property_name=ExperimentalConstants.strDERMAL_MOUSE_LD50;
			} else if(this.Test_organisms_species.equals("rabbit")) {
				er.property_name=ExperimentalConstants.strDERMAL_RABBIT_LD50;
			} else {
//				System.out.println("dermal\t"+this.Harmonized_Template+"\t"+this.Test_organisms_species+"\t"+this.Test_organisms_species_other);
				
			}
			er.property_category=ExperimentalConstants.strAcuteDermalToxicity;
		}

		if(er.property_name==null) {
			er.property_name="Not set";
			er.keep=false;
			er.reason="Property name not set";
		}
		
		
//		if(this.Endpoint_other!=null && this.Endpoint_other.equals("stimulation index")) {
//			System.out.println(er.property_name);
//		}
		
	}
	
	
	private void setPropertyNameSensitization(ExperimentalRecord er) {
		
		if(Database.equals("ECHA REACH")) {
			if(this.Harmonized_Template==null && this.Harmonized_Template.equals("SkinSensitisation")) {
				er.property_name=ExperimentalConstants.strSkinSensitizationLLNA;
			}
		} else if (Database.equals("Skin Sensitization")) {
			if(Assay.contains("LLNA")) {
				er.property_name=ExperimentalConstants.strSkinSensitizationLLNA;
				checkLLNA_Guideline(er);
			}
		}
		
		if(er.property_name==null) {
			er.property_name="Not set";
			er.keep=false;
			er.reason="Property name not set";
		}
		
		
	}
	
	
	public static Vector<JsonObject> parseQSAR_ToolBoxRecordsFromExcel(String fileName,String sourceName) {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		esr.headerRowNum=2;
		
		
		Vector<JsonObject> records = esr.parseRecordsFromExcel(2); // TODO Chemical name index guessed from header. Is this accurate?
		return records;
	}
	
	private ExperimentalRecord toExperimentalRecordBCF(String propertyName, String method, String BCF_units, String BCF_mean, Hashtable<String, List<Species>> htSpecies) {
		
		
		boolean limitToFish=false;
		if(propertyName.toLowerCase().contains("fish")) limitToFish=true;
		
		boolean limitToWholeBody=false;
		if(propertyName.toLowerCase().contains("whole")) limitToWholeBody=true;
		
		ExperimentalRecord er=new ExperimentalRecord();
		setSourceInformation(er);		
		setIdentifiers(er);
		
		er.property_name=propertyName;
		er.experimental_parameters=new Hashtable<>();
		er.experimental_parameters.put("Measurement method",method);		
		er.property_name = propertyName;
		
		setSpeciesParameters(htSpecies, limitToFish, er);
		
		if(limitToWholeBody && (Organ==null || !Organ.equals("Whole body"))) {
			er.keep=false;
			er.reason="Not whole body";
		}

		try {
			String property_value = BCF_mean;			
			if(BCF_mean!=null) {
				er.property_value_point_estimate_original = Double.parseDouble(BCF_mean);
			}
			er.property_value_units_original=BCF_units;
			er.property_value_string = property_value + " "+er.property_value_units_original;				

		} catch (Exception e) {
			System.out.println("Parse error BCF:\n"+gson.toJson(this));
			e.printStackTrace();
		}
		
		er.property_category="bioconcentration";
		addMetadata(er);
		
		unitConverter.convertRecord(er);
		return er;
	}
	
	private void setTemperature(ExperimentalRecord er) {
		Temperature=Temperature.replace(" °C", "");
		Temperature=Temperature.replace(" ± 1", "");
		Temperature=Temperature.replace("±1", "");
		if(!Temperature.equals("Not reported")) {
			if(Temperature.contains("-")) {
				String[] tempSplit = Temperature.split("-");
				double tempMin=Double.parseDouble(tempSplit[0]);
				double tempMax=Double.parseDouble(tempSplit[1]);
				double tempAvg=(tempMin + tempMax)/2.0;
				er.temperature_C=tempAvg;
			} else {
				er.temperature_C=Double.parseDouble(Temperature);
			}
		}
	}
	
	private void setpH(ExperimentalRecord er) {
		if(!pH.equals("Not reported")) {
			pH=pH.replace(",", ".");
			pH=pH.replace(" - ", "-");
			if(pH.contains("-")) {
				String[] pHsplit = pH.split("-");
				double pHmin=Double.parseDouble(pHsplit[0]);
				double pHmax=Double.parseDouble(pHsplit[1]);
				double pHavg=(pHmin + pHmax)/2.0;
				er.pH="" + pHavg;
			} else {
				er.pH=pH;
			}
		}
	}

	//Adds all metadata for each of BCF data sets
	private void addMetadata(ExperimentalRecord er) {
		if(Database==null) { 
			er.keep=false;
			er.reason="Database is missing";
		} else if(Database.equals("Bioaccumulation fish CEFIC LRI")) {				
			if(this.Reliability_score==null || this.Reliability_score.contains("3") || this.Reliability_score.contains("4")) {
				er.keep=false;
				er.reason="Insufficient reliability";
			} else {
				er.experimental_parameters.put("Reliability",Reliability_score);
			}
			if(Comments!=null) {
				er.note=Comments;
			}
			er.reference=Reference_source;
			er.experimental_parameters.put("Media type",Water_type);
			er.experimental_parameters.put("Tissue", Organ);
			er.experimental_parameters.put("Species latin",Test_organisms_species);
			er.experimental_parameters.put("Species common",Species_common_name);
			setTemperature(er);
			setpH(er);
			if(Statistics!=null) {
				er.note=Statistics;
			}
			if(Duration_MinValue!=null) {
				er.experimental_parameters.put("Exposure Duration (in days or Lifetime)", Duration_MinValue);
			}
			if(Duration_MaxValue!=null) {
				er.experimental_parameters.put("Duration_MaxValue", Duration_MaxValue);
			}

			LiteratureSource ls=new LiteratureSource();
			er.literatureSource=ls;
			if(Author!=null) {
				ls.name=Author + " (" + Year + ")";
				ls.author=Author;
				ls.title=Title;
				ls.citation=Author+" ("+Year+"). "+ls.title+". "+Reference_source;
			} else {
				ls.citation=Reference_source + " (" + Year + ")";
			}
		} else if(Database.equals("Bioaccumulation Canada")) {
			er.experimental_parameters.put("Species latin",Test_organisms_species);
			if(Duration_MeanValue!=null) {
				if(Duration_Unit.equals("h")) {
					double duration=Double.parseDouble(Duration_MeanValue);
					Duration_MeanValue="" + duration/24;
				}
				er.experimental_parameters.put("Exposure Duration (in days or Lifetime)", Duration_MeanValue);
			}
			if(Duration_MinValue!=null) {
				er.experimental_parameters.put("Duration_MinValue", Duration_MinValue);
			}

			er.experimental_parameters.put("Duration Units", Duration_Unit);
		} else if(Database.equals("Bioconcentration and logKow NITE")) {
			
			if(Duration_MeanValue!=null) {
				ParameterValue pv=new ParameterValue();
				pv.parameter.name="Exposure duration";
				pv.unit.abbreviation="days";
				double wc=Double.parseDouble(Duration_MeanValue);					
				pv.valuePointEstimate=wc;
				er.parameter_values.add(pv);
			}
			
			if(Temperature_MeanValue!=null) {
				er.temperature_C=Double.parseDouble(Temperature_MeanValue);
				setpH(er);
			}
			
			if(Exposure_concentration_MeanValue!=null) {
				
//				System.out.println("Have water conc="+Exposure_concentration_MeanValue);
				
				ParameterValue pv=new ParameterValue();
				pv.parameter.name="Water concentration";
				pv.unit.abbreviation=ExperimentalConstants.str_g_L;
				double wc=Double.parseDouble(Exposure_concentration_MeanValue);					
				pv.valuePointEstimate=wc*1e-3;
				er.parameter_values.add(pv);
			}
			
			if(Test_guideline!=null) {
				er.experimental_parameters.put("Test guideline", Test_guideline);
			}
			
			if(Year!=null) {
				er.document_name="NITE " + Year;
			}
			
			er.publicSourceOriginal=new PublicSource();
			
//			if(Reference_source.contains("jcheck")) {
//				er.publicSourceOriginal.name="J-Check";
////				er.publicSourceOriginal.url="https://www.nite.go.jp/chem/jcheck/top.action?request_locale=en";
//				er.publicSourceOriginal.url="https://www.nite.go.jp/en/chem/qsar/evaluation.html";
////				er.publicSourceOriginal.description="J-CHECK is a database developed to provide the information regarding \"Act on the Evaluation of Chemical Substances and Regulation of Their Manufacture, etc. (commonly known as \"CSCL\") by the authorities of the law, Ministry of Health, Labour and Welfare, Ministry of Economy, Trade and Industry, and Ministry of the Environment. J-CHECK provides the information regarding CSCL, such as the list of CSCL, chemical safety information obtained in the existing chemicals survey program, risk assessment, etc. in cooperation with eChemPortal by OECD.";
//				er.publicSourceOriginal.description="Biodegradation and bioconcentration data conducted for the evaluations of new chemicals and existing chemicals under CSCL are available in OECD QSAR Toolbox version 3.0 data format (excel file).";
//			} else if(Reference_source.contains("safe")) {
//				er.publicSourceOriginal.name="SAFE";
//				er.publicSourceOriginal.url="https://www.nite.go.jp/en/chem/qsar/evaluation.html";
//				er.publicSourceOriginal.description="Biodegradation and bioconcentration data conducted for the evaluations of new chemicals and existing chemicals under CSCL are available in OECD QSAR Toolbox version 3.0 data format (excel file).";
//			}
			
			//Simplify it because we cant find matching data in J-check:
			er.publicSourceOriginal.name="Bioconcentration and logKow NITE";
			er.publicSourceOriginal.url="https://www.nite.go.jp/en/chem/qsar/evaluation.html";
			er.publicSourceOriginal.description="National Institute of Technology and Evaluation (Japan)";

			if(Tissue_analyzed!=null) {
				er.experimental_parameters.put("Media type",Water_type);
				er.experimental_parameters.put("Response site", Tissue_analyzed);
				er.note=Statistics;
			}
		}
	}
	

	/**
	 * Selects kinetic values for CEFIC data set
	 *  
	 * @param propertyName
	 * @param htSpecies
	 * @return
	 */
	ExperimentalRecord toExperimentalRecordBCF_NITE_Kinetic(String propertyName, Hashtable<String, List<Species>> htSpecies) {
		
		String method="kinetic";
		String BCF_units=ExperimentalConstants.str_L_KG;
		String BCF_mean=Original_value_MeanValue;

		ExperimentalRecord er = toExperimentalRecordBCF(propertyName, method, BCF_units, BCF_mean, htSpecies);
//		if(er!=null) filterRecord(er, limitToWholeOrganism,limitToFish);
		return er;
	}

	
	/**
	 * Selects steady state values for CEFIC data set 
	 * @param propertyName
	 * @param htSpecies
	 * @return
	 */
	ExperimentalRecord toExperimentalRecordBCF_NITE_SS(String propertyName, Hashtable<String, List<Species>> htSpecies) {

		String method="steady state";
		String BCF_mean=BCFss_lipid_MeanValue;
		String BCF_units=ExperimentalConstants.str_LOG_L_KG;
		ExperimentalRecord er = toExperimentalRecordBCF(propertyName, method, BCF_units, BCF_mean, htSpecies);

//		if(er!=null) filterRecord(er, propertyName);
		return er;
	}
	
	/**
	 * Selects kinetic values for Canada data set 
	 * @param propertyName
	 * @return
	 */
	ExperimentalRecord toExperimentalRecordBCFCanada(String propertyName) {

		String method="kinetic";
		String BCF_mean=Value_MeanValue;
		String BCF_units=ExperimentalConstants.str_LOG_L_KG;
		Hashtable<String, List<Species>> htSpecies = null;
		ExperimentalRecord er = toExperimentalRecordBCF(propertyName, method, BCF_units, BCF_mean, htSpecies);

		if(er!=null) filterRecord(er, propertyName);
		return er;
	}

	
	/**
	 * Directly separates steady state and kinetic BCF values for NITE dataset and converts to experimental records different from other BCF data 
	 * @param propertyName
	 * @param htSpecies
	 * @return
	 */
	public ExperimentalRecord toExperimentalRecordBCFNITE(String propertyName, Hashtable<String, List<Species>> htSpecies) {
		
		boolean limitToFish=false;
		if(propertyName.toLowerCase().contains("fish")) limitToFish=true;
		
		boolean limitToWholeBody=false;
		if(propertyName.toLowerCase().contains("whole")) limitToWholeBody=true;
		
		ExperimentalRecord er=new ExperimentalRecord();
		er.parameter_values=new ArrayList<>();
		setSourceInformation(er);		
		setIdentifiers(er);
		
		er.property_name=propertyName;
		er.experimental_parameters=new Hashtable<>();
		if(Endpoint!=null) {
			if(Endpoint.equals("BCF") && Endpoint!=null) {
				String method="kinetic";
				er.experimental_parameters.put("Measurement method",method);
			} else if(Endpoint.equals("BCFss") && Endpoint!=null) {
				String method="steady state";
				er.experimental_parameters.put("Measurement method",method);
			} else if(Endpoint.equals("LogPow") && Endpoint!=null) {
				er.keep=false;
				er.reason="Incorrect property";
			}
		}

		setSpeciesParameters(htSpecies, limitToFish, er);	
		if(limitToWholeBody && (Tissue_analyzed==null || !Tissue_analyzed.equals("Whole body"))) {
			er.keep=false;
			er.reason="Not whole body";
		}

		String BCF_mean=Original_value_MeanValue;
		String BCF_units=ExperimentalConstants.str_L_KG;
		try {
			String property_value = BCF_mean;			
			if(BCF_mean!=null) {
				er.property_value_point_estimate_original = Double.parseDouble(BCF_mean);
			}
//			Value_Qualifier=Value_Qualifier.replace("≤", "<=");
			er.property_value_numeric_qualifier=Value_Qualifier;
			er.property_value_units_original=BCF_units;
			er.property_value_string = property_value + " "+er.property_value_units_original;				

		} catch (Exception e) {
			System.out.println("Parse error BCF:\n"+gson.toJson(this));
			e.printStackTrace();
		}
		
		er.property_category="bioconcentration";
		addMetadata(er);
		
		unitConverter.convertRecord(er);
		return er;
	}
	
	
	/**
	 * Filters BCF values based on whole organism and fish. Only used if no common name 
	 * @param er
	 * @param propertyName
	 */
	private void filterRecord(ExperimentalRecord er, String propertyName) {

		boolean limitToFish=false;
		if(propertyName.toLowerCase().contains("fish")) limitToFish=true;
		
		boolean limitToWholeBody=false;
		if(propertyName.toLowerCase().contains("whole")) limitToWholeBody=true;
		
		if (limitToWholeBody) {
			if (Organ == null
					|| !Organ.toLowerCase().equals("whole body")) {
				er.keep = false;
				er.reason = "Not whole body";
			}
		}

		if (limitToFish) {
			if(Superclass!=null) {
				if (!Superclass.toLowerCase().contains("actinopterygii")) {
					er.keep = false;
					er.reason = "not a fish";
				} 
			} else {
				if(Test_organisms_species!=null) {
					if(!Test_organisms_species.equals("Gnathopogon coerulescens") && !Test_organisms_species.equals("Chasmichthys gulosus") && !Test_organisms_species.equals("Cyprinodontidae") && !Test_organisms_species.equals("Tilapia nilotica") && !Test_organisms_species.equals("Salmo gairdneri")){
						er.keep = false;
						er.reason = "not a fish";
					}
				}
			}
		}
	}
	
	private void setSpeciesParameters(Hashtable<String, List<Species>> htSpecies, boolean limitToFish,
			ExperimentalRecord er) {
		if(Test_organisms_species!=null) {
			er.experimental_parameters.put("Species latin", Test_organisms_species);
		}
		if(Species_common_name!=null) {
			er.experimental_parameters.put("Species common", Species_common_name);
			String supercategory=getSpeciesSupercategory(htSpecies);
			if(supercategory!=null)	{
				er.experimental_parameters.put("Species supercategory", supercategory);
			}
			
			if(limitToFish && supercategory!=null) {
				if(!supercategory.equals("Fish")) {
					er.keep=false;
					er.reason="Not a fish species";
				}
			}
		} else if(Superclass!=null && Superclass.toLowerCase().contains("actinopterygii")) {
			er.experimental_parameters.put("Species supercategory", "Fish");
			String supercategory="Fish";
			
			if(limitToFish && supercategory!=null) {
				if(!supercategory.equals("Fish")) {
					er.keep=false;
					er.reason="Not a fish species";
				}
			}
		}
	}
	
	private String getSpeciesSupercategory(Hashtable<String, List<Species>> htSpecies) {
		if(htSpecies!=null) {
			if(htSpecies.containsKey(Species_common_name.toLowerCase())) {

				List<Species>speciesList=htSpecies.get(Species_common_name.toLowerCase());

				for(Species species:speciesList) {

					if(species.species_supercategory.contains("fish")) {
						return "Fish";
					} else if(species.species_supercategory.contains("algae")) {
						return "Algae";
					} else if(species.species_supercategory.contains("crustaceans")) {
						return "Crustaceans";
					} else if(species.species_supercategory.contains("insects/spiders")) {
						return "Insects/spiders";
					} else if(species.species_supercategory.contains("molluscs")) {
						return "Molluscs";
					} else if(species.species_supercategory.contains("worms")) {
						return "Worms";
					} else if(species.species_supercategory.contains("invertebrates")) {
						return "Invertebrates";
					} else if(species.species_supercategory.contains("flowers, trees, shrubs, ferns")) {
						return "Flowers, trees, shrubs, ferns";
					} else if(species.species_supercategory.equals("omit")) {
						return "omit";
					} else {
						System.out.println("Handle\t"+Species_common_name+"\t"+species.species_supercategory);	
					}
				}
			} else {
				System.out.println("missing in hashtable:\t"+"*"+Species_common_name.toLowerCase()+"*");
			}
		}
		return null;
	}


	static class Species {
		Integer id;
		String species_common;
		String species_scientific;
		String species_supercategory;
		String habitat;
	}


	/**
	 * this works for prod_dsstox- not v93 version since species table is different
	 * 
	 * @param tvq
	 * @return
	 */
	public static Hashtable<String, List<Species>> createSupercategoryHashtable(Connection conn) {
		Hashtable<String,List<Species>>htSpecies=new Hashtable<>();

		String sql="select species_id, species_common, species_scientific, species_supercategory, habitat from species";

		try {

			Statement st = conn.createStatement();			
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {

				Species species=new Species();

				species.id=rs.getInt(1);
				species.species_common=rs.getString(2);
				species.species_scientific=rs.getString(3);
				species.species_supercategory=rs.getString(4);
				species.habitat=rs.getString(5);

				if(htSpecies.get(species.species_common)==null) {
					List<Species>speciesList=new ArrayList<>();
					speciesList.add(species);
					htSpecies.put(species.species_common, speciesList);
				} else {
					List<Species>speciesList=htSpecies.get(species.species_common);
					speciesList.add(species);
				}
			}


			//			System.out.println(sql);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return htSpecies;
	}

	void putEntry(Hashtable<String, List<Species>> htSpecies,String species_common,String supercategory) {

		if(htSpecies.get(species_common)==null) {
			List<Species>speciesList=new ArrayList<>();
			Species species=new Species();
			species.species_common=species_common;
			species.species_supercategory=supercategory;
			speciesList.add(species);
			htSpecies.put(species_common, speciesList);
		} else {
			List<Species>speciesList=htSpecies.get(species_common);

			Species species=new Species();
			species.species_common=species_common;
			species.species_supercategory=supercategory;
			speciesList.add(species);
		}


	}
}