package gov.epa.exp_data_gathering.parse.Arnot2006;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

/**
* @author TMARTI02
*/
public class RecordArnot2006 {

	static String fileName="arnot 2006 a06-005.xls";
	static String sourceName="Arnot 2006";
	
	static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues()
			.create();

	
	String endpoint_sorting_category;
	String casrn;
	String chemical_name;
	String estimated_LogKow;
	String measured_LogKow_episuite;
	String measured_LogKow_other;
	String LogKow_Reference;
	String LogKow1;
	String LogKow2;
	String organism_classification;
	String scientific_name;
	String common_name;
	String LogBAF_WW_L_kg;
	String LogBCF_WW_L_kg;
	String water_concentration_mean_ug_L;
	String water_concentration_type;
	String radiolabel_type;
	String exposure_duration_days;
	String exposure_type;
	String temperature_mean_C;
	String exposure_route;
	String exposure_media;
	String ph_mean;
	String total_organic_carbon_mg_L;
	String wet_weight_mean_g;
	String lipid_content_percentage;
	String tissue_analyzed;
	String calculation_method;
	
	String comments;
	
	String source_author;
	String source_year;
	String source_title;
	String source_journal;
	
	String criterion_water_concentration_measured;
	
	String criterion_radiolabel;
	String criterion_radiolabel_comment;
	
	String criterion_aqueous_solubility;
	String criterion_aqueous_solubility_comment;
	
	String criterion_exposure_duration;
	String criterion_exposure_duration_comment;
	
	String criterion_tissue_analyzed;
	
	String criterion_other_major_source;
	String criterion_other_major_source_comment;
	
	String overall_score;

	
	public static Vector<JsonObject> parseRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		HashMap<Integer, String> hmFieldNames = getHeaderMap();
		Vector<JsonObject> records = esr.parseRecordsFromExcel(hmFieldNames,-1);
		
		for(int i=1;i<=10;i++) {
			records.remove(0);
		}
		
//		for(JsonObject jo:records) {
//			RecordArnot2006 rec=gson.fromJson(jo, RecordArnot2006.class);
////			System.out.println(gson.toJson(rec));	
//		}
		
		return records;
	}


	private static  HashMap<Integer, String> getHeaderMap() {
		HashMap<Integer, String> hmFieldNames=new HashMap<>();
		for(int i=0;i<=50;i++) {
			hmFieldNames.put(i,"Col"+i);
		}
		int i=0;
		
		hmFieldNames.put(i++,"endpoint_sorting_category");
		hmFieldNames.put(i++,"casrn");
		hmFieldNames.put(i++,"chemical_name");
		hmFieldNames.put(i++,"estimated_LogKow");
		hmFieldNames.put(i++,"measured_LogKow_episuite");
		hmFieldNames.put(i++,"measured_LogKow_other");
		hmFieldNames.put(i++,"LogKow_Reference");
		hmFieldNames.put(i++,"LogKow1");
		hmFieldNames.put(i++,"LogKow2");
		hmFieldNames.put(i++,"organism_classification");
		hmFieldNames.put(i++,"scientific_name");
		hmFieldNames.put(i++,"common_name");
		hmFieldNames.put(i++,"LogBAF_WW_L_kg");
		hmFieldNames.put(i++,"LogBCF_WW_L_kg");
		hmFieldNames.put(i++,"water_concentration_mean_ug_L");
		hmFieldNames.put(i++,"water_concentration_type");
		hmFieldNames.put(i++,"radiolabel_type");
		hmFieldNames.put(i++,"exposure_duration_days");
		hmFieldNames.put(i++,"exposure_type");
		hmFieldNames.put(i++,"temperature_mean_C");
		hmFieldNames.put(i++,"exposure_route");
		hmFieldNames.put(i++,"exposure_media");
		hmFieldNames.put(i++,"ph_mean");
		hmFieldNames.put(i++,"total_organic_carbon_mg_L");
		hmFieldNames.put(i++,"wet_weight_mean_g");
		hmFieldNames.put(i++,"lipid_content_percentage");
		hmFieldNames.put(i++,"tissue_analyzed");
		hmFieldNames.put(i++,"calculation_method");
		hmFieldNames.put(i++,"comments");
		hmFieldNames.put(i++,"source_author");
		hmFieldNames.put(i++,"source_year");
		hmFieldNames.put(i++,"source_title");
		hmFieldNames.put(i++,"source_journal");
		i++;
		hmFieldNames.put(i++,"criterion_water_concentration_measured");
		i++;
		hmFieldNames.put(i++,"criterion_radiolabel");
		hmFieldNames.put(i++,"criterion_radiolabel_comment");
		i++;
		hmFieldNames.put(i++,"criterion_aqueous_solubility");
		hmFieldNames.put(i++,"criterion_aqueous_solubility_comment");
		i++;
		hmFieldNames.put(i++,"criterion_exposure_duration");
		hmFieldNames.put(i++,"criterion_exposure_duration_comment");
		i++;
		hmFieldNames.put(i++,"criterion_tissue_analyzed");
		i++;
		hmFieldNames.put(i++,"criterion_other_major_source");
		hmFieldNames.put(i++,"criterion_other_major_source_comment");
		i++;
		hmFieldNames.put(i,"overall_score");
		return hmFieldNames;
	}
	
	
	public static void main(String[] args) {
		RecordArnot2006 r=new RecordArnot2006();
		r.parseRecordsFromExcel();
	}
	
	

}



