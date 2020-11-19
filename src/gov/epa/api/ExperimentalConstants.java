package gov.epa.api;

public class ExperimentalConstants {

	//Add list of property names here:
	public static final String strWaterSolubility="Water solubility";
	public static final String str_pKA="pKA";
	public static final String strVaporPressure="Vapor pressure";
	public static final String strHenrysLawConstant="Henry's law constant";
	public static final String strLogKow="Octanol water partition coefficient";
	public static final String strDensity="Density";
	public static final String strMeltingPoint="Melting point";
	public static final String strBoilingPoint="Boiling point";
	public static final String strFlashPoint="Flash point";
	public static final String strAppearance="Appearance";

	//Add list of well defined property units here:
	public static final String str_mg_L="mg/L";
	public static final String str_mg_mL="mg/mL";
	public static final String str_g_L="g/L";
	public static final String str_ug_L="ug/L";
	public static final String str_ug_mL="ug/mL";
	public static final String str_g_cm3="g/cm3";
	public static final String str_g_mL="g/mL";
	public static final String str_C="C";
	public static final String str_F="F";
	public static final String str_pctWt="%w";
	public static final String str_pct="%";
	public static final String str_ppm="ppm";
	public static final String str_m3_atm_mol="m3-atm/mol";
	public static final String str_mmHg="mmHg";
	public static final String str_atm="atm";
	public static final String str_kpa="kPa";
	public static final String str_M="M";
	
	//Other:
	public static final String str_dec="decomposes";
	public static final String str_lit="literature";
	public static final String str_subl="sublimates";
	public static final String str_relative_density="relative density (water = 1)";
	public static final String str_relative_mixture_density="relative density of the vapor-air mixture (air = 1)";
	public static final String str_relative_gas_density="relative gas density (air = 1)";
	public static final String str_est="estimated";
	public static final String str_ext="extrapolated";
	public static final String str_negl="negligible";
	
	//Add list of source names here:
	public static final String strSourceLookChem="LookChem";
	public static final String strSourcePubChem="PubChem";
	public static final String strSourceEChem="eChemPortal";
	
	//Conversion factors:
	public static final double mmHg_to_kPa=0.133322;
	public static final double atm_to_kPa=101.325;
	
}