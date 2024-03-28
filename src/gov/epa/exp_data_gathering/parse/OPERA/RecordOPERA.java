package gov.epa.exp_data_gathering.parse.OPERA;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.stream.Collectors;

import org.json.CDL;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import gov.epa.api.ExperimentalConstants;

import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;

/**
 * See OPERA articles:
 * Mansouri, 2018: https://doi.org/10.1186/s13321-018-0263-1
 * Mansouri, 2019: https://doi.org/10.1186/s13321-019-0384-1
 * 
 * See OPERA repo for zip file: https://github.com/NIEHS/OPERA/blob/master/OPERA_Data.zip
 * 
 * TODO: Need to get data for BP, HL, LogP, MP, pKA, VP, and WS
 * 
 * @author TMARTI02
 *
 */
public class RecordOPERA {
	


	public String ChemID;
	public String Original_SMILES;//Map to ExperimentalRecord.smiles
	public String dsstox_substance_id;//Map to ExperimentalRecord.dsstox_substance_id (need to add field)
	
	// **********************************************************************
	//May not need to map following to ExperimentalRecord:
	public String CAS;//Derived quantity from DSSTOX
	
	public String ChemicalName;//Derived quantity from DSSTOX
	public String IUPACName;
	public String PreferredName;
	
	public String dsstox_compound_id;//Derived quantity from DSSTOX
	public String Canonical_QSARr;//Derived quantity from DSSTOX
	public String InChI_Code_QSARr;//Derived quantity from DSSTOX
	public String InChI_Key_QSARr;//Derived quantity from DSSTOX
	// **********************************************************************
	
	public String Salt_Solvent;//used to account for the fact that solubility of salt was measured
	public String Salt_Solvent_ID;
	public String MPID;
	public String qc_level; // CR: this is going to be added to the notes of experimentalrecords
	
	// **********************************************************************
	// pka specific terms:
	public String MPID_a;
	public String MPID_b;
	public String pKa_a_ref;
	public String pKa_b_ref;
	public String pKa_a;
	public String pKa_b;
//	public String Substance_CASRN;
//	public String Substance_Name;
	public String Extenal_ID;
	public String DSSTox_Structure_Id;
	public String DSSTox_QC_Level;
	
	
	// the many forms of property value string
	public String property_name;
	public String LogHL;
	public String LogP;
	public String MP;
	public String LogVP;
	public String BP;
	public String LogMolar;
	
	public String LogOH;
	public String OH;
	public String LogBCF;
	public String LogHalfLife;
	public String LogKOC;
	public String LogKmHL;
	public String LogKOA;

	public String Ready_Biodeg;
	public String Clint;
	public String FU;
	
	//***************************************************************************************************************
	//Catmos related:

	public String CATMoS_logLD50_data;
	public String CATMoS_LD50_str;
	public String CATMoS_LD50_mgkg;
	public String CATMoS_GHS_data;
	public String CATMoS_EPA_data;
	public String CATMoS_NT_data;
	public String CATMoS_VT_data;

	public String CATMoS_LD50_pred;
	public String CATMoS_GHS_pred;
	public String CATMoS_EPA_pred;
	public String CATMoS_NT_pred;
	public String CATMoS_VT_pred;
	
	public String concordance_GHS;
	public String concordance_LD50;
	public String concordance_EPA;
	public String concordance_VT;
	public String concordance_NT;

	public String consensus_LD50;
	public String consensus_VT;
	public String consensus_NT;
	public String consensus_GHS;
	public String consensus_EPA;
	
	public String nbr_models_EPA;
	public String nbr_models_GHS;
	public String nbr_models_NT;
	public String nbr_models_VT;
	public String nbr_models_LD50;
	
	
	public String Conf_index_CATMoS;
	public String AD_CATMoS;
	public String AD_index_CATMoS;
	
	public String CATMoS_Tr_Tst;
	public String N_LD50;
	

	//***********************************************************
	//CERAPP fields
	
	public String Potency_binding;
	public String concordance_binding;
	public String consensus_binding;

	
	public String SOURCE;
	public String CERAPP_ID;
//	public String Original_SMILES_all;

	public String literature_sources_binding;
	public String Observed_class_binding;
	public String nbr_yes_binding;
	public String nbr_no_binding;
	public String yes_score_binding;
	public String no_score_binding;
	public String active_concordance_binding;
	public String inactive_concordance_binding;

	public String Potency_class_binding;
	public String Potency_class_antagonist;
	public String Potency_class_agonist;	

	public String yes_score_agonist;
	public String no_score_agonist;
	public String nbr_yes_agonist;
	public String nbr_no_agonist;
	public String consensus_agonist;
	public String active_concordance_agonist;
	public String inactive_concordance_agonist;
	public String Potency_agonist;
	public String literature_sources_agonist;
	public String Observed_class_agonist;
	public String Observed_POTENCY_LEVEL_agonist;
	public String concordance_agonist;
	
	public String yes_score_antagonist;
	public String no_score_antagonist;
	public String nbr_yes_antagonist;
	public String nbr_no_antagonist;
	public String consensus_antagonist;
	public String active_concordance_antagonist;
	public String inactive_concordance_antagonist;
	public String Potency_antagonist;
	public String concordance_antagonist;
	public String literature_sources_antagonist;
	public String Observed_class_antagonist;
	public String Observed_POTENCY_LEVEL_antagonist;
	public String Observed_POTENCY_LEVEL_binding;
	
	//COMPARA fields
	//***********************************************************
	public String CoMPARA_ID;
	public String iupac;
	public String cid;
	public String gsid;
	
	public String BD_class_Exp_binary;
	public String BD_class_Exp;
	public String BD_potency_Exp;
	public String nbr_models_binding;
	public String AG_Potency_Exp;
	public String AG_class_Exp_binary;
	public String AG_class_Exp;
	public String nbr_models_agonist;
	public String AN_class_Exp_binary;
	public String AN_class_Exp;
	public String AN_potency_Exp;
	public String nbr_models_antagonist;
	public String Salt_Solvent_all;
	

//	***********************************************************

	public Double property_value_original;//sometimes it will take some work to figure this out by comparing to data values from other sources
	public String property_value_units_original;//sometimes it will take some work to figure this out by comparing to data values from other sources
	public String Temperature;//Map to ExperimentalRecord.temperature_C
	public String Reference;//Map to ExperimentalRecord.reference (might need to add a field), since should put PHYSPROP for original_source_name
	
	public String Tr_1_Tst_0;//tells you whether compound appears in their training or test sets

	public String file_name;

	

	public String toJSON() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();// makes it multiline and readable
		Gson gson = builder.create();
		return gson.toJson(this);//all in one line!
	}

	
	private static Vector<RecordOPERA> parseOPERA_SDF(String endpoint,String sourceName, String filename) {

		
		Vector<RecordOPERA> records=new Vector<>();
		
		String folder="data\\experimental\\"+sourceName+"\\OPERA_SDFS\\";
		
		if (filename==null) {
			System.out.println("filename missing for "+endpoint);
			return null;
		}
		

		AtomContainerSet acs=LoadFromSDF(folder+filename);

//		System.out.println(acs.getAtomContainerCount());
		
		for (int i=0;i<acs.getAtomContainerCount();i++) {
			AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
			RecordOPERA ro = createRecordOpera(ac,endpoint);
			ro.file_name=filename;
			records.add(ro);
			
//			if (ro.property_value_original!=null)
//			System.out.println(ro.property_name+"\t"+ro.dsstox_substance_id+"\t"+  ro.property_value_original);
			
			//Print out:
			// if (ro.Substance_CASRN.contentEquals("71-43-2")) System.out.println(ro.toJSON());
			// if (ro.CAS.contentEquals("71-43-2")) System.out.println(ro.toJSON());
//			if (ro.CAS != null) { // only added because pka printing for benzene doesn't work
//				if (ro.CAS.contentEquals("71-43-2")) System.out.println(ac.getProperties());
//			}
		}
		return records;
	}
	
	

	/**
	 * Gets the units for the property values in sdf files
	 * 
	 * @param propertyName
	 * @return
	 */
	private static String getOriginalUnits(String propertyName) {
		
		switch (propertyName) {
		
			case ExperimentalConstants.strWaterSolubility:
				return ExperimentalConstants.str_log_M;//Note: later to get M need to use Math.pow(10,value)
			case ExperimentalConstants.strHenrysLawConstant:
				return ExperimentalConstants.str_log_atm_m3_mol;//TODO- determine what "?" is
			case ExperimentalConstants.strVaporPressure:
				return ExperimentalConstants.str_log_mmHg;
			case ExperimentalConstants.strCLINT:
				return ExperimentalConstants.str_LOG_UL_MIN_1MM_CELLS;
			case ExperimentalConstants.strFUB:
				return ExperimentalConstants.str_dimensionless;
			case ExperimentalConstants.strRBIODEG:
				return ExperimentalConstants.str_binary;
			case ExperimentalConstants.strORAL_RAT_LD50:
				return ExperimentalConstants.str_mg_kg;
			case ExperimentalConstants.strOH:
				return ExperimentalConstants.str_LOG_CM3_MOLECULE_SEC;

			case ExperimentalConstants.strBCF:
			case ExperimentalConstants.strKOC:
				return ExperimentalConstants.str_LOG_L_KG;

			case ExperimentalConstants.str_pKA:
			case ExperimentalConstants.strLogKOW:
			case ExperimentalConstants.strLogKOA:
				return ExperimentalConstants.str_LOG_UNITS;

			case ExperimentalConstants.strCACO2:
				return ExperimentalConstants.str_LOG_CM_SEC;
			case ExperimentalConstants.strBIODEG_HL_HC:
			case ExperimentalConstants.strKmHL:
				return ExperimentalConstants.str_LOG_DAYS;
				
			case ExperimentalConstants.strMeltingPoint:
			case ExperimentalConstants.strBoilingPoint:
				return ExperimentalConstants.str_C;
			case ExperimentalConstants.strER:
			case ExperimentalConstants.strAR:
			case ExperimentalConstants.str_ESTROGEN_RECEPTOR_AGONIST:
			case ExperimentalConstants.str_ESTROGEN_RECEPTOR_ANTAGONIST:
			case ExperimentalConstants.str_ESTROGEN_RECEPTOR_BINDING:
				return ExperimentalConstants.str_binary;


			default:
				System.out.println("need to add entry for "+propertyName+" in RecordOpera.getOriginalUnits()");
				return null;
		}
		
			
	}
	
	

	/**
	 * Converts atom container property data into RecordOPERA object
	 * 
	 * @param ac
	 * @return
	 */
	private static RecordOPERA createRecordOpera(AtomContainer ac,String property_name) {
		RecordOPERA ro=new RecordOPERA();
		ro.property_name=property_name;

		Map<Object,Object>props=ac.getProperties();
		for (Map.Entry<Object,Object> entry : props.entrySet()) {  

			String key=(String)entry.getKey();
			
			if (key.contains("cdk")) continue;

			String value=(String)entry.getValue();

			if(value==null) continue;
			
			value=value.trim();
			
			if(value.isEmpty() || value.equals("NA") || value.equals("NaN") || value.equals("?")) continue;
			
			if(key.toLowerCase().contains("cas")) {
				key="CAS";
			}

			if(key.toLowerCase().equals("preferred_name") || key.toLowerCase().equals("substance_name") ||  key.toLowerCase().equals("name") || key.equals("CHEMICAL NAME")) {
				key="ChemicalName";
			} else if(key.toLowerCase().contains("name")) {
				System.out.println(property_name+"\t"+key+"\tDiff name");
				key="ChemicalName";
			}
			
			if(key.equals("Original_SMILES_all")) {
				key="Original_SMILES";
			}
			
			if(key.toLowerCase().equals("dsstox_substance_id") || key.contentEquals("DTXSID")) key="dsstox_substance_id";
			
//			System.out.println(key);
			if (key.contains(" ")) {
//				System.out.println("space in key\t"+key);
				if (key.contains("Reference") || key.contains("Temperature")) {
					key=key.substring(key.indexOf(" ")+1,key.length());
				}			
			}			
			key=key.replace("KocRef","Reference").replace("LogKOA_Ref", "Reference");
			
			
//			System.out.println(key);

			
//			if(key.contains("Temperature")) {
//				System.out.println(key+"\t"+value);
//			}
			
			try {
				//Use reflection to assign values from key/value pair:
				Field myField = ro.getClass().getDeclaredField(key.replace(" ", "_").replace("-", "_"));
				//System.out.println(key+"\t"+value);
				myField.set(ro, value);
			} catch (Exception e) {
				//TODO if get error- add if statement above to account for it or in some cases add a new field to RecordOPERA class
				e.printStackTrace();
			}		            				
						
		}

		//Set property value and units:
		if (!property_name.equals(ExperimentalConstants.strORAL_RAT_LD50) && !property_name.equals(ExperimentalConstants.strAR) && !property_name.equals(ExperimentalConstants.strER) && !property_name.equals(ExperimentalConstants.str_pKA))
			ro.property_value_original=getPropertyValueOriginal(property_name,ro);
		
		ro.property_value_units_original=getOriginalUnits(property_name);

		if(ro.ChemicalName!=null && ro.ChemicalName.toLowerCase().indexOf("noname")==0) ro.ChemicalName=null;
		if(ro.CAS!=null && ro.CAS.toLowerCase().indexOf("nocas")==0) ro.CAS=null;

		
		return ro;
	}

	/**
	 * TODO - move to a utilities class 
	 * 
	 * @param filepath
	 * @return
	 */
	public static AtomContainerSet LoadFromSDF(String filepath) {

		AtomContainerSet acs=new AtomContainerSet();

		try {
			IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepath),DefaultChemObjectBuilder.getInstance());								

			while (mr.hasNext()) {

				AtomContainer m=null;					
				try {
					m = (AtomContainer)mr.next();
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}

				if (m==null || m.getAtomCount()==0) break;
				acs.addAtomContainer(m);

			}// end while true;

			mr.close();

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;

		}
		
		return acs;
	}

	
	
	
	// TODO: Need to get data for BP, HL, LogP, MP, pKA, VP, and WS
	/**
	 * this was the old main method, seems sensible to use it in the parseOPERA class in a manner similar to 'parseRecordsInDatabase' from other classes.
	 * @return
	 */
	public static Vector<RecordOPERA> parseOperaSdfs(String sourceName) {
		Vector<RecordOPERA> records = new Vector<>();

		TreeMap<String,String>ht=new TreeMap<>();//sorts by key

		ht.put(ExperimentalConstants.strHenrysLawConstant, "HL_QR.sdf");
		ht.put(ExperimentalConstants.strWaterSolubility, "WS_QR.sdf");
		ht.put(ExperimentalConstants.strVaporPressure, "VP_QR.sdf");
		ht.put(ExperimentalConstants.strMeltingPoint, "MP_QR.sdf");
		ht.put(ExperimentalConstants.strLogKOW, "LogP_QR.sdf");
		ht.put(ExperimentalConstants.strBoilingPoint, "BP_QR.sdf");

		ht.put(ExperimentalConstants.strLogKOA,"KOA_QR.sdf");
		ht.put(ExperimentalConstants.strBCF, "BCF_QR.sdf");
		ht.put(ExperimentalConstants.strKOC, "KOC_QR.sdf");
		ht.put(ExperimentalConstants.strKmHL, "KM_QR.sdf");
		ht.put(ExperimentalConstants.strBIODEG_HL_HC, "Biodeg_QR.sdf");
		ht.put(ExperimentalConstants.strOH, "AOH_QR.sdf");
		ht.put(ExperimentalConstants.str_pKA, "pKa_QR.sdf");
		ht.put(ExperimentalConstants.strRBIODEG,"RBiodeg_QR.sdf");
		ht.put(ExperimentalConstants.strORAL_RAT_LD50,"CATMoS_QR50k.sdf");
		
//		//These 2 look superseded by the csv: but code will use latest value from csv
		ht.put(ExperimentalConstants.strCLINT,"Clint_QR.sdf");
		ht.put(ExperimentalConstants.strFUB,"FU_QR.sdf");
		
		ht.put(ExperimentalConstants.strAR, "CoMPARA_QR.sdf");
////		ht.put(ExperimentalConstants.strER, "CERAPP_QR.sdf");//Just use csvs provided by Kamel; doesnt have SIDs and need to set suspicious to inactive

		//TODO add RT? (omit according to Tony)
//		ht.put(ExperimentalConstants.strInVitroToxicity,"CERAPP_QR.sdf");
		
        for(String property : ht.keySet()) {
			String filename=ht.get(property);
			System.out.println("Parsing "+filename+" for " + property);
        	records.addAll(parseOPERA_SDF(property,sourceName,ht.get(property)));
        }
		
		return records;
	}
	
	public static Vector<RecordOPERA> parseOperaCSVs(String sourceName) {
		Vector<RecordOPERA> records = new Vector<>();

		TreeMap<String,String>ht=new TreeMap<>();

		//Following came from our sources so dont need in our db:
//		ht.put(ExperimentalConstants.strHenrysLawConstant, "HL_2.9_update.csv");
//		ht.put(ExperimentalConstants.strWaterSolubility, "WS_2.9_update.csv");
//		ht.put(ExperimentalConstants.strVaporPressure, "VP_2.9_update.csv");
//		ht.put(ExperimentalConstants.strMeltingPoint, "MP_2.9_update.csv");
//		ht.put(ExperimentalConstants.strLogKOW, "LogP_2.9_update.csv");
//		ht.put(ExperimentalConstants.strBoilingPoint, "BP_2.9_update.csv");
		
		ht.put(ExperimentalConstants.strCACO2,"Caco2_QR.csv");//no matching sdf
		ht.put(ExperimentalConstants.strFUB,"FU_QR.csv");//supercedes sdf?
		ht.put(ExperimentalConstants.strCLINT,"Clint_QR.csv");//supercedes sdf?

		// for-each loop
		for (String property : ht.keySet()) {
			String filename=ht.get(property);
			System.out.println("Parsing "+filename+" for " + property);
			records.addAll(parseOPERA_CSV(property, sourceName, ht.get(property)));
		}

		return records;
	}
	
	
	public static Vector<RecordOPERA> parseOperaCSVs2(String sourceName,boolean skipIfMissingProperty) {

		Vector<RecordOPERA> records = new Vector<>();

		TreeMap<String,String>ht=new TreeMap<>();

		ht.put(ExperimentalConstants.str_ESTROGEN_RECEPTOR_AGONIST, "CERAPP_ag.csv");
		ht.put(ExperimentalConstants.str_ESTROGEN_RECEPTOR_ANTAGONIST, "CERAPP_anta.csv");
		ht.put(ExperimentalConstants.str_ESTROGEN_RECEPTOR_BINDING, "CERAPP_bind.csv");

		// for-each loop
		for (String property : ht.keySet()) {
			String filename=ht.get(property);
			System.out.println("Parsing "+filename+" for " + property);
			records.addAll(parseOPERA_CSV2(property, sourceName, filename,skipIfMissingProperty));
		}

		return records;
	}
	
	private static Vector<RecordOPERA>  parseOPERA_CSV2(String property, String sourceName,
			String filename,boolean skipIfMissingProperty) {

		Vector<RecordOPERA>records=new Vector<>();
		String folder = "data\\experimental\\" + sourceName + "\\OPERA_SDFS\\";
		
		try {
			
			
			InputStream inputStream= new FileInputStream(folder+filename);
			
			String csvAsString = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
			String json = CDL.toJSONArray(csvAsString).toString();
			inputStream.close();
			Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

			JsonArray ja=gson.fromJson(json, JsonArray.class);
			
			int count=0;
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				
				RecordOPERA ro=new RecordOPERA();
				ro.CERAPP_ID=jo.get("Var1").getAsString();
				ro.property_name=property;
				ro.file_name=filename;
				
				if (skipIfMissingProperty) {
					if(jo.get("Var4")==null || jo.get("Var4").getAsString().equals("NA")) continue;
				}
					
				if(property.equals(ExperimentalConstants.str_ESTROGEN_RECEPTOR_AGONIST)) {
					ro.Potency_class_agonist=jo.get("Var4").getAsString();
				} else if(property.equals(ExperimentalConstants.str_ESTROGEN_RECEPTOR_ANTAGONIST)) {
					ro.Potency_class_antagonist=jo.get("Var4").getAsString();
				} else if(property.equals(ExperimentalConstants.str_ESTROGEN_RECEPTOR_BINDING)) {
					ro.Potency_class_binding=jo.get("Var4").getAsString();
				}
				
				for (int j=1;j<=100;j++) {
					
					if(jo.get("Var2_"+j)==null) break;
					
					String dtxsid=jo.get("Var2_"+j).getAsString();
					
					dtxsid=dtxsid.trim();
					
					if(dtxsid.isEmpty() || dtxsid.equals("?")) break;
					
					count++;
					
					if(ro.dsstox_substance_id==null) {
						ro.dsstox_substance_id=dtxsid;
					} else {
						ro.dsstox_substance_id=ro.dsstox_substance_id+"|"+dtxsid;
					}
					
//					System.out.println(dtxsid);
				}
				
				for (int j=1;j<=100;j++) {
					if(jo.get("Var3_"+j)==null) break;

					String casrn=jo.get("Var3_"+j).getAsString();
					if(casrn.isEmpty()) break;
					
					if(ro.CAS==null) {
						ro.CAS=casrn;
					} else {
						ro.CAS=ro.CAS+"|"+casrn;
					}
//					System.out.println(dtxsid);
				}

				ro.property_value_units_original=getOriginalUnits(property);
				
				records.add(ro);

//				System.out.println(gson.toJson(jo));

//				if( ro.dsstox_substance_id==null || ro.dsstox_substance_id.isEmpty())
//					System.out.println(gson.toJson(ro)+"\n");
				
			} 
			
			System.out.println(count);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		
		return records;
	}


	private static Vector<RecordOPERA> parseOPERA_CSV(String property_name, String sourceName, String filename) {

		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		Vector<RecordOPERA> records = new Vector<>();

		String folder = "data\\experimental\\" + sourceName + "\\OPERA_SDFS\\";

		if (filename == null) {
			System.out.println("filename missing for " + property_name);
			return null;
		}


		try {
			InputStream inputStream= new FileInputStream(folder+filename);
			
			String csvAsString = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
			String json = CDL.toJSONArray(csvAsString).toString();
			inputStream.close();
			JsonArray ja=gson.fromJson(json, JsonArray.class);
			
			for (int i=0;i<ja.size();i++) {
				
				JsonObject jo=ja.get(i).getAsJsonObject();
				RecordOPERA ro=new RecordOPERA();
				
				ro.property_name=property_name;
				ro.file_name=filename;
				
				
				
				Set<Entry<String, JsonElement>> entrySet = jo.entrySet();
				
				for(Map.Entry<String,JsonElement> entry : entrySet){

					String key=entry.getKey();
//					System.out.println(entry.getKey());
					String value=jo.get(entry.getKey()).getAsString();
					
					value=value.trim();
							
					if (value.isBlank() || value.equals("?")) value=null;

					if (key.contains("[")) continue;
					String fieldName=getFieldName(key);
					
					
					try {
						//Use reflection to assign values from key/value pair:
						if (fieldName==null) continue;
						Field myField = ro.getClass().getDeclaredField(fieldName);
						//System.out.println(key+"\t"+value);
						
						if (fieldName.equals("property_value_original")) {
							myField.set(ro, Double.parseDouble(value));
						} else {
							myField.set(ro, value);	
						}
						
					} catch (Exception e) {
						//TODO if get error- add if statement above to account for it or in some cases add a new field to RecordOPERA class
						System.out.println("error setting\t"+key+"\t"+fieldName+"\t"+value+"\t"+e.getMessage());
//						e.printStackTrace();
					}	
				}			

//				ro.property_value_original=getPropertyValueOriginalCSV(property_name, jo);
				ro.property_value_units_original=RecordOPERA.getOriginalUnits(property_name);
				
				if(ro.PreferredName!=null) {
					ro.ChemicalName=ro.PreferredName;
				} else if (ro.IUPACName!=null) {
					ro.ChemicalName=ro.IUPACName;
				} else {
				}
				
				if(ro.ChemicalName!=null && ro.ChemicalName.toLowerCase().indexOf("noname")==0) ro.ChemicalName=null;
				if(ro.CAS!=null && ro.CAS.toLowerCase().indexOf("nocas")==0) ro.CAS=null;
								
//				if (ro.property_name.equals(ExperimentalConstants.strCACO2)) {
//					System.out.println(ro.dsstox_substance_id+"\t"+ro.ChemicalName+"\t"+ro.Original_SMILES+"\t"+ro.property_value_original);
//				}
				
//				System.out.println(gson.toJson(jo));
//				System.out.print("\n");
//				System.out.println(gson.toJson(ro));
//				System.out.println("**********************\n\n");
				records.add(ro);
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return records;

	}
	
	static String getFieldName(String key) {

		
		switch (key) {
		
		case "DSSTOX_SUBSTANCE_ID":
		case "DTXSID":
			return "dsstox_substance_id";
		
		case "DSSTOX_COMPOUND_ID":						
			return "dsstox_compound_id";
		
		case "IUPAC_Name":
			return "IUPACName";
		
		case "PREFERRED_NAME":
			return "PreferredName";
		
		case "QC_LEVEL": 
			return "qc_level";
		
		case "pub_source_name":
		case "Source":
			return "Reference";
		
		case "CASRN":
			return "CAS";
		
		case "QC_Level":
			return "qc_level";
			
		case "InChIKey_QSARr":
		case "InChI Key_QSARr":
		case "InChI_Key_QSARr":
			return "InChI_Key_QSARr";
		
		case "Salt_Solvent_ID":
		case "Salt_Solvent":
		case "Canonical_QSARr":
		case "InChI_Code_QSARr":
		case "Original_SMILES":
			return key;
		
		case "value_point_estimate":
		case "fup":
		case "CalculatedLogPapp":
		case "logCL":
			return "property_value_original";
		
		case "Number of connected components":
		case "unit":
		case "Class":
		case "log10_fup":
		case "tr_tst":
		case "Original_InChiKey":
		case "Original_InChi":
		case "outliers":
		case "PUBCHEM_CID":
		case "CHEMBL_ID":
		case "PappClass":
		case "Index":
			return null;
		
		default:
			System.out.println("need to handle "+key);
			return null;
		}
	}


	
	private static void getTemperatureCondition(ExperimentalRecord er, String propertyValue) {
		if ((propertyValue != null && propertyValue.length() > 0) && (!propertyValue.contains("|")))
			er.temperature_C = Double.parseDouble(propertyValue);
		else if ((propertyValue != null && propertyValue.length() > 0) && propertyValue.contains("|")) {
			int vertLineIndex = propertyValue.indexOf("|");
			String temp1 = propertyValue.substring(0,vertLineIndex);
			String temp2 = propertyValue.substring(vertLineIndex + 1,propertyValue.length());
			double temp1double = Double.parseDouble(temp1);
			double temp2double = Double.parseDouble(temp2);
			er.temperature_C = (temp1double + temp2double)/ 2;
		}
	}
	
	
	ExperimentalRecord toExperimentalRecord(String date,String sourceName, String propertyName) {

//		if(CAS!=null && CAS.contains("NOCAS")) CAS=null;
//		if(ChemicalName!=null && ChemicalName.contains("NoName")) ChemicalName=null;

		ExperimentalRecord er = new ExperimentalRecord();

		er.property_name=propertyName;
		er.source_name = sourceName;
		er.document_name = Reference;
		er.property_value_units_original = property_value_units_original;

		er.dsstox_substance_id = dsstox_substance_id;
		er.casrn=this.CAS;
		er.chemical_name=this.ChemicalName;
		er.smiles=this.Original_SMILES;
		er.file_name=file_name;

		getTemperatureCondition(er,Temperature);		
//		if(Temperature!=null) er.temperature_C=Double.parseDouble(Temperature);
		
		if(DSSTox_QC_Level!=null && !DSSTox_QC_Level.isEmpty())
			er.note = "qc_level= " + DSSTox_QC_Level;
		er.date_accessed = date;
		er.keep = true;
		
		if(Reference!=null) {
			
			if(Reference.contains(";<")) {
				er.property_value_numeric_qualifier="<";
				Reference=Reference.substring(0,Reference.indexOf(";<")).trim();
				er.updateNote("Value has qualifier according to reference");
			}
			
			Reference=Reference.replace("|?", "");
//			System.out.println(ro.Reference);
//			er.literatureSource=createLiteratureSource(ro.Reference);
			er.document_name=Reference;//just store as string to avoid complications in the db
		}

		
		return er;
		
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	private static Double getPropertyValueOriginal(String propertyName,RecordOPERA ro) {
		
		String fieldName;
		
		switch (propertyName) {

		case ExperimentalConstants.strWaterSolubility:
			fieldName="LogMolar";
			break;
		case ExperimentalConstants.strHenrysLawConstant:
			fieldName="LogHL";
			break;
		case ExperimentalConstants.strVaporPressure:
			fieldName="LogVP";
			break;
		case ExperimentalConstants.strCLINT:
			fieldName="Clint";
			break;
		case ExperimentalConstants.strFUB:
			fieldName="FU";
			break;
		case ExperimentalConstants.strMeltingPoint:
			fieldName="MP";
			break;
		case ExperimentalConstants.strBoilingPoint:
			fieldName="BP";
			break;
		case ExperimentalConstants.strRBIODEG:
			fieldName="Ready_Biodeg";
			break;
		case ExperimentalConstants.strOH:
			fieldName= "LogOH";
			break;
		case ExperimentalConstants.strBCF:
			fieldName="LogBCF";
			break;
		case ExperimentalConstants.strBIODEG_HL_HC:
			fieldName="LogHalfLife";
			break;
		case ExperimentalConstants.strLogKOW:
			fieldName="LogP";
			break;
		case ExperimentalConstants.strLogKOA:
			fieldName="LogKOA";
			break;
		case ExperimentalConstants.strKmHL:
			fieldName="LogKmHL";
			break;
		case ExperimentalConstants.strKOC:
			fieldName="LogKOC";
			break;
		default:
			//TODO add CERAPP, COMPARA, RT (omit according to Tony)
			System.out.println("need to add entry for "+propertyName+" in RecordOpera.getPropertyValueOriginal");
			return null;
		}
		
		try {
			
			Field field = ro.getClass().getDeclaredField(fieldName);
			String fieldValue=(String)field.get(ro);
//			System.out.println(fieldName+"\t"+fieldValue+"\t"+ro.LogKOA);
 			
			if (fieldValue!=null && fieldValue.isBlank()) return null;
			return  Double.parseDouble(fieldValue);
			
		} catch (Exception e) {
			System.out.println("Cant retrieve "+fieldName+" for "+propertyName);
			return null;
		}
		
		
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
//	private static Double getPropertyValueOriginalCSV(String propertyName,JsonObject jo) {
//		
//		String fieldName;
//		
//		switch (propertyName) {
//
//		case ExperimentalConstants.strFUB:
//			fieldName="fup";
//			break;
//		case ExperimentalConstants.strCLINT:
//			fieldName="logCL";
//			break;
//		case ExperimentalConstants.strCACO2:
//			fieldName="CalculatedLogPapp";
//			break;
//		case ExperimentalConstants.strVaporPressure:
//		case ExperimentalConstants.strBoilingPoint:
//		case ExperimentalConstants.strHenrysLawConstant:
//		case ExperimentalConstants.strWaterSolubility:
//		case ExperimentalConstants.strMeltingPoint:
//		case ExperimentalConstants.strLogKOW:
//			fieldName="value_point_estimate";
//			break;
//		default:
//			//TODO add CERAPP, COMPARA, RT (omit according to Tony)
//			System.out.println("need to add entry for "+propertyName+" in RecordOpera.getPropertyValueOriginalCSV");
//			return null;
//		}
//		
//		try {
//			return  Double.parseDouble(jo.get(fieldName).getAsString());
//		} catch (Exception e) {
//			System.out.println("Cant retrieve "+fieldName+" for "+propertyName);
//			return null;
//		}
//		
//	}
	
	

	private Vector<RecordOPERA> splitRecords(Vector<RecordOPERA> records) {

		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		Vector<RecordOPERA> records2=new Vector<>();
		
		
		for (int j=0;j<records.size();j++) {
			
			RecordOPERA record=records.get(j);
			
//			System.out.println(record.ChemicalName);
//			System.out.println(record.Original_SMILES);
//			System.out.println(record.CAS);

			String [] casrns=record.CAS.split("\\|");

			String [] names=null;
			if (record.ChemicalName!=null) {
				names=record.ChemicalName.split("\\|");	
			}

				
//			System.out.println(casrns.length+"\t"+names.length+"\t"+smiles.length);
			
			List<String> smiles=getSmilesArray(record);
			
			if (casrns.length!=smiles.size()) {
				System.out.println(casrns.length+"\t"+record.CAS);
				System.out.println(smiles.size()+"\t"+record.Original_SMILES+"\n");
			}

			
			for (int i=0;i<casrns.length;i++)	{			
				//		System.out.println(dtxsid);
				
				RecordOPERA erClone=gson.fromJson(gson.toJson(record), RecordOPERA.class);
				
				erClone.CAS=casrns[i].trim();

				if(names!=null) {					
					if(names.length>1)
						erClone.ChemicalName=names[i].trim();
					else if (names.length==1) {
						erClone.ChemicalName=record.ChemicalName;
					}
				}
				
				
				if(smiles.size()>0) {					
					if(smiles.size()>1)
						erClone.Original_SMILES=smiles.get(i).trim();
					else if (smiles.size()==1) {
						erClone.Original_SMILES=record.Original_SMILES;
					}
				}

				if(erClone.CAS.contains("?")) {
					continue;//discard since we already have a different record with a CAS
				}

				records2.add(erClone);
				erClone.CERAPP_ID=erClone.CERAPP_ID+"_"+(i+1);
			}

		} 
		
		return records2;

	}


	private List<String> getSmilesArray(RecordOPERA record) {
		
		List<String>smiles=new ArrayList<>();

		boolean print=false;

		
		if(record.Original_SMILES!=null) {
			String [] array=record.Original_SMILES.split("\\|");
			for (String smile:array) smiles.add(smile);
			
			
			for (int i=1;i<smiles.size();i++) {
				String smilePrev=smiles.get(i-1);
				String smile=smiles.get(i);
				
				if(smile.contains(",") || smile.contains("&") || smile.contains(":")) {
					print =true;
//						System.out.println("|"+smile+"|");
					smiles.set(i-1,smilePrev+"|"+smile+"|");
					smiles.remove(i);
					i--;
//						System.out.println(smiles.get(i-1));
				}
			}
			
		}
		
		List<String>smiles2=new ArrayList<>();
		
		for (String smile:smiles) {
			if(!smile.isEmpty())smiles2.add(smile);
		}
		
//		if (print) {
//			for (int i=0;i<smiles2.size();i++) {
//				System.out.println(i+"\t"+smiles2.get(i));
//			}
//		}

		
		return smiles2;
	}
	
	private Vector<RecordOPERA> splitRecords2(Vector<RecordOPERA> records) {

		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		Vector<RecordOPERA> records2=new Vector<>();
		
		
		for (int j=0;j<records.size();j++) {
			
			RecordOPERA record=records.get(j);
//			System.out.println(record.ChemicalName);
//			System.out.println(record.Original_SMILES);
//			System.out.println(record.CAS);

			if (record.dsstox_substance_id!=null ) {

				String [] dtxsids=record.dsstox_substance_id.split("\\|");
				String [] casrns=record.CAS.split("\\|");

				
				if(casrns.length!=dtxsids.length) {
					System.out.println(record.CERAPP_ID+"\t"+casrns.length+"\t"+dtxsids.length);
				}
				
//				System.out.println(casrns.length+"\t"+names.length+"\t"+smiles.length);
				
				for (int i=0;i<dtxsids.length;i++)	{			
					//		System.out.println(dtxsid);
					
					RecordOPERA erClone=gson.fromJson(gson.toJson(record), RecordOPERA.class);
					erClone.dsstox_substance_id=dtxsids[i];
					erClone.CAS=casrns[i].trim();
					records2.add(erClone);
					erClone.CERAPP_ID=erClone.CERAPP_ID+"_"+(i+1);
				}
			} else {
				String [] casrns=record.CAS.split("\\|");

//				System.out.println(casrns.length+"\t"+names.length+"\t"+smiles.length);
				
				for (int i=0;i<casrns.length;i++)	{			
					//		System.out.println(dtxsid);
					
					RecordOPERA erClone=gson.fromJson(gson.toJson(record), RecordOPERA.class);
					erClone.CAS=casrns[i].trim();
					records2.add(erClone);
					erClone.CERAPP_ID=erClone.CERAPP_ID+"_"+(i+1);
				}
				
			}
			
			

		} 
		
		return records2;

	}
			

	
	void makeCERAPPChemRegList() {
		Vector<RecordOPERA>recs=parseOPERA_SDF(ExperimentalConstants.strER,ExperimentalConstants.strSourceOPERA29,"CERAPP_QR.sdf");
//		System.out.println(recs.size());
		recs=splitRecords(recs);
//		System.out.println(recs.size());
		
//		System.out.println(gson.toJson(recs));
		
		try {
			FileWriter fw=new FileWriter("data\\experimental\\OPERA2.9\\OPERA_SDFS\\cerapp chemreg import.txt");
			fw.write("EXTERNAL_ID\tSource_CAS\tSource_ChemicalName\tSourceSmiles\r\n");
			for (RecordOPERA rec:recs) {
				String line=rec.CERAPP_ID+"\t"+rec.CAS+"\t"+rec.ChemicalName+"\t"+rec.Original_SMILES;
				line=line.replace("\n","");
				fw.write(line+"\r\n");
			}
			fw.flush();
			fw.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	void getKamelSIDs() {
		Vector<RecordOPERA>records=parseOperaCSVs2("OPERA2.9",false);
		
		records=splitRecords2(records);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		TreeMap<String,RecordOPERA>ht=new TreeMap<>();
		
		for (RecordOPERA record:records) {
//			System.out.println(gson.toJson(record));
			ht.put(record.CERAPP_ID, record);
		}
		
		for (String id:ht.keySet()) {
			RecordOPERA ro=ht.get(id);
			System.out.println(id+"\t"+ro.dsstox_substance_id+"\t"+ro.CAS);
		}
		
	}
	
	
	/**
	 * exists only to 
	 * @param args
	 */
	public static void main(String[] args) {
//		parseOPERA_SDF(ExperimentalConstants.strWaterSolubility,"WS_QR.sdf");
		RecordOPERA r=new RecordOPERA();
		
//		r.parseOperaSdfs(ExperimentalConstants.strSourceOPERA29);
//		r.parseOperaCSVs2(ExperimentalConstants.strSourceOPERA29);
//		r.makeCERAPPChemRegList();
		r.getKamelSIDs();

		
	}
}
