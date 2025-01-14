package gov.epa.exp_data_gathering.parse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;

import com.google.gson.JsonObject;


/**
 * Class to read data sources provided as a single, column-based Excel
 * spreadsheet
 * 
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class ExcelSourceReader {
	public String sourceName;
	public static String lastUpdated;

	private String sourceFolderPath;
	private String fileName;
	public Sheet sheet;
	
	public int headerRowNum=0;

	boolean omitColNoData=false;
	
	public ExcelSourceReader() {}

	/**
	 * Initializes a new reader for the given source from the given filename NOTE:
	 * Currently can only read a single sheet from a single file
	 * 
	 * @param fileName   The file to read records from
	 * @param sourceName The data source to assign records to
	 */
	public ExcelSourceReader(String fileName, String sourceName) {
		this.sourceName = sourceName;
		this.fileName = fileName;
		sourceFolderPath = "data" + File.separator + "experimental" + File.separator + sourceName;

		String filePath = sourceFolderPath + File.separator + "excel files" + File.separator + fileName;
		this.lastUpdated = DownloadWebpageUtilities.getStringCreationDate(filePath); // TODO add lastUpdated as
																						// parameter instead?
		try {

			FileInputStream fis = new FileInputStream(new File(filePath));
			Workbook wb = WorkbookFactory.create(fis);
//			XSSFWorkbook wb = new XSSFWorkbook(filePath); 

			sheet = wb.getSheetAt(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes a new reader for the given source from the given filename NOTE:
	 * Currently can only read a single sheet from a single file
	 * 
	 * @param fileName   The file to read records from
	 * @param sourceName The data source to assign records to
	 */
	public ExcelSourceReader(String fileName, String mainFolderPath, String sourceName) {
		this.sourceName = sourceName;
		this.fileName = fileName;

		sourceFolderPath = mainFolderPath + File.separator + sourceName;

		String filePath = sourceFolderPath + File.separator + "excel files" + File.separator + fileName;

		System.out.println(filePath);

		this.lastUpdated = DownloadWebpageUtilities.getStringCreationDate(filePath); // TODO add lastUpdated as
																						// parameter instead?
		try {
			FileInputStream fis = new FileInputStream(new File(filePath));
			Workbook wb = WorkbookFactory.create(fis);
			sheet = wb.getSheetAt(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
//	public ExcelSourceReader(String fileName, String mainFolderPath, String sourceName,String sheetName,int headerRowNum) {
//		this.sourceName = sourceName;
//		this.fileName = fileName;
//
//		sourceFolderPath = mainFolderPath + File.separator + sourceName;
//
//		String filePath = sourceFolderPath + File.separator + "excel files" + File.separator + fileName;
//
//		System.out.println(filePath);
//
//		this.lastUpdated = DownloadWebpageUtilities.getStringCreationDate(filePath); // TODO add lastUpdated as
//																						// parameter instead?
//		try {
//			FileInputStream fis = new FileInputStream(new File(filePath));
//			Workbook wb = WorkbookFactory.create(fis);
//			sheet = wb.getSheet(sheetName);
//			
//			this.headerRowNum=headerRowNum;
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * Initializes a new reader for the given source from the given filename NOTE:
	 * Currently can only read a single sheet from a single file
	 * 
	 * @param fileName   The file to read records from
	 * @param sourceName The data source to assign records to
	 */
	public ExcelSourceReader(String fileName, String mainFolderPath, String sourceName, String sheetName) {
		this.sourceName = sourceName;
		this.fileName = fileName;

		sourceFolderPath = mainFolderPath + File.separator + sourceName;

		String filePath = sourceFolderPath + File.separator + "excel files" + File.separator + fileName;

		System.out.println(filePath);

		this.lastUpdated = DownloadWebpageUtilities.getStringCreationDate(filePath); // TODO add lastUpdated as
																						// parameter instead?
		try {
			FileInputStream fis = new FileInputStream(new File(filePath));
			Workbook wb = WorkbookFactory.create(fis);
			sheet = wb.getSheet(sheetName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes a new reader for the given source from the given filename NOTE:
	 * Currently can only read a single sheet from a single file
	 * 
	 * @param fileName   The file to read records from
	 * @param sourceName The data source to assign records to
	 */
	public void getSheet(String excelFilePath, int sheetNum) {

		lastUpdated = DownloadWebpageUtilities.getStringCreationDate(excelFilePath); // TODO add lastUpdated as
																						// parameter instead?
		try {
			FileInputStream fis = new FileInputStream(new File(excelFilePath));
			Workbook wb = WorkbookFactory.create(fis);
			sheet = wb.getSheetAt(sheetNum);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

//	/**
//	 * Writes records from a spreadsheet to JSON original records format consistent with field names of an existing Record[SourceName] class
//	 * @param hmFieldNames	Matches column numbers to output fields of a Record[SourceName] class
//	 * @param chemicalNameIndex		Column index containing chemical names (for special escape character treatment)
//	 */
//	public Vector<JsonObject> parseRecordsFromExcel(HashMap<Integer,String> hmFieldNames, int chemicalNameIndex) {
//		Vector<JsonObject> records = new Vector<JsonObject>();
//		try {
//			int numRows = sheet.getLastRowNum();
//			for (int i = 1; i <= numRows; i++) {
//				Row row = sheet.getRow(i);
//				if (row==null) { continue; }
//				JsonObject jo = new JsonObject();
//				boolean hasAnyFields = false;
//				for (int k:hmFieldNames.keySet()) {
//					Cell cell = row.getCell(k);
//					if (cell==null) { continue; }
//					cell.setCellType(CELL_TYPE_STRING);
//					String content = "";
//					if (k==chemicalNameIndex) {
//						content = StringEscapeUtils.escapeHtml4(row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
//					} else {
//						content = row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
//					}
////					Hyperlink url  = row.getCell(k).getHyperlink();
////					if (url!=null) {
////						System.out.println(hmFieldNames.get(k)+"_url"+"\t"+url.getLabel());
////						jo.addProperty(hmFieldNames.get(k)+"_url", url.getAddress());
////					}
//					
//					if (content!=null && !content.isBlank()) { hasAnyFields = true; }
//					jo.addProperty(hmFieldNames.get(k), content);
//				}
//				if (hasAnyFields) { records.add(jo); }
//			}
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		return records;
//	}

	/**
	 * Writes records from a spreadsheet to JSON original records format consistent
	 * with field names of an existing Record[SourceName] class
	 * 
	 * @param hmFieldNames      Matches column numbers to output fields of a
	 *                          Record[SourceName] class
	 * @param chemicalNameIndex Column index containing chemical names (for special
	 *                          escape character treatment)
	 */
	public static Vector<JsonObject> parseRecordsFromExcel(Sheet sheet, HashMap<Integer, String> hmFieldNames) {
		Vector<JsonObject> records = new Vector<JsonObject>();
		try {
			int numRows = sheet.getLastRowNum();
			for (int i = 1; i <= numRows; i++) {
				Row row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				JsonObject jo = new JsonObject();
				boolean hasAnyFields = false;
				for (int k : hmFieldNames.keySet()) {
					Cell cell = row.getCell(k);
					if (cell == null) {
						continue;
					}

					String content = "";

					try {

						CellType type = cell.getCellType();
						if (type == CellType.STRING) {
							content = cell.getStringCellValue();
						} else if (type == CellType.NUMERIC) {
							content = cell.getNumericCellValue() + "";
						} else if (type == CellType.BOOLEAN) {
							content = cell.getBooleanCellValue() + "";
						} else if (type == CellType.BLANK) {
							content = "";
						}

//						content = row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
					} catch (Exception ex) {
						System.out.println(hmFieldNames.get(k) + "\t" + ex.getMessage());
					}

//					if (k==chemicalNameIndex) {
//						content = StringEscapeUtils.escapeHtml4(row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
//					} else {
//						content = row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
//					}

//					content = row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();

					if (content != null && !content.isBlank()) {
						hasAnyFields = true;
					}
					jo.addProperty(hmFieldNames.get(k), content);
				}
				if (hasAnyFields) {
					records.add(jo);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}

	/**
	 * Writes records from a spreadsheet to JSON original records format consistent
	 * with field names of an existing Record[SourceName] class
	 * 
	 * @param hmFieldNames      Matches column numbers to output fields of a
	 *                          Record[SourceName] class
	 * @param chemicalNameIndex Column index containing chemical names (for special
	 *                          escape character treatment)
	 */
	public static Vector<JsonObject> parseRecordsFromExcel(Sheet sheet) {
		Vector<JsonObject> records = new Vector<JsonObject>();
		try {
			int numRows = sheet.getLastRowNum();

			Row row0 = sheet.getRow(0);

			HashMap<Integer, String> hmFieldNames = new HashMap<Integer, String>();

			for (int i = 0; i < row0.getLastCellNum(); i++) {
				Cell celli = row0.getCell(i);
				String colName = celli.getStringCellValue();
				hmFieldNames.put(i, colName);
				System.out.println(i + "\t" + colName);
			}

			for (int i = 1; i <= numRows; i++) {
				Row row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				JsonObject jo = new JsonObject();
				boolean hasAnyFields = false;
				for (int k : hmFieldNames.keySet()) {
					Cell cell = row.getCell(k);
					if (cell == null) {
						continue;
					}

					String content = "";

					try {

						CellType type = cell.getCellType();
						if (type == CellType.STRING) {
							content = cell.getStringCellValue();
						} else if (type == CellType.NUMERIC) {
							content = cell.getNumericCellValue() + "";
						} else if (type == CellType.BOOLEAN) {
							content = cell.getBooleanCellValue() + "";
						} else if (type == CellType.BLANK) {
							content = "";
						}
//						content = row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
					} catch (Exception ex) {
						System.out.println(hmFieldNames.get(k) + "\t" + ex.getMessage());
					}

					// if (k==chemicalNameIndex) {
//						content = StringEscapeUtils.escapeHtml4(row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
//					} else {
//						content = row.getCell(k,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
//					}

					content = row.getCell(k, MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();

					if (content != null && !content.isBlank()) {
						hasAnyFields = true;
					}
					jo.addProperty(hmFieldNames.get(k), content);
				}
				if (hasAnyFields) {
					records.add(jo);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}

	/**
	 * Writes records from a spreadsheet to JSON original records format consistent
	 * with field names of an existing Record[SourceName] class
	 * 
	 * @param hmFieldNames      Matches column numbers to output fields of a
	 *                          Record[SourceName] class
	 * @param chemicalNameIndex Column index containing chemical names (for special
	 *                          escape character treatment)
	 */
	public Vector<JsonObject> parseRecordsFromExcel(HashMap<Integer, String> hmFieldNames, int chemicalNameIndex,
			boolean setBlankToNull) {
		Vector<JsonObject> records = new Vector<JsonObject>();
		FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();

		int numRows = sheet.getLastRowNum();
		
		for (int i = headerRowNum+1; i <= numRows; i++) {
			Row row = sheet.getRow(i);
			if (row == null) {
				continue;
			}
			JsonObject jo = new JsonObject();
			boolean hasAnyFields = false;
			for (int k : hmFieldNames.keySet()) {
				Cell cell = row.getCell(k);
				if (cell == null) {
					continue;
				}
				// cell.setCellType(CELL_TYPE_STRING);

				String content = "";

				try {
					if (k == chemicalNameIndex) {
						content = StringEscapeUtils.escapeHtml4(
								row.getCell(k, MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
					} else {
						content = getContent(setBlankToNull, evaluator, cell);
					}
					
					if(content.contentEquals("filtered out")) content=null;

				} catch (Exception ex) {
					System.out.println("Error parsing for col " + k + "\tfor row " + i);
				}

				// if(content.contains("Cadmium sulphate")) System.out.println("here1:
				// "+content);

				if (content != null && !content.isBlank()) {
					hasAnyFields = true;
					content=content.trim();
				}
				jo.addProperty(hmFieldNames.get(k), content);
				
			}
			
			jo.addProperty("lastUpdated", lastUpdated);

			
			if (hasAnyFields) {
				records.add(jo);
			}
		}
		return records;
	}

	private String getContent(boolean setBlankToNull, FormulaEvaluator evaluator, Cell cell) {

		CellType type = cell.getCellType();
		
		String content=null;
		if (type == CellType.STRING) {
			content = cell.getStringCellValue();
		} else if (type == CellType.NUMERIC) {
			content = cell.getNumericCellValue() + "";
		} else if (type == CellType.BOOLEAN) {
			content = cell.getBooleanCellValue() + "";
		} else if (type == CellType.BLANK) {
			content = "";
			if (setBlankToNull)
				content = null;
		} else if (type == CellType.FORMULA) {//2024-01-23 (TMM)
			type = evaluator.evaluateFormulaCell(cell);
			if (type == CellType.STRING) {
				content = cell.getStringCellValue();
			} else if (type == CellType.NUMERIC) {
				content = cell.getNumericCellValue() + "";
			} else if (type == CellType.BOOLEAN) {
				content = cell.getBooleanCellValue() + "";
			} else if (type == CellType.BLANK) {
				content = "";
				if (setBlankToNull)
					content = null;
			}
		}
		return content;
	}

	/**
	 * Writes records from a spreadsheet to JSON original records format consistent
	 * with field names of an existing Record[SourceName] class
	 * 
	 * @param hmFieldNames      Matches column numbers to output fields of a
	 *                          Record[SourceName] class
	 * @param chemicalNameIndex Column index containing chemical names (for special
	 *                          escape character treatment)
	 */
	public Vector<JsonObject> parseRecordsFromExcel(HashMap<Integer, String> hmFieldNames, int chemicalNameIndex) {
		return parseRecordsFromExcel(hmFieldNames, chemicalNameIndex, true);// TODO is this desired default behavior?
																			// set blanks to null?
	}

	public static String fixSpecialChars(String content) {
//		if(content.contains("(second CAS# 31119-53-6)")) System.out.println("here1:"+"\t"+fieldName+"\t"+content);
		if (content == null)
			return content;
		content = content.replace("\r", " ").replace("\n", " ");
		while (content.contains("  ")) {
			content = content.replace("  ", " ");
		}
		return content;
	}

	/**
	 * Writes records from a spreadsheet to JSON original records format assuming
	 * the template created by generateRecordClassTemplate()
	 * 
	 * @param chemicalNameIndex Column index containing chemical names (for special
	 *                          escape character treatment)
	 */
	public Vector<JsonObject> parseRecordsFromExcel(int chemicalNameIndex) {
		List<String> fieldNames = getHeaders(sheet,headerRowNum);
		HashMap<Integer, String> hm = generateDefaultMap(fieldNames, 0);
		return parseRecordsFromExcel(hm, chemicalNameIndex, false);
	}
	
	public Vector<JsonObject> parseRecordsFromExcel(String colNameChemicalName) {
		List<String> fieldNames = getHeaders(sheet,headerRowNum);
		HashMap<Integer, String> hm = generateDefaultMap(fieldNames, 0);
		
		int chemicalNameIndex=fieldNames.indexOf(colNameChemicalName);
		
		return parseRecordsFromExcel(hm, chemicalNameIndex, false);
	}

	public Vector<JsonObject> parseRecordsFromExcel(int chemicalNameIndex, boolean setBlankToNull) {
		List<String> fieldNames = getHeaders(sheet,headerRowNum);
		HashMap<Integer, String> hm = generateDefaultMap(fieldNames, 0);
		return parseRecordsFromExcel(hm, chemicalNameIndex, setBlankToNull);
	}

	/**
	 * Gets column headers in appropriate format for field naming (alphanumeric and
	 * _ only)
	 * 
	 * @return Formatted column headers as a string array
	 */
	public List<String> getHeaders( Sheet sheet, int headerRowNum) {
		Row headerRow = sheet.getRow(headerRowNum);
		int numHeaders = headerRow.getLastCellNum();
		
//		String[] headers = new String[numHeaders];
		List<String> headers = new ArrayList<>();
		
		
		FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();

		
		for (int i = 0; i < numHeaders; i++) {
			Cell headerCell = headerRow.getCell(i, MissingCellPolicy.CREATE_NULL_AS_BLANK);

//			System.out.println(headerCell.getStringCellValue());

//			headerCell.setCellType(CELL_TYPE_STRING);
			
			String headerContent=getContent(true,evaluator,headerCell);
			
			if(headerContent==null) {
				break;
			}
			
			headerContent=headerContent.trim().replaceAll("[^\\p{Alnum}]+", "_")
					.replaceAll("^_", "").replaceAll("_$", "");
			
			if (headerContent == null || headerContent.equals("_") || headerContent.equals("")) {
				headers.add("field" + i);
			} else {
				headers.add(headerContent);
			}
		}
		return headers;
	}

	/**
	 * Generates a default map from column number to field name, i.e. field names in
	 * same order as columns and none skipped Offset allows skipping blank columns
	 * at beginning of sheet
	 * 
	 * @param fieldNames The field names of the Record[SourceName] class
	 * @param offset     The number of blank columns at the beginning of the sheet
	 * @return A map from column number to field names
	 */
	public static HashMap<Integer, String> generateDefaultMap(List<String> fieldNames, int offset) {
		HashMap<Integer, String> hmFieldNames = new HashMap<Integer, String>();
		for (int i = 0; i < fieldNames.size(); i++) {
			hmFieldNames.put(i + offset, fieldNames.get(i));
		}
		return hmFieldNames;
	}
	
	public static HashMap<Integer, String> generateDefaultMap(String[] fieldNames, int offset) {
		HashMap<Integer, String> hmFieldNames = new HashMap<Integer, String>();
		for (int i = 0; i < fieldNames.length; i++) {
			hmFieldNames.put(i + offset, fieldNames[i]);
		}
		return hmFieldNames;
	}

	/**
	 * Creates templates for Record[SourceName] and Parse[SourceName] classes and
	 * saves them as TXT files
	 */
	public void createClassTemplateFiles(List<String>headers) {
		writeClassTemplateFile("Record",headers);
		writeClassTemplateFile("Parse",headers);
	}

	/**
	 * Helper method to write class templates
	 * 
	 * @param classType The class type (i.e. "Record" or "Parse")
	 */
	private void writeClassTemplateFile(String classType,List<String>headers) {
		String classTemplate = "";
		switch (classType) {
		case "Record":
			classTemplate = generateRecordClassTemplate(headers);
			break;
		case "Parse":
			classTemplate = generateParseClassTemplate();
		}
//		String templateFilePath = sourceFolderPath + File.separator + classType + sourceName + "_ClassTemplate.txt";
//		String templateFilePath = sourceFolderPath + File.separator + classType + sourceName + ".javaX";
		
		String packagePath="src\\gov\\epa\\exp_data_gathering\\parse\\"+sourceName;
		File packageFolder=new File(packagePath);
		packageFolder.mkdir();
		
		String templateFilePath ="src\\gov\\epa\\exp_data_gathering\\parse\\"+sourceName+"\\"+classType + sourceName + ".java_txt";

		File file = new File(templateFilePath);
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write(classTemplate);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Wrote " + classType + sourceName + " template to " + templateFilePath);
	}
	
	
	public static  List<String> getAllHeadersFromExcelFilesInFolder(List<String>sheetNames,int headerRowNum, File Folder) {
		List<String>allHeaders=new ArrayList<>();
				
		for (File file:Folder.listFiles()) {
			if(!file.getName().contains(".xls")) continue;
			ExcelSourceReader esr=new ExcelSourceReader();
			
			try {
				FileInputStream fis = new FileInputStream(file);
				Workbook wb = WorkbookFactory.create(fis);
				
				
				for(String sheetName:sheetNames) {
					if(wb.getSheet(sheetName)!=null) {
						Sheet sheet = wb.getSheet(sheetName);
						List<String>headers= esr.getHeaders(sheet,headerRowNum);	
						for(String header:headers) {
							if(!allHeaders.contains(header)) {
								System.out.println(file.getName()+"\t"+header);
								allHeaders.add(header);
							}
						}
						
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
//			esr.createClassTemplateFiles();
			
		}
		
		return allHeaders;
	}

	/**
	 * Generates a template for the Record[SourceName] class corresponding to the
	 * structure of the Excel file
	 * 
	 * @return The template as a string
	 */
	private String generateRecordClassTemplate(List<String>headers) {
		
		List<String> fieldNames=null;
		
		if(headers!=null) {
			fieldNames=headers;
		} else {
			fieldNames = getHeaders(sheet,headerRowNum);	
		}
				
		List<String>omitColNames=new ArrayList<>();
				
		if(omitColNoData) {
			getOmitColumnNames(fieldNames, omitColNames);
		}
		
		for(String omitCol:omitColNames) {
			System.out.println(omitCol);
		}
		
		
		int chemicalNameIndex = -1;
		
		for (int i = 0; i < fieldNames.size(); i++) {
			String fieldName = fieldNames.get(i);
			if (fieldName.toLowerCase().contains("name") || fieldName.toLowerCase().contains("chemical")
					|| fieldName.toLowerCase().contains("material") || fieldName.toLowerCase().contains("compound")
					|| fieldName.toLowerCase().contains("substance")) {
				chemicalNameIndex = i;
				break;
			}
		}

		StringBuilder sb = new StringBuilder();
		
		
		sb.append("package gov.epa.exp_data_gathering.parse."+sourceName+";\r\n"
				+ "\r\n"
				+ "import java.util.ArrayList;\r\n"
				+ "import java.util.Hashtable;\r\n"
				+ "import java.util.List;\r\n"
				+ "import java.util.Vector;\r\n"
				+ "import com.google.gson.JsonObject;\r\n"
				+ "\r\n"
				+ "import gov.epa.api.ExperimentalConstants;\r\n"
				+ "import gov.epa.exp_data_gathering.parse.ExcelSourceReader;\r\n"
				+ "import gov.epa.exp_data_gathering.parse.ExperimentalRecord;\r\n"
				+ "import gov.epa.exp_data_gathering.parse.UnitConverter;\r\n"
				+ "\r\n");
				
		sb.append("public class Record" + sourceName + " {\n");
		
		String fieldNamesString = "{";
		
		
		for (String fieldName : fieldNames) {

			if(omitColNames.contains(fieldName))continue;
			
			sb.append("\tpublic String " + fieldName + ";\n");
			fieldNamesString += "\"" + fieldName + "\",";
		}
		fieldNamesString = fieldNamesString.substring(0, fieldNamesString.length() - 1) + "}"; // Trim trailing comma
		
		
		sb.append("\tpublic static final String[] fieldNames = " + fieldNamesString + ";\n\n");
		sb.append("\tpublic static final String lastUpdated = \"" + lastUpdated + "\";\n");
		sb.append("\tpublic static final String sourceName = \"" + sourceName + "\";");
		sb.append(" // TODO Consider creating ExperimentalConstants.strSource" + sourceName + " instead.\n\n");
		sb.append("\tprivate static final String fileName = \"" + fileName + "\";\n\n");
		
		sb.append("\tprivate static final transient UnitConverter unitConverter = new UnitConverter(\"data/density.txt\");\r\n\r\n");

		sb.append("\tpublic static Vector<JsonObject> parse" + sourceName + "RecordsFromExcel() {\n");
		sb.append("\t\tExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);\n");
		sb.append("\t\tVector<JsonObject> records = esr.parseRecordsFromExcel(" + chemicalNameIndex + ");");
		sb.append(" // TODO Chemical name index guessed from header. Is this accurate?\n");
		sb.append("\t\treturn records;\n");
		sb.append("\t}\n\n");
		
		sb.append("\tpublic ExperimentalRecord toExperimentalRecord() {\r\n"
				+ "\t\tExperimentalRecord er=new ExperimentalRecord();\r\n"
				+ "\t\t//TODO Auto-generated method stub\r\n"
				+ "\t\treturn er;\r\n"
				+ "\t}\r\n"
				+ "\n}");
		
		return sb.toString();
	}

	private void getOmitColumnNames(List<String> fieldNames, List<String> omitColNames) {
		boolean setBlankToNull=true;
		FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
		
		for(int col=0;col<fieldNames.size();col++) {
			boolean haveData=false;
			
			for (int row=headerRowNum+1;row<sheet.getLastRowNum();row++) {
				
				if(sheet.getRow(row)==null) break;
				
				Cell cell=sheet.getRow(row).getCell(col);
				
				if(cell==null) continue;
				
				String content = getContent(setBlankToNull, evaluator, cell);
				
				if(content!=null && !content.contentEquals("filtered out")) {
//						System.out.println(fieldNames[col]+"\t"+content);
					haveData=true;						
					break;
				}
				
//					System.out.println(col+"\t"+row+"\t"+content);	
			}
			
			if(!haveData) {
//					System.out.println(fieldNames[col]);
				omitColNames.add(fieldNames.get(col));
			}
		}
	}

	/**
	 * Generates a template for the Parse[SourceName] class with createRecords()
	 * method already constructed
	 * 
	 * @return The template as a string
	 */
	private String generateParseClassTemplate() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("package gov.epa.exp_data_gathering.parse."+sourceName+";\n\n");
		
		
		sb.append("import java.io.File;\n");
		sb.append("import java.io.FileReader;\n");
		sb.append("import java.util.ArrayList;\n");
		sb.append("import java.util.Iterator;\n");
		sb.append("import java.util.List;\n");
		sb.append("import java.util.Vector;\n");
		sb.append("import com.google.gson.JsonObject;\n");
		sb.append("import gov.epa.exp_data_gathering.parse.ExperimentalRecord;\n");
		sb.append("import gov.epa.exp_data_gathering.parse.ExperimentalRecords;\n");
		sb.append("import gov.epa.exp_data_gathering.parse.Parse;\n\n");
				
		sb.append("public class Parse" + sourceName + " extends Parse {\n\n");
		
		sb.append("\tpublic Parse" + sourceName + "() {\n");
		sb.append("\t\tsourceName = \"" + sourceName + "\";");
		sb.append(" // TODO Consider creating ExperimentalConstants.strSource" + sourceName + " instead.\n");
		sb.append("\t\tthis.init();\n\n");
		sb.append(
				"\t\t// TODO Is this a toxicity source? If so, rename original and experimental records files here.\n\t}\n\n");
		sb.append("\t@Override\n\tprotected void createRecords() {\n");
		sb.append("\t\tVector<JsonObject> records = Record" + sourceName + ".parse" + sourceName
				+ "RecordsFromExcel();\n");
		sb.append("\t\twriteOriginalRecordsToFile(records);\n\t}\n\n");
		sb.append("\t@Override\n\tprotected ExperimentalRecords goThroughOriginalRecords() {\n");
		sb.append("\t\tExperimentalRecords recordsExperimental=new ExperimentalRecords();\n");
		sb.append(
				"\t\ttry {\n\t\t\tString jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;\n\t\t\tFile jsonFile = new File(jsonFileName);\n");
		sb.append("\t\t\tList<Record" + sourceName + "> records" + sourceName + " = new ArrayList<Record" + sourceName
				+ ">();\n");
		sb.append("\t\t\tRecord" + sourceName + "[] tempRecords = null;\n");
		sb.append(
				"\t\t\tif (howManyOriginalRecordsFiles==1) {\n\t\t\t\ttempRecords = gson.fromJson(new FileReader(jsonFile), Record"
						+ sourceName + "[].class);\n");
		sb.append("\t\t\t\tfor (int i = 0; i < tempRecords.length; i++) {\n\t\t\t\t\trecords" + sourceName
				+ ".add(tempRecords[i]);\n\t\t\t\t}\n\t\t\t} else {\n");
		sb.append("\t\t\t\tfor (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {\n");
		sb.append(
				"\t\t\t\t\tString batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(\".\")) + \" \" + batch + \".json\";\n");
		sb.append(
				"\t\t\t\t\tFile batchFile = new File(batchFileName);\n\t\t\t\t\ttempRecords = gson.fromJson(new FileReader(batchFile), Record"
						+ sourceName + "[].class);\n");
		sb.append("\t\t\t\t\tfor (int i = 0; i < tempRecords.length; i++) {\n\t\t\t\t\t\trecords" + sourceName
				+ ".add(tempRecords[i]);\n\t\t\t\t\t}\n\t\t\t\t}\n\t\t\t}\n\n");
		sb.append("\t\t\tIterator<Record" + sourceName + "> it = records" + sourceName + ".iterator();\n");
		sb.append("\t\t\twhile (it.hasNext()) {\n\t\t\t\tRecord" + sourceName
				+ " r = it.next();\n\t\t\t\taddExperimentalRecord(r,recordsExperimental);\n");
		sb.append("\t\t\t}\n");
//		sb.append("\t\t\t\t// TODO Write addExperimentalRecord() method to parse this source.\n\t\t\t}\n");
		sb.append(
				"\t\t} catch (Exception ex) {\n"
				+ "\t\t\tex.printStackTrace();\n"
				+ "\t\t}\n\n\t\treturn recordsExperimental;\n");
		sb.append("\t}\n\n");
		
		sb.append("\tprivate void addExperimentalRecord(Record"+sourceName+" r,\r\n"
				+ "				ExperimentalRecords recordsExperimental) {\r\n"
				+ "\t\tExperimentalRecord er=r.toExperimentalRecord();\r\n"
				+ "\t\trecordsExperimental.add(er);\r\n"
				+ "\t}\r\n\r\n");
		
		sb.append("\tpublic static void main(String[] args) {\r\n"
		+ "\t\tParse"+sourceName+" p = new Parse"+sourceName+"();\r\n"
		+ "\t\t\r\n"
		+ "\t\tp.generateOriginalJSONRecords=true;\r\n"
		+ "\t\t//p.howManyOriginalRecordsFiles=2;\r\n"
		+ "\t\t\r\n"
		+ "\t\tp.removeDuplicates=false;\r\n"
		+ "\t\t\r\n"
		+ "\t\tp.writeJsonExperimentalRecordsFile=true;\r\n"
		+ "\t\tp.writeExcelExperimentalRecordsFile=true;\r\n"
		+ "\t\tp.writeExcelFileByProperty=true;		\r\n"
		+ "\t\tp.writeCheckingExcelFile=false;//creates random sample spreadsheet\r\n"
		+ "\t\tp.createFiles();\r\n"
		+ "\t\t\r\n"
		+ "\t}\r\n}");
		

		return sb.toString();
	}

	public static void main(String[] args) {
		ExcelSourceReader esr = new ExcelSourceReader("etc5010-sup-0002-data_bcf_baf_pfas.xlsx", "Burkhard");
		esr.createClassTemplateFiles(null);
	}

}
