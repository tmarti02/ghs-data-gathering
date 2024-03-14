package gov.epa.exp_data_gathering.parse.ECOTOX;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.JSONUtilities;
import gov.epa.exp_data_gathering.parse.UnitConverter;
import gov.epa.exp_data_gathering.parse.ToxVal.ParseToxVal;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
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

	String source=ExperimentalConstants.strSourceEcotox_2023_12_14;
	String folder="data\\experimental\\"+source+"\\";
	String filepathWSexpLookup=folder+"WS_query_to_get_median_logWS_g_L.json";
	String filepathWSpred=folder+"WS pred xgb.json";
	
	
	public void getAcuteAquaticExperimentalRecords(boolean excludeByExposureType,boolean includeConc2) {

		String propertyName=ExperimentalConstants.strNINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;
		String source=ExperimentalConstants.strSourceEcotox_2023_12_14;
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

			
			if(!re.isAcceptableDuration(4.0)) {
//				System.out.println(re.getStudyDurationValueInDays());
				continue;//4 days
			}
			
//			if(re.dtxsid.equals("DTXSID0034566")) {
//				System.out.println(gson.toJson(re));
//			}
			
			addExperimentalRecords(re, experimentalRecords,source,includeConc2);
//			addExperimentalRecords(re, experimentalRecords,htMWfromDTXSID);//only gets you 3 more chemicals- almost all in g/L!
			htRecordEcotox.put(re.test_id,re);
		}

//		if(excludeByWS) excludeByWS(experimentalRecords);
		
		if(excludeByExposureType) excludeByExposureType(experimentalRecords);

		//TODO filter out exposure_type="NR"

		
		Hashtable<String, List<ExperimentalRecord>> htER = createExpRecordHashtable(experimentalRecords);
//		compareExposureTypeFactors(experimentalRecords);
		compareConcentrationTypeFactors(experimentalRecords);
		//Print the largest bad records:
//		lookAtLargestDeviations(htRecordEcotox, htER);
//		printRecords(htRecordEcotox, htER);
		
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
		
		
		//Writer experimental records to Json file:
		String mainFolder = "Data" + File.separator + "Experimental" + File.separator + source;
		String fileNameJsonExperimentalRecords = source+"_"+propertyName+" Experimental Records.json";
		JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(experimentalRecords),mainFolder+File.separator+fileNameJsonExperimentalRecords);
		
		//Write experimental records to excel file:
		experimentalRecords.toExcel_File_Split(mainFolder+File.separator+fileNameJsonExperimentalRecords.replace("json", "xlsx"),100000);

		
	}





	private void compareConcentrationTypeFactors(ExperimentalRecords experimentalRecords) {
		Hashtable<String, Double>htER_A=createExpRecordHashtableConcentrationType(experimentalRecords, "Active ingredient");
		Hashtable<String, Double>htER_T=createExpRecordHashtableConcentrationType(experimentalRecords, "Total");
		Hashtable<String, Double>htER_F=createExpRecordHashtableConcentrationType(experimentalRecords, "Formulation");
		Hashtable<String, Double>htER_U=createExpRecordHashtableConcentrationType(experimentalRecords, "Unionized");
		Hashtable<String, Double>htER_D=createExpRecordHashtableConcentrationType(experimentalRecords, "Dissolved");
				
		compareFactors(htER_A, htER_T,"Active ingredient","Total");
		compareFactors(htER_A, htER_F,"Active ingredient","Formulation");
		compareFactors(htER_D, htER_T,"Dissolved","Total");
		compareFactors(htER_T, htER_U,"Total","Unionized");
//		compareFactors(htER_F, htER_R,"Flowthrough","Renewal");
		
		System.out.println("A\t"+htER_A.size());
		System.out.println("T\t"+htER_T.size());
		System.out.println("F\t"+htER_F.size());
		System.out.println("D\t"+htER_F.size());
		System.out.println("U\t"+htER_F.size());
	}

	private void compareExposureTypeFactors(ExperimentalRecords experimentalRecords) {
		Hashtable<String, Double>htER_F=createExpRecordHashtableExposureType(experimentalRecords, "F");
		Hashtable<String, Double>htER_S=createExpRecordHashtableExposureType(experimentalRecords, "S");
		Hashtable<String, Double>htER_R=createExpRecordHashtableExposureType(experimentalRecords, "R");
				
		compareFactors(htER_F, htER_S,"Flowthrough","Static");
		compareFactors(htER_F, htER_R,"Flowthrough","Renewal");
		
		System.out.println("F\t"+htER_F.size());
		System.out.println("S\t"+htER_S.size());
		System.out.println("R\t"+htER_R.size());
	}




	private void compareFactors(Hashtable<String, Double> ht1, Hashtable<String, Double> ht2,String factor1,String factor2) {
		List<Double>xList=new ArrayList<>();
		List<Double>yList=new ArrayList<>();
		
		for(String dtxsid:ht1.keySet()) {
			if(!ht2.containsKey(dtxsid)) continue;
			double valF=ht1.get(dtxsid);
			double valS=ht2.get(dtxsid);
			xList.add(valF);
			yList.add(valS);
		}
		double []x=new double[xList.size()];
		double []y=new double[yList.size()];
		
		for(int i=0;i<xList.size();i++) x[i]=xList.get(i);
		for(int i=0;i<yList.size();i++) y[i]=yList.get(i);
		
		String xtitle=factor1+" median logTox_g_L";
		String ytitle=factor2+" median logTox_g_L";
//		String title="Test set predictions for the "+methodColumnName+" method";
		String title=factor1+" vs "+factor2;
		fraChart.JLabelChart fc = new fraChart.JLabelChart(x,y,title,xtitle,ytitle);

		fc.doDrawLegend=false;
		fc.doDrawStatsR2=true;
		fc.doDrawStatsMAE=false;
		fc.setVisible(true);
		
		fc.WriteImageToFile("data\\experimental\\ECOTOX_2023_12_14\\"+title+".png");
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
	
	/**
	 * 
	 * -- WS query to get median logWS_g_L for each DTXSID from WS modeling dataset 
	 * select dpc.dtxsid, percentile_cont(0.5) WITHIN GROUP (ORDER BY log10(dpc.property_value*dr.mol_weight)) as median_logWS_g_L 
	 * from qsar_datasets.datasets d
	 * join qsar_datasets.data_points dp on dp.fk_dataset_id = d.id 
	 * join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id
	 * join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id 
	 * join qsar_datasets.properties p on d.fk_property_id = p.id 
	 * join qsar_datasets.datasets_in_cheminformatics_modules did on did.fk_property_id = d.fk_property_id
	 * join qsar_datasets.units u on u.id =d.fk_unit_id_contributor
	 * left join qsar_models.dsstox_records dr on dr.dtxsid=dpc.dtxsid 
	 * where d.id =did.fk_datasets_id and keep=true and p.name='Water solubility' 
	 * group by dpc.dtxsid;
	 * 
	 * @return
	 */
	private Hashtable<String, Double> createExpWSHashtable() {
		Hashtable<String,Double>htWSfromDTXSID=new Hashtable<>();

		Type listType = new TypeToken<ArrayList<JsonObject>>(){}.getType();
		
		List<JsonObject> jaMolWeight=null;
		try {
			
			File file=new File(filepathWSexpLookup);
			
			if(!file.exists()) {
				System.out.println("Missing WS lookup file:\t"+filepathWSexpLookup);
				return null;
			}
			
			jaMolWeight = gson.fromJson(new FileReader(filepathWSexpLookup), listType);
			
			for (JsonObject jo:jaMolWeight) {
				String dtxsid=jo.get("dtxsid").getAsString();
				Double ws_g_L=Math.pow(10.0,jo.get("median_logws_g_l").getAsDouble());//convert log value back to just g_L
				htWSfromDTXSID.put(dtxsid,ws_g_L);
//				System.out.println(dtxsid+"\t"+ws_g_L);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return htWSfromDTXSID;
	}



	private Hashtable<String, List<ExperimentalRecord>> createExpRecordHashtable(
			ExperimentalRecords experimentalRecords) {
		Hashtable<String,List<ExperimentalRecord>>htER=new Hashtable<>();
		
		for (ExperimentalRecord er:experimentalRecords)  {
			
			if(!er.keep) continue;
			
			//Only use the ones with g/L in the stats calcs:
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
	
	private Hashtable<String, Double> createExpRecordHashtableExposureType(
			ExperimentalRecords experimentalRecords,String exposureType) {
		
		Hashtable<String,List<Double>>htER=new Hashtable<>();
		
		for (ExperimentalRecord er:experimentalRecords)  {
			if(!er.keep) continue;
			if(!er.experimental_parameters.get("exposure_type").equals(exposureType)) continue;
			
			//Only use the ones with g/L in the stats calcs:
			if(er.property_value_units_final.equals(ExperimentalConstants.str_g_L)) {
//				System.out.println(er.casrn+"\t"+er.property_value_point_estimate_final);
				
				if(htER.containsKey(er.dsstox_substance_id) ) {
					List<Double>recs=htER.get(er.dsstox_substance_id);
					recs.add(Math.log10(er.property_value_point_estimate_final));	
				} else {
					List<Double>recs=new ArrayList<>();
					recs.add(Math.log10(er.property_value_point_estimate_final));
					htER.put(er.dsstox_substance_id, recs);
				}
			}
		}
		Hashtable<String,Double>htER_median_logTox_g_L=new Hashtable<>();
		
		for (String dtxsid:htER.keySet()) {
			List<Double>values=htER.get(dtxsid);
			Collections.sort(values);
			htER_median_logTox_g_L.put(dtxsid,getMedianValue(values));
		}
		
		return htER_median_logTox_g_L;
	}

	private Hashtable<String, Double> createExpRecordHashtableConcentrationType(
			ExperimentalRecords experimentalRecords,String exposureType) {
		
		Hashtable<String,List<Double>>htER=new Hashtable<>();
		
		for (ExperimentalRecord er:experimentalRecords)  {
			if(!er.keep) continue;
			if(!er.experimental_parameters.get("concentration_type").equals(exposureType)) continue;
			
			//Only use the ones with g/L in the stats calcs:
			if(er.property_value_units_final.equals(ExperimentalConstants.str_g_L)) {
//				System.out.println(er.casrn+"\t"+er.property_value_point_estimate_final);
				
				if(htER.containsKey(er.dsstox_substance_id) ) {
					List<Double>recs=htER.get(er.dsstox_substance_id);
					recs.add(Math.log10(er.property_value_point_estimate_final));	
				} else {
					List<Double>recs=new ArrayList<>();
					recs.add(Math.log10(er.property_value_point_estimate_final));
					htER.put(er.dsstox_substance_id, recs);
				}
			}
		}
		Hashtable<String,Double>htER_median_logTox_g_L=new Hashtable<>();
		
		for (String dtxsid:htER.keySet()) {
			List<Double>values=htER.get(dtxsid);
			Collections.sort(values);
			htER_median_logTox_g_L.put(dtxsid,getMedianValue(values));
		}
		
		return htER_median_logTox_g_L;
	}
	
	double getMedianValue(List<Double>values) {
		int size=values.size();
		if (size % 2 == 0) {//even number of records, need to determine average of middle 2 values
			double v1 = values.get(size / 2 - 1);
			double v2 = values.get(size / 2);
			return (v1 +v2) / 2.0;
		} else if (size==1) {//only 1 record
			return values.get(0);
		} else {//odd number of records, use middle one
			return values.get(size/2);
		}	
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
	
	private void printRecords(Hashtable<String, RecordEcotox> htRecordEcotox,
			Hashtable<String, List<ExperimentalRecord>> htER) {
		
		double maxVal=0;
		
		
		for(String dtxsid:htER.keySet()) {
			List<ExperimentalRecord>recs=htER.get(dtxsid);
			
//			System.out.println(dtxsid);
//			System.out.println(recs.get(0).casrn);
			
			for (ExperimentalRecord er:recs) {
				
//				System.out.println(er.casrn+"\t"+er.property_value_point_estimate_original+"\t"+er.property_value_units_original+"\t"+er.property_value_point_estimate_final+"\t"+er.property_value_units_final);
				System.out.println(er.experimental_parameters.get("test_id")+"\t"+er.dsstox_substance_id+"\t"+er.casrn+"\t"+er.property_value_point_estimate_final+"\t"+er.property_value_units_final);
				
				if(er.property_value_point_estimate_final>maxVal) maxVal=er.property_value_point_estimate_final;
				
			}
			
		}
		
		System.out.println("maxVal="+maxVal+" g/L");
	}
	
	private void excludeByWS(ExperimentalRecords records) {

//		Hashtable<String, Double>htWS=createExpWSHashtable();
		
		Hashtable<String, Double>htWS=getHashtableWSpred();
		
		int count=0;
		int countUnmeasured=0;
		int countMeasured=0;
		int countNoExpWS=0;

		for(ExperimentalRecord er:records) {
			Double WS_g_L=null;
			
			if(htWS.containsKey(er.dsstox_substance_id)) {
				WS_g_L=htWS.get(er.dsstox_substance_id);
				
				
				if(er.property_value_point_estimate_final > WS_g_L && er.property_value_units_final.equals(ExperimentalConstants.str_g_L)) {
					er.keep=false;
					er.reason="Toxicity exceeds experimental median water solubility";
									
					
					String chem_analysis_method=(String)er.experimental_parameters.get("chem_analysis_method");
					
					if(chem_analysis_method.equals("U") || chem_analysis_method.equals("--") || chem_analysis_method.equals("NC") || chem_analysis_method.equals("NR") || chem_analysis_method.equals("X")) countUnmeasured++;
					if(chem_analysis_method.equals("M") || chem_analysis_method.equals("Z")) countMeasured++;
					
//					System.out.println(++count+"\t"+er.property_value_point_estimate_final+"\t"+WS_g_L+"\t"+chem_analysis_method);
				}
			} else {
				countNoExpWS++;
			}

			//				System.out.println(er.casrn+"\t"+er.property_value_point_estimate_original+"\t"+er.property_value_units_original+"\t"+er.property_value_point_estimate_final+"\t"+er.property_value_units_final);
			//				System.out.println(er.experimental_parameters.get("test_id")+"\t"+er.dsstox_substance_id+"\t"+er.casrn+"\t"+er.property_value_point_estimate_final+"\t"+er.property_value_units_final+"\t"+WS_g_L);
		}
		
		System.out.println("count where Tox>median exp WS\t"+count);
		System.out.println("count unmeasured\t"+countUnmeasured);
		System.out.println("count measured\t"+countMeasured);
		System.out.println("count no exp WS\t"+countNoExpWS);

	}
	
	private void excludeByExposureType(ExperimentalRecords records) {

		int count=0;
		for(ExperimentalRecord er:records) {
			
			if(!er.keep) continue;
			
			if(er.experimental_parameters.get("exposure_type")!=null) {
				String exposure_type=(String)er.experimental_parameters.get("exposure_type");
				if(exposure_type.contains("Not reported")) {
					er.keep=false;
					er.reason="exposure_type is not reported";
					count++;
				}
			}
		}
		
		System.out.println("Count omitted by exposure_type="+count);
		

	}


	
//	private void addExperimentalRecords(RecordEcotox r, ExperimentalRecords recordsExperimental,Hashtable<String,Double>htMWfromDTXSID,String sourceName) {
//
//		
//		ExperimentalRecord er1=r.toExperimentalRecord(1,sourceName);
//		
//		if(er1.keep) {
//			uc.convertRecord(er1);
//			fixMolarUnits(r, htMWfromDTXSID, er1);
//			recordsExperimental.add(er1);				
//		}
//		
//		ExperimentalRecord er2=r.toExperimentalRecord(2,sourceName);
//				
//		if(er2.keep) {
//			uc.convertRecord(er2);
//			
//			fixMolarUnits(r, htMWfromDTXSID, er2);
//			recordsExperimental.add(er2);	
//		}
//		
////		if(er1.keep && er2.keep) {
////			System.out.println(er1.dsstox_substance_id+"\t"+er1.property_value_point_estimate_final+"\t"+er2.property_value_point_estimate_final);
////		}
//				
////		er.recordOriginal=r;
//		
//	}
	


	private void addExperimentalRecords(RecordEcotox r, ExperimentalRecords recordsExperimental,String sourceName, boolean includeConc2 ) {
		
		ExperimentalRecord er1=r.toExperimentalRecord(1,sourceName);
		
		if(er1.keep) {
			uc.convertRecord(er1);
			recordsExperimental.add(er1);				
		} else {
//			System.out.println(r.test_id+"\t"+er1.dsstox_substance_id+"\t"+er1.reason);
		}

		if (includeConc2) {
			//Dont keep second tox values? they agree half the time and the other half they are way off		
			ExperimentalRecord er2=r.toExperimentalRecord(2,sourceName);

			if(er2.keep) {
				uc.convertRecord(er2);
				recordsExperimental.add(er2);	
			} else {
				//System.out.println(r.test_id+"\t"+er1.dsstox_substance_id+"\t"+er2.reason);
			}
		}
		
//		if(er1.keep && er2.keep) {
//			System.out.println(er1.dsstox_substance_id+"\t"+er1.property_value_point_estimate_final+"\t"+er2.property_value_point_estimate_final);
//		}
				
		
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
		String source=ExperimentalConstants.strSourceEcotox_2023_12_14;
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
	
	void compareExpToPredWs() {
		Hashtable<String, Double>htWS_exp= createExpWSHashtable();
		
		try {
			Hashtable<String, Double> htPredWS = getHashtableWSpred();

			System.out.println(htWS_exp.size());
			System.out.println(htPredWS.size());
			
			for (String dtxsid:htPredWS.keySet()) {
				if(!htWS_exp.containsKey(dtxsid))continue;
					
				System.out.println(dtxsid+"\t"+htWS_exp.get(dtxsid)+"\t"+htPredWS.get(dtxsid));
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}





	private Hashtable<String, Double> getHashtableWSpred()  {
		Gson gson=new Gson();
		Hashtable<String, Double> htPredWS;
		try {
			htPredWS = gson.fromJson(new FileReader(filepathWSpred), (Hashtable.class));
			return htPredWS;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static void main(String[] args) {
		ParseEcotox p = new ParseEcotox();
		p.uc.debug=true;
		
		boolean excludeByExposureType=true;
		boolean includeConc2=false;
		p.getAcuteAquaticExperimentalRecords(excludeByExposureType,includeConc2);
		//TODO implement filter to exclude LC50>10 * baseline LC50
		
//		p.compareEcotoxToToxval();
		
//		p.maxExcelRows=999999;
//		p.compareExpToPredWs();

	}

}



