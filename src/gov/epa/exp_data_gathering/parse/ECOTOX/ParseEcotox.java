package gov.epa.exp_data_gathering.parse.ECOTOX;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.JSONUtilities;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.UnitConverter;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;
/**
* @author TMARTI02
*/
public class ParseEcotox {

	Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	UnitConverter uc = new UnitConverter("Data" + File.separator + "density.txt");
	
	void getAcuteAquaticExperimentalRecords() {


		String propertyName=ExperimentalConstants.strNINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;
		
		String source=ExperimentalConstants.strSourceEcotox;
		
		List<RecordEcotox>recordsOriginal=null;
		
		boolean createOriginalRecords=false;
		
		if (createOriginalRecords) {
			recordsOriginal=RecordEcotox.get_96hr_FHM_LC50_Tox_Records_From_DB();
			String jsonPath = "data/experimental/"+source+File.separator+source+" "+propertyName+" original records.json";
			int howManyOriginalRecordsFiles = JSONUtilities.batchAndWriteJSON(recordsOriginal,jsonPath);

		} else {
			try {
				String jsonPath = "data/experimental/"+source+File.separator+source+" "+propertyName+" original records.json";
				RecordEcotox[]records2 = gson.fromJson(new FileReader(jsonPath), RecordEcotox[].class);
				recordsOriginal=Arrays.asList(records2);
				System.out.println(recordsOriginal.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		ExperimentalRecords experimentalRecords=new ExperimentalRecords();

		Hashtable<String,RecordEcotox>htRecordEcotox=new Hashtable<>();

		
		for (RecordEcotox re:recordsOriginal) {
			addExperimentalRecords(re, experimentalRecords);
			htRecordEcotox.put(re.test_id,re);
		}
		
		
		

		System.out.println("experimentalRecords.size()="+experimentalRecords.size());

		
		//Writer experimental records to Json file:
		String mainFolder = "Data" + File.separator + "Experimental" + File.separator + source;
		String fileNameJsonExperimentalRecords = "Ecotox_"+propertyName+" Experimental Records.json";
		JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(experimentalRecords),mainFolder+File.separator+fileNameJsonExperimentalRecords);
		
		System.out.println("experimentalRecords.size()="+experimentalRecords.size());

		//Writer experimental records to excel file:
//		experimentalRecords.toExcel_File_Split(mainFolder+File.separator+fileNameJsonExperimentalRecords.replace("json", "xlsx"),100000);
		
		
//		ExperimentalRecord er0=experimentalRecords.get(0);		
//		String test_id=(String)er0.experimental_parameters.get("test_id");
//		RecordEcotox re=htRecordEcotox.get(test_id);
//		System.out.println(gson.toJson(re));
				
		
		Hashtable<String,List<ExperimentalRecord>>htER=new Hashtable<>();
		
		for (ExperimentalRecord er:experimentalRecords)  {
			
			if(er.property_value_units_final.equals(ExperimentalConstants.str_g_L)) {
//				System.out.println(er.casrn+"\t"+er.property_value_point_estimate_final);
				
				if(htER.containsKey(er.dsstox_substance_id) ) {
					List<ExperimentalRecord>recs=htER.get(er.dsstox_substance_id);
					recs.add(er);	
					
				} else {
					List<ExperimentalRecord>recs=new ArrayList<>();
					recs.add(er);
					htER.put(er.dsstox_substance_id, recs);
				}
				
			}
		}
		
		
		for(String dtxsid:htER.keySet()) {
			List<ExperimentalRecord>recs=htER.get(dtxsid);

			
			double mean_log_value=0;
			
			for (ExperimentalRecord er:recs) {
				mean_log_value+=Math.log10(er.property_value_point_estimate_final);
//				System.out.println("\t"+er.property_value_point_estimate_final+"\t"+er.property_value_units_final+"\t"+(String)er.experimental_parameters.get("test_id"));
			}
			mean_log_value/=recs.size();
			
			double biggest_diff=0;
			String test_id_biggest_diff=null;
			
			for (ExperimentalRecord er:recs) {
				double log_value=Math.log10(er.property_value_point_estimate_final);
				
				double diff=Math.abs(log_value-mean_log_value);
				
				if(diff>biggest_diff) {
					biggest_diff=diff;
					test_id_biggest_diff=(String)er.experimental_parameters.get("test_id");
				}
				
			}
			

			if(biggest_diff>0.5) {
				System.out.println(dtxsid);

//				for (ExperimentalRecord er:recs) {
//					System.out.println("\t"+er.property_value_point_estimate_final+"\t"+er.property_value_units_final+"\t"+(String)er.experimental_parameters.get("test_id"));
//				}
				System.out.println("\tBiggest diff\t"+biggest_diff+"\t"+test_id_biggest_diff);
				
				System.out.println(gson.toJson(htRecordEcotox.get(test_id_biggest_diff))+"\n");
				
			}

		}
		
				
		
		
		
	}

	
	private void addExperimentalRecords(RecordEcotox r, ExperimentalRecords recordsExperimental) {

		
		ExperimentalRecord er1=r.toExperimentalRecord(1);
		
		if(er1.keep) {
			uc.convertRecord(er1);
			recordsExperimental.add(er1);	
		}
		
		ExperimentalRecord er2=r.toExperimentalRecord(2);
				
		if(er2.keep) {
			uc.convertRecord(er2);
			recordsExperimental.add(er2);	
		}
		
		
		
//		er.recordOriginal=r;
		
	}


	public static void main(String[] args) {
		ParseEcotox p = new ParseEcotox();
		p.uc.debug=true;
		
		p.getAcuteAquaticExperimentalRecords();
		
//		p.maxExcelRows=999999;
		

	}

}
