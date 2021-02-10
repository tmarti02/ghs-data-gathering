package gov.epa.exp_data_gathering.parse.AqSolDB;

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

/**
 * Stores data from AqSolDB, accessible at: https://dataverse.harvard.edu/dataset.xhtml?persistentId=doi:10.7910/DVN/OVHAW8
 * @author GSINCL01
 *
 */
public class RecordAqSolDB {
	String id;
	String name;
	String smiles;
	String solubility;
	
	public static final String lastUpdated = "12/04/2020";
	public static final String sourceName = ExperimentalConstants.strSourceAqSolDB;
	
	public static Vector<RecordAqSolDB> parseAqSolDBRecordsFromExcel() {
		Vector<RecordAqSolDB> records = new Vector<RecordAqSolDB>();
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
					Row headerRow = sheet.getRow(0);
					int idIndex = -1;
					int nameIndex = -1;
					int smilesIndex = -1;
					int solIndex = -1;
					for (Cell cell:headerRow) {
						cell.setCellType(Cell.CELL_TYPE_STRING);
						String header = cell.getStringCellValue().toLowerCase();
						int col = cell.getColumnIndex();
						
						if (header.equals("id")) { idIndex = col;	
						} else if (header.equals("name")) { nameIndex = col;
						} else if (header.equals("smiles")) { smilesIndex = col;
						} else if (header.equals("solubility")) { solIndex = col;
						}
					}
					int rows = sheet.getLastRowNum();
					for (int i = 1; i < rows; i++) {
						Row row = sheet.getRow(i);
						for (Cell cell:row) { cell.setCellType(Cell.CELL_TYPE_STRING); }
						RecordAqSolDB ar = new RecordAqSolDB();
						ar.id = row.getCell(idIndex,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						ar.name = StringEscapeUtils.escapeHtml4(row.getCell(nameIndex,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
						ar.smiles = row.getCell(smilesIndex,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						ar.solubility = row.getCell(solIndex,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						records.add(ar);
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
