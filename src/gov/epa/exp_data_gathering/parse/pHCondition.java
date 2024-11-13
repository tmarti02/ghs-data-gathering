package gov.epa.exp_data_gathering.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author TMARTI02
*/
public class pHCondition {

	/**
		 * Sets the pH condition for an ExperimentalRecord object, if present
		 * @param er			The ExperimentalRecord object to be updated
		 * @param propertyValue	The string to be read
		 * @return				The pH
		 *
		 * TODO add ability to find ranges in pH values
		 *
		 */
		public static void get_pH_Condition(ExperimentalRecord er, String propertyValue) {
			
			if(!propertyValue.contains("pH")) return;
			
	//		System.out.println(propertyValue);
			int pHindex = propertyValue.indexOf("pH");
			String substring=propertyValue.substring(pHindex,propertyValue.length());
			// If pH string found, looks for the last number that precedes them
			if (pHindex > 0) {
				try {
					Matcher m = Pattern.compile("[-]?[0-9]*\\.?[0-9]+").matcher(substring);
					String pHStr = "";
					
					int counter=0;
	
					while (m.find()) {
						counter++;
						pHStr = m.group();
						if (pHStr.length()!=0) break;//use first one
					}
					
					if (pHStr.length()!=0) {
						double[] range = TextUtilities.extractClosestDoubleRangeFromString(substring,substring.length());
						if(range!= null) {
							double min = range[0];
							double max = range[1];
							double diff = max-min;
							
							if (diff!=0) {
								double avg=(min+max)/2.0;
								er.pH = "" + avg;
								er.note = "pH averaged";
							}
						} else {
							er.pH = pHStr;
						} 
					} else {
							er.pH = pHStr;
						
	//					if(!propertyValue.contains("The mean of the results")) {
	//						System.out.println(propertyValue+"\t"+er.property_value_point_estimate_original+"\t"+er.pH);	
	//					}
					}
					
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

}
