package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_bcfbaf;

import java.text.DecimalFormat;

import gov.epa.api.Chemical;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;

public class ParseToxValBCFBAF {

	public static void createScoreRecord(Chemical chemical, RecordToxValBCFBAF r) {
		
		Score score=chemical.scoreBioaccumulation;
		
		ScoreRecord sr = new ScoreRecord();	
		sr.hazard_name=score.hazard_name;
		
		sr.source = ScoreRecord.sourceToxVal;
		sr.sourceOriginal=r.author+", "+r.year;
		
		if (r.journal.contains("http")) {
			sr.url=r.journal;
		}
		
		sr.long_ref=r.author+" ("+r.year+") "+r.title+". "+r.journal;
		
		sr.authority=ScoreRecord.typeScreening;//journal article
		
		sr.test_type="logBCF";

		try {
			sr.duration=Double.parseDouble(r.exposure_duration);
			sr.durationUnits="days";
		} catch (Exception ex) {
			//leave them blank
		}

	
		sr.test_organism=r.species_common;
		

		if (r.logbcf==null) return;//have BAF value instead probably
		
//		System.out.println("r.logbcf="+r.logbcf);
		sr.valueMass=Double.parseDouble(r.logbcf);
		//	sr.valueMassUnits="log10 ("+r.units+")";
		sr.valueMassUnits="log10 (BCF "+r.units+")";
		setBioconcentrationScore(sr.valueMass, sr);

		//TODO- add exclusion criteria so certain records arent added based on fields in RecordToxValBCFBAF
		//TODO- should we use logBAF for something?

		score.records.add(sr);

		
	}


	private static void setBioconcentrationScore(double logBCF, ScoreRecord sr) {

		DecimalFormat df = new DecimalFormat("0.00");

		if (logBCF>3.7)  {// >3.7
			sr.score = "VH";
			sr.rationale = "logBCF > 3.7";
		} else if (logBCF>=3) {
			sr.score = "H";
			sr.rationale = "3 <= logBCF <= 3.7";
		}else if (logBCF>=2) {
			sr.score = "M";
			sr.rationale = "2 <= logBCF < 3";
		} else {
			sr.score = "L";
			sr.rationale = "logBCF < 2";
		}

	}
}