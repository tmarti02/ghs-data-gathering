package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.util.Iterator;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

public class QueryOptions {
	int limit = 5000;
	String propertyName = null;
	int maxReliabilityLevel = 2;
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
	
	static final double absMin = -2147483648.0;
	static final double absMax = 2147483647.0;
	
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
	
	QueryOptions(String propertyName) {
		this.propertyName = propertyName;
		if (propertyName.equals(ExperimentalConstants.strMeltingPoint) || propertyName.equals(ExperimentalConstants.strBoilingPoint) || 
				propertyName.equals(ExperimentalConstants.strFlashPoint)) {
			endpointMin = "0";
			endpointUnits = ExperimentalConstants.str_K;
			pressureMin = "0";
			pressureUnits = ExperimentalConstants.str_pa;
			includeNullPressure = true;
		} else if (propertyName.equals(ExperimentalConstants.strDensity)) {
			endpointMin = "0";
			endpointUnits = ExperimentalConstants.str_g_cm3;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
		} else if (propertyName.equals(ExperimentalConstants.strVaporPressure)) {
			endpointMin = "0";
			endpointUnits = ExperimentalConstants.str_pa;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
		} else if (propertyName.equals(ExperimentalConstants.str_pKA)) {
			endpointMin = String.valueOf(absMin);
			endpointUnits = "";
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
		} else if (propertyName.equals(ExperimentalConstants.strLogKow)) {
			endpointMin = String.valueOf(absMin);
			endpointUnits = "";
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
			pHMin = String.valueOf(absMin);
			includeNullpH = true;
		} else if (propertyName.equals(ExperimentalConstants.strWaterSolubility)) {
			endpointMin = "0";
			endpointUnits = ExperimentalConstants.str_g_L;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
			pHMin = String.valueOf(absMin);
			includeNullpH = true;
		} else if (propertyName.equals(ExperimentalConstants.strHenrysLawConstant)) {
			endpointMin = String.valueOf(absMin);
			endpointUnits = ExperimentalConstants.str_atm_m3_mol;
			pressureMin = "0";
			pressureUnits = ExperimentalConstants.str_pa;
			includeNullTemperature = true;
			temperatureMin = "0";
			temperatureUnits = ExperimentalConstants.str_K;
			includeNullTemperature = true;
		}
	}
	
	private Vector<QueryOptions> generateSplitOptions() {
		Vector<QueryOptions> splitOptions = new Vector<QueryOptions>();
		QueryOptions lowerSplitOptions = new QueryOptions(this);
		QueryOptions upperSplitOptions = new QueryOptions(this);
		double min = endpointMin==null ? absMin : Double.parseDouble(endpointMin);
		double max = endpointMax==null ? absMax : Double.parseDouble(endpointMax);
		double midpoint = Math.floor(min + (max - min)/2.0);
		lowerSplitOptions.endpointMax = String.valueOf(midpoint);
		upperSplitOptions.endpointMin = String.valueOf(midpoint);
		splitOptions.add(lowerSplitOptions);
		splitOptions.add(upperSplitOptions);
		System.out.println("Query from "+min+" to "+max+" split at "+midpoint+".");
		return splitOptions;
	}
	
	private int getQueryMaxSize() {
		Query query = new Query(limit);
		QueryBlock queryBlock = generateQueryBlock(true,true,true);
		query.addPropertyBlock(queryBlock);
		QueryHandler handler = new QueryHandler();
		int size = handler.getQuerySize(query);
		return size;
	}
	
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
	
	public Vector<QueryOptions> resize() {
		Vector<QueryOptions> options = new Vector<QueryOptions>();
		options.add(this);
		if (getQueryMaxSize() >= 10000) {
			System.out.println("Query too large. Resizing...");
			options = resizeAll(options);
			System.out.println("Split into "+options.size()+" queries.");
			Iterator<QueryOptions> it = options.iterator();
			Vector<Integer> sizes = new Vector<Integer>();
			while (it.hasNext()) {
				QueryOptions o = it.next();
				int size = o.getQueryMaxSize();
				if (size==0) {
					it.remove();
				} else {
					sizes.add(size);
				}
			}
			int convergesTo = 0;
			int convergesAt = sizes.size();
			for (int i = 0; i < sizes.size(); i++) {
				if (!sizes.get(i).equals(convergesTo)) {
					convergesTo = sizes.get(i);
					convergesAt = i;
				}
			}
			options = new Vector<QueryOptions>(options.subList(0, convergesAt+1));
			System.out.println("Removed empty & convergent queries. "+options.size()+" queries to run.");
		} else {
			System.out.println("No resizing needed.");
		}
		return options;
	}
	
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
		
		// Endpoint range
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
		
		// Endpoint-specific necessary fields
		if (endpointKind.equals("Partition")) {
			queryBlock.addPartitionCoefficientFields();
		} else if (endpointKind.equals("WaterSolubility")) {
			queryBlock.addWaterSolubilityFields();
		}
		
		return queryBlock;
	}
	
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

	public void runDownload(boolean startFresh) {
		Vector<QueryOptions> splitOptions = resize();
		QueryHandler handler = new QueryHandler();
		Query query = splitOptions.get(0).generateQuery();
		handler.downloadQueryResultsToDatabase(query,startFresh);
		for (int i = 1; i < splitOptions.size(); i++) {
			query = splitOptions.get(i).generateQuery();
			handler.downloadQueryResultsToDatabase(query,false);
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
