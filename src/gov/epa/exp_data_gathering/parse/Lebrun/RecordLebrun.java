package gov.epa.exp_data_gathering.parse.Lebrun;

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
import gov.epa.exp_data_gathering.parse.CFSAN.RecordCFSAN;

public class RecordLebrun {
	String chemicalName;
	String casrn;
	String inVivoGHS;
	String inVivoEPA;
	String functionalGroups;
	String bcopLLBO;
	String bcopOpKit;
	String epi;
	String ice;
	String oi;
	String os;
	String ste;
	
	public static final String lastUpdated = "03/25/2021";
	public static final String sourceName = ExperimentalConstants.strSourceLebrun;
	
	public static Vector<RecordLebrun> parseLebrunRecordsFromExcel() {
		Vector<RecordLebrun> records = new Vector<RecordLebrun>();
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
						RecordLebrun lr = new RecordLebrun();
						lr.chemicalName = StringEscapeUtils.escapeHtml4(row.getCell(1,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
						lr.casrn = row.getCell(2,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						lr.inVivoGHS = row.getCell(3,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						lr.inVivoEPA = row.getCell(4,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						lr.functionalGroups = row.getCell(5,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						lr.bcopLLBO = row.getCell(6,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						lr.bcopOpKit = row.getCell(7,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						lr.epi = row.getCell(8,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						lr.ice = row.getCell(9,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						lr.oi = row.getCell(10,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						lr.os = row.getCell(11,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						lr.ste = row.getCell(12,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						records.add(lr);
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
