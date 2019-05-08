package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.ghs_data_gathering.Parse.ParseNewZealand.NewZealandRecord;
import gov.epa.ghs_data_gathering.API.AADashboard;
import gov.epa.ghs_data_gathering.API.Chemical;
import gov.epa.ghs_data_gathering.API.Chemicals;
import gov.epa.ghs_data_gathering.API.Score;
import gov.epa.ghs_data_gathering.API.ScoreRecord;

public class ParseMalaysia extends Parse {

	static class MalaysiaRecord {
		String Number;
		String Chemical_Name;
		String CAS;
		String Classification_Code;
		String Hazard_Code;
		String Labelling_Hazard_Code;
	}

	Hashtable<String, String> dictCode = CodeDictionary.populateCodeToScoreValue();
	Hashtable<String, String> dictScore = new Hashtable<String, String>();

	public ParseMalaysia() {
		sourceName = ScoreRecord.sourceMalaysia;
		fileNameSourceExcel = "Malaysia.xlsx";
		init();
//		this.populateScoreDictionary();
	}

//	private void populateScoreDictionary() {
//
//		// dictScore.put("Acute Tox. 3 (oral)", Chemical.strAcute_Mammalian_Toxicity);
//		// dictScore.put("Acute Tox. 4 (oral)", Chemical.strAcute_Mammalian_Toxicity);
//		//
//		// dictScore.put("Acute Tox. 1 (dermal)", Chemical.strAcute_Mammalian_Toxicity);
//		// dictScore.put("Acute Tox. 3 (dermal)", Chemical.strAcute_Mammalian_Toxicity);
//		// dictScore.put("Acute Tox. 4 (dermal)", Chemical.strAcute_Mammalian_Toxicity);
//		//
//		// dictScore.put("Acute Tox. 2 (inh)", Chemical.strAcute_Mammalian_Toxicity);
//		// dictScore.put("Acute Tox. 3 (inh)", Chemical.strAcute_Mammalian_Toxicity);
//		// dictScore.put("Acute Tox. 4 (inh)", Chemical.strAcute_Mammalian_Toxicity);
//
//		dictScore.put("Acute Tox. 2 (oral)", Chemical.strAcute_Mammalian_ToxicityOral);
//		dictScore.put("Acute Tox. 3 (oral)", Chemical.strAcute_Mammalian_ToxicityOral);
//		dictScore.put("Acute Tox. 4 (oral)", Chemical.strAcute_Mammalian_ToxicityOral);
//
//		dictScore.put("Acute Tox. 1 (dermal)", Chemical.strAcute_Mammalian_ToxicityDermal);
//		dictScore.put("Acute Tox. 3 (dermal)", Chemical.strAcute_Mammalian_ToxicityDermal);
//		dictScore.put("Acute Tox. 4 (dermal)", Chemical.strAcute_Mammalian_ToxicityDermal);
//
//		dictScore.put("Acute Tox. 2 (inh)", Chemical.strAcute_Mammalian_ToxicityInhalation);
//		dictScore.put("Acute Tox. 3 (inh)", Chemical.strAcute_Mammalian_ToxicityInhalation);
//		dictScore.put("Acute Tox. 4 (inh)", Chemical.strAcute_Mammalian_ToxicityInhalation);
//
//		dictScore.put("Aquatic Acute 1", Chemical.strAcute_Aquatic_Toxicity);
//
//		dictScore.put("Aquatic Chronic 1", Chemical.strChronic_Aquatic_Toxicity);
//		dictScore.put("Aquatic Chronic 2", Chemical.strChronic_Aquatic_Toxicity);
//		dictScore.put("Aquatic Chronic 3", Chemical.strChronic_Aquatic_Toxicity);
//		dictScore.put("Aquatic Chronic 4", Chemical.strChronic_Aquatic_Toxicity);
//
//		dictScore.put("Carc. 1A", Chemical.strCarcinogenicity);
//		dictScore.put("Carc. 1B", Chemical.strCarcinogenicity);
//		dictScore.put("Carc. 2", Chemical.strCarcinogenicity);
//
//		dictScore.put("Eye Dam.", Chemical.strEye_Irritation);
//		dictScore.put("Eye Dam. 1", Chemical.strEye_Irritation);
//		               
//		dictScore.put("Eye Irrit. 2", Chemical.strEye_Irritation);
//
//		dictScore.put("Muta. 1B", Chemical.strGenotoxicity_Mutagenicity);
//		dictScore.put("Muta. 2", Chemical.strGenotoxicity_Mutagenicity);
//
//		dictScore.put("Skin Corr. 1A", Chemical.strSkin_Irritation);
//		dictScore.put("Skin Corr. 1B", Chemical.strSkin_Irritation);
//		dictScore.put("Skin Irrit. 2", Chemical.strSkin_Irritation);
//
//		dictScore.put("Skin Sens. 1", Chemical.strSkin_Sensitization);
//
//		dictScore.put("STOT RE 1", Chemical.strSystemic_Toxicity_Repeat_Exposure);
//		dictScore.put("STOT RE 2", Chemical.strSystemic_Toxicity_Repeat_Exposure);
//
//		dictScore.put("STOT SE 1", Chemical.strSystemic_Toxicity_Single_Exposure);
//		dictScore.put("STOT SE 2", Chemical.strSystemic_Toxicity_Single_Exposure);
//		dictScore.put("STOT SE 3", Chemical.strSystemic_Toxicity_Single_Exposure);
//
//		/**
//		 * Resp. Sens. 1 //TODO respiratory sensitization Lact. //error?
//		 * 
//		 * //omit rest: Asp. Haz. Expl. 1.1 Org. Perox. B Ox. Gas 1 Ox. Liq. 3 Ozone
//		 * Press. Gas Press. Gas (c) Press. Gas(c) Pyr. Sol. 1 Flam. Gas 1 Flam. Gas 2
//		 * Flam. Gas. 1 Flam. Liq. 1 Flam. Liq. 2 Flam. Liq. 3 Flam. Sol. 1 Unst. Expl.
//		 * Water-react. 1 Water-react. 2
//		 **/
//	}

//	private static void removeRow(Sheet sheet, int rowIndex) {
//		int lastRowNum = sheet.getLastRowNum();
//		if (rowIndex >= 0 && rowIndex < lastRowNum) {
//			sheet.shiftRows(rowIndex + 1, lastRowNum, -1);
//		}
//		if (rowIndex == lastRowNum) {
//			Row removingRow = sheet.getRow(rowIndex);
//			if (removingRow != null) {
//				sheet.removeRow(removingRow);
//			}
//		}
//	}

	// public static void DeleteRow(Sheet sheet, Row row)
	// {
	// sheet.removeRow(row); // this only deletes all the cell values
	//
	// int rowIndex = row.getRowNum();
	//
	// int lastRowNum = sheet.getLastRowNum();
	//
	// if (rowIndex >= 0 && rowIndex < lastRowNum)
	// {
	// sheet.shiftRows(rowIndex + 1, lastRowNum, -1);
	// }
	// }

	// public void addMalaysiaRecords(Chemical chemical) {
	//
	// String CAS=chemical.CAS;
	// File jsonFile=new File(jsonFolder+"/"+CAS+".json");
	//
	// //TODO load from jar file instead later
	//
	// if (!jsonFile.exists()) {
	//// System.out.println(CAS+" does not exist in Korea records");
	// return;
	// }
	// Chemical chemicalMalaysia=Chemical.loadFromJSON(jsonFile);
	// chemical.combineRecords(chemical, chemicalMalaysia);
	//
	// }

//	private void fixDermal(String excelFilePath, String outputPath) {
//
//		try {
//
//			File file = new File(excelFilePath);
//			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
//
//			// FileWriter fw=new FileWriter(folder+"/"+name+".txt");
//
//			Workbook workbook = new XSSFWorkbook(inputStream);
//			Sheet sheet = workbook.getSheetAt(0);
//			Iterator<Row> rowIterator = sheet.iterator();
//
//			Row headerRow = rowIterator.next();// discard for now
//			headerRow = rowIterator.next();
//
//			while (rowIterator.hasNext()) {
//				Row row = rowIterator.next();
//
//				Cell cell = row.getCell(3);
//
//				if (cell != null) {
//
//					String code = cell.getStringCellValue();
//
//					if (code.equals("(dermal)")) {
//
//						int rowNum = row.getRowNum() - 1;
//
//						// get value of cell above it:
//						String val = sheet.getRow(rowNum).getCell(3).getStringCellValue();
//						sheet.getRow(rowNum).getCell(3).setCellValue(val + " " + code);
//
//						rowNum = row.getRowNum() + 1;
//
//						while (true) {
//							Row row2 = sheet.getRow(rowNum);
//
//							if (row2.getCell(2) != null) {
//								this.removeRow(sheet, rowNum - 1);
//								rowIterator = sheet.iterator();
//								break;
//							}
//
//							if (row2.getCell(3) != null) {
//								Row row3 = sheet.getRow(rowNum - 1);
//								row3.getCell(3).setCellValue(row2.getCell(3).getStringCellValue());
//							}
//
//							rowNum++;
//
//						}
//
//						// System.out.println(firstSheet.getRow(rowNum).getCell(3).getStringCellValue());
//					}
//
//				}
//
//			}
//
//			FileOutputStream fOut = new FileOutputStream(outputPath);
//			// Write the XL sheet
//			workbook.write(fOut);
//
//			fOut.flush();
//			// Done Deal..
//			fOut.close();
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}

//	private void parseExcelFile(String excelFilePath, String destFolder) {
//
//		try {
//
//			Hashtable<String, String> htHazardStatement = CodeDictionary.getHazardStatementDictionaryH();
//
//			File file = new File(excelFilePath);
//
//			File DestFolder = new File(destFolder);
//			if (!DestFolder.exists())
//				DestFolder.mkdir();
//
//			// System.out.println(file.exists());
//
//			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
//
//			// FileWriter fw=new FileWriter(folder+"/"+name+".txt");
//
//			Workbook workbook = new XSSFWorkbook(inputStream);
//			Sheet firstSheet = workbook.getSheetAt(0);
//			Iterator<Row> rowIterator = firstSheet.iterator();
//
//			Chemical chemical = null;
//
//			int rowNum = 1;
//
//			Row headerRow = rowIterator.next();// discard for now
//			headerRow = rowIterator.next();
//
//			Vector<String> uniqueHazardClasses = new Vector<String>();
//
//			int count = 0;
//
//			while (rowIterator.hasNext()) {
//				Row row = rowIterator.next();
//				rowNum++;
//
//				if (rowNum % 100 == 0)
//					System.out.println(rowNum);
//
//				int currentNum = 0;
//
//				if (row.getCell(0) != null) {
//					currentNum = (int) row.getCell(0).getNumericCellValue();
//				}
//
//				if (currentNum != 0) {
//					if (chemical != null) {
//						chemical.writeToFile(destFolder);// write out previous chemical
//					}
//
//					chemical = new Chemical();// create new chemical since number changed
//					chemical.name = row.getCell(1).getStringCellValue();
//					chemical.CAS = row.getCell(2).getStringCellValue();// for now assume all are strings with dashes
//
//				} else {
//					if (row.getCell(1) != null)
//						chemical.name += " " + row.getCell(1).getStringCellValue();
//				}
//
//				if (row.getCell(3) == null)
//					continue;
//
//				String hazardClassification = row.getCell(3).getStringCellValue();
//				String toxCode = row.getCell(4).getStringCellValue();
//
//				if (toxCode.indexOf("(") > -1)
//					toxCode = toxCode.substring(0, toxCode.indexOf("("));// trim off note
//
//				toxCode = toxCode.trim();
//
//				if (!uniqueHazardClasses.contains(hazardClassification)) {
//					uniqueHazardClasses.add(hazardClassification);
//				}
//
//				if (hazardClassification.indexOf("Repr.") > -1) {
//					handleReproDevTox(chemical, hazardClassification, toxCode, htHazardStatement);
//				}
//
//				if (this.dictScore.get(hazardClassification) != null) {
//					Score score = chemical.getScore(this.dictScore.get(hazardClassification));
//
//					String route = "";
//
//					if (hazardClassification.indexOf("(") > -1) {
//						route = hazardClassification.substring(hazardClassification.indexOf("(") + 1,
//								hazardClassification.indexOf(")"));
//						route = route.replace("inh", "inhalation");
//					}
//
//					String strScore = dictCode.get(toxCode);
//
//					if (toxCode.equals("H335, H336"))
//						strScore = dictCode.get("H335");
//
//					this.createRecord(score, hazardClassification, toxCode, route, strScore, htHazardStatement);
//
//					count++;
//
//					if (strScore == null)
//						System.out.println(count + "\t" + chemical.CAS + "\t" + hazardClassification + "\t" + toxCode
//								+ "\t" + dictCode.get(toxCode));
//				} else {
//					// System.out.println(chemical.CAS+"\t"+hazardClassification);
//				}
//
//				// write out last chemical if have no more records:
//				if (!rowIterator.hasNext()) {
//					chemical.writeToFile(destFolder);
//				}
//
//			}
//
//			// Collections.sort(uniqueHazardClasses);
//			// for (int i=0;i<uniqueHazardClasses.size();i++) {
//			// System.out.println(uniqueHazardClasses.get(i));
//			// }
//
//			// workbook.close();
//			inputStream.close();
//			// fw.close();
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//	}

	private Vector<MalaysiaRecord> parseExcelFile(String excelFilePath) {

		try {

			Vector<MalaysiaRecord> Malaysia_Records = new Vector<MalaysiaRecord>();

			File file = new File(excelFilePath);
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			DataFormatter formatter = new DataFormatter();

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet firstSheet = workbook.getSheetAt(0);

			int row = 2;

			while (true) {
				Row currentRow = firstSheet.getRow(row);

				if (row == 1128) {
					break;
				}

				MalaysiaRecord mr = new MalaysiaRecord();

				if (formatter.formatCellValue(currentRow.getCell(0)) != "") {
					mr.Chemical_Name = formatter.formatCellValue(currentRow.getCell(1)) + "\n";
					mr.CAS = formatter.formatCellValue(currentRow.getCell(2)) + "\n";
					mr.Classification_Code = formatter.formatCellValue(currentRow.getCell(3)) + "\n";
					mr.Hazard_Code = formatter.formatCellValue(currentRow.getCell(4)) + "\n";
					mr.Labelling_Hazard_Code = formatter.formatCellValue(currentRow.getCell(5)) + "\n";

					currentRow = firstSheet.getRow(++row);

					while (formatter.formatCellValue(currentRow.getCell(0)) == "") {

						if (formatter.formatCellValue(currentRow.getCell(1)) != "") {
							mr.Chemical_Name += formatter.formatCellValue(currentRow.getCell(1)) + "\n";
						}

						if (formatter.formatCellValue(currentRow.getCell(2)) != "") {
							mr.CAS += formatter.formatCellValue(currentRow.getCell(2)) + "\n";
						}

						if (formatter.formatCellValue(currentRow.getCell(3)) != "") {
							mr.Classification_Code += formatter.formatCellValue(currentRow.getCell(3)) + "\n";
						}

						if (formatter.formatCellValue(currentRow.getCell(4)) != "") {
							mr.Hazard_Code += formatter.formatCellValue(currentRow.getCell(4)) + "\n";
						}

						if (formatter.formatCellValue(currentRow.getCell(5)) != "") {
							mr.Labelling_Hazard_Code += formatter.formatCellValue(currentRow.getCell(5)) + "\n";
						}

						if (row < 1128) {
							currentRow = firstSheet.getRow(++row);
						} else
							break;

					}

				}

				
				mr.Classification_Code=mr.Classification_Code.replace("Eye. Dam. 1","Eye Dam. 1");

				Malaysia_Records.add(mr);
			}

			inputStream.close();
			workbook.close();
			return Malaysia_Records;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	
	@Override
	protected void createRecords() {
		Vector<MalaysiaRecord> records = parseExcelFile(mainFolder + "/" + fileNameSourceExcel);
		writeOriginalRecordsToFile(records);
	}
	
//	private void handleReproDevTox(Chemical chemical, String hazardClassification, String toxCode,
//			Hashtable<String, String> htHazardStatement) {
//		// TODO assign score
//		/*
//		 * Repr. 1A H360D Repr. 1B H360D Repr. 1B H360Df Repr. 1B H360F Repr. 1B H360FD
//		 * Repr. 2 H361d Repr. 2 H361f Repr. 2 H361fd
//		 */
//
//		if (toxCode.indexOf("D") > -1) {
//			String strScore = ScoreRecord.scoreH;
//			Score score = chemical.scoreDevelopmental;
//			this.createRecord(score, hazardClassification, toxCode, "", strScore, htHazardStatement);
//		}
//
//		if (toxCode.indexOf("d") > -1) {
//			String strScore = ScoreRecord.scoreM;
//			Score score = chemical.scoreDevelopmental;
//			this.createRecord(score, hazardClassification, toxCode, "", strScore, htHazardStatement);
//		}
//
//		if (toxCode.indexOf("F") > -1) {
//			String strScore = ScoreRecord.scoreH;
//			Score score = chemical.scoreReproductive;
//			this.createRecord(score, hazardClassification, toxCode, "", strScore, htHazardStatement);
//		}
//
//		if (toxCode.indexOf("f") > -1) {
//			String strScore = ScoreRecord.scoreM;
//			Score score = chemical.scoreReproductive;
//			this.createRecord(score, hazardClassification, toxCode, "", strScore, htHazardStatement);
//		}
//
//		if (toxCode.equals("H360")) {
//			System.out.println(chemical.CAS + "\t" + toxCode);
//		}
//
//		if (toxCode.equals("H361")) {
//			System.out.println(chemical.CAS + "\t" + toxCode);
//		}
//		
//		if (toxCode.equals("H362")) {
////			System.out.println(chemical.CAS + "\t" + toxCode);
//
//			String strScore = ScoreRecord.scoreH;
//			Score score = chemical.scoreDevelopmental;
//			this.createRecord(score, hazardClassification, toxCode, "", strScore, htHazardStatement);
//
//		}
//
//
//	}

	
//	private void createRecord(Score score, String hazardClassification, String toxCode, String toxRoute,
//			String strScore, Hashtable<String, String> htHazardStatement) {
//		ScoreRecord sr = new ScoreRecord();
//		score.records.add(sr);
//
//		sr.source = ScoreRecord.sourceMalaysia;
//		sr.category = hazardClassification;// TODO or assign to classification?
//		sr.hazard_code = toxCode;
//		sr.route = toxRoute;
//
//		if (htHazardStatement.get(toxCode) != null) {
//			sr.hazard_statement = htHazardStatement.get(toxCode);
//		} else if (toxCode.equals("H335, H336")) {
//			sr.hazard_statement = htHazardStatement.get("H335");
//			sr.hazard_statement += "; " + htHazardStatement.get("H336");
//			// System.out.println(sr.hazard_statement);
//		} else {
//			System.out.println("need statement for " + toxCode);
//		}
//
//		// Assign score based on toxCode:
//		sr.score = strScore;
//
//		sr.rationale = "Score of " + strScore + " was assigned based on a hazard code of " + toxCode;
//	}

	private Chemical createChemical(MalaysiaRecord malaysiaRecord) {
		
		Chemical chemical = new Chemical();
		chemical.name = malaysiaRecord.Chemical_Name.replace("\n", " ").trim();
		chemical.CAS = malaysiaRecord.CAS.trim();

		Vector<String> hazardCodes = createVectorFromDelimitedString(malaysiaRecord.Hazard_Code,"\n");
		Vector<String> hazardClasses = createVectorFromDelimitedString(malaysiaRecord.Classification_Code,"\n");
		this.translate(chemical, hazardCodes, hazardClasses);
//		chemical.writeToFile(jsonFolder);
		return chemical;

		// workbook.close();
		// inputStream.close();
		// fw.close();
	}
	
	
//	private Chemical createChemicalOld(MalaysiaRecord malaysiaRecord) {
//		
//		Chemical chemical = new Chemical();
//		ArrayList<String> uniqueHazardClasses = new ArrayList<String>();
//		Hashtable<String, String> htHazardStatement = CodeDictionary.getHazardStatementDictionaryH();
//
//		chemical.name = malaysiaRecord.Chemical_Name.replace("\n", " ").trim();
//		chemical.CAS = malaysiaRecord.CAS.trim();
//
//		String[] hazardClassification = malaysiaRecord.Classification_Code.split("\n");
//		String[] toxCode = malaysiaRecord.Hazard_Code.split("\n");
//
//		for (int i = 0; i < hazardClassification.length; i++) {
//
//			if (toxCode[i].indexOf("(") > -1)
//				toxCode[i] = toxCode[i].substring(0, toxCode[i].indexOf("("));// trim off note
//
//			toxCode[i] = toxCode[i].trim();
//
//			if (!uniqueHazardClasses.contains(hazardClassification[i])) {
//				uniqueHazardClasses.add(hazardClassification[i]);
//			}
//
//			if (hazardClassification[i].indexOf("Repr.") > -1 || hazardClassification[i].indexOf("Lact.") > -1) {
//				handleReproDevTox(chemical, hazardClassification[i], toxCode[i], htHazardStatement);
//				continue;
//			}
//
//			if (this.dictScore.get(hazardClassification[i]) != null) {
//				Score score = chemical.getScore(this.dictScore.get(hazardClassification[i]));
//
//				String route = "";
//
//				if (hazardClassification[i].indexOf("(") > -1) {
//					route = hazardClassification[i].substring(hazardClassification[i].indexOf("(") + 1,
//							hazardClassification[i].indexOf(")"));
//					route = route.replace("inh", "inhalation");
//				}
//
//				String strScore = dictCode.get(toxCode[i]);
//
//				if (toxCode[i].equals("H335, H336"))
//					strScore = dictCode.get("H335");
//
//				this.createRecord(score, hazardClassification[i], toxCode[i], route, strScore, htHazardStatement);
//
//				// count++;
//
//				if (strScore == null)
//					// System.out.println(count + "\t" + chemical.CAS + "\t" + hazardClassification
//					// + "\t" + toxCode + "\t"
//					// + dictCode.get(toxCode));
//					System.out.println(chemical.CAS + "\t" + hazardClassification[i] + "\t" + toxCode[i] + "\t"
//							+ dictCode.get(toxCode[i]));
//			} else  {
//				boolean skip=false;
//				
//				String [] strSkip= {"Resp. Sens.","Flam.","Press.","Expl.","Asp. Haz.","Ozone","Pyr. Sol.","Ox.","Water-react.","Org. Perox."};
//				
//				for (int j=0;j<strSkip.length;j++) {
////					System.out.println(hazardClassification[i]+"\t"+strSkip.get(j)+"\t"+hazardClassification[i].indexOf(strSkip.get(j)));
//					if (hazardClassification[i].indexOf(strSkip[j])>-1) {
//						skip=true;
//						break;
//					}
//				}
//				if (!skip) System.out.println("***"+chemical.CAS + "\t" + hazardClassification[i]);
//			
//			}
//			// write out last chemical if have no more records:
//			// if (!rowIterator.hasNext())
//			// writeChemical(destFolder, chemical);
//			
//		}//end loop over hazard classifications
//
//		
//		if (chemical.CAS.equals("-") || chemical.CAS.equals("NA")) {
//			chemical.CAS = "NO_CAS_" + chemical.EC_number.trim();
//		} else {
//			chemical.CAS = chemical.CAS.trim();
//		}
//
////		chemical.writeToFile(jsonFolder);
//
//		return chemical;
//
//		// workbook.close();
//		// inputStream.close();
//		// fw.close();
//	}

	
	@Override
	protected Chemicals goThroughOriginalRecords() {
		Chemicals chemicals=new Chemicals();
		try {

			String jsonFilePath = mainFolder + File.separator + fileNameJSON_Records;

			Gson gson = new Gson();
			MalaysiaRecord[] records = gson.fromJson(new FileReader(jsonFilePath), MalaysiaRecord[].class);

			for (int i = 0; i < records.length; i++) {
				MalaysiaRecord malaysiaRecord = records[i];
				Chemical chemical=createChemical(malaysiaRecord);
				handleMultipleCAS(chemicals, chemical);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	public static void main(String[] args) {

		ParseMalaysia pm = new ParseMalaysia();

		pm.createFiles();

	}
}