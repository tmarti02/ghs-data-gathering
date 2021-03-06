package gov.epa.exp_data_gathering.parse;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import gov.epa.api.ExperimentalConstants;
import gov.epa.eChemPortalAPI.Processing.FinalRecord;
import gov.epa.eChemPortalAPI.Query.APIConstants;
import gov.epa.ghs_data_gathering.Parse.Parse;


/**
 * Class to store chemicals
 * 
 * @author Todd Martin
 *
 */

public class ExperimentalRecords extends ArrayList<ExperimentalRecord> {
	
	public ExperimentalRecords() { }
	
	public ExperimentalRecords(FinalRecord rec) {
		int size = 1;
		int speciesSize = 1;
		if (rec.valueTypes!=null && !rec.valueTypes.isEmpty()) {
			size = rec.valueTypes.size();
		} else if (rec.genotoxicity!=null && !rec.genotoxicity.isEmpty()) {
			size = rec.genotoxicity.size();
			if (rec.propertyName.equals(APIConstants.geneticToxicityVitro)) { speciesSize = size; }
		}
		for (int i = 0; i < size; i++) {
			ExperimentalRecord er = new ExperimentalRecord();
			er.source_name = ExperimentalConstants.strSourceEChemPortalAPI;
			er.url = rec.url;
			er.original_source_name = rec.participant;
			er.date_accessed = rec.dateAccessed;
			er.reliability = rec.reliability;
			er.fr_id = rec.id;
			
			if (!rec.name.equals("-") && !rec.name.contains("unnamed")) {
				er.chemical_name = rec.name;
			}
			
			if (rec.numberType!=null) {
				switch (rec.numberType) {
				case "CAS Number":
					er.casrn = rec.number;
					break;
				case "EC Number":
					er.einecs = rec.number;
					break;
				}
			}
			
			String species = null;
			if (speciesSize>1) {
				species = (rec.species==null || rec.species.isEmpty()) ? null : rec.species.get(i);
			} else {
				species = (rec.species==null || rec.species.isEmpty()) ? null : rec.species.get(0);
			}
			
			String valueType = (rec.valueTypes==null || rec.valueTypes.isEmpty()) ? "" : "_"+rec.valueTypes.get(i);
			if (species!=null) {
				if (!species.toLowerCase().contains("other")) {
					er.property_name=species.replaceAll(" ","_").replaceAll(",","")+"_"+rec.propertyName+valueType;
				} else {
					er.property_name="other_"+rec.propertyName+valueType;
					er.updateNote("Species: "+species.substring(rec.species.indexOf(":")+1));
				}
			} else {
				er.property_name = rec.propertyName+valueType;
			}
			
			String value = null;
			if (rec.experimentalValues!=null && !rec.experimentalValues.isEmpty()) {
				value = rec.experimentalValues.get(i);
			} else if (rec.genotoxicity!=null && !rec.genotoxicity.isEmpty()) {
				value = rec.genotoxicity.get(i);
			} else if (rec.interpretationOfResults!=null && !rec.interpretationOfResults.isEmpty()) {
				value = rec.interpretationOfResults;
			}
			
			if (value==null) { continue; }
			ParseUtilities.getToxicity(er,value);
			er.property_value_string = "Value: "+value;
			
			this.add(er);
		}
	}

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
	
	/**
	 * TODO rewrite this so it uses same code for both tabs to make edits easier
	 * 
	 * @param filePath
	 * @param fieldNames
	 */
	public void toExcel_File(String filePath,String [] fieldNames) {

		String[] headers = fieldNames;

		int size = this.size();
		Workbook wb = new XSSFWorkbook();
		Sheet recSheet = wb.createSheet("Records");
		Sheet badSheet = wb.createSheet("Records-Bad");
		Row recSubtotalRow = recSheet.createRow(0);
		Row recHeaderRow = recSheet.createRow(1);
		Row badSubtotalRow = badSheet.createRow(0);
		Row badHeaderRow = badSheet.createRow(1);
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
			Object value = null;
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
					value = field.get(er);
					if (value!=null && !(value instanceof Double)) { 
						String strValue = ParseUtilities.reverseFixChars(StringEscapeUtils.unescapeHtml4(value.toString()));
						if (strValue.length() > 32767) { strValue = strValue.substring(0,32767); }
						row.createCell(i).setCellValue(strValue);
					} else if (value!=null) { row.createCell(i).setCellValue((double) value); }
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println(value.toString());
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
		
	public void toExcel_File(String filePath) {
		toExcel_File(filePath,ExperimentalRecord.outputFieldNames);
	}

	public ExperimentalRecords dumpBadRecords() {
		ExperimentalRecords recordsBad = new ExperimentalRecords();
		Iterator<ExperimentalRecord> it = this.iterator();
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

}
