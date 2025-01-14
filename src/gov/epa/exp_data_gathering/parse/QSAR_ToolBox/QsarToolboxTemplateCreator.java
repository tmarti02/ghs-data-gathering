package gov.epa.exp_data_gathering.parse.QSAR_ToolBox;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import gov.epa.exp_data_gathering.parse.ExcelSourceReader;

/**
* @author TMARTI02
*/
public class QsarToolboxTemplateCreator {

	
	public static void main(String[] args) {

		int headerRowNum=2;
		String sourceName="QSAR_ToolBox";
		String folder="data/experimental/"+sourceName+"/excel files";
		File Folder=new File(folder);

		List<String>sheetNames=Arrays.asList("Sheet1");
		ExcelSourceReader esr=new ExcelSourceReader();
		
		esr.sourceName=sourceName;
		
		List<String>headers=esr.getAllHeadersFromExcelFilesInFolder(sheetNames,headerRowNum, Folder);
		esr.createClassTemplateFiles(headers);

	}
	
}
