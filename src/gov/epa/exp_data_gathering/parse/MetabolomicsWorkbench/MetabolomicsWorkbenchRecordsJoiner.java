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
	
	private void joinRecords(int start, int end) {
		int metaboliteDatabaseSize = recordsMetaboliteDatabase.size();
		int refMetSize = recordsRefMet.size();
		System.out.println("Found "+metaboliteDatabaseSize+" records from Metabolite Database, matched to "+refMetSize+" records from RefMet.");
		
		HashMap<String,RecordRefMet> hmRecordsRefMetByRegno = new HashMap<String,RecordRefMet>();
		for (RecordRefMet rrm:recordsRefMet) {
			hmRecordsRefMetByRegno.put(rrm.regno,rrm);
		}
		
		int count = 0;
		int countPubChemDownloads = 0;
		for (int i = start; i < end; i++) {
			RecordMetaboliteDatabase rmd = recordsMetaboliteDatabase.get(i);
			String regno = rmd.regno;
			if (hmRecordsRefMetByRegno.containsKey(regno)) {
				RecordRefMet rrm = hmRecordsRefMetByRegno.get(regno);
				if ((rrm.systematicName==null || rrm.systematicName.isBlank()) && !(rmd.systematicName==null || rmd.systematicName.isBlank())) {
					rrm.systematicName = rmd.systematicName;
				}
				joinedRecords.add(rrm);
			} else {
				RecordRefMet rrm = new RecordRefMet(rmd);
				boolean success = downloadAndAddMissingDataFromPubChem(rrm);
				joinedRecords.add(rrm);
				if (success) {
					countPubChemDownloads++;
				}
			}
			count++;
			if (count % 1000==0) {
				System.out.println("Added "+count+" records; missing data for "+countPubChemDownloads+" records retrieved from PubChem.");
			}
		}
		if (count % 1000 != 0) {
			System.out.println("Added "+count+" records; missing data for "+countPubChemDownloads+" records retrieved from PubChem.");
		}
	}
	
	private boolean downloadAndAddMissingDataFromPubChem(RecordRefMet rrm) {
		boolean success = true;
		try {
			String pubChemDataURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/"+rrm.pubChemCID+"/property/CanonicalSMILES,InChI,InChIKey/JSON";
			long start = System.currentTimeMillis();
			String pubChemJSON = FileUtilities.getText_UTF8(pubChemDataURL);
			PubChemData pubChemData = gson.fromJson(pubChemJSON, PubChemData.class);
			if (pubChemData!=null) {
				Property property = pubChemData.propertyTable.properties.get(0);
				rrm.smiles = property.canonicalSMILES;
				rrm.inchi = property.inChI;
				rrm.inchiKey = property.inChIKey;
			} else {
				success = false;
			}
			
			long end = System.currentTimeMillis();
			if (end-start < 200) {
				Thread.sleep(200-(end-start));
			}
			
			String pubChemSDFURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/"+rrm.pubChemCID+"/SDF";
			String pubChemSDF = FileUtilities.getText_UTF8(pubChemSDFURL);
			if (pubChemSDF!=null) {
				String pubChemMolFile = pubChemSDF.substring(0,pubChemSDF.indexOf("END")+3);
				rrm.molFile = pubChemMolFile;
			} else {
				success = false;
			}
			
			long end2 = System.currentTimeMillis();
			if (end2-end < 200) {
				Thread.sleep(200-(end2-end));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			success = false;
		}
		
		return success;
	}
	
	public void joinAndWriteRecordsToDatabase(int start,int end,boolean startFresh,String databasePath) {
		joinRecords(start,end);
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		String tableName = "MetabolomicsWorkbench";
		java.sql.Connection conn=SQLite_CreateTable.create_table(databasePath, tableName, RecordRefMet.fieldNames, startFresh);
		
		for (RecordRefMet rec:joinedRecords) {
			rec.addRecordToDatabase(tableName, conn);
		}
	}
	
	public static void main(String[] args) {
		String readDatabasePath = "Data\\Experimental\\MetabolomicsWorkbench\\MetabolomicsWorkbench.db";
		List<RecordMetaboliteDatabase> recordsMD = RecordMetaboliteDatabase.parseMetaboliteDatabaseTablesInDatabase(readDatabasePath);
		List<RecordRefMet> recordsRM = RecordRefMet.parseRefMetPagesInDatabase(readDatabasePath);
		MetabolomicsWorkbenchRecordsJoiner joiner = new MetabolomicsWorkbenchRecordsJoiner(recordsMD,recordsRM);
		String writeDatabasePath = "Data\\Experimental\\MetabolomicsWorkbench\\MetabolomicsWorkbenchRecords.db";
		joiner.joinAndWriteRecordsToDatabase(1000,10000,false,writeDatabasePath); // Left off at 1000 on 2/18
	}
}
