package gov.epa.exp_data_gathering.parse;
import gov.epa.ghs_data_gathering.GetData.RecordDashboard;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import gov.epa.ghs_data_gathering.GetData.RecordDashboard;


class Infopair{
	String PropertyName;
	String PropertyValue;
}


/*experimental records that appear relevant*/
public class RecordChemicalBook {
		String name;
		String synonyms;
		String CAS; 
		String MF;
		String MW; 
		String EINECS;
		String molfile; 
		String meltingpoint; 
		String boilingpoint;
		String density; 
		String vapordensity;
		String vaporpressure; 
		String refractiveindex;
		String FP; 
		String storagetemp;
		String solubility; 
		String pka;
		String form; 
		String color;
		String odor; 
		String relativepolarity;
		String odorthreshold;
		String explosivelimit;
		String watersolubility;
		String lambdamax;
		String merck; 
		String BRN;
		String henrylc; 
		String exposurelimits;
		String stability; 
		String InCHlKey;
		String CASreference; 
		String IARC;
		String NISTchemref; 
		String EPAsrs;
		String hazardcodes; 
		String filename;


		
		//
private static RecordChemicalBook parseWebpage() {
	RecordChemicalBook rcb = new RecordChemicalBook();
	String url = "https://www.chemicalbook.com/ProductChemicalPropertiesCB1361418_EN.htm";
   
	
	
	Vector<RecordDashboard> test = Parse.getDashboardRecordsFromExcel("C:\\Users\\Weeb\\Downloads\\list_chemicals-2020-11-16-13-10-59.xls");	
	Vector<String> searchurls = getsearchURLsFromDashboardRecords(test,1,20);

	
	return rcb;

}

private static void parseDocument(String url, RecordChemicalBook rcb) {
	try {
		final Document doc = Jsoup.connect(url).get();
		Elements gray_elements = doc.getElementsByClass("ProdSupplierGN_ProductA_1");
		Elements white_elements = doc.getElementsByClass("ProdSupplierGN_ProductA_2");
		
		
		for (Element gray_element:gray_elements) {
			String header = gray_element.select("td:nth-of-type(1)").text();
			String data = gray_element.select("td:nth-of-type(2)").text();;
			if (data != null && !data.isBlank()) {
				if (data != null && !data.isBlank()) {
					if (header.contains("CAS:") && !header.contains("CAS DataBase Reference")) { rcb.CAS = data; }
					else if (header.contains("Synonyms:")) { rcb.synonyms = data; }
					else if (header.contains("MF")) { rcb.MF = data; }
					else if (header.contains("EINECS")) { rcb.EINECS = data; }
					else if (header.contains("Mol File:")) { rcb.molfile = data; }
					else if (header.contains("Boiling point")) { rcb.boilingpoint = data; }
					else if (header.contains("density")) { rcb.density = data; }
					else if (header.contains("refractive index")) { rcb.refractiveindex = data; }
					else if (header.contains("Fp")) { rcb.FP = data; }
					else if (header.contains("form:")) { rcb.form = data; }
					else if (header.contains("Merck")) { rcb.merck = data; }
					else if (header.contains("BRN")) { rcb.BRN = data; }
					else if (header.contains("Henry's Law Constant")) { rcb.henrylc = data; }
					else if (header.contains("Exposure limits")) { rcb.exposurelimits = data; }
					else if (header.contains("Stability")) { rcb.stability = data; }
					else if (header.contains("InCHlKey")) { rcb.InCHlKey = data; }
					else if (header.contains("CAS DataBase")) { rcb.CASreference = data; }
					else if (header.contains("IARC")) { rcb.IARC = data; }
					else if (header.contains("NIST Chemistry")) { rcb.NISTchemref = data; }
					else if (header.contains("EPA")) { rcb.EPAsrs = data; }
					else if (header.contains("Hazard Codes")) { rcb.hazardcodes = data; }
					else if (header.contains("storage temp.")) { rcb.storagetemp = data; }
					else if (header.contains("solubility")) { rcb.solubility = data; }
					else if (header.contains("color")) { rcb.color = data; }
					else if (header.contains("Melting point")) { rcb.meltingpoint = data; }
					else if (header.contains("vapor density")) { rcb.vapordensity = data; }
					else if (header.contains("vapor pressure")) { rcb.vaporpressure = data; }
					else if (header.contains("Relative polarity")) { rcb.relativepolarity = data; }
					else if (header.contains("Odor Threshold")) { rcb.odorthreshold = data; }
					else if (header.contains("explosivelimit")) { rcb.explosivelimit = data; }
					else if (header.contains("Water Solubility")) { rcb.watersolubility = data; }
					else if (header.contains("max")) { rcb.lambdamax = data; }					
					else if (header.contains("MW")) { rcb.MW = data;
				}

				
					}
				}
			}
		
		for (Element white_element:white_elements) {
			String header = white_element.select("td:nth-of-type(1)").text();
			String data = white_element.select("td:nth-of-type(2)").text();;
			if (data != null && !data.isBlank()) {
				if (data != null && !data.isBlank()) {
					if (header.contains("CAS:") && !header.contains("CAS DataBase Reference")) { rcb.CAS = data; }
					else if (header.contains("Synonyms:")) { rcb.synonyms = data; }
					else if (header.contains("MF")) { rcb.MF = data; }
					else if (header.contains("EINECS")) { rcb.EINECS = data; }
					else if (header.contains("Mol File:")) { rcb.molfile = data; }
					else if (header.contains("Boiling point")) { rcb.boilingpoint = data; }
					else if (header.contains("density")) { rcb.density = data; }
					else if (header.contains("refractive index")) { rcb.refractiveindex = data; }
					else if (header.contains("Fp")) { rcb.FP = data; }
					else if (header.contains("form:")) { rcb.form = data; }
					else if (header.contains("Merck")) { rcb.merck = data; }
					else if (header.contains("BRN")) { rcb.BRN = data; }
					else if (header.contains("Henry's Law Constant")) { rcb.henrylc = data; }
					else if (header.contains("Exposure limits")) { rcb.exposurelimits = data; }
					else if (header.contains("Stability")) { rcb.stability = data; }
					else if (header.contains("InCHlKey")) { rcb.InCHlKey = data; }
					else if (header.contains("CAS DataBase")) { rcb.CASreference = data; }
					else if (header.contains("IARC")) { rcb.IARC = data; }
					else if (header.contains("NIST Chemistry")) { rcb.NISTchemref = data; }
					else if (header.contains("EPA")) { rcb.EPAsrs = data; }
					else if (header.contains("Hazard Codes")) { rcb.hazardcodes = data; }
					else if (header.contains("storage temp.")) { rcb.storagetemp = data; }
					else if (header.contains("solubility")) { rcb.solubility = data; }
					else if (header.contains("color")) { rcb.color = data; }
					else if (header.contains("Melting point")) { rcb.meltingpoint = data; }
					else if (header.contains("vapor density")) { rcb.vapordensity = data; }
					else if (header.contains("vapor pressure")) { rcb.vaporpressure = data; }
					else if (header.contains("Relative polarity")) { rcb.relativepolarity = data; }
					else if (header.contains("Odor Threshold")) { rcb.odorthreshold = data; }
					else if (header.contains("explosivelimit")) { rcb.explosivelimit = data; }
					else if (header.contains("Water Solubility")) { rcb.watersolubility = data; }
					else if (header.contains("max")) { rcb.lambdamax = data; }					
					else if (header.contains("MW")) { rcb.MW = data;
				}

				
			}
			}
			}
	}
 	catch (IOException e) {
 		
	// TODO Auto-generated catch block
	e.printStackTrace();
 	}
}



private static Vector<String> getsearchURLsFromDashboardRecords(Vector<RecordDashboard> records,int start,int end) {
	String baseURL = "https://www.chemicalbook.com/Search_EN.aspx?keyword=";
	Vector<String> urls = new Vector<String>();
	for (int i = start; i < end; i++) {
		String CAS = records.get(i).CASRN;
		if (!CAS.startsWith("NOCAS")) {
			String prefix = CAS.substring(0,3);
			if (prefix.charAt(2)=='-') { prefix = prefix.substring(0,2); }
			urls.add(baseURL+CAS);
		}
	}
	return urls;
}

private static String getpropertyURLfromsearchURL(String url){
		Document doc;
		try {
			doc = Jsoup.connect(url).get();
			Element importantregion = doc.select("ul.actionspro").first();
			 return importantregion.select("a:contains(Chemical)").attr("abs:href").toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
}


public static void main(String args[]) {
	// TODO Auto-generated method stub	
	RecordChemicalBook rcb = parseWebpage();
	parseDocument("https://www.chemicalbook.com/ProductChemicalPropertiesCB1361418_EN.htm",rcb);
	System.out.println(rcb.BRN.toString());

}

}