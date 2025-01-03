package gov.epa.exp_data_gathering.parse.EChemPortalAPI.Processing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query.APIConstants;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query.Query;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query.APIJSONs.Block;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query.APIJSONs.NestedBlock;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query.APIJSONs.OriginalValue;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query.APIJSONs.Result;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Query.APIJSONs.ResultsPage;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Utility.SQLiteDatabase;
import gov.epa.exp_data_gathering.parse.EChemPortalAPI.Utility.TextProcessing;

/**
 * List of FinalRecord objects
 * @author GSINCL01 (Gabriel Sinclair)
 *
 */
public class FinalRecords extends ArrayList<FinalRecord> {

	private static final long serialVersionUID = 4293922701784802887L;

	/**
	 * Maps records by name and number for deduplication
	 * @return		All FinalRecords in current FinalRecords object mapped to their keys
	 */
	Map<String,FinalRecords> toMap() {
		Map<String,FinalRecords>mapRecords=new TreeMap<>();
		for (FinalRecord record:this) {						
			String key=null;
			if (record.number!=null && !record.number.isBlank() && !record.number.equals("unknown") &&
					record.name!=null && !record.name.isBlank() && !record.name.equals("-") && !record.name.contains("unnamed")) {
				key=record.number+"|"+record.name;
			} else if (record.number!=null && !record.number.isBlank() && !record.number.equals("unknown")) {
				key=record.number;
			}  else if (record.name!=null && !record.name.isBlank() && !record.name.equals("-") && !record.name.contains("unnamed")) {
				key=record.name;
			} else {
				continue;
			}

			if (mapRecords.containsKey(key)) {
				FinalRecords recs=mapRecords.get(key);
				recs.add(record);
			} else {
				FinalRecords recs=new FinalRecords();
				recs.add(record);
				mapRecords.put(key, recs);
			}					
		}
		
		return mapRecords;
	}

	/**
	 * Parses results downloaded from eChemPortal in a database into a FinalRecords object
	 * @param databasePath	The database to parse
	 * @return				Results parsed into a FinalRecords object
	 */
	public static FinalRecords getToxResultsInDatabase(String databasePath) {
		FinalRecords records = new FinalRecords();
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		
		try {
			Statement stat = SQLiteDatabase.getStatement(databasePath);
			ResultSet rs = SQLiteDatabase.getAllRecords(stat,"results");
			int id = 1;
			while (rs.next()) {
				String date = rs.getString("date");
				String content = rs.getString("content");
				String query = rs.getString("query");
				Query q = gson.fromJson(query, Query.class);
				String propertyName = q.propertyBlocks.get(0).queryBlock.endpointKind;
				ResultsPage page = gson.fromJson(content,ResultsPage.class);
				List<Result> results = page.results;
				for (Result r:results) {
					FinalRecord rec = new FinalRecord();
					rec.id = "FR"+id++;
					rec.url = r.endpointUrl;
					rec.memberOfCategory = r.memberOfCategory;
					rec.participant = r.participantAcronym;
					rec.name = StringEscapeUtils.escapeHtml4(r.name);
					rec.nameType = r.nameType;
					rec.number = r.number;
					rec.numberType = r.numberType;
					rec.dateAccessed = date.substring(0,date.indexOf(" "));
					rec.propertyName = propertyName;
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
								case APIConstants.endpointType:
									rec.endpointType = v.value;
									break;
								case APIConstants.effectLevel:
									rec.experimentalValues.add(v.value);
									break;
								case APIConstants.effectLevel+" Maternal":
									rec.experimentalValues.add("Maternal: " + v.value);
									break;
								case APIConstants.effectLevel+" Fetal":
									rec.experimentalValues.add("Fetal: " + v.value);
									break;
								case APIConstants.valueType:
									rec.valueTypes.add(v.value);
									break;
								case APIConstants.valueType+" Maternal":
									rec.valueTypes.add("Maternal: " + v.value);
									break;
								case APIConstants.valueType+" Fetal":
									rec.valueTypes.add("Fetal: " + v.value);
									break;
								case APIConstants.testType:
									rec.testType = v.value;
									break;
								case APIConstants.species:
									rec.species.add(v.value);
									break;
								case APIConstants.strain:
									rec.strain = v.value;
									break;
								case APIConstants.routeOfAdministration:
									rec.routeOfAdministration = v.value;
									break;
								case APIConstants.glpCompliance:
									rec.glpCompliance = v.value;
									break;
								case APIConstants.guidelineQualifier:
									rec.guidelineQualifiers.add(v.value);
									break;
								case APIConstants.guideline:
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
								case APIConstants.inhalationExposureType:
									rec.inhalationExposureType = v.value;
									break;
								case APIConstants.coverageType:
									rec.coverageType = v.value;
									break;
								case APIConstants.histoFindingsNeo:
									rec.histoFindings = v.value;
									break;
								case APIConstants.duration:
									rec.durations.add(v.value);
									break;
								case APIConstants.basis:
									rec.basis.add(v.value);
									break;
								case APIConstants.basis+" Maternal":
									rec.basis.add("Maternal: " + v.value);
									break;
								case APIConstants.basis+" Fetal":
									rec.basis.add("Fetal: " + v.value);
									break;
								case APIConstants.interpretationOfResults:
									rec.interpretationOfResults = v.value;
									break;
								case APIConstants.metabolicActivation:
									rec.metabolicActivation.add(v.value);
									break;
								case APIConstants.genotoxicity:
									rec.genotoxicity.add(v.value);
									break;
								case APIConstants.toxicity:
									rec.toxicity.add(v.value);
									break;
								case APIConstants.cytotoxicity:
									rec.cytotoxicity.add(v.value);
									break;
								case APIConstants.oxygenConditions:
									rec.oxygenConditions = v.value;
									break;
								case APIConstants.waterMediaType:
									rec.waterMediaType = v.value;
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
		System.out.println("Parsing complete!");
		return records;
	}

	/**
	 * Writes a FinalRecords object to an Excel spreadsheet
	 * @param filePath		The file to write to
	 */
	public void toExcelFile(String filePath) {
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("Records");
		Row subtotalRow = sheet.createRow(0);
		Row headerRow = sheet.createRow(1);
		String[] headers = FinalRecord.headers;
		CellStyle style = wb.createCellStyle();
		
		Font font = wb.createFont();;//Create font
		font.setBold(true);//Make font bold

		style.setFont(font);
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(style);
		}
		Class clazz = FinalRecord.class;
		int currentRow = 2;
		for (FinalRecord rec:this) {
			String strGuidelinesQualifiers = "";
			for (int g = 0; g < rec.guidelines.size(); g++) {
				if (g > 0) { strGuidelinesQualifiers = strGuidelinesQualifiers + "; "; }
				strGuidelinesQualifiers = strGuidelinesQualifiers + rec.guidelineQualifiers.get(g) + " " + rec.guidelines.get(g);
			}
			String strYears = String.join("; ", rec.years);
			try {
				int size = 1;
				int speciesSize = 1;
				if (rec.valueTypes!=null && !rec.valueTypes.isEmpty()) {
					size = rec.valueTypes.size();
				} else if (rec.genotoxicity!=null && !rec.genotoxicity.isEmpty()) {
					size = rec.genotoxicity.size();
					if (rec.propertyName.equals(APIConstants.geneticToxicityVitro)) { speciesSize = size; }
				}
				for (int i = 0; i < size; i++) {
					Row row = null;
					row = sheet.createRow(currentRow);
					currentRow++;
					for (int j = 0; j < headers.length; j++) {
						String header = headers[j];
						String value = null;
	
						if (header.equals("Guidelines & Qualifiers")) {
							value = strGuidelinesQualifiers;
						} else if (header.equals("Value Type")) {
							value = (rec.valueTypes==null || rec.valueTypes.isEmpty()) ? null : rec.valueTypes.get(i);
						} else if (header.equals("Experimental Value")) {
							value = (rec.experimentalValues==null || rec.experimentalValues.isEmpty()) ? null : rec.experimentalValues.get(i);
						} else if (header.equals("Duration/Sampling Time")) {
							value = (rec.durations==null || rec.durations.isEmpty()) ? null : rec.durations.get(i);
						} else if (header.equals("Basis")) {
							value = (rec.basis==null || rec.basis.isEmpty()) ? null : rec.basis.get(i);
						} else if (header.equals("Genotoxicity")) {
							value = (rec.genotoxicity==null || rec.genotoxicity.isEmpty()) ? null : rec.genotoxicity.get(i);
						} else if (header.equals("Toxicity")) {
							value = (rec.toxicity==null || rec.toxicity.isEmpty()) ? null : rec.toxicity.get(i);
						} else if (header.equals("Cytotoxicity")) {
							value = (rec.cytotoxicity==null || rec.cytotoxicity.isEmpty()) ? null : rec.cytotoxicity.get(i);
						} else if (header.equals("Species") && speciesSize > 1) {
							value = (rec.species==null || rec.species.isEmpty()) ? null : rec.species.get(i);
						} else if (header.equals("Species")) {
							value = (rec.species==null || rec.species.isEmpty()) ? null : rec.species.get(0);
						} else if (header.equals("Metabolic Activation") && speciesSize > 1) {
							value = (rec.metabolicActivation==null || rec.metabolicActivation.isEmpty()) ? null : rec.metabolicActivation.get(i);
						} else if (header.equals("Metabolic Activation")) {
							value = (rec.metabolicActivation==null || rec.metabolicActivation.isEmpty()) ? null : rec.metabolicActivation.get(0);
						} else if (header.equals("Years")) {
							value = strYears;
						} else {
							Field field = clazz.getDeclaredField(FinalRecord.fieldNames[j]);
							Object objValue = field.get(rec);
							if (objValue!=null) { value = objValue.toString(); }
						}
						
						if (value!=null && !value.isBlank()) { 
							String fixValue = TextProcessing.reverseFixChars(StringEscapeUtils.unescapeHtml4(value));
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
		
		FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
		for (int i = 0; i < headers.length; i++) {
			String col = CellReference.convertNumToColString(i);
			String recSubtotal = "SUBTOTAL(3,"+col+"$3:"+col+"$"+(currentRow+1)+")";
			Cell subtotalCell = subtotalRow.createCell(i);
			subtotalCell.setCellFormula(recSubtotal);
			// Hides empty columns
			if (evaluator.evaluate(subtotalCell).getNumberValue()==0) { sheet.setColumnHidden(i, true); }
		}
		
		try {
			System.out.println("Writing to "+filePath+"...");
			OutputStream fos = new FileOutputStream(filePath);
			wb.write(fos);
			wb.close();
			System.out.println("Done!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Writes a FinalRecords object to a JSON file
	 * @param filePath		The file to write to
	 */
	public void toJsonFile(String filePath) {
		try {
			File file = new File(filePath);
			file.getParentFile().mkdirs();

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			builder.disableHtmlEscaping();
			Gson gson = builder.create();

			FileWriter fw = new FileWriter(file);
			fw.write(gson.toJson(this));
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static FinalRecords loadFromJSON(String jsonFilePath) {

		try {
			Gson gson = new Gson();

			File file = new File(jsonFilePath);

			if (!file.exists()) {
				return null;
			}

			FinalRecords chemicals = gson.fromJson(new FileReader(jsonFilePath), FinalRecords.class);			
			return chemicals;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
