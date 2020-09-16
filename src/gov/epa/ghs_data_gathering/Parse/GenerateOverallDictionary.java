package gov.epa.ghs_data_gathering.Parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Vector;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;

import gov.epa.api.AADashboard;
import gov.epa.api.Chemical;
import gov.epa.api.FlatFileRecord;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Utilities.TESTConstants;
import gov.epa.ghs_data_gathering.Utilities.Utilities;


public class GenerateOverallDictionary {

//	void goThroughSource(String folder, String destfolder) {
//
//		Vector<String> lines = this.goThroughJSONFilesForSource(folder);
//
//		try {
//
//			FileWriter fw = new FileWriter(destfolder + "/" + lines.get(0) + ".txt");
//
//			for (int i = 1; i < lines.size(); i++) {
//				fw.write(lines.get(i) + "\r\n");
//			}
//
//			fw.close();
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//	}
	
	

	public void generateDictionaryFileFromFlatFile(String source, String flatFilePath,String destFolder) {

		Vector<String> lines = this.goThroughFlatFileForSource(source,flatFilePath);

		try {

			FileWriter fw = new FileWriter(destFolder + "/" + source + ".txt");

			for (int i = 1; i < lines.size(); i++) {
				fw.write(lines.get(i) + "\r\n");
			}

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	private Vector<String> goThroughFlatFileForSource(String source,String flatFilePath) {

		Vector<String> vec = new Vector<String>();
		Vector<String> vecWithCAS = new Vector<String>();


		ArrayList<String>lines=Utilities.readFileToArray(flatFilePath);
		
		String header=lines.remove(0);
		
		for (String line:lines) {
			FlatFileRecord f=FlatFileRecord.createFlatFileRecord(line);
			this.getScoreData2(source, f, vec, vecWithCAS);
			
		}
		

		// System.out.println(vec.size());
		sortByScoreDescending(vecWithCAS);
		
//		Collections.sort(vecWithCAS);
		vecWithCAS.add(0, source);

		// System.out.println("\r\n"+folder);
		// for (int i=0;i<vec.size();i++) {
		// System.out.println(vec.get(i));
		// }

		return vecWithCAS;	
	}
	
	FlatFileRecord convertLineToFlatFileRecord(String line) {
		FlatFileRecord f=new FlatFileRecord();
		
		String [] vals=line.split("\t");
		f.hazard_name=vals[0];
		f.score=vals[1];
		f.category=vals[2];
		f.hazard_code=vals[3];
		f.CAS=vals[4];
		return f;
	}
	
	/**
	 * Sorts by ScoreName then by score value descending then by category
	 * 
	 * @param vec
	 */
	void sortByScoreDescending(Vector<String>vec) {
		Collections.sort(vec, new Comparator<Object>() {
	        public int compare(Object o1, Object o2) {

	            String str1 = (String)o1;
	            String str2 = (String)o2;
	            
	            FlatFileRecord f1=convertLineToFlatFileRecord(str1);
	            FlatFileRecord f2=convertLineToFlatFileRecord(str2);
	            
	            int sComp = f1.hazard_name.compareTo(f2.hazard_name);

	            if (sComp != 0) {//sort by hazard name ascending
	               return sComp;
	            } 
	            
	            Integer x1 = ScoreRecord.scoreToInt(f1.score);
	            Integer x2 = ScoreRecord.scoreToInt(f2.score);
	            int sComp2=-x1.compareTo(x2);
	            
	            if (sComp2 != 0) {//sort by score descending
		           return sComp2;
		        } 
		            
	            int sComp3 = f1.category.compareTo(f2.category);//sort by category ascending
	            return sComp3;
	            
	            
	    }});
	}
	

//	void goThroughSource2(String folder) {
//
//		Vector<String> lines = this.goThroughJSONFilesForSource(folder);
//
//	}

//	Vector<String> goThroughJSONFilesForSource(String folder) {
//
//		folder = folder.replace("\\", "/");
//
//		String source = folder.replace("AA Dashboard/Data/", "").replace("/json files", "").trim();
//
//		Vector<String> vec = new Vector<String>();
//		Vector<String> vecWithCAS = new Vector<String>();
//
//		System.out.println(source);
//
//		File Folder = new File(folder);
//		File[] files = Folder.listFiles();
//
//		for (int i = 0; i < files.length; i++) {
//			
//			if (source.equals("Japan")) {
//				if (files[i].getName().contains("_2.json") || files[i].getName().contains("_3.json")) {
//					continue;
//				}
//			}
//			
//			if (files[i].getName().indexOf(".json") == -1)
//				continue;
//
//			// if (i%500==0) System.out.println(i);
//
//			try {
//				Gson gson = new Gson();
//
//				Chemical chemical = gson.fromJson(new FileReader(files[i]), Chemical.class);
//
//				if (chemical.CAS == null || chemical.CAS.equals("")) {
////					System.out.println(files[i].getName() + "\tno cas");
//					continue;
//				}
//
//				this.getScoreData(files[i].getName(), chemical, vec, vecWithCAS, source);
//
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
//
//		}
//
//		// System.out.println(vec.size());
//
//		Collections.sort(vecWithCAS);
//
//		vecWithCAS.add(0, source);
//
//		// System.out.println("\r\n"+folder);
//		// for (int i=0;i<vec.size();i++) {
//		// System.out.println(vec.get(i));
//		// }
//
//		return vecWithCAS;
//	}


//	int[] getCountSourcesPerEndpoint(Chemical chemical) {
//
//		int[] counts = new int[Chemical.hazard_names.length];
//
//		for (int j = 0; j < chemical.scores.size(); j++) {
//			Score scorej = chemical.scores.get(j);
//
//			if (scorej.records.size() > 0) {
//
//				for (int k = 0; k < scorej.records.size(); k++) {
//					ScoreRecord scoreRecordk = scorej.records.get(k);
//
//					if (!scoreRecordk.score.equals(ScoreRecord.scoreNA)) {
//						counts[j]++;
//					}
//				}
//			}
//		}
//		return counts;
//	}

//	int[] getCountUniqueSourcesPerEndpoint(Chemical chemical) {
//
//		int[] counts = new int[Chemical.hazard_names.length];
//
//		
//		for (int catNum=0;catNum<chemical.scores.size();catNum++) {
//			ArrayList<String> uniqueSources = new ArrayList<>();
//			
//			Score score=chemical.scores.get(catNum);
//			
//			for (int recordNum=0;recordNum<score.records.size();recordNum++) {
//				ScoreRecord scoreRecord=score.records.get(recordNum);
//				if (!scoreRecord.score.equals(ScoreRecord.scoreNA)) {
//					if (!uniqueSources.contains(scoreRecord.source)) {
//						uniqueSources.add(scoreRecord.source);
//					}
//				}
//			}
//			counts[catNum] = uniqueSources.size();
//			
////			System.out.println(counts[catNum]);
//		}
//		return counts;
//
//	}

//	int getNumberEndpointsWithScore(Chemical chemical) {
//
//		int count = 0;
//
//		for (int j = 0; j < chemical.scores.size(); j++) {
//			Score scorej = chemical.scores.get(j);
//
//			if (scorej.records.size() > 0) {
//
//				for (int k = 0; k < scorej.records.size(); k++) {
//					ScoreRecord scoreRecordk = scorej.records.get(k);
//					if (!scoreRecordk.score.equals(ScoreRecord.scoreNA)) {
//						count++;
//						break;
//					}
//				}
//			}
//		}
//		return count;
//
//	}

//	int[] goEndpointCountsForFilesInFolder(String folder) {
//
//		folder = folder.replace("\\", "/");
//
//		String source = folder.replace("AA Dashboard/Data/", "").replace("/json files", "").trim();
//
//		Vector<String> vec = new Vector<String>();
//		Vector<String> vecWithCAS = new Vector<String>();
//
//		File Folder = new File(folder);
//		File[] files = Folder.listFiles();
//
//		int[] counts = new int[Chemical.hazard_names.length];
//
//		for (int i = 0; i < files.length; i++) {
//
//			if (files[i].getName().indexOf(".json") == -1)
//				continue;
//
//			// if (i%500==0) System.out.println(i);
//
//			try {
//				Gson gson = new Gson();
//
//				Chemical chemical = gson.fromJson(new FileReader(files[i]), Chemical.class);
//
//				if (chemical.CAS == null || chemical.CAS.equals("")) {
//					// System.out.println(files[i].getName()+"\tno cas");
//					continue;
//				}
//
//				for (int j = 0; j < chemical.scores.size(); j++) {
//					Score scorej = chemical.scores.get(j);
//
//					if (scorej.records.size() > 0) {
//
//						for (int k = 0; k < scorej.records.size(); k++) {
//							ScoreRecord scoreRecordk = scorej.records.get(k);
//
//							if (!scoreRecordk.score.equals(ScoreRecord.scoreNA)) {
//								counts[j]++;
//								break;
//							}
//						}
//					}
//				}
//
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
//
//		} // end loop over the files
//
//		System.out.print(source + "\t");
//		for (int j = 0; j < Chemical.hazard_names.length; j++) {
//			System.out.print(counts[j]);
//			if (j < Chemical.hazard_names.length - 1)
//				System.out.print("\t");
//			else
//				System.out.print("\n");
//		}
//
//		return counts;
//	}
	
	
	private void getScoreData2(String source,FlatFileRecord f,Vector<String> vec, Vector<String> vecWithCAS) {
		
		if (f.category != null) {
			f.category = f.category.replace("category", "Category");
			f.category = f.category.trim();

			if (f.category.indexOf("developmental") > -1 && source.equals("Prop 65")) {
				f.category = "Developmental";
			}

			if (f.category.indexOf("reproductive") > -1 && source.equals("Prop 65")) {
				f.category = "Reproductive";
			}

		}

		if (f.hazard_code != null)
			f.hazard_code = f.hazard_code.trim();

		// System.out.println(srj.hazard_code);

		if (f.category != null) {
			if (f.category.indexOf("Mixture") > -1)
				return;
			if (f.category.indexOf("Not classified") > -1 && f.category.indexOf("Category") > -1)
				return;

			if (f.category != null && f.category.indexOf(" (") > -1)
				f.category = f.category.substring(0, f.category.indexOf(" (")).trim();

			if (f.category != null && f.category.indexOf(",") > -1)
				f.category = f.category.substring(0, f.category.indexOf(",")).trim();

			if (f.category.toLowerCase().indexOf("o-:") > -1)
				return;
		}

		
//		if (srj.category==null && chemical.CAS.equals("100-42-5")) {
//			System.out.println(chemical.CAS+"\t"+scorei.hazard_name);
//		}
		
		String line = f.hazard_name + "\t" + f.score + "\t" + f.category + "\t" + f.hazard_code;

		line = line.replace("_", " ");

		// Carcinogenicity VH Not classified null

		line = line.replace("\r", " ").replace("\n", " ");

		if (!vec.contains(line)) {
			vec.add(line);

			// System.out.println(line);

			// if (scorei.hazard_name.equals("Acute Aquatic Toxicity")) {
			// System.out.println(chemical.CAS+"\t"+chemical.name+"\t"+line);
			// }

			String Line2 = line + "\t" + f.CAS;
			vecWithCAS.add(Line2);

			// System.out.println(filename+"\t"+chemical.CAS+"\t"+line);

		}

	}
	

//	void getScoreData(String filename, Chemical chemical, Vector<String> vec, Vector<String> vecWithCAS,
//			String source) {
//		for (int i = 0; i < chemical.scores.size(); i++) {
//
//			Score scorei = chemical.scores.get(i);
//
//			if (scorei.records.size() > 0) {
//				for (int j = 0; j < scorei.records.size(); j++) {
//					ScoreRecord srj = scorei.records.get(j);
//
//					if (srj.category != null) {
//						srj.category = srj.category.replace("category", "Category");
//						srj.category = srj.category.trim();
//
//						if (srj.category.indexOf("developmental") > -1 && source.equals("Prop 65")) {
//							srj.category = "Developmental";
//						}
//
//						if (srj.category.indexOf("reproductive") > -1 && source.equals("Prop 65")) {
//							srj.category = "Reproductive";
//						}
//
//					}
//
//					if (srj.hazard_code != null)
//						srj.hazard_code = srj.hazard_code.trim();
//
//					// System.out.println(srj.hazard_code);
//
//					if (srj.category != null) {
//
//						if (srj.category.indexOf("Mixture") > -1)
//							continue;
//						if (srj.category.indexOf("Not classified") > -1 && srj.category.indexOf("Category") > -1)
//							continue;
//
//						if (srj.category != null && srj.category.indexOf(" (") > -1)
//							srj.category = srj.category.substring(0, srj.category.indexOf(" (")).trim();
//
//						if (srj.category != null && srj.category.indexOf(",") > -1)
//							srj.category = srj.category.substring(0, srj.category.indexOf(",")).trim();
//
//						if (srj.category.toLowerCase().indexOf("o-:") > -1)
//							continue;
//					}
//
//					
////					if (srj.category==null && chemical.CAS.equals("100-42-5")) {
////						System.out.println(chemical.CAS+"\t"+scorei.hazard_name);
////					}
//					
//					String line = scorei.hazard_name + "\t" + srj.score + "\t" + srj.category + "\t" + srj.hazard_code;
//
//					line = line.replace("_", " ");
//
//					// Carcinogenicity VH Not classified null
//
//					line = line.replace("\r", " ").replace("\n", " ");
//
//					if (!vec.contains(line)) {
//						vec.add(line);
//
//						// System.out.println(line);
//
//						// if (scorei.hazard_name.equals("Acute Aquatic Toxicity")) {
//						// System.out.println(chemical.CAS+"\t"+chemical.name+"\t"+line);
//						// }
//
//						String Line2 = line + "\t" + chemical.CAS;
//						vecWithCAS.add(Line2);
//
//						// System.out.println(filename+"\t"+chemical.CAS+"\t"+line);
//
//					}
//
//				}
//
//			}
//
//		}
//
//	}

//	void createDictionaryTextFiles() {
//
//		try {
//
//			AADashboard a = new AADashboard();
//
//			for (int i = 0; i < AADashboard.jsonFolders.size(); i++) {
//
//				// System.out.println(AADashboard.jsonFolders.get(i));
//
//				goThroughSource(AADashboard.jsonFolders.get(i), "AA Dashboard/Data/dictionary");
//			}
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//	}

	/**
	 * Gets counts per endpoints for all sources put together
	 */
	public void getEndpointCountsForAllSources() {
		String flatfilename="flat file 2018-08-01.txt";
		String allSourceFlatFilePath="AA Dashboard\\Data\\dictionary\\text output\\"+flatfilename;
		this.getEndpointCountsForFlatFileAllSources(true,allSourceFlatFilePath);
	}
	
	/**
	 * Gets counts per endpoint for each source
	 */
	void getEndpointCountsForEachSource() {
		AADashboard aaDashboard=new AADashboard();
		
		System.out.print("source\t");
		for (int j = 0; j < Chemical.hazard_names.length; j++) {
			System.out.print(Chemical.hazard_names[j]);

			if (j < Chemical.hazard_names.length - 1)
				System.out.print("\t");
			else
				System.out.print("\n");
		}

		
		for (String source:aaDashboard.sources) {
			getEndpointCountsForSource(source,false);
		}
		
		this.getTEST_ExpCounts();
		
	}
	
	void getEndpointCountsForSource(String source,boolean printHeader) {
		String flatfilepath="AA Dashboard/Data/"+source+"/"+source+" Chemical Records.txt";
		this.getEndpointCountsForFlatFile(source,printHeader,flatfilepath);
	}
	
	void getEndpointCountsForFlatFile(String source,boolean printHeader,String flatfilepath) {

		try {

			AADashboard a = new AADashboard();

			if (printHeader) {
				
				System.out.print("source\t");

				for (int j = 0; j < Chemical.hazard_names.length; j++) {
					System.out.print(Chemical.hazard_names[j]);

					if (j < Chemical.hazard_names.length - 1)
						System.out.print("\t");
					else
						System.out.print("\n");

				}
			}
			
			ArrayList<String>lines=Utilities.readFileToArray(flatfilepath);
			String header=lines.remove(0);
			
//			Hashtable<String,ArrayList<String>>ht=new Hashtable<>();
			
			Multimap<String,String> ht= ArrayListMultimap.create();
			
			Hashtable<String,String>htCASName=new Hashtable<>();
			
			for (int i=0;i<lines.size();i++) {
				String line=lines.get(i);
				FlatFileRecord f=FlatFileRecord.createFlatFileRecord(line);
				
				if (f.score.equals(ScoreRecord.scoreNA)) continue;
				
				//Assign a CAS based on name if have no CAS:
				if (f.CAS==null) {
					if (f.name==null) {
//						System.out.println("CAS and name =null");
						continue;
					}
					
					if (htCASName.get(f.name)==null) {
						String newCAS="NOCAS"+(htCASName.size()+1);
						htCASName.put(f.name,newCAS);
						f.CAS=newCAS;
					} else {
						f.CAS=htCASName.get(f.name);
					}
				}
				
				if (!ht.get(f.hazard_name).contains(f.CAS)) {
					ht.put(f.hazard_name, f.CAS);
					
				}
				
//				if (ht.get(f.hazard_name)==null) {
//					ArrayList<String>listCAS=new ArrayList<>();
//					listCAS.add(f.CAS);
//					ht.put(f.hazard_name, listCAS);
//					
//				} else {
//					ArrayList<String>listCAS=ht.get(f.hazard_name);
//					
//					if (!listCAS.contains(f.CAS)) {
//						
////						if (f.hazard_name.equals(Chemical.strNeurotoxicity_Repeat_Exposure)) {
////							System.out.println(line);
////						}
//						listCAS.add(f.CAS);
//					}
//				}
				
//				System.out.println(f);
				
			}
			
			System.out.print(source+"\t");
			for (String hazard_name:Chemical.hazard_names) {
				if (ht.get(hazard_name)==null) {
					System.out.print(0+"\t");
				}else {
					Collection<String> casListOverall = ht.get(hazard_name);
					System.out.print(casListOverall.size()+"\t");
				}
			}
			System.out.print("\n");
			
//			for (int i = 0; i < AADashboard.jsonFolders.size(); i++) {
//				String folderPath = AADashboard.jsonFolders.get(i);
//				int[] counts = this.goEndpointCountsForFilesInFolder(folderPath);
//			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
	void getEndpointCountsForFlatFileAllSources(boolean printHeader,String flatfilepath) {

		try {

			AADashboard a = new AADashboard();

			if (printHeader) {
				
				System.out.print("source\t");

				for (int j = 0; j < Chemical.hazard_names.length; j++) {
					System.out.print(Chemical.hazard_names[j]);

					if (j < Chemical.hazard_names.length - 1)
						System.out.print("\t");
					else
						System.out.print("\n");

				}
			}
			
			ArrayList<String>lines=Utilities.readFileToArray(flatfilepath);
			String header=lines.remove(0);
			
//			Hashtable<String,ArrayList<String>>ht=new Hashtable<>();
			
			Multimap<String,String> ht= ArrayListMultimap.create();
			
			Hashtable<String,String>htCASName=new Hashtable<>();
			
			for (int i=0;i<lines.size();i++) {
				String line=lines.get(i);
				FlatFileRecord f=FlatFileRecord.createFlatFileRecord(line);
				
				if (f.score.equals(ScoreRecord.scoreNA)) continue;
				
				//Assign a CAS based on name if have no CAS:
				if (f.CAS==null) {
					if (f.name==null) {
//						System.out.println("CAS and name =null");
						continue;
					}
					
					if (htCASName.get(f.name)==null) {
						String newCAS="NOCAS"+(htCASName.size()+1);
						htCASName.put(f.name,newCAS);
						f.CAS=newCAS;
					} else {
						f.CAS=htCASName.get(f.name);
					}
				}
				
				if (!ht.get(f.hazard_name).contains(f.CAS)) {
					ht.put(f.hazard_name, f.CAS);
					
				}
				
//				if (ht.get(f.hazard_name)==null) {
//					ArrayList<String>listCAS=new ArrayList<>();
//					listCAS.add(f.CAS);
//					ht.put(f.hazard_name, listCAS);
//					
//				} else {
//					ArrayList<String>listCAS=ht.get(f.hazard_name);
//					
//					if (!listCAS.contains(f.CAS)) {
//						
////						if (f.hazard_name.equals(Chemical.strNeurotoxicity_Repeat_Exposure)) {
////							System.out.println(line);
////						}
//						listCAS.add(f.CAS);
//					}
//				}
				
//				System.out.println(f);
				
			}
			
			System.out.print("All sources"+"\t");
			for (String hazard_name:Chemical.hazard_names) {
				if (ht.get(hazard_name)==null) {
					System.out.print(0+"\t");
				}else {
					
					Collection<String> casListOverall = ht.get(hazard_name);
//					Collections.sort(casListOverall);
					
//					for (String CAS:casListOverall) {
////						System.out.println(CAS);
//					}
					
					//add cas numbers for TEST experimental records
					String testAbbrev=this.getTEST_AbbrevFromHazardName(hazard_name);
					if (testAbbrev!=null) {
						//Pull in CAS list from experimental values in TEST:
						ArrayList<String>casList=this.getCASListTEST(testAbbrev);
						for (int i=0;i<casList.size();i++) {
							if (!casListOverall.contains(casList.get(i))) {
								casListOverall.add(casList.get(i));
							}
						}
					} 
					System.out.print(casListOverall.size()+"\t");
				}
			}
			System.out.print("\n");
			
//			for (int i = 0; i < AADashboard.jsonFolders.size(); i++) {
//				String folderPath = AADashboard.jsonFolders.get(i);
//				int[] counts = this.goEndpointCountsForFilesInFolder(folderPath);
//			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	void createOverallTextFile() {

		String folder1 = "AA Dashboard/Data";

		String folder2 = folder1 + "/Dictionary";

		File file = new File(folder2);

		File[] files = file.listFiles();

		try {

			FileWriter fw = new FileWriter(folder1 + "/overall dictionary.txt");

			for (int i = 0; i < files.length; i++) {

				if (files[i].isDirectory())
					continue;

				System.out.println(files[i].getName());

				String filename = files[i].getName();

				String endpoint = filename.substring(0, filename.indexOf("."));

				fw.write(endpoint + "\t\n");

				BufferedReader br = new BufferedReader(new FileReader(files[i]));

				while (true) {
					String Line = br.readLine();

					if (Line == null)
						break;

					fw.write(Line + "\r\n");
				}

				fw.write("\r\n");

			}

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Overall Dictionary.txt

	}

	public void getUniqueChemicalList(String folder,String flatfilename) {

		try {
			AADashboard a = new AADashboard();

			ArrayList<String> casList = new ArrayList<String>();

			FileWriter fw = new FileWriter(folder+"/unique chemicals.txt");

			String flatfilepath=folder+"/"+flatfilename;

			
			ArrayList<String>lines=Utilities.readFileToArray(flatfilepath);
			String header=lines.remove(0);
			
			for (int i=0;i<lines.size();i++) {
				
				if (i%1000==0) System.out.println(i);
				
				String line=lines.get(i);
				String CAS=line.substring(0,line.indexOf("|"));
				
				if (!casList.contains(CAS)) {
					casList.add(CAS);
					fw.write(CAS+"\r\n");
				}
				
//				System.out.println(line);
			}

			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

//	void getCountsByEndpoint() {
//
//		AADashboard aad = new AADashboard();
//
//		ArrayList<String>[] al = (ArrayList<String>[]) new ArrayList[20];
//
//		for (int i = 0; i <= 19; i++) {
//			al[i] = new ArrayList<String>();
//		}
//
//		System.out.println("*" + ParseReachVeryHighConcernList.jsonFolder);
//
//		for (int i = 0; i < aad.jsonFolders.size(); i++) {
//
//			System.out.println(aad.jsonFolders.get(i));
//			File folderj = new File(aad.jsonFolders.get(i));
//
//			File[] files = folderj.listFiles();
//
//			for (int j = 0; j < files.length; j++) {
//				// for (int j=0;j<10;j++) {
//				File filej = files[j];
//				String filename = filej.getName();
//				if (filej.isDirectory())
//					continue;
//				if (filename.indexOf(".") == -1)
//					continue;
//
//				String CAS = filename.substring(0, filename.indexOf("."));
//				if (CAS.indexOf("-") == -1)
//					continue;
//				if (CAS.length() > 15)
//					continue;
//
//				try {
//					Gson gson = new Gson();
//
//					Chemical chemical = gson.fromJson(new FileReader(filej), Chemical.class);
//
//					if (chemical == null) {
//						System.out.println(filej.getAbsolutePath() + "\tnull");
//						continue;
//					}
//
//					if (chemical.CAS == null || chemical.CAS.equals("")) {
//						// System.out.println(files[i].getName()+"\tno cas");
//						continue;
//					}
//
//					// System.out.println(gson.toJson(chemical));
//
//					for (int k = 0; k < chemical.scores.size(); k++) {
//
//						Score scorek = chemical.scores.get(k);
//
//						if (scorek.records.size() == 0)
//							continue;
//
//						for (int l = 0; l < scorek.records.size(); l++) {
//							ScoreRecord scoreRecordl = scorek.records.get(l);
//
//							if (scoreRecordl.score == null) {
//								System.out.println(folderj + "\t" + CAS + "\t" + scorek.hazard_name);
//							}
//
//							if (scoreRecordl.score.equals(ScoreRecord.scoreNA))
//								continue;
//
//							if (!al[k].contains(CAS)) {
//								al[k].add(CAS);
//								break;
//								// if (scorek.hazard_name.equals(Chemical.strReproductive)) {
//								// System.out.println(CAS+"\t"+al[k].size());
//								// }
//							}
//
//						} // end l loop over records
//
//					} // end k loop over scores
//
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//
//			} // end j loop over files
//		} // end i loop over folders
//
//		Chemical chemical = new Chemical();
//
//		for (int i = 0; i < chemical.scores.size(); i++) {
//			System.out.print(chemical.scores.get(i).hazard_name + "\t");
//		}
//		System.out.print("\n");
//
//		for (int i = 0; i <= 19; i++) {
//			System.out.print(al[i].size() + "\t");
//		}
//		System.out.print("\n");
//
//	}

	
	String getTEST_AbbrevFromHazardName(String hazardName) {
		String abbrev=null;
		
		if (hazardName.equals(Chemical.strAcute_Mammalian_ToxicityOral)) {
			abbrev=TESTConstants.abbrevChoiceRat_LD50;
		} else if (hazardName.equals(Chemical.strGenotoxicity_Mutagenicity)) {
			abbrev=TESTConstants.abbrevChoiceMutagenicity;
		} else if (hazardName.equals(Chemical.strDevelopmental)) {
			abbrev=TESTConstants.abbrevChoiceReproTox;
		} else if (hazardName.equals(Chemical.strAcute_Aquatic_Toxicity)) {
			abbrev=TESTConstants.abbrevChoiceFHM_LC50;
		} else if (hazardName.equals(Chemical.strBioaccumulation)) {
			abbrev=TESTConstants.abbrevChoiceBCF;
		} else if (hazardName.equals(Chemical.strEndocrine_Disruption)) {
			abbrev=TESTConstants.abbrevChoiceER_Binary;
		} else {
//			System.out.println("missing "+hazardName);
		}
		return abbrev;
	}
	
	
	ArrayList<String>getCASListTEST(String abbrev) {
	
		ArrayList<String> casList=new ArrayList<>();
		
		String filePath="data/"+abbrev+"/"+abbrev+"_training_set-2d.csv";
//		String filePath="data/"+abbrev+"/"+abbrev+" training set predictions.txt";
		getExpCountFromPredictionFile(casList, filePath,",");
//		
		
		filePath="data/"+abbrev+"/"+abbrev+"_prediction_set-2d.csv";
//		filePath="data/"+abbrev+"/"+abbrev+" test set predictions.txt";
		getExpCountFromPredictionFile(casList, filePath,",");
		
		Collections.sort(casList);
		
		return casList;
		
	}

	private void getExpCountFromPredictionFile(ArrayList<String> casList, String filePath,String del) {
		ArrayList<String>lines=Utilities.readFileToArray(filePath);
		lines.remove(0);
		for (String Line:lines) {
			String CAS=Line.substring(0,Line.indexOf(del));
			if (!casList.contains(CAS)) casList.add(CAS);
//			System.out.println(casList.size()+"\t"+CAS);
		}
	}
	
	void getTEST_ExpCounts() {
		
		Chemical chemical=new Chemical();
		
//		for (int i=0;i<chemical.scores.size();i++) {
//			String hazardName=chemical.scores.get(i).hazard_name;
//			
//			if (!hazardName.equals(Chemical.strEndocrine_Disruption)) {
//				String abbrev=getTEST_AbbrevFromHazardName(hazardName);
//				
//				if (abbrev==null) {
//					System.out.println(hazardName+"\t0");
//				} else {
//					ArrayList<String>casList=this.getCASListTEST(abbrev);
//					System.out.println(hazardName+"\t"+casList.size());
//				}
//			} else {
//				//Using both ER endpoints only gets you 2 more chemicals- so dont bother with extra complexity
//				ArrayList<String>casList=this.getCASListTEST(TESTConstants.abbrevChoiceER_Binary);
//				ArrayList<String>casList2=this.getCASListTEST(TESTConstants.abbrevChoiceER_LogRBA);
//				
//				System.out.println(hazardName+"\t"+casList.size());
//				System.out.println(hazardName+"\t"+casList2.size());
//				
//				for (int j=0;j<casList2.size();j++) {
//					if (!casList.contains(casList2.get(j))) {
//						casList.add(casList2.get(j));
//					}
//				}
//				System.out.println(hazardName+"\t"+casList.size());
//			}
//		}
		
		System.out.print("T.E.S.T. Experimental");
		for (int i=0;i<chemical.scores.size();i++) {
			String hazardName=chemical.scores.get(i).hazard_name;

			String abbrev=getTEST_AbbrevFromHazardName(hazardName);
			
			if (abbrev==null) {
//				System.out.println(hazardName+"\t0");
				System.out.print("\t0");
				
			} else {
				ArrayList<String>casList=this.getCASListTEST(abbrev);
				System.out.print("\t"+casList.size());
//				System.out.println(hazardName+"\t"+casList.size());
			}
		}
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GenerateOverallDictionary g = new GenerateOverallDictionary();

//		 g.getUniqueChemicalList("AA Dashboard/Data/Dictionary/text output","flat file 2018-08-23.txt");
		 
//		g.getEndpointCountsForSource(ScoreRecord.sourceJapan,true);
		 
//		g.getTEST_ExpCounts(); 
		
//		 g.getCASListTEST(Chemical.strAcute_Mammalian_ToxicityOral);
		 
//		g.getEndpointCountsForEachSource();
		g.getEndpointCountsForAllSources();
		
		 


//		 g.createOverallTextFile();
		
		 
//		g.goThroughListOfChemicals("AA Dashboard/Data/dictionary/unique chemicals.txt","AA Dashboard/Data/dictionary/text output/score counts per chemical.txt");

	}

}
