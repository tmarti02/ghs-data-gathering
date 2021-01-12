package gov.epa.QSAR.DataSetCreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
import gov.epa.exp_data_gathering.parse.DataFetcher;
import gov.epa.exp_data_gathering.parse.DataRemoveDuplicateExperimentalValues;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.RecordFinalizer;

public class ConvertExperimentalRecordsToDataSet {

//	public static final String databasePathExperimentalRecords="databases/ExperimentalRecords.db";
	public static final String databasePathExperimentalRecords="data/experimental/ExperimentalRecords.db";
	
	void go2(String property) {
////	//Get chemreg export (needed to determine if validated)
//	Vector<RecordChemReg> recordsChemReg = DSSTOX.parseChemRegExcel(filepathExcel,"ChemReg Export");
////	
////	//To get the DSSToxSID and the QSAR ready smiles:
//	Vector<RecordDSSTox> recordsDSSTox = DSSTOX.parseDSSToxExport(filepathExcel,"DSSTox Export");
//
//	
//	Vector<RecordTox> recordsTox=new Vector<>();
//	
//	for (RecordEchemportal2 recordToxOriginal:recordsToxOriginal) {
//		RecordTox recTox=getRecordTox(recordToxOriginal);		
//		recTox.source=source;
////		System.out.println(recTox);
//		
//		recordsTox.add(recTox);
//	}
//	
//	DSSTOX.goThroughToxRecords(recordsTox, recordsChemReg, recordsDSSTox,folder,endpoint,source,false);
//			
//	
//	
//	
//	System.out.println(records.toJSON());
	
//	System.out.println(records2.toJSON());
	}
	
	/**
	 * Loop through records for property 
	 * @param property
	 */
	void getListOfUniqueIdentifiersForGoodRecordsForSource(String folder, String property,String source_name,ExperimentalRecords recordsValid) {
						
		try {
		
								
			
			File Folder=new File(folder);
			Folder.mkdirs();
			
//			String filepathExcel=folder+property+"_"+source_name+"_experimental_records.xlsx";				
//			records.toExcel_File(filepathExcel);
				
			
			Map<String, Vector<String>> uniqueIdentifiers = getUniqueIdentifierMap(recordsValid);
									
			String fileout=folder+property+"_"+source_name+"_ChemReg_Import.txt";			
			FileWriter fw=new FileWriter(fileout);
			fw.write("ExternalID\tQueryCAS\tQueryName\tQuerySmiles\r\n");
			for (Map.Entry<String,Vector<String>> entry : uniqueIdentifiers.entrySet()) {
				fw.write(entry.getValue()+"\t"+entry.getKey()+"\r\n");
			}
			
//			for (int i=0;i<uniqueIdentifiers.size();i++) {
//				fw.write((i+1)+"\t"+uniqueIdentifiers.get(i)+"\r\n");
//			}
			fw.flush();
			fw.close();
			

			System.out.println(source_name+"\t"+recordsValid.size()+"\t"+uniqueIdentifiers.size());
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * Loop through records for property 
	 * @param property
	 */
	void getListOfUniqueIdentifiersForGoodRecords(String folder,String property,ExperimentalRecords recordsValid) {
						
		try {
		
												
			File Folder=new File(folder);
			Folder.mkdirs();
			
//			String filepathExcel=folder+property+"_"+source_name+"_experimental_records.xlsx";				
//			records.toExcel_File(filepathExcel);
				
			
			Map<String, Vector<String>> uniqueIdentifiers = getUniqueIdentifierMap(recordsValid);
									
			String fileout=folder+property+"_ChemReg_Import.txt";			
			FileWriter fw=new FileWriter(fileout);
			fw.write("ExternalID\tQueryCAS\tQueryEINECs\tQueryName\tQuerySmiles\r\n");
			for (Map.Entry<String,Vector<String>> entry : uniqueIdentifiers.entrySet()) {
				fw.write(entry.getValue()+"\t"+entry.getKey()+"\r\n");
			}
			
//			for (int i=0;i<uniqueIdentifiers.size();i++) {
//				fw.write((i+1)+"\t"+uniqueIdentifiers.get(i)+"\r\n");
//			}
			fw.flush();
			fw.close();
			

			System.out.println(recordsValid.size()+"\t"+uniqueIdentifiers.size());
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	Vector<String> getListOfUniqueCASForGoodRecords(String folder,String property,ExperimentalRecords recordsValid) {
		
	
		Vector<String>casList=new Vector<>();
		
		try {
		
												
			File Folder=new File(folder);
			Folder.mkdirs();
			
//			String filepathExcel=folder+property+"_"+source_name+"_experimental_records.xlsx";				
//			records.toExcel_File(filepathExcel);
				
			
			Map<String, Vector<String>> uniqueCAS = getUniqueCASMap(recordsValid);
									
			String fileout=folder+property+"_ChemReg_Import_CAS.txt";			
			FileWriter fw=new FileWriter(fileout);
			fw.write("ExternalID\tQueryCAS\r\n");
			for (Map.Entry<String,Vector<String>> entry : uniqueCAS.entrySet()) {
				
				if (entry.getKey().contains("NO")) continue;
				
//				if (entry.getValue().toString().contains("OPERA")) continue;
				
				casList.add(entry.getKey());
				
				
				fw.write(entry.getValue()+"\t"+entry.getKey()+"\r\n");
			}
			
//			for (int i=0;i<uniqueIdentifiers.size();i++) {
//				fw.write((i+1)+"\t"+uniqueIdentifiers.get(i)+"\r\n");
//			}
			fw.flush();
			fw.close();
			

			System.out.println("Unique cas count="+uniqueCAS.size());
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return casList;
		
	}
	
	
	/**
	 * Loop through records for property 
	 * @param property
	 */
	void getListOfComboIDsForGoodRecords(String property,ExperimentalRecords records) {
						
		try {
		
									
			String folder = "data\\DataSets\\"+property+"\\";
			
			File Folder=new File(folder);
			Folder.mkdirs();
			
//			String filepathExcel=folder+property+"_"+source_name+"_experimental_records.xlsx";				
//			records.toExcel_File(filepathExcel);
				
			
			Vector<String> uniqueIDs = getUniqueComboIDs(records,"|");
									
			String fileout=folder+property+"_ChemReg_Import_ComboID.txt";			
			FileWriter fw=new FileWriter(fileout);
			fw.write("ExternalID\tQueryCAS\tQueryName\tQuerySmiles\r\n");
			
			for (String ID:uniqueIDs) {
				fw.write(ID+"\t"+ID.replace("|", "\t")+"\r\n");
			}
			
//			for (int i=0;i<uniqueIdentifiers.size();i++) {
//				fw.write((i+1)+"\t"+uniqueIdentifiers.get(i)+"\r\n");
//			}
			fw.flush();
			fw.close();
			

			System.out.println(records.size()+"\t"+uniqueIDs.size());
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	private Map<String, Vector<String>> getUniqueIdentifierMap(ExperimentalRecords records2) {
		Map<String,Vector<String>>uniqueIdentifiers=new TreeMap<>();
		
		//TODO This needs to store what id_physchems have each unique identifier
		
		for (ExperimentalRecord record:records2) {
//				String comboID=record.casrn+"\t"+record.chemical_name+"\t"+record.smiles+"\t"+record.einecs;				
			
			if (!record.keep) continue;
			
			record.setComboID("\t");
			String comboID=record.comboID;
			
			if(comboID==null || comboID.trim().isBlank()) continue;
			
			if(!uniqueIdentifiers.containsKey(comboID)) {
				Vector<String>ids=new Vector<>();
				ids.add(record.id_physchem);
				uniqueIdentifiers.put(comboID,ids);
			
			} else {
				Vector<String>ids=uniqueIdentifiers.get(comboID);
				ids.add(record.id_physchem);
			}
			
		}
		return uniqueIdentifiers;
	}
	
	private Map<String, Vector<String>> getUniqueCASMap(ExperimentalRecords records2) {
		Map<String,Vector<String>>uniqueIdentifiers=new TreeMap<>();
		
		//TODO This needs to store what id_physchems have each unique identifier
		
		for (ExperimentalRecord record:records2) {
//				String comboID=record.casrn+"\t"+record.chemical_name+"\t"+record.smiles+"\t"+record.einecs;				
			
			if (!record.keep) continue;
			
			
			if(record.casrn==null || record.casrn.isBlank()) continue;
			
			if(!uniqueIdentifiers.containsKey(record.casrn)) {
				Vector<String>ids=new Vector<>();
				ids.add(record.id_physchem);
				uniqueIdentifiers.put(record.casrn,ids);
			
			} else {
				Vector<String>ids=uniqueIdentifiers.get(record.casrn);
				ids.add(record.id_physchem);
			}
			
		}
		return uniqueIdentifiers;
	}
	
	private Vector<String> getUniqueComboIDs(ExperimentalRecords records2,String del) {
		Vector<String>uniqueIdentifiers=new Vector<>();
		
		//TODO This needs to store what id_physchems have each unique identifier
		
		for (ExperimentalRecord record:records2) {
//				String comboID=record.casrn+"\t"+record.chemical_name+"\t"+record.smiles+"\t"+record.einecs;				
			
			if (!record.keep) continue;
			
			record.setComboID("|");
			String comboID=record.comboID;
			
			if(comboID==null || comboID.trim().isBlank()) continue;
			
			if(!uniqueIdentifiers.contains(comboID)) {
				uniqueIdentifiers.add(comboID);			
			} 
			
		}
		return uniqueIdentifiers;
	}




	
	
	private ExperimentalRecords getExperimentalRecordsFromDB(String property,String source_name) {

		ExperimentalRecords records=new ExperimentalRecords();
		
		String sql="select * from records where property_name=\""+property+"\" and keep=\"true\" "
				+ "and source_name=\""+source_name+"\"\r\n"+ "order by casrn";
		
		try {
			Connection conn=SQLite_Utilities.getConnection(databasePathExperimentalRecords);
			Statement stat=conn.createStatement();
			ResultSet rs = stat.executeQuery(sql);
			
			while (rs.next()) {
				ExperimentalRecord record=new ExperimentalRecord();
				SQLite_GetRecords.createRecord(rs, record);
				records.add(record);				
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return records;

	}
	
	
	


	
	ExperimentalRecords getExperimentalRecordsFromDB(String property) {

		ExperimentalRecords records=new ExperimentalRecords();
		
		String sql="select * from records where property_name=\""+property+"\" and keep=\"true\" "
				+ "\r\n"+ "order by casrn";
		
		try {
			Connection conn=SQLite_Utilities.getConnection(databasePathExperimentalRecords);
			Statement stat=conn.createStatement();
			ResultSet rs = stat.executeQuery(sql);
			
			while (rs.next()) {
				ExperimentalRecord record=new ExperimentalRecord();
				SQLite_GetRecords.createRecord(rs, record);
				records.add(record);				
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return records;
	}
	
	private Vector<String> getSourceList(Statement stat, String property) {
		Vector<String>sources=new Vector<>();

		String sql = "select distinct source_name from records"
				+ " where property_name=\""+ property+ "\" and keep=\"true\"";
				
		try {
			ResultSet rs = stat.executeQuery(sql);
			
			while (rs.next()) {
				String source=rs.getString(1);
//				System.out.println(source);
				sources.add(source);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sources;
	}


	//determine which IDs in endpoint 2 are not in endpoint 1
	
	void compareIDFiles(String endpoint1,String endpoint2) {
		String file1 = "data\\DataSets\\"+endpoint1+"\\"+endpoint1+"_ChemReg_Import.txt";	
		String file2 = "data\\DataSets\\"+endpoint2+"\\"+endpoint2+"_ChemReg_Import.txt";
	
		System.out.println(file1);

		try {
			Vector<String>comboIDs1=getIDs(file1);
			Vector<String>comboIDs2=getIDs(file2);
				
			int countNew=0;
			for (String ID:comboIDs2) {
				if (!comboIDs1.contains(ID)) {
					countNew++;
//					System.out.println(count+"\t"+ID);
				}
			}
			System.out.println(comboIDs2.size()+"\t"+countNew);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	private Vector<String> getIDs(String filepath) throws IOException {
		Vector<String>comboIDs1=new Vector<>();

		File F=new File(filepath);
		if (!F.exists()) {
			System.out.println(F.getAbsolutePath()+"\tdoesnt exist");
		}
		
		Vector<String> lines = new Vector<>();

		try (Scanner s = new Scanner(new FileReader(filepath))) {
		    while (s.hasNext()) {
		        lines.add(s.nextLine());
		    }

		}
		
		
		for (int i=1;i<lines.size();i++) {
			String Line=lines.get(i);
			String ID=Line.substring(Line.indexOf("\t")+1,Line.length());
			if(!comboIDs1.contains(ID)) comboIDs1.add(ID);
		}
		return comboIDs1;
	}
	
	ExperimentalRecords getRecordsWithProperty(String propertyName,ExperimentalRecords records) {
		ExperimentalRecords records2=new ExperimentalRecords();	

		for (ExperimentalRecord record:records) {
			if(record.property_name.contentEquals(propertyName)) {
				records2.add(record);
			}
		}
		return records2;
	}
	
	/**
	 * This version uses inchiKey1 from Structure_InChIKey to merge
	 * 
	 * @param expRecords
	 * @param folder
	 * @return
	 */
	private ExperimentalRecords mergeIsomersContinuousOmitSalts(ExperimentalRecords expRecords,String folder) {
		ExperimentalRecords expRecordsQSAR=new ExperimentalRecords();

		Hashtable<String, ExperimentalRecords> htRecordsInchiKey1 = getLookupByInchiKey1(expRecords);

		Set<String> keys = htRecordsInchiKey1.keySet();
		Iterator<String> itr = keys.iterator();

		try {
			
			FileWriter fwIsomers=new FileWriter(folder+"isomers.txt");

			fwIsomers.write(RecordTox.getHeader()+"\r\n");
			
			int count=0;
			
			int countOmittedSIDS=0;
			
			while (itr.hasNext()) {
				
				String inchiKey1=itr.next();					
				ExperimentalRecords records=htRecordsInchiKey1.get(inchiKey1);

				if (records.size()>1) {
					count++;
					
					Double finalScore=getFinalScore(inchiKey1,records);

					if (finalScore==null) {
						System.out.println("Cant assign final value:"+"\t"+inchiKey1);						
						countOmittedSIDS+=records.size();
						
					} else {
						ExperimentalRecord recExp=new ExperimentalRecord();
						recExp.property_value_point_estimate_final=finalScore;					
						expRecordsQSAR.add(recExp);
						countOmittedSIDS+=records.size()-1;
											}
										
					//Store isomer info:
					fwIsomers.write(inchiKey1+"\t"+finalScore+"\r\n");
					for(ExperimentalRecord rt:records) {
						fwIsomers.write(rt+"\r\n");
					}
					fwIsomers.write("\r\n");

//					System.out.println(inchiKey1+"\t"+records.size()+"\t"+finalScore);
				} else {
					expRecordsQSAR.add(records.get(0));
				}

			}
			fwIsomers.write("\nNumber of sets of duplicates="+count);			
			fwIsomers.write("\nNumber of SIDs omitted="+countOmittedSIDS);
			fwIsomers.flush();
			fwIsomers.close();
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return expRecordsQSAR;
	}

	private Double getFinalScore(String inchiKey1,ExperimentalRecords records) {
		Double finalValue=null;
		
		if (records.size()%2==0) {
			//even
		} else {//odd
			
		}
		
		
		return finalValue;
	}

	private Hashtable<String, ExperimentalRecords> getLookupByInchiKey1(ExperimentalRecords expRecords) {
		Hashtable<String, ExperimentalRecords> htRecordsInchiKey1=new Hashtable<>();

		//Make hashtable of records based on InChiKey1:
		for(ExperimentalRecord recordExp:expRecords) {
			
			if (!recordExp.keep) continue;
			
			String inchiKey1=recordExp.Structure_InChIKey.substring(0,recordExp.Structure_InChIKey.indexOf("-"));
			
			if (htRecordsInchiKey1.get(inchiKey1)==null) {
				ExperimentalRecords recs=new ExperimentalRecords();
				recs.add(recordExp);
				htRecordsInchiKey1.put(inchiKey1,recs);
			} else {
				ExperimentalRecords recs=htRecordsInchiKey1.get(inchiKey1);
				recs.add(recordExp);
			}							

		}
		return htRecordsInchiKey1;
	}
	
	void runRatInhalationLC50() {
		
		String property=ExperimentalConstants.strRatInhalationLC50;		
		String folder = "data\\DataSets\\"+property+"\\";

		String filepathExcel="data\\experimental\\eChemPortalAPI\\eChemPortalAPI Toxicity Experimental Records.xlsx";
		ExperimentalRecords recordsExcel = ExperimentalRecords.loadFromExcel(filepathExcel);		
		ExperimentalRecords recordsEchemportal=getRecordsWithProperty(property, recordsExcel);			

		
		String jsonPath="data\\experimental\\ChemIDplus\\ChemIDplus Experimental Records.json";
//		ExperimentalRecords recordsChemIDplus=getExperimentalRecordsFromDB(property,ExperimentalConstants.strSourceChemidplus);
		ExperimentalRecords recordsJson=ExperimentalRecords.loadFromJSON(jsonPath);
		ExperimentalRecords recordsChemIDplus=getRecordsWithProperty(property, recordsJson);
//		System.out.println("Chemidplus record count="+recordsChemIDplus.size());
					
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		recordsExperimental.addAll(recordsEchemportal);
		recordsExperimental.addAll(recordsChemIDplus);		
		
		omitBadQSARRecords(recordsExperimental);
		
		//TODO add method to remove duplicates where have echemportal and chemidplus for same chemical!
//		getListOfComboIDsForGoodRecords(property, records);	
//		getListOfUniqueIdentifiersForGoodRecords(folder,property,records);
		
		//Write records to Excel file:
//		records.toExcel_File(folder+property+"_records.xlsx");
//		recordsExperimental.dumpBadRecords();
		
		Vector<RecordDSSTox>recordsDSSTox=RecordDSSTox.getDSSToxExportRecords(folder+"DSSTox_"+property+".xlsx");
		Vector<RecordChemReg>recordsChemReg=RecordChemReg.getChemRegRecords(folder+"ChemReg_"+property+".xlsx");
		
		long t1=System.currentTimeMillis();
		boolean omitSalts=true;
		DSSTOX.goThroughToxRecords2(recordsExperimental,recordsChemReg,recordsDSSTox,folder,property,omitSalts);
		long t2=System.currentTimeMillis();
		System.out.println("time to get DSSToxInfo="+(t2-t1)/1000.0+" secs");
		
		
//		mergeIsomersContinuousOmitSalts(recordsExperimental, folder);
		
		
	}


	private void omitBadQSARRecords(ExperimentalRecords records) {
		int count=0;
		for (int i=0;i<records.size();i++) {
			ExperimentalRecord rec=records.get(i);					
			//Filtering QSAR worthy records:			
			omitBadQSARRecord(count, rec);										
			if (!rec.keep) continue;			
			count++;
//			System.out.println(count+"\t"+rec.property_value_point_estimate_final+"\t"+rec.property_value_units_final);
		}
	}
	
	void runWS() {
		
		String property=ExperimentalConstants.strWaterSolubility;		
		String folder = "data\\DataSets\\"+property+"\\";
					
		ExperimentalRecords records=getExperimentalRecordsFromDB(property);
		
		int count=0;
			
		for (int i=0;i<records.size();i++) {
			ExperimentalRecord rec=records.get(i);					

			//Filtering QSAR worthy records:			
			omitBadQSARRecord(count, rec);
		
			//WS depends a lot on temperature:
			if (rec.temperature_C!=null && (rec.temperature_C>30 || rec.temperature_C<20)) {
				rec.keep=false;
			}
									
			if (!rec.keep) continue;			
			count++;
//			System.out.println(count+"\t"+rec.property_value_point_estimate_final+"\t"+rec.property_value_units_final);
		}
		
			
//		System.out.println(count);		
//		records.toExcel_File(folder+property+"_records.xlsx");
//		
//		//TODO add method to remove duplicates where have echemportal and chemidplus for same chemical!
//		
//		getListOfComboIDsForGoodRecords(property, records);	
		getListOfUniqueIdentifiersForGoodRecords(folder,property,records);		
//		Vector<String>casList=getListOfUniqueCASForGoodRecords(folder,property,records);

		//Determine which records are in dashboard
//		Vector<RecordDashboard>recsDashboard=RecordDashboard.getDashboardRecordsBatch(folder,casList, 1000);
	
//		getChemRegInputForCASNotInProdDashboard(folder,property);

		
		
	}

	private void getChemRegInputForCASNotInProdDashboard(String folder,String endpoint) {
		Vector<RecordDashboard>recsDashboard=RecordDashboard.getDashboardRecordsFromTextFile(folder+"RecordsDashboard.txt");
		Hashtable<String,RecordDashboard>ht=new Hashtable<>();
		for (RecordDashboard rec:recsDashboard) {
			ht.put(rec.INPUT,rec);
		}
		
		try {
			
			
			FileWriter fw=new FileWriter(folder+endpoint+"_ChemReg_Import_Not_In_Dashboard.txt");
			
			Scanner scanner=new Scanner(new File(folder+endpoint+"_ChemReg_Import.txt"));
			String header=scanner.nextLine();
			String [] fieldNames=header.split("\t");
						
			fw.write("ExternalID\tQueryCAS\tQueryEINECS\tQueryName\tQuerySmiles\r\n");
			
			while (scanner.hasNext()) {
				String Line=scanner.nextLine();
				
//				System.out.println(Line);
				
				String [] fieldValues=Line.split("\t");
				
				String CAS=fieldValues[1];
				
				if (ht.get(CAS)==null) continue;
				
				if (ht.get(CAS).FOUND_BY.contentEquals("NO_MATCH")) {
//					System.out.println(Line);
					fw.write(Line+"\r\n");
				}
				
			}
			fw.flush();
			scanner.close();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	private void omitBadQSARRecord(int count, ExperimentalRecord rec) {
		if (rec.property_value_numeric_qualifier!=null && !rec.property_value_numeric_qualifier.contentEquals("~")) {
//				System.out.println(count+"\t"+rec.toJSON());
			rec.keep=false;
			rec.reason="bad qualifier";
			
		} else if (rec.reason!=null && rec.reason.contains("Range too wide to compute point estimate")) {
//				System.out.println(count+"\t"+rec.toJSON());
			rec.keep=false;
			
		} else if (rec.property_value_point_estimate_final==null && rec.property_value_max_final==null) {					
			rec.keep=false;
			rec.reason="no point estimate or max value";
		} else if (rec.property_value_point_estimate_final==null && rec.property_value_max_final!=null) {
//				RecordFinalizer.finalizeRecord(rec);				
			if (rec.property_value_point_estimate_final==null) {
				System.out.println(count+"\t"+rec.toJSON());
				rec.keep=false;
				rec.reason="no point estimate but have max value";
			}
		}
	}
	
	
	public static void main(String[] args) {
		ConvertExperimentalRecordsToDataSet c=new ConvertExperimentalRecordsToDataSet();
		
		
		c.runRatInhalationLC50();
//		c.runWS();		
		
		if (true) return;
		
		Statement stat=SQLite_Utilities.getStatement(databasePathExperimentalRecords);

		
		String property=ExperimentalConstants.strWaterSolubility;
//		String property=ExperimentalConstants.strBoilingPoint;
//		String property=ExperimentalConstants.strRatInhalationLC50;

		String folder = "data\\DataSets\\"+property+"\\";

		
		System.out.println(property);

		//Get unique list of sources for the property:
//		Vector<String>sources=c.getSourceList(stat,property);
//		
//		for (String source:sources) {
//			ExperimentalRecords records=c.getExperimentalRecords(property, source);
//			ExperimentalRecords recordsValid = c.getValidRecords(records);
//			c.getListOfUniqueIdentifiersForGoodRecordsForSource(folder,property,source,recordsValid);
//		}
//		
//		c.getListOfUniqueIdentifiersForGoodRecords(property,ExperimentalConstants.strSourceEChemPortal,stat);

		//******************************************************************************
//		ExperimentalRecords records=c.getExperimentalRecordsFromDB(property);
//		ExperimentalRecords recordsValid = c.getValidRecords(records);
//		
//		
////		recordsValid.toExcel_File(folder+"valid records.xlsx");
//		
//		c.getListOfUniqueIdentifiersForGoodRecords(folder,property,recordsValid);
//		c.getListOfComboIDsForGoodRecords(property, recordsValid);
		
//		c.compareIDFiles(ExperimentalConstants.strWaterSolubility, ExperimentalConstants.strBoilingPoint);
	}

}
