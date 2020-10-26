package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_cancer_summary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Vector;

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval.ParseToxVal;

public class ParseToxValCancer {
		
	
	/**
	 * Create hashtable to assign scores based on GHS H codes
	 * 
	 * @return
	 */
	public static Hashtable<String,String> populateCancerCallToScoreValue() {
		
		Hashtable<String,String>dictCC=new Hashtable<String,String>();
		
		dictCC.put("A (Human carcinogen)", ScoreRecord.scoreVH);
		dictCC.put("A (Human carcinogen) (Inhalation route)", ScoreRecord.scoreVH);
		dictCC.put("B1 (Probable human carcinogen - based on limited evidence of carcinogenicity in humans)", ScoreRecord.scoreVH);
		dictCC.put("B2 (Probable human carcinogen - based on sufficient evidence of carcinogenicity in animals)", ScoreRecord.scoreVH);
		dictCC.put("C (Possible human carcinogen)", ScoreRecord.scoreH);
		dictCC.put("Carcinogenic potential cannot be determined", ScoreRecord.scoreNA);
		dictCC.put("Carcinogenic potential cannot be determined (Inhalation route)", ScoreRecord.scoreNA);
		dictCC.put("Carcinogenic potential cannot be determined (Oral route)", ScoreRecord.scoreNA);
		dictCC.put("Carcinogenic to humans", ScoreRecord.scoreVH);
		dictCC.put("Carcinogenic to humans (Inhalation route)", ScoreRecord.scoreVH);
		dictCC.put("CEPA (Although there is some evidence for the carcinogenicity of inorganic fluoride, available data are inconclusive.)", ScoreRecord.scoreNA);
		dictCC.put("D (Not classifiable as to human carcinogenicity)", ScoreRecord.scoreNA);
		dictCC.put("D (Not classifiable as to human carcinogenicity) (Oral route)", ScoreRecord.scoreNA);
		dictCC.put("Data are inadequate for an assessment of human carcinogenic potential", ScoreRecord.scoreNA);
		dictCC.put("Data are inadequate for an assessment of human carcinogenic potential (Oral route)", ScoreRecord.scoreNA);
		dictCC.put("E (Evidence of non-carcinogenicity for humans)", ScoreRecord.scoreL);
		dictCC.put("Group 1 - Carcinogenic to humans", ScoreRecord.scoreVH);
		dictCC.put("Group 2A - Probably carcinogenic to humans", ScoreRecord.scoreVH);
		dictCC.put("Group 2B - Possibly carcinogenic to humans", ScoreRecord.scoreH);
		dictCC.put("Group 2B: IARC (possibly carcinogenic to humans)", ScoreRecord.scoreH);
		dictCC.put("Group 3 - Not classifiable as to its carcinogenicity to humans", ScoreRecord.scoreNA);
		dictCC.put("Group 3: IARC (not classifiable as to its carcinogenicity to humans) Group C: IRIS (possible human carcinogen)", ScoreRecord.scoreNA);
		dictCC.put("Group 4 - Probably not carcinogenic to humans", ScoreRecord.scoreL);
		dictCC.put("Group A Human Carcinogen", ScoreRecord.scoreVH);
		dictCC.put("Group A Human Carcinogen by Inhalation", ScoreRecord.scoreVH);
		dictCC.put("Group B1 Probable Human Carcinogen ", ScoreRecord.scoreVH);
		dictCC.put("Group B2 Probable Human Carcinogen ", ScoreRecord.scoreVH);
		dictCC.put("Group C Possible Human Carcinogen ", ScoreRecord.scoreH);
		dictCC.put("Group C: IRIS (a possible human carcinogen)", ScoreRecord.scoreH);
		dictCC.put("Group D Not Classifiable as to Human Carcinogenicity", ScoreRecord.scoreNA);
		dictCC.put("Group D: IRIS (not classifiable as to human carcinogenicity)", ScoreRecord.scoreNA);
		dictCC.put("Group D: IRIS (not classifiable as to human carcinogenicity) (IRIS), 1991/Group 2B: IARC (possibly carcinogenic to humans)/ GCDWQ: HC, 1986", ScoreRecord.scoreH);
		dictCC.put("Group D: IRIS (not classifiable as to human carcinogenicity); Group 2B: IARC (possibly carcinogenic to humans)", ScoreRecord.scoreH);
		dictCC.put("Group E Evidence of Non-carcinogenicity for Humans", ScoreRecord.scoreL);
		dictCC.put("Group I: CEPA (carcinogenic to humans)", ScoreRecord.scoreVH);
		dictCC.put("Group II: CEPA (probably carcinogenic to humans)", ScoreRecord.scoreVH);
		dictCC.put("Group II: CEPA (probably carcinogenic to humans) Group 2A: IARC (probably carcinogenic to humans)", ScoreRecord.scoreVH);
		dictCC.put("Group III: CEPA (possible germ cell mutagen, and possibly carcinogenic to humans)", ScoreRecord.scoreH);
		dictCC.put("Group III: CEPA (possible germ cell mutagen; possibly carcinogenic to humans)", ScoreRecord.scoreH);
		dictCC.put("Group III: CEPA (possibly carcinogenic to humans)", ScoreRecord.scoreH);
		dictCC.put("Group IIIB (possibly carcinogenic to humans, limited evidence of carcinogenicity)", ScoreRecord.scoreH);
		dictCC.put("Group IIIB: (possibly carcinogenic to humans)", ScoreRecord.scoreH);
		dictCC.put("Group IV: CEPA (unlikely to be carcinogenic to humans)", ScoreRecord.scoreL);
		dictCC.put("Group IV: CEPA (unlikely to be carcinogenic to humans) Group 2A: IARC (probably carcinogenic to humans)", ScoreRecord.scoreL);
		dictCC.put("Group IVC: CEPA (probably not carcinogenic to humans)", ScoreRecord.scoreL);
		dictCC.put("Group V (inadequate data for evaluation of carcinogenicity)", ScoreRecord.scoreNA);
		dictCC.put("Group V: CEPA (probably not carcinogenic to humans)", ScoreRecord.scoreNA);
		dictCC.put("Group VA: CEPA (inadequate data for evaluation)", ScoreRecord.scoreNA);
		dictCC.put("Group VI: CEPA (unclassifiable with respect to carcinogenicity to humans)", ScoreRecord.scoreNA);
		dictCC.put("Group VIA: CEPA (unclassifiable with respect to carcinogenicity to humans)", ScoreRecord.scoreNA);
		dictCC.put("Group VIB: CEPA (unclassifiable with respect to carcinogenesis in humans)", ScoreRecord.scoreNA);
		dictCC.put("Inadequate for an assessment of carcinogenic potential", ScoreRecord.scoreNA);
		dictCC.put("Inadequate information to assess carcinogenic potential", ScoreRecord.scoreNA);
		dictCC.put("Inadequate information to assess carcinogenic potential (Oral route)", ScoreRecord.scoreNA);
		dictCC.put("Inadequate information to assess carcinogenic potential (oral, inhalation is not likely to be carcinogenic)", ScoreRecord.scoreNA);
		dictCC.put("IOM does not consider manganese carcinogenic to humans", ScoreRecord.scoreL);
		dictCC.put("IOM does not consider molybdenum carcinogenic to humans", ScoreRecord.scoreL);
		dictCC.put("IOM does not consider selenium carcinogenic to humans. Group 3: IARC (not classifiable as to human carcinogenicity) Group B2: U.S. EPA (probable human carcinogen) for selenium sulphide", ScoreRecord.scoreNA);
		dictCC.put("IOM does not consider zinc carcinogenic to humans", ScoreRecord.scoreL);
		dictCC.put("IOM, 2001 (\"There is little convincing evidence indicating that copper is causally associated with the development of cancer in humans.\")", ScoreRecord.scoreNA);
		dictCC.put("IRIS (inadequate data for evaluation of carcinogenicity) Group 2A: IARC (probably carcinogenic to humans)", ScoreRecord.scoreVH);
		dictCC.put("IRIS (inadequate data to assess human carcinogenic potential)", ScoreRecord.scoreNA);
		dictCC.put("IRIS (likely to be carcinogenic to humans)", ScoreRecord.scoreVH);
		dictCC.put("Known Human Carcinogen", ScoreRecord.scoreVH);
		dictCC.put("Known/Likely", ScoreRecord.scoreVH);
		dictCC.put("Known/likely human carcinogen", ScoreRecord.scoreVH);
		dictCC.put("Known/likely human carcinogen (Inhalation route)", ScoreRecord.scoreVH);
		dictCC.put("Known/likely human carcinogen (Oral route)", ScoreRecord.scoreVH);
		dictCC.put("Likely Human Carcinogen", ScoreRecord.scoreVH);
		dictCC.put("Carcinogenic to Humans", ScoreRecord.scoreVH);
		
		dictCC.put("Likely to be Carcinogenic in Humans at High Doses; Not Likely to be Carcinogenic to Humans at Low Doses", ScoreRecord.scoreH);
		dictCC.put("Likely to be carcinogenic to humans", ScoreRecord.scoreVH);
		dictCC.put("Likely to be carcinogenic to humans (Combined route)", ScoreRecord.scoreVH);
		dictCC.put("Likely to be Carcinogenic to Humans (High Doses), Not Likely to be Carcinogenic to Humans (Low Doses)", ScoreRecord.scoreH);
		dictCC.put("Likely to be carcinogenic to humans (inhalation route. IN for oral route)", ScoreRecord.scoreVH);
		dictCC.put("Likely to be carcinogenic to humans (oral route)", ScoreRecord.scoreVH);
		dictCC.put("Likely to be carcinogenic to humans (oral); inadequate (inhalation)", ScoreRecord.scoreVH);
		dictCC.put("Likely to be carcinogenic to humans by inhalation route", ScoreRecord.scoreVH);
		dictCC.put("Likely to be carcinogenic to humans following prolonged, high-level exposures causing cytotoxicity and regenerative cell hyperplasia in the proximal region of the small intestine (oral exposure) or the respiratory tract (inhalation exposure), but not like", ScoreRecord.scoreH);
		dictCC.put("Likely to be carcinogenic to humans for oral exposure. Inadequate information for inhalation.", ScoreRecord.scoreVH);
		dictCC.put("no adequate data to characterize in terms of carcinogenicity", ScoreRecord.scoreNA);
		dictCC.put("No Data Available", ScoreRecord.scoreNA);
		dictCC.put("Not Likely to Be Carcinogenic in Humans", ScoreRecord.scoreL);
		dictCC.put("Not likely to be carcinogenic to humans", ScoreRecord.scoreL);
		dictCC.put("Not likely to be carcinogenic to humans (Oral route)", ScoreRecord.scoreL);
		dictCC.put("Not Likely to be Carcinogenic to Humans at Doses that Do Not Alter Rat Thyroid Hormone Homeostasis", ScoreRecord.scoreNA);
		dictCC.put("Not likely to be carcinogenic to Non-humans", ScoreRecord.scoreL);
		dictCC.put("Not Yet  Determined", ScoreRecord.scoreNA);
		dictCC.put("potential occupational carcinogen", ScoreRecord.scoreVH);
		dictCC.put("Reasonably Anticipated To Be Human Carcinogen", ScoreRecord.scoreVH);
		dictCC.put("Suggestive Evidence for Carcinogenicity in Humans", ScoreRecord.scoreH);
		dictCC.put("Suggestive evidence of carcinogenic potential", ScoreRecord.scoreH);
		dictCC.put("Suggestive evidence of carcinogenic potential (oral. Inhalation is not likely to be carcinogenic)", ScoreRecord.scoreH);
		dictCC.put("Suggestive evidence of carcinogenic potential for oral exposure.", ScoreRecord.scoreH);
		dictCC.put("Suggestive evidence of carcinogenic potential for oral exposure. Inadequate information for inhalation.", ScoreRecord.scoreH);
		dictCC.put("Suggestive Evidence of Carcinogenicity but Not Sufficient to Assess Human Carcinogenic Potential", ScoreRecord.scoreH);
		dictCC.put("Suggestive evidence of carcinogenicity in humans", ScoreRecord.scoreH);
		dictCC.put("Suggestive Evidence of Carcinogenicity to Humans", ScoreRecord.scoreH);
		dictCC.put("Suggestive evidence of carcinogenicity, but not sufficient to assess human carcinogenic potential (Inhalation route)", ScoreRecord.scoreH);
		dictCC.put("Suggestive evidence of the carcinogenic potential for the inhalation route (IN for oral).", ScoreRecord.scoreH);


		return dictCC;
		
	}
	

	public static void createScoreRecord(Chemical chemical,RecordToxValCancer rc,Hashtable<String,String>dictCC) {
//		System.out.println(rc.casrn+"\t"+rc.cancer_call);
		
		Score score=chemical.scoreCarcinogenicity;
		
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
						
		sr.name=rc.name;
		
		sr.source = ScoreRecord.sourceToxVal;
		sr.sourceOriginal=rc.source;
		
		ParseToxVal.setAuthority(sr);
		
		sr.url=rc.url;
		
		if (dictCC.get(rc.cancer_call)==null) {
			System.out.println("Need dictionary entry for " +rc.cancer_call);
			return;
		}
		
		sr.toxvalID="cancer_summary_"+rc.chemical_id;
		
		sr.score=dictCC.get(rc.cancer_call);
		sr.hazardStatement=rc.cancer_call;
		
		sr.rationale = "Score of "+sr.score+" was assigned based on a cancer call of "+"\""+rc.cancer_call+"\"";
		
		score.records.add(sr);

	}
	
	
	void goThroughRecordsMultipleChemicals(String filepathText,String destfilepathJson,String destfilepathText,Vector<String>casList) {
		
		try {
						
			Hashtable<String, String>htCC=populateCancerCallToScoreValue();
			
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
																			
				RecordToxValCancer r=RecordToxValCancer.createRecord(header, Line);
				
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
	
		ParseToxValCancer p=new ParseToxValCancer();
//		p.createFiles();
		
//		String folder="C:\\Users\\Leora\\Desktop\\Tele\\ToxVal";
		String folder="E:\\Documents\\0000 epa\\0 telework\\AA dashboard";
		
		String CAS="79-06-1"; //acrylamide
		
		//  Want to add CAS 79-01-6 trichloroethylene as another chemical with a lot of data.  -Leora
				
		String filePathDatabaseAsText=folder+File.separator+"toxval_cancer_summary_2020-01-16.txt";//excel file converted to tab delimited text file
		
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
		String filePathRecordsForCASList_json=folder+File.separator+"toxval_cancer_summary_top 20.json";		
		String filePathRecordsForCASList_txt=filePathRecordsForCASList_json.replace(".json", ".txt");

		p.goThroughRecordsMultipleChemicals(filePathDatabaseAsText,filePathRecordsForCASList_json,filePathRecordsForCASList_txt,vecCAS);
		
		
		
	}
}
