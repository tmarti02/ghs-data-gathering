package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.Block;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.NestedBlock;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.OriginalValue;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.Result;
import gov.epa.exp_data_gathering.eChemPortalAPI.ResultsJSONs.ResultsPage;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;

public class ToxRecordEChemPortalAPIForToxVal {
	public String name;
	public String nameType;
	public String number;
	public String numberType;
	public boolean memberOfCategory;
	public String participant;
	public String url;
	
	String infoType;
	String reliability;
	String endpoint;
	List<String> years;
	List<String> guidelineQualifiers;
	List<String> guidelines;
	String glpCompliance;
	String testType;
	String species;
	String strain;
	String routeOfAdministration;
	String inhalationExposureType;
	String coverageType;
	List<String> doseDescriptors;
	List<String> effectLevels;
	
	public String dateAccessed;
	
	private static String[] headers = {"Name","Name Type","Number","Number Type","Member of Category","Participant","URL","Info Type","Reliability","Endpoint",
			"Years","Guidelines & Qualifiers","GLP Compliance","Test Type","Species","Strain","Route of Administration","Inhalation Exposure Type",
			"Coverage Type","Dose Descriptor","Effect Level"};
	private static String[] fieldNames = {"name","nameType","number","numberType","memberOfCategory","participant","url","infoType","reliability","endpoint",
			"years","guidelineQualifiers+guidelines","glpCompliance","testType","species","strain","routeOfAdministration","inhalationExposureType",
			"coverageType","doseDescriptors","effectLevels"};
	
	public ToxRecordEChemPortalAPIForToxVal() {
		years = new ArrayList<String>();
		guidelineQualifiers = new ArrayList<String>();
		guidelines = new ArrayList<String>();
		doseDescriptors = new ArrayList<String>();
		effectLevels = new ArrayList<String>();
	}
	
	public static void writeToxRecordsToExcel(List<ToxRecordEChemPortalAPIForToxVal> records, String fileName) {
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("Records");
		Row subtotalRow = sheet.createRow(0);
		Row headerRow = sheet.createRow(1);
		String[] headers = ToxRecordEChemPortalAPIForToxVal.headers;
		CellStyle style = wb.createCellStyle();
		Font font = wb.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setFont(font);
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(style);
		}
		Class clazz = ToxRecordEChemPortalAPIForToxVal.class;
		int currentRow = 2;
		for (ToxRecordEChemPortalAPIForToxVal rec:records) {
			String strGuidelinesQualifiers = "";
			for (int g = 0; g < rec.guidelines.size(); g++) {
				if (g > 0) { strGuidelinesQualifiers = strGuidelinesQualifiers + "; "; }
				strGuidelinesQualifiers = strGuidelinesQualifiers + rec.guidelineQualifiers.get(g) + " " + rec.guidelines.get(g);
			}
			String strYears = String.join("; ", rec.years);
			try {
				for (int i = 0; i < rec.doseDescriptors.size(); i++) {
					Row row = null;
					row = sheet.createRow(currentRow);
					currentRow++;
					for (int j = 0; j < headers.length; j++) {
						String header = headers[j];
						String value = null;

						if (header.equals("Guidelines & Qualifiers")) {
							value = strGuidelinesQualifiers;
						} else if (header.equals("Dose Descriptor")) {
							value = rec.doseDescriptors.get(i);
						} else if (header.equals("Effect Level")) {
							value = rec.effectLevels.get(i);
						} else if (header.equals("Years")) {
							value = strYears;
						} else {
							Field field = clazz.getDeclaredField(fieldNames[j]);
							Object objValue = field.get(rec);
							if (objValue!=null) { value = objValue.toString(); }
						}
						
						if (value!=null && !value.isBlank()) { 
							String fixValue = ExperimentalRecords.reverseFixChars(StringEscapeUtils.unescapeHtml4(value));
							row.createCell(j).setCellValue(fixValue);
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		String lastCol = CellReference.convertNumToColString(headers.length);
		sheet.setAutoFilter(CellRangeAddress.valueOf("A2:"+lastCol+currentRow));
		sheet.createFreezePane(0, 2);
		
		for (int i = 0; i < headers.length; i++) {
			String col = CellReference.convertNumToColString(i);
			String recSubtotal = "SUBTOTAL(3,"+col+"$3:"+col+"$"+(currentRow+1)+")";
			subtotalRow.createCell(i).setCellFormula(recSubtotal);
		}
		
		try {
			ParseEChemPortalAPI p = new ParseEChemPortalAPI();
			String filePath = p.mainFolder+File.separator+fileName;
			OutputStream fos = new FileOutputStream(filePath);
			wb.write(fos);
			wb.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static List<ToxRecordEChemPortalAPIForToxVal> getToxResultsInDatabase(String databaseName) {
		ParseEChemPortalAPI p = new ParseEChemPortalAPI();
		String databasePath = p.databaseFolder+File.separator+databaseName;
		List<ToxRecordEChemPortalAPIForToxVal> records = new ArrayList<ToxRecordEChemPortalAPIForToxVal>();
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		
		try {
			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = SQLite_GetRecords.getAllRecords(stat,"results");
			while (rs.next()) {
				String date = rs.getString("date");
				String content = rs.getString("content");
				ResultsPage page = gson.fromJson(content,ResultsPage.class);
				List<Result> results = page.results;
				for (Result r:results) {
					ToxRecordEChemPortalAPIForToxVal rec = new ToxRecordEChemPortalAPIForToxVal();
					rec.url = r.endpointUrl;
					rec.memberOfCategory = r.memberOfCategory;
					rec.participant = r.participantAcronym;
					rec.name = StringEscapeUtils.escapeHtml4(r.name);
					rec.nameType = r.nameType;
					rec.number = r.number;
					rec.numberType = r.numberType;
					rec.dateAccessed = date.substring(0,date.indexOf(" "));
					List<Block> blocks = r.blocks;
					for (Block b:blocks) {
						List<NestedBlock> nestedBlocks = b.nestedBlocks;
						for (NestedBlock nb:nestedBlocks) {
							List<OriginalValue> originalValues = nb.originalValues;
							for (OriginalValue value:originalValues) {
								switch (value.name) {
								case "InfoType":
									rec.infoType = value.value;
									break;
								case "Reliability":
									rec.reliability = value.value;
									break;
								case "Endpoint":
									rec.endpoint = value.value;
									break;
								case "Effect Level":
									rec.effectLevels.add(value.value);
									break;
								case "Dose Descriptor":
									rec.doseDescriptors.add(value.value);
									break;
								case "Test Type":
									rec.testType = value.value;
									break;
								case "Species":
									rec.species = value.value;
									break;
								case "Strain":
									rec.strain = value.value;
									break;
								case "Route of Administration":
									rec.routeOfAdministration = value.value;
									break;
								case "GLP Compliance":
									rec.glpCompliance = value.value;
									break;
								case "Guideline Qualifier":
									rec.guidelineQualifiers.add(value.value);
									break;
								case "Guideline":
									rec.guidelines.add(value.value);
									break;
								case "Year":
									rec.years.add(value.value);
									break;
								case "Inhalation Exposure Type":
									rec.inhalationExposureType = value.value;
									break;
								case "Coverage Type":
									rec.coverageType = value.value;
									break;
								}
							}
						}
					}
					records.add(rec);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return records;
	}
	
	public static void downloadAndWriteAllToxicityResults(boolean startFresh) {
		String[] routes = {"Other"};
		String[] durations = {"AcuteToxicity"};
		for (String r:routes) {
			for (String d:durations) {
				String endpointKind = d + r;
				if (endpointKind.equals("AcuteToxicityOral")) { continue; }
				if (endpointKind.equals("AcuteToxicityDermal")) { continue; }
				ToxQueryOptions options = ToxQueryOptions.generateCompleteToxQueryOptions(endpointKind);
				String databaseName = "Toxicity" + File.separator + "EChemPortalAPI_" + endpointKind + "_RawJSON.db";
				options.runDownload(databaseName, startFresh);
				
				List<ToxRecordEChemPortalAPIForToxVal> records = getToxResultsInDatabase(databaseName);
				
				String excelFileName = "Toxicity" + File.separator + "EChemPortalAPI_" + endpointKind + "_Records.xlsx";
				writeToxRecordsToExcel(records,excelFileName);
			}
		}
	}
	
	public static void main(String[] args) {
		downloadAndWriteAllToxicityResults(true);
	}
}
