package gov.epa.ghs_data_gathering.GetData.SkinSensitization;

import java.io.BufferedReader;

//import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import gov.epa.ghs_data_gathering.GetData.DSSTOX;
import gov.epa.ghs_data_gathering.GetData.RecordChemReg;
import gov.epa.ghs_data_gathering.GetData.RecordDashboard;
import gov.epa.ghs_data_gathering.GetData.RecordTox;
import gov.epa.ghs_data_gathering.GetData.Scifinder;
import gov.epa.ghs_data_gathering.GetData.ScifinderRecord;
import gov.epa.ghs_data_gathering.Utilities.ExcelUtilities;
import gov.epa.ghs_data_gathering.Utilities.MolFileUtilities;

//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.DataFormatter;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;

public class SkinSensitizationOECDToolbox {
	

	/**
	 * Gets the column number for a column header name in the header row
	 * 
	 * @param row
	 * @param name
	 * @return
	 */
	int getColNum(XSSFRow row, String name) {
		DataFormatter formatter = new DataFormatter();
		for (int i = 0; i < row.getLastCellNum(); i++) {
			XSSFCell cell = row.getCell(i);
			String val = formatter.formatCellValue(cell);
			if (val.contentEquals(name)) {
				return i;
			}
		}
		return -1;
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
					if (!strEC3_Info.contains("LLNA"))
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

	Vector<RecordOECD_Toolbox> parseExcel2(String filePathExcel) {
		Vector<RecordOECD_Toolbox> records = new Vector<>();

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
					if (!strEC3_Info.contains("LLNA"))
						continue;

					String[] EC3_Info = strEC3_Info.split("\n");

					r.species = EC3_Info[0];
					r.endpoint = EC3_Info[1];
					r.test_type = EC3_Info[2];
					r.type_of_method = EC3_Info[3];

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



	

	void flattenRecords(Vector<RecordOECD_Toolbox> records, String filepathTextOut) {

		for (RecordOECD_Toolbox record : records) {
			System.out.println(record.CAS + "\t" + record.EC3);
		}

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

	

	

	

	

	
	
	
	public static void main(String[] args) {
		SkinSensitizationOECDToolbox s = new SkinSensitizationOECDToolbox();

//		String folder="C:\\Users\\lvegosen\\OneDrive - Environmental Protection Agency (EPA)\\ORISEResearch_LeoraVegosen\\skin_sensitization\\";

//		String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2020 skin sensitization paper\\LLNA_OECD_Toolbox\\";
////		String folder="AA Dashboard\\Data\\OECD toolbox\\";
//		String filePathExcel = folder + "Data matrix_1_8_19__15_52_25.xlsx";
//
//		String filepathExcel2=folder+"literature_data\\OECDToolbox.xlsx";
//		String filepathout2=folder+"\\literature_data\\OECDToolboxFlat.txt";
//		String filepathout = folder + "Data matrix_1_8_19__15_52_25.txt";
//		String filepathoutFlat = folder + "Data matrix_1_8_19__15_52_25_flat.txt";
//		s.parseExcelAndFlatten(filePathExcel,filepathout,filepathoutFlat);

//		String scifinderFilePath=Scifinder.folderScifinder+"\\scifinder_chemical_info.txt";
//		Vector<RecordOECD_Toolbox>records=s.parseExcel(filePathExcel,filepathout,scifinderFilePath);

//		s.flattenRecords(records, folder+"bob.txt");

//		RecordOECD_Toolbox.writeToExcel(folder+"2019-10-21 LNNA.xlsx", records);
//		RecordOECD_Toolbox.writeToExcelNoDuplicates(folder+"2019-10-21-unique cas list.xlsx", records);

//		s.flattenRecords(records, filepathoutFlat);

		
		//*******************************************************************************************************

		//2020/02/12
		String endpoint="LLNA";
		String source="OECD_Toolbox";
		String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2020 skin sensitization paper\\LLNA_OECD_Toolbox\\";
//		String folder="AA Dashboard\\Data\\OECD toolbox\\";
		String filePathExcel = folder + "Data matrix_1_8_19__15_52_25.xlsx";
		
		Vector<RecordOECD_Toolbox> recordsTox = s.parseExcel2(filePathExcel);
		
		Vector<RecordChemReg> recordsChemReg = DSSTOX.parseChemRegExcel(folder + "LLNA_OECD_Toolbox_ChemReg.xlsx");
		
		Hashtable<String, RecordDashboard> htDashboard = DSSTOX
				.parseDashboardExcel(folder + "LLNA_OCED_Toolbox_Dashboard.xls");
		
				
		Vector<RecordTox> recordsTox2=new Vector<>();
		
		for (RecordOECD_Toolbox recordOECD_Toolbox:recordsTox) {
			RecordTox recTox=getRecordTox(recordOECD_Toolbox);		
			recordsTox2.add(recTox);
		}
		
		DSSTOX.goThroughToxRecords(recordsTox2, recordsChemReg, htDashboard,folder,endpoint,source);
		

	}
	
	/**
	 * Converts to common tox record and assigns binary tox val
	 * 
	 * @param recordOECD_Toolbox
	 * @return
	 */
	public static RecordTox getRecordTox(RecordOECD_Toolbox recordOECD_Toolbox) {
		RecordTox r=new RecordTox();
		r.CAS=recordOECD_Toolbox.CAS;
		r.chemicalName=recordOECD_Toolbox.chemical_name;
		
		//Omit weak tox (EC3>10%)
		
		if (recordOECD_Toolbox.EC3.contentEquals("Negative")) {
			r.binaryToxResult=0;
		} else if (recordOECD_Toolbox.EC3.contentEquals("Strongly positive") || recordOECD_Toolbox.EC3.contentEquals("Weakly positive")) {
			r.binaryToxResult=1;
		} else {
			try {
				double EC3=Double.parseDouble(recordOECD_Toolbox.EC3);			
				r.binaryToxResult=1;
			
			}  catch (Exception ex) {				
				r.binaryToxResult=-1;
				System.out.println(recordOECD_Toolbox.CAS+"\t"+recordOECD_Toolbox.EC3+"\tAmbiguous");				
			}
		}
				
		return r;
	}
	
	/**
	 * Converts to common tox record and assigns binary tox val
	 * 
	 * @param recordOECD_Toolbox
	 * @return
	 */
	public static RecordTox getRecordToxOld(RecordOECD_Toolbox recordOECD_Toolbox) {
		RecordTox r=new RecordTox();
		r.CAS=recordOECD_Toolbox.CAS;
		r.chemicalName=recordOECD_Toolbox.chemical_name;
		
		//Omit weak tox (EC3>10%)
		
		if (recordOECD_Toolbox.EC3.contentEquals("Negative")) {
			r.binaryToxResult=0;
		} else if (recordOECD_Toolbox.EC3.contentEquals("Strongly positive")) {
			r.binaryToxResult=1;
		} else if (recordOECD_Toolbox.EC3.contentEquals("Weakly positive")) {
			r.binaryToxResult=-1;
		} else {
			double EC3=Double.parseDouble(recordOECD_Toolbox.EC3);			
			if (EC3>10) r.binaryToxResult=-1;//Weak ambiguous
			else r.binaryToxResult=1;			
		}
				
//		if (recordOECD_Toolbox.CAS.contentEquals("10461-98-0")) {
//			System.out.println("EC3="+recordOECD_Toolbox.EC3);
//			System.out.println("Result in get record tox="+r.binaryToxResult);
//		}

		
		return r;
	}


}

