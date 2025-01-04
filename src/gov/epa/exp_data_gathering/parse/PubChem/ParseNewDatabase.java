package gov.epa.exp_data_gathering.parse.PubChem;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
import gov.epa.database.SqlUtilities;
import gov.epa.exp_data_gathering.parse.JSONUtilities;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.PublicSource;
import gov.epa.exp_data_gathering.parse.UnitConverter;
import gov.epa.exp_data_gathering.parse.PubChem.RecordPubChem.MarkupChemical;
import gov.epa.ghs_data_gathering.Utilities.FileUtilities;
import gov.epa.exp_data_gathering.parse.PubChem.AnnotationQuery.Annotation;
import gov.epa.exp_data_gathering.parse.PubChem.AnnotationQuery.AnnotationData;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Data;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.IdentifierData;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Information;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Markup;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Property;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Reference;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.StringWithMarkup;

/**
 * 
 * select a.ANID, a.Annotation, a.TOCHeading, a.Date,ac.cid,i.identifiers,i.cas,i.synonyms from annotations a
left join annotation_cids ac on  a.ANID=ac.ANID
left join identifiers i on i.cid=ac.cid
-- where ac.cid=241
where a.ANID=9155079
order by TOCHeading;
 * 
 * @author TMARTI02
 */
public class ParseNewDatabase {


	static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues()
			.create();

	private static final transient UnitConverter unitConverter = new UnitConverter("data/density.txt");



	public void parseJSONsInDatabase() {
		
		String sourceName=RecordPubChem.sourceName;
		String databaseFolder = "Data" + File.separator + "Experimental" + File.separator + sourceName;
		String databasePath = databaseFolder + File.separator + sourceName + "_raw_json_v2.db";

		Connection conn=SqlUtilities.getConnectionSqlite(databasePath);
		
		ResultSet rs=SqlUtilities.runSQL2(conn, "select distinct TOCHeading from annotations;");
		
		try {
			while (rs.next()) {
				String heading=rs.getString(1);
				List<RecordPubChem>records=parseJSONsInDatabase(heading);
				String heading2=heading.replace("''", "'");
				String filepath=databaseFolder+File.separator+"Original Records "+heading2+".json";
				JSONUtilities.batchAndWriteJSON(records, filepath);
				System.out.println(filepath+"\t"+records.size());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	protected List<RecordPubChem> parseJSONsInDatabase(String heading) {

		String sourceName=RecordPubChem.sourceName;

		String databaseFolder = "Data" + File.separator + "Experimental" + File.separator + sourceName;
		String databasePath = databaseFolder + File.separator + sourceName + "_raw_json_v2.db";
		
		List<RecordPubChem> records = new ArrayList<>();

		try {
			Statement stat = SQLite_Utilities.getStatement(databasePath);

			//			ResultSet rs = SQLite_GetRecords.getAllRecords(stat, sourceName);

			String sql="select a.Annotation,  a.Date,ac.cid,i.identifiers,i.cas,i.synonyms from annotations a\r\n"
					+ "left join annotation_cids ac on  a.ANID=ac.ANID\r\n"
					+ "left join identifiers i on i.cid=ac.cid\r\n"
					+ "where a.TOCHeading='"+heading.replace("'","''")+"';";


			ResultSet rs = SQLite_GetRecords.getRecords(stat, sql);

			int counter = 0;

			System.out.println("Going through "+heading +" records in " + databasePath);

			while (rs.next()) {

				counter++;

				if (counter % 1000 == 0) {
					System.out.println(counter);
				}

				Annotation aq=gson.fromJson(rs.getString("Annotation"), Annotation.class);

				String date=rs.getString("Date");
				Long cid=rs.getLong("cid");
				String synonyms=rs.getString("synonyms");
				if(synonyms!=null) {
					synonyms=synonyms.replaceAll("\r\n", "|").replace("\n","").replace("\r","");	
				}

				try {
					IdentifierData identifierData = gson.fromJson(rs.getString("identifiers"), IdentifierData.class);
					Data casData = gson.fromJson(rs.getString("cas"), Data.class);
					getRecords(records,date, cid,aq,casData,identifierData,synonyms);
				} catch (Exception ex) {
					System.out.println("Cant parse json for ANID="+aq.ANID+", cid="+cid);
				}
				//				if(true) break;

			} // end loop over records
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}

	


	/**
	 * 
	 * @param records
	 * @param date
	 * @param cid
	 * @param aq
	 * @param casData
	 * @param identifierData
	 * @param synonyms2
	 * @throws SQLException
	 */
	private void getRecords(List<RecordPubChem> records, String date,Long cid, Annotation aq, Data casData, IdentifierData identifierData, String synonyms) throws SQLException {

		Hashtable<Long,String> htCASByANID=getCasLookupByANID(casData);

		for(AnnotationData data:aq.data) {

			List<StringWithMarkup> valueStrings = data.value.stringWithMarkup;

			if (valueStrings == null) {
				//				System.out.println("no value string for ANID="+aq.ANID);
				continue;
			}

			for (StringWithMarkup valueString : valueStrings) {

				if (valueString.string == null)
					continue;

				RecordPubChem pcr = new RecordPubChem();
				pcr.date_accessed = date.substring(0, date.indexOf(" "));
				pcr.cid = cid;
				pcr.ANID=aq.ANID;

				pcr.propertyName=data.TOCHeading.TOCHeading;

				pcr.propertyValue = valueString.string;				
				if(pcr.propertyValue!=null) pcr.propertyValue=pcr.propertyValue.trim();
				//
				addMarkupChemicals(valueString, pcr);
				//
				pcr.addIdentifiers(identifierData, synonyms);
				addSourceMetadata(aq,data, pcr, htCASByANID);
				records.add(pcr);

				//				System.out.println(gson.toJson(pcr));
			}
		}

	}

	private void addSourceMetadata(Annotation annotation, AnnotationData data, RecordPubChem pcr, Hashtable<Long, String> htCASByANID) {

		pcr.chemicalNameReference=annotation.Name;
		pcr.casReference=htCASByANID.get(annotation.ANID);
		//		System.out.println(annotation.ANID+"\t"+htCASByANID.get(annotation.ANID));

		pcr.publicSourceOriginal = new PublicSource();
		pcr.publicSourceOriginal.name = annotation.SourceName;
		pcr.publicSourceOriginal.description = annotation.Description;
		pcr.publicSourceOriginal.url = annotation.URL;// TODO fix these to remove specific page
		pcr.publicSourceOriginal.licenseUrl = annotation.LicenseURL;// TODO fix these to remove specific page


		if (data.references != null) {
			pcr.literatureSource = new LiteratureSource();

			String citation1 = null;
			String citation2 = null;

			for (String reference : data.references) {

				if (reference.contains("PMID:")) {

					if (reference.indexOf("PMID:") == 0) {
						String pmid = reference.substring(reference.indexOf(":") + 1, reference.length());
						pcr.literatureSource.url = "https://pubmed.ncbi.nlm.nih.gov/" + pmid + "/";
						//						System.out.println(pcr.literatureSource.doi);
					} else if (reference.indexOf("DOI") > -1) {

						if (reference.indexOf("PMID") > -1) {
							String doi2 = reference.substring(reference.indexOf("DOI:") + 4, reference.length());
							doi2 = doi2.substring(0, doi2.indexOf(" ") - 1).trim();
							doi2 = "https://doi.org/" + doi2;
							pcr.literatureSource.doi = doi2;

						} else {
							System.out.println("Here2\treference=" + reference);
						}

						citation1 = reference.substring(0, reference.indexOf("DOI"));
						pcr.literatureSource.citation = citation1;

						if (reference.indexOf("PMID:") > 0) {
							//							System.out.println(reference);
							String pmid = reference.substring(reference.indexOf("PMID:") + 5, reference.length());
							pcr.literatureSource.url = "https://pubmed.ncbi.nlm.nih.gov/" + pmid + "/";
							//							System.out.println(pcr.literatureSource.url);
						}
					} else {
						//						System.out.println("Here3\treference="+reference);
						pcr.literatureSource.citation = reference;
					}

				} else if (reference.contains("Tested as SID")) {
					pcr.notes = reference;
					//					System.out.println(pcr.notes);
				} else {
					citation2 = reference;
					pcr.literatureSource.citation = citation2;
					//					System.out.println(citation2);
				}
			}

			//			if (citation1!=null && citation2!=null) {
			//				System.out.println("citation1="+citation1);
			//				System.out.println("citation2="+citation2+"\n");
			//			}
			//			System.out.println("pcr.notes="+pcr.notes+"\n");
			//			if (information.reference.size() > 1) {
			//				System.out.println(gson.toJson(pcr.literatureSource));
			//			}
		}
	}


	private static void addSourceMetadata(Hashtable<Integer, Reference> htReferences, Information information,
			RecordPubChem pcr, Hashtable<String, String> htCAS) {

		if (information.referenceNumber != null) {
			int refNum = Integer.parseInt(information.referenceNumber);

			Reference reference = htReferences.get(refNum);
			pcr.publicSourceOriginal = new PublicSource();
			pcr.publicSourceOriginal.name = reference.sourceName;
			pcr.publicSourceOriginal.description = reference.description;
			pcr.publicSourceOriginal.url = reference.url;// TODO fix these to remove specific page

			if(htCAS.containsKey(information.referenceNumber)) {
				pcr.casReference=htCAS.get(information.referenceNumber);
			} else {
				//				System.out.println("cant get cas from ref num:"+information.referenceNumber+"\t"+pcr.cid);
			}





			//			System.out.println(gson.toJson(reference));
		}
	}

	


	private void addMarkupChemicals(StringWithMarkup valueString, RecordPubChem pcr) {
		if(valueString.Markup!=null) {
			pcr.markupChemicals=new ArrayList<MarkupChemical>();
			for (Markup m:valueString.Markup) {
				MarkupChemical mc=pcr.new MarkupChemical();

				if (m.Extra!=null && m.Extra.indexOf("CID-")==0) {
					mc.cid=m.Extra.substring(4,m.Extra.length());	
				}

				if(m.URL!=null && m.URL.contains("compound")) 
					mc.name=m.URL.replace("https://pubchem.ncbi.nlm.nih.gov/compound/", "");	
				else if (m.URL!=null && m.URL.contains("element"))
					mc.name=m.URL.replace("https://pubchem.ncbi.nlm.nih.gov/element/", "");

				pcr.markupChemicals.add(mc);
			}
		}
	}

	static Hashtable<Long,String> getCasLookupByANID(Data casData) {

		Hashtable<Long,String> htCASByANID=new Hashtable<Long,String>();//lookup cas based on reference number
		if (casData == null) return htCASByANID; 

		Hashtable<Long,String> htCASByRefNum=new Hashtable<Long,String>();//lookup cas based on reference number

		List<Information> casInfo = casData.record.section.get(0).section.get(0).section.get(0).information;

		for (Information c : casInfo) {
			String newCAS = c.value.stringWithMarkup.get(0).string;
			Long refNum = Long.parseLong(c.referenceNumber);
			//			String refNum = c.referenceNumber;
			htCASByRefNum.put(refNum, newCAS);
		}

		//		for (long refNum:htCASByRefNum.keySet()) {
		//			System.out.println(refNum+"\t"+htCASByRefNum.get(refNum));
		//		}

		List<Reference> references = casData.record.reference;
		for (Reference reference:references) {

			Long refNum = Long.parseLong(reference.referenceNumber);
			//			String refNum = reference.referenceNumber;
			//			System.out.println(refNum+"\t"+htCASByRefNum.get(refNum));

			Long ANID=Long.parseLong(reference.ANID);

			if (htCASByRefNum.containsKey(refNum)) {
				htCASByANID.put(ANID, htCASByRefNum.get(refNum));
				//				System.out.println(ANID+"\t"+htCASByRefNum.get(refNum));
			}
		}

		return htCASByANID;
	}
	
	
	
	
	
	

	private void testParseFromAnnotation() {
		String folder="data/experimental/"+RecordPubChem.sourceName+"/";
		try {
			Annotation aq=gson.fromJson(new FileReader(folder+"annotation 241.json"), Annotation.class);
			Data casData = gson.fromJson(new FileReader(folder+"cas 241.json"), Data.class);
			IdentifierData identifierData = gson.fromJson(new FileReader(folder+"id 241.json"), IdentifierData.class);
			String synonyms=new String(Files.readAllBytes(Paths.get(folder+"synonyms 241.txt")), StandardCharsets
					.UTF_8).replace("\n", "|");

			List<RecordPubChem> records=new ArrayList<>();
			String date="12/09/2024 16:44:34";
			Long cid=241L;
			getRecords(records,date, cid,aq,casData,identifierData,synonyms);
			//			System.out.println(gson.toJson(aq));
			//			System.out.println(gson.toJson(casData));
			//			System.out.println(gson.toJson(identifierData));

		} catch (Exception e) {
			e.printStackTrace();
		} 
	}




	public static class DB_Identifier {

		public String date;
		public long cid;
		public String identifiers;
		public String cas;
		public String synonyms;

		public DB_Identifier (String date,long cid,String identifiers,String cas,String synonyms) {
			this.date=date;
			this.cid=cid;
			this.identifiers=identifiers;
			this.cas=cas;
			this.synonyms=synonyms;
		}
		
		public static Hashtable<Long,DB_Identifier> getIdentifierHashtable(Connection conn) {
			
			Hashtable<Long,DB_Identifier>ht=new Hashtable<>();

			
			String sql="select * from identifiers;";
			
			ResultSet rs=SqlUtilities.runSQL2(conn, sql);
			
			try {
				while (rs.next()) {
					DB_Identifier dbi=new DB_Identifier(rs.getString(1),rs.getLong(2),rs.getString(3),rs.getString(4),rs.getString(5));
					ht.put(dbi.cid,dbi);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			return ht;
		}
		
		
		
		public static void loadIdentifiers(long sleep, int batchSize, Connection conn, HashSet<Long> CIDsToLoad) {
			System.out.println(CIDsToLoad.size()+"\tRemaining to load");

			int count=0;

			List<Object>db_identifiers=new ArrayList<>();
			String [] fieldNames= {"date","cid","identifiers","cas","synonyms"};

			try {

				for (long cid:CIDsToLoad) {

					String idURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/property/IUPACName,CanonicalSMILES/JSON?cid="
							+ cid;
					String casURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug_view/data/compound/" + cid
							+ "/JSON?heading=CAS";
					String synonymURL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/" + cid + "/synonyms/TXT";

					SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
					Date date = new Date();
					String strDate = formatter.format(date);

					String cas = FileUtilities.getText_UTF8(casURL);
					if (cas!=null) cas = cas.replaceAll("'", "''").replaceAll(";", "\\;");
					Thread.sleep(sleep);

					String identifiers = FileUtilities.getText_UTF8(idURL);
					if(identifiers!=null) identifiers = identifiers.replaceAll("'", "''").replaceAll(";", "\\;");
					Thread.sleep(sleep);

					String synonyms = StringEscapeUtils.escapeHtml4(FileUtilities.getText_UTF8(synonymURL));
					if(synonyms!=null) synonyms = synonyms.replaceAll("'", "''").replaceAll(";", "\\;");
					Thread.sleep(sleep);

					//	System.out.println(cid);
					//	System.out.println(cas+"\t"+casURL);
					//	System.out.println(identifiers+"\t"+idURL);
					//	System.out.println(synonyms+"\n");
					//	Object [] values= {strDate,cid,identifiers,cas,synonyms};
					//	SQLite_CreateTable.addDataToTable("identifiers", fieldNames, values, conn);

					DB_Identifier db_identifier=new DB_Identifier(strDate,cid,identifiers,cas,synonyms);
					db_identifiers.add(db_identifier);

					if(db_identifiers.size()==batchSize) {
						SqlUtilities.batchCreate("identifiers", fieldNames, db_identifiers, conn);
						db_identifiers.clear();
						count+=batchSize;
						System.out.println(count+" of "+CIDsToLoad.size());
					}

				}//end loop over cids

				//Do what's left over:
				SqlUtilities.batchCreate("identifiers", fieldNames, db_identifiers, conn);
				count+=db_identifiers.size();

				System.out.println(count+" of "+CIDsToLoad.size());


			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}


	public static class DB_Annotation {

		public long ANID;
		public String TOCHeading;
		public String Annotation;
		public String Date;

		public DB_Annotation (long ANID,String TOCHeading,String Annotation,String Date) {
			this.ANID=ANID;
			this.TOCHeading=TOCHeading;
			this.Annotation=Annotation;
			this.Date=Date;
		}
	}

	public static class DB_Annotation_CID {
		public long ANID;
		public long cid;

		public DB_Annotation_CID(long ANID, long cid) {
			this.ANID=ANID;
			this.cid=cid;
		}
	}


	public static HashSet<Long> getCIDsInDB(Statement stat,String tableName) {
		String sql="select DISTINCT cid  from "+tableName+";";
		ResultSet rs=SQLite_GetRecords.getRecords(stat, sql);
		HashSet<Long>CIDs=new HashSet<>();

		try {
			while (rs.next()) {
				long CID=rs.getLong(1);
				CIDs.add(CID);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return CIDs;
	}



	
	
	
	/**
	 * TODO - make it check for bad json
	 */
	void deleteBadIdentifiers() {
		
		String folderMain="data\\experimental\\PubChem_2024_11_27\\";
		String databasePath=folderMain+"PubChem_2024_11_27_raw_json_v2.db";

		Connection conn= SQLite_Utilities.getConnection(databasePath);

		try {
			conn.setAutoCommit(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		
		String sql="select * from identifiers";
		
		ResultSet rs=SqlUtilities.runSQL2(conn, sql);
		
		try {

			while (rs.next()) {
				
				long cid=rs.getLong("cid");
				
//				System.out.println(cid);
				
				try {
					IdentifierData identifierData = gson.fromJson(rs.getString("identifiers"), IdentifierData.class);
					Data casData = gson.fromJson(rs.getString("cas"), Data.class);
					
					if(cid==717704L) {
						System.out.println(gson.toJson(casData));
					}
					
				} catch (Exception ex) {
					System.out.println("bad cid="+cid);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void testParseRecordsInDatabase() {
		String heading="Solubility";
		List<RecordPubChem>records=parseJSONsInDatabase(heading);
		
		System.out.println("# records for "+heading +"= "+records.size());
		
		String folder="data\\experimental\\PubChem_2024_11_27\\";
		String filepath=folder+RecordPubChem.sourceName+" "+heading+" Original Records.json";
		JSONUtilities.batchAndWriteJSON(records, filepath);

	}
	
	
	public static void main(String[] args) {
		ParseNewDatabase r=new ParseNewDatabase();						
//		r.deleteBadIdentifiers();
		//r.testParseFromAnnotation();//		
//		r.testParseRecordsInDatabase();
	
	}



	

}
