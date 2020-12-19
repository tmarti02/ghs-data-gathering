package gov.epa.QSAR.DataSetCreation;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import gov.epa.QSAR.database.SQLite_GetRecords;
import gov.epa.QSAR.database.SQLite_Utilities;
import gov.epa.api.ExperimentalConstants;
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
	void getListOfUniqueIdentifiersForGoodRecords(String property,String source_name,Statement stat) {
						
		try {
		
			ExperimentalRecords records=getExperimentalRecords(property, source_name);
									
			String folder = "data\\DataSets\\"+property+"\\";
			
			File Folder=new File(folder);
			Folder.mkdirs();
			
			String filepathExcel=folder+property+"_"+source_name+"_experimental_records.xlsx";				
			records.toExcel_File(filepathExcel);
				
			ExperimentalRecords records2 = getValidRecords(records);
			
			Map<String, Vector<Integer>> uniqueIdentifiers = getUniqueIdentifierMap(records2);
									
			String fileout=folder+property+"_"+source_name+"_ChemReg_Import.txt";			
			FileWriter fw=new FileWriter(fileout);
			fw.write("ExternalID\tQueryCAS\tQueryName\tQuerySmiles\r\n");
			for (Map.Entry<String,Vector<Integer>> entry : uniqueIdentifiers.entrySet()) {
				fw.write(entry.getValue()+"\t"+entry.getKey()+"\r\n");
			}
			
//			for (int i=0;i<uniqueIdentifiers.size();i++) {
//				fw.write((i+1)+"\t"+uniqueIdentifiers.get(i)+"\r\n");
//			}
			fw.flush();
			fw.close();
			

			System.out.println(source_name+"\t"+records.size()+"\t"+records2.size()+"\t"+uniqueIdentifiers.size());
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	private Map<String, Vector<Integer>> getUniqueIdentifierMap(ExperimentalRecords records2) {
		Map<String,Vector<Integer>>uniqueIdentifiers=new TreeMap<>();
		
		//TODO This needs to store what id_physchems have each unique identifier
		
		for (ExperimentalRecord record:records2) {
//				String comboID=record.casrn+"\t"+record.chemical_name+"\t"+record.smiles+"\t"+record.einecs;				
			
			String CAS=record.casrn;
			if (CAS==null || CAS.trim().isEmpty()) CAS="CAS is not available";//need placeholder so dont get spurious match in chemreg
			else {
				CAS=CAS.trim();
				while (CAS.substring(0,1).contentEquals("0")) {//trim off zeros at front
					CAS=CAS.substring(1,CAS.length());
				}
				//TODO - do we need to handle Cases with no dashes? Check for bad cas numbers (bad check sum?)
			}
			String name=record.chemical_name;
			if (name==null || name.trim().isEmpty()) name="Chemical name is not available";//need placeholder so dont get spurious match in chemreg
			name=name.trim();
			
			String smiles=record.smiles;
			if (smiles==null || smiles.trim().isEmpty()) smiles="Smiles is not available";//need placeholder so dont get spurious match in chemreg
			smiles=smiles.trim();
			
			String comboID=CAS+"\t"+name+"\t"+smiles;//TODO add smiles
			
			if(comboID==null || comboID.trim().isBlank()) continue;
			
			if(!uniqueIdentifiers.containsKey(comboID)) {
				Vector<Integer>ids=new Vector<>();
				ids.add(record.id_physchem);
				uniqueIdentifiers.put(comboID,ids);
			
			} else {
				Vector<Integer>ids=uniqueIdentifiers.get(comboID);
				ids.add(record.id_physchem);
			}
			
		}
		return uniqueIdentifiers;
	}


	private ExperimentalRecords getValidRecords(ExperimentalRecords records) {
		ExperimentalRecords records2=new ExperimentalRecords();//store ones we want to keep
		
		for (ExperimentalRecord record:records) {
			
			if (record.property_value_numeric_qualifier!=null) {
//					System.out.println(record.toJSON());
				continue;					
			}
			
			
			if (record.property_value_point_estimate_final==null) {
				if (record.property_value_min_final!=null && record.property_value_max_final!=null) {
					//For now take an average of min and max:
					double min=record.property_value_min_final;
					double max=record.property_value_max_final;
					record.property_value_point_estimate_final=(min+max)/2.0;
//						System.out.println(record.casrn+"\t"+record.property_value_point_estimate_final);
				} else {
//						System.out.println(record.toJSON());
					continue;
				}
			}
			
			if (record.temperature_C!=null && (record.temperature_C<20 || record.temperature_C>30)) {
//				System.out.println(record.temperature_C);
				continue;
			}
			
			if (record.pressure_mmHg!=null) {
				try {
					double pres=Double.parseDouble(record.pressure_mmHg);				
					if (pres<760*0.95 || pres>760*1.05) continue;
					
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

	
	
	public static void main(String[] args) {
		ConvertExperimentalRecordsToDataSet c=new ConvertExperimentalRecordsToDataSet();
		
		
		Statement stat=SQLite_Utilities.getStatement(databasePathExperimentalRecords);

		
		String property=ExperimentalConstants.strWaterSolubility;
//		String property=ExperimentalConstants.strBoilingPoint;
		System.out.println(property);

		//Get unique list of sources for the property:
		Vector<String>sources=c.getSourceList(stat,property);
		
		for (String source:sources) {
			c.getListOfUniqueIdentifiersForGoodRecords(property,source,stat);
		}
		
//		c.getListOfUniqueIdentifiersForGoodRecords(property,ExperimentalConstants.strSourceEChemPortal,stat);

	}

}
