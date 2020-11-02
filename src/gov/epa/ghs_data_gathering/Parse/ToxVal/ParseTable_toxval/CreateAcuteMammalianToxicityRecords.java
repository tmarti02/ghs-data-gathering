package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval;

import java.util.ArrayList;

import gov.epa.api.Chemical;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;

/* Inclusion criteria for Acute Mammalian Toxicity:
 All routes
 	human_eco: human health
 	species_common: rat, mouse, rabbit, or guinea pig
 		(another possible choice would be to just include all mammals:
 		 species_supercategory = mammals, but those are the typical four)
 Oral
   exposure_route: oral
   toxval_type: LD50
   toxval_units: mg/kg
 Dermal
   exposure_route: dermal
   toxval_type: LD50
   toxval_units: mg/kg
 Inhalation
   exposure_route: inhalation
   toxval_type: LC50
   toxval_units: mg/L or mg/m3
-Leora
 */

public class CreateAcuteMammalianToxicityRecords {

	static void createAcuteMammalianToxicityRecords(Chemical chemical, RecordToxVal r) {
		if((r.exposure_route.contentEquals("oral") ||
				r.toxval_subtype.toLowerCase().contains("oral")) &&
				r.toxval_type.contentEquals("LD50") &&
				r.toxval_units.contentEquals("mg/kg") &&
				r.human_eco.contentEquals("human health")) {
			createAcuteMammalianToxicityOralRecord(chemical, r);						
		} else if((r.exposure_route.contentEquals("dermal") ||
				r.toxval_subtype.toLowerCase().contains("dermal")) &&
				r.toxval_type.contentEquals("LD50") &&
				r.toxval_units.contentEquals("mg/kg") &&
				r.human_eco.contentEquals("human health")) {
			createAcuteMammalianToxicityDermalRecord(chemical, r);
		} else if((r.exposure_route.contentEquals("inhalation") ||
				r.toxval_subtype.toLowerCase().contains("inhalation")) &&
				(r.toxval_units.contentEquals("mg/L") ||
						r.toxval_units.contentEquals("mg/m3")) &&
				r.toxval_type.contentEquals("LC50") &&
				r.human_eco.contentEquals("human health")){
			createAcuteMammalianToxicityInhalationRecord(chemical, r);
		}

		/* 1 mg/L = 1000 mg/m3
		 * need to add code for mg/m3
		 */

		/*} else if((r.toxval_units.contentEquals("mg/L") || r.toxval_units.contentEquals("mg/m3")) &&
				r.exposure_route.contentEquals("inhalation") &&
				r.toxval_type.contentEquals("LC50") &&
				r.human_eco.contentEquals("human health")){
			createAcuteMammalianToxicityInhalationRecord(chemical, r);
			}*/
	}




	/* I added criteria here (in the code above).  Its easier to see the criteria here
	 towards the top of the code. */


	private static void setOralScore(ScoreRecord sr, Chemical chemical) {
		double dose = sr.valueMass;
		String strDose = ParseToxVal.formatDose(dose);

		// System.out.println("setting oral score");

		// System.out.println(chemical.CAS+"\t"+dose+"\t"+strDose);

		if (sr.valueMassOperator.equals(">")) {

			if (dose >= 2000) {// >2000
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Oral LD50 > 2000 mg/kg";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = "Oral LD50 does not provide enough information to assign a score";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			}

		} else if (sr.valueMassOperator.equals("<")) {
			if (dose <=50) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = "Oral LD50 < 50 mg/kg";
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = "Oral LD50 does not provide enough information to assign a score";

				// System.out.println(chemical.CAS + "\tless than operator detected for oral\t" + dose);
			}

		} else if (sr.valueMassOperator.equals("") || sr.valueMassOperator.equals("=") || sr.valueMassOperator.equals("~") || sr.valueMassOperator.equals(">=") || sr.valueMassOperator.equals("<=")) {

			if (dose <= 50) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = "Oral LD50 <= 50 mg/kg";
			} else if (dose > 50 && dose <= 300) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = "50 mg/kg < Oral LD50 <=300 mg/kg";
			} else if (dose > 300 && dose <= 2000) {
				sr.score = ScoreRecord.scoreM;
				sr.rationale = "300 mg/kg < Oral LD50 <=2000 mg/kg";
			} else if (dose > 2000) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Oral LD50 > 2000 mg/kg";
			} else {
				System.out.println(chemical.CAS + "\toral\t" + strDose);
			}
		} else {
			System.out.println("Unknown operator: "+sr.valueMassOperator+" for acute oral");
		}
	}


	private static void setDermalScore(ScoreRecord sr, Chemical chemical) {
		double dose = sr.valueMass;

		String strDose = ParseToxVal.formatDose(dose);

		if (sr.valueMassOperator.equals(">")) {

			if (dose >= 2000) {// >2000
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Dermal LD50 > 2000 mg/kg";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = "Dermal LD50 does not provide enough information to assign a score";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			}

		} else if (sr.valueMassOperator.equals("<")) {
			if (dose <= 200) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = "Dermal LD50 <= 200 mg/kg";
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = "Dermal LD50 does not provide enough information to assign a score";	
				System.out.println(chemical.CAS + "\tless than operator detected for dermal\t" + dose);
			}

		} else if (sr.valueMassOperator.equals("") || sr.valueMassOperator.equals("=") || sr.valueMassOperator.equals("~") || sr.valueMassOperator.equals(">=") || sr.valueMassOperator.equals("<=")) {
			/*		if (dose <= 200) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = "Dermal LD50 + (" + strDose + " mg/kg) <= 200 mg/kg";*/
			if (dose <= 200) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = "Dermal LD50 <= 200 mg/kg";
			} else if (dose > 200 && dose <= 1000) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = "200 mg/kg < Dermal LD50 <=1000 mg/kg";
			} else if (dose > 1000 && dose <= 2000) {
				sr.score = ScoreRecord.scoreM;
				sr.rationale = "1000 mg/kg < Dermal LD50 <=2000 mg/kg";
			} else if (dose > 2000) {// >2000
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Dermal LD50 > 2000 mg/kg";
			} else {
				System.out.println(chemical.CAS + "\tDermal\t" + strDose);
			}
		} else {
			System.out.println("Unknown operator: "+sr.valueMassOperator+" for acute dermal");
		}

	}



	private static void setInhalationScore(ScoreRecord sr, Chemical chemical) {

		sr.rationale = "route: " + sr.route + ", ";
		double dose = sr.valueMass;
		String strDose = ParseToxVal.formatDose(dose);

		// System.out.println(chemical.CAS+"\t"+strDose);

		// System.out.println("****"+strDose);

		if (sr.valueMassOperator.equals(">")) {

			if (dose >= 20) {// >20
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Inhalation LC50 > 20 mg/L";
			//	System.out.println(chemical.CAS+"\t"+sr.rationale);
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = "Inhalation LC50 does not provide enough information to assign a score";
			//	System.out.println(chemical.CAS+"\t"+sr.rationale);
			}

		} else if (sr.valueMassOperator.equals("<")) {
			if (dose <= 2) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = "Inhalation LC50 < 2 mg/L";
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = "Inhalation LC50 does not provide enough information to assign a score";
				System.out.println(chemical.CAS + "\tless than operator detected for inhalation\t" + dose);
			}
			/* Need to fix the code for these operators.
   Need to add code for "<".
   Also, need to add code for ">=" and "<=".
   I think we should do:
   If "<", if toxval_numeric is less than the cutoff for VH then VH
 		else N/A.
   If ">=" or "<=" treat it as if it were "=".
			 */

		} else if (sr.valueMassOperator.equals("") || sr.valueMassOperator.equals("=") || sr.valueMassOperator.equals("~") || sr.valueMassOperator.equals(">=") || sr.valueMassOperator.equals("<=")) {
			if (dose <= 2) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = "Inhalation LC50 <= 2  mg/L";
			//	System.out.println("inhalation VH");
			} else if (dose > 2 && dose <= 10) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = "2 mg/L < Inhalation LC50 <=10 mg/L";
			//	System.out.println("inhalation H");
			} else if (dose > 10 && dose <= 20) {
				sr.score = ScoreRecord.scoreM;
				sr.rationale = "10 mg/L < Inhalation LC50 <=20 mg/L";
			//	System.out.println("inhalation M");
			} else if (dose > 20) {// >20
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Inhalation LC50 > 20 mg/L";
			//	System.out.println("inhalation L");
			} else {
				System.out.println(chemical.CAS + "\tinhalation\t" + dose);
			}


		} else {
			System.out.println("Unknown operator: "+sr.valueMassOperator+" for acute inhalation");
		}
	}








	private static void createAcuteMammalianToxicityOralRecord(Chemical chemical, RecordToxVal tr) {
		// System.out.println("Creating AcuteMammalianToxicityOralRecord");


		if (!tr.toxval_type.contentEquals("LD50")) { 
			//			System.out.println("invalid oral toxval_type="+tr.toxval_type);
			return;
		}


		if(!isOkMammalianSpecies(tr)) return;


		// System.out.println(chemical.CAS+"\t"+tr.ReportedDose+"\t"+tr.NormalizedDose);

		Score score=chemical.scoreAcute_Mammalian_ToxicityOral;
		
		ScoreRecord sr = ParseToxVal.saveToxValInfo(score,tr);
		sr.hazardName=score.hazard_name;
		
		setOralScore(sr, chemical);
		score.records.add(sr);

	}

	private static void createAcuteMammalianToxicityDermalRecord(Chemical chemical, RecordToxVal tr) {
		// System.out.println("Creating AcuteMammalianToxicityDermalRecord");
		if (!tr.toxval_type.contentEquals("LD50")) { 
			//			System.out.println("invalid dermal toxval_type="+tr.toxval_type);
			return;
		}

		/*
		 * EPA Health Effects Test Guidelines OPPTS 870.1200 Acute Dermal Toxicity: "The
		 * rat, rabbit, or guinea pig may be used. The albino rabbit is preferred
		 * because of its size, ease of handling, skin permeability, and extensive data
		 * base. Commonly used laboratory strains should be employed. If a species other
		 * than rats, rabbits, or guinea pigs is used, the tester should provide
		 * justification and reasoning for its selection.
		 */

		if(!isOkMammalianSpecies(tr)) return;
			
		Score score=chemical.scoreAcute_Mammalian_ToxicityDermal;
		ScoreRecord sr =ParseToxVal.saveToxValInfo(score,tr);
		sr.hazardName=score.hazard_name;
		setDermalScore(sr, chemical);
		score.records.add(sr);

	}


	public static boolean isOkMammalianSpecies (RecordToxVal tr) {
		if (tr.species_common.toLowerCase().contains("rabbit")) return true;
		if (tr.species_common.toLowerCase().contains("rat")) return true;
		if (tr.species_common.toLowerCase().contains("mouse")) return true;
		if (tr.species_common.toLowerCase().contains("guinea pig")) return true;
		return false;
	}

	private static void createAcuteMammalianToxicityInhalationRecord(Chemical chemical, RecordToxVal tr) {
		//		System.out.println("Creating AcuteMammalianToxicityInhalationRecord");

		if (tr.toxval_units.contentEquals("mg/m3")){

			// change value and units
			/* 1 mg/L = 1000 mg/m3
			 * need to add code for mg/m3
			 */

			double toxval_numeric2 = Double.parseDouble(tr.toxval_numeric)/1000.0;
			tr.toxval_numeric = toxval_numeric2 + "";
			tr.toxval_units = "mg/L (converted from mg/m3)";

		}

		//TODO - do we only want to use LC50? I know richard might not restrict to LC50s

		/*
		 * The Tiger Team didn't use acute tox by itself, only HER/BER/TER. Richard's
		 * code doesn't specify LC50.
		 * -Leora 4/24/20
		 */

		// if (!tr.toxval_type.contentEquals("LC50")) { 
		//			System.out.println("invalid inhalation toxval_type="+tr.toxval_type);
		//	return;

		/* So this prints the toxval_type if it isn't LC50. But I think we need to add
			 code to specify that the toxval_type needs to be LC50 and units need to be mg/L
			 for inhalation, and for oral and dermal, need LD50 and mg/kg.
		 */


		/*
		 * EPA Health Effects Test Guidelines OPPTS 870.1300 Acute Inhalation Toxicity:
		 * "Although several mammalian test species may be used, the preferred species
		 * is the rat. Commonly used laboratory strains should be employed. If another
		 * mammalian species is used, the tester should provide justification and
		 * reasoning for its selection."
		 */




		/* Okay, I understand the code.  This basically renames what Richard called toxval_numeric
		 * into valueMass and then valueMass is renamed score,
		 * so then for AcuteMammalianToxicity, the same code that we used for the
		 * AA Dashboard/CHA Database is directly used.  -Leora 4/23/20  */



	

		/* It looks like Richard's code doesn't mention species for acute tox.
		 * So presumably all mammalian species in the ToxVal database were included for the Tiger Team.
		 * 
		 * But other work specified species. For example,
		 * In Grace's Science Webinar presentation on 4/22/20, she talked about using ToxVal data to develop TTC.
		 * She filtered from ToxVal:
		 * toxval type: NO(A)EL or NO(A)EC
		 * species: rats, mice, rabbits
		 * To derive representative values, she removed outliers that exceeded the IQR.
		 * 
		 * For acrylamide, the only mammals with data for acute toxicity are the same mammals that we included
		 * (rat, mouse, rabbit, guinea pig).
		 * Need to check whether this is the case for all chemicals.
		 * 
		 * I think the more important thing for the code is to specify that the species_supercategory is mammals.
		 * Need to add code to do this.
		 * 
		 * -Leora 4/23/20 */

		if(!isOkMammalianSpecies(tr)) return;

		Score score=chemical.scoreAcute_Mammalian_ToxicityInhalation;
		ScoreRecord sr =ParseToxVal.saveToxValInfo(score,tr);
		sr.hazardName=score.hazard_name;
		setInhalationScore(sr, chemical);
		score.records.add(sr);
	

	}



}
