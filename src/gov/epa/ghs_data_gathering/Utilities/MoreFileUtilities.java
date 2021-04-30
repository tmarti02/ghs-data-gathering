package gov.epa.ghs_data_gathering.Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class MoreFileUtilities {
	public static List<String> getListFromFile(String filePath, int sheetNum, int col, boolean hasHeader) {
		if (filePath.endsWith("csv")) {
			return getListFromCSV(filePath, ",", col, hasHeader);
		} else if (filePath.endsWith("tsv") || filePath.endsWith("txt")) {
			return getListFromCSV(filePath, "\t", col, hasHeader);
		} else if (filePath.endsWith("xlsx") || filePath.endsWith("xls")) {
			return getListFromXLSX(filePath, sheetNum, col, hasHeader);
		} else {
			System.out.println("Unrecognized file type.");
			return null;
		}
	}
	
	public static HashMap<String,String> getSimpleDictFromFile(String filePath, int sheetNum, int keyCol, int valueCol, boolean hasHeader) {
		if (filePath.endsWith("csv")) {
			return getSimpleDictFromCSV(filePath, ",", keyCol, valueCol, hasHeader);
		} else if (filePath.endsWith("tsv") || filePath.endsWith("txt")) {
			return getSimpleDictFromCSV(filePath, "\t", keyCol, valueCol, hasHeader);
		} else if (filePath.endsWith("xlsx") || filePath.endsWith("xls")) {
			return getSimpleDictFromXLSX(filePath, sheetNum, keyCol, valueCol, hasHeader);
		} else {
			System.out.println("Unrecognized file type.");
			return null;
		}
	}
	
	private static List<String> getListFromCSV(String filePath, String delimiter, int col, boolean hasHeader) {
		List<String> list = new ArrayList<String>();
		try {
			File file = new File(filePath);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line="";
			if (hasHeader) { br.readLine(); }
			while ((line=br.readLine())!=null) {
				String[] cells = line.split(delimiter);
				String id = cells[col];
				list.add(id);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	private static List<String> getListFromXLSX(String filePath, int sheetNum, int col, boolean hasHeader) {
		List<String> list = new ArrayList<String>();
		try {
			Workbook wb = WorkbookFactory.create(new File(filePath), null, true);
			Sheet sheet = wb.getSheetAt(sheetNum);
			DataFormatter df = new DataFormatter();
			for (Row row:sheet) {
				if (hasHeader && row.getRowNum()==0) { continue; }
				Cell cell = row.getCell(col);
				list.add(df.formatCellValue(cell));
			}
			wb.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	private static HashMap<String,String> getSimpleDictFromCSV(String filePath, String delimiter, int keyCol, int valueCol, boolean hasHeader) {
		HashMap<String,String> dict = new HashMap<String,String>();
		try {
			File file = new File(filePath);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			if (hasHeader) { br.readLine(); }
			while ((line = br.readLine())!=null) {
				String[] cells = line.split(delimiter);
				dict.put(cells[keyCol], cells[valueCol]);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return dict;
	}
	
	private static HashMap<String,String> getSimpleDictFromXLSX(String filePath, int sheetNum, int keyCol, int valueCol, boolean hasHeader) {
		HashMap<String,String> dict = new HashMap<String,String>();
		try {
			Workbook wb = WorkbookFactory.create(new File(filePath), null, true);
			Sheet sheet = wb.getSheetAt(sheetNum);
			DataFormatter df = new DataFormatter();
			for (Row row:sheet) {
				if (hasHeader && row.getRowNum()==0) { continue; }
				Cell keyCell = row.getCell(keyCol);
				Cell valueCell = row.getCell(valueCol);
				dict.put(df.formatCellValue(keyCell), df.formatCellValue(valueCell));
			}
			wb.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return dict;
	}
}
