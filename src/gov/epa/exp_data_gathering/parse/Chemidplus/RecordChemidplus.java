package gov.epa.exp_data_gathering.parse.Chemidplus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.Random;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.api.RawDataRecord;
import gov.epa.database.SQLite_CreateTable;
import gov.epa.database.SQLite_GetRecords;
import gov.epa.database.SQLite_Utilities;
import gov.epa.ghs_data_gathering.Utilities.FileUtilities;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class RecordChemidplus {

	String Molecular_Formula;
	String Molecular_Weight;

	ArrayList<String> ClassificationCodes = new ArrayList<>();
	ArrayList<String> SuperlistClassificationCodes = new ArrayList<>();

	String NameOfSubstance;
	ArrayList<String> Synonyms = new ArrayList<>();
	ArrayList<String> SystematicNames = new ArrayList<>();
	ArrayList<String> SuperListNames = new ArrayList<>();

	String CASRegistryNumber;
	ArrayList<String> OtherRegistryNumbers = new ArrayList<>();

	String Inchi;
	String InchiKey;
	String Smiles;

	ArrayList<ToxicityRecord> ToxicityRecords = new ArrayList<>();
	ArrayList<PhysicalPropertyRecord> PhysicalProperties = new ArrayList<>();

	String url;
	String fileName;
	String date_accessed;

	public String toJSON() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();// makes it multiline and readable
		Gson gson = builder.create();
		return gson.toJson(this);//all in one line!
	}

	class PhysicalPropertyRecord {
		String PhysicalProperty;
		String Value;
		String Units;
		String TempdegC;
		String Source;
	}
	
	public class ToxicityRecord {
		String Organism;
		String TestType;
		String Route;
		public String ReportedDose;
		public String NormalizedDose;// appears below ReportDose in ()
		String Effect;
		String Source;
	}

		
	class ParseChemidplusElements {

		public void createRecordChemidplus(RecordChemidplus cr,Document doc) {
			
			Elements innerBody = doc.select("div.innerMain");

			// for molecular weight and formula
			//contained in the chemical "summary" section of html
			Elements summaryOfChemical = innerBody.select("div#summary");
			parseMolecularFormulaAndWeight(cr, summaryOfChemical);

			// contains all necessary contents
			Elements divTabs = innerBody.select("div#tabs");

			// checking if section exists
			Elements headerTags = divTabs.select("h1, h2, h3, h4, h5, h6");

			parseWebpage(cr, divTabs, headerTags, innerBody);
			
		}

		
		/*
		 * main method that executes
		 * parse methods
		 */
		private void parseWebpage(RecordChemidplus cr, Elements divTabs, Elements headerTags, Elements innerBody) {

			// if the headers contain the following strings
			// they will execute parse method
			
			
			if (headerTags.text().contains("Classification Codes")) {
				parseClassificationCodes(cr, divTabs);
			}

			if (headerTags.text().contains("Name of Substance")) {
				parseName(cr, divTabs);
			} else if (innerBody.text().contains("Substance Name")) {
				String name = innerBody.select("h1").first().text();

				if (name.indexOf("RN:")>-1)
					name = name.substring(16, name.indexOf("RN:")).trim();
				else {
					try {
						name=name.substring(name.indexOf(":")+1,name.indexOf("ID:")).trim();
//							System.out.println("Couldnt trim name:"+cr.url+", name="+name);
					} catch (Exception ex) {
						System.out.println("Couldnt trim name:"+cr.url+", name="+name);
					}
				}

				cr.NameOfSubstance = name;
			}

			if (headerTags.text().contains("Synonyms")) {
				parseSynonyms(cr, divTabs);
			}

			if (headerTags.text().contains("Systematic Names")) {
				parseSystematicNames(cr, divTabs);
			}

			if (headerTags.text().contains("Superlist Names")) {
				parseSuperlistNames(cr, divTabs);
			}

			if (headerTags.text().contains("CAS Registry Number")) {
				parseCAS(cr, divTabs);
			}

			if (headerTags.text().contains("Other Registry Numbers")) {
				parseOtherRegistryNumbers(cr, divTabs);
			}

			
			if (headerTags.text().contains("InChI") || headerTags.text().contains("Smiles")) {
				parseInchiAndSmiles(cr, divTabs);
			}

			if (headerTags.text().contains("Toxicity")) {
				parseToxicityTable(cr, divTabs);
			}

			if (headerTags.text().contains("Physical Properties")) {
				parsePhysicalPropertiesTable(cr, divTabs);
			}
		}

		private void parseSuperlistNames(RecordChemidplus cr, Elements divTabs) {
			Elements headers = divTabs.select("h3");

			Element superlistBlock = null;

			for (Element e : headers) {
				if (e.text().equals("Superlist Names")) {
					superlistBlock = e.nextElementSibling();
				}
			}

			Elements innerElements = superlistBlock.select("div ul li div");

			for (Element e : innerElements) {
				cr.SuperListNames.add(e.text());
			}
		}

		private void parseSystematicNames(RecordChemidplus cr, Elements divTabs) {
			// > selects base child elements
			Elements headers = divTabs.select("h3");

			Element systematicBlock = null;

			for (Element e : headers) {
				if (e.text().equals("Systematic Names") || e.text().equals("Systematic Name")) {
					systematicBlock = e.nextElementSibling();
				}
			}

			// Element systematicNames = divTabs.select("div#names div.ds >
			// div[class]").get(1);

			Elements innerElements = systematicBlock.select("ul");

			for (Element e : innerElements) {
				cr.SystematicNames.add(e.text());
			}
		}

		private void parseSynonyms(RecordChemidplus cr, Elements divTabs) {
			// Element synonyms = divTabs.select("div#names div.ds div[class]").get(0);
			// try catch error block?
			Elements headers = divTabs.select("h3");

			Element synonymBlock = null;

			for (Element e : headers) {
				if (e.text().equals("Synonyms") || e.text().equals("Synonym")) {
					synonymBlock = e.nextElementSibling();
				}
			}
			// System.out.println(synonymBlock);

			if (synonymBlock != null) {
				Elements innerLists = synonymBlock.select("div ul li div");

				for (Element e : innerLists) {
					cr.Synonyms.add(e.text());
				}
			}
		}

		private void parseName(RecordChemidplus cr, Elements divTabs) {
		    String name = divTabs.select("div#names div.ds ul li div").first().text();

			cr.NameOfSubstance = name;
		}
		
		private void parseClassificationCodes(RecordChemidplus cr, Elements divTabs) {
			Elements classifications = divTabs.select("div[id~=classifications$] h3");

			for (Element element : classifications) {
				StringBuilder sb = new StringBuilder(element.toString());

				Element next = element.nextElementSibling();
				while (next != null && !next.tagName().startsWith("h")) {
					sb.append(next.toString()).append("\n");
					String temp = sb.toString();
					// System.out.println(temp);
					if (temp.contains("<h3>Classification Codes</h3>")) {
						Elements classificationData = next.select("div div div");
						for (Element e : classificationData) {
							// System.out.println(e.text());
							cr.ClassificationCodes.add(e.text());
						}

					} else if (temp.contains("<h3>Superlist Classification Codes</h3>")) {
						Elements superClassificationData = next.select("div div div");
						for (Element e : superClassificationData) {
							// System.out.println(e.text());
							cr.SuperlistClassificationCodes.add(e.text());
						}
					}
					next = next.nextElementSibling();
				}
			}
		}

		private void parseCAS(RecordChemidplus cr, Elements divTabs) {
			String CAS = divTabs.select("div#numbers div.ds > ul li div").get(0).text();
		
			cr.CASRegistryNumber = CAS;
		}

		private void parseInchiAndSmiles(RecordChemidplus cr, Elements divTabs) {
			Elements inchiAndSmiles = divTabs.select("div#structureDescs div");
			
		
			for (int i = 0; i < inchiAndSmiles.size(); i++) {
				String header = inchiAndSmiles.get(i).select("h3").text();
				if (header.equals("InChI")) {
					cr.Inchi = inchiAndSmiles.get(i).ownText();
				} else if (header.equals("InChIKey")) {
					cr.InchiKey = inchiAndSmiles.get(i).ownText();
				} else if (header.equals("Smiles")) {
					cr.Smiles = inchiAndSmiles.get(i).ownText();
				}
		
			}
		}

		private void parseMolecularFormulaAndWeight(RecordChemidplus cr, Elements summary) {
			Elements headers = summary.select("div[class] > div div div h2");
		
			// loop through all headers(h2) within the summary tag
			for (int i = 0; i < headers.size(); i++) {
				String header = headers.get(i).select("h2").text();
		
				if (header.equals("Molecular Formula")) {
					cr.Molecular_Formula = headers.get(i).siblingElements().first().text();
				} else if (header.equals("Molecular Weight")) {
					cr.Molecular_Weight = headers.get(i).siblingElements().text();
				}
			}
		
		}

		private void parseOtherRegistryNumbers(RecordChemidplus cr, Elements divTabs) {
			Elements otherRegistryNumbers = divTabs.select("div#numbers div.ds > div[class] div ul li div");
		
			for (Element e : otherRegistryNumbers) {
				cr.OtherRegistryNumbers.add(e.text());
			}
		}

		private void parsePhysicalPropertiesTable(RecordChemidplus cr, Elements divTabs) {
			Elements physicalTable = divTabs.select("div#physical tr");
		
			for (int i = 1; i < physicalTable.size(); i++) {
				Element table = physicalTable.get(i);
				Elements tableRows = table.select("td");
		
				PhysicalPropertyRecord pr = cr.new PhysicalPropertyRecord();
		
				for (int rowIndex = 0; rowIndex < tableRows.size(); rowIndex++) {
					String row = tableRows.get(rowIndex).text();
		
					switch (rowIndex) {
					case 0:
						pr.PhysicalProperty = row;
						break;
					case 1:
						pr.Value = row;
						break;
					case 2:
						pr.Units = row;
						break;
					case 3:
						pr.TempdegC = row;
						break;
					case 4:
						pr.Source = row;
						break;
					}
		
				}
				cr.PhysicalProperties.add(pr);
			}
		}

		@SuppressWarnings("unused")
		private void parseToxicityTable(RecordChemidplus cr, Elements divTabs) {
			Elements toxicityTable = divTabs.select("div#toxicity tr");
		
			
			for (int i = 1; i < toxicityTable.size(); i++) {
				Element tableRow = toxicityTable.get(i);
				Elements tableColumnCells = tableRow.select("td");
		
				ToxicityRecord tr = cr.new ToxicityRecord();
		
				tr.Organism = tableColumnCells.get(0).text().trim();
				tr.TestType = tableColumnCells.get(1).text().trim();
				tr.Route = tableColumnCells.get(2).text().trim();
		
				String reportedAndNormalizedDoseColumn = tableColumnCells.get(3).text().trim();
		
				if (reportedAndNormalizedDoseColumn.contains("(")) {
		
					// 790mg(base)/k (790mg/kg)
					// tr.ReportedDose = row.substring(0, row.indexOf(" ")).trim();
					// tr.NormalizedDose = row.substring(row.indexOf("(") + 1, row.length() - 1);
		
					int countSpaces = 0;
					int indexLastSpace = -1;
		
					for (int j = 0; j < reportedAndNormalizedDoseColumn.length(); j++) {
						if (reportedAndNormalizedDoseColumn.substring(j, j + 1).equals(" ")) {
							countSpaces++;
							indexLastSpace = j;
						}
					}
		
					tr.ReportedDose = reportedAndNormalizedDoseColumn.substring(0, indexLastSpace).trim();
					tr.NormalizedDose = reportedAndNormalizedDoseColumn.substring(indexLastSpace + 2,
							reportedAndNormalizedDoseColumn.length() - 1);
		
					// System.out.println(tr.ReportedDose+"\t"+tr.NormalizedDose);
		
					// System.out.println(tr.ReportedDose);
				} else
					tr.ReportedDose = tableColumnCells.get(3).text().trim();
		
				tr.Effect = tableColumnCells.get(4).text().trim();
				tr.Source = tableColumnCells.get(5).text().trim();
		
				cr.ToxicityRecords.add(tr);
			}
		
		}
		
		
	}
	
	

	static void getChemidplusSearchResult (String destPath,String testType,String route) {
		
		
		try {
						
			HttpResponse<String> response = Unirest.get("https://chem.nlm.nih.gov/chemidplus/ProxyServlet?"
					+ "objectHandle=Search&actionHandle=searchChemIdLite&"
					+ "nextPage=jsp%2Fchemidheavy%2FChemidDataview.jsp&"
					+ "responseHandle=JSP&QV8="+testType+"&QF8=ToxTestType&QV7="+route+"&"
					+ "QF7=ToxRoute&DT_ROWS_PER_PAGE=100000")
			  .asString();
			
			
			FileWriter fw=new FileWriter(destPath);
			fw.write(response.getBody());
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	static Vector<String> getURLList(String filepath) {

		Vector<String>urlVec=new Vector<String>();
		
		try {
			Document doc = Jsoup.parse(new File(filepath),"UTF-8");
			
			Element elementDivResults =doc.select("div.results-bar").first();
			Element elementTable=elementDivResults.nextElementSibling();
			
			Elements imgElements=elementTable.getElementsByTag("img");
			
			for	(Element element:imgElements) {
				String src=element.attr("src");				
				String CAS=src.replace("/chemidplus/structure/", "");
//				System.out.println(CAS);
				urlVec.add("https://chem.nlm.nih.gov/chemidplus/rn/"+CAS);

			}

			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return urlVec;
	}
	
	
	public static void main(String[] args) {
		
		//***********************************************************************************
//		boolean startFresh=false;
//		String route="inhalation";
//		String testType="LC50";
		//***********************************************************************************				
//		boolean startFresh=false;
//		String route="oral";
//		String testType="LD50";
		//***********************************************************************************
		boolean startFresh=false;
				String route="skin";
		String testType="LD50";
		//***********************************************************************************

		ParseChemidplus p=new ParseChemidplus("Toxicity");
		
		String filePathSearchResults=p.mainFolder+File.separator+route+testType+".html";	//	
				
		//Perform search using API to get list of chemicals with route and testType:
//		getChemidplusSearchResult(filePathSearchResults,testType,route);
	
		Vector<String>urls=getURLList(filePathSearchResults);		
		System.out.println(urls.size());
		
		//For some reason following urls were missing in search webpage:
//		Vector<String>urls=new Vector<>();
//		urls.add("https://chem.nlm.nih.gov/chemidplus/rn/11104-93-1");
//		urls.add("https://chem.nlm.nih.gov/chemidplus/rn/150824-47-8");
//		urls.add("https://chem.nlm.nih.gov/chemidplus/rn/173994-67-7");
		

		String databasePath = p.databaseFolder+File.separator+p.sourceName+"_raw_html.db";			
		downloadWebpages(databasePath, urls, "webpages", startFresh);		

			
	}
	
	/**
	 * Parses the HTML strings in the raw HTML database to RecordLookChem objects
	 * @return	A vector of RecordLookChem objects containing the data from the raw HTML database
	 */
	public Vector<RecordChemidplus> parseWebpagesInDatabase(String databasePath) {
		Vector<RecordChemidplus> records = new Vector<>();

		try {
			Statement stat = SQLite_Utilities.getStatement(databasePath);
			ResultSet rs = SQLite_GetRecords.getAllRecords(stat, "webpages");

			int counter = 1;
			
			ParseChemidplusElements pce=new ParseChemidplusElements();
			
			while (rs.next()) {
				// if (counter % 100==0) { System.out.println("Parsed "+counter+" pages"); }
				
				String html = rs.getString("content");
				String url = rs.getString("url");
				String date = rs.getString("date");
				Document doc = Jsoup.parse(html);
				
				RecordChemidplus rc=new RecordChemidplus();
				rc.url=url;
				rc.fileName=url.substring(url.lastIndexOf("/")+1, url.length());
				rc.date_accessed = date.substring(0,date.indexOf(" "));
				
				pce.createRecordChemidplus(rc, doc);

				
				records.add(rc);
				counter++;

//				if (rc.CASRegistryNumber != null) {
//					records.add(rc);
//					counter++;
//				} else {
//					// rs.updateString("html", ExperimentalConstants.strRecordUnavailable);
//					// Updater doesn't work - JDBC version issue?
//				}
			}
			
			System.out.println("Parsed "+(counter-1)+" pages");
			return records;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	

	
	public static void downloadWebpages(String databasePath,Vector<String> urls,String tableName, boolean startFresh) {

		File db = new File(databasePath);
		if(!db.getParentFile().exists()) { db.getParentFile().mkdirs(); }
		
		java.sql.Connection conn=SQLite_CreateTable.create_table(databasePath, tableName, RawDataRecord.fieldNames, startFresh);
		Random rand = new Random();
		
		try {
			int counter = 0;
			for (int i = 0; i < urls.size(); i++) {
				String url = urls.get(i);
				
				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");  
				Date date = new Date();  
				String strDate=formatter.format(date);
				
				RawDataRecord rec=new RawDataRecord(strDate, url, "");
				boolean haveRecord=rec.haveRecordInDatabase(databasePath,tableName,conn);

				if (i%1000==0) System.out.println(i);
				
				if (!haveRecord || startFresh) {
				
					System.out.println(i+"\t"+url);
					long delay = 0;
					try {
						long startTime=System.currentTimeMillis();
						rec.content=FileUtilities.getText(url).replaceAll("'", "''"); //single quotes mess with the SQL insert later
						
						long endTime=System.currentTimeMillis();
						delay = endTime-startTime;
						rec.addRecordToDatabase(tableName, conn);
						counter++;
						if (counter % 100==0) { System.out.println("Downloaded "+counter+" pages"); }
					} catch (Exception ex) {
						System.out.println("Failed to download "+url);
					}
					Thread.sleep(3000);
				}
			}
			
			System.out.println("Downloaded "+counter+" pages");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
