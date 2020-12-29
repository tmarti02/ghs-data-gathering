package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DataAnalyzer {
	private static String[] sourceNames;
	private static Vector<SourceRecords> recordsBySource;
	private static ExperimentalRecords records;
	
	public static final String mainFolder = "Data"+File.separator+"Experimental";
	
	private class SourceRecords {
		ExperimentalRecords records;
		String sourceName;
	}
	
	public DataAnalyzer(String[] sources,boolean includeBadRecords) {
		sourceNames = sources;
		records = new ExperimentalRecords();
		recordsBySource = new Vector<SourceRecords>();
		for (String source:sourceNames) {
			SourceRecords sr = new SourceRecords();
			sr.sourceName = source;
			sr.records = new ExperimentalRecords();
			String recordFileName = mainFolder+File.separator+source+" Experimental Records.json";
			String badRecordFileName = mainFolder+File.separator+source+" Experimental Records-Bad.json";
			try {
				System.out.println("Fetching data from "+source.substring(source.lastIndexOf("\\")+1));
				ExperimentalRecords sourceRecords = ExperimentalRecords.loadFromJSON(recordFileName);
				records.addAll(sourceRecords);
				sr.records.addAll(sourceRecords);
				if (includeBadRecords) {
					ExperimentalRecords badSourceRecords = ExperimentalRecords.loadFromJSON(badRecordFileName);
					records.addAll(badSourceRecords);
					sr.records.addAll(badSourceRecords);
				}
				recordsBySource.add(sr);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private static boolean isSameRecord(ExperimentalRecord er1,ExperimentalRecord er2) {
		boolean hasCAS = er1.casrn!=null && !er1.casrn.isBlank() && er2.casrn!=null && !er2.casrn.isBlank();
		boolean hasEINECS = er1.einecs!=null && !er1.einecs.isBlank() && er2.einecs!=null && !er2.einecs.isBlank();
		boolean hasSMILES = er1.smiles!=null && !er1.smiles.isBlank() && er2.smiles!=null && !er2.smiles.isBlank();
		boolean sameCAS = hasCAS ? er1.casrn.equals(er2.casrn) : true;
		boolean sameEINECS = hasEINECS ? er1.einecs.equals(er2.einecs) : true;
		boolean sameSMILES = hasSMILES ? er1.smiles.equals(er2.smiles) : true;
		boolean same = false;
		if (hasCAS || hasEINECS || hasSMILES) {
			same = sameCAS && sameEINECS && sameSMILES && er1.property_name.equals(er2.property_name);
		} else if (er1.chemical_name!=null && er2.chemical_name!=null) {
			same = er1.chemical_name.equals(er2.chemical_name) && er1.property_name.equals(er2.property_name);
		}
		return same;
	}
	
	private static boolean hasSameConditions(ExperimentalRecord er1,ExperimentalRecord er2) {
		boolean hasTemp = er1.temperature_C!=null && er2.temperature_C!=null;
		boolean hasPressure = er1.pressure_mmHg!=null && !er1.pressure_mmHg.isBlank() && er2.pressure_mmHg!=null && !er2.pressure_mmHg.isBlank();
		boolean hasPH = er1.pH!=null && !er1.pH.isBlank() && er2.pH!=null && !er2.pH.isBlank();
		boolean same = false;
		// TODO condition checking
		return same;
	}
	
	public void getRedundancyMatrix() {
		int size = sourceNames.length;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("Redundancy-All");
		Row headerRow = sheet.createRow(0);
		CellStyle style = wb.createCellStyle();
		Font font = wb.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setFont(font);
		for (int i = 1; i < size+1; i++) {
			Cell headerCell = headerRow.createCell(i);
			headerCell.setCellValue(sourceNames[i-1].substring(sourceNames[i-1].lastIndexOf("\\")+1));
			headerCell.setCellStyle(style);
			Row row = sheet.createRow(i);
			Cell firstCell = row.createCell(0);
			firstCell.setCellValue(sourceNames[i-1].substring(sourceNames[i-1].lastIndexOf("\\")+1));
			firstCell.setCellStyle(style);
			ExperimentalRecords recordsI = recordsBySource.get(i-1).records;
			String sourceI = recordsBySource.get(i-1).sourceName;
			for (int j = 1; j < size+1; j++) {
				if (i!=j) {
					ExperimentalRecords recordsJ = recordsBySource.get(j-1).records;
					String sourceJ = recordsBySource.get(j-1).sourceName;
					System.out.println("Comparing "+sourceI+" to "+sourceJ+"...");
					int total = recordsI.size();
					int redundant = 0;
					Iterator<ExperimentalRecord> itI = recordsI.iterator();
					while (itI.hasNext()) {
						ExperimentalRecord recI = itI.next();
						boolean found = false;
						Iterator<ExperimentalRecord> itJ = recordsJ.iterator();
						while (itJ.hasNext() && !found) {
							if (isSameRecord(recI,itJ.next())) {
								redundant++;
								found = true;
							}
						}
						// TODO checking for same (or "close enough") values
					}
					double redundancy = (double) redundant/(double) total;
					System.out.println("Out of "+total+" records in "+sourceI+", found "+redundant+" in "+sourceJ+". Redundancy: "+ParseUtilities.formatDouble(redundancy)+".");
					Cell cell = row.createCell(j);
					cell.setCellValue(redundancy);
				}
			}
		}
		try {
			OutputStream fos = new FileOutputStream(mainFolder + File.separator + "Redundancy Matrices.xlsx");
			wb.write(fos);
			wb.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String[] sources = {"eChemPortal\\eChemPortal","LookChem\\LookChem PFAS\\LookChem","PubChem\\PubChem","OChem\\OChem","OFMPub\\OFMPub","QSARDB\\QSARDB",
				"Bradley\\Bradley","ADDoPT\\ADDoPT","AqSolDB\\AqSolDB"};
		DataAnalyzer d = new DataAnalyzer(sources,false);
		d.getRedundancyMatrix();
	}
}
