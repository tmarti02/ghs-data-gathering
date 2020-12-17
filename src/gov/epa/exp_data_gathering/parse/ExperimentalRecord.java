package gov.epa.exp_data_gathering.parse;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Objects;

import gov.epa.api.ExperimentalConstants;
import gov.epa.ghs_data_gathering.Database.CreateGHS_Database;

public class ExperimentalRecord {

	int physchem_id;//	Autonumbered record number for physchem data (generated by database later)
	int record_source_id;//	Record number for reference that the physchem data came from (generated by database later- may only need for records from journal articles)

	public String casrn;//Chemical abstracts service number (only if provided by the reference)
	public String einecs;
	public String chemical_name;//	Most systematic name (only if provided in the reference)
	public String synonyms;//	Pipe deliminated synonyms (only if provided in the reference)
	public String smiles;//Simplified Molecular Input Line Entry System for molecular structure (only if provided in the reference)
	public String property_name;//	Name of the property (use  "options_property_names" lookup table to consistently populate the field)
	public String property_value_numeric_qualifier;// >, <, or ~
	public Double property_value_min_final;//The minimum value of a property when a range of values is given
	public Double property_value_max_final;//The maximum value of a property when a range of values is given
	public Double property_value_point_estimate_final;// Point estimate of the property (when a single value is given)
	public String property_value_units_final;//The units for the property value (convert to defined values in ExperimentalConstants class)
	public String property_value_qualitative;// Valid qualitative data: solubility descriptor, appearance
	public Double temperature_C;//The temperature in C that the property is measured at (vapor pressure might be given at 23 C for example)
	public String pressure_mmHg;//The pressure in kPa that the property is measured at (important for boiling points for example)
	public String pH;
	public String measurement_method;//	The experimental method used to measure the property
	public String reliability;
	public String note;//	Any additional note

	public String property_value_string;//Store original string from source for checking later
	public Double property_value_min_original;//The minimum value of a property when a range of values is given
	public Double property_value_max_original;//The maximum value of a property when a range of values is given
	public Double property_value_point_estimate_original;// Point estimate of the property (when a single value is given)
	public String property_value_units_original;//The units for the property value (convert to defined values in ExperimentalConstants class)
	
	public String url;
	public String source_name;//use Experimental constants
	public String original_source_name;//If specific reference/paper provided
								//"original_source_name" rather than "source_name_original" to avoid syntactic confusion with "*_original" vs "*_final" fields above
	public String date_accessed;//use Experimental constants
	
	public boolean keep;//Does the record contain useful data?
	public boolean flag;
	public String reason;//If keep=false or flag=true, why?
	
	//TODO do we need parent url too? sometimes there are several urls we have to follow along the way to get to the final url

	public final static String [] outputFieldNames= {"keep",
			"reason",
			"casrn",
			"einecs",
			"chemical_name",
			"synonyms",
			"smiles",
			"source_name",
			"property_name",
			"property_value_string",
			"property_value_numeric_qualifier",
			"property_value_point_estimate_final",
			"property_value_min_final",
			"property_value_max_final",
			"property_value_units_final",
			"pressure_mmHg",
			"temperature_C",
			"pH",
			"property_value_qualitative",
			"measurement_method",
			"note",
			"flag",
			"original_source_name",
			"url",
			"date_accessed"};

	public void finalizeUnits() {
		if (property_name.equals(ExperimentalConstants.str_pKA) || property_name.equals(ExperimentalConstants.strLogKow)) {
			if (property_value_point_estimate_original!=null) { property_value_point_estimate_final = property_value_point_estimate_original; }
			if (property_value_min_original!=null) { property_value_min_final = property_value_min_original; }
			if (property_value_max_original!=null) { property_value_max_final = property_value_max_original; }
			property_value_units_final = property_value_units_original;
		} else if ((property_name.equals(ExperimentalConstants.strMeltingPoint) || property_name.equals(ExperimentalConstants.strBoilingPoint) ||
				property_name.equals(ExperimentalConstants.strFlashPoint)) && property_value_units_original!=null) {
			UnitConverter.convertTemperature(this);
		} else if (property_name.equals(ExperimentalConstants.strDensity)) {
			UnitConverter.convertDensity(this);
		} else if (property_name.equals(ExperimentalConstants.strVaporPressure) && property_value_units_original!=null) {
			UnitConverter.convertPressure(this);
		} else if (property_name.equals(ExperimentalConstants.strHenrysLawConstant) && property_value_units_original!=null) {
			double conversionFactor = 1.0;
			if (property_value_units_original.equals(ExperimentalConstants.str_atm_m3_mol)) {
				conversionFactor = UnitConverter.atm_to_Pa;
			}
			if (property_value_point_estimate_original!=null) { property_value_point_estimate_final = property_value_point_estimate_original*conversionFactor; }
			if (property_value_min_original!=null) { property_value_min_final = property_value_min_original*conversionFactor; }
			if (property_value_max_original!=null) { property_value_max_final = property_value_max_original*conversionFactor; }
			property_value_units_final = ExperimentalConstants.str_Pa_m3_mol;
		} else if (property_name.equals(ExperimentalConstants.strWaterSolubility) && property_value_units_original!=null) {
			UnitConverter.convertSolubility(this);
		}
	}
	
	public String toString(String del) {
		// TODO Auto-generated method stub
		return toString(del,outputFieldNames);
	}


	//convert to string by reflection:
	public String toString(String del,String [] fieldNames) {

		String Line = "";
		
		for (int i = 0; i < fieldNames.length; i++) {
			try {


				Field myField = this.getClass().getDeclaredField(fieldNames[i]);

				String val=null;

				//						System.out.println(myField.getType().getName());

				if (myField.getType().getName().contains("Double")) {
					if (myField.get(this)==null) {
						val="";	
					} else {
						val=myField.get(this)+"";
					}

				} else if (myField.getType().getName().contains("Integer")) {
					if (myField.get(this)==null) {
						val="";	
					} else {
						val=myField.get(this)+"";
					}

				} else {//string
					if (myField.get(this)==null) {
						//								val="\"\"";
						val="";
					} else {
						//								val="\""+(String)myField.get(this)+"\"";
						val=(String)myField.get(this);
					} 
				}

				val=val.replace("\r\n","<br>");
				val=val.replace("\n","<br>");

				if (val.contains(del)) {
					System.out.println("***WARNING***"+this.casrn+"\t"+fieldNames[i]+"\t"+val+"\thas delimiter");
				}

				Line += val;
				if (i < fieldNames.length - 1) {
					Line += del;
				}


			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return Line;
	}
	
	/**
	 * Adds a string to the note field of an ExperimentalRecord object
	 * @param er	The ExperimentalRecord object to be updated
	 * @param str	The string to be added
	 * @return		The updated ExperimentalRecord object
	 */
	public void updateNote(String str) {
		note = Objects.isNull(note) ? str : note+"; "+str;
	}

	public String[] getValuesForDatabase() {
		String name = chemical_name==null ? "" : chemical_name.replaceAll("(?<!\\\\)'", "\'");
		String pointEstimate = property_value_point_estimate_final==null ? "" : Double.toString(property_value_point_estimate_final);
		String min = property_value_min_final==null ? "" : Parse.formatDouble(property_value_min_final);
		String max = property_value_max_final==null ? "" : Parse.formatDouble(property_value_max_final);
		String temp = temperature_C==null ? "" : Parse.formatDouble(temperature_C);
		String [] values= {Boolean.toString(keep),
				reason,
				casrn,
				einecs,
				name,
				synonyms,
				smiles,
				source_name,
				property_name,
				property_value_string,
				property_value_numeric_qualifier,
				pointEstimate,
				min,
				max,
				property_value_units_final,
				pressure_mmHg,
				temp,
				pH,
				property_value_qualitative,
				measurement_method,
				note,
				Boolean.toString(flag),
				original_source_name,
				url,
				date_accessed};
		return values;
	}
}
