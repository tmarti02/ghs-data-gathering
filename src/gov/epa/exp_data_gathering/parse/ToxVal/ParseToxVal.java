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

import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.JSONUtilities;
import gov.epa.exp_data_gathering.parse.LiteratureSource;




public class ParseToxVal {

	static String version ="v93";
	
	void getAcuteAquaticExperimentalRecords(String commonName,double duration,String type,String criticalEffect,String propertyType) {

		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		
		ToxValQuery tvq=new ToxValQuery();
		tvq.setConnectionToxValV93();
		List<ToxValRecord> toxValRecords = tvq.getRecords(commonName, type,ToxValQuery.TOXVAL_FILTERED_QUERY_BY_SPECIES_AND_TYPE_V93);
		tvq.close();
		
//		for (ToxValRecord tr:toxValRecords) {
//			System.out.println(tr.toxval_id);
//		}
//		System.out.println(gson.toJson(toxValRecords));
//		if(true) return;
		
		List<ExperimentalRecord> experimentalRecords=new ArrayList<>();
		for (ToxValRecord toxValRecord:toxValRecords) {
			if (!toxValRecord.isAcceptable(duration, criticalEffect, null)) continue;
//			if (!toxValRecord.toxval_units_original.equals(toxValRecord.toxval_units))
//				System.out.println(toxValRecord.toxval_units+"\t"+toxValRecord.toxval_units_original);
			ExperimentalRecord er=toxValRecord.toExperimentalRecord(version, duration,propertyType);
			experimentalRecords.add(er);
		}
		
		
//		assignLiteratureSourceNames(experimentalRecords);
//		System.out.println(gson.toJson(experimentalRecords));		
//		System.out.println(toxValRecords.size());
//		System.out.println(experimentalRecords.size());
		//TODO get source_url to store in literature_sources table

		String mainFolder = "Data" + File.separator + "Experimental" + File.separator + "ToxVal_"+version;
		String propertyName=experimentalRecords.get(0).property_name;
		String fileNameJsonExperimentalRecords = "ToxVal_"+version+" "+propertyName+" Experimental Records.json";
		JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(experimentalRecords),mainFolder+File.separator+fileNameJsonExperimentalRecords);
		
	}
	
	void getBCFExperimentalRecords() {
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
		
		version="";
		ToxValQuery tvq=new ToxValQuery();
		tvq.setConnectionToxVal();
		List<ToxValRecord> toxValRecords = tvq.getRecords(ToxValQuery.TOXVAL_FILTERED_QUERY_LOG_BCF2);
		
		//Need to create a dictionary to map all fish by common name:
		Hashtable<String, String> htSuperCategory = createSupercategoryHashtable(tvq);

		htSuperCategory.put("phytoplankton", "seabug");
		htSuperCategory.put("baskettail dragonfly", "bug");
		htSuperCategory.put("willow shiner", "fish");
		htSuperCategory.put("golden ide", "fish");
		htSuperCategory.put("gobi", "fish");
		htSuperCategory.put("topmouth gudgeon", "fish");
		htSuperCategory.put("shorthead redhorse", "fish");
		htSuperCategory.put("golden redhorse", "fish");
		htSuperCategory.put("medaka, high-eyes", "fish");
		htSuperCategory.put("common bay mussel", "hate");
		htSuperCategory.put("biwi lake gudgeon, goby or willow shiner", "fish");
		htSuperCategory.put("common shrimp", "seabug");
		
		
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
		HashSet<String>dtxsids=new HashSet<>();
		
		ExperimentalRecords experimentalRecords=new ExperimentalRecords();
		
		for (ToxValRecord toxValRecord:toxValRecords) {
//			if (!toxValRecord.isAcceptable(duration, criticalEffect, null)) continue;
//			if (!toxValRecord.toxval_units_original.equals(toxValRecord.toxval_units))
//				System.out.println(toxValRecord.toxval_units+"\t"+toxValRecord.toxval_units_original);
			ExperimentalRecord er=toxValRecord.toxvalBCF_to_ExperimentalRecord(version, ToxValQuery.propertyCategoryBioaccumulation);
			experimentalRecords.add(er);
			dtxsids.add(er.dsstox_substance_id);
		}
		
		
//		System.out.println(gson.toJson(experimentalRecords));		
//		System.out.println(toxValRecords.size());
//		System.out.println(experimentalRecords.size());
		//TODO get source_url to store in literature_sources table

		String mainFolder = "Data" + File.separator + "Experimental" + File.separator + "ToxVal_"+version;
		String propertyName=experimentalRecords.get(0).property_name;
		String fileNameJsonExperimentalRecords = "ToxVal_"+version+" "+propertyName+" Experimental Records.json";
		JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(experimentalRecords),mainFolder+File.separator+fileNameJsonExperimentalRecords);
		
		System.out.println(experimentalRecords.size());
		System.out.println(dtxsids.size());
		
		experimentalRecords.toExcel_File_Split(mainFolder+File.separator+fileNameJsonExperimentalRecords.replace("json", "xlsx"));
		
	}

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
//		p.getAcuteAquaticExperimentalRecords("Fathead minnow",ToxValQuery.FATHEAD_MINNOW_DURATION,ToxValQuery.TYPE,ToxValQuery.CRITICAL_EFFECT,ToxValQuery.propertyCategoryAcuteAquaticToxicity);
		//TODO look at UnitConverter.convertRecord- should we use convertToxicity or convertSolubility??
		//TODO get journal citation so can store in exp_prop
		
		p.getBCFExperimentalRecords();
		
	}

}
