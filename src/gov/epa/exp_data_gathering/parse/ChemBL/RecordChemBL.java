package gov.epa.exp_data_gathering.parse.ChemBL;

import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import gov.epa.api.ExperimentalConstants;
import gov.epa.api.RawDataRecord;
import gov.epa.database.SQLite_CreateTable;
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
import gov.epa.exp_data_gathering.parse.ChemBL.JSONsForChemBL.ActivityData;
import gov.epa.ghs_data_gathering.Utilities.FileUtilities;

public class RecordChemBL {
	@SerializedName("activity_comment")
	public String activityComment;
	@SerializedName("activity_id")
	public String activityId;
	@SerializedName("activity_properties")
	public List<String> activityProperties;
	@SerializedName("assay_chembl_id")
	public String assayChemblId;
	@SerializedName("assay_description")
	public String assayDescription;
	@SerializedName("assay_type")
	public String assayType;
	@SerializedName("bao_endpoint")
	public String baoEndpoint;
	@SerializedName("bao_format")
	public String baoFormat;
	@SerializedName("bao_label")
	public String baoLabel;
	@SerializedName("canonical_smiles")
	public String canonicalSmiles;
	@SerializedName("data_validity_comment")
	public String dataValidityComment;
	@SerializedName("data_validity_description")
	public String dataValidityDescription;
	@SerializedName("document_chembl_id")
	public String documentChemblId;
	@SerializedName("document_journal")
	public String documentJournal;
	@SerializedName("document_year")
	public String documentYear;
	@SerializedName("ligand_efficiency")
	public Object ligandEfficiency;
	@SerializedName("molecule_chembl_id")
	public String moleculeChemblId;
	@SerializedName("molecule_pref_name")
	public String moleculePrefName;
	@SerializedName("parent_molecule_chembl_id")
	public String parentMoleculeChemblId;
	@SerializedName("pchembl_value")
	public String pchemblValue;
	@SerializedName("potential_duplicate")
	public Boolean potentialDuplicate;
	@SerializedName("qudt_units")
	public String qudtUnits;
	@SerializedName("record_id")
	public String recordId;
	@SerializedName("relation")
	public String relation;
	@SerializedName("src_id")
	public String srcId;
	@SerializedName("standard_flag")
	public Boolean standardFlag;
	@SerializedName("standard_relation")
	public String standardRelation;
	@SerializedName("standard_text_value")
	public String standardTextValue;
	@SerializedName("standard_type")
	public String standardType;
	@SerializedName("standard_units")
	public String standardUnits;
	@SerializedName("standard_upper_value")
	public String standardUpperValue;
	@SerializedName("standard_value")
	public String standardValue;
	@SerializedName("target_chembl_id")
	public String targetChemblId;
	@SerializedName("target_organism")
	public String targetOrganism;
	@SerializedName("target_pref_name")
	public String targetPrefName;
	@SerializedName("target_tax_id")
	public String targetTaxId;
	@SerializedName("text_value")
	public String textValue;
	@SerializedName("toid")
	public String toid;
	@SerializedName("type")
	public String type;
	@SerializedName("units")
	public String units;
	@SerializedName("uo_units")
	public String uoUnits;
	@SerializedName("upper_value")
	public String upperValue;
	@SerializedName("value")
	public String value;
	
	public String date_accessed;
	public String url;
	public static final String sourceName = ExperimentalConstants.strSourceChemBL;

	private static void downloadAllJSONsToDatabase(boolean startFresh) {
		String[] endpoints = {"pka","pKa","pKA","Tm","LogP","Solubility","solubility"};
		downloadEndpointJSONsToDatabase(startFresh,endpoints[0]);
		for (int i = 1; i < endpoints.length; i++) {
			downloadEndpointJSONsToDatabase(false,endpoints[i]);
		}
	}
	
	private static void downloadEndpointJSONsToDatabase(boolean startFresh, String standardType) {
		String firstURL = "https://www.ebi.ac.uk/chembl/api/data/activity?standard_type="+standardType+"&format=json";
		String databasePath = "Data"+File.separator+"Experimental"+File.separator+sourceName+File.separator+sourceName+"_raw_json.db";
		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
		java.sql.Connection conn=SQLite_CreateTable.create_table(databasePath, sourceName, RawDataRecord.fieldNames, startFresh);
		
		try {
			int counter = 0;
			System.out.println("Querying "+standardType);
			String firstJSON = FileUtilities.getText_UTF8(firstURL).replaceAll("'", "\'");
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
			Date date = new Date();  
			String strDate=formatter.format(date);
			RawDataRecord firstRec=new RawDataRecord(strDate, firstURL, firstJSON);
			boolean haveRecord=firstRec.haveRecordInDatabase(databasePath,sourceName,conn);
			if (firstRec.content!=null && (!haveRecord || startFresh)) {
				try {
					firstRec.addRecordToDatabase(sourceName, conn);
					counter++;
				} catch (Exception ex) {
					System.out.println("Failed to download "+firstURL);
				}
			}
			ActivityData data = gson.fromJson(firstJSON, ActivityData.class);
			String url = "https://www.ebi.ac.uk";
			String urlTail = "";
			while ((urlTail = data.pageMeta.next)!=null) {
				date = new Date();  
				strDate=formatter.format(date);
				RawDataRecord rec=new RawDataRecord(strDate, url+urlTail, "");
				haveRecord=rec.haveRecordInDatabase(databasePath,sourceName,conn);
				if (!haveRecord || startFresh) {
					try {
						rec.content = FileUtilities.getText_UTF8(url+urlTail).replaceAll("'", "\'");
						if (rec.content!=null) {
							rec.addRecordToDatabase(sourceName, conn);
							counter++;
						}
					} catch (Exception ex) {
						System.out.println("Failed to download "+url+urlTail);
					}
				}
				data = gson.fromJson(rec.content, ActivityData.class);
				if (counter % 100 == 0) { System.out.println("Downloaded "+counter+" pages"); }
			}
			System.out.println("Downloaded "+counter+" pages for "+standardType);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Vector<RecordChemBL> parseJSONsInDatabase() {
		String databaseFolder = "Data"+File.separator+"Experimental"+ File.separator + sourceName;
		String databasePath = databaseFolder+File.separator+sourceName+"_raw_json.db";
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
		Vector<RecordChemBL> records = new Vector<RecordChemBL>();
		
		try {
			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = SQLite_GetRecords.getAllRecords(stat,sourceName);

			int counter = 0;
			while (rs.next()) {
				String json = rs.getString("content");
				String url = rs.getString("url");
				String date = rs.getString("date");
				ActivityData data = gson.fromJson(json,ActivityData.class);
				for (RecordChemBL cbr:data.activities) {
					cbr.url = url;
					cbr.date_accessed = date.substring(0,date.indexOf(" "));
					records.add(cbr);
					counter++;
				}
				if (counter % 100==0) { System.out.println("Parsed "+counter+" pages"); }
			}
			
			System.out.println("Parsed "+counter+" pages");
			return records;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		downloadAllJSONsToDatabase(true);
	}
}
