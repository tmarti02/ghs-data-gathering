package gov.epa.ghs_data_gathering.ParseNew;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import gov.epa.ghs_data_gathering.Utilities.FileUtilities;

public class ChemIDPlusLD50Oral {

		public static final String folder = "L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Chemidplus\\";

		public static final String textFile = folder + "CAS Numbers Oral.txt";

		public static final String htmlFolder = folder + "Oral LD50 searches";

		public static final String webpageFolder = folder + "Oral LD50 webpages";

		class CASRecord {

			ArrayList<String> CAS = new ArrayList<>();

		}

		private void htmlParse(String htmlFolder) {

			CASRecord cr = new CASRecord();

			Document doc;

			try {

				File fileFolder = new File(htmlFolder);
				File[] files = fileFolder.listFiles();

				for (int i = 0; i < files.length; i++) {
					File inputFile = files[i];

					if (inputFile.getName().indexOf(".html") == -1)
						continue;

					doc = Jsoup.parse(inputFile, "utf-8");

					Elements mainTable = doc.select("table");

					Element innerTable = mainTable.select("tbody").get(1);

					Elements CAS = innerTable.select("td td");

					for (Element e : CAS) {

						// removing newline characters
						if (e.ownText().isEmpty()) {
							continue;
						}

						cr.CAS.add(e.ownText());
					}
				}
//				writeToTextFile(cr);
				downloadWebPages(cr);

			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}

		private void writeToTextFile(CASRecord cr) {

			try {

				File inputTextFile = new File(textFile);
				FileWriter fw = new FileWriter(inputTextFile);

				for (int i = 0; i < cr.CAS.size(); i++) {
					fw.write(cr.CAS.get(i) + "\r\n");
				}

				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private void downloadWebPages(CASRecord cr) {

			String url = "https://chem.nlm.nih.gov/chemidplus/rn/";
			try {

				for (int i = 0; i < cr.CAS.size(); i++) {

					String CAS = cr.CAS.get(i);

					String strURL = url + CAS;

					String destFilePath = webpageFolder + "/" + CAS + ".html";

					File destFile = new File(destFilePath);

					if (destFile.exists())
						continue;

					FileUtilities.downloadFile(strURL, destFilePath);

					Thread.sleep(3000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public static void main(String[] args) {

			ChemIDPlusLD50Oral co = new ChemIDPlusLD50Oral();

			co.htmlParse(htmlFolder);

		}
	}
