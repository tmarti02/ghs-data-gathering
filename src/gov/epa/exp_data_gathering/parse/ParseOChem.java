package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

/**
 * Parses data from ochem.eu
 * @author GSINCL01
 *
 */
public class ParseOChem extends Parse {
	Map<String,Double> hmTEST;

	public ParseOChem() {
		sourceName = ExperimentalConstants.strSourceOChem;
		this.init();
		hmTEST = new HashMap<String,Double>();
		List<String> lines = gov.epa.QSAR.utilities.FileUtilities.readFile("Data\\Experimental\\OChem\\excel files\\TESTRecordsToRemove.csv");
		for (int i = 1; i < lines.size(); i++) {
			String[] cells = lines.get(i).split(",");
			hmTEST.put(cells[0], Double.parseDouble(cells[2]));
		}
	}
	
	@Override
	protected void createRecords() {
		Vector<RecordOChem> records = RecordOChem.parseOChemQueriesFromExcel();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordOChem[] recordsOChem = gson.fromJson(new FileReader(jsonFile), RecordOChem[].class);
			
			int countRemoved = 0;
			for (int i = 0; i < recordsOChem.length; i++) {
				RecordOChem rec = recordsOChem[i];
				boolean removed = addExperimentalRecords(rec,recordsExperimental);
				if (removed) countRemoved++;
			}
			System.out.println("Removed "+countRemoved+" records with sign errors");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return recordsExperimental;
	}
	
	private boolean addExperimentalRecords(RecordOChem ocr,ExperimentalRecords records) {
		ExperimentalRecord er = new ExperimentalRecord();
		er.date_accessed = RecordOChem.lastUpdated;
		er.casrn = ocr.casrn;
		er.smiles = ocr.smiles;
		er.chemical_name = ocr.name;
		er.measurement_method = ocr.measurementMethod;
		er.source_name = ExperimentalConstants.strSourceOChem;
		er.url = "https://ochem.eu/home/show.do"; // How do we get individual OChem URLs?
		er.property_value_string = "Value: "+ocr.propertyValue+" "+ocr.propertyUnit;
		
		switch (ocr.propertyName.trim()) {
		case "melting point":
			er.property_name = ExperimentalConstants.strMeltingPoint;
			break;
		case "boiling point":
			er.property_name = ExperimentalConstants.strBoilingPoint;
			break;
		case "flash point":
			er.property_name = ExperimentalConstants.strFlashPoint;
			break;
		case "density":
			er.property_name = ExperimentalConstants.strDensity;
			break;
		case "water solubility":
			er.property_name = ExperimentalConstants.strWaterSolubility;
			break;
		case "vapor pressure":
			er.property_name = ExperimentalConstants.strVaporPressure;
			break;
		case "pka":
			er.property_name = ExperimentalConstants.str_pKA;
			break;
		case "logpow":
			er.property_name = ExperimentalConstants.strLogKow;
			break;
		case "henry's law constant":
			er.property_name = ExperimentalConstants.strHenrysLawConstant;
			break;
		}
		switch (ocr.propertyUnit.trim()) {
		case "Celsius":
			er.property_value_units_original = ExperimentalConstants.str_C;
			break;
		case "g/cm3":
			er.property_value_units_original = ExperimentalConstants.str_g_cm3;
			break;
		case "g/L":
			er.property_value_units_original = ExperimentalConstants.str_g_L;
			break;
		case "Log unit":
			break;
		case "m^(3)*Pa/mol":
			er.property_value_units_original = ExperimentalConstants.str_Pa_m3_mol;
			break;
		case "mm Hg":
			er.property_value_units_original = ExperimentalConstants.str_mmHg;
			break;
		}
		er.property_value_point_estimate_original = Double.parseDouble(ocr.propertyValue);
		if (ocr.temperature!=null && !ocr.temperature.isBlank()) {
			er.property_value_string = er.property_value_string + "; Temperature: " + ocr.temperature + " " + ocr.temperatureUnit;
			String cleanTemp = ocr.temperature.replaceAll("[^0-9.,E]","");
			double temp = 0.0;
			try {
				temp = Double.parseDouble(cleanTemp);
			} catch (NumberFormatException ex) {
				ocr.temperatureUnit = "";
			}
			if (ocr.temperatureUnit.contains("C")) {
				er.temperature_C = temp;
			} else if (ocr.temperatureUnit.contains("K")) {
				er.temperature_C = UnitConverter.K_to_C(temp);
			} else if (ocr.temperatureUnit.contains("F")) {
				er.temperature_C = UnitConverter.F_to_C(temp);
			}
		}
		if (ocr.pressure!=null && !ocr.pressure.isBlank()) {
			er.property_value_string = er.property_value_string + "; Pressure: " + ocr.pressure + " " + ocr.pressureUnit;
			double pressure = Double.parseDouble(ocr.pressure.replaceAll("[^0-9.,E]",""));
			if (ocr.pressureUnit.contains("mm Hg") || ocr.pressureUnit.contains("Torr")) {
				er.pressure_mmHg = ParseUtilities.formatDouble(pressure);
			} else if (ocr.pressureUnit.contains("atm")) {
				er.pressure_mmHg = ParseUtilities.formatDouble(pressure*UnitConverter.atm_to_mmHg);
			} else if (ocr.pressureUnit.contains("Pa")) {
				er.pressure_mmHg = ParseUtilities.formatDouble(pressure*UnitConverter.Pa_to_mmHg);
			}
		}
		if (ocr.pH!=null && !ocr.pH.isBlank()) {
			er.property_value_string = er.property_value_string + "; pH: " + ocr.pH;
			er.pH = ocr.pH;
		}
		
		boolean removed = false;
		if (er.property_name.equals(ExperimentalConstants.strWaterSolubility) && er.casrn!=null) {
			Double trueWS = hmTEST.get(er.casrn);
			if (trueWS!=null) {
				Double logTrueWS = Math.log10(trueWS/1000.0);
				Double logNewWS = Math.log10(er.property_value_point_estimate_original);
				if (Math.abs(logNewWS-(-logTrueWS))<0.5) {
					er.keep = false;
					er.reason = "Sign error in OChem record";
					removed = true;
				}
			}
		}
		
		if (!ParseUtilities.hasIdentifiers(er)) {
			er.keep = false;
			er.reason = "No identifiers";
		}
		
		if (er.measurement_method!=null && er.measurement_method.contains("est")) {
			er.updateNote(ExperimentalConstants.str_est);
			er.keep = false;
			er.reason = "Estimated";
		}
		
		uc.convertRecord(er);
		records.add(er);
		return removed;
	}
	
	public static void main(String[] args) {
		ParseOChem p = new ParseOChem();
		p.createFiles();
	}
}
