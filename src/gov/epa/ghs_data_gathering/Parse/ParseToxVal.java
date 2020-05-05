package gov.epa.ghs_data_gathering.Parse;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;

import gov.epa.ghs_data_gathering.API.Chemical;
import gov.epa.ghs_data_gathering.API.Score;
import gov.epa.ghs_data_gathering.API.ScoreRecord;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class ParseToxVal extends Parse {
	
	void getRecordsForCAS(String CAS,String filePathDatabaseAsText,String filePathRecordsForCAS) {
		
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filePathDatabaseAsText));
			
			FileWriter fw=new FileWriter(filePathRecordsForCAS);
			
			String header=br.readLine();
			
			fw.write(header+"\r\n");
			
			int colCAS=Utilities.FindFieldNumber(header, "casrn","\t");
			System.out.println(colCAS);
			
			while (true) {
				
				String Line=br.readLine();
				
//				System.out.println(Line);
				
				if  (Line==null) break;
				
				String [] vals=Line.split("\t");
				
				String currentCAS=vals[colCAS];
				
				if (currentCAS.contentEquals(CAS)) {
					System.out.println(Line);
					fw.write(Line+"\r\n");
				}
				
				
			}
			br.close();
			fw.flush();
			fw.close();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	void goThroughRecords(String filepath,String destfilepath) {
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));								
			
			String header=br.readLine();			
			LinkedList<String>hlist=Utilities.Parse3(header, "\t");
						
			Chemical chemical=new Chemical();
			
			while (true) {
				
				String Line=br.readLine();
				
//				System.out.println(Line);
				
				if  (Line==null) break;
													
				LinkedList<String>list=Utilities.Parse3(Line, "\t");
				
				RecordToxVal r=RecordToxVal.createRecord(hlist, list);
					
				Score score=null;
				
				if (r.risk_assessment_class.contentEquals("acute") && r.human_eco.contentEquals("human health")) {
					createAcuteMammalianToxicityRecords(chemical, r);
					
					/* Added: && r.human_eco.contentEquals("human health")
					 * because there is at least one eco entry labeled "acute"
					 * I seem to remember it should be && instead of & but need to check this.
					 * -Leora 4/23/20 */
					
					
				} else if (r.risk_assessment_class.contentEquals("cancer")) {
					createCancerRecords(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("developmental")) {
					createDevelopmentalRecords(chemical,r);
				
					
					/* Added the rest of the rac here but need to add methods.  -Leora 4/24/20 */
					
				} else if (r.risk_assessment_class.contentEquals("mortality:acute")) {
					createEcoToxAcuteRecords(chemical,r);	
					
				} else if (r.risk_assessment_class.contentEquals("mortality:chronic")) {
					createEcoToxChronicRecords(chemical,r);
					
					/* Need to double check that these are all eco.  -Leora 4/23/20 */
					
				} else if (r.risk_assessment_class.contentEquals("chronic")) {
					createChronicRecords(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("subchronic")) {
					createSubchronicRecords(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("short-term")) {
					createShorttermRecords(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("repeat dose")) {
					createRepeatDoseRecords(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("subacute")) {
					createSubacuteRecords(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("growth:acute")) {
					createGrowthAcuteRecords(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("growth:chronic")) {
					createGrowthChronicRecords(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("reproductive")) {
					createReproductiveRecords(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("neurotoxicity")) {
					createNeurotoxicityRecords(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("developmental neurotoxicity")) {
					createDevelopmentalNeurotoxicityRecords(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("ecotoxicity invertebrate")) {
					createEcotoxInvertebrateRecords(chemical,r);
					
					
					/* There does not appear to be a rac for genetox. However, there is a separate file called
					 * toxval_genetox_summary_2020-01-16
					 * that I downloaded from the ftp site
					 * I want to do something like:
					 * If the chemical is in the toxval_genetox_summary_2020-01-16 file then
					 * createGenetoxScore(chemical,r);
					 * 
					 * -Leora*/
					
					
				//} else {
					//TODO add methods for other risk assessment classes
					/* System.out.println("unknown rac="+r.risk_assessment_class);
					 * 
					 * rac to add -Leora 4/23/20:
					 * 
					 * mortality:acute
					 * mortality:chronic
					 * 
					 * chronic (human health: species include human and rat; eco: different aquatic species)
					 * subchronic (human health: rat)
					 * short-term
					 * repeat dose
					 * subacute
					 * 
					 * growth:acute
					 * growth:chronic
					 *
					 * reproductive
					 * 
					 * neurotoxicity
					 * developmental neurotoxicity
					 * 
					 * ecotoxicity invertebrate
					 * 
					 * 

						Excellent! That’s what you are supposed to get because
						code hasn’t been added to handle these risk assessment classes.
						If you look at the code for the GoThroughRecords method in the
						ParseToxVal class, you will see a block of code like this:
						if (r.risk_assessment_class.contentEquals("acute")) {........
						For right now you can comment out the line above that
						prints unknown rac so that it doesn’t clutter the output.
						So far I have only handled a few of the hazard categories
						(and may not be complete yet).
						
						Look at the code for the first 3
						I added and make sure I did it the same way as Richard.
						I may have restricted the records more than he did-
						for example for acute oral I only used “toxval_type”= “LD50”
						and the 4 species that we used earlier in the ParseChemIdplus class.
						
				****[I think the point is to look at what Richard did and suggest
					changes if we don't agree.  -Leora 4/23/20]****
						
						If you didn’t remember, pressing F3 will jump to a different method when
						the cursor is on it. Alt + left arrow will go back to where you were.
						Todd

					 *  
					 *  */
					
				}
				
				
			}
			
			writeChemicalToFile(chemical, destfilepath);
			
			br.close();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	private void createAcuteMammalianToxicityRecords(Chemical chemical, RecordToxVal r) {
		if(r.exposure_route.contentEquals("oral")) {
			createAcuteMammalianToxicityOralRecord(chemical, r);						
		} else if(r.exposure_route.contentEquals("dermal")) {
			createAcuteMammalianToxicityDermalRecord(chemical, r);
		} else if(r.exposure_route.contentEquals("inhalation")) {
			this.createAcuteMammalianToxicityInhalationRecord(chemical, r);
		}
	}
	
	
	private void createCancerRecords(Chemical chemical, RecordToxVal r) {
		
		/* if cancer_call is not missing then
		 * [L,M,H,VH,N/A based on dictionary that I added for cancer_call]
		 * 	
		 * else if cancer_call is missing or if cancer_call is N/A then
		 * 
		 * I want to code something like the above to use the cancer_call if there is one
		 * and only use the other toxval data if there is no cancer_call.
		 * The cancer call is in the separate file
		 * toxval_cancer_summary_2020-01-16
		 * I downloaded this file from the rtp site.
		 * This file includes 2,827 records.
		 * 
		 * (It looks like there is probably a lot of overlap with this database and our CHA Database.)
		 * 
		 * I created a dictionary in Excel based on the possible cancer_call values:
		 * 
		 * VH: A (Human carcinogen); A (Human carcinogen) (Inhalation route);  B1 (Probable human carcinogen - based on limited evidence of carcinogenicity in humans); B2 (Probable human carcinogen - based on sufficient evidence of carcinogenicity in animals); Carcinogenic to humans; Carcinogenic to humans (Inhalation route); Group 1 - Carcinogenic to humans; Group 2A - Probably carcinogenic to humans; Group A Human Carcinogen; Group A Human Carcinogen by Inhalation; Group B1 Probable Human Carcinogen; Group B2 Probable Human Carcinogen; Group I: CEPA (carcinogenic to humans); Group II: CEPA (probably carcinogenic to humans); "Group II: CEPA (probably carcinogenic to humans) Group 2A: IARC (probably carcinogenic to humans)"; "Group IV: CEPA (unlikely to be carcinogenic to humans) Group 2A: IARC (probably carcinogenic to humans)"; "IRIS (inadequate data for evaluation of carcinogenicity) Group 2A: IARC (probably carcinogenic to humans)"; IRIS (likely to be carcinogenic to humans); Known Human Carcinogen; Known/likely human carcinogen (Oral route); Known/Likely; Known/likely human carcinogen; Known/likely human carcinogen (Inhalation route); Likely Human Carcinogen; Likely to be carcinogenic to humans; Likely to be carcinogenic to humans (Combined route);  Likely to be carcinogenic to humans (inhalation route. IN for oral route); Likely to be carcinogenic to humans (oral route); Likely to be carcinogenic to humans (oral); inadequate (inhalation); Likely to be carcinogenic to humans following prolonged, high-level exposures causing cytotoxicity and regenerative cell hyperplasia in the proximal region of the small intestine (oral exposure) or the respiratory tract (inhalation exposure), but not like; Likely to be carcinogenic to humans for oral exposure. Inadequate information for inhalation.; potential occupational carcinogen; Reasonably Anticipated To Be Human Carcinogen; Suggestive Evidence for Carcinogenicity in Humans; 
		 * H: C (Possible human carcinogen); Group 2B - Possibly carcinogenic to humans; Group 2B: IARC (possibly carcinogenic to humans); Group C Possible Human Carcinogen; Group C: IRIS (a possible human carcinogen); "Group D: IRIS (not classifiable as to human carcinogenicity) (IRIS), 1991/Group 2B: IARC (possibly carcinogenic to humans)/ GCDWQ: HC, 1986"; "Group D: IRIS (not classifiable as to human carcinogenicity); Group 2B: IARC (possibly carcinogenic to humans)"; Group III: CEPA (possible germ cell mutagen, and possibly carcinogenic to humans); Group IIIB (possibly carcinogenic to humans, limited evidence of carcinogenicity); Group IIIB: (possibly carcinogenic to humans); "Likely to be Carcinogenic in Humans at High Doses; Not Likely to be Carcinogenic to Humans at Low Doses"; "Likely to be Carcinogenic to Humans (High Doses), Not Likely to be Carcinogenic to Humans (Low Doses)"; Suggestive evidence of carcinogenic potential; Suggestive evidence of carcinogenic potential (oral. Inhalation is not likely to be carcinogenic); Suggestive evidence of carcinogenic potential for oral exposure.; Suggestive evidence of carcinogenic potential for oral exposure. Inadequate information for inhalation.; Suggestive evidence of the carcinogenic potential for the inhalation route (IN for oral); Suggestive Evidence of Carcinogenicity but Not Sufficient to Assess Human Carcinogenic Potential; Suggestive evidence of carcinogenicity, but not sufficient to assess human carcinogenic potential (Inhalation route)
		 * M: [None of the cancer_call values fit in the M category based on DfE/AA/CHA]
		 * L: E (Evidence of non-carcinogenicity for humans); Group 4 - Probably not carcinogenic to humans; Group E Evidence of Non-carcinogenicity for Humans; Group IV: CEPA (unlikely to be carcinogenic to humans); Group IVC: CEPA (probably not carcinogenic to humans); IOM does not consider manganese carcinogenic to humans; IOM does not consider molybdenum carcinogenic to humans; IOM does not consider zinc carcinogenic to humans; Not Likely to Be Carcinogenic in Humans; Not likely to be carcinogenic to humans (Oral route)
		 * N/A: Carcinogenic potential cannot be determined; Carcinogenic potential cannot be determined (Inhalation route);  Carcinogenic potential cannot be determined (Oral route); CEPA (Although there is some evidence for the carcinogenicity of inorganic fluoride, available data are inconclusive.); D (Not classifiable as to human carcinogenicity); D (Not classifiable as to human carcinogenicity) (Oral route); Data are inadequate for an assessment of human carcinogenic potential; Data are inadequate for an assessment of human carcinogenic potential (Oral route); Group 3 - Not classifiable as to its carcinogenicity to humans; Group D Not Classifiable as to Human Carcinogenicity; Group D: IRIS (not classifiable as to human carcinogenicity); Group V (inadequate data for evaluation of carcinogenicity); Group V: CEPA (probably not carcinogenic to humans); Group VA: CEPA (inadequate data for evaluation); Group VI: CEPA (unclassifiable with respect to carcinogenicity to humans); Group VIA: CEPA (unclassifiable with respect to carcinogenicity to humans); Group VIB: CEPA (unclassifiable with respect to carcinogenesis in humans); Inadequate for an assessment of carcinogenic potential; Inadequate information to assess carcinogenic potential; Inadequate information to assess carcinogenic potential (Oral route); Inadequate information to assess carcinogenic potential (oral, inhalation is not likely to be carcinogenic); "IOM does not consider selenium carcinogenic to humans. Group 3: IARC (not classifiable as to human carcinogenicity) Group B2: U.S. EPA (probable human carcinogen) for selenium sulphide"; IOM, 2001 ("There is little convincing evidence indicating that copper is causally associated with the development of cancer in humans."); IRIS (inadequate data to assess human carcinogenic potential); no adequate data to characterize in terms of carcinogenicity; No Data Available; Not Likely to be Carcinogenic to Humans at Doses that Do Not Alter Rat Thyroid Hormone Homeostasis; Not likely to be carcinogenic to Non-humans; Not Yet  Determined
		 * 
		 * Need to convert this dictionary to Java code.
		 * 
		 * -Leora
		 *  */

		if (r.toxval_type.contentEquals("NOAEL") && r.toxval_units.contentEquals("mg/kg bw/day")) {
			//TODO
			
			/*I added units above.  I want to say if the content contains "mg/kg bw/day" so that would include
			 *cases where it says something additional such as "mg/kg bw/day (actual dose received)."
			 *Need to figure out how to do that.
			 * units for acrylamide:
			 * mg/kg bw/day
			 * mg/kg bw/day (actual dose received) 
			 * 
			 * NOAEL for acrylamide = 0.5 or 0.1
			 * 
			 * Determining cutoffs based on ToxVals has a lot of uncertainty because typically, a weight of evidence
			 * approach is used to qualitatively estimate risk for cancer.
			 * Thus, DfE and GHS and the Tiger Team don't have numeric cutoffs for cancer.
			 * 
			 * So I'm not sure whether we should use numeric cutoffs.
			 * 
			 * Maybe we should only use the data that have a cancer_call,
			 * though that is a relatively small number of chemicals.
			 * 
			 *
			 * 
			 * If we want to consider numeric cutoffs for the dictionary for NOAEL:
			 * 
			 * If toxval_numeric_qualifier is = or >= or >
			 * 
			 * Maybe:
			 * >0 & <=1 VH
			 * >1 & <=10 H
			 * >10 & <=1000 M
			 * >1000 L
			 * 
			 * * If toxval_numeric_qualifier is <= or < & toxval_numeric is <=1 then score= VH
			 * else if toxval_numeric_qualifier is <= or < then skip or score=n/a
			 * 
			 * The NOAEL is used to calculate the RfD in humans by dividing by uncertainty factors.
			 * Typically, the NOAEL would be divided by at least 100.
			 * 
			 * -Leora
			 * */
			
		} else if (r.toxval_type.contentEquals("cancer unit risk")&& r.toxval_units.contentEquals("ug/m3)-1")) {
			//TODO
			
			/* 
			 * The cancer unit risk is an estimate of the increased cancer risk from (usually inhalation) exposure
			 * to a concentration of 1 µg/m3 for a lifetime.
			 * 
			 * I added units because the meaning of the numeric values will change if the units are different.
			 * For acrylamide, the only units for unit risk are (ug/m3)-1.  This is the typical units, but
			 * need to check whether there are any other units for other chemicals.
			 * 
			 * For acrylamide, the unit risk is 0.0001.
			 * 
			 * Options for dictionary for unit risk:
			 * 
			 * Option 1.  If there is a unit risk (or unit risk greater than zero) then score VH
			 * because a unit risk indicates risk in humans.
			 * 
			 * Option 2.  Numeric cutoff points: maybe:
			 * 
			 * If toxval_numeric_qualifier is = or < or <=
			 * 
			 *  >= 0.000001 (1 in a million) VH
			 *  < 0.000001 & >= 0.0000001 H
			 *  < 0.0000001 & >= 0.00000001 M
			 *  = 0 L
			 *  [blank] N/A
			 *  
			 * If toxval_numeric_qualifier is >= or > & toxval_numeric is >= 0.000001 then score= VH
			 * else if toxval_numeric_qualifier is >= or > then skip or score=n/a
			 * 
			 *  
			 *  -Leora
			 */
			
			
		} else {//need code for all important toxval_types
			
			/* NOAEL and cancer unit risk are the only toxval_types for acrylamide. -Leora 4/23/20 */
			
			
			/* Richard seemed to be suggesting that we treat all toxval_types and all rac the same and just use
			If(min value < 1) high
			Else if(min value < 10) medium
			Else low
			I think that is oversimplifying and doesn't scale properly
			and we need to use appropriate cutoffs for the different rac.
			-Leora
 
*/
			
			
		}

		
	}

	/* There does not appear to be a rac for genetox. However, there is a separate file called
	 toxval_genetox_summary_2020-01-16
	 that I downloaded from the ftp site
	 I want to do something like:
	 If the chemical is in the toxval_genetox_summary_2020-01-16 file then
	 createGenetoxScore(chemical,r);
	  
	 Need to import the toxval_genetox_summary_2020-01-16 Excel file.  Then:
	 private void createGenetoxRecords(Chemical chemical, RecordToxVal r) {
	 if genetox_call = "clastogen" OR "gentox" OR "pred clastogen" OR "pred gentox" then score= VH
	 [the vertical line key to indicate "OR" is not working on my keyboard]
	 [there are no genetox_call data that would indicate H or M]
	 if genetox_call = "non gentox" OR "pred non gentox" then score= L
	 if genetox_call = "inconclusive" OR "not clastogen" then score= N/A
	 -Leora */
			
			
	private void createDevelopmentalRecords(Chemical chemical, RecordToxVal r) {
		if (r.toxval_type_supercategory.contentEquals("Point of Departure") &&
			r.toxval_units.contentEquals("mg/kg bw/day") && r.exposure_route.contentEquals("oral")) {
			createDevelopmentalOralRecord(chemical, r);	
		} else if(r.toxval_type_supercategory.contentEquals("Point of Departure") &&
			r.toxval_units.contentEquals("mg/kg bw/day") && r.exposure_route.contentEquals("dermal")) {
			createDevelopmentalDermalRecord(chemical, r);
		} else if(r.toxval_type_supercategory.contentEquals("Point of Departure") &&
				r.toxval_units.contentEquals("mg/L/day") && r.exposure_route.contentEquals("inhalation")) {
			createDevelopmentalInhalationRecord(chemical, r);
		}
	}	
			
	private void createDevelopmentalOralRecord(Chemical chemical, RecordToxVal tr) {
			System.out.println("Creating Developmental Oral Record");
			//This is not printing.

			ScoreRecord sr = new ScoreRecord();
			sr = new ScoreRecord();
			sr.source = ScoreRecord.sourceToxVal;
			sr.sourceOriginal=tr.source;
			
			sr.route = "Oral";

			sr.valueMassOperator=tr.toxval_numeric_qualifier;
			sr.valueMass = Double.parseDouble(tr.toxval_numeric);
			sr.valueMassUnits = tr.toxval_units;

			setDevelopmentalOralScore(sr, chemical);

			sr.note=this.createNote(tr);
				
			chemical.scoreDevelopmental.records.add(sr);
				
		}
	
				
	private void setDevelopmentalOralScore(ScoreRecord sr, Chemical chemical) {
				
		sr.rationale = "route: " + sr.route + ", ";
		double dose = sr.valueMass;
		String strDose = this.formatDose(dose);
							
		System.out.println(chemical.CAS+"\t"+strDose);
					
		System.out.println("****"+strDose);			
				
		if (dose < 50) {
			sr.score = ScoreRecord.scoreH;
			sr.rationale = "Oral POD" + " (" + strDose + " mg/kg) < 50 mg/kg";
		} else if (dose >= 50 && dose <= 250) {
			sr.score = ScoreRecord.scoreM;
			sr.rationale = "50 mg/kg <= Oral POD (" + strDose + " mg/kg) <=250 mg/kg";
		} else if (dose > 250) {
			sr.score = ScoreRecord.scoreL;
			sr.rationale = "Oral POD" + "(" + strDose + " mg/kg) > 250 mg/kg";
		} else { System.out.println(chemical.CAS + "\toral\t" + strDose);
				 
		}
			
		}

		private void createDevelopmentalDermalRecord(Chemical chemical, RecordToxVal tr) {
			System.out.println("Creating Developmental Dermal Record");

			ScoreRecord sr = new ScoreRecord();
			sr = new ScoreRecord();
			sr.source = ScoreRecord.sourceToxVal;
			sr.sourceOriginal=tr.source;

			sr.valueMassOperator=tr.toxval_numeric_qualifier;
			sr.valueMass = Double.parseDouble(tr.toxval_numeric);
			sr.valueMassUnits = tr.toxval_units;

			setDevelopmentalDermalScore(sr, chemical);

			sr.note=this.createNote(tr);
					
			chemical.scoreDevelopmental.records.add(sr);
					
			}
		
					
		private void setDevelopmentalDermalScore(ScoreRecord sr, Chemical chemical) {
					
			sr.rationale = "route: " + sr.route + ", ";
			double dose = sr.valueMass;
			String strDose = this.formatDose(dose);
				
			if (dose < 100) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = "Dermal POD" + " (" + strDose + " mg/kg) < 100 mg/kg";
			} else if (dose >= 100 && dose <= 500) {
				sr.score = ScoreRecord.scoreM;
				sr.rationale = "100 mg/kg <= Dermal POD (" + strDose + " mg/kg) <=500 mg/kg";
			} else if (dose > 500) {
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Dermal POD" + "(" + strDose + " mg/kg) > 500 mg/kg";
					/*
					 * } else { System.out.println(chemical.CAS + "\toral\t" + strDose);
					 */
			}
		}
				
				
				
		private void createDevelopmentalInhalationRecord(Chemical chemical, RecordToxVal tr){
					// System.out.println("Creating Developmental Inhalation Record");

					ScoreRecord sr = new ScoreRecord();
					sr = new ScoreRecord();
					sr.source = ScoreRecord.sourceToxVal;
					sr.sourceOriginal=tr.source;

					sr.valueMassOperator=tr.toxval_numeric_qualifier;
					sr.valueMass = Double.parseDouble(tr.toxval_numeric);
					sr.valueMassUnits = tr.toxval_units;

					setDevelopmentalDermalScore(sr, chemical);

					sr.note=this.createNote(tr);
					
					chemical.scoreDevelopmental.records.add(sr);
					
				}
		
		private void setDevelopmentalInhalationScore(ScoreRecord sr, Chemical chemical) {
					
						sr.rationale = "route: " + sr.route + ", ";
						double dose = sr.valueMass;
						String strDose = this.formatDose(dose);
						
//						System.out.println(chemical.CAS+"\t"+strDose);
						
//						System.out.println("****"+strDose);					
				
				
					if (dose < 1) {
						sr.score = ScoreRecord.scoreH;
						sr.rationale = "Inhalation POD" + " (" + strDose + " mg/L) < 1 mg/L";
					} else if (dose >= 1 && dose <= 2.5) {
						sr.score = ScoreRecord.scoreM;
						sr.rationale = "1 mg/L <= Inhalation POD (" + strDose + " mg/L) <=2.5 mg/L";
					} else if (dose > 2.5) {
						sr.score = ScoreRecord.scoreL;
						sr.rationale = "Inhalation POD" + "(" + strDose + " mg/L) > 2.5 mg/L";
						/*
						 * } else { System.out.println(chemical.CAS + "\toral\t" + strDose);
						 */
					}
			}		
		
		/* Just doing if toxval_type_supercategory is "Point of Departure"
		 * rather than separating NOAEL, LOAEL, NEL, LEL.
		 * DfE is based on the NOAEL OR LOAEL.  Maybe we should omit NEL and LEL.
		 * 
		 * DfE criteria for Reproductive and Developmental Toxicity:
		 * DfE has no VH for Reproductive and Developmental Toxicity
		 * Dfe has a VL category, which will just be included in the L category here.
		 * 
		 * Route						H				M			L				(VL)
		 * Oral(mg/kg/day)				< 50		50 - 250	250 - 1000			(> 1000)
		 * Dermal (mg/kg/day)			< 100		100 - 500	> 500 - 2000		(> 2000)
		 * Inhalation (mg/L/day)
		 * 		vapor/gas				< 1			1 - 2.5		> 2.5 - 20			(> 20)
		 * 		dust/mist/fume			< 0.1		0.1 - 0.5	> 0.5 - 5			(> 5)
		 * 
		 * For inhalation, using DfE criteria for vapor/gas because that's what we did for acute toxicity.
		 * But if vapor/gas vs. dust/mist/fume is specified in ToxVal, then we should use the specific criteria.
		 * For acrylamide, data only includes oral (no inhalation data),
		 * so I need to look at the data for other chemicals.
		 * -Leora
		 * */						
		 

	
	private void createEcoToxAcuteRecords(Chemical chemical, RecordToxVal tr) {

			ScoreRecord sr = new ScoreRecord();
			sr = new ScoreRecord();
			sr.source = ScoreRecord.sourceToxVal;
			sr.sourceOriginal=tr.source;
		

			sr.valueMassOperator=tr.toxval_numeric_qualifier;
			sr.valueMass = Double.parseDouble(tr.toxval_numeric);
			sr.valueMassUnits = tr.toxval_units;

			setEcoToxAcuteScore(sr, chemical);

			sr.note=this.createNote(tr);
				
			chemical.scoreEcoToxAcute.records.add(sr);
			//Need to address this error message.
				
		}
	
				
	private void setEcoToxAcuteScore(ScoreRecord sr, Chemical chemical) {
				
		sr.rationale = "route: " + sr.route + ", ";
		double dose = sr.valueMass;
		String strDose = this.formatDose(dose);		
		
		/* DfE criteria:
		 * LC50 or EC50
		 * mg/L
		 * < 1.0 VH
		 * 1 - 10 H
		 * >10 - 100 M
		 * >100 L
		 * -Leora */
				
		if (dose < 1) {
			sr.score = ScoreRecord.scoreVH;
			sr.rationale = "LC50 or EC50" + " (" + strDose + " mg/L) < 1 mg/L";
		} else if (dose >= 1 && dose <= 10) {
			sr.score = ScoreRecord.scoreH;
			sr.rationale = "1 mg/kg <= LC50 or EC50 (" + strDose + " mg/L) <=10 mg/L";
		} else if (dose > 10 && dose <= 100) {
			sr.score = ScoreRecord.scoreM;
			sr.rationale = "10 mg/kg < LC50 or EC50 (" + strDose + " mg/L) <=100 mg/L";
		} else if (dose > 100) {
			sr.score = ScoreRecord.scoreL;
			sr.rationale = "LC50 or EC50" + "(" + strDose + " mg/L) > 100 mg/L";
		} else { System.out.println(chemical.CAS + "\tEcoToxAcute\t" + strDose);
				 
		}
	}
		
	
	
	private void createEcoToxChronicRecords(Chemical chemical, RecordToxVal tr) {
		
		ScoreRecord sr = new ScoreRecord();
		sr = new ScoreRecord();
		sr.source = ScoreRecord.sourceToxVal;
		sr.sourceOriginal=tr.source;
	

		sr.valueMassOperator=tr.toxval_numeric_qualifier;
		sr.valueMass = Double.parseDouble(tr.toxval_numeric);
		sr.valueMassUnits = tr.toxval_units;

		setEcoToxChronicScore(sr, chemical);

		sr.note=this.createNote(tr);
			
		chemical.scoreEcoToxChronic.records.add(sr);
		//Need to address this error message.
		
		
	}
	
	private void setEcoToxChronicScore(ScoreRecord sr, Chemical chemical) {
		
		sr.rationale = "route: " + sr.route + ", ";
		double dose = sr.valueMass;
		String strDose = this.formatDose(dose);	
		
		/* DfE criteria:
		 * NOEC or LOEC
		 * mg/L
		 * < 0.1 VH
		 * 0.1 - 1 H
		 * > 1 - 10 M
		 * > 10 L
		 * -Leora */
		
		if (dose < .1) {
			sr.score = ScoreRecord.scoreVH;
			sr.rationale = "NOEC or LOEC" + " (" + strDose + " mg/L) < 1 mg/L";
		} else if (dose >= 0.1 && dose <= 1) {
			sr.score = ScoreRecord.scoreH;
			sr.rationale = "0.1 mg/L <= NOEC or LOEC (" + strDose + " mg/L) <=1 mg/L";
		} else if (dose > 1 && dose <= 10) {
			sr.score = ScoreRecord.scoreM;
			sr.rationale = "1 mg/L < NOEC or LOEC (" + strDose + " mg/L) <=10 mg/L";
		} else if (dose > 10) {
			sr.score = ScoreRecord.scoreL;
			sr.rationale = "NOEC or LOEC" + "(" + strDose + " mg/L) > 10 mg/L";
		} else { System.out.println(chemical.CAS + "\tEcoToxChronic\t" + strDose);
		
	}
		
	}
	
	private void createChronicRecords(Chemical chemical, RecordToxVal r) {
		
		// Need to add method.  -Leora 4/24/20
		
	}
	
	private void createSubchronicRecords(Chemical chemical, RecordToxVal r) {
		
		/*
		 * // study_duration_value and study_duration_units can be used to determine the
		 * actual duration for studies called subchronic, short term, or repeat dose.
		 * Then DfE criteria for repeated dose toxicity (28, 40-50, or 90 days) can be used.
		 */
		
	}

	private void createShorttermRecords(Chemical chemical, RecordToxVal r) {
		
		// Need to add method.  -Leora 4/24/20
		
	}

	private void createRepeatDoseRecords(Chemical chemical, RecordToxVal r) {
		
		// For repeat dose, if study_type is... need to come back to this. 
		
	}
	
	private void createSubacuteRecords(Chemical chemical, RecordToxVal r) {
		
		// Need to add method.  -Leora 4/24/20
		
	}
	
	private void createGrowthAcuteRecords(Chemical chemical, RecordToxVal r) {
		
		// Growth is for ecotox.  Maybe use the same cutoffs as ecotox.
		
	}
	
	private void createGrowthChronicRecords(Chemical chemical, RecordToxVal r) {
		
		// Growth is for ecotox.  Maybe use the same cutoffs as ecotox.
		
	}
	
	private void createReproductiveRecords(Chemical chemical, RecordToxVal r) {
		
		/* Reproductive will have the same code as Developmental (same DfE criteria),
		 * which is detailed above.  I'll add the code for Reproductive when I'm sure Developmental is correct.
		 * -Leora */
		
	}
	
	private void createNeurotoxicityRecords(Chemical chemical, RecordToxVal r) {
		
		// study_duration_value and study_duration_units
		// DfE criteria
		
	}
	
	private void createDevelopmentalNeurotoxicityRecords(Chemical chemical, RecordToxVal r) {
		
		/* DevelopmentalNeurotoxicity will have the same code as Developmental (same DfE criteria),
		 * which is detailed above.
		 * -Leora */
		
	}
	
	private void createEcotoxInvertebrateRecords(Chemical chemical, RecordToxVal r) {
		
		// Need to add method.  -Leora 4/24/20
		
	}
	
	
	
	private void createAcuteMammalianToxicityInhalationRecord(Chemical chemical, RecordToxVal tr) {
		// System.out.println("Creating AcuteMammalianToxicityInhalationRecord");

		ScoreRecord sr = new ScoreRecord();
		sr = new ScoreRecord();
		sr.source = ScoreRecord.sourceToxVal;
		sr.sourceOriginal=tr.source;

		sr.route = "Inhalation";
		
		//TODO - do we only want to use LC50? I know richard might not restrict to LC50s
		
		/*
		 * The Tiger Team didn't use acute tox by itself, only HER/BER/TER. Richard's
		 * code doesn't specify LC50.
		 * -Leora 4/24/20
		 */
		
		if (!tr.toxval_type.contentEquals("LC50")) { 
			System.out.println("invalid inhalation toxval_type="+tr.toxval_type);
			return;
		}
		
		/*
		 * This is printing {invalid inhalation toxval_type=LC0.  I wouldn't use LC0 (the maximum dose without deaths).
		 * We probably should just restrict to LC50 because LC50 is the typical acute inhalation toxicity value.
		 * -Leora
		 */
		
		
		/*
		 * EPA Health Effects Test Guidelines OPPTS 870.1300 Acute Inhalation Toxicity:
		 * "Although several mammalian test species may be used, the preferred species
		 * is the rat. Commonly used laboratory strains should be employed. If another
		 * mammalian species is used, the tester should provide justification and
		 * reasoning for its selection."
		 */

		ArrayList<String> okSpecies = new ArrayList<String>();
		okSpecies.add("rat");
		okSpecies.add("mouse");// include?
		okSpecies.add("rabbit");//
		okSpecies.add("guinea pig");

		sr.valueMassOperator=tr.toxval_numeric_qualifier;
		sr.valueMass = Double.parseDouble(tr.toxval_numeric);
		sr.valueMassUnits = tr.toxval_units;
		
		/* I think I understand the code.  This basically renames what Richard called toxval_numeric
		 * into valueMass and then valueMass is renamed score,
		 * so then for AcuteMammalianToxicity, the same code that we used for the
		 * AA Dashboard/CHA Database is directly used.  -Leora  */
		


		if (!okSpecies.contains(tr.species_common))//TODO- does richard use all species???
			return;

		/* It looks like Richard's code doesn't mention species for acute tox.
		 * So presumably all mammalian species in the ToxVal database were included for the Tiger Team.
		 * 
		 * But other work specified species. For example,
		 * In Grace's Science Webinar presentation on 4/22/20, she talked about using ToxVal data to develop TTC.
		 * She filtered from ToxVal:
		 * toxval type: NO(A)EL or NO(A)EC
		 * species: rats, mice, rabbits
		 * To derive representative values, she removed outliers that exceeded the IQR.
		 * 
		 * For acrylamide, the only mammals with data for acute toxicity are the same mammals that we included
		 * (rat, mouse, rabbit, guinea pig).
		 * Need to check whether this is the case for all chemicals.
		 * 
		 * I think the more important thing for the code is to specify that the species_supercategory is mammals.
		 * Need to add code to do this.
		 * 
		 * -Leora 4/23/20 */

		setInhalationScore(sr, chemical);

		sr.note=this.createNote(tr);
		
		chemical.scoreAcute_Mammalian_ToxicityInhalation.records.add(sr);

	}
	
	private void setInhalationScore(ScoreRecord sr, Chemical chemical) {

		sr.rationale = "route: " + sr.route + ", ";
		double dose = sr.valueMass;
		String strDose = this.formatDose(dose);
		
		System.out.println(chemical.CAS+"\t"+strDose);
		
		System.out.println("****"+strDose);
		
		// These statements aren't printing.
		
		if (sr.valueMassOperator.equals(">")) {

			if (dose >= 20) {// >20
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Inhalation LC50 ( > " + strDose + " mg/L) > 20 mg/L";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = "Inhalation LC50 ( > " + strDose
						+ " mg/L) does not provide enough information to assign a score";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			}

		} else if (sr.valueMassOperator.equals("<")) {
			System.out.println(chemical.CAS + "\tless than operator detected for inhalation\t" + dose);

		} else if (sr.valueMassOperator.equals("") || sr.valueMassOperator.equals("=") || sr.valueMassOperator.equals("~")) {
			if (dose <= 2) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = "Inhalation LC50 (" + strDose + " mg/L) <= 2  mg/L";
			} else if (dose > 2 && dose <= 10) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = "50 mg/L < Inhalation LC50 (" + strDose + " mg/L) <=10 mg/L";
			} else if (dose > 10 && dose <= 20) {
				sr.score = ScoreRecord.scoreM;
				sr.rationale = "300 mg/L < Inhalation LC50 (" + strDose + " mg/L) <=20 mg/L";
			} else if (dose > 20) {// >20
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Inhalation LC50 (" + strDose + " mg/L) > 20 mg/L";
			} else {
				System.out.println(chemical.CAS + "\tinhalation\t" + dose);
				
			}
		} else {
			System.out.println("Unknown operator: "+sr.valueMassOperator+" for acute inhalation");
		}
	}
	
	private void createAcuteMammalianToxicityDermalRecord(Chemical chemical, RecordToxVal tr) {
		// System.out.println("Creating AcuteMammalianToxicityDermalRecord");

		
		if (!tr.toxval_type.contentEquals("LD50")) { 
			System.out.println("invalid dermal toxval_type="+tr.toxval_type);
			return;
		}
		
		ScoreRecord sr = new ScoreRecord();
		sr = new ScoreRecord();

		sr.route = "Dermal";
		sr.source = ScoreRecord.sourceToxVal;
		sr.sourceOriginal=tr.source;
		sr.sourceOriginal=tr.source;


		/*
		 * EPA Health Effects Test Guidelines OPPTS 870.1200 Acute Dermal Toxicity: "The
		 * rat, rabbit, or guinea pig may be used. The albino rabbit is preferred
		 * because of its size, ease of handling, skin permeability, and extensive data
		 * base. Commonly used laboratory strains should be employed. If a species other
		 * than rats, rabbits, or guinea pigs is used, the tester should provide
		 * justification and reasoning for its selection.
		 */

		ArrayList<String> okSpecies = new ArrayList<String>();
		okSpecies.add("rabbit");
		okSpecies.add("rat");
		okSpecies.add("guinea pig");
		okSpecies.add("mouse");// include?

		
		sr.valueMassOperator=tr.toxval_numeric_qualifier;
		sr.valueMass = Double.parseDouble(tr.toxval_numeric);
		sr.valueMassUnits = tr.toxval_units;


		if (!okSpecies.contains(tr.species_common))//TODO- does richard use all species???
			return;


		setDermalScore(sr, chemical);

		sr.note=this.createNote(tr);
		chemical.scoreAcute_Mammalian_ToxicityDermal.records.add(sr);

	}
	
	void writeChemicalToFile(Chemical chemical, String filepath) {

		try {
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			
			FileWriter fw = new FileWriter(filepath);
			String strRecords=gson.toJson(chemical);
			
//			getUniqueChars(strRecords);
			
			
			strRecords=this.fixChars(strRecords);
			
			fw.write(strRecords);
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	private void createAcuteMammalianToxicityOralRecord(Chemical chemical, RecordToxVal tr) {
		// System.out.println("Creating AcuteMammalianToxicityOralRecord");

		
		if (!tr.toxval_type.contentEquals("LD50")) { 
			System.out.println("invalid oral toxval_type="+tr.toxval_type);
			return;
		}
		
		ScoreRecord sr = new ScoreRecord();
		sr = new ScoreRecord();
		
		
//		System.out.println(tr.toxval_type);
		
		
		sr.source = ScoreRecord.sourceToxVal;
		sr.sourceOriginal=tr.source;
		sr.route = "Oral";

		ArrayList<String> okSpecies = new ArrayList<String>();
		okSpecies.add("mouse");// 27796
		okSpecies.add("rat");// 13124
		okSpecies.add("rabbit");// 1089
		okSpecies.add("guinea pig");// 970

		sr.valueMassOperator=tr.toxval_numeric_qualifier;
		sr.valueMass = Double.parseDouble(tr.toxval_numeric);
		sr.valueMassUnits = tr.toxval_units;


		if (!okSpecies.contains(tr.species_common))//TODO- does richard use all species???
			return;

		// System.out.println(chemical.CAS+"\t"+tr.ReportedDose+"\t"+tr.NormalizedDose);

		setOralScore(sr, chemical);

		sr.note=this.createNote(tr);
		chemical.scoreAcute_Mammalian_ToxicityOral.records.add(sr);

	}
	
	private String createNote(RecordToxVal tr) {
//		Organism 	Test Type 	Route 	Reported Dose (Normalized Dose) 	Effect 	Source
		String note="Test organism: " + tr.species_common+"<br>\r\n";
		note+="Reported Dose: "+tr.toxval_numeric_original+"<br>\r\n";
		note+="Normalized Dose: "+tr.toxval_numeric+"<br>\r\n";
		
//		if (tr.Effect==null || tr.Effect.equals("")) {
//			tr.Effect="N/A";
//		}
		note+="Source: "+tr.source;
		return note; 
		
	}
	
	
	private String formatDose(double dose) {
		DecimalFormat df = new DecimalFormat("0.00");
		DecimalFormat df2 = new DecimalFormat("0");
		DecimalFormat dfSci = new DecimalFormat("0.00E00");

		double doseRoundDown = Math.floor(dose);

		double percentDifference = Math.abs(doseRoundDown - dose) / dose * 100.0;

		if (dose < 0.01) {
			return dfSci.format(dose);
		} else {
			if (percentDifference > 0.1) {
				return df.format(dose);
			} else {
				return df2.format(dose);
			}
		}

	}
	private void setOralScore(ScoreRecord sr, Chemical chemical) {
		double dose = sr.valueMass;
		String strDose = this.formatDose(dose);

		// System.out.println(chemical.CAS+"\t"+dose+"\t"+strDose);

		if (sr.valueMassOperator.equals(">")) {

			if (dose >= 2000) {// >2000
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Oral LD50 ( > " + strDose + " mg/kg) > 2000 mg/kg";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = "Oral LD50 ( > " + strDose
						+ " mg/kg) does not provide enough information to assign a score";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			}

		} else if (sr.valueMassOperator.equals("<")) {
			System.out.println(chemical.CAS + "\tless than operator detected for oral\t" + dose);

		} else if (sr.valueMassOperator.equals("") || sr.valueMassOperator.equals("=") || sr.valueMassOperator.equals("~")) {
		/*	if (dose <= 50) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = "Oral LD50" + "(" + strDose + " mg/kg) <= 50 mg/kg";*/
			if (dose <= 50) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = "Oral LD50" + " (" + strDose + " mg/kg) <= 50 mg/kg";
//		Added a space " ("  -Leora V
			} else if (dose > 50 && dose <= 300) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = "50 mg/kg < Oral LD50 (" + strDose + " mg/kg) <=300 mg/kg";
			} else if (dose > 300 && dose <= 2000) {
				sr.score = ScoreRecord.scoreM;
				sr.rationale = "300 mg/kg < Oral LD50 (" + strDose + " mg/kg) <=2000 mg/kg";
			} else if (dose > 2000) {// >2000
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Oral LD50" + "(" + strDose + " mg/kg) > 2000 mg/kg";
			} else {
				System.out.println(chemical.CAS + "\toral\t" + strDose);
			}
		} else {
			System.out.println("Unknown operator: "+sr.valueMassOperator+" for acute oral");
		}
	}
	
	
	private void setDermalScore(ScoreRecord sr, Chemical chemical) {
		double dose = sr.valueMass;

		String strDose = this.formatDose(dose);

		if (sr.valueMassOperator.equals(">")) {

			if (dose >= 2000) {// >2000
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Dermal LD50 ( > " + strDose + " mg/kg) > 2000 mg/kg";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			} else {
				sr.score = ScoreRecord.scoreNA;
				sr.rationale = "Dermal LD50 ( > " + strDose
						+ " mg/kg) does not provide enough information to assign a score";
				// System.out.println(chemical.CAS+"\t"+sr.rationale);
			}

		} else if (sr.valueMassOperator.equals("<")) {
			System.out.println(chemical.CAS + "\tless than operator detected for dermal\t" + dose);

		} else if (sr.valueMassOperator.equals("") || sr.valueMassOperator.equals("=") || sr.valueMassOperator.equals("~")) {
	/*		if (dose <= 200) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = "Dermal LD50 + (" + strDose + " mg/kg) <= 200 mg/kg";*/
			if (dose <= 200) {
				sr.score = ScoreRecord.scoreVH;
				sr.rationale = "Dermal LD50 (" + strDose + " mg/kg) <= 200 mg/kg";
//		I deleted the plus sign after "Dermal LD50".  -Leora V
			} else if (dose > 200 && dose <= 1000) {
				sr.score = ScoreRecord.scoreH;
				sr.rationale = "200 mg/kg < Dermal LD50 (" + strDose + " mg/kg) <=1000 mg/kg";
			} else if (dose > 1000 && dose <= 2000) {
				sr.score = ScoreRecord.scoreM;
				sr.rationale = "1000 mg/kg < Dermal LD50 (" + strDose + " mg/kg) <=2000 mg/kg";
			} else if (dose > 2000) {// >2000
				sr.score = ScoreRecord.scoreL;
				sr.rationale = "Dermal LD50 (" + strDose + " mg/kg) > 2000 mg/kg";
			} else {
				System.out.println(chemical.CAS + "\tDermal\t" + strDose);
			}
		} else {
			System.out.println("Unknown operator: "+sr.valueMassOperator+" for acute dermal");
		}

	}
	
	/* So this class is just for creating the individual scores.  Do we also want to integrate into one score
	 * for each chemical?  Or is it actually best to not even do that so that we are not assigning a "final" score?
	 * But integrating the scores is one of the things that I've been contemplating for the ToxVal data.
	 * As we discussed, it might make sense to use the priority_id field and take the minimum score from each of
	 * the seven priority_id categories and then priority_id 1>2>3>4>5>6>7 in the trumping method.
	 * 
	 * Also, instead of, or in combination with, the trumping scheme, we could remove extreme outliers and
	 * then take the minimum of the remaining scores.
	 * Since the values are continuous instead of ordinal, removing outliers makes sense. 
	 * 
	 * In Grace's Science Webinar presentation on 4/22/20, she talked about using ToxVal data to develop TTC.
	 * She filtered from ToxVal:
	 * toxval type: NO(A)EL or NO(A)EC
	 * species: rats, mice, rabbits
	 * To derive representative values, she removed outliers that exceeded the IQR.
	 * Maybe we should remove outliers similar to what Grace did.
	 *
	 * -Leora 4/23/20  */
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	
		ParseToxVal p=new ParseToxVal();
//		p.createFiles();
		
		String folder="C:\\Users\\Leora\\Desktop\\Tele\\ToxVal";
		
		String CAS="79-06-1"; //acrylamide
		
		//  Want to add CAS 79-01-6 trichloroethylene as another chemical with a lot of data.  -Leora
				
		String filePathDatabaseAsText=folder+File.separator+"toxval_pod_summary_with_references_2020-01-16.txt";
		String filePathRecordsForCAS=folder+File.separator+"toxval_pod_summary_"+CAS+".txt";
		String filePathRecordsForCAS_json=folder+File.separator+"toxval_pod_summary_"+CAS+".json";
		
//		p.getRecordsForCAS(CAS,filePathDatabaseAsText, filePathRecordsForCAS);		

		p.goThroughRecords(filePathRecordsForCAS,filePathRecordsForCAS_json);
		
		
	}

}
