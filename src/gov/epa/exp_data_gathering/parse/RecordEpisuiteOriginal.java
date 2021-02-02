package gov.epa.exp_data_gathering.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
	String url;
	String LogP; // 
	String ExpLogWsol; // log molar experimental water solubility that appears in the LogP data source (probably redundant)
	String EstWSol; // pcChem estimated water solubility, part of LogP data source (useless)
	String EstMP; // similar to above
	String EstWsolEq19; // not important
	String EstWsolEq9; // not important
	String Property;
	
	static final String sourceName="EpisuiteOriginal";
	
	
	private static Vector<RecordEpisuiteOriginal> gettxtRecords(String filepath){
		Vector<RecordEpisuiteOriginal> records = new Vector<>();
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filepath));
			String line = reader.readLine();
			
			while (line != null) {
				if (line.startsWith("CAS") || line.startsWith("---")) { // skips headers in txt file
					line = reader.readLine();
				}
				else { // pick up logP data
				
				RecordEpisuiteOriginal r = new RecordEpisuiteOriginal();
				r.CAS = line.substring(0,11); // takes 11 characters, enough for entire CAS.
				
				String NameAndRest = line.substring(13);

				Matcher m = Pattern.compile("(.+?)(?=([ ]?[-]?[0-9]\\.[0-9]+))").matcher(NameAndRest);
				if (m.find()) {
					// System.out.println("group 1 = " + m.group(1) + " group 2 = " + m.group(2));
					r.Name = m.group(1);
					r.LogP = m.group(2);
				}
				
				r.url = "http://esc.syrres.com/interkow/Download/WSKOWWIN_Datasets.zip";
				r.Property = ExperimentalConstants.strLogKow;
				
				// TODO get logwsol #'s for the logP records
				// System.out.println(r.CAS);
				
				records.add(r);
				
				line = reader.readLine();
				
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return records;
	}
	

	
	private static Vector<RecordEpisuiteOriginal> getRecords(String filepath, int sheetnum){
		Vector<RecordEpisuiteOriginal> records = new Vector<>();
			
		try {
			FileInputStream fis = new FileInputStream(new File(filepath));
			
			Workbook wb = null;

			if (filepath.contains(".xlsx")) wb=new XSSFWorkbook(fis);
			else if (filepath.contains(".xls")) wb=new HSSFWorkbook(fis);
			
			// handles inclusion of the LogP data
			else if (filepath.contains(".txt")) {
				records = gettxtRecords(filepath);
				return records;
			}
			
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
				
				// url for the wsol data
				r.url = "http://esc.syrres.com/interkow/Download/WaterFragmentDataFiles.zip";
				r.Property = ExperimentalConstants.strWaterSolubility;
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
		String txtPath = ExcelFolder+File.separator + "EpisuiteKOWExperimental.txt";
		Vector<RecordEpisuiteOriginal> recordsTraining = getRecords(ExcelPath, 0);
		Vector<RecordEpisuiteOriginal> recordsValidation = getRecords(ExcelPath, 1);
		Vector<RecordEpisuiteOriginal> recordsLogP = getRecords(txtPath, 0);
		
		records.addAll(recordsTraining);
		records.addAll(recordsValidation);
		records.addAll(recordsLogP);
		
		System.out.println(records.get(4).CAS);
		return(records);
	}

	
	public static void main (String[] args) {
		// Vector<RecordEpisuiteOriginal> records = recordWaterFragmentData();
		gettxtRecords("C:\\Users\\CRAMSLAN\\OneDrive - Environmental Protection Agency (EPA)\\Java\\ghs_data_gathering\\Data\\Experimental\\EpisuiteOriginal\\EpisuiteKOWExperimental.txt");
	}
}
