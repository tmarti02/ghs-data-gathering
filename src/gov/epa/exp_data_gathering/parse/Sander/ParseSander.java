package gov.epa.exp_data_gathering.parse.Sander;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.api.Chemical;
import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.Parse;
import kong.unirest.json.JSONObject;

public class ParseSander extends Parse {
	
	public ParseSander() {
		sourceName = RecordSander.sourceName;
		this.init();
	}
	@Override
	protected void createRecords() {

		if(generateOriginalJSONRecords) {
			Vector<RecordSander> records = RecordSander.parseWebpagesInDatabase();
			writeOriginalRecordsToFile(records);
		}
	}
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordSander> recordsSander = new ArrayList<RecordSander>();
			RecordSander[] tempRecords = null;
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordSander[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsSander.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordSander[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsSander.add(tempRecords[i]);
					}
				}
			}
			
			System.out.println(recordsSander.size());
			
			
			Iterator<RecordSander> it = recordsSander.iterator();
			while (it.hasNext()) {
				RecordSander r = it.next();
				
				addExperimentalRecords(r,recordsExperimental);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	
	/**
	 * populates experimentalrecord fields with data from the recordSander object.
	 * @param rs
	 * @param records
	 */
	private void addExperimentalRecords(RecordSander rs, ExperimentalRecords records) {

		ExperimentalRecord er = new ExperimentalRecord();
		er.date_accessed = rs.date_accessed;
		er.keep = true;
		er.url = rs.url;

		String CAS = rs.CASRN;
		if (CAS != null && !CAS.contains("???")) {
			er.casrn = CAS;
		}
		//Note all the ones with inchiKeys are QSPR predictions so dont get kept !
		

		// er.reference = Gabrieldemo(rs);

		er.property_value_string = rs.hcp + " " + ExperimentalConstants.str_mol_m3_atm;
		er.chemical_name = rs.chemicalName.replace("? ? ? ", "");
		er.property_name = ExperimentalConstants.strHenrysLawConstant;
		String propertyValue = rs.hcp;

		er.property_value_units_original = ExperimentalConstants.str_mol_m3_atm;
		er.property_value_units_final = ExperimentalConstants.str_atm_m3_mol;

		getnumericalhcp(er, propertyValue);

		// below converts Sander's weird inverted units to atm*m3/mol
		if (!(er.property_value_point_estimate_original == null)) {
			er.property_value_point_estimate_final = 1 / (er.property_value_point_estimate_original * 101325);
		}

		er.temperature_C = (double) 25;//we are assuming that Sander converted all to 25 using the correction term
		
		/*
		 * CR: (Wednesday August 18) temperature and pressure information, if it is
		 * given at all is found in the notes. er.pressure_mmHg = "760"; TMM: WTF?
		 */

		er.source_name = RecordSander.sourceName;
		// er.original_source_name = rs.referenceAbbreviated.get(i);

		LiteratureSource ls = new LiteratureSource();
		er.literatureSource = ls;
		ls.name = rs.referenceAbbreviated;
		ls.citation = rs.referenceFull;

		// TODO store full citation from reference (it's in the HTML)

		assignKeepAndNoteUsingType(er, rs.type);

		er.updateNote(rs.notes);
		
		if(er.property_value_point_estimate_final==null &&  er.property_value_min_final==null && er.property_value_max_final==null) {
			er.keep=false;
			er.reason="No property value";
		}

		
		
		records.add(er);

	}

	public static void main(String[] args) {
		ParseSander p = new ParseSander();

		p.generateOriginalJSONRecords=true;
		p.removeDuplicates=false;//dont know which one is right
		p.createFiles();

		
	}


	/**
	 * converts strings of the form 5.8×10-4 to the correct value as a double.
	 * @param er
	 * @param propertyValue
	 */
	public static void getnumericalhcp(ExperimentalRecord er, String propertyValue) {
		Matcher sanderhcpMatcher = Pattern.compile("([0-9]*\\.?[0-9]+)(\\×10(\\-)?([0-9]+))?").matcher(propertyValue);
		if (sanderhcpMatcher.find()) {
			String strMantissa = sanderhcpMatcher.group(1);
			String strNegMagnitude = sanderhcpMatcher.group(3);
			String strMagnitude = sanderhcpMatcher.group(4);
			if (!(strMagnitude == null)){
				if (!(strNegMagnitude == null)) { // ? corresponds to negative magnitude (e.g. 3.4 * 10^-4), otherwise positive
					Double mantissa = Double.parseDouble(strMantissa.replaceAll("\\s",""));
					Double magnitude =  Double.parseDouble(strMagnitude.replaceAll("\\s","").replaceAll("\\+", ""));
					er.property_value_point_estimate_original = mantissa*Math.pow(10, (-1)*magnitude);
				} else {
					Double mantissa = Double.parseDouble(strMantissa.replaceAll("\\s",""));
					Double magnitude =  Double.parseDouble(strMagnitude.replaceAll("\\s","").replaceAll("\\+", ""));
					er.property_value_point_estimate_original = mantissa*Math.pow(10, magnitude);
				}
			}
			else {
				er.property_value_point_estimate_original = Double.parseDouble(strMantissa.replaceAll("\\s",""));
			}
		}
	}
	
	

	/**
	 * Keeps the Henry's law constants that were derived by measurement, VP/AS, literature, or citation.
	 * @param er
	 * @param type
	 */
	public static void assignKeepAndNoteUsingType(ExperimentalRecord er, String type) {
		/**
		 Table entries are sorted according to reliability of the data, listing the
		 most reliable type first: L) literature review, M) measured, V) VP/AS = vapor
		 pressure/aqueous solubility, R) recalculation, T) thermodynamical
		 calculation, X) original paper not available, C) citation, Q) QSPR, E)
		 estimate, ?) unknown, W) wrong. See Section 3.1 of Sander (2015) for further
		 details.
		  
		 TMM 2020-12-23: Keeping L, M, V (most reliable)
		
		https://acp.copernicus.org/articles/23/10901/2023/acp-23-10901-2023.pdf
		
		“L” The cited paper is a literature review.
		“M” The cited paper presents the original measurements.
		“V” The vapor pressure of the pure substance was divided by its aqueous solubility (sometimes called the
		“VP/AS” method).
		“R” The cited paper presents a recalculation of previously
		published material (e.g. extrapolation to a different temperature or concentration range).
		“T” The value was obtained from a thermodynamical calculation (1solG=−RT lnH; see Sander, 1999 for details).
		“X” The original paper was not available for this study. The
		data listed here were found in a secondary source.
		“C” The paper is a citation of a reference which I could not
		obtain (personal communication, PhD theses, grey literature).
		“Q” The value was calculated using a “quantitative
		structure–property relationship” (QSPR) or a similar
		theoretical method.
		“E” The value is an estimate. Estimates are listed only if no
		reliable data are available.
		“?” The cited paper does not clearly state how the value was
		obtained.
		“W” The value is probably wrong. It is not listed in the table, in order to avoid spreading of erroneous data. More
		information can be found in the notes.
		 */
		
		if (type.contains("L")) {
			er.keep=true;
			er.note = "Source: literature review";
		} else if (type.contains("M")) {
			er.note = "Source: measured";
			er.keep = true;
		} else if (type.contains("V")) {
			er.note = "Source: VP/AS = vapor pressure/aqueous solubility";
			er.keep = true;
		} else if (type.contains("R")) {
			er.note = "Source: recalculation";
			er.keep = false;
			er.reason = "Source: recalculation";
		} else if (type.contains("T")) {
			er.note = "Source: thermodynamical calculation";
			er.keep = false;
			er.reason= "thermodynamical calculation";
		} else if (type.contains("X")) {
			er.reason = "original paper not available";
			er.keep = false;
		} else if (type.contains("C")) {
			er.note = "Source: unavailable citation";
			er.reason = "citation not available";
			er.keep = false;		
		} else if (type.contains("Q")) {
			er.note="Source: QSPR";
			er.reason = "QSPR";
			er.keep = false;
		} else if (type.contains("E")) {
			er.note="Source: estimate";
			er.reason = "estimate";
			er.keep=false;
		} else if (type.contains("?")) {
			er.note = "Source: unknown";
			er.reason = "unknown";
			er.keep=false;
		} else if (type.contains("W")) {
			er.note = "Value is wrong";
			er.reason = "wrong";
			er.keep=false;
		} else {
			System.out.println("Unhandled method: "+type);
			er.note = "Unhandled Type "+type;
			er.keep = false;
		}
//		er.note="Method = "+er.note;
	}

}
