package gov.epa.exp_data_gathering.parse;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.PressureCondition.PressureUnitsResults;

/**
* @author TMARTI02
*/
public class TemperatureCondition {

	public static class TempUnitsResult {
		int unitsIndex=1;
		String units;
	}
	
	
	public static boolean findScientificNotationInString(String propertyValue,	int unitsIndex) {
		

		try {
			String strSearch=propertyValue.toLowerCase().substring(0,unitsIndex);
			
			Matcher sciMatcher = Pattern.compile("([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]?(e|x[ ]?10\\^?|\\*?10\\^)[ ]?[\\(]?([-|\\+]?[ ]?[0-9]+)[\\)]?").matcher(strSearch);
//			Matcher sciMatcher = Pattern.compile("([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]?(e|x[ ]?10\\?|\\*?10\\^)[ ]?[\\(]?([-|\\+]?[ ]?[0-9]+)[\\)]?").matcher(propertyValue.toLowerCase().substring(0,unitsIndex));
//			int minDiff=9999;
//			String strMantissaMin="";
//			String strMagnitudeMin="";

			
			String strMantissa="";
			String strMagnitude="";

			int count=0;
			
			
			int diffLast=-1;
			
			while (sciMatcher.find()) {//find last one
				
				count++;
				strMantissa = sciMatcher.group(1).trim();
				strMagnitude = sciMatcher.group(3);
				
//				int propertyValueIndex2=propertyValue.indexOf(strMantissa);
//				int propertyValueIndex2=sciMatcher.start(1);
				
//				System.out.println(propertyValueIndex2+"\t"+propertyValueIndex2b+"\t"+unitsIndex);
								
//				int diff=unitsIndex-propertyValueIndex2;

//				if(diff<minDiff) {
//					minDiff=diff;
//					strMantissaMin=strMantissa;
//					strMagnitudeMin=strMagnitude;
//				}
//				
//				diffLast=diff;
				
			}
			
//			if(diffLast!=minDiff && diffLast!=-1) {
//				System.out.println(propertyValue+"\tdiffLast!=minDiff\t"+minDiff+"\t"+diffLast);
//			}
						
			if(!strMagnitude.isBlank()) {
//				System.out.println("Found sci notation in "+strSearch);
				return true;
			} 
			
		} catch (Exception ex) {
//			System.out.println(propertyValue);
//			ex.printStackTrace();
		}
		return false;
	}	
	/**
	 * Sets the temperature condition for an ExperimentalRecord object, if present
	 * 
	 * @param er            The ExperimentalRecord object to be updated
	 * @param propertyValue The string to be read
	 * @return The temperature condition in C
	 * 
	 * 
	 * Props with temperature condition: Density, Water solubility, Vapor pressure, LogKow,
	 * Viscosity, Surface tension
	 * 
	 * 
	 */

	public static void getTemperatureCondition(ExperimentalRecord er, String propertyValue) {

		int tempNumberIndex=-1;
		
		propertyValue=propertyValue.replace("log Kow = ", "").replace("deg K","K").replace("log Kow=","").replace("Log Kow =","").replace("Kow =", "");		

		//		propertyValue=propertyValue.replace("propertyValue=", "");


		DecimalFormat df2 = new DecimalFormat("0.#");

		//		System.out.println("here1 propertyValue="+propertyValue);

		TempUnitsResult unitsResult=getTemperatureUnits(propertyValue,er);

		if(unitsResult==null) {
			//			if(er.property_name.equals(ExperimentalConstants.strVaporPressure))			
			//				System.out.println("cant get temp units for:"+propertyValue);
			//			System.out.println("Cant find temp units for "+propertyValue);
			return;
		}



		// If temperature units were found, looks for the last number that precedes them

		//		if (unitsResult.unitsIndex > 0) {
		//			
		//				double[] range = TextUtilities.extractClosestDoubleRangeFromString(propertyValue,unitsResult.unitsIndex);
		//				foundSciNot =findScientificNotationValue(er,propertyValue, unitsResult.unitsIndex, foundSciNot);

		//				if (range!=null && !foundSciNot) {
		//					String min = TextUtilities.formatDouble(range[0]);
		//					String max = TextUtilities.formatDouble(range[1]);
		//					switch ( unitsResult.units) {
		//						case ExperimentalConstants.str_C:
		//							System.out.println("Min-Max= " + min+"-"+max);
		//							er.temperature_C = min+"-"+max;
		//							break;
		//						case ExperimentalConstants.str_F:
		//							double minConvertedF = UnitConverter.F_to_C(Double.valueOf(min));
		//							double maxConvertedF = UnitConverter.F_to_C(Double.valueOf(max));
		//							er.temperature_C = "" + minConvertedF +'-'+ maxConvertedF;
		//							break;
		//						case ExperimentalConstants.str_K:
		//							double minConvertedK = UnitConverter.K_to_C(Double.valueOf(min));
		//							double maxConvertedK = UnitConverter.K_to_C(Double.valueOf(max));
		//							er.temperature_C = "" + minConvertedK + '-' + maxConvertedK;
		//							break;
		//					}
		//				}

		try {

			String substring=propertyValue.substring(0,unitsResult.unitsIndex);


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


			//First try to get from substring:
			setValue(er, unitsResult, substring, df2);
			if(er.temperature_C!=null) return ;


			//			if(substring.contains("to") || substring.contains("-")) {
			//				System.out.println(er.property_name+"\tpropertyValue="+propertyValue+"\tsubstring="+substring+"\t"+unitsResult.units);				
			//			}


			//			System.out.println(propertyValue+"\t"+substring);


			Matcher m = Pattern.compile("[-]?[0-9]*\\.?[0-9]+").matcher(substring);

			
			String tempStr = "";

			//			int minDiff=9999;
			//			String strClosest=null;

			while (m.find()) { 
				tempStr = m.group();
				tempNumberIndex=m.start();
			}
			//here tempStr is last number in string
			

			//			if(propertyValue.contains(" to "))			
			//				System.out.println(er.property_name+"\tpropertyValue="+propertyValue+"\tsubstring="+substring+"\ttempStr="+tempStr+"\t"+unitsResult.units);


			//			if(true)return;			

			//	int index=propertyValue.indexOf(tempStr);
			//				int diff=unitsResult.unitsIndex-m.start();

			//				if(diff<minDiff) {
			//					minDiff=diff;
			//					strClosest=tempStr;
			//				}
			//			}


			if(tempStr.length()==0) return;//no number 


			List<Double>numbers=TextUtilities.getNumbers(substring,substring.length());
			

			//double temp = Double.parseDouble(tempStr);
			double[] range = TextUtilities.extractClosestDoubleRangeFromString(substring,substring.length());
			boolean foundSciNotation = findScientificNotationInString(substring, substring.length());


			boolean useRange=false;
			if(range!=null) {
				useRange=true;
				if(numbers.size()>2 & numbers.get(0)==range[0]) {//if we have more than 2 numbers and the first one matches the lower bound of the range, dont use the range to set the temp condition
					useRange=false;
//					System.out.println("\n"+substring);
//					System.out.println(range[0]+" to "+range[1]);
//					for(int i=0;i<numbers.size();i++) {
//						System.out.println((i+1)+"\t"+numbers.get(i));
//					}
				}
			}

			
			if(useRange && !foundSciNotation) {

				double min = range[0];
				double max = range[1];
				double diff = max-min;

				//					System.out.println("min="+min+"\tmax="+max);

				if (diff!=0) {

					//System.out.println("tempStr= " + tempStr + "	temp= " + temp);
					//if (!foundSciNot) {

					if(unitsResult.units.equals(ExperimentalConstants.str_F)) {
						min = UnitConverter.F_to_C(min);
						max =  UnitConverter.F_to_C(max);
					} else if (unitsResult.units.equals(ExperimentalConstants.str_K)) {
						min = UnitConverter.K_to_C(min);
						max = UnitConverter.K_to_C(max);
					}

					double avg=(min+max)/2.0;

					
					er.temperature_C = Double.parseDouble(df2.format(avg));
					er.note = "temperature averaged";

//					System.out.println(er.property_name+"\ttempC from range="+er.temperature_C+"\t"+er.property_value_string);

					//						System.out.println(propertyValue+"\t"+tempStr+"\t"+unitsResult.units+"\t"+unitsResult.unitsIndex);
					//						System.out.println(propertyValue+"\t"+substring);


					//						System.out.println(propertyValue+"\t"+er.temperature_C);


					//}
				} else {
					setValue(er, unitsResult, tempStr, df2);

//					System.out.println("tempC="+er.temperature_C+"\t"+er.property_value_string);

				}
			} else {
				//TODO get the temp when not a range
				
//				System.out.println("else:"+er.property_name+"\t"+tempStr+"\t"+er.property_value_string);
								
				setValue(er, unitsResult, tempStr, df2);
				
//				if(er.temperature_C!=null) { 
//					System.out.println("else using tempStr:"+er.property_name+"\t"+er.temperature_C+"\t"+er.property_value_string);
//				}
				
			}

			//				System.out.println(er.temperature_C+"\n");

			
			
			
		}	catch (Exception ex) {
			ex.printStackTrace();
		}
		
//		return tempNumberIndex;
		
	}
	private static void setValue(ExperimentalRecord er, TempUnitsResult unitsResult, String tempStr, DecimalFormat df2) {
		
		try {
			double temp = Double.parseDouble(tempStr);
			
			switch ( unitsResult.units) {
			
			case ExperimentalConstants.str_C:
				er.temperature_C = Double.parseDouble(df2.format(temp));
				break;
			case ExperimentalConstants.str_F:
				er.temperature_C = Double.parseDouble(df2.format(UnitConverter.F_to_C(temp)));
				break;
			case ExperimentalConstants.str_K:
				er.temperature_C = Double.parseDouble(df2.format(UnitConverter.K_to_C(temp)));
				break;
			}
			
//			System.out.println("tempC="+er.temperature_C+"\t"+er.property_value_string);

		} catch (Exception ex) {
			return;
		}
		
		
	}
	
	/**
	 * Gets temp units and index
	 * 
	 * @param propertyValue	The string to be read
	 * @return temp units and index as object
	 */
	public static TempUnitsResult getTemperatureUnits(String propertyValue,ExperimentalRecord er) {

		
		TempUnitsResult result=new TempUnitsResult();
			
		String pv1=propertyValue;
		
		propertyValue=propertyValue.replace("Â","");
		
//		System.out.println("here2 propertyValue="+propertyValue);

		propertyValue = TextUtilities.correctDegreeSymbols(propertyValue);
		
		if(!propertyValue.equals(pv1)) {
//			System.out.println("changed:"+pv1+"\t"+propertyValue);
		}
		
//		System.out.println("here3 propertyValue="+propertyValue);

		
//		if(propertyValue.equals("-27 dec C (closed cup)")) {
//			System.out.println("***propertyValue1="+propertyValue1);
//		}
		
		
		List <String>listC=Arrays.asList("\u00B0C","oC","deg.C","degC","C degrees","degrees C","C deg","degree C","deg. C","dec C","°C","°C","°C", "dg C");
		List <String>listF=Arrays.asList("\u00B0F","oF","degrees F","F deg","° F","degree F","deg. F","°F","°F", "°F");
		List <String>listK=Arrays.asList("K");
		List <String>listD=Arrays.asList("\u00B0");
		
		
		for(String strC:listC) {
			if(propertyValue.contains(strC)) {
				result.units = ExperimentalConstants.str_C;
				result.unitsIndex=propertyValue.indexOf(strC);
				return result;
			}
		}
		
		for(String strF:listF) {
			if(propertyValue.contains(strF)) {
				result.units = ExperimentalConstants.str_F;
				result.unitsIndex=propertyValue.indexOf(strF);
				return result;
			}
		}
		
		for(String strK:listK) {
			boolean foundInstance = PressureCondition.findInstances(propertyValue,"K");
			if(propertyValue.contains(strK) && foundInstance) {
				result.units = ExperimentalConstants.str_K;
				result.unitsIndex=propertyValue.indexOf(strK);
				return result;
			}
		}
		
		//If cant find longer strings, use single character;
		if (propertyValue.indexOf("C") > 0 && Character.isDigit(propertyValue.charAt(propertyValue.indexOf("C")-1))) {
			result.units = ExperimentalConstants.str_C;
			result.unitsIndex=propertyValue.indexOf("C");
//			System.out.println("C by itself:"+propertyValue1);
			return result;
		}
				
		if (propertyValue.indexOf("F") > 0 && Character.isDigit(propertyValue.charAt(propertyValue.indexOf("F")-1))) {
			result.units = ExperimentalConstants.str_F;
			result.unitsIndex=propertyValue.indexOf("F");
//			System.out.println("F by itself:"+propertyValue1);
			return result;
		}
		
		if (propertyValue.indexOf("K") > 0 && Character.isDigit(propertyValue.charAt(propertyValue.indexOf("K")-1))) {
			result.units = ExperimentalConstants.str_K;
			result.unitsIndex=propertyValue.indexOf("K");
//			System.out.println("K by itself:"+propertyValue1);
			return result;
		}
		
		
		for(String strD:listD) {
			if(propertyValue.contains(strD)) {
				result.units = ExperimentalConstants.str_C;
				result.unitsIndex=propertyValue.indexOf(strD);
				er.updateNote("Celsius units assumed");				
				return result;
			}
		}


//		System.out.println("No temp units match:"+propertyValue);
		
		return null;
	}
}
