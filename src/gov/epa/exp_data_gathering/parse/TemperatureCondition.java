package gov.epa.exp_data_gathering.parse;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.api.ExperimentalConstants;


/**
* @author TMARTI02
*/
public class TemperatureCondition {

	public static class TempUnitsResult {
		int unitsIndex=1;
		String units;
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
		
//		System.out.println("here1 propertyValue="+propertyValue);
		
		TempUnitsResult unitsResult=getTemperatureUnits(propertyValue);
		
		if(unitsResult==null) {
//			if(er.property_name.equals(ExperimentalConstants.strVaporPressure))			
//				System.out.println("cant get temp units for:"+propertyValue);
//			System.out.println("Cant find temp units for "+propertyValue);
			return;
		}
		
		
//		int tempIndex = propertyValue.indexOf(units);
//		if (tempIndex==propertyValue.toLowerCase().indexOf("cc")) {
//			tempIndex = propertyValue.indexOf(units,tempIndex+2);
//		}
		
//		if (propertyValue.toLowerCase().indexOf("cc")>-1) {
//			System.out.println("Have cc in:"+propertyValue);
//		}
		
		
		// If temperature units were found, looks for the last number that precedes them
		if (unitsResult.unitsIndex > 0) {
			try {
				
				String substring=propertyValue.substring(0,unitsResult.unitsIndex);
//				System.out.println(propertyValue);
				
				Matcher m = Pattern.compile("[-]?[0-9]*\\.?[0-9]+").matcher(propertyValue.substring(0,unitsResult.unitsIndex));
				String tempStr = "";
				
				int minDiff=9999;
				
				String strClosest=null;
				
				while (m.find()) { 
					tempStr = m.group();
					
					
//					int index=propertyValue.indexOf(tempStr);
					int diff=unitsResult.unitsIndex-m.start();
					
					if(diff<minDiff) {
						minDiff=diff;
						strClosest=tempStr;
					}
				}
				
				
//				if(!strClosest.equals(tempStr)) {
//					System.out.println("Mismatch:"+tempStr+"\t"+strClosest+"\t"+minDiff);
//				}
				
				if (tempStr.length()!=0) {
					// Converts to C as needed
					double temp = Double.parseDouble(tempStr);
					switch ( unitsResult.units) {
					case ExperimentalConstants.str_C:
						er.temperature_C = temp;
						break;
					case ExperimentalConstants.str_F:
						er.temperature_C = UnitConverter.F_to_C(temp);
						break;
					case ExperimentalConstants.str_K:
						er.temperature_C = UnitConverter.K_to_C(temp);
						break;
					}
				}
				
//				System.out.println(er.temperature_C+"\n");
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Gets temp units and index
	 * 
	 * @param propertyValue	The string to be read
	 * @return temp units and index as object
	 */
	public static TempUnitsResult getTemperatureUnits(String propertyValue) {

		
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
		
		
		List <String>listC=Arrays.asList("\u00B0C","oC","deg.C","degC","C degrees","degrees C","C deg","degree C","deg. C","dec C","°C","°C","°C");
		List <String>listF=Arrays.asList("\u00B0F","oF","degrees F","F deg","° F","degree F","deg. F","°F","°F", "°F");
		List <String>listK=Arrays.asList("K");

		
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
			if(propertyValue.contains(strK)) {
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

//		System.out.println("No temp units match:"+propertyValue);
		
		return null;
	}
}
