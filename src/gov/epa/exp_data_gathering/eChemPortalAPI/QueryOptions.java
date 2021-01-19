package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;

/**
 * Defines options for an eChemPortal API query
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class QueryOptions {
	int limit = 5000; // Most efficient page limit in most tests
	String propertyName = null;
	int maxReliabilityLevel = 2; // Recommended
	String afterYear = null;
	String beforeYear = null;
	String endpointMin = null;
	String endpointMax = null;
	String endpointUnits = null;
	String pressureMin = null;
	String pressureMax = null;
	String pressureUnits = null;
	boolean includeNullPressure = false;
	String temperatureMin = null;
	String temperatureMax = null;
	String temperatureUnits = null;
	boolean includeNullTemperature = false;
	String pHMin = null;
	String pHMax = null;
	boolean includeNullpH = false;
	boolean includeOtherReliability = false;
	boolean includeAllGLPCompliances = false;
	boolean includeAllGuidelines = false;
	boolean includeAllUnits = false;
	
	// Default null constructor
	QueryOptions() { }
	
	public QueryOptions copy() {
		Gson gson = new GsonBuilder().create();
		String optionsJSON = gson.toJson(this);
		return gson.fromJson(optionsJSON,QueryOptions.class);
	}
	
	/**
	 * Creates a default set of options that will download all available records for a single property
	 * @param propertyName		Desired property from ExperimentalConstants
	 */
	QueryOptions(String propertyName) {
		this.propertyName = propertyName;
		if (propertyName.equals(ExperimentalConstants.strMeltingPoint) || propertyName.equals(ExperimentalConstants.strBoilingPoint) || 
				propertyName.equals(ExperimentalConstants.strFlashPoint)) {
			endpointMin = "0";
			endpointMax = "10000";
			includeAllUnits = true;
			pressureMin = "0";
			pressureUnits = ExperimentalConstants.str_pa;
			includeNullPressure = true;
		} else if (propertyName.equals(ExperimentalConstants.strDensity)) {
			endpointMin = "0";
			endpointMax = "1000";
			includeAllUnits = true;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
		} else if (propertyName.equals(ExperimentalConstants.strVaporPressure)) {
			endpointMin = "0";
			endpointMax = "5000000";
			includeAllUnits = true;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
		} else if (propertyName.equals(ExperimentalConstants.str_pKA)) {
			endpointMin = "-1000";
			endpointMax = "1000";
			includeAllUnits = true;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
		} else if (propertyName.equals(ExperimentalConstants.strLogKow)) {
			endpointMin = "-1000";
			endpointMax = "126000000000"; // Largest non-log Pow value in eChemPortal
			includeAllUnits = true;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
			pHMin = "-1000";
			includeNullpH = true;
		} else if (propertyName.equals(ExperimentalConstants.strWaterSolubility)) {
			endpointMin = "0";
			endpointMax = "5000";
			includeAllUnits = true;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
			pHMin = "-1000";
			includeNullpH = true;
		} else if (propertyName.equals(ExperimentalConstants.strHenrysLawConstant)) {
			this.propertyName = ExperimentalConstants.strHenrysLawConstant;
			endpointMin = String.valueOf(Integer.MIN_VALUE);
			includeAllUnits = true;
			pressureMin = "0";
			pressureUnits = ExperimentalConstants.str_pa;
			includeNullPressure = true;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
		}
	}
	
	public static List<QueryOptions> generateAllQueryOptions() {
		List<QueryOptions> allOptions = new ArrayList<QueryOptions>();
		allOptions.add(new QueryOptions(ExperimentalConstants.strMeltingPoint));
		allOptions.add(new QueryOptions(ExperimentalConstants.strBoilingPoint));
		allOptions.add(new QueryOptions(ExperimentalConstants.strFlashPoint));
		allOptions.add(new QueryOptions(ExperimentalConstants.strDensity));
		allOptions.add(new QueryOptions(ExperimentalConstants.strVaporPressure));
		allOptions.add(new QueryOptions(ExperimentalConstants.strWaterSolubility));
		allOptions.add(new QueryOptions(ExperimentalConstants.strLogKow));
		allOptions.add(new QueryOptions(ExperimentalConstants.str_pKA));
		allOptions.add(new QueryOptions(ExperimentalConstants.strHenrysLawConstant));
		return allOptions;
	}
	
	/**
	 * Splits the QueryOptions object at the range midpoint to reduce query size
	 * @return		Two QueryOptions objects that jointly cover the same range as the original
	 */
	private List<QueryOptions> generateSplitOptions() {
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
	 * Merges the endpoint range of another QueryOptions object into the current QueryOptions
	 * @param options	The QueryOptions object to merge
	 */
	protected boolean mergeOptions(QueryOptions options) {
		if (this.propertyName.equals(options.propertyName)) {
			if (this.endpointMax.equals(options.endpointMin)) {
				this.endpointMax = options.endpointMax;
				return true;
			} else if (this.endpointMin.equals(options.endpointMax)) {
				this.endpointMin = options.endpointMin;
				return true;
			} else if (this.beforeYear.equals(options.afterYear)) {
				this.beforeYear = options.beforeYear;
				return true;
			} else if (this.afterYear.equals(options.beforeYear)) {
				this.afterYear = options.afterYear;
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
		return size;
	}
	
	/**
	 * Recursively splits a vector of QueryOptions until all queries have size < 10000
	 * @param options	Vector of QueryOptions to be resized
	 * @return			Vector of QueryOptions of permitted size
	 */
	private static List<QueryOptions> resizeAll(List<QueryOptions> options) {
		List<QueryOptions> newOptions = new ArrayList<QueryOptions>();
		for (QueryOptions o:options) {
			int size = o.getQueryMaxSize();
			if (size >= 10000) {
				List<QueryOptions> splitOptions = o.generateSplitOptions();
				splitOptions = resizeAll(splitOptions);
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
	public List<QueryOptions> resize() {
		List<QueryOptions> options = new ArrayList<QueryOptions>();
		options.add(this);
		if (getQueryMaxSize() >= 10000) {
			System.out.println(this.propertyName+" query too large. Resizing...");
			options = resizeAll(options);
			System.out.println("Split into "+options.size()+" queries. Optimizing...");
			ListIterator<QueryOptions> it = options.listIterator();
			int lastSize = 0;
			while (it.hasNext()) {
				QueryOptions currentOptions = it.next();
				int size = currentOptions.getQueryMaxSize();
				if (it.previousIndex() > 0 && size + lastSize < 10000) {
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
	public QueryBlock generateQueryBlock(boolean removePressureField,boolean removeTemperatureField,boolean removepHField) {
		String endpointKind = getEndpointKind(propertyName);
		QueryBlock queryBlock = new QueryBlock(endpointKind);
		queryBlock.addInfoTypeField();
		queryBlock.addReliabilityField(maxReliabilityLevel,includeOtherReliability);
		
		// Endpoint value
		if (includeAllUnits) {
			queryBlock.addAllUnitEndpointField(endpointMin, endpointMax);
		} else {
			queryBlock.addEndpointField(endpointMin,endpointMax,endpointUnits);
		}
		
		// Pressure condition
		boolean hasPressureCondition = pressureMin!=null || pressureMax!=null;
		if (!removePressureField && hasPressureCondition && (endpointKind.equals(EChemPortalAPIConstants.meltingPoint) || 
				endpointKind.equals(EChemPortalAPIConstants.boilingPoint) || 
				endpointKind.equals(EChemPortalAPIConstants.flashPoint) ||
				endpointKind.equals(EChemPortalAPIConstants.henrysLawConstant))) {
			queryBlock.addAtmPressureField(pressureMin,pressureMax,pressureUnits);
		} else if (hasPressureCondition && !removePressureField) {
			System.out.println("Warning: Pressure condition not supported for "+propertyName+". Non-null values ignored.");
		}
		
		// Temperature condition
		boolean hasTemperatureCondition = temperatureMin!=null || temperatureMax!=null;
		if (!removeTemperatureField && hasTemperatureCondition && (endpointKind.equals("Density") ||
				endpointKind.equals(EChemPortalAPIConstants.vaporPressure) ||
				endpointKind.equals(EChemPortalAPIConstants.partitionCoefficient) || 
				endpointKind.equals(EChemPortalAPIConstants.waterSolubility) ||
				endpointKind.equals(EChemPortalAPIConstants.dissociationConstant) ||
				endpointKind.equals(EChemPortalAPIConstants.henrysLawConstant))) {
			queryBlock.addTemperatureField(temperatureMin,temperatureMax,temperatureUnits);
		} else if (hasTemperatureCondition && !removeTemperatureField) {
			System.out.println("Warning: Temperature condition not supported for "+propertyName+". Non-null values ignored.");
		}
		
		// pH condition
		boolean haspHCondition = pHMin!=null || pHMax!=null;
		if (!removepHField && haspHCondition && (endpointKind.equals(EChemPortalAPIConstants.partitionCoefficient) || 
				endpointKind.equals(EChemPortalAPIConstants.waterSolubility))) {
			queryBlock.addpHField(pHMin,pHMax);
		} else if (haspHCondition && !removepHField) {
			System.out.println("Warning: pH condition not supported for "+propertyName+". Non-null values ignored.");
		}
		
		// Endpoint-specific conditions
		if (endpointKind.equals(EChemPortalAPIConstants.partitionCoefficient)) {
			queryBlock.addPartitionCoefficientFields();
		} else if (endpointKind.equals(EChemPortalAPIConstants.waterSolubility)) {
			queryBlock.addWaterSolubilityFields();
		}
		
		return queryBlock;
	}
	
	/**
	 * Creates the Query object corresponding to the given options
	 * @return		The desired Query
	 */
	public Query generateQuery() {
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
		
		if (propertyName.equals(ExperimentalConstants.strHenrysLawConstant) &&
				hasPressureCondition && includeNullPressure && hasTemperatureCondition && includeNullTemperature) {
			query.addOperatorBlock("OR");
			QueryBlock nullPressureandTemperatureBlock = generateQueryBlock(true,true,false);
			query.addPropertyBlock(nullPressureandTemperatureBlock);
		}
		
		if ((propertyName.equals(ExperimentalConstants.strWaterSolubility) || propertyName.equals(ExperimentalConstants.strLogKow)) &&
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
	public void runDownload(String databaseName,boolean startFresh) {
		List<QueryOptions> splitOptions = resize();
		QueryHandler handler = new QueryHandler(1000,10);
		int counter = 0;
		for (QueryOptions options:splitOptions) {
			System.out.println("Querying "+propertyName+" results from "+options.endpointMin+" to "+options.endpointMax+" "+endpointUnits+"...");
			Query query = options.generateQuery();
			if (counter==0) {
				handler.downloadQueryResultsToDatabase(query,databaseName,startFresh);
			} else {
				handler.downloadQueryResultsToDatabase(query,databaseName,false);
			}
			counter++;
		}
	}
	
	/**
	 * Translates our endpoint identifiers to eChemPortal's
	 * @param propertyName	Endpoint to query from ExperimentalConstants
	 * @return				eChemPortal's corresponding endpoint identifier
	 */
	private static String getEndpointKind(String propertyName) {
		String endpointKind = "";
		switch (propertyName) {
		case ExperimentalConstants.strMeltingPoint:
			endpointKind = EChemPortalAPIConstants.meltingPoint;
			break;
		case ExperimentalConstants.strBoilingPoint:
			endpointKind = EChemPortalAPIConstants.boilingPoint;
			break;
		case ExperimentalConstants.strFlashPoint:
			endpointKind = EChemPortalAPIConstants.flashPoint;
			break;
		case ExperimentalConstants.strDensity:
			endpointKind = EChemPortalAPIConstants.density;
			break;
		case ExperimentalConstants.strVaporPressure:
			endpointKind = EChemPortalAPIConstants.vaporPressure;
			break;
		case ExperimentalConstants.strLogKow:
			endpointKind = EChemPortalAPIConstants.partitionCoefficient;
			break;
		case ExperimentalConstants.strWaterSolubility:
			endpointKind = EChemPortalAPIConstants.waterSolubility;
			break;
		case ExperimentalConstants.str_pKA:
			endpointKind = EChemPortalAPIConstants.dissociationConstant;
			break;
		case ExperimentalConstants.strHenrysLawConstant:
			endpointKind = EChemPortalAPIConstants.henrysLawConstant;
			break;
		}
		return endpointKind;
	}
}
