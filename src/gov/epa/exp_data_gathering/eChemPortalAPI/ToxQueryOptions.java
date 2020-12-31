package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ToxQueryOptions extends QueryOptions {
	List<String> testTypes;
	boolean includeOtherTestType;
	List<String> species;
	boolean includeOtherSpecies;
	List<String> strains;
	boolean includeOtherStrain;
	List<String> routesOfAdministration;
	boolean includeOtherRoute;
	List<String> inhalationTypes;
	boolean includeOtherInhalationType;
	List<String> doseDescriptors;
	boolean includeOtherDoseDescriptor;
	
	// Default null constructor
	ToxQueryOptions() {
		limit = 5000;
		propertyName = null;
		maxReliabilityLevel = 2;
		endpointMin = null;
		endpointMax = null;
		endpointUnits = null;
		testTypes = new ArrayList<String>();
		species = new ArrayList<String>();
		strains = new ArrayList<String>();
		routesOfAdministration = new ArrayList<String>();
		inhalationTypes = new ArrayList<String>();
		doseDescriptors = new ArrayList<String>();
		includeOtherTestType = false;
		includeOtherSpecies = false;
		includeOtherStrain = false;
		includeOtherRoute = false;
		includeOtherInhalationType = false;
		includeOtherDoseDescriptor = false;
	}
	
	// Duplicate constructor
	ToxQueryOptions(ToxQueryOptions options) {
		limit = options.limit;
		propertyName = options.propertyName;
		maxReliabilityLevel = options.maxReliabilityLevel;
		endpointMin = options.endpointMin;
		endpointMax = options.endpointMax;
		endpointUnits = options.endpointUnits;
		testTypes = options.testTypes;
		species = options.species;
		strains = options.strains;
		routesOfAdministration = options.routesOfAdministration;
		inhalationTypes = options.inhalationTypes;
		doseDescriptors = options.doseDescriptors;
		includeOtherTestType = options.includeOtherTestType;
		includeOtherSpecies = options.includeOtherSpecies;
		includeOtherStrain = options.includeOtherStrain;
		includeOtherRoute = options.includeOtherRoute;
		includeOtherInhalationType = options.includeOtherInhalationType;
		includeOtherDoseDescriptor = options.includeOtherDoseDescriptor;
	}
	
	public static List<ToxQueryOptions> generateSimpleInhalationLC50Options() {
		List<ToxQueryOptions> options = new ArrayList<ToxQueryOptions>();
		String[] units = {"mg/L air","mg/L air (nominal)","mg/L air (analytical)","mg/m^3 air","mg/m^3 air (nominal)","mg/m^3 air (analytical)","ppm"};
		String[] species = {"rat","mouse","guinea pig","rabbit"};
		String[] routesOfAdministration = {"inhalation","inhalation: aerosol","inhalation: dust","inhalation: gas","inhalation: mist","inhalation: vapour",
				"inhalation: mixture of gas, vapour and aerosol","inhalation: mixture of gas and vapour","inhalation: mixture of vapour and aerosol / mist"};
		String[] doseDescriptors = {"LC50"};
		
		for (String unit:units) {
			ToxQueryOptions tqo = new ToxQueryOptions();
			tqo.propertyName = "AcuteToxicityInhalation";
			tqo.endpointMin = "0";
			tqo.endpointMax = String.valueOf(Integer.MAX_VALUE);
			tqo.endpointUnits = unit;
			tqo.species = Arrays.asList(species);
			tqo.routesOfAdministration = Arrays.asList(routesOfAdministration);
			tqo.includeOtherRoute = true;
			tqo.doseDescriptors = Arrays.asList(doseDescriptors);
			options.add(tqo);
		}
		
		return options;
	}
	
	public static List<ToxQueryOptions> generateComplexInhalationLC50Options() {
		List<ToxQueryOptions> options = generateSimpleInhalationLC50Options();
		String[] testTypes = {"acute toxic class method","concentration x time method","fixed concentration procedure","traditional method","standard acute method"};
		String[] species = {"cat","cattle","dog","gerbil","hamster","hamster, Armenian","hamster, Chinese","hamster, Syrian","hen","rat","mouse","guinea pig","rabbit",
				"miniature swine","monkey","pig","primate","sheep"};
		String[] strains = {"Abyssinian","AKR","Angora","B6C3F1","Balb/c","Belgian Hare","Brown Norway","C3H","C57BL","CAF1","Californian","CB6F1","CBA","CD-1","CF-1",
				"Chinchilla","Crj: CD(SD)","DBA","DBF1","Dunkin-Hartley","Dutch","Fischer 344","Fischer 344/DuCrj","Flemish Giant","FVB","Hartley","Himalayan","ICL-ICR",
				"ICR","Lewis","Long-Evans","New Zealand Black","New Zealand Red","New Zealand White","NMRI","not specified","Nude","Nude Balb/cAnN","Nude CD-1",
				"Osborne-Mendel","Peruvian","Pirbright-Hartley","Polish","San Juan","Sencar","Sherman","Shorthair","SIV 50","SKH/HR1","Sprague-Dawley","Strain A","Swiss",
				"Swiss Webster","Tif:MAGf","Vienna White","Wistar","Wistar Kyoto (WKY)","Zucker"};
		String[] inhalationTypes = {"head only","nose/head only","nose only","not specified","whole body"};
		
		ListIterator<ToxQueryOptions> it = options.listIterator();
		while (it.hasNext()) {
			ToxQueryOptions o = it.next();
			o.species = Arrays.asList(species);
			o.includeOtherSpecies = true;
			o.strains = Arrays.asList(strains);
			o.includeOtherStrain = true;
			o.testTypes = Arrays.asList(testTypes);
			o.includeOtherTestType = true;
			o.inhalationTypes = Arrays.asList(inhalationTypes);
			o.includeOtherInhalationType = true;
			it.set(o);
		}
		
		return options;
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
		toxQueryBlock.addReliabilityField(maxReliabilityLevel);
		
		// Endpoint value (called "Effect level" for tox data)
		toxQueryBlock.addEndpointField(endpointMin,endpointMax,endpointUnits);
		
		// Test types
		if (testTypes!=null && !testTypes.isEmpty()) {
			toxQueryBlock.addTestTypeField(testTypes,includeOtherTestType);
		}
		
		// Species
		if (species!=null && !species.isEmpty()) {
			toxQueryBlock.addSpeciesField(species,includeOtherSpecies);
		}
		
		// Strain
		if (strains!=null && !strains.isEmpty()) {
			toxQueryBlock.addStrainField(strains,includeOtherStrain);
		}
		
		// Route of administration
		if (routesOfAdministration!=null && !routesOfAdministration.isEmpty()) {
			toxQueryBlock.addAdministrationRouteField(routesOfAdministration,includeOtherRoute);
		}
		
		// Inhalation types
		if (inhalationTypes!=null && !inhalationTypes.isEmpty()) {
			toxQueryBlock.addInhalationTypeField(inhalationTypes,includeOtherInhalationType);
		}
		
		// Dose descriptor
		if (doseDescriptors!=null && !doseDescriptors.isEmpty()) {
			toxQueryBlock.addDoseDescriptorField(doseDescriptors, includeOtherDoseDescriptor);
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
