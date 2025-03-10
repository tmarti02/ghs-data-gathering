package hazard;

import java.io.File;
import java.io.FileOutputStream;
import java.util.TreeMap;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
* @author TMARTI02
*/
public class ScoreRecord {
	
	public ScoreRecord() {}
	
	
	public ScoreRecord(String dtxsid, String cas, String smiles, String name, String similarity,String likelihood) {
		this.sid = dtxsid;
		this.cas = cas;
		this.smiles = smiles;
		this.name = name;
		this.similarity = similarity;
		this.likelihood=likelihood;

	}

	public static final String[] fields = { "smiles", "similarity","molWeight","molWeightDiff", "score", "name", "cas", "sid", "source",
			"sourceOriginal", "listType", "category", "hazardCode", "hazardStatement", "rationale", "route", "note",
			"valueMassOperator", "valueMass", "valueMassUnits", "duration", "durationUnits", "testOrganism", "testType",
			"url", "longRef", "toxvalID" };
	
	public static final String[] fieldsCTS = { "smiles", "likelihood","molWeight","score", "name", "cas", "sid", "source",
			"sourceOriginal", "listType", "category", "hazardCode", "hazardStatement", "rationale", "route", "note",
			"valueMassOperator", "valueMass", "valueMassUnits", "duration", "durationUnits", "testOrganism", "testType",
			"url", "longRef", "toxvalID" };
	
	public static String[] displayFieldNamesExcel = { "Hazard Name", "CAS", "Name", "Source", "Original Source", "List Type", "Score", "Rationale", "Route", "Category", "Hazard Code",
			"Hazard Statement", "Duration", "Duration Units", "Test organism", "Toxicity Type", "Toxicity Value", "Toxicity Value Units", "Reference", "Note" };

	public static String[] actualFieldNamesExcel = { "hazardName", "CAS", "name", "source", "sourceOriginal", "listType", "score", "rationale", "route", "category",
			"hazardCode", "hazardStatement", "duration", "durationUnits", "testOrganism", "testType", "valueMass", "valueMassUnits", "longRef", "note" };


	public String similarity;// TMM for creating analog reports
	public Double molWeight;
	public Double molWeightDiff;
	public String sid;// TMM for creating analog reports
	public String smiles;// TMM for creating analog reports
	public String cas;
	public String name;
	
	public String likelihood;// TMM for creating CTS metabolite reports
	
	
	public String source;
	public String sourceOriginal;
	public String listType;

	public String score;
	public String category;
	public String hazardCode;
	public String hazardStatement;
	public String rationale;

	public String route;
	public String note;

	public Double valueMass;
	public String valueMassUnits;
	public String valueMassOperator;

	public Double duration;
	public String durationUnits;

	public String testOrganism;
	public String testType;
	public String url;
	public String longRef;
	public String toxvalID;
	
	
	
	

}