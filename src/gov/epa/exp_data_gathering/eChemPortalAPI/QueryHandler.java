package gov.epa.exp_data_gathering.eChemPortalAPI;


import java.io.File;
import java.io.FileWriter;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.ResultsData;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Builds and runs queries on the eChemPortal API
 * @author GSINCL01
 *
 */
public class QueryHandler {
	private Gson gson = null;
	private Gson prettyGson = null;
	private Logger logger = null;
	
	public QueryHandler() {
		gson = new GsonBuilder().create();
		prettyGson = new GsonBuilder().setPrettyPrinting().create();
		logger = (Logger) LoggerFactory.getLogger("org.apache.http");
		// Can adjust debug logging as desired
    	logger.setLevel(Level.WARN);
    	logger.setAdditive(false);
	}
	
	/**
	 * Translates our endpoint identifiers to eChemPortal's
	 * @param propertyName	Endpoint to query from ExperimentalConstants
	 * @return				eChemPortal's corresponding endpoint identifier
	 */
	private static String getEndpointKind(String propertyName) {
		String endpointKind = "";
		switch (propertyName) {
		case ExperimentalConstants.strMeltingPoint:
			endpointKind = "Melting";
			break;
		case ExperimentalConstants.strBoilingPoint:
			endpointKind = "BoilingPoint";
			break;
		case ExperimentalConstants.strFlashPoint:
			endpointKind = "FlashPoint";
			break;
		case ExperimentalConstants.strDensity:
			endpointKind = "Density";
			break;
		case ExperimentalConstants.strVaporPressure:
			endpointKind = "Vapour";
			break;
		case ExperimentalConstants.strLogKow:
			endpointKind = "Partition";
			break;
		case ExperimentalConstants.strWaterSolubility:
			endpointKind = "WaterSolubility";
			break;
		case ExperimentalConstants.str_pKA:
			endpointKind = "DissociationConstant";
			break;
		case ExperimentalConstants.strHenrysLawConstant:
			endpointKind = "HenrysLawConstant";
			break;
		}
		return endpointKind;
	}
	
	/**
	 * Creates an API query with indicated options
	 * @param propertyName				Endpoint to query from ExperimentalConstants
	 * @param maxReliabilityLevel		Maximum reliability to return (1 = most reliable, 4 = least; 2 recommended)
	 * @param endpointMin				Minimum value for endpoint (may be null)
	 * @param endpointMax				Maximum value for endpoint (may be null)
	 * @param endpointUnits				Endpoint units from ExperimentalConstants
	 * @param pressureOpOrLower			Operator (ex. "GT_EQUALS") or minimum value for pressure condition // TODO handle this more cleanly
	 * @param pressureValueOrUpper		Value or maximum value for pressure condition // TODO handle this more cleanly
	 * @param pressureUnits				Pressure condition units from ExperimentalConstants
	 * @param temperatureOpOrLower		Operator (ex. "GT_EQUALS") or minimum value for temperature condition // TODO handle this more cleanly
	 * @param temperatureValueOrUpper	Value or maximum value for temperature condition // TODO handle this more cleanly
	 * @param temperatureUnits			Temperature condition units from ExperimentalConstants
	 * @param pHMin						Minimum value for pH condition
	 * @param pHMax						Maximum value for pH condition
	 * @return							A QueryData object with the indicated options
	 */
	private static QueryData generateQueryData(String propertyName,int maxReliabilityLevel,
			String endpointMin,String endpointMax,String endpointUnits,
			String pressureOpOrLower,String pressureValueOrUpper,String pressureUnits,
			String temperatureOpOrLower,String temperatureValueOrUpper,String temperatureUnits,
			String pHMin,String pHMax) {
		String endpointKind = getEndpointKind(propertyName);
		QueryBlock queryBlock = new QueryBlock(endpointKind);
		queryBlock.addInfoTypeField();
		queryBlock.addReliabilityField(maxReliabilityLevel);
		
		// Disambiguates potential unit collisions between density and solubility, pressure and HLC
		if (endpointUnits.equals(ExperimentalConstants.str_g_L) || endpointUnits.equals(ExperimentalConstants.str_g_cm3) ||
				endpointUnits.equals(ExperimentalConstants.str_kg_m3)) {
			if (endpointKind.equals("Density")) {
				endpointUnits += "_density";
			} else if (endpointKind.equals("WaterSolubility")) {
				endpointUnits += "_solubility";
			}
		} else if (endpointUnits.equals(ExperimentalConstants.str_atm)) {
			if (endpointKind.equals("Vapour")) {
				endpointUnits += "_VP";
			} else if (endpointKind.equals("HenrysLawConstant")) {
				endpointUnits += "_H";
			}
		}
		
		// Endpoint range
		queryBlock.addEndpointField(endpointMin,endpointMax,endpointUnits);
		
		// Pressure condition
		if (endpointKind.equals("Melting") || endpointKind.equals("BoilingPoint") || endpointKind.equals("FlashPoint") || endpointKind.equals("HenrysLawConstant")) {
			queryBlock.addAtmPressureField(pressureOpOrLower,pressureValueOrUpper,pressureUnits);
		} else if (pressureOpOrLower!=null || pressureValueOrUpper!=null || pressureUnits!=null) {
			System.out.println("Warning: Pressure conditions not supported for "+propertyName+". Non-null values ignored.");
		}
		
		// Temperature condition
		if (endpointKind.equals("Density") || endpointKind.equals("Vapour") || endpointKind.equals("Partition") || endpointKind.equals("WaterSolubility") ||
				endpointKind.equals("DissociationConstant") || endpointKind.equals("HenrysLawConstant")) {
			queryBlock.addTemperatureField(temperatureOpOrLower,temperatureValueOrUpper,temperatureUnits);
		} else if (temperatureOpOrLower!=null || temperatureValueOrUpper!=null || temperatureUnits!=null) {
			System.out.println("Warning: Temperature conditions not supported for "+propertyName+". Non-null values ignored.");
		}
		
		// pH condition
		if (endpointKind.equals("Partition") || endpointKind.equals("WaterSolubility")) {
			queryBlock.addpHField(pHMin,pHMax);
		} else if (pHMin!=null || pHMax!=null) {
			System.out.println("Warning: pH conditions not supported for "+propertyName+". Non-null values ignored.");
		}
		
		// Endpoint-specific necessary fields
		if (endpointKind.equals("Partition")) {
			queryBlock.addPartitionCoefficientFields();
		} else if (endpointKind.equals("WaterSolubility")) {
			queryBlock.addWaterSolubilityFields();
		}
		
		PropertyBlock propertyBlock = new PropertyBlock(0,"property",queryBlock);
		QueryData query = new QueryData(propertyBlock);
		return query;
	}
	
	/**
	 * Runs the API query described by a QueryData object
	 * @param query	API query to run
	 * @return		Results as a vector of ResultsData objects
	 */
	private Vector<ResultsData> runQuery(QueryData query) {
		Vector<ResultsData> results = new Vector<ResultsData>();
		Unirest.setTimeouts(0, 0);
		String bodyString = gson.toJson(query);
		try {	
			HttpResponse<String> response = Unirest.post("https://www.echemportal.org/echemportal/api/property-search")
			  .header("Content-Type", "application/json")
			  .header("Accept", "application/json")
			  .body(bodyString)
			  .asString();
			Thread.sleep(500);
			String json=response.getBody();
			ResultsData data=gson.fromJson(json, ResultsData.class);
			results.add(data);
			
			int totalResults = data.pageInfo.totalElements;
			System.out.println("Found "+totalResults+" results. Downloading...");
			int offset = 100;
			while (offset < totalResults) {
				query.updateOffset(offset);
				bodyString = gson.toJson(query);
				response = Unirest.post("https://www.echemportal.org/echemportal/api/property-search")
						  .header("Content-Type", "application/json")
						  .header("Accept", "application/json")
						  .body(bodyString)
						  .asString();
				Thread.sleep(500);
				json = response.getBody();
				data = gson.fromJson(json, ResultsData.class);
				results.add(data);
				offset += 100;
			}
			return results;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		QueryHandler qh = new QueryHandler();
		QueryData query = generateQueryData(ExperimentalConstants.strHenrysLawConstant,2,
				"0","100",ExperimentalConstants.str_atm_m3_mol,
				"GT_EQUALS","0",ExperimentalConstants.str_pa,
				"GT_EQUALS","0",ExperimentalConstants.str_K,
				null,null);
		System.out.println(qh.prettyGson.toJson(query));
		Vector<ResultsData> results = qh.runQuery(query);
		System.out.println("Done! Writing to JSON...");
		String filePath = "Data" + File.separator + "Experimental" + File.separator + ExperimentalConstants.strSourceEChem + File.separator +
				ExperimentalConstants.strSourceEChem + " API Records.json";
		try {

			File file = new File(filePath);
			file.getParentFile().mkdirs();

			FileWriter fw = new FileWriter(file);
			fw.write(qh.prettyGson.toJson(results));
			fw.flush();
			fw.close();
			
			System.out.println("Done!");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
