package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.CanadaRecord.Record2015;

/* 
 * add comments fields from Hazard field only
 */
public class ParseCanada extends Parse {

	String fileNameSourceExcel = "\\Canada Data.xlsx";

	Multimap<String,String>codeToCategory=CodeDictionary.populateCodeToCategoryCanada();
	
	public ParseCanada() {
		sourceName = ScoreRecord.sourceCanada;
		fileNameHtmlZip = "Canada_Webpages.zip";
		init();
	}

	private Vector<CanadaRecord> parseExcelFile(String excelFilePath) {

		try {

			Vector<CanadaRecord> data_field = new Vector<CanadaRecord>();

			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet firstSheet = workbook.getSheetAt(0);

			int row = 1;

			while (true) {
				Row nextRow = firstSheet.getRow(row);

				if (nextRow == null) {
					break;
				}

				CanadaRecord ar = createDataField(nextRow);

				data_field.add(ar);

				row++;
			}

			inputStream.close();
			workbook.close();
			return data_field;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private static CanadaRecord createDataField(Row row) {
		CanadaRecord cr = new CanadaRecord();
		DataFormatter formatter = new DataFormatter();

		Cell cell0 = row.getCell(0);
		Cell cell1 = row.getCell(1);
		Cell cell2 = row.getCell(2);
		Cell cell3 = row.getCell(3);
		Cell cell4 = row.getCell(4);
		Cell cell5 = row.getCell(5);
		Cell cell6 = row.getCell(6);
		Cell cell7 = row.getCell(7);
		Cell cell8 = row.getCell(8);
		Cell cell9 = row.getCell(9);

		cr.Name = formatter.formatCellValue(cell0);
		cr.CAS = formatter.formatCellValue(cell1);
		cr.WHMIS_1988_Classification = formatter.formatCellValue(cell2);
		cr.WHMIS_1988_ClassificationNote = formatter.formatCellValue(cell3);
		cr.WHMIS_1988_Classifiction_Comments = formatter.formatCellValue(cell4);
		cr.WHMIS_2015_Classification = formatter.formatCellValue(cell5);
		cr.WHMIS_2015_Classification_Note = formatter.formatCellValue(cell6);
		cr.Hazard_Statement = formatter.formatCellValue(cell7);
		cr.Hazard_Code = formatter.formatCellValue(cell8);
		cr.Hazard_Comments = formatter.formatCellValue(cell9);

		return cr;
	}

	@Override
	protected void createRecords() {
//		Vector<CanadaRecord> records = parseExcelFile(mainFolder + "/" + fileNameSourceExcel);
		
		Vector<CanadaRecord> records=CanadaRecord.parseChemicalWebpagesInZipFile(mainFolder + "/" + this.fileNameHtmlZip);
		
		writeOriginalRecordsToFile(records);
	}
	
	

	
	private Chemical createChemical(CanadaRecord cr) {

		Chemical chemical = new Chemical();

		if (cr==null) return null;
		if (cr.CAS!=null) chemical.CAS = cr.CAS.trim();
		
		chemical.name = cr.Name.trim();
		
		Vector<String>classifications=new Vector<String>();
		
		for (Record2015 r:cr.vec2015_Classification) {
			if (r.classification.contains("\n")) {
				r.classification=r.classification.substring(0,r.classification.indexOf("\n"));
			}
			if (r.classification.contains("<ul>")) {
				r.classification=r.classification.substring(0,r.classification.indexOf("<ul>"));
			}
			r.classification=r.classification.trim();
			classifications.add(r.classification);
		}
		
		
		for (Record2015 r:cr.vec2015_Classification) {
			 
			if (r.hazardCode==null) continue;
//			System.out.println(cr.CAS+"\t"+r.hazardCode);
			
			if (dictCodeToScoreName.get(r.hazardCode) == null) continue;
			
			List<String> listScore = (List<String>) dictCodeToScoreName.get(r.hazardCode);			

			for(String scoreName:listScore) {
				if(scoreName.equals("Omit")) continue;
				Score score = chemical.getScore(scoreName);
				if (this.dictCodeToScoreValue.get(r.hazardCode) == null) {
					System.out.println(chemical.CAS + "\t" + scoreName + "\t" + r.hazardCode + "\tmissing");
					continue;
				}
				
				String toxRoute="";
				toxRoute=this.getRoute(scoreName, toxRoute);
				
//				System.out.println(r.classification);
				
				
				String classification=resolveClassification(cr, classifications, r);
				
//				System.out.println(r.classification+"\t"+codeToCategory.get(r.hazardCode));
				
				
				String strScore = dictCodeToScoreValue.get(r.hazardCode);
				this.createRecord(chemical,score, classification, r.hazardCode, r.hazardStatement,toxRoute, strScore, cr.WHMIS_2015_Classification_Note);
				
				
				ScoreRecord sr = score.records.get(score.records.size()-1);
				

				
			}
			
		}
		return chemical;
	}

	/**
	 * Sometimes they dont put them all in the right order so need to fix it
	 * 
	 * @param cr
	 * @param classifications
	 * @param r
	 * @return
	 */
	private String resolveClassification(CanadaRecord cr, Vector<String> classifications, Record2015 r) {
		String strClassification="";
		
		if (this.codeToCategory.get(r.hazardCode).contains(r.classification)) {
			classifications.remove(r.classification);
			strClassification=r.classification;
			
//					System.out.println(cr.CAS+"\t"+r.hazardCode+"\tOK");
		} else {
			
			boolean haveClassification=false;
			
			for (String classification:classifications) {
				if (codeToCategory.get(r.hazardCode).contains(classification)) {
					classifications.remove(r.classification);
					haveClassification=true;
					strClassification=classification;
					break;
				}
			}
			
			if (!haveClassification) {
				List<String> listCategory = (List<String>)	codeToCategory.get(r.hazardCode);
				
				//Just use the correct one from multimap- only happens once in database:
				strClassification=listCategory.get(0);
				
				System.out.println(cr.CAS+"\t"+r.classification+"\t"+r.hazardCode+"\tmissing");

				for (String category:listCategory) {
					System.out.println("From listCategory:"+category);
				}
				
				for (String classification:classifications) {
					System.out.println("Remaining classification:"+classification);
				}

				System.out.println("r.classification="+r.classification+"\n");
				
			}
		}
		
		return strClassification;
	}
	
	@Override
	protected Chemicals goThroughOriginalRecords() {
		
		Chemicals chemicals=new Chemicals();
		
		try {
			Gson gson = new Gson();
			File jsonFile = new File(mainFolder + "/" + this.fileNameJSON_Records);
			CanadaRecord[] records = gson.fromJson(new FileReader(jsonFile), CanadaRecord[].class);
			
			for (int i = 0; i < records.length; i++) {
				if (i%1000==0) System.out.println(i);
				CanadaRecord ir = records[i];
				Chemical chemical = this.createChemical(ir);
				if (chemical == null)	continue;
				handleMultipleCAS(chemicals, chemical);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}


	public static void main(String[] args) {

		ParseCanada pc = new ParseCanada();
//		pc.createRecords();
		pc.createFiles();

	}

}
