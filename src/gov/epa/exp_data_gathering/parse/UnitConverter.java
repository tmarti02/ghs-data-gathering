package gov.epa.exp_data_gathering.parse;

import java.util.ArrayList;
import java.util.Hashtable;

import gov.epa.api.ExperimentalConstants;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

public class UnitConverter {

	Hashtable<String, Double> htDensity = new Hashtable<String, Double>(); // density look up table, densities in g/ml

	public static final double airDensitySTP = 1.2041 / 1000.0;
	public static final double kPa_to_mmHg = 7.50062;
	public static final double atm_to_mmHg = 760.0;
	public static final double psi_to_mmHg = 51.7149;
	public static final double hPa_to_mmHg = 0.750061;
	public static final double mPa_to_mmHg = 7.50062e-6;//millipascals not mega
	public static final double Pa_to_mmHg = 0.00750062;
	public static final double bar_to_mmHg = 750.062;
	public static final double atm_to_Pa = 101325.0;
	public static final double Pa_to_atm = 1.0 / 101325.0;
	public static final double megaPa_to_mmHg=kPa_to_mmHg*1000.0;
	public static final double uPa_to_mmHg = 7.50062e-9;
	public static final double nPa_to_mmHg = 7.50062e-12;

	public static final double N_m_to_dyn_cm=1000.0;
	public static final double N_cm_to_dyn_cm=100000.0;
	public static final double mN_cm_to_dyn_cm=100.0;
	
	
	public boolean debug = false;

	public static double F_to_C(double F) {
		return (F - 32.0) * 5.0 / 9.0;
	}

	private static void F_to_C(ExperimentalRecord er) {
		if (er.property_value_point_estimate_original != null) {
			er.property_value_point_estimate_final = F_to_C(er.property_value_point_estimate_original);
		}
		if (er.property_value_min_original != null) {
			er.property_value_min_final = F_to_C(er.property_value_min_original);
		}
		if (er.property_value_max_original != null) {
			er.property_value_max_final = F_to_C(er.property_value_max_original);
		}
	}

	public static double K_to_C(double K) {
		return K - 273.15;
	}

	private static void K_to_C(ExperimentalRecord er) {
		if (er.property_value_point_estimate_original != null) {
			er.property_value_point_estimate_final = K_to_C(er.property_value_point_estimate_original);
		}
		if (er.property_value_min_original != null) {
			er.property_value_min_final = K_to_C(er.property_value_min_original);
		}
		if (er.property_value_max_original != null) {
			er.property_value_max_final = K_to_C(er.property_value_max_original);
		}
	}

	/**
	 * Loads density values and stores in a hashtable
	 */
	public UnitConverter(String densityFilePath) {
		ArrayList<String> lines = Utilities.readFileToArray(densityFilePath);
		for (int i = 1; i < lines.size(); i++) {// first line is header
			// System.out.println(lines.get(i));
			String[] vals = lines.get(i).split("\t");

			String CAS = vals[0];
			String strDensity = vals[1];

			htDensity.put(CAS, Double.parseDouble(strDensity));
		}
		// System.out.println(htDensity.get("7487-28-7"));
	}

	/**
	 * Converts to final units and assigns point estimates for any ranges within
	 * tolerance:
	 * 
	 * @param er - ExperimentalRecord to convert units and store final values (Also
	 *           does checksum and fixes leading zeroes in casrn field - convenient
	 *           place to do it)
	 */
	public void convertRecord(ExperimentalRecord er) {

		
		er.casrn = ParseUtilities.fixCASLeadingZero(er.casrn);

		if (er.casrn != null) {
			if (er.casrn.equals("-") || er.casrn.isBlank())
				er.casrn = null;
		}

		if (er.casrn != null && !ParseUtilities.isValidCAS(er.casrn) && er.keep) {
			er.flag = true;			
//			er.reason = "Invalid CAS";
			er.updateNote("Invalid CAS");
			
		} else if (er.casrn != null && er.casrn.toLowerCase().contains("mixture")) {
			er.keep = false;
			er.reason = "Mixture";
		}

		if (er.property_value_numeric_qualifier != null && er.property_value_numeric_qualifier.equals("?")) {
			er.keep = false;
			er.reason = "Undetermined numeric qualifier";
		}

		if (er.property_value_units_final != null) {
//			System.out.println("Already converted units for " + er.property_name);
			return;// already converted it in previous code
		}

		if (er.property_value_units_original == null && !er.property_name.equals(ExperimentalConstants.strDensity) && !er.property_name.equals(ExperimentalConstants.strVaporDensity)) {
//			System.out.println(
//					"Missing original units for " + er.property_name + ", " + er.casrn + "\t" + er.chemical_name);
			er.keep = false;
			
			if(er.reason==null) {//dont override reason
				er.reason = "Original units missing";				
			}


		} else if (er.property_category != null) {
			
			if (er.property_category.toLowerCase().contains("bioconcentration")) {
				convertBCF(er);
			} else	if (er.property_category.toLowerCase().contains("acute oral toxicity") || er.property_category.toLowerCase().contains("acute dermal toxicity")) {
				convertOralMammalianToxicity(er);			
			}else if (er.property_category.toLowerCase().contains("acute aquatic toxicity")) {
				convertSolubility(er);
			} else if (er.property_category.toLowerCase().contains("acute inhalation toxicity")) {
				convertInhalationMammalianToxicity(er);
			} else {
				System.out.println("UnitConverter: Unknown property category:\t"+er.property_category);
			}
			
		
		} else if (er.property_name.equals(ExperimentalConstants.strKmHL)
				|| er.property_name.equals(ExperimentalConstants.strBIODEG_HL_HC)) {
			convertKm(er);
		} else if (er.property_name.equals(ExperimentalConstants.strKOC)
				|| er.property_name.equals(ExperimentalConstants.strBCF)) {
			convertBCF(er);
		} else if (er.property_name.equals(ExperimentalConstants.strOH)) {
			convertOH(er);
		} else if (er.property_name.equals(ExperimentalConstants.strCACO2)) {
			convertCACO2(er);
		} else if (er.property_name.equals(ExperimentalConstants.strCLINT)) {
			convertCLINT(er);

		} else if (er.property_name.equals(ExperimentalConstants.str_pKA) || // values usually in log units
				er.property_name.equals(ExperimentalConstants.strLogKOW) || // values usually in log units
				er.property_name.equals(ExperimentalConstants.strLogKOA) || // values usually in log units
				er.property_name.equals(ExperimentalConstants.str_pKAa) || // values usually in log units
				er.property_name.equals(ExperimentalConstants.str_pKAb)) { // values usually in log units
			convertToLog(er);

		} else if (er.property_name.equals(ExperimentalConstants.str_ANDROGEN_RECEPTOR_AGONIST)
				|| er.property_name.equals(ExperimentalConstants.str_ANDROGEN_RECEPTOR_ANTAGONIST)
				|| er.property_name.equals(ExperimentalConstants.str_ANDROGEN_RECEPTOR_BINDING)
				|| er.property_name.equals(ExperimentalConstants.str_ESTROGEN_RECEPTOR_AGONIST)
				|| er.property_name.equals(ExperimentalConstants.str_ESTROGEN_RECEPTOR_ANTAGONIST)
				|| er.property_name.equals(ExperimentalConstants.str_ESTROGEN_RECEPTOR_BINDING)) { // values usually in
																									// fraction
																									// (dimensionless)
			convertBinary(er);

		} else if (er.property_name.equals(ExperimentalConstants.strFUB) || er.property_name.equals(ExperimentalConstants.strTTR_ANSA)) { // values usually in fraction
																			// (dimensionless)
			convertDimensionless(er);
		} else if (er.property_name.equals(ExperimentalConstants.strRBIODEG)) {// binary
			convertBinary(er);
		} else if ((er.property_name.equals(ExperimentalConstants.strMeltingPoint)
				|| er.property_name.equals(ExperimentalConstants.strBoilingPoint)
				|| er.property_name.equals(ExperimentalConstants.strAutoIgnitionTemperature)
				|| er.property_name.equals(ExperimentalConstants.strFlashPoint))) {
			convertTemperature(er);
		} else if (er.property_name.equals(ExperimentalConstants.strDensity) || er.property_name.equals(ExperimentalConstants.strVaporDensity)) {
			convertDensity(er);
		} else if (er.property_name.equals(ExperimentalConstants.strVaporPressure)) {
			convertPressure(er);
		} else if (er.property_name.equals(ExperimentalConstants.strHenrysLawConstant)) {
			convertHenrysLawConstant(er);
		} else if (er.property_name.equals(ExperimentalConstants.strWaterSolubility)) {
			convertSolubility(er);

		} else if (er.property_name.equals(ExperimentalConstants.strViscosity)) {
			convertViscosity(er);

		} else if (er.property_name.equals(ExperimentalConstants.strSurfaceTension)) {
			convertSurfaceTension(er);

		} else if (er.property_name.equals(ExperimentalConstants.strNINETY_SIX_HOUR_FATHEAD_MINNOW_LC50)
				|| er.property_name.equals(ExperimentalConstants.strNINETY_SIX_HOUR_BLUEGILL_LC50)
				|| er.property_name.equals(ExperimentalConstants.strNINETY_SIX_HOUR_SCUD_LC50)) {
		
			//TODO use property_category instead to get all the species at once
			convertSolubility(er);
		
		} else {
//			if(debug) System.out.println("Need to handle property in UnitConverter.convertRecord");
			System.out.println("Need to handle property " + er.property_name
					+ " in UnitConverter.convertRecord, original units=" + er.property_value_units_original);
			er.keep = false;
			er.reason = "Property not handled in UnitConverter.convertRecord";
		}

		if (er.property_value_units_final != null && !er.property_value_units_final.isBlank()
				&& !er.property_value_units_final.equals(ExperimentalConstants.str_C)
				&& !er.property_value_units_final.toLowerCase().contains("log")
				&& !er.property_value_units_final.equals(ExperimentalConstants.str_dimensionless) //for TTR_Binding
				&& !er.property_value_units_final.equals(ExperimentalConstants.str_dimensionless_H)) {// TMM: not sure
																										// we need this
																										// last if
			if ((er.property_value_point_estimate_final != null && er.property_value_point_estimate_final < 0)
					|| (er.property_value_min_final != null && er.property_value_min_final < 0)
					|| (er.property_value_max_final != null && er.property_value_max_final < 0)) {
				
				if (er.keep) {
					er.keep = false;
					er.reason = "Negative value not plausible";
//					System.out.println(er.reason + " for " + er.property_name + " for " + er.chemical_name+":");
//					System.out.println(er.property_value_string+"\n");
				}
			}
		} else if (er.temperature_C != null && er.temperature_C < 0 ) {

			if(er.keep) {
				er.flag=true;
				er.reason = "Negative temperature may be artifact of bad range parsing";				
//				System.out.println("Keep, neg temp: "+er.property_value_string);
				
			} else {
				er.updateNote("Negative temperature may be artifact of bad range parsing");
//				System.out.println("Dont keep, neg temp: "+er.reason+"\t"+er.property_value_string);

			}

		}

//		System.out.println(rec.property_value_units_original+"\t"+rec.property_value_units_final);

	}

	/**
	 * Need to use ideal gas law to convert ppm to g/L
	 * 
	 * @param er
	 * @return
	 */
	private boolean convertInhalationMammalianToxicity(ExperimentalRecord er) {
				
		if (er.property_value_units_original.equals(ExperimentalConstants.str_mL_m3)
				|| er.property_value_units_original.equals("ul/L")) {

			if (er.casrn == null || htDensity.get(er.casrn) == null) {
				er.flag = true;
				er.updateNote("Conversion to mg/L not possible (missing density)");
				System.out.println(er.casrn + "\tConversion to mg/L not possible (missing density)");
				assignFinalFieldsWithoutConverting(er);
				er.property_value_units_final = er.property_value_units_original;
				return false;
			} else {
				double density = htDensity.get(er.casrn);
//				System.out.println(er.casrn+"\tConversion to mg/L using density="+density);
				convertAndAssignFinalFields(er, density / 1000.0);
				er.property_value_units_final = ExperimentalConstants.str_g_L;
				er.updateNote("Converted using density: " + density + " g/mL");
			}
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mL_L)) {

			if (er.casrn == null || htDensity.get(er.casrn) == null) {
				er.flag = true;
				er.updateNote("Conversion to mg/L not possible (missing density)");

				System.out.println(er.casrn + "\tConversion to mg/L not possible (missing density)");

				assignFinalFieldsWithoutConverting(er);
				er.property_value_units_final = er.property_value_units_original;
				return false;
			} else {
				double density = htDensity.get(er.casrn);

//					System.out.println(er.casrn+"\tConversion to mg/L using density="+density);

				convertAndAssignFinalFields(er, density);
				er.property_value_units_final = ExperimentalConstants.str_g_L;
				er.updateNote("Converted using density: " + density + " g/mL");
			}
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_L)
				|| er.property_value_units_original.equals(ExperimentalConstants.str_mg_mL)
				|| er.property_value_units_original.equals(ExperimentalConstants.str_kg_m3)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_m3)) {
			// Added by TMM
			convertAndAssignFinalFields(er, 1.0e-6);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_mL)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_log_mg_L)) {
			powConvertAndAssignFinalFields(er, 1.0 / 1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_M)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_log_M)) {
			powAndAssignFinalFields(er);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mM)) {
			convertAndAssignFinalFields(er, 1.0 / 1000.0);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_uM)
				|| er.property_value_units_original.equals("umol/L")) {
			convertAndAssignFinalFields(er, 1.0 / 1000000.0);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_nM)) {
			convertAndAssignFinalFields(er, 1.0 / 1000000000.0);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ppm)
				|| er.property_value_units_original.equals("AI ppm")) {
			er.property_value_units_final = ExperimentalConstants.str_ppm;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
//			https://www.ccohs.ca/oshanswers/chemicals/convert.html
			//Need MW: value in mg/m3 = MW g/mol  * tox ppm / 24.45
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ppb)) {
			convertAndAssignFinalFields(er, 1.0e-3);
			er.property_value_units_final = ExperimentalConstants.str_ppm;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ppt)) {
			convertAndAssignFinalFields(er, 1.0e-6);
			er.property_value_units_final = ExperimentalConstants.str_ppm;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_L)
				|| er.property_value_units_original.equals(ExperimentalConstants.str_ug_mL)
				|| er.property_value_units_original.equals("AI mg/L")) {
			convertAndAssignFinalFields(er, 1.0 / 1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ug_L)
				|| er.property_value_units_original.equals("AI ug/L")) {
//			System.out.println("Converting ug/L");
			convertAndAssignFinalFields(er, 1.0 / 1000000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;

		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ng_L)
				|| er.property_value_units_original.equals("AI ng/L")) {
			convertAndAssignFinalFields(er, 1.0 / 1e9);
			er.property_value_units_final = ExperimentalConstants.str_g_L;

		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_100mL)) {
			convertAndAssignFinalFields(er, 10.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_kg_H20)) {
			// TMM TODO we just have to assume a density of water
			convertAndAssignFinalFields(er, 1.0 / 1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_kg_H20;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");

		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_pct)) {
			// TMM: Gabriel had the following but I dont think we can trust this conversion:
//			convertAndAssignFinalFields(er,10.0);
//			er.property_value_units_final = ExperimentalConstants.str_g_L;
			// TMM: dont convert
			er.property_value_units_final = ExperimentalConstants.str_pct;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_pctWt)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_pctWt;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_pctVol)
				|| er.property_value_units_original.equals("% v/v")) {

			// vol % * (1 ml A/ 100 ml water)/vol % * (1000 ml water / L water) * density (g
			// A / ml A)
//			if (er.casrn==null || htDensity.get(er.casrn) == null) {
//				er.flag = true;
//				er.updateNote("Conversion to mg/L not possible (missing density)");
//				System.out.println(er.casrn+"\tConversion to g/L not possible (missing density)");
//				assignFinalFieldsWithoutConverting(er);
//				er.property_value_units_final = er.property_value_units_original;
//				return false;
//			} else {
//				double density = htDensity.get(er.casrn);
//				convertAndAssignFinalFields(er, 10.0*density);
//				er.property_value_units_final = ExperimentalConstants.str_g_L;
//				
//				System.out.println(er.casrn+"\tConversion from pctVol to g/L using density="+density);
//				System.out.println(er.dsstox_substance_id+"\t"+er.property_value_point_estimate_final+"\t"+er.property_value_units_final);				
//				er.note="Converted using density: "+density+" g/mL";
//			}
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;

		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_100mL)) {
			convertAndAssignFinalFields(er, 1.0 / 100.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_10mL)) {
			convertAndAssignFinalFields(er, 1.0 / 10.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_10mL)) {
			convertAndAssignFinalFields(er, 100.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ug_100mL)) {
			convertAndAssignFinalFields(er, 10.0 / 1000000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_mL)
				|| er.property_value_units_original.equals(ExperimentalConstants.str_g_cm3)) {
			convertAndAssignFinalFields(er, 1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
			// under construction - CR

		} else if (er.property_value_units_original == null) {
			er.keep = false;
			er.reason = "Original units missing";

		} else {
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (unknown units)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;
			if (debug)
				System.out.println(
						"Unrecognized units for " + er.property_name + ": " + er.property_value_units_original);
		}

		return !er.flag;
	}

	private void convertText(ExperimentalRecord er) {
		er.property_value_units_final = ExperimentalConstants.str_binary;
	}

	private void convertBCF(ExperimentalRecord er) {
		
//		System.out.println("enter convert bcf");
		
		if (er.property_value_units_original.equals(ExperimentalConstants.str_L_KG)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_L_KG;
		
//		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_L_g)) {
			//TODO TMM: Some of these L_g values are way off if we convert them- perhaps a typo?
//			er.property_value_units_final = ExperimentalConstants.str_L_KG;
//			convertAndAssignFinalFields(er, 1.0e3);
//		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_L_mg)) {
//			er.property_value_units_final = ExperimentalConstants.str_L_KG;
//			convertAndAssignFinalFields(er, 1.0e6);
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_LOG_L_KG)) {
			powAndAssignFinalFields(er);
			er.property_value_units_final = ExperimentalConstants.str_L_KG;

		} else if(er.property_value_units_original.equals("RA") ||
				er.property_value_units_original.equals("--") ||
				er.property_value_units_original.equals("NA")||
				er.property_value_units_original.equals("NR")) {
			
			er.property_value_units_final = er.property_value_units_original;
			er.keep = false;
			er.reason="Uncertain units";			
			assignFinalFieldsWithoutConverting(er);
		
		} else {			
			er.property_value_units_final = er.property_value_units_original;
			er.keep = false;
			er.reason="Units can't be reliably converted yet";			
			assignFinalFieldsWithoutConverting(er);
//			System.out.println("convertBCF: Need to handle "+er.property_value_units_original);	
		}
		
		
		
	}

	private void convertCACO2(ExperimentalRecord er) {
		if (er.property_value_units_original.equals(ExperimentalConstants.str_CM_SEC)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_CM_SEC;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_LOG_CM_SEC)) {
			powAndAssignFinalFields(er);
			er.property_value_units_final = ExperimentalConstants.str_CM_SEC;
		}
	}

	private void convertOH(ExperimentalRecord er) {
		if (er.property_value_units_original.equals(ExperimentalConstants.str_CM3_MOLECULE_SEC)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_CM3_MOLECULE_SEC;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_LOG_CM3_MOLECULE_SEC)) {
			powAndAssignFinalFields(er);
			er.property_value_units_final = ExperimentalConstants.str_CM3_MOLECULE_SEC;
		}
	}

	private void convertToLog(ExperimentalRecord er) {
		if (er.property_value_units_original.equals(ExperimentalConstants.str_LOG_UNITS)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_LOG_UNITS;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_dimensionless)) {
			logAndAssignFinalFields(er);
			er.property_value_units_final = ExperimentalConstants.str_LOG_UNITS;
		}
	}

	private void convertDimensionless(ExperimentalRecord er) {

		if (er.property_value_units_original.equals(ExperimentalConstants.str_dimensionless)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_dimensionless;
		} else {
			System.out.println("UnitConverter.convertDimensionless: " + er.property_name + " units = "
					+ er.property_value_units_original);
		}
	}

	private void convertBinary(ExperimentalRecord er) {

		if (er.property_value_units_original.equals(ExperimentalConstants.str_binary)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_binary;
		} else {
			System.out.println("UnitConverter.convertBinary: " + er.property_name + " units = "
					+ er.property_value_units_original);
		}
	}

	private void convertCLINT(ExperimentalRecord er) {
		if (er.property_value_units_original.equals(ExperimentalConstants.str_UL_MIN_1MM_CELLS)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_UL_MIN_1MM_CELLS;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_LOG_UL_MIN_1MM_CELLS)) {
			powAndAssignFinalFields(er);
			er.property_value_units_final = ExperimentalConstants.str_UL_MIN_1MM_CELLS;
		}
	}

	private boolean convertKm(ExperimentalRecord er) {

		if (er.property_value_units_original.equals(ExperimentalConstants.str_DAYS)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_DAYS;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_LOG_DAYS)) {
			powAndAssignFinalFields(er);
			er.property_value_units_final = ExperimentalConstants.str_DAYS;
		}

		return !er.flag;
	}

	/**
	 * TODO Gabriel check this- used for echemportal toxicity values...
	 * 
	 * @param er
	 * @return
	 */
	private boolean convertOralMammalianToxicity(ExperimentalRecord er) {

		if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_kg)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_mg_kg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_kg)) {
			convertAndAssignFinalFields(er, 1000.0);
			er.property_value_units_final = ExperimentalConstants.str_mg_kg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mL_kg)) {
			if (er.casrn == null || htDensity.get(er.casrn) == null) {
				er.flag = true;
				er.updateNote("Conversion to mg/kg not possible (missing density)");
				assignFinalFieldsWithoutConverting(er);
				er.property_value_units_final = er.property_value_units_original;
				return false;
			} else {
				double density = htDensity.get(er.casrn);
				convertAndAssignFinalFields(er, density * 1000.0);
				er.property_value_units_final = ExperimentalConstants.str_mg_kg;
				er.updateNote("Converted using density: " + density + " g/mL");
			}
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg)) {
			er.flag = true;
			er.updateNote("Conversion to mg/kg not possible (dimensions differ)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;
		} else {
			er.flag = true;
			er.updateNote("Conversion not possible (unknown units)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;
			if (debug)
				System.out.println(
						"Unrecognized units for " + er.property_name + ": " + er.property_value_units_original);
		}

		return !er.flag;
	}

	private void convertTemperature(ExperimentalRecord er) {
		if (er.property_value_units_original.equals(ExperimentalConstants.str_C)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_C;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_F)) {
			F_to_C(er);
			er.property_value_units_final = ExperimentalConstants.str_C;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_K)) {
			K_to_C(er);
			er.property_value_units_final = ExperimentalConstants.str_C;
		} else {
			er.flag = true;
			er.updateNote("Conversion to C not possible (unknown units)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;
			if (debug)
				System.out.println(
						"Unrecognized units for " + er.property_name + ": " + er.property_value_units_original);
		}
	}

	private void convertDensity(ExperimentalRecord er) {

		
		if (er.property_value_units_original != null && !er.property_value_units_original.equals(ExperimentalConstants.str_dimensionless)) {
			
			if (er.property_value_units_original.equals(ExperimentalConstants.str_g_cm3)
					|| er.property_value_units_original.equals(ExperimentalConstants.str_kg_L)
					|| er.property_value_units_original.equals(ExperimentalConstants.str_g_mL)
					|| er.property_value_units_original.equals(ExperimentalConstants.str_kg_dm3)) {
				
				assignFinalFieldsWithoutConverting(er);
				er.property_value_units_final = ExperimentalConstants.str_g_cm3;
			} else if (er.property_value_units_original.equals(ExperimentalConstants.str_kg_m3)
					|| er.property_value_units_original.equals(ExperimentalConstants.str_g_L)) {
				convertAndAssignFinalFields(er, 1.0 / 1000.0);
				er.property_value_units_final = ExperimentalConstants.str_g_cm3;

			} else if (er.property_value_units_original.equals(ExperimentalConstants.str_lb_ft3)) {
				convertAndAssignFinalFields(er, 0.0160185);
				er.property_value_units_final = ExperimentalConstants.str_g_cm3;
			} else if (er.property_value_units_original.equals(ExperimentalConstants.str_lb_gal)) {
				convertAndAssignFinalFields(er, 0.119826);
				er.property_value_units_final = ExperimentalConstants.str_g_cm3;
			} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_L)) {
				convertAndAssignFinalFields(er, 0.000001);
				er.property_value_units_final = ExperimentalConstants.str_g_cm3;
			}

		} else {
			
			if (er.note != null && er.note.contains(ExperimentalConstants.str_relative_density)) {
				
//				System.out.println("assigning:"+er.property_value_string);
				
				assignFinalFieldsWithoutConverting(er);
				er.property_value_units_final = ExperimentalConstants.str_g_cm3;
//				System.out.println("relative density: "+er.property_value_string+"\t"+er.property_value_point_estimate_final);

			} else if (er.note != null && (er.note.contains(ExperimentalConstants.str_relative_gas_density)
					|| er.note.contains(ExperimentalConstants.str_relative_mixture_density))) {
				
				convertAndAssignFinalFields(er, airDensitySTP);
				
//				if(er.property_value_point_estimate_final==null) {
//					System.out.println("couldnt assign vapor/gas:"+er.property_value_string+"\t"+er.property_value_point_estimate_original);;					
//				}
				
				er.property_value_units_final = ExperimentalConstants.str_g_cm3;
//				System.out.println("relative gas/mixture density: "+er.property_value_string+"\t"+er.property_value_point_estimate_final);

			} else {
				if (er.property_value_point_estimate_original != null || er.property_value_min_original != null) {
					er.flag = true;
					er.updateNote("Conversion to g/cm3 not possible (missing units)");
					assignFinalFieldsWithoutConverting(er);
					er.property_value_units_final = er.property_value_units_original;
					
//					System.out.println("no units: "+er.property_value_string+"\t"+er.property_value_point_estimate_final);


					if (debug)
						System.out.println("missing units for " + er.property_name + ": " + er.property_value_string);

				}
			}
		}
	}
	
	private void convertSurfaceTension(ExperimentalRecord er) {
		if (er.property_value_units_original != null
				&& (er.property_value_units_original.equals(ExperimentalConstants.str_dyn_cm) || er.property_value_units_original.equals(ExperimentalConstants.str_mN_m))) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_dyn_cm;

		} else if (er.property_value_units_original != null
					&& (er.property_value_units_original.equals(ExperimentalConstants.str_N_m))) {

			convertAndAssignFinalFields(er, N_m_to_dyn_cm);
			er.property_value_units_final = ExperimentalConstants.str_dyn_cm;
//			System.out.println("ST converted: "+er.property_value_point_estimate_final+"\t"+ er.property_value_units_final);					

		} else if (er.property_value_units_original != null
				&& (er.property_value_units_original.equals(ExperimentalConstants.str_N_cm))) {

			convertAndAssignFinalFields(er, N_cm_to_dyn_cm);
			er.property_value_units_final = ExperimentalConstants.str_dyn_cm;
//			System.out.println("ST converted: "+er.property_value_point_estimate_final+"\t"+ er.property_value_units_final);					

		} else {
			System.out.println("Unrecognized units for " + er.property_name + ": " + er.property_value_string);
			
		}
	}
	
	private void convertViscosity(ExperimentalRecord er) {

		if (er.property_value_units_original.equals(ExperimentalConstants.str_cP)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_cP;
		}	else if (er.property_value_units_original.equals(ExperimentalConstants.str_mP)) {			
			convertAndAssignFinalFields(er, 0.1);
			er.property_value_units_final = ExperimentalConstants.str_cP;		
		}	else if (er.property_value_units_original.equals(ExperimentalConstants.str_uP)) {
			convertAndAssignFinalFields(er, 1e-4);
			er.property_value_units_final = ExperimentalConstants.str_cP;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_P)) {
			convertAndAssignFinalFields(er, 100.0);
			er.property_value_units_final = ExperimentalConstants.str_cP;

		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_Pa_sec)) {
			convertAndAssignFinalFields(er, 1000.0);
			er.property_value_units_final = ExperimentalConstants.str_cP;

		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_uPa_sec)) {
			convertAndAssignFinalFields(er, 1e-3);
			er.property_value_units_final = ExperimentalConstants.str_cP;

		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_cSt)) {

			if (er.casrn == null || htDensity.get(er.casrn) == null) {
				er.flag = true;
				er.updateNote("Conversion to cP not possible (missing density)");
//				System.out.println(er.casrn + "\tConversion to cP not possible (missing density)");
				assignFinalFieldsWithoutConverting(er);
				er.property_value_units_final = er.property_value_units_original;
				
			} else {
				double density = htDensity.get(er.casrn);
				//			System.out.println(er.casrn+"\tConversion to mg/L using density="+density);
				convertAndAssignFinalFields(er, density);
				er.property_value_units_final = ExperimentalConstants.str_cP;
				er.updateNote("Converted using density: " + density + " g/mL");
			}
		
		} else {
			System.out.println("Need to handle "+er.property_value_units_original+" for " + er.property_name + ": " + er.property_value_string);
		} 
		
		
	}

	private void convertPressure(ExperimentalRecord er) {
		if (er.property_value_units_original.equals(ExperimentalConstants.str_log_mmHg)) {
			powAndAssignFinalFields(er);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mmHg)
				|| er.property_value_units_original.equals(ExperimentalConstants.str_torr)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_atm)) {
			convertAndAssignFinalFields(er, atm_to_mmHg);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_hpa)
				|| er.property_value_units_original.equals(ExperimentalConstants.str_mbar)) {
			convertAndAssignFinalFields(er, hPa_to_mmHg);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mpa)) {
			convertAndAssignFinalFields(er, mPa_to_mmHg);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_pa)) {
			convertAndAssignFinalFields(er, Pa_to_mmHg);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_bar)) {
			convertAndAssignFinalFields(er, bar_to_mmHg);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_psi)) {
			convertAndAssignFinalFields(er, psi_to_mmHg);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_kpa)) {
			convertAndAssignFinalFields(er, kPa_to_mmHg);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		}else if (er.property_value_units_original.equals(ExperimentalConstants.str_upa)) {
			convertAndAssignFinalFields(er, uPa_to_mmHg);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		}else if (er.property_value_units_original.equals(ExperimentalConstants.str_npa)) {
			convertAndAssignFinalFields(er, nPa_to_mmHg);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else {
			er.flag = true;
			er.updateNote("Conversion to mmHg not possible (unknown units)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;
			if (debug)
				System.out.println(
						"Unrecognized units for " + er.property_name + ": " + er.property_value_units_original);
		}
	}

	private static boolean convertAquaticToxicity(ExperimentalRecord er) {
		// TODO just use convertSolubility methods but add extra if statements for
		// AI/ae?
//		ug/L
//		mg/L
//		% v/v
//		ul/L
//		g/L
//		uM
//		nM
//		M
//		ppb
//		AI mg/L
//		AI ug/L
//		AI ng/L
//		%
//		umol/L
//		ae mg/L
//		ppm
		return !er.flag;

	}

	private boolean convertSolubility(ExperimentalRecord er) {

		if (er.property_value_units_original.equals(ExperimentalConstants.str_mL_m3)
				|| er.property_value_units_original.equals("ul/L")) {

			if (er.casrn == null || htDensity.get(er.casrn) == null) {
				er.flag = true;
				er.updateNote("Conversion to mg/L not possible (missing density)");

				System.out.println(er.casrn + "\tConversion to mg/L not possible (missing density)");

				assignFinalFieldsWithoutConverting(er);
				er.property_value_units_final = er.property_value_units_original;
				return false;
			} else {
				double density = htDensity.get(er.casrn);

//				System.out.println(er.casrn+"\tConversion to mg/L using density="+density);

				convertAndAssignFinalFields(er, density / 1000.0);
				er.property_value_units_final = ExperimentalConstants.str_g_L;
				er.updateNote("Converted using density: " + density + " g/mL");
			}
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mL_L)) {

			if (er.casrn == null || htDensity.get(er.casrn) == null) {
				er.flag = true;
				er.updateNote("Conversion to mg/L not possible (missing density)");

				System.out.println(er.casrn + "\tConversion to mg/L not possible (missing density)");

				assignFinalFieldsWithoutConverting(er);
				er.property_value_units_final = er.property_value_units_original;
				return false;
			} else {
				double density = htDensity.get(er.casrn);

//					System.out.println(er.casrn+"\tConversion to mg/L using density="+density);

				convertAndAssignFinalFields(er, density);
				er.property_value_units_final = ExperimentalConstants.str_g_L;
				er.updateNote("Converted using density: " + density + " g/mL");
			}
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_L)
				|| er.property_value_units_original.equals(ExperimentalConstants.str_mg_mL)
				|| er.property_value_units_original.equals(ExperimentalConstants.str_kg_m3)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_m3)) {
			// Added by TMM
			convertAndAssignFinalFields(er, 1.0e-6);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_mL)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_log_mg_L)) {
			powConvertAndAssignFinalFields(er, 1.0 / 1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_M)||
				er.property_value_units_original.equals(ExperimentalConstants.str_mol_L)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_log_M)) {
			powAndAssignFinalFields(er);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_neg_log_M)) {
			negPowAndAssignFinalFields(er);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
//			System.out.println(er.casrn+"\t"+er.property_value_point_estimate_final+"\t"+er.property_value_units_final);

		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mM)||
				er.property_value_units_original.equals(ExperimentalConstants.str_mmol_L)) {
			convertAndAssignFinalFields(er, 1.0 / 1000.0);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_uM)
				|| er.property_value_units_original.equals("umol/L")
				|| er.property_value_units_original.equals("mmol/m3")){
			convertAndAssignFinalFields(er, 1.0 / 1000000.0);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_nM)) {
			convertAndAssignFinalFields(er, 1.0 / 1000000000.0);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_L)
				|| er.property_value_units_original.equals(ExperimentalConstants.str_mg_dm3)
				|| er.property_value_units_original.equals(ExperimentalConstants.str_ug_mL)
				|| er.property_value_units_original.equals("AI mg/L")
				|| er.property_value_units_original.equals(ExperimentalConstants.str_ppm)
				|| er.property_value_units_original.equals("AI ppm")) {
//			ppm is same as mg/L according to:
//			https://cfpub.epa.gov/ncer_abstracts/index.cfm/fuseaction/display.files/fileid/14285
			convertAndAssignFinalFields(er, 1.0 / 1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;

		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ppb)
				|| er.property_value_units_original.equals("AI ppb")) {
			convertAndAssignFinalFields(er, 1.0 / 1.0e6);
			// ppb = 1/1000 ppm = 1/1e6 g/L

			er.property_value_units_final = ExperimentalConstants.str_g_L;
//			er.flag = true;
//			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ppt)) {
			convertAndAssignFinalFields(er, 1.0 / 1.0e6);
			// ppb = 1/1000 ppm = 1/1e6 g/L

			er.property_value_units_final = ExperimentalConstants.str_g_L;

		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ug_L)
				|| er.property_value_units_original.equals("AI ug/L")
				|| er.property_value_units_original.equals("ng/ml")) {
//			System.out.println("Converting ug/L");
			convertAndAssignFinalFields(er, 1.0 / 1000000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;

		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ng_L)
				|| er.property_value_units_original.equals("AI ng/L")) {
			convertAndAssignFinalFields(er, 1.0 / 1e9);
			er.property_value_units_final = ExperimentalConstants.str_g_L;

		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_100mL)) {
			convertAndAssignFinalFields(er, 10.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
//		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ppm) || er.property_value_units_original.equals("AI ppm")) {
//			assignFinalFieldsWithoutConverting(er);
//			er.property_value_units_final = ExperimentalConstants.str_ppm;
//			er.flag = true;
//			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_kg_H20)) {
			// TMM TODO we just have to assume a density of water
			convertAndAssignFinalFields(er, 1.0 / 1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_kg_H20;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");

		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_pct)) {
			// TMM: Gabriel had the following but I dont think we can trust this conversion:
//			convertAndAssignFinalFields(er,10.0);
//			er.property_value_units_final = ExperimentalConstants.str_g_L;
			// TMM: dont convert
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_pct;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_pctWt)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_pctWt;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_pctVol)
				|| er.property_value_units_original.equals("% v/v")) {

			// vol % * (1 ml A/ 100 ml water)/vol % * (1000 ml water / L water) * density (g
			// A / ml A)
//			if (er.casrn==null || htDensity.get(er.casrn) == null) {
//				er.flag = true;
//				er.updateNote("Conversion to mg/L not possible (missing density)");
//				System.out.println(er.casrn+"\tConversion to g/L not possible (missing density)");
//				assignFinalFieldsWithoutConverting(er);
//				er.property_value_units_final = er.property_value_units_original;
//				return false;
//			} else {
//				double density = htDensity.get(er.casrn);
//				convertAndAssignFinalFields(er, 10.0*density);
//				er.property_value_units_final = ExperimentalConstants.str_g_L;
//				
//				System.out.println(er.casrn+"\tConversion from pctVol to g/L using density="+density);
//				System.out.println(er.dsstox_substance_id+"\t"+er.property_value_point_estimate_final+"\t"+er.property_value_units_final);				
//				er.note="Converted using density: "+density+" g/mL";
//			}
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;

		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_100mL)) {
			convertAndAssignFinalFields(er, 1.0 / 100.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_10mL)) {
			convertAndAssignFinalFields(er, 1.0 / 10.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_10mL)) {
			convertAndAssignFinalFields(er, 100.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ug_100mL)) {
			convertAndAssignFinalFields(er, 10.0 / 1000000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_mL)
				|| er.property_value_units_original.equals(ExperimentalConstants.str_g_cm3)) {
			convertAndAssignFinalFields(er, 1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_L;
			// under construction - CR
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_kg_H20)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_g_kg_H20;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_kg_kg_H20)) {
			convertAndAssignFinalFields(er, 1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_kg_H20;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_ug_g_H20)) {
			convertAndAssignFinalFields(er, 1.0 / 1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_kg_H20;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_g_100g)) {
			convertAndAssignFinalFields(er, 10.0);
			er.property_value_units_final = ExperimentalConstants.str_g_kg_H20;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mg_100g)) {
			convertAndAssignFinalFields(er, 10.0 / 1000.0);
			er.property_value_units_final = ExperimentalConstants.str_g_kg_H20;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (dimensions differ)");
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_mol_m3_H20)) {
			convertAndAssignFinalFields(er, 1.0 / 1000.0);
			er.property_value_units_final = ExperimentalConstants.str_M;
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (need MW)");
			// end of construction
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_oz_gal)) {
			Double oz_to_g = 28.3495;
			Double gal_to_L = 3.78541;
			convertAndAssignFinalFields(er, oz_to_g / gal_to_L);
			er.property_value_units_final = ExperimentalConstants.str_g_L;

		} else if (er.property_value_units_original == null) {
			er.keep = false;
			er.reason = "Original units missing";

		} else {
			er.flag = true;
			er.updateNote("Conversion to g/L not possible (unknown units)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;
			if (debug)
				System.out.println(
						"Unrecognized units for " + er.property_name + ": " + er.property_value_units_original);
		}

		return !er.flag;
	}

	private boolean convertHenrysLawConstant(ExperimentalRecord er) {
		if (er.property_value_units_original.equals(ExperimentalConstants.str_Pa_m3_mol)) {
			convertAndAssignFinalFields(er, Pa_to_atm);
			er.property_value_units_final = ExperimentalConstants.str_atm_m3_mol;

		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_atm_cm3_mol)) {
			convertAndAssignFinalFields(er, 1e-6);
			er.property_value_units_final = ExperimentalConstants.str_atm_m3_mol;

		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_atm_m3_mol)) {
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = ExperimentalConstants.str_atm_m3_mol;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_atm)) {
			er.flag = true;
			er.updateNote("Conversion to atm-m3/mol not possible (dimensions differ)");
			convertAndAssignFinalFields(er, atm_to_mmHg);
			er.property_value_units_final = ExperimentalConstants.str_mmHg;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_log_atm_m3_mol)) {
			powAndAssignFinalFields(er);
			er.property_value_units_final = ExperimentalConstants.str_atm_m3_mol;
		} else if (er.property_value_units_original.equals(ExperimentalConstants.str_dimensionless_H)
				|| er.property_value_units_original.equals(ExperimentalConstants.str_dimensionless_H_vol)) {
			er.flag = true;
			er.updateNote("Conversion to atm-m3/mol not possible (dimensions differ)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;
		} else {
			er.flag = true;
			er.updateNote("Conversion to atm-m3/mol not possible (unknown units)");
			assignFinalFieldsWithoutConverting(er);
			er.property_value_units_final = er.property_value_units_original;
			if (debug)
				System.out.println(
						"Unrecognized units for " + er.property_name + ": " + er.property_value_units_original);
		}

		return !er.flag;
	}

	private static void convertAndAssignFinalFields(ExperimentalRecord er, double conversionFactor) {
		if (er.property_value_point_estimate_original != null) {
			er.property_value_point_estimate_final = er.property_value_point_estimate_original * conversionFactor;
		}
		if (er.property_value_min_original != null) {
			er.property_value_min_final = er.property_value_min_original * conversionFactor;
		}
		if (er.property_value_max_original != null) {
			er.property_value_max_final = er.property_value_max_original * conversionFactor;
		}
	}

	private static void powConvertAndAssignFinalFields(ExperimentalRecord er, double conversionFactor) {
		if (er.property_value_point_estimate_original != null) {
			er.property_value_point_estimate_final = Math.pow(10.0, er.property_value_point_estimate_original)
					* conversionFactor;
		}
		if (er.property_value_min_original != null) {
			er.property_value_min_final = Math.pow(10.0, er.property_value_min_original) * conversionFactor;
		}
		if (er.property_value_max_original != null) {
			er.property_value_max_final = Math.pow(10.0, er.property_value_max_original) * conversionFactor;
		}
	}
	
	private static void negPowConvertAndAssignFinalFields(ExperimentalRecord er, double conversionFactor) {
		if (er.property_value_point_estimate_original != null) {
			er.property_value_point_estimate_final = Math.pow(10.0, -er.property_value_point_estimate_original)
					* conversionFactor;
		}
		if (er.property_value_min_original != null) {
			er.property_value_min_final = Math.pow(10.0, -er.property_value_min_original) * conversionFactor;
		}
		if (er.property_value_max_original != null) {
			er.property_value_max_final = Math.pow(10.0, -er.property_value_max_original) * conversionFactor;
		}
	}

	private static void negPowAndAssignFinalFields(ExperimentalRecord er) {
		negPowConvertAndAssignFinalFields(er, 1.0);
	}

	
	private static void powAndAssignFinalFields(ExperimentalRecord er) {
		powConvertAndAssignFinalFields(er, 1.0);
	}

	private static void assignFinalFieldsWithoutConverting(ExperimentalRecord er) {
		convertAndAssignFinalFields(er, 1.0);
	}

	private static void logAndAssignFinalFields(ExperimentalRecord er) {
		if (er.property_value_point_estimate_original != null)
			er.property_value_point_estimate_final = Math.log10(er.property_value_point_estimate_original);
		if (er.property_value_min_original != null)
			er.property_value_min_final = Math.log10(er.property_value_min_original);
		if (er.property_value_max_original != null)
			er.property_value_max_final = Math.log10(er.property_value_max_original);
	}

}
