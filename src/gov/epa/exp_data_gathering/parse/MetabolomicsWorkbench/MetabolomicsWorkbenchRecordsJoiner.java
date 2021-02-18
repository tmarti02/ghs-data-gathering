package gov.epa.exp_data_gathering.parse.MetabolomicsWorkbench;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import gov.epa.database.SQLite_CreateTable;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.ghs_data_gathering.Utilities.FileUtilities;

public class MetabolomicsWorkbenchRecordsJoiner {
	List<RecordMetaboliteDatabase> recordsMetaboliteDatabase;
	List<RecordRefMet> recordsRefMet;
	List<RecordRefMet> joinedRecords;
	Gson gson;
	
	public static class PubChemData {
		@SerializedName("PropertyTable")
		public PropertyTable propertyTable;
	}
	
	public static class PropertyTable {
		@SerializedName("Properties")
		public List<Property> properties = null;
	}
	
	public static class Property {
		@SerializedName("CID")
		public Integer cid;
		@SerializedName("CanonicalSMILES")
		public String canonicalSMILES;
		@SerializedName("InChI")
		public String inChI;
		@SerializedName("InChIKey")
		public String inChIKey;
	}
	
	MetabolomicsWorkbenchRecordsJoiner(List<RecordMetaboliteDatabase> recordsMetaboliteDatabase, List<RecordRefMet> recordsRefMet) {
		joinedRecords = new ArrayList<RecordRefMet>();
		gson = new GsonBuilder().create();
		this.recordsMetaboliteDatabase = recordsMetaboliteDatabase;
		this.recordsRefMet = recordsRefMet;
	}
	
	private void joinRecords() {
		int metaboliteDatabaseSize = recordsMetaboliteDatabase.size();
		int refMetSize = recordsRefMet.size();
		System.out.println("Found "+metaboliteDatabaseSize+" records from Metabolite Database, matched to "+refMetSize+" records from RefMet.");
		
		HashMap<String,RecordRefMet> hmRecordsRefMetByRegno = new HashMap<String,RecordRefMet>();
		for (RecordRefMet rrm:recordsRefMet) {
			hmRecordsRefMetByRegno.put(rrm.regno,rrm);
		}
		
		int countPubChemDownloads = 0;
		List<String> missingMolFileURLs = new ArrayList<String>();
		for (RecordMetaboliteDatabase rmd:recordsMetaboliteDatabase) {
			String regno = rmd.regno;
			if (hmRecordsRefMetByRegno.containsKey(regno)) {
				RecordRefMet rrm = hmRecordsRefMetByRegno.get(regno);
				if ((rrm.systematicName==null || rrm.systematicName.isBlank()) && !(rmd.systematicName==null || rmd.systematicName.isBlank())) {
					rrm.systematicName = rmd.systematicName;
				}
				joinedRecords.add(rrm);
			} else {
				RecordRefMet rrm = new RecordRefMet(rmd);
				downloadAndAddMissingDataFromPubChem(rrm);
				joinedRecords.add(rrm);
				countPubChemDownloads++;
				missingMolFileURLs.add(rmd.url);
			}
		}
		System.out.println("Retrieved missing data for "+countPubChemDownloads+" records from PubChem.");
		writeMissingMolFileURLsToTXT(missingMolFileURLs);
	}
	
	private void downloadAndAddMissingDataFromPubChem(RecordRefMet rrm) {
		try {
			String pubChemURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/"+rrm.pubChemCID+"/property/CanonicalSMILES,InChI,InChIKey/JSON";
			String pubChemJSON = FileUtilities.getText_UTF8(pubChemURL);
			PubChemData pubChemData = gson.fromJson(pubChemJSON, PubChemData.class);
			Property identifiers = pubChemData.propertyTable.properties.get(0);
			
			rrm.smiles = identifiers.canonicalSMILES;
			rrm.inchi = identifiers.inChI;
			rrm.inchiKey = identifiers.inChIKey;
			
			Thread.sleep(200);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void writeMissingMolFileURLsToTXT(List<String> urls) {
		String filePath = "Data\\Experimental\\MetabolomicsWorkbench\\MissingMolFileURLs.txt";
		File file = new File(filePath);
		if(!file.getParentFile().exists()) { file.getParentFile().mkdirs(); }
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
			for (String url:urls) {
				bw.write(url+"\r\n");
			}
			bw.close();
			System.out.println("Wrote missing molfile URLs to "+filePath+".");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void joinAndWriteRecordsToDatabase(String databasePath) {
		joinRecords();
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		String tableName = "MetabolomicsWorkbench";
		java.sql.Connection conn=SQLite_CreateTable.create_table(databasePath, tableName, RecordRefMet.fieldNames, true);
		
		for (RecordRefMet rec:joinedRecords) {
			rec.addRecordToDatabase(tableName, conn);
		}
	}
	
	public static void main(String[] args) {
		String databasePath = "Data\\Experimental\\MetabolomicsWorkbench\\MetabolomicsWorkbenchTest.db";
		List<RecordMetaboliteDatabase> recordsMD = RecordMetaboliteDatabase.parseMetaboliteDatabaseTablesInDatabase(databasePath);
		List<RecordRefMet> recordsRM = RecordRefMet.parseRefMetPagesInDatabase(databasePath);
		MetabolomicsWorkbenchRecordsJoiner joiner = new MetabolomicsWorkbenchRecordsJoiner(recordsMD,recordsRM);
		joiner.joinAndWriteRecordsToDatabase("Data\\Experimental\\MetabolomicsWorkbench\\MetabolomicsWorkbenchFinalTest.db");
	}
}
