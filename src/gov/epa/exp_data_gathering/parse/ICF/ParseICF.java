package gov.epa.exp_data_gathering.parse.ICF;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.UnitConverter;

public class ParseICF extends Parse {
	
	private static Pattern bpPressure;

	public ParseICF() {
		sourceName = ExperimentalConstants.strSourceICF;
		// Compile pattern here and generate matcher later to avoid excessive computation
		bpPressure = Pattern.compile("at ([0-9]+\\.?[0-9]*) mm");
		this.init();
	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordICF.parseICFRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordICF> recordsICF = new ArrayList<RecordICF>();
			RecordICF[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordICF[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsICF.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordICF[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsICF.add(tempRecords[i]);
					}
				}
			}
			
			Iterator<RecordICF> it = recordsICF.iterator();
			while (it.hasNext()) {
				RecordICF r = it.next();
				addExperimentalRecord(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	private void addExperimentalRecord(RecordICF r, ExperimentalRecords recordsExperimental) {
		
		ExperimentalRecord er = new ExperimentalRecord();
		er.casrn = r.casrn;
		if (er.casrn!=null && er.casrn.equals("unsure")) { er.casrn = null; }
		er.chemical_name = r.chemical_name;
		er.synonyms = r.synonyms;
		er.url = r.url;
		
		er.source_name = ExperimentalConstants.strSourceICF;
		if (r.Record_source_level.equals("Primary")) {
			er.reference = r.long_ref;
		} else if (r.Record_source_level.equals("Secondary")) {
			er.reference = r.Record_source_primary;
		}
		
		switch (r.property_name) {
		case "boiling point":
			er.property_name = ExperimentalConstants.strBoilingPoint;
			break;
		case "henry's law constant":
			er.property_name = ExperimentalConstants.strHenrysLawConstant;
			break;
		case "log Kow":
			er.property_name = ExperimentalConstants.strLogKow;
			break;
		case "pKA":
			er.property_name = ExperimentalConstants.str_pKA;
			break;
		case "vapor pressure":
			er.property_name = ExperimentalConstants.strVaporPressure;
			break;
		case "water solubility":
			er.property_name = ExperimentalConstants.strWaterSolubility;
			break;
		default:
			return; // TODO what do we do about pKa1 and pKa2?
		}
		
		if (r.property_value_point_estimate!=null && !r.property_value_point_estimate.isBlank()) {
			er.property_value_point_estimate_original = Double.parseDouble(r.property_value_point_estimate);
			er.property_value_string = "Value: " + r.property_value_point_estimate;
		} else if (r.property_value_min!=null && !r.property_value_min.isBlank() && r.property_value_max!=null && !r.property_value_max.isBlank()) {
			er.property_value_min_original = Double.parseDouble(r.property_value_min);
			er.property_value_max_original = Double.parseDouble(r.property_value_max);
			er.property_value_string = "Value: " + r.property_value_min + "-" + r.property_value_max;
		} else if (r.property_value_max!=null && !r.property_value_max.isBlank()) {
			er.property_value_point_estimate_original = Double.parseDouble(r.property_value_max);
			er.property_value_numeric_qualifier = "<";
			er.property_value_string = "Value: <" + r.property_value_max;
		} else if (r.property_value_min!=null && !r.property_value_min.isBlank()) {
			er.property_value_point_estimate_original = Double.parseDouble(r.property_value_min);
			er.property_value_numeric_qualifier = ">";
			er.property_value_string = "Value: >" + r.property_value_min;
		}
		
		if (r.property_value_units!=null) {
			switch (r.property_value_units) {
			case "mm Hg":
				er.property_value_units_original = ExperimentalConstants.str_mmHg;
				er.property_value_string += " mm Hg";
				break;
			case "mg/L":
				er.property_value_units_original = ExperimentalConstants.str_mg_L;
				er.property_value_string += " mg/L";
				break;
			case "C":
				er.property_value_units_original = ExperimentalConstants.str_C;
				er.property_value_string += " C";
				break;
			case "atm-m^3/mol":
				er.property_value_units_original = ExperimentalConstants.str_atm_m3_mol;
				er.property_value_string += " atm-m^3/mol";
				break;
			}
		}
		
		if (r.temperature_C!=null && !r.temperature_C.isBlank()) {
			er.temperature_C = Double.parseDouble(r.temperature_C);
			er.property_value_string += "; Temp: " + r.temperature_C + " C";
		}
		
		if (r.pressure_kPa!=null && !r.pressure_kPa.isBlank() && !er.property_name.equals(ExperimentalConstants.strVaporPressure)) {
			double p = Double.parseDouble(r.pressure_kPa);
			er.pressure_mmHg = ParseUtilities.formatDouble(p*UnitConverter.kPa_to_mmHg);
			er.property_value_string += "; Pressure: " + r.pressure_kPa + " kPa";
		} else if (r.pressure_atm!=null && !r.pressure_atm.isBlank() && !er.property_name.equals(ExperimentalConstants.strVaporPressure)) {
			double p = Double.parseDouble(r.pressure_atm);
			er.pressure_mmHg = ParseUtilities.formatDouble(p*UnitConverter.atm_to_mmHg);
			er.property_value_string += "; Pressure: " + r.pressure_atm + " atm";
		} else if (er.property_name.equals(ExperimentalConstants.strBoilingPoint) && r.note!=null) {
			Matcher bpPressureMatcher = bpPressure.matcher(r.note);
			if (bpPressureMatcher.find()) {
				double p = Double.parseDouble(bpPressureMatcher.group(1));
				er.pressure_mmHg = ParseUtilities.formatDouble(p);
				er.property_value_string += "; Pressure: " + bpPressureMatcher.group();
			}
		}
		
		if (r.pH!=null && !r.pH.isBlank()) {
			er.pH = r.pH;
			er.property_value_string += "; pH: " + r.pH;
		}
		
		if (r.measurement_method!=null && !r.measurement_method.isBlank()) {
			er.measurement_method = r.measurement_method;
		}
		
		if (r.note!=null && !r.note.isBlank()) {
			er.note = r.note;
		}
		
		if (r.Uncertainty!=null && !r.Uncertainty.isBlank()) {
			er.updateNote("Uncertainty: (+/-) " + ParseUtilities.formatDouble(Double.parseDouble(r.Uncertainty)));
		}
		
		if (er.note!=null) {
			if (er.note.contains("seawater") || er.note.toLowerCase().contains("nacl solution")) {
				er.keep = false;
				er.reason = "Non-aqueous solubility";
			}
			
			if (er.note.contains("salt")) {
				er.keep = false;
				er.reason = "Solubility for salt form with wrong CAS";
			}
			
			if (er.note.contains("not found in cited source")) {
				er.keep = false;
				er.reason = "Not found in cited source";
			}
			
			if (er.note.contains("Author suggests this method inaccurate")) {
				er.keep = false;
				er.reason = "Author suggests this method inaccurate";
			}
			
			if (er.note.contains("Unclear if experimental")) {
				er.keep = false;
				er.reason = "Unclear if experimental";
			}
		}
		
		uc.convertRecord(er);
		recordsExperimental.add(er);
	}
	
	public static void main(String[] args) {
		ParseICF p = new ParseICF();
		p.createFiles();
	}
}