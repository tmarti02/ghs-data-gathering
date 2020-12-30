package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ToxQueryOptions extends QueryOptions {
	List<String> species;
	List<String> strain;
	List<String> routesOfAdministration;
	List<String> doseDescriptors;
	
	public ToxQueryBlock generateToxQueryBlock() {
		String endpointKind = propertyName;
		ToxQueryBlock toxQueryBlock = new ToxQueryBlock(endpointKind);
		toxQueryBlock.addInfoTypeField();
		toxQueryBlock.addReliabilityField(maxReliabilityLevel);
		
		// Endpoint value (called "Effect level" for tox data)
		toxQueryBlock.addEndpointField(endpointMin,endpointMax,endpointUnits);
		
		// Species
		if (species!=null && !species.isEmpty()) {
			toxQueryBlock.addSpeciesField(species,false);
		}
		
		// Route of administration
		if (routesOfAdministration!=null && !routesOfAdministration.isEmpty()) {
			toxQueryBlock.addAdministrationRouteField(routesOfAdministration,true);
		}
		
		// Dose descriptor
		if (doseDescriptors!=null && !doseDescriptors.isEmpty()) {
			toxQueryBlock.addDoseDescriptorField(doseDescriptors, false);
		}
		
		return toxQueryBlock;
	}
	
	/**
	 * Creates the Query object corresponding to the given options
	 * @return		The desired Query
	 */
	@Override
	public Query generateQuery() {
		Query query = new Query(limit);
		ToxQueryBlock toxQueryBlock = generateToxQueryBlock();
		query.addPropertyBlock(toxQueryBlock);
		return query;
	}
	
	public static void main(String[] args) {
		String[] species = {"rat","mouse","guinea pig","rabbit"};
		String[] routesOfAdministration = {"inhalation","inhalation: aerosol","inhalation: dust","inhalation: gas","inhalation: mist","inhalation: vapour",
				"inhalation: mixture of gas, vapour and aerosol","inhalation: mixture of gas and vapour","inhalation: mixture of vapour and aerosol / mist"};
		String[] doseDescriptors = {"LC50"};
		ToxQueryOptions tqo = new ToxQueryOptions();
		tqo.propertyName = "AcuteToxicityInhalation";
		tqo.endpointMin = "0";
		tqo.endpointMax = "10000000";
		tqo.endpointUnits = "mg/L air";
		tqo.species = Arrays.asList(species);
		tqo.routesOfAdministration = Arrays.asList(routesOfAdministration);
		tqo.doseDescriptors = Arrays.asList(doseDescriptors);
		Query query = tqo.generateQuery();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println(gson.toJson(query));
		tqo.runDownload("eChemPortalAPI_raw_tox_json.db", true);
	}
}
