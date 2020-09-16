package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.AADashboard;
import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.ghs_data_gathering.Parse.ParseTSCA.TSCA_Record;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

public class ParseUMD extends Parse {

	public ParseUMD() {
		sourceName = ScoreRecord.sourceUMD;
		fileNameSourceExcel = "UMD list of Acute Toxins, Teratogens, Carcinogens, or Mutagens.xls";
		init();
	}
	
	static class UMDRecord {

		String Name;
		String SubstanceID;
		String CASRN;
		String AssayID;
		String AcuteToxin;
		String Mutagen;
		String Teratogen;
		String Carcinogen;

	}

	private Vector<UMDRecord> parseExcelFile(String excelFilePath) {

		try {

			Vector<UMDRecord> data_field = new Vector<UMDRecord>();

			File file = new File(excelFilePath);
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new HSSFWorkbook(inputStream);
			Sheet thirdSheet = workbook.getSheetAt(2);

			int row = 1;

			while (true) {
				Row nextRow = thirdSheet.getRow(row);

				if (nextRow == null) {
					break;
				}

				UMDRecord tr = createDataField(nextRow);

				data_field.add(tr);

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

	private static UMDRecord createDataField(Row row) {
		UMDRecord tr = new UMDRecord();
		DataFormatter formatter = new DataFormatter();

		String cell0 = formatter.formatCellValue(row.getCell(0));
		String cell1 = formatter.formatCellValue(row.getCell(1));
		String cell2 = formatter.formatCellValue(row.getCell(2));
		String cell3 = formatter.formatCellValue(row.getCell(3));
		String cell4 = formatter.formatCellValue(row.getCell(4));
		String cell5 = formatter.formatCellValue(row.getCell(5));
		String cell6 = formatter.formatCellValue(row.getCell(6));
		String cell7 = formatter.formatCellValue(row.getCell(7));

		tr.Name = cell0;
		tr.SubstanceID = cell1;
		tr.CASRN = cell2;
		tr.AssayID = cell3;
		tr.AcuteToxin = " ";
		tr.Mutagen = " ";
		tr.Teratogen = " ";
		tr.Carcinogen = " ";

		if (cell4.contains("Acute Toxin")) {
			tr.AcuteToxin = cell4;
		}

		if (cell4.contains("Mutagen")) {
			tr.Mutagen = cell4;
		}

		if (cell4.contains("Teratogen")) {
			tr.Teratogen = cell4;
		}

		if (cell4.contains("Carcinogen")) {
			tr.Carcinogen = cell4;
		}

		if (cell5.contains("Acute Toxin")) {
			tr.AcuteToxin = cell5;
		}

		if (cell5.contains("Mutagen")) {
			tr.Mutagen = cell5;
		}

		if (cell5.contains("Teratogen")) {
			tr.Teratogen = cell5;
		}

		if (cell5.contains("Carcinogen")) {
			tr.Carcinogen = cell5;
		}

		if (cell6.contains("Acute Toxin")) {
			tr.AcuteToxin = cell6;
		}

		if (cell6.contains("Mutagen")) {
			tr.Mutagen = cell6;
		}

		if (cell6.contains("Teratogen")) {
			tr.Teratogen = cell6;
		}

		if (cell6.contains("Carcinogen")) {
			tr.Carcinogen = cell6;
		}

		if (cell7.contains("Acute Toxin")) {
			tr.AcuteToxin = cell7;
		}

		if (cell7.contains("Mutagen")) {
			tr.Mutagen = cell7;
		}

		if (cell7.contains("Teratogen")) {
			tr.Teratogen = cell7;
		}

		if (cell7.contains("Carcinogen")) {
			tr.Carcinogen = cell7;
		}

		return tr;
	}

	@Override
	protected void createRecords() {
		Vector<UMDRecord> records = parseExcelFile(mainFolder + "/" + fileNameSourceExcel);
		writeOriginalRecordsToFile(records);
	}

	public static void main(String[] args) {

		ParseUMD pd = new ParseUMD();
		pd.createFiles();

	}

	
	@Override
	protected Chemicals goThroughOriginalRecords() {
		Chemicals chemicals=new Chemicals();
		try {
			Gson gson = new Gson();
			File jsonFile = new File(mainFolder + "/" + fileNameJSON_Records);
			UMDRecord[] records = gson.fromJson(new FileReader(jsonFile), UMDRecord[].class);

			for (int i = 0; i < records.length; i++) {

				if (i%100==0) System.out.println(i);
				
				UMDRecord tr = records[i];

//				if (!tr.CASRN.equals("79-06-1")) continue;
				
				Chemical chemical = this.createChemical(tr);
				this.handleMultipleCAS(chemicals, chemical);

			} // end loop over records

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return chemicals;
	}

	private Chemical createChemical(UMDRecord tr) {
		Chemical chemical = new Chemical();

		chemical.CAS = tr.CASRN;
		chemical.name = tr.Name;

//		System.out.println(chemical.CAS);
		this.createScoreRecord(chemical.scoreAcute_Mammalian_ToxicityOral, "Acute toxin", tr.AcuteToxin,ScoreRecord.scoreVH);
		this.createScoreRecord(chemical.scoreGenotoxicity_Mutagenicity, "Mutagen", tr.Mutagen,ScoreRecord.scoreVH);
		this.createScoreRecord(chemical.scoreCarcinogenicity, "Carcinogen", tr.Carcinogen,ScoreRecord.scoreVH);

		return chemical;
	}

	private void createScoreRecord(Score score, String endpoint, String var,String strScore) {

		if (var.toLowerCase().indexOf(endpoint.toLowerCase()) == -1) {
			return;
		}

		var = var.replace("&gt;", ">").replace("&lt;", "<");

		// System.out.println(endpoint+"\t"+var);

		ScoreRecord sr = new ScoreRecord();
		score.records.add(sr);

		sr.source = ScoreRecord.sourceUMD;

		sr.score = strScore;

		sr.category = endpoint;
		sr.rationale = "Score of " + sr.score + " was assigned based on a category of " + sr.category;
		// sr.note=var;

		
//		System.out.println(var);
		
		if (var.indexOf("->") > -1) {// convert note into list to make more readable in web page:
			LinkedList<String> l = Utilities.Parse(var, "->");
			
//			sr.note = "<ul>\r\n";
//			for (int i = 1; i < l.size(); i++) {
//				sr.note += "<li>" + l.get(i) + "</li>\r\n";
//			}
//			sr.note += "</ul>\r\n";
			
			sr.note="";
			
			for (int i = 0; i < l.size(); i++) {
				String val=l.get(i).trim();
				
				if (val.equals("")) continue;
				
				val = val.substring(0, 1).toUpperCase() + val.substring(1);
				
//				System.out.println(i+"\t"+val);
				
				sr.note += val;
				if (i<l.size()-1) sr.note+="<br><br>\r\n";
			}
			
//			System.out.println("note="+sr.note);
			
		} else {
			sr.note = var;
			System.out.println(var);
		}

	}

}