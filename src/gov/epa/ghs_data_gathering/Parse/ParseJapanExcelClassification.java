package gov.epa.ghs_data_gathering.Parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;

import com.google.gson.JsonElement;

/**
 * This class takes data from excel spreadsheets and creates chemical objects
 * using the "classification" to determine the scores. 
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
public class ParseJapanExcelClassification extends Parse {
//	String file_path;
	JsonObject japanData;


	Hashtable<String, String> htHazards =createHazardHashtable();

	private static final String strNumber="hazard_id_number";
	private static final String strHazardClass="hazard_class";
	private static final String strClassification="classification";
	private static final String strSymbol="symbol";
	private static final String strSignalWord="signal_word";
	private static final String strHazardStatement="hazard_statement";
	private static final String strRationale="rationale";
	
    String strName="chemical_name";
    String strCAS="cas_number";
    
	String[] hazardFields = { strNumber, strHazardClass, strClassification, strSymbol, strSignalWord,
			strHazardStatement, strRationale };

	boolean debug=false;
	
	
	/**
	 * Japan incorrectly assigned several ratings of "Not Classifiable" as "Not
	 * classified" which means not toxic
	 * 
	 * @param CAS
	 * @param classification
	 * @param rationale
	 * @return
	 */
	private boolean isClassifiable(String CAS, String classification, String rationale) {

		String r = rationale.toLowerCase().trim();

		String[] strCase1 = { "group 3", "classified into 3", "category 3", "three of iarc", "class 3",
				"classificationed into \"3", "category of iarc (2001): 3" };
		boolean case1 = false;
		for (int i = 0; i < strCase1.length; i++) {
			if (r.indexOf(strCase1[i]) > -1 && r.indexOf("iarc") > -1) {
				case1 = true;
				break;
			}
		}

		boolean case2 = r.indexOf("a4") > -1 && r.indexOf("acgih") > -1;

		boolean case3 = false;

		String[] strCase3 = { "according to epa with i", "classified into i", "rated as d", "rated d", "category d",
				"group d", "classified into d", "rated as class d" };

		for (int i = 0; i < strCase3.length; i++) {
			if (r.indexOf(strCase3[i]) > -1 && r.indexOf("epa") > -1) {
				case3 = true;
				break;
			}
		}

		if (case1 || case2 || case3) {
			return false;
		} else {
			// System.out.println(CAS + "\t" + r);
			return true;
		}

	}

//	private void translate(JsonObject jo) {
//		String CAS = jo.get("cas_number").getAsJsonArray().get(0).getAsString();
//
//		JsonObject translationCriteria = japanData.get("translationCriteria").getAsJsonObject();
//		JsonObject translationPatterns = japanData.get("translationPatterns").getAsJsonObject();
//
//		JsonObject hazards = jo.get("hazards").getAsJsonObject();
//
//		JsonObject translatedData = new JsonObject();
//		jo.add("translated_dataTMM", translatedData);
//
//		String[] keys = { "AA", "AT", "B", "C", "CA", "D", "E", "F", "IrE", "IrS", "M", "N_r", "N_s", "P", "R", "Rx",
//				"ST_r", "ST_s", "SnR", "SnS" };
//
//		for (int ii = 0; ii < keys.length; ii++) {// loop through string array to go through in alphabetical order
//			String key = keys[ii];
//			JsonObject criterion = translationCriteria.get(key).getAsJsonObject();
//
//			boolean requires_parsing = criterion.get("requires_parsing").getAsBoolean();
//			JsonArray nativeClassifications = criterion.get("native_classification").getAsJsonArray();
//			JsonArray patterns = criterion.get("pattern").getAsJsonArray();
//
//			// System.out.println(key+"\t"+requires_parsing);
//
//			String keywordsName = null;
//			JsonArray keywords = null;
//
//			if (requires_parsing) {
//				keywordsName = criterion.get("keywords").getAsString();
//				// System.out.println(keywordsName);
//				keywords = japanData.get(keywordsName).getAsJsonArray();
//			}
//
//			int maxRating = 0;
//
//			for (int i = 0; i < nativeClassifications.size(); i++) {
//
//				String nativeClassification = nativeClassifications.get(i).getAsString();
//				String pattern = patterns.get(i).getAsString();
//
//				JsonObject hazard = hazards.get(nativeClassification).getAsJsonObject();
//				String classifications = hazard.get(strClassification).getAsString().trim();
//
////				if (classifications.contains("\n")) {
////					System.out.println(CAS+"\t"+classifications);
////				}
//				
//				// Sometimes a classification has different rating for different keywords- each
//				// on it's own line:
//				String[] classificationArray = classifications.split("\n");
//
//				for (int j = 0; j < classificationArray.length; j++) {
//
//					String classification = classificationArray[j];
//					// System.out.println(CAS+"\t"+j+"\t"+classification);
//
//					if (requires_parsing) {
//						boolean haveKeyword = false;
//
//						// Loop through keywords to see if a keyword shows up in classification:
//						for (int k = 0; k < keywords.size(); k++) {
//							if (classification.indexOf(keywords.get(k).getAsString()) > -1) {
//								// System.out.println(key+"\t"+keywords.get(k)+"\t"+classification);
//								haveKeyword = true;
//								break;
//							}
//						}
//
//						if (!haveKeyword)
//							continue;
//					}
//					JsonObject translationPattern = translationPatterns.get(pattern).getAsJsonObject();
//
//					// Loop through translation pattern to see if a rating shows up in
//					// classification:
//					for (Entry<String, JsonElement> valueEntry2 : translationPattern.entrySet()) {
//						String key2 = valueEntry2.getKey();
//
//						if (classification.indexOf(key2) > -1) {
//							String rating = translationPattern.get(key2).getAsString();
//							int irating = Integer.parseInt(rating);
//							if (irating > maxRating)
//								maxRating = irating;
//							// System.out.println(CAS+"\t"+key+"\t"+i+"\t"+nativeClassification+"\t"+pattern+"\t"+classification+"\t"+rating);
//							break;
//						}
//					}
//
//				} // end loop over classifications
//
//			} // end loop over native classifications
//
//			// System.out.println(key+"\t"+maxRating);
//			translatedData.addProperty(key, maxRating);
//		} // end loop over keys
//
//		// compareTranslations(jo, CAS, translatedData, keys);
//
//	}

	/**
	 * This method translates scores and adds records to chemical at the same time
	 * 
	 * @param jo
	 * @param chemical
	 */
	private void translate2(JsonObject jo, Chemical chemical) {

		JsonObject translationCriteria = japanData.get("translationCriteria").getAsJsonObject();
		JsonObject translationPatterns = japanData.get("translationPatterns").getAsJsonObject();

		JsonObject hazards = jo.get("hazards").getAsJsonObject();

		for (Entry<String, JsonElement> valueEndpoints : translationCriteria.entrySet()) {

			String key = valueEndpoints.getKey();

			// System.out.println(key);

			JsonObject criterion = translationCriteria.get(key).getAsJsonObject();

			boolean requires_parsing = criterion.get("requires_parsing").getAsBoolean();
			JsonArray nativeClassifications = criterion.get("native_classification").getAsJsonArray();
			JsonArray patterns = criterion.get("pattern").getAsJsonArray();

			// System.out.println(key+"\t"+requires_parsing);

			String keywordsName = null;
			JsonArray keywords = null;

			if (requires_parsing) {
				keywordsName = criterion.get("keywords").getAsString();
				// System.out.println(keywordsName);
				keywords = japanData.get(keywordsName).getAsJsonArray();
			}

			// System.out.println(nativeClassifications.size());

			for (int i = 0; i < nativeClassifications.size(); i++) {

				int maxRating = 0;// have separate rating for each native classification- where as wehage took the
									// max over all the native classifications

				String nativeClassification = nativeClassifications.get(i).getAsString();
				String pattern = patterns.get(i).getAsString();

				JsonObject hazard = hazards.get(nativeClassification).getAsJsonObject();
				String classifications = hazard.get(strClassification).getAsString().trim();

				String route = "";

				if (nativeClassification.indexOf("acute_toxicity_") > -1) {
					route = nativeClassification.replace("acute_toxicity_", "");
				}

				// Sometimes a classification has different rating for different keywords- each
				// on it's own line:
				String[] classificationArray = classifications.split("\n");

				
//				if (classifications.contains("\n")) {
//					System.out.println(chemical.CAS+"\t"+classifications);	
//				}
				
				boolean haveKeyword = false;
				
				for (int j = 0; j < classificationArray.length; j++) {

					String classification = classificationArray[j];
					// System.out.println(CAS+"\t"+j+"\t"+classification);

					if (requires_parsing) {
						// Loop through keywords to see if a keyword shows up in classification:
						for (int k = 0; k < keywords.size(); k++) {
							if (classification.toLowerCase().contains(keywords.get(k).getAsString().toLowerCase())) {
//								 System.out.println(chemical.CAS+"\t"+key+"\t"+keywords.get(k)+"\t"+classification);
								haveKeyword = true;
								break;
							}
						}

						if (!haveKeyword)
							continue;
					}
					JsonObject translationPattern = translationPatterns.get(pattern).getAsJsonObject();

					// Loop through translation pattern to see if a rating shows up in
					// classification:
					for (Entry<String, JsonElement> valueEntry2 : translationPattern.entrySet()) {
						String key2 = valueEntry2.getKey();

						if (classification.indexOf(key2) > -1) {
							String rating = translationPattern.get(key2).getAsString();
							int irating = Integer.parseInt(rating);
							if (irating > maxRating)
								maxRating = irating;
							// System.out.println(CAS+"\t"+key+"\t"+i+"\t"+nativeClassification+"\t"+pattern+"\t"+classification+"\t"+rating);
							break;
						}
					}

				} // end loop over classifications

				if (requires_parsing && !haveKeyword) continue;
				
				if (chemical.getScore(key) == null)
					continue;// not implemented in Chemical yet

				if (key.equals(Chemical.strCarcinogenicity)) {
					String classification = hazard.get("classification").getAsString().trim();
					String rationale = hazard.get("rationale").getAsString().trim();

					if (classification.equals("Not classified")) {
						if (!isClassifiable(chemical.CAS, classification, rationale)) {
							maxRating = 0;// score = scoreNA
							String newClassification = "Classification not possible";
							hazard.addProperty("classification", newClassification);
							hazard.addProperty("rationale", rationale.replace("Not classified", newClassification));
						}
					} else {
						// System.out.println(chemical.CAS+"\t"+classification);
					}

				}

				// Create ScoreRecord
				Score score = chemical.getScore(key);
				ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
				sr.score = getScoreFromTranslatedData(maxRating);
				
				String filename=jo.get("file_path").getAsString();
				assignValues(hazard, sr, route,filename);
				score.records.add(sr);
				
				if (debug) System.out.println(chemical.CAS + "\t" + key + "\t" + maxRating + "\t" + sr.score);

			} // end loop over native classifications

		} // end loop over endpoints

		if (debug) System.out.println("");

	}

//	private void translate(NITERecordWehage n) {
//		String CAS = n.cas_number.get(0);
//
//		JsonObject translationCriteria = japanData.get("translationCriteria").getAsJsonObject();
//		JsonObject translationPatterns = japanData.get("translationPatterns").getAsJsonObject();
//
//		try {
//
//			String[] keys = { "AA", "AT", "B", "C", "CA", "D", "E", "F", "IrE", "IrS", "M", "N_r", "N_s", "P", "R",
//					"Rx", "ST_r", "ST_s", "SnR", "SnS" };
//
//			for (int ii = 0; ii < keys.length; ii++) {// loop through string array to go through in alphabetical order
//				String key = keys[ii];
//				JsonObject criterion = translationCriteria.get(key).getAsJsonObject();
//
//				boolean requires_parsing = criterion.get("requires_parsing").getAsBoolean();
//				JsonArray nativeClassifications = criterion.get("native_classification").getAsJsonArray();
//				JsonArray patterns = criterion.get("pattern").getAsJsonArray();
//
//				// System.out.println(key+"\t"+requires_parsing);
//
//				String keywordsName = null;
//				JsonArray keywords = null;
//
//				if (requires_parsing) {
//					keywordsName = criterion.get("keywords").getAsString();
//					// System.out.println(keywordsName);
//					keywords = japanData.get(keywordsName).getAsJsonArray();
//				}
//
//				int maxRating = 0;
//
//				for (int i = 0; i < nativeClassifications.size(); i++) {
//
//					String nativeClassification = nativeClassifications.get(i).getAsString();
//					String pattern = patterns.get(i).getAsString();
//
//					hazards hazards = n.hazards;
//					Field field = hazards.getClass().getDeclaredField(nativeClassification);
//					field.setAccessible(true);
//					hazard hazard = (hazard) field.get(hazards);
//
//					String classifications = hazard.classification.trim();
//					// System.out.println(classifications);
//
//					// Sometimes a classification has different rating for different keywords- each
//					// on it's own line:
//					String[] classificationArray = classifications.split("\n");
//
//					for (int j = 0; j < classificationArray.length; j++) {
//
//						String classification = classificationArray[j];
//						// System.out.println(CAS+"\t"+j+"\t"+classification);
//
//						if (requires_parsing) {
//							boolean haveKeyword = false;
//
//							// Loop through keywords to see if a keyword shows up in classification:
//							for (int k = 0; k < keywords.size(); k++) {
//								if (classification.indexOf(keywords.get(k).getAsString()) > -1) {
//									// System.out.println(key+"\t"+keywords.get(k)+"\t"+classification);
//									haveKeyword = true;
//									break;
//								}
//							}
//
//							if (!haveKeyword)
//								continue;
//						}
//						JsonObject translationPattern = translationPatterns.get(pattern).getAsJsonObject();
//
//						// Loop through translation pattern to see if a rating shows up in
//						// classification:
//						for (Entry<String, JsonElement> valueEntry2 : translationPattern.entrySet()) {
//							String key2 = valueEntry2.getKey();
//
//							if (classification.indexOf(key2) > -1) {
//								String rating = translationPattern.get(key2).getAsString();
//								int irating = Integer.parseInt(rating);
//								if (irating > maxRating)
//									maxRating = irating;
//								// System.out.println(CAS+"\t"+key+"\t"+i+"\t"+nativeClassification+"\t"+pattern+"\t"+classification+"\t"+rating);
//								break;
//							}
//						}
//
//					}
//
//				} // end loop over classifications
//
//				// System.out.println(key+"\t"+maxRating);
//				// translatedData.addProperty(key, maxRating);
//			} // end loop over keys
//
//			// compareTranslations(jo, CAS, translatedData, keys);
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//	}

//	private void compareTranslations(JsonObject jo, String CAS, JsonObject translatedData, String[] keys) {
//		JsonObject originalTranslatedData = jo.get("translated_data").getAsJsonObject();
//
//		boolean match = true;
//
//		for (int i = 0; i < keys.length; i++) {
//			String oldVal = originalTranslatedData.get(keys[i]).getAsString();
//			String newVal = translatedData.get(keys[i]).getAsString();
//
//			if (!oldVal.equals(newVal)) {
//				match = false;
//				break;
//			}
//			// System.out.println(keys[i]+"\t"+oldVal+"\t"+newVal);
//		}
//		if (!match) {
//			for (int i = 0; i < keys.length; i++) {
//				String oldVal = originalTranslatedData.get(keys[i]).getAsString();
//				String newVal = translatedData.get(keys[i]).getAsString();
//
//				if (!oldVal.equals(newVal)) {
//					System.out.println(CAS + "\t" + keys[i] + "\t" + oldVal + "\t" + newVal);
//					break;
//				}
//				//
//			}
//		}
//
//		// System.out.println(CAS+"\t"+match);
//
//		// System.out.println(gson.toJson(translatedData));
//		// System.out.println(gson.toJson(jo.get("translated_data")));
//
//	}

	public static String removeBadChars(String name) {

		String[] bad = { "\\", ":", "/", "{", "}", "~", "#", "%", "&", "*", "{", "}", "<", ">", "?", "+", "|" };

		String newName = name;
		for (int i = 0; i < bad.length; i++) {
			newName = newName.replace(bad[i], "_");
		}
		return newName;

	}

//	String getUniqueID(String ID) {
//		
//		int count=1;
//		File file=new File(this.jsonFolder+"/"+ID+".json");
//		if (file.exists()) {
//			while (true) {
//				count++;
//				file=new File(this.jsonFolder+"/"+ID+"_"+count+".json");
//				if (!file.exists()) {
//					ID=ID+"_"+count;
//					System.out.println(ID+"\tduplicate CAS (revision file)");
//					break;
//				}
//			}
//		}
//		
//		return ID;
//	}
	
	private Chemicals createChemicals(JsonObject jo) {

		Chemicals chemicals=new Chemicals();
		
		
		if (jo.get(strCAS)==null) {
			Chemical chemical = new Chemical();
			chemical.name = jo.get(strName).getAsString();
			this.translate2(jo, chemical);
			chemicals.add(chemical);
			return chemicals;
		}
		
		JsonArray jaCAS = jo.get(strCAS).getAsJsonArray();

		for (int i = 0; i < jaCAS.size(); i++) {
			Chemical chemical = new Chemical();
			chemical.CAS = jaCAS.get(i).getAsString();
			chemical.name = jo.get(strName).getAsString();

			if (chemical.CAS.equals("")) {
				chemical.CAS = removeBadChars(chemical.name);
				// System.out.println(chemical.CAS);
			}

			if (chemical.CAS.length() > 100)
				continue;// dont write super long ones out

			this.translate2(jo, chemical);

//			String ID=getUniqueID(chemical.CAS);
			
			if (chemical != null) {
//				chemical.writeToFile(ID,this.jsonFolder);
				chemicals.add(chemical);
			} else {
				System.out.println(jo.get(strName).getAsString() + "\tnull");
			}
		}
		return chemicals;
		
	}

	private static void assignValues(JsonObject hazard, ScoreRecord sr, String route,String filename) {
		sr.source = ScoreRecord.sourceJapan;
		
		sr.category = hazard.get(strClassification).getAsString().trim();
		
		sr.hazard_statement = hazard.get(strHazardStatement).getAsString().trim();
		sr.route = route;
		
		sr.rationale = "N/A";

		if (sr.category.indexOf("Category") > -1 || sr.category.indexOf("Not classified") > -1) {
			sr.rationale = "Score was assigned based on a category of \"" + sr.category + "\".";
		}

		// sr.rationale+=hazard.rationale.trim();
		sr.note = hazard.get(strRationale).getAsString().trim();

		if (sr.category.equals("")) sr.category=null;
		if (sr.route.equals("")) sr.route=null;
		
		sr.note2=filename;

		// System.out.println(sr.rationale);
	}

	private String getScoreFromTranslatedData(int value) {

		// TODO- do we need to change this depending on which toxicity value it is? i.e.
		// do I trust wehage's translation patterns? i.e. to match DfE like I did in
		// ChemHAT.java?

		if (value == 5) {
			return ScoreRecord.scoreVH;
		} else if (value == 4) {
			return ScoreRecord.scoreH;
		} else if (value == 3) {
			return ScoreRecord.scoreM;
		} else if (value == 2) {
			return ScoreRecord.scoreL;
		} else {
			return ScoreRecord.scoreNA;
		}
	}
	
	private Hashtable<String, String> createHazardHashtable() {

		Hashtable<String, String> ht = new Hashtable<String, String>();

		//Physical hazards			
		ht.put("Explosives", "explosives");
		ht.put("Flammable gases (including chemically unstable gases)","flammable_gases");
		ht.put("Aerosols", "flammable_aerosols");
		ht.put("Oxidizing gases", "oxidizing_gases");
		ht.put("Gases under pressure", "gases_under_pressure");
		ht.put("Flammable liquids", "flammable_liquids");
		ht.put("Flammable solids", "flammable_solids");
		ht.put("Self-reactive substances and mixtures", "self_reactive_substances");
		ht.put("Pyrophoric liquids", "pyrophoric_liquids");
		ht.put("Pyrophoric solids", "pyrophoric_solids");
		ht.put("Self-heating substances and mixtures", "self_heating_substances");
		ht.put("Substances and mixtures which, in contact with water, emit flammable gases","substances_mixtures_emit_flammable_gas_in_contact_with_water");
		ht.put("Oxidizing liquids", "oxidizing_liquids");
		ht.put("Oxidizing solids", "oxidizing_solids");
		ht.put("Organic peroxides", "organic_peroxides");
		ht.put("Corrosive to metals", "corrosive_to_metals");
		ht.put("Flammable solid", "Flammable solid");
		ht.put("Hazardous to the ozone layer", "Hazardous to the ozone layer");

		//Health hazards	
		ht.put("Acute toxicity (Oral)", "acute_toxicity_oral");
		ht.put("Acute toxicity (Dermal)", "acute_toxicity_dermal");
		ht.put("Acute toxicity (Inhalation: Gases)", "acute_toxicity_inhalation_gas");
		ht.put("Acute toxicity (Inhalation: Vapours)", "acute_toxicity_inhalation_vapor");
		ht.put("Acute toxicity (Inhalation: Dusts and mists)", "acute_toxicity_inhalation_dust");
		ht.put("Skin corrosion/irritation", "skin_corrosion_irritation");
		ht.put("Serious eye damage/eye irritation", "serious_eye_damage_irritation");
		ht.put("Respiratory sensitization", "respiratory_sensitizer");
		ht.put("Skin sensitization", "skin_sensitizer");
		ht.put("Germ cell mutagenicity", "germ_cell_mutagenicity");
		ht.put("Carcinogenicity", "carcinogenicity");
		ht.put("Reproductive toxicity", "reproductive_toxicity");
		ht.put("Specific target organ toxicity - Single exposure", "systemic_toxicity_single_exposure");
		ht.put("Specific target organ toxicity - Repeated exposure", "systemic_toxicity_repeat_exposure");
		ht.put("Aspiration hazard", "aspiration_hazard");
		
		//Environmental hazards	
		ht.put("Hazardous to the aquatic environment (Acute)", "acute_aquatic_toxicity");
		ht.put("Hazardous to the aquatic environment (Long-term)", "chronic_aquatic_toxicity"); //TODO chronic or acute
		
		return ht;
	}
	
	public String fixSpecialCharacters(String str) {
//		// assigning pattern to special characters
//		Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
//		Matcher m = p.matcher(str);
//		boolean b = m.find();
//
//		// if contains special characters enters if statement
//		if (b) {
//			// replacing non printable characters
//			str = str.replaceFirst("[^\\p{Print}]", "(");
//			str = str.replaceAll("[^\\p{Print}]", ")");
//		}
		
		str=str.replace("（", "(").replace("）", ")");
				
		
		return str;
	}
	
	
	private JsonObject importData(String filename, Sheet sheet, ArrayList<String> hazardsNotYetDefined) {

		DataFormatter df = new DataFormatter();

		String date_imported = new SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date());

		JsonObject jo = new JsonObject();
		jo.addProperty("Country", "Japan");
		jo.addProperty("date_imported", date_imported);
		jo.addProperty("file_path", filename);

		JsonObject hazards = new JsonObject();

		try {


			for (int rowNumber = 0; rowNumber <= 46; rowNumber++) {

				Row row = sheet.getRow(rowNumber);

				if (row == null)
					continue;

				String cell0 = df.formatCellValue(row.getCell(0));
				String cell2 = df.formatCellValue(row.getCell(2));
				
				cell2=fixSpecialCharacters(cell2);

				if (cell2.equals(""))
					continue;

				if (cell0.contains("ID")) {
					jo.addProperty("ID", cell2);
				} else if (cell0.contains("CAS")) {
					
					String[] CASListString = cell2.split(",");
					JsonArray CASList = new JsonArray();

					for (int i = 0; i < CASListString.length; i++) {

						CASList.add(CASListString[i]);
					}
					jo.add(strCAS, CASList);

				} else if (cell0.contains("Chemical Name")) {
					jo.addProperty(strName, cell2);
				}

				if (cell0.length() > 2)
					continue;// dont create a hazard since one of id field rows

				// System.out.println(cell0.length());
				// System.out.println(cell0+"\t"+cell2);

				JsonObject hazard = new JsonObject();

				String hazardClass = htHazards.get(cell2);
				// System.out.println(cell2);

				for (int i = 0; i < hazardFields.length; i++) {
					if (hazardFields[i].equals(strHazardClass))
						continue;// dont need to add since already have as name of hazard
					
					
					hazard.addProperty(hazardFields[i], fixSpecialCharacters(df.formatCellValue(row.getCell(i + 1))));
				}

				hazards.add(hazardClass, hazard);

				// System.out.println("***"+cell2);

			}

			jo.add("hazards", hazards);
			// System.out.println(gson.toJson(jo));

			return jo;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Old version that uses json files
	 */
//	private void mergeRevisions() {
//		
//		try {
//		
//
//			File folder=new File(this.jsonFolder);
//			
//			File [] files=folder.listFiles();
//			
//			ArrayList<String> CASList=new ArrayList<>();
//			
//			
//			for (File file:files) {
//				if (file.getName().length()>15) continue;
//				
//				if (file.getName().contains("_")) {
//					String CAS=file.getName().substring(0, file.getName().indexOf("_"));
//					if (!CASList.contains(CAS)) CASList.add(CAS);
//				}
//			}
//
////			for (String CAS:CASList) {
////				System.out.println(CAS);
////			}
//			
////			String CAS="96-45-7";
//			
//			
//			for (String CAS : CASList) {
//				
////				if (!CAS.equals("100-44-7")) continue;
//				
//				Chemicals chemicals=new Chemicals();
//
//				for (int i=1;i<=3;i++) {
//					
//					File filei=null;
//					
//					if (i==1 ) {
//						filei=new File(this.jsonFolder+"/"+CAS+".json");
//					} else {
//						filei=new File(this.jsonFolder+"/"+CAS+"_"+i+".json");
//					}
//					
//					if (!filei.exists()) continue;
//
////					JsonObject jo = gson.fromJson(new FileReader(filei), JsonObject.class);
//						// System.out.println(gson.toJson(jo));
//
//					Chemical chemical = Chemical.loadFromJSON(filei);
//					if (!chemical.CAS.equals(CAS))	continue;
//					
//					chemicals.add(chemical);
//					// TODO check if CAS numbers in the file match
//				}
//				
//				Chemical chemical0=chemicals.get(0);
////				System.out.println(gson.toJson(chemical0));
//				for (int i=1;i<chemicals.size();i++) {
//					Chemical chemicali=chemicals.get(i);
//					this.merge(chemical0, chemicali);
//				}
//				
////				for (int i=2;i<=3;i++) {
////					File filei=null;
////					filei=new File(this.jsonFolder+"/"+CAS+"_"+i+".json");
////					if (filei.exists()) {
////						System.out.println("Deleting "+filei.getName());
////						filei.deleteOnExit();
////					}
////				}
//				
//				chemical0.writeToFile(jsonFolder);
//				
////				System.out.println(gson.toJson(chemical0));
//				
//				System.out.println("");
//
//			}//end loop over CAS numbers
//		
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}
	

	private void mergeRevisions2(Chemicals allChemicals) {
		try {
		
			ArrayList<String> CASList=new ArrayList<>();
			
			//Get list of unique cas numbers that dont have an underscore:
			for (Chemical chemical:allChemicals) {
				if (!CASList.contains(chemical.CAS) && !chemical.CAS.contains("_"))  {
					CASList.add(chemical.CAS);
				}
			}
			
			
			for (String CAS : CASList) {
//				if (!CAS.equals("100-44-7")) continue;
				Chemicals chemicals=new Chemicals();

				//Create array of chemicals that have the cas number (including ones with underscore):
				for (int i=1;i<=3;i++) {
					String casSeek="";
					
					if (i==1 ) casSeek=CAS;
					else casSeek=CAS+"_"+i;

					for (Chemical chemical:allChemicals) {
						if (chemical.CAS.equals(casSeek)) chemicals.add(chemical);
					}
				}
				
				if (chemicals.isEmpty()) System.out.println(CAS);
				
				Chemical chemical0=chemicals.get(0);
//				System.out.println(gson.toJson(chemical0));
				for (int i=1;i<chemicals.size();i++) {
					Chemical chemicali=chemicals.get(i);
					this.merge(chemical0, chemicali);//merge changes from chemicali into chemical0
				}
				
				chemicals.remove(0);
				
				//Remove the chemicals with underscore from overall list of chemicals:
				for (int i=0;i<chemicals.size();i++) {					
					Chemical chemicali=chemicals.get(i);
					
					for (int j=0;j<allChemicals.size();j++) {
						Chemical chemicalj=allChemicals.get(j);
								
						if (chemicali.CAS.equals(chemicalj.CAS)) {
							allChemicals.remove(j);
							break;
						}
					}
				}
				
				
//				System.out.println(gson.toJson(chemical0));
//				System.out.println("");
				
//				System.out.println(CAS);

			}//end loop over CAS numbers
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void merge(Chemical chemical1,Chemical chemical2) {
		
		for (int i=0;i<chemical1.scores.size();i++) {
			
			Score score1=chemical1.scores.get(i);
			Score score2=chemical2.scores.get(i);
			
			for (int j=0;j<score1.records.size();j++) {
				
				ScoreRecord scoreRecord1=score1.records.get(j);
				ScoreRecord scoreRecord2=null;
				
				if (score2.records.size()==0) {
//					System.out.println(chemical2.CAS+"\t"+score2.hazard_name+"\tno records");
					continue;
				}
				
				if (scoreRecord1.route==null) {
					scoreRecord2=score2.records.get(j);
				} else {
					//We need to make sure we match up the right inhalation records by route:
					for (int k=0;k<score2.records.size();k++) {
						if (score2.records.get(k).route.equals(scoreRecord1.route)) {
							scoreRecord2=score2.records.get(k);
						}
					}
				}

				String cat1=scoreRecord1.category;
				String cat2=scoreRecord2.category;

				if (cat2!=null) {
//					System.out.println(score1.hazard_name);
					
					if (cat1==null || !cat1.equals(cat2) ) {
//						System.out.println(chemical1.CAS+"\t"+score1.hazard_name+"\t"+cat1+"\t"+cat2);
					}
					score1.records.remove(j);
					
					scoreRecord2.note2=scoreRecord1.note2+"; Revised by "+scoreRecord2.note2;
					
					score1.records.add(scoreRecord2);
					
//					System.out.println(scoreRecord1.category);
					
				} else {
//					System.out.println("***null");
					continue;
				}
			}
		}
	}
	
	
	

//	private boolean haveChemical(Chemicals chemicals,Chemical chemicalNew) {
//		
//		for (Chemical chemical:chemicals) {
//			if (chemical.CAS.equals(chemicalNew.CAS)) return true;
//		}
//		return false;
//	}
	
	@Override
	protected Chemicals goThroughOriginalRecords() {

		
		
		Chemicals chemicals=new Chemicals();
		
		ArrayList<String>uniqueCAS=new ArrayList<>();
		
		try {
			File jsonFile = new File(mainFolder + "/" + fileNameJSON_Records);
			JsonArray records = gson.fromJson(new FileReader(jsonFile), JsonArray.class);

			System.out.println("Creating chemicals from Japan records json file");

			for (int i = 0; i < records.size(); i++) {

				if (i % 500 == 0)
					System.out.println(i);

				JsonObject jo = records.get(i).getAsJsonObject();

				// if (jo.get(strCAS)==null) continue;
				// if (!jo.get(strCAS).getAsJsonArray().get(0).getAsString().equals("102-06-7"))
				// continue;
				// System.out.println(gson.toJson(jo));

				// Translate scores:
				Chemicals chemicalsi=this.createChemicals(jo);
				
				for (Chemical chemicali:chemicalsi) {
					
					if (chemicali.CAS==null) {
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
			
			this.mergeRevisions2(chemicals);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
		
	}

//	private void deleteExtraFiles() {
//		File folder=new File(this.jsonFolder);
//		File [] files=folder.listFiles();
//		
//		for (File file:files) {
//			if (file.getName().contains("_2.json") ||file.getName().contains("_3.json")) {
//				file.deleteOnExit();
//			}
//		}
//	}
	
	@Override
	protected void createRecords() {
		JsonArray records = new JsonArray();
		File Folder = new File(mainFolder+File.separator+folderNameExcel);
		File[] files = Folder.listFiles();
		ArrayList<String> hazardsNotYetDefined = new ArrayList<>();
		
		for (int i = 0; i < files.length; i++) {
			File filei = files[i];
			if (filei.getName().indexOf(".xls") == -1)
				continue;
//			if (!filei.getName().equals("h20_mhlw_new_e.xls"))
//				continue;// for debug

			System.out.println(filei.getName());
			
			try {

				FileInputStream inputStream = new FileInputStream(filei);
				Workbook workbook = new HSSFWorkbook(inputStream);

				for (int j = 1; j < workbook.getNumberOfSheets(); j++) {// skip first sheet (summary sheet)
					String sheetname = workbook.getSheetName(j);
//					if (!sheetname.equals("20A2059"))
//						continue;// for debug
					records.add(importData(filei.getName(), workbook.getSheetAt(j), hazardsNotYetDefined));
				}

				workbook.close();
				inputStream.close();

			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
		
		for(String s: hazardsNotYetDefined) {
			System.out.println(s);
		}

		try {
			FileWriter fw = new FileWriter(mainFolder + "/" + fileNameJSON_Records);
			fw.write(gson.toJson(records));
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

//	private void createJapanDataFromCode() {
//		this.japanData = new JsonObject();
//
//		String[] sk = { "respiratory", "blood", "kidney", "liver", "adrenal", "gastro", "systemic", "eye", "heart",
//				"bone", "hematop", "cardio", "spleen", "thyroid", "lung", "gingi", "testes", "urinary" };
//		JsonArray systemic_keywords = new JsonArray();
//		for (int i = 0; i < sk.length; i++)
//			systemic_keywords.add(sk[i]);
//		japanData.add("systemic_keywords", systemic_keywords);
//
//		String[] nk = { "neuro", "nervous" };
//		JsonArray neuro_keywords = new JsonArray();
//		japanData.add("neuro_keywords", neuro_keywords);
//		for (int i = 0; i < nk.length; i++)
//			neuro_keywords.add(nk[i]);
//
//		JsonObject translationPatterns = new JsonObject();
//
//		JsonObject jo1 = new JsonObject();
//		jo1.addProperty("Category 1", 4);
//		jo1.addProperty("Category 1A", 4);
//		jo1.addProperty("Category 1B", 4);
//		jo1.addProperty("Category 2", 3);
//		jo1.addProperty("Not classified", 2);
//		translationPatterns.add("1", jo1);
//
//		this.japanData.add("translationPatterns", translationPatterns);
//
//		// TODO add rest
//
//	}

	public ParseJapanExcelClassification() {

		sourceName="Japan (excel file and classification)";
		folderNameExcel = "excel files";
		
		this.init();
		
//		System.out.println(this.mainFolder);
//		System.out.println("here main folder="+mainFolder);

		loadJSONDictionary();
	}

private void loadJSONDictionary() {
	try {

		InputStream is = this.getClass().getResourceAsStream("JapanDictionary2.json");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		// BufferedReader br=new BufferedReader(new FileReader("AA
		// Dashboard/data/Japan/JapanDictionary2.json"));
		this.japanData = gson.fromJson(br, JsonObject.class);
//			System.out.println(gson.toJson(japanData));
		// JsonObject
		// translationCriteria=japanData.get("translationCriteria").getAsJsonObject();

	} catch (Exception ex) {
		ex.printStackTrace();
	}
}

	public static void main(String[] args)  {
		ParseJapanExcelClassification p2 = new ParseJapanExcelClassification();
		p2.createFiles();
		
//		System.out.println("Merging files");
//		p2.mergeRevisions();
		
//		System.out.println("Deleting extra files");
		
		//For some reason- it doesnt want to delete the extra files, so here do it again:
//		p2.deleteExtraFiles();

		
	}

}

