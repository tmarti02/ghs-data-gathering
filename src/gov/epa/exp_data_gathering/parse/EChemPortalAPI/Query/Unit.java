package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Replicates the Unit object of an eChemPortal API search query JSON
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class Unit {
	@SerializedName("phraseGroupId")
	@Expose
	protected String phraseGroupId;
	@SerializedName("phraseId")
	@Expose
	protected String phraseId;
	
	// Default constructor
	Unit() {
		phraseGroupId = null;
		phraseId = null;
	}
	
	/**
	 * Translates our unit identifiers to eChemPortal's Unit objects
	 * @param unit	Desired unit from APIConstants
	 */
	Unit(String unit,String endpointKind) {
		switch (unit) {
		case "":
			// For partition coefficient, pH, pKa
			phraseId = null;
			break;
		case APIConstants.C:
			phraseGroupId = "A102";
			phraseId = "2493";
			break;
		case APIConstants.F:
			phraseGroupId = "A102";
			phraseId = "3888";
			break;
		case APIConstants.K:
			phraseGroupId = "A102";
			phraseId = "3887";
			break;
		case APIConstants.pa:
			phraseGroupId = "P02";
			phraseId = "1349";
			break;
		case APIConstants.hpa:
			phraseGroupId = "P02";
			phraseId = "1954";
			break;
		case APIConstants.kpa:
			phraseGroupId = "P02";
			phraseId = "2019";
			break;
		case APIConstants.atm:
			phraseGroupId = "P02";
			phraseId = "1740";
			break;
		case APIConstants.bar:
			phraseGroupId = "P02";
			phraseId = "139";
			break;
		case APIConstants.mbar:
			phraseGroupId = "P02";
			phraseId = "2046";
			break;
		case APIConstants.mmhg:
			phraseGroupId = "P02";
			phraseId = "2121";
			break;
		case APIConstants.psi:
			phraseGroupId = "P02";
			phraseId = "1348";
			break;
		case APIConstants.torr:
			phraseGroupId = "P02";
			phraseId = "1616";
			break;
		case APIConstants.g_cm3:
			phraseGroupId = "P18";
			phraseId = "1929";
			break;
		case APIConstants.kg_m3:
			phraseGroupId = "P18";
			phraseId = "2022";
			break;
		case APIConstants.g_L:
			phraseGroupId = "P18";
			phraseId = "1935";
			break;
		case APIConstants.ug_L:
			phraseGroupId = "P08";
			phraseId = "2500";
			break;
		case APIConstants.mg_L:
			phraseGroupId = "P08";
			phraseId = "2098";
			break;
		case APIConstants.ppb:
			phraseGroupId = "P08";
			phraseId = "2282";
			break;
		case APIConstants.volPct:
			phraseGroupId = "P08";
			phraseId = "2453";
			break;
		case APIConstants.atm_m3_mol:
			phraseGroupId = "P101";
			phraseId = "1741";
			break;
		case APIConstants.Pa_m3_mol:
			phraseGroupId = "P101";
			phraseId = "1350";
			break;
		case APIConstants.dimensionless:
			phraseGroupId = "P101";
			phraseId = "1852";
			break;
		case APIConstants.dimensionless_vol:
			phraseGroupId = "P101";
			phraseId = "1853";
			break;
		}
		
		switch (endpointKind) {
		case APIConstants.henrysLawConstant:
			phraseGroupId = "P101";
			break;
		case APIConstants.waterSolubility:
			phraseGroupId = "P08";
			break;
		}
	}
}