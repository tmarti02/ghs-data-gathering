package gov.epa.exp_data_gathering.parse.CFSAN;

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

public class RecordCFSAN {
	String srNo;
	String chemName;
	String casNr;
	String activity;
	
	public static final String lastUpdated = "03/23/2021";
	public static final String sourceName = ExperimentalConstants.strSourceCFSAN;
	
	public static Vector<RecordCFSAN> parseCFSANRecordsFromExcel() {
		Vector<RecordCFSAN> records = new Vector<RecordCFSAN>();
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
					Row headerRow = sheet.getRow(0);
					int srNoIndex = -1;
					int chemNameIndex = -1;
					int casNrIndex = -1;
					int activityIndex = -1;
					for (Cell cell:headerRow) {
						cell.setCellType(Cell.CELL_TYPE_STRING);
						String header = cell.getStringCellValue().toLowerCase();
						int col = cell.getColumnIndex();
						
						if (header.trim().equals("sr. no.")) { srNoIndex = col;	
						} else if (header.trim().equals("chemname")) { chemNameIndex = col;
						} else if (header.trim().equals("cas_nr")) { casNrIndex = col;
						} else if (header.trim().equals("activity")) { activityIndex = col;
						}
					}
					int rows = sheet.getLastRowNum();
					for (int i = 1; i < rows; i++) {
						Row row = sheet.getRow(i);
						for (Cell cell:row) { cell.setCellType(Cell.CELL_TYPE_STRING); }
						RecordCFSAN cr = new RecordCFSAN();
						cr.srNo = row.getCell(srNoIndex,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						cr.chemName = StringEscapeUtils.escapeHtml4(row.getCell(chemNameIndex,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
						cr.casNr = row.getCell(casNrIndex,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						cr.activity = row.getCell(activityIndex,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						records.add(cr);
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
