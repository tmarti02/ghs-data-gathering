package gov.epa.exp_data_gathering.parse.OECD_Toolbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.AtomContainer;

import gov.epa.ghs_data_gathering.GetData.Scifinder;
import gov.epa.ghs_data_gathering.GetData.ScifinderRecord;
import gov.epa.ghs_data_gathering.Utilities.ExcelUtilities;

public class RecordOECD_Toolbox_alt {

	public String chemical_number;
	public String chemical_name;
	
	public String CAS;
	public String smiles;

	public String species;
	public String endpoint;
	public String test_type;
	public String type_of_method;
	
	public String scifinderFormula;
	public String scifinderClassIdentifier;
	public String scifinderWarning="";
	
	public String reference;

	public String PII;
	public String PII_Units;
	public String PII_Result;
	

	
	static String[] varlist = { "chemical_number", "chemical_name", "CAS", "smiles","scifinderFormula","scifinderWarning","scifinderClassIdentifier", 
			"species", "endpoint", "test_type","type_of_method", "reference","EC3","EC3_Units","LLNA_Result"};

	
	public static void writeToExcel(String filepath,Vector<RecordOECD_Toolbox>records) {

		XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("sheet0");

        int rowNum = 0;
        System.out.println("Creating excel");

        XSSFRow rowHeader = sheet.createRow(rowNum);
        
        int col=0;
        for (int i=0;i<varlist.length;i++) {
        	XSSFCell cell = rowHeader.createCell(col++);
			cell.setCellValue(varlist[i]);
        }
        rowNum++;
        
        for (RecordOECD_Toolbox r:records) {
        
        	XSSFRow row = sheet.createRow(rowNum++);
        	
        	int colNum = 0;
        	
    		for (int i=0;i<varlist.length;i++) {
    			try {
    				Field myField =r.getClass().getField(varlist[i]);				
    				String val=(String)myField.get(r);
    				
    				XSSFCell cell = row.createCell(colNum++);
    				cell.setCellValue(val);
    			} catch (Exception ex){
    				ex.printStackTrace();
    			}
    		}
        }
        
        try {
            FileOutputStream outputStream = new FileOutputStream(filepath);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	
	public static String getHeader(String [] varlist) {
		String str="";
		for (int i=0;i<varlist.length;i++) {
			str+=varlist[i];
			if (i<varlist.length-1) str+="\t";
		}
		return str;
	}
	
	public static String getHeader() {
		return getHeader(varlist);
	}
	public String toString(String[] varlist) {
		String str="";
		for (int i=0;i<varlist.length;i++) {
			try {
				Field myField =this.getClass().getField(varlist[i]);				
				str+=myField.get(this);
				if (i<varlist.length-1) str+="\t";
			} catch (Exception ex){
				ex.printStackTrace();
			}
		}

		return str;
	}
	
	public String toString() {
		return toString(varlist);
	}


	Vector<RecordOECD_Toolbox> parseExcel(String filePathExcel, String filePathTextOutput, String scifinderTextPath) {
			Vector<RecordOECD_Toolbox> records = new Vector<>();
	
			try {
	
				FileInputStream inputStream = new FileInputStream(new File(filePathExcel));
				FileWriter fw = new FileWriter(filePathTextOutput);
	
				Vector<String> needSCIFINDER = new Vector<String>();
	
				Hashtable<String, ScifinderRecord> htScifinderRecords = Scifinder.getScifinderData(scifinderTextPath);
	
				XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
				XSSFSheet sheet = workbook.getSheetAt(0);
	
				XSSFRow rowChemicalNumber = sheet.getRow(0);
				XSSFRow rowCAS = sheet.getRow(3);
				XSSFRow rowName = sheet.getRow(4);
				XSSFRow rowSMILES = sheet.getRow(6);
	
	//			System.out.println("here");
	
				fw.write(RecordOECD_Toolbox.getHeader() + "\r\n");
	
				for (int i = 1; i <= 1214; i++) {
	
					int col = 3 * (i - 1) + 2;
					int col2 = 3 * (i - 1) + 3;
					int col3 = 3 * (i - 1) + 4;
	
					for (int rowNumber = 31; rowNumber <= 71; rowNumber++) {
						XSSFRow rowEC3 = sheet.getRow(rowNumber);
	
						RecordOECD_Toolbox r = new RecordOECD_Toolbox();
	
						r.chemical_name = ExcelUtilities.getStringValue(rowName.getCell(col));
						r.chemical_number = ExcelUtilities.getStringValue(rowChemicalNumber.getCell(col));
						r.CAS = ExcelUtilities.getStringValue(rowCAS.getCell(col));
						r.smiles = ExcelUtilities.getStringValue(rowSMILES.getCell(col));
	
						r.EC3 = ExcelUtilities.getStringValue(rowEC3.getCell(col));
	
						if (r.EC3.isEmpty())
							continue;
	
						if (r.EC3.contentEquals("Negative")) {
							r.LLNA_Result = "Negative";
						} else {
							r.LLNA_Result = "Positive";
						}
	
						r.EC3_Units = ExcelUtilities.getStringValue(rowEC3.getCell(col2));
	
						String strEC3_Info = ExcelUtilities.getStringValue(rowEC3.getCell(col3));
						if (!strEC3_Info.contains("Primary Irritation Index"))
							continue;
	
						String[] EC3_Info = strEC3_Info.split("\n");
	
						r.species = EC3_Info[0];
						r.endpoint = EC3_Info[1];
						r.test_type = EC3_Info[2];
						r.type_of_method = EC3_Info[3];
	
						Scifinder.getAlternateCASFromScifinderOECD(htScifinderRecords, r);
	
						if (htScifinderRecords.get(r.CAS) != null) {
							ScifinderRecord sr = htScifinderRecords.get(r.CAS);
							r.scifinderFormula = sr.Formula;
							r.scifinderClassIdentifier = sr.Class_Identifier;
							r.scifinderWarning = Scifinder.omitBasedOnScifinderFormula(r.scifinderWarning,
									r.scifinderFormula);
							r.scifinderWarning = Scifinder.omitBasedOnScifinderClassIdentifier(r.scifinderWarning, sr);
	
						} else {
	//						System.out.println(r.CAS+"\tScifinder missing");
	
							if (!needSCIFINDER.contains(r.CAS)) {
								needSCIFINDER.add(r.CAS);
							}
						}
	
						r.reference = "";
						for (int ii = 4; ii < EC3_Info.length; ii++) {
							r.reference += EC3_Info[ii];
							if (ii < EC3_Info.length - 1)
								r.reference += "; ";
						}
	
						// species, duration, test type, type of method, assay, strain, test guideline,
						// year, reference
	//					System.out.println("*"+record.EC3+"*");
	//					System.out.println(r);
	
						records.add(r);
	
						fw.write(r + "\r\n");
						fw.flush();
					}
	
				} // end loop over chemicals
	
				System.out.println("Need scifinder");
				for (String CAS : needSCIFINDER) {
					System.out.println(CAS);
				}
	
				fw.close();
	
			} catch (Exception ex) {
				ex.printStackTrace();
			}
	
			return records;
	
		}


	static Vector<RecordOECD_Toolbox_alt> parseExcel2(String filePathExcel) {
			
		
		
		Vector<RecordOECD_Toolbox_alt> records = new Vector<>();
	
			try {
	
				FileInputStream inputStream = new FileInputStream(new File(filePathExcel));
	
				XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
				XSSFSheet sheet = workbook.getSheetAt(0);
	
				XSSFRow rowChemicalNumber = sheet.getRow(0);
				XSSFRow rowCAS = sheet.getRow(3);
				XSSFRow rowName = sheet.getRow(4);
				XSSFRow rowSMILES = sheet.getRow(6);
	
	//			System.out.println("here");
	
				for (int i = 1; i <= 1214; i++) {
	
					int col = 3 * (i - 1) + 2;
					int col2 = 3 * (i - 1) + 3;
					int col3 = 3 * (i - 1) + 4;
	
					for (int rowNumber = 15; rowNumber <= 17; rowNumber++) {
						XSSFRow rowEC3 = sheet.getRow(rowNumber);
	
						RecordOECD_Toolbox_alt r = new RecordOECD_Toolbox_alt();
	
						r.chemical_name = ExcelUtilities.getStringValue(rowName.getCell(col));
						r.chemical_number = ExcelUtilities.getStringValue(rowChemicalNumber.getCell(col));
						r.CAS = ExcelUtilities.getStringValue(rowCAS.getCell(col));
						r.smiles = ExcelUtilities.getStringValue(rowSMILES.getCell(col));
	
						r.PII = ExcelUtilities.getStringValue(rowEC3.getCell(col));

/*						
						if (r.EC3.isEmpty())
							continue;
	
						if (r.EC3.contentEquals("Negative")) {
							r.LLNA_Result = "Negative";
						} else {
							r.LLNA_Result = "Positive";
						}
*/	
						r.PII_Units = ExcelUtilities.getStringValue(rowEC3.getCell(col2));
	
						String strEC3_Info = ExcelUtilities.getStringValue(rowEC3.getCell(col3));
						if (!strEC3_Info.contains("Primary Irritation Index"))
							continue;
	
						String[] EC3_Info = strEC3_Info.split("\n");
						
						for (String str: EC3_Info) {
							if (str.contains("Endpoint:"))
								r.endpoint = str;
							else if (str.contains("Type of method:"))
								r.type_of_method = str;
							else if (str.contains("guideline"))
								r.test_type = str;
							else if (str.contains("Test organisms"))
								r.species = str;
						}
							
	
						r.reference = "";
						for (int ii = 4; ii < EC3_Info.length; ii++) {
							r.reference += EC3_Info[ii];
							if (ii < EC3_Info.length - 1)
								r.reference += "; ";
						}
	
						records.add(r);
	//					System.out.println(r);
	
					}
	
				} // end loop over chemicals
	
			} catch (Exception ex) {
				ex.printStackTrace();
			}
	
			return records;
	
		}
	
	
	static Vector<RecordOECD_Toolbox_alt> parseExcel3(String filePathExcel) {
			
		
		
		Vector<RecordOECD_Toolbox_alt> records = new Vector<>();
	
			try {
	
				FileInputStream inputStream = new FileInputStream(new File(filePathExcel));
	
				XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
				XSSFSheet sheet = workbook.getSheetAt(0);
	
				XSSFRow rowChemicalNumber = sheet.getRow(0);
				XSSFRow rowCAS = sheet.getRow(3);
				XSSFRow rowName = sheet.getRow(4);
				XSSFRow rowSMILES = sheet.getRow(6);
	
	//			System.out.println("here");
	
				for (int i = 1; i <= 1214; i++) {
	
					int col = 3 * (i - 1) + 2;
					int col2 = 3 * (i - 1) + 3;
					int col3 = 3 * (i - 1) + 4;
	
					for (int rowNumber = 15; rowNumber <= 17; rowNumber++) {
						XSSFRow rowEC3 = sheet.getRow(rowNumber);
	
						RecordOECD_Toolbox_alt r = new RecordOECD_Toolbox_alt();
	
						r.chemical_name = ExcelUtilities.getStringValue(rowName.getCell(col));
						r.chemical_number = ExcelUtilities.getStringValue(rowChemicalNumber.getCell(col));
						r.CAS = ExcelUtilities.getStringValue(rowCAS.getCell(col));
						r.smiles = ExcelUtilities.getStringValue(rowSMILES.getCell(col));
	
						r.PII = ExcelUtilities.getStringValue(rowEC3.getCell(col));

/*						
						if (r.EC3.isEmpty())
							continue;
	
						if (r.EC3.contentEquals("Negative")) {
							r.LLNA_Result = "Negative";
						} else {
							r.LLNA_Result = "Positive";
						}
*/	
						r.PII_Units = ExcelUtilities.getStringValue(rowEC3.getCell(col2));
	
						String strEC3_Info = ExcelUtilities.getStringValue(rowEC3.getCell(col3));
						if (!strEC3_Info.contains("Primary Irritation Index"))
							continue;
	
						String[] EC3_Info = strEC3_Info.split("\n");
						
						for (String str: EC3_Info) {
							if (str.contains("Endpoint:"))
								r.endpoint = str;
							else if (str.contains("Type of method:"))
								r.type_of_method = str;
							else if (str.contains("guideline"))
								r.test_type = str;
							else if (str.contains("Test organisms"))
								r.species = str;
						}
							
	
						r.reference = "";
						for (int ii = 4; ii < EC3_Info.length; ii++) {
							r.reference += EC3_Info[ii];
							if (ii < EC3_Info.length - 1)
								r.reference += "; ";
						}
	
						records.add(r);
	//					System.out.println(r);
	
					}
	
				} // end loop over chemicals
	
			} catch (Exception ex) {
				ex.printStackTrace();
			}
	
			return records;
	
		}



	void parseExcelAndFlatten(String filePathExcel, String outfilepath, String outfilepathFlat) {
			try {
	
				FileWriter fw = new FileWriter(outfilepath);
				FileWriter fwFlat = new FileWriter(outfilepathFlat);
	
				fw.write("ChemicalNumber\tCAS\tName\tEC3value\tEC3units\tEC3info\tBinaryLLNA\r\n");
				fwFlat.write("ChemicalNumber\tCAS\tName\tSMILES\tFinalLLNA\r\n");
	
	//			Vector<RecordNICEATM> records = new Vector<RecordNICEATM>();
	
				FileInputStream inputStream = new FileInputStream(new File(filePathExcel));
	
				XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
				XSSFSheet sheet = workbook.getSheetAt(0);
	
				XSSFRow rowChemicalNumber = sheet.getRow(0);
				XSSFRow rowCAS = sheet.getRow(3);
				XSSFRow rowName = sheet.getRow(4);
				XSSFRow rowSMILES = sheet.getRow(6);
	
	//			System.out.println("here");
	
				for (int i = 1; i <= 1214; i++) {
	
					int col = 3 * (i - 1) + 2;
					int col2 = 3 * (i - 1) + 3;
					int col3 = 3 * (i - 1) + 4;
					String chemicalNumber = ExcelUtilities.getStringValue(rowChemicalNumber.getCell(col));
					String CAS = ExcelUtilities.getStringValue(rowCAS.getCell(col));
					String chemicalName = ExcelUtilities.getStringValue(rowName.getCell(col));
					String SMILES = ExcelUtilities.getStringValue(rowSMILES.getCell(col));
	
					double totalScore = 0;
					double numScores = 0;
	
					for (int rowNumber = 31; rowNumber <= 71; rowNumber++) {
						XSSFRow rowEC3 = sheet.getRow(rowNumber);
						String EC3value = ExcelUtilities.getStringValue(rowEC3.getCell(col));
						String EC3units = ExcelUtilities.getStringValue(rowEC3.getCell(col2));
						String EC3info = ExcelUtilities.getStringValue(rowEC3.getCell(col3)).replace("\n", "; ");
	
						Integer LLNAScore = null;
	
						if (!EC3value.isEmpty()) {
	
							if (EC3value.contentEquals("Negative")) {
								LLNAScore = 0;
							} else {
								LLNAScore = 1;
							}
							totalScore += LLNAScore;
							numScores++;
	
							fw.write(chemicalNumber + "\t" + CAS + "\t" + chemicalName + "\t" + EC3value + "\t" + EC3units
									+ "\t" + EC3info + "\t" + LLNAScore + "\n");
							fw.flush();
						}
					}
	
					Integer LLNAScoreFinal = null;
					if (numScores > 0) {
						double LLNAScoreAvg = totalScore / numScores;
	
						if (LLNAScoreAvg <= 0.2) {
							LLNAScoreFinal = 0;
						}
						if (LLNAScoreAvg >= 0.8) {
							LLNAScoreFinal = 1;
						}
						System.out.println(CAS + "\t" + totalScore + "\t" + numScores + "\t" + LLNAScoreAvg + "\t"
								+ LLNAScoreFinal + "\t");
						fwFlat.write(chemicalNumber + "\t" + CAS + "\t" + chemicalName + "\t" + SMILES + "\t"
								+ LLNAScoreFinal + "\n");
					}
				}
	
				inputStream.close();
				workbook.close();
	//			return records;
				fw.close();
				fwFlat.close();
	
			} catch (Exception ex) {
				ex.printStackTrace();
	//			return null;
			}
	
		}


	public static void writeToExcelNoDuplicates(String filepath, Vector<RecordOECD_Toolbox> records) {
		XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("sheet0");

        int rowNum = 0;
        System.out.println("Creating excel");

        XSSFRow rowHeader = sheet.createRow(rowNum);
        
        int col=0;
        for (int i=0;i<varlist.length;i++) {
        	XSSFCell cell = rowHeader.createCell(col++);
			cell.setCellValue(varlist[i]);
        }
        rowNum++;
        
        Vector<String>uniqueCAS=new Vector<>();
        
        
        for (RecordOECD_Toolbox r:records) {
        
        	if (uniqueCAS.contains(r.CAS)) continue;
        	else uniqueCAS.add(r.CAS);
        	
        	XSSFRow row = sheet.createRow(rowNum++);
        	
        	int colNum = 0;
        	
    		for (int i=0;i<varlist.length;i++) {
    			try {
    				Field myField =r.getClass().getField(varlist[i]);				
    				String val=(String)myField.get(r);
    				
    				XSSFCell cell = row.createCell(colNum++);
    				cell.setCellValue(val);
    			} catch (Exception ex){
    				ex.printStackTrace();
    			}
    		}
        }
        
        try {
            FileOutputStream outputStream = new FileOutputStream(filepath);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}

}