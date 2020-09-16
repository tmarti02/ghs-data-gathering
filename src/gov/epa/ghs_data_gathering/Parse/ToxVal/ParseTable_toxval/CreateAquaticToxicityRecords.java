package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval;

import gov.epa.api.Chemical;
import gov.epa.api.ScoreRecord;

public class CreateAquaticToxicityRecords {

	/*  Need to separate into acute and chronic.

	 	Inclusion criteria for acute aquatic toxicity:
	 	r.human_eco = "eco" and
		r.risk_assessment_class="acute" or "mortality:acute" or "growth:acute" or "reproduction:acute" or "ecotoxicity invertebrate" or "ecotoxicity plants" and
		tr.habitat = "aquatic" and
		tr.toxval_type = "LC50" or "EC50" and
		tr.toxval_units = "mg/L"

		Inclusion criteria for chronic aquatic toxicity:
		r.human_eco = "eco" and
		r.risk_assessment_class = "chronic" or "mortality:chronic" or "growth:chronic" or "reproduction:chronic" or "ecotoxicity invertebrate" or "ecotoxicity plants" and
		tr.habitat = "aquatic" and
		tr.toxval_type = "NOEC" or "LOEC" and
		tr.toxval_units = "mg/L"
		-Leora
	 */	

	
	public static void createDurationRecord(Chemical chemical, RecordToxVal tr) {

		double study_dur_in_days=-1.0;
		/* I think there is no way to make a variable blank so I made it -1, which is not ideal. */

		double study_duration_value = Double.parseDouble(tr.study_duration_value);
		/* Do I need to do this to change it from whatever format it was in into double? */

		if (tr.study_duration_units.contentEquals("day")) {
			study_dur_in_days=study_duration_value;
		} else if (tr.study_duration_units.contentEquals("week")) {
			study_dur_in_days=study_duration_value*7.0;
		} else if (tr.study_duration_units.contentEquals("month")) {
			study_dur_in_days=study_duration_value*30.0;
		} else if (tr.study_duration_units.contentEquals("year")) {
			study_dur_in_days=study_duration_value*365.0;
		} else if (tr.study_duration_units.contentEquals("hour")) {
			study_dur_in_days=study_duration_value/24.0;
		} else if (tr.study_duration_units.contentEquals("minute")) {
			study_dur_in_days=study_duration_value/1440.0;
		} else if (tr.study_duration_units.contentEquals("-")) {
			return;
		} else {
			System.out.println("unknown units="+tr.study_duration_units);
			return;
		}

	
		ScoreRecord sr=createScoreRecord(chemical, tr);//create generic score record

		sr.duration=study_dur_in_days;
		sr.durationUnits="days";
		
		
	// I added duration-based criteria based on GHS criteria,
	// which are based on OECD test guidelines:
	// Test Guideline 210, Page 13 
	// https://www.oecd-ilibrary.org/docserver/9789264203785-en.pdf?expires=1599094196&id=id&accname=guest&checksum=3DD962D873D642CBF90D56FED10E8D6E
	// 	
		
	// 96 hours = 4 days so < 5 days = acute
	// 14 days = 2 weeks so > 13 days = chronic
	// It seems a bit silly to have to create a duration record just to
	// be able to generate our own variable (study_dur_in_days)
	// but I think that's the way to do it in Java.
	
	if ((study_dur_in_days<5) &&
			(tr.toxval_type.contentEquals("LC50") ||
			 tr.toxval_type.contentEquals("EC50"))) {
		setAquaticToxAcuteScore(sr, chemical);
		chemical.scoreAcute_Aquatic_Toxicity.records.add(sr);
		
	} else if ((study_dur_in_days>13) &&
			(tr.toxval_type.contentEquals("NOEC") ||
			 tr.toxval_type.contentEquals("LOEC"))) {
		setAquaticToxChronicScore(sr, chemical);
		chemical.scoreChronic_Aquatic_Toxicity.records.add(sr);
		
	}
	}

//	static void createAquaticToxAcuteRecords(Chemical chemical, RecordToxVal tr, DurationRecord dr) {
//		ScoreRecord sr = ParseToxVal.saveToxValInfo(tr);
//		sr.duration=study_dur_in_days;
//		sr.durationUnits="days";
//		setAquaticToxAcuteScore(sr, chemical);
//		chemical.scoreAcute_Aquatic_Toxicity.records.add(sr);
//	}
//
//
//
//	static void createAquaticToxChronicRecords(Chemical chemical, RecordToxVal tr) {
//		ScoreRecord sr = ParseToxVal.saveToxValInfo(tr);
//		setAquaticToxChronicScore(sr, chemical);		
//		chemical.scoreChronic_Aquatic_Toxicity.records.add(sr);
//	}
//	
	
	private static ScoreRecord createScoreRecord(Chemical chemical, RecordToxVal tr) {
		ScoreRecord sr = ParseToxVal.saveToxValInfo(tr);
		return sr;
	}


	private static void setAquaticToxChronicScore(ScoreRecord sr, Chemical chemical) {

		sr.rationale = "route: " + sr.route + ", ";
		double dose = sr.valueMass;
		String strDose = ParseToxVal.formatDose(dose);	

		/* DfE criteria:
		 * NOEC or LOEC
		 * mg/L
		 * < 0.1 VH
		 * 0.1 - 1 H
		 * > 1 - 10 M
		 * > 10 L
		 * -Leora */

		if (sr.valueMassOperator.equals(">")) {

			if (dose >= 10) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = sr.toxval_type+" > 10 mg/L";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = sr.toxval_type+" does not provide enough information to assign a score";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			}

		} else if (sr.valueMassOperator.equals("<")) {
			if (dose <=0.1) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = sr.toxval_type+" < 0.1 mg/L";
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = sr.toxval_type+" does not provide enough information to assign a score";

				// System.out.println(chemical.CAS + "\tless than operator detected for oral\t" + dose);
			}

		} else if (sr.valueMassOperator.equals("") || sr.valueMassOperator.equals("=") || sr.valueMassOperator.equals("~") || sr.valueMassOperator.equals(">=") || sr.valueMassOperator.equals("<=")) {

			if (dose < 0.1) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = sr.toxval_type+" < 1 mg/L";
			} else if (dose >= 0.1 && dose <= 1) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = "0.1 mg/L <= "+sr.toxval_type+" <=1 mg/L";
			} else if (dose > 1 && dose <= 10) {
				sr.score = ScoreRecord.scoreM;
				sr.rationale = "1 mg/L < "+sr.toxval_type+" <=10 mg/L";
			} else if (dose > 10) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = sr.toxval_type+ " > 10 mg/L";
			} else { 
				System.out.println(chemical.CAS + "\tEcoToxChronic\t" + strDose);
			}

		}
	}


	private static void setAquaticToxAcuteScore(ScoreRecord sr, Chemical chemical) {

		sr.rationale = "route: " + sr.route + ", ";
		double dose = sr.valueMass;
		String strDose = ParseToxVal.formatDose(dose);		

		/* DfE criteria:
		 * LC50 or EC50
		 * mg/L
		 * < 1.0 VH
		 * 1 - 10 H
		 * >10 - 100 M
		 * >100 L
		 * -Leora */

		if (sr.valueMassOperator.equals(">")) {

			if (dose >= 100) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = sr.toxval_type+" > 100 mg/L";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = sr.toxval_type+" does not provide enough information to assign a score";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			}

		} else if (sr.valueMassOperator.equals("<")) {
			if (dose <=1) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = sr.toxval_type+" < 1 mg/L";
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = sr.toxval_type+" does not provide enough information to assign a score";

				// System.out.println(chemical.CAS + "\tless than operator detected for oral\t" + dose);
			}

		} else if (sr.valueMassOperator.equals("") || sr.valueMassOperator.equals("=") || sr.valueMassOperator.equals("~") || sr.valueMassOperator.equals(">=") || sr.valueMassOperator.equals("<=")) {



			if (dose < 1) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = sr.toxval_type + " < 1 mg/L";
			} else if (dose >= 1 && dose <= 10) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = "1 mg/kg <= "+sr.toxval_type+" <=10 mg/L";
			} else if (dose > 10 && dose <= 100) {
				sr.score = ScoreRecord.scoreM;
				sr.rationale = "10 mg/kg < "+sr.toxval_type+" <=100 mg/L";
			} else if (dose > 100) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = sr.toxval_type+" > 100 mg/L";
			} else { System.out.println(chemical.CAS + "\tEcoToxAcute\t" + strDose);

			}
		}
	}
}
