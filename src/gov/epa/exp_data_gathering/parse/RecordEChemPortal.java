package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;

public class RecordEChemPortal {
	String substanceName;
	String nameType;
	String number;
	String numberType;
	boolean memberOfCategory;
	String participant;
	String section;
	String values;
	
	static final String sourceName = ExperimentalConstants.strSourceEChem;
	
	public static Vector<RecordEChemPortal> parseEChemPortalQueryFromExcel() {
		Vector<RecordEChemPortal> records = new Vector<RecordEChemPortal>();
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
					int rows=sheet.getLastRowNum();
					for (int i = 1; i < rows; i++) {
						RecordEChemPortal ecpr = new RecordEChemPortal();
						Row row = sheet.getRow(i);
						ecpr.substanceName = row.getCell(0).getStringCellValue();
						ecpr.nameType = row.getCell(1).getStringCellValue();
						ecpr.number = row.getCell(2).getStringCellValue();
						ecpr.numberType = row.getCell(3).getStringCellValue();
						ecpr.memberOfCategory = row.getCell(4).getBooleanCellValue();
						ecpr.participant = row.getCell(5).getStringCellValue();
						ecpr.section = row.getCell(6).getStringCellValue();
						ecpr.values = row.getCell(7).getStringCellValue();
						records.add(ecpr);
					}
					wb.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return records;
	}
	
	public static void main(String[] args) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Vector<RecordEChemPortal> records = parseEChemPortalQueryFromExcel();
		for (RecordEChemPortal record:records) {
			System.out.println(gson.toJson(record));
		}
	}
}
