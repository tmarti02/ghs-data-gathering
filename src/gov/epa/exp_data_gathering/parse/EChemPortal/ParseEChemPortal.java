package gov.epa.exp_data_gathering.parse.EChemPortal;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.PressureCondition;
import gov.epa.exp_data_gathering.parse.TemperatureCondition;
import gov.epa.exp_data_gathering.parse.TextUtilities;

/**
 * Parses data from echemportal.org
 * @author GSINCL01
 *
 */
public class ParseEChemPortal extends Parse {

	public ParseEChemPortal() {
		sourceName = ExperimentalConstants.strSourceEChemPortal;
		this.init();
	}
	
	@Override
	protected void createRecords() {
		Vector<RecordEChemPortal> records = RecordEChemPortal.parseEChemPortalQueriesFromExcel();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordEChemPortal> recordsEChemPortal = new ArrayList<RecordEChemPortal>();
			RecordEChemPortal[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordEChemPortal[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsEChemPortal.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordEChemPortal[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsEChemPortal.add(tempRecords[i]);
					}
				}
			}
			
			Iterator<RecordEChemPortal> it = recordsEChemPortal.iterator();
			while (it.hasNext()) {
				RecordEChemPortal r = it.next();
				addExperimentalRecords(r,recordsExperimental);
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
				er.date_accessed = RecordEChemPortal.lastUpdated;
				er.source_name = ExperimentalConstants.strSourceEChemPortal;
				er.original_source_name = ecpr.participant;
				if (cas.length()!=0 && !cas.equals("unknown")) { er.casrn = cas;
				} else if (einecs.length()!=0 && !einecs.equals("unknown")) { er.einecs = einecs; }
				if (ecpr.substanceName!=null && !ecpr.substanceName.equals("-") && !ecpr.substanceName.contains("unnamed")) {
					er.chemical_name = StringEscapeUtils.escapeHtml4(ecpr.substanceName);
				}
				er.url = ecpr.url;
				if (ecpr.method!=null && !ecpr.method.isBlank()) {
					er.measurement_method = ecpr.method;
				}
				er.property_value_string = ecpr.values.get(i).replaceAll("—", "-");
				String propertyValue = er.property_value_string;
				if (!ecpr.temperature.isEmpty() && ecpr.temperature.get(i)!=null) { 
					String temp = ecpr.temperature.get(i).replaceAll("—", "-");
					TemperatureCondition.getTemperatureCondition(er,temp);
					er.property_value_string = er.property_value_string + ";" + temp;
				}
				if (!ecpr.pressure.isEmpty() && ecpr.pressure.get(i)!=null) {
					String pressure = ecpr.pressure.get(i).replaceAll("—", "-");
					PressureCondition.getPressureCondition(er,pressure,sourceName);
					er.property_value_string = er.property_value_string + ";" + pressure;
				}
				if (!ecpr.pH.isEmpty() && ecpr.pH.get(i)!=null) { 
					String pHStr = ecpr.pH.get(i).replaceAll("—", "-");
					er.property_value_string = er.property_value_string + ";" + pHStr;
					boolean foundpH = false;
					try {
						double[] range = TextUtilities.extractFirstDoubleRangeFromString(pHStr,pHStr.length());
						er.pH = range[0]+"-"+range[1];
						foundpH = true;
					} catch (Exception ex) { }
					if (!foundpH) {
						try {
							double[] range = TextUtilities.extractAltFormatRangeFromString(pHStr,pHStr.length());
							er.pH = range[0]+"-"+range[1];
							foundpH = true;
						} catch (Exception ex) { }
					}
					if (!foundpH) {
						try {
							Matcher caMatcher = Pattern.compile(".*?(ca. )?([-]?[ ]?[0-9]*\\.?[0-9]+)( ca. )([-]?[ ]?[0-9]*\\.?[0-9]+)").matcher(pHStr);
							if (caMatcher.find()) {
								String numQual = caMatcher.group(1).isBlank() ? "" : "~";
								er.pH = numQual+Double.parseDouble(caMatcher.group(2))+"~"+Double.parseDouble(caMatcher.group(4));
								foundpH = true;
							}
						} catch (Exception ex) { }
					}
					if (!foundpH && pHStr.contains(",") && !pHStr.endsWith(",")) {
						er.pH = pHStr;
						foundpH = true;
					}
					if (!foundpH) {
						try {
							double pHDouble = TextUtilities.extractClosestDoubleFromString(pHStr,pHStr.length(),"pH");
							String pHDoubleStr = Double.toString(pHDouble);
							String numQual = "";
							if (pHDouble >= 0 && pHDouble < 1) {
								numQual = TextUtilities.getNumericQualifier(pHStr,pHStr.indexOf("0"));
							} else {
								numQual = TextUtilities.getNumericQualifier(pHStr,pHStr.indexOf(pHDoubleStr.charAt(0)));
							}
							er.pH = numQual+pHDoubleStr;
							foundpH = true;
						} catch (Exception ex) { }
					}
				}
				if (ecpr.section.equals("Density")) {
					er.property_name = ExperimentalConstants.strDensity;
					ParseUtilities.getDensity(er,propertyValue);
				} else if (ecpr.section.equals("Melting / freezing point")) {
					er.property_name = ExperimentalConstants.strMeltingPoint;
					ParseUtilities.getTemperatureProperty(er,propertyValue);
				} else if (ecpr.section.equals("Boiling point")) {
					er.property_name = ExperimentalConstants.strBoilingPoint;
					ParseUtilities.getTemperatureProperty(er,propertyValue);
				} else if (ecpr.section.equals("Flash point")) {
					er.property_name = ExperimentalConstants.strFlashPoint;
					ParseUtilities.getTemperatureProperty(er,propertyValue);
				} else if (ecpr.section.equals("Water solubility")) {
					er.property_name = ExperimentalConstants.strWaterSolubility;
					ParseUtilities.getWaterSolubility(er,propertyValue,sourceName);
				} else if (ecpr.section.equals("Vapour pressure")) {
					er.property_name = ExperimentalConstants.strVaporPressure;
					ParseUtilities.getVaporPressure(er,propertyValue);
				} else if (ecpr.section.equals("Partition coefficient")) {
					er.property_name = ExperimentalConstants.strLogKOW;
					ParseUtilities.getLogProperty(er,propertyValue);
				} else if (ecpr.section.equals("Dissociation constant")) {
					er.property_name = ExperimentalConstants.str_pKA;
					ParseUtilities.getLogProperty(er,propertyValue);
				} else if (ecpr.section.equals("Henry's Law constant")) {
					er.property_name = ExperimentalConstants.strHenrysLawConstant;
					ParseUtilities.getHenrysLawConstant(er,propertyValue);
				}

				uc.convertRecord(er);
				
				if (!ParseUtilities.hasIdentifiers(er)) {
					er.keep = false;
					er.reason = "No identifiers";
				}
				
				er.reliability = ecpr.reliability;
				records.add(er);
			}
		}
	}
	
	public static void main(String[] args) {
		ParseEChemPortal p = new ParseEChemPortal();
		p.createFiles();
	}
}
