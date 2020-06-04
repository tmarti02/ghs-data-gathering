package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_models;

import java.text.DecimalFormat;

import gov.epa.ghs_data_gathering.API.Chemical;
import gov.epa.ghs_data_gathering.API.ScoreRecord;

public class ParseToxValModels {

	private static ScoreRecord createScoreRecord(RecordToxValModels r) {
		
		ScoreRecord sr = new ScoreRecord();		
		sr.source = ScoreRecord.sourceToxVal;
		sr.sourceOriginal=r.model;
				
		sr.valueMass=Double.parseDouble(r.value);
		sr.valueMassUnits=r.units;				
		return sr;		
	}
	
	public static void createScoreRecordBCF_Opera(Chemical chemical,RecordToxValModels rBCF,RecordToxValModels rBCF_AD) {
//		System.out.println(rc.casrn+"\t"+rc.cancer_call);
		
		ScoreRecord sr =createScoreRecord(rBCF);		
		double BCF_AD=Double.parseDouble(rBCF_AD.value);

		if (BCF_AD==1.0)
			setBioconcentrationScore(sr);
		else {
			sr.score=ScoreRecord.scoreNA;
			sr.rationale="Applicability domain of "+rBCF.model+" model for BCF violated";
		}					
		chemical.scoreBioaccumulation.records.add(sr);
										
	}
	
	public static void createScoreRecordBCF_EPISUITE(Chemical chemical,RecordToxValModels rBCF) {
//		System.out.println(rc.casrn+"\t"+rc.cancer_call);		
		ScoreRecord sr = createScoreRecord(rBCF);					
		setBioconcentrationScore(sr);
		chemical.scoreBioaccumulation.records.add(sr);						
	}

	
	
	private static void setBioconcentrationScore(ScoreRecord sr) {
		//TODO move this to its own class so many different parsers can use it?
		DecimalFormat df = new DecimalFormat("0.00");
		
		double logBCF=sr.valueMass;
		
		if (logBCF>3.7)  {// >3.7
			sr.score = ScoreRecord.scoreVH;
			sr.rationale = "logBCF (" + df.format(logBCF) + ") > 3.7";
		} else if (logBCF>=3) {
			sr.score = ScoreRecord.scoreH;
			sr.rationale = "3 <= logBCF (" + df.format(logBCF) + ") <= 3.7";
		}else if (logBCF>=2) {
			sr.score = ScoreRecord.scoreM;
			sr.rationale = "2 <= logBCF (" + df.format(logBCF) + ") < 3";
		} else {
			sr.score = ScoreRecord.scoreL;
			sr.rationale = "logBCF (" + df.format(logBCF) + ") < 2";
		}
		
	}

	public static void createScoreRecordPersistence_Opera(Chemical chemical, RecordToxValModels r) {
		ScoreRecord sr =createScoreRecord(r);				
		setPersistenceScoreOpera(sr);					
		chemical.scorePersistence.records.add(sr);		
	}

	private static void setPersistenceScoreOpera(ScoreRecord sr) {
		// TODO Auto-generated method stub
		
	}
	
	public static void createScoreRecordPersistence_EpiSuite(Chemical chemical, RecordToxValModels r) {
		ScoreRecord sr =createScoreRecord(r);				
		setPersistenceScoreEpiSuite(sr);					
		chemical.scorePersistence.records.add(sr);		
	}

	private static void setPersistenceScoreEpiSuite(ScoreRecord sr) {
		// TODO Auto-generated method stub
		
	}

}
