package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import gov.epa.ghs_data_gathering.Utilities.Utilities;


/**
 * Emulates the fields contained in the "toxval_pod_summary_with_references_2020-01-16" spreadsheet/query
 * 
 * @author Todd Martin
 *
 */
public class RecordToxVal {

		
		public String dtxsid;
		public String casrn;
		public String name;
		public String toxval_id;
		public String source;
		public String subsource;
		public String toxval_type;
		public String toxval_type_original;
		public String toxval_subtype;
		public String toxval_subtype_original;
		public String toxval_type_supercategory;
		public String toxval_numeric_qualifier;
		public String toxval_numeric_qualifier_original;
		public String toxval_numeric;
		public String toxval_numeric_original;
		public String toxval_numeric_converted;
		public String toxval_units;
		public String toxval_units_original;
		public String toxval_units_converted;
		public String risk_assessment_class;
		public String study_type;
		public String study_type_original;
		public String study_duration_class;
		public String study_duration_class_original;
		public String study_duration_value;
		public String study_duration_value_original;
		public String study_duration_units;
		public String study_duration_units_original;
		public String species_id;
		public String species_original;
		public String species_common;
		public String species_supercategory;
		public String habitat;
		public String human_eco;
		public String strain;
		public String strain_original;
		public String sex;
		public String sex_original;
		public String generation;
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
		public String quality_id;
		public String priority_id;
		public String source_source_id;
		public String details_text;
		public String toxval_uuid;
		public String toxval_hash;
		public String datestamp;
		public String long_ref;
		public String title;
		public String author;
		public String journal;
		public String volume;	
		public String issue;
		public String url;
		public String document_name;
		public String record_source_type;
		public String record_source_hash;
		
		
		
		public transient static String[] varlist = { "dtxsid", "casrn", "name", "toxval_id", "source", "subsource", "toxval_type",
				"toxval_type_original", "toxval_subtype", "toxval_subtype_original", "toxval_type_supercategory",
				"toxval_numeric_qualifier", "toxval_numeric_qualifier_original", "toxval_numeric",
				"toxval_numeric_original", "toxval_numeric_converted", "toxval_units", "toxval_units_original",
				"toxval_units_converted", "risk_assessment_class", "study_type", "study_type_original",
				"study_duration_class", "study_duration_class_original", "study_duration_value",
				"study_duration_value_original", "study_duration_units", "study_duration_units_original", "species_id",
				"species_original", "species_common", "species_supercategory", "habitat", "human_eco", "strain",
				"strain_original", "sex", "sex_original", "generation", "lifestage", "exposure_route",
				"exposure_route_original", "exposure_method", "exposure_method_original", "exposure_form",
				"exposure_form_original", "media", "media_original", "critical_effect", "year", "quality_id",
				"priority_id", "source_source_id", "details_text", "toxval_uuid", "toxval_hash", "datestamp",
				"long_ref", "title", "author", "journal", "volume", "year", "issue", "url", "document_name",
				"record_source_type", "record_source_hash" };

		
		/**
		 * Create record based on header list and data list in csv using reflection to assign by header name
		 * 
		 * @param hlist
		 * @param list
		 * @return
		 */
		public static RecordToxVal createRecord(String headerLine, String Line) {
			
			
			RecordToxVal r=new RecordToxVal();
			//convert to record:
			try {
//				LinkedList<String>list=Utilities.Parse3(Line, "\t");

				String [] list=Line.split("\t");
				String [] hlist=headerLine.split("\t");
				
//				if (list.length!=hlist.size()) {
//					System.out.println(list.length+"\t"+hlist.size());
//					System.out.println(Line);
//				}
				
				for (int i=0;i<hlist.length;i++) {
					Field myField =r.getClass().getField(hlist[i]);
					myField.set(r, list[i]);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println(Line);
			}
			return r;
		}

}
