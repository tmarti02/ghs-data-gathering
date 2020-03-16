package gov.epa.ghs_data_gathering.Utilities;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import net.sf.jniinchi.INCHI_OPTION;
import net.sf.jniinchi.INCHI_RET;

public class MolFileUtilities {
	
	public static String[] generateInChiKey(IAtomContainer ac) {
		String warning = "";

		try {

			// Generate factory - throws CDKException if native code does not load
//			InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
//			// Get InChIGenerator
//			InChIGenerator gen = factory.getInChIGenerator(ac);

			List<INCHI_OPTION> options = new ArrayList<INCHI_OPTION>();
			// FIXME: uncomment after updating DB
//			options.add(INCHI_OPTION.FixedH);//makes sure  tautomers come out different! 

			// TODO- do we need any of these options???
			// https://github.com/cdk/cdk/issues/253
//			options.add(INCHI_OPTION.SAbs);
//			options.add(INCHI_OPTION.SAsXYZ);
//			options.add(INCHI_OPTION.SPXYZ);
//			options.add(INCHI_OPTION.FixSp3Bug);
//			options.add(INCHI_OPTION.AuxNone);
			

			InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
			InChIGenerator gen = factory.getInChIGenerator(ac, options);

			INCHI_RET ret = gen.getReturnStatus();

			if (ret == INCHI_RET.WARNING) {
				// InChI generated, but with warning message
//				System.out.println();

				warning = "InChI warning: " + gen.getMessage();

			} else if (ret != INCHI_RET.OKAY) {
				// InChI generation failed
				warning = "InChI failed: " + ret.toString() + " [" + gen.getMessage() + "]";
			}

			String inchi = gen.getInchi();
			String inchiKey = gen.getInchiKey();
			String[] result = { inchi, inchiKey, warning };

			return result;

			// TODO: distinguish between singlet and undefined spin multiplicity
			// TODO: double bond and allene parities
			// TODO: problem recognising bond stereochemistry

		} catch (CDKException | IllegalArgumentException ex) {
			String[] result = { null, null, ex.getMessage() };
			return result;
		}
	}
	
	
	public static boolean HaveCarbon(IAtomContainer mol) {

		try {

			for (int i=0; i<mol.getAtomCount();i++) {

				String var = mol.getAtom(i).getSymbol();

				// OK: C, H, O, N, F, Cl, Br, I, S, P, Si, As, Hg, Sn

				if (var.equals("C")) {
					return true;
				}
			}

			return false;

		} catch (Exception e) {
			return true;
		}

	}
	
	public static boolean HaveBadElement(IAtomContainer mol) {
		
		try {
						
			for (int i=0; i<mol.getAtomCount();i++) {

				String var = mol.getAtom(i).getSymbol();

				// OK: C, H, O, N, F, Cl, Br, I, S, P, Si, As, Hg, Sn

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
	public static AtomContainerSet LoadFromSdfString(String mol) {

		//https://stackoverflow.com/questions/5720524/how-does-one-create-an-inputstream-from-a-string

		try {
			
//			InputStream is = new ByteArrayInputStream( mol.getBytes() );
			
//			For multi-byte support use:
			InputStream is = new ByteArrayInputStream(Charset.forName("UTF-8").encode(mol).array());
			
			//TODO- make sure encoding is right- 8 or 16
			
			IteratingSDFReader isr=new IteratingSDFReader(is, DefaultChemObjectBuilder.getInstance());
			
			AtomContainerSet acs=new AtomContainerSet();
			
			while (isr.hasNext()) {
				acs.addAtomContainer(isr.next());
			}
			
			
			return acs;
			
		} catch (Exception ex) {
			return null;
		}

		
	}
	
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
