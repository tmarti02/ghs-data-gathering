package gov.epa.QSAR.DataSetCreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import gov.epa.QSAR.utilities.ExcelUtilities;
import gov.epa.QSAR.utilities.MolFileUtilities;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.RecordFinalizer;


public class DSSTOX {
	
//	/**
//	 * Goes through multirow tox records and merges with chemreg/dashboard tables and then outputs flat file and a few others
//	 * 
//	 * @param recordsTox
//	 * @param recordsChemReg
//	 * @param recordsDashboard
//	 * @param outputFilePathToxRecords flat file of Tox records
//	 * @param outputFilePathToxRecordsOmitted omitted Tox records
//	 * @param outputFilePathBadChemReg chemicals that are not mapped or no hits
//	 */
//	public static void goThroughToxRecordsOld(Vector<RecordTox> recordsTox, Vector<RecordChemReg> recordsChemReg,
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
//		Hashtable<String, Vector<RecordTox>> htToxData = new Hashtable<>();//store tox records based on SID
//
//		FileWriter fwBadChemReg;
//		try {
//			fwBadChemReg = new FileWriter(outputFilePathBadChemReg);
//			fwBadChemReg.write(RecordOECD_Toolbox.getHeader()+"\t"+RecordChemReg.getHeader()+"\r\n");
//
//			for (RecordTox recordTox : recordsTox) {
//
//				String keyCAS_Name = recordTox.CAS + "_" + recordTox.chemicalName.replace("\"", "");
//
//				RecordChemReg recordChemReg = null;
//
//				if (htChemRegCAS_Name.get(keyCAS_Name) == null) {
//					if (htChemRegCAS.get(recordTox.CAS) != null) {// try looking up just by CAS:
//						recordChemReg = htChemRegCAS.get(recordTox.CAS);
//					} else {
//						System.out.println(recordTox.CAS + "\tNotFound");
//					}
//				} else {
//					recordChemReg = htChemRegCAS_Name.get(keyCAS_Name);//look up by key CAS
//				}
//
////				if (recordChemReg == null) {
////					System.out.println(recordTox.CAS + "\t" + recordTox.chemicalName + "\tNo ChemReg!");
////				}
//
//				if (recordChemReg != null && DSSTOX.isChemRegOK(recordChemReg)) {
//
//					if (htToxData.get(recordChemReg.Top_HIT_DSSTox_Substance_Id) == null) {
//						Vector<RecordTox> records = new Vector<>();
//						records.add(recordTox);
//						htToxData.put(recordChemReg.Top_HIT_DSSTox_Substance_Id, records);
//					} else {
//						Vector<RecordTox> records = htToxData.get(recordChemReg.Top_HIT_DSSTox_Substance_Id);
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
//				Vector<RecordTox> records = htToxData.get(SID);
//				
//				double tox=-1;
//
//				int count=0;//count of non ambig tox records
//				
//				for (RecordTox record:records) {				
//					if (record.binaryResult>-1)	{
//						tox+=record.binaryResult;
//						count++;
//					}
//				}
//
//				if (count>0)
//					tox/=(double)count;
//
//				int binaryTox=0;
//				if (tox>0.5) binaryTox=1;
//				
//				String omitReason=DSSTOX.getOmitReason(SID, recordsDashboard);
//				
//				if (omitReason.isEmpty()) {
//					if (tox>0.2 && tox < 0.8) omitReason="0.2 < Avg Score < 0.8";	
//					if (count==0) omitReason="No non ambiguous records";
//				}
//				
//				if (omitReason.isEmpty()) {
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
//			DSSTOX.find2dMatchesFromInChiKey(recordsDashboard, htInChi,null);
//
//			
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//	}
	
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
	 * Goes through records and assigns DSSTox info by using records from two chemreg spreadsheets
	 * This version uses id_physchem as external id to get the dsstox record

	 * @param recordsExperimental
	 * @param recordsChemReg
	 * @param recordsDSSTox
	 * @param folder
	 * @param endpoint
	 */
	public static void goThroughToxRecords(ExperimentalRecords recordsExperimental, Vector<RecordChemReg> recordsChemReg,Vector<RecordDSSTox> recordsDSSTox,String folder,String endpoint,boolean omitSalts) {

		//DSSTox excel spreadsheet doesnt have validated column!
		//ChemReg excel has validated column but doesnt have external_ID, so need to look up by CAS+Name or by CAS if that fails:
		Hashtable<String, RecordChemReg> htChemRegCAS_Name= new Hashtable<>();//look up by cas+name
		Hashtable<String, RecordChemReg> htChemRegCAS=new Hashtable<>();//just in case look up by CAS+Name fails due to special characters
		Hashtable<String, RecordChemReg> htChemRegName= new Hashtable<>();//look up by name
		RecordChemReg.createLookupForChemRegRecords(recordsChemReg, htChemRegCAS_Name, htChemRegCAS,htChemRegName);
		
		Hashtable<String,RecordDSSTox>htDSSToxID_Physchem=RecordDSSTox.getDSSToxLookupByID_Physchem(recordsDSSTox);
				 
		//****************************************************************************************
								
		try {//			
			
			for (ExperimentalRecord recordExperimental : recordsExperimental) {
				if (!recordExperimental.keep) continue;
				
				//Retrieve RecordChemReg by CAS+name (or by CAS if that fails): 
				RecordChemReg recordChemReg=RecordChemReg.getChemRegRecord(recordExperimental,htChemRegCAS_Name,htChemRegCAS,htChemRegName);
								
				if (recordChemReg==null) {
					System.out.println("CAS="+recordExperimental.casrn+"\tName="+recordExperimental.chemical_name+"\tNo chemreg match");
					recordExperimental.keep=false;
					recordExperimental.reason="No chemreg match";
					continue;
				}
				
				if (recordChemReg.Validated==null || recordChemReg.Validated.contentEquals("FALSE")) {
					System.out.println("CAS="+recordExperimental.casrn+"\tName="+recordExperimental.chemical_name+"\tValidated="+recordChemReg.Validated);
					recordExperimental.keep=false;
					recordExperimental.reason="Not validated in chemreg";
					continue;
				}
																				
				RecordDSSTox recordDSSTox=htDSSToxID_Physchem.get(recordExperimental.id_physchem);
																
				if (recordDSSTox==null) {
//					System.out.println("Missing DSSTox record for "+recordChemReg.Top_HIT_DSSTox_Substance_Id);
					recordExperimental.keep=false;
					recordExperimental.reason="No record in DSSTox for this SID";
					continue;					
				}

				setDSSToxData(recordExperimental, recordDSSTox);					
				omitBasedDSSToxRecord(recordExperimental);
				
				//Stopgap measure until we have qsarready generation in place:
				if (recordExperimental.Structure_SMILES_2D_QSAR==null) {
					if (recordExperimental.Structure_SMILES!=null) {
						recordExperimental.Structure_SMILES_2D_QSAR=recordExperimental.Structure_SMILES;
					}
				}
								
				if (omitSalts) 
					omitBasedOnSmiles(recordExperimental,recordExperimental.Structure_SMILES);
				else
					omitBasedOnSmiles(recordExperimental,recordExperimental.Structure_SMILES_2D_QSAR);

				
			} // end loop over tox records
			
			recordsExperimental.toExcel_File(folder+endpoint+"_recordsQSAR.xlsx",ExperimentalRecord.outputFieldNamesQSAR);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	
	/**
	 * Goes through records and assigns DSSTox info by using records from two chemreg spreadsheets<br>
	 * 
	 * This version uses SID from matched chemreg records to get the dsstox record since the physchem_id might change as more data is added
	 * 
	 * RecordChemReg is only way to determine if validated false since RecordDSSTox only tells you if validated=null!
	 * Only way to look up RecordChemReg is by Name/CAS (has no smiles or external_id)
	 * RecordDSSTox can be looked up by SID or external ID (if have RecordChemReg- then have SID) 
	 * 
	 * @param recordsExperimental
	 * @param recordsChemReg
	 * @param recordsDSSTox
	 * @param folder
	 * @param endpoint
	 */
	public static void goThroughToxRecords2(ExperimentalRecords recordsExperimental, Vector<RecordChemReg> recordsChemReg,Vector<RecordDSSTox> recordsDSSTox,String folder,String endpoint, boolean omitSalts) {

		//DSSTox excel spreadsheet doesnt have validated column!
		//ChemReg excel has validated column but doesnt have external_ID, so need to look up by CAS+Name or by CAS if that fails:
		Hashtable<String, RecordChemReg> htChemRegCAS_Name= new Hashtable<>();//look up by cas+name
		Hashtable<String, RecordChemReg> htChemRegCAS=new Hashtable<>();//just in case look up by CAS+Name fails due to special characters
		Hashtable<String, RecordChemReg> htChemRegName= new Hashtable<>();//look up by name
		RecordChemReg.createLookupForChemRegRecords(recordsChemReg, htChemRegCAS_Name, htChemRegCAS,htChemRegName);

		Hashtable<String,RecordDSSTox>htDSSToxSID=RecordDSSTox.getDSSToxLookupBySID(recordsDSSTox);

		//****************************************************************************************

		try {//			

			for (ExperimentalRecord recExp : recordsExperimental) {
				if (!recExp.keep) continue;

				//Retrieve RecordChemReg by CAS+name (or by CAS if that fails): 
				RecordChemReg recordChemReg=RecordChemReg.getChemRegRecord(recExp,htChemRegCAS_Name,htChemRegCAS,htChemRegName);

				if (recordChemReg==null) {
//					System.out.println("CAS="+recordExperimental.casrn+"\tName="+recordExperimental.chemical_name+"\tNo chemreg match");
					recExp.keep=false;
					recExp.reason="No chemreg match";
					continue;
				}

				if (recordChemReg.Validated==null || recordChemReg.Validated.contentEquals("FALSE")) {
//					System.out.println("CAS="+recordExperimental.casrn+"\tName="+recordExperimental.chemical_name+"\tValidated="+recordChemReg.Validated);
					recExp.keep=false;
					recExp.reason="Not validated in chemreg";
					continue;
				}
				recExp.dsstox_substance_id=recordChemReg.Top_HIT_DSSTox_Substance_Id;

				RecordDSSTox recordDSSTox=htDSSToxSID.get(recordChemReg.Top_HIT_DSSTox_Substance_Id);

				if (recordDSSTox==null) {
//					System.out.println("Missing DSSTox record for "+recordChemReg.Top_HIT_DSSTox_Substance_Id);
					recExp.keep=false;
					recExp.reason="No record in DSSTox for this SID";
					continue;					
				}

				setDSSToxData(recExp, recordDSSTox);					
				omitBasedDSSToxRecord(recExp);
				
				if (!recExp.keep) continue;
				
				//Stopgap measure until we have qsarready generation in place:
				if (recExp.Structure_SMILES_2D_QSAR==null) {
					if (recExp.Structure_SMILES!=null) {
						recExp.Structure_SMILES_2D_QSAR=recExp.Structure_SMILES;
					}
				}
								
				if (omitSalts) 
					omitBasedOnSmiles(recExp,recExp.Structure_SMILES);
				else
					omitBasedOnSmiles(recExp,recExp.Structure_SMILES_2D_QSAR);

				if (!recExp.keep) continue;
				
				RecordFinalizer.finalizeRecordAgain(recExp);
				
				

			} // end loop over tox records

			recordsExperimental.toExcel_File(folder+endpoint+"_recordsQSAR.xlsx",ExperimentalRecord.outputFieldNamesQSAR);


		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void omitBasedDSSToxRecord(ExperimentalRecord rec) {
		
		if (rec.Substance_Type.contentEquals("Mineral/Composite")
				|| rec.Substance_Type.contentEquals("Mixture/Formulation")
				|| rec.Substance_Type.contentEquals("Polymer")) {
			rec.keep=false;
			rec.reason="Bad Substance_Type";
			return;
		}
		if (rec.Structure_MolWt==null) {
			rec.keep=false;
			rec.reason="Missing Structure_MolWt";
			return;			
		}

						
	}


	private static void omitBasedOnSmiles(ExperimentalRecord rec,String smiles) {
		
		if (smiles==null || smiles.isEmpty()) {
			rec.keep=false;
			rec.reason="Structure_SMILES is missing";
			return;
		}
				
		if (smiles.contains(".")) {
			rec.keep=false;
			rec.reason="Smiles indicates a salt";
			return;
		}
					
				
		AtomContainer ac=getAtomContainer(smiles);

		if (ac==null) {
			rec.keep=false;
			rec.reason="bad Structure_SMILES";
//			System.out.println(rec);
			return;
		}

		
		if (MolFileUtilities.HaveBadElement(ac))  {
			rec.keep=false;
			rec.reason="Structure_SMILES indicates bad element and omitSalts=true";
			return;
		}

		if (ac.getAtomCount()==1) {
			rec.keep=false;
			rec.reason="only 1 atom in Structure_SMILES";
			return;
		}
		
		if (!MolFileUtilities.HaveCarbon(ac)) {
			rec.keep=false;
			rec.reason="No carbon in Structure_SMILES";
			return;
		}
	}
	
	
	
	private static void createLookupForDSSToxRecords(Vector<RecordDSSTox> recordsDSSTox,
			Hashtable<String, RecordDSSTox> htCAS_Name, Hashtable<String, RecordDSSTox> htCAS,
			Hashtable<String, RecordDSSTox> htName,Hashtable<String, RecordDSSTox> htSID) {
		
		for (RecordDSSTox recordDSSTOX : recordsDSSTox) {
			
			
			if (recordDSSTOX.DSSTox_Substance_Id==null) {
				continue;
			}
			
			String cas=recordDSSTOX.Substance_CASRN.trim();
			String name=recordDSSTOX.Substance_Name.toLowerCase().trim();

//			System.out.println(recordDSSTOX.DSSTox_Substance_Id+"\t"+recordDSSTOX.Substance_CASRN+"\t"+recordDSSTOX.Substance_Name);
			
			String key = cas + "_" + name;
			
//			if (recordDSSTOX.Substance_CASRN.contentEquals("129050-29-9")) System.out.println(key);
		
			
			if (cas!=null  && !cas.isEmpty()) {
				htCAS_Name.put(key, recordDSSTOX);
				htCAS.put(cas, recordDSSTOX);				
			}
									
			htName.put(name, recordDSSTOX);
			htSID.put(recordDSSTOX.DSSTox_Structure_Id,recordDSSTOX);
			
//			if (recordDSSTOX.Substance_Name.contentEquals("(2S)-6-fluoro-2-(oxiran-2-yl)chromane")) {
//				System.out.println(recordDSSTOX);
//			}
			
//			System.out.println("*"+recordDSSTOX.Substance_Name+"*");
			
			
			//					System.out.println(recordChemReg.Query_Casrn);
		}
	}

	
	public static void setDSSToxData(ExperimentalRecord experimentalRecord,RecordDSSTox rDSSTox) {
		String[] fieldNames = { "Substance_Name", "Substance_CASRN", "Substance_Type", "Substance_Note",
				"Structure_SMILES", "Structure_InChI", "Structure_InChIKey", "Structure_Formula", "Structure_MolWt",
				"Structure_SMILES_2D_QSAR" };

		for (String fieldName:fieldNames) {
			
			try {
				
				Field myFieldSrc =rDSSTox.getClass().getField(fieldName);				
				Field myFieldDest =experimentalRecord.getClass().getField(fieldName);							
				
				experimentalRecord.assignValue(fieldName, (String)myFieldSrc.get(rDSSTox));
				
//				if (fieldName.contentEquals("Structure_MolWt")) {
//					 if (rDSSTox.Structure_MolWt!=null && !rDSSTox.Structure_MolWt.isEmpty()) {
//						 Double MW=Double.valueOf(rDSSTox.Structure_MolWt);
//						 myFieldDest.set(experimentalRecord, MW);
//					 }
//				} else {
//					experimentalRecord.assignValue(fieldName, (String)myFieldSrc.get(rDSSTox));
//				}
				
				
			} catch (Exception ex){
				ex.printStackTrace();
			}
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
	
//	public static String getOmitReason(String SID,Hashtable<String, RecordDashboard> recordsDashboard) {
//		
//		RecordDashboard recordDashboard = recordsDashboard.get(SID);
//		String smiles = recordDashboard.SMILES;
//
//		if (smiles.contains(".")) {
//			return "Salt";
//		} else if (smiles.contentEquals("-") || smiles.isEmpty()) {
//			return "No atoms";
//		} else if (smiles.contains("|")) {
//			return "Smiles contains |";
//		} else if (smiles.contains("*")) {
//			return "Smiles contains *";
//		} else {
//			SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
//
//			AtomContainer m = null;
//			try {
//				// m=sp.parseSmiles(Smiles);
//				m = (AtomContainer) sp.parseSmiles(smiles);
//
//				if (MolFileUtilities.HaveBadElement(m)) {
//					return "Bad element";
//				} else if (!MolFileUtilities.HaveCarbon(m)) {
//					return "No carbon";
//				}
//
//			} catch (Exception ex) {
//				return "Can't parse smiles";
//			}
//		}
//		return "";
//		
//	}
	
//	public static void setSmilesFlags(RecordDSSTox rDSSTox,RecordTox rTox) {
//
//				
//		String smiles=rDSSTox.Structure_SMILES;
//
//		rTox.isSalt=smiles.contains(".");
//
//		if (smiles.contentEquals("-") || smiles.isEmpty())	rTox.hasStructure=false;
//
//		if (smiles.contains("|") || smiles.contains("*"))  rTox.hasSpecialCharacter=true;
//
//		if (!rTox.hasStructure) return;
//
//		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
//		
//
//		try {
//			// m=sp.parseSmiles(Smiles);
//			AtomContainer m = (AtomContainer) sp.parseSmiles(smiles);
//
//			rTox.haveBadElement=MolFileUtilities.HaveBadElement(m);
//			rTox.haveCarbon=MolFileUtilities.HaveCarbon(m);
//
//		} catch (Exception ex) {
//			System.out.println("For CAS="+rTox.casrn_Source+"\tCant parse smiles="+smiles);
//		}
//
//		
//	}
	
	public static AtomContainer getAtomContainer(String smiles) {
		try {
			// m=sp.parseSmiles(Smiles);
			
			if (smiles.contains("Failed") || smiles.contains("|") || smiles.contains("*")) return null;
			
			SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
			AtomContainer m = (AtomContainer) sp.parseSmiles(smiles);

			return m;
			

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
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
