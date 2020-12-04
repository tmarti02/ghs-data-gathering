package gov.epa.exp_data_gathering.parse;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.sql.RowSetListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import gov.epa.api.ExperimentalConstants;
import gov.epa.ghs_data_gathering.Database.MySQL_DB;
import gov.epa.ghs_data_gathering.GetData.RecordDashboard;
import gov.epa.ghs_data_gathering.Utilities.FileUtilities;



// Failed to Download - downloaded 4297 pages

public class RecordSander {
	
	String referenceAbbreviated;
	String chemicalName;
	String inchiKey;
	String data;
	String CASRN;
	Vector<String> d_ln_Hcp_over_d;
	Vector<String> hcp;
	Vector<String> type;
	String referenceFull;
	
	static final String sourceName="Sander";	


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		downloadWebpagesHTML();
	}
	
	
public static void downloadWebpagesHTML() {
	// Vector<String> urls = ObtainWebpages();
	ParseSander p = new ParseSander();
	p.mainFolder = p.mainFolder + File.separator + "General";
	p.databaseFolder = p.mainFolder;
	// p.downloadWebpagesToDatabaseAdaptive(urls,"tbody", sourceName,true);
	testingfunction();
}

private static void testingfunction() {
	Vector<String> html = parsePropertyLinksInDatabase();
	String example = html.get(3949);
	Document doc = Jsoup.parse(example);
	getIdentifiers(doc);
	Vector <Vector<String>> ExperimentalTable = getExperimentalTable(doc);
	Vector <String> references = getReferences(doc);
	RecordSander rs = new RecordSander();
	matchReferences(ExperimentalTable, references);
}

private static void matchReferences(Vector <Vector<String>> table, Vector <String> ref) {
	int referencesIndex = 3;
	for (int i = 0; i < table.size(); i++) {
		System.out.println(table.get(referencesIndex));
	}
}

private static void getIdentifiers(Document doc) {
	Element name = doc.select("td[width=60%] > h1").first();
	System.out.println(name.ownText());
	// I want this to not start with ???
	Element inchikey = doc.select("td[width=60%] > table > tbody > tr > td:contains(InChIKey:) ~ td").first();
	String inchi = inchikey.text();
	System.out.println(inchi);
}

private static Vector<String> ObtainWebpages() {
	String baseSearchLink = "http://satellite.mpic.de/henry/search_identifier.html?csrfmiddlewaretoken=eNhrzlz52Jf3pHHxhPPvsFfi0jCmhStqaIIF7xXrsctaPEuHMIgkdjAkRyUDvPQm&x=0&y=0&search=";
	Vector<String> allLinks = new Vector<String>();
	try {
		Document doc = Jsoup.connect(baseSearchLink).get();
		Elements rows = doc.select("td[width=60%] > table > tbody > tr");
		for (int i = 0; i < rows.size(); i++) {
			allLinks.add(rows.get(i).select("td ~ td > a").attr("abs:href").toString());
		}
	return allLinks;
	}
	catch (IOException e) {
		e.printStackTrace();
	}
	return null;
}

public static Vector<String> parsePropertyLinksInDatabase() {
	String databaseFolder = "Data"+File.separator+"Experimental"+ File.separator + sourceName + File.separator + "General";
	String databasePath = databaseFolder+File.separator+ExperimentalConstants.strSourceSander + "_raw_html.db";
	Vector<String> records = new Vector<>();
	System.out.println(databasePath);

	try {
		Statement stat = MySQL_DB.getStatement(databasePath);
		ResultSet rs = MySQL_DB.getAllRecords(stat, ExperimentalConstants.strSourceSander);
		
		int counter = 1;
		
		
		while (rs.next()) {
			if (counter % 100==0) { System.out.println("Parsed "+counter+" pages"); }
			
			String html = rs.getString("content");
			records.add(html);
		}
		

		return records;
	} catch (Exception e) {
		e.printStackTrace();
	}	
	return null;
}


private static Vector <String> getReferences(Document doc) {
	Vector<String> referencesFull = new Vector <String>();
	Elements references = doc.select("ul > li");
	for (Element reference:references) {
		referencesFull.add(reference.text());
	}
	return referencesFull;
}


private static Vector <Vector<String>> getExperimentalTable(Document doc, RecordSander rs) {
		Vector <Vector<String>> experimentInfo = new Vector <Vector<String>>();
		Vector <String> experimentalFields = new Vector<String>();
		Element table = doc.select("td[width=60%] > table ~ table[width=100%] > tbody").first();
		Elements tableRecords = table.select("tr:gt(1)");
		for (int i = 0; i < tableRecords.size(); i++) {
			Elements tableFields = tableRecords.select("td");
			for (int k = 0; k < tableFields.size(); k++) {
				experimentalFields.add(i,tableFields.get(k).text());
			}
			experimentInfo.add(i,experimentalFields);
			
		}
		/*
		 * annoying print statement that I don't want to erase yet
		for (int j = 0; j < experimentInfo.size(); j++) {
			for (int p = 0; p < experimentalFields.size(); p++) {
                System.out.println(experimentInfo.get(j).get(p));

			}
		}
		*/
	return experimentInfo;
}

}
