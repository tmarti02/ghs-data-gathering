package gov.epa.QSAR.DataSetCreation.api;


import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

import gov.epa.QSAR.DataSetCreation.DataSetDatabaseUtilities;
import gov.epa.TEST.Descriptors.DatabaseUtilities.SQLite_GetRecords;
import gov.epa.api.ExperimentalConstants;
import gov.epa.database.SQLite_CreateTable;
import gov.epa.database.SQLite_Utilities;
import gov.epa.exp_data_gathering.parse.ParseUtilities;

public class RecordsQSAR extends ArrayList<RecordQSAR> {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8480087185267457724L;
	/**
	 * 
	 */	
	public static final String tableNameOverallSet="OverallSets";
	

	public void toExcelFile(String filePath) {
		toExcelFile(filePath, RecordQSAR.outputFieldNames);
	}
	
	
	private void writeSheet(Workbook wb,String sheetName,String [] fieldNames,boolean usable) {
		Class clazz = RecordQSAR.class;
		String[] headers = fieldNames;
		
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
		
		for (RecordQSAR qr:this) {
			try {
				Row row = null;
				
				if (qr.usable==usable) {
					row = recSheet.createRow(recCurrentRow);
					recCurrentRow++;
				} else continue;
				
				for (int i = 0; i < headers.length; i++) {
					Field field = clazz.getDeclaredField(headers[i]);
					Object value = field.get(qr);
					if (value!=null && !(value instanceof Double)) { 
						String strValue = ParseUtilities.reverseFixChars(StringEscapeUtils.unescapeHtml4(value.toString()));
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
		
		for (int i = 0; i < headers.length; i++) {
			String col = CellReference.convertNumToColString(i);
			String recSubtotal = "SUBTOTAL(3,"+col+"$3:"+col+"$"+(recCurrentRow+1)+")";
			recSubtotalRow.createCell(i).setCellFormula(recSubtotal);
		}
	}
	
	public void toExcelFile(String filePath,String[] fieldNames) {
		
		Workbook wb = new XSSFWorkbook();
		writeSheet(wb, "Records", fieldNames, true);
		writeSheet(wb, "Records-Bad", fieldNames, false);
				
		try {
			OutputStream fos = new FileOutputStream(filePath);
			wb.write(fos);
			wb.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	public void addFlatQSARRecordsToDB(String dbpath) {		
		
		try {
			Connection conn=SQLite_Utilities.getConnection(dbpath);
			Statement stat=conn.createStatement();			

			//This assumes all records are for same property:
			String property=get(0).property_name;
			
			SQLite_CreateTable.create_table(stat, tableNameOverallSet, RecordQSAR.outputFieldNamesDB);
			SQLite_Utilities.deleteRecords(tableNameOverallSet, "property_name", property, stat);			
			addRecordsBatch(conn, tableNameOverallSet, RecordQSAR.outputFieldNamesDB);
						
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	
	public void addRecordsBatch (Connection conn,String tableName,String []fieldNames) {
		try {
			
			conn.setAutoCommit(false);

			String s = SQLite_CreateTable.create_sql_insert(fieldNames, tableNameOverallSet);

			int counter = 0;
			int batchCounter = 0;
			PreparedStatement prep = conn.prepareStatement(s);
			
			
			for (RecordQSAR rec:this) {				
				counter++;
//				rec.id_physchem=counter;
				
//				String[] list = rec.toStringArray(ExperimentalRecord.outputFieldNames);
				if (counter%50000==0) System.out.println("Added "+counter+" entries...");
				rec.setComboID("|");

				
				String[] list = rec.toStringArray( fieldNames);

				if (list.length!=fieldNames.length) {//probably wont happen now that list is based on names array
					System.out.println("Wrong number of values: "+list[0]);
					break;
				}

								
				for (int i = 0; i < list.length; i++) {
					if (list[i]!=null && !list[i].isBlank()) {
						prep.setString(i + 1, list[i]);
					} else {
						prep.setString(i + 1, null);
					}
				}
				
				try {
					prep.addBatch();
				} catch (Exception ex) {
					System.out.println("Failed to add: "+String.join(",", list));
				}
				
				if (counter % 1000 == 0) {
					prep.executeBatch();
					batchCounter++;
				}

			}

			int[] count = prep.executeBatch();// do what's left
			
			conn.setAutoCommit(true);
									
			System.out.println("Added "+counter+" entries. Done!");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static RecordsQSAR getExperimentalRecordsFromDB(String property) {

		RecordsQSAR records = new RecordsQSAR();

		try {
			
			String sql = "select * from "+tableNameOverallSet+" WHERE property_name=\"" + property + "\"\r\n"
					+ "order by DSSTox_Structure_Id";

			
			Connection conn = SQLite_Utilities.getConnection(DataSetDatabaseUtilities.pathDataSetDB);
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery(sql);

			while (rs.next()) {
				RecordQSAR record = new RecordQSAR();
				SQLite_GetRecords.createRecord(rs, record);
				records.add(record);
			}
			System.out.println(records.size() + " records for " + property);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return records;
	}

	
}
