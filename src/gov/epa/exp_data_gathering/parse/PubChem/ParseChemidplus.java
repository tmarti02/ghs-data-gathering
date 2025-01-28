package gov.epa.exp_data_gathering.parse.PubChem;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import gov.epa.api.ExperimentalConstants;
import gov.epa.database.SqlUtilities;
import gov.epa.exp_data_gathering.parse.ExperimentalRecords;
import gov.epa.exp_data_gathering.parse.Parse;
import gov.epa.exp_data_gathering.parse.PubChem.ParseDatabaseAnnotation.DB_Identifier;

/**
* @author TMARTI02
*/
public class ParseChemidplus extends Parse {
	
	List<String>selectedPropertyNames=null;
	
	public ParseChemidplus() {
		sourceName = ExperimentalConstants.strSourceChemidplus2024_12_04;
		this.init();
	}

	
	/**
	 * Parses JSON entries in database to RecordPubChem objects, then saves them to a JSON file
	 */
	@Override
	protected void createRecords() {
		
		if(generateOriginalJSONRecords) {
			
			String databaseFolder = "Data" + File.separator + "Experimental" + File.separator + RecordPubChem.sourceName;
			String databasePath = databaseFolder + File.separator + RecordPubChem.sourceName + "_raw_json_v2.db";
			Hashtable<Long,DB_Identifier>htIdentifiersByCID=DB_Identifier.getIdentifierHashtable(SqlUtilities.getConnectionSqlite(databasePath));
//			System.out.println(gson.toJson(htIdentifiersByCID));


			Vector<RecordPubChem> records = RecordChemidplus.parseJsonFile(htIdentifiersByCID);
			System.out.println("Added "+records.size()+" records");
			writeOriginalRecordsToFile(records);
			
		}
	}

	@Override
	protected ExperimentalRecords goThroughOriginalRecords() {
		
		File folder=new File(jsonFolder);
		List<RecordPubChem> recordsPubChem = new ArrayList<RecordPubChem>();
		
		
		for (File file:folder.listFiles()) {
			
			if(!file.getName().contains(".json")) continue;
			if(file.getName().contains("Copy")) continue;
			if(!file.getName().contains("Original Records")) continue;

			RecordPubChem[] tempRecords;
			try {
				tempRecords = gson.fromJson(new FileReader(file), RecordPubChem[].class);
				for(RecordPubChem record:tempRecords) recordsPubChem.add(record);
				System.out.println(file.getName()+"\t"+tempRecords.length);
			
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		
		ExperimentalRecords experimentalRecords=new ExperimentalRecords();
		
		Iterator<RecordPubChem> it = recordsPubChem.iterator();
		while (it.hasNext()) {
			RecordPubChem r = it.next();
			if(selectedPropertyNames!=null && !selectedPropertyNames.contains(r.propertyName)) continue; 
			experimentalRecords.add(r.toExperimentalRecord(r.propertyValue));			
		}
		return experimentalRecords;

	}
	
	
	public static void main(String[] args) {
		ParseChemidplus p = new ParseChemidplus();
		
		p.generateOriginalJSONRecords=true;
		p.removeDuplicates=true;

		p.writeJsonExperimentalRecordsFile=true;
		p.writeExcelExperimentalRecordsFile=true;
		p.writeExcelFileByProperty=true;		
		p.writeCheckingExcelFile=false;//creates random sample spreadsheet
		
		p.selectedPropertyNames=Arrays.asList(
				ExperimentalConstants.strINTRADERMAL_RABBIT_LD50,ExperimentalConstants.strDERMAL_RAT_LD50, 
				ExperimentalConstants.strInhalationMouseLC50,ExperimentalConstants.strInhalationRatLC50, 
				ExperimentalConstants.strORAL_MOUSE_LD50, ExperimentalConstants.strORAL_RAT_LD50);
		
		
//		p.selectedPropertyNames=Arrays.asList(ExperimentalConstants.strORAL_RAT_LD50);
		
		p.createFiles();

	}
}
