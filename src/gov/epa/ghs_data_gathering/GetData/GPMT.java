package gov.epa.ghs_data_gathering.GetData;

//import javax.rmi.CORBA.Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import gov.epa.ghs_data_gathering.Utilities.MolFileUtilities;
import gov.epa.ghs_data_gathering.Utilities.Utilities;

/**
 * Class for manipulating GPMT data from REACH dossiers
 * 
 * @author Todd Martin
 *
 */
public class GPMT {

	void convertRecordsToOneRecordPerChemical() {

		System.out.println("here");

		String folder="data";
		String inputFilePath=folder+"/skin sens data set.txt";
		String outputFilePath=folder+"/skin sens data set2.txt";
		DecimalFormat df=new DecimalFormat("0");

		try {

			BufferedReader br=new BufferedReader(new FileReader(inputFilePath));
			FileWriter fw=new FileWriter(outputFilePath);

			String header=br.readLine();

			int colCAS= Utilities.FindFieldNumber(header,"CAS final","\t");
			int colTox=Utilities.FindFieldNumber(header,"BinaryScore","\t");

			int sum=0;

			String currentCAS="";
			int count=-1;

			int countGood=0;


			while (true) {
				String Line=br.readLine();

				if (Line==null) {

					fw.write(currentCAS + "\t" + Math.round(sum) + "\r\n"); //write last record

					break;
				}




				LinkedList<String> l=Utilities.Parse3(Line,"\t");

				String strCAS=l.get(colCAS);
				String strTox=l.get(colTox);
				int iTox= Integer.parseInt(strTox);

				if (!strCAS.equals(currentCAS)) {

					double dtox=(double)sum/(double)count;

					if (!currentCAS.equals("")) {

						if (dtox<=0.2 || dtox >= 0.8) {
							long tox=Math.round(dtox);
//							System.out.println(currentCAS + "\t" + dtox + "\t" + count);
							fw.write(currentCAS + "\t" + tox + "\r\n");
							fw.flush();

							countGood++;
						} else {
							//Dont use these- we want good aggreement in scores
							System.out.println(currentCAS + "\t" + dtox + "\t" + count);
						}


					}

					currentCAS=strCAS;
					count=1;
					sum=iTox;

				} else {
					sum+=iTox;
					count++;
				}



			}

			fw.close();

			System.out.println("countGood="+countGood);

		} catch (Exception ex) {
			ex.printStackTrace();
		}


	}
	
	void convertSDFstoMolFiles(String SDFfolder,String molFileFolder) {
		File folder=new File(SDFfolder);
		File [] files=folder.listFiles();
		
		for (int i=0;i<files.length;i++) {
			
			File filei=files[i];
			
			String filename=filei.getName();
			
			if (filename.indexOf(".sdf")==-1) continue;
			
			this.convertSDFtoMolFiles(SDFfolder+"/"+filename, molFileFolder);
			
			System.out.println(files[i].getName());
		}
		
	}

	void createSDFfromMolFiles(String molFileFolder,String outputfilepath) {
		
		File folder=new File(molFileFolder);
		File [] files=folder.listFiles();
		
		Vector<String>casNumbers=new Vector<String>();
		
		
		for (int i=0;i<files.length;i++) {
			
			File filei=files[i];
			
			String filename=filei.getName();
			
			String CAS=filename.substring(0,filename.indexOf("."));
			
			while (CAS.length()<11) {
				CAS="0"+CAS;
			}
			
			casNumbers.add(CAS);
//			System.out.println(CAS);
		}
		
		Collections.sort(casNumbers);
		
		
		
		try {
			
			FileWriter fw=new FileWriter(outputfilepath);
			
			
			for (int i=0;i<casNumbers.size();i++) {
				
				String casi=casNumbers.get(i);
				
				while (casi.indexOf("0")==0) {
					casi=casi.substring(1,casi.length());
				}
				
				BufferedReader br=new BufferedReader(new FileReader(molFileFolder+"/"+casi+".mol"));
				
				while (true) {
					String Line=br.readLine();
					if (Line==null) break;
					
					fw.write(Line+"\r\n");
					
				}
				
//				fw.write("$$$$");
				fw.flush();
				
				System.out.println(casi);
				
			}
			
			
			fw.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	void convertSDFtoMolFiles(String SDFFilePath,String outputFolder) {
//		ToxPredictor.misc.MolFileUtilities m=new ToxPredictor.misc.MolFileUtilities();
		MolFileUtilities.CreateMolFilesFromSDFFile(SDFFilePath, outputFolder, 0, "cas.rn");
	}
	
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GPMT g=new GPMT();
//		g.convertRecordsToOneRecordPerChemical();
		
		String SDFFilePath="data/sdf/50-00-0 to 78-59-1.sdf";
		String outputFolder="data/mol files";
		
//		g.convertSDFtoMolFiles(SDFFilePath, outputFolder);
//		g.convertSDFstoMolFiles("data/sdf", "data/mol files");
//		g.createSDFfromMolFiles("data/mol files", "data/GPMT_chemicals.sdf");
	}

}
