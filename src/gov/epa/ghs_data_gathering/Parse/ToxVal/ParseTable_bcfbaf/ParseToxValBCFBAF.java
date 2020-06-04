package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_bcfbaf;

import java.text.DecimalFormat;

import gov.epa.ghs_data_gathering.API.Chemical;
import gov.epa.ghs_data_gathering.API.ScoreRecord;

public class ParseToxValBCFBAF {

	public static ScoreRecord createScoreRecord(Chemical chemical, RecordToxValBCFBAF r) {
		
		ScoreRecord sr = new ScoreRecord();		
		sr.source = ScoreRecord.sourceToxVal;
		sr.sourceOriginal=r.author+", "+r.year;
				
		sr.valueMass=Double.parseDouble(r.logbcf);
//		sr.valueMassUnits="log10 ("+r.units+")";
		sr.valueMassUnits=r.units;
		setBioconcentrationScore(sr.valueMass, sr);
		
		
		//TODO- add exclusion criteria so certain records arent added based on fields in RecordToxValBCFBAF
		//TODO- should we use logBAF for something?
		
		chemical.scoreBioaccumulation.records.add(sr);
		
		return sr;		
	}

	
	private static void setBioconcentrationScore(double logBCF, ScoreRecord sr) {
		
		DecimalFormat df = new DecimalFormat("0.00");
		
		if (logBCF>3.7)  {// >3.7
			sr.score = "VH";
			sr.rationale = "logBCF (" + df.format(logBCF) + ") > 3.7";
		} else if (logBCF>=3) {
			sr.score = "H";
			sr.rationale = "3 <= logBCF (" + df.format(logBCF) + ") <= 3.7";
		}else if (logBCF>=2) {
			sr.score = "M";
			sr.rationale = "2 <= logBCF (" + df.format(logBCF) + ") < 3";
		} else {
			sr.score = "L";
			sr.rationale = "logBCF (" + df.format(logBCF) + ") < 2";
		}
		
	}
}
