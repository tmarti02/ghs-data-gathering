package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval;

import java.util.ArrayList;

import gov.epa.api.Chemical;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;

public class CreateOrganOrSystemicToxRecords {


	/* I want to convert this Stata code to Java code:

	 gen study_dur_in_days=.
	 replace study_dur_in_days=study_duration_value if study_duration_units=="day"
	 replace study_dur_in_days=study_duration_value*7 if study_duration_units=="week"
	 replace study_dur_in_days=study_duration_value*30 if study_duration_units=="month"
	 replace study_dur_in_days=study_duration_value*365 if study_duration_units=="year"
	 replace study_dur_in_days=study_duration_value/24 if study_duration_units=="hour"
	 replace study_dur_in_days=study_duration_value/1440 if study_duration_units=="minute"

	 Here is my attempt...

	 */

	public static void createDurationRecord(Chemical chemical, RecordToxVal tr) {


		if(!CreateAcuteMammalianToxicityRecords.isOkMammalianSpecies(tr)) return;

		if (!tr.toxval_type.contentEquals("NOAEL") && !tr.toxval_type.contentEquals("LOAEL")) {
			return;//not a valid record for systemic tox
		}

		double study_dur_in_days = getStudyDurationDays(tr);

		if (study_dur_in_days<0) return;

		Score score=null;

		if(tr.study_type.toLowerCase().contains("neuro") ||	isNeuroCriticalEffect(tr)) {
			if (tr.study_type.contentEquals("single limit dose") ||
					tr.study_duration_class.contentEquals("single dose")) {	
				score=chemical.scoreNeurotoxicity_Single_Exposure;
			} else; {
				score=chemical.scoreNeurotoxicity_Repeat_Exposure;
			}
		} else {
			if (tr.study_type.contentEquals("single limit dose") ||
					tr.study_duration_class.contentEquals("single dose")) {	
				score=chemical.scoreSystemic_Toxicity_Single_Exposure;
			} else; {
				score=chemical.scoreSystemic_Toxicity_Repeat_Exposure;
			}
		}
//  Note that "Systemic Toxicity" actually could be Organ or Systemic Toxicity.		

		
		ScoreRecord sr = ParseToxVal.saveToxValInfo(score,tr);		
		sr.duration=study_dur_in_days;
		sr.durationUnits="days";

		//		System.out.println("duration="+sr.duration+" days");


		if ((tr.exposure_route.contentEquals("oral") || tr.toxval_subtype.toLowerCase().contains("oral"))
				&& tr.toxval_units.contentEquals("mg/kg-day")) {
			/* if (study_dur_in_days <= 91.0 && study_dur_in_days >= 89.0) { 
			   Broadening the range to be more inclusive (90 + or - 5).
			   Also switching the order for more logical reading. */

			if (study_dur_in_days >= 725.0 && study_dur_in_days <= 735.0) {
			//	System.out.println("2 year oral");
				setScore(sr, chemical, "2 year", "Oral", 1.25, 12.5);
				
			} else if (study_dur_in_days >= 360.0 && study_dur_in_days <= 370.0) {
			//	System.out.println("1 year oral");
				setScore(sr, chemical, "1 year", "Oral", 2.5, 25);
				
			} else if (study_dur_in_days >= 175.0 && study_dur_in_days <= 185.0) {
				setScore(sr, chemical, "6 month", "Oral", 5, 50);

			} else if (study_dur_in_days >= 85.0 && study_dur_in_days <= 95.0)	{				
				setScore(sr, chemical, "3 months", "Oral", 10, 100);

			} else if (study_dur_in_days >= 40.0 && study_dur_in_days <= 50.0) {
				/*All three of these time categories now have a range of 10. */		
				setScore(sr, chemical, "40 - 50 day", "Oral", 20, 200);

				/* } else if (study_dur_in_days <= 31.0 && study_dur_in_days >= 27.0) {
			   Broadening the range to be more inclusive (28 + or - 5)*/
			} else if (study_dur_in_days >= 23.0 && study_dur_in_days <= 33.0) {
				setScore(sr, chemical, "1 month", "Oral", 30, 300);
			}


		} else if ((tr.exposure_route.contentEquals("dermal")  || tr.toxval_subtype.toLowerCase().contains("dermal"))
				&& tr.toxval_units.contentEquals("mg/kg-day")) {
			/* if (study_dur_in_days <= 91.0 && study_dur_in_days >= 89.0) {
				Broadening the range to be more inclusive (90 + or - 5).
				Also switching the order for more logical reading. */

			if (study_dur_in_days >= 725.0 && study_dur_in_days <= 735.0)	{				
				setScore(sr, chemical, "2 year", "Dermal", 2.5, 25);
				
			} else if (study_dur_in_days >= 360.0 && study_dur_in_days <= 370.0) {
				setScore(sr, chemical, "1 year", "Dermal", 5, 50);	
				
			} else if (study_dur_in_days >= 175.0 && study_dur_in_days <= 185.0) {
				setScore(sr, chemical, "6 month", "Dermal", 10, 100);	

			} else if (study_dur_in_days >= 85.0 && study_dur_in_days <= 95.0) {
				/*Got error when tried to use "=". It said "<=" was expected.*/							
				setScore(sr, chemical, "3 month", "Dermal", 20, 200);

			} else if (study_dur_in_days >= 40.0 && study_dur_in_days <= 50.0) {
				setScore(sr, chemical, "40 - 50 day", "Dermal", 40, 400);
				/* } else if (study_dur_in_days <= 31.0 && study_dur_in_days >= 27.0) {
			Broadening the range to be more inclusive (28 + or - 5)*/
			} else if (study_dur_in_days >= 23.0 && study_dur_in_days <= 33.0) {
				setScore(sr, chemical, "1 month", "Dermal", 60, 600);
			}

		} else if ((tr.exposure_route.contentEquals("inhalation") || tr.toxval_subtype.toLowerCase().contains("inhalation"))
				&& (tr.toxval_units.contentEquals("mg/L") || tr.toxval_units.contentEquals("mg/m3"))) {

			if (tr.toxval_units.contentEquals("mg/m3")){

				// change value and units
				// 1 mg/L = 1000 mg/m3

				double toxval_numeric2 = Double.parseDouble(tr.toxval_numeric)/1000.0;
				tr.toxval_numeric = toxval_numeric2 + "";
				tr.toxval_units = "mg/L (converted from mg/m3)";
			}


			if (study_dur_in_days >= 725.0 && study_dur_in_days <= 735.0)	{				
				setScore(sr, chemical, "2 year", "Inhalation", 0.025, 0.125);
				
			} else if (study_dur_in_days >= 360.0 && study_dur_in_days <= 370.0) {
				setScore(sr, chemical, "1 year", "Inhalation", 0.05, 0.25);
				
			} else if (study_dur_in_days >= 175.0 && study_dur_in_days <= 185.0) {
				setScore(sr, chemical, "6 month", "Inhalation", 0.1, 0.5);

				/* if (study_dur_in_days <= 91.0 && study_dur_in_days >= 89.0) {
			Broadening the range to be more inclusive (90 + or - 5).
			Also switching the order for more logical reading. */
			} else if (study_dur_in_days >= 85.0 && study_dur_in_days <= 95.0) {
				/*Got error when tried to use "=". It said "<=" was expected.*/			
				setScore(sr, chemical, "3 month", "Inhalation", 0.2, 1);

			} else if (study_dur_in_days >= 40.0 && study_dur_in_days <= 50.0) {
				setScore(sr, chemical, "40 - 50 day", "Inhalation", 0.4, 2);
				/* } else if (study_dur_in_days <= 31.0 && study_dur_in_days >= 27.0) {
			Broadening the range to be more inclusive (28 + or - 5)*/
			} else if (study_dur_in_days >= 23.0 && study_dur_in_days <= 33.0) {
				setScore(sr, chemical, "1 month", "Inhalation", 0.6, 3);
			}

		}



		if (tr.study_type.contentEquals("single limit dose") ||
				tr.study_duration_class.contentEquals("single dose")) {
			if (tr.toxval_units.contentEquals("mg/kg") &&
					tr.exposure_route.contentEquals("oral")) {
				//	study_dur_in_days >= 0.0 && study_dur_in_days <= 1.0) {		

				setSingleDoseScore(sr, chemical, 300, 2000, 3000, "Oral");//Leora- is this correct?

			} else if (tr.toxval_units.contentEquals("mg/kg") &&
					tr.exposure_route.contentEquals("dermal")) {	

				setSingleDoseScore(sr, chemical, 1000, 2000, 3000, "Dermal");


			} else if ((tr.toxval_units.contentEquals("mg/L") ||
					tr.toxval_units.contentEquals("mg/m3")) &&
					tr.exposure_route.contentEquals("inhalation")) {
				setSingleDoseScore(sr, chemical, 10, 20, 30, "Inhalation");
			}
		}


		if (sr.score!=null) score.records.add(sr);//if we were able to set the score, add record to list

		//		System.out.println(sr.scoreToInt());


	}


	private static double getStudyDurationDays(RecordToxVal tr) {
		double study_duration_value = Double.parseDouble(tr.study_duration_value);

		double study_dur_in_days=-1.0;


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
			return -1;
		} else {
			System.out.println("unknown units="+tr.study_duration_units);
			return -1;
		}

		return study_dur_in_days;
	}


	public static boolean isNeuroCriticalEffect(RecordToxVal tr) {
		String ce=tr.critical_effect;
		//more keywords added
		if (ce.contains("ataxia") || 
				ce.contains("brain") ||
				ce.contains("cholinesterase") ||
				ce.contains("CNS") ||
				ce.contains("COMA") ||
				ce.contains("convulsions") ||
				ce.contains("decreased retention (memory)") ||
				ce.contains("demyelination") ||
				ce.contains("HALLUCINATIONS") ||
				ce.contains("headache, dizziness, weakness") ||
				ce.contains("impaired reflex") ||
				ce.contains("jerking movements") ||
				ce.contains("motor and sensory function") ||
				ce.contains("nerve") ||
				ce.contains("NERVOUS SYSTEM") ||
				ce.contains("neuritis")||
				ce.contains("neuro")||
				ce.contains("paralysis") ||
				ce.contains("Psychomotor") ||
				ce.contains("seizure") ||
				ce.contains("SENSE ORGANS") ||
				ce.contains("Spinal cord") ||
				ce.contains("TOXIC PSYCHOSIS") ||
				ce.contains("tremor") ) {
			return true;
		} else {
			return false;
		}
	}



	public static void setScore(ScoreRecord sr, Chemical chemical, String duration,
			String route, double dose1, double dose2) {


		double dose = sr.valueMass;
		String strDose = ParseToxVal.formatDose(dose);
		String units="mg/kg-day";

		if (sr.valueMassOperator.equals(">")) {
			if (dose >= dose2) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = duration +" "+route+" "+sr.testType+" > "+dose2+" "+units;
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = duration +" "+route+" "+sr.testType+" does not provide enough information to assign a score";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			}

		} else if (sr.valueMassOperator.equals("<")) {
			if (dose <=dose1) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = duration +" "+route+" "+sr.testType+" < "+dose1+" "+units;
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = duration +" "+route+" "+sr.testType+" does not provide enough information to assign a score";

				// System.out.println(chemical.CAS + "\tless than operator detected for oral\t" + dose);
			}

		} else if (sr.valueMassOperator.equals("") || sr.valueMassOperator.equals("=") || sr.valueMassOperator.equals("~") || sr.valueMassOperator.equals(">=") || sr.valueMassOperator.equals("<=")) {

			if (dose < dose1) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = duration +" "+route+" "+sr.testType+" < "+dose1+" "+units;
			} else if (dose >= dose1 && dose <= dose2) {
				sr.score = ScoreRecord.scoreM;
				sr.rationale = dose1+" " +units+" < "+duration +" "+route+" "+sr.testType+" <= "+dose2+" "+units;
			} else if (dose > dose2) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = duration +" "+route+" "+sr.testType + " > "+dose2+" "+units;
			} else { 
				System.out.println(chemical.CAS + "\t"+duration+" "+route+"\t" + strDose);
			}
		}
	}





	private static void setSingleDoseScore(ScoreRecord sr, Chemical chemical, double dose1,
			double dose2, double dose3, String route) {

		String units="mg/kg";

		double dose = sr.valueMass;
		String strDose = ParseToxVal.formatDose(dose);

		if (sr.valueMassOperator.equals(">")) {

			if (dose >= dose3) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Single Dose "+route+" "+sr.testType+" > "+dose3+" "+units;
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = "Single Dose "+route+" "+sr.testType+" does not provide enough information to assign a score";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			}


		} else if (sr.valueMassOperator.equals("<")) {
			if (dose <=dose1) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = "Single Dose "+route+" "+sr.testType+" < "+dose1+" "+units;
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = "Single Dose "+route+" "+sr.testType+" does not provide enough information to assign a score";

				// System.out.println(chemical.CAS + "\tless than operator detected for oral\t" + dose);
			}

		} else if (sr.valueMassOperator.equals("") || sr.valueMassOperator.equals("=") || sr.valueMassOperator.equals("~") || sr.valueMassOperator.equals(">=") || sr.valueMassOperator.equals("<=")) {


			if (dose <= dose1) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = "Single Dose "+route+" "+sr.testType+" < "+dose1+" "+units;
			} else if (dose > 300 && dose <= 2000) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = dose1+" "+units+" < Single Dose "+route+" "+sr.testType+" <= "+dose2+" "+units;
			} else if (dose > 2000 && dose <= 3000) {
				sr.score = ScoreRecord.scoreM;
				sr.rationale = dose2+" "+units+" < Single Dose "+route+" "+sr.testType+" <= "+dose3+" "+units;
			} else if (dose > 3000) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Single Dose "+route+" "+sr.testType+" > "+dose3+" "+units;

			} else { 
				System.out.println(chemical.CAS + "\tSingleDoseOral\t" + strDose);
			}
		}
	}


}

