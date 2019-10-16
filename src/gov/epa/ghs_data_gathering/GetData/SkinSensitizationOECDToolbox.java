package gov.epa.ghs_data_gathering.GetData;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.DataFormatter;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;

public class SkinSensitizationOECDToolbox {
	DataFormatter formatter = new DataFormatter();
	
	/**
	 * Gets the column number for a column header name in the header row
	 * 
	 * @param row
	 * @param name
	 * @return
	 */
	int getColNum(XSSFRow row,String name) {
		DataFormatter formatter = new DataFormatter();
		for (int i=0;i<row.getLastCellNum();i++) {
			XSSFCell cell=row.getCell(i);
			String val=formatter.formatCellValue(cell);
			if (val.contentEquals(name)) {
				return i;
			}
	    }
		return -1;
	}
	
	// I tried to rename "RecordNICEATM" to "RecordOECD" and I got error messages so I undid.
	
	void writeToFile(String filepath,Vector<RecordNICEATM>records) {
		try {
			
			FileWriter fw=new FileWriter(filepath);
			fw.write(RecordNICEATM.getHeader()+"\r\n");
			
			for (RecordNICEATM r:records) {
				fw.write(r+"\r\n");
			}
			
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	void parseExcel(String filePathExcel2,String outfilepath2) {
		try {

		/*	FileWriter fw=new FileWriter(outfilepath);
			FileWriter fwFlat=new FileWriter(outfilepathFlat);*/
			
		/*	fw.write("ChemicalNumber\tCAS\tName\tEC3value\tEC3units\tEC3info\tBinaryLLNA\r\n");
			fwFlat.write("ChemicalNumber\tCAS\tName\tSMILES\tFinalLLNA\r\n");*/
			
			Vector<RecordNICEATM> records = new Vector<RecordNICEATM>();
			
			FileInputStream inputStream = new FileInputStream(new File(filePathExcel2));
			

			XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
			XSSFSheet sheet = workbook.getSheetAt(7);
			
			XSSFRow rowChemicalNumber=sheet.getRow(0);
			XSSFRow rowCAS=sheet.getRow(3);
			XSSFRow rowName=sheet.getRow(4);
			XSSFRow rowSMILES=sheet.getRow(6);
			
//			System.out.println("here");
			
			for (int i=1;i<=1214;i++) {
				
				int col=3*(i-1)+2;
				int col2=3*(i-1)+3;
				int col3=3*(i-1)+4;	
				String chemicalNumber=getStringValue(rowChemicalNumber.getCell(col));
				String CAS=getStringValue(rowCAS.getCell(col));
				String chemicalName=getStringValue(rowName.getCell(col));
				String SMILES=getStringValue(rowSMILES.getCell(col));
				
				
				double totalScore=0;
				double numScores=0;
												
				for (int rowNumber=31;rowNumber<=71;rowNumber++) {
					XSSFRow rowEC3=sheet.getRow(rowNumber);
					String EC3value=getStringValue(rowEC3.getCell(col));	
					String EC3units=getStringValue(rowEC3.getCell(col2));
					String EC3info=getStringValue(rowEC3.getCell(col3)).replace("\n", "; ");
					
					Integer LLNAScore=null;
					
					if (!EC3value.isEmpty()) {
					
						if (EC3value.contentEquals("Negative")) {
							LLNAScore=0;
						} else{
							LLNAScore=1;
						}
						totalScore+=LLNAScore;
						numScores++;
						
						
					/*	fw.write(chemicalNumber+"\t"+CAS+"\t"+chemicalName+"\t"+EC3value+"\t"+EC3units+"\t"+EC3info+"\t"+LLNAScore+"\n");
						fw.flush(); */
					}
				}
				
				Integer LLNAScoreFinal=null;
				if (numScores>0) {
					double LLNAScoreAvg=totalScore/numScores;
					
					if(LLNAScoreAvg<=0.2) {
						LLNAScoreFinal=0;
					}
					if(LLNAScoreAvg>=0.8) {
						LLNAScoreFinal=1;
					}
					System.out.println(CAS+"\t"+totalScore+"\t"+numScores+"\t"+LLNAScoreAvg+"\t"+LLNAScoreFinal+"\t");
					fwFlat.write(chemicalNumber+"\t"+CAS+"\t"+chemicalName+"\t"+SMILES+"\t"+LLNAScoreFinal+"\n");
				}
				
				//
	
	void parseExcel(String filePathExcel,String outfilepath,String outfilepathFlat) {
		try {

			FileWriter fw=new FileWriter(outfilepath);
			FileWriter fwFlat=new FileWriter(outfilepathFlat);
			
			fw.write("ChemicalNumber\tCAS\tName\tEC3value\tEC3units\tEC3info\tBinaryLLNA\r\n");
			fwFlat.write("ChemicalNumber\tCAS\tName\tSMILES\tFinalLLNA\r\n");
			
			Vector<RecordNICEATM> records = new Vector<RecordNICEATM>();
			
			FileInputStream inputStream = new FileInputStream(new File(filePathExcel));
			

			XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
			XSSFSheet sheet = workbook.getSheetAt(0);
			
			XSSFRow rowChemicalNumber=sheet.getRow(0);
			XSSFRow rowCAS=sheet.getRow(3);
			XSSFRow rowName=sheet.getRow(4);
			XSSFRow rowSMILES=sheet.getRow(6);
			
//			System.out.println("here");
			
			for (int i=1;i<=1214;i++) {
				
				int col=3*(i-1)+2;
				int col2=3*(i-1)+3;
				int col3=3*(i-1)+4;	
				String chemicalNumber=getStringValue(rowChemicalNumber.getCell(col));
				String CAS=getStringValue(rowCAS.getCell(col));
				String chemicalName=getStringValue(rowName.getCell(col));
				String SMILES=getStringValue(rowSMILES.getCell(col));
				
				
				double totalScore=0;
				double numScores=0;
												
				for (int rowNumber=31;rowNumber<=71;rowNumber++) {
					XSSFRow rowEC3=sheet.getRow(rowNumber);
					String EC3value=getStringValue(rowEC3.getCell(col));	
					String EC3units=getStringValue(rowEC3.getCell(col2));
					String EC3info=getStringValue(rowEC3.getCell(col3)).replace("\n", "; ");
					
					Integer LLNAScore=null;
					
					if (!EC3value.isEmpty()) {
					
						if (EC3value.contentEquals("Negative")) {
							LLNAScore=0;
						} else{
							LLNAScore=1;
						}
						totalScore+=LLNAScore;
						numScores++;
						
						
						fw.write(chemicalNumber+"\t"+CAS+"\t"+chemicalName+"\t"+EC3value+"\t"+EC3units+"\t"+EC3info+"\t"+LLNAScore+"\n");
						fw.flush(); 
					}
				}
				
				Integer LLNAScoreFinal=null;
				if (numScores>0) {
					double LLNAScoreAvg=totalScore/numScores;
					
					if(LLNAScoreAvg<=0.2) {
						LLNAScoreFinal=0;
					}
					if(LLNAScoreAvg>=0.8) {
						LLNAScoreFinal=1;
					}
					System.out.println(CAS+"\t"+totalScore+"\t"+numScores+"\t"+LLNAScoreAvg+"\t"+LLNAScoreFinal+"\t");
					fwFlat.write(chemicalNumber+"\t"+CAS+"\t"+chemicalName+"\t"+SMILES+"\t"+LLNAScoreFinal+"\n");
				}
				
				
				
				
				
	//			Integer LLNA = null;
		
/*	for (int i=1;i<=1214;i++) {
				
				int col=3*(i-1)+2;
				int col2=3*(i-1)+3;
				int col3=3*(i-1)+4;	
				String chemicalNumber=getStringValue(rowChemicalNumber.getCell(col));
				String CAS=getStringValue(rowCAS.getCell(col));
				String chemicalName=getStringValue(rowName.getCell(col));
				
				
				for (int rowNumber=31;rowNumber<=71;rowNumber++) {
					XSSFRow rowEC3=sheet.getRow(rowNumber);
					String EC3value=getStringValue(rowEC3.getCell(col));	
					String EC3units=getStringValue(rowEC3.getCell(col2));
					String EC3info=getStringValue(rowEC3.getCell(col3)).replace("\n", "; ");
					
					if (!EC3value.isEmpty()) {
						fw.write(chemicalNumber+"\t"+CAS+"\t"+chemicalName+"\t"+EC3value+"\t"+EC3units+"\t"+EC3info+"\n");
						fw.flush();
					}
				}*/
			
			
				
				
			
				
			
/*	for (int i=1;i<=1214;i++) {
				
				int col=3*(i-1)+2;
				int col2=3*(i-1)+3;
				int col3=3*(i-1)+4;	
				String chemicalNumber=getStringValue(rowChemicalNumber.getCell(col));
				String CAS=getStringValue(rowCAS.getCell(col));
				String chemicalName=getStringValue(rowName.getCell(col));
				
				
				for (int rowNumber=31;rowNumber<=71;rowNumber++) {
					XSSFRow rowEC3=sheet.getRow(rowNumber);
					String EC3value=getStringValue(rowEC3.getCell(col));
					String EC3units=getStringValue(rowEC3.getCell(col2));
					String EC3info=getStringValue(rowEC3.getCell(col3)).replace("\n", "; ");
					
					
				fw.write(chemicalNumber+"\t"+CAS+"\t"+chemicalName+"\t"+EC3value+"\t"+EC3units+"\t"+EC3info+"\n");
					fw.flush();
				}*/
			
								
			
			
			
		/*	for (int i=1;i<=1214;i++) {
				
				int col=3*(i-1)+2;
				int col2=3*(i-1)+3;
				int col3=3*(i-1)+4;	
				String chemicalNumber=getStringValue(rowChemicalNumber.getCell(col));
				String CAS=getStringValue(rowCAS.getCell(col));
				String chemicalName=getStringValue(rowName.getCell(col));
				
				fw.write(chemicalNumber+"\t"+CAS+"\t"+chemicalName+"\t");
				
				for (int rowNumber=31;rowNumber<=32;rowNumber++) {
					XSSFRow rowEC3=sheet.getRow(rowNumber);
					String EC3value=getStringValue(rowEC3.getCell(col));
					String EC3units=getStringValue(rowEC3.getCell(col2));
					String EC3info=getStringValue(rowEC3.getCell(col3)).replace("\n", "; ");
					
					
					fw.write(EC3value+"\t"+EC3units+"\t"+EC3info+"\t");
					fw.flush();
					
				}
				fw.write("\r\n");
								*/
				
			
			}
				
	
			inputStream.close();
			workbook.close();
//			return records;
			fw.close();
			fwFlat.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
//			return null;
		}
		
	}
	
	String getStringValue(XSSFCell cell){
		return formatter.formatCellValue(cell).trim();
		
	}
	

	void goThroughHashtable(Hashtable<String,Vector<RecordNICEATM>>records,String outputFilePath) {
		try {
			FileWriter fw=new FileWriter(outputFilePath);
			
			List<String> keys = new ArrayList<String>(records.keySet());
			Collections.sort(keys);

			double frac=0.8;

			int countOmitted=0;
			int countGood=0;

			boolean printRecords=true;

			fw.write(RecordNICEATM.getHeader2()+"\r\n");

			for(String key: keys){
				//            System.out.println("Value of "+key+" is: "+hm.get(key));
				//        	System.out.println(key);

				Vector<RecordNICEATM>records2=records.get(key);

				int totalScores=0;

				for (RecordNICEATM record:records2) {
					if (printRecords) System.out.println(record);
					if (record.LLNA_Result.contentEquals("POS")) totalScores++;        		
				}

				double dblFinalScore=totalScores/(double)records2.size();        	
				String strFinalScore="";
				int intFinalScore=-1;

				if (dblFinalScore>=frac) {
					strFinalScore="POS";
					intFinalScore=1;
				} else if (dblFinalScore<=(1-frac)) {
					strFinalScore="NEG";
					intFinalScore=0;
				} else {
					strFinalScore="Omit";
				}

				RecordNICEATM recordFinal=new RecordNICEATM();
				recordFinal.LLNA_Result=intFinalScore+"";


				if (!strFinalScore.contentEquals("Omit")) {
					for (RecordNICEATM record:records2) {
						if (record.LLNA_Result.contentEquals(strFinalScore)) {
							//            			ReferenceFinal=record.Reference;
							recordFinal.CASRN=record.CASRN;
							recordFinal.Molecular_Weight=record.Molecular_Weight;
							recordFinal.Chemical_Name=record.Chemical_Name;
							break;
						}
					}

					for (RecordNICEATM record:records2) {
						if (record.LLNA_Result.contentEquals(strFinalScore)) {
							if (recordFinal.Reference==null) recordFinal.Reference=record.Reference;
							else {
								if (!recordFinal.Reference.contains(record.Reference))  {
									recordFinal.Reference+="; "+record.Reference;	
								}

							}
						}
					}

					countGood++;
				} else {
					countOmitted++;
				}
				//        	System.out.println(key+"\t"+CASFinal+"\t"+MolecularWeightFinal+"\t"+finalScore+"\t"+intFinalScore+"\t"+ReferenceFinal);
				//        	System.out.println(key+"\t"+CASFinal+"\t"+MolecularWeightFinal+"\t"+intFinalScore+"\t"+ReferenceFinal);

				if (!strFinalScore.contentEquals("Omit")) {
					System.out.println(recordFinal.toString(RecordNICEATM.varlist2));
					fw.write(recordFinal.toString(RecordNICEATM.varlist2)+"\r\n");
				}
				if (printRecords) System.out.println("");
			}
			System.out.println("Count Omitted="+countOmitted);
			System.out.println("Count good="+countGood);
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Get hashtable which has key=chemical name, value = vector of records for that name
	 * @param records
	 * @return
	 */
	Hashtable<String,Vector<RecordNICEATM>>getHashtable(Vector<RecordNICEATM> records) {
		Hashtable<String,Vector<RecordNICEATM>> ht=new Hashtable<>();
		
		for (RecordNICEATM record:records) {
			
			if (ht.get(record.Chemical_Name)==null) {
				Vector<RecordNICEATM>recordsForName=new Vector<>();
				recordsForName.add(record);
				ht.put(record.Chemical_Name,recordsForName);
			} else {
				Vector<RecordNICEATM>recordsForName=ht.get(record.Chemical_Name);
				recordsForName.add(record);
			}			
		}
		
		return ht;
	}
	
	private String getValue(DataFormatter formatter, int colName, XSSFRow row) {
		String val=formatter.formatCellValue(row.getCell(colName)).trim();
		val=val.replace("\n", "; ");
		return val;
	}
	


	public static void main(String[] args) {
		SkinSensitizationOECDToolbox s=new SkinSensitizationOECDToolbox();
		
		String folder="C:\\Users\\lvegosen\\OneDrive - Environmental Protection Agency (EPA)\\ORISEResearch_LeoraVegosen\\skin_sensitization\\";
		String filepathExcel=folder+"Data matrix_1_8_19__15_52_25.xlsx";
		String filepathExcel2=folder+"literature_data\\OECDToolbox.xlsx";
		String filepathout2=folder+"\\literature_data\\OECDToolboxFlat.txt";
		String filepathout=folder+"Data matrix_1_8_19__15_52_25.txt";
		String filepathoutFlat=folder+"Data matrix_1_8_19__15_52_25_flat.txt";
//		Vector<RecordNICEATM>records=s.parseExcel(filepathExcel);
		s.parseExcel(filepathExcel,filepathout,filepathoutFlat);
		
		
//		Hashtable<String,Vector<RecordNICEATM>>ht=s.getHashtable(records);
//		s.goThroughHashtable(ht,folder+"niceatm_flat.txt");
		
//		String filepathText=folder+"niceatm.txt";
//		s.writeToFile(filepathText, records);

	}

}
