package gov.epa.ghs_data_gathering.GetData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import gov.epa.ghs_data_gathering.Utilities.Utilities;

public class EChemPortalParse {
	public static final String scoreAmbiguous="-1";
	public static final String scorePositive="1";
	public static final String scoreNegative="0";

	
	
	static  Hashtable <String,String>getECHACASLookup(String filepath) {
		Hashtable <String,String>ht=new Hashtable();

		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));

			String header=br.readLine();

			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				String EC=Line.substring(0, Line.indexOf("\t"));
				String CAS=Line.substring(Line.indexOf("\t")+1,Line.length());
				ht.put(EC, CAS);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return ht;
	}
	
	
	
	/**
	 * Checks if any member of vector is contained in the IOR string
	 * 
	 * @param list
	 * @param IOR
	 * @return
	 */
	public static boolean containsSubstring(Vector<String>list,String IOR) {		
		for (String s:list) {
			if (IOR.contains(s)) return true;
		}
		return false;		
	}
	
	
	/**
	 * Appends string to current string with a semicolon separation
	 * 
	 * @param omitReason
	 * @param omitReasonNew
	 * @return
	 */
	public static String append(String omitReason,String omitReasonNew) {
		if (omitReason.isEmpty()) return omitReasonNew;
		else return omitReason+";"+omitReasonNew;

	}
	
	
	public static int getColNum(Row row,String name) {
		DataFormatter formatter = new DataFormatter();
		
		for (int i=0;i<row.getLastCellNum();i++) {
			Cell cell=row.getCell(i);
			String val=formatter.formatCellValue(cell);
			
			if (val.contentEquals(name)) {
				return i;
			}
			
	    }
		
		return -1;
		
	}
}
