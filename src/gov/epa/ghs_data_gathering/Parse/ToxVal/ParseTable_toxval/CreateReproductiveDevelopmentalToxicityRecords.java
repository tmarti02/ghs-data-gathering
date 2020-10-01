package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval;

import java.util.ArrayList;

import gov.epa.api.Chemical;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;

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


	public void createReproductiveDevelopmentalRecords(Chemical chemical, RecordToxVal r) {
		// System.out.println("Creating records");
		// This prints.  It works up to here.
		
		if(!CreateAcuteMammalianToxicityRecords.isOkMammalianSpecies(r)) return;

		Score score=getScore(chemical, r);
		ScoreRecord sr=ParseToxVal.saveToxValInfo(score,r);
		
		
		if (r.toxval_type.contains("NOAEL") || r.toxval_type.contains("LOAEL")) {
			//	System.out.println("NOAEL or LOAEL");
			if (r.toxval_units.contentEquals("mg/kg-day") &&
					(r.exposure_route.contentEquals("oral") || r.toxval_subtype.toLowerCase().contains("oral"))) {
				setScore(sr, chemical, "Oral", 50, 250);				
					
				//		System.out.println("creating RepDev oral record");
			} else if(r.toxval_units.contentEquals("mg/kg-day") &&
					(r.exposure_route.contentEquals("dermal") || r.toxval_subtype.toLowerCase().contains("dermal"))) {
				setScore(sr, chemical, "Dermal", 100, 500);
			} else if((r.toxval_units.contentEquals("mg/L") || r.toxval_units.contentEquals("mg/m3")) &&
					(r.exposure_route.contentEquals("inhalation") || r.toxval_subtype.toLowerCase().contains("inhalation"))) {
				if (r.toxval_units.contentEquals("mg/m3")){

					// change value and units
					// 1 mg/L = 1000 mg/m3

					double toxval_numeric2 = Double.parseDouble(r.toxval_numeric)/1000.0;
					r.toxval_numeric = toxval_numeric2 + "";
					r.toxval_units = "mg/L (converted from mg/m3)";
				}
								
				setScore(sr, chemical, "Inhalation", 1, 2.5);
				
				//				System.out.println("creating rep/dev inhalation record");
			}
		}
		
		if (sr.score!=null)	score.records.add(sr);
	}





	private Score getScore (Chemical chemical, RecordToxVal tr) {

		Score score=null;
		
		if (tr.study_duration_class.toLowerCase().contains("reproduct") ||
				tr.study_duration_class.toLowerCase().contains("multigeneration") ||
				tr.toxval_subtype.toLowerCase().contains("reproduct") ||
				tr.toxval_subtype.toLowerCase().contains("multigeneration") ||
				tr.study_type.toLowerCase().contains("reproduct") ||
				tr.study_type.toLowerCase().contains("multigeneration") ||
				tr.critical_effect.toLowerCase().contains("reproduct") ||
				tr.critical_effect.toLowerCase().contains("multigeneration")) {
			score=chemical.scoreReproductive;
			//	System.out.println("add reproductive sr");
		} else if (tr.study_duration_class.toLowerCase().contains("developmental") ||
				tr.toxval_subtype.toLowerCase().contains("developmental") ||
				tr.study_type.toLowerCase().contains("developmental") ||
				tr.critical_effect.toLowerCase().contains("developmental")) {
			//	System.out.println("Adding Developmental Score");
			score=chemical.scoreDevelopmental;
		}

		return score;
	}

	public static void setScore(ScoreRecord sr, Chemical chemical, String route, double dose1, double dose2) {

		
		double dose = sr.valueMass;
		String strDose = ParseToxVal.formatDose(dose);
		String units="mg/kg-day";
		
		if (sr.valueMassOperator.equals(">")) {
			if (dose >= dose2) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = route+" "+sr.testType+" > "+dose2+" "+units;
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = route+" "+sr.testType+" does not provide enough information to assign a score";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			}

		} else if (sr.valueMassOperator.equals("<")) {
			if (dose <=dose1) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = route+" "+sr.testType+" < "+dose1+" "+units;
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = route+" "+sr.testType+" does not provide enough information to assign a score";

				// System.out.println(chemical.CAS + "\tless than operator detected for oral\t" + dose);
			}

		} else if (sr.valueMassOperator.equals("") || sr.valueMassOperator.equals("=") || sr.valueMassOperator.equals("~") || sr.valueMassOperator.equals(">=") || sr.valueMassOperator.equals("<=")) {

			if (dose < dose1) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = route+" "+sr.testType+" < "+dose1+" "+units;
			} else if (dose >= dose1 && dose <= dose2) {
				sr.score = ScoreRecord.scoreM;
				sr.rationale = dose1+" " +units+" < "+route+" "+sr.testType+" <= "+dose2+" "+units;
			} else if (dose > dose2) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = route+" "+sr.testType + " > "+dose2+" "+units;
			} else { 
				System.out.println(chemical.CAS + "\t"+route+"\t" + strDose);
			}
		}
	}


	
}
