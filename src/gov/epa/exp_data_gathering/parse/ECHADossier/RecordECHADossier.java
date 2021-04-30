package gov.epa.exp_data_gathering.parse.ECHADossier;

import java.util.List;
import java.util.Vector;

import gov.epa.exp_data_gathering.parse.DownloadWebpageUtilities;
import gov.epa.ghs_data_gathering.Utilities.MoreFileUtilities;

public class RecordECHADossier {
	public static final String sourceName = "ECHADossier";
	public static final String sourceFolder = "data/experimental/" + sourceName;

	public static void downloadWebpagesFromExcelToDatabase(String filePath,int col,boolean hasHeader,boolean startFresh) {
		List<String> urls = MoreFileUtilities.getListFromFile(filePath, 0, col, hasHeader);
		
		String databasePath = sourceFolder + "/" + sourceName + "_raw_html.db";
		DownloadWebpageUtilities.downloadWebpagesToDatabaseAdaptive(new Vector<String>(urls),"div#collapseReference1CollapseGroup0",databasePath,sourceName,startFresh);		
	}
	
	public static void main(String[] args) {
		String filePath =  sourceFolder + "/SkinSensitizationLLNA_records_DossierChecked.xlsx";
		downloadWebpagesFromExcelToDatabase(filePath, 7, true, true);
	}
}
