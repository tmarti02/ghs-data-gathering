package gov.epa.exp_data_gathering.eChemPortalAPI;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Replicates the QueryBlock object of an eChemPortal API search query JSON
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
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
	
	// Property search block - full constructor with level for more complex boolean logic
	public PropertyBlock(int level,String type,QueryBlock queryBlock) {
		// id not needed for query
		this.level = level;
		this.type = type;
		this.queryBlock = queryBlock;
	}
	
	// Property search block - simplified constructor
	public PropertyBlock(QueryBlock queryBlock) {
		// id not needed for query
		level = 0;
		type = "property";
		this.queryBlock = queryBlock;
	}
	
	// Operator block constructor
	public PropertyBlock(String blockOperator,int level) {
		this.blockOperator = blockOperator;
		this.level = level;
		type = "operator";
	}
}