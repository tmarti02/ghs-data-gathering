package gov.epa.exp_data_gathering.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.AADashboard;
import gov.epa.api.ExperimentalConstants;
import gov.epa.api.RawDataRecord;
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
	String cas;
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
	
	static final String sourceName=ExperimentalConstants.strSourcePubChem;
	
	private RecordPubChem() {
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
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		for (int i = start; i < end; i++) {
			String dtxsid = records.get(i).DTXSID;
			String cid = dict.get(dtxsid);
			if (cid!=null) { cids.add(cid);
			} else { System.out.println("CID not found for "+dtxsid); }
		}
		
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
			int counter = 1;
			for (String cid:cids) {
				String experimentalURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug_view/data/compound/"+cid+"/JSON?heading=Experimental+Properties";
				String idURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/property/IUPACName,CanonicalSMILES/JSON?cid="+cid;
				String casURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug_view/data/compound/"+cid+"/JSON?heading=CAS";
				String synonymURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/"+cid+"/synonyms/TXT";
				
				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
				Date date = new Date();  
				String strDate=formatter.format(date);
				
				RawDataRecordPubChem rec=new RawDataRecordPubChem(strDate, cid, "", "", "", "");
				boolean haveRecord=rec.haveRecordInDatabase(databasePath,tableName,conn);
				if (!haveRecord || startFresh) {
					try {
						rec.experimental=FileUtilities.getText_UTF8(experimentalURL).replace("'", "''"); //single quotes mess with the SQL insert later
						Thread.sleep(200);
						rec.identifiers=FileUtilities.getText_UTF8(idURL).replace("'", "''");
						Thread.sleep(200);
						rec.cas=FileUtilities.getText_UTF8(casURL).replace("'", "''");
						Thread.sleep(200);
						rec.synonyms=FileUtilities.getText_UTF8(synonymURL).replace("'", "''");
						Thread.sleep(200);
						if (rec.experimental!=null) { 
							rec.addRecordToDatabase(tableName, conn);
							counter++;
						}
						if (counter % 100==0) { System.out.println("Downloaded "+counter+" pages"); }
					} catch (Exception ex) {
						System.out.println("Failed to download data for CID "+cid);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static Vector<RecordPubChem> parseJSONsInDatabase() {
		String databaseFolder = AADashboard.dataFolder+File.separator+"Experimental"+ File.separator + sourceName + File.separator + "databases";
		String databasePath = databaseFolder+File.separator+sourceName+"_raw_json.db";
		Vector<RecordPubChem> records = new Vector<>();
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		try {
			Statement stat = MySQL_DB.getStatement(databasePath);
			ResultSet rsExperimental = MySQL_DB.getAllRecords(stat,"ExperimentalProperties");
			while (rsExperimental.next()) {
				String json = rsExperimental.getString("content");
				Data data = gson.fromJson(json,Data.class);
				RecordPubChem pcr = new RecordPubChem();
				pcr.cid = data.record.recordNumber;
				List<Section> properties = data.record.section.get(0).section.get(0).section;
				for (Section prop:properties) {
					String heading = prop.tocHeading;
					List<Information> info = prop.information;
					if (heading.equals("Physical Description") || heading.equals("Color/Form")) {
						for (Information i:info) { pcr.physicalDescription.add(i.value.stringWithMarkup.get(0).string); }
					} else if (heading.equals("Density")) {
						for (Information i:info) { pcr.density.add(i.value.stringWithMarkup.get(0).string); }
					} else if (heading.equals("Melting Point")) {
						for (Information i:info) { pcr.meltingPoint.add(i.value.stringWithMarkup.get(0).string); }
					} else if (heading.equals("Boiling Point")) {
						for (Information i:info) { pcr.boilingPoint.add(i.value.stringWithMarkup.get(0).string); }
					} else if (heading.equals("Flash Point")) {
						for (Information i:info) { pcr.flashPoint.add(i.value.stringWithMarkup.get(0).string); }
					} else if (heading.equals("Solubility")) {
						for (Information i:info) { pcr.solubility.add(i.value.stringWithMarkup.get(0).string); }
					} else if (heading.equals("Vapor Pressure")) {
						for (Information i:info) { pcr.vaporPressure.add(i.value.stringWithMarkup.get(0).string); }
					} else if (heading.equals("Henrys Law Constant")) {
						for (Information i:info) { pcr.henrysLawConstant.add(i.value.stringWithMarkup.get(0).string); }
					} else if (heading.equals("LogP")) {
						for (Information i:info) { pcr.logP.add(i.value.stringWithMarkup.get(0).string); }
					} else if (heading.equals("pKa")) {
						for (Information i:info) { pcr.pKa.add(i.value.stringWithMarkup.get(0).string); }
					}
				}
				records.add(pcr);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}
	
	private void getSynonyms() {
		String baseURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";
		String tailURL = "/synonyms/TXT";
		String url = baseURL+cid+tailURL;
		synonyms = FileUtilities.getText_UTF8(url).replace("\r\n","|");
	}
	
	public static void main(String[] args) {
		Vector<RecordDashboard> drs = Parse.getDashboardRecordsFromExcel(AADashboard.dataFolder+"/PFASSTRUCT.xls");
		Vector<String> cids = getCIDsFromDashboardRecords(drs,AADashboard.dataFolder+"/CIDDICT.csv",1,100);
		downloadJSONsToDatabase(cids,true);
//		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
//		Vector<RecordPubChem> pcrs = parseJSONsInDatabase();
//		for (RecordPubChem pcr:pcrs) {
//			System.out.println(gson.toJson(pcr));
//		}
	}
}
