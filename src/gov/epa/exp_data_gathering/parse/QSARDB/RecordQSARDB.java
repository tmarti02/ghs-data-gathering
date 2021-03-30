package gov.epa.exp_data_gathering.parse.QSARDB;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.DownloadWebpageUtilities;

/**
 * Stores data from qsardb.org
 * @author GSINCL01
 *
 */
public class RecordQSARDB {
	String name;
	String casrn;
	String logS;
	String mp;
	String mLogP;
	String vp;
	String units;
	String reference;
	String url;
	
	static final String lastUpdated = "12/04/2020";
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
					String filepath = excelFilePath+File.separator+filename;
					String date = DownloadWebpageUtilities.getStringCreationDate(filepath);
					if (!date.equals(lastUpdated)) {
						System.out.println(sourceName+" warning: Last updated date does not match creation date of file "+filename);
					}
					FileInputStream fis = new FileInputStream(new File(filepath));
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
					String getURL = sheet.getRow(0).getCell(0).getHyperlink().getAddress();
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
						qr.url = getURL;
						String name = row.getCell(nameIndex).getStringCellValue().replaceAll("′", "'");
						if (name.trim().endsWith("i")) { name = name.trim().substring(0, name.length() - 1); }
						qr.name = name;
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
