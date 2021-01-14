package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Class to remove duplicate values that have exact same property values from 2 sources...
 * 
 * @author TMARTI02
 *
 */
public class ToxValRecordDeduplicator {

	public static List<ToxValRecordEChemPortalAPI> removeDuplicates(List<ToxValRecordEChemPortalAPI> records) {
		Map<String,List<ToxValRecordEChemPortalAPI>>mapRecords=convertToMap(records);
		Set<String> setOfKeys = mapRecords.keySet();
		List<ToxValRecordEChemPortalAPI> deduplicatedRecords = new ArrayList<ToxValRecordEChemPortalAPI>();
		for(String key : setOfKeys) {			             
            List<ToxValRecordEChemPortalAPI> recs=mapRecords.get(key);
            remove_eChemPortal_DuplicatesForKey(recs, "ECHA REACH", "ECHA CHEM");
            deduplicatedRecords.addAll(recs);
		}
		return deduplicatedRecords;
	}
	
	/**
	 * Maps records by CAS, name, and einecs. Useful for removing duplicates between ECHA REACH and ECHA CHEM (ECHA CHEM sometimes has blank name)
	 * 
	 * @param records
	 * @param omitBadNumericOperator
	 * @return
	 */
	private static Map<String,List<ToxValRecordEChemPortalAPI>> convertToMap(List<ToxValRecordEChemPortalAPI> records) {
		Map<String,List<ToxValRecordEChemPortalAPI>>mapRecords=new TreeMap<>();
		
		
		for (ToxValRecordEChemPortalAPI record:records) {
												
			String key=null;
			
			if (record.number!=null && !record.number.isBlank() && record.name!=null && !record.name.isBlank()) {
				key=record.number+"|"+record.name;
			} else if (record.number!=null && !record.number.isBlank()) {
				key=record.number;
			}  else if (record.name!=null && !record.name.isBlank()) {
				key=record.name;
			} else {
				continue;
			}
			
//			System.out.println("key="+key);

			if (mapRecords.containsKey(key)) {
				List<ToxValRecordEChemPortalAPI> recs=mapRecords.get(key);
				recs.add(record);
			} else {
				List<ToxValRecordEChemPortalAPI> recs=new ArrayList<ToxValRecordEChemPortalAPI>();
				recs.add(record);
				mapRecords.put(key, recs);
			}
									
		}
		
		return mapRecords;
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
	private static void remove_eChemPortal_DuplicatesForKey(List<ToxValRecordEChemPortalAPI> recs,String source1,String source2) {
		removeOriginalSource2IfOriginalHaveSource1(recs, source1, source2);		
		removeDuplicatesForSameSource(recs);
	}

	private static void removeDuplicatesForSameSource(List<ToxValRecordEChemPortalAPI> recs) {
		HashSet<Integer> toRemove = new HashSet<Integer>();
		
		for (int i=0;i<recs.size();i++) {			
			if (toRemove.contains(i)) continue;
			
			ToxValRecordEChemPortalAPI reci=recs.get(i);			
			
			for (int j=0;j<recs.size();j++) {

				if (i==j || toRemove.contains(j)) continue;
								
				ToxValRecordEChemPortalAPI recj=recs.get(j);
								
				if (reci.recordEquals(recj)) {
					toRemove.add(j);
				}
			}
		}
		
		List<Integer> toRemoveList = new ArrayList<Integer>(toRemove);
		Collections.sort(toRemoveList,Collections.reverseOrder());
		for (int i:toRemoveList) {
			recs.remove(i);
		}
	}

	private static void removeOriginalSource2IfOriginalHaveSource1(List<ToxValRecordEChemPortalAPI> recs, String originalSource1, String originalSource2) {
		HashSet<Integer> toRemove = new HashSet<Integer>();
		
		for (int i=0;i<recs.size();i++) {	
			if (toRemove.contains(i)) continue;
			
			ToxValRecordEChemPortalAPI reci=recs.get(i);			
			
			if (!reci.participant.contentEquals(originalSource1)) continue;
			
			for (int j=0;j<recs.size();j++) {

				if (i==j || toRemove.contains(j)) continue;
				
				ToxValRecordEChemPortalAPI recj=recs.get(j);
				
				if (!recj.participant.contentEquals(originalSource2)) continue;												
				
				if (reci.recordEquals(recj)) {
					toRemove.add(j);
				}
			}
			
		}
		
		List<Integer> toRemoveList = new ArrayList<Integer>(toRemove);
		Collections.sort(toRemoveList,Collections.reverseOrder());
		for (int i:toRemoveList) {
			recs.remove(i);
		}
	}
}