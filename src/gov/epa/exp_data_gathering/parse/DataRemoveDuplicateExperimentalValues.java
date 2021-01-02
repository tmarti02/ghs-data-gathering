package gov.epa.exp_data_gathering.parse;

import java.util.Hashtable;
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

	
	void removeDuplicates(ExperimentalRecords records,String endpoint,String source1,String source2,boolean omitBadNumericOperator) {

		Map<String,ExperimentalRecords>ht=new TreeMap<>();
		
		int count=0;
		for (ExperimentalRecord record:records) {
						
			if (!record.property_name.contentEquals(endpoint)) continue;
			
			if (omitBadNumericOperator && record.property_value_numeric_qualifier!=null) continue;
									
			String key=null;
			
			//TODO add smiles as a possible key...
			
			if (record.casrn!=null) {
				key=record.casrn;
			} else if (record.chemical_name!=null) {
				key=record.chemical_name;
			} else if (record.einecs!=null) {
				key=record.einecs;
			}
			
//			System.out.println("key="+key);

			count++;
			if (ht.containsKey(key)) {
				ExperimentalRecords recs=ht.get(key);
				recs.add(record);
			} else {
				ExperimentalRecords recs=new ExperimentalRecords();
				recs.add(record);
				ht.put(key, recs);
			}
									
		}
		System.out.println("Count before removing duplicates"+count);
		
		Set<String> setOfKeys = ht.keySet();
	
		
		ExperimentalRecords records2=new ExperimentalRecords();
		
		int uniqueCount=0;
		for(String key : setOfKeys) {			 
            
            ExperimentalRecords recs=ht.get(key);            
            if (recs.size()==1) continue;

//            System.out.println(CAS+"\t"+ht.get(CAS).size());

            removeDuplicates(recs, source1, source2);            
            records2.addAll(recs);
            
            if (recs.size()>0) uniqueCount++;
            
        }
		
//		for (ExperimentalRecord record:records2) {
//			System.out.println(record);
//		}
		 
		System.out.println("Record count after removing duplicates:"+records2.size());
		System.out.println("Unique key count:"+uniqueCount);
	
	}
	
	void removeDuplicates(ExperimentalRecords recs,String source1,String source2) {
		for (int i=0;i<recs.size();i++) {			
			ExperimentalRecord reci=recs.get(i);			
//			System.out.println(reci.original_source_name+"\t"+source1);
			
			if (!reci.original_source_name.contentEquals(source1)) continue;
			
			for (int j=0;j<recs.size();j++) {

				if (i==j) continue;
				
				ExperimentalRecord recj=recs.get(j);
				if (!recj.original_source_name.contentEquals(source2)) continue;												
				
				String tsi=getToxString(reci);
				String tsj=getToxString(recj);
				
				if (tsi.contentEquals(tsj)) {
					recs.remove(recj);
					j--;
				} else {
//					System.out.println("mismatch:"+tsi+"\t"+tsj);
				}
			}
			
		}
	}
	
	String getToxString(ExperimentalRecord er) {
		return er.property_value_numeric_qualifier+"\t"+er.property_value_point_estimate_final+"\t"+er.property_value_units_final;
	}
	
	
	
	public static void main(String[] args) {
		DataRemoveDuplicateExperimentalValues d=new DataRemoveDuplicateExperimentalValues();
		
		String filepathExcel="data\\experimental\\eChemPortalAPI\\eChemPortalAPI Toxicity Experimental Records.xlsx";
		ExperimentalRecords records = ExperimentalRecords.loadFromExcel(filepathExcel);
		String source1="ECHA REACH";
		String source2="ECHA CHEM";
		d.removeDuplicates(records,"rat_inhalation_LC50",source1,source2,true);
		
	}
	

}
