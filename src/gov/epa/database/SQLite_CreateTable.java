package gov.epa.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ser.std.StdArraySerializers.FloatArraySerializer;

public class SQLite_CreateTable {

	public static void create_table (Statement stat,String table,String []fields) {
	
		try {
	
			String sql = "create table if not exists " + table + " (";
	
			int count = 0;// number of fields
	
	
			for (int i = 0; i < fields.length; i++) {
				sql += fields[i] + " TEXT,";
				count++;
			}
	
	
			// Trim off trailing comma:
			if (sql.substring(sql.length() - 1, sql.length()).equals(",")) {
				sql = sql.substring(0, sql.length() - 1);
			}
	
			sql += ");";
	
			//			System.out.println(sql);
	
			stat.executeUpdate(sql);
	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	
	
	
	
	}

	public static void create_table (Statement stat,String table,String []fields,String primaryKey) {
	
		try {
	
			String sql = "create table if not exists " + table + " (\n";
	
			int count = 0;// number of fields
	
	
			for (int i = 0; i < fields.length; i++) {
				sql += fields[i] + " TEXT";
	
				if (fields[i].equals(primaryKey)) {
					sql+=" PRIMARY KEY";
				}
	
				if (i<fields.length-1) {
					sql+=",";
				}
	
				sql+="\n";
	
				count++;
			}
	
			sql += ");";
	
			//			System.out.println(sql);
	
			stat.executeUpdate(sql);
	
			//			System.out.println(sql);
			//			System.out.println("OK");
	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	
	}

	public static void create_table (Statement stat,String table,String []fields,String primaryKey,String secondaryKey) {
	
		try {
	
			String sql = "create table if not exists " + table + " (\n";
	
			int count = 0;// number of fields
	
	
			for (int i = 0; i < fields.length; i++) {
				sql += fields[i] + " TEXT";
	
				if (fields[i].equals(primaryKey)) {
					sql+=" PRIMARY KEY";
				}
	
				if (fields[i].equals(secondaryKey)) {
					sql+=" KEY";
				}
	
	
				if (i<fields.length-1) {
					sql+=",";
				}
	
				sql+="\n";
	
				count++;
			}
	
			sql += ");";
	
			//			System.out.println(sql);
	
			stat.executeUpdate(sql);
	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	
	}

	/**
	 * 
	 * @param stat statement
	 * @param table name of table
	 * @param fields field names
	 * @param filedTypes field data types
	 */
	public static void create_table (Statement stat,String table,String []fields,String []filedTypes) {
	
		try {
	
			String sql = "create table if not exists " + table + " (";
	
			int count = 0;// number of fields
	
	
			for (int i = 0; i < fields.length; i++) {
				sql += fields[i] + " "+filedTypes[i]+",";
				count++;
			}
	
	
			// Trim off trailing comma:
			if (sql.substring(sql.length() - 1, sql.length()).equals(",")) {
				sql = sql.substring(0, sql.length() - 1);
			}
	
			sql += ");";
	
			//			System.out.println(sql);
	
			stat.executeUpdate(sql);
	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	
	
	
	
	}
	
	public static Connection create_table(String databaseFilePath,String tableName,String [] fieldNames, boolean startFresh) {

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
				create_table(stat3, tableName, fieldNames);
			}
			
			if (Arrays.asList(fieldNames).contains("url") && startFresh) {
				conn.setAutoCommit(true);
				Statement stat4 = SQLite_Utilities.getStatement(conn);
				String sqlAddIndex="CREATE INDEX idx_url ON "+tableName+" (url)";
				stat4.executeUpdate(sqlAddIndex);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return conn;

	}

	public static void create_table_key_with_duplicates (Statement stat,String table,String []fields,String keyFieldName) {
	
		try {
	
			String sql = "create table if not exists " + table + " (\n";
	
			int count = 0;// number of fields
	
	
			for (int i = 0; i < fields.length; i++) {
				sql += fields[i] + " TEXT";
	
				if (fields[i].equals(keyFieldName)) {
					sql+=" KEY";
				}
	
				if (i<fields.length-1) {
					sql+=",";
				}
	
				sql+="\n";
	
				count++;
			}
	
			sql += ");";
	
			//			System.out.println(sql);
	
			stat.executeUpdate(sql);
	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	
	}

	public static void addDataToTable(Connection conn,String table,String [] fields,String[] values) {
	
		try {
	
			String s="insert into "+table+" values (";
	
			for (int i=1;i<=fields.length;i++) {
				s+="?";
				if (i<fields.length) s+=",";
			}
			s+=");";
	
			PreparedStatement prep = conn.prepareStatement(s);
	
			for (int i=0;i<=1;i++) {
				int field = 1;
				for (int j=1;j<=fields.length;j++) {
					prep.setString(field++, values[j]);
				}
				prep.addBatch();
			}
	
			conn.setAutoCommit(false);
			prep.executeBatch();
			conn.setAutoCommit(true);
	
	
		} catch (Exception ex) {
			ex.printStackTrace();
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
	public static void addDataToTable(String tableName,String [] fieldNames,String [] values,Connection conn) {
	
		//		Example:
		//		INSERT INTO Customers (CustomerName, ContactName, Address, City, PostalCode, Country)
		//		VALUES ('Cardinal','Tom B. Erichsen','Skagen 21','Stavanger','4006','Norway');
		String sql = "";
		try {
			sql = "INSERT INTO " + tableName + " (";
	
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
	
			//			System.out.println(sql);
	
			Statement stat = SQLite_Utilities.getStatement(conn);
			stat.executeUpdate(sql);
	
		} catch (Exception ex) {
			System.out.println(sql);
			ex.printStackTrace();
		}
	
	}

}
