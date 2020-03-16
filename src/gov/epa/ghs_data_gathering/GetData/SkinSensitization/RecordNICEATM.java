package gov.epa.ghs_data_gathering.GetData.SkinSensitization;

import java.lang.reflect.Field;
import java.util.Comparator;

import org.openscience.cdk.AtomContainer;

public class RecordNICEATM {

	public String Chemical_Name;
	public String CASRN;
	public String Molecular_Weight;
	public String Chemical_Class;
	public String LLNA_Vehicle;
	public String EC3;
	public String LLNA_Result;
	public String Class;
	public String Reference;
	public String Smiles;
	
	static String[] varlist = { "Chemical_Name","CASRN","Molecular_Weight","Chemical_Class","Smiles",
			"LLNA_Vehicle","EC3","LLNA_Result","Class","Reference"};

	static String[] varlist2 = { "CASRN","Chemical_Name","Molecular_Weight","LLNA_Result","Reference"};

	
//	class CustomComparator implements Comparator<RecordNICEATM>{
//	    int col;
//		
//		public int compare(RecordNICEATM ac1,RecordNICEATM ac2) {	        
//    		String strval1=(String)ac1.Chemical_Name;
//    		String strval2=(String)ac2.Chemical_Name;
//    		return strval1.compareTo(strval2);	
//	    }
//	}
	
	public static String getHeader(String [] varlist) {
		String str="";
		for (int i=0;i<varlist.length;i++) {
			str+=varlist[i];
			if (i<varlist.length-1) str+="\t";
		}
		return str;
	}
	public static String getHeader2() {
		return getHeader(varlist2);
	}
	
	public static String getHeader() {
		return getHeader(varlist);
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