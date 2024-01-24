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
import gov.epa.exp_data_gathering.parse.ToxVal.ParseToxVal;
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
				RecordBurkhard rb = it.next();
				addNewExperimentalRecordBCF_Kinetic(rb, databaseFolder, recordsExperimental);
				addNewExperimentalRecordBCF_SS(rb, "LogBCFSteadyState", recordsExperimental);
				addNewExperimentalRecordBAF(rb, "LogBAF", recordsExperimental);
			}

			Hashtable<String,ExperimentalRecords>htRecordsBySID=new Hashtable<>();

			for (ExperimentalRecord er:recordsExperimental) {
				
				if (!er.property_name.contains("BCF")) {
					er.keep=false;
					er.reason="Not BCF";
//					continue;
				}
				
				if (er.experimental_parameters.get("tissue")==null || 
						!er.experimental_parameters.get("tissue").equals("whole body")) {
					er.keep=false;
					er.reason="Not whole body";
				}
				
//				if (er.experimental_parameters.get("method")==null || 
//						!er.experimental_parameters.get("method").equals("steady state")) {
//					er.keep=false;
//					er.reason="Not steady state";
//				}

//				if (er.experimental_parameters.get("media")==null || 
//						!er.experimental_parameters.get("media").equals("freshwater")) {
//					er.keep=false;
//					er.reason="Not steady state";
//				}
				
				
				if(!er.keep) {
					
					JsonObject jo=new JsonObject();
					jo.addProperty("reason", er.reason);
					jo.addProperty("property_name", er.property_name);
					jo.addProperty("tissue", er.experimental_parameters.get("tissue")+"");
					jo.addProperty("method", er.experimental_parameters.get("method")+"");
					jo.addProperty("media", er.experimental_parameters.get("media")+"");
					jo.addProperty("exposure_type", er.experimental_parameters.get("exposure_type")+"");
					
					System.out.println(gson.toJson(jo));
					
					
					continue;
				}

				
				if (!er.keep) continue;
				citations.add(er.literatureSource.citation);
				
				if(htRecordsBySID.get(er.dsstox_substance_id)==null) {
					ExperimentalRecords records=new ExperimentalRecords();
					records.add(er);
					htRecordsBySID.put(er.dsstox_substance_id, records);
				} else {
					ExperimentalRecords records=htRecordsBySID.get(er.dsstox_substance_id);
					records.add(er);
				}
			}
			
			double avgSD=0;
			int count=0;
			int countOverall=0;
			
			for (String dtxsid:htRecordsBySID.keySet()) {
				ExperimentalRecords records=htRecordsBySID.get(dtxsid);
				double SD=ParseToxVal.calculateSD(records);
				avgSD+=SD;
				count++;
				countOverall+=records.size();
			}
			
			avgSD/=(double)count;			
			
			
			System.out.println("All records\t"+recordsExperimental.size());
			System.out.println("Kept records\t"+countOverall);
			System.out.println("Unique SIDs\t"+htRecordsBySID.size());
			System.out.println("Avg SD\t"+avgSD);
			
//			for (String citation:citations) {
//				System.out.println(citation);
//			}
//			System.out.println(citations.size());

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return recordsExperimental;
	}

	
	boolean isNumeric(String value) {

		if (value==null) return false;
		
		String v=value.toLowerCase().trim();

		if(v.isBlank() || v.equals("-") || v.equals("n.d.") || v.equals("na") || v.equals("--") || v.equals("n/a") || 
				v.equals("nd") || v.equals("?") || v.equals("n.c.") || 
				v.equals("<lod") || v.equals("nc") || v.equals("n.a.") || v.equals("n.a") || v.equals("na*")) {
			return false;
		} else {
			return true;
		}

	}
	

	private void addNewExperimentalRecordBAF(RecordBurkhard rb, String propertyName,
			ExperimentalRecords recordsExperimental) {
				
		
		if (!isNumeric(rb.Log_BAF_mean) && !isNumeric(rb.Log_BAF_max)) return;
		
		ExperimentalRecord er=new ExperimentalRecord();
		er.experimental_parameters=new Hashtable<>();

		
		er.property_name = propertyName;
		

//		er.property_value_units_final = rb.Log_BAF_units;
		
		er.property_value_units_final = "Log10("+rb.Log_BAF_units+")";
		
		er.property_value_units_final.replace("(L/kg-ww)", "(L/kg)");
		
//		er.experimental_parameters.put("study quality",rb.Study_Quality_BAF);
		er.reliability=rb.Study_Quality_BAF;
		
		try {
			
			String propertyValue = rb.Log_BAF_mean;
			
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

			} else if (rb.Log_BAF_arithmetic_or_logarithmic!=null
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
			
			
		} catch (Exception e) {
			System.out.println("Parse error BAF:\n"+gson.toJson(rb));
			e.printStackTrace();
		}
		
		if ((rb.Study_Quality_BAF!=null && rb.Study_Quality_BAF.toLowerCase().contains("low")) || (rb.Study_Quality_BCF!=null)) {
			er.keep = false;
			er.reason = "untrusted study";
		}
		
		addMetadata(er, rb);
		recordsExperimental.add(er);

	}


	private void addNewExperimentalRecordBCF_SS(RecordBurkhard rb, String propertyName,
			ExperimentalRecords recordsExperimental) {
		
		
		if (!isNumeric(rb.Log_BCF_Steady_State_mean) && !isNumeric(rb.Log_BCF_Steady_State_max)) return;
		
		ExperimentalRecord er=new ExperimentalRecord();
		er.experimental_parameters=new Hashtable<>();

		
		er.property_name = ExperimentalConstants.strLogBCF_Fish_Whole_Body;
		er.property_value_units_final = "Log10("+rb.Log_BCF_Steady_State_units+")";
		er.property_value_units_final.replace("(L/kg-ww)", "(L/kg)");
		
//		er.experimental_parameters.put("study quality",rb.Study_Quality_BCF);
		er.reliability=rb.Study_Quality_BCF;

		er.experimental_parameters.put("method","steady state");
		
		
//		jsonNote.put("study quality", rb.Study_Quality_BCF);
//		er.updateNote(jsonNote.toString());

		try {
			String propertyValue = rb.Log_BCF_Steady_State_mean;
			
			
			if ((rb.Log_BCF_Steady_State_max!=null) && (rb.Log_BCF_Steady_State_min!=null)) {
				
				
				if(rb.Log_BCF_Steady_State_min.contains("<")) {
					er.keep=false;
					er.reason="Min value has < symbol";
				} else {

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
				
			} else if (rb.Log_BCF_Steady_State_arithmetic_or_logarithmic!=null && rb.Log_BCF_Steady_State_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
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
			
		} catch (Exception e) {
			System.out.println("Parse error BCF SS:\n"+gson.toJson(rb));
//			e.printStackTrace();
		}
		
		if (rb.Study_Quality_BCF!=null && rb.Study_Quality_BCF.toLowerCase().contains("low")) {
			er.keep = false;
			er.reason = "untrusted study";
		}
		addMetadata(er, rb);
		recordsExperimental.add(er);

	}


	private void addNewExperimentalRecordBCF_Kinetic(RecordBurkhard rb, String propertyName,
			ExperimentalRecords recordsExperimental) {
		
		
		if (!isNumeric(rb.Log_BCF_Kinetic_mean)  && !isNumeric(rb.Log_BCF_Kinetic_max)) return;
		
		ExperimentalRecord er=new ExperimentalRecord();
		er.experimental_parameters=new Hashtable<>();

		
		er.property_name = ExperimentalConstants.strLogBCF_Fish_Whole_Body;
//		er.property_value_units_final = rb.Log_BCF_Kinetic_units;
		
		er.property_value_units_final = "Log10("+rb.Log_BCF_Kinetic_units+")";
		er.property_value_units_final.replace("(L/kg-ww)", "(L/kg)");

//		er.experimental_parameters.put("study quality",rb.Study_Quality_BCF);
		er.reliability=rb.Study_Quality_BCF;

		er.experimental_parameters.put("method","kinetic");
		
		try {
			String property_value = rb.Log_BCF_Kinetic_mean;
			
			
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
			} else if (rb.Log_BCF_Kinetic_arithmetic_or_logarithmic!=null && rb.Log_BCF_Kinetic_arithmetic_or_logarithmic.toLowerCase().contains("arithmetic")) {
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
			
		} catch (Exception e) {
			System.out.println("Parse error BCF Kinetic:\n"+gson.toJson(rb));
//			e.printStackTrace();
		}
		
		if (rb.Study_Quality_BCF!=null && rb.Study_Quality_BCF.toLowerCase().contains("low")) {
			er.keep = false;
			er.reason = "untrusted study";
		}
		
		addMetadata(er, rb);
		recordsExperimental.add(er);
		
		
	}

		
	private void addMetadata(ExperimentalRecord er,RecordBurkhard rb) {
		
		er.dsstox_substance_id = rb.DTXSID;
		er.source_name = sourceName;
		er.chemical_name = rb.Chemical;
		er.casrn = rb.CASRN;
		if(!er.casrn.contains("-")) er.casrn=null;

		if(rb.Common_Name!=null)
			er.experimental_parameters.put("species",rb.Common_Name);
		
		if (rb.Exposure_Concentrations!=null && !rb.Exposure_Concentrations.isBlank())
			er.experimental_parameters.put("exposure concentrations",rb.Exposure_Concentrations);

		
		er.literatureSource=new LiteratureSource();
		er.literatureSource.citation=rb.Reference;
		er.literatureSource.name=rb.Reference;
		
		// limiting our search to fish
		if (!rb.class_taxonomy.toLowerCase().contains("actinopteri")
				&& !rb.class_taxonomy.toLowerCase().contains("actinopterygii")
				&& !rb.class_taxonomy.toLowerCase().contains("teleostei")) {
			er.keep = false;
			er.reason = "not a fish";
		}
		
		rb.Marine_Brackish_Freshwater=rb.Marine_Brackish_Freshwater.toLowerCase();
		
		er.experimental_parameters.put("tissue",rb.Tissue);
		er.experimental_parameters.put("media",rb.Marine_Brackish_Freshwater);
		er.experimental_parameters.put("exposure_type",rb.Marine_Brackish_Freshwater);
		
		
//		if (!(er.property_value_point_estimate_final != null)) {
//			er.keep = false;
//			er.reason = "unable to parse a value";
//		}
		
	}

	public static void main(String[] args) {
		ParseBurkhard p = new ParseBurkhard();
		p.generateOriginalJSONRecords=false;
		p.createFiles();
	}

}