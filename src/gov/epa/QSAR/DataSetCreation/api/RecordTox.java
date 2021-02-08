package gov.epa.QSAR.DataSetCreation.api;

import java.lang.reflect.Field;

import org.openscience.cdk.AtomContainer;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.QSAR.DataSetCreation.DSSTOX;
import gov.epa.QSAR.utilities.MolFileUtilities;

/**
 * Common format for tox data from different sources so can use same code to merge with dashboard records
 * 
 * @author TMARTI02
 *
 */
public class RecordTox {

	//Identifiers from original source (used to map ChemReg/DSSTOX records)
	//To map to chemreg- use CAS+Name as key:
	public String record_ID;
	public String source;
	
	public String casrn_Source;
	public String name_Source;	
	public String smiles_Source;//Needed?
		
	public String DTXSID;
	public String casrn_DSSTox;
	public String name_DSSTox;
	public String smiles_DSSTox;
	public String smiles_DSSTox_QSAR_Ready;
	
	public boolean validated=true;

//	public boolean isSalt=false;
//	public boolean hasStructure=true;
//	public boolean hasSpecialCharacter=false;
//	public boolean haveBadElement=false;
//	public boolean haveCarbon=true;
	
//	boolean isBinary=true;//needed?
//	public int binaryResult=-1;//1 = positive, 0 = negative, -1 = ambiguous
//	
//	double continuousResult;
//	String continuousUnits;
			
	
	public double Tox;
	public String ToxUnits;
	
	public String toJSON() {
		GsonBuilder builder = new GsonBuilder();
//		builder.setPrettyPrinting().serializeNulls();
		builder.setPrettyPrinting();
//		builder.disableHtmlEscaping();
				
		Gson gson = builder.create();
		return gson.toJson(this);	
	}
	
	
	public boolean isRecordOK_OriginalSmiles() {

		
		if (!validated) return false;
		
		if (smiles_DSSTox==null || smiles_DSSTox.isEmpty()) return false;// no structure
				
		if (smiles_DSSTox.contains("|") || smiles_DSSTox.contains("*"))  return false;//has reaction or ambiguous atoms
		
		if (smiles_DSSTox.contains(".")) return false;//salt
		
		AtomContainer acSmilesDSSTox=DSSTOX.getAtomContainer(smiles_DSSTox);

		if (MolFileUtilities.HaveBadElement(acSmilesDSSTox)) return false;		
		if (!MolFileUtilities.HaveCarbon(acSmilesDSSTox)) return false;
		
		
		return true;
	}
	
	public String toString() {
		return toString(varlist);
	}
	
//	static String[] varlist = { "record_ID","source","casrn_Source","name_Source","smiles_Source","validated","DTXSID","casrn_DSSTox","name_DSSTox","smiles_DSSTox","smiles_DSSTox_QSAR_Ready","Tox","ToxUnits",
//			"hasSpecialCharacter","haveBadElement","haveCarbon","isSalt","hasStructure"};

	
	static String[] varlist = { "record_ID","source","casrn_Source","name_Source","smiles_Source","validated",
			"DTXSID","casrn_DSSTox","name_DSSTox","smiles_DSSTox","smiles_DSSTox_QSAR_Ready","Tox","ToxUnits"};

	
	public void setValue(String fieldName,String fieldValue) {
		
		try {
			Field myField =this.getClass().getField(fieldName);	
			
//			System.out.println(myField.getType().getName()+"\t"+fieldName+"\t"+fieldValue);
		
			if (myField.getType().getName().contentEquals("boolean")) {
				myField.setBoolean(this, new Boolean(fieldValue));
			} else if (myField.getType().getName().contentEquals("double")) {
				myField.setDouble(this, Double.parseDouble(fieldValue));
			} else {
				myField.set(this, fieldValue);	
			}
			
			
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
	}
	
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
