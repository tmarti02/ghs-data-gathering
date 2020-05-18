package gov.epa.ghs_data_gathering.Parse.ToxVal;

import gov.epa.ghs_data_gathering.API.Chemical;
import gov.epa.ghs_data_gathering.API.ScoreRecord;

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
	

	
	static void createAquaticToxAcuteRecords(Chemical chemical, RecordToxVal tr) {

		ScoreRecord sr = new ScoreRecord();
		sr = new ScoreRecord();
		sr.source = ScoreRecord.sourceToxVal;
		sr.sourceOriginal=tr.source;
	

		sr.valueMassOperator=tr.toxval_numeric_qualifier;
		sr.valueMass = Double.parseDouble(tr.toxval_numeric);
		sr.valueMassUnits = tr.toxval_units;

		setAquaticToxAcuteScore(sr, chemical);

		sr.note=ParseToxVal.createNote(tr);
			
		chemical.scoreAcute_Aquatic_Toxicity.records.add(sr);

			
					
	}

	
	
	static void createAquaticToxChronicRecords(Chemical chemical, RecordToxVal tr) {
		
		ScoreRecord sr = new ScoreRecord();
		sr = new ScoreRecord();
		sr.source = ScoreRecord.sourceToxVal;
		sr.sourceOriginal=tr.source;
	

		sr.valueMassOperator=tr.toxval_numeric_qualifier;
		sr.valueMass = Double.parseDouble(tr.toxval_numeric);
		sr.valueMassUnits = tr.toxval_units;

		setAquaticToxChronicScore(sr, chemical);

		sr.note=ParseToxVal.createNote(tr);
		
		chemical.scoreChronic_Aquatic_Toxicity.records.add(sr);
		
		//Need to address this error message.
		
		
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
		
		if (dose < .1) {
			sr.score = ScoreRecord.scoreVH;
			sr.rationale = "NOEC or LOEC" + " (" + strDose + " mg/L) < 1 mg/L";
		} else if (dose >= 0.1 && dose <= 1) {
			sr.score = ScoreRecord.scoreH;
			sr.rationale = "0.1 mg/L <= NOEC or LOEC (" + strDose + " mg/L) <=1 mg/L";
		} else if (dose > 1 && dose <= 10) {
			sr.score = ScoreRecord.scoreM;
			sr.rationale = "1 mg/L < NOEC or LOEC (" + strDose + " mg/L) <=10 mg/L";
		} else if (dose > 10) {
			sr.score = ScoreRecord.scoreL;
			sr.rationale = "NOEC or LOEC" + "(" + strDose + " mg/L) > 10 mg/L";
		} else { 
			System.out.println(chemical.CAS + "\tEcoToxChronic\t" + strDose);
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
				
		if (dose < 1) {
			sr.score = ScoreRecord.scoreVH;
			sr.rationale = "LC50 or EC50" + " (" + strDose + " mg/L) < 1 mg/L";
		} else if (dose >= 1 && dose <= 10) {
			sr.score = ScoreRecord.scoreH;
			sr.rationale = "1 mg/kg <= LC50 or EC50 (" + strDose + " mg/L) <=10 mg/L";
		} else if (dose > 10 && dose <= 100) {
			sr.score = ScoreRecord.scoreM;
			sr.rationale = "10 mg/kg < LC50 or EC50 (" + strDose + " mg/L) <=100 mg/L";
		} else if (dose > 100) {
			sr.score = ScoreRecord.scoreL;
			sr.rationale = "LC50 or EC50" + "(" + strDose + " mg/L) > 100 mg/L";
		} else { System.out.println(chemical.CAS + "\tEcoToxAcute\t" + strDose);
				 
		}
	}
}
