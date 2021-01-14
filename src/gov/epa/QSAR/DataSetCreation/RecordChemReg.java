package gov.epa.QSAR.DataSetCreation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.epa.QSAR.utilities.ExcelUtilities;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;


public class RecordChemReg {

	public String Lookup_Result;
	public String Query_Casrn;
	public String Query_Name;
	public String Top_HIT_DSSTox_Substance_Id;
	public String Top_Hit_Casrn;
	public String Top_Hit_Name;
	public String Validated;

	
	static String[] varlist = { "Lookup_Result", "Query_Casrn", "Query_Name", 
			"Top_HIT_DSSTox_Substance_Id","Top_Hit_Casrn","Top_Hit_Name","Validated"};
	
	
	
	public boolean isChemRegOK() {
		if (Lookup_Result.contentEquals("No Hits"))
			return false;
		if (Validated.contentEquals("FALSE"))
			return false;
		return true;
	}
	
	
	static void createLookupForChemRegRecords(Vector<RecordChemReg> recordsChemReg,
			Hashtable<String, RecordChemReg> htChemRegCAS_Name, Hashtable<String, RecordChemReg> htChemRegCAS,
			Hashtable<String, RecordChemReg> htChemRegName) {
		
		for (RecordChemReg recordChemReg : recordsChemReg) {			
			recordChemReg.Query_Name=recordChemReg.Query_Name.toLowerCase();
			
			String key = recordChemReg.Query_Casrn + "_" + recordChemReg.Query_Name;
			htChemRegCAS_Name.put(key, recordChemReg);
			htChemRegCAS.put(recordChemReg.Query_Casrn, recordChemReg);
			htChemRegName.put(recordChemReg.Query_Name, recordChemReg);
//			System.out.println(recordChemReg.Query_Casrn+"\t"+recordChemReg.Query_Name);
		}
	}
	
	public static RecordChemReg getChemRegRecord(RecordQSAR rec,
			Hashtable<String, RecordChemReg> htChemRegCAS_Name,
			Hashtable<String, RecordChemReg> htChemRegCAS,
			Hashtable<String, RecordChemReg> htChemRegName) {
		
		String name=null;		
		if (rec.chemical_name!=null) name=StringEscapeUtils.escapeJava(rec.chemical_name).toLowerCase();
		
		String cas=null;		
		if (rec.casrn!=null) cas=rec.casrn;

		if (cas!=null && name!=null && htChemRegCAS_Name.get(cas+"_"+name)!=null) {			
//			System.out.println(rec.id_physchem+"\tmatch by CAS_Name: "+rec.casrn+"\t"+rec.chemical_name);
			return htChemRegCAS_Name.get(cas+"_"+name);
		}
				
		if (cas!=null && htChemRegCAS.get(cas)!=null) {
			
			if (name!=null)
				System.out.println(rec.id_physchem+"\tmatch by CAS: "+rec.casrn+"\t"+rec.chemical_name+"\t"+htChemRegCAS.get(cas).Query_Name);			
			
			return htChemRegCAS.get(cas);
		}
		
		if (name!=null && htChemRegName.get(name)!=null) {
//			System.out.println(rec.id_physchem+"\tmatch by Name: "+rec.casrn+"\t"+rec.chemical_name);
			return htChemRegName.get(name);
		}
		
		return null;
	}
	
	static Vector<RecordChemReg> getChemRegRecords(String excelFilePath) {
		
		
		try {
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
			Workbook workbook = new XSSFWorkbook(inputStream);
			XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(0);

			Vector<RecordChemReg> records = new Vector<>();
			
			XSSFRow rowHeader = sheet.getRow(0);

			Hashtable<Integer, String> htColNames = new Hashtable<>();// column name for each column number

			for (int col = 0; col < rowHeader.getLastCellNum(); col++) {
				String colName = ExcelUtilities.getStringValue(rowHeader.getCell(col));
//			System.out.println(col+"\t"+colName);
				
				htColNames.put(col, colName.replace(" ", "_"));
			}

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {

				XSSFRow row = sheet.getRow(i);

				RecordChemReg r = new RecordChemReg();
				
				for (int j = 0; j < row.getLastCellNum(); j++) {
					String value = ExcelUtilities.getStringValue(row.getCell(j));
					r.setValue(htColNames.get(j), value);
				}
				
//			System.out.println(r);
							
				records.add(r);

//				if (r.Query_Casrn.contentEquals("Invalid CAS number: 0-11-0"))
//					System.out.println(r);
			}
			return records;
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
	
	public static void getUniqueSIDs(Vector<RecordChemReg> recordsChemReg) {
		Vector<String>uniqueChemRegSIDS=new Vector<>();
		
		for (RecordChemReg r:recordsChemReg) {
			if(r.Top_HIT_DSSTox_Substance_Id!=null && !uniqueChemRegSIDS.contains(r.Top_HIT_DSSTox_Substance_Id)) {
				System.out.println(r.Top_HIT_DSSTox_Substance_Id);
				uniqueChemRegSIDS.add(r.Top_HIT_DSSTox_Substance_Id);
			}
		}
	}
	
	public void setValue(String fieldName,String fieldValue) {
		
		try {
			Field myField =this.getClass().getField(fieldName);				
		
			myField.set(this, fieldValue);
			
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
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

}