package hazard;

import com.google.gson.annotations.SerializedName;

/**
* @author TMARTI02
*/
public class Params {
	
	@SerializedName("min-similarity")
	Double min_similarity;
		
	@SerializedName("fingerprint-type")
	String fingerprint_type="bingo";
	
	@SerializedName("max-similarity")
    Double max_similarity=1.0;
	
	@SerializedName("similarity-type")
    String similarity_type="tanimoto";
	
	@SerializedName("mass-type")
	String mass_type="monoisotopic-mass";
	@SerializedName("min-mass")
	Double min_mass;
	@SerializedName("max-mass")
	Double max_mass;
	@SerializedName("single-component=true")
	Boolean single_component=true;
	@SerializedName("formula")
	String formula;
	@SerializedName("formula-query")
	String formula_query;
	@SerializedName("filter-stereo")
	Boolean filter_stereo;
	@SerializedName("filter-chiral")
	Boolean filter_chiral;
	@SerializedName("filter-isotopes")
	Boolean filter_isotopes;
	@SerializedName("filter-charged")
	Boolean filter_charged;
	@SerializedName("filter-multicomponent")
	Boolean filter_multicomponent;
	@SerializedName("filter-radicals")
	Boolean filter_radicals;
	@SerializedName("filter-salts")
	Boolean filter_salts;
	@SerializedName("filter-polymers")
	Boolean filter_polymers;
	@SerializedName("filter-sgroups")
	Boolean filter_sgroups;
	@SerializedName("include-elements")
	Boolean include_elements;
	@SerializedName("exclude-elements")
	Boolean exclude_elements;
	@SerializedName("hazard-name")
	String hazard_name;
	@SerializedName("hazard-source")
	String hazard_source;
	@SerializedName("hazard-route")
	String hazard_route;
	@SerializedName("hazard-category")
	String hazard_category;
	@SerializedName("hazard-code")
	String hazard_code;
	@SerializedName("hazard-organism")
	String hazard_organism;
	@SerializedName("min-toxicity")
	String min_toxicity;

    @SerializedName("min-authority")
    String min_authority="Screening";
    
    @SerializedName("export-all-props")
    Boolean export_all_props=true;
	
}