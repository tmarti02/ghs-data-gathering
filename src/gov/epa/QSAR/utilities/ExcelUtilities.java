package gov.epa.QSAR.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtilities {
	
	static DataFormatter formatter = new DataFormatter();

	public static String getValue(DataFormatter formatter, int colName, Row row) {
//		String val=formatter.formatCellValue(row.getCell(colName)).strip();
		String val=formatter.formatCellValue(row.getCell(colName)).trim();
		val=val.replace("\n", "; ");
		return val;
	}
	
	
	/**
	 * Gets the column number for a column header name in the header row
	 * 
	 * @param row
	 * @param name
	 * @return
	 */
	public static int getColNum(Row row,String name) {
		DataFormatter formatter = new DataFormatter();
		for (int i=0;i<row.getLastCellNum();i++) {
			Cell cell=row.getCell(i);
			String val=formatter.formatCellValue(cell);
//			System.out.println(val);
			
			if (val.contentEquals(name)) {
				return i;
			}
	    }
		return -1;
	}

	
	public static XSSFSheet getSheet(String filePath,String sheetName) {
		try {

			FileInputStream inputStream = new FileInputStream(new File(filePath));

			XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
									
			return workbook.getSheet(sheetName);
			

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}

	
	private static void writeTextFileToExcelSheet(XSSFSheet sheet, String filePath) {
		BufferedReader br;
		try {
			Scanner scanner = new Scanner(new FileReader(filePath));

			int row=0;

			while (scanner.hasNext()) {
				String Line=scanner.nextLine();
				
				XSSFRow rowHeader = sheet.createRow(row++);
				
				List<String>vals=CSVUtils.parseLine(Line, "\t");
				
//				String [] vals=Line.split("\t");
				
		        for (int i=0;i<vals.size();i++) {
		        	XSSFCell cell = rowHeader.createCell(i);
					cell.setCellValue(vals.get(i));
		        }
			}
			scanner.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void createSpreadsheetFromTextFiles(String folder,Vector<String>filenames,String outputFileName) {
		
		XSSFWorkbook workbook = new XSSFWorkbook();
		
		for (String filename:filenames) {
			System.out.print("Adding "+filename+" to excel...");
			XSSFSheet sheet = workbook.createSheet(filename.substring(0,filename.indexOf(".")));
			String filePath=folder+filename;
			writeTextFileToExcelSheet(sheet, filePath);
			System.out.print("done\n");
		}
		
		try {
            FileOutputStream outputStream = new FileOutputStream(folder+"/"+outputFileName);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

		
	}

	public static String getStringValue(HSSFCell cell) {
		return formatter.formatCellValue(cell).trim();
	
	}


	private String getValue(DataFormatter formatter, int colName, XSSFRow row) {
		String val = formatter.formatCellValue(row.getCell(colName)).trim();
		val = val.replace("\n", "; ");
		return val;
	}


	public static String getStringValue(XSSFCell cell) {
		return formatter.formatCellValue(cell).trim();
	
	}
	
//	public static void main(String[] args) {
//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\QSAR_Model_Building\\data\\DataSets\\LLNA\\2d\\";
//		Vector<String>filenames=new Vector<>();
//		filenames.add("LLNA_training_set-rnd5.csv");
//		filenames.add("LLNA_prediction_set-rnd5.csv");
//		String outputFileName="LLNA_ochem_TEST_descriptors-rnd5.xlsx";
//		
//		createSpreadsheetFromTextFiles(folder, filenames, outputFileName);
//		
//	}

}
