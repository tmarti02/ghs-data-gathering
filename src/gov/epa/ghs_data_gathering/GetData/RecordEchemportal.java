package gov.epa.ghs_data_gathering.GetData;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

import com.google.common.collect.HashMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.ghs_data_gathering.GetData.ECHA_IUCLID.RecordECHA;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

import com.google.common.collect.Multimap;


public class RecordEchemportal  {

	//From main section:

	public String CAS_final="";
	public String CAS_warning="";
	
	public String Record_Number="";
	public String Substance_Name="";
	public String Name_Type="";
	public String Substance_Number="";
	public String Number_type="";
	public String Member_of_Category="";
	public String Substance_Link="";
	public String Participant="";
	public String Participant_Link="";
	public String Section="";
	public String Endpoint_Link="";
	public String Study_result_type="";
	public String Reliability="";
	public String Reference_Year="";
	public String Type_of_method="";
	public String Type_of_study="";
	public String Test_guideline_Qualifier="";
	public String GLP_Compliance="";
	public String Test_guideline_Guideline="";
	public String Species="";
	public String Strain="";
	public String Interpretation_of_results="";
	
	public String formula="";
	public String omit_reason="";
	

	static String[] varlist = { "CAS_final","CAS_warning","Record_Number", "Substance_Name", "Name_Type", "Substance_Number", "Number_type",
			"Member_of_Category", "Substance_Link", "Participant", "Participant_Link", "Section", "Endpoint_Link",
			"Study_result_type", "Reliability", "Reference_Year", "Type_of_method", "Type_of_study",
			"Test_guideline_Qualifier", "Test_guideline_Guideline", "Species", "Strain",
			"Interpretation_of_results","formula","omit_reason" };


	/**
	 * Create record based on header list and data list in csv using reflection to assign by header name
	 * 
	 * @param hlist
	 * @param list
	 * @return
	 */
	public static RecordEchemportal createRecord(List<String> hlist, List<String> list) {
		RecordEchemportal r=new RecordEchemportal();
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
	
	public static Hashtable <String,RecordEchemportal> loadRecordsFromFile(String filepath,String ID,String del) {
		Hashtable <String,RecordEchemportal>records=new Hashtable<>();
		
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
				
				RecordEchemportal r=RecordEchemportal.createRecord(hlist,list);
//				System.out.println(Line);
				records.put(r.CAS_final, r);			
	        }
			scanner.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}
	
	public static Multimap <String,RecordECHA> loadRecordsFromFileWithDuplicates(String filepath,String ID,String del) {
		Multimap <String,RecordECHA>records=HashMultimap.create();
		
		try {
			Scanner scanner = new Scanner(new File(filepath));
			String header=scanner.nextLine();
			List <String>hlist=Utilities.Parse3(header, del);
			 
			while (scanner.hasNext()) {
				String Line=scanner.nextLine();
				List <String>list=Utilities.Parse3(Line, del);
								
//				System.out.println(Line);
//				System.out.println(list.size()+"\t"+hlist.size());
				
				RecordECHA r=RecordECHA.createRecord(hlist,list);
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
