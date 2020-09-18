package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.AADashboard;
import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.ParseReportOnCarcinogens.ReportOnCarcinogens;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

public class ParseReachVeryHighConcernList extends Parse {

	String fileNameSourceExcel = "ReachVeryHighConcernList.xlsx";
	String strDefaultScore = ScoreRecord.scoreH;

	
	public ParseReachVeryHighConcernList() {
		sourceName = ScoreRecord.sourceReachVeryHighConcernList;
		init();
	}
	
	static class ReachVeryHighConcernListRecords {
		String Name;
		String EC_no;
		String CAS_no;
		String Date_of_Inclusion;
		String Intrinsic_Property_Article_57;
		String Decision;
		// String IUCLID_Dataset;

	}

	private Vector<ReachVeryHighConcernListRecords> parseExcelFile(String excelFilePath) {

		try {

			Vector<ReachVeryHighConcernListRecords> data_field = new Vector<ReachVeryHighConcernListRecords>();

			File file = new File(excelFilePath);
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet fifthSheet = workbook.getSheetAt(0);

			int row = 5;

			while (true) {
				Row nextRow = fifthSheet.getRow(row);

				if (nextRow == null) {
					break;
				}

				ReachVeryHighConcernListRecords rl = createDataField(nextRow);

				data_field.add(rl);

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

	private static ReachVeryHighConcernListRecords createDataField(Row row) {
		ReachVeryHighConcernListRecords rl = new ReachVeryHighConcernListRecords();
		DataFormatter formatter = new DataFormatter();

		Cell cell0 = row.getCell(0);
		Cell cell1 = row.getCell(1);
		Cell cell2 = row.getCell(2);
		Cell cell3 = row.getCell(3);
		Cell cell4 = row.getCell(4);
		Cell cell5 = row.getCell(5);
		// Cell cell6 = row.getCell(6);

		rl.Name = formatter.formatCellValue(cell0);
		rl.EC_no = formatter.formatCellValue(cell1);
		rl.CAS_no = formatter.formatCellValue(cell2);
		rl.Date_of_Inclusion = formatter.formatCellValue(cell3);
		rl.Intrinsic_Property_Article_57 = formatter.formatCellValue(cell4);
		rl.Decision = formatter.formatCellValue(cell5);
		// rl.IUCLID_Dataset = formatter.formatCellValue(cell0);

		return rl;
	}
	
	
	@Override
	protected void createRecords() {
		Vector<ReachVeryHighConcernListRecords> records = parseExcelFile(mainFolder + "/" + fileNameSourceExcel);
		writeOriginalRecordsToFile(records);
	}

	public static void main(String[] args) {
		ParseReachVeryHighConcernList pc = new ParseReachVeryHighConcernList();
		pc.createFiles();
	}

	
	@Override
	protected Chemicals goThroughOriginalRecords() {
		
		Chemicals chemicals=new Chemicals();
		
		try {
			Gson gson = new Gson();
			File jsonFile = new File(mainFolder + "/" + fileNameJSON_Records);
			ReachVeryHighConcernListRecords[] records = gson.fromJson(new FileReader(jsonFile),
					ReachVeryHighConcernListRecords[].class);

			for (int i = 0; i < records.length; i++) {

//				System.out.println(i);
				
				ReachVeryHighConcernListRecords roc = records[i];

				Chemical chemical = this.createChemical(roc);

				handleMultipleCAS(chemicals, chemical);

				// AADashboard.writeChemical(jsonFolder, chemical);

			}
			
//			System.out.println(chemicals.size());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	private void createScoreRecord(Score score, Chemical chemical,String category, String strScore) {
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		score.records.add(sr);
		sr.source = ScoreRecord.sourceReachVeryHighConcernList;
		sr.score = strScore;
		sr.category = category;
		sr.rationale = "Score of " + sr.score + " was assigned based on a category of " + sr.category;
	}

	private Chemical createChemical(ReachVeryHighConcernListRecords r) {
		Chemical chemical = new Chemical();
		
		chemical.EC_number = r.EC_no;

		if (r.Name.indexOf("CAS no.") > -1) {
			chemical.name = r.Name.substring(0, r.Name.indexOf("EC no."));
			chemical.CAS = r.Name.substring(r.Name.indexOf("CAS no.:") + 9, r.Name.length()).replace("-, ", "");
			// System.out.println(chemical.CAS);
		} else {
			chemical.name = r.Name;
		}

		chemical.CAS = r.CAS_no.trim();
		chemical.CAS=chemical.CAS.replace(" ", "");

		
		if (chemical.CAS.indexOf("/") > -1) {

			if (chemical.CAS.equals("11/3/75")) {
				chemical.CAS = "7775-11-3";
			} else if (chemical.CAS.equals("9/5/89")) {
				chemical.CAS = "7789-09-5";
			} else if (chemical.CAS.equals("6/2/89")) {
				chemical.CAS = "7789-06-2";
			} else if (chemical.CAS.equals("4/4/32")) {
				chemical.CAS = "7632-04-4";
			} else {
				System.out.println(chemical.CAS + "\t" + chemical.name);
			}

		} else if (chemical.CAS.equals("") || chemical.CAS.equals("-") || chemical.CAS.equals("- ")) {
			chemical.CAS=null;
//			chemical.CAS = ParseJapan.removeBadChars(chemical.name);
//			if (chemical.CAS.length() > 30)
//				chemical.CAS = chemical.CAS.substring(0, 30);
		}

		String ipa = r.Intrinsic_Property_Article_57;

		ipa = ipa.replace("vPvB (Article 57e)", "vPvB (Article 57 e)");

		LinkedList<String> list = Utilities.Parse(ipa, ",");

		Collections.sort(list);
		// Remove duplicates in tox list:
		for (int i = 0; i < list.size() - 1; i++) {
			if (list.get(i).equals(list.get(i + 1))) {
				list.remove(i + 1);
				i--;
			}
		}

		for (int i = 0; i < list.size(); i++) {
			String toxField = list.get(i).trim();

			if (toxField.equals("Toxic for reproduction (Article 57c)")) {
				createScoreRecord(chemical.scoreReproductive, chemical,"Toxic for reproduction (Article 57c)", strDefaultScore);
			} else if (toxField.equals("Carcinogenic (Article 57a)")) {
				createScoreRecord(chemical.scoreCarcinogenicity, chemical,"Carcinogenic (Article 57a)", ScoreRecord.scoreVH);
			} else if (toxField.equals("Mutagenic (Article 57b)")) {
				createScoreRecord(chemical.scoreGenotoxicity_Mutagenicity, chemical,"Mutagenic (Article 57b)", ScoreRecord.scoreVH);
			} else if (toxField.equals("Endocrine disrupting properties (Article 57(f) - environment)")) {
				// TODO
			} else if (toxField.equals("Endocrine disrupting properties (Article 57(f) - human health)")) {
				createScoreRecord(chemical.scoreEndocrine_Disruption,chemical,
						"Endocrine disrupting properties (Article 57(f) - environment)", strDefaultScore);
			} else if (toxField.equals("Respiratory sensitising properties (Article 57(f) - human health)")) {
				// TODO
			} else if (toxField
					.equals("Specific target organ toxicity after repeated exposure (Article 57(f) - human health)")) {
				createScoreRecord(chemical.scoreSystemic_Toxicity_Repeat_Exposure,chemical,
						"Specific target organ toxicity after repeated exposure (Article 57(f) - human health)",
						strDefaultScore);
			} else if (toxField.equals("vPvB (Article 57 e)")) {
				createScoreRecord(chemical.scorePersistence,chemical, "vPvB (Article 57 e)", ScoreRecord.scoreVH);
				createScoreRecord(chemical.scoreBioaccumulation, chemical,"vPvB (Article 57 e)", ScoreRecord.scoreVH);
			} else if (toxField.equals("PBT (Article 57 d)") && !list.contains("vPvB (Article 57 e)")) {
				createScoreRecord(chemical.scorePersistence, chemical,"PBT (Article 57 d)", ScoreRecord.scoreH);
				createScoreRecord(chemical.scoreBioaccumulation,chemical, "PBT (Article 57 d)", ScoreRecord.scoreH);
			} else if (toxField.equals("")) {
				// do nothing
			} else {
				// System.out.println(chemical.CAS+"\t"+i+"\t"+toxField);
			}

		}

		String Date_of_Inclusion;
		String Intrinsic_Property_Article_57;
		String Decision;

		return chemical;
	}

}