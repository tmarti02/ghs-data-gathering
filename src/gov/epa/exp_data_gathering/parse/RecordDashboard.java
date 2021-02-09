package gov.epa.exp_data_gathering.parse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;


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

	public static RecordDashboard getDashboardRecord(String cas) {


		try {
			UtilitiesUnirest.configUnirest(false);

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

			HttpResponse<byte[]> response = Unirest.post(url)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
					.header("Cookie", "font_size=normal")
					.body(body)
					.asBytes();

			InputStream is = new ByteArrayInputStream(response.getBody());

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


	public String toJSON() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();// makes it multiline and readable
		Gson gson = builder.create();
		return gson.toJson(this);//all in one line!
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