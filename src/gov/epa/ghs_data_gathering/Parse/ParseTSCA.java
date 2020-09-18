package gov.epa.ghs_data_gathering.Parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.poi.ss.usermodel.DataFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.AADashboard;
import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

/**
 * Parse TSCA work plan
 * 
 * TODO- this class isnt done- need to create records from hazard, persistence,
 * and bioacc
 * 
 * @author Todd Martin
 *
 */
public class ParseTSCA extends Parse {

	String strDefaultScore = ScoreRecord.scoreH;

	public ParseTSCA() {
		sourceName = ScoreRecord.sourceTSCA_Work_Plan;
//		String fileNameSourceExcel = "Parsing_tsca_work_plan_chemicals_2014.xls";
		fileNameSourceText = "tsca_work_plan_chemicals_2014.txt";
		init();
	}
	
	static class TSCA_Record {

		String ChemicalName;
		String When_Was_The_Chemical_Added;
		String Hazard_Criteria_Met;
		String Hazard_Score;
		String Exposure_Criteria_Met;
		String Exposure_Score;
		String Persistence_And_Bioaccumulation_Criteria_Met;
		String Persistence_And_Bioaccumulation_Score;
		String Use;
		String Risk_Assessment_Status_And_Other_Actions;
		String CASRN;
	}

	protected void createRecords() {
		Vector<TSCA_Record> records = parseTextFile(mainFolder + "/" + fileNameSourceText);
		writeOriginalRecordsToFile(records);
	}

	private Vector<TSCA_Record> parseTextFile(String filepath) {

		try {

			Vector<TSCA_Record> data_field = new Vector<>();

			BufferedReader br = new BufferedReader(new FileReader(filepath));

			String Line = br.readLine();

			while (true) {

				if (Line == null)
					break;

				LinkedList<String> list = Utilities.Parse(Line, "\t");
				TSCA_Record tr = this.createDataField(list);

				// System.out.println(tr.Exposure_Criteria_Met);
				// System.out.println(list.get(11));

				while (true) {
					Line = br.readLine();

					// System.out.println(Line);

					if (Line == null)
						break;

					// System.out.println(Line);
					list = Utilities.Parse3(Line, "\t");

					// System.out.println(list.get(4));

					if (!list.get(0).equals("")) {
						break;
					}
					this.updateDataField(tr, list);

				}
				data_field.add(tr);
				// if (true) break;

			}
			br.close();
			return data_field;

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	private static TSCA_Record createDataField(LinkedList<String> list) {
		TSCA_Record tr = new TSCA_Record();
		DataFormatter formatter = new DataFormatter();

		tr.ChemicalName = list.get(1);
		tr.When_Was_The_Chemical_Added = list.get(2);
		tr.Hazard_Criteria_Met = list.get(3);
		tr.Hazard_Score = list.get(4);
		tr.Exposure_Criteria_Met = list.get(5);
		tr.Exposure_Score = list.get(6);
		tr.Persistence_And_Bioaccumulation_Criteria_Met = list.get(7);
		tr.Persistence_And_Bioaccumulation_Score = list.get(8);
		tr.Use = list.get(9);
		tr.Risk_Assessment_Status_And_Other_Actions = list.get(10);
		tr.CASRN = list.get(11);

		return tr;
	}

	private void updateDataField(TSCA_Record tr, LinkedList<String> list) {

		if (!list.get(1).equals(""))
			tr.ChemicalName += " " + list.get(1);
		if (!list.get(2).equals(""))
			tr.When_Was_The_Chemical_Added += " " + list.get(2);
		if (!list.get(3).equals(""))
			tr.Hazard_Criteria_Met += " " + list.get(3);
		if (!list.get(4).equals(""))
			tr.Hazard_Score += " " + list.get(4);
		if (!list.get(5).equals(""))
			tr.Exposure_Criteria_Met += " " + list.get(5);
		if (!list.get(6).equals(""))
			tr.Exposure_Score += " " + list.get(6);
		if (!list.get(7).equals(""))
			tr.Persistence_And_Bioaccumulation_Criteria_Met += " " + list.get(7);
		if (!list.get(8).equals(""))
			tr.Persistence_And_Bioaccumulation_Score += " " + list.get(8);
		if (!list.get(9).equals(""))
			tr.Use += " " + list.get(9);
		if (!list.get(10).equals(""))
			tr.Risk_Assessment_Status_And_Other_Actions += " " + list.get(10);
		// if (!list.get(11).equals(""));
		// tr.CASRN += " " + list.get(11);

	}

	public static void main(String[] args) {

		ParseTSCA pt = new ParseTSCA();
		pt.createFiles();

	}

	
	@Override
	protected Chemicals goThroughOriginalRecords() {
		Chemicals chemicals=new Chemicals();
		
		try {
			Gson gson = new Gson();
			File jsonFile = new File(mainFolder + "/" + fileNameJSON_Records);
			TSCA_Record[] records = gson.fromJson(new FileReader(jsonFile), TSCA_Record[].class);

			for (int i = 0; i < records.length; i++) {
				// for (int i = 0; i < 100; i++) {

				TSCA_Record tr = records[i];

				Chemical chemical = this.createChemical(tr);
				this.handleMultipleCAS(chemicals, chemical);


			} // end loop over records

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	private Chemical createChemical(TSCA_Record tr) {
		Chemical chemical = new Chemical();

		chemical.CAS = tr.CASRN;
		chemical.name = tr.ChemicalName;

		if (chemical.CAS.equals("Category"))
			chemical.CAS = "";

		String t = tr.Hazard_Criteria_Met.replace("toxcity", "toxicity");
		t = processAcuteChronicToxicity(t, chemical);// Done- but not sure if need to include systemic toxicity for
														// chronic tox ones
		t = processCarcinogenicity(t, chemical);// Done!
		t = processEffects(t, chemical);// not done
		t = processAquaticToxicity(t, chemical);// Done!
		t = processDevReproTox(t, chemical);// Done!
		t = processRest(t, chemical);// Done!
		t = t.trim();

		if (!t.equals(""))
			System.out.println(chemical.CAS + "\t" + t);

		String p = tr.Persistence_And_Bioaccumulation_Criteria_Met;
		processPersistenceBioaccumulation(p, chemical);// Done!

		// String Exposure_Criteria_Met;//TODO

		return chemical;

	}

	private void processPersistenceBioaccumulation(String p, Chemical chemical) {

		Score score=chemical.scorePersistence;
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		score.records.add(sr);
		sr.source = ScoreRecord.sourceTSCA_Work_Plan;

		if (p.indexOf("Low environmental persistence") > -1) {
			sr.score = ScoreRecord.scoreL;
			sr.category = "Low environmental persistence";
		} else if (p.indexOf("Moderate environmental persistence") > -1) {
			sr.score = ScoreRecord.scoreM;
			sr.category = "Moderate environmental persistence";
		} else if (p.indexOf("High environmental persistence") > -1) {
			sr.score = ScoreRecord.scoreH;
			sr.category = "High environmental persistence";
		} else {
			System.out.println(p);
		}
		sr.rationale = "Score of " + sr.score + " was assigned based on a category of " + sr.category;

		// **********************************************************************************************************
		
		score=chemical.scoreBioaccumulation;		
		sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);		
		score.records.add(sr);		
		sr.source = ScoreRecord.sourceTSCA_Work_Plan;

		if (p.indexOf("Low bioaccumulation potential") > -1) {
			sr.score = ScoreRecord.scoreL;
			sr.category = "Low bioaccumulation potential";
		} else if (p.indexOf("Moderate bioaccumulation potential") > -1) {
			sr.score = ScoreRecord.scoreM;
			sr.category = "Moderate bioaccumulation potential";
		} else if (p.indexOf("High bioaccumulation potential") > -1) {
			sr.score = ScoreRecord.scoreH;
			sr.category = "High bioaccumulation potential";
		} else {
			System.out.println(p);
		}
		sr.rationale = "Score of " + sr.score + " was assigned based on a category of " + sr.category;

	}

	private String processRest(String t, Chemical chemical) {
		if (t.indexOf("Mutagenicity") > -1) {
			t = t.replace("Mutagenicity", "");
			createScoreRecord(chemical.scoreGenotoxicity_Mutagenicity, chemical,"Mutagenicity", ScoreRecord.scoreVH);
		}

		if (t.indexOf("Neurotoxicity") > -1) {
			t = t.replace("Neurotoxicity", "");
			createScoreRecord(chemical.scoreNeurotoxicity_Repeat_Exposure, chemical,"Neurotoxicity", strDefaultScore);
		}

		if (t.indexOf("Respiratory") > -1) {
			t = t.replace("Respiratory", "");
			// createScoreRecord(chemical.scoreRespiratory,"Respiratory",strDefaultScore);
			// //TODO
		}
		return t;
	}

	private String processEffects(String t, Chemical chemical) {

		// TODO- is central nervous system effects same as neurotoxicity?

		if (t.indexOf("Chronic cardiovascular, renal and musculoskeletal effects") > -1) {
			// TODO
			t = t.replace("Chronic cardiovascular, renal and musculoskeletal effects", "");
		}

		if (t.indexOf("Cardiovascular and central nervous system effects") > -1) {
			// TODO
			t = t.replace("Cardiovascular and central nervous system effects", "");
		}

		if (t.indexOf("Central nervous system effects") > -1) {
			// TODO
			t = t.replace("Central nervous system effects", "");
		}
		return t;
	}

	private void createScoreRecord(Score score, Chemical chemical,String category, String strScore) {
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		score.records.add(sr);
		sr.source = ScoreRecord.sourceTSCA_Work_Plan;
		sr.score = strScore;
		sr.category = category;
		sr.rationale = "Score of " + sr.score + " was assigned based on a category of " + sr.category;

	}

	private String processDevReproTox(String t, Chemical chemical) {

		if (t.indexOf("Developmental and reproductive toxicity") > -1) {
			// TODO
			t = t.replace("Developmental and reproductive toxicity", "");

			createScoreRecord(chemical.scoreDevelopmental, chemical,"Developmental toxicity", strDefaultScore);
			createScoreRecord(chemical.scoreReproductive, chemical,"Reproductive toxicity", strDefaultScore);

		}
		if (t.indexOf("Reproductive toxicity") > -1) {
			// TODO
			t = t.replace("Reproductive toxicity", "");
			createScoreRecord(chemical.scoreReproductive, chemical,"Reproductive toxicity", strDefaultScore);

		}

		if (t.indexOf("Developmental toxicity") > -1) {
			// TODO
			t = t.replace("Developmental toxicity", "");
			createScoreRecord(chemical.scoreDevelopmental, chemical, "Developmental toxicity", strDefaultScore);
		}
		return t;
	}

	private String processAquaticToxicity(String t, Chemical chemical) {
		if (t.toLowerCase().indexOf("aquatic toxicity") == -1)
			return t;

		// System.out.println(chemical.CAS+"\t"+t);

		if (t.indexOf("Acute and chronic aquatic toxicity") > -1) {
			t = t.replace("Acute and chronic aquatic toxicity", "");
			createScoreRecord(chemical.scoreAcute_Aquatic_Toxicity, chemical,"Acute aquatic toxicity", strDefaultScore);
			createScoreRecord(chemical.scoreChronic_Aquatic_Toxicity, chemical,"Chronic aquatic toxicity", strDefaultScore);
		}

		if (t.indexOf("Chronic aquatic toxicity") > -1) {
			t = t.replace("Chronic aquatic toxicity", "");
			createScoreRecord(chemical.scoreChronic_Aquatic_Toxicity, chemical,"Chronic aquatic toxicity", strDefaultScore);
		}

		if (t.indexOf("Acute aquatic toxicity") > -1) {// only 2 compounds
			t = t.replace("Acute aquatic toxicity", "");
			createScoreRecord(chemical.scoreAcute_Aquatic_Toxicity, chemical,"Acute aquatic toxicity", strDefaultScore);
		}

		if (t.indexOf("Aquatic toxicity") > -1) {
			// Can't use since dont know which- is it both? or just uknown?
			t = t.replace("Aquatic toxicity", "");
		}

		return t;

	}

	private String processCarcinogenicity(String t, Chemical chemical) {

		if (t.indexOf("carcinogen") == -1)
			return t;

		Score score=chemical.scoreCarcinogenicity;
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		chemical.scoreCarcinogenicity.records.add(sr);
		sr.source = ScoreRecord.sourceTSCA_Work_Plan;

		if (t.indexOf("Known human carcinogens") > -1 || t.indexOf("Known human carcinogen") > -1) {
			t = t.replace("Known human carcinogens", "");
			t = t.replace("Known human carcinogen", "");
			sr.score = "VH";
			sr.category = "Known human carcinogen";
		}

		if (t.indexOf("Probable human carcinogens") > -1 || t.indexOf("Probable human carcinogen") > -1) {
			t = t.replace("Probable human carcinogens", "");
			t = t.replace("Probable human carcinogen", "");
			sr.score = "VH";
			sr.category = "Probable human carcinogen";

		}

		if (t.indexOf("Possible human carcinogen") > -1) {
			t = t.replace("Possible human carcinogen", "");
			sr.score = "H";
			sr.category = "Possible human carcinogen";
		}

		if (t.indexOf("Potential carcinogenicity to specific target organs") > -1) {
			t = t.replace("Potential carcinogenicity to specific target organs", "");
			sr.score = "H";
			sr.category = "Potential carcinogenicity to specific target organs";
		}

		if (t.indexOf("Limited evidence of carcinogenicity") > -1) {
			t = t.replace("Limited evidence of carcinogenicity", "");
			sr.score = "M";
			sr.category = "Limited evidence of carcinogenicity";
		}

		sr.rationale = "Score of " + sr.score + " was assigned based on a category of " + sr.category;

		return t;
	}

	private String processAcuteChronicToxicity(String t, Chemical chemical) {
		if (t.indexOf("Acute and chronic toxicity from inhalation exposures") > -1) {
			// TODO- add to Systemic_Toxicity_Repeat_Exposure?
			t = t.replace("Acute and chronic toxicity from inhalation exposures", "");
			createScoreRecord(chemical.scoreAcute_Mammalian_ToxicityInhalation,chemical,
					"Acute and chronic toxicity from inhalation exposures", strDefaultScore);
		}

		if (t.indexOf("Acute toxicity from inhalation exposures") > -1) {
			t = t.replace("Acute toxicity from inhalation exposures", "");
			createScoreRecord(chemical.scoreAcute_Mammalian_ToxicityInhalation,chemical,
					"Acute and chronic toxicity from inhalation exposures", strDefaultScore);
		}

		if (t.indexOf("Chronic toxicity to target organs including the liver, kidneys and thyroid") > -1) {
			t = t.replace("Chronic toxicity to target organs including the liver, kidneys and thyroid", "");
			createScoreRecord(chemical.scoreSystemic_Toxicity_Repeat_Exposure,chemical,
					"Chronic toxicity to target organs including the liver, kidneys and thyroid", strDefaultScore);
		}

		if (t.indexOf("Chronic toxicity and liver effects") > -1) {
			t = t.replace("Chronic toxicity and liver effects", "");
			createScoreRecord(chemical.scoreSystemic_Toxicity_Repeat_Exposure,chemical,
					"Chronic toxicity to target organs including the liver, kidneys and thyroid", strDefaultScore);
		}

		if (t.indexOf("Acute and chronic toxicity") > -1) {
			// TODO- add to Systemic_Toxicity_Repeat_Exposure?
			t = t.replace("Acute and chronic toxicity", "");
			createScoreRecord(chemical.scoreAcute_Mammalian_ToxicityOral, chemical,"Acute mammalian toxicity", strDefaultScore);
		}

		if (t.indexOf("Chronic toxicity") > -1) {
			// TODO- add to Systemic_Toxicity_Repeat_Exposure?
			t = t.replace("Chronic toxicity", "");
		}

		if (t.indexOf("Acute mammalian toxicity") > -1) {// only 1 compound
			t = t.replace("Acute mammalian toxicity", "");
			createScoreRecord(chemical.scoreAcute_Mammalian_ToxicityOral,chemical, "Acute mammalian toxicity", strDefaultScore);
		}

		if (t.indexOf("Acute toxicity") > -1) {// assume same as one above
			t = t.replace("Acute toxicity", "");
			createScoreRecord(chemical.scoreAcute_Mammalian_ToxicityOral, chemical,"Acute mammalian toxicity", strDefaultScore);
		}

		return t;
	}

}
