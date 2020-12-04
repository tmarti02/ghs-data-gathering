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

public class RecordAqSolDB {
	String name;
	String smiles;
	String solubility;
	
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
					FileInputStream fis = new FileInputStream(new File(excelFilePath+File.separator+filename));
					Workbook wb = new XSSFWorkbook(fis);
					Sheet sheet = wb.getSheetAt(0);
					Row headerRow = sheet.getRow(0);
					int nameIndex = -1;
					int smilesIndex = -1;
					int solIndex = -1;
					for (Cell cell:headerRow) {
						cell.setCellType(Cell.CELL_TYPE_STRING);
						String header = cell.getStringCellValue().toLowerCase();
						int col = cell.getColumnIndex();
						
						if (header.equals("name")) { nameIndex = col;
						} else if (header.equals("smiles")) { smilesIndex = col;
						} else if (header.equals("solubility")) { solIndex = col;
						}
					}
					int rows = sheet.getLastRowNum();
					for (int i = 1; i < rows; i++) {
						Row row = sheet.getRow(i);
						for (Cell cell:row) { cell.setCellType(Cell.CELL_TYPE_STRING); }
						RecordAqSolDB ar = new RecordAqSolDB();
						ar.name = row.getCell(nameIndex,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
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
