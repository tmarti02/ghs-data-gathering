package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.epa.api.ExperimentalConstants;

public class RecordQSARDB {
	String name;
	String casrn;
	String logS;
	String mp;
	String mLogP;
	String vp;
	String units;
	String reference;
	
	static final String sourceName = ExperimentalConstants.strSourceQSARDB;

	public static Vector<RecordQSARDB> parseQSARDBRecordsFromExcel() {
		Vector<RecordQSARDB> records = new Vector<RecordQSARDB>();
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
					Row headerRow = sheet.getRow(1);
					int nameIndex = -1;
					int casrnIndex = -1;
					int logSIndex = -1;
					int mpIndex = -1;
					int mLogPIndex = -1;
					int vpIndex = -1;
					String getUnits = "";
					String getReference = sheet.getRow(0).getCell(0).getStringCellValue();
					for (Cell cell:headerRow) {
						String header = cell.getStringCellValue().toLowerCase();
						int col = cell.getColumnIndex();
						
						if (header.contains("name")) { nameIndex = col;
						} else if (header.contains("casrn")) { casrnIndex = col;
						} else if (header.contains("logs")) { logSIndex = col;
						} else if (header.contains("mp")) { mpIndex = col;
						} else if (header.contains("mlogp")) { mLogPIndex = col;
						} else if (header.contains("logvp")) { vpIndex = col;
						}
						
						if (header.contains("log_mgl")) { getUnits = "Log_mgL";
						} else if (header.contains("log_mmhg")) { getUnits = "Log_mmHg";
						} else if (header.contains("log_m")) { getUnits = "Log_M";
						}
					}
					int rows = sheet.getLastRowNum();
					for (int i = 2; i < rows; i++) {
						Row row = sheet.getRow(i);
						for (Cell cell:row) { cell.setCellType(Cell.CELL_TYPE_STRING); }
						RecordQSARDB qr = new RecordQSARDB();
						qr.reference = getReference;
						qr.name = row.getCell(nameIndex).getStringCellValue();
						qr.casrn = row.getCell(casrnIndex).getStringCellValue();
						if (logSIndex >= 0) { qr.logS = row.getCell(logSIndex).getStringCellValue(); }
						if (mpIndex >= 0) { qr.mp = row.getCell(mpIndex).getStringCellValue(); }
						if (mLogPIndex >= 0) { qr.mLogP = row.getCell(mLogPIndex).getStringCellValue(); }
						if (vpIndex >= 0) { qr.vp = row.getCell(vpIndex).getStringCellValue(); }
						qr.units = getUnits;
						records.add(qr);
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
