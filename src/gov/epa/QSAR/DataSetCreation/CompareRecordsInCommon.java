package gov.epa.QSAR.DataSetCreation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

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

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;

public class CompareRecordsInCommon {
	
	public static final String databasePathExperimentalRecords = "data/experimental/ExperimentalRecords.db";
	
	
	private void comparePropertyRecords(String property, String dbpath) {

		String source1=ExperimentalConstants.strSourceEpisuite;//change to EPISUITE later
		
		boolean writeExcel=true;		
		String folder = "Data\\DataSets\\" + property + "\\";

				
		ExperimentalRecords recordsDB = ConvertExperimentalRecordsToDataSet.getExperimentalRecordsFromDB(property, dbpath);

		RecordsQSAR recordsQSAR = recordsDB.getValidQSARRecords();
		
		Vector<String>sources=new Vector<>();
				
		for (RecordQSAR rec:recordsQSAR) {
			if (!sources.contains(rec.source_name)) {
				sources.add(rec.source_name);
				System.out.println(rec.source_name);
			}
				
		}
		
		System.out.println("#Valid records="+recordsQSAR.size());
							
		Vector<RecordDashboard> recordsDashboard = RecordDashboard
				.getRecords(folder + property +" dashboard search by CAS.xls");

		Hashtable<String,RecordDashboard>htDashboard=new Hashtable<>();
		
		for (RecordDashboard rec:recordsDashboard) {
			htDashboard.put(rec.CASRN, rec);
		}

		Workbook wb = new XSSFWorkbook();
		
		
		for (String source2:sources) {
			if (!source2.contentEquals(source1)) {				
				compare(wb,property,source1, source2, recordsQSAR, htDashboard);				
			}
		}
		
//		String source2=ExperimentalConstants.strSourceADDoPT;//change to EPISUITE later
//		compare(wb,source1, source2, recordsQSAR, htDashboard);
		
		try {			
			OutputStream fos = new FileOutputStream(folder+property+"_compare_sources.xlsx");
			wb.write(fos);
			wb.close();
		} catch (Exception ex) {
			ex.printStackTrace();
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

	double calcStdDev(Vector<Double>input) {
	
		double sum=0;
		double mean;
		
		int n=input.size();
		
		for(int i=0;i<n;i++) sum=sum+input.get(i);
		
		mean=sum/(double)n;
//       	System.out.println("Mean :"+mean);
       	
		sum=0;  
       	for(int i=0;i<n;i++) sum+=Math.pow((input.get(i)-mean),2);	
       	
       	mean=sum/(n-1);
       	double deviation=Math.sqrt(mean);
       	return deviation;
	}
	
	
	private XSSFSheet addRowsToSpreadsheet(Workbook wb, String source2, Vector<RecordInCommon> recordsInCommon) {
		Class clazz = RecordQSAR.class;

		XSSFSheet sheet = (XSSFSheet) wb.createSheet(source2);

		Row row0=sheet.createRow(0);
		int jj=0;
		row0.createCell(jj++).setCellValue("casrn");
		row0.createCell(jj++).setCellValue("diff");
		row0.createCell(jj++).setCellValue("rec1.property_value_point_estimate_qsar");
		row0.createCell(jj++).setCellValue("rec2.property_value_point_estimate_qsar");
		row0.createCell(jj++).setCellValue("rec1.property_value_units_qsar");			
		row0.createCell(jj++).setCellValue("rec1.property_value_string");
		row0.createCell(jj++).setCellValue("rec2.property_value_string");
		row0.createCell(jj++).setCellValue("rec1.temperature_C");
		row0.createCell(jj++).setCellValue("rec2.temperature_C");
		row0.createCell(jj++).setCellValue("rec2.pH");
		row0.createCell(jj++).setCellValue("rec2.original_source_name");
		row0.createCell(jj++).setCellValue("rec1.chemical_name");
		row0.createCell(jj++).setCellValue("rec2.chemical_name");
		row0.createCell(jj++).setCellValue("rec2.url");
		
		for (int i=0;i < recordsInCommon.size();i++) {
			Row row=sheet.createRow(i+1);			
			RecordInCommon rec=recordsInCommon.get(i);			
			int j=0;			
			row.createCell(j++).setCellValue(rec.rec1.casrn);
			row.createCell(j++).setCellValue(rec.diff);
			row.createCell(j++).setCellValue(rec.rec1.property_value_point_estimate_qsar);
			row.createCell(j++).setCellValue(rec.rec2.property_value_point_estimate_qsar);
			row.createCell(j++).setCellValue(rec.rec1.property_value_units_qsar);			
			row.createCell(j++).setCellValue(rec.rec1.property_value_string);
			row.createCell(j++).setCellValue(rec.rec2.property_value_string);
			row.createCell(j++).setCellValue(rec.rec1.temperature_C+"");
			row.createCell(j++).setCellValue(rec.rec2.temperature_C+"");
			row.createCell(j++).setCellValue(rec.rec2.pH+"");
			row.createCell(j++).setCellValue(rec.rec2.original_source_name+"");
			row.createCell(j++).setCellValue(rec.rec1.chemical_name+"");
			row.createCell(j++).setCellValue(rec.rec2.chemical_name+"");
//			row.createCell(j++).setCellValue(rec.rec2.url+"");
			row.createCell(j++).setCellFormula("HYPERLINK(\""+rec.rec2.url+"\")");
		}
		return sheet;
	}
	
	
	public void GenerateChart(XSSFSheet sheet,String source1,String source2,String property,String units) {
	    XSSFDrawing drawing = sheet.createDrawingPatriarch();
	    XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 10, 0, 20, 30);

	    XSSFChart chart = drawing.createChart(anchor);

	    if (chart instanceof XSSFChart) ((XSSFChart)chart).setTitleText(property+": "+source1+" vs. "+source2);


	    XSSFValueAxis bottomAxis = chart.createValueAxis(AxisPosition.BOTTOM);
	    XSSFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
	    leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);	    
	    
	    CellRangeAddress crXData = new CellRangeAddress(1, sheet.getLastRowNum(), 2, 2);
	    CellRangeAddress crYData = new CellRangeAddress(1, sheet.getLastRowNum(), 3, 3);
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
//	showChart(source1, source2, xvec, yvec);


	private void setAxisTitle(String source1, CTValAx valAx) {
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

	
	void runWS() {

		String property = ExperimentalConstants.strWaterSolubility;
		String dbpath = databasePathExperimentalRecords;
		comparePropertyRecords(property, dbpath);
	}
	
	
	class RecordInCommon implements Comparable< RecordInCommon > { 
		
		RecordQSAR rec1;
		RecordQSAR rec2;
		Double diff;
	
		RecordInCommon (RecordQSAR rec1, RecordQSAR rec2){
			this.rec1=rec1;
			this.rec2=rec2;
			this.diff=Math.abs(rec1.property_value_point_estimate_qsar-rec2.property_value_point_estimate_qsar);
		}

		@Override
		public int compareTo(RecordInCommon o) {
			// TODO Auto-generated method stub
			return -this.diff.compareTo(o.diff);
		}
	}
	
//	private void showChart(String source1, String source2, Vector<Double> xvec, Vector<Double> yvec) {
//		double [] x=new double[xvec.size()];
//		double [] y=new double[yvec.size()];
//	
//		for (int i=0;i<xvec.size();i++) {
//			x[i]=xvec.get(i);
//			y[i]=yvec.get(i);
////			System.out.println(i+"\t"+x[i]+"\t"+y[i]);
//		}
//		String title=source1+" vs "+source2;
//		String xtitle=source1;
//		String ytitle=source2;
//		fraChart fc = new fraChart(x,y,xtitle,ytitle);
//		fc.jlChart.doDrawLegend=false;
//		fc.jlChart.doDrawStatsMAE=false;
//		fc.setVisible(true);
//	}
	
	public static void main(String[] args) {
		CompareRecordsInCommon c=new CompareRecordsInCommon();
		c.runWS();
	}
	
}


