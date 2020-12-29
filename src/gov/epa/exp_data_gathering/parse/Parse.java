package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.exp_data_gathering.eChemPortalAPI.ParseEChemPortalAPI;

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
	
	protected Gson gson=null;

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
		str=str.replace("\u2070", "^0");// superscript 0
		str=str.replace("\u00B9", "^1");// superscript 1
		str=str.replace("\u00B2", "^2");// superscript 2
		str=str.replace("\u00B3", "^3");// superscript 3
		str=str.replace("\u2074", "^4");// superscript 4
		str=str.replace("\u2075", "^5");// superscript 5
		str=str.replace("\u2076", "^6");// superscript 6
		str=str.replace("\u2077", "^7");// superscript 7
		str=str.replace("\u2078", "^8");// superscript 8
		str=str.replace("\u2079", "^9");// superscript 9
		str=str.replace("\u2080", "_0");// subscript 0
		str=str.replace("\u2081", "_1");// subscript 1
		str=str.replace("\u2082", "_2");// subscript 2
		str=str.replace("\u2083", "_3");// subscript 3
		str=str.replace("\u2084", "_4");// subscript 4
		str=str.replace("\u2085", "_5");// subscript 5
		str=str.replace("\u2086", "_6");// subscript 6
		str=str.replace("\u2087", "_7");// subscript 7
		str=str.replace("\u2088", "_8");// subscript 8
		str=str.replace("\u2089", "_9");// subscript 9

		return str;
	}
	
	public static void main(String[] args) {
		ParseADDoPT.main(null);
		ParseAqSolDB.main(null);
		ParseBradley.main(null);
		ParseEChemPortalAPI.main(null);
		ParseLookChem.main(null);
		ParseOChem.main(null);
		ParseOFMPub.main(null);
		ParsePubChem.main(null);
		ParseQSARDB.main(null);
		// ParseChemBL.main(null);
		DataFetcher.main(null);
	}
}

