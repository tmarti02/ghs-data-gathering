package gov.epa.exp_data_gathering.parse.PubChem;

import java.io.File;
import java.io.FileNotFoundException;
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
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.ParseUtilities;
import gov.epa.exp_data_gathering.parse.PubChem.JSONsForPubChem.Reference;

/**

For each spreadsheet:

- Records tab
	- are all the unit conversions correct (check example of each "property_value_units_original")
		- Also check source code (do a pull first) such as ParseUtilities.getViscosity (sets "property_value_units_original") and UnitConverter.convertViscosity (converts to "property_value_units_final")
	- check records with "property_value_string" containing a semicolon (indicating a series of values). Are there cases where some of the individual records didnt make it in and was there something in first/last value that should have applied to all?

- Records_Bad tab, report the cases where >5 records exist where 
	- for reason="Bad data or units", failed to parse but was actually ok
	- for records where reason="Duplicate of experimental value from same source" but there are there no matching duplicate records in Records tab
	- for reason="decomposes" has a usable value in there somewhere
	- for reason="Estimated" accidentally flagged
	- for reason="Failed range correction", failed to parse a range
	- for reason="Incorrect property", is this correct? Look at "property_value_string_parsed"
	- for reason="Original units missing" are there some where it just failed to parse?
	
	
For following properties, also find examples (look at "property_value_string_parsed") where it fails to set the experimental conditions correctly (>5 cases for a given quirk), flag them in spreadsheet using cell color

- boiling point:
	- "pressure_mmHg" ? E.g. property_value_string_parsed = "159-160 °C at 2.00E+00 mm Hg" => pressure_mmHg = 0.0e0

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
//	boolean createExcelForSelectedProperties=false;
//	List<String>selectedProperties=null;
	List<String>selectedHeadings=null;
	
	String databaseFormatCompound="compound";
	String databaseFormatAnnotation="annotation";
	
	String databaseFormat=databaseFormatAnnotation;
//	String databaseFormat=databaseFormatCompound;
	
	
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
			
			if(databaseFormat.equals(databaseFormatCompound)) {
				Vector<RecordPubChem> records = RecordPubChem.parseJSONsInDatabase();
				System.out.println("Added "+records.size()+" records");
				writeOriginalRecordsToFile(records);
			} else if(databaseFormat.equals(databaseFormatAnnotation)) {
				ParseNewDatabase p=new ParseNewDatabase();
				p.parseJSONsInDatabase();
			}
		}
	}
	
	/**
	 * Reads the JSON file created by createRecords() and translates it to an ExperimentalRecords object
	 */
//	@Override
	protected ExperimentalRecords goThroughOriginalRecordsOld() {
		
		System.out.println("Enter goThroughOriginalRecords");
		
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
			
			Hashtable<String,String>htCID=null;
			if(this.storeDTXCIDs) htCID=getCID_HT();
			
//			for(String prop:selectedProperties) {
//				System.out.println("selProp="+prop);
//			}
			
			
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

				// Fix before splitting, need programmatic way than just replacing: 
				fixPropertyValueBeforeSplit(r);	
				
				handleRecordPubChem(recordsExperimental, htCID, r);
							
				
				
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
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}
	
	
	ExperimentalRecords goThroughOriginalRecordsCompound() {

		System.out.println("Enter goThroughOriginalRecords");
		
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		try {
			
			String jsonFileName = jsonFolder + File.separator + fileNameJSON_Records;
			File jsonFile = new File(jsonFileName);
			
			List<RecordPubChem> recordsPubChem = new ArrayList<RecordPubChem>();
			RecordPubChem[] tempRecords = null;
			
			System.out.println("howManyOriginalRecordsFiles="+howManyOriginalRecordsFiles);
			
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
			
//			File folder=new File(jsonFolder);
//			List<RecordPubChem> recordsPubChem = new ArrayList<RecordPubChem>();
//			
//			//Dont rely on the howManyOriginalRecordsFiles variable- just go by filenames:
//			for (File file:folder.listFiles()) {
//				
//				if(!file.getName().contains(".json")) continue;
//				if(file.getName().contains("Copy")) continue;
//				if(!file.getName().contains("Original Records")) continue;
//
////				String heading=file.getName().replace(".json", "").replace("Original Records ","");
////				if(selectedHeadings!=null && !selectedHeadings.contains(heading)) continue; 
//				
//				RecordPubChem[] tempRecords;
//				try {
//					tempRecords = gson.fromJson(new FileReader(file), RecordPubChem[].class);
//					for(RecordPubChem record:tempRecords) recordsPubChem.add(record);
//					System.out.println(file.getName()+"\t"+tempRecords.length);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}			
//			}
			
			System.out.println("Number of records="+recordsPubChem.size());
			
			Hashtable<String,String>htCID=null;
			if(this.storeDTXCIDs) htCID=getCID_HT();
			
//			if(selectedProperties!=null) {
//				for(String prop:selectedProperties) {
//					System.out.println("selProp="+prop);
//				}
//			}
			
			
			Iterator<RecordPubChem> it = recordsPubChem.iterator();
			while (it.hasNext()) {
				RecordPubChem r = it.next();
				
				//Skip the following until we have code to handle it:
				if(skipRecord(r)) continue;
				
				fixPropertyValueBeforeSplit(r);	
				handleRecordPubChem(recordsExperimental, htCID, r);
							
			}
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return recordsExperimental;
	}

	private void handleRecordPubChem(ExperimentalRecords recordsExperimental, Hashtable<String, String> htCID,
			RecordPubChem r) {
		
		
		if(selectedHeadings!=null && !selectedHeadings.contains(r.propertyName)) return; 
		
		
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
				
				//Density is hard to parse, need to exclude the cases where density appeared in one of the split values but not the others
				if(haveDensity && !propertyValue.toLowerCase().contains("density") && er.property_name.toLowerCase().contains("density")) {
//					System.out.println("missing density: "+r.propertyName+"\t"+r.propertyValue);
					continue;
				}
				
//				if(haveVP && !propertyValue.toLowerCase().contains("vapor pressure") && er.property_name.equals(ExperimentalConstants.strVaporPressure)) {
//					System.out.println("missingVP: "+r.propertyName+"\t"+r.propertyValue+"\t"+propertyValueOriginal);
//					continue;
//				}


//				if(er.reason!=null && er.reason.equals("No values")) {
//					System.out.println(r.propertyValue);
//				}
				
//				if(!propertyValue.equals(propertyValueOriginal)) {
//					er.updateNote("parsed property_value: "+propertyValue);
//				}
				
				er.property_value_string_parsed=propertyValue;						
				er.property_value_string=propertyValueOriginal;
				
//				if(createExcelForSelectedProperties && !selectedProperties.contains(er.property_name)) continue;
////			if(!er.property_name.equals(property)) continue;
				
				if(er==null) continue;	

				if (storeDTXCIDs) 
					if(htCID.containsKey(r.cid)) er.dsstox_compound_id=htCID.get(r.cid);

				
//				System.out.println(originalValue+"\t"+r.propertyValue);
//				System.out.println(gson.toJson(er));
//				
//				if(er.reason!=null && er.reason.equals("Original units missing")) {
//					System.out.println(propertyValue);
//				}

				recordsExperimental.add(er);
				
			} //end loop over split values	
			
		} else {//treat as one record

			ExperimentalRecord er=r.toExperimentalRecord(r.propertyValue);

			er.property_value_string_parsed=r.propertyValue;						
			
//					if(!er.property_name.equals(property)) continue;
//			if(createExcelForSelectedProperties && !selectedProperties.contains(er.property_name))
//				return;
			
//			if(er.reason!=null && er.reason.equals("No values")) {
//				System.out.println(r.propertyValue+"\t"+er.property_value_point_estimate_original);
//			}
			
			if(er==null)
				return;	
			
			if (storeDTXCIDs) {
				//Do we want to trust the cid from compounds table in dsstox???
				if(htCID.containsKey(r.cid)) er.dsstox_compound_id=htCID.get(r.cid);
			}
			
			recordsExperimental.add(er);

		}
	}
	
	
	boolean skipRecord (RecordPubChem r) {
		
		List<String>skipProperties=new ArrayList<>();
		
		skipProperties.add("Other Experimental Properties");
		skipProperties.add("Collision Cross Section");
		skipProperties.add("Odor Threshold");
		skipProperties.add("Ionization Potential");
		skipProperties.add("Polymerization");
		skipProperties.add("Stability/Shelf Life");
		skipProperties.add("Decomposition");
		skipProperties.add("Heat of Vaporization");
		skipProperties.add("Heat of Combustion");
		skipProperties.add("Enthalpy of Sublimation");
		skipProperties.add("Corrosivity");
		skipProperties.add("Taste");
		skipProperties.add("Dissociation Constants");
		skipProperties.add("pK");
		skipProperties.add("Ionization Efficiency");
		skipProperties.add("Optical Rotation");
		skipProperties.add("Refractive Index");
		skipProperties.add("Relative Evaporation Rate");
//		skipProperties.add("Viscosity");
//		skipProperties.add("Surface Tension");
		skipProperties.add("pH");
		skipProperties.add("Acid Value");
		skipProperties.add("Additive");
		skipProperties.add("Organic modifier");
		skipProperties.add("Reference");
		skipProperties.add("Ionization mode");
		skipProperties.add("logIE");
		skipProperties.add("Acid Value");
		skipProperties.add("Instrument");
		skipProperties.add("Ion source");
		skipProperties.add("Stability");
		skipProperties.add("Dielectric Constant");
		skipProperties.add("Accelerating Rate Calorimetry (ARC");
		skipProperties.add("Differential Scanning Calorimetry (DSC");
		skipProperties.add("Dispersion");
		
		
		for (String skipProperty:skipProperties) {
			if(r.propertyName.contains(skipProperty)) return true;
		}

		return false;

	}
	
	
	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		if(databaseFormat.equals(databaseFormatCompound)) return goThroughOriginalRecordsCompound();
		else if(databaseFormat.equals(databaseFormatAnnotation)) return goThroughOriginalRecordsAnnotation();
		else return null;
	}


	private ExperimentalRecords goThroughOriginalRecordsAnnotation() {
		
		List<RecordPubChem> recordsPubChem = getPubChemRecordsForSelectedHeadings();
		
		Hashtable<String,String>htCID=null;
		if(this.storeDTXCIDs) htCID=getCID_HT();
		
		ExperimentalRecords recordsExperimental=new ExperimentalRecords();
		
		Iterator<RecordPubChem> it = recordsPubChem.iterator();
		while (it.hasNext()) {
			RecordPubChem r = it.next();
			
//			String property=ExperimentalConstants.strLogKOW;

			// Fix before splitting, need programmatic way than just replacing: 
			fixPropertyValueBeforeSplit(r);	
			
			handleRecordPubChem(recordsExperimental, htCID, r);
						
			//do we want to trust the cid from compounds table in dsstox???
			
		}
		
		return recordsExperimental;
		
	}

	private List<RecordPubChem> getPubChemRecordsForSelectedHeadings() {
		File folder=new File(jsonFolder);
		List<RecordPubChem> recordsPubChem = new ArrayList<RecordPubChem>();
		
		
		for (File file:folder.listFiles()) {
			
			if(!file.getName().contains(".json")) continue;
			if(file.getName().contains("Copy")) continue;
			if(!file.getName().contains("Original Records")) continue;

			String heading=file.getName().replace(".json", "").replace("Original Records ","");
			if(selectedHeadings!=null && !selectedHeadings.contains(heading)) continue; 
			
			RecordPubChem[] tempRecords;
			try {
				tempRecords = gson.fromJson(new FileReader(file), RecordPubChem[].class);
				for(RecordPubChem record:tempRecords) recordsPubChem.add(record);
				System.out.println(heading+"\t"+tempRecords.length);
			} catch (Exception e) {
				e.printStackTrace();
			}			
			
		}
		System.out.println("total original records\t"+recordsPubChem.size());
		return recordsPubChem;
	}

	/**
	 * Fix records that have nonstandard delimiting into property values
	 * 
	 * @param r
	 */
	private void fixPropertyValueBeforeSplit(RecordPubChem r) {
		r.propertyValue=r.propertyValue.replace("337.5 °C at 760 mm Hg: 265 °C at 100 mm Hg: 240.5 °C at 40 mm Hg: 222 °Cat 20 mm Hg: 205.5 °C at 10 mm Hg; 191 °C at 5 mm Hg; 159.5 °C at 1.0 mm Hg", "337.5 °C at 760 mm Hg; 265 °C at 100 mm Hg; 240.5 °C at 40 mm Hg; 222 °C at 20 mm Hg; 205.5 °C at 10 mm Hg; 191 °C at 5 mm Hg; 159.5 °C at 1.0 mm Hg");
		r.propertyValue=r.propertyValue.replace("Boiling point = 98.9 °C at 100 mm Hg, 61 °C at 20 mm Hg, 47.4 °C at 10 mm Hg, and 9.6 °C at 1.0 mm Hg", "98.9 °C at 100 mm Hg; 61 °C at 20 mm Hg; 47.4 °C at 10 mm Hg; 9.6 °C at 1.0 mm Hg");
		r.propertyValue=r.propertyValue.replace("0.0029 mmHg at 70 °F","VP=0.0029 mmHg at 70 °F");
		r.propertyValue=r.propertyValue.replace("1.1270 (Milbemycin A3); 1.1265 (Milbemycin A4), both at 25 °C","1.1270 (Milbemycin A3) at 25 °C; 1.1265 (Milbemycin A4) at 25 °C");
		//LogKow comma delimited issues
		r.propertyValue=r.propertyValue.replace("log Kow = 1.5 (pH 5.0), -0.21 (pH 6.9), -0.76 (pH 9.0) at 25 °C","log Kow = 1.5 (pH 5.0) at 25 °C; -0.21 (pH 6.9) at 25 °C; -0.76 (pH 9.0) at 25 °C");
		r.propertyValue=r.propertyValue.replace("log Kow = 2.63 (E)-isomer; 2.73 (Z)-isomer (both 20 °C)","log Kow = 2.63 (E)-isomer at 20 °C; 2.73 (Z)-isomer at 20 °C");
		r.propertyValue=r.propertyValue.replace("log Kow = 1.49 (pH 7); 1.04 (pH 5): 1.20 (pH 9), all at 25 °C","log Kow = 1.49 (pH 7) at 25 °C; 1.04 (pH 5) at 25 °C; 1.20 (pH 9) at 25 °C");
		r.propertyValue=r.propertyValue.replace("log Kow (20 °C): -0.30 (pH 4); -1.55 (pH 7); -1.59 (pH 9)", "-0.30 (pH 4) at 20 °C; -1.55 (pH 7) at 20 °C; -1.59 (pH 9) at 20 °C");
		r.propertyValue=r.propertyValue.replace("log Kow = 1.5 (pH 5.0), -0.21 (pH 6.9), -0.76 (pH 9.0) at 25 °C","log Kow = 1.5 (pH 5.0) at 25 °C; -0.21 (pH 6.9) at 25 °C; -0.76 (pH 9.0) at 25 °C");
		r.propertyValue=r.propertyValue.replace("log Kow = 2.43 (pH 4), -0.07 (pH 7), -1.56 (pH 9)","log Kow = 2.43 (pH 4); -0.07 (pH 7); -1.56 (pH 9)");
		r.propertyValue=r.propertyValue.replace("log Kow = 4.05 (unbuffered, 20 °C), 4.16 (pH 4), 3.82 (pH 7), 2.00 (pH 9)", "log Kow = 4.05 (unbuffered, 20 °C); 4.16 (pH 4); 3.82 (pH 7); 2.00 (pH 9)");
		r.propertyValue=r.propertyValue.replace("log Kow = 2.06 (cis-isomer), 2.03 (trans-isomer) at 25 °C", "log Kow = 2.06 (cis-isomer); 2.03 (trans-isomer) at 25 °C");
		r.propertyValue=r.propertyValue.replace("log Kow = 1.1 (pH 5.0), -0.59 (pH 6.9), -1.8 (pH 9.0) at 25 °C", "log Kow = 1.1 (pH 5.0) at 25 °C; -0.59 (pH 6.9) at 25 °C; -1.8 (pH 9.0) at 25 °C");
		r.propertyValue=r.propertyValue.replace("log Kow = 0.42 (pH 5), -0.45 (pH 7), -0.96 (pH 9)","log Kow = 0.42 (pH 5); -0.45 (pH 7); -0.96 (pH 9)");
		r.propertyValue=r.propertyValue.replace("log Kow = -2.48 (pH 7), -1.12 (pH 4)", "log Kow = -2.48 (pH 7); -1.12 (pH 4)");
		r.propertyValue=r.propertyValue.replace("log Kow = 0.74 at pH 5 and -1.34 at pH 7", "log Kow = 0.74 at pH 5; -1.34 at pH 7");
		r.propertyValue=r.propertyValue.replace("log Kow = 2.8, also reported as 4.5", "log Kow = 2.8; also reported as 4.5");
		//Viscosity comma delimited issues
		r.propertyValue=r.propertyValue.replace("Viscosity coefficients = 4.88, 2.62, and 1.64 cP at 30, 60, and 90 °C, respectively ", "Viscosity coefficients = 4.88 at 30 °C; 2.62 at 60 °C; 1.64 cP at 90 °C");
		r.propertyValue=r.propertyValue.replace("0.475, 0.317, 0.276 and 0.255 cP at -69, -50, -40 and -33.5 °C, respectively","0.475 at -69 °C; 0.317 at -50 °C; 0.276 at -40 °C; 0.255 cP at -33.5 °C");
		r.propertyValue=r.propertyValue.replace("8 cP at 20 °C, 3.9 cP at 31.5 °C, 3.3 cP 44 °C, 2.2 cP at 60 °C","8 cP at 20 °C; 3.9 cP at 31.5 °C; 3.3 cP 44 °C; 2.2 cP at 60 °C");
		r.propertyValue=r.propertyValue.replace("60 CENTISTOKE AT 25 °C & 4.0 CENTISTOKE AT 100 °C /ISOMERIC MIXT/","60 CENTISTOKE AT 25 °C; 4.0 CENTISTOKE AT 100 °C");
		r.propertyValue=r.propertyValue.replace("At 18 °C: 74.35 dynes/cm (2.72 wt%), 75.85 dynes/cm (5.66 wt%), 83.05 dynes/cm (16.66 wt%), 96.05 dynes/cm (30.56 wt%), 101.05 dynes/cm (35.90 wt%)","At 18 °C: 74.35 dynes/cm (2.72 wt%); 75.85 dynes/cm (5.66 wt%) at 18 °C; 83.05 dynes/cm (16.66 wt%) at 18 °C; 96.05 dynes/cm (30.56 wt%) at 18 °C; 101.05 dynes/cm (35.90 wt%) at 18 °C");
		//Flash point comma delimited issues
		r.propertyValue=r.propertyValue.replace("104 °F (40 °C) CLOSED CUP, 150 °F (66 °C) OPEN CUP /ANHYDROUS 76%/","104 °F (40 °C) CLOSED CUP; 150 °F (66 °C) OPEN CUP /ANHYDROUS 76%/");
		r.propertyValue=r.propertyValue.replace("Solution: 225 °F (open cup), 132 °F (closed cup)","Solution: 225 °F (open cup); 132 °F (closed cup)");
		r.propertyValue=r.propertyValue.replace("91 °F (n-), 106 °F (all isomers), 69 °F (iso-)","91 °F (n-); 106 °F (all isomers); 69 °F (iso-)");
		r.propertyValue=r.propertyValue.replace("Anhydrous: 35 °C (closed cup), 38 °C (open cup); 88% solution: 42 °C","Anhydrous: 35 °C (closed cup); 38 °C (open cup)");
		r.propertyValue=r.propertyValue.replace("97 °C c.c., 102 °C o.c.","97 °C c.c.; 102 °C o.c.");
		r.propertyValue=r.propertyValue.replace("Flash point: 27.8 °C (Tag open cup), 31.1 °C (Tag closed cup)","Flash point: 27.8 °C (Tag open cup); 31.1 °C (Tag closed cup)");
		r.propertyValue=r.propertyValue.replace("111.11 °C c.c., 115 °C o.c.","111.11 °C c.c.; 115 °C o.c.");
		r.propertyValue=r.propertyValue.replace("39 °C (closed cup), 24 °C (open cup)","39 °C (closed cup); 24 °C (open cup)");
		r.propertyValue=r.propertyValue.replace("54 °C c.c., 57 °C o.c.","54 °C c.c.; 57 °C o.c.");
		r.propertyValue=r.propertyValue.replace("-19 °C (Closed cup), -15 °C (Open cup)","-19 °C (Closed cup); -15 °C (Open cup)");
		r.propertyValue=r.propertyValue.replace("24.4 °C (Tag open cup), 17.8 °C (Tag closed cup)","24.4 °C (Tag open cup); 17.8 °C (Tag closed cup)");
		r.propertyValue=r.propertyValue.replace("43 °C c.c., 57.2 °C o.c.","43 °C c.c.; 57.1 °C o.c.");
		r.propertyValue=r.propertyValue.replace("46 °C c.c., 52 °C o.c.","46 °C c.c.; 52 °C o.c.");
		r.propertyValue=r.propertyValue.replace("33.9 °C (open cup), 43.3 °C (closed cup)","33.9 °C (open cup); 43.3 °C (closed cup)");
		r.propertyValue=r.propertyValue.replace("68 °C c.c., 77 °C o.c.","68 °C c.c.; 77 °C o.c.");
		r.propertyValue=r.propertyValue.replace("110 °C  (open cup) ... 118 °C (closed cup)", "110 °C  (open cup); 118 °C (closed cup)");
		//Vapor Pressure Comma Delimited Issues
		r.propertyValue=r.propertyValue.replace("1 Pa at 1517 °C, 10 Pa at 1687 °C, 100 Pa at 1982 °C, 1 kPa at 2150 °", "1 Pa at 1517 °C; 10 Pa at 1687 °C; 100 Pa at 1982 °C; 1 kPa at 2150 °C");
		r.propertyValue=r.propertyValue.replace("Vapor pressure: 120 mm Hg at 20 °C, 190 mm Hg at 30 °C", "Vapor pressure: 120 mm Hg at 20 °C; 190 mm Hg at 30 °C");
		r.propertyValue=r.propertyValue.replace("VP: 1 Pa at -158 °C (solid), 10 Pa at -147 °C (solid), 100 Pa at -133.6 °C (solid), 1 kPa at -116.6 °C (solid), 10 kPa at -94.4 °C (solid), 100 kPa at -64.1 °C (solid)", "VP: 1 Pa at -158 °C (solid); 10 Pa at -147 °C (solid); 100 Pa at -133.6 °C (solid); 1 kPa at -116.6 °C (solid); 10 kPa at -94.4 °C (solid); 100 kPa at -64.1 °C (solid)");
		r.propertyValue=r.propertyValue.replace("Vapor pressure: cis-isomers 2.9X10-3 mPa at 25 °C, trans-isomers 9.2X10-4 Pa at 25 °C", "Vapor pressure: cis-isomers 2.9X10-3 mPa at 25 °C; trans-isomers 9.2X10-4 Pa at 25 °C");
		r.propertyValue=r.propertyValue.replace("2.13 kPa at 150 °C /0.000246 mm Hg at 25 °C/ (extrapolated)", "2.13 kPa at 150 °C ; 0.000246 mm Hg at 25 °C (extrapolated)");
		r.propertyValue=r.propertyValue.replace("Vapor pressure: 10 mm Hg at -72.3 °C; 1 mm Hg at -95.4 °C; 40 mm Hg at -53.7 °C, 100 mm Hg at -39.1 °C; 400 mm Hg at -12.0 °C", "Vapor pressure: 10 mm Hg at -72.3 °C; 1 mm Hg at -95.4 °C; 40 mm Hg at -53.7 °C; 100 mm Hg at -39.1 °C; 400 mm Hg at -12.0 °C");
		r.propertyValue=r.propertyValue.replace("VP: 0.5 mm Hg at 98-100 °C, 2 mm Hg at 138-140 °C, 11 mm Hg at 154-156 °C", "VP: 0.5 mm Hg at 98-100 °C; 2 mm Hg at 138-140 °C; 11 mm Hg at 154-156 °C");
		r.propertyValue=r.propertyValue.replace("1 mmHg at -36.9 °F, 100 mmHg at 96.3 °F, 760 mmHg at 192.2 °F", "1 mmHg at -36.9 °F; 100 mmHg at 96.3 °F; 760 mmHg at 192.2 °F");
		r.propertyValue=r.propertyValue.replace("Vapor pressure = 0.15 kPa at 20 °C, 0.48 kPa at 45 °C", " Vapor pressure = 0.15 kPa at 20 °C; 0.48 kPa at 45 °C");
		r.propertyValue=r.propertyValue.replace("14 mmHg at 36 °F, 26.2 mmHg at 73 °F, 67 mmHg at 118 °F", "14 mmHg at 36 °F; 26.2 mmHg at 73 °F; 67 mmHg at 118 °F");
		r.propertyValue=r.propertyValue.replace("34.3 mm Hg at 25 °C (cis isomer), 23.0 mm Hg at 25 °C (trans isomer)", "34.3 mm Hg at 25 °C (cis isomer); 23.0 mm Hg at 25 °C (trans isomer)");
		r.propertyValue=r.propertyValue.replace("VP: 0.16, 0.40, 1.6 and 2.7 mm Hg at 0, 10, 30 and 40 °C, respectively", "VP: 0.16 mm Hg at 0 °C; 0.40 mm Hg at 10 °C; 1.6 mm Hg at 30 °C; 2.7 mm Hg at 40 °C");
		r.propertyValue=r.propertyValue.replace("Vapor pressure: 6.07 mm Hg at 20 °C, 20 mm Hg at 26.6 °C", "Vapor pressure: 6.07 mm Hg at 20 °C; 20 mm Hg at 26.6 °C");
		r.propertyValue=r.propertyValue.replace("Vapor pressure: 760 mm Hg at -21.0 °C, 10 mm Hg at -76.8 °C (solid), 40 mm Hg at -62.7 °C (solid), 100 mm Hg at -51.8 °C (solid)", "Vapor pressure: 760 mm Hg at -21.0 °C; 10 mm Hg at -76.8 °C (solid); 40 mm Hg at -62.7 °C (solid); 100 mm Hg at -51.8 °C (solid)");
		r.propertyValue=r.propertyValue.replace("10 kPa at -71.3 °C, 100 kPa at -33.6 °C (liquid)", "10 kPa at -71.3 °C; 100 kPa at -33.6 °C (liquid)");
		r.propertyValue=r.propertyValue.replace("Vapor pressure: 1 Pa at -139 °C, 10 Pa at -127 °C, 100 Pa at -112 °C", "Vapor pressure: 1 Pa at -139 °C; 10 Pa at -127 °C; 100 Pa at -112 °C");
		r.propertyValue=r.propertyValue.replace("<4.3X10-5 mm Hg at 25 °C, <1.1X10-5 mm Hg at 20 °C /OECD Guideline 104/", "4.3X10-5 mm Hg at 25 °C; 1.1X10-5 mm Hg at 20 °C /OECD Guideline 104/");
		r.propertyValue=r.propertyValue.replace("Vapor pressure determined by gas phase analysis: 0.013, 0.025, 0.13, & 0.29 mm Hg at 0, 10, 30, & 40 °C, respectively", "Vapor pressure determined by gas phase analysis: 0.013 at 0 °C; 0.025 at 10 °C; 0.13 at 30 °C; 0.29 mm Hg at 40 °C");
		r.propertyValue=r.propertyValue.replace("Vapor pressure = 0.06 atm at 0 °C, 0.11 atm at 10 °C, 0.173 atm at 20 °C, 0.26 atm at 30 °C", "Vapor pressure = 0.06 atm at 0 °C; 0.11 atm at 10 °C; 0.173 atm at 20 °C; 0.26 atm at 30 °C");
		//Water solubility comma delimited issues
		r.propertyValue=r.propertyValue.replace("IN WATER: 102 G/100 CC @ 0 °C, 531 G/100 CC @ 80 °C", "IN WATER: 102 G/100 CC @ 0 °C; 531 G/100 CC @ 80 °C");
		r.propertyValue=r.propertyValue.replace("In water: 0.25% at 30 °C, 3.8% at 100 °C", "In water: 0.25% at 30 °C; 3.8% at 100 °C");
		r.propertyValue=r.propertyValue.replace("In water, 0.22 (pH 4), 28.3 (pH 7) (both in g/L, 20 °C)", "In water, 0.22 g/L (20 °C, pH 4); 28.3 g/L (20 °C, pH 7)");
		r.propertyValue=r.propertyValue.replace("In water, 116 (pH 5), >626 (pH 7), >628 (pH 9) (all in g/l, 25 °C)", "In water, 116 g/L (25 °C, pH 5); >626 g/L (25 °C, pH 7); >628 g/L (25 °C, pH 9)");
		r.propertyValue=r.propertyValue.replace("Water solubility: 120 mg/L at 25 °C, 350 mg/L at 50 °C, 3200 mg/L at 100 °C", "Water solubility: 120 mg/L at 25 °C; 350 mg/L at 50 °C; 3200 mg/L at 100 °C");
		r.propertyValue=r.propertyValue.replace("In water, 12 mg/L at 20 °C, 22 mg/L at 25 °C, and 23 mg/L at 30 °C.", "In water, 12 mg/L at 20 °C; 22 mg/L at 25 °C; 23 mg/L at 30 °C.");
		r.propertyValue=r.propertyValue.replace("In water, 3.34 mg/L at 20 °C, 4.46 mg/L at 25 °C", "In water, 3.34 mg/L at 20 °C; 4.46 mg/L at 25 °C");
		r.propertyValue=r.propertyValue.replace("In water, 3 ppm (pH 5),184 ppm (pH 7) at 25 °C", "In water, 3 ppm (pH 5) at 25 °C; 184 ppm (pH 7) at 25 °C");
		r.propertyValue=r.propertyValue.replace("In water, 17.6 (pH 5), 1627 (pH 7), 482 (pH 9) (all in mg/L, 20 °C)", "In water, 17.6 mg/L (20 °C, pH 5); 1627 mg/L (20 °C, pH 7); 482 mg/L (20 °C, pH 9)");
		r.propertyValue=r.propertyValue.replace("Solubility in water = 2.61 ppm at pH 5.9, 3.21 ppm at pH 4, 2.39 ppm at pH 7, and 2.32 ppm at pH 10 /Technical product/", "Solubility in water = 2.61 ppm at pH 5.9; 3.21 ppm at pH 4; 2.39 ppm at pH 7; 2.32 ppm at pH 10 /Technical product/");
		r.propertyValue=r.propertyValue.replace("In water, 0.102 mg/L (pH 5, 7) , 0.135 mg/L (pH 9)", "In water, 0.102 mg/L (pH 5, 7); 0.135 mg/L (pH 9)");
		r.propertyValue=r.propertyValue.replace("IN WATER @ PH 7.1: 0.0041 G/100 ML @ 25 °C, 0.008 G/100 ML @ 37 °C; SOL IN SOLN OF ALKALI HYDROXIDES; SPARINGLY SOL IN ETHER, CHLOROFORM; SLIGHTLY SOL IN ETHANOL", "IN WATER @ PH 7.1: 0.0041 G/100 ML @ 25 °C; 0.008 G/100 ML @ 37 °C, pH 7.1; SOL IN SOLN OF ALKALI HYDROXIDES; SPARINGLY SOL IN ETHER, CHLOROFORM; SLIGHTLY SOL IN ETHANOL");
		r.propertyValue=r.propertyValue.replace("In water, 63 mg/L (pH 5), 5850 mg/L (pH 7), and 10546 mg/L (pH 9)", "In water, 63 mg/L (pH 5), 5850 mg/L (pH 7); 10546 mg/L (pH 9)");
		r.propertyValue=r.propertyValue.replace("In water (20 °C) = 5 mg/l (pH 5), 6.7 mg/l (pH 6.5), 9800 mg/l (pH 9)", "In water (20 °C) = 5 mg/l (pH 5); 6.7 mg/l (pH 6.5); 9800 mg/l (pH 9)");
		r.propertyValue=r.propertyValue.replace("In pure water, 120 mg/L at 24 °C; in buffered creek water at 24 °C, 190 mg/L (pH 6.5), 230 mg/l (pH 7.5), 260 mg/L (pH 8.5)", "In pure water, 120 mg/L at 24 °C; in buffered creek water at 24 °C, 190 mg/L (pH 6.5); 230 mg/l (24 °C, pH 7.5); 260 mg/L (24 °C, pH 8.5)");
		r.propertyValue=r.propertyValue.replace("1 g dissolves in 0.9 mL water at room temperature, 0.2 mL water at 80 °C", "1 g dissolves in 0.9 mL water at room temperature (70°F); 1 g dissolves in 0.2 mL water at 80 °C");
		r.propertyValue=r.propertyValue.replace("Solubility in water: 3.3 (pH 5), 243 (pH 7), 5280 (pH 9) (all in mg/l)", "Solubility in water: 3.3 mg/l (pH 5); 243 mg/l (pH 7); 5280 mg/l (pH 9)");
		r.propertyValue=r.propertyValue.replace("137.8 g/100 cc water @ 0 °C, 1270 g/100 cc water @ 100 °C, 100 g/100 cc alcohol @ 12.5 °C, very slightly sol in liq ammonia /Cupric nitrate trihydrate/", "137.8 g/100 cc water @ 0 °C; 1270 g/100 cc water @ 100 °C; 100 g/100 cc alcohol @ 12.5 °C, very slightly sol in liq ammonia /Cupric nitrate trihydrate/");
		r.propertyValue=r.propertyValue.replace("Sparingly soluble in water: 3.3 ml/100 ml at 0 °C, 2.3 ml/100 ml at 20 °C", "Sparingly soluble in water: 3.3 ml/100 ml at 0 °C; 2.3 ml/100 ml at 20 °C");
		r.propertyValue=r.propertyValue.replace("In water, 0.64 g/100 g at 20 °C, 0.76 g/100 g at 25 °C, 1.27 g/100 g at 50 °C, 2.45 g/100 g at 100 °C.", "In water, 0.64 g/100 g at 20 °C; 0.76 g/100 g at 25 °C; 1.27 g/100 g at 50 °C; 2.45 g/100 g at 100 °C.");
		r.propertyValue=r.propertyValue.replace("In water (mg/L at 25 °C), 5 (pH 5.1), 67 (pH 6.1), 308 (pH 7)", "In water (mg/L at 25 °C), 5 (pH 5.1); 67 mg/L (25 °C, pH 6.1); 308 mg/L (25 °C, pH 7)");
		r.propertyValue=r.propertyValue.replace("In water, 429 mg/L (pH 7), 3936 mg/L (pH 9) at 25 °C", "In water, 429 mg/L (pH 7) at 25 °C; 3936 mg/L (pH 9) at 25 °C");
		r.propertyValue=r.propertyValue.replace("In water (mg/L at 20 °C), 0.37 (pH 5), 160 (pH 7), 2200 mg/L (pH 9)", "In water (mg/L at 20 °C), 0.37 (pH 5); 160 mg/L (20 °C, pH 7); 2200 mg/L (20 °C, pH 9)");
		r.propertyValue=r.propertyValue.replace("In water: 267 g/100 ml @ 10 °C, 620 G/100 ml @ 60 °C", "In water: 267 g/100 ml @ 10 °C; 620 G/100 ml @ 60 °C");
		r.propertyValue=r.propertyValue.replace("In water at 25 °C, 51 mg/L (pH 5), 118 mg/L (pH 7), 900 mg/L (pH 9)", "In water at 25 °C, 51 mg/L (pH 5); 118 mg/L (pH 7)at 25 °C; 900 mg/L (pH 9) at 25 °C");
		r.propertyValue=r.propertyValue.replace("In water, 3490 mg/L at 25 deg, 2790 mg/L at 20 °C, 3790 mg/L at 30 °C", "In water, 3490 mg/L at 25 degC; 2790 mg/L at 20 °C; 3790 mg/L at 30 °C");
		r.propertyValue=r.propertyValue.replace("In water, 29 mg/L (pH 4.5), 87 mg/L (pH 5.0), 4000 mg/L (pH 6.8), 43000 mg/L (pH 7.7) at 25 °C", "In water, 29 mg/L (pH 4.5), 87 mg/L (pH 5.0) at 25 °C; 4000 mg/L (pH 6.8) at 25 °C; 43000 mg/L (pH 7.7) at 25 °C");
		r.propertyValue=r.propertyValue.replace("Slowly sol in water: ~1% at 20 °C, ~1.45% at 30 °C, ~1.94% at 40 °C", "Slowly sol in water: ~1% at 20 °C; ~1.45% at 30 °C; ~1.94% at 40 °C");
		r.propertyValue=r.propertyValue.replace("SOL IN WATER: 17.2 G/100 ML @ 18 °C, 21.2 @ 25 °C; EASILY SOL IN ALC, KETONES, ETHER, ACETONE, ORG SOLVENTS, CHLORINATED & AROMATIC HYDROCARBONS; SLIGHTLY SOL IN PENTANE, PETROLEUM ETHER, LOWER PARAFFINS", "SOL IN WATER: 17.2 G/100 ML @ 18 °C; 21.2 @ 25 °C; EASILY SOL IN ALC, KETONES, ETHER, ACETONE, ORG SOLVENTS, CHLORINATED & AROMATIC HYDROCARBONS; SLIGHTLY SOL IN PENTANE, PETROLEUM ETHER, LOWER PARAFFINS");
		r.propertyValue=r.propertyValue.replace("9.5 to 13 mg/L at 20 °C in distilled water, 75.5 mg/L at 20 °C in salt water", "9.5 to 13 mg/L at 20 °C in distilled water; 75.5 mg/L at 20 °C in salt water");
		r.propertyValue=r.propertyValue.replace("2,040 mg/L at pH 7 and 18,300 mg/L at pH 9, all at 20 °C", "2,040 mg/L at 20 °C,pH 7; 18,300 mg/L at 20 °C, pH 9");
		r.propertyValue=r.propertyValue.replace("Solubility in water 4.0 g/100 ml water at 15 °C. Solubility in water 4.3 g/100 ml water at 25 °C. Solubility in water 5.0 g/100 ml water at 100 °C", "Solubility in water 4.0 g/100 ml water at 15 °C; Solubility in water 4.3 g/100 ml water at 25 °C; Solubility in water 5.0 g/100 ml water at 100 °C");
		r.propertyValue=r.propertyValue.replace("In water, 1.50X10+3 mg/L at 29 °C and 1.92X10+3 mg/L at 37 °C; solubility increases with increasing pH", "In water, 1.50X10+3 mg/L at 29 °C; 1.92X10+3 mg/L at 37 °C; solubility increases with increasing pH");
		r.propertyValue=r.propertyValue.replace("Solubility in water = 6.4, 7.6, 8.7, 10.0, 11.3, 12.7, 14.2, 16.5, and 19.1 g/100g solution at 0, 10, 20, 30, 40, 50, 60, 80, and 100 °C, respectively; Solubility in water = 6.9, 8.2, 9.6, 11.1, 12.7, 14.5, 16.5, 19.7, and 23.6 g/100g H2O at 0, 10, 20, 30, 40, 50, 60, 80, and 100 °C, respectively. Solubility is lower in the presence of sodium carbonate", "Solubility in water = 6.4 g/100g at 0 °C; 7.6 g/100g at 10 °C; 8.7 g/100g at 20 °C; 10.0 g/100g at 30  °C; 11.3 g/100g at 40 °C; 12.7 g/100g at 50 °C; 14.2 g/100g at 60 °C; 16.5 g/100g at 80 °C; 19.1 g/100g solution at 100 °C; Solubility in water = 6.9 g/100g at 0 °C; 8.2 g/100g at 10 °C; 9.6 g/100g at 20 °C; 11.1 g/100g at 30 °C; 12.7 g/100g at 40 °C; 14.5 g/100g at 50 °C; 16.5 g/100g at 60 °C; 19.7 g/100g at 80 °C; 23.6 g/100g H2O at 100 °C. Solubility is lower in the presence of sodium carbonate");
		r.propertyValue=r.propertyValue.replace("In water (25 °C), 18.4 mg/L at pH = 5, 0.221 mg/L at pH 7, 0.189 mg/L at pH 9", "In water (25 °C), 18.4 mg/L at pH = 5; 0.221 mg/L at 25 °C, pH 7; 0.189 mg/L at 25 °C, pH 9");
		r.propertyValue=r.propertyValue.replace("0.209 G/100 CC & 0.1619 G/100 CC WATER AT 30 & 100 °C; SOL IN AMMONIUM SALTS, SODIUM THIOSULFATE & GLYCERINE", "0.209 G/100 CC AT 30 °C; 0.1619 G/100 CC WATER AT 100 °C; SOL IN AMMONIUM SALTS, SODIUM THIOSULFATE & GLYCERINE");
		r.propertyValue=r.propertyValue.replace("21% (wt/vol) at 5 °C (water), 29% (wt/vol) at 40 °C (water), 33% (wt/vol at 25 °C (water)", "21% (wt/vol) at 5 °C (water); 29% (wt/vol) at 40 °C (water); 33% (wt/vol at 25 °C (water)");
		r.propertyValue=r.propertyValue.replace("Solubility in water at 0, 10, 20 and 30 °C is 6, 8.5, 17 and 28 wt %, respectively.", "Solubility in water is 6 wt % at 0 °C; 8.5 wt % at 10 °C; 17 wt % at 20 °C; 28 wt % at 30 °C");
		r.propertyValue=r.propertyValue.replace("In water at 25 °C: 2,180 mg/L (cis isomer), 2,320 mg/L (trans isomer)", "In water at 25 °C: 2,180 mg/L (cis isomer); 2,320 mg/L (trans isomer)");
		r.propertyValue=r.propertyValue.replace("0.085 G & 0.096 G/100 CC WATER @ 18 °C & 23 °C", "0.085 G/100 CC WATER @ 18 °C; 0.096 G/100 CC WATER 23 °C");
		r.propertyValue=r.propertyValue.replace("WATER: 119 G/100 ML @ 0 °C, 170.15 G/100 ML @ 100 °C; SLIGHTLY SOL IN ALCOHOL", "WATER: 119 G/100 ML @ 0 °C; 170.15 G/100 ML @ 100 °C; SLIGHTLY SOL IN ALCOHOL");
		r.propertyValue=r.propertyValue.replace("In water, 7.85X10+3 mg/L (distilled water); 188 g/L at pH 5, 143 g/L ay pH 7; 157 g/L at pH 9, all at 20 °C", "In water, 7.85X10+3 mg/L (distilled water) at 20 °C; 188 g/L at 20 °C, pH 5; 143 g/L at 20 °C, pH 7; 157 g/L at 20 °C, pH 9");
		r.propertyValue=r.propertyValue.replace("Solubility in water: 0.23 g/L at 0 °C, 11.4 g/L at 25 °C, 17.1 g/L at 50 °C, 27.95 g/L at 75 °C, 49.9 g/L at 100 °C", "Solubility in water: 0.23 g/L at 0 °C; 11.4 g/L at 25 °C; 17.1 g/L at 50 °C; 27.95 g/L at 75 °C; 49.9 g/L at 100 °C");
		r.propertyValue=r.propertyValue.replace("SOL IN WATER (G/L): 15 (25 °C), 23 (40 °C), 30 (50 °C), 55 (70 °C); SOL IN CARBON TETRACHLORIDE, PETROLEUM ETHER, NAPHTHA, XYLENE, DIBUTYL PHTHALATE, LIQUID PETROLATUM, ACETONE, ALCOHOL, VEGETABLE OILS; VERY SOL IN WATER-MISCIBLE ORGANIC SOLVENTS", "SOL IN WATER (G/L): 15 (25 °C); 23 G/L (40 °C); 30 G/L (50 °C); 55 G/L (70 °C); SOL IN CARBON TETRACHLORIDE, PETROLEUM ETHER, NAPHTHA, XYLENE, DIBUTYL PHTHALATE, LIQUID PETROLATUM, ACETONE, ALCOHOL, VEGETABLE OILS; VERY SOL IN WATER-MISCIBLE ORGANIC SOLVENTS");
		r.propertyValue=r.propertyValue.replace("IN WATER: 19.6 G/100 CC @ 14.5 °C, 83.3 G/100 CC @ 100 °C; IN ALCOHOL: 1.63 G/100 CC @ 25 °C; INSOL IN ETHER", "IN WATER: 19.6 G/100 CC @ 14.5 °C; 83.3 G/100 CC @ 100 °C; IN ALCOHOL: 1.63 G/100 CC @ 25 °C; INSOL IN ETHER");
		r.propertyValue=r.propertyValue.replace("In water, 1.067, 0.965, 0.914, and 0.896 g/100 g water at 0, 10, 20, and 30 °C, respectively", "In water, 1.067 g/100 g water at 0 °C; 0.965 g/100 g water at 10 °C; 0.914 g/100 g water at 20 °C; 0.896 g/100 g water at 30 °C");
		r.propertyValue=r.propertyValue.replace("Milbemycin A3: In water 0.88 ppm (20 °C). Milbemycin A4: In water 7.2 ppm (20 °C)", "Milbemycin A3: In water 0.88 ppm (20 °C); Milbemycin A4: In water 7.2 ppm (20 °C)");
		r.propertyValue=r.propertyValue.replace("In water, >1.14X10+4 at pH 5, 2.48X10-3 at pH 7, 0.180 at pH 9 (all in g/L at 20 °C)", "In water, >1.14X10+4 g/L at 20 °C, pH 5; 2.48X10-3 g/L at 20 °C, pH 7; 0.180 at pH 9 (all in g/L at 20 °C)");
		r.propertyValue=r.propertyValue.replace("In water, 0.027 g/L at 25 °C (pH 5); 2.1 (pH 7) (g/L, 25 °C)", "");
		r.propertyValue=r.propertyValue.replace("One gram dissolves in 20.8 mL water at 30 °C, in 38.5 mL at 18 °C, in 56.7 mL at 0 °C", "One gram dissolves in 20.8 mL water at 30 °C; One gram dissolves in 38.5 mL at 18 °C; One gram dissolves in 56.7 mL at 0 °C");
		r.propertyValue=r.propertyValue.replace("In water, 32 mg/L (pH 5), 815 mg/L (pH 7), 13,500 mg/L (pH 8.2) at 25 °C", "In water, 32 mg/L at 25 °C (pH 5); 815 mg/L at 25 °C (pH 7); 13,500 mg/L (pH 8.2) at 25 °C");
		r.propertyValue=r.propertyValue.replace("In water, 4.88 mg/L at 20 °C, 3.27 mg/L at 37 °C (column elution method)", "In water, 4.88 mg/L at 20 °C; 3.27 mg/L at 37 °C (column elution method)");
		r.propertyValue=r.propertyValue.replace("Water = 89.4 ppm (Spinosyn A) and 0.495 ppm (Spinosyn D)", "Water = 89.4 ppm (Spinosyn A); 0.495 ppm (Spinosyn D)");
		r.propertyValue=r.propertyValue.replace("In water at 25 °C: 3.48X10+3 mg/L at pH5; 2.95X10+3 mg/L at pH 7, 3.96X10+3 mg/L at pH 9", "In water at 25 °C: 3.48X10+3 mg/L at pH5; 2.95X10+3 mg/L at pH 7; 3.96X10+3 mg/L at pH 9");
		r.propertyValue=r.propertyValue.replace("1 g dissolves in: 4000 mL water at pH 6.5, 5 mL water at pH 7.5, 40 g methanol, 30 g ethanol, 10 g acetone, 1370 g ether, 2800 g chloroform; practically insol in benzene", "1 g dissolves in: 4000 mL water at pH 6.5; 5 mL water at pH 7.5, 40 g methanol, 30 g ethanol, 10 g acetone, 1370 g ether, 2800 g chloroform; practically insol in benzene");
		r.propertyValue=r.propertyValue.replace("pH-dependent solubility ranging from <0.1 mg/mL at pH 5-7 to over 1 mg/mL at pH 2 under ambient conditions", "pH-dependent solubility ranging from <0.1 mg/mL at pH 5-7; 1 mg/mL at pH 2 under ambient conditions");
		r.propertyValue=r.propertyValue.replace("Solubility in water as weight %: 24%, 0 °C; 26.3%, 20 °C; 28.9%, 40 °C; 31.7%, 60 °C; 34.4%, 80 °C, 26.2%, 100 °C", "Solubility in water as weight %: 24% at 0 °C; 26.3% at 20 °C; 28.9% at 40 °C; 31.7% at 60 °C; 34.4% at 80 °C; 26.2% at 100 °C");
		r.propertyValue=r.propertyValue.replace("Soluble in acetone, ether, ammonia, in water, 97 g/100 cc at 25 °C, 145.6 g/100 cc at 100 °C; in alcohol, 10 g/100 cc at 78 °C","Soluble in acetone, ether, ammonia, in water, 97 g/100 cc at 25 °C; 145.6 g/100 cc at 100 °C; in alcohol, 10 g/100 cc at 78 °C");
		r.propertyValue=r.propertyValue.replace("12 mg/100 ml in water @ 20 °C, 27 mg/100 ml @ 37 °C","12 mg/100 ml in water @ 20 °C; 27 mg/100 ml @ 37 °C");
		r.propertyValue=r.propertyValue.replace("SOLUBILITY IN WATER: 35% @ 0 °C, 41% @ 20 °C, 48% @ 50 °C","SOLUBILITY IN WATER: 35% @ 0 °C; 41% @ 20 °C; 48% @ 50 °C");
		r.propertyValue=r.propertyValue.replace("Sol in water (in g/100 ml water): 20.8 g @ 6 °C, 3.5 g @ 20 °C, 1.8 g @ 100 °C","Sol in water (in g/100 ml water): 20.8 g @ 6 °C; 3.5 g/100 ml @ 20 °C; 1.8 g/100 ml @ 100 °C");
		r.propertyValue=r.propertyValue.replace("In water, 5.46X10+5 mg/L at 30 °C, 4.79X10+5 mg/L at 20 °C","In water, 5.46X10+5 mg/L at 30 °C 4.79X10+5 mg/L at 20 °C");
		r.propertyValue=r.propertyValue.replace("In water, alpha-form = 0.32:, beta-form = 0.33 mg/L at 22 °C","In water, alpha-form = 0.32; beta-form = 0.33 mg/L at 22 °C");
		r.propertyValue=r.propertyValue.replace("Solubility in water at 25 °C, 300,000 mg/L at 20 °C and 340,000 mg/L at 25 °C /Maleic hydrazide potassium salt/","Solubility in water: 300,000 mg/L at 20 °C; 340,000 mg/L at 25 °C /Maleic hydrazide potassium salt/");
		r.propertyValue=r.propertyValue.replace("In water, 9.30 g/100 g at 0 °C, 39.84 g/100 g at 65.3 °C","In water, 9.30 g/100 g at 0 °C; 39.84 g/100 g at 65.3 °C");
		r.propertyValue=r.propertyValue.replace("In water, solubility is pH dependent wtih 0.1 mg/mL at pH 7 and 0.99 mg/mL at pH 11","In water, solubility is pH dependent wtih 0.1 mg/mL at pH 7; 0.99 mg/mL at pH 11");
		r.propertyValue=r.propertyValue.replace("1 gm dissolves in 46 mL water, 5.5 mL water at 80 °C, 1.5 mL boiling water, 66 mL alcohol, 22 mL alcohol at 60 °C, 50 mL acetone, 5.5 mL chloroform, 530 mL ether, 100 mL benzene, 22 mL boiling benzene.","1 gm dissolves in 46 mL water;1 gm dissolves in 5.5 mL water at 80 °C;1 gm dissolves in 1.5 mL boiling water, 66 mL alcohol, 22 mL alcohol at 60 °C, 50 mL acetone, 5.5 mL chloroform, 530 mL ether, 100 mL benzene, 22 mL boiling benzene.");
		r.propertyValue=r.propertyValue.replace("In water, 200 mg/L at 15 °C, 1368 mg/L at 50 °C","In water, 200 mg/L at 15 °C; 1368 mg/L at 50 °C");
		r.propertyValue=r.propertyValue.replace("In water at 20 °C, 5700 mg/L (pH 5.0), 7300 mg/L (pH 9.2)","In water at 20 °C, 5700 mg/L (pH 5.0); 7300 mg/L at 20 °C (pH 9.2)");
		r.propertyValue=r.propertyValue.replace("1 g sol in: 300 mL water at 25 °C, 100 mL water at 37 °C, 5 mL alcohol, 17 mL chloroform, 10-15 mL ether; less sol in anhydrous ether","1 g sol in: 300 mL water at 25 °C; 1 g sol in: 100 mL water at 37 °C; 5 mL alcohol, 17 mL chloroform, 10-15 mL ether; less sol in anhydrous ether");
		r.propertyValue=r.propertyValue.replace("In water at 20 °C, 0.5 g/L at pH 6, 1.5 g/L at pH 9","In water at 20 °C, 0.5 g/L at pH 6; 1.5 g/L at 20 °C, pH 9");
		r.propertyValue=r.propertyValue.replace("In water, 788 g/L at 25 °C, 3926 g/L at 97.5 °C","In water, 788 g/L at 25 °C; 3926 g/L at 97.5 °C");
		r.propertyValue=r.propertyValue.replace("Fluffy white powder, mp 238 °C. Solubility in water (mg/100 mL) at 30 °C: 220 (ph 3.8), 280 (pH 5.5), 290 (pH 6.7), 264 (pH 7.9), 244 (pH 9.2-10.1) /Hydrochloride/","Fluffy white powder. Solubility in water (mg/100 mL) at 30 °C: 220 (ph 3.8); 280 mg/100 mL at 30 °C (pH 5.5); 290 mg/100 mL at 30 °C (pH 6.7); 264 mg/100 mL) at 30 °C (pH 7.9); 244 mg/100 mL) at 30 °C (pH 9.2-10.1) /Hydrochloride/");
				
		r.propertyValue=r.propertyValue.replace("log Kow: pH 5: 6.2-6.3 (10 °C), 6-6.1 (20 °C), 5.8-6.0 (30 °C); pH 7: 5.1 (10 °C), 4.9 (20 °C), 4.8 (30 °C); pH 9: 4.9 (10 °C), 4.8 (20 °C), 4.6 (30 °C)","6.2-6.3 @ pH 5 and 10 °C; 6-6.1 @ pH 5 and 20 °C; 5.8-6.0 @ pH 5 and 30 °C; 5.1 @ pH 7 and 10 °C; 4.9 @ pH 7 and 20 °C; 4.8 @ pH 7 and 30 °C; 4.9 @ pH 9 and 10 °C; 4.8 @ pH 9 and 20 °C; 4.6 @ pH 9 and 30 °C");
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
		
//		p.databaseFormat=p.databaseFormatCompound;//old format
		p.databaseFormat=p.databaseFormatAnnotation;//new format based on annotation queries of pubchem
		
		storeDTXCIDs=false;//if true it stores dtxcid based on the lookup from the compounds table in dsstox

		p.generateOriginalJSONRecords=false;
		p.howManyOriginalRecordsFiles=3;//used for old format which doesnt store original jsons by heading
		p.removeDuplicates=true;

		p.writeJsonExperimentalRecordsFile=false;
		p.writeExcelExperimentalRecordsFile=true;
		p.writeExcelFileByProperty=true;		
		p.writeCheckingExcelFile=false;//creates random sample spreadsheet
		
//		p.selectedHeadings=null;
//		p.selectedHeadings=Arrays.asList("Solubility");								
//		p.selectedHeadings=Arrays.asList("Density","Vapor Density");
//		p.selectedHeadings=Arrays.asList("Vapor Density");
//		p.selectedHeadings=Arrays.asList("Density");
//		p.selectedHeadings=Arrays.asList("Vapor Pressure");
//		p.selectedHeadings=Arrays.asList("LogP");
//		p.selectedHeadings=Arrays.asList("Melting Point");
//		p.selectedHeadings=Arrays.asList("Boiling Point");
//		p.selectedHeadings=Arrays.asList("Autoignition Temperature");
//		p.selectedHeadings=Arrays.asList("Flash Point");
//		p.selectedHeadings=Arrays.asList("Viscosity");
		p.selectedHeadings=Arrays.asList("Surface Tension");
//		p.selectedHeadings=Arrays.asList("Henry's Law Constant");
		

		p.createFiles();
	}
}
