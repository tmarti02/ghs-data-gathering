package gov.epa.exp_data_gathering.parse;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;

/**
 * Class to remove duplicate values that have exact same property values from 2 sources...
 * 
 * @author TMARTI02
 *
 */
public class DataRemoveDuplicateExperimentalValues {

	/**
	 * Removes records if original source=source2 if having matching record with source1
	 * 
	 * @param records
	 * @param endpoint
	 * @param originalSource1 (preferred source)
	 * @param originalSource2 (removed source)
	 * @param omitBadNumericOperator
	 * @return
	 */

	public void removeDuplicates(ExperimentalRecords records,String sourceName,String originalSource1,String originalSource2) {
			
		Map<String,ExperimentalRecords>mapRecords=convertToMap(records);		
		Set<String> setOfKeys = mapRecords.keySet();
	
		for(String key : setOfKeys) {			             
            ExperimentalRecords recs=mapRecords.get(key);            
//            System.out.println(CAS+"\t"+ht.get(CAS).size());
            if (sourceName.equals(ExperimentalConstants.strSourceEChemPortal) || sourceName.equals(ExperimentalConstants.strSourceEChemPortalAPI)) {
            	remove_eChemPortal_DuplicatesForKey(key, recs, originalSource1, originalSource2);
            }
        }
				 
	}
	
	public void removeDuplicates(ExperimentalRecords records,String sourceName) {		
		
//		if (sourceName.equals(ExperimentalConstants.strSourceEChemPortal) || sourceName.equals(ExperimentalConstants.strSourceEChemPortalAPI)) {
		if (sourceName.equals(ExperimentalConstants.strSourceEChemPortal)) {
			eChemPortalRemoveAllDuplicates(records);
        } else {
        	basicRemoveAllDuplicates(records);
        }
				 
	}
	
	/**
	 * Maps records by CAS, name, and einecs. Useful for removing duplicates between ECHA REACH and ECHA CHEM (ECHA CHEM sometimes has blank name)
	 * 
	 * @param records
	 * @param omitBadNumericOperator
	 * @return
	 */
	Map<String,ExperimentalRecords> convertToMap(ExperimentalRecords records) {
		Map<String,ExperimentalRecords>mapRecords=new TreeMap<>();
		
		
		for (ExperimentalRecord record:records) {
												
			String key=null;
			
			if (record.casrn!=null && !record.casrn.isBlank()) {
				key=record.casrn;
			} else if (record.chemical_name!=null && !record.chemical_name.isBlank()) {
				key=record.chemical_name;
			} else if (record.einecs!=null && !record.einecs.isBlank()) {
				key=record.einecs;
			} else if (record.smiles!=null && !record.smiles.isBlank()) {
				key = record.smiles;
			} else {
				continue;
			}
			
//			System.out.println("key="+key);

			if (mapRecords.containsKey(key)) {
				ExperimentalRecords recs=mapRecords.get(key);
				recs.add(record);
			} else {
				ExperimentalRecords recs=new ExperimentalRecords();
				recs.add(record);
				mapRecords.put(key, recs);
			}
									
		}
		
		return mapRecords;
	}
	
	Map<String,ExperimentalRecords> convertToMapByComboID(ExperimentalRecords records) {
		Map<String,ExperimentalRecords>mapRecords=new TreeMap<>();
		
		int count=0;
		
		for (ExperimentalRecord record:records) {
						
			record.setComboID("|");
			String key=record.comboID;
			
//			System.out.println(key);
						
//			System.out.println("key="+key);

			count++;
			if (mapRecords.containsKey(key)) {
				ExperimentalRecords recs=mapRecords.get(key);
				recs.add(record);
			} else {
				ExperimentalRecords recs=new ExperimentalRecords();
				recs.add(record);
				mapRecords.put(key, recs);
			}
									
		}
		
		return mapRecords;
	}
	
	
	/**
	 * Removes extra records if property value is the same
	 * 
	 * @param records
	 * @param endpoint
	 * @param omitBadNumericOperator
	 * @return
	 */
	public void removeDuplicatesByComboID(ExperimentalRecords records) {
		Map<String,ExperimentalRecords>mapRecords=convertToMapByComboID(records);
		
		Set<String> setOfKeys = mapRecords.keySet();
		
//        int count = getCountIDsWithKeptRecord(mapRecords, setOfKeys);
//		System.out.println("Number of IDs before duplicates removed:"+count);
				
		for(String key : setOfKeys) {			             
            ExperimentalRecords recs=mapRecords.get(key);            
//            System.out.println(CAS+"\t"+ht.get(CAS).size());
//            System.out.println(key);
            removeDuplicatesForKey(recs);            
                    
        }

//        int countAfter = getCountIDsWithKeptRecord(mapRecords, setOfKeys);
//		System.out.println("Number of IDS after removed:"+countAfter);
						
	
	}

	private int getCountIDsWithKeptRecord(Map<String, ExperimentalRecords> mapRecords, Set<String> setOfKeys) {
		int count=0;						

		for(String key : setOfKeys) {			
            ExperimentalRecords recs=mapRecords.get(key);
			for (ExperimentalRecord rec:recs) {
				if (rec.keep) {
					count++;
					break;
				}
			}
		}
		return count;
	}
	
	/**
	 * 
	 * For echemportal records, duplicates are removed if:<br>
	 *  - have nominal value but have matching analytical value<br>
	 *  - have source 2 but have matching value from source 1 <br>
	 *  - have duplicate for same source
	 * 
	 * @param recs
	 * @param source1
	 * @param source2
	 */
	void remove_eChemPortal_DuplicatesForKey(String key, ExperimentalRecords recs,String source1,String source2) {
		removeNominalIfHaveAnalytical(recs);
		removeOriginalSource2IfOriginalHaveSource1(recs, source1, source2);				
		//now that we removed duplicates from source2, delete remaining property duplicates where source name is the same		
		removeDuplicatesForSameSource(key,recs);
	}
	
	void eChemPortalRemoveAllDuplicates(ExperimentalRecords records) {
		Map<String,ExperimentalRecords>mapRecords=convertToMap(records);
		Set<String> setOfKeys = mapRecords.keySet();
		for(String key : setOfKeys) {			             
            ExperimentalRecords recs=mapRecords.get(key);
            remove_eChemPortal_DuplicatesForKey(key,recs, "ECHA REACH", "ECHA CHEM");
		}
	}
	
	void basicRemoveAllDuplicates(ExperimentalRecords records) {
		Map<String,ExperimentalRecords>mapRecords=convertToMapByComboID(records);
		Set<String> setOfKeys = mapRecords.keySet();
		for(String key : setOfKeys) {			             
            ExperimentalRecords recs=mapRecords.get(key);
            removeDuplicatesForSameSource(key, recs);
		}
	}
	
	
	

	private void removeDuplicatesForSameSource(String key,ExperimentalRecords recs) {

		boolean print=false;
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		String cas="NOCAS_902561";
		
		boolean flagged=false;
		
		for (int i=0;i<recs.size();i++) {
			ExperimentalRecord reci=recs.get(i);	

//			if (reci.casrn!=null && reci.casrn.equals(cas))				
//				System.out.println("before\t"+i+"\t"+reci.keep+"\t"+reci.reason);
		}
				
		for (int i=0;i<recs.size();i++) {			

			ExperimentalRecord reci=recs.get(i);			
//			System.out.println(reci.original_source_name+"\t"+source1);
				
			if(!reci.keep) continue;
			
			String tsi=reci.property_value_string;
			
			if(reci.property_name==null) {
				System.out.println("When removing duplicates, no name:\t"+ParseUtilities.gson.toJson(reci)+"\n");
				continue;
			}

			
			for (int j=i+1;j<recs.size();j++) {

//				if (i==j) continue;
								
				ExperimentalRecord recj=recs.get(j);
				
				if (!recj.keep) continue;
				
				
				
				if(!reci.property_name.equals(recj.property_name)) continue;
				
				if(!reci.getOriginalSourceNames().equals(recj.getOriginalSourceNames())) {
//					System.out.println(gson.toJson(reci));
//					System.out.println(gson.toJson(recj));
//					System.out.println("*********************************");
					continue;
				}
				
//				if(reci.original_source_name!=null && !reci.original_source_name.equals(recj.original_source_name)) continue;
				
				String tsj=recj.property_value_string;
				
				
				String pvi=null;
				String pvj=null;
				
				if(reci.property_value_string_parsed!=null) pvi=reci.property_value_string_parsed;
				else pvi=reci.property_value_string;
				
				if(recj.property_value_string_parsed!=null) pvj=recj.property_value_string_parsed;
				else pvj=recj.property_value_string;
								
				boolean matchPropertyValueString=false;
				
				if(pvi!=null && pvj!=null) {
					matchPropertyValueString=pvi.contentEquals(pvj);	
				}
				
				
				boolean matchPointEstimate = getMatchPointEstimate(print, reci, recj);

				boolean matchUnits=reci.property_value_units_final!=null && recj.property_value_units_final!=null && reci.property_value_units_final==recj.property_value_units_final;
				boolean matchPressure = getMatchPressure(reci, recj);
				boolean matchTemp = getMatchTemp(reci, recj);
				boolean match_pH = getMatch_pH(reci, recj);
				
				boolean match=false;
				if((matchPropertyValueString || matchPointEstimate) && matchUnits && matchTemp && matchPressure && match_pH) 
					match=true;
				
//				if(matchUnits && matchTemp && matchPressure && match_pH && match==false  && !matchPropertyValueString) {
//					if (reci.property_value_point_estimate_final==null && recj.property_value_point_estimate_final==null) {
//						System.out.println(pvi);
//						System.out.println(pvj+"\n");
//					}
//				}
				
				if(match) {
					flagged=true;
//					System.out.println("Remove exact match same src:"+recj);
					recj.keep=false;
					recj.reason="Duplicate of experimental value from same source";
					
//					if(reci.casrn!=null && reci.casrn.equals("109-83-1")) {
//						System.out.println("Duplicate\t"+reci.casrn+"\t"+reci.chemical_name+"\n"+pvi+"\t"+pvj+"\t"+haveBothPointEstimates+"\n");
//					}
					
//					if(reci.casrn!=null && reci.casrn.equals("109-83-1")) {
//						System.out.println("************************Duplicate:");
//						System.out.println(gson.toJson(reci));
//						System.out.println(gson.toJson(recj)+"\n");
//					}
					
//					System.out.println(key+"\t"+recj.reason+"\t"+tsi+"\t"+tsj);
					
//					if (reci.casrn!=null && reci.casrn.equals(cas)) {
//						System.out.println("\n"+i+"\t"+j);
//						System.out.println(key);
//						System.out.println(recj.reason);
//						System.out.println(tsi);
//						System.out.println(tsj+"\n");
//					}

				} else {// different values

					// Following handles OPERA2.9 which has records from sdf and csv files
					if (reci.source_name.equals("OPERA2.9")) {
//							System.out.println("mismatch:"+reci.dsstox_substance_id+"\t"+tsi+"\t"+tsj);
						
						if (reci.file_name.contains("csv") && recj.file_name.contains("sdf")) {
//								System.out.println(gson.toJson(reci));
//								System.out.println(gson.toJson(recj) + "\n***********\n");
							recj.keep = false;
							recj.reason = "SDF record superceded by CSV record";
						} else if (recj.file_name.contains("csv") && reci.file_name.contains("sdf")) {
							reci.keep = false;
							reci.reason = "SDF record superceded by CSV record";
						} else {
//							System.out.println(gson.toJson(reci));
//							System.out.println(gson.toJson(recj) + "\n***********\n");

							reci.flag = true;
							reci.reason = "Duplicate record with different property value";

							recj.flag = true;
							recj.reason = "Duplicate record with different property value";

						}

					}
				}
				
//				if(print) System.out.println("");

			}//j loop
		}//i loop
			
//		for (int i=0;i<recs.size();i++) {
//			ExperimentalRecord reci=recs.get(i);	
//			if (reci.casrn!=null && reci.casrn.equals(cas))	
//				System.out.println("after\t"+i+"\t"+reci.keep+"\t"+reci.reason);
//		}

	}

	private boolean getMatch_pH(ExperimentalRecord reci, ExperimentalRecord recj) {
		boolean match_pH=false;
		
		if(reci.pH==null && recj.pH==null) {
			match_pH=true;
		}
		
		if(reci.pH!=null && recj.pH!=null) {
			if(reci.pH.contentEquals(recj.pH)) {
				match_pH=true;
			}
		}
		return match_pH;
	}

	private boolean getMatchTemp(ExperimentalRecord reci, ExperimentalRecord recj) {
		boolean matchTemp=false;
		if(reci.temperature_C==null && recj.temperature_C==null) {
			matchTemp=true;
		}
		if(reci.temperature_C !=null && recj.temperature_C!=null) {
			if(Math.abs(reci.temperature_C-recj.temperature_C)<0.01) {
				matchTemp=true;
			}
		}
		return matchTemp;
	}

	private boolean getMatchPressure(ExperimentalRecord reci, ExperimentalRecord recj) {
		boolean matchPressure=false;
		if(reci.pressure_mmHg==null && recj.pressure_mmHg==null) {
			matchPressure=true;
		}
		
		if(reci.pressure_mmHg!=null && recj.pressure_mmHg!=null) {
			if(reci.pressure_mmHg.contentEquals(recj.pressure_mmHg)) {
				matchPressure=true;
			}
		}
		return matchPressure;
	}

	private boolean getMatchPointEstimate(boolean print, ExperimentalRecord reci, ExperimentalRecord recj) {
		boolean matchPointEstimate=false;

		if (reci.property_value_point_estimate_final!=null && recj.property_value_point_estimate_final!=null) {
			double diff=Math.abs(reci.property_value_point_estimate_final-recj.property_value_point_estimate_final);

			//TODO need to check if at same temperature or pressure
			
			if (reci.property_value_point_estimate_final!=0) {
				diff/=Math.abs(reci.property_value_point_estimate_final);
				diff*=100.0;//convert to %
				
				if(diff<0.01) {//<0.01% different
					if(print) System.out.println("<0.01%\t"+reci.property_value_point_estimate_final+"\t"+recj.property_value_point_estimate_final+"\t"+diff);
					matchPointEstimate=true;
				} else {
					if(print) System.out.println(">0.01%\t"+reci.property_value_point_estimate_final+"\t"+recj.property_value_point_estimate_final+"\t"+diff);
				}
			} else {
				if(diff<1e-6) {
					if(print) System.out.println("<1e-6\t"+reci.property_value_point_estimate_final+"\t"+recj.property_value_point_estimate_final);
					matchPointEstimate=true;
				} else {
					if(print) System.out.println(">1e-6\t"+reci.property_value_point_estimate_final+"\t"+recj.property_value_point_estimate_final);
				}
			}
		}
		
		String q1=reci.property_value_numeric_qualifier+"";
		String q2=reci.property_value_numeric_qualifier+"";
		
		if(!q1.equals(q2)) {
			System.out.println("Mismatch on qualifier:\t"+q1+"\t"+q2+"\t"+reci.comboID);
			return false;
		} 
		
		
		return matchPointEstimate;
	}

	private void removeOriginalSource2IfOriginalHaveSource1(ExperimentalRecords recs, String originalSource1, String originalSource2) {
		
		for (int i=0;i<recs.size();i++) {			
			ExperimentalRecord reci=recs.get(i);			
//			System.out.println(reci.original_source_name+"\t"+source1);
			if(!reci.keep) continue;
			
			if (!reci.original_source_name.contentEquals(originalSource1)) continue;
			
			String tsi=reci.property_value_string+"\t"+reci.property_name;
			
			for (int j=0;j<recs.size();j++) {

				if (i==j) continue;
				
				ExperimentalRecord recj=recs.get(j);
				if (!recj.keep) continue;
				
				if (!recj.original_source_name.contentEquals(originalSource2)) continue;												
				
				String tsj=recj.property_value_string+"\t"+recj.property_name;
				
//				if (reci.casrn.contentEquals("10049-04-4")) {
//					System.out.println(tsi+"\t"+tsj+"\t"+tsi.contentEquals(tsj));
//				}
				
				if (tsi.contentEquals(tsj)) {
//					recs.remove(j);
//					j--;				
					recj.keep=false;
					recj.reason="Duplicate experimental value, already have from "+originalSource1;
//					System.out.println("removed duplicate:"+recj);
					
				} else {
//					System.out.println("mismatch:"+tsi+"\t"+tsj);
				}
			}
			
		}
	}

	private void removeNominalIfHaveAnalytical(ExperimentalRecords recs) {
		//Remove nominal and unspecified records if have analytical
		for (int i=0;i<recs.size();i++) {
			
			ExperimentalRecord reci=recs.get(i);			
//			System.out.println(reci.original_source_name+"\t"+source1);
			if(!reci.keep) continue;
					
			if (reci.note!=null && reci.note.contains("analytical units")) continue;

			boolean haveAnalytical=false;

			for (int j=0;j<recs.size();j++) {

				if (i==j) continue;
				
				ExperimentalRecord recj=recs.get(j);
				
				if (!recj.keep) continue;
				
				if (recj.note==null) continue;
				
				if (reci.property_name.equals(recj.property_name) && recj.note.contains("analytical units")) {
					haveAnalytical=true;
					break;
				}
				
			}
//			System.out.println(reci.casrn+"\t"+haveNonNominal);

			if (haveAnalytical) {
				reci.keep=false;
				reci.reason="Analytical value available";
			}
			
			
		}
	}
	
	/**
	 * Goes through records with same CAS (or same name or EINECS sometimes).
	 * If have same tox value for matching pair, it keeps source1 value and omits source2 value
	 * 
	 * Then it goes through and omits same exact value from same source
	 * 
	 * @param recs
	 * @param source1
	 * @param source2
	 */
	void removeDuplicatesForKey(ExperimentalRecords recs) {
		for (int i=0;i<recs.size();i++) {			
			ExperimentalRecord reci=recs.get(i);			
			
			if(!reci.keep) continue;
			
			String tsi=reci.property_value_string+"\t"+reci.property_name;
			
			for (int j=i+1;j<recs.size();j++) {

//				if (i==j) continue;
				
				ExperimentalRecord recj=recs.get(j);

				if(!recj.keep) continue;
				
				String tsj=recj.property_value_string+"\t"+recj.property_name;
				
				if (tsi.contentEquals(tsj)) {
//					recs.remove(j);
//					j--;
					
					recj.keep=false;
					recj.reason="Duplicate experimental value";
//					System.out.println("removed duplicate:"+recj);
					
				} else {
//					System.out.println("mismatch:"+tsi+"\t"+tsj);
				}
			}
			
		}
		
	}
	
	// Can just use property_value_string, which includes full experimental conditions
//	String getToxString(ExperimentalRecord er) {
//		if (er.property_value_point_estimate_final==null) {
//			return er.property_value_min_final+"-"+er.property_value_max_final+"\t"+er.property_value_units_final;
//		} else {
//			return er.property_value_numeric_qualifier+"\t"+er.property_value_point_estimate_final+"\t"+er.property_value_units_final;			
//		}
//
//	}
	


}
