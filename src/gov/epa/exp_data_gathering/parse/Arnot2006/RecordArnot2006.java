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

		ExperimentalRecord er=new ExperimentalRecord();
		er.property_name=propertyName;

		String strPropertyValue=null;
		if(propertyName.toLowerCase().contains("bioconcentration factor")) {
			if(!endpoint_sorting_category.equals("2.0")) return null;
			strPropertyValue=LogBCF_WW_L_kg;
			er.property_category="bioconcentration";//so that unit converter can handle various BCF endpoints
		} else if(propertyName.toLowerCase().contains("bioaccumulation factor")) {
			if(!endpoint_sorting_category.equals("1.0")) return null;
			strPropertyValue=LogBAF_WW_L_kg;
			er.property_category="bioaccumulation";//so that unit converter can handle various BAF endpoints

		}

		boolean limitToFish=false;
		if(propertyName.toLowerCase().contains("fish")) limitToFish=true;
		
		boolean limitToWholeBody=false;
		if(propertyName.toLowerCase().contains("whole")) limitToWholeBody=true;
		
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
		er.parameter_values=new ArrayList<>();

		setSpeciesParameters(htSpecies, limitToFish, er);
		setResponseSite(limitToWholeBody, er);//Criterion 5
		setWaterConcentration(er);//Criterion 3
		setExposureDuration(er);//Criterion 4
		setChemAnalysisMethod(er);//Criterion 1
		setExposureType(er);
		setExposureMedia(er);
		setCriteria(er);
		
		if(!temperature_mean_C.equals("N/A")) er.temperature_C=Double.parseDouble(temperature_mean_C);
		if(!ph_mean.equals("N/A")) er.pH=ph_mean;
		
		er.property_value_units_original=ExperimentalConstants.str_LOG_L_KG;
		er.property_value_string=strPropertyValue + " "+er.property_value_units_original;

		try {
			er.property_value_point_estimate_original=Double.parseDouble(strPropertyValue);
		} catch(Exception ex) {
			System.out.println("Cant convert propertyValue=\t"+strPropertyValue);
		}

		if(comments!=null) {
			er.note= comments;
		}

		uc.convertRecord(er);

		return er;	
	}
	
	

	private void setExposureDuration(ExperimentalRecord er) {
		if(exposure_duration_days.equals("L") || exposure_duration_days.equals("N/A")) {
//			exposure_duration_days=exposure_duration_days.replace("L", "9999");
			return;
		}

		ParameterValue pv=new ParameterValue();
		pv.parameter.name="Exposure duration";
		pv.unit.abbreviation="days";
		double wc=Double.parseDouble(exposure_duration_days);					
		pv.valuePointEstimate=wc;
		
		er.parameter_values.add(pv);

//		if(exposure_duration_days.contains("L")) {
//			er.experimental_parameters.put("Exposure duration", "Lifetime");
//		} else {
//			er.experimental_parameters.put("Exposure Duration (in days or Lifetime)", exposure_duration_days);
//		}
		compareT80_To_Exposure_Duration();
	}


	private void setResponseSite(boolean limitToWholeBody, ExperimentalRecord er) {
		
		if(tissue_analyzed.equals("Gills")) {
			tissue_analyzed=tissue_analyzed.replace("Gills", "Gill(s)");
		} else if(tissue_analyzed.equals("Gill")) {
			tissue_analyzed=tissue_analyzed.replace("Gill", "Gill(s)");
		} else if(tissue_analyzed.equals("Gonad")) {
			tissue_analyzed=tissue_analyzed.replace("Gonad", "Gonad(s)");
		}

		er.experimental_parameters.put("Response site", tissue_analyzed);
		
		if(limitToWholeBody && (tissue_analyzed==null || !tissue_analyzed.equals("Whole body"))) {
			er.keep=false;
			er.reason="Not whole body";
		}
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


	/**
	 * Store water concentration as separate ParameterValue object so can keep units separate
	 * 
	 * @param er
	 */
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

			if(pv.unit.abbreviation==null) {//the g/L ones
				pv.unit.abbreviation=ExperimentalConstants.str_g_L;
				double wc=Double.parseDouble(water_concentration_mean_ug_L);					
				pv.valuePointEstimate=wc*1e-6;
				//	System.out.println(pv.valuePointEstimate+" g/L");
			}
			
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

		//TODO also store comment fields for these
		
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
			exposure_type=exposure_type.replace("R","Renewal");
		} else if(exposure_type.equals("S")) {
			exposure_type=exposure_type.replace("S","Static");
		} else if(exposure_type.equals("Semi-S")) {
			exposure_type=exposure_type.replace("Semi-S","Semi-Static");
		} else if(exposure_type.equals("Lentic")) {
			exposure_type="Lentic";
		} else if(exposure_type.equals("Lotic")) {
			exposure_type="Lotic";
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
				} else if(species.species_supercategory.contains("microorganisms")) {
					return "microorganisms";
				} else if(species.species_supercategory.equals("amphibians") || species.species_supercategory.equals("amphibians; standard test species")) {
					return "amphibians";
				} else if(species.species_supercategory.equals("reptiles")) {
					return "reptiles";
				}else if(species.species_supercategory.equals("omit")) {
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

	void putEntryCommon(Hashtable<String, List<Species>> htSpecies,String species_common,String supercategory) {

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

	void putEntryLatin(Hashtable<String, List<Species>> htSpecies,String species_latin,String supercategory) {

		if(htSpecies.get(species_latin)==null) {
			List<Species>speciesList=new ArrayList<>();
			Species species=new Species();
			species.species_scientific=species_latin;
			species.species_supercategory=supercategory;
			speciesList.add(species);
			htSpecies.put(species_latin, speciesList);
		} else {
			List<Species>speciesList=htSpecies.get(species_latin);

			Species species=new Species();
			species.species_scientific=species_latin;
			species.species_supercategory=supercategory;
			speciesList.add(species);
		}


	}

	void getSpeciesSuperCategoryHashtable() {

		String toxvalVersion=ParseToxVal.versionProd;


		Connection conn=SqlUtilities.getConnectionDSSTOX();

		//Need to create a dictionary to map all fish by common name:
		Hashtable<String, List<Species>> htSuperCategory = createSupercategoryHashtable(conn);

		putEntryCommon(htSuperCategory, "phytoplankton", "microorganisms");
		putEntryCommon(htSuperCategory, "common shrimp", "omit");
		putEntryCommon(htSuperCategory, "baskettail dragonfly", "insects/spiders");
		putEntryCommon(htSuperCategory, "caddisfly larvae", "insects/spiders");
		putEntryCommon(htSuperCategory, "common bay mussel", "molluscs");
		putEntryCommon(htSuperCategory, "depressed river mussel", "molluscs");
		putEntryCommon(htSuperCategory, "clams", "molluscs");
		putEntryCommon(htSuperCategory, "tadpole", "amphibians");
		putEntryCommon(htSuperCategory, "algae, algal mat", "algae");
		putEntryCommon(htSuperCategory, "schizothrix calcicola", "omit");
		putEntryCommon(htSuperCategory, "buzzer midge", "insects/spiders");
		putEntryCommon(htSuperCategory, "narrowleaf cattail", "flowers, trees, shrubs, ferns");
		putEntryCommon(htSuperCategory, "northern leopard frog", "amphibians");
		putEntryCommon(htSuperCategory, "tadpole - northern leopard frog", "amphibians");
		putEntryCommon(htSuperCategory, "eastern tiger salamander", "amphibians");
		putEntryCommon(htSuperCategory, "mussel fatmucket", "molluscs");
		putEntryCommon(htSuperCategory, "sandworm", "worms");
		putEntryCommon(htSuperCategory, "oligocheate", "worms");
		putEntryCommon(htSuperCategory, "hornwort", "flowers, trees, shrubs, ferns");
		putEntryCommon(htSuperCategory, "orb snail family", "omit");


		putEntryCommon(htSuperCategory, "biwi lake gudgeon, goby or willow shiner", "fish");
		putEntryCommon(htSuperCategory, "willow shiner", "fish");
		putEntryCommon(htSuperCategory, "golden ide", "fish");
		putEntryCommon(htSuperCategory, "gobi", "fish");
		putEntryCommon(htSuperCategory, "topmouth gudgeon", "fish");
		putEntryCommon(htSuperCategory, "shorthead redhorse", "fish");
		putEntryCommon(htSuperCategory, "golden redhorse", "fish");
		putEntryCommon(htSuperCategory, "medaka, high-eyes", "fish");
		putEntryCommon(htSuperCategory, "brook silverside", "fish");
		putEntryCommon(htSuperCategory, "coho salmon", "fish");
		putEntryCommon(htSuperCategory, "lemon shark", "fish");
		putEntryCommon(htSuperCategory, "common carp", "fish");
		putEntryCommon(htSuperCategory, "three-spined stickleback", "fish");
		putEntryCommon(htSuperCategory, "fathead minnow", "fish");
		putEntryCommon(htSuperCategory, "american flagfish", "fish");
		putEntryCommon(htSuperCategory, "guppy", "fish");
		putEntryCommon(htSuperCategory, "mosquito fish", "fish");
		putEntryCommon(htSuperCategory, "rainbow trout", "fish");
		putEntryCommon(htSuperCategory, "sheepshead minnow", "fish");
		putEntryCommon(htSuperCategory, "carp", "fish");
		putEntryCommon(htSuperCategory, "medaka", "fish");
		putEntryCommon(htSuperCategory, "bluegill sunfish", "fish");
		putEntryCommon(htSuperCategory, "perch", "fish");
		putEntryCommon(htSuperCategory, "goldfish", "fish");
		putEntryCommon(htSuperCategory, "channel catfish", "fish");
		putEntryCommon(htSuperCategory, "spot", "fish");
		putEntryCommon(htSuperCategory, "banded tilapia", "fish");
		putEntryCommon(htSuperCategory, "juvenile chinese rare minnow", "fish");
		putEntryCommon(htSuperCategory, "marine medaka", "fish");
		putEntryCommon(htSuperCategory, "zebra fish", "fish");
		putEntryCommon(htSuperCategory, "blackrock fish", "fish");
		putEntryCommon(htSuperCategory, "crusian carp", "fish");
		putEntryCommon(htSuperCategory, "danio rerio", "fish");
		putEntryCommon(htSuperCategory, "salmo gairdneri", "fish");
		putEntryCommon(htSuperCategory, "smelt (small)", "fish");
		putEntryCommon(htSuperCategory, "smelt (large)", "fish");
		putEntryCommon(htSuperCategory, "salmonid", "fish");
		putEntryCommon(htSuperCategory, "pacific staghorn sculpin", "fish");
		putEntryCommon(htSuperCategory, "spotted sea trout", "fish");
		putEntryCommon(htSuperCategory, "stonecat", "fish");
		putEntryCommon(htSuperCategory, "brook silversides", "fish");
		putEntryCommon(htSuperCategory, "troutperch", "fish");
		putEntryCommon(htSuperCategory, "young of the year", "fish");
		
		putEntryLatin(htSuperCategory, "pseudohemiculter dispar", "fish");
		putEntryLatin(htSuperCategory, "mugil cephalus", "fish");
		putEntryLatin(htSuperCategory, "channa asiatica", "fish");
		putEntryLatin(htSuperCategory, "elops saurus", "fish");
		putEntryLatin(htSuperCategory, "ambassis miops", "fish");
		putEntryLatin(htSuperCategory, "clupea harengus membras", "fish");
		putEntryLatin(htSuperCategory, "sprattus sprattus", "fish");
		putEntryLatin(htSuperCategory, "pelophylax nigromaculatus", "amphibians");
		putEntryLatin(htSuperCategory, "ruditapes philippinarum", "molluscs");
		putEntryLatin(htSuperCategory, "ameiurus", "fish");
		putEntryLatin(htSuperCategory, "macrobrachium nipponense", "omit");
		putEntryLatin(htSuperCategory, "unionidae", "molluscs");
		putEntryLatin(htSuperCategory, "reganisalanx brachyrostralis", "fish");
		putEntryLatin(htSuperCategory, "carassius cuvieri", "fish");
		putEntryLatin(htSuperCategory, "coilia mystus", "fish");
		putEntryLatin(htSuperCategory, "culter mongolicus", "fish");
		putEntryLatin(htSuperCategory, "misgurnus anguillicaudatus", "fish");
		putEntryLatin(htSuperCategory, "rhodeus sinensis gunther", "fish");
		putEntryLatin(htSuperCategory, "ctenogobius giurinus", "fish");
		putEntryLatin(htSuperCategory, "sprattus sprattus", "fish");
		putEntryLatin(htSuperCategory, "lepomis gibbosus", "fish");
		putEntryLatin(htSuperCategory, "acanthogobius hasta, hexagrammos otakii, tridentiger trigonocephalus, carassius carassius, amblyeleotris diagonalis, tridentiger trigonocephalus, acanthogobius flavimanus, takifugu niphobles", "fish");
		putEntryLatin(htSuperCategory, "anadara granosa, ostreidae, ruditapes philippinarum, unionoida, solen strictus", "molluscs");
		putEntryLatin(htSuperCategory, "grapsidae, neocaridina heteropoda, paguridae, ocypodidae, petrolisthes cinctipes", "omit");
		putEntryLatin(htSuperCategory, "columbellidae, littorina brevicula, monodonta labio, semisulcospira libertina", "omit");
		putEntryLatin(htSuperCategory, "palaemon paucidens, alpheidae", "omit");
		putEntryLatin(htSuperCategory, "acanthogobius flavimanus", "fish");
		putEntryLatin(htSuperCategory, "canthogobius hasta", "fish");
		putEntryLatin(htSuperCategory, "sopoda, hemiptera, amphipoda, nematoda", "omit");
		putEntryLatin(htSuperCategory, "amphipoda", "omit");
		putEntryLatin(htSuperCategory, "ephemeroptera, trichoptera, odonata, hemiptera, isopoda, amphipoda", "omit");
		putEntryLatin(htSuperCategory, "leuciscus cephalus", "fish");
		putEntryLatin(htSuperCategory, "mesozooplankton", "microorganisms");
		putEntryLatin(htSuperCategory, "siniperca scherzeri", "fish");
		putEntryLatin(htSuperCategory, "channa striata", "fish");
		putEntryLatin(htSuperCategory, "eleotris fusca", "fish");
		putEntryLatin(htSuperCategory, "varuna litterata", "omit");
		putEntryLatin(htSuperCategory, "macrobrachium rosenbergii", "omit");
		putEntryLatin(htSuperCategory, "pomacea canaliculata", "omit");
		putEntryLatin(htSuperCategory, "corbicula fluminea", "molluscs");
		putEntryLatin(htSuperCategory, "mytilus edulis, crassostrea gigas", "molluscs");
		putEntryLatin(htSuperCategory, "erythroculter erythropterus", "fish");
		putEntryLatin(htSuperCategory, "erythroculter dabryi", "fish");
		putEntryLatin(htSuperCategory, "copepoda", "omit");
		putEntryLatin(htSuperCategory, "mysidacea", "omit");
		putEntryLatin(htSuperCategory, "cirrhinus molitorella", "fish");
		putEntryLatin(htSuperCategory, "clarias fuscus", "fish");
		putEntryLatin(htSuperCategory, "isopoda, hemiptera, amphipoda, nematoda", "omit");
		putEntryLatin(htSuperCategory, "periphyton", "omit");
		putEntryLatin(htSuperCategory, "gammarus", "omit");
		putEntryLatin(htSuperCategory, "engraulis encrasicolus", "fish");
		putEntryLatin(htSuperCategory, "argyrosomus regius", "fish");
		putEntryLatin(htSuperCategory, "dicentrarchus labrax", "fish");
		putEntryLatin(htSuperCategory, "dicentrarchus punctatus", "fish");
		putEntryLatin(htSuperCategory, "littorina brevicula", "omit");
		putEntryLatin(htSuperCategory, "mytilus edulis", "molluscs");
		putEntryLatin(htSuperCategory, "neritidae", "omit");
		putEntryLatin(htSuperCategory, "sebastes schlegeli", "fish");
		putEntryLatin(htSuperCategory, "neosalanx tangkahkeii taihuensis", "fish");
		putEntryLatin(htSuperCategory, "silurus glanis", "fish");
		putEntryLatin(htSuperCategory, "acanthogobius hasta", "fish");	
		putEntryLatin(htSuperCategory, "squalius laietanus", "fish");
		putEntryLatin(htSuperCategory, "lumbriculus variegatus", "worms");
		putEntryLatin(htSuperCategory, "hydrocharis dubia (blume) backer", "flowers, trees, shrubs, ferns");
		putEntryLatin(htSuperCategory, "ceratophyllum demersum l.", "flowers, trees, shrubs, ferns");
		putEntryLatin(htSuperCategory, "salvinia natans (l.) all.", "flowers, trees, shrubs, ferns");
		putEntryLatin(htSuperCategory, "megalobrama amblycephala", "fish");
		putEntryLatin(htSuperCategory, "pseudorasbora parva", "fish");
		putEntryLatin(htSuperCategory, "palinuridae", "omit");
		putEntryLatin(htSuperCategory, "pelodiscus sinensis", "reptiles");
		putEntryLatin(htSuperCategory, "limnocalanus macrurus, drepanopus bungei", "omit");
		putEntryLatin(htSuperCategory, "mastacembelus armatus", "fish");
		putEntryLatin(htSuperCategory, "alligator sinensis", "reptiles");
		putEntryLatin(htSuperCategory, "potamogeton crispus l.", "flowers, trees, shrubs, ferns");
		putEntryLatin(htSuperCategory, "ceratophyllum demersum l.", "flowers, trees, shrubs, ferns");
		putEntryLatin(htSuperCategory, "ulothrix", "algae");
		putEntryLatin(htSuperCategory, "vallisneria natans (lour.) hara", "flowers, trees, shrubs, ferns");
		putEntryLatin(htSuperCategory, "juncellus serotinus", "flowers, trees, shrubs, ferns");
		putEntryLatin(htSuperCategory, "typha angustifolia l.", "flowers, trees, shrubs, ferns");
		putEntryLatin(htSuperCategory, "phragmites australis", "flowers, trees, shrubs, ferns");
		putEntryLatin(htSuperCategory, "zizania latifolia", "flowers, trees, shrubs, ferns");
		putEntryLatin(htSuperCategory, "nelumbo nucifera gaertn.", "flowers, trees, shrubs, ferns");
		putEntryLatin(htSuperCategory, "scirpus validus vahl ", "flowers, trees, shrubs, ferns");
		putEntryLatin(htSuperCategory, "hemiculter leucisculus", "fish");
		putEntryLatin(htSuperCategory, "megalobrama terminalis", "fish");
		putEntryLatin(htSuperCategory, "acanthogobius hasta", "fish");
		putEntryLatin(htSuperCategory, "liza dussumieri", "fish");
		putEntryLatin(htSuperCategory, "silurus asotus", "fish");
		putEntryLatin(htSuperCategory, "cipangopaludina chinensis", "omit");
		putEntryLatin(htSuperCategory, "konosirus punctatus", "fish");
		putEntryLatin(htSuperCategory, "ablennes hians", "fish");
		putEntryLatin(htSuperCategory, "nibea coibor", "fish");
		putEntryLatin(htSuperCategory, "scaradon punctatus", "fish");
		putEntryLatin(htSuperCategory, "fugu rubripes", "fish");
		putEntryLatin(htSuperCategory, "pleurogrammus monopterygius", "fish");
		putEntryLatin(htSuperCategory, "sebastodes fuscescens", "fish");
		putEntryLatin(htSuperCategory, "tridentiger barbatus", "fish");
		putEntryLatin(htSuperCategory, "lepidotrigla microptera günther", "fish");
		putEntryLatin(htSuperCategory, "cynoglossus robustus", "fish");
		putEntryLatin(htSuperCategory, "lophius litulon", "fish");
		putEntryLatin(htSuperCategory, "platycephalus indicus", "fish");
		putEntryLatin(htSuperCategory, "platichthys bicoloratus", "fish");
		putEntryLatin(htSuperCategory, "zoarces elongatus", "fish");
		putEntryLatin(htSuperCategory, "odontamblyopus rubicundus", "fish");
		putEntryLatin(htSuperCategory, "portunus trituberculatus", "omit");
		putEntryLatin(htSuperCategory, "eucrata crenata de haan", "omit");
		putEntryLatin(htSuperCategory, "oratosquilla oratoria", "omit");
		putEntryLatin(htSuperCategory, "metapenaeus ensis de haan", "omit");
		putEntryLatin(htSuperCategory, "loligo chinensis", "omit");
		putEntryLatin(htSuperCategory, "octopus vulgaris", "omit");
		putEntryLatin(htSuperCategory, "scapharca subcrenata", "molluscs");
		putEntryLatin(htSuperCategory, "sinonovacula constricta", "molluscs");
		putEntryLatin(htSuperCategory, "erythroculter erythropterus", "fish");
		putEntryLatin(htSuperCategory, "rapana bezona linnaeus", "molluscs");
		putEntryLatin(htSuperCategory, "urechis unicinctus", "worms");
		putEntryLatin(htSuperCategory, "potamogeton", "flowers, trees, shrubs, ferns");
		putEntryLatin(htSuperCategory, "callitriche", "flowers, trees, shrubs, ferns");
		putEntryLatin(htSuperCategory, "bithynia tentaculata", "omit");
		putEntryLatin(htSuperCategory, "viviparus", "omit");
		putEntryLatin(htSuperCategory, "ceratophyllum demersum", "flowers, trees, shrubs, ferns");
		putEntryLatin(htSuperCategory, "salvinia natans", "flowers, trees, shrubs, ferns");
		putEntryLatin(htSuperCategory, "nelumbo nucifera", "flowers, trees, shrubs, ferns");
		putEntryLatin(htSuperCategory, "eichhornia crassipes", "flowers, trees, shrubs, ferns");
		putEntryLatin(htSuperCategory, "micropterus salmoides, barbus graellsii, cyprinus carpio", "fish");
		putEntryLatin(htSuperCategory, "ambassis natalensis", "fish");
		putEntryLatin(htSuperCategory, "rhabdosargus holubi", "fish");
		putEntryLatin(htSuperCategory, "holothuria tubulosa", "omit");
		putEntryLatin(htSuperCategory, "phytoplankton, zooplankton", "microorganisms");
		putEntryLatin(htSuperCategory, "neogobius melanostomus", "fish");
		putEntryLatin(htSuperCategory, "perca fluviatilis", "fish");
		putEntryLatin(htSuperCategory, "lepomis megalotis, micropterus salmoides, gambusia, poecilia, cypriniformes, cyprinus carpio, siluriformes, decapoda", "omit");
		putEntryLatin(htSuperCategory, "salvelinus alpinus", "fish");
		putEntryLatin(htSuperCategory, "hemibarbus labeo", "fish");
		putEntryLatin(htSuperCategory, "mysis relicta", "omit");
		putEntryLatin(htSuperCategory, "diporeia hoyi", "omit");
		putEntryLatin(htSuperCategory, "clibanarius infraspinatus", "omit");
		putEntryLatin(htSuperCategory, "seriola quinqueradiata", "fish");
		putEntryLatin(htSuperCategory, "sebastes schlegelii", "fish");
		putEntryLatin(htSuperCategory, "oplegnathus fasciatus", "fish");
		putEntryLatin(htSuperCategory, "trachemys scripta elegans, chinemys reevesii", "reptiles");
		putEntryLatin(htSuperCategory, "lepidopus caudatus", "fish");
		putEntryLatin(htSuperCategory, "micropogonias furnieri", "fish");
		putEntryLatin(htSuperCategory, "perna perna", "molluscs");
		putEntryLatin(htSuperCategory, "micropterus dolomieu, micropterus salmoides", "fish");
		putEntryLatin(htSuperCategory, "esox niger", "fish");
		putEntryLatin(htSuperCategory, "micropogonias furnieri", "fish");
		putEntryLatin(htSuperCategory, "lateolabrax japonicus", "fish");
		putEntryLatin(htSuperCategory, "conger myriaster", "fish");
		putEntryLatin(htSuperCategory, "sebastiscus marmoratus", "fish");
		putEntryLatin(htSuperCategory, "sebastes inermis", "fish");
		putEntryLatin(htSuperCategory, "acanthopagrus schlegeli", "fish");
		putEntryLatin(htSuperCategory, "trachurus japonicus", "fish");
		putEntryLatin(htSuperCategory, "argyrosomus argentatus", "fish");
		putEntryLatin(htSuperCategory, "paralichthys olivaceus", "fish");
		putEntryLatin(htSuperCategory, "decapoda", "omit");
		putEntryLatin(htSuperCategory, "micropterus salmoides", "fish");
		putEntryLatin(htSuperCategory, "microzooplankton", "microorganisms");
		putEntryLatin(htSuperCategory, "perna viridis", "molluscs");
		putEntryLatin(htSuperCategory, "channa argus", "fish");
		putEntryLatin(htSuperCategory, "gobio lozanoi", "fish");
		putEntryLatin(htSuperCategory, "pseudochondrostoma polylepis", "fish");
		putEntryLatin(htSuperCategory, "sprattus sprattus", "fish");
		putEntryLatin(htSuperCategory, "barbus guiraonis, micropterus salmoides, alburnus alburnus, anguilla anguilla, gobio lozanoi, pseudochondrostoma polylepis, esox lucius, lepomis gibbosus, salmo trutta", "fish");
		putEntryLatin(htSuperCategory, "mytilus edulis, crassostrea gigas", "molluscs");
		putEntryLatin(htSuperCategory, "elops saurus", "fish");
		putEntryLatin(htSuperCategory, "gastropoda", "molluscs");
		putEntryLatin(htSuperCategory, "capitellidae", "worms");
		putEntryLatin(htSuperCategory, "nereidae", "worms");
		putEntryLatin(htSuperCategory, "sabellidae", "worms");
		putEntryLatin(htSuperCategory, "penaeus monodon", "omit");
		putEntryLatin(htSuperCategory, "metapenaeus ensis", "omit");
		putEntryLatin(htSuperCategory, "carassius carassius", "fish");
		putEntryLatin(htSuperCategory, "barbus graellsii", "fish");
		putEntryLatin(htSuperCategory, "barbus guiraonis", "fish");
		putEntryLatin(htSuperCategory, "stuckenia pectinata", "flowers, trees, shrubs, ferns");
		
		

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



