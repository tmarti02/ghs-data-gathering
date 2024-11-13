package gov.epa.exp_data_gathering.parse;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.TemperatureCondition.TempUnitsResult;


public class PressureCondition {


	public static class PressureUnitsResults {
		int pressureIndex;
		double conversionFactor;
		String units;
	}



	private static PressureUnitsResults setValues(String propertyValue) {

		PressureUnitsResults pr=new PressureUnitsResults();

		pr.pressureIndex=-1;
		pr.conversionFactor=1.0;


		if (propertyValue.contains("kpa")) {
			pr.pressureIndex = propertyValue.indexOf("kpa");
			pr.conversionFactor = UnitConverter.kPa_to_mmHg;
			pr.units="kpa";
		} else if (propertyValue.contains("mmhg") || propertyValue.contains("mm hg") || propertyValue.contains("mm")) {
			boolean foundInstance = findInstances(propertyValue,"mm") || findInstances(propertyValue,"mmhg");
			if (foundInstance) {
				pr.pressureIndex = propertyValue.indexOf("mm");
				pr.units="mmHg";
			}
		} else if (propertyValue.contains("atm")) {
			pr.pressureIndex = propertyValue.indexOf("atm");
			pr.conversionFactor = UnitConverter.atm_to_mmHg;
			pr.units="atm";
		} else if (propertyValue.contains("hpa")) {
			pr.pressureIndex = propertyValue.indexOf("hpa");
			pr.conversionFactor = UnitConverter.hPa_to_mmHg;
			pr.units="hpa";
		} else if (propertyValue.contains("pa")) {

//			int epaIndex=propertyValue.indexOf("epa");
//			pr.pressureIndex = propertyValue.indexOf("pa");
//
//			if(epaIndex==pr.pressureIndex-1) {
//				pr.pressureIndex=-1;
//				//				System.out.println(propertyValue+"\tfound EPA when looking for pressure condition");
//			}
			boolean foundInstance = findInstances(propertyValue,"pa");
			if (foundInstance) {
				pr.pressureIndex = propertyValue.indexOf("pa");
				pr.units="pa";
				pr.conversionFactor = UnitConverter.Pa_to_mmHg;
			}
		} else if (propertyValue.contains("mbar")) {
			pr.pressureIndex = propertyValue.indexOf("mb");
			pr.conversionFactor = UnitConverter.hPa_to_mmHg;
			pr.units="mbar";
		} else if (propertyValue.contains("bar")) {
			pr.pressureIndex = propertyValue.indexOf("bar");
			pr.conversionFactor = UnitConverter.bar_to_mmHg;
			pr.units="bar";
		} else if (propertyValue.contains("torr")) {
			pr.pressureIndex = propertyValue.indexOf("torr");
			pr.units="torr";
		} else if (propertyValue.contains("psi")) {
			boolean foundInstance = findInstances(propertyValue,"psi");
			if (foundInstance) {
				pr.pressureIndex = propertyValue.indexOf("mm");
				pr.pressureIndex = propertyValue.indexOf("psi");
				pr.conversionFactor = UnitConverter.psi_to_mmHg;
				pr.units="psi";
			}
		} else if (propertyValue.contains("upa")) {
			pr.pressureIndex = propertyValue.indexOf("upa");
			pr.conversionFactor = UnitConverter.uPa_to_mmHg;
			pr.units="upa";
		} else if (propertyValue.contains("npa")) {
			pr.pressureIndex = propertyValue.indexOf("npa");
			pr.conversionFactor = UnitConverter.nPa_to_mmHg;
			pr.units="npa";
		}

		//		try {
		//			double pressure = Double.parseDouble(pressureStr);
		//			er.pressure_mmHg = "" + pr.conversionFactor*pressure;
		//		} catch (NumberFormatException ex) {
		//			// NumberFormatException means no numerical value was found; leave foundNumeric = false and do nothing else
		//		}
		return pr;
	}

	private static void setValue(ExperimentalRecord er, PressureUnitsResults pur, String pressureStr) {

		try {
			double pressure = Double.parseDouble(pressureStr);
			er.pressure_mmHg = "" + pur.conversionFactor*pressure;
			
			//System.out.println("In setValue: "+er.pressure_mmHg+"\tOriginalUnits="+pur.units+"\tPropertValueString="+er.property_value_string);
		} catch (Exception ex) {
			return;
		}


	}

	/**
	 * Finds the scientific notation value that is closest in location to the unitsIndex
	 * 
	 * @param er
	 * @param propertyValue
	 * @param unitsIndex
	 * @param unitsIndexOriginal
	 * @param foundNumeric
	 * @return
	 */
	public static boolean findClosestScientificNotationValuePressure(ExperimentalRecord er, String propertyValue,
			PressureUnitsResults pur, boolean foundNumeric) {

		try {
			Matcher sciMatcher = Pattern.compile("([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]?(e|x[ ]?10\\^?|\\*?10\\^)[ ]?[\\(]?([-|\\+]?[ ]?[0-9]+)[\\)]?").matcher(propertyValue.toLowerCase().substring(0,pur.pressureIndex));
			//			Matcher sciMatcher = Pattern.compile("([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]?(e|x[ ]?10\\?|\\*?10\\^)[ ]?[\\(]?([-|\\+]?[ ]?[0-9]+)[\\)]?").matcher(propertyValue.toLowerCase().substring(0,unitsIndex));
			int minDiff=9999;
			String strMantissaMin="";
			String strMagnitudeMin="";

			int count=0;

			while (sciMatcher.find()) {

				count++;
				String strMantissa = sciMatcher.group(1).trim();
				String strMagnitude = sciMatcher.group(3);

				//				int propertyValueIndex2=propertyValue.indexOf(strMantissa);
				int propertyValueIndex2=sciMatcher.start(1);

				//				System.out.println(propertyValueIndex2+"\t"+propertyValueIndex2b+"\t"+unitsIndex);

				int diff=pur.pressureIndex-propertyValueIndex2;

				if(diff<minDiff) {
					minDiff=diff;
					strMantissaMin=strMantissa;
					strMagnitudeMin=strMagnitude;
				}
			}

			if(!strMagnitudeMin.isBlank()) {

				//				if(count>1)
				//					System.out.println(strMantissaMin+"\t"+strMagnitudeMin+"\t"+er.property_value_units_original+"\t"+propertyValue);

				foundNumeric = true;

				Double mantissa = Double.parseDouble(strMantissaMin.replaceAll("\\s",""));
				Double magnitude =  Double.parseDouble(strMagnitudeMin.replaceAll("\\s","").replaceAll("\\+", ""));

				DecimalFormat df=new DecimalFormat("0.00E00");

				double Poriginal=mantissa*Math.pow(10, magnitude);
				double Pconverted=pur.conversionFactor*Poriginal;

				er.pressure_mmHg = df.format(Pconverted);
				
//				System.out.println("here1:"+propertyValue+"\t"+er.pressure_mmHg);

				if(Pconverted<0) {				
					System.out.println(propertyValue+"\tPressure= " + er.pressure_mmHg);
				}
			}

		} catch (Exception ex) {
			//			System.out.println(propertyValue);
			//			ex.printStackTrace();
		}
		return foundNumeric;
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
		if(pur==null) {
			return;
		}

		// If any pressure units were found, looks for the last number that precedes them
		boolean foundNumeric = false;

		if(pur.pressureIndex<=0) return;
		if (!sourceName.contains(ExperimentalConstants.strSourceEChemPortal) && !sourceName.contains(ExperimentalConstants.strSourcePubChem)) return;


		//boolean foundNumeric = false;

		//				foundNumeric = findClosestScientificNotationValuePressure(er, propertyValue, pur, foundNumeric);
		try {

			String substring=propertyValue.substring(0,pur.pressureIndex);
			if(substring.contains(" at ")) {
				int start=substring.indexOf(" at ")+4;
				int stop=substring.length();
				//				System.out.println(substring+"\t"+start+"\t"+stop);
				substring=substring.substring(start,stop).trim();
				//				System.out.println("here:"+substring);
				//				System.out.println("substring with at="+substring);
			}

			if(substring.contains(" @ ")) {
				int start=substring.indexOf(" @ ")+3;
				int stop=substring.length();
				//				System.out.println(substring+"\t"+start+"\t"+stop);
				substring=substring.substring(start,stop).trim();
				//				System.out.println("here:"+substring);
				//				System.out.println("substring with at="+substring);
			}
			
			setValue(er, pur, substring);
			if(er.pressure_mmHg!=null) return;


			Matcher m = Pattern.compile("[-]?[0-9]*\\.?[0-9]+").matcher(substring);
			String pressureStr = "";

			while (m.find()) { 
				pressureStr = m.group();
			}
			
			if(pressureStr.length()==0) return;

			//double pressure = Double.parseDouble(pressureStr);
			double[] range = TextUtilities.extractClosestDoubleRangeFromString(substring,substring.length());
			//foundNumeric = findClosestScientificNotationValuePressure(er, substring, pur, foundNumeric);
			boolean foundSciNotation = TemperatureCondition.findScientificNotationInString(substring, substring.length());

			if (foundSciNotation) {

//				if (range!= null) {
//					System.out.println("Have sci notation and a range:"+substring);
//					
//				} else {
////					System.out.println("Have sci notation:"+substring+"\t"+isRangeNull);
//					findClosestScientificNotationValuePressure(er, propertyValue, pur, foundNumeric);
//				}
//				System.out.println("sci notation in pressure:"+substring);
				findClosestScientificNotationValuePressure(er, propertyValue, pur, foundNumeric);

			} else {
				if (range!= null) {//no sci notation, have range:
					double min = range[0];
					double max = range[1]; 
					double diff = max-min;
					//er.pressure_mmHg = min+"-"+max;
					//foundNumeric = true;
					if (diff!=0) {
						min = min*pur.conversionFactor;
						max = max*pur.conversionFactor;
						double avg=(min+max)/2.0;

						er.pressure_mmHg =  "" + avg;
						
						//System.out.println(er.property_name+"\trange for pres cond:"+propertyValue+"\t"+er.pressure_mmHg+"\t"+pur.units);
						
					}
				} else {
					setValue(er,pur, pressureStr);
					
				}
			}
			
			//				if (!foundNumeric) {
			//					try {
			//
			//						double[] range = TextUtilities.extractAltFormatRangeFromString(propertyValue,pur.pressureIndex);
			//						if (range!=null) {
			//							String min = TextUtilities.formatDouble(range[0]*pur.conversionFactor);
			//							String max = TextUtilities.formatDouble(range[1]*pur.conversionFactor);
			//							er.pressure_mmHg = min+"-"+max;
			//							foundNumeric = true;
			//						}
			//
			//					} catch (Exception ex) {
			//						ex.printStackTrace();
			//					}
			//					
			//				}
			//				if (!foundNumeric) {
			//					try {
			//						Matcher caMatcher = Pattern.compile(".*?(ca. )?([-]?[ ]?[0-9]*\\.?[0-9]+)( ca. )([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(propertyValue.substring(0,pur.pressureIndex));
			//						if (caMatcher.find()) {
			//							String numQual = caMatcher.group(1).isBlank() ? "" : "~";
			//							String min = TextUtilities.formatDouble(Double.parseDouble(caMatcher.group(2)));
			//							String max = TextUtilities.formatDouble(Double.parseDouble(caMatcher.group(4)));
			//							er.pressure_mmHg = numQual+min+"~"+max;
			//							foundNumeric = true;
			//						}
			//					} catch (Exception ex) {
			//						ex.printStackTrace();
			//					}
			//				}
		} catch (Exception ex) {
			ex.printStackTrace();
		}



		//			if (!foundNumeric) {
		//				try {
		//					er.pressure_mmHg = TextUtilities.formatDouble(pur.conversionFactor*TextUtilities.extractLastDoubleFromString(propertyValue,pur.pressureIndex));
		//					foundNumeric = true;
		//				} catch (NumberFormatException ex) {
		//					// NumberFormatException means no numerical value was found; leave foundNumeric = false and do nothing else
		//				}
		//			}
		//			if (propertyValue.startsWith("ca.")) {
		//				er.pressure_mmHg = "~"+er.pressure_mmHg;
		//			}
	}
	

	
	public static void main(String[] args) {
		String[] inputStrings = {"crystals. mp: 147.5-149 °c. uv max (methanol): 228, 260, 310 nm (log epsilon 4.6, 4.3, 3.9)",
				"Ammonium: 148°c",
				"75°c at 148 mm",
				"43-45°c at 500 psi",
				"43-45°c (75 psi)",
				"75(psi)",
				"epa",
				"pa"
		};
		String[] exclusions = {"epsi", "ammonium"};
		//String targets[] = {"psi", "mm"};
        //String biggerStrings[] = {"epsilon", "ammonium"};
        
        for (String text : inputStrings) {
        	//for(String exclusion:exclusions) {
        	//findOccurrences(text, "psi", exclusions);
        	findInstances(text,"psi");
        	findInstances(text,"pa");
        		//findOccurrences(text, "mm", exclusion);
        	}
        	
        }

	
//    private static void findOccurrences(String text, String target, String exclusions[]) {
//    	for(String exclusion:exclusions) {
//    		String regex = "(?<!\\b" + exclusion + "\\S*{1,999})\\b" + target + "\\b";
//    		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
//    		Matcher matcher = pattern.matcher(text);
//        
//    		while (matcher.find()) {
//    			System.out.println("Found '" + target + "' in: " + text + "	because of	" + exclusion);
//    		}
//    	}
//    }
    
    public static boolean findInstances(String text, String target) {
        String regex = "\\b" + target + "\\b"; // Word boundary regex
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        boolean foundInstance = false;
        while (matcher.find()) {
            //System.out.println("Found '" + target + "' in: " + text);
            foundInstance = true;
        } return foundInstance;
    }

}
