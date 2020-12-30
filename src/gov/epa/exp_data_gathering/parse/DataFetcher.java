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

import gov.epa.QSAR.DataSetCreation.ConvertExperimentalRecordsToDataSet;
import gov.epa.ghs_data_gathering.Database.MySQL_DB;

public class DataFetcher {
	
	private static ExperimentalRecords records;
	
	public static final String mainFolder = "Data"+File.separator+"Experimental";
	public static final String databasePath = mainFolder+File.separator+"ExperimentalRecords.db";
	public static final String jsonPath = mainFolder+File.separator+"ExperimentalRecords.json";
	
	public DataFetcher(String[] sources) {
		records = new ExperimentalRecords();
		for (String source:sources) {
			String recordFilePath = mainFolder+File.separator+source+" Experimental Records.json";
			String badRecordFilePath = mainFolder+File.separator+source+" Experimental Records-Bad.json";
			
			File fileRecords=new File(recordFilePath);
			File fileBadRecords=new File(badRecordFilePath);
			
			ExperimentalRecords sourceRecords=null;
			
			if (fileRecords.exists()) {
				sourceRecords = ExperimentalRecords.loadFromJSON(recordFilePath);	
			}
			
			if (fileBadRecords.exists()) {
				ExperimentalRecords badSourceRecords = ExperimentalRecords.loadFromJSON(badRecordFilePath);				
				sourceRecords.addAll(badSourceRecords);
			}

//			if (sourceRecords==null) {
			//TMM- there doesnt seem to be any numbered files...
//				sourceRecords = getRecordsFromNumberedFiles(source);
//			}
			
			if (sourceRecords==null) {
				System.out.println("No file for "+source.substring(source.lastIndexOf("\\")+1));
				continue;
			}
			
			System.out.println("Fetching data from "+source.substring(source.lastIndexOf("\\")+1));

			addSourceBasedIDNumbers(sourceRecords);			
			records.addAll(sourceRecords);
				
			
			
			
		}
	}

	private void addSourceBasedIDNumbers(ExperimentalRecords records) {
		for (int i=0;i<records.size();i++) {
			ExperimentalRecord record=records.get(i);
			record.id_physchem=record.source_name+(i+1);
		}
	}
	

	
	
	private ExperimentalRecords getRecordsFromNumberedFiles(String source) {
		ExperimentalRecords sourceRecords;
		sourceRecords = new ExperimentalRecords();
		int i = 1;
		ExperimentalRecords temp = new ExperimentalRecords();
		
		while (temp!=null) {
			File ftemp=new File(mainFolder+File.separator+source+" Experimental Records "+i+".json");					
			System.out.println(ftemp.getName()+"\t"+ftemp.exists());						
			temp = ExperimentalRecords.loadFromJSON(mainFolder+File.separator+source+" Experimental Records "+i+".json");
			if (temp==null) break;
			sourceRecords.addAll(temp);
			i++;
		}
		return sourceRecords;
	}
	
	
	public void createExperimentalRecordsDatabase() {
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		makeDatabase(records);
	}
	
	public void createExperimentalRecordsJSON() {
		File json = new File(jsonPath);
		if(!json.getParentFile().exists()) { json.getParentFile().mkdirs(); }
		records.toJSON_File(jsonPath);
	}
	
	private ExperimentalRecords getExperimentalRecordsSubset(String[] cas) {
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
	
	public void createExperimentalRecordsSubsetJSON(String[] cas,String filename) {
		String path = mainFolder+File.separator+filename;
		File json = new File(path);
		if(!json.getParentFile().exists()) { json.getParentFile().mkdirs(); }
		ExperimentalRecords subsetRecords = getExperimentalRecordsSubset(cas);
		subsetRecords.toJSON_File(path);
	}
	
	public void createExperimentalRecordsSubsetExcel(String[] cas,String filename) {
		String path = mainFolder+File.separator+filename;
		File json = new File(path);
		if(!json.getParentFile().exists()) { json.getParentFile().mkdirs(); }
		ExperimentalRecords subsetRecords = getExperimentalRecordsSubset(cas);
		subsetRecords.toExcel_File(path);
	}
	
	private void makeDatabase(ExperimentalRecords records) {
		String[] fieldNames = ExperimentalRecord.outputFieldNames;
		String tableName = "records";
		System.out.println("Creating database at "+databasePath+" with fields:\n"+String.join("\n",fieldNames));
		try {
			Connection conn= MySQL_DB.getConnection(databasePath);
			Statement stat = MySQL_DB.getStatement(conn);			
			conn.setAutoCommit(true);		
			stat.executeUpdate("drop table if exists "+tableName+";");
			stat.executeUpdate("VACUUM;");
			
			MySQL_DB.create_table_key_with_duplicates(stat, tableName, fieldNames,"casrn");
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
						
			String sqlAddIndex="CREATE INDEX idx_casrn ON "+tableName+" (casrn)";
			stat.executeUpdate(sqlAddIndex);
			
			System.out.println("Created database with "+counter+" entries. Done!");

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
		    	for (Cell cell:row) { cell.setCellType(Cell.CELL_TYPE_STRING); }
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
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
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
	
	public static void main(String[] args) {

		String[] sources = {"eChemPortalAPI\\eChemPortalAPI","LookChem\\LookChem PFAS\\LookChem","PubChem\\PubChem","OChem\\OChem","OFMPub\\OFMPub","QSARDB\\QSARDB",
				"Bradley\\Bradley","ADDoPT\\ADDoPT","AqSolDB\\AqSolDB",
				"Sander\\General\\Sander","ChemicalBook\\PFAS\\ChemicalBook","ChemIDplus\\ChemIDplus"};

		DataFetcher d = new DataFetcher(sources);
		d.createExperimentalRecordsDatabase();
		d.createExperimentalRecordsJSON();
//		String[] cas = {"335-76-2","3108-42-7","3830-45-3","375-95-1","4149-60-4","307-24-4","355-46-4","3871-99-6","375-22-4","10495-86-0"};
//		d.createExperimentalRecordsSubsetJSON(cas, "ExperimentalRecords_CPHEA_120220.json");
//		d.createExperimentalRecordsSubsetExcel(cas, "ExperimentalRecords_CPHEA_120220.xlsx");
//		d.createUniqueIdentifiersExcel("eChemPortal");
	}
}
