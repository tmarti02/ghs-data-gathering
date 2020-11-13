package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.zip.ZipOutputStream;
import java.util.Random;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.AADashboard;
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
	protected String mainFolder;
	
	public static boolean generateOriginalJSONRecords=true; //runs code to generate json records from original data format (json file has all the chemicals in one file)	
	public static boolean writeFlatFile=true;//all data converted to final format stored as flat text file
	public static boolean writeJsonChemicalsFile=true;//all data converted to final format stored as Json file
	
	Gson gson=null;

	public void init() {
		fileNameJSON_Records = sourceName +" Records.json";
		fileNameFlatExperimentalRecords = sourceName +" Experimental Records.txt";
		fileNameJsonExperimentalRecords = sourceName +" Experimental Records.json";
		mainFolder = AADashboard.dataFolder + File.separator + "Experimental" + File.separator + sourceName;
		databaseFolder = mainFolder + File.separator + "databases";
		jsonFolder= mainFolder + File.separator + "json files";
		webpageFolder = mainFolder + File.separator + "web pages";
		
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		gson = builder.create();
	}
	
	/**
	 * Stores the HTML strings from a list of URLs in timestamped records in the raw HTML database
	 * @param urls			The URLs to be downloaded
	 * @param tableName		The name of the table to store the data in, i.e., the source name
	 * @param startFresh	True to remake database table completely, false to append new records to existing table
	 */
	public void downloadWebpagesToDatabase(Vector<String> urls,String tableName, boolean startFresh) {
		String databasePath = databaseFolder+File.separator+sourceName+"_raw_html.db";
		System.out.println(databasePath);
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		
		java.sql.Connection conn=CreateGHS_Database.createDatabaseTable(databasePath, tableName, RawDataRecord.fieldNames, startFresh);
		Random rand = new Random();
		
		try {
			int counter = 1;
			for (String url:urls) {
				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
				Date date = new Date();  
				String strDate=formatter.format(date);
				
				RawDataRecord rec=new RawDataRecord(strDate, url, "");
				boolean haveRecord=rec.haveRecordInDatabase(databasePath,tableName,conn);
				if (!haveRecord || startFresh) {
					long delay = 0;
					try {
						long startTime=System.currentTimeMillis();
						rec.html=FileUtilities.getText_UTF8(url).replace("'", "''"); //single quotes mess with the SQL insert later
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
	public void downloadWebpagesToDatabase(Vector<String> urls,String htmlClass,String tableName, boolean startFresh) {
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
						html=FileUtilities.getText_UTF8(url).replace("'", "''"); //single quotes mess with the SQL insert later
						long endTime=System.currentTimeMillis();
						delay=endTime-startTime;
						Document doc = Jsoup.parse(html);
						Element table=doc.select("."+htmlClass).first();
						if (table!=null) {
							rec.html=table.outerHtml();
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
	 * Downloads the HTML files from a list of URLs and saves them in a zip folder
	 * @param urls			The URLs to be downloaded
	 * @param sourceName	The source name
	 */
	public void downloadWebpagesToZipFile(Vector<String> urls,String sourceName) {
		String destZipFolder=webpageFolder+".zip";
		Random rand = new Random();

		try {
			int counter = 1;
			for (String url:urls) {
				String fileName = url.substring( url.lastIndexOf("/")+1, url.length() );
				String destFilePath = webpageFolder + "/" + fileName;
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
			File file = new File(filename);
			FileInputStream fis = new FileInputStream(file);
			HSSFWorkbook wb=new HSSFWorkbook(fis);
			HSSFSheet sheet=wb.getSheetAt(0);
			
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

		if (writeFlatFile) {
			System.out.println("Writing flat file for chemical records");
			records.toFlatFile(mainFolder+File.separator+fileNameFlatExperimentalRecords,"|");
		}
		
		if (writeJsonChemicalsFile) {
			System.out.println("Writing json file for chemical records");
			records.toJSON_File(mainFolder+File.separator+fileNameJsonExperimentalRecords);
		}
		
		System.out.println("done\n");
	}
	
	protected void writeOriginalRecordsToFile(Vector<?> records) {
		try {
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			
			FileWriter fw = new FileWriter(jsonFolder + File.separator + fileNameJSON_Records);
			String strRecords=gson.toJson(records);
			
			strRecords=Parse.fixChars(strRecords);
			
			fw.write(strRecords);
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static String fixChars(String str) {
		str=str.replace("â€“","-").replace("â€™","'");
		str=str.replace("\uff08", "(");// ï¼ˆ
		str=str.replace("\uff09", ")");// ï¼‰
		str=str.replace("\uff0f", "/");// ï¼�
		str=str.replace("\u3000", " ");//blank
		str=str.replace("\u00a0", " ");//blank
		str=str.replace("\u2003", " ");//blank
		str=str.replace("\u0009", " ");//blank
		str=str.replace("\u300c", "");// ã€Œ
		str=str.replace("\u300d", "");// ã€�
		str=str.replace("\u2264", "&le;");// <=  for some reason Gson messes this up so need to convert to html so code doesnt get mangled into weird symbol
		str=str.replace("\u03B1", "&alpha;");//alpha

		return str;
	}
}

