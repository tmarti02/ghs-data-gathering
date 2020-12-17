package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
	
	public QueryField(String setFieldName,String setType,String setLabel,Value setValue) {
		fieldName = setFieldName;
		type = setType;
		label = setLabel;
		values = new ArrayList<Value>();
		values.add(setValue);
	}
	
	public QueryField(String setFieldName,String setType,String setLabel,List<Value> setValues) {
		fieldName = setFieldName;
		type = setType;
		label = setLabel;
		values = setValues;
	}
}
