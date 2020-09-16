package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.AADashboard;
import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.ParseEPAMidAtlanticHumanHealth.CarcinogenicityRecords;

/**
 * This class parses ECHA clp spreadsheet and creates json files using code and
 * score dictionaries
 * 
 * https://echa.europa.eu/information-on-chemicals/annex-vi-to-clp
 * 
 * @author Todd Martin
 *
 */
public class ParseECHACLP extends Parse {


	public ParseECHACLP() {
		sourceName = ScoreRecord.sourceECHA_CLP;
		fileNameSourceExcel = "annex_vi_clp_table_atp_en_December2018.xlsx";
		init();
		
	}

	class ECHACLPRecord {
		String CAS;
		String Chemical_Name;
		String EC_Number;
		String Index_Number;
		String Hazard_Classification;
		String Hazard_Code;
		String Labelling_Pictogram;
		String Labelling_Hazard_Code;
		String Labelling_Suppl_Hazard_Code;
		String M_Factors;
		String Notes;
		String API_Inserted_ATP_Updated;
	}

	// public void addECHA_CLP_Records(Chemical chemical) {
	//
	// String CAS=chemical.CAS;
	// File jsonFile=new File(jsonFolder+"/"+CAS+".json");
	//
	// //TODO load from jar file instead later
	//
	// if (!jsonFile.exists()) {
	//// System.out.println(CAS+" does not exist in ECHA");
	// return;
	// }
	// Chemical chemicalECHA=Chemical.loadFromJSON(jsonFile);
	// chemical.combineRecords(chemical, chemicalECHA);
	//
	// }

	



	

	// public void createJSONFiles() {
	//
	// String filepath = "AA Dashboard/Data/ECHA CLP" + "/" +
	// fileNameSourceExcel.replace(".xlsx", ".txt");
	//
	// String fileNameJSON_Records = "ECHA CLP Records.json";
	//
	// if (AADashboard.generateOriginalJSONRecords) {
	// createRecords(mainFolder, fileNameSourceExcel, fileNameJSON_Records);
	// }
	//
	// String destFolderPath = jsonFolder;
	// File OF = new File(destFolderPath);
	//
	// if (!OF.exists())
	// OF.mkdir();
	//
	// Hashtable<String, String> htHazardStatement =
	// CodeDictionary.getHazardStatementDictionaryH();
	//
	// try {
	//
	// BufferedReader br = new BufferedReader(new FileReader(filepath));
	//
	// for (int i = 1; i <= 3; i++)
	// br.readLine();
	//
	// String h1 = br.readLine();
	// String h2 = br.readLine();
	//
	// int colIndex = ToxPredictor.Utilities.Utilities.FindFieldNumber(h1, "Index
	// No");
	// int colName = ToxPredictor.Utilities.Utilities.FindFieldNumber(h1,
	// "International Chemical Identification");
	// int colEC = ToxPredictor.Utilities.Utilities.FindFieldNumber(h1, "EC No");
	// int colCAS = ToxPredictor.Utilities.Utilities.FindFieldNumber(h1, "CAS No");
	//
	// int colHazardCode = ToxPredictor.Utilities.Utilities.FindFieldNumber(h2,
	// "Hazard Statement Code(s)");
	// int colHazardClass = ToxPredictor.Utilities.Utilities.FindFieldNumber(h2,
	// "Hazard Class and Category Code(s)");
	//
	// int row = 0;
	//
	// while (true) {
	//
	// row++;
	//
	// if (row % 100 == 0)
	// System.out.println(row);
	//
	// String Line = br.readLine();
	//
	// if (Line == null)
	// break;
	//
	// LinkedList<String> list = ToxPredictor.Utilities.Utilities.Parse3(Line,
	// "\t");
	//
	// Chemical chemical = new Chemical();
	//
	// chemical.indexNumber = list.get(colIndex);
	// chemical.CAS = list.get(colCAS);
	// chemical.EC_number = list.get(colEC);
	// chemical.name = list.get(colName);
	//
	// // System.out.println(chemical.CAS);
	//
	// String strHazardCodes = list.get(colHazardCode);
	// String strHazardClasses = list.get(colHazardClass);
	//
	// LinkedList<String> hazardCodeList =
	// ToxPredictor.Utilities.Utilities.Parse3(strHazardCodes, "|");
	// LinkedList<String> hazardClassList =
	// ToxPredictor.Utilities.Utilities.Parse3(strHazardClasses, "|");
	//
	// int count = 0;
	//
	// for (int i = 0; i < hazardClassList.size(); i++) {
	// String hazardClassification = hazardClassList.get(i);
	// String hazardCode = hazardCodeList.get(i);
	//
	// String route = "";
	//
	// if (hazardCode.toLowerCase().indexOf("(inhalation)") > -1) {
	// // store route:
	// route = "inhalation";
	// hazardCode = hazardCode.toLowerCase().replace("(inhalation)", "").trim();
	// // System.out.println(hazardCode+"\t"+route);
	// } else if (hazardCode.toLowerCase().indexOf("(oral)") > -1) {
	// route = "oral";
	// hazardCode = hazardCode.toLowerCase().replace("(oral)", "").trim();
	// // System.out.println(hazardCode+"\t"+route);
	// }
	//
	// hazardCode = hazardCode.replace(" ", "").replace("*", "").replace("h",
	// "H").trim();// TODO - what
	// // does ** mean?
	//
	// String note = "";
	//
	// if (hazardCode.indexOf("(") > -1 && hazardCode.indexOf(")") > -1) {
	// // Store target organs in a note:
	// note = "Target organs = "
	// + hazardCode.substring(hazardCode.indexOf("(") + 1, hazardCode.indexOf(")"));
	// hazardCode = hazardCode.substring(0, hazardCode.indexOf("(")).trim();
	// }
	//
	// hazardClassification = hazardClassification.replace("*", "").trim();// TODO
	// what does * mean?
	//
	// if (hazardClassification.indexOf("Repr.") > -1) {
	// handleReproDevTox(chemical, hazardClassification, hazardCode,
	// htHazardStatement);
	// } else {
	// if (this.dictScore.get(hazardClassification) != null) {
	//
	// // oral
	// if (hazardCode.equals("H300") || hazardCode.equals("H301") ||
	// hazardCode.equals("H302")
	// || hazardCode.equals("H303")) {
	// route = "oral";
	// }
	// // dermal
	// if (hazardCode.equals("H310") || hazardCode.equals("H311") ||
	// hazardCode.equals("H312")
	// || hazardCode.equals("H313")) {
	// route = "dermal";
	// }
	// // inhalation
	// if (hazardCode.equals("H330") || hazardCode.equals("H331") ||
	// hazardCode.equals("H332")
	// || hazardCode.equals("H333")) {
	// route = "inhalation";
	// }
	//
	// String scoreName = this.dictScore.get(hazardClassification);
	// Score score = null;
	//
	// if (scoreName.equals(chemical.strAcute_Mammalian_Toxicity)) {
	// if (route.equals("oral"))
	// score = chemical.scoreAcute_Mammalian_ToxicityOral;
	// else if (route.equals("inhalation"))
	// score = chemical.scoreAcute_Mammalian_ToxicityInhalation;
	// else if (route.equals("dermal"))
	// score = chemical.scoreAcute_Mammalian_ToxicityDermal;
	// } else {
	// score = chemical.getScore(scoreName);
	// }
	//
	// if (dictCode.get(hazardCode) == null) {
	// // System.out.println(chemical.CAS+"\t"+hazardCode);
	//
	// System.out.println(count + "\t" + chemical.CAS + "\t" + hazardClassification
	// + "\t"
	// + hazardCode + "\t" + dictCode.get(hazardCode));
	// continue;
	// }
	//
	// String strScore = dictCode.get(hazardCode);
	//
	// this.createRecords(score, hazardClassification, hazardCode, route, strScore,
	// note,
	// htHazardStatement);
	//
	// count++;
	//
	// } else if (hazardClassification.indexOf("Water") > -1
	// || hazardClassification.indexOf("STOT SE") > -1 // TODO
	// || hazardClassification.indexOf("Resp. Sens.") > -1 // TODO
	// || hazardClassification.indexOf("Asp. Tox.") > -1 // TODO
	// || hazardClassification.indexOf("Lact.") > -1 // TODO
	// || hazardClassification.indexOf("Flam.") > -1
	// || hazardClassification.indexOf("Press.") > -1
	// || hazardClassification.indexOf("Self-heat") > -1
	// || hazardClassification.indexOf("Pyr.") > -1 ||
	// hazardClassification.indexOf("Ox.") > -1
	// || hazardClassification.indexOf("Self-react.") > -1
	// || hazardClassification.indexOf("Expl") > -1
	// || hazardClassification.indexOf("Met. Corr.") > -1
	// || hazardClassification.indexOf("Ozone") > -1
	// || hazardClassification.indexOf("Org. Perox.") > -1) {
	//
	// } else {
	// // System.out.println(chemical.CAS+"\t"+hazardClassification);
	// }
	// }
	// }
	//
	// this.writeChemical(destFolderPath, chemical);
	//
	// // if (chemical.CAS.equals("71-43-2")) {
	// // System.out.println(chemical.toJSONString());
	// // }
	//
	// }
	//
	// br.close();
	//
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	//
	// }

	

	protected Chemicals goThroughOriginalRecords() {
		
		Chemicals chemicals=new Chemicals();
		try {
			String jsonFilePath = mainFolder + File.separator + fileNameJSON_Records;
			ECHACLPRecord[] records = gson.fromJson(new FileReader(jsonFilePath), ECHACLPRecord[].class);

			for (int i = 0; i < records.length; i++) {
				ECHACLPRecord ECHACLPRecord = records[i];
//				if (!records[i].CAS.equals("71-43-2")) continue;
				Chemical chemical = createChemical(ECHACLPRecord);
				handleMultipleCAS(chemicals, chemical);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	

	private String convertCASList(Chemical chemical) {
		String[] numbers = chemical.CAS.split("\n");
		String CAS2 = "";

		for (int j = 0; j < numbers.length; j++) {
			if (numbers[j].indexOf(" [") > -1) {
				CAS2 += numbers[j].substring(0, numbers[j].indexOf(" ["));
			} else {
				CAS2 += numbers[j];
			}
			if (j < numbers.length - 1)
				CAS2 += "; ";
		}
		CAS2=CAS2.replace(";;", ";");
		
		return CAS2;
	}

	private Chemical createChemical(ECHACLPRecord ed) {

		try {
			Chemical chemical = new Chemical();
			chemical.indexNumber = ed.Index_Number.trim();
			chemical.CAS = ed.CAS.trim();
			chemical.EC_number = ed.EC_Number.trim();
			chemical.name = ed.Chemical_Name.trim();
			
			Vector<String> hazardCodes = createVectorFromDelimitedString(ed.Hazard_Code,"\n");
			Vector<String> hazardClasses = createVectorFromDelimitedString(ed.Hazard_Classification,"\n");

			translate(chemical, hazardCodes, hazardClasses);

			// if (chemical.CAS.equals("71-43-2")) {
			// System.out.println(chemical.toJSONString());
			// }

			return chemical;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}

	

	private void convertExcelToText() {

		try {

			String folder = "AA Dashboard\\Data\\ECHA clp data";
			// String name="annex_vi_clp_table_atp09_Sept2016";
			String name = "annex_vi_clp_table_atp10_July2017";

			String excelFilePath = folder + "/" + name + ".xlsx";

			File file = new File(excelFilePath);

			System.out.println(file.exists());

			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
			FileWriter fw = new FileWriter(folder + "/" + name + ".txt");

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet firstSheet = workbook.getSheetAt(0);
			Iterator<Row> iterator = firstSheet.iterator();

			int row = 1;

			while (iterator.hasNext()) {
				Row nextRow = iterator.next();
				row++;

				Iterator<Cell> cellIterator = nextRow.cellIterator();

				short bob = nextRow.getLastCellNum();

				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();

					// getCellTypeEnum shown as deprecated for version 3.15
					// getCellTypeEnum will be renamed to getCellType starting from version 4.0
					cell.getCellTypeEnum();

					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_STRING:
						// System.out.print(cell.getStringCellValue());

						// String val=cell.getStringCellValue().trim();
						String val = cell.getStringCellValue();

						val = val.replace("\n", "|");

						fw.write(val);
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						// System.out.print(cell.getBooleanCellValue());
						fw.write(cell.getBooleanCellValue() + "");
						break;
					case Cell.CELL_TYPE_NUMERIC:
						fw.write(cell.getNumericCellValue() + "");
						break;
					}

					if (cellIterator.hasNext()) {
						fw.write("\t");
					}

					// System.out.print(" - ");
				}

				fw.write("\r\n");
				// System.out.println();
			}

			// workbook.close();
			inputStream.close();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private Vector<ECHACLPRecord> parseExcelFile(String excelFilepath) {

		try {

			FileInputStream inputStream = new FileInputStream(new File(excelFilepath));
			DataFormatter formatter = new DataFormatter();
			Vector<ECHACLPRecord> ECHACLP_Records = new Vector<>();

			XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
			Sheet firstSheet = workbook.getSheetAt(0);

			int row = 6;

			while (true) {
				Row currentRow = firstSheet.getRow(row);

				if (currentRow == null) {
					break;
				}

				ECHACLPRecord er = new ECHACLPRecord();

				er.Index_Number = formatter.formatCellValue(currentRow.getCell(0));
				er.Chemical_Name = formatter.formatCellValue(currentRow.getCell(1));
				er.EC_Number = formatter.formatCellValue(currentRow.getCell(2));
				er.CAS = formatter.formatCellValue(currentRow.getCell(3));
				er.Hazard_Classification = formatter.formatCellValue(currentRow.getCell(4));
				er.Hazard_Code = formatter.formatCellValue(currentRow.getCell(5));
				er.Labelling_Pictogram = formatter.formatCellValue(currentRow.getCell(6));
				er.Labelling_Hazard_Code = formatter.formatCellValue(currentRow.getCell(7));
				er.Labelling_Suppl_Hazard_Code = formatter.formatCellValue(currentRow.getCell(8));
				er.M_Factors = formatter.formatCellValue(currentRow.getCell(9));
				er.Notes = formatter.formatCellValue(currentRow.getCell(10));
				er.API_Inserted_ATP_Updated = formatter.formatCellValue(currentRow.getCell(11));

				ECHACLP_Records.add(er);

				row++;
			}

			workbook.close();
			inputStream.close();
			return ECHACLP_Records;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected void createRecords() {
		Vector<ECHACLPRecord> records = parseExcelFile(mainFolder + "/" + this.fileNameSourceExcel);
		writeOriginalRecordsToFile(records);
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParseECHACLP pp = new ParseECHACLP();
		// pp.convertExcelToText();

		pp.createFiles();
	}

}
