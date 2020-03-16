package gov.epa.ghs_data_gathering.Parse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import gov.epa.ghs_data_gathering.Utilities.Utilities;
import gov.epa.ghs_data_gathering.API.FlatFileRecord;
import gov.epa.ghs_data_gathering.API.FlatFileRecord2;
import gov.epa.ghs_data_gathering.API.ScoreRecord;
import gov.epa.ghs_data_gathering.GetData.SkinSensitization.RecordChembench;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;



import java.util.ArrayList;

//import org.jsoup.Jsoup;
//import org.jsoup.select.Elements;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.NodeList;


public class ParsePubChem {

	
	class Reference {
		String referenceNumber;
		String sourceName;
		String sourceID;
		String name;
		String description;
		String url;
		
		public String toString() {
			String str="referenceNumber="+referenceNumber+"\n";
			str+=("sourceName="+sourceName+"\n");
			str+=("sourceID="+sourceID+"\n");
			str+=("name="+name+"\n");
			str+=("description="+description+"\n");
			str+=("URL="+url+"\n");
			return str;
		}
	}


	private static final int ArrayList = 0;

//	void parseXML() {
//		try {
//			
//			BufferedReader br=new BufferedReader(new FileReader("AA Dashboard/Data/pubchem/CID-LCSS.xml"));
//
//			String start="<Record>";
//			String stop="</Record>";
//			
//			String Line="";
//			while (true) {
//				Line=br.readLine();
//				if (Line.contains(start)) break;
//			}
//			
//			String data=Line;
//			
//			while (true) {
//				Line=br.readLine();
//				data+=Line+"\r\n";
//				if (Line.contains(stop)) break;
//			}
//
////			System.out.println(data);
//			
//			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//			
//			ByteArrayInputStream bis=new ByteArrayInputStream(data.getBytes());
//			
//			Document doc = dBuilder.parse(bis);
//
//			//optional, but recommended
//			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
//			doc.getDocumentElement().normalize();
//			
//			
//			NodeList nList = doc.getElementsByTagName("Section");
//			
////			System.out.println(nList.getLength());
//			
//			for (int i=0;i<nList.getLength();i++) {
//				Element nodei=(Element)nList.item(i);
//				String heading=nodei.getElementsByTagName("TOCHeading").item(0).getTextContent();
//				String description=nodei.getElementsByTagName("Description").item(0).getTextContent();
//				
////				System.out.println(heading+"\t"+description);
//				
//				if (description.contains("GHS")) {
//					
//					NodeList infoElements=nodei.getElementsByTagName("Information");
//
//					for (int j=0;j<infoElements.getLength();j++) {
//						Element nodej=(Element)infoElements.item(j);
//						
//						String referenceNumber=nodej.getElementsByTagName("ReferenceNumber").item(0).getTextContent();
//						String name=nodej.getElementsByTagName("Name").item(0).getTextContent();
//						
//						String stringValue=nodej.getElementsByTagName("StringValue").item(0).getTextContent();
//						
//						org.jsoup.nodes.Document document = Jsoup.parse(stringValue);
//					    document.select("img").remove();
//					    document.select("head").remove();
//					    document.select("span.fred").remove();
//					    document.select("div.pc-thumbnail-container").remove();
//					    Elements body = document.select("body");
//					    
////					    System.out.println(referenceNumber+"\t"+body.html()+"\n\n");
//					    
//					    if(!body.select("div.ghs-hazards").isEmpty()) {
//					    	//Hcodes
//					    	boolean print=false;
//					    	handleGHS_Hazards(referenceNumber, document, body,print);
//					    } 
//					    
//					    if(!body.select("div.ghs-precautionary").isEmpty()) {
//					    	//Pcodes
//					    	handlePrecautionary(referenceNumber, document, body);
//					    }
//					    
//					    if (body.text().equals("Signal:")) continue;
//					    
//					    String bodyHTML=body.html();
//					    handleRestOfBody(referenceNumber, bodyHTML);
//					}
//				} else if (heading.equals("CAS")) {
//					NodeList infoElements=nodei.getElementsByTagName("Information");
//
//					for (int j=0;j<infoElements.getLength();j++) {
//						
//						Element nodej=(Element)infoElements.item(j);
//						
//						String referenceNumber=nodej.getElementsByTagName("ReferenceNumber").item(0).getTextContent();
//						String name=nodej.getElementsByTagName("Name").item(0).getTextContent();
//						
//						String stringValue=nodej.getElementsByTagName("StringValue").item(0).getTextContent();
//						System.out.println(stringValue);
//						
//					}
//				}
//				
//			} 
//			
//			br.close();
//			
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		
//		
//	}
//	
	void parseXML() {
		try {
//			BufferedReader br = new BufferedReader(new FileReader("AA Dashboard/Data/pubchem/CID-LCSS.xml_2019_05_06"));

			String filepath="AA Dashboard/Data/pubchem/CID-LCSS_2019_05_06.xml.gz";
			
			File file=new File(filepath);
			System.out.println(file.getAbsolutePath());
			
			InputStream fileStream = new FileInputStream(filepath);
			InputStream gzipStream = new GZIPInputStream(fileStream);
			Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
			BufferedReader br = new BufferedReader(decoder);

			FileWriter fw=new FileWriter("AA Dashboard/Data/pubchem/CID-LCSS_2019_05_06-parsed.txt");
			fw.write(FlatFileRecord2.getHeader("|")+"\r\n");
			
			String start="<Record>";
			String stop="</Record>";
			
			String header=seek(br,start);
//			System.out.println(header);
			
			int counter=0;
			
			ArrayList<String>sources=new ArrayList<>();
			
			while (true) {
				String data = seek(br, stop);
				
				if (data==null) break;
				
				
				if (counter%10==0) System.out.println(counter);
				
				if (counter==0)	{
					data="<Record>\r\n"+data;
//					System.out.println(data);
				}
				
				Document record = Jsoup.parse(data);
				ArrayList<FlatFileRecord2>records=parseSections2(record,counter);
				
				String recordNumber=record.selectFirst("recordnumber").text();
				

				if (records==null) continue;
				
				record.select("section").remove();//remove the section because they have references
				
				Hashtable<String,Reference> htRef=parseReferences(record);
				
//				System.out.println(counter+"\t"+records.size());
				
				for (FlatFileRecord2 ffr:records) {
					Reference r=htRef.get(ffr.referenceNumber);
					//				System.out.println(r);
					ffr.name=r.name;//Store name from the reference (might help with determining CAS- or need to use URL to get cas)
					ffr.source=r.sourceName;
					ffr.sourceID=r.sourceID;
					ffr.URL=r.url;
					ffr.CID=recordNumber;
					
					if (!sources.contains(ffr.source)) {
//						System.out.println(ffr.source+"\t"+ffr.URL);
						sources.add(ffr.source);
					}

					fw.write(ffr.toString("|")+"\r\n");
					fw.flush();
//					System.out.println(ffr);
				}
			
				counter++;
//				if (true) break;
				
			}
			
			
			
			fw.close();
			br.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}


	private Hashtable<String,Reference> parseReferences(Document document) {

		Elements references = document.select("Reference");
		
		Hashtable<String,Reference>ht=new Hashtable<>();
		
		for (Element eReference:references) {
			
			Reference r=new Reference();
			
			r.referenceNumber=eReference.select("referencenumber").first().text();
			r.sourceName=eReference.select("sourcename").first().text();
			r.sourceID=eReference.select("sourceid").first().text();
			
//			System.out.println("sourceName="+r.sourceName);
			
			if(eReference.select("name").size()>0) {
				r.name=eReference.select("name").first().text();
			}
			
			if(eReference.select("description").size()>0) {
				r.description=eReference.select("description").first().text();	
			}
			
			if(eReference.select("url").size()>0) {
				r.url=	eReference.select("url").first().text();	
			}
			
//			System.out.println(r);
			ht.put(r.referenceNumber, r);
			
			
		}//end loop over sections
		return ht;
}


//	private ArrayList<FlatFileRecord2> parseSections(Document document,int counter) {
//		Elements sections = document.select("Section");
//		
//		ArrayList<FlatFileRecord2> records=null;
//		
//		for (Element section:sections) {
//			String heading=section.select("TOCHeading").first().text();
////			System.out.println("*"+heading+"*");
//			
//			if (heading.equals("GHS Classification")) {
////				if (counter==0) System.out.println(section);
//				records=parseGHS(document,section,counter);
//			} else if (heading.equals("CAS")) {
//				String CAS=this.parseCAS(document, section);
//				
//				for (FlatFileRecord2 ffr: records) {
//					ffr.CAS=CAS;
//				}
//				
////					System.out.println("Identifiers:"+section.html());
//			} else if (heading.equals("Toxicity Data")) {
////					System.out.println("Toxicity Data:"+section.html());
//			}
//		}//end loop over sections
//		
//		return records;
//	}
	private ArrayList<FlatFileRecord2> parseSections2(Document record,int counter) {
		Elements sections = record.select("Section");
		
		ArrayList<FlatFileRecord2> records=null;
		
		for (Element section:sections) {
			String heading=section.select("TOCHeading").first().text();
//			System.out.println("*"+heading+"*");
			
			if (heading.equals("GHS Classification")) {
//				if (counter==0) System.out.println(section);
				records=parseGHS2(section,counter);
			} else if (heading.equals("Identifiers")) {
				
				if (records==null) continue;
				
				//Create lookup for CAS for reference numbers:
				Hashtable<String,String>htCAS=this.parseIdentifiers(section);
				
				for (FlatFileRecord2 ffr: records) {
					
					if (htCAS.get(ffr.referenceNumber)==null) {
						//We dont have a match in the look up by reference number:
//						System.out.println("CAS missing for "+ffr.referenceNumber);
						String casNumbers = getCAS_List_String(htCAS);
						
						if (casNumbers!=null && ffr.name!=null && casNumbers.contains(ffr.name)) {
							System.out.println(ffr.name+"\t"+casNumbers);
						}
						
						
				        ffr.CAS=casNumbers;
					 
					} else {
						ffr.CAS=htCAS.get(ffr.referenceNumber);	
					}
				}
				
//					System.out.println("Identifiers:"+section.html());
			} else if (heading.equals("Toxicity Data")) {
//					System.out.println("Toxicity Data:"+section.html());
			}
		}//end loop over sections
		
		return records;
	}


	private String getCAS_List_String(Hashtable<String, String> htCAS) {
		Vector<String>casPossible=new Vector<String>();

		//Just create a list of unique CAS numbers instead
		Set<String> keys = htCAS.keySet();
		for(String key: keys){
		    String CAS=htCAS.get(key);
		    if (!casPossible.contains(CAS)) casPossible.add(CAS);
		}
		
		String casNumbers="";
		for (String CAS:casPossible) {
			casNumbers+=CAS+"; ";
		}
		if (!casNumbers.isEmpty()) casNumbers=casNumbers.substring(0,casNumbers.length()-2);
		return casNumbers;
	}

//	private String parseCAS(Document document, Element section) {
//		//					System.out.println(section.html());	
//
//		ArrayList<String> identifiers= new ArrayList<>();
//		
//		Elements informationElements = section.select("information");
//
//		for (int j=0;j<informationElements.size();j++) {
//			Element information=(Element) informationElements.get(j);
//			String referenceNumber=information.select("referencenumber").text();
//			String name=information.select("name").text();
//			String stringvalue=information.select("stringvalue").text();
//			
//			if (name.equals("CAS")) {
//				if(!identifiers.contains(stringvalue)) {
////					System.out.println(stringvalue);
//					identifiers.add(stringvalue);
//				}
//			} else {
//				System.out.print("Identifier="+name);
//			}
//		}
//		
//		String CAS="";
//		for (String identifier:identifiers) {
//			if (CAS.isEmpty()) CAS=identifier;
//			else {
//				CAS+="; "+identifier;
//			}
//		}
//
//		
//		return CAS;
//		
//	}
	
	private Hashtable<String,String> parseIdentifiers(Element sectionIdentifier) {
		//					System.out.println(section.html());	

		Hashtable<String,String>htCAS=new Hashtable<>();
		
		Elements sections = sectionIdentifier.select("Section");
		
		for (Element section:sections) {
			String heading=section.select("TOCHeading").first().text();
			
			if (heading.contentEquals("CAS")) {
//				System.out.println(section);
				Elements informations=section.select("information");
				for (Element information:informations) {
					
					String CAS=information.select("string").first().text();
					String refNum=information.select("ReferenceNumber").first().text();
//					System.out.println(refNum+"\t"+CAS);	
					htCAS.put(refNum, CAS);
				}
			} else {
				//TODO- inchi and inchikey
			}
		}
		return htCAS;
	}
	

//	private ArrayList<FlatFileRecord2> parseGHS(Document document, Element section,int counter) {
//		//					System.out.println(section.html());	
//
//		ArrayList<FlatFileRecord2> records= new ArrayList<>();
//		
//		Elements informationElements = section.select("information");
//
//		for (int j=0;j<informationElements.size();j++) {
//			Element information=(Element) informationElements.get(j);
//			String referenceNumber=information.select("referencenumber").text();
//			String name=information.select("name").text();
//			String stringvalue=information.select("stringvalue").text();
//			String stringhtml=information.select("stringvalue").html();
//			
////			if (stringvalue.contains("<br>H (2<br>H315:")) {
////				System.out.println(stringhtml);
////			}
//			
//			if (stringvalue.equals("Insufficient data for GHS classification")) continue;
//			if (stringvalue.equals("No hazard classification according to GHS criteria")) continue;
//					
//			if (name.equals("GHS Hazard Statements")) {
//				
//				if (counter==0) System.out.println(information);
//				
////			if (name.equals("GHS Classification")) {
//				//							System.out.println(i+"\t"+j+"\t"+referenceNumber+"\t"+name+"\t"+stringvalue);
//
//				Elements body = getBody(information);
//				////						    
//				if (counter==0) System.out.println(referenceNumber+"\t"+body.html()+"\n\n");
//
//				if(!body.select("div.ghs-hazards").isEmpty()) {
//					//Hcodes
//					boolean print=false;
//					ArrayList<FlatFileRecord2> recordsGHS_Hazards=handleGHS_Hazards(referenceNumber, document, body,print,stringvalue);
//					
//					for (FlatFileRecord2 ffr:recordsGHS_Hazards) {
//						records.add(ffr);
//					}
//					
//				} 
//
//				if(!body.select("div.ghs-precautionary").isEmpty()) {
//					//Pcodes
//					handlePrecautionary(referenceNumber, document, body);
//				}
//				//					    
//				if (body.text().equals("Signal:")) continue;
//
//				String bodyHTML=body.html();
//				handleRestOfBody(referenceNumber, bodyHTML);
//			}
//		}
//		
//		return records;
//	}

	private ArrayList<FlatFileRecord2> parseGHS2(Element section,int counter) {
		ArrayList<FlatFileRecord2> recordsOverall= new ArrayList<>();
		
		Elements informationElements = section.select("information");


		for (Element information:informationElements) {
			String referenceNumber=information.select("referencenumber").text();
			String name=information.select("name").text();
			
//			if (counter==0) {
//				System.out.println("*"+name);
//			}

			if (name.equals("GHS Hazard Statements")) {
				
				ArrayList<FlatFileRecord2> records= new ArrayList<>();
				
				Elements stringsWithMarkup = information.select("stringwithmarkup");

				String note="";
				
				for (Element stringWithMarkup:stringsWithMarkup ) {
//					System.out.println("*"+name+" "+stringWithMarkup);					
					Elements strings = stringWithMarkup.select("string");					
//					if (counter==0) System.out.println("strings:");
						
					for (Element string:strings) {
						if (!string.hasText()) continue;
						
						if (!string.text().contains("H") || !string.text().contains(":")) {
							note+=string.text()+" ";
						} else {
							FlatFileRecord2 f=createRecord(referenceNumber,string.text());
							records.add(f);
							
//							if (counter==0)	System.out.println(referenceNumber+" "+string.text());	
						}
					}
				}
				
				note=note.trim();
				
				for (FlatFileRecord2 ffr2:records) {
					ffr2.note=note;
					recordsOverall.add(ffr2);
				}
//				System.out.println(referenceNumber+"\tNote="+note+"\n");
			}
		}
		
		return recordsOverall;
	}


	private FlatFileRecord2 createRecord(String referenceNumber,String text) {
		FlatFileRecord2 f=new FlatFileRecord2();
		
		if (!text.contains(":")) {
			System.out.println(text+": no colon");
			return null;
		}
		
		f.hazard_code=text.substring(0,text.indexOf(":"));
		
		if (f.hazard_code.contains("(")) {
			f.percentage=f.hazard_code.substring(f.hazard_code.indexOf("(")+1,f.hazard_code.indexOf(")"));
			f.hazard_code=f.hazard_code.substring(0,f.hazard_code.indexOf("(")).trim();
		}
		
		if (text.contains("[")) {
			String text2=text.substring(text.indexOf("[")+1,text.indexOf("]"));			
			f.hazard_name=text2.trim();
		}
		
		
		f.hazard_statement=text.substring(text.indexOf(":")+1,text.indexOf("[")).trim();
		f.referenceNumber=referenceNumber;
		
//		System.out.println(f);
		return f;
	}

//private Elements getBody(Element information) {
//	Element eInfo=information.select("stringvalue").first();
//	
//	Document docInfo=Jsoup.parse(eInfo.text());
//	docInfo.select("img").remove();
////	docInfo.select("a").remove();
//	docInfo.select("head").remove();
//	docInfo.select("span.fred").remove();
//	docInfo.select("div.pc-thumbnail-container").remove();
//	//
//	//						    System.out.println(referenceNumber+"\t"+docInfo.text());
//	//						    
//	Elements body = docInfo.select("body");
//	return body;
//}


	private String seek(BufferedReader br, String stop) throws IOException {
		String data="";
		
		while (true) {
			String Line=br.readLine();
			if (Line==null) return null;
			
			data+=Line+"\r\n";
			if (Line.contains(stop)) {
//				System.out.println(Line);
				break;
			}
			
		}
		return data;
	}


//	private void handlePrecautionary(String referenceNumber, org.jsoup.nodes.Document document, Elements body) {
//		Elements div=body.select("div.ghs-precautionary");
//		
//		String [] lines=div.html().split("\n");
//		
//		for (String line:lines) {
//			
//			line=line.replace("<br>","").trim();
//			
//			if (line.contains("<b>Precautionary Statement Codes</b>")) continue;
//			
//			if (line.contains("(The corresponding statement to each P-code can be found")) continue;
//			if (line.contains("<a href=\"https://pubchem.ncbi.nlm.nih.gov/ghs/#_prec\">here</a>.)")) continue;
//			
//			//TODO Parse P codes to records here
////			System.out.println(line);
//		}
//
//		
////		System.out.println(referenceNumber+"\t"+div.html());
//		document.select("div.ghs-precautionary").remove();
//	}


//	private void handleRestOfBody(String referenceNumber, String bodyHTML) {
////		System.out.println(bodyHTML);
//		
//		if (bodyHTML.contains("\n") && bodyHTML.contains("<br>")) {
//			
//			String [] lines=bodyHTML.split("\n");
//			
//			for (String line:lines) {
//				
//				if (line.contains("Signal: Danger")) continue;
//				
//				line=line.replace("<br>","").trim();
//				
//				//TODO parse line to records here
//				
////				System.out.println(line);
//			}
//		} else {
////			System.out.println(referenceNumber+"\tbodyHtml:"+bodyHTML);
//		}
//	}

	
//	private ArrayList<FlatFileRecord2> handleGHS_Hazards(String referenceNumber, Document document, Elements body,boolean print,String stringvalue) {
//		Elements div=body.select("div.ghs-hazards");
//		
//		div.select("span.fblue").remove();
//		
//		//TODO: store hazards here
////					    	System.out.println(referenceNumber+"\t"+div.html());
////		System.out.println(div.html());
//		
//		
////		if (div.html().contains("H420")) {
////			System.out.println(div.html());	
////		}
//		
//		String html=div.html();
//		html=html.replace("<b>GHS Hazard Statements</b>", "");
//		html=html.replace("\n", "").trim();
//		
//		String [] lines=html.split("<br>");
//				
//		String note="";
//		
//		ArrayList<FlatFileRecord2>records=new ArrayList<>();
//		
//		for (String line:lines) {
////			line=line.replace("<br>","").trim();
//			
//			if (line.isEmpty()) continue;
//			
//			if (line.contains("H (")) continue;
//			
//			if (!line.substring(0, 1).equals("H")) {
//				
//				if (note.isEmpty()) note=line;
//				else note+="<br>"+line;
////				if(print) {
////					System.out.println("\n"+referenceNumber+"\tNote:"+line);
////				}
//				
//			} else {
////				if(print)System.out.println(referenceNumber+"\t"+line);
//				
//				if (!line.contains(":")) continue;
//				
//				FlatFileRecord2 ffr=new FlatFileRecord2();
//				
//				try {
//					ffr.hazard_code=line.substring(0,line.indexOf(":"));
//
//					if (ffr.hazard_code.contains("(")) {
//						String concentration=ffr.hazard_code.substring(ffr.hazard_code.indexOf("(")+1,ffr.hazard_code.indexOf(")"));
//						ffr.hazard_code=ffr.hazard_code.substring(0,ffr.hazard_code.indexOf(" ("));
//						ffr.concentration=concentration;
//					}
//
//					ffr.hazard_statement=line.substring(line.indexOf(":")+1,line.indexOf("[")).trim();
//					ffr.hazard_name=line.substring(line.indexOf("[")+1,line.indexOf(" -")).trim();
//					ffr.category=line.substring(line.indexOf("- ")+1,line.indexOf("]")).trim();
//
//
//				} catch (Exception ex) {
//					System.out.println("Parse error: "+stringvalue);
//					ex.printStackTrace();
//					continue;
//				}
//				ffr.referenceNumber=referenceNumber;
//				
//				
//				records.add(ffr);
//			}
//			
//			
//		}
//		
//		for (FlatFileRecord2 ffr:records) {
//			ffr.note=note;
//			if (print) System.out.println(referenceNumber+"\t"+ffr.hazard_name+"\t"+ffr.hazard_code+"\t"+ffr.hazard_statement+"\t"+ffr.category+"\t"+ffr.concentration+"\t"+ffr.referenceNumber+"\t"+ffr.note);
//		}
//		
////		if (print) System.out.println("note="+note);
//		if(print)System.out.println("");
//		
//		document.select("div.ghs-hazards").remove();
//		
//		return records;
//	}
	
	
	
	void comparePubchemToMine() {
		
		String folderPubchem="AA Dashboard\\Data\\pubchem";
		String filePathPubchem=folderPubchem+"\\CID-LCSS_2019_05_06-parsed.txt";
		

//		String mySource=ScoreRecord.sourceJapan;
//		String mySource=ScoreRecord.sourceAustralia;
		String mySource=ScoreRecord.sourceECHA_CLP;
		
		String folderMine="AA Dashboard\\Data\\"+mySource;
		String filePathMine=folderMine+"\\"+mySource+" Chemical Records.txt";
		
		String pubChemSourceName="NITE-CMC";
//		String pubChemSourceName="EU REGULATION (EC) No 1272/2008";
//		String pubChemSourceName="European Chemicals Agency (ECHA)";
//		String pubChemSourceName="HSDB";
//		String pubChemSourceName="Hazardous Chemical Information System (HCIS), Safe Work Australia";
		
		boolean performComparison=false;
		
		String filePathPubchemSource=folderPubchem+"\\CID-LCSS_2019_05_06-parsed_"+pubChemSourceName+".txt";
		
		try {

			File filePubchemSource=new File(filePathPubchemSource);
			
			if ( !filePubchemSource.exists()) {
				Vector<FlatFileRecord2>recordsPubchem=FlatFileRecord2.loadRecordsFromFile(filePathPubchem, "CAS", "|");
				System.out.println("big pubchem loaded");
				FlatFileRecord2.writeToFile(recordsPubchem, filePathPubchemSource, "|",pubChemSourceName);
			}
			

			Vector<FlatFileRecord2>recordsPubchem=FlatFileRecord2.loadRecordsFromFile(filePathPubchemSource, "CAS", "|");
			System.out.println("pubchem loaded");
			
			Vector<FlatFileRecord>recordsMine=FlatFileRecord.loadRecordsFromFile(filePathMine, "CAS", "|");
			System.out.println("Mine loaded");
			
			
			Vector<String>uniqueCAS=new Vector<>();
			for (FlatFileRecord record:recordsMine) {
				if (!uniqueCAS.contains(record.CAS)) uniqueCAS.add(record.CAS);
			}
			System.out.println(mySource+"\t"+uniqueCAS.size()+"\t"+recordsMine.size());
			
			if (!performComparison) return;
						
			Hashtable<String,String>htHazardName=new Hashtable<>();
			htHazardName.put("Acute Mammalian Toxicity Oral","Acute toxicity, oral");
			htHazardName.put("Acute Mammalian Toxicity Inhalation","Acute toxicity, inhalation");
			htHazardName.put("Acute Mammalian Toxicity Dermal","Acute toxicity, dermal");
			htHazardName.put("Eye Irritation","eye damage/eye irritation");
			htHazardName.put("Systemic Toxicity Repeat Exposure","Specific target organ toxicity, repeated exposure");
			htHazardName.put("Systemic Toxicity Single Exposure","Specific target organ toxicity, single exposure");
			htHazardName.put("Skin Sensitization","Sensitization, Skin");
			htHazardName.put("Skin Irritation","Skin corrosion/irritation");

			htHazardName.put("Genotoxicity Mutagenicity","Germ cell mutagenicity");
			htHazardName.put("Carcinogenicity","Carcinogenicity");

			htHazardName.put("Reproductive","Reproductive toxicity");
			htHazardName.put("Developmental","Not implemented");
			
//			htHazardName.put("Neurotoxicity Single Exposure","");
//			htHazardName.put("Skin Irritation","");
			htHazardName.put("Acute Aquatic Toxicity","aquatic environment, acute hazard");
			htHazardName.put("Chronic Aquatic Toxicity","aquatic environment, long-term hazard");
			
			htHazardName.put("Neurotoxicity Repeat Exposure","Not implemented");
			htHazardName.put("Neurotoxicity Single Exposure","Not implemented");
			
			
			for (FlatFileRecord recordMine:recordsMine) {
				if (!recordMine.hazard_code.contains("H")) continue;
				if (recordMine.CAS.isEmpty()) continue;
				
				if (htHazardName.get(recordMine.hazard_name)==null) {
					System.out.println("Need "+recordMine.hazard_name);
					continue;
				} else if (htHazardName.get(recordMine.hazard_name).contains("Not implemented")) {
					continue;
				}
				
				String pubchemHazardName=htHazardName.get(recordMine.hazard_name);
				
//				System.out.println(recordMine.hazard_name+"\t"+pubchemHazardName);
				
				FlatFileRecord2 recordPubchem=this.getPubchemRecordByCAS(recordsPubchem,recordMine.CAS,pubchemHazardName,recordMine.hazard_code);
				
				if (recordPubchem!=null) {
					if (!recordPubchem.hazard_code.contentEquals(recordMine.hazard_code))					
						System.out.println(recordMine.CAS+"\t"+recordMine.hazard_name+"\t"+recordPubchem.hazard_code+"\t"+recordMine.hazard_code);
					
				} else {
//					System.out.println(recordMine.CAS+"\t"+recordMine.hazard_code+"\tNo Pubchem");
				}
				
			}
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	FlatFileRecord2 getPubchemRecordByCAS (Vector<FlatFileRecord2> recordsPubchem,String CAS,String HazardName,String HazardCode) {
		
		for (FlatFileRecord2 recordPubchem:recordsPubchem) {	
			if (!recordPubchem.hazard_name.contains(HazardName)) continue;
			String [] CASnumbers=recordPubchem.CAS.split(";");
			for (int i=0;i<CASnumbers.length;i++) {
				if (CASnumbers[i].trim().contentEquals(CAS)) {
					if (recordPubchem.hazard_code.contentEquals(HazardCode))
						return recordPubchem;
				}
			}
		}
		
		for (FlatFileRecord2 recordPubchem:recordsPubchem) {	
			if (!recordPubchem.hazard_name.contains(HazardName)) continue;
			String [] CASnumbers=recordPubchem.CAS.split(";");
			for (int i=0;i<CASnumbers.length;i++) {
				if (CASnumbers[i].trim().contentEquals(CAS)) {
					return recordPubchem;
				}
			}
		}

		
		return null;

	}
	void createSubsetFiles() {
		
		String folderPubchem="AA Dashboard\\Data\\pubchem";
		String filePathPubchem=folderPubchem+"\\CID-LCSS_2019_05_06-parsed.txt";

		Vector<FlatFileRecord2>recordsPubchem=FlatFileRecord2.loadRecordsFromFile(filePathPubchem, "CAS", "|");
		System.out.println("big pubchem loaded");

		String[] pubChemSources = { "NITE-CMC", "EU REGULATION (EC) No 1272/2008", "European Chemicals Agency (ECHA)",
				"HSDB","Hazardous Chemical Information System (HCIS), Safe Work Australia" };

		System.out.println("Source\tNumber of unique chemicals\tNumber of records");
		for (String pubChemSource:pubChemSources) {
			String filePathPubchemSource=folderPubchem+"\\CID-LCSS_2019_05_06-parsed_"+pubChemSource+".txt";
			filePathPubchemSource=filePathPubchemSource.replace("/", "_");
			FlatFileRecord2.writeToFile(recordsPubchem, filePathPubchemSource, "|",pubChemSource);
		}
		

	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParsePubChem p=new ParsePubChem();
		p.parseXML();
//		p.createSubsetFiles();
//		p.comparePubchemToMine();
	}

}
