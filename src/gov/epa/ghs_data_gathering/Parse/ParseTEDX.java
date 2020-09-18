package gov.epa.ghs_data_gathering.Parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.AADashboard;
import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.ParseUMD.UMDRecord;

public class ParseTEDX extends Parse {

	String[] identifiers = { "ENDOCRINE DISRUPTOR REFERENCES", "USE REFERENCES", "CATEGORIES OF USE", "DATE ADDED:",
			"ALTERNATIVE NAME:" };

	
	public ParseTEDX() {
		sourceName = ScoreRecord.sourceTEDX;
		fileNameSourceText = "tedx.txt";
		init();
	}
	class TEDX_Records {
		public Vector<String> CAS = new Vector<String>();
		public String name;
		public String alternativeName;

		public Vector<String> endocrineDisruptorReferences = new Vector<String>();

		public String categoriesOfUse;
		public Vector<String> useReferences = new Vector<String>();

		public String dateAdded;
		public String dateUpdated;

		public String toJSONString() {
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting().serializeNulls();
			Gson gson = builder.create();
			return gson.toJson(this);
		}
	}

	/**
	 * Go through the lines vector and retrieve the records starting at the
	 * fieldName and stopping when hit another known fieldName
	 * 
	 * @param fieldName
	 * @param lines
	 * @return
	 */
	private Vector<String> getData(String fieldName, Vector<String> lines) {

		int start = -1;
		int stop = -1;

		for (int i = 0; i < lines.size(); i++) {

			if (lines.get(i).indexOf(fieldName) > -1) {
				start = i + 1;
				break;
			}
		}

		if (start == -1)
			return null;

		for (int i = start; i < lines.size(); i++) {

			String linei = lines.get(i);

			for (int j = 0; j < identifiers.length; j++) {

				if (!identifiers[j].equals(fieldName) && linei.indexOf(identifiers[j]) > -1) {
					stop = i;
					break;
				}
			}
			if (stop != -1)
				break;
		}

		// System.out.println(start+"\t"+stop);

		Vector<String> data = new Vector<String>();

		for (int i = start; i < stop; i++) {
			// System.out.println(i+"\t"+lines.get(i));
			data.add(lines.get(i));
		}
		return data;
	}

	/**
	 * Go through lines and return the line that has the fieldName fieldName is
	 * removed from the line first
	 * 
	 * @param fieldName
	 * @param lines
	 * @return
	 */
	private String getData2(String fieldName, Vector<String> lines) {

		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).indexOf(fieldName) > -1) {
				return lines.get(i).replace(fieldName, "").trim();
			}
		}
		return null;
	}

	/**
	 * Parse text file that just has casname mashed together from copy paste from
	 * website
	 */
	private void parseTEDX_casname_text_file() {
		String filepath = "AA Dashboard\\Data\\TEDX\\casname.txt";

		try {

			BufferedReader br = new BufferedReader(new FileReader(filepath));
			File file = new File(filepath);

			// System.out.println(file.exists());

			Vector<String> bad = new Vector<String>();

			while (true) {

				String LineOriginal = br.readLine();

				String Line = LineOriginal;

				if (Line == null)
					break;

				if (Line.indexOf("-") == -1) {
					// System.out.println("No dashes:\t"+LineOriginal);
					bad.add("No dashes:\t" + LineOriginal);
					continue;
				}

				String p1 = Line.substring(0, Line.indexOf("-"));

				Line = Line.substring(Line.indexOf("-") + 1, Line.length());

				if (Line.indexOf("-") == -1) {
					// System.out.println("No second dash:\t"+LineOriginal);
					bad.add("No second dash:\t" + LineOriginal);
					continue;
				}

				String p2 = Line.substring(0, Line.indexOf("-"));

				if (p2.length() != 2) {
					// System.out.println("Length of second part <> 2 :\t"+LineOriginal);
					bad.add("Length of second part <> 2 :\t" + LineOriginal);
					continue;
				}

				Line = Line.substring(Line.indexOf("-") + 1, Line.length());

				String p3 = Line.substring(0, 1);

				Line = Line.substring(1, Line.length());

				// System.out.println(p1+"-"+p2+"-"+p3+"\t"+Line);

				if (Line.indexOf("/") > -1) {

					String CAS1 = p1 + "-" + p2 + "-" + p3;
					Vector<String> casNumbers = new Vector<String>();
					casNumbers.add(CAS1);

					// System.out.println(LineOriginal);
					// System.out.println(Line);

					while (Line.indexOf("/") > -1) {
						Line = Line.substring(Line.indexOf("/") + 1, Line.length());

						if (Line.indexOf("/") > -1) {
							String CAS = Line.substring(0, Line.indexOf("/")).trim();
							casNumbers.add(CAS);
						} else {

							p1 = Line.substring(0, Line.indexOf("-"));
							Line = Line.substring(Line.indexOf("-") + 1, Line.length());
							p2 = Line.substring(0, Line.indexOf("-"));
							Line = Line.substring(Line.indexOf("-") + 1, Line.length());
							p3 = Line.substring(0, 1);
							Line = Line.substring(1, Line.length());

							String CAS = p1 + "-" + p2 + "-" + p3;
							CAS = CAS.replace("see also", "").trim();
							casNumbers.add(CAS);
						}

						// System.out.println(Line);
					}
					// System.out.println("");

					for (int i = 0; i < casNumbers.size(); i++) {
						System.out.println(casNumbers.get(i) + "\t" + Line);
					}
					// System.out.println("");

				} else {
					System.out.println(p1 + "-" + p2 + "-" + p3 + "\t" + Line);
				}

				// System.out.println(p2);

			}

			Collections.sort(bad);

			for (int i = 0; i < bad.size(); i++) {
				System.out.println(bad.get(i));
			}

			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private TEDX_Records parseLines(Vector<String> lines) {

		TEDX_Records tr = new TEDX_Records();

		// for (int i=0;i<lines.size();i++) {
		// System.out.println(i+"\t"+lines.get(i));
		// }

		String nameCAS = lines.remove(0);

		this.parseCASNameMashup(nameCAS, tr);
		
		tr.name = tr.name.replace(" ,", ",").replace(", ", ",").replace("( + ) ", "(+)").replace("( ", "(")
				.replace(" ( ", "(").replace(") ", ")").replace(" )", ")");

//		if (tr.CAS.size() > 0) {
//			for (int i = 0; i < tr.CAS.size(); i++) {
//				System.out.println(tr.CAS.get(i));
//			}
//		}

		String alternativeName = getData2("ALTERNATIVE NAME:", lines);
		if (alternativeName != null)
			tr.alternativeName = alternativeName;

		String categoriesOfUse = getData2("CATEGORIES OF USE:", lines);
		if (categoriesOfUse != null)
			tr.categoriesOfUse = categoriesOfUse;

		String dateAdded = getData2("DATE ADDED:", lines);
		if (dateAdded.indexOf("DATE UPDATED") > -1) {
			dateAdded = dateAdded.substring(0, dateAdded.indexOf("DATE UPDATED")).trim();
		}
		tr.dateAdded = dateAdded;

		// System.out.println(dateAdded);

		String dateUpdated = getData2("DATE UPDATED:", lines);
		if (dateUpdated != null && dateUpdated.indexOf("DATE ADDED") > -1) {
			dateUpdated = dateUpdated.substring(dateUpdated.indexOf("   "), dateUpdated.length()).trim();
			// System.out.println(dateUpdated);
		}
		// System.out.println(dateUpdated);
		tr.dateUpdated = dateUpdated;

		tr.endocrineDisruptorReferences = this.getData("ENDOCRINE DISRUPTOR REFERENCES", lines);
		tr.useReferences = this.getData("USE REFERENCES", lines);

		// if (tr.endocrineDisruptorReferences!=null) {
		// for (int i=0;i<tr.endocrineDisruptorReferences.size();i++) {
		// System.out.println(tr.endocrineDisruptorReferences.get(i));
		// }
		// }

		return tr;
	}

	private void parseCASNameMashup(String LineOriginal, TEDX_Records tr) {

		String Line = LineOriginal;

		if (Line.indexOf("-") == -1) {
			// System.out.println("No dashes:\t"+LineOriginal);
			// bad.add("No dashes:\t"+LineOriginal);
			tr.name = LineOriginal;
			return;
		}

		String p1 = Line.substring(0, Line.indexOf("-"));

		Line = Line.substring(Line.indexOf("-") + 1, Line.length());

		if (Line.indexOf("-") == -1) {
			// System.out.println("No second dash:\t"+LineOriginal);
			// bad.add("No second dash:\t"+LineOriginal);
			tr.name = LineOriginal;
			return;
		}

		String p2 = Line.substring(0, Line.indexOf("-"));

		if (p2.length() != 2) {
			// System.out.println("Length of second part <> 2 :\t"+LineOriginal);
			// bad.add("Length of second part <> 2 :\t"+LineOriginal);
			tr.name = LineOriginal;
			return;
		}

		Line = Line.substring(Line.indexOf("-") + 1, Line.length());

		String p3 = Line.substring(0, 1);

		Line = Line.substring(1, Line.length());

		// System.out.println(p1+"-"+p2+"-"+p3+"\t"+Line);

		if (Line.indexOf("/") > -1) {

			String CAS1 = p1 + "-" + p2 + "-" + p3;
			Vector<String> casNumbers = new Vector<String>();
			casNumbers.add(CAS1);

			// System.out.println(LineOriginal);
			// System.out.println(Line);

			while (Line.indexOf("/") > -1) {
				Line = Line.substring(Line.indexOf("/") + 1, Line.length());

				if (Line.indexOf("/") > -1) {
					String CAS = Line.substring(0, Line.indexOf("/")).trim();
					casNumbers.add(CAS);
				} else {

					p1 = Line.substring(0, Line.indexOf("-"));
					Line = Line.substring(Line.indexOf("-") + 1, Line.length());
					p2 = Line.substring(0, Line.indexOf("-"));
					Line = Line.substring(Line.indexOf("-") + 1, Line.length());
					p3 = Line.substring(0, 1);
					Line = Line.substring(1, Line.length());

					String CAS = p1 + "-" + p2 + "-" + p3;
					CAS = CAS.replace("see also", "").trim();
					casNumbers.add(CAS);
				}

				// System.out.println(Line);
			}
			// System.out.println("");

			for (int i = 0; i < casNumbers.size(); i++) {
				tr.CAS.add(casNumbers.get(i));
				tr.name = Line.trim();
				// System.out.println(casNumbers.get(i)+"\t"+Line);
			}
			// System.out.println("");

		} else {
			String CAS = p1 + "-" + p2 + "-" + p3;
			tr.CAS.add(CAS);
			tr.name = Line.trim();
		}

	}

	/**
	 * Parse text file that has all the records with metadata from the TEDX website
	 */
	 
	@Override
	protected void createRecords() {
		Vector<TEDX_Records> records = parseTextFile(mainFolder+File.separator+this.fileNameSourceText);
		writeOriginalRecordsToFile(records);
	}


	private Vector<TEDX_Records> parseTextFile(String filepath)  {

		try {
		
		BufferedReader br = new BufferedReader(new FileReader(filepath));

		Vector<TEDX_Records> records = new Vector<TEDX_Records>();

		while (true) {

			Vector<String> lines = new Vector<String>();
			// seek until hit "DATE " in a line
			while (true) {
				String Line = br.readLine();
				if (Line == null)
					break;

				if (Line.equals(""))
					continue;

				lines.add(Line);

				if (Line.indexOf("DATE ") > -1)
					break;
			}

			if (lines.size() == 0)
				break;

			TEDX_Records tr = this.parseLines(lines);
			records.add(tr);
		}

		br.close();
		return records;
		
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	// public void addTEDXRecords(Chemical chemical) {
	//
	// String CAS=chemical.CAS;
	// File jsonFile=new File(jsonFolder+"/"+CAS+".json");
	//
	// //TODO load from jar file instead later
	//
	// if (!jsonFile.exists()) return;
	// Chemical chemicalTEDX=Chemical.loadFromJSON(jsonFile);
	// chemical.combineRecords(chemical, chemicalTEDX);
	//
	// }

	@Override
	protected Chemicals goThroughOriginalRecords() {
		
		Chemicals chemicals=new Chemicals();
		
		String jsonFilePath = mainFolder + File.separator + fileNameJSON_Records;

		File Folder = new File(jsonFolder);

		if (!Folder.exists())
			Folder.mkdir();

		try {
			Gson gson = new Gson();

			TEDX_Records[] records = gson.fromJson(new FileReader(jsonFilePath), TEDX_Records[].class);

			for (TEDX_Records tr:records) {
				for (String CAS:tr.CAS) {
					Chemical chemical = createChemical(tr, CAS);
					handleMultipleCAS(chemicals, chemical);

				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	private Chemical createChemical(TEDX_Records tr, String CAS) {
		Chemical chemical = new Chemical();

		chemical.CAS = CAS;
		chemical.name = tr.name;

		Score score=chemical.scoreEndocrine_Disruption;
		
		ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
		sr.score = ScoreRecord.scoreH;
		sr.source = ScoreRecord.sourceTEDX;

		sr.rationale = "Chemical appears in TEDX (The Endocrine Disruptors Exchange) List.";

		if (tr.endocrineDisruptorReferences != null) {

			sr.rationale += "<br><br>Endocrine disruptor references:<br><ul>";
			for (int k = 0; k < tr.endocrineDisruptorReferences.size(); k++) {
				sr.rationale += "<li>" + tr.endocrineDisruptorReferences.get(k) + "</li>";
			}
			sr.rationale += "</ul>";
		}
		score.records.add(sr);
		return chemical;
	}

	public static void main(String[] args) {
		ParseTEDX tr = new ParseTEDX();
		// tr.parseTEDX_casname_text_file();
		
		tr.createFiles();

	}

}
