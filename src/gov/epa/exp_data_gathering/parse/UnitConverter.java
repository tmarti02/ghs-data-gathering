package gov.epa.exp_data_gathering.parse;

import gov.epa.api.ExperimentalConstants;

public class UnitConverter {
	
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
	
	public static void convertTemperature(ExperimentalRecord er) {
		if (er.property_value_units_original.equals(ExperimentalConstants.str_C)) {
			if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = er.property_value_point_estimate_original; }
			if (er.property_value_min_original!=null) { er.property_value_min_final = er.property_value_min_original; }
			if (er.property_value_max_original!=null) { er.property_value_max_final = er.property_value_max_original; }
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
	
	public static void convertDensity(ExperimentalRecord er) {
		double conversionFactor = 1.0;
		if (er.property_value_units_original!=null && (er.property_value_units_original.equals(ExperimentalConstants.str_kg_m3) ||
				er.property_value_units_original.equals(ExperimentalConstants.str_g_L))) {
			conversionFactor = 1.0/1000.0;
		} else if (er.property_value_units_original==null && (er.note.contains(ExperimentalConstants.str_relative_gas_density) ||
				er.note.contains(ExperimentalConstants.str_relative_mixture_density))) {
			conversionFactor = UnitConverter.airDensitySTP;
		}
		if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = er.property_value_point_estimate_original*conversionFactor; }
		if (er.property_value_min_original!=null) { er.property_value_min_final = er.property_value_min_original*conversionFactor; }
		if (er.property_value_max_original!=null) { er.property_value_max_final = er.property_value_max_original*conversionFactor; }
		er.property_value_units_final = ExperimentalConstants.str_g_cm3;
	}
	
	public static void convertPressure(ExperimentalRecord er) {
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
			if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = er.property_value_point_estimate_original*conversionFactor; }
			if (er.property_value_min_original!=null) { er.property_value_min_final = er.property_value_min_original*conversionFactor; }
			if (er.property_value_max_original!=null) { er.property_value_max_final = er.property_value_max_original*conversionFactor; }
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		}
	}
	
	public static void convertSolubility(ExperimentalRecord er) {
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
			er.updateNote("conversion to g/L not possible");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mM)) {
			isConvertible = false;
			conversionFactor = 1.0/1000.0;
			if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = er.property_value_point_estimate_original*conversionFactor; }
			if (er.property_value_min_original!=null) { er.property_value_min_final = er.property_value_min_original*conversionFactor; }
			if (er.property_value_max_original!=null) { er.property_value_max_final = er.property_value_max_original*conversionFactor; }
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("conversion to g/L not possible");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_nM)) {
			isConvertible = false;
			conversionFactor = 1.0/1000000000.0;
			if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = er.property_value_point_estimate_original*conversionFactor; }
			if (er.property_value_min_original!=null) { er.property_value_min_final = er.property_value_min_original*conversionFactor; }
			if (er.property_value_max_original!=null) { er.property_value_max_final = er.property_value_max_original*conversionFactor; }
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("conversion to g/L not possible");
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
			er.updateNote("conversion to g/L not possible");
		}
		if (!er.flag && isConvertible) {
			if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = er.property_value_point_estimate_original*conversionFactor; }
			if (er.property_value_min_original!=null) { er.property_value_min_final = er.property_value_min_original*conversionFactor; }
			if (er.property_value_max_original!=null) { er.property_value_max_final = er.property_value_max_original*conversionFactor; }
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (isConvertible) {
			if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = er.property_value_point_estimate_original; }
			if (er.property_value_min_original!=null) { er.property_value_min_final = er.property_value_min_original; }
			if (er.property_value_max_original!=null) { er.property_value_max_final = er.property_value_max_original; }
			er.property_value_units_final = er.property_value_units_original;
		}
	}
	
	public static void convertHenrysLawConstant(ExperimentalRecord er) {
		double conversionFactor = 1.0;
		if (er.property_value_units_original.equals(ExperimentalConstants.str_Pa_m3_mol)) {
			er.property_value_units_final = ExperimentalConstants.str_Pa_m3_mol;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_atm_m3_mol)) {
			conversionFactor = UnitConverter.atm_to_Pa;
			er.property_value_units_final = ExperimentalConstants.str_Pa_m3_mol;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_atm)) {
			conversionFactor = UnitConverter.atm_to_mmHg;
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
			er.updateNote("conversion to Pa-m3/mol not possible");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_dimensionless_H) ||
				er.property_value_units_original.equals(ExperimentalConstants.str_dimensionless_H_vol)) {
			er.property_value_units_final = er.property_value_units_original;
			er.updateNote("conversion to Pa-m3/mol not possible");
		}
		if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = er.property_value_point_estimate_original*conversionFactor; }
		if (er.property_value_min_original!=null) { er.property_value_min_final = er.property_value_min_original*conversionFactor; }
		if (er.property_value_max_original!=null) { er.property_value_max_final = er.property_value_max_original*conversionFactor; }
	}
}
