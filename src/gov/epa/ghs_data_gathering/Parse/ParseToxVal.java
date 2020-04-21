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
				
				if (r.risk_assessment_class.contentEquals("acute")) {
					createAcuteMammalianToxicityRecords(chemical, r);
				} else if (r.risk_assessment_class.contentEquals("cancer")) {
					createCancerScore(chemical,r);
				} else if (r.risk_assessment_class.contentEquals("developmental")) {
					createDevelopmentalScore(chemical,r);				
				} else {
					//TODO add methods for other risk assessment classes
					System.out.println("unknown rac="+r.risk_assessment_class);
					
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
			
		}

		
	}

	private void createDevelopmentalScore(Chemical chemical, RecordToxVal r) {
		// TODO Auto-generated method stub
		
	}

	private void createAcuteMammalianToxicityInhalationRecord(Chemical chemical, RecordToxVal tr) {
		// System.out.println("Creating AcuteMammalianToxicityInhalationRecord");

		ScoreRecord sr = new ScoreRecord();
		sr = new ScoreRecord();
		sr.source = ScoreRecord.sourceToxVal;
		sr.sourceOriginal=tr.source;

		sr.route = "Inhalation";
		
		//TODO - do we only want to use LC50? I know richard might not restrict to LC50s
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


		if (!okSpecies.contains(tr.species_common))//TODO- does richard use all species???
			return;


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
				System.out.println(chemical.CAS + "\toral\t" + dose);
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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	
		ParseToxVal p=new ParseToxVal();
//		p.createFiles();
		
		String folder="E:\\Documents\\0000 epa\\0 telework\\AA dashboard";
		
		String CAS="79-06-1";
				
		String filePathDatabaseAsText=folder+File.separator+"toxval_pod_summary_with_references_2020-01-16.txt";
		String filePathRecordsForCAS=folder+File.separator+"toxval_pod_summary_"+CAS+".txt";
		String filePathRecordsForCAS_json=folder+File.separator+"toxval_pod_summary_"+CAS+".json";
		
//		p.getRecordsForCAS(CAS,filePathDatabaseAsText, filePathRecordsForCAS);		

		p.goThroughRecords(filePathRecordsForCAS,filePathRecordsForCAS_json);
		
		
	}

}
