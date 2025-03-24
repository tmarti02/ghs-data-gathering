package hazard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.database.SqlUtilities;
import gov.epa.exp_data_gathering.parse.ExcelSourceReader;
import gov.epa.exp_data_gathering.parse.UtilitiesUnirest;
import hazard.HazardChemical.Score;


/**
 * @author TMARTI02
 */
public class CaseStudies {

	String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 scientists\\Tony\\hazard module case studies\\";

	public static String urlBase="https://hcd.rtpnc.epa.gov/api";
//	public static String urlBase = "https://hazard-dev.sciencedataexperts.com/api";
	

	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls()
			.serializeSpecialFloatingPointValues().create();
	public static Gson gsonNoNulls = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
			.serializeSpecialFloatingPointValues().create();

	CaseStudies() {
		UtilitiesUnirest.configUnirest(true);
	}

	class SearchInput {

		String searchType = "SIMILAR";
		String inputType = "SMILES";
		String query;
		Integer offset = 0;
		Integer limit;
		String sortBy = "similarity";
		String sortDirection = "desc";

		Params params = new Params();

	}

//	class ScoreRecord {
//
//		String Hazard_Name;
//		String similarity;
//		String CAS;
//
//		String DTXSID;
//		String SMILES;
//
//		String Name;
//		String Source;
//		String List_Type;
//		String Score;
//		String Rationale;
//		String Route;
//		String Category;
//		String Hazard_Code;
//		String Hazard_Statement;
//
//		String Duration;
//		String Duration_Units;
//		String Test_organism;
//		String Toxicity_Type;
//		String Toxicity_Value;
//		String Toxicity_Value_Units;
//		String Reference;
//		String Note;
//
//		static String[] fields = { "SMILES", "similarity", "CAS", "DTXSID", "Name", "Source", "List_Type", "Score",
//				"Rationale", "Route", "Category", "Hazard_Code", "Hazard_Statement", "Duration", "Duration_Units",
//				"Test_organism", "Toxicity_Type", "Toxicity_Value", "Toxicity_Value_Units", "Reference", "Note" };
//
//		private static ScoreRecord[] getScoreRecords(String cas, String folder) {
//			ExcelSourceReader esr = new ExcelSourceReader();
//
//			String filePath = folder + "hazard_" + cas + ".xlsx";
//
//			System.out.println(filePath);
//
//			// parameter instead?
//			try {
//				FileInputStream fis = new FileInputStream(new File(filePath));
//				Workbook wb = WorkbookFactory.create(fis);
//				esr.sheet = wb.getSheet("Hazard Records");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			Vector<JsonObject> records = esr.parseRecordsFromExcel(3, true);
//
//			String json = gson.toJson(records);
//
//			ScoreRecord[] recs = gson.fromJson(json, ScoreRecord[].class);
//			return recs;
//		}
//
//		private static void createSpreadsheet(TreeMap<String, List<ScoreRecord>> ht, String cas, ScoreRecord sr0,
//				String folder) {
//			try {
//
//				Workbook workbook = new XSSFWorkbook();
//
//				for (String hazardName : ht.keySet()) {
//					List<ScoreRecord> recs = ht.get(hazardName);
//					Sheet sheet = workbook.createSheet(hazardName);
//					writeSheet(recs, sheet, sr0, workbook);
//
////					if(true)break;
//				}
//
//				String fileNameOut = cas + ".xlsx";
//
//				File Folder = new File(folder);
//				Folder.mkdirs();
//
//				System.out.println(folder + fileNameOut);
//
//				FileOutputStream saveExcel = new FileOutputStream(folder + fileNameOut);
//
//				workbook.write(saveExcel);
//				workbook.close();
//
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
//
//		static void writeSheet(List<ScoreRecord> recs, Sheet sheet, ScoreRecord sr0, Workbook workbook) {
//			int irow = 0;
//			Row row1 = sheet.createRow(irow);
//
//			int icol = 0;
//			for (String field : ScoreRecord.fields) {
//				row1.createCell(icol++).setCellValue(field);
//			}
//
//			sheet.setColumnWidth(0, 20 * 256);
//
////			for(int i=1;i<=19;i++) sheet.setColumnWidth(i, 60*256);
//
//			Row row0 = sheet.createRow(++irow);
//			createRow(sheet, irow, sr0, row0);
//
//			CellStyle headerCellStyle = workbook.createCellStyle();
//			headerCellStyle = workbook.createCellStyle();
//			headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
//
//			for (ScoreRecord sr : recs) {
//				Row row = sheet.createRow(++irow);
//				createRow(sheet, irow, sr, row);
//			}
//
//			for (int i = 1; i < ScoreRecord.fields.length; i++) {
//				sheet.autoSizeColumn(i);
//
//				if (sheet.getColumnWidth(i) * 1.20 > 255 * 256) {
//					sheet.setColumnWidth(i, 255 * 256);
//				} else {
//					sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * 1.20));
//				}
//
//				// sheet.setColumnWidth(i, sheet.getColumnWidth(i)+20);
//			}
//
//			for (int i = 0; i < sheet.getLastRowNum(); i++) {
//
//				Row row = sheet.getRow(i);
//				for (int j = 1; j < row.getLastCellNum(); j++) {
//					Cell cell = row.getCell(j);
//					cell.setCellStyle(headerCellStyle);
//				}
//			}
//
//		}
//
//		private static void createRow(Sheet sheet, int irow, ScoreRecord sr, Row row) {
//			int icol;
//			String json = gson.toJson(sr);
//			JsonObject jo = gson.fromJson(json, JsonObject.class);
//
////				System.out.println(gson.toJson(jo));
//
//			icol = 0;
//			for (String field : ScoreRecord.fields) {
////					System.out.println(field);
//				if (jo.get(field) != null) {
//					row.createCell(icol++).setCellValue(jo.get(field).getAsString());
//				} else {
//					row.createCell(icol++).setCellValue("");
//				}
//			}
//
//			row.setHeight((short) 2000);
//			createImage(sr.SMILES, irow, 0, sheet, 1);
//			row.setHeight((short) (2000 * 1.15));// add some space for smiles at bottom
//		}
//	}

	class SearchRecord {
		String DTXCID;
		String DTXSID;
		String Preferred_Name;
		String CAS;
		String Molecular_Formula;
		String SMILES;
		String InChIKey;
		String similarity;

		private static SearchRecord[] getSearchRecords(String cas, String folder) {
			ExcelSourceReader esr = new ExcelSourceReader();

			String filePath = folder + "hazard_" + cas + ".xlsx";

			System.out.println(filePath);

			// parameter instead?
			try {
				FileInputStream fis = new FileInputStream(new File(filePath));
				Workbook wb = WorkbookFactory.create(fis);
				esr.sheet = wb.getSheet("Search");
			} catch (Exception e) {
				e.printStackTrace();
			}

			Vector<JsonObject> records = esr.parseRecordsFromExcel(3, true);

			String json = gson.toJson(records);

			SearchRecord[] recs = gson.fromJson(json, SearchRecord[].class);
			return recs;
		}

	}

//	void go(String cas, String smiles, String dtxsid) {
//
//		ScoreRecord sr0 = new ScoreRecord();
//		sr0.CAS = cas;
//		sr0.SMILES = smiles;
//		sr0.DTXSID = dtxsid;
//		sr0.similarity = "1.0 (test chemical)";
//
//		ScoreRecord[] recs = ScoreRecord.getScoreRecords(cas, folder);
//		SearchRecord[] recsSearch = SearchRecord.getSearchRecords(cas, folder);
//
//		Hashtable<String, SearchRecord> htCASToDTXSID = new Hashtable<>();
//
//		for (SearchRecord sr : recsSearch) {
////			System.out.println(gson.toJson(sr));
//			htCASToDTXSID.put(sr.CAS, sr);
//		}
//
//		List<ScoreRecord> recsKeep = new ArrayList<>();
//
//		List<String> hazardNames = new ArrayList<>();
//
//		TreeMap<String, List<ScoreRecord>> ht = new TreeMap<>();
//
//		int maxAnalogRecords = 999;
//
//		for (ScoreRecord sr : recs) {
//
//			if (!hazardNames.contains(sr.Hazard_Name))
//				hazardNames.add(sr.Hazard_Name);
//
//			if (sr.Score.equals("N/A"))
//				continue;
//			if (sr.List_Type.equals("QSAR Model"))
//				continue;
//
//			sr.DTXSID = htCASToDTXSID.get(sr.CAS).DTXSID;
//			sr.SMILES = htCASToDTXSID.get(sr.CAS).SMILES;
//
//			recsKeep.add(sr);
//
//			if (ht.containsKey(sr.Hazard_Name)) {
//				List<ScoreRecord> recsHazard = ht.get(sr.Hazard_Name);
//
//				if (recsHazard.size() < maxAnalogRecords)
//					recsHazard.add(sr);
//
//			} else {
//				List<ScoreRecord> recsHazard = new ArrayList<>();
//				if (recsHazard.size() < maxAnalogRecords)
//					recsHazard.add(sr);
//				ht.put(sr.Hazard_Name, recsHazard);
//			}
//
////			System.out.println(gson.toJson(sr));
//
//		}
//
////		Collections.sort(hazardNames);
//
////		System.out.println(gson.toJson(ht));
//		ScoreRecord.createSpreadsheet(ht, cas, sr0, folder);
//
//	}

	public static void createImage(String smiles, int startRow, int column, Sheet sheet, int rowspan) {

		Workbook wb = sheet.getWorkbook();
		if (smiles == null || smiles.equals("N/A") || smiles.contains("error"))
			return;
		byte[] imageBytes = StructureImageUtil.generateImageBytesFromSmiles(smiles);
		if (imageBytes == null)
			return;

		int pictureIdx = wb.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);

		// create an anchor with upper left cell column/startRow, only one cell anchor
		// since bottom right depends on resizing
		CreationHelper helper = wb.getCreationHelper();
		ClientAnchor anchor = helper.createClientAnchor();
		anchor.setCol1(column);
		anchor.setRow1(startRow);

		// create a picture anchored to Col1 and Row1
		Drawing drawing = sheet.createDrawingPatriarch();
		Picture pict = drawing.createPicture(anchor, pictureIdx);

		// get the picture width in px
		int pictWidthPx = pict.getImageDimension().width;
		// get the picture height in px
		int pictHeightPx = pict.getImageDimension().height;

		// get column width of column in px
		float columnWidthPx = sheet.getColumnWidthInPixels(column);

		// get the heights of all merged rows in px
		float[] rowHeightsPx = new float[startRow + rowspan];
		float rowsHeightPx = 0f;
		for (int r = startRow; r < startRow + rowspan; r++) {
			Row row = sheet.getRow(r);
			float rowHeightPt = row.getHeightInPoints();
			rowHeightsPx[r - startRow] = rowHeightPt * Units.PIXEL_DPI / Units.POINT_DPI;
			rowsHeightPx += rowHeightsPx[r - startRow];
		}

		// calculate scale
		float scale = 1;
		if (pictHeightPx > rowsHeightPx) {
			float tmpscale = rowsHeightPx / (float) pictHeightPx;
			if (tmpscale < scale)
				scale = tmpscale;
		}
		if (pictWidthPx > columnWidthPx) {
			float tmpscale = columnWidthPx / (float) pictWidthPx;
			if (tmpscale < scale)
				scale = tmpscale;
		}

		// calculate the horizontal center position
		int horCenterPosPx = Math.round(columnWidthPx / 2f - pictWidthPx * scale / 2f);
		// set the horizontal center position as Dx1 of anchor

		anchor.setDx1(horCenterPosPx * Units.EMU_PER_PIXEL); // in unit EMU for XSSF

		// calculate the vertical center position
		int vertCenterPosPx = Math.round(rowsHeightPx / 2f - pictHeightPx * scale / 2f);
		// get Row1
		Integer row1 = null;
		rowsHeightPx = 0f;
		for (int r = 0; r < rowHeightsPx.length; r++) {
			float rowHeightPx = rowHeightsPx[r];
			if (rowsHeightPx + rowHeightPx > vertCenterPosPx) {
				row1 = r + startRow;
				break;
			}
			rowsHeightPx += rowHeightPx;
		}
		// set the vertical center position as Row1 plus Dy1 of anchor
		if (row1 != null) {
			anchor.setRow1(row1);
			if (wb instanceof XSSFWorkbook) {
				anchor.setDy1(Math.round(vertCenterPosPx - rowsHeightPx) * Units.EMU_PER_PIXEL); // in unit EMU for XSSF
			} else if (wb instanceof HSSFWorkbook) {
				// see
				// https://stackoverflow.com/questions/48567203/apache-poi-xssfclientanchor-not-positioning-picture-with-respect-to-dx1-dy1-dx/48607117#48607117
				// for HSSF
				float DEFAULT_ROW_HEIGHT = 12.75f;
				anchor.setDy1(Math.round((vertCenterPosPx - rowsHeightPx) * Units.PIXEL_DPI / Units.POINT_DPI * 14.75f
						* DEFAULT_ROW_HEIGHT / rowHeightsPx[row1]));
			}
		}

		// set Col2 of anchor the same as Col1 as all is in one column
		anchor.setCol2(column);

		// calculate the horizontal end position of picture
		int horCenterEndPosPx = Math.round(horCenterPosPx + pictWidthPx * scale);
		// set set the horizontal end position as Dx2 of anchor

		anchor.setDx2(horCenterEndPosPx * Units.EMU_PER_PIXEL); // in unit EMU for XSSF

		// calculate the vertical end position of picture
		int vertCenterEndPosPx = Math.round(vertCenterPosPx + pictHeightPx * scale);
		// get Row2
		Integer row2 = null;
		rowsHeightPx = 0f;
		for (int r = 0; r < rowHeightsPx.length; r++) {
			float rowHeightPx = rowHeightsPx[r];
			if (rowsHeightPx + rowHeightPx > vertCenterEndPosPx) {
				row2 = r + startRow;
				break;
			}
			rowsHeightPx += rowHeightPx;
		}

		// set the vertical end position as Row2 plus Dy2 of anchor
		if (row2 != null) {
			anchor.setRow2(row2);
			anchor.setDy2(Math.round(vertCenterEndPosPx - rowsHeightPx) * Units.EMU_PER_PIXEL); // in unit EMU for XSSF
		}
	}

	void runSingleChemical() {
//		c.go("26542-47-2","C[Si](C)(CCC=O)O[Si](C)(C)CCC=O","DTXSID80609414");

//		String sid = "DTXSID80609414";
//		String cas = "26542-47-2";
//		String smiles = "C[Si](C)(CCC=O)O[Si](C)(C)CCC=O";
//		String name = "3,3'-(1,1,3,3-Tetramethyldisiloxane-1,3-diyl)dipropanal";
//		Double similarity = 0.27;


//		String sid="DTXSID101382351";
//		String cas="2476411-26-2";
//		String smiles="C1(=CC=C(C=C1)C(=O)OCCCCCCCCC)C(=O)OCCCCCCCCC";
//		String name="1,4-Benzenedicarboxylic acid, 1,4-dinonyl ester, branched and linear";
//		Double similarity=0.86;


//		String sid="DTXSID801382419";
//		String cas="478549-43-8";
////		String smiles="C=CC(=O)OCCOC1=CC=C(C=C1)C(=O)C1=CC=CC=C1 |c:10,12,19,21,t:8,17,lp:3:2,4:2,7:2,15:2,Sg:n:6,5,4::ht|";
//		String smiles="C=CC(=O)OCCOC1=CC=C(C=C1)C(=O)C1=CC=CC=C1";
//		String name="alpha-(1-oxo-2-propenyl)-omega-(4-benzoylphenoxy)-Poly(oxy-1,2-ethanediyl)";
//		Double similarity=0.65;//polymer- need to pick a length...


//		String sid="DTXSID801382352";
//		String cas="35820-92-9";
//		String smiles="CCCCCCCCOC1=CC=C(C=C1)C(=O)C1=CC=CC=C1";
//		String name="[4-(Octyloxy)phenyl]phenylmethanone";
//		Double similarity=0.77;

		// *************************************************************************************
//		String sid="DTXSID20382158";
//		String cas="16627-71-7";
//		String smiles="FC(F)C(F)(F)OCC(F)(F)C(F)(F)C(F)(F)C(F)F";
//		String name="1H,1H,5H-Perfluoropentyl-1,1,2,2-tetrafluoroethylether";
//		Double similarity=0.5;

//		String sid="DTXSID00583621";
//		String cas="6792-31-0";
//		String smiles="FC(=C(C(F)(F)F)C(F)(C(F)(F)F)C(F)(F)F)C(F)(C(F)(F)F)C(F)(F)F";
//		String name="Hexafluoropropene Trimer";
//		Double similarity=0.32;
				
//		String sid="DTXSID401382350";
//		String cas="139204-12-9";
//		String smiles="CC(C)(C)C=1C=C(C=C(C1O)C(C)(C)C)CCC(=O)NN1C(C(CC1=O)C=CCCCCCCCC(C)C)=O";
//		String name="3,5-bis(1,1-dimethylethyl)-4-hydroxy-N-[3-(isododecenyl)-2,5-dioxo-1-pyrrolidinyl]-benzenepropanamide";
//		Double similarity=0.55;

		
//		String sid="DTXSID30987234";
//		Double similarity=0.49;
		
//		DTXSID401382350
//		DTXSID101382351
//		DTXSID801382352
//		DTXSID801382419

		// *************************************************************************************

////		String sid="DTXSID1021740";//butanol
//		String sid="DTXSID0021418";//tryptophan
		
		String sid="DTXSID1021952";//Triphenyl phosphate
		String cas=null;
		String smiles=null;
		String name=null;		
		Double similarity=null;
		
//		Double similarity=0.61;//if null, search for it

		boolean searchApi = true;// to get identifiers from dtxsid using Asif's API

		if (searchApi) {
			JsonObject jo = API_CCTE.searchByDTXSID(sid);
			
			if(jo.get("detail")==null) {//we got a hit
				cas = jo.get("casrn").getAsString();
				name = jo.get("preferredName").getAsString();

				if (smiles == null) {// set it using api value if we havent assigned above
					smiles = jo.get("smiles").getAsString();
				}
			}
			
		}


//		if(true)return;

		if (similarity == null) { // iterate until you get 50 search chemicals with screening hazard record:
			SearchResult sr = SearchResult.search(50, smiles);
//			System.out.println(gsonNoNulls.toJson(sr));
			similarity = sr.request.params.min_similarity;
		}

		boolean overwriteJson = false;
		
		hazard.ScoreRecord scoreRecord0 = new hazard.ScoreRecord(sid, cas, smiles, name,
				"target chemical",null);
		
		scoreRecord0.likelihood="PARENT";
		
		System.out.println(gsonNoNulls.toJson(scoreRecord0));

		urlBase="https://hazard-dev.sciencedataexperts.com/api";
		
//		urlBase="https://ccte-cced-cheminformatics.epa.gov/api";//doesnt work with CTS but works for for 56803-37-3
		getHazardResultsAnalogs(scoreRecord0, similarity, overwriteJson);
				
//		urlBase="https://hcd.rtpnc.epa.gov/api";//works with CTS but gives no output for 56803-37-3
		getHazardResultsCTSAnalogs(scoreRecord0,overwriteJson);

	}

	
	
	

	/**
	 * Regenerate spreadsheets
	 */
	void runChemicals() {
		
		List<hazard.ScoreRecord>scoreRecords=new ArrayList<>();
		
		hazard.ScoreRecord sr=null;
		
		
//		sr=new hazard.ScoreRecord();
//		sr.sid="DTXSID20382158";
//		sr.similarity=0.5+"";
//		scoreRecords.add(sr);

//		sr=new hazard.ScoreRecord();
//		sr.sid="DTXSID00583621";
//		sr.similarity=0.32+"";
//		scoreRecords.add(sr);
		
//		sr=new hazard.ScoreRecord();
//		sr.sid="DTXSID30987234";
//		sr.similarity=0.49+"";
//		scoreRecords.add(sr);

		sr=new hazard.ScoreRecord();
		sr.sid="DTXSID801382352";
		sr.smiles="CCCCCCCCOC1=CC=C(C=C1)C(=O)C1=CC=CC=C1";
		sr.similarity=0.77+"";
		scoreRecords.add(sr);
		
		sr=new hazard.ScoreRecord();
		sr.sid="DTXSID401382350";
		sr.similarity=0.55+"";
		scoreRecords.add(sr);

		sr=new hazard.ScoreRecord();
		sr.sid="DTXSID801382419";
		sr.cas="478549-43-8";
		sr.name="alpha-(1-oxo-2-propenyl)-omega-(4-benzoylphenoxy)-Poly(oxy-1,2-ethanediyl)";
		sr.smiles="C=CC(=O)OCCOC1=CC=C(C=C1)C(=O)C1=CC=CC=C1";
		sr.similarity=0.65+"";
		scoreRecords.add(sr);
		
		sr=new hazard.ScoreRecord();
		sr.sid="DTXSID101382351";
		sr.smiles="C1(=CC=C(C=C1)C(=O)OCCCCCCCCC)C(=O)OCCCCCCCCC";
		sr.cas="2476411-26-2";
		sr.name="1,4-Benzenedicarboxylic acid, 1,4-dinonyl ester, branched and linear";

		sr.similarity=0.86+"";
		scoreRecords.add(sr);
		
		for (hazard.ScoreRecord scoreRecord:scoreRecords) {
			
			
			if (scoreRecord.smiles==null) {
//				System.out.println(scoreRecord.sid);
				JsonObject jo = API_CCTE.searchByDTXSID(scoreRecord.sid);
				
				if(jo.get("detail")==null) {//we got a hit
//					System.out.println(gson.toJson(jo));
					
					scoreRecord.cas = jo.get("casrn").getAsString();
					scoreRecord.name = jo.get("preferredName").getAsString();

					if (scoreRecord.smiles == null) {// set it using api value if we havent assigned above
						scoreRecord.smiles = jo.get("smiles").getAsString();
					}
				}
			}
			
			System.out.println("\n"+scoreRecord.sid+"\t"+scoreRecord.cas+"\t"+scoreRecord.smiles);
			urlBase="https://hazard-dev.sciencedataexperts.com/api";
			
//			urlBase="https://ccte-cced-cheminformatics.epa.gov/api";//doesnt work with CTS but works for for 56803-37-3
//			getHazardResultsAnalogs(scoreRecord, null, false);
			getHazardResultsAnalogs(scoreRecord, Double.parseDouble(scoreRecord.similarity), false);
					
//			urlBase="https://hcd.rtpnc.epa.gov/api";//works with CTS but gives no output for 56803-37-3
//			getHazardResultsCTSAnalogs(scoreRecord,false);
			
			
		}
		
	}
	
	void runExample() {
		
		folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 scientists\\Tony\\hazard module case studies3\\";

		String sid="DTXSID1021952";//Triphenyl phosphate
		String cas=null;
		String smiles=null;
		String name=null;		
		
		Double similarity=null;//if null, search for it		
//		Double similarity=0.61;//set value so dont have to search

		boolean searchApi = true;// to get identifiers from dtxsid using Asif's API
		boolean overwriteJson = false;		
		
		if (searchApi) {
			JsonObject jo = API_CCTE.searchByDTXSID(sid);
			
			if(jo.get("detail")==null) {//we got a hit
				cas = jo.get("casrn").getAsString();
				name = jo.get("preferredName").getAsString();

				if (smiles == null) {// set it using api value if we havent assigned above
					smiles = jo.get("smiles").getAsString();
				}
			}
			
		}

		//Find similarity that gives 50 chemicals with screening level hazards:
		if (similarity == null) { // iterate until you get 50 search chemicals with screening hazard record:
			SearchResult sr = SearchResult.search(50, smiles);
//			System.out.println(gsonNoNulls.toJson(sr));
			similarity = sr.request.params.min_similarity;
		}
		
		hazard.ScoreRecord scoreRecord0 = new hazard.ScoreRecord(sid, cas, smiles, name,
				"target chemical",null);
		
		scoreRecord0.likelihood="PARENT";
		
		System.out.println(gsonNoNulls.toJson(scoreRecord0));

		urlBase="https://hazard-dev.sciencedataexperts.com/api";
		
//		urlBase="https://ccte-cced-cheminformatics.epa.gov/api";//doesnt work with CTS but works for for 56803-37-3
		getHazardResultsAnalogs(scoreRecord0, similarity, overwriteJson);
				
//		urlBase="https://hcd.rtpnc.epa.gov/api";//works with CTS but gives no output for 56803-37-3
		getHazardResultsCTSAnalogs(scoreRecord0,overwriteJson);
		
	}

	public static void main(String[] args) {
		CaseStudies c = new CaseStudies();
		
		
		c.folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 scientists\\Tony\\hazard module case studies2\\";
		
		c.runExample();
//		c.runSingleChemical();
//		c.runChemicals();
	}

	public static short getColorShort(String val) {
		if (val.equals("L")) {
			return IndexedColors.LIGHT_GREEN.getIndex();
		} else if (val.equals("M")) {
			return IndexedColors.LIGHT_YELLOW.getIndex();
		} else if (val.equals("H")) {
			return IndexedColors.LIGHT_ORANGE.getIndex();
		} else if (val.equals("VH")) {
			return IndexedColors.RED.getIndex();
		} else if (val.equals("I")) {
			return IndexedColors.GREY_25_PERCENT.getIndex();
		} else {
			return IndexedColors.WHITE.getIndex();
		}

	}

//	public static Hashtable<String, CellStyle> createScoreStylesHashtable(Workbook workbook) {
//		Hashtable<String, CellStyle> htStyles = new Hashtable<>();
//
//		String[] finalScores = { "VH", "H", "M", "L", "N/A", "I" };
//
//		for (String score : finalScores) {
//			CellStyle cs = workbook.createCellStyle();
//			cs.setVerticalAlignment(VerticalAlignment.CENTER);
//			cs.setAlignment(HorizontalAlignment.CENTER);
//			cs.setBorderBottom(BorderStyle.MEDIUM);
//			cs.setBorderTop(BorderStyle.MEDIUM);
//			cs.setBorderRight(BorderStyle.MEDIUM);
//			cs.setBorderLeft(BorderStyle.MEDIUM);
//			cs.setFillForegroundColor(getColorShort(score));
//			cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//			htStyles.put(score, cs);
//		}
//		
//		htStyles.put("PARENT",htStyles.get("I"));
//		htStyles.put("LIKELY",htStyles.get("L"));
//		htStyles.put("UNLIKELY",htStyles.get("H"));
//		
//		
//		return htStyles;
//	}

	void removeMissingSmiles(SearchResult sr) {
		for (int i = 0; i < sr.records.size(); i++) {
			Chemical chemical = sr.records.get(i);

			if (chemical.smiles == null) {// remove ones w/o smiles
//				System.out.println(gson.toJson(record));
				sr.records.remove(i--);
				sr.recordsCount--;
			}
		}
	}

	HazardResult getHazardResultAnalogs(ScoreRecord sr0, double similarity, File fileHazardJson) {

		hazard.SearchInput si = new hazard.SearchInput();
		
//		System.out.println("SearchInput\n"+gsonNoNulls.toJson(si)+"\n");
		
		
		si.query = sr0.smiles;
		si.params.min_similarity = similarity;
		SearchResult sr = SearchResult.runSearch(si);
//		System.out.println("SearchResult\n"+gsonNoNulls.toJson(sr)+"\n");

		removeMissingSmiles(sr);

//		System.out.println(sr.recordsCount);

		HazardInput hi = new HazardInput();

		for (Chemical chemical : sr.records) {
			chemical.checked = true;
			hi.chemicals.add(new HazardChemical(chemical));
//			hi.chemicals.add(record);
		}
		
		if(!sr.records.get(0).sid.equals(sr0.sid)) {//if similarity search omits target, add it manually:
			
			System.out.println("missing target in analog search, adding "+sr0.sid);
			
			Chemical chemical=new Chemical();
			chemical.sid=sr0.sid;
			chemical.smiles=sr0.smiles;
			chemical.casrn=sr0.cas;
			chemical.name=sr0.name;
			chemical.similarity=1.0;
			
			HazardChemical hazardChemical=new HazardChemical(chemical);
			hazardChemical.scores=new ArrayList<>();
			hi.chemicals.add(0,hazardChemical);
		}
		
//		if(hi.chemicals.get(0).)
		

//		System.out.println("HazardInput\n"+gsonNoNulls.toJson(hi)+"\n");
//		System.out.println(gson.toJson(hi));

		HazardResult hazardResult = HazardResult.runHazard(hi);

		try {
			FileWriter fw = new FileWriter(fileHazardJson);
			fw.write(gsonNoNulls.toJson(hazardResult));
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return hazardResult;
	}
	
	HazardResult getHazardResultCTSAnalogs(hazard.ScoreRecord sr0, File fileHazardJson) {

		HazardInput hi = new HazardInput();

		
		Chemical chemical=new Chemical();
		chemical.casrn=sr0.cas;
		chemical.sid=sr0.sid;
		chemical.smiles=sr0.smiles;
		chemical.name=sr0.name;
		chemical.similarity=1.0;
		
		hi.chemicals.add(new HazardChemical(chemical));
		hi.options.cts="4";
		
		
		System.out.println("HazardInput\n"+gsonNoNulls.toJson(hi)+"\n");
		

		HazardResult hazardResult = HazardResult.runHazard(hi);
//		String strHazardResult = HazardResult.runHazard2(hi);
		
//		System.out.println("HazardOutput\n"+gson.toJson(hazardResult));
//		System.out.println("HazardOutput\n"+strHazardResult);

		try {
			FileWriter fw = new FileWriter(fileHazardJson);
			fw.write(gsonNoNulls.toJson(hazardResult));
//			fw.write(gson.toJson(gson.fromJson(strHazardResult,JsonObject.class)));
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

//		return null;
		return hazardResult;
	}
	
	

	private void getHazardResultsAnalogs(hazard.ScoreRecord sr0, Double similarity, boolean overwriteJson) {
		int maxAnalogRecords = 9999;
		
		sr0.similarity="target chemical";
		
		File folder2=new File(folder+sr0.sid+"\\");
		folder2.mkdirs();


		File fileHazardJson = new File(folder2.getAbsolutePath()+File.separator+sr0.sid + " hazard analog results.json");

		HazardResult hazardResult = null;

		if (!fileHazardJson.exists() || overwriteJson) {
			
			if(similarity==null) {
				System.out.println("No analog json for "+sr0.sid);
				return;
			}
			
			hazardResult = getHazardResultAnalogs(sr0, similarity, fileHazardJson);
		
		} else {
			try {
				System.out.println(fileHazardJson.getName() + " exists using those records");
				hazardResult = gson.fromJson(new FileReader(fileHazardJson), HazardResult.class);
			} catch (Exception ex) {
				ex.printStackTrace();
				return;
			}
		}
		
		
		HazardChemical hc0=null;
		for (int i=0;i<hazardResult.hazardChemicals.size();i++) {
			HazardChemical hc=hazardResult.getHazardChemicals().get(i);
			
			if(hc.chemical.sid!=null && hc.chemical.sid.equals(sr0.sid)) {
				System.out.println("found "+sr0.sid);
				hc0=hazardResult.hazardChemicals.remove(i);
			}
		}
		
		if(hc0!=null) {
			hazardResult.hazardChemicals.add(0, hc0);//put it first	
		} else {
			System.out.println("Target chemical not in hazardResults, adding back");
			Chemical chemical=new Chemical();
			chemical.sid=sr0.sid;
			chemical.smiles=sr0.smiles;
			chemical.casrn=sr0.cas;
			chemical.name=sr0.name;
			chemical.similarity=1.0;
			hc0=new HazardChemical(chemical);
			hazardResult.hazardChemicals.add(0, hc0);//put it first
		}
		
//		System.out.println("HazardResult\n"+gsonNoNulls.toJson(hazardResult)+"\n");

		List<String> hazardNames = new ArrayList<>();
		for (HazardChemical hazardChemical : hazardResult.hazardChemicals) {
			
			if (hazardChemical==null)continue;
			if(hazardChemical.scores==null)continue;
			
			for (Score score : hazardChemical.scores) {
				if (!hazardNames.contains(score.hazardName))
					hazardNames.add(score.hazardName);
			}
		}
		Collections.sort(hazardNames);

		boolean keepNA=false;
		boolean keepI=false;
		boolean keepQSAR=false;
		boolean addLikelihood=false;
		boolean addSimilarity=!addLikelihood;
		boolean filterOutUnlikely=false;


		TreeMap<String, List<hazard.ScoreRecord>> map = getMap(hazardResult, maxAnalogRecords,keepNA,keepI,keepQSAR);

//		System.out.println(gsonNoNulls.toJson(map));

//		String fileNameOut=sr0.cas+" Analog hazard records"+".xlsx";
		String fileNameOut=sr0.sid+" Analog hazard records"+".xlsx";

		
		TableGeneratorExcel tge=new TableGeneratorExcel();
		tge.createHazardTabbedSpreadsheet(map, sr0, folder2,ScoreRecord.fields,hazardResult,fileNameOut,addLikelihood,addSimilarity,filterOutUnlikely);

	}
	
	
	private void getHazardResultsCTSAnalogs(hazard.ScoreRecord sr0, boolean overwriteJson) {
		int maxAnalogRecords = 9999;

		File folder2=new File(folder+sr0.sid);
		folder2.mkdirs();
		
		File fileHazardJson = new File(folder2.getAbsolutePath()+File.separator+sr0.sid + " hazard CTS results.json");

		HazardResult hazardResult = null;

		if (!fileHazardJson.exists() || overwriteJson) {
			hazardResult = getHazardResultCTSAnalogs(sr0, fileHazardJson);
		} else {
			try {
				System.out.println(fileHazardJson.getName() + " exists using those records");
				hazardResult = gson.fromJson(new FileReader(fileHazardJson), HazardResult.class);
			} catch (Exception ex) {
				ex.printStackTrace();
				return;
			}
		}

//		if(true)return;
		
//		System.out.println("HazardResult\n"+gsonNoNulls.toJson(hazardResult)+"\n");

		List<String> hazardNames = new ArrayList<>();
		for (HazardChemical hazardChemical : hazardResult.hazardChemicals) {
			for (Score score : hazardChemical.scores) {
				if (!hazardNames.contains(score.hazardName))
					hazardNames.add(score.hazardName);
			}
		}
		Collections.sort(hazardNames);

		boolean keepNA=false;
		boolean keepI=false;
		boolean keepQSAR=true;
		boolean addLikelihood=true;
		boolean addSimilarity=!addLikelihood;
		boolean filterOutUnlikely=false;
		
		TreeMap<String, List<hazard.ScoreRecord>> map = getMap(hazardResult, maxAnalogRecords,keepNA,keepI,keepQSAR);

//		System.out.println(gsonNoNulls.toJson(map));

//		String fileNameOut=sr0.cas+" CTS Metabolites hazard records"+".xlsx";
		String fileNameOut=sr0.sid+" CTS Metabolites hazard records"+".xlsx";
		
		TableGeneratorExcel tge=new TableGeneratorExcel();
		

		
		tge.createHazardTabbedSpreadsheet(map, sr0, folder2,ScoreRecord.fieldsCTS,hazardResult,fileNameOut,addLikelihood,addSimilarity,filterOutUnlikely);

	}

	/**
	 * Gets map by hazardName, can skip records with score="N/A" or "I", listType="QSAR Model"
	 * 
	 * @param hazardResult
	 * @param maxAnalogRecords
	 * @param keepNA
	 * @param keepI
	 * @param keepQSAR
	 * @return
	 */
	TreeMap<String, List<hazard.ScoreRecord>> getMap(HazardResult hazardResult, int maxAnalogRecords,
			boolean keepNA, boolean keepI, boolean keepQSAR) {

		TreeMap<String, List<hazard.ScoreRecord>> map = new TreeMap<>();

		DecimalFormat df = new DecimalFormat("0.00");

		for (HazardChemical hazardChemical : hazardResult.hazardChemicals) {
			
			if(hazardChemical==null)continue;
			
			if(hazardChemical.scores==null)continue;
			
			for (Score score : hazardChemical.scores) {
				for (hazard.ScoreRecord scoreRecord : score.records) {

					if (!keepNA && scoreRecord.score.equals("N/A"))
						continue;
					
					if (!keepI && scoreRecord.score.equals("I"))
						continue;
					
					if (!keepQSAR && scoreRecord.listType.equals("QSAR Model"))
						continue;

					// For convenience store extra info:
					
					if(hazardChemical.requestChemical.properties.similarity!=null)					
						scoreRecord.similarity = df.format(hazardChemical.requestChemical.properties.similarity);
					
//					scoreRecord.smiles = hazardChemical.requestChemical.chemical.smiles;
//					scoreRecord.sid = hazardChemical.requestChemical.chemical.sid;
//					scoreRecord.cas = hazardChemical.requestChemical.chemical.casrn;//it's not always there in scorerecord

					scoreRecord.smiles = hazardChemical.chemical.smiles;
					scoreRecord.sid = hazardChemical.chemical.sid;
					scoreRecord.cas = hazardChemical.chemical.casrn;//it's not always there in scorerecord
					
					
					if(hazardChemical.requestChemical.properties.ctsChemical!=null) {
						scoreRecord.likelihood=hazardChemical.requestChemical.properties.ctsChemical.likelihood;
					} else {
						scoreRecord.likelihood="PARENT";
					}
					
//					if (hazardChemical.chemical.casrn.equals("1873-90-1")) {
//						System.out.println(gsonNoNulls.toJson(scoreRecord));
//					}

					if (map.containsKey(score.hazardName)) {
						List<hazard.ScoreRecord> recsHazard = map.get(score.hazardName);
						if (recsHazard.size() < maxAnalogRecords)
							recsHazard.add(scoreRecord);
					} else {
						List<hazard.ScoreRecord> recsHazard = new ArrayList<>();
						if (recsHazard.size() < maxAnalogRecords)
							recsHazard.add(scoreRecord);
						map.put(score.hazardName, recsHazard);
					}
				}
			}
		}

		return map;

	}

}
