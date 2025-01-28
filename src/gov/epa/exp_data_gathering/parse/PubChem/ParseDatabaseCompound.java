package gov.epa.exp_data_gathering.parse.PubChem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URL;
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

import gov.epa.database.SQLite_CreateTable;
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
import gov.epa.exp_data_gathering.parse.DownloadWebpageUtilities;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.PublicSource;
import gov.epa.exp_data_gathering.parse.RecordDashboard;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Data;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.IdentifierData;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Information;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Markup;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Reference;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Section;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.StringWithMarkup;
import gov.epa.exp_data_gathering.parse.PubChem.RecordPubChem.MarkupChemical;
import gov.epa.ghs_data_gathering.Utilities.FileUtilities;

/**
* @author TMARTI02
*/
public class ParseDatabaseCompound {

	
	static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues()
			.create();

	
	protected Vector<RecordPubChem> parseJSONsInDatabase() {
		
		String sourceName=RecordPubChem.sourceName;
		
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

	private Hashtable<Integer, Reference> getReferenceHashtable(Data experimentalData) {
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

	private void getRecords(Vector<RecordPubChem> records, ResultSet rs, String date, Data experimentalData,
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
				pcr.cid = Long.parseLong(experimentalData.record.recordNumber);
	
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
	
	
				String identifiers = rs.getString("identifiers");
				IdentifierData identifierData=null;
				if (identifiers!=null) {
					identifierData = gson.fromJson(identifiers, IdentifierData.class);
				}
				String synonyms=rs.getString("synonyms");
				pcr.addIdentifiers(identifierData,synonyms);
				addSourceMetadata(pcr, htReferences, information, htCAS,htChemicalName);
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
	
	private void addSourceMetadata(RecordPubChem pcr, Hashtable<Integer, Reference> htReferences, Information information,
			Hashtable<String, String> htCAS, Hashtable<String, String> htChemicalName) {
	
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

	protected Vector<RecordPubChem> parseJSONInDatabase(String cid) {
		String sourceName=RecordPubChem.sourceName;
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

	/**
	 * Extracts DTXSIDs from CompTox dashboard records and translates them to
	 * PubChem CIDs
	 * 
	 * @param records A vector of RecordDashboard objects
	 * @param start   The index in the vector to start converting
	 * @param end     The index in the vector to stop converting
	 * @return A vector of PubChem CIDs as strings
	 */
	private static HashSet<Long> getCIDsFromDashboardRecords(Vector<RecordDashboard> records, String dictFilePath,
			int start, int end) {
		HashSet<Long> cids = new HashSet<Long>();
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
				cids.add(Long.parseLong(cid));
				counter++;
			} else {
				boolean foundCID = false;
				try {
					String inchikey = records.get(i).INCHIKEY;
					String url = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/" + inchikey
							+ "/cids/TXT";
					String cidsTxt = FileUtilities.getText_UTF8(url);
					if (cidsTxt != null) {
						cids.add(Long.parseLong(cidsTxt.split("\r\n")[0]));
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
							cids.add(Long.parseLong(cidsTxt.split("\r\n")[0]));
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

	private static void downloadJSONsToDatabase(HashSet<Long> cids, boolean startFresh) {
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
	
		HashSet<Long> cidsAlreadyQueried=getCidsInDatabase2(p.sourceName);
	
		long sleep=200;
	
	
		try {
			int counterSuccess = 0;
			int counterTotal = 0;
			int counterMissingExpData = 0;
			long start = System.currentTimeMillis();
			for (Long cid : cids) {
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
					Thread.sleep(sleep);
					if (keepLooking) {
						try {
							//							rec.cas=FileUtilities.getText_UTF8(casURL).replaceAll("'", "\'").replaceAll(";", "\\;");
							rec.cas = FileUtilities.getText_UTF8(casURL);
							rec.cas = rec.cas.replaceAll("'", "''").replaceAll(";", "\\;");
						} catch (Exception ex) {
						}
						Thread.sleep(sleep);
						try {
							//							rec.identifiers=FileUtilities.getText_UTF8(idURL).replaceAll("'", "\'").replaceAll(";", "\\;");
							rec.identifiers = FileUtilities.getText_UTF8(idURL);
							rec.identifiers = rec.identifiers.replaceAll("'", "''").replaceAll(";", "\\;");
						} catch (Exception ex) {
						}
						Thread.sleep(sleep);
						try {
							//							rec.synonyms=FileUtilities.getText_UTF8(synonymURL).replaceAll("'", "\'").replaceAll(";", "\\;");
							rec.synonyms = StringEscapeUtils.escapeHtml4(FileUtilities.getText_UTF8(synonymURL));
							rec.synonyms = rec.synonyms.replaceAll("'", "''").replaceAll(";", "\\;");
						} catch (Exception ex) {
						}
						Thread.sleep(sleep);
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

	public static HashSet<Long> getCidsInDatabase2(String sourceName) {
		String databaseName = sourceName + "_raw_json.db";
		String tableName = sourceName;
		String databaseFolder = "Data" + File.separator + "Experimental" + File.separator + sourceName;
		String databasePath = databaseFolder + File.separator + databaseName;
	
		java.sql.Connection conn = SQLite_Utilities.getConnection(databasePath);
	
		HashSet<Long> cidsAlreadyQueried = new HashSet<>();
	
		ResultSet rs = SQLite_GetRecords.getRecords(SQLite_Utilities.getStatement(conn), "select cid from "+tableName);
		try {
			long start = System.currentTimeMillis();
			while (rs.next()) {
				cidsAlreadyQueried.add(rs.getLong(1));
			}
			long end = System.currentTimeMillis();
	
			System.out.println(cidsAlreadyQueried.size() + " CIDs in " + databasePath);
	
			return cidsAlreadyQueried;
	
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	
	}

	public static HashSet<String> getCidsInDatabase(String sourceName) {
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
	
	
	public static void main(String[] args) {
		Vector<RecordDashboard> drs = DownloadWebpageUtilities.getDashboardRecordsFromExcel("Data" + "/PFASSTRUCT.xls");
		HashSet<Long> cids = getCIDsFromDashboardRecords(drs,"Data"+"/CIDDICT.csv",1,8164);

		//		List<String> cidsList = gov.epa.QSAR.utilities.FileUtilities.readFile("Data\\Experimental\\PubChem\\solubilitycids.txt");
		//		List<String> cidsList = gov.epa.QSAR.utilities.FileUtilities
		//				.readFile("Data\\Experimental\\PubChem\\solubilitycids-test.txt");

		// TMM get data using cids from gabriels sqlite
		
		downloadJSONsToDatabase(cids, true);
		//		downloadJSONsToDatabase(cids, false);

		//		Vector<RecordPubChem>recs=parseJSONInDatabase("62695");//trans-2-butene
		//		Vector<RecordPubChem>recs=parseJSONInDatabase("643835");//cis-2-hexene
		//		Vector<RecordPubChem>recs=parseJSONInDatabase("241");//trans-2-butene

		//		printPhyschemRecords(recs);

		//		getCidsWithPropertyData();
	}

}
