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

	public String source_casrn;//Map to ExperimentalRecord.casrn
	public String Original_SMILES;//Map to ExperimentalRecord.smiles

	public String dsstox_substance_id;//Map to ExperimentalRecord.dsstox_substance_id (need to add field)
	
	// **********************************************************************
	//May not need to map following to ExperimentalRecord:
	public String CAS;//Derived quantity from DSSTOX
	public String preferred_name;//Derived quantity from DSSTOX
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
	public String Substance_CASRN;
	public String Substance_Name;
	public String Extenal_ID;
	public String DSSTox_Structure_Id;
	public String DSSTox_QC_Level;
	public String DSSTox_Substance_Id;
	
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

	public static Vector<RecordOPERA> parseOPERA_SDF(String endpoint,String filename) {

		
		Vector<RecordOPERA> records=new Vector<>();
		
		String folder="data\\experimental\\OPERA\\OPERA_SDFS\\";
		
		if (filename==null) {
			System.out.println("filename missing for "+endpoint);
			return null;
		}
		

		AtomContainerSet acs=LoadFromSDF(folder+filename);

		System.out.println(acs.getAtomContainerCount());
		
		for (int i=0;i<acs.getAtomContainerCount();i++) {
			AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
			RecordOPERA ro = createRecordOpera(ac,endpoint);
			ro.property_name = endpoint;
			

			//Print out:
			// if (ro.Substance_CASRN.contentEquals("71-43-2")) System.out.println(ro.toJSON());
			// if (ro.CAS.contentEquals("71-43-2")) System.out.println(ro.toJSON());
			if (ro.CAS != null) { // only added because pka printing for benzene doesn't work
			if (ro.CAS.contentEquals("71-43-2")) System.out.println(ac.getProperties());
			}

			
			records.add(ro);
			
		}
			
		return records;
			

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
			
//			System.out.println(key);
			if (key.contains(" ")) {
				if (key.contains("Reference") || key.contains("Temperature")) {
					key=key.substring(key.indexOf(" ")+1,key.length());
				}			
			}			
			key=key.replace("KocRef","Reference").replace("LogKOA_Ref", "Reference");
			
			String value=(String)entry.getValue();

			if (key.contains("cdk")) continue;
			
			if (key.contentEquals("LogMolar")) {
				// ro.property_value_point_estimate_original=Double.parseDouble(value);
				ro.property_value_units_original=ExperimentalConstants.str_log_M;//Note: later to get M need to use Math.pow(10,value)					
			} else if (key.contentEquals("LogHL")) {
				ro.property_value_units_original=ExperimentalConstants.str_log_atm_m3_mol;//TODO- determine what "?" is
			} else if (key.contentEquals("MP")) {
				ro.property_value_units_original=ExperimentalConstants.str_C;
			} else if (key.contentEquals("LogVP")) {
				ro.property_value_units_original=ExperimentalConstants.str_log_mmHg;
			} else if (key.contentEquals("BP")) {
				ro.property_value_units_original=ExperimentalConstants.str_C;
			}
			
			if (key.contentEquals("dsstox_substance_id")) {
				ro.DSSTox_Substance_Id=value;
			} else {//Use reflection to store based on name of field
				try {
					//Use reflection to assign values from key/value pair:
//					System.out.println(key);
					Field myField = ro.getClass().getDeclaredField(key.replace(" ", "_").replace("-", "_"));

//					System.out.println(key+"\t"+value);
					
					myField.set(ro, value);
					
					//TODO if get error- add if statement above to account for it or in some cases add a new field to RecordOPERA class
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		            				
			}			
		}
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
	public static Vector<RecordOPERA> parseOperaSdf() {
		Vector<RecordOPERA> records = new Vector<>();

		Hashtable<String,String>ht=new Hashtable<>();
		
		ht.put(ExperimentalConstants.strLogOH, "AOH_QR.sdf");
		ht.put(ExperimentalConstants.strLogBCF, "BCF_QR.sdf");
		ht.put(ExperimentalConstants.strLogHalfLifeBiodegradation, "Biodeg_QR.sdf");
		ht.put(ExperimentalConstants.strBoilingPoint, "BP_QR.sdf");
		ht.put(ExperimentalConstants.strHenrysLawConstant, "HL_QR.sdf");
		ht.put(ExperimentalConstants.strLogKmHL, "KM_QR.sdf");
		ht.put(ExperimentalConstants.strLogKOA, "KOA_QR.sdf");
		ht.put(ExperimentalConstants.strLogKOC, "KOC_QR.sdf");
		ht.put(ExperimentalConstants.strLogKow, "LogP_QR.sdf");
		ht.put(ExperimentalConstants.strMeltingPoint, "MP_QR.sdf");
		ht.put(ExperimentalConstants.strVaporPressure, "VP_QR.sdf");
		ht.put(ExperimentalConstants.strWaterSolubility, "WS_QR.sdf");
		
		ht.put(ExperimentalConstants.str_pKA, "pKa_QR.sdf");
		
		Set<String> setOfProperties = ht.keySet();
		 
        // for-each loop
        for(String property : setOfProperties) {
        	records.addAll(parseOPERA_SDF(property,ht.get(property)));
        }
		
		return records;
	}

	public Double getPropertyValueOriginal() {
		if (BP != null)
			return Double.parseDouble(BP);
		else if (LogHL != null)
			return Double.parseDouble(LogHL);
		else if (LogP != null)
			return Double.parseDouble(LogP);
		else if (MP != null)
			return Double.parseDouble(MP);
		else if (LogVP != null)
			return Double.parseDouble(LogVP);
		else if (LogMolar != null)
			return Double.parseDouble(LogMolar);
		else if (LogOH!=null)
			return Double.parseDouble(LogOH);
		else if (LogBCF!=null)
			return Double.parseDouble(LogBCF);
		else if (LogHalfLife!=null)
			return Double.parseDouble(LogHalfLife);
		else if (LogKOC!=null)
			return Double.parseDouble(LogKOC);
		else if (LogKOA!=null)
			return Double.parseDouble(LogKOA);
		else if (LogKmHL!=null)
			return Double.parseDouble(LogKmHL);
		else {
			System.out.println("Need entry in getPropertyValueOriginal for "+property_name);
			return null;	
		}
		
	}
	
	
	/**
	 * exists only to 
	 * @param args
	 */
	public static void main(String[] args) {
		parseOPERA_SDF(ExperimentalConstants.strWaterSolubility,"WS_QR.sdf");
	}
}