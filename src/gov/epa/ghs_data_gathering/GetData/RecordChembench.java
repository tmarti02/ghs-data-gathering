package gov.epa.ghs_data_gathering.GetData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.ghs_data_gathering.Utilities.Utilities;

public class RecordChembench {
	

	public String Compound_name="";
	public String CASRN="";
	public String LLNA_result="";
	public String LLNA_class="";
	public String LLNA_reference="";
	public String Chembench_Name="";
	public String smiles="";

	transient static String[] varlist = { "CASRN","Compound_name", "LLNA_result", "LLNA_class", "LLNA_reference",
			"Chembench_Name", "smiles" };

	/**
	 * Create record based on header list and data list in csv using reflection to assign by header name
	 * 
	 * @param hlist
	 * @param list
	 * @return
	 */
	public static RecordChembench createRecord(List<String> hlist, List<String> list) {
		RecordChembench r=new RecordChembench();
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
	

	public static List<String>readFile(String filepath) {
		List<String>list=new LinkedList<>();
		try {
			Scanner scanner = new Scanner(new File(filepath));

			while (scanner.hasNext()) {
				String Line=scanner.nextLine();
				list.add(Line);
	        }
			scanner.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return list;
	}

	
	
//	public static Hashtable <String,RecordChembench> loadRecordsFromFile(String filepath,String ID,String del) {
//		Hashtable <String,RecordChembench>records=new Hashtable<>();
//		
//		try {
//			Scanner scanner = new Scanner(new File(filepath));
//			String header=scanner.nextLine();
//			List <String>hlist=Utilities.Parse3(header, del);
//			 
//			while (scanner.hasNext()) {
//				String Line=scanner.nextLine();
//				List <String>list=Utilities.Parse3(Line, del);
//				RecordChembench r=RecordChembench.createRecord(hlist,list);
////				System.out.println(Line);
//				records.put(r.CASRN, r);			
//	        }
//			scanner.close();
//			
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		return records;
//	}
	
	public static Hashtable <String,RecordChembench> loadRecordsFromFile(String filepath,String ID,String del) {
		Hashtable <String,RecordChembench>records=new Hashtable<>();
		
		try {
			Scanner scanner = new Scanner(new File(filepath));
			String header=scanner.nextLine();
			List <String>hlist=Utilities.Parse3(header, del);
			 
			while (scanner.hasNext()) {
				String Line=scanner.nextLine();
				List <String>list=Utilities.Parse3(Line, del);
				RecordChembench r=RecordChembench.createRecord(hlist,list);
//				System.out.println(Line);
				records.put(r.CASRN, r);			
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
	
	String getJSON_OneLine() {
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		return gson.toJson(this);
	}
	
	
}