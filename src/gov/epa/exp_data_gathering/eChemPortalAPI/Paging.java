package gov.epa.exp_data_gathering.eChemPortalAPI;

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
	public Integer offset;
	@SerializedName("limit")
	@Expose
	public Integer limit;
	
	public Paging(int offset, int limit) {
		this.offset = offset;
		this.limit = limit;
	}
}