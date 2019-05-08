package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import gov.epa.ghs_data_gathering.Utilities.FileUtilities;

public class ParseChemidplusHTML {

	public static final String folder = "AA Dashboard\\Data\\Chemidplus\\";

	public static final String textFile = folder + "CAS Numbers Inhalation.txt";

	public static final String webpageFolder = folder + "All Webpages";

	class CASRecord {
		ArrayList<String> CAS = new ArrayList<>();
	}

	class PhysicalPropertyRecord {
		String PhysicalProperty;
		String Value;
		String Units;
		String TempdegC;
		String Source;
	}

	class ChemidplusRecord {

		String Molecular_Formula;
		String Molecular_Weight;

		ArrayList<String> ClassificationCodes = new ArrayList<>();
		ArrayList<String> SuperlistClassificationCodes = new ArrayList<>();

		String NameOfSubstance;
		ArrayList<String> Synonyms = new ArrayList<>();
		ArrayList<String> SystematicNames = new ArrayList<>();
		ArrayList<String> SuperListNames = new ArrayList<>();

		String CASRegistryNumber;
		ArrayList<String> OtherRegistryNumbers = new ArrayList<>();

		String Inchi;
		String InchiKey;
		String Smiles;

		ArrayList<ToxicityRecord> ToxicityRecords = new ArrayList<>();
		ArrayList<PhysicalPropertyRecord> PhysicalProperties = new ArrayList<>();

	}

	
	public Vector<ChemidplusRecord> parseHTML_Files_in_Zip(String zipFilePath) {
		
		Vector<ChemidplusRecord> ChemID_Records = new Vector<>();
		
		try {
		
			ZipFile zipFile = new ZipFile(zipFilePath);
			
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			ZipEntry zipEntry0 = entries.nextElement();//entry for folder, discard
			
			int counter=0;
			
			while (entries.hasMoreElements()) {
				if (counter%1000==0) System.out.println(counter);
                final ZipEntry zipEntry = entries.nextElement();
//                System.out.println(entry.getName());
                Document doc = Jsoup.parse(zipFile.getInputStream(zipEntry),"utf-8","https://chem.nlm.nih.gov/chemidplus/rn");
				ChemidplusRecord cr = createChemidplusRecord(doc);
				ChemID_Records.add(cr);
				counter++;
            }
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return ChemID_Records;
	}

	private ChemidplusRecord createChemidplusRecord(Document doc) {
		ChemidplusRecord cr = new ChemidplusRecord();

		Elements innerBody = doc.select("div.innerMain");

		// for molecular weight and formula
		//contained in the chemical "summary" section of html
		Elements summaryOfChemical = innerBody.select("div#summary");
		parseMolecularFormulaAndWeight(cr, summaryOfChemical);

		// contains all necessary contents
		Elements divTabs = innerBody.select("div#tabs");

		// checking if section exists
		Elements headerTags = divTabs.select("h1, h2, h3, h4, h5, h6");

		parseWebpage(cr, divTabs, headerTags, innerBody);
		return cr;
	}
	
	/**
	 * Moves files from a search to the main folder with all the webpages
	 * 
	 * @throws IOException
	 */
	private void copyFilesToFolder(String sourceFolder,String destFolder)  {

		File destinationFolder = new File(destFolder);

		File fileFolder = new File(sourceFolder);
		File[] files = fileFolder.listFiles();

		for (int i = 0; i < files.length; i++) {
			File inputFile = files[i];

//			if (inputFile.exists())
//				continue;

			try {
				FileUtils.copyFileToDirectory(inputFile, destinationFolder);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

	}
	
	public ArrayList<ChemidplusRecord> parseHTML(String htmlFolder) {

		Document doc;

		ArrayList<ChemidplusRecord> ChemID_Records = new ArrayList<>();

		try {

			File fileFolder = new File(htmlFolder);
			File[] files = fileFolder.listFiles();

			for (int i = 0; i < files.length; i++) {
				File inputFile = files[i];

				if (inputFile.getName().indexOf(".html") == -1)
					continue;

				if (i % 100 == 0)
					System.out.println(i);

				// System.out.println(inputFile);

				doc = Jsoup.parse(inputFile, "utf-8");

				ChemidplusRecord cr=this.createChemidplusRecord(doc);

				// add toString method to ToxicityRecords
				// System.out.println(cr.ToxicityRecords);

				// System.out.println(cr.Molecular_Formula);
				// System.out.println(cr.Molecular_Weight);
				// System.out.println("-------------");

				ChemID_Records.add(cr);
			}

			return ChemID_Records;

		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
	}

	/*
	 * parses CAS number from search files
	 * and stores them in the CAS record class
	 * downloads the webpages and writes CAS #'s to text file
	 */
	
	private void htmlParse(String filePathFolder_HTMLSearch,String destFolder) {

		CASRecord cr = new CASRecord();

		Document doc;

		try {

			File fileFolder = new File(filePathFolder_HTMLSearch);
			File[] files = fileFolder.listFiles();

			for (int i = 0; i < files.length; i++) {
				File inputFile = files[i];

				if (inputFile.getName().indexOf(".html") == -1)
					continue;

				doc = Jsoup.parse(inputFile, "utf-8");

				Elements mainTable = doc.select("table");

				Element innerTable = mainTable.select("tbody").get(1);

				Elements CAS = innerTable.select("td td");

				for (Element e : CAS) {

					// removing newline characters
					if (e.ownText().isEmpty()) {
						continue;
					}

					cr.CAS.add(e.ownText());
				}
			}
			writeToTextFile(cr);
			downloadWebPages(cr,destFolder);

		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	/*
	 * writes CAS Record Cas's to text file
	 */
	private void writeToTextFile(CASRecord cr) {

		try {

			File inputTextFile = new File(textFile);
			FileWriter fw = new FileWriter(inputTextFile);

			for (int i = 0; i < cr.CAS.size(); i++) {
				fw.write(cr.CAS.get(i) + "\r\n");
			}

			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/*
	 * called in the htmlParse method
	 * loops through all of the CAS numbers
	 * and downloads respective webpages
	 */
	private void downloadWebPages(CASRecord cr,String destFolder) {

		String url = "https://chem.nlm.nih.gov/chemidplus/rn/";

		try {

			for (int i = 0; i < cr.CAS.size(); i++) {

				String CAS = cr.CAS.get(i);

				String strURL = url + CAS;

				String destFilePath = destFolder + "/" + CAS + ".html";

				File destFile = new File(destFilePath);

				if (destFile.exists())
					continue;

				FileUtilities.downloadFile(strURL, destFilePath);

				Thread.sleep(3000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/*
	 * main method that executes
	 * parse methods
	 */
	private void parseWebpage(ChemidplusRecord cr, Elements divTabs, Elements headerTags, Elements innerBody) {

		// if the headers contain the following strings
		// they will execute parse method

		if (headerTags.text().contains("Classification Codes")) {
			parseClassificationCodes(cr, divTabs);
		}

		if (headerTags.text().contains("Name of Substance")) {
			parseName(cr, divTabs);
		} else if (innerBody.text().contains("Substance Name")) {
			String name = innerBody.select("h1").first().text();
			name = name.substring(16, name.indexOf("RN:")).trim();

			cr.NameOfSubstance = name;
		}

		if (headerTags.text().contains("Synonyms")) {
			parseSynonyms(cr, divTabs);
		}

		if (headerTags.text().contains("Systematic Names")) {
			parseSystematicNames(cr, divTabs);
		}

		if (headerTags.text().contains("Superlist Names")) {
			parseSuperlistNames(cr, divTabs);
		}

		if (headerTags.text().contains("CAS Registry Number")) {
			parseCAS(cr, divTabs);
		}

		if (headerTags.text().contains("Other Registry Numbers")) {
			parseOtherRegistryNumbers(cr, divTabs);
		}

		if (headerTags.text().contains("InChI")) {
			parseInchiAndSmiles(cr, divTabs);
		}

		if (headerTags.text().contains("Toxicity")) {
			parseToxicityTable(cr, divTabs);
		}

		if (headerTags.text().contains("Physical Properties")) {
			parsePhysicalPropertiesTable(cr, divTabs);
		}
	}

	private void parseMolecularFormulaAndWeight(ChemidplusRecord cr, Elements summary) {
		Elements headers = summary.select("div[class] > div div div h2");

		// loop through all headers(h2) within the summary tag
		for (int i = 0; i < headers.size(); i++) {
			String header = headers.get(i).select("h2").text();

			if (header.equals("Molecular Formula")) {
				cr.Molecular_Formula = headers.get(i).siblingElements().first().text();
			} else if (header.equals("Molecular Weight")) {
				cr.Molecular_Weight = headers.get(i).siblingElements().text();
			}
		}

	}

	private void parsePhysicalPropertiesTable(ChemidplusRecord cr, Elements divTabs) {
		Elements physicalTable = divTabs.select("div#physical tr");

		for (int i = 1; i < physicalTable.size(); i++) {
			Element table = physicalTable.get(i);
			Elements tableRows = table.select("td");

			PhysicalPropertyRecord pr = new PhysicalPropertyRecord();

			for (int rowIndex = 0; rowIndex < tableRows.size(); rowIndex++) {
				String row = tableRows.get(rowIndex).text();

				switch (rowIndex) {
				case 0:
					pr.PhysicalProperty = row;
					break;
				case 1:
					pr.Value = row;
					break;
				case 2:
					pr.Units = row;
					break;
				case 3:
					pr.TempdegC = row;
					break;
				case 4:
					pr.Source = row;
					break;
				}

			}
			cr.PhysicalProperties.add(pr);
		}
	}

	class ToxicityRecord {
		String Organism;
		String TestType;
		String Route;
		String ReportedDose;
		String NormalizedDose;// appears below ReportDose in ()
		String Effect;
		String Source;
	}

	@SuppressWarnings("unused")
	private void parseToxicityTable(ChemidplusRecord cr, Elements divTabs) {
		Elements toxicityTable = divTabs.select("div#toxicity tr");

		
		for (int i = 1; i < toxicityTable.size(); i++) {
			Element tableRow = toxicityTable.get(i);
			Elements tableColumnCells = tableRow.select("td");

			ToxicityRecord tr = new ToxicityRecord();

			tr.Organism = tableColumnCells.get(0).text().trim();
			tr.TestType = tableColumnCells.get(1).text().trim();
			tr.Route = tableColumnCells.get(2).text().trim();

			String reportedAndNormalizedDoseColumn = tableColumnCells.get(3).text().trim();

			if (reportedAndNormalizedDoseColumn.contains("(")) {

				// 790mg(base)/k (790mg/kg)
				// tr.ReportedDose = row.substring(0, row.indexOf(" ")).trim();
				// tr.NormalizedDose = row.substring(row.indexOf("(") + 1, row.length() - 1);

				int countSpaces = 0;
				int indexLastSpace = -1;

				for (int j = 0; j < reportedAndNormalizedDoseColumn.length(); j++) {
					if (reportedAndNormalizedDoseColumn.substring(j, j + 1).equals(" ")) {
						countSpaces++;
						indexLastSpace = j;
					}
				}

				tr.ReportedDose = reportedAndNormalizedDoseColumn.substring(0, indexLastSpace).trim();
				tr.NormalizedDose = reportedAndNormalizedDoseColumn.substring(indexLastSpace + 2,
						reportedAndNormalizedDoseColumn.length() - 1);

				// System.out.println(tr.ReportedDose+"\t"+tr.NormalizedDose);

				// System.out.println(tr.ReportedDose);
			} else
				tr.ReportedDose = tableColumnCells.get(3).text().trim();

			tr.Effect = tableColumnCells.get(4).text().trim();
			tr.Source = tableColumnCells.get(5).text().trim();

			cr.ToxicityRecords.add(tr);
		}

	}

	private void parseInchiAndSmiles(ChemidplusRecord cr, Elements divTabs) {
		Elements inchiAndSmiles = divTabs.select("div#structureDescs div");

		for (int i = 0; i < inchiAndSmiles.size(); i++) {
			String header = inchiAndSmiles.get(i).select("h3").text();
			if (header.equals("InChI")) {
				cr.Inchi = inchiAndSmiles.get(i).ownText();
			} else if (header.equals("InChIKey")) {
				cr.InchiKey = inchiAndSmiles.get(i).ownText();
			} else if (header.equals("Smiles")) {
				cr.Smiles = inchiAndSmiles.get(i).ownText();
			}

		}
	}

	private void parseOtherRegistryNumbers(ChemidplusRecord cr, Elements divTabs) {
		Elements otherRegistryNumbers = divTabs.select("div#numbers div.ds > div[class] div ul li div");

		for (Element e : otherRegistryNumbers) {
			cr.OtherRegistryNumbers.add(e.text());
		}
	}

	private void parseCAS(ChemidplusRecord cr, Elements divTabs) {
		String CAS = divTabs.select("div#numbers div.ds > ul li div").get(0).text();

		cr.CASRegistryNumber = CAS;
	}

	private void parseSuperlistNames(ChemidplusRecord cr, Elements divTabs) {
		Elements headers = divTabs.select("h3");

		Element superlistBlock = null;

		for (Element e : headers) {
			if (e.text().equals("Superlist Names")) {
				superlistBlock = e.nextElementSibling();
			}
		}

		Elements innerElements = superlistBlock.select("div ul li div");

		for (Element e : innerElements) {
			cr.SuperListNames.add(e.text());
		}
	}

	private void parseSystematicNames(ChemidplusRecord cr, Elements divTabs) {
		// > selects base child elements
		Elements headers = divTabs.select("h3");

		Element systematicBlock = null;

		for (Element e : headers) {
			if (e.text().equals("Systematic Names") || e.text().equals("Systematic Name")) {
				systematicBlock = e.nextElementSibling();
			}
		}

		// Element systematicNames = divTabs.select("div#names div.ds >
		// div[class]").get(1);

		Elements innerElements = systematicBlock.select("ul");

		for (Element e : innerElements) {
			cr.SystematicNames.add(e.text());
		}
	}

	private void parseSynonyms(ChemidplusRecord cr, Elements divTabs) {
		// Element synonyms = divTabs.select("div#names div.ds div[class]").get(0);
		// try catch error block?
		Elements headers = divTabs.select("h3");

		Element synonymBlock = null;

		for (Element e : headers) {
			if (e.text().equals("Synonyms") || e.text().equals("Synonym")) {
				synonymBlock = e.nextElementSibling();
			}
		}
		// System.out.println(synonymBlock);

		if (synonymBlock != null) {
			Elements innerLists = synonymBlock.select("div ul li div");

			for (Element e : innerLists) {
				cr.Synonyms.add(e.text());
			}
		}
	}

	private void parseName(ChemidplusRecord cr, Elements divTabs) {
	    String name = divTabs.select("div#names div.ds ul li div").first().text();

		cr.NameOfSubstance = name;
	}

	private void parseClassificationCodes(ChemidplusRecord cr, Elements divTabs) {
		Elements classifications = divTabs.select("div[id~=classifications$] h3");

		for (Element element : classifications) {
			StringBuilder sb = new StringBuilder(element.toString());

			Element next = element.nextElementSibling();
			while (next != null && !next.tagName().startsWith("h")) {
				sb.append(next.toString()).append("\n");
				String temp = sb.toString();
				// System.out.println(temp);
				if (temp.contains("<h3>Classification Codes</h3>")) {
					Elements classificationData = next.select("div div div");
					for (Element e : classificationData) {
						// System.out.println(e.text());
						cr.ClassificationCodes.add(e.text());
					}

				} else if (temp.contains("<h3>Superlist Classification Codes</h3>")) {
					Elements superClassificationData = next.select("div div div");
					for (Element e : superClassificationData) {
						// System.out.println(e.text());
						cr.SuperlistClassificationCodes.add(e.text());
					}
				}
				next = next.nextElementSibling();
			}
		}
	}

	public static void main(String[] args) {

		ParseChemidplusHTML ph = new ParseChemidplusHTML();

		//Go through search webpages and download the individual webpages:
		ph.htmlParse(folder + "Oral LD50 searches",webpageFolder);
		ph.htmlParse(folder + "Skin LD50 searches",webpageFolder);
		ph.htmlParse(folder + "Inhalation LC50 searches",webpageFolder);
		

		//Parse all the webpages into json file:
//		 ArrayList<ChemidplusRecord>records=ph.parseHTML(webpageFolder);

	}
}
