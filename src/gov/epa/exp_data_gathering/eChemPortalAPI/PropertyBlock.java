package gov.epa.exp_data_gathering.eChemPortalAPI;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PropertyBlock {
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
	
	// Property search block - simplified
	public PropertyBlock(QueryBlock setQueryBlock) {
		// id not needed for query
		level = 0;
		type = "property";
		queryBlock = setQueryBlock;
	}
	
	// Operator block
	public PropertyBlock(String setBlockOperator,int setLevel) {
		blockOperator = setBlockOperator;
		level = setLevel;
		type = "operator";
	}
}