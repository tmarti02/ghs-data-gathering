package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval;

import java.util.Hashtable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CalculateRiskAssessmentClass2 {

	
	Hashtable<String,String>htStudyType=new Hashtable<>();
	Hashtable<String,String>htStudyDurationClass=new Hashtable<>();
	Hashtable<String,String>htToxvalType=new Hashtable<>();
	Hashtable<String,String>htToxvalSubtype=new Hashtable<>();
	Hashtable<String,String>htStudyDurationValueOriginal=new Hashtable<>();
	
	
	public CalculateRiskAssessmentClass2 () {
		populateDictionary_Study_Type();
		populateDictionary_Toxval_Type();
		populateDictionary_Toxval_Subtype();
		populateDictionary_Study_Duration_Class();
		populateStudyDurationValueOriginal();
		
	}
	
	public String getRACFromCriticalEffect(RecordToxVal r) {
		
		String ce=r.critical_effect;
		
		//TODO add more keywords...
		
		if (ce.contains("ataxia") || 
			ce.contains("brain") ||
			ce.contains("cholinesterase") ||
			ce.contains("CNS") ||
			ce.contains("COMA") ||
			ce.contains("convulsions") ||
			ce.contains("decreased retention (memory)") ||
			ce.contains("demyelination") ||
			ce.contains("HALLUCINATIONS") ||
			ce.contains("headache, dizziness, weakness") ||
			ce.contains("impaired reflex") ||
			ce.contains("jerking movements") ||
			ce.contains("motor and sensory function") ||
			ce.contains("nerve") ||
			ce.contains("NERVOUS SYSTEM") ||
			ce.contains("paralysis") ||
			ce.contains("Psychomotor") ||
			ce.contains("seizure") ||
			ce.contains("SENSE ORGANS") ||
			ce.contains("Spinal cord") ||
			ce.contains("TOXIC PSYCHOSIS") ||
			ce.contains("tremor") )
			
		
		
{
			return "Neurotoxicity";
		} else {
			return "Other";
		}
		
	}
	

	public String getRAC_StudyType(RecordToxVal r) {
		return getRAC(r.study_type,htStudyType);
	}
	
	public String getRAC_StudyDurationClass(RecordToxVal r) {
		return getRAC(r.study_duration_class,htStudyDurationClass);
	}
	
	public String getRAC_ToxvalType(RecordToxVal r) {
		return getRAC(r.toxval_type,htToxvalType);
	}
	
	public String getRAC_ToxvalSubtype(RecordToxVal r) {
		return getRAC(r.toxval_subtype,htToxvalSubtype);
	}
	
	public String getRAC_StudyDurationValueOriginal(RecordToxVal r) {
		return getRAC(r.study_duration_value_original,htStudyDurationValueOriginal);
	}
	
	
	public String getRAC(String val,Hashtable<String,String>ht) {
		if (ht.get(val)==null) {
			return "Other";
		} else {
			return htStudyType.get(val);
		}
	}

	void populateStudyDurationValueOriginal() {
		Hashtable<String,String>ht=htStudyDurationValueOriginal;	
		ht.put("acute","acute");
		ht.put("chronic","chronic");
		ht.put("lifetime","chronic");
		ht.put("subchronic","subchronic");

	}

	void populateDictionary_Toxval_Type() {
		Hashtable<String,String>ht=htToxvalType;		
		ht.put("AEGL 1 -  10 min (final)","acute");
		ht.put("AEGL 1 -  10 min (interim)","acute");
		ht.put("AEGL 1 -  10 min (proposed)","acute");
		ht.put("AEGL 1 -  30 min (final)","acute");
		ht.put("AEGL 1 -  30 min (interim)","acute");
		ht.put("AEGL 1 -  30 min (proposed)","acute");
		ht.put("AEGL 1 -  4 hr (final)","acute");
		ht.put("AEGL 1 -  4 hr (interim)","acute");
		ht.put("AEGL 1 -  4 hr (proposed)","acute");
		ht.put("AEGL 1 -  60 min (final)","acute");
		ht.put("AEGL 1 -  60 min (interim)","acute");
		ht.put("AEGL 1 -  60 min (proposed)","acute");
		ht.put("AEGL 1 -  8 hr (final)","acute");
		ht.put("AEGL 1 -  8 hr (interim)","acute");
		ht.put("AEGL 1 -  8 hr (proposed)","acute");
		ht.put("AEGL 2 -  10 min (final)","acute");
		ht.put("AEGL 2 -  10 min (interim)","acute");
		ht.put("AEGL 2 -  10 min (proposed)","acute");
		ht.put("AEGL 2 -  30 min (final)","acute");
		ht.put("AEGL 2 -  30 min (interim)","acute");
		ht.put("AEGL 2 -  30 min (proposed)","acute");
		ht.put("AEGL 2 -  4 hr (final)","acute");
		ht.put("AEGL 2 -  4 hr (interim)","acute");
		ht.put("AEGL 2 -  4 hr (proposed)","acute");
		ht.put("AEGL 2 -  60 min (final)","acute");
		ht.put("AEGL 2 -  60 min (interim)","acute");
		ht.put("AEGL 2 -  60 min (proposed)","acute");
		ht.put("AEGL 2 -  8 hr (final)","acute");
		ht.put("AEGL 2 -  8 hr (interim)","acute");
		ht.put("AEGL 2 -  8 hr (proposed)","acute");
		ht.put("AEGL 3 -  10 min (final)","acute");
		ht.put("AEGL 3 -  10 min (holding)","acute");
		ht.put("AEGL 3 -  10 min (interim)","acute");
		ht.put("AEGL 3 -  10 min (proposed)","acute");
		ht.put("AEGL 3 -  30 min (final)","acute");
		ht.put("AEGL 3 -  30 min (holding)","acute");
		ht.put("AEGL 3 -  30 min (interim)","acute");
		ht.put("AEGL 3 -  30 min (proposed)","acute");
		ht.put("AEGL 3 -  4 hr (final)","acute");
		ht.put("AEGL 3 -  4 hr (interim)","acute");
		ht.put("AEGL 3 -  4 hr (proposed)","acute");
		ht.put("AEGL 3 -  60 min (final)","acute");
		ht.put("AEGL 3 -  60 min (holding)","acute");
		ht.put("AEGL 3 -  60 min (interim)","acute");
		ht.put("AEGL 3 -  60 min (proposed)","acute");
		ht.put("AEGL 3 -  8 hr (final)","acute");
		ht.put("AEGL 3 -  8 hr (interim)","acute");
		ht.put("AEGL 3 -  8 hr (proposed)","acute");
		ht.put("LC","acute");
		ht.put("LC0","acute");
		ht.put("LC1","acute");
		ht.put("LC10","acute");
		ht.put("LC100","acute");
		ht.put("LC20","acute");
		ht.put("LC50","acute");
		ht.put("LC65","acute");
		ht.put("LC90","acute");
		ht.put("LD0","acute");
		ht.put("LD100","acute");
		ht.put("LD5","acute");
		ht.put("LD50","acute");
		ht.put("LD80","acute");
		ht.put("Air contaminant limit","air quality");
		ht.put("cancer unit risk","cancer");
		ht.put("ABS","chemical property");
		ht.put("Csat","chemical property");
		ht.put("GIABS","chemical property");
		ht.put("AOEL","chronic");
		ht.put("AOEL (provisional)","chronic");
		ht.put("ADI","chronic");
		ht.put("cancer slope factor","chronic");
		ht.put("Cumulative Dietary Concentration","chronic");
		ht.put("Cumulative Estimated Daily Intake","chronic");
		ht.put("MCL","chronic");
		ht.put("MCL California","chronic");
		ht.put("MCL Federal","chronic");
		ht.put("MCL-based SSL","chronic");
		ht.put("MTD","chronic");
		ht.put("No significant risk level","chronic");
		ht.put("OEHHA PHG","chronic");
		ht.put("REL","chronic");
		ht.put("RfC","chronic");
		ht.put("RfD","chronic");
		ht.put("risk-based SSL","chronic");
		ht.put("screening level (industrial air)","chronic");
		ht.put("screening level (industrial soil)","chronic");
		ht.put("screening level (residential air)","chronic");
		ht.put("screening level (residential Soil)","chronic");
		ht.put("screening level (tap water)","chronic");
		ht.put("tolerable concentration in air","chronic");
		ht.put("TTC","chronic");
		ht.put("TTC Cramer Class I","chronic");
		ht.put("TTC Cramer Class II","chronic");
		ht.put("TTC Cramer Class III","chronic");
		ht.put("unit risk","chronic");
		ht.put("TDI","chronic");
		ht.put("DWEL","chronic");
		ht.put("HBSL","chronic");
		ht.put("UL","chronic");
		ht.put("CMAC","chronic");
		ht.put("RFCi","chronic");
		ht.put("RFDo","chronic");
		ht.put("HEC5","chronic");
		ht.put("HEC5","chronic");
		ht.put("HNEL","subchronic");
		
		ht.put("NOAEL","repeat dose");
		ht.put("LED05","acute");
		ht.put("BMD2x-ADJ","chronic");
		ht.put("NOAEL05-ADJ","chronic");


	}
	
	void todo() {
		/**
		 *  Special Toxicology Study	*	study_type_original	special toxicity study	2
		 *  DOE Wildlife Benchmarks	*	source	chronic
		 *  clinical observation	*	critical_effect	clinical	5

 
		 */
					

	}
	
	
	void populateDictionary_Toxval_Subtype() {
		Hashtable<String,String>ht=htToxvalSubtype;
		
		ht.put("10-kg child 10 day","acute");
		ht.put("10-kg child one day","acute");
		ht.put("Acute toxicity: dermal","acute");
		ht.put("Acute toxicity: inhalation","acute");
		ht.put("Acute toxicity: oral","acute");
		ht.put("Acute toxicity: other routes","acute");
		ht.put("PAC_1","acute");
		ht.put("PAC_2","acute");
		ht.put("PAC_3","acute");
		ht.put("10-kg child 10 day","acute ");
		ht.put("10-kg child one day","acute ");
		ht.put("Acute toxicity: dermal","acute ");
		ht.put("Acute toxicity: inhalation","acute ");
		ht.put("Acute toxicity: oral","acute ");
		ht.put("Acute toxicity: other routes","acute ");
		ht.put("Cancer","cancer");
		ht.put("Cancer at 1E-4","cancer");
		ht.put("Chronic, non-cancer","chronic");
		ht.put("Long-term Negligible Air","chronic");
		ht.put("Long-term toxicity to fish","chronic");
		ht.put("NTP  long-term","chronic");
		ht.put("Lifetime","chronic");
		ht.put("Developmental neurotoxicity (neonatal exposure)","developmental neurotoxicity");
		ht.put("Toxicity to aquatic algae and cyanobacteria","ecotoxicity invertebrate");
		ht.put("Toxicity to microorganisms","ecotoxicity invertebrate");
		ht.put("Toxicity to other above-ground organisms","ecotoxicity invertebrate");
		ht.put("Toxicity to other aquatic organisms","ecotoxicity invertebrate");
		ht.put("Toxicity to soil macroorganisms except arthropods","ecotoxicity invertebrate");
		ht.put("Toxicity to soil microorganisms","ecotoxicity invertebrate");
		ht.put("Toxicity to terrestrial arthropods","ecotoxicity invertebrate");
		ht.put("Toxicity to aquatic plants other than algae","ecotoxicity plants");
		ht.put("Toxicity to terrestrial plants","ecotoxicity plants");
		ht.put("OECD TG 407","repeat dose");
		ht.put("Repeated dose toxicity: dermal","repeat dose");
		ht.put("Repeated dose toxicity: inhalation","repeat dose");
		ht.put("Repeated dose toxicity: oral","repeat dose");
		ht.put("OECD TG 421","reproductive developmental");
		ht.put("OECD TG 422","reproductive developmental");
		ht.put("NTP  short-term","short-term");
		ht.put("Short-term Catastrophic Air","short-term");
		ht.put("Short-term Critical Air","short-term");
		ht.put("Short-term Marginal Air","short-term");
		ht.put("Short-term Negligible Air","short-term");
		ht.put("Short-term toxicity to aquatic invertebrates","short-term");
		ht.put("Short-term toxicity to fish","short-term");
		ht.put("Short-Term, 15L/d Negligible Water","short-term");
		ht.put("Short-Term, 5L/d Negligible Water","short-term");
		ht.put("NO TRV","subchronic");
		ht.put("LO TRV","subchronic");
		ht.put("OTHER TRV","subchronic");
		ht.put("groundwater:Nonuse Aquifers, Nonresidential:","water quality");
		ht.put("groundwater:Nonuse Aquifers, Nonresidential:Aqueous solubility cap","water quality");
		ht.put("groundwater:Nonuse Aquifers, Nonresidential:inhalation","water quality");
		ht.put("groundwater:Nonuse Aquifers, Nonresidential:Lifetime health advisory","water quality");
		ht.put("groundwater:Nonuse Aquifers, Nonresidential:oral","water quality");
		ht.put("groundwater:Nonuse Aquifers, Residential:","water quality");
		ht.put("groundwater:Nonuse Aquifers, Residential:Aqueous solubility cap","water quality");
		ht.put("groundwater:Nonuse Aquifers, Residential:inhalation","water quality");
		ht.put("groundwater:Nonuse Aquifers, Residential:Lifetime health advisory","water quality");
		ht.put("groundwater:Nonuse Aquifers, Residential:oral","water quality");
		ht.put("groundwater:Used Aquifers, TDS<2500, Nonresidential:","water quality");
		ht.put("groundwater:Used Aquifers, TDS<2500, Nonresidential:Aqueous solubility cap","water quality");
		ht.put("groundwater:Used Aquifers, TDS<2500, Nonresidential:inhalation","water quality");
		ht.put("groundwater:Used Aquifers, TDS<2500, Nonresidential:Lifetime health advisory","water quality");
		ht.put("groundwater:Used Aquifers, TDS<2500, Nonresidential:oral","water quality");
		ht.put("groundwater:Used Aquifers, TDS<2500, Residential:","water quality");
		ht.put("groundwater:Used Aquifers, TDS<2500, Residential:Aqueous solubility cap","water quality");
		ht.put("groundwater:Used Aquifers, TDS<2500, Residential:inhalation","water quality");
		ht.put("groundwater:Used Aquifers, TDS<2500, Residential:Lifetime health advisory","water quality");
		ht.put("groundwater:Used Aquifers, TDS<2500, Residential:oral","water quality");
		ht.put("groundwater:Used Aquifers, TDS>2500, Nonresidential:","water quality");
		ht.put("groundwater:Used Aquifers, TDS>2500, Nonresidential:Aqueous solubility cap","water quality");
		ht.put("groundwater:Used Aquifers, TDS>2500, Nonresidential:inhalation","water quality");
		ht.put("groundwater:Used Aquifers, TDS>2500, Nonresidential:Lifetime health advisory","water quality");
		ht.put("groundwater:Used Aquifers, TDS>2500, Nonresidential:oral","water quality");
		ht.put("groundwater:Used Aquifers, TDS>2500, Residential:","water quality");
		ht.put("groundwater:Used Aquifers, TDS>2500, Residential:Aqueous solubility cap","water quality");
		ht.put("groundwater:Used Aquifers, TDS>2500, Residential:inhalation","water quality");
		ht.put("groundwater:Used Aquifers, TDS>2500, Residential:Lifetime health advisory","water quality");
		ht.put("groundwater:Used Aquifers, TDS>2500, Residential:oral","water quality");

		
	}
	

	
	void populateDictionary_Study_Duration_Class() {
		Hashtable<String,String>ht=htStudyDurationClass;
		ht.put("8-hr","acute");
		ht.put("acute","acute");
		ht.put("acute toxicity","acute");
		ht.put("single dose","acute");
		ht.put("cancer","cancer");
		ht.put("case report of chronic self-intoxication","chronic");
		ht.put("chronic","chronic");
		ht.put("epidemiological (chronic)","chronic");
		ht.put("epidemiological (chronic, occupational)","chronic");
		ht.put("epidemiological (cohort)","chronic");
		ht.put("epidemiological (natural exposure)","chronic");
		ht.put("epidemiological (occupational exposure)","chronic");
		ht.put("epidemiological (occupational, cohort)","chronic");
		ht.put("epidemiological (occupationally exposed cohort)","chronic");
		ht.put("subchronic-chronic","chronic, subchronic");
		ht.put("developmental (single generation)","developmental");
		ht.put("reproduction toxicity","reproductive");
		ht.put("two-generation reproductive toxicity","reproductive");
		ht.put("short-term toxicity","short-term");
		ht.put("Subchronic","subchronic");
		ht.put("Sub-chronic","subchronic");
		ht.put("subchronic prospective supplementation trial","subchronic");
		ht.put("subchronic, developmental","subchronic, developmental");
		ht.put("subchronic, developmental/reproductive","subchronic, developmental/reproductive");
		ht.put("subchronic, reproductive","subchronic, reproductive");

	}
	
	
	
	
	void populateDictionary_Study_Type() {
		
		Hashtable<String,String>ht=htStudyType;
		ht.put("acute","acute");
		ht.put("OECD GL 401","acute");
		ht.put("OECD TG 401","acute");
		ht.put("Single limit dose","acute");
		ht.put("Special acute respiratory reflex test","acute");
		ht.put("AcuteToxicityDermal NA","acute");
		ht.put("AcuteToxicityInhalation NA","acute");
		ht.put("AcuteToxicityOral NA","acute");
		ht.put("cancer","cancer");
		ht.put("Carcinogenicity NA","cancer");
		ht.put("cancer, repeat dose","cancer, repeat dose");
		ht.put("chronic","chronic");
		ht.put("life-term","chronic");
		ht.put("LongTermToxToFish NA","chronic");
		ht.put("LongTermToxicityToAquaInv NA","chronic");
		ht.put("chronic, subchronic","chronic, subchronic");
		ht.put("clinical","clinical");
		ht.put("developmental","developmental");
		ht.put("developmental neurotoxicity","developmental neurotoxicity");
		ht.put("genetox","genotoxicity");
		ht.put("genotoxicity, repeat dose","genotoxicity, repeat dose");
		ht.put("human","human");
		ht.put("Immunotoxicology","immunotoxicology");
		ht.put("in vitro","in vitro");
		ht.put("multigeneration","reproductive");
		ht.put("multigenerational reproductive","reproductive");
		ht.put("neurotoxicity","neurotoxicity");
		ht.put("Neurotoxicity NA","neurotoxicity");
		ht.put("ToxicityToAquaticAlgae NA","other");
		ht.put("ToxicityToOtherAqua NA","other");
		ht.put("repeat dose","repeat dose");
		ht.put("repeat dose toxicity","repeat dose");
		ht.put("RepeatedDoseToxicityDermal NA","repeat dose");
		ht.put("RepeatedDoseToxicityInhalation NA","repeat dose");
		ht.put("RepeatedDoseToxicityOral NA","repeat dose");
		ht.put("RepeatedDoseToxicityOther NA","repeat dose");
		ht.put("reproductive","reproductive");
		ht.put("reproductive multigeneration","reproductive");
		ht.put("ToxicityReproductionOther NA","reproductive");
		ht.put("OECD 422 study","reproductive developmental");
		ht.put("reproductive developmental","reproductive developmental");
		ht.put("reproductive/developmental","reproductive developmental");
		ht.put("ToxicityReproduction F1","reproductive F1");
		ht.put("ToxicityReproduction F1a","reproductive F1");
		ht.put("ToxicityReproduction F1b","reproductive F1");
		ht.put("ToxicityReproduction F2","reproductive F2");
		ht.put("ToxicityReproduction F2a","reproductive F2");
		ht.put("ToxicityReproduction F2b","reproductive F2");
		ht.put("ToxicityReproduction P0","reproductive P0");
		ht.put("short-term","short-term");
		ht.put("ShortTermToxicityToAquaInv NA","short-term");
		ht.put("ShortTermToxicityToFish NA","short-term");
		ht.put("subacute","subacute");
		ht.put("subacute, neurotoxicity","subacute, neurotoxicity");
		ht.put("subacute, subchronic","subacute, subchronic");
		ht.put("medium term","subchronic");
		ht.put("subchronic","subchronic");

	}
	
	/**
	 * Temporary class so can easily output the results for viewing
	 * @author Todd Martin
	 *
	 */
	
	class RACS {
		
		String human_eco;
		String habitat;
		
		String study_type;
		String racStudyType;
		
		String toxval_type;
		String racToxvalType;
		
		String toxval_subtype;
		String racToxvalSubType;
		
		String study_duration_original;
		String racStudyDurationValueOriginal;
		
		String study_duration_class;
		String racStudyDurationClass;
		
		String racCriticalEffect;
		String CriticalEffect;
		
		String risk_assessment_class_toxvalv8;//what was stored in the database
		
	}
	
	public String getRAC(RecordToxVal r) {
		
		String rac="Other";
		
		RACS racs=new RACS();
		
		racs.human_eco=r.human_eco;
		racs.habitat=r.habitat;
		
		
		racs.racStudyType=getRAC_StudyType(r);
		racs.study_type=r.study_type;
		
		racs.toxval_type=r.toxval_type;
		racs.racToxvalType=getRAC_ToxvalType(r);
				
		racs.toxval_subtype=r.toxval_subtype;
		racs.racToxvalSubType=getRAC_ToxvalSubtype(r);
		
		racs.study_duration_original=r.study_duration_units_original;
		racs.racStudyDurationValueOriginal=getRAC_StudyDurationValueOriginal(r);
		
		
		racs.study_duration_class=r.study_duration_class;
		racs.racStudyDurationClass=getRAC_StudyDurationClass(r);
		
		racs.racCriticalEffect=getRACFromCriticalEffect(r);
		racs.CriticalEffect=r.critical_effect;
		
		racs.risk_assessment_class_toxvalv8=r.risk_assessment_class;
		
		
		//TODO use all the intermediate RACS to assign final RAC!
		

		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		String json=gson.toJson(racs);

		//Print out so can look at the values:
		
		
//		System.out.println("RACS="+json);
		
		
		return rac;
	}
	
		
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		

	}

}
