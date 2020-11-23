package gov.epa.exp_data_gathering.parse;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Vector;

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

			FileWriter fw = new FileWriter(file);
			fw.write(gson.toJson(this));
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
		String[] headers = {"casrn","einecs","chemical_name","property_name","property_value_string","property_value_numeric_qualifier",
				"property_value_point_estimate_final","property_value_min_final","property_value_max_final","property_value_units_final","pressure_kPa","temperature_C",
				"property_value_qualitative","measurement_method","note","flag"};
		CellStyle style = wb.createCellStyle();
		Font font = wb.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setFont(font);
		for (int i = 0; i < headers.length; i++) {
			Cell recCell = recHeaderRow.createCell(i);
			recCell.setCellValue(headers[i]);
			recCell.setCellStyle(style);
			String col = CellReference.convertNumToColString(i);
			String subtotal = "SUBTOTAL(3,"+col+"$3:"+col+"$"+(size+3)+")";
			if (i >= 3) { recSubtotalRow.createCell(i).setCellFormula(subtotal); }
			if (i < 5) {
				Cell badCell = badHeaderRow.createCell(i);
				badCell.setCellValue(headers[i]);
				badCell.setCellStyle(style);
				if (i >= 3) { badSubtotalRow.createCell(i).setCellFormula(subtotal); }
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
						if (value!=null && !(value instanceof Double)) { recRow.createCell(i).setCellValue(value.toString());
						} else if (value!=null) { recRow.createCell(i).setCellValue((double) value); }
					}
				} else {
					Row badRow = badSheet.createRow(badCurrentRow);
					badCurrentRow++;
					for (int i = 0; i < 5; i++) {
						Field field = erClass.getDeclaredField(headers[i]);
						Object value = field.get(er);
						if (value!=null && !(value instanceof Double)) { badRow.createCell(i).setCellValue(value.toString());
						} else if (value!=null) { badRow.createCell(i).setCellValue((double) value); }
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		recSheet.setAutoFilter(CellRangeAddress.valueOf("A2:O"+recCurrentRow));
		badSheet.setAutoFilter(CellRangeAddress.valueOf("A2:D"+badCurrentRow));
		
		try {
			OutputStream fos = new FileOutputStream(filePath);
			wb.write(fos);
			wb.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	public static ExperimentalRecords loadFromJSON(String jsonFilePath) {

		try {
			Gson gson = new Gson();

			File file = new File(jsonFilePath);

			if (!file.exists())
				return null;

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

	

}
