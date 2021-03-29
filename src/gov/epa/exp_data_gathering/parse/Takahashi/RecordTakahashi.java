package gov.epa.exp_data_gathering.parse.Takahashi;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.DownloadWebpageUtilities;

public class RecordTakahashi {
	String testMaterial;
	String casNo;
	String draizeScore100;
	String draizeScore10;
	String draizeRank;
	String chemicalClass;
	String supplier;
	String solventUsed;
	
	public static final String lastUpdated = "03/26/2021";
	public static final String sourceName = ExperimentalConstants.strSourceTakahashi;
	
	public static Vector<RecordTakahashi> parseTakahashiRecordsFromExcel() {
		Vector<RecordTakahashi> records = new Vector<RecordTakahashi>();
		String folderNameExcel = "excel files";
		String mainFolder = "data"+File.separator+"experimental"+ File.separator + sourceName;
		String excelFilePath = mainFolder + File.separator+folderNameExcel;
		File folder = new File(excelFilePath);
		String[] filenames = folder.list();
		for (String filename:filenames) {
			if (filename.endsWith(".xlsx")) {
				try {
					String filepath = excelFilePath+File.separator+filename;
					String date = DownloadWebpageUtilities.getStringCreationDate(filepath);
					if (!date.equals(lastUpdated)) {
						System.out.println(sourceName+" warning: Last updated date does not match creation date of file "+filename);
					}
					FileInputStream fis = new FileInputStream(new File(filepath));
					Workbook wb = new XSSFWorkbook(fis);
					Sheet sheet = wb.getSheetAt(0);
					int rows = sheet.getLastRowNum();
					for (int i = 1; i < rows; i++) {
						Row row = sheet.getRow(i);
						for (Cell cell:row) { cell.setCellType(Cell.CELL_TYPE_STRING); }
						RecordTakahashi tr = new RecordTakahashi();
						tr.testMaterial = StringEscapeUtils.escapeHtml4(row.getCell(0,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
						tr.casNo = row.getCell(1,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						tr.draizeScore100 = row.getCell(2,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						tr.draizeScore10= row.getCell(3,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						tr.draizeRank= row.getCell(4,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						tr.chemicalClass= row.getCell(5,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						tr.supplier= row.getCell(6,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						tr.solventUsed= row.getCell(7,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						records.add(tr);
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
