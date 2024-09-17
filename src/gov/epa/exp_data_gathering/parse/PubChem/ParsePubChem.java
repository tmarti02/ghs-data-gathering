package gov.epa.exp_data_gathering.parse.PubChem;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.text.StringEscapeUtils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Reference;

/**
- Records tab
	- are all the unit conversions correct (check example of each "property_value_units_original")
		- Can also check source code such as ParseUtilities.getViscosity (sets "property_value_units_original") and UnitConverter.convertViscosity (converts to ""property_value_units_final")
	- check records with "property_value_string" containing a semicolon (indicating a series of values). Are there cases where some of the individual records didnt make it in and was there something in first/last value that should have applied to all?

- Records_Bad tab, report the cases where >5 records exist where 
	- for reason="Bad data or units", failed to parse but was actually ok
	- for records where reason="Duplicate of experimental value from same source" but there are there no matching duplicate records in Records tab
	- for reason="decomposes" has a usable value in there somewhere
	- for reason="Estimated" accidentally flagged
	- for reason="Failed range correction", failed to parse a range
	- for reason="Incorrect property", is this correct? Look at "property_value_string_parsed"
	- for reason="Original units missing" are there some where it just failed to parse?
	
	
For following properties, find examples where it fails to set the experimental conditions correctly (>5 cases), flag them in spreadsheet using cell color

- boiling point:
	- "pressure_mmHg" ? E.g. property_value_string = "159-160 °C at 2.00E+00 mm Hg" => pressure_mmHg = 0.0e0

- Density tab:
	- if g/cm3 assumed (see "note"), cases where this was wrong assumption
	- "pressure_mmHg" 
	- "temperature_c" 
	
- logKow
	- "temperature_c? 
	- "pH"? 

- surface tension, vapor pressure, henry's law constant, viscosity
	- "temperature_c"


 */

public class ParsePubChem extends Parse {
	
	static boolean storeDTXCIDs=false;
	
	public ParsePubChem() {
		sourceName = RecordPubChem.sourceName;
		this.init();
		folderNameWebpages=null;
	}
	
	
	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();		


	/**
	 * Parses JSON entries in database to RecordPubChem objects, then saves them to a JSON file
	 */
	@Override
	protected void createRecords() {
		
		if(generateOriginalJSONRecords) {
			Vector<RecordPubChem> records = RecordPubChem.parseJSONsInDatabase();
			System.out.println("Added "+records.size()+" records");
			writeOriginalRecordsToFile(records);
		}
	}
	
	/**
	 * Reads the JSON file created by createRecords() and translates it to an ExperimentalRecords object
	 */
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordPubChem> recordsPubChem = new ArrayList<RecordPubChem>();
			RecordPubChem[] tempRecords = null;
			
//			System.out.println(howManyOriginalRecordsFiles);
			
			if (howManyOriginalRecordsFiles==1) {
				tempRecords = gson.fromJson(new FileReader(jsonFile), RecordPubChem[].class);
				for (int i = 0; i < tempRecords.length; i++) {
					recordsPubChem.add(tempRecords[i]);
				}
			} else {
				for (int batch = 1; batch <= howManyOriginalRecordsFiles; batch++) {
					String batchFileName = jsonFileName.substring(0,jsonFileName.indexOf(".")) + " " + batch + ".json";
					File batchFile = new File(batchFileName);
					tempRecords = gson.fromJson(new FileReader(batchFile), RecordPubChem[].class);
					for (int i = 0; i < tempRecords.length; i++) {
						recordsPubChem.add(tempRecords[i]);
					}
				}
			}
			
			System.out.println(recordsPubChem.size());
			
			Hashtable<String,String>htCID=getCID_HT();
			
			Iterator<RecordPubChem> it = recordsPubChem.iterator();
			while (it.hasNext()) {
				RecordPubChem r = it.next();
				
				//Skip the following until we have code to handle it:
				if(r.propertyName.equals("Other Experimental Properties")) continue;//TODO see what properties are in there
				if(r.propertyName.equals("Collision Cross Section")) continue;
				if(r.propertyName.equals("Odor Threshold")) continue;
				if(r.propertyName.equals("Ionization Potential")) continue;
				if(r.propertyName.equals("Polymerization")) continue;
				if(r.propertyName.equals("Stability/Shelf Life")) continue;
				if(r.propertyName.equals("Decomposition")) continue;
				if(r.propertyName.equals("Heat of Vaporization")) continue;
				if(r.propertyName.equals("Heat of Combustion")) continue;
				if(r.propertyName.equals("Enthalpy of Sublimation")) continue;
				if(r.propertyName.equals("Corrosivity")) continue;
				if(r.propertyName.equals("Taste")) continue;
				
				if(r.propertyName.equals("Dissociation Constants")) continue;//TODO can get Acidic pKa if add more code
				if(r.propertyName.contains("pK")) continue;
				
				if(r.propertyName.equals("Ionization Efficiency")) continue;
				if(r.propertyName.equals("Optical Rotation")) continue;
				if(r.propertyName.equals("Refractive Index")) continue;
				if(r.propertyName.equals("Relative Evaporation Rate")) continue;
//				if(r.propertyName.equals("Viscosity")) continue;				
//				if(r.propertyName.equals("Surface Tension")) continue;
				if(r.propertyName.equals("pH")) continue;
				if(r.propertyName.equals("Acid Value")) continue;
				if(r.propertyName.equals("Additive")) continue;
				if(r.propertyName.equals("Organic modifier")) continue;
				if(r.propertyName.equals("Reference")) continue;
				if(r.propertyName.equals("Ionization mode")) continue;
				if(r.propertyName.equals("logIE")) continue;
				if(r.propertyName.equals("Acid Value")) continue;
				if(r.propertyName.equals("Instrument")) continue;
				if(r.propertyName.equals("Ion source")) continue;
				if(r.propertyName.equals("Stability")) continue;
				if(r.propertyName.equals("Dielectric Constant")) continue;
				if(r.propertyName.equals("Accelerating Rate Calorimetry (ARC)")) continue;
				if(r.propertyName.equals("Differential Scanning Calorimetry (DSC)")) continue;
				if(r.propertyName.equals("Dispersion")) continue;
				
				
//				String property=ExperimentalConstants.strLogKOW;
//				
//
//				List<String>properties=Arrays.asList(ExperimentalConstants.strWaterSolubility);//								
//				List<String>properties=Arrays.asList(ExperimentalConstants.strDensity,ExperimentalConstants.strVaporDensity);
				
//				List<String>properties=Arrays.asList(ExperimentalConstants.strDensity);
//				List<String>properties=Arrays.asList(ExperimentalConstants.strVaporPressure);

//				List<String>properties=Arrays.asList(ExperimentalConstants.strLogKOW);
				List<String>properties=Arrays.asList(ExperimentalConstants.strMeltingPoint);
//				List<String>properties=Arrays.asList(ExperimentalConstants.strBoilingPoint);
//				List<String>properties=Arrays.asList(ExperimentalConstants.strAutoIgnitionTemperature);
//				List<String>properties=Arrays.asList(ExperimentalConstants.strFlashPoint);

//				List<String>properties=Arrays.asList(ExperimentalConstants.strViscosity);
//				List<String>properties=Arrays.asList(ExperimentalConstants.strSurfaceTension);
//				List<String>properties=Arrays.asList(ExperimentalConstants.strHenrysLawConstant);
				
//				List<String>properties=Arrays.asList(ExperimentalConstants.str_pKA);
//				List<String>properties=Arrays.asList(ExperimentalConstants.str_pKA,ExperimentalConstants.str_pKAa,ExperimentalConstants.str_pKAb);
				
				boolean runOnly=false;
				
				if(r.propertyValue.contains(";") && !r.propertyName.equals("Physical Description") && !r.propertyName.equals("Color/Form") &&  !r.propertyName.equals("Odor") ) {
				
					String propertyValueOriginal=r.propertyValue;
					
					String [] vals=r.propertyValue.split(";");
					
					boolean haveDensity=false;
					boolean haveVP=false;
					
					for (String propertyValue:vals) {
						if(propertyValue.toLowerCase().contains("density")) haveDensity=true;
						if(propertyValue.toLowerCase().contains("vapor pressure")) haveVP=true;
					}
					
					for (String propertyValue:vals) {

						r.propertyValue=propertyValue.trim();
						
						if(r.propertyValue.indexOf("OECD")==0) continue;
						
						ExperimentalRecord er=r.toExperimentalRecord(propertyValueOriginal);
						
						
						if(haveDensity && !propertyValue.toLowerCase().contains("density") && er.property_name.toLowerCase().contains("density")) {
//							System.out.println("missing density: "+r.propertyName+"\t"+r.propertyValue);
							continue;
						}
						
//						if(haveVP && !propertyValue.toLowerCase().contains("vapor pressure") && er.property_name.equals(ExperimentalConstants.strVaporPressure)) {
//							System.out.println("missingVP: "+r.propertyName+"\t"+r.propertyValue+"\t"+propertyValueOriginal);
//							continue;
//						}


//						if(er.reason!=null && er.reason.equals("No values")) {
//							System.out.println(r.propertyValue);
//						}
						
						if(!propertyValue.equals(propertyValueOriginal)) {
							er.updateNote("parsed property_value: "+propertyValue);
						}
						
						er.property_value_string_parsed=propertyValue;						
						er.property_value_string=propertyValueOriginal;
						
						if(runOnly && !properties.contains(er.property_name)) continue;
//						if(!er.property_name.equals(property)) continue;
						
						if(er==null) continue;	

						if (storeDTXCIDs) 
							if(htCID.containsKey(r.cid)) er.dsstox_compound_id=htCID.get(r.cid);

						
//						System.out.println(originalValue+"\t"+r.propertyValue);
						
//						if(er.casrn!=null && er.casrn.equals("75-38-7")) {
//							System.out.println(gson.toJson(er));
//						}
						
//						if(er.reason!=null && er.reason.equals("Original units missing")) {
//							System.out.println(propertyValue);
//						}

						recordsExperimental.add(er);
					}	
					
				} else {//treat as one record

					ExperimentalRecord er=r.toExperimentalRecord(r.propertyValue);

					er.property_value_string_parsed=r.propertyValue;						
					
//					if(!er.property_name.equals(property)) continue;
					if(runOnly && !properties.contains(er.property_name)) continue;
					
//					if(er.reason!=null && er.reason.equals("No values")) {
//						System.out.println(r.propertyValue+"\t"+er.property_value_point_estimate_original);
//					}
					
					if(er==null) continue;	
					
					if (storeDTXCIDs) 
						if(htCID.containsKey(r.cid)) er.dsstox_compound_id=htCID.get(r.cid);
					
					recordsExperimental.add(er);

				}
							
				
				
//				if(r.markupChemicals!=null) {
//					System.out.println(r.propertyName);
					
//					if(er.property_name.contentEquals(ExperimentalConstants.strWaterSolubility) ||
//							er.property_name.equals(ExperimentalConstants.strBoilingPoint) || 
//							er.property_name.equals(ExperimentalConstants.strMeltingPoint) ||
//							er.property_name.equals(ExperimentalConstants.strLogKOW) ||
//							er.property_name.equals(ExperimentalConstants.strHenrysLawConstant) ||
//							er.property_name.equals(ExperimentalConstants.strDensity) ||
//							er.property_name.equals(ExperimentalConstants.strVaporPressure))
					
//					System.out.println(r.propertyName+"\t"+r.propertyValue+"\trefCAS"+er.casrn+"\trefName="+ r.chemicalNameReference+"\tfirstMarkupName="+r.markupChemicals.get(0).name+"\tcidName="+r.iupacNameCid);
//					System.out.println(r.propertyName+"\t"+r.propertyValue+"\trefCAS="+er.casrn+"\trefName="+ r.chemicalNameReference+"\tcidName="+r.iupacNameCid);

//				}
				
//				if(er.property_name.contentEquals(ExperimentalConstants.strWaterSolubility)){
//					if(er.property_value_point_estimate_original!=null)
//						System.out.println(er.chemical_name+"\t"+er.casrn+"\t"+ er.property_value_string+"\t"+er.property_value_numeric_qualifier+"\t"+  er.property_value_point_estimate_original+"\t"+er.property_value_units_original+"\t"+er.pH);
//				}
						
				
				//do we want to trust the cid from compounds table in dsstox???
				
				
				
			}
			
			recordsExperimental.getRecordsByProperty();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	public static Hashtable<String, String> getCID_HT() {
		Hashtable<String,String>ht=new Hashtable<>();

		Type listType = new TypeToken<ArrayList<JsonObject>>(){}.getType();
		
		List<JsonObject> jaMolWeight=null;
		try {
			Gson gson=new Gson();
			jaMolWeight = gson.fromJson(new FileReader("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\dsstox\\pubchem cids to dtxcids.json"), listType);
			
			for (JsonObject jo:jaMolWeight) {
				String pubchemCID=jo.get("pubchem_cid").getAsString();
				
				if(jo.get("dsstox_compound_id").isJsonNull()) continue;
				
				String dtxcid=jo.get("dsstox_compound_id").getAsString();
				ht.put(pubchemCID,dtxcid);
//				System.out.println(pubchemCID+"\t"+dtxcid);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ht;
	}

	
	public static void main(String[] args) {
		ParsePubChem p = new ParsePubChem();
		
		p.storeDTXCIDs=false;//if true it stores dtxcid based on the lookup from the compounds table in dsstox
		p.generateOriginalJSONRecords=false;
		p.howManyOriginalRecordsFiles=3;
		p.removeDuplicates=true;

		p.writeJsonExperimentalRecordsFile=true;
		p.writeExcelExperimentalRecordsFile=true;
		p.writeExcelFileByProperty=true;		
		p.writeCheckingExcelFile=true;
		
		
		p.createFiles();
	}
}
