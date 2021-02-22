package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Map;

import gov.epa.api.ExperimentalConstants;

public class DataCompare {
	public String sourceName1;
	public String sourceName2;
	private ExperimentalRecords records1;
	private ExperimentalRecords records2;
	private boolean includeBadRecords;
	
	public static final String mainFolder = "Data"+File.separator+"Experimental";
	
	private Map<String,Map<String,Boolean>> casTable;
	
	public DataCompare(String[] sourceNames,boolean includeBad,boolean includeQualitative) {
		DataFetcher df = new DataFetcher(sourceNames,"physchem");
		casTable = new HashMap<String,Map<String,Boolean>>();
		for (ExperimentalRecord rec:df.records) {
			if (rec.casrn==null) { continue; }
			if ((includeBad || rec.keep) && (includeQualitative || rec.property_value_point_estimate_final!=null || rec.property_value_min_final!=null)) {
				Map<String,Boolean> row = casTable.get(rec.casrn);
				if (row==null) { row = new HashMap<String,Boolean>(); }
				row.put(rec.source_name,true);
				casTable.put(rec.casrn, row);
			}
		}
	}

	public DataCompare(String sourceName1,String sourceName2,boolean includeBadRecords) {
		this.includeBadRecords = includeBadRecords;
		this.sourceName1 = sourceName1;
		this.sourceName2 = sourceName2;
		records1 = new ExperimentalRecords();
		String record1FileName = mainFolder+File.separator+sourceName1+File.separator+sourceName1+" Experimental Records.json";
		String badRecord1FileName = mainFolder+File.separator+sourceName1+File.separator+sourceName1+" Experimental Records-Bad.json";
		try {
			ExperimentalRecords records = ExperimentalRecords.loadFromJSON(record1FileName);
			if (records!=null) { records1.addAll(records); }
			if (includeBadRecords) {
				ExperimentalRecords badRecords = ExperimentalRecords.loadFromJSON(badRecord1FileName);
				if (badRecords!=null) { records1.addAll(badRecords); }
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		records2 = new ExperimentalRecords();
		String record2FileName = mainFolder+File.separator+sourceName2+File.separator+sourceName2+" Experimental Records.json";
		String badRecord2FileName = mainFolder+File.separator+sourceName2+File.separator+sourceName2+" Experimental Records-Bad.json";
		try {
			ExperimentalRecords records = ExperimentalRecords.loadFromJSON(record2FileName);
			if (records!=null) { records2.addAll(records); }
			if (includeBadRecords) {
				ExperimentalRecords badRecords = ExperimentalRecords.loadFromJSON(badRecord2FileName);
				if (badRecords!=null) { records2.addAll(badRecords); }
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void generateOverlapTable(String[] sourceNames) {
		Map<String,Map<String,Integer>> overlapTable = new HashMap<String,Map<String,Integer>>();
		Map<String,Integer> uniqueValueTable = new HashMap<String,Integer>();
		for (String cas:casTable.keySet()) {
			Map<String,Boolean> cas_row = casTable.get(cas);
			for (String s1:sourceNames) {
				Boolean hasS1 = cas_row.get(s1);
				if (hasS1!=null && hasS1) {
					boolean uniqueS1 = true;
					for (String s2:sourceNames) {
						if (s1.equals(s2)) { continue; }
						Boolean hasS2 = cas_row.get(s2);
						if (hasS2!=null && hasS2) {
							uniqueS1 = false;
							Map<String,Integer> overlapS1S2 = overlapTable.get(s1);
							if (overlapS1S2==null) { overlapS1S2 = new HashMap<String,Integer>(); }
							Integer countOverlapS1S2 = overlapS1S2.get(s2);
							if (countOverlapS1S2==null) { countOverlapS1S2 = 0; }
							overlapS1S2.put(s2, countOverlapS1S2+1);
							overlapTable.put(s1, overlapS1S2);
						}
					}
					if (uniqueS1) {
						Integer countUniqueS1 = uniqueValueTable.get(s1);
						if (countUniqueS1==null) { countUniqueS1 = 0; }
						uniqueValueTable.put(s1, countUniqueS1+1);
					}
				}
			}
		}
		
		System.out.println("\t" + String.join("\t", sourceNames));
		for (String s1:sourceNames) {
			System.out.print(s1);
			Map<String,Integer> overlapS1S2 = overlapTable.get(s1);
			for (String s2:sourceNames) {
				System.out.print("\t"+(overlapS1S2!=null ? overlapS1S2.get(s2) : null));
			}
			System.out.print("\n");
		}
		System.out.println("");
		for (String s1:sourceNames) {
			System.out.println(s1 + "\t" + uniqueValueTable.get(s1));
		}
	}
	
	private ExperimentalRecords compare() {
		ExperimentalRecords comp = new ExperimentalRecords();
		System.out.println("Finding records missing from "+sourceName2+"...");
		List<ExperimentalRecord> recordsIn1NotIn2 = new ArrayList<ExperimentalRecord>(records1);
		List<ExperimentalRecord> recordsIn2ToRemove = new ArrayList<ExperimentalRecord>(records2);
		recordsIn1NotIn2.removeAll(recordsIn2ToRemove);
		System.out.println("Finding records missing from "+sourceName1+"...");
		List<ExperimentalRecord> recordsIn2NotIn1 = new ArrayList<ExperimentalRecord>(records2);
		List<ExperimentalRecord> recordsIn1ToRemove = new ArrayList<ExperimentalRecord>(records1);
		recordsIn2NotIn1.removeAll(recordsIn1ToRemove);
		if (sourceName1.equals(ExperimentalConstants.strSourceEChemPortal) && sourceName2.equals(ExperimentalConstants.strSourceEChemPortalAPI)) {
			System.out.println("Correcting for eChemPortal formatting...");
			eChemPortalCorrection(recordsIn1NotIn2,recordsIn2NotIn1);
		}
		comp.addAll(recordsIn1NotIn2);
		comp.addAll(recordsIn2NotIn1);
		return comp;
	}
	
	private void eChemPortalCorrection(List<ExperimentalRecord> inExcelNotInAPI,List<ExperimentalRecord> inAPINotInExcel) {
		Iterator<ExperimentalRecord> iti = inExcelNotInAPI.iterator();
		while (iti.hasNext()) {
			ExperimentalRecord reci = iti.next();
			if (reci.property_name.equals(ExperimentalConstants.strDensity)) {
				// Original eChemPortal density download was missing criteria, so cannot be matched
				iti.remove();
				continue;
			}
			boolean remove = false;
			Iterator<ExperimentalRecord> itj = inAPINotInExcel.iterator();
			while (itj.hasNext()) {
				ExperimentalRecord recj = itj.next();
				if (identifiersMatch(reci,recj) && reci.property_name.equals(recj.property_name)) {
					// Accounts for false mismatches due to character encoding problems
					int valueIndex = reci.property_value_string.contains(";") ? reci.property_value_string.indexOf(";") : reci.property_value_string.length();
					String fixedPropertyValueString = reci.property_value_string.substring(0,valueIndex).replaceAll("\\?","-").replaceAll("—", "-");
					if (recj.property_value_string.contains(fixedPropertyValueString) && conditionsMatch(reci,recj)) {
						remove = true;
						itj.remove();
					}
				}
			}
			if (remove) { iti.remove(); }
		}
	}
	
	private static boolean identifiersMatch(ExperimentalRecord reci, ExperimentalRecord recj) {
		if ((reci.casrn!=null && Objects.equals(reci.casrn, recj.casrn)) || 
				((reci.casrn==null || recj.casrn==null) && reci.einecs!=null && Objects.equals(reci.einecs, recj.einecs)) || 
				((reci.casrn==null || recj.casrn==null) && (reci.einecs==null || recj.einecs==null) && 
						reci.chemical_name!=null && Objects.equals(reci.chemical_name,  recj.chemical_name))) {
			return true;
		} else {
			return false;
		}
	}
	
	private static boolean conditionsMatch(ExperimentalRecord reci, ExperimentalRecord recj) {
		if ((reci.pressure_mmHg==null || reci.pressure_mmHg.isBlank() || Objects.equals(reci.pressure_mmHg, recj.pressure_mmHg) || 
				Objects.equals(reci.pressure_mmHg.replaceAll("\\?", "").replaceAll("—", "-"),recj.pressure_mmHg)) &&
				(reci.temperature_C==null || Objects.equals(reci.temperature_C, recj.temperature_C)) &&
				(reci.pH==null || reci.pH.isBlank() || Objects.equals(reci.pH, recj.pH) || Objects.equals(reci.pH.replaceAll("\\?", "").replaceAll("—", "-"),recj.pH))) {
			return true;
		} else {
			return false;
		}
	}
	
	public void compareAndWriteFiles() {
		ExperimentalRecords comp = compare();
		String noteBadRecords = includeBadRecords ? "_WithBadRecords" : "_NoBadRecords";
		String jsonFilepath = mainFolder+File.separator+sourceName1+"_"+sourceName2+"_Comparison"+noteBadRecords+".json";
		String excelFilepath = mainFolder+File.separator+sourceName1+"_"+sourceName2+"_Comparison"+noteBadRecords+".xlsx";
		System.out.println("Writing to JSON file...");
		comp.toJSON_File(jsonFilepath);
		System.out.println("Writing to Excel file...");
		comp.toExcel_File(excelFilepath);
		System.out.println("Done!");
	}
	
	public static void main(String[] args) {
//		DataCompare d = new DataCompare("eChemPortal","eChemPortalAPI",true);
//		d.compareAndWriteFiles();
		String[] allSources = {ExperimentalConstants.strSourceADDoPT,
				ExperimentalConstants.strSourceAqSolDB,
				ExperimentalConstants.strSourceBradley,
				ExperimentalConstants.strSourceChemicalBook,
				ExperimentalConstants.strSourceChemidplus,
				ExperimentalConstants.strSourceEChemPortalAPI,
				ExperimentalConstants.strSourceLookChem,
				ExperimentalConstants.strSourceOChem,
				ExperimentalConstants.strSourceOFMPub,
				ExperimentalConstants.strSourceOPERA,
				ExperimentalConstants.strSourcePubChem,
				ExperimentalConstants.strSourceQSARDB,
				ExperimentalConstants.strSourceSander,
				ExperimentalConstants.strSourceEpisuiteISIS};
		DataCompare dc = new DataCompare(allSources,false,false);
		dc.generateOverlapTable(allSources);
	}
}
