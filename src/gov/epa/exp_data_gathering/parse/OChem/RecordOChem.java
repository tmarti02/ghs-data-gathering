package gov.epa.exp_data_gathering.parse.OChem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.DownloadWebpageUtilities;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.PublicSource;
import gov.epa.exp_data_gathering.parse.TextUtilities;
import gov.epa.exp_data_gathering.parse.UnitConverter;

/**
 * Stores data from ochem.eu
 * @author GSINCL01
 *
 */
public class RecordOChem {
	
	String smiles;
	String casrn;
	String name;
	String propertyName;
	String propertyValue;
	String propertyUnit;
	String temperature;
	String temperatureUnit;
	String pressure;
	String pressureUnit;
	String pH;
	String measurementMethod;
	String articleID;
	String introducer;
	
	static final String lastUpdated = "4/2024";
	static final String sourceName = ExperimentalConstants.strSourceOChem_2024_04_03;
	
	static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues()
			.create();

	static final UnitConverter uc = new UnitConverter("Data" + File.separator + "density.txt");
	
	public Vector<RecordOChem> parseOChemQueriesFromExcel() {
		
		System.out.println("Enter parseOChemQueriesFromExcel");
		
		String folderNameExcel = "excel files";
		String mainFolder = "Data"+File.separator+"Experimental"+ File.separator + sourceName;
		String excelFolderPath = mainFolder + File.separator+folderNameExcel;
		File folder = new File(excelFolderPath);
		String[] filenames = folder.list();
		
		Vector<RecordOChem> recordsAll=new Vector<>();
		
		for (String filename:filenames) {
			if (filename.endsWith(".xls")) {
				recordsAll.addAll(parseExcel(excelFolderPath, filename));
			}
		}
		
		System.out.println("number of records="+recordsAll.size());
		return recordsAll;
	}

	private Vector<RecordOChem> parseExcel(String excelFolderPath, String filename) {
		try {
			String filepath = excelFolderPath+File.separator+filename;
			String date = DownloadWebpageUtilities.getStringCreationDate(filepath);
			
			
//			if (!date.equals(lastUpdated)) {
//				System.out.println(sourceName+" warning: Last updated date does not match creation date of file "+filename);
//			}
			
			FileInputStream fis = new FileInputStream(new File(filepath));
			Workbook wb = new HSSFWorkbook(fis);
			Sheet sheet = wb.getSheetAt(0);
			Row headerRow = sheet.getRow(0);
			
//			System.out.println("Got sheet for "+filename);
			
			String propertyName = "";
			
			List<String>headers=new ArrayList<>();
			
			int propertyValueIndex = -1;
			int propertyUnitIndex = -1;
			
			for (int col=0;col<headerRow.getLastCellNum();col++) {
				Cell cell=headerRow.getCell(col);
				String header=cell.getStringCellValue();
				headers.add(header);
				
				if(filename.toLowerCase().contains("logkow")) {
					if (header.equals("logPow")) {
						propertyName = header;
						propertyValueIndex = col;
						propertyUnitIndex = col+1;
					}
				} else if(filename.toLowerCase().contains("pka")) { 
					if (header.equals("pKa")) {
						propertyName = header;
						propertyValueIndex = col;
						propertyUnitIndex = col+1;
					}
				
				} else if (header.contains("{measured, converted}")) {
					propertyName = header.substring(0,header.indexOf("{")).trim();
					propertyValueIndex = col;
					propertyUnitIndex = col+1;
				}
			}
			
			if(propertyName==null) {
				System.out.println("Missing property name for "+filename);
			}
			
			
			int rows = sheet.getLastRowNum();
			
			Vector<RecordOChem> records=new Vector<RecordOChem>();

//			if(!filename.contentEquals("melting_point_1_500.xls")) {
//				return records;
//			}
//			System.out.println(filename+"\t"+propertyUnitIndex);
			
			
//			if(true) return records;
			
			for (int i = 1; i <= rows; i++) {
//				if(i%1000==0) System.out.println(i);
				Row row = sheet.getRow(i);

				RecordOChem ocr = new RecordOChem();

				ocr.propertyName = propertyName;
				
				ocr.smiles = getCellValue(row,headers.indexOf("SMILES"));
				ocr.casrn = getCellValue(row,headers.indexOf("CASRN"));
				ocr.name= StringEscapeUtils.escapeHtml4(getCellValue(row,headers.indexOf("NAME")));
				
				
				fixCAS();
				fixName();
				
				ocr.propertyValue = getCellValue(row,propertyValueIndex);
				ocr.propertyUnit = getCellValue(row,propertyUnitIndex);
				ocr.temperature = getCellValue(row,headers.indexOf("Temperature"));
				
				if(ocr.temperature!=null) {
					ocr.temperature=ocr.temperature.replace("=", "").trim();
					try {
						Double.parseDouble(ocr.temperature);
					} catch (Exception ex) {
						System.out.println("Failed to parse temperature="+ocr.temperature);
					}
				}
				
				ocr.temperatureUnit = getCellValue(row,headers.indexOf("UNIT {Temperature}"));	
				if(ocr.temperatureUnit!=null && ocr.temperatureUnit.equals("-")) ocr.temperatureUnit=null;
				
				ocr.pressure = getCellValue(row,headers.indexOf("Pressure"));
				if(ocr.pressure!=null) {
					ocr.pressure=ocr.pressure.replace("=", "").trim();
					try {
						Double.parseDouble(ocr.pressure);
					} catch (Exception ex) {
						System.out.println("Failed to parse pressure="+ocr.pressure);
					}
				}
				ocr.pressureUnit = getCellValue(row,headers.indexOf("UNIT {Pressure}"));
				if(ocr.pressureUnit!=null && ocr.pressureUnit.equals("-")) ocr.pressureUnit=null;//TMM: 6/7/24 fixed error where this set the propertyUnit to null

				ocr.pH = getCellValue(row,headers.indexOf("pH"));
				

				if(ocr.pH!=null) {
					ocr.pH=ocr.pH.replace("=", "").trim();
					try {
						Double.parseDouble(ocr.pH);
					} catch (Exception ex) {
						System.out.println("Failed to parse ph+"+ocr.pH);
					}
				}
				ocr.measurementMethod = getCellValue(row,headers.indexOf("measurement method"));
				ocr.articleID = getCellValue(row,headers.indexOf("ARTICLEID"));
				ocr.introducer = getCellValue(row,headers.indexOf("INTRODUCER"));
				
//				if(ocr.propertyUnit==null || ocr.propertyUnit.equals("-")) {
//					System.out.println(gson.toJson(ocr));
//				}
				
//				if(propertyName.equals("Density") && ocr.propertyUnit==null) {
//					ocr.propertyUnit="g/cm3";
////					System.out.println(ocr.casrn+"\t"+ocr.name+"\t"+ocr.propertyValue);
//				}
				
//					if(filename.equals("melting_point_1_500.xls"))
//				System.out.println(filename+"\t"+i+"\t"+ocr.propertyUnit);

				
				
				records.add(ocr);
			}

			System.out.println("\t"+filename+"\t"+records.size());
//			System.out.println(gson.toJson(records));
			
			wb.close();
			
			FileWriter fw=new FileWriter(excelFolderPath+File.separator+filename.replace(".xls", ".json"));
			fw.write(gson.toJson(records));
			fw.flush();
			fw.close();
			
			return records;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private void fixCAS() {
		
		
		if(casrn==null && name!=null) {
			String name2=name.replace("'", "").trim();//sometime cas is stored in name with single quotes around it

			if(ParseUtilities.isValidCAS(name2)) {
				casrn=name2;
				name=null;
				System.out.println("Fixed cas:"+casrn+"\t"+name2+"\t"+propertyName);
			}
			return;
		}
		
		if(casrn==null || casrn.isBlank()) return;
		
		if(!casrn.contains("-")) {
			System.out.println("no dash in cas: "+casrn);
		}
		
		if(!ParseUtilities.isValidCAS(casrn)) {
			System.out.println("invalid cas: "+casrn);
		}
		
		
//		System.out.println(casrn);
		
		//Trim off leading zeros:
		int counter=0;
		
		while (casrn.indexOf("0")==0) {
			counter++;
			casrn=casrn.substring(1,casrn.length());	
			System.out.println(counter);
			if(casrn.substring(1,2).equals("-")) break;
		}

		
	}
	
 ExperimentalRecord toExperimentalRecord(Hashtable<String, LiteratureSource> htRefs) {
		
		ExperimentalRecord er = new ExperimentalRecord();
		er.date_accessed = RecordOChem.lastUpdated;
		er.casrn = casrn;
		er.smiles = smiles;
		er.chemical_name = name;
		er.measurement_method = measurementMethod;
		er.source_name = RecordOChem.sourceName;
		er.url = "https://ochem.eu/home/show.do"; // How do we get individual OChem URLs?
		er.property_value_string = "Value: "+propertyValue+" "+propertyUnit;
		
		if(er.chemical_name!=null && er.chemical_name.contains(":") && er.chemical_name.contains("GMT")) {
			if(er.chemical_name.contains(";")) {
//				System.out.println("Bad name\t"+er.chemical_name);
				
				String []names=er.chemical_name.split(";");
				er.chemical_name=names[0];
				System.out.println("Name had date and semicolon, kept name: "+er.chemical_name);
				
			} else {
//				System.out.println("Bad name\t"+er.chemical_name);
				er.chemical_name=null;
			}
		} else if(er.chemical_name!=null && er.chemical_name.contains(";")) {
//			System.out.println("Name has semicolon: "+er.chemical_name);
			//chemical can be a mixture or this could be synonyms- hard to know which- so leave as is so it doesnt get mapped to dsstox
		}
		
		setPropertyName(er);
		setPropertyUnits(er);
		setPropertyValue(er);
	
		addExperimentalTemperature(er);
		addExperimentalPressure(er);
		addExperimental_pH(er);
		
		if(htRefs.containsKey(articleID)) {
			
			LiteratureSource ls=htRefs.get(articleID);
			
			if(ls.url!=null &&   
					!ls.url.contains("doi.org") && 
					!ls.url.contains("sciencedirect.com") && 
					!ls.url.contains("scopus.com") && 
					!ls.url.contains("nature.com") &&
					!ls.url.contains("springer.com") &&
					!ls.url.contains("/article") &&
					!ls.url.contains("doi/pdf") &&
					!ls.url.contains("pubmed.ncbi.nlm.nih.gov")) {//If it's not a journal article, make it a public source:
				
				PublicSource psO=new PublicSource();
				psO.name=ls.title;
				psO.description=ls.citation;
				psO.url=ls.url;
				er.publicSourceOriginal=psO;
				er.original_source_name=psO.name;//for output to excel
								
			} else {
				er.literatureSource=ls;
				er.reference=er.literatureSource.citation;//make it easier to display in spreadsheet			
			}
		}

		
		omitBasedOnArticle(er);
		omitBasedOnOriginalSource(er);
		
		if (!ParseUtilities.hasIdentifiers(er)) {
			er.keep = false;
			er.reason = "No identifiers";
		}
		
		if (er.measurement_method!=null && er.measurement_method.contains("est")) {
			er.updateNote(ExperimentalConstants.str_est);
			er.keep = false;
			er.reason = "Estimated";
		}
		
		
		uc.convertRecord(er);
		
		return er;
	}

	private void omitBasedOnOriginalSource(ExperimentalRecord er) {

		if (er.publicSourceOriginal == null)
			return;

		PublicSource psO = er.publicSourceOriginal;

		if (psO.name.contains("PhysProp")) {
			er.keep = false;
			er.reason = "Came from PhysProp";

		} else if (psO.name.contains("(EPI) Suite")) {
			er.keep = false;
			er.reason = "Came from EPISuite";

		} else {
//			System.out.println(psO.name+"\t"+psO.description);
		}
	}

 private void omitBasedOnArticle(ExperimentalRecord er) {

//	 if (introducer != null && introducer.equals("charochkina") && articleID != null
//			 && articleID.equals("A120907") && name != null && (casrn == null || casrn.isEmpty())) {
//		 er.keep = false;
//		 er.reason = "nonsense date name, no CAS, smiles only";
//		 System.out.println(er.reason+"\t"+er.chemical_name+"\t"+er.smiles+"\t"+er.casrn);
//	 }

	 if(er.literatureSource==null) return;
	 
	 if(er.literatureSource.citation.contains("T.E.S.T.")) {
		 er.keep=false;
		 er.reason="Came from T.E.S.T. dataset";
//		 System.out.println(er.reason);
	 }

	 //	if (er.property_name.equals(ExperimentalConstants.strWaterSolubility) && 
	 //			articleID!=null && articleID.equals("A108291") &&
	 //			introducer!=null && introducer.equals("mvashurina")) {
	 //		er.keep = false;
	 //		er.reason = "Bad TEST upload";
	 //	}

	 if(er.literatureSource.citation.contains("OPERA")) { //A111278
		 er.keep=false;
		 er.reason="Came from OPERA dataset";
//		 System.out.println(er.reason);
	 }


	 if(er.literatureSource.citation.contains("(EPI) Suite")) {//A5643
		 er.keep=false;
		 er.reason="Came from EPISUITE";
//		 System.out.println(er.reason);

	 }


}
	
	private void fixName() {
		if(name==null) return;
		if(name.isBlank()) return;
		
		if(name.contains(";")) {//TODO parse out CAS?
//			System.out.println(name);
		}
	}
	
	private static String getCellValue(Row row,int index) {
		if (index > -1) {
			Cell cell=row.getCell(index,MissingCellPolicy.RETURN_BLANK_AS_NULL);			
			if(cell==null) return null;
			
			String value=cell.getStringCellValue().trim();
			
			if(value.isBlank()) return null;
			else return value;
			
		} else {
			return null;
		}
	}
	
	private void addExperimental_pH(ExperimentalRecord er) {
		if (pH!=null && !pH.isBlank()) {
			er.property_value_string = er.property_value_string + "; pH: " + pH;
			er.pH = pH;
		}
	}

	private void addExperimentalPressure(ExperimentalRecord er) {

		if (pressure==null || pressure.isBlank()) return;

		er.property_value_string = er.property_value_string + "; Pressure: " + pressure + " " + pressureUnit;
		double pressure = Double.parseDouble(this.pressure.replaceAll("[^0-9.,E]",""));

		if (pressureUnit.equals("mm Hg") || pressureUnit.contains("Torr")) {
			er.pressure_mmHg = TextUtilities.formatDouble(pressure);
		} else if (pressureUnit.equals("atm")) {
			er.pressure_mmHg = TextUtilities.formatDouble(pressure*UnitConverter.atm_to_mmHg);
		} else if (pressureUnit.equals("Pa")) {
			er.pressure_mmHg = TextUtilities.formatDouble(pressure*UnitConverter.Pa_to_mmHg);
		} else if (pressureUnit.equals("megaPa")) {
			er.pressure_mmHg = TextUtilities.formatDouble(pressure*UnitConverter.megaPa_to_mmHg);				
		} else if (pressureUnit.equals("kPa")) {
			er.pressure_mmHg = TextUtilities.formatDouble(pressure*UnitConverter.kPa_to_mmHg);				
		} else if (pressureUnit.equals("hPa")) {
			er.pressure_mmHg = TextUtilities.formatDouble(pressure*UnitConverter.hPa_to_mmHg);				

		} else {
			System.out.println("Not handled pressure unit: "+pressureUnit);
		}

	}

	private void addExperimentalTemperature(ExperimentalRecord er) {
		if (temperature!=null && !temperature.isBlank()) {
			er.property_value_string = er.property_value_string + "; Temperature: " + temperature + " " + temperatureUnit;
			String cleanTemp = temperature.replaceAll("[^0-9.,E]","");
			double temp = 0.0;
			try {
				temp = Double.parseDouble(cleanTemp);
			} catch (NumberFormatException ex) {
				temperatureUnit = "";
			}
			
			if (temperatureUnit.contains("C")) {
				er.temperature_C = temp;
			} else if (temperatureUnit.contains("K")) {
				er.temperature_C = UnitConverter.K_to_C(temp);
			} else if (temperatureUnit.contains("F")) {
				er.temperature_C = UnitConverter.F_to_C(temp);
			} else if(temperatureUnit.equals("Log unit")) {
				if(temp>=20 && temp<=30) {
					er.temperature_C=temp;
//					System.out.println("Temp units are log unit by mistake but temp is ok");
				}
			
			} else {
				System.out.println("Not handled temperature unit: "+temperatureUnit+"\t"+temperature+"\t"+propertyName);
			}
		}
	}
	
	
	private void setPropertyValue(ExperimentalRecord er) {
		if(propertyValue.substring(0,1).equals("<")) {
			er.property_value_numeric_qualifier="<";
			er.property_value_point_estimate_original = Double.parseDouble(propertyValue.substring(1,propertyValue.length()));
		} else if(propertyValue.substring(0,1).equals(">")) {
			er.property_value_numeric_qualifier=">";
			er.property_value_point_estimate_original = Double.parseDouble(propertyValue.substring(1,propertyValue.length()));
		} else if(propertyValue.contains(" - ")) {			
			er.property_value_min_original=Double.parseDouble(propertyValue.substring(0,propertyValue.indexOf(" ")));
			er.property_value_max_original=Double.parseDouble(propertyValue.substring(propertyValue.indexOf(" - ")+3,propertyValue.length()));
//			System.out.println("Found range:"+propertyValue);
//			System.out.println("Found range: "+er.property_value_min_original+" to "+er.property_value_max_original);			
		} else if(propertyValue.contains(" +-")) {
			er.property_value_point_estimate_original = Double.parseDouble(propertyValue.substring(0,propertyValue.indexOf(" +-")));
		} else {
			
			try {
				er.property_value_point_estimate_original = Double.parseDouble(propertyValue);
			} catch (Exception ex) {
				System.out.println("Error parsing propertyValue: "+propertyValue);
			}
		}
	}

	private void setPropertyUnits(ExperimentalRecord er) {
		
		if(propertyUnit==null) {
//			System.out.println("Missing property unit:"+propertyUnit+"\t"+propertyName);
//			System.out.println(gson.toJson(this));
			er.keep=false;
			er.reason="Units are missing";
			return;
		}
		
		switch (propertyUnit.trim()) {
		case "Celsius":
			er.property_value_units_original = ExperimentalConstants.str_C;
			break;
		case "g/cm3":
			er.property_value_units_original = ExperimentalConstants.str_g_cm3;
			break;
		case "g/L":
			er.property_value_units_original = ExperimentalConstants.str_g_L;
			break;
		case "Log unit":
			er.property_value_units_original=ExperimentalConstants.str_LOG_UNITS;
			break;
		case "lg(m^(3)*atm/mol)": 
			er.property_value_units_original = ExperimentalConstants.str_log_atm_m3_mol;
			break;
		case "m^(3)*Pa/mol":
			er.property_value_units_original = ExperimentalConstants.str_Pa_m3_mol;
			break;
		case "-log(M)":
			er.property_value_units_original = ExperimentalConstants.str_neg_log_M;
			break;
		case "mm Hg":
			er.property_value_units_original = ExperimentalConstants.str_mmHg;
			break;
			
		case "/":
			er.property_value_units_original = "unknown";//some logKow values look like log values and some look not log value. Toss!
			er.keep=false;
			er.reason="Units weren't specified";
			break;

		default:
			System.out.println("Unknown property unit:"+propertyUnit+"\t"+propertyName);
			System.out.println(gson.toJson(this));
		}
	}

	private void setPropertyName(ExperimentalRecord er) {
		
		switch (propertyName.trim().toLowerCase()) {
		
		case "melting point":
			er.property_name = ExperimentalConstants.strMeltingPoint;
			break;
		case "boiling point":
			er.property_name = ExperimentalConstants.strBoilingPoint;
			break;
		case "flash point":
			er.property_name = ExperimentalConstants.strFlashPoint;
			break;
		case "density":
			er.property_name = ExperimentalConstants.strDensity;
			break;
		case "water solubility":
			er.property_name = ExperimentalConstants.strWaterSolubility;
			break;
		case "vapor pressure":
			er.property_name = ExperimentalConstants.strVaporPressure;
			break;
		case "pka":
			er.property_name = ExperimentalConstants.str_pKA;
			break;
		case "logpow":
			er.property_name = ExperimentalConstants.strLogKOW;
			break;
		case "henry's law constant":
			er.property_name = ExperimentalConstants.strHenrysLawConstant;
			break;
		
		default:
			System.out.println("unknown property:" +propertyName);
			return;
		}
	}
	public static void main(String[] args) {
		RecordOChem r=new RecordOChem();
		Vector<RecordOChem> records = r.parseOChemQueriesFromExcel();
		
//		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
//		for (RecordOChem r:records) {
//			System.out.println(gson.toJson(r));
//		}
		
//		String excelFilePath="data\\experimental\\OChem_2024_04_03\\excel files";		
////		String filename="watersolubility.xls";
////		String filename="boilingpoint.xls";
//		String filename="vaporpressuremmHg.xls";
//		parseExcel(excelFilePath, filename);
	}
}
