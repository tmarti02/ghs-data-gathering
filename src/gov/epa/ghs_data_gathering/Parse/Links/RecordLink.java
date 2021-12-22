package gov.epa.ghs_data_gathering.Parse.Links;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.google.gson.JsonObject;

import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

public class RecordLink{
	public String CAS;
	public String Name;
	public String SourceName;
	public String LinkName;
	public String URL;
	
	public static String [] fieldNames= {"CAS","Name","SourceName","LinkName","URL"};
	public static String sourceName="MDH Links";
	
	public String toString() {
		return toString("\t");
	}
	
	public String toString(String del) {
		return CAS+del+Name+del+SourceName+del+LinkName+del+URL;
	}

	public static Vector<JsonObject> parseRecordsFromExcel() {
		
		HashMap<Integer,String> hm = ExcelSourceReader.generateDefaultMap(fieldNames, 0);
		
		Vector<JsonObject> recordsAll=new Vector<>();
		
		try {
			FileInputStream fis = new FileInputStream(new File("AA Dashboard\\Data\\MDH Links\\excel files\\MDH Links.xlsx"));
			Workbook wb = WorkbookFactory.create(fis);
			
			
			for (int i=0;i<wb.getNumberOfSheets();i++) {
				Sheet sheet = wb.getSheetAt(i);
				Vector<JsonObject> records = ExcelSourceReader.parseRecordsFromExcel(sheet,hm);
			
				recordsAll.addAll(records);
			}

			
			for (JsonObject record:recordsAll) {
				System.out.println(record);
			}
			
			return recordsAll;


		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	public static void main(String[] args) {
		parseRecordsFromExcel();
	}

	public static String getHeader(String del) {
		return "CAS"+del+"Name"+del+"SourceName"+del+"LinkName"+del+"URL";	
	}
}
