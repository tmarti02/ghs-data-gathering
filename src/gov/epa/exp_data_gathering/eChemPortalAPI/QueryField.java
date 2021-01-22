package gov.epa.exp_data_gathering.eChemPortalAPI;

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
	
	// Constructor for a single value
	public QueryField(String fieldName,String type,String label,Value value) {
		this.fieldName = fieldName;
		this.type = type;
		this.label = label;
		this.values = new ArrayList<Value>();
		this.values.add(value);
	}
	
	// Constructor for multiple values
	public QueryField(String fieldName,String type,String label,List<Value> values) {
		this.fieldName = fieldName;
		this.type = type;
		this.label = label;
		this.values = values;
	}
}
