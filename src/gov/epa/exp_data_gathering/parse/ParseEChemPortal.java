package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

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
		if (ecpr.numberType!= null && !ecpr.numberType.equals("CAS Number") && !ecpr.number.equals("unknown") && !ecpr.values.isEmpty()) {
			String cas = ecpr.number;
			for (int i = 0; i < ecpr.values.size(); i++) {
				ExperimentalRecord er = new ExperimentalRecord();
				er.casrn = cas;
				er.chemical_name = ecpr.substanceName;
				er.url = ecpr.url;
				er.property_value_string = ecpr.values.get(i);
				String propertyValue = ecpr.values.get(i).replaceAll(" Â"," ").replaceAll("â€”","-");
				if (!ecpr.temperature.isEmpty() && ecpr.temperature.get(i)!=null) { getTemperatureCondition(er,ecpr.temperature.get(i)); }
				if (!ecpr.pressure.isEmpty() && ecpr.pressure.get(i)!=null) { getPressureCondition(er,ecpr.pressure.get(i)); }
				if (!ecpr.pH.isEmpty() && ecpr.pH.get(i)!=null) { er.updateNote("pH: "+ecpr.pH.get(i)); }
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
					getWaterSolubility(er,ecpr.values.get(i));
				} else if (ecpr.section.equals("Vapour pressure")) {
					er.property_name = ExperimentalConstants.strVaporPressure;
					getVaporPressure(er,propertyValue);
				} else if (ecpr.section.equals("Partition coefficient")) {
					er.property_name = ExperimentalConstants.strLogKow;
					getLogProperty(er,propertyValue);
				} else if (ecpr.section.equals("Dissociation constant")) {
					er.property_name = ExperimentalConstants.str_pKA;
					getLogProperty(er,propertyValue);
				}
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
