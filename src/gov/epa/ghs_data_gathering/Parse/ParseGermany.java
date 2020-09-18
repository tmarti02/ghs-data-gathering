package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
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
import gov.epa.ghs_data_gathering.Parse.ParseHealth_Canada_Priority_Substances_Carcinogenicity_2006.CarcinogenicityRecords;
import gov.epa.ghs_data_gathering.Utilities.DoubleTypeAdapter;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

public class ParseGermany extends Parse {

	public ParseGermany() {
		sourceName = ScoreRecord.sourceGermany;
		fileNameSourceExcel = "eMAK17.xls";
		init();
	}
	
	static class GermanyEMAKRecord {

		String CAS_No;
		String RAO;
		String REF;
		String VGL;
		String Substance_Name;
		String MAK_PPM;
		String MAK_MGR;
		String Peak_Limitation;
		String H_S;
		String Carcinogen_Category;
		String Pregnancy_Risk_Group;
		String Germ_Cell_Mutagen_Category;
		String LB;

		public String toString() {
			return CAS_No + "\t" + RAO + "\t" + REF + "\t" + VGL + "\t" + Substance_Name + "\t" + MAK_PPM + "\t"
					+ MAK_MGR + "\t" + Peak_Limitation + "\t" + H_S + "\t" + Carcinogen_Category + "\t"
					+ Pregnancy_Risk_Group + "\t" + Germ_Cell_Mutagen_Category + "\t" + LB + "\r\n";
		}

	}

	private Vector<GermanyEMAKRecord> parseExcelFile(String excelFilePath) {

		try {

			Vector<GermanyEMAKRecord> data_field = new Vector<GermanyEMAKRecord>();

			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new HSSFWorkbook(inputStream);
			Sheet firstSheet = workbook.getSheetAt(0);

			int row = 1;

			while (true) {
				Row nextRow = firstSheet.getRow(row);

				if (nextRow == null) {
					break;
				}

				GermanyEMAKRecord ar = createDataField(nextRow);

				data_field.add(ar);

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

	private static GermanyEMAKRecord createDataField(Row row) {
		GermanyEMAKRecord gr = new GermanyEMAKRecord();
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
		Cell cell10 = row.getCell(10);
		Cell cell11 = row.getCell(11);
		Cell cell12 = row.getCell(12);

		gr.CAS_No = formatter.formatCellValue(cell0);
		gr.RAO = formatter.formatCellValue(cell1);
		gr.REF = formatter.formatCellValue(cell2);
		gr.VGL = formatter.formatCellValue(cell3);
		gr.Substance_Name = formatter.formatCellValue(cell4);
		gr.MAK_PPM = formatter.formatCellValue(cell5);
		gr.MAK_MGR = formatter.formatCellValue(cell6);
		gr.Peak_Limitation = formatter.formatCellValue(cell7);
		gr.H_S = formatter.formatCellValue(cell8);
		gr.Carcinogen_Category = formatter.formatCellValue(cell9);
		gr.Pregnancy_Risk_Group = formatter.formatCellValue(cell10);
		gr.Germ_Cell_Mutagen_Category = formatter.formatCellValue(cell11);
		gr.LB = formatter.formatCellValue(cell12);

		return gr;
	}

	
	@Override
	protected void createRecords() {
		Vector<GermanyEMAKRecord> records = parseExcelFile(mainFolder + "/" + fileNameSourceExcel);
		writeOriginalRecordsToFile(records);
	}
	
	
	private Chemical createChemical(GermanyEMAKRecord gr) {

		// A,B,C,D pregnancy risk groups or âââ (see p. 216)
		// H danger of percutaneous absorption (see p. 213)
		// Sa danger of sensitization of the airways (see Section IV p. 190)
		// Sh danger of sensitization of the skin (see Section IV p. 188)
		// Sah danger of sensitization of the airways and the skin (see Section IV p.
		// 192)
		// SP danger of photocontact sensitization (see Section IV p. 187)
		// Â° not registered as a pesticide
		// I/II peak limitation categories (excursion factors in parentheses), or âââ
		// (see p. 212)

		Chemical chemical = new Chemical();

		chemical.CAS = gr.CAS_No;
		chemical.name = gr.Substance_Name;

		assignValuesFromH_S(gr, chemical);
		assignValuesFromCancer(gr, chemical);
		assignValuesFromMutagenicity(gr, chemical);
		assignValuesFromPregnancy(gr, chemical);

		return chemical;

	}

	private void assignValuesFromCancer(GermanyEMAKRecord gr, Chemical chemical) {

		String cancer = gr.Carcinogen_Category.trim().replace(".0", "");

		if (cancer.equals(""))
			return;

		String strScore = null;

		// TODO- check these score assignments:
		if (cancer.equals("1") || cancer.equals("2")) {
			strScore = ScoreRecord.scoreVH;
		} else if (cancer.equals("3A") || cancer.equals("3B")) {
			strScore = ScoreRecord.scoreH;
		} else if (cancer.equals("4") || cancer.equals("5")) {
			strScore = ScoreRecord.scoreM;
		} else if (cancer.equals("-")) {
			strScore = ScoreRecord.scoreNA;
		} else {
			System.out.println(chemical.CAS + "\tcancer_unknown:\t" + cancer);
		}

		if (strScore != null) {
			
			Score score = chemical.scoreCarcinogenicity;
			
			ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
			sr.score = strScore;
			sr.category = "Category " + cancer;
			sr.source = this.sourceName;
			sr.rationale = "Score of " + sr.score + " was assigned based on a carcinogenicity category of "
					+ sr.category;
			
			score.records.add(sr);
		}

	}

	private void assignValuesFromH_S(GermanyEMAKRecord gr, Chemical chemical) {
		/*
		 * Germany skin sensitization:
		 * 
		 * From Germany’s List of MAK and BAT values 2017: “Sensitizing substances are
		 * indicated in the List of MAK and BAT Values in the column “H;S” by “Sa” or
		 * “Sh”. This designation refers only to the organ or organ system in which the
		 * allergic reaction is manifested. The pathological mechanism producing the
		 * symptoms is not taken into account. 
		 * 
		 * “Sh”: substances which can cause allergic reactions of the skin and the mucosa close to the skin (skin‐sensitizing substances). 
		 * 
		 * “Sa”: (substances causing airway sensitization) indicates that a sensitization can involve symptoms of
		 * the airways and also of the conjunctiva, and that other effects associated
		 * with reactions of immediate type are also possible. These include systemic
		 * effects (anaphylaxis) and local effects on the skin (urticaria). The latter
		 * reactions only result in the additional designation with “Sh” when the skin
		 * symptoms are relevant under workplace conditions.”
		 *
		 * 
		 */
		
		gr.H_S = gr.H_S.replace("  ", " ");

		LinkedList<String> vals = Utilities.Parse(gr.H_S, " ");

		for (int i = 0; i < vals.size(); i++) {
			String vali = vals.get(i);

			String strScore = null;

			if (vali.equals("Sh") || vali.equals("Sah")) {
				strScore = ScoreRecord.scoreH;
			} else if (vali.equals("Sa")) {
				// TODO add respiratory sensitization?
			} else if (vali.equals("SP")) {
				// TODO add photocontact sensitization?
			} else if (vali.equals("H")) {
				// TODO add danger of percutaneous absorption?
			} else if (vali.equals("") || vali.equals("-")) {
				// blank value, do nothing
			} else {
				System.out.println(chemical.CAS + "\tH_S_unknown:\t" + vali);
			}
			
			//Sh  H (or maybe M)
			//Sah H (or maybe M)


			if (strScore != null) {
				Score score = chemical.scoreSkin_Sensitization;
				
				ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
				sr.score = strScore;
				sr.category = vali;
				sr.source = this.sourceName;
				sr.rationale = "Score of " + sr.score + " was assigned based on a category of " + sr.category;

				score.records.add(sr);
			}

		} // end loop over vals
	}

	/**
	 * 
	 * @param gr
	 * @param chemical
	 */
	private void assignValuesFromMutagenicity(GermanyEMAKRecord gr, Chemical chemical) {
		
		/*
		 *
		 * From Germany’s List of MAK and BAT values 2017: “The categories for
		 * classification of germ cell mutagens have been established in analogy to the
		 * categories for carcinogenic chemicals at the workplace. 
		 * 
		 * 1. Germ cell mutagens
		 * which have been shown to increase the mutant frequency in the progeny of
		 * exposed humans 
		 * 
		 * 2. Germ cell mutagens which have been shown to increase the
		 * mutant frequency in the progeny of exposed mammals 
		 * 
		 * 3A. Substances which have
		 * been shown to induce genetic damage in germ cells of humans or animals, or
		 * which produce mutagenic effects in somatic cells of mammals in vivo and have
		 * been shown to reach the germ cells in an active form 
		 * 
		 * 3B. Substances which are suspected of being germ cell mutagens because of their genotoxic effects in
		 * mammalian somatic cells in vivo; in exceptional cases, substances for which
		 * there are no in vivo data but which are clearly mutagenic in vitro and
		 * structurally related to known in vivo mutagens 
		 * 
		 * 4. not applicable ( **) 
		 * 
		 * 5. Germ cell mutagens or suspected substances (according to the definition of
		 * Category 3A and 3B), the potency of which is considered to be so low that,
		 * provided the MAK and BAT values are observed, their contribution to genetic
		 * risk for man is considered to be very slight ( **) Category 4 carcinogenic
		 * substances are those with non‐genotoxic mechanisms of action. By definition,
		 * germ cell mutagens are genotoxic. Therefore, a Category 4 for germ cell
		 * mutagens cannot apply. At some time in the future it is conceivable that a
		 * Category 4 could be established for genotoxic substances with primary targets
		 * other than the DNA (e. g. purely aneugenic substances) if research results
		 * make this seem sensible.”
		 * 
		 * 
		 */
		
		
		String mutagenicity = gr.Germ_Cell_Mutagen_Category.trim().replace(".0", "");

		if (mutagenicity.equals(""))
			return;

		String strScore = null;

		if (mutagenicity.equals("1")) {
			strScore = ScoreRecord.scoreVH;
		} else if(mutagenicity.equals("2")) {
			strScore = ScoreRecord.scoreH;
		} else if (mutagenicity.equals("3A") || mutagenicity.equals("3B")) {
			strScore = ScoreRecord.scoreM;
		} else if (mutagenicity.equals("5")) {
			strScore = ScoreRecord.scoreL;
		} else {
			System.out.println(chemical.CAS + "\tmutagenicity_unknown:\t" + mutagenicity);
		}
		


		if (strScore != null) {
			Score score = chemical.scoreGenotoxicity_Mutagenicity;
			
			ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
			sr.score = strScore;
			sr.category = "Category " + mutagenicity;
			sr.source = this.sourceName;
			sr.rationale = "Score of " + sr.score + " was assigned based on a germ cell mutagenicity category of "
					+ sr.category;
			score.records.add(sr);
		}

	}

	/**
	 * 
	 * 	 * 
	 * @param gr
	 * @param chemical
	 */
	private void assignValuesFromPregnancy(GermanyEMAKRecord gr, Chemical chemical) {

		/*
		 * “Pregnancy risk groups Based on the criteria mentioned, the Commission is
		 * evaluating substances with MAK or BAT values as to whether prenatal toxic
		 * effects are unlikely when the MAK value or the BAT value is observed (Group
		 * C), whether, according to the currently available information, such effects
		 * cannot be excluded (Group B) or whether they have been unequivocally
		 * demonstrated (Group A). For a number of substances, however, it is not yet
		 * possible to make a statement as to prenatal toxicity (Group D).
		 * 
		 * The following pregnancy risk groups are defined:
		 * 
		 * Group A: Damage to the embryo or foetus in humans has been unequivocally
		 * demonstrated and is to be expected even when MAK and BAT values are observed.
		 * 
		 * Group B: According to currently available information damage to the embryo or
		 * foetus cannot be excluded after exposure to concentrations at the level of
		 * the MAK and BAT values. The documentation indicates, when the Commission’s
		 * assessment of the data makes it possible, which concentration would
		 * correspond to the classification in Pregnancy Risk Group C. Substances with
		 * this indication have the footnote “prerequisite for Group C, see
		 * documentation”.
		 * 
		 * Group C: Damage to the embryo or foetus is unlikely when the MAK value or the
		 * BAT value is observed.
		 * 
		 * Group D: Either there are no data for an assessment of damage to the embryo
		 * or foetus or the currently available data are not sufficient for
		 * classification in one of the groups A – C. Substances without a MAK or BAT
		 * value (carcinogenic substances or substances included in Section IIb) have no
		 * entry (−) in this column.”
		 * 
		 */
		
		
		String preg = gr.Pregnancy_Risk_Group;

		if (preg.equals("") || preg.equals("-"))
			return;

		String strScore = null;

		// TODO- check these score assignments:
		if (preg.equals("A")) {
			strScore = ScoreRecord.scoreVH;
		} else if (preg.equals("B")) {
			strScore = ScoreRecord.scoreH;
		} else if (preg.equals("C")) {
			strScore = ScoreRecord.scoreL;// damage unlikely at MAK or BAT- assign M?
		} else if (preg.equals("D")) {
			strScore = ScoreRecord.scoreNA;// insufficient data
		} else {
			System.out.println(chemical.CAS + "\tpregnancy_unknown:\t" + preg);
		}

		

		
		if (strScore != null) {
			Score score = chemical.scoreDevelopmental;
			ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
			sr.score = strScore;
			sr.category = "Pregancy risk group " + preg;
			sr.source = this.sourceName;
			sr.rationale = "Score of " + sr.score + " was assigned based on a pregnancy risk group of " + sr.category;
			score.records.add(sr);
		}

	}


	@Override
	protected Chemicals goThroughOriginalRecords() {
		
		Chemicals chemicals=new Chemicals();
		
		String jsonFilePath = mainFolder + File.separator + fileNameJSON_Records;

		Gson gson = new GsonBuilder().registerTypeAdapter(double.class, new DoubleTypeAdapter()).create();

		try {

			GermanyEMAKRecord[] records = gson.fromJson(new FileReader(jsonFilePath), GermanyEMAKRecord[].class);

			for (int i = 0; i < records.length; i++) {
				GermanyEMAKRecord gr = records[i];

				Chemical chemical = this.createChemical(gr);
				handleMultipleCAS(chemicals, chemical);

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
		
	}

	public static void main(String[] args) {

		ParseGermany pe = new ParseGermany();


		pe.createFiles();

	}

}
