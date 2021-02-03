package gov.epa.exp_data_gathering.parse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;

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
	public String mainFolder;
	
	public static boolean generateOriginalJSONRecords=true; //runs code to generate json records from original data format (json file has all the chemicals in one file)	
	public static boolean writeFlatFile=false;//all data converted to final format stored as flat text file
	public static boolean writeJsonExperimentalRecordsFile=true;//all data converted to final format stored as Json file
	public static boolean writeExcelExperimentalRecordsFile=true;//all data converted to final format stored as xlsx file
	
	protected Gson gson=null;
	protected UnitConverter uc=null;

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
		
		uc = new UnitConverter("Data" + File.separator + "density.txt");
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
						String batchFileName = fileNameExcelExperimentalRecords.substring(0,fileNameExcelExperimentalRecords.indexOf(".")) + " " + batch + ".xlsx";
						temp.toExcel_File(mainFolder+File.separator+batchFileName);
						temp.clear();
					}
				}
				batch++;
				String batchFileName = fileNameExcelExperimentalRecords.substring(0,fileNameExcelExperimentalRecords.indexOf(".")) + " " + batch + ".xlsx";
				temp.toExcel_File(mainFolder+File.separator+batchFileName);
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
			
			// Clear existing file contents
			FileWriter fw = new FileWriter(jsonPath);
			fw.close();
			
			BufferedWriter bwAppend = new BufferedWriter(new FileWriter(jsonPath,true));
			
			String[] strRecords=gson.toJson(records).split("\n");
			for (String s:strRecords) {
				s=ParseUtilities.fixChars(s);
				bwAppend.write(s+"\n");
			}
			bwAppend.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void runParse(String sourceName,String recordTypeToParse) {
		String[] toxSources = {ExperimentalConstants.strSourceChemidplus,ExperimentalConstants.strSourceEChemPortalAPI};
		if (!Arrays.asList(toxSources).contains(sourceName) && recordTypeToParse.toLowerCase().contains("tox")) {
			System.out.println("Warning: No toxicity data in "+sourceName+".");
			return;
		}
		Parse p = null;
		switch (sourceName) {
		case ExperimentalConstants.strSourceADDoPT:
			p = new ParseADDoPT();
			break;
		case ExperimentalConstants.strSourceAqSolDB:
			p = new ParseAqSolDB();
			break;
		case ExperimentalConstants.strSourceBradley:
			p = new ParseBradley();
			break;
		case ExperimentalConstants.strSourceChemBL:
			p = new ParseChemBL();
			break;
		case ExperimentalConstants.strSourceChemicalBook:
			p = new ParseChemicalBook();
			p.generateOriginalJSONRecords = false;
			break;
		case ExperimentalConstants.strSourceChemidplus:
			p = new ParseChemidplus(recordTypeToParse);
			break;
		case ExperimentalConstants.strSourceEChemPortal:
			System.out.println("Warning: Parsing eChemPortal Excel download results. Did you want eChemPortal API results instead?");
			p = new ParseEChemPortal();
			break;
		case ExperimentalConstants.strSourceEChemPortalAPI:
			if (recordTypeToParse.toLowerCase().contains("tox")) {
				p = new ToxParseEChemPortalAPI(false);
			} else {
				p = new ParseEChemPortalAPI();
			}
			break;
		case ExperimentalConstants.strSourceLookChem:
			String[] arr = {"General","PFAS"};
			p = new ParseLookChem(arr);
			break;
		case ExperimentalConstants.strSourceOChem:
			p = new ParseOChem();
			break;
		case ExperimentalConstants.strSourceOFMPub:
			p = new ParseOFMPub();
			break;
		case ExperimentalConstants.strSourceOPERA:
			p = new ParseOPERA();
			p.generateOriginalJSONRecords = false;
			break;
		case ExperimentalConstants.strSourcePubChem:
			p = new ParsePubChem();
			break;
		case ExperimentalConstants.strSourceQSARDB:
			p = new ParseQSARDB();
			break;
		case ExperimentalConstants.strSourceSander:
			p = new ParseSander();
			break;
		case ExperimentalConstants.strSourceEpisuite:
			p = new ParseEpisuiteISIS();
			break;
		}
		p.createFiles();
	}
	
	public static void main(String[] args) {
		String recordType = "physchem";
		String[] sources = {ExperimentalConstants.strSourceADDoPT,
				ExperimentalConstants.strSourceAqSolDB,
				ExperimentalConstants.strSourceBradley,
				ExperimentalConstants.strSourceChemicalBook,
				ExperimentalConstants.strSourceChemidplus,
				ExperimentalConstants.strSourceEChemPortalAPI,
				ExperimentalConstants.strSourceLookChem,
				ExperimentalConstants.strSourceOChem,
				ExperimentalConstants.strSourceOFMPub,
				ExperimentalConstants.strSourceOPERA,
				ExperimentalConstants.strSourcePubChem,
				ExperimentalConstants.strSourceQSARDB,
				ExperimentalConstants.strSourceSander,
				ExperimentalConstants.strSourceEpisuite};
		for (String s:sources) {
			runParse(s,recordType);
		}
		DataFetcher d = new DataFetcher(sources,recordType);
		d.createRecordsDatabase();
	}
}

