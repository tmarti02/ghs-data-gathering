package gov.epa.exp_data_gathering.parse;

import java.util.Vector;

import gov.epa.api.ExperimentalConstants;
import gov.epa.ghs_data_gathering.GetData.RecordDashboard;

public class RecordPubChem {
	String iupacName;
	String smiles;
	String cas;
	String synonyms;
	Vector<String> physicalDescription;
	Vector<String> density;
	Vector<String> meltingPoint;
	Vector<String> boilingPoint;
	Vector<String> flashPoint;
	Vector<String> solubility;
	Vector<String> vaporPressure;
	Vector<String> henrysLawConstant;
	Vector<String> logP;
	Vector<String> pKa;
	
	static final String sourceName=ExperimentalConstants.strSourcePubChem;
	
	/**
	 * Extracts DTXSIDs from CompTox dashboard records and translates them to PubChem CIDs
	 * @param records	A vector of RecordDashboard objects
	 * @param start		The index in the vector to start converting
	 * @param end		The index in the vector to stop converting
	 * @return			A vector of PubChem CIDs as strings
	 */
	private static Vector<String> getCIDsFromDashboardRecords(Vector<RecordDashboard> records,int start,int end) {
		Vector<String> cids = new Vector<String>();
		// TODO
		return cids;
	}
}
