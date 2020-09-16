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

public class ParseReproductive_Toxicity_Data_In_Vitro_from_EPA_HPVIS extends Parse {

	public static String sourceName = ScoreRecord.sourceReproductive_Toxicity_Data_In_Vitro_from_EPA_HPVIS;
	String fileNameSourceExcel = "Reproductive Toxicity Data In Vitro from EPA HPVIS.xls";

	public static String mainFolder = AADashboard.dataFolder + File.separator + sourceName;
	public static String jsonFolder = mainFolder + "/json files";

	static class ToxicityRecords {

		String name;
		String substance_id;
		String casrn;
		String assay_id;
		String Concentration_Result_Type;
		String Concentration_Units;
		String Concentration_Upper_Value;
		String Concentration_Value;
		String Concentration_Value_Description;
		String Conclusion;
		String Consortium_Name;
		String Dose_Remarks;
		String Exposure_Period;
		String Exposure_Period_Units;
		String Exposure_Period_Upper_Value;
		String Frequency_of_Treatment;
		String Gender;
		String GLP;
		String Key_Study_Sponsor_Indicator;
		String Mammalian_Strain;
		String Method_Guideline_Followed;
		String Number_of_Organisms_per_Dose_Concentration;
		String Other_Route_of_Administration;
		String Other_Species;
		String Other_Strain;
		String Population;
		String Post_Exposure_Depuration_Period;
		String Post_Exposure_Depuration_Period_Units;
		String Pre_Mating_Exposure_Period_for_Females;
		String Pre_Mating_Exposure_Period_for_Males;
		String Program_Flag;
		String Reliability;
		String Reliability_Remarks;
		String Route_of_Administration;
		String Species_or_in_Vitro_System;
		String Company_Name;
		String Sponsored_Chemical_Result_Type;
		String Study_Reference_1;
		String Study_Reference_2;
		String Submission_Name;
		String Test_Methods;
		String Test_Conditions_Remarks;
		String Test_Substance_Purity;
		String Administration_Method;
		String Unable_to_Measure_or_Estimate_Justification;
		String Year_Study_Performed;
		String LOAEL;
		String NOAEL_mg_kg;
		String NOAEL_mg_m3;
		String NOAEL_ppm;

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

		tr.name = formatter.formatCellValue(row.getCell(i++));
		tr.substance_id = formatter.formatCellValue(row.getCell(i++));
		tr.casrn = formatter.formatCellValue(row.getCell(i++));
		tr.assay_id = formatter.formatCellValue(row.getCell(i++));
		tr.Concentration_Result_Type = formatter.formatCellValue(row.getCell(i++));
		tr.Concentration_Units = formatter.formatCellValue(row.getCell(i++));
		tr.Concentration_Upper_Value = formatter.formatCellValue(row.getCell(i++));
		tr.Concentration_Value = formatter.formatCellValue(row.getCell(i++));
		tr.Concentration_Value_Description = formatter.formatCellValue(row.getCell(i++));
		tr.Conclusion = formatter.formatCellValue(row.getCell(i++));
		tr.Consortium_Name = formatter.formatCellValue(row.getCell(i++));
		tr.Dose_Remarks = formatter.formatCellValue(row.getCell(i++));
		tr.Exposure_Period = formatter.formatCellValue(row.getCell(i++));
		tr.Exposure_Period_Units = formatter.formatCellValue(row.getCell(i++));
		tr.Exposure_Period_Upper_Value = formatter.formatCellValue(row.getCell(i++));
		tr.Frequency_of_Treatment = formatter.formatCellValue(row.getCell(i++));
		tr.Gender = formatter.formatCellValue(row.getCell(i++));
		tr.GLP = formatter.formatCellValue(row.getCell(i++));
		tr.Key_Study_Sponsor_Indicator = formatter.formatCellValue(row.getCell(i++));
		tr.Mammalian_Strain = formatter.formatCellValue(row.getCell(i++));
		tr.Method_Guideline_Followed = formatter.formatCellValue(row.getCell(i++));
		tr.Number_of_Organisms_per_Dose_Concentration = formatter.formatCellValue(row.getCell(i++));
		tr.Other_Route_of_Administration = formatter.formatCellValue(row.getCell(i++));
		tr.Other_Species = formatter.formatCellValue(row.getCell(i++));
		tr.Other_Strain = formatter.formatCellValue(row.getCell(i++));
		tr.Population = formatter.formatCellValue(row.getCell(i++));
		tr.Post_Exposure_Depuration_Period = formatter.formatCellValue(row.getCell(i++));
		tr.Post_Exposure_Depuration_Period_Units = formatter.formatCellValue(row.getCell(i++));
		tr.Pre_Mating_Exposure_Period_for_Females = formatter.formatCellValue(row.getCell(i++));
		tr.Pre_Mating_Exposure_Period_for_Males = formatter.formatCellValue(row.getCell(i++));
		tr.Program_Flag = formatter.formatCellValue(row.getCell(i++));
		tr.Reliability = formatter.formatCellValue(row.getCell(i++));
		tr.Reliability_Remarks = formatter.formatCellValue(row.getCell(i++));
		tr.Route_of_Administration = formatter.formatCellValue(row.getCell(i++));
		tr.Species_or_in_Vitro_System = formatter.formatCellValue(row.getCell(i++));
		tr.Company_Name = formatter.formatCellValue(row.getCell(i++));
		tr.Sponsored_Chemical_Result_Type = formatter.formatCellValue(row.getCell(i++));
		tr.Study_Reference_1 = formatter.formatCellValue(row.getCell(i++));
		tr.Study_Reference_2 = formatter.formatCellValue(row.getCell(i++));
		tr.Submission_Name = formatter.formatCellValue(row.getCell(i++));
		tr.Test_Methods = formatter.formatCellValue(row.getCell(i++));
		tr.Test_Conditions_Remarks = formatter.formatCellValue(row.getCell(i++));
		tr.Test_Substance_Purity = formatter.formatCellValue(row.getCell(i++));
		tr.Administration_Method = formatter.formatCellValue(row.getCell(i++));
		tr.Unable_to_Measure_or_Estimate_Justification = formatter.formatCellValue(row.getCell(i++));
		tr.Year_Study_Performed = formatter.formatCellValue(row.getCell(i++));
		tr.LOAEL = formatter.formatCellValue(row.getCell(i++));
		tr.NOAEL_mg_kg = formatter.formatCellValue(row.getCell(i++));
		tr.NOAEL_mg_m3 = formatter.formatCellValue(row.getCell(i++));
		tr.NOAEL_ppm = formatter.formatCellValue(row.getCell(i++));

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

		ParseReproductive_Toxicity_Data_In_Vitro_from_EPA_HPVIS pe = new ParseReproductive_Toxicity_Data_In_Vitro_from_EPA_HPVIS();

		pe.createFiles();

	}

}
