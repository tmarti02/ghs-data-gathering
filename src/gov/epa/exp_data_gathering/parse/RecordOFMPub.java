package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import gov.epa.api.ExperimentalConstants;
import gov.epa.ghs_data_gathering.Database.MySQL_DB;

public class RecordOFMPub {
	String endpoint;
	String categoryChemicalCAS;
	String categoryChemicalName;
	String testSubstanceCAS;
	String testSubstanceName;
	String testSubstanceComments;
	String categoryChemicalResultType;
	String testSubstanceResultType;
	String indicator;
	String value;
	// pH and pKa fields are present in the table but only 1/7077 records has a value for them
	// and it isn't clear what pH and pKa measurements exactly they refer to
//	String pH;
//	String pKa;
	String resultRemarks;
	String reference;
	String reliability;
	String reliabilityRemarks;
	String url;
	String date_accessed;
	
	static final String sourceName=ExperimentalConstants.strSourceOFMPub;
	
	private static Vector<String> getSubmissionIDs() {
		String indexURL = "https://ofmpub.epa.gov/oppthpv/hpv_hc_characterization.get_report_by_cas?doctype=2";
		Vector<String> ids = new Vector<String>();
		try {
			Document doc = Jsoup.connect(indexURL).get();
			Element index = doc.selectFirst("table.blueTable");
			Elements rows = index.getElementsByTag("tr");
			for (Element row:rows) {
				Elements cells = row.getElementsByTag("td");
				Element lastCell = cells.last();
				String lastCellContent = lastCell.html();
				Matcher matchSubmissionID = Pattern.compile("submission_id=([0-9]+)").matcher(lastCellContent);
				if (matchSubmissionID.find()) {
					String newID = matchSubmissionID.group(1);
					if (!ids.contains(newID)) { ids.add(newID); }
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ids;
	}
	
	private static String[] constructURLs(String id) {
		String urlHead = "https://ofmpub.epa.gov/oppthpv/Public_Search.PublicTabs?section=1&SubmissionId=";
		String meltingPointURL = urlHead+id+"&epcount=1&epname=Melting+Point&epdiscp=Physical-Chemical+SIDS&selchemid=null&CategorySingle=null";
		String boilingPointURL = urlHead+id+"&epcount=1&epname=Boiling+Point&epdiscp=Physical-Chemical+SIDS&selchemid=null&CategorySingle=null";
		String vaporPressureURL = urlHead+id+"&epcount=1&epname=Vapor+Pressure&epdiscp=Physical-Chemical+SIDS&selchemid=null&CategorySingle=null";
		String partitionCoefficientURL = urlHead+id+"&epcount=1&epname=Partition+Coefficient&epdiscp=Physical-Chemical+SIDS&selchemid=null&CategorySingle=null";
		String waterSolubilityURL = urlHead+id+"&epcount=1&epname=Water+Solubility&epdiscp=Physical-Chemical+SIDS&selchemid=null&CategorySingle=null";
		String[] urls = {meltingPointURL,boilingPointURL,vaporPressureURL,partitionCoefficientURL,waterSolubilityURL};
		return urls;
	}
	
	public static void downloadWebpagesToDatabaseFromIndex(boolean startFresh) {
		Vector<String> ids = getSubmissionIDs();
		Vector<String> urls = new Vector<String>();
		for (String id:ids) {
			String[] idURLs = constructURLs(id);
			for (int i = 0; i < idURLs.length; i++) {
				urls.add(idURLs[i]);
			}
		}
		
		ParseOFMPub p = new ParseOFMPub();
		p.downloadWebpagesToDatabaseAdaptiveNonUnicode(urls,sourceName,startFresh);
	}
	
	public static void downloadWebpagesToDatabaseFromIndex(boolean startFresh,int start,int end) {
		Vector<String> ids = getSubmissionIDs();
		Vector<String> urls = new Vector<String>();
		for (String id:ids) {
			String[] idURLs = constructURLs(id);
			for (int i = 0; i < idURLs.length; i++) {
				urls.add(idURLs[i]);
			}
		}
		
		List<String> urlListSubset = urls.subList(start, end);
		Vector<String> urlSubset = new Vector<String>(urlListSubset);
		ParseOFMPub p = new ParseOFMPub();
		p.downloadWebpagesToDatabaseAdaptiveNonUnicode(urlSubset,sourceName,startFresh);
	}
	
	public static Vector<RecordOFMPub> parseWebpagesInDatabase() {
		String databaseFolder = "Data"+File.separator+"Experimental"+ File.separator + sourceName;
		String databasePath = databaseFolder+File.separator+sourceName+"_raw_html.db";
		Vector<RecordOFMPub> records = new Vector<>();

		try {
			Statement stat = MySQL_DB.getStatement(databasePath);
			ResultSet rs = MySQL_DB.getAllRecords(stat, ExperimentalConstants.strSourceOFMPub);

			int counter = 1;
			while (rs.next()) {
				if (counter % 100==0) { System.out.println("Parsed "+counter+" pages"); }
				
				String html = rs.getString("content");
				String url = rs.getString("url");
				String date = rs.getString("date");
				String cleanDate = date.substring(0,date.indexOf(" "));
				Document doc = Jsoup.parse(html);

				parseDocument(records,doc,url,cleanDate);

				counter++;
			}
			
			System.out.println("Parsed "+(counter-1)+" pages");
			return records;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static void parseDocument(Vector<RecordOFMPub> records,Document doc,String url,String date) {
		Elements tables = doc.select("table.RedTableCopy");
		for (Element table:tables) {
			RecordOFMPub opr = new RecordOFMPub();
			opr.date_accessed = date;
			opr.url = url;
			Elements rows = table.getElementsByTag("tr");
			for (Element row:rows) {
				Elements cells = row.getElementsByTag("td");
				String headerClass = cells.get(0).className();
				String header = cells.get(0).text();
				if (headerClass.equals("tableHeaderDkBlue")) { opr.endpoint = header; }
				if (cells.size() > 1) {
					String data = cells.get(1).text();
					if (header.contains("Category Chemical") && !header.contains("Result Type")) {
						Matcher matchCASandName = Pattern.compile("\\(([0-9-]+)\\)[ ]?(.+)").matcher(data);
						if (matchCASandName.find()) {
							opr.categoryChemicalCAS = matchCASandName.group(1);
							opr.categoryChemicalName = matchCASandName.group(2);
						}
					} else if (header.contains("Test Substance") && !header.contains("Result Type") && !header.contains("Comments")) {
						Matcher matchCASandName = Pattern.compile("\\(([0-9-]+)\\)?[ ]?(.*)").matcher(data);
						if (matchCASandName.find()) {
							opr.testSubstanceCAS = matchCASandName.group(1);
							opr.testSubstanceName = matchCASandName.group(2);
						}
					} else if (header.contains("Test Substance Comments")) { opr.testSubstanceComments = data;
					} else if (header.contains("Category Chemical Result Type")) { opr.categoryChemicalResultType = data;
					} else if (header.contains("Test Substance Result Type")) { opr.testSubstanceResultType = data;
					} else if (header.contains("Indicator") && !header.contains("Study")) { opr.indicator = data;
					} else if (header.contains("Value/Range")) { opr.value = data;
//					} else if (header.contains("pH Value")) { opr.pH = data;
//					} else if (header.contains("pKa")) { opr.pKa = data;
					} else if (header.contains("Results Remarks")) { opr.resultRemarks = data;
					} else if (header.contains("Study Reference")) { opr.reference = data;
					} else if (header.contains("Reliability") && !header.contains("Remarks")) { opr.reliability = data;
					} else if (header.contains("Reliability Remarks")) { opr.reliabilityRemarks = data;
					}
				}
			}
			records.add(opr);
		}
	}
	
	public static void main(String[] args) {
		downloadWebpagesToDatabaseFromIndex(true);
	}
}
