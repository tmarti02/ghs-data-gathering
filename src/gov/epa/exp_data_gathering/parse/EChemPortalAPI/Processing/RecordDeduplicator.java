package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Eliminates duplicate records from eChemPortal results
 * @author GSINCL01 (Gabriel Sinclair), TMARTI02 (Todd Martin)
 *
 */
public class RecordDeduplicator {

	/**
	 * Removes all duplicates from a list of parsed records
	 * @param records	The list of records to be deduplicated
	 * @return			The deduplicated list of records
	 */
	public static FinalRecords removeDuplicates(FinalRecords records) {
		Map<String,FinalRecords>mapRecords=records.toMap();
		Set<String> setOfKeys = mapRecords.keySet();
		FinalRecords deduplicatedRecords = new FinalRecords();
		for(String key : setOfKeys) {			             
			FinalRecords recs=mapRecords.get(key);
            removeDuplicatesForKey(recs);
            deduplicatedRecords.addAll(recs);
		}
		return deduplicatedRecords;
	}

	/**
	 * Deduplicates all records associated with a single key
	 * @param recs		Record subset to be deduplicated
	 */
	private static void removeDuplicatesForKey(FinalRecords recs) {
		removeDuplicatesBetweenSources(recs, "ECHA REACH", "ECHA CHEM");		
		removeDuplicatesWithinSource(recs);
	}

	/**
	 * Removes equivalent records from the same original source (ECHA CHEM, ECHA REACH, IUCLID, J-CHECK)
	 * @param recs		Record subset to be deduplicated
	 */
	private static void removeDuplicatesWithinSource(FinalRecords recs) {
		HashSet<Integer> toRemove = new HashSet<Integer>();
		
		for (int i=0;i<recs.size();i++) {			
			if (toRemove.contains(i)) continue;
			
			FinalRecord reci=recs.get(i);			
			
			for (int j=0;j<recs.size();j++) {

				if (i==j || toRemove.contains(j)) continue;
								
				FinalRecord recj=recs.get(j);
								
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

	/**
	 * Removes equivalent records from different sources (used primarily for frequent ECHA CHEM/REACH duplication)
	 * @param recs				Record subset to be deduplicated
	 * @param originalSource1	Preferred original source
	 * @param originalSource2	Deprecated original source
	 */
	private static void removeDuplicatesBetweenSources(FinalRecords recs, String originalSource1, String originalSource2) {
		HashSet<Integer> toRemove = new HashSet<Integer>();
		
		for (int i=0;i<recs.size();i++) {	
			if (toRemove.contains(i)) continue;
			
			FinalRecord reci=recs.get(i);			
			
			if (!reci.participant.contentEquals(originalSource1)) continue;
			
			for (int j=0;j<recs.size();j++) {

				if (i==j || toRemove.contains(j)) continue;
				
				FinalRecord recj=recs.get(j);
				
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