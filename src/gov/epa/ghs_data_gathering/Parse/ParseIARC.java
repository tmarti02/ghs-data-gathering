package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
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
import gov.epa.ghs_data_gathering.Parse.ParseIRIS.IRISRecord;

public class ParseIARC extends Parse {

	public ParseIARC() {
		sourceName = ScoreRecord.sourceIARC;
		fileNameSourceExcel = "Carcinogenicity.xlsx";
		init();
	}
		
	static class IARCRecords {
		String CAS_No;
		String Agent;
		String Group;
		String Volume;
		String Year;
		String Additional_Information;

	}

	private Vector<IARCRecords> parseExcelFile(String excelFilePath) {

		try {

			Vector<IARCRecords> data_field = new Vector<IARCRecords>();

			File file = new File(excelFilePath);
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet secondSheet = workbook.getSheetAt(1);

			int row = 2;

			while (true) {
				Row nextRow = secondSheet.getRow(row);

				if (nextRow == null) {
					break;
				}

				IARCRecords ir = createDataField(nextRow);

				data_field.add(ir);

				row++;
			}

			inputStream.close();
			return data_field;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private static IARCRecords createDataField(Row row) {
		IARCRecords ir = new IARCRecords();
		DataFormatter formatter = new DataFormatter();

		Cell cell0 = row.getCell(0);
		Cell cell1 = row.getCell(1);
		Cell cell2 = row.getCell(2);
		Cell cell3 = row.getCell(3);
		Cell cell4 = row.getCell(4);
		Cell cell5 = row.getCell(5);

		ir.CAS_No = formatter.formatCellValue(cell0);
		ir.Agent = formatter.formatCellValue(cell1);
		ir.Group = formatter.formatCellValue(cell2);
		ir.Volume = formatter.formatCellValue(cell3);
		ir.Year = formatter.formatCellValue(cell4);
		ir.Additional_Information = formatter.formatCellValue(cell5);

		return ir;
	}
	
	
	@Override
	protected void createRecords() {
		Vector<IARCRecords> records = parseExcelFile(mainFolder + "/" + fileNameSourceExcel);
		writeOriginalRecordsToFile(records);
	}


	public static void main(String[] args) {

		ParseIARC pc = new ParseIARC();
		pc.createFiles();

	}

	
	@Override
	protected Chemicals goThroughOriginalRecords() {
		
		Chemicals chemicals=new Chemicals();
		try {
			Gson gson = new Gson();
			File jsonFile = new File(mainFolder + "/" + fileNameJSON_Records);
			IARCRecords[] records = gson.fromJson(new FileReader(jsonFile), IARCRecords[].class);

			for (int i = 0; i < records.length; i++) {

				IARCRecords ir = records[i];
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

	private Chemical createChemical(IARCRecords ir) {

		if (ir.Group.equals(""))
			return null;

		Chemical chemical = new Chemical();
		chemical.CAS = ir.CAS_No;
		chemical.name = ir.Agent;

		Score score = chemical.scoreCarcinogenicity;

		// Create carcinogenicity score record:
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		score.records.add(sr);

		sr.source = ScoreRecord.sourceIARC;
		sr.category = "Group " + ir.Group;

		if (ir.Group.equals("1")) {
			sr.score = ScoreRecord.scoreVH;
			sr.hazard_statement = "Carcinogenic to humans";
		} else if (ir.Group.equals("2A")) {
			sr.score = ScoreRecord.scoreVH;
			sr.hazard_statement = "Probably carcinogenic to humans";
		} else if (ir.Group.equals("2B")) {
			sr.score = ScoreRecord.scoreH;
			sr.hazard_statement = "Possibly carcinogenic to humans";
		} else if (ir.Group.equals("3")) {
			sr.hazard_statement = "Not classifiable as to its carcinogenicity to humans";
			sr.score = ScoreRecord.scoreNA;
		} else if (ir.Group.equals("4")) {
			sr.hazard_statement = "Probably not carcinogenic to humans";
			sr.score = ScoreRecord.scoreL;// TODO- assign M instead? only 1 chemical
		} else {
			System.out.println(chemical.CAS + "\t" + chemical.name + "\t" + ir.Group);
		}

		sr.rationale = "Score of " + sr.score + " was assigned based on a carcinogenicity category of " + sr.category;
		sr.note = "Volume " + ir.Volume + ", " + ir.Year + ".";

		if (!ir.Additional_Information.equals(""))
			sr.note += " " + ir.Additional_Information + ".";
		
		
		
		if (chemical.CAS.indexOf("<br/>")>-1) {
			if (!sr.note.equals("") ) sr.note+="<br><br>\n\n";
			sr.note+="Record is for multiple CAS numbers: "+chemical.CAS.replace("<br/>", "; ").replace("\n","").replace("*","");
			
//			System.out.println(sr.note);
		}


		return chemical;
	}

}