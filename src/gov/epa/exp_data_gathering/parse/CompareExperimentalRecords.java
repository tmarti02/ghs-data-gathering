package gov.epa.exp_data_gathering.parse;

import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JFrame;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import com.google.gson.Gson;

import gov.epa.QSAR.utilities.MatlabChart;
import gov.epa.api.DsstoxLookup;
import gov.epa.api.ExperimentalConstants;
import gov.epa.api.ScoreRecord;
import gov.epa.api.DsstoxLookup.DsstoxRecord;

/**
 * @author TMARTI02
 */
public class CompareExperimentalRecords {

	public static class Source {
		String sourceName;
		String subfolder;

		public Source(String sourceName,String subfolder) {
			this.sourceName=sourceName;
			this.subfolder=subfolder;
		}
	}

	public CompareMethods cm=new CompareMethods();
	public Comparisons c=new Comparisons();
	public ExperimentalRecordManipulator rm=new ExperimentalRecordManipulator();

	public class ExperimentalRecordManipulator {

		public ExperimentalRecords getAllExperimentalRecords(List<Source> sources) {
			ExperimentalRecords recsAll=new ExperimentalRecords();
			for(Source source:sources) {
				ExperimentalRecords recs=ExperimentalRecords.getExperimentalRecords(source.sourceName, source.subfolder);
				recsAll.addAll(recs);
			}
			return recsAll;
		}
		
		public ExperimentalRecords getAllExperimentalRecords(List<Source> sources,String propertyName) {
			ExperimentalRecords recsAll=new ExperimentalRecords();
			for(Source source:sources) {
				ExperimentalRecords recs=ExperimentalRecords.getExperimentalRecords(source.sourceName, source.subfolder);
				
				for (ExperimentalRecord er:recs) {
					if(!er.property_name.contentEquals(propertyName)) continue;
					recsAll.add(er);	
				}
			}
			return recsAll;
		}

		private TreeMap<String, ExperimentalRecords> getTreeMapByCAS(String propertyName, String units,
				ExperimentalRecords experimentalRecords) {
			TreeMap<String,ExperimentalRecords>ht=new TreeMap<>();
			for(ExperimentalRecord er:experimentalRecords) {
				if(er.casrn==null) continue;
				if(!er.property_name.contentEquals(propertyName)) continue;
				if(ht.containsKey(er.casrn)) {
					ExperimentalRecords recsCAS=ht.get(er.casrn);
					recsCAS.add(er);
				} else {
					ExperimentalRecords recsCAS=new ExperimentalRecords();
					recsCAS.add(er);
					ht.put(er.casrn, recsCAS);
				}
			}
			setMedianValues(ht,units);
			return ht;
		}

		private TreeMap<String, ExperimentalRecords> getTreeMapByDTXSID(String propertyName, String units,
				ExperimentalRecords experimentalRecords) {
			TreeMap<String,ExperimentalRecords>ht=new TreeMap<>();
			for(ExperimentalRecord er:experimentalRecords) {
				if(er.dsstox_substance_id==null) continue;
				if(!er.property_name.contentEquals(propertyName)) continue;
				if(ht.containsKey(er.dsstox_substance_id)) {
					ExperimentalRecords recsCAS=ht.get(er.dsstox_substance_id);
					recsCAS.add(er);
				} else {
					ExperimentalRecords recsCAS=new ExperimentalRecords();
					recsCAS.add(er);
					ht.put(er.dsstox_substance_id, recsCAS);
				}
			}
			setMedianValues(ht,units);
			return ht;
		}

		private HashSet<String> updateCountBySourceHashtable(Hashtable<String, Integer> htCountBySource, ExperimentalRecords recs) {


			HashSet<String>sources=new HashSet<>();

			for(ExperimentalRecord er:recs) {
				String sourceName=null;
				if(er.getOriginalSourceName()==null) sourceName="Unknown";
				else sourceName=er.getOriginalSourceName();
				sources.add(sourceName);
			}


			for (String sourceName:sources) {
				if(htCountBySource.containsKey(sourceName)) {
					int oldVal=htCountBySource.get(sourceName);
					htCountBySource.put(sourceName,oldVal+1);
				} else {
					htCountBySource.put(sourceName,1);
				}
			}

			return sources;

		}

		private TreeMap<String,ExperimentalRecords> getHashtable(String sourceName,String subfolder, String propertyName,String units) {


			ExperimentalRecords experimentalRecords = ExperimentalRecords.getExperimentalRecords(sourceName, subfolder);

			int totalCount=experimentalRecords.size();


			TreeMap<String, ExperimentalRecords> ht = rm.getTreeMapByCAS(propertyName, units, experimentalRecords);

			System.out.println(sourceName+"\t"+ht.size()+"\t"+totalCount);

			return ht;
		}

		private ExperimentalRecords getAllExperimentalRecords(List<Source> sources,boolean isbad) {
			ExperimentalRecords recsAll=new ExperimentalRecords();
			for(Source source:sources) {
				if(isbad) {
					ExperimentalRecords recs=ExperimentalRecords.getExperimentalRecordsBad(source.sourceName, source.subfolder);
					recsAll.addAll(recs);
				} else {
					ExperimentalRecords recs=ExperimentalRecords.getExperimentalRecords(source.sourceName, source.subfolder);
					recsAll.addAll(recs);
				}
			}
			return recsAll;
		}

		private void setMedianValue(ExperimentalRecords recs, List<Double> vals) {
		
			//		System.out.println(recs.get(0).casrn+"\t"+vals.size());
		
			if(vals.size()%2==0) {// even
		
				int middleVal2=vals.size()/2;
				int middleVal1=middleVal2-1;
				recs.medianValue=(vals.get(middleVal1)+vals.get(middleVal2))/2.0;
				
			} else {//odd
				int middleVal=vals.size()/2;
				recs.medianValue=vals.get(middleVal);
			}
		
			//		int counter=0;
			//		for (Double val:vals) {
			//			System.out.println(val+"\t"+counter++);
			//		}
			//		System.out.println(recs.get(0).casrn+"\t"+vals.size()+"\t"+recs.medianValue+"\n");
		
		
		}

		private void setBinaryScore(ExperimentalRecords recs, List<Double> vals) {
		
			if(vals.size()==0) return;
		
			double avg=0;
			for(Double val:vals) avg+=val;
			avg/=vals.size();
		
			if(avg<=0.2) recs.medianValue=0.0;
			else if (avg>=0.8) recs.medianValue=1.0;
			else return;
		
			//		System.out.println(recs.get(0).casrn+"\t"+avg);
		
		}

		private void setMedianValue(ExperimentalRecords recs,String units) {
		
			List<Double>vals=new ArrayList<>();
		
			for (ExperimentalRecord er:recs) {
		
				//			System.out.println(er.property_value_units_final+"\t"+units);
		
				if(er.property_value_units_final==null) continue;
				if(!er.property_value_units_final.equals(units)) continue;
		
				if(er.property_value_numeric_qualifier!=null) {
//					if (er.property_value_numeric_qualifier.contains("<") || er.property_value_numeric_qualifier.contains(">")) continue;
					if (!er.property_value_numeric_qualifier.equals("~")) continue;
				}
		
				Double val=null;
		
				if(er.property_value_max_final!=null && er.property_value_min_final!=null) {
					val=(er.property_value_max_final+er.property_value_min_final)/2.0;
				} else if(er.property_value_point_estimate_final!=null) {
					val=er.property_value_point_estimate_final;
				} else continue;
		
				if(!units.toLowerCase().contains("log")) {
					val=Math.log10(val);
				}
				
				vals.add(val);
		
				//			System.out.println(er.property_value_string+"\t"+val);
			}
		
			if (vals.size()>0) {
				Collections.sort(vals);
		
				if(units.equals(ExperimentalConstants.str_binary)) {
					setBinaryScore(recs,vals);
		
					if(recs.medianValue!=null) {
						//					System.out.println("\t"+recs.get(0).casrn+"\t"+recs.medianValue);
					}
		
				} else {
					setMedianValue(recs,vals);	
				}
		
			}
		}

		void setMedianValues(TreeMap<String,ExperimentalRecords> tm, String units) {
			int count=0;
		
			for (String key:tm.keySet()) {
				ExperimentalRecords recs=tm.get(key);
				setMedianValue(recs,units);
				count+=recs.size();
			}
		
		}

		private void removeByParameter(String parameterName, String parameterValue, ExperimentalRecords recs1) {
			for (int i=0;i<recs1.size();i++) {
				ExperimentalRecord rec=recs1.get(i);
				
				if(rec.experimental_parameters.get(parameterName)==null || 
						!rec.experimental_parameters.get(parameterName).equals(parameterValue)) {
					recs1.remove(i--);
				}
			}
		}

	}

	class Comparisons {

		private void compareREACH_Sources() {
			List<Source>sources1=new ArrayList<>();
			sources1.add(new Source("QSAR_Toolbox","Acute toxicity ECHA Reach"));

			List<Source>sources2=new ArrayList<>();
			sources2.add(new Source("eChemPortalAPI","AcuteToxicityOral"));

			cm.compare(sources1, sources2, ExperimentalConstants.strORAL_RAT_LD50,ExperimentalConstants.str_mg_kg,"cas");
		}

		private void compareSensitization() {
			List<Source>sources1=new ArrayList<>();
			List<Source>sources2=new ArrayList<>();

			//Compare 2 NIEHS data sources:
			//		sources1.add(new Source("NICEATM",null));
			//		sources2.add(new Source("NIEHS_ICE_2024_08","skin sensitization"));

			//				countWithMedian1=547
			//				countWithMedian2=385
			//				countIn1Not2=229
			//				countIn2Not1=67
			//				Count in common=318
			//				Concordance=1.00
			//				Conclusion: keep NICEATM version


			//		sources1.add(new Source("NICEATM",null));
			//		sources2.add(new Source("QSAR_Toolbox","Sensitization"));

			//				countWithMedian1=547
			//				countWithMedian2=530
			//				countIn1Not2=184
			//				countIn2Not1=167
			//				Count in common=363
			//				Concordance=0.99
			//				Conclusion: keep both


			sources1.add(new Source("NICEATM",null));
			sources2.add(new Source("eChemPortalAPI","SkinSensitisation"));

			//				countWithMedian1=547
			//				countWithMedian2=3984
			//				countIn1Not2=444
			//				countIn2Not1=3881
			//				Count in common=103
			//				Concordance=0.94
			//				Conclusion: keep both

			System.out.println("Source1="+sources1.get(0).sourceName);
			System.out.println("Source2="+sources2.get(0).sourceName+"\n");
			cm.compareChemicalsInCommonConcordance(sources1, sources2,ExperimentalConstants.strSkinSensitizationLLNA,ExperimentalConstants.str_binary);



		}

		private void compareToChemidplusToEcha() {
			List<Source>sources1=new ArrayList<>();
			sources1.add(new Source("ChemIDplus_2024_12_04",null));

			List<Source>sources2=new ArrayList<>();
			sources2.add(new Source("QSAR_Toolbox","Acute toxicity ECHA Reach"));
			//		sources2.add(new Source("eChemPortalAPI","AcuteToxicityOral"));

			cm.compare(sources1, sources2, ExperimentalConstants.strORAL_RAT_LD50,ExperimentalConstants.str_mg_kg,"cas");
		}

		void compareQSAR_Toolbox_sources() {
			List<Source>sources1=new ArrayList<>();
			sources1.add(new Source("QSAR_Toolbox","Acute toxicity ECHA Reach"));

			List<Source>sources2=new ArrayList<>();
			sources2.add(new Source("QSAR_Toolbox","Acute toxicity oral toxicity db"));

			cm.compare(sources1, sources2, ExperimentalConstants.strORAL_RAT_LD50,ExperimentalConstants.str_mg_kg,"cas");
		}

		private void compareToNIEHS_OralRatLD50() {
			List<Source>sources1=new ArrayList<>();
			sources1.add(new Source("ChemIDplus_2024_12_04",null));
			//		sources1.add(new Source("eChemPortalAPI","AcuteToxicityOral"));
			sources1.add(new Source("QSAR_Toolbox","Acute toxicity ECHA Reach"));

			List<Source>sources2=new ArrayList<>();
			sources2.add(new Source("NIEHS_ICE_2024_08","Acute oral"));

			cm.compare(sources1, sources2, ExperimentalConstants.strORAL_RAT_LD50,ExperimentalConstants.str_mg_kg,"cas");
		}

		void lookAtEchemportalLD50_Guidelines() {
			List<Source>sources1=new ArrayList<>();
			sources1.add(new Source("eChemportalAPI","AcuteToxicityOral"));


			ExperimentalRecords recs=rm.getAllExperimentalRecords(sources1);

			//		System.out.println(recs.size());

			ExperimentalRecords recs401=new ExperimentalRecords();
			ExperimentalRecords recsNot401=new ExperimentalRecords();

			for (ExperimentalRecord er:recs) {

				if(er.experimental_parameters.containsKey("Guidelines")) {
					String guidelines=er.experimental_parameters.get("Guidelines")+"";

					if(guidelines.contains("401")) recs401.add(er);
					else recsNot401.add(er);
				}

			}

			String propertyName=ExperimentalConstants.strORAL_RAT_LD50;
			String units=ExperimentalConstants.str_mg_kg;

			TreeMap<String, ExperimentalRecords> tm1 = rm.getTreeMapByCAS(propertyName, units, recs401);
			TreeMap<String, ExperimentalRecords> tm2 = rm.getTreeMapByCAS(propertyName, units, recsNot401);

			//		System.out.println(tm1.size());
			//		System.out.println(tm2.size());

			System.out.println("countWithMedian1="+cm.getCountWithMedian(tm1));
			System.out.println("countWithMedian2="+cm.getCountWithMedian(tm2));
			System.out.println("countIn1Not2="+cm.getNewChemicalCount(tm1, tm2,false));
			System.out.println("countIn2Not1="+cm.getNewChemicalCount(tm2, tm1,false));

			cm.compareChemicalsInCommon(tm1, tm2, units);

		}

		void lookAtEchemportalLD50_Guidelines2() {
			List<Source>sources1=new ArrayList<>();
			sources1.add(new Source("eChemportalAPI","AcuteToxicityOral"));


			ExperimentalRecords recs=rm.getAllExperimentalRecords(sources1);

			//		System.out.println(recs.size());

			ExperimentalRecords recs423=new ExperimentalRecords();
			ExperimentalRecords recs420=new ExperimentalRecords();

			for (ExperimentalRecord er:recs) {

				if(er.experimental_parameters.containsKey("Guidelines")) {
					String guidelines=er.experimental_parameters.get("Guidelines")+"";

					if(guidelines.contains("423")) recs423.add(er);
					else if(guidelines.contains("420")) recs420.add(er);

				}

			}

			String propertyName=ExperimentalConstants.strORAL_RAT_LD50;
			String units=ExperimentalConstants.str_mg_kg;

			TreeMap<String, ExperimentalRecords> tm1 = rm.getTreeMapByCAS(propertyName, units, recs423);
			TreeMap<String, ExperimentalRecords> tm2 = rm.getTreeMapByCAS(propertyName, units, recs420);


			System.out.println("countWithMedian1="+cm.getCountWithMedian(tm1));
			System.out.println("countWithMedian2="+cm.getCountWithMedian(tm2));
			System.out.println("countIn1Not2="+cm.getNewChemicalCount(tm1, tm2,false));
			System.out.println("countIn2Not1="+cm.getNewChemicalCount(tm2, tm1,false));


			cm.compareChemicalsInCommon(tm1, tm2, units);

			System.out.println(tm1.size());
			System.out.println(tm2.size());
		}

		void lookAtEchemportalLD50_Guidelines3() {
			List<Source>sources1=new ArrayList<>();
			sources1.add(new Source("eChemportalAPI","AcuteToxicityOral"));

			ExperimentalRecords recs=rm.getAllExperimentalRecords(sources1);
			//		System.out.println(recs.size());

			ExperimentalRecords recs423=new ExperimentalRecords();
			ExperimentalRecords recs425=new ExperimentalRecords();

			for (ExperimentalRecord er:recs) {
				if(er.experimental_parameters.containsKey("Guidelines")) {
					String guidelines=er.experimental_parameters.get("Guidelines")+"";

					if(guidelines.contains("423")) recs423.add(er);
					else if(guidelines.contains("425")) recs425.add(er);

				}

			}

			String propertyName=ExperimentalConstants.strORAL_RAT_LD50;
			String units=ExperimentalConstants.str_mg_kg;

			TreeMap<String, ExperimentalRecords> tm1 = rm.getTreeMapByCAS(propertyName, units, recs423);
			TreeMap<String, ExperimentalRecords> tm2 = rm.getTreeMapByCAS(propertyName, units, recs425);


			System.out.println("countWithMedian1="+cm.getCountWithMedian(tm1));
			System.out.println("countWithMedian2="+cm.getCountWithMedian(tm2));
			System.out.println("countIn1Not2="+cm.getNewChemicalCount(tm1, tm2,false));
			System.out.println("countIn2Not1="+cm.getNewChemicalCount(tm2, tm1,false));


			cm.compareChemicalsInCommon(tm1, tm2, units);

			System.out.println(tm1.size());
			System.out.println(tm2.size());
		}

		void lookAtLLNA_MixtureVsNonMixtureNIEHS_ICE() {
			List<Source>sources1=new ArrayList<>();
			sources1.add(new Source("NIEHS_ICE_2024_08","skin sensitization"));

			ExperimentalRecords recs=rm.getAllExperimentalRecords(sources1);
			ExperimentalRecords recsBad=rm.getAllExperimentalRecords(sources1,true);

			//		System.out.println(recs.size());
			//		System.out.println(recsBad.size());

			//		if(true)return;

			String propertyName=ExperimentalConstants.strSkinSensitizationLLNA;
			String units=ExperimentalConstants.str_binary;

			TreeMap<String, ExperimentalRecords> tm1 = rm.getTreeMapByCAS(propertyName, units, recs);
			System.out.println("good\t"+recs.size()+"\t"+tm1.size());


			TreeMap<String, ExperimentalRecords> tm2 = rm.getTreeMapByCAS(propertyName, units, recsBad);
			System.out.println("bad\t"+recsBad.size()+"\t"+tm2.size());

			//		System.out.println(tm1.size());
			//		System.out.println(tm2.size());

			System.out.println("countWithMedian1="+cm.getCountWithMedian(tm1));
			System.out.println("countWithMedian2="+cm.getCountWithMedian(tm2));
			System.out.println("countIn1Not2="+cm.getNewChemicalCount(tm1, tm2,false));
			System.out.println("countIn2Not1="+cm.getNewChemicalCount(tm2, tm1,false));

			cm.compareConcordance(tm1, tm2);

		}

		
		/**
		 * TEST Sources
		 * 
		 * "73. Dimitrov, S., et al., Base-line model for identifying the bioaccumulation potential of chemicals. SAR and QSAR in Environmental Research, 2005. 16: p. 531-554
			76. Zhao, C.B., E.; Chana, A.; Roncaglioni, A.; Benfenati, E., A new hybrid system of QSAR models for predicting bioconcentration factors (BCF). Chemosphere, 2008. 73: p. 1701-1707."

				Zhao uses Dimitrov which uses Japan data.
				Dimitrov uses  Japan NITE/MITI data. The data in "000064113.xlsx" matches fairly well but chemical count is different.
				Metadata is limited unless can track down data in one of Japan's databases
			
			74. Arnot, J.A. and F.A.P.C. Gobas, A review of bioconcentration factor (BCF) and bioaccumulation factor (BAF) assessments for organic chemicals in aquatic organisms. Environ. Rev., 2006. 14: p. 257-297.
			
				ToxVal bcfbaf table has same number of records as Arnot's spreadsheet!
				Richard Judson confirmed this.
				
			75. EURAS. EURAS bioconcentration factor (BCF) Gold Standard Database. 3/30/18]; Available from: http://ambit.sourceforge.net/euras/.
			
				No longer available?
				
			Conclusion: use ToxVal and ECOTOX

		 */
		private void compareBCF() {

			printChemicalsInCommon=false;
			
			List<Source>sources1=new ArrayList<>();
			List<Source>sources2=new ArrayList<>();

			String propertyName=ExperimentalConstants.strBCF;
//			String propertyName=ExperimentalConstants.strFishBCF;
//			String propertyName=ExperimentalConstants.strFishBCFWholeBody;

//			sources1.add(new Source("Burkhard",propertyName));
//			sources1.add(new Source("ECOTOX_2023_12_14",propertyName));
//			sources2.add(new Source("ToxVal_prod",propertyName));
//			sources2.add(new Source("Arnot 2006",null));
//			sources2.add(new Source("Arnot 2006",propertyName));
			
//			sources2.add(new Source("ECOTOX_2023_12_14",propertyName));

			sources1.add(new Source("Arnot 2006",propertyName));
//			sources1.add(new Source("QSAR_Toolbox","BCF NITE//"+propertyName));//banding issue, only 37 new chemicals 

//			sources2.add(new Source("Arnot 2006",propertyName));
//			sources2.add(new Source("ECOTOX_2024_12_12",propertyName));
//			sources2.add(new Source("QSAR_Toolbox","BCF NITE//"+propertyName));//banding issue, only 37 new chemicals 
//			sources2.add(new Source("Burkhard",propertyName));
			sources2.add(new Source("OPERA2.8",null));

//			sources2.add(new Source("QSAR_Toolbox","BCF CEFIC//"+propertyName));//banding issue, only 37 new chemicals

			String units="L/kg";
//			cm.compare(sources1, sources2, propertyName, units,"cas");
			cm.compare(sources1, sources2, propertyName, units,"sid");
//			cm.compare(sources1, sources2, propertyName, units,"cas","Species supercategory","Fish");
			
			
			//We get more records if we use both even though they overlap a bit
			

		}

		void compareOralRat() {

			c.compareToNIEHS_OralRatLD50();
			//		c.compareToChemidplusToEcha();
			//		c.compare("ChemIDplus_2024_12_04", "ChemIDplus", ExperimentalConstants.strORAL_RAT_LD50,ExperimentalConstants.str_mg_kg);

			//		c.compareREACH_Sources();
			//		c.compareQSAR_Toolbox_sources();

			//		c.lookAtEchemportalLD50_Guidelines();
			//		c.lookAtEchemportalLD50_Guidelines2();
			//		c.lookAtEchemportalLD50_Guidelines3();

		}


	}

	boolean printChemicalsInCommon=true;

	
	public class CompareMethods {

		void compare(List<Source>sources1, List<Source>sources2, String propertyName,String units,String idType) {

			ExperimentalRecords recs1=rm.getAllExperimentalRecords(sources1,propertyName);
			ExperimentalRecords recs2=rm.getAllExperimentalRecords(sources2,propertyName);


			if(idType.equals("sid")) {
				recs1.addDtxsids();
				recs2.addDtxsids();
			}
			
			TreeMap<String, ExperimentalRecords> tm1=null;
			TreeMap<String, ExperimentalRecords> tm2=null;

			if(idType.equals("cas")) {
				tm1 = rm.getTreeMapByCAS(propertyName, units, recs1);
				tm2 = rm.getTreeMapByCAS(propertyName, units, recs2);
			} else if(idType.equals("sid")) {
				tm1 = rm.getTreeMapByDTXSID(propertyName, units, recs1);
				tm2 = rm.getTreeMapByDTXSID(propertyName, units, recs2);
			}

			System.out.println("sources1:"+ParseUtilities.gson.toJson(sources1));
			System.out.println("sources2:"+ParseUtilities.gson.toJson(sources2));

			System.out.println("countWithMedian1="+getCountWithMedian(tm1));
			System.out.println("countWithMedian2="+getCountWithMedian(tm2));
			System.out.println("countIn1Not2="+getNewChemicalCount(tm1, tm2,false));
			System.out.println("countIn2Not1="+getNewChemicalCount(tm2, tm1,false));
			System.out.println("countInEither="+getCountInEither(tm2, tm1,false));
			
			
			
			compareChemicalsInCommon(tm1, tm2, units);

		}
		
		void compare(List<Source>sources1, List<Source>sources2, String propertyName,String units,String idType,String parameterName,String parameterValue) {

			ExperimentalRecords recs1=rm.getAllExperimentalRecords(sources1);
			ExperimentalRecords recs2=rm.getAllExperimentalRecords(sources2);
			
			rm.removeByParameter(parameterName, parameterValue, recs1);
			rm.removeByParameter(parameterName, parameterValue, recs2);


			TreeMap<String, ExperimentalRecords> tm1=null;
			TreeMap<String, ExperimentalRecords> tm2=null;

			if(idType.equals("cas")) {
				tm1 = rm.getTreeMapByCAS(propertyName, units, recs1);
				tm2 = rm.getTreeMapByCAS(propertyName, units, recs2);
			} else if(idType.equals("sid")) {
				tm1 = rm.getTreeMapByDTXSID(propertyName, units, recs1);
				tm2 = rm.getTreeMapByDTXSID(propertyName, units, recs2);
			}

			System.out.println("countWithMedian1="+getCountWithMedian(tm1));
			System.out.println("countWithMedian2="+getCountWithMedian(tm2));
			System.out.println("countIn1Not2="+getNewChemicalCount(tm1, tm2,false));
			System.out.println("countIn2Not1="+getNewChemicalCount(tm2, tm1,false));
			System.out.println("countInEither="+getCountInEither(tm2, tm1,false));

			compareChemicalsInCommon(tm1, tm2, units);

		}

		void compare(String sourceName1,String sourceName2,String propertyName,String units) {
			TreeMap<String,ExperimentalRecords> tm1 = rm.getHashtable(sourceName1,null, propertyName,units);
			TreeMap<String,ExperimentalRecords> tm2 = rm.getHashtable(sourceName2,null, propertyName,units);

			System.out.println("countWithMedian1="+getCountWithMedian(tm1));
			System.out.println("countWithMedian2="+getCountWithMedian(tm2));
			System.out.println("countIn1Not2="+getNewChemicalCount(tm1, tm2,false));
			System.out.println("countIn2Not1="+getNewChemicalCount(tm2, tm1,true));
			compareChemicalsInCommon(tm1, tm2, units);

		}

		void compare(String sourceName1,String sourceName2,String subfolder1,String subfolder2, String propertyName,String units) {
			TreeMap<String,ExperimentalRecords> tm1 = rm.getHashtable(sourceName1,subfolder1,propertyName,units);
			TreeMap<String,ExperimentalRecords> tm2 = rm.getHashtable(sourceName2,subfolder2, propertyName,units);

			System.out.println("countWithMedian1="+getCountWithMedian(tm1));
			System.out.println("countWithMedian2="+getCountWithMedian(tm2));
			System.out.println("countIn1Not2="+getNewChemicalCount(tm1, tm2,false));
			System.out.println("countIn2Not1="+getNewChemicalCount(tm2, tm1,false));
			compareChemicalsInCommon(tm1, tm2,units);

		}

		double compareChemicalsInCommon(TreeMap<String,ExperimentalRecords>tm1,TreeMap<String,ExperimentalRecords>tm2, String units) {

			if(!units.toLowerCase().contains("log")) {
				System.out.println("Need to handle units="+units);
			}

			int countInCommon=0;
			double MAE=0;

			DecimalFormat df=new DecimalFormat("0.00");

//			boolean printValues=false;

			if(printChemicalsInCommon) System.out.println("\nLogType\tkey\tLog10median_1\tLog10median_2\tdiff");

			List<Double>vals1=new ArrayList<>();
			List<Double>vals2=new ArrayList<>();


			for (String key:tm1.keySet()) {
				ExperimentalRecords recs1=tm1.get(key);

				//			System.out.println(key);

				if(!tm2.containsKey(key))continue;

				ExperimentalRecords recs2=tm2.get(key);

				if(recs1.medianValue!=null && recs2.medianValue!=null) {
					
					Double error=Math.abs(recs1.medianValue-recs2.medianValue);
					vals1.add(recs1.medianValue);
					vals2.add(recs2.medianValue);
					if(printChemicalsInCommon) {
						System.out.println("took log\t"+key+"\t"+df.format(recs1.medianValue)+"\t"+df.format(recs2.medianValue)+"\t"+df.format(error));					
					}

					
					//				System.out.println(casrn+"\t"+recs1.medianValue+"\t"+recs2.medianValue);	
//					Double error=null;
//					if(units.toLowerCase().contains("log")) {
//						error=Math.abs(recs1.medianValue-recs2.medianValue);
//						if(printValues) {
//
//							System.out.println("already log\t"+key+"\t"+df.format(recs1.medianValue)+"\t"+df.format(recs2.medianValue)+"\t"+df.format(error));					
//						}
//					} else {
//						error=Math.abs(recs1.medianValue-recs2.medianValue);
//						vals1.add(recs1.medianValue);
//						vals2.add(recs2.medianValue);
//						if(printValues) {
//							System.out.println("took log\t"+key+"\t"+df.format(recs1.medianValue)+"\t"+df.format(recs2.medianValue)+"\t"+df.format(error));					
//						}
//					}
					
					//				if(error>0) {
					//					System.out.println(casrn+"\t"+df.format(Math.log10(recs1.medianValue))+"\t"+df.format(Math.log10(recs2.medianValue))+"\t"+df.format(error));
					//				}

					MAE+=error;
					countInCommon++;

				} 
			}
			
			
			if(!units.contains("log"))units="log10("+units+")";
			

			createPlot(units, vals1, vals2);

			MAE/=countInCommon;
			System.out.println("Count in common="+countInCommon);
			System.out.println("MAE="+MAE);
			return MAE;

		}

		void compareChemicalsInCommonConcordance(List<Source>sources1, List<Source>sources2, String propertyName,String units) {

			ExperimentalRecords recs1=rm.getAllExperimentalRecords(sources1);
			ExperimentalRecords recs2=rm.getAllExperimentalRecords(sources2);

			TreeMap<String, ExperimentalRecords> tm1 = rm.getTreeMapByCAS(propertyName, units, recs1);
			TreeMap<String, ExperimentalRecords> tm2 = rm.getTreeMapByCAS(propertyName, units, recs2);

			System.out.println("countWithMedian1="+getCountWithMedian(tm1));
			System.out.println("countWithMedian2="+getCountWithMedian(tm2));
			System.out.println("countIn1Not2="+getNewChemicalCount(tm1, tm2,false));
			System.out.println("countIn2Not1="+getNewChemicalCount(tm2, tm1,false));

			compareConcordance(tm1, tm2);

		}

		private double compareConcordance(TreeMap<String, ExperimentalRecords> tm1,
				TreeMap<String, ExperimentalRecords> tm2) {
			int countInCommon=0;
			double Concordance=0;

			DecimalFormat df=new DecimalFormat("0.00");

			//		boolean printValues=true;
			boolean printValues=false;

			if(printValues) System.out.println("casrn\tBinary_1\tBinary_2");

			for (String casrn:tm1.keySet()) {
				ExperimentalRecords recs1=tm1.get(casrn);

				if(!tm2.containsKey(casrn))continue;

				ExperimentalRecords recs2=tm2.get(casrn);

				if(recs1.medianValue!=null && recs2.medianValue!=null) {
					//				System.out.println(casrn+"\t"+recs1.medianValue+"\t"+recs2.medianValue);	

					double diff=Math.abs(recs1.medianValue-recs2.medianValue);

					if(diff<0.0001) {
						Concordance++;
					}

					countInCommon++;

					if(printValues) {
						System.out.println(casrn+"\t"+recs1.medianValue+"\t"+recs2.medianValue);					
					}
				} 
			}

			Concordance/=countInCommon;

			System.out.println("Count in common="+countInCommon);
			System.out.println("Concordance="+df.format(Concordance));

			return Concordance;
		}

		int getCountInEither(TreeMap<String,ExperimentalRecords>tm1,TreeMap<String,ExperimentalRecords>tm2,boolean printValues) {

			HashSet<String>keys=new HashSet<>();

			for (String key:tm1.keySet()) {
				ExperimentalRecords recs=tm1.get(key);
				if(recs.medianValue!=null)
					keys.add(key);
			}
			for (String key:tm2.keySet()) {
				ExperimentalRecords recs=tm2.get(key);
				if(recs.medianValue!=null)
					keys.add(key);
			}

			return keys.size();

		}

		int getCountWithMedian(TreeMap<String,ExperimentalRecords>tm) {

			int countWithMedian=0;
			for (String key:tm.keySet()) {
				ExperimentalRecords recs1=tm.get(key);

				if(recs1.medianValue!=null) countWithMedian++;
			}
			return countWithMedian;

		}

		/**
		 * Get counts of chemicals special to the first source
		 * 
		 * @param tm1
		 * @param tm2
		 * @return
		 */
		int getNewChemicalCount(TreeMap<String,ExperimentalRecords>tm1,TreeMap<String,ExperimentalRecords>tm2,boolean printValues) {

			int countIn1Not2=0;

			Hashtable<String,Integer>htCountBySource=new Hashtable<>();

			for (String casrn:tm1.keySet()) {
				ExperimentalRecords recs1=tm1.get(casrn);

				if(!tm2.containsKey(casrn) && recs1.medianValue!=null) {
					countIn1Not2++;

					HashSet<String>sources=rm.updateCountBySourceHashtable(htCountBySource, recs1);

					if(printValues) System.out.println(casrn+"\t"+(recs1.medianValue)+"\t"+sources);

					//				if(printValues && sources.contains("Unknown")) {
					//					System.out.println(casrn+"\tHas Unknown"+"\t"+recs1.medianValue);
					//				}

					continue;
				}

				ExperimentalRecords recs2=tm2.get(casrn);

				if(recs1.medianValue!=null && recs2.medianValue==null) {
					//				if(printValues) System.out.println(casrn+"\t"+(recs1.medianValue));
					countIn1Not2++;
					HashSet<String>sources=rm.updateCountBySourceHashtable(htCountBySource, recs1);

					if(printValues) System.out.println(casrn+"\t"+(recs1.medianValue)+"\t"+sources);

					//				if(printValues && sources.contains("Unknown")) {
					//					System.out.println(casrn+"\tHas Unknown"+"\t"+recs1.medianValue);
					//				}

				}
			}

			if(printValues) {
				System.out.println("\nCounts by original source that arent in other set");
				for(String source:htCountBySource.keySet()) {
					System.out.println(source+"\t"+htCountBySource.get(source));
				}
				System.out.println("");
			}

			return countIn1Not2;

		}

		public void createPlot(String units, List<Double> vals1, List<Double> vals2) {
			double[]x = makeArray(vals1);
			double[]y = makeArray(vals2);
		
			MatlabChart fig = new MatlabChart(); // figure('Position',[100 100 640 480]);
			fig.plot(x, y, "-r", 2.0f, "data"); // plot(x,y1,'-r','LineWidth',2);
			fig.plot(y, y, "-b", 2.0f, "Y=X"); // plot(x,y1,'-r','LineWidth',2);

			//        fig.plot(x, y2, ":k", 3.0f, "BAC");  // plot(x,y2,':k','LineWidth',3);
		
			fig.RenderPlot();                    // First render plot before modifying
			fig.title("Source1 vs. Source 2");    // title('Stock 1 vs. Stock 2');
			//      fig.xlim(10, 100);                   // xlim([10 100]);
			//      fig.ylim(200, 300);                  // ylim([200 300]);
		
		
			//TODO for some properties it wont be logged units in labels
		
			fig.xlabel("exp source 1 "+units);                  // xlabel('Days');
			fig.ylabel("exp source 2 "+units);                 // ylabel('Price');
			fig.grid("on","on");                 // grid on;
			fig.legend("southeast");             // legend('AAPL','BAC','Location','northeast')
			fig.font("Helvetica",15);            // .. 'FontName','Helvetica','FontSize',15
			//      fig.saveas("MyPlot.jpeg",640,480);   // saveas(gcf,'MyPlot','jpeg');
		
			XYLineAndShapeRenderer xy=(XYLineAndShapeRenderer) fig.chart.getXYPlot().getRenderer();

			xy.setSeriesShapesVisible(0, true);
			xy.setSeriesLinesVisible(0, false);

			xy.setSeriesShapesVisible(1, false);
			xy.setSeriesLinesVisible(1, true);

			
		
		
			ChartPanel cp=new ChartPanel(fig.chart);
		
		
			JFrame jframe=new JFrame();
			jframe.add(cp);
			cp.setLayout(new FlowLayout(FlowLayout.LEFT));
		
			jframe.setSize(500,500);
			jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jframe.setLocationRelativeTo(null);
			jframe.setVisible(true);
		}
		
		public void createPlot(String units, List<Double> vals1, List<Double> vals2,String source1,String source2) {
			double[]x = makeArray(vals1);
			double[]y = makeArray(vals2);
		
			MatlabChart fig = new MatlabChart(); // figure('Position',[100 100 640 480]);
			fig.plot(x, y, "-r", 2.0f, "data"); // plot(x,y1,'-r','LineWidth',2);
			fig.plot(y, y, "-b", 2.0f, "Y=X"); // plot(x,y1,'-r','LineWidth',2);

			//        fig.plot(x, y2, ":k", 3.0f, "BAC");  // plot(x,y2,':k','LineWidth',3);
		
			fig.RenderPlot();                    // First render plot before modifying
			fig.title(source1+" vs. "+source2);    // title('Stock 1 vs. Stock 2');
			//      fig.xlim(10, 100);                   // xlim([10 100]);
			//      fig.ylim(200, 300);                  // ylim([200 300]);
		
		
			//TODO for some properties it wont be logged units in labels
		
			fig.xlabel("exp "+source1+" "+units);                  // xlabel('Days');
			fig.ylabel("exp "+source2+" "+units);                 // ylabel('Price');
			fig.grid("on","on");                 // grid on;
			fig.legend("southeast");             // legend('AAPL','BAC','Location','northeast')
			fig.font("Helvetica",15);            // .. 'FontName','Helvetica','FontSize',15
			//      fig.saveas("MyPlot.jpeg",640,480);   // saveas(gcf,'MyPlot','jpeg');
		
			XYLineAndShapeRenderer xy=(XYLineAndShapeRenderer) fig.chart.getXYPlot().getRenderer();

			xy.setSeriesShapesVisible(0, true);
			xy.setSeriesLinesVisible(0, false);

			xy.setSeriesShapesVisible(1, false);
			xy.setSeriesLinesVisible(1, true);

			ChartPanel cp=new ChartPanel(fig.chart);
			JFrame jframe=new JFrame();
			jframe.add(cp);
			cp.setLayout(new FlowLayout(FlowLayout.LEFT));
		
			jframe.setSize(500,500);
			jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jframe.setLocationRelativeTo(null);
			jframe.setVisible(true);
		}

		private double[] makeArray(List<Double> vals1) {
			double[]x=new double[vals1.size()];
			int i=0;
			for (Double val:vals1) {
				x[i++]=val;
			}
			return x;
			//		Double[] array = new Double[vals1.size()];
			//		return vals1.toArray(array); 
		
		}



	}



	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CompareExperimentalRecords c=new CompareExperimentalRecords();

//		c.c.compareOralRat();

//		c.c.lookAtLLNA_MixtureVsNonMixtureNIEHS_ICE();//only 8?
//		c.c.compareSensitization();

		c.c.compareBCF();


	}






}

