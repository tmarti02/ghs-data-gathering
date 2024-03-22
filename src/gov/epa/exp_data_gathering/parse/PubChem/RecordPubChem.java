package gov.epa.exp_data_gathering.parse.PubChem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.database.SQLite_CreateTable;
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
import gov.epa.exp_data_gathering.parse.DownloadWebpageUtilities;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.PublicSource;
import gov.epa.exp_data_gathering.parse.RecordDashboard;
import gov.epa.exp_data_gathering.parse.UnitConverter;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Data;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.IdentifierData;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Information;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Property;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Reference;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Section;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.StringWithMarkup;

import gov.epa.ghs_data_gathering.Utilities.FileUtilities;

public class RecordPubChem {
	String cid;
	String iupacName;
	String smiles;
	String synonyms;
	Vector<String> cas;
//	Vector<String> physicalDescription;
//	Vector<String> density;
//	Vector<String> meltingPoint;
//	Vector<String> boilingPoint;
//	Vector<String> flashPoint;
//	Vector<String> solubility;
//	Vector<String> vaporPressure;
//	Vector<String> henrysLawConstant;
//	Vector<String> logP;
//	Vector<String> pKa;
//	Hashtable<Integer, String> physicalDescriptionHT = new Hashtable<>();
//	Hashtable<Integer, String> densityHT = new Hashtable<>();
//	Hashtable<Integer, String> meltingPointHT = new Hashtable<>();
//	Hashtable<Integer, String> boilingPointHT = new Hashtable<>();
//	Hashtable<Integer, String> solubilityHT = new Hashtable<>();
//	Hashtable<Integer, String> flashPointHT = new Hashtable<>();
//	Hashtable<Integer, String> vaporPressureHT = new Hashtable<>();
//	Hashtable<Integer, String> hlcHT = new Hashtable<>();
//	Hashtable<Integer, String> logPHT = new Hashtable<>();
//	Hashtable<Integer, String> pKaHT = new Hashtable<>();

	String propertyName;
	String propertyValue;

	String reference;
	String date_accessed;
	LiteratureSource literatureSource;
	PublicSource publicSourceOriginal;

	String pageUrl;

	static final String sourceName = ExperimentalConstants.strSourcePubChem + "_2024_03_20";

	static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues()
			.create();

	private static final transient UnitConverter unitConverter = new UnitConverter("data/density.txt");

	private RecordPubChem() {
		cas = new Vector<String>();
//		physicalDescription = new Vector<String>();
//		density = new Vector<String>();
//		meltingPoint = new Vector<String>();
//		boilingPoint = new Vector<String>();
//		flashPoint = new Vector<String>();
//		solubility = new Vector<String>();
//		vaporPressure = new Vector<String>();
//		henrysLawConstant = new Vector<String>();
//		logP = new Vector<String>();
//		pKa = new Vector<String>();
	}

	/**
	 * Extracts DTXSIDs from CompTox dashboard records and translates them to
	 * PubChem CIDs
	 * 
	 * @param records A vector of RecordDashboard objects
	 * @param start   The index in the vector to start converting
	 * @param end     The index in the vector to stop converting
	 * @return A vector of PubChem CIDs as strings
	 */
	private static Vector<String> getCIDsFromDashboardRecords(Vector<RecordDashboard> records, String dictFilePath,
			int start, int end) {
		Vector<String> cids = new Vector<String>();
		LinkedHashMap<String, String> dict = new LinkedHashMap<String, String>();

		try {
			File file = new File(dictFilePath);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] cells = line.split(",");
				dict.put(cells[0], cells[1]);
			}
			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		int counter = 0;
		for (int i = start; i < end; i++) {
			String dtxsid = records.get(i).DTXSID;
			String cid = dict.get(dtxsid);
			if (cid != null) {
				cids.add(cid);
				counter++;
			} else {
				boolean foundCID = false;
				try {
					String inchikey = records.get(i).INCHIKEY;
					String url = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/" + inchikey
							+ "/cids/TXT";
					String cidsTxt = FileUtilities.getText_UTF8(url);
					if (cidsTxt != null) {
						cids.add(cidsTxt.split("\r\n")[0]);
						foundCID = true;
						counter++;
					}
					Thread.sleep(200);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				if (!foundCID) {
					try {
						String smiles = records.get(i).SMILES;
						String url = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/smiles/" + smiles
								+ "/cids/TXT";
						String cidsTxt = FileUtilities.getText_UTF8(url);
						if (cidsTxt != null) {
							cids.add(cidsTxt.split("\r\n")[0]);
							foundCID = true;
							counter++;
						}
						Thread.sleep(200);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			if (counter % 100 == 0) {
				System.out.println("Found " + counter + " CIDs");
			}
		}
		System.out.println("Found " + counter + " CIDs");
		return cids;
	}

	
	HashSet<String> getCidsInDatabase(String sourceName) {
		String databaseName = sourceName + "_raw_json.db";
		String tableName = sourceName;
		String databaseFolder="Data" + File.separator + "Experimental" + File.separator + sourceName;
		String databasePath = databaseFolder + File.separator + databaseName;
		
		
		java.sql.Connection conn = SQLite_Utilities.getConnection(databasePath);
		
		HashSet<String> cidsAlreadyQueried = new HashSet<String>();
		ResultSet rs = SQLite_GetRecords.getAllRecords(SQLite_Utilities.getStatement(conn), tableName);
		try {
			long start = System.currentTimeMillis();
			while (rs.next()) {
				cidsAlreadyQueried.add(rs.getString("cid"));
			}
			long end = System.currentTimeMillis();
		
			System.out.println(cidsAlreadyQueried.size() + " CIDs in "+databasePath);
			
			return cidsAlreadyQueried;
		
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}
	
	private static void downloadJSONsToDatabase(HashSet<String> cids, boolean startFresh) {
		ParsePubChem p = new ParsePubChem();
		String databaseName = p.sourceName + "_raw_json.db";
		String tableName = p.sourceName;
		String databasePath = p.databaseFolder + File.separator + databaseName;
		File db = new File(databasePath);
		if (!db.getParentFile().exists()) {
			db.getParentFile().mkdirs();
		}
		java.sql.Connection conn = SQLite_CreateTable.create_table(databasePath, tableName,
				RawDataRecordPubChem.fieldNames, startFresh);
		HashSet<String> cidsAlreadyQueried = new HashSet<String>();
		ResultSet rs = SQLite_GetRecords.getAllRecords(SQLite_Utilities.getStatement(conn), tableName);
		try {
			long start = System.currentTimeMillis();
			while (rs.next()) {
				cidsAlreadyQueried.add(rs.getString("cid"));
			}
			long end = System.currentTimeMillis();
			System.out.println(cidsAlreadyQueried.size() + " CIDs already queried (" + (end - start) + " ms)");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			int counterSuccess = 0;
			int counterTotal = 0;
			int counterMissingExpData = 0;
			long start = System.currentTimeMillis();
			for (String cid : cids) {
				String experimentalURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug_view/data/compound/" + cid
						+ "/JSON?heading=Experimental+Properties";
				String idURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/property/IUPACName,CanonicalSMILES/JSON?cid="
						+ cid;
				String casURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug_view/data/compound/" + cid
						+ "/JSON?heading=CAS";
				String synonymURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/" + cid + "/synonyms/TXT";

				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				Date date = new Date();
				String strDate = formatter.format(date);

				RawDataRecordPubChem rec = new RawDataRecordPubChem(strDate, cid, "", "", "", "");
				if (cidsAlreadyQueried.add(cid) || startFresh) {
					counterTotal++;
					boolean keepLooking = true;
					try {
						rec.experimental = FileUtilities.getText_UTF8(experimentalURL);
						rec.experimental = rec.experimental.replaceAll("'", "''").replaceAll(";", "\\;");
					} catch (Exception ex) {
						counterMissingExpData++;
						keepLooking = false;
					}
					Thread.sleep(200);
					if (keepLooking) {
						try {
//							rec.cas=FileUtilities.getText_UTF8(casURL).replaceAll("'", "\'").replaceAll(";", "\\;");
							rec.cas = FileUtilities.getText_UTF8(casURL);
							rec.cas = rec.cas.replaceAll("'", "''").replaceAll(";", "\\;");
						} catch (Exception ex) {
						}
						Thread.sleep(200);
						try {
//							rec.identifiers=FileUtilities.getText_UTF8(idURL).replaceAll("'", "\'").replaceAll(";", "\\;");
							rec.identifiers = FileUtilities.getText_UTF8(idURL);
							rec.identifiers = rec.identifiers.replaceAll("'", "''").replaceAll(";", "\\;");
						} catch (Exception ex) {
						}
						Thread.sleep(200);
						try {
//							rec.synonyms=FileUtilities.getText_UTF8(synonymURL).replaceAll("'", "\'").replaceAll(";", "\\;");
							rec.synonyms = StringEscapeUtils.escapeHtml4(FileUtilities.getText_UTF8(synonymURL));
							rec.synonyms = rec.synonyms.replaceAll("'", "''").replaceAll(";", "\\;");
						} catch (Exception ex) {
						}
						Thread.sleep(200);
					}
					if (rec.experimental != null && !rec.experimental.isBlank()) {
						rec.addRecordToDatabase(tableName, conn);
						counterSuccess++;
					}
					if (counterTotal % 100 == 0) {
						long batchEnd = System.currentTimeMillis();
						System.out.println("Attempted: " + counterTotal + " (" + cidsAlreadyQueried.size() + " total)");
						System.out.println("Succeeded: " + counterSuccess);
						System.out.println("Failed - no experimental properties: " + counterMissingExpData);
						System.out.println("---------- (~" + (batchEnd - start) / 60000 + " min)");
						start = batchEnd;
					}
				}
			}
			System.out.println("Attempted: " + counterTotal + " (" + cidsAlreadyQueried.size() + " total)");
			System.out.println("Succeeded: " + counterSuccess);
			System.out.println("Failed - no experimental properties: " + counterMissingExpData);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected static Vector<RecordPubChem> parseJSONsInDatabase() {
		String databaseFolder = "Data" + File.separator + "Experimental" + File.separator + sourceName;
		String databasePath = databaseFolder + File.separator + sourceName + "_raw_json.db";
		Vector<RecordPubChem> records = new Vector<>();

		try {
			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = SQLite_GetRecords.getAllRecords(stat, sourceName);

			while (rs.next()) {

				String date = rs.getString("date");
				String experimental = rs.getString("experimental");
				Data experimentalData = gson.fromJson(experimental, Data.class);

				Hashtable<Integer, Reference> htReferences = getReferenceHashtable(experimentalData);
//				System.out.println(gson.toJson(experimentalData.record.reference));

				List<Section> experimentalProperties = experimentalData.record.section.get(0).section.get(0).section;

				for (Section section : experimentalProperties) {

//					System.out.println(gson.toJson(section));

					for (Information information : section.information) {

//						System.out.println(gson.toJson(information));

						List<StringWithMarkup> valueStrings = information.value.stringWithMarkup;
						if (valueStrings == null) {
//							System.out.println(gson.toJson(information));
							continue;
						}

						// Loop over property values
						for (StringWithMarkup valueString : valueStrings) {

							if (valueString.string == null)
								continue;

							RecordPubChem pcr = new RecordPubChem();
							pcr.date_accessed = date.substring(0, date.indexOf(" "));
							pcr.cid = experimentalData.record.recordNumber;
							pcr.propertyName = section.tocHeading.trim();
							pcr.propertyValue = valueString.string;

							addSourceMetadata(htReferences, information, pcr);
							addIdentifiers(rs, pcr);
							records.add(pcr);
						}
					}
				}

//				if(true) break;

			} // end loop over records
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}

	private static void addSourceMetadata(Hashtable<Integer, Reference> htReferences, Information information,
			RecordPubChem pcr) {

		if (information.reference != null) {
			pcr.literatureSource = new LiteratureSource();
			pcr.literatureSource.citation = information.reference.get(0);// Use first one for convenience

			if (information.reference.size() > 1) {
				System.out.println("Has multiple literature sources");
			}
		}

		if (information.referenceNumber != null) {
			int refNum = Integer.parseInt(information.referenceNumber);

			Reference reference = htReferences.get(refNum);
			pcr.publicSourceOriginal = new PublicSource();
			pcr.publicSourceOriginal.name = reference.sourceName;
			pcr.publicSourceOriginal.description = reference.description;
			pcr.publicSourceOriginal.url = reference.url;// TODO fix these to remove specific page
//			System.out.println(gson.toJson(reference));
		}
	}

	protected ExperimentalRecord toExperimentalRecord() {

		ExperimentalRecord er = new ExperimentalRecord();

		er.experimental_parameters = new Hashtable<>();

		// Creates a new ExperimentalRecord object and sets all the fields that do not
		// require advanced parsing

		er.date_accessed = date_accessed;
		er.casrn = String.join("|", cas);
		er.chemical_name = iupacName;
		er.smiles = smiles;
		
		//TODO get DTXSID from compounds table from dsstox???
		

		if (synonyms != null) {
			er.synonyms = synonyms;
		}

		if (propertyName.equals("Physical Description") || propertyName.equals("Color/Form")) {
			propertyName = ExperimentalConstants.strAppearance;
		} else if (propertyName.equals("Odor")) {
			propertyName = ExperimentalConstants.strOdor;
		} else if (propertyName.equals("Boiling Point")) {
			propertyName = ExperimentalConstants.strBoilingPoint;
		} else if (propertyName.equals("Autoignition Temperature")) {
			propertyName = ExperimentalConstants.strAutoIgnitionTemperature;
		} else if (propertyName.equals("Refractive Index")) {
			propertyName = ExperimentalConstants.strRefractiveIndex;
		} else if (propertyName.equals("Flash Point")) {
			propertyName = ExperimentalConstants.strFlashPoint;
		} else if (propertyName.equals("Vapor Pressure")) {
			propertyName = ExperimentalConstants.strVaporPressure;
		} else if (propertyName.equals("Melting Point")) {
			propertyName = ExperimentalConstants.strBoilingPoint;
		} else if (propertyName.equals("Solubility")) {
			propertyName = ExperimentalConstants.strWaterSolubility;
		} else if (propertyName.equals("Henry's Law Constant")) {
			propertyName = ExperimentalConstants.strHenrysLawConstant;
		} else if (propertyName.equals("Density")) {
			propertyName = ExperimentalConstants.strDensity;
		} else if (propertyName.equals("Vapor Density")) {
			propertyName = ExperimentalConstants.strVaporDensity;
		} else if (propertyName.equals("Viscosity")) {
			propertyName = ExperimentalConstants.strViscosity;
		} else if (propertyName.equals("LogP")) {
			propertyName = ExperimentalConstants.strLogKOW;
		} else if (propertyName.equals("Surface Tension")) {
			propertyName = ExperimentalConstants.strSurfaceTension;
		} else {
			System.out.println("Need to handle propertyName standardization:\t" + propertyName);
		}

		er.property_name = propertyName;
		er.property_value_string = propertyValue;
		er.source_name = ExperimentalConstants.strSourcePubChem;

		boolean foundNumeric = false;
		propertyValue = propertyValue.replaceAll("(?i)greater than", ">");
		propertyValue = propertyValue.replaceAll("(?i)less than", "<");
		propertyValue = propertyValue.replaceAll("(?i) or equal to ", "=");
		propertyValue = propertyValue.replaceAll("(?i)about ", "~");

		if (propertyName.equals(ExperimentalConstants.strDensity)
				|| propertyName.equals(ExperimentalConstants.strVaporDensity)) {
			foundNumeric = ParseUtilities.getDensity(er, propertyValue);
			ParseUtilities.getPressureCondition(er, propertyValue, sourceName);
			ParseUtilities.getTemperatureCondition(er, propertyValue);
		} else if (propertyName == ExperimentalConstants.strMeltingPoint
				|| propertyName == ExperimentalConstants.strBoilingPoint
				|| propertyName == ExperimentalConstants.strAutoIgnitionTemperature
				|| propertyName == ExperimentalConstants.strFlashPoint) {
			foundNumeric = ParseUtilities.getTemperatureProperty(er, propertyValue);
			ParseUtilities.getPressureCondition(er, propertyValue, sourceName);

			if (propertyValue.contains("closed cup") || propertyValue.contains("c.c.")) {
				er.experimental_parameters.put("Measurement method", "closed cup");
			}
		} else if (propertyName.equals(ExperimentalConstants.strWaterSolubility)) {
			foundNumeric = ParseUtilities.getWaterSolubility(er, propertyValue, sourceName);
			if (er.temperature_C == null) {
				ParseUtilities.getTemperatureCondition(er, propertyValue);
			}
			ParseUtilities.getQualitativeSolubility(er, propertyValue, sourceName);
			// TODO note- that ones with qualitative solubility will have keep=false due to
			// missing units
		} else if (propertyName.equals(ExperimentalConstants.strVaporPressure)) {
			foundNumeric = ParseUtilities.getVaporPressure(er, propertyValue);
			ParseUtilities.getTemperatureCondition(er, propertyValue);
		} else if (propertyName == ExperimentalConstants.strHenrysLawConstant) {
			foundNumeric = ParseUtilities.getHenrysLawConstant(er, propertyValue);
		} else if (propertyName == ExperimentalConstants.strLogKOW || propertyName == ExperimentalConstants.str_pKA) {
			foundNumeric = ParseUtilities.getLogProperty(er, propertyValue);
			er.property_value_units_original = ExperimentalConstants.str_LOG_UNITS;
			ParseUtilities.getTemperatureCondition(er, propertyValue);
		} else if (propertyName == ExperimentalConstants.strRefractiveIndex) {
			System.out.println("***TODO " + ExperimentalConstants.strRefractiveIndex + "\t" + propertyValue);
		} else if (propertyName == ExperimentalConstants.strViscosity) {
			System.out.println("***TODO " + ExperimentalConstants.strViscosity + "\t" + propertyValue);
		} else if (propertyName == ExperimentalConstants.strSurfaceTension) {
			System.out.println("***TODO " + ExperimentalConstants.strSurfaceTension + "\t" + propertyValue);
		} else if (propertyName == ExperimentalConstants.strAppearance
				|| propertyName == ExperimentalConstants.strOdor) {
			er.property_value_string = propertyValue;
			er.property_value_qualitative = propertyValue.toLowerCase().replaceAll("colour", "color")
					.replaceAll("odour", "odor").replaceAll("vapour", "vapor");
			er.property_value_units_original = ExperimentalConstants.strTEXT;
			er.property_value_units_final = ExperimentalConstants.strTEXT;

		} else {
			System.out.println("Need to handle propertyValue for " + propertyName);
		}

		if (!propertyName.equals(ExperimentalConstants.strWaterSolubility)
				&& propertyValue.toLowerCase().contains("decomposes")) {
			er.updateNote(ExperimentalConstants.str_dec);
		}
		if (propertyValue.toLowerCase().contains("est") && !propertyValue.toLowerCase().contains("ester")
				&& !propertyValue.toLowerCase().contains("test")) {
			// TODO is above if statement bulletproof?

			er.updateNote(ExperimentalConstants.str_est);
			er.keep = false;
			er.reason = "Estimated";
		}
		if ((propertyValue.toLowerCase().contains("ext") || propertyValue.toLowerCase().contains("from exp"))
				&& !propertyValue.toLowerCase().contains("extreme")
				&& !propertyValue.toLowerCase().contains("extent")) {
			er.updateNote(ExperimentalConstants.str_ext);
			er.keep = false;
			er.reason = "Estimated";
		}
		// Warns if there may be a problem with an entry
		if (propertyValue.contains("?")) {
			er.flag = true;
			er.reason = "Question mark";
		}

		if ((foundNumeric || er.property_value_qualitative != null || er.note != null) && er.keep) {
			er.keep = true;
			er.reason = null;
		} else if (er.keep) {
			er.keep = false;
			er.reason = "Bad data or units";
		}

		if (publicSourceOriginal != null) {

			er.publicSourceOriginal = publicSourceOriginal;
			er.url = publicSourceOriginal.url;

			if (publicSourceOriginal.name.equals("EPA DSSTox")) {
				er.keep = false;
				er.reason = "EPIsuite duplicate";
			} else if (publicSourceOriginal.name.equals("Sanford-Burnham Center for Chemical Genomics")) {
				er.keep = false;
				er.reason = "source data not retrievable";
			}
		}

		if (literatureSource != null) {
			er.literatureSource = literatureSource;
		}

		unitConverter.convertRecord(er);

		return er;
	}

	private static void addIdentifiers(ResultSet rs, RecordPubChem pcr) throws SQLException {
		String identifiers = rs.getString("identifiers");
		IdentifierData identifierData = gson.fromJson(identifiers, IdentifierData.class);

		if (identifierData != null) {
			Property identifierProperty = identifierData.propertyTable.properties.get(0);
			pcr.iupacName = identifierProperty.iupacName;
			pcr.smiles = identifierProperty.canonicalSMILES;
		}

		String cas = rs.getString("cas");
		Data casData = gson.fromJson(cas, Data.class);
		if (casData != null) {
			List<Information> casInfo = casData.record.section.get(0).section.get(0).section.get(0).information;
			for (Information c : casInfo) {
				String newCAS = c.value.stringWithMarkup.get(0).string;
				if (!pcr.cas.contains(newCAS)) {
					pcr.cas.add(newCAS);
				}
			}
		}

		pcr.synonyms = rs.getString("synonyms").replaceAll("\r\n", "|");
	}

	private static Hashtable<Integer, Reference> getReferenceHashtable(Data experimentalData) {
		Hashtable<Integer, Reference> htReferences = null;
		if (experimentalData.record.reference != null) {
			htReferences = new Hashtable<>();
			for (Reference reference : experimentalData.record.reference) {
				int refNum = Integer.parseInt(reference.referenceNumber);
				htReferences.put(refNum, reference);
			}
		}
		return htReferences;
	}

	public static void main(String[] args) {
//		Vector<RecordDashboard> drs = DownloadWebpageUtilities.getDashboardRecordsFromExcel("Data" + "/PFASSTRUCT.xls");
//		Vector<String> cids = getCIDsFromDashboardRecords(drs,"Data"+"/CIDDICT.csv",1,8164);

//		List<String> cidsList = gov.epa.QSAR.utilities.FileUtilities.readFile("Data\\Experimental\\PubChem\\solubilitycids.txt");
//		List<String> cidsList = gov.epa.QSAR.utilities.FileUtilities
//				.readFile("Data\\Experimental\\PubChem\\solubilitycids-test.txt");

		RecordPubChem r=new RecordPubChem();
		HashSet <String>cids=r.getCidsInDatabase("Pubchem");//old ones from 2020
		
		
//		Vector<String> cids = new Vector<String>(cidsList);
		downloadJSONsToDatabase(cids, false);
	}

	public void printObject(Object object) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println(gson.toJson(object));
	}
}
