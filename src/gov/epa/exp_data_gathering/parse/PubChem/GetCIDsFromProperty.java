package gov.epa.exp_data_gathering.parse.PubChem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import gov.epa.api.ExperimentalConstants;
import gov.epa.ghs_data_gathering.Utilities.FileUtilities;

/**
* @author TMARTI02
*/
public class GetCIDsFromProperty {

	Gson gson=new Gson();		
	
	//Everything (most of Acute Effects chemidplus table):
//	https://pubchem.ncbi.nlm.nih.gov/rest/pug_view/data/compound/2123/JSON

	//https://pubchem.ncbi.nlm.nih.gov/rest/pug_view/data/compound/2123/JSON?heading=Toxicity+Data

	//	https://pubchem.ncbi.nlm.nih.gov/compound/2123#section=Acute-Effects
	
//	https://pubchem.ncbi.nlm.nih.gov/rest/pug_view/annotations/heading/JSON?heading=Toxicity+Data&page=2&heading_type=Compound
		
	//Guidance on annotations:
//	https://pubchem.ncbi.nlm.nih.gov//docs/pug-view#section=Annotations
	
	//List of headings:
//	https://pubchem.ncbi.nlm.nih.gov/rest/pug/annotations/headings/JSON
	
	//Note: Annotation jsons dont store the name/cas from original reference
	
	void getAnnotationJsons(String heading,String folder) {
		
		//get first page:
		File Folder=new File(folder);
		if(!Folder.exists()) {
			Folder.mkdirs();
		}
		
		int page=1;
		String url="https://pubchem.ncbi.nlm.nih.gov/rest/pug_view/annotations/heading/JSON?heading="+heading.replace(" ", "+")+"&page="+page+"&heading_type=Compound";
		String json=FileUtilities.getText(url);
		writeFile(folder, heading, page, json);
		
		int totalPages=getNumPages(json);
		System.out.println("totalPages="+totalPages);
		
		for (page=2;page<=totalPages;page++) {
		
			System.out.println("getting page "+page);
			
			url="https://pubchem.ncbi.nlm.nih.gov/rest/pug_view/annotations/heading/JSON?heading="+heading.replace(" ", "+")+"&page="+page+"&heading_type=Compound";
			json=FileUtilities.getText(url);
			writeFile(folder, heading, page, json);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
	}


	void writeFile(String folder, String property,int page, String json) {
		
		Gson gson=null;
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues();
		gson = builder.create();
		
		try {
			FileWriter fw=new FileWriter(folder+property+" "+page+".json");
			JsonObject jo=gson.fromJson(json, JsonObject.class);
			fw.write(gson.toJson(jo));
			fw.flush();
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	

	int getNumPages(String json) {
		Gson gson=new Gson();
		try {
			JsonObject jo=gson.fromJson(json, JsonObject.class);
			return jo.get("Annotations").getAsJsonObject().get("TotalPages").getAsInt();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return -1;
		
	}
	
	
	void getCidsFromFile(String filepath,HashSet<Long>cidsAll) {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		
		try {
			JsonObject jo=gson.fromJson(new FileReader(filepath), JsonObject.class);
			
			JsonArray jaAnnotations=jo.get("Annotations").getAsJsonObject().get("Annotation").getAsJsonArray();
			
//			System.out.println(jaAnnotations.size());
			
			for (int i=0;i<jaAnnotations.size();i++) {
				JsonObject joAnnotation=jaAnnotations.get(i).getAsJsonObject();
				
				if (joAnnotation.get("LinkedRecords")==null) continue;
				if (joAnnotation.get("LinkedRecords").getAsJsonObject().get("CID")==null) continue;
				
				JsonArray cids=joAnnotation.get("LinkedRecords").getAsJsonObject().get("CID").getAsJsonArray();
				
				for (int j=0;j<cids.size();j++) {
					long CID=cids.get(j).getAsLong();
					
//					if(CID==241L) {
//						System.out.println(filepath+"\thas 241");
//					}
					
					cidsAll.add(CID);
				}
//				System.out.println(cids+"\t"+cids.size()+"\n");	
			}
			
//			System.out.println(cidsAll.size());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
	}
	
	void getSidsFromFile(String filepath,HashSet<Long>cidsAll) {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		
		try {
			JsonObject jo=gson.fromJson(new FileReader(filepath), JsonObject.class);
			
			JsonArray jaAnnotations=jo.get("Annotations").getAsJsonObject().get("Annotation").getAsJsonArray();
			
//			System.out.println(jaAnnotations.size());
			
			for (int i=0;i<jaAnnotations.size();i++) {
				JsonObject joAnnotation=jaAnnotations.get(i).getAsJsonObject();
				
				String sourceName=joAnnotation.get("SourceName").getAsString();
				
				if(!sourceName.equals("ChemIDplus")) {
					System.out.println(sourceName);
				}
				
				
				if (joAnnotation.get("LinkedRecords")==null) continue;
				if (joAnnotation.get("LinkedRecords").getAsJsonObject().get("SID")==null) continue;
				
				JsonArray sids=joAnnotation.get("LinkedRecords").getAsJsonObject().get("SID").getAsJsonArray();
				
				for (int j=0;j<sids.size();j++) {
					long SID=sids.get(j).getAsLong();
					cidsAll.add(SID);
				}
//				System.out.println(cids+"\t"+cids.size()+"\n");	
			}
			
//			System.out.println(cidsAll.size());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
	}
	HashSet<Long> getCidsFromFolder(String folder) {
		
		File Folder=new File(folder);
		
		HashSet<Long>cidsAll=new HashSet<>();
		
		for (File file:Folder.listFiles()) {
			if (!file.getName().contains(".json")) continue;
//			if (!file.getName().contains("Solubility")) continue;
			getCidsFromFile(file.getAbsolutePath(), cidsAll);
		}

		List<Long>cids=new ArrayList<>();
		for (Long cid:cidsAll) cids.add(cid);
		Collections.sort(cids);
		
//		for(Long cid:cids) {
//			System.out.println(cid);
//		}
		

//		System.out.println(cidsAll.size());
		
//		try {
//			FileWriter fw=new FileWriter(folder+"cids.txt");
//			for(Long cid:cids) fw.write(cid+"\r\n");
//			fw.flush();
//			fw.close();
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
		return cidsAll;
 		
	}
	
	HashSet<Long> getSidsFromFolder(String folder) {
		
		File Folder=new File(folder);
		
		HashSet<Long>sidsAll=new HashSet<>();
		
		for (File file:Folder.listFiles()) {
			if (!file.getName().contains(".json")) continue;
			if (!file.getName().contains("Acute+Effects")) continue;
			getSidsFromFile(file.getAbsolutePath(), sidsAll);
		}

		List<Long>sids=new ArrayList<>();
		for (Long sid:sidsAll) {		
			sids.add(sid);			
		}
		Collections.sort(sids);

//		System.out.println(cidsAll.size());
		
		try {
			FileWriter fw=new FileWriter(folder+"sids.txt");
			for(Long sid:sids) fw.write(sid+"\r\n");
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return sidsAll;
 		
	}
	
	

	void compileCollectionFromFolder(String collection, String folder) {
		
		File Folder=new File(folder);

		try {

			FileWriter fw=new FileWriter(folder+collection+".json");
			for (File file:Folder.listFiles()) {
				
				if (file.getName().equals(collection+".json")) continue;
				if (!file.getName().contains(".json")) continue;
				if (!file.getName().contains(collection+"_")) continue;
				
				try {
					JsonArray ja = gson.fromJson(new FileReader(file), JsonArray.class);
					
					for (int i=0;i<ja.size();i++) {
						JsonObject jo=ja.get(i).getAsJsonObject();
						fw.write(gson.toJson(jo)+"\r\n");
						fw.flush();
					}
					System.out.println(file.getName()+"\t"+ja.size());
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
			}
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		


//		System.out.println(cidsAll.size());
		
 		
	}
	

	HashSet<Long> getToxicityDataOralRatLD50FromFolder(String folder) {
		
		
		File Folder=new File(folder);

		HashSet<Long>cidsAll=new HashSet<>();
		
		try {
			for (File file:Folder.listFiles()) {
				
				if (!file.getName().contains(".json")) continue;
				if (!file.getName().contains("Toxicity Data")) continue;
				
//				System.out.println(file.getName());
				
				JsonObject jo=gson.fromJson(new FileReader(file), JsonObject.class);
				JsonArray jaAnnotations=jo.get("Annotations").getAsJsonObject().get("Annotation").getAsJsonArray();
				
//				System.out.println(jaAnnotations.size());
				
				for (int i=0;i<jaAnnotations.size();i++) {
					JsonObject joAnnotation=jaAnnotations.get(i).getAsJsonObject();
					
					if (joAnnotation.get("LinkedRecords")==null) continue;
					if (joAnnotation.get("LinkedRecords").getAsJsonObject().get("CID")==null) continue;

					
					if (joAnnotation.get("Data")==null) {
						System.out.println("no data");
						continue;
					}
					
					JsonArray jaData=joAnnotation.get("Data").getAsJsonArray();
					boolean haveOralRat=false;
					
					for (int j=0;j<jaData.size();j++) {
						JsonObject joData=jaData.get(j).getAsJsonObject();
						
						JsonObject joValue=joData.get("Value").getAsJsonObject();
						JsonArray jaStringWithMarkup=joValue.get("StringWithMarkup").getAsJsonArray();
						
						for (int k=0;k<jaStringWithMarkup.size();k++) {
							JsonObject joStringWithMarkup=jaStringWithMarkup.get(k).getAsJsonObject();
						
							String string=joStringWithMarkup.get("String").getAsString();
							
							String [] strings=string.split("\n");
							
							for (String str:strings) {
								
								String stringLC=str.toLowerCase();
								
								if(!stringLC.contains("ld") && !stringLC.contains("50")) continue;
								if(!stringLC.contains("rat")) continue;
								if(!stringLC.contains("po") && !stringLC.contains("oral")) continue;
//								System.out.println(k+"\t"+str);
								
								haveOralRat=true;
								break;
								
							}
							if (haveOralRat) break;
						}
						
					}
					
					if(!haveOralRat)continue;
					
					JsonArray cids=joAnnotation.get("LinkedRecords").getAsJsonObject().get("CID").getAsJsonArray();
					
					for (int j=0;j<cids.size();j++) {
						long CID=cids.get(j).getAsLong();
						cidsAll.add(CID);
					}
//					System.out.println(cids+"\t"+cids.size()+"\n");	
				}
			}

			
			return cidsAll;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

		
		
 		
	}

	
	void renameFilesFolder(String folder) {
		File Folder=new File(folder);
		for(File file:Folder.listFiles()) {
			if(file.getName().contains("+")) {
				File fileNew=new File(folder+file.getName().replace("+"," "));
				file.renameTo(fileNew);
			}
		}
		
	}
	
	
	public static void main(String[] args) {
		GetCIDsFromProperty g=new GetCIDsFromProperty();
		
		String folder="data\\experimental\\PubChem_2024_11_27\\json\\physchem\\";
		
//		g.renameFilesFolder(folder);
//		g.getAnnotationJsons("Henry's Law Constant", folder);
//		g.getAnnotationJsons("LogP", folder);
//		g.getAnnotationJsons("Solubility", folder);
//		g.getAnnotationJsons("Vapor Pressure", folder);
//		g.getAnnotationJsons("Melting Point", folder);
//		g.getAnnotationJsons("Boiling Point", folder);
//		g.getAnnotationJsons("Density", folder);
//		g.getAnnotationJsons("Viscosity", folder);
//		g.getAnnotationJsons("Surface Tension", folder);
//		g.getAnnotationJsons("Flash Point", folder);
//		g.getAnnotationJsons("Autoignition Temperature", folder);
//		g.getAnnotationJsons("Vapor Density", folder);

//		HashSet<Long>cidsNew=g.getCidsFromFolder(folder);
//		System.out.println(cidsNew.size());

//		String folder="data\\experimental\\PubChem_2024_11_27\\json\\toxicity\\";
//		g.renameFilesFolder(folder);
//		g.getAnnotationJsons("Toxicity+Data", folder);//		
//		g.getAnnotationJsons("Acute+Effects", folder);
		
//		HashSet<Long>sids=g.getSidsFromFolder(folder);
//		System.out.println(sids.size());

//		String collection ="chemidplus";
//		String collection ="niosh";
//		g.downloadCollectionData(collection, folder,1000);
//		g.compileCollectionFromFolder(collection,folder);
//		
//		g.compareCids(folder);
		
//		compareOldToNew(g, folder);
		
		
	}


	private void compareCids( String folder) {
		
		HashSet<Long> cidsChemidplus=getCidOralRatLD50RecordsChemidplus(folder+"chemidplus.json");
		System.out.println("Cids in chemidplus:"+cidsChemidplus.size());
		
//		HashSet<Long> cidsSmallSource=getToxicityDataOralRatLD50FromFolder(folder);
//		System.out.println("Cids in Toxicity Data:"+cidsSmallSource.size());

		HashSet<Long> cidsSmallSource=getCidOralRatLD50RecordsNiosh(folder+"niosh.json");
		System.out.println("Cids in niosh:"+cidsSmallSource.size());
		
		
		int countNew=0;
		for (Long cidToxicityData:cidsSmallSource) {
			if(!cidsChemidplus.contains(cidToxicityData))  {
				countNew++;
			}
		}
		System.out.println("New cids in small source:"+countNew);
	}

	class RecordChemidplus {
		String cid;
		String sid;
		String sourceid;
		String organism;
		String testtype;
		String route;
		String dose;
		String effect;
		String reference;
	}
	
	class RecordNiosh {
		
		Long [] cids;
		String [] synonyms;
		String measurement;
		String updatedate;
		String system;
		String routeorganism;
		String dose;
		String effect;
		String ref;

	}

	/**
	 * Dont specify the sid and just download by start and stop
	 * 
	 * @param folder
	 * @param delay
	 */
	void downloadCollectionData(String collection, String folder,long delay) { 
		
		long count=10000;
		long start=1;
		
		try {
			
			while (true) {
			
				File out=new File(folder+collection+"_"+start+"-"+(start+count-1)+".json");

				if(out.exists()) {
					System.out.println(out.getName()+" exists");
					start+=count;					
					continue;
				}
				
				String url="https://pubchem.ncbi.nlm.nih.gov/sdq/sdqagent.cgi?infmt=json&outfmt=json&"
						+ "query={%22download%22:%22*%22,%22collection%22:%22"+collection+"%22,"
						+ "%22order%22:[%22relevancescore,desc%22],%22start%22:"+start+",%22limit%22:"+count+"}";
				
				System.out.println(url);
				
				String json=FileUtilities.getText(url);
//				System.out.println(json);
				
				JsonArray ja=gson.fromJson(json,JsonArray.class);
				System.out.println(out.getName()+"\t"+ja.size());
				
				if(ja.size()==0) break;
				
				FileWriter fw=null;
				fw=new FileWriter(out);
				
				fw.write(json);
				fw.flush();
				
//				for (int i=0;i<ja.size();i++) {
//					JsonObject jo=ja.get(i).getAsJsonObject();
//					fw.write(gson.toJson(jo)+"\r\n");
//					fw.flush();
//				}
				fw.close();
				Thread.sleep(delay);
				start+=count;
				
//				if(stop>2500) break;

			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	
	


	private HashSet<Long> getSidsRanChemidPlusRecords(File out) {
		
		HashSet<Long>sids=new HashSet<>();
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(out));
			
			while (true) {
				String line=br.readLine();
				if(line==null) break;
				RecordChemidplus rc=gson.fromJson(line,RecordChemidplus.class);
				sids.add(Long.parseLong(rc.sid));
				
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return sids;
		
	}
	
	private HashSet<Long> getCidOralRatLD50RecordsChemidplus(String filepath) {
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
		
			int count=0;
			
			HashSet<Long>cids=new HashSet<>();
			
			while (true) {
				String line=br.readLine();
				if(line==null) break;
				RecordChemidplus rc=gson.fromJson(line,RecordChemidplus.class);
				
				if(rc.organism.contentEquals("rat") && rc.testtype.contentEquals("LD50") && rc.route.contentEquals("oral")) {
					count++;
					
					if(rc.cid==null) continue;
					
					cids.add(Long.parseLong(rc.cid));
				}
				
			}
			
//			System.out.println(count);
			
			
			return cids;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	private HashSet<Long> getCidOralRatLD50RecordsNiosh(String filepath) {
		
		Gson gsonPretty = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();

		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
		
			int count=0;
			
			HashSet<Long>cids=new HashSet<>();
			
			while (true) {
				String line=br.readLine();
				if(line==null) break;
				RecordNiosh rn=gson.fromJson(line,RecordNiosh.class);
				
				if(!rn.routeorganism.contains("oral/rat")) continue; 
				if(!rn.measurement.contains("Acute Toxicity Data")) continue;
				
//				System.out.println(gsonPretty.toJson(rn));
				
				for (Long cid:rn.cids) {
					cids.add(cid);	
				}
				
			}
			
//			System.out.println(count);
			
			
			return cids;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}




	private static void compareOldToNew(GetCIDsFromProperty g, String folder) {
		HashSet<Long>cidsNew=g.getCidsFromFolder(folder);
		HashSet<Long> cidsOld = RecordPubChem.getCidsInDatabase2("Pubchem_2024_03_20");// old ones from 2020
		
		HashSet<Long> cidsOnlyInOld=new HashSet<>();
		HashSet<Long> cidsOnlyInNew=new HashSet<>();
		
		//In old not in new:
		for (Long cid:cidsOld) {
			if(!cidsNew.contains(cid)) {
				cidsOnlyInOld.add(cid);
			}
		}
		
		for (Long cid:cidsNew) {
			if(!cidsOld.contains(cid)) {
				cidsOnlyInNew.add(cid);
			}
		}
		
		System.out.println("New ="+cidsNew.size());
		System.out.println("Old = "+cidsOld.size());
		System.out.println("Only in old = "+cidsOnlyInOld.size());
		System.out.println("Only in new = "+cidsOnlyInNew.size());
	}

}
