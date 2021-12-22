package gov.epa.ghs_data_gathering.Parse.Links.SVHC_Prioritization_List;

import java.io.File;

import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.JsonObject;

import gov.epa.ghs_data_gathering.Parse.Links.RecordLink;

/**
 * Retrieving webpage from:
 * https://echa.europa.eu/previous-recommendations?p_p_id=viewsubstances_WAR_echarevsubstanceportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&_viewsubstances_WAR_echarevsubstanceportlet_keywords=&_viewsubstances_WAR_echarevsubstanceportlet_orderByCol=synonymDynamicField_1547&_viewsubstances_WAR_echarevsubstanceportlet_orderByType=asc&_viewsubstances_WAR_echarevsubstanceportlet_resetCur=false&_viewsubstances_WAR_echarevsubstanceportlet_delta=200
 * @author TMARTI02
 *
 */

public class RecordSVHC_Prioritization_List {

	public String Name;
	public String EC_Number;
	public String CAS;
	public String link;
	
	public static String sourceName="SVHC Prioritzation List";
	
	public static Vector<JsonObject> parseRecordsFromHTML() {

		try {
			File file=new File("AA Dashboard/data/"+sourceName+"/Submitted recommendations - ECHA.html");
			Document doc = Jsoup.parse(file, "UTF-8");

//			System.out.println(doc.toString());
			
			Elements elements = doc.select("tbody[class=table-data]");

		    Elements rows = elements.get(0).select("tr");
//		    System.out.println(rows.size());
		    
		    for (int i=0;i<rows.size()-1;i++) {
		    	Element row=rows.get(i);
		    	
		    	RecordSVHC_Prioritization_List r=new RecordSVHC_Prioritization_List();
		    	
		    	Elements cells = row.select("td");
		    	
		    
		    	r.Name=cells.get(0).text();
		    	r.CAS=cells.get(2).text();
		    	String link=cells.get(5).getElementsByTag("button").attr("onclick");
		    	link=link.substring(link.indexOf("'")+1,link.length());
		    	link=link.substring(0,link.indexOf("'")).replace("\\x2f","/").replace("\\x2e",".").replace("\\x3a", ":");
		    	
//		    	System.out.println(r.Name+"\t"+r.CAS+"\t"+link);
		    	
		    	
		    	RecordLink rl=new RecordLink();
		    	rl.Name=r.Name;
		    	rl.CAS=r.CAS;
		    	rl.SourceName=sourceName;
		    	rl.LinkName="Submitted recommendations";
		    	rl.URL=link;
		    	
		    	if (rl.CAS.contains(" ")) {
		    		String [] listCAS=rl.CAS.split(" ");
		    		
		    		for (String CAS:listCAS) {
		    			rl.CAS=CAS;
		    			System.out.println(rl);
		    		}
		    		
		    	} else {
		    		System.out.println(rl);	
		    	}
		    	
		    	
		    }
		    
		    
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
//		Vector<JsonObject> records = esr.parseRecordsFromExcel(hm, 0);
//		return records;
		return null;
	}
	
	public static void main(String[] args) {
		parseRecordsFromHTML();
	}
}
