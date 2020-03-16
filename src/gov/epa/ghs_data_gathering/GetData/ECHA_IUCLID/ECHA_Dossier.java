package gov.epa.ghs_data_gathering.GetData.ECHA_IUCLID;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;


/**
 * Class for downloading ECHA dossier data and parsing the results
 * 
 * @author Todd Martin
 *
 */
public class ECHA_Dossier {

	void parseFile(String filepath) {
		//		</span> </a> </div> </th> </tr> </thead>
		

	}
	void downloadReachChemicals() {

		try {
			//1122 if do 200 at a time

			int min=1;
			int max=1122;

			for (int i=1;i<=max;i++) {
				String link="https://echa.europa.eu/advanced-search-for-chemicals?p_p_id=dissadvancedsearch_WAR_disssearchportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_count=1&_dissadvancedsearch_WAR_disssearchportlet_sessionCriteriaId=dissAdvancedSearchSessionParam101401485982859346&_dissadvancedsearch_WAR_disssearchportlet_searchOccurred=true&_dissadvancedsearch_WAR_disssearchportlet_delta=200&_dissadvancedsearch_WAR_disssearchportlet_orderByCol=name&_dissadvancedsearch_WAR_disssearchportlet_orderByType=asc&_dissadvancedsearch_WAR_disssearchportlet_resetCur=false&_dissadvancedsearch_WAR_disssearchportlet_cur="+i;
				this.downloadFile(i, link, "ECHA_Chemicals");

				System.out.println(i+" of "+(max-min+1));

				Thread.sleep(5000);// wait so dont get locked out	
			}



		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	void downloadListOfDossierChemicals() {


		//		https://echa.europa.eu/information-on-chemicals/registered-substances?p_p_id=dissregisteredsubstances_WAR_dissregsubsportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_dissregisteredsubstances_WAR_dissregsubsportlet_sessionCriteriaId=dissRegSubsSessionParam101401489078177443&_dissregisteredsubstances_WAR_dissregsubsportlet_orderByCol=name&_dissregisteredsubstances_WAR_dissregsubsportlet_orderByType=asc&_dissregisteredsubstances_WAR_dissregsubsportlet_resetCur=false&_dissregisteredsubstances_WAR_dissregsubsportlet_delta=200
		//https://echa.europa.eu/information-on-chemicals/registered-substances?p_p_id=dissregisteredsubstances_WAR_dissregsubsportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_dissregisteredsubstances_WAR_dissregsubsportlet_sessionCriteriaId=dissRegSubsSessionParam101401489079237047&_dissregisteredsubstances_WAR_dissregsubsportlet_delta=200&_dissregisteredsubstances_WAR_dissregsubsportlet_orderByCol=name&_dissregisteredsubstances_WAR_dissregsubsportlet_orderByType=asc&_dissregisteredsubstances_WAR_dissregsubsportlet_resetCur=false&_dissregisteredsubstances_WAR_dissregsubsportlet_cur=2
		try {


			//download the summary table of all the reach dossiers	
			for (int i=1;i<=87;i++) {//download 87 pages with 200 chemicals each

				String link="https://echa.europa.eu/information-on-chemicals/registered-substances?p_p_id=dissregisteredsubstances_WAR_dissregsubsportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_dissregisteredsubstances_WAR_dissregsubsportlet_sessionCriteriaId=dissRegSubsSessionParam101401489079237047&_dissregisteredsubstances_WAR_dissregsubsportlet_delta=200&_dissregisteredsubstances_WAR_dissregsubsportlet_orderByCol=name&_dissregisteredsubstances_WAR_dissregsubsportlet_orderByType=asc&_dissregisteredsubstances_WAR_dissregsubsportlet_resetCur=false&_dissregisteredsubstances_WAR_dissregsubsportlet_cur="+i;
				this.downloadFile(i, link, "REACH_dossier");

				Thread.sleep(5000);// wait so dont get locked out	
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}



	}

	void download() {
		try {


			//			https://echa.europa.eu/information-on-chemicals/ec-inventory
			//			https://echa.europa.eu/information-on-chemicals/ec-inventory?p_p_id=disslists_WAR_disslistsportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_disslists_WAR_disslistsportlet_keywords=&_disslists_WAR_disslistsportlet_orderByCol=name&_disslists_WAR_disslistsportlet_advancedSearch=false&_disslists_WAR_disslistsportlet_delta=50&_disslists_WAR_disslistsportlet_casNumber=&_disslists_WAR_disslistsportlet_deltaParamValue=50&_disslists_WAR_disslistsportlet_andOperator=true&_disslists_WAR_disslistsportlet_name=&_disslists_WAR_disslistsportlet_orderByType=asc&_disslists_WAR_disslistsportlet_ecNumber=&_disslists_WAR_disslistsportlet_doSearch=&_disslists_WAR_disslistsportlet_diss_mol_formula=&_disslists_WAR_disslistsportlet_resetCur=false&_disslists_WAR_disslistsportlet_cur=2

			//			https://echa.europa.eu/information-on-chemicals/ec-inventory?p_p_id=disslists_WAR_disslistsportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_disslists_WAR_disslistsportlet_keywords=&_disslists_WAR_disslistsportlet_orderByCol=name&_disslists_WAR_disslistsportlet_advancedSearch=false&_disslists_WAR_disslistsportlet_casNumber=&_disslists_WAR_disslistsportlet_deltaParamValue=50&_disslists_WAR_disslistsportlet_andOperator=true&_disslists_WAR_disslistsportlet_name=&_disslists_WAR_disslistsportlet_orderByType=asc&_disslists_WAR_disslistsportlet_ecNumber=&_disslists_WAR_disslistsportlet_doSearch=&_disslists_WAR_disslistsportlet_diss_mol_formula=&_disslists_WAR_disslistsportlet_resetCur=false&_disslists_WAR_disslistsportlet_delta=200
			//			https://echa.europa.eu/information-on-chemicals/ec-inventory?p_p_id=disslists_WAR_disslistsportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_disslists_WAR_disslistsportlet_keywords=&_disslists_WAR_disslistsportlet_orderByCol=name&_disslists_WAR_disslistsportlet_advancedSearch=false&_disslists_WAR_disslistsportlet_delta=200&_disslists_WAR_disslistsportlet_casNumber=&_disslists_WAR_disslistsportlet_deltaParamValue=1000&_disslists_WAR_disslistsportlet_andOperator=true&_disslists_WAR_disslistsportlet_name=&_disslists_WAR_disslistsportlet_orderByType=asc&_disslists_WAR_disslistsportlet_ecNumber=&_disslists_WAR_disslistsportlet_doSearch=&_disslists_WAR_disslistsportlet_diss_mol_formula=&_disslists_WAR_disslistsportlet_resetCur=false&_disslists_WAR_disslistsportlet_cur=2
			//			https://echa.europa.eu/information-on-chemicals/ec-inventory?p_p_id=disslists_WAR_disslistsportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_disslists_WAR_disslistsportlet_keywords=&_disslists_WAR_disslistsportlet_orderByCol=name&_disslists_WAR_disslistsportlet_advancedSearch=false&_disslists_WAR_disslistsportlet_delta=200&_disslists_WAR_disslistsportlet_casNumber=&_disslists_WAR_disslistsportlet_deltaParamValue=200&_disslists_WAR_disslistsportlet_andOperator=true&_disslists_WAR_disslistsportlet_name=&_disslists_WAR_disslistsportlet_orderByType=asc&_disslists_WAR_disslistsportlet_ecNumber=&_disslists_WAR_disslistsportlet_doSearch=&_disslists_WAR_disslistsportlet_diss_mol_formula=&_disslists_WAR_disslistsportlet_resetCur=false&_disslists_WAR_disslistsportlet_cur=532

			for (int i=145;i<=532;i++) {

				String link="https://echa.europa.eu/information-on-chemicals/ec-inventory?p_p_id=disslists_WAR_disslistsportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_disslists_WAR_disslistsportlet_keywords=&_disslists_WAR_disslistsportlet_orderByCol=name&_disslists_WAR_disslistsportlet_advancedSearch=false&_disslists_WAR_disslistsportlet_delta=200&_disslists_WAR_disslistsportlet_casNumber=&_disslists_WAR_disslistsportlet_deltaParamValue=200&_disslists_WAR_disslistsportlet_andOperator=true&_disslists_WAR_disslistsportlet_name=&_disslists_WAR_disslistsportlet_orderByType=asc&_disslists_WAR_disslistsportlet_ecNumber=&_disslists_WAR_disslistsportlet_doSearch=&_disslists_WAR_disslistsportlet_diss_mol_formula=&_disslists_WAR_disslistsportlet_resetCur=false&_disslists_WAR_disslistsportlet_cur="+i;
				this.downloadFile(i, link, "EC_Inventory");

				Thread.sleep(5000);// wait so dont get locked out	
			}



		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void downloadFile(int index,String URL,String destFolder) {

		try {

			java.net.URL myURL = new java.net.URL(URL);

			BufferedReader br
			= new BufferedReader(new InputStreamReader(myURL.openStream()));

			FileWriter fw=new FileWriter(destFolder+"/"+index+".html");

			int counter=0;

			while (true) {
				String Line=br.readLine();

				if (Line==null) break;

				//				System.out.println(counter+" " +Line);

				fw.write(Line+"\r\n");
				fw.flush();

				counter++;
			}

			br.close();
			fw.close();

		} catch (FileNotFoundException ex1) {
			System.out.println("file not found");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	void downloadFile(String URL,String destFilePath) {

		try {


			System.out.println(URL);
			//			System.out.println(destFilePath+"\n");

			java.net.URL myURL = new java.net.URL(URL);

			BufferedReader br
			= new BufferedReader(new InputStreamReader(myURL.openStream()));

			FileWriter fw=new FileWriter(destFilePath);

			int counter=0;

			long t1=System.currentTimeMillis();

			while (true) {
				long t2=System.currentTimeMillis();

				double time=t2-t1;
				time/=1000.0;

				if (time>10) {
					System.out.println("time out\t"+URL);
					break;
				}

				String Line=br.readLine();

				if (Line==null) break;

				//				System.out.println(counter+" " +Line);
				//				System.out.println(Line);

				fw.write(Line+"\r\n");
				fw.flush();

				counter++;
			}

			br.close();
			fw.close();

		} catch (FileNotFoundException ex1) {
			System.out.println("file not found");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	//go through the summary web pages and pull out the dossier numbers from the far right column
	void getDossierNumbersFromSummaryFiles() {

		try {

			for (int i = 1; i <= 87; i++) {

				BufferedReader br = new BufferedReader(new FileReader("ECHA index web pages/REACH_dossier/"+i+".html"));

				String seek = "https://echa.europa.eu/registration-dossier/-/registered-dossier/";

				String Line = "";
				while (true) {
					Line = br.readLine();

					// System.out.println(Line);

					if (Line.indexOf(seek) > -1)
						break;
				}

				while (Line.indexOf(seek) > -1) {
					Line = Line.substring(Line.indexOf(seek) + seek.length(), Line.length());
					String dossierNum = Line.substring(0, Line.indexOf("\""));
					System.out.println(dossierNum);
				}

				br.close();

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}




	void getCASNumbersFromECInventory() {
		String folder = "EC_Inventory";


		try {

			FileWriter fw = new FileWriter("ECinventory.txt");


			fw.write("url\tname\tECnumber\tCAS\tFormula\tDescription\r\n");

			for (int i = 1; i <= 532; i++) {

				BufferedReader br = new BufferedReader(new FileReader(folder + "/" + i + ".html"));
				String Line = br.readLine();
				Line = br.readLine();
				Vector<String> lines = this.parseInventoryLine(Line);

				for (int j = 0; j < lines.size(); j++) {
					fw.write(lines.get(j) + "\r\n");
					fw.flush();
				}

				br.close();
			}

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	void getCASNumbersFromECHAChemicals() {
		//		String folder = "ECHA index web pages/EC_Inventory";
		String folder="ECHA index web pages/ECHA_Chemicals";

		try {

			//			
			FileWriter fw = new FileWriter("ECHAchemicals.txt");

			String header="url\tname\tECnumber\tCAS";

			fw.write(header+"\r\n");

			System.out.println(header);

			String seek = "<td class=\"table-cell first table-sortable-column\">";

			for (int i = 1; i <= 1122; i++) {
				//				for (int i = 1; i <= 1; i++) {


				BufferedReader br = new BufferedReader(new FileReader(folder + "/" + i + ".html"));

				String Line="";

				while (true) {
					Line=br.readLine();
					if (Line.indexOf(seek)>-1) {
						break;
					}
				}


				Vector<String> lines = this.parseInventoryLine2(Line,seek);

				for (int j = 0; j < lines.size(); j++) {
					fw.write(lines.get(j) + "\r\n");
					fw.flush();
				}

				br.close();
			}

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	Vector<String> parseInventoryLine(String Line) {
		String seek = "<DIV CLASS=\"multiFieldTable\">";
		Vector<String> lines = new Vector<String>();

		while (Line.indexOf(seek) > -1) {

			Line = Line.substring(Line.indexOf(seek) + seek.length(), Line.length());

			Line = Line.substring(Line.indexOf("href=") + 6, Line.length());

			String url = Line.substring(0, Line.indexOf("\""));

			Line = Line.substring(Line.indexOf(">") + 1, Line.length());

			String name = Line.substring(0, Line.indexOf("<"));
			name = name.replace("&#039;&#039;", "\"");

			Line = Line.substring(Line.indexOf(seek) + seek.length(), Line.length());
			String ECnumber = Line.substring(0, Line.indexOf("<"));
			Line = Line.substring(Line.indexOf(seek) + seek.length(), Line.length());
			String CAS = Line.substring(0, Line.indexOf("<"));
			Line = Line.substring(Line.indexOf(seek) + seek.length(), Line.length());
			String Formula = Line.substring(0, Line.indexOf("<"));
			Line = Line.substring(Line.indexOf(seek) + seek.length(), Line.length());
			String Description = Line.substring(0, Line.indexOf("<"));

			// System.out.println(url);
			// System.out.println(name);
			// System.out.println(ECnumber);
			// System.out.println(CAS);
			// System.out.println(Formula);
			// System.out.println(Description);

			String newLine = url + "\t" + name + "\t" + ECnumber + "\t" + CAS + "\t" + Formula + "\t" + Description;
			lines.add(newLine);
			System.out.println(newLine);
		}
		return lines;
	}

	Vector<String> parseInventoryLine2(String Line,String seek) {


		Vector<String> lines = new Vector<String>();


		String seek2="<td class=\"table-cell \">";

		while (Line.indexOf(seek) > -1) {

			Line = Line.substring(Line.indexOf(seek) + seek.length(), Line.length());

			Line = Line.substring(Line.indexOf("href=") + 6, Line.length());

			String url = Line.substring(0, Line.indexOf("\"")).trim();

			Line = Line.substring(Line.indexOf(">") + 1, Line.length());

			String name = Line.substring(0, Line.indexOf("<"));
			name = name.replace("&#039;&#039;", "\"").trim();


			Line = Line.substring(Line.indexOf(seek2) + seek2.length(), Line.length());
			String ECnumber = Line.substring(0, Line.indexOf("<")).trim();

			Line = Line.substring(Line.indexOf(seek2) + seek2.length(), Line.length());
			String CAS = Line.substring(0, Line.indexOf("<")).trim();


			//			Line = Line.substring(Line.indexOf(seek) + seek.length(), Line.length());
			//			String Formula = Line.substring(0, Line.indexOf("<"));
			//			Line = Line.substring(Line.indexOf(seek) + seek.length(), Line.length());
			//			String Description = Line.substring(0, Line.indexOf("<"));

			// System.out.println(url);
			// System.out.println(name);
			// System.out.println(ECnumber);
			// System.out.println(CAS);
			// System.out.println(Formula);
			// System.out.println(Description);

			//			String newLine = url + "\t" + name + "\t" + ECnumber + "\t" + CAS + "\t" + Formula + "\t" + Description;
			String newLine = url + "\t" + name + "\t" + ECnumber + "\t" + CAS;
			lines.add(newLine);
			System.out.println(newLine);
		}
		return lines;
	}


	//brute force of determining valid dossier numbers
	void getListOfSubstancesWithDossiers() {

		// https://echa.europa.eu/information-on-chemicals/registered-substances
		//Database contains 15256 unique substances and contains information from 57979 dossiers
		//Showing 1 - 50 of 17,207 results.

		//Example of dossier file:
		// https://echa.europa.eu/registration-dossier/-/registered-dossier/15000

		int start = 428;
		int stop = 20000;

		try {

			FileWriter fw = new FileWriter("dossier numbers-2.txt");

			for (int i = start; i <= stop; i++) {
				String strURL = "https://echa.europa.eu/registration-dossier/-/registered-dossier/" + i;

				java.net.URL myURL = new java.net.URL(strURL);

				BufferedReader br = new BufferedReader(new InputStreamReader(myURL.openStream()));

				String Line = br.readLine();

				boolean haveNumber = false;

				if (Line.indexOf("The requested object was not found") == -1) {
					haveNumber = true;
				}

				System.out.println(i + "\t" + haveNumber);
				fw.write(i + "\t" + haveNumber + "\r\n");
				fw.flush();

				Thread.sleep(3000);// wait so dont get locked out

			}

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	void downloadToxicityDataForDossiers(String filePath_DossierList,String endpointName,String code,String reachDossierFolderPath) {


		try {

			BufferedReader br=new BufferedReader(new FileReader(filePath_DossierList));

			String destFolderPath=reachDossierFolderPath+"/"+endpointName;

			File of=new File(destFolderPath);
			if (!of.exists()) of.mkdir();

			Runtime s_runtime = Runtime.getRuntime ();


			while (true) {
				String Line=br.readLine();

				if (Line==null) break;

				String strURL="https://echa.europa.eu/registration-dossier/-/registered-dossier/"+Line+code;

				String destFilePath=destFolderPath+"/"+Line+".html";//todo base it on EC number?


				File destFile=new File(destFilePath);

				if (destFile.exists()) continue;


				this.downloadFile(strURL,destFilePath);

				s_runtime.gc();//run garbage collection to save memory




				//				System.out.println(strURL);

				Thread.sleep(3000);//wait so dont time out

			}
			br.close();



		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}


	void goThroughToxFiles(String folder,String endpoint) {
		File Folder=new File(folder);

		File [] files=Folder.listFiles();

		for (int i=0;i<files.length;i++) {

			try {
				BufferedReader br=new BufferedReader(new FileReader(files[i]));
				String Line=br.readLine();

				boolean haveEndpoint=false;

				if (Line.indexOf("<h2>"+endpoint+"</h2>")>-1) haveEndpoint=true;		


				System.out.println(i+"\t"+files[i].getName()+"\t"+haveEndpoint);

				br.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}


		}

	}


	boolean haveTag(File file,String tag) {

		boolean haveTag=false;

		try {
			BufferedReader br=new BufferedReader(new FileReader(file));



			String Line="";

			while (true) {
				Line=br.readLine();

				if (Line==null) break;

				if (Line.indexOf(tag)>-1) {
					haveTag=true;
					break;
				}

			}

			br.close();


		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return haveTag;


	}


	void moveFilesWithoutTag(String folderPath,String destFolderPath,String tag) {

		File Folder=new File(folderPath);

		File destFolder=new File(destFolderPath);
		if (!destFolder.exists()) destFolder.mkdir();


		File [] files=Folder.listFiles();

		for (int i=0;i<files.length;i++) {
			File filei=files[i];

			if (filei.isDirectory()) continue;

			if (i%100==0) System.out.println(i);

			if (!this.haveTag(filei, tag)) {
				File fileDest=new File(destFolderPath+"/"+filei.getName());
				filei.renameTo(fileDest);
			}
		}


	}

	void deleteFiles(String folderPath) {



		File Folder=new File(folderPath);

		File [] files=Folder.listFiles();


		for (int i=0;i<files.length;i++) {
			File filei=files[i];
			filei.delete();

		}

	}

	void parseSkinStudiesToJSON(String folderHTML,String folderJSON) {

		File Folder=new File(folderHTML);
		File destFolder=new File(folderJSON);

		if (!destFolder.exists()) destFolder.mkdir();


		File [] files=Folder.listFiles();

		System.out.println("files listed");


		for (int i=0;i<files.length;i++) {
			//		for (int i=12000;i<files.length;i++) {	
			if (i%100==0) System.out.println(i);

			File filei=files[i];
//			String filename=filei.getName();
			if (filei.isDirectory()) continue;

			String fileNum=filei.getName().substring(0,filei.getName().indexOf("."));

			this.parseSensitizationFile(fileNum,folderHTML,folderJSON);

		}
	}

	/**
	 * Go through all json files and make one flat text file:
	 * 
	 * @param folderJSON
	 * @param outputFilePath
	 */
	void convertJSONSkinStudiesToTextFile(String folderJSON,String outputFilePath) {


		try {

			File Folder = new File(folderJSON);

			File[] files = Folder.listFiles();

			System.out.println("files listed");


			FileWriter fw=new FileWriter(outputFilePath);

			Vector<String>interpretations=new Vector<String>();


			for (int i = 0; i < files.length; i++) {
				if (i % 100 == 0) System.out.println(i);

				File filei = files[i];
//				String filename = filei.getName();
				if (filei.isDirectory())
					continue;

				String fileNum = filei.getName().substring(0, filei.getName().indexOf("."));

				RecordECHA r=readJSONFileSimple(folderJSON,fileNum);

				String interpretation=r.interpretation_of_results;

				if (!interpretations.contains(interpretation)) interpretations.add(interpretation);

				if (i==0) fw.write(r.getHeader()+"\r\n");
				fw.write(r+"\r\n");
				fw.flush();

			}

			for (int i=0;i<interpretations.size();i++) {
				System.out.println(interpretations.get(i));
			}

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}



	void downloadAdditionalStudies(String folderPath) {
		Runtime s_runtime = Runtime.getRuntime ();

		File Folder=new File(folderPath);

		File [] files=Folder.listFiles();

		String seek="<select class=\"form-control\">";
		String seek2="<option value=\"";
//		String seek3="selected=\"selected\">";

		for (int i=0;i<files.length;i++) {
			File filei=files[i];
			String filename=filei.getName();
			if (filei.isDirectory()) continue;

			if (filename.indexOf("_")>-1) continue;



			try {

				BufferedReader br=new BufferedReader(new FileReader(filei));

				String Line=br.readLine();

				if (Line.indexOf(seek)>-1) {

					//<!DOCTYPE html> <html class="aui ltr ltr notIE" dir="ltr" lang="en-GB"> <head> <title>Molybdenum dioxide - Registration Dossier - ECHA</title> <meta charset="utf-8"> <meta http-equiv="X-UA-Compatible" content="IE=edge"> <meta name="viewport" content="width=device-width, initial-scale=1"> <meta content="text/html; charset=UTF-8" http-equiv="content-type" /> <link href="https://echa.europa.eu/diss-blank-theme/images/favicon.ico" rel="Shortcut Icon" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" rel="canonical" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;bg&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="bg-BG" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;es&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="es-ES" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;cs&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="cs-CZ" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;da&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="da-DK" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;de&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="de-DE" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;et&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="et-EE" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;el&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="el-GR" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="x-default" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="en-GB" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;fr&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="fr-FR" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;hr&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="hr-HR" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;it&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="it-IT" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;lv&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="lv-LV" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;lt&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="lt-LT" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;hu&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="hu-HU" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;mt&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="mt-MT" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;nl&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="nl-NL" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;pl&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="pl-PL" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;pt&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="pt-PT" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;ro&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="ro-RO" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;sk&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="sk-SK" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;sl&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="sl-SI" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;fi&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="fi-FI" rel="alternate" /> <link href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;sv&#x2f;registration-dossier&#x2f;-&#x2f;registered-dossier&#x2f;10167&#x2f;7&#x2f;5&#x2f;2" hreflang="sv-SE" rel="alternate" /> <link class="lfr-css-file" href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;diss-blank-theme&#x2f;css&#x2f;aui&#x2e;css&#x3f;browserId&#x3d;other&#x26;themeId&#x3d;dissblank_WAR_dissblanktheme&#x26;minifierType&#x3d;css&#x26;languageId&#x3d;en_GB&#x26;b&#x3d;6210&#x26;t&#x3d;1476949693000" rel="stylesheet" type="text/css" /> <link href="&#x2f;html&#x2f;css&#x2f;main&#x2e;css&#x3f;browserId&#x3d;other&#x26;themeId&#x3d;dissblank_WAR_dissblanktheme&#x26;minifierType&#x3d;css&#x26;languageId&#x3d;en_GB&#x26;b&#x3d;6210&#x26;t&#x3d;1472560743000" rel="stylesheet" type="text/css" /> <link href="https://echa.europa.eu/diss-factsheets-portlet/css/bootstrap.css?css_fast_load=0" rel="stylesheet" type="text/css" /> <script type="text/javascript">var Liferay={Browser:{acceptsGzip:function(){return false},getMajorVersion:function(){return 0},getRevision:function(){return""},getVersion:function(){return""},isAir:function(){return false},isChrome:function(){return false},isFirefox:function(){return false},isGecko:function(){return false},isIe:function(){return false},isIphone:function(){return false},isLinux:function(){return false},isMac:function(){return false},isMobile:function(){return false},isMozilla:function(){return false},isOpera:function(){return false},isRtf:function(){return false},isSafari:function(){return false},isSun:function(){return false},isWap:function(){return false},isWapXhtml:function(){return false},isWebKit:function(){return false},isWindows:function(){return false},isWml:function(){return false}},Data:{NAV_SELECTOR:"#navigation",isCustomizationView:function(){return false},notices:[null]},ThemeDisplay:{getLayoutId:function(){return"1088"},getLayoutURL:function(){return"https://echa.europa.eu/registration-dossier"},getParentLayoutId:function(){return"0"},isPrivateLayout:function(){return"false"},isVirtualLayout:function(){return false},getBCP47LanguageId:function(){return"en-GB"},getCDNBaseURL:function(){return"https://echa.europa.eu"},getCDNDynamicResourcesHost:function(){return""},getCDNHost:function(){return""},getCompanyId:function(){return"10136"},getCompanyGroupId:function(){return"10174"},getDefaultLanguageId:function(){return"en_GB"},getDoAsUserIdEncoded:function(){return""},getLanguageId:function(){return"en_GB"},getParentGroupId:function(){return"10162"},getPathContext:function(){return""},getPathImage:function(){return"/image"},getPathJavaScript:function(){return"/html/js"},getPathMain:function(){return"/c"},getPathThemeImages:function(){return"https://echa.europa.eu/diss-blank-theme/images"},getPathThemeRoot:function(){return"/diss-blank-theme"},getPlid:function(){return"22182805"},getPortalURL:function(){return"https://echa.europa.eu"},getPortletSetupShowBordersDefault:function(){return true},getScopeGroupId:function(){return"10162"},getScopeGroupIdOrLiveGroupId:function(){return"10162"},getSessionId:function(){return""},getSiteGroupId:function(){return"10162"},getURLControlPanel:function(){return"/group/control_panel?refererPlid=22182805"},getURLHome:function(){return"https\x3a\x2f\x2fecha\x2eeuropa\x2eeu\x2fweb\x2fguest"},getUserId:function(){return"10140"},getUserName:function(){return""},isAddSessionIdToURL:function(){return false},isFreeformLayout:function(){return false},isImpersonated:function(){return false},isSignedIn:function(){return false},isStateExclusive:function(){return false},isStateMaximized:function(){return false},isStatePopUp:function(){return false}},PropsValues:{NTLM_AUTH_ENABLED:false}};var themeDisplay=Liferay.ThemeDisplay;Liferay.AUI={getAvailableLangPath:function(){return"available_languages.jsp?browserId=other&themeId=dissblank_WAR_dissblanktheme&colorSchemeId=01&minifierType=js&languageId=en_GB&b=6210&t=1488923699000"},getCombine:function(){return true},getComboPath:function(){return"/combo/?browserId=other&minifierType=&languageId=en_GB&b=6210&t=1488923699000&"},getFilter:function(){return"min"},getJavaScriptRootPath:function(){return"/html/js"},getLangPath:function(){return"aui_lang.jsp?browserId=other&themeId=dissblank_WAR_dissblanktheme&colorSchemeId=01&minifierType=js&languageId=en_GB&b=6210&t=1488923699000"},getStaticResourceURLParams:function(){return"?browserId=other&minifierType=&languageId=en_GB&b=6210&t=1488923699000"}};Liferay.authToken="53j1voCr";Liferay.currentURL="\x2fregistration-dossier\x2f-\x2fregistered-dossier\x2f10167\x2f7\x2f5\x2f2";Liferay.currentURLEncoded="\x252Fregistration-dossier\x252F-\x252Fregistered-dossier\x252F10167\x252F7\x252F5\x252F2";</script> <script src="/html/js/barebone.jsp?browserId=other&amp;themeId=dissblank_WAR_dissblanktheme&amp;colorSchemeId=01&amp;minifierType=js&amp;minifierBundleId=javascript.barebone.files&amp;languageId=en_GB&amp;b=6210&amp;t=1488923699000" type="text/javascript"></script> <script type="text/javascript">Liferay.Portlet.list=["dissfactsheets_WAR_dissfactsheetsportlet"];</script> <script type="text/javascript">(function(d,e,j,h,f,c,b){d.GoogleAnalyticsObject=f;d[f]=d[f]||function(){var a=d[f].q||[];d[f].q=a;(d[f].q).push(arguments)};d[f].l=1*new Date();c=e.createElement(j);b=e.getElementsByTagName(j)[0];c.async=1;c.src=h;b.parentNode.insertBefore(c,b)})(window,document,"script","//www.google-analytics.com/analytics.js","ga");ga("create","UA-4606981-1","auto");ga("send","pageview");</script> <link href="https://echa.europa.eu/html/common/themes/css/custom.css?browserId=other&themeId=dissblank_WAR_dissblanktheme&minifierType=css&languageId=en_GB&b=6210&t=1476949693000" media="all" rel="stylesheet" type="text/css" /> <script type="text/javascript">function getCustomCookieValue(d){var b=d+"=";var a=document.cookie.split(";");for(var e=0;e<a.length;e++){var f=a[e];while(f.charAt(0)==" "){f=f.substring(1)}if(f.indexOf(b)==0){return f.substring(b.length,f.length)}}return""}function setCustomCookie(b,f,g,c){var h=new Date();if(c=="30"){h.setMonth(h.getMonth()+1)}else{h.setTime(h.getTime()+(c*24*60*60*1000))}var a="expires="+h.toGMTString();var e="";if(g){e="; path="+g}document.cookie=b+"="+f+"; "+a+e}function clearCustomCookie(a,b){var b=b||"/";document.cookie=a+"=; expires="+ +new Date+"; path="+b};</script> <script type="text/javascript">AUI().add("liferay-tags-translator",function(b){var c=Liferay.Util;var a={getTranslatedTag:function(f){var d=this;var e=d._translations[f];return e},_translations:{aaanewtag:"New Tag Test Updated 2",accounting:"accounting","active substance":"active substance",actor:"actor",additive:"additive","annex i (bpr)":"annex i (bpr)","annex ii":"annex ii","annex v":"annex v","annex xiv":"annex xiv","annex xv":"annex xv","annex xvii":"annex xvii","art. 95":"article 95",article:"article",authorisation:"authorisation",avou:".ant.com.","biocidal product":"biocidal product",biocides:"biocides","boa news (list)":"boa news (list)","boa-news (list)":"boa-news (list)","board of appeal":"board of appeal",bpc:"bpc",budget:"budget","business rules":"business rules","candidate list":"candidate list","candidate list substances in articles":"candidate list substances in articles",caracal:"caracal",casper:"casper","chemical similarity":"chemical similarity",chesar:"chesar","classification and labelling":"classification and labelling",clp:"clp",cmr:"cmr",committees:"committees","compliance check":"compliance check","confidential information":"confidential information",confidentiality:"confidentiality","conflict of interest":"conflict of interest",constituent:"constituent",consultation:"consultation",consumers:"consumers",corap:"CoRAP",cosmetics:"cosmetics",csa:"csa",csr:"csr","csr-news":"csr es news (list)","customs territory":"customs territory","data sharing":"data sharing",disputes:"disputes",dissemination:"dissemination",distributor:"distributor",dossier:"dossier","downstream user":"downstream user",dyo:"dyo","e-news":"e-news","echa accounts":"ECHA Accounts",ecotoxicology:"ecotoxicology",empty:"empty","ena tag":"ena tag",environment:"environment",epic:"epic","evaluating competent authority":"evaluating competent authority",evaluation:"evaluation",event:"event (list)","exclusion criteria":"exclusion criteria",exemption:"exemption","expired job vacancy":"expired job vacancy","expired procurement":"expired procurement",explosivity:"explosivity","exposure assessment":"exposure assessment","exposure scenario":"exposure scenario",factsheet:"Factsheet","family spc":"family spc",fees:"fees",forum:"forum",ghs:"ghs",gkons:"gkons","good laboratory practice (glp)":"good laboratory practice (glp)",guidance:"guidance","guidance panel biocides":"guidance panel biocides","guidance panel clp":"guidance panel clp","guidance panel pic":"guidance panel pic","guidance panel reach":"guidance panel reach",guidelines:"guidelines",hazard:"hazard","hazard assessment":"hazard assessment","hazard statements":"Hazard Statements","import":"import",importer:"importer",impurity:"impurity","information requirements":"information requirements",inquiry:"inquiry","intended release":"intended release",intermediates:"intermediates",invitation:"invitation",invoice:"invoice","iso 9001":"iso 9001",iuclid:"iuclid","job vacancy":"job vacancy","key press release":"Key press release","labelling and packaging":"Labelling and packaging","late pre-registration":"late pre-registration","latest news":"Latest news","lead registrant":"lead registrant",leaflet:"Leaflet","legal entity":"legal entity","letter of access":"letter of access","literature data":"literature data","main article":"main article",management:"management",manual:"manual",manufacturer:"manufacturer","master spc":"master spc",meeting:"meeting","member registrant":"member registrant","member states":"member states",memorandum:"memorandum","meta spc":"meta spc",migration:"migration",mixture:"mixture",monomer:"monomer","mutual acceptance of data (mad)":"mutual acceptance of data (mad)","mutual recognition":"mutual recognition",nanomaterial:"Nanomaterial","national authorisation":"national authorisation","naturally occurring substances":"naturally occurring substances","new family spc":"new family spc","news alert":"News alert","news item":"News item",news_template:"News template",newsletter:"newsletter (list)",nlp:"nlp","non-phase in substance":"non-phase in substance",nons:"nons","notification clp":"notification clp","notification reach":"notification reach",obligations:"obligations",odyssey:"odyssey","only representative":"only representative",pact:"pact","partner expert group":"partner expert group",pbt:"pbt",peg:"peg","phase-in substance":"phase-in substance",pic:"pic","pic - ongoing consultations (list)":"pic - ongoing consultations (list)",pictogram:"pictogram",polymer:"polymer",ppord:"ppord","pre-registration":"pre-registration","pre-sief":"pre-sief","precautionary statements":"precautionary statements","press release":"Press release","press releases":"Press Releases",procurement:"procurement","producer of articles":"producer of articles","product family":"product family","product type":"product type","public name":"public name",qsar:"qsar",r4bp:"r4bp",rac:"rac","re-import":"re-import",reach:"reach","reach 2018":"reach 2018","reach 2018 event list":"reach 2018 event list","reach-it":"reach-it","recovered substances":"Recovered substances",recruitment:"recruitment","reference item":"reference item","reference number":"reference number",registration:"registration",regulation:"regulation",restriction:"restriction","review programme":"review programme",ripe:"ripe","risk management":"risk management",rules:"rules","safety data sheet":"safety data sheet","scientific report":"Scientific report",sea:"SEA",seac:"seac",sief:"sief","simplified authorisation":"simplified authorisation",sme:"sme",software:"software","spc editor tool":"spc editor tool","spc package":"spc package","spc upload":"spc upload",sponsored:"sponsored",staff:"staff",stakeholder:"stakeholder",stock:"stock",submission:"submission","substance data sheet":"Substance Data Sheet","substance evaluation":"substance evaluation","substance identification":"substance identification",substitution:"substitution","supply chain":"supply chain",svhc:"svhc","technical dossier":"technical dossier",test:"test",testing:"testing","third party representative":"third party representative",tonnage:"tonnage",training:"training",translations:"Translations","treated article":"treated article","under maintenance":"Under maintenance","union authorisation":"union authorisation","use map sector (list)":"use map sector (list)",warning:"warning",waste:"waste",webinar:"webinar","webinar 2018 (list)":"webinar 2018 (list)","webinar-article":"webinar-article",workshop:"workshop"}};Liferay.TagsTranslator=a},"",{requires:["aui-base"]});</script> <link class="lfr-css-file" href="https&#x3a;&#x2f;&#x2f;echa&#x2e;europa&#x2e;eu&#x2f;diss-blank-theme&#x2f;css&#x2f;main&#x2e;css&#x3f;browserId&#x3d;other&#x26;themeId&#x3d;dissblank_WAR_dissblanktheme&#x26;minifierType&#x3d;css&#x26;languageId&#x3d;en_GB&#x26;b&#x3d;6210&#x26;t&#x3d;1476949693000" rel="stylesheet" type="text/css" /> <style type="text/css">.portlet-column-only{padding-right:0}</style> <style type="text/css"></style> <link type="text/css" rel="stylesheet" href="/diss-blank-theme/css/legal_notice_popup.css?css_fast_load=0"> <!--[if lt IE 9]> <script src="https://echa.europa.eu/diss-blank-theme/js/html5shiv.min.js?browserId=other&minifierType=js&languageId=en_GB&b=6210&t=1476949693000" type="text/javascript"></script> <script src="https://echa.europa.eu/diss-blank-theme/js/respond.min.js?browserId=other&minifierType=js&languageId=en_GB&b=6210&t=1476949693000" type="text/javascript"></script> <![endif]--> </head> <body class=" yui3-skin-sam controls-visible guest-site signed-out public-page site"> <div id="content"> <div class="columns-1" id="main-content" role="main"> <h2 class="page-title"> <span>Registration Dossier</span> </h2> <div class="portlet-layout"> <div class="portlet-column portlet-column-only" id="column-1"> <div class="portlet-dropzone portlet-column-content portlet-column-content-only" id="layout-column_column-1"> <div class="portlet-boundary portlet-boundary_dissfactsheets_WAR_dissfactsheetsportlet_ portlet-static portlet-static-end diss-factsheets-portlet dissemination-portlet " id="p_p_id_dissfactsheets_WAR_dissfactsheetsportlet_" > <span id="p_dissfactsheets_WAR_dissfactsheetsportlet"></span> <div class="portlet-borderless-container" style=""> <div class="portlet-body"> <!-- ECHA Imports --> <html> <head> <link rel="stylesheet" href="/diss-factsheets-portlet/css/REACH-registered_dossier.css"> </head> <body> <!-- Tags --> <!-- Define Objects --> <div id="legal_notice_overlay"></div> <script type="text/javascript">_dissfactsheets_WAR_dissfactsheetsportlet_ajaxCheckForDisclaimer();function _dissfactsheets_WAR_dissfactsheetsportlet_ajaxCheckForDisclaimer(){AUI().use("aui-base","aui-io-request-deprecated",function(a){a.io.request("https://echa.europa.eu/registration-dossier?p_p_auth=QotW8hxn&p_p_id=viewsubstances_WAR_echarevsubstanceportlet&p_p_lifecycle=0&p_p_state=exclusive&p_p_col_id=column-1&p_p_col_count=1&_viewsubstances_WAR_echarevsubstanceportlet_jspPage=%2Fhtml%2Fportlet%2Flegalnotice%2Fajax_check_for_disclaimer.jsp",{headers:{"Content-Type":"application/json",},dataType:"json",method:"POST",cache:"false",on:{success:function(){var b=a.Lang.trim(this.get("responseData"));var c=b.disclaimer;if(!c){_dissfactsheets_WAR_dissfactsheetsportlet_disseminationOpenDisclaimerPopup()}else{_dissfactsheets_WAR_dissfactsheetsportlet_cleanBlurredResultsAndOverlay()}},failure:function(){_dissfactsheets_WAR_dissfactsheetsportlet_disseminationOpenDisclaimerPopup()}}})})}function _dissfactsheets_WAR_dissfactsheetsportlet_disseminationOpenDisclaimerPopup(){AUI().use("aui-base","liferay-util-window","aui-io-deprecated","event",function(a){var b=Liferay.Util.Window.getWindow({dialog:{cssClass:"custom-fixed-selector-popup",destroyOnClose:true,centered:true,draggable:false,resizable:false,modal:false,width:700,height:550,},id:"legal-notice-popup",title:"\u004c\u0065\u0067\u0061\u006c\u0020\u004e\u006f\u0074\u0069\u0063\u0065"}).plug(a.Plugin.IO,{data:{articleId:"111779"},uri:"https://echa.europa.eu/registration-dossier?p_p_auth=QotW8hxn&p_p_id=viewsubstances_WAR_echarevsubstanceportlet&p_p_lifecycle=0&p_p_state=exclusive&p_p_col_id=column-1&p_p_col_count=1&_viewsubstances_WAR_echarevsubstanceportlet_jspPage=%2Fhtml%2Fportlet%2Flegalnotice%2Flegal_notice_popup.jsp"}).render();Liferay.provide(window,"closeDisclaimerPopup",function(c){var d=Liferay.Util.Window.getById(c);d.destroy();_dissfactsheets_WAR_dissfactsheetsportlet_cleanBlurredResultsAndOverlay()},["liferay-util-window"])})}function _dissfactsheets_WAR_dissfactsheetsportlet_cleanBlurredResultsAndOverlay(){var a=AUI().one("#legal_notice_overlay");if(a&&a!=null){a.remove()}AUI().all(".blurredResults").removeClass("blurredResults")};</script> <div class="blurredResults"> <div id="Header"> <div id="Disclaimer"> <div class="container"> <div class="row"> <div class="col-xs-12"> <p><small>Use of this information is subject to copyright laws and may require the permission of the owner of the information, as described in the <a href="/legal-notice">ECHA Legal Notice</a>.</small></p> </div><!-- col --> </div><!-- row --> </div><!-- container --> </div><!-- Disclaimer --> <div id="SubstanceWrapper"> <div id="SubstanceContainer"> <div class="container"> <div class="row"> <div class="col-xs-11"> <div id="SubstanceName"> <span class="sType">REACH</span> <h1><span>Molybdenum dioxide</span></h1> </div><!-- SubstanceName --> </div><!-- col --> <div class="col-xs-1"> <ul> <li class="hBriefProfile" data-toggle="tooltip" title="Go to Brief Profile"><a href="/brief-profile/-/briefprofile/100.038.746"></a></li> <li class="hPrint" data-toggle="tooltip" title="Print"><a href="#"></a></li> </ul> </div><!-- col --> </div><!-- row --> </div><!-- container --> </div><!-- SubstanceContainer --> </div><!-- SubstanceWrapper --> <div id="SubstanceDescription"> <div class="container"> <div class="row"> <div class="col-xs-12"> <p><small> <strong> EC number: 242-637-9 <span>|</span> CAS number: 18868-43-4 </strong> </small></p> </div><!-- col --> </div><!-- row --> </div><!-- container --> </div><!-- SubstanceDescription --> </div><!-- Header --> <div id="Main"> <div class="container"> <div class="row"> <div id="MainNav" class="NavCol col-xs-2"> <ul class="nav nav-tabs MainNavList" role="tablist"> <li id="MainNav1" class="NoSubNav "> <a href="/registration-dossier/-/registered-dossier/10167/1" > <i class="Rd-Icon Rd-GeneralInfo"></i> General information</a> </li> <li id="MainNav2" class="" role="presentation"> <a href="#SubNav2" aria-controls="SubNav2" role="tab" data-toggle="tab" > <i class="Rd-Icon Rd-CnL"></i> Classification & Labelling & PBT assessment</a> </li> <li id="MainNav3" class="" role="presentation"> <a href="#SubNav3" aria-controls="SubNav3" role="tab" data-toggle="tab" > <i class="Rd-Icon Rd-Manufacture"></i> Manufacture, use & exposure</a> </li> <li id="MainNav4" class="" role="presentation"> <a href="#SubNav4" aria-controls="SubNav4" role="tab" data-toggle="tab" > <i class="Rd-Icon Rd-Physical"></i> Physical & Chemical properties</a> </li> <li id="MainNav5" class="" role="presentation"> <a href="#SubNav5" aria-controls="SubNav5" role="tab" data-toggle="tab" > <i class="Rd-Icon Rd-Environmental"></i> Environmental fate & pathways</a> </li> <li id="MainNav6" class="" role="presentation"> <a href="#SubNav6" aria-controls="SubNav6" role="tab" data-toggle="tab" > <i class="Rd-Icon Rd-Ecotoxicological"></i> Ecotoxicological information</a> </li> <li id="MainNav7" class="" role="presentation"> <a href="#SubNav7" aria-controls="SubNav7" role="tab" data-toggle="tab" > <i class="Rd-Icon Rd-Toxicological"></i> Toxicological information</a> </li> <li id="MainNav8" class="NoSubNav mDisabled"> <a > <i class="Rd-Icon Rd-Guidance"></i> Analytical methods</a> </li> <li id="MainNav9" class="NoSubNav "> <a href="/registration-dossier/-/registered-dossier/10167/9" > <i class="Rd-Icon Rd-Guidance"></i> Guidance on safe use</a> </li> <li id="MainNav10" class="NoSubNav "> <a href="/registration-dossier/-/registered-dossier/10167/10" > <i class="Rd-Icon Rd-Assessment"></i> Assessment reports</a> </li> <li id="MainNav11" class="NoSubNav "> <a href="/registration-dossier/-/registered-dossier/10167/11" > <i class="Rd-Icon Rd-Reference"></i> Reference substances</a> </li> </ul> </div><!--MainNav --> <div id="SubNav" class="NavCol col-xs-2"> <div class="tab-content"> <div role="tabpanel" class="tab-pane" id="SubNav1"> </div><!-- SubNav1 --> <div role="tabpanel" class="tab-pane" id="SubNav2"> <ul class="treeview"> <li id="SubNav2_1" class=""> <a href="/registration-dossier/-/registered-dossier/10167/2/1" > GHS</a> </li> <li id="SubNav2_2" class=""> <a href="/registration-dossier/-/registered-dossier/10167/2/2" > DSD - DPD</a> </li> <li id="SubNav2_3" class=""> <a href="/registration-dossier/-/registered-dossier/10167/2/3" > PBT assessment</a> </li> </ul> </div><!-- SubNav2 --> <div role="tabpanel" class="tab-pane" id="SubNav3"> <ul class="treeview"> <li id="SubNav3_1" class="">Life Cycle description <ul> <li id="SubNav3_1_1" class="mDisabled"> <a > No identified uses</a> </li> <li id="SubNav3_1_2" class=""> <a href="/registration-dossier/-/registered-dossier/10167/3/1/2" > Manufacture</a> </li> <li id="SubNav3_1_3" class="mDisabled"> <a > Formulation</a> </li> <li id="SubNav3_1_4" class=""> <a href="/registration-dossier/-/registered-dossier/10167/3/1/4" > Uses at industrial sites</a> </li> <li id="SubNav3_1_5" class="mDisabled"> <a > Uses by professional workers</a> </li> <li id="SubNav3_1_6" class="mDisabled"> <a > Consumer Uses</a> </li> <li id="SubNav3_1_7" class=""> <a href="/registration-dossier/-/registered-dossier/10167/3/1/7" > Article service life</a> </li> </ul> </li> <li id="SubNav3_2" class="mDisabled">Uses advised against <ul> <li id="SubNav3_2_1" class="mDisabled"> <a > Formulation</a> </li> <li id="SubNav3_2_2" class="mDisabled"> <a > Uses at industrial sites</a> </li> <li id="SubNav3_2_3" class="mDisabled"> <a > Uses by professional workers</a> </li> <li id="SubNav3_2_4" class="mDisabled"> <a > Consumer Uses</a> </li> </ul> </li> <li id="SubNav3_3" class="">Exposure Scenarios; exposure and risk assessment <ul> <li id="SubNav3_3_1" class="mDisabled"> <a > Exposure Scenarios, exposure and local assessment</a> </li> <li id="SubNav3_3_2" class="mDisabled"> <a > Environmental assessment for aggregated sources</a> </li> <li id="SubNav3_3_3" class=""> <a href="/registration-dossier/-/registered-dossier/10167/3/3/3" > Generic exposure potential</a> </li> </ul> </li> <li id="SubNav3_4" class="mDisabled"> <a > Biocidal Information</a> </li> </ul> </div><!-- SubNav3 --> <div role="tabpanel" class="tab-pane" id="SubNav4"> <ul class="treeview"> <li id="SubNav4_1" class="mDisabled"> <a > Endpoint summary</a> </li> <li id="SubNav4_2" class=""> <a href="/registration-dossier/-/registered-dossier/10167/4/2" > Appearance / physical state / colour</a> </li> <li id="SubNav4_3" class=""> <a href="/registration-dossier/-/registered-dossier/10167/4/3" > Melting point / freezing point</a> </li> <li id="SubNav4_4" class=""> <a href="/registration-dossier/-/registered-dossier/10167/4/4" > Boiling point</a> </li> <li id="SubNav4_5" class=""> <a href="/registration-dossier/-/registered-dossier/10167/4/5" > Density</a> </li> <li id="SubNav4_6" class=""> <a href="/registration-dossier/-/registered-dossier/10167/4/6" > Particle size distribution (Granulometry)</a> </li> <li id="SubNav4_7" class=""> <a href="/registration-dossier/-/registered-dossier/10167/4/7" > Vapour pressure</a> </li> <li id="SubNav4_8" class=""> <a href="/registration-dossier/-/registered-dossier/10167/4/8" > Partition coefficient</a> </li> <li id="SubNav4_9" class=""> <a href="/registration-dossier/-/registered-dossier/10167/4/9" > Water solubility</a> </li> <li id="SubNav4_10" class="mDisabled"> <a > Solubility in organic solvents / fat solubility</a> </li> <li id="SubNav4_11" class=""> <a href="/registration-dossier/-/registered-dossier/10167/4/11" > Surface tension</a> </li> <li id="SubNav4_12" class=""> <a href="/registration-dossier/-/registered-dossier/10167/4/12" > Flash point</a> </li> <li id="SubNav4_13" class=""> <a href="/registration-dossier/-/registered-dossier/10167/4/13" > Auto flammability</a> </li> <li id="SubNav4_14" class=""> <a href="/registration-dossier/-/registered-dossier/10167/4/14" > Flammability</a> </li> <li id="SubNav4_15" class=""> <a href="/registration-dossier/-/registered-dossier/10167/4/15" > Explosiveness</a> </li> <li id="SubNav4_16" class=""> <a href="/registration-dossier/-/registered-dossier/10167/4/16" > Oxidising properties</a> </li> <li id="SubNav4_17" class="mDisabled"> <a > Oxidation reduction potential</a> </li> <li id="SubNav4_18" class=""> <a href="/registration-dossier/-/registered-dossier/10167/4/18" > Stability in organic solvents and identity of relevant degradation products</a> </li> <li id="SubNav4_19" class="mDisabled"> <a > Storage stability and reactivity towards container material</a> </li> <li id="SubNav4_20" class="mDisabled"> <a > Stability: thermal, sunlight, metals</a> </li> <li id="SubNav4_21" class="mDisabled"> <a > pH</a> </li> <li id="SubNav4_22" class=""> <a href="/registration-dossier/-/registered-dossier/10167/4/22" > Dissociation constant</a> </li> <li id="SubNav4_23" class=""> <a href="/registration-dossier/-/registered-dossier/10167/4/23" > Viscosity</a> </li> <li id="SubNav4_24" class="mDisabled"> <a > Additional physico-chemical information</a> </li> </ul> </div><!-- SubNav4 --> <div role="tabpanel" class="tab-pane" id="SubNav5"> <ul class="treeview"> <li id="SubNav5_1" class="mDisabled"> <a > Endpoint summary</a> </li> <li id="SubNav5_2" class="">Stability <ul> <li id="SubNav5_2_1" class="mDisabled"> <a > Endpoint summary</a> </li> <li id="SubNav5_2_2" class="mDisabled"> <a > Phototransformation in air</a> </li> <li id="SubNav5_2_3" class=""> <a href="/registration-dossier/-/registered-dossier/10167/5/2/3" > Hydrolysis</a> </li> <li id="SubNav5_2_4" class="mDisabled"> <a > Phototransformation in water</a> </li> <li id="SubNav5_2_5" class="mDisabled"> <a > Phototransformation in soil</a> </li> </ul> </li> <li id="SubNav5_3" class="">Biodegradation <ul> <li id="SubNav5_3_1" class="mDisabled"> <a > Endpoint summary</a> </li> <li id="SubNav5_3_2" class=""> <a href="/registration-dossier/-/registered-dossier/10167/5/3/2" > Biodegradation in water: screening tests</a> </li> <li id="SubNav5_3_3" class=""> <a href="/registration-dossier/-/registered-dossier/10167/5/3/3" > Biodegradation in water and sediment: simulation tests</a> </li> <li id="SubNav5_3_4" class=""> <a href="/registration-dossier/-/registered-dossier/10167/5/3/4" > Biodegradation in soil</a> </li> <li id="SubNav5_3_5" class="mDisabled"> <a > Mode of degradation in actual use</a> </li> </ul> </li> <li id="SubNav5_4" class="">Bioaccumulation <ul> <li id="SubNav5_4_1" class="mDisabled"> <a > Endpoint summary</a> </li> <li id="SubNav5_4_2" class=""> <a href="/registration-dossier/-/registered-dossier/10167/5/4/2" > Bioaccumulation: aquatic / sediment</a> </li> <li id="SubNav5_4_3" class=""> <a href="/registration-dossier/-/registered-dossier/10167/5/4/3" > Bioaccumulation: terrestrial</a> </li> </ul> </li> <li id="SubNav5_5" class="">Transport and distribution <ul> <li id="SubNav5_5_1" class="mDisabled"> <a > Endpoint summary</a> </li> <li id="SubNav5_5_2" class=""> <a href="/registration-dossier/-/registered-dossier/10167/5/5/2" > Adsorption / desorption</a> </li> <li id="SubNav5_5_3" class="mDisabled"> <a > Henry's Law constant</a> </li> <li id="SubNav5_5_4" class="mDisabled"> <a > Distribution modelling</a> </li> <li id="SubNav5_5_5" class="mDisabled"> <a > Other distribution data</a> </li> </ul> </li> <li id="SubNav5_6" class="" >Environmental data <ul> <li id="SubNav5_6_1" class="mDisabled"> <a > Endpoint summary</a> </li> <li id="SubNav5_6_2" class=""> <a href="/registration-dossier/-/registered-dossier/10167/5/6/2" > Monitoring data</a> </li> <li id="SubNav5_6_3" class="mDisabled"> <a > Field studies</a> </li> </ul> </li> <li id="SubNav5_7" class="mDisabled" > <a > Additional information on environmental fate and behaviour</a> </li> </ul> </div><!-- SubNav5 --> <div role="tabpanel" class="tab-pane" id="SubNav6"> <ul class="treeview"> <li id="SubNav6_1" class=""> <a href="/registration-dossier/-/registered-dossier/10167/6/1" > Ecotoxicological Summary</a> </li> <li id="SubNav6_2" class="">Aquatic toxicity <ul> <li id="SubNav6_2_1" class="mDisabled"> <a > Endpoint summary</a> </li> <li id="SubNav6_2_2" class=""> <a href="/registration-dossier/-/registered-dossier/10167/6/2/2" > Short-term toxicity to fish</a> </li> <li id="SubNav6_2_3" class=""> <a href="/registration-dossier/-/registered-dossier/10167/6/2/3" > Long-term toxicity to fish</a> </li> <li id="SubNav6_2_4" class=""> <a href="/registration-dossier/-/registered-dossier/10167/6/2/4" > Short-term toxicity to aquatic invertebrates</a> </li> <li id="SubNav6_2_5" class=""> <a href="/registration-dossier/-/registered-dossier/10167/6/2/5" > Long-term toxicity to aquatic invertebrates</a> </li> <li id="SubNav6_2_6" class=""> <a href="/registration-dossier/-/registered-dossier/10167/6/2/6" > Toxicity to aquatic algae and cyanobacteria</a> </li> <li id="SubNav6_2_7" class=""> <a href="/registration-dossier/-/registered-dossier/10167/6/2/7" > Toxicity to aquatic plants other than algae</a> </li> <li id="SubNav6_2_8" class=""> <a href="/registration-dossier/-/registered-dossier/10167/6/2/8" > Toxicity to microorganisms</a> </li> <li id="SubNav6_2_9" class=""> <a href="/registration-dossier/-/registered-dossier/10167/6/2/9" > Toxicity to other aquatic organisms</a> </li> </ul> </li> <li id="SubNav6_3" class=""> <a href="/registration-dossier/-/registered-dossier/10167/6/3" > Sediment toxicity</a> </li> <li id="SubNav6_4" class="">Terrestrial toxicity <ul> <li id="SubNav6_4_1" class="mDisabled"> <a > Endpoint summary</a> </li> <li id="SubNav6_4_2" class=""> <a href="/registration-dossier/-/registered-dossier/10167/6/4/2" > Toxicity to soil macroorganisms except arthropods</a> </li> <li id="SubNav6_4_3" class=""> <a href="/registration-dossier/-/registered-dossier/10167/6/4/3" > Toxicity to terrestrial arthropods</a> </li> <li id="SubNav6_4_4" class=""> <a href="/registration-dossier/-/registered-dossier/10167/6/4/4" > Toxicity to terrestrial plants</a> </li> <li id="SubNav6_4_5" class=""> <a href="/registration-dossier/-/registered-dossier/10167/6/4/5" > Toxicity to soil microorganisms</a> </li> <li id="SubNav6_4_6" class=""> <a href="/registration-dossier/-/registered-dossier/10167/6/4/6" > Toxicity to birds</a> </li> <li id="SubNav6_4_7" class=""> <a href="/registration-dossier/-/registered-dossier/10167/6/4/7" > Toxicity to other above-ground organisms</a> </li> </ul> </li> <li id="SubNav6_5" class="mDisabled"> <a > Biological effects monitoring</a> </li> <li id="SubNav6_6" class="mDisabled"> <a > Biotransformation and kinetics</a> </li> <li id="SubNav6_7"class="mDisabled"> <a > Additional ecotoxological information</a> </li> </ul> </div><!-- SubNav6 --> <div role="tabpanel" class="tab-pane" id="SubNav7"> <ul class="treeview"> <li id="SubNav7_1" class=""> <a href="/registration-dossier/-/registered-dossier/10167/7/1" > Toxicological Summary</a> </li> <li id="SubNav7_2" class="">Toxicokinetics, metabolism and distribution <ul> <li id="SubNav7_2_1" class="mDisabled"> <a > Endpoint summary</a> </li> <li id="SubNav7_2_2" class=""> <a href="/registration-dossier/-/registered-dossier/10167/7/2/2" > Basic toxicokinetics</a> </li> <li id="SubNav7_2_3" class=""> <a href="/registration-dossier/-/registered-dossier/10167/7/2/3" > Dermal absorption</a> </li> </ul> </li> <li id="SubNav7_3" class="">Acute Toxicity <ul> <li id="SubNav7_3_1" class="mDisabled"> <a > Endpoint summary</a> </li> <li id="SubNav7_3_2" class=""> <a href="/registration-dossier/-/registered-dossier/10167/7/3/2" > Acute Toxicity: oral</a> </li> <li id="SubNav7_3_3" class=""> <a href="/registration-dossier/-/registered-dossier/10167/7/3/3" > Acute Toxicity: inhalation</a> </li> <li id="SubNav7_3_4" class=""> <a href="/registration-dossier/-/registered-dossier/10167/7/3/4" > Acute Toxicity: dermal</a> </li> <li id="SubNav7_3_5" class="mDisabled"> <a > Acute Toxicity: other routes</a> </li> </ul> </li> <li id="SubNav7_4" class="" >Irritation / corrosion <ul> <li id="SubNav7_4_1" class="mDisabled"> <a > Endpoint summary</a> </li> <li id="SubNav7_4_2" class=""> <a href="/registration-dossier/-/registered-dossier/10167/7/4/2" > Skin irritation / corrosion</a> </li> <li id="SubNav7_4_3" class=""> <a href="/registration-dossier/-/registered-dossier/10167/7/4/3" > Eye irritation</a> </li> </ul> </li> <li id="SubNav7_5" class="">Sensitisation <ul> <li id="SubNav7_5_1" class="mDisabled"> <a > Endpoint summary</a> </li> <li id="SubNav7_5_2" class=""> <a href="/registration-dossier/-/registered-dossier/10167/7/5/2" > Skin sensitisation</a> </li> <li id="SubNav7_5_3" class="mDisabled"> <a > Respiratory sensitisation</a> </li> </ul> </li> <li id="SubNav7_6" class="">Repeated dose toxicity <ul> <li id="SubNav7_6_1" class="mDisabled"> <a > Endpoint summary</a> </li> <li id="SubNav7_6_2" class=""> <a href="/registration-dossier/-/registered-dossier/10167/7/6/2" > Repeated dose toxicity: oral</a> </li> <li id="SubNav7_6_3" class=""> <a href="/registration-dossier/-/registered-dossier/10167/7/6/3" >Repeated dose toxicity: inhalation</a> </li> <li id="SubNav7_6_4" class=""> <a href="/registration-dossier/-/registered-dossier/10167/7/6/4" > Repeated dose toxicity: dermal</a> </li> <li id="SubNav7_6_5" class="mDisabled"> <a > Repeated dose toxicity: other routes</a> </li> </ul> </li> <li id="SubNav7_7" class="">Genetic toxicity <ul> <li id="SubNav7_7_1" class="mDisabled"> <a > Endpoint summary</a> </li> <li id="SubNav7_7_2" class=""> <a href="/registration-dossier/-/registered-dossier/10167/7/7/2" > Genetic toxicity: in vitro</a> </li> <li id="SubNav7_7_3" class=""> <a href="/registration-dossier/-/registered-dossier/10167/7/7/3" > Genetic toxicity: in vivo</a> </li> </ul> </li> <li id="SubNav7_8" class=""> <a href="/registration-dossier/-/registered-dossier/10167/7/8" > Carcinogenicity</a> </li> <li id="SubNav7_9" class="">Toxicity to reproduction <ul> <li id="SubNav7_9_1" class="mDisabled"> <a > Endpoint summary</a> </li> <li id="SubNav7_9_2" class=""> <a href="/registration-dossier/-/registered-dossier/10167/7/9/2" > Toxicity to reproduction</a> </li> <li id="SubNav7_9_3" class=""> <a href="/registration-dossier/-/registered-dossier/10167/7/9/3" > Developmental toxicity / teratogenicity</a> </li> <li id="SubNav7_9_4" class="mDisabled"> <a > Toxicity to reproduction: other studies</a> </li> </ul> </li> <li id="SubNav7_10" class="mDisabled">Specific investigations <ul> <li id="SubNav7_10_1" class="mDisabled"> <a > Neurotoxicity</a> </li> <li id="SubNav7_10_3" class="mDisabled"> <a > Immunotoxicity</a> </li> <li id="SubNav7_10_3" class="mDisabled"> <a > Specific investigations: other studies</a> </li> </ul> </li> <li id="SubNav7_11" class="">Exposure related observations in humans <ul> <li id="SubNav7_11_1" class="mDisabled"> <a > Endpoint summary</a> </li> <li id="SubNav7_11_2" class="mDisabled"> <a > Health surveillance data</a> </li> <li id="SubNav7_11_3" class="mDisabled"> <a > Epidemiological data</a> </li> <li id="SubNav7_11_4" class="mDisabled"> <a > Direct observations: clinical cases, poisoning incidents and other</a> </li> <li id="SubNav7_11_5" class="mDisabled"> <a > Sensitisation data (human)</a> </li> <li id="SubNav7_11_6" class=""> <a href="/registration-dossier/-/registered-dossier/10167/7/11/6" > Exposure related observations in humans: other data</a> </li> </ul> </li> <li id="SubNav7_12" class="mDisabled"> <a > Toxic effects on livestock and pets</a> </li> <li id="SubNav7_13" class="mDisabled"> <a > Additional toxicological data</a> </li> </ul> </div><!-- SubNav7 --> <div role="tabpanel" class="tab-pane" id="SubNav8"> </div><!-- SubNav8 --> <div role="tabpanel" class="tab-pane" id="SubNav9"> </div><!-- SubNav9 --> <div role="tabpanel" class="tab-pane" id="SubNav10"> </div><!-- SubNav10 --> <div role="tabpanel" class="tab-pane" id="SubNav11"> </div><!-- SubNav11 --> <div role="tabpanel" class="tab-pane" id="SubNav12"> </div><!-- SubNav12 --> </div> </div><!-- SubNav --> <div id="MainContent" class="col-xs-8"><div id="Section"><div id="SectionHeaderWrapper"> <div id="SectionHeader"> <div class="SelectedSectionIcon"> <img src="https://echa.europa.eu/diss-blank-theme/images/factsheets/A-REACH/factsheet/print_toxicological-information.png" alt=""/> </div> <div class="SelectedSection"> Toxicological information </div> <h2>Skin sensitisation</h2> <div class="CurrentView input-group"> <span class="input-group-addon" id="basic-addon1">Currently viewing:</span> <select class="form-control"> <option value="/registration-dossier/-/registered-dossier/10167/7/5/2/?documentUUID=05b92c4c-d74f-4a39-9c8e-5b107284178b" selected="selected">Exp Key Skin sensitisation.002</option><option value="/registration-dossier/-/registered-dossier/10167/7/5/2/?documentUUID=59ad4e6a-54a9-4ca9-8e58-17b25c9a3a56">Exp Key Skin sensitisation.004</option><option value="/registration-dossier/-/registered-dossier/10167/7/5/2/?documentUUID=53d88a7d-e0c9-4d7f-a438-2cef5ff102dc">Exp Key Skin sensitisation.001</option><option value="/registration-dossier/-/registered-dossier/10167/7/5/2/?documentUUID=79c0e63c-592c-4137-a004-43083cdb1864">Exp Key Skin sensitisation.003</option> </select> </div><div id="SectionAnchors"> <ul class="nav" role="tablist"> <li><a href="#sAdministrativeData">Administrative data</a></li><li><a href="#sDataSource">Data source</a></li><li><a href="#sMaterialsAndMethods">Materials and methods</a></li><li><a href="#sResultsAndDiscussions">Results and discussion</a></li><li><a href="#sApplicantSummaryAndConclusion">Applicant's summary and conclusion</a></li> </ul> </div><!-- SectionAnchors --></div><!-- SectionHeader --> </div><!-- SectionHeaderWrapper --><div id="SectionContent"><h3 id="sAdministrativeData">Administrative data</h3><dl class="HorDL"><dt>Purpose flag:</dt><dd>key study</dd><dt>Study result type:</dt><dd>experimental result</dd><dt>Study period:</dt><dd class="UserEntry">1992-09-15 to 1992-10-24</dd><dt>Reliability:</dt><dd>1 (reliable without restriction)</dd><dt>Rationale for reliability incl. deficiencies:</dt><dd class="UserEntry"> Guideline study (The study was conducted according to the OECD 406 "Skin sensitisation", adopted 12 May 1981. When the study was conducted the OECD 406 of 10.07.1992 was already effective, so the study was compared to that guideline.)<br/>Minor deviations with no effect on the study results or on the high reliability of the study: <br/>- 0.2 mL of 10 % w/w sodium lauryl sulphate in Vaseline was used in order to create local irritation after intradermal injections instead of 0.5 mL. <br/>- another grading scale than the one stated in the guideline was used. <br/></dd></dl></dl><h3 id="sDataSource">Data source</h3><div class="sBlock"><h5>Reference</h5><dl class="HorDL"><dt>Reference Type:</dt><dd>study report</dd><dt>Title:</dt><dd class="UserEntry">Unnamed</dd><dt>Year:</dt><dd class="UserEntry">1

					Line=Line.substring(Line.indexOf(seek)+seek.length(), Line.length());


					Vector <String>urls=new Vector<String>();

					while (Line.indexOf(seek2)>-1) {
						Line=Line.substring(Line.indexOf(seek2)+seek2.length(), Line.length());

						String strURL="https://echa.europa.eu"+Line.substring(0,Line.indexOf("\""));

						String option=Line.substring(Line.indexOf(">")+1,Line.length());
						option=option.substring(0,option.indexOf("<"));

						if (option.indexOf(".")>-1) {
							option=option.substring(option.indexOf(".")+1,option.length());
						} else {
							option=option.substring(0,option.indexOf(" ")).trim();
						}

						urls.add(option+"\t"+strURL);						
						//						System.out.println(strURL);
						//						System.out.println(filei.getName()+"\t"+Line);
						//						System.out.println(filei.getName()+"\t"+option);
					}


					String num=filename.substring(0,filename.indexOf("."));

					for (int j=1;j<urls.size();j++) {//skip first one
						String urloption=urls.get(j);
						String optionj=urloption.substring(0,urloption.indexOf("\t"));
						String strURL=urloption.substring(urloption.indexOf("\t")+1,urloption.length());
						String destFilePath=folderPath+"/"+num+"_"+optionj+".html";

						File destFile=new File(destFilePath);
						if (!destFile.exists())	{
							this.downloadFile(strURL, destFilePath);

							System.out.println(i+"\t"+filename+"\t"+optionj);


							Thread.sleep(5000);//wait so dont get locked out from website
						}

					}

					s_runtime.gc();//run garbage collection to save memory


				} else {
					//					System.out.println("multiple options not available for "+filei.getName());
				}

				br.close();


			} catch (Exception ex) {
				ex.printStackTrace();
			}






		}
	}

	String parseOptions(String str,JsonArray array,String seek,String seek2) {

		while (str.indexOf(seek)>-1) {
			str=str.substring(str.indexOf(seek)+seek.length(), str.length());
			String parse=str.substring(0,str.indexOf(seek2));
			JsonObject reference=new JsonObject();
			this.getJSONData(reference, parse);
			array.add(reference);
		}
		//		System.out.println(str);
		return str;
	}


	void parseSensitizationFile(String fileNumber,String htmlFolderPath,String destFolderPath) {

		try {

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting().serializeNulls();// makes it multiline and readable
			Gson gson = builder.create();

			String seek="<div class=\"panel-body\"><dl class=\"HorDL\">";
			String seek2="</div><!-- panel-body -->";

			String seek3="</div><dl class=\"HorDL\">";

			String seekGuidelineOptions="<h5 id=\"sGuideline\">Test guideline<span class=\"ExpandCollapse\">";
			String seekGuideline="<h5>Test guideline</h5><dl class=\"HorDL\">";

			String seekTestMaterialOptions="<h5 id=\"sIdentity\">Test material identity<span class=\"ExpandCollapse\">";
			String seekTestMaterial="<h5>Test material identity</h5><dl class=\"HorDL\">";

			String seek11="<h4 class=\"panel-title\">";

			String strAdminData="Administrative data</h3>";
			String strDataSource="Data source</h3>";
			String strMatMethods="Materials and methods</h3>";
			String strResultsDiscussion="Results and discussion</h3>";
			String strApplicantSummaryAndConclusion="Applicant's summary and conclusion</h3";


			BufferedReader br=new BufferedReader(new FileReader(htmlFolderPath+"/"+fileNumber+".html"));

			String Line="";
			while (true) {
				String currentLine=br.readLine();
				if (currentLine==null) break;
				Line+=currentLine;
			}

			br.close();


			JsonObject record=new JsonObject();//record to store all info

			String name=Line.substring(Line.indexOf("<h1><span>")+"<h1><span>".length(),Line.indexOf("</span></h1> </div><!-- SubstanceName -->"));
			record.addProperty("chemical_name", name);

			String ECnumber=Line.substring(Line.indexOf("EC number: ")+"EC number: ".length(),Line.length());
			ECnumber=ECnumber.substring(0,ECnumber.indexOf(" <span>"));
			//			System.out.println(ECnumber);
			record.addProperty("EC_number", ECnumber);

			String CASnumber=Line.substring(Line.indexOf("CAS number: ")+"CAS number: ".length(),Line.length());
			CASnumber=CASnumber.substring(0,CASnumber.indexOf(" </strong>"));
			//			System.out.println(ECnumber);
			record.addProperty("CAS number", CASnumber);


			//**************************************************************************************************
			//Administrative Data:
			JsonObject admindata=new JsonObject();
			record.add("Administrative data", admindata);

			if (Line.indexOf(strAdminData)>-1) {
				String admindataHTML=Line.substring(Line.indexOf(strAdminData), Line.indexOf(strDataSource));
				this.getJSONData(admindata, admindataHTML);
			}

			//			System.out.println(admindata.toJSONString());

			//**************************************************************************************************
			// Data source:
			JsonObject datasource=new JsonObject();
			record.add("Data source", datasource);

			if (Line.indexOf(strDataSource) > -1) {

				String dataSourceHTML = Line.substring(Line.indexOf(strDataSource), Line.indexOf(strMatMethods));
				JsonArray references = new JsonArray();
				datasource.add("REFERENCES", references);

				if (dataSourceHTML.indexOf("Reference<span class=\"ExpandCollapse\">") > -1) {
					this.parseOptions(dataSourceHTML, references, seek, seek2);
				} else {
					JsonObject reference = new JsonObject();
					this.getJSONData(reference, dataSourceHTML);
					references.add(reference);
				}

			}
			//			System.out.println(datasource);

			//**************************************************************************************************
			// Materials and methods
			JsonObject matmethods=new JsonObject();
			record.add("Materials and methods", matmethods);

			//			System.out.println(Line.indexOf(strMatMethods));
			//			System.out.println(Line.indexOf(strResultsDiscussion));


			if (Line.indexOf(strMatMethods) > -1) {

				String matMethodsHTML = Line.substring(Line.indexOf(strMatMethods), Line.indexOf(strResultsDiscussion));

				JsonArray testGuidelines = new JsonArray();
				matmethods.add("test guidelines", testGuidelines);


				if (matMethodsHTML.indexOf(seekGuidelineOptions) > -1) {

					String parse=matMethodsHTML.substring(matMethodsHTML.indexOf(seekGuidelineOptions),matMethodsHTML.length());

					if (parse.indexOf("Test material identity")>-1) {
						parse=parse.substring(0,parse.indexOf("Test material identity"));
					}

					this.parseOptions(parse, testGuidelines, seek, seek2);

					//					System.out.println(matMethodsHTML);

					// skip past the guidelines:
					// matMethodsHTML=matMethodsHTML.substring(matMethodsHTML.indexOf(seek2),matMethodsHTML.length());

					// System.out.println("skip past guidelines:"+fileNumber);

				} else if (matMethodsHTML.indexOf(seekGuideline) > -1 && matMethodsHTML.indexOf(seek3) > -1) {

					String parse = matMethodsHTML.substring(matMethodsHTML.indexOf(seekGuideline),
							matMethodsHTML.indexOf(seek3));

					JsonObject guideline = new JsonObject();
					this.getJSONData(guideline, parse);
					testGuidelines.add(guideline);

					// skip past the guidelines:
					// matMethodsHTML=matMethodsHTML.substring(matMethodsHTML.indexOf(seek2),matMethodsHTML.length());

				} else {
					// No test guideline
					// System.out.println(matMethodsHTML);
				}

				if (matMethodsHTML.indexOf("<h5") > -1) {
					// parse Methods, GLP compliance, Type of study
					String parseMethodsInfo = matMethodsHTML.substring(0, matMethodsHTML.indexOf("<h5"));
					this.getJSONData(matmethods, parseMethodsInfo);
				}

				JsonArray testMaterials = new JsonArray();
				matmethods.add("test materials", testMaterials);

				//				System.out.println(matMethodsHTML);


				if (matMethodsHTML
						.indexOf(seekTestMaterialOptions) > -1) {

					String parse=matMethodsHTML.substring(matMethodsHTML.indexOf(seekTestMaterialOptions),matMethodsHTML.length());
					this.parseOptions(parse, testMaterials, seek, seek2);


					// skip past the test materials:
					// System.out.println("skip past test
					// materials:"+fileNumber);

					// matMethodsHTML=matMethodsHTML.substring(matMethodsHTML.indexOf("<dt>Details
					// on test material:"),matMethodsHTML.length());

				} else if (matMethodsHTML.indexOf("<h5>Test material identity") > -1) {

					// System.out.println(matMethodsHTML);

					String parse = matMethodsHTML.substring(matMethodsHTML.indexOf(seekTestMaterial), matMethodsHTML.length());
					parse = parse.substring(0, parse.indexOf("</div>"));
					// System.out.println(parse);

					JsonObject testMaterial = new JsonObject();
					this.getJSONData(testMaterial, parse);
					testMaterials.add(testMaterial);

					// skip past this part:
					// matMethodsHTML=matMethodsHTML.substring(matMethodsHTML.indexOf("</div><dl
					// class=\"HorDL\">"),matMethodsHTML.length());

				} else {
					// TODO???
					//					System.out.println("here"+fileNumber);
				}


				// get rest of info in materials and methods:
				this.getJSONData(matmethods, matMethodsHTML);

				//if accidentally have extra copy data from above getJSONData that was stored in matmethods instead of proper arrays:
				this.removeField(matmethods, "Qualifier");
				this.removeField(matmethods, "Guideline");
				this.removeField(matmethods, "Deviations");

				this.removeField(matmethods, "Identity");
				this.removeField(matmethods, "Identifier");
			}

			//**************************************************************************************************
			//Results and discussion:
			JsonObject resultsdiscussion=new JsonObject();
			record.add("Results and discussion", resultsdiscussion);


			//			System.out.println("*"+Line+"*");

			if (Line.indexOf(strResultsDiscussion)>-1) {

				String resultsDiscussionHTML=Line.substring(Line.indexOf(strResultsDiscussion),Line.indexOf(strApplicantSummaryAndConclusion));

				JsonArray testResults=new JsonArray();
				resultsdiscussion.add("test results", testResults);

				if (resultsDiscussionHTML.indexOf("<h5 id=\"sResultsOfTest\">Results of test")>-1) {

					while (resultsDiscussionHTML.indexOf(seek11)>-1) {
						resultsDiscussionHTML=resultsDiscussionHTML.substring(resultsDiscussionHTML.indexOf(seek11)+seek11.length(), resultsDiscussionHTML.length());
						String parse=resultsDiscussionHTML.substring(0,resultsDiscussionHTML.indexOf(seek2));
						JsonObject testResult=new JsonObject();
						this.getJSONData(testResult, parse);
						testResults.add(testResult);
					}

					//skip past this part:
					//				resultsDiscussionHTML=resultsDiscussionHTML.substring(resultsDiscussionHTML.indexOf("<!-- panel -->"),resultsDiscussionHTML.length());

					//				System.out.println("skip past test results:"+fileNumber);

					//get rest of results and discussion:
					this.getJSONData(resultsdiscussion,resultsDiscussionHTML);

					//Just in case delete these fields if they were just data from last test result:
					this.removeField(resultsdiscussion, "Reading");
					this.removeField(resultsdiscussion, "Hours after challenge");
					this.removeField(resultsdiscussion, "Group");
					this.removeField(resultsdiscussion, "Dose level");
					this.removeField(resultsdiscussion, "Reading");
					this.removeField(resultsdiscussion, "No. with + reactions");
					this.removeField(resultsdiscussion, "Total no. in group");

				} else {//single test result
					//				System.out.println("single test result:"+fileNumber);
					//				System.out.println(resultsDiscussionHTML);

					JsonObject testResult=new JsonObject();
					this.getJSONData(testResult,resultsDiscussionHTML);
					testResults.add(testResult);				
				} 
			}

			//			System.out.println(resultsdiscussion);

			//**************************************************************************************************
			// Applicants summary and conclusions
			JsonObject summary_conclusion=new JsonObject();
			record.add("Applicant's summary and conclusion", summary_conclusion);

			if (Line.indexOf(strApplicantSummaryAndConclusion)>-1) {
				String data=Line.substring(Line.indexOf(strApplicantSummaryAndConclusion), Line.indexOf("<!-- SectionContent -->"));
				this.getJSONData(summary_conclusion, data);
			}
			//			System.out.println(summary_conclusion.toJSONString());

			//**************************************************************************************************


			String results=gson.toJson(record);
			//output final record:
			//			System.out.println(record);

			//get rid of unicode chars:
			//TODO makes pretty but causes parsing errors of JSON later!
			//			results = org.apache.commons.lang3.StringEscapeUtils.unescapeJava(results);

			//			System.out.println(results);
			//			results=results.replace("\0", "");//get rid of null characters

			//			System.out.println(results.indexOf("\0"));

			FileWriter fw = new FileWriter(destFolderPath+"/"+fileNumber+".json");
			fw.write(results);
			fw.flush();


		} catch (Exception ex){
			System.out.println(fileNumber);
			ex.printStackTrace();

		}
	}

	void removeField(JsonObject obj,String fieldName) {
		if (obj.has(fieldName)) obj.remove(fieldName);
	}

	void getJSONData(JsonObject obj,String ad) {

		//		System.out.println(ad);

		while (ad.indexOf("<dt>")>-1) {
			String dt=ad.substring(ad.indexOf("<dt>")+4,ad.indexOf("</dt>"));
			dt=dt.replace(":","");
			ad=ad.substring(ad.indexOf("</dt>")+5,ad.length());
			//			System.out.println(ad);
			ad=ad.substring(ad.indexOf(">")+1,ad.length());
			String dd=ad.substring(0,ad.indexOf("</dd>"));
			ad=ad.substring(ad.indexOf("</dd>")+5,ad.length());

			dd=dd.replace("<span class=\"UserEntry\">", "");
			dd=dd.replace("</span>", "");

			obj.addProperty(dt, dd);

			//			System.out.println("\t\t\""+dt+"\" : \""+dd+"\",");
		}
	}


	

	

	

	//Set value of field by taking value from a JSONObject	
	void setValue(String JSONName,String fieldName,JsonObject obj,RecordECHA r) {

		if (obj.get(JSONName)==null) return; //if it's not in there do nothing

		String value=obj.get(JSONName).getAsString();

		try {
			Field myField =r.getClass().getField(fieldName);
			myField.set(r, value);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/*
	 * Set value of field directly without taking it from a JSONObject
	 */
	void setValue(String fieldName,String fieldValue,RecordECHA r) {
		try {
			Field myField =r.getClass().getField(fieldName);
			myField.set(r, fieldValue);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	//TODO- write a method that uses Record class to load the data automatically

	RecordECHA readJSONFileSimple(String folder_path,String record_number) {
		try {
			//X			Administrative data
			//0			Data source
			//			Materials and methods
			//0			Results and discussion
			//X			Applicant's summary and conclusion

			BufferedReader br = new BufferedReader(new FileReader(folder_path+"/"+record_number+".json"));

			JsonParser parser = new JsonParser();

			// Vector<String> items=new Vector<String>();//items for storing
			// list of unique keys

			String data="";

			while (true) {

				String Line = br.readLine();
				if (Line == null)
					break;
				data=data+"\r\n"+Line;
			}

			//			data=data.replace("\0", "");//get rid of null characters
			//			System.out.println(data);

			JsonObject obj = (JsonObject) parser.parse(data);
			Iterator<Entry<String, JsonElement>> iterator = obj.entrySet().iterator();

			RecordECHA r=new RecordECHA();

			r.record_number=record_number;

			this.setValue("chemical_name", "chemical_name",obj, r);
			this.setValue("CAS number", "CAS_number",obj, r);
			this.setValue("EC number", "EC_number",obj, r);


			JsonObject admindata=(JsonObject)obj.get("Administrative data");
			this.setValue("Purpose flag", "purpose_flag",admindata, r);

			this.setValue("Endpoint", "endpoint",admindata, r);

			this.setValue("Study result type", "study_result_type",admindata, r);
			this.setValue("Type of information", "study_result_type",admindata, r);

			this.setValue("Reliability", "reliability",admindata, r);

			//			Endpoint:
			//			    skin sensitisation: in vivo (non-LLNA)
			//			Adequacy of study:
			//			    key study
			//			Rationale for reliability incl. deficiencies:
			//			    other: Fully GLP- and guideline compliant study


			JsonObject matmethods=(JsonObject)obj.get("Materials and methods");

			JsonArray testGuidelines=(JsonArray)matmethods.get("test guidelines");

			for (int i=0;i<testGuidelines.size();i++) {
				JsonObject bob=(JsonObject)testGuidelines.get(i);

				if (bob.get("Guideline")!=null) {
					String guidelinei=bob.get("Guideline").getAsString();

					if (guidelinei.indexOf("OECD Guideline")>-1) {
						guidelinei=guidelinei.replace("OECD Guideline", "").trim();
						this.setValue("OECD_guideline", guidelinei, r);
					}
				}
			}

			JsonArray testMaterials=matmethods.get("test materials").getAsJsonArray();

			for (int i=0;i<testMaterials.size();i++) {
				JsonObject bob=testMaterials.get(i).getAsJsonObject();


				if (bob.get("Identifier")==null) continue;

				String identifier=bob.get("Identifier").getAsString();
				String identity=bob.get("Identity").getAsString();

				if (identifier.equals("CAS number")) {
					this.setValue("Identity", "test_material_CAS_number", bob, r);					
				} else if (identifier.equals("EC number")) {
					this.setValue("Identity", "test_material_EC_number", bob, r);
				} else if (identifier.equals("IUPAC name")) {
					this.setValue("Identity", "test_material_IUPAC_name", bob, r);
				} else if (identifier.equals("EC name")) {
					this.setValue("Identity", "test_material_EC_name", bob, r);
				} else {
					System.out.println(record_number+"/tIdentifier:\t"+identifier);
				}
			}

			this.setValue("Methods", "methods",matmethods, r);
			this.setValue("Type of study", "type_of_study",matmethods, r);
			this.setValue("Species", "species", matmethods, r);
			this.setValue("Details on test material", "test_material_details", matmethods, r);
			this.setValue("Identity of test material same as for substance defined in section 1 (if not read-across)","test_material_same_as_section_one",matmethods,r);


			JsonObject appSumConc=obj.get("Applicant's summary and conclusion").getAsJsonObject();
			this.setValue("Interpretation of results","interpretation_of_results",appSumConc,r);


			//			System.out.println(r);
			//			r.print();
			//			System.out.println(r.getHeader());
			//			System.out.println(r);

			//			System.out.println("");
			//			while (iterator.hasNext()) {
			//				String key = iterator.next();
			//				System.out.println(key);
			//			}

			return r;

		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(record_number);
			return null;
		}
	}




	void readJSONFile(String filepath) {
		try {

			//			Gson gson = new Gson();

			JsonReader reader = new JsonReader(new FileReader(filepath));

			GsonBuilder builder = new GsonBuilder();
			builder.setFieldNamingStrategy(new FieldNamingStrategy() { 
				@Override
				public String translateName(Field field) {
					//use this if we want to rename anything before outputting:
					if (field.getName().equals("CAS_number"))
						return "CAS number";
					else
						return field.getName();
				}
			});
			builder.setPrettyPrinting().serializeNulls();// makes it multiline and readable
			Gson gson = builder.create();

			RecordECHA data = gson.fromJson(reader, RecordECHA.class);


			System.out.println(gson.toJson(data));


		} catch (Exception ex){
			ex.printStackTrace();
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ECHA_Dossier g=new ECHA_Dossier();
		//		g.download();
		//		g.getCASNumbersFromECInventory();

		//		g.downloadReachChemicals();
		//		g.getCASNumbersFromECHAChemicals();
		//		g.downloadFile("https://echa.europa.eu/information-on-chemicals/registered-substances/-/disreg/substance/external/100.003.303", "phenol.html");

		//		g.getListOfSubstancesWithDossiers();//use brute force- check if integer number is a file, times out after a while- too many calls?

		//		g.downloadListOfDossierChemicals();
		//		g.getDossierNumbersFromSummaryFiles();

		//*************************************************************************************
		String folder="AA Dashboard/Data/ECHA";
		String dossierFilePath=folder+"/dossier numbers.txt";
		String reachDossierFolderPath=folder+"/REACH_dossier_data";

		//		String endpoint="Skin sensitization";
		//		String endpointSubFolder="/7/5/2";

		String endpoint="Acute Toxicity Oral";
		String endpointSubFolder="/7/3/2";

		//		g.downloadToxicityDataForDossiers(dossierFilePath, endpoint, endpointSubFolder,reachDossierFolderPath);
		//		g.goThroughToxFiles("REACH_dossier_data/Skin sensitization","Skin sensitisation");

		//***************************************************************************************************

		//		String folder="REACH_dossier_data/Skin sensitization";
		//		int dosNum=1326;// only has 1 record with sparse data
		//		int dosNum=16102;//benzene

		//		String tag="<h2>Skin sensitisation</h2>";
		//		String filePath=folder+"/"+dosNum+".html";
		//		System.out.println(g.haveTag(filePath,tag));

		//		String tag="<h2>Skin sensitisation</h2>";
		//		String folderPath="REACH_dossier_data/Skin sensitization";
		//		String destFolderPath=folderPath+"/no skin sensitization data";
		//		g.moveFilesWithoutTag(folderPath, destFolderPath,tag);

		//		String folderPath="REACH_dossier_data/Skin sensitization";
		//		g.downloadAdditionalStudies(folderPath);

		//		g.deleteFiles("REACH_dossier_data/Skin sensitization/no skin sensitization data");

		//		String f1="";
		//		String folderHTML="AA Dashboard/Data/ECHA/REACH_dossier_data/Skin sensitization/Skin sensitization";
		//		String folderJSON="AA Dashboard/Data/ECHA/REACH_dossier_data/Skin Sensitization/Skin sensitization JSON";

		//		g.parseSensitizationFile("1088",folderHTML,folderJSON);
		//		g.parseSensitizationFile("1096",folderHTML,folderJSON);

		//		g.parseSensitizationFile("1143",folderHTML,folderJSON);//single test material identity
		//		g.parseSensitizationFile("1388",folderHTML,folderJSON);//multiple test material identities

		//		g.parseSensitizationFile("1061",folderHTML,folderJSON);//multiple results
		//		g.parseSensitizationFile("1053",folderHTML,folderJSON);//extra stuff after results
		//		g.parseSensitizationFile("1001",folderHTML,folderJSON);//single result, multiple guidelines

		//		g.parseSensitizationFile("1026",folderHTML,folderJSON);//multiple guidelines, multiple test materials

		//		g.parseSensitizationFile("12528",folderHTML,folderJSON);//match tom luechtfield

		//		g.parseSensitizationFile("15124_002",folderHTML,folderJSON);//error

		//		g.parseSkinStudiesToJSON(folderHTML, folderJSON);

		//		g.readJSONFile(folderJSON+"/12528.json");
		//		g.readJSONFile(folderJSON+"/bob.json");

		//		g.readJSONFileSimple(folderJSON,"12528");
		//		g.readJSONFileSimple(folderJSON,"15124_002");

		//		Record r=g.readJSONFileSimple(folderJSON,"10052");
		//		r.print();
		//		System.out.println(r.getHeader());


		//https://echa.europa.eu/registration-dossier/-/registered-dossier/16102

		//go through all json files and make one flat text file:
		//		g.convertJSONSkinStudiesToTextFile(folderJSON, "REACH_dossier_data/echa skin data.txt");

		//		String f="AA Dashboard\\Data\\ECHA\\REACH_dossier_data";
		//		g.goThroughSkinSensitizationTextFile(f+"/echa skin data.txt",f+"/echa skin data-good records.txt",f+"/echa skin data-bad records.txt");
		//		g.omitDuplicateRecords(f+"/echa skin data-good records.txt", f+"/echa skin data-good records-omit duplicates.txt");
	}

}



