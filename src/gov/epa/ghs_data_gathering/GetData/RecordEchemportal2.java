package gov.epa.ghs_data_gathering.GetData;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.google.common.collect.HashMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.ghs_data_gathering.Utilities.Utilities;

import com.google.common.collect.Multimap;

/* 
 * 
 * Search on echemportal on 5-14-19:
 * 
* https://www.echemportal.org/echemportal/propertysearch/treeselect_input.action?queryID=PROQo55
* 
* Query Block Type: Skin sensitisation
* 
* Type of information: experimental study
* 
* Reliability: 1 (reliable without restriction)|2 (reliable with
* restrictions)|3 (not reliable)|4 (not assignable)|other:*
* 
* Reference, Year: leave blank
* 
* Test guideline, Qualifier: according to|equivalent or similar to|no guideline
* available|no guideline followed|no guideline required
* 
* Test guideline, Guideline: EPA OPP 81-6 (Skin Sensitisation)|EPA OPPTS
* 870.2600 (Skin Sensitisation)|EPA OTS 798.4100 (Skin Sensitisation)|EU Method
* B.42 (Skin Sensitisation: Local Lymph Node Assay)|EU Method B.6 (Skin
* Sensitisation)|OECD Guideline 406 (Skin Sensitisation)|OECD Guideline 429
* (Skin Sensitisation: Local Lymph Node Assay)|OECD Guideline 442A (Skin
* Sensitization: Local Lymph Node Assay: DA)|OECD Guideline 442B (Skin
* Sensitization: Local Lymph Node Assay: BrdU-ELISA)|OECD Guideline 442C (In
* Chemico Skin Sensitisation: Direct Peptide Reactivity Assay (DPRA))|OECD
* Guideline 442D (In Vitro Skin Sensitisation: ARE-Nrf2 Luciferase Test
* Method)|other:*
* 
* GLP compliance: no|not specified|yes|yes (incl. certificate)
* 
* Endpoint: skin sensitisation: in vivo (LLNA) <== use to limit records under 10,000!
* 
* Type of study: activation of dentritic cells|activation of
* keratinocytes|Buehler test|direct peptide binding assay|Draize test|Freund's
* complete adjuvant test|guinea pig maximisation test|intracutaneous
* test|Maurer optimisation test|mouse ear swelling test|mouse local lymphnode
* assay (LLNA)|mouse local lymph node assay (LLNA): BrdU-ELISA|mouse local
* lymph node assay (LLNA): DA|not specified|open epicutaneous test|patch
* test|reduced LLNA|reduced LLNA: BrdU-ELISA|reduced LLNA: DA|skin painting
* test|split adjuvant test|other:*
* 
* Species: guinea pig|mouse|rabbit|other:*|human
* 
* Strain: leave blank
* 
* Interpretation of results: Category 1 (skin sensitising) based on GHS
* criteria|Category 1A (indication of significant skin sensitising potential)
* based on GHS criteria|Category 1B (indication of skin sensitising potential)
* based on GHS criteria|GHS criteria not met|study cannot be used for
* classification|other:*|ambiguous|no data|not sensitising|sensitising
*/

public class RecordEchemportal2  {

	//File format based on output from echemportal on 5-14-19

	public String Record_Number;
	public String Substance_Name="";
	public String Name_Type="";
	public String Substance_Number="";
	public String Number_type="";
	public String Hyperlink="";
	
//	public String Member_of_Category="";
//	public String Participant="";
//	public String Section="";
//	public String Values="";
	
	public String Test_guideline_Qualifier="";
	public String Test_guideline_Guideline="";
	public String GLP_Compliance="";
	public String Reliability="";
	public String Species="";
	public String Type_of_study="";
	public String Type_of_information="";	
	public String Endpoint="";
	public String Type_of_coverage="";
	
	public String formula="";//TMM added
	public String CAS_final="";//TMM added
	public String CAS_warning="";//TMM added
	public String omit_reason="";//TMM added

	public String Interpretation_of_results="";
	public String InterpretationBasis="";
	public String FinalScore="";


	static String[] varlist = { "Record_Number","Hyperlink","Substance_Name", "Name_Type","Substance_Number", "Number_type", 
			"Test_guideline_Qualifier", "Test_guideline_Guideline", "GLP_Compliance", "Reliability", "Species",
			"Type_of_study", "Type_of_information", "Endpoint", "Type_of_coverage","formula","CAS_final", "CAS_warning", "omit_reason","Interpretation_of_results","InterpretationBasis","FinalScore"};

	/**
	 * Create record based on header list and data list in csv using reflection to assign by header name
	 * 
	 * @param hlist
	 * @param list
	 * @return
	 */
	public static RecordEchemportal2 createRecord(List<String> hlist, List<String> list) {
		RecordEchemportal2 r=new RecordEchemportal2();
		//convert to record:
		try {
			for (int i=0;i<hlist.size();i++) {
				
				Field myField =r.getClass().getField(hlist.get(i));
				myField.set(r, list.get(i));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return r;
	}

	public static String getHeader() {
		String str="";
		for (int i=0;i<varlist.length;i++) {
			str+=varlist[i];
			if (i<varlist.length-1) str+="\t";
		}
		return str;
	}

	public static String getHeaderCSV() {
		String str="";
		for (int i=0;i<varlist.length;i++) {
			str+="\""+varlist[i]+"\"";
			if (i<varlist.length-1) str+=",";
		}
		return str;
	}
	
	public String toString() {
		String str="";
		for (int i=0;i<varlist.length;i++) {
			try {
				Field myField =this.getClass().getField(varlist[i]);				
				str+=myField.get(this);
				if (i<varlist.length-1) str+="\t";
			} catch (Exception ex){
				ex.printStackTrace();
			}
		}

		return str;
	}
	
	public static void writeToFile(String filepath,Vector<RecordEchemportal2>records) {
		try {
			
//			FileWriter fw=new FileWriter(filepath,StandardCharsets.UTF_8);
			FileWriter fw=new FileWriter(filepath);
			fw.write(RecordEchemportal2.getHeader()+"\r\n");
			
			for (RecordEchemportal2 r:records) {
				fw.write(r+"\r\n");
			}
			
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public String toCSV() {
		String str="";
		for (int i=0;i<varlist.length;i++) {
			try {
				Field myField =this.getClass().getField(varlist[i]);				
				str+="\""+myField.get(this)+"\"";
				if (i<varlist.length-1) str+=",";
			} catch (Exception ex){
				ex.printStackTrace();
			}
		}

		return str;
	}
	
	public static Hashtable <String,RecordEchemportal2> loadRecordsFromFile(String filepath,String ID,String del) {
		Hashtable <String,RecordEchemportal2>records=new Hashtable<>();
		
		try {
			Scanner scanner = new Scanner(new File(filepath));
			String header=scanner.nextLine();
			
//			System.out.println(header);
			
			List <String>hlist=Utilities.Parse3(header, del);
			 
			while (scanner.hasNext()) {
				String Line=scanner.nextLine();
				List <String>list=Utilities.Parse3(Line, del);
								
				if (list.size()==hlist.size()-1) list.add("");//add empty omitReason
//				System.out.println(list.size()+"\t"+hlist.size());
				
				RecordEchemportal2 r=RecordEchemportal2.createRecord(hlist,list);
//				System.out.println(Line);
				records.put(r.CAS_final, r);			
	        }
			scanner.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}
	
	public static Multimap <String,RecordEchemportal2> loadRecordsFromFileWithDuplicates(String filepath,String ID,String del) {
		Multimap <String,RecordEchemportal2>records=HashMultimap.create();
		
		try {
			Scanner scanner = new Scanner(new File(filepath));
			String header=scanner.nextLine();
			List <String>hlist=Utilities.Parse3(header, del);
			 
			while (scanner.hasNext()) {
				String Line=scanner.nextLine();
				List <String>list=Utilities.Parse3(Line, del);
								
//				System.out.println(Line);
//				System.out.println(list.size()+"\t"+hlist.size());
				
				RecordEchemportal2 r=RecordEchemportal2.createRecord(hlist,list);
//				System.out.println(Line);
				records.put(r.CAS_final, r);			
	        }
			scanner.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}

	
	String getJSON() {
		GsonBuilder builder = new GsonBuilder();
//		builder.setPrettyPrinting().serializeNulls();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		return gson.toJson(this);
	}


}
