package gov.epa.QSAR.DataSetCreation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

import gov.epa.TEST.Descriptors.DatabaseUtilities.DescriptorDatabaseUtilities;
import gov.epa.TEST.Descriptors.DatabaseUtilities.RecordDescriptors;
import gov.epa.TEST.Descriptors.DatabaseUtilities.RecordDescriptorsMetadata;
import gov.epa.TEST.Descriptors.DatabaseUtilities.SQLite_GetRecords;
import gov.epa.TEST.Descriptors.DatabaseUtilities.StringCompression;
import gov.epa.api.ExperimentalConstants;
import gov.epa.database.SQLite_Utilities;
import weka.core.Instances;
import weka.core.converters.CSVLoader;


public class DataSetDatabaseUtilities {
	
	public static String pathDataSetDB="databases/datasets.db";
	
	
	static ArrayList<String> getCIDandProperty (String propertyName,String splitting,String t_p) {

		ArrayList<String>values=new ArrayList<>();
		try {
			
			Connection conn=SQLite_Utilities.getConnection(pathDataSetDB);
			Statement stat=conn.createStatement();	
			
			String sql="Select OverallSets.DSSTox_Structure_Id, OverallSets.property_value_point_estimate_qsar\n";
			sql+="FROM OverallSets\n";			
			sql+="INNER JOIN Splittings on OverallSets.DSSTox_Structure_Id = Splittings.DSSTOX_Structure_Id\n";
			sql += "WHERE OverallSets.property_name=\"" + propertyName + "\""
					+ " and Splittings.splitting=\""+splitting+"\""+
					" and Splittings.t_p=\""+t_p+"\"";
			sql+="ORDER BY OverallSets.DSSTox_Structure_Id;";
			System.out.println(sql);
			ResultSet rs=SQLite_GetRecords.getRecords(stat, sql);
			
			while (rs.next()) {
				values.add(rs.getString(1)+"\t"+rs.getString(2));
			}
			
//			for (int i=0;i<values.size();i++) {
//				System.out.println((i+1)+"\t"+values.get(i));
//			}
		
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return values;
		

	}
	
	public static ArrayList<String> getStringInstancesOneDB (String propertyName,String splitting,String t_p,String software) {

		ArrayList<String>values=new ArrayList<>();
		try {
			
			Connection conn=SQLite_Utilities.getConnection(pathDataSetDB);
			Statement stat=conn.createStatement();	
			
			String sql = getSQLCreateInstances(propertyName, splitting, t_p, software);
			ResultSet rs=SQLite_GetRecords.getRecords(stat, sql);
			
			while (rs.next()) {
				values.add(rs.getString(1)+"\t"+rs.getString(2)+"\t"+rs.getString(3));
			}
			
//			for (int i=0;i<values.size();i++) {
//				System.out.println((i+1)+"\t"+values.get(i));
//			}
		
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return values;
		

	}
	
	public static String getInstancesAsStringOneDB (String propertyName,String splitting,String t_p,String software,Connection conn) {

		String values="";
		try {
			
			Statement stat=conn.createStatement();	
			
			String descriptorNames=RecordDescriptorsMetadata.getDescriptorNames(stat, software);
			
			values="ID\tProperty\t"+descriptorNames+"\r\n";
			
			String sql = getSQLCreateInstances(propertyName, splitting, t_p, software);
			ResultSet rs=SQLite_GetRecords.getRecords(stat, sql);
			
			int counter=0;		
			while (rs.next()) {
				counter++;
				String strDescriptors=null;
				if (RecordDescriptors.compressDescriptorsInDB) {
					byte [] bytes=rs.getBytes(3);				
					strDescriptors=StringCompression.decompress(bytes, Charset.forName("UTF-8"));
				} else {
					strDescriptors=rs.getString(3);
				}

				values+=(rs.getString(1)+"\t"+rs.getString(2)+"\t"+strDescriptors)+"\r\n";
				
//				if (counter==1000) break;
			}
			
//			System.out.println(values);
			
//			for (int i=0;i<values.size();i++) {
//				System.out.println((i+1)+"\t"+values.get(i));
//			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return values;
		

	}
	
	public static Vector<String> getID_Property_Smiles_AsString (String propertyName,String splitting,String t_p,Connection conn) {
		Vector<String> values=new Vector<>();
		try {
			
			Statement stat=conn.createStatement();	
						
			values.add("ID\tProperty\tSmiles");
			
			String sql = getSQL_ID_Property_Smiles(propertyName, splitting, t_p);
			ResultSet rs=SQLite_GetRecords.getRecords(stat, sql);			
					
			while (rs.next()) {				
				values.add(rs.getString(1)+"\t"+rs.getString(2)+"\t"+rs.getString(3));
			}
			
//			for (int i=0;i<values.size();i++) {
//				System.out.println((i+1)+"\t"+values.get(i));
//			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return values;
		

	}

	
	public static String getInstancesAsStringOneDB (String propertyName,String software,String dbpath) {
		return getInstancesAsStringOneDB(propertyName, software, dbpath, null);
	}
	
	
	public static Instances getInstances (String property,String software,String dbpath,Vector<String>IDs) {
		String strInstances=DataSetDatabaseUtilities.getInstancesAsStringOneDB(property, software, dbpath,IDs);

		System.out.println("Done getting instances as String from db tables");	
		Instances instances=getInstances(strInstances);
		instances.setClassIndex(0);
		return instances;
	}
	
	public static Instances getInstances (String property,String software,String dbpath) {
		String strInstances=DataSetDatabaseUtilities.getInstancesAsStringOneDB(property, software, dbpath);

		System.out.println("Done getting instances as String from db tables");	
		Instances instances=getInstances(strInstances);
		instances.setClassIndex(0);
		return instances;
	}

	
	public static Instances getInstances(String strInstances) {
		InputStream inputStream = new ByteArrayInputStream(strInstances.getBytes());
    
		CSVLoader atf = new CSVLoader(); 
		
		try {					
			atf.setSource(inputStream);
			atf.setFieldSeparator("\t");
			Instances dataset = atf.getDataSet();		
			return dataset;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get instances as a single line delimited string (tabs between fields)
	 * 
	 * @param propertyName
	 * @param software
	 * @param dbpath
	 * @param ids - vector of ids for just the ids we want to get
	 * @return
	 */
	public static String getInstancesAsStringOneDB (String propertyName,String software,String dbpath,Vector<String>ids) {

		String values="";
		try {
			Connection conn=SQLite_Utilities.getConnection(dbpath);
			Statement stat=conn.createStatement();	
			
			String descriptorNames=RecordDescriptorsMetadata.getDescriptorNames(stat, software);
			
			values="ID\tProperty\t"+descriptorNames+"\r\n";
			
						
			String sql = getSQLCreateInstances(propertyName, software);
			ResultSet rs=SQLite_GetRecords.getRecords(stat, sql);
			
					
			while (rs.next()) {
				
				String strDescriptors=null;
				if (RecordDescriptors.compressDescriptorsInDB) {
					byte [] bytes=rs.getBytes(3);				
					strDescriptors=StringCompression.decompress(bytes, Charset.forName("UTF-8"));
				} else {
					strDescriptors=rs.getString(3);
				}
				
				if (strDescriptors.toLowerCase().contains("error")) continue;

				if (ids==null || ids.contains(rs.getString(1)))
					values+=(rs.getString(1)+"\t"+rs.getString(2)+"\t"+strDescriptors)+"\r\n";
			}
			
//			for (int i=0;i<values.size();i++) {
//				System.out.println((i+1)+"\t"+values.get(i));
//			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return values;
		

	}

	/**
	 * Get training or prediction set as string
	 * 
	 * @param propertyName
	 * @param splitting
	 * @param t_p
	 * @param software
	 * @return
	 */
	private static String getSQLCreateInstances(String propertyName, String splitting, String t_p, String software) {
		String sql = "Select OverallSets.DSSTox_Structure_Id, OverallSets.property_value_point_estimate_qsar, Descriptors.Descriptors\n"
				+ "FROM OverallSets\n"
				+ "INNER JOIN Splittings on OverallSets.DSSTox_Structure_Id = Splittings.DSSTOX_Structure_Id\n"
				+ "INNER JOIN Descriptors on OverallSets.DSSTox_Structure_Id = Descriptors.DSSTOX_Structure_Id\n"
				+ "WHERE OverallSets.property_name=\"" + propertyName + "\"" + 
				" and Splittings.splitting=\""+ splitting + "\"" + " and Splittings.t_p=\"" + t_p + "\" and Splittings.property_name=\""+propertyName+"\""+ 
				" and Descriptors.DescriptorSoftware=\""
				+ software + "\"\n" + "ORDER BY OverallSets.DSSTox_Structure_Id;";			
		
//		System.out.println(sql);
		return sql;
	}
	
	/**
	 * Get training or prediction set as string
	 * 
	 * @param propertyName
	 * @param splitting
	 * @param t_p
	 * @param software
	 * @return
	 */
	private static String getSQL_ID_Property_Smiles(String propertyName, String splitting, String t_p) {
		String sql = "Select OverallSets.DSSTox_Structure_Id, OverallSets.property_value_point_estimate_qsar,OverallSets.Structure_Smiles_2D_QSAR\n"
				+ "FROM OverallSets\n"
				+ "INNER JOIN Splittings on OverallSets.DSSTox_Structure_Id = Splittings.DSSTOX_Structure_Id\n"
				+ "WHERE OverallSets.property_name=\"" + propertyName + "\"" + 
				" and Splittings.splitting=\""+ splitting + "\"" + " and Splittings.t_p=\"" + t_p + "\" and Splittings.property_name=\""+propertyName+"\""+ 
				  "\n" + "ORDER BY OverallSets.DSSTox_Structure_Id;";			
		
//		System.out.println(sql);
		return sql;
	}

	/**
	 * Get overall set instances as string
	 * 
	 * @param propertyName
	 * @param splitting
	 * @param software
	 * @return
	 */
	public static String getSQLCreateInstances(String propertyName, String software) {
		String sql = "Select OverallSets.DSSTox_Structure_Id, OverallSets.property_value_point_estimate_qsar, Descriptors.Descriptors\n"
				+ "FROM OverallSets\n"
				+ "INNER JOIN Descriptors on OverallSets.DSSTox_Structure_Id = Descriptors.DSSTOX_Structure_Id\n"
				+ "WHERE OverallSets.property_name=\"" + propertyName + "\"" + " and Descriptors.DescriptorSoftware=\""
				+ software + "\"\n" + "ORDER BY OverallSets.DSSTox_Structure_Id;";			
		
//		System.out.println(sql);
		return sql;
	}
	
	
	public static void main(String[] args) {
		long t1=System.currentTimeMillis();
		String software=RecordDescriptorsMetadata.softwareTEST;
		String propertyName=ExperimentalConstants.strRatInhalationLC50;
		String splitting="rnd1";
		String t_p="T";
		
		Connection conn=SQLite_Utilities.getConnection(pathDataSetDB);
		
		String instances=getInstancesAsStringOneDB(propertyName, splitting, t_p, software, conn);
		
		System.out.println(instances);
		
		long t2=System.currentTimeMillis();
		System.out.println("time to load instances="+(t2-t1)/1000.0+" secs");
	}

	private static ArrayList<String> getStringInstances(String property,String splitting,String t_p,String software) {

		ArrayList<String>instances=new ArrayList<>();
		ArrayList<String>CID_Props=getCIDandProperty(property,splitting,t_p);
		
		try {
			Connection connDescriptors=SQLite_Utilities.getConnection(DescriptorDatabaseUtilities.filepathDB);
			Statement stat=connDescriptors.createStatement();
			
			for (String CID_Prop:CID_Props) {
				String [] vals=CID_Prop.split("\t");
				String CID=vals[0];
				String Prop=vals[1];

				String descriptors=RecordDescriptors.lookupDescriptorsInDatabase(connDescriptors, CID, software);
				String instance=CID_Prop+"\t"+descriptors;
				
				instances.add(instance);
//				System.out.println(instance);
			}

			//		RecordsQSAR records=getExperimentalRecordsFromDB(ExperimentalConstants.strRatInhalationLC50);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return instances;
	}
	
	
	
	
	

	
	
	
}
