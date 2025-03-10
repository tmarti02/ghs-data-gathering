package hazard;


import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.*;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFilterColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFilters;

import gov.epa.ghs_data_gathering.Utilities.Utilities;
import hazard.HazardChemical.Score;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;

/**
 * This class was separated out from TableGenerator to make smaller class
 *
 * @author Todd Martin
 *
 */
public class TableGeneratorExcel {
	XSSFWorkbook workbook;
	Hashtable<String, XSSFCellStyle> styles;
	Hashtable<String, XSSFFont> fonts;
	
	static double targetMW=0;

	public TableGeneratorExcel() {
		workbook = new XSSFWorkbook();
		createStyles();
	}

	private static short getColorShort(String val) {
		
		switch ( val ) {
			case "L":
				return IndexedColors.LIGHT_GREEN.getIndex();
			case "M":
			case "LG":
				return IndexedColors.LIGHT_YELLOW.getIndex();
			case "H":
				return IndexedColors.LIGHT_ORANGE.getIndex();
			case "VH":
				return IndexedColors.RED.getIndex();
			case "I":
				return IndexedColors.GREY_25_PERCENT.getIndex();
								
			case "similarity = 1":
				return IndexedColors.GREY_25_PERCENT.getIndex();
			case "similarity \u2265 0.9":
				return IndexedColors.LIGHT_GREEN.getIndex();
			case "0.8 \u2264 similarity < 0.9":
				return IndexedColors.LIGHT_BLUE.getIndex();
			case "0.7 \u2264 similarity < 0.8":
				return IndexedColors.LIGHT_YELLOW.getIndex();
			case "0.6 \u2264 similarity <0.7":
				return IndexedColors.LIGHT_ORANGE.getIndex();
			case "similarity < 0.6":
				return IndexedColors.RED.getIndex();
			default:
				return IndexedColors.WHITE.getIndex();
		}
		
		
	}
	
	

	
	private static XSSFCellStyle getStyleHyperLink(XSSFWorkbook workbook) {
		XSSFCellStyle hlinkstyle = workbook.createCellStyle();
		XSSFFont hlinkfont = workbook.createFont();
		hlinkfont.setUnderline(XSSFFont.U_SINGLE);
		hlinkfont.setColor(IndexedColors.BLUE.index);
		hlinkstyle.setFont(hlinkfont);
		return hlinkstyle;
	}

	private static XSSFCellStyle getStyleBorder(XSSFWorkbook wb) {
		XSSFCellStyle style = wb.createCellStyle();
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		return style;
	}

	private static XSSFCellStyle getStyleBorderWithRotate(XSSFWorkbook wb) {
		XSSFCellStyle style = wb.createCellStyle();
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setRotation((short) 90);
		return style;
	}

	private static XSSFCellStyle getStyleRotate(XSSFWorkbook wb) {
		XSSFCellStyle style = wb.createCellStyle();
		style.setRotation((short) 90);
		return style;
	}

	private XSSFCellStyle getStyleBold() {
		XSSFCellStyle style = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);
		return style;
	}

	private XSSFCell createMerged(XSSFSheet sheet, int row0, int row1, int col0, int col1, String value, String style) {
		return createMerged(sheet, row0, row1, col0, col1, value, style, null);
	}

	private void setRegionBorder(XSSFSheet sheet, CellRangeAddress range, XSSFCell cell) {
		RegionUtil.setBorderTop(cell.getCellStyle().getBorderTop(), range, sheet);
		RegionUtil.setBorderBottom(cell.getCellStyle().getBorderBottom(), range, sheet);
		RegionUtil.setBorderLeft(cell.getCellStyle().getBorderLeft(), range, sheet);
		RegionUtil.setBorderRight(cell.getCellStyle().getBorderBottom(), range, sheet);
		RegionUtil.setTopBorderColor(cell.getCellStyle().getTopBorderColor(), range, sheet);
		RegionUtil.setBottomBorderColor(cell.getCellStyle().getBottomBorderColor(), range, sheet);
		RegionUtil.setLeftBorderColor(cell.getCellStyle().getLeftBorderColor(), range, sheet);
		RegionUtil.setRightBorderColor(cell.getCellStyle().getRightBorderColor(), range, sheet);
	}

	private XSSFCell createMerged(XSSFSheet sheet, int row0, int row1, int col0, int col1, String value, String style, String font) {
		CellRangeAddress range = new CellRangeAddress(row0, row1, col0, col1);
		XSSFCell cell = createCell(sheet, row0, col0, value, style, font);
		sheet.addMergedRegion(range);
		setRegionBorder(sheet, range, cell);
		return cell;
	}

	private XSSFCell createMerged(XSSFSheet sheet, int row0, int row1, int col0, int col1, String value, XSSFCellStyle style) {
		CellRangeAddress range = new CellRangeAddress(row0, row1, col0, col1);
		XSSFCell cell = createCell(sheet, row0, col0, value, style);
		sheet.addMergedRegion(range);
		setRegionBorder(sheet, range, cell);
		return cell;
	}

	private XSSFCell createCell(XSSFSheet sheet, int row, int col, String value, String style) {
		return createCell(sheet, row, col, value, style, null);
	}

	private XSSFCell createCell(XSSFSheet sheet, int row, int col, String value, String style, String font) {
		XSSFRow xrow = sheet.getRow(row);
		if ( xrow == null )
			xrow = sheet.createRow(row);

		XSSFCell cell = xrow.getCell(col);
		if ( cell == null )
			cell = sheet.getRow(row).createCell(col);

		if ( font == null || !fonts.containsKey(font) )
			cell.setCellValue(value);
		else {
			XSSFRichTextString v = new XSSFRichTextString(value);
			v.applyFont(fonts.get(font));
			cell.setCellValue(v);
		}

		cell.setCellStyle(styles.get(style));

		return cell;
	}

	private XSSFCell createCell(XSSFSheet sheet, int row, int col, String value, XSSFCellStyle style) {
		XSSFRow xrow = sheet.getRow(row);
		if ( xrow == null )
			xrow = sheet.createRow(row);

		XSSFCell cell = xrow.getCell(col);
		if ( cell == null )
			cell = sheet.getRow(row).createCell(col);

		cell.setCellValue(value);
		cell.setCellStyle(style);

		return cell;
	}
	
	
	public static String getSimString(double SCi) {

		char gte = '\u2265';//converting to ? for some reason
		char lte = '\u2264';
		
//		String gte = ">=";
//		String lte = "<=";

		if (SCi >0.9999) {
			return "similarity = 1";
		} else if (SCi >= 0.9) {
			return "similarity "+gte+" 0.9";
		} else if (SCi < 0.9 && SCi >= 0.8) {
			return "0.8 "+lte+" similarity < 0.9";
		} else if (SCi < 0.8 && SCi >= 0.7) {
			return "0.7 "+lte+" similarity < 0.8";
		} else if (SCi < 0.7 && SCi >= 0.6) {
			return "0.6 "+lte+" similarity <0.7";
		} else  {
			return "similarity < 0.6";
		}

	}

	private void createStyles() {
		styles = new Hashtable<>();
		fonts = new Hashtable<>();

		XSSFFont font = workbook.createFont();
		font.setBold(true);
		fonts.put("B", font);

		font = workbook.createFont();
		font.setItalic(true);
		fonts.put("I", font);

		font = workbook.createFont();
		font.setBold(true);
		font.setItalic(true);
		fonts.put("BI", font);

		for ( String score: Arrays.asList("VH", "H", "M", "L", "ND", "I", "LG", "D") ) {
			XSSFCellStyle cs = workbook.createCellStyle();

			if ( !Arrays.asList("LG", "D").contains(score) ) {
				cs.setVerticalAlignment(VerticalAlignment.CENTER);
				cs.setAlignment(HorizontalAlignment.CENTER);
			}

			cs.setBorderBottom(BorderStyle.THIN);
			cs.setBorderTop(BorderStyle.THIN);
			cs.setBorderRight(BorderStyle.THIN);
			cs.setBorderLeft(BorderStyle.THIN);
			cs.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
			cs.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
			cs.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
			cs.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());

			if ( score.equals("LG") )
				cs.setWrapText(true);

			cs.setFillForegroundColor(getColorShort(score));
			cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			styles.put(score, cs);
		}
		
		XSSFCellStyle cs = workbook.createCellStyle();
		cs.setFillForegroundColor(getColorShort("I"));
		cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styles.put("PARENT", cs);
		
		cs = workbook.createCellStyle();
		cs.setFillForegroundColor(getColorShort("L"));
		cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styles.put("LIKELY", cs);

		cs = workbook.createCellStyle();
		cs.setFillForegroundColor(getColorShort("H"));
		cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styles.put("UNLIKELY", cs);
		
		
		for (double sim=0.5;sim<=1.1;sim+=0.05) {
			String simString=getSimString(sim);
			if(styles.containsKey(simString)) continue;
			
//			System.out.println(sim+"\t"+simString);
			cs = workbook.createCellStyle();
			cs.setVerticalAlignment(VerticalAlignment.CENTER);//messes things up if use in final score tab and hazard specific tabs
			cs.setAlignment(HorizontalAlignment.CENTER);
			cs.setFillForegroundColor(getColorShort(simString));
			cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			styles.put(simString, cs);
		}
		
		
		cs = workbook.createCellStyle();
		cs.setVerticalAlignment(VerticalAlignment.CENTER);//messes things up if use in final score tab and hazard specific tabs
		cs.setAlignment(HorizontalAlignment.CENTER);
		cs.setWrapText(true);
		styles.put("sid", cs);
		
		cs = workbook.createCellStyle();
		cs.setVerticalAlignment(VerticalAlignment.BOTTOM);//messes things up if use in final score tab and hazard specific tabs
		cs.setAlignment(HorizontalAlignment.LEFT);
//		cs.setWrapText(true);
		styles.put("structure", cs);


		XSSFCellStyle styleURL=createStyleURL(workbook);
		styles.put("url", styleURL);
		
		XSSFCellStyle headerCellStyle = createHeaderStyle(workbook);
		styles.put("header", headerCellStyle);
		
		
		styles.put("PARENT",styles.get("I"));
		styles.put("LIKELY",styles.get("L"));
		styles.put("UNLIKELY",styles.get("H"));
		styles.put("PROBABLE",styles.get("M"));

		
	}

	/***
	 * This version adds code to customize the fields
	 *
	 * @param chemicals
	 */
	public void writeScoreRecordsToWorkbookAdvanced(HazardResult chemicals) {
		try {
			
			XSSFCellStyle hlinkstyle=styles.get("url");
			
			String del = "|";

			ArrayList<String> uniqueCAS = new ArrayList<>();

			//Create a blank sheet
			XSSFSheet sheet = workbook.createSheet("Hazard Records");

			int rowNum = 1;
			XSSFRow row = sheet.createRow(rowNum);
			XSSFCellStyle styleBold = getStyleBold();

			//Write header
			for ( int i = 0; i < ScoreRecord.displayFieldNamesExcel.length; i++ ) {
				XSSFCell cell = row.createCell(i);
				cell.setCellValue(ScoreRecord.displayFieldNamesExcel[i]);
				cell.setCellStyle(styleBold);
			}

			//create hyper link style
//			XSSFCellStyle hlinkstyle = getStyleHyperLink(workbook);

			for ( HazardChemical chemical: chemicals.getHazardChemicals() ) {

				if ( !uniqueCAS.contains(chemical.getChemical().getCasrn()) )
					uniqueCAS.add(chemical.getChemical().getCasrn());

				for ( int i = 0; i < chemical.getScores().size(); i++ ) {

					Score score = chemical.getScores().get(i);

					for ( int j = 0; j < score.records.size(); j++ ) {

						ScoreRecord record = score.records.get(j);

						rowNum++;
						row = sheet.createRow(rowNum);


						for ( int k = 0; k < ScoreRecord.actualFieldNamesExcel.length; k++ ) {
							XSSFCell cell = row.createCell(k);

							String fieldName = ScoreRecord.actualFieldNamesExcel[k];
							Field myField = record.getClass().getField(fieldName);

							String val;

							if ( myField.get(record) == null )
								val = "";
							else if ( myField.getType().toString().contains("Double") )
								val = formatDoubleValue((Double) myField.get(record));
							else
								val = (String) myField.get(record);

							while ( val.contains("<br><br><br>") ) {
								val = val.replace("<br><br><br>", "<br><br>");
								val = val.replace("<br>", "\n");
							}

							if ( val.length() > 32000 )
								val = val.substring(0, 32000) + "...";

							if ( fieldName.equals("hazardName") ) {
								val = score.getHazardName();
							} else if ( fieldName.equals("CAS") ) {
								val = chemical.getChemical().casrn;
							} else if ( fieldName.equals("name") ) {
								val = chemical.getChemical().name;
							} else if ( fieldName.equals("score") ) {
								cell.setCellStyle(styles.get(record.score));
							} else if ( fieldName.contentEquals("valueMass") ) {
								//TODO should operator be separate column?
								if ( record.valueMassOperator != null && !record.valueMassOperator.contentEquals("=") ) {
									val = record.valueMassOperator + " " + val;
								}
							} else if ( fieldName.contentEquals("source") ) {
								if ( !StringUtils.isEmpty(record.url) ) {
									if ( record.source.contentEquals(HazardConstants.sourceJapan) ) {
										String[] urls = record.url.replace(" Revised by ", "").split(";");
										setHyperlink(workbook, urls[0], cell, hlinkstyle);
									} else if ( !record.source.contentEquals(HazardConstants.sourceToxVal) ) {
										setHyperlink(workbook, record.url, cell, hlinkstyle);
									}
								}
							} else if ( fieldName.contentEquals("sourceOriginal") ) {
								if ( !val.isEmpty() && !StringUtils.isEmpty(record.url) )
									setHyperlink(workbook, record.url, cell, hlinkstyle);
							}

							if ( fieldName.contentEquals("valueMass") && !val.isBlank() ) {
								try {
									//Convert to double so that Excel doesnt display error that number is stored as a string:
									cell.setCellValue(Double.parseDouble(val));
								} catch (Exception ex) {
								}
							} else {
								cell.setCellValue(val);	
							}
						}
					}
				}
			}
			
			Row recSubtotalRow = sheet.createRow(0);

			for (int i = 0; i < ScoreRecord.actualFieldNamesExcel.length; i++) {
				String col = CellReference.convertNumToColString(i);
				String recSubtotal = "SUBTOTAL(3,"+col+"$3:"+col+"$"+(rowNum+1)+")";
				recSubtotalRow.createCell(i).setCellFormula(recSubtotal);
			}
						
			int lastRow=rowNum;
			Row rowHeader=sheet.getRow(1);
						
			int lastColumn=rowHeader.getLastCellNum()-1;
			CellAddress caLast=new CellAddress(lastRow,lastColumn);
			sheet.setAutoFilter(CellRangeAddress.valueOf("A2:"+caLast.formatAsString()));


			for ( int i = 0; i < 20; i++ ) {
				sheet.autoSizeColumn(i);
				if ( sheet.getColumnWidth(i) > 50 * 256 )
					sheet.setColumnWidth(i, 50 * 256);
			}

			sheet.createFreezePane(3, 2, 3, 2);

		} catch ( Exception ex ) {
			ex.printStackTrace();
		}

	}
	
	public static String formatDoubleValue(double dose) {

		if (dose==0) return "";
		
		DecimalFormat df = new DecimalFormat("0.00");
		DecimalFormat df2 = new DecimalFormat("0");
		DecimalFormat dfSci = new DecimalFormat("0.00E00");

		double doseRoundDown = Math.floor(dose);

		double percentDifference = Math.abs(doseRoundDown - dose) / dose * 100.0;

		if (dose < 0.01) {
			return dfSci.format(dose);
		} else {
			if (percentDifference > 0.1) {
				return df.format(dose);
			} else {
				return df2.format(dose);
			}
		}

	}

	void setHyperlink(XSSFWorkbook workbook, String url, XSSFCell cell, XSSFCellStyle hlinkstyle) {
		try {
			//TODO: for now use the last link if have multiple
			if ( url.contains("<br>") ) {
				String[] urls = url.split("<br>");
				url = urls[urls.length - 1].trim();
			}

//			url = URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
//			System.out.println(url);

			Hyperlink link = workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
			link.setAddress(url);
			cell.setHyperlink(link);
			cell.setCellStyle(hlinkstyle);
		} catch ( Exception ex ) {
			System.out.println("Bad url:" + url);
		}
	}

	public void writeFinalScoresToWorkbook(ScoreRecord sr0, HazardResult hazardResult,boolean addLikelihood, boolean addSimilarity) {
		//Create a blank sheet
		XSSFSheet sheet = workbook.createSheet("Hazard Profiles");

		DecimalFormat df=new DecimalFormat("0.00");

		XSSFDrawing drawing = sheet.createDrawingPatriarch();
		
		XSSFCellStyle styleBorderWithRotate = getStyleBorderWithRotate(workbook);
		XSSFCellStyle styleBorder = getStyleBorder(workbook);
		XSSFCellStyle styleHlink = getStyleHyperLink(workbook);

		XSSFFont fontBold = workbook.createFont();
		fontBold.setBold(true);
		XSSFFont fontItalic = workbook.createFont();
		fontItalic.setItalic(true);

		final String disclaimer = "The Cheminformatics Modules is a set of prototype modules which are using a compilation of information sourced from many sites, databases and sources including U.S. Federal and state sources and international bodies that saves the user time by providing information in one location. The data are not reviewed by USEPA â the user must apply judgment in use of the information. The results do not indicate EPAâs position on the use or regulation of these chemicals.";
		createMerged(sheet, 0, 0, 0, 22, disclaimer, "LG");
		sheet.getRow(0).setHeightInPoints(35);

		createMerged(sheet, 1, 1, 3, 4, "VH - Very High", "VH");
		createMerged(sheet, 1, 1, 5, 6, "H - High", "H");
		createMerged(sheet, 1, 1, 7, 8, "M - Medium", "M");
		createMerged(sheet, 1, 1, 9, 10, "L - Low", "L");
		createMerged(sheet, 1, 1, 11, 12, "I - Inconclusive", "I");
		createMerged(sheet, 1, 1, 13, 14, "No Data", "ND");

		createMerged(sheet, 1, 1, 16, 17, "Authoritative", "A", "B");
		createMerged(sheet, 1, 1, 18, 19, "Screening", "S");
		createMerged(sheet, 1, 1, 20, 22, "QSAR Model", "Q", "I");

		final int iRow = 2;
		createMerged(sheet, iRow, iRow + 2, 0, 0, "Chemical", styleBorder);
		createMerged(sheet, iRow, iRow + 2, 1, 1, "Smiles", styleBorder);
		
		if(addSimilarity)
			createMerged(sheet, iRow, iRow + 2, 2, 2, "Similarity", styleBorder);
		else 
			createMerged(sheet, iRow, iRow + 2, 2, 2, "Likelihood", styleBorder);
		
		createMerged(sheet, iRow, iRow, 3, 17, "Human Health Effects", styleBorder);
		createMerged(sheet, iRow, iRow, 18, 19, "Ecotoxicity", styleBorder);
		createMerged(sheet, iRow, iRow, 20, 22, "Fate", styleBorder);

		createMerged(sheet, iRow + 1, iRow + 1, 3, 5, HazardConstants.strAcute_Mammalian_Toxicity, styleBorder);
		createMerged(sheet, iRow + 1, iRow + 1, 11, 12, HazardConstants.strNeurotoxicity, styleBorder);
		createMerged(sheet, iRow + 1, iRow + 1, 13, 14, HazardConstants.strSystemic_Toxicity, styleBorder);

		createCell(sheet, iRow + 2, 3, "Oral", styleBorderWithRotate);
		createCell(sheet, iRow + 2, 4, "Inhalation", styleBorderWithRotate);
		createCell(sheet, iRow + 2, 5, "Dermal", styleBorderWithRotate);

		createMerged(sheet, iRow + 1, iRow + 2, 6, 6, HazardConstants.strCarcinogenicity, styleBorderWithRotate);
		createMerged(sheet, iRow + 1, iRow + 2, 7, 7, HazardConstants.strGenotoxicity_Mutagenicity, styleBorderWithRotate);
		createMerged(sheet, iRow + 1, iRow + 2, 8, 8, HazardConstants.strEndocrine_Disruption, styleBorderWithRotate);
		createMerged(sheet, iRow + 1, iRow + 2, 9, 9, HazardConstants.strReproductive, styleBorderWithRotate);
		createMerged(sheet, iRow + 1, iRow + 2, 10, 10, HazardConstants.strDevelopmental, styleBorderWithRotate);

		createCell(sheet, iRow + 2, 11, "Repeat Exposure", styleBorderWithRotate);
		createCell(sheet, iRow + 2, 12, "Single Exposure", styleBorderWithRotate);

		createCell(sheet, iRow + 2, 13, "Repeat Exposure", styleBorderWithRotate);
		createCell(sheet, iRow + 2, 14, "Single Exposure", styleBorderWithRotate);

		createMerged(sheet, iRow + 1, iRow + 2, 15, 15, HazardConstants.strSkin_Sensitization, styleBorderWithRotate);
		createMerged(sheet, iRow + 1, iRow + 2, 16, 16, HazardConstants.strSkin_Irritation, styleBorderWithRotate);
		createMerged(sheet, iRow + 1, iRow + 2, 17, 17, HazardConstants.strEye_Irritation, styleBorderWithRotate);

		createMerged(sheet, iRow + 1, iRow + 2, 18, 18, HazardConstants.strAcute_Aquatic_Toxicity, styleBorderWithRotate);
		createMerged(sheet, iRow + 1, iRow + 2, 19, 19, HazardConstants.strChronic_Aquatic_Toxicity, styleBorderWithRotate);
		createMerged(sheet, iRow + 1, iRow + 2, 20, 20, HazardConstants.strPersistence, styleBorderWithRotate);
		createMerged(sheet, iRow + 1, iRow + 2, 21, 21, HazardConstants.strBioaccumulation, styleBorderWithRotate);
		createMerged(sheet, iRow + 1, iRow + 2, 22, 22, HazardConstants.strExposure, styleBorderWithRotate);

		sheet.getRow(iRow + 2).setHeightInPoints(115);

		for ( int i = 0; i < hazardResult.getHazardChemicals().size(); i++ ) {
			XSSFRow row = sheet.createRow(i + iRow + 3);

			HazardChemical chemical = hazardResult.getHazardChemicals().get(i);
			
			if(chemical==null)continue;
			
			Cell cellSid=createCellSID(row, chemical);
			createCellSmiles(row, chemical);
			if(addSimilarity) createCellSimilarity(sr0,df, drawing, row, chemical);
			if(addLikelihood) createCellLikelihood(sr0,drawing, row, chemical);
			createScoreCells(sheet, iRow, i, chemical);
		}
		
//		ColumnInsert ci=new ColumnInsert();
//		ci.insertNewColumnBefore(sheet, 0);
		
//		sheet.autoSizeColumn(0);
//		sheet.autoSizeColumn(1);
//		sheet.autoSizeColumn(2);
//		if ( sheet.getColumnWidth(2) > 30 * 256 )
//			sheet.setColumnWidth(2, 30 * 256);

		sheet.setColumnWidth(0, 35 * 256);//chemical
		sheet.setColumnWidth(1, 30 * 256);//structure
		sheet.setColumnWidth(2, 15 * 256);//sim/likelihood

		sheet.createFreezePane(0, iRow + 3, 0, iRow + 3);
		
		//add autofilter
		String lastCol = CellReference.convertNumToColString(sheet.getRow(2).getLastCellNum()-1);
		
		System.out.println(hazardResult.getHazardChemicals().size());
		
		System.out.println(hazardResult.getHazardChemicals().size());
		
		if(hazardResult.getHazardChemicals().size()>1)
			sheet.setAutoFilter(CellRangeAddress.valueOf("A5:"+lastCol+hazardResult.getHazardChemicals().size()));

		
		//Add structures:
		for ( int i = 0; i < hazardResult.getHazardChemicals().size(); i++ ) {
			XSSFRow row = sheet.getRow(i + iRow + 3);
			HazardChemical chemical = hazardResult.getHazardChemicals().get(i);
			row.setHeight((short)2000);
			
			String smiles=null;
			
			if(chemical==null)continue;
			
			if(chemical.getChemical()!=null) {
				smiles=chemical.getChemical().getSmiles();
			} else {
				smiles=chemical.requestChemical.chemical.getSmiles();
			}

			if(smiles!=null) {
				CaseStudies.createImage(chemical.getChemical().getSmiles(), i + iRow + 3, 1, sheet, 1);
			}
			
			row.setHeight((short)(2000*1.15));//add some space for smiles at bottom
		}

		
	}

	private void createScoreCells(XSSFSheet sheet, final int iRow, int i, HazardChemical chemical) {
		
		if(chemical.getScores()==null)return;
		List<Score> scores = chemical.getScores();
		for ( int j = 0; j < scores.size(); j++ ) {
			String score = scores.get(j).getFinalScore();
			String authority = scores.get(j).getFinalAuthority();
			String font;
			if ( authority == null )
				font = null;
			else if ( authority.equals("Authoritative") )
				font = "B";
			else if ( authority.equals("QSAR Model") )
				font = "I";
			else
				font = null;

			createCell(sheet, i + iRow + 3, j + 3, score.equals("ND") ? "" : score, score, font);
		}
	}

	private void createCellLikelihood(ScoreRecord sr0, XSSFDrawing drawing, XSSFRow row, HazardChemical chemical) {
		Cell cellLikelihood=row.createCell(2);
		
		if(chemical.requestChemical.properties.ctsChemical!=null && chemical.requestChemical.properties.ctsChemical.likelihood!=null) {				
			cellLikelihood.setCellStyle(styles.get(chemical.requestChemical.properties.ctsChemical.likelihood));
//			XSSFComment comment1 = drawing.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 4, 2, (short) 5, 3));  
//			comment1.setString(new XSSFRichTextString(chemical.requestChemical.properties.ctsChemical.likelihood));  
//			cellLikelihood.setCellComment(comment1);
			cellLikelihood.setCellValue(chemical.requestChemical.properties.ctsChemical.likelihood);  
		} else {
			cellLikelihood.setCellStyle(styles.get("PARENT"));
//			XSSFComment comment1 = drawing.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 4, 2, (short) 5, 3));  
//			comment1.setString(new XSSFRichTextString("PARENT"));  
//			cellLikelihood.setCellComment(comment1);
			cellLikelihood.setCellValue("PARENT");  
		}
		
		
//		if(sr0.sid.equals(chemical.))
		
	}

	private void createCellSimilarity(ScoreRecord sr0, DecimalFormat df, XSSFDrawing drawing, XSSFRow row, HazardChemical chemical) {
		Cell cellSimilarity=row.createCell(2);
		
		if(chemical.requestChemical!=null && chemical.requestChemical.properties.similarity!=null) {

			String simString=getSimString(chemical.requestChemical.properties.similarity);
			
			XSSFCellStyle style=styles.get(simString);
			
			if(style==null) {
				System.out.println("Null style for "+simString+"\t"+chemical.requestChemical.properties.similarity);
			} else {
				cellSimilarity.setCellStyle(style);
			}
			
			if(simString.equals("target chemical")) {
				cellSimilarity.setCellValue("target chemical");
			} else {
				cellSimilarity.setCellValue(df.format(chemical.requestChemical.properties.similarity));
			}
			
//			XSSFComment comment1 = drawing.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 4, 2, (short) 7, 3));  
//			comment1.setString(new XSSFRichTextString(simString));  
//			cellSimilarity.setCellComment(comment1);  
		} else {
			

			if(sr0.similarity.equals("target chemical")) {
				cellSimilarity.setCellValue("target chemical");
				cellSimilarity.setCellStyle(styles.get("PARENT"));
			} else {
				System.out.println("here not target but dont have req chemical");
				String simString=getSimString(chemical.chemical.similarity);
				cellSimilarity.setCellValue(df.format(chemical.chemical.similarity));
				XSSFCellStyle style=styles.get(simString);
				if(style==null) {
					System.out.println("Null style for "+simString+"\t"+chemical.requestChemical.properties.similarity);
				} else {
					cellSimilarity.setCellStyle(style);
				}

			}
			
//			System.out.println("Here set target chemical");
			
//			System.out.println(CaseStudies.gsonNoNulls.toJson(chemical));
			
//			cellSimilarity.setCellValue(chemical.chemical.similarity);
//					XSSFComment comment1 = drawing.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 4, 2, (short) 5, 3));  
//					comment1.setString(new XSSFRichTextString("PARENT"));  
//					row.getCell(2).setCellComment(comment1);  
		}
		
//		if(chemical.requestChemical.chemical.sid.equals(sr0.sid)) {
//			cellSimilarity.setCellValue("target chemical");
//		}
		
	}

	private void createCellSmiles(XSSFRow row, HazardChemical chemical) {
		
		Cell cell=row.createCell(1);
		
		if(chemical.getChemical()!=null) {
			cell.setCellValue(chemical.getChemical().getSmiles());
		} else {
			cell.setCellValue(chemical.requestChemical.chemical.getSmiles());
		}
		
		cell.setCellStyle(styles.get("structure"));
		
	}

	private XSSFCell createCellSID(XSSFRow row, HazardChemical chemical) {
		
		XSSFCell cellSid = row.createCell(0);
		
		String casrn="";
		String name="";
		
		if(chemical.getChemical()!=null) {
			casrn=chemical.getChemical().getCasrn();
			name=chemical.getChemical().getName();
		} else {
			casrn=chemical.requestChemical.chemical.getCasrn();
			name=chemical.requestChemical.chemical.getName();
		}
		
		if(casrn==null) {
			System.out.println(CaseStudies.gsonNoNulls.toJson(chemical));
		}
		
		cellSid.setCellValue(name+"\n\n"+casrn+"\n\n"+chemical.getChemical().getSid());
		cellSid.setCellStyle(styles.get("sid"));
		
		setHyperlink(workbook, "https://comptox.epa.gov/dashboard/chemical/details/"+chemical.getChemical().getSid(), cellSid, styles.get("url"));
		
//		System.out.println(cellSid.getCellStyle().getWrapText());
		return cellSid;
	}

	public File write(Path path) {
		File file = path.toFile();

		try ( FileOutputStream out = new FileOutputStream(file) ) {
			workbook.write(out);
		} catch ( Exception ex ) {
			ex.printStackTrace();
//			log.error(ex.getMessage(), ex);
		}

		return file;
	}
	
	
	public void createHazardTabbedSpreadsheet(TreeMap<String, List<ScoreRecord>> ht,ScoreRecord sr0,
			File folder,String []fields,HazardResult hazardResult,String fileNameOut,
			boolean addLikelihood,boolean addSimilarity,boolean filterOutUnlikely) {
		try {
			
//			XSSFWorkbook workbook = new XSSFWorkbook();
			
			if (hazardResult!=null) {
				writeFinalScoresToWorkbook(sr0,hazardResult,addLikelihood,addSimilarity);
			}

					
			for (String hazardName:ht.keySet()) {
				List<ScoreRecord>recs=ht.get(hazardName);
				Sheet sheet=workbook.createSheet(hazardName);
				writeSheet(recs, sheet,sr0,workbook,fields,filterOutUnlikely);
//				if(true)break;
			}
			
			File fileout=new File(folder.getAbsolutePath()+File.separator+fileNameOut);

			System.out.println(fileout.getAbsolutePath());

			FileOutputStream saveExcel = new FileOutputStream(fileout);

			workbook.write(saveExcel);
			workbook.close();
			
			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}

	private static XSSFCellStyle createHeaderStyle(Workbook workbook) {
		XSSFCellStyle headerCellStyle = (XSSFCellStyle) workbook.createCellStyle();
		headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
		headerCellStyle.setWrapText(true);
		return headerCellStyle;
	}

	static XSSFCellStyle createStyleURL(Workbook workbook) {
		XSSFCellStyle hlink_style = (XSSFCellStyle) workbook.createCellStyle();
		Font hlink_font = workbook.createFont();
		hlink_font.setUnderline(Font.U_SINGLE);
		hlink_font.setColor(Font.COLOR_NORMAL);
		hlink_style.setFont(hlink_font);
		
		hlink_style.setVerticalAlignment(VerticalAlignment.CENTER);
		hlink_style.setAlignment(HorizontalAlignment.LEFT);
		hlink_style.setWrapText(true);
		return hlink_style;
	}

	private void setCellStyles(Sheet sheet, Row rowSubtotal,String []fields) {
		
		Workbook workbook=sheet.getWorkbook();
		
//		Hashtable<String, CellStyle>htStyles=CaseStudies.createScoreStylesHashtable(workbook);
		
		for (int i=0;i<=2;i++) {
			Row row=sheet.getRow(i);
			for (int j=0;j<row.getLastCellNum();j++) {
				Cell cell=row.getCell(j);
				cell.setCellStyle(styles.get("header"));
			}
		}
		
		
		for (int i=3;i<sheet.getPhysicalNumberOfRows();i++) {
			Row row=sheet.getRow(i);
			for (int j=1;j<row.getLastCellNum();j++) {
				
				Cell cell=row.getCell(j);
									
				if(fields[j].equals("score") && i!=0) {
					cell.setCellStyle(styles.get(cell.getStringCellValue()));
				} else if (fields[j].equals("likelihood")) {
					cell.setCellStyle(styles.get(cell.getStringCellValue()));
				} else if (fields[j].equals("similarity")) {
					
					String strSimilarity=cell.getStringCellValue();
					Double similarity=null;
					
					if(strSimilarity.equals("target chemical")) similarity=1.0;
					else similarity=Double.parseDouble(strSimilarity);
	
					String simString=getSimString(similarity);
					XSSFCellStyle style=styles.get(simString);
					
					if(style==null) {
						System.out.println("Missing style for "+simString+"\t"+similarity);
					}
					
					cell.setCellStyle(style);
	
				} else if (fields[j].equals("url")) {
					cell.setCellStyle(styles.get("url"));
				} else {
					cell.setCellStyle(styles.get("header"));
				}
			}
		}		
		
	}

	private static void createRow(Sheet sheet, int irow, ScoreRecord sr, Row row,String []fields) {
			
	//		Gson gson=new Gson();
	//		String json=gson.toJson(sr);
	//		JsonObject jo=gson.fromJson(json, JsonObject.class);
	//			System.out.println(gson.toJson(jo));
	
			setMW(sr,irow);
					
			int icol=0;
			for (String fieldName:fields) {
				//				System.out.println(field);
				Cell cell=row.createCell(icol++);
				try {
					Field reflectField=ScoreRecord.class.getField(fieldName);
	
					if(reflectField.get(sr)!=null) {
						if(reflectField.getType().toString().contains("Double")) {
							cell.setCellValue((Double)reflectField.get(sr));
						} else {
							cell.setCellValue((String)reflectField.get(sr));
						}
						
						if(fieldName.equals("url")) {
							try {
								Hyperlink href = sheet.getWorkbook().getCreationHelper().createHyperlink(HyperlinkType.URL);
								href.setAddress(sr.url);
								cell.setHyperlink(href);
							} catch (Exception ex) {
								//System.out.println("Invalid url:\t"+strValue);
							}
						}
						
					} else {
	//					cell.setCellValue("");
					}
	
				} catch (Exception e) {
					System.out.println(fieldName+"\t"+e.getMessage());
					//	e.printStackTrace();
				}
			}
			
			row.setHeight((short)2000);
			CaseStudies.createImage(sr.smiles, irow, 0, sheet, 1);
			row.setHeight((short)(2000*1.15));//add some space for smiles at bottom
			
		}

	private void writeSheet(List<ScoreRecord> recs, Sheet sheet,
				ScoreRecord sr0, Workbook workbook,String []fields,boolean filterOutUnlikely) {
			
			
			int irow=0;
			Row rowSubtotal = sheet.createRow(irow++);
			Row rowBlank = sheet.createRow(irow++);
					
			Row rowHeader = sheet.createRow(irow);
			
			int icol=0;
			for (String field:fields) {
				rowHeader.createCell(icol++).setCellValue(field);	
			}
					
			sheet.setColumnWidth(0, 20*256);
	
	//		for(int i=1;i<=19;i++) sheet.setColumnWidth(i, 60*256);
					
			Row rowTargetChemical = sheet.createRow(++irow);
			createRow(sheet, irow, sr0, rowTargetChemical,fields);
			
	
			
			for (ScoreRecord sr:recs) {
				Row row = sheet.createRow(++irow);
				createRow(sheet, irow, sr, row, fields);
			}
			
			int maxWidth=50;
			
			for (int i=1;i<fields.length;i++) {
				sheet.autoSizeColumn(i);
				
				if(sheet.getColumnWidth(i)*1.20 > maxWidth*256) {
					sheet.setColumnWidth(i, maxWidth*256);
				} else {
					sheet.setColumnWidth(i, (int)(sheet.getColumnWidth(i)*1.25));	
				}
				
				//sheet.setColumnWidth(i, sheet.getColumnWidth(i)+20);
			}
			
			
			String lastCol = CellReference.convertNumToColString(fields.length-1);
			sheet.setAutoFilter(CellRangeAddress.valueOf("A3:"+lastCol+sheet.getPhysicalNumberOfRows()));
			sheet.createFreezePane(1, 4);
	

			if(filterOutUnlikely) {
				for (int i=0;i<fields.length;i++) {
					if (fields[i].equals("likelihood")) {
						try {
							setCriteriaFilter(sheet, i, 3, sheet.getPhysicalNumberOfRows(), new String[]{"LIKELY","PARENT"});
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			
			
	
			
			for (int i = 0; i < fields.length; i++) {
				String col = CellReference.convertNumToColString(i);
				String recSubtotal = "SUBTOTAL(3,"+col+"$4:"+col+"$"+(sheet.getPhysicalNumberOfRows())+")";
				rowSubtotal.createCell(i).setCellFormula(recSubtotal);
			}
			
			//Set styles
			setCellStyles(sheet, rowSubtotal,fields);
			
		}

	private static void setMW(ScoreRecord sr, int irow) {
		
		if(sr.smiles==null) return;
		
		String smiles=sr.smiles;
		SmilesParser parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		MolecularFormulaManipulator mfm=new MolecularFormulaManipulator();
		
		try {			
			IAtomContainer ac = parser.parseSmiles(smiles);
			IMolecularFormula mf=mfm.getMolecularFormula(ac);
			double mw=mfm.getMass(mf,MolecularFormulaManipulator.MolWeight);	
			
			if(irow==3)targetMW=mw;
			
			DecimalFormat df=new DecimalFormat("0.00");
			
			sr.molWeight=Double.parseDouble(df.format(mw));
			sr.molWeightDiff=Double.parseDouble(df.format(Math.abs(mw-targetMW)));
			
			
		} catch (InvalidSmilesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void setCriteriaFilter(Sheet sheet, int colId, int firstRow, int lastRow, String[] criteria) throws Exception {
		
		XSSFSheet sheet2=(XSSFSheet)sheet;
		CTAutoFilter ctAutoFilter = sheet2.getCTWorksheet().getAutoFilter();
		
		CTFilterColumn ctFilterColumn = null;
		for (CTFilterColumn filterColumn : ctAutoFilter.getFilterColumnList()) {
			if (filterColumn.getColId() == colId) ctFilterColumn = filterColumn;
		}
		if (ctFilterColumn == null) ctFilterColumn = ctAutoFilter.addNewFilterColumn();
		ctFilterColumn.setColId(colId);
		if (ctFilterColumn.isSetFilters()) ctFilterColumn.unsetFilters();
	
		CTFilters ctFilters = ctFilterColumn.addNewFilters();
		for (int i = 0; i < criteria.length; i++) {
			ctFilters.addNewFilter().setVal(criteria[i]);
		}
	
		//hiding the rows not matching the criterias
		DataFormatter dataformatter = new DataFormatter();
		for (int r = firstRow; r <= lastRow; r++) {
			XSSFRow row = sheet2.getRow(r);
			
			if(row==null) continue;
			
			boolean hidden = true;
			for (int i = 0; i < criteria.length; i++) {
				String cellValue = dataformatter.formatCellValue(row.getCell(colId));
				if (criteria[i].equals(cellValue)) hidden = false;
			}
			if (hidden) {
				row.getCTRow().setHidden(hidden);
			} else {
				if (row.getCTRow().getHidden()) row.getCTRow().unsetHidden();
			}
		}
	}
}
