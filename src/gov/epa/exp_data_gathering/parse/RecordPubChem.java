package gov.epa.exp_data_gathering.parse;

import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

public class RecordPubChem {
	String iupacName;
	String smiles;
	String cas;
	String synonyms;
	Vector<String> density;
	Vector<String> meltingPoint;
	Vector<String> boilingPoint;
	Vector<String> flashPoint;
	Vector<String> solubility;
	Vector<String> vaporPressure;
	Vector<String> henrysLawConstant;
	Vector<String> logKoa;
	Vector<String> logP;
	Vector<String> pKa;
	Vector<String> decomposition;
	Vector<String> stability;
	
	static final String sourceName=ExperimentalConstants.strSourcePubChem;
}
