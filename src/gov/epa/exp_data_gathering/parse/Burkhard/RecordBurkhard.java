package gov.epa.exp_data_gathering.parse.Burkhard;


import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.exp_data_gathering.parse.ExcelSourceReader;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
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
	
	
	private ExperimentalRecord toExperimentalRecordBAF(String propertyName) {
				
		ExperimentalRecord er=new ExperimentalRecord();
		er.experimental_parameters=new Hashtable<>();

		
		if (!isNumeric(Log_BAF_mean) && !isNumeric(Log_BAF_max)) {
			er.keep=false;
			er.reason="Missing BAF value";
		}
		
		er.property_name = propertyName;
		

//		er.property_value_units_original = Log_BAF_units;
		
		er.property_value_units_original = "Log10("+Log_BAF_units+")";
		
		er.property_value_units_original.replace("(L/kg-ww)", "(L/kg)");
		
//		er.experimental_parameters.put("study quality",Study_Quality_BAF);
		er.reliability=Study_Quality_BAF;
		
		try {
			
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
					&& Log_BAF_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
				er.property_value_string = propertyValue + " (arithmetic)";
				er.property_value_point_estimate_original = Math.log10(Double.parseDouble(Log_BAF_mean));
			} else {
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
		
		addMetadata(er);
		
		uc.convertRecord(er);
		
		return er;

	}

	
	
	ExperimentalRecord toExperimentalRecordBCF_Kinetic(String propertyName,boolean limitToWholeOrganism,boolean limitToFish) {

		String method="kinetic";
		String Log_BCF_units=Log_BCF_Kinetic_units;
		String Log_BCF_arithmetic_or_logarithmic=Log_BCF_Kinetic_arithmetic_or_logarithmic;
		String Log_BCF_min=Log_BCF_Kinetic_min;
		String Log_BCF_max=Log_BCF_Kinetic_max;
		String Log_BCF_mean=Log_BCF_Kinetic_mean;
						
		ExperimentalRecord er = toExperimentalRecordBCF(propertyName, method, Log_BCF_units,
				Log_BCF_arithmetic_or_logarithmic, Log_BCF_min, Log_BCF_max, Log_BCF_mean);
		
		if(er!=null) filterRecord(er, limitToWholeOrganism,limitToFish);
		return er;
	}


	private ExperimentalRecord toExperimentalRecordBCF(String propertyName, String method, String Log_BCF_units,
			String Log_BCF_arithmetic_or_logarithmic, String Log_BCF_min, String Log_BCF_max, String Log_BCF_mean) {
		
		if (!isNumeric(Log_BCF_mean) && !isNumeric(Log_BCF_max)) {
			return null;
		}

		
		ExperimentalRecord er=new ExperimentalRecord();
		er.experimental_parameters=new Hashtable<>();
		er.reliability=Study_Quality_BCF;
		er.experimental_parameters.put("Measurement method",method);
		er.property_name = propertyName;
		
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
		
		try {
			String property_value = Log_BCF_mean;
			
			
			if(Log_BCF_min!=null && Log_BCF_min.contains("<")) {
				System.out.println("min value has <");
				return null;
			}
			
			if ((Log_BCF_min!=null) && (Log_BCF_max!=null)) {
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
		addMetadata(er);
		
		uc.convertRecord(er);
		return er;
	}
	
	ExperimentalRecord toExperimentalRecordBCF_SS(String propertyName,boolean limitToWholeOrganism,boolean limitToFish) {
		
		String method="steady state";
		String Log_BCF_units=Log_BCF_Steady_State_units;
		String Log_BCF_arithmetic_or_logarithmic=Log_BCF_Steady_State_arithmetic_or_logarithmic;
		String Log_BCF_min=Log_BCF_Steady_State_min;
		String Log_BCF_max=Log_BCF_Steady_State_max;
		String Log_BCF_mean=Log_BCF_Steady_State_mean;
						
		ExperimentalRecord er = toExperimentalRecordBCF(propertyName, method, Log_BCF_units,
				Log_BCF_arithmetic_or_logarithmic, Log_BCF_min, Log_BCF_max, Log_BCF_mean);		
		if(er!=null) filterRecord(er, limitToWholeOrganism,limitToFish);
		return er;
	}
	
	
	private void filterRecord(ExperimentalRecord er,boolean limitToWholeOrganism,boolean limitToFish) {

		if (limitToWholeOrganism) {
			if (Tissue == null
					|| !Tissue.equals("whole body")) {
				er.keep = false;
				er.reason = "Not whole body";
			}
		}

		if (limitToFish) {
			if (!class_taxonomy.toLowerCase().contains("actinopteri")
					&& !class_taxonomy.toLowerCase().contains("actinopterygii")
					&& !class_taxonomy.toLowerCase().contains("teleostei")) {
				er.keep = false;
				er.reason = "not a fish";
			}
		}

		if (Location == null
				|| !Location.equals("laboratory")) {
			er.keep = false;
			er.reason = "Test location not in laboratory";
		}

		// if (er.experimental_parameters.get("method") == null
		// || !er.experimental_parameters.get("method").equals("steady state")) {
		// er.keep = false;
		// er.reason = "Not steady state";
		// }
		// if (er.experimental_parameters.get("media") == null
		// || !er.experimental_parameters.get("media").equals("freshwater")) {
		// er.keep = false;
		// er.reason = "Not steady state";
		// }
		// if(!er.keep) {
		// JsonObject jo=new JsonObject();
		// jo.addProperty("reason", er.reason);
		// jo.addProperty("property_name", er.property_name);
		// jo.addProperty("tissue", er.experimental_parameters.get("tissue")+"");
		// jo.addProperty("method", er.experimental_parameters.get("method")+"");
		// jo.addProperty("media", er.experimental_parameters.get("media")+"");
		// jo.addProperty("exposure_type",
		// er.experimental_parameters.get("exposure_type")+"");
		// System.out.println(gson.toJson(jo));
		// }


	}
	

	private void addMetadata(ExperimentalRecord er) {
		
		er.dsstox_substance_id = DTXSID;
		er.source_name = sourceName;
		er.chemical_name = Chemical;
		er.casrn = CASRN;
		if(!er.casrn.contains("-")) er.casrn=null;

		er.literatureSource=new LiteratureSource();
		er.literatureSource.citation=Reference;
//		er.literatureSource.name=Reference;
		er.reference=Reference;
		
		Marine_Brackish_Freshwater=Marine_Brackish_Freshwater.toLowerCase();

		if (Exposure_Concentrations!=null && !Exposure_Concentrations.isBlank())
			er.experimental_parameters.put("Exposure concentration",Exposure_Concentrations);
		er.experimental_parameters.put("Response site",Tissue);
		er.experimental_parameters.put("Media type",Marine_Brackish_Freshwater);
		er.experimental_parameters.put("Test location",Location);
		er.experimental_parameters.put("Species latin",species_taxonomy);
		er.experimental_parameters.put("Species common",Common_Name);
//		er.experimental_parameters.put("Class taxonomy",class_taxonomy);
		
		
	}

}