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
	
	public static double F_to_C(double F) {
		return (F-32.0)*5.0/9.0;
	}
	
	public static double K_to_C(double K) {
		return K-273.15;
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
		if (er.casrn!=null && !ParseUtilities.isValidCAS(er.casrn)) {
			er.keep = false;
			er.reason = "Invalid CAS";
		}
				
		if (er.property_name.equals(ExperimentalConstants.str_pKA) || er.property_name.equals(ExperimentalConstants.strLogKow)) {
			applyConversion(er,1.0);
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
		} else if ((er.property_name.toLowerCase().contains("lc50") || er.property_name.toLowerCase().contains("ld50")) &&
				er.property_value_units_original!=null) {
			convertToxicity(er);
		}
	}
	
	/**
	 * TODO Gabriel check this- used for echemportal toxicity values...
	 * 
	 * @param er
	 * @return
	 */
	private boolean convertToxicity(ExperimentalRecord er) {
		double conversionFactor = 1.0;
		if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_L)) {
			er.property_value_units_final = ExperimentalConstants.str_mg_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_m3)) {
			conversionFactor = 1.0/1000.0;
			er.property_value_units_final = ExperimentalConstants.str_mg_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mL_m3)) {
			if (htDensity.get(er.casrn) == null) {
				er.flag = true;
				er.updateNote("Conversion to mg/L not possible (missing density)");
				return false;
			} else {
				conversionFactor = htDensity.get(er.casrn);
				er.property_value_units_final = ExperimentalConstants.str_mg_L;
				er.updateNote("Converted using density: "+conversionFactor+" g/mL");
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
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_kg)) {
			er.property_value_units_final = ExperimentalConstants.str_mg_kg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mL_kg)) {
			if (htDensity.get(er.casrn) == null) {
				er.flag = true;
				er.updateNote("Conversion to mg/kg not possible (missing density)");
				return false;
			} else {
				conversionFactor = htDensity.get(er.casrn)*1000.0;
				er.property_value_units_final = ExperimentalConstants.str_mg_kg;
				er.updateNote("Converted using density: "+conversionFactor/1000.0+" g/mL");
			}
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg)) {
			er.flag = true;
			er.updateNote("Conversion to mg/kg not possible (unit types differ)");
		} else {
			er.flag = true;
			er.updateNote("Conversion not possible (unknown units)");
		}
		
		if (!er.flag) {
			applyConversion(er,conversionFactor);
		} else {
			applyConversion(er,1.0);
			er.property_value_units_final = er.property_value_units_original;
		}		
		
		return !er.flag;
	}
	
	private static void convertTemperature(ExperimentalRecord er) {
		if (er.property_value_units_original.equals(ExperimentalConstants.str_C)) {
			applyConversion(er,1.0);
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_F)) {
			if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = F_to_C(er.property_value_point_estimate_original); }
			if (er.property_value_min_original!=null) { er.property_value_min_final = F_to_C(er.property_value_min_original); }
			if (er.property_value_max_original!=null) { er.property_value_max_final = F_to_C(er.property_value_max_original); }
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_K)) {
			if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = K_to_C(er.property_value_point_estimate_original); }
			if (er.property_value_min_original!=null) { er.property_value_min_final = K_to_C(er.property_value_min_original); }
			if (er.property_value_max_original!=null) { er.property_value_max_final = K_to_C(er.property_value_max_original); }
		}
		er.property_value_units_final = ExperimentalConstants.str_C;
	}
	
	private static void convertDensity(ExperimentalRecord er) {
		double conversionFactor = 1.0;
		if (er.property_value_units_original!=null && (er.property_value_units_original.equals(ExperimentalConstants.str_kg_m3) ||
				er.property_value_units_original.equals(ExperimentalConstants.str_g_L))) {
			conversionFactor = 1.0/1000.0;
		} else if (er.property_value_units_original==null && er.note!=null && (er.note.contains(ExperimentalConstants.str_relative_gas_density) ||
				er.note.contains(ExperimentalConstants.str_relative_mixture_density))) {
			conversionFactor = UnitConverter.airDensitySTP;
		}
		applyConversion(er,conversionFactor);
		er.property_value_units_final = ExperimentalConstants.str_g_cm3;
	}
	
	private static void convertPressure(ExperimentalRecord er) {
		boolean isLog = false;
		double conversionFactor = 1.0;
		if (er.property_value_units_original.equals(ExperimentalConstants.str_log_mmHg)) {
			isLog = true;
			if (er.property_value_point_estimate_original!=null) { 
				er.property_value_point_estimate_final = Math.pow(10.0,er.property_value_point_estimate_original); }
			if (er.property_value_min_original!=null) { er.property_value_min_final = Math.pow(10.0,er.property_value_min_original); }
			if (er.property_value_max_original!=null) { er.property_value_max_final = Math.pow(10.0,er.property_value_max_original); }
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mmHg) || er.property_value_units_original.equals(ExperimentalConstants.str_torr)) {
			conversionFactor = 1.0;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_atm)) {
			conversionFactor = UnitConverter.atm_to_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_hpa) || er.property_value_units_original.equals(ExperimentalConstants.str_mbar)) {
			conversionFactor = UnitConverter.hPa_to_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_pa)) {
			conversionFactor = UnitConverter.Pa_to_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_bar)) {
			conversionFactor = UnitConverter.bar_to_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_psi)) {
			conversionFactor = UnitConverter.psi_to_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_kpa)) {
			conversionFactor = UnitConverter.kPa_to_mmHg;
		}
		if (!isLog) {
			applyConversion(er,conversionFactor);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		}
	}
	
	private static boolean convertSolubility(ExperimentalRecord er) {
		boolean isConvertible = true;
		double conversionFactor = 1.0;
		if (er.property_value_units_original.equals(ExperimentalConstants.str_log_mg_L)) {
			isConvertible = false;
			if (er.property_value_point_estimate_original!=null) { 
				er.property_value_point_estimate_final = Math.pow(10.0,er.property_value_point_estimate_original)*1.0/1000.0; }
			if (er.property_value_min_original!=null) { er.property_value_min_final = Math.pow(10.0,er.property_value_min_original)*1.0/1000.0; }
			if (er.property_value_max_original!=null) { er.property_value_max_final = Math.pow(10.0,er.property_value_max_original)*1.0/1000.0; }
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_log_M)) {
			isConvertible = false;
			if (er.property_value_point_estimate_original!=null) { 
				er.property_value_point_estimate_final = Math.pow(10.0,er.property_value_point_estimate_original); }
			if (er.property_value_min_original!=null) { er.property_value_min_final = Math.pow(10.0,er.property_value_min_original); }
			if (er.property_value_max_original!=null) { er.property_value_max_final = Math.pow(10.0,er.property_value_max_original); }
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mM)) {
			isConvertible = false;
			conversionFactor = 1.0/1000.0;
			applyConversion(er,conversionFactor);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_nM)) {
			isConvertible = false;
			conversionFactor = 1.0/1000000000.0;
			applyConversion(er,conversionFactor);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_L) || er.property_value_units_original.equals(ExperimentalConstants.str_ug_mL)
				|| er.property_value_units_original.equals(ExperimentalConstants.str_ppm)) {
			conversionFactor = 1.0/1000.0;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ug_L)) {
			conversionFactor = 1.0/1000000.0;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_100mL) ||
				er.property_value_units_original.equals(ExperimentalConstants.str_pctWt) || er.property_value_units_original.equals(ExperimentalConstants.str_pct)) {
			conversionFactor = 10.0;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_100mL)) {
			conversionFactor = 1.0/100.0;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_mL) || er.property_value_units_original.equals(ExperimentalConstants.str_g_cm3)) {
			conversionFactor = 1000.0;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ppb)) {
			conversionFactor = 1.0/1000000.0;
		} else if (!er.property_value_units_original.equals(ExperimentalConstants.str_g_L) && !er.property_value_units_original.equals(ExperimentalConstants.str_mg_mL) &&
				!er.property_value_units_original.equals(ExperimentalConstants.str_kg_m3)) {
			er.flag = true;
			er.updateNote("Conversion to g/L not possible");
		}
		if (!er.flag && isConvertible) {
			applyConversion(er,conversionFactor);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (isConvertible) {
			applyConversion(er,1.0);
			er.property_value_units_final = er.property_value_units_original;
		}
		return !er.flag;
	}
	
	private static boolean convertHenrysLawConstant(ExperimentalRecord er) {
		double conversionFactor = 1.0;
		if (er.property_value_units_original.equals(ExperimentalConstants.str_Pa_m3_mol)) {
			er.property_value_units_final = ExperimentalConstants.str_Pa_m3_mol;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_atm_m3_mol)) {
			conversionFactor = UnitConverter.atm_to_Pa;
			er.property_value_units_final = ExperimentalConstants.str_Pa_m3_mol;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_atm)) {
			conversionFactor = UnitConverter.atm_to_mmHg;
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
			er.updateNote("Conversion to Pa-m3/mol not possible (unit types differ)");
			er.flag=true;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_log_atm_m3_mol)) {
			er.property_value_point_estimate_final = Math.pow(10.0,er.property_value_point_estimate_original);
			er.property_value_units_final = ExperimentalConstants.str_atm_m3_mol;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_dimensionless_H) ||
				er.property_value_units_original.equals(ExperimentalConstants.str_dimensionless_H_vol)) {
			er.property_value_units_final = er.property_value_units_original;
			er.updateNote("Conversion to Pa-m3/mol not possible (unit types differ)");
			er.flag=true;
		}
		applyConversion(er,conversionFactor);
		return !er.flag;
	}
	
	private static void applyConversion(ExperimentalRecord er,double conversionFactor) {
		if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = er.property_value_point_estimate_original*conversionFactor; }
		if (er.property_value_min_original!=null) { er.property_value_min_final = er.property_value_min_original*conversionFactor; }
		if (er.property_value_max_original!=null) { er.property_value_max_final = er.property_value_max_original*conversionFactor; }
	}
	
	private static void applyConversion(ExperimentalRecord er,Function<Double,Double> conversionMethod) {
		if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = conversionMethod.apply(er.property_value_point_estimate_original); }
		if (er.property_value_min_original!=null) { er.property_value_min_final = conversionMethod.apply(er.property_value_min_original); }
		if (er.property_value_max_original!=null) { er.property_value_max_final = conversionMethod.apply(er.property_value_max_original); }
	}
}
