package gov.epa.exp_data_gathering.parse.ToxVal;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.LookAndFeel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.JSONUtilities;
import gov.epa.exp_data_gathering.parse.LiteratureSource;




public class ParseToxVal {

	static String version ="v93";
	
	void getAcuteAquaticExperimentalRecords(String commonName,double duration,String type,String criticalEffect,String propertyType) {

		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();

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
		System.out.println(toxValRecords.size());
		System.out.println(experimentalRecords.size());
		
		//TODO get source_url to store in literature_sources table


		String mainFolder = "Data" + File.separator + "Experimental" + File.separator + "ToxVal_"+version;
		
		String propertyName=experimentalRecords.get(0).property_name;
		String fileNameJsonExperimentalRecords = "ToxVal_"+version+" "+propertyName+" Experimental Records.json";
		JSONUtilities.batchAndWriteJSON(new Vector<ExperimentalRecord>(experimentalRecords),mainFolder+File.separator+fileNameJsonExperimentalRecords);
		
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
		p.getAcuteAquaticExperimentalRecords("Fathead minnow",ToxValQuery.FATHEAD_MINNOW_DURATION,ToxValQuery.TYPE,ToxValQuery.CRITICAL_EFFECT,ToxValQuery.propertyCategoryAcuteAquaticToxicity);
		//TODO look at UnitConverter.convertRecord- should we use convertToxicity or convertSolubility??
		//TODO get journal citation so can store in exp_prop
	}

}
