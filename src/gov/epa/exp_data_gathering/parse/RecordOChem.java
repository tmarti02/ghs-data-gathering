package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;

public class RecordOChem {
	String smiles;
	String casrn;
	String name;
	String propertyName;
	String propertyValue;
	String propertyUnit;
	String temperature;
	String temperatureUnit;
	String pH;
	String measurementMethod;
	
	static final String sourceName = ExperimentalConstants.strSourceOChem;
	
	public static Vector<RecordOChem> parseOChemQueriesFromExcel() {
		Vector<RecordOChem> records = new Vector<RecordOChem>();
		String folderNameExcel = "excel files";
		String mainFolder = "Data"+File.separator+"Experimental"+ File.separator + sourceName;
		String excelFilePath = mainFolder + File.separator+folderNameExcel;
		File folder = new File(excelFilePath);
		String[] filenames = folder.list();
		for (String filename:filenames) {
			if (filename.endsWith(".xls")) {
				try {
					FileInputStream fis = new FileInputStream(new File(excelFilePath+File.separator+filename));
					Workbook wb = new HSSFWorkbook(fis);
					Sheet sheet = wb.getSheetAt(0);
					Row headerRow = sheet.getRow(0);
					int smilesIndex = -1;
					int casrnIndex = -1;
					int nameIndex = -1;
					int propertyValueIndex = -1;
					int propertyUnitIndex = -1;
					int temperatureIndex = -1;
					int temperatureUnitIndex = -1;
					int pHIndex = -1;
					int measurementMethodIndex = -1;
					String propertyName = "";
					for (Cell cell:headerRow) {
						String header = cell.getStringCellValue();
						int col = cell.getColumnIndex();
						if (header.contains("SMILES")) { smilesIndex = col;
						} else if (header.contains("CASRN")) { casrnIndex = col;
						} else if (header.contains("NAME")) { nameIndex = col; // two name fields?
						} else if (header.contains("{measured, converted}")) {
							propertyName = header.substring(0,header.indexOf("{")).trim();
							propertyValueIndex = col;
							propertyUnitIndex = col+1;
						} else if (header.contains("TEMPERATURE")) {
							temperatureIndex = col;
							temperatureUnitIndex = col+1;
						} else if (header.contains("measurement method")) { measurementMethodIndex = col;
						} else if (header.contains("pH")) { pHIndex = col;
						}
					}
					int rows = sheet.getLastRowNum();
					for (int i = 1; i < rows; i++) {
						Row row = sheet.getRow(i);
						for (Cell cell:row) { cell.setCellType(Cell.CELL_TYPE_STRING); }
						RecordOChem ocr = new RecordOChem();
						ocr.smiles = betterGetCellValue(row,smilesIndex);
						ocr.casrn = betterGetCellValue(row,casrnIndex);
						ocr.name = betterGetCellValue(row,nameIndex);
						ocr.propertyName = propertyName;
						ocr.propertyValue = betterGetCellValue(row,propertyValueIndex);
						ocr.propertyUnit = betterGetCellValue(row,propertyUnitIndex);
						ocr.temperature = betterGetCellValue(row,temperatureIndex);
						ocr.temperatureUnit = betterGetCellValue(row,temperatureUnitIndex);
						ocr.measurementMethod = betterGetCellValue(row,measurementMethodIndex);
						ocr.pH = betterGetCellValue(row,pHIndex);
						records.add(ocr);
					}
					wb.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return records;
	}
	
	private static String betterGetCellValue(Row row,int index) {
		if (index > -1) {
			return row.getCell(index,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
		} else {
			return null;
		}
	}
	
	public static void main(String[] args) {
		Vector<RecordOChem> records = parseOChemQueriesFromExcel();
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		for (RecordOChem r:records) {
			System.out.println(gson.toJson(r));
		}
	}
}
