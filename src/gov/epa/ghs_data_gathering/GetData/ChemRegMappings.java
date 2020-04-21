package gov.epa.ghs_data_gathering.GetData;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.epa.ghs_data_gathering.Utilities.ExcelUtilities;

public class ChemRegMappings {

	
	class RecordChemReg {
		String Lookup_Result;
		String Query_Casrn;
		String Query_Name;
		String Top_HIT_DSSTox_Substance_Id;
		String Top_Hit_Casrn;
		String Top_Hit_Name;
		String Validated;

	}
	
	class RecordEchemportalExport {
		String Record_Number;
		String CAS;
		String Name;
		String LLNA_Activity;
		String Interpretation_of_results;
	}
	
	
	Vector<RecordChemReg> parseExcelChemRegExport(String filePathExcel) {
		try {

			Vector<RecordChemReg> records = new Vector<>();
			FileInputStream inputStream = new FileInputStream(new File(filePathExcel));
			DataFormatter formatter = new DataFormatter();

			XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
			XSSFSheet sheet = workbook.getSheetAt(0);
			
			Row headerRow=sheet.getRow(0);

			int colLookup_Result=ExcelUtilities.getColNum(headerRow,"Lookup Result");
			int colQueryCASRN=ExcelUtilities.getColNum(headerRow,"Query Casrn");
			int colQueryName=ExcelUtilities.getColNum(headerRow,"Query Name");
			int colTop_HIT_DSSTox_Substance_Id=ExcelUtilities.getColNum(headerRow,"Top HIT DSSTox_Substance_Id");			
			int colTopHitCasrn=ExcelUtilities.getColNum(headerRow,"Top Hit Casrn");
			int colTopHitName=ExcelUtilities.getColNum(headerRow,"Top Hit Name");
			int colValidated=ExcelUtilities.getColNum(headerRow,"Validated");
			
//			System.out.println(colName);

			int rows=sheet.getLastRowNum();
			
			for (int i=1;i<rows;i++) {
				Row row=sheet.getRow(i);
				RecordChemReg r=new RecordChemReg();
				r.Lookup_Result=ExcelUtilities.getValue(formatter, colLookup_Result, row);
				r.Query_Casrn=ExcelUtilities.getValue(formatter, colQueryCASRN, row);
				r.Query_Name=ExcelUtilities.getValue(formatter, colQueryName, row);
				r.Top_HIT_DSSTox_Substance_Id=ExcelUtilities.getValue(formatter, colTop_HIT_DSSTox_Substance_Id, row);
				r.Top_Hit_Casrn=ExcelUtilities.getValue(formatter, colTopHitCasrn, row);
				r.Top_Hit_Name=ExcelUtilities.getValue(formatter, colTopHitName, row);
				r.Validated=ExcelUtilities.getValue(formatter, colValidated, row);
				
//				System.out.println(r.Lookup_Result+"\t"+r.Query_Casrn+"\t"+r.Query_Name);
				
				records.add(r);
			}
			inputStream.close();
			workbook.close();
			return records;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}
	
	Vector<RecordEchemportalExport> parseExcelEChemPortal(String filePathExcel) {
		try {

			Vector<RecordEchemportalExport> records = new Vector<>();
			FileInputStream inputStream = new FileInputStream(new File(filePathExcel));
			DataFormatter formatter = new DataFormatter();

			XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
			XSSFSheet sheet = workbook.getSheet("Export");
			
			Row headerRow=sheet.getRow(0);
			
			int colRecord_Number=ExcelUtilities.getColNum(headerRow,"Record_Number");
			int colCAS=ExcelUtilities.getColNum(headerRow,"CAS");
			int colName=ExcelUtilities.getColNum(headerRow,"Damaged Name");			
			int colLLNA_Activity=ExcelUtilities.getColNum(headerRow,"LLNA_Activity");			
			int colInterpretation_of_results=ExcelUtilities.getColNum(headerRow,"Interpretation_of_results");
						
			int rows=sheet.getLastRowNum();
			
			for (int i=1;i<rows;i++) {
				Row row=sheet.getRow(i);
				RecordEchemportalExport r=new RecordEchemportalExport();

				r.Record_Number=ExcelUtilities.getValue(formatter, colRecord_Number, row);
				
//				System.out.println(r.Record_Number);
				
				r.CAS=ExcelUtilities.getValue(formatter, colCAS, row);
				r.Name=ExcelUtilities.getValue(formatter, colName, row);
				r.LLNA_Activity=ExcelUtilities.getValue(formatter, colLLNA_Activity, row);
				r.Interpretation_of_results=ExcelUtilities.getValue(formatter, colInterpretation_of_results, row);
				
				records.add(r);
			}
			inputStream.close();
			workbook.close();
			return records;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}
	
	void goEchemportal() {
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\ghs-data-gathering\\AA Dashboard\\Data\\ECHA\\REACH_dossier_data\\eChemPortal\\Skin Sensitization\\";
		String fileNameChemRegExport="Curator Validated All_QCas.xlsx";
		Vector<RecordChemReg>recordsChemReg=parseExcelChemRegExport(folder+fileNameChemRegExport);
		
		String fileNameEchemportalExport="skin sensitization echemportal2.xlsx";
		Vector<RecordEchemportalExport>recordsEchemportalExport=parseExcelEChemPortal(folder+fileNameEchemportalExport);
		
//		RecordEchemportalExport recordEchemportal=recordsEchemportalExport.get(0);

		for (RecordEchemportalExport recordEchemportalExport:recordsEchemportalExport) {
			if (recordEchemportalExport.CAS==null || recordEchemportalExport.CAS.isEmpty()) continue;
			
			Vector<String>vecSid=getChemRegRecordByCAS(recordEchemportalExport.CAS, recordsChemReg);
			String sids=getString(vecSid);
			
			if (vecSid.size()>1)
				System.out.println(recordEchemportalExport.CAS+"\t"+sids);
		}
		
		
//		System.out.println(recordsChemReg.size());
//		System.out.println(recordsEchemportalExport.size());
		
	}
	
	String getString(Vector<String>vec) {
		String result="";
		
		for (int i=0;i<vec.size();i++) {
			result+=vec.get(i);
			if (i<vec.size()-1) result+="|";
		}
		return result;
	}
	
	
	Vector<String> getChemRegRecordByCAS(String CAS,Vector<RecordChemReg>recordsChemReg) {
		
		Vector<String>sidVector=new Vector<>();
		
		for (RecordChemReg recordChemReg:recordsChemReg) {
			
			boolean casMatch=recordChemReg.Query_Casrn.contentEquals(CAS);
			
//			System.out.println(recordChemReg.Query_Name+"\t"+recordEchemportal.Name);
			
			if (casMatch) {
				if (!sidVector.contains(recordChemReg.Top_HIT_DSSTox_Substance_Id)) {
					sidVector.add(recordChemReg.Top_HIT_DSSTox_Substance_Id);
//					System.out.println(recordChemReg.Top_HIT_DSSTox_Substance_Id);
				}
			}
			
			
		}
		return sidVector;
	}
	
	
	public static void main(String[] args) {

		ChemRegMappings c=new ChemRegMappings ();
		c.goEchemportal();

	}

}
