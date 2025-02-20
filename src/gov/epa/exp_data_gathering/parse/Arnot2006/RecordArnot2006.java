package gov.epa.exp_data_gathering.parse.Arnot2006;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.QSAR.utilities.JsonUtilities;
import gov.epa.QSAR.utilities.CASUtilities;
import gov.epa.api.ExperimentalConstants;
import gov.epa.database.SqlUtilities;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.LiteratureSource;
import gov.epa.exp_data_gathering.parse.ParameterValue;
import gov.epa.exp_data_gathering.parse.UnitConverter;
import gov.epa.exp_data_gathering.parse.ToxVal.ParseToxVal;
import gov.epa.exp_data_gathering.parse.ToxVal.ToxValQuery;
import gov.epa.exp_data_gathering.parse.ToxVal.ToxValRecord;

/**
 * @author TMARTI02
 */
public class RecordArnot2006 {

	static int countDurationOK=0;
	static int countDurationNotOK=0;

	static String fileName="arnot 2006 a06-005.xls";
	static String sourceName="Arnot 2006";

	static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues()
			.create();

	static transient UnitConverter uc = new UnitConverter("Data" + File.separator + "density.txt");


	String endpoint_sorting_category;
	int casrn;
	String chemical_name;
	String estimated_LogKow;
	String measured_LogKow_episuite;
	String measured_LogKow_other;
	String LogKow_Reference;
	String LogKow1;
	String LogKow2;
	String organism_classification;
	String scientific_name;
	String common_name;
	String LogBAF_WW_L_kg;
	String LogBCF_WW_L_kg;
	String water_concentration_mean_ug_L;
	String water_concentration_type;
	String radiolabel_type;
	String exposure_duration_days;
	String exposure_type;
	String temperature_mean_C;
	String exposure_route;
	String exposure_media;
	String ph_mean;
	String total_organic_carbon_mg_L;
	String wet_weight_mean_g;
	String lipid_content_percentage;
	String tissue_analyzed;
	String calculation_method;

	String comments;

	String source_author;
	Integer source_year;
	String source_title;
	String source_journal;

	String criterion_water_concentration_measured;

	String criterion_radiolabel;
	String criterion_radiolabel_comment;

	String criterion_aqueous_solubility;
	String criterion_aqueous_solubility_comment;

	String criterion_exposure_duration;
	String criterion_exposure_duration_comment;

	String criterion_tissue_analyzed;

	String criterion_other_major_source;
	String criterion_other_major_source_comment;

	String overall_score;


	public static Vector<JsonObject> parseRecordsFromExcel() {
		ExcelSourceReader esr = new ExcelSourceReader(fileName, sourceName);
		HashMap<Integer, String> hmFieldNames = getHeaderMap();
		Vector<JsonObject> records = esr.parseRecordsFromExcel(hmFieldNames,-1);

		for(int i=1;i<=10;i++) {
			records.remove(0);
		}

		//		for(JsonObject jo:records) {
		//			RecordArnot2006 rec=gson.fromJson(jo, RecordArnot2006.class);
		////			System.out.println(gson.toJson(rec));	
		//		}

		return records;
	}


	private static  HashMap<Integer, String> getHeaderMap() {
		HashMap<Integer, String> hmFieldNames=new HashMap<>();
		for(int i=0;i<=50;i++) {
			hmFieldNames.put(i,"Col"+i);
		}
		int i=0;

		hmFieldNames.put(i++,"endpoint_sorting_category");
		hmFieldNames.put(i++,"casrn");
		hmFieldNames.put(i++,"chemical_name");
		hmFieldNames.put(i++,"estimated_LogKow");
		hmFieldNames.put(i++,"measured_LogKow_episuite");
		hmFieldNames.put(i++,"measured_LogKow_other");
		hmFieldNames.put(i++,"LogKow_Reference");
		hmFieldNames.put(i++,"LogKow1");
		hmFieldNames.put(i++,"LogKow2");
		hmFieldNames.put(i++,"organism_classification");
		hmFieldNames.put(i++,"scientific_name");
		hmFieldNames.put(i++,"common_name");
		hmFieldNames.put(i++,"LogBAF_WW_L_kg");
		hmFieldNames.put(i++,"LogBCF_WW_L_kg");
		hmFieldNames.put(i++,"water_concentration_mean_ug_L");
		hmFieldNames.put(i++,"water_concentration_type");
		hmFieldNames.put(i++,"radiolabel_type");
		hmFieldNames.put(i++,"exposure_duration_days");
		hmFieldNames.put(i++,"exposure_type");
		hmFieldNames.put(i++,"temperature_mean_C");
		hmFieldNames.put(i++,"exposure_route");
		hmFieldNames.put(i++,"exposure_media");
		hmFieldNames.put(i++,"ph_mean");
		hmFieldNames.put(i++,"total_organic_carbon_mg_L");
		hmFieldNames.put(i++,"wet_weight_mean_g");
		hmFieldNames.put(i++,"lipid_content_percentage");
		hmFieldNames.put(i++,"tissue_analyzed");
		hmFieldNames.put(i++,"calculation_method");
		hmFieldNames.put(i++,"comments");
		hmFieldNames.put(i++,"source_author");
		hmFieldNames.put(i++,"source_year");
		hmFieldNames.put(i++,"source_title");
		hmFieldNames.put(i++,"source_journal");
		i++;
		hmFieldNames.put(i++,"criterion_water_concentration_measured");
		i++;
		hmFieldNames.put(i++,"criterion_radiolabel");
		hmFieldNames.put(i++,"criterion_radiolabel_comment");
		i++;
		hmFieldNames.put(i++,"criterion_aqueous_solubility");
		hmFieldNames.put(i++,"criterion_aqueous_solubility_comment");
		i++;
		hmFieldNames.put(i++,"criterion_exposure_duration");
		hmFieldNames.put(i++,"criterion_exposure_duration_comment");
		i++;
		hmFieldNames.put(i++,"criterion_tissue_analyzed");
		i++;
		hmFieldNames.put(i++,"criterion_other_major_source");
		hmFieldNames.put(i++,"criterion_other_major_source_comment");
		i++;
		hmFieldNames.put(i,"overall_score");
		return hmFieldNames;
	}


	/**
	 * time to get to 80% of steady state concentration
	 * 
	 * @param logKow
	 * @param logBCF
	 * @return
	 */
	double calcT80(double logKow) {
		
		double W=0.002;
		double Dox=7.1;
		double Gv=980*Math.pow(W,0.65)/Dox;
		double Lb=0.05;
		double NLOMb=0.2;
		double NLOMg=0.24;
		double B=0.035;
		double Gd=0.015*W;
		double Gf=0.5*Gd;
		double Lg=0.012;
		double WCg=0.74;
		double WCb=1-(Lb+NLOMb);
		double T=21;
		
		double Kow=Math.pow(10,logKow);
		double Ed=1/(3e-7*Kow+2);
		double Kgb=(Lg*Kow + NLOMg*B*Kow +WCg)/(Lb*Kow+NLOMb*B*Kow+WCb);
		double Ew=0.006;
		if(logKow>=0) Ew=1/(1.85+155/Kow);
		
//		double BCF=Math.pow(10,logBCF);
//		double Cb=BCF*Cw_g_L;//organism concentration in g/kg
		
		
		double k1=Ew*Gv/W;
		double k2=k1/(Lb*Kow+NLOMb*Kow*B+WCb);
		double ke=Gf*Ed*Kgb/W;
		double kg=0.00586*Math.pow(1.13,T-20)*Math.pow(1000*W,-0.2);		
		double km=0;//assumed to not be metabolized- not true for esters
		double kt=k2+ke+kg+km;
		
		double t80=1.6/kt;
		
//		System.out.println(t80);
		
		return t80;
		
	}
	
	public ExperimentalRecord toExperimentalRecordBCF(String propertyName, Hashtable<String, List<Species>> htSpecies) {

		//Not BCF:
		if(!endpoint_sorting_category.equals("2.0")) return null; //Endpoint 2=total BCF (1=BAF,3=BCFfd,4=BAFmodeled)

		boolean limitToFish=false;
		if(propertyName.toLowerCase().contains("fish")) limitToFish=true;
		
		boolean limitToWholeBody=false;
		if(propertyName.toLowerCase().contains("whole")) limitToWholeBody=true;
		
		
		ExperimentalRecord er=new ExperimentalRecord();
		er.property_name=propertyName;
		er.property_category="bioconcentration";//so that unit converter can handle various BCF endpoints

		String CAS=CASUtilities.fixIntegerCAS(casrn);
		if(CASUtilities.isCAS_OK(CAS)) {
			er.casrn=CAS;
		} else {
			System.out.println("Invalid CAS from database: " + casrn + " 	Invalid cas: " + CAS);
		}

		er.chemical_name=chemical_name;
		er.source_name=sourceName;

		setLiteratureSource(er);

		er.experimental_parameters=new LinkedHashMap<>();//keeps insertion order
		
		setSpeciesParameters(htSpecies, limitToFish, er);
//		System.out.println(limitToFish+"\t"+supercategory);

		er.experimental_parameters.put("Response site", tissue_analyzed);
		if(limitToWholeBody && (tissue_analyzed==null || !tissue_analyzed.equals("Whole body"))) {
			er.keep=false;
			er.reason="Not whole body";
		}

		//		er.experimental_parameters.put("Water concentration (ug/L)", water_concentration_mean_ug_L);
		setWaterConcentration(er);

		//TODO store like water conc?
		
		if(exposure_duration_days.contains("L")) {
			er.experimental_parameters.put("Exposure Duration (in days or Lifetime)", "Lifetime");
		} else {
			er.experimental_parameters.put("Exposure Duration (in days or Lifetime)", exposure_duration_days);
		}

		compareT80_To_Exposure_Duration();
				
		setChemAnalysisMethod(er);//Criterion 1
		setExposureType(er);
		setExposureMedia(er);
		setCriteria(er);
		
		
		if(!temperature_mean_C.equals("N/A")) {
			er.temperature_C=Double.parseDouble(temperature_mean_C);
		}
		if(!ph_mean.equals("N/A")) {
			er.pH=ph_mean;
		}
		
		er.property_value_units_original=ExperimentalConstants.str_LOG_L_KG;
		er.property_value_string=LogBCF_WW_L_kg + " "+er.property_value_units_original;

		try {
			er.property_value_point_estimate_original=Double.parseDouble(LogBCF_WW_L_kg);
		} catch(Exception ex) {
			System.out.println("Cant convert BCF=\t"+LogBCF_WW_L_kg);
			//System.out.println(gson.toJson(this));
		}

		if(comments!=null) {
			er.note= comments;
		}

		uc.convertRecord(er);

		return er;	
	}


	private void setSpeciesParameters(Hashtable<String, List<Species>> htSpecies, boolean limitToFish,
			ExperimentalRecord er) {
		
		
		er.experimental_parameters.put("Species latin", scientific_name);
		er.experimental_parameters.put("Species common", common_name);
		er.experimental_parameters.put("Organism classification", organism_classification);
		String supercategory=getSpeciesSupercategory(htSpecies);
		if(supercategory!=null)	er.experimental_parameters.put("Species supercategory", supercategory);

		
		if(limitToFish && !supercategory.equals("Fish")) {
			er.keep=false;
			er.reason="Not a fish species";
		}
	}


	private void compareT80_To_Exposure_Duration() {

//		if(LogBCF_WW_L_kg==null || LogKow1.equals("N/A")) return;
		
		if(LogKow1.equals("N/A")) return;

		double logKow=Double.parseDouble(LogKow1);
		
//		if(!LogKow1.equals(LogKow2)) {
//			System.out.println("Mismatch logkow:"+LogKow1+"\t"+LogKow2);
//		}
		
//		double logBCF=Double.parseDouble(LogBCF_WW_L_kg);
		double t80=this.calcT80(logKow);

		if(!exposure_duration_days.equals("L") && !exposure_duration_days.equals("N/A")) {
			double duration=Double.parseDouble(exposure_duration_days);
			//				System.out.println(exposure_duration_days+"\t"+t80);

			if(t80>duration) {

				//	if(calculation_method!=null && calculation_method.equals("K1/K2")) {
				//		System.out.println("Failed t80 but k1/k2");
				//		countDurationOK++;
				//	} else {
				//		countDurationNotOK++;	
				//	}

				countDurationNotOK++;

				//	if(logKow>6)					
				//		System.out.println(chemical_name+"\t"+logKow+"\t"+exposure_duration_days+"\t"+t80);
			} else {
				//	if(logKow>5)					
				//	System.out.println(chemical_name+"\t"+logKow+"\t"+exposure_duration_days+"\t"+t80);
				countDurationOK++;
			}
		}


	}


	private void setWaterConcentration(ExperimentalRecord er) {

		if(water_concentration_mean_ug_L==null)return;
		if(water_concentration_mean_ug_L.equals("5x < Sw"))return;
		if(water_concentration_mean_ug_L.equals("N/A"))return;
		if(water_concentration_mean_ug_L.isBlank())return;

		water_concentration_mean_ug_L=water_concentration_mean_ug_L.replace("mCi/mmol", "Ci/mol");
		water_concentration_mean_ug_L=water_concentration_mean_ug_L.replace("dpm/ml", "dpm/mL");
		water_concentration_mean_ug_L=water_concentration_mean_ug_L.replace("Bq/ml", "Bq/mL");
		water_concentration_mean_ug_L=water_concentration_mean_ug_L.replace("mBq/ml", "mBq/mL");
				
		try {

			ParameterValue pv=new ParameterValue();
			pv.parameter.name="Water concentration";

			List<String>badUnits=Arrays.asList("Ci/mol","dpm/mL","mBq/mL","Bq/mL");

			for (String badUnit:badUnits) {

				if(water_concentration_mean_ug_L.contains(badUnit)) {
					pv.unit.abbreviation=badUnit;	
					
					water_concentration_mean_ug_L=water_concentration_mean_ug_L.replace(badUnit,"");
					if(water_concentration_mean_ug_L.isBlank())return;//no value
					pv.valuePointEstimate=Double.parseDouble(water_concentration_mean_ug_L);
					
					if(pv.unit.abbreviation.equals("mBq/mL")) {
//						System.out.println("Converting mBq/mL");
						pv.unit.abbreviation="Bq/mL";
						pv.valuePointEstimate/=1000.0;	
					}

					break;
				}
			}

			if(pv.unit.abbreviation==null) {
				pv.unit.abbreviation=ExperimentalConstants.str_g_L;
				double wc=Double.parseDouble(water_concentration_mean_ug_L);					
				pv.valuePointEstimate=wc*1e-6;
				//	System.out.println(pv.valuePointEstimate+" g/L");
			}

			er.parameter_values=new ArrayList<>();
			er.parameter_values.add(pv);

		} catch (Exception e) {
			System.out.println("Error converting wc:"+water_concentration_mean_ug_L+", "+this.chemical_name);
		}


	}


	private void setChemAnalysisMethod(ExperimentalRecord er) {

		//Ecotox types:
		//	    "value_text": "Measured"
		//	    "value_text": "Unmeasured"
		//	    "value_text": "Not coded"
		//	    "value_text": "Chemical analysis reported"
		//	    "value_text": "Unmeasured values (some measured values reported in article)"
		//	    "value_text": "Not reported"

		//Criterion 1
		if(water_concentration_type.equals("M")) {
			er.experimental_parameters.put("chem_analysis_method", "Measured");	
		} else if(water_concentration_type.equals("N")) {
			er.experimental_parameters.put("chem_analysis_method", "Unmeasured");
		} else if(water_concentration_type.equals("U")) {
			er.experimental_parameters.put("chem_analysis_method", "Not reported");
		} else {
			System.out.println("Handle "+water_concentration_type);
		}
	}


	private void setLiteratureSource(ExperimentalRecord er) {
		LiteratureSource ls=new LiteratureSource();
		er.literatureSource=ls;

		source_author=source_author.replace(".. .","").trim();

		ls.name=source_author+" ("+source_year+")";
		ls.author=source_author;
		ls.title=source_title;

		if(ls.title!=null) {
			if(ls.title.endsWith(".")) {
				ls.title=ls.title.substring(0,ls.title.length()-1);
				//				System.out.println(ls.title);
			}
		} 

		if(source_journal==null) {
			if(source_author.equals("Kitano, M.")) {
				source_journal="OECD Tokyo Meeting. Reference Book TSU-No. 3";
			} else {
				source_journal="";
			}
		}
		ls.citation=source_author+" ("+source_year+"). "+ls.title+". "+source_journal;
		er.reference=ls.citation;

		if(source_author==null || source_year==null || source_title==null || source_journal==null) {
			System.out.println("\nHandle missing source field:\t"+gson.toJson(ls));

			//TODO google search to find missing info
		}
	}


	private void setCriteria(ExperimentalRecord er) {
		er.experimental_parameters.put("Criterion 1- Water Concentration", criterion_water_concentration_measured); //1=Measured, 2=Uncertain, 3=Not measured/Nominal
		er.experimental_parameters.put("Criterion 2- Radiolabel", criterion_radiolabel);//1=Not used or corrected for parent, 3=total radioactivity/uncertain
		er.experimental_parameters.put("Criterion 3- Aqueous Solubility", criterion_aqueous_solubility);//1=below by a factor of 5, 2=within a factor of 5 or uncertain exposure concentration, 3=above by a factor of 5
		er.experimental_parameters.put("Criterion 4- Exposure Duration", criterion_exposure_duration);//1=calculated sufficient or kinetic or reported @ SS, 2=uncertain physchem data or duration, 3=calculated insufficient or reported not at SS
		er.experimental_parameters.put("Criterion 5- Tissue Analyzed", criterion_tissue_analyzed);//1A=whole body;lipid, 1B=whole body; no lipid, 2=tissue lipid corrected or k1/k2 or soft tissue for invertebrates, 3=tissue no lipid correction
		
		if(criterion_other_major_source!=null) {
			er.experimental_parameters.put("Criterion 6- Other Major Source", criterion_other_major_source);
		}
		//		System.out.println(exposure_duration_days);
		if(overall_score.equals("1.0")) {
			overall_score="1: Acceptable BCF";
		} else if(overall_score.equals("2.0")) {
			overall_score="2: No phys-chem";//we can estimate these if have water concentration, how often does this happen?
		} else if(overall_score.equals("3.0")) {
			overall_score="3: Low BCF";
		} else {
			overall_score=null;
			System.out.println("No overall score");
		}
		er.experimental_parameters.put("Overall Score", overall_score);//1=acceptable, 2=no physchem, 3=low
		
		
		if(er.property_name.contains("OverallScore_1_or_2")) {
			if(overall_score.contains("1") || overall_score.contains("2")) {
				
			} else {
//				System.out.println("Omit since OverallScore="+overall_score);
				er.keep=false;
				er.reason="overall_score="+overall_score;
			}
		} else if (er.property_name.contains("OverallScore_1")) {

			if(!overall_score.contains("1")) {
				er.keep=false;
				er.reason="overall_score="+overall_score;
			}
			
		}
		
	}


	private void setExposureMedia(ExperimentalRecord er) {
		if(exposure_media!=null) {
			if(exposure_media.contains("FW")) {
				exposure_media=exposure_media.replace("FW","Fresh water");
			} else if(exposure_media.contains("Fresh")) {
				exposure_media=exposure_media.replace("Fresh","Fresh water");
			} else if(exposure_media.contains("SW")) {
				exposure_media=exposure_media.replace("SW","Salt water");
			} else if(exposure_media.contains("N/A")) {
				exposure_media=null;
			} else if(exposure_media.contains("Synthetic") || exposure_media.contains("Humic water") || exposure_media.contains("Brackish"))  {
				exposure_media=exposure_media;
			} else if(exposure_media.contains("")) {
			} else {
				System.out.println("Handle exposure_media="+exposure_media);
			}
		}

		if(exposure_media!=null) er.experimental_parameters.put("Media type",exposure_media);

	}


	private void setExposureType(ExperimentalRecord er) {
		if(exposure_type.equals("FT")) {
			exposure_type=exposure_type.replace("FT","Flow-through");
		} else if(exposure_type.equals("R")) {
			exposure_type=exposure_type.replace("R","Renewed");
		} else if(exposure_type.equals("S")) {
			exposure_type=exposure_type.replace("S","Static");
		} else if(exposure_type.equals("Semi-S")) {
			exposure_type=exposure_type.replace("Semi-S","Semi-Static");
		} else if(exposure_type.equals("Lentic")) {
			exposure_type="Lentic";
		} else if(exposure_type.equals("N/A")) {
			//leave null
		} else {
			System.out.println("Handle exposure_type="+exposure_type);
		}

		if(exposure_type!=null) er.experimental_parameters.put("exposure_type", exposure_type);

	}


	private void setSpeciesSupercategory(Hashtable<String, List<Species>> htSpecies, ExperimentalRecord er) {
		if(htSpecies.containsKey(common_name.toLowerCase())) {
			List<Species>speciesList=htSpecies.get(common_name.toLowerCase());
			for(Species species:speciesList) {

				if(species.species_supercategory.contains("fish")) {
					er.experimental_parameters.put("Species supercategory", "fish");
					break;
				} else if(species.species_supercategory.contains("algae")) {
					er.experimental_parameters.put("Species supercategory", "algae");
					break;
				} else if(species.species_supercategory.contains("crustaceans")) {
					er.experimental_parameters.put("Species supercategory", "crustaceans");
					break;
				} else if(species.species_supercategory.contains("insects/spiders")) {
					er.experimental_parameters.put("Species supercategory", "insects/spiders");
					break;
				} else if(species.species_supercategory.contains("molluscs")) {
					er.experimental_parameters.put("Species supercategory", "molluscs");
					break;
				} else if(species.species_supercategory.contains("worms")) {
					er.experimental_parameters.put("Species supercategory", "worms");
					break;
				} else if(species.species_supercategory.contains("invertebrates")) {
					er.experimental_parameters.put("Species supercategory", "invertebrates");
					break;
				} else if(species.species_supercategory.contains("flowers, trees, shrubs, ferns")) {
					er.experimental_parameters.put("Species supercategory", "flowers, trees, shrubs, ferns");
					break;
				} else {
					//					System.out.println("Handle\t"+common_name+"\t"+species.species_supercategory);	
				}

			}
		} else {
			System.out.println("missing in hashtable:\t"+"*"+common_name.toLowerCase()+"*");
		}
	}

	private String getSpeciesSupercategory(Hashtable<String, List<Species>> htSpecies) {

		if(htSpecies.containsKey(common_name.toLowerCase())) {

			List<Species>speciesList=htSpecies.get(common_name.toLowerCase());

			for(Species species:speciesList) {


				//				if(species.species_scientific!=null) {
				//					if (!species.species_scientific.toLowerCase().equals(this.scientific_name.toLowerCase())) {
				//						System.out.println(this.scientific_name+"\t"+species.species_scientific+"\tmismatch");
				//					}
				//				} else {
				////					System.out.println(common_name+"\tspecies has null scientific");
				//				}

				if(species.species_supercategory.contains("fish")) {
					return "Fish";
				} else if(species.species_supercategory.contains("algae")) {
					return "Algae";
				} else if(species.species_supercategory.contains("crustaceans")) {
					return "Crustaceans";
				} else if(species.species_supercategory.contains("insects/spiders")) {
					return "Insects/spiders";
				} else if(species.species_supercategory.contains("molluscs")) {
					return "Molluscs";
				} else if(species.species_supercategory.contains("worms")) {
					return "Worms";
				} else if(species.species_supercategory.contains("invertebrates")) {
					return "Invertebrates";
				} else if(species.species_supercategory.contains("flowers, trees, shrubs, ferns")) {
					return "Flowers, trees, shrubs, ferns";
				} else if(species.species_supercategory.equals("omit")) {
					return "omit";
				} else {
					System.out.println("Handle\t"+common_name+"\t"+species.species_supercategory);	
				}
			}
		} else {
			System.out.println("missing in hashtable:\t"+"*"+common_name.toLowerCase()+"*");
		}

		return null;
	}


	static class Species {
		Integer id;
		String species_common;
		String species_scientific;
		String species_supercategory;
		String habitat;
	}


	/**
	 * this works for prod_dsstox- not v93 version since species table is different
	 * 
	 * @param tvq
	 * @return
	 */
	public static Hashtable<String, List<Species>> createSupercategoryHashtable(Connection conn) {
		Hashtable<String,List<Species>>htSpecies=new Hashtable<>();

		String sql="select species_id, species_common, species_scientific, species_supercategory, habitat from species";

		try {

			Statement st = conn.createStatement();			
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {

				Species species=new Species();

				species.id=rs.getInt(1);
				species.species_common=rs.getString(2);
				species.species_scientific=rs.getString(3);
				species.species_supercategory=rs.getString(4);
				species.habitat=rs.getString(5);

				if(htSpecies.get(species.species_common)==null) {
					List<Species>speciesList=new ArrayList<>();
					speciesList.add(species);
					htSpecies.put(species.species_common, speciesList);
				} else {
					List<Species>speciesList=htSpecies.get(species.species_common);
					speciesList.add(species);
				}
			}


			//			System.out.println(sql);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return htSpecies;
	}

	void putEntry(Hashtable<String, List<Species>> htSpecies,String species_common,String supercategory) {

		if(htSpecies.get(species_common)==null) {
			List<Species>speciesList=new ArrayList<>();
			Species species=new Species();
			species.species_common=species_common;
			species.species_supercategory=supercategory;
			speciesList.add(species);
			htSpecies.put(species_common, speciesList);
		} else {
			List<Species>speciesList=htSpecies.get(species_common);

			Species species=new Species();
			species.species_common=species_common;
			species.species_supercategory=supercategory;
			speciesList.add(species);
		}


	}



	void getSpeciesSuperCategoryHashtable() {

		String toxvalVersion=ParseToxVal.versionProd;


		Connection conn=SqlUtilities.getConnectionDSSTOX();

		//Need to create a dictionary to map all fish by common name:
		Hashtable<String, List<Species>> htSuperCategory = createSupercategoryHashtable(conn);

		putEntry(htSuperCategory, "phytoplankton", "omit");
		putEntry(htSuperCategory, "common shrimp", "omit");
		putEntry(htSuperCategory, "baskettail dragonfly", "omit");
		putEntry(htSuperCategory, "common bay mussel", "omit");
		putEntry(htSuperCategory, "depressed river mussel", "omit");
		putEntry(htSuperCategory, "clams", "omit");
		putEntry(htSuperCategory, "tadpole", "omit");
		putEntry(htSuperCategory, "algae, algal mat", "omit");
		putEntry(htSuperCategory, "schizothrix calcicola", "omit");

		putEntry(htSuperCategory, "biwi lake gudgeon, goby or willow shiner", "fish");
		putEntry(htSuperCategory, "willow shiner", "fish");
		putEntry(htSuperCategory, "golden ide", "fish");
		putEntry(htSuperCategory, "gobi", "fish");
		putEntry(htSuperCategory, "topmouth gudgeon", "fish");
		putEntry(htSuperCategory, "shorthead redhorse", "fish");
		putEntry(htSuperCategory, "golden redhorse", "fish");
		putEntry(htSuperCategory, "medaka, high-eyes", "fish");
		putEntry(htSuperCategory, "brook silverside", "fish");
		putEntry(htSuperCategory, "coho salmon", "fish");
		putEntry(htSuperCategory, "lemon shark", "fish");

		System.out.println(gson.toJson(htSuperCategory));

		JsonUtilities.savePrettyJson(htSuperCategory, "data\\experimental\\Arnot 2006\\htSuperCategory.json");

	}


	public static void main(String[] args) {
		RecordArnot2006 r=new RecordArnot2006();
		//		r.parseRecordsFromExcel();
		r.getSpeciesSuperCategoryHashtable();
		r.calcT80(5.34);
		
	}



}



