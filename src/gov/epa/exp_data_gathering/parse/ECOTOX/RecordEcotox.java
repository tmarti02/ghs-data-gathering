package gov.epa.exp_data_gathering.parse.ECOTOX;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.ToxVal.ToxValRecord;

/**
 * @author TMARTI02
 */
public class RecordEcotox {

	public String property_name;
	
	public String test_id;
	public String reference_number;
	public String test_cas;
	public String test_grade;
	public String test_grade_comments;
	public String test_formulation;
	public String test_formulation_comments;
	public String test_radiolabel;
	public String test_radiolabel_comments;
	public String test_purity_mean_op;
	public String test_purity_mean;
	public String test_purity_min_op;
	public String test_purity_min;
	public String test_purity_max_op;
	public String test_purity_max;
	public String test_purity_comments;
	public String test_characteristics;
	public String species_number;
	public String organism_habitat;
	public String organism_source;
	public String organism_source_comments;
	public String organism_lifestage;
	public String organism_lifestage_comments;
	public String organism_age_mean_op;
	public String organism_age_mean;
	public String organism_age_min_op;
	public String organism_age_min;
	public String organism_age_max_op;
	public String organism_age_max;
	public String organism_age_unit;
	public String organism_init_wt_mean_op;
	public String organism_init_wt_mean;
	public String organism_init_wt_min_op;
	public String organism_init_wt_min;
	public String organism_init_wt_max_op;
	public String organism_init_wt_max;
	public String organism_init_wt_unit;
	public String organism_length_mean_op;
	public String organism_length_mean;
	public String organism_length_min_op;
	public String organism_length_min;
	public String organism_length_max_op;
	public String organism_length_max;
	public String organism_length_type;
	public String organism_length_unit;
	public String organism_strain;
	public String organism_characteristics;
	public String organism_gender;
	public String experimental_design;
	public String study_duration_mean_op;
	public String study_duration_mean;
	public String study_duration_min_op;
	public String study_duration_min;
	public String study_duration_max_op;
	public String study_duration_max;
	public String study_duration_unit;
	public String study_duration_comments;
	public String exposure_duration_mean_op;
	public String exposure_duration_mean;
	public String exposure_duration_min_op;
	public String exposure_duration_min;
	public String exposure_duration_max_op;
	public String exposure_duration_max;
	public String exposure_duration_unit;
	public String exposure_duration_comments;
	public String study_type;
	public String study_type_comments;
	public String test_type;
	public String test_type_comments;
	public String test_location;
	public String test_location_comments;
	public String test_method;
	public String test_method_comments;
	public String exposure_type;
	public String exposure_type_comments;
	public String control_type;
	public String control_type_comments;
	public String media_type;
	public String media_type_comments;
	public String num_doses_mean_op;
	public String num_doses_mean;
	public String num_doses_min_op;
	public String num_doses_min;
	public String num_doses_max_op;
	public String num_doses_max;
	public String num_doses_comments;
	public String other_effect_comments;
	public String application_freq_mean_op;
	public String application_freq_mean;
	public String application_freq_min_op;
	public String application_freq_min;
	public String application_freq_max_op;
	public String application_freq_max;
	public String application_freq_unit;
	public String application_freq_comments;
	public String application_type;
	public String application_type_comments;
	public String application_rate;
	public String application_rate_unit;
	public String application_date;
	public String application_date_comments;
	public String application_season;
	public String application_season_comments;
	public String subhabitat;
	public String subhabitat_description;
	public String substrate;
	public String substrate_description;
	public String water_depth_mean_op;
	public String water_depth_mean;
	public String water_depth_min_op;
	public String water_depth_min;
	public String water_depth_max_op;
	public String water_depth_max;
	public String water_depth_unit;
	public String water_depth_comments;
	public String geographic_code;
	public String geographic_location;
	public String latitude;
	public String longitude;
	public String halflife_mean_op;
	public String halflife_mean;
	public String halflife_min_op;
	public String halflife_min;
	public String halflife_max_op;
	public String halflife_max;
	public String halflife_unit;
	public String halflife_comments;
	public String published_date;
	public String result_id;
	public String sample_size_mean_op;
	public String sample_size_mean;
	public String sample_size_min_op;
	public String sample_size_min;
	public String sample_size_max_op;
	public String sample_size_max;
	public String sample_size_unit;
	public String sample_size_comments;
	public String obs_duration_mean_op;
	public String obs_duration_mean;
	public String obs_duration_min_op;
	public String obs_duration_min;
	public String obs_duration_max_op;
	public String obs_duration_max;
	public String obs_duration_unit;
	public String obs_duration_comments;
	public String endpoint;
	public String endpoint_comments;
	public String trend;
	public String effect;
	public String effect_comments;
	public String measurement;
	public String measurement_comments;
	public String response_site;
	public String response_site_comments;
	public String effect_pct_mean_op;
	public String effect_pct_mean;
	public String effect_pct_min_op;
	public String effect_pct_min;
	public String effect_pct_max_op;
	public String effect_pct_max;
	public String effect_pct_comments;
	public String conc1_type;
	public String ion1;
	public String conc1_mean_op;
	public String conc1_mean;
	public String conc1_min_op;
	public String conc1_min;
	public String conc1_max_op;
	public String conc1_max;
	public String conc1_unit;
	public String conc1_comments;
	public String conc2_type;
	public String ion2;
	public String conc2_mean_op;
	public String conc2_mean;
	public String conc2_min_op;
	public String conc2_min;
	public String conc2_max_op;
	public String conc2_max;
	public String conc2_unit;
	public String conc2_comments;
	public String conc3_type;
	public String ion3;
	public String conc3_mean_op;
	public String conc3_mean;
	public String conc3_min_op;
	public String conc3_min;
	public String conc3_max_op;
	public String conc3_max;
	public String conc3_unit;
	public String conc3_comments;
	public String bcf1_mean_op;
	public String bcf1_mean;
	public String bcf1_min_op;
	public String bcf1_min;
	public String bcf1_max_op;
	public String bcf1_max;
	public String bcf1_unit;
	public String bcf1_comments;
	public String bcf2_mean_op;
	public String bcf2_mean;
	public String bcf2_min_op;
	public String bcf2_min;
	public String bcf2_max_op;
	public String bcf2_max;
	public String bcf2_unit;
	public String bcf2_comments;
	public String bcf3_mean_op;
	public String bcf3_mean;
	public String bcf3_min_op;
	public String bcf3_min;
	public String bcf3_max_op;
	public String bcf3_max;
	public String bcf3_unit;
	public String bcf3_comments;
	public String significance_code;
	public String significance_type;
	public String significance_level_mean_op;
	public String significance_level_mean;
	public String significance_level_min_op;
	public String significance_level_min;
	public String significance_level_max_op;
	public String significance_level_max;
	public String significance_comments;
	public String chem_analysis_method;
	public String chem_analysis_method_comments;
	public String endpoint_assigned;
	public String organism_final_wt_mean_op;
	public String organism_final_wt_mean;
	public String organism_final_wt_min_op;
	public String organism_final_wt_min;
	public String organism_final_wt_max_op;
	public String organism_final_wt_max;
	public String organism_final_wt_unit;
	public String organism_final_wt_comments;
	public String intake_rate_mean_op;
	public String intake_rate_mean;
	public String intake_rate_min_op;
	public String intake_rate_min;
	public String intake_rate_max_op;
	public String intake_rate_max;
	public String intake_rate_unit;
	public String intake_rate_comments;
	public String lipid_pct_mean_op;
	public String lipid_pct_mean;
	public String lipid_pct_min_op;
	public String lipid_pct_min;
	public String lipid_pct_max_op;
	public String lipid_pct_max;
	public String lipid_pct_comments;
	public String dry_wet;
	public String dry_wet_pct_mean_op;
	public String dry_wet_pct_mean;
	public String dry_wet_pct_min_op;
	public String dry_wet_pct_min;
	public String dry_wet_pct_max_op;
	public String dry_wet_pct_max;
	public String dry_wet_pct_comments;
	public String steady_state;
	public String additional_comments;
	public String companion_tag;
	public String created_date;
	public String modified_date;
	public String old_terretox_result_number;
	public String cas_number;
	public String chemical_name;
	public String ecotox_group;
	public String dtxsid;
	public String reference_db;
	public String reference_type;
	public String author;
	public String title;
	public String source;
	public String publication_year;

	
	ExperimentalRecord toExperimentalRecord(int valueNumber,String sourceName) {

		String conc_mean=null;
		String conc_min=null;
		String conc_max=null;
		
		String conc_mean_op=null;
		String conc_min_op=null;
		String conc_max_op=null;
		String conc_unit=null;
		
		if(valueNumber==1) {
			conc_mean=conc1_mean;
			conc_min=conc1_min;
			conc_max=conc1_max;
			conc_mean_op=conc1_mean_op;
			conc_min_op=conc1_min_op;
			conc_max_op=conc1_max_op;
			conc_unit=conc1_unit;
		} else if (valueNumber==2) {
			conc_mean=conc2_mean;
			conc_min=conc2_min;
			conc_max=conc2_max;
			conc_mean_op=conc2_mean_op;
			conc_min_op=conc2_min_op;
			conc_max_op=conc2_max_op;
			conc_unit=conc2_unit;
		}
		
		ExperimentalRecord er=new ExperimentalRecord();
		
		er.dsstox_substance_id=dtxsid;
		er.source_name=sourceName;
		
		String CAS1=cas_number.substring(0,cas_number.length()-3);
		String CAS2=cas_number.substring(cas_number.length()-3,cas_number.length()-1);
		String CAS3=cas_number.substring(cas_number.length()-1,cas_number.length());
				
		er.casrn=CAS1+"-"+CAS2+"-"+CAS3;
		
		er.chemical_name=chemical_name;
		
//		System.out.println(cas_number+"\t"+er.casrn);
		
		er.property_name=property_name;
		er.keep=true;
		
		LiteratureSource ls=new LiteratureSource();
		er.literatureSource=ls;
		ls.name=author+" ("+publication_year+")";
		ls.author=author;
		ls.title=title;
		ls.year=publication_year;
		ls.citation=author+" ("+publication_year+"). "+title+"."+source;

		if(conc_mean==null) {
			er.keep=false;
			er.reason="No conc1_mean value";
		} else if(conc_mean_op!=null && !conc_mean_op.equals("~")) {
			er.keep=false;
			er.reason="bad conc1_mean_op: "+conc_mean_op;
		} else if(conc_min_op!=null && !conc_min_op.equals("~")) {
			er.keep=false;
			er.reason="bad conc1_min_op:"+conc_min_op;
		} else if(conc_max_op!=null && !conc_max_op.equals("~")) {
			er.keep=false;
			er.reason="bad conc1_max_op:"+conc_max_op;
		} else if(conc_unit==null || conc_unit.isBlank()) {
			er.keep=false;
			er.reason="Missing original units";
//			System.out.println("missing units");
		}


		er.property_value_units_original=conc_unit;
		
		if (er.keep) {
			er.property_value_point_estimate_original=Double.parseDouble(conc_mean);
			
			if(conc_min!=null && conc_max!=null) {
				double log=Math.log10(Double.parseDouble(conc_max)/Double.parseDouble(conc_min));
				
				if(log>1) {
					er.keep=false;
					er.reason="Range of min and max is too wide";
//					System.out.println(min+"\t"+max);
				}
			}
			
//			System.out.println(r.conc1_max_op+"\t"+r.conc1_min_op+"\t"+r.conc1_mean_op);
		} 
		
		er.experimental_parameters=new Hashtable<>();
		
		er.experimental_parameters.put("test_id", test_id);
		er.experimental_parameters.put("exposure_type", exposure_type);
		er.experimental_parameters.put("chem_analysis_method", chem_analysis_method);
		
		er.property_value_string=er.property_value_point_estimate_original+" "+conc_unit;//TODO
		
//		if(er.dsstox_substance_id.equals("DTXSID0034566")) {
//			System.out.println("Found DTXSID0034566, keep="+er.keep+"\treason="+er.reason+"\t"+er.property_value_units_original+"\t"+valueNumber);
//		}
		
		
		return er;
		
	}
	

	static void setValue(String fieldName,String fieldValue,RecordEcotox rec) {
		
		Field field;
		try {
			field = RecordEcotox.class.getField(fieldName);

			String typeName = field.getGenericType().getTypeName();
			fieldName = field.getName();

			if (typeName.equals("java.lang.String")) {
				field.set(rec, fieldValue);
			} else if (typeName.equals("java.lang.Double")) {
				field.set(rec, Double.parseDouble(fieldValue));
			} else if (typeName.equals("java.lang.Long")) {
				field.set(rec, Long.parseLong(fieldValue));
			} else if (typeName.equals("java.lang.Integer")) {
				field.set(rec, Integer.parseInt(fieldValue));
				// } else if (typeName.equals("java.util.Date")) {
				// Date d = rs.getDate(fieldName);
				// field.set(rec, d);
			} else {
				System.out.println(typeName+" not handled");
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} 

	}

	public static List<RecordEcotox> get_96hr_FHM_LC50_Tox_Records_From_DB() {

		List<RecordEcotox>records=new ArrayList<>();

		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
//		select * from tests t
//		join results r on t.test_id=r.test_id
//		join chemicals c on c.cas_number=t.test_cas
//		join references_ r2 on r2.reference_number=t.reference_number
//		where t.species_number=1 
//		and ((r.obs_duration_mean=96 and r.obs_duration_unit='h')  or (r.obs_duration_mean=4 and r.obs_duration_unit='d'))
		
		String sql = "select *\n" + "from tests t\n" + "join results r on t.test_id=r.test_id\n"
				+ "join chemicals c on c.cas_number=t.test_cas\n"
				+ "join references_ r2 on r2.reference_number=t.reference_number\n"
//				+ "left join exposure_type_codes etc on t.exposure_type=etc.code "
//				+ "left join chemical_analysis_codes cac on r.chem_analysis_method=cac.code "
				+ "where t.species_number=1;";

		try {
//			String databasePath = "data\\experimental\\ECOTOX\\ecotox_ascii_06_15_2023.db";
			String databasePath = "data\\experimental\\ECOTOX_2023_12_14\\ecotox_ascii_12_14_2023.db";

			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = stat.executeQuery(sql);

//			JsonArray ja = new JsonArray();

			int counter=0;
			
			while (rs.next()) {
				
				counter++;
//				System.out.println(rs.getString(1));
//				JsonObject jo = new JsonObject();

				RecordEcotox rec=new RecordEcotox();
				
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					
					String columnLabel = rs.getMetaData().getColumnLabel(i);

//					if(counter==1)					
//						System.out.println(columnLabel);
					
					String columnValue = rs.getString(i);
					
					if (rs.getString(i) == null || rs.getString(i).isBlank())
						continue;

					setValue(columnLabel, columnValue, rec);
//					System.out.println(rs.getMetaData().getColumnLabel(i));
				}
				
				rec.property_name=ExperimentalConstants.strNINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;
				
				
				if(rec.media_type!=null) {
					if (!rec.media_type.contains("FW"))
					continue;
				}
				
				if (rec.test_location != null) {
					if (!rec.test_location.contains("LAB"))
						continue;
				}

				
				if (rec.endpoint != null) {
					if (!rec.endpoint.contains("LC50"))
						continue;
				}
				
				
//				if (jo.get("effect") != null) {
//					effect = jo.get("effect").getAsString();
//				}
//
				if (rec.measurement != null) {
					if (!rec.measurement.contains("MORT") && !rec.measurement.contains("SURV"))
						continue;
				}
				records.add(rec);
			}


			System.out.println(records.size());

//			System.out.println(gson.toJson(records));

		} catch (Exception ex) {
			ex.printStackTrace();

		}
		
		return records;

	}

	

	public Double getStudyDurationValueInDays() {
		
		if(obs_duration_mean==null) return null;
		
		Double studyDurationValueInDays = Double.parseDouble(obs_duration_mean);
		
		
//		if(obs_duration_unit.equals("dph")) {
//			System.out.println(obs_duration_mean+"\t"+obs_duration_unit);
//		}

		switch (obs_duration_unit) {
		
		case "d":
			break;
		case "wk":
			studyDurationValueInDays *= 7.0;
			break;
		case "mo":
			studyDurationValueInDays *= 30.0;
			break;
		case "yr":
			studyDurationValueInDays *= 365.0;
			break;
		case "h":
			studyDurationValueInDays /= 24.0;
			break;
		case "mi"://minutes
			studyDurationValueInDays /= 1440.0;
			break;
		case "-":
//			System.out.println("No study duration units for ToxVal ID " + toxval_id);
			studyDurationValueInDays = null;
			break;
		default:
			System.out.println("Unknown study duration units for ToxVal ID " + test_id + ": " + obs_duration_unit);
			studyDurationValueInDays = null;
			break;
		}
		
		return studyDurationValueInDays;
	}

	public boolean isAcceptableDuration(Double durationDays) {

		Double studyDurationValueInDays = getStudyDurationValueInDays();

		if (studyDurationValueInDays == null || studyDurationValueInDays < 0.95 * durationDays
				|| studyDurationValueInDays > 1.05 * durationDays) {
			return false;
		}

		return true;
	}
	
	public static void main(String[] args) {
		RecordEcotox r = new RecordEcotox();
		r.get_96hr_FHM_LC50_Tox_Records_From_DB();

	}

}
