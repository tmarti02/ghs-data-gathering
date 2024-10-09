package gov.epa.exp_data_gathering.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.api.ExperimentalConstants;


public class PressureCondition {

	
	public static class PressureUnitsResults {
		int pressureIndex;
		double conversionFactor;
	}
	
	
	
	private static PressureUnitsResults setValues(String propertyValue) {
		
		PressureUnitsResults pr=new PressureUnitsResults();
		
		pr.pressureIndex=-1;
		pr.conversionFactor=1.0;

		
		if (propertyValue.contains("kpa")) {
			pr.pressureIndex = propertyValue.indexOf("kpa");
			pr.conversionFactor = UnitConverter.kPa_to_mmHg;
		} else if (propertyValue.contains("mmhg") || propertyValue.contains("mm hg") || propertyValue.contains("mm")) {
			pr.pressureIndex = propertyValue.indexOf("mm");
		} else if (propertyValue.contains("atm")) {
			pr.pressureIndex = propertyValue.indexOf("atm");
			pr.conversionFactor = UnitConverter.atm_to_mmHg;
		} else if (propertyValue.contains("hpa")) {
			pr.pressureIndex = propertyValue.indexOf("hpa");
			pr.conversionFactor = UnitConverter.hPa_to_mmHg;
		} else if (propertyValue.contains("pa")) {

			int epaIndex=propertyValue.indexOf("epa");
			pr.pressureIndex = propertyValue.indexOf("pa");
			
			if(epaIndex==pr.pressureIndex-1) {
				pr.pressureIndex=-1;
//				System.out.println(propertyValue+"\tfound EPA when looking for pressure condition");
			}
			
			pr.conversionFactor = UnitConverter.Pa_to_mmHg;
		} else if (propertyValue.contains("mbar")) {
			pr.pressureIndex = propertyValue.indexOf("mb");
			pr.conversionFactor = UnitConverter.hPa_to_mmHg;
		} else if (propertyValue.contains("bar")) {
			pr.pressureIndex = propertyValue.indexOf("bar");
			pr.conversionFactor = UnitConverter.bar_to_mmHg;
		} else if (propertyValue.contains("torr")) {
			pr.pressureIndex = propertyValue.indexOf("torr");
		} else if (propertyValue.contains("psi")) {
			pr.pressureIndex = propertyValue.indexOf("psi");
			pr.conversionFactor = UnitConverter.psi_to_mmHg;
		} else if (propertyValue.contains("upa")) {
			pr.pressureIndex = propertyValue.indexOf("upa");
			pr.conversionFactor = UnitConverter.uPa_to_mmHg;
		} else if (propertyValue.contains("npa")) {
			pr.pressureIndex = propertyValue.indexOf("npa");
			pr.conversionFactor = UnitConverter.nPa_to_mmHg;
		}
		
		return pr;
	}
	
	/**
	 * Sets the pressure condition for an ExperimentalRecord object, if present
	 * @param er			The ExperimentalRecord object to be updated
	 * @param propertyValue	The string to be read
	 * @return				The pressure condition in kPa
	 */
	public static void getPressureCondition(ExperimentalRecord er,String propertyValue,String sourceName) {

		if(er.property_name.equals(ExperimentalConstants.strFlashPoint) || er.property_name.equals(ExperimentalConstants.strAutoIgnitionTemperature)) {
			return;
		}
		
		propertyValue = propertyValue.toLowerCase();
		
//		int pressureIndex = -1;
//		double conversionFactor = 1.0;
		
		PressureUnitsResults pur=setValues(propertyValue);
		
		
		// If any pressure units were found, looks for the last number that precedes them
		boolean foundNumeric = false;

		if (pur.pressureIndex > 0) {
			if (sourceName.contains(ExperimentalConstants.strSourceEChemPortal)) {
//				if (!foundNumeric) {
//					int pressureIndexOriginal = pressureIndex;
//					foundNumeric = findClosestScientificNotationValue(er, propertyValue, pressureIndex, pressureIndexOriginal,
//							foundNumeric);
//					er.pressure_mmHg = mantissa*Math.pow(10, magnitude);
//				}
				if (!foundNumeric) {
					double[] range = TextUtilities.extractFirstDoubleRangeFromString(propertyValue,pur.pressureIndex);
					if (range!=null) {
						String min = TextUtilities.formatDouble(range[0]*pur.conversionFactor);
						String max = TextUtilities.formatDouble(range[1]*pur.conversionFactor);
						er.pressure_mmHg = min+"-"+max;
						foundNumeric = true;
					}
				}
				if (!foundNumeric) {
					try {

						double[] range = TextUtilities.extractAltFormatRangeFromString(propertyValue,pur.pressureIndex);
						if (range!=null) {
							String min = TextUtilities.formatDouble(range[0]*pur.conversionFactor);
							String max = TextUtilities.formatDouble(range[1]*pur.conversionFactor);
							er.pressure_mmHg = min+"-"+max;
							foundNumeric = true;
						}

					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
				}
				if (!foundNumeric) {
					try {
						Matcher caMatcher = Pattern.compile(".*?(ca. )?([-]?[ ]?[0-9]*\\.?[0-9]+)( ca. )([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(propertyValue.substring(0,pur.pressureIndex));
						if (caMatcher.find()) {
							String numQual = caMatcher.group(1).isBlank() ? "" : "~";
							String min = TextUtilities.formatDouble(Double.parseDouble(caMatcher.group(2)));
							String max = TextUtilities.formatDouble(Double.parseDouble(caMatcher.group(4)));
							er.pressure_mmHg = numQual+min+"~"+max;
							foundNumeric = true;
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			if (!foundNumeric) {
				try {
					er.pressure_mmHg = TextUtilities.formatDouble(pur.conversionFactor*TextUtilities.extractLastDoubleFromString(propertyValue,pur.pressureIndex));
					foundNumeric = true;
				} catch (NumberFormatException ex) {
					// NumberFormatException means no numerical value was found; leave foundNumeric = false and do nothing else
				}
			}
			if (propertyValue.startsWith("ca.")) {
				er.pressure_mmHg = "~"+er.pressure_mmHg;
			}
		}
		
		
	}
}
