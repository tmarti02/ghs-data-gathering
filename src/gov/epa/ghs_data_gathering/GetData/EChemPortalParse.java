package gov.epa.ghs_data_gathering.GetData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import gov.epa.ghs_data_gathering.Utilities.Utilities;

public class EChemPortalParse {
	public static final String scoreAmbiguous="-1";
	public static final String scorePositive="1";
	public static final String scoreNegative="0";

	
	/**
	 * This class tries to remove duplicates by keeping one record if there is >= 80% consensus
	 * 
	 * TODO redo with  com.google.common.collect.Multimap for simpler code
	 * 
	 * @param filepathOld
	 * @param filepathNew
	 */
	public static void omitDuplicateRecords(String filepathOld,String filepathNew) {

		try {

			BufferedReader br=new BufferedReader(new FileReader(filepathOld));
			String headerOld=br.readLine();

			FileWriter fw=new FileWriter(filepathNew);

			fw.write(headerOld+"\r\n");

			LinkedList <String>hlist=Utilities.Parse(headerOld, "\t");

			Hashtable<String,Vector<RecordEchemportal2>>ht=new Hashtable();

			while (true) {
				//				if (counter%1000==0) System.out.println(counter);
				String Line=br.readLine();
				if (Line==null) break;
				LinkedList <String>list=Utilities.Parse(Line, "\t");
				
//				System.out.println(Line);
				
				RecordEchemportal2 r = RecordEchemportal2.createRecord(hlist, list);
				String CAS=r.CAS_final;

				if (ht.get(CAS)==null) {
					Vector<RecordEchemportal2>records=new Vector<>();
					records.add(r);
					ht.put(CAS, records);
				} else {
					Vector<RecordEchemportal2>records=ht.get(CAS);
					records.add(r);		
				}

			}
			List<String> tmp = Collections.list(ht.keys());
			Collections.sort(tmp);
			Iterator<String> it = tmp.iterator();

			while(it.hasNext()){
				String CAS =it.next();
				//			    System.out.println(CAS);

				Vector<RecordEchemportal2>records=ht.get(CAS);

				if (records.size()==1) {
					fw.write(records.get(0)+"\r\n");
					fw.flush();

				} else {

					double avgscore=0;
					int num=0;

					//figure out avg score from multiple records

					for (int i=0;i<records.size();i++) {

						String scorei=records.get(i).FinalScore;

						if (scorei.equals(EChemPortalParse.scorePositive)) {
							avgscore+=1;
							num++;
						} else if (scorei.equals(EChemPortalParse.scoreNegative)) {
							//do nothing
							num++;
						} else {
//							System.out.println(CAS+"\tinvalid\t"+scorei);
						}
					}

					avgscore/=(double)num;

					double finalScore=-1;

					double frac=0.2;//fraction that can be conflicting

					if (avgscore<=frac) {
						finalScore=0;
					} else if (avgscore>=(1-frac)) {
						finalScore=1;
					}

					if (finalScore==-1 && num>0) {
						System.out.println(CAS+"\tconflicting");
//						for (int i=0;i<records.size();i++) {
//							System.out.println(records.get(i));
//						}
						continue;
					}  else if (num==0) {
						System.out.println(CAS+"\tambiguous");
						continue;
					}


					for (int i=0;i<records.size();i++) {
						RecordEchemportal2 ri=records.get(i);
						String scorei=ri.FinalScore;

						//output the first record that matches the average score result:
						if (finalScore==0) {
							if (scorei.equals(EChemPortalParse.scoreNegative)) {
//								System.out.println(CAS+"\tfirst neg score");
								fw.write(ri+"\r\n");
								fw.flush();
								break;
							}
						} else if (finalScore==1) {
							if (scorei.equals(EChemPortalParse.scorePositive)) {
//								System.out.println(CAS+"\tfirst pos score");
								fw.write(ri+"\r\n");
								fw.flush();
								break;
							}
						}
					}
					//			    	System.out.println(CAS+"\t"+avgscore+"\t"+records.size());
				}
			}

			fw.close();
			br.close();


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
//	private void omitBasedOnGuideline(RecordEchemportal2 r) {
//	Vector<String>acceptableGuidelines=new Vector<>();
//	acceptableGuidelines.add("429");
//	acceptableGuidelines.add("442B");
//	acceptableGuidelines.add("442 B");
//
//	if (!containsSubstring(acceptableGuidelines, r.Test_guideline_Guideline)) {
//		r.omit_reason=append(r.omit_reason,"guideline");
//	}
//}
//private void omitBasedOnStudyResultType(RecordEchemportal2 r) {
//	if (r.Type_of_information.toLowerCase().contentEquals("experimental result")) return;
//	
//	ArrayList<String>resultTypeEstimated=new ArrayList<>();
//	resultTypeEstimated.add("(q)sar");
//	resultTypeEstimated.add("estimated"); 
//	resultTypeEstimated.add("read-across");		
//	resultTypeEstimated.add("read across"); 
//	
//	for (String est:resultTypeEstimated) {
//		
////		System.out.println(r.study_result_type.toLowerCase()+"\t"+est);
//		
//		if (r.Type_of_information.toLowerCase().contains(est)) {
//			r.omit_reason=append(r.omit_reason,"estimated");
//			break;
//		}
//	}
//	
//	if (r.Type_of_information.equals("")) {
//		r.omit_reason=append(r.omit_reason,"unknown study type");
//							 
//	}
//	
//}

//private void omitBasedOnTypeOfStudy(RecordEchemportal2 r) {
//	
//	ArrayList<String>acceptableTypes=new ArrayList<>();
//	acceptableTypes.add("llna");
//	
//	for (String goodType:acceptableTypes) {
//		if (r.Type_of_study.toLowerCase().contains(goodType)) {
////			System.out.println(r.CAS_final+"\t"+r.Type_of_study+"\t"+r.Test_guideline_Guideline);
//			return;
//		}
//	}
////	System.out.println(r.CAS_final+"\t"+r.Type_of_study);		
//	r.omit_reason=append(r.omit_reason,"type of study");
//}
	
	private void omitBasedOnReliability(RecordEchemportal2 r) {
		ArrayList<String>acceptableReliability=new ArrayList<>();
		acceptableReliability.add("1 (reliable without restriction)");
		acceptableReliability.add("2 (reliable with restrictions)"); 

		if (!acceptableReliability.contains(r.Reliability)) {
			r.omit_reason=append(r.omit_reason,"reliability");
		}
	}
	
	/**
	 * Go through skin sensitization records and retrieve LLNA records
	 * @param filepath
	 * @param filepathGood
	 * @param filepathBad
	 */
	public static void goThroughRecords(String filepathText,String filepathGood,String filepathBad,String scifinderFilePath,String ECHACASlookup,Hashtable<String,String>htDict,boolean printUniqueIORs) {
		try {

			Hashtable<String,ScifinderRecord>htScifinderRecords=Scifinder.getScifinderData(scifinderFilePath);
			Hashtable<String,String>htECHA_CAS_lookup=EChemPortalParse.getECHACASLookup(ECHACASlookup);
			
			BufferedReader br=new BufferedReader(new FileReader(filepathText));
			String headerOld=br.readLine();

			FileWriter fwGood=new FileWriter(filepathGood);
			FileWriter fwBad=new FileWriter(filepathBad);

			LinkedList <String>hlist=Utilities.Parse(headerOld, "\t");
			
			fwGood.write(RecordEchemportal2.getHeader()+"\r\n");
			fwBad.write(RecordEchemportal2.getHeader()+"\r\n");
			
			int goodCount=0;

			Vector<String>needSCIFINDER=new Vector<String>();
			Vector<String>uniqueIOR=new Vector<>();

			while (true) {
				//for (int ii=1;ii<=5000;ii++) {
				
				String Line=br.readLine();
				if (Line==null) break;

				LinkedList <String>list=Utilities.Parse(Line, "\t");
				RecordEchemportal2 r = RecordEchemportal2.createRecord(hlist, list);
				
//				System.out.println(r.Record_Number+"\t"+r.Substance_Number);
				
				if (r.Number_type.contentEquals("EC Number")) {
					if (htECHA_CAS_lookup.get(r.Substance_Number)!=null) {
						r.CAS_final=htECHA_CAS_lookup.get(r.Substance_Number);
						System.out.println(r.CAS_final);
					}
				} else if (r.Number_type.contentEquals("CAS Number")) {
					r.CAS_final=r.Substance_Number;
				}
				
//				//fix wrong CAS numbers:
				Scifinder.fixCASFinal(r,htScifinderRecords);
//			
//				//Use main cas number in Scifinder (i.e. not alternate one):
//				
//				//Omit based on study info:
//				omitBasedOnTypeOfStudy(r);				
//				omitBasedOnStudyResultType(r);//may not be needed since I filtered for this on echemportal website
//				omitBasedOnGuideline(r);
//				omitBasedOnReliability(r);
				
				String original=r.Interpretation_of_results;
				
				EChemPortalParse.getInterpretationBasis(r);
				
				omitBasedOnInterpretation(r,htDict,original); 
				
				if(!uniqueIOR.contains(r.Interpretation_of_results)) {
					uniqueIOR.add(r.Interpretation_of_results);
				}
				
//				
				if (htScifinderRecords.get(r.CAS_final)==null 
						&& !needSCIFINDER.contains(r.CAS_final) 
						&& r.omit_reason.isEmpty() && !r.CAS_final.isEmpty()
						&& !r.CAS_final.contains(",") && !r.CAS_final.contains("and")) {
					needSCIFINDER.add(r.CAS_final);//store in list so can download later, might have bad cas number though
				}
			
//				System.out.println(r.CAS_final+"\t"+r.CAS_warning+"\t"+r.omit_reason);
				
				if (r.omit_reason.equals("")) {
					goodCount++;
					fwGood.write(r+"\r\n");
					fwGood.flush();
				} else {//omitted record
					fwBad.write(r+"\r\n");
					fwBad.flush();
				}
//				
			}//end loop over lines

			for (int i=0;i<needSCIFINDER.size();i++) {
				System.out.println("Not in scifinder:\t"+needSCIFINDER.get(i));
			}

			System.out.println("goodCount="+goodCount);
			
			if(printUniqueIORs) {
				Collections.sort(uniqueIOR);
				
				System.out.println("\nunique IORs");
				for (String IOR:uniqueIOR) {
					System.out.println(IOR);
				}
			}
			
		
			fwGood.close();
			fwBad.close();
			br.close();


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Go through skin sensitization records and retrieve LLNA records
	 * @param filepath
	 * @param filepathGood
	 * @param filepathBad
	 */
	public static void goThroughRecordsCreateUniqueChemRegList(String filepathText,String filepathOut,String ECHACASlookup) {
		try {

			Hashtable<String,String>htECHA_CAS_lookup=EChemPortalParse.getECHACASLookup(ECHACASlookup);
			
			BufferedReader br=new BufferedReader(new FileReader(filepathText));
			String headerOld=br.readLine();

			FileWriter fwOut=new FileWriter(filepathOut);

			LinkedList <String>hlist=Utilities.Parse(headerOld, "\t");
			
			fwOut.write(RecordEchemportal2.getHeader()+"\r\n");
						
			
			Hashtable<String,RecordEchemportal2>htUniqueChems=new Hashtable<>();
			
			while (true) {
				//for (int ii=1;ii<=5000;ii++) {
				
				String Line=br.readLine();
				if (Line==null) break;

				LinkedList <String>list=Utilities.Parse(Line, "\t");
				RecordEchemportal2 r = RecordEchemportal2.createRecord(hlist, list);
				
//				System.out.println(r.Record_Number+"\t"+r.Substance_Number);
				
				if (r.Number_type.contentEquals("EC Number")) {
					if (htECHA_CAS_lookup.get(r.Substance_Number)!=null) {
						r.CAS_final=htECHA_CAS_lookup.get(r.Substance_Number);
						System.out.println(r.CAS_final);
					}
				} else if (r.Number_type.contentEquals("CAS Number")) {
					r.CAS_final=r.Substance_Number;
				}
				
				String nameCAS=r.Substance_Name.trim()+"\t"+r.CAS_final.trim();
				
				if (htUniqueChems.get(nameCAS)==null) {
					htUniqueChems.put(nameCAS, r);
					fwOut.write(r+"\r\n");
				}
				
				System.out.println(r.Substance_Name+"\t"+r.CAS_final);
				
			}//end loop over lines

						
		
			fwOut.close();
			br.close();


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	static void omitBasedOnInterpretation(RecordEchemportal2 r,Hashtable<String,String>htDict,String original) {

		String interpretReason="";

		String IOR=r.Interpretation_of_results;
		
		if (Strings.isBlank(r.Interpretation_of_results)) {
			interpretReason="no interpretation";	
		} else if (htDict.get(r.Interpretation_of_results)==null) {
			interpretReason="interpretation not in dictionary: "+r.Interpretation_of_results;
			System.out.println(r.CAS_final+"\t"+interpretReason+"\tOriginal="+original);		
		} else {
			r.FinalScore=htDict.get(r.Interpretation_of_results);
			
			if (r.FinalScore.contentEquals(scoreAmbiguous)) {
				interpretReason="ambiguous interpretation";
			}			
		}

		if (!interpretReason.equals("")) {
			r.omit_reason=append(r.omit_reason,interpretReason);
		}
	}
	
	public static void getInterpretationBasis(RecordEchemportal2 r) {
		
		r.Interpretation_of_results=r.Interpretation_of_results.toLowerCase().replace("migrated information", "").replace("other: " , "").replace("\\","").trim();

		String IOR=r.Interpretation_of_results;
		
		if (IOR.contains("based on")) {
			r.InterpretationBasis=IOR.substring(IOR.indexOf("based on"),IOR.length());
			r.Interpretation_of_results=r.Interpretation_of_results.substring(0,r.Interpretation_of_results.indexOf("based on"));
		} else if (IOR.contains("according to")) {
			r.InterpretationBasis=IOR.substring(IOR.indexOf("according to"),IOR.length());
			r.Interpretation_of_results=r.Interpretation_of_results.substring(0,r.Interpretation_of_results.indexOf("according to"));			
		} else if (IOR.contains("criteria used for")) {
			r.InterpretationBasis=IOR.substring(IOR.indexOf("criteria used for"),IOR.length());
			r.Interpretation_of_results=r.Interpretation_of_results.substring(0,r.Interpretation_of_results.indexOf("criteria used for"));
		} else if (IOR.contains("in accordance with")) {
			r.InterpretationBasis=IOR.substring(IOR.indexOf("in accordance with"),IOR.length());
			r.Interpretation_of_results=r.Interpretation_of_results.substring(0,r.Interpretation_of_results.indexOf("in accordance with"));
		}
		
		r.Interpretation_of_results=r.Interpretation_of_results.trim();
		r.InterpretationBasis=r.InterpretationBasis.trim();

		
//		System.out.println(r.Interpretation_of_results);
		
	}
	
	static  Hashtable <String,String>getECHACASLookup(String filepath) {
		Hashtable <String,String>ht=new Hashtable();

		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));

			String header=br.readLine();

			while (true) {
				String Line=br.readLine();

				if (Line==null) break;

				String EC=Line.substring(0, Line.indexOf("\t"));
				String CAS=Line.substring(Line.indexOf("\t")+1,Line.length());

				ht.put(EC, CAS);

			}



		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return ht;
	}
	
	/**
	 * Omits records where the cas warning has scifinder
	 * 
	 * @param filepathOld
	 * @param filepathNew
	 */
	public static void omitBadScifinderRecords(String filepathOld,String filepathNew) {

		try {

			BufferedReader br=new BufferedReader(new FileReader(filepathOld));
			String headerOld=br.readLine();

			FileWriter fw=new FileWriter(filepathNew);
			fw.write(headerOld+"\r\n");
			LinkedList <String>hlist=Utilities.Parse(headerOld, "\t");

			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				LinkedList <String>list=Utilities.Parse(Line, "\t");
				RecordEchemportal2 r = RecordEchemportal2.createRecord(hlist, list);
				if (!r.CAS_warning.contains("Scifinder")) {
					fw.write(r+"\r\n");
					fw.flush();
				}
			}
			
			fw.close();
			br.close();


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	/**
	 * Checks if any member of vector is contained in the IOR string
	 * 
	 * @param list
	 * @param IOR
	 * @return
	 */
	public static boolean containsSubstring(Vector<String>list,String IOR) {		
		for (String s:list) {
			if (IOR.contains(s)) return true;
		}
		return false;		
	}
	
	public static Vector<RecordEchemportal2> parseExcelEchemportalQueryResult(String filePathExcel,int sheetNumber) {
		try {

			Vector<RecordEchemportal2> records = new Vector<>();
			FileInputStream inputStream = new FileInputStream(new File(filePathExcel));
			DataFormatter formatter = new DataFormatter();

			Workbook workbook = new HSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(sheetNumber);
			
			Row headerRow=sheet.getRow(0);
			
			int colName=getColNum(headerRow,"Substance Name");
			int colNameType=getColNum(headerRow,"Name Type");
			int colSubstanceNumber=getColNum(headerRow,"Substance Number");
			int colNumberType=getColNum(headerRow,"Number type");
			int colValues=getColNum(headerRow,"Values");

			int rows=sheet.getLastRowNum();
			
			for (int i=1;i<=rows;i++) {
			
				Row row=sheet.getRow(i);
				
				RecordEchemportal2 r=new RecordEchemportal2();
				r.Record_Number=i+"";
//				r.Substance_Name=formatter.formatCellValue(row.getCell(colName));
								
				
				byte[] bytes=row.getCell(colName).getStringCellValue().getBytes(StandardCharsets.UTF_8);
				r.Substance_Name=new String (bytes);
				
				
				Hyperlink hl=row.getCell(colName).getHyperlink();
				r.Hyperlink=hl.getAddress();
				
//				System.out.println(r.Substance_Name+"\t"+r.Hyperlink);
				
				r.Name_Type=formatter.formatCellValue(row.getCell(colNameType));
				r.Substance_Number=formatter.formatCellValue(row.getCell(colSubstanceNumber));
				r.Number_type=formatter.formatCellValue(row.getCell(colNumberType));
				
//				System.out.println(r.Substance_Name+"\t"+r.Name_Type+"\t"+r.Substance_Number+"\t"+r.Number_type);
				String Values=formatter.formatCellValue(row.getCell(colValues));
				
				String [] valuesArray=Values.split("\n");
				
				for (String value:valuesArray) {
					value=value.trim();
					if (value==null || !value.contains(":")) continue;
					
					String val="";
					
					if (value.contains(":")) val=value.substring(value.indexOf(":")+1,value.length()).trim();
					
					if(value.contains("Test guideline, Qualifier:")) {
						r.Test_guideline_Qualifier=val;
					} else if (value.contains("Test guideline, Guideline:")) {
						r.Test_guideline_Guideline=val;
					} else if (value.contains("GLP compliance:")) {
						r.GLP_Compliance=val;
					} else if (value.contains("Reliability:")) {
						r.Reliability=val;
					} else if (value.contains("Interpretation of results:")) {
						r.Interpretation_of_results=val;
					} else if (value.contains("Species:")) {
						r.Species=val;
					} else if (value.contains("Type of study:")) {
						r.Type_of_study=val;
					} else if (value.contains("Type of information:")) {
						r.Type_of_information=val;
					} else if (value.contains("Endpoint:")) {
						r.Endpoint=val;
					} else if (value.contains("Type of coverage:")) {
						r.Type_of_coverage=val;
					} else {
						System.out.println(value);
					}					
						
				}
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
	/**
	 * Appends string to current string with a semicolon separation
	 * 
	 * @param omitReason
	 * @param omitReasonNew
	 * @return
	 */
	public static String append(String omitReason,String omitReasonNew) {
		if (omitReason.isEmpty()) return omitReasonNew;
		else return omitReason+";"+omitReasonNew;

	}
	
	
	public static int getColNum(Row row,String name) {
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
}
