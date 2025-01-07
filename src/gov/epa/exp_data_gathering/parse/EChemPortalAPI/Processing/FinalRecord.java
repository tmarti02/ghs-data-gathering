package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Processing;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.text.StringEscapeUtils;

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

		System.out.println(this.propertyName);
		
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
			System.out.println(propertyName+"\t"+valueType);
		}
		//		System.out.println(er.property_name+"\t"+er.property_category);

		er.property_value_string=experimentalValue;

		if(er.property_name==null) return er;

		boolean foundNumeric=ParseUtilities.getToxicity(er,experimentalValue);
		if(foundNumeric)unitConverter.convertRecord(er);

		return er;
	}

	
	public ExperimentalRecord toExperimentalRecord() {

		System.out.println(this.propertyName);
		
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

		boolean isRat=false;		
		if(this.species.get(0).equals("rat") || species.get(0).equals("other: rat, albino") )isRat=true;

//		if(this.propertyName.equals("AcuteToxicityOral") && valueType.contentEquals("LD50") && isRat) {
//			er.property_name="Oral rat LD50";
//			er.property_category="acute oral toxicity";
//		} else {
////			System.out.println(propertyName+"\t"+valueType+"\t"+url);
//			System.out.println(propertyName+"\t"+valueType);
//		}
		//		System.out.println(er.property_name+"\t"+er.property_category);

		er.property_value_string=this.interpretationOfResults;

		
	
		
		if(er.property_name==null) return er;

//		boolean foundNumeric=ParseUtilities.getToxicity(er,experimentalValue);
//		if(foundNumeric)unitConverter.convertRecord(er);

		return er;
	}

}
