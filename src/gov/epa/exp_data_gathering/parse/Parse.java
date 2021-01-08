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
//		records.dontKeepNumericQualifierRecords();
		records.addSourceBasedIDNumbers();
		
		DataRemoveDuplicateExperimentalValues d=new DataRemoveDuplicateExperimentalValues();	
		d.removeDuplicates(records,sourceName);	
		
		ExperimentalRecords recordsBad = records.dumpBadRecords();

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
	
	// Runs createFiles() process from original records file, rather than recreating original records from source data
	public void createFilesFromOriginalRecords() {
		System.out.println("Going through original records");
		ExperimentalRecords records=goThroughOriginalRecords();
//		records.dontKeepNumericQualifierRecords();
		records.addSourceBasedIDNumbers();
		
		DataRemoveDuplicateExperimentalValues d=new DataRemoveDuplicateExperimentalValues();	
		d.removeDuplicates(records,sourceName);	
		
		ExperimentalRecords recordsBad = records.dumpBadRecords();

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
			
			strRecords=ExperimentalRecords.fixChars(strRecords);
			
			fw.write(strRecords);
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		ParseADDoPT.main(null);
		ParseAqSolDB.main(null);
		ParseBradley.main(null);
		ParseChemicalBook.main(null);
		ParseEChemPortalAPI.main(null);
		String[] lookChemArgs = {"General","PFAS"};
		ParseLookChem.main(lookChemArgs);
		ParseOChem.main(null);
		ParseOFMPub.main(null);
		ParseOPERA.main(null);
		ParsePubChem.main(null);
		ParseQSARDB.main(null);
		//ParseChemBL.main(null);
		//ParseChemidplus.main(null);
		//ParseSander.main(null);

		DataFetcher.main(null);
	}
}

