package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_bcfbaf;

import java.text.DecimalFormat;

import gov.epa.api.Chemical;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;

public class ParseToxValBCFBAF {

	public static void createScoreRecord(Chemical chemical, RecordToxValBCFBAF r) {

		// Only including vertebrates (fish) and whole body.		

		if (!r.species_supercategory.contentEquals("Vertebrate"))
			return;

		if (!r.tissue.contentEquals("Whole body"))
			return;

		Score score=chemical.scoreBioaccumulation;

		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);	

		sr.source = ScoreRecord.sourceToxVal;
		sr.sourceOriginal=r.author+", "+r.year;
		sr.toxvalID="bcfbaf_"+r.bcfbaf_id;//need this so can do manual check later

		if (r.journal.contains("http")) {
			sr.url=r.journal;
		}

		sr.longRef=r.author+" ("+r.year+") "+r.title+". "+r.journal;

		sr.listType=ScoreRecord.typeScreening;//journal article

		sr.testType="logBAF or logBCF";

		try {
			sr.duration=Double.parseDouble(r.exposure_duration);
			sr.durationUnits="days";
			// How do we know that the duration units are days? -Leora
		} catch (Exception ex) {
			//leave them blank
		}


		sr.testOrganism=r.species_common;


		//		if (r.logbcf==null) return;//have BAF value instead probably
		//		I commented this out because we want either logbcf or logbaf.

		//		System.out.println("r.logbcf="+r.logbcf);



		if (r.logbaf!=null) {
			sr.valueMass=Double.parseDouble(r.logbaf);
			//	// sr.valueMassUnits="log10 ("+r.units+")";
			//		sr.valueMassUnits="log10("+r.units+")";
			//		I think the units don't need to specify log because it is already designated as logbaf.
			sr.valueMassUnits=r.units;
			setBioaccumulationScore(sr.valueMass, sr);
			score.records.add(sr);

		} else if (r.logbcf!=null) {
			sr.valueMass=Double.parseDouble(r.logbcf);
			//			sr.valueMassUnits="log10("+r.units+")";
			//			I think the units don't need to specify log because it is already designated as logbcf.
			sr.valueMassUnits=r.units;
			setBioconcentrationScore(sr.valueMass, sr);
			score.records.add(sr);
		}

		//TODO- add exclusion criteria so certain records arent added based on fields in RecordToxValBCFBAF
		//TODO- should we use logBAF for something?
		// Yes, the DfE criteria for logBAF are the same as for logBCF.  Also,
		// a field-measured BAF is the most preferred data for bioaccumulation. -Leora

	}

	private static void setBioaccumulationScore(double logBAF, ScoreRecord sr) {

		DecimalFormat df = new DecimalFormat("0.00");

		if (logBAF>3.7)  {// >3.7
			sr.score = "VH";
			sr.rationale = "logBAF > 3.7";
		} else if (logBAF>=3) {
			sr.score = "H";
			sr.rationale = "3 <= logBAF <= 3.7";
		} else if (logBAF>=2) {
			sr.score = "M";
			sr.rationale = "2 <= logBAF < 3";
		} else {
			sr.score = "L";
			sr.rationale = "logBAF < 2";
		}

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