package gov.epa.QSAR.DataSetCreation;


import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Vector;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.epa.exp_data_gathering.parse.ParseUtilities;

public class RecordsQSAR extends Vector<RecordQSAR> {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8480087185267457724L;
	/**
	 * 
	 */	


	public void toExcelFile(String filePath) {
		toExcelFile(filePath, RecordQSAR.outputFieldNames);
	}
	
	
	private void writeSheet(Workbook wb,String sheetName,String [] fieldNames,boolean usable) {
		Class clazz = RecordQSAR.class;
		String[] headers = fieldNames;
		
		Sheet recSheet = wb.createSheet(sheetName);		
		Row recSubtotalRow = recSheet.createRow(0);
		Row recHeaderRow = recSheet.createRow(1);
		
		CellStyle style = wb.createCellStyle();
		Font font = wb.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setFont(font);

		for (int i = 0; i < headers.length; i++) {
			Cell recCell = recHeaderRow.createCell(i);
			recCell.setCellValue(headers[i]);
			recCell.setCellStyle(style);
		}
		int recCurrentRow = 2;
		
		for (RecordQSAR qr:this) {
			try {
				Row row = null;
				
				if (qr.usable==usable) {
					row = recSheet.createRow(recCurrentRow);
					recCurrentRow++;
				} else continue;
				
				for (int i = 0; i < headers.length; i++) {
					Field field = clazz.getDeclaredField(headers[i]);
					Object value = field.get(qr);
					if (value!=null && !(value instanceof Double)) { 
						String strValue = ParseUtilities.reverseFixChars(StringEscapeUtils.unescapeHtml4(value.toString()));
						row.createCell(i).setCellValue(strValue);
					} else if (value!=null) { row.createCell(i).setCellValue((double) value); }
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		
		String lastCol = CellReference.convertNumToColString(headers.length);
		recSheet.setAutoFilter(CellRangeAddress.valueOf("A2:"+lastCol+recCurrentRow));
		recSheet.createFreezePane(0, 2);
		
		for (int i = 0; i < headers.length; i++) {
			String col = CellReference.convertNumToColString(i);
			String recSubtotal = "SUBTOTAL(3,"+col+"$3:"+col+"$"+(recCurrentRow+1)+")";
			recSubtotalRow.createCell(i).setCellFormula(recSubtotal);
		}
	}
	
	public void toExcelFile(String filePath,String[] fieldNames) {
		
		Workbook wb = new XSSFWorkbook();
		writeSheet(wb, "Records", fieldNames, true);
		writeSheet(wb, "Records-Bad", fieldNames, false);
				
		try {
			OutputStream fos = new FileOutputStream(filePath);
			wb.write(fos);
			wb.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
}
