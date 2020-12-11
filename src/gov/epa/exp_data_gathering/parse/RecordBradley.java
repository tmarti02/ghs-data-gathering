package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.epa.api.ExperimentalConstants;

public class RecordBradley {
	String solute;
	String soluteSMILES;
	String concentration;
	String notes;
	String citation;
	String refURL;
	
	public static final String sourceName = ExperimentalConstants.strSourceBradley;
	
	public static Vector<RecordBradley> parseBradleyRecordsFromExcel() {
		Vector<RecordBradley> records = new Vector<RecordBradley>();
		String folderNameExcel = "excel files";
		String mainFolder = "Data"+File.separator+"Experimental"+ File.separator + sourceName;
		String excelFilePath = mainFolder + File.separator+folderNameExcel;
		File folder = new File(excelFilePath);
		String[] filenames = folder.list();
		for (String filename:filenames) {
			if (filename.endsWith(".xlsx")) {
				try {
					FileInputStream fis = new FileInputStream(new File(excelFilePath+File.separator+filename));
					Workbook wb = new XSSFWorkbook(fis);
					Sheet sheet = wb.getSheetAt(0);
					Row headerRow = sheet.getRow(0);
					int soluteIndex = -1;
					int citationIndex = -1;
					int refIndex = -1;
					int soluteSMILESIndex = -1;
					int concIndex = -1;
					int notesIndex = -1;
					for (Cell cell:headerRow) {
						cell.setCellType(Cell.CELL_TYPE_STRING);
						String header = cell.getStringCellValue().toLowerCase();
						int col = cell.getColumnIndex();
						
						if (header.equals("solute")) {
							soluteIndex = col;
							soluteSMILESIndex = col+2;
						} else if (header.equals("sample or citation")) { citationIndex = col;
						} else if (header.equals("ref")) { refIndex = col;
						} else if (header.equals("concentration (m)")) { concIndex = col;
						} else if (header.contains("notes")) { notesIndex = col;
						}
					}
					int rows = sheet.getLastRowNum();
					for (int i = 1; i < rows; i++) {
						Row row = sheet.getRow(i);
						for (Cell cell:row) { cell.setCellType(Cell.CELL_TYPE_STRING); }
						RecordBradley br = new RecordBradley();
						br.solute = row.getCell(soluteIndex,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						br.citation = row.getCell(citationIndex,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						br.refURL = row.getCell(refIndex,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						br.soluteSMILES = row.getCell(soluteSMILESIndex,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						br.concentration = row.getCell(concIndex,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						br.notes = row.getCell(notesIndex,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						if (br.solute!=null && !br.solute.isBlank()) { records.add(br); }
					}
					wb.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return records;
	}
}
