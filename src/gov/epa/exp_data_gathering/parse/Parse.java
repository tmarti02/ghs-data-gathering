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
import org.jsoup.Connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.AADashboard;
import gov.epa.api.RawDataRecord;
import gov.epa.ghs_data_gathering.Database.CreateGHS_Database;
import gov.epa.ghs_data_gathering.GetData.RecordDashboard;
import gov.epa.ghs_data_gathering.Utilities.FileUtilities;

public class Parse {
	
	public static String sourceName;
	public static String jsonFolder;

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
		mainFolder = AADashboard.dataFolder + File.separator + sourceName;
		jsonFolder= mainFolder + "/json files";
		
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		gson = builder.create();

	}
	
	public static void downloadWebpagesToDatabase(Vector<String> urls,String databasePath, 
			String tableName, boolean startFresh) {
		//table name based on source name in ExperimentalConstants
		Connection conn=CreateGHS_Database.createDatabaseTable(databasePath, tableName, RawDataRecord.fieldNames, startFresh);// modify this method to only drop table if startFresh
		Random rand = new Random();
		
		for (String url:urls) {
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
			Date date = new Date();  
			String strDate=formatter.format(date);
			
			System.out.println(fileName+"\t"+strDate);
			
			//Download the file as a string:
			String html=FileUtilities.getText_UTF8(url).replace("'", "''"); //single quotes mess with the SQL insert later                                 
			System.out.println(html);  
			                            
			//TODO: for now entire html is stored- could also just store the part of html we need via JSoup
			                                                                                                                  
			RawDataRecord rec=new RawDataRecord(strDate, url, html);
			
			//TODO – code needs to be able to tell whether or not your internet failed or if the webpage simply isn’t available. Some websites will return a simple webpage if record not available- the database should reflect this so that that you don’t waste 3 seconds of time the next time this method is ran (i.e. don’t try to download it again). 
			
			//TODO- add query here to see if already have record in the database:
			boolean haveRecord=rec.haveRecordInDatabase(tableName,conn);//add this method
			
			if (!haveRecord || startFresh) {
				rec.addRecordToDatabase(tableName, conn);            
			}

			Thread.sleep(2000+rand.nextInt(2000));
		}
	}

	public static void downloadWebpagesToZipFile(Vector<String> urls,String sourceName,String mainFolder) {
		String folderNameWebpages = "web pages";
		String destFolder=mainFolder+File.separator+folderNameWebpages;
		String destZipFolder=destFolder+".zip";
		Random rand = new Random();

		try {
			
			for (String url:urls) {
				String fileName = url.substring( url.lastIndexOf("/")+1, url.length() );
				String destFilePath = destFolder + "/" + fileName;
				File destFile = new File(destFilePath);
				if(!destFile.getParentFile().exists()) {
				     destFile.getParentFile().mkdirs();
				}
	
				FileUtilities.downloadFile(url, destFilePath);
				
				Thread.sleep(2000+rand.nextInt(2000));
			}

			FileOutputStream fos = new FileOutputStream(destZipFolder); 
			ZipOutputStream zipOS = new ZipOutputStream(fos); 
			
			File webpageFolder=new File(destFolder);
			File [] files=webpageFolder.listFiles();
			
			// Create a zip file
			for (File file:files) {
				if (file.getName().contains(".html")); {
					FileUtilities.writeToZipFile(file.getName(),destFolder,folderNameWebpages, zipOS);
				}
				// Deletes all files so folder can be deleted after zip finishes
				file.delete();
			}
			// Delete web pages folder
			webpageFolder.delete();
			
			zipOS.close();
            fos.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
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
					if (RecordDashboard.getHeader().contains(field) && cell.getCellType() == CellType.STRING) {
						String data = cell.getStringCellValue();
						temp.setValue(field,data);
					}
				}
				records.add(temp);
			}
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
	 * 
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
			
//			System.out.println("here1");
			
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
	
	void writeOriginalRecordsToFile(Vector<?>records) {

		try {
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			
			FileWriter fw = new FileWriter(mainFolder + "/" + fileNameJSON_Records);
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

