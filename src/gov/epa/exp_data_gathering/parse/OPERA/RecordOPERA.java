package gov.epa.exp_data_gathering.parse.OPERA;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;

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
	public String Original_SMILES_all;

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
	
	
//	***********************************************************

	public Double property_value_original;//sometimes it will take some work to figure this out by comparing to data values from other sources
	public String property_value_units_original;//sometimes it will take some work to figure this out by comparing to data values from other sources
	public String Temperature;//Map to ExperimentalRecord.temperature_C
	public String Reference;//Map to ExperimentalRecord.reference (might need to add a field), since should put PHYSPROP for original_source_name
	
	public String Tr_1_Tst_0;//tells you whether compound appears in their training or test sets

	

	

	public String toJSON() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();// makes it multiline and readable
		Gson gson = builder.create();
		return gson.toJson(this);//all in one line!
	}

	Vector<RecordOPERA> parseOPERA_SDF(String endpoint,String filename) {

		
		Vector<RecordOPERA> records=new Vector<>();
		
		String folder="data\\experimental\\OPERA\\OPERA_SDFS\\";
		
		if (filename==null) {
			System.out.println("filename missing for "+endpoint);
			return null;
		}
		

		AtomContainerSet acs=LoadFromSDF(folder+filename);

//		System.out.println(acs.getAtomContainerCount());
		
		for (int i=0;i<acs.getAtomContainerCount();i++) {
			AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);

			RecordOPERA ro = createRecordOpera(ac,endpoint);
			ro.property_name = endpoint;
			records.add(ro);
			
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

			case ExperimentalConstants.strBIODEG_HL_HC:
			case ExperimentalConstants.strKmHL:
				return ExperimentalConstants.str_LOG_DAYS;
				
			case ExperimentalConstants.strMeltingPoint:
			case ExperimentalConstants.strBoilingPoint:
				return ExperimentalConstants.str_C;
			
			//TODO add CERAPP, COMPARA ==> Binary

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
			String value=(String)entry.getValue();

			if(value==null || value.trim().isEmpty()) continue;
			
			if(key.toLowerCase().contains("cas")) key="CAS";
			if(key.toLowerCase().contains("name")) key="ChemicalName";
			if(key.toLowerCase().equals("dsstox_substance_id") || key.contentEquals("DTXSID")) key="dsstox_substance_id";
			
//			System.out.println(key);
			if (key.contains(" ")) {
//				System.out.println("space in key\t"+key);
				if (key.contains("Reference") || key.contains("Temperature")) {
					key=key.substring(key.indexOf(" ")+1,key.length());
				}			
			}			
			key=key.replace("KocRef","Reference").replace("LogKOA_Ref", "Reference");
			if (key.contains("cdk")) continue;
			
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

//		System.out.println(ro.LogKOA);
		//Set property value and units:
		if (!property_name.equals(ExperimentalConstants.strORAL_RAT_LD50) && !property_name.equals(ExperimentalConstants.str_pKA))
			ro.property_value_original=getPropertyValueOriginal(property_name,ro);
		
		ro.property_value_units_original=getOriginalUnits(property_name);

		
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
	public Vector<RecordOPERA> parseOperaSdfs() {
		Vector<RecordOPERA> records = new Vector<>();

		Hashtable<String,String>ht=new Hashtable<>();

		ht.put(ExperimentalConstants.strLogKOA,"KOA_QR.sdf");
		ht.put(ExperimentalConstants.strLogKOW, "LogP_QR.sdf");
		ht.put(ExperimentalConstants.strBCF, "BCF_QR.sdf");
		ht.put(ExperimentalConstants.strKOC, "KOC_QR.sdf");
		ht.put(ExperimentalConstants.strKmHL, "KM_QR.sdf");
		ht.put(ExperimentalConstants.strBIODEG_HL_HC, "Biodeg_QR.sdf");
		ht.put(ExperimentalConstants.strOH, "AOH_QR.sdf");
		ht.put(ExperimentalConstants.strBoilingPoint, "BP_QR.sdf");
		ht.put(ExperimentalConstants.strHenrysLawConstant, "HL_QR.sdf");
		ht.put(ExperimentalConstants.strMeltingPoint, "MP_QR.sdf");
		ht.put(ExperimentalConstants.strVaporPressure, "VP_QR.sdf");
		ht.put(ExperimentalConstants.strWaterSolubility, "WS_QR.sdf");
		ht.put(ExperimentalConstants.str_pKA, "pKa_QR.sdf");
		ht.put(ExperimentalConstants.strCLINT,"Clint_QR.sdf");
		ht.put(ExperimentalConstants.strFUB,"FU_QR.sdf");
		ht.put(ExperimentalConstants.strRBIODEG,"RBiodeg_QR.sdf");
		ht.put(ExperimentalConstants.strORAL_RAT_LD50,"CATMoS_QR50k.sdf");
		
//		ht.put(ExperimentalConstants.strInVitroToxicity,"CERAPP_QR.sdf");
		

//		ht.put(ExperimentalConstants.strCACO2,"NA.sdf");//TODO No SDF available
		//TODO add CERAPP, COMPARA, RT (omit according to Tony)
		
        // for-each loop
        for(String property : ht.keySet()) {
        	System.out.println("Parsing sdf for "+property);
        	records.addAll(parseOPERA_SDF(property,ht.get(property)));
        }
		
		return records;
	}

	ExperimentalRecord toExperimentalRecord(String date,String propertyName) {

//		if(CAS!=null && CAS.contains("NOCAS")) CAS=null;
//		if(ChemicalName!=null && ChemicalName.contains("NoName")) ChemicalName=null;

		ExperimentalRecord er = new ExperimentalRecord();

		er.property_name=propertyName;
		er.source_name = ExperimentalConstants.strSourceOPERA;
		er.document_name = Reference;
		er.property_value_units_original = property_value_units_original;

		er.dsstox_substance_id = dsstox_substance_id;
		er.dsstox_compound_id = dsstox_compound_id;
		er.casrn = CAS;
		er.chemical_name = ChemicalName;
		er.smiles = Original_SMILES;
		
		er.note = "qc_level= " + DSSTox_QC_Level;
		er.date_accessed = date;
		er.keep = true;
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
	 * exists only to 
	 * @param args
	 */
	public static void main(String[] args) {
//		parseOPERA_SDF(ExperimentalConstants.strWaterSolubility,"WS_QR.sdf");
		RecordOPERA r=new RecordOPERA();
		
		r.parseOperaSdfs();
	}
}