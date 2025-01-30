package gov.epa.exp_data_gathering.parse.ToxVal;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
//import java.util.ArrayList;
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
	static String versionV94 ="v94";
	public static String versionProd ="prod";
	
	void getAcuteAquaticExperimentalRecords(String toxvalVersion, String commonName,double duration,String type,String criticalEffect,String propertyType) {

		
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		
		ToxValQuery tvq=new ToxValQuery();
		tvq.setConnectionToxVal(toxvalVersion);
		
		if (tvq.conn==null) return;

		String sql=null;
		
		if(toxvalVersion.equals(versionV93)) {
			sql=ToxValQuery.TOXVAL_FILTERED_QUERY_BY_SPECIES_AND_TYPE_V93;
		} else if (toxvalVersion.equals(versionProd)) {
			sql=ToxValQuery.TOXVAL_FILTERED_QUERY_BY_SPECIES_AND_TYPE_PROD;
		}
		
		
		List<ToxValRecord> toxValRecords = tvq.getRecords(commonName, type,sql);
		tvq.close();
		
//		for (ToxValRecord tr:toxValRecords) {
//			System.out.println(tr.toxval_id);
//		}
//		System.out.println(gson.toJson(toxValRecords));
//		if(true) return;
		
		Hashtable<String,ExperimentalRecords>htRecordsBySID=new Hashtable<>();

		
		ExperimentalRecords experimentalRecords=new ExperimentalRecords();
		for (ToxValRecord toxValRecord:toxValRecords) {
			if (!toxValRecord.isAcceptable(duration, criticalEffect, null)) continue;
//			if (!toxValRecord.toxval_units_original.equals(toxValRecord.toxval_units))
//				System.out.println(toxValRecord.toxval_units+"\t"+toxValRecord.toxval_units_original);
			ExperimentalRecord er=toxValRecord.toExperimentalRecord(toxvalVersion, duration,propertyType);

			if(er.original_source_name.equals("EnviroTox_v2")) continue;//dont use envirotox because sometimes drops the qualifier!
//			if(!er.original_source_name.equals("ECOTOX: EPA ORD")) continue;//only use ECOTOX			
//			if(!er.original_source_name.equals("ECOTOX: EPA ORD") && !er.original_source_name.equals("EnviroTox_v2")) continue;//use ECOTOX and envirotox
//			if(!er.original_source_name.equals("EnviroTox_v2")) continue;//only use envirotox
//			if(!er.original_source_name.equals("ECHA eChemPortal: ECHA REACH") && !er.original_source_name.equals("ECHA eChemPortal: ECHA CHEM")) continue;
//			if(!er.original_source_name.equals("ECHA eChemPortal: ECHA REACH")) continue;
//			if(!er.original_source_name.equals("ECHA eChemPortal: ECHA CHEM")) continue;
//			if(!er.original_source_name.equals("ECOTOX: EPA ORD") && !er.original_source_name.equals("ECHA eChemPortal: ECHA REACH") && !er.original_source_name.equals("ECHA eChemPortal: ECHA CHEM")) continue;
//			if(!er.original_source_name.equals("ECOTOX: EPA ORD") && !er.original_source_name.equals("ECHA eChemPortal: ECHA CHEM")) continue;
//			if(!er.original_source_name.equals("EFSA")) continue;
//			if(!er.original_source_name.equals("DOD ERED: USACE_ERDC_ERED_database_12_07_2018")) continue;


			experimentalRecords.add(er);
			
			if (!er.keep) continue;
			if(!er.property_value_units_final.equals(ExperimentalConstants.str_g_L)) continue;//dont use for stdev calcs

//			System.out.println(er.original_source_name);

			if(er.dsstox_substance_id==null) continue;
			
			if(htRecordsBySID.get(er.dsstox_substance_id)==null) {
				ExperimentalRecords records=new ExperimentalRecords();
				records.add(er);
				htRecordsBySID.put(er.dsstox_substance_id, records);
			} else {
				ExperimentalRecords records=htRecordsBySID.get(er.dsstox_substance_id);
				records.add(er);
			}
			
			
		}
		
		
		String propertyName=experimentalRecords.get(0).property_name;
		Hashtable<String, ExperimentalRecords> htER = experimentalRecords.createExpRecordHashtableBySID(ExperimentalConstants.str_g_L);
		ExperimentalRecords.calculateStdDev(htER, true);

//		assignLiteratureSourceNames(experimentalRecords);
//		System.out.println(gson.toJson(experimentalRecords));		

		System.out.println("toxValRecords.size()="+toxValRecords.size());
		System.out.println("experimentalRecords.size()="+experimentalRecords.size());

		//TODO get source_url to store in literature_sources table

		String mainFolder = "Data" + File.separator + "Experimental" + File.separator + "ToxVal_"+versionV93;
		String fileNameJsonExperimentalRecords = "ToxVal_"+toxvalVersion+" "+propertyName+" Experimental Records.json";
		JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(experimentalRecords),mainFolder+File.separator+fileNameJsonExperimentalRecords);
		
	}
	
	void getBCFExperimentalRecordsFish(String toxvalVersion,String propertyName) {
		
		boolean limitToFish=false;
		if(propertyName.toLowerCase().contains("fish")) {
			limitToFish=true;
		}
		
		boolean limitToWholeOrganism=false;
		if(propertyName.toLowerCase().contains("whole")) {
			limitToWholeOrganism=true;
		}

		//TODO- should we limit to standard test species for fish? 
		// For example see gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval.CreateAquaticToxicityRecords.validAquaticSpeciesToxvalv94() in TEST project
		
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
		
		ToxValQuery tvq=new ToxValQuery();
		tvq.setConnectionToxVal(toxvalVersion);
		if (tvq.conn==null) return;
		
		
		String sql=ToxValQuery.TOXVAL_FILTERED_QUERY_LOG_BCF;
		
		System.out.println(sql);

		List<ToxValRecord> toxValRecords = tvq.getRecords(sql);
		
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
		
		System.out.println(toxValRecords.size());
		
//		for (ToxValRecord tr:toxValRecords) {
//			System.out.println(tr.toxval_id);
//		}
//		System.out.println(gson.toJson(toxValRecords));
//		if(true) return;
		
		ExperimentalRecords experimentalRecords=new ExperimentalRecords();
		
		for (ToxValRecord toxValRecord:toxValRecords) {
//			if (!toxValRecord.isAcceptable(duration, criticalEffect, null)) continue;
//			if (!toxValRecord.toxval_units_original.equals(toxValRecord.toxval_units))
//				System.out.println(toxValRecord.toxval_units+"\t"+toxValRecord.toxval_units_original);
			ExperimentalRecord er=toxValRecord.toxvalBCF_to_ExperimentalRecord(toxvalVersion, propertyName, 
					ToxValQuery.propertyCategoryBioaccumulation,limitToFish,limitToWholeOrganism,htSuperCategory);
			experimentalRecords.add(er);
		}
		
		Hashtable<String,ExperimentalRecords>htSid=experimentalRecords.createExpRecordHashtableBySID(null);
		
		ExperimentalRecords.calculateStdDev(htSid, true);

		System.out.println("originalRecords.size()="+toxValRecords.size());
		System.out.println("experimentalRecords.size()="+experimentalRecords.size());
		System.out.println("experimentalRecords unique sids)="+htSid.size());
				
//		System.out.println(gson.toJson(experimentalRecords));		
//		System.out.println(toxValRecords.size());
//		System.out.println(experimentalRecords.size());
		//TODO get source_url to store in literature_sources table

		String mainFolder = "Data" + File.separator + "Experimental" + File.separator + "ToxVal_"+toxvalVersion;
		
		mainFolder+=File.separator+propertyName;
		new File(mainFolder).mkdirs();
		
		String fileNameJsonExperimentalRecords = "ToxVal_"+toxvalVersion+" Experimental Records.json";
		String fileNameJsonExperimentalRecordsBad = "ToxVal_"+toxvalVersion+" Experimental Records-Bad.json";
		experimentalRecords.toExcel_File_Split(mainFolder+File.separator+fileNameJsonExperimentalRecords.replace("json", "xlsx"),100000);
		
		ExperimentalRecords experimentalRecordsBad = experimentalRecords.dumpBadRecords();
		
		JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(experimentalRecords),mainFolder+File.separator+fileNameJsonExperimentalRecords);
		JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(experimentalRecordsBad),mainFolder+File.separator+fileNameJsonExperimentalRecordsBad);
		
		
		
	}
	

	//*
	
	
	
	/**
	 * this works for prod_dsstox- not v93 version since species table is different
	 * 
	 * @param tvq
	 * @return
	 */
	public static Hashtable<String, String> createSupercategoryHashtable(ToxValQuery tvq) {
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
//		p.getAcuteAquaticExperimentalRecords(versionV93,"Fathead minnow",ToxValQuery.FATHEAD_MINNOW_DURATION,ToxValQuery.TYPE_LC50,ToxValQuery.CRITICAL_EFFECT,ToxValQuery.propertyCategoryAcuteAquaticToxicity);
//		p.getAcuteAquaticExperimentalRecords(versionProd,"Fathead minnow",ToxValQuery.FATHEAD_MINNOW_DURATION,ToxValQuery.TYPE_LC50,ToxValQuery.CRITICAL_EFFECT,ToxValQuery.propertyCategoryAcuteAquaticToxicity);

//		String version=versionV94;
		String version=versionProd;
		p.getBCFExperimentalRecordsFish(version,ExperimentalConstants.strFishBCF);
		p.getBCFExperimentalRecordsFish(version,ExperimentalConstants.strFishBCFWholeBody);
		
	}

}

