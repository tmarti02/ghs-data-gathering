package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.AADashboard;
import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.ParseIARC.IARCRecords;

/* 
 * use the parse ParseReportOnCarcinogens as a template to
 * edit this class. Can be found in email
 * 
 * change all of the carcinogenicity variables to reproductive.
 * score should be high for all of them 
 *
 */

public class ParseHealth_Canada_Priority_Substances_Reproductive_2006 extends Parse {

	public ParseHealth_Canada_Priority_Substances_Reproductive_2006() {
		sourceName = ScoreRecord.sourceHealth_Canada_Priority_Substance_Lists_Reproductive;
		fileNameSourceExcel = "Health Canada Priority Substance Lists (Reproductive Toxicity).xls";
		init();
	}
	
	static class ReproductiveRecords {

		String Name;
		String Substance_ID;
		String CASRN;
		String Assay_ID;
		String Reproductive_Toxicity;

	}

	private Vector<ReproductiveRecords> parseExcelFile(String excelFilePath) {

		try {

			Vector<ReproductiveRecords> data_field = new Vector<ReproductiveRecords>();

			File file = new File(excelFilePath);
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new HSSFWorkbook(inputStream);
			Sheet thirdSheet = workbook.getSheetAt(2);

			int row = 1;

			while (true) {
				Row nextRow = thirdSheet.getRow(row);

				if (nextRow == null) {
					break;
				}

				ReproductiveRecords rr = createDataField(nextRow);

				data_field.add(rr);

				row++;
			}

			inputStream.close();
			return data_field;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private static ReproductiveRecords createDataField(Row row) {
		ReproductiveRecords rr = new ReproductiveRecords();
		DataFormatter formatter = new DataFormatter();

		int i = 0;

		rr.Name = formatter.formatCellValue(row.getCell(i++));
		rr.Substance_ID = formatter.formatCellValue(row.getCell(i++));
		rr.CASRN = formatter.formatCellValue(row.getCell(i++));
		rr.Assay_ID = formatter.formatCellValue(row.getCell(i++));
		rr.Reproductive_Toxicity = formatter.formatCellValue(row.getCell(i++));

		return rr;
	}

	
	@Override
	protected void createRecords() {
		Vector<ReproductiveRecords> records = parseExcelFile(mainFolder + "/" + fileNameSourceExcel);
		writeOriginalRecordsToFile(records);
	}
	

	@Override
	protected Chemicals goThroughOriginalRecords() {

		Chemicals chemicals = new Chemicals();
		try {
			Gson gson = new Gson();
			File jsonFile = new File(mainFolder + "/" + fileNameJSON_Records);
			ReproductiveRecords[] records = gson.fromJson(new FileReader(jsonFile), ReproductiveRecords[].class);

			for (int i = 0; i < records.length; i++) {
				ReproductiveRecords roc = records[i];
				Chemical chemical = this.createChemical(roc);
				handleMultipleCAS(chemicals, chemical);			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}


	private Chemical createChemical(ReproductiveRecords rr) {

		Chemical chemical = new Chemical();
		chemical.CAS = rr.CASRN;
		chemical.name = rr.Name;

		Score score = chemical.scoreReproductive;

		// Create reproductive score record:
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		score.records.add(sr);

		sr.source = ScoreRecord.sourceReproductiveCanada;
		sr.category = rr.Reproductive_Toxicity;

		sr.score = ScoreRecord.scoreH;
		sr.hazard_statement = "Known to be reproductive toxin";

		sr.rationale = "Score of " + sr.score + " was assigned based on a reproductive category of " + sr.category;
		sr.note = "";

		return chemical;
	}

	public static void main(String[] args) {

		ParseHealth_Canada_Priority_Substances_Reproductive_2006 ph = new ParseHealth_Canada_Priority_Substances_Reproductive_2006();

		// ph.createReproductiveRecord(sourceExcelFile, folder, outputeFileName);

		ph.createFiles();

	}

}