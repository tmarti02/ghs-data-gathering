package gov.epa.exp_data_gathering.parse.NICEATM;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.AtomContainer;

import gov.epa.ghs_data_gathering.Utilities.ExcelUtilities;

public class RecordNICEATM {

	public String Chemical_Name;
	public String CASRN;
	public String Molecular_Weight;
	public String Chemical_Class;
	public String LLNA_Vehicle;
	public String EC3;
	public String LLNA_Result;
	public String Class;
	public String Reference;
	public String Smiles;
	
	static String[] varlist = { "Chemical_Name","CASRN","Molecular_Weight","Chemical_Class","Smiles",
			"LLNA_Vehicle","EC3","LLNA_Result","Class","Reference"};

	static String[] varlist2 = { "CASRN","Chemical_Name","Molecular_Weight","LLNA_Result","Reference"};

	
//	class CustomComparator implements Comparator<RecordNICEATM>{
//	    int col;
//		
//		public int compare(RecordNICEATM ac1,RecordNICEATM ac2) {	        
//    		String strval1=(String)ac1.Chemical_Name;
//    		String strval2=(String)ac2.Chemical_Name;
//    		return strval1.compareTo(strval2);	
//	    }
//	}
	
	public static String getHeader(String [] varlist) {
		String str="";
		for (int i=0;i<varlist.length;i++) {
			str+=varlist[i];
			if (i<varlist.length-1) str+="\t";
		}
		return str;
	}
	public static String getHeader2() {
		return getHeader(varlist2);
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
	
	Vector<RecordNICEATM> parseExcel(String filePathExcel) {
			try {
	
				Vector<RecordNICEATM> records = new Vector<>();
				FileInputStream inputStream = new FileInputStream(new File(filePathExcel));
				DataFormatter formatter = new DataFormatter();
	
				Workbook workbook = new HSSFWorkbook(inputStream);
				Sheet sheet = workbook.getSheetAt(0);
				
				Row headerRow=sheet.getRow(0);
				
				int colName=ExcelUtilities.getColNum(headerRow,"Chemical Name");
				int colCASRN=ExcelUtilities.getColNum(headerRow,"CASRN");
				int colMolecularWeight=ExcelUtilities.getColNum(headerRow,"Molecular Weight (g/mol)");
				int colChemical_Class=ExcelUtilities.getColNum(headerRow,"Chemical Class");
				
				int colLLNA_Vehicle=ExcelUtilities.getColNum(headerRow,"LLNA Vehicle");
				int colEC3=ExcelUtilities.getColNum(headerRow,"EC3 (%)");
				int colLLNA_Result=ExcelUtilities.getColNum(headerRow," LLNA Result");
				int colReference=ExcelUtilities.getColNum(headerRow,"Reference");
				
	//			System.out.println(colName);
	
				int rows=sheet.getLastRowNum();
				
				for (int i=1;i<rows;i++) {
				
					Row row=sheet.getRow(i);
					
					RecordNICEATM r=new RecordNICEATM();
					
					if (row.getCell(colName)==null) {
						break;
					}
					
					r.Chemical_Name=ExcelUtilities.getValue(formatter, colName, row);
					r.CASRN=ExcelUtilities.getValue(formatter, colCASRN, row);
					r.Molecular_Weight=ExcelUtilities.getValue(formatter, colMolecularWeight, row);
					r.Chemical_Class=ExcelUtilities.getValue(formatter, colChemical_Class, row);
					
					r.LLNA_Vehicle=ExcelUtilities.getValue(formatter, colLLNA_Vehicle, row);
					r.EC3=ExcelUtilities.getValue(formatter, colEC3, row);
					r.LLNA_Result=ExcelUtilities.getValue(formatter, colLLNA_Result, row);
					r.Reference=ExcelUtilities.getValue(formatter, colReference, row);
					
					
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
	public static Vector<RecordNICEATM> parseExcel2(String filePathExcel) {
		try {
	
			Vector<RecordNICEATM> records = new Vector<>();
			FileInputStream inputStream = new FileInputStream(new File(filePathExcel));
			DataFormatter formatter = new DataFormatter();
	
			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(0);
			
			Row headerRow=sheet.getRow(0);
			
			int colName=ExcelUtilities.getColNum(headerRow,"Compound name");
			int colCASRN=ExcelUtilities.getColNum(headerRow,"CASRN");
			int colSMILES=ExcelUtilities.getColNum(headerRow,"SMILES");
			int colActivity=ExcelUtilities.getColNum(headerRow,"Activity");
			int colClass=ExcelUtilities.getColNum(headerRow,"Class");
			
			int colEC3=ExcelUtilities.getColNum(headerRow,"EC3 (%)");
			int colMolecularWeight=ExcelUtilities.getColNum(headerRow,"MW");
			int colChemical_Class=ExcelUtilities.getColNum(headerRow,"Chemical Class");
			
			int rows=sheet.getLastRowNum();
			
			for (int i=1;i<rows;i++) {
			
				Row row=sheet.getRow(i);
				
				RecordNICEATM r=new RecordNICEATM();
				
				if (row.getCell(colName)==null) {
					break;
				}
				
				r.Chemical_Name=ExcelUtilities.getValue(formatter, colName, row);
				r.CASRN=ExcelUtilities.getValue(formatter, colCASRN, row);
				r.Molecular_Weight=ExcelUtilities.getValue(formatter, colMolecularWeight, row);
				r.Chemical_Class=ExcelUtilities.getValue(formatter, colChemical_Class, row);
				
				r.EC3=ExcelUtilities.getValue(formatter, colEC3, row);
				r.Class=ExcelUtilities.getValue(formatter, colClass, row);
				r.LLNA_Result=ExcelUtilities.getValue(formatter, colActivity, row);
				r.Smiles=ExcelUtilities.getValue(formatter, colSMILES, row);				
				
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

}