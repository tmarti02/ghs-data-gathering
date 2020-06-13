package gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

import gov.epa.ghs_data_gathering.API.Chemical;
import gov.epa.ghs_data_gathering.API.Chemicals;
import gov.epa.ghs_data_gathering.API.Score;
import gov.epa.ghs_data_gathering.API.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.Parse;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ParseToxVal extends Parse {

	void getRecordsForCAS(String CAS, String filePathDatabaseAsText, String filePathRecordsForCAS) {

		try {

			BufferedReader br = new BufferedReader(new FileReader(filePathDatabaseAsText));

			FileWriter fw = new FileWriter(filePathRecordsForCAS);

			String header = br.readLine();

			fw.write(header + "\r\n");

			int colCAS = Utilities.FindFieldNumber(header, "casrn", "\t");
			System.out.println(colCAS);

			while (true) {

				String Line = br.readLine();

				//				System.out.println(Line);

				if (Line == null)
					break;

				String[] vals = Line.split("\t");

				String currentCAS = vals[colCAS];

				if (currentCAS.contentEquals(CAS)) {
					System.out.println(Line);
					fw.write(Line + "\r\n");
				}

			}
			br.close();
			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	void goThroughRecords(String filepath, String destFilepathJSON, String destFilepathTXT) {

		try {

			BufferedReader br = new BufferedReader(new FileReader(filepath));
			String header = br.readLine();

			Chemical chemical = new Chemical();

			while (true) {

				String Line = br.readLine();

				//				System.out.println(Line);

				if (Line == null)
					break;

				RecordToxVal r = RecordToxVal.createRecord(header, Line);

				Score score = null;

				createScoreRecord(chemical, r);

			}

			chemical.writeChemicalToJsonFile(destFilepathJSON);
			chemical.toFlatFile(destFilepathTXT, "\t");

			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	void goThroughRecordsMultipleChemicals(String filepathText, String destfilepathJson, String destfilepathText,
			Vector<String> casList) {

		try {

			//			FileInputStream inputStream = new FileInputStream(new File(filepathXLSX));
			//			Workbook workbook = new XSSFWorkbook(inputStream);
			//			Sheet firstSheet = workbook.getSheetAt(0);
			//			Row rowHeader=firstSheet.getRow(0);
			//			for (int i=0;i<rowHeader.getLastCellNum();i++) {
			//				System.out.println(rowHeader.getCell(i));
			//			}

			BufferedReader br = new BufferedReader(new FileReader(filepathText));

			String header = br.readLine();

			String[] hlist = header.split("\t");

			Chemicals chemicals = new Chemicals();

			Chemical chemical = new Chemical();

			String oldCAS = "";

			while (true) {

				String Line = br.readLine();
				//				System.out.println(Line);

				if (Line == null)
					break;

				RecordToxVal r = RecordToxVal.createRecord(header, Line);

				if (!casList.contains(r.casrn))
					continue;

				if (!r.casrn.contentEquals(oldCAS)) {
					chemical = new Chemical();
					chemical.CAS = r.casrn;
					chemical.name = r.name;
					chemicals.add(chemical);
					oldCAS = r.casrn;
				}

				createScoreRecord(chemical, r);
				//				System.out.println(Line);

			}

			chemicals.writeToFile(destfilepathJson);
			chemicals.toFlatFile(destfilepathText, "\t");
			//			writeChemicalToFile(chemical, destfilepath);

			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static void createScoreRecord(Chemical chemical, RecordToxVal r) {



		if (chemical.CAS == null) {
			chemical.CAS = r.casrn;
			chemical.name = r.name;
		}

		if (r.human_eco.contentEquals("human health")) {			
			if (r.risk_assessment_class.contentEquals("acute")) {

				/*
				 * Added: && r.human_eco.contentEquals("human health") because there is at least
				 * one eco entry labeled "acute" I seem to remember it should be && instead of &
				 * but need to check this. -Leora 4/23/20
				 * 
				 * 
				 * I'm not quite sure whether things the inclusion criteria such as for
				 * human_eco should go here or whether they should be located in the code for
				 * the class.
				 * 
				 */

				CreateAcuteMammalianToxicityRecords.createAcuteMammalianToxicityRecords(chemical, r);	

			} else if (r.risk_assessment_class.contentEquals("repeat dose")
					|| r.risk_assessment_class.contentEquals("short-term")
					|| r.risk_assessment_class.contentEquals("subacute")
					|| r.risk_assessment_class.contentEquals("subchronic")
					|| r.risk_assessment_class.contentEquals("chronic")) {
				CreateOrganOrSystemicToxRecords.createDurationRecord(chemical, r);

			} else if (r.risk_assessment_class.contentEquals("cancer")) {

				CreateCancerRecords.createCancerRecords(chemical, r);

			} else if (r.risk_assessment_class.contentEquals("developmental")
					|| r.risk_assessment_class.contentEquals("developmental neurotoxicity")
					|| r.risk_assessment_class.contentEquals("reproductive")){
				CreateReproductiveDevelopmentalToxicityRecords.createReproductiveDevelopmentalRecords(chemical, r);

			} else if (r.risk_assessment_class.contentEquals("neurotoxicity")) {
				createNeurotoxicityRecords(chemical, r);
			}



		} else if (r.human_eco.contentEquals("eco") &&
				r.habitat.contentEquals("aquatic") && r.toxval_units.contentEquals("mg/L")) {


			if(r.toxval_type.contentEquals("LC50") || r.toxval_type.contentEquals("EC50")){

				if (r.risk_assessment_class.contentEquals("acute")
						|| r.risk_assessment_class.contentEquals("mortality:acute")
						|| r.risk_assessment_class.contentEquals("growth:acute")
						|| r.risk_assessment_class.contentEquals("reproduction:acute")
						|| r.risk_assessment_class.contentEquals("ecotoxicity invertebrate")
						|| r.risk_assessment_class.contentEquals("ecotoxicity plants")) {
					CreateAquaticToxicityRecords.createAquaticToxAcuteRecords(chemical, r);
				}


			} else if(r.toxval_type.contentEquals("NOEC") || r.toxval_type.contentEquals("LOEC")){

				if (r.risk_assessment_class.contentEquals("chronic")
						|| r.risk_assessment_class.contentEquals("mortality:chronic")
						|| r.risk_assessment_class.contentEquals("growth:chronic")
						|| r.risk_assessment_class.contentEquals("reproduction:chronic")
						|| r.risk_assessment_class.contentEquals("ecotoxicity invertebrate")
						|| r.risk_assessment_class.contentEquals("ecotoxicity plants")) {
					CreateAquaticToxicityRecords.createAquaticToxChronicRecords(chemical, r);

				}
			}

			/* Probably should rename as Acute or Chronic AquaticToxicity instead of Ecotoxicity.

		} else if (r.risk_assessment_class.contentEquals("ecotoxicity invertebrate")) {
			CreateEcotoxicityRecords.createEcotoxInvertebrateRecords(chemical, r);

		} else if (r.risk_assessment_class.contentEquals("subchronic")) {
			 createSubchronicRecords(chemical,r);

		} else if (r.risk_assessment_class.contentEquals("short-term")) {
			 createShorttermRecords(chemical,r);

		} else if (r.risk_assessment_class.contentEquals("subacute")) {
			 createSubacuteRecords(chemical,r);
			 */


			//		} else if (r.risk_assessment_class.contentEquals("acute")) {
			//			//TODO
			//
			//		} else if (r.risk_assessment_class.contentEquals("chronic")) {
			//			//TODO
			//			
			//		} else if (r.risk_assessment_class.contentEquals("mortality:acute")) {
			//			//TODO
			//			
			//		} else if (r.risk_assessment_class.contentEquals("mortality:chronic")) {
			//			//TODO

		} else {
			//			System.out.println("unknown rac="+r.risk_assessment_class);
		}

	}




	/*
	 * } else if
	 * (r.risk_assessment_class.contentEquals("developmental neurotoxicity")) {
	 * createDevelopmentalNeurotoxicityRecords(chemical, r);
	 */

	/*
	 * There does not appear to be a rac for genetox. However, there is a separate
	 * file called toxval_genetox_summary_2020-01-16 that I downloaded from the ftp
	 * site I want to do something like: If the chemical is in the
	 * toxval_genetox_summary_2020-01-16 file then createGenetoxScore(chemical,r);
	 * 
	 * -Leora
	 */

	// } else {
	// TODO add methods for other risk assessment classes
	/*
	 * System.out.println("unknown rac="+r.risk_assessment_class);
	 * 
	 * rac to add -Leora 4/23/20:
	 * 
	 * mortality:acute mortality:chronic
	 * 
	 * chronic (human health: species include human and rat; eco: different aquatic
	 * species) subchronic (human health: rat) short-term repeat dose subacute
	 * 
	 * growth:acute growth:chronic
	 *
	 * reproductive
	 * 
	 * neurotoxicity developmental neurotoxicity
	 * 
	 * ecotoxicity invertebrate
	 * 
	 * 
	 * 
	 * Excellent! That’s what you are supposed to get because code hasn’t been
	 * added to handle these risk assessment classes. If you look at the code for
	 * the GoThroughRecords method in the ParseToxVal class, you will see a block of
	 * code like this: if (r.risk_assessment_class.contentEquals("acute")) {........
	 * For right now you can comment out the line above that prints unknown rac so
	 * that it doesn’t clutter the output. So far I have only handled a few of the
	 * hazard categories (and may not be complete yet).
	 * 
	 * Look at the code for the first 3 I added and make sure I did it the same way
	 * as Richard. I may have restricted the records more than he did- for example
	 * for acute oral I only used “toxval_type”= “LD50” and the 4 species
	 * that we used earlier in the ParseChemIdplus class.
	 **** 
	 * [I think the point is to look at what Richard did and suggest changes if we
	 * don't agree. -Leora 4/23/20]****
	 * 
	 * If you didn’t remember, pressing F3 will jump to a different method when
	 * the cursor is on it. Alt + left arrow will go back to where you were. Todd
	 * 
	 * 
	 */


	/*
	 * There does not appear to be a rac for genetox. However, there is a separate
	 * file called toxval_genetox_summary_2020-01-16 that I downloaded from the ftp
	 * site I want to do something like: If the chemical is in the
	 * toxval_genetox_summary_2020-01-16 file then createGenetoxScore(chemical,r);
	 * 
	 * Need to import the toxval_genetox_summary_2020-01-16 Excel file. Then:
	 * private void createGenetoxRecords(Chemical chemical, RecordToxVal r) { if
	 * genetox_call = "clastogen" OR "gentox" OR "pred clastogen" OR "pred gentox"
	 * then score= VH [the vertical line key to indicate "OR" is not working on my
	 * keyboard] [there are no genetox_call data that would indicate H or M] if
	 * genetox_call = "non gentox" OR "pred non gentox" then score= L if
	 * genetox_call = "inconclusive" OR "not clastogen" then score= N/A -Leora
	 */

	static String createNote(RecordToxVal tr) {
		// Organism Test Type Route Reported Dose (Normalized Dose) Effect Source
		String note = "Test organism: " + tr.species_common + "<br>\r\n";
		note += "Reported Dose: " + tr.toxval_numeric_original+" "+tr.toxval_units_original + "<br>\r\n";
		note += "Normalized Dose: " + tr.toxval_numeric +" "+tr.toxval_units+"<br>\r\n";

		// if (tr.Effect==null || tr.Effect.equals("")) {
		// tr.Effect="N/A";
		// }
		note += "Source: " + tr.source;
		return note;

	}

	static String formatDose(double dose) {
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

	/*
	 * Combining all of these into OrganOrSystemicToxRecords
	 * 
	 * private void createChronicRecords(Chemical chemical, RecordToxVal r) {
	 * }
	 * 
	 * private void createSubchronicRecords(Chemical chemical, RecordToxVal r) {
	 * 
	 * // study_duration_value and study_duration_units can be used to determine the
	 * actual duration for studies called subchronic, short term, or repeat dose.
	 * Then DfE criteria for repeated dose toxicity (28, 40-50, or 90 days) can be
	 * used.
	 * }
	 * 
	 * private void createShorttermRecords(Chemical chemical, RecordToxVal r) {
	 * 
	 * private void createSubacuteRecords(Chemical chemical, RecordToxVal r) {
	 * 
	 * }
	 */



	/*
	 * private void createGrowthAcuteRecords(Chemical chemical, RecordToxVal r) {
	 * 
	 * }
	 * 
	 * private void createGrowthChronicRecords(Chemical chemical, RecordToxVal r) {
	 * 
	 * }
	 */

	private void createReproductiveRecords(Chemical chemical, RecordToxVal r) {

		/*
		 * Reproductive will have the same code as Developmental (same DfE criteria),
		 * which is detailed above. I'll add the code for Reproductive when I'm sure
		 * Developmental is correct. -Leora
		 */

	}

	private static void createNeurotoxicityRecords(Chemical chemical, RecordToxVal r) {

		// study_duration_value and study_duration_units
		// DfE criteria

	}

	private void createDevelopmentalNeurotoxicityRecords(Chemical chemical, RecordToxVal r) {

		/*
		 * DevelopmentalNeurotoxicity will have the same code as Developmental (same DfE
		 * criteria), which is detailed above. -Leora
		 */

	}

	/*
	 * So this class is just for creating the individual scores. Do we also want to
	 * integrate into one score for each chemical? Or is it actually best to not
	 * even do that so that we are not assigning a "final" score? But integrating
	 * the scores is one of the things that I've been contemplating for the ToxVal
	 * data. As we discussed, it might make sense to use the priority_id field and
	 * take the minimum score from each of the seven priority_id categories and then
	 * priority_id 1>2>3>4>5>6>7 in the trumping method.
	 * 
	 * Also, instead of, or in combination with, the trumping scheme, we could
	 * remove extreme outliers and then take the minimum of the remaining scores.
	 * Since the values are continuous instead of ordinal, removing outliers makes
	 * sense.
	 * 
	 * In Grace's Science Webinar presentation on 4/22/20, she talked about using
	 * ToxVal data to develop TTC. She filtered from ToxVal: toxval type: NO(A)EL or
	 * NO(A)EC species: rats, mice, rabbits To derive representative values, she
	 * removed outliers that exceeded the IQR. Maybe we should remove outliers
	 * similar to what Grace did.
	 *
	 * -Leora 4/23/20
	 */

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ParseToxVal p = new ParseToxVal();
		//		p.createFiles();

		String folder = "C:\\Users\\Leora\\Desktop\\Tele\\ToxVal";
		// String folder="E:\\Documents\\0000 epa\\0 telework\\AA dashboard";

		// String CAS = "79-06-1"; // acrylamide
		String CAS = "123-91-1"; // 1,4-Dioxane

		String filePathDatabaseAsText = folder + File.separator + "toxval_pod_summary_with_references_2020-01-16.txt";

		String filePathRecordsForCAS = folder + File.separator + "toxval_pod_summary_" + CAS + ".txt";

		String filePathRecordsForCAS_json = folder + File.separator + "records_" + CAS + ".json";
		String filePathRecordsForCAS_txt = folder + File.separator + "records_" + CAS + ".txt";

		//		p.getRecordsForCAS(CAS,filePathDatabaseAsText, filePathRecordsForCAS);		

		p.goThroughRecords(filePathRecordsForCAS, filePathRecordsForCAS_json, filePathRecordsForCAS_txt);

		//		Vector<String>vecCAS=new Vector<>();
		//		vecCAS.add("79-06-1");
		//		vecCAS.add("79-01-6"); 
		//		vecCAS.add("108-95-2"); 
		//		vecCAS.add("50-00-0"); 
		//		vecCAS.add("111-30-8");
		//		vecCAS.add("302-01-2"); 
		//		vecCAS.add("75-21-8"); 
		//		vecCAS.add("7803-57-8"); 
		//		vecCAS.add("101-77-9"); 
		//		vecCAS.add("10588-01-9"); 
		//		vecCAS.add("107-13-1"); 
		//		vecCAS.add("110-91-8"); 
		//		vecCAS.add("106-93-4"); 
		//		vecCAS.add("67-56-1"); 
		//		vecCAS.add("7664-39-3"); 
		//		vecCAS.add("556-52-5"); 
		//		vecCAS.add("87-86-5"); 
		//		vecCAS.add("62-53-3"); 
		//		vecCAS.add("106-89-8"); 
		//		vecCAS.add("7778-50-9");
		//				
		//		String filePathRecordsForCASList_json=folder+File.separator+"toxval_pod_summary_top 20.json";		
		//		String filePathRecordsForCASList_txt=folder+File.separator+"toxval_pod_summary_Top20.txt";
		//
		//		p.goThroughRecordsMultipleChemicals(filePathDatabaseAsText,filePathRecordsForCASList_json,filePathRecordsForCASList_txt,vecCAS);

	}

}
