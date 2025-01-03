package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Replicates the Sorting object of an eChemPortal API search query JSON
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class Sorting {
	@SerializedName("field")
	@Expose
	private String field;
	@SerializedName("direction")
	@Expose
	private String direction;
	
	Sorting(String field,String direction) {
		this.field = field;
		this.direction = direction;
	}
}
