package hazard;

import java.util.List;

import com.google.gson.Gson;

import hazard.HazardInput.Options;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

/**
 * @author TMARTI02
 */
public class HazardResult {

	Options options;
	List<HazardChemical> skippedChemicals;
	List<HazardChemical> hazardChemicals;

	public static HazardResult runHazard(HazardInput hi) {

		Gson gson = new Gson();

		HttpResponse<JsonNode> response = Unirest.post(CaseStudies.urlBase + "/hazard")
				.header("Content-Type", "application/json")
				.body(gson.toJson(hi)).asJson();

//		System.out.println(response.getBody().toString());

		HazardResult hazardResult = gson.fromJson(response.getBody().toString(), HazardResult.class);

//		System.out.println(gsonNoNulls.toJson(hazardResult));
		return hazardResult;
	}
	
	public static String runHazard2(HazardInput hi) {

		Gson gson = new Gson();

		HttpResponse<JsonNode> response = Unirest.post(CaseStudies.urlBase + "/hazard")
				.header("Content-Type", "application/json")
				.body(gson.toJson(hi)).asJson();

//		System.out.println(response.getBody().toString());


//		System.out.println(gsonNoNulls.toJson(hazardResult));
		return response.getBody().toString();
	}

	public List<HazardChemical> getHazardChemicals() {
		return hazardChemicals;
	}

	public void setHazardChemicals(List<HazardChemical> hazardChemicals) {
		this.hazardChemicals = hazardChemicals;
	}

}
