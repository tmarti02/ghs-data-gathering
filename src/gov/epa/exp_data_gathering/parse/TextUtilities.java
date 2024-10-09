package gov.epa.exp_data_gathering.parse;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.api.ExperimentalConstants;

public class TextUtilities {

	public static String formatDouble(double d) {
		DecimalFormat df2 = new DecimalFormat("0.###");
		DecimalFormat dfSci = new DecimalFormat("0.0##E0");
		if (d < 0.01) {
			return dfSci.format(d);
		} else {
			return df2.format(d);
		}
	
	}

	public static double[] extractAltFormatRangeFromString(String str,int end) throws Exception {
			Matcher anyRangeMatcher = Pattern.compile(">[=]?[ ]?([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]?<[=]?[ ]?([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(str.substring(0,end));
			
			//TODO this will find the first instance of a range- but do we want the closest to the unitsIndex
			// may need while loop 
			
			if (anyRangeMatcher.find()) {
				String strMin = anyRangeMatcher.group(1).replace(" ","");
				String strMax = anyRangeMatcher.group(2).replace(" ","");
				double min = Double.parseDouble(strMin);
				double max = Double.parseDouble(strMax);
				if (min >= max) {
					int digits = strMax.length();
					if (digits > strMin.length()) {
						// If max value is smaller but digitwise longer, swaps the values
						double temp = min;
						min = max;
						max = temp;
					} else {
						// Otherwise replaces substring
						strMax = strMin.substring(0,strMin.length()-digits)+strMax;
						max = Double.parseDouble(strMax);
					}
				}
				double[] range = {min, max};
				
	//			System.out.println("alt range found: "+min+"\t"+max+"\t"+str);
				
				return range;
			} else {
	//			System.out.println("couldnt find range:"+str);
				return null;
			}
		}

	/**
		 * Extracts the number closest to index of unitsIndex
		 * If end=length, it will use last match
		 * 
		 * @param str	The string to be read
		 * @param end	The index to stop searching
		 * @return		The number found as a double
		 * @throws IllegalStateException	If no number is found in the given range
		 */
		public static Double extractClosestDoubleFromString(String str,int end,String propertyName) throws NumberFormatException {
			Matcher numberMatcher = Pattern.compile("[-]?[ ]?[0-9]*\\.?[0-9]+").matcher(str.substring(0,end));
			
			
			int minDiff=999;
			String strDoubleBest=null;
			
			List<String>lines=new ArrayList<>();
			
			while (numberMatcher.find()) {
				String strDouble = numberMatcher.group();
	//			int index=str.indexOf(strDouble);
				int index=numberMatcher.start();
				
				int diff=end-index;
				if(diff<minDiff) {
					minDiff=diff;
					strDoubleBest=strDouble;
				}
				lines.add(strDouble);
				
	//			System.out.println(counter+"\t"+strDouble+"\t"+index+"\t"+end+"\t"+str);
			}
			
			if(lines.size()>1) {
				if(propertyName.equals(ExperimentalConstants.strLogKOW)) {
					
	//				System.out.println(str);
	//				for(int i=0;i<lines.size();i++) {
	//					System.out.println((i+1)+"\t"+lines.get(i));
	//				}
	//				
	////				System.out.println("end:"+end);
	//				System.out.println("Best:"+strDoubleBest+"\n");
					
				}
			}
			
			if(strDoubleBest==null) {
	//			System.out.println("Null value:\t"+str);
				return null;
			}
			
			
			if(strDoubleBest.isBlank()) {
				if(str.contains("Relative density of the vapour/air-mixture")) {//for some reason this doesnt get handled by regex above
					strDoubleBest=str.substring(str.indexOf(":")+1,str.length()).trim();
	//				System.out.println(str+"\t"+strDouble);
				}
			}
			
			return Double.parseDouble(strDoubleBest.replace(" ",""));
		}

	/**
		 * Looks for a range of values closest to unitsIndex
		 * 
		 * @param str
		 * @param end
		 * @return
		 * @throws IllegalStateException
		 */
		public static double[] extractClosestDoubleRangeFromString(String str,int end) throws IllegalStateException {
			Matcher anyRangeMatcher = Pattern.compile("([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]*([—]|[-]{1}|to|/|ca\\.|[\\?])[ ]*([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(str.substring(0,end));
	
			int counter=0;
			
			List<String>lines=new ArrayList<>();
			
			
			int minDiff=9999;
			
			String strMinBest=null;
			String strMaxBest=null;
			
			while (anyRangeMatcher.find()) {
				counter++;
				
				String strMin = anyRangeMatcher.group(1).replace(" ","");
				String strMax = anyRangeMatcher.group(3).replace(" ","");
				
	//			int indexMin=str.indexOf(anyRangeMatcher.group(1));//TODO need to use start method instead
				int indexMin=anyRangeMatcher.start(1);
				
				int diff=end-indexMin;
				
				if(diff<minDiff) {
					minDiff=diff;
					strMinBest=strMin;
					strMaxBest=strMax;
				}
				String minmax=strMin+" to "+strMax+"\t"+indexMin+"\t"+end;
				lines.add(minmax);
	
			}
			
	//		if(lines.size()>1) {
	//			for(int i=0;i<lines.size();i++) {
	//				System.out.println((i+1)+"\t"+lines.get(i)+"\t"+str);
	//			}
	//		}
			
			if(strMinBest==null) return null;
	
			String strMax=strMaxBest;
			String strMin=strMinBest;
			double min=Double.parseDouble(strMin);
			double max=Double.parseDouble(strMax);
			
			if (min > max) {
	//			System.out.println("min>max,"+min+"\t"+max+"\t"+str);
				int digits = strMax.length();
				if (digits > strMin.length() || (digits == strMin.length() && strMin.startsWith("-") && strMax.startsWith("-")) || strMax.equals("0")) {
					// Swaps values for negative ranges
					double temp = min;
					min = max;
					max = temp;
				} else {
					// Otherwise replaces substring
					strMax = strMin.substring(0,strMin.length()-digits)+strMax;
					try {
						max = Double.parseDouble(strMax);
					} catch (Exception ex) {
						System.out.println("Failed range correction: "+str);
					}
				}
			}
			
	//		if(lines.size()>1) {
	//			System.out.println("Range found:"+min+"\t"+max);
	//		}
			
			double[] range = {min, max};
			return range;
	
			
		}

	/**
		 * Extracts the first range of numbers before a given index in a string
		 * 
		 * @param str	The string to be read
		 * @param end	The index to stop searching
		 * @return		The range found as a double[2]
		 * @throws IllegalStateException	If no number range is found in the given range
		 */
		public static double[] extractFirstDoubleRangeFromString(String str,int end) throws IllegalStateException {
			Matcher anyRangeMatcher = Pattern.compile("([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]*([—]|[-]{1}|to|ca\\.||/||�|[\\?])[ ]*([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(str.substring(0,end));
			if (anyRangeMatcher.find()) {
	//			System.out.println("extractFirstDoubleRangeFromString: Group2="+anyRangeMatcher.group(2));
				String strMin = anyRangeMatcher.group(1).replace(" ","");
				String strMax = anyRangeMatcher.group(3).replace(" ","");
				double min = Double.parseDouble(strMin);
				double max = Double.parseDouble(strMax);
				if (min > max) {
					int digits = strMax.length();
					if (digits > strMin.length() || (digits == strMin.length() && strMin.startsWith("-") && strMax.startsWith("-")) || strMax.equals("0")) {
						// Swaps values for negative ranges
						double temp = min;
						min = max;
						max = temp;
					} else {
						// Otherwise replaces substring
						strMax = strMin.substring(0,strMin.length()-digits)+strMax;
						try {
							max = Double.parseDouble(strMax);
						} catch (Exception ex) {
	//						System.out.println("Failed range correction: "+str);
						}
					}
				}
				double[] range = {min, max};
				return range;
			} else {
				return null;
			}
		}

	/**
		 * Extracts the last number before a given index in a string
		 * @param str	The string to be read
		 * @param end	The index to stop searching
		 * @return		The number found as a double
		 * @throws IllegalStateException	If no number is found in the given range
		 */
		public static double extractLastDoubleFromString(String str,int end) throws NumberFormatException {
			Matcher numberMatcher = Pattern.compile("[-]?[ ]?[0-9]*\\.?[0-9]+").matcher(str.substring(0,end));
			String strDouble = "";
			
			int counter=0;
			while (numberMatcher.find()) {
				counter++;
				strDouble = numberMatcher.group();
				int index=str.indexOf(strDouble);
	//			System.out.println(counter+"\t"+strDouble+"\t"+index+"\t"+end+"\t"+str);
			}
			
			
			if(strDouble.isBlank()) {
				if(str.contains("Relative density of the vapour/air-mixture")) {//for some reason this doesnt get handled by regex above
					strDouble=str.substring(str.indexOf(":")+1,str.length()).trim();
	//				System.out.println(str+"\t"+strDouble);
				}
			}
			
			return Double.parseDouble(strDouble.replace(" ",""));
		}

	public static String fixChars(String str) {
		StringBuilder sb = new StringBuilder(str);
		try {
		replaceAll(sb,"Ã¢â¬â","-");
		replaceAll(sb,"Ã¢â¬â¢","'");
		replaceAll(sb,"\uff08", "(");// Ã¯Â¼Ë
		replaceAll(sb,"\uff09", ")");// Ã¯Â¼â°
		replaceAll(sb,"\uff0f", "/");// Ã¯Â¼ï¿½
		replaceAll(sb,"\u3000", " ");//blank
		replaceAll(sb,"\u00a0", " ");//blank
		replaceAll(sb,"\u2003", " ");//blank
		replaceAll(sb,"\u0009", " ");//blank
		replaceAll(sb,"\u300c", "");// Ã£â¬Å
		replaceAll(sb,"\u300d", "");// Ã£â¬ï¿½
		replaceAll(sb,"\u2070", "^0");// superscript 0
		replaceAll(sb,"\u00B9", "^1");// superscript 1
		replaceAll(sb,"\u00B2", "^2");// superscript 2
		replaceAll(sb,"\u00B3", "^3");// superscript 3
		replaceAll(sb,"\u2074", "^4");// superscript 4
		replaceAll(sb,"\u2075", "^5");// superscript 5
		replaceAll(sb,"\u2076", "^6");// superscript 6
		replaceAll(sb,"\u2077", "^7");// superscript 7
		replaceAll(sb,"\u2078", "^8");// superscript 8
		replaceAll(sb,"\u2079", "^9");// superscript 9
		replaceAll(sb,"\u2080", "_0");// subscript 0
		replaceAll(sb,"\u2081", "_1");// subscript 1
		replaceAll(sb,"\u2082", "_2");// subscript 2
		replaceAll(sb,"\u2083", "_3");// subscript 3
		replaceAll(sb,"\u2084", "_4");// subscript 4
		replaceAll(sb,"\u2085", "_5");// subscript 5
		replaceAll(sb,"\u2086", "_6");// subscript 6
		replaceAll(sb,"\u2087", "_7");// subscript 7
		replaceAll(sb,"\u2088", "_8");// subscript 8
		replaceAll(sb,"\u2089", "_9");// subscript 9
		} catch (Exception ex) {
			System.out.println(sb.toString());
		}
		return sb.toString();
	}

	public static String reverseFixChars(String str) {
		StringBuilder sb = new StringBuilder(str);
		replaceAll(sb,"\\^0","\u2070");// superscript 0
		replaceAll(sb,"\\^1","\u00B9");// superscript 1
		replaceAll(sb,"\\^2","\u00B2");// superscript 2
		replaceAll(sb,"\\^3","\u00B3");// superscript 3
		replaceAll(sb,"\\^4","\u2074");// superscript 4
		replaceAll(sb,"\\^5","\u2075");// superscript 5
		replaceAll(sb,"\\^6","\u2076");// superscript 6
		replaceAll(sb,"\\^7","\u2077");// superscript 7
		replaceAll(sb,"\\^8","\u2078");// superscript 8
		replaceAll(sb,"\\^9","\u2079");// superscript 9
		replaceAll(sb,"_0","\u2080");// subscript 0
		replaceAll(sb,"_1","\u2081");// subscript 1
		replaceAll(sb,"_2","\u2082");// subscript 2
		replaceAll(sb,"_3","\u2083");// subscript 3
		replaceAll(sb,"_4","\u2084");// subscript 4
		replaceAll(sb,"_5","\u2085");// subscript 5
		replaceAll(sb,"_6","\u2086");// subscript 6
		replaceAll(sb,"_7","\u2087");// subscript 7
		replaceAll(sb,"_8","\u2088");// subscript 8
		replaceAll(sb,"_9","\u2089");// subscript 9
		return sb.toString();
	}

	private static void replaceAll(StringBuilder sb, String find, String replace){
	    
	    //compile pattern from find string
	    Pattern p = Pattern.compile(find);
	    
	    //create new Matcher from StringBuilder object
	    Matcher matcher = p.matcher(sb);
	    
	    //index of StringBuilder from where search should begin
	    int startIndex = 0;
	    
	    while (matcher.find(startIndex)) {
	        
	        sb.replace(matcher.start(), matcher.end(), replace);
	        
	        //set next start index as start of the last match + length of replacement
	        startIndex = matcher.start() + replace.length();
	    }
	}

	public static String correctDegreeSymbols(String s) {
			StringBuilder sb = new StringBuilder(s);
			
			List<String>symbols=Arrays.asList("\u00BA","\u1D52","\u02DA","\u309C","\u18DE","\u2070",
					"\u2218","\u29B5","\u1BC8","u26AC");//TODO TMM are these correct? Seems like a lot of symbols
				
			String s_new=s;
			
			for (String symbol:symbols) {
				s_new=s_new.replace(symbol, "\u00B0");
			}
			
			s_new=s_new.replace("\u2103","\u00B0C");
			s_new=s_new.replace("\u2109","\u00B0F");
					
	//		replaceAll(sb,"[]","\u00B0");
			
			s_new = s_new.replace("&deg;","\u00B0"); 
			
			
			return s_new;
		}

	public static String getNumericQualifier(String str,int index) {
		String symbol = "";
		if (index > 0) {
			if (str.charAt(index-1)=='>' || str.toLowerCase().contains("above") || str.toLowerCase().contains("more than")) {
				symbol = ">";
			} else if (str.charAt(index-1)=='?') {
				symbol = "?";
			} else if (str.charAt(index-1)=='<' || str.toLowerCase().contains("below")) {
				symbol = "<";
			} else if (str.charAt(index-1)=='~' || str.contains("ca.") || str.contains("circa") || str.toLowerCase().contains("approx") || str.contains("near") || str.toLowerCase().contains("approximately")) {
				symbol = "~";
			} else if (index > 1 && str.charAt(index-2)=='>' && str.charAt(index-1)=='=' || str.contains("more than=") || str.contains(">/=")) {
				symbol = ">=";
			} else if (index > 1 && str.charAt(index-2)=='<' && str.charAt(index-1)=='=') {
				symbol = "<=";
			}
	
		}
		return symbol;
	}

}
