
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
import gov.epa.ghs_data_gathering.Parse.ParseHealth_Canada_Priority_Substances_Reproductive_2006.ReproductiveRecords;

public class ParseHealth_Canada_Priority_Substances_Carcinogenicity_2006 extends Parse {

	public ParseHealth_Canada_Priority_Substances_Carcinogenicity_2006() {
		sourceName = ScoreRecord.sourceHealth_Canada_Priority_Substance_Lists_Carcinogenicity;
		fileNameSourceExcel = "Health Canada Priority Substance Lists (Carcinogenicity).xls";
		init();
		
	}
	
	static class CarcinogenicityRecords {

		String Name;
		String Substance_ID;
		String CASRN;
		String Assay_ID;
		String Carcinogenicity;

	}

	private Vector<CarcinogenicityRecords> parseExcelFile(String excelFilePath) {

		try {

			Vector<CarcinogenicityRecords> data_field = new Vector<CarcinogenicityRecords>();

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

				CarcinogenicityRecords cr = createDataField(nextRow);

				data_field.add(cr);

				row++;
			}

			inputStream.close();
			return data_field;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private static CarcinogenicityRecords createDataField(Row row) {
		CarcinogenicityRecords cr = new CarcinogenicityRecords();
		DataFormatter formatter = new DataFormatter();

		int i = 0;

		cr.Name = formatter.formatCellValue(row.getCell(i++));
		cr.Substance_ID = formatter.formatCellValue(row.getCell(i++));
		cr.CASRN = formatter.formatCellValue(row.getCell(i++));
		cr.Assay_ID = formatter.formatCellValue(row.getCell(i++));
		cr.Carcinogenicity = formatter.formatCellValue(row.getCell(i++));

		return cr;
	}

	@Override
	protected void createRecords() {
		Vector<CarcinogenicityRecords> records = parseExcelFile(mainFolder + "/" + fileNameSourceExcel);
		writeOriginalRecordsToFile(records);
	}
	
	public static void main(String[] args) {

		ParseHealth_Canada_Priority_Substances_Carcinogenicity_2006 ph = new ParseHealth_Canada_Priority_Substances_Carcinogenicity_2006();
		ph.createFiles();

	}

	
	@Override
	protected Chemicals goThroughOriginalRecords() {
		Chemicals chemicals=new Chemicals();
		
		try {
			Gson gson = new Gson();
			File jsonFile = new File(mainFolder + "/" + fileNameJSON_Records);
			CarcinogenicityRecords[] records = gson.fromJson(new FileReader(jsonFile), CarcinogenicityRecords[].class);

			for (int i = 0; i < records.length; i++) {
				CarcinogenicityRecords ir = records[i];
				Chemical chemical = this.createChemical(ir);
				if (chemical == null)continue;
				handleMultipleCAS(chemicals, chemical);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}
	

	private Chemical createChemical(CarcinogenicityRecords ir) {
		Chemical chemical = new Chemical();

		chemical.CAS = ir.CASRN;
		chemical.name = ir.Name;

		if (!ir.Carcinogenicity.equals("")) {

			Score score = chemical.scoreCarcinogenicity;
			ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
			score.records.add(sr);
			sr.source = ScoreRecord.sourceHealth_Canada_Priority_Substance_Lists_Carcinogenicity;
			sr.category = "Carcinogen";
			// Assign score based on toxCode:
			sr.score = ScoreRecord.scoreVH;
			sr.rationale = "Score of " + sr.score + " was assigned on based on a category of Carcinogen";
//			System.out.println(chemical.CAS + "\t" + ir.Carcinogenicity);
		}

		return chemical;

	}

}
