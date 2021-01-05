package gov.epa.exp_data_gathering.parse;

import org.apache.commons.text.StringEscapeUtils;

import gov.epa.api.ExperimentalConstants;

public class RecordFinalizer {

	
	/**
	 * Converts to final units and assigns point estimates for any ranges within tolerance:
	 * @param er - ExperimentalRecord to convert units and store final values
	 */
	public static void finalizeRecord(ExperimentalRecord er) {
		double logTolerance = 1.0;//log units for properties that vary many orders of magnitude; if value was 1, then max would be 10x bigger than min
		double temperatureTolerance = 10.0;//C For Melting point, boiling point, flash point
		double densityTolerance = 0.1;//g/cm^3 for density
		
		//Properties which are usually modeled as log of the property value: pKA, logKow, WS, HLC, VP, LC50, LD50
		
								
		if (er.property_name.equals(ExperimentalConstants.str_pKA) || er.property_name.equals(ExperimentalConstants.strLogKow)) {
			if (er.property_value_point_estimate_original!=null) { er.property_value_point_estimate_final = er.property_value_point_estimate_original; }
	
			if (er.property_value_min_original!=null) { 
				er.property_value_min_final = er.property_value_min_original;
				er.property_value_max_final = er.property_value_max_original;
	
				if (isWithinTolerance(er,logTolerance)) {
					calculateFinalValueFromMinMaxAverage(er);//values are already in log units so dont need to use geometric median
				} else if (!isWithinTolerance(er,logTolerance)) {
					er.flag = true;
					er.reason = "Range too wide to compute point estimate";
				}
			}
			er.property_value_units_final = er.property_value_units_original;
		} else if ((er.property_name.equals(ExperimentalConstants.strMeltingPoint) || er.property_name.equals(ExperimentalConstants.strBoilingPoint) ||
				er.property_name.equals(ExperimentalConstants.strFlashPoint)) && er.property_value_units_original!=null) {
			UnitConverter.convertTemperature(er);
			if (er.property_value_min_final!=null && isWithinTolerance(er,temperatureTolerance)) {
				calculateFinalValueFromMinMaxAverage(er);
			} else if (er.property_value_min_final!=null && !isWithinTolerance(er,temperatureTolerance)) {
				er.flag = true;
				er.reason = "Range too wide to compute point estimate";
			}
		} else if (er.property_name.equals(ExperimentalConstants.strDensity)) {
			UnitConverter.convertDensity(er);
			if (er.property_value_min_final!=null && isWithinTolerance(er,densityTolerance)) {
				calculateFinalValueFromMinMaxAverage(er);
			} else if (er.property_value_min_final!=null && !isWithinTolerance(er,densityTolerance)) {
				er.flag = true;
				er.reason = "Range too wide to compute point estimate";
			}
		} else if (er.property_name.equals(ExperimentalConstants.strVaporPressure) && er.property_value_units_original!=null) {
			UnitConverter.convertPressure(er);
			if (er.property_value_min_final!=null && isWithinLogTolerance(er,logTolerance)) {
				calculateFinalValueFromMinMaxAverage(er);
			} else if (er.property_value_min_final!=null && !isWithinLogTolerance(er,logTolerance)) {
				if (er.property_value_min_final==0) {
					er.keep = false;
					er.reason = "Range with minimum 0 is not interpretable";
				} else {
					er.flag = true;
					er.reason = "Range too wide to compute point estimate";
				}
			}
		} else if (er.property_name.equals(ExperimentalConstants.strHenrysLawConstant) && er.property_value_units_original!=null) {
			UnitConverter.convertHenrysLawConstant(er);
			if (er.property_value_min_final!=null && isWithinLogTolerance(er,logTolerance)) {
				calculateFinalValueFromMinMaxAverage(er);
			} else if (er.property_value_min_final!=null && !isWithinLogTolerance(er,logTolerance)) {
				if (er.property_value_min_final==0) {
					er.keep = false;
					er.reason = "Range with minimum 0 is not interpretable";
				} else {
					er.flag = true;
					er.reason = "Range too wide to compute point estimate";
				}
			}
		} else if (er.property_name.equals(ExperimentalConstants.strWaterSolubility) && er.property_value_units_original!=null) {
			UnitConverter.convertSolubility(er);
			if (er.property_value_min_final!=null && isWithinLogTolerance(er,logTolerance)) {
				calculateFinalValueFromMinMaxAverage(er);
			} else if (er.property_value_min_final!=null && !isWithinLogTolerance(er,logTolerance)) {
				if (er.property_value_min_final==0) {
					er.keep = false;
					er.reason = "Range with minimum 0 is not interpretable";
				} else {
					er.flag = true;
					er.reason = "Range too wide to compute point estimate";
				}
			}
		} else if ((er.property_name.contains("LC50") || er.property_name.contains("LD50")) && er.property_value_units_original!=null) {
			UnitConverter.convertToxicity(er);
			if (er.property_value_min_final!=null && isWithinLogTolerance(er,logTolerance)) {
				calculateFinalValueFromMinMaxAverage(er);
			} else if (er.property_value_min_final!=null && !isWithinLogTolerance(er,logTolerance)) {
				if (er.property_value_min_final==0) {
					er.keep = false;
					er.reason = "Range with minimum 0 is not interpretable";
				} else {
					er.flag = true;
					er.reason = "Range too wide to compute point estimate";
				}
			}
		}
	}

	public static boolean isWithinLogTolerance(ExperimentalRecord er,double logTolerance) {
		if (er.property_value_min_final!=0) {
			return Math.log10(er.property_value_max_final/er.property_value_min_final) <= logTolerance;
		} else {
			return false;
		}
	}

	public static boolean isWithinTolerance(ExperimentalRecord er,double tolerance) {		
		return er.property_value_max_final-er.property_value_min_final <= tolerance;
	}

	public static void calculateFinalValueFromMinMaxAverage(ExperimentalRecord er) {
		er.property_value_point_estimate_final = (er.property_value_min_final + er.property_value_max_final)/2.0;				
		er.updateNote("Point estimate computed from average of range");
	}

//	/**
//	 * Use this when values span many orders of magnitude (and modeled property is the log of the value)
//	TMM Note: Maybe only use geometric median when calculating a final value from values from multiple sources rather than min/max values
//	 */
//	public static void calculateFinalValueFromMinMaxGeometricMedian(ExperimentalRecord er) {
//		er.property_value_point_estimate_final = Math.sqrt(er.property_value_min_final * er.property_value_max_final);		
//		//Note: since usually the log value is the modeled property, geometric median = 10 ^ average log value (properties of logarithms)
//	}

}
