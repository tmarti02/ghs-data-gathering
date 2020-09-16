package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval;

import java.util.ArrayList;

import gov.epa.ghs_data_gathering.API.Chemical;
import gov.epa.ghs_data_gathering.API.ScoreRecord;

public class CreateReproductiveDevelopmentalToxicityRecords {

	/* Inclusion criteria for Developmental Toxicity:
	 All routes
	 	human_eco: human health
	 	species_common: rat, mouse, rabbit, or guinea pig
	 		(another possible choice would be to just include all mammals:
	 		 species_supercategory = mammals, but those are the typical four)
	 	toxval_type: NOAEL or LOAEL
	 Oral
	   exposure_route: oral
	   toxval_units: mg/kg-day
	 Dermal
	   exposure_route: dermal
	   toxval_units: mg/kg-day
	 Inhalation
	   exposure_route: inhalation
	   toxval_units: mg/L-day
	-Leora
	 */	



	/* toxval_type should be NOAEL or LOAEL
	 * 
	 * DfE criteria for Reproductive and Developmental Toxicity:
	 * DfE has no VH for Reproductive and Developmental Toxicity
	 * Dfe has a VL category, which will just be included in the L category here.
	 * 
	 * Route						H				M			L				(VL)
	 * Oral(mg/kg/day)				< 50		50 - 250	250 - 1000			(> 1000)
	 * Dermal (mg/kg/day)			< 100		100 - 500	> 500 - 2000		(> 2000)
	 * Inhalation (mg/L/day)
	 * 		vapor/gas				< 1			1 - 2.5		> 2.5 - 20			(> 20)
	 * 		dust/mist/fume			< 0.1		0.1 - 0.5	> 0.5 - 5			(> 5)
	 * 
	 * For inhalation, using DfE criteria for vapor/gas because that's what we did for acute toxicity.
	 * But if vapor/gas vs. dust/mist/fume is specified in ToxVal, then we should use the specific criteria.
	 * For acrylamide, data only includes oral (no inhalation data),
	 * so I need to look at the data for other chemicals.
	 * -Leora
	 * */						


	void createReproductiveDevelopmentalRecords(Chemical chemical, RecordToxVal r) {
		// System.out.println("Creating records");
		// This prints.  It works up to here.
		if (r.toxval_type.contains("NOAEL") || r.toxval_type.contains("LOAEL")) {
			//	System.out.println("NOAEL or LOAEL");
			if (r.toxval_units.contentEquals("mg/kg-day") && r.exposure_route.contentEquals("oral")) {
				createOralRecord(chemical, r);	
				//		System.out.println("creating RepDev oral record");
			} else if(r.toxval_units.contentEquals("mg/kg-day") && r.exposure_route.contentEquals("dermal")) {
				createDermalRecord(chemical, r);
			} else if(r.toxval_units.contentEquals("mg/L") && r.exposure_route.contentEquals("inhalation")) {
				createInhalationRecord(chemical, r);
//				System.out.println("creating rep/dev inhalation record");
			}
		}
	}


	private void createOralRecord(Chemical chemical, RecordToxVal tr) {
		//		System.out.println("*Creating Oral Record");

		
		if(!CreateAcuteMammalianToxicityRecords.isOkMammalianSpecies(tr)) return;
		
//		Create an instance of a class to call a non-static method:
//		CreateAcuteMammalianToxicityRecords bob=new CreateAcuteMammalianToxicityRecords();
//		bob.[Method Name]
		
//		ArrayList<String> okSpecies = new ArrayList<String>();
//		okSpecies.add("mouse");// 27796
//		okSpecies.add("rat");// 13124
//		okSpecies.add("rabbit");// 1089
//		okSpecies.add("guinea pig");// 970
//		okSpecies.add("house mouse");
//		okSpecies.add("mouse / rat");
//		okSpecies.add("cottontail rabbit");
//		okSpecies.add("white-footed mouse");
//		okSpecies.add("norway rat");
//
//		if (!okSpecies.contains(tr.species_common))
//			return;

		ScoreRecord sr=ParseToxVal.saveToxValInfo(tr);
		setOralScore(sr, chemical);

		addRecord(chemical, tr, sr);

		//		if (tr.risk_assessment_class.contentEquals("developmental") ||
		//			tr.risk_assessment_class.contentEquals("developmental neurotoxicity")) {
		//			chemical.scoreDevelopmental.records.add(sr);
		//		} else if (tr.risk_assessment_class.contentEquals("reproductive")) {
		//			chemical.scoreReproductive.records.add(sr);
		//		}
		//			
		//	}


	}



 void addRecord (Chemical chemical, RecordToxVal tr, ScoreRecord sr) {
		if (tr.study_duration_class.toLowerCase().contains("reproduct") ||
				tr.study_duration_class.toLowerCase().contains("multigeneration") ||
				tr.toxval_subtype.toLowerCase().contains("reproduct") ||
				tr.toxval_subtype.toLowerCase().contains("multigeneration") ||
				tr.study_type.toLowerCase().contains("reproduct") ||
				tr.study_type.toLowerCase().contains("multigeneration") ||
				tr.critical_effect.toLowerCase().contains("reproduct") ||
				tr.critical_effect.toLowerCase().contains("multigeneration")) {
			chemical.scoreReproductive.records.add(sr);
		//	System.out.println("add reproductive sr");
		} else if (tr.study_duration_class.toLowerCase().contains("developmental") ||
				tr.toxval_subtype.toLowerCase().contains("developmental") ||
				tr.study_type.toLowerCase().contains("developmental") ||
				tr.critical_effect.toLowerCase().contains("developmental")) {
		//	System.out.println("Adding Developmental Score");
			chemical.scoreDevelopmental.records.add(sr);
		}
	}



	private static void setOralScore(ScoreRecord sr, Chemical chemical) {

		//		System.out.println("setting oral score");

		sr.rationale = "route: " + sr.route + ", ";
		double dose = sr.valueMass;
		String strDose = ParseToxVal.formatDose(dose);

		//				System.out.println(chemical.CAS+"\t"+strDose);					
		//		System.out.println("****"+strDose);	


		if (sr.valueMassOperator.equals(">")) {

			if (dose >= 250) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Oral POD ( > " + strDose + " mg/kg) > 250 mg/kg";
				//				System.out.println(chemical.CAS+"\t"+sr.rationale);
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = "Oral POD ( > " + strDose
						+ " mg/kg) does not provide enough information to assign a score";
				//				System.out.println(chemical.CAS+"\t"+sr.rationale);
			}


		} else if (sr.valueMassOperator.equals("<")) {
			if (dose <=50) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = "Oral POD ( < " + strDose + " mg/kg) < 50 mg/kg";
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = "Oral POD ( < " + strDose
						+ " mg/kg) does not provide enough information to assign a score";

				//				System.out.println(chemical.CAS + "\tless than operator detected for oral\t" + dose);
			}

		} else if (sr.valueMassOperator.equals("") || sr.valueMassOperator.equals("=") || sr.valueMassOperator.equals("~") || sr.valueMassOperator.equals("=") || sr.valueMassOperator.equals("~")
				|| sr.valueMassOperator.equals(">=") || sr.valueMassOperator.equals("<=")){

			//				System.out.println("Operator ok");


			if (dose < 50) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = "Oral POD" + " (" + strDose + " mg/kg) < 50 mg/kg";
			} else if (dose >= 50 && dose <= 250) {
				sr.score = ScoreRecord.scoreM;
				sr.rationale = "50 mg/kg <= Oral POD (" + strDose + " mg/kg) <=250 mg/kg";
			} else if (dose > 250) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Oral POD" + "(" + strDose + " mg/kg) > 250 mg/kg";
			} else { 
				//				System.out.println(chemical.CAS + "\toral\t" + strDose);			 
			}		
		}
	}

	private void createDermalRecord(Chemical chemical, RecordToxVal tr) {
		//		System.out.println("Creating Dermal Record");

		if(!CreateAcuteMammalianToxicityRecords.isOkMammalianSpecies(tr)) return;
		ScoreRecord sr =ParseToxVal.saveToxValInfo(tr);
		setDermalScore(sr, chemical);
		
		addRecord(chemical, tr, sr);


	}


	private static void setDermalScore(ScoreRecord sr, Chemical chemical) {

		sr.rationale = "route: " + sr.route + ", ";
		double dose = sr.valueMass;
		String strDose = ParseToxVal.formatDose(dose);


		if (sr.valueMassOperator.equals(">")) {

			if (dose >= 500) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Dermal POD ( > " + strDose + " mg/kg) > 500 mg/kg";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = "Dermal POD ( > " + strDose
						+ " mg/kg) does not provide enough information to assign a score";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			}


		} else if (sr.valueMassOperator.equals("<")) {
			if (dose <=100) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = "Dermal POD ( < " + strDose + " mg/kg) < 100 mg/kg";
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = "Dermal POD ( < " + strDose
						+ " mg/kg) does not provide enough information to assign a score";

				System.out.println(chemical.CAS + "\tless than operator detected for oral\t" + dose);
			}

		} else if (sr.valueMassOperator.equals("") || sr.valueMassOperator.equals("=") || sr.valueMassOperator.equals("~") || sr.valueMassOperator.equals("=") || sr.valueMassOperator.equals("~")
				|| sr.valueMassOperator.equals(">=") || sr.valueMassOperator.equals("<=")){	

			if (dose < 100) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = "Dermal POD" + " (" + strDose + " mg/kg) < 100 mg/kg";
			} else if (dose >= 100 && dose <= 500) {
				sr.score = ScoreRecord.scoreM;
				sr.rationale = "100 mg/kg <= Dermal POD (" + strDose + " mg/kg) <=500 mg/kg";
			} else if (dose > 500) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Dermal POD" + "(" + strDose + " mg/kg) > 500 mg/kg";
			}
		}
	}



	private void createInhalationRecord(Chemical chemical, RecordToxVal tr){
		// System.out.println("Creating Inhalation Record");


		if(!CreateAcuteMammalianToxicityRecords.isOkMammalianSpecies(tr)) return;


		ScoreRecord sr = ParseToxVal.saveToxValInfo(tr);		
		setInhalationScore(sr, chemical);

		if (sr.score==null)
			return;

		addRecord(chemical, tr, sr);

	}

	private static void setInhalationScore(ScoreRecord sr, Chemical chemical) {

		sr.rationale = "route: " + sr.route + ", ";
		double dose = sr.valueMass;
		String strDose = ParseToxVal.formatDose(dose);

		//					System.out.println(chemical.CAS+"\t"+strDose);

		//					System.out.println("****"+strDose);					


		if (sr.valueMassOperator.equals(">")) {

			if (dose >= 2.5) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Inhalation POD ( > " + strDose + " mg/kg) > 2.5 mg/kg";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = "Inhalation POD ( > " + strDose
						+ " mg/kg) does not provide enough information to assign a score";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			}


		} else if (sr.valueMassOperator.equals("<")) {
			if (dose <=1) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = "Inhalation POD ( < " + strDose + " mg/kg) < 1 mg/kg";
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = "Inhalation POD ( < " + strDose
						+ " mg/kg) does not provide enough information to assign a score";

				System.out.println(chemical.CAS + "\tless than operator detected for oral\t" + dose);
			}

		} else if (sr.valueMassOperator.equals("") || sr.valueMassOperator.equals("=") || sr.valueMassOperator.equals("~") || sr.valueMassOperator.equals(">=") || sr.valueMassOperator.equals("<=")) {





			if (dose < 1) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = "Inhalation POD" + " (" + strDose + " mg/L) < 1 mg/L";
			} else if (dose >= 1 && dose <= 2.5) {
				sr.score = ScoreRecord.scoreM;
				sr.rationale = "1 mg/L <= Inhalation POD (" + strDose + " mg/L) <=2.5 mg/L";
			} else if (dose > 2.5) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Inhalation POD" + "(" + strDose + " mg/L) > 2.5 mg/L";
				/*
				 * } else { System.out.println(chemical.CAS + "\toral\t" + strDose);
				 */
			}
		}		
	}
}
