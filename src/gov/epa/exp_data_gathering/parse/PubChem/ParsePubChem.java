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
	- "pressure_mmHg" ? E.g. property_value_string_parsed = "159-160 캜 at 2.00E+00 mm Hg" => pressure_mmHg = 0.0e0

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
				if(er==null) continue;
				
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
			if(er==null) return;	

			er.property_value_string_parsed=r.propertyValue;						
			
//			if(er.reason!=null && er.reason.equals("No values")) {
//				System.out.println(r.propertyValue+"\t"+er.property_value_point_estimate_original);
//			}
			
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
		r.propertyValue=r.propertyValue.replace("337.5 캜 at 760 mm Hg: 265 캜 at 100 mm Hg: 240.5 캜 at 40 mm Hg: 222 캜at 20 mm Hg: 205.5 캜 at 10 mm Hg; 191 캜 at 5 mm Hg; 159.5 캜 at 1.0 mm Hg", "337.5 캜 at 760 mm Hg; 265 캜 at 100 mm Hg; 240.5 캜 at 40 mm Hg; 222 캜 at 20 mm Hg; 205.5 캜 at 10 mm Hg; 191 캜 at 5 mm Hg; 159.5 캜 at 1.0 mm Hg");
		r.propertyValue=r.propertyValue.replace("Boiling point = 98.9 캜 at 100 mm Hg, 61 캜 at 20 mm Hg, 47.4 캜 at 10 mm Hg, and 9.6 캜 at 1.0 mm Hg", "98.9 캜 at 100 mm Hg; 61 캜 at 20 mm Hg; 47.4 캜 at 10 mm Hg; 9.6 캜 at 1.0 mm Hg");
		r.propertyValue=r.propertyValue.replace("0.0029 mmHg at 70 캟","VP=0.0029 mmHg at 70 캟");
		r.propertyValue=r.propertyValue.replace("1.1270 (Milbemycin A3); 1.1265 (Milbemycin A4), both at 25 째C","1.1270 (Milbemycin A3) at 25 캜; 1.1265 (Milbemycin A4) at 25 캜");
		//LogKow comma delimited issues
		r.propertyValue=r.propertyValue.replace("log Kow = 1.5 (pH 5.0), -0.21 (pH 6.9), -0.76 (pH 9.0) at 25 캜","log Kow = 1.5 (pH 5.0) at 25 캜; -0.21 (pH 6.9) at 25 캜; -0.76 (pH 9.0) at 25 캜");
		r.propertyValue=r.propertyValue.replace("log Kow = 2.63 (E)-isomer; 2.73 (Z)-isomer (both 20 캜)","log Kow = 2.63 (E)-isomer at 20 캜; 2.73 (Z)-isomer at 20 캜");
		r.propertyValue=r.propertyValue.replace("log Kow = 1.49 (pH 7); 1.04 (pH 5): 1.20 (pH 9), all at 25 캜","log Kow = 1.49 (pH 7) at 25 캜; 1.04 (pH 5) at 25 캜; 1.20 (pH 9) at 25 캜");
		r.propertyValue=r.propertyValue.replace("log Kow (20 캜): -0.30 (pH 4); -1.55 (pH 7); -1.59 (pH 9)", "-0.30 (pH 4) at 20 캜; -1.55 (pH 7) at 20 캜; -1.59 (pH 9) at 20 캜");
		r.propertyValue=r.propertyValue.replace("log Kow = 1.5 (pH 5.0), -0.21 (pH 6.9), -0.76 (pH 9.0) at 25 캜","log Kow = 1.5 (pH 5.0) at 25 캜; -0.21 (pH 6.9) at 25 캜; -0.76 (pH 9.0) at 25 캜");
		r.propertyValue=r.propertyValue.replace("log Kow = 2.43 (pH 4), -0.07 (pH 7), -1.56 (pH 9)","log Kow = 2.43 (pH 4); -0.07 (pH 7); -1.56 (pH 9)");
		r.propertyValue=r.propertyValue.replace("log Kow = 4.05 (unbuffered, 20 캜), 4.16 (pH 4), 3.82 (pH 7), 2.00 (pH 9)", "log Kow = 4.05 (unbuffered, 20 캜); 4.16 (pH 4); 3.82 (pH 7); 2.00 (pH 9)");
		r.propertyValue=r.propertyValue.replace("log Kow = 2.06 (cis-isomer), 2.03 (trans-isomer) at 25 캜", "log Kow = 2.06 (cis-isomer); 2.03 (trans-isomer) at 25 캜");
		r.propertyValue=r.propertyValue.replace("log Kow = 1.1 (pH 5.0), -0.59 (pH 6.9), -1.8 (pH 9.0) at 25 캜", "log Kow = 1.1 (pH 5.0) at 25 캜; -0.59 (pH 6.9) at 25 캜; -1.8 (pH 9.0) at 25 캜");
		r.propertyValue=r.propertyValue.replace("log Kow = 0.42 (pH 5), -0.45 (pH 7), -0.96 (pH 9)","log Kow = 0.42 (pH 5); -0.45 (pH 7); -0.96 (pH 9)");
		r.propertyValue=r.propertyValue.replace("log Kow = -2.48 (pH 7), -1.12 (pH 4)", "log Kow = -2.48 (pH 7); -1.12 (pH 4)");
		r.propertyValue=r.propertyValue.replace("log Kow = 0.74 at pH 5 and -1.34 at pH 7", "log Kow = 0.74 at pH 5; -1.34 at pH 7");
		r.propertyValue=r.propertyValue.replace("log Kow = 2.8, also reported as 4.5", "log Kow = 2.8; also reported as 4.5");
		//Viscosity comma delimited issues
		r.propertyValue=r.propertyValue.replace("Viscosity coefficients = 4.88, 2.62, and 1.64 cP at 30, 60, and 90 캜, respectively ", "Viscosity coefficients = 4.88 at 30 캜; 2.62 at 60 캜; 1.64 cP at 90 캜");
		r.propertyValue=r.propertyValue.replace("0.475, 0.317, 0.276 and 0.255 cP at -69, -50, -40 and -33.5 캜, respectively","0.475 at -69 캜; 0.317 at -50 캜; 0.276 at -40 캜; 0.255 cP at -33.5 캜");
		r.propertyValue=r.propertyValue.replace("8 cP at 20 캜, 3.9 cP at 31.5 캜, 3.3 cP 44 캜, 2.2 cP at 60 캜","8 cP at 20 캜; 3.9 cP at 31.5 캜; 3.3 cP 44 캜; 2.2 cP at 60 캜");
		r.propertyValue=r.propertyValue.replace("60 CENTISTOKE AT 25 캜 & 4.0 CENTISTOKE AT 100 캜 /ISOMERIC MIXT/","60 CENTISTOKE AT 25 캜; 4.0 CENTISTOKE AT 100 캜");
		r.propertyValue=r.propertyValue.replace("At 18 캜: 74.35 dynes/cm (2.72 wt%), 75.85 dynes/cm (5.66 wt%), 83.05 dynes/cm (16.66 wt%), 96.05 dynes/cm (30.56 wt%), 101.05 dynes/cm (35.90 wt%)","At 18 캜: 74.35 dynes/cm (2.72 wt%); 75.85 dynes/cm (5.66 wt%) at 18 캜; 83.05 dynes/cm (16.66 wt%) at 18 캜; 96.05 dynes/cm (30.56 wt%) at 18 캜; 101.05 dynes/cm (35.90 wt%) at 18 캜");
		r.propertyValue = r.propertyValue.replace("0.475, 0.317, 0.276 and 0.255 cP at -69, -50, -40 and -33.5 째C, respectively","0.475 cP at -69캜; 0.317 cP at -50캜; 0.276 cP at -40캜; 0.255 cP -33.5 째C, respectively");
		r.propertyValue = r.propertyValue.replace("0.606 cP at 0 째C, 0.424 cP at 40 째C.","0.606 cP at 0 째C; 0.424 cP at 40 째C.");
		r.propertyValue = r.propertyValue.replace("1.78 mPa-s at 21.2 째C, 0.295 mPa-s at 178.2 째C","1.78 mPa-s at 21.2 째C; 0.295 mPa-s at 178.2 째C");
		r.propertyValue = r.propertyValue.replace("2.447 centipoise at 37.7 째C, 1.479 centistokes; 1.131 centipoise at 98.8 째C, 0.724 centistokes.","2.447 centipoise at 37.7 째C; 1.479 centistokes; 1.131 centipoise at 98.8 째C; 0.724 centistokes.");
		r.propertyValue = r.propertyValue.replace("60 CENTISTOKE AT 25 째C & 4.0 CENTISTOKE AT 100 째C /ISOMERIC MIXT/","60 CENTISTOKE AT 25 째C; 4.0 CENTISTOKE AT 100 째C /ISOMERIC MIXT/");
		r.propertyValue = r.propertyValue.replace("8 cP at 20 째C, 3.9 cP at 31.5 째C, 3.3 cP 44 째C, 2.2 cP at 60 째C","8 cP at 20 째C; 3.9 cP at 31.5 째C; 3.3 cP at 44 째C; 2.2 cP at 60 째C");
		r.propertyValue = r.propertyValue.replace("Viscosity coefficients = 4.88, 2.62, and 1.64 cP at 30, 60, and 90 째C, respectively","Viscosity coefficients = 4.88 cP at 30캜; 2.62 cP at 60캜; 1.64 cP at 90 째C, respectively");
		r.propertyValue = r.propertyValue.replace("3,300 mPa-s at 20 째C","3300 mPa-s at 20 째C");
		r.propertyValue = r.propertyValue.replace("1040 cP at 20 rpm, 572 cP at 50 rpm, 375 cP at 100 rpm (Brookfield viscometer at 25 째C)","1040 cP at 25 째C, 20 rpm; 572 cP at 25 째C, 50 rpm; 375 cP at 100 rpm (Brookfield viscometer at 25 째C)");
		
		//Flash point comma delimited issues
		r.propertyValue=r.propertyValue.replace("104 캟 (40 캜) CLOSED CUP, 150 캟 (66 캜) OPEN CUP /ANHYDROUS 76%/","104 캟 (40 캜) CLOSED CUP; 150 캟 (66 캜) OPEN CUP /ANHYDROUS 76%/");
		r.propertyValue=r.propertyValue.replace("Solution: 225 캟 (open cup), 132 캟 (closed cup)","Solution: 225 캟 (open cup); 132 캟 (closed cup)");
		r.propertyValue=r.propertyValue.replace("91 캟 (n-), 106 캟 (all isomers), 69 캟 (iso-)","91 캟 (n-); 106 캟 (all isomers); 69 캟 (iso-)");
		r.propertyValue=r.propertyValue.replace("Anhydrous: 35 캜 (closed cup), 38 캜 (open cup); 88% solution: 42 캜","Anhydrous: 35 캜 (closed cup); 38 캜 (open cup)");
		r.propertyValue=r.propertyValue.replace("97 캜 c.c., 102 캜 o.c.","97 캜 c.c.; 102 캜 o.c.");
		r.propertyValue=r.propertyValue.replace("Flash point: 27.8 캜 (Tag open cup), 31.1 캜 (Tag closed cup)","Flash point: 27.8 캜 (Tag open cup); 31.1 캜 (Tag closed cup)");
		r.propertyValue=r.propertyValue.replace("111.11 캜 c.c., 115 캜 o.c.","111.11 캜 c.c.; 115 캜 o.c.");
		r.propertyValue=r.propertyValue.replace("39 캜 (closed cup), 24 캜 (open cup)","39 캜 (closed cup); 24 캜 (open cup)");
		r.propertyValue=r.propertyValue.replace("54 캜 c.c., 57 캜 o.c.","54 캜 c.c.; 57 캜 o.c.");
		r.propertyValue=r.propertyValue.replace("-19 캜 (Closed cup), -15 캜 (Open cup)","-19 캜 (Closed cup); -15 캜 (Open cup)");
		r.propertyValue=r.propertyValue.replace("24.4 캜 (Tag open cup), 17.8 캜 (Tag closed cup)","24.4 캜 (Tag open cup); 17.8 캜 (Tag closed cup)");
		r.propertyValue=r.propertyValue.replace("43 캜 c.c., 57.2 캜 o.c.","43 캜 c.c.; 57.1 캜 o.c.");
		r.propertyValue=r.propertyValue.replace("46 캜 c.c., 52 캜 o.c.","46 캜 c.c.; 52 캜 o.c.");
		r.propertyValue=r.propertyValue.replace("33.9 캜 (open cup), 43.3 캜 (closed cup)","33.9 캜 (open cup); 43.3 캜 (closed cup)");
		r.propertyValue=r.propertyValue.replace("68 캜 c.c., 77 캜 o.c.","68 캜 c.c.; 77 캜 o.c.");
		r.propertyValue=r.propertyValue.replace("110 캜  (open cup) ... 118 캜 (closed cup)", "110 캜  (open cup); 118 캜 (closed cup)");
		//Vapor Pressure Comma Delimited Issues
		r.propertyValue=r.propertyValue.replace("1 Pa at 1517 캜, 10 Pa at 1687 캜, 100 Pa at 1982 캜, 1 kPa at 2150 �", "1 Pa at 1517 캜; 10 Pa at 1687 캜; 100 Pa at 1982 캜; 1 kPa at 2150 캜");
		r.propertyValue=r.propertyValue.replace("Vapor pressure: 120 mm Hg at 20 캜, 190 mm Hg at 30 캜", "Vapor pressure: 120 mm Hg at 20 캜; 190 mm Hg at 30 캜");
		r.propertyValue=r.propertyValue.replace("VP: 1 Pa at -158 캜 (solid), 10 Pa at -147 캜 (solid), 100 Pa at -133.6 캜 (solid), 1 kPa at -116.6 캜 (solid), 10 kPa at -94.4 캜 (solid), 100 kPa at -64.1 캜 (solid)", "VP: 1 Pa at -158 캜 (solid); 10 Pa at -147 캜 (solid); 100 Pa at -133.6 캜 (solid); 1 kPa at -116.6 캜 (solid); 10 kPa at -94.4 캜 (solid); 100 kPa at -64.1 캜 (solid)");
		r.propertyValue=r.propertyValue.replace("Vapor pressure: cis-isomers 2.9X10-3 mPa at 25 캜, trans-isomers 9.2X10-4 Pa at 25 캜", "Vapor pressure: cis-isomers 2.9X10-3 mPa at 25 캜; trans-isomers 9.2X10-4 Pa at 25 캜");
		r.propertyValue=r.propertyValue.replace("2.13 kPa at 150 캜 /0.000246 mm Hg at 25 캜/ (extrapolated)", "2.13 kPa at 150 캜 ; 0.000246 mm Hg at 25 캜 (extrapolated)");
		r.propertyValue=r.propertyValue.replace("Vapor pressure: 10 mm Hg at -72.3 캜; 1 mm Hg at -95.4 캜; 40 mm Hg at -53.7 캜, 100 mm Hg at -39.1 캜; 400 mm Hg at -12.0 캜", "Vapor pressure: 10 mm Hg at -72.3 캜; 1 mm Hg at -95.4 캜; 40 mm Hg at -53.7 캜; 100 mm Hg at -39.1 캜; 400 mm Hg at -12.0 캜");
		r.propertyValue=r.propertyValue.replace("VP: 0.5 mm Hg at 98-100 캜, 2 mm Hg at 138-140 캜, 11 mm Hg at 154-156 캜", "VP: 0.5 mm Hg at 98-100 캜; 2 mm Hg at 138-140 캜; 11 mm Hg at 154-156 캜");
		r.propertyValue=r.propertyValue.replace("1 mmHg at -36.9 캟, 100 mmHg at 96.3 캟, 760 mmHg at 192.2 캟", "1 mmHg at -36.9 캟; 100 mmHg at 96.3 캟; 760 mmHg at 192.2 캟");
		r.propertyValue=r.propertyValue.replace("Vapor pressure = 0.15 kPa at 20 캜, 0.48 kPa at 45 캜", " Vapor pressure = 0.15 kPa at 20 캜; 0.48 kPa at 45 캜");
		r.propertyValue=r.propertyValue.replace("14 mmHg at 36 캟, 26.2 mmHg at 73 캟, 67 mmHg at 118 캟", "14 mmHg at 36 캟; 26.2 mmHg at 73 캟; 67 mmHg at 118 캟");
		r.propertyValue=r.propertyValue.replace("34.3 mm Hg at 25 캜 (cis isomer), 23.0 mm Hg at 25 캜 (trans isomer)", "34.3 mm Hg at 25 캜 (cis isomer); 23.0 mm Hg at 25 캜 (trans isomer)");
		r.propertyValue=r.propertyValue.replace("VP: 0.16, 0.40, 1.6 and 2.7 mm Hg at 0, 10, 30 and 40 캜, respectively", "VP: 0.16 mm Hg at 0 캜; 0.40 mm Hg at 10 캜; 1.6 mm Hg at 30 캜; 2.7 mm Hg at 40 캜");
		r.propertyValue=r.propertyValue.replace("Vapor pressure: 6.07 mm Hg at 20 캜, 20 mm Hg at 26.6 캜", "Vapor pressure: 6.07 mm Hg at 20 캜; 20 mm Hg at 26.6 캜");
		r.propertyValue=r.propertyValue.replace("Vapor pressure: 760 mm Hg at -21.0 캜, 10 mm Hg at -76.8 캜 (solid), 40 mm Hg at -62.7 캜 (solid), 100 mm Hg at -51.8 캜 (solid)", "Vapor pressure: 760 mm Hg at -21.0 캜; 10 mm Hg at -76.8 캜 (solid); 40 mm Hg at -62.7 캜 (solid); 100 mm Hg at -51.8 캜 (solid)");
		r.propertyValue=r.propertyValue.replace("10 kPa at -71.3 캜, 100 kPa at -33.6 캜 (liquid)", "10 kPa at -71.3 캜; 100 kPa at -33.6 캜 (liquid)");
		r.propertyValue=r.propertyValue.replace("Vapor pressure: 1 Pa at -139 캜, 10 Pa at -127 캜, 100 Pa at -112 캜", "Vapor pressure: 1 Pa at -139 캜; 10 Pa at -127 캜; 100 Pa at -112 캜");
		r.propertyValue=r.propertyValue.replace("<4.3X10-5 mm Hg at 25 캜, <1.1X10-5 mm Hg at 20 캜 /OECD Guideline 104/", "4.3X10-5 mm Hg at 25 캜; 1.1X10-5 mm Hg at 20 캜 /OECD Guideline 104/");
		r.propertyValue=r.propertyValue.replace("Vapor pressure determined by gas phase analysis: 0.013, 0.025, 0.13, & 0.29 mm Hg at 0, 10, 30, & 40 캜, respectively", "Vapor pressure determined by gas phase analysis: 0.013 at 0 캜; 0.025 at 10 캜; 0.13 at 30 캜; 0.29 mm Hg at 40 캜");
		r.propertyValue=r.propertyValue.replace("Vapor pressure = 0.06 atm at 0 캜, 0.11 atm at 10 캜, 0.173 atm at 20 캜, 0.26 atm at 30 캜", "Vapor pressure = 0.06 atm at 0 캜; 0.11 atm at 10 캜; 0.173 atm at 20 캜; 0.26 atm at 30 캜");
		//Water solubility comma delimited issues
		r.propertyValue=r.propertyValue.replace("IN WATER: 102 G/100 CC @ 0 캜, 531 G/100 CC @ 80 캜", "IN WATER: 102 G/100 CC @ 0 캜; 531 G/100 CC @ 80 캜");
		r.propertyValue=r.propertyValue.replace("In water: 0.25% at 30 캜, 3.8% at 100 캜", "In water: 0.25% at 30 캜; 3.8% at 100 캜");
		r.propertyValue=r.propertyValue.replace("In water, 0.22 (pH 4), 28.3 (pH 7) (both in g/L, 20 캜)", "In water, 0.22 g/L (20 캜, pH 4); 28.3 g/L (20 캜, pH 7)");
		r.propertyValue=r.propertyValue.replace("In water, 116 (pH 5), >626 (pH 7), >628 (pH 9) (all in g/l, 25 캜)", "In water, 116 g/L (25 캜, pH 5); >626 g/L (25 캜, pH 7); >628 g/L (25 캜, pH 9)");
		r.propertyValue=r.propertyValue.replace("Water solubility: 120 mg/L at 25 캜, 350 mg/L at 50 캜, 3200 mg/L at 100 캜", "Water solubility: 120 mg/L at 25 캜; 350 mg/L at 50 캜; 3200 mg/L at 100 캜");
		r.propertyValue=r.propertyValue.replace("In water, 12 mg/L at 20 캜, 22 mg/L at 25 캜, and 23 mg/L at 30 캜.", "In water, 12 mg/L at 20 캜; 22 mg/L at 25 캜; 23 mg/L at 30 캜.");
		r.propertyValue=r.propertyValue.replace("In water, 3.34 mg/L at 20 캜, 4.46 mg/L at 25 캜", "In water, 3.34 mg/L at 20 캜; 4.46 mg/L at 25 캜");
		r.propertyValue=r.propertyValue.replace("In water, 3 ppm (pH 5),184 ppm (pH 7) at 25 캜", "In water, 3 ppm (pH 5) at 25 캜; 184 ppm (pH 7) at 25 캜");
		r.propertyValue=r.propertyValue.replace("In water, 17.6 (pH 5), 1627 (pH 7), 482 (pH 9) (all in mg/L, 20 캜)", "In water, 17.6 mg/L (20 캜, pH 5); 1627 mg/L (20 캜, pH 7); 482 mg/L (20 캜, pH 9)");
		r.propertyValue=r.propertyValue.replace("Solubility in water = 2.61 ppm at pH 5.9, 3.21 ppm at pH 4, 2.39 ppm at pH 7, and 2.32 ppm at pH 10 /Technical product/", "Solubility in water = 2.61 ppm at pH 5.9; 3.21 ppm at pH 4; 2.39 ppm at pH 7; 2.32 ppm at pH 10 /Technical product/");
		r.propertyValue=r.propertyValue.replace("In water, 0.102 mg/L (pH 5, 7) , 0.135 mg/L (pH 9)", "In water, 0.102 mg/L (pH 5, 7); 0.135 mg/L (pH 9)");
		r.propertyValue=r.propertyValue.replace("IN WATER @ PH 7.1: 0.0041 G/100 ML @ 25 캜, 0.008 G/100 ML @ 37 캜; SOL IN SOLN OF ALKALI HYDROXIDES; SPARINGLY SOL IN ETHER, CHLOROFORM; SLIGHTLY SOL IN ETHANOL", "IN WATER @ PH 7.1: 0.0041 G/100 ML @ 25 캜; 0.008 G/100 ML @ 37 캜, pH 7.1; SOL IN SOLN OF ALKALI HYDROXIDES; SPARINGLY SOL IN ETHER, CHLOROFORM; SLIGHTLY SOL IN ETHANOL");
		r.propertyValue=r.propertyValue.replace("In water, 63 mg/L (pH 5), 5850 mg/L (pH 7), and 10546 mg/L (pH 9)", "In water, 63 mg/L (pH 5), 5850 mg/L (pH 7); 10546 mg/L (pH 9)");
		r.propertyValue=r.propertyValue.replace("In water (20 캜) = 5 mg/l (pH 5), 6.7 mg/l (pH 6.5), 9800 mg/l (pH 9)", "In water (20 캜) = 5 mg/l (pH 5); 6.7 mg/l (pH 6.5); 9800 mg/l (pH 9)");
		r.propertyValue=r.propertyValue.replace("In pure water, 120 mg/L at 24 캜; in buffered creek water at 24 캜, 190 mg/L (pH 6.5), 230 mg/l (pH 7.5), 260 mg/L (pH 8.5)", "In pure water, 120 mg/L at 24 캜; in buffered creek water at 24 캜, 190 mg/L (pH 6.5); 230 mg/l (24 캜, pH 7.5); 260 mg/L (24 캜, pH 8.5)");
		r.propertyValue=r.propertyValue.replace("1 g dissolves in 0.9 mL water at room temperature, 0.2 mL water at 80 캜", "1 g dissolves in 0.9 mL water at room temperature (70캟); 1 g dissolves in 0.2 mL water at 80 캜");
		r.propertyValue=r.propertyValue.replace("Solubility in water: 3.3 (pH 5), 243 (pH 7), 5280 (pH 9) (all in mg/l)", "Solubility in water: 3.3 mg/l (pH 5); 243 mg/l (pH 7); 5280 mg/l (pH 9)");
		r.propertyValue=r.propertyValue.replace("137.8 g/100 cc water @ 0 캜, 1270 g/100 cc water @ 100 캜, 100 g/100 cc alcohol @ 12.5 캜, very slightly sol in liq ammonia /Cupric nitrate trihydrate/", "137.8 g/100 cc water @ 0 캜; 1270 g/100 cc water @ 100 캜; 100 g/100 cc alcohol @ 12.5 캜, very slightly sol in liq ammonia /Cupric nitrate trihydrate/");
		r.propertyValue=r.propertyValue.replace("Sparingly soluble in water: 3.3 ml/100 ml at 0 캜, 2.3 ml/100 ml at 20 캜", "Sparingly soluble in water: 3.3 ml/100 ml at 0 캜; 2.3 ml/100 ml at 20 캜");
		r.propertyValue=r.propertyValue.replace("In water, 0.64 g/100 g at 20 캜, 0.76 g/100 g at 25 캜, 1.27 g/100 g at 50 캜, 2.45 g/100 g at 100 캜.", "In water, 0.64 g/100 g at 20 캜; 0.76 g/100 g at 25 캜; 1.27 g/100 g at 50 캜; 2.45 g/100 g at 100 캜.");
		r.propertyValue=r.propertyValue.replace("In water (mg/L at 25 캜), 5 (pH 5.1), 67 (pH 6.1), 308 (pH 7)", "In water (mg/L at 25 캜), 5 (pH 5.1); 67 mg/L (25 캜, pH 6.1); 308 mg/L (25 캜, pH 7)");
		r.propertyValue=r.propertyValue.replace("In water, 429 mg/L (pH 7), 3936 mg/L (pH 9) at 25 캜", "In water, 429 mg/L (pH 7) at 25 캜; 3936 mg/L (pH 9) at 25 캜");
		r.propertyValue=r.propertyValue.replace("In water (mg/L at 20 캜), 0.37 (pH 5), 160 (pH 7), 2200 mg/L (pH 9)", "In water (mg/L at 20 캜), 0.37 (pH 5); 160 mg/L (20 캜, pH 7); 2200 mg/L (20 캜, pH 9)");
		r.propertyValue=r.propertyValue.replace("In water: 267 g/100 ml @ 10 캜, 620 G/100 ml @ 60 캜", "In water: 267 g/100 ml @ 10 캜; 620 G/100 ml @ 60 캜");
		r.propertyValue=r.propertyValue.replace("In water at 25 캜, 51 mg/L (pH 5), 118 mg/L (pH 7), 900 mg/L (pH 9)", "In water at 25 캜, 51 mg/L (pH 5); 118 mg/L (pH 7)at 25 캜; 900 mg/L (pH 9) at 25 캜");
		r.propertyValue=r.propertyValue.replace("In water, 3490 mg/L at 25 deg, 2790 mg/L at 20 캜, 3790 mg/L at 30 캜", "In water, 3490 mg/L at 25 degC; 2790 mg/L at 20 캜; 3790 mg/L at 30 캜");
		r.propertyValue=r.propertyValue.replace("In water, 29 mg/L (pH 4.5), 87 mg/L (pH 5.0), 4000 mg/L (pH 6.8), 43000 mg/L (pH 7.7) at 25 캜", "In water, 29 mg/L (pH 4.5), 87 mg/L (pH 5.0) at 25 캜; 4000 mg/L (pH 6.8) at 25 캜; 43000 mg/L (pH 7.7) at 25 캜");
		r.propertyValue=r.propertyValue.replace("Slowly sol in water: ~1% at 20 캜, ~1.45% at 30 캜, ~1.94% at 40 캜", "Slowly sol in water: ~1% at 20 캜; ~1.45% at 30 캜; ~1.94% at 40 캜");
		r.propertyValue=r.propertyValue.replace("SOL IN WATER: 17.2 G/100 ML @ 18 캜, 21.2 @ 25 캜; EASILY SOL IN ALC, KETONES, ETHER, ACETONE, ORG SOLVENTS, CHLORINATED & AROMATIC HYDROCARBONS; SLIGHTLY SOL IN PENTANE, PETROLEUM ETHER, LOWER PARAFFINS", "SOL IN WATER: 17.2 G/100 ML @ 18 캜; 21.2 @ 25 캜; EASILY SOL IN ALC, KETONES, ETHER, ACETONE, ORG SOLVENTS, CHLORINATED & AROMATIC HYDROCARBONS; SLIGHTLY SOL IN PENTANE, PETROLEUM ETHER, LOWER PARAFFINS");
		r.propertyValue=r.propertyValue.replace("9.5 to 13 mg/L at 20 캜 in distilled water, 75.5 mg/L at 20 캜 in salt water", "9.5 to 13 mg/L at 20 캜 in distilled water; 75.5 mg/L at 20 캜 in salt water");
		r.propertyValue=r.propertyValue.replace("2,040 mg/L at pH 7 and 18,300 mg/L at pH 9, all at 20 캜", "2,040 mg/L at 20 캜,pH 7; 18,300 mg/L at 20 캜, pH 9");
		r.propertyValue=r.propertyValue.replace("Solubility in water 4.0 g/100 ml water at 15 캜. Solubility in water 4.3 g/100 ml water at 25 캜. Solubility in water 5.0 g/100 ml water at 100 캜", "Solubility in water 4.0 g/100 ml water at 15 캜; Solubility in water 4.3 g/100 ml water at 25 캜; Solubility in water 5.0 g/100 ml water at 100 캜");
		r.propertyValue=r.propertyValue.replace("In water, 1.50X10+3 mg/L at 29 캜 and 1.92X10+3 mg/L at 37 캜; solubility increases with increasing pH", "In water, 1.50X10+3 mg/L at 29 캜; 1.92X10+3 mg/L at 37 캜; solubility increases with increasing pH");
		r.propertyValue=r.propertyValue.replace("Solubility in water = 6.4, 7.6, 8.7, 10.0, 11.3, 12.7, 14.2, 16.5, and 19.1 g/100g solution at 0, 10, 20, 30, 40, 50, 60, 80, and 100 캜, respectively; Solubility in water = 6.9, 8.2, 9.6, 11.1, 12.7, 14.5, 16.5, 19.7, and 23.6 g/100g H2O at 0, 10, 20, 30, 40, 50, 60, 80, and 100 캜, respectively. Solubility is lower in the presence of sodium carbonate", "Solubility in water = 6.4 g/100g at 0 캜; 7.6 g/100g at 10 캜; 8.7 g/100g at 20 캜; 10.0 g/100g at 30  캜; 11.3 g/100g at 40 캜; 12.7 g/100g at 50 캜; 14.2 g/100g at 60 캜; 16.5 g/100g at 80 캜; 19.1 g/100g solution at 100 캜; Solubility in water = 6.9 g/100g at 0 캜; 8.2 g/100g at 10 캜; 9.6 g/100g at 20 캜; 11.1 g/100g at 30 캜; 12.7 g/100g at 40 캜; 14.5 g/100g at 50 캜; 16.5 g/100g at 60 캜; 19.7 g/100g at 80 캜; 23.6 g/100g H2O at 100 캜. Solubility is lower in the presence of sodium carbonate");
		r.propertyValue=r.propertyValue.replace("In water (25 캜), 18.4 mg/L at pH = 5, 0.221 mg/L at pH 7, 0.189 mg/L at pH 9", "In water (25 캜), 18.4 mg/L at pH = 5; 0.221 mg/L at 25 캜, pH 7; 0.189 mg/L at 25 캜, pH 9");
		r.propertyValue=r.propertyValue.replace("0.209 G/100 CC & 0.1619 G/100 CC WATER AT 30 & 100 캜; SOL IN AMMONIUM SALTS, SODIUM THIOSULFATE & GLYCERINE", "0.209 G/100 CC AT 30 캜; 0.1619 G/100 CC WATER AT 100 캜; SOL IN AMMONIUM SALTS, SODIUM THIOSULFATE & GLYCERINE");
		r.propertyValue=r.propertyValue.replace("21% (wt/vol) at 5 캜 (water), 29% (wt/vol) at 40 캜 (water), 33% (wt/vol at 25 캜 (water)", "21% (wt/vol) at 5 캜 (water); 29% (wt/vol) at 40 캜 (water); 33% (wt/vol at 25 캜 (water)");
		r.propertyValue=r.propertyValue.replace("Solubility in water at 0, 10, 20 and 30 캜 is 6, 8.5, 17 and 28 wt %, respectively.", "Solubility in water is 6 wt % at 0 캜; 8.5 wt % at 10 캜; 17 wt % at 20 캜; 28 wt % at 30 캜");
		r.propertyValue=r.propertyValue.replace("In water at 25 캜: 2,180 mg/L (cis isomer), 2,320 mg/L (trans isomer)", "In water at 25 캜: 2,180 mg/L (cis isomer); 2,320 mg/L (trans isomer)");
		r.propertyValue=r.propertyValue.replace("0.085 G & 0.096 G/100 CC WATER @ 18 캜 & 23 캜", "0.085 G/100 CC WATER @ 18 캜; 0.096 G/100 CC WATER 23 캜");
		r.propertyValue=r.propertyValue.replace("WATER: 119 G/100 ML @ 0 캜, 170.15 G/100 ML @ 100 캜; SLIGHTLY SOL IN ALCOHOL", "WATER: 119 G/100 ML @ 0 캜; 170.15 G/100 ML @ 100 캜; SLIGHTLY SOL IN ALCOHOL");
		r.propertyValue=r.propertyValue.replace("In water, 7.85X10+3 mg/L (distilled water); 188 g/L at pH 5, 143 g/L ay pH 7; 157 g/L at pH 9, all at 20 캜", "In water, 7.85X10+3 mg/L (distilled water) at 20 캜; 188 g/L at 20 캜, pH 5; 143 g/L at 20 캜, pH 7; 157 g/L at 20 캜, pH 9");
		r.propertyValue=r.propertyValue.replace("Solubility in water: 0.23 g/L at 0 캜, 11.4 g/L at 25 캜, 17.1 g/L at 50 캜, 27.95 g/L at 75 캜, 49.9 g/L at 100 캜", "Solubility in water: 0.23 g/L at 0 캜; 11.4 g/L at 25 캜; 17.1 g/L at 50 캜; 27.95 g/L at 75 캜; 49.9 g/L at 100 캜");
		r.propertyValue=r.propertyValue.replace("SOL IN WATER (G/L): 15 (25 캜), 23 (40 캜), 30 (50 캜), 55 (70 캜); SOL IN CARBON TETRACHLORIDE, PETROLEUM ETHER, NAPHTHA, XYLENE, DIBUTYL PHTHALATE, LIQUID PETROLATUM, ACETONE, ALCOHOL, VEGETABLE OILS; VERY SOL IN WATER-MISCIBLE ORGANIC SOLVENTS", "SOL IN WATER: 15 (G/L) (25 캜); 23 G/L (40 캜); 30 G/L (50 캜); 55 G/L (70 캜); SOL IN CARBON TETRACHLORIDE, PETROLEUM ETHER, NAPHTHA, XYLENE, DIBUTYL PHTHALATE, LIQUID PETROLATUM, ACETONE, ALCOHOL, VEGETABLE OILS; VERY SOL IN WATER-MISCIBLE ORGANIC SOLVENTS");
		r.propertyValue=r.propertyValue.replace("IN WATER: 19.6 G/100 CC @ 14.5 캜, 83.3 G/100 CC @ 100 캜; IN ALCOHOL: 1.63 G/100 CC @ 25 캜; INSOL IN ETHER", "IN WATER: 19.6 G/100 CC @ 14.5 캜; 83.3 G/100 CC @ 100 캜; IN ALCOHOL: 1.63 G/100 CC @ 25 캜; INSOL IN ETHER");
		r.propertyValue=r.propertyValue.replace("In water, 1.067, 0.965, 0.914, and 0.896 g/100 g water at 0, 10, 20, and 30 캜, respectively", "In water, 1.067 g/100 g water at 0 캜; 0.965 g/100 g water at 10 캜; 0.914 g/100 g water at 20 캜; 0.896 g/100 g water at 30 캜");
		r.propertyValue=r.propertyValue.replace("Milbemycin A3: In water 0.88 ppm (20 캜). Milbemycin A4: In water 7.2 ppm (20 캜)", "Milbemycin A3: In water 0.88 ppm (20 캜); Milbemycin A4: In water 7.2 ppm (20 캜)");
		r.propertyValue=r.propertyValue.replace("In water, >1.14X10+4 at pH 5, 2.48X10-3 at pH 7, 0.180 at pH 9 (all in g/L at 20 캜)", "In water, >1.14X10+4 g/L at 20 캜, pH 5; 2.48X10-3 g/L at 20 캜, pH 7; 0.180 at pH 9 (all in g/L at 20 캜)");
		r.propertyValue=r.propertyValue.replace("In water, 0.027 g/L at 25 캜 (pH 5); 2.1 (pH 7) (g/L, 25 캜)", "");
		r.propertyValue=r.propertyValue.replace("One gram dissolves in 20.8 mL water at 30 캜, in 38.5 mL at 18 캜, in 56.7 mL at 0 캜", "One gram dissolves in 20.8 mL water at 30 캜; One gram dissolves in 38.5 mL at 18 캜; One gram dissolves in 56.7 mL at 0 캜");
		r.propertyValue=r.propertyValue.replace("In water, 32 mg/L (pH 5), 815 mg/L (pH 7), 13,500 mg/L (pH 8.2) at 25 캜", "In water, 32 mg/L at 25 캜 (pH 5); 815 mg/L at 25 캜 (pH 7); 13,500 mg/L (pH 8.2) at 25 캜");
		r.propertyValue=r.propertyValue.replace("In water, 4.88 mg/L at 20 캜, 3.27 mg/L at 37 캜 (column elution method)", "In water, 4.88 mg/L at 20 캜; 3.27 mg/L at 37 캜 (column elution method)");
		r.propertyValue=r.propertyValue.replace("Water = 89.4 ppm (Spinosyn A) and 0.495 ppm (Spinosyn D)", "Water = 89.4 ppm (Spinosyn A); 0.495 ppm (Spinosyn D)");
		r.propertyValue=r.propertyValue.replace("In water at 25 캜: 3.48X10+3 mg/L at pH5; 2.95X10+3 mg/L at pH 7, 3.96X10+3 mg/L at pH 9", "In water at 25 캜: 3.48X10+3 mg/L at pH5; 2.95X10+3 mg/L at pH 7; 3.96X10+3 mg/L at pH 9");
		r.propertyValue=r.propertyValue.replace("1 g dissolves in: 4000 mL water at pH 6.5, 5 mL water at pH 7.5, 40 g methanol, 30 g ethanol, 10 g acetone, 1370 g ether, 2800 g chloroform; practically insol in benzene", "1 g dissolves in: 4000 mL water at pH 6.5; 5 mL water at pH 7.5, 40 g methanol, 30 g ethanol, 10 g acetone, 1370 g ether, 2800 g chloroform; practically insol in benzene");
		r.propertyValue=r.propertyValue.replace("pH-dependent solubility ranging from <0.1 mg/mL at pH 5-7 to over 1 mg/mL at pH 2 under ambient conditions", "pH-dependent solubility ranging from <0.1 mg/mL at pH 5-7; 1 mg/mL at pH 2 under ambient conditions");
//		r.propertyValue=r.propertyValue.replace("Solubility in water as weight %: 24%, 0 캜; 26.3%, 20 캜; 28.9%, 40 캜; 31.7%, 60 캜; 34.4%, 80 캜, 26.2%, 100 캜", "Solubility in water as weight %: 24% at 0 캜; 26.3% at 20 캜; 28.9% at 40 캜; 31.7% at 60 캜; 34.4% at 80 캜; 26.2% at 100 캜");
		r.propertyValue=r.propertyValue.replace("Soluble in acetone, ether, ammonia, in water, 97 g/100 cc at 25 캜, 145.6 g/100 cc at 100 캜; in alcohol, 10 g/100 cc at 78 캜","Soluble in acetone, ether, ammonia, in water, 97 g/100 cc at 25 캜; 145.6 g/100 cc at 100 캜; in alcohol, 10 g/100 cc at 78 캜");
		r.propertyValue=r.propertyValue.replace("12 mg/100 ml in water @ 20 캜, 27 mg/100 ml @ 37 캜","12 mg/100 ml in water @ 20 캜; 27 mg/100 ml @ 37 캜");
		r.propertyValue=r.propertyValue.replace("SOLUBILITY IN WATER: 35% @ 0 캜, 41% @ 20 캜, 48% @ 50 캜","SOLUBILITY IN WATER: 35% @ 0 캜; 41% @ 20 캜; 48% @ 50 캜");
		r.propertyValue=r.propertyValue.replace("Sol in water (in g/100 ml water): 20.8 g @ 6 캜, 3.5 g @ 20 캜, 1.8 g @ 100 캜","Sol in water (in g/100 ml water): 20.8 g @ 6 캜; 3.5 g/100 ml @ 20 캜; 1.8 g/100 ml @ 100 캜");
		r.propertyValue=r.propertyValue.replace("In water, 5.46X10+5 mg/L at 30 캜, 4.79X10+5 mg/L at 20 캜","In water, 5.46X10+5 mg/L at 30 캜 4.79X10+5 mg/L at 20 캜");
		r.propertyValue=r.propertyValue.replace("In water, alpha-form = 0.32:, beta-form = 0.33 mg/L at 22 캜","In water, alpha-form = 0.32; beta-form = 0.33 mg/L at 22 캜");
		r.propertyValue=r.propertyValue.replace("Solubility in water at 25 캜, 300,000 mg/L at 20 캜 and 340,000 mg/L at 25 캜 /Maleic hydrazide potassium salt/","Solubility in water: 300,000 mg/L at 20 캜; 340,000 mg/L at 25 캜 /Maleic hydrazide potassium salt/");
		r.propertyValue=r.propertyValue.replace("In water, 9.30 g/100 g at 0 캜, 39.84 g/100 g at 65.3 캜","In water, 9.30 g/100 g at 0 캜; 39.84 g/100 g at 65.3 캜");
		r.propertyValue=r.propertyValue.replace("In water, solubility is pH dependent wtih 0.1 mg/mL at pH 7 and 0.99 mg/mL at pH 11","In water, solubility is pH dependent wtih 0.1 mg/mL at pH 7; 0.99 mg/mL at pH 11");
		r.propertyValue=r.propertyValue.replace("1 gm dissolves in 46 mL water, 5.5 mL water at 80 캜, 1.5 mL boiling water, 66 mL alcohol, 22 mL alcohol at 60 캜, 50 mL acetone, 5.5 mL chloroform, 530 mL ether, 100 mL benzene, 22 mL boiling benzene.","1 gm dissolves in 46 mL water;1 gm dissolves in 5.5 mL water at 80 캜;1 gm dissolves in 1.5 mL boiling water, 66 mL alcohol, 22 mL alcohol at 60 캜, 50 mL acetone, 5.5 mL chloroform, 530 mL ether, 100 mL benzene, 22 mL boiling benzene.");
		r.propertyValue=r.propertyValue.replace("In water, 200 mg/L at 15 캜, 1368 mg/L at 50 캜","In water, 200 mg/L at 15 캜; 1368 mg/L at 50 캜");
		r.propertyValue=r.propertyValue.replace("In water at 20 캜, 5700 mg/L (pH 5.0), 7300 mg/L (pH 9.2)","In water at 20 캜, 5700 mg/L (pH 5.0); 7300 mg/L at 20 캜 (pH 9.2)");
		r.propertyValue=r.propertyValue.replace("1 g sol in: 300 mL water at 25 캜, 100 mL water at 37 캜, 5 mL alcohol, 17 mL chloroform, 10-15 mL ether; less sol in anhydrous ether","1 g sol in: 300 mL water at 25 캜; 1 g sol in: 100 mL water at 37 캜; 5 mL alcohol, 17 mL chloroform, 10-15 mL ether; less sol in anhydrous ether");
		r.propertyValue=r.propertyValue.replace("In water at 20 캜, 0.5 g/L at pH 6, 1.5 g/L at pH 9","In water at 20 캜, 0.5 g/L at pH 6; 1.5 g/L at 20 캜, pH 9");
		r.propertyValue=r.propertyValue.replace("In water, 788 g/L at 25 캜, 3926 g/L at 97.5 캜","In water, 788 g/L at 25 캜; 3926 g/L at 97.5 캜");
		r.propertyValue=r.propertyValue.replace("Fluffy white powder, mp 238 캜. Solubility in water (mg/100 mL) at 30 캜: 220 (ph 3.8), 280 (pH 5.5), 290 (pH 6.7), 264 (pH 7.9), 244 (pH 9.2-10.1) /Hydrochloride/","Fluffy white powder. Solubility in water (mg/100 mL) at 30 캜: 220 (ph 3.8); 280 mg/100 mL at 30 캜 (pH 5.5); 290 mg/100 mL at 30 캜 (pH 6.7); 264 mg/100 mL) at 30 캜 (pH 7.9); 244 mg/100 mL) at 30 캜 (pH 9.2-10.1) /Hydrochloride/");
				
		r.propertyValue=r.propertyValue.replace("log Kow: pH 5: 6.2-6.3 (10 캜), 6-6.1 (20 캜), 5.8-6.0 (30 캜); pH 7: 5.1 (10 캜), 4.9 (20 캜), 4.8 (30 캜); pH 9: 4.9 (10 캜), 4.8 (20 캜), 4.6 (30 캜)","6.2-6.3 @ pH 5 and 10 캜; 6-6.1 @ pH 5 and 20 캜; 5.8-6.0 @ pH 5 and 30 캜; 5.1 @ pH 7 and 10 캜; 4.9 @ pH 7 and 20 캜; 4.8 @ pH 7 and 30 캜; 4.9 @ pH 9 and 10 캜; 4.8 @ pH 9 and 20 캜; 4.6 @ pH 9 and 30 캜");
		//Melting Point 12/12/2024
		r.propertyValue = r.propertyValue.replace("MP: 70-100 째C; stable toward dil mineral acids; hydrolyzed rapidly by alkalies; commercial product is a mixture of alpha-isomer, MP: 108-110 째C, and beta-isomer, MP: 208-210 째C /Technical/","MP: 70-100 째C; stable toward dil mineral acids; hydrolyzed rapidly by alkalies; commercial product is a mixture of alpha-isomer, MP: 108-110 째C; and beta-isomer, MP: 208-210 째C /Technical/");
		r.propertyValue = r.propertyValue.replace("Exists in alpha, beta, and gamma forms having mp 63 째C, 55-56 째C, and 50 째C respectively. MP for acid of commerce: 61-63 째C","mp: 63 째C in alpha form; 55-56 째C in beta form; and 50 째C in gamma form; MP for acid of commerce: 61-63 째C");
		r.propertyValue = r.propertyValue.replace("-76.5 째C (trans), -69 째C (cis)","-76.5 째C (trans); -69 째C (cis)");
		r.propertyValue = r.propertyValue.replace("6.9 째C (trans), 21 째C (cis)","6.9 째C (trans); 21 째C (cis)");
		r.propertyValue = r.propertyValue.replace("44 째F (trans-) 70 째F (cis-)","44 째F (trans-); 70 째F (cis-)");
		r.propertyValue = r.propertyValue.replace("44.4-70 째F (trans-) 70 째F (cis-)","44.4-70 째F (trans-); 70 째F (cis-)");
		r.propertyValue = r.propertyValue.replace("15 째C for pure glyoxal and approximately -10 째C for the 40% solution","15 째C for pure glyoxal; approximately -10 째C for the 40% solution");
		r.propertyValue = r.propertyValue.replace("MP: 7 째C (ALPHA), 0 째C (BETA), 4.1 째C (GAMMA)","MP: 7 째C (ALPHA); 0 째C (BETA); 4.1 째C (GAMMA)");
		r.propertyValue = r.propertyValue.replace("5-10 째C and 20-25 째C (two forms)","5-10 째C; 20-25 째C (two forms)");
		r.propertyValue = r.propertyValue.replace("MP: 176-178 째C, resolidifying and remelting at 185-187 째C; (technical melts 150-170 째C, and again at 170-185 째C)","MP: 176-178 째C; resolidifying and remelting at 185-187 째C; (technical melts 150-170 째C, and again at 170-185 째C)");
		r.propertyValue = r.propertyValue.replace("Two crystalline modifications with melting point of 12 째C for beta-form and 46.5 째C for alpha-form","Two crystalline modifications with melting point of 12 째C for beta-form; 46.5 째C for alpha-form");
		r.propertyValue = r.propertyValue.replace("Crystals, melting point 184 to 186 째C, 188 to 189 째C (a cis-trans mixture of approx 1:5) /HYDROCHLORIDE/","Crystals, melting point 184 to 186 째C; 188 to 189 째C (a cis-trans mixture of approx 1:5) /HYDROCHLORIDE/");
		r.propertyValue = r.propertyValue.replace("Crystals, melting point 161 to 164 째C, 168 to 169 째C /MALEATE/","Crystals, melting point 161 to 164 째C; 168 to 169 째C /MALEATE/");
		r.propertyValue = r.propertyValue.replace("One form of polymorphic crystals melts at about 155 째C, the other at about 162 째C","One form of polymorphic crystals melts at about 155 째C; the other at about 162 째C");
		r.propertyValue = r.propertyValue.replace("MP: 184 째C (Lewistein), 177-178 째C (Sankyo Co)","MP: 184 째C (Lewistein); 177-178 째C (Sankyo Co)");
		r.propertyValue = r.propertyValue.replace("224.5 to 225.5 째C (also reported as 221 to 223 째C)","224.5 to 225.5 째C; (also reported as 221 to 223 째C)");
		r.propertyValue = r.propertyValue.replace("Crystals from absolute alcohol. MP: 235-237 째C, also reported as 224.5-226 째C /Fluphenazine dihydrochloride/","Crystals from absolute alcohol. MP: 235-237 째C; also reported as 224.5-226 째C /Fluphenazine dihydrochloride/");
		r.propertyValue = r.propertyValue.replace("MP: also reported as -75.24 째C and -81.53 째C for two unstable solid forms","MP: also reported as -75.24 째C; -81.53 째C for two unstable solid forms");
		r.propertyValue = r.propertyValue.replace("221 째C ... also reported as 208-209 째C","221 째C; also reported as 208-209 째C");
		r.propertyValue = r.propertyValue.replace("176 째C (also reported as mp 178-179 째C; mp 198-199 째C; mp 205-207 째C)","176 째C; (also reported as mp 178-179 째C; mp 198-199 째C; mp 205-207 째C)");
		r.propertyValue = r.propertyValue.replace("169 째C ... Also frequently reported as 90 째C from benzene (one mole of benzene of crystallization)","169 째C; Also frequently reported as 90 째C from benzene (one mole of benzene of crystallization)");
		r.propertyValue = r.propertyValue.replace("Solvated crystals from ethyl acetate: MP = 162-163 째C and 233-234 째C; solvated crystals from methanol or ethanol: MP: 249-250 째C","Solvated crystals from ethyl acetate: MP = 162-163 째C; and 233-234 째C; solvated crystals from methanol or ethanol: MP: 249-250 째C");
		r.propertyValue = r.propertyValue.replace("14 째C (cis-isomer), 71 째C (trans-isomer)","14 째C (cis-isomer); 71 째C (trans-isomer)");
		r.propertyValue = r.propertyValue.replace("43 째C (cis-isomer), 67 째C (trans-isomer)","43 째C (cis-isomer); 67 째C (trans-isomer)");
		r.propertyValue = r.propertyValue.replace("MP: 55 째C (ALPHA), 64.5 째C (BETA'), 73 째C (BETA)","MP: 55 째C (ALPHA); 64.5 째C (BETA'); 73 째C (BETA)");
		r.propertyValue = r.propertyValue.replace("120-130 째C (crystals from ethyl acetatetoluene) and 119-121.5 째C (crystals from acetonitrile)","120-130 째C (crystals from ethyl acetatetoluene); 119-121.5 째C (crystals from acetonitrile)");
		r.propertyValue = r.propertyValue.replace("86-87 째C, also reported as mp 96 째C","86-87 째C; also reported as mp 96 째C");
		r.propertyValue = r.propertyValue.replace("147-149 째C ... from hexane/ethyl acetate, mp 151-152 째C ... also reported as white crystalline solid, mp 161 째C","147-149 째C; from hexane/ethyl acetate mp 151-152 째C; also reported as white crystalline solid, mp 161 째C");
		r.propertyValue = r.propertyValue.replace("MP: 44.6-46.9 째C, also reported as ... 46 째C","MP: 44.6-46.9 째C; also reported as ... 46 째C");
		r.propertyValue = r.propertyValue.replace("275-277 째C ... /also reported as/ 272-275 째C","275-277 째C; /also reported as/ 272-275 째C");
		r.propertyValue = r.propertyValue.replace("140-143.6 째F (cis-cis) 147.2-149 째F (trans-trans) (NTP, 1992)","140-143.6 째F (cis-cis); 147.2-149 째F (trans-trans) (NTP, 1992)");
		r.propertyValue = r.propertyValue.replace("-174.6 째F (Melting point is -13.7 째F for a 39.17% weight/weight solution.) (EPA, 1998)","-174.6 째F; (Melting point is -13.7 째F for a 39.17% weight/weight solution.) (EPA, 1998)");
		//Boiling Point 12/16/2024
		r.propertyValue = r.propertyValue.replace("Boiling point = 98.9 째C at 100 mm Hg, 61 째C at 20 mm Hg, 47.4 째C at 10 mm Hg, and 9.6 째C at 1.0 mm Hg","Boiling point = 98.9 째C at 100 mm Hg; 61 째C at 20 mm Hg; 47.4 째C at 10 mm Hg; and 9.6 째C at 1.0 mm Hg");
		r.propertyValue = r.propertyValue.replace("337.5 째C at 760 mm Hg: 265 째C at 100 mm Hg: 240.5 째C at 40 mm Hg: 222 째Cat 20 mm Hg: 205.5 째C at 10 mm Hg; 191 째C at 5 mm Hg; 159.5 째C at 1.0 mm Hg","337.5 째C at 760 mm Hg; 265 째C at 100 mm Hg; 240.5 째C at 40 mm Hg; 222 째Cat 20 mm Hg; 205.5 째C at 10 mm Hg; 191 째C at 5 mm Hg; 159.5 째C at 1.0 mm Hg");
		r.propertyValue = r.propertyValue.replace("31.8 째C at 760 mm Hg, also reported as 36.5 째C /at 760 mm Hg/","31.8 째C at 760 mm Hg; also reported as 36.5 째C /at 760 mm Hg/");
		r.propertyValue = r.propertyValue.replace("Boiling point: 106-108 째C at 50 mm Hg, 71-72 째C at 10 mm Hg","Boiling point: 106-108 째C at 50 mm Hg; 71-72 째C at 10 mm Hg");
		r.propertyValue = r.propertyValue.replace("306 째C, also stated as  294 째C","306 째C; also stated as  294 째C");
		r.propertyValue = r.propertyValue.replace("203 째C (also reported as 201.030 째C)","203 째C; (also reported as 201.030 째C)");
		r.propertyValue = r.propertyValue.replace("Boiling point = 185-195 째C at 1 mbar and 140-150 째C at 0.1 mbar","Boiling point = 185-195 째C at 1 mbar; 140-150 째C at 0.1 mbar");
		r.propertyValue = r.propertyValue.replace("338 째F (Pyrethrin I), 392 째F (Pyrethrin II), 279 째F (Cinerin I), 361 째F (Cinerin II)","338 째F (Pyrethrin I); 392 째F (Pyrethrin II); 279 째F (Cinerin I); 361 째F (Cinerin II)");
		//Vapor Pressure 12/16/2024
		r.propertyValue = r.propertyValue.replace("Specific heat = 1.747 Joules/g; Vapor pressure = 0.15 kPa at 20 째C, 0.48 kPa at 45 째C", "Specific heat = 1.747 Joules/g; Vapor pressure = 0.15 kPa at 20 째C; 0.48 kPa at 45 째C");
		r.propertyValue = r.propertyValue.replace("Vapor pressure: 120 mm Hg at 20 째C, 190 mm Hg at 30 째C", "Vapor pressure: 120 mm Hg at 20 째C; 190 mm Hg at 30 째C");
		r.propertyValue = r.propertyValue.replace("Vapor pressure = 0.06 atm at 0 째C, 0.11 atm at 10 째C, 0.173 atm at 20 째C, 0.26 atm at 30 째C", "Vapor pressure = 0.06 atm at 0 째C; 0.11 atm at 10 째C; 0.173 atm at 20 째C; 0.26 atm at 30 째C");
		r.propertyValue = r.propertyValue.replace("Vapor pressure: 1 Pa at -139 째C, 10 Pa at -127 째C, 100 Pa at -112 째C; 1 kPa at -94.5 째C (solids); 10 kPa at -71.3 째C, 100 kPa at -33.6 째C (liquid)", "Vapor pressure: 1 Pa at -139 째C; 10 Pa at -127 째C; 100 Pa at -112 째C; 1 kPa at -94.5 째C (solids); 10 kPa at -71.3 째C; 100 kPa at -33.6 째C (liquid)");
		r.propertyValue = r.propertyValue.replace("VP: 1 Pa at -158 째C (solid), 10 Pa at -147 째C (solid), 100 Pa at -133.6 째C (solid), 1 kPa at -116.6 째C (solid), 10 kPa at -94.4 째C (solid), 100 kPa at -64.1 째C (solid)", "VP: 1 Pa at -158 째C (solid); 10 Pa at -147 째C (solid); 100 Pa at -133.6 째C (solid); 1 kPa at -116.6 째C (solid); 10 kPa at -94.4 째C (solid); 100 kPa at -64.1 째C (solid)");
		r.propertyValue = r.propertyValue.replace("34.3 mm Hg at 25 째C (cis isomer), 23.0 mm Hg at 25 째C (trans isomer)", "34.3 mm Hg at 25 째C (cis isomer); 23.0 mm Hg at 25 째C (trans isomer)");
		r.propertyValue = r.propertyValue.replace("VP: approx 60 Pa at 20 째C, approx 130 Pa at 30 째C, approx 520 Pa at 50 째C", "VP: approx 60 Pa at 20 째C; approx 130 Pa at 30 째C; approx 520 Pa at 50 째C");
		r.propertyValue = r.propertyValue.replace("Vapor pressure = 200 kPa at 21 째C, 669 kPa at 54 째C", "Vapor pressure = 200 kPa at 21 째C; 669 kPa at 54 째C");
		r.propertyValue = r.propertyValue.replace("VP: 0.16, 0.40, 1.6 and 2.7 mm Hg at 0, 10, 30 and 40 째C, respectively", "VP: 0.16 mm Hg at 0 째C; 0.40 mm Hg at 10 째C; 1.6 mm Hg at 30 째C; 2.7 mm Hg at 40 째C, respectively");
		r.propertyValue = r.propertyValue.replace("2.13 kPa at 150 째C /0.000246 mm Hg at 25 째C/ (extrapolated)", "2.13 kPa at 150 째C; /0.000246 mm Hg at 25 째C/ (extrapolated)");
		r.propertyValue = r.propertyValue.replace("VP: 0.5 mm Hg at 98-100 째C, 2 mm Hg at 138-140 째C, 11 mm Hg at 154-156 째C", "VP: 0.5 mm Hg at 98-100 째C; 2 mm Hg at 138-140 째C; 11 mm Hg at 154-156 째C");
		r.propertyValue = r.propertyValue.replace("1 mmHg at -36.9 째F, 100 mmHg at 96.3 째F, 760 mmHg at 192.2 째F", "1 mmHg at -36.9 째F; 100 mmHg at 96.3 째F; 760 mmHg at 192.2 째F");
		r.propertyValue = r.propertyValue.replace("1 mmHg at 211.6 째F, 0.0018 mmHg at 77 째F", "1 mmHg at 211.6 째F; 0.0018 mmHg at 77 째F");
		r.propertyValue = r.propertyValue.replace("1.1 mmHg at 122 째F, 3 mmHg at 140 째F", "1.1 mmHg at 122 째F; 3 mmHg at 140 째F");
		r.propertyValue = r.propertyValue.replace("3 mmHg at 86 째F, 760 mmHg at 410.9 째F", "3 mmHg at 86 째F; 760 mmHg at 410.9 째F");
		r.propertyValue = r.propertyValue.replace("1 mmHg@68 째F, 1.5 mmHg@77 째F", "1 mmHg@68 째F; 1.5 mmHg@77 째F");
		r.propertyValue = r.propertyValue.replace("1 mmHg at 32 째F, 5 mmHg at 76.5 째F", "1 mmHg at 32 째F; 5 mmHg at 76.5 째F");
		r.propertyValue = r.propertyValue.replace("14 mmHg at 36 째F, 26.2 mmHg at 73 째F, 67 mmHg at 118 째F", "14 mmHg at 36 째F; 26.2 mmHg at 73 째F; 67 mmHg at 118 째F");
	    // Vapor Density 12/17/2024
 		r.propertyValue = r.propertyValue.replace("kg/m�^3", "kg/cu m");
 		r.propertyValue = r.propertyValue.replace("Liquid density (174 K): 1.274 g/cu cm. Vapor density (25 째C, 1 atm): 2.849 g/L", "Liquid density (174 K): 1.274 g/cu cm; Vapor density (25 째C, 1 atm): 2.849 g/L");
		// Density 12/17/2024
 		r.propertyValue = r.propertyValue.replace("Density: 0.45 g/mL at 20 째C, apparent density (packing weight) 0.60 g/mL","Density: 0.45 g/mL at 20 째C; apparent density (packing weight) 0.60 g/mL");
 		r.propertyValue = r.propertyValue.replace("1.0459 and 1.0465 at 20 째C","1.0459; 1.0465 at 20 째C");
 		r.propertyValue = r.propertyValue.replace("1.2 at 68 째F 1.08 at 145 째C (liquid) (USCG, 1999) - Denser than water; will sink","1.2 at 68 째F; 1.08 at 145 째C (liquid) (USCG, 1999) - Denser than water; will sink");
 		r.propertyValue = r.propertyValue.replace("2.2 to 2.4 (crude), 0.05 to 0.3 (expanded) (NIOSH, 2024)","2.2 to 2.4 (crude); 0.05 to 0.3 (expanded) (NIOSH, 2024)");
 		r.propertyValue = r.propertyValue.replace("2.34 red, 2.36 violet; 2.70 black; 1.8 white-yellow (EPA, 1998)","2.34 red; 2.36 violet; 2.70 black; 1.8 white-yellow (EPA, 1998)");
 		r.propertyValue = r.propertyValue.replace("1.2 at 275 째F 1.53 at 20 째C (solid) (USCG, 1999) - Denser than water; will sink","1.2 at 275 째F; 1.53 at 20 째C (solid) (USCG, 1999) - Denser than water; will sink");
 		r.propertyValue = r.propertyValue.replace("16.65 (metal), 14.40 (powder) (NIOSH, 2024) - Denser than water; will sink","16.65 (metal); 14.40 (powder) (NIOSH, 2024) - Denser than water; will sink");
 		r.propertyValue = r.propertyValue.replace("8.15 (crystalline form)/6.95 (amorphous form)","8.15 (crystalline form); 6.95 (amorphous form)");
 		r.propertyValue = r.propertyValue.replace("1.23 (Solid at 77 째F) 1.19 (Liquid at 122 째F)","1.23 (Solid at 77 째F); 1.19 (Liquid at 122 째F)");
 		r.propertyValue = r.propertyValue.replace("1.53 (Flake) 1.20 (Molten)","1.53 (Flake); 1.20 (Molten)");
 		r.propertyValue = r.propertyValue.replace("1.23 (Solid at 77 째F) 1.19 (Liquid at 122 째F)","1.23 (Solid at 77 째F); 1.19 (Liquid at 122 째F)");
 		r.propertyValue = r.propertyValue.replace("0.799 at 140 째F (70% sol), 0.933 at 20 째C","0.799 at 140 째F (70% sol); 0.933 at 20 째C");
 		r.propertyValue = r.propertyValue.replace("16.65 (metal) 14.40 (powder)","16.65 (metal); 14.40 (powder)");
 		r.propertyValue = r.propertyValue.replace("1.503 (also given as 1.104 and 1.234)","1.503; (also given as 1.104-1.234)");
 		r.propertyValue = r.propertyValue.replace("Diammonium: 1.8 at 68.0 째F Monoammonium: 1.6 at 20 째C (USCG, 1999)", "Diammonium: 1.8 at 68.0 째F; Monoammonium: 1.6 at 20 째C (USCG, 1999)");
 		r.propertyValue = r.propertyValue.replace("The apparent density of beech charcoal is 0.45 g/mL and that of pine charcoal is 0.28 g/mL", "The apparent density of beech charcoal is 0.45 g/mL; that of pine charcoal is 0.28 g/mL");
 		r.propertyValue = r.propertyValue.replace("0.8665 at 68 째F 0.8593 at 25 째C (USCG, 1999) - Less dense than water; will float", "0.8665 at 68 째F; 0.8593 at 25 째C (USCG, 1999) - Less dense than water; will float");
 		r.propertyValue = r.propertyValue.replace("1.463 at 32 째F 1.29/1.3 at 68F for concentrations greater than 52% (EPA, 1998) - Denser than water; will sink", "1.463 at 32 째F; 1.29/1.3 at 68F for concentrations greater than 52% (EPA, 1998) - Denser than water; will sink");
 		r.propertyValue = r.propertyValue.replace("1.485 at 68 째F 1.2797 at 100 째C (Liquid) (USCG, 1999) - Denser than water; will sink", "1.485 at 68 째F; 1.2797 at 100 째C (Liquid) (USCG, 1999) - Denser than water; will sink");
 		r.propertyValue = r.propertyValue.replace("1.81 at 68 째F 1.79 at 25 째C (USCG, 1999) - Denser than water; will sink", "1.81 at 68 째F; 1.79 at 25 째C (USCG, 1999) - Denser than water; will sink");
 		r.propertyValue = r.propertyValue.replace("Relative density = 2.12, 2.18", "Relative density = 2.12; 2.18");
 		r.propertyValue = r.propertyValue.replace("2.71 at 68 째F 1.7 at 20 째C (USCG, 1999) - Denser than water; will sink", "2.71 at 68 째F; 1.7 at 20 째C (USCG, 1999) - Denser than water; will sink");
 		r.propertyValue = r.propertyValue.replace("4.84 at 59 째F 4.95 at 25 째C (USCG, 1999) - Denser than water; will sink", "4.84 at 59 째F; 4.95 at 25 째C (USCG, 1999) - Denser than water; will sink");
 		r.propertyValue = r.propertyValue.replace("8.235 at 68 째F (Stolzite) 8.46 at 68 째F (Raspite) (USCG, 1999)", "8.235 at 68 째F (Stolzite); 8.46 at 68 째F (Raspite) (USCG, 1999)");
 		// Flash Point 12/18/2024
 		r.propertyValue = r.propertyValue.replace("Anhydrous: 35 째C (closed cup), 38 째C (open cup); 88% solution: 42 째C", "Anhydrous: 35 째C (closed cup); 38 째C (open cup); 88% solution: 42 째C");
 		r.propertyValue = r.propertyValue.replace("104 째F (40 째C) CLOSED CUP, 150 째F (66 째C) OPEN CUP /ANHYDROUS 76%/", "104 째F (40 째C) CLOSED CUP; 150 째F (66 째C) OPEN CUP /ANHYDROUS 76%/");
 		r.propertyValue = r.propertyValue.replace("33.9 째C (open cup), 43.3 째C (closed cup)", "33.9 째C (open cup); 43.3 째C (closed cup)");
 		r.propertyValue = r.propertyValue.replace("Flash point: 27.8 째C (Tag open cup), 31.1 째C (Tag closed cup)", "Flash point: 27.8 째C (Tag open cup); 31.1 째C (Tag closed cup)");
 		r.propertyValue = r.propertyValue.replace("Solution: 225 째F (open cup), 132 째F (closed cup)", "Solution: 225 째F (open cup); 132 째F (closed cup)");
 		r.propertyValue = r.propertyValue.replace("24.4 째C (Tag open cup), 17.8 째C (Tag closed cup)", "24.4 째C (Tag open cup); 17.8 째C (Tag closed cup)");
 		r.propertyValue = r.propertyValue.replace("39 째C (closed cup), 24 째C (open cup)", "39 째C (closed cup); 24 째C (open cup)");
 		r.propertyValue = r.propertyValue.replace("66 째C (OPEN CUP); COMMERCIAL GRADE IS 8 째C (CLOSED CUP) & 13 째C (OPEN CUP)", "66 째C (OPEN CUP); COMMERCIAL GRADE IS 8 째C (CLOSED CUP); 13 째C (OPEN CUP)");
 		r.propertyValue = r.propertyValue.replace("110 째C  (open cup) ... 118 째C (closed cup)", "110 째C  (open cup); 118 째C (closed cup)");
 		r.propertyValue = r.propertyValue.replace("Sure Sol-170: 180 째F closed cup Sure Sol-175: 175 째F closed cup /Sure sol-170: 38% methylbiphenyls, 25% biphenyl, 37% dimethyl naphthalenes; sure sol-175 55% methylbiphenyls, 14% biphenyl, and 31% other C12+ aromatic hydrocarbons/", "Sure Sol-170: 180 째F closed cup; Sure Sol-175: 175 째F closed cup /Sure sol-170: 38% methylbiphenyls, 25% biphenyl, 37% dimethyl naphthalenes; sure sol-175 55% methylbiphenyls, 14% biphenyl, and 31% other C12+ aromatic hydrocarbons/");
 		r.propertyValue = r.propertyValue.replace("111.11 째C c.c., 115 째C o.c.", "111.11 째C c.c.; 115 째C o.c.");
 		r.propertyValue = r.propertyValue.replace("97 째C c.c., 102 째C o.c.", "97 째C c.c.; 102 째C o.c.");
 		r.propertyValue = r.propertyValue.replace("46 째C c.c., 52 째C o.c.", "46 째C c.c.; 52 째C o.c.");
 		r.propertyValue = r.propertyValue.replace("54 째C c.c., 57 째C o.c.", "54 째C c.c.; 57 째C o.c.");
 		r.propertyValue = r.propertyValue.replace("68 째C c.c., 77 째C o.c.", "68 째C c.c.; 77 째C o.c.");
 		r.propertyValue = r.propertyValue.replace("43 째C c.c., 57.2 째C o.c.", "43 째C c.c.; 57.2 째C o.c.");
 		r.propertyValue = r.propertyValue.replace("91 째F (n-), 106 째F (all isomers), 69 째F (iso-)", "91 째F (n-); 106 째F (all isomers); 69 째F (iso-)");
		//Surface Tension
 		r.propertyValue = r.propertyValue.replace("At 18 째C: 74.35 dynes/cm (2.72 wt%), 75.85 dynes/cm (5.66 wt%), 83.05 dynes/cm (16.66 wt%), 96.05 dynes/cm (30.56 wt%), 101.05 dynes/cm (35.90 wt%)", "At 18 째C: 74.35 dynes/cm (2.72 wt%); At 18 째C: 75.85 dynes/cm (5.66 wt%); At 18 째C: 83.05 dynes/cm (16.66 wt%); At 18 째C: 96.05 dynes/cm (30.56 wt%); At 18 째C: 101.05 dynes/cm (35.90 wt%)");
 		r.propertyValue = r.propertyValue.replace("38.82X10-5 N/cm at 21.1 째C, 31.70X10-5 N/cm at 88 째C", "38.82X10-5 N/cm at 21.1 째C; 31.70X10-5 N/cm at 88 째C");
 		r.propertyValue = r.propertyValue.replace("LIQUID SURFACE TENSION: EST 15 DYNES/CM= 0.015 N/M @ 20 째C LIQUID-WATER INTERFACIAL TENSION: EST 30 DYNES/CM= 0.03 N/M @ 20 째C.", "LIQUID SURFACE TENSION: EST 15 DYNES/CM= 0.015 N/M @ 20 째C; LIQUID-WATER INTERFACIAL TENSION: EST 30 DYNES/CM= 0.03 N/M @ 20 째C.");
 		//HLC
 		r.propertyValue = r.propertyValue.replace("Henry's Law constant = 5X10-10 (pH 5); 2.5X10-11 (pH 7); 3.2X10-12 (pH 9) (all in Pa-cu m/mol, 20 째C)", "Henry's Law constant = 5X10-10 Pa-cu m/mol at 20 째C (pH 5); 2.5X10-11 Pa-cu m/mol at 20 째C (pH 7); 3.2X10-12 (pH 9) (all in Pa-cu m/mol, 20 째C)");
 		//LogKow
 		r.propertyValue = r.propertyValue.replace("log Kow = 2.06 (cis-isomer), 2.03 (trans-isomer) at 25 째C", "log Kow = 2.06 (cis-isomer) at 25 째C; 2.03 (trans-isomer) at 25 째C");
 		r.propertyValue = r.propertyValue.replace("log Kow = 1.49 (pH 7); 1.04 (pH 5): 1.20 (pH 9), all at 25 째C", "log Kow = 1.49 (pH 7) at 25 째C; 1.04 (pH 5) at 25 째C: 1.20 (pH 9), all at 25 째C");
 		r.propertyValue = r.propertyValue.replace("log Kow (23 째C) = 0.276 (pH 4): -1.362 (pH 7); -1.580 (pH 9)", "log Kow (23 째C) = 0.276 (pH 4);log Kow (23 째C) = -1.362 (pH 7);log Kow (23 째C) = -1.580 (pH 9)");
 		r.propertyValue = r.propertyValue.replace("log Kow = 4.05 (unbuffered, 20 째C), 4.16 (pH 4), 3.82 (pH 7), 2.00 (pH 9)", "log Kow = 4.05 (unbuffered, 20 째C); 4.16 (pH 4); 3.82 (pH 7); 2.00 (pH 9)");
 		r.propertyValue = r.propertyValue.replace("log Kow = 1.1 (pH 5.0), -0.59 (pH 6.9), -1.8 (pH 9.0) at 25 째C", "log Kow = 1.1 (pH 5.0); -0.59 (pH 6.9); -1.8 (pH 9.0) at 25 째C");
 		r.propertyValue = r.propertyValue.replace("log Kow = 1.5 (pH 5.0), -0.21 (pH 6.9), -0.76 (pH 9.0) at 25 째C", "log Kow = 1.5 (pH 5.0); -0.21 (pH 6.9); -0.76 (pH 9.0) at 25 째C");
 		r.propertyValue = r.propertyValue.replace("-0.52,-0.78", "-0.52; -0.78");
 		r.propertyValue = r.propertyValue.replace("log Kow = -0.52 and -0.78", "log Kow = -0.52; -0.78");
 		r.propertyValue = r.propertyValue.replace("log Kow = 4.31 (pH 5, 7, 9; 21 째C)", "log Kow = 4.31 (pH 5, 7, 9, 21 째C)");
		r.propertyValue = r.propertyValue.replace("log Kow > 5 (pH 4-5, 20-25 째C); log Kow = 3.8-4.1 (pH 6-7, 20-25 째C); log Kow = 2.5-3.2 (pH 9-10, 20-25 째C); log Kow = 4.3 (in purified water at 23 째C, pH not stated)", "log Kow > 5 at 22.5 째C (pH 4.5); log Kow = 3.8-4.1 at 22.5 째C (pH 6.5); log Kow = 2.5-3.2 at 22.5 째C (pH 9.5); log Kow = 4.3 (in purified water at 23 째C, pH not stated)");
		//Water Solubility
		r.propertyValue = r.propertyValue.replace("1 gm dissolves in 46 mL water, 5.5 mL water at 80 째C, 1.5 mL boiling water", "1 gm dissolves in 46 mL water; 1 gm dissolves in 5.5 mL water at 80 째C; 1 gm dissolves in 1.5 mL boiling water");
		r.propertyValue = r.propertyValue.replace("In water, 1160 mg/L at 25 째C, 800 mg/L at 20 째C", "In water, 1160 mg/L at 25 째C; 800 mg/L at 20 째C");
		r.propertyValue = r.propertyValue.replace("Soluble in acetone, ether, ammonia, in water, 97 g/100 cc at 25 째C, 145.6 g/100 cc at 100 째C", "Soluble in acetone, ether, ammonia, in water, 97 g/100 cc at 25 째C; 145.6 g/100 cc at 100 째C");
		r.propertyValue = r.propertyValue.replace("IN WATER: 7.5 G/L AT 25 째C & 477 G/L AT 100 째C", "IN WATER: 7.5 G/L AT 25 째C; 477 G/L AT 100 째C");
		r.propertyValue = r.propertyValue.replace("In water, 4.88 mg/L at 20 째C, 3.27 mg/L at 37 째C (column elution method)", "In water, 4.88 mg/L at 20 째C; 3.27 mg/L at 37 째C (column elution method)");
		r.propertyValue = r.propertyValue.replace("137.8 g/100 cc water @ 0 째C, 1270 g/100 cc water @ 100 째C, 100 g/100 cc alcohol @ 12.5 째C, very slightly sol in liq ammonia /Cupric nitrate trihydrate/", "137.8 g/100 cc water @ 0 째C; 1270 g/100 cc water @ 100 째C, 100 g/100 cc alcohol @ 12.5 째C, very slightly sol in liq ammonia /Cupric nitrate trihydrate/");
		r.propertyValue = r.propertyValue.replace("In water, 6.6 (pH 1.8), >250 (pH 4.1, 6.6, 8.2) (all in g/L at 25 째C)", "In water, 6.6 g/L at 25 째C (pH 1.8); >250 (pH 4.1, 6.6, 8.2) (all in g/L at 25 째C)");
		r.propertyValue = r.propertyValue.replace("In water, alpha-form = 0.32:, beta-form = 0.33 mg/L at 22 째C", "In water, alpha-form = 0.32; beta-form = 0.33 mg/L at 22 째C");
		r.propertyValue = r.propertyValue.replace("IN WATER: 102 G/100 CC @ 0 째C, 531 G/100 CC @ 80 째C; SOL IN ETHER, AMMONIA", "IN WATER: 102 G/100 CC @ 0 째C; 531 G/100 CC @ 80 째C; SOL IN ETHER, AMMONIA");
		r.propertyValue = r.propertyValue.replace("In water: 267 g/100 ml @ 10 째C, 620 G/100 ml @ 60 째C", "In water: 267 g/100 ml @ 10 째C; 620 G/100 ml @ 60 째C");
		r.propertyValue = r.propertyValue.replace("In water, 5.46X10+5 mg/L at 30 째C, 4.79X10+5 mg/L at 20 째C", "In water, 5.46X10+5 mg/L at 30 째C; 4.79X10+5 mg/L at 20 째C");
		r.propertyValue = r.propertyValue.replace("In water, 1.067, 0.965, 0.914, and 0.896 g/100 g water at 0, 10, 20, and 30 째C, respectively", "In water, 1.067 g/100 g water at 0 째C; 0.965 g/100 g water at 10 째C; 0.914 g/100 g water at 20 째C; and 0.896 g/100 g water 30 째C, respectively");
		r.propertyValue = r.propertyValue.replace("IN WATER: 19.6 G/100 CC @ 14.5 째C, 83.3 G/100 CC @ 100 째C", "IN WATER: 19.6 G/100 CC @ 14.5 째C; 83.3 G/100 CC @ 100 째C");
		r.propertyValue = r.propertyValue.replace("In water, 6.72 g/L at 20 째C. Also water solubility = 3.85 g/L at 0 째C", "In water, 6.72 g/L at 20 째C; Also water solubility = 3.85 g/L at 0 째C");
		r.propertyValue = r.propertyValue.replace("In water, 3490 mg/L at 25 deg, 2790 mg/L at 20 째C, 3790 mg/L at 30 째C", "In water, 3490 mg/L at 25 째C; 2790 mg/L at 20 째C; 3790 mg/L at 30 째C");
		r.propertyValue = r.propertyValue.replace("1 G DISSOLVES IN 5.5 ML WATER @ 13 째C, 2 ML @ 25 째C", "1 G DISSOLVES IN 5.5 ML WATER @ 13 째C;1 G DISSOLVES IN 2 ML WATER @ 25 째C");
		r.propertyValue = r.propertyValue.replace("1 g sol in: 300 mL water at 25 째C, 100 mL water at 37 째C, 5 mL alcohol, 17 mL chloroform, 10-15 mL ether", "1 g sol in: 300 mL water at 25 째C; 1 g sol in: 100 mL water at 37 째C, 5 mL alcohol, 17 mL chloroform, 10-15 mL ether");
		r.propertyValue = r.propertyValue.replace("Solubility in water, 20 g/100 ml at 0 째C, 73.8 g/100 ml at 100 째C.", "Solubility in water, 20 g/100 ml at 0 째C; 73.8 g/100 ml at 100 째C.");
		r.propertyValue = r.propertyValue.replace("In water, 788 g/L at 25 째C, 3926 g/L at 97.5 째C", "In water, 788 g/L at 25 째C; 3926 g/L at 97.5 째C");
		r.propertyValue = r.propertyValue.replace("WATER: 119 G/100 ML @ 0 째C, 170.15 G/100 ML @ 100 째C", "WATER: 119 G/100 ML @ 0 째C; 170.15 G/100 ML @ 100 째C");
		r.propertyValue = r.propertyValue.replace("Solubility in water = 6.4, 7.6, 8.7, 10.0, 11.3, 12.7, 14.2, 16.5, and 19.1 g/100g solution at 0, 10, 20, 30, 40, 50, 60, 80, and 100 째C, respectively", "Solubility in water = 6.4 g/100g at 0 째C; 7.6 g/100g at 10 째C; 8.7 g/100g at 20 째C; 10.0 g/100g at 30 째C; 11.3 g/100g at 40 째C; 12.7 g/100g at 50 째C; 14.2 g/100g at 60 째C; 16.5 g/100g at 80 째C; and 19.1 g/100g solution at 100 째C, respectively");
		r.propertyValue = r.propertyValue.replace("1 g dissolves in 0.9 mL water at room temperature, 0.2 mL water at 80 째C", "1 g dissolves in 0.9 mL water at room temperature; 1 g dissolves in 0.2 mL water at 80 째C");
		r.propertyValue = r.propertyValue.replace("21% (wt/vol) at 5 째C (water), 29% (wt/vol) at 40 째C (water), 33% (wt/vol at 25 째C (water)", "21% (wt/vol) at 5 째C (water); 29% (wt/vol) at 40 째C (water); 33% (wt/vol at 25 째C (water)");
		r.propertyValue = r.propertyValue.replace("In water, 0.64 g/100 g at 20 째C, 0.76 g/100 g at 25 째C, 1.27 g/100 g at 50 째C, 2.45 g/100 g at 100 째C.", "In water, 0.64 g/100 g at 20 째C; 0.76 g/100 g at 25 째C; 1.27 g/100 g at 50 째C; 2.45 g/100 g at 100 째C.");
		r.propertyValue = r.propertyValue.replace("70.9 g/100 ml water at 18 째C, 100 g/100 ml water at 100 째C", "70.9 g/100 ml water at 18 째C; 100 g/100 ml water at 100 째C");
		r.propertyValue = r.propertyValue.replace("Solubility in water: 80% at 100 째C, 40% at 45 째C", "Solubility in water: 80% at 100 째C; 40% at 45 째C");
		r.propertyValue = r.propertyValue.replace("Solubility in water: 0.10 g/100 ml @ 0 째C, 0.95 g/100 ml @ 100 째C", "Solubility in water: 0.10 g/100 ml @ 0 째C; 0.95 g/100 ml @ 100 째C");
		r.propertyValue = r.propertyValue.replace("In water (25 째C), 18.4 mg/L at pH = 5, 0.221 mg/L at pH 7, 0.189 mg/L at pH 9", "In water (25 째C), 18.4 mg/L at pH = 5; 0.221 mg/L at 25 째C, pH 7; 0.189 mg/L at 25 째C, pH 9");
		r.propertyValue = r.propertyValue.replace("In water, 9.30 g/100 g at 0 째C, 39.84 g/100 g at 65.3 째C", "In water, 9.30 g/100 g at 0 째C; 39.84 g/100 g at 65.3 째C");
		r.propertyValue = r.propertyValue.replace("In water at 20 째C, 0.5 g/L at pH 6, 1.5 g/L at pH 9", "In water at 20 째C, 0.5 g/L at pH 6; 1.5 g/L at pH 9");
		r.propertyValue = r.propertyValue.replace("In water at 25 째C: 2,180 mg/L (cis isomer), 2,320 mg/L (trans isomer)", "In water at 25 째C: 2,180 mg/L (cis isomer); 2,320 mg/L at 25 째C (trans isomer)");
		r.propertyValue = r.propertyValue.replace("SOLUBILITY IN WATER: 35% @ 0 째C, 41% @ 20 째C, 48% @ 50 째C", "SOLUBILITY IN WATER: 35% @ 0 째C; 41% @ 20 째C; 48% @ 50 째C");
		r.propertyValue = r.propertyValue.replace("SOLUBILITIES: 222.5 G/100 CC WATER AT 0 째C, 273.7 G/100 CC WATER AT 45 째C, SOL IN ALCOHOL, ACETONE, CHLOROFORM /HEXAHYDRATE/", "SOLUBILITIES: 222.5 G/100 CC WATER AT 0 째C; 273.7 G/100 CC WATER AT 45 째C, SOL IN ALCOHOL, ACETONE, CHLOROFORM /HEXAHYDRATE/");
		r.propertyValue = r.propertyValue.replace("Water solubility: 120 mg/L at 25 째C, 350 mg/L at 50 째C, 3200 mg/L at 100 째C", "Water solubility: 120 mg/L at 25 째C; 350 mg/L at 50 째C; 3200 mg/L at 100 째C");
		r.propertyValue = r.propertyValue.replace("34.4%, 80 째C, 26.2%, 100 째C", "34.4% at 80 째C; 26.2% at 100 째C");
		r.propertyValue = r.propertyValue.replace("31.6 g/100 cc water @ 0 째C, 203.3 g/100 cc @ 100 째C", "31.6 g/100 cc water @ 0 째C; 203.3 g/100 cc @ 100 째C");
		r.propertyValue = r.propertyValue.replace("SOL IN WATER: 0 G/L @ PH 4, 0.65 G/L @ PH 7", "SOL IN WATER: 0 G/L @ PH 4; 0.65 G/L @ PH 7");
		r.propertyValue = r.propertyValue.replace("12 mg/100 ml in water @ 20 째C, 27 mg/100 ml @ 37 째C", "12 mg/100 ml in water @ 20 째C; 27 mg/100 ml @ 37 째C");
		r.propertyValue = r.propertyValue.replace("SOL IN WATER (G/L): 15 (25 째C), 23 (40 째C), 30 (50 째C), 55 (70 째C)", "SOL IN WATER (G/L): 15 G/L at 25 째C; 23 G/L at 40 째C; 30 G/L at 50 째C; 55 G/L at 70 째C");
		r.propertyValue = r.propertyValue.replace("IN WATER @ PH 7.1: 0.0041 G/100 ML @ 25 째C, 0.008 G/100 ML @ 37 째C", "IN WATER @ PH 7.1: 0.0041 G/100 ML @ 25 째C; 0.008 G/100 ML @ 37 째C, PH 7.1");
		r.propertyValue = r.propertyValue.replace("SOL IN WATER: 17.2 G/100 ML @ 18 째C, 21.2 @ 25 째C", "SOL IN WATER: 17.2 G/100 ML @ 18 째C; 21.2 G/100 ML @ 25 째C");
		r.propertyValue = r.propertyValue.replace("In water at 25 째C, 51 mg/L (pH 5), 118 mg/L (pH 7), 900 mg/L (pH 9)", "In water at 25 째C, 51 mg/L (pH 5); 118 mg/L at 25 째C (pH 7); 900 mg/L at 25 째C (pH 9)");
		r.propertyValue = r.propertyValue.replace(" in buffered creek water at 24 째C, 190 mg/L (pH 6.5), 230 mg/l (pH 7.5), 260 mg/L (pH 8.5)", " in buffered creek water at 24 째C; 190 mg/L (pH 6.5); 230 mg/l (pH 7.5); 260 mg/L (pH 8.5)");
		r.propertyValue = r.propertyValue.replace("Solubility in water: 0.23 g/L at 0 째C, 11.4 g/L at 25 째C, 17.1 g/L at 50 째C, 27.95 g/L at 75 째C, 49.9 g/L at 100 째C", "Solubility in water: 0.23 g/L at 0 째C; 11.4 g/L at 25 째C; 17.1 g/L at 50 째C; 27.95 g/L at 75 째C; 49.9 g/L at 100 째C");
		r.propertyValue = r.propertyValue.replace("In water, 1.50X10+3 mg/L at 29 째C and 1.92X10+3 mg/L at 37 째C", "In water, 1.50X10+3 mg/L at 29 째C; 1.92X10+3 mg/L at 37 째C");
		r.propertyValue = r.propertyValue.replace("In water (g/100g): 83.2 at 20 째C, 89.8 at 40 째C, 98.4 at 60 째C, 111.9 at 80 째C, 128.3 at 100 째C", "In water: 83.2 g/100g at 20 째C; 89.8 g/100g at 40 째C; 98.4 g/100g at 60 째C; 111.9 g/100g at 80 째C; 128.3 g/100g at 100 째C");
		r.propertyValue = r.propertyValue.replace("Solubility in water at 0, 10, 20 and 30 째C is 6, 8.5, 17 and 28 wt %, respectively.", "Solubility in water is 6 wt % at 0 째C; 8.5 wt % at 10 째C; 17 wt % at 20 째C; 28 wt % at 30 째C, respectively.");
		r.propertyValue = r.propertyValue.replace("2.34 mg/L at pH 9, at 25 째C. In pure water, 0.063 mg/L at 21 째C", "2.34 mg/L at pH 9, at 25 째C; In pure water, 0.063 mg/L at 21 째C");
		r.propertyValue = r.propertyValue.replace("In water, >1.14X10+4 at pH 5, 2.48X10-3 at pH 7, 0.180 at pH 9 (all in g/L at 20 째C)", "In water, >1.14X10+4 g/L at 20 째C, pH 5; 2.48X10-3 g/L at 20 째C, pH 7; 0.180 at pH 9 (all in g/L at 20 째C)");
		r.propertyValue = r.propertyValue.replace("188 g/L at pH 5, 143 g/L ay pH 7", "188 g/L at pH 5; 143 g/L at pH 7");
		r.propertyValue = r.propertyValue.replace("In water at 20 째C, 5700 mg/L (pH 5.0), 7300 mg/L (pH 9.2)", "In water at 20 째C, 5700 mg/L (pH 5.0); 7300 mg/L at 20 째C (pH 9.2)");
		r.propertyValue = r.propertyValue.replace(" 2,040 mg/L at pH 7 and 18,300 mg/L at pH 9, all at 20 째C", "2,040 mg/L at 20 째C, pH 7; 18,300 mg/L at pH 9, all at 20 째C");
		r.propertyValue = r.propertyValue.replace("In water, 3 ppm (pH 5),184 ppm (pH 7) at 25 째C", "In water, 3 ppm at 25 째C (pH 5) ; 184 ppm (pH 7) at 25 째C");
		r.propertyValue = r.propertyValue.replace("In water, 63 mg/L (pH 5), 5850 mg/L (pH 7)", "In water, 63 mg/L (pH 5); 5850 mg/L (pH 7)");
		r.propertyValue = r.propertyValue.replace("In water, 116 (pH 5), >626 (pH 7), >628 (pH 9) (all in g/l, 25 째C)", "In water, 116 g/l, 25 째C (pH 5); >626 g/l, 25 째C(pH 7); >628 (pH 9) (all in g/l, 25 째C)");
		r.propertyValue = r.propertyValue.replace("In water (20 째C) = 5 mg/l (pH 5), 6.7 mg/l (pH 6.5), 9800 mg/l (pH 9)", "In water (20 째C) = 5 mg/l (pH 5); 6.7 mg/l at 20 째C (pH 6.5); 9800 mg/l at 20 째C (pH 9)");
		r.propertyValue = r.propertyValue.replace("In water, 1.9 mg/L (pH 5), 2.4 mg/L (pH 9), 1.9 mg/L (distilled), all at 20 째C", "In water, 1.9 mg/L at 20 째C (pH 5); 2.4 mg/L at 20 째C (pH 9); 1.9 mg/L (distilled), all at 20 째C");
		r.propertyValue = r.propertyValue.replace("In water, 12 mg/L at 20 째C, 22 mg/L at 25 째C, and 23 mg/L at 30 째C.", "In water, 12 mg/L at 20 째C; 22 mg/L at 25 째C; and 23 mg/L at 30 째C.");
		r.propertyValue = r.propertyValue.replace("In water 0.85 (distilled), 0.78 (pH 5 and 7) mg/L at 25 째C.", "In water 0.85 (distilled); 0.78 (pH 5 and 7) mg/L at 25 째C.");
		r.propertyValue = r.propertyValue.replace("2.95X10+3 mg/L at pH 7, 3.96X10+3 mg/L at pH 9", "2.95X10+3 mg/L at pH 7; 3.96X10+3 mg/L at pH 9");
		r.propertyValue = r.propertyValue.replace("In water, 0.304 (pH 4), 0.340 (pH 10) (both g/L, 20 째C)", "In water, 0.304 g/L, 20 째C (pH 4); 0.340 (pH 10) (both g/L, 20 째C)");
		r.propertyValue = r.propertyValue.replace("Milbemycin A3: In water 0.88 ppm (20 째C). Milbemycin A4: In water 7.2 ppm (20 째C)", "Milbemycin A3: In water 0.88 ppm (20 째C); Milbemycin A4: In water 7.2 ppm (20 째C)");
		r.propertyValue = r.propertyValue.replace("In water, 0.023% at 18 째C, 0.09% at 70 째C", "In water, 0.023% at 18 째C; 0.09% at 70 째C");
		r.propertyValue = r.propertyValue.replace("In water, 17.6 (pH 5), 1627 (pH 7), 482 (pH 9) (all in mg/L, 20 째C)", "In water, 17.6 mg/L, 20 째C (pH 5); 1627 mg/L, 20 째C (pH 7); 482 (pH 9) (all in mg/L, 20 째C)");
		r.propertyValue = r.propertyValue.replace("In water (ppm), 44 at pH 3.5: 22000 (pH 7)", "In water (ppm), 44 at pH 3.5; 22000 ppm (pH 7)");
		r.propertyValue = r.propertyValue.replace("In water, 0.027 (pH 5), 2.1 (pH 7) (g/L, 25 째C)", "In water, 0.027 (pH 5); 2.1 g/L, 25 째(pH 7) (g/L, 25 째C)");
		r.propertyValue = r.propertyValue.replace("In water at 20 째C: 2810 mg/L (in Milli-Q water), 3130 mg/L (pH 4), 4200 mg/L (pH 7), 3870 mg/L (pH 9)", "In water at 20 째C: 2810 mg/L (in Milli-Q water); 3130 mg/L at 20 째C (pH 4); 4200 mg/L at 20 째C(pH 7); 3870 mg/L at 20 째C (pH 9)");
		r.propertyValue = r.propertyValue.replace("One gram dissolves in 20.8 mL water at 30 째C, in 38.5 mL at 18 째C, in 56.7 mL at 0 째C", "One gram dissolves in 20.8 mL water at 30 째C; One gram dissolves in 38.5 mL at 18 째C; One gram dissolves in 56.7 mL at 0 째C");
		r.propertyValue = r.propertyValue.replace("Sol in water (in g/100 ml water): 20.8 g @ 6 째C, 3.5 g @ 20 째C, 1.8 g @ 100 째C","Sol in water: 20.8 g/100 ml water @ 6 째C; 3.5 g/100 ml water @ 20 째C; 1.8 g/100 ml water @ 100 째C");
		r.propertyValue = r.propertyValue.replace("Fluffy white powder, mp 238 째C. Solubility in water (mg/100 mL) at 30 째C: 220 (ph 3.8), 280 (pH 5.5), 290 (pH 6.7), 264 (pH 7.9), 244 (pH 9.2-10.1) /Hydrochloride/","Fluffy white powder, mp 238 째C. Solubility in water: 220 mg/100 mL at 30 째C (ph 3.8); 280 mg/100 mL at 30 째C (pH 5.5); 290 mg/100 mL at 30 째C (pH 6.7); 264 mg/100 mL at 30 째C (pH 7.9); 244 mg/100 mL at 30 째C (pH 9.2-10.1) /Hydrochloride/");
		r.propertyValue = r.propertyValue.replace("Water solubility at 80 째C (g/100 g water): 0.94 (at 3.4 MPa), 1.54 (at 6.9 MPa)","Water solubility: 0.94 g/100 g water at 80 째C; Water solubility: 1.54 g/100 g water at 80 째C");
		r.propertyValue = r.propertyValue.replace("SOLUBILITY (G/100 CC WATER): 7 G @ 0 째C, 42 G @ 80 째C /ANHYDROUS/", "SOLUBILITY: 7 G/100 CC WATER @ 0 째C; 42 G/100 CC WATER @ 80 째C /ANHYDROUS/");
		r.propertyValue = r.propertyValue.replace("Solubility in water (g/100 g water): 27.5 at 0 째C; 48.8 at 40 째C; 62.6 at 60 째C; 75.8 at 80 째C; 90.8 at 100 째C", "Solubility in water: 27.5 g/100 g water at 0 째C; 48.8 g/100 g water at 40 째C; 62.6 g/100 g water at 60 째C; 75.8 g/100 g water at 80 째C; 90.8 g/100 g water at 100 째C");
		r.propertyValue = r.propertyValue.replace("Solubility in water (%): 0.2 at 25 째C; 2.6 at 90 째C; 10.0 at 150 째C", "Solubility in water: 0.2 wt% at 25 째C; 2.6 wt% at 90 째C; 10.0 wt% at 150 째C");
		r.propertyValue = r.propertyValue.replace("Solubility in water (wt%): 1.52 at 0 째C; 1.31 at 20 째C; 1.16 at 40 째C; 1.00 at 60 째C; 0.84 at 80 째C; 0.71 at 100 째C", "Solubility in water: 1.52 wt% at 0 째C; 1.31 wt% at 20 째C; 1.16 wt% at 40 째C; 1.00 wt% at 60 째C; 0.84 wt% at 80 째C; 0.71 wt% at 100 째C");
		r.propertyValue = r.propertyValue.replace("In water (g/100 g): 18.1 at 20 째C; 19.2 at 5 째C", "In water: 18.1 g/100 g at 20 째C; 19.2 g/100 g at 5 째C");
		r.propertyValue = r.propertyValue.replace("In water (g/100 g H2O): 70.6 at 0 째C; 76.7 at 25 째C; 103.8 at 100 째C", "In water: 70.6 g/100 g H2O at 0 째C; 76.7 g/100 g H2O at 25 째C; 103.8 g/100 g H2O at 100 째C");
		r.propertyValue = r.propertyValue.replace("Solubility (g/100 cc solvent): 5.70 g in water @ 10 째C; 6.23 g in water @ 25 째C; 250 +/- 10 g in carbon tetrachloride @ 20 째C", "Solubility (solvent): 5.70 g/100 cc in water @ 10 째C; 6.23 g/100 cc in water @ 25 째C; 250 +/- 10 g/100 cc in carbon tetrachloride @ 20 째C");
		r.propertyValue = r.propertyValue.replace("SOLUBILITY (G/100 CC SOLVENT): 184 G IN WATER @ 25 째C; 302 G IN WATER @ 100 째C; 42.57 G IN ALCOHOL @ 25 째C; 39.9 G IN ACETONE @ 25 째C", "SOLUBILITY: 184 G/100 CC IN WATER @ 25 째C; 302 G/100 CC IN WATER @ 100 째C; 42.57 G IN ALCOHOL @ 25 째C; 39.9 G IN ACETONE @ 25 째C");
		r.propertyValue = r.propertyValue.replace("Sol in water (% wt/wt): 27.2% @ 45.3 째C; 18.1% @ 48.1 째C; 12.1% @ 57.5 째C; 9.5% @ 74.5 째C; miscible with dimethylformamide and tetrahydrofuran", "Sol in water: 27.2% wt/wt @ 45.3 째C; 18.1% wt/wt@ 48.1 째C; 12.1% wt/wt @ 57.5 째C; 9.5% wt/wt @ 74.5 째C; miscible with dimethylformamide and tetrahydrofuran");
		r.propertyValue = r.propertyValue.replace("Solubility in water as weight %: 4.72%, 0 째C; 9.27%, 25 째C; 12.35%, 40 째C; 16.9%, 60 째C; 21.4%, 80 째C; 25.6%, 100 째C; 32.0%, 135 째C", "Solubility in water: 4.72wt% at 0 째C; 9.27wt% at 25 째C; 12.35wt% at 40 째C; 16.9wt% at 60 째C; 21.4wt% at 80 째C; 25.6wt% at 100 째C; 32.0wt% at 135 째C");
		r.propertyValue = r.propertyValue.replace("SOLUBILITY IN WATER @ 20-25 째C: 22% /LITHIUM SALT/; 14% /SODIUM SALT/; 10.7% /POTASSIUM SALT/", "SOLUBILITY IN WATER: 22% @ 20-25 째C /LITHIUM SALT/; 14% @ 20-25 째C/SODIUM SALT/; 10.7% @ 20-25 째C /POTASSIUM SALT/");
		r.propertyValue = r.propertyValue.replace("In water, 32 mg/L (pH 5), 815 mg/L (pH 7), 13,500 mg/L (pH 8.2) at 25 째C","In water, 32 mg/L at 25 째C (pH 5); 815 mg/L at 25 째C (pH 7); 13,500 mg/L (pH 8.2) at 25 째C");
		r.propertyValue = r.propertyValue.replace("In water, 29 mg/L (pH 4.5), 87 mg/L (pH 5.0), 4000 mg/L (pH 6.8), 43000 mg/L (pH 7.7) at 25 째C","In water, 29 mg/L at 25 째C (pH 4.5); 87 mg/L at 25 째C (pH 5.0); 4000 mg/L at 25 째C (pH 6.8); 43000 mg/L (pH 7.7) at 25 째C");
		r.propertyValue = r.propertyValue.replace("In water (mg/L at 25 째C), 5 (pH 5.1), 67 (pH 6.1), 308 (pH 7)","In water, 5 mg/L at 25 째C (pH 5.1); 67 mg/L at 25 째C (pH 6.1); 308 mg/L at 25 째C (pH 7)");
		r.propertyValue = r.propertyValue.replace("In water, 429 mg/L (pH 7), 3936 mg/L (pH 9) at 25 째C","In water, 429 mg/L at 25 째C (pH 7); 3936 mg/L (pH 9) at 25 째C");
		r.propertyValue = r.propertyValue.replace("In water (mg/L at 20 째C), 0.37 (pH 5), 160 (pH 7), 2200 mg/L (pH 9)","In water, 0.37 mg/L at 20 째C (pH 5); 160 mg/L at 20 째C (pH 7); 2200 mg/L at 20 째C (pH 9)");
		r.propertyValue = r.propertyValue.replace("The solubility of aspartame in water is dependent on pH and temperature, the maximum solubility is reached at pH 2.2 (20 mg/mL at 25 째C) and the minimum solubility at pH 5.2 (pHi) is 13.5 mg/mL at 25 째C.","The solubility of aspartame in water is dependent on pH and temperature, the maximum solubility is 20 mg/mL reached at 25 째C and pH 2.2; and the minimum solubility is 13.5 mg/mL at 25 째C and pH 5.2");
		r.propertyValue = r.propertyValue.replace("Water solubility in wt %: 1.65%, 0 째C; 3.74%, 20 째C; 7.60%, 40 째C; 17.32%, 60 째C; 50.35%, 80 째C", "Water solubility in: 1.65wt % at 0 째C; 3.74wt % at 20 째C; 7.60wt % at 40 째C; 17.32wt % at 60 째C; 50.35wt % at 80 째C");
		r.propertyValue = r.propertyValue.replace("pK2 = 9.24. Solubility in water (g/L): 9.97 at 0 째C", "Solubility in water: 9.97 g/L at 0 째C");
		r.propertyValue = r.propertyValue.replace("Solubility in water as weight %: 24%, 0 째C; 26.3%, 20 째C; 28.9%, 40 째C; 31.7%, 60 째C; 34.4% at 80 째C; 26.2% at 100 째C", "Solubility in water: 24wt% at 0 째C; 26.3wt% at 20 째C; 28.9wt% at 40 째C; 31.7wt% at 60 째C; 34.4wt% at 80 째C; 26.2wt% at 100 째C");
//		r.propertyValue = r.propertyValue.replace("", "");

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
//		p.howManyOriginalRecordsFiles=3;

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
		p.selectedHeadings=Arrays.asList("Solubility");								
//		p.selectedHeadings=Arrays.asList("Density");
//		p.selectedHeadings=Arrays.asList("Vapor Density");
//		p.selectedHeadings=Arrays.asList("Density", "Vapor Density");
//		p.selectedHeadings=Arrays.asList("Vapor Pressure");
//		p.selectedHeadings=Arrays.asList("LogP");
//		p.selectedHeadings=Arrays.asList("Melting Point");
//		p.selectedHeadings=Arrays.asList("Boiling Point");
//		p.selectedHeadings=Arrays.asList("Autoignition Temperature");
//		p.selectedHeadings=Arrays.asList("Flash Point");
//		p.selectedHeadings=Arrays.asList("Viscosity");
//		p.selectedHeadings=Arrays.asList("Surface Tension");
//		p.selectedHeadings=Arrays.asList("Henry's Law Constant");
		

		p.createFiles();
	}
}
