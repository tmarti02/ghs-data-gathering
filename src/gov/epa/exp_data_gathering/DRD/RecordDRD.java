package gov.epa.exp_data_gathering.DRD;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.epa.api.ExperimentalConstants;
import gov.epa.exp_data_gathering.parse.DownloadWebpageUtilities;
import gov.epa.exp_data_gathering.parse.Lebrun.RecordLebrun;

public class RecordDRD {
	String studyNr;
	String testChemicalName;
	String casrn;
	String organicFunctionalGroups;
	String physicalFormAsTested;
	String physicalFormConfirmation;
	String dataSource;
	String commercialSource;
	String availablePurity;
	String nrOfStudies;
	String ghsClassification;
	String severityCutOff;
	String severityNrOfAnimals;
	String persistenceCutOff;
	String persistenceNrOfAnimals;
	String specificObservations;
	String specificObsNrOfAnimals;
	String comments;
	String shouldNotBeUsed;
	
	public static final String lastUpdated = "03/26/2021";
	public static final String sourceName = ExperimentalConstants.strSourceDRD;
	
	public static Vector<RecordDRD> parseDRDRecordsFromExcel() {
		Vector<RecordDRD> records = new Vector<RecordDRD>();
		String folderNameExcel = "excel files";
		String mainFolder = "data"+File.separator+"experimental"+ File.separator + sourceName;
		String excelFilePath = mainFolder + File.separator+folderNameExcel;
		File folder = new File(excelFilePath);
		String[] filenames = folder.list();
		for (String filename:filenames) {
			if (filename.endsWith(".xlsx")) {
				try {
					String filepath = excelFilePath+File.separator+filename;
					String date = DownloadWebpageUtilities.getStringCreationDate(filepath);
					if (!date.equals(lastUpdated)) {
						System.out.println(sourceName+" warning: Last updated date does not match creation date of file "+filename);
					}
					FileInputStream fis = new FileInputStream(new File(filepath));
					Workbook wb = new XSSFWorkbook(fis);
					Sheet sheet = wb.getSheetAt(0);
					int rows = sheet.getLastRowNum();
					for (int i = 1; i < rows; i++) {
						Row row = sheet.getRow(i);
						for (Cell cell:row) { cell.setCellType(Cell.CELL_TYPE_STRING); }
						RecordDRD dr = new RecordDRD();
						dr.studyNr = row.getCell(0,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						dr.testChemicalName = StringEscapeUtils.escapeHtml4(row.getCell(1,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
						dr.casrn = row.getCell(2,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						dr.organicFunctionalGroups= row.getCell(3,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						dr.physicalFormAsTested= row.getCell(4,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						dr.physicalFormConfirmation= row.getCell(5,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						dr.dataSource= row.getCell(6,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						dr.commercialSource= row.getCell(7,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						dr.availablePurity= row.getCell(8,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						dr.nrOfStudies= row.getCell(9,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						dr.ghsClassification= row.getCell(10,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						dr.severityCutOff= row.getCell(11,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						dr.severityNrOfAnimals= row.getCell(12,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						dr.persistenceCutOff= row.getCell(13,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						dr.persistenceNrOfAnimals= row.getCell(14,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						dr.specificObservations= row.getCell(15,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						dr.specificObsNrOfAnimals= row.getCell(16,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						dr.comments= row.getCell(17,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						dr.shouldNotBeUsed= row.getCell(18,MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
						records.add(dr);
					}
					wb.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return records;
	}

}
