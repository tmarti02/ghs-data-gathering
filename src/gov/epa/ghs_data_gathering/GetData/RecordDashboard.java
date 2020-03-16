package gov.epa.ghs_data_gathering.GetData;

import java.lang.reflect.Field;


public class RecordDashboard {
	
	public String INPUT;
	public String FOUND_BY;
	public String DTXSID;
	public String PREFERRED_NAME;
	public String CASRN;
	public String INCHIKEY;
	public String IUPAC_NAME;
	public String SMILES;
	public String INCHI_STRING;
	public String MOLECULAR_FORMULA;
	public String QSAR_READY_SMILES;

	
	static String[] varlist = { "INPUT","FOUND_BY","DTXSID","PREFERRED_NAME","CASRN","INCHIKEY","IUPAC_NAME","SMILES","INCHI_STRING","MOLECULAR_FORMULA","QSAR_READY_SMILES"};
	
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