package gov.epa.exp_data_gathering.parse.AqSolDB;

import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

/**
 * Stores data from AqSolDB, accessible at: https://dataverse.harvard.edu/dataset.xhtml?persistentId=doi:10.7910/DVN/OVHAW8
 * @author GSINCL01
 *
 */
public class RecordAqSolDB {
	public String ID;
	public String Name;
	public String InChI;
	public String InChIKey;
	public String DTXSID;
	public String SMILES;
	public String Solubility;
	public String SD;
	public String Ocurrences;
	public String Group;
	public String MolWt;
	public String MolLogP;
	public String MolMR;
	public String HeavyAtomCount;
	public String NumHAcceptors;
	public String NumHDonors;
	public String NumHeteroatoms;
	public String NumRotatableBonds;
	public String NumValenceElectrons;
	public String NumAromaticRings;
	public String NumSaturatedRings;
	public String NumAliphaticRings;
	public String RingCount;
	public String TPSA;
	public String LabuteASA;
	public String BalabanJ;
	public String BertzCT;
	public static final String[] fieldNames = {"ID","Name","InChI","InChIKey","DTXSID","SMILES","Solubility","SD","Ocurrences","Group","MolWt","MolLogP","MolMR","HeavyAtomCount","NumHAcceptors","NumHDonors","NumHeteroatoms","NumRotatableBonds","NumValenceElectrons","NumAromaticRings","NumSaturatedRings","NumAliphaticRings","RingCount","TPSA","LabuteASA","BalabanJ","BertzCT"};

	public static final String lastUpdated = "12/04/2020";
	public static final String sourceName = ExperimentalConstants.strSourceAqSolDB;

	private static final String fileName = "Aqueous Solubility Nature Scientific Data with DTXSIDs.xlsx";

	public static Vector<JsonObject> parseAqSolDBRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		Vector<JsonObject> records = esr.parseRecordsFromExcel(1);
		return records;
	}
}