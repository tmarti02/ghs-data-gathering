package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import gov.epa.api.ExperimentalConstants;

public class RecordEChemPortal {
	String substanceName;
	String nameType;
	String number;
	String numberType;
	boolean memberOfCategory;
	String participant;
	String section;
	String url;
	String reliability;
	String method;
	Vector<String> values;
	Vector<String> pressure;
	Vector<String> temperature;
	Vector<String> pH;
	
	static final String sourceName = ExperimentalConstants.strSourceEChem;
	
	private RecordEChemPortal() {
		values = new Vector<String>();
		pressure = new Vector<String>();
		temperature = new Vector<String>();
		pH = new Vector<String>();
	}
	
	public static Vector<RecordEChemPortal> parseEChemPortalQueryFromExcel() {
		Vector<RecordEChemPortal> records = new Vector<RecordEChemPortal>();
		String folderNameExcel = "excel files";
		String mainFolder = "Data"+File.separator+"Experimental"+ File.separator + sourceName;
		String excelFilePath = mainFolder + File.separator+folderNameExcel;
		File folder = new File(excelFilePath);
		String[] filenames = folder.list();
		Vector<String> filenamesSorted = new Vector<String>();
		for (String filename:filenames) {
			if (filename.contains("1Condition")) { filenamesSorted.add(filename); }
		}
		for (String filename:filenames) {
			if (filename.contains("2Conditions")) { filenamesSorted.add(0,filename);
			} else if (filename.contains("All")) { filenamesSorted.add(filename); }
		}
		HashSet<String> urlCheck = new HashSet<String>();
		int countDuplicates = 0;
		for (String filename:filenamesSorted) {
			if (filename.endsWith(".xls")) {
				try {
					FileInputStream fis = new FileInputStream(new File(excelFilePath+File.separator+filename));
					Workbook wb = new HSSFWorkbook(fis);
					Sheet sheet = wb.getSheetAt(0);
					int rows = sheet.getLastRowNum();
					for (int i = 1; i < rows; i++) {
						Row row = sheet.getRow(i);
						String url = row.getCell(6).getHyperlink().getAddress();
						if (urlCheck.add(url)) {
							RecordEChemPortal ecpr = new RecordEChemPortal();
							ecpr.url = url;
							ecpr.substanceName = new String(row.getCell(0).getStringCellValue().trim().getBytes("UTF-8"));
							ecpr.nameType = row.getCell(1).getStringCellValue().trim();
							ecpr.number = row.getCell(2).getStringCellValue().trim();
							ecpr.numberType = row.getCell(3).getStringCellValue().trim();
							ecpr.memberOfCategory = row.getCell(4).getBooleanCellValue();
							ecpr.participant = row.getCell(5).getStringCellValue().trim();
							ecpr.section = row.getCell(6).getStringCellValue().trim();
							if (ecpr.section.equals("Melting point / freezing point")) { ecpr.section = "Melting / freezing point"; }
							ecpr.getValues(new String(row.getCell(7).getStringCellValue().trim().getBytes("UTF-8")));
							records.add(ecpr);
						} else { countDuplicates++; }
					}
					wb.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		System.out.println("Eliminated "+countDuplicates+" duplicate records by URL");
		return records;
	}
	
	private void getValues(String cellValues) {
		String[] entryArray = cellValues.split("\n");
		for (String entry:entryArray) {
			if (entry!=null && entry.contains(":")) {
				entry = entry.trim();
				String data = entry.substring(entry.indexOf(":")+1).trim().replaceAll("�","").replaceAll("—","-");
				if (entry.startsWith("Reliability")) { reliability = data;
				} else if (entry.startsWith("Type of method")) { method = data;
				} else if (entry.startsWith(section+", "+section.split(" ")[0]) || entry.startsWith(section+", pKa")
						|| entry.startsWith(section+" H, H")) { values.add(data);
				} else if (entry.startsWith(section+", Atm. press.")) { pressure.add(data);
				} else if (entry.startsWith(section+", Temp.")) { temperature.add(data);
				} else if (entry.startsWith(section+", pH")) { pH.add(data);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		// TODO
	}
}