package gov.epa.exp_data_gathering.parse.PubChem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Markup;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Property;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Reference;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Section;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.StringWithMarkup;

import gov.epa.ghs_data_gathering.Utilities.FileUtilities;

public class RecordPubChem {
	String cid;
	String iupacNameCid;//from pubchem- based on cid 
	String canonSmilesCid;////from pubchem - based on cid
	String synonyms;
	
	String casReference;//cas number from original source from reference number
	String chemicalNameReference;// name from original source from reference number

	
//	transient Hashtable<String,String> htCAS;//lookup cas based on reference number
//	transient Hashtable<String,String> htChemicalName;//lookup chemical name based on reference number
	
	
	
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
	
	List<MarkupChemical> markupChemicals;
	
	
	public class MarkupChemical {
		String name;
		String cid;
	}


	String reference;
	String date_accessed;
	LiteratureSource literatureSource;
	PublicSource publicSourceOriginal;

	String pageUrl;

	String notes;

	static final String sourceName = ExperimentalConstants.strSourcePubChem + "_2024_03_20";

	static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues()
			.create();

	private static final transient UnitConverter unitConverter = new UnitConverter("data/density.txt");

	private RecordPubChem() {
//		cas = new Vector<String>();
//		htCAS=new Hashtable<String,String>();
//		htChemicalName=new Hashtable<String, String>();
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
		String databaseFolder = "Data" + File.separator + "Experimental" + File.separator + sourceName;
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

			System.out.println(cidsAlreadyQueried.size() + " CIDs in " + databasePath);

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
//		String databasePath = databaseFolder + File.separator + sourceName + "_raw_json - Copy.db";
		String databasePath = databaseFolder + File.separator + sourceName + "_raw_json.db";
		Vector<RecordPubChem> records = new Vector<>();

		try {
			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = SQLite_GetRecords.getAllRecords(stat, sourceName);

			int counter = 0;

			System.out.println("Going through records in " + databasePath);

			while (rs.next()) {

				counter++;

				if (counter % 1000 == 0) {
					System.out.println(counter);
				}

				String date = rs.getString("date");
				String experimental = rs.getString("experimental");
				Data experimentalData = gson.fromJson(experimental, Data.class);

				Hashtable<Integer, Reference> htReferences = getReferenceHashtable(experimentalData);
//				System.out.println(gson.toJson(experimentalData.record.reference));

				List<Section> experimentalProperties = experimentalData.record.section.get(0).section.get(0).section;

				for (Section section : experimentalProperties) {

//					System.out.println(gson.toJson(section));

					getRecords(records, rs, date, experimentalData, htReferences, section);
					
//					if (section.tocHeading.trim().equals("Dissociation Constants")) {
//						//TODO Other Experimental Properties has mix of things with no property name explicitly listed
//						getRecordsWithEmbeddedPropertyNames(records, rs, date, experimentalData, htReferences, section);
//					} else {
//						getRecords(records, rs, date, experimentalData, htReferences, section);
//					}

				}

//				if(true) break;

			} // end loop over records
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}
	
	
	protected static Vector<RecordPubChem> parseJSONInDatabase(String cid) {
		String databaseFolder = "Data" + File.separator + "Experimental" + File.separator + sourceName;
//		String databasePath = databaseFolder + File.separator + sourceName + "_raw_json - Copy.db";
		String databasePath = databaseFolder + File.separator + sourceName + "_raw_json.db";
		Vector<RecordPubChem> records = new Vector<>();

		try {
			Statement stat = SQLite_Utilities.getStatement(databasePath);
			
			String sql="select * from "+sourceName+" where cid="+cid+";";
			
			ResultSet rs = SQLite_GetRecords.getRecords(stat, sql);

			int counter = 0;

			System.out.println("Going through records in " + databasePath);

			while (rs.next()) {

				counter++;

				if (counter % 1000 == 0) {
					System.out.println(counter);
				}

				String date = rs.getString("date");
				String experimental = rs.getString("experimental");
				Data experimentalData = gson.fromJson(experimental, Data.class);

				Hashtable<Integer, Reference> htReferences = getReferenceHashtable(experimentalData);
//				System.out.println(gson.toJson(experimentalData.record.reference));

				List<Section> experimentalProperties = experimentalData.record.section.get(0).section.get(0).section;

				for (Section section : experimentalProperties) {

//					System.out.println(gson.toJson(section));

					getRecords(records, rs, date, experimentalData, htReferences, section);
					
//					if (section.tocHeading.trim().equals("Dissociation Constants")) {
//						//TODO Other Experimental Properties has mix of things with no property name explicitly listed
//						getRecordsWithEmbeddedPropertyNames(records, rs, date, experimentalData, htReferences, section);
//					} else {
//						getRecords(records, rs, date, experimentalData, htReferences, section);
//					}

				}

//				if(true) break;

			} // end loop over records
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}

	private static void getRecords(Vector<RecordPubChem> records, ResultSet rs, String date, Data experimentalData,
			Hashtable<Integer, Reference> htReferences, Section section) throws SQLException {
		
		Hashtable<String,String> htCAS=new Hashtable<String,String>();//lookup cas based on reference number
		Hashtable<String,String> htChemicalName=new Hashtable<String,String>();//lookup chemical name based on reference number

		String cas = rs.getString("cas");
		Data casData = gson.fromJson(cas, Data.class);
		if (casData != null) {
			List<Information> casInfo = casData.record.section.get(0).section.get(0).section.get(0).information;
			for (Information c : casInfo) {
				String newCAS = c.value.stringWithMarkup.get(0).string;
				htCAS.put(c.referenceNumber, newCAS);
			}
			
			if(casData.record!=null && casData.record.reference!=null) {
				List<Reference>reference=casData.record.reference;
				for (Reference ref:reference) {
					htChemicalName.put(ref.referenceNumber,ref.name);
				}
//				System.out.println(gson.toJson(pcr.htChemicalName));
			}
		}
		
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
				
//				pcr.propertyName = section.tocHeading.trim();
				
				if (information.name != null) {//happens with dissociation constants and other experimental properties
					pcr.propertyName = information.name.trim();
					// will have to parse out property name from the property value later
				} else {
					pcr.propertyName=section.tocHeading.trim();//temporary
				}
				
				pcr.propertyValue = valueString.string;				
				if(pcr.propertyValue!=null) pcr.propertyValue=pcr.propertyValue.trim();
				
				String s=pcr.propertyValue;
				
//				if(s.substring(s.length()-1,s.length()).contentEquals("/") && !s.contains("Estimated")) {//ends with /
//					System.out.println(pcr.propertyValue+"\n");
//				}
				
//				if(valueString.Markup==null) {
				
//				if(!pcr.propertyName.contentEquals("Physical Description") && !pcr.propertyName.contains("Other") && !pcr.propertyName.contains("Odor")) {
//				
//					System.out.println(pcr.propertyName+"\n"+gson.toJson(valueString));
				
//				}
				
				if(valueString.Markup!=null) {
					pcr.markupChemicals=new ArrayList<MarkupChemical>();
					for (Markup m:valueString.Markup) {
						MarkupChemical mc=pcr.new MarkupChemical();
						
						if (m.Extra!=null && m.Extra.indexOf("CID-")==0) {
							mc.cid=m.Extra.substring(4,m.Extra.length());	
						}
						
						if(m.URL!=null && m.URL.contains("compound")) 
							mc.name=m.URL.replace("https://pubchem.ncbi.nlm.nih.gov/compound/", "");	
						else if (m.URL!=null && m.URL.contains("element"))
							mc.name=m.URL.replace("https://pubchem.ncbi.nlm.nih.gov/element/", "");
					
						pcr.markupChemicals.add(mc);
					}
					
//					System.out.println(gson.toJson(pcr));
				}
				
				
				addIdentifiers(rs, pcr);
				addSourceMetadata(htReferences, information, pcr, htCAS,htChemicalName);
				records.add(pcr);
			}

		}
	}

//	private static void getRecordsWithEmbeddedPropertyNames(Vector<RecordPubChem> records, ResultSet rs, String date,
//			Data experimentalData, Hashtable<Integer, Reference> htReferences, Section section) throws SQLException {
//
//		for (Information information : section.information) {
//
//			List<StringWithMarkup> valueStrings = information.value.stringWithMarkup;
//			if (valueStrings == null) {
////							System.out.println(gson.toJson(information));
//				continue;
//			}
//
//			// Loop over property values
//			for (StringWithMarkup valueString : valueStrings) {
//
//				if (valueString.string == null)
//					continue;
//
//				RecordPubChem pcr = new RecordPubChem();
//				pcr.date_accessed = date.substring(0, date.indexOf(" "));
//				pcr.cid = experimentalData.record.recordNumber;
//
//				if (information.name != null) {
//					pcr.propertyName = information.name.trim();
//					// will have to extra later
//				} else {
//					pcr.propertyName=section.tocHeading.trim();//temporary
//				}
//
//				pcr.propertyValue = valueString.string;
//				addSourceMetadata(htReferences, information, pcr);
//				addIdentifiers(rs, pcr);
//				records.add(pcr);
////					System.out.println("here pcr="+gson.toJson(pcr));
//			}
//
//		}
//	}

	private static void addSourceMetadata(Hashtable<Integer, Reference> htReferences, Information information,
			RecordPubChem pcr, Hashtable<String, String> htCAS, Hashtable<String, String> htChemicalName) {

		if (information.reference != null) {
			pcr.literatureSource = new LiteratureSource();

			String citation1 = null;
			String citation2 = null;

			for (String reference : information.reference) {

				if (reference.contains("PMID:")) {

					if (reference.indexOf("PMID:") == 0) {
						String pmid = reference.substring(reference.indexOf(":") + 1, reference.length());
						pcr.literatureSource.url = "https://pubmed.ncbi.nlm.nih.gov/" + pmid + "/";
//						System.out.println(pcr.literatureSource.doi);
					} else if (reference.indexOf("DOI") > -1) {

						if (reference.indexOf("PMID") > -1) {
							String doi2 = reference.substring(reference.indexOf("DOI:") + 4, reference.length());
							doi2 = doi2.substring(0, doi2.indexOf(" ") - 1).trim();
							doi2 = "https://doi.org/" + doi2;
							pcr.literatureSource.doi = doi2;

						} else {
							System.out.println("Here2\treference=" + reference);
						}

						citation1 = reference.substring(0, reference.indexOf("DOI"));
						pcr.literatureSource.citation = citation1;

						if (reference.indexOf("PMID:") > 0) {
//							System.out.println(reference);
							String pmid = reference.substring(reference.indexOf("PMID:") + 5, reference.length());
							pcr.literatureSource.url = "https://pubmed.ncbi.nlm.nih.gov/" + pmid + "/";
//							System.out.println(pcr.literatureSource.url);
						}
					} else {
//						System.out.println("Here3\treference="+reference);
						pcr.literatureSource.citation = reference;
					}

				} else if (reference.contains("Tested as SID")) {
					pcr.notes = reference;
//					System.out.println(pcr.notes);
				} else {
					citation2 = reference;
					pcr.literatureSource.citation = citation2;
//					System.out.println(citation2);
				}
			}

//			if (citation1!=null && citation2!=null) {
//				System.out.println("citation1="+citation1);
//				System.out.println("citation2="+citation2+"\n");
//			}
//			System.out.println("pcr.notes="+pcr.notes+"\n");
//			if (information.reference.size() > 1) {
//				System.out.println(gson.toJson(pcr.literatureSource));
//			}
		}

		if (information.referenceNumber != null) {
			int refNum = Integer.parseInt(information.referenceNumber);

			Reference reference = htReferences.get(refNum);
			pcr.publicSourceOriginal = new PublicSource();
			pcr.publicSourceOriginal.name = reference.sourceName;
			pcr.publicSourceOriginal.description = reference.description;
			pcr.publicSourceOriginal.url = reference.url;// TODO fix these to remove specific page
			
			if(htCAS.containsKey(information.referenceNumber)) {
				pcr.casReference=htCAS.get(information.referenceNumber);
			} else {
//				System.out.println("cant get cas from ref num:"+information.referenceNumber+"\t"+pcr.cid);
			}

			if(htChemicalName.containsKey(information.referenceNumber)) {
				pcr.chemicalNameReference=htChemicalName.get(information.referenceNumber);	
//				System.out.println(pcr.chemical_name);
			} else {
//				System.out.println("cant get name from ref num:"+pcr.iupacName);
//				pcr.chemical_name=pcr.iupacName;//do we want to use this? doesnt come from original source
			}
			
					
			
//			System.out.println(gson.toJson(reference));
		}
	}

	/**
	 * Creates a new ExperimentalRecord object and sets all the fields that do not
	 * require advanced parsing
	 * 
	 * @return ExperimentalRecord
	 */
	protected ExperimentalRecord toExperimentalRecord(String propertyValueNonSplit) {

	
		ExperimentalRecord er = new ExperimentalRecord();

		er.experimental_parameters = new Hashtable<>();
		er.experimental_parameters.put("PubChem CID", cid);

		er.date_accessed = date_accessed;
		
		if(casReference==null && chemicalNameReference==null) {//use the values mapped to the cid if dont have anything from the reference
			er.smiles=canonSmilesCid;//use pubchem mapped smiles
			er.chemical_name=iupacNameCid;//use pubchem mapped name
//			System.out.println("Using main pubchem identifiers="+er.chemical_name+", smiles="+er.smiles);
//			System.out.println(er.publicSourceOriginal.name);
		} else {
			//use the identifiers from the property value's original reference (if available):
			er.casrn = casReference;				
			er.chemical_name = chemicalNameReference;
//			System.out.println("Using reference identifiers="+er.chemical_name+", cas="+er.casrn);
//			System.out.println("From main pubchem identifiers="+iupacNameCid+", smiles="+canonSmilesCid+"\n");
		}

		// TODO the propertyValue sometimes has a different chemical name inside it but
		// it's too hard to consistently parse it out due to the free formatting
		
		// TODO should we override chemical identifiers by setting DTXSID from dsstox compounds table using pubchem cid???

		if (synonyms != null) {
			er.synonyms = synonyms;
		}
		
		if (propertyName == null)
			return null;
		

		//TODO no way of knowing which pKa is acidic or basic unless they tag it as acidic or amino group		
//		if(propertyName.equals("Dissociation Constants")) {
//			String pv2=propertyValue.replace(" ", "").toLowerCase();
//
//			if(pv2.contains("pkb")) {
//				er.property_name=ExperimentalConstants.str_pKAb;
//			} else if(pv2.contains("kb")) {
//				er.property_name=ExperimentalConstants.str_KAb;
//			} else if (pv2.contains("pk")) {
//				er.property_name=ExperimentalConstants.str_pKA;//dont know whether acidic or basic
//			} else { 
//				System.out.println(er.casrn+"\t"+er.chemical_name+"\t"+ propertyValue);
//				return er;
//			}
//		} else {
//			er.property_name = standardizePropertyName(propertyName);	
//		}

		er.property_name = standardizePropertyName(propertyName);
		
		er.property_value_string = propertyValue;
		er.source_name = RecordPubChem.sourceName;

		boolean foundNumeric = false;
		propertyValue = propertyValue.replaceAll("(?i)greater than", ">");
		propertyValue = propertyValue.replaceAll("(?i)less than", "<");
		propertyValue = propertyValue.replaceAll("(?i) or equal to ", "=");
		propertyValue = propertyValue.replaceAll("(?i)about ", "~");
		
//		if(propertyValue.contains("pH") && !propertyName.equals(ExperimentalConstants.strWaterSolubility) && !propertyName.equals(ExperimentalConstants.strLogKOW) &&  !propertyName.equals(ExperimentalConstants.strAppearance)) {
//			System.out.println(propertyName+"\t"+propertyValue);
//		}
		

		if (er.property_name.equals(ExperimentalConstants.strDensity)
				|| er.property_name.equals(ExperimentalConstants.strVaporDensity)) {
			foundNumeric = ParseUtilities.getDensity(er, propertyValue,propertyValueNonSplit);
			ParseUtilities.getPressureCondition(er, propertyValue, sourceName);
			ParseUtilities.getTemperatureCondition(er, propertyValue);
						
//			if(!foundNumeric)
//				System.out.println("Density\t"+foundNumeric+"\t"+propertyValue);	

			
		} else if (er.property_name == ExperimentalConstants.strMeltingPoint
				|| er.property_name == ExperimentalConstants.strBoilingPoint
				|| er.property_name == ExperimentalConstants.strAutoIgnitionTemperature
				|| er.property_name == ExperimentalConstants.strFlashPoint) {
			foundNumeric = ParseUtilities.getTemperatureProperty(er, propertyValue);
			ParseUtilities.getPressureCondition(er, propertyValue, sourceName);
			
//			if(propertyValue.toLowerCase().contains("decomp")) {
//				er.keep=false;
//				er.reason="Decomposes";
////				er.property_value_qualitative="Decomposes";
////				System.out.println("Decomposes: "+propertyValue);
//			}

			if(er.property_name.equals(ExperimentalConstants.strFlashPoint)) {
				String PVLC=propertyValue.toLowerCase();
				
				if((PVLC.contains("close") && PVLC.contains("cup")) || PVLC.contains("c.c.") || PVLC.contains("closed")) {
					er.measurement_method="Closed cup";
				} else if((PVLC.contains("open") && PVLC.contains("cup")) || PVLC.contains("o.c.") || PVLC.contains("OC.") || PVLC.contains(", open")) {
					er.measurement_method="Open cup";
				} else {
//					System.out.println(propertyValue);
				}
			}
			
			
			if(propertyValue.toLowerCase().contains("not flammable")) {
				er.keep=false;
				er.property_value_qualitative="Not flammable";
//				System.out.println("Decomposes: "+propertyValue);
			} else if(propertyValue.toLowerCase().contains("flammable gas")) {
				er.keep=false;
				er.property_value_qualitative="Flammable gas";
			}

			
			if(propertyValue.toLowerCase().contains("sublim")) {
				er.keep=false;
				er.reason="Sublimates";
				er.property_value_qualitative="Sublimates";
//				System.out.println("Decomposes: "+propertyValue);
			}


		} else if (er.property_name.equals(ExperimentalConstants.strWaterSolubility)) {
			
//			System.out.println("Here1 sol");
			foundNumeric = ParseUtilities.getWaterSolubility(er, propertyValue, sourceName);

			if (er.temperature_C == null) {
				ParseUtilities.getTemperatureCondition(er, propertyValue);
			}
			
			ParseUtilities.get_pH_Condition(er, propertyValue);
			// TODO get pH- difficult because pH can be in difference places, especially
			// when have different solvents in same string

			ParseUtilities.getQualitativeSolubility(er, propertyValue, sourceName);

//			if(er.property_value_qualitative!=null && propertyValue.contains("ethanol") && !foundNumeric)
//				System.out.println(er.property_value_qualitative+"\t"+propertyValue);
			
			
//			System.out.println("Here2 sol");

//			if(er.property_value_point_estimate_original!=null && er.property_value_point_estimate_original<0) {
//				System.out.println("Negative value:"+gson.toJson(er));
//			}

			// TODO note- that ones with qualitative solubility will have keep=false due to
			// missing units
		} else if (er.property_name.equals(ExperimentalConstants.strVaporPressure)) {
			foundNumeric = ParseUtilities.getVaporPressure(er, propertyValue,propertyValue);
			ParseUtilities.getTemperatureCondition(er, propertyValue);
		} else if (er.property_name == ExperimentalConstants.strHenrysLawConstant) {
			foundNumeric = ParseUtilities.getHenrysLawConstant(er, propertyValue);
			
			ParseUtilities.getTemperatureCondition(er, propertyValue);
			
//			if(!foundNumeric)
//				System.out.println("HLC\t"+foundNumeric+"\t"+propertyValue);	
			
			
		} else if (er.property_name == ExperimentalConstants.strLogKOW
				|| er.property_name == ExperimentalConstants.str_pKA
				|| er.property_name == ExperimentalConstants.str_pKAa
				|| er.property_name == ExperimentalConstants.str_pKAb) {

			// TMM TODO fix cases with pH since it retrieves the pH instead of the property
			// value:
//			log Kow = -2.82 @ pH 7   ==> 7
//			log Kow: -0.89 (pH 4); -1.85 (pH 7); -1.89 (pH 9)  ==> 9

			// Following one works, but doesnt get the value at pH7:
//			log Kow = 0.74 at pH 5 and -1.34 at pH 7  ==> 0.74 

			foundNumeric = ParseUtilities.getLogProperty(er, propertyValue);
			er.property_value_units_original = ExperimentalConstants.str_LOG_UNITS;
			ParseUtilities.getTemperatureCondition(er, propertyValue);
			ParseUtilities.get_pH_Condition(er, propertyValue);
			
		} else if (er.property_name == ExperimentalConstants.strRefractiveIndex) {
			System.out.println("***TODO " + ExperimentalConstants.strRefractiveIndex + "\t" + propertyValue);
		
		} else if (er.property_name == ExperimentalConstants.strViscosity) {
			
//			System.out.println("***TODO " + ExperimentalConstants.strViscosity + "\t" + propertyValue);
			
			foundNumeric = ParseUtilities.getViscosity(er, propertyValue,propertyValueNonSplit);
//			ParseUtilities.getPressureCondition(er, propertyValue, sourceName);
			ParseUtilities.getTemperatureCondition(er, propertyValue);
			
			String pvLC=propertyValue.toLowerCase();
			
			if(pvLC.contains("@ boiling point") || pvLC.contains("at boiling point")) {
				er.reason="Value @ boiling point";
				er.keep=false;
			}
			
			if(pvLC.contains("@ melting point") || pvLC.contains("at melting point")) {
				er.reason="Value @ melting point";
				er.keep=false;
			}

			
			
			if(pvLC.contains("gas") || pvLC.contains("vapor")) {
				er.reason="Gas viscosity";
				er.keep=false;
			}
			
			if(pvLC.contains("%")|| pvLC.contains("soln") || pvLC.contains("solution")) {
				er.keep=false;
				er.reason="Solution";
			}

			
			
		} else if (er.property_name == ExperimentalConstants.strSurfaceTension) {
//			System.out.println("***TODO " + ExperimentalConstants.strSurfaceTension + "\t" + propertyValue);			
			foundNumeric = ParseUtilities.getSurfaceTension(er, propertyValue);
//			ParseUtilities.getPressureCondition(er, propertyValue, sourceName);
			ParseUtilities.getTemperatureCondition(er, propertyValue);

			
		} else if (er.property_name == ExperimentalConstants.strAppearance
				|| er.property_name == ExperimentalConstants.strOdor) {
			er.property_value_string = propertyValue;
			
//			er.property_value_qualitative = propertyValue.toLowerCase().replaceAll("colour", "color")
			
			er.property_value_qualitative = propertyValue.replace("colour", "color")
					.replace("odour", "odor").replace("vapour", "vapor");
			
			er.property_value_units_original = ExperimentalConstants.strTEXT;
			er.property_value_units_final = ExperimentalConstants.strTEXT;

		} else {
			System.out.println("Need to handle propertyValue for " + er.property_name);
		}

//		if (!er.property_name.equals(ExperimentalConstants.strWaterSolubility)
//				&& propertyValue.toLowerCase().contains("decomposes")) {
//			er.updateNote(ExperimentalConstants.str_dec);
//		}
		
//		if (propertyValue.toLowerCase().contains("est") && !propertyValue.toLowerCase().contains("ester")
//				&& !propertyValue.toLowerCase().contains("test")) {
//			// TODO is above if statement bulletproof?
//			er.updateNote(ExperimentalConstants.str_est);
//			er.keep = false;
//			er.reason = "Estimated";
//			System.out.println("Estimated:"+propertyValueNonSplit);
//		}
		
		
		if ((propertyValue.toLowerCase().contains("from experimentally derived coefficients"))) {
//			er.updateNote("Estimated from experimentally fit equation");
			er.updateNote(ExperimentalConstants.str_ext);
//			System.out.println("extrapolated from exp eqn:"+propertyValueNonSplit);
		} else if (propertyValueNonSplit.toLowerCase().contains("est vp/ws") 
				|| propertyValueNonSplit.toLowerCase().contains("est from vp/wsol")
				|| propertyValueNonSplit.toLowerCase().contains("estimated, vp/wsol")) {
			
			//OK
		} else if (propertyValueNonSplit.toLowerCase().contains("calcul")
				|| propertyValueNonSplit.toLowerCase().contains("estimat")
				|| propertyValueNonSplit.toLowerCase().contains("(est")
				|| propertyValueNonSplit.toLowerCase().contains("/est")
				|| propertyValueNonSplit.toLowerCase().contains("(calc")) {
			// TODO is above if statement bulletproof?
//			er.updateNote(ExperimentalConstants.str_est);
			er.keep = false;
			er.reason = "Estimated";
//			System.out.println("calculated:"+propertyValueNonSplit);
		
		} else if (propertyValueNonSplit.toLowerCase().contains("est") && !propertyValue.toLowerCase().contains("ester")) {			
//			System.out.println(propertyValueNonSplit);
		} else if (propertyValueNonSplit.toLowerCase().contains("calc")) {			
//			System.out.println(propertyValueNonSplit);			
		} else if ((propertyValueNonSplit.toLowerCase().contains("extrap"))) {
			er.updateNote(ExperimentalConstants.str_ext);
//			System.out.println("extrapolated:"+propertyValueNonSplit);
		}
		
		
		

		// Warns if there may be a problem with an entry
		if (propertyValue.contains("?")) {
			er.flag = true;
//			er.keep=false;
			er.updateNote("Question mark");
		}

		if(er.keep) {
			if ((foundNumeric || er.property_value_qualitative != null || er.note != null)) {

				if(er.reason!=null) {
					System.out.println("reason: "+er.reason+"\tflag: "+er.flag+"\tpV:"+er.property_value_string+"\tNote: "+er.note);
				}
				
//				er.reason = null;
				
			} else {

				er.keep = false;
				
				if(er.reason!=null) {
					System.out.println(er.reason);
				}
				
//				if(er.reason==null) System.out.println("Prev reason:"+er.reason);
				er.reason = "Bad data or units";

//				if(er.reason!=null) {
//					System.out.println(er.reason);
//				}

			}
		}
		
		
		if (publicSourceOriginal != null) {

			er.publicSourceOriginal = publicSourceOriginal;
			er.original_source_name=publicSourceOriginal.name;
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
			
//			if (literatureSource.doi != null)
//				System.out.println(gson.toJson(literatureSource));
			
			er.literatureSource = literatureSource;
			er.reference = literatureSource.citation;
		}


		unitConverter.convertRecord(er);
				
		if(propertyValue.toLowerCase().contains("decomp")) {
			er.keep=false;
			er.reason="decomposes";
		} else if(er.property_value_qualitative!=null) {//TODO is this bullet proof?
			er.keep=true;
			er.reason=null;
		}
		
		if(er.keep && er.property_value_qualitative==null && er.property_value_point_estimate_final==null 
				&& er.property_value_max_final==null && er.property_value_min_final==null ) {

			if(er.reason!=null && er.reason.equals("Incorrect property")) {
				// do nothing
			} else {
				er.keep=false;
				er.reason="No values";
//				er.updateNote("parsed propertyValue: "+er.property_value_string);
			}
			
		}

//		if(propertyValue.contains("pH") && (propertyValue.contains("@") || propertyValue.contains("log Kow"))) {
//		if(er.reference!=null && er.reference.equals("MacBean C, ed; The e-Pesticide Manual, 15th ed., Version 5.0.1. Surrey UK, British Crop Protection Council. Spirodiclofen (148477-71-8) (2010)")) {
//			System.out.println(propertyValue+"\tpoint_estimate="+er.property_value_point_estimate_original);
////			System.out.println(er.reference+"\n");
//		}
		


		return er;
	}

	/**
	 * Convert pubchem names to our db name
	 * 
	 */
	private String standardizePropertyName(String propertyName) {
		if (propertyName.equals("Physical Description") || propertyName.equals("Color/Form")) {
			return ExperimentalConstants.strAppearance;
		} else if (propertyName.equals("Odor")) {
			return ExperimentalConstants.strOdor;
		} else if (propertyName.equals("Boiling Point")) {
			return ExperimentalConstants.strBoilingPoint;
		} else if (propertyName.equals("Autoignition Temperature")) {
			return ExperimentalConstants.strAutoIgnitionTemperature;
		} else if (propertyName.equals("Refractive Index")) {
			return ExperimentalConstants.strRefractiveIndex;
		} else if (propertyName.equals("Flash Point")) {
			return ExperimentalConstants.strFlashPoint;
		} else if (propertyName.equals("Vapor Pressure")) {
			return ExperimentalConstants.strVaporPressure;
		} else if (propertyName.equals("Melting Point")) {
			return ExperimentalConstants.strMeltingPoint;
		} else if (propertyName.equals("Solubility")) {
			return ExperimentalConstants.strWaterSolubility;// may be any solvent though!
		} else if (propertyName.equals("Henry's Law Constant")) {
			return ExperimentalConstants.strHenrysLawConstant;
		} else if (propertyName.equals("Density")) {
			return ExperimentalConstants.strDensity;
		} else if (propertyName.equals("Vapor Density")) {
			return ExperimentalConstants.strVaporDensity;
		} else if (propertyName.equals("Viscosity")) {
			return ExperimentalConstants.strViscosity;
		} else if (propertyName.equals("LogP")) {
			return ExperimentalConstants.strLogKOW;
		} else if (propertyName.equals(ExperimentalConstants.str_pKA)) {
			return ExperimentalConstants.str_pKA;
		} else if (propertyName.equals(ExperimentalConstants.str_pKAa)) {
			return ExperimentalConstants.str_pKAa;
		} else if (propertyName.equals(ExperimentalConstants.str_pKAb)) {
			return ExperimentalConstants.str_pKAb;
		} else if (propertyName.equals("Surface Tension")) {
			return ExperimentalConstants.strSurfaceTension;
		} else {
			System.out.println("In standardizePropertyName() need to handle\t" + propertyName);
			return null;
		}
	}

	/***
	 * This info is from pubchem cid and not the original source
	 * 
	 * @param rs
	 * @param pcr
	 * @throws SQLException
	 */
	private static void addIdentifiers(ResultSet rs, RecordPubChem pcr) throws SQLException {
		String identifiers = rs.getString("identifiers");
		IdentifierData identifierData = gson.fromJson(identifiers, IdentifierData.class);

		if (identifierData != null) {
			Property identifierProperty = identifierData.propertyTable.properties.get(0);
			pcr.iupacNameCid = identifierProperty.iupacName;
			pcr.canonSmilesCid = identifierProperty.canonicalSMILES;
		}

		if (rs.getString("synonyms") != null)
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

	static void getCidsWithPropertyData() {

		Hashtable<String, String> htCIDs = ParsePubChem.getCID_HT();

		try {
			String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\PubChem_2024_03_20\\";
			FileWriter fw = new FileWriter(folder + "pubchem cids with data.txt");

			int counter = 0;

			for (String pubchemCID : htCIDs.keySet()) {

				counter++;

				String experimentalURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug_view/data/compound/" + pubchemCID
						+ "/JSON?heading=Experimental+Properties";

				try {
//					String experimental = FileUtilities.getText_UTF8_Line(experimentalURL);

					URL url = new URL(experimentalURL);
					HttpURLConnection huc = (HttpURLConnection) url.openConnection();

					int responseCode = huc.getResponseCode();

//					System.out.println(pubchemCID+"\t"+responseCode);

					if (responseCode == 200)
						fw.write(pubchemCID + "\t1\r\n");
					else
						fw.write(pubchemCID + "\t0\r\n");

//					System.out.println(experimental);

				} catch (Exception ex) {
					fw.write(pubchemCID + "\t0\r\n");
				}
//				Thread.sleep(100);			
				fw.flush();

				if (counter % 10 == 0)
					System.out.println(counter);
			}

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static void main(String[] args) {
//		Vector<RecordDashboard> drs = DownloadWebpageUtilities.getDashboardRecordsFromExcel("Data" + "/PFASSTRUCT.xls");
//		Vector<String> cids = getCIDsFromDashboardRecords(drs,"Data"+"/CIDDICT.csv",1,8164);

//		List<String> cidsList = gov.epa.QSAR.utilities.FileUtilities.readFile("Data\\Experimental\\PubChem\\solubilitycids.txt");
//		List<String> cidsList = gov.epa.QSAR.utilities.FileUtilities
//				.readFile("Data\\Experimental\\PubChem\\solubilitycids-test.txt");

		// TMM get data using cids from gabriels sqlite
//		RecordPubChem r = new RecordPubChem();
//		HashSet<String> cids = r.getCidsInDatabase("Pubchem");// old ones from 2020
//		downloadJSONsToDatabase(cids, false);

//		Vector<RecordPubChem>recs=parseJSONInDatabase("62695");//trans-2-butene
//		Vector<RecordPubChem>recs=parseJSONInDatabase("643835");//cis-2-hexene
//		Vector<RecordPubChem>recs=parseJSONInDatabase("241");//trans-2-butene
		
//		printPhyschemRecords(recs);

		
		
//		getCidsWithPropertyData();
	}

	private static void printPhyschemRecords(Vector<RecordPubChem> recs) {
		for (RecordPubChem rec:recs) {
			
			if(!rec.propertyName.equals("Solubility") &&
					!rec.propertyName.equals("Boiling Point") && 
					!rec.propertyName.equals("Melting Point") &&
					!rec.propertyName.equals("LogP") &&
					!rec.propertyName.equals("Henry's Law Constant") &&
					!rec.propertyName.equals("Vapor Pressure")) continue; 
			
			ExperimentalRecord er=rec.toExperimentalRecord(rec.propertyValue);
			
			if(rec.markupChemicals==null) continue;
			
			System.out.println(gson.toJson(rec)+"\n");
			System.out.println(gson.toJson(er)+"\n\n**********************\n");
			
		}
	}

	
	public void printObject(Object object) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println(gson.toJson(object));
	}
}
