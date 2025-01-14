package gov.epa.exp_data_gathering.parse.NIEHS_ICE_2024_08;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

/**
* @author TMARTI02
*/
public class NIEHS_ICE_TemplateCreator {

	public static void main(String[] args) {

		int headerRowNum=2;
		
		String sourceName="NIEHS_ICE_2024_08";
		String folder="data/experimental/"+sourceName+"/excel files";
		File Folder=new File(folder);
		
		List<String>sheetNames=Arrays.asList("Data", "Data_invivo","Data_invitro");

		ExcelSourceReader esr=new ExcelSourceReader();
		esr.sourceName=RecordNIEHS_ICE_2024_08.sourceName;

		List<String>headers=esr.getAllHeadersFromExcelFilesInFolder(sheetNames,headerRowNum, Folder);
		esr.createClassTemplateFiles(headers);

	}

	

}
