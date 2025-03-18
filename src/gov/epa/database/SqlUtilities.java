package gov.epa.database;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SqlUtilities {

	private static Map<String, Connection> connPool = new HashMap<>();

	public static final String dbPostGres="Postgres";
	public static final String dbDSSTOX="DSSTOX";
	public static final String dbToxVal93="dbToxVal93";

	/**
	 * Gives you connection to Postgres database based on the environment variables
	 * connPool allows the next call to be instantaneous
	 * 
	 * @return
	 */
	public static Connection getConnectionPostgres() {

		try {
			if (connPool.containsKey(dbPostGres) && connPool.get(dbPostGres) != null && !connPool.get(dbPostGres).isClosed()) {
				//				System.out.println("have active conn");
				return connPool.get(dbPostGres);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String host = System.getenv().get("DEV_QSAR_HOST");
		String port = System.getenv().get("DEV_QSAR_PORT");
		String db = System.getenv().get("DEV_QSAR_DATABASE");

		String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;
		String user = System.getenv().get("DEV_QSAR_USER");
		String password = System.getenv().get("DEV_QSAR_PASS");

		try {
			Connection conn = DriverManager.getConnection(url, user, password);
			connPool.put(dbPostGres, conn);
			return conn;
		} catch (Exception ex) {
			return null;
		}
	}

	public static Connection getConnectionSqlite(String databasePath)  {
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



	public static Connection getConnectionDSSTOX() {

		try {
			if (connPool.containsKey(dbDSSTOX) && connPool.get(dbDSSTOX) != null && !connPool.get(dbDSSTOX).isClosed()) {
				return connPool.get(dbDSSTOX);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		String host = System.getenv().get("DSSTOX_HOST");
		String port = System.getenv().get("DSSTOX_PORT");
		String db = System.getenv().get("DSSTOX_DATABASE");

		String url = "jdbc:mysql://" + host + ":" + port + "/" + db;

//		System.out.println(url);


		String user = System.getenv().get("DSSTOX_USER");
		String password = System.getenv().get("DSSTOX_PASS");

		try {
			Connection conn = DriverManager.getConnection(url, user, password);

			connPool.put(dbDSSTOX, conn);

			return conn;
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Use reflection to store sql record into Java class
	 * 
	 * @param rs
	 * @param r
	 */
	public static void createRecord(ResultSet rs, Object r) {
		ResultSetMetaData rsmd;
		try {
			rsmd = rs.getMetaData();

			int columnCount = rsmd.getColumnCount();

			// The column count starts from 1
			for (int i = 1; i <= columnCount; i++ ) {
				String name = rsmd.getColumnLabel(i);
				
				if(name.equals("class")) name="class_";
												
				String val=rs.getString(i);
				
				if(val==null || val.equals("-") || val.isBlank()) continue;

				//				System.out.println(name+"\t"+val);

				if (val!=null) {
					Field myField = r.getClass().getDeclaredField(name);	
					
					String type=myField.getType().getName();
					
					if (type.contentEquals("boolean")) {
						myField.setBoolean(r, rs.getBoolean(i));
					} else if (type.contentEquals("double")) {
						myField.setDouble(r, rs.getDouble(i));
					} else if (type.contentEquals("int")) {
						myField.setInt(r, rs.getInt(i));
					} else if (type.contentEquals("long")) {
						myField.setLong(r, rs.getLong(i));

					} else if (type.contentEquals("java.lang.Long") || type.contentEquals("java.lang.Double") || type.contentEquals("java.lang.Integer")) {
						myField.set(r, rs.getObject(i));


				//Following parses from string- so if need to change data types, this will let you do it:
//					} else if (type.contentEquals("java.lang.Double")) {
//						Double dvalue=(Double)rs.getObject(i);
//						myField.set(r, dvalue);
////						System.out.println(name+"\tDouble");
//						try {
//							Double dval=Double.parseDouble(val);						
//							myField.set(r, dval);
//						} catch (Exception ex) {
//							System.out.println("Error parsing "+val+" for field "+name+" to Double for "+rs.getString(1));
//						}
//					} else if (type.contentEquals("java.lang.Integer")) {
//						Integer ival=Integer.parseInt(val);
//						myField.setInt(r,ival);
					} else if (type.contentEquals("[B")) {		
						myField.set(r,rs.getBytes(i));
					} else if (type.contentEquals("java.lang.String")) {
						myField.set(r, val);
					} else if (type.contentEquals("java.util.Set")) {
//						System.out.println(name+"\t"+val);
						val=val.replace("[", "").replace("]", "");
						
						String  [] values = val.split(", ");
						Set<String>list=new HashSet<>();
						for (String value:values) {
							list.add(value.trim());
						}
						myField.set(r,list);

					} else if (type.contentEquals("java.util.List")) {
						
						//TODO use getArray instead and then convert array to list?
						
//						System.out.println(name+"\t"+val);
						val=val.replace("[", "").replace("]", "");
						
						String  [] values = val.split(",");
						ArrayList<String>list=new ArrayList<>();
						for (String value:values) {
							list.add(value.trim());
						}
						myField.set(r,list);
					} else {
						System.out.println("Need to implement: "+type);
					}					
										
				}

			}


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Connection getConnectionToxValV93() {

		try {
			if (connPool.containsKey(dbToxVal93) && connPool.get(dbToxVal93) != null && !connPool.get(dbToxVal93).isClosed()) {
				return connPool.get(dbToxVal93);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		String host = System.getenv().get("DSSTOX_HOST");
		String port = System.getenv().get("DSSTOX_PORT");
		String db = "prod_toxval_v93";

		String url = "jdbc:mysql://" + host + ":" + port + "/" + db;
		String user = System.getenv().get("DSSTOX_USER");
		String password = System.getenv().get("DSSTOX_PASS");

		try {
			Connection conn = DriverManager.getConnection(url, user, password);

			connPool.put(dbToxVal93, conn);

			return conn;
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Lets you look up splitNum from smiles for the given splittingName and datasetName
	 * @param datasetName
	 * @param splittingName
	 * @return
	 */
	public static Hashtable<String, Integer> getHashtableSplitNum(String datasetName, String splittingName) {
		Hashtable<String,Integer>htSplitNum=new Hashtable<>();

		String sql="select dp.canon_qsar_smiles ,dpis.split_num  from qsar_datasets.data_points_in_splittings dpis\n"+
				"join qsar_datasets.splittings s on s.id=dpis.fk_splitting_id\n"+
				"join qsar_datasets.data_points dp on dp.id=dpis.fk_data_point_id\n"+
				"join qsar_datasets.datasets d on d.id=dp.fk_dataset_id\n"+
				"where s.\"name\" ='"+splittingName+"' and d.\"name\" ='"+datasetName+"';";


		ResultSet rs=runSQL2(getConnectionPostgres(), sql);

		try {
			while (rs.next()) {				
				String ID=rs.getString(1);
				Integer splitNum=Integer.parseInt(rs.getString(2));
				htSplitNum.put(ID, splitNum);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return htSplitNum;
	}

	//	/**
	//	 * Get predicted values for the model's splitting
	//	 * @param modelId
	//	 * @return
	//	 */
	//	public static Hashtable<String, Double> getHashtablePredValues(long modelId) {
	//		//Get pred values:
	//
	//		String sql="select p.canon_qsar_smiles,p.qsar_predicted_value, p.fk_splitting_id  from qsar_models.predictions p\n"+ 
	//		"join qsar_models.models m on m.id=p.fk_model_id\n"+
	//		"join qsar_datasets.splittings s on s.id=p.fk_splitting_id\n"+ 
	//		"where fk_model_id="+modelId+" and s.\"name\" =m.splitting_name;";
	//
	//		Connection conn=getConnectionPostgres();
	//		ResultSet rs=DatabaseLookup.runSQL2(conn, sql);
	//		Hashtable<String,Double>htPred=new Hashtable<>();
	//		
	//		try {
	//			while (rs.next()) {				
	//				String ID=rs.getString(1);
	//				Double pred=rs.getDouble(2);
	//				int splittingId=rs.getInt(3);//Just for inspection
	//				htPred.put(ID, pred);
	//			}
	//			
	//		} catch (SQLException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//			return null;
	//		}
	//		return htPred;
	//	}

	/**
	 * Get predicted values for specified splitting (e.g. CV split)
	 * Lets you look up predicted value from smiles
	 * 
	 * @param modelId
	 * @param splittingName
	 * @return
	 */
	public static LinkedHashMap<String, Double> getHashtablePredValues(long modelId,String splittingName) {
		//Get pred values:

		String sql="select p.canon_qsar_smiles,p.qsar_predicted_value, p.fk_splitting_id  from qsar_models.predictions p\n"+ 
				"join qsar_models.models m on m.id=p.fk_model_id\n"+
				"join qsar_datasets.splittings s on s.id=p.fk_splitting_id\n"+ 
				"where fk_model_id="+modelId+" and s.\"name\"='"+splittingName+"'"+
				"order by p.canon_qsar_smiles;";

		//		System.out.println(sql);


		ResultSet rs=runSQL2(getConnectionPostgres(), sql);
		LinkedHashMap<String,Double>htPred=new LinkedHashMap<>();

		try {
			while (rs.next()) {				
				String ID=rs.getString(1);
				Double pred=rs.getDouble(2);
				int splittingId=rs.getInt(3);//Just for inspection
				htPred.put(ID, pred);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return htPred;
	}


	public static String runSQL(Connection conn, String sql) {
		try {
			Statement st = conn.createStatement();			
			ResultSet rs = st.executeQuery(sql);
			if (rs.next()) {
				return rs.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void runSQLUpdate(Connection conn, String sql) {
		try {
			conn.setAutoCommit(false);
			System.out.println("\nrunning "+sql);
			Statement st = conn.createStatement();			
			st.executeUpdate(sql);
			conn.commit();
			conn.setAutoCommit(true);
			System.out.println("done");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static ResultSet runSQL2(Connection conn, String sql) {
		try {
			Statement st = conn.createStatement();			
			ResultSet rs = st.executeQuery(sql);
			return rs;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}		
	}




	private String createSqlInsertWithCurrentTimestamp(String[] fieldNames,String tableName,String schema) {

		String sql;
		
		if (schema!=null) {
			sql="INSERT INTO "+schema+"."+tableName+" (";	
		} else {
			sql="INSERT INTO "+tableName+" (";
		}
		
		for (int i=0;i<fieldNames.length;i++) {
			sql+=fieldNames[i];
			if (i<fieldNames.length-1)sql+=",";
			else sql+=") VALUES (";
		}

		for (int i=0;i<fieldNames.length-1;i++) {
			sql+="?";
			if (i<fieldNames.length-1)sql+=",";			 		
		}
		sql+="current_timestamp)";
		return sql;
	}
	
	private static String createSqlInsert(String[] fieldNames,String tableName,String schema) {

		String sql;
		
		if (schema!=null) {
			sql="INSERT INTO "+schema+"."+tableName+" (";	
		} else {
			sql="INSERT INTO "+tableName+" (";
		}
		
		for (int i=0;i<fieldNames.length;i++) {
			sql+=fieldNames[i];
			if (i<fieldNames.length-1)sql+=",";
			else sql+=") VALUES (";
		}

		for (int i=0;i<fieldNames.length;i++) {
			sql+="?";
			if (i<fieldNames.length-1)sql+=",";			 		
			else sql+=")";
		}
		
//		System.out.println(sql);
		return sql;
	}


	public static void batchCreateWithCloning(String tableName, String []fieldNames, List<Object> records, Connection conn) throws SQLException {

		long t1=System.currentTimeMillis();

		String sql = createSqlInsert(fieldNames,tableName,null);	
		PreparedStatement prep = conn.prepareStatement(sql);
				
		int batchSize=1000;
		
		conn.setAutoCommit(false);
		
		List<Object> recordsClone=new ArrayList<>();
		for (Object pd:records) recordsClone.add(pd);//clone the list of records

		while (true) {

			if(recordsClone.size()==0) break;

			List<Object> records2=new ArrayList<>();

			for (int i=1;i<=batchSize;i++) {
				records2.add(recordsClone.remove(0));//remove records from the clone list to not alter original list
				if(recordsClone.size()==0) break;
			}
			
//			System.out.println(records2.size());
			
			addBatch(prep, records2,fieldNames);
			
			
		}
		conn.setAutoCommit(true);

	}
	
	/**
	 * Doesnt clone
	 * 
	 * @param tableName
	 * @param fieldNames
	 * @param records
	 * @param conn
	 * @throws SQLException
	 */

	public static void batchCreate(String tableName, String []fieldNames, List<Object> records, Connection conn) throws SQLException {
		long t1=System.currentTimeMillis();

		String sql = createSqlInsert(fieldNames,tableName,null);	
		PreparedStatement prep = conn.prepareStatement(sql);
				
		int batchSize=1000;
		
		conn.setAutoCommit(false);
		
		List<Object> records2=new ArrayList<>();
		
		for (int i=0;i<records.size();i++) {
			records2.add(records.get(i));//might be slightly slow if have large list since has to seek from start
			if(records2.size() == batchSize) {
				addBatch(prep, records2,fieldNames);
				records2.clear();
			}
		}
		
		//do what's left
		addBatch(prep, records2,fieldNames);

		conn.setAutoCommit(true);

	}

	private static void addBatch(PreparedStatement prep, List<Object> records,String[]fieldNames) throws SQLException {

		for (Object r:records) {
			
			int fieldNum=1;
			
			for (String fieldName:fieldNames) {
				
//				if(fieldName.toLowerCase().equals("created_at")) continue;
				
				try {

					Field myField = r.getClass().getDeclaredField(fieldName);
					String name=myField.getType().getName().toLowerCase();
					
					if (name.contains("double")) {
						
						if(myField.get(r)==null) {
							prep.setNull(fieldNum, Types.DOUBLE);
						} else {
							prep.setDouble(fieldNum, myField.getDouble(r));	
						}
//						System.out.println(fieldNum+"\t"+fieldName+"\t"+myField.getDouble(r));
					} else if (name.contains("bool")) {
						
						if(myField.get(r)==null) {
							prep.setNull(fieldNum, Types.BOOLEAN);
						} else {
							prep.setBoolean(fieldNum, myField.getBoolean(r));	
						}
//						System.out.println(fieldNum+"\t"+fieldName+"\t"+myField.getBoolean(r));
					} else if (name.contains("long")) {

						if(myField.get(r)==null) {
							prep.setNull(fieldNum, Types.BIGINT);
						} else {
							prep.setLong(fieldNum, myField.getLong(r));	
						}
//						System.out.println(fieldNum+"\t"+fieldName+"\t"+myField.getLong(r));
					} else if (name.contains("int")) {
						
						if(myField.get(r)==null) {
							prep.setNull(fieldNum, Types.INTEGER);
						} else {
							prep.setInt(fieldNum, myField.getInt(r));	
						}

//						System.out.println(fieldNum+"\t"+fieldName+"\t"+myField.getInt(r));
					
					} else if (name.contains("date")) {
						
						if(myField.get(r)==null) {
							prep.setNull(fieldNum, Types.TIMESTAMP);
						} else {
							Date date=(Date)(myField.get(r));
							//TODO does this work? Or just dont include date fields in list of fields and rely on hibernate
							java.sql.Timestamp sqlTimeStamp = new java.sql.Timestamp(date.getTime());
							prep.setTimestamp(fieldNum, sqlTimeStamp);
							System.out.println(fieldNum+"\t"+fieldName+"\t"+date);
						}

						
					} else {//string
						
						if(myField.get(r)==null) {
							prep.setNull(fieldNum, Types.VARCHAR);
						} else {
							prep.setString(fieldNum, (String)myField.get(r));	
						}
						
//						System.out.println(fieldNum+"\t"+fieldName+"\t"+myField.get(r));
					}
					
					
					
				} catch (Exception e) {
					e.printStackTrace();
				} 
				
				fieldNum++;
				
			}

			prep.addBatch();

		}
		
		prep.executeBatch();

	}

	
	static class Annotation extends Object {
		public long ANID;
		public String TOCHeading;
		public String Annotation;
		public String Date;
		
//		Annotation(long ANID, String TOCHeading,String Annotation,Date created_at) {
		public Annotation(long ANID, String TOCHeading,String Annotation,String Date) {
			this.ANID=ANID;
			this.TOCHeading=TOCHeading;
			this.Annotation=Annotation;
			this.Date=Date;
		}
	}
	
	public static void main(String[] args) {
		SqlUtilities s=new SqlUtilities();
		
		testBatchCreate(s);
		
	}

	private static void testBatchCreate(SqlUtilities s) {
		String [] fieldNames= {"ANID","TOCHeading","Annotation","Date"};


		try {

			Connection conn=getConnectionSqlite("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\PubChem_2024_11_27\\PubChem_2024_11_27_raw_json_v2_no data.db");

			long t1=System.currentTimeMillis();
			
			List<Object>annotations=new ArrayList<>();

			for (int i=1;i<=10001;i++) {

				Date date = new Date();
				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				String strDate = formatter.format(date);

				annotations.add(new Annotation(i,"Water Solubility","{a}",strDate));	

				if (annotations.size()==1000) {
					s.batchCreate("annotations", fieldNames, annotations, conn);	
					annotations.clear();
				}
			}

			//Do what's left:
			s.batchCreate("annotations", fieldNames, annotations, conn);	

			long t2=System.currentTimeMillis();
			
			System.out.println((t2-t1)+" millisecs");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}