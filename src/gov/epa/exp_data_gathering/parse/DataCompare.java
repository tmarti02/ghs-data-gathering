package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DataCompare {
	public String sourceName1;
	public String sourceName2;
	private ExperimentalRecords records1;
	private ExperimentalRecords records2;
	
	public static final String mainFolder = "Data"+File.separator+"Experimental";

	public DataCompare(String sourceName1,String sourceName2) {
		this.sourceName1 = sourceName1;
		this.sourceName2 = sourceName2;
		records1 = new ExperimentalRecords();
		String record1FileName = mainFolder+File.separator+sourceName1+File.separator+sourceName1+" Experimental Records.json";
		String badRecord1FileName = mainFolder+File.separator+sourceName1+File.separator+sourceName1+" Experimental Records-Bad.json";
		try {
			ExperimentalRecords records = ExperimentalRecords.loadFromJSON(record1FileName);
			if (records!=null) { records1.addAll(records); }
			ExperimentalRecords badRecords = ExperimentalRecords.loadFromJSON(badRecord1FileName);
			if (badRecords!=null) { records1.addAll(badRecords); }
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		records2 = new ExperimentalRecords();
		String record2FileName = mainFolder+File.separator+sourceName2+File.separator+sourceName2+" Experimental Records.json";
		String badRecord2FileName = mainFolder+File.separator+sourceName2+File.separator+sourceName2+" Experimental Records-Bad.json";
		try {
			ExperimentalRecords records = ExperimentalRecords.loadFromJSON(record2FileName);
			if (records!=null) { records2.addAll(records); }
			ExperimentalRecords badRecords = ExperimentalRecords.loadFromJSON(badRecord2FileName);
			if (badRecords!=null) { records2.addAll(badRecords); }
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public ExperimentalRecords compare() {
		ExperimentalRecords comp = new ExperimentalRecords();
		List<ExperimentalRecord> recordsIn1NotIn2 = new ArrayList<ExperimentalRecord>(records1);
		List<ExperimentalRecord> recordsIn2ToRemove = new ArrayList<ExperimentalRecord>(records2);
		recordsIn1NotIn2.removeAll(recordsIn2ToRemove);
		System.out.println("Found "+recordsIn1NotIn2.size()+" records in "+sourceName1+" missing from "+sourceName2+".");
		List<ExperimentalRecord> recordsIn2NotIn1 = new ArrayList<ExperimentalRecord>(records2);
		List<ExperimentalRecord> recordsIn1ToRemove = new ArrayList<ExperimentalRecord>(records1);
		recordsIn2NotIn1.removeAll(recordsIn1ToRemove);
		System.out.println("Found "+recordsIn2NotIn1.size()+" records in "+sourceName2+" missing from "+sourceName1+".");
		comp.addAll(recordsIn1NotIn2);
		comp.addAll(recordsIn2NotIn1);
		return comp;
	}
	
	public void compareAndWriteFiles() {
		ExperimentalRecords comp = compare();
		comp.toJSON_File(mainFolder+File.separator+sourceName1+"_"+sourceName2+"_RecordCompare.json");
		comp.toExcel_File(mainFolder+File.separator+sourceName1+"_"+sourceName2+"_RecordCompare.xlsx");
	}
	
	public static void main(String[] args) {
		DataCompare d = new DataCompare("eChemPortal","eChemPortalAPI");
		d.compareAndWriteFiles();
	}
}
