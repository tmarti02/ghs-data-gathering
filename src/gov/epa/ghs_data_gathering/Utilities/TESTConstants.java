package gov.epa.ghs_data_gathering.Utilities;

//import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
//import org.apache.logging.log4j.util.Strings;

public class TESTConstants {
	public static String SoftwareVersion = "5.01";
	public static String SoftwareTitle = "T.E.S.T (Toxicity Estimation Software Tool)";
	
	//*add endpoint* 01
	public static final String ChoiceFHM_LC50="Fathead minnow LC50 (96 hr)";
	public static final String ChoiceDM_LC50="Daphnia magna LC50 (48 hr)";
	public static final String ChoiceTP_IGC50="T. pyriformis IGC50 (48 hr)";
	public static final String ChoiceRat_LD50="Oral rat LD50";
	public static final String ChoiceGA_EC50="Green algae EC50 (96 hr)";
//	public static final String ChoiceBCF="Bioaccumulation factor";
	public static final String ChoiceBCF="Bioconcentration factor";//TMM 3/30/18
	public static final String ChoiceReproTox="Developmental Toxicity";
	public static final String ChoiceMutagenicity="Mutagenicity";
	public static final String ChoiceEstrogenReceptorRelativeBindingAffinity="Estrogen Receptor RBA";//mg/L
	public static final String ChoiceEstrogenReceptor="Estrogen Receptor Binding";//mg/L
	
	public static final String ChoiceBoilingPoint="Normal boiling point"; //(°C)
	public static final String ChoiceVaporPressure="Vapor pressure at 25°C"; //mmHg
	public static final String ChoiceMeltingPoint="Melting point"; //(°C)
	public static final String ChoiceFlashPoint="Flash point";// (°C)
	public static final String ChoiceDensity="Density"; // (g/cm³)
	public static final String ChoiceSurfaceTension="Surface tension at 25°C";//  (dyn/cm)
	public static final String ChoiceThermalConductivity="Thermal conductivity at 25°C";// (mW/mK)
	public static final String ChoiceViscosity="Viscosity at 25°C";//(cP)
	public static final String ChoiceWaterSolubility="Water solubility at 25°C";//mg/L
	
	public static final String ChoiceDescriptors="Descriptors";
	
	//*add endpoint* 02
	public static final String abbrevChoiceFHM_LC50="LC50";
	public static final String abbrevChoiceDM_LC50="LC50DM";
	public static final String abbrevChoiceTP_IGC50="IGC50";
	public static final String abbrevChoiceRat_LD50="LD50";
	public static final String abbrevChoiceGA_EC50="EC50GA";
	public static final String abbrevChoiceBCF="BCF";
	public static final String abbrevChoiceReproTox="DevTox";
	public static final String abbrevChoiceMutagenicity="Mutagenicity";
	public static final String abbrevChoiceER_Binary="ER_Binary";
	public static final String abbrevChoiceER_LogRBA="ER_LogRBA";
	
	public static final String abbrevChoiceBoilingPoint="BP";
	public static final String abbrevChoiceVaporPressure="VP";
	public static final String abbrevChoiceMeltingPoint="MP";
	public static final String abbrevChoiceDensity="Density";
	public static final String abbrevChoiceFlashPoint="FP";
	public static final String abbrevChoiceSurfaceTension="ST";
	public static final String abbrevChoiceThermalConductivity="TC";
	public static final String abbrevChoiceViscosity="Viscosity";
	public static final String abbrevChoiceWaterSolubility="WS";
	
	public static final String abbrevChoiceDescriptors="Descriptors";
	
//	Endpoints:
	public static final int numChoiceFHM_LC50=1;
	public static final int numChoiceDM_LC50=2;
	public static final int numChoiceTP_IGC50=3;
	public static final int numChoiceRat_LD50=4;
	public static final int numChoiceGA_EC50=8;
	public static final int numChoiceBCF=5;
	public static final int numChoiceReproTox=6;
	public static final int numChoiceMutagenicity=7;
	public static final int numChoiceER_Binary=9;
	public static final int numChoiceER_LogRBA=10;
	
	public static final int numChoiceBoilingPoint=20;
	public static final int numChoiceVaporPressure=21;
	public static final int numChoiceMeltingPoint=22;
	public static final int numChoiceFlashPoint=23;
	public static final int numChoiceDensity=24;
	public static final int numChoiceSurfaceTension=25;
	public static final int numChoiceThermalConductivity=26;
	public static final int numChoiceViscosity=27;
	public static final int numChoiceWaterSolubility=28;

	public static final int numChoiceDescriptors=99;

	
	// QSAR Methods
	public static final String ChoiceHierarchicalMethod="Hierarchical clustering";
	public static final String ChoiceFDAMethod="FDA";
	public static final String ChoiceSingleModelMethod="Single model";
	public static final String ChoiceNearestNeighborMethod="Nearest neighbor";
	public static final String ChoiceGroupContributionMethod="Group contribution";
	public static final String ChoiceRandomForrestCaesar="CAESAR Random Forest";
	public static final String ChoiceLDA="Mode of action";
	public static final String ChoiceNotApplicable="N/A";
	public static final String ChoiceConsensus="Consensus";
	
	// Abbreviated QSAR methods
	public static final String abbrevChoiceHierarchicalMethod="hc";
	public static final String abbrevChoiceFDAMethod="fda";
	public static final String abbrevChoiceSingleModelMethod="sm";
	public static final String abbrevChoiceNearestNeighborMethod="nn";
	public static final String abbrevChoiceGroupContributionMethod="gc";
	public static final String abbrevChoiceRandomForrestCaesar="rf";
	public static final String abbrevChoiceLDA="lda";
	public static final String abbrevChoiceConsensus="consensus";

	// Numerical constants for QSAR methods
	public static final int numChoiceHierarchicalMethod=1;
	public static final int numChoiceFDAMethod=2;
	public static final int numChoiceSingleModelMethod=3;//not applicable to all endpoints
	public static final int numChoiceNearestNeighborMethod=4;
	public static final int numChoiceGroupContributionMethod=5;//not applicable to all endpoints
	public static final int numChoiceConsensus=10;

	public static final int typeTaskSingle=1;
	public static final int typeTaskBatch=2;
	
	//File types:
	public static final int numFormatSDF=1;
	public static final int numFormatSMI=2;
	public static final int numFormatMOL=3;
	public static final int numFormatSMILES=4;
	public static final int numFormatXML=5;
	public static final int numFormatJSON=6;
	public static final int numFormatCSV=7;
	public static final int numFormatTXT=8;
	
	public static final String abbrevFormatSDF = "SDF";
	public static final String abbrevFormatSMI = "SMI";
	public static final String abbrevFormatMOL = "MOL";
	public static final String abbrevFormatSMILES = "SMILES";
	public static final String abbrevFormatXML = "XML";
	public static final String abbrevFormatJSON = "JSON";
	public static final String abbrevFormatCSV = "CSV";
	public static final String abbrevFormatTXT = "TXT";
	
	public static final String timeoutVariableName = "path.timeout";
	public static final String defaultPathGenerationTimeout = "120000"; // 2 mins
	public static int pathGenerationTimeout = 
	        Integer.valueOf(System.getProperty(timeoutVariableName, 
            defaultPathGenerationTimeout));
	

	// TODO: Move out translation functions as they contain context-dependent logic
	
	public static String getFormat(int i) {
		switch ( i ) {
		case numFormatSDF:
			return abbrevFormatSDF;
		case numFormatSMI:
			return abbrevFormatSMI;
		case numFormatMOL:
			return abbrevFormatMOL;
		case numFormatSMILES:
			return abbrevFormatSMILES;
		case numFormatXML:
			return abbrevFormatXML;
		case numFormatJSON:
			return abbrevFormatJSON;
		case numFormatCSV:
			return abbrevFormatCSV;
		case numFormatTXT:
			return abbrevFormatTXT;
		default:
			return "?";
		}
	}
	
	/**
	 * Defines file format by its abbreviated representation.
	 * @param abbrevFormat - one of SDF, SMI, MOL, XML, JSON or CSV
	 * @return numeric file type
	 */
	public static int getFormat(String abbrevFormat) {
		if ( StringUtils.isEmpty(abbrevFormat) )
			return -1;
		if (abbrevFormat.equalsIgnoreCase(abbrevFormatSDF)) {
			return numFormatSDF;
		} else if (abbrevFormat.equalsIgnoreCase(abbrevFormatSMI)) {
			return numFormatSMI;
		} else if (abbrevFormat.equalsIgnoreCase(abbrevFormatMOL)) {
			return numFormatMOL;
		} else if (abbrevFormat.equalsIgnoreCase(abbrevFormatSMILES)) {
			return numFormatSMILES;
		} else if (abbrevFormat.equalsIgnoreCase(abbrevFormatXML)) {
			return numFormatXML;
		} else if (abbrevFormat.equalsIgnoreCase(abbrevFormatJSON)) {
			return numFormatJSON;
		} else if (abbrevFormat.equalsIgnoreCase(abbrevFormatCSV)) {
			return numFormatCSV;
		} else if (abbrevFormat.equalsIgnoreCase(abbrevFormatTXT)) {
			return numFormatTXT;
		} else {
			return -1;
		}
	}

//	/**
//	 * Defines file format by its extension. Supported file formats are SDF, SMI, MOL, XML, JSON and CSV.
//	 * @param file - file name
//	 * @return numeric constant representing file type
//	 */
//	public static int getFormatByFileName(String file) {
//		String ext = FilenameUtils.getExtension(file);
//		if ( Strings.isEmpty(ext) )
//			return -1;
//		
//		if ( ext.equalsIgnoreCase("sdf") )
//			return TESTConstants.numFormatSDF;
//		if ( ext.equalsIgnoreCase("smi") )
//			return TESTConstants.numFormatSMI;
//		if ( ext.equalsIgnoreCase("mol") )
//			return TESTConstants.numFormatMOL;
//		if ( ext.equalsIgnoreCase("xml") )
//			return TESTConstants.numFormatXML;
//		if ( ext.equalsIgnoreCase("json") )
//			return TESTConstants.numFormatJSON;
//		if ( ext.equalsIgnoreCase("csv") )
//			return TESTConstants.numFormatCSV;
//		if ( ext.equalsIgnoreCase("txt") )
//			return TESTConstants.numFormatTXT;
//		
//		return -1;
//	}
	
	/**
	 * Translates numerical endpoint into a full endpoint name
	 * @param iEndpoint - numerical endpoint value
	 * @return
	 */
	public static String getEndpoint(int iEndpoint) {
		switch ( iEndpoint ) {
		// Biological props
		case numChoiceFHM_LC50:
			return ChoiceFHM_LC50;
		case numChoiceDM_LC50:
			return ChoiceDM_LC50;
		case numChoiceTP_IGC50:
			return ChoiceTP_IGC50;
		case numChoiceRat_LD50:
			return ChoiceRat_LD50;
		case numChoiceGA_EC50:
			return ChoiceGA_EC50;
		case numChoiceBCF:
			return ChoiceBCF;
		case numChoiceReproTox:
			return ChoiceReproTox;
		case numChoiceMutagenicity:
			return ChoiceMutagenicity;
		case numChoiceER_Binary:
			return ChoiceEstrogenReceptor;
		case numChoiceER_LogRBA:
			return ChoiceEstrogenReceptorRelativeBindingAffinity;
			
		// Phys-chem props
		case numChoiceBoilingPoint:
			return ChoiceBoilingPoint;
		case numChoiceVaporPressure:
			return ChoiceVaporPressure;
		case numChoiceMeltingPoint:
			return ChoiceMeltingPoint;
		case numChoiceFlashPoint:
			return ChoiceFlashPoint;
		case numChoiceDensity:
			return ChoiceDensity;
		case numChoiceSurfaceTension:
			return ChoiceSurfaceTension;
		case numChoiceThermalConductivity:
			return ChoiceThermalConductivity;
		case numChoiceViscosity:
			return ChoiceViscosity;
		case numChoiceWaterSolubility:
			return ChoiceWaterSolubility;
			
		// Pseudo props
		case numChoiceDescriptors:
			return ChoiceDescriptors;
		default:
			return "?";
		}
	}
	
	/**
	 * Translates abbreviated endpoint into numerical
	 * @param endpoint - abbreviated endpoint name
	 * @return numerical endpoint constant
	 */
	public static int getEndpoint(String endpoint) {
		// Biological props
		if ( endpoint.equalsIgnoreCase(abbrevChoiceFHM_LC50) ) {
			return numChoiceFHM_LC50;
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceDM_LC50) ) {
			return numChoiceDM_LC50;
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceTP_IGC50) ) {
			return numChoiceTP_IGC50;
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceRat_LD50) ) {
			return numChoiceRat_LD50;
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceGA_EC50) ) {
			return numChoiceGA_EC50;
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceBCF) ) {
			return numChoiceBCF;
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceReproTox) ) {
			return numChoiceReproTox;
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceMutagenicity) ) {
			return numChoiceMutagenicity;
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceER_Binary) ) {
			return numChoiceER_Binary;
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceER_LogRBA) ) {
			return numChoiceER_LogRBA;
			
		// Phys-chem props
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceBoilingPoint) ) {
			return numChoiceBoilingPoint;
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceVaporPressure) ) {
			return numChoiceVaporPressure;
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceMeltingPoint) ) {
			return numChoiceMeltingPoint;
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceFlashPoint) ) {
			return numChoiceFlashPoint;
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceDensity) ) {
			return numChoiceDensity;
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceSurfaceTension) ) {
			return numChoiceSurfaceTension;
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceThermalConductivity) ) {
			return numChoiceThermalConductivity;
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceViscosity) ) {
			return numChoiceViscosity;
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceWaterSolubility) ) {
			return numChoiceWaterSolubility;
			
		// Pseudo props
		} else if (endpoint.equalsIgnoreCase(abbrevChoiceDescriptors) ) {
			return numChoiceDescriptors;
		} else {
			return -1;
		}
	}
	
	/**
	 * Translates full endpoint name into abbreviated one
	 * @param endpoint - full endpoint name
	 * @return abbreviated endpoint name
	 */
	public static String getAbbrevEndpoint(String endpoint) {
		// Biological
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceFHM_LC50))
			return TESTConstants.abbrevChoiceFHM_LC50;
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceDM_LC50))
			return TESTConstants.abbrevChoiceDM_LC50;
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceTP_IGC50))
			return TESTConstants.abbrevChoiceTP_IGC50;
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceRat_LD50))
			return TESTConstants.abbrevChoiceRat_LD50;
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceGA_EC50))
			return TESTConstants.abbrevChoiceGA_EC50;
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceBCF))
			return TESTConstants.abbrevChoiceBCF;
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceReproTox))
			return TESTConstants.abbrevChoiceReproTox;
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceMutagenicity))
			return TESTConstants.abbrevChoiceMutagenicity;
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceEstrogenReceptor))
			return TESTConstants.abbrevChoiceER_Binary;
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity))
			return TESTConstants.abbrevChoiceER_LogRBA;
		
		// Phys-chem props
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceBoilingPoint))
			return TESTConstants.abbrevChoiceBoilingPoint;
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceVaporPressure))
			return TESTConstants.abbrevChoiceVaporPressure;
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceMeltingPoint))
			return TESTConstants.abbrevChoiceMeltingPoint;
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceFlashPoint))
			return TESTConstants.abbrevChoiceFlashPoint;
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceDensity))
			return TESTConstants.abbrevChoiceDensity;
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceSurfaceTension))
			return TESTConstants.abbrevChoiceSurfaceTension;
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceThermalConductivity))
			return TESTConstants.abbrevChoiceThermalConductivity;
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceViscosity))
			return TESTConstants.abbrevChoiceViscosity;
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceWaterSolubility))
			return TESTConstants.abbrevChoiceWaterSolubility;
		
		// Pseudo props
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceDescriptors))
			return TESTConstants.abbrevChoiceDescriptors;
		
		return "?";
	}
	
	/**
	 * Translates abbreviated endpoint name into a full one
	 * @param endpoint - abbreviated endpoint name
	 * @return full endpoint name
	 */
	public static String getFullEndpoint(String endpoint) {
		// Biological
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceFHM_LC50))
			return TESTConstants.ChoiceFHM_LC50;
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceDM_LC50))
			return TESTConstants.ChoiceDM_LC50;
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceTP_IGC50))
			return TESTConstants.ChoiceTP_IGC50;
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceRat_LD50))
			return TESTConstants.ChoiceRat_LD50;
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceGA_EC50))
			return TESTConstants.ChoiceGA_EC50;
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceBCF))
			return TESTConstants.ChoiceBCF;
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceReproTox))
			return TESTConstants.ChoiceReproTox;
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceMutagenicity))
			return TESTConstants.ChoiceMutagenicity;
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceER_Binary))
			return TESTConstants.ChoiceEstrogenReceptor;
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceER_LogRBA))
			return TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity;
		
		// Phys-chem props
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceBoilingPoint))
			return TESTConstants.ChoiceBoilingPoint;
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceVaporPressure))
			return TESTConstants.ChoiceVaporPressure;
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceMeltingPoint))
			return TESTConstants.ChoiceMeltingPoint;
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceFlashPoint))
			return TESTConstants.ChoiceFlashPoint;
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceDensity))
			return TESTConstants.ChoiceDensity;
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceSurfaceTension))
			return TESTConstants.ChoiceSurfaceTension;
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceThermalConductivity))
			return TESTConstants.ChoiceThermalConductivity;
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceViscosity))
			return TESTConstants.ChoiceViscosity;
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceWaterSolubility))
			return TESTConstants.ChoiceWaterSolubility;
		
		// Pseudo props
		if (endpoint.equalsIgnoreCase(TESTConstants.abbrevChoiceDescriptors))
			return TESTConstants.ChoiceDescriptors;
		
		return "?";
	}
	
	/**
	 * Translates an array of abbreviated endpoint names into an array of full names. If none is given returns all endpoints, but descriptors. 
	 * @param endpoints
	 * @return
	 */
	public static String[] getFullEndpoints(String[] endpoints) {
		if ( endpoints != null && endpoints.length != 0 ) {
			String[] result = new String[endpoints.length]; 
			for (int i=0; i<endpoints.length; i++) {
				result[i] = getFullEndpoint(endpoints[i]);
			}
			return result;
		}
		else {
			return new String[] {
					TESTConstants.ChoiceFHM_LC50,
					TESTConstants.ChoiceDM_LC50,
					TESTConstants.ChoiceTP_IGC50,
					TESTConstants.ChoiceRat_LD50,
					// TESTConstants.ChoiceGA_EC50,
					TESTConstants.ChoiceBCF,
					TESTConstants.ChoiceReproTox,
					TESTConstants.ChoiceMutagenicity,
					TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity,
					TESTConstants.ChoiceEstrogenReceptor,
					
					TESTConstants.ChoiceBoilingPoint,
					TESTConstants.ChoiceVaporPressure,
					TESTConstants.ChoiceMeltingPoint,
					TESTConstants.ChoiceFlashPoint,
					TESTConstants.ChoiceDensity,
					TESTConstants.ChoiceSurfaceTension,
					TESTConstants.ChoiceThermalConductivity,
					TESTConstants.ChoiceViscosity,
					TESTConstants.ChoiceWaterSolubility,
			};
		}
	}
	
	/**
	 * Translates numeric method value into a full name 
	 * @param iMethod - numeric method
	 * @return full method name
	 */
	public static String getMethod(int iMethod) {
		switch ( iMethod ) {
		case numChoiceHierarchicalMethod:
			return ChoiceHierarchicalMethod;
		case numChoiceFDAMethod:
			return ChoiceFDAMethod;
		case numChoiceSingleModelMethod:
			return ChoiceSingleModelMethod;
		case numChoiceNearestNeighborMethod:
			return ChoiceNearestNeighborMethod;
		case numChoiceGroupContributionMethod:
			return ChoiceGroupContributionMethod;
		case numChoiceConsensus:
			return ChoiceConsensus;
		default:
			return "?";
		}
	}
	
	public static int getMethod(String abbrevMethod) {
		if (abbrevMethod.equalsIgnoreCase(abbrevChoiceHierarchicalMethod)) {
			return numChoiceHierarchicalMethod;
		} else if (abbrevMethod.equalsIgnoreCase(abbrevChoiceFDAMethod)) {
			return numChoiceFDAMethod;
		} else if (abbrevMethod.equalsIgnoreCase(abbrevChoiceSingleModelMethod)) {
			return numChoiceSingleModelMethod;
		} else if (abbrevMethod.equalsIgnoreCase(abbrevChoiceNearestNeighborMethod)) {
			return numChoiceNearestNeighborMethod;
		} else if (abbrevMethod.equalsIgnoreCase(abbrevChoiceGroupContributionMethod)) {
			return numChoiceGroupContributionMethod;
		} else if (abbrevMethod.equalsIgnoreCase(abbrevChoiceConsensus)) {
			return numChoiceConsensus;
		} else {
			return -1;
		}
	}

	public static String getAbbrevMethod(String method) {
		if (method.equalsIgnoreCase(ChoiceHierarchicalMethod)) {
			return abbrevChoiceHierarchicalMethod;
		} else if (method.equalsIgnoreCase(ChoiceFDAMethod)) {
			return abbrevChoiceFDAMethod;
		} else if (method.equalsIgnoreCase(ChoiceSingleModelMethod)) {
			return abbrevChoiceSingleModelMethod;
		} else if (method.equalsIgnoreCase(ChoiceNearestNeighborMethod)) {
			return abbrevChoiceNearestNeighborMethod;
		} else if (method.equalsIgnoreCase(ChoiceGroupContributionMethod)) {
			return abbrevChoiceGroupContributionMethod;
		} else if (method.equalsIgnoreCase(ChoiceConsensus)) {
			return abbrevChoiceConsensus;
		} else {
			return "?";
		}
	}
	
	public static String getFullMethod(String abbrevMethod) {
		if (abbrevMethod.equalsIgnoreCase(abbrevChoiceHierarchicalMethod)) {
			return ChoiceHierarchicalMethod;
		} else if (abbrevMethod.equalsIgnoreCase(abbrevChoiceFDAMethod)) {
			return ChoiceFDAMethod;
		} else if (abbrevMethod.equalsIgnoreCase(abbrevChoiceSingleModelMethod)) {
			return ChoiceSingleModelMethod;
		} else if (abbrevMethod.equalsIgnoreCase(abbrevChoiceNearestNeighborMethod)) {
			return ChoiceNearestNeighborMethod;
		} else if (abbrevMethod.equalsIgnoreCase(abbrevChoiceGroupContributionMethod)) {
			return ChoiceGroupContributionMethod;
		} else if (abbrevMethod.equalsIgnoreCase(abbrevChoiceConsensus)) {
			return ChoiceConsensus;
		} else {
			return "?";
		}
	}
	
	public static String[] getFullMethods(String[] methods) {
		if ( methods != null && methods.length != 0 ) {
			String[] result = new String[methods.length]; 
			for (int i=0; i<methods.length; i++) {
				result[i] = getFullMethod(methods[i]);
			}
			return result;	
		}
		else {
			return new String[] { ChoiceConsensus };
		}
	}
	
	public static boolean haveSingleModelMethod(String endpoint) {
		//*add endpoint* 08
		if (!endpoint.equalsIgnoreCase(ChoiceFHM_LC50)
				&& !endpoint.equalsIgnoreCase(ChoiceDM_LC50)
				&& !endpoint.equalsIgnoreCase(ChoiceBCF)
				&& !endpoint.equalsIgnoreCase(ChoiceReproTox)
				&& !endpoint.equalsIgnoreCase(ChoiceThermalConductivity)
				&& !endpoint.equalsIgnoreCase(ChoiceEstrogenReceptor)
				&& !endpoint.equalsIgnoreCase(ChoiceEstrogenReceptorRelativeBindingAffinity)
				&& !endpoint.equalsIgnoreCase(ChoiceViscosity)) { // we are OK
			// If endpoint is NOT one of the above, don't have single model method
			return false;
		} else {
			return true;
		}
	}
	
	public static boolean haveGroupContributionMethod(String endpoint) {
		//*add endpoint* 09
		if (endpoint.equalsIgnoreCase(ChoiceRat_LD50)
				|| endpoint.equalsIgnoreCase(ChoiceMutagenicity)
				//				|| endpoint.equals(ChoiceDM_LC50)
				|| endpoint.equalsIgnoreCase(ChoiceReproTox)) {
			//If endpoint IS one of the above, don't have group contribution method
			return false; 
		} else {
			return true;
		}
	}
	
	public static boolean isBinary(String endpoint) {
		//*add endpoint* 06
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceReproTox) || endpoint.equalsIgnoreCase(TESTConstants.ChoiceMutagenicity) || endpoint.equalsIgnoreCase(TESTConstants.ChoiceEstrogenReceptor)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isLogMolar(String endpoint) {
		//*add endpoint* 07
		if (endpoint.equalsIgnoreCase(TESTConstants.ChoiceBoilingPoint) || 
				endpoint.equalsIgnoreCase(TESTConstants.ChoiceDensity) ||
				endpoint.equalsIgnoreCase(TESTConstants.ChoiceFlashPoint) ||
				endpoint.equalsIgnoreCase(TESTConstants.ChoiceMeltingPoint) ||
				endpoint.equalsIgnoreCase(TESTConstants.ChoiceSurfaceTension) ||
				endpoint.equalsIgnoreCase(TESTConstants.ChoiceThermalConductivity)) {
			return false;
		} else {
			return true;
		}
	}
	
	public static String getMassUnits(String endpoint) {
		// *add endpoint*
		if (endpoint.equals(TESTConstants.ChoiceFHM_LC50) || endpoint.equals(TESTConstants.ChoiceTP_IGC50) || endpoint.equals(TESTConstants.ChoiceDM_LC50)
				|| endpoint.equals(TESTConstants.ChoiceGA_EC50) || endpoint.equals(TESTConstants.ChoiceWaterSolubility)) {
			return "mg/L";
		} else if (endpoint.equals(TESTConstants.ChoiceRat_LD50)) {
			return "mg/kg";
		} else if (endpoint.equals(TESTConstants.ChoiceBCF) || endpoint.equals(TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity)) {
			// return "L/kg";
			return "";
		} else if (endpoint.equals(TESTConstants.ChoiceReproTox) || endpoint.equals(TESTConstants.ChoiceMutagenicity) || endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
			return "";
		} else if (endpoint.equals(TESTConstants.ChoiceBoilingPoint) || endpoint.equals(TESTConstants.ChoiceFlashPoint) || endpoint.equals(TESTConstants.ChoiceMeltingPoint)) {
			return "°C";
		} else if (endpoint.equals(TESTConstants.ChoiceDensity)) {
			return "g/cm³";
		} else if (endpoint.equals(TESTConstants.ChoiceSurfaceTension)) {
			return "dyn/cm";
		} else if (endpoint.equals(TESTConstants.ChoiceThermalConductivity)) {
			return "mW/mK";
		} else if (endpoint.equals(TESTConstants.ChoiceViscosity)) {
			return "cP";
		} else if (endpoint.equals(TESTConstants.ChoiceVaporPressure)) {
			return "mmHg";
		} else {
			return "?";
		}
	}

	public static String getMolarLogUnits(String endpoint) {
		if (endpoint.equals(TESTConstants.ChoiceFHM_LC50) || endpoint.equals(TESTConstants.ChoiceTP_IGC50) || endpoint.equals(TESTConstants.ChoiceDM_LC50)
				|| endpoint.equals(TESTConstants.ChoiceGA_EC50) || endpoint.equals(TESTConstants.ChoiceWaterSolubility)) {
			return "-Log10(mol/L)";
		} else if (endpoint.equals(TESTConstants.ChoiceRat_LD50)) {
			return "-Log10(mol/kg)";
		} else if (endpoint.equals(TESTConstants.ChoiceBCF) || endpoint.equals(TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity)) {
			// return "Log(L/kg)";
			// return "Log<sub>10</sub>";
			return "Log10";

		} else if (endpoint.equals(TESTConstants.ChoiceViscosity)) {
			// return "Log<sub>10</sub>(cP)";
			return "Log10(cP)";
			// return "ln(cP)";
		} else if (endpoint.equals(TESTConstants.ChoiceReproTox) || endpoint.equals(TESTConstants.ChoiceMutagenicity) || endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
			return "";
		} else if (endpoint.equals(TESTConstants.ChoiceVaporPressure)) {
			return "Log10(mmHg)";
		} else if (endpoint.indexOf("Score") > -1) {
			return "";
		} else {
			return "?";
		}
	}
}
