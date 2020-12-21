package gov.epa.exp_data_gathering.eChemPortalAPI;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Replicates the Value object of an eChemPortal API search query JSON
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
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
	
	// Constructor for range or numeric search
	public Value(String type,String opOrLower,String valueOrUpper,Unit unit) {
		switch (type) {
		case "range":
			matchMode = "OVERLAPPING";
			searchValueLower = opOrLower;
			searchValueUpper = valueOrUpper;
			this.unit = unit;
			break;
		case "numeric":
			// This turns out to be unnecessary for physchem data, but leaving the code in case it is useful for tox data
			this.op = opOrLower;
			searchValue = valueOrUpper;
			this.unit = unit;
			break;
		}
	}
	
	// Constructor for string expression search
	public Value(String op,String searchExpression) {
		this.op = op;
		this.searchExpression = searchExpression;
	}
}