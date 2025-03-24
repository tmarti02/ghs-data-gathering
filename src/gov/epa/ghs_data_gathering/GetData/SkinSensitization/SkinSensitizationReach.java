package gov.epa.ghs_data_gathering.GetData.SkinSensitization;

import java.io.BufferedReader;
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

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.formula.MolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import gov.epa.ghs_data_gathering.GetData.ScifinderRecord;
import gov.epa.ghs_data_gathering.GetData.ECHA_IUCLID.RecordECHA;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

/***
 * This class goes through my scrape of the ECHA Reach records (downloaded via ECHA_Dossier.java)
 * 
 * @author Todd Martin
 *
 */
public class SkinSensitizationReach {

	/**
	 * Determine final CAS number from metadata in ECHA record, first priority is from test material info
	 * 
	 * @param r
	 * @param htECHA_CAS
	 */
	void determine_CAS_final(RecordECHA r,Hashtable<String,String>htECHA_CAS) {

		if (!r.test_material_CAS_number.equals("")) {

			if (!r.test_material_EC_number.equals("")) {
				if (htECHA_CAS.get(r.test_material_EC_number)!=null) {
					String CAS2=htECHA_CAS.get(r.test_material_EC_number);
					if (!CAS2.equals(r.test_material_CAS_number)) { 
						r.CAS_warning="CAS material for test material does match CAS for its EC number";
					}
				}
			}
			r.CAS_final=r.test_material_CAS_number;
			r.CAS_final_source="test material CAS number";

			if (!r.CAS_number.equals("-")) {
				if (!r.CAS_final.contentEquals(r.CAS_number)) {
					r.CAS_warning="test material CAS does not match dossier CAS";
				}
			} else if (!r.EC_number.equals("-")) {
				if (htECHA_CAS.get(r.EC_number)!=null) {
					String CAS_From_EC=htECHA_CAS.get(r.EC_number);
					if (!r.CAS_final.contentEquals(CAS_From_EC)) {
						r.CAS_warning="test material CAS does not match CAS corresponding to EC number for Dossier";
					}
				}
			}

			return;
		}

		if (!r.test_material_EC_number.equals("")) {
			if (htECHA_CAS.get(r.test_material_EC_number)!=null) {
				r.CAS_final=htECHA_CAS.get(r.test_material_EC_number);
				r.CAS_final_source="test material EC number";

				if (!r.CAS_number.equals("-")) {
					if (!r.CAS_final.contentEquals(r.CAS_number)) {
						r.CAS_warning="test material CAS does not match dossier CAS";
					}
				} else if (!r.EC_number.equals("-")) {
					if (htECHA_CAS.get(r.EC_number)!=null) {
						String CAS_From_EC=htECHA_CAS.get(r.EC_number);
						if (!r.CAS_final.contentEquals(CAS_From_EC)) {
							r.CAS_warning="test material CAS does not match CAS corresponding to EC number for Dossier";
						}
					}
				}

				return;
			}
		}
		if (!r.CAS_number.equals("-")) {

			if (!r.EC_number.equals("-")) {

				if (htECHA_CAS.get(r.EC_number)!=null) {
					String CAS2=htECHA_CAS.get(r.EC_number);
					if (!CAS2.equals(r.CAS_number)) {
						r.CAS_warning="CAS number does not match CAS for its EC number";
						//						System.out.println(r.CAS_number+"\t"+CAS2);
					}
				}
			}

			r.CAS_final=r.CAS_number;
			r.CAS_final_source="dossier CAS number";
			return;
		}


		if (!r.EC_number.equals("-")) {
			if (htECHA_CAS.get(r.EC_number)!=null) {
				r.CAS_final=htECHA_CAS.get(r.EC_number);
				r.CAS_final_source="dossier EC number";
				return;
			}
		}


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
	
	private void omitBasedOnInterpretation(RecordECHA r) {

		String interpretReason="";

		String IOR=r.interpretation_of_results.toLowerCase();

		if (IOR.equals("sensitising") || IOR.equals("not sensitising")) {
			//OK
		} else if (IOR.indexOf("other: sensitising")>-1 || IOR.indexOf("other: moderate")>-1 || IOR.indexOf("category")>-1) {
			r.interpretation_of_results="sensitising";
		} else if (IOR.equals("other: not sensitising") || 
				IOR.equals("other: not be classified as a skin sensitizer") || 
				IOR.equals("other: not required to be classified") || 
				IOR.equals("other: classification for skin sensitization is not required according to the EU classification criteria") || IOR.equals("other: CLP/EU GHS criteria not met, no classification required according to Regulation (EC) No 1272/2008") ||
				IOR.indexOf("not classified")>-1) {
			r.interpretation_of_results="not sensitising";
		} else if (IOR.equals("")) {
			interpretReason="no interpretation";
		} else if (IOR.indexOf("ambiguous")>-1 || IOR.equals("other: doubtful") || IOR.equals("other: equivocal")) {
			interpretReason ="ambiguous interpretation";
		} else if (IOR.equals("study cannot be used for classification") ) {
			interpretReason="cannot classify";
		} else if (IOR.equals("ghs criteria not met")) {
			interpretReason="GHS criteria not met";
		} else {
			interpretReason="other/mild sensitizer";

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

		if (!interpretReason.equals("")) {
			r.omit_reason=append(r.omit_reason,interpretReason);
		}
		
	}
	private String omitBasedOnScifinderFormula(String omitReason, String formula) {
		if (formula.equals("Unspecified")) {
			omitReason=append(omitReason,"Scifinder:Formula unspecified");
		} else if (formula.indexOf(".")>-1) {
			omitReason=append(omitReason,"Scifinder:Formula indicates salt or mixture");
		} else if (formula.indexOf("(")>-1) {
			omitReason=append(omitReason,"Scifinder:Formula indicates polymer");
		} else {

			//							org.openscience.cdk.tools.manipulator. 
			//							Molecule m=new Molecule();
			//							MFAnalyser mfa = new MFAnalyser(f,m);

			MolecularFormula mf=(MolecularFormula)MolecularFormulaManipulator.getMolecularFormula(formula,DefaultChemObjectBuilder.getInstance());

			AtomContainer m=(AtomContainer) MolecularFormulaManipulator.getAtomContainer(mf);

			if (haveBadElement(m))  {
				omitReason=append(omitReason,"Scifinder:Have bad element");
			} 

			if (!haveElement(m,"C")) {
				omitReason=append(omitReason,"Scifinder:No carbon atoms");
				//								System.out.println(r.CAS_final+"\tNo carbon atoms\t"+f);
			}

			//							System.out.println(r.CAS_final+"\t"+f+"\t"+haveBadElement);	
		}
		return omitReason;
	}
	
	private void omitBasedOnTypeOfStudy(RecordECHA r) {
		
		ArrayList<String>acceptableTypes=new ArrayList<>();
		acceptableTypes.add("llna");
		
		for (String goodType:acceptableTypes) {
			if (r.type_of_study.toLowerCase().contains(goodType)) {
//				System.out.println(r.CAS_final+"\t"+r.type_of_study+"\t"+r.OECD_guideline);
				return;
			}
		}

//		System.out.println(r.CAS_final+"\t"+r.type_of_study);
		r.omit_reason=append(r.omit_reason,"type of study");
	}
	
	
	
	private String omitBasedOnGuideline(String omitReason, RecordECHA r) {
		ArrayList<String>acceptableGuidelines=new ArrayList<>();
		acceptableGuidelines.add("429 (Skin Sensitisation: Local Lymph Node Assay)");
		acceptableGuidelines.add("442B (Skin Sensitization)"); 
		acceptableGuidelines.add("442B (Skin Sensitization: Local Lymph Node Assay: BrdU-ELISA)");
		acceptableGuidelines.add("other:  442B (Skin Sensitisation: Local Lymph Node Assay: BrdU-ELISA");

		if (!acceptableGuidelines.contains(r.OECD_guideline)) {
			omitReason=append(omitReason,"guideline");
		}
		return omitReason;
	}
	private void omitBasedOnReliability(RecordECHA r) {
		ArrayList<String>acceptableReliability=new ArrayList<>();
		acceptableReliability.add("1 (reliable without restriction)");
		acceptableReliability.add("2 (reliable with restrictions)"); 

		if (!acceptableReliability.contains(r.reliability)) {
			r.omit_reason=append(r.omit_reason,"reliability");
		}
		
	}
	
	private void omitBasedOnStudyResultType(RecordECHA r) {
		if (r.study_result_type.toLowerCase().contentEquals("experimental result")) return;
		
		ArrayList<String>resultTypeEstimated=new ArrayList<>();
		resultTypeEstimated.add("(q)sar");
		resultTypeEstimated.add("estimated"); 
		resultTypeEstimated.add("read-across");		
		resultTypeEstimated.add("read across"); 
		
		for (String est:resultTypeEstimated) {
			
//			System.out.println(r.study_result_type.toLowerCase()+"\t"+est);
			
			if (r.study_result_type.toLowerCase().contains(est)) {
				r.omit_reason=append(r.omit_reason,"estimated");
				break;
			}
		}
		
		if (r.study_result_type.equals("")) {
			r.omit_reason=append(r.omit_reason,"unknown study type");
								 
		}
		
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
		Hashtable <String,String>ht=new Hashtable<>();

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
	
	private void getAlternateCASFromScifinder(Hashtable<String, ScifinderRecord> htScifinderRecords, RecordECHA r) {
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
			
			fwGood.write(RecordECHA.getHeader()+"\r\n");
			fwBad.write(RecordECHA.getHeader()+"\r\n");
			

			int goodCount=0;

			Vector<String>needSCIFINDER=new Vector<String>();

			while (true) {
				//for (int ii=1;ii<=5000;ii++) {
				
				String Line=br.readLine();
				if (Line==null) break;

				LinkedList <String>list=Utilities.Parse(Line, "\t");
				RecordECHA r = RecordECHA.createRecord(hlist, list);

//				r.omit_reason = omitBasedOnGuideline(r.omit_reason, r);
				
				determine_CAS_final(r,htECHA_CAS_lookup);
				//fix wrong CAS numbers:
				this.fixCASFinal(r);
			
				//Use main cas number in Scifinder (i.e. not alternate one):
				getAlternateCASFromScifinder(htScifinderRecords, r);
				
				if (!r.CAS_final.isEmpty()) {
					if (htScifinderRecords.get(r.CAS_final)!=null) {
						ScifinderRecord sr=htScifinderRecords.get(r.CAS_final);
						r.formula=sr.Formula;
//						System.out.println(r.CAS_final+"\t"+r.formula);
						r.omit_reason = omitBasedOnScifinderFormula(r.omit_reason, r.formula);
						r.omit_reason = omitBasedOnScifinderClassIdentifier(r.omit_reason, sr);
					} 
					
				} else {
					if (r.omit_reason.equals("")) r.omit_reason=append(r.omit_reason,"can't determine CAS number");
				}
												
				if (r.CAS_final.contains(",") || r.CAS_final.contains("and")) {
					r.omit_reason=append(r.omit_reason,"multiple CAS numbers");
					//TODO handle multiple CAS
				}

				//Omit based on study info:
				omitBasedOnTypeOfStudy(r);				
				omitBasedOnStudyResultType(r);
				omitBasedOnReliability(r);
				omitBasedOnInterpretation(r); 
				
				if (htScifinderRecords.get(r.CAS_final)==null 
						&& !needSCIFINDER.contains(r.CAS_final) 
						&& r.omit_reason.isEmpty() && !r.CAS_final.isEmpty()
						&& !r.CAS_final.contains(",") && !r.CAS_final.contains("and")) {
					needSCIFINDER.add(r.CAS_final);//store in list so can download later, might have bad cas number though
				}
				
				if (r.omit_reason.equals("")) {
					goodCount++;
					fwGood.write(r+"\r\n");
					fwGood.flush();
				} else {//omitted record
					fwBad.write(r+"\r\n");
					fwBad.flush();
				}
			
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
	
	private void fixCASFinal(RecordECHA r) {
		
		//@TODO Put list of corrections in a text file 
		
		
		if (r.CAS_final==null) r.CAS_final="";

		//		System.out.println(r);
		if (r.CAS_final.isEmpty())
			r.CAS_warning="No final CAS available";
		else {		
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

		}
	}

	
	private void omitDuplicateRecords(String filepathOld,String filepathNew) {

		try {

			BufferedReader br=new BufferedReader(new FileReader(filepathOld));
			String headerOld=br.readLine();

			FileWriter fw=new FileWriter(filepathNew);

			fw.write(headerOld+"\r\n");

			LinkedList <String>hlist=Utilities.Parse(headerOld, "\t");

			Hashtable<String,Vector<RecordECHA>>ht=new Hashtable();

			while (true) {
				//				if (counter%1000==0) System.out.println(counter);
				String Line=br.readLine();
				if (Line==null) break;
				LinkedList <String>list=Utilities.Parse(Line, "\t");
				
//				System.out.println(Line);
				
				RecordECHA r = RecordECHA.createRecord(hlist, list);
				String CAS=r.CAS_final;

				if (ht.get(CAS)==null) {
					Vector<RecordECHA>records=new Vector<RecordECHA>();
					records.add(r);
					ht.put(CAS, records);
				} else {
					Vector<RecordECHA>records=ht.get(CAS);
					records.add(r);		
				}

			}
			List<String> tmp = Collections.list(ht.keys());
			Collections.sort(tmp);
			Iterator<String> it = tmp.iterator();

			while(it.hasNext()){
				String CAS =it.next();
				//			    System.out.println(CAS);

				Vector<RecordECHA>records=ht.get(CAS);

				if (records.size()==1) {
					fw.write(records.get(0)+"\r\n");
					fw.flush();

				} else {

					double avgscore=0;

					//figure out avg score from multiple records

					for (int i=0;i<records.size();i++) {

						String scorei=records.get(i).interpretation_of_results;

						if (scorei.equals("sensitising")) {
							avgscore+=1;
						} else if (scorei.equalsIgnoreCase("not sensitising")) {
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
						RecordECHA ri=records.get(i);
						String scorei=ri.interpretation_of_results;

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
	
	public static void main(String[] args) {
		SkinSensitizationReach ssr=new SkinSensitizationReach();
		
		String folder="AA Dashboard\\Data\\ECHA\\REACH_dossier_data\\Skin Sensitization\\Skin sensitization parse";		
		String filepath=folder+"\\echa skin data.txt";		
		String filepathGood=folder+"\\echa skin data-LLNA good.txt";
		String filepathBad=folder+"\\echa skin data-LLNA bad.txt";
		String scifinderFilePath="AA Dashboard\\Data\\ECHA\\REACH_dossier_data\\Skin Sensitization\\Structure data\\SciFinder\\scifinder_chemical_info.txt";
		String ECHACASlookup="AA Dashboard\\Data\\ECHA\\REACH_dossier_data\\echa cas lookup.txt";
		ssr.goThroughSkinSensitizationTextFileLLNA(filepath, filepathGood, filepathBad, scifinderFilePath, ECHACASlookup);
		
		String filepathGoodNoDuplicates=folder+"\\echa skin data-LLNA good-no duplicates.txt";
		ssr.omitDuplicateRecords(filepathGood, filepathGoodNoDuplicates);
	}
}