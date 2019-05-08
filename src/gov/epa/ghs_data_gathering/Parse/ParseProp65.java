package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.ghs_data_gathering.Parse.ParseReachVeryHighConcernList.ReachVeryHighConcernListRecords;
import gov.epa.ghs_data_gathering.API.AADashboard;
import gov.epa.ghs_data_gathering.API.Chemical;
import gov.epa.ghs_data_gathering.API.Chemicals;
import gov.epa.ghs_data_gathering.API.Score;
import gov.epa.ghs_data_gathering.API.ScoreRecord;

public class ParseProp65 extends Parse {

		public ParseProp65() {
		sourceName = ScoreRecord.sourceProp65;
		fileNameSourceExcel = "p65listlinked111017_1.xlsx";
		init();
	}

	static class Prop65Records {
		String Chemical;
		String Type_of_Toxicity;
		String Listing_Mechanism;
		String Listing_MechanismURL;
		String CAS_No;
		String Date_Listed;
		String NSRL_or_MADL;

	}

	private Vector<Prop65Records> parseExcelFile(String excelFilePath) {

		try {

			Vector<Prop65Records> data_field = new Vector<Prop65Records>();

			File file = new File(excelFilePath);
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(0);

			int row = 14;

			while (row < 1006) {
				Row nextRow = sheet.getRow(row);

				if (nextRow == null) {
					break;
				}

				Prop65Records pr = createDataField(nextRow);

				data_field.add(pr);

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

	private static Prop65Records createDataField(Row row) {
		Prop65Records pr = new Prop65Records();
		DataFormatter formatter = new DataFormatter();

		Cell cell0 = row.getCell(0);
		Cell cell1 = row.getCell(1);
		Cell cell2 = row.getCell(2);
		Cell cell3 = row.getCell(3);
		Cell cell4 = row.getCell(4);
		Cell cell5 = row.getCell(5);

		pr.Chemical = formatter.formatCellValue(cell0);
		pr.Type_of_Toxicity = formatter.formatCellValue(cell1);
		pr.Listing_Mechanism = formatter.formatCellValue(cell2);

		if (cell2.getHyperlink() != null)
			pr.Listing_MechanismURL = cell2.getHyperlink().getAddress();

		pr.CAS_No = formatter.formatCellValue(cell3);
		pr.Date_Listed = formatter.formatCellValue(cell4);
		pr.NSRL_or_MADL = formatter.formatCellValue(cell5);

		return pr;
	}

	
	@Override
	protected void createRecords() {
		Vector<Prop65Records> records = parseExcelFile(mainFolder + "/" + fileNameSourceExcel);
		writeOriginalRecordsToFile(records);
	}
	
	

	public static void main(String[] args) {

		ParseProp65 pc = new ParseProp65();
		pc.createFiles();

	}

	
	
	
	
	protected Chemicals goThroughOriginalRecords() {
		
		Chemicals chemicals=new Chemicals();
		
		try {
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();

			File jsonFile = new File(mainFolder + "/" + fileNameJSON_Records);
			Prop65Records[] records = gson.fromJson(new FileReader(jsonFile), Prop65Records[].class);

			for (int i = 0; i < records.length; i++) {

				Prop65Records ir = records[i];
				if (ir.CAS_No.equals("") && ir.Chemical.equals("")) continue;
				if (ir.Type_of_Toxicity.equals("")) continue;
				
				Chemical chemicalOld = chemicals.getChemical(ir.CAS_No);

				if (chemicalOld!=null) {
					Chemical chemical2 = this.createChemical(ir);
					
					if (chemical2==null) {
//						System.out.println(gson.toJson(ir));
						continue;
					}
					chemicalOld.addRecords(chemical2);
//					System.out.println("adding records:"+gson.toJson(chemicalOld));
				} else {
					Chemical chemical = this.createChemical(ir);
					handleMultipleCAS(chemicals, chemical);
				}
				
			} // end loop over records

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	private Chemical createChemical(Prop65Records ir) {
		// TODO Auto-generated method stub
		// if (ir.Group.equals("")) return null;

		Chemical chemical = new Chemical();
		chemical.CAS = ir.CAS_No.trim().replace(" ", "");
		chemical.name = ir.Chemical;
		if (ir.Listing_Mechanism.equals(""))
			return null;
		createScoreRecord(ir, chemical);
		return chemical;
	}

	private void createScoreRecord(Prop65Records ir, Chemical chemical) {
		// Create carcinogenicity score record:
		ScoreRecord sr = new ScoreRecord();
		sr.source = ScoreRecord.sourceProp65;

		Score score = null;

		// if (chemical.CAS.equals("79-06-1")) {
		// System.out.println(ir.Type_of_Toxicity);
		// }

		if (ir.Type_of_Toxicity.indexOf("cancer") > -1) {
			score = chemical.scoreCarcinogenicity;
			sr.category = "Carcinogen";
			sr.score = ScoreRecord.scoreVH;
		} else if (ir.Type_of_Toxicity.indexOf("developmental") > -1) {
			score = chemical.scoreDevelopmental;
			sr.category = ir.Type_of_Toxicity;
			sr.score = ScoreRecord.scoreH;
			// System.out.println(sr.category);

		} else if (ir.Type_of_Toxicity.indexOf("male") > -1 || ir.Type_of_Toxicity.indexOf("female") > -1) {
			score = chemical.scoreReproductive;
			// System.out.println("*"+chemical.CAS+"\t"+ir.Type_of_Toxicity+"\t"+ir.Listing_Mechanism);

			sr.category = ir.Type_of_Toxicity.trim() + " reproductive toxicity";
			sr.score = ScoreRecord.scoreH;
			// System.out.println(sr.category);
		} else {
			System.out.println("*" + chemical.CAS + "\t" + ir.Type_of_Toxicity + "\t" + ir.Listing_Mechanism);
		}

		// if (sr.category.equals("Carcinogen")) {
		// sr.score=ScoreRecord.scoreVH;
		// } else {
		// sr.score=ScoreRecord.scoreH;
		// }

		

		// sr.hazard_statement="Carcinogenic to humans";
		// sr.category="Group "+ir.Group;
		sr.rationale = "Score of " + sr.score + " was assigned based on a carcinogenicity category of " + sr.category;

		score.records.add(sr);

		// if (chemical.CAS.equals("79-06-1")) {
		// System.out.println("*0*" + score.hazard_name + "\t" + score.records.size());
		//
		// System.out.println("**" + chemical.scores.get(7).hazard_name + "\t" +
		// chemical.scores.get(7).records.size());
		// System.out.println("**" + chemical.scores.get(3).hazard_name + "\t" +
		// chemical.scores.get(3).records.size());
		//
		//// for (int j = 0; j < chemical.scores.size(); j++) {
		////
		//// System.out.println(j+"\t"+chemical.scores.get(j).hazard_name);
		////
		//// if (chemical.scores.get(j).records.size() > 0) {
		//// System.out.println(j + "\t" + chemical.CAS + "\t" +
		// chemical.scores.get(j).hazard_name + "\t"
		//// + chemical.scores.get(j).records.get(0).score);
		//// }
		//// }
		// }

	}

}