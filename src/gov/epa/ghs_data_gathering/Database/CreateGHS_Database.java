package gov.epa.ghs_data_gathering.Database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Field;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import gov.epa.api.AADashboard;
import gov.epa.api.Chemical;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;

import gov.epa.ghs_data_gathering.Utilities.Utilities;

/**
 * Class to store AADashboard database staticly
 * 
 * TODO- is there a way to avoid using a second class and still store in static fashion?
 * 
 * @author TMARTI02
 *
 */
public class CreateGHS_Database  {
    
	public static Chemical getChemicalFromRecords(Statement stat,String CAS) {
		
		Chemical chemical=new Chemical();
		
		ArrayList<ScoreRecord>array=getRecords(stat,CAS,"HazardRecords");
		if (array.size()==0) return null;
		
		ScoreRecord r0=array.get(0);
		
		chemical.CAS=r0.CAS;
		chemical.name=r0.name;
		
		for (ScoreRecord sr:array) {
			Score score=chemical.getScore(sr.hazardName);
			score.records.add(sr);
		}
		return chemical;
	}
	
	
	public static Chemical getChemicalFromRecordsUsingPrimaryKey(Statement stat,String CAS) {
		
		Chemical chemical=new Chemical();
		
		ArrayList<ScoreRecord>array=getRecordsUsingPrimaryKey(stat,CAS,"HazardRecords");
		if (array.size()==0) return null;
		
		ScoreRecord r0=array.get(0);
		
		chemical.CAS=r0.CAS;
		chemical.name=r0.name;
		
		for (ScoreRecord sr:array) {
			Score score=chemical.getScore(sr.hazardName);
			score.records.add(sr);
		}
		return chemical;
	}
	
//	public static Chemical getChemicalFromRecords(Statement stat,int CAS) {
//		
//		Chemical chemical=new Chemical();
//		
//		ArrayList<FlatFileRecord>array=getRecords(stat,CAS,"HazardRecords");
//		if (array.size()==0) return null;
//		
//		FlatFileRecord r0=array.get(0);
//		
//		chemical.CAS=r0.CAS;
//		chemical.name=r0.name;
//		
//		for (FlatFileRecord f:array) {
//			Score score=chemical.getScore(f.hazard_name);
//			ScoreRecord sr=f.getScoreRecord();
//			score.records.add(sr);
//		}
//		return chemical;
//	}

	public static ResultSet getRecords(Statement stat,String tableName,String keyField,String keyValue) {
		 ResultSet rs=MySQL_DB.getRecords(stat, tableName, keyField, keyValue);
		 return rs;
	}
	
	/***
	 * In this method, it is assumed all of the records are stored in one field and retrieve by primary key
	 * 
	 * @param stat
	 * @param CAS
	 * @param tableName
	 * @return
	 */
	public static ArrayList<ScoreRecord> getRecordsUsingPrimaryKey(Statement stat,String CAS,String tableName) {

		ArrayList<ScoreRecord>array=new ArrayList<>();

		long t1=System.currentTimeMillis();
		ResultSet rs=MySQL_DB.getRecords(stat, tableName, "CAS", CAS);
		long t2=System.currentTimeMillis();

		//    	 System.out.println("time to pull AA records= "+(t2-t1)+" milliseconds");

		try {
			if (rs.next()) {
				String lines=rs.getString(2);

				String [] records=lines.split("\r\n");
				
				for (String record:records) {
					ScoreRecord f=ScoreRecord.createScoreRecord(record);
					//				 System.out.println(f.toString());
					array.add(f);
				}
				
				
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return array;

	}
	
	
	public static ArrayList<ScoreRecord> getRecords(Statement stat,String CAS,String tableName) {
		
		ArrayList<ScoreRecord>array=new ArrayList<>();
		
		long t1=System.currentTimeMillis();
    	ResultSet rs=MySQL_DB.getRecords(stat, tableName, "CAS", CAS);
    	long t2=System.currentTimeMillis();
    	 
//    	 System.out.println("time to pull AA records= "+(t2-t1)+" milliseconds");
    	 
    	 
		 try {
			 while (rs.next()) {
				 ScoreRecord f=createScoreRecord(rs);
//				 System.out.println(f.toString());
				 array.add(f);
			 }
			 
		 } catch (Exception ex) {
			 ex.printStackTrace();
		 }
		 return array;
		 
	}
	
//public static ArrayList<FlatFileRecord> getRecords(Statement stat,int CAS,String tableName) {
//		
//		ArrayList<FlatFileRecord>array=new ArrayList<>();
//		
//		long t1=System.currentTimeMillis();
//    	ResultSet rs=MySQL_DB.getToxicityRecord(stat, tableName, "CAS", CAS);
//    	long t2=System.currentTimeMillis();
//    	 
////    	 System.out.println("time to pull AA records= "+(t2-t1)+" milliseconds");
//    	 
//    	 
//		 try {
//			 while (rs.next()) {
//				 FlatFileRecord f=createFlatFileRecord(rs);
////				 System.out.println(f.toString());
//				 array.add(f);
//			 }
//			 
//		 } catch (Exception ex) {
//			 ex.printStackTrace();
//		 }
//		 return array;
//		 
//	}
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
	public static void createDatabase(String textFilePath,String del,String tableName,String [] fieldNames) {

		try {
			System.out.println("Creating AA dashboard SQlite table");

			Connection conn= MySQL_DB.getConnection(AADashboard.DB_Path_AA_Dashboard_Records);
			Statement stat = MySQL_DB.getStatement(conn);
			
			conn.setAutoCommit(true);
			
			
			stat.executeUpdate("drop table if exists "+tableName+";");
			 
			stat.executeUpdate("VACUUM;");//compress db now that have deleted the table
			
//			MySQL_DB.create_table(stat, tableName, fields);
			
			//Need CAS as the primary key if we are doing lots of searches- otherwise searches will be like 1 second each!
			MySQL_DB.create_table_key_with_duplicates(stat, tableName, fieldNames,"CAS");//need unique values in the table for key field for this to work!

			conn.setAutoCommit(false);

			BufferedReader br = new BufferedReader(new FileReader(textFilePath));

			String header = br.readLine();

			String s = "insert into " + tableName + " values (";

			for (int i = 1; i <= fieldNames.length; i++) {
				s += "?";
				if (i < fieldNames.length)
					s += ",";
			}
			s += ");";


			int counter = 0;

			PreparedStatement prep = conn.prepareStatement(s);
			

			while (true) {
				String Line = br.readLine();

				counter++;
				
//				if (counter==100) break;

				if (Line == null)
					break;

				if (!Line.isEmpty()) {

					LinkedList<String> list = Utilities.Parse(Line, del);
					
					if (list.size()!=fieldNames.length) {
						System.out.println("*wrong number of values: "+Line);
					}

//					 System.out.println(Line);

					for (int i = 0; i < list.size(); i++) {
						prep.setString(i + 1, list.get(i));
//						 System.out.println((i+1)+"\t"+list.get(i));
					}

					prep.addBatch();
				}

				if (counter % 1000 == 0) {
					// System.out.println(counter);
					prep.executeBatch();
				}

			}

			int[] count = prep.executeBatch();// do what's left

			conn.setAutoCommit(true);
						
			String sqlAddIndex="CREATE INDEX idx_CAS ON "+tableName+" (CAS)";
			stat.executeUpdate(sqlAddIndex);			

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
		
//			System.out.println(sql);
			
			Statement stat = MySQL_DB.getStatement(conn);
			stat.executeUpdate(sql);
						

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
	public static Connection createDatabaseTable(String databaseFilePath,String tableName,String [] fieldNames) {

		Connection conn=null;
		
		try {
			System.out.println("Creating "+tableName+" table");

			conn= MySQL_DB.getConnection(databaseFilePath);
			Statement stat = MySQL_DB.getStatement(conn);
			
			conn.setAutoCommit(true);
						
			stat.executeUpdate("drop table if exists "+tableName+";");			 
			stat.executeUpdate("VACUUM;");//compress db now that have deleted the table
			
			MySQL_DB.create_table(stat, tableName, fieldNames);
			
//			conn.setAutoCommit(true);
//						
//			String sqlAddIndex="CREATE INDEX idx_CAS ON "+tableName+" (CAS)";
//			stat.executeUpdate(sqlAddIndex);			

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return conn;

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
	public static void createDatabaseWithPrimaryKey(String textFilePath,String dbPath,String del,String tableName,String [] fieldNames) {

		try {
			System.out.println("Creating AA dashboard SQlite table");

			Connection conn= MySQL_DB.getConnection(dbPath);
			Statement stat = MySQL_DB.getStatement(conn);
			
			conn.setAutoCommit(true);
			
			
			stat.executeUpdate("drop table if exists "+tableName+";");
			 
			stat.executeUpdate("VACUUM;");//compress db now that have deleted the table
			
//			MySQL_DB.create_table(stat, tableName, fields);
			
			//Need CAS as the primary key if we are doing lots of searches- otherwise searches will be like 1 second each!
			MySQL_DB.create_table(stat, tableName, fieldNames,"CAS");//need unique values in the table for key field for this to work!

			conn.setAutoCommit(false);
			
			ArrayList<String>lines=Utilities.readFileToArray(textFilePath);
			String header=lines.remove(0);
			Collections.sort(lines);

			String s = "insert into " + tableName + " values (";

			for (int i = 1; i <= fieldNames.length; i++) {
				s += "?";
				if (i < fieldNames.length)
					s += ",";
			}
			s += ");";


			int counter = 0;

			PreparedStatement prep = conn.prepareStatement(s);
			
			String CAS="";
			
			String records="";
			
			int count=0;
			
			for (String Line:lines) {
//				System.out.println(Line);
				
				String currentCAS=Line.substring(0,Line.indexOf(del));
				
				if (!CAS.equals(currentCAS)) {
					
					if (!CAS.isEmpty()) { 
						count++;
						prep.setString(1, CAS);
						prep.setString(2, records);
						prep.addBatch();

						if (counter % 1000 == 0) {
							// System.out.println(counter);
							prep.executeBatch();
						}
					}
					
					records=Line;
					CAS=currentCAS;
				} else {
					records+=Line+"\r\n";//separate records in Records field with a carriage return
				}
			}
			
			prep.setString(1, CAS);
			prep.setString(2, records);
			prep.addBatch();
			
			prep.executeBatch();// do what's left
			

			conn.setAutoCommit(true);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
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
//	public static void createDatabaseIntegerKey(String textFilePath,String del,String tableName,String [] fieldNames,String dbFilePath) {
//
//		try {
//			System.out.println("Creating AA dashboard SQlite table");
//
//			Connection conn= MySQL_DB.getConnection(dbFilePath);
//			Statement stat = MySQL_DB.getStatement(conn);
//			
//			conn.setAutoCommit(true);
//			
//			
//			stat.executeUpdate("drop table if exists "+tableName+";");
//			 
//			stat.executeUpdate("VACUUM;");//compress db now that have deleted the table
//			
////			MySQL_DB.create_table(stat, tableName, fields);
//			
//			//Need CAS as the primary key if we are doing lots of searches- otherwise searches will be like 1 second each!
//			MySQL_DB.create_table_key_with_duplicates_integer_key(stat, tableName, fieldNames,"CAS");//need unique values in the table for key field for this to work!
//
//			conn.setAutoCommit(false);
//
//			BufferedReader br = new BufferedReader(new FileReader(textFilePath));
//
//			String header = br.readLine();
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
//			while (true) {
//				String Line = br.readLine();
//
//				counter++;
//				
////				if (counter==100) break;
//
//				if (Line == null)
//					break;
//
//				if (!Line.isEmpty()) {
//
//					LinkedList<String> list = ToxPredictor.Utilities.Utilities.Parse(Line, del);
//					
//					
//					if (list.size()!=fieldNames.length) {
//						System.out.println("*wrong number of values: "+Line);
//					}
//
//					
//					String CAS=list.getFirst();
//					String CAS2=CAS.replace("-", "");	
//						
//					int iCAS=-1;
//					
//					try {
//						iCAS=Integer.parseInt(CAS2);
//					} catch (Exception ex) {
//						continue;
//					}
//					
//					
////					 System.out.println(Line);
//
//					for (int i = 0; i < list.size(); i++) {
//						if (i==0) {
//							prep.setInt(i + 1, iCAS );
//						} else {
//							prep.setString(i + 1, list.get(i));	
//						}
//						
////						 System.out.println((i+1)+"\t"+list.get(i));
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
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//	}
	
	private  static ScoreRecord createScoreRecord(ResultSet rs) {
		ScoreRecord f=new ScoreRecord(null,null,null);
		
		 for (int i = 0; i < f.allFieldNames.length; i++) {
				try {
				
					Field myField = f.getClass().getDeclaredField(f.allFieldNames[i]);
					
					if (myField.getType().getName().contains("Double")) {
						double val=rs.getDouble(i+1);
//						System.out.println("*"+val);
						
						myField.set(f, val);
						
					} else {
						String val=rs.getString(i+1);
						
						if (val!=null) {
							myField.set(f, val);
						} 
					}
				
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		 return f;
		 
	}
	
//	static void sortRecordsByCAS(String folder,String filename,String filenameSorted) {
//		
//		ArrayList<String>lines=ToxPredictor.Utilities.Utilities.readFileToArray(folder+"/"+filename);
//		
//		String header=lines.remove(0);
//		
//		Collections.sort(lines);
//		
//		try {
//			
//			FileWriter fw=new FileWriter(folder+"/"+filenameSorted);
//			
//			
//			
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		
//	}
//	
	
	
	
	
	public static void main(String[] args) {

		//Create files for all sources:
//		Parse.recreateFilesAllSources();
		
		String folder=AADashboard.dataFolder+"\\dictionary\\text output";
		String filename="flat file 2020-09-21.txt";
		String textFilePath=folder+"\\"+filename;

//		ScoreRecord.createFlatFileFromAllSources(textFilePath);

		//Create flat file for all data:
		ScoreRecord.createFlatFileFromAllSourcesSortedByCAS(textFilePath);
		
		//Get counts for each source:
//		FlatFileRecord.analyzeRecords(textFilePath,folder+"/counts.txt");
		
		String del="|";		
		//Create Sqlite database from flat file:
		CreateGHS_Database.createDatabase(textFilePath,del,"HazardRecords",ScoreRecord.allFieldNames);
		
		
		String  []fields= {"CAS","Records"};
//		CreateGHS_Database.createDatabaseWithPrimaryKey(textFilePath,"databases/AA dashboard_w_primary_key.db", del, "HazardRecords", fields);
	}
}
