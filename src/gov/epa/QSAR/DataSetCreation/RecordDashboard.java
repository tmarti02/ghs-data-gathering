package gov.epa.QSAR.DataSetCreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Vector;


import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import gov.epa.QSAR.utilities.ExcelUtilities;

public class RecordDashboard {

	public String INPUT;
	public String FOUND_BY;
	public String DTXSID;
	public String PREFERRED_NAME;
	public String CASRN;
	public String INCHIKEY;
	public String IUPAC_NAME;
	public String SMILES;
	public String INCHI_STRING;
	public String MOLECULAR_FORMULA;
	public String QSAR_READY_SMILES;
	public String AVERAGE_MASS;

	static String[] varlist = { "INPUT","FOUND_BY","DTXSID","PREFERRED_NAME","CASRN","INCHIKEY","IUPAC_NAME","SMILES","INCHI_STRING","MOLECULAR_FORMULA","QSAR_READY_SMILES","AVERAGE_MASS"};

	public static String getHeader(String [] varlist) {
		String str="";
		for (int i=0;i<varlist.length;i++) {
			str+=varlist[i];
			if (i<varlist.length-1) str+="\t";
		}
		return str;
	}

	public static String getHeader() {
		return getHeader(varlist);
	}

	public static Vector<RecordDashboard> getDashboardRecordsBatch(String folder,Vector<String>casList, int batchSize) {		
		Vector<RecordDashboard>records=new Vector<>();	
		try {

			String filePath=folder+"RecordsDashboard.txt";			
			FileWriter fw=new FileWriter(filePath);			

			fw.write(RecordDashboard.getHeader()+"\r\n");

			boolean stop=false;
			while (true) {

				Vector<String>casListBatch =new Vector<>();
				for (int i=0;i<batchSize;i++) {
					casListBatch.add(casList.remove(0));
					if (casList.size()==0) {
						stop=true;
						break;
					}
				}

				Vector<RecordDashboard>recsDashboard=RecordDashboard.getDashboardRecordsExcel(casListBatch);

				for (RecordDashboard rec:recsDashboard) {
					fw.write(rec+"\r\n");					
				}
				fw.flush();

				records.addAll(recsDashboard);

				if (stop) {
					break;
				}

			}


			fw.close();

			FileWriter fw2=new FileWriter(folder+"cas list not in dashboard.txt");
			for (RecordDashboard rec:records) {
				if (rec.FOUND_BY.contentEquals("NO_MATCH")) {
					fw2.write(rec.INPUT+"\r\n");
				}
			}
			fw2.flush();
			fw2.close();


		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return records;
	}

	public static Vector<RecordDashboard> getDashboardRecordsFromTextFile(String filepath) {
		Vector<RecordDashboard>records=new Vector<RecordDashboard>();

		try {

			Scanner scanner=new Scanner(new File(filepath));
			String header=scanner.nextLine();
			String [] fieldNames=header.split("\t");

			while (scanner.hasNext()) {
				String Line=scanner.nextLine();

				//				System.out.println(Line);

				String [] fieldValues=Line.split("\t");

				RecordDashboard rec=new RecordDashboard();

				for (int i=0;i<fieldNames.length;i++) {
					rec.setValue(fieldNames[i], fieldValues[i]);
				}
				records.add(rec);

			}

			scanner.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return records;
	}


	public static RecordDashboard getDashboardRecord(String cas) {


		try {
			Unirest.setTimeouts(0, 0);

			String fileType="excel";
			String url="https://comptox-prod.epa.gov/dashboard/batch_search_download";


			String body="input_type=synonym";

			body+="&inputs%5B%5D="+cas;

			body+="&synonym_types%5B%5D=casrn";
			body+="&filetype="+fileType;

			String [] fields={"casrn","inchi","inchi_key","preferred_name","acd_iupac_name","smiles",
					"qsar_ready_smiles","mol_formula","dtxsid","average_mass"};

			for (String field:fields) {
				body+="&output_types%5B%5D="+field;	
			}

			//			System.out.println(body);

			HttpResponse<String> response = Unirest.post(url)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
					.header("Cookie", "font_size=normal")
					.body(body)
					.asString();

			InputStream is = response.getRawBody();

			Workbook workbook = new HSSFWorkbook(is);
			Sheet sheet = workbook.getSheetAt(0);

			Row currentRow = sheet.getRow(0);

			Vector<String>fieldNames=new Vector<>();
			for (int col=0;col<currentRow.getLastCellNum();col++) {
				String fieldName=currentRow.getCell(col).getStringCellValue();
				fieldNames.add(fieldName);
				//				System.out.println(fieldName);
			}

			//			System.out.println(sheet.getLastRowNum());

			DataFormatter formatter = new DataFormatter();

			currentRow = sheet.getRow(1);
			RecordDashboard rec=new RecordDashboard();

			for (int col=0;col<currentRow.getLastCellNum();col++) {

				String fieldValue=formatter.formatCellValue(currentRow.getCell(col)).replace("\n", "").replace("\r", "").trim();		
				//				System.out.println(fieldNames.get(col)+"\t"+ fieldValue);
				rec.setValue(fieldNames.get(col), fieldValue);					
			}				


			workbook.close();
			return rec;	


		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}


	static Vector<RecordDashboard> getRecords(String excelFilePath) {

		try {
			FileInputStream fis=new FileInputStream(excelFilePath);
			return getRecords(fis);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}


	}
	public static Vector<RecordDashboard> getDashboardRecordsExcel(Vector<String>casList) {

		Vector<RecordDashboard>records=new Vector<>();

		try {
			Unirest.setTimeouts(0, 0);

			String fileType="excel";
			String url="https://comptox-prod.epa.gov/dashboard/batch_search_download";


			String body="input_type=synonym";

			for (String cas:casList) 
				body+="&inputs%5B%5D="+cas;

			body+="&synonym_types%5B%5D=casrn";
			body+="&filetype="+fileType;

			String [] fields={"casrn","inchi","inchi_key","preferred_name","acd_iupac_name","smiles",
					"qsar_ready_smiles","mol_formula","dtxsid","average_mass"};

			for (String field:fields) {
				body+="&output_types%5B%5D="+field;	
			}

			//			System.out.println(body);

			HttpResponse<String> response = Unirest.post(url)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
					.header("Cookie", "font_size=normal")
					.body(body)
					.asString();

			InputStream is = response.getRawBody();

			return getRecords(is);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return records;
	}

	private static Vector<RecordDashboard> getRecords(InputStream is) throws IOException {

		Vector<RecordDashboard> records=new Vector<>();
		Workbook workbook = new HSSFWorkbook(is);
		Sheet sheet = workbook.getSheetAt(0);

		Row currentRow = sheet.getRow(0);

		Vector<String>fieldNames=new Vector<>();
		for (int col=0;col<currentRow.getLastCellNum();col++) {
			String fieldName=currentRow.getCell(col).getStringCellValue();
			fieldNames.add(fieldName);
			//				System.out.println(fieldName);
		}

		//			System.out.println(sheet.getLastRowNum());

		DataFormatter formatter = new DataFormatter();

		for (int row=1;row<=sheet.getLastRowNum();row++) {

			currentRow = sheet.getRow(row);
			RecordDashboard rec=new RecordDashboard();

			for (int col=0;col<currentRow.getLastCellNum();col++) {

				String fieldValue=formatter.formatCellValue(currentRow.getCell(col)).replace("\n", "").replace("\r", "").trim();		
				//					System.out.println(fieldNames.get(col)+"\t"+ fieldValue);
				rec.setValue(fieldNames.get(col), fieldValue);					
			}				

			//				System.out.println(rec.toJSON());
			records.add(rec);
		}

		workbook.close();
		return records;

	}


	public String toJSON() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();// makes it multiline and readable
		Gson gson = builder.create();
		return gson.toJson(this);//all in one line!
	}

	public static void main(String[] args) {
		Vector<String>casList=new Vector<String>();
		casList.add("71-43-2");
		//		casList.add("123-45-6");
		//		casList.add("1392-21-8");
		//		getDashboardRecords(casList);
		Vector<RecordDashboard>recs=getDashboardRecordsExcel(casList);

		for (RecordDashboard rec:recs) {
			System.out.println(rec.toJSON());
		}
	}

	public void setValue(String fieldName,String fieldValue) {

		try {
			Field myField =this.getClass().getField(fieldName);				

			myField.set(this, fieldValue);

		} catch (Exception ex){
			ex.printStackTrace();
		}

	}

	public String toString(String[] varlist) {
		String str="";
		for (int i=0;i<varlist.length;i++) {
			try {
				Field myField =this.getClass().getField(varlist[i]);				
				str+=myField.get(this);
				if (i<varlist.length-1) str+="\t";
			} catch (Exception ex){
				ex.printStackTrace();
			}
		}

		return str;
	}

	public String toString() {
		return toString(varlist);
	}

}