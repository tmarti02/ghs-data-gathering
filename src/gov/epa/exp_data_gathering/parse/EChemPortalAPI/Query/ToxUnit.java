package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query;

/**
 * Replicates the Unit object of an eChemPortal API search query JSON specific to toxicity properties
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class ToxUnit extends Unit {
	
	/**
	 * Translates our unit identifiers to eChemPortal's Unit objects for toxicity properties
	 * @param unit	Desired unit from APIConstants
	 */
	ToxUnit(String unit,String endpointKind) {
		switch (unit) {
		case APIConstants.mg_kg_bw:
			phraseGroupId = "T04";
			phraseId = "2081";
			break;
		case APIConstants.mL_kg_bw:
			phraseGroupId = "T04";
			phraseId = "2119";
			break;
		case APIConstants.mg_m2:
			phraseGroupId = "T04";
			phraseId = "2103";
			break;
		case APIConstants.mg_L_air:
			phraseGroupId = "T07";
			phraseId = "2099";
			break;
		case APIConstants.mg_L_air_nom:
			phraseGroupId = "T07";
			phraseId = "2101";
			break;
		case APIConstants.mg_L_air_anal:
			phraseGroupId = "T07";
			phraseId = "2100";
			break;
		case APIConstants.mg_m3_air:
			phraseGroupId = "T07";
			phraseId = "2104";
			break;
		case APIConstants.mg_m3_air_nom:
			phraseGroupId = "T07";
			phraseId = "2106";
			break;
		case APIConstants.mg_m3_air_anal:
			phraseGroupId = "T07";
			phraseId = "2105";
			break;
		case APIConstants.ppm:
			phraseGroupId = "T07";
			phraseId = "2283";
			break;
		case APIConstants.mg_L:
			phraseGroupId = "T12";
			phraseId = "2098";
			break;
		case APIConstants.mg_kg_bw_day_nom:
			phraseGroupId = "T28-1";
			phraseId = "2087";
			break;
		case APIConstants.mg_kg_bw_day_act:
			phraseGroupId = "T28-1";
			phraseId = "2086";
			break;
		case APIConstants.mg_kg_diet:
			phraseGroupId = "T28-1";
			phraseId = "2090";
			break;
		case APIConstants.mg_L_water:
			phraseGroupId = "T28-1";
			phraseId = "2102";
			break;
		case APIConstants.mg_kg_bw_tot:
			phraseGroupId = "T28-1";
			phraseId = "2082";
			break;
		case APIConstants.mg_kg_bw_day:
			phraseGroupId = "T28-3";
			phraseId = "2085";
			break;
		case APIConstants.mg_cm2_per_day:
			phraseGroupId = "T28-3";
			phraseId = "2078";
			break;
		case APIConstants.mg_cm2_per_day_nom:
			phraseGroupId = "T28-3";
			phraseId = "2080";
			break;
		case APIConstants.mg_cm2_per_day_anal:
			phraseGroupId = "T28-3";
			phraseId = "2079";
			break;
		case APIConstants.ppm_nom:
			phraseGroupId = "T28-5";
			phraseId = "2285";
			break;
		case APIConstants.ppm_anal:
			phraseGroupId = "T28-5";
			phraseId = "2284";
			break;
		}
		
		switch (endpointKind) {
		case APIConstants.acuteToxicityOther:
			phraseGroupId = "T12";
			break;
		case APIConstants.repeatedDoseToxicityOral:
			phraseGroupId = "T28-1";
			break;
		case APIConstants.repeatedDoseToxicityInhalation:
			phraseGroupId = "T28-2";
			break;
		case APIConstants.repeatedDoseToxicityDermal:
			phraseGroupId = "T28-3";
			break;
		case APIConstants.repeatedDoseToxicityOther:
			phraseGroupId = "T28-5";
			break;
		}
	}
}
