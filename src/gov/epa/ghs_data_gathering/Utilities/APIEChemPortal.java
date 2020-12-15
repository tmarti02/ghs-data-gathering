package gov.epa.ghs_data_gathering.Utilities;


import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import gov.epa.api.ExperimentalConstants;
import gov.epa.ghs_data_gathering.Utilities.JSONsForEChemPortal.PropertyBlock;
import gov.epa.ghs_data_gathering.Utilities.JSONsForEChemPortal.QueryBlock;
import gov.epa.ghs_data_gathering.Utilities.JSONsForEChemPortal.QueryData;
import gov.epa.ghs_data_gathering.Utilities.JSONsForEChemPortal.QueryField;
import gov.epa.ghs_data_gathering.Utilities.JSONsForEChemPortal.ResultsData;
import gov.epa.ghs_data_gathering.Utilities.JSONsForEChemPortal.Unit;
import gov.epa.ghs_data_gathering.Utilities.JSONsForEChemPortal.Value;

public class APIEChemPortal {
	private static Gson unprettyGson = new GsonBuilder().create();
	private static Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
	
	private static QueryData generateQueryData() {
		Unit kelvin = new Unit("A102","3887");
		Value meltingPointValue = new Value("0","400",kelvin);
		QueryField meltingPoint = new QueryField("ENDPOINT_STUDY_RECORD.Melting.ResultsAndDiscussion.MeltingPoint.MeltingPoint","range",meltingPointValue);
		Unit mmHg = new Unit("P02","2121");
		Value atmPressureValue = new Value("0",null,mmHg);
		QueryField atmPressure = new QueryField("ENDPOINT_STUDY_RECORD.Melting.ResultsAndDiscussion.MeltingPoint.Pressure","range",atmPressureValue);
		Value reliabilityValue1 = new Value("EQUALS","1 (reliable without restriction)");
		Value reliabilityValue2 = new Value("EQUALS","2 (reliable with restriction)");
		QueryField reliability = new QueryField("ENDPOINT_STUDY_RECORD.Melting.AdministrativeData.Reliability","string",reliabilityValue1);
		reliability.values.add(reliabilityValue2);
		Value infoTypeValue = new Value("EQUALS","experimental study");
		QueryField infoType = new QueryField("ENDPOINT_STUDY_RECORD.Melting.AdministrativeData.StudyResultType","string",infoTypeValue);
		QueryBlock queryBlock = new QueryBlock("Melting",infoType);
		queryBlock.queryFields.add(reliability);
		queryBlock.queryFields.add(meltingPoint);
		queryBlock.queryFields.add(atmPressure);
		PropertyBlock propertyBlock = new PropertyBlock(1,"property",queryBlock);
		QueryData query = new QueryData(propertyBlock);
		return query;
	}
	
	private static int testQuerySize(QueryData query) {
		String bodyString = unprettyGson.toJson(query);
		try {	
			HttpResponse<String> response = Unirest.post("https://www.echemportal.org/echemportal/api/property-search")
			  .header("Content-Type", "application/json")
			  .header("Accept", "application/json")
			  .body(bodyString)
			  .asString();
			String json=response.getBody();
			ResultsData data=prettyGson.fromJson(json, ResultsData.class);	
			return data.pageInfo.totalElements;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return 0;
	}
	
	private static Vector<ResultsData> queryAPI() {
		Vector<ResultsData> results = new Vector<ResultsData>();
		Unirest.setTimeouts(0, 0);
		QueryData query = generateQueryData();
		String bodyString = unprettyGson.toJson(query);
		try {	
			HttpResponse<String> response = Unirest.post("https://www.echemportal.org/echemportal/api/property-search")
			  .header("Content-Type", "application/json")
			  .header("Accept", "application/json")
			  .body(bodyString)
			  .asString();
			Thread.sleep(1000);
			String json=response.getBody();
			ResultsData data=prettyGson.fromJson(json, ResultsData.class);
			results.add(data);
			
			int pages = data.pageInfo.totalPages;
			int offset = 0;
			for (int i = 1; i < pages; i++) {
				offset += 100;
				query.updateOffset(offset);
				bodyString = unprettyGson.toJson(query);
				response = Unirest.post("https://www.echemportal.org/echemportal/api/property-search")
						  .header("Content-Type", "application/json")
						  .header("Accept", "application/json")
						  .body(bodyString)
						  .asString();
				Thread.sleep(1000);
				json = response.getBody();
				data = prettyGson.fromJson(json, ResultsData.class);
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
