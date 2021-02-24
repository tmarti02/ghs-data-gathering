package gov.epa.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//import gov.epa.QSAR.utilities.CSVUtils;
//import gov.epa.QSAR.utilities.FileUtilities;
//import gov.epa.QSAR.utilities.LineSplitter;
//import gov.epa.QSAR.utilities.TESTConstants;




/**
 * Creates a sqlite database from the results zip files
 * 
 * @author TMARTI02
 *
 */
public class SQLite_Utilities {

	private static Map<String, Connection> connPool = new HashMap<>();

	
	/**
	 * Create sqlite database table with CAS as primary key (needs unique values for this to work)
	 * 
	 * Can search by any field in table but CAS is much faster since primary key
	 * 
	 * See http://sqlitebrowser.org/ for user friendly sqlite GUI to look at the database once it's created
	 * 
	 * @param filepath
	 * @return
	 */
//	public static void createDatabaseFromTextFile(String filepathDatabase, String textFilePath,String del,String tableName,String [] fieldNames) {
//
//		try {
//			System.out.println("Creating AA dashboard SQlite table");
//
//			Connection conn= SQLite_Utilities.getConnection(filepathDatabase);
//			Statement stat = SQLite_Utilities.getStatement(conn);
//
//			conn.setAutoCommit(true);
//
//
//			stat.executeUpdate("drop table if exists "+tableName+";");
//
//			stat.executeUpdate("VACUUM;");//compress db now that have deleted the table
//
//			//			MySQL_DB.create_table(stat, tableName, fields);
//
//			//Need CAS as the primary key if we are doing lots of searches- otherwise searches will be like 1 second each!
//			SQLite_CreateTable.create_table_key_with_duplicates(stat, tableName, fieldNames,"CAS");//need unique values in the table for key field for this to work!
//
//			conn.setAutoCommit(false);
//
//			Scanner scanner = new Scanner(new FileReader(textFilePath));
//
//			String header = scanner.nextLine();
//
//			String s = "insert into " + tableName + " values (";
//
//			for (int i = 1; i <= fieldNames.length; i++) {
//				s += "?";
//				if (i < fieldNames.length)
//					s += ",";
//			}
//			s += ");";
//
//
//			int counter = 0;
//
//			PreparedStatement prep = conn.prepareStatement(s);
//
//
//			while (scanner.hasNext()) {
//				String Line = scanner.nextLine();
//
//				counter++;
//
//				//				if (counter==100) break;
//
//
//				if (!Line.isEmpty()) {
//
//					List<String>list=CSVUtils.parseLine(Line,del);
//
//					if (list.size()!=fieldNames.length) {
//						System.out.println("*wrong number of values: "+Line);
//					}
//
//					//					 System.out.println(Line);
//
//					for (int i = 0; i < list.size(); i++) {
//						prep.setString(i + 1, list.get(i));
//						//						 System.out.println((i+1)+"\t"+list.get(i));
//					}
//
//					prep.addBatch();
//				}
//
//				if (counter % 1000 == 0) {
//					// System.out.println(counter);
//					prep.executeBatch();
//				}
//
//			}
//
//			int[] count = prep.executeBatch();// do what's left
//
//			conn.setAutoCommit(true);
//
//			String sqlAddIndex="CREATE INDEX idx_CAS ON "+tableName+" (CAS)";
//			stat.executeUpdate(sqlAddIndex);			
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//	}
//
//
//
//
//
//
//
//
//	/**
//	 * Create sqlite database table with CAS as primary key (needs unique values for this to work)
//	 * 
//	 * Can search by any field in table but CAS is much faster since primary key
//	 * 
//	 * See http://sqlitebrowser.org/ for user friendly sqlite GUI to look at the database once it's created
//	 * 
//	 * @param filepath
//	 * @return
//	 */
//	public static void createDatabaseWithPrimaryKeyFromTextFile(String textFilePath,String dbPath,String del,String tableName,String [] fieldNames) {
//
//		try {
//			System.out.println("Creating AA dashboard SQlite table");
//
//			Connection conn= SQLite_Utilities.getConnection(dbPath);
//			Statement stat = SQLite_Utilities.getStatement(conn);
//
//			conn.setAutoCommit(true);
//
//
//			stat.executeUpdate("drop table if exists "+tableName+";");
//
//			stat.executeUpdate("VACUUM;");//compress db now that have deleted the table
//
//			//			MySQL_DB.create_table(stat, tableName, fields);
//
//			//Need CAS as the primary key if we are doing lots of searches- otherwise searches will be like 1 second each!
//			SQLite_CreateTable.create_table(stat, tableName, fieldNames,"CAS");//need unique values in the table for key field for this to work!
//
//			conn.setAutoCommit(false);
//
//			List<String>lines=FileUtilities.readFile(textFilePath);
//			String header=lines.remove(0);
//			Collections.sort(lines);
//
//			String s = "insert into " + tableName + " values (";
//
//			for (int i = 1; i <= fieldNames.length; i++) {
//				s += "?";
//				if (i < fieldNames.length)
//					s += ",";
//			}
//			s += ");";
//
//
//			int counter = 0;
//
//			PreparedStatement prep = conn.prepareStatement(s);
//
//			String CAS="";
//
//			String records="";
//
//			int count=0;
//
//			for (String Line:lines) {
//				//				System.out.println(Line);
//
//				String currentCAS=Line.substring(0,Line.indexOf(del));
//
//				if (!CAS.equals(currentCAS)) {
//
//					if (!CAS.isEmpty()) { 
//						count++;
//						prep.setString(1, CAS);
//						prep.setString(2, records);
//						prep.addBatch();
//
//						if (counter % 1000 == 0) {
//							// System.out.println(counter);
//							prep.executeBatch();
//						}
//					}
//
//					records=Line;
//					CAS=currentCAS;
//				} else {
//					records+=Line+"\r\n";//separate records in Records field with a carriage return
//				}
//			}
//
//			prep.setString(1, CAS);
//			prep.setString(2, records);
//			prep.addBatch();
//
//			prep.executeBatch();// do what's left
//
//
//			conn.setAutoCommit(true);
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//	}

	
	

	

	public static void printResultSet(ResultSet rs) {
		try {

			ResultSetMetaData rsmd = rs.getMetaData();

			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				System.out.print(rsmd.getColumnName(i));
				if (i < rsmd.getColumnCount())
					System.out.print("\t");
				else
					System.out.print("\n");
			}

			while (rs.next()) {
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					System.out.print(rs.getString(i));
					if (i < rsmd.getColumnCount())
						System.out.print("\t");
					else
						System.out.print("\n");
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}



	

	public static Connection getConnection(String databasePath)  {
		try {
			if (connPool.containsKey(databasePath) && connPool.get(databasePath) != null && !connPool.get(databasePath).isClosed()) {
				return connPool.get(databasePath);
			} else {
				Class.forName("org.sqlite.JDBC");
				Connection conn = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
				connPool.put(databasePath, conn); 
				return conn;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static Connection getConnectionMySQL(String mySQL_DB_URL,String USER,String PASS)  {
		try {
			//TODO- Note: using a connection pool is actually slightly slower than just making a new connection- the first connection is always slow but second time is fast even if not using a map to get the connection
			if (connPool.containsKey(mySQL_DB_URL) && connPool.get(mySQL_DB_URL) != null && !connPool.get(mySQL_DB_URL).isClosed()) {
				//				System.out.println("we haz it!");
				return connPool.get(mySQL_DB_URL);
			} else {
				long t1=System.currentTimeMillis();
				//STEP 2: Register JDBC driver
				Class.forName("com.mysql.cj.jdbc.Driver");

				//STEP 3: Open a connection
				//			   System.out.println("Connecting to database...");
				Connection conn = DriverManager.getConnection(mySQL_DB_URL, USER, PASS);
				connPool.put(mySQL_DB_URL, conn); 
				long t2=System.currentTimeMillis();
				//				System.out.println("Made new connection in "+(t2-t1)+" milliseconds");

				return conn;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Create sqlite database table with CAS as primary key (needs unique values for this to work)
	 * 
	 * Can search by any field in table but CAS is much faster since primary key
	 * 
	 * See http://sqlitebrowser.org/ for user friendly sqlite GUI to look at the database once it's created
	 * 
	 * @param filepath
	 * @return
	 */
	public static Connection createDatabaseTable(String databaseFilePath,String tableName,String [] fieldNames,String [] fieldTypes, boolean startFresh) {

		Connection conn=null;

		try {
			conn= SQLite_Utilities.getConnection(databaseFilePath);
			conn.setAutoCommit(true);

			if (startFresh) {
				System.out.println("Creating "+tableName+" table");
				Statement stat1 = SQLite_Utilities.getStatement(conn);
				stat1.executeUpdate("drop table if exists "+tableName+";");
				stat1.close();
				Statement stat2 = SQLite_Utilities.getStatement(conn);
				stat2.executeUpdate("VACUUM;");//compress db now that have deleted the table
				stat2.close();
				Statement stat3 = SQLite_Utilities.getStatement(conn);
				SQLite_CreateTable.create_table(stat3, tableName, fieldNames,fieldTypes);
			}

			//			conn.setAutoCommit(true);
			//						
			//			String sqlAddIndex="CREATE INDEX idx_CAS ON "+tableName+" (CAS)";
			//			stat.executeUpdate(sqlAddIndex);			

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return conn;

	}
	
	
	public static Statement getStatement(Connection conn)  {

		try {
			return conn.createStatement();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static Statement getStatement(String databasePath) {

		try {
			Class.forName("org.sqlite.JDBC");

			// create the db:
			Connection conn = getConnection(databasePath);

			//			System.out.println("getting statement for "+databasePath);

			Statement stat = conn.createStatement();
			return stat;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	
	public static void deleteRecords(String tableName, String fieldName,String fieldValue, Statement stat) {
		
		try {
			String sql="DELETE FROM "+tableName+" WHERE "+fieldName+" = \""+fieldValue+"\";";
//			System.out.println(sql);
			stat.execute(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	




	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SQLite_Utilities m=new SQLite_Utilities();



	}

}
