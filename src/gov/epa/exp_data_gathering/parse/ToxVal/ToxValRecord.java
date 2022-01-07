package gov.epa.exp_data_gathering.parse.ToxVal;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.List;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
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
	public String dtxsid;
	public String casrn;
	public String name;
	public Integer toxval_id;
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
	public Double study_duration_value_original;
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
	public String species_common;
	public String species_supercategory;
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
	public String year;
	public Integer quality_id;
	public Integer priority_id;
	public Integer source_source_id;
	public String details_text;
	public String toxval_uuid;
	public String toxval_hash;
	public String datestamp;
	public String long_ref;
	public String url;
	
	private static final transient Class CLASS = ToxValRecord.class;
	private static final transient Field[] FIELDS = CLASS.getDeclaredFields();
	
	private static final transient UnitConverter converter = new UnitConverter("data/density.txt");
	
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
	
	public ExperimentalRecord toExperimentalRecord() {
		ExperimentalRecord rec = new ExperimentalRecord();
		
		rec.casrn = casrn.startsWith("NOCAS") ? null : casrn;
		rec.chemical_name = name;
		
		rec.property_name = species_common.replaceAll("\\s+", "_") + "_" + toxval_type;
		rec.property_value_numeric_qualifier = toxval_numeric_qualifier.equals("=") ? null : toxval_numeric_qualifier;
		rec.property_value_point_estimate_original = toxval_numeric;
		
		switch (toxval_units) {
		case "mg/L":
			rec.property_value_units_original = ExperimentalConstants.str_mg_L;
			break;
		case "g/L":
			rec.property_value_units_original = ExperimentalConstants.str_g_L;
			break;
		case "mol/L":
			rec.property_value_units_original = ExperimentalConstants.str_M;
			break;
		default:
			rec.property_value_units_original = toxval_units;
			break;
		}
		
		rec.source_name = "ToxVal";
		rec.original_source_name = source + ": " + subsource;
		rec.reference = long_ref.equals("-") ? null : long_ref;
		rec.url = url.equals("-") ? null : url;
		rec.dsstox_substance_id = dtxsid.startsWith("NODTXSID") ? null : dtxsid;
		
		converter.convertRecord(rec);
		
		return rec;
	}
	
	private Double getStudyDurationValueInDays() {
		Double studyDurationValueInDays = study_duration_value;
		switch (study_duration_units) {
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
		case "hour":
			studyDurationValueInDays /= 24.0;
			break;
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
			return false;
		}
		
		if (omitSources!=null && omitSources.contains(source)) {
			return false;
		}
		
		return true;
	}

}
