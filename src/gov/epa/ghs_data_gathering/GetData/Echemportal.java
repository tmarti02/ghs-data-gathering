package gov.epa.ghs_data_gathering.GetData;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.Vector;


public class Echemportal {

	class Record {
				
		String Substance_Name;//0
		String Name_Type;//1
		String Substance_Number;//2
		String Number_type;//3
		String Member_of_Category;//4
		String Substance_Link;//5
		String Participant;//6
		String Participant_Link;//7
		String Section;//8
		String Endpoint_Link;//9
		String Values;//10		
		
		//Values data:
		String Type_of_information;
		String Reliability;
		String Endpoint;
		String Strain;
		String Species;
		String Type_of_study;
		String GLP_compliance;
		String Interpretation_of_results;
		String Reference_Year;
		String Test_guideline;
		String Migrated_information;
		String Criteria_used_for_interpretation_of_results;
		
		
		String[] vars = { "Substance_Number", "Number_type","Substance_Name", "Name_Type",  
				"Member_of_Category",
				"Substance_Link","Participant","Participant_Link","Section","Endpoint_Link",
				"Type_of_information","Type_of_study","Test_guideline","Reliability",
				"Endpoint","Strain","Species",
				"GLP_compliance","Interpretation_of_results","Criteria_used_for_interpretation_of_results","Migrated_information","Reference_Year",};
		
		
		String getHeader(String del,String []vars) {
			
			String header="";
			for (int i=0;i<vars.length;i++) {
				header+=vars[i];
				if (i<vars.length-1) {
					header+=del;
				}
			}
			return header;
		}
		
		String toString(String del,String [] vars) {
			
			String vals="";

			try {
				for (int i=0;i<vars.length;i++) {
					Field field =this.getClass().getDeclaredField(vars[i]);
					String val="";
//					System.out.println(field.getType().getName());

					if (field.getType().getName().equals("java.lang.String")) {
						if (field.get(this)!=null)
							val=field.get(this)+"";						
					}
					
					vals+=val;
					
					if (i<vars.length-1) {
						vals+=del;
					}
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}

			return vals;
			
		}
		
		
		
		public void assignValue(String key,String value) {
			
			try {
				
				String key2=key.replace(" ", "_").replace(",","");
				
				Field field =this.getClass().getDeclaredField(key2);
				
				String oldvalue=(String)field.get(this);
				
//				System.out.println("**"+oldvalue);
				
				
				if (oldvalue!=null) {
//					System.out.println("*"+oldvalue);
					field.set(this, oldvalue.trim()+"; "+value.trim());
					
				} else {
					field.set(this, value.trim());	
				}
				
				
			
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		
	}
	
	/**
	 * Split line based on delimiter, accounts for having quotation marks to avoid extra fields for name
	 * 
	 * @param Line
	 * @param Delimiter
	 * @return
	 */
	public static Vector<String> split(String Line, String Delimiter) {

	    Vector<String> myList = new Vector<String>();

	    if (Line.indexOf("\"")==0) {
	    	Line=Line.substring(1, Line.length());
	    	String name=Line.substring(0,Line.indexOf("\""));
//	    	System.out.println(name);
	    	
	    	Line=Line.substring(Line.indexOf("\";")+2,Line.length());
	    	myList.add(name);
	    } 
	    
	    String [] vals=Line.split(";");
	    	
	    for (int i=0;i<vals.length;i++) {
	    	if (vals[i].indexOf("\"")==0) {	    		
	    		vals[i]=vals[i].substring(1, vals[i].length());
//	    		System.out.println("have quote!!!");
	    	}
	    	myList.add(vals[i]);
	    }
	    		    	    
//	    for (int i=0; i<myList.size();i++) {
//	    	System.out.println(i+"\t"+myList.get(i));
//	    }
	    return myList;

	  }
	
	void parseFile(String folder,String inputFileName,String outputFileName) {
				
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(folder+"/"+inputFileName));
			FileWriter fw=new FileWriter(folder+"/"+outputFileName);
			
			String header=br.readLine();
//			System.out.println(header);
						
			Record r=new Record();
			
			fw.write(r.getHeader("\t", r.vars)+"\r\n");
			
			int counter=0;
			
			while (true) {
				String chemLine="";
				
				while (chemLine.isEmpty()) {
					chemLine=br.readLine();
					if (chemLine==null) break;
				}
				
				if (chemLine==null) break;
				
				counter++;
				
				Record record=parseChemLine(chemLine);
				
//				System.out.println("chemLine="+chemLine);

				//				System.out.println(counter+"\t"+chemical.Number_type+"\t"+chemical.Substance_Number);
//				System.out.println(counter+"\t"+chemical.Endpoint_Link);

				//get rest of data for Values:
				while (true) {
					String Line=br.readLine();
					//	System.out.println(Line);
					if (Line==null) break;
					if (Line.isEmpty()) continue;
					record.Values+="\n"+Line.replace("\"", "");
					if (Line.contains("\"") && !Line.contains("\"\"")) break;
				}
				
//				System.out.println("@"+record.Values+"#");
				
				parseValues(record);
				
				fw.write(record.toString("\t", record.vars)+"\r\n");
				fw.flush();
				
//				System.out.println(counter+"\t"+chemical.Substance_Number+"\t*"+chemical.Values+"*");
			}
			
			
			
			fw.close();
			br.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
				
	}

	/** Parse chemical metadata line
	 * 
	 * @param chemLine
	 * @return
	 */
	Record parseChemLine(String chemLine) {
		Vector<String>vals=split(chemLine, ";");

		Record record=new Record();				
		record.Substance_Name=vals.get(0);
		record.Name_Type=vals.get(1);
		record.Substance_Number=vals.get(2);
		record.Number_type=vals.get(3);
		record.Member_of_Category=vals.get(4);
		record.Substance_Link=vals.get(5);
		record.Participant=vals.get(6);
		record.Participant_Link=vals.get(7);
		record.Section=vals.get(8);
		record.Endpoint_Link=vals.get(9);
		record.Values=vals.get(10);

		if (vals.size()!=11)  {
			System.out.println("Dont have 11 fields:");
			for (int i=0; i<vals.size();i++) {
				System.out.println(i+"\t"+vals.get(i));
			}
		}
		return record;
		
	}
	
	/**
	 * Parse the key-value pairs that are in the Values variable
	 * 
	 * @param record
	 */
	void parseValues (Record record) {
				
		String [] vals=record.Values.split("\n");
		
		for (int i=0;i<vals.length;i++) {			
			if (!vals[i].isEmpty() && vals[i].contains(":")) {
				
				String s=vals[i];
				String key=s.substring(0, s.indexOf(":"));
				String value=s.substring(s.indexOf(":")+2,s.length());
				
				if (key.equals("Type of information") || key.equals("Reliability") 
						|| key.equals("Endpoint") || key.equals("Strain") || key.equals("Species") || 
						key.equals("Type of study") || key.equals("GLP compliance") || 
						 key.equals("Reference, Year")) {
					record.assignValue(key, value);
				
				} else if (key.equals("Interpretation of results") ) {
					
					if (value.contains("Migrated information")) {
						record.assignValue("Migrated information", "y");
						value=value.replace("Migrated information","");
					}
					
					if (value.contains("Criteria used for interpretation of results:")) {
						
						String search="Criteria used for interpretation of results:";
						String criteria=value.substring(value.indexOf(search)+search.length(), value.length()).trim();
//						System.out.println(criteria);
						
						record.assignValue("Criteria used for interpretation of results", criteria);
						value=value.substring(0,value.indexOf("Criteria used"));
						
					}
					
					//TODO: add code to account for "other:" in the interpretation of results field
					
					record.assignValue(key, value);
					
				} else if (key.equals("Test guideline, Qualifier")) {
					
					s=vals[i+1];//retrieve info for guideline
					
					String key2=s.substring(0, s.indexOf(":"));
					String value2=s.substring(s.indexOf(":")+2,s.length());

					record.assignValue("Test guideline", value+" "+value2);
					
					i++;//skip guideline since accounted for above
					
				} else if (key.equals("Test guideline, Guideline")) {
//					System.out.println(record.Substance_Number+"\t"+key+"\t"+value);
					record.assignValue("Test guideline",value);
				} else {
					System.out.println(i+"\t"+key+"\t"+value);	
//					System.out.println(key+"\t"+value);
				}
				
			
			} else if (!vals[i].isEmpty()) {
				System.out.println("have line without colon\t"+vals[i]);
			}
		}
		
		
//		System.out.println(record.Substance_Number+"\t"+record.Interpretation_of_results);
		
		
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Echemportal e=new Echemportal();
		
		String folder="AA Dashboard/Data/ECHA";
		String inputFileName="sample skin sens data.csv";
		String outputFileName="sample skin sens data parsed.txt";
		
		e.parseFile(folder,inputFileName,outputFileName);

	}

}
