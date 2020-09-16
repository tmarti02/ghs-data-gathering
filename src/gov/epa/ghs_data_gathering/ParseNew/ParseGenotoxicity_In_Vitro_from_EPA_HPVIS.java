package gov.epa.ghs_data_gathering.ParseNew;

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
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.Parse;

public class ParseGenotoxicity_In_Vitro_from_EPA_HPVIS extends Parse {

	public static String sourceName = ScoreRecord.sourceGenotoxicity_Data_In_Vitro_EPA_HPVIS;
	String fileNameSourceExcel = "Genotoxicity Data In Vitro from EPA HPVIS.xls";

	public static String mainFolder = AADashboard.dataFolder + File.separator + sourceName;
	public static String jsonFolder = mainFolder + "/json files";

	static class ToxicityRecords {

		String Name;
		String Substance_ID;
		String CASRN;
		String Assay_ID;
		String Conclusion_GeneTox;
		String Conclusion;
		String Consortium_Name;
		String Details_on_Cytogenetic_Assay;
		String Dose_Remarks;
		String Genotoxic_Effect;
		String GLP;
		String Key_Study_Sponsor_Indicator;
		String Metabolic_Activation;
		String Method_Guideline_Followed;
		String Other_Species;
		String Other_Strain;
		String Positive_Negative_and_Solvent_Control_Substances;
		String Program_Flag;
		String Reliability;
		String Reliability_Remarks;
		String Results_Remarks;
		String Species;
		String Sponsor_Name;
		String Sponsored_Chemical_Result_Type;
		String Statistical_Results;
		String Strain;
		String Study_Reference;
		String Submission_Name;
		String Submitters_Name;
		String Test_Conditions_Remarks;
		String Test_Substance_Purity;
		String Type_of_Study;
		String Unable_to_Measure_or_Estimate_Justification;
		String Year_Study_Performed;

	}

	private Chemical createChemical(ToxicityRecords tr) {

		Chemical chemical = new Chemical();

		return chemical;

	}

	private Vector<ToxicityRecords> parseExcelFile(String excelFilePath) {

		try {

			Vector<ToxicityRecords> data_field = new Vector<ToxicityRecords>();

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

				ToxicityRecords cr = createDataField(nextRow);

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

	private static ToxicityRecords createDataField(Row row) {
		ToxicityRecords tr = new ToxicityRecords();
		DataFormatter formatter = new DataFormatter();

		int i = 0;

		tr.Name = formatter.formatCellValue(row.getCell(i++));
		tr.Substance_ID = formatter.formatCellValue(row.getCell(i++));
		tr.CASRN = formatter.formatCellValue(row.getCell(i++));
		tr.Assay_ID = formatter.formatCellValue(row.getCell(i++));
		tr.Conclusion_GeneTox = formatter.formatCellValue(row.getCell(i++));
		tr.Conclusion = formatter.formatCellValue(row.getCell(i++));
		tr.Consortium_Name = formatter.formatCellValue(row.getCell(i++));
		tr.Details_on_Cytogenetic_Assay = formatter.formatCellValue(row.getCell(i++));
		tr.Dose_Remarks = formatter.formatCellValue(row.getCell(i++));
		tr.Genotoxic_Effect = formatter.formatCellValue(row.getCell(i++));
		tr.GLP = formatter.formatCellValue(row.getCell(i++));
		tr.Key_Study_Sponsor_Indicator = formatter.formatCellValue(row.getCell(i++));
		tr.Metabolic_Activation = formatter.formatCellValue(row.getCell(i++));
		tr.Method_Guideline_Followed = formatter.formatCellValue(row.getCell(i++));
		tr.Other_Species = formatter.formatCellValue(row.getCell(i++));
		tr.Other_Strain = formatter.formatCellValue(row.getCell(i++));
		tr.Positive_Negative_and_Solvent_Control_Substances = formatter.formatCellValue(row.getCell(i++));
		tr.Program_Flag = formatter.formatCellValue(row.getCell(i++));
		tr.Reliability = formatter.formatCellValue(row.getCell(i++));
		tr.Reliability_Remarks = formatter.formatCellValue(row.getCell(i++));
		tr.Results_Remarks = formatter.formatCellValue(row.getCell(i++));
		tr.Species = formatter.formatCellValue(row.getCell(i++));
		tr.Sponsor_Name = formatter.formatCellValue(row.getCell(i++));
		tr.Sponsored_Chemical_Result_Type = formatter.formatCellValue(row.getCell(i++));
		tr.Statistical_Results = formatter.formatCellValue(row.getCell(i++));
		tr.Strain = formatter.formatCellValue(row.getCell(i++));
		tr.Study_Reference = formatter.formatCellValue(row.getCell(i++));
		tr.Submission_Name = formatter.formatCellValue(row.getCell(i++));
		tr.Submitters_Name = formatter.formatCellValue(row.getCell(i++));
		tr.Test_Conditions_Remarks = formatter.formatCellValue(row.getCell(i++));
		tr.Test_Substance_Purity = formatter.formatCellValue(row.getCell(i++));
		tr.Type_of_Study = formatter.formatCellValue(row.getCell(i++));
		tr.Unable_to_Measure_or_Estimate_Justification = formatter.formatCellValue(row.getCell(i++));
		tr.Year_Study_Performed = formatter.formatCellValue(row.getCell(i++));

		return tr;
	}

	private void createRecords(String folder, String inputExcelFileName, String outputJSON_Filename) {

		try {

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();

			Vector<ToxicityRecords> cr = parseExcelFile(folder + "/" + inputExcelFileName);

			FileWriter fw = new FileWriter(folder + "/" + outputJSON_Filename);
			fw.write(gson.toJson(cr));
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	

	private void createScoreRecord(Score score, String hazardCategory, String hazardCode, String hazardStatement,
			String toxRoute, String strScore, String strNote) {
		ScoreRecord sr = new ScoreRecord();
		score.records.add(sr);
	}

	public static void main(String[] args) {

		ParseGenotoxicity_In_Vitro_from_EPA_HPVIS pg = new ParseGenotoxicity_In_Vitro_from_EPA_HPVIS();

		pg.createFiles();

	}

}