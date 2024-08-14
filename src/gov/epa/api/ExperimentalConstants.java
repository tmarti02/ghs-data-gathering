package gov.epa.api;

public class ExperimentalConstants {

	//Add list of property names here:
	public static final String strWaterSolubility="Water solubility";
	public static final String strDMSOSolubility="DMSO solubility";

	public static final String strVaporPressure="Vapor pressure";
	public static final String strHenrysLawConstant="Henry's law constant";

//	public static final String strLogKow="Octanol water partition coefficient";//TODO add log() to it?
//	public static final String strLogKoa="Octanol air partition coefficient";//TODO add log() to it?

	public static final String strLogKOW="LogKow: Octanol-Water";//TODO add log() to it?
	public static final String strLogKOA="LogKoa: Octanol-Air";//TODO add log() to it?
	
	public static final String strDensity="Density";
	public static final String strMeltingPoint="Melting point";
	public static final String strBoilingPoint="Boiling point";
	public static final String strFlashPoint="Flash point";
	public static final String strAppearance="Appearance";
	public static final String strOdor="Odor";

	public static final String str_pKA="pKA";
	public static final String str_pKAa="Acidic pKa";
	public static final String str_pKAb="Basic pKa";

	
	public static final String strLogOH="LogOH";
	public static final String strOH = "Atmospheric hydroxylation rate";//OPERA

	public static final String strLogBCF="LogBCF";
	public static final String strBCF = "Bioconcentration factor";
	public static final String strFishBCF = "Fish bioconcentration factor";
	public static final String strFishBCFWholeBody = "Whole body fish bioconcentration factor";
	public static final String strStandardFishBCF = "Standard test species fish bioconcentration factor";
//	public static final String strLogBCF_Fish_Whole_Body="LogBCF_Fish_Whole_Body";

	
	public static final String strLogKOC = "LogKOC";
	public static final String strKOC = "Soil Adsorption Coefficient (Koc)";
	
	public static final String strLogKmHL = "LogKmHL";
	public static final String strKmHL = "Fish biotransformation half-life (Km)";//OPERA


	public static final String strLogHalfLifeBiodegradation = "LogHalfLife";
	public static final String strBIODEG_HL_HC = "Biodegradation half-life for hydrocarbons";//OPERA


	
	public static final String strSkinSensitizationLLNA="SkinSensitizationLLNA";
	public static final String strEyeIrritation="EyeIrritation";
	public static final String strEyeCorrosion="EyeCorrosion";
	public static final String strSkinIrritationPII="SkinIrritationPII";
	public static final String strSkinIrritation="SkinIrritation";
	public static final String strSkinCorrosion="SkinCorrosion";
	
	public static final String strCLINT = "Human hepatic intrinsic clearance";//OPERA
	public static final String strFUB = "Fraction unbound in human plasma";//OPERA
	public static final String strTTR_ANSA = "Binding to TTR (replacement of ANSA)";//OPERA
	public static final String strCACO2 = "Caco-2 permeability (Papp)";//OPERA
	public static final String strRBIODEG = "Ready biodegradability";//OPERA

	public static final String strORAL_RAT_LD50="Oral rat LD50";//OPERA
	public static final String strRatOralLD50="rat_oral_LD50";
	public static final String strInhalationLC50="inhalation_LC50";
	
	public static final String strFOUR_HOUR_INHALATION_RAT_LC50="4 hour Inhalation rat LC50";
	

	
	//Add list of well defined property units here:
	public static final String str_mg_L="mg/L";
	public static final String str_mg_m3="mg/m^3";
	public static final String str_g_m3="g/m^3";
	public static final String str_mL_m3="mL/m^3";
	public static final String str_mL_L="mL/L";
	public static final String str_mg_mL="mg/mL";
	public static final String str_g_L="g/L";
	public static final String str_ug_L="ug/L";
	public static final String str_ng_L="ng/L";
	public static final String str_ug_mL="ug/mL";
	public static final String str_g_100mL="g/100mL";
	public static final String str_mg_100mL="mg/100mL";
	public static final String str_ng_ml="ng/mL";
	public static final String str_g_cm3="g/cm3";
	public static final String str_kg_m3="kg/m3";
	public static final String str_g_mL="g/mL";
	public static final String str_kg_dm3="kg/dm3";
	public static final String str_C="C";
	public static final String str_F="F";
	public static final String str_K="K";
	public static final String str_pctWt="%w";
	public static final String str_pctVol="%v";
	public static final String str_pct="%";
	public static final String str_ppm="ppm";
	public static final String str_ppb="ppb";
	public static final String str_ppt = "ppt";
	public static final String str_atm_m3_mol="atm-m3/mol";
	public static final String str_mol_m3_atm = "mol/m3-Pa";
	public static final String str_Pa_m3_mol="Pa-m3/mol";
	public static final String str_mmHg="mmHg";
	public static final String str_atm="atm";
	public static final String str_kpa="kPa";
	public static final String str_hpa="hPa";
	public static final String str_mpa="mPa";
	public static final String str_pa="Pa";
	public static final String str_mbar="mbar";
	public static final String str_bar="bar";
	public static final String str_torr="Torr";
	public static final String str_psi="psi";
	public static final String str_M="M";
	public static final String str_mM="mM";
	public static final String str_nM="nM";
	public static final String str_uM="uM";
	public static final String str_log_M="log10(M)";
	public static final String str_neg_log_M = "-log10(M)";
	public static final String str_log_mg_L="log10(mg/L)";
	public static final String str_log_ppm="log10(ppm)";
	public static final String str_log_mmHg="log10(mmHg)";
	public static final String str_log_atm_m3_mol="log10(atm-m3/mol)";
	public static final String str_dimensionless_H="Dimensionless H";
	public static final String str_dimensionless_H_vol="Dimensionless H (volumetric)";
	public static final String str_dimensionless="Dimensionless";
	public static final String str_binary = "Binary";

	public static final String str_LOG_UNITS = "Log units";
	
	public static final String str_DAYS = "days";
	public static final String str_LOG_DAYS = "log10(days)";
	
	public static final String str_LOG_CM_SEC="log10(cm/sec)";
	public static final String str_CM_SEC="cm/sec";

	
	public static final String str_COUNT = "Count";
	public static final String str_POUNDS = "lbs";

	public static final String str_LOG_CM3_MOLECULE_SEC="log10(cm3/molecule-sec)";
	public static final String str_CM3_MOLECULE_SEC="cm3/molecule-sec";
	public static final String str_LOG_L_KG = "log10(L/kg)";
	public static final String str_LOG_UL_MIN_1MM_CELLS="log10(ul/min/10^6 cells)";//for clint
	public static final String str_UL_MIN_1MM_CELLS="ul/min/10^6 cells";//for clint


	public static final String str_mg_kg="mg/kg";
	public static final String str_g_kg="g/kg";
	public static final String str_mL_kg="mL/kg";
	public static final String str_iu_kg="iu/kg";
	public static final String str_L_KG = "L/kg";
	public static final String str_L_g = "L/g";
	public static final String str_L_mg = "L/mg";
//	public static final String str_mL_mg = "mL/mg";
//	public static final String str_ml_g="ml/g";

	public static final String str_units_kg="units/kg";
	public static final String str_mg="mg";
	public static final String str_mg_kg_H20="mg/kg H2O";
	public static final String str_g_Mg_H20="g/Mg H2O";
	public static final String str_g_100g="g/100g";
	public static final String str_mg_100g="mg/100g";
	public static final String str_mol_m3_H20="mol/m3 H2O";
	public static final String str_mol_kg_H20="mol/kg H2O";
	public static final String str_kg_kg_H20="kg/kg H2O";
	public static final String str_g_kg_H20="g/kg H2O";
	public static final String str_ug_g_H20 ="ug/g H2O";
	public static final String str_ug_100mL = "ug/100mL";
	public static final String str_mg_10mL = "mg/10mL";
	public static final String str_g_10mL = "g/10mL";
	public static final String str_oz_gal = "oz/gal";
	public static final String str_pii="PII";
	
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
	public static final String strSourceEChemPortal="eChemPortal";
	public static final String strSourceEChemPortalAPI="eChemPortalAPI";
	public static final String strSourceOChem="OChem";
	public static final String strSourceOChem_2024_04_03="OChem_2024_04_03";
	
	public static final String strSourceOFMPub="OFMPub";
	public static final String strSourceSigmaAldrich="Sigma-Aldrich";
	public static final String strSourceChemicalBook="ChemicalBook";
	public static final String strSourceSander="Sander";
//	public static final String strSourceSander="Sander v4.0";
	public static final String strSourceQSARDB="QSARDB";
	public static final String strSourceBradley="Bradley";
	public static final String strSourceADDoPT="ADDoPT";
	public static final String strSourceAqSolDB="AqSolDB";
	public static final String strSourceChemBL="ChemBL";
	public static final String strSourceChemidplus="ChemIDplus";
	public static final String strSourceOPERA="OPERA";
	public static final String strSourceOPERA29="OPERA2.9";
	public static final String strSourceEpisuiteOriginal="EpisuiteOriginal";
	public static final String strSourceEpisuiteISIS="EpisuiteISIS";
	public static final String strSourceICF="ICF";
	public static final String strSource3M="ThreeM";
	
	public static final String strSourceOECD_Toolbox="OECD Toolbox";
	public static final String strSourceOECD_Toolbox_SkinIrrit = "OECD Toolbox Skin Irritation";
	public static final String strSourceNICEATM="NICEATM";
	public static final String strSourceCFSAN="CFSAN";
	public static final String strSourceLebrun="Lebrun";
	public static final String strSourceDRD="DRD";
	public static final String strSourceTakahashi="Takahashi";
	public static final String strSourceBurkhard="Burkhard";
	public static final String strSourceHayashi="Hayashi";
	public static final String strSourceBagley="Bagley";
	public static final String strSourceKodithala="Kodithala";
	public static final String strSourceVerheyen="Verheyen";
	
	public static final String strSourceCERAPP_Exp="CERAPP_Exp";

	public static final String strInVitroToxicity = "in vitro toxicity";
	public static final String strNINETY_SIX_HOUR_FATHEAD_MINNOW_LC50 ="96 hour fathead minnow LC50";
	public static final String strNINETY_SIX_HOUR_BLUEGILL_LC50 = "96 hour bluegill LC50";
	public static final String strNINETY_SIX_HOUR_RAINBOW_TROUT_LC50= "96 hour rainbow trout LC50";
//	public static final String strFORTY_EIGHT_HOUR_WATER_FLEA_LC50= "48 hour water flea LC50";
	public static final String strFORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50 ="48 hour Daphnia magna LC50";

	public static final String strSourceSampleSource="SampleSource";

	public static final String strAR = "Androgen receptor activity";
	public static final String strER = "Estrogen receptor activity";
	
	public static final String str_ANDROGEN_RECEPTOR_AGONIST = "Androgen receptor agonist";//OPERA
	public static final String str_ANDROGEN_RECEPTOR_ANTAGONIST = "Androgen receptor antagonist";//OPERA
	public static final String str_ANDROGEN_RECEPTOR_BINDING = "Androgen receptor binding";//OPERA

	public static final String str_ESTROGEN_RECEPTOR_AGONIST = "Estrogen receptor agonist";//OPERA
	public static final String str_ESTROGEN_RECEPTOR_ANTAGONIST = "Estrogen receptor antagonist";//OPERA
	public static final String str_ESTROGEN_RECEPTOR_BINDING = "Estrogen receptor binding";//OPERA

	
	public static final String strTEXT="Text";

//	public static final String strSourceEcotox="ECOTOX";
	public static final String strSourceEcotox_2023_12_14="ECOTOX_2023_12_14";
	public static final String sourceNITE_OPPT = "NITE_OPPT";

	public static final String strAutoIgnitionTemperature="Autoignition temperature";
	public static final String strRefractiveIndex="Refractive index";
	public static final String strVaporDensity="Vapor density";
	public static final String strViscosity="Viscosity";
	public static final String strSurfaceTension = "Surface tension";
	

}