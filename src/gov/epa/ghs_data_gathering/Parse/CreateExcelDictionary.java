package gov.epa.ghs_data_gathering.Parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.api.AADashboard;
//import gov.epa.api.FlatFileRecord;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

public class CreateExcelDictionary {
	XSSFWorkbook workbook = new XSSFWorkbook();

	public static String[] endpoints = {  
			"Acute Mammalian Toxicity Oral","Acute Mammalian Toxicity Dermal", "Acute Mammalian Toxicity Inhalation", 
			"Carcinogenicity","Genotoxicity Mutagenicity","Endocrine Disruption",
			"Reproductive","Developmental",
			"Neurotoxicity Single Exposure","Neurotoxicity Repeat Exposure", 
			"Systemic Toxicity Repeat Exposure", "Systemic Toxicity Single Exposure",
			"Skin Sensitization","Skin Irritation","Eye Irritation",  
			"Acute Aquatic Toxicity","Chronic Aquatic Toxicity",
			"Bioaccumulation", "Persistence" };

	public static String[] strScores = { "VH", "H", "M", "L", "N/A" };

	void createDictionaryUsingFilesInDictionaryFolder(String folder, String outputFilePath) {

		try {

			File Folder = new File(folder);
			File[] files = Folder.listFiles();

			JsonObject joAll = new JsonObject();
			Vector<String> sources = new Vector<String>();

			getData(files, joAll, sources,"\t");

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			String strAll = gson.toJson(joAll);// convert to JSON string
			System.out.println(strAll);

			goThroughEndpoints(joAll, sources);

			FileOutputStream fileOut = new FileOutputStream(outputFilePath);

			//loop through number of sheets, assign freeze pane to active sheet
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				XSSFSheet activeSheet = workbook.getSheetAt(i);
				activeSheet.createFreezePane(1, 2);
			}

			workbook.write(fileOut);
			workbook.close();
			fileOut.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void goThroughEndpoints(JsonObject joAll, Vector<String> sources) throws Exception {
		for (int i = 0; i < endpoints.length; i++) {
			//
			String endpoint = endpoints[i];
			// String endpoint="Acute Aquatic Toxicity";

			XSSFSheet sheet = workbook.createSheet(endpoint);
			createScoreColumn(sheet);

			int numSources = 0;

			for (int k = 0; k < sources.size(); k++) {
				String source = sources.get(k);

				if (joAll.get(source) == null) {
					System.out.println(source + "\tnull");
					continue;
				}

				JsonObject joSource = joAll.getAsJsonObject(source);

				// System.out.println(endpoint+"\t"+source+"\t"+joSource.isJsonNull());

				if (joSource.get(endpoint) == null)
					continue;

				JsonObject joEndpoint = joSource.getAsJsonObject(endpoint);

				numSources++;
				createCells(joEndpoint, source, sheet, numSources);

			}

		}
	}

	private void getData(File[] files, JsonObject joAll, Vector<String> sources,String del) {
		for (int i = 0; i < files.length; i++) {
			File filei = files[i];
			if (filei.getName().indexOf(".txt") == -1)
				continue;
			if (filei.isDirectory())
				continue;
			String source = filei.getName().substring(0, filei.getName().indexOf("."));
			sources.add(source);
			System.out.println(source);
			Vector<String> lines = getLinesFromFile(filei);
			for (int j = 0; j < lines.size(); j++) {
				String line = lines.get(j);
				storeData(joAll, source, line,del);
			}
		}
	}

	private void storeData(JsonObject joAll, String source, String line,String del) {
		LinkedList<String> l = Utilities.Parse3(line, del);

//		System.out.println(line);
		
		String endpoint = l.get(0);
		String score = l.get(1);

		String category = l.get(2);
		String hazard = l.get(3);
		String cas = l.get(4);

		if (joAll.getAsJsonObject(source) == null) {
			JsonObject joSource = new JsonObject();
			joAll.add(source, joSource);
		}
		JsonObject joSource = joAll.getAsJsonObject(source);

		if (joSource.getAsJsonObject(endpoint) == null) {
			JsonObject joEndpoint = new JsonObject();
			joSource.add(endpoint, joEndpoint);
		}
		JsonObject joEndpoint = joSource.getAsJsonObject(endpoint);

		if (joEndpoint.getAsJsonObject(score) == null) {
			JsonObject joScore = new JsonObject();
			joEndpoint.add(score, joScore);
		}
		JsonObject joScore = joEndpoint.getAsJsonObject(score);

		if (joScore.getAsJsonArray("Category") == null) {

			JsonArray jaCategory = new JsonArray();
			JsonArray jaHazard = new JsonArray();
			JsonArray jaCas = new JsonArray();

			joScore.add("Category", jaCategory);
			joScore.add("Hazard", jaHazard);
			joScore.add("Cas", jaCas);

		}

		JsonArray jaCategory = joScore.getAsJsonArray("Category");
		JsonArray jaHazard = joScore.getAsJsonArray("Hazard");
		JsonArray jaCas = joScore.getAsJsonArray("Cas");

		jaCategory.add(category);
		jaHazard.add(hazard);
		jaCas.add(cas);
	}

	private Vector<String> getLinesFromFile(File filei) {
		Vector<String> lines = new Vector<String>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(filei));
			while (true) {
				String Line = br.readLine();
				if (Line == null)
					break;
				lines.add(Line);
			}

			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return lines;
	}

	private void createScoreColumn(XSSFSheet sheet) {
		sheet.createRow((short) 0);
		sheet.createRow((short) 1);

		CellStyle styleCenter = workbook.createCellStyle();
		styleCenter.setAlignment(CellStyle.ALIGN_CENTER);
		styleCenter.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		for (int i = 0; i < this.strScores.length; i++) {
			XSSFRow row3 = sheet.createRow((short) (i + 2));
			Cell cell = row3.createCell(0);
			cell.setCellValue(this.strScores[i]);
			cell.setCellStyle(styleCenter);
		}
	}

	@SuppressWarnings({ "deprecation", "static-access" })
	private void createCells(JsonObject joEndpoint, String source, XSSFSheet sheet, int numSources) throws Exception {

		int colCategory = 1 + 4 * (numSources - 1);
		int colHazardCode = colCategory + 1;
		int colExampleCAS = colCategory + 2;

		CellStyle styleWrapText = workbook.createCellStyle();
		styleWrapText.setWrapText(true);
		styleWrapText.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		CellStyle styleCenter = workbook.createCellStyle();
		styleCenter.setAlignment(CellStyle.ALIGN_CENTER);
		styleCenter.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		CellStyle styleBottomBorder = workbook.createCellStyle();
		styleBottomBorder.setBorderBottom(BorderStyle.THIN);

		sheet.addMergedRegion(new CellRangeAddress(0, 0, colCategory, colExampleCAS));
		sheet.getRow(0).createCell(colCategory).setCellValue(source);
		sheet.getRow(0).getCell(colCategory).setCellStyle(styleCenter);

		sheet.setColumnWidth(colCategory, 15000);
		sheet.setColumnWidth(colHazardCode, 15000);

		Cell cell1 = sheet.getRow(1).createCell(colCategory);
		cell1.setCellValue("Category");
		cell1.setCellStyle(styleBottomBorder);

		Cell cell2 = sheet.getRow(1).createCell(colHazardCode);
		cell2.setCellValue("Hazard Score");
		cell2.setCellStyle(styleBottomBorder);

		Cell cell3 = sheet.getRow(1).createCell(colExampleCAS);
		cell3.setCellValue("Example CAS");
		cell3.setCellStyle(styleBottomBorder);

		int maxCat = 12;// min size
		int maxHaz = 12;
		int maxCAS = 12;

		for (int i = 0; i < this.strScores.length; i++) {

			if (joEndpoint.getAsJsonObject(this.strScores[i]) == null)
				continue;

			JsonObject joScore = joEndpoint.getAsJsonObject(this.strScores[i]);

			JsonArray jaCategory = joScore.getAsJsonArray("Category");
			JsonArray jaHazard = joScore.getAsJsonArray("Hazard");
			JsonArray jaCas = joScore.getAsJsonArray("Cas");

			for (int j = 0; j < jaCategory.size(); j++) {

				String category = jaCategory.get(j).getAsString();
				String hazardCode = jaHazard.get(j).getAsString();
				String exampleCAS = jaCas.get(j).getAsString();

				if (category.length() > maxCat)
					maxCat = category.length();
				if (hazardCode.length() > maxHaz)
					maxHaz = hazardCode.length();
				if (exampleCAS.length() > maxCAS)
					maxCAS = exampleCAS.length();
			}
		}

		// System.out.println(maxCat+"\t"+maxHaz+"\t"+maxCAS);

		int fac = 256;
		int padding = 2 * 256;

		sheet.setColumnWidth(colCategory, maxCat * fac + padding);
		sheet.setColumnWidth(colHazardCode, maxHaz * fac + padding);
		sheet.setColumnWidth(colExampleCAS, maxCAS * fac + padding);

		for (int i = 0; i < this.strScores.length; i++) {

			Cell cellCategory = sheet.getRow(i + 2).createCell(colCategory);
			Cell cellHazardCode = sheet.getRow(i + 2).createCell(colHazardCode);
			Cell cellExampleCAS = sheet.getRow(i + 2).createCell(colExampleCAS);

			cellCategory.setCellStyle(styleWrapText);
			cellHazardCode.setCellStyle(styleWrapText);
			cellExampleCAS.setCellStyle(styleWrapText);

			if (joEndpoint.getAsJsonObject(this.strScores[i]) == null)
				continue;

			JsonObject joScore = joEndpoint.getAsJsonObject(this.strScores[i]);
			// GsonBuilder builder = new GsonBuilder();
			// builder.setPrettyPrinting();
			// Gson gson = builder.create();
			// String str=gson.toJson(joScore);//convert to JSON string

			// System.out.println(strScores[i]);
			// System.out.println(str);

			JsonArray jaCategory = joScore.getAsJsonArray("Category");
			JsonArray jaHazard = joScore.getAsJsonArray("Hazard");
			JsonArray jaCas = joScore.getAsJsonArray("Cas");

			if (jaCategory != null) {
				String category = getVals(jaCategory);
				cellCategory.setCellValue(category);
			}

			if (jaHazard != null) {
				String hazardCode = getVals(jaHazard);
				cellHazardCode.setCellValue(hazardCode);
			}

			if (jaCas != null) {
				String exampleCAS = getVals(jaCas);
				cellExampleCAS.setCellValue(exampleCAS);
			}

		}
	}

	private String getVals(JsonArray ja) {
		String vals = "";

		for (int j = 0; j < ja.size(); j++) {
			vals += ja.get(j).getAsString();

			if (j < ja.size() - 1) {
				vals += "\n";
			}
		}
		return vals;
	}
	
	void createDictionaryFileFromDashboardSources (String outputFilePath) {
		
		try {


			JsonObject joAll = new JsonObject();
			Vector<String> sources = new Vector<String>();

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			
			goThroughSources(joAll, sources);

			String strAll = gson.toJson(joAll);// convert to JSON string
			System.out.println(strAll);

			goThroughEndpoints(joAll, sources);

			
			FileOutputStream fileOut = new FileOutputStream(outputFilePath);
			//loop through number of sheets, assign freeze pane to active sheet
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				XSSFSheet activeSheet = workbook.getSheetAt(i);
				activeSheet.createFreezePane(1, 2);
			}

			workbook.write(fileOut);
			workbook.close();
			fileOut.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	private void goThroughSources(JsonObject joAll, Vector<String> sources) {
		AADashboard a=new AADashboard();
		String del="\t";
		
		
		for (String source:AADashboard.sources) {
				
			String filePathFlatChemicalRecords = AADashboard.dataFolder+File.separator+"Dictionary"+File.separator+source+".txt";

			File file=new File(filePathFlatChemicalRecords);
				
			if (!file.exists())  {
				System.out.println("*** "+source+" text file missing");
				continue;
			} 
			
			sources.add(source);
			System.out.println(source);
			Vector<String> lines = getLinesFromFile(file);
			for (int j = 0; j < lines.size(); j++) {
				String line = lines.get(j);

				
				storeData(joAll, source, line,del);
			}
		}
	}
	

	public static void main(String[] args) {
		CreateExcelDictionary c = new CreateExcelDictionary();

//		String folder = "AA Dashboard\\Data";
//		c.createDictionaryUsingFilesInDictionaryFolder(folder + "/Dictionary", folder + "/Dictionary/excel/dictionary 2018-05-10.xlsx");

		String strDate = getStringDate();
		 
		String outputFilePath="AA Dashboard/Data/Dictionary/excel/dictionary "+strDate+".xlsx";
		c.createDictionaryFileFromDashboardSources(outputFilePath);
	}

	private static String getStringDate() {
		Date today = new Date(); // 
		SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
		String strDate = format.format(today);
		
		return strDate;
	}

}