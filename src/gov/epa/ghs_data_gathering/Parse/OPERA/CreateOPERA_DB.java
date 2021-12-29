package gov.epa.ghs_data_gathering.Parse.OPERA;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import gov.epa.database.SQLite_CreateTable;
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

	public static void getChemicalInfo(String filePath,String dbPath){
		try {
			ZipFile zip = new ZipFile(new File(filePath));
			ZipEntry zipEntry=zip.getEntry("DSSTox_082021/Structures/DSSTox_082021_IDs_Structures.zip");
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

	public static void getStructureInfo(String filePath,String dbPath){
		try {
			ZipFile zip = new ZipFile(new File(filePath));
			ZipEntry zipEntry=zip.getEntry("DSSTox_082021/Structures/DSSTox_082021_IDs_Structures.zip");
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


	public static void main(String[] args) {
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\";
		String dbpath="D:\\opera\\OPERA_2.7.db";
		getOPERA_Results(folder+"DSSTox_082021-20211020T175916Z-001.zip",dbpath);
		getOPERA_Results(folder+"DSSTox_082021-20211020T175916Z-002.zip",dbpath);
		getChemicalInfo(folder+"DSSTox_082021-20211020T175916Z-001.zip", dbpath);
		getStructureInfo(folder+"DSSTox_082021-20211020T175916Z-001.zip",dbpath);
	}

}
