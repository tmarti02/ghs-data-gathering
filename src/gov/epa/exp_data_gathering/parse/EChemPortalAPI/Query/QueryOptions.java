package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Defines options for an eChemPortal API query
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class QueryOptions {
	protected int limit = 5000; // Most efficient page limit in most tests
	protected String endpointKind = null;
	protected int maxReliabilityLevel = 2; // Recommended
	protected String afterYear = null;
	protected String beforeYear = null;
	protected String endpointMin = null;
	protected String endpointMax = null;
	protected String endpointUnits = null;
	protected boolean includeAllUnits = false;
	protected String pressureMin = null;
	protected String pressureMax = null;
	protected String pressureUnits = null;
	protected boolean includeNullPressure = false;
	protected String temperatureMin = null;
	protected String temperatureMax = null;
	protected String temperatureUnits = null;
	protected boolean includeNullTemperature = false;
	protected String pHMin = null;
	protected String pHMax = null;
	protected boolean includeNullpH = false;
	protected List<StringField> stringFields;
	protected boolean includeAllDuration = false;
	
	/**
	 * Nested class to store data for query fields that are handled as lists of strings
	 * @author GSINCL01 (Gabriel Sinclair)
	 *
	 */
	static class StringField {
		String field;
		List<String> fieldValues;
		boolean includeOther;
		boolean includeAll;
		
		StringField(String field,List<String> fieldValues,boolean includeOther) {
			this.field = field;
			this.fieldValues = fieldValues;
			this.includeOther = includeOther;
			this.includeAll = false;
		}
		
		StringField(String field) {
			this.field = field;
			this.includeAll = true;
		}
	}
	
	QueryOptions() {
		stringFields = new ArrayList<StringField>();
	}
	
	/**
	 * Creates a copy of the current QueryOptions object by serializing/deserializing
	 * @return	Another QueryOptions object with the same contents
	 */
	private QueryOptions copy() {
		Gson gson = new GsonBuilder().create();
		String optionsJSON = gson.toJson(this);
		return gson.fromJson(optionsJSON,QueryOptions.class);
	}
	
	/**
	 * Creates a default set of options that will download all available records for a single property
	 * @param endpointKind		Desired property from ExperimentalConstants
	 */
	public QueryOptions(String endpointKind) {
		this.endpointKind = endpointKind;
		if (endpointKind.equals(APIConstants.meltingPoint) || endpointKind.equals(APIConstants.boilingPoint) || 
				endpointKind.equals(APIConstants.flashPoint)) {
			endpointMin = "0";
			endpointMax = "10000";
			includeAllUnits = true;
			pressureMin = "0";
			pressureUnits = APIConstants.pa;
			includeNullPressure = true;
		} else if (endpointKind.equals(APIConstants.density)) {
			endpointMin = "0";
			endpointMax = "1000";
			includeAllUnits = true;
			temperatureMin = "0";
			temperatureUnits = APIConstants.K;
			includeNullTemperature = true;
		} else if (endpointKind.equals(APIConstants.vaporPressure)) {
			endpointMin = "0";
			endpointMax = "5000000";
			includeAllUnits = true;
			temperatureMin = "0";
			temperatureUnits = APIConstants.K;
			includeNullTemperature = true;
		} else if (endpointKind.equals(APIConstants.dissociationConstant)) {
			endpointMin = "-1000";
			endpointMax = "1000";
			includeAllUnits = true;
			temperatureMin = "0";
			temperatureUnits = APIConstants.K;
			includeNullTemperature = true;
		} else if (endpointKind.equals(APIConstants.partitionCoefficient)) {
			endpointMin = "-1000";
			endpointMax = "126000000000"; // Largest non-log Pow value in eChemPortal
			includeAllUnits = true;
			temperatureMin = "0";
			temperatureUnits = APIConstants.K;
			includeNullTemperature = true;
			pHMin = "-1000";
			includeNullpH = true;
		} else if (endpointKind.equals(APIConstants.waterSolubility)) {
			endpointMin = "0";
			endpointMax = "5000";
			includeAllUnits = true;
			temperatureMin = "0";
			temperatureUnits = APIConstants.K;
			includeNullTemperature = true;
			pHMin = "-1000";
			includeNullpH = true;
		} else if (endpointKind.equals(APIConstants.henrysLawConstant)) {
			this.endpointKind = APIConstants.henrysLawConstant;
			endpointMin = String.valueOf(Integer.MIN_VALUE);
			includeAllUnits = true;
			pressureMin = "0";
			pressureUnits = APIConstants.pa;
			includeNullPressure = true;
			temperatureMin = "0";
			temperatureUnits = APIConstants.K;
			includeNullTemperature = true;
		}
	}
	
	/**
	 * Splits the QueryOptions object at the range midpoint to reduce query size
	 * @return		Two QueryOptions objects that jointly cover the same range as the original
	 */
	private List<QueryOptions> splitOptionsByValue() {
		List<QueryOptions> splitOptions = new ArrayList<QueryOptions>();
		QueryOptions lowerSplitOptions = this.copy();
		QueryOptions upperSplitOptions = this.copy();
		double min = endpointMin==null ? Integer.MIN_VALUE : Double.parseDouble(endpointMin);
		double max = endpointMax==null ? Integer.MAX_VALUE : Double.parseDouble(endpointMax);
		double midpoint = min + (max - min)/2.0;
		lowerSplitOptions.endpointMax = String.valueOf(midpoint);
		upperSplitOptions.endpointMin = String.valueOf(midpoint);
		splitOptions.add(lowerSplitOptions);
		splitOptions.add(upperSplitOptions);
		return splitOptions;
	}
	
	/**
	 * Splits the QueryOptions object at the midpoint of the time range to reduce query size
	 * @return		Two QueryOptions objects that jointly cover the same time range as the original
	 */
	private List<QueryOptions> splitOptionsByYear() {
		List<QueryOptions> splitOptions = new ArrayList<QueryOptions>();
		QueryOptions lowerSplitOptions = this.copy();
		QueryOptions upperSplitOptions = this.copy();
		Date date = new Date();
		int thisYear = date.getYear()+1900;
		int minYear = this.afterYear==null ? 1900 : Integer.parseInt(this.afterYear);
		int maxYear = this.beforeYear==null ? thisYear : Integer.parseInt(this.beforeYear);
		String midpointYear = String.valueOf((maxYear+minYear)/2);
		lowerSplitOptions.beforeYear = midpointYear;
		upperSplitOptions.afterYear = midpointYear;
		splitOptions.add(lowerSplitOptions);
		splitOptions.add(upperSplitOptions);
		return splitOptions;
	}
	
	/**
	 * Merges the endpoint range of another QueryOptions object into the current QueryOptions
	 * @param options	The QueryOptions object to merge
	 */
	protected boolean mergeOptions(QueryOptions options) {
		if (this.endpointKind.equals(options.endpointKind)) {
			if (this.endpointMax.equals(options.endpointMin)) {
				this.endpointMax = options.endpointMax;
				return true;
			} else if (this.endpointMin.equals(options.endpointMax)) {
				this.endpointMin = options.endpointMin;
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Gets the maximum size of the query corresponding to the given options (i.e. with no conditions specified)
	 * @return
	 */
	private int getQueryMaxSize() {
		Query query = new Query(limit);
		QueryBlock queryBlock = generateQueryBlock(true,true,true);
		query.addPropertyBlock(queryBlock);
		QueryHandler handler = new QueryHandler(1000,10);
		int size = handler.getQuerySize(query);
		if (size==0) {
			System.out.println("Warning: Query returns 0 results. Make sure you have not included invalid fields for the endpoint kind!");
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			System.out.println(gson.toJson(query));
		}
		return size;
	}
	
	/**
	 * Recursively splits a vector of QueryOptions until all queries have size {@literal <} 10000
	 * @param options	Vector of QueryOptions to be resized
	 * @return			Vector of QueryOptions of permitted size
	 */
	private static List<QueryOptions> resizeAll(List<QueryOptions> options, int maxSize) {
		List<QueryOptions> newOptions = new ArrayList<QueryOptions>();
		for (QueryOptions o:options) {
			int size = o.getQueryMaxSize();
			if (size >= maxSize) {
				System.out.println("Resizing...");
				List<QueryOptions> splitOptions = o.splitOptionsByValue();
				splitOptions = resizeAll(splitOptions,maxSize);
				newOptions.addAll(splitOptions);
			} else {
				newOptions.add(o);
			}
		}
		return newOptions;
	}
	
	/**
	 * If QueryOptions specify a query too large for the API (limit 10000 results), splits it into a vector of permitted query size
	 * @return		Vector of QueryOptions of permitted size
	 */
	private List<QueryOptions> resize(int maxSize) {
		List<QueryOptions> options = new ArrayList<QueryOptions>();
		options.add(this);
		if (getQueryMaxSize() >= maxSize) {
			System.out.println(this.endpointKind+" query too large. Resizing...");
			options = resizeAll(options,maxSize);
			System.out.println("Split into "+options.size()+" queries. Merging small queries...");
			ListIterator<QueryOptions> it = options.listIterator();
			int lastSize = 0;
			while (it.hasNext()) {
				QueryOptions currentOptions = it.next();
				int size = currentOptions.getQueryMaxSize();
				if (it.previousIndex() > 0 && size + lastSize < maxSize) {
					it.remove();
					QueryOptions lastOptions = it.previous();
					lastOptions.mergeOptions(currentOptions);
					it.set(lastOptions);
					lastSize = lastOptions.getQueryMaxSize();
					it.next();
				} else {
					lastSize = size;
				}
			}
			System.out.println("Merged small queries. Running "+options.size()+" queries...");
		} else {
			System.out.println("No resizing needed. Running query...");
		}
		return options;
	}
	
	/**
	 * Creates the QueryBlock object corresponding to the given options
	 * @param removePressureField		Ignore pressure condition
	 * @param removeTemperatureField	Ignore temperature condition
	 * @param removepHField				Ignore pH condition
	 * @return		The desired QueryBlock
	 */
	private QueryBlock generateQueryBlock(boolean removePressureField,boolean removeTemperatureField,boolean removepHField) {
		QueryBlock queryBlock = new QueryBlock(endpointKind);
		queryBlock.addInfoTypeField();
		queryBlock.addReliabilityField(maxReliabilityLevel);
		
		// Endpoint value
		if (includeAllUnits) {
			queryBlock.addAllUnitRangeField(APIConstants.endpoint,endpointMin,endpointMax);
		} else {
			queryBlock.addRangeField(APIConstants.endpoint,endpointMin,endpointMax,endpointUnits);
		}
		
		// Pressure condition
		boolean hasPressureCondition = pressureMin!=null || pressureMax!=null;
		if (!removePressureField && hasPressureCondition && (endpointKind.equals(APIConstants.meltingPoint) || 
				endpointKind.equals(APIConstants.boilingPoint) || 
				endpointKind.equals(APIConstants.flashPoint) ||
				endpointKind.equals(APIConstants.henrysLawConstant))) {
			queryBlock.addRangeField(APIConstants.pressure,pressureMin,pressureMax,pressureUnits);
		} else if (hasPressureCondition && !removePressureField) {
			System.out.println("Warning: Pressure condition not supported for "+endpointKind+". Non-null values ignored.");
		}
		
		// Temperature condition
		boolean hasTemperatureCondition = temperatureMin!=null || temperatureMax!=null;
		if (!removeTemperatureField && hasTemperatureCondition && (endpointKind.equals("Density") ||
				endpointKind.equals(APIConstants.vaporPressure) ||
				endpointKind.equals(APIConstants.partitionCoefficient) || 
				endpointKind.equals(APIConstants.waterSolubility) ||
				endpointKind.equals(APIConstants.dissociationConstant) ||
				endpointKind.equals(APIConstants.henrysLawConstant))) {
			queryBlock.addRangeField(APIConstants.temperature,temperatureMin,temperatureMax,temperatureUnits);
		} else if (hasTemperatureCondition && !removeTemperatureField) {
			System.out.println("Warning: Temperature condition not supported for "+endpointKind+". Non-null values ignored.");
		}
		
		// pH condition
		boolean haspHCondition = pHMin!=null || pHMax!=null;
		if (!removepHField && haspHCondition && (endpointKind.equals(APIConstants.partitionCoefficient) || 
				endpointKind.equals(APIConstants.waterSolubility))) {
			queryBlock.addRangeField(APIConstants.pH,pHMin,pHMax,"");
		} else if (haspHCondition && !removepHField) {
			System.out.println("Warning: pH condition not supported for "+endpointKind+". Non-null values ignored.");
		}
		
		// Endpoint-specific conditions
		if (endpointKind.equals(APIConstants.partitionCoefficient)) {
			queryBlock.addPartitionCoefficientFields();
		} else if (endpointKind.equals(APIConstants.waterSolubility)) {
			queryBlock.addWaterSolubilityFields();
		}
		
		return queryBlock;
	}
	
	/**
	 * Creates the Query object corresponding to the given options
	 * @return		The desired Query
	 */
	private Query generateQuery() {
		boolean hasPressureCondition = pressureMin!=null || pressureMax!=null;
		boolean hasTemperatureCondition = temperatureMin!=null || temperatureMax!=null;
		boolean haspHCondition = pHMin!=null || pHMax!=null;
		
		Query query = new Query(limit);
		QueryBlock queryBlock = generateQueryBlock(false,false,false);
		query.addPropertyBlock(queryBlock);
		
		if (hasPressureCondition && includeNullPressure) {
			query.addOperatorBlock("OR");
			QueryBlock nullPressureBlock = generateQueryBlock(true,false,false);
			query.addPropertyBlock(nullPressureBlock);
		}
		
		if (hasTemperatureCondition && includeNullTemperature) {
			query.addOperatorBlock("OR");
			QueryBlock nullTemperatureBlock = generateQueryBlock(false,true,false);
			query.addPropertyBlock(nullTemperatureBlock);
		}
		
		if (haspHCondition && includeNullpH) {
			query.addOperatorBlock("OR");
			QueryBlock nullpHBlock = generateQueryBlock(false,false,true);
			query.addPropertyBlock(nullpHBlock);
		}
		
		if (endpointKind.equals(APIConstants.henrysLawConstant) &&
				hasPressureCondition && includeNullPressure && hasTemperatureCondition && includeNullTemperature) {
			query.addOperatorBlock("OR");
			QueryBlock nullPressureandTemperatureBlock = generateQueryBlock(true,true,false);
			query.addPropertyBlock(nullPressureandTemperatureBlock);
		}
		
		if ((endpointKind.equals(APIConstants.waterSolubility) || endpointKind.equals(APIConstants.partitionCoefficient)) &&
				hasTemperatureCondition && includeNullTemperature && haspHCondition && includeNullpH) {
			query.addOperatorBlock("OR");
			QueryBlock nullTemperatureandpHBlock = generateQueryBlock(false,true,true);
			query.addPropertyBlock(nullTemperatureandpHBlock);
		}
		
		return query;
	}

	/**
	 * Downloads the results of the given query to the results database
	 * @param startFresh	True to rebuild the database from scratch, false otherwise
	 */
	public void runDownload(String databaseName,boolean startFresh, int maxSize) {
		List<QueryOptions> splitOptions = resize(maxSize);
		QueryHandler handler = new QueryHandler(1000,10);
		int counter = 0;
		for (QueryOptions options:splitOptions) {
			String unitString = options.includeAllUnits ? ", all units..." : " "+endpointUnits+"...";
			System.out.println("Querying "+endpointKind+" results from "+options.endpointMin+" to "+options.endpointMax+unitString);
			Query query = options.generateQuery();
			if (counter==0) {
				handler.downloadQueryResultsToDatabase(query,databaseName,startFresh);
			} else {
				handler.downloadQueryResultsToDatabase(query,databaseName,false);
			}
			counter++;
		}
	}
}
