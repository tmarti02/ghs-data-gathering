package gov.epa.QSAR.DataSetCreation;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.epa.QSAR.utilities.ExcelUtilities;


public class RecordDSSTox {

	public String External_ID;
	public String DSSTox_Substance_Id;
	public String DSSTox_Source_Record_Id;
	public String DSSTox_Structure_Id;
	public String DSSTox_QC_Level;
	public String Substance_Name;
	public String Substance_CASRN;
	public String Substance_Type;
	public String Substance_Note;
	public String Structure_SMILES;
	public String Structure_InChI;
	public String Structure_InChIKey;
	public String Structure_Formula;
	public String Structure_MolWt;
	public String Structure_SMILES_2D_QSAR;
	public String DateModified;
	

	static String[] varlist = { "External_ID","DSSTox_Substance_Id","DSSTox_Source_Record_Id","DSSTox_Structure_Id","DSSTox_QC_Level",
			"Substance_Name","Substance_CASRN","Substance_Type","Substance_Note","Structure_SMILES","Structure_InChI","Structure_InChIKey","Structure_Formula",
			"Structure_MolWt","Structure_SMILES_2D_QSAR","DateModified"};
	
	public static String getHeader(String [] varlist) {
		String str="";
		for (int i=0;i<varlist.length;i++) {
			str+=varlist[i];
			if (i<varlist.length-1) str+="\t";
		}
		return str;
	}
	
	public static  Hashtable<String,RecordDSSTox> getDSSToxLookupByID_Physchem(Vector<RecordDSSTox> recordsDSSTox) {
		Hashtable<String,RecordDSSTox>htDSSTox=new Hashtable<>();
		
		for (RecordDSSTox recordDSSTox:recordsDSSTox) {
			
			String IDs=recordDSSTox.External_ID;
			IDs=IDs.replace("[", "").replace("]", "").replace(" ", "");
			
			String [] IDarray=IDs.split(",");

			for (String ID:IDarray) {
				htDSSTox.put(ID, recordDSSTox);
			}			
		}
		
		return htDSSTox;
	}
	
	
	public static  Hashtable<String,RecordDSSTox> getDSSToxLookupBySID(Vector<RecordDSSTox> recordsDSSTox) {
		Hashtable<String,RecordDSSTox>htDSSTox=new Hashtable<>();		
		for (RecordDSSTox recordDSSTox:recordsDSSTox) {
			if (recordDSSTox.DSSTox_Substance_Id!=null)
				htDSSTox.put(recordDSSTox.DSSTox_Substance_Id, recordDSSTox);				
		}
		return htDSSTox;
	}


	
	static RecordDSSTox getDSSToxRecord(RecordTox rTox,Hashtable<String, RecordDSSTox> htCAS_Name,Hashtable<String, RecordDSSTox> htCAS,Hashtable<String, RecordDSSTox> htName) {
//		System.out.println("here cas="+rTox.casrn_Source);
		
//		if (rTox.casrn_Source.contentEquals("129050-29-9")) {
//			System.out.println(htCAS_Name.get(rTox.casrn_Source+"_"+rTox.name_Source));
//			System.out.println(htCAS.get(rTox.casrn_Source));
//		}
		
		
		String name=rTox.name_Source.toLowerCase().trim();
		String cas=rTox.casrn_Source.trim();
		
		if (htCAS_Name.get(cas+"_"+name)!=null) {
			return htCAS_Name.get(cas+"_"+name);
		}
				
		if (htCAS.get(cas)!=null) {
//			System.out.println("Record retrieved by cas="+rTox.casrn_Source);
			return htCAS.get(cas);
		}
		
		if (htName.get(name)!=null) {
//			System.out.println("Record retrieved by name="+rTox.name_Source);
			return htName.get(name);
		}

		System.out.println("No record retrieved for "+rTox.name_Source+"\t"+rTox.casrn_Source);
		
//		System.out.println(htChemRegCAS_Name.get(rTox.CAS+"_"+rTox.chemicalName));
//		System.out.println(htChemRegCAS.get(rTox.CAS));
//		System.out.println();
		
		return null;
	}
	
	public static Vector<RecordDSSTox> getDSSToxExportRecords(String excelFilePath) {
		
			
		try {
			
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
			Workbook workbook = new XSSFWorkbook(inputStream);
			XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(0);

			
			Vector<RecordDSSTox> records = new Vector<>();
			
			XSSFRow rowHeader = sheet.getRow(0);

			Hashtable<Integer, String> htColNames = new Hashtable<>();// column name for each column number

			for (int col = 0; col < rowHeader.getLastCellNum(); col++) {
				String colName = ExcelUtilities.getStringValue(rowHeader.getCell(col)).replace("-", "_");
				colName=colName.replace("Extenal", "External");
				htColNames.put(col, colName.replace(" ", "_"));
			}

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {

				XSSFRow row = sheet.getRow(i);

				RecordDSSTox r = new RecordDSSTox();
				
				for (int j = 0; j < row.getLastCellNum(); j++) {
					String value = ExcelUtilities.getStringValue(row.getCell(j));
					r.setValue(htColNames.get(j), value);
					
//				System.out.println(htColNames.get(j)+"\t"+value);
					
				}
				
//			System.out.println(r);
							
				records.add(r);

//				if (r.Query_Casrn.contentEquals("Invalid CAS number: 0-11-0"))
//					System.out.println(r);
			}
			return records;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getHeader() {
		return getHeader(varlist);
	}
	
//	public static void getUniqueSIDs(Vector<RecordDSSTox> recordsDSSTox) {
//		Vector<String>uniqueChemRegSIDS=new Vector<>();
//		
//		for (RecordDSSTox r:recordsDSSTox) {
//			if(r.Top_HIT_DSSTox_Substance_Id!=null && !uniqueChemRegSIDS.contains(r.Top_HIT_DSSTox_Substance_Id)) {
//				System.out.println(r.Top_HIT_DSSTox_Substance_Id);
//				uniqueChemRegSIDS.add(r.Top_HIT_DSSTox_Substance_Id);
//			}
//		}
//	}
	
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