package gov.epa.exp_data_gathering.parse;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;

import com.google.gson.Gson;

import gov.epa.api.ExperimentalConstants;

/**
* @author TMARTI02
*/
public class CompareExperimentalRecords {

	static class Source {
		String sourceName;
		String subfolder;
		
		Source(String sourceName,String subfolder) {
			this.sourceName=sourceName;
			this.subfolder=subfolder;
		}
	}
	
	
	
	void compare(String sourceName1,String sourceName2,String propertyName,String units) {
		TreeMap<String,ExperimentalRecords> tm1 = getHashtable(sourceName1,null, propertyName,units);
		TreeMap<String,ExperimentalRecords> tm2 = getHashtable(sourceName2,null, propertyName,units);
				
		System.out.println("countWithMedian1="+getCountWithMedian(tm1));
		System.out.println("countWithMedian2="+getCountWithMedian(tm2));
		System.out.println("countIn1Not2="+getNewChemicalCount(tm1, tm2,false));
		System.out.println("countIn2Not1="+getNewChemicalCount(tm2, tm1,true));
		compareChemicalsInCommon(tm1, tm2, units);
		
	}
	
	void compare(String sourceName1,String sourceName2,String subfolder1,String subfolder2, String propertyName,String units) {
		TreeMap<String,ExperimentalRecords> tm1 = getHashtable(sourceName1,subfolder1,propertyName,units);
		TreeMap<String,ExperimentalRecords> tm2 = getHashtable(sourceName2,subfolder2, propertyName,units);
				
		System.out.println("countWithMedian1="+getCountWithMedian(tm1));
		System.out.println("countWithMedian2="+getCountWithMedian(tm2));
		System.out.println("countIn1Not2="+getNewChemicalCount(tm1, tm2,false));
		System.out.println("countIn2Not1="+getNewChemicalCount(tm2, tm1,false));
		compareChemicalsInCommon(tm1, tm2,units);
		
	}
	
	
	
	void compare(List<Source>sources1, List<Source>sources2, String propertyName,String units) {
		
		ExperimentalRecords recs1=getAllExperimentalRecords(sources1);
		ExperimentalRecords recs2=getAllExperimentalRecords(sources2);
		
		TreeMap<String, ExperimentalRecords> tm1 = getTreeMapByCAS(propertyName, units, recs1);
		TreeMap<String, ExperimentalRecords> tm2 = getTreeMapByCAS(propertyName, units, recs2);

				
		System.out.println("countWithMedian1="+getCountWithMedian(tm1));
		System.out.println("countWithMedian2="+getCountWithMedian(tm2));
		System.out.println("countIn1Not2="+getNewChemicalCount(tm1, tm2,false));
		System.out.println("countIn2Not1="+getNewChemicalCount(tm2, tm1,false));
		compareChemicalsInCommon(tm1, tm2, units);
		
	}

	private ExperimentalRecords getAllExperimentalRecords(List<Source> sources) {
		ExperimentalRecords recsAll=new ExperimentalRecords();
		for(Source source:sources) {
			ExperimentalRecords recs=getExperimentalRecords(source.sourceName, source.subfolder);
			recsAll.addAll(recs);
		}
		return recsAll;
	}
	private ExperimentalRecords getAllExperimentalRecords(List<Source> sources,boolean isbad) {
		ExperimentalRecords recsAll=new ExperimentalRecords();
		for(Source source:sources) {
			if(isbad) {
				ExperimentalRecords recs=getExperimentalRecordsBad(source.sourceName, source.subfolder);
				recsAll.addAll(recs);
			} else {
				ExperimentalRecords recs=getExperimentalRecords(source.sourceName, source.subfolder);
				recsAll.addAll(recs);
			}
		}
		return recsAll;
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
				
				HashSet<String>sources=updateCountBySourceHashtable(htCountBySource, recs1);
				
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
				HashSet<String>sources=updateCountBySourceHashtable(htCountBySource, recs1);
				
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
	

	int getCountInEither(TreeMap<String,ExperimentalRecords>tm1,TreeMap<String,ExperimentalRecords>tm2,boolean printValues) {
		
		HashSet<String>keys=new HashSet<>();
		
		Hashtable<String,Integer>htCountBySource=new Hashtable<>();
		
		for (String key:tm1.keySet()) keys.add(key);
		for (String key:tm2.keySet()) keys.add(key);
		
		return keys.size();
		
	}

	private HashSet<String> updateCountBySourceHashtable(Hashtable<String, Integer> htCountBySource, ExperimentalRecords recs) {
		
		
		HashSet<String>sources=new HashSet<>();
		
		for(ExperimentalRecord er:recs) {
			String sourceName=null;
			if(getOriginalSourceName(er)==null) sourceName="Unknown";
			else sourceName=getOriginalSourceName(er);
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

	private String getOriginalSourceName(ExperimentalRecord er) {
		String originalSource;
		if(er.publicSourceOriginal!=null) {
			originalSource=er.publicSourceOriginal.name;
		} else {
			originalSource=er.original_source_name;
		}
		return originalSource;
	}
	
	

	double compareChemicalsInCommon(TreeMap<String,ExperimentalRecords>tm1,TreeMap<String,ExperimentalRecords>tm2, String units) {

		if(!units.toLowerCase().contains("log")) {
			System.out.println("Need to handle units");
		}
		
		int countInCommon=0;
		double MAE=0;

		DecimalFormat df=new DecimalFormat("0.00");

		boolean printValues=true;
//		boolean printValues=false;
		
		if(printValues) System.out.println("\nkey\tLog10median_1\tLog10median_2\tdiff");

		for (String key:tm1.keySet()) {
			ExperimentalRecords recs1=tm1.get(key);
			
//			System.out.println(key);
			
			if(!tm2.containsKey(key))continue;
			
			ExperimentalRecords recs2=tm2.get(key);
			
			if(recs1.medianValue!=null && recs2.medianValue!=null) {
//				System.out.println(casrn+"\t"+recs1.medianValue+"\t"+recs2.medianValue);	
				
				Double error=null;
				
				if(units.toLowerCase().contains("log")) {
					error=Math.abs(recs1.medianValue-recs2.medianValue);
					if(printValues) {
						
						System.out.println("already log\t"+key+"\t"+df.format(recs1.medianValue)+"\t"+df.format(recs2.medianValue)+"\t"+df.format(error));					
					}
				} else {
					error=Math.abs(Math.log10(recs1.medianValue)-Math.log10(recs2.medianValue));
					if(printValues) {
						System.out.println("took log\t"+key+"\t"+df.format(Math.log10(recs1.medianValue))+"\t"+df.format(Math.log10(recs2.medianValue))+"\t"+df.format(error));					
					}
				}
				
				
//				if(error>0) {
//					System.out.println(casrn+"\t"+df.format(Math.log10(recs1.medianValue))+"\t"+df.format(Math.log10(recs2.medianValue))+"\t"+df.format(error));
//				}
				
				
				
				MAE+=error;
//				if(printValues)
				
				

				countInCommon++;
				
			} 
		}
		
		MAE/=countInCommon;
		
		System.out.println("Count in common="+countInCommon);
		System.out.println("MAE="+MAE);

		return MAE;
		
	}
	
	
	void compareChemicalsInCommonConcordance(List<Source>sources1, List<Source>sources2, String propertyName,String units) {

		ExperimentalRecords recs1=getAllExperimentalRecords(sources1);
		ExperimentalRecords recs2=getAllExperimentalRecords(sources2);
		
		TreeMap<String, ExperimentalRecords> tm1 = getTreeMapByCAS(propertyName, units, recs1);
		TreeMap<String, ExperimentalRecords> tm2 = getTreeMapByCAS(propertyName, units, recs2);
		
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
	int getCountWithMedian(TreeMap<String,ExperimentalRecords>tm) {
		
		int countWithMedian=0;
		for (String casrn:tm.keySet()) {
			ExperimentalRecords recs1=tm.get(casrn);
			
			if(recs1.medianValue!=null) countWithMedian++;
		}
		return countWithMedian;

	}
	
	void setMedianValues(TreeMap<String,ExperimentalRecords> tm, String units) {
		int count=0;
		
		for (String casrn:tm.keySet()) {
			ExperimentalRecords recs=tm.get(casrn);
			setMedianValue(recs,units);
			count+=recs.size();
		}

	}
	
	
	

	private void setMedianValue(ExperimentalRecords recs,String units) {

		List<Double>vals=new ArrayList<>();
		
		for (ExperimentalRecord er:recs) {
			
//			System.out.println(er.property_value_units_final+"\t"+units);
			
			if(er.property_value_units_final==null) continue;
			if(!er.property_value_units_final.equals(units)) continue;

			if(er.property_value_numeric_qualifier!=null) {
				if (er.property_value_numeric_qualifier.contains("<") || er.property_value_numeric_qualifier.contains(">")) continue;
			}

			Double val=null;
				
			if(er.property_value_max_final!=null && er.property_value_min_final!=null) {
				val=(er.property_value_max_final+er.property_value_min_final)/2.0;
			} else if(er.property_value_point_estimate_final!=null) {
				val=er.property_value_point_estimate_final;
			} else continue;
			
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

	private TreeMap<String,ExperimentalRecords> getHashtable(String sourceName,String subfolder, String propertyName,String units) {
		
		
		ExperimentalRecords experimentalRecords = getExperimentalRecords(sourceName, subfolder);

		int totalCount=experimentalRecords.size();

		
		TreeMap<String, ExperimentalRecords> ht = getTreeMapByCAS(propertyName, units, experimentalRecords);
		
		System.out.println(sourceName+"\t"+ht.size()+"\t"+totalCount);
		
		return ht;
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


	private ExperimentalRecords getExperimentalRecords(String sourceName, String subfolder) {
		String folder="data\\experimental\\"+sourceName+"\\";
		if(subfolder!=null) folder+=subfolder+"\\";
		String filepath1=folder+sourceName+" Experimental Records.json";
		
		ExperimentalRecords experimentalRecords=ExperimentalRecords.loadFromJSON(filepath1);
		
		System.out.println(filepath1+"\t"+experimentalRecords.size());
		
		return experimentalRecords;
	}
	
	private ExperimentalRecords getExperimentalRecordsBad(String sourceName, String subfolder) {
		String folder="data\\experimental\\"+sourceName+"\\";
		if(subfolder!=null) folder+=subfolder+"\\";
		String filepath1=folder+sourceName+" Experimental Records-Bad.json";
		ExperimentalRecords experimentalRecords=ExperimentalRecords.loadFromJSON(filepath1);
		return experimentalRecords;
	}
	
	
	void compareQSAR_Toolbox_eChemportalAPI() {
		List<Source>sources1=new ArrayList<>();
		sources1.add(new Source("QSAR_Toolbox","Acute toxicity ECHA Reach"));
				
		List<Source>sources2=new ArrayList<>();
		sources2.add(new Source("eChemPortalAPI","AcuteToxicityOral"));
		
		compare(sources1, sources2, ExperimentalConstants.strORAL_RAT_LD50,ExperimentalConstants.str_mg_kg);
	}
	
	
	
	void lookAtEchemportalLD50_Guidelines() {
		List<Source>sources1=new ArrayList<>();
		sources1.add(new Source("eChemportalAPI","AcuteToxicityOral"));

		
		ExperimentalRecords recs=getAllExperimentalRecords(sources1);
		
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
		
		TreeMap<String, ExperimentalRecords> tm1 = getTreeMapByCAS(propertyName, units, recs401);
		TreeMap<String, ExperimentalRecords> tm2 = getTreeMapByCAS(propertyName, units, recsNot401);

//		System.out.println(tm1.size());
//		System.out.println(tm2.size());
		
		System.out.println("countWithMedian1="+getCountWithMedian(tm1));
		System.out.println("countWithMedian2="+getCountWithMedian(tm2));
		System.out.println("countIn1Not2="+getNewChemicalCount(tm1, tm2,false));
		System.out.println("countIn2Not1="+getNewChemicalCount(tm2, tm1,false));
		
		compareChemicalsInCommon(tm1, tm2, units);
		
	}
	
	void lookAtLLNA_MixtureVsNonMixtureNIEHS_ICE() {
		List<Source>sources1=new ArrayList<>();
		sources1.add(new Source("NIEHS_ICE_2024_08","skin sensitization"));

		ExperimentalRecords recs=getAllExperimentalRecords(sources1);
		ExperimentalRecords recsBad=getAllExperimentalRecords(sources1,true);
		
//		System.out.println(recs.size());
//		System.out.println(recsBad.size());
		
//		if(true)return;
		
		String propertyName=ExperimentalConstants.strSkinSensitizationLLNA;
		String units=ExperimentalConstants.str_binary;
		
		TreeMap<String, ExperimentalRecords> tm1 = getTreeMapByCAS(propertyName, units, recs);
		System.out.println("good\t"+recs.size()+"\t"+tm1.size());
		
		
		TreeMap<String, ExperimentalRecords> tm2 = getTreeMapByCAS(propertyName, units, recsBad);
		System.out.println("bad\t"+recsBad.size()+"\t"+tm2.size());
		
//		System.out.println(tm1.size());
//		System.out.println(tm2.size());
		
		System.out.println("countWithMedian1="+getCountWithMedian(tm1));
		System.out.println("countWithMedian2="+getCountWithMedian(tm2));
		System.out.println("countIn1Not2="+getNewChemicalCount(tm1, tm2,false));
		System.out.println("countIn2Not1="+getNewChemicalCount(tm2, tm1,false));
		
		compareConcordance(tm1, tm2);
		
	}
	
	void lookAtEchemportalLD50_Guidelines2() {
		List<Source>sources1=new ArrayList<>();
		sources1.add(new Source("eChemportalAPI","AcuteToxicityOral"));

		
		ExperimentalRecords recs=getAllExperimentalRecords(sources1);
		
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
		
		TreeMap<String, ExperimentalRecords> tm1 = getTreeMapByCAS(propertyName, units, recs423);
		TreeMap<String, ExperimentalRecords> tm2 = getTreeMapByCAS(propertyName, units, recs420);

		
		System.out.println("countWithMedian1="+getCountWithMedian(tm1));
		System.out.println("countWithMedian2="+getCountWithMedian(tm2));
		System.out.println("countIn1Not2="+getNewChemicalCount(tm1, tm2,false));
		System.out.println("countIn2Not1="+getNewChemicalCount(tm2, tm1,false));

		
		compareChemicalsInCommon(tm1, tm2, units);
		
		System.out.println(tm1.size());
		System.out.println(tm2.size());
	}
	
	void lookAtEchemportalLD50_Guidelines3() {
		List<Source>sources1=new ArrayList<>();
		sources1.add(new Source("eChemportalAPI","AcuteToxicityOral"));

		
		ExperimentalRecords recs=getAllExperimentalRecords(sources1);
		
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
		
		TreeMap<String, ExperimentalRecords> tm1 = getTreeMapByCAS(propertyName, units, recs423);
		TreeMap<String, ExperimentalRecords> tm2 = getTreeMapByCAS(propertyName, units, recs425);

		
		System.out.println("countWithMedian1="+getCountWithMedian(tm1));
		System.out.println("countWithMedian2="+getCountWithMedian(tm2));
		System.out.println("countIn1Not2="+getNewChemicalCount(tm1, tm2,false));
		System.out.println("countIn2Not1="+getNewChemicalCount(tm2, tm1,false));

		
		compareChemicalsInCommon(tm1, tm2, units);
		
		System.out.println(tm1.size());
		System.out.println(tm2.size());
	}
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CompareExperimentalRecords c=new CompareExperimentalRecords();
		
//		c.compare("QSAR_Toolbox_ECHA_Reach_Acute_Toxicity", "QSAR_Toolbox_Acute_Toxicity", ExperimentalConstants.strORAL_RAT_LD50,ExperimentalConstants.str_mg_kg);
		
//		c.compare("ChemIDplus_2024_12_04", "ChemIDplus", ExperimentalConstants.strORAL_RAT_LD50,ExperimentalConstants.str_mg_kg);
		
//		c.compareToChemidplusToEcha();
		
//		c.lookAtLLNA_MixtureVsNonMixtureNIEHS_ICE();
//		c.compareSensitization();
		c.compareBCF();
		
//		c.lookAtEchemportalLD50_Guidelines();
//		c.lookAtEchemportalLD50_Guidelines2();
//		c.lookAtEchemportalLD50_Guidelines3();
		
//		c.compareToNIEHS_OralRatLD50();

//		
//		c.compareREACH_Sources();
		
//		c.compareQSAR_Toolbox_eChemportalAPI();
		
		
//		c.compare("ChemIDplus_2024_12_04", "NIEHS_ICE_2024_08",null, "Acute oral", ExperimentalConstants.strORAL_RAT_LD50,ExperimentalConstants.str_mg_kg);
		
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
		compareChemicalsInCommonConcordance(sources1, sources2,ExperimentalConstants.strSkinSensitizationLLNA,ExperimentalConstants.str_binary);
	

	
	}
	
	
	private void compareBCF() {
		
		List<Source>sources1=new ArrayList<>();
		List<Source>sources2=new ArrayList<>();

		String propertyName=ExperimentalConstants.strFishBCF;
//		String propertyName=ExperimentalConstants.strFishBCFWholeBody;

//		sources1.add(new Source("ECOTOX_2023_12_14",propertyName));
//		sources2.add(new Source("ToxVal_prod",propertyName));

		sources1.add(new Source("ECOTOX_2023_12_14",propertyName));
		sources1.add(new Source("ToxVal_prod",propertyName));
		sources2.add(new Source("Burkhard",propertyName));

		
		String units="L/kg";
		
		ExperimentalRecords recs1=getAllExperimentalRecords(sources1);
		ExperimentalRecords recs2=getAllExperimentalRecords(sources2);
		
		TreeMap<String, ExperimentalRecords> tm1 = getTreeMapByDTXSID(propertyName, units, recs1);
		TreeMap<String, ExperimentalRecords> tm2 = getTreeMapByDTXSID(propertyName, units, recs2);
		
		Gson gson=new Gson();
		
		System.out.println("Source1="+gson.toJson(sources1)+"\t"+tm1.size());
		System.out.println("Source2="+gson.toJson(sources2)+"\t"+tm2.size()+"\n");

		System.out.println("countWithMedian1="+getCountWithMedian(tm1));
		System.out.println("countWithMedian2="+getCountWithMedian(tm2));
		System.out.println("countIn1Not2="+getNewChemicalCount(tm1, tm2,false));
		System.out.println("countIn2Not1="+getNewChemicalCount(tm2, tm1,false));
		System.out.println("countInEither="+getCountInEither(tm2, tm1,false));

		this.compareChemicalsInCommon(tm1, tm2, units);
	
	
	}
	
	
	


	private void compareToNIEHS_OralRatLD50() {
		List<Source>sources1=new ArrayList<>();
		sources1.add(new Source("ChemIDplus_2024_12_04",null));
		sources1.add(new Source("eChemPortalAPI","AcuteToxicityOral"));
//		sources1.add(new Source("QSAR_Toolbox","Acute toxicity ECHA Reach"));
		
		List<Source>sources2=new ArrayList<>();
		sources2.add(new Source("NIEHS_ICE_2024_08","Acute oral"));
		
		compare(sources1, sources2, ExperimentalConstants.strORAL_RAT_LD50,ExperimentalConstants.str_mg_kg);
	}
	

	
	
	private void compareToChemidplusToEcha() {
		List<Source>sources1=new ArrayList<>();
		sources1.add(new Source("ChemIDplus_2024_12_04",null));
				
		List<Source>sources2=new ArrayList<>();
//		sources2.add(new Source("QSAR_Toolbox","Acute toxicity ECHA Reach"));
		sources2.add(new Source("eChemPortalAPI","AcuteToxicityOral"));
		
		compare(sources1, sources2, ExperimentalConstants.strORAL_RAT_LD50,ExperimentalConstants.str_mg_kg);
	}

	
	private void compareREACH_Sources() {
		List<Source>sources1=new ArrayList<>();
		sources1.add(new Source("QSAR_Toolbox","Acute toxicity ECHA Reach"));
	
		List<Source>sources2=new ArrayList<>();
		sources2.add(new Source("eChemPortalAPI","AcuteToxicityOral"));
		
		compare(sources1, sources2, ExperimentalConstants.strORAL_RAT_LD50,ExperimentalConstants.str_mg_kg);
	}

	
	

}
