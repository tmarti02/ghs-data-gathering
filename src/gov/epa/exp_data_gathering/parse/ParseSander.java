package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.api.ExperimentalConstants;

public class ParseSander extends Parse{
	
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
	private void addExperimentalRecords(RecordSander rs,ExperimentalRecords records) {
			String CAS = rs.CASRN;
			String inchikey = rs.inchiKey;
			for (int i = 0; i < rs.recordCount; i++) {
				if (!(rs.hcp.get(i).isBlank())) {
				ExperimentalRecord er = new ExperimentalRecord();
				er.url = rs.url;
				er.casrn = CAS;
				er.chemical_name = rs.chemicalName.replace("? ? ? ", "");
				er.property_name = ExperimentalConstants.strHenrysLawConstant;
				er.property_value_string = rs.hcp.get(i);
				String propertyValue = er.property_value_string;
				er.property_value_units_original = "mol/m3-Pa";
				er.property_value_units_final = ExperimentalConstants.str_atm_m3_mol;
				getnumericalhcp(er, propertyValue);
				er.property_value_point_estimate_final = 1/(er.property_value_point_estimate_original*101325);
				getnotes(er,rs.type.get(i));
				er.keep = true;
				er.temperature_C = (double)25;
				er.pressure_mmHg = "760";
				er.source_name = rs.referenceAbbreviated.get(i);
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
	
	
	public static void getnumericalhcp(ExperimentalRecord er, String propertyValue) {
		Matcher sanderhcpMatcher = Pattern.compile("([0-9]*\\.?[0-9]+)(\\×10(\\?)?([0-9]+))?").matcher(propertyValue);
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

	public static void getnotes(ExperimentalRecord er, String type) {
		if (type.contains("L")) {
			er.note = "literature review";
		}
		else if (type.contains("M")) {
			er.note = "measured";
		}
		else if (type.contains("V")) {
			er.note = "VP/AS = vapor pressure/aqueous solubility";
		}
		else if (type.contains("R")) {
			er.note = "recalculation";
		}
		else if (type.contains("")) {
			er.note = "thermodynamical calculation";
		}
		else if (type.contains("X")) {
			er.note = "original paper not available";
		}
		else if (type.contains("C")) {
			er.note = "citation";
		}
		else if (type.contains("Q")) {
			er.note = "QSPR";
		}
		else if (type.contains("E")) {
			er.note = "estimate";
		}
		else if (type.contains("?")) {
			er.note = "unknown";
		}
		else if (type.contains("W")) {
			er.note = "wrong";
		}
		else {
			er.note = "";
		}
	}

}
