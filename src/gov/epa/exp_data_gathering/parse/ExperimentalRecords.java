package gov.epa.exp_data_gathering.parse;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;


import gov.epa.ghs_data_gathering.Parse.Parse;


/**
 * Class to store chemicals
 * 
 * @author Todd Martin
 *
 */

public class ExperimentalRecords extends Vector<ExperimentalRecord> {

	public JsonElement toJsonElement() {
		String strJSON=this.toJSON();
		Gson gson = new Gson();
		JsonElement json = gson.fromJson(strJSON, JsonElement.class);
		
		
		return json;
	}
	
	public ExperimentalRecord getRecord(String CAS) {
		
		for (ExperimentalRecord record:this) {
			if (record.casrn.equals(CAS)) return record;
		}
		return null;
	}
	
	public void addSourceBasedIDNumbers() {

		for (int i=0;i<size();i++) {
			ExperimentalRecord record=get(i);
			record.id_physchem=record.source_name+(i+1);
		}
	}
	
	
	public void toFlatFile(String filepath,String del) {
		
		try {
								
			FileWriter fw=new FileWriter(filepath);
			
			fw.write(getHeader(del)+"\r\n");
											
			for (ExperimentalRecord record:this) {				
				String line=record.toString("|");				
				line=Parse.fixChars(line);							
				fw.write(line+"\r\n");
			}
			fw.flush();
			fw.close();
						
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	public static String getHeader(String del) {
		// TODO Auto-generated method stub

		String [] fieldNames=ExperimentalRecord.outputFieldNames;
		String Line = "";
		for (int i = 0; i < fieldNames.length; i++) {
			Line += fieldNames[i];
			if (i < fieldNames.length - 1) {
				Line += del;
			}
			
		}

		return Line;
	}
	
	public String toJSON() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();// makes it multiline and readable
		Gson gson = builder.create();
		return gson.toJson(this);//all in one line!
	}
	

	
	public void toJSON_File(String filePath) {

		try {

			File file = new File(filePath);
			file.getParentFile().mkdirs();

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting().disableHtmlEscaping();
			Gson gson = builder.create();

			FileWriter fw = new FileWriter(file);
			fw.write(gson.toJson(this));
			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	public static ExperimentalRecords loadFromExcel(String excelFilePath) {

		try {

			ExperimentalRecords records = new ExperimentalRecords();

			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet firstSheet = workbook.getSheetAt(0);

			DataFormatter formatter = new DataFormatter();
			
			Row rowHeader = firstSheet.getRow(1);

			Vector<String>fieldNames=new Vector<String>();
			
			for (int i=0;i<rowHeader.getLastCellNum();i++) {
				String fieldName=rowHeader.getCell(i).getStringCellValue();
				fieldNames.add(fieldName);
			}
			
			
			for (int i=2;i<firstSheet.getLastRowNum();i++) {
				ExperimentalRecord record=new ExperimentalRecord();
			
				Row row = firstSheet.getRow(i);
				
				for (int j=0;j<fieldNames.size();j++) {
					String fieldName=fieldNames.get(j);					

					if (row.getCell(j)!=null) {
						String fieldValue=formatter.formatCellValue(row.getCell(j));
						record.assignValue(fieldName, fieldValue);
					}
					
				}
				records.add(record);
//				System.out.println(record.toJSON());
				
			}
			
			inputStream.close();
			workbook.close();
			return records;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public void toExcel_File(String filePath) {
		int size = this.size();
		Workbook wb = new XSSFWorkbook();
		Sheet recSheet = wb.createSheet("Records");
		Sheet badSheet = wb.createSheet("Records-Bad");
		Row recSubtotalRow = recSheet.createRow(0);
		Row recHeaderRow = recSheet.createRow(1);
		Row badSubtotalRow = badSheet.createRow(0);
		Row badHeaderRow = badSheet.createRow(1);
		String[] headers = ExperimentalRecord.outputFieldNames;
		CellStyle style = wb.createCellStyle();
		Font font = wb.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setFont(font);
		for (int i = 0; i < headers.length; i++) {
			Cell recCell = recHeaderRow.createCell(i);
			recCell.setCellValue(headers[i]);
			recCell.setCellStyle(style);
			Cell badCell = badHeaderRow.createCell(i);
			badCell.setCellValue(headers[i]);
			badCell.setCellStyle(style);
		}
		int recCurrentRow = 2;
		int badCurrentRow = 2;
		for (ExperimentalRecord er:this) {
			Class erClass = er.getClass();
			try {
				Row row = null;
				if (er.keep) {
					row = recSheet.createRow(recCurrentRow);
					recCurrentRow++;
				} else {
					row = badSheet.createRow(badCurrentRow);
					badCurrentRow++;
				}
				for (int i = 0; i < headers.length; i++) {
					Field field = erClass.getDeclaredField(headers[i]);
					Object value = field.get(er);
					if (value!=null && !(value instanceof Double)) { 
						String strValue = reverseFixChars(StringEscapeUtils.unescapeHtml4(value.toString()));
						row.createCell(i).setCellValue(strValue);
					} else if (value!=null) { row.createCell(i).setCellValue((double) value); }
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		String lastCol = CellReference.convertNumToColString(headers.length);
		recSheet.setAutoFilter(CellRangeAddress.valueOf("A2:"+lastCol+recCurrentRow));
		recSheet.createFreezePane(0, 2);
		badSheet.setAutoFilter(CellRangeAddress.valueOf("A2:"+lastCol+badCurrentRow));
		badSheet.createFreezePane(0, 2);
		
		for (int i = 0; i < headers.length; i++) {
			String col = CellReference.convertNumToColString(i);
			String recSubtotal = "SUBTOTAL(3,"+col+"$3:"+col+"$"+(recCurrentRow+1)+")";
			recSubtotalRow.createCell(i).setCellFormula(recSubtotal);
			String badSubtotal = "SUBTOTAL(3,"+col+"$3:"+col+"$"+(badCurrentRow+1)+")";
			badSubtotalRow.createCell(i).setCellFormula(badSubtotal);
		}
		
		try {
			OutputStream fos = new FileOutputStream(filePath);
			wb.write(fos);
			wb.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	public static String fixChars(String str) {
		str=str.replace("â€“","-").replace("â€™","'");
		str=str.replace("\uff08", "(");// ï¼ˆ
		str=str.replace("\uff09", ")");// ï¼‰
		str=str.replace("\uff0f", "/");// ï¼�
		str=str.replace("\u3000", " ");//blank
		str=str.replace("\u00a0", " ");//blank
		str=str.replace("\u2003", " ");//blank
		str=str.replace("\u0009", " ");//blank
		str=str.replace("\u300c", "");// ã€Œ
		str=str.replace("\u300d", "");// ã€�
		str=str.replace("\u2070", "^0");// superscript 0
		str=str.replace("\u00B9", "^1");// superscript 1
		str=str.replace("\u00B2", "^2");// superscript 2
		str=str.replace("\u00B3", "^3");// superscript 3
		str=str.replace("\u2074", "^4");// superscript 4
		str=str.replace("\u2075", "^5");// superscript 5
		str=str.replace("\u2076", "^6");// superscript 6
		str=str.replace("\u2077", "^7");// superscript 7
		str=str.replace("\u2078", "^8");// superscript 8
		str=str.replace("\u2079", "^9");// superscript 9
		str=str.replace("\u2080", "_0");// subscript 0
		str=str.replace("\u2081", "_1");// subscript 1
		str=str.replace("\u2082", "_2");// subscript 2
		str=str.replace("\u2083", "_3");// subscript 3
		str=str.replace("\u2084", "_4");// subscript 4
		str=str.replace("\u2085", "_5");// subscript 5
		str=str.replace("\u2086", "_6");// subscript 6
		str=str.replace("\u2087", "_7");// subscript 7
		str=str.replace("\u2088", "_8");// subscript 8
		str=str.replace("\u2089", "_9");// subscript 9
	
		return str;
	}

	public static ExperimentalRecords dumpBadRecords(ExperimentalRecords records) {
		ExperimentalRecords recordsBad = new ExperimentalRecords();
		Iterator<ExperimentalRecord> it = records.iterator();
		while (it.hasNext() ) {
			ExperimentalRecord temp = it.next();
			if (!temp.keep) {
				recordsBad.add(temp);
				it.remove();
			}
		}
		return recordsBad;
	}

	public static ExperimentalRecords loadFromJSON(String jsonFilePath) {

		try {
			Gson gson = new Gson();

			File file = new File(jsonFilePath);

			if (!file.exists()) {
				return null;
			}

			ExperimentalRecords chemicals = gson.fromJson(new FileReader(jsonFilePath), ExperimentalRecords.class);			
			return chemicals;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
//		ExperimentalRecords records = loadFromJSON("sample.json");
//		System.out.println(records.toJSON());
//		chemicals.toJSONElement();
		
		ExperimentalRecords records = loadFromExcel("data\\experimental\\eChemPortalAPI\\eChemPortalAPI Toxicity Experimental Records.xlsx");
		
	}

	public static String reverseFixChars(String str) {
		str=str.replace("^0","\u2070");// superscript 0
		str=str.replace("^1","\u00B9");// superscript 1
		str=str.replace("^2","\u00B2");// superscript 2
		str=str.replace("^3","\u00B3");// superscript 3
		str=str.replace("^4","\u2074");// superscript 4
		str=str.replace("^5","\u2075");// superscript 5
		str=str.replace("^6","\u2076");// superscript 6
		str=str.replace("^7","\u2077");// superscript 7
		str=str.replace("^8","\u2078");// superscript 8
		str=str.replace("^9","\u2079");// superscript 9
		str=str.replace("_0","\u2080");// subscript 0
		str=str.replace("_1","\u2081");// subscript 1
		str=str.replace("_2","\u2082");// subscript 2
		str=str.replace("_3","\u2083");// subscript 3
		str=str.replace("_4","\u2084");// subscript 4
		str=str.replace("_5","\u2085");// subscript 5
		str=str.replace("_6","\u2086");// subscript 6
		str=str.replace("_7","\u2087");// subscript 7
		str=str.replace("_8","\u2088");// subscript 8
		str=str.replace("_9","\u2089");// subscript 9
		return str;
	}

}
