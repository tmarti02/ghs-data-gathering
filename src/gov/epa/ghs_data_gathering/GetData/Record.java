package gov.epa.ghs_data_gathering.GetData;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.LinkedList;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.ghs_data_gathering.Utilities.Utilities;


/**
 * Generic hashtable version
 * 		
 * @author TMARTI02
 *
 */
public class Record {

	Hashtable<String,String>htVals;
	
	
	String get(String fieldName) {
		return htVals.get(fieldName);
	}
	
	void set(String fieldName,String value) {
		htVals.put(fieldName,value);
	}

	String getJSON() {
		GsonBuilder builder = new GsonBuilder();
//		builder.setPrettyPrinting().serializeNulls();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		return gson.toJson(htVals);
	}
	
	
	/**
	 * Create record based on header list and data list in csv using reflection to assign by header name
	 * 
	 * @param hlist
	 * @param list
	 * @return
	 */
	public static Record createRecord(LinkedList<String> hlist, LinkedList<String> list) {
		Record r=new Record();
		
		r.htVals=new Hashtable<String,String>();
		
		//convert to record:
		try {
			for (int i=0;i<hlist.size();i++) {
				r.htVals.put(hlist.get(i),list.get(i));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return r;
	}
	
	public static Hashtable <String,Record> loadRecordsFromFile(String filepath,String ID,String del) {
		Hashtable <String,Record>records=new Hashtable<>();
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));

			String header=br.readLine();
			LinkedList <String>hlist=Utilities.Parse(header, del);
			
			while (true) {
				
				String Line=br.readLine();
				if (Line==null) break;
				
				LinkedList <String>list=Utilities.Parse3(Line, del);
				
				Record r=Record.createRecord(hlist,list);
//				System.out.println(cr);
				records.put(r.get(ID), r);
			}
			
			br.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return records;
		
	}
	
	
	
	
}
