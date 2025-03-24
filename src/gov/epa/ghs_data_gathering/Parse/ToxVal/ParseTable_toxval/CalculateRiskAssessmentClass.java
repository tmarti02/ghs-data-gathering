package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CalculateRiskAssessmentClass {

	public static Vector<DictionaryRow>dictionaryRows=new Vector<>();
	
	
	public static class DictionaryRow {
		String term;
		String field;
		String risk_assessment_class;
	}
	
	
	public static void assignRAC(RecordToxVal r) {
		String rac="";
		
		
		if (dictionaryRows.size()==0) {
			loadDictionary();
		}
		
		for (DictionaryRow dr:dictionaryRows) {
			try {			
				Field field = r.getClass().getDeclaredField(dr.field);//get field in recordtoxval by reflection				
				String fieldValue=(String)field.get(r);
				
				if(fieldValue.contentEquals(dr.term)) {
//					System.out.println(dr.field+"\t"+fieldValue);					
					r.risk_assessment_class_calc=dr.risk_assessment_class;					
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
	}
	
	
	public static void loadDictionary() 
	{
		try
		{
			
					
			FileInputStream file = new FileInputStream(new File("AA Dashboard/RAC_conversion_5.xlsx"));

			//Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);

			//Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);

			//Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();
			
			rowIterator.next();
			
			while (rowIterator.hasNext()) 
			{
				Row row = rowIterator.next();
				//For each row, iterate through all the columns
				
				DictionaryRow dr=new DictionaryRow();
				
				dr.term=row.getCell(0).getStringCellValue().trim();
				dr.field=row.getCell(2).getStringCellValue().trim();
				dr.risk_assessment_class=row.getCell(3).getStringCellValue().trim();
				
				dictionaryRows.add(dr);

//				System.out.println(dr.term+"\t"+dr.field+"\t"+dr.risk_assessment_class);
			}
			
//			System.out.println(rows.get(0).term);
			
			file.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CalculateRiskAssessmentClass.loadDictionary();

	}

}
