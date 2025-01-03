package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.exp_data_gathering.parse.UtilitiesUnirest;

/**
 * Defines options for an eChemPortal API query specific to toxicity properties
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class ToxQueryOptions extends QueryOptions {
	
	private ToxQueryOptions copy() {
		Gson gson = new GsonBuilder().create();
		String optionsJSON = gson.toJson(this);
		return gson.fromJson(optionsJSON,ToxQueryOptions.class);
	}
	
	/**
	 * Creates ToxQueryOptions objects for inhalation LC50 queries requested by Todd Martin
	 * @return		The list of associated ToxQueryOptions objects
	 */
	public static Query generateInhalationLC50Queries() {
		String[] speciesSimple = {"rat","mouse","guinea pig","rabbit"};
		String[] lc50 = {"LC50"};

		ToxQueryOptions simple = new ToxQueryOptions();
		simple.endpointKind = APIConstants.acuteToxicityInhalation;
		simple.endpointMin = "0";
		simple.includeAllUnits = true;
		simple.stringFields.add(new StringField(APIConstants.species,Arrays.asList(speciesSimple),false));
		simple.stringFields.add(new StringField(APIConstants.routeOfAdministration));
		simple.stringFields.add(new StringField(APIConstants.valueType,Arrays.asList(lc50),false));
		
		ToxQueryOptions complex = new ToxQueryOptions();
		complex.endpointKind = APIConstants.acuteToxicityInhalation;
		complex.endpointMin = "0";
		complex.includeAllUnits = true;
		complex.stringFields.add(new StringField(APIConstants.testType));
		complex.stringFields.add(new StringField(APIConstants.species));
		complex.stringFields.add(new StringField(APIConstants.strain));
		complex.stringFields.add(new StringField(APIConstants.inhalationExposureType));
		complex.stringFields.add(new StringField(APIConstants.routeOfAdministration));
		complex.stringFields.add(new StringField(APIConstants.valueType,Arrays.asList(lc50),false));
		
		Query query = new Query(5000);
		ToxQueryBlock simpleBlock = simple.generateQueryBlock(false);
		ToxQueryBlock complexBlock = complex.generateQueryBlock(false);
		query.addPropertyBlock(complexBlock);
		query.addOperatorBlock("OR");
		query.addPropertyBlock(simpleBlock);
		return query;
	}
	
	/**
	 * Sets defaults for a ToxQueryOptions object according to ToxVal query requirements
	 * @param endpointKind	The endpoint kind to query
	 * @return				The associated ToxQueryOptions object
	 */
	public static ToxQueryOptions generateEndpointToxQueryOptionsForToxVal(String endpointKind) {
		ToxQueryOptions options = generateEndpointToxQueryOptions(endpointKind,5,"1950",true);
		return options;
	}
	
	/**
	 * Sets defaults for a ToxQueryOptions object according to dashboard query requirements
	 * @param endpointKind	The endpoint kind to query
	 * @return				The associated ToxQueryOptions object
	 */
	public static ToxQueryOptions generateEndpointToxQueryOptionsForDashboard(String endpointKind) {
		ToxQueryOptions options = generateEndpointToxQueryOptions(endpointKind,2,"1900",false);
		return options;
	}
	
	/**
	 * Sets defaults for a ToxQueryOptions object according to ToxVal query requirements
	 * @param endpointKind	The endpoint kind to query
	 * @return				The associated ToxQueryOptions object
	 */
	public static ToxQueryOptions generateEndpointToxQueryOptions(String endpointKind, int maxReliabilityLevel,
			String afterYear, boolean includeGLP) {
		ToxQueryOptions options = new ToxQueryOptions();
		boolean isEcoTox = APIConstants.ecoToxEndpointsList.contains(endpointKind);
		boolean hasEndpointType = !endpointKind.contains("AcuteToxicity") && 
				(!endpointKind.contains("Aqua") || endpointKind.equals(APIConstants.bioaccumulationAquaticSediment)) &&
				!endpointKind.equals(APIConstants.toxicityToOtherAboveGroundOrganisms) &&
				!endpointKind.equals(APIConstants.toxicityToSoilMicroorganisms) &&
				!endpointKind.equals(APIConstants.developmentalToxicityTeratogenicity);
		boolean hasNumericEffectLevel = !endpointKind.equals(APIConstants.carcinogenicity) && !endpointKind.contains("Irritation") &&
				!endpointKind.equals(APIConstants.skinSensitisation) && !endpointKind.contains("Genetic");
		options.endpointKind = endpointKind;
		options.maxReliabilityLevel = maxReliabilityLevel;
		options.afterYear = afterYear;
		options.stringFields.add(new StringField(APIConstants.guideline));
		options.stringFields.add(new StringField(APIConstants.guidelineQualifier));
		if (includeGLP) { options.stringFields.add(new StringField(APIConstants.glpCompliance)); }
		if (!endpointKind.contains("Biodegradation")) { options.stringFields.add(new StringField(APIConstants.species)); }
		
		if (hasNumericEffectLevel) {
			// Adds endpoint value range and dose descriptor field for all endpoints except carcinogenicity
			options.endpointMin = "0";
			options.endpointMax = "10000000";
			options.includeAllUnits = true;
			if (!endpointKind.equals(APIConstants.developmentalToxicityTeratogenicity) && !endpointKind.contains("BiodegradationInSoil")) {
				options.stringFields.add(new StringField(APIConstants.valueType));
				if (endpointKind.equals(APIConstants.biodegradationInWater)) { options.stringFields.add(new StringField(APIConstants.interpretationOfResults)); }
			} else if (!endpointKind.contains("BiodegradationInSoil")) {
				options.stringFields.add(new StringField(APIConstants.valueType+" Maternal"));
				options.stringFields.add(new StringField(APIConstants.valueType+" Fetal"));
			} 
		} else if (endpointKind.equals(APIConstants.carcinogenicity)) {
			// Adds histopathological findings field for carcinogenicity
			options.stringFields.add(new StringField(APIConstants.histoFindingsNeo));
		} else if (!endpointKind.contains("Genetic")) {
			// Adds interpretation field for skin irritation & sensitization, eye irritation
			options.stringFields.add(new StringField(APIConstants.interpretationOfResults));
		}
		
		// Adds test type field for skin sensitization and acute oral, inhalation, and dermal toxicities
		if ((endpointKind.contains("AcuteToxicity") && !endpointKind.contains("Other")) ||
				endpointKind.equals(APIConstants.skinSensitisation) || endpointKind.contains("Genetic") || endpointKind.contains("BiodegradationInSoil") ||
				endpointKind.equals(APIConstants.bioaccumulationAquaticSediment)) {
			options.stringFields.add(new StringField(APIConstants.testType));
		}
		
		// Adds endpoint type field for endpoints that specify further
		if (hasEndpointType) {
			options.stringFields.add(new StringField(APIConstants.endpointType));
		}
		
		// Adds test animal strain field for non-eco toxicities
		if (!isEcoTox && !endpointKind.equals(APIConstants.geneticToxicityVitro)) {
			options.stringFields.add(new StringField(APIConstants.strain));
		}
		
		if (endpointKind.contains("Dermal") || endpointKind.equals(APIConstants.skinIrritationCorrosion)) {
			// Adds coverage type field for dermal toxicities
			options.stringFields.add(new StringField(APIConstants.coverageType));
		} else if (endpointKind.equals(APIConstants.eyeIrritation) || endpointKind.equals(APIConstants.skinSensitisation) ||
				endpointKind.contains("Genetic")) {
			// Do nothing
		} else if (!isEcoTox) {
			// Adds route of administration field for non-dermal, non-eco toxicities
			options.stringFields.add(new StringField(APIConstants.routeOfAdministration));
			// Adds basis for effect field for developmental toxicity
			if (endpointKind.equals(APIConstants.developmentalToxicityTeratogenicity)) {
				options.stringFields.add(new StringField(APIConstants.basis+" Maternal"));
				options.stringFields.add(new StringField(APIConstants.basis+" Fetal"));
			}
		} else {
			// Adds duration and basis for effect fields for eco toxicities
			if (!endpointKind.equals(APIConstants.biodegradationInSoilHalfLife)) {
				if (!endpointKind.equals(APIConstants.bioaccumulationAquaticSediment)) {
					options.includeAllDuration = true;
				} else {
					options.stringFields.add(new StringField(APIConstants.routeOfAdministration));
				}
				
				if (!endpointKind.equals(APIConstants.biodegradationInSoilPctDegr) && !endpointKind.equals(APIConstants.biodegradationInWater)) {
					options.stringFields.add(new StringField(APIConstants.basis));
				}
			}
		}
		
		// Adds inhalation exposure type for acute and repeated dose inhalation toxicity
		if (endpointKind.contains("Inhalation")) { options.stringFields.add(new StringField(APIConstants.inhalationExposureType)); }
		
		if (endpointKind.contains("Genetic")) {
			options.stringFields.add(new StringField(APIConstants.genotoxicity));
			if (endpointKind.equals(APIConstants.geneticToxicityVitro)) {
				options.stringFields.add(new StringField(APIConstants.metabolicActivation));
			}
		}
		
		if (endpointKind.equals(APIConstants.biodegradationInWater)) { options.stringFields.add(new StringField(APIConstants.oxygenConditions)); }
		
		if (endpointKind.equals(APIConstants.bioaccumulationAquaticSediment)) { options.stringFields.add(new StringField(APIConstants.waterMediaType)); }
		
		return options;
	}
	
	/**
	 * Merges the time range of another ToxQueryOptions object into the current ToxQueryOptions
	 * @param options	The ToxQueryOptions object to merge
	 */
	@Override
	protected boolean mergeOptions(QueryOptions options) {
		if (this.endpointKind.equals(options.endpointKind)) {
			if (this.beforeYear.equals(options.afterYear)) {
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
	 * @return	Query size
	 */
	private int getQueryMaxSize() {
		Query query = new Query(limit);
		ToxQueryBlock toxQueryBlock = generateQueryBlock(true);
		query.addPropertyBlock(toxQueryBlock);
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
	 * Splits the QueryOptions object at the range midpoint to reduce query size
	 * @return		Two QueryOptions objects that jointly cover the same range as the original
	 */
	private List<ToxQueryOptions> splitOptionsByValue() {
		List<ToxQueryOptions> splitOptions = new ArrayList<ToxQueryOptions>();
		ToxQueryOptions lowerSplitOptions = this.copy();
		ToxQueryOptions upperSplitOptions = this.copy();
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
	private List<ToxQueryOptions> splitOptionsByYear() {
		List<ToxQueryOptions> splitOptions = new ArrayList<ToxQueryOptions>();
		ToxQueryOptions lowerSplitOptions = this.copy();
		ToxQueryOptions upperSplitOptions = this.copy();
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
	 * Recursively splits a vector of ToxQueryOptions until all queries have size {@literal <} maxSize
	 * @param options	Vector of ToxQueryOptions to be resized
	 * @return			Vector of ToxQueryOptions of permitted size
	 */
	private static List<ToxQueryOptions> resizeAll(List<ToxQueryOptions> options, int maxSize) {
		List<ToxQueryOptions> newOptions = new ArrayList<ToxQueryOptions>();
		for (ToxQueryOptions o:options) {
			int size = o.getQueryMaxSize();
			if (size >= maxSize) {
				System.out.println("Resizing...");
				List<ToxQueryOptions> splitOptions = o.splitOptionsByYear();
				splitOptions = resizeAll(splitOptions,maxSize);
				newOptions.addAll(splitOptions);
			} else {
				newOptions.add(o);
			}
		}
		return newOptions;
	}
	
	/**
	 * If QueryOptions specify a query too large for the API (limit maxSize results), splits it into a vector of permitted query size
	 * @return		Vector of QueryOptions of permitted size
	 */
	private List<ToxQueryOptions> resize(int maxSize) {
		List<ToxQueryOptions> options = new ArrayList<ToxQueryOptions>();
		options.add(this);
		if (getQueryMaxSize() >= maxSize) {
			System.out.println(this.endpointKind+" query too large. Resizing...");
			options = resizeAll(options,maxSize);
			System.out.println("Split into "+options.size()+" queries. Merging small queries...");
			ListIterator<ToxQueryOptions> it = options.listIterator();
			int lastSize = 0;
			while (it.hasNext()) {
				ToxQueryOptions currentOptions = it.next();
				int size = currentOptions.getQueryMaxSize();
				if (it.previousIndex() > 0 && size + lastSize < maxSize) {
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
	private ToxQueryBlock generateQueryBlock(boolean ignoreOtherToxForGeneticTox) {
		ToxQueryBlock toxQueryBlock = new ToxQueryBlock(endpointKind);
		toxQueryBlock.addInfoTypeField();
		toxQueryBlock.addReliabilityField(maxReliabilityLevel);
		
		if (!endpointKind.equals(APIConstants.developmentalToxicityTeratogenicity)) {
			if (includeAllUnits) {
				toxQueryBlock.addAllUnitRangeField(APIConstants.effectLevel,endpointMin,endpointMax);
			} else if (endpointMin!=null || endpointMax!=null || endpointUnits!=null) {
				toxQueryBlock.addRangeField(APIConstants.effectLevel,endpointMin,endpointMax,endpointUnits);
			}
		} else {
			if (includeAllUnits) {
				toxQueryBlock.addAllUnitRangeField(APIConstants.effectLevel+" Maternal",endpointMin,endpointMax);
				toxQueryBlock.addAllUnitRangeField(APIConstants.effectLevel+" Fetal",endpointMin,endpointMax);
			} else if (endpointMin!=null || endpointMax!=null || endpointUnits!=null) {
				toxQueryBlock.addRangeField(APIConstants.effectLevel+" Maternal",endpointMin,endpointMax,endpointUnits);
				toxQueryBlock.addRangeField(APIConstants.effectLevel+" Fetal",endpointMin,endpointMax,endpointUnits);
			}
		}
		
		for (StringField sf:stringFields) {
			toxQueryBlock.addStringField(sf);
		}
		
		if (afterYear!=null) {
			toxQueryBlock.addAfterYearField(afterYear);
		}
		
		if (beforeYear!=null) {
			toxQueryBlock.addBeforeYearField(beforeYear);
		}
		
		if (includeAllDuration) {
			toxQueryBlock.addAllDurationField();
		}
		
		if (endpointKind.contains("Genetic") && !ignoreOtherToxForGeneticTox) { toxQueryBlock.addOtherGeneticToxicityFields(); }
		
		return toxQueryBlock;
	}
	
	/**
	 * Creates the Query object corresponding to the given options
	 * @return		The desired Query
	 */
	private Query generateQuery() {
		Query query = new Query(limit);
		ToxQueryBlock toxQueryBlock = generateQueryBlock(false);
		query.addPropertyBlock(toxQueryBlock);
		if (endpointKind.contains("Genetic")) {
			query.addOperatorBlock("OR");
			ToxQueryBlock toxQueryBlock2 = generateQueryBlock(true);
			query.addPropertyBlock(toxQueryBlock2);
		}
		return query;
	}
	
	/**
	 * Downloads the results of the given query to the results database
	 * @param startFresh	True to rebuild the database from scratch, false otherwise
	 */
	@Override
	public void runDownload(String databaseName,boolean startFresh, int maxSize) {
		
		UtilitiesUnirest.configUnirest(true);

		
		List<ToxQueryOptions> splitOptions = resize(maxSize);
		QueryHandler handler = new QueryHandler(1000,10);
		int counter = 0;
						
		for (ToxQueryOptions options:splitOptions) {
			String after = options.afterYear==null ? "the beginning of time" : options.afterYear;
			String before = options.beforeYear==null ? "present" : options.beforeYear;
			System.out.println("Querying "+endpointKind+" results from "+after+" to "+before+"...");
			Query query = options.generateQuery();
			if (counter==0) {
				handler.downloadQueryResultsToDatabase(query,databaseName,startFresh);
			} else {
				handler.downloadQueryResultsToDatabase(query,databaseName,false);
			}
			counter++;
		}
		System.out.println("Download complete!");
	}
}
