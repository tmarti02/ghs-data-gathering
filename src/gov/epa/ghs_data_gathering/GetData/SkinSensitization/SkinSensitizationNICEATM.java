package gov.epa.ghs_data_gathering.GetData.SkinSensitization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.epa.ghs_data_gathering.GetData.DSSTOX;
import gov.epa.ghs_data_gathering.GetData.RecordChemReg;
import gov.epa.ghs_data_gathering.GetData.RecordDashboard;
import gov.epa.ghs_data_gathering.GetData.RecordTox;
import gov.epa.ghs_data_gathering.Utilities.ExcelUtilities;

public class SkinSensitizationNICEATM {

	
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
			
			int colName=ExcelUtilities.getColNum(headerRow,"Chemical Name");
			int colCASRN=ExcelUtilities.getColNum(headerRow,"CASRN");
			int colMolecularWeight=ExcelUtilities.getColNum(headerRow,"Molecular Weight (g/mol)");
			int colChemical_Class=ExcelUtilities.getColNum(headerRow,"Chemical Class");
			
			int colLLNA_Vehicle=ExcelUtilities.getColNum(headerRow,"LLNA Vehicle");
			int colEC3=ExcelUtilities.getColNum(headerRow,"EC3 (%)");
			int colLLNA_Result=ExcelUtilities.getColNum(headerRow," LLNA Result");
			int colReference=ExcelUtilities.getColNum(headerRow,"Reference");
			
//			System.out.println(colName);

			int rows=sheet.getLastRowNum();
			
			for (int i=1;i<rows;i++) {
			
				Row row=sheet.getRow(i);
				
				RecordNICEATM r=new RecordNICEATM();
				
				if (row.getCell(colName)==null) {
					break;
				}
				
				r.Chemical_Name=ExcelUtilities.getValue(formatter, colName, row);
				r.CASRN=ExcelUtilities.getValue(formatter, colCASRN, row);
				r.Molecular_Weight=ExcelUtilities.getValue(formatter, colMolecularWeight, row);
				r.Chemical_Class=ExcelUtilities.getValue(formatter, colChemical_Class, row);
				
				r.LLNA_Vehicle=ExcelUtilities.getValue(formatter, colLLNA_Vehicle, row);
				r.EC3=ExcelUtilities.getValue(formatter, colEC3, row);
				r.LLNA_Result=ExcelUtilities.getValue(formatter, colLLNA_Result, row);
				r.Reference=ExcelUtilities.getValue(formatter, colReference, row);
				
				
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
	
	Vector<RecordNICEATM> parseExcel2(String filePathExcel) {
		try {

			Vector<RecordNICEATM> records = new Vector<>();
			FileInputStream inputStream = new FileInputStream(new File(filePathExcel));
			DataFormatter formatter = new DataFormatter();

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(0);
			
			Row headerRow=sheet.getRow(0);
			
			int colName=ExcelUtilities.getColNum(headerRow,"Compound name");
			int colCASRN=ExcelUtilities.getColNum(headerRow,"CASRN");
			int colSMILES=ExcelUtilities.getColNum(headerRow,"SMILES");
			int colActivity=ExcelUtilities.getColNum(headerRow,"Activity");
			int colClass=ExcelUtilities.getColNum(headerRow,"Class");
			
			int colEC3=ExcelUtilities.getColNum(headerRow,"EC3 (%)");
			int colMolecularWeight=ExcelUtilities.getColNum(headerRow,"MW");
			int colChemical_Class=ExcelUtilities.getColNum(headerRow,"Chemical Class");
			
			int rows=sheet.getLastRowNum();
			
			for (int i=1;i<rows;i++) {
			
				Row row=sheet.getRow(i);
				
				RecordNICEATM r=new RecordNICEATM();
				
				if (row.getCell(colName)==null) {
					break;
				}
				
				r.Chemical_Name=ExcelUtilities.getValue(formatter, colName, row);
				r.CASRN=ExcelUtilities.getValue(formatter, colCASRN, row);
				r.Molecular_Weight=ExcelUtilities.getValue(formatter, colMolecularWeight, row);
				r.Chemical_Class=ExcelUtilities.getValue(formatter, colChemical_Class, row);
				
				r.EC3=ExcelUtilities.getValue(formatter, colEC3, row);
				r.Class=ExcelUtilities.getValue(formatter, colClass, row);
				r.LLNA_Result=ExcelUtilities.getValue(formatter, colActivity, row);
				r.Smiles=ExcelUtilities.getValue(formatter, colSMILES, row);				
				
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
	
	
//	void goThroughToxRecords(Vector<RecordNICEATM> recordsTox, Vector<RecordChemReg> recordsChemReg,
//			Hashtable<String, RecordDashboard> recordsDashboard,String outputFilePathToxRecords,String outputFilePathToxRecordsOmitted,String outputFilePathBadChemReg) {
//		
//		
//		//****************************************************************************************
//		// Create look up based on name+CAS:
//		Hashtable<String, RecordChemReg> htChemRegCAS_Name = new Hashtable<>();
//
//		// Create look up just based on CAS: (for when names get mangled by saving to
//		// file)
//		Hashtable<String, RecordChemReg> htChemRegCAS = new Hashtable<>();
//
//		for (RecordChemReg recordChemReg : recordsChemReg) {
//			String key = recordChemReg.Query_Casrn + "_" + recordChemReg.Query_Name;
//			htChemRegCAS_Name.put(key, recordChemReg);
//			htChemRegCAS.put(recordChemReg.Query_Casrn, recordChemReg);
////			System.out.println(recordChemReg.Query_Casrn);
//		}
//		//****************************************************************************************
//
//		Hashtable<String, Vector<RecordNICEATM>> htToxData = new Hashtable<>();//store tox records based on SID
//
//		FileWriter fwBadChemReg;
//		try {
//			fwBadChemReg = new FileWriter(outputFilePathBadChemReg);
//			fwBadChemReg.write(RecordNICEATM.getHeader()+"\t"+RecordChemReg.getHeader()+"\r\n");
//
//			for (RecordNICEATM recordTox : recordsTox) {
//
//				String keyCAS_Name = recordTox.CASRN + "_" + recordTox.Chemical_Name.replace("\"", "");
//
//				RecordChemReg recordChemReg = null;
//
//				if (htChemRegCAS_Name.get(keyCAS_Name) == null) {
//					if (htChemRegCAS.get(recordTox.CASRN) != null) {// try looking up just by CAS:
//						recordChemReg = htChemRegCAS.get(recordTox.CASRN);
//					} else {
//						System.out.println(recordTox.CASRN + "\tNotFound");
//					}
//				} else {
//					recordChemReg = htChemRegCAS_Name.get(keyCAS_Name);//look up by key CAS
//				}
//
//				if (recordChemReg == null) {
//					System.out.println(recordTox.CASRN + "\t" + recordTox.Chemical_Name + "\tNo ChemReg!");
//				}
//
//				if (DSSTOX.isChemRegOK(recordChemReg)) {
//
//					if (htToxData.get(recordChemReg.Top_HIT_DSSTox_Substance_Id) == null) {
//						Vector<RecordNICEATM> records = new Vector<>();
//						records.add(recordTox);
//						htToxData.put(recordChemReg.Top_HIT_DSSTox_Substance_Id, records);
//					} else {
//						Vector<RecordNICEATM> records = htToxData.get(recordChemReg.Top_HIT_DSSTox_Substance_Id);
//						records.add(recordTox);
//					}
//
//				} else {
//					fwBadChemReg.write(recordTox+"\t"+recordChemReg+"\r\n");
//					fwBadChemReg.flush();
//					//TODO write out bad chemreg recs...
//				}
//
//
//			} // end loop over tox records
//
//			fwBadChemReg.close();
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		Set<String> setOfSIDS = htToxData.keySet();
//		Iterator<String> iterator = setOfSIDS.iterator();
//
//		//Loop through SIDs for stored tox records:
//		int uniqueChemicalsWithTox=0;
//		
//		try {
//			FileWriter fw1=new FileWriter(outputFilePathToxRecords);
//			FileWriter fw2=new FileWriter(outputFilePathToxRecordsOmitted);
//
//			fw1.write(RecordDashboard.getHeader()+"\tAvgTox\tBinaryTox\r\n");
//			fw2.write(RecordDashboard.getHeader()+"\tAvgTox\tBinaryTox\tOmitReason\r\n");
//			
//			
//			TreeMap<String,String>htInChi=new TreeMap<>();
//			
//			while (iterator.hasNext()) {
//
//				String SID = iterator.next();
//				
//				RecordDashboard recordDashboard = recordsDashboard.get(SID);
//				
//				Vector<RecordNICEATM> records = htToxData.get(SID);
//				
//				double tox=0;
//				int count=0;
//				
//				for (RecordNICEATM record:records) {									
//					if(record.Class.contentEquals("Weak") || record.Class.isEmpty()) {
//						//skip it (Ambiguous)
//					} else if (record.Class.contentEquals("Non-sensitizer")) {
//						count++;
//					} else if (record.Class.contentEquals("Moderate") || record.Class.contentEquals("Extreme") || record.Class.contentEquals("Strong")) {
//						tox+=1;
//						count++;
//					} else {
//						System.out.println(record.Class);
//					}
//				}
//
//				tox/=(double)count;
//
//				int binaryTox=0;
//				if (tox>0.5) binaryTox=1;
//				
//				String omitReason=DSSTOX.getOmitReason(SID, recordsDashboard);
//				
//				if (omitReason.isBlank()) {
//					if (tox>0.2 && tox < 0.8) omitReason="0.2 < Avg Score < 0.8";	
//					if (count==0) omitReason="No non ambiguous records";
//				}
//				
//
//				if (omitReason.isBlank()) {
//					uniqueChemicalsWithTox++;
//					htInChi.put(recordDashboard.INCHIKEY,SID);
//					fw1.write(recordDashboard+"\t"+tox+"\t"+binaryTox+"\r\n");
//					fw1.flush();
//				} else {
//					fw2.write(recordDashboard+"\t"+tox+"\t"+binaryTox+"\t"+omitReason+"\r\n");
//					fw2.flush();					
//				}
//			}
//			
//			
//			System.out.println("uniqueChemicalsWithTox=" + uniqueChemicalsWithTox);
//			fw1.close();
//			fw2.close();
//			
//			//Determine which chemicals are 2d matches- need to omit one from each pair:
//			DSSTOX.find2dMatchesFromInChiKey(recordsDashboard, htInChi);
//
//			
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//	}
	
	public static void main(String[] args) {
		SkinSensitizationNICEATM s=new SkinSensitizationNICEATM();
		
//		String folder="AA Dashboard\\Data\\NICEATM\\";
//		String filepathExcel=folder+"niceatm-llnadatabase-23dec2013.xls";
//		Vector<RecordNICEATM>records=s.parseExcel(filepathExcel);
//		
//		Hashtable<String,Vector<RecordNICEATM>>ht=s.getHashtable(records);
//		s.goThroughHashtable(ht,folder+"niceatm_flat.txt");
//		
////		String filepathText=folder+"niceatm.txt";
////		s.writeToFile(filepathText, records);
//***************************************************************************************************************		
		
		String endpoint="LLNA";
		String source="NICEATM";
		String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2020 skin sensitization paper\\LLNA_NICEATM\\";

//		String filepathExcel=folder+"niceatm-llnadatabase-23dec2013.xls";
//		Vector<RecordNICEATM>recordsTox=s.parseExcel(filepathExcel);

		String filepathExcel=folder+"NICEATM LLNA DB_original.xlsx";
		Vector<RecordNICEATM>recordsTox=s.parseExcel2(filepathExcel);

		
		Vector<RecordChemReg> recordsChemReg = DSSTOX.parseChemRegExcel(folder + "LLNA_NICEATM_ChemReg.xlsx");

		Hashtable<String, RecordDashboard> htDashboard = DSSTOX
				.parseDashboardExcel(folder + "LLNA_NICEATM_Dashboard.xls");
		
		
		Vector<RecordTox> recordsTox2=new Vector<>();
		
		for (RecordNICEATM recordSource:recordsTox) {
			RecordTox recTox=getRecordTox(recordSource);		
			recordsTox2.add(recTox);
		}
		
		DSSTOX.goThroughToxRecords(recordsTox2, recordsChemReg, htDashboard,folder,endpoint,source);


	}
	
	/**
	 * 
	 * Converts to common tox record and assigns binary tox val
	 * weaks are assigned to ambiguous
	 * 
	 * @param recordNICEATM
	 * @return
	 */
	public static RecordTox getRecordToxOld(RecordNICEATM recordNICEATM) {
		RecordTox r=new RecordTox();
		r.CAS=recordNICEATM.CASRN;
		r.chemicalName=recordNICEATM.Chemical_Name;

		//Omit weak tox (EC3>10%)

		if (recordNICEATM.EC3.contentEquals("NC")) {
			r.binaryResult=0;
		} else if (recordNICEATM.EC3.contains(">") || recordNICEATM.EC3.isEmpty() || recordNICEATM.EC3.contentEquals("IDR")) {
			r.binaryResult=-1;//Weak ambiguous
		} else {
			double EC3=Double.parseDouble(recordNICEATM.EC3);			
			if (EC3>10) r.binaryResult=-1;//Weak ambiguous
			else r.binaryResult=1;			
		}

		return r;
	}
	
	/**
	 * 
	 * Converts to common tox record and assigns binary tox val
	 * 
	 * @param recordNICEATM
	 * @return
	 */
	public static RecordTox getRecordTox(RecordNICEATM recordNICEATM) {
		RecordTox r=new RecordTox();
		r.CAS=recordNICEATM.CASRN;
		r.chemicalName=recordNICEATM.Chemical_Name;

		if (recordNICEATM.EC3.contentEquals("NC")) {
			r.binaryResult=0;
		} else if (recordNICEATM.EC3.contains(">") || recordNICEATM.EC3.isEmpty() || recordNICEATM.EC3.contentEquals("IDR")) {
			r.binaryResult=-1;//ambiguous
		} else {

			//If have an EC3 value, assign to positive (makes models protective):
			try {
				double EC3=Double.parseDouble(recordNICEATM.EC3);
				r.binaryResult=1;				
			} catch (Exception ex) {
				System.out.println(recordNICEATM.CASRN+"\t"+recordNICEATM.EC3+"\tAmbiguous");
				r.binaryResult=-1;
			}
			
		}

		return r;
	}


}
