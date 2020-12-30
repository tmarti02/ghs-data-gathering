package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.util.ArrayList;
import java.util.List;

public class ToxQueryBlock extends QueryBlock {
	
	public ToxQueryBlock(String endpointKind) {
		super(endpointKind);
	}
	
	/**
	 * Adds a QueryField to set endpoint value bounds
	 * @param lower		Lower endpoint value bound
	 * @param upper		Upper endpoint value bound
	 * @param unit		Desired endpoint value unit from ExperimentalConstants
	 */
	public void addEndpointField(String lower,String upper,String unit) {
		String fieldName = "ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion.EffectLevels.EffectLevel";
		String type = "range";
		Value endpointValue = new Value(type,lower,upper,new ToxUnit(unit));
		QueryField endpoint = new QueryField(fieldName,type,"Value",endpointValue);
		queryFields.add(endpoint);
	}
	
	/**
	 * Adds a QueryField to select species of results
	 * @param	List of species to include
	 */
	public void addSpeciesField(List<String> species,boolean includeOther) {
		List<Value> speciesValues = new ArrayList<Value>();
		for (String s:species) {
			speciesValues.add(new Value("EQUALS",s));
		}
		if (includeOther) {
			speciesValues.add(new Value("LIKE","other:*"));
		}
		QueryField speciesField = new QueryField("ENDPOINT_STUDY_RECORD."+endpointKind+".MaterialsAndMethods.TestAnimals.Species","string","Species",speciesValues);
		queryFields.add(speciesField);
	}
	
	/**
	 * Adds a QueryField to select administration route of results
	 * @param	List of administration routes to include
	 */
	public void addAdministrationRouteField(List<String> routesOfAdministration,boolean includeOther) {
		List<Value> routeValues = new ArrayList<Value>();
		for (String r:routesOfAdministration) {
			routeValues.add(new Value("EQUALS",r));
		}
		if (includeOther) {
			routeValues.add(new Value("LIKE","other:*"));
		}
		QueryField routeField = new QueryField("ENDPOINT_STUDY_RECORD."+endpointKind+".MaterialsAndMethods.AdministrationExposure.RouteOfAdministration",
				"string","Route of Administration",routeValues);
		queryFields.add(routeField);
	}
	
	/**
	 * Adds a QueryField to select dose descriptor of results
	 * @param	List of dose descriptors to include
	 */
	public void addDoseDescriptorField(List<String> doseDescriptors,boolean includeOther) {
		List<Value> doseDescriptorValues = new ArrayList<Value>();
		for (String d:doseDescriptors) {
			doseDescriptorValues.add(new Value("EQUALS",d));
		}
		if (includeOther) {
			doseDescriptorValues.add(new Value("LIKE","other:*"));
		}
		QueryField doseDescriptorField = new QueryField("ENDPOINT_STUDY_RECORD."+endpointKind+".ResultsAndDiscussion.EffectLevels.Endpoint",
				"string","DoseDescriptor",doseDescriptorValues);
		queryFields.add(doseDescriptorField);
	}
}