package gov.epa.exp_data_gathering.eChemPortalAPI;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Paging {
	@SerializedName("offset")
	@Expose
	public Integer offset;
	@SerializedName("limit")
	@Expose
	public Integer limit;
	
	public Paging(int setOffset, int setLimit) {
		offset = setOffset;
		limit = setLimit;
	}
}