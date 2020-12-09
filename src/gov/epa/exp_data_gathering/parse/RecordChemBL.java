package gov.epa.exp_data_gathering.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;

public class RecordChemBL {
	String moleculeChemBLID;
	String moleculeName;
	String smiles;
	String standardType;
	String standardRelation;
	String standardValue;
	String standardUnits;
	String dataValidityComment;
	String comment;
	String potentialDuplicate;
	String assayChemBLID;
	String assayDescription;
	String documentChemBLID;
	String sourceID;
	String sourceDescription;
	String documentJournal;
	String documentYear;
	
	public static final String sourceName = ExperimentalConstants.strSourceChemBL;
	
	public static Vector<RecordChemBL> parseChemBLQueriesFromCSV() {
		Vector<RecordChemBL> records = new Vector<RecordChemBL>();
		String folderNameExcel = "excel files";
		String mainFolder = "Data"+File.separator+"Experimental"+ File.separator + sourceName;
		String excelFilePath = mainFolder + File.separator+folderNameExcel;
		File folder = new File(excelFilePath);
		String[] filenames = folder.list();
		for (String filename:filenames) {
			if (filename.endsWith(".csv")) {
				try {
					BufferedReader br = new BufferedReader(new FileReader(excelFilePath+File.separator+filename));
					String str = "";
					br.readLine(); // discard header row
					while ((str = br.readLine())!=null) {
						String[] fields = str.split(";");
						RecordChemBL cbr = new RecordChemBL();
						cbr.moleculeChemBLID = fields[0].replaceAll("\"","");
						cbr.moleculeName = fields[1].replaceAll("\"","");
						cbr.smiles = fields[7].replaceAll("\"","");
						cbr.standardType = fields[8].replaceAll("\"","");
						cbr.standardRelation = fields[9].replaceAll("\"","").replaceAll("'","");
						cbr.standardValue = fields[10].replaceAll("\"","");
						cbr.standardUnits = fields[11].replaceAll("\"","");
						cbr.dataValidityComment = fields[13].replaceAll("\"","");
						cbr.comment = fields[14].replaceAll("\"","");
						cbr.potentialDuplicate = fields[20].replaceAll("\"","");
						cbr.assayChemBLID = fields[21].replaceAll("\"","");
						cbr.assayDescription = fields[22].replaceAll("\"","");
						cbr.documentChemBLID = fields[35].replaceAll("\"","");
						cbr.sourceID = fields[36].replaceAll("\"","");
						cbr.sourceDescription = fields[37].replaceAll("\"","");
						cbr.documentJournal = fields[38].replaceAll("\"","");
						cbr.documentYear = fields[39].replaceAll("\"","");
						records.add(cbr);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return records;
	}
}
