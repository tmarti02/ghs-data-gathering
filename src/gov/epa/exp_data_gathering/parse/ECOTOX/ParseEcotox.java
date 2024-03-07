package gov.epa.exp_data_gathering.parse.ECOTOX;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.JSONUtilities;
import gov.epa.exp_data_gathering.parse.UnitConverter;
import gov.epa.exp_data_gathering.parse.ToxVal.ParseToxVal;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
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
	
	public void getAcuteAquaticExperimentalRecords() {


		String propertyName=ExperimentalConstants.strNINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;
		
		String source=ExperimentalConstants.strSourceEcotox;
		
		List<RecordEcotox>recordsOriginal=null;
		
		boolean createOriginalRecords=true;
		
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
		
//		Hashtable<String, Double> htMWfromDTXSID = getMolWeightHashtable();//usi
						
		for (RecordEcotox re:recordsOriginal) {
			
			if(re.isAcceptable(4.0));//4 days
			
//			if(re.dtxsid.equals("DTXSID0034566")) {
//				System.out.println(gson.toJson(re));
//			}
			
			addExperimentalRecords(re, experimentalRecords);
//			addExperimentalRecords(re, experimentalRecords,htMWfromDTXSID);//only gets you 3 more chemicals- almost all in g/L!
			htRecordEcotox.put(re.test_id,re);
		}
		
		//Writer experimental records to Json file:
		String mainFolder = "Data" + File.separator + "Experimental" + File.separator + source;
		String fileNameJsonExperimentalRecords = "Ecotox_"+propertyName+" Experimental Records.json";
		JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(experimentalRecords),mainFolder+File.separator+fileNameJsonExperimentalRecords);
		
		//Write experimental records to excel file:
//		experimentalRecords.toExcel_File_Split(mainFolder+File.separator+fileNameJsonExperimentalRecords.replace("json", "xlsx"),100000);
				
//		ExperimentalRecord er0=experimentalRecords.get(0);		
//		String test_id=(String)er0.experimental_parameters.get("test_id");
//		RecordEcotox re=htRecordEcotox.get(test_id);
//		System.out.println(gson.toJson(re));
				
		
		Hashtable<String, List<ExperimentalRecord>> htER = createExpRecordHashtable(experimentalRecords);
				
		//Print the largest bad records:
		lookAtLargestDeviations(htRecordEcotox, htER);
		
		double avgSD=0;
		int count=0;
		int countOverall=0;

		for (String dtxsid:htER.keySet()) {
			List<ExperimentalRecord> records=htER.get(dtxsid);
			double SD=ParseToxVal.calculateSD(records);//TODO need to determine SD when converted to log values
			avgSD+=SD;
			count++;
			countOverall+=records.size();
		}
		
		avgSD/=(double)count;

		
//		assignLiteratureSourceNames(experimentalRecords);
//		System.out.println(gson.toJson(experimentalRecords));		

		System.out.println("originalRecords.size()="+recordsOriginal.size());
		System.out.println("experimentalRecords.size()="+experimentalRecords.size());
		System.out.println("Kept records\t"+countOverall);
		System.out.println("Unique SIDs\t"+htER.size());
		System.out.println("Avg SD\t"+avgSD);		
		
		
	}


	private Hashtable<String, Double> getMolWeightHashtable() {
		Hashtable<String,Double>htMWfromDTXSID=new Hashtable<>();

		Type listType = new TypeToken<ArrayList<JsonObject>>(){}.getType();
		
		List<JsonObject> jaMolWeight=null;
		try {
			jaMolWeight = gson.fromJson(new FileReader("data\\experimental\\ECOTOX\\mol_weight_look_up_from_dtxsid.json"), listType);
			
			for (JsonObject jo:jaMolWeight) {
				htMWfromDTXSID.put(jo.get("dtxsid").getAsString(),jo.get("mol_weight").getAsDouble());
//				System.out.println(jo.get("dtxsid").getAsString()+"\t"+jo.get("mol_weight").getAsDouble());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return htMWfromDTXSID;
	}


	private Hashtable<String, List<ExperimentalRecord>> createExpRecordHashtable(
			ExperimentalRecords experimentalRecords) {
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
		return htER;
	}


	private void lookAtLargestDeviations(Hashtable<String, RecordEcotox> htRecordEcotox,
			Hashtable<String, List<ExperimentalRecord>> htER) {
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
			

			if(biggest_diff>1.0) {
				System.out.println(dtxsid);

//				for (ExperimentalRecord er:recs) {
//					System.out.println("\t"+er.property_value_point_estimate_final+"\t"+er.property_value_units_final+"\t"+(String)er.experimental_parameters.get("test_id"));
//				}
				System.out.println("\tBiggest diff\t"+biggest_diff+"\t"+test_id_biggest_diff);
				
				System.out.println(gson.toJson(htRecordEcotox.get(test_id_biggest_diff))+"\n");
				
			}

		}
	}

	
	private void addExperimentalRecords(RecordEcotox r, ExperimentalRecords recordsExperimental,Hashtable<String,Double>htMWfromDTXSID) {

		
		ExperimentalRecord er1=r.toExperimentalRecord(1);
		
		if(er1.keep) {
			uc.convertRecord(er1);
			fixMolarUnits(r, htMWfromDTXSID, er1);
			recordsExperimental.add(er1);				
		}
		
		ExperimentalRecord er2=r.toExperimentalRecord(2);
				
		if(er2.keep) {
			uc.convertRecord(er2);
			
			fixMolarUnits(r, htMWfromDTXSID, er2);
			recordsExperimental.add(er2);	
		}
		
//		if(er1.keep && er2.keep) {
//			System.out.println(er1.dsstox_substance_id+"\t"+er1.property_value_point_estimate_final+"\t"+er2.property_value_point_estimate_final);
//		}
				
//		er.recordOriginal=r;
		
	}
	


	private void addExperimentalRecords(RecordEcotox r, ExperimentalRecords recordsExperimental) {
		
		ExperimentalRecord er1=r.toExperimentalRecord(1);
		
		if(er1.keep) {
			uc.convertRecord(er1);
			recordsExperimental.add(er1);				
		} else {
//			System.out.println(r.test_id+"\t"+er1.dsstox_substance_id+"\t"+er1.reason);
		}
		
		ExperimentalRecord er2=r.toExperimentalRecord(2);
				
		if(er2.keep) {
			uc.convertRecord(er2);
			recordsExperimental.add(er2);	
		} else {
//			System.out.println(r.test_id+"\t"+er1.dsstox_substance_id+"\t"+er2.reason);
		}
		
//		if(er1.keep && er2.keep) {
//			System.out.println(er1.dsstox_substance_id+"\t"+er1.property_value_point_estimate_final+"\t"+er2.property_value_point_estimate_final);
//		}
				
//		er.recordOriginal=r;
		
	}



	private void fixMolarUnits(RecordEcotox r, Hashtable<String, Double> htMWfromDTXSID, ExperimentalRecord er) {
		
		if(er.property_value_units_final==null) {
			System.out.println("Here 2 final units are null for original units="+er.property_value_units_original);
			return;
		}
		
		if(er.property_value_units_final.equals(ExperimentalConstants.str_M)) {
			
//			System.out.println(er.property_value_units_final+"\t"+htMWfromDTXSID.get(r.dtxsid));
			
			if (htMWfromDTXSID.get(r.dtxsid)!=null) {
				double MW=htMWfromDTXSID.get(r.dtxsid);
				er.property_value_point_estimate_final=er.property_value_point_estimate_final*MW;
				er.property_value_units_final=ExperimentalConstants.str_g_L;
			}
		}
	}

	void compareEcotoxToToxval() {

		ExperimentalRecords erECOTOX=ExperimentalRecords.loadFromJSON("data\\experimental\\ECOTOX\\Ecotox_96 hour fathead minnow LC50 Experimental Records.json");
		ExperimentalRecords erToxValECOTOX=ExperimentalRecords.loadFromJSON("data\\experimental\\ToxVal_v93\\ToxVal_v93 96 hour fathead minnow LC50 Experimental Records.json");
		
		List<String>sidsEcotox=new ArrayList<String>();
		List<String>sidsToxValEcotox=new ArrayList<String>();
		
		for (ExperimentalRecord er:erECOTOX) {
			if(!er.keep) continue;
			if(!er.property_value_units_final.equals(ExperimentalConstants.str_g_L))continue;
			if(!sidsEcotox.contains(er.dsstox_substance_id)) sidsEcotox.add(er.dsstox_substance_id);
		}
		
		for (ExperimentalRecord er:erToxValECOTOX) {			
			if(!er.keep) continue;
			if(!er.property_value_units_final.equals(ExperimentalConstants.str_g_L))continue;
			
			if(!sidsToxValEcotox.contains(er.dsstox_substance_id)) sidsToxValEcotox.add(er.dsstox_substance_id);
		}
		

		System.out.println(sidsEcotox.size());
		System.out.println(sidsToxValEcotox.size()+"\n");


		String propertyName=ExperimentalConstants.strNINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;
		String source=ExperimentalConstants.strSourceEcotox;
		String jsonPath = "data/experimental/"+source+File.separator+source+" "+propertyName+" original records.json";

		try {
			RecordEcotox[]recordsOriginal = gson.fromJson(new FileReader(jsonPath), RecordEcotox[].class);
			
			Hashtable<String,RecordEcotox>ht=new Hashtable<>();
			
			for (RecordEcotox re:recordsOriginal) {
				ht.put(re.dtxsid,re);
			}
			
			for (String dtxsidToxValEcotox:sidsToxValEcotox) {
				if(!sidsEcotox.contains(dtxsidToxValEcotox)) {
					System.out.println(dtxsidToxValEcotox);
					
					if(ht.containsKey(dtxsidToxValEcotox)) {
						System.out.println(gson.toJson(ht.get(dtxsidToxValEcotox))+"\n\n");
					}
					
				}
			}

			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}
	
	

	public static void main(String[] args) {
		ParseEcotox p = new ParseEcotox();
		p.uc.debug=true;
		
		p.getAcuteAquaticExperimentalRecords();
//		p.compareEcotoxToToxval();
		
//		p.maxExcelRows=999999;
		

	}

}
