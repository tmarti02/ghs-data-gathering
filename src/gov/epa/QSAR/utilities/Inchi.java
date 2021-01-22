package gov.epa.QSAR.utilities;

import org.openscience.cdk.interfaces.IAtomContainer;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoException;
import com.epam.indigo.IndigoInchi;
import com.epam.indigo.IndigoObject;



public class Inchi {

	public String inchi, inchiKey, inchiKey1,warning; 
	
//	public static Inchi generateInChiKeyCDK(IAtomContainer ac) {		
//		return CDKUtilities.generateInChiKey(ac);		
//	}
	
	public static Inchi generateInChiKeyIndigo(IAtomContainer ac) {		
		return IndigoUtilities.generateInChiKey(ac);		
	}

	public static Inchi generateInChiKeyIndigo(String smiles) {
		return IndigoUtilities.toInchiIndigo(smiles);	
	}
	
	
}
