package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Query {
	@SerializedName("property_blocks")
	@Expose
	public List<PropertyBlock> propertyBlocks = null;
	@SerializedName("paging")
	@Expose
	public Paging paging;
	@SerializedName("filtering")
	@Expose
	public List<Object> filtering = null;
	@SerializedName("sorting")
	@Expose
	public List<Object> sorting = null;
	@SerializedName("participants")
	@Expose
	public List<Integer> participants = null;
	
	public Query(int limit) {
		propertyBlocks = new ArrayList<PropertyBlock>();
		paging = new Paging(0,limit);
		// filtering & sorting not needed for query
		Integer[] participantsArray = {101,140,580,60,1};
		participants = Arrays.asList(participantsArray);
	}
	
	public void addOperatorBlock(String op) {
		propertyBlocks.add(new PropertyBlock(op.toUpperCase(),0));
	}
	
	public void addPropertyBlock(QueryBlock queryBlock) {
		propertyBlocks.add(new PropertyBlock(queryBlock));
	}
	
	public void updateOffset() {
		paging.offset += paging.limit;
	}
}
