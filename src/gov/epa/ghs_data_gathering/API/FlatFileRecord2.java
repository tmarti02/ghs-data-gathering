package gov.epa.ghs_data_gathering.API;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import gov.epa.ghs_data_gathering.GetData.RecordChembench;
import gov.epa.ghs_data_gathering.Utilities.Utilities;


public class FlatFileRecord2 {
	
	public String CAS;
	public String name;
	public String CID;
	
	public String source;// where the record came from
	//Extra fields for pubchem:
	public String sourceID;// where the record came from
	public String concentration;
	public String referenceNumber;
	public String URL;

	public String score;// i.e. L,M,H,VH
	public String hazard_name;
	
	public String category;// i.e. Category 1
	public String hazard_code;// code for hazard, i.e. "H301"
	public String hazard_statement;// text based description of what hazard they think it is
	public String rationale;// why classification was assigned
	public String route;// i.e. oral, dermal, inhalation- used mainly for acute mammalian toxicity for
						// now
	public String note;// extra clarification that doesn't fit into above fields
	public String note2;// extra clarification that doesn't fit into above fields
	
	
	// **************************************************************************************
	public Double valueMass;// quantitative value in mass units such as mg/L
	public String valueMassUnits;
	public String valueMassOperator;// "<",">", or ""
	public String percentage;

//	public static String[] fieldNames = { "CAS","CID","referenceNumber","sourceName","sourceID","name","URL","concentration","hazard_name", "category", "hazard_code",
//			"hazard_statement", "note"};

	public static String[] fieldNames = { "CAS","CID","referenceNumber","source","sourceID","name","URL","hazard_name","hazard_code","percentage","hazard_statement", "note"};
	
	
	
	public static String getHeader() {
		return getHeader("\t");
	}
	

	
	public static String getHeader(String d) {
		// TODO Auto-generated method stub

		String Line = "";
		for (int i = 0; i < fieldNames.length; i++) {
//			Line += "\""+fieldNames[i]+"\"";
			Line += fieldNames[i];
			if (i < fieldNames.length - 1) {
				Line += d;
			} 
		}

		return Line;
	}
	
	/**
	 * Create record based on header list and data list in csv using reflection to assign by header name
	 * 
	 * @param hlist
	 * @param list
	 * @return
	 */
	public static FlatFileRecord2 createRecord(List<String> hlist, List<String> list) {
		FlatFileRecord2 r=new FlatFileRecord2();
		//convert to record:
		try {
			for (int i=0;i<list.size();i++) {
				Field myField =r.getClass().getField(hlist.get(i));
				myField.set(r, list.get(i));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return r;
	}
	
	public static void writeToFile(Vector<FlatFileRecord2>records,String destFilePath,String del,String source) {
		
		try {
			
			FileWriter fw=new FileWriter(destFilePath);
			
			fw.write(FlatFileRecord2.getHeader(del)+"\r\n");
			
			Vector<String>uniqueCAS=new Vector<>();
			
			int counter=0;
			
			for (FlatFileRecord2 record:records) 
				if (record.source.contentEquals(source)) {
					fw.write(record.toString(del)+"\r\n");
					if (!uniqueCAS.contains(record.CAS)) uniqueCAS.add(record.CAS);
					counter++;
				}

			fw.flush();
			fw.close();
			
//			System.out.println("Source="+source+"\tNumber of unique chemicals="+uniqueCAS.size()+"\tNumber of records="+counter);
			System.out.println(source+"\t"+uniqueCAS.size()+"\t"+counter);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	public static Vector <FlatFileRecord2> loadRecordsFromFile(String filepath,String ID,String del) {
		
		Vector <FlatFileRecord2>records=new Vector();
		
		String Line="";
		
		try {
			Scanner scanner = new Scanner(new File(filepath));
			String header=scanner.nextLine();
			List <String>hlist=Utilities.Parse(header, del);
			 
			while (scanner.hasNext()) {
				Line=scanner.nextLine();
				if (Line==null) break;
				List <String>list=Utilities.Parse(Line, del);
				FlatFileRecord2 r=FlatFileRecord2.createRecord(hlist,list);
				
//				String valueID=list.get(hlist.indexOf(ID));
//				records.put(valueID, r);
				records.add(r);
				
	        }
			scanner.close();
			
		} catch (Exception ex) {
			System.out.println(ex.getMessage()+"\t"+Line);
			ex.printStackTrace();
		}
		return records;
	}
	
	public String toString() {
		return toString("\t");
	}
	
	//convert to string by reflection:
	public String toString(String d) {
		
		String Line = "";
		for (int i = 0; i < fieldNames.length; i++) {
			try {
			
				
				Field myField = this.getClass().getDeclaredField(fieldNames[i]);
				
				String val=null;
				
				if (fieldNames[i].equals("valueMass")) {
					if (myField.get(this)==null) {
						val="";	
					} else {
						val=(Double)myField.get(this)+"";
					}
					
				} else {
					if (myField.get(this)==null) {
//						val="\"\"";
						val="";
					} else {
//						val="\""+(String)myField.get(this)+"\"";
						val=(String)myField.get(this);
					} 
				}
				
				val=val.replace("\r\n","<br>");
				val=val.replace("\n","<br>");
				
//				if (fieldNames[i].equals("note")) {
//					System.out.println(CAS+"\t"+source+"\t"+hazard_name+"\t"+val);
//				}

				if (val.contains(d)) {
					System.out.println(this.CAS+"\t"+fieldNames[i]+"\t"+val+"\thas delimiter");
				}
				
				Line += val;
				if (i < fieldNames.length - 1) {
					Line += d;
				}
			
			
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		Line=Line.replaceAll("\0", "");
		return Line;

	}
	
}
