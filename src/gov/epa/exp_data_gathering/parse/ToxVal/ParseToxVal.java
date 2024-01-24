package gov.epa.exp_data_gathering.parse.ToxVal;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
//import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.LookAndFeel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.JSONUtilities;
import gov.epa.exp_data_gathering.parse.LiteratureSource;




public class ParseToxVal {

	static String versionV93 ="v93";
	static String versionProd ="prod";
	
	void getAcuteAquaticExperimentalRecords(String toxvalVersion, String commonName,double duration,String type,String criticalEffect,String propertyType) {

		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		
		ToxValQuery tvq=new ToxValQuery();
		tvq.setConnectionToxVal(toxvalVersion);
		
		if (tvq.conn==null) return;

		List<ToxValRecord> toxValRecords = tvq.getRecords(commonName, type,ToxValQuery.TOXVAL_FILTERED_QUERY_BY_SPECIES_AND_TYPE_V93);
		tvq.close();
		
//		for (ToxValRecord tr:toxValRecords) {
//			System.out.println(tr.toxval_id);
//		}
//		System.out.println(gson.toJson(toxValRecords));
//		if(true) return;
		
		Hashtable<String,ExperimentalRecords>htRecordsBySID=new Hashtable<>();

		
		List<ExperimentalRecord> experimentalRecords=new ArrayList<>();
		for (ToxValRecord toxValRecord:toxValRecords) {
			if (!toxValRecord.isAcceptable(duration, criticalEffect, null)) continue;
//			if (!toxValRecord.toxval_units_original.equals(toxValRecord.toxval_units))
//				System.out.println(toxValRecord.toxval_units+"\t"+toxValRecord.toxval_units_original);
			ExperimentalRecord er=toxValRecord.toExperimentalRecord(toxvalVersion, duration,propertyType);
			experimentalRecords.add(er);
			
			
			if(htRecordsBySID.get(er.dsstox_substance_id)==null) {
				ExperimentalRecords records=new ExperimentalRecords();
				records.add(er);
				htRecordsBySID.put(er.dsstox_substance_id, records);
			} else {
				ExperimentalRecords records=htRecordsBySID.get(er.dsstox_substance_id);
				records.add(er);
			}
			
		}
		
		double avgSD=0;
		int count=0;
		int countOverall=0;

		for (String dtxsid:htRecordsBySID.keySet()) {
			ExperimentalRecords records=htRecordsBySID.get(dtxsid);
			double SD=calculateSD(records);//TODO need to determine SD when converted to log values
			avgSD+=SD;
			count++;
			countOverall+=records.size();
		}
		
		avgSD/=(double)count;

		
//		assignLiteratureSourceNames(experimentalRecords);
//		System.out.println(gson.toJson(experimentalRecords));		

		System.out.println("toxValRecords.size()="+toxValRecords.size());
		System.out.println("experimentalRecords.size()="+experimentalRecords.size());
		System.out.println("Kept records\t"+countOverall);
		System.out.println("Unique SIDs\t"+htRecordsBySID.size());
		System.out.println("Avg SD\t"+avgSD);

		//TODO get source_url to store in literature_sources table

		String mainFolder = "Data" + File.separator + "Experimental" + File.separator + "ToxVal_"+versionV93;
		String propertyName=experimentalRecords.get(0).property_name;
		String fileNameJsonExperimentalRecords = "ToxVal_"+toxvalVersion+" "+propertyName+" Experimental Records.json";
		JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(experimentalRecords),mainFolder+File.separator+fileNameJsonExperimentalRecords);
		
	}
	
	void getBCFExperimentalRecords(String toxvalVersion) {
		
		//TODO- should we limit to standard test species for fish? 
		// For example see gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval.CreateAquaticToxicityRecords.validAquaticSpeciesToxvalv94() in TEST project
		
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
		
		ToxValQuery tvq=new ToxValQuery();
		tvq.setConnectionToxVal(toxvalVersion);
		if (tvq.conn==null) return;

		List<ToxValRecord> toxValRecords = tvq.getRecords(ToxValQuery.TOXVAL_FILTERED_QUERY_LOG_BCF2);
		
		//Need to create a dictionary to map all fish by common name:
		Hashtable<String, String> htSuperCategory = createSupercategoryHashtable(tvq);

		htSuperCategory.put("phytoplankton", "omit");
		htSuperCategory.put("common shrimp", "omit");
		htSuperCategory.put("baskettail dragonfly", "omit");
		htSuperCategory.put("common bay mussel", "omit");
		
		htSuperCategory.put("biwi lake gudgeon, goby or willow shiner", "fish");
		htSuperCategory.put("willow shiner", "fish");
		htSuperCategory.put("golden ide", "fish");
		htSuperCategory.put("gobi", "fish");
		htSuperCategory.put("topmouth gudgeon", "fish");
		htSuperCategory.put("shorthead redhorse", "fish");
		htSuperCategory.put("golden redhorse", "fish");
		htSuperCategory.put("medaka, high-eyes", "fish");

		tvq.close();
		
		for (int i=0;i<toxValRecords.size();i++) {
			ToxValRecord t=toxValRecords.get(i);
			
			t.species_common=t.species_common.toLowerCase();
			String supercategory=htSuperCategory.get(t.species_common);
			
			if(supercategory==null || !supercategory.contains("fish")) {
//				System.out.println(t.species_common+"\t"+supercategory);
				toxValRecords.remove(i--);
			}
		}
//		for (ToxValRecord tr:toxValRecords) {
//			System.out.println(tr.toxval_id);
//		}
		
//		System.out.println(gson.toJson(toxValRecords));
		
		
//		if(true) return;
		
		
		Hashtable<String,ExperimentalRecords>htRecordsBySID=new Hashtable<>();
		
		ExperimentalRecords experimentalRecords=new ExperimentalRecords();
		
		for (ToxValRecord toxValRecord:toxValRecords) {
//			if (!toxValRecord.isAcceptable(duration, criticalEffect, null)) continue;
//			if (!toxValRecord.toxval_units_original.equals(toxValRecord.toxval_units))
//				System.out.println(toxValRecord.toxval_units+"\t"+toxValRecord.toxval_units_original);
			ExperimentalRecord er=toxValRecord.toxvalBCF_to_ExperimentalRecord(toxvalVersion, ToxValQuery.propertyCategoryBioaccumulation);
			experimentalRecords.add(er);
			
			if (er.dsstox_substance_id==null || !er.dsstox_substance_id.contains("DTXSID")) continue;

			
			if (er.dsstox_substance_id==null) {
				er.keep=false;
				er.reason="No DTXSID";
			}

			if (!er.experimental_parameters.get("tissue").equals("Whole body")) {
				er.keep=false;
				er.reason="Not whole body";
			}
			
//			if (!er.experimental_parameters.get("media").equals("FW")) {
//				er.keep=false;
//				er.reason="Not FW";
//			}

//			if (!er.experimental_parameters.get("exposure_type").equals("FT")) {
//				er.keep=false;
//				er.reason="Not FT";
//			}
			
//			if (er.experimental_parameters.get("method")==null || 
//					!er.experimental_parameters.get("method").equals("Steady state")) {
//				er.keep=false;
//				er.reason="Not Steady state";
//			} 
			
			if(!er.keep) {
//				System.out.println(er.reason+"\t"+er.experimental_parameters.get("tissue")+"\t"+er.experimental_parameters.get("method")+"\t"+er.experimental_parameters.get("media")+"\t"+er.experimental_parameters.get("exposure_type"));
				continue;
			}
			
			if(htRecordsBySID.get(er.dsstox_substance_id)==null) {
				ExperimentalRecords records=new ExperimentalRecords();
				records.add(er);
				htRecordsBySID.put(er.dsstox_substance_id, records);
			} else {
				ExperimentalRecords records=htRecordsBySID.get(er.dsstox_substance_id);
				records.add(er);
			}
		}
		
		
		double avgSD=0;
		int count=0;
		int countOverall=0;
		
		for (String dtxsid:htRecordsBySID.keySet()) {
			ExperimentalRecords records=htRecordsBySID.get(dtxsid);
			double SD=calculateSD(records);
			avgSD+=SD;
			count++;
			countOverall+=records.size();
		}
		
		avgSD/=(double)count;
		
		
//		System.out.println(gson.toJson(experimentalRecords));		
//		System.out.println(toxValRecords.size());
//		System.out.println(experimentalRecords.size());
		//TODO get source_url to store in literature_sources table

		String mainFolder = "Data" + File.separator + "Experimental" + File.separator + "ToxVal_"+versionV93;
		String propertyName=experimentalRecords.get(0).property_name;
		String fileNameJsonExperimentalRecords = "ToxVal_"+versionV93+" "+propertyName+" Experimental Records.json";
		JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(experimentalRecords),mainFolder+File.separator+fileNameJsonExperimentalRecords);
		
		System.out.println("Kept records\t"+countOverall);
		System.out.println("experimentalRecords.size()="+experimentalRecords.size());
		System.out.println("Unique SIDs\t"+htRecordsBySID.size());
		System.out.println("Avg SD\t"+avgSD);
		
		experimentalRecords.toExcel_File_Split(mainFolder+File.separator+fileNameJsonExperimentalRecords.replace("json", "xlsx"));
		
	}
	
	/**
	 * TODO this needs to make sure all units are correct and for g/L values need SD of log(g/L)
	 * 
	 * @param recs
	 * @return
	 */
	public static double calculateSD(ExperimentalRecords recs) {
		
		Gson gson=new Gson();
		
		int count=0;
		double avg=0;
		
		
		for (ExperimentalRecord er:recs) {
			
//			System.out.println(er.property_value_units_final);
			
//			if (!er.property_value_units_final.equals(ExperimentalConstants.str_g_L)) continue;
			
			if (er.property_value_point_estimate_final==null) {
				try {
					er.property_value_point_estimate_final=(er.property_value_min_final+er.property_value_max_final)/2.0;
				} catch (Exception ex) {
					System.out.println(er.toJSON());
					continue;
				}
			}
			avg+=er.property_value_point_estimate_final;
			count++;
			
		}
		avg/=(double)count;
		
		
		
		double SD=0;
		
		for (ExperimentalRecord er:recs) {
			
			if (!er.property_value_units_final.equals(ExperimentalConstants.str_g_L)) continue;

			if (er.property_value_point_estimate_final!=null) {
				SD+=Math.pow(er.property_value_point_estimate_final-avg,2);
				
//				if(count==40) {
//					System.out.println("\t"+er.property_value_point_estimate_final);
//				}
				
			} else {
				System.out.println("Missing final point estimate");
			}
		}
		SD/=(double)count;
		
		SD=Math.sqrt(SD);
		
//		System.out.println(SD+"\t"+count);
		
		
		return SD;
		
	}
	

	/**
	 * this works for prod_dsstox- not v93 version since species table is different
	 * 
	 * @param tvq
	 * @return
	 */
	private Hashtable<String, String> createSupercategoryHashtable(ToxValQuery tvq) {
		Hashtable<String,String>ht=new Hashtable<>();
		
		String sql="select species_common, species_supercategory from species";
		
		try {
			
			Statement st = tvq.conn.createStatement();			
			ResultSet rs = st.executeQuery(sql);
			
			while (rs.next()) {
				String species_common=rs.getString(1);
				String species_supercategory=rs.getString(2);
//				System.out.println(species_common+"\t"+species_supercategory);
				ht.put(species_common, species_supercategory);
			}
			
	
//			System.out.println(sql);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ht;
	}
	
	
//	void assignLiteratureSourceNames(List<ExperimentalRecord> experimentalRecords) {
//		
//		Hashtable <String,List<LiteratureSource>>htLitSrc=new Hashtable<>();
//		
//		for (ExperimentalRecord er:experimentalRecords) {
//			
//			if (er.literatureSource==null) continue;
//			
//			String key=null;
//			
//			LiteratureSource ls=er.literatureSource;
//			
////			String name="";
////			
////			if(ls.author!=null && ls.title!=null) {
//////				name=ls.author+" ("+ls.year+"). "+ls.title;
////				name=ls.author+" ("+ls.year+")";
//////				System.out.println(name);
////			} else if (ls.title!=null){
////				name=ls.title+" ("+ls.year+").";
////				
//////				System.out.println(name);
////
////			} else {
////				System.out.println(ls.author);
////				System.out.println(ls.title);
////				System.out.println(ls.year);
////				System.out.println(ls.longReference);
////				System.out.println("\n");
////			}
//			String name=er.literatureSource.longReference;
//			er.literatureSource.name=name;
//			
//			
//			if(htLitSrc.get(name)==null) {
//				List<LiteratureSource>srcs=new ArrayList<>();
//				srcs.add(er.literatureSource);
//				htLitSrc.put(name,srcs);
//			} else {
//				List<LiteratureSource>srcs=htLitSrc.get(name);
//				srcs.add(er.literatureSource);
//			}
//		}
//		
//		//Make sure we didnt accidentally assign different references to same name:
//		
//		for (String name:htLitSrc.keySet()) {
//			List<LiteratureSource>srcs=htLitSrc.get(name);
//			
//			LiteratureSource ls0=srcs.get(0);
//			String key0=ls0.author+ls0.title+ls0.year+ls0.longReference;
//			
//			for (int i=1;i<srcs.size();i++) {
//				LiteratureSource lsi=srcs.get(i);	
//				String keyi=lsi.author+lsi.title+lsi.year+lsi.longReference;	
//				
//				if(!key0.equals(keyi)) {
//					System.out.println("key0:"+key0);
//					System.out.println("keyi:"+keyi+"\n");
//				}
//			}
//		}
//		
//		
//		
//	}
	
	
	static void lookatrecords() {
		Gson gson=new Gson();
		
		String filePath="data\\experimental\\ToxVal_v93\\ToxVal_v93 96 hr Fathead Minnow LC50 Experimental Records.json";

		ExperimentalRecord [] records=gson.fromJson(filePath, ExperimentalRecord[].class);
		
		for (ExperimentalRecord er:records) {
			
			LiteratureSource ls=er.literatureSource;
			
			
			
		}
		

	}
	
	public static void main(String[] args) {
		
//		lookatrecords();
		
		
		ParseToxVal p=new ParseToxVal();
		// TODO Auto-generated method stub
		p.getAcuteAquaticExperimentalRecords(versionV93,"Fathead minnow",ToxValQuery.FATHEAD_MINNOW_DURATION,ToxValQuery.TYPE_LC50,ToxValQuery.CRITICAL_EFFECT,ToxValQuery.propertyCategoryAcuteAquaticToxicity);
//		p.getAcuteAquaticExperimentalRecords(versionProd,"Fathead minnow",ToxValQuery.FATHEAD_MINNOW_DURATION,ToxValQuery.TYPE_LC50,ToxValQuery.CRITICAL_EFFECT,ToxValQuery.propertyCategoryAcuteAquaticToxicity);

		//TODO look at UnitConverter.convertRecord- should we use convertToxicity or convertSolubility??
		//TODO get journal citation so can store in exp_prop
		
//		p.getBCFExperimentalRecords(versionProd);
		
	}

}
