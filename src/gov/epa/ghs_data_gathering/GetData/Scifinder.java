package gov.epa.ghs_data_gathering.GetData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
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

import gov.epa.ghs_data_gathering.Utilities.MolFileUtilities;

public class Scifinder {

	
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
	
	
	void convertSDFtoMolFiles2(String filepath,String outputFolder) {

		try {
			
			File file=new File(filepath);
			
			 IteratingSDFReader reader = new IteratingSDFReader(
					   new FileInputStream(file), DefaultChemObjectBuilder.getInstance());
					 
			 while (reader.hasNext()) {
					   IAtomContainer m = (IAtomContainer)reader.next();

				if (m == null || m.getAtomCount() == 0)
					break;

				String CAS = ((String) m.getProperty("CASRN"));
				m.removeProperty("CASRN");
				m.removeProperty("INPUT");
				m.setProperty("CAS",CAS);

				FileWriter fw=new FileWriter(outputFolder+"/"+CAS+".mol");
				
				MDLV2000Writer mw=new MDLV2000Writer(fw);
//				mw.setSdFields(m.getProperties());
				mw.writeMolecule(m);
				
				fw.close();
				
			} // end while true;


		} catch (Exception e) {
			e.printStackTrace();

		}

//		return moleculeSet;

	}
	
	
	void convertSDFtoMolFiles(String filepath,String outputFolder) {

		

		try {
			
			File file=new File(filepath);
			
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			IteratingSDFReader reader = new IteratingSDFReader(
					   new FileInputStream(file), DefaultChemObjectBuilder.getInstance());
					 
			 while (reader.hasNext()) {
					   IAtomContainer m = (IAtomContainer)reader.next();

				if (m == null || m.getAtomCount() == 0)
					break;

				String CAS = ((String) m.getProperty("cas.rn")).replace("CAS-","");;
				m.removeProperty("cas.rn");
				m.setProperty("CAS",CAS);

				FileWriter fw=new FileWriter(outputFolder+"/"+CAS+".mol");
				
				MDLV2000Writer mw=new MDLV2000Writer(fw);
//				mw.setSdFields(m.getProperties());
				mw.writeMolecule(m);
				
				fw.close();
				

			} // end while true;

			br.close();

		} catch (Exception e) {
			e.printStackTrace();

		}

//		return moleculeSet;

	}
	
	void fixSDFs(String sdffolderpath,String molfilefolderpath) {
		File folder=new File(sdffolderpath);
		
		File [] files=folder.listFiles();
		
		for (int i=0;i<files.length;i++) {
			
			String filename=files[i].getName();
			
			if (filename.indexOf(".sdf")==-1) continue;
			
//			System.out.println(filename);
			
			this.getCASRangeSDF(sdffolderpath, filename);
			this.convertSDFtoMolFiles(files[i].getAbsolutePath(), molfilefolderpath);
		}
		
	}
	
	void compileTxtFiles(String folderPath,String outputFilePath) {

		try {
			File folder=new File(folderPath);

			File [] files=folder.listFiles();
			
			FileWriter fw=new FileWriter(outputFilePath);
			
			int fileCount=0;
			
			
			for (int i=0;i<files.length;i++) {

				String filename=files[i].getName();

				if (filename.indexOf(".txt")==-1) continue;
				
				fileCount++;
				
				BufferedReader br=new BufferedReader(new FileReader(files[i]));
				
				String header=br.readLine();
				
				if (fileCount==1) fw.write(header+"\r\n");
				
				while (true) {
					String Line=br.readLine();
					if (Line==null) break;
					
					fw.write(Line+"\r\n");
					fw.flush();
				}
			}
			
			
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	void removeMissingChemicalsFromV3000SDF(String folder,String name1,String name2) {
		
		try {
		
			BufferedReader br=new BufferedReader(new FileReader(folder+"/"+name1));
			
			FileWriter fw=new FileWriter(folder+"/"+name2);
			
			Vector<String>lines=new Vector<String>();
			
			while (true) {
			
				String Line=br.readLine();
				
				if (Line==null) break;
				
				lines.add(Line);
				
				
				if (Line.equals("$$$$")) {
					
					if (lines.size()>15) {
						for (int i=0;i<lines.size();i++) {
							fw.write(lines.get(i)+"\r\n");
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
	
	
	public static void main(String[] args) {
		Scifinder s=new Scifinder();
		
		String folder="AA Dashboard\\Data\\ECHA\\REACH_dossier_data\\Skin Sensitization\\Structure data\\SciFinder";
		String sdffolder=folder+"/sdf";
//		String molfolder=folder+"/mol files";
//		s.fixSDFs(sdffolder,molfolder);
		String textFolder=sdffolder;
		s.compileTxtFiles(textFolder,folder+"/scifinder_chemical_info.txt");
		
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
