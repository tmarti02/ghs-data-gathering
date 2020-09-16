package gov.epa.api;

import java.util.ArrayList;

public class HazardRecord {
	public String hazardClass;
	public ArrayList<String> classifications=new ArrayList<>();
	public ArrayList<String> hazardStatements=new ArrayList<>();
	public ArrayList<String> hazardCodes=new ArrayList<>();
	public String rationale;

	//Extra fields for sources like Japan:
	public String symbol;
	public String signalWord;
	public String precautionaryStatement;
}
