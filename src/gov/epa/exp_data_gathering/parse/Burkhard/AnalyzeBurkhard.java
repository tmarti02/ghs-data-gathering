package gov.epa.exp_data_gathering.parse.Burkhard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.DataRemoveDuplicateExperimentalValues;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.JSONUtilities;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.Sander.RecordSander;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.charts.AxisCrosses;
import org.apache.poi.ss.usermodel.charts.AxisPosition;
import org.apache.poi.ss.usermodel.charts.ChartDataSource;
import org.apache.poi.ss.usermodel.charts.DataSources;
import org.apache.poi.ss.usermodel.charts.LegendPosition;
import org.apache.poi.ss.usermodel.charts.ScatterChartSeries;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.charts.XSSFChartLegend;
import org.apache.poi.xssf.usermodel.charts.XSSFScatterChartData;
import org.apache.poi.xssf.usermodel.charts.XSSFValueAxis;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTMarkerStyle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTitle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.STMarkerStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNoFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;


public class AnalyzeBurkhard extends Parse {
	
	public AnalyzeBurkhard() {
		sourceName = "Burkhard_medians";
		generateOriginalJSONRecords = false;
		this.init();

		fileNameJSON_Records = sourceName +" Toxicity Original Records.json";
		fileNameFlatExperimentalRecords = sourceName +" Toxicity Experimental Records.txt";
		fileNameFlatExperimentalRecordsBad = sourceName +" Toxicity Experimental Records-Bad.txt";
		fileNameJsonExperimentalRecords = sourceName +" Toxicity Experimental Records.json";
		fileNameJsonExperimentalRecordsBad = sourceName +" Toxicity Experimental Records-Bad.json";
		fileNameExcelExperimentalRecords = sourceName +" Toxicity Experimental Records.xlsx";

	}

	
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsOriginal=new ExperimentalRecords();
		Gson gson=null;
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues();
		gson = builder.create();
		try {
			String jsonFileName = "Data" + File.separator + "Experimental" + File.separator + "Burkhard" + File.separator + "Burkhard Experimental Records.json";
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
				recordsOriginal.add(r);
			}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		
		ExperimentalRecords recordsExperimental = performActionOnER(recordsOriginal);
			return recordsExperimental;
		}	

	
	
	
	
	public static void main(String[] args) {
		
		AnalyzeBurkhard ab = new AnalyzeBurkhard();
		
		ab.createFiles();
		
	}
	
	
	private static ExperimentalRecords performActionOnER(ExperimentalRecords er) {
		MultiMap<String, ExperimentalRecord> multimap = new MultiMap();
		for (int i = 0; i < er.size(); i++) {
			multimap.put(er.get(i).dsstox_substance_id, er.get(i));
		}
		
		MultiMap<String,Double> numericalvals = new MultiMap<String,Double>(); 

        for (String dsstox_substance_id: multimap.keySet()) {
			
    		ArrayList<ExperimentalRecord> only1763231 = new ArrayList<>(multimap.get(dsstox_substance_id));
    		
			for (int j = 0; j < only1763231.size(); j++) {


			if (only1763231.get(j).property_name.toLowerCase().contains("logbcf") && only1763231.get(j).keep == true && only1763231.get(j).note.toLowerCase().contains("whole body") && !(only1763231.get(j).dsstox_substance_id.toLowerCase().equals("-"))) {
			numericalvals.put(only1763231.get(j).dsstox_substance_id, only1763231.get(j).property_value_point_estimate_final);
			}
		}
			
        }
       
        Hashtable<String,Double> logbcfsteadystatemedians = findmedianfromMultiMap(numericalvals);
        
        for (String key : logbcfsteadystatemedians.keySet()) {
            System.out.println(key + "\t" + logbcfsteadystatemedians.get(key));
        }

        ExperimentalRecords recordsExperimental = addExperimentalRecords(logbcfsteadystatemedians,multimap);
        
        return recordsExperimental;
	}
	
	
	private static ExperimentalRecords addExperimentalRecords(Hashtable<String,Double> medians, MultiMap<String, ExperimentalRecord> multimap) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
		Date date = new Date();  
		String strDate=formatter.format(date);
		String dayOnly = strDate.substring(0,strDate.indexOf(" "));

		ExperimentalRecords recordsExperimental = new ExperimentalRecords();

		
        for (String key : medians.keySet()) {
        	ExperimentalRecord er = new ExperimentalRecord();
			er.date_accessed = dayOnly;
			er.property_value_point_estimate_final = medians.get(key);
			er.property_value_string = "Median BCF:" + medians.get(key);
			er.dsstox_substance_id = key;
			er.property_name = ExperimentalConstants.strLogBCF;
			er.keep = true;
			er.source_name="Burkhard_medians";

        	
            for (String dsstox_substance_id: multimap.keySet()) {
            	if (dsstox_substance_id.equals(key)) {

            		ArrayList<ExperimentalRecord> only1763231 = new ArrayList<>(multimap.get(dsstox_substance_id));
            		
            		
            		for (int i = 0; i < only1763231.size(); i++) {
            			
            			if (only1763231.get(i).property_name.toLowerCase().contains("logbcf") && only1763231.get(i).keep == true && only1763231.get(i).note.toLowerCase().contains("whole body")) {
            			er.chemical_name =only1763231.get(i).chemical_name;
            			er.casrn = only1763231.get(i).casrn;
            			String originalER = ReflectionToStringBuilder.toString(only1763231.get(i));
            			er.updateNote(originalER);
            			}
            		}
            	}
            	
            	
            }
        System.out.println(ReflectionToStringBuilder.toString(er));
        recordsExperimental.add(er);
        }

        return recordsExperimental;
        
		
	}
	
	private static Hashtable<String,Double> findmedianfromMultiMap(MultiMap<String,Double> m) {
		Hashtable<String,Double> medianValues = new Hashtable<String,Double>();
        for (String key : m.keySet()) {
        	// Collection x = htLogBCFSteadyState.get(key);
        	Double[] arrLogBCFSteadyState = ArrayUtils.toObject(m.get(key).stream().mapToDouble(Double::doubleValue).toArray());
            Double median = medianCal(arrLogBCFSteadyState.length, arrLogBCFSteadyState);
            medianValues.put(key, median);
            
            // can print these for verification
        	// System.out.println("key= "+ key + "value= " + m.get(key));
            // System.out.println(median);
        }
        return medianValues;
	}

	
	private static Double medianCal(int  n, Double in[]) {
		double m=0;
		
		if (in.length == 0) {
			return null;
		}
		else {
		Arrays.sort(in);

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
	 }



}



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

