package gov.epa.ghs_data_gathering.Parse;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.ghs_data_gathering.Utilities.FileUtilities;
import gov.epa.ghs_data_gathering.API.AADashboard;
import gov.epa.ghs_data_gathering.API.HazardRecord;

import java.util.ArrayList;


/**
 * Japan records from webpages
 * 
 * @author TMARTI02
 *
 */

public class JapanRecord {
	String chemicalName;
	String CAS;
	String ID;
	String classifier;
	String yearClassified;
	String referenceManual;
	String year;
	String new_revised;
	String fileName;
	
	ArrayList<HazardRecord>records=new ArrayList<>();
	static Multimap<String, String> dictHazardNameToScoreName = CodeDictionary.populateJapanHazardClassToScoreName();
	
	
	public static String getToxRoute(HazardRecord hr, String scoreName) {
		String toxRoute=CodeDictionary.getRouteFromScoreName(scoreName);
		
		if (hr.hazardClass.toLowerCase().contains("inhalation")) {
			toxRoute=hr.hazardClass.substring(hr.hazardClass.indexOf("(")+1,hr.hazardClass.indexOf(")"));
		}
		return toxRoute;
	}


	
	
	/**
	 * Japan incorrectly assigned several ratings of "Not Classifiable" as "Not
	 * classified" which means not toxic, this method determines whether a record should be classifiable or not
	 * based on the cancer classification from IARC or ACGIH
	 * 
	 * @param CAS
	 * @param classification
	 * @param rationale
	 * @return
	 */
	public static boolean isClassifiable(String CAS, String rationale) {

		String r = rationale.toLowerCase().trim();
		
		String[] strCase1 = { "group 3", "classified into 3", "category 3", "three of iarc", "class 3",
				"classificationed into \"3", "category of iarc (2001): 3" };
		
		boolean case1 = false;
		for (int i = 0; i < strCase1.length; i++) {
			if (r.contains(strCase1[i]) && r.contains("iarc")) {
				case1 = true;
				break;
			}
		}

		boolean case2 = r.contains("a4") && r.contains("acgih");

		boolean case3 = false;

		String[] strCase3 = { "according to epa with i", "classified into i", "rated as d", "rated d", "category d",
				"group d", "classified into d", "rated as class d" };

		for (int i = 0; i < strCase3.length; i++) {
			if (r.contains(strCase3[i]) && r.contains("epa")) {
				case3 = true;
				break;
			}
		}

		if (case1 || case2 || case3) {
			return false;
		} else {
			// System.out.println(CAS + "\t" + r);
			return true;
		}

	}
	
	private void getNameCAS(JapanRecord jr, Document doc) {
		Element div = doc.select("div").get(1);
		Elements bolds = div.select("B");
		
		for (Element bold:bolds) {
			Element next=bold.nextElementSibling();
			//TODO- might be better to just parse the file line by line and look for "<B>CAS"
			if (bold.text().equals("GHS Classification Result")) {
				String h=next.html();
				
				if (h.contains("</b>")) {
					jr.chemicalName=h.substring(h.indexOf("</b>")+4, h.length());
					jr.chemicalName=jr.chemicalName.substring(0,jr.chemicalName.indexOf("<br>"));
					jr.CAS=h.substring(h.indexOf("<br>")+4,h.length());
					jr.CAS=jr.CAS.substring(jr.CAS.indexOf("</b>")+4, jr.CAS.indexOf("<br>"));
				} else {
					System.out.println(h);
				}
//					System.out.println(jr.chemicalName);	
//					System.out.println(jr.CAS);
			}
		}
	}

	
	private static void getClassificationMetadata(JapanRecord jr, Element table) {
		Elements trs = table.select("tr");

		for (Element tr:trs) {
			Elements tds = tr.select("td");
			String field=tds.get(0).text();
			if (field.equals("ID:")) {
				jr.ID=tds.get(1).text();
			} else if (field.equals("Classifier:")) {
				jr.classifier=tds.get(1).text();
			} else if (field.equals("Year Classified:")) {
				jr.yearClassified=tds.get(1).text();
			} else if (field.equals("Reference Manual:")) {
				jr.referenceManual=tds.get(1).text().replace("（", "(").replace("）", ")");
			} 
		}
	}

	private static void parseDocument(String filename,JapanRecord jr, Document doc,String format) {
		//			System.out.println(doc.text());

		jr.fileName=filename;
		Elements tables = doc.select("TABLE");

		//				System.out.println(format);
		
		if (format.equals("old")) {
			for (Element table:tables) {
				//TODO- need better way to locate ID table
				if (table.hasAttr("style") && table.attr("style").equals("font-size : 10pt")) {
					getClassificationMetadata(jr, table);
				} else if (table.attr("class").contains("Rstable")) {
					getHazardData(filename,jr, table);
				}
			}
		} else if (format.equals("new")) {

			Elements tables1 = tables.select("table.rstable1");

			Element generalInformation=tables1.get(0);
			getGeneralInformation(filename, jr, generalInformation);


			Element referenceInformation=tables1.get(1);

			Element physicalHazards=tables1.get(2);

			getHazardData(filename,jr, physicalHazards);


			Element healthHazards = tables.select("table.rstable2").first();
			getHazardData(filename,jr, healthHazards);

		} else {
			System.out.println(filename+"\tunknown format");
		}

	}

	public static void downloadWebpages() {
		String sourceName="Japan";
		String mainFolder = AADashboard.dataFolder + File.separator + sourceName;
		String folderNameWebpages = "web pages";

		//Download webpage at https://www.nite.go.jp/chem/english/ghs/all_fy_e.html ==> GHS Classification Results.html 		
		File indexFile=new File(mainFolder+"/GHS Classification Results.html");

		String destFolder=mainFolder+File.separator+folderNameWebpages;

		try {
			
			FileWriter fw=new FileWriter(mainFolder+"/links.txt");
			Document doc = Jsoup.parse(indexFile, "utf-8");
			Element table = doc.select("table.tblghs02").first();
			Elements trs = table.select("tr");

			for (int i=1;i<=3;i++) trs.remove(0);

			for (Element tr:trs) {
				if (tr.hasAttr("bgcolor") && tr.attr("bgcolor").equals("gray")) continue;
				Elements as = tr.select("a");

				for (Element a:as) {
					if (a.attr("href").contains(".html")) {

						String url=a.attr("href");
						String fileName = url.substring( url.lastIndexOf("/")+1, url.length() );
						System.out.println(fileName);

						String destFilePath = destFolder + "/" + fileName;
						File destFile = new File(destFilePath);

						//								System.out.println(destFilePath);

						if (destFile.exists()) continue;

						FileUtilities.downloadFile(url, destFilePath);
						Thread.sleep(3000);
						fw.write(a.attr("href")+"\r\n");
						fw.flush();
					}
				}
			}

			FileOutputStream fos = new FileOutputStream(mainFolder+"/web pages.zip"); 
			ZipOutputStream zipOS = new ZipOutputStream(fos); 
			
			File webPageFolder=new File(mainFolder+"/"+folderNameWebpages);
			File [] files=webPageFolder.listFiles();
			
			//Create a zip file
			for (File file:files) {
				if (file.getName().contains(".html")); {
					FileUtilities.writeToZipFile(file.getName(),destFolder,folderNameWebpages, zipOS);
//					file.delete();//could delete to avoid having lots of files on drive
				}
			}
			//delete web pages folder once you are safe ok to do so.
			
			zipOS.close();
            fos.close();
			
            fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}


	}


	public static String fixSpecialCharacters(String str) {
		str=str.replace("（", "(").replace("）", ")");
		return str;
	}


	private static void getHazardData(String filename,JapanRecord jr, Element table) {
		Elements trs = table.select("tr");
		Element header=trs.remove(0);//header- should probably determine column numbers for each column name but for now hard code the column numbers

		Elements hElements=header.getAllElements();

		Hashtable<String,Integer>cols=new Hashtable<>();

		for (int i=0;i<hElements.size();i++) {

			if (hElements.get(i).text().isEmpty()) {//weird bug-sometimes have blank header column
				hElements.remove(i);
				i--;
			} else {
				cols.put(hElements.get(i).text(),new Integer(i));
				//						System.out.println(i+"\t"+hElements.get(i).text());
			}
		}

		for (Element tr:trs) {
			Elements tds = tr.select("td");

			HazardRecord hr=new HazardRecord();

			String strHazardClass=tds.get(cols.get("Hazard class")).text();
			hr.hazardClass=fixSpecialCharacters(strHazardClass);

			if (dictHazardNameToScoreName.get(hr.hazardClass)==null) {
				System.out.println(hr.hazardClass+"\tnull");
				continue;
			} else {
				List<String>list=(List<String>)dictHazardNameToScoreName.get(hr.hazardClass);
				if (list.contains("omit")) continue;
			}

			
			String strClassification=tds.get(cols.get("Classification")).html();
			
//			if (jr.CAS.equals("87-86-5")) {
//				System.out.println(strClassification);
//			}
			
//			if (jr.CAS.equals("133220-30-1")) {
//				System.out.println(strClassification);
//			}
			
			
			String [] classifications=null;
			
			if (strClassification.contains(", Category")) {
				strClassification=strClassification.replace(", Category", "\nCategory");
				strClassification=strClassification.replace("<br>", "");
				classifications=strClassification.split("\n");
			} else if (strClassification.contains("<br>")) {
				classifications=strClassification.split("<br>");
			} else {
				classifications=new String [1];
				classifications[0]=strClassification;
			}
			
//			if (classifications==null) {
//				System.out.println(strClassification);
//			}
			
			
			for (String classification:classifications) {
				hr.classifications.add(classification);
//				if (jr.CAS.equals("100-69-6")) {
//					System.out.println(classification);
//				}
				
			}

			//Japan web pages arent consistent!!!
			if (cols.get("Symbol")!=null) {
				hr.symbol=tds.get(cols.get("Symbol")).text();
				hr.signalWord=tds.get(cols.get("Signal word")).text();
			} else if  (cols.get("Symbol Signal word")!=null) {
				hr.signalWord=tds.get(cols.get("Symbol Signal word")).text();
			} else if (cols.get("Pictogram (Code: symbol) Signal word")!=null) {
				hr.signalWord=tds.get(cols.get("Pictogram (Code: symbol) Signal word")).text();
			} else if (cols.get("Pictogram Signal word")!=null) {
				hr.signalWord=tds.get(cols.get("Pictogram Signal word")).text();
			} else {
				System.out.println("Unknown symbol col name for " +filename);
			}


			Element strHazardStatement=null;
			if (cols.get("Hazard statement")!=null)  {
				strHazardStatement=tds.get(cols.get("Hazard statement"));
			} else if (cols.get("Hazard statement (code)")!=null) {
				strHazardStatement=tds.get(cols.get("Hazard statement (code)"));
			} else if (cols.get("Code (Hazard statement)")!=null) {
				strHazardStatement=tds.get(cols.get("Code (Hazard statement)"));
			} else {
				System.out.println("Unknown hazard statement col name for " +filename);
			}

			parseHazardStatementAndCode(filename,jr,hr, strHazardStatement);

			if (cols.get("Precautionary statement")!=null) {
				hr.precautionaryStatement=tds.get(cols.get("Precautionary statement")).text();	
			}else if (cols.get("Precautionary statement (code)")!=null) {
				hr.precautionaryStatement=tds.get(cols.get("Precautionary statement (code)")).text();	
			} else if (cols.get("Code (Precautionary statement)")!=null) {
				hr.precautionaryStatement=tds.get(cols.get("Code (Precautionary statement)")).text();
			} else {
				System.out.println("Unknown precautionary statement col name for " +filename);
			}

			//					System.out.println(filename+"\t"+cols.get("Rationale for the classification")+"\t"+hElements.size()+"\t"+tds.size());
			hr.rationale=tds.get(cols.get("Rationale for the classification")).text();
			if (hr.rationale.equals(" -")) hr.rationale="-";
			if (hr.rationale.equals("  -")) hr.rationale="-";
			jr.records.add(hr);
		}
	}

	private static void parseHazardStatementAndCode(String filename,JapanRecord jr,HazardRecord hr, Element str) {

		String [] vals=str.html().split("<br>");

		for (String val:vals) {
			//					System.out.println(val);

			String hazardCode=null;


			if (val.contains("span")) {

				String hazardStatement=val.substring(val.indexOf("\"")+1,val.lastIndexOf("\""));

				if (hazardStatement.contains(":")) {
					hazardStatement=hazardStatement.substring(hazardStatement.indexOf(":")+2,hazardStatement.length());
				}

				hr.hazardStatements.add(hazardStatement);

				hazardCode=val.substring(val.indexOf(">")+1,val.lastIndexOf("<"));

				if (!hazardCode.isEmpty())								
					hr.hazardCodes.add(hazardCode);

				//						System.out.println(jr.CAS+"\t"+val);				

			} else if (val.contains(":")) {//split into hazard code and statement:
				try {
					hazardCode=val.substring(0,val.indexOf(":")).trim();

					if (!hazardCode.isEmpty())
						hr.hazardCodes.add(hazardCode);

					if (val.indexOf(":")<val.length()-1) {
						String strHazardStatement=val.substring(val.indexOf(":")+2, val.length()).trim();
						hr.hazardStatements.add(strHazardStatement);
					} else {
						hr.hazardStatements.add("-");
					}

				} catch (Exception ex) {
					System.out.println("***"+val);
				}
			} else {

				if (val.contains("H")) {
					hr.hazardCodes.add(val.trim());
					hr.hazardStatements.add("-");
				} else if (val.equals("-")){
					hr.hazardStatements.add(val);
				} else {
					System.out.println(val+"\thazardstatement???");
				}
			}

		}

	}

	private static void getGeneralInformation (String filename,JapanRecord jr, Element table) {

		Elements trs = table.select("tr");
		trs.remove(0);//header- should probably determine column numbers for each column name but for now hard code the column numbers

		for (Element tr:trs) {
			Elements tds = tr.select("td");

			String field=tds.get(0).text();
			String value=tds.get(1).text();

			if (field.equals("CAS number") || field.equals("CAS RN")) jr.CAS=value;
			if (field.equals("Chemical name") || field.equals("Chemical Name")) jr.chemicalName=value;
			if (field.equals("Substance ID")) jr.ID=value;
			if (field.equals("Classifier(s) (Ministries)") || field.equals("Ministry who conducted the classification")) jr.classifier=value;
			if (field.equals("Fiscal year of classification conducted") || field.equals("Classification year (FY)")) jr.year=value;
			if (field.equals("New/Revised")) jr.new_revised=value;

			//					System.out.println(field+"\t"+value);
		}
	}

	JapanRecord parseWebpage(File file) {
		JapanRecord jr=new JapanRecord();

		try {

			BufferedReader br=new BufferedReader(new FileReader(file));

			String format=getFormat(jr,br,file.getName());
			//					System.out.println(file.getName()+"\t"+format);

			//					System.out.println(file.exists());
			Document doc = Jsoup.parse(file, "Shift_JIS");

			parseDocument(file.getName(),jr, doc,format);
			
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();

			
			System.out.println(gson.toJson(jr));
			//					System.out.println(div.html());

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return jr;
	}

	private Vector<JapanRecord> parseChemicalWebpages(String htmlFolder) {

		Vector<JapanRecord> records = new Vector<>();

		try {
			File fileFolder = new File(htmlFolder);
			File[] files = fileFolder.listFiles();

			for (int j = 0; j < files.length; j++) {

				if (files[j].getName().indexOf(".html") == -1)
					continue;

				if (j % 100 == 0)
					System.out.println(j);

				JapanRecord jr = parseWebpage(files[j]);
				records.add(jr);

			}
			return records;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static JapanRecord parseZipWebpage(ZipFile zipFile, final ZipEntry zipEntry)
			throws IOException, UnsupportedEncodingException {
		JapanRecord jr=new JapanRecord();

		InputStream input = zipFile.getInputStream(zipEntry);
		BufferedReader br = new BufferedReader(new InputStreamReader(input, "Shift_JIS"));

		String format=getFormat(jr,br,zipEntry.getName());
		
		if (format.contentEquals("maintenance")) {
			System.out.println(zipEntry.getName());
			return null;
		}
		
		//		    			System.out.println(file.getName()+"\t"+format);

		//		                System.out.println(theString);
		Document doc = Jsoup.parse(zipFile.getInputStream(zipEntry),"Shift_JIS","");

		String filename=zipEntry.getName().replace("web pages/", "");

		//				System.out.println(filename);

		parseDocument(filename,jr, doc,format);
		return jr;
	}

	public static String getFormat (JapanRecord jr,BufferedReader br,String filename) {

		try {

			while (true) {
				String Line=br.readLine();
				
				if (Line.contains("Our website is currently unavailable due to maintenance")) return "maintenance";
				
//				System.out.println(Line);
				
				if (Line==null) break;

				if (Line.contains("<B>Chemical Name")) {

					try {
						jr.chemicalName=Line.substring(Line.indexOf("</B>")+4, Line.length()).replace("<BR>","");

						Line=br.readLine();
						if (Line.equals("<BR>")) Line=br.readLine();						
						jr.CAS=Line.substring(Line.indexOf("</B>")+4, Line.length()).replace("<BR>","");

						if (jr.CAS.equals(">")) {
							System.out.println(Line);
						}

						//								System.out.println(jr.chemicalName+"\t"+jr.CAS);
						return "old";
					} catch (Exception ex1) {
						System.out.println(filename+"\t"+Line);
					}
				}

				if (Line.contains("<B>GENERAL INFORMATION")) {
					return "new";
				}
			}

			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;

	}

	public static Vector<JapanRecord> parseChemicalWebpagesInZipFile(String zipFilePath) {
		Vector<JapanRecord> records = new Vector<>();

		try {

			ZipFile zipFile = new ZipFile(zipFilePath);

			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			ZipEntry zipEntry0 = entries.nextElement();//entry for folder, discard
			int counter=1;

			while (entries.hasMoreElements()) {

				if (counter%1000==0) System.out.println(counter);

				//						if (counter==5) break;
				final ZipEntry zipEntry = entries.nextElement();
//                System.out.println(zipEntry.getName());

				// if (!zipEntry.getName().equals("web pages/14-mhlw-2001e.html")) continue;


				JapanRecord jr = parseZipWebpage(zipFile, zipEntry);
				//System.out.println(inputFile.getName() + "\t" + ar.CasNumber);

				if (jr!=null) {
					records.add(jr);
					counter++;
				} else {
					File badFile=new File("AA Dashboard/Data/Japan/"+zipEntry.getName());
					if (badFile.exists()) badFile.delete();
				}
			}
			return records;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JapanRecord.downloadWebpages();
	}

}






