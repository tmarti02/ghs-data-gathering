package gov.epa.api;

import java.io.File;
import java.util.ArrayList;

public class AADashboard {
	public static final String dataFolder = "AA Dashboard"+File.separator+"Data";
	public static final String dictionaryFolder = dataFolder+File.separator+"dictionary";
	public static ArrayList<String> sources = new ArrayList<String>();
		
//	public static final String DB_Path_AA_Dashboard_Records = "AA Dashboard/databases/AA dashboard.db";
	public static final String DB_Path_AA_Dashboard_Records = "databases/AA dashboard.db";
	//fast if you add index for CAS: "CREATE INDEX idx_CAS ON "+tableName+" (CAS)"
	
	
	private void addSources() {
		sources.add(ScoreRecord.sourceAustralia);//OK
		sources.add(ScoreRecord.sourceCanada);//OK
		sources.add(ScoreRecord.sourceChemIDplus);//OK
		sources.add(ScoreRecord.sourceDenmark);//OK
		sources.add(ScoreRecord.sourceDSL);//OK
		sources.add(ScoreRecord.sourceECHA_CLP);//OK
		sources.add(ScoreRecord.sourceEPAMidAtlanticHumanHealth);//OK
		sources.add(ScoreRecord.sourceGermany);
		sources.add(ScoreRecord.sourceHealth_Canada_Priority_Substance_Lists_Carcinogenicity);
		sources.add(ScoreRecord.sourceHealth_Canada_Priority_Substance_Lists_Reproductive);
		sources.add(ScoreRecord.sourceIARC);
		sources.add(ScoreRecord.sourceIRIS);
		sources.add(ScoreRecord.sourceJapan);
//		sources.add(ScoreRecord.sourceKorea);//omit Korea for now since cant update it since they locked down the webpages
		sources.add(ScoreRecord.sourceMalaysia);
		sources.add(ScoreRecord.sourceNewZealand);
		sources.add(ScoreRecord.sourceNIOSH_Potential_Occupational_Carcinogens);
		// sources.add(ParseOSPAR.sourceName);
		sources.add(ScoreRecord.sourceProp65);
		sources.add(ScoreRecord.sourceReachVeryHighConcernList);
		sources.add(ScoreRecord.sourceReportOnCarcinogens);
		sources.add(ScoreRecord.sourceSIN);
		sources.add(ScoreRecord.sourceTEDX);
		sources.add(ScoreRecord.sourceTSCA_Work_Plan);
		sources.add(ScoreRecord.sourceUMD);
		
		sources.add(ScoreRecord.strSourceCERAPP_Exp);
		sources.add(ScoreRecord.strSourceCOMPARA_Exp);
		sources.add(ScoreRecord.strSourceOPERA_MDH);
		sources.add(ScoreRecord.strSourceSEEM3);
		
		
	}

	// class DataRow {
	// public 
	public AADashboard() {
		if (sources.size()==0) addSources();
	}
	
	
	
	
}
