package gov.epa.exp_data_gathering.parse.ECOTOX;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.JSONUtilities;
import gov.epa.exp_data_gathering.parse.LiteratureSource;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

/**
* @author TMARTI02
*/
public class ParseEcotox {

	Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	
	void getAcuteAquaticExperimentalRecords() {


		String propertyName=ExperimentalConstants.strNINETY_SIX_HOUR_FATHEAD_MINNOW_LC50;
		
		String source=ExperimentalConstants.strSourceEcotox;
		
		List<RecordEcotox>recordsOriginal=null;
		
		boolean createOriginalRecords=false;
		
		if (createOriginalRecords) {
			recordsOriginal=RecordEcotox.get_96hr_FHM_LC50_Tox_Records_From_DB();
			String jsonPath = "data/experimental/"+source+File.separator+source+" "+propertyName+" original records.json";
			int howManyOriginalRecordsFiles = JSONUtilities.batchAndWriteJSON(recordsOriginal,jsonPath);

		} else {
			try {
				String jsonPath = "data/experimental/"+source+File.separator+source+" "+propertyName+" original records.json";
				RecordEcotox[]records2 = gson.fromJson(new FileReader(jsonPath), RecordEcotox[].class);
				recordsOriginal=Arrays.asList(records2);
				System.out.println(recordsOriginal.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		ExperimentalRecords recordsExp=new ExperimentalRecords();
		
		for (RecordEcotox re:recordsOriginal) {
			addExperimentalRecords(re, recordsExp);
		}
		
		
		
		System.out.println(recordsExp.size());
		
	}

	
	private void addExperimentalRecords(RecordEcotox r, ExperimentalRecords recordsExperimental) {

		ExperimentalRecord er=new ExperimentalRecord();
		
		er.dsstox_substance_id=r.dtxsid;
		er.property_name=r.property_name;
		er.keep=true;
		
		er.property_value_string="1.23 mg/L";
		
		
		LiteratureSource ls=new LiteratureSource();
		er.literatureSource=ls;
		ls.author=r.author;
		ls.title=r.title;
		ls.year=r.publication_year;
		ls.citation=r.author+" ("+r.publication_year+"). "+r.title+"."+r.source;

		if(r.conc1_mean==null) {
			er.keep=false;
			er.reason="No conc1_mean value";
		} else if(r.conc1_mean_op!=null && !r.conc1_mean_op.equals("~")) {
			er.keep=false;
			er.reason="bad conc1_mean_op: "+r.conc1_mean_op;
		} else if(r.conc1_min_op!=null && !r.conc1_min_op.equals("~")) {
			er.keep=false;
			er.reason="bad conc1_min_op:"+r.conc1_min_op;
		} else if(r.conc1_max_op!=null && !r.conc1_max_op.equals("~")) {
			er.keep=false;
			er.reason="bad conc1_max_op:"+r.conc1_max_op;
		}


		if (er.keep) {
			er.property_value_point_estimate_original=Double.parseDouble(r.conc1_mean);
			er.property_value_units_original=r.conc1_unit;
			
			if(r.conc1_min!=null && r.conc1_max!=null) {

				double max=Double.parseDouble(r.conc1_max);
				double min=Double.parseDouble(r.conc1_min);
				double log=Math.log10(max/min);
				
				if(log>1) {
					er.keep=false;
					er.reason="Range of min and max is too wide";
//					System.out.println(min+"\t"+max);
				}
			}
			
//			System.out.println(r.conc1_max_op+"\t"+r.conc1_min_op+"\t"+r.conc1_mean_op);
		} 
		
		if(!er.keep) return;
				
//		uc.convertRecord(er);
		
		recordsExperimental.add(er);
		
//		er.recordOriginal=r;
		
	}


	public static void main(String[] args) {
		ParseEcotox p = new ParseEcotox();
		p.getAcuteAquaticExperimentalRecords();
		
		

//		p.maxExcelRows=999999;
		

	}

}
