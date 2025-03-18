package gov.epa.exp_data_gathering.parse.Burkhard;


import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.ParameterValue;
import gov.epa.exp_data_gathering.parse.UnitConverter;




public class RecordBurkhard {
	
	transient Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	static transient UnitConverter uc = new UnitConverter("Data" + File.separator + "density.txt");

	
	public String Chemical;
	public String CASRN;
	public String Abbreviation;
	public String DTXSID;
	public String Log_BCF_Steady_State_mean;
	public String Log_BCF_Steady_State_stdev;
	public String Log_BCF_Steady_State_min;
	public String Log_BCF_Steady_State_max;
	public String Log_BCF_Steady_State_lower_CI;
	public String Log_BCF_Steady_State_upper_CI;
	public String Log_BCF_Steady_State_arithmetic_or_logarithmic;
	public String Log_BCF_Steady_State_units;
	public String field12;
	public String Log_BCF_Kinetic_mean;
	public String Log_BCF_Kinetic_stdev;
	public String Log_BCF_Kinetic_min;
	public String Log_BCF_Kinetic_max;
	public String Log_BCF_Kinetic_lower_CI;
	public String Log_BCF_Kinetic_upper_CI;
	public String Log_BCF_Kinetic_arithmetic_or_logarithmic;
	public String Log_BCF_Kinetic_units;
	public String field21;
	public String BCF_Adjustment_DW_to_WW;
	public String Finalized_log10_BCF_L_kg_ww;
	public String field24;
	public String Log_BAF_mean;
	public String Log_BAF_stdev;
	public String Log_BAF_min;
	public String Log_BAF_max;
	public String Log_BAF_lower_CI;
	public String Log_BAF_upper_CI;
	public String Log_BAF_arithmetic_or_logarithmic;
	public String Log_BAF_units;
	public String field33;
	public String BAF_Adjustment_DW_to_WW;
	public String Finalized_log10_BAF_L_kg_ww;
	public String field36;
	public String Measured_Trophic_Level;
	public String field38;
	public String field39;
	public String Estimated_Trophic_Level;
	public String Common_Name;
	public String Species_Latin_Name;
	public String Species_Weight_g;
	public String field44;
	public String field45;
	public String field46;
	public String Tissue;
	public String Location;
	public String Reference;
	public String BCF_data;
	public String OECD_305;
	public String ku;
	public String ku_sd;
	public String field54;
	public String days_of_uptake;
	public String ke;
	public String ke_sd;
	public String field58;
	public String days_of_elimination;
	public String half_life_days;
	public String half_life_sd_days;
	public String SS;
	public String Kinetic;
	public String Modeled;
	public String Exposure_Concentrations;
	public String Study_Quality_BCF;
	public String Comments_BCF;
	public String k_dietary;
	public String k_dietary_sd;
	public String field70;
	public String Assimulation_Efficiency;
	public String Assimulation_Efficiency_SD;
	public String field73;
	public String BAF;
	public String BAF_of_water_samples;
	public String BAF_start_date_for_water_sampling;
	public String BAF_end_date_for_water_sampling;
	public String field78;
	public String BAF_of_biota_samples;
	public String BAF_start_date_for_biota_sampling;
	public String BAF_end_date_for_biota_sampling;
	public String Average_weight_g;
	public String Average_Length_cm;
	public String Sex_F_M;
	public String Average_Age;
	public String Age_sd;
	public String General_experimental_design;
	public String Water_Biota_spatial_coordination;
	public String Water_Biota_temporal_coordination;
	public String Number_Biota_Samples;
	public String Number_of_Water;
	public String Study_Quality_BAF;
	public String Comments_BAFs;
	public String field94;
	public String Mixture_Exposure;
	public String Concentration_in_Biota_mean;
	public String Concentration_in_Biota_stdev;
	public String Concentration_in_Biota_units;
	public String Concentration_in_Biota_MDL;
	public String Comments_Biota_Samples;
	public String Concentration_in_Water_mean;
	public String Concentration_in_Water_stdev;
	public String Concentration_in_Water_units;
	public String Concentration_in_Water_MDL;
	public String Comments_Environmental_Media;
	public String field106;
	public String Marine_Brackish_Freshwater;
	public String Comments_Marine_Brackish_Freshwater;
	public String field109;
	public String Taxonomy_Name_Level;
	public String kingdom_taxonomy;
	public String phylum_taxonomy;
	public String class_taxonomy;
	public String order_taxonomy;
	public String family_taxonomy;
	public String genus_taxonomy;
	public String species_taxonomy;
	public String ITIS_TaxID;
	public String NCBI_TaxID;
	public String GBIF_TaxID;
	public String Common_Name_NCBI_ITIS;
	public String Taxomony_Name_Source;
	public String field123;
	public static final String[] fieldNames = {"Chemical","CASRN","Abbreviation","DTXSID","Log_BCF_Steady_State_mean","Log_BCF_Steady_State_stdev","Log_BCF_Steady_State_min","Log_BCF_Steady_State_max","Log_BCF_Steady_State_lower_CI","Log_BCF_Steady_State_upper_CI","Log_BCF_Steady_State_arithmetic_or_logarithmic","Log_BCF_Steady_State_units","field12","Log_BCF_Kinetic_mean","Log_BCF_Kinetic_stdev","Log_BCF_Kinetic_min","Log_BCF_Kinetic_max","Log_BCF_Kinetic_lower_CI","Log_BCF_Kinetic_upper_CI","Log_BCF_Kinetic_arithmetic_or_logarithmic","Log_BCF_Kinetic_units","field21","BCF_Adjustment_DW_to_WW","Finalized_log10_BCF_L_kg_ww","field24","Log_BAF_mean","Log_BAF_stdev","Log_BAF_min","Log_BAF_max","Log_BAF_lower_CI","Log_BAF_upper_CI","Log_BAF_arithmetic_or_logarithmic","Log_BAF_units","field33","BAF_Adjustment_DW_to_WW","Finalized_log10_BAF_L_kg_ww","field36","Measured_Trophic_Level","field38","field39","Estimated_Trophic_Level","Common_Name","Species_Latin_Name","Species_Weight_g","field44","field45","field46","Tissue","Location","Reference","BCF_data","OECD_305","ku","ku_sd","field54","days_of_uptake","ke","ke_sd","field58","days_of_elimination","half_life_days","half_life_sd_days","SS","Kinetic","Modeled","Exposure_Concentrations","Study_Quality_BCF","Comments_BCF","k_dietary","k_dietary_sd","field70","Assimulation_Efficiency","Assimulation_Efficiency_SD","field73","BAF","BAF_of_water_samples","BAF_start_date_for_water_sampling","BAF_end_date_for_water_sampling","field78","BAF_of_biota_samples","BAF_start_date_for_biota_sampling","BAF_end_date_for_biota_sampling","Average_weight_g","Average_Length_cm","Sex_F_M","Average_Age","Age_sd","General_experimental_design","Water_Biota_spatial_coordination","Water_Biota_temporal_coordination","Number_Biota_Samples","Number_of_Water","Study_Quality_BAF","Comments_BAFs","field94","Mixture_Exposure","Concentration_in_Biota_mean","Concentration_in_Biota_stdev","Concentration_in_Biota_units","Concentration_in_Biota_MDL","Comments_Biota_Samples","Concentration_in_Water_mean","Concentration_in_Water_stdev","Concentration_in_Water_units","Concentration_in_Water_MDL","Comments_Environmental_Media","field106","Marine_Brackish_Freshwater","Comments_Marine_Brackish_Freshwater","field109","Taxonomy_Name_Level","kingdom_taxonomy","phylum_taxonomy","class_taxonomy","order_taxonomy","family_taxonomy","genus_taxonomy","species_taxonomy","ITIS_TaxID","NCBI_TaxID","GBIF_TaxID","Common_Name_NCBI_ITIS","Taxomony_Name_Source","field123"};

	public static final String lastUpdated = "08/02/2021";
	public static final String sourceName = "Burkhard"; // TODO Consider creating ExperimentalConstants.strSourceBurkhard2 instead.

	private static final String fileName = "BurkhardPFAS_BCF_BAF_DATA_with DTXSIDs.xlsx";

	public static Vector<JsonObject> parseBurkhardRecordsFromExcel() {
		
		
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
//		System.out.println("here");
		Vector<JsonObject> records = esr.parseRecordsFromExcel(0,true); // TODO Chemical name index guessed from header. Is this accurate?
		return records;
	}
	
	
	boolean isNumeric(String value) {

		if (value==null) return false;
		
		String v=value.toLowerCase().trim();

		if(v.isBlank() || v.equals("-") || v.equals("n.d.") || v.equals("na") || v.equals("--") || v.equals("n/a") || 
				v.equals("nd") || v.equals("?") || v.equals("n.c.") || 
				v.equals("<lod") || v.equals("nc") || v.equals("n.a.") || v.equals("n.a") || v.equals("na*")) {
			return false;
		} else {
			return true;
		}

	}
	
	
	ExperimentalRecord toExperimentalRecordBAF(String propertyName, Hashtable<String, List<Species>> htSpecies) {
		
		boolean limitToFish=false;
		if(propertyName.toLowerCase().contains("fish")) limitToFish=true;
		
		boolean limitToWholeBody=false;
		if(propertyName.toLowerCase().contains("whole")) limitToWholeBody=true;
		
		ExperimentalRecord er=new ExperimentalRecord();
		er.experimental_parameters=new Hashtable<>();
		er.parameter_values=new ArrayList<>();
		
		if(Study_Quality_BAF!=null) {
			er.experimental_parameters.put("Reliability", Study_Quality_BAF);
		}
//		er.reliability=Study_Quality_BAF;

		
		if (!isNumeric(Log_BAF_mean) && !isNumeric(Log_BAF_max)) {
			er.keep=false;
			er.reason="Missing BAF value";
		}
		
		er.property_name = propertyName;
		setSpeciesParameters(htSpecies, limitToFish, er);	
		if(limitToWholeBody) {
			if (Tissue == null || !Tissue.toLowerCase().contains("whole body")) {
				er.keep = false;
				er.reason = "Not whole body";
			}
		}
		
		if(Log_BAF_units!=null && Log_BAF_units.toLowerCase().contains("kg-ww")) {
			Log_BAF_units=Log_BAF_units.toLowerCase().replace("l/kg-ww", "L/kg");
			er.updateNote("value based on wet weight");
		}
		
		if(Log_BAF_units!=null && Log_BAF_units.toLowerCase().contains("kg-dw")) {
			Log_BAF_units=Log_BAF_units.toLowerCase().replace("l/kg-dw", "L/kg");
			er.updateNote("value based on dry weight");
		}	
//		er.property_value_units_original = Log_BAF_units;
//		er.property_value_units_original = "log10("+Log_BAF_units+")";
//		Log_BAF_units=Log_BAF_units.replace("L/kg-ww", "L/kg");
//		Log_BAF_units=Log_BAF_units.replace("L/kg-dw", "L/kg");
		if (Log_BAF_arithmetic_or_logarithmic!=null && Log_BAF_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
			er.property_value_units_original = Log_BAF_units;
		} else {
			er.property_value_units_original = Log_BAF_units;
			er.property_value_units_original="log10("+er.property_value_units_original+")";
		}
		
//		if(Log_BAF_units.contains("L/kg")) {
//			er.property_value_units_original.equals(ExperimentalConstants.str_LOG_L_KG);
//		} else {
//			er.property_value_units_original = Log_BAF_units;
//		}
		
//		er.experimental_parameters.put("study quality",Study_Quality_BAF);
		
		try {
			if(Log_BAF_mean!=null) {
				Log_BAF_mean=Log_BAF_mean.replace("?", "--");
			}
			String propertyValue = Log_BAF_mean;
			
			if (Log_BAF_min!=null && Log_BAF_max!=null) {
				String propertyValue2 = Log_BAF_min + "~" + Log_BAF_max;

				if ((Log_BAF_arithmetic_or_logarithmic!=null)
						&& Log_BAF_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
					er.property_value_string = propertyValue2 + " (arithmetic)";

					er.property_value_max_original = Math.log10(Double.parseDouble(Log_BAF_min));
					er.property_value_min_original = Math.log10(Double.parseDouble(Log_BAF_max));
				} else {
					er.property_value_max_original = Double.parseDouble(Log_BAF_min);
					er.property_value_min_original = Double.parseDouble(Log_BAF_max);
					er.property_value_string = propertyValue2 + " (log)";

				}

			} else if (Log_BAF_arithmetic_or_logarithmic!=null
					&& Log_BAF_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic") && !Log_BAF_mean.toLowerCase().contains("na" ) && !Log_BAF_mean.toLowerCase().contains("n.a" ) && !Log_BAF_mean.toLowerCase().contains("n/a" ) && !Log_BAF_mean.toLowerCase().contains("n.d" ) && !Log_BAF_mean.toLowerCase().contains("nc") && !Log_BAF_mean.toLowerCase().contains("nd") && !Log_BAF_mean.contains("--" ) && !Log_BAF_mean.toLowerCase().contains("n.c" ) && !Log_BAF_mean.equals("-") && !Log_BAF_mean.contains("LOD")) {
				er.property_value_string = propertyValue + " (arithmetic)";
				er.property_value_point_estimate_original = Math.log10(Double.parseDouble(Log_BAF_mean));
			} else if(Log_BAF_mean!=null && !Log_BAF_mean.toLowerCase().contains("na" ) && !Log_BAF_mean.toLowerCase().contains("n.a" ) && !Log_BAF_mean.toLowerCase().contains("n/a" ) && !Log_BAF_mean.toLowerCase().contains("n.d" ) && !Log_BAF_mean.toLowerCase().contains("nc") && !Log_BAF_mean.toLowerCase().contains("nd") && !Log_BAF_mean.contains("--" ) && !Log_BAF_mean.toLowerCase().contains("n.c" ) && !Log_BAF_mean.equals("-") && !Log_BAF_mean.contains("LOD")){
				if (Log_BAF_mean.contains("<") || Log_BAF_mean.contains(">")) {
					er.property_value_numeric_qualifier=Log_BAF_mean.substring(0,1);
					Log_BAF_mean=Log_BAF_mean.substring(1,Log_BAF_mean.length());
				}
				
				er.property_value_point_estimate_original = Double.parseDouble(Log_BAF_mean);
				er.property_value_string = propertyValue + " (log)";

			}
			
			
		} catch (Exception e) {
			System.out.println("Parse error BAF:\n"+gson.toJson(this));
			e.printStackTrace();
		}
		
		if ((Study_Quality_BAF!=null && Study_Quality_BAF.toLowerCase().contains("low")) || (Study_Quality_BCF!=null)) {
			er.keep = false;
			er.reason = "untrusted study";
		}
		
		addMetadata(er, htSpecies);
		
		uc.convertRecord(er);
		
		return er;

	}

	
	
	ExperimentalRecord toExperimentalRecordBCF_Kinetic(String propertyName, Hashtable<String, List<Species>> htSpecies) {

		String method="kinetic";
		String Log_BCF_units=Log_BCF_Kinetic_units;
		String Log_BCF_arithmetic_or_logarithmic=Log_BCF_Kinetic_arithmetic_or_logarithmic;
		String Log_BCF_min=Log_BCF_Kinetic_min;
		String Log_BCF_max=Log_BCF_Kinetic_max;
		String Log_BCF_mean=Log_BCF_Kinetic_mean;
						
		ExperimentalRecord er = toExperimentalRecordBCF(propertyName, method, Log_BCF_units,
				Log_BCF_arithmetic_or_logarithmic, Log_BCF_min, Log_BCF_max, Log_BCF_mean, htSpecies);
		
//		if(er!=null) filterRecord(er, propertyName);
		return er;
	}


	private ExperimentalRecord toExperimentalRecordBCF(String propertyName, String method, String Log_BCF_units,
			String Log_BCF_arithmetic_or_logarithmic, String Log_BCF_min, String Log_BCF_max, String Log_BCF_mean, Hashtable<String, List<Species>> htSpecies) {
		
		if (!isNumeric(Log_BCF_mean) && !isNumeric(Log_BCF_max)) {
			return null;
		}

		boolean limitToFish=false;
		if(propertyName.toLowerCase().contains("fish")) limitToFish=true;
		
		boolean limitToWholeBody=false;
		if(propertyName.toLowerCase().contains("whole")) limitToWholeBody=true;
		
		ExperimentalRecord er=new ExperimentalRecord();
		er.experimental_parameters=new Hashtable<>();
		er.experimental_parameters.put("Reliability", Study_Quality_BCF);
		er.experimental_parameters.put("Measurement method",method);
		er.property_name = propertyName;
		er.parameter_values=new ArrayList<>();
		
		if(Log_BCF_units.contains("kg-ww")) {
			Log_BCF_units=Log_BCF_units.replace("kg-ww", "kg");
			er.updateNote("value based on wet weight");
		}		
		
		if (Log_BCF_arithmetic_or_logarithmic!=null && Log_BCF_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
			er.property_value_units_original = Log_BCF_units;
		} else {
			er.property_value_units_original = Log_BCF_units;
			er.property_value_units_original="log10("+er.property_value_units_original+")";
		}
				
//		er.experimental_parameters.put("study quality",Study_Quality_BCF);
		setSpeciesParameters(htSpecies, limitToFish, er);	
		if(limitToWholeBody) {
			if (Tissue == null || !Tissue.toLowerCase().contains("whole body")) {
				er.keep = false;
				er.reason = "Not whole body";
			}
		}
		
		if (Location == null
				|| !Location.equals("laboratory")) {
			er.keep = false;
			er.reason = "Test location not in laboratory";
		}
		
		try {
			String property_value = Log_BCF_mean;
			
			
			if(Log_BCF_min!=null && Log_BCF_min.contains("<")) {
//				System.out.println("min value has <");
				return null;
			}
			
			if ((Log_BCF_min!=null) && (Log_BCF_max!=null)) {//in burkhard only have matching pairs of min and max
				property_value = Log_BCF_min + "~" + Log_BCF_max;
				er.property_value_max_original = Double.parseDouble(Log_BCF_max);
				er.property_value_min_original = Double.parseDouble(Log_BCF_min);
			}
				
			if (Log_BCF_mean != null) {
				if (Log_BCF_mean.contains("<") || Log_BCF_mean.contains(">")) {
					er.property_value_numeric_qualifier = Log_BCF_mean.substring(0, 1);
					Log_BCF_mean = Log_BCF_mean.substring(1, Log_BCF_mean.length()).trim();
					er.property_value_point_estimate_original = Double.parseDouble(Log_BCF_mean);
				} else {
					er.property_value_point_estimate_original = Double.parseDouble(Log_BCF_mean);
				}
			}
		
			er.property_value_string = property_value + " "+er.property_value_units_original;				

		} catch (Exception e) {
			System.out.println("Parse error BCF:\n"+gson.toJson(this));
			e.printStackTrace();
		}
		
		if (Study_Quality_BCF!=null && Study_Quality_BCF.toLowerCase().contains("low")) {
			er.keep = false;
			er.reason = "untrusted study";
		}
		er.property_category="bioconcentration";
		addMetadata(er, htSpecies);
		
		uc.convertRecord(er);
		return er;
	}
	
	ExperimentalRecord toExperimentalRecordBCF_SS(String propertyName, Hashtable<String, List<Species>> htSpecies) {
		
		String method="steady state";
		String Log_BCF_units=Log_BCF_Steady_State_units;
		String Log_BCF_arithmetic_or_logarithmic=Log_BCF_Steady_State_arithmetic_or_logarithmic;
		String Log_BCF_min=Log_BCF_Steady_State_min;
		String Log_BCF_max=Log_BCF_Steady_State_max;
		String Log_BCF_mean=Log_BCF_Steady_State_mean;
						
		ExperimentalRecord er = toExperimentalRecordBCF(propertyName, method, Log_BCF_units,
				Log_BCF_arithmetic_or_logarithmic, Log_BCF_min, Log_BCF_max, Log_BCF_mean, htSpecies);		
//		if(er!=null) filterRecord(er, propertyName);
		return er;
	}
	
	static class Species {
		Integer id;
		String species_common;
		String species_scientific;
		String species_supercategory;
		String habitat;
	}
	
	private String getSpeciesSupercategory(Hashtable<String, List<Species>> htSpecies) {

		if(Common_Name!=null && htSpecies.containsKey(Common_Name.toLowerCase())) {
			
				List<Species>speciesList=htSpecies.get(Common_Name.toLowerCase());

				for(Species species:speciesList) {


					//				if(species.species_scientific!=null) {
					//					if (!species.species_scientific.toLowerCase().equals(this.scientific_name.toLowerCase())) {
					//						System.out.println(this.scientific_name+"\t"+species.species_scientific+"\tmismatch");
					//					}
					//				} else {
					////					System.out.println(common_name+"\tspecies has null scientific");
					//				}

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
					} else if(species.species_supercategory.contains("microorganisms")) {
						return "microorganisms";
					} else if(species.species_supercategory.contains("amphibians")) {
						return "amphibians";
					} else if(species.species_supercategory.equals("omit")) {
						return "omit";
					}
				}
			} else if(htSpecies.containsKey(Species_Latin_Name.toLowerCase())) {

				List<Species>speciesList=htSpecies.get(Species_Latin_Name.toLowerCase());

				for(Species species:speciesList) {


					//				if(species.species_scientific!=null) {
					//					if (!species.species_scientific.toLowerCase().equals(this.scientific_name.toLowerCase())) {
					//						System.out.println(this.scientific_name+"\t"+species.species_scientific+"\tmismatch");
					//					}
					//				} else {
					////					System.out.println(common_name+"\tspecies has null scientific");
					//				}

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
					} else if(species.species_supercategory.contains("microorganisms")) {
						return "microorganisms";
					} else if(species.species_supercategory.contains("amphibians")) {
						return "amphibians";
					} else if(species.species_supercategory.equals("reptiles")) {
						return "reptiles";
					}else if(species.species_supercategory.equals("omit")) {
						return "omit";
					} else if(class_taxonomy.contains("Teleostei") || class_taxonomy.contains("Actinoper")){
						return "Fish";	
					} else {
						System.out.println("Handle\t"+Species_Latin_Name+"\t"+species.species_supercategory);	
					}
				}
			} else {
				System.out.println("missing in hashtable:\t"+"*"+Species_Latin_Name.toLowerCase()+"*");
			}
//		}
	return null;
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

	
	private void setSpeciesParameters(Hashtable<String, List<Species>> htSpecies, boolean limitToFish, ExperimentalRecord er) {
		
		er.experimental_parameters.put("Species latin",Species_Latin_Name);
		if(Common_Name!=null) {
			er.experimental_parameters.put("Species common",Common_Name);
		}
		String supercategory=getSpeciesSupercategory(htSpecies);
		if(supercategory!=null)	er.experimental_parameters.put("Species supercategory", supercategory);
		
		if(limitToFish && supercategory!=null) {
			if(!supercategory.equals("Fish")) {
				er.keep=false;
				er.reason="Not a fish species";
			}
		}

	}
	
	private void addMetadata(ExperimentalRecord er, Hashtable<String, List<Species>> htSpecies) {
		
		er.dsstox_substance_id = DTXSID;
		er.source_name = sourceName;
		er.chemical_name = Chemical;
		er.casrn = CASRN;
		if(!er.casrn.contains("-")) er.casrn=null;

		er.literatureSource=new LiteratureSource();
		er.literatureSource.citation=Reference;
		er.literatureSource.name=Reference;
		er.reference=Reference;
		
		Marine_Brackish_Freshwater=Marine_Brackish_Freshwater.toLowerCase();
		
		setWaterConcentration(er);
//		er.experimental_parameters.put("Exposure concentration",Exposure_Concentrations);
		er.experimental_parameters.put("Response site",Tissue);
		er.experimental_parameters.put("Media type",Marine_Brackish_Freshwater);
		er.experimental_parameters.put("Test location",Location);
//		er.experimental_parameters.put("Class taxonomy",class_taxonomy);
		if(days_of_uptake!=null) {
			days_of_uptake=days_of_uptake.replace("24 hr", "1");
			ParameterValue pv=new ParameterValue();
			pv.parameter.name="Exposure duration";
			pv.unit.abbreviation="days";
			pv.valuePointEstimate=Double.parseDouble(days_of_uptake);
			er.parameter_values.add(pv);
		}
		
	}

	private void setWaterConcentration(ExperimentalRecord er) {
		if (Exposure_Concentrations!=null && !Exposure_Concentrations.isBlank()) {
			ParameterValue pv=new ParameterValue();
			pv.parameter.name="Water Concentration";
//			pv.unit.abbreviation=ExperimentalConstants.str_g_L;
			int unitsIndex = -1;
			
			Exposure_Concentrations=Exposure_Concentrations.replace("total concentration ", "");
			Exposure_Concentrations=Exposure_Concentrations.replace("0.3, 1, 3, 10 & 30 ug/L (0.04, 0.14, 0.42, 1.4 and 4.2 uCi 14C-PFOA/L)", "0.3 to 30 ug/L");
			Exposure_Concentrations=Exposure_Concentrations.replace("mixture ", "");
			Exposure_Concentrations=Exposure_Concentrations.replace("Angus AFFF foam at 1000 ug/L", "1000 ug/L");
			Exposure_Concentrations=Exposure_Concentrations.replace("3M AFFF foam at 1000 ug/L  PFDS: 15 ± 8 ng/L", "1000 ug/L");
			Exposure_Concentrations=Exposure_Concentrations.replace("3M AFFF foam at 1000 ug/L  PFOS: 2.4 ± 1 ug/L", "1000 ug/L");
			Exposure_Concentrations=Exposure_Concentrations.replace("3M AFFF foam at 1000 ug/L  PFHxS: 178 ± 58 ng/L", "1000 ug/L");
			Exposure_Concentrations=Exposure_Concentrations.replace("3M AFFF foam at 1000 ug/L", "1000 ug/L");
			Exposure_Concentrations=Exposure_Concentrations.replace("100ug/L nominal  WebPlotDigitizer to get residues", "100 ug/L");
			Exposure_Concentrations=Exposure_Concentrations.replace("? ","");
			if(Exposure_Concentrations.contains(" to ")) {
				unitsIndex = Exposure_Concentrations.indexOf("g/L")-1;
				String value=Exposure_Concentrations.substring(0, unitsIndex);
				String[] range = value.split(" to ");
				if(Exposure_Concentrations.contains("ng/L")) {
					pv.valueMin=Double.parseDouble(range[0])/1e9;
					pv.valueMax=Double.parseDouble(range[1])/1e9;
				} else if(Exposure_Concentrations.contains("ug/L")){
					pv.valueMin=Double.parseDouble(range[0])/1e6;
					pv.valueMax=Double.parseDouble(range[1])/1e6;
				}
			} else if(Exposure_Concentrations.contains("±")) {
				Exposure_Concentrations=Exposure_Concentrations.replace(" ± ", "±");
				unitsIndex = Exposure_Concentrations.indexOf("g/L")-1;
				String value=Exposure_Concentrations.substring(0, unitsIndex);
				String[] range = value.split("±");
				if(Exposure_Concentrations.contains("ng/L")) {
					pv.valueMin=(Double.parseDouble(range[0])-Double.parseDouble(range[1]))/1e9;
					pv.valueMax=(Double.parseDouble(range[0])+Double.parseDouble(range[1]))/1e9;
				} else if(Exposure_Concentrations.contains("ug/L")){
					pv.valueMin=(Double.parseDouble(range[0])-Double.parseDouble(range[1]))/1e6;
					pv.valueMax=(Double.parseDouble(range[0])+Double.parseDouble(range[1]))/1e6;
				}
			} else {
				if(Exposure_Concentrations.contains("ng/L")) {
					unitsIndex = Exposure_Concentrations.indexOf("ng/L");
					String value=Exposure_Concentrations.substring(0, unitsIndex);
					pv.valuePointEstimate=Double.parseDouble(value)/1e9;
					pv.unit.abbreviation=ExperimentalConstants.str_g_L;
//					System.out.println(value +" ng/L "+ pv.valuePointEstimate + " g/L");
				} else if(Exposure_Concentrations.contains("mg/L")) {
					unitsIndex = Exposure_Concentrations.indexOf("mg/L");
					String value=Exposure_Concentrations.substring(0, unitsIndex);
					pv.valuePointEstimate=Double.parseDouble(value)/1000;
					pv.unit.abbreviation=ExperimentalConstants.str_g_L;
//					System.out.println(value +" mg/L "+ pv.valuePointEstimate + " g/L");
				} else if(Exposure_Concentrations.contains("ug/g")) {
					unitsIndex = Exposure_Concentrations.indexOf("ug/g");
					String value=Exposure_Concentrations.substring(0, unitsIndex);
					pv.valuePointEstimate=Double.parseDouble(value)/1000;
					pv.unit.abbreviation=ExperimentalConstants.str_g_L;
				} else if(Exposure_Concentrations.contains("ug/L")){
					unitsIndex = Exposure_Concentrations.indexOf("ug/L");
					String value=Exposure_Concentrations.substring(0, unitsIndex);
					pv.valuePointEstimate=Double.parseDouble(value)/1e6;
					pv.unit.abbreviation=ExperimentalConstants.str_g_L;
//					System.out.println(value +" ug/L "+ pv.valuePointEstimate + " g/L");
				} else if(Exposure_Concentrations.contains("mL")) {
					unitsIndex = Exposure_Concentrations.indexOf("mL");
					String value=Exposure_Concentrations.substring(0, unitsIndex);
					pv.valuePointEstimate=Double.parseDouble(value);
					pv.unit.abbreviation="mL";
				} else if(Exposure_Concentrations.contains("nM")) {
					unitsIndex = Exposure_Concentrations.indexOf("nM");
					String value=Exposure_Concentrations.substring(0, unitsIndex);
					pv.valuePointEstimate=Double.parseDouble(value);
					pv.unit.abbreviation=ExperimentalConstants.str_nM;
				} else {
					try {
						pv.valuePointEstimate=Double.parseDouble(Exposure_Concentrations);
					} catch(NumberFormatException e) {
						pv.valueText=Exposure_Concentrations;
//						System.out.println("Units not handled:	" + Exposure_Concentrations);
//					return;
					}
				}
			}
			er.parameter_values.add(pv);
		}
	}
	
}