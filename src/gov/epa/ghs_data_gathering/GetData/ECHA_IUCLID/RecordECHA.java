package gov.epa.ghs_data_gathering.GetData.ECHA_IUCLID;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

import com.google.common.collect.HashMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.ghs_data_gathering.Utilities.Utilities;

import com.google.common.collect.Multimap;


public class RecordECHA  {

	//From main section:

	public String record_number="";

	public String chemical_name="";
	public String EC_number="";

	public String CAS_number="";
	public String CAS_final="";
	public String CAS_final_source="";
	public String CAS_warning="";

	public String formula="";

	//From administrative data:
	public String purpose_flag="";
	public String study_result_type="";
	public String reliability="";

	//from materials and methods
	public String methods="";
	public String type_of_study="";
	public String endpoint="";
	public String species="";
	public String test_material_details="";
	public String test_material_same_as_section_one="";
	public String OECD_guideline="";

	public String test_material_CAS_number="";
	public String test_material_EC_number="";
	public String test_material_EC_name="";
	public String test_material_IUPAC_name="";

	public String interpretation_of_results="";
	public String omit_reason="";


	static String[] varlist = { "record_number","chemical_name", "EC_number", "CAS_number","CAS_final","CAS_final_source","CAS_warning","formula", 
			"test_material_CAS_number",	"test_material_EC_number","test_material_EC_name",
			"test_material_IUPAC_name",	"test_material_details","test_material_same_as_section_one", 
			"purpose_flag", "study_result_type",
			"reliability", "methods", "type_of_study", "species", 
			"OECD_guideline", 				
	"interpretation_of_results","omit_reason" };



	/**
	 * Create record based on header list and data list in csv using reflection to assign by header name
	 * 
	 * @param hlist
	 * @param list
	 * @return
	 */
	public static RecordECHA createRecord(List<String> hlist, List<String> list) {
		RecordECHA r=new RecordECHA();
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
	
	public static Hashtable <String,RecordECHA> loadRecordsFromFile(String filepath,String ID,String del) {
		Hashtable <String,RecordECHA>records=new Hashtable<>();
		
		try {
			Scanner scanner = new Scanner(new File(filepath));
			String header=scanner.nextLine();
			List <String>hlist=Utilities.Parse3(header, del);
			 
			while (scanner.hasNext()) {
				String Line=scanner.nextLine();
				List <String>list=Utilities.Parse3(Line, del);
								
				if (list.size()==hlist.size()-1) list.add("");//add empty omitReason
				
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
