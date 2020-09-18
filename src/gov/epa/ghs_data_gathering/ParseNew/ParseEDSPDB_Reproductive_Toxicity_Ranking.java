
/*A java package is a group of related classes and interfaces.  Using the package AADashboard and parsing a database within it.*/

package gov.epa.ghs_data_gathering.ParseNew;


/*Import files, utilities, and packages.*/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.AADashboard;
import gov.epa.api.Chemical;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.Parse;



/* Within the class ParseEDSPDB_Reproductive_Toxicity_Ranking, implement the Parse interface. Declare methods:
   "public static..." "String" means the method will return a String.
 */

public class ParseEDSPDB_Reproductive_Toxicity_Ranking extends Parse {

	public static String sourceName = ScoreRecord.sourceEDSPDB_Reproductive_Toxicity_Ranking;
	String fileNameSourceExcel = "\\EDSPDB Reproductive Toxicity Ranking.xls";

	public static String mainFolder = AADashboard.dataFolder + File.separator + sourceName;
	public static String jsonFolder = mainFolder + "/json files";

	
/*	Within the subclass ToxicityRecord, declare variables.  All of the variables are declared as String.*/
	
	static class ToxicityRecord {

		String Name;//*used
		String Substance_ID;
		String CASRN;//*used
//		double molecularWeight;
//		Don't need molecular weight anymore.
		
		
		String Assay_ID;
		String RepDev_AbsType;
		String RepDev_Abstract;
		String RepDev_Prim_Ref;
		String RepDev_PeerReview;
		String RepDev_Purity;
		String RepDev_Species;
		String RepDev_Strain;
		String RepDev_TotalTested;
		String RepDev_Male;
		String RepDev_NumMale;
		String RepDev_Female;
		String RepDev_NumFemale;
		String RepDev_Condition;
		String RepDev_DoseReg;
		String RepDev_Route;
		String RepDev_Effect;
		String RepDev_Compartment;
		String RepDev_Outcome;
		String RepDev_Final_Dose;
		String RepDev_Final_Units;
		String RepDev_EffLevelRep;
		String RepDev_EffLevelUnits;
		String RepDev_Endpoint;
		String RepDev_Reference;
		String RepDev_Method;
		String RepDev_Obs_Dose;
		String RepDev_TableEntryRef;
		String RepDev_RTECS_Num;
		String RepDev_Hours;
		String RepDev_Rank;

	}
	
	
	/* Using the Chemical class from gov.epa.api.Chemical, Create a chemical with a ToxicityRecord tr. */
	
	private Chemical createChemical(ToxicityRecord tr) {

		
	/*	Create a new object called "chemical" that is an instance of the class "Chemical". */
		
		Chemical chemical = new Chemical();

	/* For the object called "chemical", the variable "name" has the value of "Name" of the object "tr"
	   For the object called "chemical", the variable "CAS" has the value of "CASRN" of the object "tr". */
		
		chemical.name=tr.Name;
		chemical.CAS=tr.CASRN;
		
		
//		System.out.println(tr.CASRN);
		
	/*	Create a new object called "sr" that is an instance of the class "ScoreRecord". */
		
		
		
	/*	Within the class Score, start with the value of score set to null. */
		
		Score score=null;
		
		
	/* If the value of the variable RepDev_Compartment equals "Reproductive"
	   then the score for the object "chemical" is scoreReproductive.
	   Else if the value of the variable RepDev_Compartment equals "Developmental"
	   then the score for the object "chemical' is scoreDevelopmental.
	   Else print the CASRN from the toxicity record and RepDev_Compartment from the toxicity record and "missing compartment!" */
		
		if (tr.RepDev_Compartment.equals("Reproductive")) {
			score=chemical.scoreReproductive;
		} else if (tr.RepDev_Compartment.equals("Developmental")) {
			score=chemical.scoreDevelopmental;
		} else {
			System.out.println(tr.CASRN+"\t"+tr.RepDev_Compartment+"\tmissing compartment!");
			return chemical;
		}

		
		ScoreRecord sr=new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		
	/* For the object "sr", the variable "source" has the value of "sourceEDSPDB_Reproductive_Toxicity_Ranking"
	   of the object "ScoreRecord". */
		
		sr.source=ScoreRecord.sourceEDSPDB_Reproductive_Toxicity_Ranking;

		

	/* Create a string variable called outcome that equals RepDev_Outcome converted to lowercase.
	   Create a string variable called route that equals RepDev_Route converted to lowercase.
	   Create a variable called dose that is a double. A double is a type of floating-point number, or number with a decimal point.
	   A float is also a type of floating-point number but a double seems to have a wider range.  */
		
		String outcome=tr.RepDev_Outcome.toLowerCase();
		String route = tr.RepDev_Route.toLowerCase();
		double dose;
		
		
		/*Including effLevelUnits variable.*/
		
		String effLevelUnits=tr.RepDev_EffLevelUnits;
		
		
		/*If dose is not missing then dose is Rep_Dev_Final_Dose parsed as a double.
		Note that ! means not.
		Else if dose is missing then dose=-9999.*/
		
		if (!tr.RepDev_Final_Dose.equals("")) {
			dose=Double.parseDouble(tr.RepDev_Final_Dose);
		} else {
			dose=-9999;
		}
		
	/*	If the outcome is negative then the score is equal to very low (VL).
****>	Should we differentiate low vs. very low depending on the highest dose tested? */
	
		
		if (outcome.equals("negative")) {
			sr.score=ScoreRecord.scoreVL;
			
			if (route.equals("")) {
				sr.rationale="Outcome is negative for reproductive or developmental toxicity (route unspecified).";
			} else {
				sr.rationale="Outcome is negative for reproductive or developmental toxicity for "+route+" route.";	
			}
			
//			System.out.println(tr.CASRN+"\t"+sr.rationale);
			
		} else if (outcome.equals("positive")) {
			//TODO
			
			String finalRoute="";
			//IF the route is dermal THEN use DfE criteria for dermal (DfE version 2.0 page 14)
			if (route.equals("administration onto the skin") || route.equals("dermal")) {
				finalRoute="dermal";
				setDermalScore(tr, sr, dose, finalRoute);
				
//			IF the route is oral THEN use DfE criteria for oral (DfE version 2.0 page 14)
			} else if (route.equals("oral") || route.equals("fed diet") || route.equals("in drinking water") ||
					route.equals("ingestion") || route.equals("oral or intraperitoneal") ||
					route.equals("oral via capsule") || route.equals("oral via drinking water") ||
					route.equals("test compound administered in drinking water") || route.equals("oral via feed")) {
				finalRoute="oral";
				setOralScore(tr, sr, dose, finalRoute);
				
//			IF the route is gavage THEN use DfE criteria for oral but note/comment that route is gavage.
			} else if (route.equals("gavage") || route.equals("gastric intubation") ||
					route.equals("gavage route in a 1:1 solution of honey:water") ||
					route.equals("gavage route in corn oil") || route.equals("gavage route in olive oil")
					|| route.equals("oral intubation") || route.equals("oral via gavage") ||
					route.equals("oral via gavage in oil") || route.equals("oral via gavage in water")) {
				finalRoute="gavage";
				setOralScore(tr, sr, dose, finalRoute);
				
				
//			IF the route is inhalation, DFE criteria are in mg/L/day.
			} else if (route.equals("inhalation") || route.equals("inhalation - whole body")) {
				finalRoute="inhalation";
				this.setInhalationScore(tr, sr, finalRoute);
				
		/*	} else if (route.equals("")) {
				sr.score=ScoreRecord.scoreNA;
				
				//TODO- do we want to assign a score if we know it's positive but either have another route or unknown route???
				 * 
				 * Still uncertain about this due to all of the uncertainty associated with comparing different types of studies,
				 * but for now:
				 * 
				 * If the route is missing and RepDev_EffLevelUnits is mg/kg bw/day then using criteria for oral
				 * because most studies reporting mg/kg bw/day are for the oral route.
				 * Alternatively, could use the criteria for dermal to be more protective.
				 * Usually being more protective is better but it seems more likely that most of the studies were
				 * oral rather than dermal.
				 * 
				 * 
				 * If the route is something other than oral, gavage, dermal, or inhalation, then the exposure might not be
				 * physiologically relevant and cutoff points based on a NOAEL or LOAEL are not clear.
				 * 
				 * page 185 of GHS rev. 7 2017 says "Studies involving routes of administration such as
				 * intravenous or intraperitoneal injection, which may result in exposure of the reproductive organs to
				 * unrealistically high levels of the test substance, or elicit local damage to the reproductive organs,
				 * e.g., by irritation, must be interpreted with extreme caution and on their own would not normally be
				 * the basis for classification."
				 * 
				 * Therefore, the score is being left as N/A for routes other than oral, gavage, dermal, or inhalation.
				 * 
				sr.rationale = "Route is unknown.";*/
				
			} else if (route.equals("")) {
				if (effLevelUnits.equals("mg/kg bw/day")) {
					finalRoute="route unknown, using criteria for oral because effect level was reported as mg/kg bw/day";
					setOralScore(tr, sr, dose, finalRoute);
				} else {
				sr.score=ScoreRecord.scoreNA;
				sr.rationale = "Route is unknown."; }
			} else {
				sr.score=ScoreRecord.scoreNA;
				
				//TODO- do we want to assign a score if we know it's positive but either have another route or unknown route???
				
				sr.rationale= "route: " + route + ", Cannot be assigned to oral (or oral gavage), dermal, or inhalation";
				
//				System.out.println(tr.CASRN+"\t"+route);
			}
			
		} else {
			System.out.println(tr.RepDev_Outcome+"\tunknown outcome!!");
			
		}
		
		
	/*	It appears that RepDev_Final_Dose and RepDev_EffLevelRep are equivalent measurements of the LOAEL or NOAEL,
	    but the conversion is complicated when the units are for a concentration vs. a dose.
	    If the route is dermal or oral, using RepDev_Final_Dose for hazard designation because the units are mg/kg bw/day.
		If the route is inhalation, using RepDev_EffLevelRep for hazard designation because the units are
		for a concentration in air.
		*/
	
		
		sr.note="Species: " + tr.RepDev_Species +"<br>"+
				"Original reference: " + tr.RepDev_Prim_Ref + "<br>"+
				"Source database: "+tr.RepDev_Reference + "<br>"+
				"Abstract: " + tr.RepDev_Abstract + "<br>"+
				"Description of effect: " +	tr.RepDev_Effect;
		
	/*			
		The abstract usually has the description of effect but sometimes the abstract is missing,
		so both the abstract and the description of effect are included in the note above.
		
		The abstract also often has information about the quality of the study,
		such as "PEER REVIEWED" or "UNVALIDATED IBT STUDY EFFECT NOT REPRODUCED."
		
		The abstract also sometimes has information about the route of exposure when the RepDev_Route is missing.
		
		The abstract also sometimes has information about the dose when RepDev_EffLevelRep is missing.
		
****> Is there a way to get java to extract information from the abstract?
		For example, maybe make an if statement that says if "unvalidated" appears anywhere in the abstract
		then the score is N/A.
		
		Looking manually at each abstract to get information on route and dose might be feasible for this
		relatively small database but not for larger databases.
		
		Many developmental studies use birds such as chickens because it is easier to observe the embryo.
		
		I would designate the score as N/A for the study that assessed the effects of citronella on mosquitoes,
		but it already is N/A due to route being missing.
		
		
		 
		*/
		
		
		
		System.out.println(tr.CASRN+"\t"+route+"\t"+tr.RepDev_EffLevelRep+"\t"+tr.RepDev_EffLevelUnits+"\t"+tr.RepDev_Final_Dose+"\t"+sr.score+"\t"+sr.rationale+"\t"+sr.note);
			
		
		score.records.add(sr);
				

		return chemical;

	}



	private void setDermalScore(ToxicityRecord tr, ScoreRecord sr, double dose, String finalRoute) {
		sr.rationale="route: " +finalRoute +", ";
		if (dose==-9999) {
//					System.out.println(tr.CASRN+"\t"+finalRoute+", dose is missing!");
			sr.score=ScoreRecord.scoreH;
			sr.rationale +="Outcome was positive but no dose was listed";
			
		} else if (dose<100) {
			sr.score=ScoreRecord.scoreH;
			sr.rationale += tr.RepDev_Endpoint+" " + dose + " mg/kg/day < 100 mg/kg/day";
//					System.out.println(tr.CASRN+"\t"+finalRoute+"\t"+sr.score+"\t"+sr.rationale);
		} else if (dose<=500) {
			sr.score=ScoreRecord.scoreM;
			sr.rationale += " 100 mg/kg/day <= " + tr.RepDev_Endpoint + " " + dose + " mg/kg/day <= 500 mg/kg/day";
//			System.out.println(tr.CASRN+"\t"+finalRoute+"\t"+sr.score+"\t"+sr.rationale);
		} else if (dose<=2000) {
			sr.score=ScoreRecord.scoreL;
			sr.rationale += " 500 mg/kg/day < " + tr.RepDev_Endpoint + " " + dose + " mg/kg/day <= 2000 mg/kg/day";
		} else if (dose>2000) {
			sr.score=ScoreRecord.scoreVL;
			sr.rationale += tr.RepDev_Endpoint+" " + dose + " mg/kg/day > 2000 mg/kg/day";
		} else {
			System.out.println("01:"+tr.CASRN+"\tdermal\t"+dose);
		}
	}
	
	
//	If finalRoute is oral:
	
	
//	private void setOralScore() {
	private void setOralScore(ToxicityRecord tr, ScoreRecord sr, double dose, String finalRoute) {
		sr.rationale="route: " +finalRoute +", ";
		
		if (dose==-9999) {
//					System.out.println(tr.CASRN+"\t"+finalRoute+", dose is missing!");
			sr.score=ScoreRecord.scoreH;
			sr.rationale+="Outcome was positive but no dose was listed";
			
		} else if (dose<50) {
			sr.score=ScoreRecord.scoreH;
			sr.rationale += tr.RepDev_Endpoint+" " + dose + " mg/kg/day < 50 mg/kg/day";
//					System.out.println(tr.CASRN+"\t"+finalRoute+"\t"+sr.score+"\t"+sr.rationale);
		} else if (dose<=250) {
			sr.score=ScoreRecord.scoreM;
			sr.rationale += "50 mg/kg/day <= " + tr.RepDev_Endpoint + " " + dose + " mg/kg/day <= 250 mg/kg/day";
//			System.out.println(tr.CASRN+"\t"+finalRoute+"\t"+sr.score+"\t"+sr.rationale);
		} else if (dose<=1000) {
			sr.score=ScoreRecord.scoreL;
			sr.rationale += "250 mg/kg/day < " + tr.RepDev_Endpoint + " " + dose + " mg/kg/day <= 1000 mg/kg/day";
		} else if (dose>1000) {
			sr.score=ScoreRecord.scoreVL;
			sr.rationale += tr.RepDev_Endpoint+ " " + dose + " mg/kg/day > 1000 mg/kg/day";
		} else {
			System.out.println(tr.CASRN+"\toral\t"+dose);
		}
	}
	
	
//	If finalRoute is gavage: (Don't need this code because it is the same as for finalRoute oral). 
	
//	private void setGavageScore(ToxicityRecord tr, ScoreRecord sr, double dose, String finalRoute) {
//		if (dose==-9999) {
////					System.out.println(tr.CASRN+"\t"+finalRoute+", dose is missing!");
//			sr.score=ScoreRecord.scoreH;
//			
//		} else if (dose<50) {
//			sr.score=ScoreRecord.scoreH;
//			sr.rationale = tr.RepDev_Endpoint+" (" + dose + " mg/kg/day) < 50 mg/kg/day";
////					System.out.println(tr.CASRN+"\t"+finalRoute+"\t"+sr.score+"\t"+sr.rationale);
//		} else if (dose<=250) {
//			sr.score=ScoreRecord.scoreM;
//			sr.rationale = "( 50 mg/kg/day <= the " + tr.RepDev_Endpoint + " " + dose + " mg/kg/day <= 250 mg/kg/day)";
//			System.out.println(tr.CASRN+"\t"+finalRoute+"\t"+sr.score+"\t"+sr.rationale);
//		} else if (dose<=1000) {
//			sr.score=ScoreRecord.scoreL;
//			sr.rationale = "( 250 mg/kg/day < the " + tr.RepDev_Endpoint + " " + dose + " mg/kg/day <= 1000 mg/kg/day)";
//		} else if (dose>1000) {
//			sr.score=ScoreRecord.scoreVL;
//			sr.rationale = tr.RepDev_Endpoint+" (" + dose + " mg/kg/day) > 1000 mg/kg/day";
//		}
//	}
	
	
	
//	If finalRoute is inhalation:
	
	/* "The Superfund Program has updated its inhalation risk paradigm to be compatible with the
	Inhalation Dosimetry Methodology, which represents the Agency's current methodology for
	inhalation dosimetry and derivation of inhalation toxicity values. This document recommends that
	when estimating risk via inhalation, risk assessors should use the concentration of the chemical in air
	as the exposure metric (e.g., mg/m3), rather than inhalation intake of a contaminant in air based on
	IR and BW (e.g., mg/kg-day)."  EPA, Risk Assessment Guidance for Superfund, Volume I: Human Health Evaluation Manual
		(Part F, Supplemental Guidance for Inhalation Risk Assessment), page 2
		(https://www.epa.gov/sites/production/files/2015-09/documents/partf_200901_final.pdf)
*/	
	
	
	/**
	 * 
	 * From the 2009 document for the EPA Methodology For Risk-Based Prioritization Under ChAMP, there are cutoffs based on ppm
	 * on page 12.
	 * Reproductive/Developmental Toxicity Characterization Criteria for Inhalation (gas) (ppm/day)
	 * Hazard Characterization:
	 * High <50 ppm  Moderate 50 -250 ppm Low > 250 ppm
	 * (https://ntrl.ntis.gov/NTRL/dashboard/searchResults/titleDetail/PB2010101039.xhtml)
	 *
	 *
	 *
	 * 
	 * 
	 * @param tr - ToxicityRecord from spreadsheet
	 * @param sr - ScoreRecord to store score and rationale
	 * @param finalRoute - what route (handy for printing to screen)
	 */
	private void setInhalationScore(ToxicityRecord tr, ScoreRecord sr, String finalRoute) {


		String effLevelUnits=tr.RepDev_EffLevelUnits;
		double dose;

		
		if (!tr.RepDev_EffLevelRep.equals("")) {
			tr.RepDev_EffLevelRep=tr.RepDev_EffLevelRep.replace(",", "");
			dose=Double.parseDouble(tr.RepDev_EffLevelRep);
		} else {
			dose=-9999;
			sr.score=ScoreRecord.scoreH;
			sr.rationale="Outcome was positive but no dose was listed";
			return;
		}

/* 	****>		Some of the calculations of dose result in numbers with lots of decimal places.
  				Round to two decimal places or two significant figures?
 				Reading about how to round using Java... */

		//		RepDev_EffLevelRep	RepDev_EffLevelUnits

		if (effLevelUnits.equalsIgnoreCase("mg/m3") || effLevelUnits.equalsIgnoreCase("mg cu/m") || effLevelUnits.equalsIgnoreCase("mg/m cu") ||
				effLevelUnits.equalsIgnoreCase("mg/m3/24h") || effLevelUnits.equalsIgnoreCase("mg/cu m")) {
			
			//1 mg/L= 1000 mg/m3
			dose=dose/1000;
			effLevelUnits="mg/L";
		} else if (effLevelUnits.equalsIgnoreCase("mg/m3/2h")) {
			/*     Conversion to 24 hours based on EPA, Risk Assessment Guidance for Superfund, Volume I: Human Health Evaluation Manual
			(Part F, Supplemental Guidance for Inhalation Risk Assessment), page 5
			(https://www.epa.gov/sites/production/files/2015-09/documents/partf_200901_final.pdf)
			 */
			dose=(dose/1000)*(2.0/24.0);
			effLevelUnits="mg/L";
		} else if (effLevelUnits.equalsIgnoreCase("mg/m3/4h")) {
			dose=(dose/1000)*(4.0/24.0);
			effLevelUnits="mg/L";
		} else if (effLevelUnits.equalsIgnoreCase("mg/m3/6h")) {
			dose=(dose/1000)*(6.0/24.0);
			effLevelUnits="mg/L";
		} else if (effLevelUnits.equalsIgnoreCase("ug/m3") || effLevelUnits.equalsIgnoreCase("ug/m3/24h")) {
//				1 mg/L= 1000 mg/m3 and 1 mg = 1000 ug
			dose = dose/1E6;
			effLevelUnits="mg/L";
		} else if (effLevelUnits.equalsIgnoreCase("ug/m3/4h")) {
			dose = (dose/1e6)*(4.0/24.0);
			effLevelUnits="mg/L";
		} else if (effLevelUnits.equalsIgnoreCase("mg/liter") || effLevelUnits.equalsIgnoreCase("mg/l")) {
			effLevelUnits="mg/L";
		} else if (effLevelUnits.equalsIgnoreCase("%") || effLevelUnits.equalsIgnoreCase("% concn of fuel gas")) {
			dose=dose*10000;
			effLevelUnits="ppm";
		} else if (effLevelUnits.equalsIgnoreCase("ppm")) {
			effLevelUnits="ppm";
		} else if (effLevelUnits.equalsIgnoreCase("ppm/6h")) {
			dose=dose*6/24;
			effLevelUnits="ppm";
		} else if (effLevelUnits.equalsIgnoreCase("ppm/7h")) {
			dose=dose*7/24;
			effLevelUnits="ppm";
		} else if(effLevelUnits.equalsIgnoreCase("ppb/6H/13W-I")) {
//			1 ppm = 1000 ppb.  Assuming 13W-I means 13 weeks and 6H means 6 hours per day.
			dose=(dose/1000)*(6.0/24.0);
			effLevelUnits="ppm";
		} else {
			sr.score=ScoreRecord.scoreNA;
			sr.rationale = "unknown units of "+effLevelUnits;
//			System.out.println(tr.CASRN+"\t units of "+effLevelUnits+" are missing!!!");
			return;
		}


		if (effLevelUnits.equals("mg/L")){
			sr.rationale = "route: " + finalRoute + ", ";
			if (dose<50) {
				sr.score=ScoreRecord.scoreH;
				sr.rationale += tr.RepDev_Endpoint+" " + dose + " mg/L < 1 mg/L/day";
				//					System.out.println(tr.CASRN+"\t"+finalRoute+"\t"+sr.score+"\t"+sr.rationale);
			} else if (dose<=2.5) {
				sr.score=ScoreRecord.scoreM;
				sr.rationale += "1 mg/L/day <=  " + tr.RepDev_Endpoint + " " + dose + " mg/L/day <= 2.5 mg/L/day";
//				System.out.println(tr.CASRN+"\t"+finalRoute+"\t"+sr.score+"\t"+sr.rationale);
			} else if (dose<=20) {
				sr.score=ScoreRecord.scoreL;
				sr.rationale += "2.5 mg/L/day < " + tr.RepDev_Endpoint + " " + dose + " mg/L/day <= 1000 mg/L/day";
			} else if (dose>20) {
				sr.score=ScoreRecord.scoreVL;
				sr.rationale += tr.RepDev_Endpoint+" " + dose + " mg/L/day > 1000 mg/L/day";
			} else {
				System.out.println("units = mg/L and dose = "+dose);
			}
		} else if (effLevelUnits.equals("ppm")) {
			sr.rationale = "route: " + finalRoute +", ";
			//	mgL = (dose*tr.molecularWeight*0.001)/24.45;
			if (dose<50) {
				sr.score=ScoreRecord.scoreH;
				sr.rationale += tr.RepDev_Endpoint+" " + dose + " ppm < 50 ppm";
				//							System.out.println(tr.CASRN+"\t"+finalRoute+"\t"+sr.score+"\t"+sr.rationale);
			} else if (dose<=250) {
				sr.score=ScoreRecord.scoreM;
				sr.rationale += " 50 ppm <= " + tr.RepDev_Endpoint + " " + dose + " ppm <= 250 ppm";
//				System.out.println(tr.CASRN+"\t"+finalRoute+"\t"+sr.score+"\t"+sr.rationale);
			} else if (dose>250) {
				sr.score=ScoreRecord.scoreL;
				sr.rationale += tr.RepDev_Endpoint+" " + dose + " ppm > 250 ppm";
			} else {
				
				System.out.println("units = ppm and dose = "+dose);
				
			}
		} else {
			
			System.out.println("units not mg/L and not ppm: "+effLevelUnits);
			
		}

		
//		if (!tr.RepDev_EffLevelRep.equals("")) {
//			System.out.println(tr.CASRN+"\t"+tr.RepDev_EffLevelRep+"\t"+dose+"\t"+effLevelUnits+"\t"+sr.score+"\t"+sr.rationale);
//		}
		
		 /* Note to self: explanation....
		  * If final route is inhalation:
				if effLevelUnits = ppm
				To convert concentrations in air (at 25 °C) from ppm to mg/m3:
						mg/m3 = (ppm) × (molecular weight of the compound)/(24.45).

						1 milligram per cubic meter ( mg/m3 ) = 0.0010 milligrams per liter ( mg/l ). 

						So 

						mg/L = ((ppm) × (molecular weight of the compound)/(24.45))*0.001

				Don't need to convert to mg/m3 if use the criteria based on ppm.	
		  */
		
		
	}// end method
				

	private Vector<ToxicityRecord> parseExcelFile(String excelFilePath) {

		try {

			Vector<ToxicityRecord> data_field = new Vector<ToxicityRecord>();

			File file = new File(excelFilePath);
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new HSSFWorkbook(inputStream);
			Sheet thirdSheet = workbook.getSheetAt(2);

			int row = 1;

			while (true) {
				Row nextRow = thirdSheet.getRow(row);

				if (nextRow == null) {
					break;
				}

				ToxicityRecord cr = createDataField(nextRow);

				data_field.add(cr);

				row++;
			}

			inputStream.close();
			workbook.close();
			return data_field;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private static ToxicityRecord createDataField(Row row) {
		ToxicityRecord tr = new ToxicityRecord();
		DataFormatter formatter = new DataFormatter();

		int i = 0;

		tr.Name = formatter.formatCellValue(row.getCell(i++));
		tr.Substance_ID = formatter.formatCellValue(row.getCell(i++));
		tr.CASRN = formatter.formatCellValue(row.getCell(i++));
		tr.Assay_ID = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_AbsType = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Abstract = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Prim_Ref = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_PeerReview = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Purity = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Species = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Strain = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_TotalTested = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Male = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_NumMale = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Female = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_NumFemale = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Condition = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_DoseReg = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Route = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Effect = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Compartment = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Outcome = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Final_Dose = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Final_Units = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_EffLevelRep = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_EffLevelUnits = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Endpoint = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Reference = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Method = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Obs_Dose = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_TableEntryRef = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_RTECS_Num = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Hours = formatter.formatCellValue(row.getCell(i++));
		tr.RepDev_Rank = formatter.formatCellValue(row.getCell(i++));

		return tr;
	}

	private void createRecords(String folder, String inputExcelFileName, String outputJSON_Filename) {

		try {

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();

			Vector<ToxicityRecord> cr = parseExcelFile(folder + "/" + inputExcelFileName);

			FileWriter fw = new FileWriter(folder + "/" + outputJSON_Filename);
			fw.write(gson.toJson(cr));
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void createFiles() {

		String fileNameJSON_Records = fileNameSourceExcel.replace(".xls", ".json");

//		if (AADashboard.generateOriginalJSONRecords) {
//			createRecords(fileNameSourceExcel, mainFolder, fileNameJSON_Records);
//		}
		
		try {
			Gson gson = new Gson();
			File jsonFile = new File(mainFolder + "/" + fileNameJSON_Records);
			ToxicityRecord[] records = gson.fromJson(new FileReader(jsonFile), ToxicityRecord[].class);

			
			System.out.println("CASRN\troute\tRepDev_EffLevelRep\tRepDev_EffLevelUnits\tRepDev_Final_Dose\tsr.score\tsr.rationale\tsr.note");

			
			for (int i = 0; i < records.length; i++) {

				ToxicityRecord ir = records[i];
				
//				System.out.println(ir.CASRN);
				
				Chemical chemical = this.createChemical(ir);

				if (chemical == null)
					continue;

				if (chemical.CAS.indexOf("\n") > -1)
					continue;// TODO

				chemical.writeToFile(jsonFolder);

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void createScoreRecord(Score score, ToxicityRecord tr) {
		ScoreRecord sr = new ScoreRecord(score.hazard_name,tr.CASRN,tr.Name);
		score.records.add(sr);
	}

	public static void main(String[] args) {

		ParseEDSPDB_Reproductive_Toxicity_Ranking pe = new ParseEDSPDB_Reproductive_Toxicity_Ranking();

		pe.createFiles();

	}

}
