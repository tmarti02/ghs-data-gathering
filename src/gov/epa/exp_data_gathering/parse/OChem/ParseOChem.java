package gov.epa.exp_data_gathering.parse.OChem;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.UnitConverter;

/**
 * Parses data from ochem.eu
 * @author GSINCL01
 *
 */
public class ParseOChem extends Parse {

	public ParseOChem() {
		sourceName = ExperimentalConstants.strSourceOChem;
		this.init();
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
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordOChem> recordsOChem = new ArrayList<RecordOChem>();
			RecordOChem[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordOChem[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsOChem.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordOChem[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsOChem.add(tempRecords[i]);
					}
				}
			}
			
			Iterator<RecordOChem> it = recordsOChem.iterator();
			while (it.hasNext()) {
				RecordOChem r = it.next();
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	private void addExperimentalRecords(RecordOChem ocr,ExperimentalRecords records) {
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
		
		if (ocr.articleID!=null && ocr.articleID.equals("A5643")) {
			er.keep = false;
			er.reason = "EPI Suite data";
		}
		
		if (ocr.articleID!=null && ocr.articleID.equals("A111278")) {
			er.keep = false;
			er.reason = "OPERA data";
		}
		
		if (ocr.introducer!=null && ocr.introducer.equals("charochkina") &&
		ocr.articleID!=null && ocr.articleID.equals("A120907") &&
		ocr.name != null && (ocr.casrn == null || ocr.casrn.isEmpty())) {
				er.keep = false;
				er.reason = "nonsense date name, no CAS, smiles only";
		}
		
		if (er.property_name.equals(ExperimentalConstants.strWaterSolubility) && 
				ocr.articleID!=null && ocr.articleID.equals("A108291") &&
				ocr.introducer!=null && ocr.introducer.equals("mvashurina")) {
			er.keep = false;
			er.reason = "Bad TEST upload";
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
	}
	
	public static void main(String[] args) {
		ParseOChem p = new ParseOChem();
		p.createFiles();
	}
}
