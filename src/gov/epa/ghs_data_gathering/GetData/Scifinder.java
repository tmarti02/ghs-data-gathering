package gov.epa.ghs_data_gathering.GetData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.openscience.cdk.AtomContainer;
//import org.openscience.cdk.MoleculeSet;
//import org.openscience.cdk.interfaces.IMolecule;
//import org.openscience.cdk.io.MDLWriter;
//import GetData.MDLReader;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.formula.MolecularFormula;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import gov.epa.exp_data_gathering.parse.OECD_Toolbox.RecordOECD_Toolbox;
import gov.epa.ghs_data_gathering.Database.MySQL_DB;
import gov.epa.ghs_data_gathering.Utilities.MolFileUtilities;
import gov.epa.ghs_data_gathering.Utilities.TESTConstants;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

public class Scifinder {

	public static String folderScifinder = "AA Dashboard\\Structure data\\SciFinder";

	
	/**
	 * Fixes CAS so that uses CAS that's listed as Scifinder's main cas and not alternate cas in scifinder 
	 */
	
//	public static void getAlternateCASFromScifinder(Hashtable<String, ScifinderRecord> htScifinderRecords, RecordEchemportal2 r) {
//		List<String> tmp = Collections.list(htScifinderRecords.keys());
//		Iterator<String> it = tmp.iterator();
//		
//		while(it.hasNext()){
//			ScifinderRecord sr=htScifinderRecords.get(it.next());
//			
////			System.out.println(r.CAS_final+"\talternate reg numbers:"+sr.Alternate_Registry_Numbers);
//			
//			String [] altCAS=sr.Alternate_Registry_Numbers.split(",");
//			
//			for (int i=0;i<altCAS.length;i++) {
//				if(altCAS[i].trim().equals(r.CAS_final)) {
//					System.out.println("Alternate CAS Match:"+r.CAS_final+"\t"+sr.Registry_Number+"\t"+altCAS[i]);
//					r.CAS_warning=EChemPortalParse.append(r.CAS_warning, "Alternative CAS ("+r.CAS_final+") replaced");
//					r.CAS_final=sr.Registry_Number;
//					return;
//				}
//			}
//			
//			String [] deletedCAS=sr.Deleted_Registry_Numbers.split(",");
//			
//			for (int i=0;i<deletedCAS.length;i++) {
//				if(deletedCAS[i].trim().equals(r.CAS_final)) {
//					System.out.println("Deleted CAS Match:"+r.CAS_final+"\t"+sr.Registry_Number+"\t"+deletedCAS[i]);
//					r.CAS_warning=EChemPortalParse.append(r.CAS_warning, "Deleted CAS ("+r.CAS_final+") replaced");
//					r.CAS_final=sr.Registry_Number;
//					return;
//				}
//			}
//			
//		}
//	}
	
	/**
	 * Fixes CAS so that uses CAS that's listed as Scifinder's main cas and not alternate cas in scifinder 
	 */
	
	public static void getAlternateCASFromScifinderOECD(Hashtable<String, ScifinderRecord> htScifinderRecords, RecordOECD_Toolbox r) {
		List<String> tmp = Collections.list(htScifinderRecords.keys());
		Iterator<String> it = tmp.iterator();
		
		while(it.hasNext()){
			ScifinderRecord sr=htScifinderRecords.get(it.next());
			
//			System.out.println(r.CAS_final+"\talternate reg numbers:"+sr.Alternate_Registry_Numbers);
			
			String [] altCAS=sr.Alternate_Registry_Numbers.split(",");
			
			for (int i=0;i<altCAS.length;i++) {
				if(altCAS[i].trim().equals(r.CAS)) {
					System.out.println("Alternate CAS Match:"+r.CAS+"\t"+sr.Registry_Number+"\t"+altCAS[i]);
					r.scifinderWarning=EChemPortalParse.append(r.scifinderWarning, "Alternative CAS ("+r.CAS+") replaced");
					r.CAS=sr.Registry_Number;
					return;
				}
			}
			
			String [] deletedCAS=sr.Deleted_Registry_Numbers.split(",");
			
			for (int i=0;i<deletedCAS.length;i++) {
				if(deletedCAS[i].trim().equals(r.CAS)) {
					System.out.println("Deleted CAS Match:"+r.CAS+"\t"+sr.Registry_Number+"\t"+deletedCAS[i]);
					r.scifinderWarning=EChemPortalParse.append(r.scifinderWarning, "Deleted CAS ("+r.CAS+") replaced");
					r.CAS=sr.Registry_Number;
					return;
				}
			}
			
		}
	}
	
	
	void getCASRangeSDF(String folder, String filename) {

		AtomContainerSet moleculeSet = MolFileUtilities.LoadFromSDF3(folder + "/" + filename);

		AtomContainer mfirst = (AtomContainer) moleculeSet.getAtomContainer(0);
		AtomContainer mlast = (AtomContainer) moleculeSet.getAtomContainer(moleculeSet.getAtomContainerCount() - 1);

		String CASfirst = (String) mfirst.getProperty("cas.rn");
		String CASlast = (String) mlast.getProperty("cas.rn");

		String result = CASlast.substring(4, CASlast.length()) + " to " + CASfirst.substring(4, CASfirst.length());

		// System.out.println(result);

		System.out.println(filename + "\t" + result);
		// System.out.println("New name:"+result);

		File file = new File(folder + "/" + filename);
		file.renameTo(new File(folder + "/" + result + ".sdf"));

		// return moleculeSet;

	}
	
	
	
	
//	public static void fixCASFinal(RecordEchemportal2 r,Hashtable<String, ScifinderRecord>htScifinderRecords) {
//		
//		//@TODO Put list of corrections in a text file 
//		
//		
//		if (r.CAS_final==null) r.CAS_final="";
//
//		//		System.out.println(r);
//		if (!r.CAS_final.isEmpty()) {
//			r.CAS_final=r.CAS_final.trim();
//
//			if (r.CAS_final.equals("133-06-02")) {
//				r.CAS_final="133-06-2";
//			} else if (r.CAS_final.equals("68037-0-14")) {
//				r.CAS_final="68037-01-4";
//			} else if (r.CAS_final.contentEquals("188416- 34-4")) {
//				r.CAS_final="188416-34-4";
//			} else if (r.CAS_final.contentEquals("Basic Violet 1: 8004-87-3")) {
//				r.CAS_final="8004-87-3";
//			}
//			getAlternateCASFromScifinder(htScifinderRecords, r);
//
//			if (htScifinderRecords.get(r.CAS_final)!=null) {
//				ScifinderRecord sr=htScifinderRecords.get(r.CAS_final);
//				r.formula=sr.Formula;
//				r.CAS_warning=omitBasedOnScifinderFormula(r.CAS_warning, r.formula);
//				r.CAS_warning=omitBasedOnScifinderClassIdentifier(r.CAS_warning, sr);
//			}
//
//		} else {
//			r.CAS_warning="No final CAS available";
////			r.omit_reason=EChemPortalParse.append(r.omit_reason,r.CAS_warning);
//
//		}
//	}
	
	private static boolean haveBadElement(AtomContainer mol) {

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
	public static String omitBasedOnScifinderFormula(String casWarning, String formula) {
		if (formula.equals("Unspecified")) {
			casWarning=EChemPortalParse.append(casWarning,"Scifinder:Formula unspecified");
		} else if (formula.indexOf(".")>-1) {
			casWarning=EChemPortalParse.append(casWarning,"Scifinder:Formula indicates salt or mixture");
		} else if (formula.indexOf("(")>-1) {
			casWarning=EChemPortalParse.append(casWarning,"Scifinder:Formula indicates polymer");
		} else {

			//							org.openscience.cdk.tools.manipulator. 
			//							Molecule m=new Molecule();
			//							MFAnalyser mfa = new MFAnalyser(f,m);

			MolecularFormula mf=(MolecularFormula)MolecularFormulaManipulator.getMolecularFormula(formula,DefaultChemObjectBuilder.getInstance());

			AtomContainer m=(AtomContainer) MolecularFormulaManipulator.getAtomContainer(mf);

			if (haveBadElement(m))  {
				casWarning=EChemPortalParse.append(casWarning,"Scifinder:Have bad element");
			} 

			if (!haveElement(m,"C")) {
				casWarning=EChemPortalParse.append(casWarning,"Scifinder:No carbon atoms");
				//								System.out.println(r.CAS_final+"\tNo carbon atoms\t"+f);
			}

			//							System.out.println(r.CAS_final+"\t"+f+"\t"+haveBadElement);	
		}
		return casWarning;
	}
	
	static boolean haveElement(AtomContainer mol,String symbol) {
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
	
	public static String omitBasedOnScifinderClassIdentifier(String omitReason, ScifinderRecord sr) {
		String cid=sr.Class_Identifier;
		if (cid!=null && !cid.equals("")) {

			if (cid.indexOf("Incompletely Defined Substance")>-1) {
				omitReason=EChemPortalParse.append(omitReason,"Scifinder:Incompletely Defined Substance");
			} else if (cid.indexOf("Mineral")>-1) {
				omitReason=EChemPortalParse.append(omitReason,"Scifinder:Mineral");
			} else if (cid.indexOf("Coordination Compound")>-1) {
				omitReason=EChemPortalParse.append(omitReason,"Scifinder:Coordination Compound");
			} else if (cid.indexOf("Inorganic")>-1) {
				omitReason=EChemPortalParse.append(omitReason,"Scifinder:Inorganic");
			} else if (cid.indexOf("Polymer")>-1) {
				omitReason=EChemPortalParse.append(omitReason,"Scifinder:Polymer");
			} else {
				//								System.out.println(r.CAS_final+"\t"+c);	
			}

		}
		return omitReason;
	}
	
	/**
	 * Loads scifinder structure data from file
	 * 
	 * @param filepath
	 * @return
	 */
	public static Hashtable <String,ScifinderRecord>getScifinderData(String filepath) {
		Hashtable <String,ScifinderRecord>ht=new Hashtable();

		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));

			String header=br.readLine();

			header=header.replace(" ", "_");
			header=header.replace("(s)", "s");

			LinkedList<String>hl=Utilities.Parse3(header, ",");

			//			System.out.println(header);

			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				LinkedList<String>l=Utilities.Parse3(Line, ",");

				ScifinderRecord sr = createScifinderRecord(hl, l);
				//				System.out.println(sr.Registry_Number+"\t"+sr.Formula);
				ht.put(sr.Registry_Number, sr);
				//				System.out.println(sr);

			}

			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return ht;
	}


	private static ScifinderRecord createScifinderRecord(LinkedList<String> hl, LinkedList<String> l) {
		ScifinderRecord sr=new ScifinderRecord();
		
//		for (int i=0;i<sr.varlist.length;i++) {
//
//			String fieldName=sr.varlist[i];
//
//			for (int j=0;j<hl.size();j++) {
//				System.out.println(hl.get(j)+"\t"+fieldName);
//				
//				if (hl.get(j).equals(fieldName)) {
//					try {
//						String value=l.get(j);
//
//						Field myField =sr.getClass().getField(sr.varlist[i]);
//						myField.set(sr, value);
//
//					} catch (Exception ex){
//						ex.printStackTrace();
//					}
//					break;
//				}
//			}
//		}
		
		for (int i=0;i<hl.size();i++) {

			String headerName=hl.get(i);
			String value=l.get(i);
					
			try {
				Field myField =sr.getClass().getField(headerName.replace(" ", "_"));
				myField.set(sr, value);
			} catch (java.lang.NoSuchFieldException nsfe) {
				
			} catch (Exception ex){
				ex.printStackTrace();
			}
			
//			System.out.println(headerName+"\t"+value);
				
			
		}

		
		
		
		return sr;
	}
	

	void convertSDFtoMolFiles2(String filepath, String outputFolder) {

		try {

			File file = new File(filepath);

			IteratingSDFReader reader = new IteratingSDFReader(new FileInputStream(file),
					DefaultChemObjectBuilder.getInstance());

			while (reader.hasNext()) {
				IAtomContainer m = (IAtomContainer) reader.next();

				if (m == null || m.getAtomCount() == 0)
					break;

				String CAS = ((String) m.getProperty("CASRN"));
				m.removeProperty("CASRN");
				m.removeProperty("INPUT");
				m.setProperty("CAS", CAS);

				FileWriter fw = new FileWriter(outputFolder + "/" + CAS + ".mol");

				MDLV2000Writer mw = new MDLV2000Writer(fw);
//				mw.setSdFields(m.getProperties());
				mw.writeMolecule(m);

				fw.close();

			} // end while true;

		} catch (Exception e) {
			e.printStackTrace();

		}

//		return moleculeSet;

	}

	void convertSDFtoMolFiles(String filepath, String outputFolder) {

		try {

			File file = new File(filepath);

			IteratingSDFReader reader = new IteratingSDFReader(new FileInputStream(file),
					DefaultChemObjectBuilder.getInstance());

			while (reader.hasNext()) {
				IAtomContainer m = (IAtomContainer) reader.next();

				if (m == null || m.getAtomCount() == 0)
					break;

				String CAS = ((String) m.getProperty("cas.rn")).replace("CAS-", "");
				;
				m.removeProperty("cas.rn");
				m.setProperty("CAS", CAS);

				FileWriter fw = new FileWriter(outputFolder + "/" + CAS + ".mol");

				MDLV2000Writer mw = new MDLV2000Writer(fw);
//				mw.setSdFields(m.getProperties());
				mw.writeMolecule(m);

				fw.close();

			} // end while true;

		} catch (Exception e) {
			e.printStackTrace();

		}

//		return moleculeSet;

	}

	void fixSDFs(String sdffolderpath, String molfilefolderpath) {
		File folder = new File(sdffolderpath);

		File[] files = folder.listFiles();

		for (int i = 0; i < files.length; i++) {

			String filename = files[i].getName();

			if (filename.indexOf(".sdf") == -1)
				continue;

//			System.out.println(filename);

			this.getCASRangeSDF(sdffolderpath, filename);
			this.convertSDFtoMolFiles(files[i].getAbsolutePath(), molfilefolderpath);
		}

	}

	void compileTxtFiles(String folderPath, String outputFilePath) {

		try {
			File folder = new File(folderPath);

			File[] files = folder.listFiles();

			FileWriter fw = new FileWriter(outputFilePath);

			int fileCount = 0;
			
			Hashtable <String,ScifinderRecord>htScifinderRecords=new Hashtable();

			for (int i = files.length-1; i>=0; i--) {//go backwards to use more recent ones first

				String filename = files[i].getName();

				if (filename.indexOf(".txt") == -1)
					continue;

				fileCount++;

				BufferedReader br = new BufferedReader(new FileReader(files[i]));

				String header = br.readLine();
				LinkedList<String>hl=Utilities.Parse3(header, ",");

				if (fileCount == 1)
					fw.write(header + "\r\n");

				while (true) {
					String Line = br.readLine();
					if (Line == null)
						break;

					LinkedList<String>l=Utilities.Parse3(Line, ",");
					ScifinderRecord sr=createScifinderRecord(hl, l);
					System.out.println(Line);
					
					if (htScifinderRecords.get(sr.Registry_Number)==null) {
						htScifinderRecords.put(sr.Registry_Number, sr);
						fw.write(Line + "\r\n");
						fw.flush();
					} else {
						//skip it 
					}
				}
				
				br.close();
			}

			fw.close();
			

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	void compileTxtFilesToDB(String folderPath, String dbFilePath) {

		try {

			// create the db:
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
			Statement stat = conn.createStatement();

			String tableName = "quoted_format";
			String[] fields = { "T", "Database", "Copyright", "Registry_Number", "CA_Index_Name", "Other_Names",
					"Formula", "Alternate_Formula", "Class_Identifier", "Editor_Notes", "Definition_Field",
					"Alternate_Registry_Numbers", "Deleted_Registry_Numbers", "Source_of_Registration",
					"Sequence_Length", "Sequence", "Accession_Number", "Version_Number", "Definition", "Organism" };

			
			String sql = "drop table if exists " + tableName + ";";
			stat.executeUpdate(sql);
			
			MySQL_DB.create_table(stat, tableName, fields, "Registry_Number");

			File folder = new File(folderPath);

			File[] files = folder.listFiles();

			String s = "insert into " + tableName + " values (";

			for (int i = 1; i <= fields.length; i++) {
				s += "?";
				if (i < fields.length)
					s += ",";
			}
			s += ");";

			int fileCount = 0;

			for (int i = 0; i <files.length ; i++) {//go in descending order to use more recent first

				String filename = files[i].getName();

				if (filename.indexOf(".txt") == -1)
					continue;

				fileCount++;

				BufferedReader br = new BufferedReader(new FileReader(files[i]));

				String header = br.readLine();

				while (true) {
					String Line = br.readLine();
					if (Line == null)
						break;
					LinkedList<String> values = Utilities.Parse3(Line, ",");
					
					while (values.size()<fields.length) values.add("");
					
					try {
						PreparedStatement prep = conn.prepareStatement(s);
						for (int j = 0; j < values.size(); j++) {
							prep.setString((j + 1), values.get(j));
						}
						prep.execute();

					} catch (Exception ex) {
						System.out.println(ex.getMessage());
					}
				}
				
				br.close();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	void removeMissingChemicalsFromV3000SDF(String folder, String name1, String name2) {

		try {

			BufferedReader br = new BufferedReader(new FileReader(folder + "/" + name1));

			FileWriter fw = new FileWriter(folder + "/" + name2);

			Vector<String> lines = new Vector<String>();

			while (true) {

				String Line = br.readLine();

				if (Line == null)
					break;

				lines.add(Line);

				if (Line.equals("$$$$")) {

					if (lines.size() > 15) {
						for (int i = 0; i < lines.size(); i++) {
							fw.write(lines.get(i) + "\r\n");
						}
						fw.flush();
					}

					lines.removeAllElements();
				}

			}
			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

//	//TODO- use inchikey to compare instead
//	void compareStructureFiles() {
//		String folder="REACH_dossier_data/Skin Sensitization/Structure data";
//
//		String folderPath1=folder+"/ChemistryDashboard/mol files";
//		String folderPath2=folder+"/SciFinder/mol files";
//		String folderPath3=folder+"/Compare";
//		
//		File Folder1=new File(folderPath1);
//		File Folder2=new File(folderPath2);
//		
//		File [] files1=Folder1.listFiles();
//		
//		
//		
//		for (int i=0;i<files1.length;i++) {
//			
//			File filei=files1[i];
//			
//			File filei2=new File(folderPath2+"/"+filei.getName());
//			
//			
//			String CAS=filei.getName().substring(0,filei.getName().indexOf("."));
//			
//			if (CAS.equals("10016-20-3")) continue;
//			if (CAS.equals("106990-43-6")) continue;
//			
//			if (!filei2.exists()) {
//				System.out.println(i+"\t"+CAS+"\tmissing");
//				continue;
//			}
//			
////			System.out.println(CAS);
//			
//			AtomContainer mol1= MolFileUtilities.LoadChemicalFromMolFile(CAS,folderPath1);
//			AtomContainer mol2= MolFileUtilities.LoadChemicalFromMolFile(CAS,folderPath2);
//			
//			try {
//				boolean match=chemicalcompare.isIsomorphHybrid(mol1, mol2, true, true, true);
//				System.out.println(i+"\t"+CAS+"\t"+match);
//				
//				if (!match) {
//					File file1a=new File(folderPath1+"/"+CAS+".mol");
//					File file1b=new File(folderPath3+"/"+CAS+"_dashboard.mol");
//					AADashboard.Utilities.Utilities.CopyFile(file1a, file1b);
//					
//					File file2a=new File(folderPath2+"/"+CAS+".mol");
//					File file2b=new File(folderPath3+"/"+CAS+"_scifinder.mol");
//					AADashboard.Utilities.Utilities.CopyFile(file2a, file2b);
//					
//					
//				}
//				
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
//			
//		}
//		
//		
//	}

	
	void getInfo(String casfilepath,String dbFilePath) {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
			Statement stat = conn.createStatement();
			
			BufferedReader br=new BufferedReader(new FileReader(casfilepath));
			
			SmilesGenerator sg =SmilesGenerator.unique();
			
			while (true) {
				String CAS=br.readLine();
				if (CAS==null) break;
//				System.out.println(CAS);

				String query="select * from "+"sdf"+" where cas_rn = \""+CAS+"\";";
//				System.out.println(query);

				ResultSet rs = stat.executeQuery(query);
				
				if (!rs.isClosed()) {
					String molfile=rs.getString("molfile");
					AtomContainer ac=(AtomContainer)MolFileUtilities.LoadFromSdfString(molfile).getAtomContainer(0);
					
					String [] inchis=MolFileUtilities.generateInChiKey(ac);
					String inchiKey=inchis[1];

					
					String smiles = sg.create(ac);

					
					System.out.println(CAS+"\t"+inchiKey+"\t"+smiles);
					
				} else {
					System.out.println(CAS+"\tN/A");
				}
				
				
			}
			
			br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	void go_through_SDF_Folder_to_db(String folderPath, String dbFilePath) {
		File Folder = new File(folderPath);

		File[] files = Folder.listFiles();
		String tableName = "sdf";

		try {
			Class.forName("org.sqlite.JDBC");

//			String dbfilename = "R:/TEST_Results.db";

			File db = new File(dbFilePath);
			if (db.exists())
				db.delete();

			// create the db:
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
			Statement stat = conn.createStatement();

			String[] fields = { "cas_rn", "molecular_formula", "cas_index_name", "molfile","date" };

			MySQL_DB.create_table(stat, tableName, fields, "cas_rn");

			for (int i=files.length-1;i>=0;i--) {
				File file=files[i];
				if (!file.getName().contains(".sdf"))
					continue;
				
				System.out.println(file.getName());
				addChemicalsFromSDFtoDB(tableName, file, fields, conn);
//				if (true) break;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void addChemicalsFromSDFtoDB(String tableName, File fileSDF, String[] fields, Connection conn)
			throws IOException {
		// String exractedFile = OUTPUT_DIR + entry.getName();
		// FileOutputStream fos = null;

		try {

//			conn.setAutoCommit(false);
			Statement stat=conn.createStatement();

			String s = "insert into " + tableName + " values (";

			for (int i = 1; i <= fields.length; i++) {
				s += "?";
				if (i < fields.length)
					s += ",";
			}
			s += ");";

			int counter = 0;

			IteratingSDFReader reader = new IteratingSDFReader(new FileInputStream(fileSDF),
					DefaultChemObjectBuilder.getInstance());

//			BasicFileAttributes attrs=Files.readAttributes(fileSDF.toPath(), BasicFileAttributes.class);
//			FileTime time = attrs.creationTime();			    
			String pattern = "yyyy-MM-dd HH-mm-ss";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);				
			String strDateCurrent = simpleDateFormat.format( new Date( fileSDF.lastModified()));
			
			while (reader.hasNext()) {
				IAtomContainer m = (IAtomContainer) reader.next();

				if (m == null || m.getAtomCount() == 0)
					break;

				String cas_rn = ((String) m.getProperty("cas.rn")).replace("CAS-", "");
				String molecular_formula = (String) m.getProperty("molecular.formula");
				String cas_index_name = (String) m.getProperty("cas.index.name");

				java.io.StringWriter fw = new java.io.StringWriter();

				MDLV2000Writer mw = new MDLV2000Writer(fw);
//				mw.setSdFields(m.getProperties());
				mw.writeMolecule(m);

				String molfile = fw.toString();
				mw.close();

//				System.out.println(cas_rn);
//				System.out.println(fw.toString());

				try {
					PreparedStatement prep = conn.prepareStatement(s);					
					prep.setString(1, cas_rn);
					prep.setString(2, molecular_formula);
					prep.setString(3, cas_index_name);
					prep.setString(4, molfile);
					prep.setString(5, strDateCurrent);
					prep.execute();

				} catch (Exception ex) {
					if (ex.getMessage().contains("UNIQUE constraint failed: sdf.cas_rn")) {
						String query="select * from "+tableName+" where cas_rn = \""+cas_rn+"\";";
//						System.out.println(query);

						ResultSet rs = stat.executeQuery(query);

						SimpleDateFormat sdf = new SimpleDateFormat(pattern);
						Date dateCurrent = new Date( fileSDF.lastModified());
				        Date dateExisting = sdf.parse(rs.getString("date"));
				        
				        if (dateCurrent.compareTo(dateExisting)<0) {
				        	System.out.println("current record on "+dateCurrent+" is older than existing record on"+dateExisting);
				        	
				        } else if (dateCurrent.compareTo(dateExisting)==0) {
//				        	System.out.println("current record on "+date2+" is same date as existing record");
				        } else if (dateCurrent.compareTo(dateExisting)>0) {
				        	System.out.println("current record on "+dateCurrent+" is more recent than existing record on"+dateExisting);
				        } else {
				        	System.out.println("else");
				        }
						
					}
					//If 
					
				}

//				prep.addBatch();

			} // end while true;

//			int [] count=prep.executeBatch();//do what's left
//			conn.setAutoCommit(true);
//			conn.commit();
//			System.out.println(count.length);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	void renameSDFFiles(String sdfFolderPath) {
		File sdfFolder=new File(sdfFolderPath);
		
		File [] files=sdfFolder.listFiles();
		
		for (File file:files) {
			try {
//				BasicFileAttributes attrs=Files.readAttributes(file.toPath(), BasicFileAttributes.class);
//				FileTime time = attrs.creationTime();			    
				String pattern = "yyyy-MM-dd HH-mm-ss";
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);				
				String date = simpleDateFormat.format( new Date( file.lastModified() ) );
				boolean rename=file.renameTo(new File(sdfFolderPath+"/"+date+".sdf"));
				System.out.println(rename+"\t"+file.lastModified());
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		

	}
	
	void renameQuoteFiles(String quoteFolderPath) {
		File sdfFolder=new File(quoteFolderPath);
		
		File [] files=sdfFolder.listFiles();
		
		for (File file:files) {
			try {
//				BasicFileAttributes attrs=Files.readAttributes(file.toPath(), BasicFileAttributes.class);
//				FileTime time = attrs.creationTime();			    
				String pattern = "yyyy-MM-dd HH-mm-ss";
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);				
				String date = simpleDateFormat.format( new Date( file.lastModified() ) );
				
				if (!file.getName().contains(".txt")) continue;
				
				if (!file.getName().contentEquals(date+".txt")) {
					boolean rename=file.renameTo(new File(quoteFolderPath+"/"+date+".txt"));
					System.out.println(file.getName()+"\t"+rename+"\t"+date);
				}
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		

	}
	
	public static void main(String[] args) {
		Scifinder s = new Scifinder();

		String sdffolder = folderScifinder + "\\sdf";
		String textFolder = folderScifinder + "\\quoted format";
		String dbFilePath = "AA Dashboard/databases/scifinder.db";

//		String molfolder=folder+"/mol files";
//		s.fixSDFs(sdffolder,molfolder);

//		s.renameSDFFiles(sdffolder);
//		s.renameQuoteFiles(textFolder);

		
		s.compileTxtFiles(textFolder,folderScifinder+"/scifinder_chemical_info.txt");

//		s.go_through_SDF_Folder_to_db(sdffolder,dbFilePath);
		
//		String casfilepath="AA Dashboard/databases/cas ncct mismatch2.txt";
//		s.getInfo(casfilepath, dbFilePath);
//		s.compileTxtFilesToDB(textFolder, dbFilePath);

		/////////////////////////////////////////////////////////////////////////////////////////////////////////
//		s.removeMissingChemicalsFromV3000SDF("REACH_dossier_data","ChemistryDashboard-AdvancedSearch_2017-04-13_14_35_52.sdf","chem dashboard remove blanks.sdf");

//		String folder="REACH_dossier_data/Skin Sensitization/Structure data/ChemistryDashboard";
//		String filepath=folder+"/chem dashboard remove blanks v2000.sdf";
//		String destFolder=folder+"/mol files";
//		s.convertSDFtoMolFiles2(filepath, destFolder);
		/////////////////////////////////////////////////////////////////////////////////////////////////////////

//		s.compareStructureFiles();

//		String CAS="122586-52-1";
//		String folder="REACH_dossier_data/Skin Sensitization/Structure data";
//		String folderPath1=folder+"/ChemistryDashboard/mol files";
//		String folderPath2=folder+"/SciFinder/mol files";
//		String folderPath3=folder+"/Compare";
//
//		File file1a=new File(folderPath1+"/"+CAS+".mol");
//		File file1b=new File(folderPath3+"/"+CAS+"_dashboard.mol");
//		ToxPredictor.Utilities.Utilities.CopyFile(file1a, file1b);
//		
//		File file2a=new File(folderPath2+"/"+CAS+".mol");
//		File file2b=new File(folderPath3+"/"+CAS+"_scifinder.mol");
//		ToxPredictor.Utilities.Utilities.CopyFile(file2a, file2b);

	}

}
