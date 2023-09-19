package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Vector;
import java.util.zip.ZipOutputStream;

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

import gov.epa.api.RawDataRecord;
import gov.epa.database.SQLite_CreateTable;

import gov.epa.ghs_data_gathering.Utilities.FileUtilities;

public class DownloadWebpageUtilities {

	/**
	 * Stores the content from a list of URLs in timestamped records in a RawDataRecord database
	 * Crawl delay is adaptive: Random multiplier of 1-2 times the time taken to load the page (~1-2 seconds on LookChem)
	 * @param urls			The URLs to be downloaded
	 * @param tableName		The name of the table to store the data in, i.e., the source name
	 * @param startFresh	True to remake database table completely, false to append new records to existing table
	 */
	public static void downloadWebpagesToDatabaseAdaptive(Vector<String> urls,String databasePath,String tableName, boolean startFresh) {
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		
		java.sql.Connection conn=SQLite_CreateTable.create_table(databasePath, tableName, RawDataRecord.fieldNames, startFresh);
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
						rec.content=FileUtilities.getText_UTF8(url).replaceAll("'", "''").replaceAll(";", "\\;"); //single quotes mess with the SQL insert later
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

	public static void downloadWebpagesToDatabaseAdaptiveNonUnicode(Vector<String> urls,String databasePath,String tableName, boolean startFresh) {
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		
		java.sql.Connection conn=SQLite_CreateTable.create_table(databasePath, tableName, RawDataRecord.fieldNames, startFresh);
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
						rec.content=FileUtilities.getText(url).replaceAll("'", "''").replaceAll(";", "\\;"); //single quotes mess with the SQL insert later
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
	public static void downloadWebpagesToDatabaseAdaptive(Vector<String> urls,String css,String databasePath,String tableName,boolean startFresh) {
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		
		java.sql.Connection conn=SQLite_CreateTable.create_table(databasePath, tableName, RawDataRecord.fieldNames, startFresh);
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
						html=FileUtilities.getText_UTF8(url).replaceAll("'", "''").replaceAll(";", "\\;"); //single quotes mess with the SQL insert later
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
	public static void downloadWebpagesToDatabase(Vector<String> urls,String databasePath,String tableName, boolean startFresh) {
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		
		java.sql.Connection conn=SQLite_CreateTable.create_table(databasePath, tableName, RawDataRecord.fieldNames, startFresh);
		
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
						rec.content=FileUtilities.getText_UTF8(url).replaceAll("'", "''").replaceAll(";", "\\;"); //single quotes mess with the SQL insert later
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
	public static void downloadWebpagesToZipFile(Vector<String> urls,String webpageFolder) {
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
					
					//TODO probably should check if the varlist contains the field name rather than the header tab delimited string
					if (RecordDashboard.getHeader().contains(field) && cell.getCellType() == CellType.STRING) {
						String data = cell.getStringCellValue();
						temp.setValue(field,data);
					} else if (RecordDashboard.getHeader().contains(field) && cell.getCellType() == CellType.NUMERIC) {
						double data = cell.getNumericCellValue();
						temp.setValue(field,data+"");
					}
				}
				records.add(temp);
//				System.out.println(temp.AVERAGE_MASS);
			}
			wb.close();
			fis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return records;
		
	}

	// Gets the creation date of any file as a string
	public static String getStringCreationDate(String filepath) {
		Path path = Paths.get(filepath);
		try {
			BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
			FileTime createdAt = attrs.creationTime();
			Date creationDate = new Date(createdAt.toMillis());
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");  
			String strCreationDate=formatter.format(creationDate);
			return strCreationDate;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
