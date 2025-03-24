package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_bcfbaf;

import java.text.DecimalFormat;

import gov.epa.api.Chemical;
import gov.epa.api.ExperimentalConstants;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ParameterValue;
import gov.epa.exp_data_gathering.parse.ParseUtilities;

public class ParseToxValBCFBAF {

	public static void createScoreRecord(Chemical chemical, RecordToxValBCFBAF r) {

		// Only including vertebrates (fish) and whole body.		

		if (!r.species_supercategory.contentEquals("Vertebrate"))//TODO use only fish? Need supercategory hashtable from ParseArnot2006
			return;

		if (r.tissue==null || !r.tissue.contentEquals("Whole body"))
			return;

		Score score=chemical.scoreBioaccumulation;

		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);	
		sr.dtxsid=chemical.dtxsid;

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
			sr.duration=r.exposure_duration;
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
			setBioconcentrationScore(sr.valueMass, sr);
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
	
	public static ScoreRecord createScoreRecord(ExperimentalRecord er) {

		ScoreRecord sr = new ScoreRecord(Chemical.strBioaccumulation,er.casrn,er.chemical_name);	

		sr.dtxsid=er.dsstox_substance_id;
		sr.source = er.source_name;

		if(er.literatureSource!=null) {
			sr.longRef=er.literatureSource.citation;
			sr.sourceOriginal=er.literatureSource.author+", "+er.literatureSource.year;
//			System.out.println(sr.sourceOriginal);
		}
		
		if(er.publicSourceOriginal!=null) {
			sr.sourceOriginal=er.publicSourceOriginal.name;
			sr.url=er.publicSourceOriginal.url;
		} else if (er.original_source_name!=null) {
			sr.sourceOriginal=er.original_source_name;
		}

		sr.listType=ScoreRecord.typeScreening;//experimental data
		
		if(er.property_name.equals(ExperimentalConstants.strBCF)) {
			sr.testType="logBCF";	
		} else if(er.property_name.equals(ExperimentalConstants.strBAF)) {
			sr.testType="logBAF";
		} else {
			System.out.println("Handle property_name="+er.property_name);
		}

		if (er.parameter_values != null) {
			for (ParameterValue pv : er.parameter_values) {
				if (pv.parameter.name.equals("Exposure duration")) {//taken from Observation duration for ECOTOX
//					if (pv.valuePointEstimate != null) {
//						sr.duration = pv.valuePointEstimate;
//					} else {
//						System.out.println("Exposure duration point estimate missing");
//					}
					
					sr.duration=pv.toStringNoUnits();
					sr.durationUnits = pv.unit.abbreviation;
					
//					System.out.println(sr.testType+"\t"+sr.duration);
				}
			}
		}


		sr.testOrganism=(String)er.experimental_parameters.get("Species common");
		sr.testOrganismType=(String)er.experimental_parameters.get("Species supercategory");

		// Limit to fish:	
		if(sr.testOrganismType==null || !sr.testOrganismType.equals("Fish")) {
//			System.out.println("sr.testOrganismType="+sr.testOrganismType);
			return null;
		}

		String responseSite=(String)er.experimental_parameters.get("Response site");
		//Only include whole body response site:
		if (responseSite==null || !responseSite.toLowerCase().contains("whole body")) {
//			System.out.println("responseSite="+responseSite);
			return null;
		}
		
		
//		if(er.property_value_min_final!=null || er.property_value_max_final!=null || (er.property_value_numeric_qualifier!=null && !er.property_value_numeric_qualifier.equals("~"))) {
//			System.out.println(er.property_value_min_final+"\t"+er.property_value_max_final+"\t"+er.property_value_numeric_qualifier);
////			return null;
//		}
		
		if(!er.property_value_units_final.equals(ExperimentalConstants.str_L_KG)) {
			System.out.println("Invalid units:\t"+er.property_value_units_final);
			return null;
			
		}
		
		setBioconcentrationScore(er, sr);
		
		if(sr.score==null) {
//			System.out.println("score is null");
			return null;		
		}
	
		if(sr.duration!=null)
			System.out.println(sr.testType+"\t"+sr.duration);
		
//		if(sr.valueMassOperator!=null) {
//			System.out.println(er.casrn);
//		}
		
//		System.out.println(ParseUtilities.gson.toJson(sr));
		
//		score.records.add(sr);
		return sr;
	}

//	private static void setBioaccumulationScore(double logBAF, ScoreRecord sr) {
//
//		DecimalFormat df = new DecimalFormat("0.00");
//
//		if (logBAF>3.7)  {// >3.7
//			sr.score = "VH";
//			sr.rationale = "logBAF > 3.7";
//		} else if (logBAF>=3) {
//			sr.score = "H";
//			sr.rationale = "3 <= logBAF <= 3.7";
//		} else if (logBAF>=2) {
//			sr.score = "M";
//			sr.rationale = "2 <= logBAF < 3";
//		} else {
//			sr.score = "L";
//			sr.rationale = "logBAF < 2";
//		}
//
//	}

	private static void setBioconcentrationScore(double logBCF, ScoreRecord sr) {

		if (logBCF>3.7)  {// >3.7
			sr.score = "VH";
			sr.rationale = sr.testType+" > 3.7";
		} else if (logBCF>=3) {
			sr.score = "H";
			sr.rationale = "3 <= "+sr.testType+" <= 3.7";
		}else if (logBCF>=2) {
			sr.score = "M";
			sr.rationale = "2 <= "+sr.testType+" < 3";
		} else {
			sr.score = "L";
			sr.rationale = sr.testType+" < 2";
		}

		sr.valueMass=logBCF;
		sr.valueMassUnits="log10(L/kg)";
		
	}
	
	private static void setBioconcentrationScoreFromMax(double logBCFmax, ScoreRecord sr) {
		if(logBCFmax<2) {
			sr.score = "L";
			sr.rationale = sr.testType+" < 2";
			sr.valueMass=logBCFmax;
			sr.valueMassUnits="log10(L/kg)";
			sr.valueMassOperator="<";
//			System.out.println(logBCFmax+"\t"+sr.score);
		}

	}
	
	private static void setBioconcentrationScoreFromMin(double logBCFmin, ScoreRecord sr) {
		
		if(logBCFmin>3.7) {
			sr.score = "VH";
			sr.rationale = sr.testType+" > 3.7";
			sr.valueMass=logBCFmin;
			sr.valueMassUnits="log10(L/kg)";
			sr.valueMassOperator=">";
//			System.out.println(logBCFmin+"\t"+sr.score);
		} else {
//			System.out.println(logBCFmin);
		}
	}
	
	
	private static void setBioconcentrationScore(ExperimentalRecord er, ScoreRecord sr) {

		DecimalFormat df = new DecimalFormat("0.00");
		
		Double logBCF=null;
		Double logBCFmax=null;
		Double logBCFmin=null;
		
//		if(er.property_value_min_final!=null && (er.property_value_min_final==200 ||er.property_value_min_final==87 || er.property_value_min_final==20 )) {
//			System.out.println(ParseUtilities.gson.toJson(er));
//		}
		
		
		if(er.property_value_point_estimate_final!=null && (er.property_value_numeric_qualifier==null || er.property_value_numeric_qualifier.equals("~"))) {
			logBCF=Math.log10(er.property_value_point_estimate_final);
		} else if(er.property_value_min_final!=null && er.property_value_max_final!=null) {
			double min=Math.log10(er.property_value_min_final);
			double max=Math.log10(er.property_value_max_final);
			if(max-min<1.0) {
				logBCF=(min+max)/2.0;
//				System.out.println(min+"\t"+max);
			} else {
				System.out.println("values too far apart:"+logBCFmin+"\t"+logBCFmax);
				return;
			}
		} else if(er.property_value_point_estimate_final!=null && (er.property_value_numeric_qualifier!=null && er.property_value_numeric_qualifier.contains("<"))) {
			logBCFmax=Math.log10(er.property_value_point_estimate_final);
		}  else if(er.property_value_point_estimate_final!=null && (er.property_value_numeric_qualifier!=null && er.property_value_numeric_qualifier.contains(">"))) {
			logBCFmin=Math.log10(er.property_value_point_estimate_final);
		} else {
			System.out.println("Can't set bcf min/max/point estimate");
		}
		
		if(logBCF!=null) {
			setBioconcentrationScore(logBCF,sr);	
		} else if (logBCFmax!=null) {
			setBioconcentrationScoreFromMax(logBCFmax, sr);
		} else if (logBCFmin!=null) {
			setBioconcentrationScoreFromMin(logBCFmin, sr);
		} else {
			System.out.println("Cant set BCF score");
		}
		

	}

	
	
	
}
