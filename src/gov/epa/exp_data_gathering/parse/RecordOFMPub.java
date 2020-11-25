package gov.epa.exp_data_gathering.parse;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RecordOFMPub {
	String cas;
	String name;
	String submissionID;
	Vector<String> meltingPoint;
	Vector<String> boilingPoint;
	Vector<String> vaporPressure;
	Vector<String> partitionCoefficient;
	Vector<String> waterSolubility;
	
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
					ids.add(matchSubmissionID.group(1));
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
	
	public static void downloadWebpagesToDatabase() {
		Vector<String> ids = getSubmissionIDs();
		Vector<String> urls = new Vector<String>();
		for (String id:ids) {
			String[] idURLs = constructURLs(id);
			for (int i = 0; i < idURLs.length; i++) {
				urls.add(idURLs[i]);
			}
		}
	}
	
	public static void main(String[] args) {
		// TODO
	}
}
