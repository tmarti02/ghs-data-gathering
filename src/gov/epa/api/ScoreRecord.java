package gov.epa.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import com.google.gson.Gson;

import gov.epa.ghs_data_gathering.Database.MySQL_DB;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

//Revised version of this class removed need to have FlatFileRecord class

public class ScoreRecord {
	public String hazardName;//used to be in FlatFileRecord
	public String CAS;////used to be in FlatFileRecord
	public String name;//chemical name
	public String source;// where the record came from
	public String sourceOriginal;// where the record came from
	public String score;// i.e. L,M,H,VH
	
	public String listType;
	
	// following fields will be a work in progress as we gather data from different
	// sources:

	// public String classification;//classification in the scheme of the original
	// source, i.e. "Acute Tox. 3"

	// String hazard_name;
	public String category;// i.e. Category 1
	public String hazardCode;// code for hazard, i.e. "H301"
	public String hazardStatement;// text based description of what hazard they think it is
	public String rationale;// why classification was assigned
	// String signal_word;//
	// String symbol;
	public String route;// i.e. oral, dermal, inhalation- used mainly for acute mammalian toxicity for
						// now
	public String note;// extra clarification that doesn't fit into above fields
	public String note2;// extra clarification that doesn't fit into above fields
	
//	public String listType;//right now dont need this because getListType gets it from the source name
	
	public String toxvalID;
	public String testOrganism;//common name for animal used in testing
//	public String reported_dose;
//	public String normalized_dose;

	public String testType;//LC50, LD50 etc
	
	// **************************************************************************************
	public String valueMassOperator;// "<",">", or ""
	public Double valueMass;// quantitative value in mass units such as mg/L
	// Should this be concentration instead of mass?
	public String valueMassUnits;
	
	public Boolean valueActive;
	
	public String effect;

	
	public Double duration;
	public String durationUnits;
	
	public String url;
	public String longRef;
	


//	All the fields in the class in the desired order:	
		public static String[] allFieldNames= {"CAS","name","hazardName","source","sourceOriginal", 
				"score", "listType","route", "category", "hazardCode",
				"hazardStatement", "rationale", "note","note2","toxvalID",
				"testOrganism","testType","valueMassOperator","valueMass","valueMassUnits","effect",
				"duration","durationUnits","url","longRef"};
		
		//Following for displaying results of hazard comparison in webpage:	
		public static String[] displayFieldNames = { "Name","Source", "Original Source","Score", "Rationale","Route", "Category", "Hazard Code",
				"Hazard Statement",  "Test duration","Test organism","Toxicity Type","Toxicity Value","Reference","Note" };

		public static String[] actualFieldNames = { "name","source", "sourceOriginal","score", "rationale","route", "category", "hazardCode",
				"hazardStatement", "duration","testOrganism", "testType","valueMass","longRef","note" };



	public static final String scoreVH = "VH";
	public static final String scoreH = "H";
	public static final String scoreM = "M";
	public static final String scoreL = "L";
	public static final String scoreVL = "VL";
	public static final String scoreNA = "N/A";

	public static final String sourceBoyes = "Nervous System";
	public static final String sourceCanada = "Canada";
	public static final String sourceJapan = "Japan";
	public static final String sourceTEST_Predicted = "T.E.S.T. (predicted value)";
	public static final String sourceTEST_Experimental = "T.E.S.T. (experimental value)";
	public static final String sourceChemIDplus = "ChemIDplus";
	public static final String sourceNewZealand = "New Zealand";
	public static final String sourceKorea = "Korea";
	public static final String sourceMalaysia = "Malaysia";
	public static final String sourceECHA_CLP = "ECHA CLP";
	public static final String sourceTEDX = "TEDX";
	public static final String sourceSIN = "SIN";
//	public static final String sourceMAK = "List of MAK and BAT Values (Germany)";
	public static final String sourceDenmark = "Denmark";
	public static final String sourceIRIS = "IRIS";
	public static final String sourceROC = "Report on Carcinogens (ROC)";
	public static final String sourceIARC = "IARC";
	public static final String sourceProp65 = "Prop 65";
	public static final String sourceDSL = "DSL";
	public static final String sourceUMD = "UMD";
	public static final String sourceTSCA_Work_Plan = "TSCA work plan";
	public static final String sourceReachVeryHighConcernList = "REACH Very High Concern List";
	public static final String sourceAustralia = "Australia";
	public static final String sourceNIOSH_Potential_Occupational_Carcinogens = "NIOSH list of potential occupational carcinogens";
	public static final String sourceEPAMidAtlanticHumanHealth = "EPA mid-Atlantic Region Human Health Risk-Based Concentrations";
	public static final String sourceHealth_Canada_Priority_Substance_Lists_Carcinogenicity = "Health Canada Priority Substance Lists (Carcinogenicity)";
	public static final String sourceHealth_Canada_Priority_Substance_Lists_Reproductive = "Health Canada Priority Substance Lists (Reproductive Toxicity)";
	public static final String sourceReproductiveCanada = "Reproductive Canada";
	public static final String sourceEDSPDB_Reproductive_Toxicity_Ranking = "EDSPDB Reproductive Toxicity Ranking";
	public static final String sourceTeratogenicity_Data_in_Vitro_from_EPA_HPVIS = "Teratogenicity Data in Vitro from EPA HPVIS";
	public static final String sourceCarcinogenicityNIOSHList = "Carcinogenicity NIOSH List";
	public static final String sourceGermany = "Germany";
//	public static final String sourceGermany_EMAK = "Germany\\MAK2017";
	public static final String sourceEU_Detergent_Ingredient_Database_2014 = "EU Detergent Ingredient Database 2014";
	public static final String sourceOSPAR = "OSPAR";
	public static final String sourceReach_JSON_Files = "Reach JSON Files";
	public static final String sourceReportOnCarcinogens = "Report On Carcinogens";
	public static final String sourceGenotoxicity_Data_In_Vitro_EPA_HPVIS = "Genotoxicity Data In Vitro from EPA HPVIS";
	public static final String sourceAcute_Toxicity_Data_from_EPA_HPVIS = "Acute Toxicity Data from EPA HPVIS";
	public static final String sourceReproductive_Toxicity_Data_In_Vitro_from_EPA_HPVIS = "Reproductive Toxicity Data in Vitro from EPA HPVIS";

	public static final String sourceToxVal="ToxVal";
	
	
//	public static final float weightECHA_CLP = 20.0f;
//	public static final float weightIRIS = 20.0f;
//
//	public static final float weightJapan = 10.0f;
//	public static final float weightAustralia = 10.0f;
//	public static final float weightKorea = 5.0f;
//	public static final float weightMalaysia = 5.0f;
//	public static final float weightGermany = 5.0f;
//	public static final float weightTEDX = 5.0f;
//	public static final float weightSIN = 5.0f;
//	public static final float weightNewZealand = 5.0f;
//	public static final float weightTSCA_Work_Plan = 5.0f;
//	public static final float weightReachVeryHighConcernList = 5.0f;
//	public static final float weightEPAMidAtlanticHumanHealth = 5.0f;
//
//	// TODO- maybe weight the sources with only one possible category (yes or no)
//	// lower...
//
//	public static final float weightROC = 5.0f;
//	public static final float weightIARC = 5.0f;
//	public static final float weightProp65 = 5.0f;
//	public static final float weightDSL = 5.0f;
//	public static final float weightUMD = 5.0f;
//	public static final float weightNIOSH_Carcinogen = 5.0f;
//
//	public static final float weightHealthCanadaPrioritySubstanceListsCarcinogenicity = 5.0f;
//	public static final float weightHealthCanadaPrioritySubstanceListsReproductive = 5.0f;
//
//	public static final float weightTEST_Experimental = 10.0f;
//	
//	public static final float weightTEST_Predicted = 1.0f;
//	public static final float weightDenmark = 1.0f;
	
	
//	public static final String typeAuthoritativeA="Authoritative A";
//	public static String typeAuthoritativeB="Authoritative B";
//	public static String typeScreeningA="Screening A";
//	public static String typeScreeningB="Screening B";
	
	
	/*public static final String typeAuthoritative="Authoritative";
	public static String typeScreening="Screening";*/
	
//	public static String typePredicted="Predicted";
	
	
	public static final String typeAuthoritative="Authoritative"; // Authoritative List
	public static final String typeScreening="Screening"; // Screening  List
	public static final String typePredicted="QSAR Model"; // Predicted value from QSAR model
	

//	public static final String typeAuthoritativeGS="AuthoritativeGS"; // Authoritative GreenScreen Specified List
//	public static final String typeScreeningGS="ScreeningGS"; // Screening GreenScreen Specified List
//	public static final String typeAuthoritativeOth="AuthoritativeOth"; // Authoritative not specified by GreenScreen
//	public static final String typeScreeningOth="ScreeningOth"; // Screening not specified by GreenScreen

	
/*	public static final String gsYes="Yes"; // GreenScreen Specified List
	public static final String gsNo="No"; // Not specified by GreenScreen
*/	
	
	
	/* From page 7 of Pharos CML description: 
	 	 The Pharos CML uses the following trumping scheme:
		"First, lists are sorted by level of authority:
		 1. Authoritative lists (designated by GreenScreen)
		 2. Priority lists proposed for GreenScreen
		 3. Screening lists (designated by GreenScreen)
		 4. Lists not included in or proposed for GreenScreen
		If there are multiple lists of the highest authority level, they are then sorted by hazard level,
		and the list with the highest hazard is selected for display."
		
	So maybe we should distinguish between lists designated by GreenScreen and lists not included in GreenScreen.
	Maybe designate lists as authoritative or screening and then have a separate variable that indicates
	whether the list is designated by GreenScreen.  That way we could run a GreenScreen List Translator analysis
	to compare results with official List Translators (Pharos and Toxnot).
	
	So the boolean variable isInGreenScreen() = true when the list is in GreenScreen, otherwise false.
	
	Should we also indicate whether the list is included in Pharos, Toxnot, or whether the lists are required by
	product certification or disclosure programs such as Health Product Declaration (HPD) referenced in Pharos?

		*/
//	
	
	public static final String listTypeECHA_CLP = typeAuthoritative; // typeAuthoritative? // Is this the same as EU-GHS?
																	//     page 105 GreenScreen v 1.4?
																	// or Annex VI CMRs Annex VI to CLP GreenScreen page 104.
	public static final String listTypeIRIS = typeAuthoritative; // page 103 GreenScreen v. 1.4
	public static final String listTypeJapan = typeScreening; // page 112 GreenScreen v. 1.4
	public static final String listTypeAustralia = typeScreening;//see pg 112 GreenScreen v 1.4
	public static final String listTypeKorea = typeScreening; // page 112 GreenScreen v 1.4
	public static final String listTypeMalaysia = typeScreening; // page 112 GreenScreen v 1.4
	public static final String listTypeGermany = typeAuthoritative; // page 107 GreenScreen v 1.4
	public static final String listTypeTEDX = typeScreening; // page 110 GreenScreen v 1.4
	public static final String listTypeSIN = typeScreening; // page 109 GreenScreen v 1.4
	public static final String listTypeNewZealand = typeScreening; // page 113 GreenScreen v 1.4 
	public static final String listTypeTSCA_Work_Plan = typeScreening; // don't see it on the GreenScreen list.
															// Should the TSCA Work Plan be authoritative because
															// the EPA is authoritative or should it be screening
															// because it is a list of chemicals designated for
															// further assessment?  Making it screening for now.

	
	public static final String listTypeReachVeryHighConcernList = typeAuthoritative; // page 105 and 106 GreenScreen v 1.4
																	// Do we have all of the REACH lists:
																	// Candidate, Prioritization, and Subject to Authorization? 
	public static final String listTypeEPAMidAtlanticHumanHealth = typeAuthoritative; // don't see it on the GreenScreen List
																		// Should this be authoritative because it is EPA?
																		// Or does the info on this list come from other lists
																		// so then it should be screening?
	
	// TODO- maybe weight the sources with only one possible category (yes or no)
	// lower...

	public static final String listTypeROC = typeAuthoritative; // page 108 GreenScreen v 1.4
	public static final String listTypeIARC = typeAuthoritative; // page 106 GreenScreen v 1.4 
	public static final String listTypeProp65 = typeAuthoritative; // page 109 GreenScreen v 1.4
	public static final String listTypeDSL = typeScreening; // page 103 GreenScreen v1.4
															// if this is the same as EC-CEPA DSL. 
	public static final String listTypeUMD = typeScreening; // Screening?
															// don't see it on the GreenScreen list.  University of Maryland??
	public static final String listTypeNIOSH_Carcinogen = typeAuthoritative; // page 107 GreenScreen v 1.4


	//Added by TMM, 3/26/18:
	public static final String listTypeCanada=typeScreening;
	public static final String listTypeReproductiveCanada=typeScreening;

	public static final String listTypeHealthCanadaPrioritySubstanceListsCarcinogenicity = typeScreening; // screening or authoritative?
																							// don't see it on the GreenScreen list 
																							// or is it one of the CEPA lists on GreenScreen pages 102-103?
	public static final String listTypeHealthCanadaPrioritySubstanceListsReproductive = typeScreening; // screening or authoritative? 
																							// don't see it on the GreenScreen list
																							// or is it one of the CEPA lists on GreenScreen pages 102-103?
	public static final String listTypeTEST_Experimental = typeScreening; // don't see it on the GreenScreen list
	
	public static final String listTypeTEST_Predicted = typePredicted; // don't see it on the GreenScreen list
	public static final String listTypeDenmark = typePredicted; // don't see it on the GreenScreen list
	
	private static final String listTypeChemidplus=typeScreening;
	
	
//public static final String listTypeECHA_CLP = ""; // typeAuthoritative? // Is this the same as EU-GHS?
//	//     page 105 GreenScreen v 1.4?
//	// or Annex VI CMRs Annex VI to CLP GreenScreen page 104.
//public static final String listTypeIRIS = typeAuthoritativeGS; // page 103 GreenScreen v. 1.4
//public static final String listTypeJapan = typeScreeningGS; // page 112 GreenScreen v. 1.4
//public static final String listTypeAustralia = typeScreeningGS;//see pg 112 GreenScreen v 1.4
//public static final String listTypeKorea = typeScreeningGS; // page 112 GreenScreen v 1.4
//public static final String listTypeMalaysia = typeScreeningGS; // page 112 GreenScreen v 1.4
//public static final String listTypeMAK = typeAuthoritativeGS; // page 107 GreenScreen v 1.4
//public static final String listTypeTEDX = typeScreeningGS; // page 110 GreenScreen v 1.4
//public static final String listTypeSIN = typeScreeningGS; // page 109 GreenScreen v 1.4
//public static final String listTypeNewZealand = typeScreeningGS; // page 113 GreenScreen v 1.4 
//public static final String listTypeTSCA_Work_Plan = typeScreeningOth; // don't see it on the GreenScreen list.
//// Should the TSCA Work Plan be authoritative because
//// the EPA is authoritative or should it be screening
//// because it is a list of chemicals designated for
//// further assessment?  Making it screening for now.
//
//
//public static final String listTypeReachVeryHighConcernList = typeAuthoritativeGS; // page 105 and 106 GreenScreen v 1.4
//	// Do we have all of the REACH lists:
//	// Candidate, Prioritization, and Subject to Authorization? 
//public static final String listTypeEPAMidAtlanticHumanHealth = ""; // don't see it on the GreenScreen List
//		// Should this be authoritative because it is EPA?
//		// Or does the info on this list come from other lists
//		// so then in should be screening?
//
//// TODO- maybe weight the sources with only one possible category (yes or no)
//// lower...
//
//public static final String listTypeROC = typeAuthoritativeGS; // page 108 GreenScreen v 1.4
//public static final String listTypeIARC = typeAuthoritativeGS; // page 106 GreenScreen v 1.4 
//public static final String listTypeProp65 = typeAuthoritativeGS; // page 109 GreenScreen v 1.4
//public static final String listTypeDSL = typeScreeningGS; // page 103 GreenScreen v1.4
//// if this is the same as EC-CEPA DSL. 
//public static final String listTypeUMD = ""; // don't see it on the GreenScreen list.  University of Maryland??
//public static final String listTypeNIOSH_Carcinogen = typeAuthoritativeGS; // page 107 GreenScreen v 1.4
//public static final String listTypeHealthCanadaPrioritySubstanceListsCarcinogenicity = ""; // don't see it on the GreenScreen list
//public static final String listTypeHealthCanadaPrioritySubstanceListsReproductive = ""; // don't see it on the GreenScreen listpublic static final String listTypeTEST_Experimental = ""; // don't see it on the GreenScreen list
//public static final String listTypeTEST_Experimental = typeScreeningOth; // don't see it on the GreenScreen list
//public static final String listTypeTEST_Predicted = typeScreeningOth; // don't see it on the GreenScreen list
//public static final String listTypeDenmark = typeScreeningOth; // don't see it on the GreenScreen list

	
	public ScoreRecord(String CAS,String name,String hazard_name,String source,String score,String category,String hazard_code,String hazard_statement,
			String rationale,String route,String note,String note2,String url) {
		
		this.CAS=CAS;
		this.name=name;
		this.hazardName=hazard_name;		
		this.source=source;
		this.score=score;
		this.category=category;
		this.hazardCode=hazard_code;
		this.hazardStatement=hazard_statement;
		this.rationale=rationale;
		this.route=route;
		this.note=note;
		this.note2=note2;
		this.url=url;
		
	}
	
	public ScoreRecord(String hazard_name,String CAS,String name) {
		this.hazardName=hazard_name;
		this.CAS=CAS;
		this.name=name;
	}

	
	public int getAuthorityWeight() {
		return getAuthorityWeight(getListType());
	}

	

	/**
	 * Create record based on header list and data list in csv using reflection to assign by header name
	 * 
	 * @param hlist
	 * @param list
	 * @return
	 */
	public static ScoreRecord createRecord(List<String> hlist, List<String> list) {
		ScoreRecord r=new ScoreRecord(null,null,null);
		//convert to record:
		try {
			for (int i=0;i<list.size();i++) {
				Field myField =r.getClass().getField(hlist.get(i));
				
				
				if (myField.getType().getName().contains("Double")) {
					if (!list.get(i).isEmpty())
						myField.set(r, Double.parseDouble(list.get(i)));					
				} else {
					myField.set(r, list.get(i));	
				}
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return r;
	}


	//convert to string by reflection:
	public String toString(String d) {
		
		String Line = "";
		for (int i = 0; i < allFieldNames.length; i++) {
			try {
			
				
				Field myField = this.getClass().getDeclaredField(allFieldNames[i]);
				
				String val=null;
				
//				System.out.println(myField.getType().getName());
				
				if (myField.getType().getName().contains("Double")) {
					if (myField.get(this)==null) {
						val="";	
					} else {
						val=(Double)myField.get(this)+"";
					}
					
				} else {
					if (myField.get(this)==null) {
//						val="\"\"";
						val="";
					} else {
//						val="\""+(String)myField.get(this)+"\"";
						val=(String)myField.get(this);
					} 
				}
				
				val=val.replace("\r\n","<br>");
				val=val.replace("\n","<br>");
				
//				while(val.contains("<br><br>")) {
//					val=val.replace("<br><br>", "<br>");
//				}
				
//				if (fieldNames[i].equals("note")) {
//					System.out.println(CAS+"\t"+source+"\t"+hazard_name+"\t"+val);
//				}

				if (val.contains(d)) {
					System.out.println(this.CAS+"\t"+allFieldNames[i]+"\t"+val+"\thas delimiter");
				}
				
				Line += val;
				if (i < allFieldNames.length - 1) {
					Line += d;
				}
			
			
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return Line;
	}

	
	//convert to string by reflection:
		public String toString(String del,String [] fieldNames) {
			
			String Line = "";
			for (int i = 0; i < fieldNames.length; i++) {
				try {
				
					
					Field myField = this.getClass().getDeclaredField(fieldNames[i]);
					
					String val=null;
					
//					System.out.println(myField.getType().getName());
					
					if (myField.getType().getName().contains("Double")) {
						if (myField.get(this)==null) {
							val="";	
						} else {
							val=(Double)myField.get(this)+"";
						}
						
					} else {
						if (myField.get(this)==null) {
//							val="\"\"";
							val="";
						} else {
//							val="\""+(String)myField.get(this)+"\"";
							val=(String)myField.get(this);
						} 
					}
					
					val=val.replace("\r\n","<br>");
					val=val.replace("\n","<br>");
					
//					while(val.contains("<br><br>")) {
//						val=val.replace("<br><br>", "<br>");
//					}
					
//					if (fieldNames[i].equals("note")) {
//						System.out.println(CAS+"\t"+source+"\t"+hazard_name+"\t"+val);
//					}

					if (val.contains(del)) {
						System.out.println(this.CAS+"\t"+fieldNames[i]+"\t"+val+"\thas delimiter");
					}
					
					Line += val;
					if (i < fieldNames.length - 1) {
						Line += del;
					}
				
				
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			return Line;
		}
	
//	void createFlatFileFromAllSources() {
//	AADashboard a=new AADashboard();
//	String d="|";
//	
//	try {
//		
//		FileWriter fw=new FileWriter("AA Dashboard\\Data\\dictionary\\text output\\flat file 2018-05-10.txt");
//		
//		fw.write(FlatFileRecord.getHeader(d)+"\r\n");
//		
//		int NOCAScount=0;
//		
//		for (String jsonFolder:a.jsonFolders) {
//			
//			System.out.println(jsonFolder);
//			
//			File Folder = new File(jsonFolder);
//			
//			File[] files = Folder.listFiles();
//
//			int counter=0;
//			for (File file:files) {
//				
//				if (counter%1000==0) {
//					System.out.println(counter);
//				}
//				
//				Chemical chemical=Chemical.loadFromJSON(file);
//				
//				if (chemical.CAS==null || chemical.CAS.isEmpty()) {
//					NOCAScount++;
//					chemical.CAS="NOCAS"+NOCAScount;
//				}
//				
//				ArrayList<String>lines=chemical.toStringArray(d);
//				
//				for(String line:lines) {
//					fw.write(line+"\r\n");
//				}
//				fw.flush();
//				
//				counter++;
//			}
//		}
//		
//		fw.close();
//		
//	
//	} catch (Exception ex) {
//		ex.printStackTrace();
//	}
//
//	
//}

	
	public static Vector <ScoreRecord> loadRecordsFromFile(String filepath,String ID,String del) {
		
		Vector <ScoreRecord>records=new Vector();
		
		String Line="";
		
		try {
			Scanner scanner = new Scanner(new File(filepath));
			String header=scanner.nextLine();
			List <String>hlist=Utilities.Parse(header, del);
			 
			while (scanner.hasNext()) {
				Line=scanner.nextLine();
				if (Line==null) break;
				List <String>list=Utilities.Parse(Line, del);
				ScoreRecord r=ScoreRecord.createRecord(hlist,list);
				
//				String valueID=list.get(hlist.indexOf(ID));
//				records.put(valueID, r);
				records.add(r);
				
	        }
			scanner.close();
			
		} catch (Exception ex) {
			System.out.println(ex.getMessage()+"\t"+Line);
			ex.printStackTrace();
		}
		return records;
	}
	
	
	
	public int scoreToInt() {
		return scoreToInt(this.score);
	}
	
	public static int scoreToInt(String score) {
		if (score.equals("VH")) {
			return 5;
		} else if (score.equals("H")) {
			return 4;
		} else if (score.equals("M")) {
			return 3;
		} else if (score.equals("L")) {
			return 2;
		} else {
			return 0;
		}
	}

	
	public static int getAuthorityWeight(String listType) {
		
		if (listType.equals(typeAuthoritative)) {
			return 3;
		} else if (listType.equals(typeScreening)) {
			return 2;
		} else if (listType.equals(typePredicted)) {
			return 1;
		} else {
			return 0;
		}
		
	}
	
	public float getWeight() {
		return getWeight(getListType());
	}
	
	//TODO- is it necessary to have both weight and authority weight?
	public static float getWeight(String listType) {
		
		if (listType.equals(typeAuthoritative)) {
			return 10;
		} else if (listType.equals(typeScreening)) {
			return 5;
		} else if (listType.equals(typePredicted)) {
			return 1;
		} else {
			return 0;
		}
		
	}
	
	
//	public static float getWeight(String source) {
//		float weight = -9999;
//
//		if (source.equals(ScoreRecord.sourceJapan)) {
//			weight = ScoreRecord.weightJapan;
//		} else if (source.equals(ScoreRecord.sourceKorea)) {
//			weight = ScoreRecord.weightKorea;
//		} else if (source.equals(ScoreRecord.sourceMalaysia)) {
//			weight = ScoreRecord.weightMalaysia;
//		} else if (source.equals(ScoreRecord.sourceECHA_CLP)) {
//			weight = ScoreRecord.weightECHA_CLP;
//		} else if (source.equals(ScoreRecord.sourceGermany)) {
//			weight = ScoreRecord.weightGermany;
//		} else if (source.equals(ScoreRecord.sourceTEST_Experimental)) {
//			weight = ScoreRecord.weightTEST_Experimental;
//		} else if (source.equals(ScoreRecord.sourceTEST_Predicted)) {
//			weight = ScoreRecord.weightTEST_Predicted;
//		} else if (source.equals(ScoreRecord.sourceDenmark)) {
//			weight = ScoreRecord.weightDenmark;
//		} else if (source.equals(ScoreRecord.sourceTEDX)) {
//			weight = ScoreRecord.weightTEDX;
//		} else if (source.equals(ScoreRecord.sourceSIN)) {
//			weight = ScoreRecord.weightSIN;
//		} else if (source.equals(ScoreRecord.sourceIRIS)) {
//			weight = ScoreRecord.weightIRIS;
//		} else if (source.equals(ScoreRecord.sourceNewZealand)) {
//			weight = ScoreRecord.weightNewZealand;
//		} else if (source.equals(ScoreRecord.sourceROC)) {
//			weight = ScoreRecord.weightROC;
//		} else if (source.equals(ScoreRecord.sourceIARC)) {
//			weight = ScoreRecord.weightIARC;
//		} else if (source.equals(ScoreRecord.sourceProp65)) {
//			weight = ScoreRecord.weightProp65;
//		} else if (source.equals(ScoreRecord.sourceDSL)) {
//			weight = ScoreRecord.weightDSL;
//		} else if (source.equals(ScoreRecord.sourceUMD)) {
//			weight = ScoreRecord.weightUMD;
//		} else if (source.equals(ScoreRecord.sourceTSCA_Work_Plan)) {
//			weight = ScoreRecord.weightTSCA_Work_Plan;
//		} else if (source.equals(ScoreRecord.sourceReachVeryHighConcernList)) {
//			weight = ScoreRecord.weightReachVeryHighConcernList;
//		} else if (source.equals(ScoreRecord.sourceAustralia)) {
//			weight = ScoreRecord.weightAustralia;
//		} else if (source.equals(ScoreRecord.sourceNIOSH_Potential_Occupational_Carcinogens)) {
//			weight = ScoreRecord.weightNIOSH_Carcinogen;
//		} else if (source.equals(ScoreRecord.sourceEPAMidAtlanticHumanHealth)) {
//			weight = ScoreRecord.weightEPAMidAtlanticHumanHealth;
//		} else if (source.equals(ScoreRecord.sourceHealth_Canada_Priority_Substance_Lists_Carcinogenicity)) {
//			weight = weightHealthCanadaPrioritySubstanceListsCarcinogenicity;
//		} else if (source.equals(ScoreRecord.sourceHealth_Canada_Priority_Substance_Lists_Reproductive)) {
//			weight = weightHealthCanadaPrioritySubstanceListsReproductive;
//		}
//
//		if (weight == -9999)
//			System.out.println(source + "\t" + weight);
//
//		return weight;
//
//	}
	
	public boolean isInGreenScreen() {
		if (this.source.equals(ScoreRecord.sourceJapan)) {
			return true;
		} else if (this.source.equals(ScoreRecord.sourceKorea)) {
			return true;
		} else if (this.source.equals(ScoreRecord.sourceMalaysia)) {
			return true;
		} else if (this.source.equals(ScoreRecord.sourceECHA_CLP)) {
			return true;
		} else if (this.source.equals(ScoreRecord.sourceGermany)) {
			return true;
		} else if (this.source.equals(ScoreRecord.sourceTEST_Experimental)) {
			return false;
		} else if (this.source.equals(ScoreRecord.sourceTEST_Predicted)) {
			return false;
		} else if (this.source.equals(ScoreRecord.sourceDenmark)) {
			return false;
		} else if (this.source.equals(ScoreRecord.sourceTEDX)) {
			return true;
		} else if (this.source.equals(ScoreRecord.sourceSIN)) {
			return true;
		} else if (this.source.equals(ScoreRecord.sourceIRIS)) {
			return true;
		} else if (this.source.equals(ScoreRecord.sourceNewZealand)) {
			return true;
		} else if (this.source.equals(ScoreRecord.sourceROC)) {
			return true;
		} else if (this.source.equals(ScoreRecord.sourceIARC)) {
			return true;
		} else if (this.source.equals(ScoreRecord.sourceProp65)) {
			return true;
		} else if (this.source.equals(ScoreRecord.sourceDSL)) {
			return true;
		} else if (this.source.equals(ScoreRecord.sourceUMD)) {
			return false;
		} else if (this.source.equals(ScoreRecord.sourceTSCA_Work_Plan)) {
			return false;
		} else if (this.source.equals(ScoreRecord.sourceReachVeryHighConcernList)) {
			return true;
		} else if (this.source.equals(ScoreRecord.sourceAustralia)) {
			return true;
		} else if (this.source.equals(ScoreRecord.sourceNIOSH_Potential_Occupational_Carcinogens)) {
			return true;
		} else if (this.source.equals(ScoreRecord.sourceEPAMidAtlanticHumanHealth)) {
			return false;
		} else if (this.source.equals(ScoreRecord.sourceHealth_Canada_Priority_Substance_Lists_Carcinogenicity)) {
			return false;
		} else if (this.source.equals(ScoreRecord.sourceHealth_Canada_Priority_Substance_Lists_Reproductive)) {
			return false;
		} else {
			return false;
		}
	}
	
	public ScoreRecord clone() {		
		Gson gson= new Gson();
		String tmp = gson.toJson(this);
		ScoreRecord sr = gson.fromJson(tmp, ScoreRecord.class);		
		return sr;		
	}
	
	public String getListType() {
		if (listType!=null) return listType;
		return getListType(source);
	}
	
	public String getListType(String source) {
		String listType ="";

		if (source.equals(ScoreRecord.sourceJapan)) {
			listType = ScoreRecord.listTypeJapan;
		} else if (source.equals(ScoreRecord.sourceKorea)) {
			listType = ScoreRecord.listTypeKorea;
		} else if (source.equals(ScoreRecord.sourceMalaysia)) {
			listType = ScoreRecord.listTypeMalaysia;
		} else if (source.equals(ScoreRecord.sourceECHA_CLP)) {
			listType = ScoreRecord.listTypeECHA_CLP;
		} else if (source.equals(ScoreRecord.sourceGermany)) {
			listType = ScoreRecord.listTypeGermany;
		} else if (source.equals(ScoreRecord.sourceTEST_Experimental)) {
			listType = ScoreRecord.listTypeTEST_Experimental;
		} else if (source.equals(ScoreRecord.sourceTEST_Predicted)) {
			listType = ScoreRecord.listTypeTEST_Predicted;
		} else if (source.equals(ScoreRecord.sourceDenmark)) {
			listType = ScoreRecord.listTypeDenmark;
		} else if (source.equals(ScoreRecord.sourceTEDX)) {
			listType = ScoreRecord.listTypeTEDX;
		} else if (source.equals(ScoreRecord.sourceSIN)) {
			listType = ScoreRecord.listTypeSIN;
		} else if (source.equals(ScoreRecord.sourceIRIS)) {
			listType = ScoreRecord.listTypeIRIS;
		} else if (source.equals(ScoreRecord.sourceNewZealand)) {
			listType = ScoreRecord.listTypeNewZealand;
		} else if (source.equals(ScoreRecord.sourceROC)) {
			listType = ScoreRecord.listTypeROC;
		} else if (source.equals(ScoreRecord.sourceIARC)) {
			listType = ScoreRecord.listTypeIARC;
		} else if (source.equals(ScoreRecord.sourceProp65)) {
			listType = ScoreRecord.listTypeProp65;
		} else if (source.equals(ScoreRecord.sourceDSL)) {
			listType = ScoreRecord.listTypeDSL;
		} else if (source.equals(ScoreRecord.sourceUMD)) {
			listType = ScoreRecord.listTypeUMD;
		} else if (source.equals(ScoreRecord.sourceTSCA_Work_Plan)) {
			listType = ScoreRecord.listTypeTSCA_Work_Plan;
		} else if (source.equals(ScoreRecord.sourceReachVeryHighConcernList)) {
			listType = ScoreRecord.listTypeReachVeryHighConcernList;
		} else if (source.equals(ScoreRecord.sourceAustralia)) {
			listType = ScoreRecord.listTypeAustralia;
		} else if (source.equals(ScoreRecord.sourceNIOSH_Potential_Occupational_Carcinogens)) {
			listType = ScoreRecord.listTypeNIOSH_Carcinogen;
		} else if (source.equals(ScoreRecord.sourceEPAMidAtlanticHumanHealth)) {
			listType = ScoreRecord.listTypeEPAMidAtlanticHumanHealth;
		} else if (source.equals(ScoreRecord.sourceHealth_Canada_Priority_Substance_Lists_Carcinogenicity)) {
			listType = listTypeHealthCanadaPrioritySubstanceListsCarcinogenicity;
		} else if (source.equals(ScoreRecord.sourceHealth_Canada_Priority_Substance_Lists_Reproductive)) {
			listType = listTypeHealthCanadaPrioritySubstanceListsReproductive;
		} else if (source.equals(ScoreRecord.sourceCanada)) {
			listType=listTypeCanada;
		} else if (source.equals(ScoreRecord.sourceReproductiveCanada)) {
			listType=listTypeReproductiveCanada;
		} else if (source.equals(ScoreRecord.sourceChemIDplus)) {
			listType=listTypeChemidplus;
		} else if (source.contentEquals(ScoreRecord.sourceToxVal)) {
		}


		if (!source.equals("") && listType.equals(""))
			System.out.println(source + "\tmissing list type");

		return listType;

	}
	

	public static String getHeader(String del) {
		// TODO Auto-generated method stub

		String Line = "";
		for (int i = 0; i < allFieldNames.length; i++) {
			Line += allFieldNames[i];
			if (i < allFieldNames.length - 1) {
				Line += del;
			}
			
//			} else {
//				Line += "\r\n";
//			}
		}

		return Line;
	}

	
	
	public static String getHeader() {
		// TODO Auto-generated method stub
		return getHeader("\t");
	}

	
	
	
	/**
	 * Get count of records per score for all chemicals
	 * 
	 * @param outputPath
	 */
	public static void analyzeRecords(String filePath,String outputFilePath) {

		ArrayList<String>lines=Utilities.readFileToArray(filePath);

		String header=lines.remove(0);

		Chemical chemical=new Chemical();

		FileWriter fw;
		try {
			fw = new FileWriter(outputFilePath);

			String d="|";
					

			fw.write("CAS"+d+"name"+d);
			for (Score score:chemical.scores) {
				fw.write(score.hazard_name+d);
			}
			fw.write("\n");

			for (String line:lines) {
				ScoreRecord sr=createScoreRecord(line);

				//			System.out.println(line);

				if (chemical.CAS==null) {
					chemical.CAS=sr.CAS;
					chemical.name=sr.name;
//				} else if ( !f.CAS.equals(chemical.CAS) || !f.name.equals(chemical.name)) {
				} else if ( !sr.CAS.equals(chemical.CAS)) {
					
					fw.write(chemical.CAS+d+chemical.name+d);
					for (Score score:chemical.scores) {
						fw.write(score.records.size()+d);
					}
					fw.write("\n");
					chemical=new Chemical();
				}

				Score score=chemical.getScore(sr.hazardName);
				
				score.records.add(sr);


			}
			fw.close();


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	public static void createFlatFileFromAllSources(String outputPath) {
		AADashboard a=new AADashboard();
		String d="|";
		
		try {
			
			FileWriter fw=new FileWriter(outputPath);
			fw.write(ScoreRecord.getHeader(d)+"\r\n");
			
			Hashtable<String,String>htCASName=new Hashtable<>();
			
			for (String source:a.sources) {
				
				String filePathFlatChemicalRecords = AADashboard.dataFolder+File.separator+source+File.separator+source +" Chemical Records.txt";

				File file=new File(filePathFlatChemicalRecords);
				
				if (!file.exists())  {
					System.out.println("*** "+source+" text file missing");
					continue;
				} else {
					System.out.println(source);
				}
				
				ArrayList<String>lines=Utilities.readFileToArray(filePathFlatChemicalRecords);
				
				String header=lines.remove(0);

				for (String line:lines) {
					
//					FlatFileRecord f=FlatFileRecord.createFlatFileRecord(line);
					
					String CAS=line.substring(0,line.indexOf(d));
					line=line.substring(line.indexOf(d)+1,line.length());
					String name=line.substring(0,line.indexOf(d));
					line=line.substring(line.indexOf(d)+1,line.length());
					
					if (CAS.isEmpty()) {
						if (name.isEmpty()) {
							System.out.println("CAS and name =null: "+line);
							continue;
						}
						
						if (htCASName.get(name)==null) {
							String newCAS="NOCAS"+(htCASName.size()+1);
							htCASName.put(name,newCAS);
							CAS=newCAS;
						} else {
							CAS=htCASName.get(name);
						}
						
					}
					
					line=line.replace("<br><br><br>","<br><br>");
					fw.write(CAS+d+name+d+line+"\r\n");

				}
				fw.flush();
			}
			
			fw.close();
			
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	public static ScoreRecord createScoreRecord(String line) {
		ScoreRecord f = new ScoreRecord(null,null,null);
		LinkedList<String> list = Utilities.Parse(line, "|");
//		if (list.getFirst().isEmpty()) System.out.println(line);
		
		for (int i = 0; i < allFieldNames.length; i++) {

			try {
				String val = list.get(i);
				
				Field myField = f.getClass().getDeclaredField(allFieldNames[i]);
				
				if (myField.getType().getName().contains("Double")) {
														
					if (!val.isEmpty()) {
						myField.set(f, Double.parseDouble(val));						
					}
				} else {
					if (!val.isEmpty()) {
						
//						System.out.println(fieldNames[i]+"\t"+val);
						myField.set(f, val);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return f;

	}
	

	
	public static void createFlatFileFromAllSourcesSortedByCAS(String outputPath) {
		AADashboard a=new AADashboard();
		String d="|";
		
		try {
						
			Hashtable<String,String>htCASName=new Hashtable<>();
			
			ArrayList<String>overallLines=new ArrayList<>();
			
			for (String source:a.sources) {
				
				String filePathFlatChemicalRecords = AADashboard.dataFolder+File.separator+source+File.separator+source +" Chemical Records.txt";

				File file=new File(filePathFlatChemicalRecords);
				
								
				
				if (!file.exists())  {
					System.out.println("*** "+source+" text file missing");
					continue;
				} else {
					
					Path path = Paths.get(file.getAbsolutePath());
		            BasicFileAttributes attr =
		                Files.readAttributes(path, BasicFileAttributes.class);
		           		           
					System.out.println(source+"\t"+attr.lastModifiedTime());
				}
				
				ArrayList<String>lines=Utilities.readFileToArray(filePathFlatChemicalRecords);
				
				String header=lines.remove(0);
				
				LinkedList<String>hlist=Utilities.Parse(header, "|");

				for (String line:lines) {
					
					line=line.replace("<br><br><br>","<br><br>");
					
//					FlatFileRecord f=FlatFileRecord.createFlatFileRecord(line);
					
					LinkedList<String>list=Utilities.Parse(line, "|");
					
					if (list.size()!=hlist.size()) {
						System.out.println("mismatch in num columns:"+line);
						continue;
					}
					
					
					ScoreRecord sr=ScoreRecord.createRecord(hlist, list);
										
					if (sr.CAS.isEmpty()) {
						if (sr.name.isEmpty()) {
							System.out.println("CAS and name =null: "+line);
							continue;
						}
						
						if (htCASName.get(sr.name)==null) {
							String newCAS="NOCAS"+(htCASName.size()+1);
							htCASName.put(sr.name,newCAS);
							sr.CAS=newCAS;
						} else {
							sr.CAS=htCASName.get(sr.name);
						}
						
					}
					
					
					overallLines.add(sr.toString("|"));
				}
			}
			
			Collections.sort(overallLines);
			
			FileWriter fw=new FileWriter(outputPath);
			fw.write(ScoreRecord.getHeader(d)+"\r\n");

			for (String Line:overallLines) {
				fw.write(Line+"\r\n");
			}
			fw.flush();
			fw.close();
			
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
			
	static void deleteExtraFiles() {
		
//		String folder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Chemidplus\\Inhalation LC50 searches";
//		String folder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Chemidplus\\Oral LD50 searches";
		String folder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Chemidplus\\Skin LD50 searches";
		
		File Folder=new File(folder);
		
		File [] files=Folder.listFiles();
		
		for (File file:files) {
			if (!file.getName().contains("_files")) continue;
			
			file.deleteOnExit();
			
			File [] files2=file.listFiles();
			
			if (files2==null) continue;
		
			for (File file2:files2) {
				file2.deleteOnExit();
			}
			
			
			
			System.out.println(file.getName());
		}
		
		
	}

	static void deleteExtraFiles2() {
		
//		String folder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Chemidplus\\Oral LD50 webpages";
		String folder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Chemidplus\\Inhalation LC50 webpages";
		
		File Folder=new File(folder);
		
		File [] files=Folder.listFiles();
		
		for (File file:files) {
			file.deleteOnExit();
			System.out.println(file.getName());
		}
		
		
	}
	// TODO add static strings for above field names

	// /**
	// * Convert to JSON object
	// *
	// * @deprecated - dont need since Chemicals class can convert to JSON all in
	// one step
	// *
	// * @return
	// */
	// public JsonObject toJSON() {
	// JsonObject jo=new JsonObject();
	//
	// jo.addProperty("source", source);
	// jo.addProperty("score", score);
	// jo.addProperty("classification", classification);
	// jo.addProperty("hazard_statement", hazard_statement);
	// jo.addProperty("route", route);
	// jo.addProperty("rationale", rationale);
	// jo.addProperty("note", note);
	//
	// return jo;
	//
	// }

}
