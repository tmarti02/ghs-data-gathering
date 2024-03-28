package gov.epa.exp_data_gathering.parse.Sander;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.ExperimentalConstants;
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
import gov.epa.exp_data_gathering.parse.DownloadWebpageUtilities;
import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.exp_data_gathering.parse.LiteratureSource;


public class RecordSander {

	String chemicalName;
	String inchiKey;
	String CASRN;
	String d_ln_Hcp_over_d;
	String hcp;
	String type;
	
	String referenceAbbreviated;
	String referenceFull;
//	Vector<String> referenceFull; //unsorted, don't align with referencesAbbreviated, regular expression code needs to be refined to do so
	
	String url;
//	int recordCount;
	String fileName;
	String date_accessed;
	String notes;


	static final String sourceName=ExperimentalConstants.strSourceSander+"_v5";	

	static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues()
			.create();

	public static void main(String[] args) {
		downloadWebpagesHTML();
	}
	// as the name implies, only related to the webpage download process

	public static void downloadWebpagesHTML() {
		Vector<String> urls = ObtainWebpages();

		for (String url:urls) {
			System.out.println(url);
		}
		
		System.out.println(urls.size());
		if(true) return;
		
		//	Vector<String> html = parsePropertyLinksInDatabase();
		
		boolean startFresh=true;

		ParseSander p = new ParseSander();
		String databasePath = p.databaseFolder + File.separator + sourceName + "_raw_html.db";
		DownloadWebpageUtilities.downloadWebpagesToDatabaseAdaptive(urls,"tbody", databasePath,sourceName,startFresh);

	}

	

	// regex, to be used later depending on whether Todd wants full reference list
	public static void Gabrieldemo() {
		String Referenceshort = "Mackay and Shiu 1981";
		String Referencelong = "Mackay, D. and Shiu, W. Y.: A critical review of Henry's law constants for chemicals of environmental interest, J. Phys. Chem. Ref. Data, 10, 1175-1199, doi:10.1063/1.555654, 1981.";
		Pattern p = Pattern.compile("(([^ ]+) .*?)([^\\s]+$)");
		Matcher m = p.matcher(Referenceshort);
		if (m.find()) {
			System.out.println(m.group(2) + " " + m.group(3));
		}
	}
	// since the HTML for sander code is a lot more split up than chemicalbook, I am calling 3 separate functions for the 3 parts of the page

	//assigns a chemical name, cas number, and inchi key to the recordSander object
	private static void getIdentifiers(Document doc, RecordSander rs) {
		Element chemicalName = doc.select("td[width=60%] > h1").first();
		rs.chemicalName = chemicalName.ownText().trim().replace("→", "").trim();

		// I want this to not start with ???
		
		Element inchikey = doc.select("td[width=60%] > table > tbody > tr > td:contains(InChIKey:) ~ td").first();
		if(inchikey!=null && inchikey.text()!=null) rs.inchiKey = inchikey.text();

		Element casrn = doc.selectFirst("td[width=60%] > table > tbody > tr > td:contains(CAS RN:) ~ td");
		if(casrn!=null && casrn.text()!=null)	rs.CASRN = casrn.text().trim();
		 
	}
	// scrapes the 'Sander - full' page to obtain the links for all chemicals on the site
	private static Vector<String> ObtainWebpages() {

		//TODO change to https://www.henrys-law.org/henry/

		String baseSearchLink = "https://www.henrys-law.org/henry/search_identifier.html?csrfmiddlewaretoken=ZaAV0nh7GWRmm5Z5UDshScMxM4OujgEpC2Ywh1iSPfqDh6CCafT9iHkx0lrIIfgc&x=0&y=0&search=";
		Vector<String> allLinks = new Vector<String>();
		try {
			Document doc = Jsoup.connect(baseSearchLink).get();
			//		Elements rows = doc.select("td[width=60%] > table > tbody > tr");
			//		for (int i = 0; i < rows.size(); i++) {
			//			allLinks.add(rows.get(i).select("td ~ td > a").attr("abs:href").toString());
			//		}


			Elements rows = doc.select("a");
			for (int i = 0; i < rows.size(); i++) {
				Element row=rows.get(i);
				String url = row.attr("href");
				url="https://www.henrys-law.org/henry"+url;
				if(!url.contains("/henry/casrn") && !url.contains("/henry/inchikey")) continue;

				if(!allLinks.contains(url)) {
					allLinks.add(url);
//					System.out.println(url);
				}

			}
			return allLinks;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// returns all html from the websites downloaded to the database
	public static Vector<String> parsePropertyLinksInDatabase() {
		String databaseFolder = "Data"+File.separator+"Experimental"+ File.separator + sourceName;
		String databasePath = databaseFolder+File.separator+ExperimentalConstants.strSourceSander + "_raw_html.db";
		Vector<String> records = new Vector<>();
		System.out.println(databasePath);

		try {
			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = SQLite_GetRecords.getAllRecords(stat, ExperimentalConstants.strSourceSander);

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


	// gets the full references for the Sander site
	private static Vector<String>  getReferencesList(Document doc) {
		Vector<String> referencesFull = new Vector <String>();
		Elements references = doc.select("ul > li");
		for (Element reference:references) {
			referencesFull.add(reference.text());
		}
		return referencesFull;
//		rs.referenceFull = referencesFull;
	}
	
	
	// gets the full references for the Sander site
		private static Hashtable<String,String>  getNotesHashtable(Document doc) {
			Hashtable<String,String>htNotes = new Hashtable<>();
			
			Elements rows =doc.select("h2 + table").first().select("tr");
			
			for (Element row:rows) {
				Elements columns=row.select("td");
				String key=columns.get(0).text();
				
				if(columns.size()==1) continue;
				
				String value=columns.get(1).text();
				htNotes.put(key, value);
//				System.out.println(key+"\t"+value);
			}
			return htNotes;
//			rs.referenceFull = referencesFull;
		}

	// gets all the experimental information from the parsed Sander html. hcp, references abbreviated, etc.
	private static void parseDocument(Document doc, Vector<RecordSander> records, String url, String date) {

		Element table = doc.select("td[width=60%] > table ~ table[width=100%] > tbody").first();
		Elements tableRecords = table.select("tr:gt(1)");
		int recordno = 0;

		Hashtable<String,String>htNotes=getNotesHashtable(doc);
		Vector<String>fullRefs=	getReferencesList(doc);
		
		for (Element tableRecord:tableRecords) {
			Elements tableFields = tableRecord.select("td");
			
			RecordSander rs=new RecordSander();
			rs.fileName=url.substring(url.lastIndexOf("/")+1, url.length());
			rs.url = url;
			rs.date_accessed = date.substring(0,date.indexOf(" "));
			
			rs.hcp=tableFields.get(0).text(); // 0 is the first column, the Hcp
			rs.d_ln_Hcp_over_d=(tableFields.get(1).text()); // 1 is the second column, the d ln Hcp / d (1/T) [K]
			rs.referenceAbbreviated=(tableFields.get(2).text()); // 2 is the third column, the reference
			rs.type=(tableFields.get(3).text()); // 3 is the fourth column, the type

			getIdentifiers(doc, rs);

			getNote(tableFields, rs, htNotes);
			getFullReference(url, rs, fullRefs);

			recordno++;

			records.add(rs);
			
		}

	}

	private static void getNote(Elements tableFields, RecordSander rs, Hashtable<String, String> htNotes) {
		String strNoteKey=tableFields.get(4).text().trim();
		
		String []noteKeys=strNoteKey.split(" ");
		
		boolean missing=false;
		
		for (int i=0;i<noteKeys.length;i++) {

			String key=noteKeys[i];
			
			if(key.isBlank()) {
//					System.out.println(i+"\t"+strNoteKey+"\thas blank");
				continue;
			}
			
//				System.out.println(i+"\t"+key);
			
			String note=htNotes.get(key);

			if(note==null) {
				System.out.println("Missing note for key="+key);
				missing=true;
				continue;
			}
			
			if(rs.notes==null) rs.notes=note;
			else rs.notes+="; "+note;
		}
		
//		if(rs.notes.contains("null")) {
//			System.out.println(rs.notes);
//		}
		
		
		if(missing) {
			System.out.println(rs.url+"\t"+gson.toJson(htNotes));
		}
	}

	/**
	 * This is a hack to figure out which full reference to use
	 * Later we should use their postgres db
	 * 
	 * @param url
	 * @param rs
	 * @param fullRefs
	 */
	private static void getFullReference(String url, RecordSander rs, Vector<String> fullRefs) {
		String ra=rs.referenceAbbreviated;
		
		if (ra.isBlank() || !ra.contains("(")) return;
		
		String year=ra.substring(ra.indexOf("(")+1,ra.indexOf(")"));					
		String author=ra.substring(0,ra.indexOf("(")).replace("et al.", "").trim();
		List<String> authors = new ArrayList<String>(Arrays.asList(author.split(" and ")));
		
		for (String citation:fullRefs) {

			if(citation.contains(year)) {
				
				if(authors.size()==1) {
					if(citation.indexOf(author)==0) {
						rs.referenceFull=citation;							
						break;
					}
				} else if (authors.size()==2) {
					String author1=authors.get(0);
					String author2=authors.get(1);						

					if(citation.indexOf(author1)==0 && citation.contains(author2)) {
						rs.referenceFull=citation;							
						break;
					} else {
//							System.out.println("****"+"\t"+author1+"\t"+author2+"\t"+year);
					}
				} else {
					System.out.println("\n"+url+"\t"+authors.size()+"\t"+author);
				}
				//System.out.println("Match\t"+ls.name+"\t"+citation);
			} 
		}

		if(rs.referenceFull==null) {
			System.out.println(url+"\tNo match\t"+ra+"\t"+author+"\t"+year+"\t"+authors.size());

			for (String citation:fullRefs) {
				System.out.println("\t"+citation);	
			}
		}
	}

	/**
	 * Parses the HTML strings in the raw HTML database to RecordSander objects
	 * @return	A vector of RecordSander objects containing the data from the raw HTML database
	 */
	public static Vector<RecordSander> parseWebpagesInDatabase() {
		String databaseFolder = "Data"+File.separator+"Experimental"+ File.separator + sourceName;
		String databasePath = databaseFolder+File.separator+sourceName+"_raw_html.db";
		
		System.out.println(databasePath);
		
		Vector<RecordSander> records = new Vector<>();

		try {
			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = SQLite_GetRecords.getAllRecords(stat, sourceName);

			int counter = 1;

			while (rs.next()) {
				if (counter % 100==0) { System.out.println("Parsed "+counter+" pages"); }

				String html = rs.getString("content");
				html = html.replaceAll("\\u2212", "-"); // there are some minus signs rather than "-" <- wanted, "eN" dash, and "eM" dashes
				String url = rs.getString("url");
				String date = rs.getString("date");
				Document doc = Jsoup.parse(html);

				parseDocument(doc,records,url,date);
				counter++;

//				if(counter>3) break;
				
			}


			return records;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
