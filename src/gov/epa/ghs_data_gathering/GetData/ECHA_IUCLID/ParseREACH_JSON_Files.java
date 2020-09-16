package gov.epa.ghs_data_gathering.GetData.ECHA_IUCLID;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.epa.api.AADashboard;
import gov.epa.api.ScoreRecord;

//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.JSONArray;

/**
 * Class to parse luechfields echa data
 * 
 * @author Todd Martin
 *
 */

public class ParseREACH_JSON_Files {

	// uses reflection to sort by any field

	public static String sourceName = ScoreRecord.sourceReach_JSON_Files;

	public static String mainFolder = AADashboard.dataFolder + File.separator + sourceName;
	public static String jsonFolder = mainFolder + "/json files";

	public class RecordSorter implements Comparator<skinSensitizationDataRecord> {

		String key = "";

		RecordSorter(String key) {
			this.key = key;
		}

		public int compare(skinSensitizationDataRecord r1, skinSensitizationDataRecord r2) {

			try {
				Field myField = r1.getClass().getField(key);
				String s1 = (String) myField.get(r1);

				Field myField2 = r2.getClass().getField(key);
				String s2 = (String) myField2.get(r2);

				return s1.compareTo(s2);

			} catch (Exception ex) {
				ex.printStackTrace();
				return -1;
			}
		}
	}

	class skinSensitizationDataRecord {
		public String _id = "";
		public String cid = "";
		public String cas = "";
		public String cas_TESTMAT = "";
		public String TESTMAT_DETAILS = "";
		public String ECNumber = "";
		public String ECNumber_TESTMAT = "";
		public String chemical_name = "";
		// public String sdf = "";// MDL structure
		// public String SMILES = "";

		public String type = "";
		public String oecd = "";
		public String category = "";

		public String Purpose_flag = "";
		public String Study_result_type = "";
		public String Study_period = "";
		public String Reliability = "";
		public String Rationale_for_reliability_incl_deficiencies = "";

		public String TYPE_INVIVO_INVITRO = "";
		public String STUDYTYPE = "";

		public String INTERPRET_RS_SUBMITTER = "";

		public String APPL_CL = "";

		public String BinaryScore = "-1";
		public String BinaryScoreJustification = "";
		public String Flag = "";

		// String[] fields = { "ECNumber", "cas", "cas_TESTMAT","chemical_name",
		// "SMILES", "type", "oecd", "category", "Purpose_flag",
		// "Study_result_type","Study_period","Reliability","Rationale_for_reliability_incl_deficiencies"
		// };

		String[] fields = { "ECNumber", "ECNumber_TESTMAT", "cas", "cas_TESTMAT", "TESTMAT_DETAILS", "chemical_name",
				"type", "oecd", "Purpose_flag", "Reliability", "Rationale_for_reliability_incl_deficiencies",
				"TYPE_INVIVO_INVITRO", "STUDYTYPE", "INTERPRET_RS_SUBMITTER", "APPL_CL", "BinaryScore",
				"BinaryScoreJustification", "Flag" };

		String delimiter = "|";

		public String toString() {
			String str = "";

			for (int i = 0; i < fields.length; i++) {
				try {
					Field myField = this.getClass().getField(fields[i]);

					String val = (String) myField.get(this);

					str += val;

					if (i < fields.length - 1)
						str += delimiter;

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			str = str.replace("\r", "");

			return str;
			// return ECNumber+"\t"+cas+"\t"+name+"\t"+SMILES;
		}

		public String getHeader() {
			String str = "";

			for (int i = 0; i < fields.length; i++) {
				str += fields[i];

				if (i < fields.length - 1)
					str += delimiter;
			}
			// System.out.println(str);
			return str;

		}

	}

	class ChemicalDataRecords {

		String _id = "";
		String cid = "";
		String cas = "";
		String ECNumber = "";
		String name = "";
		String sdf = "";// MDL structure
		String SMILES = "";

		String predictedSensitiser = "";
		String classifiedSensitiser = "";

	}

	// TODO write a method that can load his data directly into Java class
	void parseSkinSensitizationData() {

		String folder = "L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\ECHA\\REACH_dossier_data\\Skin Sensitization\\Luechtefeld";
		String filename = "GPMT_compact.json";

		JsonParser parser = new JsonParser();

		String Line = "";

		try {

			Vector<skinSensitizationDataRecord> records = new Vector();

			BufferedReader br = new BufferedReader(new FileReader(folder + "/" + filename));

			// Vector<String> items=new Vector<String>();//items for storing
			// list of unique keys

			// int counter = 0;
			// while (true) {

			// if (true) {

			for (int counter = 1; counter <= 1717; counter++) {
				// for (int counter=1;counter<=10;counter++) {

				Line = br.readLine();

				// if (Line==null) break;

				Line = Line.replace("NaN", "-9999");// to avoid crash since it's
													// looking for a number!

				JsonObject obj = (JsonObject) parser.parse(Line);

				skinSensitizationDataRecord c = new skinSensitizationDataRecord();

				if (counter == 1) {
					System.out.println(c.getHeader());
				}

				Iterator<Entry<String, JsonElement>> iterator = obj.entrySet().iterator();

				while (iterator.hasNext()) {

					Entry<String, JsonElement> nextObject = iterator.next();

					if (nextObject.getKey().equals("ECNumber") || nextObject.getKey().equals("cas")
							|| nextObject.getKey().equals("chemical_name") || nextObject.getKey().equals("type")) {
						String val = nextObject.getValue().getAsString();

						Field myField = c.getClass().getField(nextObject.getKey());
						myField.set(c, val);

						if (nextObject.equals("ECNumber")) {
							if (val.indexOf("-") == -1) {
								System.out.println("***" + val);
							}
						}

					} else if (nextObject.getKey().equals("_id")) {
						JsonObject jo = obj.get("_id").getAsJsonObject();
						c._id = jo.get("$oid").getAsString();
					} else if (nextObject.getKey().equals("cid")) {
						// //TODO get value from array
						// JSONObject jo=(JSONObject)obj.get("cid");
						// c.cid=(String)jo.get("$numberLong");
					} else if (nextObject.getKey().equals("SkinClassification")) {
						// JSONObject
						// jo=(JSONObject)obj.get("SkinClassification");
						// if (jo.get("predictedSensitiser")!=null) {
						// c.predictedSensitiser=((Boolean)jo.get("predictedSensitiser")).toString();
						// }
						// if (jo.get("classifiedSensitiser")!=null) {
						// c.classifiedSensitiser=((Boolean)jo.get("classifiedSensitiser")).toString();
						// }
					} else if (nextObject.getKey().equals("oecd") || nextObject.getKey().equals("category")) {
						JsonArray array = obj.get(nextObject.getKey()).getAsJsonArray();

						if (array.size() == 1) {

							Field myField = c.getClass().getField(nextObject.getKey());
							myField.set(c, array.get(0).getAsString());

						} else if (array.size() > 1) {

							// For now just use the first member since extra vals arent useful
							Field myField = c.getClass().getField(nextObject.getKey());
							myField.set(c, array.get(0).getAsString());

							// String vals="";
							//
							//
							// for (int i=0;i<array.size();i++) {
							// if (vals.equals("")) {
							// vals=(String)array.get(i);
							// } else {
							// vals=vals+"\t"+(String)array.get(i);
							// }
							// }
							// Field myField = c.getClass().getField(key);
							// myField.set(c, vals);
							// System.out.println(vals);

						} else {

						}
						// c.oecd=(String)jo[0];
					} else if (nextObject.equals("ADMIN_DATA")) {

						JsonObject obj2 = obj.get(nextObject.getKey()).getAsJsonObject();

						Iterator<Entry<String, JsonElement>> iterator2 = obj2.entrySet().iterator();

						while (iterator2.hasNext()) {

							String key2 = iterator2.next().getKey();
							// System.out.println("ADMIN_DATA:"+key2);

							Field myField = c.getClass().getField(key2.replace(" ", "_"));
							myField.set(c, obj2.get(key2).getAsString());
						}

					} else if (nextObject.equals("Materials and methods")) {

						JsonObject obj2 = obj.get(nextObject.getKey()).getAsJsonObject();
						Iterator<Entry<String, JsonElement>> iterator2 = obj2.entrySet().iterator();

						c.cas_TESTMAT = "";

						while (iterator2.hasNext()) {

							String key2 = iterator2.next().getKey();

							if (key2.equals("TESTMAT")) {
								JsonArray obj3 = obj2.get(key2).getAsJsonArray();

								for (int i = 0; i < obj3.size(); i++) {
									JsonArray obj4 = obj3.get(i).getAsJsonArray(); // row i

									for (int j = 0; j < obj4.size(); j++) {
										JsonArray obj5 = obj4.get(j).getAsJsonArray();

										if (obj5.get(0).equals("CAS number")) {
											String CAS = obj5.get(2).getAsString();

											if (c.cas_TESTMAT.equals("")) {
												c.cas_TESTMAT = CAS;
											} else {
												c.cas_TESTMAT = c.cas_TESTMAT + "/t" + CAS;
											}
										} else if (obj5.get(0).equals("EC number")) {
											String ECNumber = obj5.get(2).getAsString();
											// System.out.println("ECNumber_TESTMAT="+ECNumber+"\t"+c.ECNumber);

											if (c.ECNumber_TESTMAT.equals("")) {
												c.ECNumber_TESTMAT = ECNumber;
											} else {
												c.ECNumber_TESTMAT = c.ECNumber_TESTMAT + "/t" + ECNumber;
											}

										}
									}
								}
								// System.out.println(obj3.toString());

							} else {

								if (key2.equals("TYPE_INVIVO_INVITRO")) {
									JsonObject obj3 = obj2.get(key2).getAsJsonObject();
									c.TYPE_INVIVO_INVITRO = obj3.get("LIST_POP").getAsString();
								} else if (key2.equals("STUDYTYPE")) {
									JsonObject obj3 = obj2.get(key2).getAsJsonObject();
									c.STUDYTYPE = obj3.get("LIST_POP").getAsString();
								} else if (key2.equals("TESTMAT_DETAILS")) {
									JsonObject obj3 = obj2.get(key2).getAsJsonObject();
									c.TESTMAT_DETAILS = obj3.get("freetext").getAsString();
									// c.TESTMAT_DETAILS=c.TESTMAT_DETAILS.replace("- ", "");
								}
							}

						}

					} else if (nextObject.equals("Applicant's summary and conclusion")) {
						JsonObject obj2 = obj.get(nextObject.getKey()).getAsJsonObject();

						Iterator<Entry<String, JsonElement>> iterator2 = obj2.entrySet().iterator();

						while (iterator2.hasNext()) {
							String key2 = iterator2.next().getKey();

							JsonObject obj3 = obj2.get(key2).getAsJsonObject();

							if (key2.equals("INTERPRET_RS_SUBMITTER")) {
								c.INTERPRET_RS_SUBMITTER = obj3.get("LIST_POP_FIX").getAsString();
							} else if (key2.equals("APPL_CL")) {
								c.APPL_CL = obj3.get("freetext").getAsString();
							}

							// System.out.println(key2);
						}

						// System.out.println(key);
					} else {
					}

				} // end iterator

				// System.out.println(c);
				records.add(c);

			}

			Collections.sort(records, new RecordSorter("ECNumber"));

			Vector<String> unusedscores = new Vector();

			for (int i = 0; i < records.size(); i++) {
				skinSensitizationDataRecord s = records.get(i);

				if (!s.cas.equals(""))
					s.cas = "'" + s.cas;
				if (!s.cas_TESTMAT.equals(""))
					s.cas_TESTMAT = "'" + s.cas_TESTMAT;

				// System.out.println(records.get(i));

				if (s.Reliability.equals("3 (not reliable)") || s.Reliability.equals("4 (not assignable)")) {
					// records.remove(i--);
					s.BinaryScore = "-1";
					s.BinaryScoreJustification = "low reliability";
					continue;
				}

				if (!s.ECNumber_TESTMAT.equals("")) {
					if (!s.ECNumber_TESTMAT.equals(s.ECNumber)) {
						s.Flag = "ECNumber doesn't match TESTMAT";
					}
				}

				if (!s.cas_TESTMAT.equals("") && !s.cas.equals("")) {
					if (!s.cas_TESTMAT.equals(s.cas)) {
						s.Flag = "CAS doesn't match TESTMAT";
					}
				}

				String score = s.INTERPRET_RS_SUBMITTER;

				if (score.equals("")) {

					s.BinaryScoreJustification = "no interpretation available";

				} else if (score.indexOf("ambiguous") > -1 || score.equals("other: Equivocal")
						|| score.equals("other: not classified") || score.equals("other: inconclusive")) {
					// records.remove(i--);
					s.BinaryScore = "-1";
					s.BinaryScoreJustification = "inconclusive result";

				} else if (score.indexOf("not sensitising") == 0) {
					s.BinaryScore = "0";
				} else if (score.indexOf("sensitising") == 0 || score.equals("other: moderate sensitiser")
						|| score.equals("other: Skin sensitizer Category 1A")
						|| score.equals("other: Sensitiser cat. 1B") || score.equals("other: Weak sensitizer")) {
					s.BinaryScore = "1";
				} else {
					// System.out.println(score);
					unusedscores.add(score);
					// records.remove(i--);

					s.BinaryScore = "-1";

					// 0 other: Positive in 10% of the test animals.
					// 1 other: Positive in 15% of the test animals.
					// 2 other: Result unreliable
					// 3 other: delayed contact hypersensitivity
					// 4 other: irritant
					// 5 other: mild sensitizer not requiring classification
					// 6 other: negative with weakly sensitisation potential
					// 7 other: no evaluation possible (see conclucion)
					// 8 other: no quantitative data, interpretation of results is not possible.
					// 9 other: see conclusions
					// 10 other: see conclusions
					// 11 other: sensitizing, but disregarded since concentration chosen for
					// intradermal induction was too high (induction of necrotic effects)
					// 12 other: slightly

					s.BinaryScoreJustification = "other result";

				}

			}
			Collections.sort(unusedscores);

			// for (int i=0;i<unusedscores.size();i++) {
			// System.out.println(i+"\t"+unusedscores.get(i));
			// }

			for (int i = 0; i < records.size(); i++) {
				skinSensitizationDataRecord s = records.get(i);
				System.out.println(s);
			}

			// System.out.println(records.size());
			//

			// System.out.println(counter);

		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(Line);
		}

	}

	void parseChemicalDataJSON() {

		String folder = "L:/Priv/Cin/NRMRL/CompTox/comptox/0 reach/reachextractor_V1";
		String filename = "ChemicalData.json";

		JsonParser parser = new JsonParser();

		String Line = "";

		/*
		 * ECNumber 4 _id 6 cid 15 cas 3 name 5 sdf 13 SMILES 10
		 * 
		 * SkinClassification 7
		 * 
		 * 
		 * hazards 1 nonHazards 2 EyeAcute 8 skinStructuralAlerts 9 OralTox 11
		 * computedProperties 12 Pubchem2D 14 skinStudies 16 pubchem2dModule 17
		 * 
		 */

		try {

			BufferedReader br = new BufferedReader(new FileReader(folder + "/" + filename));

			// Vector<String> items=new Vector<String>();//items for storing
			// list of unique keys

			int counter = 0;
			while (true) {

				Line = br.readLine();

				if (Line == null)
					break;

				Line = Line.replace("NaN", "-9999");// to avoid crash since it's
													// looking for a number!

				JsonObject obj = (JsonObject) parser.parse(Line);

				ChemicalDataRecords c = new ChemicalDataRecords();

				Iterator<Entry<String, JsonElement>> iterator = obj.entrySet().iterator();

				while (iterator.hasNext()) {

					Entry<String, JsonElement> nextObject = iterator.next();

					String key = nextObject.getKey();
					String val = nextObject.getValue().getAsString();

					if (key.equals("ECNumber")) {
						c.ECNumber = val;
						;
					} else if (key.equals("cas")) {
						c.cas = val;
					} else if (key.equals("name")) {
						c.name = val;
					} else if (key.equals("sdf")) {
						c.sdf = val;
					} else if (key.equals("SMILES")) {
						c.SMILES = val;
					} else if (key.equals("_id")) {
						JsonObject jo = nextObject.getValue().getAsJsonObject();
						c._id = jo.get("$oid").getAsString();
					} else if (key.equals("cid")) {
						JsonObject jo = nextObject.getValue().getAsJsonObject();
						c.cid = jo.get("$numberLong").getAsString();
					} else if (key.equals("SkinClassification")) {
						JsonObject jo = nextObject.getValue().getAsJsonObject();
						if (!jo.get("predictedSensitiser").isJsonNull()) {
							c.predictedSensitiser = jo.get("predictedSensitiser").getAsString();
						}
						if (!jo.get("classifiedSensitiser").isJsonNull()) {
							c.classifiedSensitiser = jo.get("classifiedSensitiser").getAsString();
						}
					}

				}

				// System.out.println(counter+"\t"+c.ECNumber+"\t"+c.cas+"\t"+c.SMILES+"\t"+c.cid);

				if (!c.classifiedSensitiser.equals("")) {

					if (!c.cas.equals("")) {
						// System.out.println(counter+"\t"+c.ECNumber+"\t"+c.cas+"\t"+c.name+"\t"+c.classifiedSensitiser);
						// counter++;

					} else if (!c.name.equals("")) {

						System.out.println(counter + "\t" + c.ECNumber + "\t" + c.cas + "\t" + c.name + "\t"
								+ c.classifiedSensitiser);
						counter++;
					}
				}

				// JSONObject objID=(JSONObject)obj.get("_id");
				// String ID=(String)objID.get("$oid");
				// System.out.println(objID+"\t"+ID);

				// {"_id":{"$oid":"5559ffe860377fa76ad6f659"},"SkinClassification":{},"ECNumber":"422-470-1","cas":"","name":"CIN
				// 10067729","EyeAcute":{"studies":[]},"nonHazards":[],"hazards":[]}

			}
			System.out.println(counter);

		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(Line);
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParseREACH_JSON_Files p = new ParseREACH_JSON_Files();
		// p.parseChemicalDataJSON();
		// p.getCASNumbersFromECInventory();
		p.parseSkinSensitizationData();
	}

}
