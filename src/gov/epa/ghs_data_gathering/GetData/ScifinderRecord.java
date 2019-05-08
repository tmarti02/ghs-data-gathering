package gov.epa.ghs_data_gathering.GetData;

import java.lang.reflect.Field;

public class ScifinderRecord {

	public String Registry_Number;
	public String CA_Index_Name;
	public String Other_Names;
	public String Formula;
	public String Alternate_Formula;
	public String Class_Identifier;
	public String Alternate_Registry_Numbers;
	public String Deleted_Registry_Numbers;
		
	public String [] varlist={ "Registry_Number", "CA_Index_Name", "Other_Names", "Formula","Class_Identifier",
		"Alternate_Registry_Numbers", "Deleted_Registry_Numbers"};
	
		public String getHeader() {
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
					String strVal=(String)myField.get(this);				
					str+=strVal;
					if (i<varlist.length-1) str+="\t";
				} catch (Exception ex){
					ex.printStackTrace();
				}
			}

			return str;
		}
	}