package gov.epa.ghs_data_gathering.Parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.ParseChemidplusHTML.ChemidplusRecord;
import gov.epa.ghs_data_gathering.Parse.ParseChemidplusHTML.ToxicityRecord;
import gov.epa.ghs_data_gathering.Utilities.FileUtilities;

public class ParseChemidplus extends Parse {

	Hashtable<String, Double> htDensity = new Hashtable<String, Double>(); // density look up table, densities in g/ml

	public ParseChemidplus() {
		sourceName = ScoreRecord.sourceChemIDplus;
//		folderNameWebpages="All Webpages";
		fileNameHtmlZip="All Webpages.zip";
		init();
		this.loadDensityData();
	}

	/**
	 * Loads density values and stores in a hashtable
	 */
	private void loadDensityData() {

		ArrayList<String> lines = gov.epa.ghs_data_gathering.Utilities.Utilities
				.readFileToArray("AA Dashboard\\Data\\Chemidplus\\density.txt");

		for (int i = 1; i < lines.size(); i++) {// first line is header
			// System.out.println(lines.get(i));
			String[] vals = lines.get(i).split("\t");

			String CAS = vals[0];
			String strDensity = vals[1];

			htDensity.put(CAS, new Double(strDensity));
		}

		// System.out.println(htDensity.get("7487-28-7"));

	}

	class UniqueValues {

		ArrayList<String> uniqueMeasurements = new ArrayList<>();
		ArrayList<String> uniqueRoute = new ArrayList<>();
		ArrayList<String> needDensity = new ArrayList<>();
		ArrayList<String> needMolecularWeight = new ArrayList<>();
	}

	class UniqueSpecies {
		HashMap<String, Integer> oralLD50 = new HashMap<>();
		HashMap<String, Integer> inhalationLC50 = new HashMap<>();
		HashMap<String, Integer> dermalLD50 = new HashMap<>();

	}

	@SuppressWarnings("unused")
	private void lookatcasnumbers() {

		try {

			BufferedReader br = new BufferedReader(new FileReader("AA Dashboard\\Data\\Chemidplus\\CurrentChemID.xml"));

			int count = 0;

			while (true) {
				String Line = br.readLine();

				if (Line == null)
					break;

				if (Line.indexOf("CASRegistryNumber") > -1) {
					// System.out.println(count+"\t"+Line);
					count++;
				}

				if (count % 100 == 0) {
					System.out.println(count);
				}

			}

			System.out.println(count);
			br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Override
	protected void createRecords() {
//		ArrayList<ChemidplusRecord> cr = p.parseHTML(mainFolder + "/"+folderNameWebpages);
		ParseChemidplusHTML p=new ParseChemidplusHTML();
		
		Vector<ChemidplusRecord> records = p.parseHTML_Files_in_Zip(mainFolder + "/"+fileNameHtmlZip);
		writeOriginalRecordsToFile(records);
	}
	


//	@Override
	protected Chemicals goThroughOriginalRecords() {
		Chemicals chemicals=new Chemicals();
		try {

			Gson gson = new Gson();
			ChemidplusRecord[] records = gson.fromJson(new FileReader(mainFolder + "\\" + this.fileNameJSON_Records),
					ChemidplusRecord[].class);
			
			System.out.println("Records loaded from original records file");

			// storing unique routes and measurements
			UniqueValues uv = new UniqueValues();
			// storing species count for each route
			UniqueSpecies un = new UniqueSpecies();
			
//			String f="AA Dashboard/Data/Chemidplus";
//			String caslistpath=f+"/need molecular weight.txt";
//			ArrayList<String>casList=ToxPredictor.Utilities.Utilities.readFileToArray(caslistpath);

			for (int i = 0; i < records.length; i++) {

				// System.out.println(i);
				 if (i % 100 == 0) System.out.println(i);
				ChemidplusRecord chemidplusRecord = records[i];
				// System.out.println(chemidplusRecord.CASRegistryNumber);

//				if(!casList.contains(chemidplusRecord.CASRegistryNumber)) continue;//just run the ones that need molecular weight
//				if (!chemidplusRecord.CASRegistryNumber.equals("79-01-6"))continue;

				Chemical chemical=createChemical(chemidplusRecord, uv, un);
				handleMultipleCAS(chemicals, chemical);
				
			}
			
			printSortedSpeciesCountsToFile(un);
			// if (true) return;
			printValues(uv);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	private void printValues(UniqueValues uv) {
		 System.out.println();
		 System.out.println("Unique Measurements: ");
		 for (String s : uv.uniqueMeasurements) {
		 System.out.println(s);
		 }
		
		 System.out.println();
		 System.out.println("Unique Routes: ");
		
		 // for (int i=0;i<uv.uniqueRoute.size();i++) {//alternative way to loop through
		 // string array (Leora)
		 for (String s : uv.uniqueRoute) {
		 System.out.println(s);
		 }
		
		 System.out.println();

		if (uv.needDensity.size()>0) {
			System.out.println("Need density to convert from ml to mg/kg:");
			for (String s : uv.needDensity) {
				System.out.println(s);
			}
			System.out.println();
		}
		
		
		if (uv.needMolecularWeight.size()>0) {
		 System.out.println("Need molecular weight to convert from ppm to mg/L:");
		 for (String s : uv.needMolecularWeight) {
			 System.out.println(s);
		 }
		}
	}

	/**
	 * 
	 * @param chemical
	 * @param tr
	 * @param uv
	 * @param un
	 * 
	 * 
	 * 
	 */
	private void createAcuteMammalianToxicityOralRecord(Chemical chemical, ToxicityRecord tr, UniqueValues uv,
			UniqueSpecies un) {
		// System.out.println("Creating AcuteMammalianToxicityOralRecord");

		ScoreRecord sr = new ScoreRecord();
		sr = new ScoreRecord();
		sr.source = ScoreRecord.sourceChemIDplus;

		// sr.category = "";
		// sr.classification = "";
		// sr.hazard_statement = "";
		// sr.hazard_code = "";

		sr.route = "Oral";

		/*
		 * 
		 * EPA Series 870 - Health Effects Test Guidelines
		 * https://www.epa.gov/test-guidelines-pesticides-and-toxic-substances/series-
		 * 870-health-effects-test-guidelines
		 * 
		 * EPA Health Effects Test Guidelines OPPTS 870.1100 Acute Oral Toxicity:
		 * "The preferred rodent species is the rat although other rodent species may be used."
		 * 
		 * 
		 * EPA Health Effects Test Guidelines OPPTS 870.1200 Acute Dermal Toxicity: "The
		 * rat, rabbit, or guinea pig may be used. The albino rabbit is preferred
		 * because of its size, ease of handling, skin permeability, and extensive data
		 * base. Commonly used laboratory strains should be employed. If a species other
		 * than rats, rabbits, or guinea pigs is used, the tester should provide
		 * justification and reasoning for its selection.
		 * 
		 * 
		 * EPA Health Effects Test Guidelines OPPTS 870.1300 Acute Inhalation Toxicity:
		 * "Although several mammalian test species may be used, the preferred species
		 * is the rat. Commonly used laboratory strains should be employed. If another
		 * mammalian species is used, the tester should provide justification and
		 * reasoning for its selection."
		 * 
		 */
		ArrayList<String> okSpecies = new ArrayList<String>();
		okSpecies.add("mouse");// 27796
		okSpecies.add("rat");// 13124
		okSpecies.add("rabbit");// 1089
		okSpecies.add("guinea pig");// 970
		// okSpecies.add("hamster");//110
		// okSpecies.add("squirrel");//1
		// okSpecies.add("gerbil");//11

		// Contemplating which makes more sense: including all rodents or including only
		// rats.

		// Should we look individually at the studies of "man", "human" and "child"?

		// Species tested:
		// mouse: 27796
		// rat: 13124
		// rabbit: 1089
		// guinea pig: 970
		// mammal (species unspecified): 774
		// dog: 746
		// chicken: 382
		// quail: 370
		// bird - wild: 366
		// duck: 206
		// cat: 186
		// hamster: 110
		// monkey: 93
		// pigeon: 74
		// bird - domestic: 49
		// domestic animals - goat/sheep: 45
		// pig: 26
		// frog: 14
		// gerbil: 11
		// cattle: 9
		// turkey: 9
		// horse/donkey: 6
		// human: 5
		// child: 2
		// squirrel: 1
		// man: 1

		if (un.oralLD50.get(tr.Organism) == null) {
			un.oralLD50.put(tr.Organism, new Integer(1));
		} else {
			int oldCount = un.oralLD50.get(tr.Organism);
			int newCount = oldCount + 1;
			un.oralLD50.put(tr.Organism, newCount);
		}

		if (!okSpecies.contains(tr.Organism))
			return;

		// System.out.println(chemical.CAS+"\t"+tr.ReportedDose+"\t"+tr.NormalizedDose);
		boolean ok = parseAndConvertUniqueMeasurements(chemical, uv, sr, tr);
		if (!ok)
			return;
		setOralScore(sr, chemical);

		sr.note=this.createNote(tr);
		chemical.scoreAcute_Mammalian_ToxicityOral.records.add(sr);

	}

	private void createAcuteMammalianToxicityDermalRecord(Chemical chemical, ToxicityRecord tr, UniqueValues uv,
			UniqueSpecies un) {
		// System.out.println("Creating AcuteMammalianToxicityDermalRecord");

		ScoreRecord sr = new ScoreRecord();
		sr = new ScoreRecord();
		sr.source = ScoreRecord.sourceChemIDplus;

		// sr.category = "";
		// sr.classification = "";
		// sr.hazard_statement = "";
		// sr.hazard_code = "";

		sr.route = "Dermal";

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

		// Species tested:
		// Dermal LD50
		// rabbit: 2539
		// rat: 994
		// guinea pig: 262
		// mouse: 242
		// mammal (species unspecified): 46
		// duck: 15
		// dog: 14
		// bird - wild: 12
		// cat: 11
		// domestic animals - goat/sheep: 5
		// monkey: 2
		// hamster: 2
		// pig: 2
		// quail: 2
		// human: 2
		// chicken: 1
		// turkey: 1

		if (un.dermalLD50.get(tr.Organism) == null) {
			un.dermalLD50.put(tr.Organism, new Integer(1));
		} else {
			int oldCount = un.dermalLD50.get(tr.Organism);
			int newCount = oldCount + 1;
			un.dermalLD50.put(tr.Organism, newCount);
		}

		if (!okSpecies.contains(tr.Organism))
			return;

		boolean ok = parseAndConvertUniqueMeasurements(chemical, uv, sr, tr);
		if (!ok)
			return;

		setDermalScore(sr, chemical);

		sr.note=this.createNote(tr);
		chemical.scoreAcute_Mammalian_ToxicityDermal.records.add(sr);

	}

	private void createAcuteMamalianToxicityInhalationRecord(Chemical chemical, ToxicityRecord tr, UniqueValues uv,
			UniqueSpecies un) {
		// System.out.println("Creating AcuteMammalianToxicityInhalationRecord");

		ScoreRecord sr = new ScoreRecord();
		sr = new ScoreRecord();
		sr.source = ScoreRecord.sourceChemIDplus;
		// sr.category = "";
		// sr.classification = "";
		// sr.hazard_statement = "";
		// sr.hazard_code = "";

		sr.route = "Inhalation";

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

		// Other Mammals:
		// guinea pig
		// monkey
		// hamster
		// mammal (species unspecified)
		// mouse
		// cat
		// rabbit
		// domestic animals - goat/sheep
		// dog

		// Species tested:
		// rat: 1226
		// mouse: 651
		// mammal (species unspecified): 129
		// guinea pig: 82
		// rabbit: 49
		// dog: 30
		// cat: 27
		// monkey: 20
		// hamster: 11
		// domestic animals - goat/sheep: 4
		// quail: 2
		// duck: 2
		// pigeon: 1
		// bird - wild: 1
		// bird - domestic: 1

		if (tr.TestType.equals("LC50")) {
			if (un.inhalationLC50.get(tr.Organism) == null) {
				un.inhalationLC50.put(tr.Organism, new Integer(1));
			} else {
				int oldCount = un.inhalationLC50.get(tr.Organism);
				int newCount = oldCount + 1;
				un.inhalationLC50.put(tr.Organism, newCount);
			}
		}

		if (!okSpecies.contains(tr.Organism))
			return;

		boolean ok = parseAndConvertUniqueMeasurements(chemical, uv, sr, tr);
		if (!ok)
			return;


		setInhalationScore(sr, chemical);

		sr.note=this.createNote(tr);
		
		chemical.scoreAcute_Mammalian_ToxicityInhalation.records.add(sr);

	}

	private String createNote(ToxicityRecord tr) {
//		Organism 	Test Type 	Route 	Reported Dose (Normalized Dose) 	Effect 	Source
		String note="Test organism: " + tr.Organism+"<br>\r\n";
		note+="Reported Dose: "+tr.ReportedDose+"<br>\r\n";
		note+="Normalized Dose: "+tr.NormalizedDose+"<br>\r\n";
		
		if (tr.Effect==null || tr.Effect.equals("")) {
			tr.Effect="N/A";
		}
		note+="Source: "+tr.Source;
		return note; 
		
	}
	
	
	/*  If the duration was not four hours, this is noted in the reported dose, not the normalized dose.
	For example, reported dose 1320mg/m3/2H and normalized dose 1320mg/m3.
	
	New Zealand converted this to a four hour dose:
	"1.32 mg/l 2 hr converted to 4 hr as follows: 1.32 * 2/4 = 0.66 mg/l 4hr"
	
	GHS Rev 7 page 115: "Inhalation cut-off values in the table are based on 4 hour testing exposures.
	Conversion of existing inhalation toxicity data which has been generated according to 1 hour exposures
	should be by dividing by a factor of 2 for gases and vapours and 4 for dusts and mists."
	
	So we probably should convert to 4 hour exposures...  need to figure out code to do that... -Leora
	*/
	
	
	
	
	/**
	 * Converts value and units from normalized dose. Assigns dose and operator. If
	 * can't convert the units, it returns false.
	 * 
	 * If TestType is LD50, the final units should be mg/kg. If TestType is LC50 the
	 * final units should be mg/L.
	 * 
	 * @param chemical
	 * @param uv
	 * @param sr
	 * @param tr
	 * @return
	 */
	private boolean parseAndConvertUniqueMeasurements(Chemical chemical, UniqueValues uv, ScoreRecord sr,
			ToxicityRecord tr) {
		// Example bad records:
		// 12321-44-7 LD50 units/kg => unknown units
		// 103451-84-9 units iu/kg => unknown units
		// 31282-04-9 LD50 units/kg => unknown units
		// 108-42-9 LD50 ppm => concentration units
		// 103112-35-2 LD50 mg/m3 => concentration units
		// 143-18-0 LC50 mg/kg ==> dose units

		sr.valueMassOperator = "";
		// >=, \u2265
		// <=, \u2264
		if (tr.ReportedDose.contains("<") || tr.ReportedDose.contains(">") || tr.ReportedDose.contains("\u2264")
				|| tr.ReportedDose.contains("\u2265")) {
			sr.valueMassOperator = tr.ReportedDose.substring(0, 1);
		}

		if (tr.NormalizedDose.matches(".*[a-z].*")) {
			// finding first alphabetic
			Pattern p = Pattern.compile("\\p{Alpha}");
			Matcher m = p.matcher(tr.NormalizedDose);

			if (m.find()) {
				sr.valueMass = Double.parseDouble(tr.NormalizedDose.substring(0, m.start()));
				sr.valueMassUnits = tr.NormalizedDose.substring(m.start(), tr.NormalizedDose.length());
			}
		} else {
			sr.valueMass = Double.parseDouble(tr.NormalizedDose);
			sr.valueMassUnits = "?";
			System.out.println(chemical.CAS + "\tunknown units for normalized dose");
			return false;
		}
		
		
		
		
//		System.out.println(tr.TestType+"\t"+sr.valueMass+"\t"+sr.valueMassUnits);
		
	

		if (!uv.uniqueMeasurements.contains(sr.valueMassUnits)) {
			uv.uniqueMeasurements.add(sr.valueMassUnits);
		}

		if (tr.TestType.equals("LC50")) {
			
//			System.out.println(tr.TestType+"\t"+sr.valueMass+"\t"+sr.valueMassUnits);
			
			if (tr.ReportedDose.contains("H")) {
				
				String strHour=tr.ReportedDose.substring(tr.ReportedDose.length()-2,tr.ReportedDose.length()-1);
				double hour=Double.parseDouble(strHour);
				sr.valueMass*=hour/4.0;
//				System.out.println(hour);
				
			}
			

			if (sr.valueMassUnits.equals("mg/m3")) {
				// 1 mg/L= 1000 mg/m3
				// converting mg/L to mg/m3
				sr.valueMass /= 1000.0;
				sr.valueMassUnits = "mg/L";

			} else if (sr.valueMassUnits.equals("ppm")) {
				// To convert concentrations in air (at 25 °C) from ppm to mg/m3:
				// mg/m3 = (ppm) × (molecular weight of the compound)/(24.45).
				// 1 milligram per cubic meter ( mg/m3 ) = 0.0010 milligrams per liter ( mg/l ).
				// So mg/L = ((ppm) × (molecular weight of the compound)/(24.45))*0.001

					
				if (chemical.molecularWeight==0) {
					if (!uv.needMolecularWeight.contains(chemical.CAS)) {
						uv.needMolecularWeight.add(chemical.CAS);
						System.out.println(chemical.CAS+"\tneed MW");
						return false;
					}
				}
				
				sr.valueMass = (sr.valueMass * (chemical.molecularWeight) / (24.45)) * .001;
				sr.valueMassUnits = "mg/L";

//				System.out.println(chemical.CAS+"\t"+sr.valueMass);

			} else if (sr.valueMassUnits.equals("mL/m3")) {
				if (htDensity.get(chemical.CAS) == null) {
					if (!uv.needDensity.contains(chemical.CAS)) {
						uv.needDensity.add(chemical.CAS);
					}
					return false;

				} else {
					double density = htDensity.get(chemical.CAS);
					sr.valueMass *= density; // (ml/m3)*(density g/ml)*(1000 mg/g)*(1 m3/1000 L)
					// System.out.println(dose+" mg/kg (converted from mL/m3)");
					sr.valueMassUnits = "mg/L";
				}
			} else if (sr.valueMassUnits.equals("mg/L")) {
				// we are ok
			} else {
				// System.out.println(chemical.CAS + "\t" + tr.TestType + "\t" +
				// sr.valueMassUnits);
				return false;
			}
		} else if (tr.TestType.equals("LD50")) {

			if (sr.valueMassUnits.equals("mL/kg")) {
				// Need density to convert from mL to mg. Need to print a list of CAS numbers
				// that we need density for.

				if (htDensity.get(chemical.CAS) == null) {
					if (!uv.needDensity.contains(chemical.CAS)) {
						uv.needDensity.add(chemical.CAS);
					}
					return false;

				} else {
					double density = htDensity.get(chemical.CAS);
					sr.valueMass *= density * 1000.0;
					sr.valueMassUnits = "mg/kg";
				}
			} else if (sr.valueMassUnits.equals("iu/kg")) {
				// need concentration to convert iu to mg
				// System.out.println(chemical.CAS + "\tunits iu/kg");
				return false;
			} else if (sr.valueMassUnits.equals("mg/kg")) {
				// we are ok
			} else {
				// System.out.println(chemical.CAS + "\t" + tr.TestType + "\t" +
				// sr.valueMassUnits);
				return false;
			}

		} else {
			// System.out.println(chemical.CAS + "\t" + tr.TestType + "\t" +
			// sr.valueMassUnits);
			return false;
		}

		return true;

	}

	// print list of all possible units to view to determine what needs to be
	// converted.

	// for oral and dermal we want to convert units to mg/kg

	// for inhalation we want to convert units to mg/L

	// need list of CAS numbers for ones that need density to convert units

	// print out CAS numbers if mL/L or mL/kg because need to convert to mg/L or
	// mg/kg using density

	// need to include info on species

	// need to combine results for each species into one score

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

		} else if (sr.valueMassOperator.equals("")) {
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
		}
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

		} else if (sr.valueMassOperator.equals("")) {
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
			System.out.println("other operator: " + sr.valueMassOperator);
		}

	}

	// Can we distinguish vapor/gas from dust/mist/fume or should we just use the
	// criteria for vapor/gas?

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

		} else if (sr.valueMassOperator.equals("")) {
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
				System.out.println(chemical.CAS + "\toral\t" + dose);
			}
		}
	}

	
	private void printSortedSpeciesCountsToFile(UniqueSpecies us) {

		
		try {

			FileWriter fw = new FileWriter(new File(mainFolder+"/Species List.txt"));

			// creating comparator to sort the map values which will then be passed to
			// Collections.sort
			Comparator<Map.Entry<String, Integer>> byMapValues = new Comparator<Map.Entry<String, Integer>>() {
				@Override
				public int compare(Map.Entry<String, Integer> left, Map.Entry<String, Integer> right) {
					return left.getValue().compareTo(right.getValue());
				}
			};

			writeSortedEntries(fw, byMapValues,us.oralLD50,"Oral LD50");
			writeSortedEntries(fw, byMapValues,us.dermalLD50,"Dermal LD50");
			writeSortedEntries(fw, byMapValues,us.inhalationLC50,"Inhalation LC50");

			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void writeSortedEntries(FileWriter fw, Comparator<Map.Entry<String, Integer>> byMapValues,HashMap<String,Integer>hm,String name)
			throws IOException {
		// create a list of map entries
		List<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>();

		// add all entries
		entries.addAll(hm.entrySet());

		// sort the collection
		Collections.sort(entries, byMapValues.reversed());

		Iterator i = entries.iterator();

		fw.write(name+"\r\n");
		while (i.hasNext()) {
			Map.Entry me = (Map.Entry) i.next();
			fw.write(me.getKey() + ":\t");
			fw.write(me.getValue() + "\r\n");
		}
		fw.write("\r\n");
	}

	private Chemical createChemical(ChemidplusRecord chemidplusRecord, UniqueValues uv, UniqueSpecies un) {

		Chemical chemical = new Chemical();

		chemical.CAS = chemidplusRecord.CASRegistryNumber;
		chemical.name = chemidplusRecord.NameOfSubstance;
		
		if (chemidplusRecord.Molecular_Weight!=null)
			chemical.molecularWeight=Double.parseDouble(chemidplusRecord.Molecular_Weight);
		
		chemical.molecularFormula=chemidplusRecord.Molecular_Formula;

		// loop through ToxicityRecords

		for (int i = 0; i < chemidplusRecord.ToxicityRecords.size(); i++) {

			ToxicityRecord tr = chemidplusRecord.ToxicityRecords.get(i);

			// rat LD50 oral ==> AcuteMammalianToxicityOral

			// System.out.println(tr.Organism + "\t" + tr.TestType + "\t" + tr.Route + "\t"
			// + tr.ReportedDose + "\t"
			// + tr.NormalizedDose + "\t" + tr.Effect + "\t" + tr.Source);

			if (!uv.uniqueRoute.contains(tr.Route)) {
				uv.uniqueRoute.add(tr.Route);
			}

			if (tr.NormalizedDose.equals("")) {
				System.out.println(
						chemidplusRecord.CASRegistryNumber + "\t" + tr.ReportedDose + "\tnormalized dose is blank");
				return null;
			}

			if (tr.TestType.equals("LD50") && tr.Route.equals("oral")) {
				// if (tr.Organism.equals("rat") || tr.Organism.equals("mouse")) {
				this.createAcuteMammalianToxicityOralRecord(chemical, tr, uv, un);
				// }
			} else if (tr.TestType.equals("LD50") && (tr.Route.equals("Dermal") || tr.Route.equals("skin"))) {
				// if (tr.Organism.equals("rat") || tr.Organism.equals("mouse")) {
				this.createAcuteMammalianToxicityDermalRecord(chemical, tr, uv, un);
				// }
			} else if (tr.TestType.equals("LC50") && tr.Route.equals("inhalation")) {
				// if (tr.Organism.equals("rat") || tr.Organism.equals("mouse")) {
//				System.out.println(tr.NormalizedDose+"\t"+tr.ReportedDose);
				
				this.createAcuteMamalianToxicityInhalationRecord(chemical, tr, uv, un);

				// }
			}

		}

		// Not including if endpoint is LCLo

//		 chemical.writeToFile(jsonFolder);
		 
		 return chemical;

	}
	
	void createZipFileFromWebpages() {
		String f="AA Dashboard/Data/Chemidplus";
		String folderHtml =f+"/All Webpages";
		String destFilePath=f+"/All Webpages.zip";
		String destFilePathTxt=f+"/All Webpages.txt";
		
//		FileUtilities.createZipFileFromFolder(folderHtml, destFilePath);
		FileUtilities.createTextFileFromFolder(folderHtml, destFilePathTxt);
	}

	public static void main(String[] args) {
		ParseChemidplus pc = new ParseChemidplus();

		pc.createFiles();
		
//		pc.createZipFileFromWebpages();
		// pc.lookatcasnumbers();

	}

}
