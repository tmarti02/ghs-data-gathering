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
import gov.epa.ghs_data_gathering.Parse.ParseGermany.GermanyEMAKRecord;

public class ParseEPAMidAtlanticHumanHealth extends Parse {


	public ParseEPAMidAtlanticHumanHealth() {
		sourceName = ScoreRecord.sourceEPAMidAtlanticHumanHealth;
		fileNameSourceExcel = "EPA mid-Atlantic Region Human Health Risk-Based Concentrations.xls";
		init();
	}
	
	static class CarcinogenicityRecords {

		String Name;
		String Substance_ID;
		String CASRN;
		String Assay_ID;
		String SFO;
		String SFONote;
		String IUR;
		String IURNote;
		String RfDo;
		String RfDoNote;
		String RfCi;
		String RfCiNote;
		String VOC;
		String Mutagen;
		String RAGS_Part_E_GIABS;
		String RAGS_Part_E_ABS;
		String CSAT;

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
		cr.SFO = formatter.formatCellValue(row.getCell(i++));
		cr.SFONote = formatter.formatCellValue(row.getCell(i++));
		cr.IUR = formatter.formatCellValue(row.getCell(i++));
		cr.IURNote = formatter.formatCellValue(row.getCell(i++));
		cr.RfDo = formatter.formatCellValue(row.getCell(i++));
		cr.RfDoNote = formatter.formatCellValue(row.getCell(i++));
		cr.RfCi = formatter.formatCellValue(row.getCell(i++));
		cr.RfCiNote = formatter.formatCellValue(row.getCell(i++));
		cr.VOC = formatter.formatCellValue(row.getCell(i++));
		cr.Mutagen = formatter.formatCellValue(row.getCell(i++));
		cr.RAGS_Part_E_GIABS = formatter.formatCellValue(row.getCell(i++));
		cr.RAGS_Part_E_ABS = formatter.formatCellValue(row.getCell(i++));
		cr.CSAT = formatter.formatCellValue(row.getCell(i++));

		return cr;
	}

	
	@Override
	protected void createRecords() {
		Vector<CarcinogenicityRecords> records = parseExcelFile(mainFolder + "/" + fileNameSourceExcel);
		writeOriginalRecordsToFile(records);
	}
	

	public static void main(String[] args) {

		ParseEPAMidAtlanticHumanHealth ph = new ParseEPAMidAtlanticHumanHealth();
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

				if (chemical == null)
					continue;

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

		if (ir.Mutagen.equals("M")) {
			Score score = chemical.scoreGenotoxicity_Mutagenicity;
			ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
			score.records.add(sr);
			sr.source = ScoreRecord.sourceEPAMidAtlanticHumanHealth;
			sr.category = "Mutagen";
			// Assign score based on toxCode:
			sr.score = ScoreRecord.scoreVH;
			sr.rationale = "Score of " + sr.score + " was assigned based on a category of M (mutagen)";
		} else {
			// System.out.println(chemical.CAS+"\t"+ir.Mutagen);
		}

		if (!ir.SFO.equals("")) {

			Score score = chemical.scoreCarcinogenicity;
			ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
			score.records.add(sr);
			sr.source = ScoreRecord.sourceEPAMidAtlanticHumanHealth;
			sr.category = "Carcinogen";
			// Assign score based on toxCode:
			sr.score = ScoreRecord.scoreVH;
			
			sr.test_type="Cancer slope factor (SFO)";
			sr.valueMass=Double.parseDouble(ir.SFO);
//			sr.valueMassUnits=ir.
			
			
			sr.rationale = "Score of " + sr.score + " was assigned on having a value for the cancer slope factor (SFO)";
			sr.note = "Source: " + ir.SFONote;

//			System.out.println(chemical.CAS + "\t" + ir.SFO);

		}

		// TODO-use any of other fields?

		return chemical;
	}

}
