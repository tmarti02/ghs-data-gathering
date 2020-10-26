package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_genetox_summary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Vector;

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;

public class ParseToxValGenetox {

	
	/**
	 * Create hashtable to assign scores based on GHS H codes
	 * 
	 * @return
	 */
	public static Hashtable<String,String> populateGenetoxCallToScoreValue() {
		
		Hashtable<String,String>dictCC=new Hashtable<String,String>();
		
		dictCC.put("gentox", ScoreRecord.scoreVH);
		dictCC.put("pred gentox", ScoreRecord.scoreVH);
		dictCC.put("pred clastogen", ScoreRecord.scoreVH);
		dictCC.put("clastogen", ScoreRecord.scoreVH);
		
		dictCC.put("non gentox", ScoreRecord.scoreL);
		dictCC.put("pred non gentox", ScoreRecord.scoreL);

		dictCC.put("inconclusive", ScoreRecord.scoreNA);
		dictCC.put("not clastogen", ScoreRecord.scoreNA);
		
		return dictCC;
		
	}
	
	public static void createScoreRecord(Chemical chemical,RecordToxValGenetox rc,Hashtable<String,String>dictCC) {
//		System.out.println(rc.casrn+"\t"+rc.cancer_call);
		
		Score score=chemical.scoreGenotoxicity_Mutagenicity;
		
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
						
		sr.source = ScoreRecord.sourceToxVal;
		sr.name=rc.name;
		
		sr.toxvalID="genetox_summary_"+rc.genetox_summary_id;
		
		//TODO determine what model predicted values come from (ask Richard!)- assign to sourceOriginal
		
		if (rc.genetox_call.contains("pred")) {
			sr.listType=ScoreRecord.typePredicted;	
		} else {
			sr.listType=ScoreRecord.typeScreening;//Leora check this!
		}
						
		
		if (dictCC.get(rc.genetox_call)==null) {
			System.out.println("Need dictionary entry for \"" +rc.genetox_call+"\"");
			return;
		}
		
		sr.score=dictCC.get(rc.genetox_call);
		sr.hazardStatement=rc.genetox_call;
		
		sr.rationale = "Score of "+sr.score+" was assigned based on a genetox call of "+"\""+rc.genetox_call+"\"";
		
		score.records.add(sr);

	}
	
	void goThroughRecordsMultipleChemicals(String filepathText,String destfilepathJson,String destfilepathText,Vector<String>casList) {
		
		try {
						
			Hashtable<String, String>htCC=populateGenetoxCallToScoreValue();
			
			BufferedReader br=new BufferedReader(new FileReader(filepathText));
			
			String header=br.readLine();
								
			String [] hlist=header.split("\t");
			
			Chemicals chemicals=new Chemicals();
			
			Chemical chemical=new Chemical();
			
			String oldCAS="";
			
			while (true) {
				
				String Line=br.readLine();			
//				System.out.println(Line);
				
				if  (Line==null) break;
																			
				RecordToxValGenetox r=RecordToxValGenetox.createRecord(header, Line);
				
				if (casList!=null)
					if (!casList.contains(r.casrn)) continue;
								
				if (!r.casrn.contentEquals(oldCAS)) {
					chemical=new Chemical();
					chemical.CAS=r.casrn;
					chemical.name=r.name;										
					chemicals.add(chemical);										
					oldCAS=r.casrn;					
				}
				
				createScoreRecord(chemical, r,htCC);
//				System.out.println(Line);

				
			}
			
			chemicals.writeToFile(destfilepathJson);
			chemicals.toFlatFile(destfilepathText,"\t");
//			writeChemicalToFile(chemical, destfilepath);
			
			br.close();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	public static void main(String[] args) {
			// TODO Auto-generated method stub
		
			ParseToxValGenetox p=new ParseToxValGenetox();
	//		p.createFiles();
			
	//		String folder="C:\\Users\\Leora\\Desktop\\Tele\\ToxVal";
			String folder="E:\\Documents\\0000 epa\\0 telework\\AA dashboard";
			
			String CAS="79-06-1"; //acrylamide

			String filePathDatabaseAsText=folder+File.separator+"dictionary_genetox_CHA.txt";//excel file converted to tab delimited text file
			
	//		String filePathRecordsForCAS=folder+File.separator+"toxval_cancel_summary_"+CAS+".txt";		
	//		String filePathRecordsForCAS_json=folder+File.separator+"records_"+CAS+".json";
	//		String filePathRecordsForCAS_txt=folder+File.separator+"records_"+CAS+".txt";		
	//		p.getRecordsForCAS(CAS,filePathDatabaseAsText, filePathRecordsForCAS);//Not needed 		
	//		p.goThroughRecords(filePathRecordsForCAS,filePathRecordsForCAS_json,filePathRecordsForCAS_txt);//Not needed 
					
			Vector<String>vecCAS=new Vector<>();
			vecCAS.add("79-06-1");
			vecCAS.add("79-01-6"); 
			vecCAS.add("108-95-2"); 
			vecCAS.add("50-00-0"); 
			vecCAS.add("111-30-8");
			vecCAS.add("302-01-2"); 
			vecCAS.add("75-21-8"); 
			vecCAS.add("7803-57-8"); 
			vecCAS.add("101-77-9"); 
			vecCAS.add("10588-01-9"); 
			vecCAS.add("107-13-1"); 
			vecCAS.add("110-91-8"); 
			vecCAS.add("106-93-4"); 
			vecCAS.add("67-56-1"); 
			vecCAS.add("7664-39-3"); 
			vecCAS.add("556-52-5"); 
			vecCAS.add("87-86-5"); 
			vecCAS.add("62-53-3"); 
			vecCAS.add("106-89-8"); 
			vecCAS.add("7778-50-9");
	//				
//			String filePathRecordsForCASList_json=folder+File.separator+"toxval_cancer_summary_top 20.json";		
//			String filePathRecordsForCASList_txt=filePathRecordsForCASList_json.replace(".json", ".txt");	
//			p.goThroughRecordsMultipleChemicals(filePathDatabaseAsText,filePathRecordsForCASList_json,filePathRecordsForCASList_txt,vecCAS);
			
			String filepathGenetoxAllJSON=folder+File.separator+"genetox call.json";
			String filepathGenetoxAllTXT=folder+File.separator+"genetox call.txt";			
			p.goThroughRecordsMultipleChemicals(filePathDatabaseAsText,filepathGenetoxAllJSON,filepathGenetoxAllTXT,null);
			
		}
	
	
	
	
}
