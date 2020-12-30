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
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;

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
			fw.write("ExternalID\tQueryCAS\tQueryName\tQuerySmiles\r\n");
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
	
	
	/**
	 * Loop through records for property 
	 * @param property
	 */
	void getListOfComboIDsForGoodRecords(String property,ExperimentalRecords recordsValid) {
						
		try {
		
									
			String folder = "data\\DataSets\\"+property+"\\";
			
			File Folder=new File(folder);
			Folder.mkdirs();
			
//			String filepathExcel=folder+property+"_"+source_name+"_experimental_records.xlsx";				
//			records.toExcel_File(filepathExcel);
				
			
			Vector<String> uniqueIDs = getUniqueComboIDs(recordsValid,"|");
									
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
			

			System.out.println(recordsValid.size()+"\t"+uniqueIDs.size());
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	private Map<String, Vector<String>> getUniqueIdentifierMap(ExperimentalRecords records2) {
		Map<String,Vector<String>>uniqueIdentifiers=new TreeMap<>();
		
		//TODO This needs to store what id_physchems have each unique identifier
		
		for (ExperimentalRecord record:records2) {
//				String comboID=record.casrn+"\t"+record.chemical_name+"\t"+record.smiles+"\t"+record.einecs;				
			
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
	
	private Vector<String> getUniqueComboIDs(ExperimentalRecords records2,String del) {
		Vector<String>uniqueIdentifiers=new Vector<>();
		
		//TODO This needs to store what id_physchems have each unique identifier
		
		for (ExperimentalRecord record:records2) {
//				String comboID=record.casrn+"\t"+record.chemical_name+"\t"+record.smiles+"\t"+record.einecs;				
			
			record.setComboID("|");
			String comboID=record.comboID;
			
			if(comboID==null || comboID.trim().isBlank()) continue;
			
			if(!uniqueIdentifiers.contains(comboID)) {
				uniqueIdentifiers.add(comboID);			
			} 
			
		}
		return uniqueIdentifiers;
	}




	ExperimentalRecords getValidRecords(ExperimentalRecords records) {
		ExperimentalRecords records2=new ExperimentalRecords();//store ones we want to keep
		
		for (ExperimentalRecord record:records) {
			
			if (record.property_value_numeric_qualifier!=null) {
//					System.out.println(record.toJSON());
				continue;					
			}
			
			
			if (record.property_value_point_estimate_final==null) {
				
				if (record.property_value_min_final==null || record.property_value_max_final==null) {
					continue;
				}				
				
//				if (record.property_value_min_final!=null && record.property_value_max_final!=null) {
//					//For now take an average of min and max:					
//					//TODO- if min and max is too far apart exclude if 10K for MP				
//					double min=record.property_value_min_final;
//					double max=record.property_value_max_final;
//					record.property_value_point_estimate_final=(min+max)/2.0;
////						System.out.println(record.casrn+"\t"+record.property_value_point_estimate_final);
//				} else {
//						System.out.println(record.toJSON());
//					continue;
//				}
			}
			
			if (record.temperature_C!=null && (record.temperature_C<20 || record.temperature_C>30)) {
//				System.out.println("bad temp="+record.temperature_C);
				continue;
			}
			
			if (record.pressure_mmHg!=null) {
				try {
					double pres=Double.parseDouble(record.pressure_mmHg);				
					if (pres<760*0.95 || pres>760*1.05)  {
						System.out.println("bad pres="+pres);
						continue;
					}
					
				} catch (Exception ex) {
					System.out.println("Couldnt parse pressure:"+record.pressure_mmHg);
					//TODO parse range and get average pressure to decide whether to skip
				}								
			}

			//TODO add code to exclude records at bad pHs...

			//If everything above is ok, add record to kept records:
			records2.add(record);
		
//			System.out.println(records.toJSON());
		}
		return records2;
	}

	
	private ExperimentalRecords getExperimentalRecords(String property,String source_name) {

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

	
	ExperimentalRecords getExperimentalRecords(String property) {

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
	
	
	public static void main(String[] args) {
		ConvertExperimentalRecordsToDataSet c=new ConvertExperimentalRecordsToDataSet();
		
		Statement stat=SQLite_Utilities.getStatement(databasePathExperimentalRecords);

		
//		String property=ExperimentalConstants.strWaterSolubility;
//		String property=ExperimentalConstants.strBoilingPoint;
		String property=ExperimentalConstants.strRatInhalationLC50;

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
		ExperimentalRecords records=c.getExperimentalRecords(property);
		ExperimentalRecords recordsValid = c.getValidRecords(records);
		
		
//		recordsValid.toExcel_File(folder+"valid records.xlsx");
		
		c.getListOfUniqueIdentifiersForGoodRecords(folder,property,recordsValid);
		c.getListOfComboIDsForGoodRecords(property, recordsValid);
		c.compareIDFiles(ExperimentalConstants.strWaterSolubility, ExperimentalConstants.strBoilingPoint);
	}

}
