package gov.epa.exp_data_gathering.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.apache.tika.parser.txt.CharsetDetector;//doesnt want to import even with entry in pom.xml


/**
 * @author TMARTI02
 */
public class TextSourceReader {

	String sourceName;
	String fileName;
	String lastUpdated;



	public static void tsvToExcel(String filename, String srcFolderPath,String destFolderPath) {        

		String tsvFilePath=srcFolderPath+File.separator+filename;
		String excelFilePath = destFolderPath+File.separator+filename.replace(".tsv", ".xlsx");

		if (new File(excelFilePath).exists()) return;
//		if(!filename.equals("echa reach acute toxicity by test material.tsv"))return;
		
//		System.out.println(filename);
		
		try {
						
//			CharsetDetector detector = new CharsetDetector();
//	        detector.setText(new FileInputStream(tsvFilePath));
//	        String charset = detector.detect().getName();
//	        System.out.println(charset);
	        
			Workbook workbook = new XSSFWorkbook();
			FileOutputStream excelOutputStream = new FileOutputStream(excelFilePath);

			Sheet sheet = workbook.createSheet("TSV Data");

//			Scanner scanner = new Scanner(tsvInputStream, "UTF-8");
//			Scanner scanner = new Scanner(tsvInputStream);
			
			FileInputStream is = new FileInputStream(tsvFilePath);
			
//			Charset charset=java.nio.charset.StandardCharsets.ISO_8859_1;
			Charset charset=java.nio.charset.StandardCharsets.UTF_16;
			
			InputStreamReader isr = new InputStreamReader(is, charset);
			BufferedReader br = new BufferedReader(isr);
						
//			BufferedReader br=new BufferedReader(new FileReader(tsvFilePath,Charset.));
			
			int rowNum = 0;
			
			while (true) {
				String line = br.readLine();
				
//				if(line.length()>5000)				
//					System.out.println((rowNum+1)+"\t"+line.length());
				
//				System.out.println(line);
				
				if(line==null) break;
				if(line.length()==0) break;
				
				String[] values = line.split("\t");

				Row row = sheet.createRow(rowNum++);
				int colNum = 0;
				for (String value : values) {
					Cell cell = row.createCell(colNum++);
					
					if (value.length()>32765)
						value=value.substring(0,32765);
					
					cell.setCellValue(value);
				}
				
				if(rowNum%1000==0)
					System.out.println(filename+"\t"+rowNum);

			}
			
			System.out.println(filename+"\t"+rowNum);
			

			workbook.write(excelOutputStream);
			workbook.close();
			excelOutputStream.flush();
			excelOutputStream.close();
			//            System.out.println("TSV file converted to Excel successfully!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes a new reader for the given source from the given filename NOTE:
	 * Currently can only read a single sheet from a single file
	 * 
	 * @param fileName   The file to read records from
	 * @param sourceName The data source to assign records to
	 */
	public TextSourceReader(String fileName, String mainFolderPath, String sourceName,String delimiter) {
		this.sourceName = sourceName;
		this.fileName = fileName;

		String sourceFolderPath = mainFolderPath + File.separator + sourceName;

		String filePath = sourceFolderPath + File.separator + "excel files" + File.separator + fileName;

		System.out.println(filePath);

		this.lastUpdated = DownloadWebpageUtilities.getStringCreationDate(filePath); // TODO add lastUpdated as
		// parameter instead?
		try {
			FileInputStream fis = new FileInputStream(new File(filePath));
			//TODO
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void convertTsvsInFolderToExcel (String srcFolderPath,String destFolderPath) {
		File folder=new File(srcFolderPath);
		for (File file:folder.listFiles()) {
			if (!file.getName().contains(".tsv")) continue;
			tsvToExcel(file.getName(),srcFolderPath,destFolderPath);
		}
	}


	public static void main(String[] args) {
		TextSourceReader tsr = new TextSourceReader("skin sensitization.tsv","data\\experimental\\QSAR Toolbox\\" ,"skin sensitization db","\t");
		//		tsr.createClassTemplateFiles();
		
	}
}
