package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Utility;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods to fix garbage characters in data
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class TextProcessing {
	public static String formatDouble(double d) {
		DecimalFormat df2 = new DecimalFormat("0.###");
		DecimalFormat dfSci = new DecimalFormat("0.0##E0");
		if (d < 0.01) {
			return dfSci.format(d);
		} else {
			return df2.format(d);
		}

	}
	
	public static String fixChars(String str) {
		StringBuilder sb = new StringBuilder(str);
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
}
