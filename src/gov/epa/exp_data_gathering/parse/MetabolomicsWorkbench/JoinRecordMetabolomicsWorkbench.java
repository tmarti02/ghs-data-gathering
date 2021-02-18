package gov.epa.exp_data_gathering.parse.MetabolomicsWorkbench;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import gov.epa.database.SQLite_CreateTable;
import gov.epa.ghs_data_gathering.Utilities.FileUtilities;

public class JoinRecordMetabolomicsWorkbench {
	List<RecordRefMet> joinedRecords;
	
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
	
	JoinRecordMetabolomicsWorkbench(List<RecordMetaboliteDatabase> recordsMetaboliteDatabase, List<RecordRefMet> recordsRefMet) {
		joinedRecords = new ArrayList<RecordRefMet>();
		Gson gson = new GsonBuilder().create();
		
		int metaboliteDatabaseSize = recordsMetaboliteDatabase.size();
		int refMetSize = recordsRefMet.size();
		System.out.println("Found "+metaboliteDatabaseSize+" records from Metabolite Database, matched to "+refMetSize+" records from RefMet.");
		
		HashMap<String,RecordRefMet> hmRecordsRefMetByRegno = new HashMap<String,RecordRefMet>();
		for (RecordRefMet rrm:recordsRefMet) {
			hmRecordsRefMetByRegno.put(rrm.regno,rrm);
		}
		
		int countPubChemDownloads = 0;
		for (RecordMetaboliteDatabase rmd:recordsMetaboliteDatabase) {
			String regno = rmd.regno;
			if (hmRecordsRefMetByRegno.containsKey(regno)) {
				joinedRecords.add(hmRecordsRefMetByRegno.get(regno));
			} else {
				try {
					RecordRefMet rrm = new RecordRefMet(rmd);
					String pubChemURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/"+rmd.pubChemCID+"/property/CanonicalSMILES,InChI,InChIKey/JSON";
					String pubChemJSON = FileUtilities.getText_UTF8(pubChemURL);
					PubChemData pubChemData = gson.fromJson(pubChemJSON, PubChemData.class);
					Property identifiers = pubChemData.propertyTable.properties.get(0);
					
					rrm.smiles = identifiers.canonicalSMILES;
					rrm.inchi = identifiers.inChI;
					rrm.inchiKey = identifiers.inChIKey;
					
					joinedRecords.add(rrm);
					countPubChemDownloads++;
					
					Thread.sleep(200);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		System.out.println("Retrieved missing data for "+countPubChemDownloads+" records from PubChem.");
	}
	
	private void writeJoinedRecordsToDatabase(String databasePath) {
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
		JoinRecordMetabolomicsWorkbench joiner = new JoinRecordMetabolomicsWorkbench(recordsMD,recordsRM);
		joiner.writeJoinedRecordsToDatabase("Data\\Experimental\\MetabolomicsWorkbench\\MetabolomicsWorkbenchFinalTest.db");
	}
}
