package gov.epa.ghs_data_gathering.Utilities;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JSONsForEChemPortal {

	public class ResultsData {
		@SerializedName("results")
		@Expose
		public List<Result> results = null;
		@SerializedName("page_info")
		@Expose
		public PageInfo pageInfo;
		@SerializedName("filter_options")
		@Expose
		public List<FilterOption> filterOptions = null;
		@SerializedName("subtancesWithProperties")
		@Expose
		public List<Object> subtancesWithProperties = null;
		@SerializedName("subtancesWithGhs")
		@Expose
		public List<Object> subtancesWithGhs = null;
	}
	
	public class Result {
		@SerializedName("baseUrl")
		@Expose
		public String baseUrl;
		@SerializedName("chapter")
		@Expose
		public String chapter;
		@SerializedName("endpoint_key")
		@Expose
		public String endpointKey;
		@SerializedName("endpoint_kind")
		@Expose
		public String endpointKind;
		@SerializedName("endpoint_url")
		@Expose
		public String endpointUrl;
		@SerializedName("member_of_category")
		@Expose
		public Boolean memberOfCategory;
		@SerializedName("blocks")
		@Expose
		public List<Block> blocks = null;
		@SerializedName("participant_id")
		@Expose
		public Integer participantId;
		@SerializedName("participant_acronym")
		@Expose
		public String participantAcronym;
		@SerializedName("participant_url")
		@Expose
		public String participantUrl;
		@SerializedName("substance_id")
		@Expose
		public String substanceId;
		@SerializedName("name")
		@Expose
		public String name;
		@SerializedName("name_type")
		@Expose
		public String nameType;
		@SerializedName("number")
		@Expose
		public String number;
		@SerializedName("substance_url")
		@Expose
		public String substanceUrl;
		@SerializedName("number_type")
		@Expose
		public String numberType;
	}
	
	public class PageInfo {
		@SerializedName("size")
		@Expose
		public Integer size;
		@SerializedName("total_elements")
		@Expose
		public Integer totalElements;
		@SerializedName("total_pages")
		@Expose
		public Integer totalPages;
		@SerializedName("number")
		@Expose
		public Integer number;
	}
	
	public class FilterOption {
		@SerializedName("field")
		@Expose
		public String field;
		@SerializedName("options")
		@Expose
		public List<String> options = null;
	}
	
	public class Block {
		@SerializedName("nested_blocks")
		@Expose
		public List<NestedBlock> nestedBlocks = null;
	}
	
	public class NestedBlock {
		@SerializedName("original_values")
		@Expose
		public List<OriginalValue> originalValues = null;
	}
	
	public class OriginalValue {
		@SerializedName("name")
		@Expose
		public String name;
		@SerializedName("value")
		@Expose
		public String value;
	}
}
