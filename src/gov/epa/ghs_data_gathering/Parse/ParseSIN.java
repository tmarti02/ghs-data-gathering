package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.ghs_data_gathering.Parse.ParseUMD.UMDRecord;
import gov.epa.ghs_data_gathering.Utilities.Utilities;
import gov.epa.ghs_data_gathering.API.AADashboard;
import gov.epa.ghs_data_gathering.API.Chemical;
import gov.epa.ghs_data_gathering.API.Chemicals;
import gov.epa.ghs_data_gathering.API.ScoreRecord;

public class ParseSIN extends Parse {
	
	public ParseSIN() {
		sourceName = ScoreRecord.sourceSIN;
		fileNameSourceExcel = "\\EndocrineMasterSheetDataSearch.xlsx";
		init();
	}
	
	static class SINRecord{
		// TODO use all of fields? or just Reasons_for_inclusion?
		
		String CAS;
		String EC_Number;
		String Substance_Name;
		String SIN_Group;
		String Reasons_For_Inclusion_On_The_SIN_List;
		String REACH_Status;
		String Registration_Information;
		String Hazard_Class_and_Category_Codes;
		String Synonyms;
		String Hazard_Statement_Codes;
		String Registered_Production_Volume;
		String Biomonitoring_Data;
		String Possible_Uses;
		String Technical_Function_of_Substance;
		String Registered_Uses_SU;
		String Registered_Uses_PC;
		String Registered_Uses_AC;
		String Substitution_Options;
		
	}
	
	private Vector<SINRecord> parseExcelFile(String excelFilePath) {

		try {

			Vector<SINRecord> SIN_Records = new Vector<SINRecord>();

			File file = new File(excelFilePath);
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet firstSheet = workbook.getSheetAt(0);

			int row = 1;

			while (true) {
				Row nextRow = firstSheet.getRow(row);

				if (nextRow == null) {
					break;
				}

				SINRecord sr = createDataField(nextRow);

				SIN_Records.add(sr);

				row++;
			}

			inputStream.close();
			workbook.close();
			return SIN_Records;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	
	private static SINRecord createDataField(Row row) {
		SINRecord sr = new SINRecord();
		DataFormatter formatter = new DataFormatter();

		int i = 0;

		sr.CAS = formatter.formatCellValue(row.getCell(i++));
		sr.EC_Number = formatter.formatCellValue(row.getCell(i++));
		sr.Substance_Name = formatter.formatCellValue(row.getCell(i++));
		sr.SIN_Group = formatter.formatCellValue(row.getCell(i++));
		sr.Reasons_For_Inclusion_On_The_SIN_List = formatter.formatCellValue(row.getCell(i++));
		sr.REACH_Status = formatter.formatCellValue(row.getCell(i++));
		sr.Registration_Information = formatter.formatCellValue(row.getCell(i++));
		sr.Hazard_Class_and_Category_Codes = formatter.formatCellValue(row.getCell(i++));
		sr.Synonyms = formatter.formatCellValue(row.getCell(i++));
		sr.Hazard_Statement_Codes = formatter.formatCellValue(row.getCell(i++));
		sr.Registered_Production_Volume = formatter.formatCellValue(row.getCell(i++));
		sr.Biomonitoring_Data = formatter.formatCellValue(row.getCell(i++));
		sr.Possible_Uses = formatter.formatCellValue(row.getCell(i++));
		sr.Technical_Function_of_Substance = formatter.formatCellValue(row.getCell(i++));
		sr.Registered_Uses_SU = formatter.formatCellValue(row.getCell(i++));
		sr.Registered_Uses_PC = formatter.formatCellValue(row.getCell(i++));
		sr.Registered_Uses_AC = formatter.formatCellValue(row.getCell(i++));
		sr.Substitution_Options = formatter.formatCellValue(row.getCell(i++));
		
		return sr;
	}

	private void parseExcelFile(String excelFilePath, String sheetName, String destFolder) {

		try {

			File file = new File(excelFilePath);

			File DestFolder = new File(destFolder);
			if (!DestFolder.exists())
				DestFolder.mkdir();

			// System.out.println(file.exists());

			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			// FileWriter fw=new FileWriter(folder+"/"+name+".txt");

			Workbook workbook = new XSSFWorkbook(inputStream);

			// System.out.println(workbook.getNumberOfSheets());

			Sheet sheet = workbook.getSheet(sheetName);
			Iterator<Row> rowIterator = sheet.iterator();

			int rowNum = 1;

			Row headerRow = rowIterator.next();// discard for now

			Vector<String> uniqueHazardClasses = new Vector<String>();

			int count = 0;

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				rowNum++;

				Chemical chemical = new Chemical();

				String CAS = row.getCell(0).getStringCellValue();

				// if (CAS.indexOf("/")>-1) {
				// System.out.println(CAS);
				// }
				LinkedList<String> Cas_numbers = Utilities.Parse(CAS, "/");

				for (int i = 0; i < Cas_numbers.size(); i++) {
					chemical.CAS = Cas_numbers.get(i).trim();

					if (row.getCell(1) != null)
						chemical.EC_number = row.getCell(1).getStringCellValue();

					if (row.getCell(2) != null)
						chemical.name = row.getCell(2).getStringCellValue();

					if (row.getCell(4) != null) {

						String reason = row.getCell(4).getStringCellValue();

						if (reason.toLowerCase().indexOf("endocrine") > -1) {
							count++;

							System.out.println(chemical.CAS);

							ScoreRecord sr = new ScoreRecord();
							sr.score = ScoreRecord.scoreH;
							sr.source = ScoreRecord.sourceSIN;
							sr.rationale = "Chemical appears in SIN (Substitute It Now) List:<br><br>";
							sr.rationale += reason;
							chemical.scoreEndocrine_Disruption.records.add(sr);

							chemical.writeToFile(jsonFolder);

						} else if (reason.toLowerCase().indexOf("cmr") > -1) {
							// TODO
						}
					}

				}

			}

			System.out.println(count);

			inputStream.close();
			workbook.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
	@Override
	protected void createRecords() {
		Vector<SINRecord> records = parseExcelFile(mainFolder + "/" + fileNameSourceExcel);
		writeOriginalRecordsToFile(records);
	}


	@Override
	protected Chemicals goThroughOriginalRecords() {
	
		Chemicals chemicals=new Chemicals();
		try {

			String jsonFilePath = mainFolder + File.separator + this.fileNameJSON_Records;

			Gson gson = new Gson();
			SINRecord[] records = gson.fromJson(new FileReader(jsonFilePath), SINRecord[].class);

			for (int i = 0; i < records.length; i++) {
				SINRecord sinRecord = records[i];
				Chemical chemical=createChemical(sinRecord);
				if (chemical==null) continue;
				handleMultipleCAS(chemicals, chemical);
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}


	private Chemical createChemical(SINRecord sinRecord) {
		Chemical chemical = new Chemical();
		String CAS = sinRecord.CAS;
		LinkedList<String> Cas_numbers = Utilities.Parse(CAS, "/");

		for (int j = 0; j < Cas_numbers.size();j++) {
			chemical.CAS = Cas_numbers.get(j).trim();

			if (sinRecord.EC_Number != null)
				chemical.EC_number = sinRecord.EC_Number;

			if (sinRecord.Substance_Name != null)
				chemical.name = sinRecord.Substance_Name;

			if (sinRecord.Reasons_For_Inclusion_On_The_SIN_List != null) {

				String reason = sinRecord.Reasons_For_Inclusion_On_The_SIN_List;

				if (reason.toLowerCase().indexOf("endocrine") > -1) {
//							count++;

//					System.out.println(chemical.CAS);

					ScoreRecord sr = new ScoreRecord();
					sr.score = ScoreRecord.scoreH;
					sr.source = ScoreRecord.sourceSIN;
					sr.rationale = "Chemical appears in SIN (Substitute It Now) List:<br><br>";
					sr.rationale += reason;
					chemical.scoreEndocrine_Disruption.records.add(sr);

					return chemical;
//					chemical.writeToFile(jsonFolder);

				} else if (reason.toLowerCase().indexOf("cmr") > -1) {
					// TODO
				}
			}
		}
		return null;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParseSIN ps = new ParseSIN();
		ps.createFiles();
		
		
//		String sheetName = "SinList_all_chemicals";
//		String excelFilePath = mainFolder + "\\" + fileNameSourceExcel;
//		String outputFolderPath = mainFolder + "/json files";
//		parseExcelFile(excelFilePath, sheetName, outputFolderPath);
		// TODO Auto-generated method stub

	}
	
}
