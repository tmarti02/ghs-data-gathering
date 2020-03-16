package gov.epa.ghs_data_gathering.GetData.SkinSensitization;

import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.setting.IOSetting;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesGenerator;


public class SkinSensitizationChembench {

	private void readUsingZipFile(String zipfilepath,String outputSDFFilePath,String textfilepath)  {
		SmilesGenerator sg =SmilesGenerator.unique();

	    try {
	    	
		    final ZipFile file = new ZipFile(zipfilepath);
		    System.out.println("Iterating over zip file : " + zipfilepath);

		    AtomContainerSet acs=new AtomContainerSet();
		    
	        final Enumeration<? extends ZipEntry> entries = file.entries();
	        while (entries.hasMoreElements()) {
	            final ZipEntry entry = entries.nextElement();
	            if (!entry.getName().contains("Visualization/Structures/")) continue;
//	            System.out.println(entry.getName());
	            InputStream fis=file.getInputStream(entry);
	           IAtomContainer ac=extractEntry(fis);
//	            System.out.println(ac.getAtomCount());
	            acs.addAtomContainer(ac);
//	            if (true) break;
	        }
	        
	        FileWriter fwSDF = new FileWriter(outputSDFFilePath);
	        FileWriter fwText = new FileWriter(textfilepath);
	        
//	        MDLV2000Writer m=new MDLV2000Writer(fw);
	        SDFWriter writer = new SDFWriter(fwSDF);
	        
	        
	        IOSetting iosABT=writer.getSetting("WriteAromaticBondTypes");
	        iosABT.setSetting("true");

//	        Collection<IOSetting>settings=writer.getSettings();
//	        for (IOSetting ios:settings) {
//	        	System.out.println(ios.getName()+"\t"+ios.getDefaultSetting());
//	        }
	        
//	        m.setWriteAromaticBondTypes(true);
	        
	       ArrayList<String>fields=new ArrayList<String>();
	       fields.add("Compound name");
	       fields.add("CASRN");
	       fields.add("LLNA result");
	       fields.add("LLNA class");
	       fields.add("LLNA reference");
	       fields.add("Chembench_Name");

	       for (int i=0;i<fields.size();i++) {
	    	   fwText.write(fields.get(i));
	    	   if (i<fields.size()-1) {
	    		   fwText.write("\t");
	    	   } 
	       }
	       
	       fwText.write("\tsmiles");	       
	       fwText.write("\r\n");
	       
	       fwText.flush();
	        
	       for (int i=0;i<acs.getAtomContainerCount();i++) {
	    	   try {
	    		   IAtomContainer aci=acs.getAtomContainer(i);
	    		   
	    		   String smiles="";
	    		   try {
	    			   smiles = sg.create(aci);
	    		   } catch (Exception smilesex) {
	    			   System.out.println("Cant gen smiles for "+aci.getProperty("CASRN"));
	    		   }
	    		   
	    		   //for (Object key : aci.getProperties().keySet()) {
//	    			    System.out.println(key);
//	    			}
	    		   
	    		   for (int j=0;j<fields.size();j++) {
//	    			   System.out.println(fields.get(j)+"\t"+aci.getProperty(fields.get(j)));
	    			   
	    			   if (aci.getProperty(fields.get(j))==null) {
	    				   fwText.write("N/A");
	    			   } else {
	    				   fwText.write((String)aci.getProperty(fields.get(j)));   
	    			   }
	    			   
	    			   if (j<fields.size()-1) {
	    				   fwText.write("\t");
	    			   } 
	    		   }
	    		   fwText.write("\t"+smiles);
	    		   fwText.write("\r\n");
	    		   writer.write(aci);


	    	   } catch (Exception ex) {
	    		   ex.printStackTrace();
	    	   }
	       }
	        
	        
	        fwSDF.close();
	        
//	        System.out.printf("Zip file %s extracted successfully in %s", filename, OUTPUT_DIR);
	        
	        file.close();
	        
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }

	}
	
	IAtomContainer extractEntry(InputStream is) {
		try {
			  MDLV2000Reader in=new MDLV2000Reader(is);
			  IAtomContainer  container=in.read(SilentChemObjectBuilder.getInstance().newInstance(IAtomContainer.class));
//			  System.out.println(container.getAtomCount()+"\t"+container.getBondCount());
			  in.close();
			  return container;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String folder="AA Dashboard\\Data\\ECHA\\REACH_dossier_data\\Skin Sensitization\\chembench data";
		String filename="Skin_Sensitization_Animal_LLNA_data.zip";
		String zipfilepath=folder+"\\"+filename;
		String sdffilepath=folder+"\\Skin_Sensitization_Animal_LLNA_data.sdf";
		String textfilepath=folder+"\\Skin_Sensitization_Animal_LLNA_data.txt";
		SkinSensitizationChembench ssc=new SkinSensitizationChembench();
		ssc.readUsingZipFile(zipfilepath, sdffilepath,textfilepath);
		
	}

}
