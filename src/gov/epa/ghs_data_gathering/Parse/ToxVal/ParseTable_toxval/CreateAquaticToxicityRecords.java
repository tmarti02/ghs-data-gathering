package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.epa.api.Chemical;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ParameterValue;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseToxValDB;

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

	
	public static boolean validAquaticSpeciesToxvalv94(String species_scientific) {
		
		//Toxval v8 was queried for valid species which was then matched up with species in toxval v94 latin names using a spreadsheet
		/**
		 * select * from species where habitat='aquatic' and
		 * lower(species_supercategory) like '%standard test species%' and
		 * lower(species_supercategory) not like '%nuisance%' and 
		 * (species_supercategory like '%fish%' or 
		 * species_supercategory like '%algae%' or
		 * species_supercategory like '%crustacean%');
		 */
		
//		List<String> valid_latin_names = Arrays.asList("Alburnus alburnus", "Americamysis bahia", "Ampelisca abdita",
//				"Anabaena cylindrica", "Anabaena flosaquae", "Ankistrodesmus sp.", "Atherinops affinis",
//				"Catostomus commersoni", "Ceriodaphnia dubia", "Champia parvula", "Chlamydomonas reinhardtii",
//				"Chlorella vulgaris", "Cymatogaster aggregata", "Cypridopsis sp.", "Cyprinodon variegatus",
//				"Danio rerio", "Daphnia magna", "Daphnia pulex", "Dicentrarchus labrax", "Diporeia sp.",
//				"Eohaustorius estuarius", "Esox lucius", "Gammarus lacustris", "Gammarus pseudolimnaeus",
//				"Grandidierella japonica", "Hyalella azteca", "Ictalurus punctatus", "Jordanella floridae",
//				"Leiostomus xanthurus", "Lepomis macrochirus", "Leptocheirus plumulosus", "Menidia beryllina",
//				"Menidia menidia", "Menidia peninsulae", "Navicula pelliculosa", "Oncorhynchus kisutch",
//				"Oncorhynchus mykiss", "Oncorhynchus tshawytscha", "Oryzias latipes", "Parophrys vetulus",
//				"Penaeus aztecus", "Penaeus setiferus", "Pimephales promelas", "Poecilia reticulata",
//				"Rhepoxynius abronius", "Salmo salar", "Salmo trutta", "Salvelinus fontinalis", "Salvelinus namaycush",
//				"Scenedesmus acutus", "Skeletonema costatum", "Stigeoclonium sp.", "Pseudokirchneriella sp.",
//				"Farfantepenaeus duorarum");
		
		List<String> valid_latin_names=new ArrayList<String>();
		
		valid_latin_names.add("Alburnus alburnus");
		valid_latin_names.add("Americamysis bahia");
		valid_latin_names.add("Ampelisca abdita");
		valid_latin_names.add("Anabaena cylindrica");
		valid_latin_names.add("Anabaena flosaquae");
		valid_latin_names.add("Ankistrodesmus sp.");
		valid_latin_names.add("Atherinops affinis");
		valid_latin_names.add("Catostomus commersoni");
		valid_latin_names.add("Ceriodaphnia dubia");
		valid_latin_names.add("Champia parvula");
		valid_latin_names.add("Chlamydomonas reinhardtii");
		valid_latin_names.add("Chlorella vulgaris");
		valid_latin_names.add("Cymatogaster aggregata");
		valid_latin_names.add("Cypridopsis sp.");
		valid_latin_names.add("Cyprinodon variegatus");
		valid_latin_names.add("Danio rerio");
		valid_latin_names.add("Daphnia magna");
		valid_latin_names.add("Daphnia pulex");
		valid_latin_names.add("Dicentrarchus labrax");
		valid_latin_names.add("Diporeia sp.");
		valid_latin_names.add("Eohaustorius estuarius");
		valid_latin_names.add("Esox lucius");
		valid_latin_names.add("Gammarus lacustris");
		valid_latin_names.add("Gammarus pseudolimnaeus");
		valid_latin_names.add("Grandidierella japonica");
		valid_latin_names.add("Hyalella azteca");
		valid_latin_names.add("Ictalurus punctatus");
		valid_latin_names.add("Jordanella floridae");
		valid_latin_names.add("Leiostomus xanthurus");
		valid_latin_names.add("Lepomis macrochirus");
		valid_latin_names.add("Leptocheirus plumulosus");
		valid_latin_names.add("Menidia beryllina");
		valid_latin_names.add("Menidia menidia");
		valid_latin_names.add("Menidia peninsulae");
		valid_latin_names.add("Navicula pelliculosa");
		valid_latin_names.add("Oncorhynchus kisutch");
		valid_latin_names.add("Oncorhynchus mykiss");
		valid_latin_names.add("Oncorhynchus tshawytscha");
		valid_latin_names.add("Oryzias latipes");
		valid_latin_names.add("Parophrys vetulus");
		valid_latin_names.add("Penaeus aztecus");
		valid_latin_names.add("Penaeus setiferus");
		valid_latin_names.add("Pimephales promelas");
		valid_latin_names.add("Poecilia reticulata");
		valid_latin_names.add("Rhepoxynius abronius");
		valid_latin_names.add("Salmo salar");
		valid_latin_names.add("Salmo trutta");
		valid_latin_names.add("Salvelinus fontinalis");
		valid_latin_names.add("Salvelinus namaycush");
		valid_latin_names.add("Scenedesmus acutus");
		valid_latin_names.add("Skeletonema costatum");
		valid_latin_names.add("Stigeoclonium sp.");
		valid_latin_names.add("Pseudokirchneriella sp.");//found manually in list of toxvalv94 species
		valid_latin_names.add("Farfantepenaeus duorarum");//found manually in list of toxvalv94 species
		
		return valid_latin_names.contains(species_scientific);
	}
	
	
	public static ScoreRecord createDurationRecord(ExperimentalRecord er) {

		ScoreRecord sr = new ScoreRecord(Chemical.strAcute_Aquatic_Toxicity,er.casrn,er.chemical_name);	
		sr.dtxsid=er.dsstox_substance_id;
		sr.source = er.source_name;
		
		String test_id=(String)er.experimental_parameters.get("test_id");
		String result_id=(String)er.experimental_parameters.get("result_id");
		String testType=(String)er.experimental_parameters.get("test_type");
		String effect=(String)er.experimental_parameters.get("Effect");
		String species_super_category=(String)er.experimental_parameters.get("Species supercategory");

		Double study_dur_in_days = getStudyDurationDays(er);
		if(study_dur_in_days==null) {
//			System.out.println("Couldnt get study duration for test_id="+test_id+", result_id="+result_id);
			return null;
		}

		if(er.property_value_point_estimate_final==null) {
			System.out.println("No point estimate for test_id="+test_id+", result_id="+result_id);
			return null;
		}
		
		double diff=0.1;
		double diff4day=Math.abs(4.0-study_dur_in_days)/4.0;
		double diff2day=Math.abs(2.0-study_dur_in_days)/2.0;
		double diff3day=Math.abs(3.0-study_dur_in_days)/3.0;

		boolean fish4day = species_super_category.toLowerCase().contains("fish") && diff4day<diff;
		boolean crustacean2day = species_super_category.toLowerCase().contains("crustacean") && diff2day<diff;
		boolean algae3or4day = species_super_category.toLowerCase().contains("algae") && (study_dur_in_days>=3*(1-diff) && study_dur_in_days <= 4*(1+diff));

//		boolean fish4day = species_super_category.toLowerCase().contains("fish") && study_dur_in_days==4;
//		boolean crustacean2day = species_super_category.toLowerCase().contains("crustacean") && study_dur_in_days==2;
//		boolean algae3or4day = species_super_category.toLowerCase().contains("algae") && (study_dur_in_days==3 || study_dur_in_days==4);
		
		String species_scientific=(String)er.experimental_parameters.get("Species latin");
		String species_common=(String)er.experimental_parameters.get("Species common");

		String species_type=(String)er.experimental_parameters.get("Species type");
		boolean validSpecies=validAquaticSpeciesToxvalv94(species_scientific);

//		System.out.println(species_common+"\t"+species_type+"\t"+validSpecies);
		
		boolean validAcute=fish4day || crustacean2day || algae3or4day;
		
//		if(validAcute)
//			System.out.println(species_super_category+"\t"+study_dur_in_days+"\t"+validAcute);
		
		sr.duration=study_dur_in_days+"";
		sr.durationUnits="days";
		
		sr.valueMassOperator=er.property_value_numeric_qualifier;
		if(sr.valueMassOperator==null)sr.valueMassOperator="";
		
		sr.valueMass = er.property_value_point_estimate_final;
		sr.valueMassUnits = er.property_value_units_final;
		
		sr.listType=ScoreRecord.typeScreening;//experimental data
		sr.testOrganismType=species_super_category;
		sr.testOrganism=species_common;
		sr.testType=testType;
		sr.effect=effect;
		
		if(er.literatureSource!=null) {
			sr.longRef=er.literatureSource.citation;
			sr.sourceOriginal=er.literatureSource.author+", "+er.literatureSource.year;
//			System.out.println(sr.sourceOriginal);
		}
		
		if(er.publicSourceOriginal!=null) {
			sr.sourceOriginal=er.publicSourceOriginal.name;
		} else if (er.original_source_name!=null) {
			sr.sourceOriginal=er.original_source_name;
		}
		
		if(er.literatureSource!=null && er.literatureSource.url!=null) {
			sr.url=er.literatureSource.url;
		} else if(er.publicSourceOriginal!=null && er.publicSourceOriginal.url!=null) {
			sr.url=er.publicSourceOriginal.url;
		}
		

		if ((testType.contentEquals("LC50") || testType.contentEquals("EC50")) &&  validAcute) {
			setAquaticToxAcuteScore(sr);
		} else if ((study_dur_in_days>6) && (testType.contentEquals("NOEC") || testType.contentEquals("LOEC"))) {
//  	For chronic aquatic toxicity, the GHS criteria document says "durations can vary widely depending on the test purpose
//		(anywhere from 7 days to over 200 days).
//		So we're making the criteria > 6 days.	"							
			setAquaticToxChronicScore(sr);
		} else {
//			System.out.println("Couldnt create record for test_id="+test_id+", result_id="+result_id+",testType="+testType+",studyDuration="+study_dur_in_days+",species_super_category="+species_super_category);
			return null;
		}

		return sr;
		
	}


	private static Double getStudyDurationDays(ExperimentalRecord er) {
		Double study_dur_in_days=null;
		
		if(er.parameter_values!=null) {
			for (ParameterValue pv:er.parameter_values) {
				if(pv.parameter.name.equals("Observation duration")) {
					if(pv.valuePointEstimate!=null && pv.unit.abbreviation.equals("days")) {
						study_dur_in_days=pv.valuePointEstimate;
					} else {
//						System.out.println("Cant set observation duration for result_id="+er.experimental_parameters.get("result_id"));
					}
				}
			}
		}
		return study_dur_in_days;
	}
	
	
	
	
	// I added duration-based criteria based on GHS criteria,
	// which are based on OECD test guidelines:
	// Test Guideline 210, Page 13 
	// https://www.oecd-ilibrary.org/docserver/9789264203785-en.pdf?expires=1599094196&id=id&accname=guest&checksum=3DD962D873D642CBF90D56FED10E8D6E
	// 	
	// 96 hours = 4 days so < 5 days = acute
	// 14 days = 2 weeks so > 13 days = chronic
	// It seems a bit silly to have to create a duration record just to
	// be able to generate our own variable (study_dur_in_days)
	// but I think that's the way to do it in Java.

	//TODO- does it need to be lethality as effect??? or is growth ok?
	// GHS criteria are based on chronic tests for endpoints that "can include
	// "hatching success, growth (length and weight changes), spawning success, and survival."

//  We further restricted the criteria for acute aquatic toxicity based on the GHS criteria document:
//	"Acute aquatic toxicity is normally determined using a fish 96 hour LC50 (OECD Test Guideline 203 or equivalent),
//  a crustacea species 48 hour EC50 (OECD Test Guideline 202 or equivalent) and/or
//  an algal species 72 or 96 hour EC50 (OECD Test Guideline 201 or equivalent).
//  These species are considered as surrogate for all aquatic organisms and
//  data on other species (e.g. Lemna spp.) may also be considered if the test methodology is suitable."
//	Our criteria aim to include only standard test methods for consistency across chemicals.	

	
	/**
	 * 
	 * 
	 * @param chemical
	 * @param tr
	 */

	public static void createDurationRecord(Chemical chemical, RecordToxVal tr) {

//		System.out.println("aquatic");
		
		Double study_dur_in_days=null;
		double study_duration_value = Double.parseDouble(tr.study_duration_value);

		//in toxval 94 units became plural
		if (tr.study_duration_units.contentEquals("day") || tr.study_duration_units.contentEquals("days")) {
			study_dur_in_days=study_duration_value;
		} else if (tr.study_duration_units.contentEquals("week") || tr.study_duration_units.contentEquals("weeks")) {
			study_dur_in_days=study_duration_value*7.0;
		} else if (tr.study_duration_units.contentEquals("month") || tr.study_duration_units.contentEquals("months")) {
			study_dur_in_days=study_duration_value*30.0;
		} else if (tr.study_duration_units.contentEquals("year") || tr.study_duration_units.contentEquals("years")) {
			study_dur_in_days=study_duration_value*365.0;
		} else if (tr.study_duration_units.contentEquals("hour") || tr.study_duration_units.contentEquals("hours")) {
			study_dur_in_days=study_duration_value/24.0;
		} else if (tr.study_duration_units.contentEquals("minute") || tr.study_duration_units.contentEquals("minutes")) {
			study_dur_in_days=study_duration_value/1440.0;
		} else if (tr.study_duration_units.contentEquals("-")) {
			return;
		} else {
			if(ParseToxValDB.debug)
				System.out.println("unknown units="+tr.study_duration_units);
			return;
		}

//		if (tr.species_supercategory.toLowerCase().contains("exotic") ||
//				tr.species_supercategory.toLowerCase().contains("nuisance") ||
//				tr.species_supercategory.toLowerCase().contains("invasive"))
//			return;
		// Excluding invasive species.
		//		if ((study_dur_in_days<5) &&
		
		
		String species = tr.species_supercategory.toLowerCase();
		
		int maxPercentDiff=10;
		
		double percentDiff4day=Math.abs(4.0-study_dur_in_days)/4.0*100;
		double percentDiff2day=Math.abs(2.0-study_dur_in_days)/2.0*100;
		double percentDiff3day=Math.abs(3.0-study_dur_in_days)/3.0*100;
		
		boolean fish4day = species.contains("fish") && percentDiff4day<maxPercentDiff;
		boolean crustacean2day = species.contains("crustacean") && percentDiff2day<maxPercentDiff;
		boolean algae3or4day = species.contains("algae") && (percentDiff3day<10 || percentDiff4day<maxPercentDiff);
		
		if ((tr.toxval_type.contentEquals("LC50") || tr.toxval_type.contentEquals("EC50")) &&
				(fish4day || crustacean2day || algae3or4day)) {
			Score score=chemical.scoreAcute_Aquatic_Toxicity;

			ScoreRecord sr = ParseToxVal.saveToxValInfo(score,tr, chemical);

			sr.duration=study_dur_in_days+"";
			sr.durationUnits="days";

			setAquaticToxAcuteScore(sr);
			score.records.add(sr);

			
		} else if ((study_dur_in_days>6) && (tr.toxval_type.contentEquals("NOEC") || tr.toxval_type.contentEquals("LOEC"))) {

//		} else if ((study_dur_in_days>13) &&
//  	For chronic aquatic toxicity, the GHS criteria document says "durations can vary widely depending on the test purpose
//		(anywhere from 7 days to over 200 days).
//		So we're making the criteria > 6 days.	"							
			
			Score score=chemical.scoreChronic_Aquatic_Toxicity;
			ScoreRecord sr = ParseToxVal.saveToxValInfo(score,tr,chemical);
			sr.duration=study_dur_in_days+"";
			sr.durationUnits="days";
			setAquaticToxChronicScore(sr);
			score.records.add(sr);

		}
	}

	//	static void createAquaticToxAcuteRecords(Chemical chemical, RecordToxVal tr, DurationRecord dr) {
	//		ScoreRecord sr = ParseToxVal.saveToxValInfo(tr);
	//		sr.duration=study_dur_in_days;
	//		sr.durationUnits="days";
	//		setAquaticToxAcuteScore(sr, chemical);
	//		chemical.scoreAcute_Aquatic_Toxicity.records.add(sr);
	//	}
	//
	//
	//
	//	static void createAquaticToxChronicRecords(Chemical chemical, RecordToxVal tr) {
	//		ScoreRecord sr = ParseToxVal.saveToxValInfo(tr);
	//		setAquaticToxChronicScore(sr, chemical);		
	//		chemical.scoreChronic_Aquatic_Toxicity.records.add(sr);
	//	}
	//	



	private static void setAquaticToxChronicScore(ScoreRecord sr) {

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

		if (sr.valueMassOperator.equals(">")) {

			if (dose >= 10) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = sr.testType+" > 10 mg/L";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = sr.testType+" does not provide enough information to assign a score";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			}

		} else if (sr.valueMassOperator.equals("<")) {
			if (dose <=0.1) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = sr.testType+" < 0.1 mg/L";
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = sr.testType+" does not provide enough information to assign a score";

				// System.out.println(chemical.CAS + "\tless than operator detected for oral\t" + dose);
			}

		} else if (sr.valueMassOperator.equals("") || sr.valueMassOperator.equals("=") || sr.valueMassOperator.equals("~") || sr.valueMassOperator.equals(">=") || sr.valueMassOperator.equals("<=")) {

			if (dose < 0.1) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = sr.testType+" < 1 mg/L";
			} else if (dose >= 0.1 && dose <= 1) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = "0.1 mg/L <= "+sr.testType+" <=1 mg/L";
			} else if (dose > 1 && dose <= 10) {
				sr.score = ScoreRecord.scoreM;
				sr.rationale = "1 mg/L < "+sr.testType+" <=10 mg/L";
			} else if (dose > 10) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = sr.testType+ " > 10 mg/L";
			} else { 
				System.out.println(sr.CAS + "\tEcoToxChronic\t" + strDose);
			}

		}
	}


	private static void setAquaticToxAcuteScore(ScoreRecord sr) {

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

		if (sr.valueMassOperator.equals(">")) {

			if (dose >= 100) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = sr.testType+" > 100 mg/L";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = sr.testType+" does not provide enough information to assign a score";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			}

		} else if (sr.valueMassOperator.equals("<")) {
			if (dose <=1) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = sr.testType+" < 1 mg/L";
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = sr.testType+" does not provide enough information to assign a score";

				// System.out.println(chemical.CAS + "\tless than operator detected for oral\t" + dose);
			}

		} else if (sr.valueMassOperator.equals("") || sr.valueMassOperator.equals("=") || sr.valueMassOperator.equals("~") || sr.valueMassOperator.equals(">=") || sr.valueMassOperator.equals("<=")) {

			if (dose < 1) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = sr.testType + " < 1 mg/L";
			} else if (dose >= 1 && dose <= 10) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = "1 mg/kg <= "+sr.testType+" <=10 mg/L";
			} else if (dose > 10 && dose <= 100) {
				sr.score = ScoreRecord.scoreM;
				sr.rationale = "10 mg/kg < "+sr.testType+" <=100 mg/L";
			} else if (dose > 100) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = sr.testType+" > 100 mg/L";
			} else { System.out.println(sr.CAS + "\tEcoToxAcute\t" + strDose);

			}
		}
	}
}
