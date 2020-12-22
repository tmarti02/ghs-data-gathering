package gov.epa.exp_data_gathering.parse;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Vector;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;

/**
 * See OPERA article, Mansouri, 2018: https://doi.org/10.1186/s13321-018-0263-1
 * See OPERA repo for zip file: https://github.com/NIEHS/OPERA/blob/master/OPERA_Data.zip
 * 
 * TODO: Need to get data for BP, HL, LogP, MP, pKA, VP, and WS
 * 
 * @author TMARTI02
 *
 */
public class RecordOPERA {

	String ChemID;

	String source_casrn;//Map to ExperimentalRecord.casrn
	String Original_SMILES;//Map to ExperimentalRecord.smiles

	String dsstox_substance_id;//Map to ExperimentalRecord.dsstox_substance_id (need to add field)
	
	// **********************************************************************
	//May not need to map following to ExperimentalRecord:
	String CAS;//Derived quantity from DSSTOX
	String preferred_name;//Derived quantity from DSSTOX
	String dsstox_compound_id;//Derived quantity from DSSTOX
	String Canonical_QSARr;//Derived quantity from DSSTOX
	String InChI_Code_QSARr;//Derived quantity from DSSTOX
	String InChI_Key_QSARr;//Derived quantity from DSSTOX
	// **********************************************************************
	
	String Salt_Solvent;//used to account for the fact that solubility of salt was measured
	String Salt_Solvent_ID;
	String MPID;
	
	public Double property_value_point_estimate_original;//each property in OPERA stores the property value in a different field- so need field to store all of them
	public String property_value_units_original;//sometimes it will take some work to figure this out by comparing to data values from other sources
		
	String Temperature;//Map to ExperimentalRecord.temperature_C
	String Reference;//Map to ExperimentalRecord.reference (might need to add a field), since should put PHYSPROP for original_source_name
	
	String Tr_1_Tst_0;//tells you whether compound appears in their training or test sets

	public String toJSON() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();// makes it multiline and readable
		Gson gson = builder.create();
		return gson.toJson(this);//all in one line!
	}

	public static Vector<RecordOPERA> parseOPERA_SDF(String endpoint) {

		
		Vector<RecordOPERA> records=new Vector<>();
		
		String folder="data\\experimental\\OPERA\\OPERA_SDFS\\";
		

		String filename = getFileName(endpoint);
		
		if (filename==null) return null;
		

		AtomContainerSet acs=LoadFromSDF(folder+filename);

		
		for (int i=0;i<acs.getAtomContainerCount();i++) {
			AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
			RecordOPERA ro = createRecordOpera(ac);

			//Print out:
			if (ro.CAS.contentEquals("71-43-2")) System.out.println(ro.toJSON());

			
			records.add(ro);
			
		}
			
		return records;
			

	}

	private static String getFileName(String endpoint) {
		switch (endpoint) {
		case ExperimentalConstants.strWaterSolubility:
			return "WS_QR.sdf";			
		case ExperimentalConstants.strVaporPressure:
			return "VP_QR.sdf";
			
		//TODO add other pertinent properties...

		}
		return null;

	}

	/**
	 * Converts atom container property data into RecordOPERA object
	 * 
	 * @param ac
	 * @return
	 */
	private static RecordOPERA createRecordOpera(AtomContainer ac) {
		RecordOPERA ro=new RecordOPERA();

		Map<Object,Object>props=ac.getProperties();

		for (Map.Entry<Object,Object> entry : props.entrySet()) {  

			String key=(String)entry.getKey();
			String value=(String)entry.getValue();

			if (key.contains("cdk")) continue;

//			System.out.println("Key = " + key + 
//					", Value = " + value);
			
			if (key.contentEquals("LogMolar")) {
				ro.property_value_point_estimate_original=Double.parseDouble(value);
				ro.property_value_units_original="log10_M";//Note: later to get M need to use Math.pow(10,value)					
			
			} else if (key.contentEquals("LogHL")) {
				ro.property_value_point_estimate_original=Double.parseDouble(value);
				ro.property_value_units_original="log10_?";//TODO- determine what "?" is 					
				
			} else if (key.contains("Reference")) {
				ro.Reference=value;
				
			} else if (key.contains("Temperature")) {
				ro.Temperature=value;
				
			} else {
				try {
					//Use reflection to assign values from key/value pair:
//					System.out.println(key);
					Field myField = ro.getClass().getDeclaredField(key.replace(" ", "_"));
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


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		parseOPERA_SDF(ExperimentalConstants.strWaterSolubility);
	}

}
