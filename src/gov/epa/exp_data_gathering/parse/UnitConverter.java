package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Function;

import gov.epa.api.ExperimentalConstants;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

public class UnitConverter {
	Hashtable<String, Double> htDensity = new Hashtable<String, Double>(); // density look up table, densities in g/ml
	
	public static final double airDensitySTP = 1.2041/1000.0;
	public static final double kPa_to_mmHg=7.50062;
	public static final double atm_to_mmHg=760.0;
	public static final double psi_to_mmHg=51.7149;
	public static final double hPa_to_mmHg=0.750061;
	public static final double Pa_to_mmHg=0.00750062;
	public static final double bar_to_mmHg=750.062;
	public static final double atm_to_Pa=101325.0;
	public static final double Pa_to_atm=1.0/101325.0;
	
	public static double F_to_C(double F) {
		return (F-32.0)*5.0/9.0;
	}
	
	private static void F_to_C(ExperimentalRecord er) {
		if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = F_to_C(er.property_value_point_estimate_original); }
		if (er.property_value_min_original!=null) { er.property_value_min_final = F_to_C(er.property_value_min_original); }
		if (er.property_value_max_original!=null) { er.property_value_max_final = F_to_C(er.property_value_max_original); }
	}
	
	public static double K_to_C(double K) {
		return K-273.15;
	}
	
	private static void K_to_C(ExperimentalRecord er) {
		if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = K_to_C(er.property_value_point_estimate_original); }
		if (er.property_value_min_original!=null) { er.property_value_min_final = K_to_C(er.property_value_min_original); }
		if (er.property_value_max_original!=null) { er.property_value_max_final = K_to_C(er.property_value_max_original); }
	}
	
	/**
	 * Loads density values and stores in a hashtable
	 */
	public UnitConverter(String densityFilePath) {
		ArrayList<String> lines = Utilities.readFileToArray(densityFilePath);
		for (int i = 1; i < lines.size(); i++) {// first line is header
			// System.out.println(lines.get(i));
			String[] vals = lines.get(i).split("\t");

			String CAS = vals[0];
			String strDensity = vals[1];

			htDensity.put(CAS, Double.parseDouble(strDensity));
		}
		// System.out.println(htDensity.get("7487-28-7"));
	}
	
	/**
	 * Converts to final units and assigns point estimates for any ranges within tolerance:
	 * @param er - ExperimentalRecord to convert units and store final values
	 * (Also does checksum and fixes leading zeroes in casrn field - convenient place to do it)
	 */
	public void convertRecord(ExperimentalRecord er) {
		er.casrn = ParseUtilities.fixCASLeadingZero(er.casrn);
		if (er.casrn!=null && !ParseUtilities.isValidCAS(er.casrn) && er.keep) {
			er.flag = true;
			er.reason = "Invalid CAS";
		} else if (er.casrn!=null && er.casrn.toLowerCase().contains("mixture")) {
			er.keep = false;
			er.reason = "Mixture";
		}
		
		if (er.property_value_numeric_qualifier!=null && er.property_value_numeric_qualifier.equals("?")) {
			er.keep = false;
			er.reason = "Undetermined numeric qualifier";
		}
		
		if (er.property_value_units_original!=null && er.property_value_units_original.equals("binary")) {
			return;
		}
				
		if (er.property_name.equals(ExperimentalConstants.str_pKA) || er.property_name.equals(ExperimentalConstants.strLogKow)) {
			assignFinalFieldsWithoutConverting(er);
		} else if ((er.property_name.equals(ExperimentalConstants.strMeltingPoint) || er.property_name.equals(ExperimentalConstants.strBoilingPoint) ||
				er.property_name.equals(ExperimentalConstants.strFlashPoint)) && er.property_value_units_original!=null) {
			convertTemperature(er);
		} else if (er.property_name.equals(ExperimentalConstants.strDensity)) {
			convertDensity(er);
		} else if (er.property_name.equals(ExperimentalConstants.strVaporPressure) && er.property_value_units_original!=null) {
			convertPressure(er);
		} else if (er.property_name.equals(ExperimentalConstants.strHenrysLawConstant) && er.property_value_units_original!=null) {
			convertHenrysLawConstant(er);
		} else if (er.property_name.equals(ExperimentalConstants.strWaterSolubility) && er.property_value_units_original!=null) {
			convertSolubility(er);
		} else if (er.property_value_units_original!=null) {
			convertToxicity(er);
		}
		
		if (er.property_value_units_final!=null && !er.property_value_units_final.isBlank() &&
				!er.property_value_units_final.equals(ExperimentalConstants.str_C) && !er.property_value_units_final.equals(ExperimentalConstants.str_dimensionless_H)) {
			if ((er.property_value_point_estimate_final!=null && er.property_value_point_estimate_final < 0) ||
					(er.property_value_min_final!=null && er.property_value_min_final < 0) ||
					(er.property_value_max_final!=null && er.property_value_max_final < 0)) {
				er.keep = false;
				er.reason = "Negative value not plausible";
			}
		} else if (er.temperature_C!=null && er.temperature_C<0) {
			er.flag = true;
			er.reason = "Negative temperature may be artifact of bad range parsing";
		}
	}
	
	/**
	 * TODO Gabriel check this- used for echemportal toxicity values...
	 * 
	 * @param er
	 * @return
	 */
	private boolean convertToxicity(ExperimentalRecord er) {
		if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_L) || er.property_value_units_original.equals(ExperimentalConstants.str_g_m3)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_mg_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_m3)) {
			convertAndAssignFinalFields(er,1.0/1000.0);
			er.property_value_units_final = ExperimentalConstants.str_mg_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mL_m3)) {
			if (er.casrn==null || htDensity.get(er.casrn) == null) {
				er.flag = true;
				er.updateNote("Conversion to mg/L not possible (missing density)");
				assignFinalFieldsWithoutConverting(er);
				er.property_value_units_final = er.property_value_units_original;
				return false;
			} else {
				double density = htDensity.get(er.casrn);
				convertAndAssignFinalFields(er,density);
				er.property_value_units_final = ExperimentalConstants.str_mg_L;
				er.updateNote("Converted using density: "+density+" g/mL");
			}
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ppm)) {
//			if (er.Structure_MolWt!=null) {								
//				conversionFactor = 0.001*er.Structure_MolWt/24.45;
//				er.property_value_units_final = ExperimentalConstants.str_mg_L;
//			} else {
//				er.flag = true;
//				er.updateNote("Conversion to mg/L not possible (need MW)");
//			}
			er.flag = true;
			er.updateNote("Conversion to mg/L not possible (need MW)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_kg)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_mg_kg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_kg)) {
			convertAndAssignFinalFields(er,1000.0);
			er.property_value_units_final = ExperimentalConstants.str_mg_kg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mL_kg)) {
			if (er.casrn==null || htDensity.get(er.casrn) == null) {
				er.flag = true;
				er.updateNote("Conversion to mg/kg not possible (missing density)");
				assignFinalFieldsWithoutConverting(er);
				er.property_value_units_final = er.property_value_units_original;
				return false;
			} else {
				double density = htDensity.get(er.casrn);
				convertAndAssignFinalFields(er,density*1000.0);
				er.property_value_units_final = ExperimentalConstants.str_mg_kg;
				er.updateNote("Converted using density: "+density+" g/mL");
			}
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg)) {
			er.flag = true;
			er.updateNote("Conversion to mg/kg not possible (dimensions differ)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;
		} else {
			er.flag = true;
			er.updateNote("Conversion not possible (unknown units)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;
			System.out.println("Unrecognized units for "+er.property_name+": "+er.property_value_units_original);
		}

		return !er.flag;
	}
	
	private static void convertTemperature(ExperimentalRecord er) {
		if (er.property_value_units_original.equals(ExperimentalConstants.str_C)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_C;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_F)) {
			F_to_C(er);
			er.property_value_units_final = ExperimentalConstants.str_C;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_K)) {
			K_to_C(er);
			er.property_value_units_final = ExperimentalConstants.str_C;
		} else {
			er.flag = true;
			er.updateNote("Conversion to C not possible (unknown units)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;
			System.out.println("Unrecognized units for "+er.property_name+": "+er.property_value_units_original);
		}
	}
	
	private static void convertDensity(ExperimentalRecord er) {
		if (er.property_value_units_original!=null && (er.property_value_units_original.equals(ExperimentalConstants.str_g_cm3) ||
				er.property_value_units_original.equals(ExperimentalConstants.str_g_mL))) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_g_cm3;
		} else if (er.property_value_units_original==null && er.note!=null && (er.note.contains(ExperimentalConstants.str_relative_density))) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_g_cm3;
		} else if (er.property_value_units_original!=null && (er.property_value_units_original.equals(ExperimentalConstants.str_kg_m3) ||
				er.property_value_units_original.equals(ExperimentalConstants.str_g_L))) {
			convertAndAssignFinalFields(er,1.0/1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_cm3;
		} else if (er.property_value_units_original==null && er.note!=null && (er.note.contains(ExperimentalConstants.str_relative_gas_density) ||
				er.note.contains(ExperimentalConstants.str_relative_mixture_density))) {
			convertAndAssignFinalFields(er,airDensitySTP);
			er.property_value_units_final = ExperimentalConstants.str_g_cm3;
		} else if (er.property_value_point_estimate_original!=null || er.property_value_min_original!=null) {
			er.flag = true;
			er.updateNote("Conversion to g/cm3 not possible (unknown units)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;
			System.out.println("Unrecognized units for "+er.property_name+": "+er.property_value_string);
		}
	}
	
	private static void convertPressure(ExperimentalRecord er) {
		if (er.property_value_units_original.equals(ExperimentalConstants.str_log_mmHg)) {
			powAndAssignFinalFields(er);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mmHg) || er.property_value_units_original.equals(ExperimentalConstants.str_torr)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_atm)) {
			convertAndAssignFinalFields(er,atm_to_mmHg);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_hpa) || er.property_value_units_original.equals(ExperimentalConstants.str_mbar)) {
			convertAndAssignFinalFields(er,hPa_to_mmHg);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_pa)) {
			convertAndAssignFinalFields(er,Pa_to_mmHg);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_bar)) {
			convertAndAssignFinalFields(er,bar_to_mmHg);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_psi)) {
			convertAndAssignFinalFields(er,psi_to_mmHg);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_kpa)) {
			convertAndAssignFinalFields(er,kPa_to_mmHg);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else {
			er.flag = true;
			er.updateNote("Conversion to mmHg not possible (unknown units)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;
			System.out.println("Unrecognized units for "+er.property_name+": "+er.property_value_units_original);
		}
	}
	
	private static boolean convertSolubility(ExperimentalRecord er) {
		if (er.property_value_units_original.equals(ExperimentalConstants.str_g_L) || er.property_value_units_original.equals(ExperimentalConstants.str_mg_mL) ||
				er.property_value_units_original.equals(ExperimentalConstants.str_kg_m3)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_mL)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_log_mg_L)) {
			powConvertAndAssignFinalFields(er,1.0/1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_M)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_log_M)) {
			powAndAssignFinalFields(er);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mM)) {
			convertAndAssignFinalFields(er,1.0/1000.0);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_uM)) {
			convertAndAssignFinalFields(er,1.0/1000000.0);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_nM)) {
			convertAndAssignFinalFields(er,1.0/1000000000.0);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_L) || er.property_value_units_original.equals(ExperimentalConstants.str_ug_mL)) {
			convertAndAssignFinalFields(er,1.0/1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ppm)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_ppm;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ug_L)) {
			convertAndAssignFinalFields(er,1.0/1000000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_100mL)) {
			convertAndAssignFinalFields(er,10.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_pctWt)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_pctWt;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_pct)) {
			convertAndAssignFinalFields(er,10.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_100mL)) {
			convertAndAssignFinalFields(er,1.0/100.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_10mL)) {
			convertAndAssignFinalFields(er,1.0/10.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_10mL)) {
			convertAndAssignFinalFields(er,100.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ug_100mL)) {
			convertAndAssignFinalFields(er,10.0/1000000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_mL) || er.property_value_units_original.equals(ExperimentalConstants.str_g_cm3)) {
			convertAndAssignFinalFields(er,1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		// under construction - CR
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_kg_H20)) {
			convertAndAssignFinalFields(er,1.0/1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_kg_H20;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_kg_H20)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_g_kg_H20;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_kg_kg_H20)) {
			convertAndAssignFinalFields(er,1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_kg_H20;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ug_g_H20)) {
			convertAndAssignFinalFields(er,1.0/1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_kg_H20;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_100g)) {
			convertAndAssignFinalFields(er,10.0);
			er.property_value_units_final = ExperimentalConstants.str_g_kg_H20;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_100g)) {
			convertAndAssignFinalFields(er,10.0/1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_kg_H20;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mol_m3_H20)) {
			convertAndAssignFinalFields(er,1.0/1000.0);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		// end of construction
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ppb)) {
			convertAndAssignFinalFields(er,1.0/1000.0);
			er.property_value_units_final = ExperimentalConstants.str_ppm;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_pctVol)) {
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_oz_gal)) {
			Double oz_to_g = 28.3495;
			Double gal_to_L = 3.78541;
			convertAndAssignFinalFields(er,oz_to_g/gal_to_L);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else {
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (unknown units)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;
			System.out.println("Unrecognized units for "+er.property_name+": "+er.property_value_units_original);
		}

		return !er.flag;
	}
	
	private static boolean convertHenrysLawConstant(ExperimentalRecord er) {
		if (er.property_value_units_original.equals(ExperimentalConstants.str_Pa_m3_mol)) {
			convertAndAssignFinalFields(er,Pa_to_atm);
			er.property_value_units_final = ExperimentalConstants.str_atm_m3_mol;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_atm_m3_mol)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_atm_m3_mol;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_atm)) {
			er.flag=true;
			er.updateNote("Conversion to atm-m3/mol not possible (dimensions differ)");
			convertAndAssignFinalFields(er,atm_to_mmHg);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_log_atm_m3_mol)) {
			powAndAssignFinalFields(er);
			er.property_value_units_final = ExperimentalConstants.str_atm_m3_mol;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_dimensionless_H) ||
				er.property_value_units_original.equals(ExperimentalConstants.str_dimensionless_H_vol)) {
			er.flag=true;
			er.updateNote("Conversion to atm-m3/mol not possible (dimensions differ)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;
		} else {
			er.flag = true;
			er.updateNote("Conversion to atm-m3/mol not possible (unknown units)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;
			System.out.println("Unrecognized units for "+er.property_name+": "+er.property_value_units_original);
		}

		return !er.flag;
	}
	
	private static void convertAndAssignFinalFields(ExperimentalRecord er,double conversionFactor) {
		if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = er.property_value_point_estimate_original*conversionFactor; }
		if (er.property_value_min_original!=null) { er.property_value_min_final = er.property_value_min_original*conversionFactor; }
		if (er.property_value_max_original!=null) { er.property_value_max_final = er.property_value_max_original*conversionFactor; }
	}
	
	private static void powConvertAndAssignFinalFields(ExperimentalRecord er,double conversionFactor) {
		if (er.property_value_point_estimate_original!=null) { 
			er.property_value_point_estimate_final = Math.pow(10.0,er.property_value_point_estimate_original)*conversionFactor; }
		if (er.property_value_min_original!=null) { er.property_value_min_final = Math.pow(10.0,er.property_value_min_original)*conversionFactor; }
		if (er.property_value_max_original!=null) { er.property_value_max_final = Math.pow(10.0,er.property_value_max_original)*conversionFactor; }
	}
	
	private static void powAndAssignFinalFields(ExperimentalRecord er) {
		powConvertAndAssignFinalFields(er,1.0);
	}
	
	private static void assignFinalFieldsWithoutConverting(ExperimentalRecord er) {
		convertAndAssignFinalFields(er,1.0);
	}
}
