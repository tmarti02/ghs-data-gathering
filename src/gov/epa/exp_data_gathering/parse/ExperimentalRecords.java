package gov.epa.exp_data_gathering.parse;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.common.usermodel.HyperlinkType;
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
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
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
	
	public static final String tableName = "ExperimentalRecords";


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
		
		CellStyle styleURL=createStyleURL(wb);
		
		writeSheet(headers, "Records",true,wb,styleURL);
		writeSheet(headers, "Records_Bad",false,wb,styleURL);
		
		try {
			OutputStream fos = new FileOutputStream(filePath);
			wb.write(fos);
			wb.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	static CellStyle createStyleURL(Workbook workbook) {
		CellStyle hlink_style = workbook.createCellStyle();
		Font hlink_font = workbook.createFont();
		hlink_font.setUnderline(Font.U_SINGLE);
		hlink_font.setColor(Font.COLOR_RED);
		hlink_style.setFont(hlink_font);
		return hlink_style;
	}

	private void writeSheet(String[] headers, String sheetName, boolean keep, Workbook wb, CellStyle styleURL) {
		Sheet recSheet = wb.createSheet(sheetName);
		Row recSubtotalRow = recSheet.createRow(0);
		Row recHeaderRow = recSheet.createRow(1);
		CellStyle style = wb.createCellStyle();
		Font font = wb.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setFont(font);
		for (int i = 0; i < headers.length; i++) {
			Cell recCell = recHeaderRow.createCell(i);
			recCell.setCellValue(headers[i]);
			recCell.setCellStyle(style);
		}
		int recCurrentRow = 2;
		for (ExperimentalRecord er:this) {
			if (!er.keep==keep) continue; 
			
			Class erClass = er.getClass();
			Object value = null;
			try {
				Row row = recSheet.createRow(recCurrentRow);
				recCurrentRow++;
				 
				for (int i = 0; i < headers.length; i++) {
					
					Field field = erClass.getDeclaredField(headers[i]);
					value = field.get(er);
					
					if (value==null) continue;
					
					if (headers[i].contentEquals("url")) {
						Cell cell = row.createCell(i);     						
						Hyperlink href = wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
						cell.setHyperlink((org.apache.poi.ss.usermodel.Hyperlink) href);
						href.setAddress((String)value);
						cell.setCellStyle(styleURL);
						cell.setCellValue(value+"");

					}else if (!(value instanceof Double)) { 
						String strValue = ParseUtilities.reverseFixChars(StringEscapeUtils.unescapeHtml4(value.toString()));
						if (strValue.length() > 32767) { strValue = strValue.substring(0,32767); }
						row.createCell(i).setCellValue(strValue);
					} else { 
						row.createCell(i).setCellValue((double) value); 
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println(value.toString());
			}
		}
		
		String lastCol = CellReference.convertNumToColString(headers.length);
		recSheet.setAutoFilter(CellRangeAddress.valueOf("A2:"+lastCol+recCurrentRow));
		recSheet.createFreezePane(0, 2);
		
		for (int i = 0; i < headers.length; i++) {
			String col = CellReference.convertNumToColString(i);
			String recSubtotal = "SUBTOTAL(3,"+col+"$3:"+col+"$"+(recCurrentRow+1)+")";
			recSubtotalRow.createCell(i).setCellFormula(recSubtotal);
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

	public static Vector<String> getSourceList(Connection conn, String property) {
		Vector<String> sources = new Vector<>();

		String sql = "select distinct source_name from "+tableName+" where property_name=\"" + property
				+ "\" and keep=\"true\"";

		try {
			Statement stat=conn.createStatement();
			ResultSet rs = stat.executeQuery(sql);

			while (rs.next()) {
				String source = rs.getString(1);
//				System.out.println(source);
				sources.add(source);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sources;
	}
	public static ExperimentalRecords getExperimentalRecordsFromDB_ForSource(String property, String source_name,
			String expRecordsDBPath) {

		ExperimentalRecords records = new ExperimentalRecords();

//		String sql = "select * from "+tableName+" where property_name=\"" + property + "\" and keep=\"true\" "
//				+ "and source_name=\"" + source_name + "\"\r\n" + "order by casrn";
		
		String sql = "select * from "+tableName+" where property_name=\"" + property + "\""
				+ "and source_name=\"" + source_name + "\"\r\n" + "order by casrn";

		
		System.out.println("sql for getExperimentalRecordsFromDB_ForSource:"+sql);
		try {
			Connection conn = SQLite_Utilities.getConnection(expRecordsDBPath);
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery(sql);

			while (rs.next()) {
				ExperimentalRecord record = new ExperimentalRecord();
				SQLite_GetRecords.createRecord(rs, record);
				records.add(record);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return records;

	}
	public static ExperimentalRecords getExperimentalRecordsFromDB(String property, String expRecordsDBPath, boolean useKeep) {

		ExperimentalRecords records = new ExperimentalRecords();

		String sql="select * from "+tableName+" where property_name=\"" + property + "\""
				+ "order by casrn";
		
		if (useKeep) {
			sql="select * from "+tableName+" where property_name=\"" + property + "\" and keep=\"true\" " + "\r\n"
					+ "order by casrn";
		}
		
		try {
			Connection conn = SQLite_Utilities.getConnection(expRecordsDBPath);
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery(sql);

			while (rs.next()) {
				ExperimentalRecord record = new ExperimentalRecord();

				SQLite_GetRecords.createRecord(rs, record);
				records.add(record);
			}
			System.out.println(records.size() + " record for " + property + " where keep=true");

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return records;
	}
	
	ExperimentalRecords getRecordsWithProperty(String propertyName, ExperimentalRecords records) {
		ExperimentalRecords records2 = new ExperimentalRecords();

		for (ExperimentalRecord record : records) {
			if (record.property_name.contentEquals(propertyName)) {
				records2.add(record);
			}
		}
		return records2;
	}

	
	public static void main(String[] args) {
//		ExperimentalRecords records = loadFromJSON("sample.json");
//		System.out.println(records.toJSON());
//		chemicals.toJSONElement();
		
		ExperimentalRecords records = loadFromExcel("data\\experimental\\eChemPortalAPI\\eChemPortalAPI Toxicity Experimental Records.xlsx");
		
	}

}
