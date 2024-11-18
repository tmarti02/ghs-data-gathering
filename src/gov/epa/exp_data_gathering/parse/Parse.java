package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.eChemPortalAPI.Processing.FinalRecords;
import gov.epa.exp_data_gathering.parse.ADDoPT.ParseADDoPT;
import gov.epa.exp_data_gathering.parse.AqSolDB.ParseAqSolDB;
import gov.epa.exp_data_gathering.parse.Bagley.ParseBagley;
import gov.epa.exp_data_gathering.parse.Bradley.ParseBradley;
import gov.epa.exp_data_gathering.parse.Burkhard.ParseBurkhard;
import gov.epa.exp_data_gathering.parse.CFSAN.ParseCFSAN;
import gov.epa.exp_data_gathering.parse.ChemBL.ParseChemBL;
import gov.epa.exp_data_gathering.parse.ChemicalBook.ParseChemicalBook;
import gov.epa.exp_data_gathering.parse.Chemidplus.ParseChemidplus;
import gov.epa.exp_data_gathering.parse.DRD.ParseDRD;
import gov.epa.exp_data_gathering.parse.EChemPortal.ParseEChemPortal;
import gov.epa.exp_data_gathering.parse.EChemPortal.ParseEChemPortalAPI;
import gov.epa.exp_data_gathering.parse.EChemPortal.ToxParseEChemPortalAPI;
import gov.epa.exp_data_gathering.parse.EPISUITE.ParseEpisuiteISIS;
import gov.epa.exp_data_gathering.parse.EPISUITE.ParseEpisuiteOriginal;
import gov.epa.exp_data_gathering.parse.Hayashi.ParseHayashi;
import gov.epa.exp_data_gathering.parse.ICF.ParseICF;
import gov.epa.exp_data_gathering.parse.Kodithala.ParseKodithala;
import gov.epa.exp_data_gathering.parse.Lebrun.ParseLebrun;
import gov.epa.exp_data_gathering.parse.LookChem.ParseLookChem;
import gov.epa.exp_data_gathering.parse.NICEATM.ParseNICEATM;
import gov.epa.exp_data_gathering.parse.OChem.ParseOChem;
import gov.epa.exp_data_gathering.parse.OECD_Toolbox.ParseOECD_Toolbox;
import gov.epa.exp_data_gathering.parse.OECD_Toolbox.ParseOECD_Toolbox_SkinIrrit;
import gov.epa.exp_data_gathering.parse.OFMPub.ParseOFMPub;
import gov.epa.exp_data_gathering.parse.OPERA.ParseOPERA;
import gov.epa.exp_data_gathering.parse.PubChem.ParsePubChem;
import gov.epa.exp_data_gathering.parse.QSARDB.ParseQSARDB;
import gov.epa.exp_data_gathering.parse.Sander.ParseSander;
import gov.epa.exp_data_gathering.parse.Takahashi.ParseTakahashi;
import gov.epa.exp_data_gathering.parse.ThreeM.ParseThreeM;
import gov.epa.exp_data_gathering.parse.Verheyen.ParseVerheyen;

/**
 * @author gsinclair, tmartin, cramsland
 *
 */
public class Parse {
	public int maxExcelRows=65000;
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
	protected String fileNameExcelExperimentalRecordsCheck;
	protected String fileNameFlatExperimentalRecordsBad;
	protected String fileNameJsonExperimentalRecordsBad;
	public String mainFolder;
	
	public boolean generateOriginalJSONRecords=true; //runs code to generate json records from original data format (json file has all the chemicals in one file)	
	public boolean writeFlatFile=false;//all data converted to final format stored as flat text file
	public boolean writeJsonExperimentalRecordsFile=true;//all data converted to final format stored as Json file
	public boolean writeExcelExperimentalRecordsFile=true;//all data converted to final format stored as xlsx file
	public boolean writeCheckingExcelFile=true;
	public boolean writeExcelFileByProperty=false;

	protected Gson gson=null;
	protected UnitConverter uc=null;
	
	protected int howManyOriginalRecordsFiles=1;//TODO add code to calculate this later from list of files

	/**
	 * init method for parse class that specifies folder locations of the files to be found or written to.
	 * tool for consistent, readable json formatting is called here
	 */
	public void init() {
		fileNameJSON_Records = sourceName +" Original Records.json";
		fileNameFlatExperimentalRecords = sourceName +" Experimental Records.txt";
		fileNameFlatExperimentalRecordsBad = sourceName +" Experimental Records-Bad.txt";
		fileNameJsonExperimentalRecords = sourceName +" Experimental Records.json";
		fileNameJsonExperimentalRecordsBad = sourceName +" Experimental Records-Bad.json";
		fileNameExcelExperimentalRecords = sourceName +" Experimental Records.xlsx";

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmm");
		String timestamp = sdf.format(new Date());
		fileNameExcelExperimentalRecordsCheck = sourceName +" Experimental Records Check " + timestamp + ".xlsx";
		
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
	 * Loads original records from source files and stores as json file in original format
	 * 
	 * Needs to be overridden
	 */
	protected void createRecords() {
		System.out.println("Need to override createRecords()!");	
	}
	
	/**
	 * Loads original records from Json file and converts to ExperimentalRecords
	 * 
	 * Need to overridden
	 * @return
	 */
	protected ExperimentalRecords goThroughOriginalRecords() {
		System.out.println("Need to override goThroughOriginalRecords()!");
		return null;
	}
	
	/**
	 * the method called by every parse class to create the json and excel files that are saved to source specific main folder
	 * duplicate records are removed according to identical property_value_strings, original_source_name and property_name.
	 * 
	 */
	public void createFiles() {

		System.out.println(folderNameExcel);

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
			
			int dupCount=0;
			for(ExperimentalRecord er:records) {
				if(er.reason!=null && er.reason.contains("Duplicate of experimental value from same source")) {
					dupCount++;
				}
			}
 			System.out.println("\nDuplicates removed:"+dupCount);
			
		}
		
		records.getRecordsByProperty();
		
		if (writeExcelExperimentalRecordsFile) {

			System.out.println("Writing Excel file(s) for chemical records");

			if(writeExcelFileByProperty) {
				writeExcelRecordsByProperty(records);
			} else {
				records.toExcel_File_Split(mainFolder+File.separator+fileNameExcelExperimentalRecords,maxExcelRows);
			}
			
		}
		if (writeCheckingExcelFile) {
			records.createCheckingFile(records, mainFolder+File.separator+fileNameExcelExperimentalRecordsCheck,maxExcelRows);
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
			JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(records),mainFolder+File.separator+fileNameJsonExperimentalRecords);
			JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(recordsBad),mainFolder+File.separator+fileNameJsonExperimentalRecordsBad);
		}
		
		
		System.out.println("done\n");
	}

	private void writeExcelRecordsByProperty(ExperimentalRecords records) {
		Hashtable<String,ExperimentalRecords>ht=new Hashtable<>();
		
		for(ExperimentalRecord er:records) {
			if(ht.get(er.property_name)!=null) {
				ExperimentalRecords recs=ht.get(er.property_name);
				recs.add(er);
			} else {
				ExperimentalRecords recs=new ExperimentalRecords();
				recs.add(er);
				ht.put(er.property_name,recs);
			}
		}
		for(String property_name:ht.keySet()) {
			ExperimentalRecords property_records=ht.get(property_name);
			
			String property_name2=property_name.replace(":","_");//messes up filewriting 
			
			String filepath=mainFolder+File.separator+fileNameExcelExperimentalRecords.replace(".xlsx", " "+property_name2+".xlsx");
			property_records.toExcel_File_Split(filepath,maxExcelRows);
		}
	}
	
	/**
	 * writes original records to JSON format to the specified json folder
	 * @param records - a vector of original record objects (e.g. RecordLookChem)
	 */
	protected void writeOriginalRecordsToFile(Vector<?> records) {
		try {
			String jsonPath = jsonFolder + File.separator + fileNameJSON_Records;
			
//			System.out.println(records.size());
			
			howManyOriginalRecordsFiles = JSONUtilities.batchAndWriteJSON(records,jsonPath);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Parses multiple sources, calls the classes that parse their data into the experimentalrecords json and excel files
	 * @param sourceName a string with the source name matching those found in the ExperimentalConstants dictionary
	 * @param recordTypeToParse tox or physchem, those exact strings can be found in parsePhyschem and parseTox methods
	 */
	public static void runParse(String sourceName,String recordTypeToParse) {
		String[] toxSources = {
				ExperimentalConstants.strSourceChemidplus,
				ExperimentalConstants.strSourceEChemPortalAPI,
				ExperimentalConstants.strSourceNICEATM,
				ExperimentalConstants.strSourceOECD_Toolbox,
				ExperimentalConstants.strSourceCFSAN,
				ExperimentalConstants.strSourceLebrun,
				ExperimentalConstants.strSourceDRD,
				ExperimentalConstants.strSourceTakahashi,
				ExperimentalConstants.strSourceBagley,
				ExperimentalConstants.strSourceHayashi,
				ExperimentalConstants.strSourceKodithala,
				ExperimentalConstants.strSourceVerheyen,
				ExperimentalConstants.strSourceOECD_Toolbox_SkinIrrit,
				ExperimentalConstants.strSourceBurkhard
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
		case ExperimentalConstants.strSourceICF:
			p = new ParseICF();
			break;
		case ExperimentalConstants.strSource3M:
			p = new ParseThreeM();
			break;
		case ExperimentalConstants.strSourceBagley:
			p = new ParseBagley();
			break;
		case ExperimentalConstants.strSourceHayashi:
			p = new ParseHayashi();
			break;
		case ExperimentalConstants.strSourceKodithala:
			p = new ParseKodithala();
			break;
		case ExperimentalConstants.strSourceOECD_Toolbox_SkinIrrit:
			p = new ParseOECD_Toolbox_SkinIrrit(recordTypeToParse);
			break;
		case ExperimentalConstants.strSourceVerheyen:
			p = new ParseVerheyen();
			break;
//		case ExperimentalConstants.strSourceBurkhard:
//			p = new ParseBurkhard();
//			break;
		default:
			System.out.println("Need to add parse case for "+sourceName);
			return;
		}
			
		p.createFiles();
	}
	
	/**
	 * parses all physchem sources in the allsources string array
	 * sources in the reparsesources string array are parsed when the parsePhyschem method is called, sources not in reparsesources have existing saved files collected by data fetcher to build database
	 */
	static void parsePhyschem() { 
		String recordType = "physchem";
		String[] allSources = {ExperimentalConstants.strSourceADDoPT,
				// ExperimentalConstants.strSourceAqSolDB,
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
				ExperimentalConstants.strSourceEpisuiteISIS,
				ExperimentalConstants.strSourceICF,
				ExperimentalConstants.strSource3M,
				ExperimentalConstants.strSourceBurkhard,
				ExperimentalConstants.strSourceAqSolDB
};
		
		String[] reparseSources = {
				ExperimentalConstants.strSourceICF,
				ExperimentalConstants.strSource3M,
			};
		
		boolean reparse=true;
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
	
	/**
	 * parses all tox sources in the allsources string array
	 * sources in the parseSources string array are parsed when the parseTox method is called, sources not in parseSources have existing saved files collected by data fetcher to build database
	 */
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
				ExperimentalConstants.strSourceTakahashi,
				ExperimentalConstants.strSourceBagley,
				ExperimentalConstants.strSourceHayashi,
				ExperimentalConstants.strSourceKodithala,
				ExperimentalConstants.strSourceVerheyen,
				ExperimentalConstants.strSourceOECD_Toolbox_SkinIrrit,
				ExperimentalConstants.strSourceBurkhard};
						
		//Update echemportal data: 
		
		if (updateEchemportal) {
			Parse p = new ToxParseEChemPortalAPI(true);//downloads from API, stores in raw json db,  and then parses into json files
			p.createFiles();
		}
		
		String[] parseSources = {
				ExperimentalConstants.strSourceLebrun,
				ExperimentalConstants.strSourceDRD,
				ExperimentalConstants.strSourceBagley,
				ExperimentalConstants.strSourceHayashi,
				ExperimentalConstants.strSourceKodithala,
				ExperimentalConstants.strSourceVerheyen,
				ExperimentalConstants.strSourceOECD_Toolbox_SkinIrrit,
				ExperimentalConstants.strSourceBurkhard

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
		parsePhyschem();
//		parseTox();
//		runParse(ExperimentalConstants.strSourceADDoPT, "physchem");
	}
}

