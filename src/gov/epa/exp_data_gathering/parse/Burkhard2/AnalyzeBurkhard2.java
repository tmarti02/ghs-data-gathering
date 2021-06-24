package gov.epa.exp_data_gathering.parse.Burkhard2;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.Burkhard2.RecordBurkhard2;


class MultiMap<K, V>
{
    private Map<K, Collection<V>> map = new HashMap<>();
 
    /**
    * Add the specified value with the specified key in this multimap.
    */
    public void put(K key, V value)
    {
        if (map.get(key) == null) {
            map.put(key, new ArrayList<V>());
        }
 
        map.get(key).add(value);
    }
 
    /**
    * Associate the specified key with the given value if not
    * already associated with a value
    */
    public void putIfAbsent(K key, V value)
    {
        if (map.get(key) == null) {
            map.put(key, new ArrayList<>());
        }
 
        // if the value is absent, insert it
        if (!map.get(key).contains(value)) {
            map.get(key).add(value);
        }
    }
 
    /**
    * Returns the Collection of values to which the specified key is mapped,
    * or null if this multimap contains no mapping for the key.
    */
    public Collection<V> get(Object key) {
        return map.get(key);
    }
 
    /**
    * Returns a set view of the keys contained in this multimap.
    */
    public Set<K> keySet() {
        return map.keySet();
    }
 
    /**
    * Returns a set view of the mappings contained in this multimap.
    */
    public Set<Map.Entry<K, Collection<V>>> entrySet() {
        return map.entrySet();
    }
 
    /**
    * Returns a Collection view of Collection of the values present in
    * this multimap.
    */
    public Collection<Collection<V>> values() {
        return map.values();
    }
 
    /**
    * Returns true if this multimap contains a mapping for the specified key.
    */
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }
 
    /**
    * Removes the mapping for the specified key from this multimap if present
    * and returns the Collection of previous values associated with key, or
    * null if there was no mapping for key.
    */
    public Collection<V> remove(Object key) {
        return map.remove(key);
    }
 
    /**
    * Returns the total number of key-value mappings in this multimap.
    */
    public int size()
    {
        int size = 0;
        for (Collection<V> value: map.values()) {
            size += value.size();
        }
        return size;
    }
 
    /**
    * Returns true if this multimap contains no key-value mappings.
    */
    public boolean isEmpty() {
        return map.isEmpty();
    }
 
    /**
    * Removes all the mappings from this multimap.
    */
    public void clear() {
        map.clear();
    }
 
    /**
    * Removes the entry for the specified key only if it is currently
    * mapped to the specified value and return true if removed
    */
    public boolean remove(K key, V value)
    {
        if (map.get(key) != null) // key exists
            return map.get(key).remove(value);
 
        return false;
    }
 
    /**
    * Replaces the entry for the specified key only if currently
    * mapped to the specified value and return true if replaced
    */
}


public class AnalyzeBurkhard2 {

	protected static ExperimentalRecords goThroughExperimentalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		Gson gson=null;
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues();
		gson = builder.create();

		
		try {
			String jsonFileName = "C:\\Users\\Weeb\\Documents\\refreshedproject\\data\\experimental\\Burkhard2" + File.separator + "Burkhard2 Experimental Records.json";
			File jsonFile = new File(jsonFileName);
			List<ExperimentalRecord> recordsBurkhard2 = new ArrayList<ExperimentalRecord>();
			ExperimentalRecord[] tempRecords = null;
				tempRecords = gson.fromJson(new FileReader(jsonFile), ExperimentalRecord[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsBurkhard2.add(tempRecords[i]);
				}
	
			Iterator<ExperimentalRecord> it = recordsBurkhard2.iterator();
			while (it.hasNext()) {
				ExperimentalRecord r = it.next();
				recordsExperimental.add(r);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}	

	
	private void notmain() {
        MultiMap<String, ExperimentalRecord> multimap = new MultiMap();
		ExperimentalRecords er = goThroughExperimentalRecords();
		for (int i = 0; i < er.size(); i++) {
			multimap.put(er.get(i).casrn, er.get(i));
		}
		
		
		ArrayList<ExperimentalRecord> only1763231 = new ArrayList<>(multimap.get("1763-23-1"));
		
		
		for (int k = 0; k < only1763231.size(); k++) {
			ArrayList<ExperimentalRecord> only1763231LogBCFSteadyState = new ArrayList<>();
			for (int j = 0; j < only1763231.size(); j++) {
			if (only1763231.get(j).property_name.equals("LogBCFSteadyState")) {
				only1763231LogBCFSteadyState.add(only1763231.get(j));			
			}
			
			ArrayList<Double> pointestimates = new ArrayList<>();
			for (int l = 0; l < only1763231LogBCFSteadyState.size(); l++) {
				pointestimates.add(only1763231LogBCFSteadyState.get(l).property_value_point_estimate_final);
				}
	        double[] arr = pointestimates.stream().mapToDouble(Double::doubleValue).toArray();

			
			double median = medianCal(only1763231LogBCFSteadyState.size(), arr);
			System.out.println(median);

			}

			
			}
			// System.out.println(only1763231.get(k));
		
		
        for (String casrn: multimap.keySet()) {
            // System.out.println(casrn + ": " + multimap.get(casrn));
         
        }
	}


	private static double medianCal(int  n, double in[]) {
		double m=0;	
		
		if(n%2==1)
		{
			m=in[((n+1)/2)-1];
			
		}
		else
		{
			m=(in[n/2-1]+in[n/2])/2;
			
		}
	return m;
		
	 }
	
	public static void main(String[] args) {
		AnalyzeBurkhard2 abh2 = new AnalyzeBurkhard2();
		abh2.notmain();
	}

}
