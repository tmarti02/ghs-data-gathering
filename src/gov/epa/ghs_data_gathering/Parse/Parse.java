package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileWriter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.ghs_data_gathering.Utilities.Utilities;
import gov.epa.ghs_data_gathering.API.AADashboard;
import gov.epa.ghs_data_gathering.API.Chemical;
import gov.epa.ghs_data_gathering.API.Chemicals;
import gov.epa.ghs_data_gathering.API.Score;
import gov.epa.ghs_data_gathering.API.ScoreRecord;


public class Parse {

	public static String sourceName;
	public static String jsonFolder;

	protected String fileNameSourceExcel;//input excel spreadsheet
	protected String fileNameHtmlZip;//input excel spreadsheet
	protected String fileNameSourceText;
	protected String folderNameWebpages;
	protected String folderNameExcel;
	
	protected String fileNameJSON_Records;//records in original format
	
	protected String fileNameFlatChemicalRecords;//records in flat format
	protected String fileNameJsonChemicalRecords;//records in Chemical class format
	protected String mainFolder;
	
	public static boolean generateOriginalJSONRecords=true; //runs code to generate json records from original excel files (json file has all the chemicals in one file)
	public static boolean createDictionaryFile=true;
	public static boolean writeFlatFile=true;
	public static boolean writeJsonChemicalsFile=true;

	//For hazardCode based sources
	static Hashtable<String, String> dictCodeToScoreValue = CodeDictionary.populateCodeToScoreValue();
	static Multimap<String, String> dictCodeToScoreName = CodeDictionary.populateCodeToScoreName();
	static Hashtable<String, String> dictCodeToStatement = CodeDictionary.getHazardStatementDictionaryH();


	Gson gson=null;

	public void translate(Chemical chemical, Vector<String> hazardCodes, Vector<String> hazardClasses) {
		int count = 0;
		
		for (int i=0;i<hazardCodes.size();i++) {
			String hazardClass = hazardClasses.get(i);
			String hazardCode = hazardCodes.get(i);

			String route = "";
			String note="";

			if (hazardCode.contains("(") && hazardCode.contains(")")) {
				String organs="";
				String str2=hazardCode;
				while (str2.contains("(")) {
					organs+=hazardCode.substring(hazardCode.indexOf("(")+1, hazardCode.indexOf(")"))+"\t";
					str2=str2.substring(str2.indexOf(")")+1,str2.length());
				}
				organs=organs.trim();
				
				if (organs.length()>1) {
					note = "Target organs = "+organs;	
				}
				// Store target organs in a note:
				
				hazardCode=hazardCode.substring(0, hazardCode.indexOf("(")).trim();
//					System.out.println(hazardCode+"\t"+organs);
				//TODO check keywords to determine if have neurotox?
			}
			
			hazardCode = hazardCode.replace(" ", "").replace("*", "").replace("h", "H").trim();// TODO - what
			hazardClass = hazardClass.replace("*", "").trim();// TODO what does * mean?
			
			hazardCode=hazardCode.replace("H335,H336", "H335");//only happens twice for malaysia
			
//			System.out.println(chemical.CAS+"\t"+hazardCode+"\t"+dictCodeToScoreName.get(hazardCode));
			
			if (hazardCode.isEmpty()) continue;
			
			if (dictCodeToScoreName.get(hazardCode).isEmpty()) {
				System.out.println("Unknown Score Name\t"+hazardCode+"\t"+hazardClass);
				continue;
			}
			
//			if (dictCodeToScoreName.get(hazardCode)==null) continue;
			
			List<String> listScore = (List<String>) dictCodeToScoreName.get(hazardCode);			

			for(String scoreName:listScore) {
				if (scoreName==null || scoreName.equals("Omit")) continue;
				Score score=chemical.getScore(scoreName);
				
				if ((hazardCode.toLowerCase().contains("d") || hazardCode.toLowerCase().contains("f")) && (scoreName.equals(Chemical.strReproductive) || scoreName.equals(Chemical.strDevelopmental))) {
					handleReproDevTox(chemical, hazardClass, hazardCode,"",scoreName);
					continue;
				}
				
				route=getRoute(scoreName,route);
				
				if (scoreName.equals(Chemical.strAcute_Mammalian_ToxicityOral)) route="oral";
				if (scoreName.equals(Chemical.strAcute_Mammalian_ToxicityDermal)) route="dermal";
				if (scoreName.equals(Chemical.strAcute_Mammalian_ToxicityInhalation)) route="inhalation";
				
				if (dictCodeToScoreValue.get(hazardCode) == null) {
					// System.out.println(chemical.CAS+"\t"+hazardCode);

					System.out.println(count + "\t" + chemical.CAS + "\t" + hazardClass + "\t"
							+ hazardCode + "\t" + dictCodeToScoreValue.get(hazardCode));
					continue;
				}

				String strScore = dictCodeToScoreValue.get(hazardCode);
				createRecord(score, hazardClass, hazardCode, "",route, strScore, note);
				count++;

			}//end loop over score names
			
		}
	}
	
	
	String getRoute(String scoreName,String toxRoute) {
		
		if (scoreName.equals(Chemical.strAcute_Mammalian_ToxicityOral)) toxRoute="oral";
		if (scoreName.equals(Chemical.strAcute_Mammalian_ToxicityDermal)) toxRoute="dermal";
		if (scoreName.equals(Chemical.strAcute_Mammalian_ToxicityInhalation)) toxRoute="inhalation";

		return toxRoute;
	}
	
	protected void handleReproDevTox(Chemical chemical, String hazardClassification, String toxCode,String hazardStatement,String scoreName) {
		// TODO assign score
		/*
		 * Repr. 1A H360D Repr. 1B H360D Repr. 1B H360Df Repr. 1B H360F Repr. 1B H360FD
		 * Repr. 2 H361d Repr. 2 H361f Repr. 2 H361fd
		 */
		
		if (scoreName.equals(Chemical.strReproductive)) {
			if (toxCode.indexOf("F") > -1) {
				String strScore = ScoreRecord.scoreH;
				Score score = chemical.scoreReproductive;
				createRecord(score, hazardClassification, toxCode, hazardStatement,"", strScore, "");
			} else if (toxCode.indexOf("f") > -1) {
				String strScore = ScoreRecord.scoreM;
				Score score = chemical.scoreReproductive;
				createRecord(score, hazardClassification, toxCode, hazardStatement,"", strScore, "");
			}
			
		} else if (scoreName.equals(Chemical.strDevelopmental)) {
			
			if (toxCode.indexOf("D") > -1) {
				String strScore = ScoreRecord.scoreH;
				Score score = chemical.scoreDevelopmental;
				createRecord(score, hazardClassification, toxCode, hazardStatement,"", strScore, "");
			} else if (toxCode.indexOf("d") > -1) {
				String strScore = ScoreRecord.scoreM;
				Score score = chemical.scoreDevelopmental;
				createRecord(score, hazardClassification, toxCode, hazardStatement,"", strScore, "");
			}
		}
	}
	
	
	protected Vector<String> createVectorFromDelimitedString(String str,String del) {
		
		String[] hazardCodeList = str.split(del);
		Vector<String>hazardCodes=new Vector<String>();
		for(String s : hazardCodeList){
		    hazardCodes.add(s);
		}
		return hazardCodes;
	}

	protected void handleMultipleCAS(Chemicals chemicals, Chemical chemical) {
		
		boolean print=false;
		
		if (chemical.CAS!=null) {
			if (chemical.CAS.equals("NOCAS")  || chemical.CAS.equals("NA")) chemical.CAS="";
			if (chemical.CAS.equals("132-73-9")) chemical.CAS="123-73-9";
			if (chemical.CAS.equals("25136-40-9")) chemical.CAS="25316-40-9";
		}
		
		if (chemical.CAS==null || chemical.CAS.equals("") || chemical.CAS.equals("-") || chemical.CAS.equals("NA") || chemical.CAS.contains("--")) {
			chemical.CAS="";
			chemicals.add(chemical);
			if(print) System.out.println("No CAS\t"+chemical.name);
		
		} else if (chemical.CAS.contains("<br/>")) {
			chemical.CAS = chemical.CAS.replace("\n", "").replace("*", "");
			String [] numbers=chemical.CAS.split("<br/>");
			for (String CAS:numbers) {
				if (CAS.contains("deleted"))continue;
				Chemical clone=chemical.clone();
				clone.CAS=CAS;
				chemicals.add(clone);
				if(print) System.out.println("Have <br> tag\t"+CAS);
			}
		
		} else if (chemical.CAS.contains(";")) {	
			String [] numbers=chemical.CAS.split(";");
			for (String CAS:numbers) {
				Chemical clone=chemical.clone();
				clone.CAS=CAS.trim();
				chemicals.add(clone);
				if(print) System.out.println("Have semicolon\t"+CAS);
			}
			
		} else if (chemical.CAS.contains("/")) {
			String [] numbers=chemical.CAS.split("/");
			for (String CAS:numbers) {
				Chemical clone=chemical.clone();
				clone.CAS=CAS.trim();
				chemicals.add(clone);
				if(print) System.out.println("Have slash\t"+CAS);
			}
			
		} else if (chemical.CAS.indexOf(",") > -1) {
			String [] numbers=chemical.CAS.split(",");
			for (String CAS:numbers) {
				if (CAS.contains("deleted"))continue;
				Chemical clone=chemical.clone();
				clone.CAS=CAS.trim();
				chemicals.add(clone);
				if(print) System.out.println("Have comma\t"+CAS);
			}
		} else if (chemical.CAS.contains("\n")) {
			String [] numbers=chemical.CAS.split("\n");
			for (String CAS:numbers) {
				if(CAS.contains("[")) CAS=CAS.substring(0,CAS.indexOf("[")).trim();
				Chemical clone=chemical.clone();
				clone.CAS=CAS.trim();
				chemicals.add(clone);
				if(print) System.out.println("have new line character\t"+CAS);
			}
		} else if (chemical.CAS.contains("[")) {
			chemical.CAS=chemical.CAS.substring(0,chemical.CAS.indexOf("[")).trim();
			chemicals.add(chemical);
			if(print) System.out.println("Have [ and no carriage return\t"+chemical.CAS);
		} else if (!chemical.isCAS_OK()) {
			System.out.println(chemical.CAS + "\tbad cas\t" + chemical.name);// doesnt happen
			chemicals.add(chemical);
		} else {
			if(print) System.out.println("Ok CAS\t"+chemical.CAS);
			chemicals.add(chemical);
		}
	}
	
	public void createRecord(Score score, String hazardClassification, String toxCode, String hazardStatement,
			String toxRoute, String strScore, String strNote) {
		ScoreRecord sr = new ScoreRecord();
		score.records.add(sr);

		sr.source = sourceName;
		sr.category = hazardClassification;// TODO or assign to classification?
		
		
//		String bob=hazardClassification+toxCode+hazardStatement+toxRoute+strScore+strNote;
////		if (bob.contains("<br>")) 
//			System.out.println("***"+hazardClassification);
		
		sr.hazard_code = toxCode;
		sr.route = toxRoute;

		if (hazardStatement.isEmpty()) {
			if (dictCodeToStatement.get(toxCode) != null) {
				sr.hazard_statement = dictCodeToStatement.get(toxCode);
			} else {
				System.out.println("need statement for " + toxCode);
			}
		} else {
			sr.hazard_statement=hazardStatement;	
		}
		
		
		

		// Assign score based on toxCode:
		sr.score = strScore;
		sr.rationale = "Score of " + strScore + " was assigned based on a hazard code of " + toxCode;
		sr.note = strNote;

	}
	
	void writeOriginalRecordsToFile(Vector<?>records) {

		try {
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			
			FileWriter fw = new FileWriter(mainFolder + "/" + fileNameJSON_Records);
			String strRecords=gson.toJson(records);
			
//			getUniqueChars(strRecords);
			
			
			strRecords=this.fixChars(strRecords);
			
			fw.write(strRecords);
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	String fixChars(String str) {
/**
Added code to fix these:
（	\uff08	100-21-0|Terephthalic Acid|Systemic Toxicity Single Exposure|Japan|M||Category 3 （Respiratory tract irritation）|H335|May cause respiratory irritation (respiratory tract irritation)|Score of M was assigned based on a hazard code of H335|-|06-imcg-0155e.html; Revised by 14-mhlw-2064e.html|||
）	\uff09	100-21-0|Terephthalic Acid|Systemic Toxicity Single Exposure|Japan|M||Category 3 （Respiratory tract irritation）|H335|May cause respiratory irritation (respiratory tract irritation)|Score of M was assigned based on a hazard code of H335|-|06-imcg-0155e.html; Revised by 14-mhlw-2064e.html|||
／	\uff0f	3268-49-3|3-(Methylsulfanyl)propanal|Systemic Toxicity Single Exposure|Japan|H||Category 1／2 (systemic toxicity)|H370|Causes damage to organs (systemic toxicity)|Score of H was assigned based on a hazard code of H370|Although there were descriptions of "breathing difficulty, nasal mucus, crackle, ataxia, and locomotor activity reduction" (SIDS (2003)) in the acute oral toxicity test employing rats (OECD TG 401, GLP) and "locomotor activity reduction and ataxia" (SIDS (2003)) in the dermal administration test employing rabbits (OECD TG 402), since the dose was unknown, it classified into Category 1/2 (systemic toxicity).|08-meti-0065e.html|||
　	\u3000	104-87-0|p-Tolylaldehyde|Neurotoxicity Repeat Exposure|Japan|N/A||Classification not possible|-|-|Score of N/A was assigned based on a classification of "Classification not possible"|In the 17-day gavage study in rats (dose levels: 100 - 1000 mg/kg/day, converted dose levels as those of 90-day study: 18.8 - 188 mg/kg/day), food consumption, body weight gain, hematology, clinical chemistry, gross and histopathological findings were normal (PATTY, 5th (2001)). In addition, in the 13-week oral toxicity study in rats administered at 50 - 500 mg/kg, no unfavorable effects were observed except a decrease in relative pituitary weight of females in the high dose (HSDB (2009)). Thus, in spite of the classification applicable to "Not classified" in the oral route, the classification for specific target organ toxicity (repeated exposure)　was concluded as "Classification not possible" due to no information available for the other routes (inhalation and dermal exposure).|10-mhlw-0035e.html|||
 	\u00a0	NOCAS783|Hexachlorodibenzo-p-dioxin (HxCDD), mixture of 1,2,3,6,7,8-HxCDD and 1,2,3,7,8,9-HxCDD |Carcinogenicity|IRIS|VH||Category B2||Probable human carcinogen - based on sufficient evidence of carcinogenicity in animals|Score of VH was assigned based on a carcinogenicity category of Category B2|Hepatic tumors in mice and rats by gavage||||
 	\u2003	12179-04-3|Disodium tetraborate heptaoxide pentahydrate|Acute Mammalian Toxicity Dermal|Japan|N/A|dermal|Classification not possible|-|-|Score of N/A was assigned based on a classification of "Classification not possible"| Due to lack of data, the classification is not possible.|15-mhlw-0008e.html|||
	\u0009	106-91-2|2,3-epoxypropyl methacrylate; glycidyl methacrylate|Systemic Toxicity Repeat Exposure|ECHA CLP|H||STOT RE 1|H372|Causes damage to organs through prolonged or repeated exposure|Score of H was assigned based on a hazard code of H372|Target organs = respiratory tract	respiratory tract||||
「	\u300c	1310-73-2|Sodium hydroxide|Acute Mammalian Toxicity Oral|Japan|N/A|oral|Classification not possible|-|-|Score of N/A was assigned based on a classification of "Classification not possible"|「Classification not possible due to lack of data. As relevant information, a LD50 value is 325 mg/kg for rabbits (SIDS (2002)).|09-mhlw-2010e.html|||
」	\u300d	111-31-9|1-Hexanethiol|Eye Irritation|Japan|L||Not classified|-|-|Score of L was assigned based on a classification of "Not classified"|Based on a result of "not irritating" in a rabbit eye test (PATTY (5th, 2001)), the substance was classified as "Not classified".」|09-mhlw-0121e.html|||
?	\u003f	100-39-0|?-bromotoluene; benzyl bromide|Eye Irritation|ECHA CLP|H||Eye Irrit. 2|H319|Causes serious eye irritation|Score of H was assigned based on a hazard code of H319|||||

Not yet fixed:
£	\u00a3	68439-51-0|Alcohols, C12-14, ethoxylated propoxylated|Chronic Aquatic Toxicity|New Zealand|VH|algal|Category 9.1A (Category 1)||Very ecotoxic in the aquatic environment|Score was assigned based on a category of Category 9.1A (Category 1).|REMARK: Algae constitute the group of aquatic organisms which appears to be the most sensitive to AE. The acute toxicity of linear and branched AE to algae is in the same range with EC50 values from 0.05 to 50 mg/l. Besides the differences in chemical structure, the reason for the variation may be due to different test conditions and different test species. For the linear types, the toxicity increases with increasing hydrophobe chain length (comparison of C13 EO7-8 and C15 EO7-8, Table 4.8) and decreasing EO chain length (comparison of C12-14 with 4-13 EO, Table 4.8). The toxicity of AE to algae tends to decrease with increasing degree of branching (Table 4.9). Based on the low EC50 values (£ 1 mg/l), the linear AE of C12-15 EO6-8 are considered as very toxic to algae. When the degree of branching is low (£ 25%), the branched types are also considered very toxic to algae. A C12-14 EO9 end-capped with an n-butyl-group was very toxic to a non-specified alga as the EC50 was 0.3 mg/l (Schöberl et al. 1988). <br><br><br>REFERENCE SOURCE: [DANISH EPA]<br><br><br><br><br><br><br><br>Bioccumulative: No<br><br><br><br>Rapidly Degradable: ND<br><br>||||
¿	\u00bf	7783-28-0|Ammonium monohydrogen orthophosphate|Acute Mammalian Toxicity Inhalation|New Zealand|L|inhalation|Category 6.1E (Category 5)||Acutely toxic|Score was assigned based on a category of Category 6.1E (Category 5).|Inhalation Form:dust/mist<br><br> <br><br><br>SPECIES: <br><br><br>ENDPOINT: Mortality<br><br><br>VALUE: 1500 ppm<br><br><br>REFERENCE SOURCE: Kemira Pernis B.V. Rotterdam (32) American Conference of Governmental Industrial Hygienists (ACGIH), 1991-1993 cited in Cheminfo database.¿ (33) Canadian Centre for Occupational Health and Safety, MSDS database, 1993, Recordnummer: 568927, 567222, 551925, 540450, 434866, 52470. [IUCLID 2000]<br><br><br><br><br><br>REMARK: Death can occur at 1500 ppm<br><br><br>x ppm = y mg/m3*(24.45/mw)<br><br><br>mw = 132.1<br><br><br>1500 / (24.45/132.1) = 8104 mg/m3 = 8.1 mg/L<br><br><br><br><br><br>||||
	\u0007	67306-00-7|fenpropidin|Acute Mammalian Toxicity Inhalation|New Zealand|M|inhalation|Category 6.1D (Category 4)||Acutely toxic|Score was assigned based on a category of Category 6.1D (Category 4).|Inhalation Form:<br><br> SD rats<br><br><br><br><br><br>Doses: 0, 0.470, 0.679, 1.094, 1.338, 1.778, 2.385 mg/L (aerosol)<br><br><br><br><br><br>Number of animals: 16 per dose group (8 males and 8 females)<br><br><br><br><br><br>The particle size MMAD (mass median aerodynamic diameter) were between 1.89 and 2.32 mm with 100% w/w of the particles ? 8 mm and 72-83% w/w of the particles ? 4 mm depending on dose group.Acute inhalation toxicity (4 hours; nose only)<br><br><br>Fenpropidin (Ro 12-3049/000) batch and purity not stated<br><br><br>Conducted to GLP<br><br><br><br><br><br>Study was conducted prior to OECD Guideline 403 but has been checked in EFSA Review for compliance. No information was given on chemical identification, purity, vapour pressure, boiling point, flashpoint and explosivity. Data was also not given for equilibrium before dosing and acclimitisation of animals were for only 3 days for some animals. <br><br><br><br><br><br>Study Acceptability: Yes<br><br><br>LC50 = 1.220 mg/L<br><br><br><br><br><br>At a dose level of 1.778 mg/L there was evidence of pulmonary and dermal irritation. <br><br><br><br><br><br>Owen PE (1981), Ro 12-3049/000: Acute inhalation study - LC50 rats (4 hour exposure)||||
�	\ufffd	100-97-0|Methenamine|Skin Sensitization|Canada|H||Skin sensitization - Category 1B|H317|May cause allergic skin reaction|Score of H was assigned based on a hazard code of H317|Comments: This product belongs to the hazard class "Combustible dust" if 5% or more by weight of its composition has a particle size < 500 �m.||||
	\u0010	101-83-7|Cyclohexanamine, N-cyclohexyl-|Acute Aquatic Toxicity|DSL|L||Not Inherently_Toxic_to_Aquatic_Organisms|||Score of L was assigned based on a category of "Not Inherently_Toxic_to_Aquatic_Organisms"|||||

Should I fix any of these?
­	\u00ad	101-61-1|Michler's base [4,4´-methylenebis(<i>N</i>,<i>N</i>-dimethyl)­benzenamine]|Carcinogenicity|IARC|H||Group 2B||Possibly carcinogenic to humans|Score of H was assigned based on a carcinogenicity category of Group 2B|Volume 27, Sup 7, 99, 2010.||||
−	\u2212	142-92-7|Hexyl acetate|Acute Aquatic Toxicity|Japan|L||Not classified|-|-|Score of L was assigned based on a classification of "Not classified"|−|09-mhlw-0084e.html|||
‑	\u2011	149-30-4|2‑Mercaptobenzothiazole|Carcinogenicity|Prop 65|VH||Carcinogen|||Score of VH was assigned based on a carcinogenicity category of Carcinogen|||||
…	\u2026	10361-76-9|Potassium monopersulfate|Eye Irritation|New Zealand|VH||Category 8.3A (Category 1)||Corrosive to ocular tissue|Score was assigned based on a category of Category 8.3A (Category 1).|Info found under CAS 70693-62-8 (Potassium monopersulfate compound). <br><br><br>Eye contact…………...May cause irritation to the eyes, including pain, redness and<br><br><br>reversible damage.<br><br><br>[MSDS http://www.chem-world.com/pdf/potassium-monopersulfate-msds.pdf||||
!	\u0021	107-07-3|Ethylene chlorohydrin|Acute Mammalian Toxicity Oral|New Zealand|VH|oral|Category 6.1B (Category 2)||Acutely toxic|Score was assigned based on a category of Category 6.1B (Category 2).|R-PHRASE: R 28 Very toxic if swallowed. [ECB]<br><br><br>REMARK: R28: LD50 <br><!--= 25 mg/kg<br /--><br><br><br><br>||||
¡	\u00a1	13939-25-8|Triphosphoric acid, aluminum salt (1:1)|Systemic Toxicity Repeat Exposure|New Zealand|M|oral|Category 6.9B (Category 2)||Harmful to human target organs or systems|Score was assigned based on a category of Category 6.9B (Category 2).|EndPoint: NOAEL<br><br>Primary Organ: <br><br>Triphosphoric acid aluminium salt<br><br><br>[CAS No. 13939-25-8]<br><br><br>Aluminium triphosphate<br><br><br>Molecular formula: AlH6O12P3¡¡Molecular weight: 317.94<br><br><br><br><br><br>ABSTRACT<br><br><br>Triphosphoric acid aluminium salt was studied for oral toxicity in rats in both a single dose toxicity test at doses of 0 and 2000 mg/kg, and in a combined repeat dose and reproductive/developmental toxicity screening test at doses of 0, 100, 300 and 1000 mg/kg/day, in accordance with the OECD Test Guidelines 401 and 422. <br><br><br>With regard to repeated dose toxicity, decreases in total protein and calcium levels were observed in females given 1000 mg/kg. Atrophy of testes and epididymides was observed in males given 300 mg/kg or more. Atrophy of seminiferous tubules in testes, and decrease of spermatozoa in epididymides were observed on histopathological examination. <br><br><br>The NOELs are considered to be 100 mg/kg/day in males and 300 mg/kg/day in females.<br><br><br><br><br><br>2. Repeated Dose and Reproductive/Developmental Toxicity 1)<br><br><br>Purity : 94.7 % <br><br><br>Test species/strain : Rat/Crj:CD(SD)IGS <br><br><br>Test method : OECD Test Guideline 422 <br><br><br> Route : Oral (gavage) <br><br><br> Dosage : 0 (vehicle), 100, 300, 1000 mg/kg <br><br><br> Number of animals/group Males, 10; females, 10 <br><br><br> Vehicle : 0.5 % Sodium carboxymethylcellulose <br><br><br> Administration period : Males, 46 days<br><br><br>Females, from 14 days before mating to day 4 of lactation <br><br><br> Terminal killing : Males, day 47<br><br><br>Females, day 5 of lactation <br><br><br>GLP : Yes <br><br><br><br><br><br> Test results: <br><br><br><br><repeated dose toxicity> <br> <br><br> <br>Decreases in total protein and calcium levels were observed in females given 1000 mg/kg. <br> <br><br> <br>Atrophy of testes and epididymides was observed in males given 300 mg/kg or more. Atrophy of seminiferous tubules in testes and decrease of spermatozoa in epididymides were observed on histopathological examination. <br> <br><br> <br>The NOELs are considered to be 100 mg/kg/day in males and 300 mg/kg/day in females<br> <br><br> <br><br> <br><br> <br><br> <reproductive and developmental toxicity> <br>  <br><br>  <br>No effects were observed on reproductive performance in males and females given any dose, or on developmental performance in the pups. <br>  <br><br>  <br>The NOEL for reproductive/developmental toxicity is considered to be 1000 mg/kg/day for reproductive performance and offspring development.<br>  <br><br>  <br><br>  <br><br>  <br>[Global Information Network on Chemicals - The Databases of Chemicals<br>  <br><br>  <br>http://wwwdb.mhlw.go.jp/ginc/dbfile1/file/file13939-25-8.html<br>  <br><br>  <br>MHLW = Ministry of Health, Labour and Wealth, Japan]<br>  <br><br>  <br> <br> </reproductive><br></repeated>||||

OK:
%	\u0025	100-00-5|p-Nitrochlorobenzene|Chronic Aquatic Toxicity|Japan|H||Category 2|H411|Toxic to aquatic life with long lasting effects|Score of H was assigned based on a hazard code of H411|Classified into Category 2 since its acute toxicity is Category 2 and it is not rapidly degradable (BOD degradation rate: 0% (Biodegradation and Bioconcentration of Existing Chemical Substances under the Chemical Substances Control Law, 1976)).|09-mhlw-2096e.html|||
|	\u007c	CAS|name|hazard_name|source|score|route|category|hazard_code|hazard_statement|rationale|note|note2|valueMassOperator|valueMass|valueMassUnits
→	\u2192	5490-27-7|D-Streptamine, O-2-deoxy-2-(methylamino)-.alpha.-L-glucopyranosyl-(1→2)-O-5-deoxy-3-C-(hydroxymethyl)-.alpha.-L-lyxofuranosyl-(1→4)-N,N\'-bis(aminoiminomethyl)-, sulfate (2:3) (salt)|Acute Mammalian Toxicity Dermal|New Zealand|H|dermal|Category 6.1C (Category 3)||Acutely toxic|Score was assigned based on a category of Category 6.1C (Category 3).|||||
$	\u0024	101940-13-0|Thiocyanic acid, (1,3,8,10-tetrahydro-1,3, 8,10-tetraoxoanthra[2,1,9-def:6,5,10-d'e'f']diisoquinoline -2,9-diyl)di-3,1-phenylene ester, reaction products with sodium sulfide (Na2(S$x)), leuco deriv.|Acute Aquatic Toxicity|DSL|L||Not Inherently_Toxic_to_Aquatic_Organisms|||Score of L was assigned based on a category of "Not Inherently_Toxic_to_Aquatic_Organisms"|||||
·	\u00b7	10124-43-3|Cobalt (II) sulphate|Systemic Toxicity Repeat Exposure|New Zealand|H|inhalation|Category 6.9A (Category 1)||Toxic to human target organs or systems|Score was assigned based on a category of Category 6.9A (Category 1).|EndPoint: LOAEL <br><br>Primary Organ: <br><br>Tox - 5: Toxicity Studies of Cobalt Sulfate Heptahydrate in F344/N Rats and B6C3F1 (Inhalation Studies) (CAS No. 10026-24-1) <br><br><br>Chemical Formula: Co SO4 · 7H2O <br><br><br>Toxicology studies of cobalt sulfate heptahydrate (99% pure) were conducted by exposing groups of F344/N rats and B6C3F1 mice of each sex to a cobalt sulfate heptahydrate aerosol 6 hours per day, 5 days per week, for 16 days or 13 weeks. <br><br><br>In 16-day studies, all rats and mice exposed at the top concentration of 200 mg cobalt sulfate/m3 died (5 animals per group); partial survival was seen in the 50 mg/m3 exposure groups. Degeneration of the olfactory epithelium and necrotizing inflammation occurred in the nose of all rats and mice that died and in animals exposed to 50 mg/m3. Necrotizing inflammation was observed in the larynx and trachea of rats and mice at concentrations as low as 5 mg/m3, and a similar lesion was present in the bronchi at exposure concentrations of 50 mg/m3 or higher. Regenerative and inflammatory lesions, including peribronchial and septal fibrosis in the lung, were found in rats and mice exposed to 50 mg/m3. <br><br><br>In 13-week studies, all rats, all female mice, and all but 2 male mice exposed at the top concentration survived to the end of the studies (target exposure concentrations of 0, 0.3, 1, 3, 10, and 30 mg/m3, 10 animals per group). Rats and mice exposed to 30 mg/m3 lost weight during the first exposure week and then gained weight at the same rate as controls. Lung weights were increased over those of controls in rats exposed at concentrations as low as 1 mg/m3 and in mice exposed to 10 mg/m3 or more. Polycythemia was observed in rats exposed to cobalt sulfate but not in mice. Sperm motility was decreased in mice exposed at 3 mg/m3 or at higher concentrations (lower concentrations were not evaluated), and increased numbers of abnormal sperm were found in mice exposed to 30 mg/m3. Testis and epididymal weights were decreased in mice exposed to 30 mg/m3. Cobalt content in the urine of rats increased with increasing atmospheric cobalt exposure. <br><br><br>Lesions seen in the respiratory tract in 13-week studies in rats and mice included degeneration of the olfactory epithelium, squamous metaplasia of the respiratory epithelium, and inflammation in the nose; inflammation, necrosis, squamous metaplasia, ulcers (rats), and inflammatory polyps (rats) of the larynx; squamous metaplasia of the trachea (mice); and histiocytic infiltrates, bronchiolar regeneration, peribronchiolar and septal fibrosis, and epithelial hyperplasia in the alveoli of the lung. The most sensitive tissue was the larynx, with squamous metaplasia observed in rats and mice at the lowest exposure concentration of 0.3 mg/m3. Thus, a no-observed-adverse-effect level was not reached in these studies. <br><br><br>Report Date: January 1991<br><br><br>[NTP]<br><br><br>||||
•	\u2022	10265-92-6|Methamidophos|Systemic Toxicity Repeat Exposure|New Zealand|H|inhalation|Category 6.9A (Category 1)||Toxic to human target organs or systems|Score was assigned based on a category of Category 6.9A (Category 1).|EndPoint: NOAEL <br><br>Primary Organ: <br><br>**315-122; 089116; \"SRA 5172 Study of the Subchronic Inhalation Toxicity to Rats in Accordance with OECD Guideline No. 413\", Laboratory Project ID Report No. 98370; J. Pauluhn, Bayer AG, Wuppertal, Germany; 3/30/88. Groups of 10 Wistar rats/sex were head/nose exposed 6 hours/day, 5 days/week for 3 months to methamidophos (SRA 5172, batch # TOX 1767-00, 73.4%) at mean analytical concentrations of 0 (air only), 0 (vehicle), 0 (vehicle, recovery group), 1.1, 5.4, 23.1 (recovery group), or 23.1 mg/m3. After the 3 month exposure period, the two recovery groups (vehicle and 23.1 mg/m3) were allowed a 6-week exposure -free period. Treatment-related effects were seen in both sexes at 23.1 mg/m3: slight to moderate tremors on the day of exposure but not prior to exposure the next day; decreased body weight gain, decreased relative spleen weights and increased relative adrenal weights. There were treatment-related, noncumulative decreases in cholinesterase activity in plasma and brain, and increased sensitivity to the acetylcholine provocation test in both sexes at 5.4 and 23.1 mg/m3. The NOEL = 1.1 mg/m3 (• 0.3 mg/kg body weight/day) based on decreases in brain cholinesterase ant the mid and high doses and tremors at the high dose. No adverse effect was indicated. The study is acceptable as supplemental data (S. Morris and J. Gee, 1/19/99).<br><br><br>[Cal EPA]<br><br><br>||||
°	\u00b0	1066-33-7|Ammonium bicarbonate|Acute Mammalian Toxicity Oral|New Zealand|M|oral|Category 6.1D (Category 4)||Acutely toxic|Score was assigned based on a category of Category 6.1D (Category 4).|SPECIES: Rat<br><br><br>ENDPOINT: LD50<br><br><br>VALUE: 1576 mg/kg<br><br><br>REFERENCE SOURCE: Terni Industrie Chimiche S.p.A. Nera Montoro (22) BASF AG: Abt. Toxikologie, unveroeffentlichte Untersuchung, Project N° 10A0362/891061, 11.10.89<br><br><br>[IUCLID 2000]<br><br><br><br><br><br>||||
´	\u00b4	101-61-1|Michler's base [4,4´-methylenebis(<i>N</i>,<i>N</i>-dimethyl)­benzenamine]|Carcinogenicity|IARC|H||Group 2B||Possibly carcinogenic to humans|Score of H was assigned based on a carcinogenicity category of Group 2B|Volume 27, Sup 7, 99, 2010.||||
'	\u0027	100-00-5|p-Nitrochlorobenzene|Carcinogenicity|Japan|H||Category 2|H351|Suspected of causing cancer|Score of H was assigned based on a hazard code of H351|Two-year oral (feeding) tests were conducted in rats and mice. In rats, increased incidences of fibrosarcoma in the spleen, osteosarcoma, angiosarcoma, sarcoma N.O.S, fibroma and pheochromocytoma in the adrenal gland were observed in both sexes, and the carcinogenicity of p-chloronitrobenzene in F344/DuCrj (Fischer) rats was indicated. In mice, increased incidences of hemangioma, malignant lymphoma and hepatocellular carcinoma were observed in the males, and increased incidences of angiosarcoma in the liver and hepatocellular carcinoma were observed in females. However, it is not possible to conclude that p-chloronitrobenzene is carcinogenic in Crj:BDF1 mice due to the low incidences (Results from Carcinogenicity Studies (Ministry of Health, Labour and Welfare) (1991)). Based on the data, the Ministry of Health, Labour and Welfare has made a public announcement on guidelines in order to prevent the impairment of worker's health based on Industrial Safety and Health Act Article 28-3 (2006). The substance was classified into Category 2. The substance is classified into "3" in IARC (IARC 65 (1996)), "A3" in ACGIH (ACGIH-TLV (1985)) and "3" in EU classification (EU-Annex I (access on Jul. 2009)). The result from Carcinogenicity Studies (Ministry of Health, Labour and Welfare) (1991) was not used in these classifications.|09-mhlw-2096e.html|||
`	\u0060	101-68-8|4,4'-Methylene diphenyl diisocyanate (MDI) (inhalable fraction) see also ``polymeric MDI''|Carcinogenicity|Germany|M||Category 4|||Score of M was assigned based on a carcinogenicity category of Category 4|||||
′	\u2032	14047-09-7|3,3′,4,4′-Tetrachloroazobenzene|Carcinogenicity|IARC|VH||Group 2A||Probably carcinogenic to humans|Score of VH was assigned based on a carcinogenicity category of Group 2A|Volume 117, In prep.. NB: Overall evaluation upgraded to Group 2A.||||
‘	\u2018	544-97-8|Dimethylzinc|Neurotoxicity Repeat Exposure|Japan|N/A||Classification not possible|-|-|Score of N/A was assigned based on a classification of "Classification not possible"|Lack of data. Besides, there is information that "excessive or prolonged inhalation of fumes from ignition or decomp may cause ‘metal fume fever' (sore throat, headache, fever, chills, nausea, vomiting, muscular aches, perspiration, constricting sensation in lung, weakness, prostration)" (HSDB (2008)).|11-mhlw-0100e.html|||
“	\u201c	420-12-2|Ethylene sulfide|Skin Irritation|Japan|L||Not classified|-|-|Score of L was assigned based on a classification of "Not classified"|On the basis that this substance was mild irritant to skin with recovery within 24 hours (PATTY (5th, 2001)), it was classified as “Not classified” in JIS classification (Category 3 in UN GHS classification).|11-mhlw-0167e.html|||
”	\u201d	107-46-0|Hexamethyldisiloxane|Skin Sensitization|Japan|N/A||Classification not possible|-|-|Score of N/A was assigned based on a classification of "Classification not possible"|A repeated insult patch test with 100 subjects reported that any evidence of sensitization was not shown (KemI-Riskline (2002)), and a guinea pig maximization test (GLP compliance) resulted in not sensitizing (IUCLID (2000)). But it was classified as "Classification not possible” because the both information is from List 2.|11-mhlw-0018e.html|||
~	\u007e	1037-50-9|Sulphadimethoxine sodium salt|Chronic Aquatic Toxicity|New Zealand|VH|algal|Category 9.1A (Category 1)||Very ecotoxic in the aquatic environment|Score was assigned based on a category of Category 9.1A (Category 1).|DATA FROM SULFAMETHOXAZOLE - RISK ID 8380<br><br><br>Cyclotella meneghiniana NOEC = 1250 LOEC = 2500 EC50 = mg/l<br><br><br>Synechococcus leopolensis NOEC = 5.90 LOEC = 11.7 EC50 = 26.8 ug/l<br><br><br>TITLE: ECOTOXICOLOGICAL ASSESSMENTS AND REMOVAL TECHNOLOGIES FOR PHARMACEUTTICALS IN WASTEWATERS<br><br><br>http://cds.unina.it/~rmarotta/<br><br><br>Bioccumulative: ND<br><br><br><br>Rapidly Degradable: ND<br><br>||||
˜	\u02dc	7723-14-0|Phosphorus, white, yellow, dry or in solution|Acute Mammalian Toxicity Dermal|New Zealand|VH|dermal|Category 6.1A (Category 1)||Acutely toxic|Score was assigned based on a category of Category 6.1A (Category 1).|REMARK: Three..studies used rats as models of acute dermal burn. Doses of 29 mg/kg/day (Ben-Hur et al.1972), 100 mg/kg/day (Ben-Hur and Appelbaum 1973), and ˜ 182 mg/kg/day (Eldad and Simon 1991)<br><br><br>resulted in 5 of 10 (50%), 4 of 8 (50%), and 16 of 16 (100%) deaths, respectively, in the groups that were<br><br><br>burned with white phosphorus. <br><br><br>REFERENCE SOURCE: [ATSDR]<br><br><br><br><br><br>EXPERT JUDGEMENT: While dermal data is not a specific LD50 the Ben-Hur et al.1972 study showed that a dose of 29 mg/kg/day resulted in 5 of 10 (50%) rats.<br><br><br><br><br><br>||||
#	\u0023	100-44-7|Benzyl chloride|Skin Irritation|New Zealand|VH||Category 8.2B (Category 1B)||Corrosive to dermal tissue|Score was assigned based on a category of Category 8.2B (Category 1B).|UN #1738 UN CLASS: 8 PG II||||
*	\u002a	100-02-7|4-Nitrophenol|Chronic Aquatic Toxicity|New Zealand|L|algal|Category 9.1D (Category 4)||Slightly harmful in the aquatic environment or are otherwise designed for biocidal action|Score was assigned based on a category of Category 9.1D (Category 4).|SPECIES: Phaeodactylum tricornutum (Algae)<br><br><br>TYPE OF EXPOSURE:<br><br><br>DURATION: 72 hr<br><br><br>ENDPOINT: EC50 (Biomass)<br><br><br>VALUE: 20 mg/l<br><br><br>REFERENCE SOURCE: Hoechst AG Frankfurt/Main<br><br><br>Hoechst AG Frankfurt/Main Clariant GmbH Frankfurt am Main (87) Madsen (1984): Ecotoxicol. Bull 36, 165-170. [IUCLID 2000]<br><br><br><br><br><br><br><br>Bioccumulative: No<br><br>BCF values of 2 to 8 were measured for carp exposed to 0.2 mg/l of 4-nitrophenol over a 6 week incubation period(1). BCF values of 3 to 5 were measured for carp exposed to 0.02 mg/l of 4-nitrophenol over a 6 week incubation period(1). The BCF value of 4-nitrophenol was reported as 79 in fathead minnows(2) and 58 in golden orfe(3). According to a classification scheme(4), these BCF values suggest bioconcentration in aquatic organisms is low to moderate. <br><br><br>[(1) Chemicals Inspection and Testing Institute; Japan Chemical Industry Ecology - Toxicology and Information Center. ISBN 4-89074-101-1 (1992) (2) Call DJ et al; Arch Environ Contam Toxicol 9: 699-714 (1980) (3) Freitag D et al; Ecotox Environ Safety 6: 60-81 (1982) (4) Franke C et al; Chemosphere 29: 1501-14 (1994)]**PEER REVIEWED**<br><br><br>[HSDB]<br><br><br>Rapidly Degradable: Yes<br><br>Variety of reports indicate that substance does not persist in water. Report cited below is longest persistence obtained.<br><br><br> Summary : DEGR <br><br><br> System : WATER <br><br><br> Rate : 100% DEGR IN 15 DAYS (5 MG/L); 100% DEGR IN 3 DAYS (ACCLIM SLUDGE, 200 MG/L) <br><br><br> Analyt. Meth. : GC <br><br><br> Test Cond. : ACTIV SLUDGE WITH AND WITHOUT ACCLIM <br><br><br> Abbrev. Ref. : DOJLIDO,JR (1979) <br><br><br>[EFDB]||||
§	\u00a7	1918-16-7|Propachlor|Systemic Toxicity Repeat Exposure|New Zealand|M|oral|Category 6.9B (Category 2)||Harmful to human target organs or systems|Score was assigned based on a category of Category 6.9B (Category 2).|EndPoint: NOAEL<br><br>Primary Organ: Hepatotoxicity (liver)<br><br>GLN 83-5: Combined Chronic Toxicity/Carcinogenicity Study in Rats<br><br><br>In the combined chronic toxicity/carcinogenicity study in rats [MRID 44168301], Propachlor<br><br><br>[97.83% a.i.] was administered to 60 F-344 rats/sex/dose via the diet at dose levels of 0, 100, 300, 1000, and 2500 [males]/5000 [females] ppm [males 0, 5.4, 16.1, 53.6, and 125.3/females 0, 6.4, 19.3, 65.5, and 292.1 mg/kg/day, respectively] for 24 months. Due to palatability problems, the high dose level was attained by ramping the dose from 1000 ppm initially to the desired level by increasing by 500 ppm each week.<br><br><br>The NOEL in males is 5.4 mg/kg/day and in females is 6.4 mg/kg/day. The LOEL in males is 16.1 mg/kg/day and in females is 19.3 mg/kg/day, based on stomach lesions in males and liver lesions in both sexes. This guideline [§83-5] chronic toxicity/carcinogenicity study in the rat is Acceptable.<br><br><br>[REDS]||||
®	\u00ae	107-46-0|Hexamethyldisiloxane|Chronic Aquatic Toxicity|New Zealand|VH|fish|Category 9.1A (Category 1)||Very ecotoxic in the aquatic environment|Score was assigned based on a category of Category 9.1A (Category 1).|Data provided by notifier from submission to phase 2 scNOTS.<br><br><br>Ecotoxicity:<br><br><br>Species Test method Exp. Time Result Source<br><br><br>rainbow trout (Oncorhynchusmykiss)acute 96 h 0,46 mg/l (LC50) test report<br><br><br>Selenastrum capricornutum acute 96 h &gt; 0,93 mg/l (IC50) test report<br><br><br>Daphnia magna repro 21 d 0,3 mg/l (EC50) test report<br><br><br>Very toxic to aquatic organisms. May have long-term damaging effects in in-shore waters.<br><br><br>Safety Data Sheet (91/155/EEC)<br><br><br>Material: 60002656 WACKER® SILICONE FLUID AK 0,65<br><br><br>Version: 2.0 (REG_EUROPE) Date of print: 23.05.2006 Date of last alteration: 21.02.2005<br><br><br>[SCO 24/5/2006]<br><br><br><br><br><br>Bioccumulative: ND<br><br><br><br>Rapidly Degradable: ND<br><br>||||
™	\u2122	2386-87-0|7-Oxabicyclo[4.1.0]heptane-3-carboxylic acid, 7-oxabicyclo[4.1.0]hept-3-ylmethyl ester|Chronic Aquatic Toxicity|New Zealand|M|algal|Category 9.1C (Category 3)||Harmful in the aquatic environment|Score was assigned based on a category of Category 9.1C (Category 3).|R-PHRASE: R 52/53 [MSDS]<br><br><br>Bioccumulative: ND<br><br><br><br>Rapidly Degradable: No<br><br>Cycloaliphatic epoxy resin Xi* 36/38-43, 52/53<br><br><br>(Material Safety Data Sheet Product/Trade Name: Accura™ SI 40 - Nd type SL)<br><br><br>[http://www.3dsystems.com/products/datafiles/accura/msds/24022-S02-00-MSDS_SI40_Nd_AR%20HC.pdf]||||
：	\uff1a	280-57-9|Triethylenediamine|Acute Mammalian Toxicity Inhalation|Japan|L|Inhalation: Dusts and mists|Not classified|-|-|Score of L was assigned based on a classification of "Not classified"|Exposure at the saturated vapor pressure concentration (4.48 mL/L) /8hrs (converted value as that of 4-hour exposure: 8.96 mL/L) to rats caused no mortality (LC50： > 8.96 mL/L/4hrs) (SIDS (2005)). And in another study in rats, exposure at 20.2 mL/L/1h (converted value as that of 4-hour exposure: 5.05 mL/L) caused no mortality (LC50: > 5.05 mL/L/4hrs) (SIDS (2005)). Both test concentrations corresponded to "Not classified". The test concentration was higher than the saturated vapor pressure concentration (4.48 mL/L), the criterion values for dust/mist were adopted.|10-mhlw-0178e.html|||
＾	\uff3e	81-64-1|1,4-Dihydroxyanthraquinone|Acute Mammalian Toxicity Inhalation|Japan|N/A|Inhalation: Dusts and mists|Classification not possible|-|-|Score of N/A was assigned based on a classification of "Classification not possible"|Although a LC50 was >1 mg/L/4h for rats (IUCLID (2000)), classification was not possible since determination of category was impossible. Since the LC50 value (1 mg/L) was higher than saturated vapour pressure concentration (3.1*10＾-7 mg/L), the test was considered to be conducted for dusts.|09-mhlw-0112e.html|||
^	\u005e	10026-04-7|silicon tetrachloride|Acute Mammalian Toxicity Inhalation|Japan|L|Inhalation: Vapours|Not classified|-|-|Score of L was assigned based on a classification of "Not classified"|Its rat LC50 value of 8000 ppm/4h (56 mg/L) (IUCLID (2000)) falls under the "Not classified" category. Since its saturated vapour concentration is <= 0.34*10^6 ppm (2398 mg/L), the test is assumed to have been conducted in vapour with almost no included mists.|08-mhlw-0244e.html|||
±	\u00b1	1011-74-1|Benzenemethanol, alpha-(aminomethyl)-4-hydroxy-3-methoxy-, hydrochloride, (±)-|Skin Sensitization|Denmark|H||SkinSens1||May cause an allergic skin reaction|Prediction of "positive" and "strong" in the TOPKAT guinea pig models or a prediction of "very active" in the allergic contact dermatitis model|||||
［	\uff3b	18883-66-4|1-Methyl-1-nitroso-3-［(2S,3R,4R,5S,6R)‐2,4,5‐trihydroxy‐6‐(hydroxymethyl)oxsan-3-yl］urea, (alias Streptozocin)|Acute Aquatic Toxicity|Japan|N/A||Classification not possible|-|-|Score of N/A was assigned based on a classification of "Classification not possible"|No data available.|09-mhlw-0240e.html|||
‐	\u2010	18883-66-4|1-Methyl-1-nitroso-3-［(2S,3R,4R,5S,6R)‐2,4,5‐trihydroxy‐6‐(hydroxymethyl)oxsan-3-yl］urea, (alias Streptozocin)|Acute Aquatic Toxicity|Japan|N/A||Classification not possible|-|-|Score of N/A was assigned based on a classification of "Classification not possible"|No data available.|09-mhlw-0240e.html|||
］	\uff3d	18883-66-4|1-Methyl-1-nitroso-3-［(2S,3R,4R,5S,6R)‐2,4,5‐trihydroxy‐6‐(hydroxymethyl)oxsan-3-yl］urea, (alias Streptozocin)|Acute Aquatic Toxicity|Japan|N/A||Classification not possible|-|-|Score of N/A was assigned based on a classification of "Classification not possible"|No data available.|09-mhlw-0240e.html|||
≥	\u2265	10039-54-0|Hydroxylammonium sulphate, ≥5 - 9%in a non hazardous diluent|Chronic Aquatic Toxicity|New Zealand|H|algal|Category 9.1B (Category 2)||Very ecotoxic in the aquatic environment|Score was assigned based on a category of Category 9.1B (Category 2).|Classification based on parent record and mixture rules. <br><br><br><br><br><br>Bioccumulative: ND<br><br><br><br>Rapidly Degradable: ND<br><br>||||
≤	\u2264	1344-09-8|Sodium silicate with a molar ratio SiO2:Na2O ≤ 1.6, >10 - 24% in a non hazardous diluent|Acute Mammalian Toxicity Oral|New Zealand|L|oral|Category 6.1E (Category 5)||Acutely toxic|Score was assigned based on a category of Category 6.1E (Category 5).|Classification based on the parent record and mixture rules. <br><br>||||
>	\u003e	100-00-5|1-Chloro-4-nitrobenzene|Acute Mammalian Toxicity Dermal|ChemIDplus|L|Dermal||||Dermal LD50 (16000 mg/kg) > 2000 mg/kg|Test organism: rat<br><br>Reported Dose: 16gm/kg<br><br>Normalized Dose: 16000mg/kg<br><br>Source: Archiv fuer Gewerbepathologie und Gewerbehygiene. Vol. 17, Pg. 217, 1959.|||16000.0|mg/kg
<	\u003c	100-00-5|1-Chloro-4-nitrobenzene|Acute Mammalian Toxicity Dermal|ChemIDplus|L|Dermal||||Dermal LD50 (16000 mg/kg) > 2000 mg/kg|Test organism: rat<br><br>Reported Dose: 16gm/kg<br><br>Normalized Dose: 16000mg/kg<br><br>Source: Archiv fuer Gewerbepathologie und Gewerbehygiene. Vol. 17, Pg. 217, 1959.|||16000.0|mg/kg
=	\u003d	100-00-5|1-Chloro-4-nitrobenzene|Acute Mammalian Toxicity Oral|ChemIDplus|M|Oral||||300 mg/kg < Oral LD50 (420 mg/kg) <=2000 mg/kg|Test organism: rat<br><br>Reported Dose: 420mg/kg<br><br>Normalized Dose: 420mg/kg<br><br>Source: Archiv fuer Gewerbepathologie und Gewerbehygiene. Vol. 17, Pg. 217, 1959.|||420.0|mg/kg
@	\u0040	121-44-8|Ethanamine, N,N-diethyl-|Eye Irritation|New Zealand|VH||Category 8.3A (Category 1)||Corrosive to ocular tissue|Score was assigned based on a category of Category 8.3A (Category 1).|SPECIES: Rabbit<br><br><br>RESULT: Strongly alkaline and when drop is applied to the rabbits eye, causes severe injury, graded 9 on a scale of 1 to 10 after 24 hr/ Most severe injuries ahve been rated 10. tests of aq soln on rabbit eyes @ pH 10 and pH 11 indicate injuriousness of triethylamine/ is rated principally to degree of alkalinity. Chronic exposure of rabbits to vapours @ concn as low as 50 ppm in air causes multiple erosions of cornea and conjunctiva in 6 weeks.<br><br><br>REFERENCE SOURCE: [Grant, W.M. Toxicology of the Eye. 3rd ed. Springfield, IL: Charles C. Thomas Publisher, 1986. 944]**PEER REVIEWED** [HSDB]<br><br><br><br><br><br><br><br>||||
&	\u0026	0-21-3|Chlorophora excelsa (Welw.) Benth. & Hook|Skin Sensitization|Germany|H||Sh|||Score of H was assigned based on a category of Sh|||||
×	\u00d7	108171-26-2|Paraffin waxes and hydrocarbon waxes, chlorinated (C12, 60% chlorine)|Systemic Toxicity Repeat Exposure|New Zealand|M|oral|Category 6.9B (Category 2)||Harmful to human target organs or systems|Score was assigned based on a category of Category 6.9B (Category 2).|EndPoint: <br><br>Primary Organ: Hepatotoxicity (liver)<br><br>n repeated dose toxicity studies by the oral route, the liver,<br><br><br> kidney and thyroid have been shown to be the primary target organs for<br><br><br> the toxicity of chlorinated paraffins (Table 32). For the short chain<br><br><br> compounds, increases in liver and kidney weight and hypertrophy of the<br><br><br> liver and thyroid have been observed at lowest doses (LOEL = 100 mg/kg<br><br><br> body weight per day; NOEL = 10 mg/kg body weight per day; rats).<br><br><br>UNITED NATIONS ENVIRONMENT PROGRAMME / INTERNATIONAL LABOUR ORGANISATION / WORLD HEALTH ORGANIZATION<br><br><br>INTERNATIONAL PROGRAMME ON CHEMICAL SAFETY<br><br><br>ENVIRONMENTAL HEALTH CRITERIA 181: CHLORINATED PARAFFINS<br><br><br>[INCHEM]<br><br><br><br><br><br>On the basis of available data on repeated dose toxicity, a<br><br><br> Tolerable Daily Intake (TDI) for non-neoplastic effects of short chain<br><br><br> chlorinated paraffins for the general population can be developed:<br><br><br><br><br><br> 10 mg/kg body<br><br><br> weight per day<br><br><br> TDI = = 100 µg/kg body weight per day<br><br><br> 100<br><br><br><br><br><br> where 10 mg/kg body weight per day is the lowest reported<br><br><br> no-observed-effect level (increases in liver and kidney weights<br><br><br> and hypertrophy of the liver and thyroid at the next highest dose<br><br><br> in a 13-week study on rats) (IRDC, 1984a); and 100 is the<br><br><br> uncertainty factor (× 10 for interspecies variation; × 10 for<br><br><br> intraspecies variation).<br><br><br><br><br><br> On the basis of multistage modelling of the tumours with highest<br><br><br> incidence (hepatocellular adenomas or carcinomas (combined) in male<br><br><br> mice) in the carcinogenesis bioassay with short chain chlorinated<br><br><br> paraffins, the estimated dose associated with a 5% increase in tumour<br><br><br> incidence is 11 mg/kg body weight per day (amortized for period of<br><br><br> administration). After dividing this value by 1000 (uncertainty<br><br><br> factor for a non-genotoxic carcinogen), it can be recommended that<br><br><br> daily doses of short chain chlorinated paraffins for the general<br><br><br> population should not exceed 11 µg/kg body weight, on the basis of<br><br><br> neoplastic effects.<br><br><br>UNITED NATIONS ENVIRONMENT PROGRAMME / INTERNATIONAL LABOUR ORGANISATION / WORLD HEALTH ORGANIZATION<br><br><br>INTERNATIONAL PROGRAMME ON CHEMICAL SAFETY<br><br><br>ENVIRONMENTAL HEALTH CRITERIA 181: CHLORINATED PARAFFINS<br><br><br>[INCHEM]<br><br><br><br><br><br>||||
©	\u00a9	90622-58-5|Aliphatic hydrocarbon solvents - medium flashpoint|Acute Mammalian Toxicity Oral|New Zealand|L|oral|Category 6.1E (Category 5)||Acutely toxic|Score was assigned based on a category of Category 6.1E (Category 5).|Type: LD50<br><br><br>Species: rat Sex:<br><br><br>Number of Animals:<br><br><br>Vehicle:<br><br><br>Value: &gt; 5000 mg/kg bw<br><br><br>Method: other: not specified<br><br><br>Year: GLP: no data<br><br><br>Test substance: as prescribed by 1.1 - 1.4<br><br><br>Source: Exxon Chemical Belgium Antwerpen<br><br><br>Test substance: Isopar L<br><br><br>(6) Esso Research and Engineering Company. (1961). Acute Oral Administration-Rats; Acute Dermal Application-Rabbits;<br><br><br>Acute Inhalation Exposure-Mice, Rats, Guinea Pigs (Isopar<br><br><br>L). Unpublished report.<br><br><br>[IUCLID 2000]<br><br><br><br><br><br>Risk Phrases are: R65. Harmful: May cause lung damage if swallowed.<br><br><br>Swallowed: Because of the low viscosity of this product, it may directly enter the lungs if swallowed, or if subsequently vomited. Once in the lungs, it is very difficult to remove and can cause severe injury or death.<br><br><br>MSDS DF2000 copyright © Kilford &amp; Kilford Pty Ltd, January, 2001<br><br><br>[GOOGLE]||||
 */
		
		str=str.replace("–","-").replace("’","'");
		str=str.replace("\uff08", "(");// （
		str=str.replace("\uff09", ")");// ）
		str=str.replace("\uff0f", "/");// ／
		str=str.replace("\u3000", " ");//blank
		str=str.replace("\u00a0", " ");//blank
		str=str.replace("\u2003", " ");//blank
		str=str.replace("\u0009", " ");//blank
		str=str.replace("\u300c", "");// 「
		str=str.replace("\u300d", "");// 」
		str=str.replace("\u2264", "&le;");// <=  for some reason Gson messes this up so need to convert to html so code doesnt get mangled into weird symbol
		str=str.replace("\u03B1", "&alpha;");//alpha
		
//		System.out.println(str);
		return str;
		
	}
	
	public void init() {
		fileNameJSON_Records = sourceName +" Records.json";
		fileNameFlatChemicalRecords = sourceName +" Chemical Records.txt";
		fileNameJsonChemicalRecords = sourceName +" Chemical Records.json";
		mainFolder = AADashboard.dataFolder + File.separator + sourceName;
		jsonFolder= mainFolder + "/json files";
		
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
//		builder.disableHtmlEscaping();
		gson = builder.create();
		
		
//		System.out.println("here main folder="+mainFolder);
//		System.out.println("here db folder="+AADashboard.dataFolder);
	}
	
	
	public static void recreateFilesAllSources() {

		
//		Parse[] parsers = { new ParseDenmark(), new ParseDSL(), new ParseECHACLP(),
//				new ParseEPAMidAtlanticHumanHealth(),new ParseGermany() };
//		
//		for (Parse parse:parsers) {
//			System.out.println(parse.sourceName);
//			parse.createFiles();
//		}
//		if (true) return;

		//TODO: For some reason running here messes up the character encoding- but is fine if run from ParseAustralia
		ParseAustralia parseAustralia = new ParseAustralia();
		parseAustralia.createFiles();
		
		ParseDenmark parseDenmark = new ParseDenmark();
		parseDenmark.createFiles();

		ParseDSL parseDSL = new ParseDSL();
		parseDSL.createFiles();
		ParseECHACLP parseECHACLP = new ParseECHACLP();
		parseECHACLP.createFiles();

		ParseEPAMidAtlanticHumanHealth parseEPAMidAtlanticHumanHealth = new ParseEPAMidAtlanticHumanHealth();
		parseEPAMidAtlanticHumanHealth.createFiles();

		ParseGermany parseGermany = new ParseGermany();
		parseGermany.createFiles();

		ParseHealth_Canada_Priority_Substances_Carcinogenicity_2006 parseHealthCanadaCarcinogenicity = new ParseHealth_Canada_Priority_Substances_Carcinogenicity_2006();
		parseHealthCanadaCarcinogenicity.createFiles();

		ParseHealth_Canada_Priority_Substances_Reproductive_2006 parseHealthCanadaReproductive = new ParseHealth_Canada_Priority_Substances_Reproductive_2006();
		parseHealthCanadaReproductive.createFiles();

		ParseIARC parseIARC = new ParseIARC();
		parseIARC.createFiles();

		ParseIRIS parseIRIS = new ParseIRIS();
		parseIRIS.createFiles();

		ParseJapanExcelClassification parseJapan = new ParseJapanExcelClassification();
		parseJapan.createFiles();

//Need new source for Korea info		
//		ParseKorea parseKorea = new ParseKorea();
//		parseKorea.createFiles();

		ParseMalaysia parseMalaysia = new ParseMalaysia();
		parseMalaysia.createFiles();

		ParseNewZealand parseNZ = new ParseNewZealand();
		parseNZ.createFiles();

		ParseNIOSH_List_of_Potential_Occupational_Carcinogens parseNIOSH = new ParseNIOSH_List_of_Potential_Occupational_Carcinogens();
		parseNIOSH.createFiles();

		// System.out.print("Creating " + ParseOSPAR.sourceName + " json files...");
		// ParseOSPAR parseOSPAR=new ParseOSPAR();
		// parseOSPAR.createJSONFiles();
		// System.out.println("done");

		ParseProp65 parseProp65 = new ParseProp65();
		parseProp65.createFiles();

		ParseReachVeryHighConcernList parseReach = new ParseReachVeryHighConcernList();
		parseReach.createFiles();

		ParseReportOnCarcinogens parseROC = new ParseReportOnCarcinogens();
		parseROC.createFiles();

		ParseSIN parseSIN = new ParseSIN();
		parseSIN.createFiles();

		ParseTEDX parseTEDX = new ParseTEDX();
		parseTEDX.createFiles();

		ParseTSCA parseTSCA = new ParseTSCA();
		parseTSCA.createFiles();

		ParseUMD parseUMD = new ParseUMD();
		parseUMD.createFiles();

	}
	
	/**
	 * Need to override
	 */
	protected void createRecords() {
		System.out.println("Need to override createRecords()!");
		
	}
	
	/**
	 * Need to override
	 * 
	 * @return
	 */
	protected Chemicals goThroughOriginalRecords() {
		System.out.println("Need to override goThroughOriginalRecords()!");
		return null;
	}
	
	
	static void checkFileForSpecialChars(String filePath) {
		
		ArrayList<String>lines=Utilities.readFileToArray(filePath);
		
		ArrayList<Character> uniqueValues = new ArrayList<>();
		
		for (String line:lines) {
			char[] chars = line.toCharArray();
			for (Character c : chars) {
				// regex to not include letters, numbers, "[", "{", ":", "\"", ",", "_", "(",
				// "-", ".", "\", "/"
				if (c.toString().matches("[^\\p{L}\\p{N}\\[\\]+\n;{}:\",_(),.\\\\/-]") && (c != ' ')) {
					String s = String.format("\\u%04x", (int) c);
					// if (s.equals("\\u000a")) continue;

					if (!uniqueValues.contains(c)) {
						System.out.println(c+"\t"+s + "\t" + line);
						uniqueValues.add(c);
					}
				}
			}
			
		}
		
		
	}
	
	
	static void getUniqueChars(String data) {

		ArrayList<Character> uniqueValues = new ArrayList<>();

		System.out.println("Unique chars:");
		// converting string to array and checking each character
		char[] chars = data.toCharArray();
		for (Character c : chars) {
			// regex to not include letters, numbers, "[", "{", ":", "\"", ",", "_", "(",
			// "-", ".", "\", "/"
			if (c.toString().matches("[^\\p{L}\\p{N}\\[\\]+\n;{}:\",_(),.\\\\/-]") && (c != ' ')) {
				String s = String.format("\\u%04x", (int) c);
				// if (s.equals("\\u000a")) continue;

				if (!uniqueValues.contains(c)) {
					System.out.println(s + "\t" + c);
					uniqueValues.add(c);
				}
			}
		}

		// // printing unique characters
		// for (Character c : uniqueValues) {
		// System.out.println(c);
		// }

	}
	
	
	
	public void createFiles() {

		System.out.println("Creating " + sourceName + " json files...");
		
		if (generateOriginalJSONRecords) {
			if (fileNameSourceExcel!=null) {
				System.out.println("Parsing "+fileNameSourceExcel);
			} else if (folderNameWebpages!=null) {
				System.out.println("Parsing webpages in "+folderNameWebpages);
			} else if (folderNameExcel!=null) {
				System.out.println("Parsing excel files in "+folderNameExcel);
			} else {
				System.out.println("Parsing original file(s)");	
			}
			
//			System.out.println("here1");
			
			createRecords();
		}

		System.out.println("Going through original records");
		Chemicals chemicals=goThroughOriginalRecords();

		if (writeFlatFile) {
			System.out.println("Writing flat file for chemical records");
			chemicals.toFlatFile(mainFolder+File.separator+fileNameFlatChemicalRecords);
		}
		
		if (writeJsonChemicalsFile) {
			System.out.println("Writing json file for chemical records");
			chemicals.toJSON_File(mainFolder+File.separator+fileNameJsonChemicalRecords);
		}

		if (createDictionaryFile) {
			System.out.println("Creating dictionary file");
			GenerateOverallDictionary g = new GenerateOverallDictionary();
			String flatFilePath=mainFolder+"/"+this.fileNameFlatChemicalRecords;
			g.generateDictionaryFileFromFlatFile(sourceName,flatFilePath, AADashboard.dictionaryFolder);
		}
		
		System.out.println("done\n");

	}
	public static void main(String[] args) {
//		Parse.recreateFilesAllSources();
//		Parse.checkFileForSpecialChars("L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Australia\\Australia Chemical Records.txt");
		Parse.checkFileForSpecialChars("AA Dashboard\\Data\\dictionary\\text output\\flat file 2018-08-03.txt");
		
	}
	
}
