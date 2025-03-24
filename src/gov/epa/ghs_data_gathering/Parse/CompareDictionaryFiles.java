package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CompareDictionaryFiles {

	static void go () {

		String fileLocation="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 alternatives assessment\\0 dictionary\\HCD_dictionary-2020-12-4.xlsx";
		String fileLocation2="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 alternatives assessment\\0 dictionary\\HCD_dictionary-2021-03-04.xlsx";
		
		try {
		
			FileInputStream fis = new FileInputStream(new File(fileLocation));			
			Workbook workbook = new XSSFWorkbook(fis);
			
			FileInputStream fis2 = new FileInputStream(new File(fileLocation2));			
			Workbook workbook2 = new XSSFWorkbook(fis2);

//			int sheetNum=0;
			for (int sheetNum=0;sheetNum<workbook.getNumberOfSheets();sheetNum++) {
				Map<Integer, List<String>>map1=getMapForSheet(workbook,sheetNum);
				Map<Integer, List<String>>map2=getMapForSheet(workbook2,sheetNum);

				for (Integer rowNum : map1.keySet()) {
					//				System.out.println(rowNum);

					List<String>list1=map1.get(rowNum);
					List<String>list2=map2.get(rowNum);

					for (int i=0;i<list1.size();i++) {
						
						if (i==list2.size()) {
							System.out.println(workbook.getSheetAt(sheetNum).getSheetName()+"\t"+rowNum+"\t"+i+"\t"+list1.get(i)+"\tMissing");
							continue;
						}
						
						if (!list1.get(i).equals(list2.get(i)))
							System.out.println(workbook.getSheetAt(sheetNum).getSheetName()+"\t"+rowNum+"\t"+i+"\t"+list1.get(i)+"\t"+list2.get(i));
					}
				}
			}
				
			
			
			fis.close();
			fis2.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private static Map<Integer, List<String>>  getMapForSheet(Workbook wb,int sheetNum) {
		Map<Integer, List<String>> data = new HashMap<>();
		
		Sheet sheet = wb.getSheetAt(sheetNum);
		
		
		
		for (int i=0;i<sheet.getLastRowNum();i++) {
		    Row row=sheet.getRow(i);
		    
		    if (row==null) continue;
		    
			data.put(row.getRowNum(), new ArrayList<String>());
		    
			for (int j=0;j<row.getLastCellNum();j++) {
		    	
				Cell cell=row.getCell(j);
		    	
				if (cell==null) {
					data.get(Integer.valueOf(row.getRowNum())).add(" ");
					continue;
				}
				
		        switch (cell.getCellType()) {
		            case STRING: 
		            	 data.get(Integer.valueOf(row.getRowNum())).add(cell.getStringCellValue());
//		            	 System.out.println(cell.getRowIndex()+"\t"+cell.getColumnIndex()+"\t"+cell.getStringCellValue());			            	 
		            	break;
		            case NUMERIC: 
		            	data.get(Integer.valueOf(row.getRowNum())).add(cell.getNumericCellValue()+"");
		            	break;
		            case BOOLEAN: 
		            	data.get(Integer.valueOf(row.getRowNum())).add(cell.getBooleanCellValue()+"");
		            	break;
		            case FORMULA: 
		            	break;
		            default: data.get(Integer.valueOf(row.getRowNum())).add(" ");
		        }
		    }
		    
		}
		
		return data;
		
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		go();

	}

}
