package hazard;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

/**
* @author TMARTI02
*/
public class API_CCTE {

	static String urlBase="https://api-ccte.epa.gov";
	
	public static JsonObject searchByDTXSID(String dtxsid) {

		Gson gson = new Gson();
		
		
		String x_api_key = System.getenv().get("x-api-key");
		
		if(x_api_key==null) {
			System.out.println("Need to set x-api-key in environment variables");
			return null;
		}

		HttpResponse<String> response = Unirest.get(urlBase + "/chemical/detail/search/by-dtxsid/"+dtxsid)
				.header("accept", "application/json")
				.header("x-api-key", x_api_key).asString();
		
				
//		System.out.println(response.getBody().toString());

		
		JsonObject jo=gson.fromJson(response.getBody().toString(), JsonObject.class);
		return jo;

//		System.out.println(gsonNoNulls.toJson(hazardResult));
//		return hazardResult;
	}
	public static void main(String[] args) {

		searchByDTXSID("DTXSID1021740");

	}

}
