package gov.epa.exp_data_gathering.parse;

import org.apache.commons.text.StringEscapeUtils;

import gov.epa.api.ExperimentalConstants;

public class RecordFinalizer {

	/**
	 * Converts to final units and assigns point estimates for any ranges within tolerance:
	 * LogKow, pKa = 1 log unit
	 * Melting point, boiling point, flash point = 10 C
	 * Density = 0.1 g/cm^3
	 * Vapor pressure = 10 mmHg
	 * HLC = 100 Pa-m^3/mol
	 * Water solubility = 1 g/L
	 */
	public static void finalizeRecord(ExperimentalRecord er) {
		double logTolerance = 0.5;//if value was 1, then max would be 10x bigger than min
		double temperatureTolerance = 10.0;
		double densityTolerance = 0.1;
		
		//Properties which are usually modeled as log of the property value: pKA, logKow, WS, HLC, VP, LC50, LD50
		
								
		if (er.property_name.equals(ExperimentalConstants.str_pKA) || er.property_name.equals(ExperimentalConstants.strLogKow)) {
			if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = er.property_value_point_estimate_original; }
	
			if (er.property_value_min_original!=null) { 
				er.property_value_min_final = er.property_value_min_original;
				er.property_value_max_final = er.property_value_max_original;
	
				if (isWithinTolerance(er,logTolerance)) {
					calculateFinalValueFromMinMaxAverage(er);//values are already in log units so dont need to use geometric median
					er.updateNote("Point estimate computed from range");
				}
			}
			er.property_value_units_final = er.property_value_units_original;
		} else if ((er.property_name.equals(ExperimentalConstants.strMeltingPoint) || er.property_name.equals(ExperimentalConstants.strBoilingPoint) ||
				er.property_name.equals(ExperimentalConstants.strFlashPoint)) && er.property_value_units_original!=null) {
			UnitConverter.convertTemperature(er);
			if (er.property_value_min_final!=null && isWithinTolerance(er,temperatureTolerance)) {
				calculateFinalValueFromMinMaxAverage(er);
				er.updateNote("Point estimate computed from average of range");
			}
		} else if (er.property_name.equals(ExperimentalConstants.strDensity)) {
			UnitConverter.convertDensity(er);
			if (er.property_value_min_final!=null && isWithinTolerance(er,densityTolerance)) {
				calculateFinalValueFromMinMaxAverage(er);
				er.updateNote("Point estimate computed from average of range");
			}
		} else if (er.property_name.equals(ExperimentalConstants.strVaporPressure) && er.property_value_units_original!=null) {
			UnitConverter.convertPressure(er);
			if (er.property_value_min_final!=null && isWithinLogTolerance(er,logTolerance)) {
				calculateFinalValueFromMinMaxGeometricMedian(er);
				er.updateNote("Point estimate computed from geometric median of range");
			}
		} else if (er.property_name.equals(ExperimentalConstants.strHenrysLawConstant) && er.property_value_units_original!=null) {
			boolean converted = UnitConverter.convertHenrysLawConstant(er);
			if (converted && er.property_value_min_final!=null && isWithinLogTolerance(er,logTolerance)) {
				calculateFinalValueFromMinMaxGeometricMedian(er);
				er.updateNote("Point estimate computed from geometric median of range");
			}
		} else if (er.property_name.equals(ExperimentalConstants.strWaterSolubility) && er.property_value_units_original!=null) {
			boolean converted = UnitConverter.convertSolubility(er);
			if (converted && er.property_value_min_final!=null && isWithinLogTolerance(er,logTolerance)) {
				calculateFinalValueFromMinMaxGeometricMedian(er);
				er.updateNote("Point estimate computed from geometric median of range");
			}
		} else if ((er.property_name.contains("LC50") || er.property_name.contains("LD50")) && er.property_value_units_original!=null) {
			boolean converted=UnitConverter.convertToxicity(er);
			if (converted && er.property_value_min_final!=null && isWithinLogTolerance(er,logTolerance)) {
				calculateFinalValueFromMinMaxGeometricMedian(er);
				er.updateNote("Point estimate computed from geometric median of range");
			}
		}
	}

	public static boolean isWithinLogTolerance(ExperimentalRecord er,double logTolerance) {
		return Math.log10(er.property_value_max_final/er.property_value_min_final) <= logTolerance;
	}

	public static boolean isWithinTolerance(ExperimentalRecord er,double temperatureTolerance) {
		return er.property_value_max_final-er.property_value_min_final <= temperatureTolerance;
	}

	public static void calculateFinalValueFromMinMaxAverage(ExperimentalRecord er) {
		er.property_value_point_estimate_final = (er.property_value_min_final + er.property_value_max_final)/2.0;		
		//@Gabriel this is same as property_value_min_final + (property_value_max_final-property_value_min_final)/2.0;
	}

	/**
	 * Use this when values span many orders of magnitude (and modeled property is the log of the value)
	 */
	public static void calculateFinalValueFromMinMaxGeometricMedian(ExperimentalRecord er) {
		er.property_value_point_estimate_final = Math.sqrt(er.property_value_min_final * er.property_value_max_final);		
		//Note: since usually the log value is the modeled property, geometric median = 10 ^ average log value (properties of logarithms)
	}

}
