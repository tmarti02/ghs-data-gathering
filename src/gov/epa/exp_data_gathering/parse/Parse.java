package gov.epa.exp_data_gathering.parse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.eChemPortalAPI.eChemPortalAPI;
import gov.epa.eChemPortalAPI.Processing.FinalRecords;
import gov.epa.exp_data_gathering.DRD.ParseDRD;
import gov.epa.exp_data_gathering.parse.ADDoPT.ParseADDoPT;
import gov.epa.exp_data_gathering.parse.AqSolDB.ParseAqSolDB;
import gov.epa.exp_data_gathering.parse.Bradley.ParseBradley;
import gov.epa.exp_data_gathering.parse.CFSAN.ParseCFSAN;
import gov.epa.exp_data_gathering.parse.ChemBL.ParseChemBL;
import gov.epa.exp_data_gathering.parse.ChemicalBook.ParseChemicalBook;
import gov.epa.exp_data_gathering.parse.Chemidplus.ParseChemidplus;
import gov.epa.exp_data_gathering.parse.EChemPortal.ParseEChemPortal;
import gov.epa.exp_data_gathering.parse.EChemPortal.ParseEChemPortalAPI;
import gov.epa.exp_data_gathering.parse.EChemPortal.ToxParseEChemPortalAPI;
import gov.epa.exp_data_gathering.parse.EPISUITE.ParseEpisuiteISIS;
import gov.epa.exp_data_gathering.parse.EPISUITE.ParseEpisuiteOriginal;
import gov.epa.exp_data_gathering.parse.Lebrun.ParseLebrun;
import gov.epa.exp_data_gathering.parse.LookChem.ParseLookChem;
import gov.epa.exp_data_gathering.parse.NICEATM.ParseNICEATM;
import gov.epa.exp_data_gathering.parse.OChem.ParseOChem;
import gov.epa.exp_data_gathering.parse.OECD_Toolbox.ParseOECD_Toolbox;
import gov.epa.exp_data_gathering.parse.OFMPub.ParseOFMPub;
import gov.epa.exp_data_gathering.parse.OPERA.ParseOPERA;
import gov.epa.exp_data_gathering.parse.PubChem.ParsePubChem;
import gov.epa.exp_data_gathering.parse.QSARDB.ParseQSARDB;
import gov.epa.exp_data_gathering.parse.Sander.ParseSander;
import gov.epa.exp_data_gathering.parse.Takahashi.ParseTakahashi;

public class Parse {
	
	public String sourceName;
	public String jsonFolder;
	public String databaseFolder;
	public String webpageFolder;
	
	public boolean removeDuplicates=true;

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
	
	public boolean generateOriginalJSONRecords=true; //runs code to generate json records from original data format (json file has all the chemicals in one file)	
	public boolean writeFlatFile=false;//all data converted to final format stored as flat text file
	public boolean writeJsonExperimentalRecordsFile=true;//all data converted to final format stored as Json file
	public boolean writeExcelExperimentalRecordsFile=true;//all data converted to final format stored as xlsx file
	
	protected Gson gson=null;
	protected UnitConverter uc=null;
	
	protected int howManyOriginalRecordsFiles=1;

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
		builder.setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues();
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
		if (generateOriginalJSONRecords) {
			System.out.println("Creating " + sourceName + " json files...");
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

		System.out.println("Going through original records...");
		ExperimentalRecords records=goThroughOriginalRecords();
		records.addSourceBasedIDNumbers();
		
		if (removeDuplicates) {
			DataRemoveDuplicateExperimentalValues d=new DataRemoveDuplicateExperimentalValues();	
			d.removeDuplicates(records,sourceName);
		}
		
		ExperimentalRecords recordsBad = records.dumpBadRecords();

		if (writeFlatFile) {
			System.out.println("Writing flat file for chemical records");
			records.toFlatFile(mainFolder+File.separator+fileNameFlatExperimentalRecords,"|");
			recordsBad.toFlatFile(mainFolder+File.separator+fileNameFlatExperimentalRecordsBad,"|");
		}
		
		if (writeJsonExperimentalRecordsFile) {
			System.out.println("Writing json file for chemical records");
			System.out.println(mainFolder+File.separator+fileNameJsonExperimentalRecords);
			batchAndWriteJSON(new Vector<ExperimentalRecord>(records),mainFolder+File.separator+fileNameJsonExperimentalRecords);
			batchAndWriteJSON(new Vector<ExperimentalRecord>(recordsBad),mainFolder+File.separator+fileNameJsonExperimentalRecordsBad);
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
			String jsonPath = jsonFolder + File.separator + fileNameJSON_Records;
			howManyOriginalRecordsFiles = batchAndWriteJSON(records,jsonPath);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static int batchAndWriteJSON(Vector<?> records, String baseFileName) {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues();
		Gson gson = builder.create();
		int batch = 0;
		
		if (records.size() <= 100000) {
			String jsonRecords = gson.toJson(records);
			writeJSONLineByLine(jsonRecords,baseFileName);
			batch = 1;
		} else {
			List<Object> temp = new ArrayList<Object>();
			Iterator<?> it = records.iterator();
			int i = 0;
			while (it.hasNext()) {
				temp.add(it.next());
				i++;
				if (i!=0 && i%100000==0) {
					batch++;
					String batchFileName = baseFileName.substring(0,baseFileName.indexOf(".")) + " " + batch + ".json";
					String jsonRecords = gson.toJson(temp);
					writeJSONLineByLine(jsonRecords,batchFileName);
					temp.clear();
				}
			}
			batch++;
			String batchFileName = baseFileName.substring(0,baseFileName.indexOf(".")) + " " + batch + ".json";
			String jsonRecords = gson.toJson(temp);
			writeJSONLineByLine(jsonRecords,batchFileName);
		}
		
		return batch;
	}
	
	private static void writeJSONLineByLine(String jsonRecords,String filePath) {
		String[] strRecords = jsonRecords.split("\n");
		
		File file = new File(filePath);
		if(!file.getParentFile().exists()) { file.getParentFile().mkdirs(); }
		
		try {
			// Clear existing file contents
			FileWriter fw = new FileWriter(filePath);
			fw.close();
			
			BufferedWriter bwAppend = new BufferedWriter(new FileWriter(filePath,true));
		
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
		String[] toxSources = {
				ExperimentalConstants.strSourceChemidplus,
				ExperimentalConstants.strSourceEChemPortalAPI,
				ExperimentalConstants.strSourceNICEATM,
				ExperimentalConstants.strSourceOECD_Toolbox,
				ExperimentalConstants.strSourceCFSAN,
				ExperimentalConstants.strSourceLebrun,
				ExperimentalConstants.strSourceDRD,
				ExperimentalConstants.strSourceTakahashi
		};
		
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
		case ExperimentalConstants.strSourceEpisuiteISIS:
			p = new ParseEpisuiteISIS();
			p.generateOriginalJSONRecords = true;
			break;
		case ExperimentalConstants.strSourceEpisuiteOriginal:
			p = new ParseEpisuiteOriginal();
			break;
		case ExperimentalConstants.strSourceNICEATM:
			p = new ParseNICEATM(recordTypeToParse);
			break;
		case ExperimentalConstants.strSourceOECD_Toolbox:
			p = new ParseOECD_Toolbox(recordTypeToParse);
			break;
		case ExperimentalConstants.strSourceCFSAN:
			p = new ParseCFSAN();
			break;
		case ExperimentalConstants.strSourceLebrun:
			p = new ParseLebrun();
			break;
		case ExperimentalConstants.strSourceDRD:
			p = new ParseDRD();
			break;
		case ExperimentalConstants.strSourceTakahashi:
			p = new ParseTakahashi();
			break;
		default:
			System.out.println("Need to add parse case for "+sourceName);
			return;
		}
			
		p.createFiles();
	}
	
	static void parsePhyschem() { 
		String recordType = "physchem";
		String[] allSources = {ExperimentalConstants.strSourceADDoPT,
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
				ExperimentalConstants.strSourceEpisuiteISIS};
		
		String[] reparseSources = {
				ExperimentalConstants.strSourcePubChem,
				ExperimentalConstants.strSourceLookChem};
		
		boolean reparse=false;
		boolean reparseAll=false;

		String [] parseSources=reparseSources;
		if (reparseAll) parseSources=allSources;

		if (reparse) {
			for (String s:parseSources) {
				runParse(s,recordType);
			}
		}
		
		DataFetcher d = new DataFetcher(allSources,recordType);
		d.createRecordsDatabase();
	}
	
	static void parseTox() { 
		
		boolean updateEchemportal=false;
		boolean reparse=true;
		
		String recordType = "tox";
		String[] allSources = {ExperimentalConstants.strSourceChemidplus,
				ExperimentalConstants.strSourceEChemPortalAPI,
				ExperimentalConstants.strSourceNICEATM,
				ExperimentalConstants.strSourceOECD_Toolbox,
				ExperimentalConstants.strSourceCFSAN,
				ExperimentalConstants.strSourceLebrun,
				ExperimentalConstants.strSourceDRD,
				ExperimentalConstants.strSourceTakahashi};
						
		//Update echemportal data: 
		
		if (updateEchemportal) {
			Parse p = new ToxParseEChemPortalAPI(true);//downloads from API, stores in raw json db,  and then parses into json files
			p.createFiles();
		}
		
		String[] parseSources = {
//				ExperimentalConstants.strSourceChemidplus,
//				ExperimentalConstants.strSourceNICEATM,
//				ExperimentalConstants.strSourceOECD_Toolbox ,
				ExperimentalConstants.strSourceCFSAN,
				ExperimentalConstants.strSourceLebrun,
				ExperimentalConstants.strSourceDRD,
				ExperimentalConstants.strSourceTakahashi
		};
				
		if (reparse) for (String s:parseSources) runParse(s,recordType);
				
		DataFetcher d = new DataFetcher(allSources,recordType);
				
		File f=new File(d.databasePath);
		if (f.exists()) f.delete();//Delete database so dont get duplicate records		
		
		d.createRecordsDatabase();//Create ExperimentalRecords table
		
		FinalRecords finalRecordsEChemPortal=d.getFinalRecordsFromNumberedFiles(ExperimentalConstants.strSourceEChemPortalAPI);
		System.out.println("FinalRecords from eChemportalAPI:"+finalRecordsEChemPortal.size());
		d.addFinalRecordsTableToDatabase(finalRecordsEChemPortal);
		

	}
	
	
	public static void main(String[] args) {
//		parsePhyschem();
		parseTox();
	}
}

