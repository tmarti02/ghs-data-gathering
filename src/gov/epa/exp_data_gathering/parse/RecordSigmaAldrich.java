package gov.epa.exp_data_gathering.parse;

import java.io.File;
import java.util.Random;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import gov.epa.api.ExperimentalConstants;


public class RecordSigmaAldrich {
	String cas;
	String name;
	String vaporPressure;
	String bp;
	String mp;
	String density;
	String solubility;
	String smiles;
	
	static final String sourceName = ExperimentalConstants.strSourceSigmaAldrich;
	
	public static void downloadWebpagesFromExcelToDatabase(String filename,int start,int end,boolean startFresh) {
		Vector<RecordDashboard> records = DownloadWebpageUtilities.getDashboardRecordsFromExcel(filename);
		Vector<String> searchURLs = getSearchURLsFromDashboardRecords(records,start,end);
		Vector<String> listingURLs = new Vector<String>();
		// Random rand = new Random();
		for (String search:searchURLs) {
			System.out.println(search);
			try {
				// long startTime = System.currentTimeMillis();
				Document doc = Jsoup.connect(search).get();
				// long endTime = System.currentTimeMillis();
				// long delay = endTime - startTime;
				Elements products = doc.select("div.productContainer clearfix");
				for (Element product:products) {
					Elements listings = product.select("li.productNumberValue");
					for (Element listing:listings) {
						String url = listing.getElementsByTag("a").first().attr("href");
						listingURLs.add("https://www.sigmaaldrich.com/"+url);
					}
				}
				Thread.sleep(15000);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		ParseSigmaAldrich p = new ParseSigmaAldrich();
		String databasePath = p.databaseFolder + File.separator + sourceName + "_raw_html.db";
		DownloadWebpageUtilities.downloadWebpagesToDatabaseAdaptive(listingURLs,"div#productDetailProperties",databasePath,sourceName,startFresh);		
	}
	
	private static Vector<String> getSearchURLsFromDashboardRecords(Vector<RecordDashboard> records,int start,int end) {
		String headURL = "https://www.sigmaaldrich.com/catalog/search?term=";
		String tailURL = "&interface=CAS%20No.&N=0&mode=match%20partialmax&lang=en&region=US&focus=product";
		Vector<String> urls = new Vector<String>();
		for (int i = start; i < end; i++) {
			String CAS = records.get(i).CASRN;
			if (!CAS.startsWith("NOCAS")) {
				urls.add(headURL+CAS+tailURL);
			}
		}
		return urls;
	}
	
	public static void main(String[] args) {
		downloadWebpagesFromExcelToDatabase("Data"+"/PFASSTRUCT.xls",1,2,true);
	}
}
