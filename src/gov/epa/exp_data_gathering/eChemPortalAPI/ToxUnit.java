package gov.epa.exp_data_gathering.eChemPortalAPI;

public class ToxUnit extends Unit {
	
	/**
	 * Translates our unit identifiers to eChemPortal's Unit objects for toxicity properties
	 * @param unit	Desired unit (string from dropdown menu on eChemPortal)
	 */
	public ToxUnit(String unit,String endpointKind) {
		switch (unit) {
		case "mg/kg bw":
			phraseGroupId = "T04";
			phraseId = "2081";
			break;
		case "mL/kg bw":
			phraseGroupId = "T04";
			phraseId = "2119";
			break;
		case "mg/m^2":
			phraseGroupId = "T04";
			phraseId = "2103";
			break;
		case "mg/L air":
			phraseGroupId = "T07";
			phraseId = "2099";
			break;
		case "mg/L air (nominal)":
			phraseGroupId = "T07";
			phraseId = "2101";
			break;
		case "mg/L air (analytical)":
			phraseGroupId = "T07";
			phraseId = "2100";
			break;
		case "mg/m^3 air":
			phraseGroupId = "T07";
			phraseId = "2104";
			break;
		case "mg/m^3 air (nominal)":
			phraseGroupId = "T07";
			phraseId = "2106";
			break;
		case "mg/m^3 air (analytical)":
			phraseGroupId = "T07";
			phraseId = "2105";
			break;
		case "ppm":
			phraseGroupId = "T07";
			phraseId = "2283";
			break;
		case "mg/L":
			phraseGroupId = "T12";
			phraseId = "2098";
			break;
		case "mg/kg bw/day (nominal)":
			phraseGroupId = "T28-1";
			phraseId = "2087";
			break;
		case "mg/kg bw/day (actual dose received)":
			phraseGroupId = "T28-1";
			phraseId = "2086";
			break;
		case "mg/kg diet":
			phraseGroupId = "T28-1";
			phraseId = "2090";
			break;
		case "mg/L drinking water":
			phraseGroupId = "T28-1";
			phraseId = "2102";
			break;
		case "mg/kg bw (total dose)":
			phraseGroupId = "T28-1";
			phraseId = "2082";
			break;
		case "mg/kg bw/day":
			phraseGroupId = "T28-3";
			phraseId = "2085";
			break;
		case "mg/cm^2 per day":
			phraseGroupId = "T28-3";
			phraseId = "2078";
			break;
		case "mg/cm^2 per day (nominal)":
			phraseGroupId = "T28-3";
			phraseId = "2080";
			break;
		case "mg/cm^2 per day (analytical)":
			phraseGroupId = "T28-3";
			phraseId = "2079";
			break;
		case "ppm (nominal)":
			phraseGroupId = "T28-5";
			phraseId = "2285";
			break;
		case "ppm (analytical)":
			phraseGroupId = "T28-5";
			phraseId = "2284";
			break;
		}
		
		switch (endpointKind) {
		case EChemPortalAPIConstants.acuteToxicityOther:
			phraseGroupId = "T12";
			break;
		case EChemPortalAPIConstants.repeatedDoseToxicityOral:
			phraseGroupId = "T28-1";
			break;
		case EChemPortalAPIConstants.repeatedDoseToxicityInhalation:
			phraseGroupId = "T28-2";
			break;
		case EChemPortalAPIConstants.repeatedDoseToxicityDermal:
			phraseGroupId = "T28-3";
			break;
		case EChemPortalAPIConstants.repeatedDoseToxicityOther:
			phraseGroupId = "T28-5";
			break;
		}
	}
	
	public ToxUnit(String endpointKind) {
		switch (endpointKind) {
		case EChemPortalAPIConstants.acuteToxicityOral:
			phraseGroupId = "T04";
			break;
		case EChemPortalAPIConstants.acuteToxicityInhalation:
			phraseGroupId = "T07";
			break;
		case EChemPortalAPIConstants.acuteToxicityDermal:
			phraseGroupId = "T04";
			break;
		case EChemPortalAPIConstants.acuteToxicityOther:
			phraseGroupId = "T12";
			break;
		case EChemPortalAPIConstants.repeatedDoseToxicityOral:
			phraseGroupId = "T28-1";
			break;
		case EChemPortalAPIConstants.repeatedDoseToxicityInhalation:
			phraseGroupId = "T28-2";
			break;
		case EChemPortalAPIConstants.repeatedDoseToxicityDermal:
			phraseGroupId = "T28-3";
			break;
		case EChemPortalAPIConstants.repeatedDoseToxicityOther:
			phraseGroupId = "T28-5";
			break;
		}
	}
}
