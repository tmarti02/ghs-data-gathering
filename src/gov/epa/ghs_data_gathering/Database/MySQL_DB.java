package gov.epa.ghs_data_gathering.Database;

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
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import gov.epa.ghs_data_gathering.Utilities.TESTConstants;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

/**
 * Creates a sqlite database from the results zip files
 * 
 * @author TMARTI02
 *
 */
public class MySQL_DB {
    
    private static Map<String, Connection> connPool = new HashMap<>();
    
	/**
	 * Oddly search for multiple CAS numbers in one query is slower than multiple single chemical queries
	 * 
	 * @param tableName
	 * @param keyField
	 * @param vec
	 */
	public static void getRecords(String databasePath,String tableName,String keyField,Vector<String> vec) {
		try {
			Statement stat=getStatement(databasePath);
			getRecords(stat, tableName, keyField, vec);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Oddly search for multiple CAS numbers in one query is slower than multiple single chemical queries
	 * 
	 * @param tableName
	 * @param keyField
	 * @param vec
	 */
	public static ResultSet getRecords(Statement stat,String tableName,String keyField,Vector<String> vec) {

		try {

			String query="select * from "+tableName+" where "+keyField+" = ";
			
			for (int i=0;i<vec.size();i++) {
				query+="\""+vec.get(i)+"\"";
				
				if (i<vec.size()-1) {
					query+=" or "+keyField+" = " ;
				} else {
					query+=";";
				}
			}
			
//			System.out.println(query);
			ResultSet rs = stat.executeQuery(query);
//			printResultSet(rs);
			return rs;
						
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
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
		
	
	
	public static void getRecords(String databasePath,String tableName,String keyField,String keyValue) {
		Statement stat = getStatement(databasePath);
		getRecords(stat, tableName, keyField, keyValue);
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
	
	public static ResultSet getAllRecords(Statement stat,String tableName) {

		try {
			String query="select * from "+tableName+";";
//			System.out.println(query);
			ResultSet rs = stat.executeQuery(query);
//			ResultSetMetaData rsmd = rs.getMetaData();
			
			return rs;
//			this.printResultSet(rs);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}	

	
	public static ResultSet getRecords(Statement stat,String tableName,String keyField,String keyValue) {

		try {
			String query="select * from "+tableName+" where "+keyField+" = \""+keyValue+"\";";
//			System.out.println(query);
			ResultSet rs = stat.executeQuery(query);
//			ResultSetMetaData rsmd = rs.getMetaData();
			
			return rs;
//			this.printResultSet(rs);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static ResultSet getRecords(Statement stat,String sql) {

		try {
			
//			System.out.println(query);
			ResultSet rs = stat.executeQuery(sql);
//			ResultSetMetaData rsmd = rs.getMetaData();
			
			return rs;
//			this.printResultSet(rs);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}	

	
	private void createSQliteDB_FromTextFilesSimple(String dbFilePath,String textFileFolder) {
		
		try {
			Class.forName("org.sqlite.JDBC");

			
//			String dbfilename = "R:/TEST_Results.db";
			
			File db=new File(dbFilePath);
			if (db.exists()) db.delete();
			

			//create the db:
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
			Statement stat=conn.createStatement();
		
			File Folder=new File(textFileFolder);
			
			File [] files=Folder.listFiles();
			
			for (int i=0;i<files.length;i++) {
				
				String filename = files[i].getName();
				if (filename.indexOf(".txt")==-1) continue;
				
	            String endpoint=filename.substring(0,filename.indexOf("."));
	            System.out.println(endpoint);
	            
				String[] fields = getFieldsSimple(endpoint);
	            
				String fullEndpoint=TESTConstants.getFullEndpoint(endpoint);
				if (fullEndpoint.equals("?")) continue;
	            
	            create_table(stat,endpoint,fields,"CAS");
	            
//	            System.out.printf("File: %s Size %d  Modified on %TD %n", entry.getName(), entry.getSize(), new Date(entry.getTime()));
	            extractEntry(new FileInputStream(files[i]),fields,endpoint,conn,"\t");

				
			}
			
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
			
		
	}
	
	
	private void createSQliteDB_FromTextFiles(String dbFilePath,String textFileFolder) {
		
		try {
			Class.forName("org.sqlite.JDBC");

			
//			String dbfilename = "R:/TEST_Results.db";
			
			File db=new File(dbFilePath);
			if (db.exists()) db.delete();
			

			//create the db:
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
			Statement stat=conn.createStatement();
		
			File Folder=new File(textFileFolder);
			
			File [] files=Folder.listFiles();
			
			for (int i=0;i<files.length;i++) {
				
				String filename = files[i].getName();
				if (filename.indexOf(".txt")==-1) continue;
				
	            String endpoint=filename.substring(0,filename.indexOf("."));
	            System.out.println(endpoint);
	            
				String[] fields = getFields(endpoint);
	            
				String fullEndpoint=TESTConstants.getFullEndpoint(endpoint);
				if (fullEndpoint.equals("?")) continue;
	            
	            create_table(stat,endpoint,fields,"CAS");
	            
//	            System.out.printf("File: %s Size %d  Modified on %TD %n", entry.getName(), entry.getSize(), new Date(entry.getTime()));
	            extractEntry(new FileInputStream(files[i]),fields,endpoint,conn,"\t");

				
			}
			
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
			
		
	}
	
	private void createSQliteDB_FromZipFiles() {

		try {
			Class.forName("org.sqlite.JDBC");

			String dbfilename = "data/TEST_Results.db";
//			String dbfilename = "R:/TEST_Results.db";
			
			File db=new File(dbfilename);
			if (db.exists()) db.delete();
			

			//create the db:
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbfilename);
			Statement stat=conn.createStatement();
			
			String folder = "todd/results NCCT";
			
			
//			String[] fields = { "CAS", "gsid", "DSSTOXSID", "DSSTOXCID", "ExpToxValue", "Hierarchical", "SingleModel",
//					"GroupContribution", "NearestNeighbor", "Consensus" };

			
			File Folder=new File(folder);
			
			File [] files=Folder.listFiles();
			
			for (int i=0;i<files.length;i++) {
				
				String filename = files[i].getName();
				
//				if (!filename.equals("TEST_2017-07-31_0.zip")) continue;
				
				if (filename.indexOf(".zip")==-1) continue;
				
				String filepath = folder + "/" + filename;
				
				readUsingZipFile(filepath,conn,stat);

			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
	
	private void createEndpointFilesFromZipFiles(String folder,String extension) {

		try {
			
//			String folder = "todd/results NCCT";
			
			String outputFolder=folder+"/"+extension;
			
			File OF=new File(outputFolder);
			if(!OF.exists()) OF.mkdir();
			
			
			File Folder=new File(folder);
			
			File [] files=Folder.listFiles();
			
			Hashtable<String,FileWriter>htFW=new Hashtable<String,FileWriter>();
			readUsingZipFile(folder+"/csv.zip",htFW,outputFolder,extension);
			
			
//			for (int i=0;i<files.length;i++) {
//				String filename = files[i].getName();
////				if (!filename.equals("TEST_2017-07-31_0.zip")) continue;
//				if (filename.indexOf(".zip")==-1) continue;
//				String filepath = folder + "/" + filename;
//				readUsingZipFile(filepath,htFW,outputFolder,extension);
//			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

/*
 * Example of reading Zip archive using ZipFile class
 * 
 * Read more: http://javarevisited.blogspot.com/2014/06/2-examples-to-read-zip-files-in-java-zipFile-vs-zipInputStream.html#ixzz4tX8o2FVe
 * 
 */

private void readUsingZipFile(String filepath,Connection conn,Statement stat) throws IOException {
    final ZipFile file = new ZipFile(filepath);
    System.out.println("Iterating over zip file : " + filepath);

    try {
        final Enumeration<? extends ZipEntry> entries = file.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            
            System.out.println(entry.getName());
            
            if (entry.getName().indexOf(".csv")==-1) continue;
            
            
            String endpoint=entry.getName().substring(entry.getName().indexOf("-")+1,entry.getName().indexOf("."));
            endpoint=endpoint.substring(endpoint.indexOf("-")+1,endpoint.length());
            
//            System.out.println(endpoint);
            
            
			String[] fields = getFields(endpoint);
            
			String fullEndpoint=TESTConstants.getFullEndpoint(endpoint);
			if (fullEndpoint.equals("?")) continue;
            
            create_table(stat,endpoint,fields,"CAS");
            
//            System.out.printf("File: %s Size %d  Modified on %TD %n", entry.getName(), entry.getSize(), new Date(entry.getTime()));
            extractEntry(file.getInputStream(entry),fields,endpoint,conn,",");
            
//            if (true) break;
            
        }
//        System.out.printf("Zip file %s extracted successfully in %s", filename, OUTPUT_DIR);
    } finally {
        file.close();
    }

}

private void readUsingZipFile(String filepath,Hashtable<String,FileWriter>htFW,String outputFolder,String extension) throws IOException {
    final ZipFile file = new ZipFile(filepath);
    System.out.println("Iterating over zip file : " + filepath);

    try {
        final Enumeration<? extends ZipEntry> entries = file.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            
            if (entry.getName().indexOf("."+extension)==-1) continue;
//            if (entry.getName().indexOf(".tsv")==-1) continue;
//            if (entry.getName().indexOf("2_QR-6-Viscosity.csv")==-1) continue;
            
            System.out.println(entry.getName());
            
            
            String endpoint=entry.getName().substring(entry.getName().indexOf("-")+1,entry.getName().indexOf("."));
            endpoint=endpoint.substring(endpoint.indexOf("-")+1,endpoint.length());
            
//            System.out.println(endpoint);
            
            
			String[] fields = getFields(endpoint);
            
			String fullEndpoint=TESTConstants.getFullEndpoint(endpoint);
			if (fullEndpoint.equals("?")) continue;
            
            this.createFileWriter(endpoint,outputFolder,htFW,fields);
            
            
            
//            System.out.printf("File: %s Size %d  Modified on %TD %n", entry.getName(), entry.getSize(), new Date(entry.getTime()));
            extractEntry(file.getInputStream(entry),fields,htFW.get(endpoint));
            
            
            
//            if (true) break;
            
        }
//        System.out.printf("Zip file %s extracted successfully in %s", filename, OUTPUT_DIR);
    } finally {
        file.close();
    }

}

private void createFileWriter(String endpoint,String folder,Hashtable<String,FileWriter>htFW,String[] fields) {
	try {
		
		if (htFW.get(endpoint)!=null) return;
		
		FileWriter fw=new FileWriter(folder+"/"+endpoint+".txt");
		
		for (int i=0;i<fields.length;i++) {
			fw.write(fields[i]);
			if (i<fields.length-1) fw.write("\t");
			else fw.write("\r\n");
			fw.flush();
		} 

		
		htFW.put(endpoint, fw);
		
	} catch (Exception ex) {
		ex.printStackTrace();
	}
}


private String[] getFields(String endpoint) {
	
//	String[] fields = { "CAS", "gsid", "DSSTOXSID", "DSSTOXCID", "ExpToxValue", "Hierarchical", "SingleModel",
//	"GroupContribution", "NearestNeighbor", "Consensus" };

	
	String endpointFull=TESTConstants.getFullEndpoint(endpoint);
	Vector<String>vFields=new Vector<String>();
	vFields.add("CAS");
	
	//following 3 fields can be in separate look up table: (note- overall doesnt really reduce file sizes if omit them!)
	vFields.add("gsid");
	vFields.add("DSSTOXSID");
	vFields.add("DSSTOXCID");
	
	
	vFields.add("ExpToxValue");

	vFields.add("Hierarchical");
	
	if (TESTConstants.haveSingleModelMethod(endpointFull)) vFields.add("SingleModel");
	
	if (TESTConstants.haveGroupContributionMethod(endpointFull)) vFields.add("GroupContribution");
	
	vFields.add("NearestNeighbor");
	
	vFields.add("Consensus");
	
	String [] fields=new String [vFields.size()];
	 
	for (int i=0;i<fields.length;i++) {
		fields[i]=vFields.get(i);
	}
	return fields;
}

private String[] getFieldsSimple(String endpoint) {
	
//	String[] fields = { "CAS", "gsid", "DSSTOXSID", "DSSTOXCID", "ExpToxValue", "Hierarchical", "SingleModel",
//	"GroupContribution", "NearestNeighbor", "Consensus" };

	
	String endpointFull=TESTConstants.getFullEndpoint(endpoint);
	Vector<String>vFields=new Vector<String>();
	vFields.add("CAS");
	
	//following 3 fields can be in separate look up table: (note- overall doesnt really reduce file sizes if omit them!)
//	vFields.add("gsid");
//	vFields.add("DSSTOXSID");
//	vFields.add("DSSTOXCID");
	
	
	vFields.add("ExpToxValue");
//	vFields.add("Hierarchical");
	
//	if (TESTConstants.haveSingleModelMethod(endpointFull)) vFields.add("SingleModel");
//	if (TESTConstants.haveGroupContributionMethod(endpointFull)) vFields.add("GroupContribution");
//	vFields.add("NearestNeighbor");
	
	vFields.add("Consensus");
	
	String [] fields=new String [vFields.size()];
	 
	for (int i=0;i<fields.length;i++) {
		fields[i]=vFields.get(i);
	}
	return fields;
}


	private void extractEntry( InputStream is, String[] fields, String endpoint,
			Connection conn,String delimiter) throws IOException {
		// String exractedFile = OUTPUT_DIR + entry.getName();
		// FileOutputStream fos = null;

		
		try {

			conn.setAutoCommit(false);

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String header = br.readLine();
			
			LinkedList<String> hlist = Utilities.Parse3(header, delimiter);
			
			Hashtable <String,Integer>htColNum=new Hashtable<String,Integer>();
			
			for (int i=0;i<hlist.size();i++) {
				htColNum.put(hlist.get(i), new Integer(i));
			}
			

			String s = "insert into " + endpoint + " values (";

			for (int i = 1; i <= fields.length; i++) {
				s += "?";
				if (i < fields.length)
					s += ",";
			}
			s += ");";

			
//			System.out.println(header);
//			for (int i=0;i<fields.length;i++) {
//				System.out.print(fields[i]+",");
//			}
//			System.out.print("\n");
			

			int counter=0;
			
			PreparedStatement prep = conn.prepareStatement(s);;
			
			while (true) {
				String Line = br.readLine();
				
//				System.out.println(Line);
				
				counter++;
				
				if (Line == null)
					break;

				if (!Line.equals("")) {
					
					LinkedList<String> list = Utilities.Parse3(Line, delimiter);
					
//					System.out.println(list.size()+"\t"+fields.length);
					
//					for (int i = 0; i < list.size(); i++) {
//						prep.setString(i+1, list.get(i));
////						System.out.println((i+1)+"\t"+list.get(i));
//					}
					
					for (int i=0;i<fields.length;i++) {
						
//						System.out.println(fields[i]+"\t"+htColNum.get(fields[i]));
						
						prep.setString(i+1, list.get(htColNum.get(fields[i])));
					}

					prep.addBatch();
				}

				if (counter%1000==0) {
//					System.out.println(counter);
//					prep.executeBatch();
				}
				
			}
			int [] count=prep.executeBatch();//do what's left
			
			conn.setAutoCommit(true);
//			conn.commit();
			
			
//			System.out.println(count.length);


		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
	private void extractEntry(InputStream is,String []fields,FileWriter fw) throws IOException {
		// String exractedFile = OUTPUT_DIR + entry.getName();
		// FileOutputStream fos = null;

		
		try {

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String header = br.readLine();
			
			LinkedList<String> hlist = Utilities.Parse3(header, ",");
			
			Hashtable <String,Integer>htColNum=new Hashtable<String,Integer>();
			
			for (int i=0;i<hlist.size();i++) {
				htColNum.put(hlist.get(i), new Integer(i));
			}
			
			int counter=0;
			
			while (true) {
				String Line = br.readLine();
				
				counter++;
				
//				if (counter%1000==0)
//					System.out.println(counter);
				
				if (Line == null)
					break;

				if (!Line.equals("")) {
					
					LinkedList<String> list = Utilities.Parse3(Line, ",");

//					System.out.println(Line);
					
					boolean ok=true;
					
					for (int i=0;i<fields.length;i++) {
						if (htColNum.get(fields[i])>=list.size()) {
							ok=false;
							break;
						}
					}
					
					if (!ok) continue;
					
					for (int i=0;i<fields.length;i++) {
						
						if (htColNum.get(fields[i])>=list.size()) {
							fw.write("\r\n");
							break;
						}
						
						fw.write(list.get(htColNum.get(fields[i])));
						if (i<fields.length-1) fw.write("\t");
						else fw.write("\r\n");
						
					}
					
					
				}
			}
			fw.flush();
			

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	public static void insertDataIntoTable(Connection conn,String table,String [] fields,String[] values) {
		
		try {
			
				String s="insert into "+table+" values (";
					
					for (int i=1;i<=fields.length;i++) {
						s+="?";
						if (i<fields.length) s+=",";
					}
					s+=");";
					
					PreparedStatement prep = conn
					.prepareStatement(s);
					
//					CAS,gsid,DSSTOXSID,DSSTOXCID,ExpToxValue,Hierarchical,SingleModel,GroupContribution,NearestNeighbor,Consensus

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
	
	public static void create_table_mysql (Statement stat,String table,String []fields,int [] lengths,String primaryKey) {
		
		try {
			
			String sql = "create table if not exists " + table + " (\n";

			int count = 0;// number of fields


			for (int i = 0; i < fields.length; i++) {
				if (lengths[i]>100) {
					sql += fields[i] + " TEXT ("+lengths[i]+"),\n";
				} else {
					sql += fields[i] + " VARCHAR ("+lengths[i]+"),\n";	
				}
				
				count++;
			}

			sql += "PRIMARY KEY ("+primaryKey+"));";
			
//			System.out.println(sql);
			
			stat.executeUpdate(sql);
			
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
	

	private void removeDuplicates(String folderPath,String outputFolderPath) {
		File folder=new File(folderPath);
		
		File OF=new File(outputFolderPath);
		
		if (!OF.exists()) OF.mkdir();
		
		File [] files=folder.listFiles();
		
		for (int i=0;i<files.length;i++) {
			System.out.println(files[i].getName());
			if (files[i].getName().indexOf(".txt")==-1) continue;
			
			this.sortEndpointFileRemoveDuplicates(folderPath+"/"+files[i].getName(),outputFolderPath+"/"+files[i].getName());
		}
	}
	
	private String getSanitizedString(String input) {
		if (input == null) {
			return "null";
		}
		
		return input.replace("'", "''").replace(";", "");
	}
	
//	private String getSearchQuery(SearchRequest request) {
//		   final String SELECT = "select casrn, gsid, dsstox_substance_id, preferred_name, cid, dsstox_compound_id, Canonical_QSARr, InChi_Code_QSARr, InChi_Key_QSARr from NCCT_ID where ";
//			StringBuilder sb = new StringBuilder();
//			sb.append(SELECT);
//			int n = 0;
//			for (String field : request.getFields()) {
//				if (field != null) {
//					String f = getSanitizedString(field);
//					if (request.getEntries() != null && !request.getEntries().isEmpty()) {
//						for (String entry : request.getEntries()) {
//							if (n++ > 0) {
//								sb.append(" OR ");
//							}
//							sb.append(f);
//							sb.append(" like ");
//							sb.append("'");
//							String s = getSanitizedString(entry);
//							if (!request.isExact()) {
//								sb.append("%");
//							}
//							sb.append(s);
//							if (!request.isExact()) {
//								sb.append("%");
//							}
//							sb.append("'");
//						}
//					}
//				}
//			}
//			
//			return sb.toString();
//	}
	
//	public synchronized List<NcctRecord> searchNcctDb(String databaseLocation, SearchRequest request, int maxRecordCount) {
//		List<NcctRecord> result = new ArrayList<>();
//		final String SQL = getSearchQuery(request);
//		Statement stat = getStatement(databaseLocation);
//		try {
//			ResultSet rs = stat.executeQuery(SQL);
//			
//			while (rs.next()) {
//				if (result.size() >= maxRecordCount) {
//					break;
//				}
//				
//				NcctRecord r = new NcctRecord();
//				r.setCasrn(rs.getString("casrn"));
//				r.setGsid(Integer.valueOf(rs.getString("gsid")));
//				r.setDsstoxSubstanceId(rs.getString("dsstox_substance_id"));
//				r.setDsstoxCompoundId(rs.getString("dsstox_compound_id"));
//				r.setInChICode(rs.getString("InChi_Code_QSARr"));
//				r.setInChIKey(rs.getString("InChi_Key_QSARr"));
//				r.setCid(rs.getString("cid"));
//				r.setSmiles(rs.getString("Canonical_QSARr"));
//				r.setKey(rs.getString("InChi_Code_QSARr"));
//				r.setName(rs.getString("preferred_name"));
//				result.add(r);
//			}
//			
//			rs.close();
//		} catch (SQLException e) {
//			e.printStackTrace();
//			throw new RuntimeException(e.getMessage());
//		}
//		
//
//		return result;
//	}
	
	private void sortEndpointFileRemoveDuplicates (String filePath,String destFilePath) {
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filePath));
			String header=br.readLine();
			
			Vector<String>vec=new Vector<String>(); 

			int counter=0;
			
			while (true) {
				
				counter++;
				
				if (counter%100000==0) System.out.println(counter);

				String Line=br.readLine();
				
				if (Line==null) break;
				
				String CAS=Line.substring(0, Line.indexOf("\t"));
				
				String CAS2=CAS;
				
				while (CAS2.length()<12) {
					CAS2="0"+CAS2;
				}
				
				vec.add(CAS2+"\t"+Line);
				
//				System.out.println(CAS2);
				
			}
			br.close();
			
//			System.out.println(vec.size());
			
			Collections.sort(vec);
			
			for (int i=1;i<vec.size();i++) {
				
				String Line1=vec.get(i);
				String Line2=vec.get(i-1);
				
				String CAS1=Line1.substring(0, Line1.indexOf("\t"));
				String CAS2=Line2.substring(0, Line2.indexOf("\t"));
				
				if (CAS2.equals(CAS1)) {
//					System.out.println(Line1+"\t"+Line2);
					vec.remove(i);
					i--;
				}
				
//				System.out.println(vec.get(i));
			}
			
			FileWriter fw=new FileWriter(destFilePath);
			
			fw.write(header+"\r\n");
			
			for (int i=0;i<vec.size();i++) {
				String Line=vec.get(i);
				
				Line=Line.substring(Line.indexOf("\t")+1,Line.length());
				
//				System.out.println(Line);
				
				fw.write(Line+"\r\n");
				
			}
			fw.flush();
			
			fw.close();
			
//			System.out.println(vec.size());
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	/**
	 * This version uses "contains"- for some reason a lot slower than first method
	 * 
	 * @param filePath
	 * @param destFilePath
	 */
private void sortEndpointFileRemoveDuplicates2 (String filePath,String destFilePath) {
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filePath));
			String header=br.readLine();
			
			Vector<String>vec=new Vector<String>(); 
			
			int counter=0;
			
			while (true) {
				counter++;
				
				if (counter%1000==0) System.out.println(counter);
				
				String Line=br.readLine();
				
				if (Line==null) break;
				
				String CAS=Line.substring(0, Line.indexOf("\t"));
				
				String CAS2=CAS;
				
				while (CAS2.length()<12) {
					CAS2="0"+CAS2;
				}
				
				String Line2=CAS2+"\t"+Line;
				
				if (!vec.contains(Line2)) {
//					System.out.println(Line2);
					vec.add(Line2);
				}
				
//				System.out.println(CAS2);
				
			}
			br.close();
			
			System.out.println(vec.size());
			
			Collections.sort(vec);
			
			
			FileWriter fw=new FileWriter(destFilePath);
			
			fw.write(header+"\r\n");
			
			for (int i=0;i<vec.size();i++) {
				String Line=vec.get(i);
				System.out.println(Line);
				Line=Line.substring(Line.indexOf("\t")+1,Line.length());
//				System.out.println(Line);
				fw.write(Line+"\r\n");
			}
			fw.flush();
			
			fw.close();
			
//			System.out.println(vec.size());
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MySQL_DB m=new MySQL_DB();

		String f1="R:/NCCT Results 11_16_17";
		String extension="csv";
//		m.createEndpointFilesFromZipFiles(f1,extension);
		
		String inputFolder=f1+"/csv";
		String outputFolder=inputFolder+"/no duplicates";
//		m.removeDuplicates(inputFolder,outputFolder);
		
//		m.createSQliteDB_FromTextFiles(f1+"/test_results.db", outputFolder);
		m.createSQliteDB_FromTextFilesSimple(f1+"/test_results_simple.db", outputFolder);
//		m.createSQliteDB_FromZipFiles();//do it all in one step- doesnt remove duplicates
		
		if (true) return;
		
		//**************************************************************************
		
		Vector<String>vec=new Vector<String>();
		vec.add("1163-19-5");
		vec.add("115-86-6");
		vec.add("57583-54-7");
		vec.add("5945-33-5");

		long t1=System.currentTimeMillis();
		
		String databasePath = "todd/results NCCT/TEST_Results.db";
		Statement stat=m.getStatement(databasePath);
				
		long t2=System.currentTimeMillis();
		
		m.getRecords(stat,"LC50","CAS",vec);
		
		
		long t3=System.currentTimeMillis();
		
		for (int i=0;i<vec.size();i++) {
			m.getRecords(stat,"LC50", "CAS", vec.get(i));
		}

		long t4=System.currentTimeMillis();
		
		
		System.out.println((t2-t1)+"\tloading database");
		System.out.println((t3-t2)+"\tget all chemicals at once");
		System.out.println((t4-t3)+"\tget one record at a time");
		System.out.println((t4-t1)+"\toverall time");

	}

}
