package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;
import java.util.Random;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.api.RawDataRecord;
import gov.epa.ghs_data_gathering.Database.CreateGHS_Database;
import gov.epa.ghs_data_gathering.GetData.RecordDashboard;
import gov.epa.ghs_data_gathering.Utilities.FileUtilities;

public class Parse {
	
	public String sourceName;
	public String jsonFolder;
	public String databaseFolder;
	public String webpageFolder;

	protected String fileNameSourceExcel;//input excel spreadsheet
	protected String fileNameHtmlZip;//input as zip file of webpages
	protected String fileNameSourceText;
	protected String folderNameWebpages;//input as folder of webpages
	protected String folderNameExcel;
	
	protected String fileNameJSON_Records;//records in original format
	
	protected String fileNameFlatExperimentalRecords;//records in flat format
	protected String fileNameJsonExperimentalRecords;//records in ExperimentalRecord class format
	protected String fileNameExcelExperimentalRecords;
	protected String fileNameFlatExperimentalRecordsBad;
	protected String fileNameJsonExperimentalRecordsBad;
	protected String mainFolder;
	
	public static boolean generateOriginalJSONRecords=true; //runs code to generate json records from original data format (json file has all the chemicals in one file)	
	public static boolean writeFlatFile=false;//all data converted to final format stored as flat text file
	public static boolean writeJsonExperimentalRecordsFile=true;//all data converted to final format stored as Json file
	public static boolean writeExcelExperimentalRecordsFile=true;//all data converted to final format stored as xlsx file
	
	Gson gson=null;

	public void init() {
		fileNameJSON_Records = sourceName +" Original Records.json";
		fileNameFlatExperimentalRecords = sourceName +" Experimental Records.txt";
		fileNameFlatExperimentalRecordsBad = sourceName +" Experimental Records-Bad.txt";
		fileNameJsonExperimentalRecords = sourceName +" Experimental Records.json";
		fileNameJsonExperimentalRecordsBad = sourceName +" Experimental Records-Bad.json";
		fileNameExcelExperimentalRecords = sourceName +" Experimental Records.xlsx";
		mainFolder = "Data" + File.separator + "Experimental" + File.separator + sourceName;
		databaseFolder = mainFolder;
		jsonFolder= mainFolder;
		webpageFolder = mainFolder + File.separator + "web pages";
		folderNameExcel=mainFolder + File.separator + "excel files";
		
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting().disableHtmlEscaping();
		gson = builder.create();
	}
	
	/**
	 * Stores the content from a list of URLs in timestamped records in a RawDataRecord database
	 * Crawl delay is adaptive: Random multiplier of 1-2 times the time taken to load the page (~1-2 seconds on LookChem)
	 * @param urls			The URLs to be downloaded
	 * @param tableName		The name of the table to store the data in, i.e., the source name
	 * @param startFresh	True to remake database table completely, false to append new records to existing table
	 */
	public void downloadWebpagesToDatabaseAdaptive(Vector<String> urls,String tableName, boolean startFresh) {
		String databasePath = databaseFolder+File.separator+sourceName+"_raw_html.db";
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		
		java.sql.Connection conn=CreateGHS_Database.createDatabaseTable(databasePath, tableName, RawDataRecord.fieldNames, startFresh);
		Random rand = new Random();
		
		try {
			int counter = 0;
			for (int i = 0; i < urls.size(); i++) {
				String url = urls.get(i);
				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
				Date date = new Date();  
				String strDate=formatter.format(date);
				
				RawDataRecord rec=new RawDataRecord(strDate, url, "");
				boolean haveRecord=rec.haveRecordInDatabase(databasePath,tableName,conn);
				if (!haveRecord || startFresh) {
					long delay = 0;
					try {
						long startTime=System.currentTimeMillis();
						rec.content=FileUtilities.getText_UTF8(url).replaceAll("'", "\'"); //single quotes mess with the SQL insert later
						long endTime=System.currentTimeMillis();
						delay = endTime-startTime;
						rec.addRecordToDatabase(tableName, conn);
						counter++;
						if (counter % 100==0) { System.out.println("Downloaded "+counter+" pages"); }
					} catch (Exception ex) {
						System.out.println("Failed to download "+url);
					}
					Thread.sleep((long) (delay*(1+rand.nextDouble())));
				}
			}
			
			System.out.println("Downloaded "+counter+" pages");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void downloadWebpagesToDatabaseAdaptiveNonUnicode(Vector<String> urls,String tableName, boolean startFresh) {
		String databasePath = databaseFolder+File.separator+sourceName+"_raw_html.db";
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		
		java.sql.Connection conn=CreateGHS_Database.createDatabaseTable(databasePath, tableName, RawDataRecord.fieldNames, startFresh);
		Random rand = new Random();
		
		try {
			int counter = 0;
			for (int i = 0; i < urls.size(); i++) {
				String url = urls.get(i);
				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
				Date date = new Date();  
				String strDate=formatter.format(date);
				
				RawDataRecord rec=new RawDataRecord(strDate, url, "");
				boolean haveRecord=rec.haveRecordInDatabase(databasePath,tableName,conn);
				if (!haveRecord || startFresh) {
					long delay = 0;
					try {
						long startTime=System.currentTimeMillis();
						rec.content=FileUtilities.getText(url).replaceAll("'", "\'"); //single quotes mess with the SQL insert later
						long endTime=System.currentTimeMillis();
						delay = endTime-startTime;
						rec.addRecordToDatabase(tableName, conn);
						counter++;
						if (counter % 100==0) { System.out.println("Downloaded "+counter+" pages"); }
					} catch (Exception ex) {
						System.out.println("Failed to download "+url);
					}
					Thread.sleep((long) (delay*(1+rand.nextDouble())));
				}
			}
			
			System.out.println("Downloaded "+counter+" pages");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Stores HTML excerpts from a list of URLs in timestamped records in the raw HTML database
	 * @param urls			The URLs to be downloaded
	 * @param htmlClass		The class for the elements to be excerpted and stored
	 * @param tableName		The name of the table to store the data in, i.e., the source name
	 * @param startFresh	True to remake database table completely, false to append new records to existing table
	 */
	public void downloadWebpagesToDatabaseAdaptive(Vector<String> urls,String css,String tableName,boolean startFresh) {
		String databasePath = databaseFolder+File.separator+sourceName+"_raw_html.db";
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		
		java.sql.Connection conn=CreateGHS_Database.createDatabaseTable(databasePath, tableName, RawDataRecord.fieldNames, startFresh);
		Random rand = new Random();
		
		try {
			long totalStartTime=System.currentTimeMillis();
			int counter = 0;
			for (String url:urls) {
				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
				Date date = new Date();  
				String strDate=formatter.format(date);
				
				RawDataRecord rec=new RawDataRecord(strDate, url, "");
				boolean haveRecord=rec.haveRecordInDatabase(databasePath,tableName,conn);
				if (!haveRecord || startFresh) {
					String html="";
					long delay=0;
					try {
						long startTime=System.currentTimeMillis();
						html=FileUtilities.getText_UTF8(url).replaceAll("'", "\'"); //single quotes mess with the SQL insert later
						long endTime=System.currentTimeMillis();
						delay=endTime-startTime;
						Document doc = Jsoup.parse(html);
						Element table=doc.select(css).first();
						if (table!=null) {
							rec.content=table.outerHtml();
							rec.addRecordToDatabase(tableName, conn);
							counter++;
							if (counter % 100==0) {
								long batchTime = System.currentTimeMillis();
								System.out.println("Downloaded "+counter+" pages in "+(batchTime-totalStartTime)/1000+" seconds");
							}
						} else { System.out.println("No data table at "+url); }
					} catch (Exception ex) {
						System.out.println("Failed to download "+url);
					}
					Thread.sleep((long) (delay*(1+rand.nextDouble())));
				}
			}
			
			System.out.println("Downloaded "+counter+" pages");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stores the content from a list of URLs in timestamped records in a RawDataRecord database
	 * Constant crawl delay (200 ms for PubChem, per stated 5 requests/sec policy)
	 * @param urls			The URLs to be downloaded
	 * @param tableName		The name of the table to store the data in, i.e., the source name
	 * @param startFresh	True to remake database table completely, false to append new records to existing table
	 */
	public void downloadWebpagesToDatabase(Vector<String> urls,String databaseName,String tableName, boolean startFresh) {
		String databasePath = databaseFolder+File.separator+databaseName;
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		
		java.sql.Connection conn=CreateGHS_Database.createDatabaseTable(databasePath, tableName, RawDataRecord.fieldNames, startFresh);
		
		try {
			int counterSuccess = 0;
			int counterTotal = 0;
			for (String url:urls) {
				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
				Date date = new Date();  
				String strDate=formatter.format(date);
				
				RawDataRecord rec=new RawDataRecord(strDate, url, "");
				boolean haveRecord=rec.haveRecordInDatabase(databasePath,tableName,conn);
				if (!haveRecord || startFresh) {
					try {
						rec.content=FileUtilities.getText_UTF8(url).replaceAll("'", "\'"); //single quotes mess with the SQL insert later
						if (rec.content!=null) { 
							rec.addRecordToDatabase(tableName, conn);
							counterSuccess++;
						}
						counterTotal++;
						if (counterTotal % 100==0) { System.out.println("Attempted "+counterTotal+" pages, downloaded "+counterSuccess+" pages"); }
					} catch (Exception ex) {
						System.out.println("Failed to download "+url);
					}
					Thread.sleep(200);
				}
			}
			System.out.println("Attempted "+counterTotal+" pages, downloaded "+counterSuccess+" pages");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Downloads the HTML files from a list of URLs and saves them in a zip folder
	 * @param urls			The URLs to be downloaded
	 * @param sourceName	The source name
	 */
	public void downloadWebpagesToZipFile(Vector<String> urls) {
		String destZipFolder=webpageFolder+".zip";
		Random rand = new Random();

		try {
			int counter = 1;
			for (String url:urls) {
				String fileName = url.substring( url.lastIndexOf("/")+1, url.length() );
				String destFilePath = webpageFolder + File.separator + fileName;
				File destFile = new File(destFilePath);
				if(!destFile.getParentFile().exists()) { destFile.getParentFile().mkdirs(); }
	
				long delay = 0;
				try {
					long startTime=System.currentTimeMillis();
					FileUtilities.downloadFile(url, destFilePath);
					long endTime=System.currentTimeMillis();
					delay=endTime-startTime;
					counter++;
					if (counter % 100==0) { System.out.println("Downloaded "+counter+" pages"); }
				} catch (Exception ex) {
					System.out.println("Failed to download "+url);
				}
				Thread.sleep((long) (delay*(1+rand.nextDouble())));
			}
			System.out.println("Downloaded "+counter+" pages");

			FileOutputStream fos = new FileOutputStream(destZipFolder); 
			ZipOutputStream zipOS = new ZipOutputStream(fos); 
			
			File webpageFile=new File(webpageFolder);
			File[] files=webpageFile.listFiles();
			
			// Create a zip file
			for (File file:files) {
				if (file.getName().contains(".html")); {
					FileUtilities.writeToZipFile(file.getName(),webpageFolder,"web pages", zipOS);
				}
				// Deletes all files so folder can be deleted after zip finishes
				file.delete();
			}
			// Delete web pages folder
			webpageFile.delete();
			
			zipOS.close();
            fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Parses an Excel sheet downloaded from the CompTox dashboard to RecordDashboard objects
	 * @param filename	The name or path to the Excel file to be parsed
	 * @return			A vector of RecordDashboard objects containing the data from the Excel file
	 */
	public static Vector<RecordDashboard> getDashboardRecordsFromExcel(String filename) {
		Vector<RecordDashboard> records = new Vector<RecordDashboard>();
		
		try {
			Workbook wb = null;
			File file = new File(filename);
			FileInputStream fis = new FileInputStream(file);
			if (filename.endsWith(".xlsx")) {
				wb=new XSSFWorkbook(fis);
			} else if (filename.endsWith(".xls")) {
				wb=new HSSFWorkbook(fis);
			}
			Sheet sheet=wb.getSheetAt(0);
			
			for (Row row:sheet) {
				RecordDashboard temp = new RecordDashboard();
				for (Cell cell:row) {
					int col = cell.getColumnIndex();
					String field = sheet.getRow(0).getCell(col).getStringCellValue();
					if (RecordDashboard.getHeader().contains(field) && CellType.forInt(cell.getCellType()) == CellType.STRING) {
						String data = cell.getStringCellValue();
						temp.setValue(field,data);
					}
				}
				records.add(temp);
			}
			wb.close();
			fis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return records;
		
	}
	
	/**
	 * Need to override
	 */
	protected void createRecords() {
		System.out.println("Need to override createRecords()!");	
	}
	
	/**
	 * Need to override
	 * @return
	 */
	protected ExperimentalRecords goThroughOriginalRecords() {
		System.out.println("Need to override goThroughOriginalRecords()!");
		return null;
	}
	
	public void createFiles() {
		System.out.println("Creating " + sourceName + " json files...");
		
		if (generateOriginalJSONRecords) {
			if (fileNameSourceExcel!=null) {
				System.out.println("Parsing "+fileNameSourceExcel);
			} else if (folderNameWebpages!=null) {
				System.out.println("Parsing webpages in "+folderNameWebpages);
			} else if (folderNameExcel!=null) {
				System.out.println("Parsing excel files in "+folderNameExcel);
			} else {
				System.out.println("Parsing original file(s)");	
			}

			createRecords();
		}

		System.out.println("Going through original records");
		ExperimentalRecords records=goThroughOriginalRecords();
		ExperimentalRecords recordsBad = dumpBadRecords(records);

		if (writeFlatFile) {
			System.out.println("Writing flat file for chemical records");
			records.toFlatFile(mainFolder+File.separator+fileNameFlatExperimentalRecords,"|");
			recordsBad.toFlatFile(mainFolder+File.separator+fileNameFlatExperimentalRecordsBad,"|");
		}
		
		if (writeJsonExperimentalRecordsFile) {
			System.out.println("Writing json file for chemical records");
			records.toJSON_File(mainFolder+File.separator+fileNameJsonExperimentalRecords);
			recordsBad.toJSON_File(mainFolder+File.separator+fileNameJsonExperimentalRecordsBad);
		}
		
		if (writeExcelExperimentalRecordsFile) {
			System.out.println("Writing Excel file for chemical records");
			ExperimentalRecords merge = new ExperimentalRecords();
			merge.addAll(records);
			merge.addAll(recordsBad);
			if (merge.size() <= 100000) {
				merge.toExcel_File(mainFolder+File.separator+fileNameExcelExperimentalRecords);
			} else {
				ExperimentalRecords temp = new ExperimentalRecords();
				Iterator<ExperimentalRecord> it = merge.iterator();
				int i = 0;
				int batch = 0;
				while (it.hasNext()) {
					temp.add(it.next());
					i++;
					if (i!=0 && i%100000==0) {
						batch++;
						temp.toExcel_File(mainFolder+File.separator+sourceName +" Experimental Records "+batch+".xlsx");
						temp.removeAllElements();
					}
				}
				batch++;
				temp.toExcel_File(mainFolder+File.separator+sourceName +" Experimental Records "+batch+".xlsx");
			}
		}
		
		System.out.println("done\n");
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
	
	protected void writeOriginalRecordsToFile(Vector<?> records) {
		try {
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting().disableHtmlEscaping();
			Gson gson = builder.create();
			
			String jsonPath = jsonFolder + File.separator + fileNameJSON_Records;
			File file = new File(jsonPath);
			if(!file.getParentFile().exists()) { file.getParentFile().mkdirs(); }
			
			FileWriter fw = new FileWriter(jsonPath);
			String strRecords=gson.toJson(records);
			
			strRecords=Parse.fixChars(strRecords);
			
			fw.write(strRecords);
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	static boolean getNumericalValue(ExperimentalRecord er, String propertyValue, int unitsIndex, boolean badUnits) {
		if (badUnits) { unitsIndex = propertyValue.length(); }
		if (propertyValue.contains("±")) { unitsIndex = Math.min(propertyValue.indexOf("±"),unitsIndex); }
		boolean foundNumeric = false;
		if (!foundNumeric) {
			try {
				Matcher sciMatcher = Pattern.compile("([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]?(e|x10)[ ]?([-|\\+]?[ ]?[0-9]+)").matcher(propertyValue.toLowerCase().substring(0,unitsIndex));
				sciMatcher.find();
				String strMantissa = sciMatcher.group(1);
				String strMagnitude = sciMatcher.group(3);
				Double mantissa = Double.parseDouble(strMantissa.replaceAll("\\s",""));
				Double magnitude =  Double.parseDouble(strMagnitude.replaceAll("\\s","").replaceAll("\\+", ""));
				er.property_value_point_estimate_original = mantissa*Math.pow(10, magnitude);
				foundNumeric = true;
				int propertyValueIndex;
				if ((propertyValueIndex = propertyValue.indexOf(strMantissa)) > 0) {
					String checkSymbol = propertyValue.replaceAll("\\s","");
					er.property_value_numeric_qualifier = getNumericQualifier(checkSymbol,propertyValueIndex);
				}
			} catch (Exception ex) { }
		}
		
		if (!foundNumeric) {
			try {
				double[] range = Parse.extractFirstDoubleRangeFromString(propertyValue,unitsIndex);
				if (!badUnits) {
					er.property_value_min_original = range[0];
					er.property_value_max_original = range[1];
					foundNumeric = true;
				}
				if (propertyValue.contains("~") || propertyValue.contains("ca.")) {
					er.property_value_numeric_qualifier = "~";
				}
			} catch (Exception ex) { }
		}
		
		if (!foundNumeric) {
			try {
				double[] range = Parse.extractAltFormatRangeFromString(propertyValue,unitsIndex);
				if (!badUnits) {
					er.property_value_min_original = range[0];
					er.property_value_max_original = range[1];
					foundNumeric = true;
				}
				if (propertyValue.contains("~") || propertyValue.contains("ca.")) {
					er.property_value_numeric_qualifier = "~";
				}
			} catch (Exception ex) { }
		}
		
		if (!foundNumeric) {
			try {
				double propertyValueAsDouble = Parse.extractDoubleFromString(propertyValue,unitsIndex);
				int propertyValueIndex = -1;
				if (propertyValueAsDouble >= 0 && propertyValueAsDouble < 1) {
					propertyValueIndex = Math.min(propertyValue.replaceAll("\\s","").indexOf("0"),propertyValue.replaceAll("\\s","").indexOf("."));
				} else {
					propertyValueIndex = propertyValue.replaceAll("\\s","").indexOf(Double.toString(propertyValueAsDouble).charAt(0));
				}
				if (!badUnits) {
					er.property_value_point_estimate_original = propertyValueAsDouble;
					foundNumeric = true;
					if (propertyValueIndex > 0) {
						String checkSymbol = propertyValue.replaceAll("\\s","");
						er.property_value_numeric_qualifier = getNumericQualifier(checkSymbol,propertyValueIndex);
					}
				}
			} catch (Exception ex) { }
		}
		return foundNumeric;
	}
	
	static String getNumericQualifier(String str,int index) {
		String symbol = "";
		if (index > 0) {
			if (str.charAt(index-1)=='>') {
				symbol = ">";
			} else if (str.charAt(index-1)=='<') {
				symbol = "<";
			} else if (str.charAt(index-1)=='~' || str.contains("ca.") || str.contains("circa")) {
				symbol = "~";
			} else if (index > 1 && str.charAt(index-2)=='>' && str.charAt(index-1)=='=') {
				symbol = ">=";
			} else if (index > 1 && str.charAt(index-2)=='<' && str.charAt(index-1)=='=') {
				symbol = "<=";
			}
			
		}
		return symbol;
	}
	
	static boolean getDensity(ExperimentalRecord er, String propertyValue) {
		boolean badUnits = true;
		int unitsIndex = -1;
		propertyValue = propertyValue.replaceAll("([0-9]),([0-9])", "$1.$2");
		if (propertyValue.toLowerCase().contains("g/cm") || propertyValue.toLowerCase().contains("g/cu cm") || propertyValue.toLowerCase().contains("gm/cu cm")) {
			er.property_value_units_original = ExperimentalConstants.str_g_cm3;
			unitsIndex = propertyValue.toLowerCase().indexOf("g");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("g/ml")) {
			er.property_value_units_original = ExperimentalConstants.str_g_mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/m");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("kg/m")) {
			er.property_value_units_original = ExperimentalConstants.str_kg_m3;
			unitsIndex = propertyValue.toLowerCase().indexOf("kg/m");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("g/l")) {
			er.property_value_units_original = ExperimentalConstants.str_g_L;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/l");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("relative")) {
			unitsIndex = propertyValue.length();
			badUnits = false;
			if (propertyValue.toLowerCase().contains("mixture")) {
				er.updateNote(ExperimentalConstants.str_relative_mixture_density);
			} else if (propertyValue.toLowerCase().contains("gas")) {
				er.updateNote(ExperimentalConstants.str_relative_gas_density);
			} else {
				er.updateNote(ExperimentalConstants.str_relative_density);
			}
		} else {
			er.property_value_units_original = ExperimentalConstants.str_g_cm3;
			if (propertyValue.contains(":")) {
				unitsIndex = propertyValue.length();
			} else if (propertyValue.contains(" ")) {
				unitsIndex = propertyValue.indexOf(" ");
			} else {
				unitsIndex = propertyValue.length();
			}
			badUnits = false;
			er.updateNote(ExperimentalConstants.str_g_cm3+" assumed");
		}
		boolean foundNumeric = getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		return foundNumeric;
	}
	
	// Applicable for melting point, boiling point, and flash point
	static boolean getTemperatureProperty(ExperimentalRecord er,String propertyValue) {
		boolean badUnits = true;
		int unitsIndex = -1;
		String units = Parse.getTemperatureUnits(propertyValue);
		if (units.length()!=0) {
			er.property_value_units_original = units;
			unitsIndex = propertyValue.indexOf(units);
			badUnits = false;
		}
		boolean foundNumeric = getNumericalValue(er,propertyValue, unitsIndex,badUnits);
		return foundNumeric;
	}
	
	boolean getWaterSolubility(ExperimentalRecord er,String propertyValue) {
		boolean badUnits = true;
		int unitsIndex = -1;
		propertyValue = propertyValue.replaceAll("([0-9]),([0-9]{3})", "$1$2");
		if (propertyValue.toLowerCase().contains("mg/l")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_L;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mg/ml")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ug/ml") || propertyValue.toLowerCase().contains("µg/ml")) {
			er.property_value_units_original = ExperimentalConstants.str_ug_mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("ug/") == -1 ? propertyValue.toLowerCase().indexOf("µg/") : propertyValue.toLowerCase().indexOf("ug/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ug/l") || propertyValue.toLowerCase().contains("µg/l")) {
			er.property_value_units_original = ExperimentalConstants.str_ug_L;
			unitsIndex = propertyValue.toLowerCase().indexOf("ug/") == -1 ? propertyValue.toLowerCase().indexOf("µg/") : propertyValue.toLowerCase().indexOf("ug/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("g/ml")) {
			er.property_value_units_original = ExperimentalConstants.str_g_mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("g/l")) {
			er.property_value_units_original = ExperimentalConstants.str_g_L;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mg/100")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_100mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("g/100")) {
			er.property_value_units_original = ExperimentalConstants.str_g_100mL;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("% w/w") || propertyValue.toLowerCase().contains("wt%")) {
			er.property_value_units_original = ExperimentalConstants.str_pctWt;
			unitsIndex = propertyValue.indexOf("%");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("%")) {
			er.property_value_units_original = ExperimentalConstants.str_pct;
			unitsIndex = propertyValue.indexOf("%");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ppm")) {
			er.property_value_units_original = ExperimentalConstants.str_ppm;
			unitsIndex = propertyValue.toLowerCase().indexOf("ppm");
			badUnits = false;
		} else if (propertyValue.contains("M")) {
			unitsIndex = propertyValue.indexOf("M");
			if (unitsIndex>0) {
				er.property_value_units_original = ExperimentalConstants.str_M;
				badUnits = false;
			}
		} 
		
		if (er.source_name!=ExperimentalConstants.strSourceOFMPub && unitsIndex < propertyValue.indexOf(":")) {
			unitsIndex = propertyValue.length();
		}
		
		if (Character.isAlphabetic(propertyValue.charAt(0)) && !(propertyValue.contains("water") || propertyValue.contains("h2o")) &&
				!(propertyValue.contains("ca") || propertyValue.contains("circa") || propertyValue.contains(">") ||
						propertyValue.contains("<") || propertyValue.contains("=") || propertyValue.contains("~"))) {
			er.keep = false;
			er.reason = "Non-aqueous solubility";
		}
		
		boolean foundNumeric = getNumericalValue(er,propertyValue, unitsIndex,badUnits);
		return foundNumeric;
	}
	
	void getQualitativeSolubility(ExperimentalRecord er, String propertyValue) {
		propertyValue = propertyValue.toLowerCase();
		String solventMatcherStr = "";
		if (sourceName.equals(ExperimentalConstants.strSourceLookChem)) {
			solventMatcherStr = "(([a-zA-Z0-9\s-]+?)(,| and|\\.|\\z|[ ]?\\(|;))?";
		} else if (sourceName.equals(ExperimentalConstants.strSourcePubChem)) {
			solventMatcherStr = "(([a-zA-Z0-9\s,-]+?)(\\.|\\z| at| and only|\\(|;))?";
		}
		Matcher solubilityMatcher = Pattern.compile("(([a-zA-Z]+y[ ]?)?([a-zA-Z]+y[ ]?)?(in|im)?(so[l]?uble|miscible))( (in|with) )?[[ ]?\\.{3}]*"+solventMatcherStr).matcher(propertyValue);
		while (solubilityMatcher.find()) {
			String qualifier = solubilityMatcher.group(1);
			qualifier = qualifier.equals("souble") ? "soluble" : qualifier;
			String prep = solubilityMatcher.group(6);
			String solvent = solubilityMatcher.group(9);
			if (solvent==null || solvent.length()==0 || solvent.contains("water")) {
				er.property_value_qualitative = qualifier;
			} else {
				prep = prep==null ? " " : prep;
				er.updateNote(qualifier + prep + solvent);
			}
		}
		
		if (propertyValue.contains("reacts") || propertyValue.contains("reaction")) {
			er.property_value_qualitative = "reaction";
		}
		
		if (propertyValue.contains("hydrolysis") || propertyValue.contains("hydrolyse") || propertyValue.contains("hydrolyze")) {
			er.property_value_qualitative = "hydrolysis";
		}
		
		if (propertyValue.contains("decompos")) {
			er.property_value_qualitative = "decomposes";
		}
		
		if (propertyValue.contains("autoignition")) {
			er.property_value_qualitative = "autoignition";
		}
		
		String[] qualifiers = {"none","very poor","poor","low","negligible","slight","significant","complete"};
		for (String qual:qualifiers) {
			if ((propertyValue.startsWith(qual) || (propertyValue.contains("solubility in water") && propertyValue.contains(qual))) &&
					(er.property_value_qualitative==null || er.property_value_qualitative.isBlank())) {
				er.property_value_qualitative = qual;
			}
		}
		
		if (er.property_value_qualitative!=null || er.note!=null) {
			er.keep = true;
			er.reason = null;
		}
	}

	boolean getVaporPressure(ExperimentalRecord er,String propertyValue) {
		boolean badUnits = true;
		int unitsIndex = -1;
		propertyValue = propertyValue.replaceAll("([0-9]),([0-9]{3})", "$1$2");
		if (propertyValue.toLowerCase().contains("kpa")) {
			er.property_value_units_original = ExperimentalConstants.str_kpa;
			unitsIndex = propertyValue.toLowerCase().indexOf("kpa");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mm")) {
			er.property_value_units_original = ExperimentalConstants.str_mmHg;
			unitsIndex = propertyValue.toLowerCase().indexOf("mm");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("atm")) {
			er.property_value_units_original = ExperimentalConstants.str_atm;
			unitsIndex = propertyValue.toLowerCase().indexOf("atm");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("hpa")) {
			er.property_value_units_original = ExperimentalConstants.str_hpa;
			unitsIndex = propertyValue.toLowerCase().indexOf("hpa");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("pa")) {
			er.property_value_units_original = ExperimentalConstants.str_pa;
			unitsIndex = propertyValue.toLowerCase().indexOf("pa");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mbar")) {
			er.property_value_units_original = ExperimentalConstants.str_mbar;
			unitsIndex = propertyValue.toLowerCase().indexOf("mb");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("bar")) {
			er.property_value_units_original = ExperimentalConstants.str_bar;
			unitsIndex = propertyValue.toLowerCase().indexOf("bar");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("torr")) {
			er.property_value_units_original = ExperimentalConstants.str_torr;
			unitsIndex = propertyValue.toLowerCase().indexOf("torr");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("psi")) {
			er.property_value_units_original = ExperimentalConstants.str_psi;
			unitsIndex = propertyValue.toLowerCase().indexOf("psi");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains(ExperimentalConstants.str_negl)) {
			er.property_value_qualitative = ExperimentalConstants.str_negl;
		}
		
		if (er.source_name!=ExperimentalConstants.strSourceOFMPub && propertyValue.contains(":")) {
			unitsIndex = propertyValue.length();
		}

		boolean foundNumeric = getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		return foundNumeric;
	}

	static boolean getHenrysLawConstant(ExperimentalRecord er,String propertyValue) {
		boolean badUnits = true;
		int unitsIndex = -1;
		if (propertyValue.toLowerCase().contains("atm-m3/mole") || propertyValue.toLowerCase().contains("atm m³/mol")) {
			er.property_value_units_original = ExperimentalConstants.str_atm_m3_mol;
			unitsIndex = propertyValue.toLowerCase().indexOf("atm");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("pa m³/mol")) {
			er.property_value_units_original = ExperimentalConstants.str_Pa_m3_mol;
			unitsIndex = propertyValue.toLowerCase().indexOf("pa");
			badUnits = false;
		}
		boolean foundNumeric = getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		return foundNumeric;
	}

	// Applicable for LogKow and pKa
	static boolean getLogProperty(ExperimentalRecord er,String propertyValue) {
		int unitsIndex = -1;
		if (propertyValue.contains("at")) {
			unitsIndex = propertyValue.indexOf("at");
		} else {
			unitsIndex = propertyValue.length();
		}
		boolean badUnits = false;
		boolean foundNumeric = getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		return foundNumeric;
	}
	
	/**
	 * Sets the temperature condition for an ExperimentalRecord object, if present
	 * @param er			The ExperimentalRecord object to be updated
	 * @param propertyValue	The string to be read
	 * @return				The temperature condition in C
	 */
	static void getTemperatureCondition(ExperimentalRecord er, String propertyValue) {
		String units = getTemperatureUnits(propertyValue);
		int tempIndex = propertyValue.indexOf(units);
		// If temperature units were found, looks for the last number that precedes them
		if (tempIndex > 0) {
			try {
				Matcher m = Pattern.compile("[-]?[0-9]*\\.?[0-9]+").matcher(propertyValue.substring(0,tempIndex));
				String tempStr = "";
				while (m.find()) { tempStr = m.group(); }
				if (tempStr.length()!=0) {
					// Converts to C as needed
					double temp = Double.parseDouble(tempStr);
					switch (units) {
					case "C":
						er.temperature_C = temp;
						break;
					case "F":
						er.temperature_C = (temp-32)*5/9;
						break;
					case "K":
						er.temperature_C = temp-273.15;
						break;
					}
				}
			} catch (Exception ex) { }
		}
	}

	/**
	 * Sets the pressure condition for an ExperimentalRecord object, if present
	 * @param er			The ExperimentalRecord object to be updated
	 * @param propertyValue	The string to be read
	 * @return				The pressure condition in kPa
	 */
	void getPressureCondition(ExperimentalRecord er,String propertyValue) {
		propertyValue = propertyValue.toLowerCase();
		int pressureIndex = -1;
		double conversionFactor = 1.0;
		if (propertyValue.contains("kpa")) {
			pressureIndex = propertyValue.indexOf("kpa");
			conversionFactor = UnitConverter.kPa_to_mmHg;
		} else if (propertyValue.contains("mmhg") || propertyValue.contains("mm hg") || propertyValue.contains("mm")) {
			pressureIndex = propertyValue.indexOf("mm");
		} else if (propertyValue.contains("atm")) {
			pressureIndex = propertyValue.indexOf("atm");
			conversionFactor = UnitConverter.atm_to_mmHg;
		} else if (propertyValue.contains("hpa")) {
			pressureIndex = propertyValue.indexOf("hpa");
			conversionFactor = UnitConverter.hPa_to_mmHg;
		} else if (propertyValue.contains("pa")) {
			pressureIndex = propertyValue.indexOf("pa");
			conversionFactor = UnitConverter.Pa_to_mmHg;
		} else if (propertyValue.contains("mbar")) {
			pressureIndex = propertyValue.indexOf("mb");
			conversionFactor = UnitConverter.hPa_to_mmHg;
		} else if (propertyValue.contains("bar")) {
			pressureIndex = propertyValue.indexOf("bar");
			conversionFactor = UnitConverter.bar_to_mmHg;
		} else if (propertyValue.contains("torr")) {
			pressureIndex = propertyValue.indexOf("torr");
		} else if (propertyValue.contains("psi")) {
			pressureIndex = propertyValue.indexOf("psi");
			conversionFactor = UnitConverter.psi_to_mmHg;
		} 
		// If any pressure units were found, looks for the last number that precedes them
		boolean foundNumeric = false;
		if (pressureIndex > 0) {
			if (sourceName.equals(ExperimentalConstants.strSourceEChem)) {
				if (!foundNumeric) {
					try {
						double[] range = Parse.extractFirstDoubleRangeFromString(propertyValue,pressureIndex);
						String min = formatDouble(range[0]*conversionFactor);
						String max = formatDouble(range[1]*conversionFactor);
						er.pressure_mmHg = min+"-"+max;
						foundNumeric = true;
					} catch (Exception ex) { }
				}
				if (!foundNumeric) {
					try {
						Matcher caMatcher = Pattern.compile(".*?(ca. )?([-]?[ ]?[0-9]*\\.?[0-9]+)( ca. )([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(propertyValue.substring(0,pressureIndex));
						if (caMatcher.find()) {
							String numQual = caMatcher.group(1).isBlank() ? "" : "~";
							String min = formatDouble(Double.parseDouble(caMatcher.group(2)));
							String max = formatDouble(Double.parseDouble(caMatcher.group(4)));
							er.pressure_mmHg = numQual+min+"~"+max;
							foundNumeric = true;
						}
					} catch (Exception ex) { }
				}
			}
			if (!foundNumeric) {
				try {
					er.pressure_mmHg = formatDouble(conversionFactor*Parse.extractDoubleFromString(propertyValue,pressureIndex));
					foundNumeric = true;
				} catch (Exception ex) { }
			}
		}
	}
	
	public static String formatDouble(double d) {
        DecimalFormat df2 = new DecimalFormat("0.##");
        DecimalFormat dfSci = new DecimalFormat("0.00E0");
        if (d < 0.01) {
        	return dfSci.format(d);
        } else {
	    	return df2.format(d);
        }

	}


	/**
	 * Extracts the first range of numbers before a given index in a string
	 * @param str	The string to be read
	 * @param end	The index to stop searching
	 * @return		The range found as a double[2]
	 * @throws IllegalStateException	If no number range is found in the given range
	 */
	static double[] extractFirstDoubleRangeFromString(String str,int end) throws IllegalStateException {
		Matcher anyRangeMatcher = Pattern.compile("([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]*([-]{1}|to|ca\\.)[ ]*([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(str.substring(0,end));
		anyRangeMatcher.find();
		String strMin = anyRangeMatcher.group(1).replace(" ","");
		String strMax = anyRangeMatcher.group(3).replace(" ","");
		double min = Double.parseDouble(strMin);
		double max = Double.parseDouble(strMax);
		if (min >= max) {
			int digits = strMax.length();
			strMax = strMin.substring(0,strMin.length()-digits)+strMax;
			max = Double.parseDouble(strMax);
		}
		double[] range = {min, max};
		return range;
	}
	
	static double[] extractAltFormatRangeFromString(String str,int end) throws IllegalStateException {
		Matcher anyRangeMatcher = Pattern.compile(">[=]?[ ]?([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]?<[=]?[ ]?([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(str.substring(0,end));
		anyRangeMatcher.find();
		String strMin = anyRangeMatcher.group(1).replace(" ","");
		String strMax = anyRangeMatcher.group(2).replace(" ","");
		double min = Double.parseDouble(strMin);
		double max = Double.parseDouble(strMax);
		if (min >= max) {
			int digits = strMax.length();
			strMax = strMin.substring(0,strMin.length()-digits)+strMax;
			max = Double.parseDouble(strMax);
		}
		double[] range = {min, max};
		return range;
	}

	/**
	 * Extracts the last number before a given index in a string
	 * @param str	The string to be read
	 * @param end	The index to stop searching
	 * @return		The number found as a double
	 * @throws IllegalStateException	If no number is found in the given range
	 */
	static double extractDoubleFromString(String str,int end) throws IllegalStateException, NumberFormatException {
		Matcher numberMatcher = Pattern.compile("[-]?[ ]?[0-9]*\\.?[0-9]+").matcher(str.substring(0,end));
		String strDouble = "";
		while (numberMatcher.find()) { strDouble = numberMatcher.group(); }
		return Double.parseDouble(strDouble.replace(" ",""));
	}

	/**
	 * If the property value string contains temperature units, returns the units in standardized format
	 * @param propertyValue	The string to be read
	 * @return				A standardized temperature unit string from ExperimentalConstants
	 */
	static String getTemperatureUnits(String propertyValue) {
		propertyValue=propertyValue.replaceAll(" ","");
		String units = "";
		if (propertyValue.contains("°C") || propertyValue.contains("ºC") || propertyValue.contains("oC")
				|| (propertyValue.indexOf("C") > 0 && Character.isDigit(propertyValue.charAt(propertyValue.indexOf("C")-1)))) {
			units = ExperimentalConstants.str_C;
		} else if (propertyValue.contains("°F") || propertyValue.contains("ºF") || propertyValue.contains("oF")
				|| (propertyValue.indexOf("F") > 0 && Character.isDigit(propertyValue.charAt(propertyValue.indexOf("F")-1)))) {
			units = ExperimentalConstants.str_F;
		} else if ((propertyValue.indexOf("K") > 0 && Character.isDigit(propertyValue.charAt(propertyValue.indexOf("K")-1)))) {
			units = ExperimentalConstants.str_K;
		} 
		return units;
	}

	public static String fixChars(String str) {
		str=str.replace("Ã¢â‚¬â€œ","-").replace("Ã¢â‚¬â„¢","'");
		str=str.replace("\uff08", "(");// Ã¯Â¼Ë†
		str=str.replace("\uff09", ")");// Ã¯Â¼â€°
		str=str.replace("\uff0f", "/");// Ã¯Â¼ï¿½
		str=str.replace("\u3000", " ");//blank
		str=str.replace("\u00a0", " ");//blank
		str=str.replace("\u2003", " ");//blank
		str=str.replace("\u0009", " ");//blank
		str=str.replace("\u300c", "");// Ã£â‚¬Å’
		str=str.replace("\u300d", "");// Ã£â‚¬ï¿½
		// str=str.replace("\u2264", "&le;");// <=  for some reason Gson messes this up so need to convert to html so code doesnt get mangled into weird symbol
		// str=str.replace("\u03B1", "&alpha;");//alpha

		return str;
	}
	
	public static void main(String[] args) {
		ParseADDoPT.main(null);
		ParseAqSolDB.main(null);
		ParseBradley.main(null);
		ParseEChemPortal.main(null);
		ParseLookChem.main(null);
		ParseOChem.main(null);
		ParseOFMPub.main(null);
		ParsePubChem.main(null);
		ParseQSARDB.main(null);
		// ParseChemBL.main(null);
		DataFetcher.main(null);
	}
}

