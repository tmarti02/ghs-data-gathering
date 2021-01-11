package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ToxQueryOptions extends QueryOptions {
	List<String> testTypes;
	boolean includeOtherTestType;
	boolean includeAllTestTypes;
	List<String> species;
	boolean includeOtherSpecies;
	boolean includeAllSpecies;
	List<String> strains;
	boolean includeOtherStrain;
	boolean includeAllStrains;
	List<String> routesOfAdministration;
	boolean includeOtherRoute;
	boolean includeAllRoutes;
	List<String> inhalationTypes;
	boolean includeOtherInhalationType;
	boolean includeAllInhalationTypes;
	List<String> coverageTypes;
	boolean includeOtherCoverageType;
	boolean includeAllCoverageTypes;
	List<String> doseDescriptors;
	boolean includeOtherDoseDescriptor;
	boolean includeAllDoseDescriptors;
	boolean includeAllEndpointTypes;
	
	// Default null constructor
	ToxQueryOptions() {
		testTypes = new ArrayList<String>();
		species = new ArrayList<String>();
		strains = new ArrayList<String>();
		routesOfAdministration = new ArrayList<String>();
		inhalationTypes = new ArrayList<String>();
		coverageTypes = new ArrayList<String>();
		doseDescriptors = new ArrayList<String>();
	}
	
	// Duplicate constructor
	ToxQueryOptions(ToxQueryOptions options) {
		limit = options.limit;
		propertyName = options.propertyName;
		maxReliabilityLevel = options.maxReliabilityLevel;
		afterYear = options.afterYear;
		endpointMin = options.endpointMin;
		endpointMax = options.endpointMax;
		endpointUnits = options.endpointUnits;
		testTypes = options.testTypes;
		species = options.species;
		strains = options.strains;
		routesOfAdministration = options.routesOfAdministration;
		inhalationTypes = options.inhalationTypes;
		coverageTypes = options.coverageTypes;
		doseDescriptors = options.doseDescriptors;
		includeOtherTestType = options.includeOtherTestType;
		includeOtherSpecies = options.includeOtherSpecies;
		includeOtherStrain = options.includeOtherStrain;
		includeOtherRoute = options.includeOtherRoute;
		includeOtherInhalationType = options.includeOtherInhalationType;
		includeOtherCoverageType = options.includeOtherCoverageType;
		includeOtherDoseDescriptor = options.includeOtherDoseDescriptor;
		includeAllTestTypes = options.includeAllTestTypes;
		includeAllSpecies = options.includeAllSpecies;
		includeAllStrains = options.includeAllStrains;
		includeAllRoutes = options.includeAllRoutes;
		includeAllInhalationTypes = options.includeAllInhalationTypes;
		includeAllCoverageTypes = options.includeAllCoverageTypes;
		includeAllGLPCompliances = options.includeAllGLPCompliances;
		includeAllGuidelines = options.includeAllGuidelines;
		includeAllDoseDescriptors = options.includeAllDoseDescriptors;
		includeAllEndpointTypes = options.includeAllEndpointTypes;
	}
	
	public static List<Query> generateInhalationLC50Queries() {
		List<Query> queries = new ArrayList<Query>();
		String[] units = {"mg/L air","mg/L air (nominal)","mg/L air (analytical)","mg/m^3 air","mg/m^3 air (nominal)","mg/m^3 air (analytical)","ppm"};
		String[] speciesSimple = {"rat","mouse","guinea pig","rabbit"};
		String[] lc50 = {"LC50"};
		
		for (String unit:units) {
			ToxQueryOptions simple = new ToxQueryOptions();
			simple.propertyName = "AcuteToxicityInhalation";
			simple.endpointMin = "0";
			simple.endpointMax = String.valueOf(Integer.MAX_VALUE);
			simple.endpointUnits = unit;
			simple.species = Arrays.asList(speciesSimple);
			simple.includeAllRoutes = true;
			simple.doseDescriptors = Arrays.asList(lc50);
			
			ToxQueryOptions complex = new ToxQueryOptions();
			complex.propertyName = "AcuteToxicityInhalation";
			complex.endpointMin = "0";
			complex.endpointMax = String.valueOf(Integer.MAX_VALUE);
			complex.endpointUnits = unit;
			complex.includeAllTestTypes = true;
			complex.includeAllSpecies = true;
			complex.includeAllStrains = true;
			complex.includeAllInhalationTypes = true;
			complex.includeAllRoutes = true;
			complex.doseDescriptors = Arrays.asList(lc50);
			
			Query query = new Query(5000);
			ToxQueryBlock simpleBlock = simple.generateToxQueryBlock();
			ToxQueryBlock complexBlock = complex.generateToxQueryBlock();
			query.addPropertyBlock(complexBlock);
			query.addOperatorBlock("OR");
			query.addPropertyBlock(simpleBlock);
			queries.add(query);
		}
		
		return queries;
	}
	
	public static List<Query> generateRepeatedDoseOralQueries() {
		List<Query> queries = new ArrayList<Query>();
		String[] units = {"mg/kg bw/day (nominal)","mg/kg bw/day (actual dose received)","mg/kg diet","mg/L drinking water","mg/kg bw (total dose)","ppm"};
		for (String unit:units) {
			ToxQueryOptions options = new ToxQueryOptions();
			options.propertyName = "RepeatedDoseToxicityOral";
			options.endpointMin = "0";
			options.endpointMax = "1000000";
			options.endpointUnits = unit;
			options.maxReliabilityLevel = 4;
			options.includeOtherReliability = true;
			options.afterYear = "1960";
			options.includeAllGuidelines = true;
			options.includeAllGLPCompliances = true;
			options.includeAllEndpointTypes = true;
			options.includeAllSpecies = true;
			options.includeAllStrains = true;
			options.includeAllRoutes = true;
			options.includeAllDoseDescriptors = true;
			
			Query query = new Query(5000);
			ToxQueryBlock toxQueryBlock = options.generateToxQueryBlock();
			query.addPropertyBlock(toxQueryBlock);
			queries.add(query);
		}
		return queries;
	}
	
	/**
	 * Splits the QueryOptions object at the range midpoint to reduce query size
	 * @return		Two QueryOptions objects that jointly cover the same range as the original
	 */
	protected List<ToxQueryOptions> generateSplitOptions() {
		List<ToxQueryOptions> splitOptions = new ArrayList<ToxQueryOptions>();
		ToxQueryOptions lowerSplitOptions = new ToxQueryOptions(this);
		ToxQueryOptions upperSplitOptions = new ToxQueryOptions(this);
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
	 * Gets the maximum size of the query corresponding to the given options (i.e. with no conditions specified)
	 * @return
	 */
	private int getQueryMaxSize() {
		Query query = new Query(limit);
		QueryBlock queryBlock = generateToxQueryBlock();
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
	private static List<ToxQueryOptions> resizeAll(List<ToxQueryOptions> options) {
		List<ToxQueryOptions> newOptions = new ArrayList<ToxQueryOptions>();
		for (ToxQueryOptions o:options) {
			int size = o.getQueryMaxSize();
			if (size >= 10000) {
				List<ToxQueryOptions> splitOptions = o.generateSplitOptions();
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
	public List<ToxQueryOptions> toxResize() {
		List<ToxQueryOptions> options = new ArrayList<ToxQueryOptions>();
		options.add(this);
		if (getQueryMaxSize() >= 10000) {
			System.out.println(this.propertyName+" query too large. Resizing...");
			options = resizeAll(options);
			System.out.println("Split into "+options.size()+" queries. Optimizing...");
			ListIterator<ToxQueryOptions> it = options.listIterator();
			int lastSize = 0;
			while (it.hasNext()) {
				ToxQueryOptions currentOptions = it.next();
				int size = currentOptions.getQueryMaxSize();
				if (it.previousIndex() > 0 && size + lastSize < 10000) {
					it.remove();
					ToxQueryOptions lastOptions = it.previous();
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
	
	public ToxQueryBlock generateToxQueryBlock() {
		String endpointKind = propertyName;
		ToxQueryBlock toxQueryBlock = new ToxQueryBlock(endpointKind);
		toxQueryBlock.addInfoTypeField();
		toxQueryBlock.addReliabilityField(maxReliabilityLevel,includeOtherReliability);
		
		toxQueryBlock.addEffectLevelField(endpointMin,endpointMax,endpointUnits);

		if (includeAllTestTypes) {
			toxQueryBlock.addAllTestTypeField();
		} else if (testTypes!=null && !testTypes.isEmpty()) {
			toxQueryBlock.addTestTypeField(testTypes,includeOtherTestType);
		}

		if (includeAllSpecies) {
			toxQueryBlock.addAllSpeciesField();
		} else if (species!=null && !species.isEmpty()) {
			toxQueryBlock.addSpeciesField(species,includeOtherSpecies);
		}

		if (includeAllStrains) {
			toxQueryBlock.addAllStrainField();
		} else if (strains!=null && !strains.isEmpty()) {
			toxQueryBlock.addStrainField(strains,includeOtherStrain);
		}

		if (includeAllRoutes) {
			toxQueryBlock.addAllAdministrationRouteField();
		} else if (routesOfAdministration!=null && !routesOfAdministration.isEmpty()) {
			toxQueryBlock.addAdministrationRouteField(routesOfAdministration,includeOtherRoute);
		}

		if (includeAllInhalationTypes) {
			toxQueryBlock.addAllInhalationTypeField();
		} else if (inhalationTypes!=null && !inhalationTypes.isEmpty()) {
			toxQueryBlock.addInhalationTypeField(inhalationTypes,includeOtherInhalationType);
		}
		
		if (includeAllCoverageTypes) {
			toxQueryBlock.addAllCoverageTypeField();
		} else if (coverageTypes!=null && !coverageTypes.isEmpty()) {
			toxQueryBlock.addCoverageTypeField(coverageTypes,includeOtherCoverageType);
		}

		if (includeAllDoseDescriptors) {
			toxQueryBlock.addAllDoseDescriptorField();
		} else if (doseDescriptors!=null && !doseDescriptors.isEmpty()) {
			toxQueryBlock.addDoseDescriptorField(doseDescriptors, includeOtherDoseDescriptor);
		}
		
		if (includeAllGLPCompliances) {
			toxQueryBlock.addAllGLPComplianceField();
		}
		
		if (includeAllGuidelines) {
			toxQueryBlock.addAllTestGuidelineAndQualifierFields();
		}
		
		if (includeAllEndpointTypes) {
			toxQueryBlock.addAllEndpointTypeField();
		}
		
		if (afterYear!=null) {
			toxQueryBlock.addAfterYearField(afterYear);
		}
		
		return toxQueryBlock;
	}
	
	/**
	 * Creates the Query object corresponding to the given options
	 * @return		The desired Query
	 */
	@Override
	public Query generateQuery() {
		Query query = new Query(limit);
		ToxQueryBlock toxQueryBlock = generateToxQueryBlock();
		query.addPropertyBlock(toxQueryBlock);
		return query;
	}
	
	/**
	 * Downloads the results of the given query to the results database
	 * @param startFresh	True to rebuild the database from scratch, false otherwise
	 */
	public void runDownload(String databaseName,boolean startFresh) {
		List<ToxQueryOptions> splitOptions = toxResize();
		QueryHandler handler = new QueryHandler(1000,10);
		int counter = 0;
		for (ToxQueryOptions options:splitOptions) {
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
}
