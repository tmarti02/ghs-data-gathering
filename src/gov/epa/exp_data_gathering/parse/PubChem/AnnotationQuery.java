package gov.epa.exp_data_gathering.parse.PubChem;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
import gov.epa.database.SqlUtilities;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Value;
import gov.epa.exp_data_gathering.parse.PubChem.ParseNewDatabase.DB_Annotation;
import gov.epa.exp_data_gathering.parse.PubChem.ParseNewDatabase.DB_Annotation_CID;
import gov.epa.exp_data_gathering.parse.PubChem.ParseNewDatabase.DB_Identifier;


public class AnnotationQuery {

	Annotations Annotations;
	
	
	public class Annotation {
		
		String SourceName;
		String SourceID;
		String Name;
		String Description;
		String URL;
		String LicenseURL;
		
		@SerializedName("Data")
		List<AnnotationData> data;
		
		Long ANID;
		
		@SerializedName("LinkedRecords")
		LinkedRecords linkedRecords;

		
		String getKey() {
			return ANID+"\t"+data.get(0).TOCHeading.TOCHeading;
		}

	}
	
	
	
	public class LinkedRecords {
		@SerializedName("CID")
		public long [] cids;
	}

	
	class AnnotationData {
		TOCHeading TOCHeading;
		String Description;
		
		@SerializedName("Reference")
		String [] references;
		
		@SerializedName("Value")
		Value value;
	}
	
	
	class TOCHeading {
		String type;
		@SerializedName("#TOCHeading")
		String TOCHeading;
	}
	
	
	
	public class Annotations {
		List<Annotation> Annotation;
		int Page;
		int TotalPages;
	}
	
	
	/**
	 * Loads missing identifier records for records in annotation_cids
	 * TODO make it refresh entries that are too old
	 * 
	 */
	public void loadIdentifiersForAnnotation_cids() {

		long sleep=200;
		int batchSize=10;

		String folderMain="data\\experimental\\PubChem_2024_11_27\\";
		String databasePath=folderMain+"PubChem_2024_11_27_raw_json_v2.db";

		Connection conn= SQLite_Utilities.getConnection(databasePath);

		try {
			conn.setAutoCommit(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Statement stat=SQLite_Utilities.getStatement(conn);
		HashSet<Long> CIDsAnnotationCIDs = ParseNewDatabase.getCIDsInDB(stat,"annotation_cids");
		HashSet<Long> CIDsInIdentifiers = ParseNewDatabase.getCIDsInDB(stat,"identifiers");

		HashSet<Long> CIDsToLoad=new HashSet<>();

		for (long cid:CIDsAnnotationCIDs) {
			if(!CIDsInIdentifiers.contains(cid)) {
				CIDsToLoad.add(cid);
			}
		}

		DB_Identifier.loadIdentifiers(sleep, batchSize, conn, CIDsToLoad);

		//		System.out.println(CIDs.size());

	}
	
	/**
	 * Load annotation jsons to sqlite db
	 * 
	 */
	public void loadFromAnnotationJsons() {
		
		String folderMain="data\\experimental\\PubChem_2024_11_27\\";
		String folderPath=folderMain+"\\json\\physchem\\";
		String databasePath=folderMain+"PubChem_2024_11_27_raw_json_v2.db";

		Connection conn= SQLite_Utilities.getConnection(databasePath);

		try {
			conn.setAutoCommit(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		//		String annotationFilePath=folder+"Henry's Law Constant 1.json";
		//		String annotationFilePath=folder+"Henry's Law Constant 2.json";
		//		r.loadAnnotationFile(annotationFilePath,conn);

		File folder=new File(folderPath);

		Statement stat=SQLite_Utilities.getStatement(conn);

		HashSet<String> keysAnnotations = getANID_TOCHeadingsInDB(stat);
		HashSet<String> keysAnnotationCids = getANID_CIDInDB(stat);

		System.out.println(keysAnnotations.size());

		for (File file:folder.listFiles()) {
			if(!file.getName().contains(".json")) continue;
			loadAnnotationFile(file.getAbsolutePath(), conn, keysAnnotations,keysAnnotationCids);
			System.out.println(file.getName()+"\t"+keysAnnotations.size());
		}


	}


	private HashSet<String> getANID_TOCHeadingsInDB(Statement stat) {
		String sql="select ANID,TOCHeading from annotations;";
		ResultSet rs=SQLite_GetRecords.getRecords(stat, sql);
		HashSet<String>keys=new HashSet<>();

		try {
			while (rs.next()) {
				long ANID=rs.getLong(1);
				String TOCHeading=rs.getString(2);
				keys.add(ANID+"\t"+TOCHeading);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return keys;
	}

	private HashSet<String> getANID_CIDInDB(Statement stat) {
		String sql="select ANID,cid from annotation_cids;";
		ResultSet rs=SQLite_GetRecords.getRecords(stat, sql);
		HashSet<String>keys=new HashSet<>();

		try {
			while (rs.next()) {
				long ANID=rs.getLong(1);
				long cid=rs.getLong(2);
				keys.add(ANID+"\t"+cid);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return keys;
	}


	private void loadAnnotationFile(String filepath, Connection conn,HashSet<String>keysAnnotations, HashSet<String> keysAnnotationCids) 
	{
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		//		Gson gson = builder.setPrettyPrinting().disableHtmlEscaping().create();

		try {
			//			JsonObject jo=gson.fromJson(new FileReader(filepath), JsonObject.class);

			AnnotationQuery aq=gson.fromJson(new FileReader(filepath), AnnotationQuery.class);
			//			if(true) return;

			//			System.out.println(jaAnnotations.size());

			String [] fieldNamesAnnotation= {"ANID","TOCHeading","Annotation","Date"};
			String [] fieldNamesAnnotationCID= {"ANID","cid"};


			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			Date date = getDate(filepath);
			String strDate = formatter.format(date);

			//			System.out.println(strDate);

			List<Annotation> annotations=aq.Annotations.Annotation;

			//List of records that will be added to db when hit 1000 records:
			List<Object>db_annotations=new ArrayList<>();
			List<Object>db_annotation_cids=new ArrayList<>();


			for (Annotation annotation:annotations) {

				//				System.out.println(annotation.getKey()+"\t"+keysAnnotations.contains(annotation.getKey()));

				if (keysAnnotations.contains(annotation.getKey())) continue;

				keysAnnotations.add(annotation.getKey());

				if (annotation.linkedRecords==null) continue;
				if (annotation.linkedRecords.cids==null) continue;

				String strAnnotation=gson.toJson(annotation);
				String TOCHeading=annotation.data.get(0).TOCHeading.TOCHeading;
				TOCHeading=TOCHeading.replace("'", "''");

				DB_Annotation db_annotation=new DB_Annotation(annotation.ANID,TOCHeading,strAnnotation,strDate);

				db_annotations.add(db_annotation);

				if(db_annotations.size()==1000) {
					SqlUtilities.batchCreate("annotations", fieldNamesAnnotation, db_annotations, conn);
					db_annotations.clear();
				}


				//				Object [] values= {annotation.ANID,TOCHeading,strAnnotation,strDate};
				//				SQLite_CreateTable.addDataToTable("annotations", fieldNamesAnnotation, values, conn);

				for (long cid:annotation.linkedRecords.cids) {
					String key=annotation.ANID+"\t"+cid;
					if(keysAnnotationCids.contains(key)) continue;

					DB_Annotation_CID db_annotation_cid=new DB_Annotation_CID(annotation.ANID,cid);
					db_annotation_cids.add(db_annotation_cid);

					if(db_annotation_cids.size()==1000) {
						SqlUtilities.batchCreate("annotation_cids", fieldNamesAnnotationCID, db_annotation_cids, conn);
						db_annotation_cids.clear();
					}

					//					Object [] valuesCid= {annotation.ANID,cid};
					//					SQLite_CreateTable.addDataToTable("annotation_cids", fieldNamesAnnotationCID, valuesCid, conn);


					keysAnnotationCids.add(key);

				}

				//				if(true)break;
				//				System.out.println(gson.toJson(joAnnotation));
				//				System.out.println(cids+"\t"+cids.size()+"\n");	
			}//end loop over annotations in file

			//Do what's left:
			SqlUtilities.batchCreate("annotations", fieldNamesAnnotation, db_annotations, conn);
			SqlUtilities.batchCreate("annotation_cids", fieldNamesAnnotationCID, db_annotation_cids, conn);


			//			System.out.println(cidsAll.size());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	Date getDate (String filepath) {

		File file = new File(filepath);
		Path filePath = file.toPath();

		BasicFileAttributes attributes = null;
		try {
			attributes = Files.readAttributes(filePath, BasicFileAttributes.class);
		} catch (IOException exception) {
			System.out.println("Exception handled when trying to get file " +
					"attributes: " + exception.getMessage());
		}

		Date creationDate =
				new Date(attributes.creationTime().to(TimeUnit.MILLISECONDS));

		return creationDate;
	}

	
	public static void main(String[] args) {
		AnnotationQuery r = new AnnotationQuery();
//		r.loadFromAnnotationJsons();
		r.loadIdentifiersForAnnotation_cids();
	}


}
