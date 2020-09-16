package gov.epa.ghs_data_gathering.Parse.ToxVal;

import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Vector;

import gov.epa.ghs_data_gathering.Database.MySQL_DB;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval.ParseToxVal;
import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval.RecordToxVal;

public class GetToxValRecordsByCAS {

	
	public static final String DB_Path_AA_Dashboard_Records = "AA Dashboard/databases/toxval_v8.db";

	public static Statement statToxVal = MySQL_DB.getStatement(DB_Path_AA_Dashboard_Records);
	
	
	Vector<RecordToxVal> getDataFromTable_toxval(String CAS) {

		try {

//			String sql=createSQLQuery_toxval(chemical.CAS);				
			String sql=createSQLQuery_toxval(CAS);
						
			ResultSet rs=MySQL_DB.getRecords(statToxVal, sql);

			Vector<RecordToxVal>records=new Vector();
									
			while (rs.next()) {						 
				RecordToxVal r=new RecordToxVal();							
				ParseToxValDB.createRecord(rs,r);
				records.add(r);						
			}
								
			return records;

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;

	}
	
	
	 
	static String createSQLQuery_toxval(String CAS) {
		
		String SQL="SELECT\r\na.dtxsid, a.casrn,a.name,\r\n" + 
				"b.toxval_id, b.source,b.subsource,b.toxval_type,b.toxval_type_original,b.toxval_subtype,b.toxval_subtype_original,e.toxval_type_supercategory,\r\n" + 
				"b.toxval_numeric_qualifier,b.toxval_numeric_qualifier_original,b.toxval_numeric,b.toxval_numeric_original,\r\n" + 
				"b.toxval_numeric_converted, b.toxval_units,b.toxval_units_original,b.toxval_units_converted, b.risk_assessment_class,\r\n" + 
				"b.study_type,b.study_type_original,b.study_duration_class,b.study_duration_class_original, b.study_duration_value,\r\n" + 
				"b.study_duration_value_original,b.study_duration_units,b.study_duration_units_original,b.human_eco,\r\n" + 
				"b.strain,b.strain_original,b.sex,b.sex_original,b.generation,\r\n" + 
				"d.species_id,b.species_original,\r\n" + 
				"d.species_common,d.species_supercategory,d.habitat,\r\n" + 
				"b.lifestage,b.exposure_route,b.exposure_route_original,b.exposure_method,b.exposure_method_original,\r\n" + 
				"b.exposure_form,b.exposure_form_original, b.media,b.media_original,b.critical_effect,b.year,b.quality_id,b.priority_id,\r\n" + 
				"b.source_source_id,b.details_text,b.toxval_uuid,b.toxval_hash,b.datestamp\r\n" + 
//				"c.long_ref, c.title, c.author, c.journal, c.volume, c.issue, c.url, c.document_name, c.record_source_type, c.record_source_hash\r\n" + 
				"\r\n" + 
				
				"FROM toxval b\r\n" + 
				"INNER JOIN chemical a on a.dtxsid=b.dtxsid\r\n" + 
				"LEFT JOIN species d on b.species_id=d.species_id\r\n" + 
				"INNER JOIN toxval_type_dictionary e on b.toxval_type=e.toxval_type\r\n" + 
//				"LEFT JOIN record_source c ON b.toxval_id=c.toxval_id\r\n" +				
				"WHERE\r\n"+
				"b.toxval_numeric>0\r\n" + 									
				"AND a.casrn=\""+CAS+"\";";		

//		System.out.println("\n"+SQL);

		return SQL;


	}
	void goThroughRecordsMultipleChemicals(Vector<String>casList,String destfilepathText) {


		try {

			Vector<RecordToxVal>recordsAll=new Vector<>();
			
			for (int i=0;i<casList.size();i++) {

				String CAS=casList.get(i);

				Vector<RecordToxVal>records=getDataFromTable_toxval(CAS);
				
				
				for (RecordToxVal record:records) {
					recordsAll.add(record);
				}
				
// ***Uncomment these later.***
//				getDataFromTable_cancer_summary(chemical);
//				getDataFromTable_genetox_summary(chemical);
//				getDataFromTable_models(chemical);
//				getDataFromTable_bcfbaf(chemical);//TODO

			}

			FileWriter fw=new FileWriter(destfilepathText);
			
			fw.write(RecordToxVal.getHeader()+"\r\n");
			
			for (RecordToxVal record:recordsAll) {
				fw.write(record.toString()+"\r\n");
				
			}

			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
			
		}

	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Vector<String>casList=new Vector<String>();
		casList.add("81-63-0");
		casList.add("82-05-3");
		casList.add("128-80-3");
		casList.add("842-07-9");
		casList.add("2481-94-9");
		casList.add("4314-14-1");
		casList.add("14233-37-5");
		casList.add("17354-14-2");
		casList.add("81-48-1");
		
		casList.add("1229-55-6");
		casList.add("82-38-2");
		casList.add("128-66-5");
		casList.add("3118-97-6");
		casList.add("8003-22-3");		
		
		
		GetToxValRecordsByCAS g=new GetToxValRecordsByCAS();

		String folder="AA dashboard/toxval";
		String filePathRecordsForCASList_txt=folder+File.separator+"toxval_data_dyes.txt"; 

		
		g.goThroughRecordsMultipleChemicals(casList, filePathRecordsForCASList_txt);
		
		
		String filePathRecordsForCASList_json=folder+File.separator+"toxval_pod_summary_dyes.json"; 
		filePathRecordsForCASList_txt=folder+File.separator+"toxval_pod_summary_dyes.txt"; 
		ParseToxValDB p=new ParseToxValDB();
		p.goThroughRecordsMultipleChemicals(casList,filePathRecordsForCASList_json, filePathRecordsForCASList_txt);



	}

}
