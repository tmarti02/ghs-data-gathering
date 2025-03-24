package gov.epa.ghs_data_gathering.Parse.ToxVal;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SqlUtilities {
	
	private static Map<String, Connection> connPool = new HashMap<>();
	
	public static final String dbToxVal="dbToxVal";
	public static final String dbDsstox="dbDsstox";
	public static final String dbPostGres="Postgres";
	
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

	public static Connection getConnectionToxVal() {
		
		try {
			if (connPool.containsKey(dbToxVal) && connPool.get(dbToxVal) != null && !connPool.get(dbToxVal).isClosed()) {
//				System.out.println("here");
				return connPool.get(dbToxVal);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String db = System.getenv().get("TOXVAL_DATABASE");
		String host = System.getenv().get("TOXVAL_HOST");
		String port = System.getenv().get("TOXVAL_PORT");
		
//		String db = "prod_toxval_v93";
		
//		System.out.println(db);
		
//		String db = "20230417_toxval_v94";

		String url = "jdbc:mysql://" + host + ":" + port + "/" + db;
		
		
		String user = System.getenv().get("TOXVAL_USER");
		String password = System.getenv().get("TOXVAL_PASS");

		System.out.println(url);
//		System.out.println(user+"\t"+password);

		
		try {
			Connection conn = DriverManager.getConnection(url, user, password);
			connPool.put(dbToxVal, conn);
			return conn;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	

	public static Connection getConnectionDsstox() {
		
		try {
			if (connPool.containsKey(dbDsstox) && connPool.get(dbDsstox) != null && !connPool.get(dbDsstox).isClosed()) {
//				System.out.println("here");
				return connPool.get(dbDsstox);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String db = System.getenv().get("DSSTOX_DATABASE");
		String host = System.getenv().get("DSSTOX_HOST");
		String port = System.getenv().get("DSSTOX_PORT");
		
//		String db = "prod_toxval_v93";
		
//		System.out.println(db);
		
//		String db = "20230417_toxval_v94";

		String url = "jdbc:mysql://" + host + ":" + port + "/" + db;
		
		
		String user = System.getenv().get("DSSTOX_USER");
		String password = System.getenv().get("DSSTOX_PASS");

//		System.out.println(url);
//		System.out.println(user+"\t"+password);

		
		try {
			Connection conn = DriverManager.getConnection(url, user, password);
			connPool.put(dbToxVal, conn);
			return conn;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
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
			Statement st = conn.createStatement();			
			st.executeUpdate(sql);
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

	private static void addBatch(PreparedStatement prep, List<Object> records,String[]fieldNames) throws SQLException {
	
		for (Object r:records) {
	
			int fieldNum=1;
	
			for (String fieldName:fieldNames) {
	
				//				if(fieldName.toLowerCase().equals("created_at")) continue;
	
				try {
	
					Field myField = r.getClass().getDeclaredField(fieldName);
					String name=myField.getType().getName().toLowerCase();
	
					//					System.out.println(myField.getType().getName());
	
					if (name.contains("double")) {
	
						if(myField.get(r)==null) {
							prep.setNull(fieldNum, Types.DOUBLE);
						} else {
							//							prep.setDouble(fieldNum, myField.getDouble(r));
							prep.setDouble(fieldNum, (Double)myField.get(r));
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
						prep.setString(fieldNum, (String)myField.get(r));
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
	
		//		ResultSet keys=prep.getGeneratedKeys();
		//
		//		Iterator<Object> iterator=records.iterator();
		//		while (keys!=null && keys.next()) {
		//			Object obj=iterator.next();
		//			Long key = keys.getLong(1);
		//			
		//	        try {
		//				Class<?> objClass = obj.getClass();
		//		        Method getNameMethod = objClass.getDeclaredMethod("setId");
		//		        getNameMethod.setAccessible(true);
		//				getNameMethod.invoke(obj,key);
		//
		//			} catch (Exception e) {
		//				e.printStackTrace();
		//			}
		//			
		//		}
	
	}

	public static void batchCreate(String tableName,String schema, String []fieldNames, List<? extends Object> records, Connection conn) throws SQLException {
	
		if(records.size()==0) {
			return;
		}
		long t1=System.currentTimeMillis();
	
		String sql = createSqlInsert(fieldNames,tableName,schema);	
	
		PreparedStatement prep = conn.prepareStatement(sql);
	
		//		PreparedStatement prep = conn.prepareStatement(sql, new String[]{"id"});//for some reason much faster than using Statement.RETURN_GENERATED_KEYS!
	
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

	//	ParseToxValDB p=new ParseToxValDB();
	
	public static String createSqlInsert(String[] fieldNames,String tableName,String schema) {
	
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
	
	
	
	
}