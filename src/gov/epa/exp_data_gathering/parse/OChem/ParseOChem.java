package gov.epa.exp_data_gathering.parse.OChem;

import java.io.*;

import java.util.*;

import com.google.gson.JsonArray;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.PublicSource;
import gov.epa.exp_data_gathering.parse.UnitConverter;
import gov.epa.exp_data_gathering.parse.OChem.DriverOChem.ReferenceOChem;

/**
 * Parses data from ochem.eu
 * @author GSINCL01
 *
 */
public class ParseOChem extends Parse {

	public ParseOChem() {
		sourceName = RecordOChem.sourceName;
		this.init();
	}
	
	@Override
	protected void createRecords() {
		if(generateOriginalJSONRecords) {
			RecordOChem r=new RecordOChem();
			Vector<RecordOChem> records = r.parseOChemQueriesFromExcel();
			writeOriginalRecordsToFile(records);
		}
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();

		Hashtable<String,LiteratureSource>htRefs=getReferenceHashtable();

		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);

			List<RecordOChem> recordsOChem = new ArrayList<RecordOChem>();
			RecordOChem[] tempRecords = null;
			
//			File folder=new File(jsonFolder);
//			int numFiles=0;
//			for(File file:folder.listFiles()) {
//				if(file.getName().contains(".json") && file.getName().contains("Original")) {
//					System.out.println(file.getName());
//				}
//			}
			
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordOChem[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsOChem.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordOChem[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsOChem.add(tempRecords[i]);
//						if (tempRecords[i].propertyName.contains("Water solubility") && batch<6) {
//							System.out.println(batch+"\tWS");
//						}
					}
				}
			}

			Iterator<RecordOChem> it = recordsOChem.iterator();
			while (it.hasNext()) {
				RecordOChem r = it.next();
				recordsExperimental.add(r.toExperimentalRecord(htRefs));
			}

			recordsExperimental.getRecordsByProperty();
			
			
			Hashtable<String,PublicSource>htOriginalSources=new Hashtable<String,PublicSource>();
			
			for (ExperimentalRecord er:recordsExperimental) {
				if(er.publicSourceOriginal==null) continue;
				if(!htOriginalSources.contains(er.publicSourceOriginal.name)) {
					htOriginalSources.put(er.publicSourceOriginal.name,er.publicSourceOriginal);
				}
			}
			
//			for (String name:htOriginalSources.keySet()) {
//				System.out.println(gson.toJson(htOriginalSources.get(name)));
//			}


		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}

	
	
	Hashtable<String,LiteratureSource> getReferenceHashtable() {
		
		String folderPath="data\\experimental\\OChem_2024_04_03\\excel files";
		String filePath=folderPath+File.separator+"article_lookup.json";

		DriverOChem d=new DriverOChem();
		JsonArray ja=d.loadReferencesArray(filePath);		
		List<ReferenceOChem>refs=d.getReferenceList(ja);
//		System.out.println(gson.toJson(refs));
		
		Hashtable<String,LiteratureSource>htLSRefs=new Hashtable<String,LiteratureSource>();
		
		for (ReferenceOChem ref:refs) {
			LiteratureSource ls=new LiteratureSource();
			ref.JournalReference=ref.JournalReference.replace(" ();", "");
			
			if(ref.Title!=null) {
				ref.Title=ref.Title.replace(" External web link", "");
			}
			
			if(!ref.Authors.isBlank()) ls.author=ref.Authors;
			
			if(ref.URL!=null)  {
				ref.URL=ref.URL.trim();

				ref.URL=ref.URL.replace("http://https//","https://");//https//
				
				if(ref.URL.substring(ref.URL.length()-2,ref.URL.length()).equals("Y2")) 
					ref.URL=ref.URL.substring(0,ref.URL.length()-2);
				
				if(ref.URL.contains(" ") && !ref.URL.contains("terrabase-")) {
					ref.URL=ref.URL.substring(0,ref.URL.indexOf(" "));
					if(ref.URL.substring(ref.URL.length()-2,ref.URL.length()).equals("Y2")) ref.URL=ref.URL.substring(0,ref.URL.length()-2);
					System.out.println("Fixed URL:"+ref.URL);
				} else if(ref.URL.contains(" ") && ref.URL.contains("terrabase-")) {
					ref.URL=ref.URL.replace(" ", "");
					System.out.println("Fixed URL: "+ref.URL);
				}
				
				
				if(ref.URL.toLowerCase().contains("doi")) ls.doi=ref.URL;
				else ls.url=ref.URL;	
			}
			
			if(ls.author!=null) {
				ls.citation=ls.author+" "+ref.Title+" "+ref.JournalReference;
			} else {
				ls.citation=ref.Title+" "+ref.JournalReference;
			}
			ls.title=ref.Title;
			ls.citation=ls.citation.replace(" ,", ",");
			
			if (ls.citation.endsWith(";")) {
				ls.citation = ls.citation.substring(0, ls.citation.length() - 1);
			}
			
			if(ref.PubMedReference!=null) {
				ls.url = "https://pubmed.ncbi.nlm.nih.gov/" + ref.PubMedReference + "/";
			}
			
			ls.name="OChem_"+ref.ArticleIdentifer;
			
//			System.out.println(gson.toJson(ls));
			
			htLSRefs.put(ref.ArticleIdentifer, ls);
			
		}
		
//		System.out.println(gson.toJson(htLSRefs));
		return htLSRefs;

	}
	
	
	public static void main(String[] args) {
		ParseOChem p = new ParseOChem();
		
		p.generateOriginalJSONRecords=true;
		p.howManyOriginalRecordsFiles=8;
		p.removeDuplicates=false;//just in case, algorithm isnt bulletproof- TODO change to true since have updated the code
//		System.out.println(p.removeDuplicates);
		p.createFiles();
//		p.getReferenceHashtable();
	}
}
