package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

/**
 * Parses data from qsardb.org
 * @author GSINCL01
 *
 */
public class ParseQSARDB extends Parse {

	public ParseQSARDB() {
		sourceName = ExperimentalConstants.strSourceQSARDB;
		this.init();
	}
	
	@Override
	protected void createRecords() {
		Vector<RecordQSARDB> records = RecordQSARDB.parseQSARDBRecordsFromExcel();
		writeOriginalRecordsToFile(records);
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		try {
			File jsonFile = new File(jsonFolder + File.separator + fileNameJSON_Records);
			
			RecordQSARDB[] recordsQSARDB = gson.fromJson(new FileReader(jsonFile), RecordQSARDB[].class);
			
			for (int i = 0; i < recordsQSARDB.length; i++) {
				RecordQSARDB rec = recordsQSARDB[i];
				addExperimentalRecords(rec,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return recordsExperimental;
	}
	
	private void addExperimentalRecords(RecordQSARDB qr,ExperimentalRecords records) {
		if (qr.logS!=null && !qr.logS.isBlank()) {
			ExperimentalRecord er = new ExperimentalRecord();
			er.date_accessed = RecordQSARDB.lastUpdated;
			er.source_name = ExperimentalConstants.strSourceQSARDB;
			er.original_source_name = qr.reference;
			er.url = qr.url;
			er.chemical_name = qr.name;
			er.casrn = qr.casrn;
			er.property_name = ExperimentalConstants.strWaterSolubility;
			er.property_value_string = "LogS: "+qr.logS;
			ParseUtilities.getNumericalValue(er,qr.logS,qr.logS.length(),false);
			er.property_value_point_estimate_original = er.property_value_point_estimate_original;
			if (qr.units.contains("mgL")) { er.property_value_units_original = ExperimentalConstants.str_log_mg_L;
			} else if (qr.units.contains("M")) { er.property_value_units_original = ExperimentalConstants.str_log_M;
			}
			er.finalizePropertyValues();
			er.keep = true;
			er.flag = false;
			records.add(er);
		}
		if (qr.mp!=null && !qr.mp.isBlank()) {
			ExperimentalRecord er = new ExperimentalRecord();
			er.date_accessed = RecordQSARDB.lastUpdated;
			er.source_name = ExperimentalConstants.strSourceQSARDB;
			er.original_source_name = qr.reference;
			er.url = qr.url;
			er.chemical_name = qr.name;
			er.casrn = qr.casrn;
			er.property_name = ExperimentalConstants.strMeltingPoint;
			er.property_value_string = "MP (C): "+qr.mp;
			ParseUtilities.getNumericalValue(er,qr.mp,qr.mp.length(),false);
			er.property_value_units_original = ExperimentalConstants.str_C;
			er.finalizePropertyValues();
			er.keep = true;
			er.flag = false;
			records.add(er);
		}
		if (qr.mLogP!=null && !qr.mLogP.isBlank()) {
			ExperimentalRecord er = new ExperimentalRecord();
			er.date_accessed = RecordQSARDB.lastUpdated;
			er.source_name = ExperimentalConstants.strSourceQSARDB;
			er.original_source_name = qr.reference;
			er.url = qr.url;
			er.chemical_name = qr.name;
			er.casrn = qr.casrn;
			er.property_name = ExperimentalConstants.strLogKow;
			er.property_value_string = "mLogP: "+qr.mLogP;
			ParseUtilities.getNumericalValue(er,qr.mLogP,qr.mLogP.length(),false);
			er.finalizePropertyValues();
			er.keep = true;
			er.flag = false;
			records.add(er);
		}
		if (qr.vp!=null && !qr.vp.isBlank()) {
			ExperimentalRecord er = new ExperimentalRecord();
			er.date_accessed = RecordQSARDB.lastUpdated;
			er.source_name = ExperimentalConstants.strSourceQSARDB;
			er.original_source_name = qr.reference;
			er.url = qr.url;
			er.chemical_name = qr.name;
			er.casrn = qr.casrn;
			er.property_name = ExperimentalConstants.strVaporPressure;
			er.property_value_string = "LogVP: "+qr.vp;
			ParseUtilities.getNumericalValue(er,qr.vp,qr.vp.length(),false);
			er.property_value_point_estimate_original = er.property_value_point_estimate_original;
			er.property_value_units_original = ExperimentalConstants.str_log_mmHg;
			er.finalizePropertyValues();
			er.keep = true;
			er.flag = false;
			records.add(er);
		}
	}
	
	public static void main(String[] args) {
		ParseQSARDB p = new ParseQSARDB();
		p.createFiles();
	}
}
