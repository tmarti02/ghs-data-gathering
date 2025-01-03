package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Replicates the PropertyBlock object of an eChemPortal API search query JSON
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class PropertyBlock {
	@SerializedName("blockOperator")
	@Expose
	private String blockOperator;
	@SerializedName("level")
	@Expose
	private Integer level;
	@SerializedName("type")
	@Expose
	private String type;
	@SerializedName("id")
	@Expose
	private String id;
	@SerializedName("queryBlock")
	@Expose
	public QueryBlock queryBlock;
	
	/**
	 * Full constructor with level for advanced block logic
	 */
	PropertyBlock(int level,String type,QueryBlock queryBlock) {
		// id not needed for query
		this.level = level;
		this.type = type;
		this.queryBlock = queryBlock;
	}
	
	/**
	 * Simplified query block constructor
	 */
	PropertyBlock(QueryBlock queryBlock) {
		// id not needed for query
		level = 0;
		type = "property";
		this.queryBlock = queryBlock;
	}
	
	/**
	 * Simplified operator block constructor
	 */
	PropertyBlock(String blockOperator,int level) {
		this.blockOperator = blockOperator;
		this.level = level;
		type = "operator";
	}
}