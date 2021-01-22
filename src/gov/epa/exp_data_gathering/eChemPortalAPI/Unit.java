package gov.epa.exp_data_gathering.eChemPortalAPI;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import gov.epa.api.ExperimentalConstants;

/**
 * Replicates the Unit object of an eChemPortal API search query JSON
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class Unit {
	@SerializedName("phraseGroupId")
	@Expose
	public String phraseGroupId;
	@SerializedName("phraseId")
	@Expose
	public String phraseId;
	
	// Default constructor
	public Unit() {
		phraseGroupId = null;
		phraseId = null;
	}
	
	/**
	 * Translates our unit identifiers to eChemPortal's Unit objects
	 * @param unit	Desired unit from ExperimentalConstants
	 */
	public Unit(String unit) {
		switch (unit) {
		case "":
			// For partition coefficient, pH, pKa
			phraseId = null;
			break;
		case ExperimentalConstants.str_C:
			phraseGroupId = "A102";
			phraseId = "2493";
			break;
		case ExperimentalConstants.str_F:
			phraseGroupId = "A102";
			phraseId = "3888";
			break;
		case ExperimentalConstants.str_K:
			phraseGroupId = "A102";
			phraseId = "3887";
			break;
		case ExperimentalConstants.str_pa:
			phraseGroupId = "P02";
			phraseId = "1349";
			break;
		case ExperimentalConstants.str_hpa:
			phraseGroupId = "P02";
			phraseId = "1954";
			break;
		case ExperimentalConstants.str_kpa:
			phraseGroupId = "P02";
			phraseId = "2019";
			break;
		case ExperimentalConstants.str_atm+"_VP":
			phraseGroupId = "P02";
			phraseId = "1740";
			break;
		case ExperimentalConstants.str_bar:
			phraseGroupId = "P02";
			phraseId = "139";
			break;
		case ExperimentalConstants.str_mbar:
			phraseGroupId = "P02";
			phraseId = "2046";
			break;
		case ExperimentalConstants.str_mmHg:
			phraseGroupId = "P02";
			phraseId = "2121";
			break;
		case ExperimentalConstants.str_psi:
			phraseGroupId = "P02";
			phraseId = "1348";
			break;
		case ExperimentalConstants.str_torr:
			phraseGroupId = "P02";
			phraseId = "1616";
			break;
		case ExperimentalConstants.str_g_cm3+"_density":
			phraseGroupId = "P18";
			phraseId = "1929";
			break;
		case ExperimentalConstants.str_kg_m3+"_density":
			phraseGroupId = "P18";
			phraseId = "2022";
			break;
		case ExperimentalConstants.str_g_L+"_density":
			phraseGroupId = "P18";
			phraseId = "1935";
			break;
		case ExperimentalConstants.str_ug_L:
			phraseGroupId = "P08";
			phraseId = "2500";
			break;
		case ExperimentalConstants.str_mg_L:
			phraseGroupId = "P08";
			phraseId = "2098";
			break;
		case ExperimentalConstants.str_g_L+"_solubility":
			phraseGroupId = "P08";
			phraseId = "1935";
			break;
		case ExperimentalConstants.str_g_cm3+"_solubility":
			phraseGroupId = "P08";
			phraseId = "1929";
			break;
		case ExperimentalConstants.str_kg_m3+"_solubility":
			phraseGroupId = "P08";
			phraseId = "2022";
			break;
		case ExperimentalConstants.str_ppb:
			phraseGroupId = "P08";
			phraseId = "2282";
			break;
		case ExperimentalConstants.str_pctVol:
			phraseGroupId = "P08";
			phraseId = "2453";
			break;
		case ExperimentalConstants.str_atm_m3_mol:
			phraseGroupId = "P101";
			phraseId = "1741";
			break;
		case ExperimentalConstants.str_Pa_m3_mol:
			phraseGroupId = "P101";
			phraseId = "1350";
			break;
		case ExperimentalConstants.str_dimensionless_H:
			phraseGroupId = "P101";
			phraseId = "1852";
			break;
		case ExperimentalConstants.str_dimensionless_H_vol:
			phraseGroupId = "P101";
			phraseId = "1853";
			break;
		case ExperimentalConstants.str_atm+"_H":
			phraseGroupId = "P101";
			phraseId = "1740";
			break;
		}
	}
}