package gov.epa.exp_data_gathering.parse;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.sql.RowSetListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class RecordSander {
	
	String reference;
	static final String sourceName="Sander";	


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		downloadWebpagesHTML();
	}
	
	
public static void downloadWebpagesHTML() {
	Vector<String> urls = ObtainWebpages();
	for (int i = 0; i < urls.size(); i++) {
		System.out.println(urls);
	}
	ParseSander p = new ParseSander();
	p.mainFolder = p.mainFolder + File.separator + "General";
	p.databaseFolder = p.mainFolder;
	p.downloadWebpagesToDatabaseAdaptive(urls,"tbody", sourceName,true);
}


private static Vector<String> ObtainWebpages() {
	String baseSearchLink = "http://satellite.mpic.de/henry/search_identifier.html?csrfmiddlewaretoken=eNhrzlz52Jf3pHHxhPPvsFfi0jCmhStqaIIF7xXrsctaPEuHMIgkdjAkRyUDvPQm&x=0&y=0&search=";
	Vector<String> allLinks = new Vector<String>();
	try {
		Document doc = Jsoup.connect(baseSearchLink).get();
		Elements rows = doc.select("td[width=60%] > table > tbody > tr");
		for (int i = 1; i < rows.size(); i++) {
			allLinks.add(rows.get(i).select("td ~ td > a").attr("abs:href").toString());
		}
	return allLinks;
	}
	catch (IOException e) {
		e.printStackTrace();
	}
	return null;
}

private static void Parsewebpage(String url) {
	try {
		Document doc = Jsoup.connect("").get();
		Element table = doc.select("td[width=60%] > table ~ table[width=100%] > tbody").first();
		Elements tableData = table.select("tr");
		for (int i = 0; i < tableData.size(); i++) {
			System.out.println(tableData.get(i).toString());
		}
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

}
