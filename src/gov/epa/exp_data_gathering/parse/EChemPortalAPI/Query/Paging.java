package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Replicates the Paging object of an eChemPortal API search query JSON
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class Paging {
	@SerializedName("offset")
	@Expose
	Integer offset;
	@SerializedName("limit")
	@Expose
	Integer limit;
	
	Paging(int offset, int limit) {
		this.offset = offset;
		this.limit = limit;
	}
}