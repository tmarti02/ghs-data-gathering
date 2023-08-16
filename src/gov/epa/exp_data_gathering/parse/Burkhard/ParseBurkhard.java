package gov.epa.exp_data_gathering.parse.Burkhard;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import com.google.gson.JsonObject;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.Burkhard.RecordBurkhard;
import kong.unirest.json.JSONObject;

public class ParseBurkhard extends Parse {

	public ParseBurkhard() {
		sourceName = ExperimentalConstants.strSourceBurkhard;
		this.init();

		// toxicity record status commented out for now
		/*
		 * fileNameJSON_Records = sourceName +" Toxicity Original Records.json";
		 * fileNameFlatExperimentalRecords = sourceName
		 * +" Toxicity Experimental Records.txt"; fileNameFlatExperimentalRecordsBad =
		 * sourceName +" Toxicity Experimental Records-Bad.txt";
		 * fileNameJsonExperimentalRecords = sourceName
		 * +" Toxicity Experimental Records.json"; fileNameJsonExperimentalRecordsBad =
		 * sourceName +" Toxicity Experimental Records-Bad.json";
		 * fileNameExcelExperimentalRecords = sourceName
		 * +" Toxicity Experimental Records.xlsx";
		 */

	}

	@Override
	protected void createRecords() {
		Vector<JsonObject> records = RecordBurkhard.parseBurkhardRecordsFromExcel();
		
		for (int i=0;i<records.size();i++) {
			JsonObject jo=records.get(i);
			if(jo.get("Chemical").getAsString().isEmpty()) records.remove(i--);
			
		}
		writeOriginalRecordsToFile(records);
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental = new ExperimentalRecords();
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			List<RecordBurkhard> recordsBurkhard = new ArrayList<RecordBurkhard>();
			RecordBurkhard[] tempRecords = null;
			if (howManyOriginalRecordsFiles == 1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordBurkhard[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsBurkhard.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0, jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordBurkhard[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsBurkhard.add(tempRecords[i]);
					}
				}
			}

			Iterator<RecordBurkhard> it = recordsBurkhard.iterator();
			
			HashSet<String>dtxsids=new HashSet<>();
			
			TreeSet<String> citations=new TreeSet<>();
			
			while (it.hasNext()) {
				RecordBurkhard r = it.next();
				addExperimentalRecords(r, recordsExperimental);							
				dtxsids.add(r.DTXSID);
				
			}
			
			for (ExperimentalRecord er:recordsExperimental) {
				citations.add(er.literatureSource.citation);
			}
			
			
			System.out.println(recordsExperimental.size());
			System.out.println(dtxsids.size());
			
			for (String citation:citations) {
				System.out.println(citation);
			}
			System.out.println(citations.size());

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}

	private void addExperimentalRecords(RecordBurkhard rb, ExperimentalRecords recordsExperimental) {
		if (rb.Log_BCF_Steady_State_mean != null && !rb.Log_BCF_Steady_State_mean.isBlank()) {
			addNewExperimentalRecord(rb, "LogBCFSteadyState", recordsExperimental);
		}
		if (rb.Log_BCF_Kinetic_mean != null && !rb.Log_BCF_Kinetic_mean.isBlank()) {
			addNewExperimentalRecord(rb, "LogBCFKinetic", recordsExperimental);
		}
		if (rb.Log_BAF_mean != null && !rb.Log_BAF_mean.isBlank()) {
//			System.out.println("Here BAF");
			addNewExperimentalRecord(rb, "LogBAF", recordsExperimental);
		}

	}
	
	boolean isNumeric(String value) {

		String v=value.toLowerCase().trim();

		if(v.equals("-") || v.equals("n.d.") || v.equals("na") || v.equals("--") || v.equals("n/a") || 
				v.equals("nd") || v.equals("?") || v.equals("n.c.") || 
				v.equals("<lod") || v.equals("nc") || v.equals("n.a.") || v.equals("n.a") || v.equals("na*")) {
			return false;
		} else {
			return true;
		}

	}
	

	private void addNewExperimentalRecord(RecordBurkhard rb, String propertyName,
			ExperimentalRecords recordsExperimental) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = new Date();
		String strDate = formatter.format(date);
		String dayOnly = strDate.substring(0, strDate.indexOf(" "));
		ExperimentalRecord er = new ExperimentalRecord();

		er.experimental_parameters=new Hashtable<>();

//		JSONObject jsonNote = new JSONObject();
//		jsonNote.put("species", rb.Common_Name);
//		jsonNote.put("tissue", rb.Tissue);
//		jsonNote.put("exposure concentrations", rb.Exposure_Concentrations);

		if(rb.Common_Name!=null)
			er.experimental_parameters.put("species",rb.Common_Name);
		
//		er.experimental_parameters.put("tissue",rb.Tissue);
		
		if (rb.Exposure_Concentrations!=null && !rb.Exposure_Concentrations.isBlank())
			er.experimental_parameters.put("exposure concentrations",rb.Exposure_Concentrations);
		

		er.source_name = sourceName;
		er.chemical_name = rb.Chemical;
		er.casrn = rb.CASRN;
		
		if(!er.casrn.contains("-")) er.casrn=null;
		
		er.dsstox_substance_id = rb.DTXSID;
		
//		er.reference = rb.Reference;
		
		er.literatureSource=new LiteratureSource();
		er.literatureSource.citation=rb.Reference;
		er.literatureSource.name=rb.Reference;
		
		

		if (propertyName.equals("LogBCFSteadyState")) {
			
			if (!isNumeric(rb.Log_BCF_Steady_State_mean)) return;
			
			er.property_name = ExperimentalConstants.strLogBCF_Fish_Whole_Body;
			er.property_value_units_final = "Log10("+rb.Log_BCF_Steady_State_units+")";
			er.property_value_units_final.replace("(L/kg-ww)", "(L/kg)");
			
//			er.experimental_parameters.put("study quality",rb.Study_Quality_BCF);
			er.reliability=rb.Study_Quality_BCF;

			er.experimental_parameters.put("method","Steady state");
			
			
//			jsonNote.put("study quality", rb.Study_Quality_BCF);
//			er.updateNote(jsonNote.toString());

			try {
				String propertyValue = rb.Log_BCF_Steady_State_mean;
				if (rb.Log_BCF_Steady_State_arithmetic_or_logarithmic!=null && rb.Log_BCF_Steady_State_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
					er.property_value_point_estimate_final = Math
							.log10(Double.parseDouble(rb.Log_BCF_Steady_State_mean));
					er.property_value_string = propertyValue + " (arithmetic)";

				} else {
					
					if (rb.Log_BCF_Steady_State_mean.contains("<") || rb.Log_BCF_Steady_State_mean.contains(">")) {
						er.property_value_numeric_qualifier=rb.Log_BCF_Steady_State_mean.substring(0,1);
						rb.Log_BCF_Steady_State_mean=rb.Log_BCF_Steady_State_mean.substring(1,rb.Log_BCF_Steady_State_mean.length()).trim();
					} 
					
					er.property_value_point_estimate_final = Double.parseDouble(rb.Log_BCF_Steady_State_mean);
					er.property_value_string = propertyValue + " (log)";

				}
				if ((rb.Log_BCF_Steady_State_max!=null) && (rb.Log_BCF_Steady_State_min!=null)) {
					String property_value = rb.Log_BCF_Steady_State_min + "~" + rb.Log_BCF_Steady_State_max;
					if (rb.Log_BCF_Steady_State_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
						er.property_value_max_final = Math.log10(Double.parseDouble(rb.Log_BCF_Steady_State_max));
						er.property_value_min_final = Math.log10(Double.parseDouble(rb.Log_BCF_Steady_State_min));
						er.property_value_string = property_value + " (arithmetic)";
					} else {
						er.property_value_max_final = Double.parseDouble(rb.Log_BCF_Steady_State_max);
						er.property_value_min_final = Double.parseDouble(rb.Log_BCF_Steady_State_min);
						er.property_value_string = property_value + " (log)";
					}
				}
			} catch (NumberFormatException e) {
				System.out.println("rb.Log_BCF_Steady_State_mean="+rb.Log_BCF_Steady_State_mean);
//				e.printStackTrace();
			}
		} else if (propertyName.equals("LogBCFKinetic")) {
			
			if (!isNumeric(rb.Log_BCF_Kinetic_mean)) return;
			
			er.property_name = ExperimentalConstants.strLogBCF_Fish_Whole_Body;
//			er.property_value_units_final = rb.Log_BCF_Kinetic_units;
			
			er.property_value_units_final = "Log10("+rb.Log_BCF_Kinetic_units+")";
			er.property_value_units_final.replace("(L/kg-ww)", "(L/kg)");

//			er.experimental_parameters.put("study quality",rb.Study_Quality_BCF);
			er.reliability=rb.Study_Quality_BCF;

			er.experimental_parameters.put("method","Kinetic");
			
			try {
				String property_value = rb.Log_BCF_Kinetic_mean;
				if (rb.Log_BCF_Kinetic_arithmetic_or_logarithmic!=null && rb.Log_BCF_Kinetic_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
					er.property_value_point_estimate_final = Math.log10(Double.parseDouble(rb.Log_BCF_Kinetic_mean));
					er.property_value_string = property_value + " (arithmetic)";
				} else {
					
					if (rb.Log_BCF_Kinetic_mean.contains("<") || rb.Log_BCF_Kinetic_mean.contains(">")) {
						er.property_value_numeric_qualifier=rb.Log_BCF_Kinetic_mean.substring(0,1);
						rb.Log_BCF_Kinetic_mean=rb.Log_BCF_Kinetic_mean.substring(1,rb.Log_BCF_Kinetic_mean.length()).trim();
					} 
					
					er.property_value_point_estimate_final = Double.parseDouble(rb.Log_BCF_Kinetic_mean);
					er.property_value_string = property_value + " (log)";
				}
				if ((rb.Log_BCF_Kinetic_min!=null) && (rb.Log_BCF_Kinetic_max!=null)) {
					String propertyValue = rb.Log_BCF_Kinetic_min + "~" + rb.Log_BCF_Kinetic_max;
					if (rb.Log_BCF_Kinetic_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
						er.property_value_max_final = Math.log10(Double.parseDouble(rb.Log_BCF_Kinetic_min));
						er.property_value_min_final = Math.log10(Double.parseDouble(rb.Log_BCF_Kinetic_max));
						er.property_value_string = propertyValue + " (arithmetic)";
					} else {
						er.property_value_max_final = Double.parseDouble(rb.Log_BCF_Kinetic_max);
						er.property_value_min_final = Double.parseDouble(rb.Log_BCF_Kinetic_min);
						er.property_value_string = propertyValue + " (log)";

					}
				}
			} catch (NumberFormatException e) {
				System.out.println("rb.Log_BCF_Kinetic_mean="+rb.Log_BCF_Kinetic_mean);
//				e.printStackTrace();
			}

		} else if (propertyName.equals("LogBAF")) {
			
			if (!isNumeric(rb.Log_BAF_mean)) return;
			er.property_name = propertyName;
			er.keep = false;
			er.reason = "BAF value";

//			er.property_value_units_final = rb.Log_BAF_units;
			
			er.property_value_units_final = "Log10("+rb.Log_BAF_units+")";
			
			er.property_value_units_final.replace("(L/kg-ww)", "(L/kg)");
			
//			er.experimental_parameters.put("study quality",rb.Study_Quality_BAF);
			er.reliability=rb.Study_Quality_BAF;
			
			try {
				
				String propertyValue = rb.Log_BAF_mean;
				
				if (rb.Log_BAF_arithmetic_or_logarithmic!=null
						&& rb.Log_BAF_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
					er.property_value_string = propertyValue + " (arithmetic)";
					er.property_value_point_estimate_final = Math.log10(Double.parseDouble(rb.Log_BAF_mean));
				} else {
					
					if (rb.Log_BAF_mean.contains("<") || rb.Log_BAF_mean.contains(">")) {
						er.property_value_numeric_qualifier=rb.Log_BAF_mean.substring(0,1);
						rb.Log_BAF_mean=rb.Log_BAF_mean.substring(1,rb.Log_BAF_mean.length());
					}
					
					er.property_value_point_estimate_final = Double.parseDouble(rb.Log_BAF_mean);
					er.property_value_string = propertyValue + " (log)";

				}
				if (rb.Log_BAF_min!=null && rb.Log_BAF_max!=null) {
					String propertyValue2 = rb.Log_BAF_min + "~" + rb.Log_BAF_max;

					if ((rb.Log_BAF_arithmetic_or_logarithmic!=null)
							&& rb.Log_BAF_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
						er.property_value_string = propertyValue2 + " (arithmetic)";

						er.property_value_max_final = Math.log10(Double.parseDouble(rb.Log_BAF_min));
						er.property_value_min_final = Math.log10(Double.parseDouble(rb.Log_BAF_max));
					} else {
						er.property_value_max_final = Double.parseDouble(rb.Log_BAF_min);
						er.property_value_min_final = Double.parseDouble(rb.Log_BAF_max);
						er.property_value_string = propertyValue2 + " (log)";

					}
				}
			} catch (NumberFormatException e) {
				System.out.println("rb.Log_BAF_mean="+rb.Log_BAF_mean);
				e.printStackTrace();
			}
		} else {
			System.out.println("null property:"+propertyName);
			System.out.println(gson.toJson(rb));
			return;
		}

		// limiting things to whole body BCF based on feedback from Todd Martin 9/1/2019
		if (!(rb.Tissue.contains("whole body"))) {
			er.keep = false;
			er.reason = "not whole body";
		} else if (rb.Tissue.contains("whole body") && !(propertyName.equals("LogBAF"))) {
			er.keep = true;
		}

		// limiting our search to fish
		if (!rb.class_taxonomy.toLowerCase().contains("actinopteri")
				&& !rb.class_taxonomy.toLowerCase().contains("actinopterygii")
				&& !rb.class_taxonomy.toLowerCase().contains("teleostei")) {
			er.keep = false;
			er.reason = "not a fish";
		}

		// limiting to only high and medium quality studies
		if ((rb.Study_Quality_BAF!=null && rb.Study_Quality_BAF.toLowerCase().contains("low")) || (rb.Study_Quality_BCF!=null && rb.Study_Quality_BCF.toLowerCase().contains("low"))) {
			er.keep = false;
			er.reason = "untrusted study";
		}

		/*
		 * if ((rb.Tissue != null && !rb.Tissue.isBlank()) &&
		 * (rb.Tissue.toLowerCase().contains("plasma"))) { er.keep = false; er.reason =
		 * "empty tissue or plasma record"; }
		 */

		if (!(er.property_value_point_estimate_final != null)) {
			er.keep = false;
			er.reason = "unable to parse a value";
		}
		
//		System.out.println(er.property_name);
		
		
		recordsExperimental.add(er);
	}

	public static void main(String[] args) {
		ParseBurkhard p = new ParseBurkhard();
		p.generateOriginalJSONRecords=false;
		p.createFiles();
	}

}