package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Defines options for an eChemPortal API query for toxicity properties
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
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
		beforeYear = options.beforeYear;
		endpointMin = options.endpointMin;
		endpointMax = options.endpointMax;
		endpointUnits = options.endpointUnits;
		includeAllUnits = options.includeAllUnits;
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
	
	/**
	 * Creates ToxQueryOptions objects for inhalation LC50 queries requested by Todd Martin
	 * @return				The list of associated ToxQueryOptions objects
	 */
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
	
	/**
	 * Sets defaults for a ToxQueryOptions object to retrieve all results after 1960 for any acute or repeated dose toxicity endpoint
	 * @param endpointKind	The endpoint kind to query
	 * @return				The associated ToxQueryOptions object
	 */
	public static ToxQueryOptions generateCompleteToxQueryOptions(String endpointKind) {
		ToxQueryOptions options = new ToxQueryOptions();
		options.propertyName = endpointKind;
		options.endpointMin = "0";
		options.endpointMax = "1000000";
		options.includeAllUnits = true;
		options.maxReliabilityLevel = 4;
		options.includeOtherReliability = true;
		options.afterYear = "1960";
		options.includeAllGuidelines = true;
		options.includeAllGLPCompliances = true;
		if (endpointKind.contains("AcuteToxicity") && !endpointKind.contains("Other")) { options.includeAllTestTypes = true; }
		if (endpointKind.contains("RepeatedDoseToxicity")) { options.includeAllEndpointTypes = true; }
		options.includeAllSpecies = true;
		options.includeAllStrains = true;
		if (endpointKind.contains("Dermal")) {
			options.includeAllCoverageTypes = true;
		} else {
			options.includeAllRoutes = true;
		}
		if (endpointKind.contains("Inhalation")) { options.includeAllInhalationTypes = true; }
		options.includeAllDoseDescriptors = true;
		return options;
	}
	
	/**
	 * Splits the QueryOptions object at the unit or time range midpoint (depending on tolerance) to reduce query size
	 * @return		Two QueryOptions objects that jointly cover the same ranges as the original
	 */
	protected List<ToxQueryOptions> generateSplitOptions(double tolerance) {
		List<ToxQueryOptions> splitOptions = new ArrayList<ToxQueryOptions>();
		ToxQueryOptions lowerSplitOptions = new ToxQueryOptions(this);
		ToxQueryOptions upperSplitOptions = new ToxQueryOptions(this);
		double min = endpointMin==null ? Integer.MIN_VALUE : Double.parseDouble(endpointMin);
		double max = endpointMax==null ? Integer.MAX_VALUE : Double.parseDouble(endpointMax);
		if (Math.abs(max-min) > tolerance) {
			double midpoint = (max + min)/2.0;
			lowerSplitOptions.endpointMax = String.valueOf(midpoint);
			upperSplitOptions.endpointMin = String.valueOf(midpoint);
		} else {
			Date date = new Date();
			int thisYear = date.getYear()+1900;
			int minYear = this.afterYear==null ? 1900 : Integer.parseInt(this.afterYear);
			int maxYear = this.beforeYear==null ? thisYear : Integer.parseInt(this.beforeYear);
			String midpointYear = String.valueOf((maxYear+minYear)/2);
			lowerSplitOptions.beforeYear = midpointYear;
			upperSplitOptions.afterYear = midpointYear;
		}
		splitOptions.add(lowerSplitOptions);
		splitOptions.add(upperSplitOptions);
		return splitOptions;
	}
	
	/**
	 * Splits the QueryOptions object at the midpoint of the time range to reduce query size
	 * @return		Two QueryOptions objects that jointly cover the same time range as the original
	 */
	protected List<ToxQueryOptions> generateSplitOptions() {
		List<ToxQueryOptions> splitOptions = new ArrayList<ToxQueryOptions>();
		ToxQueryOptions lowerSplitOptions = new ToxQueryOptions(this);
		ToxQueryOptions upperSplitOptions = new ToxQueryOptions(this);
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
	 * Gets the maximum size of the query corresponding to the given options (i.e. with no conditions specified)
	 * @return	Query size
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
	 * Recursively splits a vector of ToxQueryOptions until all queries have size < 10000
	 * @param options	Vector of ToxQueryOptions to be resized
	 * @return			Vector of ToxQueryOptions of permitted size
	 */
	private static List<ToxQueryOptions> resizeAll(List<ToxQueryOptions> options) {
		List<ToxQueryOptions> newOptions = new ArrayList<ToxQueryOptions>();
		for (ToxQueryOptions o:options) {
			int size = o.getQueryMaxSize();
			System.out.println(o.endpointMin + " to " + o.endpointMax + " units, "+o.afterYear+" to "+o.beforeYear+": " + size + " records");
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
					boolean success = lastOptions.mergeOptions(currentOptions);
					if (success) {
						it.set(lastOptions);
						lastSize = lastOptions.getQueryMaxSize();
					} else {
						it.add(currentOptions);
						lastSize = currentOptions.getQueryMaxSize();
					}
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
	 * Creates the ToxQueryBlock object corresponding to the given options
	 * @return		The desired QueryBlock
	 */
	public ToxQueryBlock generateToxQueryBlock() {
		String endpointKind = propertyName;
		ToxQueryBlock toxQueryBlock = new ToxQueryBlock(endpointKind);
		toxQueryBlock.addInfoTypeField();
		toxQueryBlock.addReliabilityField(maxReliabilityLevel,includeOtherReliability);
		
		if (includeAllUnits) {
			toxQueryBlock.addAllUnitEffectLevelField(endpointMin, endpointMax);
		} else {
			toxQueryBlock.addEffectLevelField(endpointMin,endpointMax,endpointUnits);
		}

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
		
//		if (afterYear!=null || beforeYear!=null) {
//			toxQueryBlock.addYearField(afterYear,beforeYear);
//		}
		
		if (afterYear!=null) {
			toxQueryBlock.addAfterYearField(afterYear);
		}
		
		if (beforeYear!=null) {
			toxQueryBlock.addBeforeYearField(beforeYear);
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
//		Gson gson = new GsonBuilder().setPrettyPrinting().create();
//		System.out.println(gson.toJson(query));
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
