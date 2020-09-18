
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
import gov.epa.ghs_data_gathering.Parse.ParseProp65.Prop65Records;

public class ParseNIOSH_List_of_Potential_Occupational_Carcinogens extends Parse {

	
	static class CarcinogenicityRecords {

		String Name;
		String Substance_ID;
		String CASRN;
		String Assay_ID;
		String NIOSH_List_of_Potential_Occupational_Carcinogens;

	}
	
	public ParseNIOSH_List_of_Potential_Occupational_Carcinogens() {
		sourceName = ScoreRecord.sourceNIOSH_Potential_Occupational_Carcinogens;
		fileNameSourceExcel = "NIOSH list of potential occupational carcinogens.xls";
		init();
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
			workbook.close();
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
		cr.NIOSH_List_of_Potential_Occupational_Carcinogens = formatter.formatCellValue(row.getCell(i++));

		return cr;
	}

	@Override
	protected void createRecords() {
		Vector<CarcinogenicityRecords> records = parseExcelFile(mainFolder + "/" + fileNameSourceExcel);
		writeOriginalRecordsToFile(records);
	}
	

	public static void main(String[] args) {

		ParseNIOSH_List_of_Potential_Occupational_Carcinogens ph = new ParseNIOSH_List_of_Potential_Occupational_Carcinogens();
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
				// for (int i = 0; i < 10; i++) {

				CarcinogenicityRecords ir = records[i];
				Chemical chemical = this.createChemical(ir);

				if (chemical == null) continue;
				
				handleMultipleCAS(chemicals, chemical);

				// if (chemical.CAS.indexOf("\n")>-1) continue;//TODO
//				chemical.writeToFile(jsonFolder);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	private Chemical createChemical(CarcinogenicityRecords ir) {
		// TODO Auto-generated method stub
		Chemical chemical = new Chemical();

		chemical.name = ir.Name;
		chemical.CAS = ir.CASRN;

		Score score = chemical.scoreCarcinogenicity;
		this.createScoreRecord(score, chemical, ir.NIOSH_List_of_Potential_Occupational_Carcinogens, "", "", "",
				ScoreRecord.scoreVH, "");

		return chemical;
	}

	private void createScoreRecord(Score score, Chemical chemical,String hazardCategory, String hazardCode, String hazardStatement,
			String toxRoute, String strScore, String strNote) {
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		score.records.add(sr);

		sr.source = ScoreRecord.sourceNIOSH_Potential_Occupational_Carcinogens;
		sr.category = hazardCategory;// TODO or assign to classification?
		sr.hazard_code = hazardCode;
		sr.route = toxRoute;

		sr.hazard_statement = hazardStatement;

		sr.note = strNote;

		// Assign score based on toxCode:
		sr.score = strScore;

		sr.rationale = "Score of " + strScore
				+ " was assigned since this chemical appeared on the NIOSH List of Potential Occupational Carcinogens";

	}

}
