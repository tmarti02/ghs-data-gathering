package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;

/**
 * Parses downloaded results from eChemPortal API into RecordEChemPortal API objects and translates them to ExperimentalRecords
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class ParseEChemPortalAPI extends Parse {
	
	public ParseEChemPortalAPI() {
		sourceName = ExperimentalConstants.strSourceEChemPortalAPI;
		this.init();
		fileNameSourceExcel=null;
		folderNameWebpages=null;
		folderNameExcel=null;
	}
	
	private static void benchmarkDuplicateElimination() {
		QueryOptions options = new QueryOptions(ExperimentalConstants.strWaterSolubility);
		double[] downloadResults = new double[5];
		for (int i = 0; i < 5; i++) {
			long start = System.currentTimeMillis();
			options.runDownload(true, false);
			long end = System.currentTimeMillis();
			downloadResults[i] = (end - start)/1000.0;
		}
		double[] noStrategyResults = new double[5];
		double[] lastDuplicateRecResults = new double[5];
		for (int i = 0; i < 5; i++) {
			long start = System.currentTimeMillis();
			RecordEChemPortalAPI.parseResultsInDatabase(false);
			long end = System.currentTimeMillis();
			noStrategyResults[i] = (end - start)/1000.0;
			
			start = System.currentTimeMillis();
			RecordEChemPortalAPI.parseResultsInDatabase(true);
			end = System.currentTimeMillis();
			lastDuplicateRecResults[i] = (end - start)/1000.0;
		}
		
		double[] downloadSortedResults = new double[5];
		for (int i = 0; i < 5; i++) {
			long start = System.currentTimeMillis();
			options.runDownload(true, true);
			long end = System.currentTimeMillis();
			downloadSortedResults[i] = (end - start)/1000.0;
		}
		double[] lastDuplicateRecSortedResults = new double[5];
		for (int i = 0; i < 5; i++) {
			long start = System.currentTimeMillis();
			RecordEChemPortalAPI.parseResultsInDatabase(true);
			long end = System.currentTimeMillis();
			lastDuplicateRecSortedResults[i] = (end - start)/1000.0;
		}
		
		System.out.println("Unsorted download speeds: "+Arrays.toString(downloadResults));
		System.out.println("Sorted download speeds: "+Arrays.toString(downloadSortedResults));
		System.out.println("Naive elimination speeds: "+Arrays.toString(noStrategyResults));
		System.out.println("Unsorted, last duplicate record elimination speeds: "+Arrays.toString(lastDuplicateRecResults));
		System.out.println("Sorted, last duplicate record elimination speeds: "+Arrays.toString(lastDuplicateRecSortedResults));
	}
	
	/**
	 * Parses JSON entries in database to RecordPubChem objects, then saves them to a JSON file
	 */
	@Override
	protected void createRecords() {
		List<RecordEChemPortalAPI> records = RecordEChemPortalAPI.parseResultsInDatabase(false);
		writeOriginalRecordsToFile(new Vector<RecordEChemPortalAPI>(records));
	}
	
	/**
	 * Reads the JSON file created by createRecords() and translates it to an ExperimentalRecords object
	 */
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordEChemPortalAPI[] recordsEChemPortalAPI = gson.fromJson(new FileReader(jsonFile), RecordEChemPortalAPI[].class);
			
			for (int i = 0; i < recordsEChemPortalAPI.length; i++) {
				RecordEChemPortalAPI r = recordsEChemPortalAPI[i];
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	/**
	 * Creates and adds the ExperimentalRecord corresponding to a given RecordEChemPortalAPI object
	 * @param r			The RecordEChemPortalAPI object to translate
	 * @param records	The ExperimentalRecords to add created record to
	 */
	private void addExperimentalRecords(RecordEChemPortalAPI r, ExperimentalRecords records) {
		ExperimentalRecord er = new ExperimentalRecord();
		er.source_name = sourceName;
		er.url = r.endpointURL;
		er.original_source_name = r.participantAcronym;
		er.date_accessed = r.dateAccessed;
		er.reliability = r.reliability;
		
		if (!r.name.equals("-") && !r.name.contains("unnamed")) {
			er.chemical_name = r.name;
		}
		
		if (r.numberType!=null) {
			switch (r.numberType) {
			case "CAS Number":
				er.casrn = r.number;
				break;
			case "EC Number":
				er.einecs = r.number;
				break;
			}
		}
		
		switch (r.endpointKind) {
		case "Melting":
			er.property_name = ExperimentalConstants.strMeltingPoint;
			getTemperatureProperty(er,r.value);
			break;
		case "BoilingPoint":
			er.property_name = ExperimentalConstants.strBoilingPoint;
			getTemperatureProperty(er,r.value);
			break;
		case "FlashPoint":
			er.property_name = ExperimentalConstants.strFlashPoint;
			getTemperatureProperty(er,r.value);
			break;
		case "Density":
			er.property_name = ExperimentalConstants.strDensity;
			getDensity(er,r.value);
			break;
		case "Vapour":
			er.property_name = ExperimentalConstants.strVaporPressure;
			getVaporPressure(er,r.value);
			break;
		case "Partition":
			er.property_name = ExperimentalConstants.strLogKow;
			getLogProperty(er,r.value);
			break;
		case "WaterSolubility":
			er.property_name = ExperimentalConstants.strWaterSolubility;
			getWaterSolubility(er,r.value);
			break;
		case "DissociationConstant":
			er.property_name = ExperimentalConstants.str_pKA;
			getLogProperty(er,r.value);
			break;
		case "HenrysLawConstant":
			er.property_name = ExperimentalConstants.strHenrysLawConstant;
			getHenrysLawConstant(er,r.value);
			break;
		}
		er.property_value_string = "Value: "+r.value;
		
		if (r.pressure!=null) {
			getPressureCondition(er,r.pressure);
			er.property_value_string = er.property_value_string + ";Pressure: " + r.pressure;
		}
		
		if (r.temperature!=null) {
			try {
				er.temperature_C = Double.parseDouble(r.temperature);
			} catch (NumberFormatException ex) {
				getTemperatureCondition(er,r.temperature);
			}
			er.property_value_string = er.property_value_string + ";Temperature: " + r.temperature;
		}
		
		// Handles all kinds of weird pH formatting
		if (r.pH!=null) {
			String pHStr = r.pH;
			er.property_value_string = er.property_value_string + ";pH: " + pHStr;
			boolean foundpH = false;
			try {
				double[] range = Parse.extractFirstDoubleRangeFromString(pHStr,pHStr.length());
				er.pH = range[0]+"-"+range[1];
				foundpH = true;
			} catch (Exception ex) { }
			if (!foundpH) {
				try {
					double[] range = Parse.extractAltFormatRangeFromString(pHStr,pHStr.length());
					er.pH = range[0]+"-"+range[1];
					foundpH = true;
				} catch (Exception ex) { }
			}
			if (!foundpH) {
				try {
					Matcher caMatcher = Pattern.compile(".*?(ca. )?([-]?[ ]?[0-9]*\\.?[0-9]+)( ca. )([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(pHStr);
					if (caMatcher.find()) {
						String numQual = caMatcher.group(1).isBlank() ? "" : "~";
						er.pH = numQual+Double.parseDouble(caMatcher.group(2))+"~"+Double.parseDouble(caMatcher.group(4));
						foundpH = true;
					}
				} catch (Exception ex) { }
			}
			// Handles J-CHECK entries with multiple discrete pH values
			if (!foundpH && pHStr.contains(",") && !pHStr.endsWith(",")) {
				er.pH = pHStr;
				foundpH = true;
			}
			if (!foundpH) {
				try {
					double pHDouble = Parse.extractDoubleFromString(pHStr,pHStr.length());
					String pHDoubleStr = Double.toString(pHDouble);
					String numQual = "";
					if (pHDouble >= 0 && pHDouble < 1) {
						numQual = getNumericQualifier(pHStr,pHStr.indexOf("0"));
					} else {
						numQual = getNumericQualifier(pHStr,pHStr.indexOf(pHDoubleStr.charAt(0)));
					}
					er.pH = numQual+pHDoubleStr;
					foundpH = true;
				} catch (Exception ex) { }
			}
		}
		
		er.finalizeUnits();
		
		if ((er.casrn==null || er.casrn.isBlank()) && (er.einecs==null || er.einecs.isBlank()) &&
				(er.chemical_name==null || er.chemical_name.isBlank()) && (er.smiles==null || er.smiles.isBlank())) {
			er.keep = false;
			er.reason = "No identifiers";
		} else {
			er.keep = true;
			er.reason = null;
		}
		
		records.add(er);
	}

	public static void main(String[] args) {
		ParseEChemPortalAPI p = new ParseEChemPortalAPI();
		p.createFiles();
		
//		benchmarkDuplicateElimination();
	}
}
