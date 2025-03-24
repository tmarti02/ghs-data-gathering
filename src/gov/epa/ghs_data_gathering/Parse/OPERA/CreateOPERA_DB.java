package gov.epa.ghs_data_gathering.Parse.OPERA;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import gov.epa.database.SQLite_CreateTable;
import gov.epa.database.SqlUtilities;
import gov.epa.ghs_data_gathering.Database.MySQL_DB;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

public class CreateOPERA_DB {

	public static void getOPERA_Results(String filePath,String dbPath){
		FileInputStream fis = null;
		ZipInputStream zipIs = null;
		ZipEntry zEntry = null;
		try {
			fis = new FileInputStream(filePath);
			ZipFile zip = new ZipFile(new File(filePath));
			zipIs = new ZipInputStream(new BufferedInputStream(fis));
			while((zEntry = zipIs.getNextEntry()) != null){
				if (zEntry.getName().contains(".gz")) {
					System.out.println(zEntry.getName());
					InputStream is = new GZIPInputStream(zip.getInputStream(zEntry));
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br=new BufferedReader(isr);
					getRecordsFromTextFile(br, dbPath, ",", "Results", "DSSTOX_COMPOUND_ID");
					br.close();
				}
			}
			zipIs.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void getOPERA_Results2(String filePath,String CID){
		FileInputStream fis = null;
		ZipInputStream zipIs = null;
		ZipEntry zEntry = null;
		try {
			fis = new FileInputStream(filePath);
			ZipFile zip = new ZipFile(new File(filePath));
			zipIs = new ZipInputStream(new BufferedInputStream(fis));
			while((zEntry = zipIs.getNextEntry()) != null){
				if (zEntry.getName().contains(".gz")) {
//					System.out.println(zEntry.getName());
					InputStream is = new GZIPInputStream(zip.getInputStream(zEntry));
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br=new BufferedReader(isr);
					getRecordsFromTextFile2(zEntry.getName(), br, ",", CID);
					br.close();
				}
			}
			zipIs.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void getChemicalInfo(String filePath,String dbPath,String zippath){
		try {
			ZipFile zip = new ZipFile(new File(filePath));
			ZipEntry zipEntry=zip.getEntry(zippath);
			ZipEntry zEntry=null;
			ZipInputStream is = new ZipInputStream(zip.getInputStream(zipEntry));			
			while((zEntry = is.getNextEntry()) != null){
				//            	System.out.println(zEntry.getName());
				if (zEntry.getName().equals("DSSTox_082021_IDs.csv")) {
					//            		InputStream is2 = new ZipInputStream(is);
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br=new BufferedReader(isr);
					getRecordsFromTextFile(br, dbPath, ",", "IDs", "DSSTOX_COMPOUND_ID2");                    
				}
			}
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void getStructureInfo(String filePath,String dbPath,String zippath){
		try {
			ZipFile zip = new ZipFile(new File(filePath));
			ZipEntry zipEntry=zip.getEntry(zippath);
			ZipEntry zEntry=null;
			ZipInputStream is = new ZipInputStream(zip.getInputStream(zipEntry));			

			while((zEntry = is.getNextEntry()) != null){
				if (zEntry.getName().equals("DSSTox_082021_Structures.csv")) {
					//            		InputStream is2 = new ZipInputStream(is);
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br=new BufferedReader(isr);
					getRecordsFromTextFile(br, dbPath, ",", "Structure", "DSSTOX_COMPOUND_ID3");                    
				}
			}
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 *  Reads text file and stores in db. Adds autonumber id field 
	 * @param filepath
	 * @return
	 */
	public static void getRecordsFromTextFile(BufferedReader br,String dbPath, String del,String tableName,String indexName) {

		try {

			Connection conn= MySQL_DB.getConnection(dbPath);
			Statement stat = MySQL_DB.getStatement(conn);
			conn.setAutoCommit(true);

			String header = br.readLine();
			header=header.replace(" ","_");
//			System.out.println(header);

			String [] fieldNames=header.split(",");

			MySQL_DB.create_table_key_with_duplicates_with_auto_id(stat, tableName, fieldNames,"XX");//need unique values in the table for key field for this to work!

			conn.setAutoCommit(false);

			String s=SQLite_CreateTable.create_sql_insert_with_field_names(fieldNames, tableName);

			int counter = 0;

			PreparedStatement prep = conn.prepareStatement(s);

			while (true) {
				String Line = br.readLine();
				counter++;
				//if (counter==100) break;

				if (Line == null)
					break;

				if (!Line.isEmpty()) {
					LinkedList<String> list = Utilities.Parse3(Line, del);

					while (list.size()<fieldNames.length) {
						list.add("");
					}
					for (int i = 0; i < list.size(); i++) {
						prep.setString(i + 1, list.get(i));
					}

					prep.addBatch();
				}

				if (counter % 1000 == 0) {
					prep.executeBatch();
				}

			}

			int[] count = prep.executeBatch();// do what's left
			conn.setAutoCommit(true);

			String sqlAddIndex="CREATE INDEX IF NOT EXISTS "+indexName+" ON "+tableName+" (DSSTOX_COMPOUND_ID)";
			stat.executeUpdate(sqlAddIndex);			

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
	/**
	 *  Reads text file and stores in db. Adds autonumber id field 
	 * @param resultsCIDs 
	 * @param filepath
	 * @return
	 */
	public static void getRecordsFromTextFile(String filePathText,Hashtable<String,String>htSID_To_CID, String dbPath, String del,String tableName, HashSet<String> resultsCIDs) {

		try {
			
			
			BufferedReader br=new BufferedReader(new FileReader(filePathText));

			Connection conn= MySQL_DB.getConnection(dbPath);
			
			conn.setAutoCommit(true);

			ResultSet rs=SqlUtilities.runSQL2(conn, "select * from "+tableName+" where id=1;");
			List<String>colNamesResults=new ArrayList<>();
			
			ResultSetMetaData rsmd=rs.getMetaData();
			for (int i=1;i<=rsmd.getColumnCount();i++) {
				String colName=rsmd.getColumnName(i);
//				System.out.println(colName);
				colNamesResults.add(colName);
			}
			colNamesResults.remove("id");
			
//			String[]fieldNames=new String[colNamesResults.size()];
//			int col=0;
//			for (String colNameResults:colNamesResults) {
//				fieldNames[col++]=colNameResults;
//			}
			
			Object[]fieldNames=colNamesResults.toArray();
			
			String header = br.readLine();
			header=header.replace("MoleculeID", "DSSTOX_COMPOUND_ID");
			header=header.replace("Molecule name", "DSSTOX_COMPOUND_ID");
			header=header.replace(" ","_");
			
//			System.out.println(header);
			List<String>colNamesText=Arrays.asList(header.split(","));
			
			for (String colNameText:colNamesText) {
				if(!colNamesResults.contains(colNameText))
					System.out.println(colNameText+"\tNot in "+tableName);
			}
			
			
			for (String colNameResults:colNamesResults) {
//				System.out.println(colNameResults);
				if(!colNamesText.contains(colNameResults))
					System.out.println(colNameResults+"\tNot in text file");
			}

//			if (true) return;

			conn.setAutoCommit(false);

			String s=SQLite_CreateTable.create_sql_insert_with_field_names(fieldNames, tableName);

			int counter = 0;

			PreparedStatement prep = conn.prepareStatement(s);

			while (true) {
				String Line = br.readLine();
				counter++;
				//if (counter==100) break;

				if (Line == null || Line.isEmpty())
					break;


				LinkedList<String> list = Utilities.Parse3(Line, del);

				Hashtable<String,String>htValues=new Hashtable<>();

				if(colNamesText.size()!=list.size()) {
					System.out.println("Mismatch in # values:\t"+filePathText+"\tLine="+counter);
					break;
				}

				//System.out.println(counter+"\t"+colNamesText.size()+"\t"+list.size());

				for (int i=0;i<colNamesText.size();i++) {
					String colName=colNamesText.get(i);
					String colValue=list.get(i).trim();

					if(colValue.equals("NaN"))colValue="";//get rid of undefined values

					if(colName.equals("DSSTOX_COMPOUND_ID")) {
						colValue=htSID_To_CID.get(colValue);//convert to DTXCID
					}

					htValues.put(colName,colValue);
					//if (counter==1) System.out.println(colName+"\t"+colValue);	
											
				}
				
				String dtxcid=htValues.get("DSSTOX_COMPOUND_ID");
				
				if(resultsCIDs.contains(dtxcid)) {
					System.out.println("Already have\t"+dtxcid);
					continue;
				} else {
					resultsCIDs.add(dtxcid);
				}
				

				for (int i=0;i<colNamesResults.size();i++) {
					String colName=colNamesResults.get(i);
					String colValue=htValues.get(colName);
//					if (counter==1) System.out.println((i+1)+"\t"+colName+"\t"+colValue);	
					prep.setString(i + 1, colValue);

				}
				prep.addBatch();


				if (counter % 1000 == 0) {
					prep.executeBatch();
				}

			}

			int[] count = prep.executeBatch();// do what's left
			conn.setAutoCommit(true);
			
			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
	
	/**
	 *  Reads text file and stores in db. Adds autonumber id field 
	 * @param filepath
	 * @return
	 */
	public static Hashtable<String,String> getHT_SID_to_CID_FromSmilesFile(String filepath,String del) {

		Hashtable<String,String>ht=new Hashtable<>();
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			while (true) {
				String Line = br.readLine();
				
				if (Line == null || Line.isEmpty() || !Line.contains(del))
					break;
				
				LinkedList<String> list = Utilities.Parse3(Line, del);
				
				String smiles=list.get(1);
				String sid=list.get(1);
				String cid=list.get(2);
				
				ht.put(sid, cid);

			}
			
			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return ht;

	}
	
	
	/**
	 *  Reads text file and stores in db. Adds autonumber id field 
	 * @param filepath
	 * @return
	 */
	public static void getRecordsFromTextFile2(String filename, BufferedReader br,String del,String CID) {

		try {


			String header = br.readLine();
			header=header.replace(" ","_");
//			System.out.println(header);

			String [] fieldNames=header.split(",");

			int counter = 0;

			while (true) {
				String Line = br.readLine();
				counter++;
				//if (counter==100) break;

				if (Line == null)
					break;

				if (!Line.isEmpty()) {
//					LinkedList<String> list = Utilities.Parse3(Line, del);

					String strCID=Line.substring(0,Line.indexOf(",")).replace("\"", "");
										
					if (strCID.equals(CID)) {
						System.out.println(filename+"\t"+Line);	
					}
					
				}
			}


		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	
	static void createOPERA2_7_DB() {
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.7\\";
//		String dbpath="D:\\opera\\OPERA_2.7.db";
		String dbpath=folder+"OPERA_2.7.db";
		
		getOPERA_Results(folder+"DSSTox_082021-20211020T175916Z-001.zip",dbpath);
		getOPERA_Results(folder+"DSSTox_082021-20211020T175916Z-002.zip",dbpath);

		String zippath="DSSTox_082021/Structures/DSSTox_082021_IDs_Structures.zip";
		getChemicalInfo(folder+"DSSTox_082021-20211020T175916Z-001.zip", dbpath,zippath);
		getStructureInfo(folder+"DSSTox_082021-20211020T175916Z-001.zip",dbpath,zippath);
	}
	
	static void createOPERA2_8_DB() {
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\";
//		String dbpath="D:\\opera\\OPERA_2.7.db";
		String dbpath=folder+"OPERA_2.8.db";
		
		getOPERA_Results(folder+"OPERA_v2.8-20220426T194622Z-001.zip",dbpath);
		getOPERA_Results(folder+"OPERA_v2.8-20220426T194622Z-002.zip",dbpath);

		String zippath="Structures/DSSTox_082021_IDs_Structures.zip";		               
		String structureFileName="Structures-20220426T191038Z-001.zip";
		getChemicalInfo(folder+structureFileName, dbpath,zippath);		
		getStructureInfo(folder+structureFileName,dbpath,zippath);
	}


	static void findCID() {
		
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\";
		
		String CID="DTXCID9041287";
		getOPERA_Results2(folder+"OPERA_v2.8-20220426T194622Z-001.zip",CID);
		getOPERA_Results2(folder+"OPERA_v2.8-20220426T194622Z-002.zip",CID);

		
	}
	static void goThroughDSSTOX_SDF() {
		//		DSSTox_v2000_full.zip

		String strCID="DTXCID9041287";
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\";
		String filePath=folder+"DSSTox_v2000_full.zip";

		FileInputStream fis = null;
		ZipInputStream zipIs = null;
		ZipEntry zEntry = null;
		try {
			fis = new FileInputStream(filePath);
			ZipFile zip = new ZipFile(new File(filePath));
			zipIs = new ZipInputStream(new BufferedInputStream(fis));
			int count=0;

			while((zEntry = zipIs.getNextEntry()) != null){
				
				System.out.println(zEntry.getName());
				
				if (zEntry.getName().contains(".sdf")) {
					
//					System.out.println("here");
					
					//					System.out.println(zEntry.getName());
//					InputStream is = new ZipInputStream(zip.getInputStream(zEntry));
					//					InputStreamReader isr = new InputStreamReader(is);
					//					BufferedReader br=new BufferedReader(isr);


					IteratingSDFReader mr = new IteratingSDFReader(zip.getInputStream(zEntry),DefaultChemObjectBuilder.getInstance());

					while (mr.hasNext()) {
						
//						System.out.println("here1");

						AtomContainer m=null;
						try {
							m = (AtomContainer)mr.next();
							count++;
							String CID=m.getProperty("DSSTox_Compound_id");
							
							if (CID.equals(strCID)) {
								System.out.println(CID+" found");	
							}
							
						} catch (Exception e) {							
							if (!e.getMessage().contains("QueryAtomContainer")) {
								System.out.println(e);	
							}
						}
					}

				}
			}
			
			System.out.println("Count="+count);
			
			zipIs.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	static void diff_11_24_snapshot() {
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\";
		String folderDiff=folder+"difference with 11_12_24 snapshot\\";
				
		String filepathSmilesDiff=folderDiff+"difference with 11_12_24_SID_CID snapshot.smi";
		Hashtable<String,String>htSID_to_CID=getHT_SID_to_CID_FromSmilesFile(filepathSmilesDiff, "\t");
		//Might be better to get this hashtable directly from dsstox_records table for given snapshot
		
		System.out.println(htSID_to_CID.size());
		
//		String dbpath="D:\\opera\\OPERA_2.7.db";
		
		String dbPath=folder+"OPERA_2.8.db";
		Connection conn= SqlUtilities.getConnectionSqlite(dbPath);

		HashSet<String> resultsCIDs = getCidsInTable(conn,"Results");
		HashSet<String> structureCIDs = getCidsInTable(conn,"Structure");
		
//		for (String sid:htSID_to_CID.keySet()) {
//			String cid=htSID_to_CID.get(sid);
//			if (resultsCIDs.contains(cid)) {
//				System.out.println(sid+"\t"+cid+"\tIn OPERA2.8 db");	
//			}
//		}
		
		//New Results table records have id>1096006
		
		
//		
		for (int i=1;i<=13;i++) {
//			String filePathText=folderDiff+"results\\"+i+"-smi_OPERA2.8Pred.csv";
//			getRecordsFromTextFile(filePathText, htSID_to_CID, dbPath, ",", "Results",resultsCIDs);
			
//			//previous last structure id before update: 1095916
			String filePathTextStructure=folderDiff+"results\\"+i+"_Summary_file.csv";
			getRecordsFromTextFile(filePathTextStructure, htSID_to_CID, dbPath, ",", "Structure",structureCIDs);

		}

		
	}

	private static HashSet<String> getCidsInTable(Connection conn,String tableName) {
		String sql="select DSSTOX_COMPOUND_ID from "+tableName+";";
		ResultSet rs=SqlUtilities.runSQL2(conn, sql);
		HashSet<String>resultsCIDs=new HashSet<>();
		try {
			while (rs.next()) {
				String cid=rs.getString(1);
//				System.out.println(cid);
				
				if(resultsCIDs.contains(cid)) {
//					System.out.println(cid+"\tduplicate");
				} else {
					resultsCIDs.add(cid);	
				}
			}
			System.out.println(resultsCIDs.size());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultsCIDs;
	}
	
	public static void main(String[] args) {
//		createOPERA2_7_DB();
		diff_11_24_snapshot();
		
		
//		createOPERA2_8_DB();
//		findCID();
//		goThroughDSSTOX_SDF();
		
	}

}
