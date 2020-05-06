package gov.epa.ghs_data_gathering.API;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
//import java.util.Vector;

import org.openscience.cdk.AtomContainer;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Class to store hazard profile for a chemical
 *
 * This version has the scores as separate defined objects and the Json converts
 * to array
 *
 * @author Todd Martin
 *
 */
public class Chemical {

	public String indexNumber;
	public String EC_number;//EU classification number (or ID number from whatever source we got data from)
	public String name;// name of chemical (for display)
	public String CAS;// chemical abstracts service number for chemical
	
	public double molecularWeight;//molecular weight- need to convert from molar to mass units
	public String molecularFormula;
	
	public transient AtomContainer atomContainer;
	
	public String molFileV3000;//store MDL MOL FILE V3000 here from Chemistry Dashboard?
	public String SMILES;//molecular structure as SMILES string

	public ArrayList<Score> scores=new ArrayList<Score>();//array to store all the score data and their associated records	

	//Note: transient makes it not get serialized- scores object saves it to json instead
//	public transient Score scoreAcute_Mammalian_Toxicity = new Score();
	
	public transient Score scoreAcute_Mammalian_ToxicityOral = new Score();
	public transient Score scoreAcute_Mammalian_ToxicityInhalation = new Score();
	public transient Score scoreAcute_Mammalian_ToxicityDermal = new Score();
	
	public transient Score scoreCarcinogenicity = new Score();
	public transient Score scoreGenotoxicity_Mutagenicity = new Score();
	public transient Score scoreEndocrine_Disruption = new Score();
	public transient Score scoreReproductive = new Score();
	public transient Score scoreDevelopmental = new Score();
	
	public transient Score scoreNeurotoxicity_Repeat_Exposure = new Score();
	public transient Score scoreNeurotoxicity_Single_Exposure = new Score();
	
	public transient Score scoreSystemic_Toxicity_Repeat_Exposure = new Score();
	public transient Score scoreSystemic_Toxicity_Single_Exposure = new Score();
	
	public transient Score scoreSkin_Sensitization = new Score();
	public transient Score scoreEye_Irritation = new Score();
	public transient Score scoreSkin_Irritation = new Score();
	public transient Score scoreAcute_Aquatic_Toxicity = new Score();
	public transient Score scoreChronic_Aquatic_Toxicity = new Score();
	public transient Score scorePersistence = new Score();
	public transient Score scoreBioaccumulation = new Score();

	public transient Score scoreWaterSolubility=new Score();
	
	//Transformation products from CTS:
	ArrayList<Chemical> transformationProducts = new ArrayList<Chemical>();
	
	//Synthesis route chemicals from Task 4:
	ArrayList<Chemical> synthesisRouteChemicals = new ArrayList<Chemical>();
		
	
	//Hazard_names:
	public static final String strAcute_Mammalian_Toxicity = "Acute Mammalian Toxicity";
	
	public static final String strAcute_Mammalian_ToxicityOral = "Acute Mammalian Toxicity Oral";
	public static final String strAcute_Mammalian_ToxicityInhalation = "Acute Mammalian Toxicity Inhalation";
	public static final String strAcute_Mammalian_ToxicityDermal = "Acute Mammalian Toxicity Dermal";
	
	
	public static final String strCarcinogenicity = "Carcinogenicity";
	public static final String strGenotoxicity_Mutagenicity = "Genotoxicity Mutagenicity";
	public static final String strEndocrine_Disruption = "Endocrine Disruption";
	public static final String strReproductive = "Reproductive";
	public static final String strDevelopmental = "Developmental";
	
	public static final String strNeurotoxicity = "Neurotoxicity";
	public static final String strNeurotoxicity_Repeat_Exposure = "Neurotoxicity Repeat Exposure";
	public static final String strNeurotoxicity_Single_Exposure = "Neurotoxicity Single Exposure";
	
	public static final String strSystemic_Toxicity="Systemic Toxicity";
	public static final String strSystemic_Toxicity_Repeat_Exposure = "Systemic Toxicity Repeat Exposure";
	public static final String strSystemic_Toxicity_Single_Exposure = "Systemic Toxicity Single Exposure";
	
	public static final String strSkin_Sensitization = "Skin Sensitization";
	public static final String strSkin_Irritation = "Skin Irritation";
	public static final String strEye_Irritation = "Eye Irritation";

	public static final String strAcute_Aquatic_Toxicity = "Acute Aquatic Toxicity";
	public static final String strChronic_Aquatic_Toxicity = "Chronic Aquatic Toxicity";

	public static final String strPersistence = "Persistence";
	public static final String strBioaccumulation = "Bioaccumulation";

	//Array of hazard_names for convenience:
//	public static String[] hazard_names = { strAcute_Mammalian_Toxicity, strCarcinogenicity,
//			strGenotoxicity_Mutagenicity, strEndocrine_Disruption, strReproductive, strDevelopmental, strNeurological,
//			strRepeated_Dose, strSkin_Sensitization, strEye_Irritation, strSkin_Irritation, strAcute_Aquatic_Toxicity,
//			strChronic_Aquatic_Toxicity, strPersistence, strBioaccumulation };

	public static String[] hazard_names = { strAcute_Mammalian_ToxicityOral,strAcute_Mammalian_ToxicityInhalation,strAcute_Mammalian_ToxicityDermal, strCarcinogenicity,
	strGenotoxicity_Mutagenicity, strEndocrine_Disruption, strReproductive, strDevelopmental, strNeurotoxicity_Repeat_Exposure,strNeurotoxicity_Single_Exposure,
	strSystemic_Toxicity_Repeat_Exposure,strSystemic_Toxicity_Single_Exposure, strSkin_Sensitization, strEye_Irritation, strSkin_Irritation, strAcute_Aquatic_Toxicity,
	strChronic_Aquatic_Toxicity, strPersistence, strBioaccumulation };

	
	
	/**
	 * Constructor for Chemical
	 */
	public Chemical() {
		
		//Objects to store all the score data:
		//TODO- add score for systemic_toxicity_single_exposure since have systemic_toxicity_repeat_exposure from scoreRepeatedDose?
		//TODO- add score for nervous system tox?
		//TODO- add add score respiratory sensitization?
		
		//assign hazard names to the score objects (for json output):
//		scoreAcute_Mammalian_Toxicity.hazard_name = strAcute_Mammalian_Toxicity;
		
		scoreAcute_Mammalian_ToxicityOral.hazard_name = strAcute_Mammalian_ToxicityOral;
		scoreAcute_Mammalian_ToxicityInhalation.hazard_name = strAcute_Mammalian_ToxicityInhalation;
		scoreAcute_Mammalian_ToxicityDermal.hazard_name = strAcute_Mammalian_ToxicityDermal;
		
		
		scoreCarcinogenicity.hazard_name = strCarcinogenicity;
		scoreGenotoxicity_Mutagenicity.hazard_name = strGenotoxicity_Mutagenicity;
		scoreEndocrine_Disruption.hazard_name = strEndocrine_Disruption;
		scoreReproductive.hazard_name = strReproductive;
		scoreDevelopmental.hazard_name = strDevelopmental;
		
		scoreNeurotoxicity_Repeat_Exposure.hazard_name = strNeurotoxicity_Repeat_Exposure;
		scoreNeurotoxicity_Single_Exposure.hazard_name = strNeurotoxicity_Single_Exposure;
		
		scoreSystemic_Toxicity_Repeat_Exposure.hazard_name = strSystemic_Toxicity_Repeat_Exposure;
		scoreSystemic_Toxicity_Single_Exposure.hazard_name = strSystemic_Toxicity_Single_Exposure;
		
		scoreSkin_Sensitization.hazard_name = strSkin_Sensitization;
		scoreSkin_Irritation.hazard_name = strSkin_Irritation;
		scoreEye_Irritation.hazard_name = strEye_Irritation;
		scoreAcute_Aquatic_Toxicity.hazard_name = strAcute_Aquatic_Toxicity;
		scoreChronic_Aquatic_Toxicity.hazard_name = strChronic_Aquatic_Toxicity;
		scorePersistence.hazard_name = strPersistence;
		scoreBioaccumulation.hazard_name = strBioaccumulation;
		
//		scores.add(scoreAcute_Mammalian_Toxicity);
		
		scores.add(scoreAcute_Mammalian_ToxicityOral);
		scores.add(scoreAcute_Mammalian_ToxicityInhalation);
		scores.add(scoreAcute_Mammalian_ToxicityDermal);
		
		scores.add(scoreCarcinogenicity);
		scores.add(scoreGenotoxicity_Mutagenicity);
		scores.add(scoreEndocrine_Disruption);
		scores.add(scoreReproductive);
		scores.add(scoreDevelopmental);
		
		scores.add(scoreNeurotoxicity_Repeat_Exposure);
		scores.add(scoreNeurotoxicity_Single_Exposure);
		
		scores.add(scoreSystemic_Toxicity_Repeat_Exposure);
		scores.add(scoreSystemic_Toxicity_Single_Exposure);
		
		scores.add(scoreSkin_Sensitization);
		scores.add(scoreSkin_Irritation);
		scores.add(scoreEye_Irritation);
		scores.add(scoreAcute_Aquatic_Toxicity);
		scores.add(scoreChronic_Aquatic_Toxicity);
		scores.add(scorePersistence);
		scores.add(scoreBioaccumulation);
		
	}
	
	public Chemical clone() {
		
		Chemical clone=new Chemical();
		
		clone.indexNumber=indexNumber;
		clone.EC_number=EC_number;
		clone.name=name;
		clone.CAS=CAS;
		clone.molecularWeight=molecularWeight;
		clone.molecularFormula=molecularFormula;
		clone.molFileV3000=molFileV3000;
		clone.SMILES=SMILES;
		
		for (int i=0;i<scores.size();i++) {
			Score score=scores.get(i);
			Score scoreClone=clone.scores.get(i);
			
			ArrayList<ScoreRecord>records=score.records;
			ArrayList<ScoreRecord>recordsClone=scoreClone.records;
			
			for (ScoreRecord sr:records) {
				recordsClone.add(sr.clone());	
			}
		}
		
		ArrayList<String>lines=clone.toStringArray();
		
//		for (String line:lines) {
//			System.out.println("*"+line);
//		}
		
		return clone;
	}
	
	
	public boolean isCAS_OK() {
		
		if (CAS.indexOf(" ")>-1) {
			System.out.println("Space!");
			return false;
		}
		
		String [] part=CAS.split("-");
		
		if (part.length!=3) return false;
		
		String part1=part[0];
		String part2=part[1];
		String part3=part[2];
		
		
		
		int sum=0;
		
		for (int i=0;i<part1.length();i++) {
			String s=part1.substring(i, i+1);
			if (!Character.isDigit(s.charAt(0))) return false;
			sum+=(part1.length()+2-i)*Integer.parseInt(s);
		}
		
		String s1=part2.substring(0, 1);
		String s2=part2.substring(1, 2);
		
		if (!Character.isDigit(s1.charAt(0)) || !Character.isDigit(s2.charAt(0))) {
			return false;
		}
		
		int N2=Integer.parseInt(s1);
		int N1=Integer.parseInt(s2);
		int R=Integer.parseInt(part3);
		
		sum+=2*N2+N1;
		
		double bob=((double)sum)/10.0;
		double bob2=Math.floor(bob);
		double bob3=(bob-bob2)*10.0;
		
		int R2=(int)Math.round(bob3);
		
//		System.out.println(bob3);
//		System.out.println(R+"\t"+R2);
		
		return R2==R;
		
		
	}
	
	
	/**
	 * Store records from second chemical into first chemical
	 * 
	 * @param c1
	 * @param c2
	 */
	public void addRecords(Chemical c2) {
		
		

		for (int i=0;i<scores.size();i++) {
			
			Score score1i=scores.get(i);
			Score score2i=c2.scores.get(i);
			
			for (int j=0;j<score2i.records.size();j++) {
				score1i.records.add(score2i.records.get(j));
//				System.out.println(CAS+"\t"+score1i.hazard_name+"\t"+score1i.records.size());
			}
		}
	}
	

	/**
	 * Convenience method to create decaBDE without needing to load data from a file
	 * 
	 * 
	 * @return
	 */
	public static Chemical createDecaBDE() {
		Chemical c = new Chemical();

		c.name = "decaBDE";
		c.CAS = "1163-19-5";

		// for (int i=0;i<c.Categories.length;i++) {
		// System.out.println("Score
		// score"+Categories[i]+"=c.getScore(str"+Categories[i]+");");
		// }

		// //Human Health Effects scores
//		c.scoreAcute_Mammalian_Toxicity.final_score = "L";
		c.scoreAcute_Mammalian_ToxicityOral.final_score = "L";
		
		c.scoreCarcinogenicity.final_score = "M";
		c.scoreGenotoxicity_Mutagenicity.final_score = "L";
		c.scoreEndocrine_Disruption.final_score = "M";
		c.scoreReproductive.final_score = "L";
		c.scoreDevelopmental.final_score = "M";
		c.scoreNeurotoxicity_Repeat_Exposure.final_score = "M";
		c.scoreSystemic_Toxicity_Repeat_Exposure.final_score = "L";
		c.scoreSkin_Sensitization.final_score = "L";
		c.scoreEye_Irritation.final_score = "L";
		c.scoreSkin_Irritation.final_score = "L";
		//
		// //Ecotox endpoints
		c.scoreAcute_Aquatic_Toxicity.final_score = "L";
		c.scoreChronic_Aquatic_Toxicity.final_score = "L";
		//
		// //Fate endpoints
		c.scorePersistence.final_score = "VH";
		c.scoreBioaccumulation.final_score = "M";
		//
		// // Transformation Products
		c.transformationProducts = new ArrayList<Chemical>();
		c.transformationProducts.add(createPhenol());

		// ***********************************************************************************
		// Japan NITE data:
		// TODO: Japan ScoreRecord data should be loaded from NITE json files from Wehage using java code
		ScoreRecord sr_Acute_Aquatic_Toxicity = new ScoreRecord();
		sr_Acute_Aquatic_Toxicity.source = ScoreRecord.sourceJapan;
		sr_Acute_Aquatic_Toxicity.score = "L";
		sr_Acute_Aquatic_Toxicity.hazard_code = "Not classified";
		sr_Acute_Aquatic_Toxicity.hazard_statement = "-";
		sr_Acute_Aquatic_Toxicity.rationale = "Since 72 hours EC50 of the algae (Skeletonema) was more than the water solubility (EU-RAR (2003)), it was classified into Not classified.";
		c.scoreAcute_Aquatic_Toxicity.records.add(sr_Acute_Aquatic_Toxicity);

		ScoreRecord sr_Chronic_Aquatic_Toxicity = new ScoreRecord();
		sr_Chronic_Aquatic_Toxicity.source = ScoreRecord.sourceJapan;
		sr_Chronic_Aquatic_Toxicity.score = "N/A";
		sr_Chronic_Aquatic_Toxicity.hazard_code = "Not classified";
		sr_Chronic_Aquatic_Toxicity.hazard_statement = "-";
		sr_Chronic_Aquatic_Toxicity.rationale = "Although it is water-insolubility and acute toxicity was not reported within the aqueous solubility concentrations and there was no rapidly degrading (the decomposition by BOD: 0%(Existing Chemical Safety Inspections Data)), since the bio-accumulation (BCF<50 (Existing Chemical Safety Inspections Data)) was low, it was classified into Not classified.";
		c.scoreChronic_Aquatic_Toxicity.records.add(sr_Chronic_Aquatic_Toxicity);
		//
		 
		
		ScoreRecord sr_Acute_Toxicity_Oral=new ScoreRecord();
		 sr_Acute_Toxicity_Oral.source=ScoreRecord.sourceJapan;
		 sr_Acute_Toxicity_Oral.score="L";
		 sr_Acute_Toxicity_Oral.hazard_code="Not classified";
		 sr_Acute_Toxicity_Oral.hazard_statement="-";
		 sr_Acute_Toxicity_Oral.rationale="Based on the rat LD50 (oral route) value of > 5,000mg/kg (EHC 162 (1994)).";
		 sr_Acute_Toxicity_Oral.route="Oral";
//		 c.scoreAcute_Mammalian_Toxicity.records.add(sr_Acute_Toxicity_Oral);
		 c.scoreAcute_Mammalian_ToxicityOral.records.add(sr_Acute_Toxicity_Oral);
		
		 ScoreRecord sr_Acute_Toxicity_Dermal=new ScoreRecord();
		 sr_Acute_Toxicity_Dermal.source=ScoreRecord.sourceJapan;
		 sr_Acute_Toxicity_Dermal.score="L";
		 sr_Acute_Toxicity_Dermal.hazard_code="Classification not possible";
		 sr_Acute_Toxicity_Dermal.hazard_statement="-";
		 sr_Acute_Toxicity_Dermal.rationale="Insufficient data available";
		 sr_Acute_Toxicity_Dermal.route="Dermal";
//		 c.scoreAcute_Mammalian_Toxicity.records.add(sr_Acute_Toxicity_Dermal);
		 c.scoreAcute_Mammalian_ToxicityDermal.records.add(sr_Acute_Toxicity_Dermal);
		 
		 
		 ScoreRecord sr_Acute_Toxicity_Inhalation_Gas=new ScoreRecord();
		 sr_Acute_Toxicity_Inhalation_Gas.source=ScoreRecord.sourceJapan;
		 sr_Acute_Toxicity_Inhalation_Gas.score="L";
		 sr_Acute_Toxicity_Inhalation_Gas.hazard_code="Not applicable";
		 sr_Acute_Toxicity_Inhalation_Gas.hazard_statement="-";
		 sr_Acute_Toxicity_Inhalation_Gas.rationale="Due to the fact that the substance is \"solid\" according to the GHS definition and inhalation of its gas is not expected.";
		 sr_Acute_Toxicity_Inhalation_Gas.route="Inhalation gas";
//		 c.scoreAcute_Mammalian_Toxicity.records.add(sr_Acute_Toxicity_Inhalation_Gas);
		 c.scoreAcute_Mammalian_ToxicityInhalation.records.add(sr_Acute_Toxicity_Inhalation_Gas);
		
		ScoreRecord sr_Carcinogenicity = new ScoreRecord();
		sr_Carcinogenicity.source = ScoreRecord.sourceJapan;
		sr_Carcinogenicity.score = "L";
		sr_Carcinogenicity.hazard_code = "Not classified";
		sr_Carcinogenicity.hazard_statement = "-";
		sr_Carcinogenicity.rationale = "Due to the fact that the substance is classified as Group 3 by IARC (1999) and Category C by EPA (1990).";
		c.scoreCarcinogenicity.records.add(sr_Carcinogenicity);
		//
		ScoreRecord sr_Germ_Cell_Mutagenicity = new ScoreRecord();
		sr_Germ_Cell_Mutagenicity.source = ScoreRecord.sourceJapan;
		sr_Germ_Cell_Mutagenicity.score = "M";
		sr_Germ_Cell_Mutagenicity.hazard_code = "Category 2";
		sr_Germ_Cell_Mutagenicity.hazard_statement = "Suspected of causing genetic defects";
		sr_Germ_Cell_Mutagenicity.rationale = "Based on the absence of data on multi-generation mutagenicity tests, germ cell mutagenicity tests in vivo and germ cell genotoxicity tests in vivo, and positive data on somatic cell mutagenicity tests in vivo (micronucleus tests), described in NITE Initial Risk Assessment No.56 (2005), CERI-NITE Hazard Assessment No.56 (2005), EU-RAR No.17 (2002) and NTP DB (Access on April 2006).";
		c.scoreGenotoxicity_Mutagenicity.records.add(sr_Germ_Cell_Mutagenicity);

		ScoreRecord sr_Skin_Corrosion_Irritation = new ScoreRecord();
		sr_Skin_Corrosion_Irritation.source = ScoreRecord.sourceJapan;
		sr_Skin_Corrosion_Irritation.score = "M";
		sr_Skin_Corrosion_Irritation.hazard_code = "Category 3";
		sr_Skin_Corrosion_Irritation.hazard_statement = "Causes mild skin irritation";
		sr_Skin_Corrosion_Irritation.rationale = "Based on the description in the report on rabbit skin irritation tests (EHC 162 (1994)): \"The substance initially caused no irritation of the skin. After an observation period of 72 hours, slight erythematous and edematous responses were noted.\"";
		c.scoreSkin_Irritation.records.add(sr_Skin_Corrosion_Irritation);
		//
		//
		ScoreRecord sr_Serious_Eye_Damage_Irritation = new ScoreRecord();
		sr_Serious_Eye_Damage_Irritation.source = ScoreRecord.sourceJapan;
		sr_Serious_Eye_Damage_Irritation.score = "M";
		sr_Serious_Eye_Damage_Irritation.hazard_code = "Category 2B";
		sr_Serious_Eye_Damage_Irritation.hazard_statement = "Causes eye irritation";
		sr_Serious_Eye_Damage_Irritation.rationale = "Based on the description in the report on rabbit eye irritation tests (EHC 162 (1994), CERI-NITE Hazard Assessment No.56 (2005)): \"Animals treated with the substance showed only transient congestion and edema of the conjunctival membranes. These symptoms subsided by 24 hours,\" \"At 24 hours, only slight redness (4 of 6), only slight edema (2 of 6), and only slight discharge (1 of 6) of the conjunctiva were noted.\" The substance is thus considered a mild eye irritant.";
		c.scoreEye_Irritation.records.add(sr_Serious_Eye_Damage_Irritation);

		ScoreRecord sr_Skin_Sensitizer = new ScoreRecord();
		sr_Skin_Sensitizer.source = ScoreRecord.sourceJapan;
		sr_Skin_Sensitizer.score = "L";
		sr_Skin_Sensitizer.hazard_code = "Not classified";
		sr_Skin_Sensitizer.hazard_statement = "-";
		sr_Skin_Sensitizer.rationale = "Based on the negative results in human skin sensitization tests (CERI Hazard Data 97-16 (1998) and EHC 162 (1994)).";
		c.scoreSkin_Sensitization.records.add(sr_Skin_Sensitizer);

		ScoreRecord sr_Reproductive_Toxicity = new ScoreRecord();
		sr_Reproductive_Toxicity.source = ScoreRecord.sourceJapan;
		sr_Reproductive_Toxicity.score = "L";
		sr_Reproductive_Toxicity.hazard_code = "Not classified";
		sr_Reproductive_Toxicity.hazard_statement = "-";
		sr_Reproductive_Toxicity.rationale = "Based on no definitive evidence of reproductive toxicity observed in reproductive toxicity studies and teratogenicity studies in rats and mice, described in NITE Initial Risk Assessment No.56 (2005) and CERI-NITE Hazard Assessment No.56 (2005).";
		c.scoreReproductive.records.add(sr_Reproductive_Toxicity);
		
		//********************************************************************************************
		//TEST records
		sr_Acute_Toxicity_Oral = new ScoreRecord();
		sr_Acute_Toxicity_Oral.source = ScoreRecord.sourceTEST_Experimental;
		sr_Acute_Toxicity_Oral.score = "L";
		sr_Acute_Toxicity_Oral.hazard_code = "";
		sr_Acute_Toxicity_Oral.hazard_statement = "";
		sr_Acute_Toxicity_Oral.rationale = "Experimental LD50 > 5,000mg/kg";
		sr_Acute_Toxicity_Oral.route = "Oral";
//		c.scoreAcute_Mammalian_Toxicity.records.add(sr_Acute_Toxicity_Oral);
		c.scoreAcute_Mammalian_ToxicityOral.records.add(sr_Acute_Toxicity_Oral);

		sr_Acute_Toxicity_Oral = new ScoreRecord();
		sr_Acute_Toxicity_Oral.source = ScoreRecord.sourceTEST_Predicted;
		sr_Acute_Toxicity_Oral.score = "VH";
		sr_Acute_Toxicity_Oral.hazard_code = "";
		sr_Acute_Toxicity_Oral.hazard_statement = "";
		sr_Acute_Toxicity_Oral.rationale = "Predicted LD50 = 24.2 mg/kg";
		sr_Acute_Toxicity_Oral.route = "Oral";
//		c.scoreAcute_Mammalian_Toxicity.records.add(sr_Acute_Toxicity_Oral);
		c.scoreAcute_Mammalian_ToxicityOral.records.add(sr_Acute_Toxicity_Oral);
		
		sr_Germ_Cell_Mutagenicity = new ScoreRecord();
		sr_Germ_Cell_Mutagenicity.source = ScoreRecord.sourceTEST_Experimental;
		sr_Germ_Cell_Mutagenicity.score = "L";
		sr_Germ_Cell_Mutagenicity.hazard_code = "Mutagenicity Negative";
		sr_Germ_Cell_Mutagenicity.hazard_statement = "";
		sr_Germ_Cell_Mutagenicity.rationale = "Experimental Ames mutagenicity is negative";
		c.scoreGenotoxicity_Mutagenicity.records.add(sr_Germ_Cell_Mutagenicity);
		
		sr_Germ_Cell_Mutagenicity = new ScoreRecord();
		sr_Germ_Cell_Mutagenicity.source = ScoreRecord.sourceTEST_Predicted;
		sr_Germ_Cell_Mutagenicity.score = "L";
		sr_Germ_Cell_Mutagenicity.hazard_code = "Mutagenicity Negative";
		sr_Germ_Cell_Mutagenicity.hazard_statement = "";
		sr_Germ_Cell_Mutagenicity.rationale = "Experimental Ames mutagenicity is negative";
		c.scoreGenotoxicity_Mutagenicity.records.add(sr_Germ_Cell_Mutagenicity);


		sr_Acute_Aquatic_Toxicity = new ScoreRecord();
		sr_Acute_Aquatic_Toxicity.source = ScoreRecord.sourceTEST_Predicted;
		sr_Acute_Aquatic_Toxicity.score = "L";
		sr_Acute_Aquatic_Toxicity.hazard_code = "";
		sr_Acute_Aquatic_Toxicity.hazard_statement = "";
		sr_Acute_Aquatic_Toxicity.rationale = "Predicted LC50 = 6.44E-04 mg/L is greater than the experimental water solubility of 1E-04 mg/L";
		c.scoreAcute_Aquatic_Toxicity.records.add(sr_Acute_Aquatic_Toxicity);

		//TODO add ECHA CLP records
		
		return c;
	}

	// /**
	// * Convenience method to create decaBDE without needing to load data from a
	// file
	// *
	// *
	// *
	// * @return
	// */
	// public static Chemical createDecaBDE_old() {
	// Chemical c=new Chemical();
	//
	// c.name="decaBDE";
	// c.CAS="1163-19-5";
	//
	//
	// //Human Health Effects scores
	// c.scoreAcute_Mammalian_Toxicity="L";
	// c.scoreCarcinogenicity="M";
	// c.scoreGenotoxicity_Mutagenicity="L";
	// c.scoreEndocrine_Disruption="M";
	// c.scoreReproductive="L";
	// c.scoreDevelopmental="M";
	// c.scoreNeurological="M";
	// c.scoreRepeated_Dose="L";
	// c.scoreSkin_Sensitization="L";
	// c.scoreEye_Irritation="L";
	// c.scoreDermal_Irritation="L";
	//
	// //Ecotox endpoints
	// c.scoreAcute_Aquatic_Toxicity="L";
	// c.scoreChronic_Aquatic_Toxicity="L";
	//
	// //Fate endpoints
	// c.scorePersistence="VH";
	// c.scoreBioaccumulation="M";
	//
	// // Transformation Products
	// c.transformationProducts = new Vector<>();
	// c.transformationProducts.add(createPhenol());
	//
	// //***********************************************************************************
	// //Japan NITE data:
	//// TODO: Japan ScoreRecord data should be loaded from NITE json files from
	// Wehage using java code
	// ScoreRecord sr_Acute_Aquatic_Toxicity=new ScoreRecord();
	// sr_Acute_Aquatic_Toxicity.source=sourceJapan;
	// sr_Acute_Aquatic_Toxicity.score="L";
	// sr_Acute_Aquatic_Toxicity.hazard_code="Not classified";
	// sr_Acute_Aquatic_Toxicity.hazard_statement="-";
	// sr_Acute_Aquatic_Toxicity.rationale="Since 72 hours EC50 of the algae
	// (Skeletonema) was more than the water solubility (EU-RAR (2003)), it was
	// classified into Not classified.";
	// c.recordsAcute_Aquatic_Toxicity.add(sr_Acute_Aquatic_Toxicity);
	//
	// ScoreRecord sr_Chronic_Aquatic_Toxicity = new ScoreRecord();
	// sr_Chronic_Aquatic_Toxicity.source = sourceJapan;
	// sr_Chronic_Aquatic_Toxicity.score = "N/A";
	// sr_Chronic_Aquatic_Toxicity.hazard_code = "Not classified";
	// sr_Chronic_Aquatic_Toxicity.hazard_statement = "-";
	// sr_Chronic_Aquatic_Toxicity.rationale = "Although it is water-insolubility
	// and acute toxicity was not reported within the aqueous solubility
	// concentrations and there was no rapidly degrading (the decomposition by BOD:
	// 0%(Existing Chemical Safety Inspections Data)), since the bio-accumulation
	// (BCF<50 (Existing Chemical Safety Inspections Data)) was low, it was
	// classified into Not classified.";
	// c.recordsChronic_Aquatic_Toxicity.add(sr_Chronic_Aquatic_Toxicity);
	//
	//// ScoreRecord sr_Acute_Toxicity_Oral=new ScoreRecord();
	// sr_Acute_Toxicity_Oral.source=sourceJapan; sr_Acute_Toxicity_Oral.score="L";
	// sr_Acute_Toxicity_Oral.hazard_code="Not classified";
	// sr_Acute_Toxicity_Oral.hazard_statement="-";
	// sr_Acute_Toxicity_Oral.rationale="Based on the rat LD50 (oral route) value of
	// > 5,000mg/kg (EHC 162 (1994)).";
	// c.recordsAcute_Toxicity_Oral.add(sr_Acute_Toxicity_Oral);
	//// ScoreRecord sr_Acute_Toxicity_Dermal=new ScoreRecord();
	// sr_Acute_Toxicity_Dermal.source=sourceJapan;
	// sr_Acute_Toxicity_Dermal.score="L";
	// sr_Acute_Toxicity_Dermal.hazard_code="Classification not possible";
	// sr_Acute_Toxicity_Dermal.hazard_statement="-";
	// sr_Acute_Toxicity_Dermal.rationale="Insufficient data available";
	// c.recordsAcute_Toxicity_Dermal.add(sr_Acute_Toxicity_Dermal);
	//// ScoreRecord sr_Acute_Toxicity_Inhalation_Gas=new ScoreRecord();
	// sr_Acute_Toxicity_Inhalation_Gas.source=sourceJapan;
	// sr_Acute_Toxicity_Inhalation_Gas.score="L";
	// sr_Acute_Toxicity_Inhalation_Gas.hazard_code="Not applicable";
	// sr_Acute_Toxicity_Inhalation_Gas.hazard_statement="-";
	// sr_Acute_Toxicity_Inhalation_Gas.rationale="Due to the fact that the
	// substance is "solid" according to the GHS definition and inhalation of its
	// gas is not expected.";
	// c.recordsAcute_Toxicity_Inhalation_Gas.add(sr_Acute_Toxicity_Inhalation_Gas);
	//
	// ScoreRecord sr_Carcinogenicity = new ScoreRecord();
	// sr_Carcinogenicity.source = sourceJapan;
	// sr_Carcinogenicity.score = "L";
	// sr_Carcinogenicity.hazard_code = "Not classified";
	// sr_Carcinogenicity.hazard_statement = "-";
	// sr_Carcinogenicity.rationale = "Due to the fact that the substance is
	// classified as Group 3 by IARC (1999) and Category C by EPA (1990).";
	// c.recordsCarcinogenicity.add(sr_Carcinogenicity);
	//
	// ScoreRecord sr_Germ_Cell_Mutagenicity = new ScoreRecord();
	// sr_Germ_Cell_Mutagenicity.source = sourceJapan;
	// sr_Germ_Cell_Mutagenicity.score = "M";
	// sr_Germ_Cell_Mutagenicity.hazard_code = "Category 2";
	// sr_Germ_Cell_Mutagenicity.hazard_statement = "Suspected of causing genetic
	// defects";
	// sr_Germ_Cell_Mutagenicity.rationale = "Based on the absence of data on
	// multi-generation mutagenicity tests, germ cell mutagenicity tests in vivo and
	// germ cell genotoxicity tests in vivo, and positive data on somatic cell
	// mutagenicity tests in vivo (micronucleus tests), described in NITE Initial
	// Risk Assessment No.56 (2005), CERI-NITE Hazard Assessment No.56 (2005),
	// EU-RAR No.17 (2002) and NTP DB (Access on April 2006).";
	// c.recordsGenotoxicity_Mutagenicity.add(sr_Germ_Cell_Mutagenicity);
	//
	//
	// ScoreRecord sr_Skin_Corrosion_Irritation = new ScoreRecord();
	// sr_Skin_Corrosion_Irritation.source = sourceJapan;
	// sr_Skin_Corrosion_Irritation.score = "M";
	// sr_Skin_Corrosion_Irritation.hazard_code = "Category 3";
	// sr_Skin_Corrosion_Irritation.hazard_statement = "Causes mild skin
	// irritation";
	// sr_Skin_Corrosion_Irritation.rationale = "Based on the description in the
	// report on rabbit skin irritation tests (EHC 162 (1994)): \"The substance
	// initially caused no irritation of the skin. After an observation period of 72
	// hours, slight erythematous and edematous responses were noted.\"";
	// c.recordsDermal_Irritation.add(sr_Skin_Corrosion_Irritation);
	//
	//
	// ScoreRecord sr_Serious_Eye_Damage_Irritation = new ScoreRecord();
	// sr_Serious_Eye_Damage_Irritation.source = sourceJapan;
	// sr_Serious_Eye_Damage_Irritation.score = "M";
	// sr_Serious_Eye_Damage_Irritation.hazard_code = "Category 2B";
	// sr_Serious_Eye_Damage_Irritation.hazard_statement = "Causes eye irritation";
	// sr_Serious_Eye_Damage_Irritation.rationale = "Based on the description in the
	// report on rabbit eye irritation tests (EHC 162 (1994), CERI-NITE Hazard
	// Assessment No.56 (2005)): \"Animals treated with the substance showed only
	// transient congestion and edema of the conjunctival membranes. These symptoms
	// subsided by 24 hours,\" \"At 24 hours, only slight redness (4 of 6), only
	// slight edema (2 of 6), and only slight discharge (1 of 6) of the conjunctiva
	// were noted.\" The substance is thus considered a mild eye irritant.";
	// c.recordsEye_Irritation.add(sr_Serious_Eye_Damage_Irritation);
	//
	//
	// // ScoreRecord sr_Skin_Sensitizer=new ScoreRecord();
	// sr_Skin_Sensitizer.source=sourceJapan; sr_Skin_Sensitizer.score="L";
	// sr_Skin_Sensitizer.hazard_code="Not classified";
	// sr_Skin_Sensitizer.hazard_statement="-";
	// sr_Skin_Sensitizer.rationale="Based on the negative results in human skin
	// sensitization tests (CERI Hazard Data 97-16 (1998) and EHC 162 (1994)).";
	// c.recordsSkin_Sensitizer.add(sr_Skin_Sensitizer);
	//// ScoreRecord sr_Reproductive_Toxicity=new ScoreRecord();
	// sr_Reproductive_Toxicity.source=sourceJapan;
	// sr_Reproductive_Toxicity.score="L";
	// sr_Reproductive_Toxicity.hazard_code="Not classified";
	// sr_Reproductive_Toxicity.hazard_statement="-";
	// sr_Reproductive_Toxicity.rationale="Based on no definitive evidence of
	// reproductive toxicity observed in reproductive toxicity studies and
	// teratogenicity studies in rats and mice, described in NITE Initial Risk
	// Assessment No.56 (2005) and CERI-NITE Hazard Assessment No.56 (2005).";
	// c.recordsReproductive_Toxicity.add(sr_Reproductive_Toxicity);
	//
	//
	// //TODO add TEST experimental and predicted value
	//
	//
	// return c;
	// }
	public static Chemical createPhenol() {
		Chemical c = new Chemical();
		c.name = "Phenol";
		c.CAS="108-95-2";
		

		// Human Health Effects scores
//		c.scoreAcute_Mammalian_Toxicity.final_score = "M";
		c.scoreAcute_Mammalian_ToxicityOral.final_score = "M";
		c.scoreCarcinogenicity.final_score = "L";
		c.scoreGenotoxicity_Mutagenicity.final_score = "M";
		c.scoreEndocrine_Disruption.final_score = "L";
		c.scoreReproductive.final_score = "L";
		c.scoreDevelopmental.final_score = "L";
		c.scoreNeurotoxicity_Repeat_Exposure.final_score = "M";
		c.scoreSystemic_Toxicity_Repeat_Exposure.final_score = "H";
		c.scoreSkin_Sensitization.final_score = "L";
		c.scoreEye_Irritation.final_score = "H";
		c.scoreSkin_Irritation.final_score = "H";

		// Ecotox endpoints
		c.scoreAcute_Aquatic_Toxicity.final_score = "M";
		c.scoreChronic_Aquatic_Toxicity.final_score = "M";

		// Fate endpoints
		c.scorePersistence.final_score = "L";
		c.scoreBioaccumulation.final_score = "L";
		return c;
	}

	public static Chemical createTPP() {
		Chemical c = new Chemical();

		c.name = "TPP";
		c.CAS = "115-86-6";

		// Human Health Effects scores
//		c.scoreAcute_Mammalian_Toxicity.final_score = "L";
		c.scoreAcute_Mammalian_ToxicityOral.final_score = "L";
		c.scoreCarcinogenicity.final_score = "L";
		c.scoreGenotoxicity_Mutagenicity.final_score = "L";
		c.scoreEndocrine_Disruption.final_score = "N/A";
		c.scoreReproductive.final_score = "L";
		c.scoreDevelopmental.final_score = "L";
		c.scoreNeurotoxicity_Repeat_Exposure.final_score = "L";
		c.scoreSystemic_Toxicity_Repeat_Exposure.final_score = "M";
		c.scoreSkin_Sensitization.final_score = "L";
		c.scoreEye_Irritation.final_score = "M";
		c.scoreSkin_Irritation.final_score = "L";

		// Ecotox endpoints
		c.scoreAcute_Aquatic_Toxicity.final_score = "H";
		c.scoreChronic_Aquatic_Toxicity.final_score = "H";

		// Fate endpoints
		c.scorePersistence.final_score = "L";
		c.scoreBioaccumulation.final_score = "M";
		
		// TODO: Japan ScoreRecord data should be loaded from NITE json files from Wehage using java code		
		ScoreRecord sr_Acute_Aquatic_Toxicity = new ScoreRecord();
		sr_Acute_Aquatic_Toxicity.source = ScoreRecord.sourceJapan;
		sr_Acute_Aquatic_Toxicity.score = "VH";
		sr_Acute_Aquatic_Toxicity.hazard_code = "Category 1";
		sr_Acute_Aquatic_Toxicity.hazard_statement = "Very toxic to aquatic life";
		sr_Acute_Aquatic_Toxicity.rationale = "It was classified into Category 1 from 96-hour LC50=0.18-0.32mg/L of Crustacea (Mysid shrimp) (EHC111, 1991).";
		c.scoreAcute_Aquatic_Toxicity.records.add(sr_Acute_Aquatic_Toxicity);
		
		ScoreRecord sr_Chronic_Aquatic_Toxicity = new ScoreRecord();
		sr_Chronic_Aquatic_Toxicity.source = ScoreRecord.sourceJapan;
		sr_Chronic_Aquatic_Toxicity.score = "N/A";//TODO - wehage's scheme needs to be fixed!
		sr_Chronic_Aquatic_Toxicity.hazard_code = "Category 1";
		sr_Chronic_Aquatic_Toxicity.hazard_statement = "Very toxic to aquatic life with long lasting effects";
		sr_Chronic_Aquatic_Toxicity.rationale = "Classified into Category 1, since acute toxicity is Category 1, and supposedly bioaccumulative (log Kow=4.59(PHYSPROP Database, 2005)), though rapidly degrading (BOD: 90% (existing chemical substances safety inspections data)).";
		c.scoreChronic_Aquatic_Toxicity.records.add(sr_Chronic_Aquatic_Toxicity);
		
		
		ScoreRecord sr_Acute_Toxicity_Oral = new ScoreRecord();
		sr_Acute_Toxicity_Oral.source = ScoreRecord.sourceJapan;
		sr_Acute_Toxicity_Oral.score = "L";
		sr_Acute_Toxicity_Oral.hazard_code = "Category 5";
		sr_Acute_Toxicity_Oral.hazard_statement = "May be harmful if swallowed";
		sr_Acute_Toxicity_Oral.rationale = "Rat LD50 value: 3500mg/kg (MOE Risk Assessment vol.4,  2005, EHC 111, 1991), 3800mg/kg (EHC 111, 1999, ACGIH 7th, 2001, DFGOT vol.2, 1991), 10800mg/kg (EHC 111, 1991, DFGOT vol.2, 1991), >5000mg/kg (EHC 111, 1991) and >6400mg/kg (PATTY 4th, 1994). Calculated based on the data above. Since the calculated values was 3723.1mg/kg, it was classified to category 5.";
		sr_Acute_Toxicity_Oral.route="Oral";
//		c.scoreAcute_Mammalian_Toxicity.records.add(sr_Acute_Toxicity_Oral);
		c.scoreAcute_Mammalian_ToxicityOral.records.add(sr_Acute_Toxicity_Oral);

		ScoreRecord sr_Acute_Toxicity_Dermal = new ScoreRecord();
		sr_Acute_Toxicity_Dermal.source = ScoreRecord.sourceJapan;
		sr_Acute_Toxicity_Dermal.score = "L";
		sr_Acute_Toxicity_Dermal.hazard_code = "Not classified";
		sr_Acute_Toxicity_Dermal.hazard_statement = "-";
		sr_Acute_Toxicity_Dermal.rationale = "Based on rabbit LD50 value: >7900mg/kg (MOE Risk Assessment the 4th volume, 2005, EHC 111, 1991, DFGOT vol.2, 1991), and >10000mg/kg (DFGOT vol.2, 1991), it was set as the outside of Category.";
		sr_Acute_Toxicity_Dermal.route="Dermal";
//		c.scoreAcute_Mammalian_Toxicity.records.add(sr_Acute_Toxicity_Dermal);
		c.scoreAcute_Mammalian_ToxicityDermal.records.add(sr_Acute_Toxicity_Dermal);

		ScoreRecord sr_Acute_Toxicity_Inhalation_Gas = new ScoreRecord();
		sr_Acute_Toxicity_Inhalation_Gas.source = ScoreRecord.sourceJapan;
		sr_Acute_Toxicity_Inhalation_Gas.score = "L";
		sr_Acute_Toxicity_Inhalation_Gas.hazard_code = "Not applicable";
		sr_Acute_Toxicity_Inhalation_Gas.hazard_statement = "-";
		sr_Acute_Toxicity_Inhalation_Gas.rationale = "Solid (GHS definition)";
		sr_Acute_Toxicity_Inhalation_Gas.route="Inhalation gas";
//		c.scoreAcute_Mammalian_Toxicity.records.add(sr_Acute_Toxicity_Inhalation_Gas);
		c.scoreAcute_Mammalian_ToxicityInhalation.records.add(sr_Acute_Toxicity_Inhalation_Gas);
		
		
		ScoreRecord sr_Carcinogenicity = new ScoreRecord();
		sr_Carcinogenicity.source = ScoreRecord.sourceJapan;
		sr_Carcinogenicity.score = "L";
		sr_Carcinogenicity.hazard_code = "Not classified";
		sr_Carcinogenicity.hazard_statement = "-";
		sr_Carcinogenicity.rationale = "Since it was classified into A4 in ACGIH (ACGIH 7th, 2001), it was considered as the outside of Category.";
		c.scoreCarcinogenicity.records.add(sr_Carcinogenicity);
		
		ScoreRecord sr_Germ_Cell_Mutagenicity = new ScoreRecord();
		sr_Germ_Cell_Mutagenicity.source = ScoreRecord.sourceJapan;
		sr_Germ_Cell_Mutagenicity.score = "N/A";
		sr_Germ_Cell_Mutagenicity.hazard_code = "Classification not possible";
		sr_Germ_Cell_Mutagenicity.hazard_statement = "-";
		sr_Germ_Cell_Mutagenicity.rationale = "Classification not possible due to lack of data";
		c.scoreGenotoxicity_Mutagenicity.records.add(sr_Germ_Cell_Mutagenicity);

		ScoreRecord sr_Skin_Corrosion_Irritation = new ScoreRecord();
		sr_Skin_Corrosion_Irritation.source = ScoreRecord.sourceJapan;
		sr_Skin_Corrosion_Irritation.score = "L";
		sr_Skin_Corrosion_Irritation.hazard_code = "Not classified";
		sr_Skin_Corrosion_Irritation.hazard_statement = "-";
		sr_Skin_Corrosion_Irritation.rationale = "From description that irritation was not admitted in the test applied to the skin of the rat for 4 hours on DFGOT (2 vol. 1991) and ACGIH (7th, 2001), it was carried out the outside of Category.";
		c.scoreSkin_Irritation.records.add(sr_Skin_Corrosion_Irritation);

		ScoreRecord sr_Serious_Eye_Damage_Irritation=new ScoreRecord();
		sr_Serious_Eye_Damage_Irritation.source=ScoreRecord.sourceJapan;
		sr_Serious_Eye_Damage_Irritation.score="M";
		sr_Serious_Eye_Damage_Irritation.hazard_code="Category 2B";
		sr_Serious_Eye_Damage_Irritation.hazard_statement="Causes eye irritation";	sr_Serious_Eye_Damage_Irritation.rationale="We classified it as Category 2B based on the description that a slight conjunctival reddening was acknowledged and it disappeared within 7 days in the test applied to the eyes of the rabbits (DFGOT(vol.2,1991)).";	
		c.scoreEye_Irritation.records.add(sr_Serious_Eye_Damage_Irritation);
		
		ScoreRecord sr_Skin_Sensitizer = new ScoreRecord();
		sr_Skin_Sensitizer.source = ScoreRecord.sourceJapan;
		sr_Skin_Sensitizer.score = "N/A";
		sr_Skin_Sensitizer.hazard_code = "Classification not possible";
		sr_Skin_Sensitizer.hazard_statement = "-";
		sr_Skin_Sensitizer.rationale = "ACGIH (7th, 2001) and HSDB (2006) had description of the case report of allergic contact dermatitis, however, both of which were considered to be the same description of one case and did not have the report of two or more cases which is a judging standard of skin sensitization, and we thought the data was insufficient, therefore we presupposed that we could not classify it.";
		c.scoreSkin_Sensitization.records.add(sr_Skin_Sensitizer);
		
		ScoreRecord sr_reproductive_toxicity=new ScoreRecord();
		sr_reproductive_toxicity.source=ScoreRecord.sourceJapan;
		sr_reproductive_toxicity.score="L";
		sr_reproductive_toxicity.hazard_code="Not classified";
		sr_reproductive_toxicity.hazard_statement="-";
		sr_reproductive_toxicity.rationale="It was considered as out of Category based on the description that clear reproductive toxicity was not observed at the dose as which general toxicity is observed in parent animals in the test administered orally before mating till the term pregnancy using rat (MOE Risk Assessment 4th volume (2005), ACGIH (7th, 2001), and EHC 111 (1991)).";
		c.scoreReproductive.records.add(sr_reproductive_toxicity);


		// Transformation Products
		c.transformationProducts = new ArrayList<Chemical>();
		c.transformationProducts.add(createPhenol());
		c.transformationProducts.add(createDiphenylPhosphate());

		return c;
	}

	public static Chemical createDiphenylPhosphate() {
		Chemical chemical = new Chemical();
		chemical.name = "Diphenyl Phosphate";
		chemical.CAS="838-85-7";

		// Human Health Effects scores
//		chemical.scoreAcute_Mammalian_Toxicity.final_score = "M";
		chemical.scoreAcute_Mammalian_ToxicityOral.final_score = "M";
		chemical.scoreCarcinogenicity.final_score = "N/A";
		chemical.scoreGenotoxicity_Mutagenicity.final_score = "H";
		chemical.scoreEndocrine_Disruption.final_score = "N/A";
		chemical.scoreReproductive.final_score = "N/A";
		chemical.scoreDevelopmental.final_score = "H";
		chemical.scoreNeurotoxicity_Repeat_Exposure.final_score = "N/A";
		chemical.scoreSystemic_Toxicity_Repeat_Exposure.final_score = "N/A";
		chemical.scoreSkin_Sensitization.final_score = "N/A";
		chemical.scoreEye_Irritation.final_score = "N/A";
		chemical.scoreSkin_Irritation.final_score = "N/A";

		// Ecotox endpoints
		chemical.scoreAcute_Aquatic_Toxicity.final_score = "VH";
		chemical.scoreChronic_Aquatic_Toxicity.final_score = "N/A";

		// Fate endpoints
		chemical.scorePersistence.final_score = "H";
		chemical.scoreBioaccumulation.final_score = "L";
		return chemical;
	}

	// //Saves a chemical as JSON object (google gson)
	// public JsonObject toJSON() {
	// JsonObject jo=new JsonObject();
	//
	// jo.addProperty("Name", getName());
	// jo.addProperty("CAS", getCAS());
	//
	// //TODO - shorten using reflection? Or would that slow things down?
	//
	// //Overall final scores:
	// jo.addProperty("scoreAcute_Mammalian_Toxicity",scoreAcute_Aquatic_Toxicity);
	// jo.addProperty("scoreCarcinogenicity",scoreCarcinogenicity);
	// jo.addProperty("scoreGenotoxicity_Mutagenicity",scoreGenotoxicity_Mutagenicity);
	// jo.addProperty("scoreEndocrine_Disruption",scoreEndocrine_Disruption);
	// jo.addProperty("scoreReproductive",scoreReproductive);
	// jo.addProperty("scoreDevelopmental",scoreDevelopmental);
	// jo.addProperty("scoreNeurological",scoreNeurological);
	// jo.addProperty("scoreRepeated_Dose",scoreRepeated_Dose);
	// jo.addProperty("scoreSkin_Sensitization",scoreSkin_Sensitization);
	// jo.addProperty("scoreEye_Irritation",scoreEye_Irritation);
	// jo.addProperty("scoreDermal_Irritation",scoreDermal_Irritation);
	// jo.addProperty("scoreAcute_Aquatic_Toxicity",scoreAcute_Aquatic_Toxicity);
	// jo.addProperty("scoreChronic_Aquatic_Toxicity",scoreChronic_Aquatic_Toxicity);
	// jo.addProperty("scorePersistence",scorePersistence);
	// jo.addProperty("scoreBioaccumulation",scoreBioaccumulation);
	//
	//
	// add_Vector_to_JSON_Object(jo,"recordsAcute_Mammalian_Toxicity",recordsAcute_Mammalian_Toxicity);
	// add_Vector_to_JSON_Object(jo,"recordsCarcinogenicity",recordsCarcinogenicity);
	// add_Vector_to_JSON_Object(jo,"recordsGenotoxicity_Mutagenicity",recordsGenotoxicity_Mutagenicity);
	// add_Vector_to_JSON_Object(jo,"recordsEndocrine_Disruption",recordsEndocrine_Disruption);
	// add_Vector_to_JSON_Object(jo,"recordsReproductive",recordsReproductive);
	// add_Vector_to_JSON_Object(jo,"recordsDevelopmental",recordsDevelopmental);
	// add_Vector_to_JSON_Object(jo,"recordsNeurological",recordsNeurological);
	// add_Vector_to_JSON_Object(jo,"recordsRepeated_Dose",recordsRepeated_Dose);
	// add_Vector_to_JSON_Object(jo,"recordsSkin_Sensitization",recordsSkin_Sensitization);
	// add_Vector_to_JSON_Object(jo,"recordsEye_Irritation",recordsEye_Irritation);
	// add_Vector_to_JSON_Object(jo,"recordsDermalIrritation",recordsDermal_Irritation);
	//
	// add_Vector_to_JSON_Object(jo,"recordsAcute_Aquatic_Toxicity",recordsAcute_Aquatic_Toxicity);
	// add_Vector_to_JSON_Object(jo,"recordsChronic_Aquatic_Toxicity",recordsChronic_Aquatic_Toxicity);
	//
	// add_Vector_to_JSON_Object(jo,"recordsPersistence",recordsPersistence);
	// add_Vector_to_JSON_Object(jo,"recordsBioaccumulation",recordsBioaccumulation);
	//
	//
	//
	// JsonArray jaTransformationProducts=new JsonArray();
	// jo.add("transformationProducts", jaTransformationProducts);
	//
	// for (int i=0;i<transformationProducts.size();i++) {
	// jaTransformationProducts.add(transformationProducts.get(i).toJSON());
	// }
	//
	// return jo;
	// }

	
	public static Chemical loadFromJSON(BufferedReader br) {

		try {
			Gson gson = new Gson();

			if (br==null)
				return null;

			Chemical chemical = gson.fromJson(br, Chemical.class);

			// test it to see if it outputs back out correctly:
			// System.out.println(c.toJSON());
			return chemical;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static Chemical loadFromJSON(File jsonFile) {

		try {
			Gson gson = new Gson();

			if (!jsonFile.exists())
				return null;

			FileReader fr=new FileReader(jsonFile);
			Chemical chemical = gson.fromJson(fr, Chemical.class);
			fr.close();

			// test it to see if it outputs back out correctly:
			// System.out.println(c.toJSON());
			return chemical;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	
	public void removeEmptyFields() {
		
		for (int i=0;i<scores.size();i++) {
			Score score=scores.get(i);
			if (score.records.size()==0) {
				scores.remove(i--);
			}
		}
		if (transformationProducts.size()==0) {
			transformationProducts=null;
		}
		
		if (synthesisRouteChemicals.size()==0) {
			synthesisRouteChemicals=null;
		}
		
		
	}

	
	public static Chemical stringArrayToChemical(String header,ArrayList<String>lines) {
		
		Chemical chemical=new Chemical();
		
		
		
				

		
		
		return chemical;
	}
	
	ArrayList<String>toStringArray() {
		return toStringArray("|");
	}
	
	
	ArrayList<String>toStringArray(String d) {
		ArrayList<String>a=new ArrayList<>();
		
//		System.out.println(FlatFileRecord.getHeader("|"));
		
		for (Score score: scores) {
			for (ScoreRecord sr: score.records) {

				FlatFileRecord f=new FlatFileRecord();
				f.CAS=CAS;
				f.name=name;

				f.hazard_name=score.hazard_name;
				
				f.source=sr.source;
				f.sourceOriginal=sr.sourceOriginal;
				f.score=sr.score;
				f.category=sr.category;
				f.hazard_code=sr.hazard_code;
				f.hazard_statement=sr.hazard_statement;
				f.rationale=sr.rationale;
				f.route=sr.route;
				f.note=sr.note;
				f.note2=sr.note2;
				f.valueMassOperator=sr.valueMassOperator;
				
				if (sr.valueMass!=null)	f.valueMass=sr.valueMass;
				
				f.valueMassUnits=sr.valueMassUnits;
				
//				System.out.println(f.toString("|"));
				a.add(f.toString(d));
				
			}
			
		}
		
		return a;
		
	}
	
	
	/**
	 * Output chemical as a json string
	 * @return
	 */
	public String toJSONString() {
		
		GsonBuilder builder = new GsonBuilder();
//		builder.setPrettyPrinting().serializeNulls();
		builder.setPrettyPrinting();
//		builder.disableHtmlEscaping();
		
		builder.setFieldNamingStrategy(new FieldNamingStrategy() { 
			@Override
			public String translateName(Field field) {
				//use this if we want to rename anything before outputting:
				if (field.getName().equals("BOB"))
					return "bob";
				else
					return field.getName();
			}
		});
		
		Gson gson = builder.create();
		return gson.toJson(this);
	}
	
	/**
	 * Merges changes in a chemical (used mainly for Japan revisions)
	 * 
	 * @param chemical1
	 * @param chemical2
	 */
	public static void merge(Chemical chemical1,Chemical chemical2) {
		
//		if (chemical1.CAS.equals("107-02-8")) {
//			GsonBuilder builder = new GsonBuilder();
//			builder.setPrettyPrinting();
//			Gson gson = builder.create();
//
//			System.out.println(gson.toJson(chemical1));
//			System.out.println(gson.toJson(chemical2));
//		}
		
		
		for (int i=0;i<chemical1.scores.size();i++) {
			
			Score score1=chemical1.scores.get(i);
			Score score2=chemical2.scores.get(i);
			
			for (int j=0;j<score1.records.size();j++) {
				
				ScoreRecord scoreRecord1=score1.records.get(j);
				ScoreRecord scoreRecord2=null;
				
				if (scoreRecord1.route==null) {
					
					if (j<score2.records.size())
						scoreRecord2=score2.records.get(j);
					else {
//						System.out.println(chemical2.CAS+"\tno record for "+score1.hazard_name);
						continue;
					}
				} else {
					//We need to make sure we match up the right inhalation records by route:
					for (int k=0;k<score2.records.size();k++) {
						if (score2.records.get(k).route.equals(scoreRecord1.route)) {
							scoreRecord2=score2.records.get(k);
						}
					}
					
					if (scoreRecord2==null) {
//						System.out.println(chemical2.CAS+"\tno record for "+score1.hazard_name+", route="+scoreRecord1.route);
						continue;
					}
					
				}
				
				
				if (scoreRecord2.category!=null) {

//					System.out.println(score1.hazard_name);
					
//					if (scoreRecord1.category==null || !scoreRecord1.category.equals(scoreRecord2.category) ) {
//						if (chemical1.CAS.equals("107-02-8"))
//							System.out.println(chemical1.CAS+"\t"+score1.hazard_name+"\t"+scoreRecord1.category+"\t"+scoreRecord2.category);
//					}
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
	
//	/**
//	 * @deprecated - don't need since can do all in one step now in Chemicals class
//	 * 
//	 * @return
//	 */
//	public JsonObject toJSON() {
//		JsonObject jo = new JsonObject();
//
//		jo.addProperty("Name", getName());
//		jo.addProperty("CAS", getCAS());
//
//		// for (int i=0;i<hazard_names.length;i++) {
//		// System.out.println("score"+hazard_names[i]+".hazard_name=str"+hazard_names[i]+";");
//		// }
//
//		JsonArray jaScores = new JsonArray();//store scores in array since it makes it easier later to create comparison table
//
//		jaScores.add(this.scoreAcute_Mammalian_Toxicity().toJSON());
//		jaScores.add(this.scoreCarcinogenicity().toJSON());
//		jaScores.add(this.scoreGenotoxicity_Mutagenicity().toJSON());
//		jaScores.add(this.scoreEndocrine_Disruption().toJSON());
//		jaScores.add(this.scoreReproductive().toJSON());
//		jaScores.add(this.scoreDevelopmental().toJSON());
//		jaScores.add(this.scoreNeurological().toJSON());
//		jaScores.add(this.scoreRepeated_Dose().toJSON());
//		jaScores.add(this.scoreSkin_Sensitization().toJSON());
//		jaScores.add(this.scoreEye_Irritation().toJSON());
//		jaScores.add(this.scoreDermal_Irritation().toJSON());
//		jaScores.add(this.scoreAcute_Aquatic_Toxicity().toJSON());
//		jaScores.add(this.scoreChronic_Aquatic_Toxicity().toJSON());
//		jaScores.add(this.scorePersistence().toJSON());
//		jaScores.add(this.scoreBioaccumulation().toJSON());
//
//		jo.add("scores", jaScores);
//
//		if (transformationProducts.size() > 0) {
//
//			JsonArray jaTransformationProducts = new JsonArray();
//			jo.add("transformationProducts", jaTransformationProducts);
//
//			for (int i = 0; i < transformationProducts.size(); i++) {
//				jaTransformationProducts.add(transformationProducts.get(i).toJSON());
//			}
//
//		}
//		return jo;
//	}

	// //Saves a chemical as JSON object (google gson)
	// public JsonObject toJSON_using_reflection() {
	// JsonObject jo=new JsonObject();
	//
	// jo.addProperty("Name", getName());
	// jo.addProperty("CAS", getCAS());
	//
	//
	// String[] names = { "Acute_Mammalian_Toxicity", "Carcinogenicity",
	// "Genotoxicity_Mutagenicity",
	// "Endocrine_Disruption", "Reproductive", "Developmental", "Neurological",
	// "Repeated_Dose",
	// "Skin_Sensitization","Eye_Irritation","Dermal_Irritation","Acute_Aquatic_Toxicity",
	// "Chronic_Aquatic_Toxicity","Persistence","Bioaccumulation"};
	//
	// for (int i=0;i<names.length;i++) {
	// try {
	// Field myField = this.getClass().getField("score"+names[i]);
	// jo.addProperty("score"+names[i],(String)myField.get(this));
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	//
	// }
	//
	//
	// for (int i=0;i<names.length;i++) {
	// try {
	// Field myField2 = this.getClass().getField("records"+names[i]);
	// Vector<ScoreRecord>vector=(Vector<ScoreRecord>)myField2.get(this);
	// add_Vector_to_JSON_Object(jo,"records"+names[i],vector);
	//
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	// }
	//
	// JsonArray jaTransformationProducts=new JsonArray();
	// jo.add("transformationProducts", jaTransformationProducts);
	//
	// for (int i=0;i<transformationProducts.size();i++) {
	// jaTransformationProducts.add(transformationProducts.get(i).toJSON());
	// }
	//
	// return jo;
	// }

//	private void add_Vector_to_JSON_Object(JsonObject jo, String fieldname, ArrayList<ScoreRecord> vector) {
//
//		if (vector.size() == 0)
//			return;
//		JsonArray ja = new JsonArray();
//		jo.add(fieldname, ja);
//		for (int i = 0; i < vector.size(); i++) {
//			ja.add(vector.get(i).toJSON());
//		}
//	}

	public static Chemical createRDP() {
		Chemical c = new Chemical();

		c.name = "RDP";
		c.CAS = "57583-54-7";

		// Human Health Effects scores
//		c.scoreAcute_Mammalian_Toxicity.final_score = "L";
		c.scoreAcute_Mammalian_ToxicityOral.final_score = "L";
		c.scoreCarcinogenicity.final_score = "L";
		c.scoreGenotoxicity_Mutagenicity.final_score = "L";
		c.scoreEndocrine_Disruption.final_score = "N/A";
		c.scoreReproductive.final_score = "L";
		c.scoreDevelopmental.final_score = "L";
		c.scoreNeurotoxicity_Repeat_Exposure.final_score = "L";
		c.scoreSystemic_Toxicity_Repeat_Exposure.final_score = "M";
		c.scoreSkin_Sensitization.final_score = "L";
		c.scoreEye_Irritation.final_score = "M";
		c.scoreSkin_Irritation.final_score = "L";

		// Ecotox endpoints
		c.scoreAcute_Aquatic_Toxicity.final_score = "L";
		c.scoreChronic_Aquatic_Toxicity.final_score = "H";

		// Fate endpoints
		c.scorePersistence.final_score = "M";
		c.scoreBioaccumulation.final_score = "H";

		return c;
	}

	public Score getScore(String name) {
		for (int i=0;i<scores.size();i++) {
			if (scores.get(i).hazard_name.equals(name)) return scores.get(i);
		}
		return null;
	}
	
	public double getMolecularWeight() {
		return molecularWeight;
	}

	public void setMolecularWeight(double molecularWeight) {
		this.molecularWeight = molecularWeight;
	}

	public String getMolFileV3000() {
		return molFileV3000;
	}

	public void setMolFileV3000(String molFileV3000) {
		this.molFileV3000 = molFileV3000;
	}

	public ArrayList<Score> getScores() {
		return scores;
	}

	public void setScores(ArrayList<Score> scores) {
		this.scores = scores;
	}

	
	public static Chemical createBPADP() {
		Chemical c = new Chemical();

		c.name = "BPADP";
		c.CAS = "5945-33-5";

		// Human Health Effects scores
//		c.scoreAcute_Mammalian_Toxicity.final_score = "L";
		c.scoreAcute_Mammalian_ToxicityOral.final_score = "L";
		c.scoreCarcinogenicity.final_score = "L";
		c.scoreGenotoxicity_Mutagenicity.final_score = "L";
		c.scoreEndocrine_Disruption.final_score = "N/A";
		c.scoreReproductive.final_score = "L";
		c.scoreDevelopmental.final_score = "L";
		c.scoreNeurotoxicity_Repeat_Exposure.final_score = "L";
		c.scoreSystemic_Toxicity_Repeat_Exposure.final_score = "M";
		c.scoreSkin_Sensitization.final_score = "L";
		c.scoreEye_Irritation.final_score = "M";
		c.scoreSkin_Irritation.final_score = "L";

		// Ecotox endpoints
		c.scoreAcute_Aquatic_Toxicity.final_score = "L";
		c.scoreChronic_Aquatic_Toxicity.final_score = "L";

		// Fate endpoints
		c.scorePersistence.final_score = "H";
		c.scoreBioaccumulation.final_score = "L";

		return c;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCAS() {
		return CAS;
	}

	public void setCAS(String CAS) {
		this.CAS = CAS;
	}

	public ArrayList<Chemical> getTransformationProducts() {
		return transformationProducts;
	}

	public void setTransformationProducts(ArrayList<Chemical> transformationProducts) {
		this.transformationProducts = transformationProducts;
	}

	public ArrayList<Chemical> getSynthesisRouteChemicals() {
		return synthesisRouteChemicals;
	}

	public void setSynthesisRouteChemicals(ArrayList<Chemical> synthesisRouteChemicals) {
		this.synthesisRouteChemicals = synthesisRouteChemicals;
	}
	
	public  void writeToFile(String identifier, String destFolder) {

		try {

//			removeEmptyFields();//save space
			
//			this.CAS = CAS;
			String fileName = identifier + ".json";
			String outputPath = destFolder + "/" + fileName;

			File file = new File(outputPath);
			file.getParentFile().mkdirs();

			FileWriter fw = new FileWriter(outputPath);
			
//			System.out.println(toJSONString());
			
			String jsonString=toJSONString();
			
			jsonString=jsonString.replace("–", "-").replace("’", "'");//TODO use StringEscapeUtils?
			
//			System.out.println(jsonString.contains(""));
			
//			String jsonString2=StringEscapeUtils.escapeJava(jsonString);
			
//			System.out.println(jsonString);
//			System.out.println("\n\n");
//			System.out.println(jsonString2);
			
			fw.write(jsonString);
			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void writeToFile(String destFolder) {
		writeToFile(CAS, destFolder);
	}
	
	public void toFlatFile(String filepath,String delimiter) {

		try {

			
			FileWriter fw=new FileWriter(filepath);

			fw.write(FlatFileRecord.getHeader(delimiter)+"\r\n");

			ArrayList<String>uniqueCAS=new ArrayList<>();

			ArrayList<String>lines=this.toStringArray(delimiter);

			if (!uniqueCAS.contains(this.CAS)) uniqueCAS.add(this.CAS);


			for (String line:lines) {
				line=line.replace("–", "-").replace("’", "'");//TODO use StringEscapeUtils?
				fw.write(line+"\r\n");
			}

			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	
	
	public static void main(String[] args) {
		
		String folder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Australia\\json files";
		String filename="91-94-1.json";
		
		Chemical chemical=Chemical.loadFromJSON(new File(folder+"\\"+filename));
		
		
		long t1=System.currentTimeMillis();
		chemical.toStringArray("|");
//		System.out.println(chemical.getScore(strCarcinogenicity).records.get(0).category);
		
		long t2=System.currentTimeMillis();
		
		System.out.println((t2-t1)/1000.0);
		
		
//		chemical.removeEmptyFields();
		
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		builder.disableHtmlEscaping();
		Gson gson = builder.create();
		String strJSON=gson.toJson(chemical);//convert back to JSON string to see if we have implemented all the needed fields
//		System.out.println(strJSON);
		
		
	}

	public String getSMILES() {
		return SMILES;
	}

	public void setSMILES(String sMILES) {
		SMILES = sMILES;
	}
}
