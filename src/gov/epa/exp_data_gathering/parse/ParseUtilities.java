package gov.epa.exp_data_gathering.parse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.Chemidplus.RecordChemidplus.ToxicityRecord;
import gov.epa.exp_data_gathering.parse.TemperatureCondition.TempUnitsResult;

public class ParseUtilities extends Parse {

	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();		

	
	
	/**
	 * Converts propertyValue string to experimental record
	 * 
	 * Outstanding issues:
	 * 
	 * 
	 * 
	 * @param er
	 * @param propertyValue
	 * @return
	 */
    public static boolean getDensity(ExperimentalRecord er, String propertyValue) {
		boolean badUnits = true;
		int unitsIndex = -1;
		propertyValue = propertyValue.replaceAll("([0-9]),([0-9])", "$1.$2");
		propertyValue = propertyValue.replace("lbs/gal","lb/gal");
		
		propertyValue=propertyValue.replace("Specific gravity = ","").replace("Specific gravity: ","").replace("Specific gravity ","").replace("Bulk density = ","").replace("Specific Gravity = ", "");
		propertyValue=propertyValue.replace("Density = ","").replace("BULK DENSITY ","");
//		propertyValue=propertyValue.replace("Relative density (water = 1): ","");
				
		propertyValue=propertyValue.replace("> ", ">");
		propertyValue=propertyValue.replace("< ", "<");
		propertyValue=propertyValue.replace(" to ", "-");
		
//		propertyValue=propertyValue.replace("°)", "C°)");
		
//		System.out.println(propertyValue);
		
//		propertyValue = propertyValue.replace(" lb at "," lb/gal at ");
		
		String PVLC=propertyValue.toLowerCase(); 
//		String pvlc2=propertyValueNonSplit.toLowerCase();
		
		if(PVLC.contains("will float")) {
			er.property_value_qualitative="will float";
			return false;
		}
		
		if(PVLC.contains("will sink")) {
			er.property_value_qualitative="will sink";
			return false;
		}
		
		if(PVLC.contains("will rise")) {
			er.property_value_qualitative="will rise";
			return false;
		}



		List<String> badProps = Arrays.asList("properties","corros", "odor", "react", "volume",
				"absorption", "particle", "range", "vp", "tension", "buffering", "charge density", "optical",
				"porosity", "atomic density", "1 mg/l=", "1 mg/l =", "1 ppm=", "equivalent", "percent", "correction",
				"conversion", "coefficient", "critical", "radius", "resistivity", "ionization", "heat capacity",
				"conductivity", "mobility", "dispersion", "bp", "logp", "vapor pressure", "magnetic", "viscosity",
				"loss", "equiv", "osmolality", "collision", "liquifies", "explosion", "stability", "storage", "% in",
				"detonation", "friction", "energy", "heat", "enthalpy", "abundance", "dielectric", "activation", "liquid", "0%)","5%)", "kmol", "distillation");

		for (String badProp:badProps) {
			if(PVLC.contains(badProp)) {
				er.keep=false;
				er.reason="Incorrect property";
//				er.updateNote("propertyValue="+propertyValue);
//				System.out.println("Incorrect property:\t"+badProp+"\t"+propertyValue);
				return false;
			}
		}

		//TODO convert volumes to density?
//		if(PVLC.contains("pow")) {
//			System.out.println("pow:"+PVLC);
//		}
		
		if ((PVLC.contains("relative") || PVLC.contains("gas") || PVLC.replace(" ","").contains("air=1") || PVLC.contains("than air")) && !PVLC.contains("(liq") && !PVLC.contains("liquid")) {//fix the relative one
			
//			if(!PVLC.contains("relative") && !PVLC.replace(" ","").contains("air=1") && !PVLC.contains("than air") && !PVLC.contains("saturated air") && !PVLC.contains("gas")) {
//				System.out.println("Mismatch, propertyValue:"+PVLC+"\npropertyValueNonSplit:"+pvlc2+"\n");
//			}
						
			er.property_value_units_original=null;
			
			if (er.source_name.equals(ExperimentalConstants.strSourceEChemPortalAPI)) {
				int relativeIndex = PVLC.indexOf("relative");
				int densityIndex = PVLC.indexOf("density");
				if (densityIndex>=0 && densityIndex<relativeIndex) {
					unitsIndex = densityIndex;
				} else {
					unitsIndex = relativeIndex;
				}

			} else if(PVLC.contains("(ntp")){
				unitsIndex = PVLC.indexOf("(ntp");
			} else if(PVLC.contains("(epa")){
				unitsIndex = PVLC.indexOf("(epa");
			} else if(PVLC.contains("(niosh")){
				unitsIndex = PVLC.indexOf("(niosh");
			} else if(PVLC.contains("(icsc")){
				unitsIndex = PVLC.indexOf("(icsc");
			} else if(PVLC.contains("(nfpa")){
				unitsIndex = PVLC.indexOf("(nfpa");
			} else if(PVLC.contains("(uscg")){
				unitsIndex = PVLC.indexOf("(uscg");	
			} else if((PVLC.contains("relative density of the vapour/air-mixture") || PVLC.contains("relative vapor density")) && PVLC.contains(":")) {
<<<<<<< HEAD
				unitsIndex = propertyValue.length();
=======
                unitsIndex = propertyValue.length();            
>>>>>>> dd07d0425e3df135a8d0b20e058e9d30c45e78cf
			} else if (PVLC.contains("(air")) {
				unitsIndex=PVLC.indexOf("(air");
			} else {
				unitsIndex = propertyValue.length();
			}
			
//			vapor/gas:2.48 (Air = 1)
			
			badUnits = false;
			er.property_value_units_original=ExperimentalConstants.str_dimensionless;



			if (PVLC.contains("mixture") || (PVLC.contains("sat") && PVLC.contains("air"))) {
				
				er.updateNote(ExperimentalConstants.str_relative_mixture_density);
				er.keep=false;
				er.reason="vapour/air-mixture";
				er.property_name=ExperimentalConstants.strVaporDensity;
//				System.out.println("here1 vapour/air-mixture\t"+propertyValueNonSplit);
				
			} else if (PVLC.contains("gas") || PVLC.contains("vapor") || PVLC.contains("vapour") || PVLC.contains("air")) {
				er.updateNote(ExperimentalConstants.str_relative_gas_density);
				er.property_name=ExperimentalConstants.strVaporDensity;
//				System.out.println("here2 gas or air\t"+propertyValue);
			} else {
//				System.out.println("here3\t"+propertyValueNonSplit+"\t"+propertyValue);
//				System.out.println("here3\t"+propertyValue);
				er.updateNote(ExperimentalConstants.str_relative_density);//liquid
			}

		} else if (PVLC.contains("g/cm") || PVLC.contains("g/cu cm") || PVLC.contains("gm/cu cm") || PVLC.contains("gm/cc")) {
			er.property_value_units_original = ExperimentalConstants.str_g_cm3;
			unitsIndex = PVLC.indexOf("g");
			badUnits = false;
			
		} else if (PVLC.contains("kg/cu m")) {
			er.property_value_units_original = ExperimentalConstants.str_kg_m3;
			unitsIndex = PVLC.indexOf("kg/cu m");
			badUnits = false;
			
		} else if (PVLC.contains("g/cu m")) {
//			er.property_value_units_original = ExperimentalConstants.str_g_m3;
			er.property_value_units_original = ExperimentalConstants.str_g_cm3;
			er.updateNote("g/cm^3 assumed instead of g/m^3");
			unitsIndex = PVLC.indexOf("g/cu m");
			badUnits = false;

		} else if (PVLC.contains("mg/ml")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_mL;
			unitsIndex = PVLC.indexOf("mg/ml");
			badUnits = false;
			
		} else if (PVLC.contains("wt/gal") && PVLC.contains("lb")) {
			er.property_value_units_original = ExperimentalConstants.str_lb_gal;
			unitsIndex = PVLC.indexOf("lb");
			badUnits = false;
			
		} else if (PVLC.contains("mg/l")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_L;
			unitsIndex = PVLC.indexOf("g");
			badUnits = false;
		
		} else if (PVLC.contains("g/ml") || PVLC.contains("gm/ml")) {
			er.property_value_units_original = ExperimentalConstants.str_g_mL;
			unitsIndex = PVLC.indexOf("g");
			badUnits = false;
		
		} else if (PVLC.contains("kg/l")) {
			er.property_value_units_original = ExperimentalConstants.str_kg_L;
			unitsIndex = PVLC.indexOf("kg/l");
			badUnits = false;
			
		} else if (PVLC.contains("kg/m")) {
			er.property_value_units_original = ExperimentalConstants.str_kg_m3;
			unitsIndex = PVLC.indexOf("kg/m");
			badUnits = false;
		} else if (PVLC.contains("g/l")) {
			er.property_value_units_original = ExperimentalConstants.str_g_L;
			unitsIndex = PVLC.indexOf("g/l");
			badUnits = false;
		
		} else if (PVLC.contains("lb/cu f")) {
			er.property_value_units_original = ExperimentalConstants.str_lb_ft3;
			unitsIndex = PVLC.indexOf("lb/cu f");
			badUnits = false;

		} else if (PVLC.contains("lb/cubic f")) {
			er.property_value_units_original = ExperimentalConstants.str_lb_ft3;
			unitsIndex = PVLC.indexOf("lb/cubic f");
			badUnits = false;
			
			
		} else if (PVLC.contains("lb/gal")) {
			er.property_value_units_original = ExperimentalConstants.str_lb_gal;
			unitsIndex = PVLC.indexOf("lb/gal");
			badUnits = false;

			
		} else if (PVLC.contains("lbs/cu ft")) {
			er.property_value_units_original = ExperimentalConstants.str_lb_ft3;
			unitsIndex = PVLC.indexOf("lbs/cu ft");
			badUnits = false;
			 
		} else if (PVLC.contains("kg/dm3")) {
			er.property_value_units_original = ExperimentalConstants.str_kg_dm3;
			unitsIndex = PVLC.indexOf("kg/dm3");
			badUnits = false;
			
		
		} else {

			if (er.source_name.equals(ExperimentalConstants.strSourceEChemPortalAPI)) {
				unitsIndex = propertyValue.length();
			
			} else if (propertyValue.contains("°F):")) {
				
				String value=propertyValue.substring(propertyValue.indexOf(":")+1,propertyValue.length());
				String temp=propertyValue.substring(0,propertyValue.indexOf(":")).replace("(","").replace(")", "");
				propertyValue=value+" @ "+temp;//fix the formatting of property value
//				System.out.println(propertyValue);
				
				unitsIndex = propertyValue.length();
				
			} else if (propertyValue.contains(":")) {
				unitsIndex = propertyValue.length();
				
				
				
			} else if (propertyValue.contains(" ")) {
				unitsIndex = propertyValue.indexOf(" ");
			} else {
				unitsIndex = propertyValue.length();
			}
			
			if(er.property_value_units_original==null && er.property_name==ExperimentalConstants.strVaporDensity) {				
//				System.out.println("no units, vapor density, PVLC="+PVLC);
				er.property_value_units_original=ExperimentalConstants.str_dimensionless;
				er.updateNote(ExperimentalConstants.str_relative_gas_density);

			} else {
				
				if(PVLC.equals("0.637 (gas)")) {
					System.out.println("Here found 0.637 gas:"+er.property_value_units_original+"\t"+er.property_name);
				}
				
				badUnits = false;
				er.property_value_units_original = ExperimentalConstants.str_g_cm3;
				er.updateNote(ExperimentalConstants.str_g_cm3+" assumed");
			}
			
			
//			System.out.println(propertyValue+": "+er.note);
		}
		

		if (!PVLC.contains("relative") && !PVLC.replace(" ","").contains("air=1")) {

			if(er.keep && er.property_name.equals(ExperimentalConstants.strDensity)) {
				//				System.out.println(propertyValueNonSplit);

				if((PVLC.contains("sat") && PVLC.contains("air"))) {
					er.keep=false;
					er.reason="vapour/air-mixture";
					er.property_name=ExperimentalConstants.strVaporDensity;
					er.updateNote(ExperimentalConstants.str_relative_mixture_density);

					//					System.out.println("sat air\t"+propertyValueNonSplit);				

				} else if(PVLC.contains("air")) {
					//					er.keep=false;
					//					er.reason="vapour/air-mixture";
					er.property_name=ExperimentalConstants.strVaporDensity;
					//					System.out.println("air\t"+propertyValueNonSplit);
					//					er.updateNote(ExperimentalConstants.str_relative_gas_density);
					//					Air= 1
				} else if (PVLC.contains("gas") || PVLC.contains("vapor") || PVLC.contains("vapour")) {
					er.updateNote("vapor density");
					er.property_name=ExperimentalConstants.strVaporDensity;
					//					System.out.println("gas/vapor\t"+propertyValueNonSplit);				
				}
			}
		}
		

		
		boolean foundNumeric = TextUtilities.getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		
//		if(propertyValue.contains("(NTP, 1992) - Heavier than air")) {
//			System.out.println("NTP3\t"+unitsIndex+"\t"+propertyValue+"\t"+foundNumeric+"\t"+er.property_value_point_estimate_original);
//		}

		
//		if(propertyValue.equals("(77 °F): 1.42")) {
//			System.out.println("Found (77 °F): 1.42\t"+unitsIndex+"\t"+foundNumeric+"\n"+gson.toJson(er));
//		}

		
		if(PVLC.contains("soln") || PVLC.contains("solution")) {
			er.keep=false;
			er.reason="Solution";
			return foundNumeric;
		}

		if(er.property_value_point_estimate_original!=null && er.property_value_point_estimate_original==0) {
			er.keep=false;
			er.reason="Density of zero not possible";
			return foundNumeric;
		}
		
		
//		if(er.keep && er.property_name.equals("Density") && er.property_value_point_estimate_original!=null && er.property_value_point_estimate_original==1) {
//			System.out.println("original=1\t"+propertyValue);
//		}
		
		return foundNumeric;
	}
	
	
	public static boolean getSurfaceTension(ExperimentalRecord er, String propertyValue) {
		boolean badUnits = true;
		int unitsIndex = -1;

		propertyValue=propertyValue.replace("(1.0 mN/m = 1.0 dyn/cm)", "");
		
		propertyValue = propertyValue.replaceAll("([0-9]),([0-9])", "$1.$2");
		
		String pvLC=propertyValue.toLowerCase();
		
//		cu m/s ???
				
		if(pvLC.contains("dyn/cm") || pvLC.contains("dyne/cm") || pvLC.contains("dynes/cm") || pvLC.contains("dyns/cm") || pvLC.contains("dyne cm")) {
			er.property_value_units_original = ExperimentalConstants.str_dyn_cm;
			unitsIndex = pvLC.indexOf("dyn");
			badUnits = false;
//			System.out.println(pvLC+"\t"+unitsIndex);
		
		} else if(pvLC.contains("mn/m") || pvLC.contains("mnm")) {
			er.property_value_units_original = ExperimentalConstants.str_mN_m;
			unitsIndex = pvLC.indexOf("mn");
			badUnits = false;

		} else if(pvLC.contains("millinewton/m")) {
			er.property_value_units_original = ExperimentalConstants.str_mN_m;
			unitsIndex = pvLC.indexOf("millinewton/m");
			badUnits = false;

			
		} else if(pvLC.contains("mn/cm")) {
			er.property_value_units_original = ExperimentalConstants.str_mN_cm;
			unitsIndex = pvLC.indexOf("mn/cm");
			badUnits = false;

		} else if(pvLC.contains("n/cm")) {
			er.property_value_units_original = ExperimentalConstants.str_N_cm;
			unitsIndex = pvLC.indexOf("n/cm");
			badUnits = false;

		} else if(pvLC.contains("newtons/m")) {
			er.property_value_units_original = ExperimentalConstants.str_N_m;
			unitsIndex = pvLC.indexOf("newtons/m");
			badUnits = false;
		} else if(pvLC.contains("n.m")) {
			er.property_value_units_original = ExperimentalConstants.str_N_m;
			unitsIndex = pvLC.indexOf("n.m");
			badUnits = false;
		} else if(pvLC.contains("n/m")) {
			er.property_value_units_original = ExperimentalConstants.str_N_m;
			unitsIndex = pvLC.indexOf("n/m");
			badUnits = false;
		
		} else  {
//			System.out.println("ST="+propertyValue);
		}
		

		boolean foundNumeric = TextUtilities.getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		return foundNumeric;
	}
	
	/**
	 * See https://www.engineeringtoolbox.com/viscosity-converter-d_413.html
	 * 
	 * @param er
	 * @param propertyValue
	 * @return
	 */
	public static boolean getViscosity(ExperimentalRecord er, String propertyValue,String propertyValueNonSplit) {
		
		boolean badUnits = true;
		int unitsIndex = -1;

		propertyValue=propertyValue.replace("0.3 mm^2/s at 20-25 °C", "0.3 mm^2/s at 22.5 °C");
		
		if(propertyValue.equals("Gas at 101.325 KPa at 25 °C; 0.012 8 m Pa.S; 0.012 8 cP.") || propertyValue.equals("0.012 8 m Pa.S") || propertyValue.equals("0.012 8 cP.")) {
			er.reason="Gas viscosity";
			er.keep=false;
			return false;
		}
		propertyValue = propertyValue.replaceAll("([0-9]),([0-9])", "$1.$2");//convert EU number notation

		if(propertyValueNonSplit.contains("all in")) {
			if(propertyValueNonSplit.contains("mPa.s")) {
				er.property_value_units_original=ExperimentalConstants.str_cP;
				unitsIndex=propertyValue.indexOf(" ");
			} else if(propertyValueNonSplit.contains("uPa.s")) {
				er.property_value_units_original=ExperimentalConstants.str_uPa_sec;
				unitsIndex=propertyValue.indexOf(" ");
			} else {
				System.out.println(propertyValueNonSplit+"\tNeed to detect units");
			}
		} else if (propertyValueNonSplit.contains("cP:") || propertyValueNonSplit.contains("Viscosity in mPa.s (cP):") 
				|| propertyValueNonSplit.contains("Liquid (cP):")) {
			
			if(propertyValue.contains(":") && !propertyValue.contains("Vapor:")) {
//				System.out.println("before\t"+propertyValue);
				propertyValue=propertyValue.substring(propertyValue.indexOf(": ")+2,propertyValue.length());
//				System.out.println("after\t"+propertyValue);
				
			}
			
			er.property_value_units_original=ExperimentalConstants.str_cP;
			unitsIndex=propertyValue.indexOf(" ");
//			System.out.println(propertyValueNonSplit);
		}
		
		 
		
//		List<String> upUnits=Arrays.asList("micropoise","uP");
//		unitsIndex = lookForUnitsInList(er, propertyValue, unitsIndex, upUnits, ExperimentalConstants.str_uP);
//		
//		List<String> mpUnits = Arrays.asList("millipoise","mP");
//		unitsIndex = lookForUnitsInList(er, propertyValue, unitsIndex, mpUnits, ExperimentalConstants.str_mP);

		
		List<String> cpUnits = Arrays.asList("centpoise", "centapoise", "centipoise", "CENTIPOISE", "CENTIPOISES", "centipose",
				"mPa.sec", "mPa-sec", "mPa s", "mPa-s","mP-s", "mPa.s", "mPa*s", "mPaXs", "millipascal second", "mPas",
				"m Pa.S", "mPa.S", "mN/sec/sq m", "mN.s/sq m","mN/s/m", "mN.s.m-2", "millipascal-sec", "mPa S", "CP", "Cp", "cp","cP", "mPa");
		unitsIndex = lookForUnitsInList(er, propertyValue, unitsIndex, cpUnits, ExperimentalConstants.str_cP);
		
		List<String> uPa_sec_Units = Arrays.asList("uPa-sec","uPa.s");
		unitsIndex = lookForUnitsInList(er, propertyValue, unitsIndex, uPa_sec_Units, ExperimentalConstants.str_uPa_sec);
		
		List<String> upUnits=Arrays.asList("micropoise","uP");
		unitsIndex = lookForUnitsInList(er, propertyValue, unitsIndex, upUnits, ExperimentalConstants.str_uP);
		
		List<String> mpUnits = Arrays.asList("millipoise","mP");
		unitsIndex = lookForUnitsInList(er, propertyValue, unitsIndex, mpUnits, ExperimentalConstants.str_mP);
		
		
		List<String> Pa_secUnits = Arrays.asList("Pa.s","Pa-s","Pa-sec","Pa sec","Pa-secec","Pa-sec","Pa*s","pascal-sec");		
		unitsIndex = lookForUnitsInList(er, propertyValue, unitsIndex, Pa_secUnits, ExperimentalConstants.str_Pa_sec);

		List<String> pUnits = Arrays.asList("poises", "poise","POISE","Poise");
		unitsIndex = lookForUnitsInList(er, propertyValue, unitsIndex, pUnits, ExperimentalConstants.str_P);

		List<String> cStUnits = Arrays.asList("centistokes","Centistokes","CENTISTOKE","cSt","cST", 
				"mm^2/s","sq mm/s","sq mm.s","sq m/sec");		
		unitsIndex = lookForUnitsInList(er, propertyValue, unitsIndex, cStUnits, ExperimentalConstants.str_cSt);

		
		if(unitsIndex!=-1) badUnits=false;
		boolean foundNumeric = TextUtilities.getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		return foundNumeric;
	}

	private static int lookForUnitsInList(ExperimentalRecord er, String propertyValue, int unitsIndex, List<String> unitsList,
			String finalUnit) {
		
		if(er.property_value_units_original!=null) 
			return unitsIndex;
		
		
		for(String unit:unitsList) {			
			if(propertyValue.contains(unit)) {
//				propertyValue=propertyValue.replace(cpUnit, finalUnit);//do i need to do this?
				er.property_value_units_original=finalUnit;
				unitsIndex = propertyValue.indexOf(unit);
				if(unit == "mPa") {
					er.note = "mPa*sec unit assumed";
				}
					
//				badUnits=false;
				break;
			}
		}
		return unitsIndex;
	}

	// Applicable for melting point, boiling point, and flash point
	public static boolean getTemperatureProperty(ExperimentalRecord er,String propertyValue) {
		boolean badUnits = true;
//		propertyValue = propertyValue.replace("±", "-").replace("+/-", "-");
		
//		if(er.property_name.equals(ExperimentalConstants.strBoilingPoint)) {
//			if(propertyValue.contains("±")) {
//				System.out.println(propertyValue);
//			}
//		}
		
		
		String PVLC=propertyValue.toLowerCase();
		List<String> badProps = new ArrayList<String>(Arrays.asList("properties","corros", "odor", "react", "volume",
				"absorption", "particle", "vp", "tension", "buffering", "charge density", "optical",
				"porosity", "atomic density", "1 mg/l=", "1 mg/l =", "1 ppm=", "equivalent", "percent", "correction",
				"conversion", "coefficient", "critical", "radius", "resistivity", "ionization", "heat capacity",
				"conductivity", "mobility", "dispersion", "logp", "vapor pressure", "magnetic", "viscosity",
				"loss", "equiv", "osmolality", "collision", "liquifies", "explosion", "stability", "storage", "% in",
				"detonation", "friction", "energy", "heat of", "enthalpy", "abundance", "dielectric", "activation", "loses",
				"volatility", "specific gravity", "pk", "entropy", "coeff", "cps", "specific heat", "refractive index", "specific rotation", "triple point", "stable at"));
		//"range"
		
		if(!er.property_name.contentEquals(ExperimentalConstants.strBoilingPoint)) {
			badProps.add("bp");
			badProps.add("boiling point");
		}
        if(!er.property_name.contentEquals(ExperimentalConstants.strMeltingPoint)) {
            badProps.add("mp");
            badProps.add("fp");//TMM In this case I think fp=mp (but sometimes fp=flash point)
            badProps.add("freezing point");
        }
		
		for (String badProp:badProps) {
			
			if(PVLC.contains(badProp)) {
				
				er.keep=false;
				er.reason="Incorrect property";
//				er.updateNote("parsed propertyValue: "+propertyValue);
								
//				if(er.property_name.equals(ExperimentalConstants.strBoilingPoint))				
//					System.out.println("Incorrect property: "+badProp+"\t"+er.property_name+"\t"+propertyValue);

//				if(er.property_name.equals(ExperimentalConstants.strFlashPoint))				
//					System.out.println("Incorrect property: "+badProp+"\t"+er.property_name+"\t"+propertyValue);
				
				return false;
			}
		}

        TempUnitsResult tempUnitResult = TemperatureCondition.getTemperatureUnits(propertyValue,er);
		
		if(tempUnitResult==null) {
//			if(er.property_name.equals(ExperimentalConstants.strFlashPoint))			
//				System.out.println("cant get temp units for:"+propertyValue);
			
			return false;			
		} else {
			if(tempUnitResult.unitsIndex!=-1) {
				badUnits = false;
				er.property_value_units_original=tempUnitResult.units;
			}
		}
				
//		if(er.property_name.equals(ExperimentalConstants.strFlashPoint))
//			System.out.println(er.property_value_units_original+"\t"+tempUnitResult.unitsIndex+"\t"+propertyValue);

		
		boolean foundNumeric = TextUtilities.getNumericalValue(er,propertyValue, tempUnitResult.unitsIndex,badUnits);
		return foundNumeric;
	}
	
	private static String adjustPropertyValue(ExperimentalRecord er,String propertyValue) {
		
//		String propertyValueOriginal=propertyValue;
		
		propertyValue = propertyValue.replaceAll("([0-9]+%-)?[0-9]+% (m?ethanol|alcohol|EtOH|alc)", "$2"); // Alcohol percentages confuse the parser, so snip them out
		propertyValue = propertyValue.replaceAll("[0-9]+% (M?ETHANOL|ALCOHOL)", "$1"); // Some PubChem records in all caps
		propertyValue = propertyValue.replaceAll("[0-9.]+ ?(M|N) (NaOH|HCl)", "$1 $2"); // Acid/base molarities confuse the parser, so snip them out
		propertyValue = propertyValue.replaceAll(" [Pp][Ee][Rr] ","/"); // Correct usage of "per" in some PubChem records
		
<<<<<<< HEAD
		
		if(propertyValue.toLowerCase().contains("solubility of water in")) {
			//We want the solubility of the chemical in water
			er.keep = false;
			er.reason = "Non-aqueous solubility";
			return propertyValue;
		}

		String[] badSolvents = { "heptane", "hexan", "ethyl", "ether", "benzene", "naoh", "hcl", "chloroform", "ligroin", "acet", "alc",
				"dmso", "hexane", "meoh", "dichloromethane", "dcm", "toluene", "glyc", "oils", "soybean oil",
				"organic solvent", "dmf", "mcoh", "chc1", "xylene", "dioxane", "hydrocarbon", "kerosene", "acid",
				"oxide", "pyri", "carbon tetrachloride", "pet", "anol", "ch3oh", "ch2cl2", "chcl3", "alkali", "dsmo",
				"dma", "buffer", "ammonia water", "pgmea", "water-ethanol solution", "cs2", "mineral oil","lard oil",
				"hydrochloric", "sodium carbonate", "nh4oh", "kh2po4", "ethanol:buffered water", "c2h5oh", "et2o",
				"etoac", "etoh", "ethanol: water", "ethanol:water", "ethanol", "tfa","dichloroethane","dimethoxyethane","bromoethane","tetrachloroethane" };
		
		
//		"dimethyl sulfoxide"
//		"ethylene dichloride"

=======
		String[] badSolvents = {"ether","benzene","naoh","hcl","chloroform","ligroin","acet","alc","dmso","dimethyl sulfoxide","hexane","meoh",
				"dichloromethane","dcm","toluene","glyc","oils","organic solvent","dmf","mcoh","chc1","xylene","dioxane","hydrocarbon","kerosene",
				"acid","oxide","pyri","carbon tetrachloride","pet","anol","ch3oh","ch2cl2","chcl3","alkali","dsmo","dma","buffer","ammonia water","pgmea",
				"water-ethanol solution","cs2","ethylene dichloride","mineral oil","hydrochloric","sodium carbonate","nh4oh","kh2po4","ethanol:buffered water",
				"c2h5oh","et2o","etoac","etoh","ethanol: water","ethanol:water","ethanol", "tetrachloroethane", "xylene", "isopropyl stearate",
				"tfa", "ethanol", "methanol", "ethylenediamine","morpholine","lard oil", "tetrahydrofuran","diglyime", "cottonseed oil",
				"polysorbate 80","pine oil", "dimethylformamide","hydrogen fluoride","dimethyl formamide","olive oil", "ch3cn",
				"methylene chloride", "isophorone", "stoddard solvent", "turpentine", "n-pentane", "pentane", "heptane", "cyclohexanone",
				"2-butanone","mineral spirit", "corn oil", "octane","carbon disulfide", "soybean oil","tetrahydrofuran", "intestinal juice", "methyl ethyl ketone",
				"liquid ammonia", "morpholine", "butan-2-ol", "bromoethane", "methyl cellosolve", "sodium salt", "methyl isobutyl ketone",
				"amyl chloride", "butyl butyrate", "alpha-chloronaphthalene", "dibutyl","diethyl", "peanut oil", "hydroxylic", "potassium salt"};
		
>>>>>>> dd07d0425e3df135a8d0b20e058e9d30c45e78cf
//		if(propertyValue.equals("Soluble (in ethanol)")) {
//			System.out.println("Found1: Soluble (in ethanol)");
//		}
		
//		List<String>solvents=Arrays.asList("chloro","ethyl","ethan","alcohol","prop");

		
		boolean foundWater = false;
		String[] waterSynonyms = {"water (distilled)","water","h2o","aqueous solution"};
		
		for (String solvent:badSolvents) {
			if (!propertyValue.toLowerCase().contains(solvent)) { continue; }
			if (solvent.equals("alc") && propertyValue.toLowerCase().contains("calc")) { continue; } // For obvious reasons
			// Snip out any easily-recognized entries for other solvents
			propertyValue = propertyValue.replaceAll(solvent+": ([ <>~=\\.0-9MmGguLl/@%\\(\\)\\u00B0CcFKPpHh]+)[;,$]","");
			// Check for non-aqueous solvents
			if (er.chemical_name==null || !er.chemical_name.contains(solvent)) {
				foundWater = false;
				// See if there is an aqueous record too
				for (String water:waterSynonyms) {
					// Stop searching if water synonym already found
					if (foundWater) { continue; }
					// If water synonym found, parse out aqueous entry
					if (propertyValue.toLowerCase().contains(water) && !solvent.contains(water)) { // Handles "ammonia water" and similar
						if (propertyValue.toLowerCase().contains("% "+water) || 
								propertyValue.toLowerCase().contains("/"+water) || 
								propertyValue.toLowerCase().contains(water+"/") ||
								propertyValue.toLowerCase().contains("?"+water) ||
								propertyValue.toLowerCase().contains("ethanol in "+water) ||
								propertyValue.toLowerCase().contains("etoh in "+water) ||
								propertyValue.toLowerCase().contains("alkaline") ||
								propertyValue.toLowerCase().contains("acidified")) { continue; } // Ignores records with water mixtures
						foundWater = true;
						boolean parsed = false;
						if (!parsed) {
							Matcher colonFormat1 = Pattern.compile(water+"( solubility)?(: |[ ]?=[ ]?)([ <>~=\\.0-9MmGguLl/@%()\u00B0CcFfKkPpHh]+)[;,$]")
									.matcher(propertyValue.toLowerCase().trim());
							if (colonFormat1.find() && TextUtilities.containsNumber(colonFormat1.group(3))) {
								propertyValue = colonFormat1.group(3);
								parsed = true;
//								er.updateNote("Aqueous entry: "+propertyValue);
							}
						}
						if (!parsed) {
							Matcher colonFormat2 = Pattern.compile("solubility: ([ <>~=\\.0-9MmGguLl/@%()\\u00B0CcFfKkPpHh]+)\\("+water+"\\)[;,$]")
									.matcher(propertyValue.toLowerCase().trim());
							if (colonFormat2.find() && TextUtilities.containsNumber(colonFormat2.group())) {
								propertyValue = colonFormat2.group();
								parsed = true;
//								er.updateNote("Aqueous entry: "+propertyValue);
							}
						}
						if (!parsed) {
							Matcher colonFormat3 = Pattern.compile("@ [0-9]+ \\u00B0[cfk]: "+water+"[ <>~=.*0-9XxMmGguLlat/@%()\\u00B0CcFfKkPpHhWwTtVvOoLl+-]*")
									.matcher(propertyValue.toLowerCase().trim());
							if (colonFormat3.find() && TextUtilities.containsNumber(colonFormat3.group())) {
								propertyValue = colonFormat3.group();
								parsed = true;
//								er.updateNote("Aqueous entry: "+propertyValue);
							}
						}
						if (!parsed) {
							Matcher inWaterFormat1 = Pattern.compile("([<>=~?]{0,2} ?[0-9.]+[ <>~=.*0-9XxMmGguLlat/@%()\\u00B0CcFfKkPpHhWwTtVvOoznLl+-]* ?(g )?(in)? ?)"
									+water+":?( ?(@|at)? ?[ <>~=0-9MmGgLl/@%()\\u00B0CcFfKPpHh.]+)?")
									.matcher(propertyValue.toLowerCase().trim());
							if (inWaterFormat1.find() && TextUtilities.containsNumber(inWaterFormat1.group())) {
								Matcher volMatcher = Pattern.compile("[0-9]+ ?ml ?"+water).matcher(inWaterFormat1.group());
								if (!volMatcher.matches()) {
									propertyValue = inWaterFormat1.group().replaceAll(":","");
									parsed = true;
//									er.updateNote("Aqueous entry: "+propertyValue);
								}
							}
						}
						if (!parsed) {
							Matcher inWaterFormat2 = Pattern.compile("sol(ubility)? in "+water+"([^:]*: | )([<>~=]{0,2}[0-9.]+[ <>~=.*0-9XxMmGguLlat/@%()\\u00B0CcFfKkPpHhWwTtVvOoLl+-]*)( in)?")
									.matcher(propertyValue.toLowerCase().trim());
							if (inWaterFormat2.find() && inWaterFormat2.group(4)==null) {
								propertyValue = inWaterFormat2.group(3);
								parsed = true;
//								er.updateNote("Aqueous entry: "+propertyValue);
							}
						}
						if (!parsed) {
							Matcher inWaterFormat3 = Pattern.compile("(in )?"+water+"(( ?[(]?(@|at)? ?[0-9.]+ ?\\u00B0[CcFfKk])?[)]?,?:? ([^).,;$]*((?<=[0-9])[\\.,][0-9])?([^).;$]|,(?! ))*)*[).,;$[^\\p{Graph}]])")
									.matcher(propertyValue.toLowerCase().trim());
							if (inWaterFormat3.find() && TextUtilities.containsNumber(inWaterFormat3.group())) {
								propertyValue = inWaterFormat3.group();
								parsed = true;
//								er.updateNote("Aqueous entry: "+propertyValue);
							}
						}
						if (!parsed) {
							Matcher qualMatcher = Pattern.compile("(ly |in)sol(uble)? in (cold |hot |warm |boiling )?"+water).matcher(propertyValue.toLowerCase());
							if (qualMatcher.find()) {
								return null;
							}
						}
						if (!parsed) {
							er.keep = false;
							er.reason = "Bad data or units";
						}
					}
				}
				// If no water synonyms found, mark record as bad
				if (!foundWater) {
					er.keep = false;
					er.reason = "Non-aqueous solubility";
				}
			}
		}
		
		return propertyValue;
	}
	
	

	

	public static boolean getWaterSolubility(ExperimentalRecord er,String propertyValue,String sourceName) {
		if (propertyValue==null) { return false; }
		
		// Don't waste time looking for a numerical entry if there are no numbers in the string!
		if (!TextUtilities.containsNumber(propertyValue)) { return false; }
		
		String PVLC=propertyValue.toLowerCase();
		
		if (PVLC.contains("parts")) { return false; } // No way to interpret mass vs. volume vs. molar for "parts" ratio records
		if (PVLC.contains("water (6:3:1)")) { return false; } // Weird entry with a water mixture

		
		//TODO: Water solubility has very specific units- do we need following?		
		List<String> badProps = new ArrayList<String>(Arrays.asList("properties","corros", "odor", "volume",
				"absorption", "particle", "vp", "bp", "tension", "buffering", "charge density", "optical",
				"porosity", "atomic density", "1 mg/l=", "1 mg/l =", "1 ppm=", "equivalent", "percent", "correction",
				"conversion", "coefficient", "critical", "radius", "resistivity", "ionization", "heat capacity",
				"conductivity", "mobility", "dispersion", "logp", "vapor pressure", "magnetic", "viscosity",
				"loss", "equiv", "osmolality", "collision", "liquifies", "explosion", "stability", "storage", 
				"detonation", "friction", "energy", "heat of", "enthalpy", "abundance", "dielectric", "activation", "ph of 1%", "ph of 10%", "ph (1% aq", "ph of a 1% aq", "ph of 3%", "ph of a 2%", "1% soln"));
		
		for (String badProp:badProps) {
			if(PVLC.contains(badProp)) {
				er.keep=false;
				er.reason="Incorrect property";
				er.updateNote("parsed propertyValue: "+propertyValue);
//				System.out.println("Incorrect property: "+badProp+"\t"+er.property_name+"\t"+propertyValue);
				return false;
			}
		}

		propertyValue = propertyValue.replaceAll("([0-9]),([0-9]{3})", "$1$2");
		propertyValue = propertyValue.replaceAll("([0-9]),([0-9])", "$1\\.$2");
		propertyValue = propertyValue.replaceAll("([Hh])20", "$12O"); // Yes, "H20" instead of "H2O" has actually caused problems for the parser...
//		propertyValue = correctDegreeSymbols(propertyValue);
		
		propertyValue = propertyValue.replace("mcg/mL","ug/mL").replace("mcg/ml", "ug/mL");//otherwise omits the micro

		//Fix following otherwise it will cause WS=1000:
		if(propertyValue.contains("Solubility in water, g/100ml at ") && propertyValue.contains(":")) {
			
			String val1=propertyValue.substring(propertyValue.indexOf(":")+1, propertyValue.length()).trim();
			String val2=propertyValue.substring(propertyValue.indexOf(" at")+1, propertyValue.indexOf(":")).trim();
			if(val1.contains("(")) {
				val1=val1.replace(" ("," g/100ml (");
			} else {
				val1=val1+" g/100ml";
			}
			propertyValue=val1+" "+val2;
//			System.out.println(propertyValue);
		}
		
		
		if (er.source_name.contains(ExperimentalConstants.strSourcePubChem) || 
				er.source_name.equals(ExperimentalConstants.strSourceLookChem) ||
				er.source_name.equals(ExperimentalConstants.strSourceEChemPortalAPI) ||
				er.source_name.equals(ExperimentalConstants.strSourceChemicalBook)) {
			if (getTextSolubility(er,propertyValue)) { return true; }
			
			if (getSimpleNumericSolubility(er,propertyValue)) { 
//				System.out.println("here1"+gson.toJson(er));
				return true; 
			}
			
			String adjustedPropertyValue = adjustPropertyValue(er,propertyValue);
			if (adjustedPropertyValue==null) {
				return false;
			} else {
				propertyValue = adjustedPropertyValue;
			}
		}
		
		boolean badUnits = true;
		int unitsIndex = getSolubilityUnits(er,propertyValue);
		
		if (unitsIndex > -1) badUnits = false; 
		
		
		
		
		if (badUnits && propertyValue.length() < er.property_value_string.length()) {
			int candidateIndex = getSolubilityUnits(er,er.property_value_string);
			if (candidateIndex > -1) {
				unitsIndex = propertyValue.length();
				badUnits = false;
			}
		}

		if (!er.source_name.equals(ExperimentalConstants.strSourceOFMPub) && unitsIndex < propertyValue.indexOf(":")) {
			unitsIndex = propertyValue.length();
		}
		


		boolean foundNumeric = false;
		if (er.keep) {
			if (er.source_name.contains(ExperimentalConstants.strSourcePubChem) && propertyValue.toLowerCase().contains("ph")) {
				Matcher pHMatcher = Pattern.compile("([0-9.]+) \\(ph [0-9.]+\\)").matcher(propertyValue.toLowerCase());
				if (pHMatcher.find()) {
					er.property_value_point_estimate_original = Double.parseDouble(pHMatcher.group(1));
					foundNumeric = true;
				}
			}
			if (!foundNumeric) {
				foundNumeric = TextUtilities.getNumericalValue(er,propertyValue,unitsIndex,badUnits);
			}
		}
		
//		if(er.property_value_units_original!=null && er.property_value_units_original.equals("%") && er.property_value_point_estimate_original==null && er.property_value_min_original==null && er.keep) {
//			System.out.println(gson.toJson(er));
//		}
		
		//TODO why do ones with % units fail to parse a numeric value
		
//		if(er.property_value_units_original!=null && er.property_value_units_original.equals("%")) {
//			System.out.println(unitsIndex+"\t"+propertyValue+"\t"+foundNumeric);
//		}

		return foundNumeric;
	}
	
	private static int getSolubilityUnits(ExperimentalRecord er, String propertyValue) {
		int unitsIndex = -1;

		String pvLC = propertyValue.toLowerCase();

		if (pvLC.contains("mg/ml")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_mL;
			unitsIndex = pvLC.indexOf("mg/");
		} else if (pvLC.contains("mg/l") || pvLC.contains("mg l-1") || pvLC.contains("mgl-1") || pvLC.contains("mg / l")
				|| (pvLC.contains("mg/1") && !pvLC.contains("mg/10"))) {
			er.property_value_units_original = ExperimentalConstants.str_mg_L;
			unitsIndex = pvLC.indexOf("mg");
		} else if (pvLC.contains("ug/ml") || pvLC.contains("\u00B5g/ml")) {
			er.property_value_units_original = ExperimentalConstants.str_ug_mL;
			unitsIndex = pvLC.indexOf("ug/") == -1 ? pvLC.indexOf("\u00B5g/") : pvLC.indexOf("ug/");
		} else if (pvLC.contains("ug/l") || pvLC.contains("\u00B5g/l")) {
			er.property_value_units_original = ExperimentalConstants.str_ug_L;
			unitsIndex = pvLC.indexOf("ug/") == -1 ? pvLC.indexOf("\u00B5g/") : pvLC.indexOf("ug/");
		} else if (pvLC.contains("g/ml")) {
			er.property_value_units_original = ExperimentalConstants.str_g_mL;
			unitsIndex = pvLC.indexOf("g/");
		} else if (pvLC.contains("g/cm")) {
			er.property_value_units_original = ExperimentalConstants.str_g_cm3;
			unitsIndex = pvLC.indexOf("g/");
		} else if (pvLC.contains("ng/l")) {
			er.property_value_units_original = ExperimentalConstants.str_ng_L;
			unitsIndex = pvLC.indexOf("ng/");
		} else if (pvLC.contains("kg/l")) {
			er.property_value_units_original = ExperimentalConstants.str_kg_L;
			unitsIndex = pvLC.indexOf("kg/");
		} else if (pvLC.contains("g/l")) {
			er.property_value_units_original = ExperimentalConstants.str_g_L;
			unitsIndex = pvLC.indexOf("g/");
		} else if (pvLC.contains("kg/m")) {
			er.property_value_units_original = ExperimentalConstants.str_kg_m3;
			unitsIndex = pvLC.indexOf("kg/");
		} else if (pvLC.contains("kg/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_kg_kg_H20;
			unitsIndex = pvLC.indexOf("kg/");
		} else if (pvLC.contains("g/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_g_kg_H20;
			unitsIndex = pvLC.indexOf("g/");
		} else if (pvLC.contains("mg/100ml") || pvLC.contains("mg/100 ml")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_100mL;
			unitsIndex = pvLC.indexOf("mg/");
		} else if (pvLC.contains("mg/10ml") || pvLC.contains("mg/10 ml") || pvLC.contains("mg/dl")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_10mL;
			unitsIndex = pvLC.indexOf("mg/");
		} else if (pvLC.contains("ug/100ml") || pvLC.contains("ug/100 ml")) {
			er.property_value_units_original = ExperimentalConstants.str_ug_100mL;
			unitsIndex = pvLC.indexOf("ug/");
		} else if (pvLC.contains("g/100ml") || pvLC.contains("g / 100ml") || pvLC.contains("g/ 100 ml")
				|| pvLC.contains("g / 100 ml") || pvLC.contains("g/100 cu cm") || pvLC.contains("g / 100 cc")
				|| pvLC.contains("g/100 ml") || pvLC.contains("g/100 cc")) {
			er.property_value_units_original = ExperimentalConstants.str_g_100mL;
			unitsIndex = pvLC.indexOf("/");
		} else if (pvLC.contains("mg/100g") || pvLC.contains("mg / 100g") || pvLC.contains("mg/ 100 g")
				|| pvLC.contains("mg / 100 g") || pvLC.contains("mg/100 g")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_100g;
			unitsIndex = pvLC.indexOf("/");
		} else if (pvLC.contains("g/100g") || pvLC.contains("g / 100g") || pvLC.contains("g/ 100 g")
				|| pvLC.contains("g / 100 g") || pvLC.contains("g/100 g")) {
			er.property_value_units_original = ExperimentalConstants.str_g_100g;
			unitsIndex = pvLC.indexOf("/");
		} else if (pvLC.contains("g/10ml") || pvLC.contains("g/10 ml")) {
			er.property_value_units_original = ExperimentalConstants.str_g_10mL;
			unitsIndex = pvLC.indexOf("g/");
		} else if (pvLC.contains("% w/w") || pvLC.contains("wt%") || pvLC.contains("wt percent")
				|| pvLC.contains("% wt") || pvLC.contains("% (w/w)") || pvLC.contains("weight %")
				|| pvLC.contains("wt %") || pvLC.contains("% by wt") || (pvLC.contains("%") && pvLC.contains("wt"))) {
			er.property_value_units_original = ExperimentalConstants.str_pctWt;
			unitsIndex = Math.max(propertyValue.indexOf("%"), propertyValue.indexOf("percent"));
		} else if (pvLC.contains("vol%")) {
			er.property_value_units_original = ExperimentalConstants.str_pctVol;
			unitsIndex = propertyValue.indexOf("vol");
		} else if (pvLC.contains("%")) {
			er.property_value_units_original = ExperimentalConstants.str_pct;
			unitsIndex = propertyValue.indexOf("%");
		} else if (pvLC.contains("ppm")) {
			er.property_value_units_original = ExperimentalConstants.str_ppm;
			unitsIndex = pvLC.indexOf("ppm");
		} else if (pvLC.contains("ppb")) {
			er.property_value_units_original = ExperimentalConstants.str_ppb;
			unitsIndex = pvLC.indexOf("ppb");
		} else if (pvLC.contains("mmol/l")) {
			er.property_value_units_original = ExperimentalConstants.str_mM;
			unitsIndex = pvLC.indexOf("mmol");
		} else if (checkMolarUnits(ExperimentalConstants.str_mM, propertyValue) > -1) {
			er.property_value_units_original = ExperimentalConstants.str_mM;
			unitsIndex = checkMolarUnits(ExperimentalConstants.str_mM, propertyValue);
		} else if (checkMolarUnits(ExperimentalConstants.str_uM, propertyValue) > -1) {
			er.property_value_units_original = ExperimentalConstants.str_uM;
			unitsIndex = checkMolarUnits(ExperimentalConstants.str_uM, propertyValue);
		} else if (pvLC.contains("mol/l") || pvLC.contains("mols/l")) {
			er.property_value_units_original = ExperimentalConstants.str_M;
			unitsIndex = pvLC.indexOf("mol");
		} else if (checkMolarUnits(ExperimentalConstants.str_M, propertyValue) > -1) {
			er.property_value_units_original = ExperimentalConstants.str_M;
			unitsIndex = checkMolarUnits(ExperimentalConstants.str_M, propertyValue);
		} else if (pvLC.contains("oz/gallon")) {
			er.property_value_units_original = ExperimentalConstants.str_oz_gal;
			unitsIndex = pvLC.indexOf("oz/");
		} else if (er.source_name == ExperimentalConstants.strSourceEChemPortalAPI) {
			unitsIndex = ExtraEChemPortalRecords(propertyValue, er, unitsIndex);
		}

		return unitsIndex;
	}
	
	private static int checkMolarUnits(String units,String propertyValue) {
		int index = -1;
		int checkUnitsLength = -1;
		switch (units) {
		case ExperimentalConstants.str_mM:
			index = propertyValue.toLowerCase().indexOf("mm");
			checkUnitsLength = 2;
			break;
		case ExperimentalConstants.str_uM:
			index = Math.max(propertyValue.toLowerCase().indexOf("um"),propertyValue.toLowerCase().indexOf("\u00B5m"));
			checkUnitsLength = 2;
			break;
		case ExperimentalConstants.str_M:
			index = propertyValue.toLowerCase().indexOf("m");
			checkUnitsLength = 1;
			break;
		}
		
		if (index==-1) { return index; }
		
		boolean standalone = false;
		boolean before = index > 0 && (Character.isDigit(propertyValue.charAt(index-1)) || Character.isWhitespace(propertyValue.charAt(index-1)));
		boolean after = (index+checkUnitsLength < propertyValue.length() && !Character.isAlphabetic(propertyValue.charAt(index+checkUnitsLength)))
				|| index == propertyValue.length()-1;
		standalone = before && after;
		
		if (standalone) { 
			return index;
		} else {
			return -1;
		}
	}
	
	private static int ExtraEChemPortalRecords(String propertyValue, ExperimentalRecord er, int unitsIndex) {
		if (propertyValue.toLowerCase().contains("mg/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_kg_H20;
			return unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
		} else if (propertyValue.toLowerCase().contains("g/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_g_kg_H20;
			return unitsIndex = propertyValue.toLowerCase().indexOf("g/");
		} else if (propertyValue.toLowerCase().contains("mol/m3")) {
			er.property_value_units_original = ExperimentalConstants.str_mol_m3_H20;
			return unitsIndex = propertyValue.toLowerCase().indexOf("mol");
		}
		Matcher microGperG = Pattern.compile("(\\u00B5g/g)").matcher(propertyValue.trim());
		if (microGperG.find()) {
//			System.out.println(microGperG.group(1));
			er.property_value_units_original = ExperimentalConstants.str_ug_g_H20;
			return propertyValue.toLowerCase().indexOf(microGperG.group(1));
		}
		Matcher microGAIperG = Pattern.compile("(\\u00B5g[ ]?a.i./)").matcher(propertyValue.trim());
		if (microGAIperG.find()) {
//			System.out.println(microGAIperG.group(1));
			er.property_value_units_original = ExperimentalConstants.str_ug_L;
			return propertyValue.toLowerCase().indexOf(microGAIperG.group(1));
		}
		Matcher microGpermL = Pattern.compile("(micrograms(per mL)?(/milliliter)?)").matcher(propertyValue.trim());
		if (microGpermL.find()) {
//			System.out.println(microGpermL.group(1));
			er.property_value_units_original = ExperimentalConstants.str_ug_mL;
			return propertyValue.toLowerCase().indexOf(microGpermL.group(1));
		}
		return unitsIndex;
	}
	
	
	public static void getQualitativeSolubility(ExperimentalRecord er, String propertyValue,String sourceName) {
		boolean gotQualitativeSolubility = false;
		String pvLC = propertyValue.toLowerCase().replace("(ntp, 1992)", "").replace("(niosh, 2023)","").replace("(nfpa, 2010)","").replace("Solubility in water:","").trim();
		
		String solventMatcherStr = "";
		if (sourceName.equals(ExperimentalConstants.strSourceLookChem)) {
			solventMatcherStr = "(([a-zA-Z0-9\\s-%]+?)(,| and|\\.|\\z|[ ]?\\(|;))?";
		} else if (sourceName.contains(ExperimentalConstants.strSourcePubChem)) {
			solventMatcherStr = "(([a-zA-Z0-9\\s,-]+?)(\\.|\\z| at| and only|\\(|;|[0-9]))?";
		}
		Matcher solubilityMatcher = Pattern.compile("(([a-zA-Z]+y[ ]?)?([a-zA-Z]+y[ ]?)?(i[nm])?(s[ou]l?uble|miscible|sol(?!u)))( [\\(]?(in|with) )?[[ ]?\\.{3}]*"+solventMatcherStr).matcher(pvLC);
		while (solubilityMatcher.find()) {

			try {

				String qualifier = solubilityMatcher.group(1);
				String prep = solubilityMatcher.group(6);
				String solvent = solubilityMatcher.group(9);
				
				if (solvent == null || solvent.length() == 0 || (solvent.contains("water") && !solvent.contains("alc"))
						|| (solvent.contains("aqueous solution") && !solvent.contains("alkaline"))) {
					er.property_value_qualitative = qualifier;
					gotQualitativeSolubility = true;
				} else {
					prep = prep == null ? " " : prep;
					er.updateNote(qualifier + prep + solvent);
					gotQualitativeSolubility = true;
				}
			
			} catch (Exception ex) {
			
				//TODO TMM 2024-03-22, could handle with regex but this fix works:
				if(propertyValue.contains("SOL IN ALL PROPORTIONS IN WATER") || 
						propertyValue.contains("Solubility in water: soluble")||
						propertyValue.contains("Soluble in water")) {
					
					er.property_value_qualitative = propertyValue;
					gotQualitativeSolubility = true;					
				}
				
			}
		}
		
		if (er.note!=null) { er.note = er.note.replaceAll("[;.,];", ";"); } // Regex leaves double punctuation sometimes

		if (pvLC.contains("reacts") || pvLC.contains("reaction")) {
			er.property_value_qualitative = "reacts";
			gotQualitativeSolubility = true;
		}

		if (pvLC.contains("hydrolysis") || pvLC.contains("hydrolyse") || pvLC.contains("hydrolyze")) {
			er.property_value_qualitative = "hydrolysis";
			gotQualitativeSolubility = true;
		}

		if (pvLC.contains("decompos")) {
			er.property_value_qualitative = "decomposes";
			gotQualitativeSolubility = true;
		}

		if (pvLC.contains("autoignition")) {
			er.property_value_qualitative = "autoignition";
			gotQualitativeSolubility = true;
		}
		
		

		String[] qualifiers = {"none","very poor","poor","low","negligible","slight","significant","complete"};
		for (String qual:qualifiers) {
			if ((pvLC.startsWith(qual) || (pvLC.contains("solubility in water") && pvLC.contains(qual))) &&
					(er.property_value_qualitative==null || er.property_value_qualitative.isBlank())) {
				er.property_value_qualitative = qual;
				gotQualitativeSolubility = true;
			}
		}

		if (gotQualitativeSolubility && er.reason!=null && !er.reason.toLowerCase().contains("non-aqueous solubility")) {
			er.keep = true;
			er.reason = null;
		}
		
//		List<String>bads=Arrays.asList("soluble (in ethanol)","miscible (in ethanol)",
//				"slightly soluble in proylene glycol","sparingly soluble (in ethanol)","slightly sol in ethanol",
//				"slightly soluble in chloroform",
//				"SLIGHTLY SOL IN ETHER".toLowerCase());
//		
//		for(String bad:bads) {
//			if(pvLC.equals(bad)) {
//				er.property_value_qualitative=null;
//			}
//		}
		
		List<String>waters=Arrays.asList("water","h2o","aqueous solution");
		
		boolean haveWater=false;
		
		for(String water:waters) {
			if(pvLC.contains(water)) {
				haveWater=true;
				break;
			}
		}
				
		//If match these exact strings we are ok		
		List<String> goods = Arrays.asList("sparingly soluble", "very soluble", "soluble", "slightly", "insoluble",
				"slight", "miscible", "insoluble", "insoluble.", "freely soluble",
				"low solubility", "quite soluble", "insoluble (mesylate form)", "slightly soluble in cold",
				"soluble (tartrate form)", "practically insoluble.", "insoluble.",
				"very slightly","very slightly soluble", "practically insoluble", "very poor solubility", "relatively insoluble",
				"slightly solubility", "completely miscible", "moderately soluble", "sparingly soluble",
				"partially soluble", "slightly soluble", "almost insoluble", "slighty soluble", "readily soluble",
				"partly miscible", "poorly soluble", "partly soluble", "highly soluble", "not soluble.", "not soluble",
				"non-soluble", "negligible", "immiscible", "complete", "poor", "low","good","very good","moderate");

				
		if(er.property_value_qualitative!=null && !haveWater && !goods.contains(pvLC)) {
//			System.out.println(er.property_value_qualitative+"\t"+pvLC);
			er.property_value_qualitative=null;//not water and not a good one
		}
		
		if(er.property_value_qualitative==null) {
			if(pvLC.contains("react")) {
				er.property_value_qualitative="reacts";
			}
		}
		
		if(er.property_value_qualitative!=null) {
			if (er.property_value_qualitative.contains("sol") && !er.property_value_qualitative.contains("soluble")) {
				er.property_value_qualitative=er.property_value_qualitative.replace("sol","soluble");
			}
			
			if(er.property_value_qualitative.equals("reaction")) 
				er.property_value_qualitative="reacts";
		}
		
		
//		if(gotQualitativeSolubility && er.property_value_qualitative!=null && !propertyValue.contains("water")) {
//			System.out.println(er.property_value_qualitative+"\t"+propertyValue);
//		}
	}

	public static boolean getVaporPressure(ExperimentalRecord er,String propertyValue) {
		
		if (propertyValue.toLowerCase().contains(ExperimentalConstants.str_negl)) {
			er.property_value_qualitative = ExperimentalConstants.str_negl;
		} else if (propertyValue.toLowerCase().contains("very low")) {
			er.property_value_qualitative = "very low";
		} else if (propertyValue.toLowerCase().contains("extremely low")) {
			er.property_value_qualitative = "extremely low";
		} else if (propertyValue.toLowerCase().contains("low")) {
			er.property_value_qualitative = "low";
		}
		
		if(propertyValue.contains("(approx)")) {
			er.keep=false;
			er.reason="Approximate value";
			return false;
		}
		
		//For right now, keep antoine extrapolations
//		if(propertyValue.contains("equation ")) {
//			er.keep=false;
//			er.reason="Approximate value";
//			return false;
//		}
		if(propertyValue.equals("Vapor pressure between 511 and 835 °C is given by equation logP(kPa)= 6.7249-(5960.2/K)")) {
			er.keep=false;
			er.reason="Antoine equation at high temperature";
		}
		
		if(propertyValue.contains("log P")) {
			er.keep=false;
			er.reason="Wrong Property";
		}
		
		 if (propertyValue.equals("VP: 5 mm Hg at 80 to 81 mm Hg /L-alpha-Terpineol/")) {
			er.keep=false;
			er.reason="Bad data or units";
		 }
		
		
		boolean badUnits = true;
		int unitsIndex = -1;
		propertyValue = propertyValue.replaceAll("([0-9]),([0-9]{3})", "$1$2");
		
		if(propertyValue.contains("Vapor pressure, Pa at ")) {
			String propVal=propertyValue.substring(propertyValue.indexOf(":")+1, propertyValue.length()).trim();
			String temp=propertyValue.substring(propertyValue.indexOf("at"), propertyValue.indexOf(":")).trim();
			propertyValue=propVal+" Pa "+temp;
//			System.out.println(propertyValueNonSplit+"\t"+propertyValue);
//			Vapor pressure, Pa at 60 °C: 0.013	
		} else if(propertyValue.contains("Vapor pressure, kPa at ")) {
			String propVal=propertyValue.substring(propertyValue.indexOf(":")+1, propertyValue.length()).trim();
			String temp=propertyValue.substring(propertyValue.indexOf("at"), propertyValue.indexOf(":")).trim();
			propertyValue=propVal+" kPa "+temp;
//			System.out.println(propertyValueNonSplit+"\t"+propertyValue);
//		} else if(propertyValue.indexOf("VP:")==0 || propertyValue.indexOf("Vapor pressure:")==0) {
//			propertyValue=propertyValue.substring(propertyValue.indexOf(":")+1,propertyValue.length()).trim();
//			System.out.println(propertyValue);
			
		} else if (propertyValue.substring(0,1).equals("(") && propertyValue.contains(":")) {
			String propVal=propertyValue.substring(propertyValue.indexOf(":")+1, propertyValue.length()).trim();
			String temp=propertyValue.substring(0,propertyValue.indexOf(":")).trim();
			propertyValue=propVal+" "+temp;
//			System.out.println(propertyValue);
		}
				
		
		if (propertyValue.toLowerCase().contains("kpa")) {
			er.property_value_units_original = ExperimentalConstants.str_kpa;
			unitsIndex = propertyValue.toLowerCase().indexOf("kpa");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mm")) {
			er.property_value_units_original = ExperimentalConstants.str_mmHg;
			unitsIndex = propertyValue.toLowerCase().indexOf("mm");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("atm")) {
			er.property_value_units_original = ExperimentalConstants.str_atm;
			unitsIndex = propertyValue.toLowerCase().indexOf("atm");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("hpa")) {
			er.property_value_units_original = ExperimentalConstants.str_hpa;
			unitsIndex = propertyValue.toLowerCase().indexOf("hpa");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mpa")) {
			er.property_value_units_original = ExperimentalConstants.str_mpa;
			unitsIndex = propertyValue.toLowerCase().indexOf("mpa");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("upa")) {
			er.property_value_units_original = ExperimentalConstants.str_upa;
			unitsIndex = propertyValue.toLowerCase().indexOf("upa");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("npa")) {
			er.property_value_units_original = ExperimentalConstants.str_npa;
			unitsIndex = propertyValue.toLowerCase().indexOf("npa");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("pa")) {
			er.property_value_units_original = ExperimentalConstants.str_pa;
			unitsIndex = propertyValue.toLowerCase().indexOf("pa");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mbar")) {
			er.property_value_units_original = ExperimentalConstants.str_mbar;
			unitsIndex = propertyValue.toLowerCase().indexOf("mb");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("millibar")) {
			er.property_value_units_original = ExperimentalConstants.str_mbar;
			unitsIndex = propertyValue.toLowerCase().indexOf("millibar");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("bar")) {
			er.property_value_units_original = ExperimentalConstants.str_bar;
			unitsIndex = propertyValue.toLowerCase().indexOf("bar");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("torr")) {
			er.property_value_units_original = ExperimentalConstants.str_torr;
			unitsIndex = propertyValue.toLowerCase().indexOf("torr");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("psi")) {
			er.property_value_units_original = ExperimentalConstants.str_psi;
			unitsIndex = propertyValue.toLowerCase().indexOf("psi");
			badUnits = false;
		}
		
//		if (er.source_name!=ExperimentalConstants.strSourceOFMPub && propertyValue.contains(":")) {
//			unitsIndex = propertyValue.length();
//		}
		
		boolean foundNumeric = TextUtilities.getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		return foundNumeric;
	}

	public static boolean getHenrysLawConstant(ExperimentalRecord er,String propertyValue) {
		boolean badUnits = true;
		int unitsIndex = -1;

		if(propertyValue.contains("Based on measured Henry's law constants reported in literature, the equation")) {
			er.keep=false;
			er.reason="Equation";
			return false;
		}
		
		if(propertyValue.contains("the Henry's Law constant of water (4.34X10-7 atm-cu m/mol)") || propertyValue.contains("Henry's law constant: 7.352x10-5 to 3.505x10-4 MPa-cu m/mol at 4-40 °C")) {
//			System.out.println("Found < water HLC");
			er.keep=false;
			er.reason="Bad data or units";
			return false;
		}
		
	
//		atm-cu cm/mol
//		MPa-cu m/mol 
//		Pa-L/mol		
//		Pa/cu m mole : ill defined
//		atm-cu/mole : ill defined
//		kPa/mol : wrong units

		//While there are a couple records in atm-cu cm/mol, the values dont match sander when converted! Bad units probably
		List<String> valsATM_CM3_MOL = Arrays.asList("atm-cu cm/mol");
		unitsIndex = lookForUnitsInList(er, propertyValue, unitsIndex, valsATM_CM3_MOL, ExperimentalConstants.str_atm_cm3_mol);		

		if(er.property_value_units_original!=null && er.property_value_units_original.equals(ExperimentalConstants.str_atm_cm3_mol)) {
			er.keep=false;
			er.reason="bad data or units";//should be ok but the 2 records dont match sander (by a big margin)
			return false;
		}
		
		List<String> valsATM_M3_MOL = Arrays.asList("amt-cu m/mol", "atm cu m/ mole", "atm-cu m", "atm-cu m /mole", "atm-cu-m/mol", 
				 "atm-m cu/mol", "atm-m3/mol", "atm m³/mol", "atm-cu m/mol", "atm cu m/mol",
				 "atm cu-m/mol",  "atm cu-m\\mol", "atm m^3/mol",
				 "atn-cu m/mol","cu m-atm/mol","cu m atm/mol");
		unitsIndex = lookForUnitsInList(er, propertyValue, unitsIndex, valsATM_M3_MOL, ExperimentalConstants.str_atm_m3_mol);

		List<String> valsPA_M3_MOL=  Arrays.asList("pa m³/mol","pa m^3/mol","Pa-cu m/mol","Pa-cu m/mol");
		unitsIndex = lookForUnitsInList(er, propertyValue, unitsIndex, valsPA_M3_MOL, ExperimentalConstants.str_Pa_m3_mol);
		
				
		unitsIndex = lookForUnitsInList(er, propertyValue.toLowerCase(), unitsIndex, Arrays.asList("dimensionless - vol"), ExperimentalConstants.str_dimensionless_H_vol);
		unitsIndex = lookForUnitsInList(er, propertyValue.toLowerCase(), unitsIndex, Arrays.asList("dimensionless"), ExperimentalConstants.str_dimensionless_H);
		unitsIndex = lookForUnitsInList(er, propertyValue.toLowerCase(), unitsIndex, Arrays.asList("atm"), ExperimentalConstants.str_atm);
	
		if(unitsIndex!=-1) badUnits=false;

		
		boolean foundNumeric = TextUtilities.getNumericalValue(er,propertyValue,unitsIndex,badUnits);

//		if(propertyValue.equals("Henry's Law constant = 1.76X10-5 Pa-cu m/mol /1.74X10-10 atm-cu m/mol/ at 25 °C")) {
//			System.out.println("unitsIndex="+unitsIndex);
//			System.out.println("propertyValue="+propertyValue);
//			System.out.println("unitsOriginal="+er.property_value_units_original);
//			System.out.println("pointEstimateOriginal="+er.property_value_point_estimate_original);
//		}

		
		
		return foundNumeric;
	}

	public static boolean getToxicity(ExperimentalRecord er,String propertyValue) {
		if (propertyValue==null) { return false; }
		boolean badUnits = true;
		int unitsIndex = -1;

		if (propertyValue.toLowerCase().contains("mg/l")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_L;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mg/m^3") || propertyValue.toLowerCase().contains("mg/cu m")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_m3;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("g/m^3") || propertyValue.toLowerCase().contains("g/m³") || propertyValue.toLowerCase().contains("g/m3") ||
				propertyValue.toLowerCase().contains("g/cubic meter")) {
			er.property_value_units_original = ExperimentalConstants.str_g_m3;
			unitsIndex = propertyValue.toLowerCase().indexOf("g/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ml/m3")) {
			er.property_value_units_original = ExperimentalConstants.str_mL_m3;
			unitsIndex = propertyValue.toLowerCase().indexOf("ml/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ppm")) {
			er.property_value_units_original = ExperimentalConstants.str_ppm;
			unitsIndex = propertyValue.toLowerCase().indexOf("ppm");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mg/kg") || propertyValue.toLowerCase().contains("mg /kg")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_kg;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("g/kg") || propertyValue.toLowerCase().contains("gm/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_g_kg;
			unitsIndex = propertyValue.toLowerCase().indexOf("g");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ml/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_mL_kg;
			unitsIndex = propertyValue.toLowerCase().indexOf("ml/");
			badUnits = false;
		} else {
			badUnits = true;
			er.keep = false;
			er.reason = "Unhandled units";
		}
		
		if (propertyValue.toLowerCase().contains("nominal")) {
			er.updateNote("nominal units");
		} else if (propertyValue.toLowerCase().contains("analytical")) {
			er.updateNote("analytical units");
		}

		boolean foundNumeric = TextUtilities.getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		return foundNumeric;
	}

	public static boolean getToxicity(ExperimentalRecord er,ToxicityRecord tr) {
		String propertyValue = tr.NormalizedDose;
		if (propertyValue == null) { return false; }
		boolean badUnits = true;
		int unitsIndex = -1;

		if (propertyValue.toLowerCase().contains("mg/m3")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_m3;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ml/m3")) {
			er.property_value_units_original = ExperimentalConstants.str_mL_m3;
			unitsIndex = propertyValue.toLowerCase().indexOf("ml/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mg/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_mg_kg;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ml/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_mL_kg;
			unitsIndex = propertyValue.toLowerCase().indexOf("ml/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("iu/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_iu_kg;
			unitsIndex = propertyValue.toLowerCase().indexOf("iu/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("units/kg")) {
			er.property_value_units_original = ExperimentalConstants.str_units_kg;
			unitsIndex = propertyValue.toLowerCase().indexOf("units/");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("ppm")) {
			er.property_value_units_original = ExperimentalConstants.str_ppm;
			unitsIndex = propertyValue.toLowerCase().indexOf("ppm");
			badUnits = false;
		} else if (propertyValue.toLowerCase().contains("mg")) {
			er.property_value_units_original = ExperimentalConstants.str_mg;
			unitsIndex = propertyValue.toLowerCase().indexOf("mg");
			badUnits = false;
		}
		
		boolean foundNumeric = TextUtilities.getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		
		String strippedReportedDose = tr.ReportedDose.replaceAll("\\s","");
		Matcher m = Pattern.compile("\\d").matcher(strippedReportedDose);
		if (m.find()) {
			er.property_value_numeric_qualifier = TextUtilities.getNumericQualifier(strippedReportedDose,m.start());
		}
		
		if (strippedReportedDose.contains("H")) {
			String strH=strippedReportedDose.substring(strippedReportedDose.lastIndexOf("/")+1,strippedReportedDose.length()-1);
			try {
				double h=Double.parseDouble(strH);
				//use haber's rule that C*t=k (see DOI: 10.1093/toxsci/kfg213 that shows this might not be great approx)	
				if (h!=4.0) {
					er.property_value_point_estimate_original*=h/4.0;
					er.note="Duration: "+TextUtilities.formatDouble(h)+" H, adjusted to 4 H using Haber's law (conc*time=constant)";
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}			
		}

		return foundNumeric;
	}

	// Applicable for LogKow and pKa
	public static boolean getLogProperty(ExperimentalRecord er,String propertyValue) {
		
		propertyValue=propertyValue.replace(", measured OECD Method 107", "");
		propertyValue=propertyValue.replace(" (OECD Method 107)","");
		propertyValue=propertyValue.replace(" (unstated pH)","");
		propertyValue=propertyValue.replace(", unstated pH","");
		propertyValue=propertyValue.replace(", distilled water","");

		if(propertyValue.equals("log Kow = 3.20 /Kow = 1.6X10+3/")) {
			propertyValue="log Kow = 3.20";
		} else if(propertyValue.equals("Kow = 2.3X10+8 (log Kow 8.36)")) {
			propertyValue="log Kow = 8.36";
		} else if(propertyValue.equals("Kow = <10")) {
			propertyValue="LogKow<1";
		} else if(propertyValue.equals("Kow = 0.13 (pH 7.0) /log Kow = -0.88/")) {
			propertyValue="log Kow = -0.88 (pH 7.0)";
		} else if(propertyValue.equals("Kow = 1600 at 24 °C (log Kow = 3.20)")) {
			propertyValue="log Kow = 3.20 at 24 °C";
		} else if(propertyValue.equals("log Kow = 1.85 (mixture of 70% cis- and 30% trans-2-Butene)")) {
			propertyValue="log Kow = 1.85";
		} else if(propertyValue.equals("Kow = 3.2X10-2 at 25 °C.")) {
			propertyValue="log Kow = -1.49 at 25°C";
		} else if(propertyValue.equals("Kow = 4900")) {
			propertyValue="log Kow = 3.69";
		} else if (propertyValue.length()==4 && propertyValue.contains(",")) {
			propertyValue=propertyValue.replace(",", ".");
		} else if (propertyValue.equals("log Kow = -3.68 (octanol/water); logP = -1.98 (butanol/water), at pH 7")) {
			propertyValue="log Kow = -3.68 at pH 7";
		} else if (propertyValue.equals("log Kow = 5.3 (Milbemycin A3)")) {
			propertyValue="log Kow = 5.3";
		} else if (propertyValue.equals("log Kow = 5.9 (Milbemycin A4)")) {
			propertyValue="log Kow = 5.9";
		} else if (propertyValue.indexOf("Kow=")==0) {
			System.out.println("need to handle propertyValue="+propertyValue);
		}
		//fix cases with pH since it retrieves the pH instead of the property value:
//		log Kow = -2.82 @ pH 7   Need to set unitsIndex to location of @
//		log Kow: -0.89 (pH 4); -1.85 (pH 7); -1.89 (pH 9)  Need to split by ; into separate records
//		log Kow = 0.74 at pH 5 and -1.34 at pH 7  ==> 0.74   Need to split by and 
//		log Kow = -1.5 at pH 5,7, and 9 @ 21 °C

		
		int index_pH=propertyValue.indexOf("pH");
		int index_atSymbol=propertyValue.indexOf("@");
		int indexEstimatedBy=propertyValue.indexOf("(estimated by");
		int indexAverage=propertyValue.indexOf("(average");
		int indexAvg=propertyValue.indexOf("(avg");
		int indexAt=propertyValue.indexOf("at");
		
		List<Integer>indices=Arrays.asList(index_pH,index_atSymbol,indexEstimatedBy,indexAverage,indexAvg,indexAt);
		
		int unitsIndex = 9999;
		
		for(Integer index:indices) {
			if (index!=-1 && index<unitsIndex ) {
				unitsIndex=index;
			}
		}

		if(unitsIndex==9999) unitsIndex=propertyValue.length();
		
		
//		if (propertyValue.contains("(pH")) {
//			unitsIndex = propertyValue.indexOf("(pH");
//		} else if (propertyValue.contains("@")) {
//			unitsIndex = propertyValue.indexOf("@");		
//		} else if(propertyValue.contains("(estimated by")) {
//			unitsIndex = propertyValue.indexOf("(estimated by");
//		} else if(propertyValue.contains("(average")) {
//			unitsIndex = propertyValue.indexOf("(average");
//		} else if(propertyValue.contains("(avg")) {
//			unitsIndex = propertyValue.indexOf("(avg");
//
//		} else if (propertyValue.contains(" at")) {
//			unitsIndex = propertyValue.indexOf("at");
//		} else {
//			unitsIndex = propertyValue.length();
//		}
		
//		if(propertyValue.equals("log Kow = -1.5 at pH 5,7, and 9 @ 21 °C")) {
//			System.out.println("Index="+unitsIndex);
//		}
				
//		System.out.println(propertyValue+"\t"+unitsIndex);
		
		boolean badUnits = false;
		boolean foundNumeric = TextUtilities.getNumericalValue(er,propertyValue,unitsIndex,badUnits);
		return foundNumeric;
	}
	
	public static boolean hasIdentifiers(ExperimentalRecord er) {
		if ((er.casrn==null || er.casrn.isBlank()) && (er.einecs==null || er.einecs.isBlank()) &&
				(er.chemical_name==null || er.chemical_name.isBlank()) && (er.smiles==null || er.smiles.isBlank())) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Strips whitespace and leading zero from CAS RN
	 * @param cas	CAS RN to fix
	 * @return		Fixed CAS RN
	 */
	public static String fixCASLeadingZero(String cas) {
		if (cas!=null && !cas.isBlank()) {
			cas=cas.trim();
			while (cas.substring(0,1).contentEquals("0")) {//trim off zeros at front
				cas=cas.substring(1,cas.length());
			}
			return cas;
		} else {
			return null;
		}
	}
	
	/**
	 * Verifies CAS checksum per https://www.cas.org/support/documentation/chemical-substances/checkdig
	 * 
	 * @param casInput	CAS RN (or sequence of multiple CAS RNs delimited by pipe and/or semicolon)
	 * @return			True if checksum holds for each CAS RN in input; false otherwise
	 */
	public static boolean isValidCAS(String casInput) {
		long t1=System.currentTimeMillis();
		if(casInput.toUpperCase().contains("CHEMBL")) return false;
		if(casInput.toUpperCase().contains("SRC")) return false;
			
		String regex = "[0-9\\-]+"; //only has numbers and dashes
		Pattern p = Pattern.compile(regex); 
        Matcher m = p.matcher(casInput);
        
        if(!m.matches()) return false;
        
        if(casInput.substring(0,1).equals("-")) return false;

		
		String[] casArray = casInput.split("\\||;|,");
		boolean valid = true;
		for (String cas:casArray) {
			String casTemp = cas.replaceAll("[^0-9]","");//do we really want to discard non numbers???
			int len = casTemp.length();
			if (len > 10 || len <= 0) { return false; }
			int check = Character.getNumericValue(casTemp.charAt(len-1));
			int sum = 0;
			for (int i = 1; i <= len-1; i++) {
				sum += i*Character.getNumericValue(casTemp.charAt(len-1-i));
			}
			if (sum % 10 != check) {
				valid = false;
				break;
			}
			// There are no valid CAS RNs with bad formatting in the current data set, but if that happens in other sources, could add format correction here
//			else if (!cas.contains("-")) {
//				System.out.println("Valid CAS with bad format: "+cas);
//			}
		}
		long t2=System.currentTimeMillis();
//		System.out.println((t2-t1)+" millisecs to check cas");
		
		return valid;
	}

	public static boolean getTextSolubility(ExperimentalRecord er, String propertyValue) {
		String propertyValue1 = propertyValue.toLowerCase();
		Matcher matcher = Pattern.compile("([0-9.]+|one)[ ]?gr?a?m? .*(soluble |dissolves? )?in:?.*?((less|greater|more) than|<|>|~|about)? ?([0-9.,]+) (ml|cc) (of )?(cold|hot|warm|boiling)? ?(water|h2o)").matcher(propertyValue1);
		if (!matcher.find()) { return false; }
		String num = matcher.group(1);
		String qual = matcher.group(3);
		String denom = matcher.group(5);
		if (num==null || num.isBlank() || denom==null || denom.isBlank()) { return false; }
		denom = denom.replaceAll(",", "");
		if (num.equals("one")) { 
			er.property_value_point_estimate_original = 1.0/Double.parseDouble(denom);
		} else {
			er.property_value_point_estimate_original = Double.parseDouble(num)/Double.parseDouble(denom);
		}
		er.property_value_units_original = ExperimentalConstants.str_g_mL;
		if (qual==null || qual.isBlank()) {
			return true;
		} else if (qual.contains("greater") || qual.contains(">") || qual.contains("more")) {
			er.property_value_numeric_qualifier = "<";
		} else if (qual.contains("about") || qual.contains("~")) {
			er.property_value_numeric_qualifier = "~";
		} else if (qual.contains("less") || qual.contains("<")) {
			er.property_value_numeric_qualifier = ">";
		}
		return true;
	}

	public static boolean getSimpleNumericSolubility(ExperimentalRecord er, String propertyValue) {
		String propertyValue1 = propertyValue.toLowerCase();
		Matcher matcher = Pattern.compile("([~=><]{1,2}|about |approx[\\.]?(imately)? )?([0-9.]+) ?g ?/ ?([0-9.]+) ?(ml|cc)( ?[(] ?[0-9]+ ?\\u00B0C ?[)])?( in| of)? (hot |cold |boiling |warm )?water(( at| @) ([0-9.]+) \u00B0C)?")
				.matcher(propertyValue1);
		if (!matcher.find()) { return false; }
		String qual = matcher.group(1);
		String num = matcher.group(3);
		String denom = matcher.group(4);
		if (num==null || num.isBlank() || denom==null || denom.isBlank()) { return false; }
		denom = denom.replaceAll(",", "");
		er.property_value_point_estimate_original = Double.parseDouble(num)/Double.parseDouble(denom);
		er.property_value_units_original = ExperimentalConstants.str_g_mL;
		if (qual!=null) { 
			if (qual.contains("about") || qual.contains("approx")) { 
				er.property_value_numeric_qualifier = "~";
			} else {
				er.property_value_numeric_qualifier = qual;
			}
		}
		return true;
	}

}
