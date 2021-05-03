package gov.epa.exp_data_gathering.parse.ECHADossier;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public String headerName;
	public String headerECNumber;
	public String headerCASNumber;
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
	
	public static final String[] headers = { "Header Name", "Header EC Number", "Header CAS Number", "Details on test material", "Test material form",
			"Molecular formula", "Remarks", "EC Number", "EC Name", "Cas Number", "IUPAC Name", "Reference substance name" };
	
	private static Pattern ecNumberPattern = Pattern.compile("[0-9]{3}-[0-9]{3}-[0-9]");
	private static Pattern casNumberPattern = Pattern.compile("[0-9]+-[0-9]{2}-[0-9]");
	
	public String[] values() {
		String[] values = new String[headers.length];
		values[0] = headerName;
		values[1] = headerECNumber;
		values[2] = headerCASNumber;
		values[3] = detailsOnTestMaterial;
		values[4] = testMaterialForm;
		
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
		
		values[5] = molecularFormula.toString();
		values[6] = remarks.toString();
		values[7] = ecNumber.toString();
		values[8] = ecName.toString();
		values[9] = casNumber.toString();
		values[10] = iupacName.toString();
		values[11] = referenceSubstanceName.toString();
		
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
				String headerDiv = rs.getString("header");
				String testMaterialDiv = rs.getString("content");
				
				Document headerDoc = Jsoup.parse(headerDiv);
				Document testMaterialDoc = Jsoup.parse(testMaterialDiv);
				
				RecordECHADossier rec = new RecordECHADossier();
				String url = rs.getString("url");
				rec.url = url;
				
				parseHeaderDoc(headerDoc, rec);
				parseTestMaterialDoc(testMaterialDoc, rec);
				
				records.add(rec);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return records;
	}
	
	private static void parseHeaderDoc(Document headerDoc, RecordECHADossier rec) {
		rec.headerName = headerDoc.selectFirst("h1").text();
		
		String headerNumbers = headerDoc.selectFirst("div#SubstanceDescription").text();
		System.out.println(headerNumbers);
		Matcher ecNumberMatcher = ecNumberPattern.matcher(headerNumbers);
		Matcher casNumberMatcher = casNumberPattern.matcher(headerNumbers);
		
		if (ecNumberMatcher.find()) {
			rec.headerECNumber = ecNumberMatcher.group();
		}
		
		if (casNumberMatcher.find()) {
			rec.headerCASNumber = casNumberMatcher.group();
		}
	}
	
	private static void parseTestMaterialDoc(Document testMaterialDoc, RecordECHADossier rec) {
		Elements testDLs = testMaterialDoc.select("dl");
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
		Elements sBlocks = testMaterialDoc.select("div.sBlock");
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
	
	public static void addHeadersToExistingDatabase() {
		Connection conn= SQLite_Utilities.getConnection(databasePath);
		try (Statement stat = conn.createStatement()) {
			String query = "select url from " + sourceName + " WHERE header IS NULL;";
			ResultSet rs = stat.executeQuery(query);
			
			Random rand = new Random();
			int count = 0;
			while (rs.next()) {
				long delay = 0;
				String url = rs.getString("url");
				long start = System.currentTimeMillis();
				Document doc = Jsoup.connect(url).get();
				long end = System.currentTimeMillis();
				delay = end - start;
			
				String header = doc.selectFirst("div#Header").outerHtml();
				String safeHeader = (header==null || header.isBlank()) ? null : header.replaceAll("'", "''").replaceAll(";", "\\;");
				if (safeHeader!=null) {
					String updateHeader = "UPDATE " + sourceName + " SET header = '" + safeHeader + "' WHERE url = '" + url + "';";
					try (Statement stat2 = conn.createStatement()) {
						stat2.execute(updateHeader);
					} catch (Exception e1) {
						e1.printStackTrace();
						System.out.println(updateHeader);
					}
				}
				
				count++;
				if (count % 100 == 0) { System.out.println("Made " + count + " updates..."); }
				
				Thread.sleep((long) (delay*(1+rand.nextDouble())));
			}
			System.out.println("Updated " + count + " records!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String filePath = sourceFolder + "/SkinSensitizationLLNA_records_DossierChecked.xlsx";
//		downloadWebpagesFromExcelToDatabase(filePath, 4, true, true);
		
		List<RecordECHADossier> records = parseWebpagesInDatabase();
		HashMap<String, RecordECHADossier> hm = mapRecords(records);
		addRecordsToExcel(hm, 4, 5, filePath);
		
//		addHeadersToExistingDatabase();
	}
}
