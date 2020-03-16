package gov.epa.ghs_data_gathering.Utilities;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;

public class ExcelUtilities {
	
	static DataFormatter formatter = new DataFormatter();

	public static String getValue(DataFormatter formatter, int colName, Row row) {
		String val=formatter.formatCellValue(row.getCell(colName)).strip();
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
			if (val.contentEquals(name)) {
				return i;
			}
	    }
		return -1;
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

}
