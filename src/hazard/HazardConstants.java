package hazard;

public class HazardConstants {

	public static final String strAcute_Mammalian_Toxicity = "Acute Mammalian Toxicity";
	public static final String strAcute_Mammalian_ToxicityOral = "Acute Mammalian Toxicity Oral";
	public static final String strAcute_Mammalian_ToxicityInhalation = "Acute Mammalian Toxicity Inhalation";
	public static final String strAcute_Mammalian_ToxicityDermal = "Acute Mammalian Toxicity Dermal";
	public static final String strCarcinogenicity = "Carcinogenicity";
	public static final String strGenotoxicity_Mutagenicity = "Genotoxicity Mutagenicity";
	public static final String strEndocrine_Disruption = "Endocrine Disruption";
	public static final String strReproductive = "Reproductive";
	public static final String strDevelopmental = "Developmental";
	public static final String strNeurotoxicity = "Neurotoxicity";
	public static final String strNeurotoxicity_Repeat_Exposure = "Neurotoxicity Repeat Exposure";
	public static final String strNeurotoxicity_Single_Exposure = "Neurotoxicity Single Exposure";
	public static final String strSystemic_Toxicity = "Systemic Toxicity";

	public static final String strSystemic_Toxicity_Repeat_Exposure = "Systemic Toxicity Repeat Exposure";
	public static final String strSystemic_Toxicity_Single_Exposure = "Systemic Toxicity Single Exposure";
	public static final String strSkin_Sensitization = "Skin Sensitization";
	public static final String strSkin_Irritation = "Skin Irritation";
	public static final String strEye_Irritation = "Eye Irritation";
	public static final String strAcute_Aquatic_Toxicity = "Acute Aquatic Toxicity";
	public static final String strChronic_Aquatic_Toxicity = "Chronic Aquatic Toxicity";
	public static final String strPersistence = "Persistence";
	public static final String strBioaccumulation = "Bioaccumulation";

	public static final String strExposure = "Exposure";

	public static final String strWaterSolubility = "Water Solubility";

	public static final String emergencyResponseProfileId = "emergencyResponse";
	public static final String emergencyResponseProfileTitle = "Emergency Response";
	public static final String siteSpecificProfileId = "siteSpecific";
	public static final String siteSpecificProfileTitle = "Site-Specific Screening";

	public static final String[] hazardNames = {
			strAcute_Mammalian_ToxicityOral,
			strAcute_Mammalian_ToxicityInhalation,
			strAcute_Mammalian_ToxicityDermal,
			strCarcinogenicity,
			strGenotoxicity_Mutagenicity,
			strEndocrine_Disruption,
			strReproductive,
			strDevelopmental,
			strNeurotoxicity_Repeat_Exposure,
			strNeurotoxicity_Single_Exposure,
			strSystemic_Toxicity_Repeat_Exposure,
			strSystemic_Toxicity_Single_Exposure,
			strSkin_Sensitization,
			strEye_Irritation,
			strSkin_Irritation,
			strAcute_Aquatic_Toxicity,
			strChronic_Aquatic_Toxicity,
			strPersistence,
			strBioaccumulation
	};

	public static final String[] humanHealthEffectsEndpoints = {
			strAcute_Mammalian_Toxicity,
			strCarcinogenicity,
			strGenotoxicity_Mutagenicity,
			strEndocrine_Disruption,
			strReproductive, strDevelopmental,
			strNeurotoxicity,
			strSystemic_Toxicity,
			strSkin_Sensitization,
			strSkin_Irritation,
			strEye_Irritation
	};

	public static final String[] ecotoxEndpoints = {
			strAcute_Aquatic_Toxicity,
			strChronic_Aquatic_Toxicity
	};

	public static final String[] fateEndpoints = {
			strPersistence,
			strBioaccumulation
	};

	public static final String scoreVH = "VH";
	public static final String scoreH = "H";
	public static final String scoreM = "M";
	public static final String scoreL = "L";
	public static final String scoreVL = "VL";
	public static final String scoreND = "ND";
	public static final String scoreI = "I";

	public static final String sourceBoyes = "Nervous System";
	public static final String sourceCanada = "Canada";
	public static final String sourceJapan = "Japan";
	public static final String sourceTEST_Predicted = "T.E.S.T. (predicted value)";
	public static final String sourceTEST_Experimental = "T.E.S.T. (experimental value)";
	//	public String listType;//right now dont need this because getListType gets it from the source name
	public static final String sourceChemIDplus = "ChemIDplus";
	public static final String sourceNewZealand = "New Zealand";
	public static final String sourceKorea = "Korea";
	public static final String sourceMalaysia = "Malaysia";
	public static final String sourceECHA_CLP = "ECHA CLP";
	public static final String sourceTEDX = "TEDX";
	public static final String sourceSIN = "SIN";
	public static final String sourceMAK = "List of MAK and BAT Values (Germany)";
	public static final String sourceDenmark = "Denmark";
	public static final String sourceIRIS = "IRIS";
	public static final String sourceROC = "Report on Carcinogens (ROC)";
	public static final String sourceIARC = "IARC";
	public static final String sourceProp65 = "Prop 65";
	public static final String sourceDSL = "DSL";
	public static final String sourceUMD = "UMD";
	public static final String sourceTSCA_Work_Plan = "TSCA work plan";
	public static final String sourceReachVeryHighConcernList = "REACH Very High Concern List";
	public static final String sourceAustralia = "Australia";
	public static final String sourceNIOSH_Potential_Occupational_Carcinogens = "NIOSH list of potential occupational carcinogens";
	public static final String sourceEPAMidAtlanticHumanHealth = "EPA mid-Atlantic Region Human Health Risk-Based Concentrations";
	public static final String sourceHealth_Canada_Priority_Substance_Lists_Carcinogenicity = "Health Canada Priority Substance Lists (Carcinogenicity)";
	public static final String sourceHealth_Canada_Priority_Substance_Lists_Reproductive = "Health Canada Priority Substance Lists (Reproductive Toxicity)";
	public static final String sourceReproductiveCanada = "Reproductive Canada";
	public static final String sourceEDSPDB_Reproductive_Toxicity_Ranking = "EDSPDB Reproductive Toxicity Ranking";
	public static final String sourceTeratogenicity_Data_in_Vitro_from_EPA_HPVIS = "Teratogenicity Data in Vitro from EPA HPVIS";
	public static final String sourceCarcinogenicityNIOSHList = "Carcinogenicity NIOSH List";
	public static final String sourceGermany = "Germany";
	public static final String sourceGermany_EMAK = "Germany\\MAK2017";
	public static final String sourceEU_Detergent_Ingredient_Database_2014 = "EU Detergent Ingredient Database 2014";
	public static final String sourceOSPAR = "OSPAR";
	public static final String sourceReach_JSON_Files = "Reach JSON Files";
	public static final String sourceReportOnCarcinogens = "Report On Carcinogens";
	public static final String sourceGenotoxicity_Data_In_Vitro_EPA_HPVIS = "Genotoxicity Data In Vitro from EPA HPVIS";
	public static final String sourceAcute_Toxicity_Data_from_EPA_HPVIS = "Acute Toxicity Data from EPA HPVIS";
	public static final String sourceReproductive_Toxicity_Data_In_Vitro_from_EPA_HPVIS = "Reproductive Toxicity Data in Vitro from EPA HPVIS";

	public static final String sourceToxVal="ToxVal v94";
	public static final String sourceToxVal_NTP_ROC = "NTP ROC";//NTP Report on Carcinogens from ToxVal db
	
	public static final String typeAuthoritative = "Authoritative"; // Authoritative List
	public static final String typeScreening = "Screening"; // Screening List
	public static final String typePredicted = "QSAR Model"; // Predicted value from QSAR model
	public static final String typeExperimental = "Experimental"; // Predicted value from QSAR model

	public static final String listTypeECHA_CLP = typeAuthoritative; // typeAuthoritative? // Is this the same as
	// EU-GHS?
	// page 105 GreenScreen v 1.4?
	// or Annex VI CMRs Annex VI to CLP GreenScreen
	// page 104.
	public static final String listTypeIRIS = typeAuthoritative; // page 103 GreenScreen v. 1.4
	public static final String listTypeJapan = typeScreening; // page 112 GreenScreen v. 1.4
	public static final String listTypeAustralia = typeScreening;// see pg 112 GreenScreen v 1.4
	public static final String listTypeKorea = typeScreening; // page 112 GreenScreen v 1.4
	public static final String listTypeMalaysia = typeScreening; // page 112 GreenScreen v 1.4
	public static final String listTypeGermany = typeAuthoritative; // page 107 GreenScreen v 1.4
	public static final String listTypeTEDX = typeScreening; // page 110 GreenScreen v 1.4
	public static final String listTypeSIN = typeScreening; // page 109 GreenScreen v 1.4
	public static final String listTypeNewZealand = typeScreening; // page 113 GreenScreen v 1.4
	public static final String listTypeTSCA_Work_Plan = typeScreening; // don't see it on the GreenScreen list.
	public static final String listTypeReachVeryHighConcernList = typeAuthoritative; // page 105 and 106 GreenScreen v
	// 1.4
	// Do we have all of the REACH lists:
	// Candidate, Prioritization, and Subject to Authorization?
	public static final String listTypeEPAMidAtlanticHumanHealth = typeAuthoritative; // don't see it on the GreenScreen
	public static final String listTypeROC = typeAuthoritative; // page 108 GreenScreen v 1.4
	public static final String listTypeIARC = typeAuthoritative; // page 106 GreenScreen v 1.4
	/*
	 * public static final String typeAuthoritative="Authoritative"; public static
	 * String typeScreening="Screening";
	 */
	public static final String listTypeProp65 = typeAuthoritative; // page 109 GreenScreen v 1.4
	public static final String listTypeDSL = typeScreening; // page 103 GreenScreen v1.4
	// if this is the same as EC-CEPA DSL.
	public static final String listTypeUMD = typeScreening; // Screening?
	/*
	 * From page 7 of Pharos CML description: The Pharos CML uses the following
	 * trumping scheme: "First, lists are sorted by level of authority: 1.
	 * Authoritative lists (designated by GreenScreen) 2. Priority lists proposed
	 * for GreenScreen 3. Screening lists (designated by GreenScreen) 4. Lists not
	 * included in or proposed for GreenScreen If there are multiple lists of the
	 * highest authority level, they are then sorted by hazard level, and the list
	 * with the highest hazard is selected for display."
	 *
	 * So maybe we should distinguish between lists designated by GreenScreen and
	 * lists not included in GreenScreen. Maybe designate lists as authoritative or
	 * screening and then have a separate variable that indicates whether the list
	 * is designated by GreenScreen. That way we could run a GreenScreen List
	 * Translator analysis to compare results with official List Translators (Pharos
	 * and Toxnot).
	 *
	 * So the boolean variable isInGreenScreen() = true when the list is in
	 * GreenScreen, otherwise false.
	 *
	 * Should we also indicate whether the list is included in Pharos, Toxnot, or
	 * whether the lists are required by product certification or disclosure
	 * programs such as Health Product Declaration (HPD) referenced in Pharos?
	 *
	 */
//
	// don't see it on the GreenScreen list. University of
	// Maryland??
	public static final String listTypeNIOSH_Carcinogen = typeAuthoritative; // page 107 GreenScreen v 1.4
	// Added by TMM, 3/26/18:
	public static final String listTypeCanada = typeScreening;
	public static final String listTypeReproductiveCanada = typeScreening;
	public static final String listTypeHealthCanadaPrioritySubstanceListsCarcinogenicity = typeScreening; // screening
	// or
	// authoritative?
	// don't see it on the GreenScreen list
	// or is it one of the CEPA lists on GreenScreen pages 102-103?
	public static final String listTypeHealthCanadaPrioritySubstanceListsReproductive = typeScreening; // screening or authoritative?
	// don't see it on the GreenScreen list
	// or is it one of the CEPA lists on GreenScreen pages 102-103?
	public static final String listTypeTEST_Experimental = typeScreening; // don't see it on the GreenScreen list
	public static final String listTypeTEST_Predicted = typePredicted; // don't see it on the GreenScreen list
	public static final String listTypeDenmark = typePredicted; // don't see it on the GreenScreen list
	static final String listTypeChemidplus = typeScreening;

	public static int scoreToInt(String score) {
		switch ( score ) {
			case scoreVH:
				return 5;
			case scoreH:
				return 4;
			case scoreM:
				return 3;
			case scoreL:
				return 2;
			case scoreVL:
				return 1;
			default:
				return 0;
		}
	}

	public static int getAuthorityWeight(String listType) {
		switch ( listType ) {
			case typeAuthoritative:
				return 3;
			case typeScreening:
				return 2;
			case typePredicted:
				return 1;
			default:
				return 0;
		}
	}

	// TODO- is it necessary to have both weight and authority weight?
	public static float getWeight(String listType) {
		switch ( listType ) {
			case typeAuthoritative:
				return 10;
			case typeScreening:
				return 5;
			case typePredicted:
				return 1;
			default:
				return 0;
		}
	}

	public static String getListType(String source) {
		String listType = "";

		switch ( source ) {
			case sourceJapan:
				return listTypeJapan;
			case sourceKorea:
				return listTypeKorea;
			case sourceMalaysia:
				return listTypeMalaysia;
			case sourceECHA_CLP:
				return listTypeECHA_CLP;
			case sourceGermany:
				return listTypeGermany;
			case sourceTEST_Experimental:
				return listTypeTEST_Experimental;
			case sourceTEST_Predicted:
				return listTypeTEST_Predicted;
			case sourceDenmark:
				return listTypeDenmark;
			case sourceTEDX:
				return listTypeTEDX;
			case sourceSIN:
				return listTypeSIN;
			case sourceIRIS:
				return listTypeIRIS;
			case sourceNewZealand:
				return listTypeNewZealand;
			case sourceROC:
				return listTypeROC;
			case sourceIARC:
				return listTypeIARC;
			case sourceProp65:
				return listTypeProp65;
			case sourceDSL:
				return listTypeDSL;
			case sourceUMD:
				return listTypeUMD;
			case sourceTSCA_Work_Plan:
				return listTypeTSCA_Work_Plan;
			case sourceReachVeryHighConcernList:
				return listTypeReachVeryHighConcernList;
			case sourceAustralia:
				return listTypeAustralia;
			case sourceNIOSH_Potential_Occupational_Carcinogens:
				return listTypeNIOSH_Carcinogen;
			case sourceEPAMidAtlanticHumanHealth:
				return listTypeEPAMidAtlanticHumanHealth;
			case sourceHealth_Canada_Priority_Substance_Lists_Carcinogenicity:
				return listTypeHealthCanadaPrioritySubstanceListsCarcinogenicity;
			case sourceHealth_Canada_Priority_Substance_Lists_Reproductive:
				return listTypeHealthCanadaPrioritySubstanceListsReproductive;
			case sourceCanada:
				return listTypeCanada;
			case sourceReproductiveCanada:
				return listTypeReproductiveCanada;
			case sourceChemIDplus:
				return listTypeChemidplus;
			default:
				System.out.println(source + "\tmissing list type");
		}

		return "";
	}

	public static boolean isInGreenScreen(String source) {
		switch ( source ) {
			case sourceJapan:
			case sourceNIOSH_Potential_Occupational_Carcinogens:
			case sourceKorea:
			case sourceMalaysia:
			case sourceECHA_CLP:
			case sourceGermany:
			case sourceSIN:
			case sourceIRIS:
			case sourceTEDX:
			case sourceNewZealand:
			case sourceROC:
			case sourceIARC:
			case sourceProp65:
			case sourceDSL:
			case sourceReachVeryHighConcernList:
			case sourceAustralia:
				return true;
			case sourceTEST_Experimental:
			case sourceTEST_Predicted:
			case sourceDenmark:
			case sourceUMD:
			case sourceTSCA_Work_Plan:
			case sourceEPAMidAtlanticHumanHealth:
			case sourceHealth_Canada_Priority_Substance_Lists_Carcinogenicity:
			case sourceHealth_Canada_Priority_Substance_Lists_Reproductive:
				return false;
			default:
				return false;
		}
	}
}
