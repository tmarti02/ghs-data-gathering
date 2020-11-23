package gov.epa.exp_data_gathering.parse;

public class UnitConverter {
	
	public static final double airDensitySTP = 1.2041/1000.0;
	public static final double mmHg_to_kPa=0.133322;
	public static final double atm_to_kPa=101.325;
	public static final double atm_to_Pa=101325.0;
	public static final double psi_to_kPa=6.89476;
	
	public static double F_to_C(double F) {
		return (F-32.0)*5.0/9.0;
	}
	
	public static double K_to_C(double K) {
		return K-273.15;
	}
	
}
