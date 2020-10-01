package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.AADashboard;
import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.ParseKorea.KoreaRecord;

public class ParseIRIS extends Parse {

	public ParseIRIS() {
		sourceName = ScoreRecord.sourceIRIS;
		fileNameSourceExcel = "IRISTR_v1b_544_15Feb2008_nostructures.xls";
		init();
	}
	
	/**
	 * 
	 * Pertinent cancer data extracted from DSSTox file for IRIS
	 * 
	 * @author Todd Martin
	 *
	 */
	static class IRISRecord {

		String TestSubstance_ChemicalName;
		String TestSubstance_CASRN;
		String STRUCTURE_MolecularWeight;

		String WtOfEvidence_Cancer_Concern;
		String WtOfEvidence_1986GuidelineCategories;
		String WtOfEvidence_UpdatedGuidelinesUsed;
		String WtOfEvidence_Cancer_Narrative;

	}

	@Override
	protected void createRecords() {
		Vector<IRISRecord> records = parseExcelFile(mainFolder + "/" + fileNameSourceExcel);
		writeOriginalRecordsToFile(records);
	}
	

	private Vector<IRISRecord> parseExcelFile(String excelFilePath) {

		try {
			Vector<IRISRecord> records = new Vector<IRISRecord>();

			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new HSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(0);

			Row headerRow = sheet.getRow(0);

			int row = 1;

			while (true) {
				Row nextRow = sheet.getRow(row);

				if (nextRow == null) {
					break;
				}

				IRISRecord ir = createRecord(nextRow, headerRow);

				// System.out.println(ir.TestSubstance_ChemicalName+"\t"+ir.TestSubstance_CASRN);
				records.add(ir);
				row++;
			}

			workbook.close();
			inputStream.close();
			return records;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private String getValue(String name, Row hRow, Row row) {

		DataFormatter f = new DataFormatter();

		for (int i = 0; i < hRow.getLastCellNum(); i++) {
			if (f.formatCellValue(hRow.getCell(i)).equals(name)) {
				return f.formatCellValue(row.getCell(i));
			}
		}

		return "";

	}

	private IRISRecord createRecord(Row row, Row hRow) {
		IRISRecord dr = new IRISRecord();

		dr.TestSubstance_ChemicalName = this.getValue("TestSubstance_ChemicalName", hRow, row).trim();
		dr.TestSubstance_CASRN = this.getValue("TestSubstance_CASRN", hRow, row).trim();
		dr.STRUCTURE_MolecularWeight = this.getValue("STRUCTURE_MolecularWeight", hRow, row).trim();

		dr.WtOfEvidence_Cancer_Concern = this.getValue("WtOfEvidence_Cancer_Concern", hRow, row).trim();
		dr.WtOfEvidence_1986GuidelineCategories = this.getValue("WtOfEvidence_1986GuidelineCategories", hRow, row)
				.trim();
		dr.WtOfEvidence_UpdatedGuidelinesUsed = this.getValue("WtOfEvidence_UpdatedGuidelinesUsed", hRow, row).trim();
		dr.WtOfEvidence_Cancer_Narrative = this.getValue("WtOfEvidence_Cancer_Narrative", hRow, row).trim();

		return dr;
	}

	
	@Override
	protected Chemicals goThroughOriginalRecords() {
		Chemicals chemicals=new Chemicals();
		
		try {
			Gson gson = new Gson();
			File jsonFile = new File(mainFolder + "/" + fileNameJSON_Records);
			IRISRecord[] records = gson.fromJson(new FileReader(jsonFile), IRISRecord[].class);

			for (int i = 0; i < records.length; i++) {

				IRISRecord ir = records[i];
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

	private Chemical createChemical(IRISRecord ir) {
		Chemical chemical = new Chemical();

		chemical.name = ir.TestSubstance_ChemicalName;
		chemical.CAS = ir.TestSubstance_CASRN;

		if (!ir.STRUCTURE_MolecularWeight.equals(""))
			chemical.molecularWeight = Double.parseDouble(ir.STRUCTURE_MolecularWeight);

		if (ir.WtOfEvidence_1986GuidelineCategories.equals("Not assessed under the IRIS program.")
				|| ir.WtOfEvidence_1986GuidelineCategories
						.equals("Information reviewed but value not estimated - refer to IRIS Summary.")
				|| ir.WtOfEvidence_1986GuidelineCategories.equals(
						"Not applicable. This substance was not assessed using the 1986 cancer guidelines (US EPA 1986).")) {
			return null;
		}

		Score score=chemical.scoreCarcinogenicity;
		
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		score.records.add(sr);

		sr.source = ScoreRecord.sourceIRIS;

		String WtOfEvidence=ir.WtOfEvidence_1986GuidelineCategories;
		String narrative=ir.WtOfEvidence_Cancer_Narrative;
		
		
		if (WtOfEvidence.equals(
				"(i) A; Human Carcinogen (Inhalation route); (ii) D; Not classifiable as to human carcinogenicity (Oral route)")) {
			sr.category = "Category A (inhalation); Category D (oral)";
			sr.hazardStatement = "Human carcinogen (inhalation); Not classifiable as to human carcinogenicity (oral)";
			sr.score = ScoreRecord.scoreVH;
			sr.rationale = "Score of VH was assigned based on a carcinogenicity category of Category A from inhalation";
//			sr.listType=ScoreRecord.typeAuthoritativeA;
			return chemical;
		}

		// System.out.println(WtOfEvidence);

		sr.category = "Category " + WtOfEvidence.substring(0, WtOfEvidence.indexOf(";"));
		sr.hazardStatement = WtOfEvidence.substring(WtOfEvidence.indexOf(";") + 2, WtOfEvidence.length());

		
//		sr.listType=ScoreRecord.typeAuthoritativeA;//default type
		
		if (sr.category.equals("Category A") || sr.category.equals("Category B1")
				|| sr.category.equals("Category B2")) {
			sr.score = ScoreRecord.scoreVH;
//			System.out.println("here\t"+sr.category+"\t"+WtOfEvidence);
		} else if (sr.category.equals("Category C")) {
			sr.score = ScoreRecord.scoreH;
		} else if (sr.category.equals("Category D")) {
			sr.score = ScoreRecord.scoreNA;
//			sr.listType=ScoreRecord.typeAuthoritativeB; 
		} else if (sr.category.equals("Category E")) {
			sr.score = ScoreRecord.scoreL;
		} else {
			//TODO - is the category ever "Suggestive evidence of carcinogenic potential"? THen it would be Authoritative B
			// System.out.println(sr.category+"\t"+sr.hazard_statement+"\t"+WtOfEvidence);
			
		/*  The categories and the GreenScreen List Translator Mappings are:
			   Group A -- Human carcinogen: H (Authoritative A) (Why H and not VH?)
			   Group B1 -- Probable human carcinogen: H (Authoritative A)
			   Group B2 -- Probable human carcinogen: H (Authoritative A)
			   Group C -- Possible human carcinogen: M (Authoritative A)
			   Group D -- Not classifiable as to human carcinogenicity: Hazard Range: H, M, or L, Score UNK (unknown)
																(Authoritative B because multiple possible scores)
			Group E -- Evidence of non-carcinogenicity for humans: L (Authoritative A)
			Thus, GreenScreen List Translator actually has a less conservative scoring scale than the code above.
			A challenge is distinguishing between strength of evidence and strength of effect or potency. For example,
			"probable carcinogen" is different than "carcinogenic at high doses."
			
			DfE and GreenScreen have different criteria for classifying carcinogenicity.
			
			DfE criteria for carcinogenicity:
			Known or presumed human carcinogen (GHS Category 1A and 1B): VH
			Suspected human carcinogen (GHS Category 2): H
			Limited or marginal evidence of carcinogenicity in animals (and inadequate evidence in humans): M
			Negative studies or robust mechanism-based SAR: L
			*/
			
			System.out.println(sr.category+"\t"+WtOfEvidence);
		}
		
		sr.rationale = "Score of " + sr.score + " was assigned based on a carcinogenicity category of " + sr.category;
		// sr.note="Based on "+year+" guidelines";
		sr.note = narrative;


		return chemical;
	}

	private void createRecord(Score score, IRISRecord ir) {
		

		// System.out.println(sr.category+"\t"+sr.hazard_statement+"\t"+sr.score);

	}

	public static void main(String[] args) {
		ParseIRIS parseIRIS = new ParseIRIS();

		parseIRIS.createFiles();

	}

}
