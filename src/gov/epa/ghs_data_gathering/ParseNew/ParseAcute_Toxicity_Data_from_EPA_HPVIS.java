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

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.Parse;

public class ParseAcute_Toxicity_Data_from_EPA_HPVIS extends Parse {


	public ParseAcute_Toxicity_Data_from_EPA_HPVIS() {
		sourceName = ScoreRecord.sourceAcute_Toxicity_Data_from_EPA_HPVIS;
		fileNameSourceExcel = "Acute Toxicity Data from EPA HPVIS.xls";
		init();
	}
	
	static class ToxicityRecords {

		String Name;
		String Substance_ID;
		String CASRN;
		String Assay_ID;
		String ConcentrationPercentage;
		String ConcentrationResultType;
		String ConcentrationUnits;
		String ConcentrationUpperValue;
		String ConcentrationValue;
		String ConcentrationValueDescription;
		String Conclusion;
		String ConsortiumName;
		String DoseRemarks;
		String Gender;
		String GLP;
		String KeyStudySponsorIndicator;
		String MammalianStrain;
		String MethodGuidelineFollowed;
		String NumberofDeathsFemales;
		String NumberofDeathsMales;
		String NumberofDeathsTotal;
		String NumberofOrganismsperDoseConcentration;
		String OtherRouteofAdministration;
		String OtherSpecies;
		String OtherStrain;
		String ProgramFlag;
		String Reliability;
		String ReliabilityRemarks;
		String ResultsRemarks;
		String RouteofAdministration;
		String SpeciesorinVitroSystem;
		String SponsorName;
		String SponsoredChemicalResultType;
		String StudyReference;
		String SubmissionName;
		String SubmittersName;
		String TestConditionsRemarks;
		String TestSubstancePurity;
		String TypeofExposure;
		String UnabletoMeasureorEstimateJustification;
		String YearStudyPerformed;
		String LD50mgkg;
		String LD50mgL;
		String LD50mgm3;
		String LD50ppm;
		String LD50mlkg;

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

				ToxicityRecords tr = createDataField(nextRow);

				data_field.add(tr);

				row++;
			}

			inputStream.close();
			return data_field;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private static ToxicityRecords createDataField(Row row) {
		ToxicityRecords tr = new ToxicityRecords();
		DataFormatter formatter = new DataFormatter();

		for (int i = 0; i <= 45; i++) {
			Cell cell = row.getCell(i);

			switch (i) {

			case 0:
				tr.Name = formatter.formatCellValue(cell);
				break;
			case 1:
				tr.Substance_ID = formatter.formatCellValue(cell);
				break;
			case 2:
				tr.CASRN = formatter.formatCellValue(cell);
				break;
			case 3:
				tr.Assay_ID = formatter.formatCellValue(cell);
				break;
			case 4:
				tr.ConcentrationPercentage = formatter.formatCellValue(cell);
				break;
			case 5:
				tr.ConcentrationResultType = formatter.formatCellValue(cell);
				break;
			case 6:
				tr.ConcentrationUnits = formatter.formatCellValue(cell);
				break;
			case 7:
				tr.ConcentrationUpperValue = formatter.formatCellValue(cell);
				break;
			case 8:
				tr.ConcentrationValue = formatter.formatCellValue(cell);
				break;
			case 9:
				tr.ConcentrationValueDescription = formatter.formatCellValue(cell);
				break;
			case 10:
				tr.Conclusion = formatter.formatCellValue(cell);
				break;
			case 11:
				tr.ConsortiumName = formatter.formatCellValue(cell);
				break;
			case 12:
				tr.DoseRemarks = formatter.formatCellValue(cell);
				break;
			case 13:
				tr.Gender = formatter.formatCellValue(cell);
				break;
			case 14:
				tr.GLP = formatter.formatCellValue(cell);
				break;
			case 15:
				tr.KeyStudySponsorIndicator = formatter.formatCellValue(cell);
				break;
			case 16:
				tr.MammalianStrain = formatter.formatCellValue(cell);
				break;
			case 17:
				tr.MethodGuidelineFollowed = formatter.formatCellValue(cell);
				break;
			case 18:
				tr.NumberofDeathsFemales = formatter.formatCellValue(cell);
				break;
			case 19:
				tr.NumberofDeathsMales = formatter.formatCellValue(cell);
				break;
			case 20:
				tr.NumberofDeathsTotal = formatter.formatCellValue(cell);
				break;
			case 21:
				tr.NumberofOrganismsperDoseConcentration = formatter.formatCellValue(cell);
				break;
			case 22:
				tr.OtherRouteofAdministration = formatter.formatCellValue(cell);
				break;
			case 23:
				tr.OtherSpecies = formatter.formatCellValue(cell);
				break;
			case 24:
				tr.OtherStrain = formatter.formatCellValue(cell);
				break;
			case 25:
				tr.ProgramFlag = formatter.formatCellValue(cell);
				break;
			case 26:
				tr.Reliability = formatter.formatCellValue(cell);
				break;
			case 27:
				tr.ReliabilityRemarks = formatter.formatCellValue(cell);
				break;
			case 28:
				tr.ResultsRemarks = formatter.formatCellValue(cell);
				break;
			case 29:
				tr.RouteofAdministration = formatter.formatCellValue(cell);
				break;
			case 30:
				tr.SpeciesorinVitroSystem = formatter.formatCellValue(cell);
				break;
			case 31:
				tr.SponsorName = formatter.formatCellValue(cell);
				break;
			case 32:
				tr.SponsoredChemicalResultType = formatter.formatCellValue(cell);
				break;
			case 33:
				tr.StudyReference = formatter.formatCellValue(cell);
				break;
			case 34:
				tr.SubmissionName = formatter.formatCellValue(cell);
				break;
			case 35:
				tr.SubmittersName = formatter.formatCellValue(cell);
				break;
			case 36:
				tr.TestConditionsRemarks = formatter.formatCellValue(cell);
				break;
			case 37:
				tr.TestSubstancePurity = formatter.formatCellValue(cell);
				break;
			case 38:
				tr.TypeofExposure = formatter.formatCellValue(cell);
				break;
			case 39:
				tr.UnabletoMeasureorEstimateJustification = formatter.formatCellValue(cell);
				break;
			case 40:
				tr.YearStudyPerformed = formatter.formatCellValue(cell);
				break;
			case 41:
				tr.LD50mgkg = formatter.formatCellValue(cell);
				break;
			case 42:
				tr.LD50mgL = formatter.formatCellValue(cell);
				break;
			case 43:
				tr.LD50mgm3 = formatter.formatCellValue(cell);
				break;
			case 44:
				tr.LD50ppm = formatter.formatCellValue(cell);
				break;
			case 45:
				tr.LD50mlkg = formatter.formatCellValue(cell);
				break;
			}

		}
		return tr;
	}

	@Override
	protected void createRecords() {

		try {

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();

			Vector<ToxicityRecords> tr = parseExcelFile(mainFolder + "/" + this.fileNameSourceExcel);

			FileWriter fw = new FileWriter(mainFolder + "/" + this.fileNameJSON_Records);
			fw.write(gson.toJson(tr));
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	protected Chemicals goThroughOriginalRecords() {
		Chemicals chemicals=new Chemicals();
		return chemicals;
	}

	

	private void createScoreRecord(Score score, String hazardCategory, String hazardCode, String hazardStatement,
			String toxRoute, String strScore, String strNote) {
		ScoreRecord sr = new ScoreRecord();
		score.records.add(sr);
	}

	public static void main(String[] args) {

		ParseAcute_Toxicity_Data_from_EPA_HPVIS ph = new ParseAcute_Toxicity_Data_from_EPA_HPVIS();

		ph.createFiles();

	}

}