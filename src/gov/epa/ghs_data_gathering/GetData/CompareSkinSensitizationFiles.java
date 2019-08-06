package gov.epa.ghs_data_gathering.GetData;
import java.util.*;

import com.google.common.collect.Multimap;

public class CompareSkinSensitizationFiles {
	
	void testDifferentWaysToStoreData() {
		String folder="AA Dashboard\\Data\\ECHA\\REACH_dossier_data\\Skin Sensitization\\Skin sensitization parse";
		String chembenchFileName="chembenchLLNA.txt";
		
		Hashtable<String,Record> records=Record.loadRecordsFromFile(folder+"\\"+chembenchFileName, "CASRN", "\t");
		Record r=records.get("100-43-6");		
		System.out.println(r.getJSON());
		System.out.println(r.get("LLNA_result"));
		
		Hashtable<String,RecordChembench> recordsCB=RecordChembench.loadRecordsFromFile(folder+"\\"+chembenchFileName, "CASRN", "\t");
		RecordChembench rCB=recordsCB.get("100-43-6");
		System.out.println(rCB.getJSON());
		System.out.println(rCB.LLNA_result);
		
	}
	
	
	/** 
	 * Finds records that appear in Chembench but dont appear in my ECHA scrape and outputs the "bad" records that might match
	 * 
	 * @param recordsCB
	 * @param recordsECHA
	 */
	void getInMyECHAScrapeNotInChembench(Hashtable<String,RecordChembench>recordsCB,Hashtable<String,RecordECHA> recordsECHA,Multimap<String,RecordECHA> recordsECHA_Bad) {

		
		List<String> v = new ArrayList<String>(recordsECHA.keySet());
		
		int count=0;

//	    System.out.println(RecordECHA.getHeaderCSV());
	    System.out.println(RecordECHA.getHeader());

		
		for (String CAS_ECHA : v) {
			RecordECHA recordECHA=recordsECHA.get(CAS_ECHA);
			
			if (recordsCB.get(CAS_ECHA)==null) {
				count++;
//				System.out.println(recordECHA.toCSV());
				System.out.println(recordECHA);
				
				
			}
			
			
		}
		System.out.println("\nCount not found in chembench:"+count);
	}

	/** 
	 * Finds records that appear in my ECHA scrape that dont appear in Jeremy's echemportal 
	 * @param recordsCB
	 * @param recordsECHA
	 */
	void getInMyECHAScrapeNotInEchemportal(Hashtable<String,RecordECHA> recordsECHA,Hashtable<String,RecordEchemportal> recordsEchemportal) {

		
		List<String> v = new ArrayList<String>(recordsECHA.keySet());
		
		int count=0;

//	    System.out.println(RecordECHA.getHeaderCSV());
	    System.out.println(RecordECHA.getHeader());

		
		for (String CAS_ECHA : v) {
			RecordECHA recordECHA=recordsECHA.get(CAS_ECHA);
			if (recordsEchemportal.get(CAS_ECHA)==null) {
				count++;
//				System.out.println(recordECHA.toCSV());
				System.out.println(recordECHA);
			}
			
		}
		
		System.out.println("\nCount not found in Echemportal:"+count);
	}
	
	/** 
	 * Finds records that appear in my ECHA scrape that dont appear in my echemportal download
	 *  
	 * @param recordsCB
	 * @param recordsECHA
	 */
	void getInMyECHAScrapeNotInMyEchemportal(Hashtable<String,RecordECHA> recordsECHA,Hashtable<String,RecordEchemportal2> recordsEchemportal) {

		
		List<String> v = new ArrayList<String>(recordsECHA.keySet());
		
		int count=0;

//	    System.out.println(RecordECHA.getHeaderCSV());
	    System.out.println(RecordECHA.getHeader());

		
		for (String CAS_ECHA : v) {
			RecordECHA recordECHA=recordsECHA.get(CAS_ECHA);
			if (recordsEchemportal.get(CAS_ECHA)==null) {
				count++;
//				System.out.println(recordECHA.toCSV());
				System.out.println(recordECHA);
			}
			
		}
		
		System.out.println("\nCount not found in Echemportal:"+count);
	}
	
	/** 
	 * Finds records that appear in my echemportal download that dont appear in my ECHA scrape  
	 *  
	 * @param recordsCB
	 * @param recordsECHA
	 */
	void getInMyEchemportalNotInMyECHAScrape(Hashtable<String,RecordECHA> recordsECHA,Hashtable<String,RecordEchemportal2> recordsEchemportal) {

		
		List<String> v = new ArrayList<String>(recordsEchemportal.keySet());
		
		int count=0;

//	    System.out.println(RecordECHA.getHeaderCSV());
	    System.out.println(RecordEchemportal2.getHeader());

		
		for (String CAS_EChemportal : v) {
			RecordEchemportal2 recordEchemportal=recordsEchemportal.get(CAS_EChemportal);
			
			if (recordsECHA.get(CAS_EChemportal)==null) {
				count++;
//				System.out.println(recordECHA.toCSV());
				System.out.println(recordEchemportal);
			}
			
		}
		
		System.out.println("\nCount not found in ECHA scrape:"+count);
	}
	
	
	/** 
	 * Finds records that appear in Chembench but dont appear in my ECHA scrape and outputs the "bad" records that might match
	 * 
	 * @param recordsCB
	 * @param recordsECHA
	 */
	void getInChembenchNotInMyECHAScrape(Hashtable<String,RecordChembench>recordsCB,Hashtable<String,RecordECHA> recordsECHA,Multimap<String,RecordECHA> recordsECHA_Bad) {
		
		List<String> v = new ArrayList<String>(recordsCB.keySet());
	    Collections.sort(v);

	    int count=0;

	    System.out.println(RecordChembench.getHeader());
	    System.out.println(RecordECHA.getHeader());
	    System.out.println("");
	    
	    for (String CAS_Chembench : v) {
	    	
	    	RecordChembench recordCB=recordsCB.get(CAS_Chembench);

	    	//Only look at ones that cite REACH:
	    	if (!recordCB.LLNA_reference.contains("REACH")) continue;
	    	
	    	
	    	if (recordsECHA.get(CAS_Chembench)==null) {
		    	count++;
		    	
		    	Collection<RecordECHA>recordsBad=recordsECHA_Bad.get(CAS_Chembench);
		    	
		    	if (recordsBad.size()==0) {
			    	//Try to find matches anywhere in the all the records:
			    	Vector<RecordECHA> vecMatch=findPossibleMatches(CAS_Chembench, recordsECHA, recordsECHA_Bad);

			    	System.out.println(recordCB);
			    	for (RecordECHA rECHA:vecMatch) {
			    		System.out.println(rECHA);
			    	}
		    		System.out.println("");
		    	} else {
		    		System.out.println(recordCB);
			    	for (RecordECHA recordBad:recordsBad) {
			    		System.out.println(recordBad);
			    	}
			    	System.out.println("");
		    	}
		    	
		    	
	    	}
	    	
	    }
	    System.out.println("\nCount not found:"+count);
	}
	
	Vector<RecordECHA> findPossibleMatches(String CAS,Hashtable<String,RecordECHA> recordsECHA,Multimap<String,RecordECHA> recordsECHA_Bad) {
		
		Vector<RecordECHA>list=new Vector<>();
		
		Set<String> CASNumbers_Good = recordsECHA.keySet();
        for(String CASNumber_Good: CASNumbers_Good){
        	RecordECHA rECHA=recordsECHA.get(CASNumber_Good);
        	
        	if (rECHA.toString().contains(CAS)) {
        		list.add(rECHA);
        	}
        }
        
		Set<String> CASNumbers_Bad = recordsECHA_Bad.keySet();
        for(String CASNumber_Bad: CASNumbers_Good){
        	Collection<RecordECHA>recordsBad=recordsECHA_Bad.get(CASNumber_Bad);
        	
        	for (RecordECHA rECHA:recordsBad) {
            	if (rECHA.toString().contains(CAS)) {
            		list.add(rECHA);
            	}
        	}
        	
        }
        return list;
		
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String folder="AA Dashboard\\Data\\ECHA\\REACH_dossier_data\\Skin Sensitization\\Skin sensitization parse";

		String chembenchFileName="chembenchLLNA.txt";
		Hashtable<String,RecordChembench> recordsCB=RecordChembench.loadRecordsFromFile(folder+"\\"+chembenchFileName, "CASRN", "\t");
		
		String ECHAFileName="echa skin data-LLNA good-no duplicates.txt";
		Hashtable<String,RecordECHA> recordsECHA=RecordECHA.loadRecordsFromFile(folder+"\\"+ECHAFileName, "CAS_final", "\t");
		
		String ECHAFileNameBad="echa skin data-LLNA bad.txt";
		Multimap<String,RecordECHA> recordsECHA_Bad=RecordECHA.loadRecordsFromFileWithDuplicates(folder+"\\"+ECHAFileNameBad, "CAS_final", "\t");

//		String EchemportalFileName="echemportal skin data-LLNA good-no duplicates.txt";
//		String EchemportalFolder="AA Dashboard\\Data\\ECHA\\REACH_dossier_data\\ECHEMPORTAL DATA From Jeremy";
//		Hashtable<String,RecordEchemportal> recordsEchemportal=RecordEchemportal.loadRecordsFromFile(EchemportalFolder+"\\"+EchemportalFileName, "CAS_final", "\t");

		String EchemportalFileName="echemportal skin data-LLNA good-no duplicates-omit bad scifinder.txt";
		String EchemportalFolder="AA Dashboard\\Data\\ECHA\\REACH_dossier_data\\eChemPortal";
		Hashtable<String,RecordEchemportal2> recordsEchemportal2=RecordEchemportal2.loadRecordsFromFile(EchemportalFolder+"\\"+EchemportalFileName, "CAS_final", "\t");
		
		
		CompareSkinSensitizationFiles c=new CompareSkinSensitizationFiles();
//		c.getInChembenchNotInMyECHAScrape(recordsCB, recordsECHA,recordsECHA_Bad);
//		c.getInMyECHAScrapeNotInChembench(recordsCB, recordsECHA, recordsECHA_Bad);
		
		c.getInMyECHAScrapeNotInMyEchemportal(recordsECHA, recordsEchemportal2);
//		c.getInMyEchemportalNotInMyECHAScrape(recordsECHA, recordsEchemportal2);
		
	}

}
