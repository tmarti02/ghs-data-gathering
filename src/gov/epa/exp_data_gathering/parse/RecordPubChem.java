package gov.epa.exp_data_gathering.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.JSONsForPubChem.*;
import gov.epa.ghs_data_gathering.Database.CreateGHS_Database;
import gov.epa.ghs_data_gathering.Database.MySQL_DB;
import gov.epa.ghs_data_gathering.GetData.RecordDashboard;
import gov.epa.ghs_data_gathering.Utilities.FileUtilities;

public class RecordPubChem {
	String cid;
	String iupacName;
	String smiles;
	String synonyms;
	Vector<String> cas;
	Vector<String> physicalDescription;
	Vector<String> density;
	Vector<String> meltingPoint;
	Vector<String> boilingPoint;
	Vector<String> flashPoint;
	Vector<String> solubility;
	Vector<String> vaporPressure;
	Vector<String> henrysLawConstant;
	Vector<String> logP;
	Vector<String> pKa;
	String date_accessed;
	
	static final String sourceName=ExperimentalConstants.strSourcePubChem;
	
	private RecordPubChem() {
		cas = new Vector<String>();
		physicalDescription = new Vector<String>();
		density = new Vector<String>();
		meltingPoint = new Vector<String>();
		boilingPoint = new Vector<String>();
		flashPoint = new Vector<String>();
		solubility = new Vector<String>();
		vaporPressure = new Vector<String>();
		henrysLawConstant = new Vector<String>();
		logP = new Vector<String>();
		pKa = new Vector<String>();
	}
	
	/**
	 * Extracts DTXSIDs from CompTox dashboard records and translates them to PubChem CIDs
	 * @param records	A vector of RecordDashboard objects
	 * @param start		The index in the vector to start converting
	 * @param end		The index in the vector to stop converting
	 * @return			A vector of PubChem CIDs as strings
	 */
	private static Vector<String> getCIDsFromDashboardRecords(Vector<RecordDashboard> records,String dictFilePath,int start,int end) {
		Vector<String> cids = new Vector<String>();
		LinkedHashMap<String,String> dict = new LinkedHashMap<String, String>();
		
		try {
			File file = new File(dictFilePath);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line="";
			while ((line=br.readLine())!=null) {
				String[] cells=line.split(",");
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
			if (cid!=null) {
				cids.add(cid);
				counter++;
			} else {
				boolean foundCID = false;
				try {
					String inchikey = records.get(i).INCHIKEY;
					String url = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/"+inchikey+"/cids/TXT";
					String cidsTxt = FileUtilities.getText_UTF8(url);
					if (cidsTxt!=null) {
						cids.add(cidsTxt.split("\r\n")[0]);
						foundCID = true;
						counter++;
					}
					Thread.sleep(200);
				} catch (Exception ex) { ex.printStackTrace(); }
				if (!foundCID) {
					try {
						String smiles = records.get(i).SMILES;
						String url = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/smiles/"+smiles+"/cids/TXT";
						String cidsTxt = FileUtilities.getText_UTF8(url);
						if (cidsTxt!=null) {
							cids.add(cidsTxt.split("\r\n")[0]);
							foundCID = true;
							counter++;
						}
						Thread.sleep(200);
					} catch (Exception ex) { ex.printStackTrace(); }
				}
			}
			if (counter % 100 == 0) {
				System.out.println("Found "+counter+" CIDs");
			}
		}
		System.out.println("Found "+counter+" CIDs");
		return cids;
	}
	
	private static void downloadJSONsToDatabase(Vector<String> cids, boolean startFresh) {
		ParsePubChem p = new ParsePubChem();
		String databaseName = p.sourceName+"_raw_json.db";
		String tableName = p.sourceName;
		String databasePath = p.databaseFolder+File.separator+databaseName;
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		java.sql.Connection conn=CreateGHS_Database.createDatabaseTable(databasePath, tableName, RawDataRecordPubChem.fieldNames, startFresh);
		
		try {
			int counterSuccess = 0;
			int counterTotal = 0;
			int counterNew = 0;
			int counterMissingExpData = 0;
			for (String cid:cids) {
				String experimentalURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug_view/data/compound/"+cid+"/JSON?heading=Experimental+Properties";
				String idURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/property/IUPACName,CanonicalSMILES/JSON?cid="+cid;
				String casURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug_view/data/compound/"+cid+"/JSON?heading=CAS";
				String synonymURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/"+cid+"/synonyms/TXT";
				
				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
				Date date = new Date();  
				String strDate=formatter.format(date);
				
				counterTotal++;
				RawDataRecordPubChem rec=new RawDataRecordPubChem(strDate, cid, "", "", "", "");
				boolean haveRecord=rec.haveRecordInDatabase(databasePath,tableName,conn);
				if (!haveRecord || startFresh) {
					boolean keepLooking = true;
					try {
						rec.experimental=FileUtilities.getText_UTF8(experimentalURL).replaceAll("'", "\'");
					} catch (Exception ex) { 
						counterMissingExpData++;
						keepLooking = false;
					}
					Thread.sleep(200);
					if (keepLooking) {
						try {
							rec.cas=FileUtilities.getText_UTF8(casURL).replaceAll("'", "\'");
						} catch (Exception ex) { }
						Thread.sleep(200);
						try {
							rec.identifiers=FileUtilities.getText_UTF8(idURL).replaceAll("'", "\'");
						} catch (Exception ex) { }
						Thread.sleep(200);
						try {
							rec.synonyms=FileUtilities.getText_UTF8(synonymURL).replaceAll("'", "\'");
						} catch (Exception ex) { }
						Thread.sleep(200);
					}
					counterNew++;
					if (rec.experimental!=null && !rec.experimental.isBlank()) {
						rec.addRecordToDatabase(tableName, conn);
						counterSuccess++;
					}
					if (counterTotal % 100==0) {
						System.out.println("Attempted: "+counterTotal);
						System.out.println("New: "+counterNew);
						System.out.println("Succeeded: "+counterSuccess);
						System.out.println("Failed - no experimental properties: "+counterMissingExpData);
						System.out.println("~~~~~~~~~~");
					}
				}
			}
			System.out.println("Attempted: "+counterTotal);
			System.out.println("New: "+counterNew);
			System.out.println("Succeeded: "+counterSuccess);
			System.out.println("Failed - no experimental properties: "+counterMissingExpData);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	protected static Vector<RecordPubChem> parseJSONsInDatabase() {
		String databaseFolder = "Data"+File.separator+"Experimental"+ File.separator + sourceName;
		String databasePath = databaseFolder+File.separator+sourceName+"_raw_json.db";
		Vector<RecordPubChem> records = new Vector<>();
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		try {
			Statement stat = MySQL_DB.getStatement(databasePath);
			ResultSet rs = MySQL_DB.getAllRecords(stat,sourceName);
			while (rs.next()) {
				String date = rs.getString("date");
				String experimental = rs.getString("experimental");
				Data experimentalData = gson.fromJson(experimental,Data.class);
				RecordPubChem pcr = new RecordPubChem();
				pcr.date_accessed = date.substring(0,date.indexOf(" "));
				pcr.cid = experimentalData.record.recordNumber;
				List<Section> experimentalProperties = experimentalData.record.section.get(0).section.get(0).section;
				pcr.getExperimentalData(experimentalProperties);
				String identifiers = rs.getString("identifiers");
				IdentifierData identifierData = gson.fromJson(identifiers, IdentifierData.class);
				Property identifierProperty = identifierData.propertyTable.properties.get(0);
				pcr.iupacName = identifierProperty.iupacName;
				pcr.smiles = identifierProperty.canonicalSMILES;
				String cas = rs.getString("cas");
				Data casData = gson.fromJson(cas, Data.class);
				if (casData!=null) {
					List<Information> casInfo = casData.record.section.get(0).section.get(0).section.get(0).information;
					for (Information c:casInfo) {
						String newCAS = c.value.stringWithMarkup.get(0).string;
						if (!pcr.cas.contains(newCAS)) { pcr.cas.add(newCAS); }
					}
				}
				pcr.synonyms = rs.getString("synonyms").replaceAll("\r\n","|");
				records.add(pcr);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}
	
	private void getExperimentalData(List<Section> properties) {
		for (Section prop:properties) {
			String heading = prop.tocHeading;
			List<Information> info = prop.information;
			if (heading.equals("Physical Description") || heading.equals("Color/Form")) {
				for (Information i:info) { physicalDescription.add(getStringFromInformation(i)); }
			} else if (heading.equals("Density")) {
				for (Information i:info) { density.add(getStringFromInformation(i)); }
			} else if (heading.equals("Melting Point")) {
				for (Information i:info) { meltingPoint.add(getStringFromInformation(i)); }
			} else if (heading.equals("Boiling Point")) {
				for (Information i:info) { boilingPoint.add(getStringFromInformation(i)); }
			} else if (heading.equals("Flash Point")) {
				for (Information i:info) { flashPoint.add(getStringFromInformation(i)); }
			} else if (heading.equals("Solubility")) {
				for (Information i:info) { solubility.add(getStringFromInformation(i)); }
			} else if (heading.equals("Vapor Pressure")) {
				for (Information i:info) { vaporPressure.add(getStringFromInformation(i)); }
			} else if (heading.equals("Henrys Law Constant")) {
				for (Information i:info) { henrysLawConstant.add(getStringFromInformation(i)); }
			} else if (heading.equals("LogP")) {
				for (Information i:info) { logP.add(getStringFromInformation(i)); }
			} else if (heading.equals("pKa")) {
				for (Information i:info) { pKa.add(getStringFromInformation(i)); }
			}
		}
	}
	
	private String getStringFromInformation(Information i) {
		List<StringWithMarkup> strings = i.value.stringWithMarkup;
		if (strings!= null) {
			return strings.get(0).string;
		} else {
			return null;
		}
	}
	
	public static void main(String[] args) {
		Vector<RecordDashboard> drs = Parse.getDashboardRecordsFromExcel("Data"+"/PFASSTRUCT.xls");
		Vector<String> cids = getCIDsFromDashboardRecords(drs,"Data"+"/CIDDICT.csv",1,8164);
		downloadJSONsToDatabase(cids,false);
	}
}
