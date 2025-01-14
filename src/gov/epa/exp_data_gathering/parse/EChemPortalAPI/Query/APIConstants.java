package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * String constants used to manipulate the eChemPortal API
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class APIConstants {
	// File structure
	public static final String toxValFolder = "Data" + File.separator + "ToxVal";
	public static final String dashboardFolder = "Data" + File.separator + "Dashboard";
	
	// Physicochemical endpoints
	public static final String meltingPoint = "Melting";
	public static final String boilingPoint = "BoilingPoint";
	public static final String flashPoint = "FlashPoint";
	public static final String density = "Density";
	public static final String vaporPressure = "Vapour";
	public static final String partitionCoefficient = "Partition";
	public static final String waterSolubility = "WaterSolubility";
	public static final String dissociationConstant = "DissociationConstant";
	public static final String henrysLawConstant = "HenrysLawConstant";
	public static final String[] physchemEndpoints = {
			meltingPoint,
			boilingPoint,
			flashPoint,
			density,
			vaporPressure,
			partitionCoefficient,
			waterSolubility,
			dissociationConstant,
			henrysLawConstant
	};

	// Toxicity endpoints
	public static final String acuteToxicityOral = "AcuteToxicityOral";
	public static final String acuteToxicityInhalation = "AcuteToxicityInhalation";
	public static final String acuteToxicityDermal = "AcuteToxicityDermal";
	public static final String acuteToxicityOther = "AcuteToxicityOtherRoutes";
	public static final String repeatedDoseToxicityOral = "RepeatedDoseToxicityOral";
	public static final String repeatedDoseToxicityInhalation = "RepeatedDoseToxicityInhalation";
	public static final String repeatedDoseToxicityDermal = "RepeatedDoseToxicityDermal";
	public static final String repeatedDoseToxicityOther = "RepeatedDoseToxicityOther";
	public static final String carcinogenicity = "Carcinogenicity";
	public static final String toxicityToReproductionP0 = "ToxicityReproductionP0";
	public static final String toxicityToReproductionF1 = "ToxicityReproductionF1";
	public static final String developmentalToxicityTeratogenicity = "DevelopmentalToxicityTeratogenicity";
	public static final String toxicityToAquaticAlgae = "ToxicityToAquaticAlgae";
	public static final String toxicityToAquaticPlant = "ToxicityToAquaticPlant";
	public static final String toxicityToOtherAqua = "ToxicityToOtherAqua";
	public static final String toxicityToMicroorganisms = "ToxicityToMicroorganisms";
	public static final String longTermToxicityToFish = "LongTermToxToFish";
	public static final String shortTermToxicityToFish = "ShortTermToxicityToFish";
	public static final String longTermToxicityToAquaInv = "LongTermToxicityToAquaInv";
	public static final String shortTermToxicityToAquaInv = "ShortTermToxicityToAquaInv";
	public static final String toxicityToTerrestrialArthropods = "ToxicityToTerrestrialArthropods";
	public static final String toxicityToBirds = "ToxicityToBirds";
	public static final String toxicityToSoilMacroorganismsExceptArthropods = "ToxicityToSoilMacroorganismsExceptArthropods";
	public static final String toxicityToOtherAboveGroundOrganisms = "ToxicityToOtherAboveGroundOrganisms";
	public static final String toxicityToTerrestrialPlants = "ToxicityToTerrestrialPlants";
	public static final String toxicityToSoilMicroorganisms = "ToxicityToSoilMicroorganisms";
	public static final String skinIrritationCorrosion = "SkinIrritationCorrosion";
	public static final String eyeIrritation = "EyeIrritation";
	public static final String skinSensitisation = "SkinSensitisation";
	public static final String geneticToxicityVivo = "GeneticToxicityVivo";
	public static final String geneticToxicityVitro = "GeneticToxicityVitro";
	public static final String biodegradationInSoilPctDegr = "BiodegradationInSoilPctDegr";
	public static final String biodegradationInSoilHalfLife = "BiodegradationInSoilHalfLife";
	public static final String biodegradationInWater = "BiodegradationInWaterScreeningTests";
	public static final String bioaccumulationAquaticSediment = "BioaccumulationAquaticSediment";
	public static final String[] dashboardToxEndpoints = {
			acuteToxicityOral,
			acuteToxicityInhalation,
			acuteToxicityDermal,
			carcinogenicity,
			toxicityToReproductionP0,
			toxicityToReproductionF1,
			developmentalToxicityTeratogenicity,
			repeatedDoseToxicityOral,
			repeatedDoseToxicityInhalation,
			repeatedDoseToxicityDermal,
			repeatedDoseToxicityOther,
			skinIrritationCorrosion,
			eyeIrritation,
			skinSensitisation,
			geneticToxicityVivo,
			geneticToxicityVitro,
			shortTermToxicityToFish,
			shortTermToxicityToAquaInv,
			longTermToxicityToFish,
			longTermToxicityToAquaInv,
			biodegradationInSoilHalfLife,
			biodegradationInWater,
			bioaccumulationAquaticSediment
	};
	public static final String[] toxValEndpoints = {
			acuteToxicityOral,
			acuteToxicityInhalation,
			acuteToxicityDermal,
			acuteToxicityOther,
			repeatedDoseToxicityOral,
			repeatedDoseToxicityInhalation,
			repeatedDoseToxicityDermal,
			repeatedDoseToxicityOther,
			carcinogenicity,
			toxicityToReproductionP0,
			toxicityToReproductionF1,
			developmentalToxicityTeratogenicity,
			toxicityToAquaticAlgae,
			toxicityToAquaticPlant,
			toxicityToOtherAqua,
			toxicityToMicroorganisms,
			longTermToxicityToFish,
			shortTermToxicityToFish,
			longTermToxicityToAquaInv,
			shortTermToxicityToAquaInv,
			toxicityToTerrestrialArthropods,
			toxicityToBirds,
			toxicityToSoilMacroorganismsExceptArthropods,
			toxicityToOtherAboveGroundOrganisms,
			toxicityToTerrestrialPlants,
			toxicityToSoilMicroorganisms
	};
	public static final String[] ecoToxEndpoints = {
			toxicityToAquaticAlgae,
			toxicityToAquaticPlant,
			toxicityToOtherAqua,
			toxicityToMicroorganisms,
			longTermToxicityToFish,
			shortTermToxicityToFish,
			longTermToxicityToAquaInv,
			shortTermToxicityToAquaInv,
			toxicityToTerrestrialArthropods,
			toxicityToBirds,
			toxicityToSoilMacroorganismsExceptArthropods,
			toxicityToOtherAboveGroundOrganisms,
			toxicityToTerrestrialPlants,
			toxicityToSoilMicroorganisms,
			biodegradationInSoilPctDegr,
			biodegradationInSoilHalfLife,
			biodegradationInWater,
			bioaccumulationAquaticSediment
	};
	public static final List<String> ecoToxEndpointsList = Arrays.asList(ecoToxEndpoints);
	
	// Physicochemical fields
	public static final String endpoint = "Endpoint";
	public static final String pressure = "Pressure";
	public static final String temperature = "Temperature";
	public static final String pH = "pH";
	public static final String glpCompliance = "GLP Compliance";
	public static final String guideline = "Guideline";
	public static final String guidelineQualifier = "Guideline Qualifier";
	
	// Toxicity fields
	public static final String effectLevel = "Effect Level";
	public static final String testType = "Test Type";
	public static final String species = "Species";
	public static final String strain = "Strain";
	public static final String routeOfAdministration = "Route of Administration";
	public static final String inhalationExposureType = "Inhalation Exposure Type";
	public static final String coverageType = "Coverage Type";
	public static final String valueType = "Value Type";
	public static final String endpointType = "Endpoint Type";
	public static final String histoFindingsNeo = "Histopathological Findings: Neoplastic";
	public static final String duration = "Duration";
	public static final String basis = "Basis";
	public static final String interpretationOfResults = "Interpretation of Results";
	public static final String toxicity = "Toxicity";
	public static final String genotoxicity = "Genotoxicity";
	public static final String cytotoxicity = "Cytotoxicity";
	public static final String metabolicActivation = "Metabolic Activation";
	public static final String oxygenConditions = "Oxygen Conditions";
	public static final String waterMediaType = "Water Media Type";
	
	// Units
	public static final String C="C";
	public static final String K="K";
	public static final String F="F";
	public static final String pa="Pa";
	public static final String hpa="hPa";
	public static final String kpa="kPa";
	public static final String atm="atm";
	public static final String bar="Bar";
	public static final String mbar="mBar";
	public static final String mmhg="mmHg";
	public static final String psi="psi";
	public static final String torr="Torr";
	public static final String g_cm3="g/cm^3";
	public static final String kg_m3="kg/m^3";
	public static final String g_L="g/L";
	public static final String ug_L="ug/L";
	public static final String mg_L="mg/L";
	public static final String ppb="ppb";
	public static final String volPct="vol%";
	public static final String dimensionless="dimensionless";
	public static final String dimensionless_vol="dimensionless - volumetric basis";
	public static final String atm_m3_mol="atm m^3/mol";
	public static final String Pa_m3_mol="Pa m^3/mol";
	public static final String mg_kg_bw="mg/kg bw";
	public static final String mL_kg_bw="mL/kg bw";
	public static final String mg_m2="mg/m^2";
	public static final String mg_L_air="mg/L air";
	public static final String mg_L_air_nom="mg/L air (nominal)";
	public static final String mg_L_air_anal="mg/L air (analytical)";
	public static final String mg_m3_air="mg/m^3 air";
	public static final String mg_m3_air_nom="mg/m^3 (nominal)";
	public static final String mg_m3_air_anal="mg/m^3 (analytical)";
	public static final String ppm="ppm";
	public static final String mg_kg_bw_day_nom="mg/kg bw/day (nominal)";
	public static final String mg_kg_bw_day_act="mg/kg bw/day (actual dose received)";
	public static final String mg_kg_diet="mg/kg diet";
	public static final String mg_L_water="mg/L drinking water";
	public static final String mg_kg_bw_tot="mg/kg bw (total dose)";
	public static final String mg_kg_bw_day="mg/kg bw/day";
	public static final String ppm_nom="ppm (nominal)";
	public static final String ppm_anal="ppm (analytical)";
	public static final String mg_cm2_per_day="mg/cm^2 per day";
	public static final String mg_cm2_per_day_nom="mg/cm^2 per day (nominal)";
	public static final String mg_cm2_per_day_anal="mg/cm^2 per day (analytical)";
	public static final String ng_L="ng/L";
	public static final String umol_L="umol/L";
	public static final String mmol_L="mmol/L";
	public static final String mol_L="mol/L";
	public static final String min="min";
	public static final String h="h";
	public static final String d="d";
	public static final String wk="wk";
	
	
	// Accepts all participants 
//	//CCR=101
	//CHEM=140
	//IUCLID=580
	//J-CHECK=60
	//REACH=1
//	Integer[] participantsArray = {101,140,580,60,1};//TMM doesnt work anymore
	
	
	//2025-01-14 from https://www.echemportal.org/echemportal/property-search
	//ECHA REACH=761
	//OECD SIDS IUCLID=1
	//J-CHECK=60
//	//CCR=101
	// Accepts all participants (CCR, J-CHECK, REACH)
	public static final Integer[] participantsArray = {60, 101,761,1};
	
}
