package gov.epa.ghs_data_gathering.Utilities;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import gov.epa.ghs_data_gathering.Utilities.JSONsForEChemPortal.ResultsData;

public class APIEChemPortal {
	
	private String generateBodyString() {
		String body = "";
		return body;
	}

	public static void main(String[] args) {
		Unirest.setTimeouts(0, 0);
		try {
			
			HttpResponse<String> response = Unirest.post("https://www.echemportal.org/echemportal/api/property-search")
			  .header("Content-Type", "application/json")
			  .header("Accept", "application/json")
			  .header("Cookie", "BIGipServerechemportal.org-http-ext_pool=570510602.36895.0000")
			  .body("{\"property_blocks\":[{\"level\":0,\"type\":\"property\","
			  		+ "\"id\":\"v56ovsc4qkij27s3b\","
			  		+ "\"queryBlock\":{\"endpointKind\":\"WaterSolubility\","
			  		+ "\"queryFields\":[{\"fieldName\":\"ENDPOINT_STUDY_RECORD.WaterSolubility.ResultsAndDiscussion.WaterSolubility.Solubility\","
			  		+ "\"type\":\"range\","
			  		+ "\"label\":\"Water solubility, Water solubility\","
			  		+ "\"values\":[{\"matchMode\":\"OVERLAPPING\","
			  		+ "\"searchValueLower\":\"10\",\"searchValueUpper\":\"20\","
			  		+ "\"unit\":{\"phraseGroupId\":\"P08\","
			  		+ "\"phraseId\":\"2500\"}}]}]}}],"
			  		+ "\"paging\":{\"offset\":0,\"limit\":5},"
			  		+ "\"filtering\":[],"
			  		+ "\"sorting\":[],"
			  		+ "\"participants\":[101,140,580,60,1]}")
			  .asString();
			
//			System.out.println(response.getBody());
			
			String json=response.getBody();
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			
			ResultsData data=gson.fromJson(json, ResultsData.class);
			System.out.println(gson.toJson(data));
		} catch (UnirestException e) {
			e.printStackTrace();
		}

	}

}
