package gov.epa.ghs_data_gathering.Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseTable_toxval.RecordToxVal;

public class OCHEM_AD_Stats {

	static final String strProbStd="PROB-STD";
	static final String strClassLag="CLASS-LAG";
	static final String strASNN_STDEV="ASNN-STDEV";
	static final String strASNN_CORREL="ASNN-CORREL";
	static final String strBaggingSTD="BAGGING-STD";
					
	class RecordOCHEM {
		String ID;
		String exp;
		String pred;
		Double AD;
	}
	
	
	Vector<RecordOCHEM> getRecords(String filepath,String colNameAD,int sheetNum) {
		
		Vector<RecordOCHEM> records=new Vector<>();
		
		try {
			
			
//			System.out.println(fourth);
			
			FileInputStream fis = new FileInputStream(new File(filepath));
			
			Workbook wb = null;
			
			if (filepath.contains(".xlsx")) wb=new XSSFWorkbook(fis);
			else if (filepath.contains(".xls")) wb=new HSSFWorkbook(fis);
						
			Sheet sheet = wb.getSheetAt(sheetNum);
			
			Vector<String>colNames=new Vector<>();
			Row row = sheet.getRow(0);
			
			for (int i=0;i<row.getLastCellNum();i++) {
				String colName=row.getCell(i).getStringCellValue();
				colNames.add(colName);
//				System.out.println(i+"\t"+colName);
			}
			
			int colNumID=colNames.indexOf("EXTERNALID");
			int colNumExp=-1;
			int colNumPred=-1;
			int colNumAD=-1;
			
			for (int i=0;i<colNames.size();i++) {
				if (colNames.get(i).contains("{measured")) colNumExp=i;
				
				if(colNumPred==-1) 
					if (colNames.get(i).contains("{predicted")) colNumPred=i;
				
				if(colNameAD!=null) 
					if (colNames.get(i).contains(colNameAD)) colNumAD=i;
			}
			
//			System.out.println(colNumExp+"\t"+colNumPred+"\t"+colNumAD);
			
			for (int i=1;i<sheet.getLastRowNum();i++) {
				
				Row rowi = sheet.getRow(i);
				
				RecordOCHEM r=new RecordOCHEM();
				
				r.ID=rowi.getCell(colNumID).getStringCellValue();
				r.exp=rowi.getCell(colNumExp).getStringCellValue();
				r.pred=rowi.getCell(colNumPred).getStringCellValue();
				if(colNameAD!=null) r.AD=rowi.getCell(colNumAD).getNumericCellValue();
				
				if (r.pred.isEmpty())r.AD=99999.0;
				
//				if (r.ID.contentEquals("ADD SIDs"))r.AD=99999.0;
//				*** add SIDs for the chemicals that are too large.***
				
//				I created a boolean for isTooLargeMolecule.
//				This is for molecules that should (but don't always) result in errors
//				in OCHEM because "in sdf too large molecule with na>100".
//				-Leora
				
				if (isTooLargeMolecule(r)) r.AD=99999.0;		

//				System.out.println(i+"\t"+r.ID+"\t"+r.exp+"\t"+r.pred+"\t"+r.AD);
				records.add(r);

			}
			
			if(colNameAD!=null) {
				Comparator<RecordOCHEM> comparator = new RecordComparator();
				Collections.sort(records, comparator);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
		
	}
	
	StatsBinary getStats(String filepath,String colNameAD,double fracTrainingInsideAD) {
		
		try {
						
			Vector<RecordOCHEM> recordsTraining=getRecords(filepath, colNameAD, 0);
			Vector<RecordOCHEM> recordsPrediction=getRecords(filepath, colNameAD, 1);
			
			double AD_at_Frac=9999;
			
			if(colNameAD!=null) AD_at_Frac= getAD_at_frac(fracTrainingInsideAD, recordsTraining);
							
			StatsBinary sb=CalculateBinaryStats(recordsPrediction, AD_at_Frac);
			

			return sb;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		
	}

	 
	 /**
	  * Get the AD value that gives you a certain fraction predicted<br><br>
	  * 
	  * TODO- note this treats chemicals that were too large for OCHEM as chemicals outside the AD.<br>
		Not sure if fraction should just apply to the chemicals that werent too large...		

	  * @param fracTrainingInsideAD
	  * @param recordsTraining
	  * @return
	  */
	private double getAD_at_frac(double fracTrainingInsideAD, Vector<RecordOCHEM> recordsTraining) {

//		for (int i=0;i<recordsTraining.size();i++) {
//			System.out.println(i+"\t"+recordsTraining.get(i).AD);
//		}
		
		int countForFrac=(int)(recordsTraining.size()*fracTrainingInsideAD)-1;			
		return recordsTraining.get(countForFrac).AD;
		
	}
	
	
	class RecordComparator implements Comparator<RecordOCHEM>
	{
	    public int compare(RecordOCHEM o1, RecordOCHEM o2)
	    {
	        return o1.AD.compareTo(o2.AD);
	    }
	}
	
	void getStatsForFolder(String filepathFolder,String colNameAD,double fracTrainingInsideAD,String methodName) {
		
		
		File folder=new File(filepathFolder);
		
		File [] files=folder.listFiles();
		
		DecimalFormat df=new DecimalFormat("0.000");
		
		for (File file:files) {
			if (!file.getName().toLowerCase().contains(".xls")) continue;
			if (!file.getName().toLowerCase().contains("_"+methodName+"_")) continue;
			
//			System.out.println(file.getName()+"\t"+colNameAD);
			
			StatsBinary sb=getStats(file.getAbsolutePath(), colNameAD, fracTrainingInsideAD);
						
			double product=sb.balancedAccuracy*sb.coverage;
						
			System.out.println(file.getName()+"\t"+colNameAD+"\t"+fracTrainingInsideAD+"\t"+df.format(sb.balancedAccuracy)+"\t"+df.format(sb.coverage)+"\t"+df.format(product));
			

		}
		
	}
	
// Creating a boolean for large molecules with na>100.  -Leora	
	public static boolean isTooLargeMolecule (RecordOCHEM r) {
		String dtxsid=r.ID;
		if (dtxsid.contentEquals("DTXSID701019687") || 
			dtxsid.contentEquals("DTXSID701014651")  || 
			dtxsid.contentEquals("DTXSID801019523")  || 
			dtxsid.contentEquals("DTXSID6036205")  || 
			dtxsid.contentEquals("DTXSID50888572")  || 
			dtxsid.contentEquals("DTXSID4070037")  || 
			dtxsid.contentEquals("DTXSID601019622")  || 
			dtxsid.contentEquals("DTXSID101019560")  || 
			dtxsid.contentEquals("DTXSID5066794")  || 
			dtxsid.contentEquals("DTXSID2068474")  || 
			dtxsid.contentEquals("DTXSID8030760")  || 
			dtxsid.contentEquals("DTXSID501019398")  || 
			dtxsid.contentEquals("DTXSID801017622")  || 
			dtxsid.contentEquals("DTXSID60240602")  || 
			dtxsid.contentEquals("DTXSID2060125")  || 
			dtxsid.contentEquals("DTXSID3022829")  || 
			dtxsid.contentEquals("DTXSID60893197")  || 
			dtxsid.contentEquals("DTXSID1065508")  || 
			dtxsid.contentEquals("DTXSID401015105")  || 
			dtxsid.contentEquals("DTXSID7041706")  || 
			dtxsid.contentEquals("DTXSID50889948")  || 
			dtxsid.contentEquals("DTXSID101019508")) {
			return true;
		} else {
			return false;
		}	
	}
	
	
	
	
	
	public static void main(String[] args) {
		
		OCHEM_AD_Stats o=new OCHEM_AD_Stats();
		
		String folderPath="OCHEM";
				
		System.out.println("file\tAD_Name\tFrac training\tBA prediction set\tCoverage prediction set\tProduct");
							
//		String [] adnames= {strProbStd,strClassLag,strASNN_CORREL,strASNN_STDEV, strBaggingSTD};
//		String method="asnn";
//		
//		o.getStatsForFolder(folderPath,null, 1.0,method);
//		
//		for (String adname:adnames) {
//			o.getStatsForFolder(folderPath,adname, 0.95,method);	
//		}
//				
//		for (String adname:adnames) {
//			o.getStatsForFolder(folderPath,adname, 1.0,method);	
//		}
//					
//		o.getStatsForFolder(folderPath, OCHEM_AD_Stats.strClassLag, 0.95,"xgboost");
//		o.getStatsForFolder(folderPath, OCHEM_AD_Stats.strClassLag, 1.0,"xgboost");
//		o.getStatsForFolder(folderPath, null, 1.0,"xgboost");//if dont have AD use null
				
		o.getStatsForFolder(folderPath, OCHEM_AD_Stats.strBaggingSTD, 0.95,"knn");
		o.getStatsForFolder(folderPath, OCHEM_AD_Stats.strBaggingSTD, 1.0,"knn");
		o.getStatsForFolder(folderPath, null, 1.0,"knn");//if dont have AD use null
			
	}
	
	public class StatsBinary {
		
		public double coverage;
		public double concordance;
		public double posConcordance;
		public double negConcordance;
		public int posPredcount;
		public int negPredcount;
		public double balancedAccuracy;
		
	}
	
	public StatsBinary CalculateBinaryStats(Vector<RecordOCHEM>records,double ADcutoff) {		
//		double cutoff=30; //cutoff for deciding if value = C or NC
		
		StatsBinary sc=new StatsBinary();
		
		try {
			
			
			int predcount=0;
			int totalcount=0;
			sc.concordance=0;
			
			sc.posPredcount=0;
			sc.negPredcount=0;
			
			sc.posConcordance=0;
			sc.negConcordance=0;

			for (int i=0;i<records.size();i++) {
				
				RecordOCHEM r=records.get(i);
				

				boolean haveExp=false;
				boolean havePred=false;
				
				if (r.exp.contentEquals("N") || r.exp.contentEquals("P")) {
					totalcount++;
					haveExp=true;
				}
				
				if (r.pred.contentEquals("N") || r.pred.contentEquals("P")) {
					havePred=true;
				}

				
				
				if (havePred ) {

					if ((r.AD!=null && r.AD<=ADcutoff) || r.AD==null)  {

						if (haveExp) {						
							predcount++;
							if (r.exp.contentEquals("P")) sc.posPredcount++;
							else if (r.exp.contentEquals("N")) sc.negPredcount++;

							if (r.exp.contentEquals(r.pred)) {
								sc.concordance++;

								if (r.exp.contentEquals("P")) sc.posConcordance++;
								else if (r.exp.contentEquals("N")) sc.negConcordance++;

							}

						}

					} 
				}
				
				
			}
									
			sc.coverage=(double)predcount/(double)totalcount;
			sc.concordance/=(double)predcount;			
			sc.posConcordance/=(double)sc.posPredcount;
			sc.negConcordance/=(double)sc.negPredcount;
			sc.balancedAccuracy=(sc.posConcordance+sc.negConcordance)/2.0;
								
					
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sc;
		
	}

}
