package gov.epa.exp_data_gathering.parse;


import java.io.IOException;
import java.util.Vector;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/*experimental records that appear relevant*/
public class RecordChemicalBook {
		String name;
		String synonyms;
		String CAS; // second(grayed)
		String MF;
		String MW; // fourth
		String EINECS;
		String molFile; // eighth
		String meltingPoint; // ninth
		String boilingPoint;
		String density; //11th
		String vapordensity;
		String vaporpressure; //13th
		String refractiveindex;
		String FP; // 15th
		String storagetemp;
		String solubility; //17th
		String pka;
		String form; // 19th
		String color;
		String odor; // 21st
		String relativepolarity;
		String odorthreshold; //23rd
		String explosivelimit;
		String watersolubility; // 25th
		String lambdamax;
		String merck; // 27th
		String BRN;
		String henrylc; // 29th
		String exposurelimits;
		String stability; //31st
		String InCHlKey;
		String CASreference; // 33rd
		String IARC;
		String NISTchemref; // 35th
		String EPAsrs;
		String hazardcodes; // 37th
		String filename;



private static RecordChemicalBook parseWebpage() {	
	String url = "https://www.chemicalbook.com/ProductChemicalPropertiesCB6854153_EN.htm";

	
	RecordChemicalBook rcb = new RecordChemicalBook();
	
	try {
		final Document doc = Jsoup.connect(url).get();
		
		Elements gray_elements = doc.getElementsByClass("ProdSupplierGN_ProductA_1");
		Elements white_elements = doc.getElementsByClass("ProdSupplierGN_ProductA_2");
		Vector<String> gray_vector = new Vector<String>(20);
		Vector<String> white_vector = new Vector<String>(20);

						
		// System.out.println(gray_elements.select("tr").text());
		
		for (Element gray_element:gray_elements) {
			gray_vector.add(gray_element.select("td:nth-of-type(2)").text());
			// System.out.println(gray_element.select("td:nth-of-type(2)").text());
			
			
		}
		
		// have a whole lot more of these to fill out.
		rcb.CAS = gray_vector.get(0);
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return rcb;

}
	


public static void main(String args[]) {
	// TODO Auto-generated method stub	
	RecordChemicalBook rcb = parseWebpage();
	System.out.println(rcb.CAS);

}

}