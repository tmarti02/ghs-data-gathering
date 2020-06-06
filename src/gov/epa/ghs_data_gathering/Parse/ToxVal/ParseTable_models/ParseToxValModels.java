package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_models;

import java.text.DecimalFormat;

import gov.epa.ghs_data_gathering.API.Chemical;
import gov.epa.ghs_data_gathering.API.ScoreRecord;

public class ParseToxValModels {

	private static ScoreRecord createScoreRecord(RecordToxValModels r) {

		ScoreRecord sr = new ScoreRecord();
		sr.source = ScoreRecord.sourceToxVal;
		sr.sourceOriginal = r.model;

		sr.valueMass = Double.parseDouble(r.value);
		sr.valueMassUnits = r.units;
		return sr;
	}

	public static void createScoreRecordBCF_Opera(Chemical chemical, RecordToxValModels rBCF,
			RecordToxValModels rBCF_AD) {
		// System.out.println(rc.casrn+"\t"+rc.cancer_call);

		ScoreRecord sr = createScoreRecord(rBCF);
		double BCF_AD = Double.parseDouble(rBCF_AD.value);
		if (BCF_AD == 1.0)
			setBioconcentrationScore(sr);
		else {
			sr.score = ScoreRecord.scoreNA;
			sr.rationale = "Applicability domain of " + rBCF.model + " model for BCF violated";
		}
		chemical.scoreBioaccumulation.records.add(sr);

	}

	public static void createScoreRecordBCF_EPISUITE(Chemical chemical, RecordToxValModels rBCF) {
		// System.out.println(rc.casrn+"\t"+rc.cancer_call);
		ScoreRecord sr = createScoreRecord(rBCF);
		setBioconcentrationScore(sr);
		chemical.scoreBioaccumulation.records.add(sr);
	}

	private static void setBioconcentrationScore(ScoreRecord sr) {
		// TODO move this to its own class so many different parsers can use it?
		DecimalFormat df = new DecimalFormat("0.00");

		double logBCF = sr.valueMass;

		if (logBCF > 3.7) {// >3.7
			sr.score = ScoreRecord.scoreVH;
			sr.rationale = "logBCF (" + df.format(logBCF) + ") > 3.7";
		} else if (logBCF >= 3) {
			sr.score = ScoreRecord.scoreH;
			sr.rationale = "3 <= logBCF (" + df.format(logBCF) + ") <= 3.7";
		} else if (logBCF >= 2) {
			sr.score = ScoreRecord.scoreM;
			sr.rationale = "2 <= logBCF (" + df.format(logBCF) + ") < 3";
		} else {
			sr.score = ScoreRecord.scoreL;
			sr.rationale = "logBCF (" + df.format(logBCF) + ") < 2";
		}

	}

	public static void createScoreRecordPersistence_Opera(Chemical chemical, RecordToxValModels r) {
		ScoreRecord sr = createScoreRecord(r);
		setPersistenceScoreOpera(sr);
		chemical.scorePersistence.records.add(sr);
	}

	private static void setPersistenceScoreOpera(ScoreRecord sr) {
		// TODO Auto-generated method stub

		DecimalFormat df = new DecimalFormat("0.00");

		double halflife = sr.valueMass;

		if (halflife > 180) {
			sr.score = ScoreRecord.scoreVH;
			sr.rationale = "half-life (" + df.format(halflife) + ") > 180 days";
		} else if (halflife >= 60) {
			sr.score = ScoreRecord.scoreH;
			sr.rationale = "60 days <= half-life (" + df.format(halflife) + ") <= 180 days";
		} else if (halflife >= 16) {
			sr.score = ScoreRecord.scoreM;
			sr.rationale = "16 days <= half-life (" + df.format(halflife) + ") < 60 days";
		} else { // if (halflife < 16) {
			sr.score = ScoreRecord.scoreL;
			sr.rationale = "half-life (" + df.format(halflife) + ") < 16 days";

		}
	}


	public static void createScoreRecordPersistence_EpiSuite(Chemical chemical, RecordToxValModels r) {
		ScoreRecord sr =createScoreRecord(r);				
		setPersistenceScoreEpiSuite(sr);					
		chemical.scorePersistence.records.add(sr);		
	}

	private static void setPersistenceScoreEpiSuite(ScoreRecord sr) {
		// TODO Auto-generated method stub

		/*	EpiSuite data is in the from of Biodegredation Score (Biowin Score)
 Conversion to days based on Scheringer citing Aronson et al 2006.  Can't access full Aronsen text.
I tried to paste the conversion here but it wouldn't save due to unrecognized characters. 
-Leora

Biowin Score	Aerobic biodegredation half-life (days)		Score
> 4.75				0.17
4.25–4.75			1.25
3.75–4.25			2.33
3.25–3.75			8.67					(>=3.25)	L
2.75–3.25			15					(> 2.25 & < 3.25) M
2.25–2.75			37.5								
1.75–2.25			120					(1.75-2.25) H
1.25–1.75			240					(<1.75)		VH
< 1.25				720									

		 */

		DecimalFormat df = new DecimalFormat("0.00");
		double biowin = sr.valueMass;

		if (biowin < 1.75) {
			sr.score = ScoreRecord.scoreVH;
			sr.rationale = "Biowin Score (" + df.format(biowin) + ") < 1.75";
		} else if (biowin <= 2.25) {
			sr.score = ScoreRecord.scoreH;
			sr.rationale = "1.75 <= Biowin Score (" + df.format(biowin) + ") <= 2.25 days";
		} else if (biowin <= 2.75) {
			sr.score = ScoreRecord.scoreM;
			sr.rationale = "2.25 <= Biowin Score (" + df.format(biowin) + ") <= 2.75";
		} else { // if (biowin > 2.75) {
			sr.score = ScoreRecord.scoreL;
			sr.rationale = "Biowin Score (" + df.format(biowin) + ") > 2.75";
		} 
	}

}


