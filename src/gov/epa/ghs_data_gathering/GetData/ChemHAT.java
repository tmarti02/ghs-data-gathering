package gov.epa.ghs_data_gathering.GetData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import gov.epa.ghs_data_gathering.Utilities.Utilities;

/**
 * Class for downloading and manipulating ChemHat data
 * 
 * @author Todd Martin
 *
 */
public class ChemHAT {

	String[] acuteEndpoints = { "Toxic To Humans & Animals", "Irritates The Eyes", "Irritates The Skin" };

	// String [] chronicEndpoints={"Breast Cancer","Cancer","Birth Defects",
	// "Reproductive Harm","Gene Damage","Endocrine Disruption","Asthma
	// Trigger",
	// "Sensitizes The Skin","Other Health Effects","Brain/Nervous System
	// Harm"};

	String[] chronicEndpoints = { "Breast Cancer", "Cancer", "Birth Defects", "Reproductive Harm", "Gene Damage",
			"Endocrine Disruption", "Asthma Trigger", "Sensitizes the Skin", "Other Health Effects",
			"Brain/Nervous System Harm", "PBT (Persistent Bioaccumulative Toxicant)" };

	String[] inherentHazards = { "Flammable", "Restricted List", "Reactive" };

	String[] environmentalHazards = { "Immediate Harm to Aquatic Ecosystems", "Long-Term Harm to Aquatic Ecosystems",
			"Harmful to Land Ecosystems", "Bioaccumulative", "Persistent" };

	String lookup(String CAS) {

		String strURL = "http://www.chemhat.org/chemical-finder/";
		strURL += CAS.substring(0, CAS.length() - 2);

		try {
			java.net.URL myURL = new java.net.URL(strURL);
			// System.out.println(strURL);

			BufferedReader br = new BufferedReader(new InputStreamReader(myURL.openStream()));

			String seek = "<span class=\"views-field views-field-title\">        <span class=\"field-content\">";

			while (true) {
				String Line = br.readLine();

				if (Line == null) {
					return "not found";
				}

				if (Line.indexOf(seek) > -1) {

					Line = Line.substring(Line.indexOf(seek) + seek.length(), Line.length());
					Line = Line.substring(Line.indexOf("href=\"") + 6, Line.length());
					Line = Line.substring(0, Line.indexOf("\""));

					if (Line.indexOf("/" + CAS + "/") > -1) {// make sure have
																// CAS match:

						String strURL2 = "http://www.chemhat.org" + Line;

						System.out.println(strURL2);
						return strURL2;
					}

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "err";
		}

	}

	/**
	 * Programmatically determine all the chemhat chemical urls by searching for
	 * chemicals with 1 through 9 (since CAS numbers have these numbers in them)
	 * <br><br>
	 * Creates nums1.txt - nums9.txt which have all the URLs
	 * 
	 */
	void getChemHatChems() {
		// http://www.chemhat.org/en/chemical-finder/1



		// int num=1;

		for (int num = 1; num <= 9; num++) {
			
			// find all records with num in it:

			String strURL = "http://www.chemhat.org/en/chemical-finder/" + num;

			try {
				java.net.URL myURL = new java.net.URL(strURL);
				// System.out.println(strURL);

				BufferedReader br = new BufferedReader(new InputStreamReader(myURL.openStream()));

				String seek = "<li class=\"pager-last last\">";

				// first determine number of pages:
				int pageCount = 0;

				while (true) {
					String Line = br.readLine();

					if (Line.indexOf(seek) > -1) {
						Line = Line.substring(Line.indexOf("page=") + 5, Line.length());
						Line = Line.substring(0, Line.indexOf("\""));
						// System.out.println(Line);

						pageCount = Integer.parseInt(Line);
						break;
					}
				}

				FileWriter fw = new FileWriter("ChemHat/chems" + num + ".txt");

				for (int i = 0; i <= pageCount; i++) {

					System.out.println("num=" + num + ", page=" + i);

					// for (int i=0;i<=0;i++) {
					String strURLi = "http://www.chemhat.org/en/chemical-finder/" + num
							+ "?sort_bef_combine=title%20ASC&sort_order=ASC&sort_by=title&page=" + i;
					this.getChems(strURLi, fw);
				}
				fw.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		} // end loop over nums

	}

	void getChems(String strURL, FileWriter fw) {

		try {
			// System.out.println(strURL);
			java.net.URL myURL = new java.net.URL(strURL);

			BufferedReader br = new BufferedReader(new InputStreamReader(myURL.openStream()));

			String seek = "<div class=\"views-row views-row-1\">";

			while (true) {
				String Line = br.readLine();

				if (Line.indexOf(seek) > -1) {
					break;
				}
			}

			String seek2 = "<h2 class=\"element-invisible\">";

			while (true) {

				br.readLine();// blank line

				String Line = br.readLine();

				if (Line.indexOf(seek2) > -1) {
					break;
				}

				Line = Line.substring(Line.indexOf(">") + 1, Line.length());
				Line = Line.substring(Line.indexOf(">") + 1, Line.length());
				Line = Line.substring(Line.indexOf("\"") + 1, Line.length());
				Line = Line.substring(0, Line.indexOf("\""));

				String chemURL = "http://www.chemhat.org" + Line;

				fw.write(chemURL + "\r\n");
				fw.flush();

				for (int i = 1; i <= 3; i++) {
					br.readLine();
				}

			}

			// <h2 class="element-invisible">

			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * Gets a list of all the unique URLs for chemicals in ChemHat.org
	 * 
	 */
	void getUniqueChems() {
		String strFolder = "ChemHat";

		File Folder = new File(strFolder);
		Vector<String> urls = new Vector<String>();
		try {

			for (int i = 1; i <= 9; i++) {

				BufferedReader br = new BufferedReader(new FileReader(strFolder + "/chems" + i + ".txt"));

				while (true) {
					String Line = br.readLine();

					if (Line == null)
						break;

					if (!urls.contains(Line)) {
						urls.add(Line);
					}

				}

				br.close();

			} // end loop over files

			FileWriter fw = new FileWriter(strFolder + "/unique urls.txt");

			for (int i = 0; i < urls.size(); i++) {
				fw.write(urls.get(i) + "\r\n");
				fw.flush();
			}

			fw.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Download web pages from ChemHat.org using the list of URLS previously
	 * determined to be all their chemicals
	 * 
	 * @param urllist
	 * @param destFolderPath
	 */
	void downloadchemicals(String urllist, String destFolderPath) {

		try {

			BufferedReader br = new BufferedReader(new FileReader(urllist));

			String bob = "http://www.chemhat.org/en/chemical/";

			while (true) {
				String Line = br.readLine();

				if (Line == null || Line.equals(""))
					break;

				String CAS = Line.substring(Line.indexOf(bob) + bob.length(), Line.length());

				try {
					CAS = CAS.substring(0, CAS.indexOf("/"));
				} catch (Exception ex1) {
					// System.out.println(Line);
				}

				File newFile = new File(destFolderPath + "/" + CAS + ".html");
				if (newFile.exists()) {
					System.out.println(CAS + "\texists");
					continue;
				}

				java.net.URL myURL = new java.net.URL(Line);

				// System.out.println(CAS);

				BufferedReader br2 = new BufferedReader(new InputStreamReader(myURL.openStream()));

				System.out.println(CAS);

				FileWriter fw = new FileWriter(destFolderPath + "/" + CAS + ".html");

				// first determine number of pages:
				int pageCount = 0;

				while (true) {
					String Line2 = br2.readLine();

					// System.out.println(Line2);

					if (Line2 == null)
						break;

					fw.write(Line2 + "\r\n");

				}
				fw.close();

				Thread.sleep(3000);// sleep so dont get locked out

			}

			br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	String seek(BufferedReader br, String strSeek) {
		String Line = "";

		try {
			int counter = 0;

			while (true) {
				Line = br.readLine();
				counter++;
				if (Line == null)
					break;

				// System.out.println(counter+":"+Line);

				if (Line.indexOf(strSeek) > -1) {
					return Line;
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "Not found";
	}

	void parseStrings(String result, String[] endpoints) {
		String bob1 = "<h4>Direct Hazard <span class=\"caret\">&raquo;</span> ";
		String bob2 = "</h4>";

		for (int i = 0; i < endpoints.length; i++) {
			String strSeek2 = bob1 + endpoints[i] + bob2;

			if (result.indexOf(strSeek2) > -1) {
				//
				String Line2 = result.substring(result.indexOf(strSeek2) + strSeek2.length(), result.length());
				Line2 = Line2.substring(0, Line2.indexOf("</li></ul>"));
				System.out.println("\n" + endpoints[i]);
				this.parseString(Line2);
			}

		}

	}

	void parseStrings(String result, String[] endpoints, FileWriter fw) {
		String bob1 = "<h4>Direct Hazard <span class=\"caret\">&raquo;</span> ";

		// <h4>Direct Hazard <span class="caret">&raquo;</span> Toxic to Humans
		// & Animals</h4>

		// <h4>Direct Hazard <span class="caret">»</span> Toxic to Humans &amp;
		// Animals</h4>

		String bob2 = "</h4>";

		for (int i = 0; i < endpoints.length; i++) {

			String strSeek2 = bob1 + endpoints[i] + bob2;
			if (endpoints[i].toLowerCase().equals("bioaccumulative")) {
				strSeek2 = bob1 + endpoints[i] + " " + bob2;
			}

			// String strSeek2=bob1+endpoints[i]+" "+bob2;

			// if (endpoints[i].toLowerCase().equals("bioaccumulative") &&
			// result.toLowerCase().indexOf("bioaccumulative")>-1) {
			// System.out.println("**"+result);
			// System.out.println(strSeek2);
			// }

			// System.out.println(result);
			// System.out.println("strSeek2="+strSeek2+"\n");

			if (result.toLowerCase().indexOf(strSeek2.toLowerCase()) > -1) {

				String Line2 = result.substring(
						result.toLowerCase().indexOf(strSeek2.toLowerCase()) + strSeek2.length(), result.length());

				// String
				// Line2=result.substring(result.indexOf(strSeek2.toLowerCase())+strSeek2.length(),
				// result.length());

				// if (result.indexOf("Irritates the Eyes")>-1) {
				// System.out.println("*"+strSeek2+"\t"+endpoints[i]);
				//
				//// System.out.println(Line2+"\n");
				//
				//// System.out.println(result);
				//// System.out.println(strSeek2);
				//// System.out.println("***"+Line2+"\n");
				// }

				// System.out.println(strSeek2);
				// System.out.println(endpoints[i]+"\t"+Line2);

				Line2 = Line2.substring(0, Line2.indexOf("</li></ul>"));

				// System.out.println(endpoints[i]+"\t"+Line2+"\n");

				// if (endpoints[i].toLowerCase().equals("bioaccumulative")) {
				// System.out.println(Line2);
				// }

				try {
					fw.write("\n" + endpoints[i] + "\r\n");

					// System.out.println(endpoints[i]);

					// System.out.println(Line2);

					Vector<String> lines = this.parseString2(Line2);

					for (int j = 0; j < lines.size(); j++) {
						// System.out.println(lines.get(j));
						fw.write(lines.get(j) + "\r\n");
					}
					fw.flush();

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}

		}

	}

	void parseChemHatFile(String filePath) {

		try {

			BufferedReader br = new BufferedReader(new FileReader(filePath));

			String strSeek = "<h3>Data sources:</h3>";

			while (true) {

				String result = this.seek(br, strSeek);

				if (result.indexOf("sources-acute") > -1)
					this.parseStrings(result, acuteEndpoints);
				if (result.indexOf("sources-chronic") > -1)
					this.parseStrings(result, chronicEndpoints);
				if (result.indexOf("sources-inherent") > -1)
					this.parseStrings(result, inherentHazards);
				if (result.indexOf("sources-enviro") > -1)
					this.parseStrings(result, environmentalHazards);

				// System.out.println(result);

				if (result.equals("Not found"))
					break;

			}

			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * For each endpoint it generates an endpoint file which has multiple lines per chemical.
	 * <br><br>
	 * Data is taken from parsed text files.
	 * 
	 * Typically files are stored at "ChemHat\endpoint files\endpoint.txt"
	 * 
	 * @param srcfolder
	 * @param destfolder
	 */
	
	void generateAllEndpointFiles(String srcfolder, String destfolder) {

		File DF = new File(destfolder);
		if (!DF.exists())
			DF.mkdir();

		String[] allEndpoints = { "Toxic To Humans & Animals", "Irritates The Eyes", "Irritates The Skin",
				"Breast Cancer", "Cancer", "Birth Defects", "Reproductive Harm", "Gene Damage", "Endocrine Disruption",
				"Asthma Trigger", "Sensitizes The Skin", "Other Health Effects", "Brain/Nervous System Harm",
				"Flammable", "Restricted List", "Reactive", "Immediate Harm to Aquatic Ecosystems",
				"Long-Term Harm to Aquatic Ecosystems", "Harmful to Land Ecosystems", "Bioaccumulative", "Persistent" };

		// String [] allEndpoints={"Irritates The Eyes"};

		for (int i = 0; i < allEndpoints.length; i++) {
			System.out.println("\r\n" + allEndpoints[i]);
			generateEndpointFile(srcfolder, allEndpoints[i], destfolder);
		}

	}

	/**
	 * This version returns the final classifications according to chemhat
	 * 
	 * @param filePath
	 */
	String parseChemHatFile2(String filePath, boolean outputToScreen) {

		try {
			// in the meta data, the "To" is capitalized!

			String[] scoreAcute = new String[acuteEndpoints.length];
			String[] scoreChronic = new String[chronicEndpoints.length];
			String[] scoreInherentHazards = new String[inherentHazards.length];
			String[] scoreEnvironmentalHazards = new String[environmentalHazards.length];

			for (int i = 0; i < scoreAcute.length; i++)
				scoreAcute[i] = "";
			for (int i = 0; i < scoreChronic.length; i++)
				scoreChronic[i] = "";
			for (int i = 0; i < scoreInherentHazards.length; i++)
				scoreInherentHazards[i] = "";
			for (int i = 0; i < scoreEnvironmentalHazards.length; i++)
				scoreEnvironmentalHazards[i] = "";

			BufferedReader br = new BufferedReader(new FileReader(filePath));

			while (true) {

				String Line = br.readLine();

				if (Line == null)
					break;

				if (Line.indexOf("<h3>Acute (Short Term) Effects") > -1) {
					Line = br.readLine();
					this.getFinalVals("Acute (Short Term) Effects", Line, acuteEndpoints, scoreAcute, outputToScreen);

				} else if (Line.indexOf("<h3>Chronic (Long Term) Effects") > -1) {
					Line = br.readLine();

					this.getFinalVals("Chronic (Long Term) Effects", Line, chronicEndpoints, scoreChronic,
							outputToScreen);

					// System.out.println(Line);
				} else if (Line.indexOf("<h2>Inherent Hazards") > -1) {
					for (int i = 1; i <= 3; i++) {
						Line = br.readLine();
						// System.out.println(Line);
					}
					this.getFinalVals("Inherent Hazards", Line, inherentHazards, scoreInherentHazards, outputToScreen);

				} else if (Line.indexOf("<h2>How does this chemical impact the environment?") > -1) {

					for (int i = 1; i <= 3; i++) {
						Line = br.readLine();
					}

					this.getFinalVals("Environmental Hazards", Line, environmentalHazards, scoreEnvironmentalHazards,
							outputToScreen);

				}

			}

			br.close();

			String result = "";
			String header = "CAS\t";

			for (int i = 0; i < acuteEndpoints.length; i++) {
				result += scoreAcute[i] + "\t";
				header += acuteEndpoints[i] + "\t";
			}

			for (int i = 0; i < chronicEndpoints.length; i++) {
				result += scoreChronic[i] + "\t";
				header += chronicEndpoints[i] + "\t";
			}

			for (int i = 0; i < inherentHazards.length; i++) {
				result += scoreInherentHazards[i] + "\t";
				header += inherentHazards[i] + "\t";
			}

			for (int i = 0; i < environmentalHazards.length; i++) {
				result += scoreEnvironmentalHazards[i] + "\t";
				header += environmentalHazards[i] + "\t";
			}

			File filei = new File(filePath);
			String CAS = filei.getName().substring(0, filei.getName().indexOf("."));

			if (outputToScreen) {
				System.out.println(header);
				System.out.println(CAS + "\t" + result);
			}

			return result;

		} catch (Exception ex) {
			ex.printStackTrace();
			return ex.getMessage();
		}

	}

	void getFinalVals(String endpointClass, String Line, String[] endpointNames, String[] endpointValues,
			boolean outputToScreen) {
		if (Line.indexOf("div class=\"result endpoint\">") > -1) {

			if (outputToScreen)
				System.out.println("\r\n" + endpointClass);

			while (Line.indexOf("alt=") > -1) {
				Line = Line.substring(Line.indexOf("alt=") + 5, Line.length());

				// System.out.println(Line);

				String result = Line.substring(0, Line.indexOf("\""));

				for (int i = 0; i < endpointNames.length; i++) {

					// System.out.println(i+"\t"+
					// result+"\t"+acuteEndpoints[i]);
					// result=result.substring(result.indexOf("priority"),
					// result.length());
					if (result.toLowerCase().indexOf(endpointNames[i].toLowerCase()) > -1) {
						endpointValues[i] = result.substring(result.length() - 1, result.length());
						// System.out.println(i+"\t"+endpointNames[i]+"\t"+result);
						if (outputToScreen)
							System.out.println(i + "\t" + endpointNames[i] + "\t" + endpointValues[i]);
					}
				}
			}

		}
	}

	/**
	 * Parses ChemHat web pages and converts to text file which has different
	 * records on separate lines
	 * 
	 * @param srcFolderPath
	 * @param destFolderPath
	 */
	void parseChemHatFiles(String srcFolderPath, String destFolderPath) {

		try {

			File srcFolder = new File(srcFolderPath);

			File destFolder = new File(destFolderPath);
			if (!destFolder.exists())
				destFolder.mkdir();

			File[] files = srcFolder.listFiles();

			for (int i = 0; i < files.length; i++) {

				String filename = files[i].getName();

				if (filename.indexOf(".html") == -1)
					continue;

				// if (!filename.equals("115-86-6.html")) continue;

				BufferedReader br = new BufferedReader(new FileReader(files[i]));

				String strSeek = "<h3>Data sources:</h3>";

				File filenew = new File(destFolderPath + "/" + filename.replace("html", "txt"));

				FileWriter fw = new FileWriter(filenew);

				// System.out.println(filename);

				while (true) {

					String result = this.seek(br, strSeek);

					// System.out.println("result of seek="+result);

					if (result.indexOf("sources-acute") > -1)
						this.parseStrings(result, acuteEndpoints, fw);
					if (result.indexOf("sources-chronic") > -1)
						this.parseStrings(result, chronicEndpoints, fw);
					if (result.indexOf("sources-inherent") > -1)
						this.parseStrings(result, inherentHazards, fw);
					if (result.indexOf("sources-enviro") > -1) {

						// if (result.indexOf("bioaccum")>-1) {
						// System.out.println("\r\n"+filename+":"+result);
						// }
						this.parseStrings(result, environmentalHazards, fw);
					}

					// System.out.println(result);

					if (result.equals("Not found"))
						break;

				}
				br.close();
				fw.close();

				if (filenew.length() == 0)
					filenew.delete();

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	//
	/**
	 * gets the final scores according to chemhat scheme from the web pages
	 * 
	 * @param srcFolderPath
	 * @param destFilePath
	 */
	void parseChemHatFiles2(String srcFolderPath, String destFilePath) {

		try {

			String[] acuteEndpoints = { "Toxic to Humans & Animals", "Irritates the Eyes", "Irritates the Skin" };

			String[] chronicEndpoints = { "Breast Cancer", "Cancer", "Birth Defects", "Reproductive Harm",
					"Gene Damage", "Endocrine Disruption", "Asthma Trigger", "Sensitizes the Skin",
					"Other Health Effects", "Brain/Nervous System Harm" };

			String[] inherentHazards = { "Flammable", "Restricted List", "Reactive" };

			String[] environmentalHazards = { "Immediate Harm to Aquatic Ecosystems",
					"Long-Term Harm to Aquatic Ecosystems", "Harmful to Land Ecosystems", "Bioaccumulative",
					"Persistent" };

			File srcFolder = new File(srcFolderPath);
			File[] files = srcFolder.listFiles();

			FileWriter fw = new FileWriter(destFilePath);

			String header = "CAS\t";

			for (int i = 0; i < acuteEndpoints.length; i++) {
				header += acuteEndpoints[i] + "\t";
			}

			for (int i = 0; i < chronicEndpoints.length; i++) {
				header += chronicEndpoints[i] + "\t";
			}

			for (int i = 0; i < inherentHazards.length; i++) {
				header += inherentHazards[i] + "\t";
			}

			for (int i = 0; i < environmentalHazards.length; i++) {
				header += environmentalHazards[i] + "\t";
			}

			fw.write(header + "\r\n");

			System.out.println("Begin loop through files");

			for (int i = 0; i < files.length; i++) {

				String filename = files[i].getName();
				String CAS = filename.substring(0, filename.indexOf("."));

				String result = this.parseChemHatFile2(files[i].getAbsolutePath(), true);

				fw.write(CAS + "\t" + result + "\r\n");
				fw.flush();

				if (i % 100 == 0)
					System.out.println(i);

			}

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	// void parseString(String strParse) {
	//
	// String category=strParse.substring(0,strParse.indexOf("</div>"));
	//
	// String bob="</div><div class=\"listName\">";
	//
	// strParse=strParse.substring(strParse.indexOf(bob)+bob.length(),strParse.length());
	//
	// String listName=strParse.substring(0,strParse.indexOf("</div>"));
	//
	// String bob2="</div><div class=\"agency\">";
	//
	// strParse=strParse.substring(strParse.indexOf(bob2)+bob2.length(),strParse.length());
	//
	// String agency=strParse.substring(0,strParse.indexOf("</div>"));
	//
	//// System.out.println(agency);
	//
	//// System.out.println(category+"\t"+listName+"\t"+agency);
	//// System.out.println(listName);
	//
	// }

	Vector<String> parseString2(String strParse) {

		String bob = "<div class=\"sublistName\">";
		String bob2 = "</div><div class=\"listName\">";
		String bob3 = "</div><div class=\"agency\">";

		Vector<String> lines = new Vector<String>();

		String bob99 = strParse;

		while (true) {

			// System.out.println(strParse);

			if (strParse.indexOf(bob) == -1)
				break;

			strParse = strParse.substring(strParse.indexOf(bob) + bob.length());

			// System.out.println(strParse);

			// if (strParse.indexOf("</div>")==-1) break;

			String category = strParse.substring(0, strParse.indexOf(bob2));

			// System.out.println("category="+category);

			strParse = strParse.substring(strParse.indexOf(bob2) + bob2.length(), strParse.length());

			// System.out.println(strParse);

			String listName = strParse.substring(0, strParse.indexOf(bob3));

			listName = listName.replace("&amp;", "&");

			// System.out.println("listName="+listName);

			strParse = strParse.substring(strParse.indexOf(bob3) + bob3.length(), strParse.length());

			// System.out.println("bob1:"+strParse);

			String agency = "";

			if (strParse.indexOf("</div>") > -1) {
				agency = strParse.substring(0, strParse.indexOf("</div>"));
			} else {
				agency = strParse;
			}

			// if (bob99.toLowerCase().indexOf("bioaccumulative")>-1) {
			// System.out.println(category+"\t"+listName+"\t"+agency);
			// }

			// System.out.println("agency="+agency);

			lines.add(category + "\t" + listName + "\t" + agency);
		}

		// System.out.println(agency);
		return lines;
		//
		// System.out.println(listName);

	}

	void parseString(String strParse) {

		// System.out.println("\n"+strParse+"\n");

		String bob = "<div class=\"sublistName\">";
		String bob2 = "</div><div class=\"listName\">";
		String bob3 = "</div><div class=\"agency\">";

		while (true) {

			// System.out.println(strParse);

			if (strParse.indexOf(bob) == -1)
				break;

			strParse = strParse.substring(strParse.indexOf(bob) + bob.length());

			// System.out.println(strParse);

			// if (strParse.indexOf("</div>")==-1) break;

			String category = strParse.substring(0, strParse.indexOf(bob2));

			// System.out.println("category="+category);

			strParse = strParse.substring(strParse.indexOf(bob2) + bob2.length(), strParse.length());

			// System.out.println(strParse);

			String listName = strParse.substring(0, strParse.indexOf(bob3));

			listName = listName.replace("&amp;", "&");

			// System.out.println("listName="+listName);

			strParse = strParse.substring(strParse.indexOf(bob3) + bob3.length(), strParse.length());

			// System.out.println("bob1:"+strParse);

			String agency = "";

			if (strParse.indexOf("</div>") > -1) {
				agency = strParse.substring(0, strParse.indexOf("</div>"));
			} else {
				agency = strParse;
			}

			// System.out.println("agency="+agency);

			System.out.println(category + "\t" + listName + "\t" + agency);
		}

	}

	void generateEndpointFile(String folder, String endpoint, String outputFolderPath) {
		File Folder = new File(folder);

		File[] files = Folder.listFiles();

		// System.out.println(folder);

		try {

			File textOutputFile = new File(outputFolderPath + "/" + endpoint.replace("/", "_") + ".txt");
			File excelOutputFile=new File(outputFolderPath+"/"+endpoint.replace("/", "_")+"_ChemHat.xls");
			
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet = wb.createSheet("all data");

			// if (newFile.exists()) return;

			FileWriter fw = new FileWriter(textOutputFile);

			fw.write("CAS	Score	ListName	Agency\r\n");

			int count = 0;//count of chemicals with endpoint
			int recordCount=0;//count of records with endpoint
			
			TreeMap<String,Integer>htCounts=new TreeMap<String,Integer>();
			
			createHeader(sheet);
			
			for (int i = 0; i < files.length; i++) {

				File file = files[i];

				// System.out.println(file.getName()+"\t"+endpoint);

				String filename = file.getName();

				String CAS = filename.substring(0, filename.indexOf("."));

				Vector<String> data = this.getEndpointData(file, endpoint);
				
				if (i%100==0) System.out.println(i);
				
//				if (i==500) break;

				if (data.size() > 0) {
//					System.out.println(CAS);
					count++;
				}

				for (int j = 0; j < data.size(); j++) {
					recordCount++;
					
					String record=data.get(j);
					
//					CAS	Score	ListName	Agency
					LinkedList<String>list=gov.epa.ghs_data_gathering.Utilities.Utilities.Parse3(record, "\t");
					
					String Score=list.get(0);
					String ListName=list.get(1);
					String Agency=list.get(2);
					
					String listNameAbbrev="";
					if (ListName.length()>30) {
						listNameAbbrev=ListName.substring(0, 30);
					} else {
						listNameAbbrev=ListName;
					}
					
					listNameAbbrev=listNameAbbrev.replace(":", "_").replace("/", "_");
					
					if (wb.getSheet(listNameAbbrev)==null) {
						wb.createSheet(listNameAbbrev);
					}
					
					//add data to overall data tab:
					createDataRow(sheet, recordCount, CAS, Score, ListName, Agency);

//					System.out.println(ListName);
					
					if (htCounts.get(ListName)==null) {
						htCounts.put(ListName, new Integer(1));
						createHeader(wb.getSheet(listNameAbbrev));
					} else {
						int oldCount=htCounts.get(ListName);
						int newCount=oldCount+1;
						htCounts.put(ListName, newCount);
					}
					
					createDataRow(wb.getSheet(listNameAbbrev), htCounts.get(ListName), CAS, Score, ListName, Agency);
					
					fw.write(CAS + "\t" + data.get(j) + "\r\n");
				}
				fw.flush();

			}
			
			fw.write("\r\n\r\n");
			
			Set set = htCounts.entrySet();
		      // Get an iterator
		      Iterator i = set.iterator();
		      // Display elements
			
		      recordCount++;
		      
		      while (i.hasNext()) {
				
				Map.Entry me = (Map.Entry) i.next();
				fw.write(me.getKey() + ": " + me.getValue() + "\r\n");

				recordCount++;
				HSSFRow row=sheet.createRow(recordCount);

				Cell cell=row.createCell(2);
				cell.setCellValue(me.getKey()+"");

				cell=row.createCell(3);
				cell.setCellValue(me.getValue()+"");

			}

			System.out.println("num chemicals with endpoint = " + count);

			fw.close();
			
			//write out excel file:
			FileOutputStream fOut = new FileOutputStream(excelOutputFile);
			wb.setActiveSheet(0);
			wb.write(fOut);


		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void createDataRow(HSSFSheet sheet, int recordCount, String CAS, String Score, String ListName,
			String Agency) {
		HSSFRow row = sheet.createRow(recordCount);
		
		Cell cell=row.createCell(0);
		cell.setCellValue(CAS);

		cell=row.createCell(1);
		cell.setCellValue(Score);

		cell=row.createCell(2);
		cell.setCellValue(ListName);

		cell=row.createCell(3);
		cell.setCellValue(Agency);
	}

	private void createHeader(HSSFSheet sheet) {
		HSSFRow rowHeader = sheet.createRow(0);
		
		Cell cellHeader=rowHeader.createCell(0);
		cellHeader.setCellValue("CAS");

		cellHeader=rowHeader.createCell(1);
		cellHeader.setCellValue("Score");

		cellHeader=rowHeader.createCell(2);
		cellHeader.setCellValue("ListName");

		cellHeader=rowHeader.createCell(3);
		cellHeader.setCellValue("Agency");
		
		//Set column widths
		sheet.setColumnWidth(0, 18*256);
		
		for (int i=1;i<=3;i++) {
			sheet.setColumnWidth(i, 50*256);
		}
	}

	Vector<String> getEndpointData(File file, String endpoint) {
		Vector<String> data = new Vector<String>();

		try {

			BufferedReader br = new BufferedReader(new FileReader(file));

			boolean haveEndpoint = false;

			while (true) {
				String Line = br.readLine();

				if (Line == null)
					return data;

				if (Line.toLowerCase().indexOf(endpoint.toLowerCase()) == 0) {
					// System.out.println("here");
					haveEndpoint = true;
					break;

				}
			}

			if (!haveEndpoint) {
				br.close();
				return data;
			}

			while (true) {
				String Line = br.readLine();

				if (Line == null || Line.equals("")) {
					break;
				}

				data.add(Line);
			}

			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return data;
	}

	String[] determineScoreToxicToHumansAndAnimals(Vector records, String route) {
		String score1 = "VH";
		String score2 = "H";
		String score3 = "M";
		String score4 = "L";
		String score5 = "VL";

		String src = "";
		String finalScore = "";
		String finalScore2 = "";

		String scoreEU_H = "";// EU H system
		String scoreEU_R = "";// EU R system
		String scoreA = "";// Australia
		String scoreNZ = "";// New Zealand
		String scoreJ = "";// Japan
		String scoreK = "";// Korea
		String scoreM = "";// Malaysia
		String scoreW = "";// WHMIS-SIMDUT: Controlled Products Classifications

		String scoreEU_H2 = "";// EU H system
		String scoreEU_R2 = "";// EU R system
		String scoreA2 = "";// Australia
		String scoreNZ2 = "";// New Zealand
		String scoreJ2 = "";// Japan
		String scoreK2 = "";// Korea
		String scoreM2 = "";// Malaysia
		String scoreW2 = "";

		// X 0 Substances with EU Risk & Safety Phrases (Commission Directive
		// 67-548-EEC)
		// X 1 Regulation on the Classification, Labelling and Packaging of
		// Substances and Mixtures (CLP) Annex 6 Table 3-1 - GHS Hazard code
		// criteria
		// X 2 WHMIS-SIMDUT: Controlled Products Classifications
		// X 3 New Zealand HSNO Chemical Classifications
		// X 4 Japan GHS Classifications
		// X 5 Australia - GHS
		// X 6 Malaysia - GHS
		// O 7 Federal Insecticide, Fungicide, and Rodenticide Act (FIFRA)
		// Registered Pesticides (Selections)
		// O 8 Extremely Hazardous Substances - EPCRA Section 302
		// X 9 Korea GHS Classification and Labelling for Toxic Chemicals
		// O 10 Risk Management Actions & TSCA Work Plans

		for (int i = 0; i < records.size(); i++) {
			LinkedList<String> list = (LinkedList<String>) records.get(i);

			String CAS = list.get(0);
			String Score = list.get(1);
			String listName = list.get(2);
			String Agency = list.get(3);

			if (route.equals("oral")) {
				if (listName.equals(
						"Regulation on the Classification, Labelling and Packaging of Substances and Mixtures (CLP) Annex 6 Table 3-1 - GHS Hazard  code criteria")) {
					if (Score.indexOf("if swallowed") > -1) {
						if (Score.equals("H300 - Fatal if swallowed")) {
							scoreEU_H = score1;
						} else if (Score.equals("H301 - Toxic if swallowed")) {
							scoreEU_H = score2;
						} else if (Score.equals("H302 - Harmful if swallowed")) {
							scoreEU_H = score3;
						} else if (Score.equals("H304 - May be fatal if swallowed and enters airways")) {
							scoreEU_H = score2;// TODO
						} else {
							System.out.println("EU_H:" + CAS + "\t" + Score);
						}

						scoreEU_H2 = Score;
					}

				} else if (listName.indexOf("Substances with EU Risk & Safety Phrases") > -1) {

					if (Score.toLowerCase().indexOf("if swallowed") > -1) {

						if (Score.equals("R28 - Very Toxic if Swallowed")) {
							scoreEU_R = score1;
						} else if (Score.equals("R25 - Toxic if Swallowed")) {
							scoreEU_R = score2;
						} else if (Score.equals("R22 - Harmful if Swallowed")) {
							scoreEU_R = score3;
						} else {
							System.out.println("EU_R:" + CAS + "\t" + Score);
						}

						scoreEU_R2 = Score;

					} // end if swallowed

				} else if (listName.indexOf("Australia - GHS") > -1) {

					if (Score.toLowerCase().indexOf("if swallowed") > -1) {

						if (Score.equals("H300 - Fatal if swallowed")) {
							scoreA = score1;
						} else if (Score.equals("H301 - Toxic if swallowed")) {
							scoreA = score2;
						} else if (Score.equals("H302 - Harmful if swallowed")) {
							scoreA = score3;
						} else if (Score.equals("H304 - May be fatal if swallowed and enters airways")) {
							scoreA = score2;// TODO
						} else {
							System.out.println("A:" + CAS + "\t" + Score);
						}

						scoreA2 = Score;
					}

				} else if (listName.equals("Korea GHS Classification and Labelling for Toxic Chemicals")) {

					if (Score.toLowerCase().indexOf("if swallowed") > -1) {
						if (Score.indexOf("H300 - Fatal if swallowed") > -1) {
							scoreK = score1;
						} else if (Score.indexOf("H301 - Toxic if swallowed") > -1) {
							scoreK = score2;
						} else if (Score.indexOf("H302 - Harmful if swallowed") > -1) {
							scoreK = score3;
						} else if (Score.indexOf("H304 - May be fatal if swallowed and enters airways") > -1) {
							scoreK = score2;// TODO
						} else {
							System.out.println("K:" + CAS + "\t" + Score);
						}
						scoreK2 = Score;

					}

				} else if (listName.equals("Malaysia - GHS")) {
					if (Score.toLowerCase().indexOf("if swallowed") > -1) {
						if (Score.indexOf("H300 - Fatal if swallowed") > -1) {
							scoreM = score1;
						} else if (Score.indexOf("H301 - Toxic if swallowed") > -1) {
							scoreM = score2;
						} else if (Score.indexOf("H302 - Harmful if swallowed") > -1) {
							scoreM = score3;
						} else if (Score.indexOf("H304 - May be fatal if swallowed and enters airways") > -1) {
							scoreM = score2;// TODO
						} else {
							System.out.println("M:" + CAS + "\t" + Score);
						}
					}

				} else if (listName.equals("New Zealand HSNO Chemical Classifications")) {
					if (Score.toLowerCase().indexOf("oral") > -1) {

						if (Score.equals("6.1A (oral) - Acutely toxic")) {
							scoreNZ = score1;
						} else if (Score.equals("6.1B (oral) - Acutely toxic")) {
							scoreNZ = score1;
						} else if (Score.equals("6.1C (oral) - Acutely toxic")) {
							scoreNZ = score2;
						} else if (Score.equals("6.1D (oral) - Acutely toxic")) {
							scoreNZ = score3;
						} else if (Score.equals("6.1E (oral) - Acutely toxic")) {
							scoreNZ = score4;
						} else {
							System.out.println("NZ" + CAS + "\t" + Score);
						}

						scoreNZ2 = Score;

					}
				} else if (listName.equals("Japan GHS Classifications")) {
					if (Score.toLowerCase().indexOf("oral") > -1) {

						if (Score.equals("Acute toxicity (oral) - Category 1")) {
							scoreJ = score1;
						} else if (Score.equals("Acute toxicity (oral) - Category 2")) {
							scoreJ = score1;
						} else if (Score.equals("Acute toxicity (oral) - Category 3")) {
							scoreJ = score2;
						} else if (Score.equals("Acute toxicity (oral) - Category 4")) {
							scoreJ = score3;
						} else if (Score.equals("Acute toxicity (oral) - Category 5")) {
							scoreJ = score4;
						} else {
							System.out.println("J" + CAS + "\t" + Score);
						}
						scoreJ2 = Score;
					}
				} else if (listName.equals("WHMIS-SIMDUT: Controlled Products Classifications")) {

					/*
					 * Division 1 (D1) contains
					 * "Materials Causing Immediate and Serious Toxic Effects".
					 * It is represented by the WHMIS symbol to the right. As
					 * the title suggests, these materials can cause immediate
					 * and serious health effects. Within this division, there
					 * are two additional subdivisions that separate "Toxics"
					 * and "Very Toxics". The "Very Toxics" are D1A; the
					 * "Toxics" are D1B. The main difference between D1A and D1B
					 * is the value used to determine acute toxicity (e.g. LD50,
					 * LC50). In simplest terms, D1A substances require much
					 * less material to produce a fatal effect. Symbol -
					 * Materials Causing Other Toxic Effects
					 * 
					 * Division 2 (D2) is for
					 * "Materials Causing Other Toxic Effects". It is
					 * represented by the WHMIS symbol to the right. These
					 * materials have toxic effects but these effects may be
					 * delayed. The D2 division also has two subdivisions that
					 * separate "Toxics" and "Very Toxics". The "Very Toxics"
					 * are D2A; the "Toxics" are D2B. The "Toxic" group here
					 * also includes products that produce immediate but less
					 * serious reversible effects.
					 * 
					 * Under the D2A heading of
					 * "Materials Causing Other Toxic Effects", the health
					 * effects considered for very toxic materials (D2A)
					 * include: severe chronic toxic effects reproductive
					 * toxicity (material known or suspected to cause a negative
					 * impact on reproductive functions (male or female)
					 * teratogenicity and embryotoxicity (material known or
					 * suspected to cause a negative impact on a developing
					 * embryo or fetus) carcinogenicity (material known or
					 * suspected to cause cancer) respiratory sensitization
					 * 
					 * Under the D2 heading of
					 * "Materials Causing Other Toxic Effects", the health
					 * effects considered for toxic materials (D2B) include:
					 * chronic toxic effects skin or eye irritation skin
					 * sensitization mutagenicity (material known or suspected
					 * to cause changes to cells)
					 * 
					 * TMM: D2 shouldnt be used for acute tox endpoint! Maybe
					 * use for chronic tox! TODO
					 * 
					 * 
					 */

					if (Score.equals("Class D1A - Very toxic material causing immediate and serious toxic effects")) {
						scoreW = score1;
					} else if (Score.equals("Class D1B - Toxic material causing immediate and serious toxic effects")) {
						scoreW = score2;
					} else if (Score.indexOf("Class D2") > -1) {

						// for now dont use following for acute tox:
						// } else if (Score.equals("Class D2A - Very toxic
						// material causing other toxic effects")) {
						// scoreW=score3;
						// } else if (Score.equals("Class D2B - Toxic material
						// causing other toxic effects")) {
						// scoreW=score4;
					} else {
						System.out.println(listName + "\t" + Score);
					}
					scoreW2 = Score;

				} else if (listName.equals("Risk Management Actions & TSCA Work Plans")) {
					// System.out.println("RM:"+CAS+"\t"+Score);
					// for now don't use
				} else if (listName.equals(
						"Federal Insecticide, Fungicide, and Rodenticide Act (FIFRA) Registered Pesticides (Selections)")) {
					// System.out.println("FIFRA:"+CAS+"\t"+Score);
					// for now don't use
				} else if (listName.equals("Extremely Hazardous Substances - EPCRA Section 302")) {
					// System.out.println("EPCRA:"+CAS+"\t"+Score);
					// TODO- use score1?
				} else {
					System.out.println(listName + "\t" + Score);
				}

			} // end route = oral
			else if (route.equals("dermal")) {
				if (listName.equals(
						"Regulation on the Classification, Labelling and Packaging of Substances and Mixtures (CLP) Annex 6 Table 3-1 - GHS Hazard  code criteria")) {
					if (Score.indexOf("in contact with skin") > -1) {
						if (Score.equals("H310 - Fatal in contact with skin")) {
							scoreEU_H = score1;
						} else if (Score.equals("H311 - Toxic in contact with skin")) {
							scoreEU_H = score2;
						} else if (Score.equals("H312 - Harmful in contact with skin")) {
							scoreEU_H = score3;
						} else {
							System.out.println("EU_H:" + CAS + "\t" + Score);
						}

						scoreEU_H2 = Score;
					}

				} else if (listName.indexOf("Substances with EU Risk & Safety Phrases") > -1) {

					if (Score.toLowerCase().indexOf("in contact with skin") > -1) {

						if (Score.equals("R27 - Very Toxic in Contact with Skin")) {
							scoreEU_R = score1;
						} else if (Score.equals("R24 - Toxic in Contact with Skin")) {
							scoreEU_R = score2;
						} else if (Score.equals("R21 - Harmful in Contact with Skin")) {
							scoreEU_R = score3;
						} else {
							System.out.println("EU_R:" + CAS + "\t" + Score);
						}

						scoreEU_R2 = Score;

					} // end if swallowed

				} else if (listName.indexOf("Australia - GHS") > -1) {

					if (Score.toLowerCase().indexOf("in contact with skin") > -1) {

						if (Score.equals("H310 - Fatal in contact with skin")) {
							scoreA = score1;
						} else if (Score.equals("H311 - Toxic in contact with skin")) {
							scoreA = score2;
						} else if (Score.equals("H312 - Harmful in contact with skin")) {
							scoreA = score3;
						} else {
							System.out.println("A:" + CAS + "\t" + Score);
						}

						scoreA2 = Score;
					}

				} else if (listName.equals("Korea GHS Classification and Labelling for Toxic Chemicals")) {

					if (Score.toLowerCase().indexOf("skin") > -1) {
						if (Score.equals("Acute toxicity (dermal) - Category 1 [H310 - Fatal in contact with skin]")) {
							scoreK = score1;
						} else if (Score
								.equals("Acute toxicity (dermal) - Category 3 [H311 - Toxic in contact with skin]")) {
							scoreK = score2;
						} else if (Score
								.equals("Acute toxicity (dermal) - Category 4 [H312 - Harmful in contact with skin]")) {
							scoreK = score3;
						} else {
							System.out.println("K:" + CAS + "\t" + Score);
						}
						scoreK2 = Score;

					}

				} else if (listName.equals("Malaysia - GHS")) {
					if (Score.toLowerCase().indexOf("skin") > -1) {
						if (Score.indexOf("H310 - Fatal in contact with skin") > -1) {
							scoreM = score1;
						} else if (Score.indexOf("H311 - Toxic in contact with skin") > -1) {
							scoreM = score2;
						} else if (Score.indexOf("H312 - Harmful in contact with skin") > -1) {
							scoreM = score3;
						} else {
							System.out.println("M:" + CAS + "\t" + Score);
						}
					}

				} else if (listName.equals("New Zealand HSNO Chemical Classifications")) {
					if (Score.toLowerCase().indexOf("dermal") > -1) {

						if (Score.equals("6.1A (dermal) - Acutely toxic")) {
							scoreNZ = score1;
						} else if (Score.equals("6.1B (dermal) - Acutely toxic")) {
							scoreNZ = score1;
						} else if (Score.equals("6.1C (dermal) - Acutely toxic")) {
							scoreNZ = score2;
						} else if (Score.equals("6.1D (dermal) - Acutely toxic")) {
							scoreNZ = score3;
						} else if (Score.equals("6.1E (dermal) - Acutely toxic")) {
							scoreNZ = score4;
						} else {
							System.out.println("NZ" + CAS + "\t" + Score);
						}

						scoreNZ2 = Score;

					}
				} else if (listName.equals("Japan GHS Classifications")) {
					if (Score.toLowerCase().indexOf("dermal") > -1) {

						if (Score.equals("Acute toxicity (dermal) - Category 1")) {
							scoreJ = score1;
						} else if (Score.equals("Acute toxicity (dermal) - Category 2")) {
							scoreJ = score1;
						} else if (Score.equals("Acute toxicity (dermal) - Category 3")) {
							scoreJ = score2;
						} else if (Score.equals("Acute toxicity (dermal) - Category 4")) {
							scoreJ = score3;
						} else if (Score.equals("Acute toxicity (dermal) - Category 5")) {
							scoreJ = score4;
						} else {
							System.out.println("J" + CAS + "\t" + Score);
						}
						scoreJ2 = Score;
					}
				} else if (listName.equals("WHMIS-SIMDUT: Controlled Products Classifications")) {

					// for now dont use, since doesnt specify skin

				} else if (listName.equals("Risk Management Actions & TSCA Work Plans")) {
					// System.out.println("RM:"+CAS+"\t"+Score);
					// for now don't use
				} else if (listName.equals(
						"Federal Insecticide, Fungicide, and Rodenticide Act (FIFRA) Registered Pesticides (Selections)")) {
					// System.out.println("FIFRA:"+CAS+"\t"+Score);
					// for now don't use
				} else if (listName.equals("Extremely Hazardous Substances - EPCRA Section 302")) {
					// System.out.println("EPCRA:"+CAS+"\t"+Score);
					// TODO- use score1?
				} else {
					// System.out.println(listName+"\t"+Score);
				}

			} // end dermal
			else if (route.equals("inhaled")) {
				if (listName.equals(
						"Regulation on the Classification, Labelling and Packaging of Substances and Mixtures (CLP) Annex 6 Table 3-1 - GHS Hazard  code criteria")) {
					if (Score.indexOf("inhaled") > -1) {
						if (Score.equals("H330 - Fatal if inhaled")) {
							scoreEU_H = score1;
						} else if (Score.equals("H331 - Toxic if inhaled")) {
							scoreEU_H = score2;
						} else if (Score.equals("H332 - Harmful if inhaled")) {
							scoreEU_H = score3;
						} else {
							System.out.println("EU_H:" + CAS + "\t" + Score);
						}

						scoreEU_H2 = Score;
					}

				} else if (listName.indexOf("Substances with EU Risk & Safety Phrases") > -1) {

					if (Score.toLowerCase().indexOf("inhalation") > -1) {

						if (Score.equals("R26 - Very Toxic by Inhalation")) {
							scoreEU_R = score1;
						} else if (Score.equals("R23 - Toxic by Inhalation (gas, vapour, dust/mist)")) {
							scoreEU_R = score2;
						} else if (Score.equals("R20 - Harmful by Inhalation (gas or vapor or dust/mist)")) {
							scoreEU_R = score3;
						} else {
							System.out.println("EU_R:" + CAS + "\t" + Score);
						}

						scoreEU_R2 = Score;

					} // end if swallowed

				} else if (listName.indexOf("Australia - GHS") > -1) {

					if (Score.toLowerCase().indexOf("inhaled") > -1) {

						if (Score.equals("H330 - Fatal if inhaled")) {
							scoreA = score1;
						} else if (Score.equals("H331 - Toxic if inhaled")) {
							scoreA = score2;
						} else if (Score.equals("H332 - Harmful if inhaled")) {
							scoreA = score3;
						} else {
							System.out.println("A:" + CAS + "\t" + Score);
						}

						scoreA2 = Score;
					}

				} else if (listName.equals("Korea GHS Classification and Labelling for Toxic Chemicals")) {

					if (Score.toLowerCase().indexOf("inhal") > -1) {
						if (Score.equals("Acute toxicity (inhalation) - Category 1 [H330 - Fatal if inhaled]")) {
							scoreK = score1;
						} else if (Score.equals("Acute toxicity (inhalation) - Category 3 [H331 - Toxic if inhaled]")) {
							scoreK = score2;
						} else if (Score
								.equals("Acute toxicity (inhalation) - Category 4 [H332 - Harmful if inhaled]")) {
							scoreK = score3;
						} else {
							System.out.println("K:" + CAS + "\t" + Score);
						}
						scoreK2 = Score;

					}

				} else if (listName.equals("Malaysia - GHS")) {
					if (Score.toLowerCase().indexOf("inhal") > -1) {
						if (Score.indexOf("H330 - Fatal if inhaled") > -1) {
							scoreM = score1;
						} else if (Score.indexOf("H331 - Toxic if inhaled") > -1) {
							scoreM = score2;
						} else if (Score.indexOf("H332 - Harmful if inhaled") > -1) {
							scoreM = score3;
						} else {
							System.out.println("M:" + CAS + "\t" + Score);
						}
					}

				} else if (listName.equals("New Zealand HSNO Chemical Classifications")) {
					if (Score.toLowerCase().indexOf("inhalation") > -1) {

						if (Score.equals("6.1A (inhalation) - Acutely toxic")) {
							scoreNZ = score1;
						} else if (Score.equals("6.1B (inhalation) - Acutely toxic")) {
							scoreNZ = score1;
						} else if (Score.equals("6.1C (inhalation) - Acutely toxic")) {
							scoreNZ = score2;
						} else if (Score.equals("6.1D (inhalation) - Acutely toxic")) {
							scoreNZ = score3;
						} else if (Score.equals("6.1E (inhalation) - Acutely toxic")) {
							scoreNZ = score4;
						} else {
							System.out.println("NZ" + CAS + "\t" + Score);
						}

						scoreNZ2 = Score;

					}
				} else if (listName.equals("Japan GHS Classifications")) {
					if (Score.toLowerCase().indexOf("inhal") > -1) {

						if (Score.equals("Acute toxicity (inhalation: gas) - Category 1")
								|| Score.equals("Acute toxicity (inhalation: vapor) - Category 1")
								|| Score.equals("Acute toxicity (inhalation: dust, mist) - Category 1")) {
							scoreJ = score1;
						} else if (Score.equals("Acute toxicity (inhalation: gas) - Category 2")
								|| Score.equals("Acute toxicity (inhalation: vapor) - Category 2")
								|| Score.equals("Acute toxicity (inhalation: dust, mist) - Category 2")) {
							scoreJ = score1;
						} else if (Score.equals("Acute toxicity (inhalation: gas) - Category 3")
								|| Score.equals("Acute toxicity (inhalation: vapor) - Category 3")
								|| Score.equals("Acute toxicity (inhalation: dust, mist) - Category 3")) {
							scoreJ = score2;
						} else if (Score.equals("Acute toxicity (inhalation: gas) - Category 4")
								|| Score.equals("Acute toxicity (inhalation: vapor) - Category 4")
								|| Score.equals("Acute toxicity (inhalation: dust, mist) - Category 4")) {
							scoreJ = score3;
						} else if (Score.equals("Acute toxicity (inhalation: gas) - Category 5")
								|| Score.equals("Acute toxicity (inhalation: vapor) - Category 5")
								|| Score.equals("Acute toxicity (inhalation: dust, mist) - Category 5")) {
							scoreJ = score4;
						} else {
							System.out.println("J" + CAS + "\t" + Score);
						}
						scoreJ2 = Score;
					}
				} else if (listName.equals("WHMIS-SIMDUT: Controlled Products Classifications")) {

					// for now dont use, since doesnt specify inhalation

				} else if (listName.equals("Risk Management Actions & TSCA Work Plans")) {
					// System.out.println("RM:"+CAS+"\t"+Score);
					// for now don't use
				} else if (listName.equals(
						"Federal Insecticide, Fungicide, and Rodenticide Act (FIFRA) Registered Pesticides (Selections)")) {
					// System.out.println("FIFRA:"+CAS+"\t"+Score);
					// for now don't use
				} else if (listName.equals("Extremely Hazardous Substances - EPCRA Section 302")) {
					// System.out.println("EPCRA:"+CAS+"\t"+Score);
					// TODO- use score1?
				} else {
					// System.out.println(listName+"\t"+Score);
				}

			}
		} // end loop over records

		if (!scoreEU_H.equals("")) {
			finalScore = scoreEU_H;
			finalScore2 = scoreEU_H2;
			src = "EU_H";
		} else if (!scoreEU_R.equals("")) {
			finalScore = scoreEU_R;
			finalScore2 = scoreEU_R2;
			src = "EU_R";
		} else if (!scoreNZ.equals("")) {
			finalScore = scoreNZ;
			finalScore2 = scoreNZ2;
			src = "New Zealand";

		} else if (!scoreJ.equals("")) {
			finalScore = scoreJ;
			finalScore2 = scoreJ2;
			src = "Japan";
		} else if (!scoreA.equals("")) {
			finalScore = scoreA;
			finalScore2 = scoreA2;
			src = "Australia";
		} else if (!scoreK.equals("")) {
			finalScore = scoreK;
			finalScore2 = scoreK2;
			src = "Korea";
		} else if (!scoreM.equals("")) {
			finalScore = scoreM;
			finalScore2 = scoreM2;
			src = "Malaysia";
		} else if (!scoreW.equals("")) {
			finalScore = scoreW;
			finalScore2 = scoreW2;
			src = "WHMIS-SIMDUT";
		}

		String result1 = finalScore + "\t" + finalScore2 + "\t" + src;
		String result2 = scoreEU_H + "\t" + scoreEU_R + "\t" + scoreNZ + "\t" + scoreJ + "\t" + scoreA + "\t" + scoreK
				+ "\t" + scoreM + "\t" + scoreW;

		String[] results = { result1, result2 };

		return results;

	}

	String[] determineEyeIrritationScore(Vector records) {

		String score1 = "VH";
		String score2 = "H";
		String score3 = "M";

		String scoreEU_H = "";// EU H system
		String scoreEU_R = "";// EU R system
		String scoreNZ = "";// New Zealand
		String scoreA = "";// Australia
		String scoreJ = "";// Japan
		String scoreK = "";// Korea
		String scoreM = "";// Malaysia

		String scoreEU_H2 = "";// EU H system
		String scoreEU_R2 = "";// EU R system
		String scoreNZ2 = "";// New Zealand
		String scoreA2 = "";// Australia
		String scoreJ2 = "";// Japan
		String scoreK2 = "";// Korea
		String scoreM2 = "";// Malaysia

		for (int i = 0; i < records.size(); i++) {
			LinkedList<String> list = (LinkedList<String>) records.get(i);

			String CAS = list.get(0);
			String Score = list.get(1);
			String listName = list.get(2);
			String Agency = list.get(3);

			if (listName.indexOf(
					"Regulation on the Classification, Labelling and Packaging of Substances and Mixtures (CLP)") > -1) {

				if (Score.indexOf("H318") > -1) {
					scoreEU_H = score1;
				} else if (Score.indexOf("H319") > -1) {
					scoreEU_H = score2;
				} else if (Score.indexOf("H320") > -1) {
					scoreEU_H = score3;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreEU_H2 = Score;
			} else if (listName.indexOf("Substances with EU Risk & Safety Phrases") > -1) {

				if (Score.indexOf("R41 - Risk of serious damage to eyes") > -1) {
					scoreEU_R = score1;
				} else if (Score.indexOf("R36 - Irritating to eyes") > -1) {
					scoreEU_R = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreEU_R2 = Score;
			} else if (listName.indexOf("New Zealand HSNO Chemical Classifications") > -1) {

				if (Score.indexOf("8.3A - Corrosive to ocular tissue (Cat. 1)") > -1) {
					scoreNZ = score1;
				} else if (Score.indexOf("Irritating to the eye (Cat. 2A)") > -1) {
					scoreNZ = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreNZ2 = Score;
			} else if (listName.indexOf("Australia - GHS") > -1) {
				if (Score.indexOf("H318") > -1) {
					scoreA = score1;
				} else if (Score.indexOf("H319") > -1) {
					scoreA = score2;
				} else if (Score.indexOf("H320") > -1) {
					scoreA = score3;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreA2 = Score;

			} else if (listName.equals("Japan GHS Classifications")) {

				if (Score.equals("Serious eye damage / eye irritation - Category 1")) {
					scoreJ = score1;
				} else if (Score.equals("Serious eye damage / eye irritation - Category 2A")) {
					scoreJ = score2;
				} else if (Score.equals("Serious eye damage / eye irritation - Category 2B")) {
					scoreJ = score3;
				} else if (Score.equals("Serious eye damage / eye irritation - Category 2")) {
					// TODO- for now use score2
					scoreJ = score2;
					// System.out.println(CAS+"\t"+Score);
					// System.out.println(CAS+"\t"+scoreEU_H+"\t"+scoreEU_R+"\t"+scoreNZ);
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreJ2 = Score;

			} else if (listName.equals("Korea GHS Classification and Labelling for Toxic Chemicals")) {

				if (Score.indexOf("H318") > -1) {
					scoreK = score1;
				} else if (Score.indexOf("H319") > -1) {
					scoreK = score2;
				} else if (Score.indexOf("H320") > -1) {
					scoreK = score3;
					// System.out.println(CAS+"\t"+Score);
				} else {
					// System.out.println(CAS+"\t"+Score);
				}
				scoreK2 = Score;

			} else if (listName.equals("Malaysia - GHS")) {
				if (Score.indexOf("H318") > -1) {
					scoreM = score1;
					// System.out.println(CAS+"\t"+Score);
				} else if (Score.indexOf("H319") > -1) {
					scoreM = score2;
				} else if (Score.indexOf("H320") > -1) {
					scoreM = score3;
					// System.out.println(CAS+"\t"+Score);
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreM2 = Score;

			} else {
				System.out.println(CAS + "\t" + listName);
			}

		}

		String src = "";
		String finalScore = "";
		String finalScore2 = "";

		if (!scoreEU_H.equals("")) {
			finalScore = scoreEU_H;
			finalScore2 = scoreEU_H2;
			src = "EU_H";
		} else if (!scoreEU_R.equals("")) {
			finalScore = scoreEU_R;
			finalScore2 = scoreEU_R2;
			src = "EU_R";
		} else if (!scoreNZ.equals("")) {
			finalScore = scoreNZ;
			finalScore2 = scoreNZ2;
			src = "New Zealand";
		} else if (!scoreJ.equals("")) {
			finalScore = scoreJ;
			finalScore2 = scoreJ2;
			src = "Japan";
		} else if (!scoreA.equals("")) {
			finalScore = scoreA;
			finalScore2 = scoreA2;
			src = "Australia";
		} else if (!scoreK.equals("")) {
			finalScore = scoreK;
			finalScore2 = scoreK2;
			src = "Korea";
		} else if (!scoreM.equals("")) {
			finalScore = scoreM;
			finalScore2 = scoreM2;
			src = "Malaysia";
		}

		String result1 = finalScore + "\t" + finalScore2 + "\t" + src;
		String result2 = scoreEU_H + "\t" + scoreEU_R + "\t" + scoreNZ + "\t" + scoreA + "\t" + scoreJ + "\t" + scoreK
				+ "\t" + scoreM;

		String[] results = { result1, result2 };

		return results;

	}

	/**
	 * 
	 * Done H score
	 * 
	 * @param records
	 * @return
	 */
	String[] determineCancerScore(Vector records) {

		String score1 = "VH";
		String score2 = "H";
		String score3 = "M";
		String score4 = "L";
		String score5 = "VL";

		String scoreI = "";// IRIS
		String scoreEU = "";// EU
		String scoreEU_H = "";// EU H
		String scoreEU_R = "";// EU R
		String scoreR = "";// Reach
		String scoreNZ = "";// NZ
		String scoreJ = "";// Japan
		String scoreA = "";// Australia
		String scoreK = "";// Korea
		String scoreM = "";// Malaysia
		String scoreMAK_BAT = "";
		String scoreN = "";// NIOSH
		String scoreC = "";// California
		String scoreT = "";// TSCA
		String scoreROC = "";// Report on Carcinogens
		String scoreROM = "";// Restrictions On The Manufacture, Placing On The
								// Market And Use Of Certain Dangerous
								// Substances, Preparations And Articles -
								// Carcinogens, Mutagens & Reproductive
								// Toxicants
		// String scoreLBC="";//Living Building Challenge 3.0 - Red List of
		// Materials & Chemicals
		// String scoreMDH="";//minnesota dept of health

		String scoreI2 = "";// IRIS
		String scoreEU2 = "";// EU
		String scoreEU_H2 = "";// EU H
		String scoreEU_R2 = "";// EU R
		String scoreR2 = "";// Reach
		String scoreNZ2 = "";// NZ
		String scoreJ2 = "";// Japan
		String scoreA2 = "";// Australia
		String scoreK2 = "";// Korea
		String scoreM2 = "";// Malaysia
		String scoreMAK_BAT2 = "";
		String scoreN2 = "";// NIOSH
		String scoreC2 = "";// California
		String scoreT2 = "";// TSCA
		String scoreROC2 = "";// Report on Carcinogens
		String scoreROM2 = "";
		// String scoreLBC2="";//Living Building Challenge 3.0 - Red List of
		// Materials & Chemicals
		// String scoreMDH2="";//minnesota dept of health

		/*
		 * 
		 * O 0 Monographs On the Evaluation of Carcinogenic Risks to Humans X 1
		 * List of Substances with MAK & BAT Values & Categories X 2 Regulation
		 * on the Classification, Labelling and Packaging of Substances and
		 * Mixtures (CLP) Annex 6 Table 3-1 - GHS Hazard code criteria X 3
		 * Substances with EU Risk & Safety Phrases (Commission Directive
		 * 67-548-EEC) X 4 Classification, Labelling and Packaging Regulation
		 * (CLP) - Classification and Labelling Inventory - CMRs X 5 New Zealand
		 * HSNO Chemical Classifications X 6 Chemicals Known to the State to
		 * Cause Cancer or Reproductive Toxicity - California Proposition 65 -
		 * Safe Drinking Water and Toxic Enforcement Act Of 1986 X 7 NIOSH
		 * Carcinogen List X 8 Japan GHS Classifications X 9 Australia - GHS X
		 * 10 Integrated Risk Information System Database (IRIS) X 11 Risk
		 * Management Actions & TSCA Work Plans X 12 Report on Carcinogens X 13
		 * Restrictions On The Manufacture, Placing On The Market And Use Of
		 * Certain Dangerous Substances, Preparations And Articles -
		 * Carcinogens, Mutagens & Reproductive Toxicants X 14 Korea GHS
		 * Classification and Labelling for Toxic Chemicals X 15 Substances of
		 * Very High Concern for REACH Annex XIV authorisation (Article 59) X 16
		 * Malaysia - GHS O 17 Living Building Challenge 3.0 - Red List of
		 * Materials & Chemicals O 18 Minnesota Department of Health - Chemicals
		 * of High Concern and Priority Chemicals O 19 Safer Consumer Product
		 * Candidate Chemicals O 20 BIFMA - e3/level Annex B list of chemicals
		 * 
		 * //TODO- add IARC, NTP from original source!
		 * 
		 */

		for (int i = 0; i < records.size(); i++) {
			LinkedList<String> list = (LinkedList<String>) records.get(i);

			String CAS = list.get(0);
			String Score = list.get(1);
			String listName = list.get(2);
			String Agency = list.get(3);

			if (listName.equals("Integrated Risk Information System Database (IRIS)")) {

				if (Score.equals("(1986) Group A - Human Carcinogen")
						|| Score.equals("(1986) Group B1 - Probable human Carcinogen")
						|| Score.equals("(1986) Group B2 - Probable human Carcinogen")
						|| Score.equals("(1996) Known/likely human Carcinogen")
						|| Score.equals("(2005) Likely to be Carcinogenic to humans")
						|| Score.equals("(1999) Carcinogenic to humans")
						|| Score.equals("(1999) Likely to be Carcinogenic to humans")
						|| Score.equals("(2005) Carcinogenic to humans")) {
					scoreI = score1;
				} else if (Score.equals("(1986) Group C - Possible human Carcinogen")
						|| Score.equals("(1999) Suggestive evidence of carcinogenicity")
						|| Score.equals("(2005) Suggestive evidence of Carcinogenic potential")) {
					scoreI = score2;
				} else if (Score.equals("(1986) Group D - Not classifiable as to human carcinogenicity")
						|| Score.equals("(1999) Data are inadequate for an assessment of human carcinogenic potential")
						|| Score.equals("(2005) Inadequate information to assess carcinogenic potential")
						|| Score.equals("(1996) Carcinogenic potential cannot be determined")) {
					// assign score 3 or nothing? TODO
				} else if (Score.equals("(1986) Group E - Evidence of non-carcinogenicity for humans")
						|| Score.equals("(1996) Not likely to be carcinogenic to humans")
						|| Score.equals("(1999) Not likely to be Carcinogenic to humans")
						|| Score.equals("(2005) Not likely to be Carcinogenic to humans")) {
					scoreI = score4;

				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreI2 = Score;

			} else if (listName.equals("Risk Management Actions & TSCA Work Plans")) {
				if (Score.equals("Known human carcinogen - TSCA Criteria met")
						|| Score.equals("Probable human carcinogen - TSCA Criteria met")) {
					scoreT = score1;
				} else if (Score.equals("Possible carcinogen - TSCA Criteria met")
						|| Score.equals("Potential carcinogenicity to specifc target organs - TSCA Criteria met")) {
					scoreT = score2;
				} else if (Score.equals("Limited evidence of carcinogenicity - TSCA Criteria met")) {
					scoreT = score3;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreT2 = Score;

			} else if (listName.equals("NIOSH Carcinogen List")) {
				if (Score.equals("Occupational Carcinogen")) {
					scoreN = score1;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreN2 = Score;

			} else if (listName.equals(
					"Chemicals Known to the State to Cause Cancer or Reproductive Toxicity  - California Proposition 65 - Safe Drinking Water and Toxic Enforcement Act Of 1986")) {
				if (Score.equals("Carcinogen")
						|| Score.equals("Carcinogen - specific to chemical form or exposure route")) {
					scoreC = score1;
				} else if (Score.equals("Carcinogen - Delisted")) {
					scoreC = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreC2 = Score;

			} else if (listName.equals(
					"Regulation on the Classification, Labelling and Packaging of Substances and Mixtures (CLP) Annex 6 Table 3-1 - GHS Hazard  code criteria")) {

				if (Score.equals("H350 - May cause cancer") || Score.equals("H350i - May cause cancer by inhalation")) {
					scoreEU_H = score1;
				} else if (Score.equals("H351 - Suspected of causing cancer")) {
					scoreEU_H = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreEU_H2 = Score;
			} else if (listName.equals("Substances with EU Risk & Safety Phrases (Commission Directive 67-548-EEC)")) {

				if (Score.equals("R49 - May cause cancer by inhalation") || Score.equals("R45 - May cause cancer")) {
					scoreEU_R = score1;
				} else if (Score.equals("R40 - Limited Evidence of Carcinogenic Effects")) {
					scoreEU_R = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreEU_R2 = Score;

			} else if (listName.equals(
					"Classification, Labelling and Packaging Regulation (CLP) - Classification and Labelling Inventory - CMRs")) {

				if (Score.equals("Carcinogen Category 1A - Known human Carcinogen based on human evidence")
						|| Score.equals("Carcinogen Category 1B - Presumed Carcinogen based on animal evidence")) {
					scoreEU = score1;
				} else if (Score.equals("Carcinogen Category 2 - Suspected human Carcinogen")) {
					scoreEU = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreEU2 = Score;
			} else if (listName
					.equals("Substances of Very High Concern for REACH Annex XIV authorisation (Article 59)")) {

				if (Score.equals("Carcinogenic - Banned unless Authorised")) {
					scoreR = score1;
				} else if (Score.equals("Carcinogenic - Candidate list")) {
					scoreR = score2;
				} else if (Score.equals("Carcinogenic - Prioritized for listing")) {
					scoreR = score3;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreR2 = Score;

			} else if (listName.equals("New Zealand HSNO Chemical Classifications")) {
				if (Score.equals("6.7A - Known or presumed human carcinogens")
						|| Score.equals("6.7B - Suspected human carcinogens")) {
					scoreNZ = score1;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreNZ2 = Score;
			} else if (listName.equals("Japan GHS Classifications")) {
				if (Score.equals("Carcinogenicity - Category 1A") || Score.equals("Carcinogenicity - Category 1B")
						|| Score.equals("Carcinogenicity - Category 1A-1B")) {
					scoreJ = score1;
				} else if (Score.equals("Carcinogenicity - Category 2")) {
					scoreJ = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreJ2 = Score;
			} else if (listName.equals("Australia - GHS")) {
				if (Score.equals("H350 - May cause cancer") || Score.equals("H350i - May cause cancer by inhalation")) {
					scoreA = score1;
				} else if (Score.equals("H351 - Suspected of causing cancer")) {
					scoreA = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreA2 = Score;
			} else if (listName.equals("Korea GHS Classification and Labelling for Toxic Chemicals")) {

				if (Score.equals("Carcinogenicity - Category 1 [H350 - May cause cancer]")) {
					scoreK = score1;
				} else if (Score.equals("Carcinogenicity - Category 2 [H351 - Suspected of causing cancer]")) {
					scoreK = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreK2 = Score;
			} else if (listName.equals("Malaysia - GHS")) {

				if (Score.equals("H350 - May cause cancer") || Score.equals("H350i - May cause cancer by inhalation")) {
					scoreM = score1;
				} else if (Score.equals("H351 - Suspected of causing cancer")) {
					scoreM = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreM2 = Score;
			} else if (listName.equals("List of Substances with MAK & BAT Values & Categories")) {
				if (Score.equals("Carcinogen Group 1 - Substances that cause cancer in man")
						|| Score.equals("Carcinogen Group 2 - Considered to be carcinogenic for man")) {
					scoreMAK_BAT = score1;// VH
				} else if (Score
						.equals("Carcinogen Group 3A - Evidence of carcinogenic effects but not sufficient to establish MAK/BAT value")
						|| Score.equals(
								"Carcinogen Group 3B - Evidence of carcinogenic effects but not sufficient for classification")) {
					scoreMAK_BAT = score2;// H
				} else if (Score
						.equals("Carcinogen Group 4 - Non-genotoxic carcinogen with low risk under MAK/BAT levels")
						|| Score.equals(
								"Carcinogen Group 5 - Genotoxic carcinogen with very slight risk under MAK/BAT levels")) {
					scoreMAK_BAT = score3;// M
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreMAK_BAT2 = Score;
			} else if (listName.equals("Report on Carcinogens")) {
				if (Score.equals("Known to be a human Carcinogen")
						|| Score.equals("Reasonably Anticipated to be Human Carcinogen")
						|| Score.equals("Known to be Human Carcinogen (respirable size - occupational setting)")
						|| Score.equals(
								"Reasonably Anticipated to be Human Carcinogen (respirable size - occupational setting)")) {
					scoreROC = score1;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreROC2 = Score;

			} else if (listName.equals(
					"Restrictions On The Manufacture, Placing On The Market And Use Of Certain Dangerous Substances, Preparations And Articles - Carcinogens, Mutagens & Reproductive Toxicants")) {
				if (Score.equals("Carcinogen Category 1 - Substances known to be Carcinogenic to man") || Score.equals(
						"Carcinogen Category 2 - Substances which should be regarded as if they are Carcinogenic to man")) {
					scoreROM = score1;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreROM2 = Score;

			} else if (listName.equals("Living Building Challenge 3.0 - Red List of Materials & Chemicals")) {
				// do nothing, only 2 chemicals
			} else if (listName
					.equals("Minnesota Department of Health - Chemicals of High Concern and Priority Chemicals")) {
				// do nothing, only 1 chemical
			} else if (listName.equals("Safer Consumer Product Candidate Chemicals")) {
				// do nothing, only 1 chemical
			} else if (listName.equals("BIFMA - e3/level Annex B list of chemicals")) {
				// do nothing, only 1 chemical
			} else if (listName.equals("Monographs On the Evaluation of Carcinogenic Risks to Humans")) {
				System.out.println(CAS + "\t" + Score);
			} else {
				// System.out.println(CAS+"\t"+listName);
			}

		}

		String src = "";
		String finalScore = "";
		String finalScore2 = "";

		if (!scoreI.equals("")) {
			finalScore = scoreI;
			finalScore2 = scoreI2;
			src = "IRIS";
		} else if (!scoreT.equals("")) {
			finalScore = scoreT;
			finalScore2 = scoreT2;
			src = "TSCA";
		} else if (!scoreEU_H.equals("")) {
			finalScore = scoreEU_H;
			finalScore2 = scoreEU_H2;
			src = "EU_H";
		} else if (!scoreEU_R.equals("")) {
			finalScore = scoreEU_R;
			finalScore2 = scoreEU_R2;
			src = "EU_R";
		} else if (!scoreEU.equals("")) {
			finalScore = scoreEU;
			finalScore2 = scoreEU2;
			src = "EU";
		} else if (!scoreMAK_BAT.equals("")) {
			finalScore = scoreMAK_BAT;
			finalScore2 = scoreMAK_BAT2;
			src = "MAK_BAT";
		} else if (!scoreR.equals("")) {
			finalScore = scoreR;
			finalScore2 = scoreR2;
			src = "REACH";
		} else if (!scoreNZ.equals("")) {
			finalScore = scoreNZ;
			finalScore2 = scoreNZ2;
			src = "New Zealand";
		} else if (!scoreJ.equals("")) {
			finalScore = scoreJ;
			finalScore2 = scoreJ2;
			src = "Japan";
		} else if (!scoreA.equals("")) {
			finalScore = scoreA;
			finalScore2 = scoreA2;
			src = "Australia";
		} else if (!scoreK.equals("")) {
			finalScore = scoreK;
			finalScore2 = scoreK2;
			src = "Korea";
		} else if (!scoreM.equals("")) {
			finalScore = scoreM;
			finalScore2 = scoreM2;
			src = "Malaysia";
		} else if (!scoreROC.equals("")) {
			finalScore = scoreROC;
			finalScore2 = scoreROC2;
			src = "Report on Carcinogens";
		} else if (!scoreROM.equals("")) {
			finalScore = scoreROM;
			finalScore2 = scoreROM2;
			src = "Restrictions On The Manufacture";
		} else if (!scoreN.equals("")) {
			finalScore = scoreN;
			finalScore2 = scoreN2;
			src = "NIOSH";
		} else if (!scoreC.equals("")) {
			finalScore = scoreC;
			finalScore2 = scoreC2;
			src = "California";
		}

		String result1 = finalScore + "\t" + finalScore2 + "\t" + src;
		String result2 = scoreI + "\t" + scoreT + "\t" + scoreEU_H + "\t" + scoreEU_R + "\t" + scoreEU + "\t"
				+ scoreMAK_BAT + "\t" + scoreR + "\t" + scoreNZ + "\t" + scoreJ + "\t" + scoreA + "\t" + scoreK + "\t"
				+ scoreM + "\t" + scoreROC + "\t" + scoreROM + "\t" + scoreN + "\t" + scoreC;

		String[] results = { result1, result2 };

		return results;

	}

	String[] determineGeneDamageScore(Vector records) {

		String score1 = "VH";
		String score2 = "H";
		String score3 = "M";
		String score4 = "L";
		String score5 = "VL";

		String scoreEU_H = "";// EU_H
		String scoreEU_R = "";// EU_R
		String scoreEU_CMR = "";// EU_CMR
		String scoreT = "";// TSCA
		String scoreR = "";// REACH
		String scoreNZ = "";// NZ
		String scoreJ = "";// Japan
		String scoreA = "";// Australia
		String scoreM = "";// Malaysia
		String scoreK = "";// Korea
		String scoreMAK_BAT = "";// Korea
		String scoreROM = "";// Restrictions On The Manufacture

		String scoreEU_H2 = "";// EU_H
		String scoreEU_R2 = "";// EU_R
		String scoreEU_CMR2 = "";// EU_CMR
		String scoreT2 = "";// TSCA
		String scoreR2 = "";// REACH
		String scoreNZ2 = "";// NZ
		String scoreJ2 = "";// Japan
		String scoreA2 = "";// Australia
		String scoreM2 = "";// Malaysia
		String scoreK2 = "";// Korea
		String scoreMAK_BAT2 = "";// Korea
		String scoreROM2 = "";

		String listEU_H = "Regulation on the Classification, Labelling and Packaging of Substances and Mixtures (CLP) Annex 6 Table 3-1 - GHS Hazard  code criteria";
		String listEU_R = "Substances with EU Risk & Safety Phrases (Commission Directive 67-548-EEC)";
		String listEU_CMR = "Classification, Labelling and Packaging Regulation (CLP) - Classification and Labelling Inventory - CMRs";
		String listR = "Substances of Very High Concern for REACH Annex XIV authorisation (Article 59)";
		String listT = "Risk Management Actions & TSCA Work Plans";
		String listNZ = "New Zealand HSNO Chemical Classifications";
		String listJ = "Japan GHS Classifications";
		String listK = "Korea GHS Classification and Labelling for Toxic Chemicals";
		String listA = "Australia - GHS";
		String listM = "Malaysia - GHS";
		String listMAK_BAT = "List of Substances with MAK & BAT Values & Categories";
		String listROM = "Restrictions On The Manufacture, Placing On The Market And Use Of Certain Dangerous Substances, Preparations And Articles - Carcinogens, Mutagens & Reproductive Toxicants";

		for (int i = 0; i < records.size(); i++) {
			LinkedList<String> list = (LinkedList<String>) records.get(i);

			String CAS = list.get(0);
			String Score = list.get(1);
			String listName = list.get(2);
			String Agency = list.get(3);

			if (listName.equals(listEU_H)) {
				if (Score.equals("H340 - May cause genetic defects")) {
					scoreEU_H = score1;
				} else if (Score.equals("H341 - Suspected of causing genetic defects")) {
					scoreEU_H = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreEU_H2 = Score;
			} else if (listName.equals(listEU_R)) {
				if (Score.equals("R46 - May cause heritable genetic damage")) {
					scoreEU_R = score1;
				} else if (Score.equals("R68 - May cause irreversible effects")) {
					scoreEU_R = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreEU_R2 = Score;

			} else if (listName.equals(listEU_CMR)) {
				if (Score.equals("Mutagen - Category 1B") || Score.equals("Mutagen - Category 2")) {
					scoreEU_CMR = score1;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreEU_CMR2 = Score;

			} else if (listName.equals(listR)) {

				if (Score.equals("Mutagenic - Banned unless Authorised")
						|| Score.equals("Mutagenic - Candidate list")) {
					scoreR = score1;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreR2 = Score;

			} else if (listName.equals(listT)) {
				// only 4 compounds
				if (Score.equals("Mutagenicity - TSCA Criteria met")) {
					scoreT = score1;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreT2 = Score;
			} else if (listName.equals(listNZ)) {
				if (Score.equals("6.6A - Known or presumed human mutagens")) {
					scoreNZ = score1;
				} else if (Score.equals("6.6B - Suspected human mutagens")) {
					scoreNZ = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreNZ2 = Score;
			} else if (listName.equals(listJ)) {
				// TODO- are these human mutagenicity scores???
				// if (Score.equals("Germ cell mutagenicity - Category 1B") ||
				// Score.equals("Germ cell mutagenicity - Category 2")) {
				// scoreJ=score1;
				if (Score.equals("Germ cell mutagenicity - Category 1B")) {
					scoreJ = score1;
				} else if (Score.equals("Germ cell mutagenicity - Category 2")) {
					scoreJ = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreJ2 = Score;

			} else if (listName.equals(listK)) {
				// TODO- are these human mutagenicity scores???
				if (Score.equals("Germ cell mutagenicity - Category 1 [H340 - May cause genetic defects]")) {
					scoreK = score1;
				} else if (Score
						.equals("Germ cell mutagenicity - Category 2 [H341 - Suspected of causing genetic defects]")) {
					scoreK = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreK2 = Score;

			} else if (listName.equals(listA)) {
				// TODO- are these human mutagenicity scores???
				if (Score.equals("H340 - May cause genetic defects")) {
					scoreA = score1;
				} else if (Score.equals("H341 - Suspected of causing genetic defects")) {
					scoreA = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreA2 = Score;
			} else if (listName.equals(listM)) {

				if (Score.equals("H340 - May cause genetic defects")) {
					scoreM = score1;
				} else if (Score.equals("H341 - Suspected of causing genetic defects")) {
					scoreM = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreM2 = Score;

			} else if (listName.equals(listMAK_BAT)) {
				// 1. Germ cell mutagens which have been shown to increase the
				// mutant frequency in the progeny of exposed humans
				// 2. Germ cell mutagens which have been shown to increase the
				// mutant frequency in the progeny of exposed mammals
				// 3A. Substances which have been shown to induce genetic damage
				// in germ cells of humans or animals, or which are mutagenic in
				// somatic cells and have been shown to reach the germ cells in
				// their active forms
				// 3B. Substances which are suspected of being germ cell
				// mutagens because of their genotoxic effects in mammalian
				// somatic cells in vivo or, in exceptional cases, in the
				// absence of in vivo data if they are clearly mutagenic in
				// vitro and structurally related to in vivo mutagens
				// 4. Not applicable *
				// 5. Germ cell mutagens, the potency of which is considered to
				// be so low that, provided the MAK value is observed, their
				// contribution to genetic risk is expected not to be
				// significant

				if (Score.equals("Germ Cell Mutagen 2")) {
					scoreMAK_BAT = score1;
				} else if (Score.equals("Germ Cell Mutagen 3a") || Score.equals("Germ Cell Mutagen 3b")) {
					scoreMAK_BAT = score2;
				} else if (Score.equals("Germ Cell Mutagen 5")) {
					scoreMAK_BAT = score4;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreMAK_BAT2 = Score;
			} else if (listName.equals(listROM)) {
				if (Score.equals(
						"Mutagen Category 2 - Substances which should be regarded as if they are Mutagenic to man")) {
					scoreROM = score1;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreROM2 = Score;

			} else {
				System.out.println(CAS + "\t" + listName);
			}

		}

		String src = "";
		String finalScore = "";
		String finalScore2 = "";

		if (!scoreEU_H.equals("")) {
			finalScore = scoreEU_H;
			finalScore2 = scoreEU_H2;
			src = listEU_H;
		} else if (!scoreEU_R.equals("")) {
			finalScore = scoreEU_R;
			finalScore2 = scoreEU_R2;
			src = listEU_R;
		} else if (!scoreEU_CMR.equals("")) {
			finalScore = scoreEU_CMR;
			finalScore2 = scoreEU_CMR2;
			src = listEU_CMR;
		} else if (!scoreR.equals("")) {
			finalScore = scoreR;
			finalScore2 = scoreR2;
			src = listR;
		} else if (!scoreMAK_BAT.equals("")) {
			finalScore = scoreMAK_BAT;
			finalScore2 = scoreMAK_BAT2;
			src = listMAK_BAT;
		} else if (!scoreROM.equals("")) {
			finalScore = scoreROM;
			finalScore2 = scoreROM2;
			src = listROM;
		} else if (!scoreT.equals("")) {
			finalScore = scoreT;
			finalScore2 = scoreT2;
			src = listT;
		} else if (!scoreNZ.equals("")) {
			finalScore = scoreNZ;
			finalScore2 = scoreNZ2;
			src = listNZ;
		} else if (!scoreJ.equals("")) {
			finalScore = scoreJ;
			finalScore2 = scoreJ2;
			src = listJ;
		} else if (!scoreA.equals("")) {
			finalScore = scoreA;
			finalScore2 = scoreA2;
			src = listA;
		} else if (!scoreK.equals("")) {
			finalScore = scoreK;
			finalScore2 = scoreK2;
			src = listK;
		} else if (!scoreM.equals("")) {
			finalScore = scoreM;
			finalScore2 = scoreM2;
			src = listM;
		}

		String result1 = finalScore + "\t" + finalScore2 + "\t" + src;
		String result2 = scoreEU_H + "\t" + scoreEU_R + "\t" + scoreEU_CMR + "\t" + scoreR + "\t" + scoreMAK_BAT + "\t"
				+ scoreROM + "\t" + scoreT + "\t" + scoreNZ + "\t" + scoreJ + "\t" + scoreA + "\t" + scoreK + "\t"
				+ scoreM;

		String[] results = { result1, result2 };

		return results;

	}

	String[] determineReproductiveHarmScore(Vector records) {

		//
		// String score1="VH";
		// String score2="H";
		// String score3="M";
		// String score4="L";
		// String score5="VL";
		//
		// String scoreEU_H="";//EU_H
		// String scoreEU_R="";//EU_R
		// String scoreEU_CMR="";//EU_CMR
		// String scoreT="";//TSCA
		// String scoreR="";//REACH
		// String scoreNZ="";//NZ
		// String scoreJ="";//Japan
		// String scoreA="";//Australia
		// String scoreM="";//Malaysia
		// String scoreK="";//Korea
		// String scoreMAK_BAT="";//Korea
		// String scoreROM="";//Restrictions On The Manufacture
		//
		// String scoreEU_H2="";//EU_H
		// String scoreEU_R2="";//EU_R
		// String scoreEU_CMR2="";//EU_CMR
		// String scoreT2="";//TSCA
		// String scoreR2="";//REACH
		// String scoreNZ2="";//NZ
		// String scoreJ2="";//Japan
		// String scoreA2="";//Australia
		// String scoreM2="";//Malaysia
		// String scoreK2="";//Korea
		// String scoreMAK_BAT2="";//Korea
		// String scoreROM2="";
		//
		//
		// String listEU_H="Regulation on the Classification, Labelling and
		// Packaging of Substances and Mixtures (CLP) Annex 6 Table 3-1 - GHS
		// Hazard code criteria";
		// String listEU_R="Substances with EU Risk & Safety Phrases (Commission
		// Directive 67-548-EEC)";
		// String listEU_CMR="Classification, Labelling and Packaging Regulation
		// (CLP) - Classification and Labelling Inventory - CMRs";
		// String listR="Substances of Very High Concern for REACH Annex XIV
		// authorisation (Article 59)";
		// String listT="Risk Management Actions & TSCA Work Plans";
		// String listNZ="New Zealand HSNO Chemical Classifications";
		// String listJ="Japan GHS Classifications";
		// String listK="Korea GHS Classification and Labelling for Toxic
		// Chemicals";
		// String listA="Australia - GHS";
		// String listM="Malaysia - GHS";
		// String listMAK_BAT="List of Substances with MAK & BAT Values &
		// Categories";
		// String listROM="Restrictions On The Manufacture, Placing On The
		// Market And Use Of Certain Dangerous Substances, Preparations And
		// Articles - Carcinogens, Mutagens & Reproductive Toxicants";
		//
		// for (int i=0;i<records.size();i++) {
		// LinkedList<String>list=(LinkedList<String>)records.get(i);
		//
		// String CAS=list.get(0);
		// String Score=list.get(1);
		// String listName=list.get(2);
		// String Agency=list.get(3);
		//
		// if (listName.equals(listEU_H)) {
		// if (Score.equals("H340 - May cause genetic defects")) {
		// scoreEU_H=score1;
		// } else if (Score.equals("H341 - Suspected of causing genetic
		// defects")) {
		// scoreEU_H=score2;
		// } else {
		// System.out.println(CAS+"\t"+Score);
		// }
		// scoreEU_H2=Score;
		// } else if (listName.equals(listEU_R)) {
		// if (Score.equals("R46 - May cause heritable genetic damage")) {
		// scoreEU_R=score1;
		// } else if (Score.equals("R68 - May cause irreversible effects")) {
		// scoreEU_R=score2;
		// } else {
		//// System.out.println(CAS+"\t"+Score);
		// }
		// scoreEU_R2=Score;
		//
		// } else if (listName.equals(listEU_CMR)) {
		// if (Score.equals("Mutagen - Category 1B") || Score.equals("Mutagen -
		// Category 2")) {
		// scoreEU_CMR=score1;
		// } else {
		//// System.out.println(CAS+"\t"+Score);
		// }
		// scoreEU_CMR2=Score;
		//
		// } else if (listName.equals(listR)) {
		//
		// if (Score.equals("Mutagenic - Banned unless Authorised") ||
		// Score.equals("Mutagenic - Candidate list")) {
		// scoreR=score1;
		// } else {
		//// System.out.println(CAS+"\t"+Score);
		// }
		// scoreR2=Score;
		//
		// } else if (listName.equals(listT)) {
		// //only 4 compounds
		// if (Score.equals("Mutagenicity - TSCA Criteria met")) {
		// scoreT=score1;
		// } else {
		//// System.out.println(CAS+"\t"+Score);
		// }
		// scoreT2=Score;
		// } else if (listName.equals(listNZ)) {
		// if (Score.equals("6.6A - Known or presumed human mutagens")) {
		// scoreNZ=score1;
		// } else if (Score.equals("6.6B - Suspected human mutagens")) {
		// scoreNZ=score2;
		// } else {
		//// System.out.println(CAS+"\t"+Score);
		// }
		// scoreNZ2=Score;
		// } else if (listName.equals(listJ)) {
		// //TODO- are these human mutagenicity scores???
		// if (Score.equals("Germ cell mutagenicity - Category 1B") ||
		// Score.equals("Germ cell mutagenicity - Category 2")) {
		// scoreJ=score1;
		// } else {
		//// System.out.println(CAS+"\t"+Score);
		// }
		// scoreJ2=Score;
		//
		// } else if (listName.equals(listK)) {
		// //TODO- are these human mutagenicity scores???
		// if (Score.equals("Germ cell mutagenicity - Category 1 [H340 - May
		// cause genetic defects]")) {
		// scoreK=score1;
		// } else if (Score.equals("Germ cell mutagenicity - Category 2 [H341 -
		// Suspected of causing genetic defects]")) {
		// scoreK=score2;
		// } else {
		//// System.out.println(CAS+"\t"+Score);
		// }
		// scoreK2=Score;
		//
		// } else if (listName.equals(listA)) {
		// //TODO- are these human mutagenicity scores???
		// if (Score.equals("H340 - May cause genetic defects")) {
		// scoreA=score1;
		// } else if (Score.equals("H341 - Suspected of causing genetic
		// defects")) {
		// scoreA=score2;
		// } else {
		//// System.out.println(CAS+"\t"+Score);
		// }
		// scoreA2=Score;
		// } else if (listName.equals(listM)) {
		//
		// if (Score.equals("H340 - May cause genetic defects")) {
		// scoreM=score1;
		// } else if (Score.equals("H341 - Suspected of causing genetic
		// defects")) {
		// scoreM=score2;
		// } else {
		//// System.out.println(CAS+"\t"+Score);
		// }
		// scoreM2=Score;
		//
		// } else if (listName.equals(listMAK_BAT)) {
		//// 1. Germ cell mutagens which have been shown to increase the mutant
		// frequency in the progeny of exposed humans
		//// 2. Germ cell mutagens which have been shown to increase the mutant
		// frequency in the progeny of exposed mammals
		//// 3A. Substances which have been shown to induce genetic damage in
		// germ cells of humans or animals, or which are mutagenic in somatic
		// cells and have been shown to reach the germ cells in their active
		// forms
		//// 3B. Substances which are suspected of being germ cell mutagens
		// because of their genotoxic effects in mammalian somatic cells in vivo
		// or, in exceptional cases, in the absence of in vivo data if they are
		// clearly mutagenic in vitro and structurally related to in vivo
		// mutagens
		//// 4. Not applicable *
		//// 5. Germ cell mutagens, the potency of which is considered to be so
		// low that, provided the MAK value is observed, their contribution to
		// genetic risk is expected not to be significant
		//
		// if (Score.equals("Germ Cell Mutagen 2")) {
		// scoreMAK_BAT=score1;
		// } else if (Score.equals("Germ Cell Mutagen 3a") || Score.equals("Germ
		// Cell Mutagen 3b")) {
		// scoreMAK_BAT=score2;
		// } else if (Score.equals("Germ Cell Mutagen 5")) {
		// scoreMAK_BAT=score4;
		// } else {
		//// System.out.println(CAS+"\t"+Score);
		// }
		// scoreMAK_BAT2=Score;
		// } else if (listName.equals(listROM)) {
		// if (Score.equals("Mutagen Category 2 - Substances which should be
		// regarded as if they are Mutagenic to man")) {
		// scoreROM=score1;
		// } else {
		//// System.out.println(CAS+"\t"+Score);
		// }
		// scoreROM2=Score;
		//
		// } else {
		//// System.out.println(CAS+"\t"+listName);
		// }
		//
		// }
		//
		// String src="";
		// String finalScore="";
		// String finalScore2="";
		//
		// if (!scoreEU_H.equals("")) {
		// finalScore=scoreEU_H;
		// finalScore2=scoreEU_H2;
		// src=listEU_H;
		// } else if (!scoreEU_R.equals("")) {
		// finalScore=scoreEU_R;
		// finalScore2=scoreEU_R2;
		// src=listEU_R;
		// } else if (!scoreEU_CMR.equals("")) {
		// finalScore=scoreEU_CMR;
		// finalScore2=scoreEU_CMR2;
		// src=listEU_CMR;
		// } else if (!scoreR.equals("")) {
		// finalScore=scoreR;
		// finalScore2=scoreR2;
		// src=listR;
		// } else if (!scoreMAK_BAT.equals("")) {
		// finalScore=scoreMAK_BAT;
		// finalScore2=scoreMAK_BAT2;
		// src=listMAK_BAT;
		// } else if (!scoreROM.equals("")) {
		// finalScore=scoreROM;
		// finalScore2=scoreROM2;
		// src=listROM;
		// } else if (!scoreT.equals("")) {
		// finalScore=scoreT;
		// finalScore2=scoreT2;
		// src=listT;
		// } else if (!scoreNZ.equals("")) {
		// finalScore=scoreNZ;
		// finalScore2=scoreNZ2;
		// src=listNZ;
		// } else if (!scoreJ.equals("")) {
		// finalScore=scoreJ;
		// finalScore2=scoreJ2;
		// src=listJ;
		// } else if (!scoreA.equals("")) {
		// finalScore=scoreA;
		// finalScore2=scoreA2;
		// src=listA;
		// } else if (!scoreK.equals("")) {
		// finalScore=scoreK;
		// finalScore2=scoreK2;
		// src=listK;
		// } else if (!scoreM.equals("")) {
		// finalScore=scoreM;
		// finalScore2=scoreM2;
		// src=listM;
		// }
		//
		//
		//
		// String result1=finalScore+"\t"+finalScore2+"\t"+src;
		// String
		// result2=scoreEU_H+"\t"+scoreEU_R+"\t"+scoreEU_CMR+"\t"+scoreR+"\t"+scoreMAK_BAT+"\t"+
		// scoreROM+"\t"+scoreT+"\t"+scoreNZ+"\t"+scoreJ+"\t"+scoreA+"\t"+scoreK+"\t"+scoreM;
		//
		// String [] results={result1,result2};
		//
		// return results;

		return null;
		//
		//
	}

	
	/**
	 * Done H score
	 * 
	 * Need to fix EU_R
	 * 
	 * @param records
	 * @return
	 */
	String[] determineAcuteAquaticToxScore(Vector records) {

		String score1 = "VH";
		String score2 = "H";
		String score3 = "M";
		String score4 = "L";
		String score5 = "VL";

		String scoreEU_H = "";// EU_H
		String scoreEU_R = "";// EU_R
		String scoreT = "";// TSCA
		String scoreNZ = "";// NZ
		String scoreJ = "";// Japan
		String scoreA = "";// Australia
		String scoreM = "";// Malaysia
		String scoreK = "";// Korea

		String scoreEU_H2 = "";// EU_H
		String scoreEU_R2 = "";// EU_R
		String scoreT2 = "";// TSCA
		String scoreNZ2 = "";// NZ
		String scoreJ2 = "";// Japan
		String scoreA2 = "";// Australia
		String scoreM2 = "";// Malaysia
		String scoreK2 = "";// Korea

		String listEU_H = "Regulation on the Classification, Labelling and Packaging of Substances and Mixtures (CLP) Annex 6 Table 3-1 - GHS Hazard  code criteria";
		String listEU_R = "Substances with EU Risk & Safety Phrases (Commission Directive 67-548-EEC)";
		String listT = "Risk Management Actions & TSCA Work Plans";
		String listNZ = "New Zealand HSNO Chemical Classifications";
		String listJ = "Japan GHS Classifications";
		String listK = "Korea GHS Classification and Labelling for Toxic Chemicals";
		String listA = "Australia - GHS";
		String listM = "Malaysia - GHS";

		// X 0 Substances with EU Risk & Safety Phrases (Commission Directive
		// 67-548-EEC)
		// X 1 Japan GHS Classifications
		// X 2 New Zealand HSNO Chemical Classifications
		// ~ 3 Regulation on the Classification, Labelling and Packaging of
		// Substances and Mixtures (CLP) Annex 6 Table 3-1 - GHS Hazard code
		// criteria
		// X 4 Korea GHS Classification and Labelling for Toxic Chemicals
		// X 5 Malaysia - GHS
		// X 6 Australia - GHS
		// X 7 Risk Management Actions & TSCA Work Plans

		for (int i = 0; i < records.size(); i++) {
			LinkedList<String> list = (LinkedList<String>) records.get(i);

			String CAS = list.get(0);
			String Score = list.get(1);
			String listName = list.get(2);
			String Agency = list.get(3);

			if (listName.equals(listEU_H)) {
				if (Score.indexOf("H400 - Very toxic to aquatic life") > -1) {
					scoreEU_H = score1;
					// System.out.println(CAS);
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreEU_H2 = Score;
			} else if (listName.equals(listEU_R)) {
				if (Score.equals("R50 - Very Toxic to Aquatic Organisms")) {
					scoreEU_R = score1;
				} else if (Score.equals("R51 - Toxic to Aquatic Organisms")) {
					scoreEU_R = score2;// TODO- need to find chart that tells
										// you what concentration range R51
										// corresponds to!
				} else if (Score.equals("R52 - Harmful to Aquatic Organisms")) {
					scoreEU_R = score3;// TODO
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreEU_R2 = Score;

			} else if (listName.equals(listT)) {
				// only 4 compounds
				if (Score.equals("Acute Aquatic toxicity - TSCA Criteria met")) {
					scoreT = score1;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreT2 = Score;
			} else if (listName.equals(listNZ)) {

				if (Score.equals("9.1A (fish) - Very ecotoxic in the aquatic environment")) {
					scoreNZ = score1;
				} else if (Score.equals(
						"9.1D (fish) - Slightly harmful in the aquatic environment or are otherwise designed for biocidal action")) {
					scoreNZ = score2;
				} else {
					if (Score.indexOf("fish") > -1)
						System.out.println(CAS + "\t" + Score);
				}

				scoreNZ2 = Score;
			} else if (listName.equals(listJ)) {
				if (Score.equals("Hazardous to the aquatic environment (acute) - Category 1")) {
					scoreJ = score1;
				} else if (Score.equals("Hazardous to the aquatic environment (acute) - Category 2")) {
					scoreJ = score2;
				} else if (Score.equals("Hazardous to the aquatic environment (acute) - Category 3")) {
					scoreJ = score3;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreJ2 = Score;

			} else if (listName.equals(listK)) {
				// TODO- are these human mutagenicity scores???
				if (Score.indexOf("H400") > -1) {
					scoreK = score1;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreK2 = Score;

			} else if (listName.equals(listA)) {
				// TODO- are these human mutagenicity scores???
				if (Score.indexOf("H400") > -1) {
					scoreA = score1;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreA2 = Score;
			} else if (listName.equals(listM)) {

				if (Score.indexOf("H400") > -1) {
					scoreM = score1;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreM2 = Score;

			} else {
				// System.out.println(CAS+"\t"+listName);
			}

		}

		String src = "";
		String finalScore = "";
		String finalScore2 = "";

		if (!scoreEU_H.equals("")) {
			finalScore = scoreEU_H;
			finalScore2 = scoreEU_H2;
			src = listEU_H;
		} else if (!scoreEU_R.equals("")) {
			finalScore = scoreEU_R;
			finalScore2 = scoreEU_R2;
			src = listEU_R;
		} else if (!scoreT.equals("")) {
			finalScore = scoreT;
			finalScore2 = scoreT2;
			src = listT;
		} else if (!scoreNZ.equals("")) {
			finalScore = scoreNZ;
			finalScore2 = scoreNZ2;
			src = listNZ;
		} else if (!scoreJ.equals("")) {
			finalScore = scoreJ;
			finalScore2 = scoreJ2;
			src = listJ;
		} else if (!scoreA.equals("")) {
			finalScore = scoreA;
			finalScore2 = scoreA2;
			src = listA;
		} else if (!scoreK.equals("")) {
			finalScore = scoreK;
			finalScore2 = scoreK2;
			src = listK;
		} else if (!scoreM.equals("")) {
			finalScore = scoreM;
			finalScore2 = scoreM2;
			src = listM;
		}

		String result1 = finalScore + "\t" + finalScore2 + "\t" + src;
		String result2 = scoreEU_H + "\t" + scoreEU_R + "\t" + scoreT + "\t" + scoreNZ + "\t" + scoreJ + "\t" + scoreA
				+ "\t" + scoreK + "\t" + scoreM;

		String[] results = { result1, result2 };

		return results;

		//
	}

	String[] determineBirthDefectsScore(Vector records) {
		// TODO assign scores (1-5) for each score for each list

		String score1 = "VH";
		String score2 = "H";
		String score3 = "M";
		String score4 = "L";
		String score5 = "VL";

		String scoreEU_H = "";
		String scoreCA = "";
		String scoreB = "";

		String listEU_H = "Regulation on the Classification, Labelling and Packaging of Substances and Mixtures (CLP) Annex 6 Table 3-1 - GHS Hazard  code criteria";
		String listEU_R = "Substances with EU Risk & Safety Phrases (Commission Directive 67-548-EEC)";
		String listAU = "Australia - GHS";
		String listCA = "Chemicals Known to the State to Cause Cancer or Reproductive Toxicity  - California Proposition 65 - Safe Drinking Water and Toxic Enforcement Act Of 1986";
		String listMAK_BAK = "List of Substances with MAK & BAT Values & Categories";
		String listTSCA = "Risk Management Actions & TSCA Work Plans";
		String listDevNeurotox = "Developmental neurotoxicity of industrial chemicals, List of 201 Chemicals known to be neurotoxic in man";
		String listM = "Malaysia - GHS";
		String listE = "Expert Panel Reports & Monographs on Reproductive and Developmental Toxicity";
		String listK = "Korea GHS Classification and Labelling for Toxic Chemicals";
		String listB = "Chemicals with occupational exposure standards based on nervous system effects (Boyes 2001)";

		for (int i = 0; i < records.size(); i++) {
			LinkedList<String> list = (LinkedList<String>) records.get(i);

			String CAS = list.get(0);
			String Score = list.get(1);
			String listName = list.get(2);
			String Agency = list.get(3);

			if (listName.equals(listEU_H)) {
				if (Score.equals("H362 - May cause harm to breast-fed children")) {

				} else if (Score.equals("H361 - Suspected of damaging fertility or the unborn child")) {

				} else if (Score.equals("H361d - Suspected of damaging the unborn child")) {

				} else if (Score.equals("H360Df - May damage the unborn child. Suspected of damaging fertility")) {

				} else if (Score.equals("H360D - May damage the unborn child")) {

				} else {
					System.out.println(CAS + "\t" + Score + "*");
				}

			} else if (listName.equals(listEU_R)) {
				if (Score.equals("R64 - May cause harm to breastfed babies")) {

				} else if (Score.equals("R63 - Possible risk of harm to the unborn child")) {

				} else if (Score.equals("R61 - May cause harm to the unborn child")) {

				} else {
					System.out.println(CAS + "\t" + Score + "*");
				}

			} else if (listName.equals(listAU)) {

				if (Score.equals("H362 - May cause harm to breast-fed children")) {

				} else if (Score.equals("H361 - Suspected of damaging fertility or the unborn child")) {

				} else if (Score.equals("H361d - Suspected of damaging the unborn child")) {

				} else if (Score.equals("H360Df - May damage the unborn child. Suspected of damaging fertility")) {

				} else if (Score.equals("H360D - May damage the unborn child")) {

				} else {
					System.out.println(CAS + "\t" + Score + "*");
				}

			} else if (listName.equals(listM)) {

				if (Score.equals("H362 - May cause harm to breast-fed children")) {

				} else if (Score.equals("H361 - Suspected of damaging fertility or the unborn child")) {

				} else if (Score.equals("H361d - Suspected of damaging the unborn child")) {

				} else if (Score.equals("H360Df - May damage the unborn child. Suspected of damaging fertility")) {

				} else if (Score.equals("H360D - May damage the unborn child")) {

				} else {
					System.out.println(CAS + "\t" + Score + "*");
				}

			} else if (listName.equals(listK)) {

				if (Score.equals("H362 - May cause harm to breast-fed children")
						|| Score.equals("H362: May cause harm to breast-fed children")) {

				} else if (Score.equals("H361 - Suspected of damaging fertility or the unborn child")) {

				} else if (Score.equals("H361d - Suspected of damaging the unborn child")) {

				} else if (Score.equals("H360Df - May damage the unborn child. Suspected of damaging fertility")) {

				} else if (Score.equals("H360D - May damage the unborn child")) {

				} else {
					System.out.println(CAS + "\t" + Score + "*");
				}

			} else if (listName.equals(listB)) {

				if (Score.equals("Developmental Neurotoxicity")) {
					scoreB = score2;
				} else {
					System.out.println(CAS + "\t" + Score + "***");
				}

			} else if (listName.equals(listE)) {

				if (Score.equals("Clear Evidence of Adverse Effects - Developmental Toxicity")) {

				} else if (Score.equals("Some Evidence of Adverse Effects - Developmental Toxicity")) {

				} else if (Score.equals("Limited Evidence of Adverse Effects- Developmental Toxicity")) {

				} else if (Score.equals("Developmental Toxicity - Nominated for study")) {

				} else if (Score.equals("Clear Evidence of no Adverse Effects - Developmental Toxicity")) {

				} else if (Score.equals("Insufficient Evidence for a Conclusion - Developmental Toxicity")) {

				} else {
					System.out.println(CAS + "\t" + Score + "***");
				}

			} else if (listName.equals(listCA)) {
				if (Score.indexOf("Developmental toxicity") > -1) {
					scoreCA = score2;
					// System.out.println(CAS);
				} else if (Score.equals("Developmental - specific to chemical form or exposure route")) {

				} else {
					System.out.println(CAS + "\t" + Score + "*");
				}
				scoreCA = Score;
			} else if (listName.equals(listMAK_BAK)) {
				if (Score.equals("Pregnancy Risk Group B")) {

				} else if (Score.equals("Pregnancy Risk Group C")) {

				} else if (Score.equals("Pregnancy Risk Group D")) {

				} else {
					System.out.println(CAS + "\t" + Score + "*");
				}

			} else if (listName.equals(listTSCA)) {

				if (Score.equals("Developmental toxicity - TSCA Criteria met")) {

				} else if (Score.equals("Developmental Effects")) {

				} else {
					System.out.println(CAS + "\t" + Score + "*");
				}

			} else if (listName.equals(listDevNeurotox)) {
				if (Score.equals("Developmental Neurotoxicant")) {

				} else if (Score.equals("Developmental neurotoxicant (2014)")) {

				} else {
					System.out.println(CAS + "\t" + Score + "*");
				}
			} else {
				System.out.println(CAS + "\t" + listName);
			}

		}

		String src = "";
		String finalScore = "";
		String finalScore2 = "";

		// if (!scoreCEPA.equals("")) {
		// finalScore=scoreCEPA;
		// finalScore2=scoreCEPA2;
		// src=listCEPA;
		// } else if (!scoreEU_R.equals("")) {
		// finalScore=scoreEU_R;
		// finalScore2=scoreEU_R2;
		// src=listEU_R;
		// } else if (!scoreT.equals("")) {
		// finalScore=scoreT;
		// finalScore2=scoreT2;
		// src=listT;
		// } else if (!scoreESIS.equals("")) {
		// finalScore=scoreESIS;
		// finalScore2=scoreESIS2;
		// src=listESIS;
		// } else if (!scoreSIN.equals("")) {
		// finalScore=scoreSIN;
		// finalScore2=scoreSIN2;
		// src=listSIN;
		// } else if (!scoreA.equals("")) {
		// finalScore=scoreA;
		// finalScore2=scoreA2;
		// src=listA;
		// } else if (!scoreK.equals("")) {
		// finalScore=scoreK;
		// finalScore2=scoreK2;
		// src=listK;
		// } else if (!scoreM.equals("")) {
		// finalScore=scoreM;
		// finalScore2=scoreM2;
		// src=listM;
		// }

		String result1 = finalScore + "\t" + finalScore2 + "\t" + src;

		// String
		// result2=scoreCEPA+"\t"+scoreEU_R+"\t"+scoreT+"\t"+scoreESIS+"\t"+scoreSIN+"\t"+scoreA+
		// "\t"+scoreK+"\t"+scoreM;

		String result2 = scoreCA;

		String[] results = { result1, result2 };

		return results;

		//
	}

	String[] determinePersistentScore(Vector records) {

		// TODO finish this method

		String score1 = "VH";
		String score2 = "H";
		String score3 = "M";
		String score4 = "L";
		String score5 = "VL";

		String scoreCEPA = "";// EU_H
		String scoreEU_R = "";// EU_R
		String scoreT = "";// TSCA

		String scoreESIS = "";// ESIS
		String scoreSIN = "";
		String scoreA = "";// Australia
		String scoreM = "";// Malaysia
		String scoreK = "";// Korea

		String scoreCEPA2 = "";// EU_H
		String scoreEU_R2 = "";// EU_R
		String scoreT2 = "";// TSCA

		String scoreESIS2 = "";
		String scoreSIN2 = "";// Japan
		String scoreA2 = "";// Australia
		String scoreM2 = "";// Malaysia
		String scoreK2 = "";// Korea

		String listCEPA = "Canadian Environmental Protection Act (CEPA) - Environmental Registry - Domestic Substances List (DSL)";
		String listT = "Risk Management Actions & TSCA Work Plans";
		String listESIS = "European chemical Substances Information System (ESIS) - PBT List";
		String listSIN = "SIN (Substitute It Now) List";

		String listK = "Korea GHS Classification and Labelling for Toxic Chemicals";
		String listA = "Australia - GHS";
		String listM = "Malaysia - GHS";

		// X 0 Canadian Environmental Protection Act (CEPA) - Environmental
		// Registry - Domestic Substances List (DSL)
		// X 1 Risk Management Actions & TSCA Work Plans
		// X 2 European chemical Substances Information System (ESIS) - PBT List
		// X 3 SIN (Substitute It Now) List
		// 4 Chapter 173-333 WAC Persistent Bioaccumulative Toxins
		// 5 Priority PBT Profiles (Pollution Prevention and Toxics)
		// 6 TRI PBT Chemical List
		// 7 Priority Persistent Pollutant (P3) List
		// 8 Priority Chemicals List
		// 9 Chemical Lists of Priority Action & Possible Concern
		// 10 Substances of Very High Concern for REACH Annex XIV authorisation
		// (Article 59)
		// 11 Stockholm Convention on Persistent Organic Pollutants (POPs) -
		// Annex A, B & C and under Review

		for (int i = 0; i < records.size(); i++) {
			LinkedList<String> list = (LinkedList<String>) records.get(i);

			String CAS = list.get(0);
			String Score = list.get(1);
			String listName = list.get(2);
			String Agency = list.get(3);

			if (listName.equals(listCEPA)) {

				if (Score.indexOf("Persistent") > -1) {
					scoreCEPA = score2;
					// System.out.println(CAS);
				} else {
					System.out.println(CAS + "\t" + Score + "*");
				}
				scoreCEPA2 = Score;

			} else if (listName.equals(listT)) {
				// only 4 compounds
				if (Score.equals("High environmental persistence - TSCA Criteria met")) {
					scoreT = score2;
				} else if (Score.equals("Medium environmental persistence - TSCA Criteria met")) {
					scoreT = score3;
				} else if (Score.equals("Low environmental persistence - TSCA Criteria met")) {
					scoreT = score4;
				} else if (Score.equals("PBTs for Expedited Action to Reduce Risks")) {
					scoreT = score2;// TODO

				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreT2 = Score;

			} else if (listName.equals(listESIS)) {

				System.out.println(CAS + "\t" + listName + "\t" + Score);

			} else if (listName.equals(listSIN)) {
				// only 4 chemicals
				if (Score.equals(
						"PBT / vPvB (Persistent, Bioaccumulative, &amp; Toxic / very Persistent &amp; very Bioaccumulative)")) {
					scoreSIN = score2;
				} else {
					// System.out.println(CAS+"\t"+Score);
				}
				scoreSIN2 = Score;

			} else if (listName.equals(listK)) {
				if (Score.indexOf("H400") > -1) {
					scoreK = score1;
				} else {
					// System.out.println(CAS+"\t"+Score);
				}
				scoreK2 = Score;

			} else if (listName.equals(listA)) {
				// TODO- are these human mutagenicity scores???
				if (Score.indexOf("H400") > -1) {
					scoreA = score1;
				} else {
					// System.out.println(CAS+"\t"+Score);
				}
				scoreA2 = Score;
			} else if (listName.equals(listM)) {

				if (Score.indexOf("H400") > -1) {
					scoreM = score1;
				} else {
					// System.out.println(CAS+"\t"+Score);
				}
				scoreM2 = Score;

			} else {
				// System.out.println(CAS+"\t"+listName);
			}

		}

		String src = "";
		String finalScore = "";
		String finalScore2 = "";

		if (!scoreCEPA.equals("")) {
			finalScore = scoreCEPA;
			finalScore2 = scoreCEPA2;
			src = listCEPA;
		} else if (!scoreT.equals("")) {
			finalScore = scoreT;
			finalScore2 = scoreT2;
			src = listT;
		} else if (!scoreESIS.equals("")) {
			finalScore = scoreESIS;
			finalScore2 = scoreESIS2;
			src = listESIS;
		} else if (!scoreSIN.equals("")) {
			finalScore = scoreSIN;
			finalScore2 = scoreSIN2;
			src = listSIN;
		} else if (!scoreA.equals("")) {
			finalScore = scoreA;
			finalScore2 = scoreA2;
			src = listA;
		} else if (!scoreK.equals("")) {
			finalScore = scoreK;
			finalScore2 = scoreK2;
			src = listK;
		} else if (!scoreM.equals("")) {
			finalScore = scoreM;
			finalScore2 = scoreM2;
			src = listM;
		}

		String result1 = finalScore + "\t" + finalScore2 + "\t" + src;
		String result2 = scoreCEPA + "\t" + scoreEU_R + "\t" + scoreT + "\t" + scoreESIS + "\t" + scoreSIN + "\t"
				+ scoreA + "\t" + scoreK + "\t" + scoreM;

		String[] results = { result1, result2 };

		return results;

		//
	}

	String[] determineEndocrineDisruptionScore(Vector records) {

		String score1 = "VH";
		String score2 = "H";
		String score3 = "M";
		String score4 = "L";
		String score5 = "VL";

		String scoreEU = "";// EU
		String scoreS = "";// SIN (Substitute It Now) List
		String scoreT = "";// TEDX (The Endocrine Disruption eXchange) List of
							// Potential Endocrine Disruptors
		String scoreR = "";// Reach
		String scoreC = "";// Chemical Lists of Priority Action & Possible
							// Concern

		String scoreEU2 = "";// EU
		String scoreS2 = "";// SIN (Substitute It Now) List
		String scoreT2 = "";// TEDX (The Endocrine Disruption eXchange) List of
							// Potential Endocrine Disruptors
		String scoreR2 = "";// Reach
		String scoreC2 = "";// Chemical Lists of Priority Action & Possible
							// Concern

		for (int i = 0; i < records.size(); i++) {
			LinkedList<String> list = (LinkedList<String>) records.get(i);

			String CAS = list.get(0);
			String Score = list.get(1);
			String listName = list.get(2);
			String Agency = list.get(3);

			if (listName.equals("EU Community Strategy for Endocrine Disrupters - Priority List")) {

				if (Score.equals("Category 1 - In vivo evidence of Endocrine Disruption Activity")) {
					scoreEU = score1;
				} else if (Score.equals(
						"Category 2 - In vitro evidence of biological activity related to Endocrine Disruption")) {
					scoreEU = score2;
				} else if (Score.equals("Category 3a (ED Studies available but no indication of ED effects)")) {
					scoreEU = score4;
				} else if (Score.equals("Category 3b (Substances with no or insufficient data gathered)")) {
					scoreEU = score5;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreEU2 = Score;
			} else if (listName
					.equals("Substances of Very High Concern for REACH Annex XIV authorisation (Article 59)")) {
				if (Score.equals("Equivalent Concern - Candidate List")) {
					scoreR = score2;
				} else if (Score.equals("Equivalent Concern - Prioritized for Listing")) {
					scoreR = score3;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreR2 = Score;

			} else if (listName.equals("Chemical Lists of Priority Action & Possible Concern")) {
				if (Score.equals("Endocrine Disruptor - Chemical for Priority Action")) {
					scoreC = score2;
				} else if (Score.equals("Endocrine Disruptor - Substance of Possible Concern")) {
					scoreC = score3;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreC2 = Score;

			} else if (listName.equals("SIN (Substitute It Now) List")) {
				if (Score.equals("Endocrine Disruption")) {
					scoreS = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreS2 = Score;

			} else if (listName
					.equals("TEDX (The Endocrine Disruption eXchange) List of Potential Endocrine Disruptors")) {
				if (Score.equals("Potential Endocrine Disruptor")) {
					scoreT = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreT2 = Score;

			} else {
				// System.out.println(CAS+"\t"+listName);
			}

		}

		String src = "";
		String finalScore = "";
		String finalScore2 = "";

		if (!scoreEU.equals("")) {
			finalScore = scoreEU;
			finalScore2 = scoreEU2;
			src = "EU";
		} else if (!scoreR.equals("")) {
			finalScore = scoreR;
			finalScore2 = scoreR2;
			src = "REACH";
		} else if (!scoreC.equals("")) {
			finalScore = scoreC;
			finalScore2 = scoreC2;
			src = "Chemical Lists";
		} else if (!scoreT.equals("")) {
			finalScore = scoreT;
			finalScore2 = scoreT2;
			src = "TEDX List";
		} else if (!scoreS.equals("")) {
			finalScore = scoreS;
			finalScore2 = scoreS2;
			src = "SIN List";
		}

		String result1 = finalScore + "\t" + finalScore2 + "\t" + src;
		String result2 = scoreEU + "\t" + scoreR + "\t" + scoreC + "\t" + scoreS + "\t" + scoreT;

		String[] results = { result1, result2 };

		return results;

	}

	String[] determineSkinIrritationScore(Vector records) {

		String score1 = "VH";
		String score2 = "H";
		String score3 = "M";
		String score4 = "L";
		String score5 = "VL";

		// According to DfE, VH= corrosive, H= severe irritation, M=moderate
		// irr, L=mild irr, VL= not irritating

		String scoreEU_H = "";// EU H system
		String scoreEU_R = "";// EU R system
		String scoreNZ = "";// New Zealand
		String scoreA = "";// Australia
		String scoreJ = "";// Japan
		String scoreK = "";// Korea

		String scoreEU_H2 = "";// EU H system
		String scoreEU_R2 = "";// EU R system
		String scoreNZ2 = "";// New Zealand
		String scoreA2 = "";// Australia
		String scoreJ2 = "";// Japan
		String scoreK2 = "";// Korea

		for (int i = 0; i < records.size(); i++) {
			LinkedList<String> list = (LinkedList<String>) records.get(i);

			String CAS = list.get(0);
			String Score = list.get(1);
			String listName = list.get(2);
			String Agency = list.get(3);

			if (listName.indexOf(
					"Regulation on the Classification, Labelling and Packaging of Substances and Mixtures (CLP)") > -1) {

				if (Score.equals("H314 - Causes severe skin burns and eye damage")) {
					scoreEU_H = score1;
				} else if (Score.equals("H315 - Causes skin irritation")) {
					scoreEU_H = score3;
				} else if (Score.indexOf("H316") > -1 || Score.equals("H317 - May cause an allergic skin reaction")) {// H316 didnt appear in ChemHAT
					// H316: Causes mild skin irritation (doesnt appear in ChemHAT)
					scoreEU_H = score4;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreEU_H2 = Score;

			} else if (listName.indexOf("Substances with EU Risk & Safety Phrases") > -1) {
				if (Score.equals("R35 - Causes severe burns")) {
					scoreEU_R = score1;
				} else if (Score.indexOf("R34 - Causes burns") > -1) {
					scoreEU_R = score1;
				} else if (Score.indexOf("R38 - Irritating to skin") > -1) {
					scoreEU_R = score3;
				} else if (Score.indexOf("R66: Repeated exposure may cause skin dryness or cracking") > -1) {
					scoreEU_R = score4;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreEU_R2 = Score;
			} else if (listName.indexOf("New Zealand HSNO Chemical Classifications") > -1) {
				// TODO- there's no score 2!
				if (Score.equals("8.2A - Corrosive to dermal tissue (Cat. 1A)")) {
					scoreNZ = score1;
				} else if (Score.equals("8.2B - Corrosive to dermal tissue (Cat. 1B)")) {
					scoreNZ = score1;
				} else if (Score.equals("8.2C - Corrosive to dermal tissue (Cat. 1C)")) {
					scoreNZ = score1;
				} else if (Score.equals("6.3A - Irritating to the skin (Cat. 2)")) {
					scoreNZ = score3;
				} else if (Score.equals("Irritating to the skin (Cat. 2)")) {
					scoreNZ = score3;
				} else if (Score.equals("6.3B - Mildly irritating to the skin")) {
					scoreNZ = score4;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreNZ2 = Score;
			} else if (listName.indexOf("Australia - GHS") > -1) {

				// System.out.println(CAS+"\t"+Score);

				if (Score.equals("H314 - Causes severe skin burns and eye damage")) {
					scoreA = score1;
				} else if (Score.equals("H315 - Causes skin irritation")) {
					scoreA = score3;
				} else if (Score.indexOf("H316") > -1 || Score.equals("H317 - May cause an allergic skin reaction")) {// H316
																														// didnt
																														// appear
																														// in
																														// ChemHAT
					// H316: Causes mild skin irritation
					scoreA = score4;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreA2 = Score;

			} else if (listName.equals("Japan GHS Classifications")) {

				// TODO
				if (Score.equals("Skin corrosion / irritation - Category 1")) {
					scoreJ = score1;
				} else if (Score.equals("Skin corrosion / irritation - Category 1A")
						|| Score.equals("Skin corrosion / irritation - Category 1B")
						|| Score.equals("Skin corrosion / irritation - Category 1C")) {
					scoreJ = score1;
				} else if (Score.equals("Skin corrosion / irritation - Category 2")
						|| Score.equals("Skin corrosion / irritation - Category 2-3")) {
					scoreJ = score2;
				} else if (Score.equals("Skin corrosion / irritation - Category 3")) {
					scoreJ = score4;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreJ2 = Score;

			} else if (listName.equals("Korea GHS Classification and Labelling for Toxic Chemicals")) {

				if (Score.equals(
						"Skin corrosion/irritation - Category 1 [H314 - Causes severe skin burns and eye damage]")) {
					scoreK = score1;
				} else if (Score
						.indexOf("Skin corrosion/irritation - Category 2 [H315 - Causes skin irritation]") > -1) {
					scoreK = score2;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreK2 = Score;

			} else {
				// System.out.println(CAS+"\t"+listName);
			}

			System.out.println(CAS + "\t" + scoreEU_H + "\t" + scoreEU_R + "\t" + scoreNZ + "\t" + scoreA + "\t"
					+ scoreJ + "\t" + scoreK);

		}

		String src = "";
		String finalScore = "";
		String finalScore2 = "";

		if (!scoreEU_H.equals("")) {
			finalScore = scoreEU_H;
			finalScore2 = scoreEU_H2;
			src = "EU_H";
		} else if (!scoreEU_R.equals("")) {
			finalScore = scoreEU_R;
			finalScore2 = scoreEU_R2;
			src = "EU_R";
		} else if (!scoreNZ.equals("")) {
			finalScore = scoreNZ;
			finalScore2 = scoreNZ2;
			src = "New Zealand";
		} else if (!scoreJ.equals("")) {
			finalScore = scoreJ;
			finalScore2 = scoreJ2;
			src = "Japan";
		} else if (!scoreA.equals("")) {
			finalScore = scoreA;
			finalScore2 = scoreA2;
			src = "Australia";
		} else if (!scoreK.equals("")) {
			finalScore = scoreK;
			finalScore2 = scoreK2;
			src = "Korea";
		}

		String result1 = finalScore + "\t" + finalScore2 + "\t" + src;
		String result2 = scoreEU_H + "\t" + scoreEU_R + "\t" + scoreNZ + "\t" + scoreA + "\t" + scoreJ + "\t" + scoreK;

		String[] results = { result1, result2 };

		return results;

	}

	String[] determineSkinSensitizationScore(Vector records) {

		String score1 = "VH";
		String score2 = "H";
		String score3 = "M";
		String score4 = "L";
		String score5 = "VL";

		// According to DfE, VH= corrosive, H= severe irritation, M=moderate
		// irr, L=mild irr, VL= not irritating

		String scoreEU_R = "";// EU R system
		String scoreNZ = "";// New Zealand
		String scoreJ = "";// Japan
		String scoreK = "";// Korea
		String scoreMAK_BAT = "";

		String scoreEU_R2 = "";// EU R system
		String scoreNZ2 = "";// New Zealand
		String scoreJ2 = "";// Japan
		String scoreK2 = "";// Korea
		String scoreMAK_BAT2 = "";

		for (int i = 0; i < records.size(); i++) {
			LinkedList<String> list = (LinkedList<String>) records.get(i);

			String CAS = list.get(0);
			String Score = list.get(1);
			String listName = list.get(2);
			String Agency = list.get(3);

			if (listName.indexOf("Substances with EU Risk & Safety Phrases") > -1) {
				// TODO- add other R codes???
				if (Score.equals("R43 - May cause sensitization by skin contact")) {
					scoreEU_R = score3;
				} else {
					System.out.println(CAS + "\t" + Score);
				}

				scoreEU_R2 = Score;

			} else if (listName.equals("New Zealand HSNO Chemical Classifications")) {
				// TODO- there's no score 2!
				if (Score.equals("6.5B (contact) - Contact sensitisers (Cat. 1)")) {
					scoreNZ = score3;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreNZ2 = Score;
			} else if (listName.equals("Japan GHS Classifications")) {
				if (Score.equals("Skin sensitizer - Category 1")) {
					scoreJ = score3;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreJ2 = Score;

			} else if (listName.equals("Korea GHS Classification and Labelling for Toxic Chemicals")) {
				if (Score.equals("Skin sensitization - Category 1 [H317 - May cause an allergic skin reaction]")) {
					scoreK = score3;
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreK2 = Score;

			} else if (listName.equals("List of Substances with MAK & BAT Values & Categories")) {
				if (Score.equals("Sensitizing Substance Sh - Danger of skin sensitization")) {
					scoreMAK_BAT = score3;
				} else if (Score.equals("Sensitizing Substance SP - Danger of photocontact sensitization")) {
					scoreMAK_BAT = score3;// TODO???
				} else {
					System.out.println(CAS + "\t" + Score);
				}
				scoreMAK_BAT2 = Score;
			} else {
				System.out.println(CAS + "\t" + listName);
			}

		}

		String src = "";
		String finalScore = "";
		String finalScore2 = "";
		if (!scoreEU_R.equals("")) {
			finalScore = scoreEU_R;
			finalScore2 = scoreEU_R2;
			src = "EU_R";
		} else if (!scoreNZ.equals("")) {
			finalScore = scoreNZ;
			finalScore2 = scoreNZ2;
			src = "New Zealand";
		} else if (!scoreJ.equals("")) {
			finalScore = scoreJ;
			finalScore2 = scoreJ2;
			src = "Japan";
		} else if (!scoreK.equals("")) {
			finalScore = scoreK;
			finalScore2 = scoreK2;
			src = "Korea";
		} else if (!scoreMAK_BAT.equals("")) {
			finalScore = scoreMAK_BAT;
			finalScore2 = scoreMAK_BAT2;
			src = "Malaysia";
		}

		String result1 = finalScore + "\t" + finalScore2 + "\t" + src;
		String result2 = scoreEU_R + "\t" + scoreNZ + "\t" + scoreJ + "\t" + scoreK + "\t" + scoreMAK_BAT;

		String[] results = { result1, result2 };

		return results;

	}

	String[] determineNeurotoxScore(Vector records) {

		String score1 = "VH";
		String score2 = "H";
		String score3 = "M";
		String score4 = "L";
		String score5 = "VL";

		// According to DfE, VH= corrosive, H= severe irritation, M=moderate
		// irr, L=mild irr, VL= not irritating

		String scoreEU_H = "";// EU H system
		String scoreEU_R = "";// EU R system
		String scoreA = "";// Australia
		String scoreK = "";// Korea
		String scoreM = "";// Malaysia
		String scoreB = "";// Chemicals with occupational exposure standards
							// based on nervous system effects (Boyes 2001)
		String scoreDN = "";// Developmental neurotoxicity of industrial
							// chemicals, List of 201 Chemicals
		String scoreRM = "";// Risk Management Actions & TSCA Work Plans

		String scoreEU_H2 = "";// EU H system
		String scoreEU_R2 = "";// EU R system
		String scoreA2 = "";// Australia
		String scoreK2 = "";// Korea
		String scoreM2 = "";// Malaysia
		String scoreB2 = "";// Chemicals with occupational exposure standards
							// based on nervous system effects (Boyes 2001)
		String scoreDN2 = "";// Developmental neurotoxicity of industrial
								// chemicals, List of 201 Chemicals
		String scoreRM2 = "";// Risk Management Actions & TSCA Work Plans

		// 0 Japan GHS Classifications
		// 1 New Zealand HSNO Chemical Classifications
		// 2 Substances with EU Risk & Safety Phrases (Commission Directive
		// 67-548-EEC)
		// 3 Korea GHS Classification and Labelling for Toxic Chemicals
		// 4 List of Substances with MAK & BAT Values & Categories

		for (int i = 0; i < records.size(); i++) {
			LinkedList<String> list = (LinkedList<String>) records.get(i);

			// if (i==0) CAS=list.get(0);

			String CAS = list.get(0);
			String Score = list.get(1);
			String listName = list.get(2);
			String Agency = list.get(3);

			if (listName.equals(
					"Regulation on the Classification, Labelling and Packaging of Substances and Mixtures (CLP) Annex 6 Table 3-1 - GHS Hazard  code criteria")) {

				if (Score.equals("H336 - May cause drowsiness or dizziness")) {
					scoreEU_H = score2;// TODO is this correct???
				} else {
					System.out.println("EU_H:" + CAS + "\t" + Score);
				}
				scoreEU_H2 = Score;
			} else if (listName.indexOf("Substances with EU Risk & Safety Phrases") > -1) {
				// TODO- add other R codes???
				if (Score.equals("R67 - Vapors may cause drowsiness and dizziness")) {
					scoreEU_R = score2;
				} else {
					System.out.println("EU:" + CAS + "\t" + Score);
				}
				scoreEU_R2 = Score;

			} else if (listName.indexOf("Australia - GHS") > -1) {

				if (Score.equals("H336 - May cause drowsiness or dizziness")) {
					scoreA = score2;// TODO is this correct???
				} else {
					System.out.println("Australia: " + CAS + "\t" + Score);
				}

				scoreA2 = Score;

			} else if (listName.equals("Korea GHS Classification and Labelling for Toxic Chemicals")) {
				if (Score.indexOf("H336") > -1) {
					scoreK = score2;
				} else {
					System.out.println("Korea: " + CAS + "\t" + Score);
				}
				scoreK2 = Score;

			} else if (listName.equals("Malaysia - GHS")) {
				if (Score.equals("H336 - May cause drowsiness or dizziness")) {
					scoreM = score2;// TODO is this correct???
				} else {
					System.out.println("Malaysia: " + CAS + "\t" + Score);
				}
				scoreM2 = Score;

			} else if (listName.equals(
					"Chemicals with occupational exposure standards based on nervous system effects (Boyes 2001)")) {

				if (Score.equals("Neurotoxic")) {
					scoreB = score2;// TODO is this correct???
				} else {
					System.out.println("Boyes: " + CAS + "\t" + Score);
				}
				scoreB2 = Score;
			} else if (listName.equals(
					"Developmental neurotoxicity of industrial chemicals, List of 201 Chemicals known to be neurotoxic in man")) {

				if (Score.equals("Known to be neurotoxic in man (2014)")) {
					scoreDN = score1;// TODO is this correct???
				} else if (Score.equals("Neurotoxic") || Score.equals("Emerging neurotoxicant")) {
					scoreDN = score2;
				} else {
					System.out.println("Developmental neurotoxic list: " + CAS + "\t" + Score);
				}

				scoreDN2 = Score;

			} else if (listName.equals("Risk Management Actions & TSCA Work Plans")) {

				if (Score.equals("Neurobehavioral effects")) {
					scoreRM = score1;// TODO- score 3 or score 1???
				} else if (Score.equals("Neurotoxicity - TSCA Criteria met")) {
					scoreRM = score2;// TODO
				} else {
					System.out.println("Risk management: " + CAS + "\t" + Score);
				}

				scoreRM2 = Score;

			} else {
				System.out.println("Other list: " + CAS + "\t" + listName);
			}

			// System.out.println(CAS+"\t"+scoreEU_R+"\t"+scoreNZ+"\t"+scoreJ+"\t"+scoreK+"\t"+scoreMAK_BAT);

		}

		String src = "";
		String finalScore = "";
		String finalScore2 = "";

		if (!scoreEU_H.equals("")) {
			finalScore = scoreEU_H;
			finalScore2 = scoreEU_H2;
			src = "EU_H";
		} else if (!scoreEU_R.equals("")) {
			finalScore = scoreEU_R;
			finalScore2 = scoreEU_R2;
			src = "EU_R";
		} else if (!scoreA.equals("")) {
			finalScore = scoreA;
			finalScore2 = scoreA2;
			src = "Australia";
		} else if (!scoreK.equals("")) {
			finalScore = scoreK;
			finalScore2 = scoreK2;
			src = "Korea";
		} else if (!scoreM.equals("")) {
			finalScore = scoreM;
			finalScore2 = scoreM2;
			src = "Malaysia";
		} else if (!scoreB.equals("")) {
			finalScore = scoreB;
			finalScore2 = scoreB2;
			src = "Boyes";
		} else if (!scoreDN.equals("")) {
			finalScore = scoreDN;
			finalScore2 = scoreDN2;
			src = "Developmental neurotox list";
		} else if (!scoreRM.equals("")) {
			finalScore = scoreRM;
			finalScore2 = scoreRM2;
			src = "Risk Management Actions & TSCA Work Plans";
		}

		String result1 = finalScore + "\t" + finalScore2 + "\t" + src;
		String result2 = scoreEU_H + "\t" + scoreEU_R + "\t" + scoreA + "\t" + scoreK + "\t" + scoreM + "\t" + scoreB
				+ "\t" + scoreDN + "\t" + scoreRM;

		String[] results = { result1, result2 };

		return results;

	}

	String determineFlammableScore(Vector records) {

		String score1 = "VH";
		String score2 = "H";
		String score3 = "M";
		String score4 = "L";
		String score5 = "VL";

		// According to DfE, VH= corrosive, H= severe irritation, M=moderate
		// irr, L=mild irr, VL= not irritating

		String scoreEU_H = "";// EU H system
		String scoreEU_R = "";// EU R system
		// String scoreNZ="";//New Zealand
		String scoreA = "";// Australia
		// String scoreJ="";// Japan
		String scoreK = "";// Korea
		String scoreM = "";// Malaysia
		// String scoreMAK_BAT="";

		String scoreB = "";// Chemicals with occupational exposure standards
							// based on nervous system effects (Boyes 2001)
		String scoreDN = "";// Developmental neurotoxicity of industrial
							// chemicals, List of 201 Chemicals
		String scoreRM = "";// Risk Management Actions & TSCA Work Plans

		// 0 Japan GHS Classifications
		// 1 New Zealand HSNO Chemical Classifications
		// 2 Substances with EU Risk & Safety Phrases (Commission Directive
		// 67-548-EEC)
		// 3 Korea GHS Classification and Labelling for Toxic Chemicals
		// 4 List of Substances with MAK & BAT Values & Categories

		for (int i = 0; i < records.size(); i++) {
			LinkedList<String> list = (LinkedList<String>) records.get(i);

			// if (i==0) CAS=list.get(0);

			String CAS = list.get(0);
			String Score = list.get(1);
			String listName = list.get(2);
			String Agency = list.get(3);

			if (listName.equals(
					"Regulation on the Classification, Labelling and Packaging of Substances and Mixtures (CLP) Annex 6 Table 3-1 - GHS Hazard  code criteria")) {

				if (Score.equals("H250 - Catches fire spontaneously if exposed to air")
						|| Score.equals("H220 - Extremely flammable gas")
						|| Score.equals("H224 - Extremely flammable liquid and vapour")) {
					scoreEU_H = score1;
				} else if (Score.equals("H225 - Highly flammable liquid and vapour")) {
					scoreEU_H = score2;// TODO is this correct???
				} else if (Score.equals("H226 - Flammable liquid and vapour") || Score.equals("H228 - Flammable solid")
						|| Score.equals("H221 - Flammable gas")) {
					scoreEU_H = score3;
				} else {
					System.out.println("EU_H:" + CAS + "\t" + Score);
				}
				scoreEU_H += "\t" + Score;
			}
			// TODO finish flammable!

			// else if (listName.indexOf("Substances with EU Risk & Safety
			// Phrases")>-1) {
			// //TODO- add other R codes???
			// if (Score.equals("R67 - Vapors may cause drowsiness and
			// dizziness")) {
			// scoreEU_R=score2;
			// } else {
			// System.out.println("EU:"+CAS+"\t"+Score);
			// }
			//
			// } else if (listName.indexOf("Australia - GHS")>-1) {
			//
			// if (Score.equals("H336 - May cause drowsiness or dizziness")) {
			// scoreA=score2;//TODO is this correct???
			// } else {
			//// System.out.println("Australia: "+CAS+"\t"+Score);
			// }
			//
			// } else if (listName.equals("Korea GHS Classification and
			// Labelling for Toxic Chemicals")) {
			// if (Score.indexOf("H336")>-1) {
			// scoreK=score2;
			// } else {
			//// System.out.println("Korea: "+CAS+"\t"+Score);
			// }
			//
			// } else if (listName.equals("Malaysia - GHS")) {
			// if (Score.equals("H336 - May cause drowsiness or dizziness")) {
			// scoreM=score2;//TODO is this correct???
			// } else {
			//// System.out.println("Malaysia: "+CAS+"\t"+Score);
			// }
			// } else if (listName.equals("Chemicals with occupational exposure
			// standards based on nervous system effects (Boyes 2001)")) {
			//
			// if (Score.equals("Neurotoxic")) {
			// scoreB=score2;//TODO is this correct???
			// } else {
			//// System.out.println("Boyes: "+CAS+"\t"+Score);
			// }
			// } else if (listName.equals("Developmental neurotoxicity of
			// industrial chemicals, List of 201 Chemicals known to be
			// neurotoxic in man")) {
			//
			// if (Score.equals("Known to be neurotoxic in man (2014)")) {
			// scoreDN=score1;//TODO is this correct???
			// } else if (Score.equals("Neurotoxic") || Score.equals("Emerging
			// neurotoxicant")) {
			// scoreDN=score2;
			// } else {
			//// System.out.println("Developmental neurotoxic list:
			// "+CAS+"\t"+Score);
			// }
			// } else if (listName.equals("Risk Management Actions & TSCA Work
			// Plans")) {
			//
			// if (Score.equals("Neurobehavioral effects")) {
			// scoreRM=score1;//TODO- score 3 or score 1???
			// } else if (Score.equals("Neurotoxicity - TSCA Criteria met")) {
			// scoreRM=score2;//TODO
			// } else {
			//// System.out.println("Risk management: "+CAS+"\t"+Score);
			// }
			//
			// } else {
			//// System.out.println("Other list: "+CAS+"\t"+listName);
			// }

			// System.out.println(CAS+"\t"+scoreEU_R+"\t"+scoreNZ+"\t"+scoreJ+"\t"+scoreK+"\t"+scoreMAK_BAT);

		}

		String src = "";
		String finalScore = "";

		if (!scoreEU_H.equals("")) {
			finalScore = scoreEU_H;
			src = "EU_H";
		} else if (!scoreEU_R.equals("")) {
			finalScore = scoreEU_R;
			src = "EU_R";
		} else if (!scoreA.equals("")) {
			finalScore = scoreA;
			src = "Australia";
		} else if (!scoreK.equals("")) {
			finalScore = scoreK;
			src = "Korea";
		} else if (!scoreM.equals("")) {
			finalScore = scoreM;
			src = "Malaysia";
		} else if (!scoreB.equals("")) {
			finalScore = scoreB;
			src = "Boyes";
		} else if (!scoreDN.equals("")) {
			finalScore = scoreDN;
			src = "Developmental neurotox list";
		} else if (!scoreRM.equals("")) {
			finalScore = scoreRM;
			src = "Risk Management Actions & TSCA Work Plans";
		}

		// if (finalScore.equals("")) {
		// System.out.println(src);
		// }

		return finalScore + "\t" + src;

		// String scoreB="";//Chemicals with occupational exposure standards
		// based on nervous system effects (Boyes 2001)
		// String scoreDN="";//Developmental neurotoxicity of industrial
		// chemicals, List of 201 Chemicals
		// String scoreRM="";//Risk Management Actions & TSCA Work Plans

	}

	String getFinalScoreInfo(String CAS, String filepath) {

		try {

			BufferedReader br = new BufferedReader(new FileReader(filepath));

			String header = br.readLine();

			while (true) {
				String Line = br.readLine();

				if (Line == null)
					break;

				String currentCAS = Line.substring(0, Line.indexOf("\t"));

				if (CAS.equals(currentCAS)) {
					br.close();
					return Line;
				}

			}

			br.close();

			return CAS + "\tN/A";

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return CAS + "\terror";

	}

	Vector<String> getScores(String CAS, String filepath) {

		Vector<String> results = new Vector<String>();

		try {

			BufferedReader br = new BufferedReader(new FileReader(filepath));

			String header = br.readLine();

			while (true) {
				String Line = br.readLine();

				if (Line == null)
					break;

				String currentCAS = Line.substring(0, Line.indexOf("\t"));

				if (CAS.equals(currentCAS)) {
					results.add(Line);
				}

			}

			br.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return results;

	}

	/**
	 * Create files for each endpoint (one line per chemical) 
	 * <br><br>
	 * First file: has L,M,H,VH score from all sources on each line<br>
	 * ChemHat\endpoint files\final\endpoint_allscores.txt
	 * <br><br>
	 * Second file: has final score on each line (CAS,Score,ScoreGHS,Source)<br>
	 * ChemHat\endpoint files\final\Birth Defects_final.txt
	 * 
	 * 
	 * @param folder
	 */
	void generateScoreFiles(String folder) {

		// String [] allEndpoints={"Toxic To Humans & Animals","Irritates The
		// Eyes","Irritates The Skin",
		// "Breast Cancer","Cancer","Birth Defects", "Reproductive Harm","Gene
		// Damage","Endocrine Disruption","Asthma Trigger",
		// "Sensitizes The Skin","Other Health Effects","Brain/Nervous System
		// Harm",
		// "Flammable","Restricted List","Reactive","Immediate Harm to Aquatic
		// Ecosystems","Long-Term Harm to Aquatic Ecosystems","Harmful to Land
		// Ecosystems","Bioaccumulative","Persistent"};

		String[] allEndpoints = { "Toxic To Humans & Animals", "Irritates The Eyes", "Irritates The Skin",
				"Breast Cancer", "Cancer", "Birth Defects", "Reproductive Harm", "Gene Damage", "Endocrine Disruption",
				"Sensitizes The Skin", "Other Health Effects", "Brain/Nervous System Harm", "Flammable",
				"Restricted List", "Reactive", "Immediate Harm to Aquatic Ecosystems",
				"Long-Term Harm to Aquatic Ecosystems", "Harmful to Land Ecosystems", "Bioaccumulative", "Persistent" };

		// String [] allEndpoints={"Irritates The Eyes"};

		for (int i = 0; i < allEndpoints.length; i++) {
			System.out.println("\r\n" + allEndpoints[i]);

			if (allEndpoints[i].equals("Toxic To Humans & Animals")) {
				generateScoreFile(folder, allEndpoints[i], "oral");
				generateScoreFile(folder, allEndpoints[i], "dermal");
				generateScoreFile(folder, allEndpoints[i], "inhaled");
			} else {
				generateScoreFile(folder, allEndpoints[i]);
			}

		}
	}

	void generateScoreFile(String folder, String endpoint) {
		generateScoreFile(folder, endpoint, "");
	}

	void generateScoreFile(String folder, String endpoint, String route) {

		// String endpoint="Irritates the Eyes";//done
		// String endpoint="Irritates the Skin";//done
		// String endpoint="Sensitizes the skin";//done
		// String endpoint="Brain_Nervous System Harm";//done
		// String endpoint="Endocrine Disruption";//done
		// String endpoint="Cancer";
		// String endpoint="Gene Damage";
		// String endpoint="Reproductive Harm";
		// String endpoint="Immediate Harm to Aquatic Ecosystems";
		// String endpoint="Persistent";
		// String endpoint="Birth Defects";

		// String endpoint="Toxic to Humans & Animals";
		// String route="oral";
		// String route="dermal";
		// String route="inhaled";
		//
		// System.out.println(endpoint);
		//
		//
		// String endpoint="Flammable";
		try {

			File finalFolder = new File(folder + "/final");
			if (!finalFolder.exists())
				finalFolder.mkdir();

			File endpointFile = new File(folder + "/" + endpoint + ".txt");

			if (!endpointFile.exists())
				return;

			BufferedReader br = new BufferedReader(new FileReader(folder + "/" + endpoint + ".txt"));

			// System.out.println(folder+"/"+endpoint+".txt");

			String filePath = "";
			String filePathAll = "";

			if (endpoint.toLowerCase().equals("toxic to humans & animals")) {
				filePath = folder + "/final/" + endpoint + "_" + route + "_final.txt";
				filePathAll = folder + "/final/" + endpoint + "_" + route + "_allscores.txt";
			} else {
				filePath = folder + "/final/" + endpoint + "_final.txt";
				filePathAll = folder + "/final/" + endpoint + "_allscores.txt";
			}

			FileWriter fw = new FileWriter(filePath);
			FileWriter fwAll = new FileWriter(filePathAll);

			fw.write("CAS\tScore\tScoreGHS\tSource\r\n");

			String CAS = "";
			Vector records = new Vector();

			Vector<String> listNames = new Vector<String>();

			if (endpoint.toLowerCase().equals("irritates the skin")) {
				fwAll.write("CAS\tscoreEU_H\tscoreEU_R\tscoreNZ\tscoreA\tscoreJ\tscoreK\r\n");
			} else if (endpoint.toLowerCase().equals("irritates the eyes")) {
				fwAll.write("CAS\tscoreEU_H\tscoreEU_R\tscoreNZ\tscoreA\tscoreJ\tscoreK\tscoreM\r\n");
			} else if (endpoint.toLowerCase().equals("sensitizes the skin")) {
				fwAll.write("CAS\tscoreEU_R\tscoreNZ\tscoreJ\tscoreK\tscoreMAK_BAT\r\n");
			} else if (endpoint.toLowerCase().equals("brain_nervous system harm")) {
				fwAll.write("CAS\tscoreEU_H\tscoreEU_R\tscoreA\tscoreK\tscoreM\tscoreB\tscoreDN\tscoreRM\r\n");
			} else if (endpoint.toLowerCase().equals("toxic to humans & animals")) {
				fwAll.write("CAS\tscoreEU_H\tscoreEU_R\tscoreNZ\tscoreJ\tscoreA\tscoreK\tscoreM\tScoreW\r\n");
			} else if (endpoint.toLowerCase().equals("endocrine disruption")) {
				fwAll.write("CAS\tscoreEU\tscoreR\tscoreC\tscoreS\tscoreT\r\n");
			} else if (endpoint.toLowerCase().equals("cancer")) {
				fwAll.write("CAS\tscoreI\tscoreT\tscoreEU_H\tscoreEU_R\tscoreEU\tscoreMAK_BAT\tscoreR\tscoreNZ"
						+ "\tscoreJ\tscoreA\tscoreK\tscoreM\tscoreROC\tscoreROM\tscoreN\tscoreC\r\n");
			} else if (endpoint.toLowerCase().equals("gene damage")) {
				fwAll.write(
						"CAS\tscoreEU_H\tscoreEU_R\tscoreEU_CMR\tscoreR\tscoreMAK_BAT\tscoreROM\tscoreT\tscoreNZ\tscoreJ\tscoreA\tscoreK\tscoreM\r\n");
			} else if (endpoint.toLowerCase().equals("birth defects")) {
				fwAll.write("CAS\tTODO\r\n");
			} else {
				System.out.println("Need header for " + endpoint);
			}

			boolean stop = false;

			while (true) {

				String Line = br.readLine();

				if (Line == null) {
					if (records.size() > 0) {
						// figure out final class
						String[] results = this.getResults(endpoint, records, route);
						// System.out.println(CAS+"\t"+score);

						if (results != null) {
							fw.write(CAS + "\t" + results[0] + "\r\n");
							fw.flush();
							fwAll.write(CAS + "\t" + results[1] + "\r\n");
							fwAll.flush();
						}
						records.clear();
					}
					stop = true;

				} else {
					if (Line.indexOf("CAS\t") > -1)
						Line = br.readLine();// read header
					if (Line == null)
						stop = true;
				}

				if (stop)
					break;

				// System.out.println(Line);

				// System.out.println("here:"+Line);
				LinkedList<String> list = Utilities.Parse3(Line, "\t");

				String listNameCurrent = list.get(2);

				if (!listNames.contains(listNameCurrent)) {
					listNames.add(listNameCurrent);
				}

				String newCAS = list.getFirst();

				if (!newCAS.equals(CAS)) {

					if (records.size() > 0) {
						// figure out final class

						String[] results = this.getResults(endpoint, records, route);

						if (results != null) {

							// System.out.println(CAS+"\t"+score);
							fw.write(CAS + "\t" + results[0] + "\r\n");
							fw.flush();

							fwAll.write(CAS + "\t" + results[1] + "\r\n");
							fwAll.flush();
						}

						records.clear();
					}
					CAS = newCAS;

				}

				records.add(list);

			}

			br.close();
			fw.close();

			System.out.println("");
			for (int i = 0; i < listNames.size(); i++) {
				System.out.println(i + "\t" + listNames.get(i));
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	String[] getResults(String endpoint, Vector records, String route) {

		String[] results = null;

		// TODO: "Breast Cancer","Other Health Effects","Flammable","Restricted
		// List","Reactive","Long-Term Harm to Aquatic Ecosystems","Harmful to
		// Land Ecosystems","Bioaccumulative"

		if (endpoint.toLowerCase().equals("irritates the eyes")) {
			results = determineEyeIrritationScore(records);
		} else if (endpoint.toLowerCase().equals("irritates the skin")) {
			results = determineSkinIrritationScore(records);
		} else if (endpoint.toLowerCase().equals("sensitizes the skin")) {
			results = determineSkinSensitizationScore(records);
		} else if (endpoint.toLowerCase().equals("brain_nervous system harm")) {
			results = determineNeurotoxScore(records);
		} else if (endpoint.toLowerCase().equals("toxic to humans & animals")) {
			results = determineScoreToxicToHumansAndAnimals(records, route);
		} else if (endpoint.toLowerCase().equals("endocrine disruption")) {
			results = determineEndocrineDisruptionScore(records);
		} else if (endpoint.toLowerCase().equals("cancer")) {
			results = determineCancerScore(records);
		} else if (endpoint.toLowerCase().equals("gene damage")) {
			results = determineGeneDamageScore(records);
		} else if (endpoint.toLowerCase().equals("reproductive harm")) {
			results = determineReproductiveHarmScore(records);
		} else if (endpoint.toLowerCase().equals("immediate harm to aquatic ecosystems")) {
			results = determineAcuteAquaticToxScore(records);
		} else if (endpoint.toLowerCase().equals("persistent")) {
			results = determinePersistentScore(records);
		} else if (endpoint.toLowerCase().equals("birth defects")) {
			results = determineBirthDefectsScore(records);
		}

		return results;

		// } else if (endpoint.toLowerCase().equals("flammable")) {
		// score=determineFlammableScore(records);
		// }
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ChemHAT a = new ChemHAT();

//		a.getUniqueSources();
		
		// a.lookup("117-81-7");
//		a.getChemHatChems();
//		 a.getUniqueChems();

//		 a.downloadchemicals("ChemHat/unique urls.txt","ChemHat/records");
		// a.downloadchemicals("ChemHat/unique urls flame
		// retardants.txt","ChemHat/records flame retardants 3-18-17");

		// String CAS="100-00-5";
		// String CAS="1333-82-0";
		// String CAS="71-43-2";
		// String CAS="7778-50-9";
//		 String CAS="50-70-4";
//		 a.parseChemHatFile("ChemHat/records/"+CAS+".html");
//		 a.parseChemHatFile2("ChemHat/records/"+CAS+".html",true);

//		 a.parseChemHatFiles("ChemHat/records","ChemHat/records2");
//		 a.parseChemHatFiles2("ChemHat/records","ChemHat/scores.txt");

		// a.parseChemHatFiles("ChemHat/flame retardants/records","ChemHat/flame
		// retardants/records2");
		// a.parseChemHatFiles2("ChemHat/flame
		// retardants/records","ChemHat/flame retardants/scores flame
		// retardants.txt");

		String f1="AA Dashboard/data";
		 String folder1=f1+"/ChemHat/records2";
		 String folder2=f1+"/ChemHat/endpoint files";
//		 a.generateEndpointFile(folder1, "Brain/Nervous System Harm", folder2);
		 a.generateEndpointFile(folder1, "Cancer", folder2);
		 a.generateAllEndpointFiles(folder1,folder2);
		
		

		// String folder1="ChemHat/flame retardants/records2";
		// String folder2="ChemHat/flame retardants/endpoint files";
		// a.generateAllEndpointFiles(folder1,folder2);

//		 a.generateScoreFile("ChemHat/endpoint files","Cancer");
//		a.generateScoreFiles("ChemHat/endpoint files");
		
		
		
//		 a.generateScoreFiles("ChemHat/flame retardants/endpoint files");

		//
		// a.generateEndpointFile("ChemHat/records2",
		// "bioaccumulative","ChemHat/endpoint files");
		// a.generateEndpointFile("ChemHat/records2", "PBT (Persistent
		// Bioaccumulative Toxicant)","ChemHat/endpoint files");

		// a.generateEndpointFile("ChemHat/records2", "Cancer","ChemHat/endpoint
		// files");

		////////////////////////////////////////////////////////////////////////////////////////

		// String [] endpoints={"Toxic to Humans & Animals_oral","Cancer"};
		//
		// String folder="ChemHat/endpoint files/final";
		// String endpoint="Toxic to Humans & Animals_oral";
		// String filepath=folder+"/"+endpoint+"_final.txt";
		//// String CAS="1163-19-5";
		// String CAS="115-86-6";
		//
		// String result=a.getFinalScoreInfo(CAS, filepath)+"\t"+endpoint;
		// System.out.println(result);

		////////////////////////////////////////////////////////////////////////////////////////

		// String CAS="1163-19-5";
		// String CAS="115-86-6";
		//
		// String[] endpoints = { "Toxic to Humans & Animals_oral", "Cancer",
		// "Gene Damage", "Endocrine Disruption",
		// "Reproductive Harm", "Brain_Nervous System Harm", "Sensitizes the
		// skin", "Irritates the Eyes",
		// "Irritates the Skin","Immediate Harm to Aquatic
		// Ecosystems","Persistent" };
		//
		// //TODO- add Birth Defects, Long-term harm to aquatic ecosystems
		//
		// String folder="ChemHat/endpoint files/final";
		//
		// for (int i=0;i<endpoints.length;i++) {
		// String filepath=folder+"/"+endpoints[i]+"_final.txt";
		// String result=a.getFinalScoreInfo(CAS, filepath);
		// System.out.println(result+"\t"+endpoints[i]);
		// }

		////////////////////////////////////////////////////////////////////////////////////////

		// String folder="ChemHat/endpoint files";
		// String endpoint="Toxic to Humans & Animals";
		// String filepath=folder+"/"+endpoint+".txt";
		//
		//// String CAS="1163-19-5";
		// String CAS="115-86-6";
		//// String CAS="1163-19-5";
		//// String CAS="1163-19-5";
		//
		//
		// Vector<String> result=a.getScores(CAS, filepath);
		//
		// for (int i=0;i<result.size();i++) {
		// System.out.println(result.get(i)+"\t"+endpoint);
		// }

	}

	private void getUniqueSources() {

		File folder= new File("AA Dashboard/Data/ChemHat/endpoint files");
		
		File [] files=folder.listFiles();
		
		for (int i=0;i<files.length;i++) {
//		for (int i=0;i<1;i++) {	
			File filei=files[i];
			
			if (filei.getName().indexOf(".txt")==-1) continue;
			
			try {
				
				BufferedReader br=new BufferedReader(new FileReader(filei));
				
				String header=br.readLine();
				
				Vector<String>uniqueSources=new Vector<String>();
				
				while (true) {
					String Line=br.readLine();
					
					if (Line==null) break;
					
					LinkedList<String>list=Utilities.Parse3(Line, "\t");
					
					String listName=list.get(2);
					String agency=list.get(3);
					
					String a_l=agency+"|"+listName;
					
					if (!uniqueSources.contains(a_l)) {
						uniqueSources.add(a_l);
					}
					
				}
			
				Collections.sort(uniqueSources);
				
				System.out.println(filei.getName().substring(0,filei.getName().indexOf(".")));
				for (int j=0;j<uniqueSources.size();j++) {
					System.out.println(uniqueSources.get(j));
				}
				System.out.println("");
				
				br.close();
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			
			
		}
		
		
	}

}
