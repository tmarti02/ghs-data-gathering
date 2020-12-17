package gov.epa.exp_data_gathering.eChemPortalAPI;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Value {
	@SerializedName("op")
	@Expose
	public String op;
	@SerializedName("searchExpression")
	@Expose
	public String searchExpression;
	@SerializedName("matchMode")
	@Expose
	public String matchMode;
	@SerializedName("searchValue")
	@Expose
	public String searchValue;
	@SerializedName("searchValueLower")
	@Expose
	public String searchValueLower;
	@SerializedName("searchValueUpper")
	@Expose
	public String searchValueUpper;
	@SerializedName("unit")
	@Expose
	public Unit unit;
	
	// Range or numeric search
	public Value(String type,String opOrLower,String valueOrUpper,Unit setUnit) {
		switch (type) {
		case "range":
			matchMode = "OVERLAPPING";
			searchValueLower = opOrLower;
			searchValueUpper = valueOrUpper;
			unit = setUnit;
			break;
		case "numeric":
			// This turns out to be unnecessary for physchem data, but leaving the code in case it is useful for tox data
			op = opOrLower;
			searchValue = valueOrUpper;
			unit = setUnit;
			break;
		}
	}
	
	// String expression search
	public Value(String setOp,String setSearchExpression) {
		op = setOp;
		searchExpression = setSearchExpression;
	}
}