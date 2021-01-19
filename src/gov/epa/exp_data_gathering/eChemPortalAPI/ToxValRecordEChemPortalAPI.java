package gov.epa.exp_data_gathering.eChemPortalAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
import gov.epa.exp_data_gathering.parse.ParseUtilities;

public class ToxValRecordEChemPortalAPI {
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
	Set<String> years;
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
	String histoFindings;
	
	public String dateAccessed;
	
	private static String[] headers = {"Name","Name Type","Number","Number Type","Member of Category","Participant","URL","Info Type","Reliability","Endpoint",
			"Years","Guidelines & Qualifiers","GLP Compliance","Test Type","Species","Strain","Route of Administration","Inhalation Exposure Type",
			"Coverage Type","Dose Descriptor","Effect Level","Histopathological Findings: Neoplastic"};
	private static String[] fieldNames = {"name","nameType","number","numberType","memberOfCategory","participant","url","infoType","reliability","endpoint",
			"years","guidelineQualifiers+guidelines","glpCompliance","testType","species","strain","routeOfAdministration","inhalationExposureType",
			"coverageType","doseDescriptors","effectLevels","histoFindings"};
	
	public ToxValRecordEChemPortalAPI() {
		years = new HashSet<String>();
		guidelineQualifiers = new ArrayList<String>();
		guidelines = new ArrayList<String>();
		doseDescriptors = new ArrayList<String>();
		effectLevels = new ArrayList<String>();
	}
	
	// Checks equality of information, ignores source/URL
	public boolean recordEquals(Object o) {
		if (o==this) {
			return true;
		}
		
		if (!(o instanceof ToxValRecordEChemPortalAPI)) {
			return false;
		}
		
		ToxValRecordEChemPortalAPI r = (ToxValRecordEChemPortalAPI) o;
		if (!Objects.equals(name,r.name) ||
				!Objects.equals(nameType, r.nameType) ||
				!Objects.equals(number, r.number) ||
				!Objects.equals(numberType, r.numberType) ||
				!Objects.equals(memberOfCategory, r.memberOfCategory) ||
				!Objects.equals(infoType, r.infoType) ||
				!Objects.equals(reliability, r.reliability) ||
				!Objects.equals(endpoint, r.endpoint) ||
				!Objects.equals(years, r.years) ||
				!Objects.equals(guidelineQualifiers, r.guidelineQualifiers) ||
				!Objects.equals(guidelines, r.guidelines) ||
				!Objects.equals(glpCompliance, r.glpCompliance) ||
				!Objects.equals(testType, r.testType) ||
				!Objects.equals(species, r.species) ||
				!Objects.equals(strain, r.strain) ||
				!Objects.equals(routeOfAdministration, r.routeOfAdministration) ||
				!Objects.equals(inhalationExposureType, r.inhalationExposureType) ||
				!Objects.equals(coverageType, r.coverageType) ||
				!Objects.equals(doseDescriptors, r.doseDescriptors) ||
				!Objects.equals(effectLevels, r.effectLevels) ||
				!Objects.equals(histoFindings, r.histoFindings)) {
			return false;
		} else {
			return true;
		}
	}
	
	public static void writeToxRecordsToExcel(List<ToxValRecordEChemPortalAPI> records, String fileName) {
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("Records");
		Row subtotalRow = sheet.createRow(0);
		Row headerRow = sheet.createRow(1);
		String[] headers = ToxValRecordEChemPortalAPI.headers;
		CellStyle style = wb.createCellStyle();
		Font font = wb.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setFont(font);
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(style);
		}
		Class clazz = ToxValRecordEChemPortalAPI.class;
		int currentRow = 2;
		for (ToxValRecordEChemPortalAPI rec:records) {
			String strGuidelinesQualifiers = "";
			for (int g = 0; g < rec.guidelines.size(); g++) {
				if (g > 0) { strGuidelinesQualifiers = strGuidelinesQualifiers + "; "; }
				strGuidelinesQualifiers = strGuidelinesQualifiers + rec.guidelineQualifiers.get(g) + " " + rec.guidelines.get(g);
			}
			String strYears = String.join("; ", rec.years);
			try {
				int size = (rec.doseDescriptors==null || rec.doseDescriptors.isEmpty()) ? 1 : rec.doseDescriptors.size();
				for (int i = 0; i < size; i++) {
					Row row = null;
					row = sheet.createRow(currentRow);
					currentRow++;
					for (int j = 0; j < headers.length; j++) {
						String header = headers[j];
						String value = null;

						if (header.equals("Guidelines & Qualifiers")) {
							value = strGuidelinesQualifiers;
						} else if (header.equals("Dose Descriptor")) {
							value = (rec.doseDescriptors==null || rec.doseDescriptors.isEmpty()) ? null : rec.doseDescriptors.get(i);
						} else if (header.equals("Effect Level")) {
							value = (rec.effectLevels==null || rec.effectLevels.isEmpty()) ? null : rec.effectLevels.get(i);
						} else if (header.equals("Years")) {
							value = strYears;
						} else {
							Field field = clazz.getDeclaredField(fieldNames[j]);
							Object objValue = field.get(rec);
							if (objValue!=null) { value = objValue.toString(); }
						}
						
						if (value!=null && !value.isBlank()) { 
							String fixValue = ParseUtilities.reverseFixChars(StringEscapeUtils.unescapeHtml4(value));
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
	
	public static List<ToxValRecordEChemPortalAPI> getToxResultsInDatabase(String databaseName) {
		ParseEChemPortalAPI p = new ParseEChemPortalAPI();
		String databasePath = p.databaseFolder+File.separator+databaseName;
		List<ToxValRecordEChemPortalAPI> records = new ArrayList<ToxValRecordEChemPortalAPI>();
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
					ToxValRecordEChemPortalAPI rec = new ToxValRecordEChemPortalAPI();
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
							for (OriginalValue v:originalValues) {
								switch (v.name) {
								case "InfoType":
									rec.infoType = v.value;
									break;
								case "Reliability":
									rec.reliability = v.value;
									break;
								case "Endpoint":
									rec.endpoint = v.value;
									break;
								case "Effect Level":
									rec.effectLevels.add(v.value);
									break;
								case "Dose Descriptor":
									rec.doseDescriptors.add(v.value);
									break;
								case "Test Type":
									rec.testType = v.value;
									break;
								case "Species":
									rec.species = v.value;
									break;
								case "Strain":
									rec.strain = v.value;
									break;
								case "Route of Administration":
									rec.routeOfAdministration = v.value;
									break;
								case "GLP Compliance":
									rec.glpCompliance = v.value;
									break;
								case "Guideline Qualifier":
									rec.guidelineQualifiers.add(v.value);
									break;
								case "Guideline":
									rec.guidelines.add(v.value);
									break;
								case "After Year":
									String fixAfterYear = (v.value!=null && v.value.contains(";")) ? v.value.substring(v.value.indexOf(";")+1) : v.value;
									rec.years.add(fixAfterYear);
									break;
								case "Before Year":
									String fixBeforeYear = (v.value!=null && v.value.contains(";")) ? v.value.substring(v.value.indexOf(";")+1) : v.value;
									rec.years.add(fixBeforeYear);
									break;
								case "Inhalation Exposure Type":
									rec.inhalationExposureType = v.value;
									break;
								case "Coverage Type":
									rec.coverageType = v.value;
									break;
								case "Histopathological Findings: Neoplastic":
									rec.histoFindings = v.value;
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
//		String[] durations = {"AcuteToxicity","RepeatedDoseToxicity"};
//		String[] routes = {"Oral","Inhalation","Dermal","Other"};
//		for (String d:durations) {
//			for (String r:routes) {
//				String endpointKind = d + r;
//				ToxQueryOptions options = ToxQueryOptions.generateCompleteToxQueryOptions(endpointKind);
//				String databaseName = "Toxicity" + File.separator + "EChemPortalAPI_" + endpointKind + "_RawJSON.db";
//				options.runDownload(databaseName, startFresh);
//				
//				List<ToxValRecordEChemPortalAPI> records = getToxResultsInDatabase(databaseName);
//				records = ToxValRecordDeduplicator.removeDuplicates(records);
//				
//				String excelFileName = "Toxicity" + File.separator + "EChemPortalAPI_" + endpointKind + "_Records.xlsx";
//				writeToxRecordsToExcel(records,excelFileName);
//			}
//		}
		String[] endpointKinds = {/*"Carcinogenicity","ToxicityReproductionP0",*/ "ToxicityReproductionF1","ToxicityToAquaticAlgae"};
		for (String e:endpointKinds) {
			ToxQueryOptions options = ToxQueryOptions.generateCompleteToxQueryOptions(e);
			String databaseName = "Toxicity" + File.separator + "EChemPortalAPI_" + e + "_RawJSON.db";
			options.runDownload(databaseName, startFresh);
			
			List<ToxValRecordEChemPortalAPI> records = getToxResultsInDatabase(databaseName);
			records = ToxValRecordDeduplicator.removeDuplicates(records);
			
			String excelFileName = "Toxicity" + File.separator + "EChemPortalAPI_" + e + "_Records.xlsx";
			writeToxRecordsToExcel(records,excelFileName);
		}
	}
	
	public static void main(String[] args) {
		downloadAndWriteAllToxicityResults(true);
	}
}
