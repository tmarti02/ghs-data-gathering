package gov.epa.ghs_data_gathering.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JSONsForEChemPortal {

	public static class QueryData {
		@SerializedName("property_blocks")
		@Expose
		public List<PropertyBlock> propertyBlocks = null;
		@SerializedName("paging")
		@Expose
		public Paging paging;
		@SerializedName("filtering")
		@Expose
		public List<Object> filtering = null;
		@SerializedName("sorting")
		@Expose
		public List<Object> sorting = null;
		@SerializedName("participants")
		@Expose
		public List<Integer> participants = null;
		
		public QueryData(PropertyBlock setPropertyBlock) {
			propertyBlocks = new ArrayList<PropertyBlock>();
			propertyBlocks.add(setPropertyBlock);
			paging = new Paging(0,100);
			// filtering & sorting not needed for query
			Integer[] participantsArray = {101,140,580,60,1};
			participants = Arrays.asList(participantsArray);
		}
		
		public void updateOffset(int newOffset) {
			paging.offset = newOffset;
		}
	}
	
	public static class PropertyBlock {
		@SerializedName("blockOperator")
		@Expose
		public String blockOperator;
		@SerializedName("level")
		@Expose
		public Integer level;
		@SerializedName("type")
		@Expose
		public String type;
		@SerializedName("id")
		@Expose
		public String id;
		@SerializedName("queryBlock")
		@Expose
		public QueryBlock queryBlock;
		
		// Property search block
		public PropertyBlock(int setLevel,String setType,QueryBlock setQueryBlock) {
			// id not needed for query
			level = setLevel;
			type = setType;
			queryBlock = setQueryBlock;
		}
		
		// Operator block
		public PropertyBlock(String setBlockOperator,int setLevel,String setType) {
			blockOperator = setBlockOperator;
			level = setLevel;
			type = setType;
		}
	}
	
	public static class Paging {
		@SerializedName("offset")
		@Expose
		public Integer offset;
		@SerializedName("limit")
		@Expose
		public Integer limit;
		
		public Paging(int setOffset, int setLimit) {
			offset = setOffset;
			limit = setLimit;
		}
	}
	
	public static class QueryBlock {
		@SerializedName("endpointKind")
		@Expose
		public String endpointKind;
		@SerializedName("queryFields")
		@Expose
		public List<QueryField> queryFields = null;
		
		public QueryBlock(String setEndpointKind, QueryField setQueryField) {
			endpointKind = setEndpointKind;
			queryFields = new ArrayList<QueryField>();
			queryFields.add(setQueryField);
		}
	}
	
	public static class QueryField {
		@SerializedName("fieldName")
		@Expose
		public String fieldName;
		@SerializedName("type")
		@Expose
		public String type;
		@SerializedName("label")
		@Expose
		public String label;
		@SerializedName("values")
		@Expose
		public List<Value> values = null;
		
		public QueryField(String setFieldName,String setType,Value setValue) {
			fieldName = setFieldName;
			type = setType;
			label = setFieldName.substring(setFieldName.lastIndexOf(".")+1);
			values = new ArrayList<Value>();
			values.add(setValue);
		}
	}
	
	public static class Value {
		@SerializedName("op")
		@Expose
		public String op;
		@SerializedName("searchExpression")
		@Expose
		public String searchExpression;
		@SerializedName("matchMode")
		@Expose
		public String matchMode;
		@SerializedName("searchValueLower")
		@Expose
		public String searchValueLower;
		@SerializedName("searchValueUpper")
		@Expose
		public String searchValueUpper;
		@SerializedName("unit")
		@Expose
		public Unit unit;
		
		// Range
		public Value(String setSearchValueLower,String setSearchValueUpper,Unit setUnit) {
			matchMode = "OVERLAPPING";
			searchValueLower = setSearchValueLower;
			searchValueUpper = setSearchValueUpper;
			unit = setUnit;
		}
		
		// Discrete value(s)
		public Value(String setOp,String setSearchExpression) {
			op = setOp;
			searchExpression = setSearchExpression;
		}
	}
	
	public static class Unit {
		@SerializedName("phraseGroupId")
		@Expose
		public String phraseGroupId;
		@SerializedName("phraseId")
		@Expose
		public String phraseId;
		
		public Unit(String setPhraseGroupID,String setPhraseID) {
			phraseGroupId = setPhraseGroupID;
			phraseId = setPhraseID;
		}
	}
	
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
