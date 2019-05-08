package gov.epa.ghs_data_gathering.GetData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import gov.epa.ghs_data_gathering.GetData.IUCLID_Document.Part1;
import gov.epa.ghs_data_gathering.GetData.IUCLID_Document.Part2;
import gov.epa.ghs_data_gathering.GetData.IUCLID_Document.Part2.AdministrativeData;
import gov.epa.ghs_data_gathering.GetData.IUCLID_Document.Part2.DataSource;
import gov.epa.ghs_data_gathering.GetData.IUCLID_Document.Part2.MaterialsAndMethods;
import gov.epa.ghs_data_gathering.GetData.IUCLID_Document.Part2.ResultsAndDiscussion;


/**
 * Class to query IUCLID database server, download json records, and convert to revised json records with phrases and substance info
 * 
 * To Start the database server:
 * 1. Download "reach_study_results_iuclid6_09-02-2017.zip" from https://iuclid6.echa.europa.eu/rsr-iuclid6
 * 2. Unzip to "reach_study_results_iuclid6_09-02-2017" folder
 * 3. Start database server by running "iuclid6.exe" inside the "reach_study_results_iuclid6_09-02-2017" folder
 *
 * 
 * To get the records:
 * 
 * Assume startURL=http://localhost:8080/iuclid6-ext/api/ext/v1 (port number may vary)
 * 
 * 1. 	Download list of dossiers:
 *		Call getDossierNumberJSON_File
 *		url = startURL/query/iuclid6/byType?doc.type=DOSSIER&l=9999999
 * 
 * 2.	Get list of document numbers for each dossier number:   
 *		Call downloadDocumentIDs
 *		url = startURL/dossier/dossier_uuid/subject/documents?l=999999
 *
 * 3.	Download documents for doc_type:	
 * 		Call downloadDocumentsForEndpoint
 * 		url = startURL/dossier/dossier_uuid/subject/document/doc_type/document_uuid
 * 
 * 4. 	Download phrases
 * 		Call downloadPhrases
 * 		url = startURL/definition/phrases";
 * 
 * 		Load phrases and store as hashtable:
 * 		Hashtable<String,String>htPhrases=getPhrasesAsHashtable();
 * 
 * 5.	Go through documents for a certain endpoint and created revised documents:
 * 		Call goThroughFolder which in turn calls createRevisedDocument for each document
 * 
 * 		Get TestMaterialInformation
 * 			updated using data from url = startURL/dossier/dossier_uuid/TEST_MATERIAL_INFORMATION/tmi_uuid
 * 	 		
 * 		Get dossier chemical info:
 * 			Get the ReferenceSubstance_uuid from url=startURL/dossier/dossier_uuid/subject	
 * 			Get the CAS/EC Number/name from the reference substance: url=startURL/dossier/document_uuid/REFERENCE_SUBSTANCE/ReferenceSubstance_uuid	
 * 		
 *  * @author Todd Martin
 *
 */
public class ECHA_IUCLID {

	
	String startURL="http://localhost:8080/iuclid6-ext/api/ext/v1";
	boolean suppressLogging=true;
	
	
	public ECHA_IUCLID () {
		if (suppressLogging) {
			turnoffLogging();
		}
	}
	
	public static void turnoffLogging () {
		Set<String> loggers = new HashSet<>(Arrays.asList("org.apache.http", "groovyx.net.http"));
		for(String log:loggers) { 
			Logger logger = (Logger)LoggerFactory.getLogger(log);
			logger.setLevel(Level.INFO);
			logger.setAdditive(false);
		} //end
	}
	
	class Phrase {
		
		String code;
		String text;
		String description;
		boolean open;
	}
	
	class Records {
		int limit;
		int totalCount;
		URIRecord [] results;
	}
	
	class URIRecord {
		String uri;
		String representation;
	}
	
	
	String runRequest(String url,String accept) {
		
		try {
			HttpGet request = new HttpGet(url);
			request.addHeader("IUCLID6-USER", "SuperUser");
			request.addHeader("IUCLID6-PASS", "root");
			request.addHeader("Accept", accept);

			HttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
			org.apache.http.HttpResponse response = client.execute(request);
			
			if (response.getLastHeader("Location")!=null) {//run redirect:
				String location = response.getLastHeader("Location").getValue();
//				System.out.println("***"+location);
				request.setURI(new URI(location));
				
//				System.out.println("***"+location);
				
				response = client.execute(request);
			}

			BufferedReader rd = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));

			ObjectMapper mapper = new ObjectMapper();
			com.fasterxml.jackson.databind.JsonNode json = mapper.readTree(rd);
	        
			String results=mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
			
			return results;
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "error";
		
	}

	
	String runRequest(String url) {
		return runRequest(url,"application/vnd.iuclid6.ext+json; type=iuclid6.Document");
	}
	
	
	/**
	 * Download the document info for a given dossier id
	 * 
	 * @param dossier_uuid
	 */
	void downloadDocumentsForDossierID(String folder,String dossier_uuid) {

		
		try {
			
			String url=startURL+"/dossier/"+dossier_uuid+"/subject/documents?l=999999";
						
			System.out.println(url);
			
	        String filepath=folder+"/document ids/"+dossier_uuid+".json";
	        
	        File file=new File(filepath);
//	        if (file.exists()) return;
			
			long t1=System.currentTimeMillis();

			String results=runRequest(url);
	        FileWriter fw=new FileWriter(filepath);
	        fw.write(results+"\r\n");
	        fw.close();
	        
	        long t2=System.currentTimeMillis();
	        
	        System.out.println((t2-t1)/1000.0+" seconds");
			

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	/**
	 * Creates hashtable with key=code, value=phrase
	 *  
	 * @return
	 */
	Hashtable <String,String> getPhrasesAsHashtable (String folder,String filename) {
		long t1=System.currentTimeMillis();
		File jsonFile = new File(folder + "/"+filename);
		
		try {
			Gson gson = new Gson();
			Phrase [] phrases = gson.fromJson(new FileReader(jsonFile), Phrase[].class);
			Hashtable <String,String>htPhrases=new Hashtable<String,String>();
			
			for (int i=0;i<phrases.length;i++) {
				Phrase p=phrases[i];
				htPhrases.put(p.code, p.text);
//				System.out.println(p.code+"\t"+p.text);
			}
			
	        long t2=System.currentTimeMillis();
//	        System.out.println((t2-t1)/1000.0+" seconds");
			return htPhrases;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	
	
	String [] loadResultsFromJSON_File (String folder,String filename) {
		long t1=System.currentTimeMillis();
		File jsonFile = new File(folder + "/"+filename);
		
//		System.out.println(jsonFile.getAbsolutePath());
		
		Gson gson = new Gson();
		
		try {
			Records records = gson.fromJson(new FileReader(jsonFile), Records.class);
			
//			System.out.println(records.results.length);
			
			String [] ids=new String [records.results.length];
			
			for (int i=0;i<records.results.length;i++) {
				ids[i]=records.results[i].uri;
//				System.out.println(ids[i]);
			}
			
	        long t2=System.currentTimeMillis();
//	        System.out.println((t2-t1)/1000.0+" seconds");

			return ids;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}
	
	
	void setPartsOfDocumentSeparately(String folder, String filename) {

		File jsonFile = new File(folder + "/" + filename);

		// System.out.println(jsonFile.getAbsolutePath());

		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();

		try {

			Object[] data = gson.fromJson(new FileReader(jsonFile), Object[].class);

			String strData1 = gson.toJson(data[0]);
			String strData2 = gson.toJson(data[1]);

			JsonObject jo = gson.fromJson(strData2, JsonObject.class);

			 JsonObject adminData=jo.get("AdministrativeData").getAsJsonObject();
			 String strAdminData=gson.toJson(adminData);
			// System.out.println(adminData);
			 AdministrativeData ad=gson.fromJson(strAdminData, AdministrativeData.class);
			 System.out.println(gson.toJson(ad));
			// System.out.println(strAdminData);
			
			 System.out.println(ad.Endpoint.code);

			// *********************************************************************************
			 JsonObject dataSource=jo.get("DataSource").getAsJsonObject();
			 String strDataSource=gson.toJson(dataSource);
			// System.out.println(adminData);
			 DataSource ds=gson.fromJson(strDataSource, DataSource.class);
			 System.out.println(gson.toJson(ds));

			// *********************************************************************************
			// JsonObject
			 JsonObject materialsAndMethods=jo.get("MaterialsAndMethods").getAsJsonObject();
			 String strMaterialsAndMethods=gson.toJson(materialsAndMethods);
			// System.out.println(adminData);
			 MaterialsAndMethods mam=gson.fromJson(strMaterialsAndMethods,
			 MaterialsAndMethods.class);
			 System.out.println(gson.toJson(mam));


			// *********************************************************************************
			 JsonObject resultsAndDiscussion=jo.get("ResultsAndDiscussion").getAsJsonObject();
			 String strResultsAndDiscussion=gson.toJson(resultsAndDiscussion);
			// System.out.println(adminData);
			 ResultsAndDiscussion rad=gson.fromJson(strResultsAndDiscussion,
			 ResultsAndDiscussion.class);
			 System.out.println(gson.toJson(rad));

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
//	/**
//	 * Takes document json and converts the codes into phrases. It also moves the metadata in first
//	 * part of document to a FileData section instead of having 2 member array which is awkward
//	 * 
//	 * @param folder
//	 * @param filename
//	 * @param htPhrases
//	 * @return
//	 */
//	String createRevisedDocument (String folder,String filename,Hashtable<String,String>htPhrases) {
//		File jsonFile = new File(folder + "/"+filename);
//		
////		System.out.println(jsonFile.getAbsolutePath());
//		
//		GsonBuilder builder = new GsonBuilder();
//		builder.setPrettyPrinting();
//		Gson gson = builder.create();
//
//		try {
//			
//			Object[] data = gson.fromJson(new FileReader(jsonFile), Object[].class);
//
//			String strData1=gson.toJson(data[0]);//convert to JSON string
//			String strData2=gson.toJson(data[1]);//convert to JSON string
//			
//			JsonObject jo = gson.fromJson(strData2, JsonObject.class);//convert part2 back to a JSON object
//			LoopThrough(jo, htPhrases);//convert codes to phrases (codes are overwritten)
//			String strData2_with_phrases=gson.toJson(jo);//convert back to a JSON string
////			System.out.println(strData2_with_phrases);
//
//			Part1 part1 = gson.fromJson(strData1,Part1.class);//convert to instance of part1 class
//			Part2 part2 = gson.fromJson(strData2_with_phrases,Part2.class);//convert to instance of part2 class
//			
//			IUCLID_Document2 iud2=new IUCLID_Document2();//create a new document2 instance
//			iud2.FileData=part1;//store part1 as FileData object
//
//			//store rest of blocks in document2 from part2 object:
//			iud2.AdministrativeData=part2.AdministrativeData;
//			iud2.DataSource=part2.DataSource;
//			iud2.MaterialsAndMethods=part2.MaterialsAndMethods;
//			iud2.ResultsAndDiscussion=part2.ResultsAndDiscussion;
//
//			String strIUD2=gson.toJson(iud2);//convert new document to JSON formatted string
////			System.out.println(strIUD2);//output to look at it
//			return strIUD2;
//			
////			IUCLID_Document iud=new IUCLID_Document();
////			iud.objects[0]=part1;
////			iud.objects[1]=part2;
////			System.out.println("Part1:"+gson.toJson(part1));
////			System.out.println("Part2:"+gson.toJson(part2));
////			System.out.println("Both:"+gson.toJson(iud.objects));
//
//			
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			return ex.getMessage();
//		}
//		
//		
//		//TODO- for some reason only stores first data block. Might need to just iterate through generic JSON object manually!
//		
//		
//	}
	
	void addSection (String sectionName,JsonObject joAll,JsonObject joPart2) {
		if (joPart2.get(sectionName)!=null)
			joAll.add(sectionName, joPart2.get(sectionName).getAsJsonObject());
	}
	/**
	 * Takes document json and converts the codes into phrases. It also moves the metadata in first
	 * part of document to a FileData section instead of having 2 member array which is awkward
	 * 
	 * @param folder
	 * @param filename
	 * @param htPhrases
	 * @return
	 */
	String createRevisedDocument (String folder,String dossier_uuid,String document_uuid,Hashtable<String,String>htPhrases) {

		
		String filename="dossier_uuid="+dossier_uuid+"_document_uuid="+document_uuid+".json";
		File jsonFile = new File(folder + "/"+filename);
		
//		System.out.println(jsonFile.getAbsolutePath());
		
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();

		try {
			
			JsonArray ja = gson.fromJson(new FileReader(jsonFile), JsonArray.class);
			
			JsonObject joPart1=ja.get(0).getAsJsonObject();
			JsonObject joPart2=ja.get(1).getAsJsonObject();
			
			//Get name/cas/ec number for test material:
			getTestMaterialInformation(dossier_uuid, gson, joPart2);
			
			JsonObject joReferenceSubstance=getDossierChemicalInformation(dossier_uuid, gson, joPart2);
			
			if (joReferenceSubstance!=null) {
				joPart1.add("ReferenceSubstance", joReferenceSubstance);
			}
			

			//convert codes to phrases (codes are overwritten)
			LoopThrough(joPart2, htPhrases);
			
			//Create a new json object with the first section moved to a FileData section
			//So that can be read into IUCLID_Document2 class in one line
			JsonObject joAll=new JsonObject();
			joAll.add("DossierData", joPart1);
			this.addSection("AdministrativeData", joAll, joPart2);
			this.addSection("DataSource", joAll, joPart2);
			this.addSection("MaterialsAndMethods", joAll, joPart2);
			this.addSection("ResultsAndDiscussion", joAll, joPart2);
			String strAll=gson.toJson(joAll);//convert to JSON string
//			System.out.println(strAll);

			return strAll;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return ex.getMessage();
		}
		
		
		//TODO- for some reason only stores first data block. Might need to just iterate through generic JSON object manually!
		
		
	}
	
	void oldCode() {
//		String strData2=gson.toJson(joPart2);//convert to JSON string
//		System.out.println(strData2);
		
//		Object[] data = gson.fromJson(new FileReader(jsonFile), Object[].class);
//
//		String strData1=gson.toJson(data[0]);//convert to JSON string
//		String strData2=gson.toJson(data[1]);//convert to JSON string
//		
//		JsonObject jo = gson.fromJson(strData2, JsonObject.class);//convert part2 back to a JSON object
//		LoopThrough(jo, htPhrases);//convert codes to phrases (codes are overwritten)
//		String strData2_with_phrases=gson.toJson(jo);//convert back to a JSON string
////		System.out.println(strData2_with_phrases);
//		
//		Part1 part1 = gson.fromJson(strData1,Part1.class);//convert to instance of part1 class
//		Part2 part2 = gson.fromJson(strData2_with_phrases,Part2.class);//convert to instance of part2 class
//		
//		IUCLID_Document2 iud2=new IUCLID_Document2();//create a new document2 instance
//		iud2.FileData=part1;//store part1 as FileData object
//
//		//store rest of blocks in document2 from part2 object:
//		iud2.AdministrativeData=part2.AdministrativeData;
//		iud2.DataSource=part2.DataSource;
//		iud2.MaterialsAndMethods=part2.MaterialsAndMethods;
//		iud2.ResultsAndDiscussion=part2.ResultsAndDiscussion;
//
//		String strIUD2=gson.toJson(iud2);//convert new document to JSON formatted string
////		System.out.println(strIUD2);//output to look at it

//		return strIUD2;

	}
	

	private void getTestMaterialInformation(String dossier_uuid, Gson gson, JsonObject joPart2) {
		
		if (joPart2.getAsJsonObject("MaterialsAndMethods")==null) return;
		if (joPart2.getAsJsonObject("MaterialsAndMethods").getAsJsonObject("TestMaterials")==null) return;

		
		JsonObject joTestMaterials=joPart2.getAsJsonObject("MaterialsAndMethods").getAsJsonObject("TestMaterials");
		
		String tmi_uuid=joTestMaterials.getAsJsonPrimitive("TestMaterialInformation").getAsString();
		
		tmi_uuid=tmi_uuid.substring(0,tmi_uuid.indexOf("/"));

		String url=startURL+"/dossier/"+dossier_uuid+"/TEST_MATERIAL_INFORMATION/"+tmi_uuid;
		String strJSON_TMI=this.runRequest(url);
		//				System.out.println(url);
//		System.out.println(strJSON_TMI);

		JsonArray jaTMI = gson.fromJson(strJSON_TMI,JsonArray.class);
		JsonObject joTMI=jaTMI.get(0).getAsJsonObject();
		
		String Name=joTMI.get("name").getAsString();
		
		Name=Name.replace("\n", "").replace("\r", "").trim();//kill carriage returns
		
		//TODO separate Name field into separate arrays if have ";" and "/" separating values
		//Need advanced code to properly parse all the possibilities

		
		//Overwrite TestMaterialInformation field with name (has CAS,EC Number, and name separated by slashes)
		joPart2.getAsJsonObject("MaterialsAndMethods").getAsJsonObject("TestMaterials").addProperty("TestMaterialInformation", Name);

//		if (Name.indexOf(";")==-1) {
//			return;
//		}

//		System.out.println(Name);
		
		JsonObject joTMI2=jaTMI.get(1).getAsJsonObject();

		if (joTMI2.getAsJsonObject("Composition")!=null) {
			
			if (joTMI2.getAsJsonObject("Composition").getAsJsonArray("CompositionList") !=null) {
				
				JsonArray jaCompositionList=joTMI2.getAsJsonObject("Composition").getAsJsonArray("CompositionList");
				
				
				JsonArray jaTestMaterials=new JsonArray();
				
				joTestMaterials.add("ReferenceSubstances",jaTestMaterials);
				
				for (int i=0;i<jaCompositionList.size();i++) {
					
					String ReferenceSubstance_uuid=jaCompositionList.get(i).getAsJsonObject().getAsJsonPrimitive("ReferenceSubstance").getAsString();
					
					ReferenceSubstance_uuid=ReferenceSubstance_uuid.substring(0, ReferenceSubstance_uuid.indexOf("/"));
					
//					System.out.println("i="+i);
					JsonObject joRSI=getReferenceSubstanceInfo(dossier_uuid, gson, ReferenceSubstance_uuid);
					jaTestMaterials.add(joRSI);
				}
			}
		}
		
	}
	
	private JsonObject getDossierChemicalInformation(String dossier_uuid, Gson gson, JsonObject joPart2) {

//		System.out.println("\n"+dossier_uuid);
		
		//Find the ReferenceSubstance UUID from the result json:
		String url=startURL+"/dossier/"+dossier_uuid+"/subject";
		String strSubject=this.runRequest(url);
		//		System.out.println(strSubject);
		JsonArray ja = gson.fromJson(strSubject,JsonArray.class);//convert to instance of part1 class

		if (ja.get(1).getAsJsonObject().getAsJsonObject("ReferenceSubstance")==null) {
			return null;
		}

		String ReferenceSubstance_uuid=ja.get(1).getAsJsonObject().getAsJsonObject("ReferenceSubstance").getAsJsonPrimitive("ReferenceSubstance").getAsString();
		ReferenceSubstance_uuid=ReferenceSubstance_uuid.substring(0, ReferenceSubstance_uuid.indexOf("/"));
		
		return getReferenceSubstanceInfo(dossier_uuid, gson, ReferenceSubstance_uuid);

	}

	private JsonObject getReferenceSubstanceInfo(String dossier_uuid, Gson gson, String ReferenceSubstance_uuid) {
/**
	 It looks like IUCLID has bad algorithm for converting the Name field with /'s into arrays of CAS,ECNumber, and IUPACName
	 Or that the Name field is inconsistently filled in when there's multiple substances (i.e. has ";")
	 
  "TestMaterialInformation": "ff12db84-4ae9-3185-83ef-5e884068375f/ECHA-022326db-c64a-4839-82b9-679c6675da8f",
  "Name": "28182-81-2 / 28182-81-2; 931-274-8 / 931-274-8",
  "ReferenceSubstances": [
    {
      "IUPACName": "28182-81-2",
      "CAS": "28182-81-2"
    },
    {
      "EC_Number": "931-274-8",
      "IUPACName": "931-274-8"
    }
  ]
}

TODO- implement our algorithm to parse Name field into ReferenceSubstances array

 */
		
		JsonObject joRSI=new JsonObject();
		
		String url;
		//		System.out.println(ReferenceSubstance_uuid);

		//Get the ReferenceSubstance info:
		url=startURL+"/dossier/"+dossier_uuid+"/REFERENCE_SUBSTANCE/"+ReferenceSubstance_uuid;
		String strReferenceSubstance=this.runRequest(url);
//				System.out.println(strReferenceSubstance);

		JsonArray ja2 = gson.fromJson(strReferenceSubstance,JsonArray.class);//convert string to JsonArray


		if (ja2.get(1).getAsJsonObject().getAsJsonObject("Inventory")!=null) {
			if (ja2.get(1).getAsJsonObject().getAsJsonObject("Inventory").getAsJsonArray("InventoryEntry")!=null) {
				JsonArray jaEC_Numbers=new JsonArray();
				JsonArray jaInventoryEntry=ja2.get(1).getAsJsonObject().getAsJsonObject("Inventory").getAsJsonArray("InventoryEntry");

				
				if (jaInventoryEntry.size()>1) {
					System.out.println(dossier_uuid+"\tInventoryEntry size ="+jaInventoryEntry.size());
				}
				
				//For now assume EC_Number is not an array- I couldnt find any cases where it was an array- and the CAS number wasnt an array
				String EC_Number=jaInventoryEntry.get(0).getAsString();
				EC_Number=EC_Number.substring(0, EC_Number.indexOf("@"));
				joRSI.addProperty("EC_Number", EC_Number);
				
//				for (int i=0;i<jaInventoryEntry.size();i++) {
//					String EC_Number=jaInventoryEntry.get(i).getAsString();
//					EC_Number=EC_Number.substring(0, EC_Number.indexOf("@"));
//
//					jaEC_Numbers.add(EC_Number);
////					System.out.println(i+"\tEC_Number\t"+EC_Number);
//				}
//				joRSI.add("EC_Numbers", jaEC_Numbers);
				
			}
			
		}

		if (ja2.get(1).getAsJsonObject().getAsJsonObject("ReferenceSubstanceInfo")!=null) {

			JsonObject joReferenceSubstanceInfo=ja2.get(1).getAsJsonObject().getAsJsonObject("ReferenceSubstanceInfo");
			
			if (joReferenceSubstanceInfo.getAsJsonPrimitive("IupacName")!=null) {
				String IUPACName=joReferenceSubstanceInfo.getAsJsonPrimitive("IupacName").getAsString().replace("\n","").replace("\r", "").trim();
				joRSI.addProperty("IUPACName", IUPACName);
//				System.out.println("IUPACName\t"+IUPACName);
			}

			if (joReferenceSubstanceInfo.getAsJsonArray("Synonyms")!=null) {
				JsonArray jaSynonyms2=new JsonArray();
				JsonArray jaSynonyms=joReferenceSubstanceInfo.getAsJsonArray("Synonyms");
				for (int i=0;i<jaSynonyms.size();i++) {
					String Synonym=jaSynonyms.get(i).getAsJsonObject().getAsJsonPrimitive("Name").getAsString().replace("\n","").replace("\r", "").trim();
//					System.out.println(i+"\tSynonym\t"+Synonym);
					jaSynonyms2.add(Synonym);
				}
				
				joRSI.add("Synonyms", jaSynonyms2);
				
			}

			if (joReferenceSubstanceInfo.getAsJsonObject("CASInfo")!=null) {
				String CASNumber=joReferenceSubstanceInfo.getAsJsonObject("CASInfo").getAsJsonPrimitive("CASNumber").getAsString();
				joRSI.addProperty("CAS", CASNumber);
//				System.out.println("CASNumber\t"+CASNumber);
			}
		}
		
		if (ja2.get(1).getAsJsonObject().getAsJsonObject("MolecularStructuralInfo")!=null) {
			
			JsonObject joMolecularStructuralInfo=ja2.get(1).getAsJsonObject().getAsJsonObject("MolecularStructuralInfo");
			
			if (joMolecularStructuralInfo.getAsJsonPrimitive("MolecularFormula")!=null) {
				String molecularFormula=joMolecularStructuralInfo.getAsJsonPrimitive("MolecularFormula").getAsString().replace("\n","").replace("\r", "").trim();
				joRSI.addProperty("MolecularFormula", molecularFormula);
			}
			
			
			if (joMolecularStructuralInfo.getAsJsonPrimitive("SmilesNotation")!=null) {
				String smilesNotation=joMolecularStructuralInfo.getAsJsonPrimitive("SmilesNotation").getAsString().replace("\n","").replace("\r", "").trim();
				joRSI.addProperty("SmilesNotation", smilesNotation);
			}
			
			if (joMolecularStructuralInfo.getAsJsonPrimitive("InChl")!=null) {
				String InChl=joMolecularStructuralInfo.getAsJsonPrimitive("InChl").getAsString().replace("\n","").replace("\r", "").trim();
				joRSI.addProperty("InChl", InChl);
			}

			
			
			if (joMolecularStructuralInfo.getAsJsonObject("MolecularWeightRange")!=null) {
				JsonObject joMWR=joMolecularStructuralInfo.getAsJsonObject("MolecularWeightRange");
				joRSI.add("MolecularWeightRange", joMWR);
			}
			
			//TODO- do we need what's in  "StructuralFormula" : "ECHA-fc5a7aaf-e735-4313-ac13-5d0509a544a3/ECHA-00023031-faa6-41b4-b2ea-25b2f8a83b97"
			
		}
		
//		String strRSI=gson.toJson(joRSI);
//		System.out.println(strRSI);
		
		return joRSI;

		//TODO store all the objects in a new chemical info JsonObject  
	}
	
	
//	void LoopThrough(JsonObject jo) {
//		
//		for (Entry<String, JsonElement> entry : jo.entrySet()) {
//			JsonElement je=entry.getValue();
//			
//			if (je.isJsonObject()) {
//				System.out.println(entry.getKey());
//				LoopThrough(je.getAsJsonObject());	
//			} else if (je.isJsonArray()) {
//				System.out.println(entry.getKey()+"\tarray");
//				
//				JsonArray ja=je.getAsJsonArray();
//				
//				for (int i=0;i<ja.size();i++) {
//					JsonElement jei=ja.get(i);
//					
//					if (jei.isJsonObject()) {
//						LoopThrough(jei.getAsJsonObject());
//					} 
//				}
// 				
//			} else if (je.isJsonPrimitive()) {
//				System.out.println(entry.getKey()+"\tprimitive");
//			}
//		}
//		
//	}
	
	/**
	 * Recursively Loop through the object and convert codes using hashtable
	 * 
	 * @param jo
	 * @param htPhrases
	 */
	void LoopThrough(JsonObject jo,Hashtable<String,String>htPhrases) {
		
		for (Entry<String, JsonElement> entry : jo.entrySet()) {
			JsonElement je=entry.getValue();
			
			if (je.isJsonObject()) {
//				System.out.println(entry.getKey());
				LoopThrough(je.getAsJsonObject(),htPhrases);	
			} else if (je.isJsonArray()) {
//				System.out.println(entry.getKey()+"\tarray");
				
				JsonArray ja=je.getAsJsonArray();
				
				for (int i=0;i<ja.size();i++) {
					JsonElement jei=ja.get(i);
					
					if (jei.isJsonObject()) {
						LoopThrough(jei.getAsJsonObject(),htPhrases);
					} 
				}
 				
			} else if (je.isJsonPrimitive()) {
//				System.out.println(entry.getKey()+"\tprimitive");
				if (entry.getKey().equals("code")) {
					String phrase=htPhrases.get(je.getAsString());
//					System.out.println("here2:"+je.getAsString()+"\t"+phrase);
				
					JsonPrimitive jp=je.getAsJsonPrimitive();
					
//					System.out.println(entry.getKey()+"\t"+phrase);
					jo.addProperty("code", phrase);//overwrite code with the phrase from hashtable
				}
				
			}
		}
		
	}

	

	
	/**
	 * Download list of dossier numbers
	 */
	void getDossierNumberJSON_File(String outputFolderPath,String outputFileName) {
		
		
		try {
			
			long t1=System.currentTimeMillis();
			
			HttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();

			String url = startURL+"/query/iuclid6/byType?doc.type=DOSSIER&l=9999999";
			
			String results=this.runRequest(url);

	        String filepath=outputFolderPath+"/"+outputFileName;
	        
	        FileWriter fw=new FileWriter(filepath);
	        fw.write(results+"\r\n");
	        fw.close();
	        
	        long t2=System.currentTimeMillis();
	        
	        System.out.println((t2-t1)/1000.0+" seconds");
			

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	
	}


	/**
	 * Loop through files to download all the documents for a given endpoint
	 * 
	 * @param documentIDFolder
	 * @param endpoint
	 */
	void downloadDocumentsForEndpointForDossiersInFolder(String documentIDFolder,String endpoint,String destFolder,String esr) {
		
		File folder=new File(documentIDFolder);
		
		File [] files=folder.listFiles();
		
		for (int i=0;i< files.length;i++) {
			File file=files[i];
			String filename=file.getName();
			String dossierID=filename.substring(0, filename.indexOf("."));
			this.downloadDocumentsForEndpointForOneDossier(documentIDFolder, dossierID, endpoint,destFolder,esr);
		}
	}

	
	void downloadDocumentsForEndpointForOneDossier(String documentIDFolder,String dossierID,String endpoint,String destFolderPath,String esr) {
		
//		String jsonFilePath=documentIDFolder+"/"+dossierID;
		
		
		String [] documentIDs=loadResultsFromJSON_File(documentIDFolder,dossierID+".json");

		
		for (int i=0;i<documentIDs.length;i++) {
			
			if (documentIDs[i].indexOf(esr+"."+endpoint)>-1) {
				
//				System.out.println(documentIDs[i]);
				
				String url=documentIDs[i].replace("iuclid6:/","");
				url=url.replace(esr,"document/"+esr);//add "document" to the url path
				
				String documentID=url.substring(url.indexOf(endpoint)+endpoint.length()+1,url.length());
				
//				System.out.println(documentID);
				
				url=startURL+"/dossier/"+url;
				
//				System.out.println("url="+url);
				
				String destFileName="dossier_uuid="+dossierID+"_document_uuid="+documentID+".json";
//				
//				System.out.println(destFileName);
				
				this.downloadDocument(url, destFileName,destFolderPath);
			}
		}
		
	}
	
	void downloadDocument(String url, String destFileName,String destFolderPath) {

		File destFolder = new File(destFolderPath);
		if (!destFolder.exists())
			destFolder.mkdir();

		String filepath=destFolderPath+"/"+destFileName;
		File file=new File(filepath);
		
//		if (file.exists()) return;
		
		try {
			
//			System.out.println(url);
			String results=this.runRequest(url);
			System.out.println(results);
			
	        FileWriter fw=new FileWriter(filepath);
	        fw.write(results+"\r\n");
	        fw.close();

			
//			System.out.println(results);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	/**
	 * Download document numbers for each dossier number
	 */
	void downloadDocumentIDs(String folder,String filename) {
		
		long t1=System.currentTimeMillis();
		
		String [] dossierIDs=loadResultsFromJSON_File(folder,filename);
		
		for (int i=0;i<dossierIDs.length;i++) {
			String dossierID=dossierIDs[i];
			dossierID=dossierID.replace("iuclid6:/","");
			System.out.println(dossierID);
			downloadDocumentsForDossierID(folder,dossierID);
		}
		
        long t2=System.currentTimeMillis();
        System.out.println((t2-t1)/1000.0+" seconds");
		
	}
	
	
	// HTTP GET request
		private void testOralRat()  {


			String dossier_uuid = "ECHA-b3cdee14-6d51-4f84-aa8f-89b9b5db3e66";
			String doc_def = "ENDPOINT_STUDY_RECORD.AcuteToxicityOral";
			String document_uuid = "ECHA-41cd4815-d7e8-47a4-89e6-6aa4b4c1725f";

			String url = startURL+"/dossier/" + dossier_uuid + "/subject/document/"
					+ doc_def + "/" + document_uuid;

			System.out.println(url);
			
			String results=this.runRequest(url);
			System.out.println(results);
			
		}

		/**
		 * Download list of phrases for each code
		 * 
		 */
		void downloadPhrases(String folder,String fileNamePhrases) {
			
			String url=startURL+"/definition/phrases";
			
			try {
				
		        String filepath=folder+"/"+fileNamePhrases;

		        String results=this.runRequest(url,"");
		        FileWriter fw=new FileWriter(filepath);
		        
//		        System.out.println(results);
		        
		        fw.write(results+"\r\n");
		        fw.close();
				
			} catch (Exception ex) {
				
			}
			
		}
		
		void getFilesForUUID(String dossier_uuid,String mainFolder,String phrasesFileName) {
			
			Hashtable<String,String>htPhrases=getPhrasesAsHashtable(mainFolder,phrasesFileName);
			
			String folder=mainFolder+"\\endpoints with codes\\SkinSensitisation";
			
			File file=new File(folder);
			
			File [] files=file.listFiles();
			
			for (int i=0;i<files.length;i++) {
				
				File filei=files[i];
				
				String filename=filei.getName();
				
				if (filename.indexOf(dossier_uuid)>-1) {
					System.out.println(filename);
//					String strIUD=createRevisedDocument(folder, filename, htPhrases);
//					System.out.println(strIUD);

				}

			}
			
			

		}
		
		void goThroughFolder(String srcFolder,Hashtable<String,String>htPhrases,String destFolder) {

			try {
				
				System.out.println(srcFolder+"\r\n");
				
				File Folder=new File(srcFolder);
				File DestFolder=new File(destFolder);

				Path pathDest = Paths.get(destFolder);
				Files.createDirectories(pathDest);

				if (!DestFolder.exists()) DestFolder.mkdir();

				File [] files=Folder.listFiles();

				//			for (int i=0;i<100;i++) {
				for (int i=0;i<files.length;i++) {	
					File filei=files[i];
					String filename=filei.getName();
					
					String dossier_uuid=filename.substring(filename.indexOf("=")+1, filename.indexOf("_document_uuid"));
					String document_uuid=filename.substring(filename.indexOf("document_uuid=")+"document_uuid=".length(),filename.indexOf("."));
					//				System.out.println(dossier_uuid+"\t"+document_uuid);

					File destFile=new File(destFolder+"/"+filename);
					
					if (i%1000==0 ) System.out.println(i);
					
					if (destFile.exists()) continue;
					
					System.out.println("Converting "+filename);

					
					String strNewJSON=createRevisedDocument(srcFolder, dossier_uuid,document_uuid,htPhrases);

					FileWriter fw=new FileWriter(destFile);
					fw.write(strNewJSON+"\r\n");
					fw.close();

				}


			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		
		public static void main(String[] args) {
		// TODO Auto-generated method stub
		ECHA_IUCLID e=new ECHA_IUCLID();

		//default values:
//		e.startURL="http://localhost:8080/iuclid6-ext/api/ext/v1";
//		e.suppressLogging=true;

		//*****************************************************************************************************
		//Test accessing the database server:
//		e.suppressLogging=false;
//		e.testOralRat();

		//*****************************************************************************************************

		String mainFolder="E:\\ECHA REACH Data\\reach_study_results_iuclid6_09-02-2017\\Export";
		String fileNameDossiers="dossiers.json";
		String fileNamePhrases="phrases.json";
		
//		String endpoint="AcuteToxicityOral";
		String endpoint="SkinSensitisation";
//		String endpoint="Carcinogenicity";
//		String endpoint="EyeIrritation";
//		String endpoint="DataTox";
		
		//*****************************************************************************************************
		//Step 1
		//Download list of dossier numbers:
		e.getDossierNumberJSON_File(mainFolder,fileNameDossiers);
		
		//*****************************************************************************************************
		//Step 2:Download document numbers for each dossier number:
//		e.downloadDocumentIDs(mainFolder,fileNameDossiers);

//		Download the document info for a given dossier id (TEST for one dossier id):		
//		e.downloadDocumentsForDossierID(mainFolder,"ECHA-ffa353e3-1a63-4cfe-999a-880520689f7d");
		
		//*****************************************************************************************************
		//Step 3: Download documents for doc_type:
		//Loop through the document numbers and download the documents for a given endpoint:
		String destFolderPath=mainFolder+"/endpoints with codes/"+endpoint;
		
		String esr="ENDPOINT_STUDY_RECORD";
//		String esr="ENDPOINT_SUMMARY";
		
//		e.downloadDocumentsForEndpointForDossiersInFolder(mainFolder+"/document ids", endpoint,destFolderPath,esr);
		
		//Download documents for a specific dossier_uuid (TEST for one dossier id):
		//		String dossier_uuid="ECHA-b9f043a2-6912-4d71-93a2-f3e28b65507f";
		String dossier_uuid="ECHA-b3cdee14-6d51-4f84-aa8f-89b9b5db3e66";
		
		
		
//		e.downloadDocumentsForEndpointForOneDossier(mainFolder+"/document ids", dossier_uuid,endpoint,destFolderPath,esr);

		//*****************************************************************************************************
		//Step 4: download phrases for each code:
//		e.downloadPhrases(mainFolder,fileNamePhrases);
		
		//Load phrases and store as hashtable:
//		Hashtable<String,String>htPhrases=e.getPhrasesAsHashtable(mainFolder,fileNamePhrases);
		
		//*****************************************************************************************************
//		Step 5: go through folder
//		endpoint="SkinSensitisation";
		String srcFolder=mainFolder+"/endpoints with codes/"+endpoint;
		String destFolder=mainFolder+"/endpoints with phrases/"+endpoint;
//		e.goThroughFolder(srcFolder,htPhrases,destFolder);
		//*****************************************************************************************************

		//Revise a single document for testing:
//		String dossier_uuid="ECHA-b3cdee14-6d51-4f84-aa8f-89b9b5db3e66";//benzene
//		String document_uuid="ECHA-a042d61a-7f6a-439a-813d-9027cf2e6560";//skin sens doc
//		String dossier_uuid="ECHA-00db4779-74ba-4e0a-a6ce-c33ed17581e9";
//		String document_uuid="ECHA-f6b2501e-407b-4c9b-a74c-f03c61f47818";
//		String strIUD=e.createRevisedDocument(srcFolder, dossier_uuid,document_uuid,htPhrases);
		
	}
		
		
		
		

}

