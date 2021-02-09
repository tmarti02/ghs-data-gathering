package gov.epa.exp_data_gathering.parse.ChemBL;

import java.util.List;
import com.google.gson.annotations.SerializedName;

/**
 * Classes to replicate the structure of various JSONs downloaded from ChemBL
 * @author GSINCL01
 *
 */
public class JSONsForChemBL {
	
	public class ActivityData {
		@SerializedName("activities")
		public List<RecordChemBL> activities;
		@SerializedName("page_meta")
		public PageMeta pageMeta;
	}
	
	public class PageMeta {
		@SerializedName("limit")
		public Integer limit;
		@SerializedName("next")
		public String next;
		@SerializedName("offset")
		public Integer offset;
		@SerializedName("previous")
		public String previous;
		@SerializedName("total_count")
		public Integer totalCount;
	}
	
}
