package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query;

/**
 * Replicates the QueryBlock object of an eChemPortal API search query JSON specific to toxicity properties
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class ToxQueryBlock extends QueryBlock {
	
	// Different versions of field name strings are used in constructing queries, depending on endpoint kind
	// Transient fields will not be serialized/deserialized in the JSON
	private transient String effectLevelString;
	private transient String effectLevelString2;
	private transient String speciesString;
	private transient String coverageString;
	private transient String typeString;
	private transient String strainString;
	private transient String routeString;
	private transient String valueTypeString;
	private transient String basisString;
	
	ToxQueryBlock(String endpointKind) {
		boolean isEcoTox = APIConstants.ecoToxEndpointsList.contains(endpointKind);
		this.endpointKind = endpointKind;
		effectLevelString2 = ".EffectLevel";
		if (endpointKind.contains("AcuteToxicity") || endpointKind.equals(APIConstants.toxicityToBirds)) {
			effectLevelString = "EffectLevels";
		} else if (endpointKind.contains("RepeatedDoseToxicity")) {
			effectLevelString = "EffectLevels.Efflevel";
		} else if (endpointKind.equals(APIConstants.toxicityToReproductionP0)) {
			effectLevelString = "ResultsOfExaminationsParentalAnimals.EffectLevelsP0.Efflevel";
			this.endpointKind = "ToxicityReproduction";
		} else if (endpointKind.equals(APIConstants.toxicityToReproductionF1)) {
			effectLevelString = "ResultsOfExaminationsOffspring.EffectLevelsF1.Efflevel";
			this.endpointKind = "ToxicityReproduction";
		} else if (endpointKind.equals(APIConstants.biodegradationInSoilHalfLife)) {
			effectLevelString = "HalfLifeOfParentCompound.HalfLife";
			effectLevelString2 = "";
			this.endpointKind = "BiodegradationInSoil";
		} else if (endpointKind.equals(APIConstants.biodegradationInSoilPctDegr)) {
			effectLevelString = "Degradation";
			effectLevelString2 = ".Degr";
			this.endpointKind = "BiodegradationInSoil";
		} else if (endpointKind.equals(APIConstants.biodegradationInWater)) {
			effectLevelString = "Degradation";
			effectLevelString2 = ".Degr";
		} else if (endpointKind.equals(APIConstants.bioaccumulationAquaticSediment)) {
			effectLevelString = "BioaccumulationFactor";
			effectLevelString2 = ".Value";
		} else {
			effectLevelString = "EffectConcentrations";
			effectLevelString2 = ".EffectConc";
		}
		
		if (endpointKind.equals(APIConstants.toxicityToSoilMicroorganisms)) {
			speciesString = ".MaterialsAndMethods.TestOrganisms.TestOrganismsInoculum";
		} else if (endpointKind.equals(APIConstants.toxicityToTerrestrialPlants)) {
			speciesString = ".MaterialsAndMethods.TestOrganisms.TestOrganisms.Species";
		} else if (isEcoTox) {
			speciesString = ".MaterialsAndMethods.TestOrganisms.TestOrganismsSpecies";
		} else if (endpointKind.equals(APIConstants.skinSensitisation)) {
			speciesString = ".MaterialsAndMethods.InVivoTestSystem.TestAnimals.Species";
			strainString = "InVivoTestSystem.TestAnimals.Strain";
		} else if (endpointKind.equals(APIConstants.geneticToxicityVitro)) {
			speciesString = ".ResultsAndDiscussion.TestRs.Organism";
		} else {
			speciesString = ".MaterialsAndMethods.TestAnimals.Species";
			strainString = "TestAnimals.Strain";
		}
		
		if (endpointKind.contains("Dermal")) {
			coverageString = "AdministrationExposure";
		} else if (endpointKind.equals(APIConstants.skinIrritationCorrosion)) {
			coverageString = "TestSystem";
		}
		
		if (endpointKind.equals(APIConstants.skinSensitisation)) {
			typeString = "TypeOfStudy";
		} else if (endpointKind.equals(APIConstants.geneticToxicityVivo)) {
			typeString = "Studytype";
		} else if (endpointKind.equals(APIConstants.geneticToxicityVitro)) {
			typeString = "TypeOfAssay";
		} else if (endpointKind.equals(APIConstants.bioaccumulationAquaticSediment)) {
			typeString = "StudyDesign.TestType";
		} else {
			typeString = "TestType";
		}
		
		if (endpointKind.equals(APIConstants.bioaccumulationAquaticSediment)) {
			routeString = "StudyDesign.RouteOfExposure";
			valueTypeString = ".Type";
			basisString = ".Basis";
		} else if (endpointKind.equals(APIConstants.biodegradationInWater)) {
			valueTypeString = ".Parameter";
		} else {
			routeString = "AdministrationExposure.RouteOfAdministration";
			valueTypeString = ".Endpoint";
			basisString = ".BasisForEffect";
		}
	}
	
	/**
	 * Adds a range-type QueryField for a single unit
	 * Must be overridden in order to use toxicity units
	 * @param field		The field type to add
	 * @param lower		Min value
	 * @param upper		Max value
	 * @param unit		Unit
	 */
	@Override
	public void addRangeField(String field,String lower,String upper,String unit) {
		String fieldName = getFieldName(field);
		Value value = new Value("range",lower,upper,new ToxUnit(unit,endpointKind));
		QueryField queryField = new QueryField(fieldName,"range",field,value);
		queryFields.add(queryField);
	}
	
	/**
	 * Assigns the field name used in the query based on endpoint and field
	 * @param field		The field to be added
	 * @return			The API field name
	 */
	@Override
	protected String getFieldName(String field) {
		String fieldName = super.getFieldName(field);
		if (fieldName!=null && !fieldName.isBlank()) { return fieldName; }
		
		switch (field) {
		case APIConstants.effectLevel:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion."+effectLevelString+effectLevelString2;
			break;
		case APIConstants.effectLevel+" Maternal":
			fieldName = "ENDPOINT_STUDY_RECORD.DevelopmentalToxicityTeratogenicity.ResultsAndDiscussion."
					+ "ResultsMaternalAnimals.EffectLevelsMaternalAnimals.Efflevel.EffectLevel";
			break;
		case APIConstants.effectLevel+" Fetal":
			fieldName = "ENDPOINT_STUDY_RECORD.DevelopmentalToxicityTeratogenicity.ResultsAndDiscussion.ResultsFetuses.EffectLevelsFetuses.Efflevel.EffectLevel";
			break;
		case APIConstants.testType:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".MaterialsAndMethods."+typeString;
			break;
		case APIConstants.species:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+speciesString;
			break;
		case APIConstants.strain:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".MaterialsAndMethods."+strainString;
			break;
		case APIConstants.routeOfAdministration:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".MaterialsAndMethods."+routeString;
			break;
		case APIConstants.inhalationExposureType:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".MaterialsAndMethods.AdministrationExposure.TypeOfInhalationExposure";
			break;
		case APIConstants.coverageType:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".MaterialsAndMethods."+coverageString+".TypeOfCoverage";
			break;
		case APIConstants.valueType:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion."+effectLevelString+valueTypeString;
			break;
		case APIConstants.valueType+" Maternal":
			fieldName = "ENDPOINT_STUDY_RECORD.DevelopmentalToxicityTeratogenicity.ResultsAndDiscussion."
					+ "ResultsMaternalAnimals.EffectLevelsMaternalAnimals.Efflevel.Endpoint";
			break;
		case APIConstants.valueType+" Fetal":
			fieldName = "ENDPOINT_STUDY_RECORD.DevelopmentalToxicityTeratogenicity.ResultsAndDiscussion.ResultsFetuses.EffectLevelsFetuses.Efflevel.Endpoint";
			break;
		case APIConstants.endpointType:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".AdministrativeData.Endpoint";
			break;
		case APIConstants.histoFindingsNeo:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion.ResultsOfExaminations.ObservHistopatholNeoplastic";
			break;
		case APIConstants.basis:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion."+effectLevelString+basisString;
			break;
		case APIConstants.basis+" Maternal":
			fieldName = "ENDPOINT_STUDY_RECORD.DevelopmentalToxicityTeratogenicity.ResultsAndDiscussion.ResultsMaternalAnimals.EffectLevelsMaternalAnimals.Efflevel.Basis";
			break;
		case APIConstants.basis+" Fetal":
			fieldName = "ENDPOINT_STUDY_RECORD.DevelopmentalToxicityTeratogenicity.ResultsAndDiscussion.ResultsFetuses.EffectLevelsFetuses.Efflevel.Basis";
			break;
		case APIConstants.interpretationOfResults:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ApplicantSummaryAndConclusion.InterpretationOfResults";
			break;
		case APIConstants.genotoxicity:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion.TestRs.Genotoxicity";
			break;
		case APIConstants.metabolicActivation:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion.TestRs.MetActIndicator";
			break;
		case APIConstants.oxygenConditions:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".MaterialsAndMethods.StudyDesign.OxygenConditions";
			break;
		case APIConstants.waterMediaType:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".MaterialsAndMethods.StudyDesign.WaterMediaType";
			break;
		default:
			return null;
		}
		return fieldName;
	}
	
	/**
	 * Adds a QueryField to include all duration values in results
	 */
	void addAllDurationField() {
		Value durationValue = new Value("numeric","GT_EQUALS","0",null);
		String fieldName = "";
		if (endpointKind.contains("BiodegradationInSoil") || endpointKind.equals(APIConstants.biodegradationInWater)) {
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion.Degradation.SamplingTime";
		} else {
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion."+effectLevelString+".Duration";
		}
		QueryField durationField = new QueryField(fieldName,"numeric",APIConstants.duration,durationValue);
		queryFields.add(durationField);
	}
	
	void addOtherGeneticToxicityFields() {
		String toxicityFieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion.TestRs.Toxicity";
		String cytotoxicityFieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion.TestRs.Cytotoxicity";
		Value value = new Value("LIKE","*");
		if (endpointKind.equals(APIConstants.geneticToxicityVivo)) {
			queryFields.add(new QueryField(toxicityFieldName,"string",APIConstants.toxicity,value));
		} else if (endpointKind.equals(APIConstants.geneticToxicityVitro)) {
			queryFields.add(new QueryField(cytotoxicityFieldName,"string",APIConstants.cytotoxicity,value));
		}
	}
}
