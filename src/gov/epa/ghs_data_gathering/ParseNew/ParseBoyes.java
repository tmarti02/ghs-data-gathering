package gov.epa.ghs_data_gathering.ParseNew;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Vector;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.ghs_data_gathering.Parse.Parse;
import gov.epa.ghs_data_gathering.API.AADashboard;
import gov.epa.ghs_data_gathering.API.Chemical;
import gov.epa.ghs_data_gathering.API.Score;
import gov.epa.ghs_data_gathering.API.ScoreRecord;

public class ParseBoyes extends Parse {

	public static String sourceName = ScoreRecord.sourceBoyes;
	String fileNameSourceExcel = "boyes 2001.xlsx";
	
	public static String mainFolder = AADashboard.dataFolder + File.separator + sourceName;
	public static String jsonFolder = mainFolder + "/json files";

	static class BoyesRecord {

		String Chemical;
		String CAS_Number;
		String Primary_Basis;
		String Current_OSHA_PEL;
		String OSHA_Proposed_PEL;
		String NIOSH_REL;
	}

	private Vector<BoyesRecord> parseExcelFile(String excelFilePath) {

		try {

			Vector<BoyesRecord> Boyes_Record = new Vector<>();

			DataFormatter formatter = new DataFormatter();

			File file = new File(excelFilePath);
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet firstSheet = workbook.getSheetAt(0);

			int row = 2;

			while (row < 448) {
				Row currentRow = firstSheet.getRow(row);

				BoyesRecord br = new BoyesRecord();

				br.Chemical = "";
				br.CAS_Number = "";
				br.Primary_Basis = "";
				br.Current_OSHA_PEL = "";
				br.OSHA_Proposed_PEL = "";
				br.NIOSH_REL = "";

				while (true) {

					if (currentRow != null && (formatter.formatCellValue(currentRow.getCell(0)) != ""
							|| formatter.formatCellValue(currentRow.getCell(1)) != ""
							|| formatter.formatCellValue(currentRow.getCell(2)) != ""
							|| formatter.formatCellValue(currentRow.getCell(3)) != ""
							|| formatter.formatCellValue(currentRow.getCell(4)) != ""
							|| formatter.formatCellValue(currentRow.getCell(5)) != "")) {

						br.Chemical += (formatter.formatCellValue(currentRow.getCell(0)));
						br.Chemical += " ";
						br.CAS_Number += (formatter.formatCellValue(currentRow.getCell(1)));
						br.Primary_Basis += (formatter.formatCellValue(currentRow.getCell(2)));
						br.Current_OSHA_PEL += (formatter.formatCellValue(currentRow.getCell(3)));
						br.Current_OSHA_PEL += " ";
						br.OSHA_Proposed_PEL += (formatter.formatCellValue(currentRow.getCell(4)));
						br.OSHA_Proposed_PEL += " ";
						br.NIOSH_REL += (formatter.formatCellValue(currentRow.getCell(5)));
						br.NIOSH_REL += " ";

					} else
						break;
					currentRow = firstSheet.getRow(++row);
				}

				Boyes_Record.add(br);
				row++;

			}

			inputStream.close();
			workbook.close();
			return Boyes_Record;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}

	private Chemical createChemical(BoyesRecord br) {

		Chemical chemical = new Chemical();
		return chemical;
	}

	private void createRecords(String folder, String inputExcelFileName, String outputJSON_Filename) {

		try {

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();

			Vector<BoyesRecord> br = parseExcelFile(folder + "/" + inputExcelFileName);

			FileWriter fw = new FileWriter(folder + "/" + outputJSON_Filename);
			fw.write(gson.toJson(br));
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	private void createScoreRecord(Score score, String hazardCategory, String hazardCode, String hazardStatement,
			String toxRoute, String strScore, String strNote) {
	}

	public static void main(String[] args) {

		ParseBoyes pb = new ParseBoyes();

		pb.createFiles();

	}

}