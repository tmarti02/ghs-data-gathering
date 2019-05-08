package gov.epa.ghs_data_gathering.Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

public class MolFileUtilities {
	
	/** Works no matter if molecules are empty
	 * 
	 * @param SDFfilepath
	 * @return
	 */

	public static AtomContainerSet LoadFromSDF3(String SDFfilepath) {
		AtomContainerSet moleculeSet = new AtomContainerSet();

		try {
			BufferedReader br = new BufferedReader(new FileReader(SDFfilepath));

			IteratingSDFReader isr=new IteratingSDFReader(br, DefaultChemObjectBuilder.getInstance());

			
			int counter=0;
			
			//read file into String Vector:
			while (isr.hasNext()) {
				counter++;
				
				AtomContainer ac=(AtomContainer) isr.next();

				if (ac == null) {
					//dont add molecule
				} else {
					moleculeSet.addAtomContainer(ac);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return moleculeSet;

	}
	
	public static AtomContainer LoadChemicalFromMolFile(String CAS,String folder) {
		
		try {

			
			
			String filePath = folder + File.separator + CAS + ".mol";
			File f = new File(filePath);
			
//			System.out.println(filename);
			
			if(!f.exists()) {
				System.out.println(filePath);
				
				return null;
			}

			AtomContainer m=new AtomContainer();
			MDLV2000Reader mr=new MDLV2000Reader(new FileInputStream(filePath));
			mr.read(m);
						
//			for (int i=0;i<molecule.getAtomCount();i++) {
//				System.out.println(i+"\t"+molecule.getAtom(i).getSymbol()+"\t"+molecule.getAtom(i).getFormalCharge());
//			}

			return m;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	
	/**
	 * Creates series of mol files from SDF file
	 * 
	 * @param SDFfilename
	 * @param outputfileloc
	 */
	public static void CreateMolFilesFromSDFFile(String SDFfilename, String outputfileloc,
			int Start,String CASFieldName) {
	
		
		try {
	
			BufferedReader br = new BufferedReader(new FileReader(SDFfilename));
	
	
			IteratingSDFReader mr = new IteratingSDFReader(br,DefaultChemObjectBuilder.getInstance());
			MDLV2000Writer mw;
	
			IAtomContainer m;
	
			int NoCASCount = 0;
			int counter = 0;
			while (mr.hasNext()) {
	
				try {
	
					m = mr.next();
					counter++;
	
					if (counter % 1000 == 0)
						System.out.println(counter);
	
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
	
	
				String CAS = m.getProperty("CAS");
	
				if (CAS.equals("-9999")) {
					NoCASCount++;
					CAS = "NoCAS-" + NoCASCount;
				}
	
				if (counter < Start)
					continue;
	
				FileWriter fw = new FileWriter(outputfileloc + "/" + CAS
						+ ".mol");
				mw = new MDLV2000Writer(fw);
				mw.write(m);
	
				fw.flush();
				fw.close();
	
			}// end while true;
	
			br.close();
	
		} catch (Exception e) {
			e.printStackTrace();
	
		}
	
	}
}
