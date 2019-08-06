package gov.epa.ghs_data_gathering.GetData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.formula.MolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;


import gov.epa.ghs_data_gathering.Utilities.Utilities;

/**
 * @author Todd Martin
 *
 */



public class SkinSensitizationEchemportal {

	
	void print(String s) {
		System.out.println(s);
	}
	
	private boolean haveBadElement(AtomContainer mol) {

		try {

			for (int i=0; i<mol.getAtomCount();i++) {

				String var = mol.getAtom(i).getSymbol();

				// OK: C, H, O, N, F, Cl, Br, I, S, P, Si, As
				if (!var.equals("C") && !var.equals("H") && !var.equals("O")
						&& !var.equals("N") && !var.equals("F")
						&& !var.equals("Cl") && !var.equals("Br")
						&& !var.equals("I") && !var.equals("S")
						&& !var.equals("P") && !var.equals("Si")
						&& !var.equals("As") && !var.equals("Hg")
						&& !var.equals("Sn")) {
					return true;
				}
			}
			return false;

		} catch (Exception e) {
			return true;
		}


	}

	private boolean haveElement(AtomContainer mol,String symbol) {
		try {
			for (int i=0; i<mol.getAtomCount();i++) {
				String var = mol.getAtom(i).getSymbol();
				if (var.equals(symbol)) return true;
			}
			return false;

		} catch (Exception e) {
			return true;
		}
	}
	
	private String lookAtInterpretation(RecordECHA r) {
		String reason="";

		String i=r.interpretation_of_results.toLowerCase();

		if (i.equals("sensitising") || i.equals("not sensitising")) {
			//OK
		} else if (i.indexOf("other: sensitising")>-1 || i.indexOf("other: moderate")>-1 || i.indexOf("category")>-1) {
			r.interpretation_of_results="sensitising";
		} else if (i.equals("other: not sensitising") || 
				i.equals("other: not be classified as a skin sensitizer") || 
				i.equals("other: not required to be classified") || 
				i.equals("other: classification for skin sensitization is not required according to the EU classification criteria") || i.equals("other: CLP/EU GHS criteria not met, no classification required according to Regulation (EC) No 1272/2008") ||
				i.indexOf("not classified")>-1) {
			r.interpretation_of_results="not sensitising";
		} else if (i.equals("")) {
			reason="no interpretation";
		} else if (i.indexOf("ambiguous")>-1 || i.equals("other: doubtful") || i.equals("other: equivocal")) {
			reason ="ambiguous interpretation";
		} else if (i.equals("study cannot be used for classification") ) {
			reason="cannot classify";
		} else if (i.equals("ghs criteria not met")) {
			reason="GHS criteria not met";
		} else {
			reason="other/mild sensitizer";

			//omit these:
			//			other:
			//			other: ‘May cause sensitization by skin contact’
			//			other: delayed contact hypersensitivity
			//			other: irritant
			//			other: irritant
			//			other: irritant
			//			other: irritant
			//			other: irritant
			//			other: May cause sensitization by skin contact
			//			other: may cause sensitization by skin contact
			//			other: May cause sensitization by skin contact
			//			other: mild sensitiser
			//			other: mild sensitiser
			//			other: mild sensitization potential (Grade II)
			//			other: mild sensitizer not requiring classification
			//			other: mildly sensitizing
			//			other: negative with weakly sensitisation potential
			//			other: no photoallergenic properties
			//			other: one batch showed a sensitization effect
			//			other: one batch showed a sensitization effect
			//			other: Positive in 10% of the test animals.
			//			other: Positive in 15% of the test animals.
			//			other: see conclusions
			//			other: slightly
			//			other: sufficient data is not available for the interpretation of results
			//			other: very low sensitization
			//			other: Weak sensitizer

		}


		return reason;

	}
	
	/**
	 * Appends string to current string with a semicolon separation
	 * 
	 * @param omitReason
	 * @param omitReasonNew
	 * @return
	 */
	String append(String omitReason,String omitReasonNew) {
		if (omitReason.isEmpty()) return omitReasonNew;
		else return omitReason+";"+omitReasonNew;

	}
	
	
	private String omitBasedOnScifinderFormula(String casWarning, String formula) {
		if (formula.equals("Unspecified")) {
			casWarning=append(casWarning,"Scifinder:Formula unspecified");
		} else if (formula.indexOf(".")>-1) {
			casWarning=append(casWarning,"Scifinder:Formula indicates salt or mixture");
		} else if (formula.indexOf("(")>-1) {
			casWarning=append(casWarning,"Scifinder:Formula indicates polymer");
		} else {

			//							org.openscience.cdk.tools.manipulator. 
			//							Molecule m=new Molecule();
			//							MFAnalyser mfa = new MFAnalyser(f,m);

			MolecularFormula mf=(MolecularFormula)MolecularFormulaManipulator.getMolecularFormula(formula,DefaultChemObjectBuilder.getInstance());

			AtomContainer m=(AtomContainer) MolecularFormulaManipulator.getAtomContainer(mf);

			if (haveBadElement(m))  {
				casWarning=append(casWarning,"Scifinder:Have bad element");
			} 

			if (!haveElement(m,"C")) {
				casWarning=append(casWarning,"Scifinder:No carbon atoms");
				//								System.out.println(r.CAS_final+"\tNo carbon atoms\t"+f);
			}

			//							System.out.println(r.CAS_final+"\t"+f+"\t"+haveBadElement);	
		}
		return casWarning;
	}
	
	
	
	
	private String omitBasedOnScifinderClassIdentifier(String omitReason, ScifinderRecord sr) {
		String cid=sr.Class_Identifier;
		if (cid!=null && !cid.equals("")) {

			if (cid.indexOf("Incompletely Defined Substance")>-1) {
				omitReason=append(omitReason,"Scifinder:Incompletely Defined Substance");
			} else if (cid.indexOf("Mineral")>-1) {
				omitReason=append(omitReason,"Scifinder:Mineral");
			} else if (cid.indexOf("Coordination Compound")>-1) {
				omitReason=append(omitReason,"Scifinder:Coordination Compound");
			} else if (cid.indexOf("Inorganic")>-1) {
				omitReason=append(omitReason,"Scifinder:Inorganic");
			} else if (cid.indexOf("Polymer")>-1) {
				omitReason=append(omitReason,"Scifinder:Polymer");
			} else {
				//								System.out.println(r.CAS_final+"\t"+c);	
			}

		}
		return omitReason;
	}
	
	private Hashtable <String,String>getECHACASLookup(String filepath) {
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
	 * Fixes CAS so that uses CAS that's listed as Scifinder's main cas and not alternate cas in scifinder 
	 */
	
	private void getAlternateCASFromScifinder(Hashtable<String, ScifinderRecord> htScifinderRecords, RecordEchemportal2 r) {
		List<String> tmp = Collections.list(htScifinderRecords.keys());
		Iterator<String> it = tmp.iterator();
		
		while(it.hasNext()){
			ScifinderRecord sr=htScifinderRecords.get(it.next());
			
			if (sr.Alternate_Registry_Numbers.isEmpty()) continue;

//			System.out.println(r.CAS_final+"\talternate reg numbers:"+sr.Alternate_Registry_Numbers);
			
			String [] altCAS=sr.Alternate_Registry_Numbers.split(",");

			for (int i=0;i<altCAS.length;i++) {
				if(altCAS[i].equals(r.CAS_final)) {
//					System.out.println("Match:"+r.CAS_final+"\t"+sr.Registry_Number+"\t"+altCAS[i]);
					r.CAS_warning=append(r.CAS_warning, "Alternative CAS ("+r.CAS_final+") replaced with Scifinder main CAS");
					r.CAS_final=sr.Registry_Number;
					return;
				}
			}
			
		}
	}

	/**
	 * Loads scifinder structure data from file
	 * 
	 * @param filepath
	 * @return
	 */
	Hashtable <String,ScifinderRecord>getScifinderData(String filepath) {
		Hashtable <String,ScifinderRecord>ht=new Hashtable();

		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));

			String header=br.readLine();

			header=header.replace(" ", "_");
			header=header.replace("(s)", "s");

			LinkedList<String>hl=gov.epa.ghs_data_gathering.Utilities.Utilities.Parse3(header, ",");

			//			System.out.println(header);

			while (true) {
				String Line=br.readLine();
				if (Line==null) break;

				LinkedList<String>l=gov.epa.ghs_data_gathering.Utilities.Utilities.Parse3(Line, ",");

				ScifinderRecord sr=new ScifinderRecord();
				for (int i=0;i<sr.varlist.length;i++) {

					String fieldName=sr.varlist[i];

					for (int j=0;j<hl.size();j++) {
						if (hl.get(j).equals(fieldName)) {
							try {
								String value=l.get(j);

								Field myField =sr.getClass().getField(sr.varlist[i]);
								myField.set(sr, value);

							} catch (Exception ex){
								ex.printStackTrace();
							}
							break;
						}
					}


				}
				//				System.out.println(sr.Registry_Number+"\t"+sr.Formula);

				ht.put(sr.Registry_Number, sr);
				//				System.out.println(sr);

			}



		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return ht;
	}
	
	/**
	 * Go through skin sensitization records and retrieve LLNA records
	 * @param filepath
	 * @param filepathGood
	 * @param filepathBad
	 */
	public void goThroughSkinSensitizationTextFileLLNA(String filepath,String filepathGood,String filepathBad,String scifinderFilePath,String ECHACASlookup) {
		try {

			Hashtable<String,ScifinderRecord>htScifinderRecords=this.getScifinderData(scifinderFilePath);
			Hashtable<String,String>htECHA_CAS_lookup=this.getECHACASLookup(ECHACASlookup);
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			String headerOld=br.readLine();

			FileWriter fwGood=new FileWriter(filepathGood);
			FileWriter fwBad=new FileWriter(filepathBad);

			LinkedList <String>hlist=Utilities.Parse(headerOld, "\t");
			
			fwGood.write(RecordEchemportal2.getHeader()+"\r\n");
			fwBad.write(RecordEchemportal2.getHeader()+"\r\n");
			
			int goodCount=0;

			Vector<String>needSCIFINDER=new Vector<String>();

			while (true) {
				//for (int ii=1;ii<=5000;ii++) {
				
				String Line=br.readLine();
				if (Line==null) break;

				LinkedList <String>list=Utilities.Parse(Line, "\t");
				RecordEchemportal2 r = RecordEchemportal2.createRecord(hlist, list);
				
				if (r.Number_type.contentEquals("EC Number")) {
					if (htECHA_CAS_lookup.get(r.Substance_Number)!=null) {
						r.CAS_final=htECHA_CAS_lookup.get(r.Substance_Number);
					}
				} else if (r.Number_type.contentEquals("CAS Number")) {
					r.CAS_final=r.Substance_Number;
				}
				
//				//fix wrong CAS numbers:
				this.fixCASFinal(r,htScifinderRecords);
//			
//				//Use main cas number in Scifinder (i.e. not alternate one):
//				
//				//Omit based on study info:
				omitBasedOnTypeOfStudy(r);				
				omitBasedOnStudyResultType(r);//may not be needed since I filtered for this on echemportal website
				omitBasedOnGuideline(r);
				omitBasedOnReliability(r);
				omitBasedOnInterpretation(r); 
				
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
		
			fwGood.close();
			fwBad.close();
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
	boolean containsSubstring(Vector<String>list,String IOR) {		
		for (String s:list) {
			if (IOR.contains(s)) return true;
		}
		return false;		
	}
	
	private void omitBasedOnInterpretation(RecordEchemportal2 r) {

		r.Interpretation_of_results=r.Interpretation_of_results.replace("Migrated information", "");
		
		String interpretReason="";

		String IOR=r.Interpretation_of_results.toLowerCase();
		IOR=IOR.replace("other:","");
		
		
		getInterpretationBasis(r, IOR);

//		if (IOR.contains("in accordance with")) IOR=IOR.substring(0,IOR.indexOf("in accordance with"));
		
		IOR=IOR.trim();
				
		Vector<String>notSensitizingStrings=new Vector<>();
		notSensitizingStrings.add("not sensitising");
		notSensitizingStrings.add("not sensiting");
		notSensitizingStrings.add("not a sensitizer");		
		notSensitizingStrings.add("not sensitizer");
		notSensitizingStrings.add("not sensitizing");
		notSensitizingStrings.add("not classified");
		notSensitizingStrings.add("non-sensitizer");
		notSensitizingStrings.add("no classification required");
		notSensitizingStrings.add("no skin sensitization potential");		
		notSensitizingStrings.add("ghs criteria not met");
		notSensitizingStrings.add("not be classified as a skin sensitizer");
		notSensitizingStrings.add("not required to be classified");
		notSensitizingStrings.add("classification for skin sensitization is not required"); 
		notSensitizingStrings.add("clp criteria not met");
		notSensitizingStrings.add("no skin sensitisation potential");
		notSensitizingStrings.add("no evidence of skin sensitization");
		notSensitizingStrings.add("no skin sensitization potencial");
		notSensitizingStrings.add("does not meet the criteria for classification");
		notSensitizingStrings.add("not induce delayed contact hypersensivity");
		notSensitizingStrings.add("eu classification criteria not met");		
		notSensitizingStrings.add("no indication for a specific skin sensitizing effect");
		notSensitizingStrings.add("no sensitizing potential");
		notSensitizingStrings.add("not a skin sensitiser");
		notSensitizingStrings.add("not irritant or sensitising");
		notSensitizingStrings.add("not skin sensitising");
		notSensitizingStrings.add("not skin sensitizer ");
											
		Vector<String>sensitizingStrings=new Vector<>();
		sensitizingStrings.add("sensitizing");
		sensitizingStrings.add("sensitising weak");
		sensitizingStrings.add("skin sensitizing");		
		sensitizingStrings.add("sensitising");
		sensitizingStrings.add("sensitizer");		
		sensitizingStrings.add("sensitiser");		
		sensitizingStrings.add("category");
		sensitizingStrings.add("cat.");
		sensitizingStrings.add("cat 1");
		sensitizingStrings.add("1b");
		sensitizingStrings.add("weak dermal sensitization potential");
		sensitizingStrings.add("classified under eu criteria");
		
		Vector<String>ambiguousStrings=new Vector<>();		
		ambiguousStrings.add("ambiguous");
		ambiguousStrings.add("ambigous");
		ambiguousStrings.add("invalid");
		ambiguousStrings.add("inconclusive");
		ambiguousStrings.add("not reliable");
		ambiguousStrings.add("not conclusive");
		ambiguousStrings.add("equivocal");
		ambiguousStrings.add("false positive");
		ambiguousStrings.add("study cannot be used for classification");

		if (containsSubstring(ambiguousStrings,IOR)) {
			r.FinalScore ="ambiguous";
			interpretReason="ambiguous interpretation";
		} else if (IOR.isEmpty() || IOR.isBlank()) {
			r.FinalScore="no score";
			interpretReason="no interpretation";			
		} else if (containsSubstring(notSensitizingStrings,IOR)) {
			r.FinalScore="not sensitizing";			
		} else if (containsSubstring(sensitizingStrings,IOR)) {
//			System.out.println(IOR);
			r.FinalScore="sensitizing";
		} else {
//			System.out.println(IOR+"\t***"+r.Interpretation_of_results);
//			System.out.println(r.CAS_final+"\t"+IOR);
			interpretReason="bad IOR:"+IOR;
		}

		if (!interpretReason.equals("")) {
			r.omit_reason=append(r.omit_reason,interpretReason);
		}
		
	}

	private void getInterpretationBasis(RecordEchemportal2 r, String IOR) {
		//TODO- store in separate variable
		if (IOR.contains("based on")) {
			r.InterpretationBasis=IOR.substring(IOR.indexOf("based on"),IOR.length());
//			System.out.println(r.Interpretation_of_results);
		}
		
		if (r.Interpretation_of_results.contains("based on")) {
			r.Interpretation_of_results=r.Interpretation_of_results.substring(0,r.Interpretation_of_results.indexOf("based on"));
		}
		
		
		if (IOR.contains("according to")) {
			r.InterpretationBasis=IOR.substring(IOR.indexOf("according to"),IOR.length());
//			System.out.println(r.InterpretationBasis);
		}
		
		if (r.Interpretation_of_results.contains("according to")) {
			r.Interpretation_of_results=r.Interpretation_of_results.substring(0,r.Interpretation_of_results.indexOf("according to"));			
		}
		
		if (r.Interpretation_of_results.contains("According to")) {
			r.Interpretation_of_results=r.Interpretation_of_results.substring(0,r.Interpretation_of_results.indexOf("According to"));			
		}

		
		if (IOR.contains("criteria used for")) {
			r.InterpretationBasis=IOR.substring(IOR.indexOf("criteria used for"),IOR.length());
//			System.out.println(r.InterpretationBasis);
		}
		
		if (r.Interpretation_of_results.contains("Criteria used for")) {
			r.Interpretation_of_results=r.Interpretation_of_results.substring(0,r.Interpretation_of_results.indexOf("Criteria used for"));
		}
		
		if (IOR.contains("in accordance with")) {
			r.InterpretationBasis=IOR.substring(IOR.indexOf("in accordance with"),IOR.length());
//			System.out.println(r.InterpretationBasis);
		}
		
		if (r.Interpretation_of_results.contains("in accordance with")) {
			r.Interpretation_of_results=r.Interpretation_of_results.substring(0,r.Interpretation_of_results.indexOf("in accordance with"));			
		}

		
//		System.out.println(r.Interpretation_of_results);
		
	}
	private void omitBasedOnReliability(RecordEchemportal2 r) {
		ArrayList<String>acceptableReliability=new ArrayList<>();
		acceptableReliability.add("1 (reliable without restriction)");
		acceptableReliability.add("2 (reliable with restrictions)"); 

		if (!acceptableReliability.contains(r.Reliability)) {
			r.omit_reason=append(r.omit_reason,"reliability");
		}
		
		
	}
	
	private void omitBasedOnGuideline(RecordEchemportal2 r) {
		Vector<String>acceptableGuidelines=new Vector<>();
		acceptableGuidelines.add("429");
		acceptableGuidelines.add("442B");
		acceptableGuidelines.add("442 B");

		if (!containsSubstring(acceptableGuidelines, r.Test_guideline_Guideline)) {
			r.omit_reason=append(r.omit_reason,"guideline");
		}
	}
	private void omitBasedOnStudyResultType(RecordEchemportal2 r) {
		if (r.Type_of_information.toLowerCase().contentEquals("experimental result")) return;
		
		ArrayList<String>resultTypeEstimated=new ArrayList<>();
		resultTypeEstimated.add("(q)sar");
		resultTypeEstimated.add("estimated"); 
		resultTypeEstimated.add("read-across");		
		resultTypeEstimated.add("read across"); 
		
		for (String est:resultTypeEstimated) {
			
//			System.out.println(r.study_result_type.toLowerCase()+"\t"+est);
			
			if (r.Type_of_information.toLowerCase().contains(est)) {
				r.omit_reason=append(r.omit_reason,"estimated");
				break;
			}
		}
		
		if (r.Type_of_information.equals("")) {
			r.omit_reason=append(r.omit_reason,"unknown study type");
								 
		}
		
	}
	
	private void omitBasedOnTypeOfStudy(RecordEchemportal2 r) {
		
		ArrayList<String>acceptableTypes=new ArrayList<>();
		acceptableTypes.add("llna");
		
		for (String goodType:acceptableTypes) {
			if (r.Type_of_study.toLowerCase().contains(goodType)) {
//				System.out.println(r.CAS_final+"\t"+r.Type_of_study+"\t"+r.Test_guideline_Guideline);
				return;
			}
		}

//		System.out.println(r.CAS_final+"\t"+r.Type_of_study);
		
		r.omit_reason=append(r.omit_reason,"type of study");
	}
	private void fixCASFinal(RecordEchemportal2 r,Hashtable<String, ScifinderRecord>htScifinderRecords) {
		
		//@TODO Put list of corrections in a text file 
		
		
		if (r.CAS_final==null) r.CAS_final="";

		//		System.out.println(r);
		if (!r.CAS_final.isEmpty()) {
			r.CAS_final=r.CAS_final.trim();

			if (r.CAS_final.equals("133-06-02")) {
				r.CAS_final="133-06-2";
			} else if (r.CAS_final.equals("68037-0-14")) {
				r.CAS_final="68037-01-4";
			} else if (r.CAS_final.contentEquals("188416- 34-4")) {
				r.CAS_final="188416-34-4";
			} else if (r.CAS_final.contentEquals("Basic Violet 1: 8004-87-3")) {
				r.CAS_final="8004-87-3";
			}
			getAlternateCASFromScifinder(htScifinderRecords, r);

			if (htScifinderRecords.get(r.CAS_final)!=null) {
				ScifinderRecord sr=htScifinderRecords.get(r.CAS_final);
				r.formula=sr.Formula;
				r.CAS_warning=omitBasedOnScifinderFormula(r.CAS_warning, r.formula);
				r.CAS_warning=omitBasedOnScifinderClassIdentifier(r.CAS_warning, sr);
			}

		} else {
			r.CAS_warning="No final CAS available";
			r.omit_reason=append(r.omit_reason,r.CAS_warning);

		}
	}

	
	/**
	 * This class tries to remove duplicates by keeping one record if there is >= 80% consensus
	 * 
	 * TODO redo with  com.google.common.collect.Multimap for simpler code
	 * 
	 * @param filepathOld
	 * @param filepathNew
	 */
	private void omitDuplicateRecords(String filepathOld,String filepathNew) {

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

					//figure out avg score from multiple records

					for (int i=0;i<records.size();i++) {

						String scorei=records.get(i).FinalScore;

						if (scorei.equals("sensitizing")) {
							avgscore+=1;
						} else if (scorei.equalsIgnoreCase("not sensitizing")) {
							//do nothing
						} else {
							System.out.println(CAS+"\tinvalid\t"+scorei);
						}
					}

					avgscore/=(double)records.size();

					double finalScore=-1;

					double frac=0.2;//fraction that can be conflicting

					if (avgscore<=frac) {
						finalScore=0;
					} else if (avgscore>=(1-frac)) {
						finalScore=1;
					}

					if (finalScore==-1) {
						System.out.println(CAS+"\tconflicting");
						continue;
					} 


					for (int i=0;i<records.size();i++) {
						RecordEchemportal2 ri=records.get(i);
						String scorei=ri.Interpretation_of_results;

						//output the first record that matches the average score result:
						if (finalScore==0) {
							if (scorei.equals("not sensitising")) {
								fw.write(ri+"\r\n");
								fw.flush();
								break;
							}
						} else if (finalScore==1) {
							if (scorei.equals("sensitising")) {
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
	

	/**
	 * This class tries to remove duplicates by keeping one record if there is >= 80% consensus
	 * 
	 * TODO redo with  com.google.common.collect.Multimap for simpler code
	 * 
	 * @param filepathOld
	 * @param filepathNew
	 */
	private void omitBadScifinderRecords(String filepathOld,String filepathNew) {

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
	
	Vector<RecordEchemportal2> parseExcelEchemportalQueryResult(String filePathExcel) {
		try {

			Vector<RecordEchemportal2> records = new Vector<>();
			FileInputStream inputStream = new FileInputStream(new File(filePathExcel));
			DataFormatter formatter = new DataFormatter();

			Workbook workbook = new HSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(0);
			
			Row headerRow=sheet.getRow(0);
			
			int colName=getColNum(headerRow,"Substance Name");
			int colNameType=getColNum(headerRow,"Name Type");
			int colSubstanceNumber=getColNum(headerRow,"Substance Number");
			int colNumberType=getColNum(headerRow,"Number type");
			int colValues=getColNum(headerRow,"Values");

			int rows=sheet.getLastRowNum();
			
			for (int i=1;i<rows;i++) {
			
				Row row=sheet.getRow(i);
				
				RecordEchemportal2 r=new RecordEchemportal2();
				r.Substance_Name=formatter.formatCellValue(row.getCell(colName));
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
	
	void writeToFile(String filepath,Vector<RecordEchemportal2>records) {
		try {
			
			FileWriter fw=new FileWriter(filepath);
			fw.write(RecordEchemportal2.getHeader()+"\r\n");
			
			for (RecordEchemportal2 r:records) {
				fw.write(r+"\r\n");
			}
			
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		SkinSensitizationEchemportal ssr=new SkinSensitizationEchemportal();
		
//		String folder="AA Dashboard\\Data\\ECHA\\REACH_dossier_data\\ECHEMPORTAL DATA From Jeremy";		
//		String filepath=folder+"\\SkinSensitizationNewHeader.tsv";		
//
		
		String folder="AA Dashboard\\Data\\ECHA\\REACH_dossier_data\\eChemPortal\\";
		String filepathExcel=folder+"query2.xls";
		String filepathText=folder+"query2_parsed.txt";

//		Vector<RecordEchemportal2>records=ssr.parseExcelEchemportalQueryResult(filepathExcel);
//		ssr.writeToFile(filepathText, records);
		
		
		String filepathGood=folder+"echemportal skin data-LLNA good.txt";
		String filepathBad=folder+"echemportal skin data-LLNA bad.txt";
		
		String scifinderFilePath=Scifinder.folderScifinder+"\\scifinder_chemical_info.txt";
		
		String ECHACASlookup="AA Dashboard\\Data\\ECHA\\REACH_dossier_data\\echa cas lookup.txt";
		ssr.goThroughSkinSensitizationTextFileLLNA(filepathText, filepathGood, filepathBad, scifinderFilePath, ECHACASlookup);
		
		String filepathGoodNoDuplicates=folder+"\\echemportal skin data-LLNA good-no duplicates.txt";
		ssr.omitDuplicateRecords(filepathGood, filepathGoodNoDuplicates);
		
		String filepathOmitBadScifinder=folder+"\\echemportal skin data-LLNA good-no duplicates-omit bad scifinder.txt";		
		ssr.omitBadScifinderRecords(filepathGoodNoDuplicates, filepathOmitBadScifinder);

	}
}