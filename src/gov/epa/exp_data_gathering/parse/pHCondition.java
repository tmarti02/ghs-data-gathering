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
			
			// If pH string found, looks for the last number that precedes them
			if (pHindex > 0) {
				try {
					Matcher m = Pattern.compile("[-]?[0-9]*\\.?[0-9]+").matcher(propertyValue.substring(pHindex,propertyValue.length()));
					String tempStr = "";
					
					int counter=0;
	
					while (m.find()) {
						counter++;
						tempStr = m.group();
						if (tempStr.length()!=0) break;//use first one
					}
					
					if (tempStr.length()!=0) {
						// Converts to C as needed
						er.pH = tempStr;
						
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
