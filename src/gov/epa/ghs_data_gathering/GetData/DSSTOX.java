package gov.epa.ghs_data_gathering.GetData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import gov.epa.ghs_data_gathering.GetData.SkinSensitization.RecordOECD_Toolbox;
import gov.epa.ghs_data_gathering.Utilities.ExcelUtilities;
import gov.epa.ghs_data_gathering.Utilities.MolFileUtilities;

public class DSSTOX {
	
	/**
	 * Goes through multirow tox records and merges with chemreg/dashboard tables and then outputs flat file and a few others
	 * 
	 * @param recordsTox
	 * @param recordsChemReg
	 * @param recordsDashboard
	 * @param outputFilePathToxRecords flat file of Tox records
	 * @param outputFilePathToxRecordsOmitted omitted Tox records
	 * @param outputFilePathBadChemReg chemicals that are not mapped or no hits
	 */
	public static void goThroughToxRecordsOld(Vector<RecordTox> recordsTox, Vector<RecordChemReg> recordsChemReg,
			Hashtable<String, RecordDashboard> recordsDashboard,String outputFilePathToxRecords,String outputFilePathToxRecordsOmitted,String outputFilePathBadChemReg) {
		
		
		//****************************************************************************************
		// Create look up based on name+CAS:
		Hashtable<String, RecordChemReg> htChemRegCAS_Name = new Hashtable<>();

		// Create look up just based on CAS: (for when names get mangled by saving to
		// file)
		Hashtable<String, RecordChemReg> htChemRegCAS = new Hashtable<>();

		for (RecordChemReg recordChemReg : recordsChemReg) {
			String key = recordChemReg.Query_Casrn + "_" + recordChemReg.Query_Name;
			htChemRegCAS_Name.put(key, recordChemReg);
			htChemRegCAS.put(recordChemReg.Query_Casrn, recordChemReg);
//			System.out.println(recordChemReg.Query_Casrn);
		}
		//****************************************************************************************

		Hashtable<String, Vector<RecordTox>> htToxData = new Hashtable<>();//store tox records based on SID

		FileWriter fwBadChemReg;
		try {
			fwBadChemReg = new FileWriter(outputFilePathBadChemReg);
			fwBadChemReg.write(RecordOECD_Toolbox.getHeader()+"\t"+RecordChemReg.getHeader()+"\r\n");

			for (RecordTox recordTox : recordsTox) {

				String keyCAS_Name = recordTox.CAS + "_" + recordTox.chemicalName.replace("\"", "");

				RecordChemReg recordChemReg = null;

				if (htChemRegCAS_Name.get(keyCAS_Name) == null) {
					if (htChemRegCAS.get(recordTox.CAS) != null) {// try looking up just by CAS:
						recordChemReg = htChemRegCAS.get(recordTox.CAS);
					} else {
						System.out.println(recordTox.CAS + "\tNotFound");
					}
				} else {
					recordChemReg = htChemRegCAS_Name.get(keyCAS_Name);//look up by key CAS
				}

//				if (recordChemReg == null) {
//					System.out.println(recordTox.CAS + "\t" + recordTox.chemicalName + "\tNo ChemReg!");
//				}

				if (recordChemReg != null && DSSTOX.isChemRegOK(recordChemReg)) {

					if (htToxData.get(recordChemReg.Top_HIT_DSSTox_Substance_Id) == null) {
						Vector<RecordTox> records = new Vector<>();
						records.add(recordTox);
						htToxData.put(recordChemReg.Top_HIT_DSSTox_Substance_Id, records);
					} else {
						Vector<RecordTox> records = htToxData.get(recordChemReg.Top_HIT_DSSTox_Substance_Id);
						records.add(recordTox);
					}

				} else {
					fwBadChemReg.write(recordTox+"\t"+recordChemReg+"\r\n");
					fwBadChemReg.flush();
					//TODO write out bad chemreg recs...
				}


			} // end loop over tox records

			fwBadChemReg.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Set<String> setOfSIDS = htToxData.keySet();
		Iterator<String> iterator = setOfSIDS.iterator();

		//Loop through SIDs for stored tox records:
		int uniqueChemicalsWithTox=0;
		
		try {
			FileWriter fw1=new FileWriter(outputFilePathToxRecords);
			FileWriter fw2=new FileWriter(outputFilePathToxRecordsOmitted);

			fw1.write(RecordDashboard.getHeader()+"\tAvgTox\tBinaryTox\r\n");
			fw2.write(RecordDashboard.getHeader()+"\tAvgTox\tBinaryTox\tOmitReason\r\n");
			
			
			TreeMap<String,String>htInChi=new TreeMap<>();
			
			while (iterator.hasNext()) {

				String SID = iterator.next();
				
				RecordDashboard recordDashboard = recordsDashboard.get(SID);
				
				Vector<RecordTox> records = htToxData.get(SID);
				
				double tox=-1;

				int count=0;//count of non ambig tox records
				
				for (RecordTox record:records) {				
					if (record.binaryToxResult>-1)	{
						tox+=record.binaryToxResult;
						count++;
					}
				}

				if (count>0)
					tox/=(double)count;

				int binaryTox=0;
				if (tox>0.5) binaryTox=1;
				
				String omitReason=DSSTOX.getOmitReason(SID, recordsDashboard);
				
				if (omitReason.isEmpty()) {
					if (tox>0.2 && tox < 0.8) omitReason="0.2 < Avg Score < 0.8";	
					if (count==0) omitReason="No non ambiguous records";
				}
				
				if (omitReason.isEmpty()) {
					uniqueChemicalsWithTox++;
					htInChi.put(recordDashboard.INCHIKEY,SID);
					fw1.write(recordDashboard+"\t"+tox+"\t"+binaryTox+"\r\n");
					fw1.flush();
				} else {
					fw2.write(recordDashboard+"\t"+tox+"\t"+binaryTox+"\t"+omitReason+"\r\n");
					fw2.flush();					
				}
			}
			
			
			System.out.println("uniqueChemicalsWithTox=" + uniqueChemicalsWithTox);
			fw1.close();
			fw2.close();
			
			//Determine which chemicals are 2d matches- need to omit one from each pair:
			DSSTOX.find2dMatchesFromInChiKey(recordsDashboard, htInChi,null);

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	/**
	 * Use first part of inchikey (connectivity) to find records in set 2 not in set 1
	 * 
	 * @param dataSetPath1
	 * @param dataSetPath2
	 */
	static void findRecordsNotInDataSet(String dataSetPath1,String dataSetPath2,String outputFilePathUniqueInSet2) {
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(dataSetPath1));
			
			String header=br.readLine();
			String [] colNames=header.split("\t");
			
			Hashtable <String,Integer>htColNums=new Hashtable<>();
			Hashtable <String,String>htInChiKeyTox=new Hashtable<>();
			
			for (int i=0;i<colNames.length;i++) htColNums.put(colNames[i],i);
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
				String [] vals=Line.split("\t");
				
				String InChiKey=vals[htColNums.get("INCHIKEY")];
				String InChiKey2=InChiKey.substring(0,InChiKey.indexOf("-"));
				String BinaryTox=vals[htColNums.get("BinaryTox")];
//				System.out.println(InChiKey2+"\t"+BinaryTox);
				
				htInChiKeyTox.put(InChiKey2, BinaryTox);
				
			}
			br.close();
			System.out.println(htInChiKeyTox.contains("OMIGHNLMNHATMP"));
			
			BufferedReader br2=new BufferedReader(new FileReader(dataSetPath2));
			
			String header2=br2.readLine();
			String [] colNames2=header2.split("\t");
			
			while (true) {
				String Line=br2.readLine();
				if (Line==null) break;
				
				String [] vals=Line.split("\t");
				
				String InChiKey=vals[htColNums.get("INCHIKEY")];
				String InChiKey2=InChiKey.substring(0,InChiKey.indexOf("-"));
				String BinaryTox=vals[htColNums.get("BinaryTox")];
				
				if (htInChiKeyTox.containsKey(InChiKey2)) {
					
					if (!BinaryTox.contentEquals(htInChiKeyTox.get(InChiKey2))) {
						//TODO delete mismatches from data set 1...
						System.out.println(InChiKey2+"\tset 1 value="+htInChiKeyTox.get(InChiKey2)+"\tset 2 value="+BinaryTox);
						
					}					
				}
			
			}
			
			
			br2.close();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	
	/**
	 * Goes through multirow tox records and merges with chemreg/dashboard tables and then outputs flat file and a few others
	 * 
	 * @param recordsTox
	 * @param recordsChemReg
	 * @param recordsDashboard
	 * @param outputFilePathToxRecords flat file of Tox records
	 * @param outputFilePathToxRecordsOmitted omitted Tox records
	 * @param outputFilePathBadChemReg chemicals that are not mapped or no hits
	 */
	public static void goThroughToxRecords(Vector<RecordTox> recordsTox, Vector<RecordChemReg> recordsChemReg,
			Hashtable<String, RecordDashboard> recordsDashboard,String folder,String endpoint,String source) {
		
				
		String fileNameBadChemReg=endpoint+"_"+source+"_BadChemReg.txt";
		String fileNameGoodRecords=endpoint+"_"+source+".txt";
		String fileNameBadRecords=endpoint+"_"+source+"_Omitted.txt";
		String fileNameDuplicate2d=endpoint+"_"+source+"_Duplicate2d.txt";
		String fileNameOutputExcel=endpoint+"_"+source+"_Results.xlsx";
		
		//****************************************************************************************
		// Create look up based on name+CAS:
		Hashtable<String, RecordChemReg> htChemRegCAS_Name = new Hashtable<>();

		// Create look up just based on CAS: (for when names get mangled by saving to
		// file)
		Hashtable<String, RecordChemReg> htChemRegCAS = new Hashtable<>();

		for (RecordChemReg recordChemReg : recordsChemReg) {
			String key = recordChemReg.Query_Casrn + "_" + recordChemReg.Query_Name;
			htChemRegCAS_Name.put(key, recordChemReg);
			htChemRegCAS.put(recordChemReg.Query_Casrn, recordChemReg);
//			System.out.println(recordChemReg.Query_Casrn);
		}
		//****************************************************************************************

		Hashtable<String, Vector<RecordTox>> htToxData = new Hashtable<>();//store tox records based on SID

		FileWriter fwBadChemReg;
		try {
			
			fwBadChemReg = new FileWriter(folder+fileNameBadChemReg);			
			fwBadChemReg.write(RecordTox.getHeader()+"\t"+RecordChemReg.getHeader()+"\r\n");

			for (RecordTox recordTox : recordsTox) {

				String keyCAS_Name = recordTox.CAS + "_" + recordTox.chemicalName.replace("\"", "");

				RecordChemReg recordChemReg = null;

				if (htChemRegCAS_Name.get(keyCAS_Name) == null) {
					if (htChemRegCAS.get(recordTox.CAS) != null) {// try looking up just by CAS:
						recordChemReg = htChemRegCAS.get(recordTox.CAS);
					} else {
						System.out.println(recordTox.CAS + "\tNotFound");
					}
				} else {
					recordChemReg = htChemRegCAS_Name.get(keyCAS_Name);//look up by key CAS
				}

//				if (recordChemReg == null) {
//					System.out.println(recordTox.CAS + "\t" + recordTox.chemicalName + "\tNo ChemReg!");
//				}

				if (recordChemReg != null && DSSTOX.isChemRegOK(recordChemReg)) {

					if (htToxData.get(recordChemReg.Top_HIT_DSSTox_Substance_Id) == null) {
						Vector<RecordTox> records = new Vector<>();
						records.add(recordTox);
						htToxData.put(recordChemReg.Top_HIT_DSSTox_Substance_Id, records);
					} else {
						Vector<RecordTox> records = htToxData.get(recordChemReg.Top_HIT_DSSTox_Substance_Id);
						records.add(recordTox);
					}

				} else {
					fwBadChemReg.write(recordTox+"\t"+recordChemReg+"\r\n");
					fwBadChemReg.flush();
					//TODO write out bad chemreg recs...
				}


			} // end loop over tox records

			fwBadChemReg.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Number of unique chemicals="+htToxData.size());
		
		Set<String> setOfSIDS = htToxData.keySet();
		Iterator<String> iterator = setOfSIDS.iterator();

		//Loop through SIDs for stored tox records:
		int uniqueChemicalsWithTox=0;
		
		try {
			
			
			FileWriter fw1=new FileWriter(folder+fileNameGoodRecords);
			FileWriter fw2=new FileWriter(folder+fileNameBadRecords);

			fw1.write(RecordDashboard.getHeader()+"\tAvgTox\tNumberOfRecords\tBinaryTox\r\n");
			fw2.write(RecordDashboard.getHeader()+"\tAvgTox\tBinaryTox\tOmitReason\r\n");
			
			
			TreeMap<String,String>htInChi=new TreeMap<>();
			
			while (iterator.hasNext()) {

				String SID = iterator.next();
				
				RecordDashboard recordDashboard = recordsDashboard.get(SID);
				
				Vector<RecordTox> records = htToxData.get(SID);
				
				double tox=0;
				int count=0;//count of non ambig tox records
				
				for (RecordTox record:records) {				
					if (record.binaryToxResult>-1)	{
						tox+=record.binaryToxResult;
						count++;
					}
					
//					if (record.CAS.contentEquals("10461-98-0")) {
//						System.out.println("Result in go through records="+record.binaryToxResult);
//					}
				}


				int binaryTox=-1;
				
				if (count>0) {
					tox/=(double)count;
					
					if (tox>0.5) binaryTox=1;
					else binaryTox=0;
					
				} else {
					tox=-1;
				}
				
				
				
				String omitReason=DSSTOX.getOmitReason(SID, recordsDashboard);
				
				if (omitReason.isEmpty()) {
					if (tox>0.2 && tox < 0.8) omitReason="0.2 < Avg Score < 0.8";	
					if (count==0) omitReason="No non ambiguous records";
				}
				
				if (omitReason.isEmpty()) {
					uniqueChemicalsWithTox++;
					htInChi.put(recordDashboard.INCHIKEY,SID);
					fw1.write(recordDashboard+"\t"+tox+"\t"+count+"\t"+binaryTox+"\r\n");
					fw1.flush();
				} else {
					fw2.write(recordDashboard+"\t"+tox+"\t"+binaryTox+"\t"+omitReason+"\r\n");
					fw2.flush();					
				}
			}
						
			System.out.println("uniqueChemicalsWithTox=" + uniqueChemicalsWithTox);
			fw1.close();
			fw2.close();
			
			//Determine which chemicals are 2d matches- need to omit one from each pair:
			DSSTOX.find2dMatchesFromInChiKey(recordsDashboard, htInChi,folder+fileNameDuplicate2d);
			
			Vector<String>fileNames=new Vector<String>();
			fileNames.add(fileNameGoodRecords);
			fileNames.add(fileNameBadRecords);
			fileNames.add(fileNameBadChemReg);
			fileNames.add(fileNameDuplicate2d);
			
			createSpreadsheetFromTextFiles(folder, fileNames, fileNameOutputExcel);

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	private static void writeTextFileToExcelSheet(XSSFSheet sheet, String filePath) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(filePath));

			int row=0;

			while (true) {
				String Line=br.readLine();
				if (Line==null) break;

				XSSFRow rowHeader = sheet.createRow(row++);
				
				String [] vals=Line.split("\t");
				
		        for (int i=0;i<vals.length;i++) {
		        	XSSFCell cell = rowHeader.createCell(i);
					cell.setCellValue(vals[i]);
		        }
			}
			br.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void createSpreadsheetFromTextFiles(String folder,Vector<String>filenames,String outputFileName) {
		
		XSSFWorkbook workbook = new XSSFWorkbook();
		
		for (String filename:filenames) {
			XSSFSheet sheet = workbook.createSheet(filename.substring(0,filename.indexOf(".")));
			String filePath=folder+filename;
			writeTextFileToExcelSheet(sheet, filePath);
		}
		
		try {
            FileOutputStream outputStream = new FileOutputStream(folder+"/"+outputFileName);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

		
	}
	
	public static Hashtable<String, RecordDashboard> parseDashboardExcel(String filePathExcel) {
		Hashtable<String, RecordDashboard> records = new Hashtable<>();

		try {

			FileInputStream inputStream = new FileInputStream(new File(filePathExcel));

			HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
			HSSFSheet sheet = workbook.getSheetAt(0);

			HSSFRow rowHeader = sheet.getRow(0);

			Hashtable<Integer, String> htColNames = new Hashtable<>();// column name for each column number

			for (int col = 0; col < rowHeader.getLastCellNum(); col++) {
				String colName = ExcelUtilities.getStringValue(rowHeader.getCell(col));
				htColNames.put(col, colName.replace(" ", "_"));
			}

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {

				HSSFRow row = sheet.getRow(i);

				RecordDashboard r = new RecordDashboard();

				for (int j = 0; j < row.getLastCellNum(); j++) {
					String value = ExcelUtilities.getStringValue(row.getCell(j));
					r.setValue(htColNames.get(j), value);
				}
//				System.out.println(r);
				
				r.IUPAC_NAME=r.IUPAC_NAME.replace("\r", "").replace("\n", "");
				
				
				records.put(r.DTXSID, r);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return records;

	}
	
	
	
	public static  void find2dMatchesFromInChiKey(Hashtable<String, RecordDashboard> recordsDashboard,
			TreeMap<String, String> htInChi) {
		
		try {

			
			Set set = htInChi.entrySet();
			// Get an iterator
			Iterator i = set.iterator();
			// Display elements
			while(i.hasNext()) {
				Map.Entry me = (Map.Entry)i.next();

				if (!i.hasNext()) break; 

				Map.Entry me2 = (Map.Entry)i.next();

				String inchi1=(String)me.getKey();
				String inchi2=(String)me2.getKey();

				String bob1=inchi1.substring(0,inchi1.indexOf("-"));
				String bob2=inchi2.substring(0,inchi2.indexOf("-"));

				if (bob1.contentEquals(bob2)) {

					String SID1=(String)me.getValue();
					String SID2=(String)me2.getValue();

					RecordDashboard recordDashboard1 = recordsDashboard.get(SID1);
					RecordDashboard recordDashboard2 = recordsDashboard.get(SID2);

				}

			}


		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	public static  void find2dMatchesFromInChiKey(Hashtable<String, RecordDashboard> recordsDashboard,
			TreeMap<String, String> htInChi,String outputFilePath) {
		
		try {

			FileWriter fw=new FileWriter(outputFilePath);
			
			Set set = htInChi.entrySet();
			// Get an iterator
			Iterator i = set.iterator();
			// Display elements
			while(i.hasNext()) {
				Map.Entry me = (Map.Entry)i.next();

				if (!i.hasNext()) break; 

				Map.Entry me2 = (Map.Entry)i.next();

				String inchi1=(String)me.getKey();
				String inchi2=(String)me2.getKey();

				String bob1=inchi1.substring(0,inchi1.indexOf("-"));
				String bob2=inchi2.substring(0,inchi2.indexOf("-"));

				if (bob1.contentEquals(bob2)) {

					String SID1=(String)me.getValue();
					String SID2=(String)me2.getValue();

					RecordDashboard recordDashboard1 = recordsDashboard.get(SID1);
					RecordDashboard recordDashboard2 = recordsDashboard.get(SID2);

					fw.write(SID1+"\t"+SID2+"\t"+recordDashboard1.IUPAC_NAME+"\t"+recordDashboard2.IUPAC_NAME+"\r\n");
					fw.flush();
				}

			}
			fw.close();


		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	public static String getOmitReason(String SID,Hashtable<String, RecordDashboard> recordsDashboard) {
		
		RecordDashboard recordDashboard = recordsDashboard.get(SID);
		String smiles = recordDashboard.SMILES;

		if (smiles.contains(".")) {
			return "Salt";
		} else if (smiles.contentEquals("-") || smiles.isEmpty()) {
			return "No atoms";
		} else if (smiles.contains("|")) {
			return "Smiles contains |";
		} else if (smiles.contains("*")) {
			return "Smiles contains *";
		} else {
			SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

			AtomContainer m = null;
			try {
				// m=sp.parseSmiles(Smiles);
				m = (AtomContainer) sp.parseSmiles(smiles);

				if (MolFileUtilities.HaveBadElement(m)) {
					return "Bad element";
				} else if (!MolFileUtilities.HaveCarbon(m)) {
					return "No carbon";
				}

			} catch (Exception ex) {
				return "Can't parse smiles";
			}
		}
		return "";
		
	}

	
	public static boolean isChemRegOK(RecordChemReg recordChemReg) {
		if (recordChemReg.Lookup_Result.contentEquals("No Hits"))
			return false;
		if (recordChemReg.Validated.contentEquals("FALSE"))
			return false;
		return true;
	}

	
	public static Vector<RecordChemReg> parseChemRegExcel(String filePathExcel) {
		Vector<RecordChemReg> records = new Vector<>();

		try {

			FileInputStream inputStream = new FileInputStream(new File(filePathExcel));

			XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
			XSSFSheet sheet = workbook.getSheetAt(0);

			XSSFRow rowHeader = sheet.getRow(0);

			Hashtable<Integer, String> htColNames = new Hashtable<>();// column name for each column number

			for (int col = 0; col < rowHeader.getLastCellNum(); col++) {
				String colName = ExcelUtilities.getStringValue(rowHeader.getCell(col));
				htColNames.put(col, colName.replace(" ", "_"));
			}

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {

				XSSFRow row = sheet.getRow(i);

				RecordChemReg r = new RecordChemReg();

				for (int j = 0; j < row.getLastCellNum(); j++) {
					String value = ExcelUtilities.getStringValue(row.getCell(j));
					r.setValue(htColNames.get(j), value);
				}
				records.add(r);

//				if (r.Query_Casrn.contentEquals("Invalid CAS number: 0-11-0"))
//					System.out.println(r);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return records;

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2020 skin sensitization paper\\";
		
		String endpoint="LLNA";
		String source1="NICEATM";
//		String source2="OECD_Toolbox";
		String source2="eChemPortal";
		
		String dataSetPath1=folder+endpoint+"_"+source1+"\\"+endpoint+"_"+source1+".txt";
		String dataSetPath2=folder+endpoint+"_"+source2+"\\"+endpoint+"_"+source2+".txt";
		String dataSetPath3=folder+endpoint+"_"+source2+"\\"+endpoint+"_"+source2+"_not_in_"+source1+".txt";
		
		DSSTOX.findRecordsNotInDataSet(dataSetPath1, dataSetPath2,dataSetPath3);
	}

}
