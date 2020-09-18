package gov.epa.ghs_data_gathering.Parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Utilities.FileUtilities;

/**
 *
 * http://hcis.safeworkaustralia.gov.au/HazardousChemical
 * http://hcis.safeworkaustralia.gov.au/SearchKey#Cut-offs
 * 
 * @author Todd Martin
 *
 */
public class ParseAustralia extends Parse {

	Multimap<String, String> dictCodeToCategory = CodeDictionary.populateCodeToCategory();
	Multimap<String, String> dictCodeToStatement = CodeDictionary.populateCodeToStatementMultimap();

	boolean debug = false;

	
	static class AustraliaRecord {
		String CAS_No;
		String Substance_Name;
		String GHS_Hazard_Category;
		String Pictogram_Codes_and_Signal_Word;
		String Hazard_Statement_Codes;
		String Hazard_Statements;
		String Note;
		String Source;

	}

	
	static class AustraliaRecord2 {
		String CasNumber;
		String ChemicalName;
		String ChemicalSynonym;

		ArrayList<String> HazardCategories = new ArrayList<String>();

		// H312 (Harmful in contact with skin) - H312 is the code, value in parentheses
		// is statement:
		ArrayList<String> HazardCodes = new ArrayList<String>();
		ArrayList<String> HazardStatements = new ArrayList<String>();

		ArrayList<String> PictogramCodes = new ArrayList<String>();

		String SignalWordWarning;
		String Cut_offs;

		String Notes;
		String Sources;
		String url;

		ArrayList<HistoryRecord> ChemicalHistory = new ArrayList<>();

		class HistoryRecord {
			String Date;
			String Description;
		}

	}

	public ParseAustralia() {
		sourceName = ScoreRecord.sourceAustralia;
//		fileNameSourceExcel = "Australia GHS Hazardous Chemical Information List (No Search Version).xlsx";
//		folderNameWebpages="Chemical Webpages";
		fileNameHtmlZip="Chemical Webpages.zip";
		init();
		
	}


	private void downloadWebpages(String htmlFolder) {

		String[] vowels = { "a", "e", "i", "o", "u", "y" };

		for (int i = 0; i < vowels.length; i++) {

			String baseUrl = "http://hcis.safeworkaustralia.gov.au/HazardousChemical/Search?SearchBy=Name&SearchText="
					+ vowels[i] + "&results=30&sortBy=Cas_No&isAdvancedSearch=false&page=";

			int pageNumber = 1;

			int numberOfPages = 0;

			switch (vowels[i]) {

			case "a":
				numberOfPages = 147;
				break;
			case "e":
				numberOfPages = 157;
				break;
			case "i":
				numberOfPages = 149;
				break;
			case "o":
				numberOfPages = 151;
				break;
			case "u":
				numberOfPages = 89;
				break;
			case "y":
				numberOfPages = 135;
				break;

			}

			while (true) {

				if (pageNumber > numberOfPages) {
					break;
				}

				String url = baseUrl + pageNumber;

				try {

					String destFilePath = htmlFolder + "/" + "page" + vowels[i] + pageNumber + ".html";

					pageNumber++;

					File destFile = new File(destFilePath);

					if (destFile.exists())
						continue;

					FileUtilities.downloadFile(url, destFilePath);

					Thread.sleep(3000);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

		}
	}

	private void parseWebpageLinks() {

		String folder = "L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Australia\\";
		String outputTextFile = folder + "Chemical Links.txt";
		String htmlFolder = folder + "Webpages";

		String frontOfLink = "http://hcis.safeworkaustralia.gov.au";

		ArrayList<String> hyperlinks = new ArrayList<>();

		File textFile = new File(outputTextFile);

		ArrayList<String> IDs = new ArrayList<String>();

		Document doc;

		try {

			FileWriter fw = new FileWriter(textFile);
			File fileFolder = new File(htmlFolder);
			File[] files = fileFolder.listFiles();

			for (int fileIterator = 0; fileIterator < files.length; fileIterator++) {
				File inputFile = files[fileIterator];

				if (inputFile.getName().indexOf(".html") == -1)
					continue;

				if (fileIterator % 100 == 0)
					System.out.println(fileIterator);

				doc = Jsoup.parse(inputFile, "utf-8");

				Elements links = doc.select("a[href]");

				for (int i = 0; i < links.size(); i++) {

					String stringLine = links.get(i).toString();
					String textLinks = "";

					if (stringLine.contains("chemicalID=")) {
						textLinks += links.get(i).attr("href").toString();
						hyperlinks.add(textLinks);
					}

				}

			}

			for (String s : hyperlinks) {
				String line = frontOfLink + s;
				if (!IDs.contains(line)) {
					IDs.add(line);
				}
			}

			for (String s : IDs) {
				fw.write(s + "\r\n");
			}

			fw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	// Saves webpages by their ChemicalID
	private void downloadTextLinks() {
		String folder = "L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Australia\\";
		String chemicalLinks = folder + "Chemical Links.txt";
		String webpageFolder = folder + "Chemical Webpages2";

		try {

			BufferedReader br = new BufferedReader(new FileReader(chemicalLinks));

			while (true) {

				String line = br.readLine();

				if (line == null) {
					break;
				}

				String chemicalID = line.substring(line.indexOf("ID=") + 3, line.length());

				String strURL = line;

				String destFilePath = webpageFolder + "/" + chemicalID + ".html";

				File destFile = new File(destFilePath);

				if (destFile.exists())
					continue;

				FileUtilities.downloadFile(strURL, destFilePath);

				Thread.sleep(3000);
			}

			br.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static Vector<AustraliaRecord2> parseChemicalWebpages(String htmlFolder) {

		Vector<AustraliaRecord2> Australia_Record = new Vector<>();

		Document doc;

		try {

			File fileFolder = new File(htmlFolder);
			File[] files = fileFolder.listFiles();

			for (int j = 0; j < files.length; j++) {
				// for (int j = 0; j < 10; j++) {
				File inputFile = files[j];

				if (inputFile.getName().indexOf(".html") == -1)
					continue;

				if (j % 100 == 0)
					System.out.println(j);

				doc = Jsoup.parse(inputFile, "utf-8");
				AustraliaRecord2 ar = createAustraliaRecord(doc);
				Australia_Record.add(ar);

			}
			return Australia_Record;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static Vector<AustraliaRecord2> parseChemicalWebpagesInZipFile(String zipFilePath) {

		Vector<AustraliaRecord2> Australia_Record = new Vector<>();

		try {

			ZipFile zipFile = new ZipFile(zipFilePath);
			
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			ZipEntry zipEntry0 = entries.nextElement();//entry for folder, discard
			
			int counter=0;
			
			while (entries.hasMoreElements()) {
				
				if (counter%1000==0) System.out.println(counter);
				
//				if (counter==5) break;
				
                final ZipEntry zipEntry = entries.nextElement();
//                System.out.println(entry.getName());
                
                
//                System.out.println(theString);
                
                
                Document doc = Jsoup.parse(zipFile.getInputStream(zipEntry),"utf-8","http://hcis.safeworkaustralia.gov.au/HazardousChemical/Details?chemicalID=");

				AustraliaRecord2 ar = createAustraliaRecord(doc);
				
				
				String ID=zipEntry.getName();
				ID=ID.substring(ID.indexOf("/")+1,ID.indexOf("."));
				
				ar.url="http://hcis.safeworkaustralia.gov.au/HazardousChemical/Details?chemicalID="+ID;
				
//				System.out.println(ar.CasNumber+"\t"+ar.url);

//				System.out.println(inputFile.getName() + "\t" + ar.CasNumber);

				Australia_Record.add(ar);
				
				counter++;

			}
			return Australia_Record;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static AustraliaRecord2 createAustraliaRecord(Document doc) {
		AustraliaRecord2 ar = new AustraliaRecord2();


		// Elements mainTable = doc.select("table[class]");

		Elements headers = doc.select("h2");

		for (int i = 0; i < headers.size(); i++) {
			Element header = headers.get(i);
			String line = headers.get(i).text();

			if (line.contains("Hazardous Chemical Details")) {
				Element chemicalDetails = header.nextElementSibling();

				Elements tableRows = chemicalDetails.select("tbody tr td");

				parseHazardousChemicalDetails(ar, tableRows);

			} else if (line.contains("Hazardous Chemical History")) {
				Element chemicalHistory = header.nextElementSibling();

				Elements tableRows = chemicalHistory.select("tbody tr td");

				parseChemicalHistory(ar, tableRows);
			}

		}

		// if (ar.CasNumber!=null && !ar.CasNumber.equals("")) {
		// inputFile.renameTo(new File(htmlFolder+"/"+ar.CasNumber+".html"));
		// }
		return ar;
	}

	private static void parseChemicalHistory(AustraliaRecord2 ar, Elements tableRows) {
		for (int tableIterator = 0; tableIterator < tableRows.size(); tableIterator++) {

			// String row = tableRows.get(tableIterator).text();

			AustraliaRecord2.HistoryRecord hr = ar.new HistoryRecord();

			hr.Date = tableRows.get(tableIterator).text();
			hr.Description = tableRows.get(tableIterator + 1).text();

			ar.ChemicalHistory.add(hr);
			tableIterator++;

		}
	}

	private static void parseHazardousChemicalDetails(AustraliaRecord2 ar, Elements tableRows) {
		for (int tableIterator = 0; tableIterator < tableRows.size(); tableIterator++) {

			String row = tableRows.get(tableIterator).text();
			if (row.contains("Cas Number")) {
				ar.CasNumber = (tableRows.get(tableIterator + 1).text());
			} else if (row.contains("Chemical Name")) {
				ar.ChemicalName = (tableRows.get(tableIterator + 1).text());
			} else if (row.contains("Chemical Synonym")) {
				ar.ChemicalSynonym = (tableRows.get(tableIterator + 1).text());
			} else if (row.contains("Hazard Category")) {
				String hazardCategories = tableRows.get(tableIterator + 1).toString();

				String[] hazardCategoryList = hazardCategories.split("<br>");

				for (String hazardCategory : hazardCategoryList) {
					hazardCategory = hazardCategory.replaceAll("<td>", "").replaceAll("</td>", "");
					hazardCategory = hazardCategory.trim();
					if (!hazardCategory.equals(""))
						ar.HazardCategories.add(hazardCategory);
				}

			} else if (row.contains("Hazard Statement")) {
				String hazardStatements = tableRows.get(tableIterator + 1).toString();

				String[] hazardStatementList = hazardStatements.split("<br>");

				for (String hazardStatement : hazardStatementList) {
					hazardStatement = hazardStatement.replaceAll("<td>", "").replaceAll("</td>", "");
					hazardStatement = hazardStatement.trim();

					String hazardCode = "";
					String hazardStatementText = "";

					if (hazardStatement.contains("(")) {
						hazardCode = hazardStatement.substring(0, hazardStatement.indexOf("(")).trim();
						hazardStatementText = hazardStatement
								.substring(hazardStatement.indexOf("(") + 1, hazardStatement.indexOf(")")).trim();

					} else {
						hazardCode = hazardStatement;
					}
					if (!hazardCode.equals(""))
						ar.HazardCodes.add(hazardCode);
					if (!hazardStatementText.equals(""))
						ar.HazardStatements.add(hazardStatementText);
				}
			} else if (row.contains("Pictogram")) {
				String pictogramList = (tableRows.get(tableIterator + 1).text());

				String[] separateWords = pictogramList.split("[\\(||\\)]");

				for (int iterator = 0; iterator < separateWords.length; iterator++) {
					if (separateWords[iterator].length() < 1) {
						break;
					}
					String firstWord = separateWords[iterator].trim();
					String secondWord = " (" + separateWords[iterator + 1] + ")".trim();

					ar.PictogramCodes.add(firstWord + secondWord);

					iterator++;
				}
			} else if (row.contains("Signal Word")) {
				ar.SignalWordWarning = (tableRows.get(tableIterator + 1).text());
			} else if (row.contains("Cut-offs")) {
				ar.Cut_offs = (tableRows.get(tableIterator + 1).text());
			} else if (row.contains("Notes")) {
				ar.Notes = (tableRows.get(tableIterator + 1).text());
			} else if (row.contains("Sources")) {
				//whitelist br tags
			    Whitelist whitelist = new Whitelist();
			    whitelist.addTags("br");
			    
			    //parse html while leaving whitelisted br tag
			    String sources = Jsoup.clean(tableRows.get(tableIterator + 1).toString(), whitelist);
			    
				ar.Sources = (sources); //TODO <br>
			}

		}
	}

	@Override
	protected void createRecords() {
//		Vector<AustraliaRecord2> records = parseChemicalWebpages(mainFolder + "/" + folderNameWebpages);
		Vector<AustraliaRecord2> records = parseChemicalWebpagesInZipFile(mainFolder + "/" + this.fileNameHtmlZip);
		writeOriginalRecordsToFile(records);
	}
	
	private void compareJSONFiles() {

		String originalJSON = "L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Australia\\Australia GHS Hazardous Chemical Information List (No Search Version).json";
		String newJSON = "L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Australia\\Australia Record.json";
		String comparisonTextFile = "L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Australia\\Chemicals Not Contained in Australia Record.txt";

		try {

			// loading jsons into variables for comparison
			AustraliaRecord[] australiaRecords = gson.fromJson(new FileReader(originalJSON), AustraliaRecord[].class);
			AustraliaRecord2[] australiaRecords2 = gson.fromJson(new FileReader(newJSON), AustraliaRecord2[].class);

			File file = new File(comparisonTextFile);

			FileWriter fw = new FileWriter(file);

			// loop through old record, compare name and cas
			for (int i = 0; i < australiaRecords.length; i++) {

				AustraliaRecord ar = australiaRecords[i];
				String chemicalName = ar.Substance_Name;
				String CAS = ar.CAS_No.trim();

				if (chemicalName.contains(";")) {
					chemicalName = chemicalName.substring(0, chemicalName.indexOf(";"));
				}

				// to keep track of any matches found
				int count = 0;

				// loop through new record
				for (int j = 0; j < australiaRecords2.length; j++) {
					AustraliaRecord2 ar2 = australiaRecords2[j];
					String chemicalName2 = ar2.ChemicalName;
					String CAS2 = ar2.CasNumber.trim();

					if (chemicalName.equals(chemicalName2)) {
						count++;
					} else if (CAS.equals(CAS2)) {
						count++;
					}

					// check list of cas numbers if above fail
					if (count == 0) {
						String[] casList = CAS.split("\n");

						if (casList.length > 1) {
							for (int index = 0; index < casList.length; index++) {
								if (CAS.equals(casList[index])) {
									count++;
								}
							}
						}
					}
				}
				if (count == 0) {
					fw.write(chemicalName + ",\t" + CAS + "\r\n");
				}
			}

			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {

		ParseAustralia pa = new ParseAustralia();

		// pa.downloadWebpages(htmlFolder);
		// String folder = "L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA
		// Dashboard\\Data\\Australia";
		// String htmlFolder = "Chemical Webpages";
		// String output_JSON_FileName = "Australia Record.json";
		// pa.createAustraliaRecordsFromWebPages(folder, htmlFolder,
		// output_JSON_FileName);

//		 pa.parseWebpageLinks();
//		 pa.downloadTextLinks();

		pa.createFiles();
		// pa.compareJSONFiles();

	}
	

	@Override
	protected Chemicals goThroughOriginalRecords() {
		Chemicals chemicals=new Chemicals();
		
		try {
			
			File jsonFile = new File(mainFolder + "/" + fileNameJSON_Records);
			AustraliaRecord2[] records = gson.fromJson(new FileReader(jsonFile), AustraliaRecord2[].class);

			for (int i = 0; i < records.length; i++) {
				// for (int i = 0; i < 100; i++) {
				if (i % 500 == 0)
					System.out.println(i);

				AustraliaRecord2 ar = records[i];
				// if (!ir.CasNumber.equals("148324-78-1")) continue;
//				 System.out.println(gson.toJson(ar));
				Chemical chemical = this.createChemical(ar);
				if (chemical==null) continue;
				handleMultipleCAS(chemicals, chemical);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
		
	}

	private String convertCASList(String CAS) {
		String[] numbers = CAS.split("\n");
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
		CAS2 = CAS2.replace(";;", ";");

		return CAS2;
	}

	private ArrayList<String> convertCASToList(String CAS) {
		String[] numbers = CAS.split("\n");

		ArrayList<String> list = new ArrayList<String>();

		for (int j = 0; j < numbers.length; j++) {
			if (numbers[j].indexOf(" [") > -1) {
				list.add(numbers[j].substring(0, numbers[j].indexOf(" [")));
			} else {
				list.add(numbers[j]);
			}
		}
		return list;
	}

	private Chemical createChemical(AustraliaRecord2 ar) {

		// if (ar.CasNumber.equals("121-20-0")) System.out.println("here 121-20-0");

		ar.Sources=ar.Sources.replace("\n", "");
		
//		System.out.println(ar.CasNumber+"\t"+ar.Sources);
		
		String [] sources=ar.Sources.split("<br>");
		
		for (String source:sources) {
			if (source.contains("EU (Classification information")) {
				// skip it since we already have that covered by ECHA clp
				return null;
			}
		}
		
		Chemical chemical = new Chemical();
		chemical.CAS = ar.CasNumber.trim();
		chemical.name = ar.ChemicalName.trim().replace("\n", "");
//		System.out.println(chemical.CAS);

		// String strNote=ar.Source.trim()+": "+this.getSourceNote(ar.Source.trim());

		String strNote = ar.Sources;

		// String Note=getNote(ar);//TODO need to parse multiple notes separated by
		// space and only assign the notes that go with the appropriate tox field

		if (chemical.CAS.indexOf("\n") > -1) {
			String CAS2 = convertCASList(chemical.CAS);
			strNote += "<br><br>Record is for multiple CAS numbers: " + CAS2;
			// System.out.println("*"+note+"*");
			// System.out.println(chemical.CAS);
		}
		
		makeCategoriesConsistent(ar);
		makeHazardCodesConsistent(ar);

		goThroughHazardCodes(ar, chemical, strNote);

		if (ar.HazardCategories.size() > 0) {
			handleExtraCategories(ar, ar.HazardCategories, chemical, strNote);
		}

		assignMissingCategories(chemical);

//		 System.out.println(chemical.toJSONString());

		return chemical;
	}

	private void goThroughHazardCodes(AustraliaRecord2 ar, Chemical chemical, String strNote) {
		for (int i = 0; i < ar.HazardCodes.size(); i++) {
			String hazardCode = ar.HazardCodes.get(i);
			if (hazardCode.equals("")) continue;

			List<String> listScore = (List<String>) this.dictCodeToScoreName.get(hazardCode);

			if (listScore.size() == 0) {
//				System.out.println("No score name for " + hazardCode);
				continue;
			} else if (listScore.size() == 1) {
				if (listScore.get(0).equals("Omit")) {
					continue;
				}
			}

//			ArrayList<String> matchingCats = this.getCategory(hazardCode, chemical, ar);
			
			String hazardStatement = getHazardStatement(hazardCode, ar);

			// System.out.println("*** "+hazardCode);

			for (int k = 0; k < listScore.size(); k++) {

				String scoreName = listScore.get(k);
				Score score = chemical.getScore(scoreName);
				String toxRoute = null;
				String strScore = null;

				String hazardCategory = getCategory(scoreName,hazardCode, chemical, ar);

//				System.out.println("*"+hazardCode+"\t"+hazardCategory);
				

				if (debug)
					System.out.println(hazardCode + "\t" + hazardCategory + "\t" + hazardStatement);

				if (hazardCategory.equals("")) {
					System.out.println(chemical.CAS + "\tmissing category\t" + scoreName);
				}

				if (scoreName.equals(Chemical.strReproductive) || scoreName.equals(Chemical.strDevelopmental)) {
					strScore = getReproDevToxScoreValue(scoreName, hazardCode);
				} else {
					strScore = this.dictCodeToScoreValue.get(hazardCode);
				}

				if (strScore == null) {
					System.out.println("Score=null\t" + hazardCode + "\t" + scoreName);
					continue;
				}

				if (hazardStatement == null) {
					System.out.println(chemical.CAS + "\t" + hazardCategory + "\t" + hazardStatement);
					continue;
				}

				if (hazardStatement.indexOf("swallowed") > -1) {
					scoreName = Chemical.strAcute_Mammalian_ToxicityOral;
					toxRoute = "oral";
				} else if (hazardStatement.indexOf("skin") > -1) {
					scoreName = Chemical.strAcute_Mammalian_ToxicityDermal;
					toxRoute = "dermal";
				} else if (hazardStatement.indexOf("inhaled") > -1) {
					scoreName = Chemical.strAcute_Mammalian_ToxicityInhalation;
					toxRoute = "inhalation";
				} else {
					// System.out.println(hazardStatement);
				}

//				if (hazardCategory.indexOf("-") > -1) {
//					System.out.println(chemical.CAS + "\t" + hazardCategory);
//				}

				this.createScoreRecord(score, ar.CasNumber,ar.ChemicalName,hazardCategory, hazardCode, hazardStatement, toxRoute, strScore, strNote,ar.url);

			}

		}
	}

	private void makeHazardCodesConsistent(AustraliaRecord2 ar) {
		for (int i = 0; i < ar.HazardCodes.size(); i++) {

			String hazardCode = ar.HazardCodes.get(i).replace(" ", "").replace("H361f d", "H361fd").replace("H360FD.",
					"H360FD");

			if (hazardCode.equals("")) continue;
			ar.HazardCodes.set(i, hazardCode);
		}
	}

	private void makeCategoriesConsistent(AustraliaRecord2 ar) {
		for (int i = 0; i < ar.HazardCategories.size(); i++) {
			// Make things consistent:
			String category = ar.HazardCategories.get(i);
			
//			category=category.trim().replace("-", "-");
			category = category.replace("(inhalation)", "").replace("  ", " ");
			category = category.replace("(ingestion)", "").replace("  ", " ");
			category = category.replace("(dermal)", "").replace("  ", " ");
			
//			System.out.println("category="+category);
			
//			System.out.println(ar.HazardCategories.get(i)+"\t"+category);
			
			ar.HazardCategories.set(i, category);
		}
	}

	private void assignMissingCategories(Chemical chemical) {
		
		String catDevRepro="";

		for (int i = 0; i < chemical.scores.size(); i++) {
			Score score = chemical.scores.get(i);
			for (int j = 0; j < score.records.size(); j++) {
				ScoreRecord sr = score.records.get(j);
				if (!sr.category.equals("N/A")) {
					if (sr.category.contains("Reproductive")) {
						catDevRepro=sr.category;
					}
				}
			}
		}
		
		
		for (int i = 0; i < chemical.scores.size(); i++) {
			Score score = chemical.scores.get(i);
			for (int j = 0; j < score.records.size(); j++) {

				ScoreRecord sr = score.records.get(j);

				if (sr.category.equals("N/A")) {
					List<String> listCategory = (List<String>) this.dictCodeToCategory.get(sr.hazard_code);
					if (listCategory.size() == 0) {
						System.out.println(chemical.CAS + "\tNo category for " + sr.hazard_code);
					} else if (listCategory.size() == 1) {
						sr.category = listCategory.get(0);// assign missing category for that code
					} else {
						// Add missing category based on the hazard code:
						if (sr.hazard_code.equals("H300") || sr.hazard_code.equals("H310")
								|| sr.hazard_code.equals("H330")) {
							sr.category = "Acute toxicity - category 1 or 2";
						} else if (sr.hazard_code.contains("H360")) {
							if (!catDevRepro.isEmpty()) sr.category=catDevRepro;
							else {
								sr.category = "Reproductive toxicity - category 1A or 1B";
//								System.out.println(chemical.CAS+"\t"+sr.category);
							}
						} else {
							
							if (score.hazard_name.equals(Chemical.strSkin_Irritation)) {
								if (sr.hazard_code.equals("H314")) {
									sr.category="Skin corrosion - category 1";
								} else {
									System.out.println(chemical.CAS + "\tmultiple categories for " + sr.hazard_code+"\t"+sr.hazard_statement);									
								}
							} else {
								System.out.println(chemical.CAS + "\tmultiple categories for " + sr.hazard_code+"\t"+sr.hazard_statement);
							}
							
						}
					}
				} 
			}

		}
	}

	private String getHazardStatement(String hazardCode, AustraliaRecord2 ar) {
		List<String> listStatement = (List<String>) dictCodeToStatement.get(hazardCode);
		String hazardStatement = null;

		if (listStatement == null) {
			System.out.println(hazardCode + "\thazardStatementFromDict==null");
			return "N/A";
		}

		for (int k = 0; k < ar.HazardStatements.size(); k++) {
			String hazardStatementk = ar.HazardStatements.get(k).trim();
			// System.out.println("*"+hazardStatementk+"*");
			// System.out.println("hazardStatementFromDict="+hazardStatementFromDict+"\thazardStatementK="+hazardStatementk+"\thazardCode="+hazardCode);

			// if (hazardCode.equals("H411")) {
			// System.out.println(hazardStatementk);
			// System.out.println(listStatement.get(0));
			// }

			if (listStatement.contains(hazardStatementk)) {
				// System.out.println(chemical.CAS+"\t"+hazardCode+"\t"+hazardStatementFromDict+"\t"+hazardStatementk);
				hazardStatement = hazardStatementk;
				ar.HazardStatements.remove(k);
				return hazardStatement;
			}
		}
		return "N/A";
	}

	private String getCategory(String scoreName,String hazardCode, Chemical chemical, AustraliaRecord2 ar) {

//		ArrayList<String> matchingCats = new ArrayList<String>();

		List<String> listCategory = (List<String>) this.dictCodeToCategory.get(hazardCode);

		if (listCategory.size() == 0) {
			System.out.println(chemical.CAS + "\tNo category for " + hazardCode);
//			matchingCats.add("N/A");
			return "N/A";
		} else if (listCategory.size() == 1) {
			if (listCategory.get(0).equals("omit")) {
//				matchingCats.add("N/A");
//				return matchingCats;
				return "N/A";
			}
		}

		
		for (int i = 0; i < ar.HazardCategories.size(); i++) {

			if (scoreName.equals("Eye Irritation") && ar.HazardCategories.get(i).equals("Skin corrosion - category 1")) {
				continue;
			}
			
			for (int j = 0; j < listCategory.size(); j++) {
				
				
				if (scoreName.contains("Skin")) {
					if (!ar.HazardCategories.get(i).toLowerCase().contains("skin")) {
						continue;
					} 
				}
//
				if (scoreName.contains("Eye")) {
					if (!ar.HazardCategories.get(i).toLowerCase().contains("eye")) {
						continue;
					}
				}
					
				
//				System.out.println(ar.HazardCategories.get(i)+"\t"+listCategory.get(j));

				if (ar.HazardCategories.get(i).equals(listCategory.get(j))) {
					return ar.HazardCategories.remove(i);
//					if (!matchingCats.contains(category)) matchingCats.add(category);
//					break;
				}
			}
		}
		
//		return matchingCats;
		
		return "N/A";
	}

	private void handleExtraCategories(AustraliaRecord2 ar, ArrayList<String> GHSHazardCategories, Chemical chemical,
			String strNote) {
		// System.out.println("Remaining categories:");
		String CAS = ar.CasNumber;

		if (CAS.equals("35691-65-7") || CAS.equals("149861-22-3") || CAS.equals("302-97-6") || CAS.equals("122-60-1")) {
			// known errors
			return;
		}

		String[] strSkip = { "gasses under pressure", "flammable", "gas under", "aspiration", "self-heating",
				"corrosive","peroxide","explosive","reactive","oxidising","ozone","pyrophoric",
				"a ghs classification","respiratory sensitisation" };		
		
		
		for (int j = 0; j < GHSHazardCategories.size(); j++) {

			String cat = GHSHazardCategories.get(j);
			
			boolean skip=false;
			
			for (int k=0;k<strSkip.length;k++) {
				if (cat.toLowerCase().contains(strSkip[k])) {
					skip=true;
					break;
				}
			}
			
			if (skip) continue;

			if (cat.equals("")) continue;

			
			String hazardCategory = GHSHazardCategories.get(j).trim();
			String toxRoute = "";
			String hazardCode = "";
			Score score=null;
			String hazardStatement=null;
			String strScore=null;

			// It looks like australians took tox codes from the labelling column of Eu
			// table which is missing the code for acute aquatic tox (should have used
			// classification column)

			if (cat.indexOf("aquatic environment (acute)") > -1) {
				score = chemical.scoreAcute_Aquatic_Toxicity;
				if (cat.indexOf("category 1") > -1) {
					hazardCode = "H400";
				} else if (cat.indexOf("category 2") > -1) {
					hazardCode = "H401";
				} else if (cat.indexOf("category 3") > -1) {
					hazardCode = "H402";
				}

				if (debug)
					System.out.println(hazardCode + "\t" + cat + "\t" + hazardStatement);
				//

			} else if (cat.equals("Eye damage - category 1")) {
				score=chemical.scoreEye_Irritation;
 				hazardCode="H314";
			} else {
				// System.out.println("Respiratory sensitisation - category
				// 1"+"\t"+cat+"\t"+cat.equals("Respiratory sensitisation - category 1"));
				System.out.println(chemical.CAS+"\t"+chemical.name+"\t"+"unknown cat\t" +GHSHazardCategories.get(j));
				continue;
			}
			
//			System.out.println(hazardCode);
			
			
			hazardStatement = ((List<String>) dictCodeToStatement.get(hazardCode)).get(0);
			strScore = this.dictCodeToScoreValue.get(hazardCode);
			ScoreRecord sr=this.createScoreRecord(score, ar.CasNumber,ar.ChemicalName,hazardCategory, hazardCode, hazardStatement, toxRoute, strScore, strNote,ar.url);

//			System.out.println("handleExtraCategories:"+score.hazard_name+"\t"+sr.score+"\t"+sr.hazard_code+"\t"+sr.rationale+"\t"+sr.hazard_statement);
			

		} // end loop over hazard categories
	}

	private String fixStatements(String statements) {

		LinkedList<String> hazardStatements = gov.epa.ghs_data_gathering.Utilities.Utilities.Parse(statements, "\n");

		// System.out.println("1:"+statements);
		statements = "";

		for (int i = 0; i < hazardStatements.size(); i++) {
			String statement = hazardStatements.get(i);

			if (statement.equals("May damage fertility. May damage the unborn child")) {
				statement = "May damage fertility. May damage the unborn child.";// add a "." to end
			} else if (statement.equals("May damage the unborn child. Suspected of damaging fertility")) {
				statement = "May damage the unborn child. Suspected of damaging fertility.";// add a "." to end
			} else if (statement.equals("May damage fertility. Suspected of damaging the unborn child")) {
				statement = "May damage fertility. Suspected of damaging the unborn child."; // add a "." to end
			} else if (statement.equals("Toxic in inhaled")) {
				statement = "Toxic if inhaled";
			} else if (statement.equals("May damage the unborn child.")) {
				statement = "May damage the unborn child";// remove "." at end
			} else if (statement.equals("Suspected of damaging fertility. Suspected of damaging the unborn child")) {
				statement = "Suspected of damaging fertility. Suspected of damaging the unborn child.";// add a "."
			} else if (statement.equals("causes damage to the lungs through inhalation")) {
				statement = "Causes damage to the lungs through inhalation";// capitalize
			}

			statements += statement + "\n";
		}

		// System.out.println("2:"+statements);
		return statements;

	}

	private String getNote(AustraliaRecord ar) {

		// TODO this needs to take into account that can have "H K U\n8" as the Note
		// value
		// Need to figure out which notes go with which tox scores and assign
		// accordingly

		String Note = "";
		if (ar.Note.equals("A")) {
			Note = "The name of the substance should appear on the label in the form of one of the designations given in this spreadsheet. "
					+ "<br><br>Use is sometimes made of a general description such as ‘... compounds’ or ‘... salts’. In this case, the supplier is should state the correct name on the label.";
		} else if (ar.Note.equals("B")) {
			Note = "Some substances (acids, bases, etc.) are placed on the market in aqueous solutions at various concentrations and, therefore, these solutions require different classification and labelling since the hazards vary at different concentrations. "
					+ "<br><br>Entries with Note B have a general designation of the following type: ‘nitric acid … %’. "
					+ "<br><br>In this case the supplier should state the percentage concentration of the solution on the label. Unless otherwise stated, it is assumed that the percentage concentration is calculated on a weight/weight basis.";
		} else if (ar.Note.equals("C")) {
			Note = "\"Some organic substances may be marketed either in a specific isomeric form or as a mixture of several isomers."
					+ "<br><br>In this case the supplier should state on the label whether the substance is a specific isomer or a mixture of isomers.";
		} else if (ar.Note.equals("D")) {
			Note = "Certain substances which are susceptible to spontaneous polymerisation or decomposition are generally placed on the market in a stabilised form. It is in this form that they are listed in this spreadsheet."
					+ "<br><br>However, such substances are sometimes placed on the market in a non-stabilised form. In this case, the supplier should state on the label the name of the substance followed by the words ‘non-stabilised’.";
		} else if (ar.Note.equals("F")) {
			Note = "This substance may contain a stabiliser. If the stabiliser changes the hazardous properties of the substance, as indicated by the classification in this spreadsheet, classification and labelling should be provided in accordance with the rules for classification and labelling of hazardous mixtures, as set out in the GHS 3rd revised edition.";
		} else if (ar.Note.equals("G")) {
			Note = "This substance may be marketed in an explosive form in which case it must be evaluated using the appropriate test methods. The classification and labelling provided should reflect the explosive properties.";
		} else if (ar.Note.equals("H")) {
			Note = "\"The classification and labelling shown for this substance applies to the hazardous property(ies) indicated by the hazard statement(s) in combination with the hazard class(es) and category(ies) shown.\r\n"
					+ "<br><br>For hazard classes where the route of exposure or the nature of the effects leads to a differentiation of the classification of the hazard class, the manufacturer, importer or downstream user should consider the routes of exposure or the nature of the effects not already considered.";
		} else if (ar.Note.equals("J")) {
			Note = "The classification as a carcinogen or mutagen need not apply if it can be shown that the substance contains less than 0.1 % w/w benzene (CAS No 9072-35-9). This note applies only to certain complex coal- and oil derived substances in this spreadsheet.";
		} else if (ar.Note.equals("K")) {
			Note = "The classification as a carcinogen or mutagen need not apply if it can be shown that the substance contains less than 0.1 % w/w 1,3-butadiene (CAS No 106-99-0). This note applies only to certain complex oil-derived substances in this spreadsheet.";
		} else if (ar.Note.equals("L")) {
			Note = "The classification as a carcinogen need not apply if it can be shown that the substance contains less than 3 % DMSO extract as measured by IP 346 ‘Determination of polycyclic aromatics in unused lubricating base oils and asphaltene free petroleum fractions — Dimethyl sulphoxide extraction refractive index method’, Institute of Petroleum, London. This note applies only to certain complex oil-derived substances in this spreadsheet.";
		} else if (ar.Note.equals("M")) {
			Note = "The classification as a carcinogen need not apply if it can be shown that the substance contains less than 0.005 % w/w benzo[a]-pyrene (CAS No 50-32-8). This note applies only to certain complex coal-derived substances in this spreadsheet.";
		} else if (ar.Note.equals("N")) {
			Note = "The classification as a carcinogen need not apply if the full refining history is known and it can be shown that the substance from which it is produced is not a carcinogen. This note applies only to certain complex oil derived substances in this spreadsheet.";
		} else if (ar.Note.equals("P")) {
			Note = "The classification as a carcinogen or mutagen need not apply if it can be shown that the substance contains less than 0.1 % w/w benzene (CAS No 71-43-2).";
		} else if (ar.Note.equals("Q")) {
			Note = "The classification as a carcinogen need not apply if it can be shown that the substance fulfils one of the following conditions:"
					+ "<br><br>- a short term biopersistence test by inhalation has shown that the fibres longer than 20 μm have a weighted half-life less than 10 days; or"
					+ "<br><br>- a short term biopersistence test by intratracheal instillation has shown that the fibres longer than 20 μm have a weighted half-life less than 40 days; or"
					+ "<br><br>- an appropriate intra-peritoneal test has shown no evidence of excess carcinogenicity; or"
					+ "<br><br>- absence of relevant pathogenicity or neoplastic changes in a suitable long term inhalation test.";

		} else if (ar.Note.equals("R")) {
			Note = "R	The classification as a carcinogen need not apply to fibres with a length weighted geometric mean diameter less two standard geometric errors greater than 6 μm.";
		} else if (ar.Note.equals("T")) {
			Note = "This substance may be marketed in a form which does not have the physical hazards indicated by the classification in the entry in this spreadsheet. If the specific form of substance marketed does not exhibit these physical hazards, the substance should be given a classification which reflects this. Relevant information, including reference to the relevant test method(s) should be included in the safety data sheet.";
		} else if (ar.Note.equals("U")) {
			Note = "When put on the market gases should be classified as ‘Gases under pressure’, in one of the groups compressed gas, liquefied gas, refrigerated liquefied gas or dissolved gas. The group depends on the physical state in which the gas is packaged and therefore has to be assigned case by case.";
		} else if (ar.Note.equals("7")) {
			Note = "Alloys containing nickel are classified for skin sensitisation when the release rate of 0.5 μg Ni/cm2/week, as measured by the European Standard reference test method EN 1811, is exceeded.";
		} else if (ar.Note.equals("8")) {
			Note = "The tables in schedule 6 of the WHS regulations replace some tables in the GHS, this may affect the cut off concentrations for this chemical.";
		}

		return Note;

	}

	private String getReproDevToxScoreValue(String scoreName, String hazardCode) {

		// System.out.println(scoreName+"\t"+hazardCode);

		// No f or d
		if (hazardCode.toLowerCase().indexOf("f") == -1 && hazardCode.toLowerCase().indexOf("d") == -1) {
			if (hazardCode.equals("H360")) {
				return ScoreRecord.scoreH;
			} else if (hazardCode.equals("H361")) {
				return ScoreRecord.scoreM;
			} else if (hazardCode.equals("H362")) {
				return ScoreRecord.scoreH;
			} else {
				System.out.println("Unknown ReproDevTox code=" + hazardCode);
				return ScoreRecord.scoreNA;
			}

		}
		if (scoreName.equals(Chemical.strDevelopmental)) {
			if (hazardCode.indexOf("D") > -1) {
				return ScoreRecord.scoreH;
			} else if (hazardCode.indexOf("d") > -1) {
				return ScoreRecord.scoreM;
			} else {
				System.out.println("Unknown dev tox code = " + hazardCode);
				return ScoreRecord.scoreNA;
			}
		}
		if (scoreName.equals(Chemical.strReproductive)) {
			if (hazardCode.indexOf("F") > -1) {
				return ScoreRecord.scoreH;
			} else if (hazardCode.indexOf("f") > -1) {
				return ScoreRecord.scoreM;
			} else {
				System.out.println("Unknown repro tox code = " + hazardCode);
				return ScoreRecord.scoreNA;
			}
		}

		return ScoreRecord.scoreNA;

	}

	// private void handleReproDevTox(Chemical chemical, String hazardCode, String
	// hazardStatement, String category,
	// String note) {
	// // TODO assign score
	// /*
	// * Repr. 1A H360D Repr. 1B H360D Repr. 1B H360Df Repr. 1B H360F Repr. 1B
	// H360FD
	// * Repr. 2 H361d Repr. 2 H361f Repr. 2 H361fd
	// */
	//
	// if (hazardCode.indexOf("D") > -1) {
	// String strScore = ScoreRecord.scoreH;
	// Score score = chemical.scoreDevelopmental;
	// this.createScoreRecord(score, category, hazardCode, hazardStatement, "",
	// strScore, note);
	// }
	//
	// if (hazardCode.indexOf("d") > -1) {
	// String strScore = ScoreRecord.scoreM;
	// Score score = chemical.scoreDevelopmental;
	// this.createScoreRecord(score, category, hazardCode, hazardStatement, "",
	// strScore, note);
	// }
	//
	// if (hazardCode.indexOf("F") > -1) {
	// String strScore = ScoreRecord.scoreH;
	// Score score = chemical.scoreReproductive;
	// this.createScoreRecord(score, category, hazardCode, hazardStatement, "",
	// strScore, note);
	// }
	//
	// if (hazardCode.indexOf("f") > -1) {
	// String strScore = ScoreRecord.scoreM;
	// Score score = chemical.scoreReproductive;
	// this.createScoreRecord(score, category, hazardCode, hazardStatement, "",
	// strScore, note);
	// }
	// }

	private ScoreRecord createScoreRecord(Score score, String CAS,String name,String hazardCategory, String hazardCode, String hazardStatement,
			String toxRoute, String strScore, String strNote,String url) {
		ScoreRecord sr = new ScoreRecord();
		score.records.add(sr);

		sr.CAS=CAS;
		sr.name=name;
		sr.hazard_name=score.hazard_name;
		sr.source = ScoreRecord.sourceAustralia;
		sr.category = hazardCategory;// TODO or assign to classification?
		sr.hazard_code = hazardCode;
		sr.route = toxRoute;
		sr.hazard_statement = hazardStatement;
		sr.note = strNote;
		sr.score = strScore;
		sr.rationale = "Score of " + strScore + " was assigned based on a hazard code of " + hazardCode;
		sr.url=url;
		return sr;

	}

}

