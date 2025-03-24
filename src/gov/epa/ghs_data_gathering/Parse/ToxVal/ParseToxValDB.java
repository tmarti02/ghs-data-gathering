package gov.epa.ghs_data_gathering.Parse.ToxVal;


import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;

import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_bcfbaf.ParseToxValBCFBAF;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_bcfbaf.RecordToxValBCFBAF;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_cancer_summary.ParseToxValCancer;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_cancer_summary.RecordToxValCancer;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_genetox_summary.ParseToxValGenetox;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_genetox_summary.RecordToxValGenetox;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_models.ParseToxValModels;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_models.RecordToxValModels;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval.ParseToxVal;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval.RecordToxVal;


/**
 * From Risa 8/2024:
 * supercategories have changed, priority, toxval_uuid, details_text have been deprecated, 
 * toxval_hash is now called source_hash, source_source_id is now called external_source_id 
 * 
these are the effect categories we'll have (categorized for a subset)
 
body weight
clinical chemistry
clinical signs
development
enzyme activity
food/water consumption
gross pathology
hematology
mortality/survival
multiple
neurobehavior
nonneoplastic histopathology
urinalysis
neurotransmitter
organ weight
other
reproduction
none
 
so maybe you could just use "neurobehavior" and "neurotransmitter"

ECO sources being retired are: TOXVAL-829:
- DOD ERED
- EnviroTox_v2
- DOD LANL ECORISK
- EPA OW NRWQC-ALC
- EPA OW OPP-ALB
 
 */

public class ParseToxValDB {


	//	public static final String DB_Path_AA_Dashboard_Records = "C:\\Users\\Leora\\Desktop\\Tele\\ToxVal\\databases\\toxval_v8.db";//fast if you add index for CAS: "CREATE INDEX idx_CAS ON "+tableName+" (CAS)"

	//use relative path so dont have to keep changing this- i.e. it is relative to java installation:  "D:\Users\TMARTI02\OneDrive - Environmental Protection Agency (EPA)\0 java\ghs-data-gathering\AA Dashboard\databases\toxval_v8.db"
	//	public static final String DB_Path_AA_Dashboard_Records = "AA Dashboard/databases/toxval_v8.db";
	public static final String DB_Path_AA_Dashboard_Records_v8 = "databases/toxval_v8.db";
	public static final String DB_Path_AA_Dashboard_Records_v94 = "databases/toxval_v94.db";
	public static final String DB_Path_AA_Dashboard_Records_v96 = "databases/toxval_v96.db";

	//  The "databases/toxval_v8.db" folder didn't work even when I moved the database there, so I switched it back to the "AA Dashboard/databases/toxval_v8.db" folder.
	public static Statement statToxValv8 = MySQL_DB.getStatement(DB_Path_AA_Dashboard_Records_v8);


	private static final Logger logger = LogManager.getLogger(ParseToxValDB.class);

	public static final String v8="v8";
	public static final String v94="v94";
	public static final String v96="v96";

	public static boolean debug=false;

	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();

	/**
	 * Get data from toxval table by CAS
	 * 
	 * @param CAS
	 * @return
	 */
	@Deprecated
	private String createSQLQuery_toxval(String CAS) {

		String SQL="SELECT ";

		for(String field : RecordToxVal.varlist) {    		

			// The following fields come from the chemical_list table:
			if(field.contentEquals("casrn")) continue;//skip it
			if(field.contentEquals("name")) continue;

			// The following fields come from the toxval_type_dictionary:
			if (field.contentEquals("toxval_type_supercategory")) continue; //do we need this field? need to add another join

			// The following fields come from the species table, not the toxval table:
			if (field.contentEquals("species_common")) continue; 
			if (field.contentEquals("species_supercategory")) continue; 
			if (field.contentEquals("habitat")) continue; 

			// The following fields come from the record_source table, not the toxval table:
			if (field.contentEquals("long_ref")) continue; 
			if (field.contentEquals("title")) continue; 
			if (field.contentEquals("author")) continue; 
			if (field.contentEquals("journal")) continue; 
			if (field.contentEquals("volume")) continue; 
			if (field.contentEquals("issue")) continue; 
			if (field.contentEquals("url")) continue; 
			if (field.contentEquals("document_name")) continue; 
			if (field.contentEquals("record_source_type")) continue; 
			if (field.contentEquals("record_source_hash")) continue; 

			SQL+="toxval."+field+", ";    		
		}

		SQL+="chemical.casrn, chemical.name, ";
		SQL+="species.species_common, species.species_supercategory, species.habitat, \n";
		SQL+="long_ref, title, author, journal, volume, issue, url, document_name, record_source_type, record_source_hash \n";

		SQL+="FROM chemical\n";

		SQL+="LEFT JOIN toxval ON toxval.dtxsid = chemical.dtxsid\n";
		SQL+="LEFT JOIN species ON toxval.species_id=species.species_id\n";
		SQL+="LEFT JOIN record_source ON toxval.toxval_id=record_source.toxval_id\n";

		SQL+="WHERE chemical.casrn=\""+CAS+"\";";		

		//		System.out.println("\n"+SQL);

		return SQL;



	}



	/**
	 * Modified version 2 to not retrieve data from record_source table so that have 1 record per toxval_id 
	 * 
	 * Note: did not filter on human_eco field (since we need some eco data for aquatic tox)
	 * 
	 * @param CAS
	 * @return
	 */
	@Deprecated
	private String createSQLQuery_toxval_v8_by_CAS(String CAS) {

		String SQL="SELECT\r\na.dtxsid, a.casrn,a.name,\r\n" + 
				"b.toxval_id, b.source,b.subsource,b.toxval_type,b.toxval_type_original,b.toxval_subtype,b.toxval_subtype_original,e.toxval_type_supercategory,\r\n" + 
				"b.toxval_numeric_qualifier,b.toxval_numeric_qualifier_original,b.toxval_numeric,b.toxval_numeric_original,\r\n" + 
				"b.toxval_numeric_converted, b.toxval_units,b.toxval_units_original,b.toxval_units_converted, b.risk_assessment_class,\r\n" + 
				"b.study_type,b.study_type_original,b.study_duration_class,b.study_duration_class_original, b.study_duration_value,\r\n" + 
				"b.study_duration_value_original,b.study_duration_units,b.study_duration_units_original,b.human_eco,\r\n" + 
				"b.strain,b.strain_original,b.sex,b.sex_original,b.generation,\r\n" + 
				"d.species_id,b.species_original,\r\n" + 
				"d.species_common,d.species_supercategory,d.habitat,\r\n" + 
				"b.lifestage,b.exposure_route,b.exposure_route_original,b.exposure_method,b.exposure_method_original,\r\n" + 
				"b.exposure_form,b.exposure_form_original, b.media,b.media_original,b.critical_effect,b.year,b.quality_id,b.priority_id,\r\n" + 
				"b.source_source_id,b.details_text,b.toxval_uuid,b.toxval_hash,b.datestamp\r\n" + 
				//				"c.long_ref, c.title, c.author, c.journal, c.volume, c.issue, c.url, c.document_name, c.record_source_type, c.record_source_hash\r\n" + 
				"\r\n" + 

				"FROM toxval b\r\n" + 
				"INNER JOIN chemical a on a.dtxsid=b.dtxsid\r\n" + 
				"LEFT JOIN species d on b.species_id=d.species_id\r\n" + 
				"INNER JOIN toxval_type_dictionary e on b.toxval_type=e.toxval_type\r\n" + 
				//				"LEFT JOIN record_source c ON b.toxval_id=c.toxval_id\r\n" +				
				"WHERE\r\n"+
				"b.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')\r\n" + 				
				"AND e.toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')\r\n" + 
				"AND b.toxval_numeric>0\r\n" + 									
				"AND a.casrn=\""+CAS+"\";";		

		//		System.out.println("\n"+SQL);

		return SQL;


	}

	/**
	 * Modified version 2 to not retrieve data from record_source table so that have 1 record per toxval_id 
	 * 
	 * Note: did not filter on human_eco field (since we need some eco data for aquatic tox)
	 * 
	 * @param CAS
	 * @return
	 */
	private String createSQLQuery_toxval_v8_by_DTXSID(String dtxsid) {

		String SQL="SELECT b.dtxsid, b.toxval_id, b.source,b.subsource,b.toxval_type,b.toxval_type_original,b.toxval_subtype,b.toxval_subtype_original,e.toxval_type_supercategory,\r\n" + 
				"b.toxval_numeric_qualifier,b.toxval_numeric_qualifier_original,b.toxval_numeric,b.toxval_numeric_original,\r\n" + 
				"b.toxval_numeric_converted, b.toxval_units,b.toxval_units_original,b.toxval_units_converted, b.risk_assessment_class,\r\n" + 
				"b.study_type,b.study_type_original,b.study_duration_class,b.study_duration_class_original, b.study_duration_value,\r\n" + 
				"b.study_duration_value_original,b.study_duration_units,b.study_duration_units_original,b.human_eco,\r\n" + 
				"b.strain,b.strain_original,b.sex,b.sex_original,b.generation,\r\n" + 
				"d.species_id,b.species_original,\r\n" + 
				"d.species_common,d.species_supercategory,d.habitat,\r\n" + 
				"b.lifestage,b.exposure_route,b.exposure_route_original,b.exposure_method,b.exposure_method_original,\r\n" + 
				"b.exposure_form,b.exposure_form_original, b.media,b.media_original,b.critical_effect,b.year,b.quality_id,b.priority_id,\r\n" + 
				"b.source_source_id,b.details_text,b.toxval_uuid,b.toxval_hash,b.datestamp\r\n" + 
				//				"c.long_ref, c.title, c.author, c.journal, c.volume, c.issue, c.url, c.document_name, c.record_source_type, c.record_source_hash\r\n" + 
				"\r\n" + 

				"FROM toxval b\r\n" + 
				"LEFT JOIN species d on b.species_id=d.species_id\r\n" + 
				"INNER JOIN toxval_type_dictionary e on b.toxval_type=e.toxval_type\r\n" + 
				//				"LEFT JOIN record_source c ON b.toxval_id=c.toxval_id\r\n" +				
				"WHERE\r\n"+
				"b.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')\r\n" + 				
				"AND e.toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')\r\n" + 
				"AND b.toxval_numeric>0\r\n" + 									
				"AND b.dtxsid='"+dtxsid+"';";		

		//		System.out.println("\n"+SQL);

		return SQL;


	}

	/**
	 * Modified version 2 to not retrieve data from record_source table so that have 1 record per toxval_id 
	 * 
	 * Note: did not filter on human_eco field (since we need some eco data for aquatic tox)
	 * 
	 * @param CAS
	 * @return
	 */
	@Deprecated
	private String createSQLQuery_toxval_v94_by_CAS(String CAS) {

		String SQL="SELECT\r\na.dtxsid, a.casrn,a.name,\r\n" + 
				"b.toxval_id, b.source,b.subsource,b.toxval_type,b.toxval_type_original,b.toxval_subtype,b.toxval_subtype_original,e.toxval_type_supercategory,\r\n" + 
				"b.toxval_numeric_qualifier,b.toxval_numeric_qualifier_original,b.toxval_numeric,b.toxval_numeric_original,\r\n" + 
				"b.toxval_numeric_converted, b.toxval_units,b.toxval_units_original,b.toxval_units_converted, b.risk_assessment_class,\r\n" + 
				"b.study_type,b.study_type_original,b.study_duration_class,b.study_duration_class_original, b.study_duration_value,\r\n" + 
				"b.study_duration_value_original,b.study_duration_units,b.study_duration_units_original,b.human_eco,\r\n" + 
				"b.strain,b.strain_original,b.sex,b.sex_original,b.generation,\r\n" + 
				"d.species_id,b.species_original,\r\n" + 
				"d.latin_name as species_scientific, d.common_name as species_common,d.ecotox_group as species_supercategory,\r\n" + 
				"b.lifestage,b.exposure_route,b.exposure_route_original,b.exposure_method,b.exposure_method_original,\r\n" + 
				//				"b.exposure_form,b.exposure_form_original, b.media,b.media_original,b.critical_effect,b.year,b.quality_id,b.priority_id,\r\n" + 
				"b.exposure_form,b.exposure_form_original, b.media,b.media_original,b.critical_effect,b.year,b.priority_id,\r\n" + 
				"b.source_source_id,b.details_text,b.toxval_uuid,b.toxval_hash,b.datestamp\r\n" + 
				//				"c.long_ref, c.title, c.author, c.journal, c.volume, c.issue, c.url, c.document_name, c.record_source_type, c.record_source_hash\r\n" + 
				"\r\n" + 

				"FROM toxval b\r\n" + 
				"INNER JOIN chemical a on a.dtxsid=b.dtxsid\r\n" + 
				"LEFT JOIN species d on b.species_id=d.species_id\r\n" + 
				"INNER JOIN toxval_type_dictionary e on b.toxval_type=e.toxval_type\r\n" + 
				//				"LEFT JOIN record_source c ON b.toxval_id=c.toxval_id\r\n" +				
				
				getToxValFilterV94()+
				"AND a.casrn=\""+CAS+"\";";		

		//		System.out.println("\n"+SQL);

		return SQL;


	}


	
	/**
	 * Search by SID instead of CAS since the cas and name in chemical table isnt from the original source
	 * 
	 * NOTE: it wasn't necessary to create extra database columns for columns like species_common because can use sql to label it to match the java variable name

	 * 
	 * @param dtxsid
	 * @return
	 */
	private String createSQLQuery_toxval_v94_by_DTXSID(String dtxsid) {

		String SQL="SELECT b.dtxsid, b.toxval_id, b.source,b.subsource,b.toxval_type,b.toxval_type_original,b.toxval_subtype,b.toxval_subtype_original,e.toxval_type_supercategory,\r\n" + 
				"b.toxval_numeric_qualifier,b.toxval_numeric_qualifier_original,b.toxval_numeric,b.toxval_numeric_original,\r\n" + 
				"b.toxval_numeric_converted, b.toxval_units,b.toxval_units_original,b.toxval_units_converted, b.risk_assessment_class,\r\n" + 
				"b.study_type,b.study_type_original,b.study_duration_class,b.study_duration_class_original, b.study_duration_value,\r\n" + 
				"b.study_duration_value_original,b.study_duration_units,b.study_duration_units_original,b.human_eco,\r\n" + 
				"b.strain,b.strain_original,b.sex,b.sex_original,b.generation,\r\n" + 
				"d.species_id,b.species_original,\r\n" +
				//				"d.latin_name as species_scientific, d.common_name as species_common,d.ecotox_group as species_supercategory,\r\n" + 
				"d.latin_name as species_scientific, d.species_common, d.species_supercategory,\r\n" + 
				"b.lifestage,b.exposure_route,b.exposure_route_original,b.exposure_method,b.exposure_method_original,\r\n" + 
				"b.exposure_form,b.exposure_form_original, b.media,b.media_original,b.critical_effect,b.year,b.priority_id,\r\n" + 
				"b.source_source_id,b.details_text,b.toxval_uuid,b.toxval_hash,b.datestamp,\r\n" + 
				"c.long_ref, c.title, c.author, c.journal, c.volume, c.issue, c.url, c.document_name, c.record_source_type, c.record_source_hash\r\n" + 
				"\r\n" + 

				"FROM toxval b\r\n" + 
				"LEFT JOIN species d on b.species_id=d.species_id\r\n" + 
				"JOIN toxval_type_dictionary e on b.toxval_type=e.toxval_type\r\n" + 
				"JOIN record_source c ON b.toxval_id=c.toxval_id\r\n" +				
				getToxValFilterV94()+
				"AND b.dtxsid='"+dtxsid+"';";		

		//		System.out.println("\n"+SQL);

		return SQL;


	}
	
	public String createSQLQuery_toxval_v94() {

		String SQL="SELECT b.dtxsid, b.toxval_id, b.source,b.subsource,b.toxval_type,b.toxval_type_original,b.toxval_subtype,b.toxval_subtype_original,e.toxval_type_supercategory,\r\n" + 
				"b.toxval_numeric_qualifier,b.toxval_numeric_qualifier_original,b.toxval_numeric,b.toxval_numeric_original,\r\n" + 
				"b.toxval_numeric_converted, b.toxval_units,b.toxval_units_original,b.toxval_units_converted, b.risk_assessment_class,\r\n" + 
				"b.study_type,b.study_type_original,b.study_duration_class,b.study_duration_class_original, b.study_duration_value,\r\n" + 
				"b.study_duration_value_original,b.study_duration_units,b.study_duration_units_original,b.human_eco,\r\n" + 
				"b.strain,b.strain_original,b.sex,b.sex_original,b.generation,\r\n" + 
				"d.species_id,b.species_original,\r\n" +
				//				"d.latin_name as species_scientific, d.common_name as species_common,d.ecotox_group as species_supercategory,\r\n" + 
				"d.latin_name as species_scientific, d.species_common, d.species_supercategory,\r\n" + 
				"b.lifestage,b.exposure_route,b.exposure_route_original,b.exposure_method,b.exposure_method_original,\r\n" + 
				"b.exposure_form,b.exposure_form_original, b.media,b.media_original,b.critical_effect,b.year,b.priority_id,\r\n" + 
				"b.source_source_id,b.details_text,b.toxval_uuid,b.toxval_hash,b.datestamp,\r\n" + 
				"c.long_ref, c.title, c.author, c.journal, c.volume, c.issue, c.url, c.document_name, c.record_source_type, c.record_source_hash\r\n" + 
				"\r\n" + 

				"FROM toxval b\r\n" + 
				"LEFT JOIN species d on b.species_id=d.species_id\r\n" + 
				"JOIN toxval_type_dictionary e on b.toxval_type=e.toxval_type\r\n" + 
				"JOIN record_source c ON b.toxval_id=c.toxval_id\r\n" +				
				getToxValFilterV94();
		//		System.out.println("\n"+SQL);

		return SQL;


	}
	
	public static String createSQLQuery_toxval_v94_no_record_source() {

		String SQL="SELECT b.dtxsid, b.toxval_id, b.source,b.subsource,b.toxval_type,b.toxval_type_original,b.toxval_subtype,b.toxval_subtype_original,\r\n" + 
				"b.toxval_numeric_qualifier,b.toxval_numeric_qualifier_original,b.toxval_numeric,b.toxval_numeric_original,\r\n" + 
				"b.toxval_numeric_converted, b.toxval_units,b.toxval_units_original,b.toxval_units_converted, b.risk_assessment_class,\r\n" + 
				"b.study_type,b.study_type_original,b.study_duration_class,b.study_duration_class_original, b.study_duration_value,\r\n" + 
				"b.study_duration_value_original,b.study_duration_units,b.study_duration_units_original,b.human_eco,\r\n" + 
				"b.strain,b.strain_original,b.sex,b.sex_original,b.generation,\r\n" + 
				"d.species_id,b.species_original,\r\n" +
				//				"d.latin_name as species_scientific, d.common_name as species_common,d.ecotox_group as species_supercategory,\r\n" + 
				"d.latin_name as species_scientific, d.species_common, d.species_supercategory,\r\n" + 
				"b.lifestage,b.exposure_route,b.exposure_route_original,b.exposure_method,b.exposure_method_original,\r\n" + 
				"b.exposure_form,b.exposure_form_original, b.media,b.media_original,b.critical_effect,b.year,b.priority_id,\r\n" + 
				"b.source_source_id,b.details_text,b.toxval_uuid,b.toxval_hash,b.datestamp\r\n" + 
//				"c.long_ref, c.title, c.author, c.journal, c.volume, c.issue, c.url, c.document_name, c.record_source_type, c.record_source_hash\r\n" + 
				"\r\n" + 

				"FROM toxval b\r\n" + 
				"LEFT JOIN species d on b.species_id=d.species_id\r\n" + 
//				"JOIN toxval_type_dictionary e on b.toxval_type=e.toxval_type\r\n" + 
//				"JOIN record_source c ON b.toxval_id=c.toxval_id\r\n" +				
				getToxValFilterV94();		

		//		System.out.println("\n"+SQL);

		return SQL;


	}
	
	public String createSQLQuery_toxval_v94_name_cas() {

		String SQL="SELECT b.dtxsid,c2.casrn,c2.name, b.toxval_id, b.source,b.subsource,b.toxval_type,b.toxval_type_original,b.toxval_subtype,b.toxval_subtype_original,e.toxval_type_supercategory,\r\n" + 
				"b.toxval_numeric_qualifier,b.toxval_numeric_qualifier_original,b.toxval_numeric,b.toxval_numeric_original,\r\n" + 
				"b.toxval_numeric_converted, b.toxval_units,b.toxval_units_original,b.toxval_units_converted, b.risk_assessment_class,\r\n" + 
				"b.study_type,b.study_type_original,b.study_duration_class,b.study_duration_class_original, b.study_duration_value,\r\n" + 
				"b.study_duration_value_original,b.study_duration_units,b.study_duration_units_original,b.human_eco,\r\n" + 
				"b.strain,b.strain_original,b.sex,b.sex_original,b.generation,\r\n" + 
				"d.species_id,b.species_original,\r\n" +
				//				"d.latin_name as species_scientific, d.common_name as species_common,d.ecotox_group as species_supercategory,\r\n" + 
				"d.latin_name as species_scientific, d.species_common, d.species_supercategory,\r\n" + 
				"b.lifestage,b.exposure_route,b.exposure_route_original,b.exposure_method,b.exposure_method_original,\r\n" + 
				"b.exposure_form,b.exposure_form_original, b.media,b.media_original,b.critical_effect,b.year,b.priority_id,\r\n" + 
				"b.source_source_id,b.details_text,b.toxval_uuid,b.toxval_hash,b.datestamp,\r\n" + 
				"c.long_ref, c.title, c.author, c.journal, c.volume, c.issue, c.url, c.document_name, c.record_source_type, c.record_source_hash\r\n" + 
				"\r\n" + 

				"FROM toxval b\r\n" + 
				"LEFT JOIN species d on b.species_id=d.species_id\r\n" + 
				"JOIN toxval_type_dictionary e on b.toxval_type=e.toxval_type\r\n" + 
				"JOIN record_source c ON b.toxval_id=c.toxval_id\r\n" +				
				"JOIN chemical c2 ON c2.dtxsid=b.dtxsid\r\n" +
				getToxValFilterV94()+";"; 

		//		System.out.println("\n"+SQL);

		return SQL;


	}

	/**
	 * Gets record_sources for ones with data
	 * @return
	 */
	public static String createRecordSourceSqlV96() {
		
//		String SQL="SELECT rs.toxval_id, rs.long_ref,rs.title,rs.author,rs.journal,rs.volume,rs.issue,rs.url,rs.document_name,rs.record_source_type\r\n"
//				+ "		FROM toxval t\r\n"
//				+ "		JOIN record_source rs ON t.toxval_id=rs.toxval_id ;";
		
		String SQL="SELECT rs.toxval_id, rs.long_ref,rs.title,rs.author,rs.journal,rs.volume,rs.issue,rs.url,rs.document_name,rs.record_source_type\r\n"
				+ "FROM toxval t\r\n"
				+ "\tJOIN record_source rs ON t.toxval_id=rs.toxval_id\r\n"
				+ getToxValFilterV96();
		return SQL; 	
	}
	
	

	public static String createRecordSourceSqlV94() {
		
		//Just get the necessary ones:
		String SQL="SELECT rs.toxval_id, rs.long_ref,rs.title,rs.author,rs.journal,rs.volume,"
				+ "rs.issue,rs.url,rs.document_name,rs.record_source_type\r\n"
				+ "FROM toxval t\r\n"
				+ "\tJOIN record_source rs ON t.toxval_id=rs.toxval_id\r\n"
				+getToxValFilterV94();
		return SQL; 	
	}
	
	static String getToxValFilterV94() {
		return getToxValFilter();
	}
	
	static String getToxValFilterV96() {
		return getToxValFilter();
	}

	
	
//	String getToxValTypeSuperCategoryV96() {
//		return "AND toxval_type_supercategory in ('Dose Response Summary Value','Toxicity Value','Mortality Response Summary Value')";
//	}
//	
//	String getToxValTypeSuperCategoryV94() {
//		return "AND toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')"; 
//	}
//
//	
//	String getToxValFilterV94() {
//		return "WHERE\r\n"+ getUnitsFilter()+ getToxValTypeSuperCategoryV94()+"\r\n"
//		+ "AND toxval_numeric>0";
//	}
//	
//	String getToxValFilterV96() {
//		return "WHERE\r\n"+ getUnitsFilter()+ getToxValTypeSuperCategoryV96()+"\r\n"
//		+ "AND toxval_numeric>0";
//	}
	
	static String getToxValFilter() {
		return "WHERE\r\n"+ getUnitsFilter()+ " AND "+ getToxvalTypeFilter()+"\r\n"
		+ "AND toxval_numeric>0;";
	}

	
	static String getUnitsFilter() {
		return "toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')\r\n";
	}
	
	static String getToxvalTypeFilter() {
		return "(toxval_type like '%cancer slope factor%' or toxval_type like '%cancer unit risk%' or\r\n"
				+ "toxval_type like '%LC50%' or toxval_type like '%LD50%' or\r\n"
				+ "toxval_type like '%LOEC%' or toxval_type like '%NOEC%' or\r\n" //Chronic tox from ECOTOX
				+ "toxval_type like '%NOAEL%' or toxval_type like '%LOAEL%')";

	}
	
	
	
	/**
	 * Retrieving from mysql version
	 * 
	 * @return
	 */
	public static String createSQLQuery_toxval_v96() {

		//there is one to relationship between record_source and toxval tables thankfully
		
		String SQL="SELECT t.dtxsid,t.toxval_id,t.source,t.subsource,t.toxval_type,t.toxval_type_original,t.toxval_subtype,t.toxval_subtype_original,\r\n"
				+ "       t.toxval_numeric_qualifier,t.toxval_numeric_qualifier_original,t.toxval_numeric,t.toxval_numeric_original,\r\n"
				+ "       t.toxval_units,t.toxval_units_original,t.risk_assessment_class,\r\n"
				+ "       t.study_type,t.study_type_original,t.study_duration_class,t.study_duration_class_original,t.study_duration_value,\r\n"
				+ "       t.study_duration_value_original,t.study_duration_units,t.study_duration_units_original,t.human_eco,\r\n"
				+ "       t.strain,t.strain_original,t.sex,t.sex_original,t.generation,\r\n"
				+ "       s.species_id,t.species_original,\r\n"
				+ "       s.latin_name as species_scientific,s.common_name as species_common,s.ecotox_group as species_supercategory,\r\n"
				+ "       t.lifestage,t.exposure_route,t.exposure_route_original,t.exposure_method,t.exposure_method_original,\r\n"
				+ "       t.exposure_form,t.exposure_form_original,t.media,t.media_original,\r\n"
				+ "       t.toxicological_effect as critical_effect,\r\n"
				+ "       t.year,t.priority_id,\r\n"
				+ "       t.source_source_id,t.details_text,t.toxval_uuid,t.toxval_hash,t.datestamp\r\n"
				+ "FROM toxval t\r\n"
				+ "LEFT JOIN species s on t.species_id= s.species_id\r\n"
				+ getToxValFilterV96()+";";
		//		System.out.println("\n"+SQL);

		return SQL;


	}
//	
//	/**
//	 * Assumes everything is in toxval table in sqlite copy
//	 *  
//	 * @param source
//	 * @return
//	 */
//	public String createSQLQuery_toxval_v96_by_source_sqlite(String source) {
//
//		//there is one to relationship between record_source and toxval tables thankfully
//		
//		String SQL="SELECT t.dtxsid,t.toxval_id,t.source,t.subsource,t.toxval_type,t.toxval_type_original,t.toxval_subtype,t.toxval_subtype_original,t.toxval_type_supercategory,\r\n"
//				+ "       t.toxval_numeric_qualifier,t.toxval_numeric_qualifier_original,t.toxval_numeric,t.toxval_numeric_original,\r\n"
//				+ "       t.toxval_units,t.toxval_units_original,t.risk_assessment_class,\r\n"
//				+ "       t.study_type,t.study_type_original,t.study_duration_class,t.study_duration_class_original,t.study_duration_value,\r\n"
//				+ "       t.study_duration_value_original,t.study_duration_units,t.study_duration_units_original,t.human_eco,\r\n"
//				+ "       t.strain,t.strain_original,t.sex,t.sex_original,t.generation,\r\n"
//				+ "       t.species_id,t.species_original,\r\n"
//				+ "       t.species_scientific,t.species_common,t.species_supercategory,\r\n"
//				+ "       t.lifestage,t.exposure_route,t.exposure_route_original,t.exposure_method,t.exposure_method_original,\r\n"
//				+ "       t.exposure_form,t.exposure_form_original,t.media,t.media_original,\r\n"
//				+ "       t.critical_effect,\r\n"
//				+ "       t.year,t.priority_id,\r\n"
//				+ "       t.source_source_id,t.details_text,t.toxval_uuid,t.toxval_hash,t.datestamp\r\n"
////				+ "       rs.long_ref,rs.title,rs.author,rs.journal,rs.volume,rs.issue,rs.url,rs.document_name,rs.record_source_type,rs.record_source_hash\r\n"
//				+ "FROM toxval t\r\n"
////				+ "LEFT JOIN species s on t.species_id= s.species_id\r\n"
////				+ "JOIN toxval_type_dictionary ttd on t.toxval_type=ttd.toxval_type\r\n"
////				+ "JOIN record_source rs ON t.toxval_id=rs.toxval_id\r\n"
//				+ getToxValFilterV96()
//				+ "AND t.source='"+source+"';";
//		//		System.out.println("\n"+SQL);
//
//		return SQL;
//
//
//	}
	
//	/**
//	 * Sqlite version has everything in toxval table
//	 * @return
//	 */
//	public String createSQLQuery_toxval_v96_sqlite() {
//
//		//there is one to relationship between record_source and toxval tables thankfully
//		
//		String SQL="SELECT t.dtxsid,t.toxval_id,t.source,t.subsource,t.toxval_type,t.toxval_type_original,t.toxval_subtype,t.toxval_subtype_original,t.toxval_type_supercategory,\r\n"
//				+ "       t.toxval_numeric_qualifier,t.toxval_numeric_qualifier_original,t.toxval_numeric,t.toxval_numeric_original,\r\n"
//				+ "       t.toxval_units,t.toxval_units_original,t.risk_assessment_class,\r\n"
//				+ "       t.study_type,t.study_type_original,t.study_duration_class,t.study_duration_class_original,t.study_duration_value,\r\n"
//				+ "       t.study_duration_value_original,t.study_duration_units,t.study_duration_units_original,t.human_eco,\r\n"
//				+ "       t.strain,t.strain_original,t.sex,t.sex_original,t.generation,\r\n"
//				+ "       t.species_id,t.species_original,\r\n"
//				+ "       t.species_scientific,t.species_common,t.species_supercategory,\r\n"
//				+ "       t.lifestage,t.exposure_route,t.exposure_route_original,t.exposure_method,t.exposure_method_original,\r\n"
//				+ "       t.exposure_form,t.exposure_form_original,t.media,t.media_original,\r\n"
//				+ "       t.critical_effect,\r\n"
//				+ "       t.year,t.priority_id,\r\n"
//				+ "       t.source_source_id,t.details_text,t.toxval_uuid,t.toxval_hash,t.datestamp\r\n"
////				+ "       rs.long_ref,rs.title,rs.author,rs.journal,rs.volume,rs.issue,rs.url,rs.document_name,rs.record_source_type,rs.record_source_hash\r\n"
//				+ "FROM toxval t\r\n"
////				+ "LEFT JOIN species s on t.species_id= s.species_id\r\n"
////				+ "JOIN toxval_type_dictionary ttd on t.toxval_type=ttd.toxval_type\r\n"
////				+ "JOIN record_source rs ON t.toxval_id=rs.toxval_id\r\n"
//				+ "WHERE\r\n"
//				+ "t.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')\r\n"
//				+ getToxValTypeSuperCategoryV96().replace("ttd.", "t.")+"\r\n"
//				+ "AND t.toxval_numeric>0;";
//
//		return SQL;
//
//
//	}
	
	/**
	 * Sqlite version has everything in one toxval table and is prefiltered by :
	 * - toval_units
	 * - toxval_type_supercategory
	 * - toxval_numeric 
	 * 
	 * @return
	 */
	public String createSQLQuery_toxval_complete() {
		return "SELECT * from toxval_complete;";
	}



	
	
	/**
	 * Get all data from toxval table for given source (includes reference info from record_source table since they are 1 to 1 now
	 * 
	 * NOTE: it wasn't necessary to create extra database columns for columns like species_common because can use sql to label it to match the java variable name
	 * In toxval v94 there is one to one relationship between toxval table and record_source table so dont need separate query to get record_source info
	 * 
	 * @param dtxsid
	 * @return
	 */
	private String createSQLQuery_toxval_v94_by_source(String source) {

		String SQL="SELECT b.dtxsid, b.toxval_id, b.source,b.subsource,b.toxval_type,b.toxval_type_original,b.toxval_subtype,b.toxval_subtype_original,e.toxval_type_supercategory,\r\n" + 
				"b.toxval_numeric_qualifier,b.toxval_numeric_qualifier_original,b.toxval_numeric,b.toxval_numeric_original,\r\n" + 
				"b.toxval_numeric_converted, b.toxval_units,b.toxval_units_original,b.toxval_units_converted, b.risk_assessment_class,\r\n" + 
				"b.study_type,b.study_type_original,b.study_duration_class,b.study_duration_class_original, b.study_duration_value,\r\n" + 
				"b.study_duration_value_original,b.study_duration_units,b.study_duration_units_original,b.human_eco,\r\n" + 
				"b.strain,b.strain_original,b.sex,b.sex_original,b.generation,\r\n" + 
				"d.species_id,b.species_original,\r\n" + 
				//				"d.latin_name as species_scientific, d.common_name as species_common,d.ecotox_group as species_supercategory,\r\n" + 
				"d.latin_name as species_scientific, d.species_common, d.species_supercategory,\r\n" + 
				"b.lifestage,b.exposure_route,b.exposure_route_original,b.exposure_method,b.exposure_method_original,\r\n" + 
				//				"b.exposure_form,b.exposure_form_original, b.media,b.media_original,b.critical_effect,b.year,b.quality_id,b.priority_id,\r\n" + 
				"b.exposure_form,b.exposure_form_original, b.media,b.media_original,b.critical_effect,b.year,b.priority_id,\r\n" + 
				"b.source_source_id,b.details_text,b.toxval_uuid,b.toxval_hash,b.datestamp,\r\n" + 
				"c.long_ref, c.title, c.author, c.journal, c.volume, c.issue, c.url, c.document_name, c.record_source_type, c.record_source_hash\r\n" + 
				"\r\n" + 

				"FROM toxval b\r\n" + 
				"LEFT JOIN species d on b.species_id=d.species_id\r\n" + 
				"JOIN toxval_type_dictionary e on b.toxval_type=e.toxval_type\r\n" + 
				"JOIN record_source c ON b.toxval_id=c.toxval_id\r\n" +				
				getToxValFilterV94()+
				"AND b.source='"+source+"';";		

		//		System.out.println("\n"+SQL);

		return SQL;

	}

	/**
	 * Get all data from toxval table for a given source
	 * 
	 * @param source
	 * @return
	 */
	private String createSQLQuery_toxval_v8_by_source(String source) {

		//	String SQL="SELECT\r\na.dtxsid, a.casrn,a.name,\r\n" + 
		String SQL="SELECT b.dtxsid, b.toxval_id, b.source,b.subsource,b.toxval_type,b.toxval_type_original,b.toxval_subtype,b.toxval_subtype_original,e.toxval_type_supercategory,\r\n" + 
				"b.toxval_numeric_qualifier,b.toxval_numeric_qualifier_original,b.toxval_numeric,b.toxval_numeric_original,\r\n" + 
				"b.toxval_numeric_converted, b.toxval_units,b.toxval_units_original,b.toxval_units_converted, b.risk_assessment_class,\r\n" + 
				"b.study_type,b.study_type_original,b.study_duration_class,b.study_duration_class_original, b.study_duration_value,\r\n" + 
				"b.study_duration_value_original,b.study_duration_units,b.study_duration_units_original,b.human_eco,\r\n" + 
				"b.strain,b.strain_original,b.sex,b.sex_original,b.generation,\r\n" + 
				"d.species_id,b.species_original,\r\n" + 
				"d.species_common,d.species_supercategory,d.habitat,\r\n" + 
				"b.lifestage,b.exposure_route,b.exposure_route_original,b.exposure_method,b.exposure_method_original,\r\n" + 
				"b.exposure_form,b.exposure_form_original, b.media,b.media_original,b.critical_effect,b.year,b.quality_id,b.priority_id,\r\n" + 
				"b.source_source_id,b.details_text,b.toxval_uuid,b.toxval_hash,b.datestamp\r\n" + 
//				"c.long_ref, c.title, c.author, c.journal, c.volume, c.issue, c.url, c.document_name, c.record_source_type, c.record_source_hash\r\n" + 
				"\r\n" + 

			"FROM toxval b\r\n" + 
			"LEFT JOIN species d on b.species_id=d.species_id\r\n" + 
			"JOIN toxval_type_dictionary e on b.toxval_type=e.toxval_type\r\n" + 
//			"JOIN record_source c ON b.toxval_id=c.toxval_id\r\n" +				
			"WHERE\r\n"+
			"b.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')\r\n" + 				
			"AND e.toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')\r\n" + 
			"AND b.toxval_numeric>0\r\n" + 									
			"AND b.source='"+source+"';";		

//			System.out.println("\n"+SQL);

		return SQL;


	}
	
	public String createSQLQuery_toxval_v8() {

		//	String SQL="SELECT\r\na.dtxsid, a.casrn,a.name,\r\n" + 
		String SQL="SELECT b.dtxsid, b.toxval_id, b.source,b.subsource,b.toxval_type,b.toxval_type_original,b.toxval_subtype,b.toxval_subtype_original,e.toxval_type_supercategory,\r\n" + 
				"b.toxval_numeric_qualifier,b.toxval_numeric_qualifier_original,b.toxval_numeric,b.toxval_numeric_original,\r\n" + 
				"b.toxval_numeric_converted, b.toxval_units,b.toxval_units_original,b.toxval_units_converted, b.risk_assessment_class,\r\n" + 
				"b.study_type,b.study_type_original,b.study_duration_class,b.study_duration_class_original, b.study_duration_value,\r\n" + 
				"b.study_duration_value_original,b.study_duration_units,b.study_duration_units_original,b.human_eco,\r\n" + 
				"b.strain,b.strain_original,b.sex,b.sex_original,b.generation,\r\n" + 
				"d.species_id,b.species_original,\r\n" + 
				"d.species_common,d.species_supercategory,d.habitat,\r\n" + 
				"b.lifestage,b.exposure_route,b.exposure_route_original,b.exposure_method,b.exposure_method_original,\r\n" + 
				"b.exposure_form,b.exposure_form_original, b.media,b.media_original,b.critical_effect,b.year,b.quality_id,b.priority_id,\r\n" + 
				"b.source_source_id,b.details_text,b.toxval_uuid,b.toxval_hash,b.datestamp\r\n" + 
//				"c.long_ref, c.title, c.author, c.journal, c.volume, c.issue, c.url, c.document_name, c.record_source_type, c.record_source_hash\r\n" + 
				"\r\n" + 

			"FROM toxval b\r\n" + 
			"LEFT JOIN species d on b.species_id=d.species_id\r\n" + 
			"JOIN toxval_type_dictionary e on b.toxval_type=e.toxval_type\r\n" + 
//			"JOIN record_source c ON b.toxval_id=c.toxval_id\r\n" +				
			"WHERE\r\n"+
			"b.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')\r\n" + 				
			"AND e.toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')\r\n" + 
			"AND b.toxval_numeric>0;";  									
//			System.out.println("\n"+SQL);

		return SQL;


	}



	/**
	 * Getting the reference info
	 * 
	 * @param CAS
	 * @return
	 */
	private String createReferenceQuery(String CAS) {

		String SQL="SELECT\r\na.dtxsid, a.casrn,a.name,\r\n" + 
				"b.toxval_id, b.toxval_units, b.toxval_numeric, e.toxval_type_supercategory,"+
				"c.long_ref, c.title, c.author, c.journal, c.volume, c.issue, c.url, c.document_name, c.record_source_type, c.record_source_hash\r\n" + 
				"\r\n" + 				
				"FROM toxval b\r\n" + 
				"INNER JOIN chemical a on a.dtxsid=b.dtxsid\r\n" + 
				"LEFT JOIN record_source c ON b.toxval_id=c.toxval_id\r\n" +	
				"INNER JOIN toxval_type_dictionary e on b.toxval_type=e.toxval_type\r\n" + 
				"WHERE\r\n"+
				"b.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')\r\n" + 				
				"AND e.toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')\r\n" + 
				"AND b.toxval_numeric>0\r\n" + 									
				"AND a.casrn=\""+CAS+"\";";		

		//		System.out.println("\n"+SQL);

		return SQL;


	}


	/**
	 * Getting the reference info
	 * 
	 * @param CAS
	 * @return
	 */
	private String createReferenceQueryByToxval_id(String toxval_id) {

		String SQL="SELECT b.dtxsid, b.toxval_id, b.toxval_units, b.toxval_numeric, e.toxval_type_supercategory,"+
				"c.long_ref, c.title, c.author, c.journal, c.volume, c.issue, c.url, c.document_name, c.record_source_type, c.record_source_hash\r\n" + 
				"\r\n" + 				
				"FROM toxval b\r\n" + 
				"LEFT JOIN record_source c ON b.toxval_id=c.toxval_id\r\n" +	
				"INNER JOIN toxval_type_dictionary e on b.toxval_type=e.toxval_type\r\n" + 
				"WHERE\r\n"+
				"b.toxval_id="+toxval_id+";"; 		


//				System.out.println("\n"+SQL);

		return SQL;


	}


	@Deprecated
	private String createSQLQueryByCAS(String CAS,String table,String [] varlist) {

		String SQL="SELECT ";    	    	    	    	
		for(String field : varlist) {    		

			//Following comes from chemical_list table:
			if(field.contentEquals("casrn")) continue;
			if(field.contentEquals("name")) continue;

			SQL+=table+"."+field+", ";    		
		}

		SQL+="chemical.casrn, chemical.name\n";   	    	
		SQL+="FROM chemical\n";    	
		SQL+="JOIN "+table+" ON "+table+".dtxsid = chemical.dtxsid\n";

		SQL+="WHERE chemical.casrn=\""+CAS+"\" AND "+table+".dtxsid is not null;";		
		//    	SQL+="WHERE chemical.casrn=\""+CAS+"\";";

		//    	System.out.println("\n"+SQL);

		return SQL;


	}

	/**
	 * Get all data from a table in toxval
	 * 
	 * @param table
	 * @param varlist
	 * @return
	 */
	public static String createSQLQueryByTable(String table,String [] varlist) {

		String SQL="SELECT ";    	    	    	    	

		//		for(String field : varlist) {
		for (int i=0;i<varlist.length;i++) {

			String field=varlist[i];

			//Following comes from chemical_list table:
			if(field.contentEquals("casrn")) continue;
			if(field.contentEquals("name")) continue;
			SQL+=table+"."+field;

			if(i<varlist.length-1) SQL+=", ";

		}

		//		SQL+="chemical.casrn, chemical.name\n";   	    	
		//		SQL+="FROM chemical\n";    	
		//		SQL+="JOIN "+table+" ON "+table+".dtxsid = chemical.dtxsid\n";

		SQL+="\nFrom "+table;
		SQL+="\nWHERE "+table+".dtxsid is not null;";		
		return SQL;


	}

	
	public static String createSQLQueryByTable2(String table,String [] varlist) {

		String SQL="SELECT ";    	    	    	    	

		//		for(String field : varlist) {
		for (int i=0;i<varlist.length;i++) {
			String field=varlist[i];
			//Following comes from chemical_list table:
			SQL+=table+"."+field;
			if(i<varlist.length-1) SQL+=", ";
		}

		SQL+="\nFrom "+table+";";
		return SQL;


	}
	
	private String createSQLQueryByDTXSID(String dtxsid, String table,String [] varlist) {

		String SQL="SELECT ";    	    	    	    	

		//		for(String field : varlist) {
		for (int i=0;i<varlist.length;i++) {
			String field=varlist[i];
			if(field.contentEquals("casrn")) continue;
			if(field.contentEquals("name")) continue;
			SQL+=table+"."+field;
			if(i<varlist.length-1) SQL+=", ";
		}

		SQL+="\nFrom "+table;
		SQL+="\nWHERE "+table+".dtxsid ='"+dtxsid+"';";		
		return SQL;
	}


	@Deprecated
	private String createSQLQueryByCAS(String CAS,String table,String [] varlist,String [] fieldNames,String [] fieldValues) {

		String SQL="SELECT ";    	    	    	    	
		for(String field : varlist) {    		

			//Following comes from chemical_list table:
			if(field.contentEquals("casrn")) continue;
			if(field.contentEquals("name")) continue;

			SQL+=table+"."+field+", ";    		
		}

		SQL+="chemical.casrn, chemical.name\n";   	    	
		SQL+="FROM chemical\n";    	
		SQL+="JOIN "+table+" ON "+table+".dtxsid = chemical.dtxsid\n";

		SQL+="WHERE chemical.casrn=\""+CAS+"\" AND ";

		for (int i=0;i<fieldNames.length;i++) {
			SQL+=fieldNames[i]+" = \""+fieldValues[i]+"\"";
			if (i<fieldNames.length-1) SQL+=" AND ";
		}

		SQL+=";";

		//    	System.out.println("\n"+SQL);

		return SQL;


	}
	
	/**
	 * Used to get records from model table by dtxsid
	 * 
	 * @param dtxsid
	 * @param table
	 * @param varlist
	 * @param fieldNames
	 * @param fieldValues
	 * @return
	 */
	private String createSQLQueryByDTXSID(String dtxsid,String table,String [] varlist,String [] fieldNames,String [] fieldValues) {

		String SQL="SELECT ";    	    	    	    	
		for (int i=0;i<varlist.length;i++) {
			String field=varlist[i];
			if(field.contentEquals("casrn")) continue;
			if(field.contentEquals("name")) continue;
			SQL+=table+"."+field;
			if(i<varlist.length-1) SQL+=", ";
		}

		SQL+="\nFrom "+table;
		SQL+="\nWHERE dtxsid=\""+dtxsid+"\" AND ";

		for (int i=0;i<fieldNames.length;i++) {
			SQL+=fieldNames[i]+" = \""+fieldValues[i]+"\"";
			if (i<fieldNames.length-1) SQL+=" AND ";
		}

		SQL+=";";
//		System.out.println("\n"+SQL);
		return SQL;
	}



	/**
	 * Refactored so that it only pulled the reference info for the toxval records that were actually used one at a time
	 * 
	 * @param chemical
	 */
	void getDataFromTable_toxval(Chemical chemical,String versionToxVal,Statement statToxVal) {

		try {

			//			String sql=createSQLQuery_toxval(chemical.CAS);	

			String sql=null;

			if (chemical.dtxsid==null) return;

			if(versionToxVal.equals(ParseToxValDB.v8)) {
				sql=createSQLQuery_toxval_v8_by_DTXSID(chemical.dtxsid);
			} else if (versionToxVal.equals(ParseToxValDB.v94)) {
				sql=createSQLQuery_toxval_v94_by_DTXSID(chemical.dtxsid);
			} else {
				System.out.println("Bad version:"+versionToxVal);
				return;
			}

			//			System.out.println(sql);

			ResultSet rs=MySQL_DB.getRecords(statToxVal, sql);

			Vector<RecordToxVal>records=new Vector();

			while (rs.next()) {						 
				RecordToxVal r=new RecordToxVal();							
				createRecord(rs,r);
				records.add(r);						
			}

			//***************************************************************************************************************************************

			for (int i=0;i<records.size();i++) {
				RecordToxVal ri=records.get(i);

				if(versionToxVal.equals(ParseToxValDB.v8)) {
					Vector<RecordToxVal>recordsRef=new Vector<>();

					//create second query to get reference info: (there is potentially more than 1 reference record for each toxval_id)
					String sqlRef=createReferenceQueryByToxval_id(ri.toxval_id);
					ResultSet rsRef=MySQL_DB.getRecords(statToxVal, sqlRef);

					while (rsRef.next()) {
						RecordToxVal recordRef=new RecordToxVal();
						createRecord(rsRef,recordRef);		
						recordsRef.add(recordRef);
					}
					addReferenceInfo(recordsRef, ri);	
				} else if (versionToxVal.equals(ParseToxValDB.v94)) {
					editReferenceInfo(ri);
					//this needs to be fixed so that it only uses first record of each unique toxvalid 
					//Create Hashtable<toxval_id, List<RecordToxVal>>
				}
				
				//				if (recordsRef.size()>1)
				//					System.out.println(ri.toxval_id+"\t"+recordsRef.size());

				//Create score records:
				ParseToxVal.createScoreRecord(chemical, ri,versionToxVal);

			}

			//			System.out.println("Records in toxval table for "+chemical.CAS+" = "+records.size());
			//			System.out.println("# refs="+numRefs);

			//			System.out.println("CAS="+chemical.CAS);
			//			System.out.println("records.size()="+records.size());
			//			System.out.println("Records in toxval table for "+chemical.CAS+" = "+count);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

//	Hashtable<String, Chemical> getDataFromTable_toxval(String source,Statement statToxVal,String toxvalVersion) {
//
//		try {
//
//			Hashtable<String, Chemical>htChemicals=new Hashtable<>();
//			//			String sql=createSQLQuery_toxval(chemical.CAS);
//			String sql=null;
//
//			if(toxvalVersion.equals(v94)) {
//				sql=createSQLQuery_toxval_v94_by_source(source);
//			} else if(toxvalVersion.equals(v96)) {
//				sql=createSQLQuery_toxval_v96_by_source_sqlite(source);
//			} else if (toxvalVersion.equals(v8)) {
//				sql=createSQLQuery_toxval_v8_by_source(source);
//			}
//
////			System.out.println(sql);
//
//			ResultSet rs=MySQL_DB.getRecords(statToxVal, sql);
//
//			Vector<RecordToxVal>records=new Vector();
//			
//			while (rs.next()) {						 
//				RecordToxVal ri=new RecordToxVal();							
//				createRecord(rs,ri);
//				records.add(ri);
//				Chemical chemical=null;
//
//				if(ri.dtxsid==null) {
//					continue;
//				}
//				if(htChemicals.get(ri.dtxsid)!=null) {
//					chemical=htChemicals.get(ri.dtxsid);
//				} else {
//					chemical=new Chemical();
//					htChemicals.put(ri.dtxsid, chemical);
//				}
//				ParseToxVal.createScoreRecord(chemical, ri,toxvalVersion);
//			}
//
////			Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
//			//***************************************************************************************************************************************
//
//			getCounts(source, htChemicals);
//
//			
//
//			return htChemicals;
//
//		} catch (Exception ex) {			
//			ex.printStackTrace();
//			return null;
//		}
//
//	}
	
	
	/**
	 * Look at data for all sources with one big query
	 * 
	 * @param statToxVal
	 * @param toxvalVersion
	 * @param sources
	 * @return
	 */
	Hashtable<String, Chemical> getDataFromTable_toxval(Statement statToxVal,String toxvalVersion,List<String>sources) {

		try {

			Hashtable<String, Chemical>htChemicals=new Hashtable<>();
			//			String sql=createSQLQuery_toxval(chemical.CAS);
			String sql=null;

			if(toxvalVersion.equals(v94)) {
				sql=createSQLQuery_toxval_complete();
			} else if(toxvalVersion.equals(v96)) {
				sql=createSQLQuery_toxval_complete();
			} else if (toxvalVersion.equals(v8)) {
				sql=createSQLQuery_toxval_v8();
			}

//			System.out.println(sql);

			ResultSet rs=MySQL_DB.getRecords(statToxVal, sql);

//			Vector<RecordToxVal>records=new Vector();
			
			while (rs.next()) {						 
				RecordToxVal ri=new RecordToxVal();							
				createRecord(rs,ri);
//				records.add(ri);
				Chemical chemical=null;

				if(ri.dtxsid==null) {
					continue;
				}
				if(htChemicals.containsKey(ri.dtxsid)) {
					chemical=htChemicals.get(ri.dtxsid);
				} else {
					chemical=new Chemical();
					htChemicals.put(ri.dtxsid, chemical);
				}
				ParseToxVal.createScoreRecord(chemical, ri,toxvalVersion);
			}

//			Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
			//***************************************************************************************************************************************

			for (String source:sources) {
				getCounts2(source, htChemicals);	
			}
			
			Chemical chemical=new Chemical();
			
			int total=0;
			System.out.println("\nHazard name\tcount");
			for (Score score:chemical.scores) {
				total+=getCounts3(score.hazard_name, htChemicals);	
			}
			System.out.println("Total\t"+total);
			System.out.print("\n");
			
			return htChemicals;

		} catch (Exception ex) {			
			ex.printStackTrace();
			return null;
		}

	}



	private void getCounts(String source, Hashtable<String, Chemical> htChemicals) {
		int recordCount=0;
		
		Hashtable<String,List<ScoreRecord>>htRecords=new Hashtable<>();
		
		for (String dtxsid:htChemicals.keySet()) {
			Chemical chemical=htChemicals.get(dtxsid);

			int recordCountChemical=0;

			for (int i=0;i<chemical.getScores().size();i++) {
				Score score=chemical.getScores().get(i);
				recordCount+=score.records.size();
				recordCountChemical+=score.records.size();
				
				if (score.records.size()==0) {
					chemical.getScores().remove(i--);
				} else {
					//						System.out.println(chemical.getDtxsid()+"\t"+score.hazard_name);
					
					if(htRecords.get(score.hazard_name)==null) {
						List<ScoreRecord>recs=new ArrayList<>();
						recs.addAll(score.records);
						htRecords.put(score.hazard_name, recs);
					} else {
						List<ScoreRecord>recs=htRecords.get(score.hazard_name);
						recs.addAll(score.records);
					}
					
				}
			}

			//	if (recordCountChemical>0)
			//		System.out.println(gson.toJson(chemical));
		}
		
		System.out.println(source+"\t"+recordCount);
		
		for (String hazardName:htRecords.keySet()) {
			List<ScoreRecord>recs=htRecords.get(hazardName);
			System.out.println("\t"+source+"\t"+hazardName+"\t"+recs.size());	
		}
	}
	
	/**
	 * In this version data from all sources is included in the hashtable so need to extract data just for that sourceOriginal
	 * 
	 * @param sourceOriginal
	 * @param htChemicals
	 */
	private void getCounts2(String sourceOriginal, Hashtable<String, Chemical> htChemicals) {
		int recordCount=0;
		
		Hashtable<String,List<ScoreRecord>>htRecords=new Hashtable<>();
		
		for (String dtxsid:htChemicals.keySet()) {
			Chemical chemical=htChemicals.get(dtxsid);
			
//			System.out.println(gson.toJson(chemical));


			for (int i=0;i<chemical.getScores().size();i++) {
				Score score=chemical.getScores().get(i);
				
				
				if(!htRecords.containsKey(score.hazard_name)) {
					List<ScoreRecord>recs=new ArrayList<>();
					
					for(ScoreRecord rec:score.records) {
						
						if(rec.sourceOriginal.equals(sourceOriginal)) {
							recs.add(rec);
							recordCount++;
						}
					}

					htRecords.put(score.hazard_name, recs);
				} else {
					List<ScoreRecord>recs=htRecords.get(score.hazard_name);
					
					for(ScoreRecord rec:score.records) {
						
						if(rec.sourceOriginal.equals(sourceOriginal)) {
							recordCount++;
							recs.add(rec);		
						}
					}

				}

			}
			

			//	if (recordCountChemical>0)
			//		System.out.println(gson.toJson(chemical));
		}
		
		if(recordCount==0) return;
		
		
		for (String hazardName:htRecords.keySet()) {
			List<ScoreRecord>recs=htRecords.get(hazardName);
			
			if(recs.size()>0)			
				System.out.println(sourceOriginal+"\t"+hazardName+"\t"+recs.size());	
		}
		
		
		System.out.println(sourceOriginal+"\tTotal\t"+recordCount+"\n");

	}
	
	
	private int getCounts3(String hazardName, Hashtable<String, Chemical> htChemicals) {

		List<ScoreRecord>hazardRecords=new ArrayList<>();
		for (String dtxsid:htChemicals.keySet()) {
			Chemical chemical=htChemicals.get(dtxsid);
			Score score=chemical.getScore(hazardName);
			hazardRecords.addAll(score.records);
		}
		
//		System.out.println(sourceOriginal+"\t"+recordCount);
		
		if(hazardRecords.size()>0)
			System.out.println(hazardName+"\t"+hazardRecords.size());
		
		return hazardRecords.size();
		
	}
	
	
	private void getCountsBySource(String table,Hashtable<String, Chemical> htChemicals) {
		int recordCount=0;
		
		Hashtable<String,List<ScoreRecord>>htRecords=new Hashtable<>();
		
		for (String dtxsid:htChemicals.keySet()) {
			Chemical chemical=htChemicals.get(dtxsid);

			int recordCountChemical=0;

			for (int i=0;i<chemical.getScores().size();i++) {
				Score score=chemical.getScores().get(i);
				recordCount+=score.records.size();
				recordCountChemical+=score.records.size();
				
				if (score.records.size()==0) {
					chemical.getScores().remove(i--);
				} else {
					//						System.out.println(chemical.getDtxsid()+"\t"+score.hazard_name);
					
					
					for(ScoreRecord sr:score.records) {
						if(htRecords.get(sr.sourceOriginal)==null) {
							List<ScoreRecord>recs=new ArrayList<>();
							recs.add(sr);
							htRecords.put(sr.sourceOriginal, recs);
							
						} else {
							List<ScoreRecord>recs=htRecords.get(sr.sourceOriginal);
							recs.add(sr);
						}
						
					}
					
				}
			}

			//	if (recordCountChemical>0)
			//		System.out.println(gson.toJson(chemical));
		}
		
		System.out.println(table+"\t"+recordCount);
		
		for (String source:htRecords.keySet()) {
			List<ScoreRecord>recs=htRecords.get(source);
			System.out.println("\t"+source+"\t"+recs.size());	
		}
	}
	
	List<String>getNA_refs() {
		return Arrays.asList("-","- - - -","- - - NA","- Unnamed - NA","- Unnamed - -");
	}
	
	
	/**
	 * Edits reference info for long_ref and url
	 * 
	 * @param recordsRef
	 * @param ri
	 */

	private void editReferenceInfo(RecordToxVal ri) {

		if (getNA_refs().contains(ri.long_ref)) {//dont store NA references with usable info
			ri.long_ref="";
		}	

		if (ri.long_ref.contains("doi:")) {				
			String DOI=ri.long_ref.substring(ri.long_ref.indexOf("doi: ")+5,ri.long_ref.length());
			
			if (ri.url.isEmpty())  ri.url=DOI;
			else ri.url+="<br>"+DOI;		
		}									

		ri.long_ref=ri.long_ref.trim();
		ri.url=ri.url.trim();

		if (ri.long_ref.indexOf("<br>")==0) {
			ri.long_ref=ri.long_ref.substring(5,ri.long_ref.length());
		}

		if (ri.url.indexOf("<br>")==0) {
			ri.url=ri.url.substring(5,ri.url.length());
		}

	}

	void getDataFromTable_toxval_old(Chemical chemical) {

		try {


			//			String sql=createSQLQuery_toxval(chemical.CAS);				
			String sql=createSQLQuery_toxval_v8_by_CAS(chemical.CAS);

			ResultSet rs=MySQL_DB.getRecords(statToxValv8, sql);

			Vector<RecordToxVal>records=new Vector();

			while (rs.next()) {						 
				RecordToxVal r=new RecordToxVal();							
				createRecord(rs,r);
				records.add(r);						
			}

			//***************************************************************************************************************************************
			//create second query to get reference info: (there is potentially more than 1 reference record for each toxval_id)
			String sqlRef=createReferenceQuery(chemical.CAS);
			ResultSet rsRef=MySQL_DB.getRecords(statToxValv8, sqlRef);

			//			logger.warn("here3:");

			Hashtable<String,Vector<RecordToxVal>>recordsRef=new Hashtable<>();

			int numRefs=0;


			//			if (chemical.CAS.equals("10108-64-2")) {
			//				logger.warn(sqlRef+"\n");
			//			}

			while (rsRef.next()) {
				numRefs++;

				//				if (numRefs==2000 && TESTApplication.forMDH) break;//temporary bug fix to stop it from hanging for 10108-64-2 in MDL list

				RecordToxVal r=new RecordToxVal();							
				createRecord(rsRef,r);		

				//store reference info by toxval_id for easy retrieval when looping through tox data
				if (recordsRef.get(r.toxval_id)==null) {										
					Vector<RecordToxVal>recs=new Vector<>();
					recs.add(r);
					recordsRef.put(r.toxval_id,recs);								
				} else {
					Vector<RecordToxVal>recs=recordsRef.get(r.toxval_id);
					recs.add(r);
				}

			}

			//			logger.warn("here4:");

			//**************************************************************************************************************
			//Add the reference info to the records:

			//			GsonBuilder builder = new GsonBuilder();
			//			builder.setPrettyPrinting();
			//			Gson gson = builder.create();


			for (int i=0;i<records.size();i++) {
				RecordToxVal ri=records.get(i);
				addReferenceInfo(recordsRef.get(ri.toxval_id), ri);				

				//Create score records:
				ParseToxVal.createScoreRecord(chemical, ri,ParseToxValDB.v8);

				//				String json=gson.toJson(ri);				
				//				System.out.println(json);

				//				if (recs.size()>1) {
				//					System.out.println("**"+r0.long_ref+"**");
				//					System.out.println("@@"+r0.url+"@@\n");
				//				}



			}

			//			System.out.println("Records in toxval table for "+chemical.CAS+" = "+records.size());
			//			System.out.println("# refs="+numRefs);



			//			System.out.println("CAS="+chemical.CAS);
			//			System.out.println("records.size()="+records.size());
			//			System.out.println("Records in toxval table for "+chemical.CAS+" = "+count);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}


	/**
	 * Flattens reference info and stores inside ri record
	 * 
	 * @param recordsRef
	 * @param ri
	 */
	private void addReferenceInfo(Vector<RecordToxVal>recsRef, RecordToxVal ri) {

		//				System.out.println("0"+"\t"+r0.toxval_id);
		//		System.out.println("toxvalid="+ri.toxval_id);

		ri.long_ref="";
		ri.url="";


		for (int j=0;j<recsRef.size();j++) {								
			RecordToxVal recRef=recsRef.get(j);

			if (!getNA_refs().contains(recRef.long_ref)) {
				if (ri.long_ref.isEmpty()) ri.long_ref=recRef.long_ref; 
				else ri.long_ref+="<br>"+recRef.long_ref;
			}

			if (ri.url.isEmpty())  ri.url=recRef.url;
			else ri.url+="<br>"+recRef.url;

			if (recRef.long_ref.contains("doi:")) {				
				String DOI=recRef.long_ref.substring(recRef.long_ref.indexOf("doi: ")+5,recRef.long_ref.length());

				if (ri.url.isEmpty())  ri.url=DOI;
				else ri.url+="<br>"+DOI;		

				//				System.out.println(DOI);
			}									
		}

		ri.long_ref=ri.long_ref.trim();
		ri.url=ri.url.trim();

		if (ri.long_ref.indexOf("<br>")==0) {
			ri.long_ref=ri.long_ref.substring(5,ri.long_ref.length());
		}

		if (ri.url.indexOf("<br>")==0) {
			ri.url=ri.url.substring(5,ri.url.length());
		}
		//		System.out.println(ri.toxval_id+"\t"+ri.url);


	}

	void getDOI(RecordToxVal r) {

		if (r.long_ref.contains("doi:") && r.url.isEmpty()) {
			r.url=r.long_ref.substring(r.long_ref.indexOf("doi: ")+5,r.long_ref.length());
			//			System.out.println("new url="+r.url);
		}

	}

	void getDataFromTable_cancer_summary(Chemical chemical,Statement statToxVal) {

		try {

			String sql=createSQLQueryByDTXSID(chemical.dtxsid,"cancer_summary",RecordToxValCancer.varlist);				


			ResultSet rs=MySQL_DB.getRecords(statToxVal, sql);

			int count=0;

			Hashtable<String,String>dictCC=ParseToxValCancer.populateCancerCallToScoreValue();

			while (rs.next()) {						 
				RecordToxValCancer r=new RecordToxValCancer();			
				createRecord(rs, r);

				ParseToxValCancer.createScoreRecord(chemical, r, dictCC);
				//System.out.println(r.risk_assessment_class);
				count++;
			}

			//			System.out.println("Records in cancer_summary table for "+chemical.CAS+" = "+count);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}


	void getDataFromTable_genetox_summary(Chemical chemical,Statement statToxVal) {

		try {

			String sql=createSQLQueryByDTXSID(chemical.dtxsid,"genetox_summary",RecordToxValGenetox.varlist);				
			ResultSet rs=MySQL_DB.getRecords(statToxVal, sql);

			int count=0;

			Hashtable<String,String>dictCC=ParseToxValGenetox.populateGenetoxCallToScoreValue();


			while (rs.next()) {						 
				RecordToxValGenetox r=new RecordToxValGenetox();						
				createRecord(rs, r);						
				ParseToxValGenetox.createScoreRecord(chemical, r,dictCC);
				count++;
			}

			//			System.out.println("Records in genetox_summary table for "+chemical.CAS+" = "+count);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	void getDataFromTable_models(Chemical chemical,Statement statToxVal) {

		try {

			createRecordBCF_OPERA(chemical,statToxVal);
			createRecordBCF_EPISUITE(chemical,statToxVal);

			//Create record based on opera biodegradation prediction:
			//			createRecordPersistence_OPERA(chemical);
			//  Not including this model.

			//Create record based on episuite biodegradation prediction:
			createRecordPersistence_EPISUITE(chemical,statToxVal);		

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
	private Hashtable<String, Chemical> getDataFromTable_models(Hashtable<String, List<RecordToxValModels>> htRecordsModels) {

		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues();
		Gson gson = builder.create();
		
		Hashtable<String, Chemical>htChemicals=new Hashtable<>();


		for (String dtxsid:htRecordsModels.keySet()) {

			List<RecordToxValModels>recs=htRecordsModels.get(dtxsid);
			
			RecordToxValModels rBCF_OPERA=null;
			RecordToxValModels rBCF_OPERA_AD=null;
			RecordToxValModels rBiodeg_EPISUITE=null;
			RecordToxValModels rBCF_EPISUITE=null;
			
			for (RecordToxValModels rec:recs) {
				
				if(rec.model.equals("OPERA") && rec.metric.equals("BCF")) {
					rBCF_OPERA=rec;
				} else if (rec.model.equals("OPERA") && rec.metric.equals("BCF_AD")) {
					rBCF_OPERA_AD=rec;
				} else if (rec.model.equals("EpiSuite") && rec.metric.equals("Biodegradation Score")) {
					rBiodeg_EPISUITE=rec;
				} else if (rec.model.equals("EpiSuite") && rec.metric.equals("BCF")) {
					rBCF_EPISUITE=rec;
				} else {
					System.out.println(gson.toJson(rec));
				}
			}
			
			Chemical chemical=new Chemical();
			chemical.dtxsid=dtxsid;
			
			if (rBCF_OPERA!=null && rBCF_OPERA_AD!=null) {
				ParseToxValModels.createScoreRecordBCF_Opera(chemical, rBCF_OPERA,rBCF_OPERA_AD);
			}
			
			if(rBCF_EPISUITE!=null) {
				ParseToxValModels.createScoreRecordBCF_EPISUITE(chemical, rBCF_EPISUITE);
			}
			
			if(rBiodeg_EPISUITE!=null) {
				ParseToxValModels.createScoreRecordPersistence_EpiSuite(chemical, rBiodeg_EPISUITE);
			}
			
//			System.out.println(gson.toJson(recs));
			htChemicals.put(dtxsid, chemical);
		}
		
		return htChemicals;
		
	}

	//	I'm commenting this out since we're not including the OPERA persistence model data from ToxVal,
	//	due to applicability domain issues, as discussed.  -Leora

	//	// Note different spellings: "Biodegredation Half-life" vs. "Biodegradation Score"  -Leora
	//	
	//	private void createRecordPersistence_OPERA(Chemical chemical) throws SQLException {
	//		//Get OPERA Persistence (from Biodegredation Half-life).
	//		// I think I changed this code correctly.  -Leora
	//		
	//		String model="OPERA";
	//		String [] fieldNames= {"model","metric"};
	//
	//		String [] fieldValuesPersistence= {model,"Biodegredation Half-life"};			
	//		String query=createSQLQuery(chemical.CAS, "models", RecordToxValModels.varlist, fieldNames, fieldValuesPersistence);			
	//		ResultSet rs=MySQL_DB.getRecords(statToxVal, query);			
	//	
	//		
	//		RecordToxValModels r=null;
	//		
	//		if (rs.next()) {
	//			r=new RecordToxValModels();						
	//			createRecord(rs, r);				
	////			System.out.println("BCF value="+r.value);				
	//		} else {
	//			return;
	//		}
	//
	//		
	//		ParseToxValModels.createScoreRecordPersistence_Opera(chemical, r);
	//	}


	private void createRecordPersistence_EPISUITE(Chemical chemical,Statement statToxVal) throws SQLException {
		//Get EpiSuite Persistence:

		String model="EpiSuite";
		String [] fieldNames= {"model","metric"};

		String [] fieldValuesBCF= {model,"Biodegradation Score"};			
		String query=createSQLQueryByDTXSID(chemical.dtxsid, "models", RecordToxValModels.varlist, fieldNames, fieldValuesBCF);			

		System.out.println(query);

		ResultSet rs=MySQL_DB.getRecords(statToxVal, query);			


		RecordToxValModels r=null;
		//		RecordToxValModels rBCF_AD=null;

		if (rs.next()) {
			System.out.println("Found persistence record");
			r=new RecordToxValModels();						
			createRecord(rs, r);				
			//			System.out.println("BCF value="+r.value);				
		} else {
			return;
		}


		ParseToxValModels.createScoreRecordPersistence_EpiSuite(chemical, r);
	}


	private void createRecordBCF_OPERA(Chemical chemical,Statement statToxVal) throws SQLException {
		//Get OPERA BCF:

		String model="OPERA";
		String [] fieldNames= {"model","metric"};
		String [] fieldValuesBCF= {model,"BCF"};	
		String queryBCF=createSQLQueryByDTXSID(chemical.dtxsid, "models", RecordToxValModels.varlist, fieldNames, fieldValuesBCF);			
		ResultSet rsBCF=MySQL_DB.getRecords(statToxVal, queryBCF);	

		RecordToxValModels rBCF=null;
		RecordToxValModels rBCF_AD=null;

		if (rsBCF.next()) {
			rBCF=new RecordToxValModels();						
			createRecord(rsBCF, rBCF);				
			//			System.out.println("BCF value="+r.value);				
		} else {
			return;
		}

		String [] fieldValuesBCF_AD= {model,"BCF_AD"};		
		String queryBCF_AD=createSQLQueryByDTXSID(chemical.dtxsid, "models", RecordToxValModels.varlist, fieldNames, fieldValuesBCF_AD);			
		ResultSet rsBCF_AD=MySQL_DB.getRecords(statToxVal, queryBCF_AD);

		if (rsBCF_AD.next()) {
			rBCF_AD=new RecordToxValModels();						
			createRecord(rsBCF_AD, rBCF_AD);										
			//			System.out.println("BCF_AD value="+r.value);				
		} else {
			//			System.out.println("No values for AD");
			return;
		}


		ParseToxValModels.createScoreRecordBCF_Opera(chemical, rBCF,rBCF_AD);
	}




	private void createRecordBCF_EPISUITE(Chemical chemical,Statement statToxVal) throws SQLException {
		//Get OPERA BCF:

		String model="EpiSuite";

		String [] fieldNames= {"model","metric"};
		String [] fieldValuesBCF= {model,"BCF"};			
		String queryBCF=createSQLQueryByDTXSID(chemical.dtxsid, "models", RecordToxValModels.varlist, fieldNames, fieldValuesBCF);			
		ResultSet rsBCF=MySQL_DB.getRecords(statToxVal, queryBCF);			

		RecordToxValModels rBCF=null;

		if (rsBCF.next()) {
			rBCF=new RecordToxValModels();						
			createRecord(rsBCF, rBCF);				
			//			System.out.println("BCF value="+r.value);				
		} else {
			return;
		}


		ParseToxValModels.createScoreRecordBCF_EPISUITE(chemical, rBCF);
	}


	public static void createRecord(ResultSet rs, Object r) {
		ResultSetMetaData rsmd;
		try {
			rsmd = rs.getMetaData();

			int columnCount = rsmd.getColumnCount();

			// The column count starts from 1
			for (int i = 1; i <= columnCount; i++ ) {
				String name = rsmd.getColumnLabel(i);
				//				System.out.println(name);								
				String val=rs.getString(i);

				//				logger.warn(name+"\t"+val);

				if (val!=null) {
					Field myField = r.getClass().getDeclaredField(name);			
					myField.set(r, val);
				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.warn("here3a"+e.getMessage());
			logger.warn("here3a"+e.getStackTrace().toString());
		}
	}


	//	public static void createRecord(ResultSet rs, Object r,String CAS) {
	//		ResultSetMetaData rsmd;
	//		try {
	//			rsmd = rs.getMetaData();
	//
	//			int columnCount = rsmd.getColumnCount();
	//
	//			// The column count starts from 1
	//			for (int i = 1; i <= columnCount; i++ ) {
	//				String name = rsmd.getColumnName(i);
	//				//				System.out.println(name);								
	//				String val=rs.getString(i);
	//
	////				if (CAS.equals("10108-64-2")) {
	////					logger.warn(i+" of "+columnCount+"\t"+r.getClass().getName()+"\t"+name+"\t"+val);
	//////					logger.warn("*\t"+r.getClass().getName()+"\t"+name+"\t"+val.length());
	////				}
	//
	//
	//				if (val!=null) {
	//					Field myField = r.getClass().getDeclaredField(name);			
	//					myField.set(r, val);
	//				}
	//				
	////				if (CAS.equals("10108-64-2")) {
	////					logger.warn(i+" set");
	////				}
	//
	//
	//			}
	//
	//		} catch (Exception e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//			logger.warn("error creating tox val record:"+e.getMessage());
	////			logger.warn("here3a"+e.getStackTrace().toString());
	//		}
	//	}

	public Chemicals goThroughRecordsMultipleChemicals(Vector<String>casList,String destfilepathJson, String destfilepathText,String versionToxVal,Statement statToxVal) {


		try {

			Chemicals chemicals = new Chemicals();

			Chemical chemical = new Chemical();			

			for (int i=0;i<casList.size();i++) {

				String CAS=casList.get(i);

				chemical = new Chemical();
				chemical.CAS = CAS;				
				chemicals.add(chemical);

				getDataFromTable_toxval(chemical,versionToxVal, statToxVal);

				// ***Uncomment these later.***
				getDataFromTable_cancer_summary(chemical, statToxVal);
				getDataFromTable_genetox_summary(chemical, statToxVal);
				getDataFromTable_models(chemical, statToxVal);
				getDataFromTable_bcfbaf(chemical, statToxVal);//TODO

			}

			chemicals.writeToFile(destfilepathJson);
			chemicals.toFlatFile(destfilepathText, "\t");
			return chemicals;
			//			writeChemicalToFile(chemical, destfilepath);


		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}


	void getDataFromTable_bcfbaf(Chemical chemical,Statement statToxVal) {

		try {

			String sql=createSQLQueryByDTXSID(chemical.dtxsid,"bcfbaf",RecordToxValBCFBAF.varlist);				

			//				System.out.println(sql);

			ResultSet rs=MySQL_DB.getRecords(statToxVal, sql);

			int count=0;


			while (rs.next()) {						 
				RecordToxValBCFBAF r=new RecordToxValBCFBAF();			
				createRecord(rs, r);

				ParseToxValBCFBAF.createScoreRecord(chemical, r);
				count++;
			}

			//			System.out.println("Records in bcfbaf table for "+chemical.CAS+" = "+count);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}


	void getDataFromTable_bcfbaf(Statement statToxVal) {
		try {

			Hashtable<String, Chemical>htChemicals=new Hashtable<>();

			String sql=createSQLQueryByTable("bcfbaf",RecordToxValBCFBAF.varlist);				

			//			System.out.println(sql);

			ResultSet rs=MySQL_DB.getRecords(statToxVal, sql);

			int count=0;

			List<RecordToxValBCFBAF>records=new ArrayList<>();

			while (rs.next()) {						 
				RecordToxValBCFBAF r=new RecordToxValBCFBAF();			
				createRecord(rs, r);

				records.add(r);

				Chemical chemical=null;

				if(r.dtxsid==null) continue;

				if(htChemicals.get(r.dtxsid)!=null) {
					chemical=htChemicals.get(r.dtxsid);
				} else {
					chemical=new Chemical();
					htChemicals.put(r.dtxsid, chemical);
				}

				ParseToxValBCFBAF.createScoreRecord(chemical, r);
				count++;
			}

//			System.out.println("bcfbaf\t"+records.size()+"\t"+recordCount);
			getCounts("bcfbaf", htChemicals);
			//			System.out.println("Records in bcfbaf table for "+chemical.CAS+" = "+count);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	void getDataFromTable_cancer_summary(Statement statToxVal) {

		try {

			Hashtable<String,String>dictCC=ParseToxValCancer.populateCancerCallToScoreValue();

			Hashtable<String, Chemical>htChemicals=new Hashtable<>();

			String sql=createSQLQueryByTable("cancer_summary",RecordToxValCancer.varlist2);				

			//			System.out.println(sql);

			ResultSet rs=MySQL_DB.getRecords(statToxVal, sql);

			int count=0;

			List<RecordToxValCancer>records=new ArrayList<>();

			while (rs.next()) {						 
				RecordToxValCancer r=new RecordToxValCancer();			
				createRecord(rs, r);

				records.add(r);

				Chemical chemical=null;

				if(r.dtxsid==null) continue;

				if(htChemicals.get(r.dtxsid)!=null) {
					chemical=htChemicals.get(r.dtxsid);
				} else {
					chemical=new Chemical();
					htChemicals.put(r.dtxsid, chemical);
				}

				ParseToxValCancer.createScoreRecord(chemical, r,dictCC);
				count++;
			}

			int recordCount=0;

			getCountsBySource("cancer_summary", htChemicals);


			//			System.out.println("Records in bcfbaf table for "+chemical.CAS+" = "+count);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	void getDataFromTable_models(Statement statToxVal) {

		try {

			String sql=createSQLQueryByTable("models",RecordToxValModels.varlist2);				

			//			System.out.println(sql);
			ResultSet rs=MySQL_DB.getRecords(statToxVal, sql);

			Hashtable<String,List<RecordToxValModels>>htRecordsModels=new Hashtable<>();

			while (rs.next()) {						 
				RecordToxValModels r=new RecordToxValModels();			
				createRecord(rs, r);

//				System.out.println(r.dtxsid);
				if(r.dtxsid==null) continue;

				if(htRecordsModels.get(r.dtxsid)!=null) {
					List<RecordToxValModels>recs=htRecordsModels.get(r.dtxsid);
					recs.add(r);
				} else {
					List<RecordToxValModels>recs=new ArrayList<>();
					recs.add(r);
					htRecordsModels.put(r.dtxsid, recs);
				}
				
//				if(htRecordsModels.size()==100) break;
			}
			
//			System.out.println(htRecordsModels.size());
			
			
			Hashtable<String, Chemical>htChemicals=getDataFromTable_models(htRecordsModels);

			getCountsBySource("models", htChemicals);
			

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	



	void getDataFromTable_genetox_summary(Statement statToxVal) {

		try {

			Hashtable<String,String>dictCC=ParseToxValGenetox.populateGenetoxCallToScoreValue();

			Hashtable<String, Chemical>htChemicals=new Hashtable<>();

			String sql=createSQLQueryByTable("genetox_summary",RecordToxValGenetox.varlist2);				

			//			System.out.println(sql);

			ResultSet rs=MySQL_DB.getRecords(statToxVal, sql);

			int count=0;

			List<RecordToxValGenetox>records=new ArrayList<>();

			while (rs.next()) {						 
				RecordToxValGenetox r=new RecordToxValGenetox();			
				createRecord(rs, r);
				records.add(r);

				Chemical chemical=null;

				if(r.dtxsid==null) continue;

				if(htChemicals.get(r.dtxsid)!=null) {
					chemical=htChemicals.get(r.dtxsid);
				} else {
					chemical=new Chemical();
					htChemicals.put(r.dtxsid, chemical);
				}

				ParseToxValGenetox.createScoreRecord(chemical, r,dictCC);
				count++;
			}

			int recordCount=0;

			getCounts("genetox_summary", htChemicals);


			//			System.out.println("Records in bcfbaf table for "+chemical.CAS+" = "+count);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}


	void runSingleCalculation() {
		
		Statement statToxVal= ParseToxValDB.statToxValv8;
		String versionToxVal=ParseToxValDB.v8;

		String CAS="79-06-1";
		Vector<String>casList=new Vector<String>();		
		casList.add(CAS);

		String folder="AA dashboard/toxval/test spreadsheets";//use relative path so dont have to keep changing this- i.e. it is relative to java installation:  "D:\Users\TMARTI02\OneDrive - Environmental Protection Agency (EPA)\0 java\ghs-data-gathering\AA Dashboard\toxval"

		String filePathRecordsForCAS_json=folder+File.separator+"records_"+CAS+".json"; //
		String filePathRecordsForCAS_txt=folder+File.separator+"records_"+CAS+".txt";			

		Chemicals chemicals=goThroughRecordsMultipleChemicals(casList, filePathRecordsForCAS_json,filePathRecordsForCAS_txt,versionToxVal,statToxVal);

	}
	
	void runBatchCalculation() {

		Statement statToxVal= ParseToxValDB.statToxValv8;
		String versionToxVal=ParseToxValDB.v8;

		String folder="AA dashboard/toxval/test spreadsheets";//use relative path so dont have to keep changing this- i.e. it is relative to java installation:  "D:\Users\TMARTI02\OneDrive - Environmental Protection Agency (EPA)\0 java\ghs-data-gathering\AA Dashboard\toxval"

		String filePathRecordsForCASList_json=folder+File.separator+"toxval_pod_summary_top 10.json"; String
		filePathRecordsForCASList_txt=folder+File.separator+"toxval_pod_summary_Top10.txt"; 			

		Vector<String>casList=new Vector<String>();					

		//Cas numbers in order of appearance on toxval.xls checking spreadsheet:
		casList.add("79-06-1"); 
		casList.add("79-01-6"); 
		casList.add("111-30-8");
		casList.add("75-21-8");
		casList.add("101-77-9");
		casList.add("7803-57-8");
		casList.add("50-00-0");
		casList.add("10588-01-9");
		casList.add("302-01-2");

		//TODO which is 10th?
		// Yes, 108-95-2 is the chemical that isn't in the toxval checking spreadsheet.
		//			casList.add("108-95-2");//dont add it until it is in the toxval spreadsheet

		Vector<String>tableNames=new Vector<>();//tables in toxval, we have a manual xls file for each
		tableNames.add("toxval");
		tableNames.add("bcfbaf");
		tableNames.add("cancer_summary");
		tableNames.add("genetox_summary");
		tableNames.add("models");

		Chemicals chemicals=goThroughRecordsMultipleChemicals(casList, filePathRecordsForCASList_json,filePathRecordsForCASList_txt,versionToxVal,statToxVal);			
		String filePathExcelManual=folder+"/toxval.xlsx";
		compareWithManual(chemicals,folder,tableNames,casList);
	}
	

	List<String> getToxValSourceList(Connection conn) {
		
		List<String>sources=new ArrayList<>();
		
		String sql="select distinct source from toxval_complete order by source";
		
		ResultSet rs=SqlUtilities.runSQL2(conn, sql);
		
		try {
			while (rs.next()) {
				String source=rs.getString(1);
//				System.out.println(source);
				sources.add(source);
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sources;
		
	}
	private void getRecordCountsToxValTables() {

//		String toxvalVersion=ParseToxValDB.v8;  //
//		String toxvalVersion=null;
//		String toxvalVersion=ParseToxValDB.v94; //
		String toxvalVersion=ParseToxValDB.v96;
		
		Connection conn=null;
		Statement stat=null;

		try {
		
			if (toxvalVersion==null) {
				conn=SqlUtilities.getConnectionToxVal();
			} else if (toxvalVersion.equals(ParseToxValDB.v8)) {
				conn=MySQL_DB.getConnection(ParseToxValDB.DB_Path_AA_Dashboard_Records_v8);
			} else if (toxvalVersion.equals(ParseToxValDB.v94)) {
				conn=MySQL_DB.getConnection(ParseToxValDB.DB_Path_AA_Dashboard_Records_v94);
			} else if (toxvalVersion.equals(ParseToxValDB.v96)) {
				conn=MySQL_DB.getConnection(ParseToxValDB.DB_Path_AA_Dashboard_Records_v96);
			}
			
			stat=conn.createStatement();

			List<String>sources=getToxValSourceList(conn);

			System.out.println("toxval");
			getDataFromTable_toxval(stat,toxvalVersion,sources);
//			for (String source:sources) {
//				Hashtable<String, Chemical>htChemicals=getDataFromTable_toxval(source,stat,toxvalVersion);
//			}
			
			getDataFromTable_bcfbaf(stat);//v8: 6973/4182; v94: 6967/4182
			getDataFromTable_cancer_summary(stat);//v8: 2886/2885; v94: 3016/3016
			getDataFromTable_genetox_summary(stat);//16663/16663; v94: 9224/9224
			getDataFromTable_models(stat);
			
			//TODO in toxval v94 models table is blank- need to pull OPERA/EPISUITE predictions from res_qsar database when they are available
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method runs the calculations and compares to manual results stored in a series of spreadsheets
	 * @param args
	 */	
	public static void main(String[] args) {
		ParseToxValDB p = new ParseToxValDB();
		
//		p.runSingleCalculation();
//		p.runBatchCalculation();
		p.getRecordCountsToxValTables();
		
	}

	static Vector<ScoreRecord> getManualResults(String folder,String tableName,Vector<ScoreRecord>recs) {

		try
		{

			FileInputStream file = new FileInputStream(new File(folder+File.separator+tableName+".xlsx"));

			//Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);


			for (int i=0;i<workbook.getNumberOfSheets();i++) {
				//Get first/desired sheet from the workbook
				XSSFSheet sheet = workbook.getSheetAt(i);

				getRecordsFromSheet(recs, sheet,tableName);
			}
			//			System.out.println("here size="+recs.size());


		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return recs;

	}

	private static void getRecordsFromSheet(Vector<ScoreRecord> recs, XSSFSheet sheet,String tableName) {
		try {
			Row row=sheet.getRow(0);

			Hashtable<String,Integer>htColNums=new Hashtable<>();

			//			System.out.println(sheet.getSheetName());

			for (int i=0;i<row.getLastCellNum();i++) {
				Cell cell=row.getCell(i);
				String colName=cell.getStringCellValue();
				htColNums.put(colName,new Integer(i));
			}

			for (int i=1;i<=sheet.getLastRowNum();i++) {
				Row rowi=sheet.getRow(i);

				//				System.out.println("column number for cas="+htColNums.get("casrn"));

				String hazard_name=rowi.getCell(htColNums.get("ManualHazardEndpointCategorization")).getStringCellValue();

				String name=null;
				String CAS=null;

				if (htColNums.get("name")!=null) {
					name=rowi.getCell(htColNums.get("name")).getStringCellValue();										
				}

				if (htColNums.get("casrn")!=null) {
					CAS=rowi.getCell(htColNums.get("casrn")).getStringCellValue();	
				}


				ScoreRecord f=new ScoreRecord(hazard_name,CAS,name);

				//				System.out.println((i+1)+"\t"+sheet.getSheetName());

				if (tableName.contentEquals("toxval")) {
					f.toxvalID=(int)(rowi.getCell(htColNums.get("toxval_id")).getNumericCellValue())+"";	
				} else if (tableName.contentEquals("bcfbaf")) {					
					f.toxvalID="bcfbaf_"+(int)(rowi.getCell(htColNums.get("bcfbaf_id")).getNumericCellValue())+"";					
				} else if (tableName.contentEquals("cancer_summary")) {					
					f.toxvalID="cancer_summary_"+(int)(rowi.getCell(htColNums.get("chemical_id")).getNumericCellValue())+"";									
				} else if (tableName.contentEquals("genetox_summary")) {
					f.toxvalID="genetox_summary_"+(int)(rowi.getCell(htColNums.get("genetox_summary_id")).getNumericCellValue())+"";					
					//					f.toxvalID="genetox_summary_"+(int)(rowi.getCell(htColNums.get("genetox_summary_id")).getNumericCellValue())+"";														f.toxvalID="genetox_summary_"+(int)(rowi.getCell(htColNums.get("genetox_summary")).getNumericCellValue())+"";									
				} else if (tableName.contentEquals("models")) {					
					f.toxvalID="models_"+(int)(rowi.getCell(htColNums.get("model_id")).getNumericCellValue())+"";									
				} else {
					System.out.println("Need to specify ID column name");
				}

				//				f.hazard_name=rowi.getCell(htColNums.get("ManualHazardEndpointCategorization")).getStringCellValue();
				f.score=rowi.getCell(htColNums.get("ManualScore")).getStringCellValue();






				if (htColNums.get("Note")!=null) {
					Cell cell=rowi.getCell(htColNums.get("Note"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

					//					System.out.println("cell value="+cell.getStringCellValue());

					f.note=cell.getStringCellValue();//store leora's note
				}

				if (!hasRecord(recs, f.toxvalID)) {
					recs.add(f);
					//					if (tableName.contentEquals("bcfbaf"))
					//						System.out.println(sheet.getSheetName()+"\t"+f.toxvalID);
				}


				//				System.out.println(f.toxval_id+"\t"+f.hazard_name+"\t"+f.score);

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	static boolean hasRecord(Vector<ScoreRecord> recs,String toxval_id) {
		for (int i=0;i<recs.size();i++) {
			if (recs.get(i).toxvalID.contentEquals(toxval_id)) return true;
		}
		return false;
	}

	static Hashtable<String,ScoreRecord>getHashtable(Vector<ScoreRecord>records) {
		Hashtable<String,ScoreRecord> ht=new Hashtable<>();

		for (int i=0;i<records.size();i++) {
			ScoreRecord rec=records.get(i);
			//			System.out.println(rec.toxvalID);
			ht.put(rec.toxvalID, rec);
		}
		return ht;
	}


	private static void compareWithManual(Chemicals chemicals,String folderExcel, Vector<String>tableNames,Vector<String>casList) {


		Vector<ScoreRecord>recordsManual=new Vector<>();

		for (String tableName:tableNames) {
			getManualResults(folderExcel, tableName, recordsManual);
			//			System.out.println(tableName);
		}

		//		for (int i=0;i<recordsManual.size();i++) {
		//			ScoreRecord recManual=recordsManual.get(i);
		//			System.out.println("recManual: "+i+"\t"+recManual.toxvalID);
		//		}

		Vector<ScoreRecord>recordsJava=getJavaRecords(chemicals);


		for (int i=0;i<recordsJava.size();i++) {
			ScoreRecord recJava=recordsJava.get(i);
			//			System.out.println("recJava: "+i+"\t"+recJava.toxvalID);
		}

		Hashtable<String,ScoreRecord>htManual=getHashtable(recordsManual);
		Hashtable<String,ScoreRecord>htJava=getHashtable(recordsJava);

		System.out.println("\nLooping through manual records:");
		//First loop through manual records to find records present in manual but not in java:
		for (int i=0;i<recordsManual.size();i++) {
			ScoreRecord recManual=recordsManual.get(i);

			//			The following line works if there is a "casrn" column in the spreadsheet:

			//			System.out.println("here1234:"+recManual.toxvalID+"\t"+recManual.CAS);


			if (!casList.contains(recManual.CAS)) continue;//skip record if we hadnt run it in java



			//			if (!recManual.toxval_id.contentEquals("660309"))
			//				return;

			if (recManual.hazardName.contentEquals("Exclude")) continue;

			if (htJava.get(recManual.toxvalID)==null) {

				//				System.out.println(tableName);
				//				I want to get it to print the table name but I can't access the tableName variable.
				System.out.println(recManual.toxvalID+" present in manual, not in Java");

			} else  {
				ScoreRecord recJava=htJava.get(recManual.toxvalID);

				//	System.out.println("here");
				//	System.out.println(recManual.score);
				//	System.out.println(recJava.score);

				if (!recManual.hazardName.contentEquals(recJava.hazardName)) {						
					System.out.println(recJava.toxvalID+"\t"+recJava.hazardName+"\t"+recManual.hazardName+"\tmismatch hazard name\t"+recManual.note);						
				}

				if (!recManual.score.contentEquals(recJava.score)) {						
					System.out.println(recJava.toxvalID+"\t"+recJava.score+"\t"+recManual.score+"\tmismatch score\t"+recManual.note);						
				} 

			}
		}


		//Second loop through java records to find records in java but not in manual:

		System.out.println("\nLooping through java records:");

		for (int i=0;i<recordsJava.size();i++) {
			ScoreRecord recJava=recordsJava.get(i);

			if ((htManual.get(recJava.toxvalID)==null) ||
					htManual.get(recJava.toxvalID).hazardName.contentEquals("Exclude")) {		
				System.out.println(recJava.toxvalID+" present in Java, not in manual");			
			} 
		}

	}

	private static Vector<ScoreRecord> getJavaRecords(Chemicals chemicals) {
		Vector<ScoreRecord>recordsJava=new Vector<>();

		//Go through the all the records
		for (int i=0;i<chemicals.size();i++) {
			Chemical chemical=chemicals.get(i);

			for (int j=0;j<chemical.scores.size();j++) {

				Score score=chemical.scores.get(j);

				for (int k=0;k<score.records.size();k++) {

					ScoreRecord sr=score.records.get(k);

					ScoreRecord recJava=new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
					recJava.toxvalID=sr.toxvalID;					
					recJava.score=sr.score;					

					recordsJava.add(recJava);					
				}				
			}
		}
		//		System.out.println(recordsJava.size());
		return recordsJava;
	}

	public void getDataFromToxValDB(Chemical chemical,String versionToxVal,Statement statToxVal) {

		boolean debug=false;

		if (debug) System.out.print("Getting toxval records...");
		getDataFromTable_toxval(chemical,versionToxVal, statToxVal);
		if (debug) System.out.print("done\n");

		getDataFromTable_cancer_summary(chemical, statToxVal);
		if (debug) System.out.print("Getting cancer records...");
		if (debug) System.out.print("done\n");

		if (debug) System.out.print("Getting genetox records...");
		getDataFromTable_genetox_summary(chemical, statToxVal);
		if (debug) System.out.print("done\n");

		if (debug) System.out.print("Getting model records...");
		getDataFromTable_models(chemical, statToxVal);//TODO instead get from res_qsar
		if (debug) System.out.print("done\n");

		if (debug) System.out.print("Getting bcf records...");
		getDataFromTable_bcfbaf(chemical, statToxVal);//TODO
		if (debug) System.out.print("done\n");
	}

}
