package hazard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.UtilitiesUnirest;
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

	public static String getTESTDensity (String smiles) {
		
		HttpResponse<String> response= Unirest.get("https://comptox.epa.gov/dashboard/web-test/Density")
		.queryString("smiles",smiles)
		.queryString("method","consensus")
		.asString();		
		
		String json=response.getBody().toString();
		
		System.out.println(json);
		
		JsonObject jo=ParseUtilities.gson.fromJson(json, JsonObject.class);
		
		if(jo.get("predictions")==null) return null;
		
		JsonArray jaPredictions=jo.get("predictions").getAsJsonArray();
		
		if(jaPredictions.size()==0) {
			System.out.println(smiles+":\tpredictions array size=0");
			return null;
		}
		
		JsonObject joConsensus=jaPredictions.get(0).getAsJsonObject();
		
		String density=null;
		String src=null;
		
		if(joConsensus.get("expValMass")!=null) {
			density=joConsensus.get("expValMass").getAsString();
			src="Exp";
		} else if(joConsensus.get("predValMass")!=null) {
			density=joConsensus.get("predValMass").getAsString();
			src="TEST_Consensus";
		} else {
			return null;
		}
		
		String result=density+"\t"+src;
//		System.out.println(result);
//		System.out.println(ParseUtilities.gson.toJson(joConsensus));
		
		return result;
	}

	
	
//	{"identifierTypes":["CASRN"],"massError":0,"downloadItems":[],"searchItems":"71-43-2\n91-20-3","inputType":"IDENTIFIER"}

	static class BatchSearchInput {
		
		List<String>identifierTypes=new ArrayList<>();
		double massError;
		
		List<String>downloadItems=new ArrayList<>();
		String searchItems;
		String inputType;
	}
	
	
	
	
	public static void batchSearchByCAS(Collection<String>casrns) {
		
		
		BatchSearchInput bsi=new BatchSearchInput();
		bsi.identifierTypes.add("CASRN");
		bsi.searchItems="";
		
		for (String casrn:casrns) {
			bsi.searchItems+=casrn+"\n";
		}
		bsi.inputType="IDENTIFIER";
		
		Gson gson=new Gson();
		
		
		HttpResponse<String> response = Unirest.post("https://comptox.epa.gov/dashboard-api/batchsearch/chemicals")
		  .header("Content-Type", "application/json")
		  .body(gson.toJson(bsi))
		  .asString();
		
		System.out.println(response.getBody().toString());
	}
	

	public static String getSmilesFromCAS(String casrn) {
		
		BatchSearchInput bsi=new BatchSearchInput();
		bsi.identifierTypes.add("CASRN");
		bsi.searchItems=casrn;
		bsi.inputType="IDENTIFIER";
		
		Gson gson=new Gson();
		
		HttpResponse<String> response = Unirest.post("https://comptox.epa.gov/dashboard-api/batchsearch/chemicals")
		  .header("Content-Type", "application/json")
		  .body(gson.toJson(bsi))
		  .asString();
		
		String json=response.getBody().toString();
		JsonArray ja=ParseUtilities.gson.fromJson(json, JsonArray.class);

		if(ja.size()==0) return null;
		
		JsonObject jo=ja.get(0).getAsJsonObject();
		
		if(jo.get("smiles")==null || jo.get("smiles").isJsonNull()) return null;
		String smiles=jo.get("smiles").getAsString();
//		System.out.println(smiles);
		return smiles;

	}
 	
	
	public static void main(String[] args) {
		UtilitiesUnirest.configUnirest(true);
//		searchByDTXSID("DTXSID1021740");

//		batchSearchByCAS(Arrays.asList("71-43-2","91-20-3"));
//		getTESTDensity("CCO");
//		getTESTDensity("CCOCCOCOCOCOCOCCCC");
//		getTESTDensity("CCOCCOCOCOCOCOCCCCSSSSSSSSSS");
		
		
//		batchSearchByCAS(Arrays.asList("71-43-2"));
		getSmilesFromCAS("71-43-2");

	}
	
	
	

}
