package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Processing;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.UnitConverter;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Utility.TextProcessing;


/**
 * Stores downloaded data from eChemPortal in parsable format
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class FinalRecord {
	/**
	 * Chemical or substance name
	 */
	public String name;
	/**
	 * Chemical or substance ID number
	 */
	public String number;
	/**
	 * Type of chemical or substance name (e.g. IUPAC)
	 */
	public String nameType;
	/**
	 * Type of chemical or substance number (CAS or EINECS)
	 */
	public String numberType;
	public boolean memberOfCategory;
	/**
	 * Original source (ECHA CHEM, ECHA REACH, J-CHECK, CCR, or OECD SIDS IUCLID)
	 */
	public String participant;
	/**
	 * Dossier URL
	 */
	public String url;

	/**
	 * Type of information, filtered to "experimental study" only by default
	 */
	public String infoType;
	/**
	 * Reliability of information
	 */
	public String reliability;
	/**
	 * Endpoint type more specific than proeprty name, if provided
	 */
	public String endpointType;
	/**
	 * Year(s) of record
	 */
	public Set<String> years;
	/**
	 * Qualifier(s) for test guideline(s)
	 */
	public List<String> guidelineQualifiers;
	/**
	 * Test guideline(s)
	 */
	public List<String> guidelines;
	/**
	 * GLP compliance of information
	 */
	public String glpCompliance;
	/**
	 * Type of experiment
	 */
	public String testType;
	/**
	 * Species of test organisms
	 */
	public List<String> species;
	/**
	 * Strain of test organisms
	 */
	public String strain;
	/**
	 * Route of administration for experiment
	 */
	public String routeOfAdministration;
	/**
	 * Type of exposure for inhalation experiments
	 */
	public String inhalationExposureType;
	/**
	 * Type of cover for dermal/skin experiments
	 */
	public String coverageType;
	/**
	 * Water media type for bioaccumulation experiments
	 */
	public String waterMediaType;
	/**
	 * Type of experimental value ("dose descriptor" in most results)
	 */
	public List<String> valueTypes;
	/**
	 * Quantitative experimental value ("effect level", "effect concentration", "half-life", "% degraded", etc.)
	 */
	public List<String> experimentalValues;
	/**
	 * Histopathological findings for carcinogenicity experiments
	 */
	public String histoFindings;
	/**
	 * Duration(s) or sampling time(s) of experiment
	 */
	public List<String> durations;
	/**
	 * Basis for effect assessment
	 */
	public List<String> basis;
	/**
	 * Qualitative interpretation of results
	 */
	public String interpretationOfResults;
	/**
	 * Presence or absence of metabolic activation for <i>in vitro</i> experiments
	 */
	public List<String> metabolicActivation;
	/**
	 * Oxygen conditions for biodegradation experiments
	 */
	public String oxygenConditions;
	/**
	 * Presence or absence of genotoxicity
	 */
	public List<String> genotoxicity;
	/**
	 * Presence or absence of non-genetic toxicity for genotoxicity experiments
	 */
	public List<String> toxicity;
	/**
	 * Presence or absence of non-genetic cytotoxicity for genotoxicity experiments
	 */
	public List<String> cytotoxicity;

	/**
	 * Date downloaded
	 */
	public String dateAccessed;
	/**
	 * Name of property
	 */
	public String propertyName;
	/**
	 * Internal ID to match to ExperimentalRecords for dashboard processing
	 */
	public String id;

	public static String[] headers = {"Name","Name Type","Number","Number Type","Member of Category","Participant","URL","Info Type","Reliability","Endpoint Type",
			"Years","Guidelines & Qualifiers","GLP Compliance","Test Type","Species","Strain","Metabolic Activation","Route of Administration/Exposure",
			"Inhalation Exposure Type","Coverage Type","Water Media Type","Value Type","Experimental Value","Histopathological Findings: Neoplastic","Duration/Sampling Time","Basis",
			"Interpretation of Results","Oxygen Conditions","Genotoxicity","Toxicity","Cytotoxicity","Date Accessed","Property Name"};
	public static String[] fieldNames = {"name","nameType","number","numberType","memberOfCategory","participant","url","infoType","reliability","endpointType",
			"years","guidelineQualifiers+guidelines","glpCompliance","testType","species","strain","metabolicActivation","routeOfAdministration","inhalationExposureType",
			"coverageType","waterMediaType","valueTypes","experimentalValues","histoFindings","durations","basis","interpretationOfResults",
			"oxygenConditions","genotoxicity","toxicity","cytotoxicity","dateAccessed","propertyName"};
	public static String[] outputFieldNames = {"id","name","nameType","number","numberType","memberOfCategory","participant","url","infoType","reliability","endpointType",
			"years","guidelineQualifiers","guidelines","glpCompliance","testType","species","strain","metabolicActivation","routeOfAdministration","inhalationExposureType",
			"coverageType","waterMediaType","valueTypes","experimentalValues","histoFindings","durations","basis","interpretationOfResults",
			"oxygenConditions","genotoxicity","toxicity","cytotoxicity","dateAccessed","propertyName"};



	private static final transient UnitConverter unitConverter = new UnitConverter("data/density.txt");
	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();		


	public FinalRecord() {
		years = new HashSet<String>();
		guidelineQualifiers = new ArrayList<String>();
		guidelines = new ArrayList<String>();
		valueTypes = new ArrayList<String>();
		experimentalValues = new ArrayList<String>();
		durations = new ArrayList<String>();
		basis = new ArrayList<String>();
		species = new ArrayList<String>();
		metabolicActivation = new ArrayList<String>();
		genotoxicity = new ArrayList<String>();
		toxicity = new ArrayList<String>();
		cytotoxicity = new ArrayList<String>();
	}

	/**
	 * Checks equivalence of contents of two records, ignoring source and date accessed
	 * @param o		The object to check equality with
	 * @return		True if equivalent records, false otherwise
	 */
	public boolean recordEquals(Object o) {
		if (o==this) {
			return true;
		}

		if (!(o instanceof FinalRecord)) {
			return false;
		}

		FinalRecord r = (FinalRecord) o;
		if (!Objects.equals(name,r.name) ||
				!Objects.equals(nameType, r.nameType) ||
				!Objects.equals(number, r.number) ||
				!Objects.equals(numberType, r.numberType) ||
				!Objects.equals(memberOfCategory, r.memberOfCategory) ||
				!Objects.equals(infoType, r.infoType) ||
				!Objects.equals(reliability, r.reliability) ||
				!Objects.equals(endpointType, r.endpointType) ||
				!Objects.equals(years, r.years) ||
				!Objects.equals(guidelineQualifiers, r.guidelineQualifiers) ||
				!Objects.equals(guidelines, r.guidelines) ||
				!Objects.equals(glpCompliance, r.glpCompliance) ||
				!Objects.equals(testType, r.testType) ||
				!Objects.equals(species, r.species) ||
				!Objects.equals(strain, r.strain) ||
				!Objects.equals(routeOfAdministration, r.routeOfAdministration) ||
				!Objects.equals(inhalationExposureType, r.inhalationExposureType) ||
				!Objects.equals(coverageType, r.coverageType) ||
				!Objects.equals(valueTypes, r.valueTypes) ||
				!Objects.equals(experimentalValues, r.experimentalValues) ||
				!Objects.equals(histoFindings, r.histoFindings) ||
				!Objects.equals(durations, r.durations) ||
				!Objects.equals(basis, r.basis) ||
				!Objects.equals(interpretationOfResults, r.interpretationOfResults) ||
				!Objects.equals(metabolicActivation, r.metabolicActivation) ||
				!Objects.equals(oxygenConditions, r.oxygenConditions) ||
				!Objects.equals(genotoxicity, r.genotoxicity) ||
				!Objects.equals(toxicity, r.toxicity) ||
				!Objects.equals(cytotoxicity, r.cytotoxicity) ||
				!Objects.equals(waterMediaType, waterMediaType)) {
			return false;
		} else {
			return true;
		}
	}

	public String [] toStringArray(String [] fieldNames) {

		String Line = "";

		String [] array=new String [fieldNames.length];

		for (int i = 0; i < fieldNames.length; i++) {
			try {

				Field myField = this.getClass().getDeclaredField(fieldNames[i]);

				String val=null;
				String type=myField.getType().getName();


				switch (type) {
				case "java.util.Set":
					Set<String> set = (Set<String>) myField.get(this);
					if (set==null || set.isEmpty()) {
						val="";	
					} else if (set.size()==1) {
						val=(String) set.toArray()[0];
					} else {
						val=set.toString();
					}
					break;
				case "java.util.List":
					List<String> list = (List<String>) myField.get(this);
					if (list==null || list.isEmpty()) {
						val="";	
					} else if (list.size()==1) {
						val=list.get(0);
					} else {
						val=list.toString();
					}
					break;
				case "java.lang.String":
					if (myField.get(this)==null) val="";	
					else val=myField.get(this)+"";						
					val=TextProcessing.reverseFixChars(StringEscapeUtils.unescapeHtml4(val.replaceAll("(?<!\\\\)'", "\'")));					
					break;
				case "java.lang.Boolean": 							
					if (myField.get(this)==null) val="";	
					else val=myField.get(this)+"";						
					break;					
				case "boolean":
					val=myField.getBoolean(this)+"";
					break;
				}

				val=val.trim();
				val=val.replace("\r\n","<br>");
				val=val.replace("\n","<br>");

				array[i]=val;

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return array;
	}

	public ExperimentalRecord toExperimentalRecord(String experimentalValue, String valueType) {

		experimentalValue=experimentalValue.replace("gm/ Kg", ExperimentalConstants.str_g_kg);
		
		
//		System.out.println(this.propertyName);
		
		ExperimentalRecord er=new ExperimentalRecord();
		er.source_name="eChemPortalAPI";
		er.original_source_name=this.participant;
		er.date_accessed = this.dateAccessed;
		er.url=this.url;

		er.chemical_name=this.name;
		if(this.number!=null && !this.number.contentEquals("unknown")) {
			er.casrn=this.number;	
		}

		er.experimental_parameters = new Hashtable<>();
		er.experimental_parameters.put("Reliability", this.reliability);
		
		
		if(this.reliability.contains("3") || this.reliability.contains("4")) {
			er.keep=false;
			er.reason="Insufficient reliability";
//			System.out.println(er.casrn+"\t"+er.keep+"\t"+reliability);
		}


		String strGuidelinesQualifiers = "";
		for (int g = 0; g < this.guidelines.size(); g++) {
			if (g > 0) { strGuidelinesQualifiers = strGuidelinesQualifiers + "; "; }
			strGuidelinesQualifiers = strGuidelinesQualifiers + this.guidelineQualifiers.get(g) + " " + this.guidelines.get(g);
		}
		er.experimental_parameters.put("Guidelines", strGuidelinesQualifiers);
		er.experimental_parameters.put("GLP Compliance", this.glpCompliance);
		er.experimental_parameters.put("Test Type", this.testType);
		er.experimental_parameters.put("Value Type", valueType);
		er.experimental_parameters.put("Original ID", this.id);
		er.experimental_parameters.put("Route of Administration", this.routeOfAdministration);
		er.experimental_parameters.put("Strain", this.strain);

		boolean isRat=false;		
		if(this.species.get(0).equals("rat") || species.get(0).equals("other: rat, albino") )isRat=true;

		if(this.propertyName.equals("AcuteToxicityOral") && valueType.contentEquals("LD50") && isRat) {
			er.property_name="Oral rat LD50";
			er.property_category="acute oral toxicity";
		} else {
//			System.out.println(propertyName+"\t"+valueType+"\t"+url);
//			System.out.println(propertyName+"\t"+valueType);
		}
		//		System.out.println(er.property_name+"\t"+er.property_category);

		er.property_value_string=experimentalValue;

		if(er.property_name==null) {
			er.property_name="Not set";
			return er;
		}

		boolean foundNumeric=ParseUtilities.getToxicity(er,experimentalValue);
		if(foundNumeric)unitConverter.convertRecord(er);

		return er;
	}

	
	public ExperimentalRecord toExperimentalRecord() {

//		System.out.println(this.propertyName);
		
		ExperimentalRecord er=new ExperimentalRecord();
		er.source_name="eChemPortalAPI";
		er.original_source_name=this.participant;
		er.date_accessed = this.dateAccessed;
		er.url=this.url;

		er.chemical_name=this.name;
		if(this.number!=null && !this.number.contentEquals("unknown")) {
			er.casrn=this.number;	
		}

		er.experimental_parameters = new Hashtable<>();
		er.experimental_parameters.put("Reliability", this.reliability);
		
		if(this.reliability.contains("3") || this.reliability.contains("4")) {
			er.keep=false;
			er.reason="Insufficient reliability";
//			System.out.println(er.casrn+"\t"+er.keep+"\t"+reliability);
		}
		
		if(er.casrn!=null && er.casrn.equals("127-51-5")) {
			System.out.println("Here1\t"+er.keep+"\t"+this.reliability);
		}
		
		
		
//		System.out.println(this.reliability);
		

		String strGuidelinesQualifiers = "";
		for (int g = 0; g < this.guidelines.size(); g++) {
			if (g > 0) { strGuidelinesQualifiers = strGuidelinesQualifiers + "; "; }
			strGuidelinesQualifiers = strGuidelinesQualifiers + this.guidelineQualifiers.get(g) + " " + this.guidelines.get(g);
		}
		er.experimental_parameters.put("Guidelines", strGuidelinesQualifiers);
		er.experimental_parameters.put("GLP Compliance", this.glpCompliance);
		er.experimental_parameters.put("Test Type", this.testType);
//		er.experimental_parameters.put("Value Type", valueType);
		er.experimental_parameters.put("Original ID", this.id);
//		er.experimental_parameters.put("Route of Administration", this.routeOfAdministration);
		er.experimental_parameters.put("Strain", this.strain);
		er.experimental_parameters.put("Species", this.species.get(0));


//		if(this.propertyName.equals("AcuteToxicityOral") && valueType.contentEquals("LD50") && isRat) {
//			er.property_name="Oral rat LD50";
//			er.property_category="acute oral toxicity";
//		} else {
////			System.out.println(propertyName+"\t"+valueType+"\t"+url);
//			System.out.println(propertyName+"\t"+valueType);
//		}
		//		System.out.println(er.property_name+"\t"+er.property_category);		

		er.property_value_string=this.interpretationOfResults;
		
		
		if(er.keep) {
			er.property_value_units_final=ExperimentalConstants.str_binary;
			er.property_value_units_original=ExperimentalConstants.str_binary;
		}
		
		double nonSensitizing = 0;
		double sensitizing = 1;
		String PVLC=er.property_value_string.toLowerCase();
		
		
//		boolean isRat=false;		
//		if(this.species.get(0).equals("rat") || species.get(0).equals("other: rat, albino") )isRat=true;

		
		if(er.casrn!=null && er.casrn.equals("127-51-5")) {
			System.out.println("here2\t"+er.casrn+"\t"+er.keep+"\t"+this.reliability);
		}

		
		if(this.testType.contains("LLNA") && this.species.get(0).contains("mouse")) {
//			System.out.println(this.testType+"\t"+this.species.get(0));
			er.property_name=ExperimentalConstants.strSkinSensitizationLLNA;
			
			//TODO add code to set keep to false if has a guideline that we cant match back to leora OECD ones. Some EPA ones have matching OECD guidelines
			checkLLNA_Guideline(er,strGuidelinesQualifiers);
			
			
		} else {
			er.keep=false;
			er.reason="";
			er.property_name="not set";
			return er;
		}
		
		
		List<String> nonSensitizers = Arrays.asList("not sensitizer", "not sensitizing", "not sensitising", "non skin sensitizing",
				"non sensitizing", "non-sensitizer", "not irritant or sensitising", "not senstiizing",
				"non-sensitizing", "no sensitizing potential", "does not elicit a skin sensitizing reaction",
				"not sensitsing", "not sensiting", "not considered to be", "no sensitization",
				"no sensitizing or irritating potential", "no sensitization", "not a skin sensitiser",
				"not skin sensitising", "non sensitising", "non-sensitiser", "no skin sensitising potential",
				"not a skin sensitizer", "no indication", "non sensibilisant", "not a sensitizer",
				"not a skin sensitiser", "not irritating", "not skin sentising", "no skin sensitization potential",
				"not skin sensitizing", "not sensitoxing", "non-sensitising", "not skin sensitizer",
				"no skin sensitiser", "not-sensitising", "not skin sensitization", "negative",
				"no skin sensitising effects", "no skin sensitization", "non sensitising", 
				"no sensitising effects", "no or minimal reactivity", 
				"not skin sensitisng", "not sensiting", "no skin sensitization potencial",
				"not have a sensitization potential", "not show any skin sensitization", "not a sensitizer",
				"noit sensitizing", "not  sensitizing", "not sensitisting", "unlikely to be","other: not sensitizing in LLNA, sensisitizing in MEST");
		
		List<String> criteriaNotMet = Arrays.asList("criteria not met", "criteria are not met", "not classified",
				"no evidence", "shall not be classified", "are not met", "no category", "not need to be classified",
				"not required to be classified", "not fulfil the requirements",
				"criteria for classification as a skin sensitiser not met", "does not meet the criteria",
				"not considered a", "not be classified", "not likely to be", "not considered as a",
				"does not need to be classified", "no classification is required", "not considered to be sensitizing");
				
		List<String> sensitizers = Arrays.asList("sensitizer", "category", "sensitising", "skin sens. 1", "criteria met",
				"sensitizing", "skin sensitation potential is indicated", "sensitiser", "skin sens cat 1",
				"sensitization potential", "irritant", "classified", "cause sensitization", "sentisizer", "sensitising",
				"skin sens. 1", "skin sens", "reaction", "sentisization", "allergenic potency", "allergen", "positive",
				"sub-category", "cat. 1", "skin sens 1", "potential to cause skin sensitization");
	
		List<String> badData = Arrays.asList("study cannot be used", "false positive", "inconclusive", "not reliable",
				"doubtful relevance", "not a photosensitizer", "not photo-sensitising", "not photallergenic");
		
		List<String> ambiguous=Arrays.asList("ambiguous","ambigous","equivocal");
		
		if(hasString(badData, PVLC)) {
			er.keep=false;
			er.reason="Bad data or Study";
//			System.out.println(PVLC+"\tbad data\t"+this.reliability);
		} else if(hasString(ambiguous, PVLC)) {
			er.keep=false;
			er.reason="ambiguous results";
//			System.out.println(PVLC+"\tambiguous");
		} else if(hasString(nonSensitizers, PVLC)) {
			er.property_value_point_estimate_original = nonSensitizing;
//			System.out.println(PVLC+"\tnot sensitizing");
		} else if(hasString(criteriaNotMet, PVLC)) {
			er.property_value_point_estimate_original = nonSensitizing;
						
//			if(!PVLC.equals("GHS criteria not met")) {
//				System.out.println(PVLC+"\tcriteria not met");	
//			}
			
		} else if(hasString(sensitizers, PVLC)) {
			er.property_value_point_estimate_original = sensitizing;
//		System.out.println(PVLC+"\tsensitizing");
		} else {
			er.keep=false;
			er.reason="Other bad data or Study";
//			System.out.println(PVLC+"\tother bad data");
		}
		
		er.property_value_point_estimate_final = er.property_value_point_estimate_original;
		

//		boolean foundNumeric=ParseUtilities.getToxicity(er,experimentalValue);
//		if(foundNumeric)unitConverter.convertRecord(er);

		return er;
	}
	
	
	private void checkLLNA_Guideline(ExperimentalRecord er, String strGuidelinesQualifiers) {

		boolean hasGoodGuideline=false;
		
//		List<String> badGuidelines = Arrays.asList("according to guideline other:", "according to guideline other: as below", "according to guideline other: as per mentioned below",
//				"according to guideline other: LLNA assay", "according to guideline other: The objective of the study was to evaluate the utility of the LLNA assay to determine the contact sensitization potential of the test chemical",
//				"according to guideline other: Sensitive mouse lymph node assay (SLNA)", "according to guideline other: The objective of the study was to evaluate the utility of the LLNA assay to determine the contact sensitization potential of the test chemical",
//				"equivalent or similar to guideline other: according to Ulrich, P. et al. 1998: Toxicology 125, 149-168", "equivalent or similar to guideline other: As mentioned below", " equivalent or similar to guideline other: Kimber et al., 1989");
		
		List<String> goodGuidelines = Arrays.asList("429", "870.2600", "B.42", "442A", "442B","442 B", "B.51", "406", "595.12", "B.6");

		
//		if(er.casrn!=null && er.casrn.equals("127-51-5")) {
//			System.out.println(gson.toJson(this)+"\r\n");
//			System.out.println(gson.toJson(er));
//		}
		
		if(er.keep) {
			if(!hasString(goodGuidelines, strGuidelinesQualifiers)){
				
				if(er.casrn!=null && er.casrn.equals("127-51-5")) {
					System.out.println("here3\t"+er.casrn+"\t"+er.keep+"\t"+"Invalid guideline has reliability=" +er.experimental_parameters.get("Reliability"));
				}
				
//				System.out.println(er.casrn+"\t"+er.keep+"\t"+"Invalid guideline has reliability=" +er.experimental_parameters.get("Reliability"));
				er.keep=false;
				er.reason="Invalid guideline";	
			}
		}
		
		
		
	}

	boolean hasString(List<String>examples,String str) {
		for(String example:examples ) {
			if(str.contains(example)) {
				return true;
			}
		}

		return false;
	}
		
}
