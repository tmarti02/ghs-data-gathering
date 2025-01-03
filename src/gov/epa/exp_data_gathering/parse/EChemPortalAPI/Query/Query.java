package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query;

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
	Paging paging;
	@SerializedName("filtering")
	@Expose
	private List<Object> filtering = null;
	@SerializedName("sorting")
	@Expose
	private List<Sorting> sorting = null;
	@SerializedName("participants")
	@Expose
	private List<Integer> participants = null;
	
	/**
	 * Constructor with empty property block list and a specified page size limit
	 * @param limit		Results page size limit (5000 performed best in testing)
	 */
	Query(int limit) {
		propertyBlocks = new ArrayList<PropertyBlock>();
		paging = new Paging(0,limit);
		// Filtering not needed for this application
		// Default to sort by number (CAS/EINECS)
		sorting = new ArrayList<Sorting>();
		sorting.add(new Sorting("number","asc"));
		// Accepts all participants (CCR, CHEM, IUCLID, J-CHECK, REACH)
//		Integer[] participantsArray = {101,140,580,60,1};
		
		//TMM update based on latest options on website:
		Integer[] participantsArray = {101,761,60,1};
		
		participants = Arrays.asList(participantsArray);
	}
	
	/**
	 * Adds an operator block to the query
	 * @param op	Operator to add ("AND","OR","NOT")
	 */
	void addOperatorBlock(String op) {
		propertyBlocks.add(new PropertyBlock(op.toUpperCase(),0));
	}
	
	/**
	 * Adds a property query block
	 * @param queryBlock	QueryBlock object to add
	 */
	void addPropertyBlock(QueryBlock queryBlock) {
		propertyBlocks.add(new PropertyBlock(queryBlock));
	}
	
	/**
	 * Increments offset to reach the next query page
	 */
	void updateOffset() {
		paging.offset += paging.limit;
	}
}
