package gov.epa.ghs_data_gathering.GetData;

import java.lang.reflect.Field;


public class RecordChemReg {

	public String Lookup_Result;
	public String Query_Casrn;
	public String Query_Name;
	public String Top_HIT_DSSTox_Substance_Id;
	public String Top_Hit_Casrn;
	public String Top_Hit_Name;
	public String Validated;

	
	static String[] varlist = { "Lookup_Result", "Query_Casrn", "Query_Name", 
			"Top_HIT_DSSTox_Substance_Id","Top_Hit_Casrn","Top_Hit_Name","Validated"};
	
	public static String getHeader(String [] varlist) {
		String str="";
		for (int i=0;i<varlist.length;i++) {
			str+=varlist[i];
			if (i<varlist.length-1) str+="\t";
		}
		return str;
	}
	
	public static String getHeader() {
		return getHeader(varlist);
	}
	
	public void setValue(String fieldName,String fieldValue) {
		
		try {
			Field myField =this.getClass().getField(fieldName);				
		
			myField.set(this, fieldValue);
			
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
	}
	
	public String toString(String[] varlist) {
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
	
	public String toString() {
		return toString(varlist);
	}

}