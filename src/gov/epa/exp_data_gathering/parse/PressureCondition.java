package gov.epa.exp_data_gathering.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.api.ExperimentalConstants;


public class PressureCondition {
	
	int pressureIndex;
	double conversionFactor;
	
	public PressureCondition(String propertyValue) {
		setValues(propertyValue);
	}
	
	
	private void setValues(String propertyValue) {
		
		pressureIndex=-1;
		conversionFactor=1.0;

		
		if (propertyValue.contains("kpa")) {
			pressureIndex = propertyValue.indexOf("kpa");
			conversionFactor = UnitConverter.kPa_to_mmHg;
		} else if (propertyValue.contains("mmhg") || propertyValue.contains("mm hg") || propertyValue.contains("mm")) {
			pressureIndex = propertyValue.indexOf("mm");
		} else if (propertyValue.contains("atm")) {
			pressureIndex = propertyValue.indexOf("atm");
			conversionFactor = UnitConverter.atm_to_mmHg;
		} else if (propertyValue.contains("hpa")) {
			pressureIndex = propertyValue.indexOf("hpa");
			conversionFactor = UnitConverter.hPa_to_mmHg;
		} else if (propertyValue.contains("pa")) {

			int epaIndex=propertyValue.indexOf("epa");
			pressureIndex = propertyValue.indexOf("pa");
			
			if(epaIndex==pressureIndex-1) {
				pressureIndex=-1;
//				System.out.println(propertyValue+"\tfound EPA when looking for pressure condition");
			}
			
			conversionFactor = UnitConverter.Pa_to_mmHg;
		} else if (propertyValue.contains("mbar")) {
			pressureIndex = propertyValue.indexOf("mb");
			conversionFactor = UnitConverter.hPa_to_mmHg;
		} else if (propertyValue.contains("bar")) {
			pressureIndex = propertyValue.indexOf("bar");
			conversionFactor = UnitConverter.bar_to_mmHg;
		} else if (propertyValue.contains("torr")) {
			pressureIndex = propertyValue.indexOf("torr");
		} else if (propertyValue.contains("psi")) {
			pressureIndex = propertyValue.indexOf("psi");
			conversionFactor = UnitConverter.psi_to_mmHg;
		} else if (propertyValue.contains("upa")) {
			pressureIndex = propertyValue.indexOf("upa");
			conversionFactor = UnitConverter.uPa_to_mmHg;
		} else if (propertyValue.contains("npa")) {
			pressureIndex = propertyValue.indexOf("npa");
			conversionFactor = UnitConverter.nPa_to_mmHg;
		}
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
		
		PressureCondition pc=new PressureCondition(propertyValue);
		
		
		
		// If any pressure units were found, looks for the last number that precedes them
		boolean foundNumeric = false;

		if (pc.pressureIndex > 0) {
			if (sourceName.contains(ExperimentalConstants.strSourceEChemPortal)) {
//				if (!foundNumeric) {
//					int pressureIndexOriginal = pressureIndex;
//					foundNumeric = findClosestScientificNotationValue(er, propertyValue, pressureIndex, pressureIndexOriginal,
//							foundNumeric);
//					er.pressure_mmHg = mantissa*Math.pow(10, magnitude);
//				}
				if (!foundNumeric) {
					double[] range = TextUtilities.extractFirstDoubleRangeFromString(propertyValue,pc.pressureIndex);
					if (range!=null) {
						String min = TextUtilities.formatDouble(range[0]*pc.conversionFactor);
						String max = TextUtilities.formatDouble(range[1]*pc.conversionFactor);
						er.pressure_mmHg = min+"-"+max;
						foundNumeric = true;
					}
				}
				if (!foundNumeric) {
					try {

						double[] range = TextUtilities.extractAltFormatRangeFromString(propertyValue,pc.pressureIndex);
						if (range!=null) {
							String min = TextUtilities.formatDouble(range[0]*pc.conversionFactor);
							String max = TextUtilities.formatDouble(range[1]*pc.conversionFactor);
							er.pressure_mmHg = min+"-"+max;
							foundNumeric = true;
						}

					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
				}
				if (!foundNumeric) {
					try {
						Matcher caMatcher = Pattern.compile(".*?(ca. )?([-]?[ ]?[0-9]*\\.?[0-9]+)( ca. )([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(propertyValue.substring(0,pc.pressureIndex));
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
					er.pressure_mmHg = TextUtilities.formatDouble(pc.conversionFactor*TextUtilities.extractLastDoubleFromString(propertyValue,pc.pressureIndex));
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
