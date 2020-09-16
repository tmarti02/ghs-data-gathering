package gov.epa.ghs_data_gathering.Parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.FlatFileRecord;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Utilities.FileUtilities;

public class ParseNewZealand extends Parse {

	Hashtable<String, String> dictCode = new Hashtable<String, String>();

	Hashtable<String, String> dictCat = new Hashtable<String, String>();

	Hashtable<String, String> dictScore = new Hashtable<String, String>();

	boolean useNewHTMLFormat=true;
	boolean useZipFile=true;
	boolean printToScreen=false;
	
	// Parsing Html files


	public ParseNewZealand() {
		
		sourceName = ScoreRecord.sourceNewZealand;
		folderNameWebpages="html files";
		fileNameHtmlZip="html files.zip";
		init();

		this.populateScoreDictionary();
		this.populateCodeDictionaries();
	}

	class NewZealandRecord {

		String CAS;
		String Chemical_Name;
		double Molecular_Weight;
		Vector<ToxRecord> toxRecords = new Vector<ToxRecord>();
		public String Synonyms;
		public String filename;

		public String toString() {
			String results = "+";
			for (ToxRecord tr : toxRecords) {
				results += tr.toString();
			}
			return results;
		}

	}

	class ToxRecord {

		String hazardCode;
		String hazardClassification;
		String classificationData;
		String route;

		public String toString() {

			return hazardCode + "<separator>" + hazardClassification + "<separator>" + classificationData;

		}

	}

	class tempRecord {

		ArrayList<ArrayList<String>> Classification = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> Classification_Data = new ArrayList<ArrayList<String>>();

	}

	private Vector<NewZealandRecord> parseHTML_Files_in_Folder(String htmlFolder) {

		// File inputFile = new File(htmlFile);
		System.out.println(htmlFolder);
		Vector<NewZealandRecord> NewZealand_Records = new Vector<>();
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
				Document doc = Jsoup.parse(inputFile, "utf-8");
				
                NewZealandRecord nzr=null;
                
                if (useNewHTMLFormat) {
                	nzr = this.parseNewZealandRecordNewFormat(inputFile.getName(),doc);
                } else {
                	nzr = this.parseNewZealandRecord(doc);	
                }

				NewZealand_Records.add(nzr);
			}

			return NewZealand_Records;

		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}

	}
	
		

	private Document getDocFromInputStream(InputStream inputStream) {
		return getDocFromInputStream(inputStream,"utf-8","");
	}

	private Document getDocFromInputStream(InputStream inputStream,String codeBase,String baseURL) {
		try {
			Document doc = Jsoup.parse(inputStream, codeBase,baseURL);
			return doc;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	
private Vector<NewZealandRecord> parseHTML_Files_in_Zip(String zipFilePath) {
		
		Vector<NewZealandRecord> newZealandRecords = new Vector<>();
		
		try {
		
			ZipFile zipFile = new ZipFile(zipFilePath);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			ZipEntry zipEntry0 = entries.nextElement();//entry for folder, discard
			
			int counter=0;
			
			while (entries.hasMoreElements()) {
				if (counter%1000==0) System.out.println(counter);
                final ZipEntry zipEntry = entries.nextElement();
                counter++;
                
//                if (!zipEntry.getName().equals("000602.html"))  continue;
//                System.out.println(zipEntry.getName());
                
                String name=zipEntry.getName();
//                name=name.substring(0, name.indexOf("."));
                
                if (name.contains("/")) name=name.substring(name.indexOf("/")+1,name.length());
                
                
//                while (name.substring(0,1).equals("0")) name=name.substring(1,name.length());

//                System.out.println(name);
                InputStream is=zipFile.getInputStream(zipEntry);
                Document doc = getDocFromInputStream(is, "utf-8","https://chem.nlm.nih.gov/chemidplus/rn");
				
                
                NewZealandRecord nzr=null;
                
                if (useNewHTMLFormat) {
                	nzr = this.parseNewZealandRecordNewFormat(name,doc);
                } else {
                	nzr = this.parseNewZealandRecord(doc);	
                }
				
				if (nzr==null) continue;

				if (nzr.Chemical_Name==null &&  nzr.CAS==null) {
					System.out.println(zipEntry.getName()+"\tno name or cas");
				}
				
//				if (nzr.Chemical_Name==null ||  nzr.CAS==null) {
//					System.out.println(zipEntry.getName()+"\t"+nzr.CAS+"\t"+nzr.Chemical_Name);
//				}

//				System.out.println(gson.toJson(nzr));
				
				if (nzr.toxRecords.size()==0) continue;
				
				newZealandRecords.add(nzr);
				
            }
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return newZealandRecords;
	}


	private NewZealandRecord  parseNewZealandRecordNewFormat(String filename,Document doc) {
		
		Elements elementsAccordion=doc.select("accordion");

		NewZealandRecord nzr = new NewZealandRecord();
		
		
		nzr.filename=filename;
		
		if (elementsAccordion.size()==0) {
			System.out.println(filename+"\taccordion not found");
			return null;
		}
		
		Element elementAccordion0=elementsAccordion.remove(0);
		getChemicalInfo(filename,nzr, elementAccordion0,printToScreen);

		getHazardData(nzr, elementsAccordion,printToScreen);
		
//		System.out.println(gson.toJson(nzr));
		return nzr;
		
	}




	private void getHazardData(NewZealandRecord nzr, Elements elementsAccordion,boolean printToScreen) {
		for (Element elementAccordion:elementsAccordion) {

			ToxRecord tr=new ToxRecord();
			nzr.toxRecords.add(tr);

			Element elementH5=elementAccordion.select("h5").first();
			String classification=elementH5.text();
			classification=classification.substring(0, classification.indexOf("Plus")).trim();
			tr.hazardCode=classification;
			if (printToScreen) System.out.println("Hazard code\t"+classification);

			Element elementDescriptionList=elementAccordion.select("dl.description-list").first();
			Elements termElements=elementDescriptionList.select("dt.description-list__term");

			
			for (Element element:termElements) {
				//System.out.println("\t"+element.tagName()+"\t"+element.attr("class"));
				String term=element.text();
				
				Element elementDescription=element.nextElementSibling();
				String description=elementDescription.text();

//				if (description.contains("REMARK: R28: LD50")) {
//					System.out.println(description);
//				}
				
				if(description.isEmpty()) continue;
				
				if (term.equals("Classification route species:")) {
					tr.route=description.replace("(", "").replace(")", "");
				}
				if (term.equals("Classification description:")) tr.hazardClassification=description;
				if (term.equals("Classification key study:")) {
//					System.out.println(elementDescription.html());
					tr.classificationData=elementDescription.html();
				}

				if (printToScreen) System.out.println(term+"\t"+description);
			}
		}
	}

	
	void parseMW(NewZealandRecord nzr,String MW) {
		//TODO - need to handle multiple values, if have mixture etc.
		//nzr.Molecular_Weight=Double.parseDouble(MW);
	}
	
	private void getChemicalInfo(String filename,NewZealandRecord nzr, Element elementAccordion0,boolean printToScreen) {

		try {
			Element elementDescriptionList=elementAccordion0.select("dl.description-list").first();
			Elements termElements=elementDescriptionList.select("dt.description-list__term");
			
			for (Element element:termElements) {
				//			System.out.println("\t"+element.tagName()+"\t"+element.attr("class"));
				String term=element.text();
				
				Element elementDescription=element.nextElementSibling();
				String description=elementDescription.text();
				
				if(description.isEmpty()) continue;
				
//				System.out.println(term+"\t"+description);
				
				if (term.equals("Name:")) nzr.Chemical_Name=description;
				if (term.equals("CAS Number:")) nzr.CAS=description;
				if (term.equals("Synonyms:")) nzr.Synonyms=description;
				if (term.equals("Molecular weight:") ) {					
					parseMW(nzr,description);
				}
				
				
				if(printToScreen) System.out.println(term+"\t"+description);
				
			}
			
			
			
			
//			if (nzr.CAS!=null && nzr.CAS.equals("50-00-0")) {
//				System.out.println(nzr.CAS+"\t"+nzr.Chemical_Name);			
//			}
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
//			System.out.println(filename+"\t"+elementDescriptionList.html());
		}
	}

	private NewZealandRecord  parseNewZealandRecord(Document doc) {
		NewZealandRecord nzr = new NewZealandRecord();
		//TODO- need to come up with new parser for new New Zealand format:
		//	See https://www.epa.govt.nz/database-search/chemical-classification-and-information-database-ccid/view/603
		
		
		Elements allInnerDivs = doc.select("div.inner");
		
		if (allInnerDivs.first()==null) return null;
		

		Elements mainDivBody = allInnerDivs.first().select("div.inner");

		// System.out.println(mainDivBody);

		tempRecord tr = new tempRecord();

		nzr.Chemical_Name = mainDivBody.select("h1").text();

		Elements tables = mainDivBody.select("table");

		parseFirstTable(tables, nzr);

		// System.out.println(tr.CAS);
		// System.out.println(tr.Molecular_Weight);

		for (Element table : tables) {

			String checkValue = table.select("th").first().toString();

			checkValue = checkValue.substring(checkValue.indexOf("C"), checkValue.indexOf("</th>")).trim();

			if (checkValue.equals("Classification")) {

				parseSecondTable(tr, table);
			} else if (checkValue.equals("Classification Data")) {

				parseThirdTable(tr, table);

			}
		}

		// System.out.println(tr.Classification);
		// System.out.println(tr.Classification_Data);

		for (int j = 0; j < tr.Classification.size(); j++) {

			ToxRecord toxRecord = new ToxRecord();

			toxRecord.hazardCode = tr.Classification_Data.get(j).get(0);
			toxRecord.hazardClassification = tr.Classification.get(j).get(2);
			toxRecord.classificationData = tr.Classification_Data.get(j).get(1);

			nzr.toxRecords.add(toxRecord);

		}
		return nzr;

	}

	private void parseFirstTable(Elements tables, NewZealandRecord nzr) {
		Element firstTable = tables.select("table").first();

		if (firstTable==null) return;
		
		Elements firstTableRows = firstTable.getElementsByTag("tr");

		int indexOfTR = 0;

		for (Element row : firstTableRows) {

			switch (indexOfTR) {

			case 0:
				nzr.CAS = row.getElementsByTag("td").text();
				break;

			case 2:
				String molecularWeight = row.getElementsByTag("td").text().trim();

				if (molecularWeight.contains(" ")) {
					molecularWeight = molecularWeight.substring(0, molecularWeight.indexOf(" "));
				}

				if (containsAlpha(molecularWeight) && !molecularWeight.equals("") && !molecularWeight.contains("-")
						&& !molecularWeight.contains(">") && !molecularWeight.contains("/"))
					if (molecularWeight.contains("(")) {
						molecularWeight = molecularWeight.substring(0, molecularWeight.indexOf("(")).trim();
						nzr.Molecular_Weight = Double.parseDouble(molecularWeight);
					} else
						nzr.Molecular_Weight = Double.parseDouble(molecularWeight);
				break;

			}

			indexOfTR++;
		}
	}

	private boolean containsAlpha(String weight) {
		if (weight.matches(".*[a-zA-Z]+.*")) {
			return false; // return false so that it matches if statement check
		} else
			return true;
	}

	private void parseSecondTable(tempRecord tr, Element table) {
		Element classificationTable = table;
		Elements tableRows = classificationTable.getElementsByTag("tr");

		for (Element row : tableRows) {

			if (!row.toString().contains("td")) {
				continue;
			}

			ArrayList<String> classificationRows = new ArrayList<>();

			classificationRows.add(row.getElementsByTag("td").get(0).text());
			classificationRows.add(row.getElementsByTag("td").get(1).text());
			classificationRows.add(row.getElementsByTag("td").get(2).text());

			tr.Classification.add(classificationRows);

		}
	}

	private void parseThirdTable(tempRecord tr, Element table) {
		Element classificationDataTable = table;
		Elements tableRows = classificationDataTable.getElementsByTag("tr");

		for (Element row : tableRows) {

			if (!row.toString().contains("td")) {
				continue;
			}

			ArrayList<String> classificationRows = new ArrayList<>();

			classificationRows.add(row.getElementsByTag("td").get(0).text());

			String classificationNotes = row.getElementsByTag("td").get(1).toString();

			classificationNotes = classificationNotes.replaceAll("<td>", "");
			classificationNotes = classificationNotes.replaceAll("</td>", "");
			classificationNotes = classificationNotes.replaceAll("<br>", "\n").trim();

			classificationRows.add(classificationNotes);

			tr.Classification_Data.add(classificationRows);
		}
	}

	
	@Override
	protected void createRecords() {

		Vector<NewZealandRecord> records=null;
		
		if (useZipFile) {
			records = parseHTML_Files_in_Zip(mainFolder + "/"+fileNameHtmlZip);	
		} else {
			records = parseHTML_Files_in_Folder(mainFolder + "/"+folderNameWebpages);	
		}
		
		writeOriginalRecordsToFile(records);
	}
	
	private void populateScoreDictionary() {

		dictScore.put("6.1", Chemical.strAcute_Mammalian_Toxicity);// need route to assign exact score
		dictScore.put("6.3", Chemical.strSkin_Irritation);
		dictScore.put("6.4", Chemical.strEye_Irritation);

		dictScore.put("6.5", Chemical.strSkin_Sensitization);

		dictScore.put("6.6", Chemical.strGenotoxicity_Mutagenicity);
		dictScore.put("6.7", Chemical.strCarcinogenicity);

		// TODO store in both reproTox or DevTox or combine the two:
		dictScore.put("6.8", Chemical.strReproductive);

		// //TODO add code to distinguish between single dose and repeated dose (look at
		// paragraph)
		// //TODO account for fact specific target organ tox has multiple routes?
		dictScore.put("6.9", Chemical.strSystemic_Toxicity_Repeat_Exposure);// Specific Target Organ Systemic Toxicity
																			// (Single Exposure or Repeated Exposure???)

		dictScore.put("8.2", Chemical.strSkin_Irritation);
		
		dictScore.put("8.3", Chemical.strEye_Irritation);

		dictScore.put("9.1", Chemical.strChronic_Aquatic_Toxicity);

	}

	private void populateCodeDictionaries() {

		// see
		// http://www.epa.govt.nz/hazardous-substances/about/what-is-hs/Pages/List-of-classifications.aspx
		// for NZ descriptions
		// see "hsnogen-ghs-nz-hazard.pdf" for NZ to GHS category conversion

		// TODO - add the GHS category to the ScoreRecord.category field to allow better
		// comparison between sources! i.e. 6.1A (Category 1)
		// i.e. create dictCategory!

		// ***************************************************************************************************************
		//Acute_Mammalian_Toxicity
		dictCode.put("6.1A", ScoreRecord.scoreVH);// Category 1 - ​Substances that are acutely toxic - Fatal
		dictCode.put("6.1B", ScoreRecord.scoreVH);// Category 2 - ​Substances that are acutely toxic - Fatal
		dictCode.put("6.1C", ScoreRecord.scoreH);// Category 3 - ​Substances that are acutely toxic- Toxic
		dictCode.put("6.1D", ScoreRecord.scoreM);// Category 4 - Substances that are acutely toxic - Harmful
		dictCode.put("6.1E", ScoreRecord.scoreL);// Category 5 - Substances that are acutely toxic –May be harmful,
													// Aspiration hazard

		dictCat.put("6.1A", "Category 1");
		dictCat.put("6.1B", "Category 2");
		dictCat.put("6.1C", "Category 3");
		dictCat.put("6.1D", "Category 4");
		dictCat.put("6.1E", "Category 5");

		// ***************************************************************************************************************
		//Skin Irritation
		dictCode.put("8.2A", ScoreRecord.scoreVH);// Corrosive to dermal tissue (Category 1A)
		dictCode.put("8.2B", ScoreRecord.scoreVH);// Corrosive to dermal tissue (Category 1B)
		dictCode.put("8.2C", ScoreRecord.scoreVH);// Corrosive to dermal tissue (Category 1C)

		dictCat.put("8.2A", "Category 1A");
		dictCat.put("8.2B", "Category 1B");
		dictCat.put("8.2C", "Category 1C");

		dictCode.put("6.3A", ScoreRecord.scoreH);// Category 2 - substances that are irritating to the skin
		dictCode.put("6.3B", ScoreRecord.scoreM);// Category 3 - substances that are mildly irritating to the skin

		dictCat.put("6.3A", "Category 2");
		dictCat.put("6.3B", "Category 3");

		// ***************************************************************************************************************
		//Respiratory sensitizers
		// dictCode.put("6.5A", ScoreRecord.scoreVH);//Category 1- ​Substances that are
		// respiratory sensitisers
		dictCode.put("6.5B", ScoreRecord.scoreM);// Category 1- Substances that are contact sensitisers
		dictCat.put("6.5B", "Category 1");

		// ***************************************************************************************************************
		//Genotoxicity_mutagencity
		dictCode.put("6.6A", ScoreRecord.scoreVH);// Category 1A or 1B - substances that are known or presumed human
													// mutagens
		dictCode.put("6.6B", ScoreRecord.scoreH);// Category 2 - substances that are suspected human mutagens

		dictCat.put("6.6A", "Category 1A or 1B");
		dictCat.put("6.6B", "Category 2");

		// ***************************************************************************************************************
		//Carcinogenicity
		dictCode.put("6.7A", ScoreRecord.scoreVH);// Category 1A or Category 1B - substances that are known or presumed
													// human carcinogens
		dictCode.put("6.7B", ScoreRecord.scoreH);// Category 2- substances that are suspected human carcinogens

		dictCat.put("6.7A", "Category 1A or 1B");
		dictCat.put("6.7B", "Category 2");

		// ***************************************************************************************************************
		//Eye irritation
		dictCode.put("8.3A", ScoreRecord.scoreVH);// ​Category 1 - Substances that are corrosive to ocular tissue
		dictCode.put("6.4A", ScoreRecord.scoreH);// Category 2A- substances that are irritating to the eye

		dictCat.put("8.3A", "Category 1");
		dictCat.put("6.4A", "Category 2A");

		// ***************************************************************************************************************
		//Reproductive or developmental
		dictCode.put("6.8A", ScoreRecord.scoreH);// ​Category 1A or 1B - ​Substances that are known or presumed human
													// reproductive or developmental toxicants
		dictCode.put("6.8B", ScoreRecord.scoreM);// Category 2 - Substances that are suspected human reproductive or
													// developmental toxicants
		dictCode.put("6.8C", ScoreRecord.scoreH);// Effects on or via lactation - Substances that produce toxic human
													// reproductive or developmental effects on or via lactation

		dictCat.put("6.8A", "Category 1A or 1B");
		dictCat.put("6.8B", "Category 2");
		dictCat.put("6.8C", "Effects on or via lactation");

		// ***************************************************************************************************************
//		Systemic_Toxicity_Repeat_Exposure
		dictCode.put("6.9A", ScoreRecord.scoreH);// Category 1 - ​Substances that are toxic to human target organs or
													// systems
		dictCode.put("6.9B", ScoreRecord.scoreM);// Category 2 - Substances that are harmful to human target organs or
													// systems

		dictCat.put("6.9A", "Category 1");
		dictCat.put("6.9B", "Category 2");

		// ***************************************************************************************************************
//		Chronic_Aquatic_Toxicity
		// TODO check this to see if matches DfE pdf:
		dictCode.put("9.1A", ScoreRecord.scoreVH);// Category 1 - ​Substances that are very ecotoxic in the aquatic
													// environment
		dictCode.put("9.1B", ScoreRecord.scoreH);// Category 2 - ​Substances that are ecotoxic in the aquatic
													// environment
		dictCode.put("9.1C", ScoreRecord.scoreM);// Category 3 - ​Substances that are harmful in the aquatic environment
		dictCode.put("9.1D", ScoreRecord.scoreL);// Category 4 - Substances that are slightly harmful to the aquatic
													// environment or are otherwise designed for biocidal action

		dictCat.put("9.1A", "Category 1");
		dictCat.put("9.1B", "Category 2");
		dictCat.put("9.1C", "Category 3");
		dictCat.put("9.1D", "Category 4");

		// ***************************************************************************************************************
		// 9.2A ​Substances that are very ecotoxic in the soil environment
		// ​9.2B ​Substances that are ecotoxic in the soil environment
		// ​9.2C ​Substances that are harmful in the soil environment
		// ​9.2D ​Substances that are slightly harmful in the soil environment

		// ​9.3A ​Substances that are very ecotoxic to terrestrial vertebrates
		// ​9.3B ​Substances that are ecotoxic to terrestrial vertebrates
		// ​9.3C ​Substances that are harmful to terrestrial vertebrates

		// 9.4A ​Substances that are very ecotoxic to terrestrial invertebrates
		// ​9.4B ​Substances that are ecotoxic to terrestrial invertebrates
		// ​9.4C ​Substances that are harmful to terrestrial invertebrates

	}

	private Chemical createChemical(NewZealandRecord nzr) {

		try {

			Chemical chemical = new Chemical();

			
			chemical.name = nzr.Chemical_Name;
			chemical.CAS = nzr.CAS;
			chemical.molecularWeight = nzr.Molecular_Weight;

			String [] codeSkip= {"1.1","1.2","1.3","1.4","1.5","2.1","3.1","4.1","4.2","4.3","5.1","5.2","8.1","9.2","9.3","9.4"};
			
			
			for (ToxRecord tr:nzr.toxRecords) {
				
				String toxCode = tr.hazardCode.replace("Classification ", "").trim();
				if (toxCode.contains("(")) {
					toxCode=toxCode.substring(0,toxCode.indexOf(" "));
				}
				
				String toxNumber = toxCode.substring(0, toxCode.length() - 1).trim();
				String toxLetter = toxCode.substring(toxCode.length() - 1, toxCode.length());

				
				boolean skip=false;
				for (String code:codeSkip) {
					if (toxNumber.contains(code)) {
						skip=true;
						break;
					}
				}
				if(skip) continue;

				
				
//				System.out.println(nzr.filename+"\t"+toxNumber+"\t"+toxLetter+"\t"+tr.route);
				
				if (tr.route!=null && tr.route.equals("All")) continue;//TODO
				
				
				// if (toxCode.indexOf("6.4")>-1) {
				// System.out.println(chemical.CAS+"\t"+toxCode);
				// }

				if (this.dictScore.get(toxNumber) != null) {

					String scoreName = this.dictScore.get(toxNumber);
					Score score = null;

//					 System.out.println(scoreName);

					if (scoreName.equals(chemical.strAcute_Mammalian_Toxicity)) {
						if (tr.route.equals("oral"))
							score = chemical.scoreAcute_Mammalian_ToxicityOral;
						else if (tr.route.equals("inhalation"))
							score = chemical.scoreAcute_Mammalian_ToxicityInhalation;
						else if (tr.route.equals("dermal"))
							score = chemical.scoreAcute_Mammalian_ToxicityDermal;
					} else {
						score = chemical.getScore(scoreName);
					}

					String strScore = dictCode.get(toxCode);
					if (strScore == null) {
						System.out.println(nzr.filename+"\t"+chemical.CAS + "\t" + toxCode + "\tmissing toxCode in dictCode");
					} else {
						// System.out.println(chemical.CAS+"\t"+toxCode+"\t"+dictCode.get(toxCode));
						 
						this.createScoreRecord(score, toxCode, tr.route, tr.hazardClassification, tr.classificationData, strScore);
					}

				} else {
					System.out.println(nzr.filename+"\t"+chemical.CAS + "\t" + toxNumber + "\tmissing toxCode in dictScore");
				}

				// TODO finish adding these to the dictionaries:

				// } else if (toxNumber.equals("6.8")) {
				// Score score = chemical.scoreReproductive();
				// assignFields(toxCode, toxRoute, toxClassification, toxJustification,
				// toxLetter, score);
				//
				// score = chemical.scoreDevelopmental();
				// assignFields(toxCode, toxRoute, toxClassification, toxJustification,
				// toxLetter, score);
				//
				// } else if (toxNumber.equals("6.9")) {
				// Score score = chemical.scoreRepeated_Dose();
				// assignFields(toxCode, toxRoute, toxClassification, toxJustification,
				// toxLetter, score);
				// } else if (toxNumber.equals("9.1")) {
				// Score score = chemical.scoreAcute_Aquatic_Toxicity();
				// assignFields(toxCode, toxRoute, toxClassification, toxJustification,
				// toxLetter, score);
				//
				// } else if (toxNumber.equals("9.3")) {
				// // Score score=chemical.;
				// // assignFields(toxCode, toxRoute, toxClassification, toxJustification,
				// // toxLetter, score);
				// } else if (toxNumber.equals("3.1") || toxNumber.equals("4.2") ||
				// toxNumber.equals("4.3")
				// || toxNumber.equals("5.2") || toxNumber.equals("8.1") ||
				// toxNumber.equals("9.2")
				// || toxNumber.equals("9.4")) {
				// // Do nothing
				// // 3.1 = Flammable liquids
				// // 4.2 = Spontaneously Combustible Substances
				// // 4.3 = Solids that emit flammable gas
				// // 5.2 = Organic peroxides
				// // 8.1 = Corrosive to metals
				// // 9.2 = Soil
				// // 9.4 = Harmful to terrestrial invertebrates
				// } else {
				// System.out.println(htmlFile.getName()+"\t"+tr2.toString());
				// }
			}

			// if (printToScreen) System.out.println(chemical.toJSONString());

			
//			if (chemical.CAS.equals("-") || chemical.CAS.equals("NA")) {
//				chemical.CAS = "NO_CAS_" + chemical.EC_number.trim();
//			} else {
//				chemical.CAS = chemical.CAS.trim();
//			}

//			chemical.writeToFile(jsonFolder);
			return chemical;
			

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}

	

	private void createScoreRecord(Score score, String toxCode, String toxRoute, String toxClassification,
			String toxJustification, String strScore) {
		ScoreRecord sr = new ScoreRecord();
		score.records.add(sr);
		sr.source = ScoreRecord.sourceNewZealand;
		sr.category = "Category " + toxCode;

		sr.category += " (" + dictCat.get(toxCode) + ")";

		sr.hazard_statement = toxClassification;// TODO or classification?

		sr.rationale = "Score was assigned based on a category of " + sr.category + ".";

		
		
		
//		if (toxJustification.indexOf("<br>") == 0) {
//			toxJustification = toxJustification.substring(4, toxJustification.length()).trim();
//		}
		
		if (toxJustification!=null) {
			sr.note = toxJustification;
			sr.note=sr.note.replace("mg/m|3|"," mg/m3");
			        
			if (sr.note.contains("COEFF")) {
				sr.note=sr.note.replace("\n", "<br>").replace("|", "/");
//				System.out.println(sr.note);	
			} else {
				if (sr.note.contains("|")) System.out.println("note has delimiter:"+sr.note);
			}
		}		
		
		sr.route = toxRoute;
		sr.score = strScore;
	}

	
	@Override
	protected Chemicals goThroughOriginalRecords() {
		
		Chemicals chemicals=new Chemicals();
		
		try {

			String jsonFilePath = mainFolder + File.separator + fileNameJSON_Records;

			Gson gson = new Gson();
			NewZealandRecord[] records = gson.fromJson(new FileReader(jsonFilePath), NewZealandRecord[].class);

			for (int i = 0; i < records.length; i++) {
				NewZealandRecord newZealandRecord = records[i];
//				System.out.println(i);
				Chemical chemical=createChemical(newZealandRecord);
				if (chemical==null) continue;
				handleMultipleCAS(chemicals, chemical);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}
	
	void compareFiles() {
		
		String filePathOld=mainFolder+"/New Zealand Chemical Records (Old).txt";
		String filePathNew=mainFolder+"/New Zealand Chemical Records.txt";
		
		ArrayList <String>linesOld=gov.epa.ghs_data_gathering.Utilities.Utilities.readFileToArray(filePathOld);
		ArrayList <String>linesNew=gov.epa.ghs_data_gathering.Utilities.Utilities.readFileToArray(filePathNew);
		
		linesOld.remove(0);
		linesNew.remove(0);
		
		ArrayList<String>uniqueCASOld=new ArrayList<>();
		ArrayList<String>uniqueCASNew=new ArrayList<>();
		
		for (String Line:linesOld) {
			String CAS=Line.substring(0,Line.indexOf("|"));
			if(!uniqueCASOld.contains(CAS)) {
//				System.out.println(CAS);
				uniqueCASOld.add(CAS);
			}
		}
		
		for (String Line:linesNew) {
			String CAS=Line.substring(0,Line.indexOf("|"));
			if(!uniqueCASNew.contains(CAS)) {
//				System.out.println(CAS);
				uniqueCASNew.add(CAS);
			}
		}
		
		System.out.println("in old, not in new:");
		for (String CASold:uniqueCASOld) {
			if (!uniqueCASNew.contains(CASold)) {
				System.out.println(CASold);
			}
		}
		
		System.out.println("\nin new, not in old:");
		for (String CASNew:uniqueCASNew) {
			if (!uniqueCASOld.contains(CASNew)) {
				System.out.println(CASNew);
			}
		}

		

		
	}
	

	void goThroughCASList()
	{
		
		String filePath="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\New Zealand\\unique cas numbers.txt";
	
		ArrayList<String>caslist=gov.epa.ghs_data_gathering.Utilities.Utilities.readFileToArray(filePath);
		
		String CAS=caslist.get(0);
				
		String url="https://www.epa.govt.nz/database-search/chemical-classification-and-information-database-ccid/DatabaseSearchForm?SiteDatabaseSearchFilters=35&Keyword="+CAS+"&DatabaseType=CCID";
//		System.out.println(url);
		
		this.accessLink(url);
		
//		
	}
	
	/**
	 * Download a webpage line by line:
	 * 
	 * @param URL
	 * @param destFilePath
	 */
	public static void accessLink(String URL) {

		try {


			System.out.println(URL);
			//			System.out.println(destFilePath+"\n");

			java.net.URL myURL = new java.net.URL(URL);

			BufferedReader br
			= new BufferedReader(new InputStreamReader(myURL.openStream()));

			int counter=0;

			while (true) {
				String Line=br.readLine();

				if (Line==null) break;

				
				System.out.println(Line);
				
//				if (Line.contains("database-search/chemical-classification-and-information-database-ccid/DatabaseSearchForm")) {
//					System.out.println(Line);
//					break;
//				}
				
				counter++;
			}

			br.close();
			

		} catch (FileNotFoundException ex1) {
			System.out.println("file not found");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	void downloadFiles() {
		
		String destFolder=mainFolder+"/html files";
		File f=new File(destFolder);
		
		if (!f.exists()) f.mkdirs();
		
		
		for (int i=16253;i<=16253;i++) {
			String url="https://www.epa.govt.nz/database-search/chemical-classification-and-information-database-ccid/view/"+i;
//			System.out.println(i);
			FileUtilities.downloadfile2(url, destFolder+"/"+i+".html");
			
			File destFile=new File(destFolder+"/"+i+".html");
			if (destFile.exists()) {
				System.out.println(destFile.getName()+"\t"+destFile.length());
			} else {
				System.out.println(destFile.getName()+"\tnot found");
			}
			
			try {
				Thread.sleep(3000);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
		}
	}
	
	void testParse() {
		
		try {
//			int fileNum=10004;
			int fileNum=616;
			File inputFile=new File(mainFolder+"/new html files/"+fileNum+".html");
			FileInputStream fis=new FileInputStream(inputFile);
			Document doc=getDocFromInputStream(fis);
			NewZealandRecord nzr=parseNewZealandRecordNewFormat(inputFile.getName(),doc);
			
			System.out.println(gson.toJson(nzr));
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	public static void main(String[] args) {

		ParseNewZealand pz = new ParseNewZealand();

//		pz.accessLink("https://www.epa.govt.nz/database-search/chemical-classification-and-information-database-ccid/view/16696");
//		pz.downloadFiles();
//		pz.testParse();
		
//		pz.createRecords();
		pz.createFiles();
//		pz.goThroughCASList();
//		pz.compareFiles();

	}

}