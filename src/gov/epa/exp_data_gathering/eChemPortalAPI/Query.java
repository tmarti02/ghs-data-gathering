package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Replicates the wrapper of an eChemPortal API search query JSON
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
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
	public List<Sorting> sorting = null;
	@SerializedName("participants")
	@Expose
	public List<Integer> participants = null;
	
	// Constructor with empty property blocks and a specified page size limit
	public Query(int limit, boolean sortingOn) {
		propertyBlocks = new ArrayList<PropertyBlock>();
		paging = new Paging(0,limit);
		// Filtering not needed for this application
		// Default to sort by number (CAS/EINECS)
		if (sortingOn) { sorting.add(new Sorting("number","asc")); }
		// Accepts all participants (CCR, CHEM, IUCLID, J-CHECK, REACH)
		Integer[] participantsArray = {101,140,580,60,1};
		participants = Arrays.asList(participantsArray);
	}
	
	// Adds an operator block
	public void addOperatorBlock(String op) {
		propertyBlocks.add(new PropertyBlock(op.toUpperCase(),0));
	}
	
	// Adds a property query block
	public void addPropertyBlock(QueryBlock queryBlock) {
		propertyBlocks.add(new PropertyBlock(queryBlock));
	}
	
	// Increments offset to reach the next page
	public void updateOffset() {
		paging.offset += paging.limit;
	}
}
