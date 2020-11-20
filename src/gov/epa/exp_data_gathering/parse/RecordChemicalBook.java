package gov.epa.exp_data_gathering.parse;
import gov.epa.api.AADashboard;
import gov.epa.api.ExperimentalConstants;
import gov.epa.api.ParseChemicalBook;
import gov.epa.ghs_data_gathering.GetData.RecordDashboard;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import gov.epa.ghs_data_gathering.GetData.RecordDashboard;


/*experimental records that appear relevant*/
public class RecordChemicalBook extends Parse {
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
		String fileName;

		static final String sourceName="ChemicalBook";
		

		//
private static void downloadWebpages(String filename, int start, int end) {

	Vector<RecordDashboard> records = Parse.getDashboardRecordsFromExcel(filename);	
	Vector<String> searchurls = getsearchURLsFromDashboardRecords(records,start,end);
	Vector<String> propertyurls = getpropertyURLfromsearchURL(searchurls);
	ParseChemicalBook p = new ParseChemicalBook();
	p.downloadWebpagesToZipFile(propertyurls, sourceName);
}

private static RecordChemicalBook parseZipWebpage()
		throws IOException, UnsupportedEncodingException {
	
	RecordChemicalBook cbr=new RecordChemicalBook();
	String mainFolder = AADashboard.dataFolder+File.separator+"Experimental"+ File.separator + sourceName;
	String zipFilePath = mainFolder + File.separator+"web pages"+".zip"; // will only grab the first
	ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath));
	Document doc = Jsoup.parse(zis.getNextEntry().toString(),"UTF-8");
    parseDocument(doc,cbr);
    return cbr;
    }
	

private static void parseDocument(Document doc, RecordChemicalBook rcb) {
	Elements gray_elements = doc.getElementsByClass("ProdSupplierGN_ProductA_1");
	Elements white_elements = doc.getElementsByClass("ProdSupplierGN_ProductA_2");
	Elements all_elements = new Elements();
	all_elements.addAll(white_elements);
	all_elements.addAll(gray_elements);	
	for (Element all_element:all_elements) {
		String header = all_element.select("td:nth-of-type(1)").text();
		String data = all_element.select("td:nth-of-type(2)").text();
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
				else if (header.contains("MW")) { rcb.MW = data;}

				}
			}
		}
}

private static Vector<String> getsearchURLsFromDashboardRecords(Vector<RecordDashboard> records,int start,int end) {
	String baseURL = "https://www.chemicalbook.com/Search_EN.aspx?keyword=";
	Vector<String> urls = new Vector<String>();
	for (int i = start; i < end; i++) {
		String CAS = records.get(i).CASRN;
		if (!CAS.startsWith("NOCAS")) {
			urls.add(baseURL+CAS);
		}
	}
	return urls;
}

private static Vector<String> getpropertyURLfromsearchURL(Vector<String> url){
	Vector<String> propertyURL = new Vector<String>();
	for(int i = 0; i < url.size(); i++) {
		Document doc;
		try {
			doc = Jsoup.connect(url.get(i)).get();
			Element importantregion = doc.select("ul.actionspro").first();
			 propertyURL.add(importantregion.select("a:contains(Chemical)").attr("abs:href").toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	return propertyURL;
}


public static void main(String args[]) {
	// TODO Auto-generated method stub	
	try {
		RecordChemicalBook cbr = parseZipWebpage();
		System.out.println(cbr.name);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	
	//downloadWebpages("Data" +"/PFASSTRUCT.xls",1,10);
	
	// System.out.println(rcb.BRN.toString());
	

}




}
