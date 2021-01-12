package gov.epa.QSAR.utilities;

import java.io.IOException;
import java.io.StringWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Writer;
import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoException;
import com.epam.indigo.IndigoInchi;
import com.epam.indigo.IndigoObject;

public class IndigoUtilities {

	private static final Logger logger = LogManager.getLogger(IndigoUtilities.class);

	private static Indigo indigo = null;
	
	private static void init()
	{
		synchronized ( IndigoUtilities.class ) {
			if ( indigo == null ) {
				indigo = new Indigo();
				indigo.setOption("ignore-stereochemistry-errors", true);
			}
		}
	}
	
	private static IndigoObject toMol(IAtomContainer ac) {
		init();
		
		try {
			StringWriter sw = new StringWriter();
			MDLV2000Writer writer = new MDLV2000Writer(sw);
			writer.write((IAtomContainer)ac);
			writer.close();
			return indigo.loadMolecule(sw.toString());
		}
		catch ( Exception ex ) {
			return null;
		}
	}
	
	
	
	public static String generateSmiles(IAtomContainer ac) {
		IndigoObject mol = toMol(ac);
		if ( mol == null )
			return null;
		
		return mol.canonicalSmiles();
	}

	public static Inchi generateInChiKey(IAtomContainer ac) {
		IndigoObject mol = toMol(ac);
		if ( mol == null )
			return null;
		
		IndigoInchi indigoInchi = new IndigoInchi(indigo);
			
		Inchi inchi=new Inchi();
		
		try {
		
			inchi.inchi = indigoInchi.getInchi(mol);
			inchi.inchiKey = indigoInchi.getInchiKey(inchi.inchi);
			inchi.inchiKey1 = inchi.inchiKey != null ? inchi.inchiKey.substring(0, 14) : null;
			
			return inchi;
		} catch (Exception ex) {
			return null;
		}
				
	}
	
	public static Inchi toInchiIndigo(String mol) {
		try {
			Indigo indigo = new Indigo();
			indigo.setOption("ignore-stereochemistry-errors", true);

			IndigoInchi indigoInchi = new IndigoInchi(indigo);

			IndigoObject m = indigo.loadMolecule(mol);

			Inchi inchi=new Inchi();
			inchi.inchi = indigoInchi.getInchi(m);
			inchi.inchiKey = indigoInchi.getInchiKey(inchi.inchi);
			inchi.inchiKey1 = inchi.inchiKey != null ? inchi.inchiKey.substring(0, 14) : null;
			
			return inchi;
			
		} catch ( IndigoException ex ) {
//			log.error(ex.getMessage());
			return null;
		}
	}

}
