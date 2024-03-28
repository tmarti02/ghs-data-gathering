package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

//import gov.epa.QSAR.DataSetCreation.ConvertExperimentalRecordsToDataSet;
import gov.epa.api.ExperimentalConstants;
import gov.epa.database.SQLite_CreateTable;
import gov.epa.database.SQLite_Utilities;
import gov.epa.eChemPortalAPI.Processing.FinalRecord;
import gov.epa.eChemPortalAPI.Processing.FinalRecords;

public class DataFetcher {
	
	static ExperimentalRecords records;
	
	public static final String mainFolder = "Data"+File.separator+"Experimental";
	public String databasePath;
	public String jsonPath;
	public String[] sources;
	public String recordType;
	
	public DataFetcher(String[] sources,String recordType) {
		this.sources = sources;
		this.recordType = recordType;
		String fileName = recordType.toLowerCase().contains("tox") ? "ToxicityRecords" : "ExperimentalRecords";
		databasePath = mainFolder+File.separator+fileName+".db";
		jsonPath = mainFolder+File.separator+fileName+".json";
		
		records = new ExperimentalRecords();
		for (String source:sources) {
			String toxNote = recordType.toLowerCase().contains("tox") ? " Toxicity" : "";
			String recordFilePath = mainFolder+File.separator+source+File.separator+source+toxNote+" Experimental Records.json";
			String badRecordFilePath = mainFolder+File.separator+source+File.separator+source+toxNote+" Experimental Records-Bad.json";
			
			File fileRecords=new File(recordFilePath);
			File fileBadRecords=new File(badRecordFilePath);
			
			ExperimentalRecords sourceRecords=null;
			
			sourceRecords = getExperimentalRecordsFromNumberedFiles(source,toxNote, false);
			
			if ((sourceRecords==null || sourceRecords.isEmpty()) && fileRecords.exists()) {
				sourceRecords = ExperimentalRecords.loadFromJSON(recordFilePath);
			} else if (sourceRecords==null) {
				System.out.println("No records file for "+source);
				continue;
			}
			
			ExperimentalRecords badSourceRecords=null;
			
			badSourceRecords = getExperimentalRecordsFromNumberedFiles(source,toxNote, true);
			
			if ((badSourceRecords==null || badSourceRecords.isEmpty()) && fileBadRecords.exists()) {
				badSourceRecords = ExperimentalRecords.loadFromJSON(badRecordFilePath);
			}
			
			sourceRecords.addAll(badSourceRecords);
			
			System.out.println("Fetching data from "+source+"\t"+sourceRecords.size());

//			addSourceBasedIDNumbers(sourceRecords);			
			records.addAll(sourceRecords);
			
		}
	}
	
	private ExperimentalRecords getExperimentalRecordsFromNumberedFiles(String source,String toxNote, boolean getBadRecords) {
		ExperimentalRecords sourceRecords = new ExperimentalRecords();
		int batch = 1;
		ExperimentalRecords temp = new ExperimentalRecords();
		
		String badNote = getBadRecords ? "-Bad " : " ";
		while (temp!=null) {
			String filePath = mainFolder+File.separator+source+File.separator+source+toxNote+" Experimental Records"+badNote+batch+".json";
//			System.out.println("filePath from getRecordsFromNumberedFiles:"+filePath); 
			
			temp = ExperimentalRecords.loadFromJSON(filePath);
			if (temp==null) break;
			sourceRecords.addAll(temp);
			batch++;
		}
		return sourceRecords;
	}
	
	/**
	 * Gets FinalRecords from original numbered toxicity json files
	 * @param source
	 * @return
	 */
	public FinalRecords getFinalRecordsFromNumberedFiles(String source) {
		FinalRecords sourceRecords = new FinalRecords();
		int batch = 1;
		
		FinalRecords temp = new FinalRecords();
				
		while (temp!=null) {
			String filePath = mainFolder+File.separator+source+File.separator+source+" Toxicity Original Records "+batch+".json";
//			System.out.println("filePath from getRecordsFromNumberedFiles:"+filePath); 
			
			temp = FinalRecords.loadFromJSON(filePath);
			if (temp==null) break;
			sourceRecords.addAll(temp);
			batch++;
		}
		return sourceRecords;
	}
	
	
	public void createRecordsDatabase() {
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		addExperimentalRecordsToDatabase(records);
	}
	
	public void createRecordsJSON() {
		File json = new File(jsonPath);
		if(!json.getParentFile().exists()) { json.getParentFile().mkdirs(); }
		records.toJSON_File(jsonPath);
	}
	
	private ExperimentalRecords getRecordsSubset(String[] cas) {
		ExperimentalRecords subsetRecords = new ExperimentalRecords();
		for (ExperimentalRecord rec:records) {
			String casCheck="";
			if (rec.casrn!=null) { casCheck = rec.casrn; }
			boolean inSubset = false;
			int i = 0;
			while (!inSubset && i < cas.length) {
				if (casCheck.contains(cas[i])) { inSubset = true; }
				i++;
			}
			if (inSubset) { subsetRecords.add(rec); }
		}
		return subsetRecords;
	}
	
	public void createRecordsSubsetJSON(String[] cas,String filename) {
		String path = mainFolder+File.separator+filename;
		File json = new File(path);
		if(!json.getParentFile().exists()) { json.getParentFile().mkdirs(); }
		ExperimentalRecords subsetRecords = getRecordsSubset(cas);
		subsetRecords.toJSON_File(path);
	}
	
	public void createRecordsSubsetExcel(String[] cas,String filename) {
		String path = mainFolder+File.separator+filename;
		File json = new File(path);
		if(!json.getParentFile().exists()) { json.getParentFile().mkdirs(); }
		ExperimentalRecords subsetRecords = getRecordsSubset(cas);
		subsetRecords.toExcel_File(path);
	}
	
	private void addExperimentalRecordsToDatabase(ExperimentalRecords records) {
		String[] fieldNames = null;
		
		String[] outputFieldNames =ExperimentalRecord.outputFieldNames.toArray(new String[ExperimentalRecord.outputFieldNames.size()]);

		if (Arrays.asList(sources).contains(ExperimentalConstants.strSourceEChemPortalAPI) && recordType.toLowerCase().contains("tox")) {
			String[] fr_id = {"fr_id"};
			fieldNames = ArrayUtils.addAll(fr_id, outputFieldNames);
		} else {
			fieldNames = outputFieldNames;
		}
		String tableName = ExperimentalRecords.tableName;
		System.out.println("Creating ExperimentalRecords table using dbpath="+databasePath+" with fields:\n"+String.join("\n",fieldNames));
		try {
			Connection conn= SQLite_Utilities.getConnection(databasePath);
			Statement stat = SQLite_Utilities.getStatement(conn);			
			conn.setAutoCommit(true);		
			stat.executeUpdate("drop table if exists "+tableName+";");
			stat.executeUpdate("VACUUM;");
			
			SQLite_CreateTable.create_table_key_with_duplicates(stat, tableName, fieldNames,"casrn");
			conn.setAutoCommit(false);

			String s = "insert into " + tableName + " values (";

			for (int i = 1; i <= fieldNames.length; i++) {
				s += "?";
				if (i < fieldNames.length)
					s += ",";
			}
			s += ");";

			int counter = 0;
			int batchCounter = 0;
			PreparedStatement prep = conn.prepareStatement(s);
			
			
			for (ExperimentalRecord rec:records) {				
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
			
			try {
				String sqlAddIndex="CREATE INDEX idx_casrn ON "+tableName+" (casrn)";
				stat.executeUpdate(sqlAddIndex);
			} catch (Exception ex_idx_exists) {
				// Throws exception if index already exists
				// We don't care
			}
			
			System.out.println("Added "+counter+" entries to ExperimentalRecords table. Done!");

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	public void addFinalRecordsTableToDatabase(FinalRecords records) {
		String[] fieldNames = FinalRecord.outputFieldNames;
		String tableName = "FinalRecords";
		String finalRecordFilePath = mainFolder+File.separator+"eChemPortalAPI"+File.separator+"eChemPortalAPI Toxicity Original Records.json";
		try {
			Connection conn= SQLite_Utilities.getConnection(databasePath);
			Statement stat = SQLite_Utilities.getStatement(conn);			
			conn.setAutoCommit(true);		
			stat.executeUpdate("drop table if exists "+tableName+";");
			stat.executeUpdate("VACUUM;");
			
			SQLite_CreateTable.create_table_key_with_duplicates(stat, tableName, fieldNames,"number");
			conn.setAutoCommit(false);

			String s = "insert into " + tableName + " values (";

			for (int i = 1; i <= fieldNames.length; i++) {
				s += "?";
				if (i < fieldNames.length)
					s += ",";
			}
			s += ");";

			int counter = 0;
			int batchCounter = 0;
			PreparedStatement prep = conn.prepareStatement(s);
			
//			FinalRecords records = FinalRecords.loadFromJSON(finalRecordFilePath);
			for (FinalRecord rec:records) {				
				counter++;
//				rec.id_physchem=counter;
				
//				String[] list = rec.toStringArray(ExperimentalRecord.outputFieldNames);
				if (counter%50000==0) System.out.println("Added "+counter+" entries to FinalRecords...");
				
				String[] list = rec.toStringArray(fieldNames);

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
						
			try {
				String sqlAddIndex="CREATE INDEX idx_number ON "+tableName+" (number)";
				stat.executeUpdate(sqlAddIndex);
			} catch (Exception ex_idx_exists) {
				// Throws exception if index already exists
				// We don't care
			}
			
			System.out.println("Added "+counter+" entries to FinalRecords table. Done!");

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	private HashSet<List<String>> getUniqueIdentifiers(String sourceName) {
		HashSet<List<String>> unique = new HashSet<List<String>>();
		for (ExperimentalRecord er:records) {
			if (er.source_name.equals(sourceName)) {
				String cleanCAS = er.casrn==null ? null : er.casrn.trim();
				String cleanEINECS = er.einecs==null ? null : er.einecs.trim();
				String cleanName = er.chemical_name==null ? null : er.chemical_name.trim();
				String[] newID = { cleanCAS, cleanEINECS, cleanName };
				unique.add(Arrays.asList(newID));
			}
		}
		Vector<List<String>> toRemove = new Vector<List<String>>();
		for (List<String> id:unique) {
			if (id.get(2)!=null && !id.get(2).isBlank()) {
				String[] noNameID = { id.get(0), id.get(1), "" };
				String[] nullNameID = { id.get(0), id.get(1), null };
				toRemove.add(Arrays.asList(noNameID));
				toRemove.add(Arrays.asList(nullNameID));
			}
		}
		for (List<String> id:toRemove) { unique.remove(id); }
		return unique;
	}
	
	private Hashtable<String,String[]> getECInventory() {
		String filepath = "Data" + File.separator + "ECinventory.xlsx";
		Hashtable<String,String[]> inventory = new Hashtable<String,String[]>();
		try {
			InputStream fis = new FileInputStream(filepath);
			Workbook wb = WorkbookFactory.create(fis);
		    Sheet sheet = wb.getSheetAt(0);
		    for (Row row:sheet) {
//		    	for (Cell cell:row) { cell.setCellType(Cell.CELL_TYPE_STRING); }
		    	String cas = row.getCell(3).getStringCellValue();
		    	String einecs = row.getCell(2).getStringCellValue();
		    	String name = row.getCell(1).getStringCellValue();
		    	String[] value = {cas,name};
		    	inventory.put(einecs,value);
		    }
		    wb.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return inventory;
	}
	
	public void createUniqueIdentifiersExcel(String sourceName) {
		HashSet<List<String>> unique = getUniqueIdentifiers(sourceName);
		Hashtable<String,String[]> inventory = getECInventory();
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("Identifiers");
		Row subtotalRow = sheet.createRow(0);
		Row headerRow = sheet.createRow(1);
		String[] headers = {"casrn","einecs","chemical_name"};
		CellStyle style = wb.createCellStyle();
		Font font = wb.createFont();
		font.setBold(true);
		
		style.setFont(font);
		for (int i = 0; i < headers.length; i++) {
			Cell recCell = headerRow.createCell(i);
			recCell.setCellValue(headers[i]);
			recCell.setCellStyle(style);
		}
		int currentRow = 2;
		for (List<String> id:unique) {
			Row row = sheet.createRow(currentRow);
			if ((id.get(0)==null || id.get(0).isBlank()) && (id.get(2)==null || id.get(2).isBlank())) {
				String[] match = inventory.get(id.get(1));
				if (match!=null) {
					id.set(2, match[1]);
					if (!match[0].trim().equals("-")) {
						id.set(0, match[0]);
					}
				}
			}
			for (int i = 0; i < 3; i++) {
				row.createCell(i).setCellValue(id.get(i));
			}
			currentRow++;
		}
		for (int i = 0; i < headers.length; i++) {
			String col = CellReference.convertNumToColString(i);
			String subtotal = "SUBTOTAL(3,"+col+"$3:"+col+"$"+(currentRow+1)+")";
			subtotalRow.createCell(i).setCellFormula(subtotal);
		}
		sheet.setAutoFilter(CellRangeAddress.valueOf("A2:C"+currentRow));
		try {
			OutputStream fos = new FileOutputStream(mainFolder + File.separator + sourceName + " Unique Identifiers.xlsx");
			wb.write(fos);
			wb.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
