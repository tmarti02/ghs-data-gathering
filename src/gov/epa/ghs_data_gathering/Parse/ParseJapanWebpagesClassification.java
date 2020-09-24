package gov.epa.ghs_data_gathering.Parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.HazardRecord;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;

import com.google.gson.JsonElement;

/**
 * This class takes data from NITE webpages and creates chemical objects using
 * the "classification" to determine the scores.
 * 
 * This is Java conversion of python code by Wehage:
 * 
 * ghs.py This module contains a Python class, 'GHSJapanData', that is used to
 * import data from the GHS Japan website and translate the hazards from their
 * native representation to GreenScreen hazard specification. Copyright
 * 2013-2015 Kristopher Wehage University of California-Davis
 * (ktwehage@ucdavis.edu)
 * 
 * @author Todd Martin (Java)
 *
 */
public class ParseJapanWebpagesClassification extends Parse {
	// String file_path;
	JsonObject japanData;

	Hashtable<String, String> htHazards = createHazardHashtable();

	private static final String strClassification = "classification";
	private static final String strHazardStatement = "hazard_statement";
	private static final String strRationale = "rationale";
	
	String dictionaryFileName="JapanDictionary.json";
	

	String strName = "chemicalName";
	String strCAS = "CAS";

	boolean debug = false;

	Multimap<String, String> dictHazardNameToScoreName = CodeDictionary.populateJapanHazardClassToScoreName();

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

	/**
	 * This method translates scores and adds records to chemical at the same time
	 * 
	 * @param jo
	 * @param chemical
	 */
	private void translate(JapanRecord jr, Chemical chemical) {


		for (HazardRecord hr:jr.records) {
			
			if (hr.classifications.size()!=hr.hazardCodes.size()) {
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

				}
			}

			String baseURL="https://www.nite.go.jp/chem/english/ghs/";
			String filename=new File(jr.fileName).getName();
			String url=baseURL+filename;



			if (dictHazardNameToScoreName.get(hr.hazardClass)==null) {
				continue;
			}
			
			if (hr.classifications.get(0).equals("o-: Category 1 (central nervous system)")) {
				//Need to fix it for 25167-80-0
				String classification=hr.classifications.get(0)+", " +hr.classifications.remove(1);
				hr.classifications.set(0, classification);
			}
			
				
			for (int i=0;i<hr.classifications.size();i++) {

				String classification=hr.classifications.get(i);

				if (classification.equals("-")) {
					continue;
				}

				classification=classification.replace("(Unclassified)","Not classified");

				for (String scoreName:dictHazardNameToScoreName.get(hr.hazardClass)) {

					if (scoreName.equalsIgnoreCase("omit")) continue;
					
					String toxRoute = JapanRecord.getToxRoute(hr, scoreName);
					Score score=chemical.getScore(scoreName);

					//					System.out.println(scoreName);
					//				System.out.println("*"+jr.CAS+"\t"+hr.hazardClass+"\t"+scoreName+"\t"+classification);
					JsonObject translationCriteria = japanData.get("translationCriteria").getAsJsonObject();
					JsonObject translationPatterns = japanData.get("translationPatterns").getAsJsonObject();
					JsonObject translationCriterion=translationCriteria.getAsJsonObject(scoreName);
					String strPatternNumber=translationCriterion.get("pattern").getAsJsonArray().get(0).getAsString();
					JsonObject translationPattern=translationPatterns.getAsJsonObject(strPatternNumber);

					boolean requires_parsing = translationCriterion.get("requires_parsing").getAsBoolean();

					String keywordsName = null;
					JsonArray keywords = null;

					if (requires_parsing) {
						keywordsName = translationCriterion.get("keywords").getAsString();
						// System.out.println(keywordsName);
						keywords = japanData.get(keywordsName).getAsJsonArray();
					}

					boolean haveKeyword = false;

					if (requires_parsing) {
						// Loop through keywords to see if a keyword shows up in classification:
						for (int k = 0; k < keywords.size(); k++) {
							if (classification.toLowerCase().contains(keywords.get(k).getAsString().toLowerCase())) {
								//System.out.println(chemical.CAS+"\t"+key+"\t"+keywords.get(k)+"\t"+classification);
								haveKeyword = true;
								break;
							}
						}

						if (!haveKeyword)
							continue;
					}


					if (scoreName.equals(Chemical.strCarcinogenicity)) {
						//					System.out.println(classification);

						if (classification.contains("Not classified")) {
							if (!JapanRecord.isClassifiable(chemical.CAS, hr.rationale)) {							
								//Need to fix it and change it to not classifiable:							
								String newClassification = "Classification not possible";
								classification=newClassification;
								hr.rationale=hr.rationale.replace("Not classified", newClassification);
								//							System.out.println(jr.CAS+"\t"+hr.rationale);
							}
						} else {
							//						 System.out.println(chemical.CAS+"\t"+classification);
						}
					}


					String strScore=ScoreRecord.scoreNA;
					// Loop through translation pattern to see if a rating shows up in
					// classification:
					for (Entry<String, JsonElement> valueEntry2 : translationPattern.entrySet()) {
						String key2 = valueEntry2.getKey();

						if (classification.contains(key2)) {
							String rating = translationPattern.get(key2).getAsString();
							int iRating = Integer.parseInt(rating);
							strScore = getScoreFromTranslatedData(iRating);
							break;
						}
					}
					
					
					String hazard_code="-";
					if (i<hr.hazardCodes.size()) hazard_code=hr.hazardCodes.get(i);
					else {
//						System.out.println("i>hazardCodes.size()");
					}


					if (scoreName.equals("Developmental")) {
						if (hazard_code.equals("H362")) {
							strScore=ScoreRecord.scoreH;
//							System.out.println(jr.CAS+"\t"+classification+"\t"+hazard_code);
						} else {
//							System.out.println(jr.CAS+"\t"+classification+"\t"+hazard_code+"\t"+strScore);
						}
					} else if (scoreName.equals("Reproductive")) {
						if (hazard_code.equals("H362")) {
							continue;//should just assign to developemental
						}						
					}
					
					String hazard_statement="-";
					if (i<hr.hazardStatements.size()) hazard_statement=hr.hazardStatements.get(i);
					
					String rationale = "Score of " + strScore + " was assigned based on a classification of \"" + classification+"\"";
					String note=hr.rationale;
//					String note2=jr.fileName;

//					ScoreRecord sr=new ScoreRecord(chemical.CAS,chemical.name,sourceName,strScore,classification,hazard_code,hazard_statement,
//							rationale,toxRoute,note,note2,null);
										
//					public ScoreRecord(String CAS,String name,String hazard_name,String source,String score,String category,String hazard_code,String hazard_statement,
//							String rationale,String route,String note,String note2,String url) {
					
					ScoreRecord sr=new ScoreRecord(chemical.CAS, chemical.name, scoreName, ScoreRecord.sourceJapan, strScore, 
							classification, hazard_code, hazard_statement, rationale, toxRoute, note, null, url);
					score.records.add(sr);
					//				System.out.println(hr.hazardClass+"\t"+classification);

				}//end loop over scoreNames
			}//end loop over classifications


		}//end loop over hazard records
	}

	public static String removeBadChars(String name) {

		String[] bad = { "\\", ":", "/", "{", "}", "~", "#", "%", "&", "*", "{", "}", "<", ">", "?", "+", "|" };

		String newName = name;
		for (int i = 0; i < bad.length; i++) {
			newName = newName.replace(bad[i], "_");
		}
		return newName;

	}

	private Chemicals createChemicals(JapanRecord jr) {

		Chemicals chemicals = new Chemicals();

		if (jr.CAS == null) {
			Chemical chemical = new Chemical();
			chemical.name = jr.chemicalName;
			this.translate(jr, chemical);
			chemicals.add(chemical);
			return chemicals;
		}

		String strCASNumbers = jr.CAS;
		String[] casNumbers = strCASNumbers.split(",");

		for (String CAS : casNumbers) {
			Chemical chemical = new Chemical();
			chemical.CAS = CAS;
			chemical.name = jr.chemicalName;

			if (chemical.CAS.equals("")) {
				chemical.CAS = removeBadChars(chemical.name);
				// System.out.println(chemical.CAS);
			}

			if (chemical.CAS.length() > 100)
				continue;// dont write super long ones out

			this.translate(jr, chemical);

			// String ID=getUniqueID(chemical.CAS);

			if (chemical != null) {
				// chemical.writeToFile(ID,this.jsonFolder);
				chemicals.add(chemical);
			} else {
				System.out.println(jr.chemicalName + "\tnull");
			}
		}
		return chemicals;

	}

	private static void assignValues(JsonObject hazard, ScoreRecord sr, String route, String filename) {
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

		if (sr.category.equals(""))
			sr.category = null;
		if (sr.route.equals(""))
			sr.route = null;

		sr.note2 = filename;

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

		// Physical hazards
		ht.put("Explosives", "explosives");
		ht.put("Flammable gases (including chemically unstable gases)", "flammable_gases");
		ht.put("Aerosols", "flammable_aerosols");
		ht.put("Oxidizing gases", "oxidizing_gases");
		ht.put("Gases under pressure", "gases_under_pressure");
		ht.put("Flammable liquids", "flammable_liquids");
		ht.put("Flammable solids", "flammable_solids");
		ht.put("Self-reactive substances and mixtures", "self_reactive_substances");
		ht.put("Pyrophoric liquids", "pyrophoric_liquids");
		ht.put("Pyrophoric solids", "pyrophoric_solids");
		ht.put("Self-heating substances and mixtures", "self_heating_substances");
		ht.put("Substances and mixtures which, in contact with water, emit flammable gases",
				"substances_mixtures_emit_flammable_gas_in_contact_with_water");
		ht.put("Oxidizing liquids", "oxidizing_liquids");
		ht.put("Oxidizing solids", "oxidizing_solids");
		ht.put("Organic peroxides", "organic_peroxides");
		ht.put("Corrosive to metals", "corrosive_to_metals");
		ht.put("Flammable solid", "Flammable solid");
		ht.put("Hazardous to the ozone layer", "Hazardous to the ozone layer");

		// Health hazards
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

		// Environmental hazards
		ht.put("Hazardous to the aquatic environment (Acute)", "acute_aquatic_toxicity");
		ht.put("Hazardous to the aquatic environment (Long-term)", "chronic_aquatic_toxicity"); // TODO chronic or acute

		return ht;
	}

	/**
	 * Old version that uses json files
	 */
	// private void mergeRevisions() {
	//
	// try {
	//
	//
	// File folder=new File(this.jsonFolder);
	//
	// File [] files=folder.listFiles();
	//
	// ArrayList<String> CASList=new ArrayList<>();
	//
	//
	// for (File file:files) {
	// if (file.getName().length()>15) continue;
	//
	// if (file.getName().contains("_")) {
	// String CAS=file.getName().substring(0, file.getName().indexOf("_"));
	// if (!CASList.contains(CAS)) CASList.add(CAS);
	// }
	// }
	//
	//// for (String CAS:CASList) {
	//// System.out.println(CAS);
	//// }
	//
	//// String CAS="96-45-7";
	//
	//
	// for (String CAS : CASList) {
	//
	//// if (!CAS.equals("100-44-7")) continue;
	//
	// Chemicals chemicals=new Chemicals();
	//
	// for (int i=1;i<=3;i++) {
	//
	// File filei=null;
	//
	// if (i==1 ) {
	// filei=new File(this.jsonFolder+"/"+CAS+".json");
	// } else {
	// filei=new File(this.jsonFolder+"/"+CAS+"_"+i+".json");
	// }
	//
	// if (!filei.exists()) continue;
	//
	//// JsonObject jo = gson.fromJson(new FileReader(filei), JsonObject.class);
	// // System.out.println(gson.toJson(jo));
	//
	// Chemical chemical = Chemical.loadFromJSON(filei);
	// if (!chemical.CAS.equals(CAS)) continue;
	//
	// chemicals.add(chemical);
	// // TODO check if CAS numbers in the file match
	// }
	//
	// Chemical chemical0=chemicals.get(0);
	//// System.out.println(gson.toJson(chemical0));
	// for (int i=1;i<chemicals.size();i++) {
	// Chemical chemicali=chemicals.get(i);
	// this.merge(chemical0, chemicali);
	// }
	//
	//// for (int i=2;i<=3;i++) {
	//// File filei=null;
	//// filei=new File(this.jsonFolder+"/"+CAS+"_"+i+".json");
	//// if (filei.exists()) {
	//// System.out.println("Deleting "+filei.getName());
	//// filei.deleteOnExit();
	//// }
	//// }
	//
	// chemical0.writeToFile(jsonFolder);
	//
	//// System.out.println(gson.toJson(chemical0));
	//
	// System.out.println("");
	//
	// }//end loop over CAS numbers
	//
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	// }

	// private boolean haveChemical(Chemicals chemicals,Chemical chemicalNew) {
	//
	// for (Chemical chemical:chemicals) {
	// if (chemical.CAS.equals(chemicalNew.CAS)) return true;
	// }
	// return false;
	// }

	@Override
	protected Chemicals goThroughOriginalRecords() {

		Chemicals chemicals = new Chemicals();

		ArrayList<String> uniqueCAS = new ArrayList<>();

		try {
			File jsonFile = new File(mainFolder + "/" + fileNameJSON_Records);
			// JsonArray records = gson.fromJson(new FileReader(jsonFile), JsonArray.class);

			JapanRecord[] records = gson.fromJson(new FileReader(jsonFile), JapanRecord[].class);

			System.out.println("Creating chemicals from Japan records json file");

			for (int i = 0; i < records.length; i++) {
				// for (int i = 0; i < 1; i++) {
				if (i % 500 == 0)
					System.out.println(i);

				JapanRecord jr = records[i];

				// if (jo.get(strCAS)==null) continue;
				// if (!jo.get(strCAS).getAsJsonArray().get(0).getAsString().equals("102-06-7"))
				// continue;
				// System.out.println(gson.toJson(jo));

				// Translate scores:
				Chemicals chemicalsi = this.createChemicals(jr);

				for (Chemical chemicali : chemicalsi) {

					if (chemicali.CAS == null) {
						chemicali.CAS = chemicali.name;
					}

					if (uniqueCAS.contains(chemicali.CAS)) {
						int counter = 2;
						String newCAS = "";
						while (true) {
							newCAS = chemicali.CAS + "_" + counter;
							if (!uniqueCAS.contains(newCAS)) {
								break;
							}
							counter++;
						}
						chemicali.CAS = newCAS;
						uniqueCAS.add(chemicali.CAS);

						// System.out.println(newCAS);
					} else {
						uniqueCAS.add(chemicali.CAS);
					}
					chemicals.add(chemicali);
				}
			}

			chemicals.mergeRevisions();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;

	}

	// private void deleteExtraFiles() {
	// File folder=new File(this.jsonFolder);
	// File [] files=folder.listFiles();
	//
	// for (File file:files) {
	// if (file.getName().contains("_2.json") ||file.getName().contains("_3.json"))
	// {
	// file.deleteOnExit();
	// }
	// }
	// }

	@Override
	protected void createRecords() {
		// Vector<JapanRecord> records = parseChemicalWebpages(mainFolder + "/" +
		// folderNameWebpages);
		Vector<JapanRecord> records = JapanRecord
				.parseChemicalWebpagesInZipFile(mainFolder + "/" + this.fileNameHtmlZip);
		writeOriginalRecordsToFile(records);
	}

	public ParseJapanWebpagesClassification() {

		sourceName = "Japan (webpages and classification)";
		folderNameWebpages = "web pages";
		fileNameHtmlZip = "web pages.zip";
		this.init();
		loadJSONDictionary();
	}

	private void loadJSONDictionary() {
		try {

			BufferedReader br=new BufferedReader(new FileReader(mainFolder+"/"+dictionaryFileName));

//			InputStream is = this.getClass().getResourceAsStream(dictionaryFileName);
//			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			// BufferedReader br = new BufferedReader(new FileReader("AA
			// Dashboard\\Data\\Japan3\\JapanDictionary2.json"));
			// BufferedReader br=new BufferedReader(new FileReader("AA
			// Dashboard/data/Japan/JapanDictionary2.json"));
			this.japanData = gson.fromJson(br, JsonObject.class);
			// System.out.println(gson.toJson(japanData));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ParseJapanWebpagesClassification p = new ParseJapanWebpagesClassification();
		p.createFiles();

	}

}
