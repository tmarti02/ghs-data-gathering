package gov.epa.exp_data_gathering.parse.ECOTOX;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.math3.analysis.function.Exp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
import gov.epa.database.SqlUtilities;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.ParameterValue;
import gov.epa.exp_data_gathering.parse.UnitConverter;
import gov.epa.exp_data_gathering.parse.ToxVal.ToxValRecord;


/**
 * @author TMARTI02
 */
public class RecordEcotox {
	
	public static String sourceName=ExperimentalConstants.strSourceEcotox_2024_12_12;
	public static String databasePath = "data\\experimental\\ECOTOX_2024_12_12\\ecotox_ascii_12_12_2024.db";

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
	
	public String code;
	public String description;
	
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
	public Double conc1_mean;

	public String conc1_min_op;
	public Double conc1_min;
	
	public String conc1_max_op;
	public Double conc1_max;
	
	public String conc1_unit;
	public String conc1_comments;
	public String conc2_type;

	public String ion2;
	
	public String conc2_mean_op;
	public Double conc2_mean;
	
	public String conc2_min_op;
	public Double conc2_min;
	
	public String conc2_max_op;	
	public Double conc2_max;
	
	public String conc2_unit;
	public String conc2_comments;
	
	public String conc3_type;
	public String ion3;
	public String conc3_mean_op;
	public Double conc3_mean;
	public String conc3_min_op;
	public String conc3_min;
	public String conc3_max_op;
	public String conc3_max;
	public String conc3_unit;
	public String conc3_comments;
	
	public String bcf1_mean_op;
	public Double bcf1_mean;

	public String bcf1_min_op;
	public Double bcf1_min;
	
	public String bcf1_max_op;
	public Double bcf1_max;
	
	public String bcf1_unit;
	public String bcf1_comments;
	
	public String bcf2_mean_op;
	public Double bcf2_mean;
	public String bcf2_min_op;
	public String bcf2_min;
	public String bcf2_max_op;
	public String bcf2_max;
	public String bcf2_unit;
	public String bcf2_comments;
	
	public String bcf3_mean_op;
	public Double bcf3_mean;
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
	public Double intake_rate_mean;
	public String intake_rate_min_op;
	public String intake_rate_min;
	public String intake_rate_max_op;
	public String intake_rate_max;
	public String intake_rate_unit;
	public String intake_rate_comments;
	
	public String lipid_pct_mean_op;
	public Double lipid_pct_mean;
	public String lipid_pct_min_op;
	public String lipid_pct_min;
	public String lipid_pct_max_op;
	public String lipid_pct_max;
	public String lipid_pct_comments;
	
	public String dry_wet;
	public String dry_wet_pct_mean_op;
	public Double dry_wet_pct_mean;
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
	public String doi;
	
	public String species_number;
	public String latin_name;
	public String common_name;
	public String kingdom;
	public String phylum_division;
	public String subphylum_div;
	public String superclass;
	
	public String class_;
	public String tax_order;
	public String family;
	public String genus;
	public String species;
	public String subspecies;
	public String variety;
	public String ncbi_taxid;	
	
	
	
	transient Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	transient static HashSet<String>conc1_units=new HashSet<>();
	
	static transient UnitConverter uc = new UnitConverter("Data" + File.separator + "density.txt");

	
	ExperimentalRecord toExperimentalRecordFishTox(String propertyName, int valueNumber) {

		String conc_type=null;

		Double conc_mean=null;
		Double conc_min=null;
		Double conc_max=null;
		
		String conc_mean_op=null;
		String conc_min_op=null;
		String conc_max_op=null;
		String conc_unit=null;
		
		if(valueNumber==1) {
			conc_type=conc1_type;
			conc_mean=conc1_mean;			
			conc_min=conc1_min;
			conc_max=conc1_max;
			conc_mean_op=conc1_mean_op;
			conc_min_op=conc1_min_op;
			conc_max_op=conc1_max_op;
			conc_unit=conc1_unit;
		} else if (valueNumber==2) {
			conc_type=conc2_type;
			conc_mean=conc2_mean;
			conc_min=conc2_min;
			conc_max=conc2_max;
			conc_mean_op=conc2_mean_op;
			conc_min_op=conc2_min_op;
			conc_max_op=conc2_max_op;
			conc_unit=conc2_unit;
		}
		

		if(conc_min!=null && conc_min==0) conc_min=null;

		if(conc_unit.equals("ml/L")) conc_unit="mL/L";
		if(conc_unit.equals("ug/ml")) conc_unit="mg/L";
					
		ExperimentalRecord er=new ExperimentalRecord();
		
		setChemicalIdentifiers(er);
		
//		System.out.println(cas_number+"\t"+er.casrn);
		
		er.property_name=propertyName;
		er.keep=true;
		
		setLiteratureSource(er);
		
//		if(!er.property_name.equals(ExperimentalConstants.strAcuteAquaticToxicity)) {//only remove the operator ones if have a specific fish+duration property (for QSAR modeling)
//			if(conc_mean==null) {
//				er.keep=false;
//				er.reason="No conc1_mean value";
//			} else if(conc_mean_op!=null && !conc_mean_op.equals("~")) {
//				er.keep=false;
//				er.reason="bad conc1_mean_op: "+conc_mean_op;
//			} else if(conc_min_op!=null && !conc_min_op.equals("~")) {
//				er.keep=false;
//				er.reason="bad conc1_min_op:"+conc_min_op;
//			} else if(conc_max_op!=null && !conc_max_op.equals("~")) {
//				er.keep=false;
//				er.reason="bad conc1_max_op:"+conc_max_op;
//			} else if(conc_unit==null || conc_unit.isBlank()) {
//				er.keep=false;
//				er.reason="Missing original units";
//				//			System.out.println("missing units");
//			}
//		}
		
		
		er.property_value_units_original=conc_unit;
		
		if (er.keep) {
			
			if(conc_mean!=null) {
				er.property_value_numeric_qualifier=conc_mean_op;
				er.property_value_point_estimate_original=conc_mean;	
				er.property_value_string=er.property_value_point_estimate_original+" "+conc_unit;
			
			} else  if(conc_min!=null && conc_max!=null) {

				er.property_value_min_original=conc_min;
				er.property_value_max_original=conc_max;
				er.property_value_string=conc_min+" "+conc_unit+" < "+endpoint+" <"+conc_max+" "+conc_unit;

				double log=Math.log10(conc_max/conc_min);
				
				if(log>1) {
					er.keep=false;
					er.reason="Range of min and max is too wide";
//					System.out.println("Range too wide:"+conc_min+" to "+conc_max+" "+conc_unit);
				} else {
//					er.property_value_point_estimate_original=Math.sqrt(conc_min*conc_max);
				}
				
			} else if(conc_min!=null) {
				er.property_value_min_original=conc_min;
				er.property_value_string=endpoint+" > "+conc_min+" "+conc_unit;				
			} else if (conc_max!=null) {
				er.property_value_max_original=conc_max;
				er.property_value_string=endpoint+" < "+conc_max+" "+conc_unit;				

			}

			
//			System.out.println(r.conc1_max_op+"\t"+r.conc1_min_op+"\t"+r.conc1_mean_op);
		} 


		
		er.experimental_parameters=new LinkedHashMap<>();//keeps insertion order
		er.parameter_values=new ArrayList<>();
		
		er.experimental_parameters.put("test_id", test_id);
		er.experimental_parameters.put("result_id", result_id);
		
		er.experimental_parameters.put("exposure_type", exposure_type);
		er.experimental_parameters.put("chem_analysis_method", chem_analysis_method);
		er.experimental_parameters.put("concentration_type", getConcentrationType(conc_type));

		if(ParseEcotox.onlyExposureTypeSFR) {
			if(!exposure_type.equals("Static") && !exposure_type.equals("Flow-through") &&!exposure_type.equals("Renewal")) {
				er.keep=false;
				er.reason="Invalid exposure type";
			}
		}
		
		
		if(er.property_name.equals(ExperimentalConstants.strAcuteAquaticToxicity)
				|| er.property_name.equals(ExperimentalConstants.strChronicAquaticToxicity)) {//general property
			er.experimental_parameters.put("test_type", endpoint);
			setObservationDuration(er);
			addSpeciesParameters(er);
		}
		
//		if(er.dsstox_substance_id.equals("DTXSID0034566")) {
//			System.out.println("Found DTXSID0034566, keep="+er.keep+"\treason="+er.reason+"\t"+er.property_value_units_original+"\t"+valueNumber);
//		}

		if(er.property_name.equals(ExperimentalConstants.strAcuteAquaticToxicity) || er.property_name.contains("LC50")) {
			er.property_category=ExperimentalConstants.strAcuteAquaticToxicity;			
		} else if(er.property_name.equals(ExperimentalConstants.strChronicAquaticToxicity)) {
			er.property_category=ExperimentalConstants.strChronicAquaticToxicity;
		}

		
		if(effect!=null) {
			er.experimental_parameters.put("Effect", effect);
		}
		
		if(er.keep) {
			uc.convertRecord(er);
		}
		
		return er;
		
	}
	
	private void addSpeciesParameters(ExperimentalRecord er) {
		er.experimental_parameters.put("Species latin", latin_name);
		er.experimental_parameters.put("Species common", common_name);
		String supercategory=getSpeciesSupercategory();
		if(supercategory!=null) {
			er.experimental_parameters.put("Species supercategory",supercategory);	
//				System.out.println(ecotox_group+"\t"+common_name+"\t"+supercategory);
//				System.out.println(common_name+"\t"+supercategory);
		} else {
			System.out.println(common_name+"\t"+ecotox_group+"\t"+common_name+"\tMissing supercategory");
		}
		
		String speciesType=getSpeciesType();
		er.experimental_parameters.put("Species type", speciesType);
		
	}
	
		


	private void setObservationDuration(ExperimentalRecord er) {
		
		String unit=obs_duration_unit;
		Double mean=getValueInDays(obs_duration_mean,unit);
		Double min=getValueInDays(obs_duration_min,unit);
		if(min!=null && min==0) min=null;
		Double max=getValueInDays(obs_duration_max,unit);
		String meanOp=obs_duration_mean_op;
		
		if(mean==null && min==null && max==null)return;
		
//		if (unit.equals("NC") || unit.equals("NR")) return;
		
//		String parameterName="Observation duration";
		String parameterName="Exposure duration";//to be consistent with Arnot 2006
		
		ParameterValue pv=new ParameterValue();
		pv.parameter.name=parameterName;
		pv.valueQualifier=meanOp;
		pv.valuePointEstimate=mean;
		pv.unit.abbreviation="days";

		int type=-1;
		if(mean!=null) {
			type=1;
			if(meanOp!=null && meanOp.contains("<")) {
				type=2;
				pv.valueMax=mean;
				pv.valuePointEstimate=null;
			} else if(meanOp!=null && meanOp.contains(">")) {
				pv.valueMin=mean;
				pv.valuePointEstimate=null;
				type=3;
			}
			
		} else if(min!=null && max!=null) {
			type=4;
			pv.valueMin=min;
			pv.valueMax=max;
		} else if(min!=null) {//doesnt happen for BCF?
			type=5;
			pv.valueMin=min;
		} else if(max!=null) {//doesnt happen for BCF?
			type=6;
			pv.valueMax=max;
		} else {
			type=7;
			return;
		}
		
		er.parameter_values.add(pv);
//		System.out.println("BCF obs dur type="+type+"\t"+meanOp+"\t"+mean+"\t"+min+"\t"+max+"\t"+unit);
		
	}
	
	private void setExposureDuration(ExperimentalRecord er) {
		
		String unit=exposure_duration_unit;		
		Double mean=getValueInDays(exposure_duration_mean,unit);
		Double min=getValueInDays(exposure_duration_min,unit);	
		Double max=getValueInDays(exposure_duration_max,unit);
		String meanOp=exposure_duration_mean_op;
		
		if(min!=null && min==0) min=null;
		
		if(mean==null && min==null && max==null)return;
		
//		if (unit.equals("NC") || unit.equals("NR")) return;
		
		String parameterName="Exposure duration";
		
		ParameterValue pv=new ParameterValue();
		pv.parameter.name=parameterName;
		pv.valueQualifier=meanOp;
		pv.valuePointEstimate=mean;
		pv.unit.abbreviation="days";

		int type=-1;
		if(mean!=null) {
			type=1;
			if(meanOp!=null && meanOp.contains("<")) {
				type=2;
				pv.valueMax=mean;
				pv.valuePointEstimate=null;
			} else if(meanOp!=null && meanOp.contains(">")) {
				pv.valueMin=mean;
				pv.valuePointEstimate=null;
				type=3;
			}
			
		} else if(min!=null && max!=null) {
			type=4;
			pv.valueMin=min;
			pv.valueMax=max;
		} else if(min!=null) {//doesnt happen for BCF?
			type=5;
			pv.valueMin=min;
		} else if(max!=null) {//doesnt happen for BCF?
			type=6;
			pv.valueMax=max;
		} else {
			type=7;
			return;
		}
		
		er.parameter_values.add(pv);
//		System.out.println("BCF exp dur type="+type+"\t"+meanOp+"\t"+mean+"\t"+min+"\t"+max+"\t"+unit);
		
	}
	

	public static List<RecordEcotox> get_BCF_Records_From_DB(String endpoint) {
		List<RecordEcotox>records=new ArrayList<>();
		
		//TODO also get the following:
//		t.test_radiolabel: whether concentrations are imprecise radiolabel measurements (need metabolite correction?)
//		r.additional_comments: kinetic vs conc method for BCF
//		r.obs_duration_mean_op, r.obs_duration_mean, r.obs_duration_unit
		
//		String sql="select  r.endpoint, t.test_id, dtxsid, cas_number, chemical_name, bcf1_mean ,bcf1_unit,\r\n"
//				+ " conc1_mean_op, conc1_mean, conc1_unit, conc1_min, conc1_max, conc1_min_op, conc1_max_op,\r\n"
//				+ "exposure_duration_mean_op,	exposure_duration_mean,exposure_duration_unit,\r\n"
//				+ "media_type, test_location, exposure_type,chem_analysis_method, s.common_name, s.latin_name,s.ecotox_group, rsc.description as 'response_site',\r\n"
//				+ " author, publication_year, title,source from tests t\r\n"

		String sql="select  * from tests t\r\n"
				+ "	join results r on t.test_id=r.test_id\r\n"
				+ "	join chemicals c on c.cas_number=t.test_cas\r\n"
				+ "	left join references_ r2 on r2.reference_number=t.reference_number\r\n"
				+ "	left join species s on t.species_number=s.species_number\r\n"
				+ "	left join response_site_codes rsc on rsc.code=r.response_site\r\n"
				+ "	where bcf1_mean is not null and endpoint ='"+endpoint+"';";
//				+ "	where bcf1_mean is not null and endpoint like '%"+endpoint+"%';";//this will allow BCFD (dry weight)
		
//				+ "	where bcf1_mean is not null and (bcf1_mean_op ='~' or bcf1_mean_op='')\r\n"
//				+ " and media_type like '%FW%' and test_location like '%LAB%'"				
//				+ "	order by cas_number;";
		
//		String sql="select * from tests t\r\n"
//				+ "	join results r on t.test_id=r.test_id\r\n"
//				+ "	join chemicals c on c.cas_number=t.test_cas\r\n"
//				+ "	left join references_ r2 on r2.reference_number=t.reference_number\r\n"
//				+ "	left join species s on t.species_number=s.species_number\r\n"
//				+ "	left join response_site_codes rsc on rsc.code=r.response_site\r\n"
//				+ "	where bcf1_mean is not null and (bcf1_mean_op ='~' or bcf1_mean_op='')\r\n"
////				+ " and media_type like '%FW%' and test_location like '%LAB%'"				
//				+ "	order by cas_number";

		System.out.println(sql);
		
		try {

			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = stat.executeQuery(sql);

//			JsonArray ja = new JsonArray();

			Hashtable<String,String>htExposureType=getExposureTypeLookup(databasePath);
			Hashtable<String,String>htMediaType=getMediaTypeLookup(databasePath);
			Hashtable<String,String>htTestLocation=getLocationTypeLookup(databasePath);
			
			int counter=0;
			
			while (rs.next()) {
				counter++;
//				System.out.println(rs.getString(1));
//				JsonObject jo = new JsonObject();
				RecordEcotox rec=new RecordEcotox();
				SqlUtilities.createRecord(rs, rec);
				records.add(rec);
				
				rec.setExposureType(htExposureType);
				rec.setChemicalAnalysisMethod();
				rec.setMediaType(htMediaType);
				rec.setTestLocation(htTestLocation);

			}
			
			
		} catch (Exception ex) {
			ex.printStackTrace();

		}
		
		return records;
	}
	
	
	public static List<RecordEcotox> get_Acute_Tox_Records_From_DB(int speciesNumber,String propertyName) {

		List<RecordEcotox>records=new ArrayList<>();

		String sql = "select *\n" + "from tests t\n" + "join results r on t.test_id=r.test_id\n"
				+ "join chemicals c on c.cas_number=t.test_cas\n"
				+ "join references_ r2 on r2.reference_number=t.reference_number\n"
//				+ "left join exposure_type_codes etc on t.exposure_type=etc.code "
//				+ "left join chemical_analysis_codes cac on r.chem_analysis_method=cac.code "								
				+ "where t.species_number="+speciesNumber+" and \r\n"
				+ "media_type like '%FW%' and test_location like '%LAB%' and \r\n"
				+ "endpoint like '%LC50%' and \r\n"
				+ "measurement like '%MORT%';";//just use MORT to be safe
//				+ "(measurement like '%MORT%' or measurement like '%SURV%');";
		
		//Note filter for duration happens later
		
				System.out.println(sql);
		try {

			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = stat.executeQuery(sql);

//			JsonArray ja = new JsonArray();

			Hashtable<String,String>htExposureType=getExposureTypeLookup(databasePath);
			
			int counter=0;
			
			while (rs.next()) {
				
				counter++;
//				System.out.println(rs.getString(1));
//				JsonObject jo = new JsonObject();

				RecordEcotox rec=new RecordEcotox();
				SqlUtilities.createRecord(rs, rec);
				
				rec.property_name=propertyName;
				rec.setExposureType(htExposureType);
				rec.setChemicalAnalysisMethod();
				records.add(rec);
			}


			System.out.println(records.size());

//			System.out.println(gson.toJson(records));

		} catch (Exception ex) {
			ex.printStackTrace();

		}
		
		return records;

	}
	
	public static List<RecordEcotox> get_Acute_Tox_Records_From_DB() {

		List<RecordEcotox>records=new ArrayList<>();

		String sql = "select *\n" + "from tests t\n" + "join results r on t.test_id=r.test_id\n"
				+ "join chemicals c on c.cas_number=t.test_cas\n"
				+ "join references_ r2 on r2.reference_number=t.reference_number\n"
				+ "left join species s on t.species_number=s.species_number\r\n"
				+ "where media_type like '%FW%' and test_location like '%LAB%' and \r\n"
				+ "(endpoint like '%LC50%' or endpoint like '%EC50%') and \r\n"
				+ "measurement like '%MORT%';";//just use MORT to be safe
//				+ "(measurement like '%MORT%' or measurement like '%SURV%');";
		
		//Note filter for duration happens later
		
	System.out.println(sql);
		try {

			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = stat.executeQuery(sql);

			Hashtable<String,String>htExposureType=getExposureTypeLookup(databasePath);
			
			int counter=0;
			while (rs.next()) {
				counter++;
//				System.out.println(rs.getString(1));
				RecordEcotox rec=new RecordEcotox();
				SqlUtilities.createRecord(rs, rec);
				rec.setExposureType(htExposureType);
				rec.setChemicalAnalysisMethod();
				records.add(rec);
			}
//			System.out.println(records.size());
//			System.out.println(gson.toJson(records));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}

	
	public static List<RecordEcotox> get_Chronic_Tox_Records_From_DB() {

		List<RecordEcotox>records=new ArrayList<>();

		String sql = "select *\n" + "from tests t\n" + "join results r on t.test_id=r.test_id\n"
				+ "join chemicals c on c.cas_number=t.test_cas\n"
				+ "join references_ r2 on r2.reference_number=t.reference_number\n"
				+ "left join species s on t.species_number=s.species_number\r\n"
				+ "where media_type like '%FW%' and test_location like '%LAB%' and \r\n"
				+ "(endpoint like '%LOEC%' or endpoint like '%NOEC%');";
		
		//Note filter for duration happens later
		
	System.out.println(sql);
		try {

			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = stat.executeQuery(sql);

			Hashtable<String,String>htExposureType=getExposureTypeLookup(databasePath);
			Hashtable<String,String>htEffect=getEffectLookup(databasePath);
			
			int counter=0;
			while (rs.next()) {
				counter++;
//				System.out.println(rs.getString(1));
				RecordEcotox rec=new RecordEcotox();
				SqlUtilities.createRecord(rs, rec);
				rec.setExposureType(htExposureType);//rather than table join, use hashtable, is this best way?
				rec.setEffect(htEffect);//rather than table join, use hashtable, is this best way?
				rec.setChemicalAnalysisMethod();
				records.add(rec);
			}
//			System.out.println(records.size());
//			System.out.println(gson.toJson(records));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}
	
	public static Hashtable<String,String> getExposureTypeLookup(String databasePath) {

		Hashtable<String,String>htDesc=new Hashtable<>();
		String sql = "select code,description from exposure_type_codes;";
		try {

			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = stat.executeQuery(sql);
			while (rs.next()) {
				String code= rs.getString(1);
				String description= rs.getString(2);
				htDesc.put(code, description);
			}
			
			htDesc.put("U", "Not reported");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return htDesc;
	}
	
	
	public static Hashtable<String,String> getEffectLookup(String databasePath) {

		Hashtable<String,String>htDesc=new Hashtable<>();
		String sql = "select code,description from effect_codes;";
		try {

			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = stat.executeQuery(sql);
			while (rs.next()) {
				String code= rs.getString(1);
				String description= rs.getString(2);
				htDesc.put(code, description);
			}
			
			htDesc.put("U", "Not reported");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return htDesc;
	}
	
	public static Hashtable<String,String> getMediaTypeLookup(String databasePath) {

		Hashtable<String,String>htDesc=new Hashtable<>();
		String sql = "select code,description from media_type_codes;";
		try {

			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = stat.executeQuery(sql);
			while (rs.next()) {
				String code= rs.getString(1);
				String description= rs.getString(2);
				htDesc.put(code, description);
			}
			
			htDesc.put("U", "Not reported");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return htDesc;
	}
	
	public static Hashtable<String,String> getLocationTypeLookup(String databasePath) {

		Hashtable<String,String>htDesc=new Hashtable<>();
		String sql = "select code,description from test_location_codes;";
		try {

			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = stat.executeQuery(sql);
			while (rs.next()) {
				String code= rs.getString(1);
				String description= rs.getString(2);
				htDesc.put(code, description);
			}
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return htDesc;
	}
	
	void setExposureType(Hashtable<String,String>htDesc) {
		
//		if(exposure_type.contains("F")) {
//			exposure_type="Flow-through";
//		} else if(exposure_type.equals("R")) {
//			exposure_type="Renewal";
//		} else if(exposure_type.contains("S")) {
//			exposure_type="Static";
//		} else if(exposure_type.contains("L")) {
//			exposure_type="Leaching";
//		} else if(exposure_type.contains("E")) {
//			exposure_type="Lentic";
//		} else if(exposure_type.contains("O")) {
//			exposure_type="Lotic";
//		} else if(exposure_type.contains("NR")) {
//			exposure_type="Not reported";			
//		} else if(exposure_type.contains("P")) {
//			exposure_type="Pulse";
//		} else {
//			System.out.println("Unknown exposure type:\t"+exposure_type);
//		}
		
		String code=exposure_type.replace("/","");
		
		if(htDesc.containsKey(code)) {
			exposure_type=htDesc.get(code);
		} else {
			System.out.println("Unknown exposure_type: "+code);
			
		}
		
	}
	
	

	void setEffect(Hashtable<String,String>htDesc) {
		
		String code=effect.replace("/","");
		if(htDesc.containsKey(code)) {
			effect=htDesc.get(code);
		} else {
			System.out.println("Unknown effect code: "+code);
		}
		
	}
	
	
	void setTestLocation(Hashtable<String,String>htDesc) {
		
		String code=test_location.replace("/","");
		
		if(htDesc.containsKey(code)) {
			test_location=htDesc.get(code);
		} else {
			System.out.println("Unknown exposure_type: "+code);
			
		}
		
	}
	
	void setChemicalAnalysisMethod() {
		//TODO use hashtable like in exposure type
		
		String cam=chem_analysis_method.replace("/", "");
		
		if(cam.contains("M")) {
			chem_analysis_method="Measured";			
		} else if(cam.contains("Z")) {
			chem_analysis_method="Chemical analysis reported";
		} else if(cam.contains("X")) {
			chem_analysis_method="Unmeasured values (some measured values reported in article)";
		} else if(cam.equals("U")) {
			chem_analysis_method="Unmeasured";
		} else if(cam.contains("NR") || cam.equals("--")) {
			chem_analysis_method="Not reported";
		} else if(cam.contains("NC")) {
			chem_analysis_method="Not coded";
		} else {
			System.out.println("Unknown chem_analysis_method:\t"+chem_analysis_method);
		}
		
		
	}
	
	void setMediaType(Hashtable<String, String> htMediaType) {
		String code=media_type.replace("/", "");
		if(htMediaType.containsKey(code)) {
			media_type=htMediaType.get(code);
		} else {
			System.out.println("Unknown media_type: "+code);
		}
	}
	
	String getConcentrationType(String conc_type) {
		
		if(conc_type==null) return "Not available";
		else if(conc_type.equals("--")) return "Unspecified";
		else if (conc_type.equals("A")) return "Active ingredient";
		else if (conc_type.equals("D")) return "Dissolved";
		else if (conc_type.equals("F")) return "Formulation";
		else if (conc_type.equals("L")) return "Labile (free metal ion)";
		else if (conc_type.equals("NA")) return "Not applicable";
		else if (conc_type.equals("NC")) return "Not coded";
		else if (conc_type.equals("NR")) return "Not reported";
		else if (conc_type.equals("T")) return "Total";
		else if (conc_type.equals("U")) return "Unionized";
		else {
			System.out.println("Unknown conc_type:\t"+conc_type);
			return conc_type;
		}
		
			
	}
	

	public Double getValueInDays(String obs_duration,String units) {
		
		if(obs_duration==null) return null;
		Double studyDurationValue = Double.parseDouble(obs_duration);
		
		switch (units) {
		
		case "d":
		case "dpf":
		case "dph":
		case "dpu":
			return studyDurationValue;
		case "wk":
			return studyDurationValue *= 7.0;
		case "mo":
			return studyDurationValue *= 30.0;
		case "yr":
			return studyDurationValue *= 365.0;
		case "h":
		case "hpf":
		case "hph":
			return studyDurationValue /= 24.0;
		case "mi"://minutes
			return studyDurationValue /= 1440.0;
		case "s"://seconds
			return studyDurationValue /= (1440.0*60);

		case "-":
		case "NR":	
//			System.out.println("No study duration units for ToxVal ID " + toxval_id);
			return null;
		default:
//			System.out.println("Unknown observation duration units for ToxVal ID " + test_id + ": " + obs_duration_unit);
			return null;
		}
		
		
	}

	public boolean isAcceptableDuration(Double durationDays) {

		Double studyDurationValueInDays = getValueInDays(obs_duration_mean,obs_duration_unit);

		if (studyDurationValueInDays == null || studyDurationValueInDays < 0.95 * durationDays
				|| studyDurationValueInDays > 1.05 * durationDays) {
			return false;
		}

		return true;
	}
	
	public static void main(String[] args) {
		RecordEcotox r = new RecordEcotox();
		 List<RecordEcotox>records=r.get_Acute_Tox_Records_From_DB(1,ExperimentalConstants.strNINETY_SIX_HOUR_FATHEAD_MINNOW_LC50);
	}
	
	
	private String getSpeciesSupercategory() {

		if(ecotox_group==null) {
			System.out.println("Missing ecotox_group for "+common_name);
			return null;
		}
		
		String egLC=ecotox_group.toLowerCase();
		
		if(egLC.contains("fish")) {
			return "Fish";
		} else if(egLC.contains("algae")) {
			return "Algae";
		} else if(egLC.contains("amphibians")) {
			return "Amphibians";
		} else if(egLC.contains("crustaceans")) {
			return "Crustaceans";
		} else if(egLC.contains("insects/spiders")) {
			return "Insects/spiders";
		} else if(egLC.contains("molluscs")) {
			return "Molluscs";
		} else if(egLC.contains("moss, hornworts")) {
			return "Moss, hornworts";
		} else if(egLC.contains("reptiles")) {
			return "Reptiles";
		} else if(egLC.contains("birds")) {
			return "Birds";
		} else if(egLC.contains("fungi")) {
			return "Fungi";
		} else if(egLC.contains("miscellaneous")) {
			return "Miscellaneous";
		} else if(egLC.contains("mammals")) {
			return "Mammals";
		} else if(egLC.contains("worms")) {
			return "Worms";
		} else if(egLC.contains("invertebrates")) {
			return "Invertebrates";
		} else if(egLC.contains("flowers, trees, shrubs, ferns")) {
			return "Flowers, trees, shrubs, ferns";
		} else if(egLC.equals("omit")) {
			return "Omit";
		} else {
			System.out.println("Handle\t"+ecotox_group);	
		}

		return null;
	}

	private String getSpeciesType() {

		if(ecotox_group==null) {
//			System.out.println("Missing ecotox_group for "+common_name);
			return null;
		}
		
		String egLC=ecotox_group.toLowerCase();
		
		if(egLC.contains("standard")) {
			return "standard";
		} else if(egLC.contains("invasive")) {
			return "invasive";
		} else if(egLC.contains("nuisance")) {
			return "nuisance";
		}  else {
			return "nondefined";
//			System.out.println("Handle "+egLC);
		}

		
	}

	//Simple class so can look at values with gson
	class BCF {
		
		public String bcf1_mean_op;
		public Double bcf1_mean;

		public String bcf1_min_op;
		public Double bcf1_min;
		
		public String bcf1_max_op;
		public Double bcf1_max;
		
		public String bcf1_unit;
		public String bcf1_comments;

		public String test_location;
		public String media_type;

		BCF(RecordEcotox r) {
			this.bcf1_mean_op=r.bcf1_mean_op;
			this.bcf1_mean=r.bcf1_mean;
			this.bcf1_min_op=r.bcf1_min_op;
			this.bcf1_min=r.bcf1_min;
			this.bcf1_max_op=r.bcf1_max_op;
			this.bcf1_max=r.bcf1_max;
			this.bcf1_unit=r.bcf1_unit;
			this.bcf1_comments=r.bcf1_comments;
			this.test_location=r.test_location;
			this.media_type=r.media_type;
		}

	}
	
	
	public ExperimentalRecord toExperimentalRecordBCF(String propertyName) {
		
		boolean limitToFish=false;
		if(propertyName.toLowerCase().contains("fish")) {
			limitToFish=true;
		}
		boolean limitToWholeBody=false;
		if(propertyName.toLowerCase().contains("whole")) {
			limitToWholeBody=true;
		}
		boolean limitToStandardTestSpecies=false;
		if(propertyName.toLowerCase().contains("standard")) {
			limitToStandardTestSpecies=true;
		}

		ExperimentalRecord er=new ExperimentalRecord();
		er.parameter_values=new ArrayList<>();
		er.property_name=propertyName;


		if(er.property_name==null) {
			System.out.println("property_name not set for "+test_id);
		}

		if(propertyName.toLowerCase().contains("bioconcentration")) {
			er.property_category="bioconcentration";
			if(!test_location.equals("Lab")) {
				er.keep=false;
				er.reason="Test location not in Lab";
			}
		} else if (propertyName.toLowerCase().contains("bioaccumulation")) {
			er.property_category="bioaccumulation";
			System.out.println("BAF test_location:"+test_location);
		}

//		System.out.println(er.property_name);
		setChemicalIdentifiers(er);
		
		
		
//		System.out.println(cas_number+"\t"+er.casrn);
		er.keep=true;

		er.property_value_units_original=bcf1_unit.replace("ml/mg", "L/g").replace("ml/g", "L/kg");
		er.property_value_point_estimate_original=bcf1_mean;
		er.property_value_numeric_qualifier=bcf1_mean_op;

//		System.out.println(bcf1_min_op+"\t"+bcf1_max_op);

		//bcf1_min_op and bcf1_max_op are null in the db
		

		setLiteratureSource(er);
		if(er.literatureSource.citation.contains("De Bruijn,J., and J. Hermens (1991)")) {
			er.keep=false;
			er.reason="Units conversion error for this journal article by ECOTOX group";
		}

		
		if(bcf1_mean!=null) {
			er.property_value_numeric_qualifier=bcf1_mean_op;
			er.property_value_point_estimate_original=bcf1_mean;	
			er.property_value_string=er.property_value_point_estimate_original+" "+bcf1_unit;//TODO

		} else if(bcf1_min!=null && bcf1_max!=null) {
			
			double log=Math.log10(bcf1_max/bcf1_min);
			if(log>1) {
				er.keep=false;
				er.reason="Range of min and max is too wide";
			} else {
//				er.property_value_point_estimate_original=Math.sqrt(bcf1_min*bcf1_max);
			}

			er.property_value_min_original=bcf1_min;
			er.property_value_max_original=bcf1_max;
			er.property_value_string=bcf1_min+" "+bcf1_unit+" < "+endpoint+" <"+bcf1_max+" "+bcf1_unit;
			
		} else if(bcf1_min!=null) {
			er.property_value_min_original=bcf1_min;
			er.property_value_string=endpoint+" > "+bcf1_min+" "+bcf1_unit;				
		} else if (bcf1_max!=null) {
			er.property_value_max_original=bcf1_max;
			er.property_value_string=endpoint+" < "+bcf1_max+" "+bcf1_unit;				
		}

		
//			System.out.println(r.conc1_max_op+"\t"+r.conc1_min_op+"\t"+r.conc1_mean_op);
		er.experimental_parameters=new LinkedHashMap<>();
		er.experimental_parameters.put("test_id", test_id);

		setSpeciesParameters(er); 
		
		if(limitToFish && ecotox_group!=null && !ecotox_group.toLowerCase().contains("fish")) {
			er.keep=false;
			er.reason="Not a fish species";
		}
		
		if(description==null) {
//			System.out.println(gson.toJson(this));
		} else {
			if(description.contains("Whole organism")) {
				er.experimental_parameters.put("Response site", "Whole body");	
			} else {
				er.experimental_parameters.put("Response site", description);
			} 
		}
		if(limitToWholeBody && (description==null || !description.equals("Whole organism")))  {
			er.keep=false;
			er.reason="Not whole body";
//			System.out.println(description);
		}

		if(limitToStandardTestSpecies && ecotox_group!=null && !ecotox_group.toLowerCase().contains("standard")) {
			er.keep=false;
			er.reason="Not a standard test species";
		}
		
		er.experimental_parameters.put("Media type", media_type);
		if (media_type.contains("water")) {
			setWaterConcentration(er);
			if (media_type.equals("Salt water")) {
				er.keep=false;
				er.reason="Salt water";
			}
		} else {
			er.keep=false;
			er.reason="Not in water";
		}

//		setExposureDuration(er);//we want the observation duration not the exposure duration
		setObservationDuration(er);
		
		er.experimental_parameters.put("Test location", test_location);
		er.experimental_parameters.put("exposure_type", exposure_type);
		er.experimental_parameters.put("chem_analysis_method", chem_analysis_method);

		
//		if(er.keep)
//			System.out.println(test_location);
		
		//TODO store t.test_radiolabel, r.additional_comments => calculation method = kinetic or conc
		//Maybe omit radiolabeled ones since have no way to know if they corrected for metabolites when
		//determining concentrations
		
//		System.out.println(wc);
		uc.convertRecord(er);
		
//		if(er.keep)
//			System.out.println(gson.toJson(new BCF(this)));

//		if(er.casrn.equals("7783-00-8")) {
//			System.out.println(er.property_value_point_estimate_final+"\t"+ er.experimental_parameters.get("Species supercategory"));
//		}

		return er;
	}


	private void setSpeciesParameters(ExperimentalRecord er) {
		er.experimental_parameters.put("Species latin", latin_name);
		er.experimental_parameters.put("Species common", common_name);
		
		String supercategory=getSpeciesSupercategory();
		if(supercategory!=null) {
			er.experimental_parameters.put("Species supercategory",supercategory);	
		}
	}


	private void setChemicalIdentifiers(ExperimentalRecord er) {
		er.dsstox_substance_id=dtxsid;
		er.source_name=sourceName;
		String CAS1=cas_number.substring(0,cas_number.length()-3);
		String CAS2=cas_number.substring(cas_number.length()-3,cas_number.length()-1);
		String CAS3=cas_number.substring(cas_number.length()-1,cas_number.length());
		er.casrn=CAS1+"-"+CAS2+"-"+CAS3;
		er.chemical_name=chemical_name;
	}


	private void setLiteratureSource(ExperimentalRecord er) {
		LiteratureSource ls=new LiteratureSource();
		er.literatureSource=ls;
		ls.name=author+" ("+publication_year+")";
		ls.author=author;
		ls.title=title;
		ls.year=publication_year;
		ls.citation=author+" ("+publication_year+"). "+title+"."+source;
		ls.doi=doi;
		
		er.reference=ls.citation;

	}


	private void setWaterConcentration(ExperimentalRecord er) {
		
//		String wc=null;
//		if(conc1_mean!=null) {
//			String conc1=conc1_mean+" "+conc1_unit;
//			if(conc1_mean_op!=null) conc1=conc1_mean_op+" "+conc1;
//			wc=conc1;
////			System.out.println(wc);
//		} else if(conc1_min!=null && conc1_max!=null) {
//			wc=conc1_min_op+" "+conc1_min+" to "+conc1_max_op+conc1_max+" "+conc1_unit; 
//			wc=wc.replace("null", "").trim();
////			System.out.println(wc);
//		} else if(conc1_max!=null) {
////			System.out.println(conc1_mean+"\t"+conc1_min+"\t"+conc1_max);
//			wc="> "+conc1_max+" "+conc1_unit; 
////			System.out.println(wc);
//		} else {
////			System.out.println("no water conc");
//		}
//		if(wc!=null) {
//		er.experimental_parameters.put("Exposure concentration",wc);
////		System.out.println(wc);
//	}

		conc1_unit=conc1_unit.replace("ug/ml", ExperimentalConstants.str_ug_mL);
		conc1_unit=conc1_unit.replace("ng/ml", ExperimentalConstants.str_ug_L);
		conc1_unit=conc1_unit.replace("nmol/L",ExperimentalConstants.str_nM);
		conc1_unit=conc1_unit.replace("nmol/ml",ExperimentalConstants.str_uM);
		conc1_unit=conc1_unit.replace("pmol/ml",ExperimentalConstants.str_nM);
		conc1_unit=conc1_unit.replace("AI ug/mL",ExperimentalConstants.str_mg_L);
		
		
		//if the units are in mass/mass these are probably organism concentration
		// and not actually the water concentration:
		if(conc1_unit.contains("ug/g") || conc1_unit.contains("ng/g") || 
				conc1_unit.contains("ug/kg") || conc1_unit.contains("mg/kg")) 
			return;//not water concentration
		
		if(conc1_unit.equals("ug") || conc1_unit.equals("ng")) {
			return;//not water concentration
		}
		
		ExperimentalRecord erWC=new ExperimentalRecord();
		erWC.property_name=ExperimentalConstants.strWaterSolubility;
		erWC.property_value_units_original=conc1_unit;
		if(conc1_mean!=null) erWC.property_value_point_estimate_original=conc1_mean;
		if(conc1_min!=null) erWC.property_value_min_original=conc1_min;
		if(conc1_max!=null) erWC.property_value_max_original=conc1_max;
		erWC.property_value_numeric_qualifier=conc1_mean_op;
		uc.convertRecord(erWC);
		
		
		if(er.keep) {			
			if(erWC.property_value_units_final==null || (!erWC.property_value_units_final.equals("g/L") && !erWC.property_value_units_final.equals("M"))) {
				conc1_units.add(conc1_unit);	
//				System.out.println(gson.toJson(this));
//				System.out.println(gson.toJson(erWC)+"\r\n");	
			}
		}
				
//		if(!erWC.property_value_units_final.equals("g/L") && !erWC.property_value_units_final.equals("M")) {
//			if(er.keep)
//				System.out.println(gson.toJson(erWC));	
//		}
 		
		//TODO instead store "Water concentration (ug/L)"
				
		ParameterValue pv=new ParameterValue();
		pv.parameter.name="Water concentration";
		pv.unit.abbreviation=erWC.property_value_units_final;
		
		pv.valuePointEstimate=erWC.property_value_point_estimate_final;
		pv.valueMin=erWC.property_value_min_final;
		pv.valueMax=erWC.property_value_max_final;
		
		if(conc1_mean_op!=null) {
			if(!conc1_mean_op.equals("~")) 			
				pv.valueQualifier=this.conc1_mean_op;	
		}
				
		er.parameter_values.add(pv);
		
//		System.out.println(er.property_value_units_original+"\t"+pv.unit.abbreviation);
				
//		er.experimental_parameters.put("Exposure concentration", pv);
		
	}


	


	

}
