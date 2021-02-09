package gov.epa.QSAR.DataSetCreation.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.QSAR.DataSetCreation.UtilitiesUnirest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class RecordsDashboard extends ArrayList<RecordDashboard> {
	
	
	
	
	public static RecordsDashboard loadFromJson(String filePath) {

		Gson gson = new Gson();		
		try {
			RecordsDashboard recordsDashboard = gson.fromJson(new FileReader(filePath), RecordsDashboard.class);
			return recordsDashboard;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void toJSON_File(String filePath) {

		try {

			File file = new File(filePath);
			file.getParentFile().mkdirs();

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting().disableHtmlEscaping();
			Gson gson = builder.create();

			FileWriter fw = new FileWriter(file);
			fw.write(gson.toJson(this));
			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	public static RecordsDashboard getDashboardRecordsFromTextFile(String filepath) {
		RecordsDashboard records=new RecordsDashboard();

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
	
	public static RecordsDashboard  getDashboardRecordsBatch(String folder,Vector<String>casList, int batchSize) {		
		RecordsDashboard records=new RecordsDashboard ();	
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

				RecordsDashboard recsDashboard=RecordsDashboard.getDashboardRecordsExcelCAS(casListBatch);

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

	public static RecordsDashboard  getDashboardRecordsExcelCAS(Vector<String>casList) {

		RecordsDashboard records=new RecordsDashboard ();

		try {
			UtilitiesUnirest.configUnirest(true);

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

			HttpResponse<byte[]> response = Unirest.post(url)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
					.header("Cookie", "font_size=normal")
					.body(body)
					.asBytes();

//			InputStream is = response.getRawBody();
			InputStream is = new ByteArrayInputStream(response.getBody());

			return getRecords(is);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return records;
	}
	
	public static RecordsDashboard getRecords(String excelFilePath) {

		try {
			FileInputStream fis=new FileInputStream(excelFilePath);
			return getRecords(fis);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}


	}
	
	private static RecordsDashboard getRecords(InputStream is) throws IOException {

		RecordsDashboard records=new RecordsDashboard();
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
	public static RecordsDashboard getBigListFromSID(Vector<String>sidList) {
		RecordsDashboard recordsDashboardAll=new RecordsDashboard();
		
		int count=0;
		while (true) {
			Vector<String>sidListi=new Vector<>();			
			count++;
			for (int i=0;i<1000;i++) {
				sidListi.add(sidList.remove(0));
				if (sidList.size()==0) break;
			}
			RecordsDashboard recordsDashboard=RecordsDashboard.getDashboardRecordsExcelSID(sidListi);
			
			recordsDashboardAll.addAll(recordsDashboard);
			
			if (sidList.size()==0) break;
		}
		return recordsDashboardAll;
	}
	public static RecordsDashboard getDashboardRecordsExcelSID(Vector<String>sidList) {

		RecordsDashboard records=new RecordsDashboard();

		try {
			UtilitiesUnirest.configUnirest(false);

			String fileType="excel";
			String url="https://comptox-prod.epa.gov/dashboard/batch_search_download";
//			            https://comptox-prod.epa.gov/dashboard/dsstoxdb/batch_search

			String body="input_type=synonym";

			for (String sid:sidList) 
				body+="&inputs%5B%5D="+sid;

			body+="&synonym_types%5B%5D=dsstox";
			body+="&filetype="+fileType;

			String [] fields={"casrn","inchi","inchi_key","preferred_name","acd_iupac_name","smiles",
					"qsar_ready_smiles","mol_formula","dtxsid","average_mass"};

			for (String field:fields) {
				body+="&output_types%5B%5D="+field;	
			}

			//			System.out.println(body);

			HttpResponse<byte[]> response = Unirest.post(url)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
					.header("Cookie", "font_size=normal")
					.body(body)
					.asBytes();

			InputStream is = new ByteArrayInputStream(response.getBody());

			return getRecords(is);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return records;
	}
	
	public static void main(String[] args) {
		Vector<String>casList=new Vector<String>();
		casList.add("71-43-2");
		//		casList.add("123-45-6");
		//		casList.add("1392-21-8");
		//		getDashboardRecords(casList);
		RecordsDashboard recs=getDashboardRecordsExcelCAS(casList);

		for (RecordDashboard rec:recs) {
			System.out.println(rec.toJSON());
		}
	}
}
