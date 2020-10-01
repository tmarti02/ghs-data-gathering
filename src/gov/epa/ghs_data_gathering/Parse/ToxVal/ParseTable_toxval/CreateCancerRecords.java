package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval;

import java.util.ArrayList;

import gov.epa.api.Chemical;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;

/**
 * This class is to create records based on the quantitative cancer data (rather than cancer calls)
 * @author Todd Martin
 *
 */
public class CreateCancerRecords {


	public static void createCancerRecords(Chemical chemical, RecordToxVal r) {
		if((r.toxval_type.toLowerCase().contains("cancer unit risk") || r.toxval_type.toLowerCase().contains("cancer slope factor")) &&
				r.human_eco.contentEquals("human health")) {
			//	&&r.toxval_units.contentEquals("ug/m3)-1")) {
			createCancerRecord(chemical, r);
		}
	}

	/* Omitting NOAEL/LOAEL (animal) for now due to uncertainty.
	 * if (r.toxval_type.contentEquals("NOAEL") &&
	 * r.toxval_units.contentEquals("mg/kg-day")) {
	 */

	private static void createCancerRecord(Chemical chemical, RecordToxVal tr) {

		Score score=chemical.scoreCarcinogenicity;
		ScoreRecord sr = ParseToxVal.saveToxValInfo(score,tr);
		sr.hazardName=score.hazard_name;
		sr.note2 = tr.toxval_type;

		// I wanted a way to distinguish between toxval types below.
		// I think storing toxval_type in note2 works.  -Leora

		setCancerScore(sr, chemical);		
		// setCancerScore(sr, chemical, tr.toxval_type);
		score.records.add(sr);

	}


	private static void setCancerScore(ScoreRecord sr, Chemical chemical) {	
		double dose = sr.valueMass;
		if (dose < 0) {
			return;
		}
		if (sr.note2.toLowerCase().contains("cancer slope factor")) {
			//		if (dose >= 0 && sr.note2.contentEquals("cancer slope factor"))  {
			//		dose >=0 isn't working correctly so I changed it to 
			//			if (dose < 0) { return;
			sr.score = ScoreRecord.scoreVH;
			sr.rationale = "A cancer slope factor is assessed if Hazard Identification provides evidence of human carcinogenicity.";
		} else if (sr.note2.toLowerCase().contains("cancer unit risk"))  {
			//		} else if (dose >= 0 && sr.note2.contentEquals("cancer unit risk"))  {
			//			dose >=0 isn't working correctly so I changed it to 
			//			if (dose < 0) { return;
			sr.score = ScoreRecord.scoreVH;
			sr.rationale = "A cancer unit risk indicates carcinogenicity in humans.";
		}
	}

	/*	Other possible way:
 			(need to have:
 			setCancerScore(sr, chemical, tr.toxval_type);
  			in createCancerRecord method above)

 			private static void setCancerScore(ScoreRecord sr, Chemical chemical, String toxval_type) {	
			double dose = sr.valueMass;
			if (dose >= 0 && toxval_type.contentEquals("cancer slope factor")) { // sr.note2.contentEquals("cancer slope factor"))  {
			sr.score = ScoreRecord.scoreVH;
			sr.rationale = "A cancer slope factor is assessed if Hazard Identification provides evidence of human carcinogenicity.";
			} else if (dose >= 0 && toxval_type.contentEquals("cancer unit risk"))  {
			sr.score = ScoreRecord.scoreVH;
			sr.rationale = "A cancer unit risk indicates carcinogenicity in humans.";
			}*/


	/* if cancer_call is not missing then
	 * [L,M,H,VH,N/A based on dictionary that I added for cancer_call]
	 * 	
	 * else if cancer_call is missing or if cancer_call is N/A then
	 * 
	 * I want to code something like the above to use the cancer_call if there is one
	 * and only use the other toxval data if there is no cancer_call.
	 * The cancer call is in the separate file
	 * toxval_cancer_summary_2020-01-16
	 * I downloaded this file from the rtp site.
	 * This file includes 2,827 records.
	 * 
	 * (It looks like there is probably a lot of overlap with this database and our CHA Database.)
	 * 
	 * I created a dictionary in Excel based on the possible cancer_call values:
	 * 
	 * VH: A (Human carcinogen); A (Human carcinogen) (Inhalation route);  B1 (Probable human carcinogen - based on limited evidence of carcinogenicity in humans); B2 (Probable human carcinogen - based on sufficient evidence of carcinogenicity in animals); Carcinogenic to humans; Carcinogenic to humans (Inhalation route); Group 1 - Carcinogenic to humans; Group 2A - Probably carcinogenic to humans; Group A Human Carcinogen; Group A Human Carcinogen by Inhalation; Group B1 Probable Human Carcinogen; Group B2 Probable Human Carcinogen; Group I: CEPA (carcinogenic to humans); Group II: CEPA (probably carcinogenic to humans); "Group II: CEPA (probably carcinogenic to humans) Group 2A: IARC (probably carcinogenic to humans)"; "Group IV: CEPA (unlikely to be carcinogenic to humans) Group 2A: IARC (probably carcinogenic to humans)"; "IRIS (inadequate data for evaluation of carcinogenicity) Group 2A: IARC (probably carcinogenic to humans)"; IRIS (likely to be carcinogenic to humans); Known Human Carcinogen; Known/likely human carcinogen (Oral route); Known/Likely; Known/likely human carcinogen; Known/likely human carcinogen (Inhalation route); Likely Human Carcinogen; Likely to be carcinogenic to humans; Likely to be carcinogenic to humans (Combined route);  Likely to be carcinogenic to humans (inhalation route. IN for oral route); Likely to be carcinogenic to humans (oral route); Likely to be carcinogenic to humans (oral); inadequate (inhalation); Likely to be carcinogenic to humans following prolonged, high-level exposures causing cytotoxicity and regenerative cell hyperplasia in the proximal region of the small intestine (oral exposure) or the respiratory tract (inhalation exposure), but not like; Likely to be carcinogenic to humans for oral exposure. Inadequate information for inhalation.; potential occupational carcinogen; Reasonably Anticipated To Be Human Carcinogen; Suggestive Evidence for Carcinogenicity in Humans; 
	 * H: C (Possible human carcinogen); Group 2B - Possibly carcinogenic to humans; Group 2B: IARC (possibly carcinogenic to humans); Group C Possible Human Carcinogen; Group C: IRIS (a possible human carcinogen); "Group D: IRIS (not classifiable as to human carcinogenicity) (IRIS), 1991/Group 2B: IARC (possibly carcinogenic to humans)/ GCDWQ: HC, 1986"; "Group D: IRIS (not classifiable as to human carcinogenicity); Group 2B: IARC (possibly carcinogenic to humans)"; Group III: CEPA (possible germ cell mutagen, and possibly carcinogenic to humans); Group IIIB (possibly carcinogenic to humans, limited evidence of carcinogenicity); Group IIIB: (possibly carcinogenic to humans); "Likely to be Carcinogenic in Humans at High Doses; Not Likely to be Carcinogenic to Humans at Low Doses"; "Likely to be Carcinogenic to Humans (High Doses), Not Likely to be Carcinogenic to Humans (Low Doses)"; Suggestive evidence of carcinogenic potential; Suggestive evidence of carcinogenic potential (oral. Inhalation is not likely to be carcinogenic); Suggestive evidence of carcinogenic potential for oral exposure.; Suggestive evidence of carcinogenic potential for oral exposure. Inadequate information for inhalation.; Suggestive evidence of the carcinogenic potential for the inhalation route (IN for oral); Suggestive Evidence of Carcinogenicity but Not Sufficient to Assess Human Carcinogenic Potential; Suggestive evidence of carcinogenicity, but not sufficient to assess human carcinogenic potential (Inhalation route)
	 * M: [None of the cancer_call values fit in the M category based on DfE/AA/CHA]
	 * L: E (Evidence of non-carcinogenicity for humans); Group 4 - Probably not carcinogenic to humans; Group E Evidence of Non-carcinogenicity for Humans; Group IV: CEPA (unlikely to be carcinogenic to humans); Group IVC: CEPA (probably not carcinogenic to humans); IOM does not consider manganese carcinogenic to humans; IOM does not consider molybdenum carcinogenic to humans; IOM does not consider zinc carcinogenic to humans; Not Likely to Be Carcinogenic in Humans; Not likely to be carcinogenic to humans (Oral route)
	 * N/A: Carcinogenic potential cannot be determined; Carcinogenic potential cannot be determined (Inhalation route);  Carcinogenic potential cannot be determined (Oral route); CEPA (Although there is some evidence for the carcinogenicity of inorganic fluoride, available data are inconclusive.); D (Not classifiable as to human carcinogenicity); D (Not classifiable as to human carcinogenicity) (Oral route); Data are inadequate for an assessment of human carcinogenic potential; Data are inadequate for an assessment of human carcinogenic potential (Oral route); Group 3 - Not classifiable as to its carcinogenicity to humans; Group D Not Classifiable as to Human Carcinogenicity; Group D: IRIS (not classifiable as to human carcinogenicity); Group V (inadequate data for evaluation of carcinogenicity); Group V: CEPA (probably not carcinogenic to humans); Group VA: CEPA (inadequate data for evaluation); Group VI: CEPA (unclassifiable with respect to carcinogenicity to humans); Group VIA: CEPA (unclassifiable with respect to carcinogenicity to humans); Group VIB: CEPA (unclassifiable with respect to carcinogenesis in humans); Inadequate for an assessment of carcinogenic potential; Inadequate information to assess carcinogenic potential; Inadequate information to assess carcinogenic potential (Oral route); Inadequate information to assess carcinogenic potential (oral, inhalation is not likely to be carcinogenic); "IOM does not consider selenium carcinogenic to humans. Group 3: IARC (not classifiable as to human carcinogenicity) Group B2: U.S. EPA (probable human carcinogen) for selenium sulphide"; IOM, 2001 ("There is little convincing evidence indicating that copper is causally associated with the development of cancer in humans."); IRIS (inadequate data to assess human carcinogenic potential); no adequate data to characterize in terms of carcinogenicity; No Data Available; Not Likely to be Carcinogenic to Humans at Doses that Do Not Alter Rat Thyroid Hormone Homeostasis; Not likely to be carcinogenic to Non-humans; Not Yet  Determined
	 * 
	 * Need to convert this dictionary to Java code.
	 * 
	 * -Leora
	 *  */

	/*	if (r.toxval_type.contentEquals("NOAEL") && r.toxval_units.contentEquals("mg/kg-day")) {
			//TODO
	 */

	/*NOAEL for acrylamide = 0.5 or 0.1
	 * 
	 * Determining cutoffs based on ToxVals has a lot of uncertainty because typically, a weight of evidence
	 * approach is used to qualitatively estimate risk for cancer.
	 * Also, traditionally, there is assumed to be no threshold, or no safe level.
	 * Thus, DfE and GHS and the Tiger Team don't have numeric cutoffs for cancer.
	 * 
	 * So I'm not sure whether we should use numeric cutoffs.
	 * 
	 * Maybe we should only use the data that have a cancer_call,
	 * though that is a relatively small number of chemicals.
	 * 
	 *
	 * 
	 * If we want to consider numeric cutoffs for the dictionary for NOAEL:
	 * 
	 * If toxval_numeric_qualifier is = or >= or >
	 * 
	 * Maybe:
	 * >0 & <=1 VH
	 * >1 & <=10 H
	 * >10 & <=1000 M
	 * >1000 L
	 * 
	 * * If toxval_numeric_qualifier is <= or < & toxval_numeric is <=1 then score= VH
	 * else if toxval_numeric_qualifier is <= or < then skip or score=n/a
	 * 
	 * The NOAEL is used to calculate the RfD in humans by dividing by uncertainty factors.
	 * Typically, the NOAEL would be divided by at least 100.
	 * 
	 * -Leora
	 * */

	/*} else if (r.toxval_type.contentEquals("cancer unit risk")&& r.toxval_units.contentEquals("ug/m3)-1")) {*/
	//TODO

	/* 
	 * The cancer unit risk is an estimate of the increased cancer risk from (usually inhalation) exposure
	 * to a concentration of 1 µg/m3 for a lifetime.
	 * 
	 * I added units because the meaning of the numeric values will change if the units are different.
	 * For acrylamide, the only units for unit risk are (ug/m3)-1.  This is the typical units, but
	 * need to check whether there are any other units for other chemicals.
	 * 
	 * For acrylamide, the unit risk is 0.0001.
	 * 
	 * Options for dictionary for unit risk:
	 * 
	 * Option 1.  If there is a unit risk (or unit risk greater than zero) then score VH
	 * because a unit risk indicates risk in humans.
	 * 
	 * Option 2.  Numeric cutoff points: maybe:
	 * 
	 * If toxval_numeric_qualifier is = or < or <=
	 * 
	 *  >= 0.000001 (1 in a million) VH
	 *  < 0.000001 & >= 0.0000001 H
	 *  < 0.0000001 & >= 0.00000001 M
	 *  = 0 L
	 *  [blank] N/A
	 *  
	 * If toxval_numeric_qualifier is >= or > & toxval_numeric is >= 0.000001 then score= VH
	 * else if toxval_numeric_qualifier is >= or > then skip or score=n/a
	 * 
	 *  
	 *  -Leora
	 */


	/*} else {*///need code for all important toxval_types

	/* NOAEL and cancer unit risk are the only toxval_types for acrylamide. -Leora 4/23/20 */


	/* Richard seemed to be suggesting that we treat all toxval_types and all rac the same and just use
			If(min value < 1) high
			Else if(min value < 10) medium
			Else low
			I think that is oversimplifying and doesn't scale properly
			and we need to use appropriate cutoffs for the different rac.
			-Leora

	 */


}





