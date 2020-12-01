package gov.epa.exp_data_gathering.parse;
import java.io.IOException;
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ObtainWebpages();

	}

private static void ObtainWebpages() {
	String baseSearchLink = "http://satellite.mpic.de/henry/search_identifier.html?csrfmiddlewaretoken=eNhrzlz52Jf3pHHxhPPvsFfi0jCmhStqaIIF7xXrsctaPEuHMIgkdjAkRyUDvPQm&x=0&y=0&search=";
	Set<String> allLinks = new HashSet<String>();
	for (int j = 0; j < 10; j++) {
	String searchLink = baseSearchLink + j;
	try {
		Document doc = Jsoup.connect(searchLink).get();
		Elements rows = doc.select("td[width=60%] > table > tbody > tr");
		for (int i = 1; i < rows.size(); i++) {
			allLinks.add(rows.get(i).select("td ~ td > a").attr("abs:href").toString());

		}
	}
	catch (IOException e) {
		e.printStackTrace();
	}
	}
}

private static void Parsewebpage() {
	try {
		Document doc = Jsoup.connect("http://satellite.mpic.de/henry/casrn/678-39-7").get();
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