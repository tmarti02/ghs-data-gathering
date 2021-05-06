package gov.epa.exp_data_gathering.parse.Burkhard;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.exec.util.StringUtils;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.UnitConverter;



class FinalRecord2{
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

}


public class ParseBurkhard extends Parse {

	public ParseBurkhard() {
		sourceName = "Burkhard"; // TODO Consider creating ExperimentalConstants.strSourceBurkhard instead.
		this.init();

		// TODO Is this a toxicity source? If so, rename original and experimental records files here.
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordBurkhard.parseBurkhardRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			List<RecordBurkhard> recordsBurkhard = new ArrayList<RecordBurkhard>();
			RecordBurkhard[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordBurkhard[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsBurkhard.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordBurkhard[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsBurkhard.add(tempRecords[i]);
					}
				}
			}

			Iterator<RecordBurkhard> it = recordsBurkhard.iterator();
			while (it.hasNext()) {
				RecordBurkhard r = it.next();
				addExperimentalRecord(r,recordsExperimental);
				// TODO Write addExperimentalRecord() method to parse this source.
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}
	
	
	
	private static void addExperimentalRecord(RecordBurkhard rb, ExperimentalRecords records) {
		ExperimentalRecord er = new ExperimentalRecord();
		records.add(er);
	
	}
	
	public static void main(String[] args) {
		ParseBurkhard p = new ParseBurkhard();
		p.createFiles();
	}
}