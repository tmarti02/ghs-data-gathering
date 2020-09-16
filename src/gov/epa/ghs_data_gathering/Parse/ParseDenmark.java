package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.LinkedList;
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
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.ParseDSL.DSLRecords;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

public class ParseDenmark extends Parse {

	Hashtable<String, String> dictCode = new Hashtable<String,String>();
	Hashtable<String, String> dictScore = new Hashtable<String, String>();
	Hashtable<String, String> dictHazardStatement = new Hashtable<String, String>();
	Hashtable<String, String> dictRationale = new Hashtable<String, String>();
	
	
	public ParseDenmark() {
		sourceName = ScoreRecord.sourceDenmark;
		fileNameSourceExcel = "Danish EPA advisory CLP-classifications.xls";
		init();
		this.populateDictionaries();
	}

	private void populateDictionaries() {
		// see Danish EPA Environmental project 1350, 2010 (CLP-version)
		// 978-87-92708-59-5.pdf

		// ***************************************************************************************************************
		// Acute mammalian toxicity oral

		dictScore.put("AcuteTox", Chemical.strAcute_Mammalian_ToxicityOral);

		dictCode.put("AcuteTox1", ScoreRecord.scoreVH);// Category 1 - ​Substances that are acutely toxic - Fatal
		dictCode.put("AcuteTox2", ScoreRecord.scoreVH);// Category 2 - ​Substances that are acutely toxic - Fatal
		dictCode.put("AcuteTox3", ScoreRecord.scoreH);// Category 3 - ​Substances that are acutely toxic- Toxic
		dictCode.put("AcuteTox4", ScoreRecord.scoreM);// Category 4 - Substances that are acutely toxic - Harmful

		dictHazardStatement.put("AcuteTox1", "Fatal if swallowed");
		dictHazardStatement.put("AcuteTox2", "Fatal if swallowed");
		dictHazardStatement.put("AcuteTox3", "Toxic if swallowed");
		dictHazardStatement.put("AcuteTox4", "Harmful if swallowed");

		dictRationale.put("AcuteTox1", "Estimated toxicity <= 50 mg/kg body weight");
		dictRationale.put("AcuteTox2", "Estimated toxicity <= 50 mg/kg body weight");
		dictRationale.put("AcuteTox3", "50 < Estimated toxicity <= 300 mg/kg body weight");
		dictRationale.put("AcuteTox4", "300 < Estimated toxicity <= 2000 mg/kg body weight");
		
		// *************************************************************************************************************
		// Acute Aquatic Toxicity:
		dictScore.put("Acute", Chemical.strAcute_Aquatic_Toxicity);
		dictHazardStatement.put("Acute1", "Very toxic to aquatic life");
		dictCode.put("Acute1", ScoreRecord.scoreVH);
		dictRationale.put("Acute1",
				"Predicted LC50 value was less than 1 mg/L (fish, daphnia, or algae), biodegradation was \"ready\", and BCF < 500");

		// *************************************************************************************************************
		// Chronic Aquatic Toxicity:
		dictScore.put("Chron", Chemical.strChronic_Aquatic_Toxicity);

		dictCode.put("Chron1", ScoreRecord.scoreVH);
		dictCode.put("Chron2", ScoreRecord.scoreH);
		dictCode.put("Chron3", ScoreRecord.scoreM);

		dictHazardStatement.put("Chron1", "Very toxic to aquatic life with long-lasting effects");
		dictHazardStatement.put("Chron2", "Toxic to aquatic life with long-lasting effects");
		dictHazardStatement.put("Chron3", "Harmful to aquatic life with long-lasting effects");

		dictRationale.put("Chron1",
				"Predicted LC50 < 1 mg/L (fish, daphnia, or algae) and either biodegradation was \"not ready\", or BCF >= 500");
		dictRationale.put("Chron2",
				"1 mg/L < Predicted LC50 <= 10 mg/L (fish, daphnia, or algae) and either biodegradation was \"not ready\", or BCF >= 500");
		dictRationale.put("Chron3",
				"10 mg/L < Predicted LC50 <= 100 mg/L (fish, daphnia, or algae) and either biodegradation was \"not ready\", or BCF >= 500");

		// *************************************************************************************************************
		// Cancer:
		dictScore.put("Carc", Chemical.strCarcinogenicity);
		dictCode.put("Carc2", ScoreRecord.scoreH);
		dictHazardStatement.put("Carc2", "Suspected of causing cancer");
		dictRationale.put("Carc2", "Predicted to be suspected of causing cancer from the following conditions: <ul>"
				+ "<li>Positive prediction according to the FDA ICSAS method or positive experimental test for carcinogenicity in rats or mice</li>"
				+ "<li>Positive prediction or positive experimental test in at least one model for reverse mutation test (Ames), chromosomal aberration (CHO/CHL), or mouse lymphoma</li>"
				+ "</ul>");

		// *************************************************************************************************************
		// Reproductive and/or developmental toxicity codes:
		dictScore.put("Repr", Chemical.strReproductive);
		dictHazardStatement.put("Repr2", "Suspected of damaging fertility or the unborn child");
		dictCode.put("Repr2", ScoreRecord.scoreM);
		dictRationale.put("Repr2", "Positive prediction in at least one model "
				+ "(human teratogenicity, Drosophila melanogaster SLR, or rodent dominant lethal) and not predicted negative for teratogenicity");

		// *************************************************************************************************************
		// Genetic toxicity/mutagenicity
		dictScore.put("Muta", Chemical.strGenotoxicity_Mutagenicity);
		dictHazardStatement.put("Muta2", "Suspected of causing genetic defects");
		dictCode.put("Muta2", ScoreRecord.scoreH);

		dictRationale.put("Muta2", "Positive test result in at least one training set or positive prediction "
				+ "in at least two models (Drosophila melanogaster SLRL, mouse micronucleus, rodent dominant lethal, "
				+ "mouse sister chromosome exchange, mouse COMET)");

		// *************************************************************************************************************
		// Skin irritation
		dictScore.put("SkinIrr", Chemical.strSkin_Irritation);
		dictHazardStatement.put("SkinIrr2", "Causes skin irritation");
		dictCode.put("SkinIrr2", ScoreRecord.scoreH);

		dictRationale.put("SkinIrr2", "Positive prediction (DK MultiCASE model for severe skin irritation "
				+ "vs mild skin irritation) or positive training set test (rabbit skin irritation)");

		// *************************************************************************************************************
		// Skin sensitization
		dictScore.put("SkinSens", Chemical.strSkin_Sensitization);
		dictHazardStatement.put("SkinSens1", "May cause an allergic skin reaction");
		dictCode.put("SkinSens1", ScoreRecord.scoreH);
		dictRationale.put("SkinSens1",
				"Prediction of \"positive\" and \"strong\" in the TOPKAT guinea pig models or a prediction of \"very active\" in the allergic contact dermatitis model");

	}

	static class DenmarkRecords {
		String CASN;
		String CAS;
		String EINECS_Number;
		String EINECS_Name;
		String Common_Name;
		String EINECS_Formula;
		String CLP_Classification;

	}

	private Vector<DenmarkRecords> parseExcelFile(String excelFilePath) {

		try {

			Vector<DenmarkRecords> data_field = new Vector<DenmarkRecords>();

			File file = new File(excelFilePath);
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new HSSFWorkbook(inputStream);
			Sheet firstSheet = workbook.getSheetAt(0);

			int row = 7;

			while (true) {
				Row nextRow = firstSheet.getRow(row);

				if (nextRow == null) {
					break;
				}

				DenmarkRecords dr = createDataField(nextRow);

				data_field.add(dr);

				row++;
			}

			inputStream.close();
			return data_field;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private static DenmarkRecords createDataField(Row row) {
		DenmarkRecords dr = new DenmarkRecords();
		DataFormatter formatter = new DataFormatter();

		Cell cell0 = row.getCell(0);
		Cell cell1 = row.getCell(1);
		Cell cell2 = row.getCell(2);
		Cell cell3 = row.getCell(3);
		Cell cell4 = row.getCell(4);
		Cell cell5 = row.getCell(5);
		Cell cell6 = row.getCell(6);

		dr.CASN = formatter.formatCellValue(cell0);
		dr.CAS = formatter.formatCellValue(cell1);
		dr.EINECS_Number = formatter.formatCellValue(cell2);
		dr.EINECS_Name = formatter.formatCellValue(cell3);
		dr.Common_Name = formatter.formatCellValue(cell4);
		dr.EINECS_Formula = formatter.formatCellValue(cell5);
		dr.CLP_Classification = formatter.formatCellValue(cell6);

		return dr;
	}

	@Override
	protected void createRecords() {
		Vector<DenmarkRecords> records = parseExcelFile(mainFolder + "/" + this.fileNameSourceExcel);
		writeOriginalRecordsToFile(records);
	}
	
	@Override	
	protected Chemicals goThroughOriginalRecords() {
		Chemicals chemicals=new Chemicals();
		
		try {
			Gson gson = new Gson();
			File jsonFile = new File(mainFolder + "/" + fileNameJSON_Records);
			DenmarkRecords[] records = gson.fromJson(new FileReader(jsonFile), DenmarkRecords[].class);

			for (int i = 0; i < records.length; i++) {
				if (i % 1000 == 0) System.out.println(i);
				DenmarkRecords dm = records[i];
//				if (!dm.CAS.equals("208-96-8")) continue;
				Chemical chemical = this.createChemical(dm);
				handleMultipleCAS(chemicals, chemical);
				
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	private Chemical createChemical(DenmarkRecords dm) {
		Chemical chemical = new Chemical();

		chemical.CAS = dm.CAS;
		chemical.EC_number = dm.EINECS_Number;
		chemical.name = dm.EINECS_Name;

		LinkedList<String> listCLP = Utilities.Parse(dm.CLP_Classification, " ");

		for (int i = 0; i < listCLP.size(); i++) {

			String CLP = listCLP.get(i);
			String className = CLP.substring(0, CLP.length() - 1);// trim off number at end
			String classNumber = CLP.substring(CLP.length() - 1, CLP.length());
			// System.out.println(dm.CAS+"\t"+CLP);

			if (this.dictScore.get(className) != null) {
				String scoreName = dictScore.get(className);
				Score score = chemical.getScore(scoreName);
				this.createRecord(score, CLP);
			} else {
				System.out.println(className + "\tmissing");
			}
		}

		return chemical;
	}

	private void createRecord(Score score, String hazardClassification) {
		ScoreRecord sr = new ScoreRecord();
		score.records.add(sr);

		sr.source = ScoreRecord.sourceDenmark;
		sr.category = hazardClassification;// TODO or assign to classification?
		// sr.hazard_code=toxCode;
		// sr.route=toxRoute;
		// sr.note=strNote;

		if (this.dictCode.get(hazardClassification) != null) {
			sr.score = this.dictCode.get(hazardClassification);
		} else {
			System.out.println("Need score for " + hazardClassification);
		}

		if (this.dictHazardStatement.get(hazardClassification) != null) {
			sr.hazard_statement = dictHazardStatement.get(hazardClassification);
		} else {
			System.out.println("need statement for " + hazardClassification);
		}

		if (this.dictRationale.get(hazardClassification) != null) {
			sr.rationale = dictRationale.get(hazardClassification);
//			System.out.println(sr.rationale);
		} else {
			System.out.println("need rationale for " + hazardClassification);
		}

	}

	public static void main(String[] args) {

		ParseDenmark pd = new ParseDenmark();
		pd.createFiles();

	}

}
	
	


















	