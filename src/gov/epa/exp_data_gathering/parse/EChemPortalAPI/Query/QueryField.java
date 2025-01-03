package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Replicates the QueryField object of an eChemPortal API search query JSON
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class QueryField {
	@SerializedName("fieldName")
	@Expose
	private String fieldName;
	@SerializedName("type")
	@Expose
	private String type;
	@SerializedName("label")
	@Expose
	private String label;
	@SerializedName("values")
	@Expose
	private List<Value> values = null;
	
	/**
	 * Single-value constructor
	 */
	QueryField(String fieldName,String type,String label,Value value) {
		this.fieldName = fieldName;
		this.type = type;
		this.label = label;
		this.values = new ArrayList<Value>();
		this.values.add(value);
	}
	
	/**
	 * Multi-value constructor
	 */
	QueryField(String fieldName,String type,String label,List<Value> values) {
		this.fieldName = fieldName;
		this.type = type;
		this.label = label;
		this.values = values;
	}
}
