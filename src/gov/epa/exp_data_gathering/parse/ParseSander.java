package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.api.Chemical;
import gov.epa.api.ExperimentalConstants;

public class ParseSander extends Parse {
	
	public ParseSander() {
		sourceName = "Sander";
		this.init();
	}
	@Override
	protected void createRecords() {
		Vector<RecordSander> records = RecordSander.parseWebpagesInDatabase();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordSander[] recordsSander = gson.fromJson(new FileReader(jsonFile), RecordSander[].class);
			
			for (int i = 0; i < recordsSander.length; i++) {
				RecordSander rec = recordsSander[i];
				addExperimentalRecords(rec,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return recordsExperimental;
	}
	// get the names right
	/**
	 * populates experimentalrecord fields with data from the recordSander object.
	 * @param rs
	 * @param records
	 */
	private void addExperimentalRecords(RecordSander rs,ExperimentalRecords records) {
			String CAS = rs.CASRN;
			for (int i = 0; i < rs.recordCount; i++) {
				if (!(rs.hcp.get(i).isBlank())) {
				ExperimentalRecord er = new ExperimentalRecord();
				er.date_accessed = rs.date_accessed;
				er.keep = true;
				er.url = rs.url;
				er.casrn = CAS;
				if (er.casrn.contains("???")) {
					er.casrn = "";
				}
				er.property_value_string = rs.hcp.get(i);
				er.chemical_name = rs.chemicalName.replace("? ? ? ", "");
				er.property_name = ExperimentalConstants.strHenrysLawConstant;
				String propertyValue = rs.hcp.get(i);
				er.property_value_units_original = "mol/m3-Pa";
				er.property_value_units_final = ExperimentalConstants.str_atm_m3_mol;
				getnumericalhcp(er, propertyValue);
				// below converts Sander's weird inverted units to atm*m3/mol
				if (!(er.property_value_point_estimate_original == null)) {
				er.property_value_point_estimate_final = 1/(er.property_value_point_estimate_original*101325);
				}
				er.temperature_C = (double)25;
				er.pressure_mmHg = "760";
				er.source_name = rs.referenceAbbreviated.get(i);
				getnotes(er,rs.type.get(i));
				records.add(er);
				}
			}
	}
	
	public static void main(String[] args) {
		ParseSander p = new ParseSander();
		p.mainFolder = p.mainFolder + File.separator + "General";
		p.databaseFolder = p.mainFolder;
		p.jsonFolder= p.mainFolder;
		p.createFiles();
	}
	
	
	/**
	 * converts strings of the form 5.8×10-4 to the correct value as a double.
	 * @param er
	 * @param propertyValue
	 */
	public static void getnumericalhcp(ExperimentalRecord er, String propertyValue) {
		Matcher sanderhcpMatcher = Pattern.compile("([0-9]*\\.?[0-9]+)(\\×10(\\-)?([0-9]+))?").matcher(propertyValue);
		if (sanderhcpMatcher.find()) {
		String strMantissa = sanderhcpMatcher.group(1);
		String strNegMagnitude = sanderhcpMatcher.group(3);
		String strMagnitude = sanderhcpMatcher.group(4);
		if (!(strMagnitude == null)){
			if (!(strNegMagnitude == null)) { // ? corresponds to negative magnitude (e.g. 3.4 * 10^-4), otherwise positive
				Double mantissa = Double.parseDouble(strMantissa.replaceAll("\\s",""));
				Double magnitude =  Double.parseDouble(strMagnitude.replaceAll("\\s","").replaceAll("\\+", ""));
				er.property_value_point_estimate_original = mantissa*Math.pow(10, (-1)*magnitude);
			} else {
				Double mantissa = Double.parseDouble(strMantissa.replaceAll("\\s",""));
				Double magnitude =  Double.parseDouble(strMagnitude.replaceAll("\\s","").replaceAll("\\+", ""));
				er.property_value_point_estimate_original = mantissa*Math.pow(10, magnitude);
			}
		}
		else {
			er.property_value_point_estimate_original = Double.parseDouble(strMantissa.replaceAll("\\s",""));
		}
		}
	}
	
	/**
	 * Keeps the Henry's law constants that were derived by measurement, VP/AS, literature, or citation.
	 * @param er
	 * @param type
	 */
	public static void getnotes(ExperimentalRecord er, String type) {
		if (type.contains("L")) {
			er.note = "literature review";
		}
		else if (type.contains("M")) {
			er.note = "measured";
			er.keep = true;
		}
		else if (type.contains("V")) {
			er.note = "VP/AS = vapor pressure/aqueous solubility";
			er.keep = true;
		}
		else if (type.contains("R")) {
			er.note = "recalculation";
			er.keep = false;
			er.reason = "recalculation";
		}
		else if (type.contains("T")) {
			er.note = "thermodynamical calculation";
			er.keep = false;
			er.reason= "thermodynamical calculation";
		}
		else if (type.contains("X")) {
			er.reason = "original paper not available";
			er.keep = true;
		}
		else if (type.contains("C")) {
			er.note = "citation";
			er.keep = true;
		}
		else if (type.contains("Q")) {
			er.note="QSPR";
			er.reason = "QSPR";
			er.keep = false;
		}
		else if (type.contains("E")) {
			er.note="estimate";
			er.reason = "estimate";
			er.keep=false;
		}
		else if (type.contains("?")) {
			er.note = "unknown";
			er.reason = "unknown";
			er.keep=false;
		}
		else if (type.contains("W")) {
			er.note = "wrong";
			er.reason = "wrong";
			er.keep=false;
		}
		else {
			er.note = "";
		}
	}
	
}
