package gov.epa.ghs_data_gathering.GetData.SkinSensitization;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.AtomContainer;

public class RecordOECD_Toolbox {

	public String chemical_number;
	public String chemical_name;
	
	public String CAS;
	public String smiles;

	public String species;
	public String endpoint;
	public String test_type;
	public String type_of_method;
	
	public String scifinderFormula;
	public String scifinderClassIdentifier;
	public String scifinderWarning="";
	
	public String reference;

	public String EC3;
	public String EC3_Units;
	public String LLNA_Result;
	

	
	static String[] varlist = { "chemical_number", "chemical_name", "CAS", "smiles","scifinderFormula","scifinderWarning","scifinderClassIdentifier", 
			"species", "endpoint", "test_type","type_of_method", "reference","EC3","EC3_Units","LLNA_Result"};

	
	public static void writeToExcel(String filepath,Vector<RecordOECD_Toolbox>records) {

		XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("sheet0");

        int rowNum = 0;
        System.out.println("Creating excel");

        XSSFRow rowHeader = sheet.createRow(rowNum);
        
        int col=0;
        for (int i=0;i<varlist.length;i++) {
        	XSSFCell cell = rowHeader.createCell(col++);
			cell.setCellValue(varlist[i]);
        }
        rowNum++;
        
        for (RecordOECD_Toolbox r:records) {
        
        	XSSFRow row = sheet.createRow(rowNum++);
        	
        	int colNum = 0;
        	
    		for (int i=0;i<varlist.length;i++) {
    			try {
    				Field myField =r.getClass().getField(varlist[i]);				
    				String val=(String)myField.get(r);
    				
    				XSSFCell cell = row.createCell(colNum++);
    				cell.setCellValue(val);
    			} catch (Exception ex){
    				ex.printStackTrace();
    			}
    		}
        }
        
        try {
            FileOutputStream outputStream = new FileOutputStream(filepath);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	
	public static String getHeader(String [] varlist) {
		String str="";
		for (int i=0;i<varlist.length;i++) {
			str+=varlist[i];
			if (i<varlist.length-1) str+="\t";
		}
		return str;
	}
	
	public static String getHeader() {
		return getHeader(varlist);
	}
	public String toString(String[] varlist) {
		String str="";
		for (int i=0;i<varlist.length;i++) {
			try {
				Field myField =this.getClass().getField(varlist[i]);				
				str+=myField.get(this);
				if (i<varlist.length-1) str+="\t";
			} catch (Exception ex){
				ex.printStackTrace();
			}
		}

		return str;
	}
	
	public String toString() {
		return toString(varlist);
	}


	public static void writeToExcelNoDuplicates(String filepath, Vector<RecordOECD_Toolbox> records) {
		XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("sheet0");

        int rowNum = 0;
        System.out.println("Creating excel");

        XSSFRow rowHeader = sheet.createRow(rowNum);
        
        int col=0;
        for (int i=0;i<varlist.length;i++) {
        	XSSFCell cell = rowHeader.createCell(col++);
			cell.setCellValue(varlist[i]);
        }
        rowNum++;
        
        Vector<String>uniqueCAS=new Vector<>();
        
        
        for (RecordOECD_Toolbox r:records) {
        
        	if (uniqueCAS.contains(r.CAS)) continue;
        	else uniqueCAS.add(r.CAS);
        	
        	XSSFRow row = sheet.createRow(rowNum++);
        	
        	int colNum = 0;
        	
    		for (int i=0;i<varlist.length;i++) {
    			try {
    				Field myField =r.getClass().getField(varlist[i]);				
    				String val=(String)myField.get(r);
    				
    				XSSFCell cell = row.createCell(colNum++);
    				cell.setCellValue(val);
    			} catch (Exception ex){
    				ex.printStackTrace();
    			}
    		}
        }
        
        try {
            FileOutputStream outputStream = new FileOutputStream(filepath);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}

}