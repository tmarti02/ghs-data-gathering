package gov.epa.exp_data_gathering.parse.Burkhard2;

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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.Burkhard2.RecordBurkhard2;

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

	
	private static Map<String, Object[]> notmain(String field1Name, String field2Name, String experimentalfeature1, String experimentalfeature2) {
        MultiMap<String, ExperimentalRecord> multimap = new MultiMap();
        
		ExperimentalRecords er = goThroughExperimentalRecords();
		

		// groups experimental records by casrn
		for (int i = 0; i < er.size(); i++) {
			multimap.put(er.get(i).casrn, er.get(i));
		}
		
		MultiMap<String,Double> htLogBCFSteadyState = new MultiMap<String,Double>(); 
		MultiMap<String,Double> htLogBCFKinetic = new MultiMap<String,Double>(); 

		
        for (String casrn: multimap.keySet()) {
        	
    		ArrayList<ExperimentalRecord> only1763231 = new ArrayList<>(multimap.get(casrn));
    		
			for (int j = 0; j < only1763231.size(); j++) {
				
				
				try {
					Field field1 = ExperimentalRecord.class.getDeclaredField(field1Name);
					Field field2 = ExperimentalRecord.class.getDeclaredField(field2Name);

			        // Object o = field1.get(only1763231.get(j));
					// System.out.println(o);
					
					if (field1.get(only1763231.get(j)).toString().contains(experimentalfeature1) && only1763231.get(j).keep == true) {
						htLogBCFSteadyState.put(only1763231.get(j).casrn, only1763231.get(j).property_value_point_estimate_final);
					} else if (field2.get(only1763231.get(j)).toString().contains(experimentalfeature2) && only1763231.get(j).keep == true) {
						htLogBCFKinetic.put(only1763231.get(j).casrn, only1763231.get(j).property_value_point_estimate_final);
					}
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}		
        }
        
        
        Hashtable<String,Double> logbcfsteadystatemedians = findmedianfromMultiMap(htLogBCFSteadyState);
        Hashtable<String,Double> logbcfkineticmedians = findmedianfromMultiMap(htLogBCFKinetic);
        
        
        Map<String, Object[]> data = new TreeMap<String, Object[]>();
        data.put("1", new Object[]{ "CAS", experimentalfeature1, experimentalfeature2, "Difference"});
               
        
        
        for (Map.Entry<String, Double> htEntries : logbcfsteadystatemedians.entrySet()) {
            if(logbcfkineticmedians.containsKey(htEntries.getKey())) { 
            	
                data.put(htEntries.getKey(), new Object[]{htEntries.getKey() ,htEntries.getValue(), logbcfkineticmedians.get(htEntries.getKey()),  htEntries.getValue() - logbcfkineticmedians.get(htEntries.getKey())});
                
            	// System.out.println("\tBCFKey: " + htEntries.getKey() + " BCFsteadystateValue: " + htEntries.getValue());
                // System.out.println("\tKineticKey: " + htEntries.getKey() +" KineticValue: " + logbcfkineticmedians.get(htEntries.getKey()));
                
            }
        }
        
        System.out.println(data);

        return data;
        
       
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

	
	public static void notmain2() {
		
		Map<String, Object[]> data = notmain("property_name","property_name", "LogBCFKinetic","LogBCFSteadyState");
		
		Map<String, Object[]> data2 = notmain("property_name","property_name","LogBCFKinetic","LogBAF");
		

		
        XSSFWorkbook workbook = new XSSFWorkbook();
        
		
        XSSFSheet sheet1 = buildworksheet(workbook, data, "LogBCFKinetic", "LogBCFSteadyState");
        XSSFSheet sheet2 = buildworksheet(workbook, data2, "LogBCFKinetic", "LogBAF");
		
		
		GenerateChart(sheet1,"LogBCFKinetic","LogBCFSteadyState","property","units");
		GenerateChart(sheet2,"LogBCFKinetic","LogBAF","property","units");
		
        try {
            // this Writes the workbook gfgcontribute
            FileOutputStream out = new FileOutputStream(new File("C:\\Users\\Weeb\\Documents\\refreshedproject\\data\\experimental\\Burkhard2" + File.separator + "Burkhard2Analysis.xlsx"));
            workbook.write(out);
            out.close();
            System.out.println("xlsx file written successfully on disk.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
		
	}
	
	
	private static XSSFSheet buildworksheet(XSSFWorkbook ws, Map<String, Object[]> data, String property1, String property2) {
        // Create a blank sheet
        XSSFSheet sheet = ws.createSheet(property1 + property2);
        Set<String> keyset = data.keySet();
        int rownum = 0;
        
        int lastrow = 0;
        
        for (String key : keyset) {
            // this creates a new row in the sheet
            Row row = sheet.createRow(rownum++);
            lastrow++;// determines what the last row is
            Object[] objArr = data.get(key);
            int cellnum = 0;
            for (Object obj : objArr) {
                // this line creates a cell in the next column of that row
                Cell cell = row.createCell(cellnum++);
                if (obj instanceof String)
                    cell.setCellValue((String)obj);
                else if (obj instanceof Integer)
                    cell.setCellValue((Integer)obj);
                else if (obj instanceof Double)
                	cell.setCellValue((Double)obj);
            }
        }
        String formula = "TTEST(B2:B" + String.valueOf(lastrow) + ",C2:C" + String.valueOf(lastrow) + ",2,1)";
        Row analysisrow = sheet.createRow(rownum++);
        int cellnum = 0;
        Cell flavortext = analysisrow.createCell(cellnum++);
        flavortext.setCellValue((String)"TTEST");
        Cell ttest = analysisrow.createCell(cellnum++);
        ttest.setCellType(HSSFCell.CELL_TYPE_FORMULA);
        ttest.setCellFormula(formula);
        
        return sheet;

	}

	
	public static void main(String[] args) {
	AnalyzeBurkhard2 abh2 = new AnalyzeBurkhard2();
	abh2.notmain2();
	
	}
	
	public static void GenerateChart(XSSFSheet sheet,String source1,String source2,String property,String units) {
	    XSSFDrawing drawing = sheet.createDrawingPatriarch();
	    XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 10, 0, 20, 30);

	    XSSFChart chart = drawing.createChart(anchor);

	    if (chart instanceof XSSFChart) ((XSSFChart)chart).setTitleText(property+": "+source1+" vs. "+source2);


	    XSSFValueAxis bottomAxis = chart.createValueAxis(AxisPosition.BOTTOM);
	    XSSFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
	    leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);	    
	    
	    CellRangeAddress crXData = new CellRangeAddress(1, sheet.getLastRowNum() - 1, 1, 1);
	    CellRangeAddress crYData = new CellRangeAddress(1, sheet.getLastRowNum(), 2, 2);
	    CellReference crTitle = new CellReference(0,1);
//	    Cell cell = sheet.getRow(crTitle.getRow()).getCell(crTitle.getCol());

	    ChartDataSource<Number> dsXData = DataSources.fromNumericCellRange(sheet, crXData);
	    ChartDataSource<Number> dsYData = DataSources.fromNumericCellRange(sheet, crYData);

	    XSSFScatterChartData data = chart.getChartDataFactory().createScatterChartData();

	    ScatterChartSeries series1 = data.addSerie(dsXData, dsYData);
	    ScatterChartSeries series2 = data.addSerie(dsXData, dsXData);
	        
	    series1.setTitle("Exp. Data");
	    series2.setTitle("Y=X");
	    chart.plot(data, bottomAxis, leftAxis);

	    //Set axis titles:
	    CTValAx valAx = chart.getCTChart().getPlotArea().getValAxArray(0);
	    CTValAx valAy = chart.getCTChart().getPlotArea().getValAxArray(1);
	    setAxisTitle(source1+" "+units, valAx);
	    setAxisTitle(source2+" "+units, valAy);
	    
	    
	    //set properties of first scatter chart data series to not smooth the line:
	    ((XSSFChart)chart).getCTChart().getPlotArea().getScatterChartArray(0).getSerArray(0)
	     .addNewSmooth().setVal(false);
	    	    
//	    System.out.println(chart.getCTChart().getPlotArea().getScatterChartArray(0).getSerArray(0).getSpPr());
	    
	   //Set series line to no fill:		
		chart.getCTChart().getPlotArea().getScatterChartArray(0).getSerArray(0)
	    .addNewSpPr().addNewLn().addNewNoFill();
	
		chart.getCTChart().getPlotArea().getScatterChartArray(0).getSerArray(1)
	    .addNewMarker().addNewSymbol().setVal(STMarkerStyle.NONE);

		
		//Add linear trend line:
		chart.getCTChart().getPlotArea().getScatterChartArray(0).getSerArray(0)
		 .addNewTrendline()
		 .addNewTrendlineType()
		 .setVal(org.openxmlformats.schemas.drawingml.x2006.chart.STTrendlineType.LINEAR);
		
		
	    XSSFChartLegend legend = chart.getOrCreateLegend();
	    legend.setPosition(LegendPosition.BOTTOM);

	    //set properties of first scatter chart series to not vary the colors:
	    ((XSSFChart)chart).getCTChart().getPlotArea().getScatterChartArray(0)
	     .addNewVaryColors().setVal(false);
	    
	    

	}

	
	private static void setAxisTitle(String source1, CTValAx valAx) {
		CTTitle ctTitle = valAx.addNewTitle();
	    ctTitle.addNewLayout();
	    ctTitle.addNewOverlay().setVal(false);
	    CTTextBody rich = ctTitle.addNewTx().addNewRich();
	    rich.addNewBodyPr();
	    rich.addNewLstStyle();
	    CTTextParagraph p = rich.addNewP();
	    p.addNewPPr().addNewDefRPr();
	    p.addNewR().setT(source1);
	    p.addNewEndParaRPr();
	}

	
	
	
}

		
		
		/*
		// String string1763231 = "196859-54-8";
		
		
		ArrayList<ExperimentalRecord> only1763231LogBCFSteadyState = new ArrayList<>();
		ArrayList<ExperimentalRecord> only1763231LogBAF = new ArrayList<>();
		ArrayList<ExperimentalRecord> only1763231LogBCFKinetic = new ArrayList<>();

		
		ArrayList<Double> pointestimatesLogBAF = new ArrayList<>();
		ArrayList<Double> pointestimatesLogBCFSteadyState = new ArrayList<>();
		ArrayList<Double> pointestimatesLogBCFKinetic = new ArrayList<>();

		
		Map<String,Double> particularLogBCFSteadyState = new HashMap<String,Double>(); 
		Map<String,Double> particularLogBAF = new HashMap<String,Double>(); 
		Map<String,Double> particularLogBCFKinetic = new HashMap<String,Double>(); 

		
		
        for (String casrn: multimap.keySet()) {
            // System.out.println(casrn + ": " + multimap.get(casrn));
    		ArrayList<ExperimentalRecord> only1763231 = new ArrayList<>(multimap.get(casrn));

        	
			for (int j = 0; j < only1763231.size(); j++) {
			if (only1763231.get(j).property_name.equals("LogBCFSteadyState") && only1763231.get(j).keep == true) {
				only1763231LogBCFSteadyState.add(only1763231.get(j));			
				} else if (only1763231.get(j).property_name.equals("LogBAF") && only1763231.get(j).keep == true) {
				only1763231LogBAF.add(only1763231.get(j));
				} else if (only1763231.get(j).property_name.equals("LogBCFKinetic") && only1763231.get(j).keep == true) {
				only1763231LogBCFKinetic.add(only1763231.get(j));
				}
			}
        }
			for (int l = 0; l < only1763231LogBCFSteadyState.size(); l++) {
				particularLogBCFSteadyState.put(only1763231LogBCFSteadyState.get(l).casrn,only1763231LogBCFSteadyState.get(l).property_value_point_estimate_final);
				}
			for (int m = 0; m < only1763231LogBAF.size(); m++) {
				particularLogBAF.put(only1763231LogBAF.get(m).casrn, only1763231LogBAF.get(m).property_value_point_estimate_final);
				}
			for (int l = 0; l < only1763231LogBCFKinetic.size(); l++) {
				particularLogBCFKinetic.put(only1763231LogBCFKinetic.get(l).property_value_point_estimate_final,only1763231LogBCFKinetic.get(l).property_value_point_estimate_final);
				}

	        Double[] arrLogBCFSteadyState = ArrayUtils.toObject(pointestimatesLogBCFSteadyState.stream().mapToDouble(Double::doubleValue).toArray());
	        Double [] arrLogBAF = ArrayUtils.toObject(pointestimatesLogBAF.stream().mapToDouble(Double::doubleValue).toArray());
	        Double [] arrLogBCFKinetic = ArrayUtils.toObject(pointestimatesLogBCFKinetic.stream().mapToDouble(Double::doubleValue).toArray());

			
			Double medianLogBCFSteadyState = medianCal(only1763231LogBCFSteadyState.size(), arrLogBCFSteadyState);
			Double medianLogBAF = medianCal(only1763231LogBAF.size(),arrLogBAF);
			Double medianLogLogBCFKinetic = medianCal(only1763231LogBCFKinetic.size(),arrLogBCFKinetic);
			
			/*
		    if (medianLogBCFSteadyState != null)
			htLogBCFSteadyState.put(er.get(i).casrn,medianLogBCFSteadyState);
			if (medianLogLogBCFKinetic != null)
			htLogBAF.put(er.get(i).casrn, medianLogLogBCFKinetic);
			if (medianLogBAF != null)
			htLogBCFKinetic.put(er.get(i).casrn, medianLogBAF);
			*/
        
/*

		
		
		
        for (String key : htLogBCFSteadyState.keySet()) {
            // System.out.println("key= "+ key + "value= " + htLogBCFSteadyState.get(key));
        }
        
        
        for (Map.Entry<String, Double> htEntries : htLogBCFSteadyState.entrySet()) {
            if(htLogBCFKinetic.containsKey(htEntries.getKey())) { // && htLogBCFKinetic.get(htEntries.getKey()).equals(htEntries.getValue())){
                System.out.println("\tBCFKey: " + htEntries.getKey() + " BCFsteadystateValue: " + htEntries.getValue());
                System.out.println("\tKineticKey: " + htEntries.getKey() +" KineticValue: " + htLogBCFKinetic.get(htEntries.getKey()));
            }
        }
        
        
        // System.out.println(htLogBCFSteadyState);
        // System.out.println(htLogBAF);
        // System.out.println(htLogBCFKinetic);
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
	
	
	private void compare(Workbook wb,String property,String source1, String source2, RecordsQSAR recordsQSAR,
			Hashtable<String, RecordDashboard> htDashboard) {
		
		
		RecordsQSAR recordsQSAR1=new RecordsQSAR();
		Hashtable<String,RecordsQSAR> htRecordsQSAR2=new Hashtable<>();//store Records from source 2 as hashtable with cas as key
		
		for (RecordQSAR rec:recordsQSAR) {
			if (rec.casrn==null) continue;
			
			if (rec.source_name.contentEquals(source1)) recordsQSAR1.add(rec);
			
			if (rec.source_name.contentEquals(source2) && rec.casrn!=null) {
				
				if (htRecordsQSAR2.get(rec.casrn)==null) {
					RecordsQSAR records=new RecordsQSAR();
					records.add(rec);
					htRecordsQSAR2.put(rec.casrn,records);
					
				} else {
					RecordsQSAR records=htRecordsQSAR2.get(rec.casrn);
					records.add(rec);
				}
			}
		}
		
		System.out.println("# records source 1 ="+recordsQSAR1.size());
		System.out.println("# records source 2 ="+htRecordsQSAR2.size());
		
		Vector<Double>xvec=new Vector<>();
		Vector<Double>yvec=new Vector<>();
		Vector<RecordInCommon>recordsInCommon=new Vector<>();
		
		
		for (RecordQSAR rec1:recordsQSAR1) {
			
			if (htRecordsQSAR2.get(rec1.casrn)==null) continue;

			if (htDashboard.get(rec1.casrn)==null) {
				System.out.println("Need dashboard record for\t"+rec1.casrn);
				continue;
			}

			if (htDashboard.get(rec1.casrn).AVERAGE_MASS.contentEquals("-")) {
				System.out.println("Dont have MW for "+rec1.casrn);
				continue;
			}

			rec1.Structure_MolWt=Double.parseDouble(htDashboard.get(rec1.casrn).AVERAGE_MASS);
			rec1.setQSARUnits();
			
			RecordsQSAR recordsQSAR2=htRecordsQSAR2.get(rec1.casrn);
			
			Vector<Double>values2=new Vector<>();

			for (RecordQSAR rec2:recordsQSAR2) {
				rec2.Structure_MolWt=rec1.Structure_MolWt;				
				rec2.setQSARUnits();

				if (!rec2.usable) {
					System.out.println(rec2.casrn+"\t"+rec2.reason);
					continue;
				}
//				if (rec2.property_value_units_qsar.contentEquals(ExperimentalConstants.str_g_L))
					values2.add(Double.valueOf(rec2.property_value_point_estimate_qsar));
			}
			
			if (values2.size()>1) {
				double stdev=calcStdDev(values2);				
				if (stdev>0.5) {
					System.out.println(rec1.casrn+"\t"+stdev+"\t"+values2.size());		
					continue;
				}				
			}
						
			for (RecordQSAR rec2:recordsQSAR2) {
				if (!rec2.usable) continue;			
				xvec.add(rec1.property_value_point_estimate_qsar);
				yvec.add(rec2.property_value_point_estimate_qsar);

				RecordInCommon rec=new RecordInCommon(rec1,rec2);
				recordsInCommon.add(rec);

				//					System.out.println(rec1.casrn+"\t"+rec1.property_value_point_estimate_exp+"\t"+rec2.property_value_point_estimate_exp);
				//					System.out.println(rec1.casrn+"\t"+rec1.property_value_point_estimate_qsar+"\t"+rec2.property_value_point_estimate_qsar+"\t"+rec1.property_value_units_qsar);

			}
		}
		
		if (recordsInCommon.size()==0) return;
		
		Collections.sort(recordsInCommon);
			
		XSSFSheet sheet = addRowsToSpreadsheet(wb, source2, recordsInCommon);
		GenerateChart(sheet,source1,source2,property,recordsInCommon.get(0).rec1.property_value_units_qsar);
		
		
	}

	
	
	

}

// ArrayList<ExperimentalRecord> only1763231 = new ArrayList<>(multimap.get(string1763231));

/*
// for (int k = 0; k < only1763231.size(); k++) {
	ArrayList<ExperimentalRecord> only1763231LogBCFSteadyState = new ArrayList<>();
	ArrayList<ExperimentalRecord> only1763231LogBAF = new ArrayList<>();
	ArrayList<ExperimentalRecord> only1763231LogBCFKinetic = new ArrayList<>();
	for (int j = 0; j < only1763231.size(); j++) {
	if (only1763231.get(j).property_name.equals("LogBCFSteadyState")) {
		only1763231LogBCFSteadyState.add(only1763231.get(j));			
	} else if (only1763231.get(j).property_name.equals("LogBAF")) {
		only1763231LogBAF.add(only1763231.get(j));
	} else if (only1763231.get(j).property_name.equals("LogBCFKinetic")) {
		only1763231LogBCFKinetic.add(only1763231.get(j));
	}
	}
ArrayList<Double> pointestimatesLogBAF = new ArrayList<>();
ArrayList<Double> pointestimatesLogBCFSteadyState = new ArrayList<>();
ArrayList<Double> pointestimatesLogBCFKinetic = new ArrayList<>();

	for (int l = 0; l < only1763231LogBCFSteadyState.size(); l++) {
		pointestimatesLogBCFSteadyState.add(only1763231LogBCFSteadyState.get(l).property_value_point_estimate_final);
		}
	for (int l = 0; l < only1763231LogBAF.size(); l++) {
		pointestimatesLogBAF.add(only1763231LogBAF.get(l).property_value_point_estimate_final);
		}
	for (int l = 0; l < only1763231LogBCFKinetic.size(); l++) {
		pointestimatesLogBCFKinetic.add(only1763231LogBCFKinetic.get(l).property_value_point_estimate_final);
		}

    Double[] arrLogBCFSteadyState = ArrayUtils.toObject(pointestimatesLogBCFSteadyState.stream().mapToDouble(Double::doubleValue).toArray());
    Double [] arrLogBAF = ArrayUtils.toObject(pointestimatesLogBAF.stream().mapToDouble(Double::doubleValue).toArray());
    Double [] arrLogBCFKinetic = ArrayUtils.toObject(pointestimatesLogBCFKinetic.stream().mapToDouble(Double::doubleValue).toArray());

	
	Double medianLogBCFSteadyState = medianCal(only1763231LogBCFSteadyState.size(), arrLogBCFSteadyState);
	Double medianLogBAF = medianCal(only1763231LogBAF.size(),arrLogBAF);
	Double medianLogLogBCFKinetic = medianCal(only1763231LogBCFKinetic.size(),arrLogBCFKinetic);
	
	System.out.println(medianLogBCFSteadyState);
	System.out.println(medianLogLogBCFKinetic);
	System.out.println(medianLogBAF);
	
	if (medianLogBCFSteadyState != null)
	htLogBCFSteadyState.put(string1763231,medianLogBCFSteadyState);
	if (medianLogLogBCFKinetic != null)
	htLogBAF.put(string1763231, medianLogLogBCFKinetic);
	if (medianLogBAF != null)
	htLogBCFKinetic.put(string1763231, medianLogBAF);
// }
	
	*/
	
	
	// System.out.println(only1763231.get(k));
