package gov.epa.ghs_data_gathering.GetData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import org.openscience.cdk.AtomContainer;
//import org.openscience.cdk.MoleculeSet;
//import org.openscience.cdk.interfaces.IMolecule;
//import org.openscience.cdk.io.MDLWriter;
//import GetData.MDLReader;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import gov.epa.ghs_data_gathering.Database.MySQL_DB;
import gov.epa.ghs_data_gathering.Utilities.MolFileUtilities;
import gov.epa.ghs_data_gathering.Utilities.TESTConstants;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

public class Scifinder {

	public static String folderScifinder = "AA Dashboard\\Structure data\\SciFinder";

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

			for (int i = 0; i < files.length; i++) {

				String filename = files[i].getName();

				if (filename.indexOf(".txt") == -1)
					continue;

				fileCount++;

				BufferedReader br = new BufferedReader(new FileReader(files[i]));

				String header = br.readLine();

				if (fileCount == 1)
					fw.write(header + "\r\n");

				while (true) {
					String Line = br.readLine();
					if (Line == null)
						break;

					fw.write(Line + "\r\n");
					fw.flush();
				}
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

			for (int i = 0; i < files.length; i++) {

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

			String[] fields = { "cas_rn", "molecular_formula", "cas_index_name", "molfile" };

			MySQL_DB.create_table(stat, tableName, fields, "cas_rn");

			for (File file : files) {
				if (!file.getName().contains(".sdf"))
					continue;
				addChemicalsFromSDFtoDB(tableName, file, fields, conn);

//				if (true) break;

				System.out.println(file.getName());
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

				System.out.println(cas_rn);
//				System.out.println(fw.toString());

				try {
					PreparedStatement prep = conn.prepareStatement(s);
					;
					prep.setString(1, cas_rn);
					prep.setString(2, molecular_formula);
					prep.setString(3, cas_index_name);
					prep.setString(4, molfile);
					prep.execute();

				} catch (Exception ex) {
					System.out.println(ex.getMessage());
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

	public static void main(String[] args) {
		Scifinder s = new Scifinder();

		String sdffolder = folderScifinder + "\\sdf";
		String textFolder = folderScifinder + "\\quoted format";
		String dbFilePath = "AA Dashboard/databases/scifinder.db";

//		String molfolder=folder+"/mol files";
//		s.fixSDFs(sdffolder,molfolder);

//		s.compileTxtFiles(textFolder,folderScifinder+"/scifinder_chemical_info.txt");

		s.go_through_SDF_Folder_to_db(sdffolder,dbFilePath);
		s.compileTxtFilesToDB(textFolder, dbFilePath);

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
