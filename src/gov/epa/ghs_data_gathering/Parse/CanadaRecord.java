package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import gov.epa.ghs_data_gathering.Utilities.FileUtilities;

import org.jsoup.nodes.Element;


/**
 * 
 * This class downloads records from Canada and stores in CanadaRecord
 * 
 * Web pages last downloaded on 6/15/18
 * 
 * @author Todd Martin
 *
 */

public class CanadaRecord {

	String Name;//
	String CAS;//

	//********************************************************************
	String WHMIS_1988_Classification;
	String WHMIS_2015_Classification;
	String Hazard_Statement;
	String Hazard_Code;
	String Hazard_Comments;
	String WHMIS_1988_ClassificationNote;
	String WHMIS_1988_Classifiction_Comments;
	String WHMIS_2015_Classification_Note;
	//********************************************************************
	
	//More objected oriented approach
	Vector<Record1988>vec1988_Classification=new Vector<>();
	Vector<Record2015>vec2015_Classification=new Vector<>();



	class Record2015 {
		String classification;
		String hazardCode;
		String hazardStatement;
	}
	class Record1988 {
		String classification;
		Vector<String>hazardStatement=new Vector<>();
	}
	
	public static Vector<CanadaRecord> parseChemicalWebpagesInZipFile(String zipFilePath) {
		Vector<CanadaRecord> Canada_Records = new Vector<>();

		try {

			ZipFile zipFile = new ZipFile(zipFilePath);
			
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			ZipEntry zipEntry0 = entries.nextElement();//entry for folder, discard
			
			int counter=1;
			
			while (entries.hasMoreElements()) {
				
				if (counter%1000==0) System.out.println(counter);
//				if (counter==5) break;
				
                final ZipEntry zipEntry = entries.nextElement();
                
//                System.out.println(theString);

                Document doc=Jsoup.parse(zipFile.getInputStream(zipEntry),"utf-8","");
                
                String filename=zipEntry.getName().replace("Webpages/","");
                
//                if (!filename.equals("1,1,1,2-Tetrachloroethane.html")) continue;
//                if (!filename.equals("Acetic anhydride.html")) continue;
//                if (!filename.equals("Cellulose (paper fibers).html")) continue;
                
                
				CanadaRecord ar = createCanadaRecord(doc,filename);
				Canada_Records.add(ar);
				counter++;
			}
			return Canada_Records;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	private static CanadaRecord createCanadaRecord(Document doc,String filename) {

		CanadaRecord cr=new CanadaRecord();
		
		String msg=getNameCAS(doc, filename, cr);
		
		if (!msg.equals("OK")) {
			return null;
		}
		
		if (doc.select("table.table-horiz").size()>0) {
			get1988_Classification(doc, cr);
		} else {
			
			if (doc.select("h5").size()>0) {
				getUncontrolled(doc, cr);	
			} else {
				System.out.println("No uncontrolled:"+filename);
			}
			
		}

		if (doc.select("h5").size()>1) {
			get2015_Classification(doc, filename, cr);
		}
		
//		System.out.println(filename+"\t"+cr.Name+"\t"+cr.CAS);
		
//		GsonBuilder builder = new GsonBuilder();
//		builder.setPrettyPrinting();
//		Gson gson = builder.create();
//		System.out.println(filename+"\n"+gson.toJson(cr));
		
		// TODO Auto-generated method stub
		return cr;
	}

	private static void getUncontrolled(Document doc, CanadaRecord cr) {
		Element h5=doc.select("h5").get(0);
		if (h5.nextElementSibling().text().isEmpty()) h5=h5.nextElementSibling();
		while (true) {
			h5=h5.nextElementSibling();
			if (h5.text().isEmpty()) break;
			if (h5.text().contains("Uncontrolled product")) {
				Record1988 r=cr.new Record1988();
				r.classification=h5.text();
				cr.vec1988_Classification.add(r);
				break;
			}
		}
	}

	private static void get2015_Classification(Document doc, String filename, CanadaRecord cr) {
		
		try {
			Element h5=doc.select("h5").get(1);

			if (h5.nextElementSibling().text().isEmpty()) h5=h5.nextElementSibling();

			boolean haveNotDangerous=false;


			while (true) {
				h5=h5.nextElementSibling();
				if (h5.text().isEmpty()) break;
				//				System.out.println(h5.html());

				Record2015 r=cr.new Record2015();
				r.classification=h5.html().replace("<li>", "").replace("</li>","");

				if (r.classification.contains("<span")) {	
					r.classification=r.classification.substring(0,r.classification.indexOf("<span"));
					//TODO reference from span tag
				}

				cr.vec2015_Classification.add(r);

				if (r.classification.contains("Not a dangerous product")) {
					haveNotDangerous=true;
					break;
				}

				//			    if (r.classification.contains("Not reviewed")) continue;

			}

			//		if (cr.vec2015_Classification.size()==0) {
			//			System.out.println(filename);
			//			return cr;
			//		}

			if (haveNotDangerous) return;

//			if (h5.nextElementSibling().text().isEmpty()) h5=h5.nextElementSibling();
//
//			String nextText=h5.nextElementSibling().html();
//			if (nextText.contains("<p><font><strong>")) {
//				h5=h5.nextElementSibling();
//				
//				System.out.println("*"+h5.html());
//			}
			
			
			while(true) {
				h5=h5.nextElementSibling();
				if (h5==null) {
					System.out.println("Strong tag not found: "+filename);
					return;
				}
				if(h5.html().contains("<font><strong>")) break;
			}
			


			while (true) {
				h5=h5.nextElementSibling();

				//			System.out.println("*"+h5.html());

				if (h5.tagName().equals("p")) {

					String html=h5.html();
					
					if (html.contains("<font><strong>")) {
						break;
					}
					

					String [] vals=html.split("<br>");

					for (int i=0;i<vals.length;i++) {
						String val=vals[i].trim();

						if (val.contains("Not available")) continue;
						if (val.isEmpty()) continue;

						if (i>cr.vec2015_Classification.size()-1) {
							System.out.println(filename+"\ti>cr.vec2015_Classification.size()-1\t"+val);
							break;
						}

//						System.out.println(i+"\t"+cr.vec2015_Classification.size());
//						System.out.println("i="+i+"\t"+cr.vec2015_Classification.size());
						Record2015 r=cr.vec2015_Classification.get(i);

						if (val.contains("(")) {
							r.hazardStatement=val.substring(0, val.indexOf(" ("));
							r.hazardCode=val.substring(val.indexOf("(")+1,val.indexOf(")"));
						} else {
							
							if (val.contains("Comments")) {
								cr.WHMIS_2015_Classification_Note=val;
							} else if (val.contains("Ingredient disclosure") || val.contains("<a href")){
								//do nothing
							} else {
								r.hazardStatement=val;
//								System.out.println("*"+cr.CAS+"\t"+val);
							}
							
							
//							r.hazardStatement=val;
						}
					}


					break;
				}
			}

			for (int i=0;i<cr.vec2015_Classification.size();i++) {
				Record2015 r=cr.vec2015_Classification.get(i);

				if (r.classification.contains("Not reviewed")) {
					cr.vec2015_Classification.remove(i--);
				}
			}


			for (int i=1;i<=10;i++) {
				h5=h5.nextElementSibling();
				if (h5==null) break;
				if (h5.text().contains("Comments")) {
					cr.WHMIS_2015_Classification_Note=h5.text();
					//					System.out.println(h5.text());
					break;
				}
			}

		} catch (Exception ex) {
			System.out.println(filename);
			ex.printStackTrace();
		}
	}

	private static void get1988_Classification(Document doc, CanadaRecord cr) {
		Element table=doc.select("table.table-horiz").get(0);
		Elements rows = table.select("tr");

		for (int i = 0; i < rows.size(); i++) { //first row is the col names so skip it.
		    Element row = rows.get(i);
		    
		    Element th = row.select("th").get(0);
		    
		    Record1988 r=cr.new Record1988();
		    r.classification=th.text();
		    
		    Elements cols = row.select("td");
		    Elements lis = cols.select("li");
		    
		    for (int j=0;j<lis.size();j++) {
		    	Element ej=lis.get(j);
		    	r.hazardStatement.add(ej.text());
		    }
		    cr.vec1988_Classification.add(r);

		}

		cr.WHMIS_1988_Classifiction_Comments=table.nextSibling().nextSibling().toString().replace("<p>", "").replace("</p>", "");
	}

	private static String getNameCAS(Document doc, String filename, CanadaRecord cr) {
		
		try {
		Elements elementsName=doc.select("div.fiche");
		Elements elementsName2=elementsName.select("h2");
		
		cr.Name=elementsName2.get(0).text();
		
		if (cr.Name.equals("Substance not found")) return "Substance not found";
		
		if (doc.select("p.fiche-id").size()>0) {
			cr.CAS=doc.select("p.fiche-id").get(0).text().replace("CAS Number : ", "").trim();		
		}
		return "OK";
		
		} catch (Exception ex) {
			System.out.println(filename);
			ex.printStackTrace();
			return ex.getMessage();
		}
	}
	
	public static void downloadIndexPages() {
		
		String [] letters= {"a","b","c","d","e-f-g","h","i-j-k-l","m","n-o","p","q-r-s","t","u-v-w-x-y-z"};
		
		for (String letter:letters) {
			
			
			String url="http://www.csst.qc.ca/en/prevention/reptox/Pages/list-whmis-2015-"+letter+".aspx";
	
			if (letter.equals("n-o") || letter.equals("p") || letter.equals("q-r-s")) {
				//Canada was dyslexic:
				url="http://www.csst.qc.ca/en/prevention/reptox/Pages/list-whmis-2105-"+letter+".aspx";
			}

			
			File f=new File("AA Dashboard\\Data\\Canada\\index pages\\"+letter+".html");
			
			if (f.exists()) continue;
			
			FileUtilities.downloadFile(url, f.getAbsolutePath());
			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	static void downloadAllChemicals(String folder,String destFolder) {
		
		File Folder=new File(folder);
		
		File [] files=Folder.listFiles();
		
		for (File file:files) {
			downloadChemicals(file.getAbsolutePath(), destFolder);
		}
		
	}
	
	
	static void downloadChemicals(String filepath,String destFolder) {
		
		try {
			Document doc=Jsoup.parse(new File(filepath),"utf-8","");
			
			Elements elements=doc.select("dl.search-results");
			
			Elements elementsDT=elements.select("dt");
			Elements elementsDD=elements.select("dd");
			
//			System.out.println(elements.text());
			
			for (int i=0;i<elementsDT.size();i++) {
				
				
				Element link = elementsDT.get(i).select("a").first();
				String linkHref = "http://www.csst.qc.ca/"+link.attr("href"); // "http://example.com/"
				
				String name=elementsDT.get(i).text();
				
				
//				System.out.println(elementsDD.get(i).text());
				
				
				String CAS=elementsDD.get(i).text().trim();
				
				if (CAS.contains("CAS number :")) {
					CAS=CAS.replace("CAS number :", "").trim();
					
					if (CAS.contains("UN number :")) {
						CAS=CAS.substring(0, CAS.indexOf("UN")).trim();
					}
					
				}	else if (CAS.contains("UN number :")) CAS=CAS.replace("UN number :", "").trim();
				else {
//					System.out.println("*"+CAS);
				}
				
				
				
				String outputFileName="";
				
				if (CAS.isEmpty()) {
					outputFileName=name;
				} else {
					outputFileName=CAS;
				}
				
				
				System.out.println(outputFileName);
				
				File f=new File("AA Dashboard\\Data\\Canada\\Webpages\\"+outputFileName+".html");
				
//				System.out.println(name+"\t"+CAS+"\t"+linkHref);
				
				if (f.exists()) continue;
				
				FileUtilities.downloadFile(linkHref, f.getAbsolutePath());
				
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	public static void main(String[] args) {
//		CanadaRecord.downloadIndexPages();
//		CanadaRecord.downloadChemicals("AA Dashboard\\Data\\Canada\\index pages\\a.html", "AA Dashboard\\Data\\Canada\\Webpages");
		CanadaRecord.downloadAllChemicals("AA Dashboard\\Data\\Canada\\index pages", "AA Dashboard\\Data\\Canada\\Webpages");
	}

}
