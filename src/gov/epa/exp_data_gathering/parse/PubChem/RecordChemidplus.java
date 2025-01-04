package gov.epa.exp_data_gathering.parse.PubChem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.database.SQLite_Utilities;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.PublicSource;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Data;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.IdentifierData;
import gov.epa.exp_data_gathering.parse.PubChem.ParseNewDatabase.DB_Identifier;

/**
* @author TMARTI02
*/
public class RecordChemidplus {
	
	
	Long cid;	
	Long sid;
	String sourceid;
	
	String route;
	String organism;
	String testtype;
	
	String dose;
	
	String effect;
	String reference;
	
	static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues()
			.create();

	
	
	private void loadIdentifiersChemidplus() {
		String folderMain="data\\experimental\\PubChem_2024_11_27\\";
		String databasePath=folderMain+"PubChem_2024_11_27_raw_json_v2.db";

		Connection conn= SQLite_Utilities.getConnection(databasePath);
		Statement stat=SQLite_Utilities.getStatement(conn);

		HashSet<Long> CIDsInIdentifiers = ParseNewDatabase.getCIDsInDB(stat,"identifiers");
		
		System.out.println("CIDs in identifiers table:"+CIDsInIdentifiers.size());
		
		HashSet<Long> CIDsToLoad=new HashSet<>();
		
		try {
			String folder="data\\experimental\\PubChem_2024_11_27\\json\\toxicity\\";
			String pathChemidplus=folder+"chemidplus.json";
			
			BufferedReader br=new BufferedReader(new FileReader(pathChemidplus));
			
			while (true) {
				String Line=br.readLine();
				if(Line==null) break;
				
				RecordChemidplus rc=gson.fromJson(Line,RecordChemidplus.class);
				if(rc.cid==null) continue;
//				if(rc.cid!=9588813) continue;

				if(!CIDsInIdentifiers.contains(rc.cid)) {
//					System.out.println(rc.cid);	
					CIDsToLoad.add(rc.cid);
				} 
			}
			
			System.out.println("CIDs to load:"+CIDsToLoad.size());
			DB_Identifier.loadIdentifiers(100, 10, conn, CIDsToLoad);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	static HashSet<Long> getCidOralRatLD50RecordsChemidplus(String filepath) {
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
		
			int count=0;
			
			HashSet<Long>cids=new HashSet<>();
			
			while (true) {
				String line=br.readLine();
				if(line==null) break;
				RecordChemidplus rc=gson.fromJson(line,RecordChemidplus.class);
				
				if(rc.organism.contentEquals("rat") && rc.testtype.contentEquals("LD50") && rc.route.contentEquals("oral")) {
					if(rc.cid==null) continue;
					
					count++;
					cids.add(rc.cid);
				}
				
			}
			
			System.out.println(count);
			return cids;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RecordChemidplus r=new RecordChemidplus();
		r.loadIdentifiersChemidplus();
//		
	}


	public static Vector<RecordPubChem> parseJsonFile(Hashtable<Long, DB_Identifier> htIdentifiersByCID) {

		Vector<RecordPubChem> records = new Vector<>();
		
		try {
			String folder="data\\experimental\\PubChem_2024_11_27\\json\\toxicity\\";
			String pathChemidplus=folder+"chemidplus.json";
			
			BufferedReader br=new BufferedReader(new FileReader(pathChemidplus));
			
			int count=0;
			int countCasMatch=0;
			
			
			while (true) {
				String Line=br.readLine();
				
				if(Line==null) break;
				
				
				count++;
				
				RecordChemidplus rc=gson.fromJson(Line,RecordChemidplus.class);
				if(rc.cid==null) continue;

				RecordPubChem rpc=rc.toRecordPubchem();
				
				if(htIdentifiersByCID.containsKey(rpc.cid)) {
					DB_Identifier db_identifier=htIdentifiersByCID.get(rpc.cid);
					IdentifierData identifierData=null;
					
					if (db_identifier.identifiers!=null) {
						identifierData = gson.fromJson(db_identifier.identifiers, IdentifierData.class);
					} else {
//						System.out.println(rpc.cid+"\tNo identifiers from cid");
					}
					rpc.addIdentifiers(identifierData, db_identifier.synonyms);
					
					if (db_identifier.cas!=null) {
						Data casData = gson.fromJson(db_identifier.cas, Data.class);
						rpc.addReferenceNameCasFromSourceId(casData,"ChemIDplus");
						
						if(rpc.casReference!=null) {
							countCasMatch++;
						} else {
							System.out.println(rpc.cid+"\t"+rpc.iupacNameCid+"\tNo CAS match");
						}
//						System.out.println(gson.toJson(rpc));
					}

				
				} else {
//					System.out.println("No identifiers in db for cid="+rc.cid);
				}

				
				if(rpc.casReference==null && rpc.chemicalNameReference==null && rpc.iupacNameCid==null && rpc.canonSmilesCid==null) {
//					System.out.println("No identifiers");
				} else {
					records.add(rpc);	
				}

//				System.out.println(gson.toJson(rc));
//				System.out.println(gson.toJson(rpc));
//				
//				if (count==10)break;
//				if(true) break;
				
			}
			System.out.println("count="+count);
			System.out.println("countCasMatch="+countCasMatch);
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return records;

	}


	private RecordPubChem toRecordPubchem() {
		// TODO Auto-generated method stub
		RecordPubChem rpc=new RecordPubChem();
		
		rpc.propertyValue=this.dose.replace("&gt;", "> ");
		
		rpc.cid=this.cid;
		rpc.sid=this.sid;
		
		String s1 = route.substring(0, 1).toUpperCase();
		this.route = s1 + this.route.substring(1);
		
		rpc.propertyName=this.route+" "+this.organism+" "+this.testtype;
		rpc.effect=this.effect;
		rpc.sourceid=this.sourceid;
		rpc.literatureSource=new LiteratureSource();
		rpc.literatureSource.citation=this.reference;
		
		rpc.publicSourceOriginal=new PublicSource();
		rpc.publicSourceOriginal.name=ExperimentalConstants.strSourceChemidplus2024_12_04;
		
		rpc.date_accessed="2024_12_04";
		
		return rpc;
	}

}
