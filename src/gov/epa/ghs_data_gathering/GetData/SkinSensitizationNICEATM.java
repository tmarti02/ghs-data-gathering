package gov.epa.ghs_data_gathering.GetData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class SkinSensitizationNICEATM {

	/**
	 * Gets the column number for a column header name in the header row
	 * 
	 * @param row
	 * @param name
	 * @return
	 */
	int getColNum(Row row,String name) {
		DataFormatter formatter = new DataFormatter();
		for (int i=0;i<row.getLastCellNum();i++) {
			Cell cell=row.getCell(i);
			String val=formatter.formatCellValue(cell);
			if (val.contentEquals(name)) {
				return i;
			}
	    }
		return -1;
	}
	
	
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
	
	Vector<RecordNICEATM> parseExcel(String filePathExcel) {
		try {

			Vector<RecordNICEATM> records = new Vector<>();
			FileInputStream inputStream = new FileInputStream(new File(filePathExcel));
			DataFormatter formatter = new DataFormatter();

			Workbook workbook = new HSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(0);
			
			Row headerRow=sheet.getRow(0);
			
			int colName=getColNum(headerRow,"Chemical Name");
			int colCASRN=getColNum(headerRow,"CASRN");
			int colMolecularWeight=getColNum(headerRow,"Molecular Weight (g/mol)");
			int colChemical_Class=getColNum(headerRow,"Chemical Class");
			
			int colLLNA_Vehicle=getColNum(headerRow,"LLNA Vehicle");
			int colEC3=getColNum(headerRow,"EC3 (%)");
			int colLLNA_Result=getColNum(headerRow," LLNA Result");
			int colReference=getColNum(headerRow,"Reference");
			
//			System.out.println(colName);

			int rows=sheet.getLastRowNum();
			
			for (int i=1;i<rows;i++) {
			
				Row row=sheet.getRow(i);
				
				RecordNICEATM r=new RecordNICEATM();
				
				if (row.getCell(colName)==null) {
					break;
				}
				
				r.Chemical_Name=getValue(formatter, colName, row);
				r.CASRN=getValue(formatter, colCASRN, row);
				r.Molecular_Weight=getValue(formatter, colMolecularWeight, row);
				r.Chemical_Class=getValue(formatter, colChemical_Class, row);
				
				r.LLNA_Vehicle=getValue(formatter, colLLNA_Vehicle, row);
				r.EC3=getValue(formatter, colEC3, row);
				r.LLNA_Result=getValue(formatter, colLLNA_Result, row);
				r.Reference=getValue(formatter, colReference, row);
				
				
				records.add(r);
			}
			inputStream.close();
			workbook.close();
			return records;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
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
	
	private String getValue(DataFormatter formatter, int colName, Row row) {
		String val=formatter.formatCellValue(row.getCell(colName)).strip();
		val=val.replace("\n", "; ");
		return val;
	}
	
	public static void main(String[] args) {
		SkinSensitizationNICEATM s=new SkinSensitizationNICEATM();
		
		String folder="AA Dashboard\\Data\\NICEATM\\";
		String filepathExcel=folder+"niceatm-llnadatabase-23dec2013.xls";
		Vector<RecordNICEATM>records=s.parseExcel(filepathExcel);
		
		Hashtable<String,Vector<RecordNICEATM>>ht=s.getHashtable(records);
		s.goThroughHashtable(ht,folder+"niceatm_flat.txt");
		
//		String filepathText=folder+"niceatm.txt";
//		s.writeToFile(filepathText, records);

	}

}
