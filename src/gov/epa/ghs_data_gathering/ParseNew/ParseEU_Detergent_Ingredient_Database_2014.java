package gov.epa.ghs_data_gathering.ParseNew;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.AADashboard;
import gov.epa.api.Chemical;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.Parse;

public class ParseEU_Detergent_Ingredient_Database_2014 extends Parse {

	public static String sourceName = ScoreRecord.sourceEU_Detergent_Ingredient_Database_2014;
	String fileNameSourceExcel = "EU Detergent Ingredient Database (DID) 2014 version.xls";

	public static String mainFolder = AADashboard.dataFolder + File.separator + sourceName;
	public static String jsonFolder = mainFolder + "/json files";

	static class GenotoxicityRecords {

		String Name;
		String SubstanceID;
		String CASRN;
		String AssayID;
		String DetergentCategory;
		String DIDNumber;
		String LC50_EC50;
		String SF_acute;
		String TF_acute;
		String NOEC;
		String SF_chronic;
		String TF_chronic;
		String DF;
		String AerobicDegradation;
		String AssessedAerobicDegredation;
		String AnaerobicDegredation;
		String AnaerobicDegredationExplanation;
		String Notes;

	}

	private Chemical createChemical(GenotoxicityRecords tr) {

		Chemical chemical = new Chemical();

		return chemical;

	}

	private Vector<GenotoxicityRecords> parseExcelFile(String excelFilePath) {

		try {

			Vector<GenotoxicityRecords> data_field = new Vector<GenotoxicityRecords>();

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

				GenotoxicityRecords tr = createDataField(nextRow);

				data_field.add(tr);

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

	private static GenotoxicityRecords createDataField(Row row) {
		GenotoxicityRecords tr = new GenotoxicityRecords();
		DataFormatter formatter = new DataFormatter();

		for (int i = 0; i <= 17; i++) {
			Cell cell = row.getCell(i);

			switch (i) {
			case 0:
				tr.Name = formatter.formatCellValue(cell);
				break;
			case 1:
				tr.SubstanceID = formatter.formatCellValue(cell);
				break;
			case 2:
				tr.CASRN = formatter.formatCellValue(cell);
				break;
			case 3:
				tr.AssayID = formatter.formatCellValue(cell);
				break;
			case 4:
				tr.DetergentCategory = formatter.formatCellValue(cell);
				break;
			case 5:
				tr.DIDNumber = formatter.formatCellValue(cell);
				break;
			case 6:
				tr.LC50_EC50 = formatter.formatCellValue(cell);
				break;
			case 7:
				tr.SF_acute = formatter.formatCellValue(cell);
				break;
			case 8:
				tr.TF_acute = formatter.formatCellValue(cell);
				break;
			case 9:
				tr.NOEC = formatter.formatCellValue(cell);
				break;
			case 10:
				tr.SF_chronic = formatter.formatCellValue(cell);
				break;
			case 11:
				tr.TF_chronic = formatter.formatCellValue(cell);
				break;
			case 12:
				tr.DF = formatter.formatCellValue(cell);
				break;
			case 13:
				tr.AerobicDegradation = formatter.formatCellValue(cell);
				break;
			case 14:
				tr.AssessedAerobicDegredation = formatter.formatCellValue(cell);
				break;
			case 15:
				tr.AnaerobicDegredation = formatter.formatCellValue(cell);
				break;
			case 16:
				tr.AnaerobicDegredationExplanation = formatter.formatCellValue(cell);
				break;
			case 17:
				tr.Notes = formatter.formatCellValue(cell);
				break;

			}
		}

		return tr;
	}

	private void createRecords(String folder, String inputExcelFileName, String outputJSON_Filename) {

		try {

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();

			Vector<GenotoxicityRecords> tr = parseExcelFile(folder + "/" + inputExcelFileName);

			FileWriter fw = new FileWriter(folder + "/" + outputJSON_Filename);
			fw.write(gson.toJson(tr));
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void createScoreRecord(Score score, GenotoxicityRecords gr) {
		ScoreRecord sr = new ScoreRecord(score.hazard_name,gr.CASRN,gr.Name);
		score.records.add(sr);
	}

	

	public static void main(String[] args) {

		ParseEU_Detergent_Ingredient_Database_2014 pd = new ParseEU_Detergent_Ingredient_Database_2014();

		pd.createFiles();

	}

}