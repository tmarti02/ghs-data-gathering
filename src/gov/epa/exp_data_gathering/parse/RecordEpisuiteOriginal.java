package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.epa.api.ExperimentalConstants;

public class RecordEpisuiteOriginal {
	String CAS;
	String Name;
	String Sheet;
	String MolWT;
	String WsolmgL;
	String LogWsol;
	String LogEstimated;
	String Error;
	String Temp;
	String Reference;
	
	static final String sourceName="EpisuiteOriginal";

	
	private static Vector<RecordEpisuiteOriginal> getRecords(String filepath, int sheetnum){
		Vector<RecordEpisuiteOriginal> records = new Vector<>();
		
		try {
			FileInputStream fis = new FileInputStream(new File(filepath));
			
			Workbook wb = null;

			if (filepath.contains(".xlsx")) wb=new XSSFWorkbook(fis);
			else if (filepath.contains(".xls")) wb=new HSSFWorkbook(fis);
			
			// int TrainingDataSheetNo = 0;
			// int ValidationDataSheetNo = 1;
			
			Sheet sheet = wb.getSheetAt(sheetnum);

			Vector<String>colNames=new Vector<>();
			Row row = sheet.getRow(0);
			
			for (int i=0;i<row.getLastCellNum();i++) {
				String colName=row.getCell(i).getStringCellValue();
				colNames.add(colName);
				System.out.println(i+"\t"+colName);
			}
			
			int colNumCAS = -1;
			int colNumName = -1;
			int colNumWSmgL = -1;
			int colNumMW = -1;
			int colNumLogWSM = -1;
			int colNumEstM = -1;
			int colNumError = -1;
			int colNumTemp = -1;
			int colNumReference = -1;
			
			for (int i=0;i<colNames.size();i++) {
				if (colNames.get(i).contains("CAS")) colNumCAS=i;
				if (colNames.get(i).contains("Name")) colNumName=i;
				if ((colNames.get(i).contains("Mol Wt") || colNames.get(i).contains("MW"))) colNumMW=i;
				if (colNames.get(i).contains("Wsol (mg/L)")) colNumWSmgL=i;
				if ((colNames.get(i).contains("Log Wsol (moles/L)") || colNames.get(i).contains("Log WS moles/L"))) colNumLogWSM=i;
				if (colNames.get(i).contains("Log Estimated moles/L")) colNumEstM=i;
				if (colNames.get(i).contains("Error")) colNumError=i;
				if (colNames.get(i).contains("Temp")) colNumTemp=i;
				if (colNames.get(i).contains("Reference")) colNumReference=i;
			}
			

			for (int i=1;i<sheet.getLastRowNum();i++) {
				
				Row rowi = sheet.getRow(i);
				
				RecordEpisuiteOriginal r=new RecordEpisuiteOriginal();
				
				r.CAS = rowi.getCell(colNumCAS).getStringCellValue();
				r.Name = rowi.getCell(colNumName).getStringCellValue();
				r.MolWT = rowi.getCell(colNumMW).getStringCellValue();
				r.WsolmgL = rowi.getCell(colNumWSmgL).getStringCellValue();
				r.LogWsol = rowi.getCell(colNumLogWSM).getStringCellValue();
				r.LogEstimated = rowi.getCell(colNumEstM).getStringCellValue();
				r.Error = rowi.getCell(colNumError).getStringCellValue();
				if (colNumTemp > -1)
					r.Temp = rowi.getCell(colNumTemp).getStringCellValue();
				else 
					r.Temp = null;
				r.Reference = rowi.getCell(colNumReference).getStringCellValue();
				
				records.add(r);

			}

			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String ExcelFolder = "Data"+File.separator+"Experimental"+ File.separator + sourceName;
		String ExcelPath = ExcelFolder+File.separator + "WaterFragmentDataFiles.xls";
		Vector<RecordEpisuiteOriginal> recordsTraining = getRecords(ExcelPath, 0);
		Vector<RecordEpisuiteOriginal> recordsValidation = getRecords(ExcelPath, 1);


	}

}
