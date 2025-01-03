package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query.QueryOptions.StringField;

/**
 * Replicates the QueryBlock object of an eChemPortal API search query JSON
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class QueryBlock {
	@SerializedName("endpointKind")
	@Expose
	public String endpointKind;
	@SerializedName("queryFields")
	@Expose
	protected List<QueryField> queryFields = null;
	
	// Up to three different versions of the endpointKind string are used in constructing queries
	// Transient fields will not be serialized/deserialized in the JSON
	private transient String endpointKind2;
	private transient String endpointKind3;
	
	protected QueryBlock() {
		queryFields = new ArrayList<QueryField>();
	}
	
	/**
	 * Constructor that assigns alternative versions of the endpointKind string used in queries
	 * @param endpointKind		The endpoint kind to query
	 */
	protected QueryBlock(String endpointKind) {
		this.endpointKind = endpointKind;
		
		if (this.endpointKind.equals(APIConstants.meltingPoint)) {
			endpointKind2 = "MeltingPoint";
		} else if (this.endpointKind.equals(APIConstants.vaporPressure)) {
			endpointKind2 = "Vapourpr";
		} else if (this.endpointKind.equals(APIConstants.partitionCoefficient)) {
			endpointKind2 = "Partcoeff";
		} else if (this.endpointKind.equals(APIConstants.henrysLawConstant)) {
			endpointKind2 = "HenrysLawConstantH";
		} else {
			endpointKind2 = this.endpointKind;
		}
		
		if (this.endpointKind.equals(APIConstants.flashPoint)) {
			endpointKind3 = "FPoint";
		} else if (this.endpointKind.equals(APIConstants.vaporPressure)) {
			endpointKind3 = "Pressure";
		} else if (this.endpointKind.equals(APIConstants.waterSolubility)) {
			endpointKind3 = "Solubility";
		} else if (this.endpointKind.equals(APIConstants.dissociationConstant)) {
			endpointKind3 = "pka";
		} else if (this.endpointKind.equals(APIConstants.henrysLawConstant)) {
			endpointKind3 = "H";
		} else if (this.endpointKind.equals(APIConstants.partitionCoefficient)) {
			endpointKind3 = this.endpointKind;
		} else {
			endpointKind3 = endpointKind2;
		}
		
		queryFields = new ArrayList<QueryField>();
	}
	
	/**
	 * Adds a QueryField to guarantee only experimental study results
	 */
	void addInfoTypeField() {
		Value infoTypeValue = new Value("EQUALS","experimental study");
		QueryField infoType = new QueryField("ENDPOINT_STUDY_RECORD."+endpointKind+".AdministrativeData.StudyResultType","string","InfoType",infoTypeValue);
		queryFields.add(infoType);
	}

	/**
	 * Adds a QueryField to guarantee results with reliability {@literal <}= maxReliabilityLevel (2 recommended)
	 * @param maxReliabilityLevel	1 = reliable w/o restrictions, 2 = reliable w/ restrictions, 3 = not reliable, 4 = not assignable, 5 = includes "other"
	 */
	void addReliabilityField(int maxReliabilityLevel) {
		List<Value> reliabilityValues = new ArrayList<Value>();
		Value reliabilityValue1 = new Value("EQUALS","1 (reliable without restriction)");
		Value reliabilityValue2 = new Value("EQUALS","2 (reliable with restrictions)");
		Value reliabilityValue3 = new Value("EQUALS","3 (not reliable)");
		Value reliabilityValue4 = new Value("EQUALS","4 (not assignable)");
		Value reliabilityValueOther = new Value("LIKE","other:*");
		Value[] reliabilityValuesArray = {reliabilityValue1,reliabilityValue2,reliabilityValue3,reliabilityValue4,reliabilityValueOther};
		for (int i = 0; i < maxReliabilityLevel; i++) {
			reliabilityValues.add(reliabilityValuesArray[i]);
		}
		QueryField reliability = new QueryField("ENDPOINT_STUDY_RECORD."+endpointKind+".AdministrativeData.Reliability","string","Reliability",reliabilityValues);
		queryFields.add(reliability);
	}
	
	/**
	 * Adds a QueryField to set the year after which results should be queried
	 * eChemPortal does not permit querying years as ranges - maybe an issue with SQL BETWEEN?
	 * @param afterYear		Year to query after (inclusive)
	 */
	void addAfterYearField(String afterYear) {
		Value afterYearValue = new Value("numeric","GT_EQUALS",afterYear,null);
		QueryField afterYearField = new QueryField("ENDPOINT_STUDY_RECORD."+endpointKind+".DataSource.Reference+LITERATURE.GeneralInfo.ReferenceYear",
				"numeric","After Year",afterYearValue);
		queryFields.add(afterYearField);
	}
	
	/**
	 * Adds a QueryField to set the year before which results should be queried
	 * eChemPortal does not permit querying years as ranges - maybe an issue with SQL BETWEEN?
	 * @param beforeYear	Year to query before (exclusive)
	 */
	void addBeforeYearField(String beforeYear) {
		Value beforeYearValue = new Value("numeric","LT",beforeYear,null);
		QueryField beforeYearField = new QueryField("ENDPOINT_STUDY_RECORD."+endpointKind+".DataSource.Reference+LITERATURE.GeneralInfo.ReferenceYear",
				"numeric","Before Year",beforeYearValue);
		queryFields.add(beforeYearField);
	}
	
	/**
	 * Adds a range-type QueryField for a single unit
	 * @param field		The field type to add
	 * @param lower		Min value
	 * @param upper		Max value
	 * @param unit		Unit
	 */
	void addRangeField(String field,String lower,String upper,String unit) {
		String fieldName = getFieldName(field);
		Value value = new Value("range",lower,upper,new Unit(unit,endpointKind));
		QueryField queryField = new QueryField(fieldName,"range",field,value);
		queryFields.add(queryField);
	}
	
	/**
	 * Adds a range-type QueryField that includes all units
	 * @param field		The field type to add
	 * @param lower		Min value
	 * @param upper		Max value
	 */
	void addAllUnitRangeField(String field,String lower,String upper) {
		String fieldName = getFieldName(field);
		Value value = new Value("range",lower,upper,null);
		QueryField queryField = new QueryField(fieldName,"range",field,value);
		queryFields.add(queryField);
	}
	
	/**
	 * Adds a string-type QueryField
	 * @param stringField
	 */
	void addStringField(StringField stringField) {
		String fieldName = getFieldName(stringField.field);
		List<Value> values = new ArrayList<Value>();
		if (stringField.includeAll) {
			// If includeAll = true, adds a wildcard value to retrieve all values
			values.add(new Value("LIKE","*"));
		} else {
			// Otherwise, adds a specified list of strings to retrieve, with or without including "other"
			for (String s:stringField.fieldValues) {
				values.add(new Value("EQUALS",s));
			}
			if (stringField.includeOther) {
				values.add(new Value("LIKE","other:*"));
			}
		}
		QueryField queryField = new QueryField(fieldName,"string",stringField.field,values);
		queryFields.add(queryField);
	}
	
	/**
	 * Assigns the field name used in the query based on endpoint and field
	 * @param field		The field to be added
	 * @return			The API field name
	 */
	protected String getFieldName(String field) {
		String fieldName = "";
		switch (field) {
		case APIConstants.endpoint:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion."+endpointKind2+"."+endpointKind3;
			break;
		case APIConstants.pressure:
			if (endpointKind.equals(APIConstants.meltingPoint) || endpointKind.equals(APIConstants.boilingPoint)) {
				fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion."+endpointKind2+".Pressure";
			} else if (endpointKind.equals(APIConstants.flashPoint) || endpointKind.equals(APIConstants.henrysLawConstant)) {
				fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion."+endpointKind2+".AtmPressure";
			}
			break;
		case APIConstants.temperature:
			if (endpointKind.equals(APIConstants.density) || endpointKind.equals(APIConstants.partitionCoefficient) || 
					endpointKind.equals(APIConstants.waterSolubility) || endpointKind.equals(APIConstants.dissociationConstant) ||
					endpointKind.equals(APIConstants.henrysLawConstant)) {
				fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion."+endpointKind2+".Temp";
			} else if (endpointKind.equals(APIConstants.vaporPressure)) {
				fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion."+endpointKind2+".TempQualifier";
			}
			break;
		case APIConstants.pH:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion."+endpointKind2+".Ph";
			break;
		case APIConstants.glpCompliance:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".MaterialsAndMethods.GLPComplianceStatement";
			break;
		case APIConstants.guideline:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".MaterialsAndMethods.Guideline.Guideline";
			break;
		case APIConstants.guidelineQualifier:
			fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".MaterialsAndMethods.Guideline.Qualifier";
			break;
		default:
			return null;
		}
		return fieldName;
	}
	
	/**
	 * For partition coefficient queries, adds QueryFields to guarantee octanol-water partition coefficient and include both Pow and log Pow records
	 */
	void addPartitionCoefficientFields() {
		Value type1Value = new Value("EQUALS","octanol-water");
		List<Value> type2Values = new ArrayList<Value>();
		type2Values.add(new Value("EQUALS","log Pow"));
		type2Values.add(new Value("EQUALS","Pow"));
		QueryField type1 = new QueryField("ENDPOINT_STUDY_RECORD.Partition.MaterialsAndMethods.PartitionCoefficientType","string","Type",type1Value);
		QueryField type2 = new QueryField("ENDPOINT_STUDY_RECORD.Partition.ResultsAndDiscussion.Partcoeff.Type","string","isLog",type2Values);
		queryFields.add(type1);
		queryFields.add(type2);
	}
	
	/**
	 * For water solubility queries, adds a QueryField to guarantee water solubility rather than metal complex dissolution/reaction
	 */
	void addWaterSolubilityFields() {
		Value typeValue = new Value("EQUALS","water solubility");
		QueryField type = new QueryField("ENDPOINT_STUDY_RECORD.WaterSolubility.AdministrativeData.Endpoint","string","Solvent",typeValue);
		queryFields.add(type);
	}
}