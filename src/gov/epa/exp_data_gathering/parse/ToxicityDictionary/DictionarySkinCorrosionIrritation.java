package gov.epa.exp_data_gathering.parse.ToxicityDictionary;

public class DictionarySkinCorrosionIrritation {
	
	public static double convertPIIToBinaryIrritation(double propertyValue) {
		double IrritBinary = -1;
		if ((propertyValue >= 2.3)) {
			IrritBinary = 1.0;
		} else if ((propertyValue < 2.3) && (propertyValue >= 0)) {
			IrritBinary = 0.0;
		}
		return IrritBinary;
	}

}
