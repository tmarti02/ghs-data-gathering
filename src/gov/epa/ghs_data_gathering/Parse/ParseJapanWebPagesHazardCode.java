package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.google.common.collect.Multimap;

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.HazardRecord;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;

/**
 * This class takes data from NITE webpages and creates chemical objects
 * using the "Hazard code" to determine the scores. 
 * 
 * This is the current best method for parsing Japan
 * 
 * This is Java conversion of
 * python code by Wehage:
 * 
 * ghs.py
 * This module contains a Python class, 'GHSJapanData', that is used to import
 * data from the GHS Japan website and translate the hazards from their native
 * representation to GreenScreen hazard specification.
 * Copyright 2013-2015 Kristopher Wehage University of California-Davis (ktwehage@ucdavis.edu)
 * 
 * @author Todd Martin (Java)
 * 
 */
public class ParseJapanWebPagesHazardCode extends Parse{

	Multimap<String, String> dictCodeToScoreName = CodeDictionary.populateCodeToScoreName();
	Hashtable<String, String> dictCodeToScoreValue = CodeDictionary.populateCodeToScoreValue();	
	Multimap<String,String>dictHazardNameToScoreName=CodeDictionary.populateJapanHazardClassToScoreName();
	
	String [] neuro_keywords= {"neuro","nervous"};

	String[] systemic_keywords = { "respiratory", "blood", "kidney", "liver", "adrenal", "gastro", "systemic", "eye",
			"heart", "bone", "hematop", "cardio", "spleen", "thyroid", "lung", "gingi", "testes", "urinary" };
	
	
	public ParseJapanWebPagesHazardCode() {
		sourceName=ScoreRecord.sourceJapan;
//		sourceName="Japan (webpages and classification)";
		folderNameWebpages = "web pages";
		fileNameHtmlZip="web pages.zip";
		this.init();
		//Add the following since can be used for neurotox depending on keywords:		
		addNeurotoxicityToCodeToScoreNameDictionary();
	}	

	private void addNeurotoxicityToCodeToScoreNameDictionary() {
		dictCodeToScoreName.put("H372", Chemical.strNeurotoxicity_Repeat_Exposure);
		dictCodeToScoreName.put("H373", Chemical.strNeurotoxicity_Repeat_Exposure);

		// Systemic toxicity, single dose
		dictCodeToScoreName.put("H370", Chemical.strNeurotoxicity_Single_Exposure);
		dictCodeToScoreName.put("H371", Chemical.strNeurotoxicity_Single_Exposure);
		
//		//Do we want to include this:
//		dictCodeToScoreName.put("H335", Chemical.strNeurotoxicity_Single_Exposure);
//		dictCodeToScoreName.put("H336", Chemical.strNeurotoxicity_Single_Exposure);
	}
	

	
	@Override
	protected void createRecords() {
		
//		Vector<JapanRecord> records = parseChemicalWebpages(mainFolder + "/" + folderNameWebpages);
		Vector<JapanRecord> records = JapanRecord.parseChemicalWebpagesInZipFile(mainFolder + "/" + this.fileNameHtmlZip);
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected Chemicals goThroughOriginalRecords() {

		Chemicals chemicals=new Chemicals();
		
		ArrayList<String>uniqueCAS=new ArrayList<>();
		
		try {
			File jsonFile = new File(mainFolder + "/" + fileNameJSON_Records);
			
			JapanRecord[] records = gson.fromJson(new FileReader(jsonFile), JapanRecord[].class);

			System.out.println("Creating chemicals from Japan records json file");

			for (int i = 0; i < records.length; i++) {

				if (i % 500 == 0)
					System.out.println(i);

				JapanRecord jr = records[i];
				
//				if (!jr.CAS.equals("79-06-1")) continue;
				
				// Translate scores:
				Chemicals chemicalsi=this.createChemicals(jr);
				
				
				for (Chemical chemicali:chemicalsi) {
					
					if (chemicali.CAS==null || chemicali.CAS.isEmpty() || chemicali.CAS.equals("-")) {
						chemicali.CAS=chemicali.name;
					}
					
					if (uniqueCAS.contains(chemicali.CAS)) {
						int counter=2;
						String newCAS="";
						while (true) {
							newCAS=chemicali.CAS+"_"+counter;
							if (!uniqueCAS.contains(newCAS)) {
								break;
							}
							counter++;
						}
						chemicali.CAS=newCAS;
						uniqueCAS.add(chemicali.CAS);
						
						
						
//						System.out.println(newCAS);
					} else {
						uniqueCAS.add(chemicali.CAS);
					}
					chemicals.add(chemicali);
				}
			}
			
			chemicals.mergeRevisions();
			
			for (Chemical chemical:chemicals) {
				if (chemical.CAS.equals(chemical.name)) {
					chemical.CAS="";
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
		
	}
	
	
	
	
	
	private Chemicals createChemicals(JapanRecord jr) {
		
		Chemicals chemicals=new Chemicals();
		if (jr.CAS==null) {
			Chemical chemical = new Chemical();
			chemical.name =jr.chemicalName;
			chemical.CAS = chemical.name;
				// System.out.println(chemical.CAS);
//			System.out.println(jr.fileName+"\t"+chemical.name);
			
			this.translate(jr, chemical);
			chemicals.add(chemical);
			return chemicals;
		}
		
		String [] casNumbers=jr.CAS.split(",");
		
		
//		JsonArray jaCAS = jo.get(strCAS).getAsJsonArray();
//
		for (int i = 0; i < casNumbers.length; i++) {
			Chemical chemical = new Chemical();
			chemical.CAS = casNumbers[i];
			chemical.name = jr.chemicalName;
			
//			if (!chemical.CAS.contentEquals("109-89-7")) continue;//9/23/20

//			if (chemical.CAS.equals("")) {
//				chemical.CAS = chemical.name;
//				// System.out.println(chemical.CAS);
//			}

//			System.out.println("*"+chemical.CAS);

			this.translate(jr, chemical);

//			String ID=getUniqueID(chemical.CAS);
			
			if (chemical != null) {
//				chemical.writeToFile(ID,this.jsonFolder);
				chemicals.add(chemical);
			} else {
				System.out.println(jr.chemicalName + "\tnull");
			}
		}
		return chemicals;
		
	}

	private void createRecordsForNoCodeSituation(Chemical chemical,JapanRecord jr,HazardRecord hr) {


		String classification=hr.classifications.get(0);

		//				System.out.println(jr.CAS);

		if (dictHazardNameToScoreName.get(hr.hazardClass)==null || dictHazardNameToScoreName.get(hr.hazardClass).equals("omit") ) {
			return;
		}

		if (classification.equals("-")) {
			return;
		}

		classification=classification.replace("(Unclassified)","Not classified");
		

		String baseURL="https://www.nite.go.jp/chem/english/ghs/";		
		String filename=new File(jr.fileName).getName();
		String url=baseURL+filename;

		
		for (String scoreName:dictHazardNameToScoreName.get(hr.hazardClass)) {
			
			if (scoreName.equalsIgnoreCase("omit")) continue;
			
			String strScore=null;

			String note2=null;

			if (scoreName.equals(Chemical.strCarcinogenicity)) {
				//					System.out.println(classification);

				if (classification.contains("Not classified")) {
					if (!JapanRecord.isClassifiable(chemical.CAS, hr.rationale)) {
						
//						System.out.println(hr.rationale+"\t"+url);
						
						
						//Need to fix it and change it to not classifiable:							
						strScore=ScoreRecord.scoreNA;
						String newClassification = "Classification not possible";
						classification=newClassification;
						hr.rationale=hr.rationale.replace("Not classified", newClassification);
						hr.rationale+="<br><br>Note: EPA has corrected Category \"Not classified\" to \"Classification not possible\" based on the cancer group cited by Japan";
						//							System.out.println(jr.CAS+"\t"+hr.rationale);
					}
				} else {
					//						 System.out.println(chemical.CAS+"\t"+classification);
				}
			}

			if (classification.contains("Not applicable") || classification.contains("Classification not possible")) {
				strScore=ScoreRecord.scoreNA;
			} else if (classification.contains("Not classified") || classification.contains("(Unclassified)")) {
				strScore=ScoreRecord.scoreL;
			} else if (classification.contentEquals("*")) {
				continue;			
			} else  {
				strScore=ScoreRecord.scoreNA;
//				System.out.println("here NA:"+scoreName+"\t"+url);
			}

			String toxRoute = JapanRecord.getToxRoute(hr, scoreName);
			Score score=chemical.getScore(scoreName);
			
//			System.out.println(scoreName);
			
			
			this.createRecord(chemical.CAS,chemical.name,score, classification, "-", "-", toxRoute, strScore, hr.rationale,note2,url);
			
		}
	
	}
	
	private void translate(JapanRecord jr, Chemical chemical) {
		//		System.out.println("here");
		for (HazardRecord hr:jr.records) {
			if (hr.hazardCodes.size()==0) {
				createRecordsForNoCodeSituation(chemical,jr,hr);
			} else {
				createRecordsFromCodes(jr, chemical, hr);	
			}
		}
	}

	private void createRecordsFromCodes(JapanRecord jr, Chemical chemical, HazardRecord hr) {
		if (hr.classifications.size()!=hr.hazardCodes.size()) {
			fixClassifications(hr);
		}
		
		
		String baseURL="https://www.nite.go.jp/chem/english/ghs/";		
		String filename=new File(jr.fileName).getName();
		String url=baseURL+filename;

		
		for (int i=0;i<hr.hazardCodes.size();i++) {
			

			if (hr.hazardClass.equals("Gases under pressure")) continue;
			if (hr.hazardClass.equals("Explosives")) continue;
			if (hr.hazardClass.equals("Organic peroxides")) continue;
			if (hr.hazardClass.equals("Self-reactive substances and mixtures")) continue;

//			if (i==hr.classifications.size()) {//for H335& H336 and H360 and H362 they combine the classification
//				hr.classifications.add(hr.classifications.get(i-1));
//				//					System.out.println(chemical.CAS+"\t"+jr.fileName+"\t"+hr.hazardClass+"\t"+hr.classifications.get(i-1)+"\t"+hr.hazardCodes.get(i));
//				//					continue;
//			}
			

			String hazardCode=hr.hazardCodes.get(i);
			String hazardStatement=hr.hazardStatements.get(i);

			if (!hazardCode.contains("H")) {
				System.out.println(jr.CAS+"\tno hazard code\t"+hr.hazardClass);
				continue;
			}

			
			String classification=getClassification(jr.CAS,hr,hazardCode,i);
			
			if (classification==null) {
				continue;
			}
			

//			if (jr.CAS.equals("107-02-8")) {
//				System.out.println(hazardCode+"\t"+classification);
//			}

			//				String classification=hr.classifications.get(i);

			List<String> listScore = (List<String>) this.dictCodeToScoreName.get(hazardCode);

			for(String scoreName:listScore) {
				if (scoreName.equals("Omit")) continue;
				

				
//				if (scoreName.equalsIgnoreCase(Chemical.strNeurotoxicity_Single_Exposure)) {
//					if (jr.CAS.equals("107-02-8"))
//						System.out.println("here");
//				}
				

				Score score=chemical.getScore(scoreName);
				String strScore=this.dictCodeToScoreValue.get(hazardCode);
				String toxRoute = JapanRecord.getToxRoute(hr, scoreName);


				
				if(!haveKeyword(jr.CAS, classification, scoreName)) continue;

				//					System.out.println(scoreName+"\t"+classification+"\t"+hazardCode);

				
//				if (jr.CAS.equals("107-02-8")) {
//					System.out.println(scoreName+"\t"+strScore);
//				}
				
				this.createRecord(chemical.CAS,chemical.name,score, classification, hazardStatement, hazardCode, toxRoute, strScore, hr.rationale,null,url);
				//				System.out.println(hr.hazardCode+"\t"+hr.hazardClass+"\t"+scoreName);
			}
		}// end loop over hazard codes
	}

	private void fixClassifications(HazardRecord hr) {
		for(int j=0;j<hr.classifications.size();j++) {
			
			if (hr.classifications.get(j).equals("Category 3 (respiratory tract irritation, narcotic effects)")) {
				hr.classifications.remove(j);
				hr.classifications.add("Category 3 (respiratory tract irritation)");
				hr.classifications.add("Category 3 (narcotic effects)");
			}

			if (hr.classifications.get(j).equals("Category 3 (narcotic effects, respiratory tract irritation)")) {
				hr.classifications.remove(j);
				hr.classifications.add("Category 3 (narcotic effects)");
				hr.classifications.add("Category 3 (respiratory tract irritation)");
			}
			
			
			
			if (hr.classifications.get(j).equals("Category 3 (respiratory tract irritation)")) {
				if (hr.hazardCodes.contains("H335") && hr.hazardCodes.contains("H336")) {
					if (!hr.classifications.contains("Category 3 (narcotic effects)")) {
//							System.out.println(jr.CAS+"\there");
						hr.classifications.add("Category 3 (narcotic effects)");	
					}
				}
			}

			if (hr.classifications.get(j).equals("Category 3 (narcotic effects)")) {
				if (hr.hazardCodes.contains("H335") && hr.hazardCodes.contains("H336")) {
					if (!hr.classifications.contains("Category 3 (respiratory tract irritation)")) {
//							System.out.println(jr.CAS+"\there");
						hr.classifications.add("Category 3 (respiratory tract irritation)");	
					}
				}
			}
			
			
			
		}
	}

	
	
	private String getClassification(String CAS,HazardRecord hr,String hazardCode,int index) {
		String classification=null;
		if (hr.classifications.size()==hr.hazardCodes.size()) {
			classification=hr.classifications.get(index);
		} else if (CAS.equals("25167-80-0")) {//handle weird o,m,p case
			if (hazardCode.equals("H370")) {
				classification="Category 1 (central nervous system)";
			} else if (hazardCode.equals("H335")) {
				classification="Category 3 (respiratory tract irritation)";
			}
		} else if (hr.classifications.size()==1) {
			classification=hr.classifications.get(0);
		} else if (hr.hazardCodes.size()==1 && hr.classifications.size()>1) {
			classification="";
			for(int j=0;j<hr.classifications.size();j++) {
				classification+=hr.classifications.get(j);
				if (j<hr.classifications.size()-1) classification+=";";
			}
		} else {
			for(int j=0;j<hr.classifications.size();j++) {
				System.out.print(hr.hazardClass+"\t"+hr.classifications.get(j));
				if (j<hr.hazardCodes.size()) System.out.print("\t"+hr.hazardCodes.get(j));
				System.out.print("\n");
			}
			
			System.out.println(CAS+"\t"+hr.hazardCodes.size()+"\t"+hr.classifications.size());
			
		}
		return classification;
		
	}

	private boolean haveKeyword(String CAS,String classification, String scoreName) {
		

		
		if (scoreName.contains("Neurotoxicity")) {
			for (String keyword:neuro_keywords) {
				if (classification.toLowerCase().contains(keyword)) {
					return true;
				}
			}
		} else if (scoreName.contains("Systemic Toxicity")) {
			for (String keyword:systemic_keywords) {
				if (classification.toLowerCase().contains(keyword)) {
					return true;
				}
			}
		} else {//doesnt need keyword
			return true;
		}
		
		return false;
	}

	
	
	
	private void createRecord(String CAS,String name, Score score, String hazardClassification, String hazardStatement,String toxCode, String toxRoute,
			String strScore, String note,String note2,String url) {

		String rationale="";
		
		
		if (toxCode.equals("-"))
			rationale = "Score of " + strScore + " was assigned based on a classification of \"" + hazardClassification+"\"";
		else
			rationale = "Score of " + strScore + " was assigned based on a hazard code of " + toxCode;
			
		if (strScore==null) {
//			System.out.println(score.hazard_name+"\t"+url);
			return;
		}
		
		if (strScore.contentEquals(ScoreRecord.scoreNA) && hazardClassification.contains("Category 1")) {
			rationale = "Score of " + ScoreRecord.scoreNA + " was assigned since Japan did not assign a hazard code";
			System.out.println(score.hazard_name+"\t"+rationale+"\t"+url);
		}
				
		ScoreRecord sr = new ScoreRecord(CAS,name,score.hazard_name,sourceName, strScore, hazardClassification, toxCode, 
				hazardStatement,rationale, toxRoute, note, note2,url);		

		score.records.add(sr);


	}	

	

	
	

	
	
	
//	private void getHazardDataNew(String filename,JapanRecord jr, Element table) {
//		Elements trs = table.select("tr");
//		Element header=trs.remove(0);//header- should probably determine column numbers for each column name but for now hard code the column numbers
//
//		
//		for (Element h:header.getAllElements()) {
//			System.out.println(h.text());
//			
//		}
//		
//		
//		int colHazardClass=1;
//		int colClassification=2;
//		int colSignalWord=3;
//		int colHazardStatement=4;
//		
//		
//		for (Element tr:trs) {
//			Elements tds = tr.select("td");
//			HazardRecord hr=new HazardRecord();
//			
//			hr.hazardClass=tds.get(colHazardClass).text().replace("（", "(").replace("）", ")");
//			
//			String td2=tds.get(colClassification).text();
//			
//			if (td2.contains(", Category")) {
//				td2=td2.replace(", Category", "\nCategory");
////				System.out.println(td2);
//			}
//
//			String [] classifications=td2.split("\n");
//			for (String classification:classifications) {
//				hr.classification.add(classification);	
//			}
//			
//			
//			String symbol_signalWord=tds.get(colSignalWord).text();
//			
////			System.out.println(symbol_signalWord);
//			
////			hr.symbol="";//TODO
//			hr.signalWord=symbol_signalWord;
//			
//			String td4=tds.get(colHazardStatement).text();
////						System.out.println(td5);
//			
//			
////			System.out.println(td4);
//			
//			
//			parseHazardStatementAndCode(hr, td4);
//			
//			hr.precautionaryStatement=tds.get(5).text();
//			hr.rationale=tds.get(6).text().trim();
//			
//			if (hr.rationale.equals("-") || hr.rationale.equals(" -")) {	
//				hr.rationale="&#45;";
////				System.out.println(hr.rationale);
//			} else if (hr.rationale.length()<4) {
//				
//				if (hr.rationale.equals("  -")) {
//					hr.rationale="&#45;";
//				} else {
//					System.out.println(hr.rationale);	
//				}
//				
//			}
//			
//			jr.records.add(hr);
//		}
//	}

	

	

	

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParseJapanWebPagesHazardCode p=new ParseJapanWebPagesHazardCode();
		
//		p.createRecords();
//		p.goThroughOriginalRecords();

		//Download the webpages:
//		JapanRecord.downloadWebpages();
		
		//Create the files from the webpages:
		p.createFiles();
		

	}

}



