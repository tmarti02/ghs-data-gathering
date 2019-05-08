package gov.epa.ghs_data_gathering.GetData;

import java.io.FileReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.ghs_data_gathering.GetData.IUCLID_Document.Part1;
import gov.epa.ghs_data_gathering.GetData.IUCLID_Document.Part2.AdministrativeData;
import gov.epa.ghs_data_gathering.GetData.IUCLID_Document.Part2.DataSource;
import gov.epa.ghs_data_gathering.GetData.IUCLID_Document.Part2.MaterialsAndMethods;
import gov.epa.ghs_data_gathering.GetData.IUCLID_Document.Part2.ResultsAndDiscussion;


/**
 * Class that lets you load a document into Java class using single line of code and access objects easier than Gson
 * 
 * @author Todd Martin
 *
 */
public class IUCLID_Document2 {
	public Part1 DossierData;
	public AdministrativeData AdministrativeData;
	public DataSource DataSource;
	public MaterialsAndMethods MaterialsAndMethods;
	public ResultsAndDiscussion ResultsAndDiscussion;
	
	
	/**
	 * Load json file into IUCLID_Document2 object
	 * 
	 * @param filePath
	 * @return
	 */
	public static IUCLID_Document2 loadFromFile(String filePath) {
		
		try {
			
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			
			//Store Java instance from Json string in the file:
			IUCLID_Document2 iud2 = gson.fromJson(new FileReader(filePath), IUCLID_Document2.class);
		
			return iud2;


		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) {
		
		String folder="R:\\0 REACH dossiers\\reach_study_results_iuclid6_09-02-2017\\GetData\\endpoints with phrases\\AcuteToxicityOral";
//		String fileName="dossier_uuid=ECHA-fff5446b-c273-44d9-a77e-db47904a67fc_document_uuid=ECHA-7343acb2-8d3d-4cf3-81fc-784a6fb7fe80.json";
		String fileName="dossier_uuid=ECHA-cb9b5f78-d2cb-45ef-94e7-c695b4f899bb_document_uuid=ECHA-1fc5d5ae-edba-49d4-85cf-632213092a77.json";
		
		String filePath=folder+"/"+fileName;
		IUCLID_Document2 iud2=IUCLID_Document2.loadFromFile(filePath);

		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		String strJSON=gson.toJson(iud2);//convert back to JSON string to see if we have implemented all the needed fields
		System.out.println(strJSON);

		
		//To easily access a field:
		System.out.println(iud2.MaterialsAndMethods.TestMaterials.TestMaterialInformation);
		
		//Alternatively load as generic JsonObject:
		JsonObject jo=loadFromFile2(filePath);
		System.out.println(jo.getAsJsonObject("MaterialsAndMethods").getAsJsonObject("TestMaterials").getAsJsonPrimitive("TestMaterialInformation").getAsString());

		
	}

	/**
	 * Load json file into JsonObject
	 * 
	 * @param filePath
	 * @return
	 */
	public static JsonObject loadFromFile2(String filePath) {
		try {
			Gson gson=new Gson();
			JsonObject jo = gson.fromJson(new FileReader(filePath), JsonObject.class);
			return jo;
		
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
}




