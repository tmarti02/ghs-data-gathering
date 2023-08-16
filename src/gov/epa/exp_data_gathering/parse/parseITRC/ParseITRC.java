package gov.epa.exp_data_gathering.parse.parseITRC;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;

import com.google.gson.JsonObject;



public class ParseITRC {

	static void getCMC() {
		
		String filename="ITRC_PFAS_PhysChemProp_Table_4-1_Oct2021.xlsx";
		String sheetName="Critical Micelle Conc. (CMC)";
		String mainFolderPath="data\\experimental\\ITRC\\excel files\\";

		try {
			FileInputStream fis = new FileInputStream(new File(mainFolderPath+filename));
			Workbook wb = WorkbookFactory.create(fis);
			Sheet sheet = wb.getSheet(sheetName);
			
			Row hrow2 = sheet.getRow(6);
			
			Hashtable<String,Integer>htCols=new Hashtable<>();
			
			getCols(5,sheet, htCols);
			getCols(6, sheet, htCols);
			
			String name="", CAS="";
			Double CMC_logM=null;
			
			Hashtable<String,Vector<Double>>htCMC=new Hashtable<>();
			
			
			for (int i=8;i<=87;i++) {
				Row row = sheet.getRow(i);
				if (row==null) continue;
				int colName=htCols.get("PFAS Name (§)");
				int colCAS=htCols.get("CAS");
				int col_logM=htCols.get("log  (mol/L)");
				String content = row.getCell(colName,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();				
				if (!content.isBlank()) name=content;

				content = row.getCell(colCAS,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();				
				if (!content.isBlank()) CAS=content;

				double dcontent = row.getCell(col_logM,MissingCellPolicy.CREATE_NULL_AS_BLANK).getNumericCellValue();				
				if (dcontent==0) continue;
				
				CMC_logM=dcontent;
				System.out.println(name+"\t"+CAS+"\t"+CMC_logM);
				
				if (htCMC.get(CAS)==null) {					
					Vector<Double>vals=new Vector<>();
					htCMC.put(CAS, vals);
					vals.add(CMC_logM);
					
				} else {
					Vector<Double>vals=htCMC.get(CAS);
					vals.add(CMC_logM);
				}
			}
			
			calcMedianValues(htCMC);
			

			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static void getWS() {
		
		String filename="ITRC_PFAS_PhysChemProp_Table_4-1_Oct2021.xlsx";
		String sheetName="Solubility (S)";
		String mainFolderPath="data\\experimental\\ITRC\\excel files\\";

		try {
			FileInputStream fis = new FileInputStream(new File(mainFolderPath+filename));
			Workbook wb = WorkbookFactory.create(fis);
			Sheet sheet = wb.getSheet(sheetName);
			
			Row hrow2 = sheet.getRow(6);
			
			Hashtable<String,Integer>htCols=new Hashtable<>();
			
			getCols(5,sheet, htCols);
			getCols(6, sheet, htCols);
			
			String name="", CAS="";
			Double CMC_logM=null;
			
			Hashtable<String,Vector<Double>>htCMC=new Hashtable<>();
			
			
			for (int i=8;i<=264;i++) {
				Row row = sheet.getRow(i);
				if (row==null) continue;
				int colName=htCols.get("PFAS Name (§)");
				int colCAS=htCols.get("CAS");
				int col_logM=htCols.get("log  (mol/L)");
				String content = row.getCell(colName,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();				
				if (!content.isBlank()) name=content;

				content = row.getCell(colCAS,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();				
				if (!content.isBlank()) CAS=content;

				double dcontent = row.getCell(col_logM,MissingCellPolicy.CREATE_NULL_AS_BLANK).getNumericCellValue();				
				if (dcontent==0) continue;
				
				CMC_logM=dcontent;
				System.out.println(name+"\t"+CAS+"\t"+CMC_logM);
				
				if (htCMC.get(CAS)==null) {					
					Vector<Double>vals=new Vector<>();
					htCMC.put(CAS, vals);
					vals.add(CMC_logM);
					
				} else {
					Vector<Double>vals=htCMC.get(CAS);
					vals.add(CMC_logM);
				}
			}
			
			calcMedianValues(htCMC);
			

			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static double calculateSD(Vector<Double> numArray)
    {
        double sum = 0.0, standardDeviation = 0.0;
        int length = numArray.size();

        for(double num : numArray) {
            sum += num;
        }

        double mean = sum/length;

        for(double num: numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation/(length-1));
    }
	
	
	static void calcMedianValues(Hashtable<String,Vector<Double>>htCMC) {
		Set<String> setOfKeys = htCMC.keySet();
		
		for (String key : setOfKeys) {
			Vector<Double>vals=htCMC.get(key);
			
			double median=0;
			
			int size=vals.size();
						
			if (size % 2 == 0) {//even number of records, need to determine average of middle 2 values
				median=(vals.get(size/2-1)+vals.get(size/2))/2.0;
				
//				System.out.println(size+"\t"+(size/2-1)+"\t"+(size/2));
				
			} else {//odd number of records, use middle one
				median=vals.get(size/2);
//				System.out.println(size+"\t"+(size/2));
				
			}		
			
			double stdDev=calculateSD(vals);
			
			System.out.println(key+"\t"+median+"\t"+stdDev);
			
		}
		
	}


	private static void getCols(int row, Sheet sheet, Hashtable<String, Integer> htCols) {
		Row hrow = sheet.getRow(row);

		
		for (int i=0;i<hrow.getLastCellNum();i++) {
			String content = hrow.getCell(i,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			content=content.replace("\r", " ").replace("\n", " ");
			if (!content.isBlank()) {
				htCols.put(content,i);
//				System.out.println(i+"\t"+content);
			}
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		getCMC();
		getWS();
		
	}

}
