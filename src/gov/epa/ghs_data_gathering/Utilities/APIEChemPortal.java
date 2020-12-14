package gov.epa.ghs_data_gathering.Utilities;


import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import gov.epa.api.ExperimentalConstants;
import gov.epa.ghs_data_gathering.Utilities.JSONsForEChemPortal.ResultsData;

public class APIEChemPortal {
	
	private String generateBodyString() {
		String body = "";
		// TODO create body string for query block from inputs
		return body;
	}
	
	private static Vector<ResultsData> queryAPI() {
		Vector<ResultsData> results = new Vector<ResultsData>();
		Unirest.setTimeouts(0, 0);
		String bodyString = "{\"property_blocks\":"
		  		+ "[{\"level\":0,"
		  		+ "\"type\":\"property\","
		  		+ "\"id\":\"v56ovsc4qkij27s3b\","
		  		+ "\"queryBlock\":"
		  		+ "{\"endpointKind\":\"WaterSolubility\","
		  		+ "\"queryFields\":"
		  		+ "[{\"fieldName\":\"ENDPOINT_STUDY_RECORD.WaterSolubility.ResultsAndDiscussion.WaterSolubility.Solubility\","
		  		+ "\"type\":\"range\","
		  		+ "\"label\":\"Water solubility, Water solubility\","
		  		+ "\"values\":"
		  		+ "[{\"matchMode\":\"OVERLAPPING\","
		  		+ "\"searchValueLower\":\"0\",\"searchValueUpper\":\"20\","
		  		+ "\"unit\":"
		  		+ "{\"phraseGroupId\":\"P08\","
		  		+ "\"phraseId\":\"2500\"}}]}]}}],"
		  		+ "\"paging\":{\"offset\":0,\"limit\":100},"
		  		+ "\"filtering\":[],"
		  		+ "\"sorting\":[],"
		  		+ "\"participants\":[101,140,580,60,1]}";
		try {
			HttpResponse<String> response = Unirest.post("https://www.echemportal.org/echemportal/api/property-search")
			  .header("Content-Type", "application/json")
			  .header("Accept", "application/json")
			  .header("Cookie", "BIGipServerechemportal.org-http-ext_pool=570510602.36895.0000")
			  .body(bodyString)
			  .asString();
			Thread.sleep(200);
			String json=response.getBody();
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			ResultsData data=gson.fromJson(json, ResultsData.class);
			System.out.println(gson.toJson(data));
			results.add(data);
			
			int pages = data.pageInfo.totalPages;
			int offset = 0;
			for (int i = 0; i < pages; i++) {
				offset += 100;
				bodyString = "{\"property_blocks\":"
				  		+ "[{\"level\":0,"
				  		+ "\"type\":\"property\","
				  		+ "\"id\":\"v56ovsc4qkij27s3b\","
				  		+ "\"queryBlock\":"
				  		+ "{\"endpointKind\":\"WaterSolubility\","
				  		+ "\"queryFields\":"
				  		+ "[{\"fieldName\":\"ENDPOINT_STUDY_RECORD.WaterSolubility.ResultsAndDiscussion.WaterSolubility.Solubility\","
				  		+ "\"type\":\"range\","
				  		+ "\"label\":\"Water solubility, Water solubility\","
				  		+ "\"values\":"
				  		+ "[{\"matchMode\":\"OVERLAPPING\","
				  		+ "\"searchValueLower\":\"0\",\"searchValueUpper\":\"20\","
				  		+ "\"unit\":"
				  		+ "{\"phraseGroupId\":\"P08\","
				  		+ "\"phraseId\":\"2500\"}}]}]}}],"
				  		+ "\"paging\":{\"offset\":"+String.valueOf(offset)+",\"limit\":100},"
				  		+ "\"filtering\":[],"
				  		+ "\"sorting\":[],"
				  		+ "\"participants\":[101,140,580,60,1]}";
				response = Unirest.post("https://www.echemportal.org/echemportal/api/property-search")
						  .header("Content-Type", "application/json")
						  .header("Accept", "application/json")
						  .header("Cookie", "BIGipServerechemportal.org-http-ext_pool=570510602.36895.0000")
						  .body(bodyString)
						  .asString();
				Thread.sleep(200);
				json = response.getBody();
				data = gson.fromJson(json, ResultsData.class);
				results.add(data);
			}
			return results;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		Vector<ResultsData> results = queryAPI();
		String filePath = "Data" + File.separator + "Experimental" + File.separator + ExperimentalConstants.strSourceEChem + File.separator +
				ExperimentalConstants.strSourceEChem + " API Records.json";
		try {

			File file = new File(filePath);
			file.getParentFile().mkdirs();

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();

			FileWriter fw = new FileWriter(file);
			fw.write(gson.toJson(results));
			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
