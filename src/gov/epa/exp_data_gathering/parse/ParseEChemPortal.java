package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.api.ExperimentalConstants;

public class ParseEChemPortal extends Parse {

	public ParseEChemPortal() {
		sourceName = ExperimentalConstants.strSourceEChem;
		this.init();
	}
	
	@Override
	protected void createRecords() {
		Vector<RecordEChemPortal> records = RecordEChemPortal.parseEChemPortalQueryFromExcel();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordEChemPortal[] recordsEChemPortal = gson.fromJson(new FileReader(jsonFile), RecordEChemPortal[].class);
			
			for (int i = 0; i < recordsEChemPortal.length; i++) {
				RecordEChemPortal rec = recordsEChemPortal[i];
				addExperimentalRecords(rec,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return recordsExperimental;
	}
	
	private void addExperimentalRecords(RecordEChemPortal ecpr,ExperimentalRecords records) {
		if (!ecpr.values.isEmpty()) {
			String cas = "";
			String einecs = "";
			if (ecpr.numberType.equals("CAS Number")) { cas = ecpr.number;
			} else if (ecpr.numberType.equals("EC Number")) { einecs = ecpr.number; }
			for (int i = 0; i < ecpr.values.size(); i++) {
				ExperimentalRecord er = new ExperimentalRecord();
				if (cas.length()!=0 && !cas.equals("unknown")) { er.casrn = cas;
				} else if (einecs.length()!=0 && !einecs.equals("unknown")) { er.einecs = einecs; }
				er.chemical_name = ecpr.substanceName.equals("-") ? "" : ecpr.substanceName;
				er.url = ecpr.url;
				er.property_value_string = ecpr.values.get(i);
				String propertyValue = ecpr.values.get(i);
				if (!ecpr.temperature.isEmpty() && ecpr.temperature.get(i)!=null) { getTemperatureCondition(er,ecpr.temperature.get(i)); }
				if (!ecpr.pressure.isEmpty() && ecpr.pressure.get(i)!=null) { getPressureCondition(er,ecpr.pressure.get(i)); }
				if (!ecpr.pH.isEmpty() && ecpr.pH.get(i)!=null) { 
					String pHStr = ecpr.pH.get(i);
					boolean foundpH = false;
					try {
						double[] range = Parse.extractFirstDoubleRangeFromString(pHStr,pHStr.length());
						er.pH = range[0]+"-"+range[1];
						foundpH = true;
					} catch (Exception ex) { }
					if (!foundpH) {
						try {
							double[] range = Parse.extractAltFormatRangeFromString(pHStr,pHStr.length());
							er.pH = range[0]+"-"+range[1];
							foundpH = true;
						} catch (Exception ex) { }
					}
					if (!foundpH) {
						try {
							Matcher caMatcher = Pattern.compile(".*?(ca. )?([-]?[ ]?[0-9]*\\.?[0-9]+)( ca. )([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(pHStr);
							if (caMatcher.find()) {
								String numQual = caMatcher.group(1).isBlank() ? "" : "~";
								er.pH = numQual+caMatcher.group(2)+"~"+caMatcher.group(4);
								foundpH = true;
							}
						} catch (Exception ex) { }
					}
					if (!foundpH) {
						try {
							double pHDouble = Parse.extractDoubleFromString(pHStr,pHStr.length());
							String pHDoubleStr = Double.toString(pHDouble);
							String numQual = getNumericQualifier(pHStr,pHStr.indexOf(pHDoubleStr.charAt(0)));
							er.pH = numQual+pHDoubleStr;
							foundpH = true;
						} catch (Exception ex) { }
					}
					if (!foundpH) { er.pH = pHStr; }
				}
				if (ecpr.section.equals("Density")) {
					er.property_name = ExperimentalConstants.strDensity;
					getDensity(er,propertyValue);
				} else if (ecpr.section.equals("Melting / freezing point")) {
					er.property_name = ExperimentalConstants.strMeltingPoint;
					getTemperatureProperty(er,propertyValue);
				} else if (ecpr.section.equals("Boiling point")) {
					er.property_name = ExperimentalConstants.strBoilingPoint;
					getTemperatureProperty(er,propertyValue);
				} else if (ecpr.section.equals("Flash point")) {
					er.property_name = ExperimentalConstants.strFlashPoint;
					getTemperatureProperty(er,propertyValue);
				} else if (ecpr.section.equals("Water solubility")) {
					er.property_name = ExperimentalConstants.strWaterSolubility;
					getWaterSolubility(er,propertyValue);
				} else if (ecpr.section.equals("Vapour pressure")) {
					er.property_name = ExperimentalConstants.strVaporPressure;
					getVaporPressure(er,propertyValue);
				} else if (ecpr.section.equals("Partition coefficient")) {
					er.property_name = ExperimentalConstants.strLogKow;
					getLogProperty(er,propertyValue);
				} else if (ecpr.section.equals("Dissociation constant")) {
					er.property_name = ExperimentalConstants.str_pKA;
					getLogProperty(er,propertyValue);
				} else if (ecpr.section.equals("Henry's Law constant")) {
					er.property_name = ExperimentalConstants.strHenrysLawConstant;
					getHenrysLawConstant(er,propertyValue);
				}
				er.finalizeUnits();
				er.keep = true;
				records.add(er);
			}
		}
	}
	
	public static void main(String[] args) {
		ParseEChemPortal p = new ParseEChemPortal();
		p.createFiles();
	}
}
