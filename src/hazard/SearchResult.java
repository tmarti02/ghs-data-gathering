package hazard;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.google.gson.Gson;


import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

public class SearchResult {

	public long limit;
	public long offset;
	public int totalRecordsCount;
	ArrayList<Chemical> records = new ArrayList<>();
	Request request;
	Features features;
	public int recordsCount;

	public class Features {
		public float isotopes;
		public float charged;
		public float multicomponent;
		public float radicals;
		public float salts;
		public float polymers;
		public float sgroups;
		public float stereo;
		public float chiral;
		Elements ElementsObject;
		
	}
	
	

	public class Elements {
		public float Br;
		public float C;
		public float F;
		public float H;
		public float Cl;
		public float N;
		public float O;
	}

	public class Request {
		public String searchType;
		public String inputType;
		public String query;
		public String smiles;
		public String querySmiles;
		public int offset;
		public int limit;
		public String sortBy;
		public String sortDirection;
		public Params params;

		
	}
	
	public static SearchResult search(int desiredCount,String smiles) {
		
		SearchInput si=new SearchInput();
		si.query=smiles;
			
		DecimalFormat df=new DecimalFormat("0.00");
		
		for (double minSim=0.95;minSim>=0;minSim-=0.02) {
//		for (double minSim=0.39;minSim>=0;minSim-=0.02) {
			si.params.min_similarity=minSim;
			si.params.min_authority="Screening";
			
			
			SearchResult searchResult=SearchResult.runSearch(si);
			
			
			String sim=df.format(minSim);
			
			System.out.println(sim+"\t"+searchResult.recordsCount);
			
			if (searchResult.recordsCount >= desiredCount) {
//				System.out.println("here");
				return searchResult;
			}
		}
		
		return null;

	}

	
	public static SearchResult runSearch(SearchInput si) {
		
		Gson gson=new Gson();  
		
		HttpResponse<JsonNode> response = Unirest.post(CaseStudies.urlBase+"/search")		        				
		.header("Content-Type", "application/json")
		//Not working for some reason???
//		.body(br)
		.body(gson.toJson(si))
		.asJson();
		
//	System.out.println(response.getBody().toString());
			
	SearchResult searchResult=gson.fromJson(response.getBody().toString(), SearchResult.class);

//	System.out.println(gson.toJson(searchResult));
	return searchResult;
}

}