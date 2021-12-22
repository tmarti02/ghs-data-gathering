package gov.epa.ghs_data_gathering.Parse.Links.SVHC_Authorisation_List;

import java.util.HashMap;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.exp_data_gathering.parse.ExcelSourceReader;
import gov.epa.ghs_data_gathering.Parse.Links.RecordLink;

public class RecordSVHC_Authorisation_List {

	private static String sourceName="SVHC Authorisation List";
	private static final String fileName = "SVHC Authorization List.xlsx";
	
	String CAS;
	String EC_Number;
	String Name;
	String Name_url;
	String Details_url;
	
	public static final String[] fieldNames = {"Name","Name_url","EC_Number","CAS","Details_url"};
	
	public static Vector<JsonObject> parseRecordsFromExcel() {
		String mainFolderPath="AA Dashboard\\Data";
		ExcelSourceReader esr = new ExcelSourceReader(fileName, mainFolderPath,sourceName);
		
//		esr.createClassTemplateFiles();		
		
		HashMap<Integer,String> hm = esr.generateDefaultMap(fieldNames, 0);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(hm, 0);
		return records;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Vector<JsonObject> records =parseRecordsFromExcel();

		
		 Gson gson = new GsonBuilder().setPrettyPrinting().create();

		 
		for (JsonObject record:records) {
			RecordSVHC_Authorisation_List r = gson.fromJson(record.toString(),RecordSVHC_Authorisation_List.class);
		    
//			System.out.println(r.CAS+"\t"+r.Name+"\t"+r.Name_url+"\t"+r.details_url);
//			System.out.println(r.CAS+"\t"+r.Name+"\t"+r.Name_url+"\t"+r.Details_url);
//			System.out.println(json);
			
			if (r.Name.contains("EC No.")) {
				String oldName=r.Name;
				r.Name=oldName.substring(0,oldName.indexOf("EC No.")).replace("&nbsp;","");
				
				if (r.Name.contains("(9016-45-9)")) {
					r.Name=r.Name.replace("(9016-45-9)", "").trim();
					r.CAS="9016-45-9";
				} else if (r.Name.contains("(CAS 9016-45-9)")) {
					r.Name=r.Name.replace("(CAS 9016-45-9)", "").trim();
					r.CAS="9016-45-9";
				} else if (r.Name.contains("(CAS# 68412-54-4)")) {
					r.Name=r.Name.replace("(CAS 68412-54-4)", "").trim();
					r.CAS="68412-54-4";
				} else if (r.Name.contains("(CAS: 127087-87-0)")) {
					r.Name=r.Name.replace("(CAS 127087-87-0)", "").trim();
					r.CAS="127087-87-0";
				} else {
					if (oldName.contains("CAS No.:&nbsp;")) {
						r.CAS=oldName.substring(oldName.indexOf("CAS No.:&nbsp;")+"CAS No.:&nbsp;".length(),oldName.length());
//						System.out.println(r.CAS);	
					} else {
						System.out.println(oldName);
					}
				}
//				System.out.println(r.Name);
			}
			
			
			RecordLink rlName=new RecordLink();
			rlName.Name=r.Name;
			rlName.CAS=r.CAS;
			rlName.SourceName=sourceName;
			rlName.LinkName="Substance Infocard";
			rlName.URL=r.Name_url;

			
			RecordLink rlDetails=new RecordLink();
			rlDetails.Name=r.Name;
			rlDetails.CAS=r.CAS;
			rlDetails.SourceName=sourceName;
			rlDetails.LinkName="Details";
			rlDetails.URL=r.Details_url;

		    String json = gson.toJson(rlName);
		    System.out.println(rlDetails.toString("\t"));
//		    System.out.println(rlDetails);
			
		}

	}


}
