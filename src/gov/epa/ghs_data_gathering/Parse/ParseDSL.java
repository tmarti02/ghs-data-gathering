package gov.epa.ghs_data_gathering.Parse;

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
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.ParseECHACLP.ECHACLPRecord;

public class ParseDSL extends Parse {

	/**
	 * Categorizing substances on the domestic substances list Toxic substances can
	 * have different effects on human health and the environment. A substance that
	 * might be dangerous to wildlife in low levels might have no effect on people
	 * even at much higher levels. The opposite is also true: some substances can do
	 * more harm to humans than to the environment.
	 * 
	 * For this reason, Health Canada and Environment and Climate Change Canada used
	 * different but complementary criteria when sorting through the approximately
	 * 23 000 substances on the domestic substances list (DSL). This was to make
	 * sure that every substance that could potentially affect human health or our
	 * environment was evaluated to determine the need for further attention.
	 * 
	 * Environment Persistence, bioaccumulation and inherent toxicity to the
	 * environment Under the Canadian Environmental Protection Act 1999 (CEPA), the
	 * Existing Substances Evaluation Program is responsible for categorizing DSL
	 * substances to identify those that are suspected to be either:
	 * 
	 * persistent (P): chemical substances that take a very long time to break down
	 * in the environment - sometimes many years. These substances can affect the
	 * environment for a long period of time. Because they last for so long, they
	 * can travel long distances and pollute a much wider area than those that break
	 * down quickly.
	 * 
	 * bioaccumulative (B): chemical substances that can be stored in the organs,
	 * fat cells or blood of living organisms and remain for a long time. Over time,
	 * concentrations can build up and reach very high levels, and can also be
	 * transferred up the food chain.
	 * 
	 * or
	 * 
	 * inherently toxic to the environment (iTE): chemical substances that are known
	 * or suspected, through laboratory and other studies, to have a harmful effect
	 * on wildlife and the natural environment on which it depends. Human health
	 * Greatest potential for human exposure and inherent toxicity to humans Under
	 * CEPA, the Existing Substances Program at Health Canada is responsible for
	 * identifying substances that have the greatest potential for exposure and are
	 * inherently toxic to humans.
	 * 
	 * greatest potential for exposure (GPE): when assessing human exposure to
	 * chemical substances, scientists look at more than persistence and
	 * bioaccumulation. Some shorter-lived substances might affect humans just as
	 * much as persistent ones. To get the complete picture, scientists look at how
	 * a substance is used. Health Canada identified those chemical substances on
	 * the DSL to which people are expected most likely to be exposed.
	 * 
	 * inherently toxic to humans (iTH): these are chemical substances that are
	 * known or suspected of having harmful effects on humans. Substances were
	 * examined for a number of human health effects, including cancer, birth
	 * defects and damage to genetic material. Chemical substances that can
	 * potentially affect human health were also placed in a priority sequence so
	 * the Government of Canada can first deal with those suspected of presenting
	 * the highest hazard and greatest potential for exposure.
	 * 
	 * 
	 */
	
	
	public ParseDSL() {
		sourceName = ScoreRecord.sourceDSL;
		fileNameSourceExcel = "DSL_20060905_eng.xls";
		init();
	}
	
	static class DSLRecords {

		String CAS;
		String Chemical_Name;
		String Substance_Category;
		String Meets_CEPA_Categorization_Criteria;
		String Meets_Human_Health_Categorization_Criteria;
		String Human_Health_Priorities;
		String Meets_Environmental_Criteria_For_Categorization;
		String Persistent;
		String Bioaccumulative;
		String Inherently_Toxic_to_Aquatic_Organisms;

	}

	private Vector<DSLRecords> parseExcelFile(String excelFilePath) {

		try {

			Vector<DSLRecords> data_field = new Vector<DSLRecords>();

			File file = new File(excelFilePath);
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new HSSFWorkbook(inputStream);
			Sheet firstSheet = workbook.getSheetAt(0);

			int row = 1;

			while (true) {
				Row nextRow = firstSheet.getRow(row);

				if (nextRow == null) {
					break;
				}

				DSLRecords dl = createDataField(nextRow);

				data_field.add(dl);

				row++;
			}

			inputStream.close();
			return data_field;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	
	private static DSLRecords createDataField(Row row) {
		DSLRecords dl = new DSLRecords();
		DataFormatter formatter = new DataFormatter();

		Cell cell0 = row.getCell(0);
		Cell cell1 = row.getCell(1);
		Cell cell2 = row.getCell(2);
		Cell cell3 = row.getCell(3);
		Cell cell4 = row.getCell(4);
		Cell cell5 = row.getCell(5);
		Cell cell6 = row.getCell(6);
		Cell cell7 = row.getCell(7);
		Cell cell8 = row.getCell(8);
		Cell cell9 = row.getCell(9);

		dl.CAS = formatter.formatCellValue(cell0);
		dl.Chemical_Name = formatter.formatCellValue(cell1);
		dl.Substance_Category = formatter.formatCellValue(cell2);
		dl.Meets_CEPA_Categorization_Criteria = formatter.formatCellValue(cell3);
		dl.Meets_Human_Health_Categorization_Criteria = formatter.formatCellValue(cell4);
		dl.Human_Health_Priorities = formatter.formatCellValue(cell5);
		dl.Meets_Environmental_Criteria_For_Categorization = formatter.formatCellValue(cell6);
		dl.Persistent = formatter.formatCellValue(cell7);
		dl.Bioaccumulative = formatter.formatCellValue(cell8);
		dl.Inherently_Toxic_to_Aquatic_Organisms = formatter.formatCellValue(cell9);

		return dl;
	}

	
	@Override
	protected void createRecords() {
		Vector<DSLRecords> records = parseExcelFile(mainFolder + "/" + this.fileNameSourceExcel);
		writeOriginalRecordsToFile(records);
	}


	@Override
	protected Chemicals goThroughOriginalRecords() {
		
		Chemicals chemicals=new Chemicals();
		
		try {
			Gson gson = new Gson();
			File jsonFile = new File(mainFolder + "/" + fileNameJSON_Records);
			DSLRecords[] records = gson.fromJson(new FileReader(jsonFile), DSLRecords[].class);

			for (int i = 0; i < records.length; i++) {
				if (i % 1000 == 0)
					System.out.println(i);
				DSLRecords roc = records[i];
				Chemical chemical = this.createChemical(roc);
				handleMultipleCAS(chemicals, chemical);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
		
	}

	private void createRecord(Chemical chemical,Score score, String endpoint, String var) {

		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		score.records.add(sr);
		
		sr.name=chemical.name;
		sr.CAS=chemical.CAS;
		sr.hazard_name=score.hazard_name;

		sr.source = ScoreRecord.sourceDSL;

		if (var.equals("Yes")) {
			sr.score = ScoreRecord.scoreH;
			sr.category = endpoint;
		} else if (var.equals("No")) {
			sr.score = ScoreRecord.scoreL;
			sr.category = "Not " + endpoint;
		} else if (var.equals("Uncertain")) {
			sr.score = ScoreRecord.scoreNA;
			sr.category = "Uncertain";
		} else {
			System.out.println(var);
		}

		sr.rationale = "Score of " + sr.score + " was assigned based on a category of \"" + sr.category+"\"";

	}

	private Chemical createChemical(DSLRecords dr) {
		Chemical chemical = new Chemical();

		chemical.CAS = dr.CAS;
		chemical.name = dr.Chemical_Name;

		this.createRecord(chemical,chemical.scoreAcute_Aquatic_Toxicity, "Inherently_Toxic_to_Aquatic_Organisms", dr.Inherently_Toxic_to_Aquatic_Organisms);
		this.createRecord(chemical,chemical.scorePersistence, "Persistent", dr.Persistent);
		this.createRecord(chemical,chemical.scoreBioaccumulation, "Bioaccumulative", dr.Bioaccumulative);
		
		//TODO add aquatic tox record

		// TODO implement any of following as records???
		// "Meets_CEPA_Categorization_Criteria": "No",
		// "Meets_Human_Health_Categorization_Criteria": "No",
		// "Human_Health_Priorities": "",
		// "Meets_Environmental_Criteria_For_Categorization": "No",
		// "Inherently_Toxic_to_Aquatic_Organisms": "No"

		return chemical;
	}

	public static void main(String[] args) {
		ParseDSL pd = new ParseDSL();
		pd.createFiles();

	}

}