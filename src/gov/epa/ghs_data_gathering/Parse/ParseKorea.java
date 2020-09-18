package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

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
import gov.epa.ghs_data_gathering.Parse.ParseMalaysia.MalaysiaRecord;

/**
 * 
 * We got data from a spreadsheet which is no longer available on the Korea website
 * and they locked down the search engine
 * 
 * http://ncis.nier.go.kr/en/main.do
 * 
 * 
 * 
 * @author Todd Martin
 *
 */
public class ParseKorea extends Parse {


	Hashtable<String, String> dictCode = CodeDictionary.populateCodeToScoreValue();
	Hashtable<String, String> dictScore = new Hashtable<String, String>();

	public ParseKorea() {
		sourceName = ScoreRecord.sourceKorea;
		fileNameSourceExcel = "korea data unmerged.xlsx";
		init();

		this.populateScoreDictionary();
	}

	class KoreaRecord {

		String Chemical_Name;
		String EC_Number;
		String CAS;
		String Hazard_Classification;
		String Pictograms;
		String Signal_Words;
		String Hazard_Code;
		String M_Value;
		String UN_Number;
		String Date_of_Designation;

	}

	private void populateScoreDictionary() {

		// dictScore.put("Acute Oral Toxicity", Chemical.strAcute_Mammalian_Toxicity);
		// dictScore.put("Acute dermal toxicity", Chemical.strAcute_Mammalian_Toxicity);
		// dictScore.put("Acute Inhalation Toxicity",
		// Chemical.strAcute_Mammalian_Toxicity);

		dictScore.put("Acute Oral Toxicity", Chemical.strAcute_Mammalian_ToxicityOral);
		dictScore.put("Acute dermal toxicity", Chemical.strAcute_Mammalian_ToxicityDermal);
		dictScore.put("Acute Inhalation Toxicity", Chemical.strAcute_Mammalian_ToxicityInhalation);

		dictScore.put("Acute Toxicity to aquatic invertebrates", Chemical.strAcute_Aquatic_Toxicity);
		dictScore.put("Chronic Toxicity to aquatic invertebrates", Chemical.strChronic_Aquatic_Toxicity);

		dictScore.put("Skin sensitization", Chemical.strSkin_Sensitization);
		dictScore.put("Skin corrosion/irritation", Chemical.strSkin_Irritation);
		dictScore.put("Serous eye damage/eye irritation", Chemical.strEye_Irritation);
		dictScore.put("Carcinogenicity", Chemical.strCarcinogenicity);
		dictScore.put("Specific target organ systemic toxicity - Repeated exposure",
				Chemical.strSystemic_Toxicity_Repeat_Exposure);
		dictScore.put("SPECIFIC TARGET ORGAN TOXICITY - SINGLE EXPOSURE (STOT-SE)",
				Chemical.strSystemic_Toxicity_Single_Exposure);

		dictScore.put("Reproductive toxicity", Chemical.strReproductive);

		dictScore.put("genetic toxicity(in vitro, in vivo)", Chemical.strGenotoxicity_Mutagenicity);

		// TODO: Respiratory sensitization

		// Extra fields:
		// Aspiration hazard
		// Substances and mixtures which, in contact with water, emit flammable gases
		// Flammable liquids
		// Flammable solids
		// Oxidizing solids
		// Oxidizing liquids
		// Gases under pressure
		// Ozone layer Hazards
		// Flammable gases
		// Explosives
		// Corrosive to metals
		// Organic peroxides
		// Oxidizing gases
		// Pyrophoric liquids
		// Pyrophoric solids

	}

	private void parseExcelFile(String excelFilePath, String destFolder) {

		try {

			Hashtable<String, String> htHazardStatement = CodeDictionary.getHazardStatementDictionaryH();

			File file = new File(excelFilePath);

			File DestFolder = new File(destFolder);
			if (!DestFolder.exists())
				DestFolder.mkdir();

			// System.out.println(file.exists());

			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			// FileWriter fw=new FileWriter(folder+"/"+name+".txt");

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet firstSheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = firstSheet.iterator();

			Chemical chemical = null;

			int rowNum = 1;

			Row headerRow = rowIterator.next();// discard for now
			headerRow = rowIterator.next();
			headerRow = rowIterator.next();

			Vector<String> uniqueHazardClasses = new Vector<String>();

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				rowNum++;

				if (rowNum % 100 == 0)
					System.out.println(rowNum);

				int currentNum = (int) row.getCell(0).getNumericCellValue();

				if (currentNum != 0) {
					if (chemical != null) {
						if (chemical.CAS.equals("-") || chemical.CAS.equals("NA")) {
							chemical.CAS = "NO_CAS_" + chemical.EC_number.trim();
						} else {
							chemical.CAS = chemical.CAS.trim();
						}

						chemical.writeToFile(jsonFolder);

					}
					chemical = new Chemical();// create new chemical since number changed
					chemical.name = row.getCell(1).getStringCellValue();
					chemical.EC_number = row.getCell(2).getStringCellValue();// for now assume all are strings with
																				// dashes
					chemical.CAS = row.getCell(3).getStringCellValue();// for now assume all are strings with dashes

					// TODO convert dates to CAS string if Excel converted them to dates:
					// if (row.getCell(3).getCellType()==Cell.CELL_TYPE_NUMERIC) {
					// SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-D");//this doesnt work
					// //CAS number got converted to date:
					// System.out.println(sdf.format(row.getCell(0).getNumericCellValue()));
					//
					// } else {
					// chemical.CAS=row.getCell(3).getStringCellValue();
					// }

				}

				String hazardClassification = row.getCell(5).getStringCellValue();
				String toxCode = row.getCell(9).getStringCellValue();

				if (!uniqueHazardClasses.contains(hazardClassification)) {
					uniqueHazardClasses.add(hazardClassification);
				}

				// System.out.println(hazardClassification+"\t"+dictScore.get(hazardClassification));

				if (this.dictScore.get(hazardClassification) != null) {

					String hazardName = this.dictScore.get(hazardClassification);

					Score score = chemical.getScore(hazardName);
					String strScore = dictCode.get(toxCode);

					String route = "";

					if (hazardClassification.toLowerCase().indexOf("oral") > -1) {
						route = "oral";
					} else if (hazardClassification.toLowerCase().indexOf("dermal") > -1) {
						route = "dermal";
					} else if (hazardClassification.toLowerCase().indexOf("inhalation") > -1) {
						route = "inhalation";
					} else if (hazardClassification.toLowerCase().indexOf("invertebrates") > -1) {
						route = "invertebrates";
					}

					if (strScore == null) {
						System.out.println(chemical.CAS + "\t" + hazardClassification + "\t" + toxCode + "\t"
								+ dictCode.get(toxCode));
					} else {

						this.createRecord(score, chemical,toxCode, route, strScore, htHazardStatement);

						// For korea, they dont distinguish between repro and dev tox, so also add to
						// dev tox too:
						if (hazardName.equals(Chemical.strReproductive)) {
							// System.out.println(chemical.CAS+"\t"+toxCode);
							score = chemical.getScore(Chemical.strDevelopmental);
							this.createRecord(score, chemical,toxCode, route, strScore, htHazardStatement);
						}

					}

				} else {

					// if (hazardClassification.toLowerCase().indexOf("flammable")>-1) continue;
					// if (hazardClassification.toLowerCase().indexOf("oxidiz")>-1) continue;
					// if (hazardClassification.toLowerCase().indexOf("gases under pressure")>-1)
					// continue;
					// if (hazardClassification.toLowerCase().indexOf("explosive")>-1) continue;
					// if (hazardClassification.toLowerCase().indexOf("corrosive")>-1) continue;
					// if (hazardClassification.toLowerCase().indexOf("pyrophoric")>-1) continue;
					// if (hazardClassification.toLowerCase().indexOf("ozone")>-1) continue;
					// if (hazardClassification.toLowerCase().indexOf("organic")>-1) continue;
					// if (hazardClassification.toLowerCase().indexOf("aspiration")>-1) continue;
					//
					// System.out.println(chemical.CAS+"\t"+hazardClassification);
				}

				// write out last chemical if have no more records:
				if (!rowIterator.hasNext()) {
					if (chemical.CAS.equals("-") || chemical.CAS.equals("NA")) {
						chemical.CAS = "NO_CAS_" + chemical.EC_number.trim();
					} else {
						chemical.CAS = chemical.CAS.trim();
					}
					chemical.writeToFile(jsonFolder);
				}

			}

			// for (int i=0;i<uniqueHazardClasses.size();i++) {
			// System.out.println(uniqueHazardClasses.get(i));
			// }

			// workbook.close();
			inputStream.close();
			// fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private Vector<KoreaRecord> parseExcelFile(String excelFilePath) {

		try {

			File file = new File(excelFilePath);
			DataFormatter formatter = new DataFormatter();

			Vector<KoreaRecord> Korea_Records = new Vector<KoreaRecord>();

			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet firstSheet = workbook.getSheetAt(0);

			int row = 3;

			while (true) {

				Row currentRow = firstSheet.getRow(row);

				if (formatter.formatCellValue(currentRow.getCell(0)) == "end") {
					break;
				}

				KoreaRecord kr = new KoreaRecord();

				if (formatter.formatCellValue(currentRow.getCell(0)) != "") {
					kr.Chemical_Name = formatter.formatCellValue(currentRow.getCell(1));
					kr.EC_Number = formatter.formatCellValue(currentRow.getCell(2));
					kr.CAS = formatter.formatCellValue(currentRow.getCell(3));
					kr.Hazard_Classification = formatter.formatCellValue(currentRow.getCell(5)) + "\n";
					kr.Pictograms = formatter.formatCellValue(currentRow.getCell(7));
					kr.Signal_Words = formatter.formatCellValue(currentRow.getCell(8));
					kr.Hazard_Code = formatter.formatCellValue(currentRow.getCell(9)) + "\n";
					kr.M_Value = formatter.formatCellValue(currentRow.getCell(10));
					kr.UN_Number = formatter.formatCellValue(currentRow.getCell(11));
					kr.Date_of_Designation = formatter.formatCellValue(currentRow.getCell(12));

					currentRow = firstSheet.getRow(++row);

					if (currentRow == null) {
						break;
					}

					while (formatter.formatCellValue(currentRow.getCell(0)) == "") {

						if (formatter.formatCellValue(currentRow.getCell(5)) != "") {
							kr.Hazard_Classification += formatter.formatCellValue(currentRow.getCell(5)) + "\n";
						}

						if (formatter.formatCellValue(currentRow.getCell(9)) != "") {
							kr.Hazard_Code += formatter.formatCellValue(currentRow.getCell(9)) + "\n";
						}

						if (row < 3835) {
							currentRow = firstSheet.getRow(++row);
						} else
							break;

					}

				}

				Korea_Records.add(kr);
			}

			inputStream.close();
			workbook.close();
			return Korea_Records;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	@Override
	protected void createRecords() {
		Vector<KoreaRecord> records = parseExcelFile(mainFolder + "/" + fileNameSourceExcel);
		writeOriginalRecordsToFile(records);
	}

	
	private void createRecord(Score score, Chemical chemical,String toxCode, String toxRoute, String strScore,
			Hashtable<String, String> htHazardStatement) {
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		score.records.add(sr);

		sr.source = ScoreRecord.sourceKorea;
		sr.hazard_code = toxCode;
		sr.route = toxRoute;
		sr.score = strScore;

		if (htHazardStatement.get(toxCode) != null) {
			sr.hazard_statement = htHazardStatement.get(toxCode);
		} else {
			System.out.println("need statement for " + toxCode);
		}

		// sr.hazard_statement=toxClassification;//TODO or classification?
		// sr.rationale=toxJustification;

		sr.rationale = "Score of " + strScore + " was assigned based on a hazard code of " + toxCode;

	}

	// public void addKoreaRecords(Chemical chemical) {
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
	// Chemical chemicalKorea=Chemical.loadFromJSON(jsonFile);
	// chemical.combineRecords(chemical, chemicalKorea);
	//
	// }

	Chemical createChemical(KoreaRecord koreaRecord) {
		try {

			Chemical chemical = new Chemical();

			Hashtable<String, String> htHazardStatement = CodeDictionary.getHazardStatementDictionaryH();

			Vector<String> uniqueHazardClasses = new Vector<String>();

			chemical.name = koreaRecord.Chemical_Name.trim();
			chemical.EC_number = koreaRecord.EC_Number.trim();// for now assume all are strings with
			// dashes
			chemical.CAS = koreaRecord.CAS;// for now assume all are strings with dashes
			
			if (chemical.CAS.equals("NA")) {
				chemical.CAS="";
			}

			// TODO convert dates to CAS string if Excel converted them to dates:
			// if (row.getCell(3).getCellType()==Cell.CELL_TYPE_NUMERIC) {
			// SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-D");//this doesnt work
			// //CAS number got converted to date:
			// System.out.println(sdf.format(row.getCell(0).getNumericCellValue()));
			//
			// } else {
			// chemical.CAS=row.getCell(3).getStringCellValue();
			// }

			String[] hazardClassification = koreaRecord.Hazard_Classification.split("\n");
			String[] toxCode = koreaRecord.Hazard_Code.split("\n");

			for (int i = 0; i < hazardClassification.length; i++) {

				if (!uniqueHazardClasses.contains(hazardClassification[i])) {
					uniqueHazardClasses.add(hazardClassification[i]);
				}

				// System.out.println(hazardClassification+"\t"+dictScore.get(hazardClassification));

				if (this.dictScore.get(hazardClassification[i]) != null) {

					String hazardName = this.dictScore.get(hazardClassification[i]);

					Score score = chemical.getScore(hazardName);
					String strScore = dictCode.get(toxCode[i]);

					String route = "";

					if (hazardClassification[i].toLowerCase().indexOf("oral") > -1) {
						route = "oral";
					} else if (hazardClassification[i].toLowerCase().indexOf("dermal") > -1) {
						route = "dermal";
					} else if (hazardClassification[i].toLowerCase().indexOf("inhalation") > -1) {
						route = "inhalation";
					} else if (hazardClassification[i].toLowerCase().indexOf("invertebrates") > -1) {
						route = "invertebrates";
					}

					if (strScore == null) {
						System.out.println(chemical.CAS + "\t" + hazardClassification[i] + "\t" + toxCode[i] + "\t"
								+ dictCode.get(toxCode[i]));
					} else {

						this.createRecord(score, chemical, toxCode[i], route, strScore, htHazardStatement);

						// For korea, they dont distinguish between repro and dev tox, so also add to
						// dev tox too:
						if (hazardName.equals(Chemical.strReproductive)) {
							// System.out.println(chemical.CAS+"\t"+toxCode);
							score = chemical.getScore(Chemical.strDevelopmental);
							this.createRecord(score, chemical,toxCode[i], route, strScore, htHazardStatement);
						}

					}

				} else {

					// if (hazardClassification.toLowerCase().indexOf("flammable")>-1) continue;
					// if (hazardClassification.toLowerCase().indexOf("oxidiz")>-1) continue;
					// if (hazardClassification.toLowerCase().indexOf("gases under pressure")>-1)
					// continue;
					// if (hazardClassification.toLowerCase().indexOf("explosive")>-1) continue;
					// if (hazardClassification.toLowerCase().indexOf("corrosive")>-1) continue;
					// if (hazardClassification.toLowerCase().indexOf("pyrophoric")>-1) continue;
					// if (hazardClassification.toLowerCase().indexOf("ozone")>-1) continue;
					// if (hazardClassification.toLowerCase().indexOf("organic")>-1) continue;
					// if (hazardClassification.toLowerCase().indexOf("aspiration")>-1) continue;
					//
					// System.out.println(chemical.CAS+"\t"+hazardClassification);
				}

				// write out last chemical if have no more records:

			}

//			chemical.writeToFile(jsonFolder);

			// for (int i=0;i<uniqueHazardClasses.size();i++) {
			// System.out.println(uniqueHazardClasses.get(i));
			// }

			return chemical;
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	
	@Override
	protected Chemicals goThroughOriginalRecords() {
		Chemicals chemicals=new Chemicals();
		try {

			String jsonFilePath = mainFolder + File.separator + fileNameJSON_Records;

			Gson gson = new Gson();
			KoreaRecord[] records = gson.fromJson(new FileReader(jsonFilePath), KoreaRecord[].class);

			for (int i = 0; i < records.length; i++) {
				KoreaRecord koreaRecord = records[i];
				Chemical chemical=createChemical(koreaRecord);
				if (chemical==null) continue;
				handleMultipleCAS(chemicals, chemical);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	public static void main(String[] args) {

		ParseKorea pk = new ParseKorea();

		pk.createFiles();

	}

}
