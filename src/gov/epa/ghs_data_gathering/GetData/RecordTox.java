package gov.epa.ghs_data_gathering.GetData;

import java.lang.reflect.Field;

/**
 * Common format for tox data from different sources so can use same code to merge with dashboard records
 * 
 * @author TMARTI02
 *
 */
public class RecordTox {

	//Identifiers from original source (used to map ChemReg/DSSTOX records)
	//To map to chemreg- use CAS+Name as key:
	public String CAS;
	public String chemicalName;
	String SMILES;//Needed?
	
	boolean isBinary=true;//needed?
	public int binaryResult=-1;//1 = positive, 0 = negative, -1 = ambiguous
	
	double continuousResult;
	String continuousUnits;
			
	
	
	public String toString() {
		return toString(varlist);
	}
	
	static String[] varlist = { "CAS","chemicalName","binaryToxResult"};

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
}
