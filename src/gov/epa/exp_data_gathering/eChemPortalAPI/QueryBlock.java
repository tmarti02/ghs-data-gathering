package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class QueryBlock {
	@SerializedName("endpointKind")
	@Expose
	public String endpointKind;
	@SerializedName("queryFields")
	@Expose
	public List<QueryField> queryFields = null;
	
	private transient String endpointKind2;
	private transient String endpointKind3;
	
	public QueryBlock(String setEndpointKind) {
		endpointKind = setEndpointKind;
		
		if (endpointKind.equals("Melting")) {
			endpointKind2 = "MeltingPoint";
		} else if (endpointKind.equals("Vapour")) {
			endpointKind2 = "Vapourpr";
		} else if (endpointKind.equals("Partition")) {
			endpointKind2 = "Partcoeff";
		} else if (endpointKind.equals("HenrysLawConstant")) {
			endpointKind2 = "HenrysLawConstantH";
		} else {
			endpointKind2 = endpointKind;
		}
		
		if (endpointKind.equals("FlashPoint")) {
			endpointKind3 = "FPoint";
		} else if (endpointKind.equals("Vapour")) {
			endpointKind3 = "Pressure";
		} else if (endpointKind.equals("WaterSolubility")) {
			endpointKind3 = "Solubility";
		} else if (endpointKind.equals("DissociationConstant")) {
			endpointKind3 = "pka";
		} else if (endpointKind.equals("HenrysLawConstant")) {
			endpointKind3 = "H";
		} else if (endpointKind.equals("Partition")) {
			endpointKind3 = endpointKind;
		} else {
			endpointKind3 = endpointKind2;
		}
		
		queryFields = new ArrayList<QueryField>();
	}
	
	/**
	 * Adds a QueryField to guarantee only experimental study results
	 */
	public void addInfoTypeField() {
		Value infoTypeValue = new Value("EQUALS","experimental study");
		QueryField infoType = new QueryField("ENDPOINT_STUDY_RECORD."+endpointKind+".AdministrativeData.StudyResultType","string",infoTypeValue);
		queryFields.add(infoType);
	}

	/**
	 * Adds a QueryField to guarantee results with reliability <= maxReliabilityLevel
	 * NOTE: Higher reliability level = more results, less reliable (2 recommended)
	 * @param minReliability	1 = reliable w/o restrictions, 2 = reliable w/ restrictions, 3 = not reliable, 4 = not assignable
	 */
	public void addReliabilityField(int maxReliabilityLevel) {
		List<Value> reliabilityValues = new ArrayList<Value>();
		Value reliabilityValue1 = new Value("EQUALS","1 (reliable without restriction)");
		Value reliabilityValue2 = new Value("EQUALS","2 (reliable with restrictions)");
		Value reliabilityValue3 = new Value("EQUALS","3 (not reliable)");
		Value reliabilityValue4 = new Value("EQUALS","4 (not assignable)");
		Value[] reliabilityValuesArray = {reliabilityValue1,reliabilityValue2,reliabilityValue3,reliabilityValue4};
		for (int i = 0; i < maxReliabilityLevel; i++) {
			reliabilityValues.add(reliabilityValuesArray[i]);
		}
		QueryField reliability = new QueryField("ENDPOINT_STUDY_RECORD."+endpointKind+".AdministrativeData.Reliability","string",reliabilityValues);
		queryFields.add(reliability);
	}
	
	public void addAtmPressureField(String opOrLower,String valueOrUpper,String desiredUnit) {
		String fieldName = "";
		String type = "";
		if (endpointKind.equals("Melting") || endpointKind.equals("BoilingPoint")) {
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion."+endpointKind2+".Pressure";
			type = "range";
		} else if (endpointKind.equals("FlashPoint")) {
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion."+endpointKind2+".AtmPressure";
			type = "range";
		} else if (endpointKind.equals("HenrysLawConstant")) {
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion."+endpointKind2+".AtmPressure";
			type = "numeric";
		}
		Value atmPressureValue = new Value(type,opOrLower,valueOrUpper,new Unit(desiredUnit));
		QueryField atmPressure = new QueryField(fieldName,type,atmPressureValue);
		queryFields.add(atmPressure);
	}
	
	public void addTemperatureField(String opOrLower,String valueOrUpper,String desiredUnit) {
		String fieldName = "";
		String type = "";
		if (endpointKind.equals("Density") || endpointKind.equals("Partition") || endpointKind.equals("WaterSolubility") || endpointKind.equals("DissociationConstant") ||
				endpointKind.equals("HenrysLawConstant")) {
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion."+endpointKind2+".Temp";
			type = "numeric";
		} else if (endpointKind.equals("Vapour")) {
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion."+endpointKind2+".TempQualifier";
			type = "range";
		}
		Value temperatureValue = new Value(type,opOrLower,valueOrUpper,new Unit(desiredUnit));
		QueryField temperature = new QueryField(fieldName,type,temperatureValue);
		queryFields.add(temperature);
	}
	
	public void addpHField(String lower,String upper) {
		String fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion."+endpointKind2+".Ph";
		String type = "range";
		Value pHValue = new Value(type,lower,upper,new Unit(""));
		QueryField pH = new QueryField(fieldName,type,pHValue);
		queryFields.add(pH);
	}
	
	public void addEndpointField(String lower,String upper,String desiredUnit) {
		String fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion."+endpointKind2+"."+endpointKind3;
		String type = "range";
		Value endpointValue = new Value(type,lower,upper,new Unit(desiredUnit));
		QueryField endpoint = new QueryField(fieldName,type,endpointValue);
		queryFields.add(endpoint);
	}
	
	public void addPartitionCoefficientFields() {
		Value type1Value = new Value("EQUALS","octanol-water");
		Value type2Value = new Value("EQUALS","log Pow");
		QueryField type1 = new QueryField("ENDPOINT_STUDY_RECORD.Partition.MaterialsAndMethods.PartitionCoefficientType","string",type1Value);
		QueryField type2 = new QueryField("ENDPOINT_STUDY_RECORD.Partition.ResultsAndDiscussion.Partcoeff.Type","string",type2Value);
		queryFields.add(type1);
		queryFields.add(type2);
	}
	
	public void addWaterSolubilityFields() {
		Value typeValue = new Value("EQUALS","water solubility");
		QueryField type = new QueryField("ENDPOINT_STUDY_RECORD.WaterSolubility.AdministrativeData.Endpoint","string",typeValue);
		queryFields.add(type);
	}
}