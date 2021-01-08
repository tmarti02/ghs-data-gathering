package gov.epa.exp_data_gathering.eChemPortalAPI;

public class ToxUnit extends Unit {
	
	/**
	 * Translates our unit identifiers to eChemPortal's Unit objects for toxicity properties
	 * @param unit	Desired unit (string from dropdown menu on eChemPortal)
	 */
	public ToxUnit(String unit) {
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
		case "mg/kg bw_other":
			phraseGroupId = "T12";
			phraseId = "2081";
			break;
		case "mg/L":
			phraseGroupId = "T12";
			phraseId = "2098";
			break;
		case "mL/kg bw_other":
			phraseGroupId = "T12";
			phraseId = "2119";
			break;
		case "ppm_other":
			phraseGroupId = "T12";
			phraseId = "2283";
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
		case "ppm_oral":
			phraseGroupId = "T28-1";
			phraseId = "2283";
			break;
		}
	}
}
