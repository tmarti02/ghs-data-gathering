package gov.epa.exp_data_gathering.parse.ToxVal;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.UnitConverter;

//TEST dataset criteria
//The ECOTOX Media Type field = FW (fresh water)--true for all queried records
//The ECOTOX Test Location field = Lab (laboratory)--NA
//The ECOTOX Conc 1 Op (ug/L) field cannot be <, >, or ~ (i.e., use only discrete LC50 values)--handled by QSAR dataset creation code
//The ECOTOX Effect field = Mor (mortality)--check critical_effect field
//The ECOTOX Effect Measurement field = MORT (mortality)--check critical_effect field
//The ECOTOX Exposure Duration field = 4 (4 days or 96 hours)--check study_duration_value field w/ conversion
//Compounds can only contain the following element symbols: C, H, O, N, F, Cl, Br, I, S, P, Si, As--handled by QSAR dataset creation code
//Compounds must represent a single pure component...--handled by QSAR dataset creation code
public class ToxValRecord {
	
	public Long bcfbaf_id;
	public String dtxsid;
	public String casrn;
	public String name;
	public Long toxval_id;
	public String source;
	public String subsource;
	public String toxval_type;
	public String toxval_type_original;
	public String toxval_subtype;
	public String toxval_subtype_original;
	public String toxval_type_supercategory;
	public String toxval_numeric_qualifier;
	public String toxval_numeric_qualifier_original;
	public Double toxval_numeric;
	public Double toxval_numeric_original;
	public Double toxval_numeric_converted;
	public String toxval_units;
	public String toxval_units_original;
	public String toxval_units_converted;
	public String risk_assessment_class;
	public String study_type;
	public String study_type_original;
	public String study_duration_class;
	public String study_duration_class_original;
	public Double study_duration_value;
	public String study_duration_value_original;
	public String study_duration_units;
	public String study_duration_units_original;
	public String human_eco;
	public String strain;
	public String strain_original;
	public String sex;
	public String sex_original;
	public String generation;
	public Integer species_id;
	public String species_original;
	
	public String species_common="";//otherwise it causes problems when doing Collectors.toList()	
	public String species_latin="";//otherwise it causes problems when doing Collectors.toList()
	public String species_supercategory;

	public String common_name;//toxvalv93 field renamed species_common
	public String ecotox_group;//toxvalv93 field- renamed species_supercategory

	
	public String habitat;
	public String lifestage;
	public String exposure_route;
	public String exposure_route_original;
	public String exposure_method;
	public String exposure_method_original;
	public String exposure_form;
	public String exposure_form_original;
	public String media;
	public String media_original;
	public String critical_effect;

	public Integer quality_id;
	public Integer priority_id;
	public Integer source_source_id;
	public String details_text;
	public String toxval_uuid;
	public String toxval_hash;
	public String datestamp;
	
//	public String record_source_id;
	public String document_name;
	public String long_ref;
	public String url;
	public String title;
	public String author;
	public String journal;
	public String volume;
	public String year;
	public String quality;
	
	//From bcfbaf
	public Double logbcf;
	public String units;
	public String tissue;
	public String calc_method;
	public String comments;
	public String water_conc;
	public String exposure_type;
	public String exposure_duration;
	public Double temperature;
	public Double pH;

	
	private static final transient Class CLASS = ToxValRecord.class;
	private static final transient Field[] FIELDS = CLASS.getDeclaredFields();
	
	private static final transient UnitConverter unitConverter = new UnitConverter("data/density.txt");
	
	public static ToxValRecord fromResultSet(ResultSet rs) {
		ToxValRecord rec = new ToxValRecord();
		for (Field field:FIELDS) {
			String typeName = field.getGenericType().getTypeName();
			String fieldName = field.getName();
			try {
				if (typeName.equals("java.lang.String")) {
					String s = rs.getString(fieldName);
					field.set(rec, s);
				} else if (typeName.equals("java.lang.Double")) {
					Double d = rs.getDouble(fieldName);
					field.set(rec, d);
				} else if (typeName.equals("java.lang.Integer")) {
					Integer i = rs.getInt(fieldName);
					field.set(rec, i);
//				} else if (typeName.equals("java.util.Date")) {
//					Date d = rs.getDate(fieldName);
//					field.set(rec, d);
				} else {
					continue;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return rec;
	}
	
	
	public static ToxValRecord fromResultSet2(ResultSet rs) {
		ToxValRecord rec = new ToxValRecord();

		String fieldName = "";
		
		ResultSetMetaData rsMetaData;
		try {

			rsMetaData = rs.getMetaData();

			for(int col = 1; col<=rsMetaData.getColumnCount(); col++) {

				Field field=ToxValRecord.class.getField(rsMetaData.getColumnLabel(col));

				String typeName = field.getGenericType().getTypeName();
				fieldName = field.getName();

				String s = rs.getString(fieldName);
				
				if(s==null) continue;
				
				if (typeName.equals("java.lang.String")) {
					if(!s.isBlank())
						field.set(rec, s);
				} else if (typeName.equals("java.lang.Double")) {
					field.set(rec, Double.parseDouble(s));	
				} else if (typeName.equals("java.lang.Long")) {
					field.set(rec, Long.parseLong(s));	

				} else if (typeName.equals("java.lang.Integer")) {
					field.set(rec, Integer.parseInt(s));	
				
				} else {
					continue;
				}
			}

		} catch (Exception ex) {
			System.out.println(fieldName);
			ex.printStackTrace();
		}
		return rec;
	}
	
	public ExperimentalRecord toExperimentalRecord(String version,double duration_days,String propertyCategory) {
		ExperimentalRecord rec = new ExperimentalRecord();
		
		rec.casrn = casrn.startsWith("NOCAS") ? null : casrn;
		rec.chemical_name = name;
		
		if (species_common.length()>0) {
			rec.property_name = species_common.replaceAll("\\s+", "_") + "_" + toxval_type;//TODO where did this come from?	
		} else {
			int duration_hours=(int)(duration_days*24.0);
			rec.property_name = duration_hours+" hour "+common_name.toLowerCase() + " " + toxval_type;
		}
		
		rec.property_category=propertyCategory;
		
		String tnq=toxval_numeric_qualifier;
		
		if (tnq!=null) {
			if(tnq.equals("=") || tnq.isBlank()) tnq=null;
			rec.property_value_numeric_qualifier=tnq;
		} else {
			rec.property_value_numeric_qualifier=null;	
		}
		
		if(rec.property_value_numeric_qualifier!=null) {
			
			String q=rec.property_value_numeric_qualifier;
			
			if(!q.equals("~")) {
				rec.keep=false;
				rec.reason="Bad property_value_numeric_qualifier";
//				System.out.println(q+"\tBad qualifier");
			} 
		}
		

//		System.out.println(tnq+"\t"+rec.property_value_numeric_qualifier);
		
		
		rec.property_value_point_estimate_original = toxval_numeric;
		
		setOriginalUnits(rec);

		addExperimentalParameters(rec);
		
		setDataSources(rec,version);
				
		rec.dsstox_substance_id = dtxsid.startsWith("NODTXSID") ? null : dtxsid;

		//Convert Units
		unitConverter.convertRecord(rec);
		
		if(rec.property_value_point_estimate_final==null && (rec.property_value_max_final==null || rec.property_value_min_final==null)) {
			System.out.println(rec.dsstox_substance_id+"\tno point estimate possible");
			rec.keep=false;
			rec.reason="Can't generate point estimate";
		}
		
		
		return rec;
	}
	
	
	
	/**
	 * tissue = whole body (set in sql query)
	 * 
	 * 
	 * method = Steady state vs Kinetic
	 * exposure_type = S vs FT (static vs flowthrough)
	 * exposure_duration
	 * media = FW vs SW vs Humic water vs -
	 * 
	 * @param version
	 * @param propertyCategory
	 * @return
	 */
	public ExperimentalRecord toxvalBCF_to_ExperimentalRecord(String version,String propertyName, String propertyCategory) {
		ExperimentalRecord er = new ExperimentalRecord();
		
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = new Date();
		String strDate = formatter.format(date);
		String dayOnly = strDate.substring(0, strDate.indexOf(" "));
		er.date_accessed=dayOnly;
		
		er.casrn = casrn.startsWith("NOCAS") ? null : casrn;
		er.chemical_name = name;
		
		er.property_name=propertyName;
		er.property_category=propertyCategory;
		
		er.property_value_string=logbcf+" log10("+units+")";		
		er.property_value_point_estimate_original = logbcf;
		er.property_value_units_original="log10("+units+")";		
		
		er.note=getString(comments);
		
		er.experimental_parameters=new Hashtable<>();
//		rec.experimental_parameters.put("tissue",tissue);//already have whole body in property name
		er.experimental_parameters.put("Species common",species_common);
		er.experimental_parameters.put("Species latin",species_latin);
		
		if(tissue!=null)
			er.experimental_parameters.put("Response site",tissue);

//		rec.experimental_parameters.put("water_concentration",water_conc);
//		rec.experimental_parameters.put("media",media);
//		System.out.println(media);
//		rec.experimental_parameters.put("exposure_type",exposure_type);
//		rec.experimental_parameters.put("exposure_duration",exposure_duration);		
//		if(temperature!=null) rec.experimental_parameters.put("Temperature",temperature);
		
		if(calc_method!=null) {
			if (calc_method.equals("Cb/Cw")) {
				er.experimental_parameters.put("method","Steady state");
			} else if (calc_method.equals("K1/K2")) {
				er.experimental_parameters.put("method","Kinetic");
			} else if (calc_method.contains("BIO")){
				er.experimental_parameters.put("method",calc_method);
				er.keep=false;
				er.reason="Can't assess method validity";
			} else {
				er.experimental_parameters.put("method",calc_method);
			}
			
		}
		er.temperature_C=temperature;
				
		if(pH!=null && pH>0) {
//			rec.experimental_parameters.put("pH",pH);
			er.pH=pH+"";
		}
		
		er.source_name = "ToxVal_"+version;		
		er.original_source_name = "Arnot, J.A. and Gobas, F.A.P.C. (2006). A Review of Bioconcentration Factor (BCF) and Bioaccumulation Factor (BAF) Assessments for Organic Chemicals in Aquatic Organisms. Environmental Reviews, 14, 257-297";
		addLiteratureSource2(er);
				
		er.dsstox_substance_id = dtxsid.startsWith("NODTXSID") ? null : dtxsid;
		
		er.property_category="bioconcentration";
		
		
		//Convert Units
		unitConverter.convertRecord(er);
		
//		System.out.println(er.property_value_units_original+"\t"+er.property_value_units_final);

		
		return er;
	}


	void addExperimentalParameters(ExperimentalRecord er) {
		//Add experimental parameters:
		er.experimental_parameters=new Hashtable<>();
		
		if (!exposure_route.equals("-") &&  !exposure_route.equals("Not reported")) {
			er.experimental_parameters.put("Exposure route", exposure_route);
		}
		
		if (!lifestage.equals("-") &&  !lifestage.equals("Not reported")) {
			er.experimental_parameters.put("Lifestage", lifestage);				
		}

		
		if (!quality.equals("-")) {
			er.experimental_parameters.put("Reliability", quality);
		}
		
		er.experimental_parameters.put("toxval_id", toxval_id+"");
	}
	
	private void setDataSources(ExperimentalRecord rec,String version) {
		
		rec.source_name = "ToxVal_"+version;

		
		if (subsource.equals("-") || subsource.equals("EFSA")) {
			rec.original_source_name = source;
//			System.out.println(source+"\t"+subsource);
		} else {
			rec.original_source_name = source + ": " + subsource;
//			System.out.println(rec.original_source_name);
		}
		
		if (rec.original_source_name.contains("ECHA eChemPortal") || 
				rec.original_source_name.equals("EnviroTox_v2")) {

			//doesnt have literature citation so just store url in the rec
			rec.url=getString(url);

		} else if (rec.original_source_name.equals("DOD ERED: USACE_ERDC_ERED_database_12_07_2018") || 
				rec.original_source_name.equals("ECOTOX: EPA ORD") || 
				rec.original_source_name.equals("EFSA")) {
			
			//Create literature source
			addLiteratureSource(rec);
			
//			Gson gson=new Gson();
//			System.out.println(gson.toJson(rec.literatureSource));
//			System.out.println(rec.literatureSource.documentName);
			
		} else {
			System.out.println(rec.original_source_name+": need to set whether is literature or public source in in ToxValRecord class");
		}
	}


	private void setOriginalUnits(ExperimentalRecord rec) {
		switch (toxval_units) {
		
		case "% v/v":
			rec.property_value_units_original = ExperimentalConstants.str_pctVol;
			break;
		case "mg/L":
			rec.property_value_units_original = ExperimentalConstants.str_mg_L;
			break;
		case "g/L":
			rec.property_value_units_original = ExperimentalConstants.str_g_L;
			break;
		case "mol/L":
			rec.property_value_units_original = ExperimentalConstants.str_M;
			break;
		case "nM/L":
			rec.property_value_units_original = ExperimentalConstants.str_nM;
//			nM/L	nmol/L
			break;
		case "uM/L":
			rec.property_value_units_original = ExperimentalConstants.str_uM;
//			uM/L	umol/L
			break;
		case "mM/L":
			rec.property_value_units_original = ExperimentalConstants.str_mM;
//			mM/L	mmol/L
			break;
		default:
			rec.property_value_units_original = toxval_units;
//			System.out.println(toxval_units);
			break;
		}
	}


	private void addLiteratureSource(ExperimentalRecord rec) {
		rec.literatureSource=new LiteratureSource();
//			rec.literatureSource.recordSourceId=record_source_id;
//			rec.literatureSource.documentName=getString(document_name);
		
		//Have to fix this one, only one like it:
		if(long_ref.equals("Hickie BE, LS McCarty, DG Dixon.1995.Environmental Toxicology and Chemistry 14:2187-2197")) {
			author="Hickie B.E., L.S. McCarty, D.G. Dixon";
			year="1995";
			title="Development and testing of a residue-based toxicokinetic model for predicting acute aquatic toxicity from pulse exposures";
			long_ref="Environmental Toxicology and Chemistry 14:2187-2197";
		}
		
		rec.literatureSource.author=getString(author);
		rec.literatureSource.url =getString(url); 
		rec.literatureSource.title = getString(title);
		rec.literatureSource.journal = getString(journal);
		rec.literatureSource.volume = getString(volume);
		rec.literatureSource.year = getString(year);
		
		String citation="";
		
		if(rec.literatureSource.author!=null && !long_ref.contains(rec.literatureSource.author)) {
			citation=rec.literatureSource.author+" ";
		}
		
		if(rec.literatureSource.year!=null && !long_ref.contains(rec.literatureSource.year)) {
			citation+="("+rec.literatureSource.year+"). ";
		}
		
		if(rec.literatureSource.title!=null && !long_ref.contains(rec.literatureSource.title)) {
			citation+=rec.literatureSource.title+". ";
		}

		citation+=long_ref;
				
		rec.literatureSource.citation=citation;

		if(rec.literatureSource.author!=null && rec.literatureSource.year!=null) {
			rec.literatureSource.name=rec.literatureSource.author+"_"+rec.literatureSource.year;//makes it have unique name for the database constraint for literature_sources table
		} else {
			rec.literatureSource.name=citation;
		}
		
//		System.out.println(citation);
		
	}
	
	private void addLiteratureSource2(ExperimentalRecord rec) {
		rec.literatureSource=new LiteratureSource();
//			rec.literatureSource.recordSourceId=record_source_id;
//			rec.literatureSource.documentName=getString(document_name);
		
		
		rec.literatureSource.author=getString(author);
		rec.literatureSource.url =getString(url); 
		rec.literatureSource.title = getString(title);
		rec.literatureSource.journal = getString(journal);
		rec.literatureSource.volume = getString(volume);
		rec.literatureSource.year = getString(year);
		
		String citation="";
		
		if(rec.literatureSource.author!=null) {
			citation=rec.literatureSource.author+" ";
		}
		
		if(rec.literatureSource.year!=null) {
			citation+="("+rec.literatureSource.year+"). ";
		}
		
		if(rec.literatureSource.title!=null) {
			citation+=rec.literatureSource.title+". ";
		}
		
		if(rec.literatureSource.journal!=null) {
			citation+=rec.literatureSource.journal+". ";
		}

		citation=citation.replace("..", ".");

		rec.literatureSource.citation=citation;
		rec.literatureSource.name=author+" ("+rec.literatureSource.year+")";//makes it have unique name for the database constraint for literature_sources table
		rec.reference=citation;
//		System.out.println(citation);
		
	}

	
	String getString(String fieldValue) {
		if(fieldValue==null) return null;		
		else {
			fieldValue=fieldValue.trim();
			if(fieldValue.isEmpty() || fieldValue.equals("-")) return null;
			else return fieldValue;
		}
	}
	
	
	private Double getStudyDurationValueInDays() {
		Double studyDurationValueInDays = study_duration_value;
		switch (study_duration_units) {
		
		case "days post-hatch":
		case "Days":
		case "days":
		case "day":
			break;
		case "week":
			studyDurationValueInDays *= 7.0;
			break;
		case "month":
			studyDurationValueInDays *= 30.0;
			break;
		case "year":
			studyDurationValueInDays *= 365.0;
			break;
		case "hours":
		case "Hours":
		case "hour":
			studyDurationValueInDays /= 24.0;
			break;
		case "minutes":
		case "minute":
			studyDurationValueInDays /= 1440.0;
			break;
		case "-":
//			System.out.println("No study duration units for ToxVal ID " + toxval_id);
			studyDurationValueInDays = null;
			break;
		default:
			System.out.println("Unknown study duration units for ToxVal ID " + toxval_id + ": " + study_duration_units);
			studyDurationValueInDays = null;
			break;
		}
		
		return studyDurationValueInDays;
	}
	
	public boolean isAcceptable(Double duration, String criticalEffect, List<String> omitSources) {
		Double studyDurationValueInDays = getStudyDurationValueInDays();
		if (studyDurationValueInDays==null
				|| studyDurationValueInDays < 0.95 * duration
				|| studyDurationValueInDays > 1.05 * duration) {
			return false;
		}
		
		if (!critical_effect.toLowerCase().contains(criticalEffect.toLowerCase())) {
//			System.out.println("Bad critical effect:"+critical_effect);
			return false;
		}
		
		if (omitSources!=null && omitSources.contains(source)) {
			return false;
		}
		
		return true;
	}

}

