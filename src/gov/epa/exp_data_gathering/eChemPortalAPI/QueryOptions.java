package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.util.Iterator;
import java.util.Vector;

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
	
	// Default null constructor
	QueryOptions() {
		limit = 5000;
		propertyName = null;
		maxReliabilityLevel = 2;
		endpointMin = null;
		endpointMax = null;
		endpointUnits = null;
		pressureMin = null;
		pressureMax = null;
		pressureUnits = null;
		includeNullPressure = false;
		temperatureMin = null;
		temperatureMax = null;
		temperatureUnits = null;
		includeNullTemperature = false;
		pHMin = null;
		pHMax = null;
		includeNullpH = false;
	}
	
	// Duplicate constructor
	QueryOptions(QueryOptions options) {
		limit = options.limit;
		propertyName = options.propertyName;
		maxReliabilityLevel = options.maxReliabilityLevel;
		endpointMin = options.endpointMin;
		endpointMax = options.endpointMax;
		endpointUnits = options.endpointUnits;
		pressureMin = options.pressureMin;
		pressureMax = options.pressureMax;
		pressureUnits = options.pressureUnits;
		includeNullPressure = options.includeNullPressure;
		temperatureMin = options.temperatureMin;
		temperatureMax = options.temperatureMax;
		temperatureUnits = options.temperatureUnits;
		includeNullTemperature = options.includeNullTemperature;
		pHMin = options.pHMin;
		pHMax = options.pHMax;
		includeNullpH = options.includeNullpH;
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
			endpointUnits = ExperimentalConstants.str_K;
			pressureMin = "0";
			pressureUnits = ExperimentalConstants.str_pa;
			includeNullPressure = true;
		} else if (propertyName.equals(ExperimentalConstants.strDensity)) {
			endpointMin = "0";
			endpointMax = "1000";
			endpointUnits = ExperimentalConstants.str_g_cm3;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
		} else if (propertyName.equals(ExperimentalConstants.strVaporPressure)) {
			endpointMin = "0";
			endpointMax = "5000000";
			endpointUnits = ExperimentalConstants.str_pa;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
		} else if (propertyName.equals(ExperimentalConstants.str_pKA)) {
			endpointMin = "-1000";
			endpointMax = "1000";
			endpointUnits = "";
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
		} else if (propertyName.equals(ExperimentalConstants.strLogKow)) {
			endpointMin = "-1000";
			endpointMax = "1000";
			endpointUnits = "";
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
			pHMin = "-1000";
			includeNullpH = true;
		} else if (propertyName.equals(ExperimentalConstants.strWaterSolubility)) {
			endpointMin = "0";
			endpointMax = "5000";
			endpointUnits = ExperimentalConstants.str_g_L;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
			pHMin = "-1000";
			includeNullpH = true;
		} else if (propertyName.equals(ExperimentalConstants.strWaterSolubility+"_g_cm3")) {
			this.propertyName = ExperimentalConstants.strWaterSolubility;
			endpointMin = "0";
			endpointMax = "1000";
			endpointUnits = ExperimentalConstants.str_g_cm3;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
			pHMin = "-1000";
			includeNullpH = true;
		} else if (propertyName.equals(ExperimentalConstants.strWaterSolubility+"_kg_m3")) {
			this.propertyName = ExperimentalConstants.strWaterSolubility;
			endpointMin = "0";
			endpointMax = "500";
			endpointUnits = ExperimentalConstants.str_kg_m3;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
			pHMin = "-1000";
			includeNullpH = true;
		} else if (propertyName.equals(ExperimentalConstants.strWaterSolubility+"_ppb")) {
			this.propertyName = ExperimentalConstants.strWaterSolubility;
			endpointMin = "0";
			endpointMax = "2000";
			endpointUnits = ExperimentalConstants.str_ppb;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
			pHMin = "-1000";
			includeNullpH = true;
		} else if (propertyName.equals(ExperimentalConstants.strWaterSolubility+"_%v")) {
			this.propertyName = ExperimentalConstants.strWaterSolubility;
			endpointMin = "0";
			endpointMax = "500";
			endpointUnits = ExperimentalConstants.str_pctVol;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
			pHMin = "-1000";
			includeNullpH = true;
		} else if (propertyName.equals(ExperimentalConstants.strHenrysLawConstant)) {
			this.propertyName = ExperimentalConstants.strHenrysLawConstant;
			endpointMin = String.valueOf(Integer.MIN_VALUE);
			endpointUnits = ExperimentalConstants.str_Pa_m3_mol;
			pressureMin = "0";
			pressureUnits = ExperimentalConstants.str_pa;
			includeNullPressure = true;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
		} else if (propertyName.equals(ExperimentalConstants.strHenrysLawConstant+"_dimensionless")) {
			this.propertyName = ExperimentalConstants.strHenrysLawConstant;
			endpointMin = String.valueOf(Integer.MIN_VALUE);
			endpointUnits = ExperimentalConstants.str_dimensionless_H;
			pressureMin = "0";
			pressureUnits = ExperimentalConstants.str_pa;
			includeNullPressure = true;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
		} else if (propertyName.equals(ExperimentalConstants.strHenrysLawConstant+"_dimensionless_vol")) {
			this.propertyName = ExperimentalConstants.strHenrysLawConstant;
			endpointMin = String.valueOf(Integer.MIN_VALUE);
			endpointUnits = ExperimentalConstants.str_dimensionless_H_vol;
			pressureMin = "0";
			pressureUnits = ExperimentalConstants.str_pa;
			includeNullPressure = true;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
		} else if (propertyName.equals(ExperimentalConstants.strHenrysLawConstant+"_atm")) {
			this.propertyName = ExperimentalConstants.strHenrysLawConstant;
			endpointMin = String.valueOf(Integer.MIN_VALUE);
			endpointUnits = ExperimentalConstants.str_atm;
			pressureMin = "0";
			pressureUnits = ExperimentalConstants.str_pa;
			includeNullPressure = true;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
		}
	}
	
	public static Vector<QueryOptions> generateAllQueryOptions() {
		Vector<QueryOptions> allOptions = new Vector<QueryOptions>();
		allOptions.add(new QueryOptions(ExperimentalConstants.strMeltingPoint));
		allOptions.add(new QueryOptions(ExperimentalConstants.strBoilingPoint));
		allOptions.add(new QueryOptions(ExperimentalConstants.strFlashPoint));
		allOptions.add(new QueryOptions(ExperimentalConstants.strDensity));
		allOptions.add(new QueryOptions(ExperimentalConstants.strVaporPressure));
		allOptions.add(new QueryOptions(ExperimentalConstants.strWaterSolubility));
		allOptions.add(new QueryOptions(ExperimentalConstants.strWaterSolubility+"_g_cm3"));
		allOptions.add(new QueryOptions(ExperimentalConstants.strWaterSolubility+"_kg_m3"));
		allOptions.add(new QueryOptions(ExperimentalConstants.strWaterSolubility+"_ppb"));
		allOptions.add(new QueryOptions(ExperimentalConstants.strWaterSolubility+"_%v"));
		allOptions.add(new QueryOptions(ExperimentalConstants.strLogKow));
		allOptions.add(new QueryOptions(ExperimentalConstants.str_pKA));
		allOptions.add(new QueryOptions(ExperimentalConstants.strHenrysLawConstant));
		allOptions.add(new QueryOptions(ExperimentalConstants.strHenrysLawConstant+"_dimensionless"));
		allOptions.add(new QueryOptions(ExperimentalConstants.strHenrysLawConstant+"_dimensionless_vol"));
		allOptions.add(new QueryOptions(ExperimentalConstants.strHenrysLawConstant+"_atm"));
		return allOptions;
	}
	
	/**
	 * Splits the QueryOptions object at the range midpoint to reduce query size
	 * @return		Two QueryOptions objects that jointly cover the same range as the original
	 */
	private Vector<QueryOptions> generateSplitOptions() {
		Vector<QueryOptions> splitOptions = new Vector<QueryOptions>();
		QueryOptions lowerSplitOptions = new QueryOptions(this);
		QueryOptions upperSplitOptions = new QueryOptions(this);
		double min = endpointMin==null ? Integer.MIN_VALUE : Double.parseDouble(endpointMin);
		double max = endpointMax==null ? Integer.MAX_VALUE : Double.parseDouble(endpointMax);
		double midpoint = min + (max - min)/2.0;
		lowerSplitOptions.endpointMax = String.valueOf(midpoint);
		upperSplitOptions.endpointMin = String.valueOf(midpoint);
		splitOptions.add(lowerSplitOptions);
		splitOptions.add(upperSplitOptions);
		System.out.println("Query from "+min+" to "+max+" split at "+midpoint+".");
		return splitOptions;
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
	private static Vector<QueryOptions> resizeAll(Vector<QueryOptions> options) {
		Vector<QueryOptions> newOptions = new Vector<QueryOptions>();
		for (QueryOptions o:options) {
			int size = o.getQueryMaxSize();
			if (size >= 10000) {
				Vector<QueryOptions> splitOptions = o.generateSplitOptions();
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
	public Vector<QueryOptions> resize() {
		Vector<QueryOptions> options = new Vector<QueryOptions>();
		options.add(this);
		if (getQueryMaxSize() >= 10000) {
			System.out.println("Query too large. Resizing...");
			options = resizeAll(options);
			System.out.println("Split into "+options.size()+" queries.");
			Iterator<QueryOptions> it = options.iterator();
			// eChemPortal API handles > in a silly way that results in lots of duplication
			// This loop removes queries that return 1) no results, or 2) only duplicate results
			// There will still be some duplication from < entries not handled by this - eliminated in parseResultsInDatabase()
			int convergesTo = 0;
			int convergesAt = -1;
			int i = 0;
			while (it.hasNext()) {
				QueryOptions o = it.next();
				int size = o.getQueryMaxSize();
				if (size==0) {
					it.remove();
				} else {
					if (size!=convergesTo) {
						convergesTo = size;
						convergesAt = i;
					}
					i++;
				}
			}
			options = new Vector<QueryOptions>(options.subList(0, convergesAt+1));
			System.out.println("Removed empty & duplicate queries; "+options.size()+" queries to run.");
		} else {
			System.out.println("No resizing needed.");
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
		queryBlock.addReliabilityField(maxReliabilityLevel);
		
		// Disambiguates potential unit collisions between density and solubility, pressure and HLC
		if (endpointUnits.equals(ExperimentalConstants.str_g_L) || endpointUnits.equals(ExperimentalConstants.str_g_cm3) ||
				endpointUnits.equals(ExperimentalConstants.str_kg_m3)) {
			if (endpointKind.equals("Density")) {
				endpointUnits += "_density";
			} else if (endpointKind.equals("WaterSolubility")) {
				endpointUnits += "_solubility";
			}
		} else if (endpointUnits.equals(ExperimentalConstants.str_atm)) {
			if (endpointKind.equals("Vapour")) {
				endpointUnits += "_VP";
			} else if (endpointKind.equals("HenrysLawConstant")) {
				endpointUnits += "_H";
			}
		}
		
		// Endpoint value
		queryBlock.addEndpointField(endpointMin,endpointMax,endpointUnits);
		
		// Pressure condition
		boolean hasPressureCondition = pressureMin!=null || pressureMax!=null;
		if (!removePressureField && hasPressureCondition && (endpointKind.equals("Melting") || endpointKind.equals("BoilingPoint") || endpointKind.equals("FlashPoint") ||
				endpointKind.equals("HenrysLawConstant"))) {
			queryBlock.addAtmPressureField(pressureMin,pressureMax,pressureUnits);
		} else if (hasPressureCondition && !removePressureField) {
			System.out.println("Warning: Pressure condition not supported for "+propertyName+". Non-null values ignored.");
		}
		
		// Temperature condition
		boolean hasTemperatureCondition = temperatureMin!=null || temperatureMax!=null;
		if (!removeTemperatureField && hasTemperatureCondition && (endpointKind.equals("Density") || endpointKind.equals("Vapour") || endpointKind.equals("Partition") || 
				endpointKind.equals("WaterSolubility") || endpointKind.equals("DissociationConstant") || endpointKind.equals("HenrysLawConstant"))) {
			queryBlock.addTemperatureField(temperatureMin,temperatureMax,temperatureUnits);
		} else if (hasTemperatureCondition && !removeTemperatureField) {
			System.out.println("Warning: Temperature condition not supported for "+propertyName+". Non-null values ignored.");
		}
		
		// pH condition
		boolean haspHCondition = pHMin!=null || pHMax!=null;
		if (!removepHField && haspHCondition && (endpointKind.equals("Partition") || endpointKind.equals("WaterSolubility"))) {
			queryBlock.addpHField(pHMin,pHMax);
		} else if (haspHCondition && !removepHField) {
			System.out.println("Warning: pH condition not supported for "+propertyName+". Non-null values ignored.");
		}
		
		// Endpoint-specific conditions
		if (endpointKind.equals("Partition")) {
			queryBlock.addPartitionCoefficientFields();
		} else if (endpointKind.equals("WaterSolubility")) {
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
	public void runDownload(boolean startFresh) {
		System.out.println("Querying "+propertyName+" results.");
		Vector<QueryOptions> splitOptions = resize();
		QueryHandler handler = new QueryHandler(1000,10);
		int counter = 0;
		for (QueryOptions options:splitOptions) {
			Query query = options.generateQuery();
			if (counter==0) {
				handler.downloadQueryResultsToDatabase(query,startFresh);
			} else {
				handler.downloadQueryResultsToDatabase(query,false);
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
			endpointKind = "Melting";
			break;
		case ExperimentalConstants.strBoilingPoint:
			endpointKind = "BoilingPoint";
			break;
		case ExperimentalConstants.strFlashPoint:
			endpointKind = "FlashPoint";
			break;
		case ExperimentalConstants.strDensity:
			endpointKind = "Density";
			break;
		case ExperimentalConstants.strVaporPressure:
			endpointKind = "Vapour";
			break;
		case ExperimentalConstants.strLogKow:
			endpointKind = "Partition";
			break;
		case ExperimentalConstants.strWaterSolubility:
			endpointKind = "WaterSolubility";
			break;
		case ExperimentalConstants.str_pKA:
			endpointKind = "DissociationConstant";
			break;
		case ExperimentalConstants.strHenrysLawConstant:
			endpointKind = "HenrysLawConstant";
			break;
		}
		return endpointKind;
	}
}
