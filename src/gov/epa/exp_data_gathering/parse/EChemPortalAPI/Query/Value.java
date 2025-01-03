package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query;

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
	private String op;
	@SerializedName("searchExpression")
	@Expose
	private String searchExpression;
	@SerializedName("matchMode")
	@Expose
	private String matchMode;
	@SerializedName("searchValue")
	@Expose
	private String searchValue;
	@SerializedName("searchValueLower")
	@Expose
	private String searchValueLower;
	@SerializedName("searchValueUpper")
	@Expose
	private String searchValueUpper;
	@SerializedName("unit")
	@Expose
	private Unit unit;
	
	/**
	 * Constructor for range- or numeric-type search
	 * @param type			"range","numeric"
	 * @param opOrLower		Operator (e.g. "LT","EQUALS") if numeric, min value if range
	 * @param valueOrUpper	Search value if numeric, max value if range
	 * @param unit			Units
	 */
	Value(String type,String opOrLower,String valueOrUpper,Unit unit) {
		switch (type) {
		case "range":
			matchMode = "OVERLAPPING";
			searchValueLower = opOrLower;
			searchValueUpper = valueOrUpper;
			this.unit = unit;
			break;
		case "numeric":
			this.op = opOrLower;
			searchValue = valueOrUpper;
			this.unit = unit;
			break;
		}
	}
	
	/**
	 * Constructor for string-type search
	 * @param op				Operator (e.g. "EQUALS","LIKE")
	 * @param searchExpression	String search expression
	 */
	Value(String op,String searchExpression) {
		this.op = op;
		this.searchExpression = searchExpression;
	}
}