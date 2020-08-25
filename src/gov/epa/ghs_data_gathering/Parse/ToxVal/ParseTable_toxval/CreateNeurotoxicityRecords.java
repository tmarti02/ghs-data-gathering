package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval;
//
import java.util.ArrayList;
//
import gov.epa.ghs_data_gathering.API.Chemical;
import gov.epa.ghs_data_gathering.API.ScoreRecord;
//
public class CreateNeurotoxicityRecords {
	
}

//	Now including Neurotox in the CreateOrganOrSystemicToxRecords class.


//	public static void createDurationRecord(Chemical chemical, RecordToxVal tr) {
//
//		double study_dur_in_days=-1.0;
//		/* I think there is no way to make a variable blank so I made it -1, which is not ideal. */
//
//		double study_duration_value = Double.parseDouble(tr.study_duration_value);
//		/* Do I need to do this to change it from whatever format it was in into double? */
//
//		if (tr.study_duration_units.contentEquals("day")) {
//			study_dur_in_days=study_duration_value;
//		} else if (tr.study_duration_units.contentEquals("week")) {
//			study_dur_in_days=study_duration_value*7.0;
//		} else if (tr.study_duration_units.contentEquals("month")) {
//			study_dur_in_days=study_duration_value*30.0;
//		} else if (tr.study_duration_units.contentEquals("year")) {
//			study_dur_in_days=study_duration_value*365.0;
//		} else if (tr.study_duration_units.contentEquals("hour")) {
//			study_dur_in_days=study_duration_value/24.0;
//		} else if (tr.study_duration_units.contentEquals("minute")) {
//			study_dur_in_days=study_duration_value/1440.0;
//		} else if (tr.study_duration_units.contentEquals("-")) {
//			return;
//		} else {
//			System.out.println("unknown units="+tr.study_duration_units);
//			return;
//		}
//
//		ArrayList<String> okSpecies = new ArrayList<String>();
//		okSpecies.add("mouse");// 27796
//		okSpecies.add("rat");// 13124
//		okSpecies.add("rabbit");// 1089
//		okSpecies.add("guinea pig");// 970
//		okSpecies.add("house mouse");
//
//		if (!okSpecies.contains(tr.species_common))
//			return;
//
//		if (!tr.toxval_type.contentEquals("NOAEL") && !tr.toxval_type.contentEquals("LOAEL"))
//			return;//not a valid record for neurotox
//		
//		if (!tr.human_eco.contentEquals("human health"))
//			return;
//			
//		}
//
//	
//		ScoreRecord sr=createScoreRecord(chemical, tr);//create generic score record
//
//		sr.duration=study_dur_in_days;
//		sr.durationUnits="days";
//		
////		System.out.println("duration="+sr.duration+" days");
//
//
//		if (tr.exposure_route.contentEquals("oral") && tr.toxval_units.contentEquals("mg/kg-day")) {
//			/* if (study_dur_in_days <= 91.0 && study_dur_in_days >= 89.0) { 
//			   Broadening the range to be more inclusive (90 + or - 5).
//			   Also switching the order for more logical reading. */			
//			if (study_dur_in_days >= 85.0 && study_dur_in_days <= 95.0)	{
//				setNinetyOralScore(sr, chemical);
//			} else if (study_dur_in_days >= 40.0 && study_dur_in_days <= 50.0) {
//				/*All three of these time categories now have a range of 10. */		
//				setFortyFiftyOralScore(sr, chemical);
//				/* } else if (study_dur_in_days <= 31.0 && study_dur_in_days >= 27.0) {
//			   Broadening the range to be more inclusive (28 + or - 5)*/
//			} else if (study_dur_in_days >= 23.0 && study_dur_in_days <= 33.0) {
//				setTwentyEightOralScore(sr,chemical);
//			}
//
//
//		} else if (tr.exposure_route.contentEquals("dermal")  && tr.toxval_units.contentEquals("mg/kg-day")) {
//			/* if (study_dur_in_days <= 91.0 && study_dur_in_days >= 89.0) {
//				Broadening the range to be more inclusive (90 + or - 5).
//				Also switching the order for more logical reading. */
//			if (study_dur_in_days >= 85.0 && study_dur_in_days <= 95.0) {
//				/*Got error when tried to use "=". It said "<=" was expected.*/			
//				setNinetyDermalScore(sr, chemical);
//			} else if (study_dur_in_days >= 40.0 && study_dur_in_days <= 50.0) {
//				setFortyFiftyDermalScore(sr, chemical);
//				/* } else if (study_dur_in_days <= 31.0 && study_dur_in_days >= 27.0) {
//			Broadening the range to be more inclusive (28 + or - 5)*/
//			} else if (study_dur_in_days >= 23.0 && study_dur_in_days <= 33.0) {
//				setTwentyEightDermalScore(sr,chemical);
//			}
//
//		} else if (tr.exposure_route.contentEquals("inhalation")  && tr.toxval_units.contentEquals("mg/L-day")) {
//			/* if (study_dur_in_days <= 91.0 && study_dur_in_days >= 89.0) {
//			Broadening the range to be more inclusive (90 + or - 5).
//			Also switching the order for more logical reading. */
//			if (study_dur_in_days >= 85.0 && study_dur_in_days <= 95.0) {
//				/*Got error when tried to use "=". It said "<=" was expected.*/			
//				setNinetyInhalationScore(sr, chemical);
//			} else if (study_dur_in_days >= 40.0 && study_dur_in_days <= 50.0) {
//				setFortyFiftyInhalationScore(sr, chemical);
//				/* } else if (study_dur_in_days <= 31.0 && study_dur_in_days >= 27.0) {
//			Broadening the range to be more inclusive (28 + or - 5)*/
//			} else if (study_dur_in_days >= 23.0 && study_dur_in_days <= 33.0) {
//				setTwentyEightInhalationScore(sr,chemical);
//			}
//
//		}
//
//
//		if (tr.toxval_type.contentEquals("NOAEL") || tr.toxval_type.contentEquals("LOAEL")) {
//			if (tr.toxval_units.contentEquals("mg/kg") &&
//					tr.exposure_route.contentEquals("oral") &&
//					study_dur_in_days >= 0.0 && study_dur_in_days <= 1.0) {		
//				setSingleDoseOralScore(sr,chemical);	
//			} else if(tr.toxval_units.contentEquals("mg/kg") &&
//					tr.exposure_route.contentEquals("dermal") &&
//					study_dur_in_days >= 0.0 && study_dur_in_days <= 1.0) {	
//				setSingleDoseDermalScore(sr,chemical);
//			} else if(tr.toxval_units.contentEquals("mg/L") || tr.toxval_units.contentEquals("mg/m3")) {
//				if (tr.exposure_route.contentEquals("inhalation") &&
//						study_dur_in_days >= 0.0 && study_dur_in_days <= 1.0) {			
//					setSingleDoseInhalationScore(sr,chemical);
//				}
//			}
//		}	
//
//
//		if (sr.score==null) return;
//
//		//		System.out.println(sr.scoreToInt());
//
//		
//
//		if (study_dur_in_days>1) {
//			chemical.scoreSystemic_Toxicity_Repeat_Exposure.records.add(sr);
//		} else {
//			//TODO- are there any records where this happends?
//			chemical.scoreSystemic_Toxicity_Single_Exposure.records.add(sr);
//		}
//
//	}
//
//
//	
//	
//	
//	
//	/* ****************************************** */
//	
//	
//	static void createRecords(Chemical chemical, RecordToxVal r) {
//		
//	/*
//	  if (!isNeuroCriticalEffect(r))
//			return; {
//	I don't think we want to exclude all of the ones that don't have a neuro
//	 critical effect because the critical effect might be blank and another field
//	 might indicate neuro.  So I'm putting the isNeuroCriticalEffect(r) in the if
//	 statement with the other inclusion criteria.
//			*/
//		
//		if(r.human_eco.contentEquals("human health") &&
//				r.exposure_route.contentEquals("oral") &&
//					r.toxval_units.contentEquals("mg/kg-day") &&
//					(r.study_type.contains("neurotoxicity") ||
//					r.study_type.contains("Neurotoxicity") ||
//					isNeuroCriticalEffect(r))) {
//				createNinetyNeurotoxicityOralRecord(chemical, r);		
//		} else if(r	
//		}
//		
//		
//		// add criteria
//	}
//
//	public static boolean isNeuroCriticalEffect(RecordToxVal r) {
//
//		String ce=r.critical_effect;
//
//		//more keywords added
//
//		if (ce.contains("ataxia") || 
//				ce.contains("brain") ||
//				ce.contains("cholinesterase") ||
//				ce.contains("CNS") ||
//				ce.contains("COMA") ||
//				ce.contains("convulsions") ||
//				ce.contains("decreased retention (memory)") ||
//				ce.contains("demyelination") ||
//				ce.contains("HALLUCINATIONS") ||
//				ce.contains("headache, dizziness, weakness") ||
//				ce.contains("impaired reflex") ||
//				ce.contains("jerking movements") ||
//				ce.contains("motor and sensory function") ||
//				ce.contains("nerve") ||
//				ce.contains("NERVOUS SYSTEM") ||
//				ce.contains("paralysis") ||
//				ce.contains("Psychomotor") ||
//				ce.contains("seizure") ||
//				ce.contains("SENSE ORGANS") ||
//				ce.contains("Spinal cord") ||
//				ce.contains("TOXIC PSYCHOSIS") ||
//				ce.contains("tremor") ) {
//			return true;
//		} else {
//			return false;
//		}
//	}
//}
//	