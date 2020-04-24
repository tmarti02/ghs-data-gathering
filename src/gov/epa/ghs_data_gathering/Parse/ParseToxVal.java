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
					createCancerScore(chemical,r);
				} else if (r.risk_assessment_class.contentEquals("developmental")) {
					createDevelopmentalScore(chemical,r);
					
					/* Added the rest of the rac here but need to add methods.  -Leora 4/24/20 */
					
				} else if (r.risk_assessment_class.contentEquals("mortality:acute")) {
					createEcoToxAcuteScore(chemical,r);	
				} else if (r.risk_assessment_class.contentEquals("mortality:chronic")) {
					createEcoToxChronicScore(chemical,r);
					
					/* Need to double check that these are all eco.  -Leora 4/23/20 */
					
				} else if (r.risk_assessment_class.contentEquals("chronic")) {
					createChronicScore(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("subchronic")) {
					createSubchronicScore(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("short-term")) {
					createShorttermScore(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("repeat dose")) {
					createRepeatDoseScore(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("subacute")) {
					createSubacuteScore(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("growth:acute")) {
					createGrowthAcuteScore(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("growth:chronic")) {
					createGrowthChronicScore(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("reproductive")) {
					createReproductiveScore(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("neurotoxicity")) {
					createNeurotoxicityScore(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("developmental neurotoxicity")) {
					createDevelopmentalNeurotoxicityScore(chemical,r);
					
				} else if (r.risk_assessment_class.contentEquals("ecotoxicity invertebrate")) {
					createEcotoxInvertebrateScore(chemical,r);
					
					
					
				} else {
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
	
	
	private void createCancerScore(Chemical chemical, RecordToxVal r) {

		if (r.toxval_type.contentEquals("NOAEL")) {
			//TODO
		} else if (r.toxval_type.contentEquals("cancer unit risk")) {
			//TODO
		
		} else {//need code for all important toxval_types
			
			/* NOAEL and cancer unit risk are the only toxval_types for acrylamide. -Leora 4/23/20
			
			/* Richard seemed to be suggesting that we treat all toxval_types the same and just use
			If(min value < 1) high
			Else if(min value < 10) medium
			Else low
			I'm just making a note of this for now, will come back to it.  -Leora 4/23/20
 
*/
			
			
		}

		
	}

	private void createDevelopmentalScore(Chemical chemical, RecordToxVal r) {
		// TODO Auto-generated method stub
		
	}

	
	private void createEcoToxAcuteScore(Chemical chemical, RecordToxVal r) {
		
		// Need to add method.  -Leora 4/24/20
	}
	
	
	private void createEcoToxChronicScore(Chemical chemical, RecordToxVal r) {
		
		// Need to add method.  -Leora 4/24/20
		
	}
	
	private void createChronicScore(Chemical chemical, RecordToxVal r) {
		
		// Need to add method.  -Leora 4/24/20
		
	}
	
	private void createSubchronicScore(Chemical chemical, RecordToxVal r) {
		
		// Need to add method.  -Leora 4/24/20
		
	}

	private void createShorttermScore(Chemical chemical, RecordToxVal r) {
		
		// Need to add method.  -Leora 4/24/20
		
	}

	private void createRepeatDoseScore(Chemical chemical, RecordToxVal r) {
		
		// Need to add method.  -Leora 4/24/20
		
	}
	
	private void createSubacuteScore(Chemical chemical, RecordToxVal r) {
		
		// Need to add method.  -Leora 4/24/20
		
	}
	
	private void createGrowthAcuteScore(Chemical chemical, RecordToxVal r) {
		
		// Need to add method.  -Leora 4/24/20
		
	}
	
	private void createGrowthChronicScore(Chemical chemical, RecordToxVal r) {
		
		// Need to add method.  -Leora 4/24/20
		
	}
	
	private void createReproductiveScore(Chemical chemical, RecordToxVal r) {
		
		// Need to add method.  -Leora 4/24/20
		
	}
	
	private void createNeurotoxicityScore(Chemical chemical, RecordToxVal r) {
		
		// Need to add method.  -Leora 4/24/20
		
	}
	
	private void createDevelopmentalNeurotoxicityScore(Chemical chemical, RecordToxVal r) {
		
		// Need to add method.  -Leora 4/24/20
		
	}
	
	private void createEcotoxInvertebrateScore(Chemical chemical, RecordToxVal r) {
		
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
		
		/* Okay, I understand the code.  This basically renames what Richard called toxval_numeric
		 * into valueMass and then valueMass is renamed score and then for acute toxicity,
		 * so then for AcuteMammalianToxicity, the same code that we used for the
		 * AA Dashboard/CHA Database is directly used.  -Leora 4/23/20  */
		


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
		
//		System.out.println(chemical.CAS+"\t"+strDose);
		
//		System.out.println("****"+strDose);
		
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
				/* Should "\toral\t" in the line above be "\tinhalation\t"?
				 * Is this a mistake that is also in our ChemIDplus code?
				 * -Leora 4/23/20 */
				
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
	 * But integrating the scores is one of the main things that I've been contemplating for the ToxVal data.
	 * As we discussed, it might make sense to use the priority_id field and take the minimum score from each of
	 * the seven priority_id categories and then priority_id 1>2>3>4>5>6>7 in the trumping method.
	 * Also, instead of, or in combination with, the trumping scheme, we could remove extreme outliers and
	 * then take the minimum of the remaining scores.
	 * Since the values are continuous instead of ordinal, removing outliers makes sense. 
	 * 
	 * In Grace's Science Webinar presentation on 4/22/20, she talked about using ToxVal data to develop TTC.
	 * She filtered from ToxVal:
	 * toxval type: NO(A)EL or NO(A)EC
	 * species: rats, mice, rabbits
	 * To derive representative values, she removed outliers that exceeded the IQR.
	 *
	 * -Leora 4/23/20  */
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	
		ParseToxVal p=new ParseToxVal();
//		p.createFiles();
		
		String folder="C:\\Users\\Leora\\Desktop\\Tele\\ToxVal";
		
		String CAS="79-06-1";
				
		String filePathDatabaseAsText=folder+File.separator+"toxval_pod_summary_with_references_2020-01-16.txt";
		String filePathRecordsForCAS=folder+File.separator+"toxval_pod_summary_"+CAS+".txt";
		String filePathRecordsForCAS_json=folder+File.separator+"toxval_pod_summary_"+CAS+".json";
		
//		p.getRecordsForCAS(CAS,filePathDatabaseAsText, filePathRecordsForCAS);		

		p.goThroughRecords(filePathRecordsForCAS,filePathRecordsForCAS_json);
		
		
	}

}
