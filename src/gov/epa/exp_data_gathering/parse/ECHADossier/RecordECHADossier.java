package gov.epa.exp_data_gathering.parse.ECHADossier;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
import gov.epa.exp_data_gathering.parse.DownloadWebpageUtilities;
import gov.epa.ghs_data_gathering.Utilities.MoreFileUtilities;

public class RecordECHADossier {
	String url;
	public String testMaterialForm;
	public String detailsOnTestMaterial;
	List<RecordECHAConstituent> constituents;
	
	public static class RecordECHAConstituent {
		public String molecularFormula;
		public String remarks;
		public String ecNumber;
		public String ecName;
		public String casNumber;
		public String iupacName;
		public String referenceSubstanceName;
	}
	
	public static final String sourceName = "ECHADossier";
	public static final String sourceFolder = "data/experimental/" + sourceName;
	public static final String databasePath = sourceFolder + "/" + sourceName + "_raw_html.db";
	
	public static final String[] headers = { "Details on test material", "Test material form", "Molecular formula", "Remarks",
			"EC Number", "EC Name", "Cas Number", "IUPAC Name", "Reference substance name" };
	
	public String[] values() {
		String[] values = new String[headers.length];
		values[0] = detailsOnTestMaterial;
		values[1] = testMaterialForm;
		
		StringJoiner molecularFormula = new StringJoiner("|");
		StringJoiner remarks = new StringJoiner("|");
		StringJoiner ecNumber = new StringJoiner("|");
		StringJoiner ecName = new StringJoiner("|");
		StringJoiner casNumber = new StringJoiner("|");
		StringJoiner iupacName = new StringJoiner("|");
		StringJoiner referenceSubstanceName = new StringJoiner("|");
		
		for (RecordECHAConstituent constituent:constituents) {
			if (constituent.molecularFormula!=null && !constituent.molecularFormula.isBlank()) { molecularFormula.add(constituent.molecularFormula); }
			if (constituent.remarks!=null && !constituent.remarks.isBlank()) { remarks.add(constituent.remarks); }
			if (constituent.ecNumber!=null && !constituent.ecNumber.isBlank()) { ecNumber.add(constituent.ecNumber); }
			if (constituent.ecName!=null && !constituent.ecName.isBlank()) { ecName.add(constituent.ecName); }
			if (constituent.casNumber!=null && !constituent.casNumber.isBlank()) { casNumber.add(constituent.casNumber); }
			if (constituent.iupacName!=null && !constituent.iupacName.isBlank()) { iupacName.add(constituent.iupacName); }
			if (constituent.referenceSubstanceName!=null && 
					!constituent.referenceSubstanceName.isBlank()) { referenceSubstanceName.add(constituent.referenceSubstanceName); }
		}
		
		values[2] = molecularFormula.toString();
		values[3] = remarks.toString();
		values[4] = ecNumber.toString();
		values[5] = ecName.toString();
		values[6] = casNumber.toString();
		values[7] = iupacName.toString();
		values[8] = referenceSubstanceName.toString();
		
		return values;
	}
	
	RecordECHADossier() {
		constituents = new ArrayList<RecordECHAConstituent>();
	}

	public static void downloadWebpagesFromExcelToDatabase(String filePath,int col,boolean hasHeader,boolean startFresh) {
		List<String> urls = MoreFileUtilities.getListFromFile(filePath, 0, col, hasHeader);
		DownloadWebpageUtilities.downloadWebpagesToDatabaseAdaptive(new Vector<String>(urls),"div#collapseReference1CollapseGroup0",databasePath,sourceName,startFresh);		
	}
	
	public static List<RecordECHADossier> parseWebpagesInDatabase() {
		System.out.println("Parsing webpages in "+databasePath+"...");

		List<RecordECHADossier> records = new ArrayList<RecordECHADossier>();
		try {
			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = SQLite_GetRecords.getAllRecords(stat, sourceName);

			while (rs.next()) {
				String html = rs.getString("content");
				String url = rs.getString("url");
				Document doc = Jsoup.parse(html);
				
				RecordECHADossier rec = new RecordECHADossier();
				rec.url = url;
				
				Elements testDLs = doc.select("dl");
				for (Element testDL:testDLs) {
					Elements testDTs = testDL.select("dt");
					for (Element testDT:testDTs) {
						String header = testDT.text().trim();
						String content = testDT.nextElementSibling().text().trim();
						switch (header) {
						case "Test material form:":
							rec.testMaterialForm = content;
							break;
						case "Details on test material:":
							rec.detailsOnTestMaterial = content;
							break;
						}
					}
				}
				Elements sBlocks = doc.select("div.sBlock");
				for (Element sBlock:sBlocks) {
					RecordECHAConstituent constituent = new RecordECHAConstituent();
					Elements constituentDLs = sBlock.select("dl");
					for (Element constituentDL:constituentDLs) {
						Elements constituentDTs = constituentDL.select("dt");
						for (Element constituentDT:constituentDTs) {
							String header = constituentDT.text().trim();
							String content = constituentDT.nextElementSibling().text().trim();
							switch (header) {
							case "Molecular formula:":
								constituent.molecularFormula = content;
								break;
							case "Remarks:":
								constituent.remarks = content;
								break;
							case "EC Number:":
								constituent.ecNumber = content;
								break;
							case "EC Name:":
								constituent.ecName = content;
								break;
							case "Cas Number:":
								constituent.casNumber = content;
								break;
							case "IUPAC Name:":
								constituent.iupacName = content;
								break;
							case "Reference substance name:":
								constituent.referenceSubstanceName = content;
								break;
							default:
								System.out.println("Unhandled header: " + header + " with content: " + content);
								break;
							}
						}
					}
					rec.constituents.add(constituent);
				}
				records.add(rec);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return records;
	}
	
	public static HashMap<String, RecordECHADossier> mapRecords(List<RecordECHADossier> records) {
		HashMap<String, RecordECHADossier> hm = new HashMap<String, RecordECHADossier>();
		for (RecordECHADossier rec:records) {
			hm.put(rec.url, rec);
		}
		return hm;
	}
	
	public static void addRecordsToExcel(HashMap<String, RecordECHADossier> hm, int urlCol, int firstEmptyCol, String filePath) {
		try (InputStream inp = new FileInputStream(filePath)) {
		    Workbook wb = WorkbookFactory.create(inp);
		    Sheet sheet = wb.getSheetAt(0);
		    int currentRow = 0;
		    for (Row row:sheet) {
		    	if (currentRow==0) {
		    		String[] headers = RecordECHADossier.headers;
		    		for (int i = 0; i < headers.length; i++) {
		    			row.createCell(i + firstEmptyCol).setCellValue(headers[i]);
		    		}
		    		currentRow++;
		    		continue;
		    	}
		    	
		    	Cell urlCell = row.getCell(urlCol);
		    	String url = urlCell.getStringCellValue();
		    	RecordECHADossier rec = hm.get(url);
		    	
		    	if (rec==null) {
		    		currentRow++;
		    		continue;
		    	}
		    	
		    	String[] values = rec.values();
	    		for (int i = 0; i < values.length; i++) {
	    			row.createCell(i + firstEmptyCol).setCellValue(values[i]);
	    		}
		    }
		    
		    try (OutputStream os = new FileOutputStream(filePath)) {
		        wb.write(os);
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String filePath =  sourceFolder + "/SkinSensitizationLLNA_records_DossierChecked.xlsx";
//		downloadWebpagesFromExcelToDatabase(filePath, 7, true, true);
		
		List<RecordECHADossier> records = parseWebpagesInDatabase();
		HashMap<String, RecordECHADossier> hm = mapRecords(records);
		addRecordsToExcel(hm, 5, 6, filePath);
	}
}
