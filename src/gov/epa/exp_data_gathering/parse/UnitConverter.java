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
		double conversionFactor = 1.0;
		if (er.property_value_units_original.equals(ExperimentalConstants.str_mmHg) || er.property_value_units_original.equals(ExperimentalConstants.str_torr)) {
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
		if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = er.property_value_point_estimate_original*conversionFactor; }
		if (er.property_value_min_original!=null) { er.property_value_min_final = er.property_value_min_original*conversionFactor; }
		if (er.property_value_max_original!=null) { er.property_value_max_final = er.property_value_max_original*conversionFactor; }
		er.property_value_units_final = ExperimentalConstants.str_mmHg;
	}
	
	public static void convertSolubility(ExperimentalRecord er) {
		double conversionFactor = 1.0;
		if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_L) || er.property_value_units_original.equals(ExperimentalConstants.str_ug_mL)
				|| er.property_value_units_original.equals(ExperimentalConstants.str_ppm)) {
			conversionFactor = 1.0/1000.0;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ug_L)) {
			conversionFactor = 1.0/1000000.0;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_100mL) ||
				er.property_value_units_original.equals(ExperimentalConstants.str_pctWt) || er.property_value_units_original.equals(ExperimentalConstants.str_pct)) {
			conversionFactor = 10.0;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_100mL)) {
			conversionFactor = 1.0/100.0;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_mL)) {
			conversionFactor = 1000.0;
		} else if (!er.property_value_units_original.equals(ExperimentalConstants.str_g_L) && !er.property_value_units_original.equals(ExperimentalConstants.str_mg_mL)) {
			er.flag = true;
			er.updateNote("unit conversion not possible yet");
		}
		if (!er.flag) {
			if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = er.property_value_point_estimate_original*conversionFactor; }
			if (er.property_value_min_original!=null) { er.property_value_min_final = er.property_value_min_original*conversionFactor; }
			if (er.property_value_max_original!=null) { er.property_value_max_final = er.property_value_max_original*conversionFactor; }
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else {
			if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = er.property_value_point_estimate_original; }
			if (er.property_value_min_original!=null) { er.property_value_min_final = er.property_value_min_original; }
			if (er.property_value_max_original!=null) { er.property_value_max_final = er.property_value_max_original; }
			er.property_value_units_final = er.property_value_units_original;
		}
	}
}
