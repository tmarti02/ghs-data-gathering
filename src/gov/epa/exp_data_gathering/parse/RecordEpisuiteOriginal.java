package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.epa.api.ExperimentalConstants;


// References 15 and 17 of AqSolDB paper
// http://esc.syrres.com/interkow/Download/WaterFragmentDataFiles.zip
// http://esc.syrres.com/interkow/Download/WSKOWWIN_Datasets.zip


// Citation for AqSolDB paper
// Sorkun, M.C., Khetan, A. & Er, S. AqSolDB, a curated reference set of aqueous solubility and 2D descriptors for a diverse set of compounds. Sci Data 6, 143 (2019). https://doi.org/10.1038/s41597-019-0151-1


/**
 * @author cramslan
 * Obtains data from episuite excel sheet.
 */
public class RecordEpisuiteOriginal {
	String CAS;
	String Name;
	String Sheet;
	Double MolWT;
	Double WsolmgL;
	Double LogWsol;
	Double LogEstimated;
	Double Error;
	Double Temp;
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
				// System.out.println(i+"\t"+colName);
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
				if (colNames.get(i).contains("Wsol mg/L") || colNames.get(i).contains("Wsol (mg/L)")) colNumWSmgL=i;
				if ((colNames.get(i).contains("Log Wsol (moles/L)") || colNames.get(i).contains("Log WS moles/L"))) colNumLogWSM=i;
				if (colNames.get(i).contains("Log Estimated moles/L") || colNames.get(i).contains("Estimated")) colNumEstM=i;
				if (colNames.get(i).contains("Error")) colNumError=i;
				if (colNames.get(i).contains("Temp")) colNumTemp=i;
				if (colNames.get(i).contains("Reference")) colNumReference=i;
			}
			
			
			// System.out.println("colNumTemp =" + colNumTemp);
			
			for (int i=1;i<sheet.getLastRowNum();i++) {
				
				Row rowi = sheet.getRow(i);
				
				RecordEpisuiteOriginal r=new RecordEpisuiteOriginal();
				
				r.CAS = rowi.getCell(colNumCAS).getStringCellValue();
				r.Name = rowi.getCell(colNumName).getStringCellValue();
				r.MolWT = rowi.getCell(colNumMW).getNumericCellValue();
				r.WsolmgL = rowi.getCell(colNumWSmgL).getNumericCellValue();
				r.LogWsol = rowi.getCell(colNumLogWSM).getNumericCellValue();
				r.LogEstimated = rowi.getCell(colNumEstM).getNumericCellValue();
				r.Error = rowi.getCell(colNumError).getNumericCellValue();
				// sometimes the temperature cells are empty
				if (colNumTemp > 0) {
					if (rowi.getCell(colNumTemp) != null) {
					r.Temp = rowi.getCell(colNumTemp).getNumericCellValue();
					}
				}
				r.Reference = rowi.getCell(colNumReference).getStringCellValue();
				
				// specify which sheet in the recordEpisuite object
				if (sheetnum == 1)
					r.Sheet="Validation Data";
				else if (sheetnum == 0)
					r.Sheet="Training Data";
					
				
				
				records.add(r);

			}

			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}

	public static Vector<RecordEpisuiteOriginal> recordWaterFragmentData() {
		Vector<RecordEpisuiteOriginal> records = new Vector<>();

		
		String ExcelFolder = "Data"+File.separator+"Experimental"+ File.separator + sourceName;
		String ExcelPath = ExcelFolder+File.separator + "WaterFragmentDataFiles.xls";
		Vector<RecordEpisuiteOriginal> recordsTraining = getRecords(ExcelPath, 0);
		Vector<RecordEpisuiteOriginal> recordsValidation = getRecords(ExcelPath, 1);
		
		records.addAll(recordsTraining);
		records.addAll(recordsValidation);
		
		
		System.out.println(records.get(4).CAS);
		return(records);
	}

	
	public static void main (String[] args) {
		// Vector<RecordEpisuiteOriginal> records = recordWaterFragmentData();
//		Double x = null;
//		Double y = (double) x;
//		System.out.println(x);
	}
}
