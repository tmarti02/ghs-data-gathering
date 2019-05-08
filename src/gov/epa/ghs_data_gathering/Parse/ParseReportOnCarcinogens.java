package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.ghs_data_gathering.Parse.ParseSIN.SINRecord;
import gov.epa.ghs_data_gathering.API.AADashboard;
import gov.epa.ghs_data_gathering.API.Chemical;
import gov.epa.ghs_data_gathering.API.Chemicals;
import gov.epa.ghs_data_gathering.API.Score;
import gov.epa.ghs_data_gathering.API.ScoreRecord;

public class ParseReportOnCarcinogens extends Parse {

	 
	static class ReportOnCarcinogens {
		String CASRN;
		String NAME_OR_SYNONYM;
		String Listing_in_the_14th_RoC;

	}
	
	public ParseReportOnCarcinogens() {
		sourceName = ScoreRecord.sourceReportOnCarcinogens;
		fileNameSourceExcel = "Carcinogenicity.xlsx";
		init();
	}

	private Vector<ReportOnCarcinogens> parseExcelFile(String excelFilePath) {

		try {

			Vector<ReportOnCarcinogens> data_field = new Vector<ReportOnCarcinogens>();

			File file = new File(excelFilePath);
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet fourthSheet = workbook.getSheetAt(3);

			int row = 1;

			while (true) {
				Row nextRow = fourthSheet.getRow(row);

				if (nextRow == null) {
					break;
				}

				ReportOnCarcinogens rc = createDataField(nextRow);

				data_field.add(rc);

				row++;
			}

			inputStream.close();
			workbook.close();
			return data_field;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private static ReportOnCarcinogens createDataField(Row row) {
		ReportOnCarcinogens rc = new ReportOnCarcinogens();
		DataFormatter formatter = new DataFormatter();

		Cell cell0 = row.getCell(0);
		Cell cell1 = row.getCell(1);
		Cell cell2 = row.getCell(2);

		rc.CASRN = formatter.formatCellValue(cell0);
		rc.NAME_OR_SYNONYM = formatter.formatCellValue(cell1);
		rc.Listing_in_the_14th_RoC = formatter.formatCellValue(cell2);

		return rc;
	}

	
	@Override
	protected void createRecords() {
		Vector<ReportOnCarcinogens> records = parseExcelFile(mainFolder + "/" + fileNameSourceExcel);
		writeOriginalRecordsToFile(records);
	}
	
	
	@Override
	protected Chemicals goThroughOriginalRecords() {
		
		Chemicals chemicals=new Chemicals();
		try {
			Gson gson = new Gson();
			File jsonFile = new File(mainFolder + "/" + fileNameJSON_Records);
			ReportOnCarcinogens[] records = gson.fromJson(new FileReader(jsonFile), ReportOnCarcinogens[].class);

			for (int i = 0; i < records.length; i++) {
				ReportOnCarcinogens roc = records[i];
				Chemical chemical = this.createChemical(roc);
				
				handleMultipleCAS(chemicals, chemical);
				
				chemicals.add(chemical);
//				chemical.writeToFile(jsonFolder);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	private Chemical createChemical(ReportOnCarcinogens roc) {

		Chemical chemical = new Chemical();
		chemical.CAS = roc.CASRN;
		chemical.name = roc.NAME_OR_SYNONYM;

		Score score = chemical.scoreCarcinogenicity;

		// Create carcinogenicity score record:
		ScoreRecord sr = new ScoreRecord();
		score.records.add(sr);

		sr.source = ScoreRecord.sourceROC;
		sr.category = roc.Listing_in_the_14th_RoC;

		if (roc.Listing_in_the_14th_RoC.equals("Known")) {
			sr.score = ScoreRecord.scoreVH;
			sr.hazard_statement = "Known to be a human carcinogen";
		} else if (roc.Listing_in_the_14th_RoC.equals("RAHC")) {
			sr.score = ScoreRecord.scoreVH;
			sr.hazard_statement = "Reasonably anticipated to be a human carcinogen";
		} else {
			System.out.println(roc.Listing_in_the_14th_RoC);
		}

		sr.rationale = "Score of " + sr.score + " was assigned based on a carcinogenicity category of " + sr.category;
		sr.note = "";

		return chemical;
	}

	public static void main(String[] args) {

		ParseReportOnCarcinogens pc = new ParseReportOnCarcinogens();

		pc.createFiles();

	}

}