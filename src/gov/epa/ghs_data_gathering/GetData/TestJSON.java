package gov.epa.ghs_data_gathering.GetData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class TestJSON {

	
	public class Beer{
		Endpoint Endpoint;
		
		
		class Endpoint {
			String code;
		}
	}
	
	
	
	public static void main(String[] args) {
		String bob="{\"Endpoint\":{\"code\":\"60934\"}}";
		System.out.println(bob);

		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();

		Beer beer=gson.fromJson(bob, Beer.class);
		
		System.out.println(beer.Endpoint.code);


	}

}
