package gov.epa.exp_data_gathering.parse.MetabolomicsWorkbench;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import gov.epa.database.SQLite_CreateTable;
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
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
		int countPubChemFullDownloads = 0;
		int countPubChemPartialDownloads = 0;
		int countPubChemFailedDownloads = 0;
		for (int i = start; i < end; i++) {
			RecordMetaboliteDatabase rmd = recordsMetaboliteDatabase.get(i);
			String regno = rmd.regno;
			if (hmRecordsRefMetByRegno.containsKey(regno)) {
				RecordRefMet rrm = hmRecordsRefMetByRegno.get(regno);
				if ((rrm.systematicName==null || rrm.systematicName.isBlank()) && !(rmd.systematicName==null || rmd.systematicName.isBlank())) {
					rrm.systematicName = rmd.systematicName;
				} else if (rrm.systematicName!=null && rrm.systematicName.isBlank()) {
					rrm.systematicName = null;
				}
				joinedRecords.add(rrm);
			} else {
				RecordRefMet rrm = new RecordRefMet(rmd);
				if (rrm.pubChemCID==null || rrm.pubChemCID.isBlank() || rrm.pubChemCID.equals("-")) {
					rrm.pubChemCID = null;
					joinedRecords.add(rrm);
				} else {
					int success = downloadAndAddMissingDataFromPubChem(rrm);
					joinedRecords.add(rrm);
					if (success==2) {
						countPubChemFullDownloads++;
					} else if (success==1) {
						countPubChemPartialDownloads++;
					} else {
						countPubChemFailedDownloads++;
					}
				}
			}
			count++;
			if (count % 1000==0) {
				System.out.println("Added "+count+" records; full data for "+countPubChemFullDownloads+" records retrieved from PubChem; partial data for "+
						countPubChemPartialDownloads+" records retrieved from PubChem; "+countPubChemFailedDownloads+" records with no data in any source.");
			}
		}
		if (count % 1000 != 0) {
			System.out.println("Added "+count+" records; full data for "+countPubChemFullDownloads+" records retrieved from PubChem; partial data for "+
					countPubChemPartialDownloads+" records retrieved from PubChem; "+countPubChemFailedDownloads+" records with no data in any source.");
		}
	}
	
	private int downloadAndAddMissingDataFromPubChem(RecordRefMet rrm) {
		int success = 2;
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
				success--;
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
				success--;
			}
			
			long end2 = System.currentTimeMillis();
			if (end2-end < 200) {
				Thread.sleep(200-(end2-end));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			success = 0;
		}
		
		return success;
	}
	
	private void batchAddRecordsToDatabase(String databasePath,boolean startFresh) {
		String tableName = "MetabolomicsWorkbench";
		Connection conn= SQLite_Utilities.getConnection(databasePath);
		HashSet<String> hsRegno = new HashSet<String>();
		try {
			System.out.println("Writing to database...");
			conn.setAutoCommit(true);
			if (startFresh) {
				System.out.println("Creating "+tableName+" table...");
				Statement stat1 = SQLite_Utilities.getStatement(conn);
				stat1.executeUpdate("drop table if exists "+tableName+";");
				stat1.close();
				Statement stat2 = SQLite_Utilities.getStatement(conn);
				stat2.executeUpdate("VACUUM;");//compress db now that have deleted the table
				stat2.close();
				Statement stat3 = SQLite_Utilities.getStatement(conn);
				SQLite_CreateTable.create_table(stat3, tableName, RecordRefMet.fieldNames);
				Statement stat4 = SQLite_Utilities.getStatement(conn);
				String sqlAddIndex="CREATE INDEX idx_regno ON "+tableName+" (regno)";
				stat4.executeUpdate(sqlAddIndex);
			} else {
				ResultSet rs = SQLite_GetRecords.getAllRecords(SQLite_Utilities.getStatement(conn), tableName);
				try {
					while (rs.next()) {
						hsRegno.add(rs.getString("regno"));
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		String s = "insert into " + tableName + " values (";

		for (int i = 1; i <= RecordRefMet.fieldNames.length; i++) {
			s += "?";
			if (i < RecordRefMet.fieldNames.length)
				s += ",";
		}
		s += ");";

		int counter = 0;
		try {
			PreparedStatement prep = conn.prepareStatement(s);
			
			for (RecordRefMet rec:joinedRecords) {
				if (!hsRegno.add(rec.regno)) { continue; }
				
				String[] list = rec.setSafeValuesForDatabase();
	
				if (list.length!=RecordRefMet.fieldNames.length) {
					System.out.println("Wrong number of values: "+list[0]);
					continue;
				}
								
				for (int i = 0; i < list.length; i++) {
					if (list[i]!=null && !list[i].isBlank()) {
						prep.setString(i + 1, list[i]);
					} else {
						prep.setString(i + 1, null);
					}
				}
				
				counter++;
				
				try {
					prep.addBatch();
				} catch (Exception ex) {
					System.out.println("Failed to add: "+String.join(",", list));
				}
				
				if (counter % 1000 == 0) {
					prep.executeBatch();
					System.out.println("Wrote "+counter+" records to database...");
				}
			}
	
			prep.executeBatch(); // do what's left
			
			conn.setAutoCommit(true);
			
			System.out.println("Wrote "+joinedRecords.size()+" records to database at "+databasePath+".");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void joinAndWriteRecordsToDatabase(int start,int end,boolean startFresh,String databasePath) {
		joinRecords(start,end);
		batchAddRecordsToDatabase(databasePath, startFresh);
	}
	
	public void addCommonNamesToExistingDatabase(String databasePath) {
		String tableName = "MetabolomicsWorkbench";
		String columnName = "commonName";
		Connection conn= SQLite_Utilities.getConnection(databasePath);
		int countTruncated = 0;
		int countAll = 0;
		try {
			Statement stat = conn.createStatement();
			for (RecordMetaboliteDatabase rmd:recordsMetaboliteDatabase) {
				String safeCommonName = (rmd.commonName==null || rmd.commonName.isBlank()) ? null : rmd.commonName.replaceAll("'", "''").replaceAll(";", "\\;");
				if (safeCommonName==null) { continue; }
				
				if (safeCommonName.contains("...")) {
					countTruncated++;
				}
				
				String update = "UPDATE " + tableName + " SET " + columnName + " = '" + safeCommonName + "' WHERE regno = '" + rmd.regno + "';";
				try {
					stat.execute(update);
					countAll++;
					if (countAll % 1000==0) { System.out.println("Made "+countAll+" updates..."); }
				} catch (Exception ex1) {
					System.out.println(update);
					ex1.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Successfully updated "+countAll+" records with common names; "+countTruncated+" names may be truncated.");
	}
	
	public static void fixTruncationInExistingDatabase(String databasePath) {
		String tableName = "MetabolomicsWorkbench";
		Connection conn= SQLite_Utilities.getConnection(databasePath);
		try (Statement stat = conn.createStatement()) {
			String query = "select regno, commonName, systematicName from " + tableName + " where commonName like '%...%' or systematicName like '%...%';";
			ResultSet rs = stat.executeQuery(query);
			
			int count = 0;
			while (rs.next()) {
				String regno = rs.getString("regno");
				String commonName = null;
				String systematicName = null;
				
				String url = "https://www.metabolomicsworkbench.org/data/StructureData.php?RegNo=" + regno;
				Document doc = Jsoup.connect(url).get();
				Element table = doc.select("table.datatable2").first();
				Elements rows = table.select("tr");
				for (Element row:rows) {
					Elements cells = row.select("td");
					String headerText = cells.first().text();
					String dataText = cells.get(1).text();
					if (headerText.contains("Common Name")) {
						commonName = dataText;
					} else if (headerText.contains("Systematic Name")) {
						systematicName = dataText;
					}
				}
				
				String safeCommonName = (commonName==null || commonName.isBlank()) ? null : commonName.replaceAll("'", "''").replaceAll(";", "\\;");
				String safeSystematicName = (systematicName==null || systematicName.isBlank()) ? null : systematicName.replaceAll("'", "''").replaceAll(";", "\\;");
				
				if (safeCommonName!=null) {
					String updateCommonName = "UPDATE " + tableName + " SET commonName = '" + safeCommonName + "' WHERE regno = '" + regno + "';";
					try (Statement stat2 = conn.createStatement()) {
						stat2.execute(updateCommonName);
					} catch (Exception ex1) {
						ex1.printStackTrace();
						System.out.println(updateCommonName);
					}
				}
				
				if (safeSystematicName!=null) {
					String updateSystematicName = "UPDATE " + tableName + " SET systematicName = '" + safeSystematicName + "' WHERE regno = '" + regno + "';";
					try (Statement stat3 = conn.createStatement()) {
						stat3.execute(updateSystematicName);
					} catch (Exception ex1) {
						ex1.printStackTrace();
						System.out.println(updateSystematicName);
					}
				}
				
				count++;
				if (count % 1000 == 0) { System.out.println("Made " + count + " updates..."); }
			}
			System.out.println("Updated " + count + " records!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
//		String readDatabasePath = "Data\\Experimental\\MetabolomicsWorkbench\\MetabolomicsWorkbenchRaw.db";
//		List<RecordMetaboliteDatabase> recordsMD = RecordMetaboliteDatabase.parseMetaboliteDatabaseTablesInDatabase(readDatabasePath);
//		List<RecordRefMet> recordsRM = RecordRefMet.parseRefMetPagesInDatabase(readDatabasePath);
//		MetabolomicsWorkbenchRecordsJoiner joiner = new MetabolomicsWorkbenchRecordsJoiner(recordsMD,recordsRM);
		String writeDatabasePath = "Data\\Experimental\\MetabolomicsWorkbench\\MetabolomicsWorkbenchRecords_TruncationFixed.db";
		fixTruncationInExistingDatabase(writeDatabasePath);
	}
}
