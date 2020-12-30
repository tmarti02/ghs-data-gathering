package gov.epa.exp_data_gathering.parse;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
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

import gov.epa.api.ScoreRecord;
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
	
	
	
	public void toFlatFile(String filepath,String del) {
		
		try {
								
			FileWriter fw=new FileWriter(filepath);
			
			fw.write(ScoreRecord.getHeader(del)+"\r\n");
											
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
			builder.setPrettyPrinting();
			Gson gson = builder.create();

			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
			fw.write(reverseFixChars(StringEscapeUtils.unescapeHtml4(gson.toJson(this))));
			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
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
			if (i < 10) {
				Cell badCell = badHeaderRow.createCell(i);
				badCell.setCellValue(headers[i]);
				badCell.setCellStyle(style);
			}
		}
		int recCurrentRow = 2;
		int badCurrentRow = 2;
		for (ExperimentalRecord er:this) {
			Class erClass = er.getClass();
			try {
				if (er.keep) {
					Row recRow = recSheet.createRow(recCurrentRow);
					recCurrentRow++;
					for (int i = 0; i < headers.length; i++) {
						Field field = erClass.getDeclaredField(headers[i]);
						Object value = field.get(er);
						if (value!=null && !(value instanceof Double)) { recRow.createCell(i).setCellValue(reverseFixChars(value.toString()));
						} else if (value!=null) { recRow.createCell(i).setCellValue((double) value); }
					}
				} else {
					Row badRow = badSheet.createRow(badCurrentRow);
					badCurrentRow++;
					for (int i = 0; i < 11; i++) {
						Field field = erClass.getDeclaredField(headers[i]);
						Object value = field.get(er);
						if (value!=null && !(value instanceof Double)) { badRow.createCell(i).setCellValue(reverseFixChars(value.toString()));
						} else if (value!=null) { badRow.createCell(i).setCellValue((double) value); }
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		recSheet.setAutoFilter(CellRangeAddress.valueOf("A2:Z"+recCurrentRow));
		recSheet.createFreezePane(0, 2);
		badSheet.setAutoFilter(CellRangeAddress.valueOf("A2:K"+badCurrentRow));
		badSheet.createFreezePane(0, 2);
		
		for (int i = 0; i < headers.length; i++) {
			String col = CellReference.convertNumToColString(i);
			String recSubtotal = "SUBTOTAL(3,"+col+"$3:"+col+"$"+(recCurrentRow+1)+")";
			recSubtotalRow.createCell(i).setCellFormula(recSubtotal);
			if (i < 11) {
				String badSubtotal = "SUBTOTAL(3,"+col+"$3:"+col+"$"+(badCurrentRow+1)+")";
				badSubtotalRow.createCell(i).setCellFormula(badSubtotal);
			}
		}
		
		try {
			OutputStream fos = new FileOutputStream(filePath);
			wb.write(fos);
			wb.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
		ExperimentalRecords records = loadFromJSON("sample.json");
		System.out.println(records.toJSON());
//		chemicals.toJSONElement();
	}

	private static String reverseFixChars(String str) {
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
