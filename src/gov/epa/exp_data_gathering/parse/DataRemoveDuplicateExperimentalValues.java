package gov.epa.exp_data_gathering.parse;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
	 * @param source1 (preferred source)
	 * @param source2 (removed source)
	 * @param omitBadNumericOperator
	 * @return
	 */
	public void removeDuplicatesByOriginalSource(ExperimentalRecords records,String source1,String source2,boolean omitBadNumericOperator) {
			
		Map<String,ExperimentalRecords>mapRecords=convertToMap(records, omitBadNumericOperator);		
		Set<String> setOfKeys = mapRecords.keySet();
	
		for(String key : setOfKeys) {			             
            ExperimentalRecords recs=mapRecords.get(key);            
//            System.out.println(CAS+"\t"+ht.get(CAS).size());
            remove_eChemPortal_DuplicatesForKey(recs, source1, source2);            
        }
				 
	}
	
	/**
	 * Maps records by CAS, name, and einecs. Useful for removing duplicates between ECHA REACH and ECHA CHEM (ECHA CHEM sometimes has blank name)
	 * 
	 * @param records
	 * @param omitBadNumericOperator
	 * @return
	 */
	Map<String,ExperimentalRecords> convertToMap(ExperimentalRecords records,boolean omitBadNumericOperator) {
		Map<String,ExperimentalRecords>mapRecords=new TreeMap<>();
		
		
		for (ExperimentalRecord record:records) {
						
			
			if (omitBadNumericOperator && record.property_value_numeric_qualifier!=null && !record.property_value_numeric_qualifier.contentEquals("~")) {
				record.keep=false;
				record.reason="Has numeric operator";
//				System.out.println("Has numeric operator:"+record);
			} else {
//				System.out.println("No numeric operator:"+record);
			}
												
			String key=null;
			
			//TODO add smiles as a possible key...(echemportal doesnt report it)
			
			if (record.casrn!=null) {
				key=record.casrn;
			} else if (record.chemical_name!=null) {
				key=record.chemical_name;
			} else if (record.einecs!=null) {
				key=record.einecs;
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
	
	Map<String,ExperimentalRecords> convertToMapByComboID(ExperimentalRecords records,boolean omitBadNumericOperator) {
		Map<String,ExperimentalRecords>mapRecords=new TreeMap<>();
		
		int count=0;
		
		for (ExperimentalRecord record:records) {
						
			
//			if (omitBadNumericOperator && record.property_value_numeric_qualifier!=null && !record.property_value_numeric_qualifier.contentEquals("~")) {
//				record.keep=false;
//				record.reason="Has numeric operator";
//			}
						
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
	public void removeDuplicatesByComboID(ExperimentalRecords records,boolean omitBadNumericOperator) {
		Map<String,ExperimentalRecords>mapRecords=convertToMapByComboID(records, omitBadNumericOperator);
		
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
	void remove_eChemPortal_DuplicatesForKey(ExperimentalRecords recs,String source1,String source2) {
		removeNominalIfHaveAnalytical(recs);
		removeOriginalSource2IfOriginalHaveSource1(recs, source1, source2);				
		//now that we removed duplicates from source2, delete remaining property duplicates where source name is the same		
		removeDuplicatesForSameSource(recs);
	}

	private void removeDuplicatesForSameSource(ExperimentalRecords recs) {
		for (int i=0;i<recs.size();i++) {			
			ExperimentalRecord reci=recs.get(i);			
//			System.out.println(reci.original_source_name+"\t"+source1);
				
			if(!reci.keep) continue;
			
			for (int j=0;j<recs.size();j++) {

				if (i==j) continue;
								
				ExperimentalRecord recj=recs.get(j);
				
				if (!recj.keep) continue;
				
				String tsi=reci.property_value_string+"\t"+reci.original_source_name+"\t"+reci.property_name;
				String tsj=recj.property_value_string+"\t"+recj.original_source_name+"\t"+recj.property_name;
								
				if (tsi.contentEquals(tsj)) {
//					System.out.println("Remove exact match same src:"+recj);
//					recs.remove(j);
//					j--;
					
					recj.keep=false;
					recj.reason="Duplicate experimental value from same original source";
					
					
				} else {
//					System.out.println("mismatch:"+tsi+"\t"+tsj);
				}
			}
			
		}
	}

	private void removeOriginalSource2IfOriginalHaveSource1(ExperimentalRecords recs, String originalSource1, String originalSource2) {
		
		for (int i=0;i<recs.size();i++) {			
			ExperimentalRecord reci=recs.get(i);			
//			System.out.println(reci.original_source_name+"\t"+source1);
			if(!reci.keep) continue;
			
			if (!reci.original_source_name.contentEquals(originalSource1)) continue;
			
			for (int j=0;j<recs.size();j++) {

				if (i==j) continue;
				
				ExperimentalRecord recj=recs.get(j);
				if (!recj.keep) continue;
				
				if (!recj.original_source_name.contentEquals(originalSource2)) continue;												
				
				String tsi=reci.property_value_string+"\t"+reci.property_name;
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
		//Remove nominal records if have non nominal:
		for (int i=0;i<recs.size();i++) {
			
			ExperimentalRecord reci=recs.get(i);			
//			System.out.println(reci.original_source_name+"\t"+source1);
			if(!reci.keep) continue;
			
			if (reci.note==null) continue;			
			if (!reci.note.contains("nominal units")) continue;

			boolean haveNonNominal=false;

			for (int j=0;j<recs.size();j++) {

				if (i==j) continue;
				
				ExperimentalRecord recj=recs.get(j);
				
				if (!recj.keep) continue;
				
				if (recj.note==null || !recj.note.contains("nominal units")) {
					haveNonNominal=true;
					break;
				}
				
			}
//			System.out.println(reci.casrn+"\t"+haveNonNominal);

			if (haveNonNominal) {
				reci.keep=false;
				reci.reason="Concentration is nominal, analytical value available";
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
