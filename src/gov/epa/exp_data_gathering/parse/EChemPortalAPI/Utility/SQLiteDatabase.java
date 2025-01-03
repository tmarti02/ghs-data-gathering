package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods to handle data in a SQLite database
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class SQLiteDatabase {
	private static Map<String, Connection> connPool = new HashMap<>();
	
	/**
	 * Creates a new table in a database (and creates the database if it does not exist) with the option to either overwrite or append to an existing table
	 * @param databaseFilePath	The path to the database to be created or modified
	 * @param tableName			The name of the table to be created or modified
	 * @param fieldNames		The field names of the table
	 * @param startFresh		True to create new or overwrite existing table, false to append
	 * @return					Connection to the created or modified database
	 */
	public static Connection createTable(String databaseFilePath,String tableName,String[] fieldNames, boolean startFresh) {
		Connection conn=null;
		try {
			conn= getConnection(databaseFilePath);
			conn.setAutoCommit(true);
			
			if (startFresh) {
				System.out.println("Creating "+tableName+" table");
				Statement stat1 = getStatement(conn);
				stat1.executeUpdate("drop table if exists "+tableName+";");
				stat1.close();
				Statement stat2 = getStatement(conn);
				stat2.executeUpdate("VACUUM;");//compress db now that have deleted the table
				stat2.close();
				Statement stat3 = getStatement(conn);
				createTable(stat3, tableName, fieldNames);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return conn;
	}
	
	/**
	 * Creates a new table in a database (and creates the database if it does not exist)
	 * @param stat			Statement on the database to be created or modified
	 * @param table			The name of the table to be created or modified
	 * @param fieldNames	The field names of the table
	 */
	public static void createTable(Statement stat,String table,String[] fieldNames) {
		try {
			String sql = "create table if not exists " + table + " (";
			for (int i = 0; i < fieldNames.length; i++) {
				sql += fieldNames[i] + " TEXT,";
			}
	
			// Trim off trailing comma:
			if (sql.substring(sql.length() - 1, sql.length()).equals(",")) {
				sql = sql.substring(0, sql.length() - 1);
			}
	
			sql += ");";
	
			stat.executeUpdate(sql);
	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Gets a Connection to a database
	 * @param databasePath	The path to the database
	 * @return				Conection to the database
	 */
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
	
	/**
	 * Gets a statement on a database from a Connection
	 * @param conn	The Connection to the database
	 * @return		Statement on the database
	 */
	public static Statement getStatement(Connection conn)  {
		try {
			return conn.createStatement();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Gets a statement on a database from a file path
	 * @param databasePath	The path to the database
	 * @return				Statement on the database
	 */
	public static Statement getStatement(String databasePath) {
		try {
			Class.forName("org.sqlite.JDBC");

			Connection conn = getConnection(databasePath);

			Statement stat = conn.createStatement();
			return stat;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Adds an array of strings as a row in a database
	 * @param tableName		The name of the table to be added to
	 * @param fieldNames	The field names of the table
	 * @param values		The values to add
	 * @param conn			The Connection to the database to be added to
	 */
	public static void addDataToTable(String tableName,String[] fieldNames,String[] values,Connection conn) {
		try {
			String sql = "INSERT INTO " + tableName + " (";
	
			for (int i = 0; i < fieldNames.length; i++) {
				sql+=fieldNames[i];				
				if (i<fieldNames.length-1) sql+=",";
			}
			sql+=")\r\n";
	
			sql+="VALUES (";
	
			for (int i = 0; i < values.length; i++) {
				sql+="'"+values[i]+"'";				
				if (i<values.length-1) sql+=",";
			}
	
			sql+=")\r\n";

			Statement stat = getStatement(conn);
			stat.executeUpdate(sql);
	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	
	}
	
	/**
	 * Gets all records out of a database table
	 * @param stat			Statement on the database to be queried
	 * @param tableName		The table to be queried
	 * @return				Contents of the table as a ResultSet
	 */
	public static ResultSet getAllRecords(Statement stat,String tableName) {

		try {
			String query="select * from "+tableName+";";
			ResultSet rs = stat.executeQuery(query);
			return rs;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}	
}
