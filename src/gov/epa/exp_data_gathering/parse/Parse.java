package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
	protected String fileNameFlatExperimentalRecordsBad;
	protected String fileNameJsonExperimentalRecordsBad;
	protected String mainFolder;
	
	public static boolean generateOriginalJSONRecords=true; //runs code to generate json records from original data format (json file has all the chemicals in one file)	
	public static boolean writeFlatFile=false;//all data converted to final format stored as flat text file
	public static boolean writeJsonChemicalsFile=true;//all data converted to final format stored as Json file
	
	Gson gson=null;

	public void init() {
		fileNameJSON_Records = sourceName +" Records.json";
		fileNameFlatExperimentalRecords = sourceName +" Experimental Records.txt";
		fileNameFlatExperimentalRecordsBad = sourceName +" Experimental Records-Bad.txt";
		fileNameJsonExperimentalRecords = sourceName +" Experimental Records.json";
		fileNameJsonExperimentalRecordsBad = sourceName +" Experimental Records-Bad.json";
		mainFolder = AADashboard.dataFolder + File.separator + "Experimental" + File.separator + sourceName;
		databaseFolder = mainFolder + File.separator + "databases";
		jsonFolder= mainFolder + File.separator + "json files";
		webpageFolder = mainFolder + File.separator + "web pages";
		
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
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
						rec.content=FileUtilities.getText_UTF8(url).replace("'", "''"); //single quotes mess with the SQL insert later
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
	public void downloadWebpagesToDatabaseAdaptive(Vector<String> urls,String htmlClass,String tableName, boolean startFresh) {
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
		Random rand = new Random();
		
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
						rec.content=FileUtilities.getText_UTF8(url).replace("'", "''"); //single quotes mess with the SQL insert later
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
			System.out.println("Attempted "+counterTotal+"pages, downloaded "+counterSuccess+" pages");
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
		ExperimentalRecords recordsBad = dumpBadRecords(records);

		if (writeFlatFile) {
			System.out.println("Writing flat file for chemical records");
			records.toFlatFile(mainFolder+File.separator+fileNameFlatExperimentalRecords,"|");
			recordsBad.toFlatFile(mainFolder+File.separator+fileNameFlatExperimentalRecordsBad,"|");
		}
		
		if (writeJsonChemicalsFile) {
			System.out.println("Writing json file for chemical records");
			records.toJSON_File(mainFolder+File.separator+fileNameJsonExperimentalRecords);
			recordsBad.toJSON_File(mainFolder+File.separator+fileNameJsonExperimentalRecordsBad);
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
	
	void getQualitativeSolubility(ExperimentalRecord er, String propertyValue) {
		String[] solubilityIn = {ExperimentalConstants.str_inSol,ExperimentalConstants.str_verySol,ExperimentalConstants.str_freelySol,
				ExperimentalConstants.str_sparinglySol,ExperimentalConstants.str_verySlightlySol,ExperimentalConstants.str_slightlySol,
				ExperimentalConstants.str_sol,ExperimentalConstants.str_negl,ExperimentalConstants.str_dec,ExperimentalConstants.str_poor,
				ExperimentalConstants.str_none,ExperimentalConstants.str_low};
		propertyValue = propertyValue.toLowerCase();
		boolean foundWaterSol = false;
		for (String sol:solubilityIn) {
			if (!foundWaterSol && propertyValue.contains(sol+" in water")) {
				er.property_value_qualitative=sol;
				foundWaterSol = true;
			} else if (!foundWaterSol && propertyValue.contains(sol) && !propertyValue.contains(sol+" in")) {
				er.property_value_qualitative=sol; // Assume water if solvent not explicit
				foundWaterSol = true;
			}
		}
		
		String[] solubilityWith = {ExperimentalConstants.str_immisc,ExperimentalConstants.str_misc,ExperimentalConstants.str_hydr,
				ExperimentalConstants.str_reaction,ExperimentalConstants.str_reacts};
		for (String sol:solubilityWith) {
			if (!foundWaterSol && propertyValue.contains(sol+" with water")) {
				er.property_value_qualitative=sol;
				foundWaterSol = true;
			} else if (!foundWaterSol && propertyValue.contains(sol) && !propertyValue.contains(sol+" with")) {
				er.property_value_qualitative=sol; // Assume water if solvent not explicit
				foundWaterSol = true;
			}
		}
		
		String solventMatcherStr = "";
		if (sourceName.equals(ExperimentalConstants.strSourceLookChem)) {
			solventMatcherStr = "([a-zA-Z0-9\s,-]+?)(\\.|\\z| and|\\(|;)";
		} else if (sourceName.equals(ExperimentalConstants.strSourcePubChem)) {
			solventMatcherStr = "([a-zA-Z0-9\s,-]+?)(\\.|\\z| at|\\(|;)";
		}
		Vector<String> solventCheck = new Vector<String>();
		for (String sol:solubilityIn) {
			if (propertyValue.contains(sol+" in ")) {
				String search = sol+" in ";
				String searchStr = propertyValue.substring(propertyValue.indexOf(search)+search.length());
				Matcher solventMatcher = Pattern.compile(solventMatcherStr).matcher(searchStr);
				solventMatcher.find();
				String solvent = solventMatcher.group(1);
				if (!solvent.contains("water") && !solventCheck.contains(solvent)) { 
					er.updateNote(search+solvent);
					solventCheck.add(solvent);
				}
			}
		}

		for (String sol:solubilityWith) {
			if (propertyValue.contains(sol+" with ")) {
				String search = sol+" with ";
				String searchStr = propertyValue.substring(propertyValue.indexOf(search)+search.length());
				Matcher solventMatcher = Pattern.compile(solventMatcherStr).matcher(searchStr);
				solventMatcher.find();
				String solvent = solventMatcher.group(1);
				if (!solvent.contains("water") && !solventCheck.contains(solvent)) { 
					er.updateNote(search+solvent);
					solventCheck.add(solvent);
				}
			}
		}
		
		if (er.note!= null) { er.note = er.note.replaceAll("reacts", "reaction"); }
		if (er.property_value_qualitative!=null) { er.property_value_qualitative = er.property_value_qualitative.replaceAll("reacts", "reaction"); }
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
					double tempC = Double.parseDouble(tempStr);
					switch (units) {
					case "C":
						er.temperature_C = tempC;
						break;
					case "F":
						er.temperature_C = (tempC-32)*5/9;
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
	static void getPressureCondition(ExperimentalRecord er,String propertyValue) {
		propertyValue = propertyValue.toLowerCase();
		int pressureIndex = propertyValue.indexOf("mm");
		// If "mm" not found, looks for "torr" instead - a handful of records use this
		if (pressureIndex == -1) { pressureIndex = propertyValue.indexOf("torr"); }
		// If either set of pressure units were found, looks for the last number that precedes them
		if (pressureIndex > 0) {
			try {
				Matcher m = Pattern.compile("[-]?[0-9]*\\.?[0-9]+").matcher(propertyValue.substring(0,pressureIndex));
				String pressure = "";
				while (m.find()) { pressure = m.group(); }
				if (pressure.length()!=0) { er.pressure_kPa = Double.parseDouble(pressure)*ExperimentalConstants.mmHg_to_kPa; }
			} catch (Exception ex) { }
		} else if ((pressureIndex = propertyValue.indexOf("atm")) > 0) {
			try {
				Matcher m = Pattern.compile("[-]?[0-9]*\\.?[0-9]+").matcher(propertyValue.substring(0,pressureIndex));
				String pressure = "";
				while (m.find()) { pressure = m.group(); }
				if (pressure.length()!=0) { er.pressure_kPa = Double.parseDouble(pressure)*ExperimentalConstants.atm_to_kPa; }
			} catch (Exception ex) { }
		};
	}

	/**
	 * Extracts the first range of numbers before a given index in a string
	 * @param str	The string to be read
	 * @param end	The index to stop searching
	 * @return		The range found as a double[2]
	 * @throws IllegalStateException	If no number range is found in the given range
	 */
	static double[] extractFirstDoubleRangeFromString(String str,int end) throws IllegalStateException {
		Matcher anyRangeMatcher = Pattern.compile("([-]?[ ]?[0-9]*\\.?[0-9]+)[ ]*([-]{1}|to)[ ]*([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(str.substring(0,end));
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
		if (propertyValue.contains("�C") || propertyValue.contains("�C") || propertyValue.contains("oC")
				|| (propertyValue.contains("C") && Character.isDigit(propertyValue.charAt(propertyValue.indexOf("C")-1)))) {
			units = ExperimentalConstants.str_C;
		} else if (propertyValue.contains("�F") || propertyValue.contains("�F") || propertyValue.contains("oF")
				|| (propertyValue.contains("F") && Character.isDigit(propertyValue.charAt(propertyValue.indexOf("F")-1)))) {
			units = ExperimentalConstants.str_F;
		} 
		return units;
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

