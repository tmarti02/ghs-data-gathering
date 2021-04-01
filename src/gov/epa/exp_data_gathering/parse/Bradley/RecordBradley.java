package gov.epa.exp_data_gathering.parse.Bradley;

import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

/**
 * Stores data from Bradley, accessible at: https://www.nature.com/articles/npre.2010.4243.3
 * @author GSINCL01
 *
 */
public class RecordBradley {
	public String Experiment_Number_900_series_refer_to_external_references;
	public String sample_or_citation;
	public String ref;
	public String solute;
	public String DONOTUSE;
	public String solute_SMILES;
	public String solvent;
	public String solvent_SMILES;
	public String concentration_M;
	public String wiki_page;
	public String gONS;
	public String notes;
	public String identifier;
	public String solute_type;
	public String solubility_solute_mass_g;
	public String solvent_mass_g;
	public String solvent_density_g_ml;
	public String solute_density_g_ml_from_ChemSpider_prediction;
	public String solvent_volume_ml;
	public String solute_volume_ml;
	public String total_vol_ml;
	public String solute_MW;
	public String moles_solute;
	public String calculated_concentration_M_assumes_no_expansion_or_contraction_upon_mixing;
	public String calc_conc_M_from_g_100ml;
	public String liquid_at_room_temp_y_n;
	public String solute_reacts_with_solvent;
	public String csid;
	public String TRUE;
	public String solubility_g_l;
	public String calculated_conc_in_moles_liter;
	public String solubility_mole_fraction;
	public String solvent_MW_g_mol;
	public static final String[] fieldNames = {"Experiment_Number_900_series_refer_to_external_references","sample_or_citation","ref","solute","DONOTUSE","solute_SMILES","solvent","solvent_SMILES","concentration_M","wiki_page","gONS","notes","identifier","solute_type","solubility_solute_mass_g","solvent_mass_g","solvent_density_g_ml","solute_density_g_ml_from_ChemSpider_prediction","solvent_volume_ml","solute_volume_ml","total_vol_ml","solute_MW","moles_solute","calculated_concentration_M_assumes_no_expansion_or_contraction_upon_mixing","calc_conc_M_from_g_100ml","liquid_at_room_temp_y_n","solute_reacts_with_solvent","csid","TRUE","solubility_g_l","calculated_conc_in_moles_liter","solubility_mole_fraction","solvent_MW_g_mol"};

	public static final String lastUpdated = "12/04/2020";
	public static final String sourceName = ExperimentalConstants.strSourceBradley;

	private static final String fileName = "Water Solubility Subset_v2.xlsx";

	public static Vector<JsonObject> parseBradleyRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(3);
		return records;
	}
}